package io.camassia.spring.dbunit.api.extensions

import io.camassia.spring.dbunit.api.customization.TableDefaults
import io.camassia.spring.dbunit.api.dataset.Cell

class Extensions(
    val cellMapping: List<CellMappingExtension>,
    defaults: List<TableDefaults>
) {
    val defaults = Defaults(defaults)

    class Defaults(
        private val defaults: List<TableDefaults>
    ) {
        fun forTable(table: String, ignoreCase: Boolean): Set<Cell> = defaults.filter { it.table.equals(table, ignoreCase) }.flatMap { it.overrides }.toSet()
        fun forColumn(table: String, column: String, ignoreCase: Boolean): Cell? = forTable(table, ignoreCase).find { it.key.equals(column, ignoreCase) }
    }

}