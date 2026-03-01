package starry.saturn.util

import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder

public fun decodeUri(uri: String): String =
    URLDecoder.decode(uri, Charsets.UTF_8)


public fun decodeUri(uri: URI): String = decodeUri(uri.toString())

public fun encodeUri(uri: String): String = URLEncoder.encode(uri, Charsets.UTF_8)

public fun String.removeHead(vararg texts: String): String {
    for (text in texts) {
        if (startsWith(text)) return removePrefix(text)
    }
    return this
}

public fun String.removeTail(vararg texts: String): String {
    for (text in texts) {
        if (endsWith(text)) return removeSuffix(text)
    }
    return this
}
