package io.camassia.spring.dbunit.api.io

import java.io.InputStream
import java.net.URL

open class DefaultLocalResourceLoader : ResourceLoader {
    override fun getResourceUrl(clazz: Class<*>, path: String): URL = clazz.getResource(path) ?: throw AssertionError("Resource [$path] did not exist")
    override fun getResourceInputStream(clazz: Class<*>, path: String): InputStream = clazz.getResourceAsStream(path) ?: throw AssertionError("Resource [$path] did not exist")
}