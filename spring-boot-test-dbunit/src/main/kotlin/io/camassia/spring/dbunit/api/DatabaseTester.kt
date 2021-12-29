package io.camassia.spring.dbunit.api

import io.camassia.spring.dbunit.api.connection.ConnectionSupplier
import io.camassia.spring.dbunit.api.customization.DatabaseOperation
import io.camassia.spring.dbunit.api.dataset.Cell
import io.camassia.spring.dbunit.api.dataset.DataSetParser
import io.camassia.spring.dbunit.api.dataset.File
import io.camassia.spring.dbunit.api.dataset.Table
import io.camassia.spring.dbunit.api.dataset.builder.TableBasedDataSetBuilder
import io.camassia.spring.dbunit.api.extensions.Extensions
import io.camassia.spring.dbunit.api.io.ResourceLoader
import org.dbunit.AbstractDatabaseTester
import org.dbunit.database.DatabaseConfig
import org.dbunit.database.DatabaseConnection
import org.dbunit.database.IDatabaseConnection
import org.dbunit.dataset.CachedDataSet
import org.dbunit.dataset.CompositeDataSet
import org.dbunit.dataset.IDataSet
import org.dbunit.dataset.ITable
import org.dbunit.dataset.ReplacementDataSet
import org.dbunit.util.TableFormatter
import kotlin.reflect.KClass

/**
 * Acts as a Wrapper around DbUnits IDatabaseTester + adds extra utility methods
 */
open class DatabaseTester(
    private val connectionSupplier: ConnectionSupplier,
    private val config: DatabaseConfig,
    private val resourceLoader: ResourceLoader,
    private val dataSetParser: DataSetParser,
    private val extensions: Extensions,
    schema: String? = null
) : AbstractDatabaseTester(schema) {

    private val dataSetBuilder = TableBasedDataSetBuilder(extensions)

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
            val underlying = resourceLoader.getResourceInputStream(clazz, file.path).let { dataSetParser.parseDataSet(it) }
            if (file.overrides.isNotEmpty()) {
                val datasetTables = underlying.tableNames.map { it.toLowerCase() }.toSet()
                // Templated files require Defaults to be handled at a higher level
                val defaultOverrides: Set<Cell> = extensions.defaults(datasetTables)
                val testOverrides: Set<Cell> = file.overrides
                ReplacementDataSet(underlying).also { ds ->
                    (defaultOverrides + testOverrides).forEach { (key, value) ->
                        ds.addReplacementObject(key, value)
                    }
                }
            } else underlying
        }.let {
            if (it.size == 1) it.first()
            else CompositeDataSet(it.toTypedArray())
        }
        givenDataSet(dataSet, operation)
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
        val dataSet: IDataSet = dataSetBuilder.build(tables)
        givenDataSet(dataSet, operation)
    }

    /**
     * Loads a DataSet from a String
     */
    @Suppress("UsePropertyAccessSyntax")
    fun givenDataSet(
        dataset: Array<String>,
        operation: DatabaseOperation = DatabaseOperation.CLEAN_INSERT
    ) = givenDataSet(
        dataset.map { dataSetParser.parseDataSet(it) }.toTypedArray(),
        operation
    )

    @Suppress("UsePropertyAccessSyntax")
    fun givenDataSet(
        dataSet: Array<IDataSet>,
        operation: DatabaseOperation = DatabaseOperation.CLEAN_INSERT
    ) {
        val compositeDataSet = if (dataSet.size == 1) dataSet.first() else CompositeDataSet(dataSet)
        givenDataSet(compositeDataSet, operation)
    }

    /**
     * Loads a DataSet
     */
    @Suppress("UsePropertyAccessSyntax")
    fun givenDataSet(
        dataSet: IDataSet,
        operation: DatabaseOperation = DatabaseOperation.CLEAN_INSERT
    ) {
        setSetUpOperation(operation.underlying)
        setDataSet(dataSetBuilder.applyExtensions(dataSet))
        try {
            onSetup()
        } catch (throwable: Throwable) {
            val formatter = TableFormatter()
            val builder = StringBuilder()
            val iterator = dataSet.iterator()
            while (iterator.next()) {
                builder.append("\n")
                builder.append(formatter.format(iterator.table))
            }
            throw DbUnitException(
                """
                |Could not load DataSet:
                |$builder
                """.trimMargin().trim(),
                throwable
            )
        }
    }

    /**
     * Selects * from each table of the database & returns the result as an IDataSet for dataset assertions
     */
    fun createDataset(): IDataSet = usingConnection { CachedDataSet(it.createDataSet()) }

    /**
     * Selects * from each table specified & returns the result as an IDataSet for dataset assertions
     */
    fun createDataset(vararg tableNames: String): IDataSet = usingConnection { CachedDataSet(it.createDataSet(tableNames)) }

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
        .also { connectionSupplier.afterCreation(it) }

}