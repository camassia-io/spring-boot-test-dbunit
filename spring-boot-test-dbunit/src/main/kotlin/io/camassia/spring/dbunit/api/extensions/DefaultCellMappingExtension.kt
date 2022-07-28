package io.camassia.spring.dbunit.api.extensions

import io.camassia.spring.dbunit.api.DbUnitException
import io.camassia.spring.dbunit.api.dataset.Cell
import io.camassia.spring.dbunit.api.dataset.Overrides
import org.slf4j.LoggerFactory

/**
 * Handles Cells with Templated content e.g.
 * Cell("name", "\[SOME_VALUE]")
 *
 * With an override set up. If an override is missing this extension will look in global defaults for a fallback. Failing that an exception will be thrown.
 */
class DefaultTemplatedCellMappingExtension(private val defaults: Defaults) : CellMappingExtension {

    private val regex = Regex("^\\[[a-zA-Z_\\-0-9]+\\]\$")

    override fun applyTo(table: String, cell: Cell, overrides: Overrides): Cell {
        val value = cell.value
        return if (value != null && value is String && regex.matches(value)) {
            cell.mapValue {
                overrides[value]
                    ?.also { log.debug("Replacing Templated val $value with override: '$it'") }
                    ?: run {
                        defaults.forColumn(table, cell.key)?.also { log.debug("Replacing Templated val $value with default: '${it.value}'") }
                            ?: throw DbUnitException(
                                "Expected an Override for $value but there wasn't one configured. Overrides available were: ${overrides}"
                            )
                    }.value
            }
        } else cell
    }

    companion object {
        private val log = LoggerFactory.getLogger(DefaultTemplatedCellMappingExtension::class.java)
    }

}