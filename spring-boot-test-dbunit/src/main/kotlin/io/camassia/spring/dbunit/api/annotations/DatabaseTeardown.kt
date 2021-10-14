package io.camassia.spring.dbunit.api.annotations

import io.camassia.spring.dbunit.api.customization.DatabaseOperation

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class DatabaseTeardown(
    vararg val value: String = [],
    val files: Array<File> = [],
    val tables: Array<Table> = [],
    val operation: DatabaseOperation = DatabaseOperation.CLEAN_INSERT
)
