package starry.saturn.context.bean

import starry.saturn.context.annotation.Symbol
import kotlin.reflect.KClass

public interface BeanFactory {

    public companion object {
        @JvmStatic
        public val SCOPED_BEAN_FACTORY: ScopedValue<BeanFactory> = ScopedValue.newInstance<BeanFactory>()
    }

    /**
     * 根据名称获取 Bean 定义
     *
     * @param name Bean 的名称
     * @return Bean 定义
     * @throws NoSuchBeanException 如果没有找到指定名称的 Bean
     */
    public fun getBeanDefinition(name: String): BeanDefinition

    /**
     * 根据名称获取 Bean 实例
     *
     * @param name Bean 的名称
     * @return Bean 实例
     * @throws NoSuchBeanException 如果没有找到指定名称的 Bean
     */
    @Suppress("UNCHECKED_CAST")
    public fun <T : Any> getBean(name: String): T = getBeanDefinition(name).value as T

    /**
     * 根据类型获取 Bean 定义
     *
     * @param type Bean 的类型
     * @return Bean 定义
     * @throws NoSuchBeanException 如果没有找到指定类型的 Bean
     */
    public fun getBeanDefinition(type: KClass<*>, symbol: Symbol? = null): BeanDefinition

    /**
     * 根据类型获取 Bean 定义
     *
     * @param type Bean 的类型
     * @return Bean 定义
     * @throws NoSuchBeanException 如果没有找到指定类型的 Bean
     */
    @Suppress("UNCHECKED_CAST")
    public fun <T : Any> getBean(type: KClass<T>, symbol: Symbol? = null): T =
        getBeanDefinition(type, symbol).value as T

    /**
     * 根据类型获取 Bean 定义
     *
     * @param type Bean 的类型
     * @throws NoSuchBeanException 如果没有找到指定名称和类型的 Bean
     */
    public fun getBeanDefinitions(type: KClass<*>, symbol: Symbol? = null): Set<BeanDefinition>

    /**
     * 根据类型获取 Bean
     *
     * @param type Bean 的类型
     * @throws NoSuchBeanException 如果没有找到指定名称和类型的 Bean
     */
    @Suppress("UNCHECKED_CAST")
    public fun <T : Any> getBeans(type: KClass<T>, symbol: Symbol? = null): Set<T> =
        getBeanDefinitions(type, symbol).map { it.value as T }.toSet()

    /**
     * 判断 Bean 是否存在
     *
     * @param name Bean 的名称
     */
    public fun containsBean(name: String): Boolean

    /**
     * 判断 Bean 是否存在
     *
     * @param type Bean 的类型
     */
    public fun containsBean(type: KClass<*>, symbol: Symbol? = null): Boolean

}

/**
 * 获取当前作用域内的 BeanFactory 实例
 */
public fun beanFactory(): BeanFactory = BeanFactory.SCOPED_BEAN_FACTORY.get()
