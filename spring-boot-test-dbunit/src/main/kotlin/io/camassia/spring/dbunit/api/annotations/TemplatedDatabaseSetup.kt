package io.camassia.spring.dbunit.api.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class TemplatedDatabaseSetup(
    vararg val files: File,
    val operation: DatabaseOperation = DatabaseOperation.CLEAN_INSERT
)