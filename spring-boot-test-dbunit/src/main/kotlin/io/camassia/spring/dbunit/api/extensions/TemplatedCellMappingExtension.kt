package io.camassia.spring.dbunit.api.extensions

import io.camassia.spring.dbunit.api.dataset.Cell
import io.camassia.spring.dbunit.api.dataset.Overrides
import org.slf4j.LoggerFactory

/**
 * Handles Cells with Templated content e.g.
 * Cell("name", "\[SOME_VALUE]")
 *
 * With an override set up. If an override is missing the column will be left as is.
 */
object TemplatedCellMappingExtension : CellMappingExtension {

    private val log = LoggerFactory.getLogger(TemplatedCellMappingExtension::class.java)
    private val regex = Regex("^\\[[a-zA-Z_\\-0-9]+\\]\$")

    override fun applyTo(table: String, cell: Cell, overrides: Overrides): Cell {
        val value = cell.value
        return if (value != null && value is String && regex.matches(value) && overrides.containsKey(value)) {
            cell.mapValue { overrides[value]?.also { log.debug("Replacing Templated val $value with override: '$it'") } }
        } else cell
    }

}