package io.camassia.spring.dbunit.api.connection

import org.dbunit.database.DatabaseConnection
import java.sql.Connection

fun interface ConnectionSupplier {

    /**
     * Provides a connection to DB Unit
     */
    fun getConnection(): Connection

    /**
     * If you wish to modify the connection after DB Unit settings have been applied you can override this method
     */
    fun afterCreation(connection: DatabaseConnection) { /* No Op */ }

}