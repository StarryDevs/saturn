package starry.saturn.context.bean

import starry.saturn.context.annotation.Symbol

public abstract class ConfigurableApplicationContext : BeanFactory, AutoCloseable {

    public abstract fun registerSingleton(singleton: Any, name: String? = null, symbol: Symbol? = null): BeanDefinition

}
