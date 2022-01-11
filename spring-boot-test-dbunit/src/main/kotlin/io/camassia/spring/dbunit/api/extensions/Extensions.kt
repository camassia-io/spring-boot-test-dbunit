package io.camassia.spring.dbunit.api.extensions

import io.camassia.spring.dbunit.api.dataset.Cell

class Extensions(
    private val cellMapping: List<CellMappingExtension>,
    val defaults: Defaults
) {

    fun applyToCell(table: String, cell: Cell, overrides: Map<String, Any?>) = cellMapping
        .fold(cell) { acc, extension ->
            extension.applyTo(table, acc, overrides)
        }

}