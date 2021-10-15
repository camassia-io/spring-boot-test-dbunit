package io.camassia.spring.dbunit.api.dataset

/**
 * An instance of a Row for use with Table
 *
 * @param cells A map of column names to values
 * @see Table
 */
class Row(val cells: Map<String, Any>) {
    constructor(vararg cells: Pair<String, Any>) : this(cells.toMap())
    constructor(vararg cells: Cell) : this(cells.filter { it.value != null }.associate { (name, value) -> name to value!! })
}