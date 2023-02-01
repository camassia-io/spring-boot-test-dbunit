package io.camassia.spring.dbunit.api

import org.slf4j.LoggerFactory

object NestedClassUtil {

    private val log = LoggerFactory.getLogger(NestedClassUtil::class.java)

    fun getHierarchy(clazz: Class<*>): List<Class<*>> {
        val hierarchy = mutableListOf<Class<*>>()
        val builder = StringBuilder()
        val className = clazz.name
        for (i in className.indices) {
            val char = className[i]
            if (char == '$' || i == className.length) {
                safelyFetchClass(builder.toString())?.also {
                    hierarchy.add(it)
                }
            }
            builder.append(char)
        }
        safelyFetchClass(builder.toString())?.also {
            hierarchy.add(it)
        }
        return hierarchy
    }

    private fun safelyFetchClass(name: String): Class<*>? = try {
        Class.forName(name)
    } catch (throwable: ClassNotFoundException) {
        log.warn("Invalid classname [$name]. Please avoid using '$' within class names.")
        null
    }
}
