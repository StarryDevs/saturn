package starry.saturn

import org.slf4j.Logger
import starry.saturn.context.bean.AnnotationConfigApplicationContext
import starry.saturn.context.bean.ConfigurableApplicationContext
import starry.saturn.context.property.PropertyResolver
import starry.saturn.util.getLogger
import kotlin.reflect.KClass
import kotlin.time.measureTime

public data class SaturnApplicationBuilder(
    val banner: Banner = EmptyBanner,
    val applicationClass: KClass<*>? = null,
    val basePackages: Set<String> = emptySet(),
    val classLoaders: Set<ClassLoader> = emptySet(),
    val context: SaturnApplicationBuilder.() -> ConfigurableApplicationContext = {
        AnnotationConfigApplicationContext.create(
            this.applicationClass?.let(::setOf) ?: emptySet(),
            this.basePackages,
            classLoaders = this.classLoaders,
            propertyResolverInit = this.propertyResolverInit
        )
    },
    val propertyResolverInit: PropertyResolver.() -> Unit = {},
) {
    public fun banner(banner: Banner): SaturnApplicationBuilder = copy(banner = banner)
    public fun applicationClass(applicationClass: KClass<*>): SaturnApplicationBuilder = copy(applicationClass = applicationClass)
    public fun basePackages(vararg packages: String): SaturnApplicationBuilder = copy(basePackages = this.basePackages + packages)
    public fun classLoaders(vararg loaders: ClassLoader): SaturnApplicationBuilder = copy(classLoaders = this.classLoaders + loaders)
    public fun propertyResolverInit(init: PropertyResolver.() -> Unit): SaturnApplicationBuilder = copy(propertyResolverInit = init)

    public fun build(): SaturnApplication = SaturnApplication(this)

}

public class SaturnApplication(private val builder: SaturnApplicationBuilder = SaturnApplicationBuilder()) {

    public val logger: Logger = getLogger()

    public fun start(args: Array<String>): ConfigurableApplicationContext {
        logger.info("Starting SaturnApplication with arguments: ${args.joinToString(", ")}")
        Saturn.arguments = args
        builder.banner.printBanner()
        val context: ConfigurableApplicationContext
        val time = measureTime {
            context = builder.context(builder)
            Saturn.configurableApplicationContext = context
        }
        Runtime.getRuntime()
            .addShutdownHook(Thread(::shutdownHook, "Saturn Shutdown Hook"))
        logger.info("Application context loaded in ${time.inWholeMilliseconds}ms.")
        return context
    }

    private fun shutdownHook() {
        logger.info("Shutting down application context...")
        Saturn.configurableApplicationContext.close()
        logger.info("Application context shut down successfully.")
    }

}
