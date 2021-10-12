package io.camassia.spring.dbunit.api.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class DatabaseSetup(
    vararg val files: String,
    val operation: DatabaseOperation = DatabaseOperation.CLEAN_INSERT
)
