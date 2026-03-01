package starry.saturn

import starry.saturn.context.bean.ConfigurableApplicationContext
import java.util.Properties

public object Saturn {

    private val meta = Properties().apply { load(Saturn::class.java.getResourceAsStream("/META-INF/saturn.properties")) }

    public val version: String = meta.getProperty("version")

    public lateinit var arguments: Array<String>
    public lateinit var configurableApplicationContext: ConfigurableApplicationContext

}
