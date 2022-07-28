package io.camassia.spring.dbunit.api.extensions

import io.camassia.spring.dbunit.api.dataset.Cell
import io.camassia.spring.dbunit.api.dataset.Overrides

/**
 * Handles Cells with Templated content e.g.
 * Cell("name", "\[SOME_VALUE]")
 *
 * With an override set up. If an override is missing the column will be left as is.
 */
object TemplatedCellMappingExtension : CellMappingExtension {

    override fun applyTo(table: String, cell: Cell, overrides: Overrides): Cell {
        val value = cell.value
        return if (value != null && value is String && value.startsWith('[') && value.endsWith(']') && overrides.containsKey(value)) {
            cell.mapValue { overrides[value] }
        } else cell
    }

}