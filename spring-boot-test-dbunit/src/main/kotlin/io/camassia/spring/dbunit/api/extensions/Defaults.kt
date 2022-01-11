package io.camassia.spring.dbunit.api.extensions

import io.camassia.spring.dbunit.api.customization.TableDefaults
import io.camassia.spring.dbunit.api.dataset.Cell

class Defaults(
    private val defaults: List<TableDefaults>,
    private val ignoreCase: Boolean
) {
    fun forTable(table: String): Set<Cell> = defaults.filter { it.table.equals(table, ignoreCase) }.flatMap { it.overrides }.toSet()
    fun forColumn(table: String, column: String): Cell? = forTable(table).find { it.key.equals(column, ignoreCase) }
}