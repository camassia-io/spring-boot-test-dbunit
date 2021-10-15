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
)
