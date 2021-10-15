package io.camassia.spring.dbunit.api.annotations

/**
 * A File based DataSet for use with DatabaseSetup/DatabaseTeardown
 *
 * @param name The file name
 * @param overrides An array of overrides to apply to that file
 * @see DatabaseSetup
 * @see DatabaseTeardown
 * @see Override
 */
annotation class File(
    val name: String,
    vararg val overrides: Cell
)