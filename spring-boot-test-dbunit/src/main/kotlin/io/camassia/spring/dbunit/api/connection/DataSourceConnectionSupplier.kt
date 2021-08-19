package io.camassia.spring.dbunit.api.connection

import java.sql.Connection
import javax.sql.DataSource

class DataSourceConnectionSupplier(private val ds: DataSource): ConnectionSupplier {
    override fun getConnection(): Connection = ds.connection
}