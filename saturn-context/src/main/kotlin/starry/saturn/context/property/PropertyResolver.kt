package starry.saturn.context.property

import starry.saturn.context.bean.BeanFactory

public class PropertyResolver(private val beanFactory: BeanFactory) {
    public val properties: MutableMap<String, Any?> = mutableMapOf()
}
