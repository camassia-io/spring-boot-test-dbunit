package io.camassia.spring.dbunit.api.extensions

import io.camassia.spring.dbunit.api.dataset.Cell
import io.camassia.spring.dbunit.api.io.ResourceLoader

/**
 * Handles Cells with File based content e.g.
 * Cell("name", "[file:filename.txt]")
 */
class ResourceBasedValueCellMappingExtension(
    private val resourceLoader: ResourceLoader
) : CellMappingExtension {

    override fun applyTo(table: String, cell: Cell, overrides: Map<String, Any?>): Cell {
        val value = cell.value

        return if (value != null && value is String && value.startsWith("[file:") && value.endsWith("]")) {
            value.substring(6, value.length - 1).let { path ->
                resourceLoader.getResourceAsString(ResourceBasedValueCellMappingExtension::class.java, path)
            }.let { content -> cell.mapValue { content } }
        } else cell
    }
}