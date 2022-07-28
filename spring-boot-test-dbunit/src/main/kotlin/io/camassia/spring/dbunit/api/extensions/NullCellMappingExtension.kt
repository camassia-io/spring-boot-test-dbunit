package io.camassia.spring.dbunit.api.extensions

import io.camassia.spring.dbunit.api.dataset.Cell
import io.camassia.spring.dbunit.api.dataset.Overrides

/**
 * Handles Cells with null values e.g.
 * Cell("name", "\[null]")
 * and maps its value to null.
 */
object NullCellMappingExtension : CellMappingExtension {
    override fun applyTo(table: String, cell: Cell, overrides: Overrides): Cell = if(cell.value == "[null]") cell.mapValue { null } else cell
}