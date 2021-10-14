package io.camassia.spring.dbunit.api.dataset

class File(
    val path: String,
    val overrides: Set<Cell> = emptySet()
) {
    constructor(path: String, vararg overrides: Cell): this(path, overrides.toSet())
}