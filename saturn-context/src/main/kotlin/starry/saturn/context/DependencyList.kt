package starry.saturn.context

import starry.saturn.context.bean.BeanDefinition
import starry.saturn.context.bean.BeanFactory

public class DependencyList(private val beanFactory: BeanFactory, private val beans: Set<BeanDefinition>) {

    private val visited = mutableSetOf<BeanDefinition>()
    private val visiting = mutableSetOf<BeanDefinition>()
    private val localOutput = mutableListOf<BeanDefinition>()

    private fun visit(bean: BeanDefinition) {
        if (bean in visited) return
        if (bean in visiting) throw IllegalStateException("Circular dependency detected: ${visiting.joinToString(" -> ") { it.name }} -> ${bean.name}")
        visiting.add(bean)
        for (dependency in Dependency.of(bean)) {
            for (depBean in dependency.resolve(beanFactory)) {
                if (depBean in beans) visit(depBean)
            }
        }
        visiting.remove(bean)
        visited.add(bean)
        localOutput.add(bean)
    }

    public fun build() {
        beans.forEach(::visit)
    }

    public fun get(): List<BeanDefinition> = localOutput

}
