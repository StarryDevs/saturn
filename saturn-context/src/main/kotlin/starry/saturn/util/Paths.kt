package starry.saturn.util

import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Path

public object Paths {

    public fun fromJar(basePackagePath: String, jar: URI): Path =
        runCatching { FileSystems.getFileSystem(jar) }
            .getOrElse { FileSystems.newFileSystem(jar, mapOf<String, Any?>()) }
            .getPath(basePackagePath)

}

