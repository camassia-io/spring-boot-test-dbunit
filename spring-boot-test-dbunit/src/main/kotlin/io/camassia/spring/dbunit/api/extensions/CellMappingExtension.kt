package io.camassia.spring.dbunit.api.extensions

import io.camassia.spring.dbunit.api.dataset.Cell

interface CellMappingExtension {

    /**
     * @param table The name of the database table the Cell is on
     * @param cell The Cell to be mapped
     * @param overrides Key-Value pairs of Overrides
     */
    fun applyTo(
        table: String,
        cell: Cell,
        overrides: Map<String, Any?>
    ): Cell

}