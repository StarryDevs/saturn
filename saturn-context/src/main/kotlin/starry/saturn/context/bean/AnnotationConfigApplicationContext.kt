package starry.saturn.context.bean

import kotlinx.coroutines.runBlocking
import starry.saturn.Saturn
import starry.saturn.context.Dependency
import starry.saturn.context.DependencyList
import starry.saturn.context.annotation.*
import starry.saturn.context.annotation.stereotype.Component
import starry.saturn.context.annotation.stereotype.Indexed
import starry.saturn.context.property.PropertyResolver
import starry.saturn.io.ResourceResolver
import starry.saturn.util.allParameters
import starry.saturn.util.findAnnotation
import java.lang.reflect.Array
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KParameter
import kotlin.reflect.full.*
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.jvmName
import kotlin.reflect.jvm.kotlinFunction

public fun interface ClassResolver {

    public companion object : ClassResolver {
        override fun resolveClass(name: String): KClass<*> = Class.forName(name).kotlin
    }

    public fun resolveClass(name: String): KClass<*>

}

public open class AnnotationConfigApplicationContext private constructor() : ConfigurableApplicationContext() {

    public val propertyResolver: PropertyResolver = PropertyResolver(this)

    public companion object {

        public fun create(
            classes: Set<KClass<*>> = setOf(),
            basePackages: Set<String> = setOf(),
            classLoaders: Set<ClassLoader> = setOf(),
            propertyResolverInit: PropertyResolver.() -> Unit = {}
        ): AnnotationConfigApplicationContext {
            val scanClassLoaders = buildSet {
                addAll(classLoaders)
                add(Saturn::class.java.classLoader)
                add(Thread.currentThread().contextClassLoader)
            }
            val scanClasses = buildSet {
                val basePackages = buildSet {
                    add(Saturn::class.java.packageName)
                    addAll(classes.map { it.java.packageName })
                    addAll(basePackages)
                }
                for (basePackage in basePackages) {
                    val scanResult = ResourceResolver(scanClassLoaders, basePackage).scan {
                        if (!it.name.endsWith(".class")) null
                        else it.name.replace("/", ".").removeSuffix(".class")
                    }.mapNotNull {
                        runCatching { Class.forName(it).kotlin }.getOrNull()
                    }.filter {
                        !it.java.isAnnotation
                    }.filter { findAnnotation(it.annotations, Indexed::class) != null }
                    addAll(scanResult)
                }
            }
            return AnnotationConfigApplicationContext(classes + scanClasses, ClassResolver, propertyResolverInit)
        }

    }

    protected var scope: ScopedValue.Carrier = ScopedValue.where(BeanFactory.SCOPED_BEAN_FACTORY, this)
    protected val beans: MutableMap<String, BeanDefinition> = mutableMapOf()

    public constructor(classes: Set<KClass<*>>, classResolver: ClassResolver, propertyResolverInit: PropertyResolver.() -> Unit = {}) : this() {
        registerSingleton(this)
        registerSingleton(this.propertyResolver.apply(propertyResolverInit))
        val found = classes.flatMap { beanClass ->
            beanClass.findAnnotations<Import>().flatMap {
                it.classes.map(classResolver::resolveClass) + it.value
            }
        }.toSet() + classes
        for (beanClass in found) {
            val objectInstance = beanClass.objectInstance
            val classBeanAnnotation = beanClass.findAnnotation<Bean>()
            val classComponentAnnotation = findAnnotation(beanClass.annotations, Component::class)
            val beanDefinition = BeanDefinition(
                beanClass,
                classBeanAnnotation?.name?.takeUnless(String::isEmpty),
                objectInstance,
                symbol = classBeanAnnotation?.symbol?.takeUnless(Symbol::isValid),
                constructor = if (classComponentAnnotation != null) beanClass.constructors.singleOrNull() else null,
                initMethodName = classBeanAnnotation?.initMethod?.takeUnless(String::isEmpty),
                destroyMethodName = classBeanAnnotation?.destroyMethod?.takeUnless(String::isEmpty),
                initMethod = beanClass.memberFunctions.firstOrNull { it.hasAnnotation<PostConstruct>() },
                destroyMethod = beanClass.memberFunctions.firstOrNull { it.hasAnnotation<PreDestroy>() },
            )
            beans[beanDefinition.name] = beanDefinition
            for (member in beanClass.java.methods.mapNotNull { it.kotlinFunction }) {
                val methodBeanAnnotation = member.findAnnotation<Bean>() ?: continue
                val methodBeanDefinition = BeanDefinition(
                    member.returnType.classifier as KClass<*>,
                    methodBeanAnnotation.name.takeUnless(String::isEmpty)
                        ?: "${beanDefinition.name}::${member.name}",
                    symbol = methodBeanAnnotation.symbol.takeUnless(Symbol::isValid),
                    constructor = member,
                    initMethodName = methodBeanAnnotation.initMethod.takeUnless(String::isEmpty),
                    destroyMethodName = methodBeanAnnotation.destroyMethod.takeUnless(String::isEmpty)
                )
                beans[methodBeanDefinition.name] = methodBeanDefinition
            }
        }
        val dependencyList = DependencyList(this, beans.values.toSet())
        dependencyList.build()
        dependencyList.get().forEachIndexed { idx, bean ->
            bean.order = idx
        }
        val sortedBeans = beans.values.sortedBy { it.order }
        for (bean in sortedBeans) {
            if (bean.constructor == null) continue
            if (!bean.constructed) {
                bean.value = invokeScoped {
                    runBlocking { invokeMethod(bean.constructor) }
                }
                bean.constructed = true
                val initMethod = bean.getInitMethod()
                initMethod?.callBy(
                    buildMap { put(initMethod.instanceParameter ?: return@buildMap, bean.value) }
                )
            }
            autowire(bean)
        }
    }

    override fun containsBean(name: String): Boolean = beans.containsKey(name)
    override fun containsBean(type: KClass<*>, symbol: Symbol?): Boolean =
        beans.values.any { it.type.isSubclassOf(type) && (!symbol.isValid() || it.symbol.isSameAs(symbol)) }

    override fun getBeanDefinition(type: KClass<*>, symbol: Symbol?): BeanDefinition =
        beans.values.firstOrNull { it.type.isSubclassOf(type) && (!symbol.isValid() || it.symbol.isSameAs(symbol)) }
            ?: throw NoSuchBeanException(type, symbol)

    override fun getBeanDefinition(name: String): BeanDefinition =
        beans[name] ?: throw NoSuchBeanException(Qualifier(name))

    override fun getBeanDefinitions(type: KClass<*>, symbol: Symbol?): Set<BeanDefinition> =
        beans.values.filter { it.type.isSubclassOf(type) && (!symbol.isValid() || it.symbol.isSameAs(symbol)) }.toSet()

    override fun registerSingleton(singleton: Any, name: String?, symbol: Symbol?): BeanDefinition {
        val beanDefinition = BeanDefinition(
            singleton::class,
            name ?: singleton::class.jvmName,
            value = singleton,
            symbol = symbol
        )
        beanDefinition.constructed = true
        beanDefinition.autowired = true
        beans[beanDefinition.name] = beanDefinition
        return beanDefinition
    }

    protected open fun autowire(type: KClass<*>, annotations: List<Annotation>): Any? {
        val javaClass = type.java
        val beanDependency = Dependency.of(type, annotations)
        val resolved = beanDependency.resolve(this).map(BeanDefinition::value)
        return if (javaClass.isArray) {
            Array.newInstance(javaClass.componentType, resolved.size).also { array ->
                for ((idx, element) in resolved.withIndex()) {
                    Array.set(array, idx, element)
                }
            }
        } else {
            if (resolved.size > 1) throw NoSuchBeanException("Expected exactly one bean for dependency ${beanDependency}, but found ${resolved.size}")
            if (resolved.isEmpty()) throw NoSuchBeanException("No beans found for dependency $beanDependency")
            resolved.single()
        }
    }

    protected open fun autowire(beanDefinition: BeanDefinition) {
        if (beanDefinition.autowired) return
        val value = beanDefinition.value ?: return
        for (member in value::class.memberProperties) {
            if (member !is KMutableProperty<*>) continue
            if (canAutowire(member.annotations) != true) continue
            member.isAccessible = true
            member.setter.isAccessible = true
            member.setter.call(value, autowire(member.returnType.classifier as KClass<*>, member.annotations))
        }
        beanDefinition.autowired = true
        if (value is InitializingBean) {
            value.afterPropertiesSet()
        }
    }

    protected open fun canAutowire(annotations: List<Annotation>): Boolean? {
        val autowired = findAnnotation(annotations, Autowired::class)
        if (autowired != null) return autowired.enable
        return null
    }

    protected open suspend fun <T> invokeMethod(callable: KCallable<T>): T {
        val arguments = mutableMapOf<KParameter, Any?>()
        for (parameter in callable.allParameters) {
            val isOptional = parameter.isOptional
            val isNullable = parameter.type.isMarkedNullable
            try {
                val type = parameter.type.classifier as KClass<*>
                if (canAutowire(parameter.annotations) == false) continue
                val value = autowire(type, parameter.annotations)
                if (isOptional && !isNullable) continue
                arguments[parameter] = value
            } catch (exception: NoSuchBeanException) {
                if (!isOptional) throw exception
            }
        }
        return if (callable.isSuspend) {
            callable.callSuspendBy(arguments)
        } else {
            callable.callBy(arguments)
        }
    }

    protected open fun <T> invokeScoped(callable: () -> T): T =
        scope.call(ScopedValue.CallableOp { callable() })

    override fun close() {
        beans.values.sortedByDescending { it.order }.forEach {
            if (it.value == null) return@forEach
            val method = it.getDestroyMethod() ?: return@forEach
            method.isAccessible = true
            method.callBy(
                buildMap { put(method.instanceParameter ?: return@buildMap, it.value) }
            )
        }
    }

}
