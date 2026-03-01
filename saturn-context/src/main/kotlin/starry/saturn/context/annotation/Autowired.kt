package starry.saturn.context.annotation

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
public annotation class Autowired(val enable: Boolean = true)
