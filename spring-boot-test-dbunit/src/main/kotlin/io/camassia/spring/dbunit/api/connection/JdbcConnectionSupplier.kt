package io.camassia.spring.dbunit.api.connection

import java.sql.Connection
import java.sql.DriverManager

class JdbcConnectionSupplier(
    private val driverClass: String,
    private val url: String,
    private val username: String? = null,
    private val password: String? = null,
): ConnectionSupplier {

    init {
        Class.forName(driverClass)
    }

    override fun getConnection(): Connection {
        return if(username == null && password == null) DriverManager.getConnection(url)
        else DriverManager.getConnection(url, username, password)
    }
}