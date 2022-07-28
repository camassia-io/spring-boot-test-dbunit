package io.camassia.spring.dbunit.api.dataset

/**
 * Class responsible for tracking what overrides are used
 */
class Overrides(private val underlying: Map<String, Any?> = emptyMap()) {

    private val used = mutableSetOf<String>()

    constructor(pairs: Pair<String, Any?>): this(mapOf(pairs))

    fun containsKey(key: String): Boolean = underlying.containsKey(key)

    operator fun get(key: String): Any? = underlying[key].also { used.add(key) }

    override fun toString(): String = underlying.entries.joinToString(",","[","]"){ (k, v) -> "$k=$v" }

    fun unused() = Overrides(underlying.minus(used))

    fun isNotEmpty() = underlying.isNotEmpty()
    fun isEmpty() = underlying.isEmpty()
}