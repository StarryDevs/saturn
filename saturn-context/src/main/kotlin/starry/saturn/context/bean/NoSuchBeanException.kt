package starry.saturn.context.bean

import starry.saturn.context.annotation.Qualifier
import starry.saturn.context.annotation.Symbol
import kotlin.reflect.KClass

public open class NoSuchBeanException public constructor(message: String, cause: Throwable? = null) : RuntimeException(message, cause) {

    public constructor(name: Qualifier, cause: Throwable? = null) : this(message = "No bean named '${name.name}' is defined", cause)
    public constructor(type: KClass<*>, symbol: Symbol? = null, cause: Throwable? = null) : this(message = "No bean of type '${type.qualifiedName}'${symbol?.let { " with symbol '$it'" } ?: ""} is defined", cause)

}
