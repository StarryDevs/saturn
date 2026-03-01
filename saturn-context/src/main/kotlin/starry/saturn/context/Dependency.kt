package starry.saturn.context

import starry.saturn.context.annotation.Autowired
import starry.saturn.context.annotation.Qualifier
import starry.saturn.context.annotation.Symbol
import starry.saturn.context.annotation.stringify
import starry.saturn.context.bean.BeanDefinition
import starry.saturn.context.bean.BeanFactory
import starry.saturn.util.allParameters
import starry.saturn.util.findAnnotation
import kotlin.reflect.KClass
import kotlin.reflect.KParameter

public sealed class Dependency {

    public companion object {
        private fun toBeanType(type: KClass<*>): KClass<*> =
            if (type.java.isArray) type.java.componentType.kotlin else type

        public fun of(annotation: Qualifier): Dependency = QualifiedDependency(annotation)
        public fun of(type: KClass<*>, annotations: List<Annotation> = listOf()): Dependency {
            val qualifier = annotations.filterIsInstance<Qualifier>().firstOrNull()
            return if (qualifier != null) {
                of(qualifier)
            } else {
                TypedDependency(toBeanType(type), annotations.filterIsInstance<Symbol>().firstOrNull())
            }
        }

        public fun of(parameter: KParameter): Dependency =
            of(parameter.type.classifier as KClass<*>, parameter.annotations)

        public fun of(beanDefinition: BeanDefinition): Set<Dependency> {
            if (beanDefinition.constructor == null) return emptySet()
            return beanDefinition.constructor.allParameters.filter {
                findAnnotation(it.annotations, Autowired::class)?.enable != false
            }.map(::of).toSet()
        }
    }

    public abstract fun resolve(beanFactory: BeanFactory): Set<BeanDefinition>

    private class TypedDependency(private val type: KClass<*>, private val symbol: Symbol? = null) : Dependency() {
        override fun resolve(beanFactory: BeanFactory): Set<BeanDefinition> =
            beanFactory.getBeanDefinitions(type, symbol)

        override fun toString(): String =
            "bean typed ${type.qualifiedName}${symbol?.stringify()?.let { " with symbol '$it'" } ?: ""}"
    }

    private class QualifiedDependency(private val qualifier: Qualifier) : Dependency() {
        override fun resolve(beanFactory: BeanFactory): Set<BeanDefinition> = setOf(beanFactory.getBean(qualifier.name))

        override fun toString(): String =
            "bean named '${qualifier.name}'"
    }

}
