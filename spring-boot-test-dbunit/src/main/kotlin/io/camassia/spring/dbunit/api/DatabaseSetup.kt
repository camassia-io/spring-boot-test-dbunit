package io.camassia.spring.dbunit.api

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class DatabaseSetup(
    vararg val files: String
)
