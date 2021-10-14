package io.camassia.spring.dbunit.api.dataset

class Table(val name: String, val rows: Collection<Row>) {
    constructor(name: String, vararg rows: Row) : this(name, rows.toList())

    class Row(val cells: Map<String, Any>) {
        constructor(vararg cells: Pair<String, Any>) : this(cells.toMap())
    }
}