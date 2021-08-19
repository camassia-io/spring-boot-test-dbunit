package io.camassia.spring.dbunit.api.connection

import java.sql.Connection

fun interface ConnectionSupplier {
    
    fun getConnection(): Connection

}