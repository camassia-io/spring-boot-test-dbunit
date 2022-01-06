package io.camassia.spring.dbunit.api.dataset.builder

import io.camassia.spring.dbunit.api.dataset.Cell
import io.camassia.spring.dbunit.api.dataset.DecoratedDataSet
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
    caseSensitive: Boolean = false
) {
    private val nameOf: (String) -> String = if (caseSensitive) { n -> n } else { n: String -> n.toUpperCase() }
    private val defaults: Map<String, Set<Cell>> = extensions.defaults.groupBy { nameOf(it.table) }.mapValues { (_, defaults) ->
        defaults.flatMap { it.overrides }.toSet()
    }

    fun joinAndApplyExtensions(dataSets: Map<IDataSet, Map<String, Any?>>): DecoratedDataSet {
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
        overrides: Map<String, Any?>
    ): DecoratedDataSet {
        if (dataSet is DecoratedDataSet) return dataSet

        val output = CachedDataSet()
        val consumer = BufferedConsumer(output)
        consumer.startDataSet()

        val tables = dataSet.iterator()
        while (tables.next()) {
            val table = tables.table
            val tableName = nameOf(table.tableMetaData.tableName)
            val columnsFromDataSet = table.tableMetaData.columns.associateBy { nameOf(it.columnName) }
            val columnsFromDefaults = (this.defaults[tableName] ?: emptySet()).associateBy({ nameOf(it.key) }, { Column(it.key, DataType.UNKNOWN) })
            val columns: Map<String, Column> = if(columnsFromDataSet.isNotEmpty()) columnsFromDefaults + columnsFromDataSet else emptyMap()
            val primaryKeys = table.tableMetaData.primaryKeys

            consumer.startTable(DefaultTableMetaData(tableName, columns.values.toTypedArray(), primaryKeys))

            if (table.rowCount == 0) {
                consumer.row(arrayOfNulls(columns.size))
            } else {
                (0 until table.rowCount).forEach { rowIndex ->
                    val cells: Array<out Any?> = columns.keys.map(nameOf).map { columnName ->
                        val rawCellValue: Any? = if (table.tableMetaData.columns.any { nameOf(it.columnName) == columnName }) table.getValue(rowIndex, columnName) else null
                        val fallback = if(rawCellValue == null) defaults[tableName]?.find { nameOf(it.key) == columnName }?.value else null
                        // Process all extensions and fallback to a default value if the cell is not set
                        extensions.cellMapping
                            .fold(Cell(columnName, rawCellValue)) { acc, extension ->
                                extension.applyTo(tableName, acc, overrides)
                            }.mapValue { value ->
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
        overrides: Map<String, Any?>
    ): DecoratedDataSet {
        val dataSet = CachedDataSet()
        val consumer = BufferedConsumer(dataSet)
        consumer.startDataSet()

        tables.groupBy { nameOf(it.name) }.mapValues { (_, tables) ->
            tables.flatMap { it.rows }
        }.forEach { (tableName, rows) ->
            val defaults: Set<Cell> = this.defaults[tableName] ?: emptySet()
            val columns: Map<String, Column> = (rows.flatMap { it.cells.toList() }.map { it.key } + defaults.map { it.key })
                .toSet()
                .associateBy({ nameOf(it) }, { Column(it, DataType.UNKNOWN) })
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
                        val rawCellValue: Any? = row.cells.find { nameOf(it.key) == columnName }?.value
                        val fallback = if(rawCellValue == null) defaults.find { nameOf(it.key) == columnName }?.value else null
                        // Process all extensions and fallback to a default value if the cell is not set
                        extensions.cellMapping
                            .fold(Cell(columnName, rawCellValue)) { acc, extension ->
                                extension.applyTo(tableName, acc, overrides)
                            }.mapValue { value ->
                                value ?: fallback
                            }
                    }
                    .map { it.value }
                    .toTypedArray()
                consumer.row(cells)
            }

            consumer.endTable()
        }

        consumer.endDataSet()
        return DecoratedDataSet(dataSet)
    }
}