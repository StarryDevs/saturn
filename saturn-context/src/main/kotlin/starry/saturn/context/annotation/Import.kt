package starry.saturn.context.annotation

import kotlin.reflect.KClass

@MustBeDocumented
@Repeatable
@Retention(AnnotationRetention.RUNTIME)
public annotation class Import(val value: Array<KClass<*>> = [], val classes: Array<String> = [])
