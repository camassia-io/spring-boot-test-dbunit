package io.camassia.spring.dbunit.api.dataset

class File(
    val path: String,
    val overrides: Set<CellOverride> = emptySet()
) {

    constructor(path: String, vararg overrides: CellOverride): this(path, overrides.toSet())

    class CellOverride(
        val key: String,
        value: Any?
    ) {
        val value: Any? = value?.takeIf { it != "[null]" }

        operator fun component1() = key
        operator fun component2() = value
    }
}