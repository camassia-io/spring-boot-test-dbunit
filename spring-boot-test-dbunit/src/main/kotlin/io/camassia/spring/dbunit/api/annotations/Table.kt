package io.camassia.spring.dbunit.api.annotations

annotation class Table(
    val name: String,
    vararg val rows: Row
) {
    annotation class Row(vararg val cells: Cell)
    annotation class Cell(val name: String, val value: String)
}
