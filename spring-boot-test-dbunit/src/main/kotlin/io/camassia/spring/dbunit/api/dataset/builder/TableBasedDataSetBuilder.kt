package io.camassia.spring.dbunit.api.dataset.builder

import io.camassia.spring.dbunit.api.dataset.Cell
import io.camassia.spring.dbunit.api.dataset.DecoratedDataSet
import io.camassia.spring.dbunit.api.dataset.Overrides
import io.camassia.spring.dbunit.api.dataset.Table
import io.camassia.spring.dbunit.api.extensions.Extensions
import org.dbunit.dataset.CachedDataSet
import org.dbunit.dataset.Column
import org.dbunit.dataset.CompositeDataSet
import org.dbunit.dataset.DefaultTableMetaData
import org.dbunit.dataset.IDataSet
import org.dbunit.dataset.datatype.DataType
import org.dbunit.dataset.stream.BufferedConsumer

internal class TableBasedDataSetBuilder(
    private val extensions: Extensions,
    private val ignoreCase: Boolean = false
) {

    private val keyOf: (String) -> (String) = if(ignoreCase) { i -> i.uppercase() } else { i -> i }

    fun joinAndApplyExtensions(dataSets: Map<IDataSet, Overrides>): DecoratedDataSet {
        return if (dataSets.size == 1) dataSets.entries.first().let { (dataSet, overrides) ->
            applyExtensions(dataSet, overrides)
        } else {
            DecoratedDataSet(
                CompositeDataSet(
                    dataSets.map { (dataSet, overrides) ->
                        applyExtensions(dataSet, overrides)
                    }.toTypedArray()
                )
            )
        }
    }

    fun applyExtensions(
        dataSet: IDataSet,
        overrides: Overrides
    ): DecoratedDataSet {
        if (dataSet is DecoratedDataSet) return dataSet

        val output = CachedDataSet()
        val consumer = BufferedConsumer(output)
        consumer.startDataSet()

        val tables = dataSet.iterator()
        while (tables.next()) {
            val table = tables.table
            val tableName = table.tableMetaData.tableName
            val primaryKeys = table.tableMetaData.primaryKeys

            if (table.rowCount == 0) {
                consumer.startTable(DefaultTableMetaData(tableName, emptyArray(), primaryKeys))
                consumer.row(emptyArray())
            } else {
                val columnsFromDataSet = table.tableMetaData.columns.toSet()
                val columnsFromDefaults = this.extensions.defaults.forTable(tableName).map { Column(it.key, DataType.UNKNOWN) }
                val columns: Array<Column> = (columnsFromDefaults + columnsFromDataSet).distinctBy { keyOf(it.columnName) }.toTypedArray()

                consumer.startTable(DefaultTableMetaData(tableName, columns, primaryKeys))

                (0 until table.rowCount).forEach { rowIndex ->
                    val cells: Array<out Any?> = columns.map { column ->
                        val rawCellValue: Any? = columnsFromDataSet.find { it.columnName.equals(column.columnName, ignoreCase) }?.let { table.getValue(rowIndex, it.columnName) }
                        val fallback = if(rawCellValue == null) extensions.defaults.forColumn(tableName, column.columnName)?.value else null
                        // Process all extensions and fallback to a default value if the cell is not set
                        extensions
                            .applyToCell(tableName, Cell(column.columnName, rawCellValue), overrides)
                            .mapValue { value ->
                                value ?: fallback
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
        tables: Collection<Table>,
        overrides: Overrides
    ): DecoratedDataSet {
        val dataSet = CachedDataSet()
        val consumer = BufferedConsumer(dataSet)
        consumer.startDataSet()

        tables.groupBy { keyOf(it.name) }.mapValues { (_, tables) ->
            tables.flatMap { it.rows }
        }.forEach { (tableName, rows) ->
            if (rows.isEmpty()) {
                consumer.startTable(DefaultTableMetaData(tableName, emptyArray()))
                consumer.row(emptyArray())
            } else {
                val defaults: Set<Cell> = this.extensions.defaults.forTable(tableName)
                val columnsFromDataSet = rows.flatMap { it.cells }.map { Column(it.key, DataType.UNKNOWN) }
                val columnsFromDefaults = this.extensions.defaults.forTable(tableName).map { Column(it.key, DataType.UNKNOWN) }
                val columns: Array<Column> = (columnsFromDefaults + columnsFromDataSet).distinctBy { keyOf(it.columnName) }.toTypedArray()

                consumer.startTable(DefaultTableMetaData(tableName, columns))

                rows.forEach { row ->
                    val cells: Array<out Any?> = columns
                        .map { column: Column ->
                            val rawCellValue: Any? = row.cells.find { it.key.equals(column.columnName, ignoreCase) }?.value
                            val fallback = if(rawCellValue == null) defaults.find { it.key.equals(column.columnName, ignoreCase) }?.value else null
                            // Process all extensions and fallback to a default value if the cell is not set
                            extensions
                                .applyToCell(tableName, Cell(column.columnName, rawCellValue), overrides)
                                .mapValue { value ->
                                    value ?: fallback
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
        return DecoratedDataSet(dataSet)
    }
}
