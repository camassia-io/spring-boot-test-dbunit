package io.camassia.spring.dbunit.api.dataset

import java.io.InputStream
import java.net.URL

abstract class LocalResourceDataSetLoader: DataSetLoader {

    fun getResourceUrl(clazz: Class<*>, path: String): URL = clazz.getResource(path) ?: throw AssertionError("Resource [$path] did not exist")
    fun getResourceInputStream(clazz: Class<*>, path: String): InputStream = clazz.getResourceAsStream(path) ?: throw AssertionError("Resource [$path] did not exist")

}