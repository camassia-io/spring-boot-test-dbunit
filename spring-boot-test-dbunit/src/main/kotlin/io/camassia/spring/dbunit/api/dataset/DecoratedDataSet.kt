package io.camassia.spring.dbunit.api.dataset

import org.dbunit.dataset.IDataSet
import org.dbunit.dataset.ITable
import org.dbunit.dataset.ITableIterator
import org.dbunit.dataset.ITableMetaData

/**
 * Decorator class to indicate a DataSet that has had extensions etc applied already
 */
class DecoratedDataSet(private val underlying: IDataSet): IDataSet {
    override fun getTableNames(): Array<String> = underlying.tableNames

    override fun getTableMetaData(tableName: String): ITableMetaData = underlying.getTableMetaData(tableName)

    override fun getTable(tableName: String): ITable = underlying.getTable(tableName)

    override fun getTables(): Array<ITable> = underlying.tables

    override fun iterator(): ITableIterator = underlying.iterator()

    override fun reverseIterator(): ITableIterator = underlying.reverseIterator()

    override fun isCaseSensitiveTableNames(): Boolean = underlying.isCaseSensitiveTableNames
}