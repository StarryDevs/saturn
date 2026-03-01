package starry.saturn.context.annotation.stereotype

import java.lang.annotation.Inherited

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@Component
@Inherited
public annotation class Repository
