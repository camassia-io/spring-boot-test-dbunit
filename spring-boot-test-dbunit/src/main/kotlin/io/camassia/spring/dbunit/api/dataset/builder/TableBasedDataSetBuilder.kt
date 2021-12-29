package io.camassia.spring.dbunit.api.dataset.builder

import io.camassia.spring.dbunit.api.dataset.Cell
import io.camassia.spring.dbunit.api.dataset.DecoratedDataSet
import io.camassia.spring.dbunit.api.dataset.Table
import io.camassia.spring.dbunit.api.extensions.Extensions
import org.dbunit.dataset.CachedDataSet
import org.dbunit.dataset.Column
import org.dbunit.dataset.DefaultTableMetaData
import org.dbunit.dataset.IDataSet
import org.dbunit.dataset.datatype.DataType
import org.dbunit.dataset.stream.BufferedConsumer

internal class TableBasedDataSetBuilder(
    private val extensions: Extensions
) {
    private val defaults: Map<String, Set<Cell>> = extensions.defaults.groupBy { it.table }.mapValues { (_, defaults) ->
        defaults.flatMap { it.overrides }.toSet()
    }

    fun applyExtensions(
        dataSet: IDataSet
    ): DecoratedDataSet {
        if (dataSet is DecoratedDataSet) return dataSet

        val output = CachedDataSet()
        val consumer = BufferedConsumer(output)
        consumer.startDataSet()

        val tables = dataSet.iterator()
        while (tables.next()) {
            val table = tables.table
            val tableName = table.tableMetaData.tableName
            val columns = table.tableMetaData.columns

            consumer.startTable(DefaultTableMetaData(tableName, columns))

            if (table.rowCount == 0) {
                consumer.row(arrayOfNulls(columns.size))
            } else {
                (0 until table.rowCount).forEach { rowIndex ->
                    val cells: Array<out Any?> = columns.map { column ->
                        val columnName = column.columnName
                        val rawCellValue: Any? = table.getValue(rowIndex, columnName) ?: defaults[tableName]?.find { it.key == columnName }
                        extensions.cellMapping
                            .fold(Cell(columnName, rawCellValue)) { acc, extension ->
                                extension.applyTo(tableName, acc)
                            }
                    }
                        .map { it.value }
                        .toTypedArray()
                    consumer.row(cells)
                }
            }
            consumer.endTable()
        }
        consumer.endDataSet()
        return DecoratedDataSet(output)
    }

    fun build(
        tables: Collection<Table>
    ): DecoratedDataSet {
        val dataSet = CachedDataSet()
        val consumer = BufferedConsumer(dataSet)
        consumer.startDataSet()

        tables.groupBy { it.name }.mapValues { (_, tables) ->
            tables.flatMap { it.rows }
        }.forEach { (tableName, rows) ->
            val defaults: Set<Cell> = this.defaults[tableName] ?: emptySet()
            val columns: Map<String, Column> = (rows.flatMap { it.cells.toList() }.map { it.key } + defaults.map { it.key })
                .distinct()
                .associateBy({ it }, { Column(it, DataType.UNKNOWN) })
            consumer.startTable(
                DefaultTableMetaData(
                    tableName,
                    columns.values.toTypedArray()
                )
            )

            if (rows.isEmpty()) consumer.row(arrayOfNulls(columns.size))
            else rows.forEach { row ->
                val cells: Array<out Any?> = columns.keys
                    .map { columnName: String ->
                        val cellSupplied: Cell? = row.cells.find { it.key == columnName } ?: defaults.find { it.key == columnName }
                        cellSupplied?.let { cell: Cell ->
                            extensions.cellMapping
                                .fold(cell) { acc, extension ->
                                    extension.applyTo(tableName, acc)
                                }
                        }
                    }
                    .map { it?.value }
                    .toTypedArray()
                consumer.row(cells)
            }

            consumer.endTable()
        }

        consumer.endDataSet()
        return DecoratedDataSet(dataSet)
    }
}