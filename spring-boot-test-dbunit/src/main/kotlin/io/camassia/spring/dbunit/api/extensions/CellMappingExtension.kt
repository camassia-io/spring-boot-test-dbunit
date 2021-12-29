package io.camassia.spring.dbunit.api.extensions

import io.camassia.spring.dbunit.api.dataset.Cell

interface CellMappingExtension {

    fun applyTo(table: String, cell: Cell): Cell

}