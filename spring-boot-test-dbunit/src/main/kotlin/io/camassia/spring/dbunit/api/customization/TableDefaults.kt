package io.camassia.spring.dbunit.api.customization

import io.camassia.spring.dbunit.api.dataset.File

data class TableDefaults(val table: String, val overrides: Set<File.CellOverride>) {
    constructor(table: String, vararg overrides: File.CellOverride): this(table, overrides.toSet())
}