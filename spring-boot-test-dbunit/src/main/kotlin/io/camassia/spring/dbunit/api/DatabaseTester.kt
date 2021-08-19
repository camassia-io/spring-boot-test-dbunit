package io.camassia.spring.dbunit.api

import io.camassia.spring.dbunit.api.customization.ConnectionModifier
import io.camassia.spring.dbunit.api.dataset.DataSetLoader
import org.dbunit.IDatabaseTester
import org.dbunit.IOperationListener
import org.dbunit.database.DatabaseConfig
import org.dbunit.database.IDatabaseConnection
import org.dbunit.dataset.IDataSet
import org.dbunit.dataset.ITable
import org.dbunit.operation.DatabaseOperation
import kotlin.reflect.KClass

/**
 * Acts as a Wrapper around DbUnits IDatabaseTester + adds extra utility methods
 */
open class DatabaseTester(
    private val underlying: IDatabaseTester,
    private val config: DatabaseConfig,
    private val connectionModifier: ConnectionModifier,
    private val dataSetLoader: DataSetLoader
    ) : IDatabaseTester {

    /**
     * Retrieves a DB Unit Connection
     * Remember to call close() on the connection instance once you are done to free that connection up
     */
    override fun getConnection(): IDatabaseConnection = underlying.connection
        .also { it.apply(config) }
        .also { connectionModifier.modify(it) }
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
    fun givenDataSet(clazz: Class<*>, filePath1: String, vararg filePaths: String) {
        val files = arrayOf(filePath1) + filePaths
        files.forEach { file ->
            val dataSet = dataSetLoader.loadDataSet(clazz, file)
            setSetUpOperation(DatabaseOperation.CLEAN_INSERT)
            setDataSet(dataSet)
            onSetup()
        }
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

    override fun closeConnection(connection: IDatabaseConnection?) = underlying.closeConnection(connection)

    override fun getDataSet(): IDataSet = underlying.getDataSet()

    override fun getSetUpOperation(): DatabaseOperation = underlying.getSetUpOperation()

    override fun getTearDownOperation(): DatabaseOperation = underlying.getTearDownOperation()

    override fun setDataSet(dataSet: IDataSet?) = underlying.setDataSet(dataSet)

    override fun setSchema(schema: String?) = underlying.setSchema(schema)

    override fun setSetUpOperation(setUpOperation: DatabaseOperation?) = underlying.setSetUpOperation(setUpOperation)

    override fun setTearDownOperation(tearDownOperation: DatabaseOperation?) = underlying.setTearDownOperation(tearDownOperation)

    override fun onSetup() = underlying.onSetup()

    override fun onTearDown() = underlying.onTearDown()

    override fun setOperationListener(operationListener: IOperationListener?) = underlying.setOperationListener(operationListener)

}