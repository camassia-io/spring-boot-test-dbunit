package io.camassia.spring.dbunit.api.extensions

import io.camassia.spring.dbunit.api.DbUnitException
import io.camassia.spring.dbunit.api.dataset.Cell
import io.camassia.spring.dbunit.api.dataset.Overrides

/**
 * Handles Cells with Templated content e.g.
 * Cell("name", "\[SOME_VALUE]")
 *
 * With an override set up. If an override is missing this extension will look in global defaults for a fallback. Failing that an exception will be thrown.
 */
class DefaultTemplatedCellMappingExtension(private val defaults: Defaults) : CellMappingExtension {

    private val regex = Regex("^\\[[a-zA-Z_]+\\]\$")

    override fun applyTo(table: String, cell: Cell, overrides: Overrides): Cell {
        val value = cell.value
        return if (value != null && value is String && regex.matches(value)) {
            cell.mapValue {
                overrides[value] ?: run {
                    defaults.forColumn(table, cell.key) ?: throw DbUnitException(
                        "Expected an Override for $value but there wasn't one configured. Overrides available were: ${overrides}"
                    )
                }.value
            }
        } else cell
    }

}