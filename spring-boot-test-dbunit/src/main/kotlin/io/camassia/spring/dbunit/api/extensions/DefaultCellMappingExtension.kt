package io.camassia.spring.dbunit.api.extensions

import io.camassia.spring.dbunit.api.DbUnitException
import io.camassia.spring.dbunit.api.dataset.Cell

class DefaultTemplatedCellMappingExtension(private val defaults: Defaults) : CellMappingExtension {

    override fun applyTo(table: String, cell: Cell, overrides: Map<String, Any?>): Cell {
        val value = cell.value
        return if (value != null && value is String && value.startsWith('[') && value.endsWith(']')) {
            cell.mapValue {
                overrides[value] ?: defaults.forColumn(table, cell.key)?.value ?: throw DbUnitException(
                    "Expected an Override for $value but there wasn't one configured. Overrides available were: ${overrides.entries.joinToString { (k, v) -> "$k=$v" }}"
                )
            }
        } else cell
    }

}