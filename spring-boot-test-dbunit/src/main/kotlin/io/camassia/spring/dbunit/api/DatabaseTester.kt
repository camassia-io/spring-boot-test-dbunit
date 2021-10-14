package io.camassia.spring.dbunit.api

import io.camassia.spring.dbunit.api.connection.ConnectionSupplier
import io.camassia.spring.dbunit.api.customization.ConnectionModifier
import io.camassia.spring.dbunit.api.customization.DatabaseOperation
import io.camassia.spring.dbunit.api.customization.TableDefaults
import io.camassia.spring.dbunit.api.dataset.Cell
import io.camassia.spring.dbunit.api.dataset.DataSetLoader
import io.camassia.spring.dbunit.api.dataset.File
import io.camassia.spring.dbunit.api.dataset.Table
import io.camassia.spring.dbunit.api.dataset.builder.TableBasedDataSetBuilder
import org.dbunit.AbstractDatabaseTester
import org.dbunit.database.DatabaseConfig
import org.dbunit.database.DatabaseConnection
import org.dbunit.database.IDatabaseConnection
import org.dbunit.dataset.CompositeDataSet
import org.dbunit.dataset.IDataSet
import org.dbunit.dataset.ITable
import org.dbunit.dataset.ReplacementDataSet
import kotlin.reflect.KClass

/**
 * Acts as a Wrapper around DbUnits IDatabaseTester + adds extra utility methods
 */
open class DatabaseTester(
    private val connectionSupplier: ConnectionSupplier,
    private val config: DatabaseConfig,
    private val connectionModifier: ConnectionModifier,
    private val dataSetLoader: DataSetLoader,
    schema: String? = null,
    defaults: List<TableDefaults> = emptyList()
) : AbstractDatabaseTester(schema) {

    private val defaults = defaults.associateBy { it.table }

    /**
     * Safe way of using a connection & then freeing it back up again after your work is done
     */
    @Suppress("UsePropertyAccessSyntax")
    fun <T> usingConnection(body: (IDatabaseConnection) -> T): T {
        val connection = getConnection()
        try {
            return body(connection)
        } finally {
            connection.close()
        }
    }

    /**
     * Loads DataSets using the DataSet Loader
     */
    fun givenDataSet(
        clazz: KClass<*>,
        filePath1: String,
        vararg filePaths: String,
        operation: DatabaseOperation = DatabaseOperation.CLEAN_INSERT
    ) = givenDataSet(clazz.java, (listOf(filePath1) + filePaths).map { File(it) }, operation)

    /**
     * Loads DataSets using the DataSet Loader
     */
    fun givenDataSet(
        clazz: Class<*>,
        filePath1: String,
        vararg filePaths: String,
        operation: DatabaseOperation = DatabaseOperation.CLEAN_INSERT
    ) = givenDataSet(clazz, (listOf(filePath1) + filePaths).map { File(it) }, operation)


    /**
     * Loads DataSets using the DataSet Loader
     */
    fun givenDataSet(
        clazz: KClass<*>,
        file1: File,
        vararg files: File,
        operation: DatabaseOperation = DatabaseOperation.CLEAN_INSERT
    ) = givenDataSet(clazz.java, listOf(file1) + files, operation)

    /**
     * Loads DataSets using the DataSet Loader
     */
    fun givenDataSet(
        clazz: KClass<*>,
        files: Collection<File>,
        operation: DatabaseOperation = DatabaseOperation.CLEAN_INSERT
    ) = givenDataSet(clazz.java, files, operation)

    /**
     * Loads DataSets using the DataSet Loader
     */
    fun givenDataSet(
        clazz: Class<*>,
        file1: File,
        vararg files: File,
        operation: DatabaseOperation = DatabaseOperation.CLEAN_INSERT
    ) = givenDataSet(clazz, listOf(file1) + files, operation)

    /**
     * Loads DataSets using the DataSet Loader
     */
    @Suppress("UsePropertyAccessSyntax")
    fun givenDataSet(
        clazz: Class<*>,
        files: Collection<File>,
        operation: DatabaseOperation = DatabaseOperation.CLEAN_INSERT
    ) {
        val dataSet: IDataSet = files.map { file ->
            val underlying = dataSetLoader.loadDataSet(clazz, file.path) ?: throw AssertionError("Dataset [${file.path}] not found")
            if (defaults.isNotEmpty() || file.overrides.isNotEmpty()) {
                ReplacementDataSet(underlying).also { ds ->
                    val datasetTables = underlying.tableNames.map { it.toLowerCase() }.toSet()
                    val defaultOverrides: Set<Cell> = defaults.filterKeys { datasetTables.contains(it.toLowerCase()) }.values.map { it.overrides }.flatten().toSet()
                    val testOverrides: Set<Cell> = file.overrides

                    (defaultOverrides + testOverrides).forEach { (key, value) ->
                        ds.addReplacementObject(key, value)
                    }
                }
            } else underlying
        }.let {
            if (it.size == 1) it.first()
            else CompositeDataSet(it.toTypedArray())
        }

        setSetUpOperation(operation.underlying)
        setDataSet(dataSet)
        onSetup()
    }

    /**
     * Loads a DataSet built programatically
     */
    fun givenDataSet(
        vararg tables: Table,
        operation: DatabaseOperation = DatabaseOperation.CLEAN_INSERT
    ) = givenDataSet(tables.toList(), operation)

    /**
     * Loads a DataSet built programatically
     */
    @Suppress("UsePropertyAccessSyntax")
    fun givenDataSet(
        tables: Collection<Table>,
        operation: DatabaseOperation = DatabaseOperation.CLEAN_INSERT
    ) {
        val dataSet: IDataSet = TableBasedDataSetBuilder(tables, defaults.values).build()
        setSetUpOperation(operation.underlying)
        setDataSet(dataSet)
        onSetup()
    }

    /**
     * Selects * from each table of the database & returns the result as an IDataSet for dataset assertions
     */
    fun createDataset(): IDataSet = usingConnection { it.createDataSet() }

    /**
     * Selects * from each table specified & returns the result as an IDataSet for dataset assertions
     */
    fun createDataset(vararg tableNames: String): IDataSet = usingConnection { it.createDataSet(tableNames) }

    /**
     * Select * from {name} and returns the result as an ITable for table assertions
     */
    fun createTable(tableName: String): ITable = usingConnection { it.createTable(tableName) }

    /**
     * Runs a SQL Query returns the result as an ITable for table assertions
     */
    fun createQueryTable(tableName: String, sql: String): ITable = usingConnection { it.createQueryTable(tableName, sql) }

    private fun IDatabaseConnection.apply(config: DatabaseConfig) {
        for (configProperty in DatabaseConfig.ALL_PROPERTIES) {
            val name = configProperty.property
            config.getProperty(name)
                ?.takeIf { configProperty.isNullable || !configProperty.isNullable }
                ?.let { this.config.setProperty(name, it) }
        }
    }

    /* ------------------------------------- Delegate Methods ------------------------------------------ */

    /**
     * Retrieves a DB Unit Connection
     * Remember to call close() on the connection instance once you are done to free that connection up
     */
    override fun getConnection(): IDatabaseConnection = DatabaseConnection(connectionSupplier.getConnection())
        .also { it.apply(config) }
        .also { connectionModifier.modify(it) }

}