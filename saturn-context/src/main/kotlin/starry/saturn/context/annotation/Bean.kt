package starry.saturn.context.annotation


@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
public annotation class Bean(
    val name: String = "",
    val symbol: Symbol = Symbol(),
    val initMethod: String = "",
    val destroyMethod: String = ""
)
