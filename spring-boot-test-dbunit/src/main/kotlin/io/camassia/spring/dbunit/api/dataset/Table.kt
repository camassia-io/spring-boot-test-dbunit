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

    override fun toString(): String = (
            """
            |Table(
            |  name=$name,
            |  ${rows.joinToString("\n")}
            |)
            """.trimMargin()
            )

}