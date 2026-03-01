package starry.saturn.context.annotation

@Retention(AnnotationRetention.RUNTIME)
public annotation class Symbol(val namespace: String = "", val name: String = "")

public fun Symbol?.isValid(): Boolean {
    return this != null && namespace.isNotEmpty() && name.isNotEmpty()
}

public fun Symbol?.isSameAs(other: Symbol?): Boolean {
    if (!isValid() && !other.isValid()) return true
    if (!isValid() || !other.isValid()) return false
    return this?.namespace == other!!.namespace && name == other.name
}

public fun Symbol.stringify(): String? {
    return if (isValid()) "$namespace:$name" else null
}
