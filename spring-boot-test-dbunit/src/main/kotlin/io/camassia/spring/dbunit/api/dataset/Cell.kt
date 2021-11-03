package io.camassia.spring.dbunit.api.dataset

class Cell(
    val key: String,
    value: Any?
) {
    val value: Any? = value?.takeIf { it != "[null]" }

    constructor(pair: Pair<String, Any>): this(pair.first, pair.second)

    operator fun component1() = key
    operator fun component2() = value
    override fun toString(): String = "$key=$value"


}