package io.camassia.spring.dbunit.api.dataset

/**
 * An instance of a Row for use with Table
 *
 * @param cells A map of column names to values
 * @see Table
 */
class Row(val cells: Collection<Cell>) {

    fun toMap(): Map<String, Any?> = cells.associateBy({ it.key }, { it.value })

    constructor(vararg cells: Pair<String, Any?>) : this(cells.map { Cell(it.first, it.second) })
    constructor(vararg cells: Cell) : this(cells.toSet())

    fun mapCells(fn: (Cell) -> Cell) = Row(cells.map(fn))

    override fun toString(): String = "Row(${cells.toList().joinToString(",") { (k, v) -> "$k=$v" }})"

}