package io.camassia.spring.dbunit.api.annotations

/**
 * An instance of a Table for use with DatabaseSetup and DatabaseTeardown
 *
 * @param name The table name
 * @param rows An array of rows
 * @see Row
 */
annotation class Table(
    val name: String,
    vararg val rows: Row
) {
    /**
     * An instance of a Row for use with Table
     *
     * @param cells An array of cells
     * @see Table
     * @see Cell
     */
    annotation class Row(vararg val cells: Cell)

    /**
     * A Column-Value pair for use with Row
     *
     * @param name The column name
     * @param value The value for that column on that Row
     * @see Row
     */
    annotation class Cell(val name: String, val value: String)
}
