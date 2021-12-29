package io.camassia.spring.dbunit.api.io

import java.io.InputStream
import java.net.URL

interface ResourceLoader {

    fun getResourceUrl(clazz: Class<*>, path: String): URL
    fun getResourceInputStream(clazz: Class<*>, path: String): InputStream = getResourceUrl(clazz, path).openStream()
    fun getResourceAsString(clazz: Class<*>, path: String): String = getResourceUrl(clazz, path).readText(Charsets.UTF_8)

}