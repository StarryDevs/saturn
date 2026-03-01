package starry.saturn.context.bean

import starry.saturn.context.annotation.PostConstruct
import starry.saturn.context.annotation.PreDestroy
import starry.saturn.context.annotation.Symbol
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.jvmName

public class BeanDefinition(
    public val type: KClass<*>,
    name: String? = null,
    public var value: Any? = null,
    public val symbol: Symbol? = null,
    public val constructor: KCallable<*>? = type.constructors.singleOrNull(),
    private val initMethodName: String? = null,
    private val destroyMethodName: String? = null,
    private val initMethod: KFunction<*>? = null,
    private val destroyMethod: KFunction<*>? = null,
) {

    public var constructed: Boolean = false
    public var autowired: Boolean = false

    public val name: String = name ?: type.jvmName

    internal var order = -1

    override fun toString(): String =
        "BeanDefinition(name='$name', type=${type.qualifiedName}, symbol=$symbol, constructor=${constructor?.name}, initMethod=${initMethod?.name ?: initMethodName}, destroyMethod=${destroyMethod?.name ?: destroyMethodName})"

    public fun getInitMethod(): KCallable<*>? =
        initMethod
            ?: initMethodName?.let { name -> type.members.firstOrNull { it.name == name } }
            ?: value?.let { instance -> instance::class.members.firstOrNull { it.hasAnnotation<PostConstruct>() } }

    public fun getDestroyMethod(): KCallable<*>? =
        destroyMethod
            ?: destroyMethodName?.let { name -> type.members.firstOrNull { it.name == name } }
            ?: value?.let { instance -> instance::class.members.firstOrNull { it.hasAnnotation<PreDestroy>() } }

}
