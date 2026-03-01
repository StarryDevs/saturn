package starry.saturn.util

import kotlin.reflect.KClass

public fun <T : Annotation> findAnnotation(target: List<Annotation>, annotationClass: KClass<T>): T? {
    val topAnnotation = target.filterIsInstance(annotationClass.java).singleOrNull()
    if (topAnnotation != null) return topAnnotation
    for (annotation in target) {
        val annotationType = annotation.annotationClass
        if (annotationType.java.packageName != "java.lang.annotation" && annotationType.java.packageName != "kotlin.annotation") {
            val found = findAnnotation(annotationType.annotations, annotationClass) ?: continue
            return found
        }
    }
    return null
}
