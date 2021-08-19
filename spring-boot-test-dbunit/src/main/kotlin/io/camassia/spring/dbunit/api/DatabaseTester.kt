package io.camassia.spring.dbunit.api

import io.camassia.spring.dbunit.api.connection.ConnectionSupplier
import io.camassia.spring.dbunit.api.customization.ConnectionModifier
import io.camassia.spring.dbunit.api.dataset.DataSetLoader
import org.dbunit.AbstractDatabaseTester
import org.dbunit.database.DatabaseConfig
import org.dbunit.database.DatabaseConnection
import org.dbunit.database.IDatabaseConnection
import org.dbunit.dataset.CompositeDataSet
import org.dbunit.dataset.IDataSet
import org.dbunit.dataset.ITable
import org.dbunit.operation.DatabaseOperation
import kotlin.reflect.KClass

/**
 * Acts as a Wrapper around DbUnits IDatabaseTester + adds extra utility methods
 */
open class DatabaseTester(
    private val connectionSupplier: ConnectionSupplier,
    private val config: DatabaseConfig,
    private val connectionModifier: ConnectionModifier,
    private val dataSetLoader: DataSetLoader,
    schema: String? = null
) : AbstractDatabaseTester(schema) {

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
     * Loads DataSets from Local Resources
     */
    fun givenDataSet(clazz: KClass<*>, filePath1: String, vararg filePaths: String) = givenDataSet(clazz.java, filePath1, *filePaths)

    /**
     * Loads DataSets from Local Resources
     */
    @Suppress("UsePropertyAccessSyntax")
    fun givenDataSet(clazz: Class<*>, filePath1: String, vararg filePaths: String) = givenDataSet(clazz, arrayOf(filePath1) + filePaths)

    internal fun givenDataSet(clazz: Class<*>, filePaths: Array<out String>) {
        val dataSet = filePaths.map { file ->
            dataSetLoader.loadDataSet(clazz, file) ?: throw AssertionError("Dataset [$file] not found")
        }.let {
            if (it.size == 1) it.first()
            else CompositeDataSet(it.toTypedArray())
        }

        setSetUpOperation(DatabaseOperation.CLEAN_INSERT)
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
    override fun getConnection(): IDatabaseConnection = connectionSupplier.getConnection()
        .let { DatabaseConnection(it) }
        .also { it.apply(config) }
        .also { connectionModifier.modify(it) }

}