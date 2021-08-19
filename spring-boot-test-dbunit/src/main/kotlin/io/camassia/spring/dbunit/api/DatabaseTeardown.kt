package io.camassia.spring.dbunit.api

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class DatabaseTeardown(
    vararg val files: String
)
