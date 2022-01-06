package io.camassia.spring.dbunit.api.extensions

import io.camassia.spring.dbunit.api.DbUnitException
import io.camassia.spring.dbunit.api.dataset.Cell

/**
 * Handles Cells with Templated content e.g.
 * Cell("name", "\[SOME_VALUE]")
 *
 * With an override set up. If an override is missing the column will be left as is.
 */
object InitialTemplatedCellMappingExtension : CellMappingExtension {

    override fun applyTo(table: String, cell: Cell, overrides: Map<String, Any?>): Cell {
        val value = cell.value
        return if (value != null && value is String && value.startsWith('[') && value.endsWith(']') && overrides.containsKey(value)) {
            cell.mapValue { overrides[value] }
        } else cell
    }

}

/**
 * Handles Cells with Templated content e.g.
 * Cell("name", "\[SOME_VALUE]")
 *
 * With an override set up. If an override is missing an exception will be thrown.
 */
object FinalTemplatedCellMappingExtension : CellMappingExtension {

    override fun applyTo(table: String, cell: Cell, overrides: Map<String, Any?>): Cell {
        val value = cell.value
        return if (value != null && value is String && value.startsWith('[') && value.endsWith(']')) {
            cell.mapValue {
                overrides[value] ?: throw DbUnitException(
                    "Expected an Override for $value but there wasn't one configured. Overrides available were: [${overrides.entries.joinToString { (k, v) -> "$k=$v" }}]"
                )
            }
        } else cell
    }

}