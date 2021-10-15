package io.camassia.spring.dbunit.api.dataset.builder

import io.camassia.spring.dbunit.api.customization.TableDefaults
import io.camassia.spring.dbunit.api.dataset.Cell
import io.camassia.spring.dbunit.api.dataset.Row
import io.camassia.spring.dbunit.api.dataset.Table
import org.dbunit.dataset.CachedDataSet
import org.dbunit.dataset.Column
import org.dbunit.dataset.DefaultTableMetaData
import org.dbunit.dataset.IDataSet
import org.dbunit.dataset.datatype.DataType
import org.dbunit.dataset.stream.BufferedConsumer

internal class TableBasedDataSetBuilder(
    tables: Collection<Table>,
    defaults: Collection<TableDefaults>
) {
    private val tables: Map<String, Collection<Row>> = tables.groupBy { it.name }.mapValues { (_, tables) ->
        tables.flatMap { it.rows }
    }
    private val defaults: Map<String, Set<Cell>> = defaults.groupBy { it.table }.mapValues { (_, defaults) ->
        defaults.flatMap { it.overrides }.toSet()
    }.filterKeys { this.tables.containsKey(it) }

    fun build(): IDataSet {
        val dataSet = CachedDataSet()
        val consumer = BufferedConsumer(dataSet)
        consumer.startDataSet()
        this.tables.forEach { (name, rows) ->
            val defaults: Set<Cell> = this.defaults[name] ?: emptySet()
            val columns: Map<String, Column> = (rows.flatMap { it.cells.toList() }.map { it.first } + defaults.map { it.key })
                .distinct()
                .associateBy({ it }, { Column(it, DataType.UNKNOWN) })
            consumer.startTable(
                DefaultTableMetaData(
                    name,
                    columns.values.toTypedArray()
                )
            )

            if(rows.isEmpty()) consumer.row(arrayOfNulls(columns.size))
            else rows.forEach { row ->
                val cells: Array<out Any?> = columns
                    .map { (n,v) -> Cell(n,v) }
                    .map { cell ->
                        if(row.cells.containsKey(cell.key)) row.cells[cell.key] else defaults.find { it.key == cell.key }
                    }.toTypedArray()
                consumer.row(cells)
            }

            consumer.endTable()
        }

        consumer.endDataSet()
        return dataSet
    }
}