package io.camassia.spring.dbunit.api.dataset

/**
 * An instance of a Table for use with DatabaseTester
 *
 * @param name The table name
 * @param rows An array of rows
 * @see Row
 */
class Table(val name: String, val rows: Collection<Row>) {
    constructor(name: String, vararg rows: Row) : this(name, rows.toList())

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
}