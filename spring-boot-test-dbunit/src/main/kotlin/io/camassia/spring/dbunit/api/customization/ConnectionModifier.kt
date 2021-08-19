package io.camassia.spring.dbunit.api.customization

import org.dbunit.database.IDatabaseConnection

fun interface ConnectionModifier {

    fun modify(connection: IDatabaseConnection)

}