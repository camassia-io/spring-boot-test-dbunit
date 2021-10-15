package io.camassia.spring.dbunit.api.annotations

/**
 * An instance of a Row for use with Table
 *
 * @param cells An array of cells
 * @see Table
 * @see Cell
 */
annotation class Row(vararg val cells: Cell)