package io.camassia.spring.dbunit.api.customization

import io.camassia.spring.dbunit.api.dataset.Cell

data class TableDefaults(val table: String, val overrides: Set<Cell>) {
    constructor(table: String, vararg overrides: Cell): this(table, overrides.toSet())
    constructor(table: String, vararg overrides: Pair<String, Any?>): this(table, overrides.map { (key, value) -> Cell(key, value) }.toSet())
}