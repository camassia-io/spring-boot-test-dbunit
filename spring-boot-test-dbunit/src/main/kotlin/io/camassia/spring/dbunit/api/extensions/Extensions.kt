package io.camassia.spring.dbunit.api.extensions

import io.camassia.spring.dbunit.api.customization.TableDefaults
import io.camassia.spring.dbunit.api.dataset.Cell

class Extensions(
    val cellMapping: List<CellMappingExtension>,
    val defaults: List<TableDefaults>
) {
    fun defaults(table: String): Set<Cell> = defaults.filter { it.table.equals(table, true) }.flatMap { it.overrides }.toSet()
    fun defaults(tables: Set<String>): Set<Cell> = tables.map { it.toLowerCase() }.let {
        defaults.filter { default ->
            it.contains(default.table.toLowerCase())
        }.flatMap { it.overrides }.toSet()
    }

}