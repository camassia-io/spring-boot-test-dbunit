package io.camassia.spring.dbunit.api.extensions

import io.camassia.spring.dbunit.api.dataset.Cell
import io.camassia.spring.dbunit.api.dataset.Overrides

class Extensions(
    private val cellMapping: List<CellMappingExtension>,
    val defaults: Defaults
) {

    fun applyToCell(table: String, cell: Cell, overrides: Overrides) = cellMapping
        .fold(cell) { acc, extension ->
            extension.applyTo(table, acc, overrides)
        }

}