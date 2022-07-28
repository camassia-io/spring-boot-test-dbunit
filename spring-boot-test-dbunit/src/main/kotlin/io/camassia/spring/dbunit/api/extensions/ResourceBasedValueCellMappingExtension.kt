package io.camassia.spring.dbunit.api.extensions

import io.camassia.spring.dbunit.api.dataset.Cell
import io.camassia.spring.dbunit.api.dataset.Overrides
import io.camassia.spring.dbunit.api.io.ResourceLoader
import org.slf4j.LoggerFactory

/**
 * Handles Cells with File based content e.g.
 * Cell("name", "[file:filename.txt]")
 */
class ResourceBasedValueCellMappingExtension(
    private val resourceLoader: ResourceLoader
) : CellMappingExtension {

    override fun applyTo(table: String, cell: Cell, overrides: Overrides): Cell {
        val value = cell.value

        return if (value != null && value is String && value.startsWith("[file:") && value.endsWith("]")) {
            value.substring(6, value.length - 1).let { path ->
                log.debug("Replacing Templated val $value with contents of file: '$path'")
                resourceLoader.getResourceAsString(ResourceBasedValueCellMappingExtension::class.java, path)
            }.let { content -> cell.mapValue { content } }
        } else cell
    }

    companion object {
        private val log = LoggerFactory.getLogger(ResourceBasedValueCellMappingExtension::class.java)
    }
}