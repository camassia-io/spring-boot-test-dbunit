package io.camassia.spring.dbunit.api.dataset

class Cell(
    val key: String,
    val value: Any?
) {
    constructor(pair: Pair<String, Any>): this(pair.first, pair.second)

    fun mapValue(fn: (Any?) -> Any?) = Cell(key, fn(value))

    operator fun component1() = key
    operator fun component2() = value
    override fun toString(): String = "$key=$value"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Cell) return false
        if (key != other.key) return false
        if (value != other.value) return false
        return true
    }
    override fun hashCode(): Int {
        var result = key.hashCode()
        result = 31 * result + (value?.hashCode() ?: 0)
        return result
    }

}