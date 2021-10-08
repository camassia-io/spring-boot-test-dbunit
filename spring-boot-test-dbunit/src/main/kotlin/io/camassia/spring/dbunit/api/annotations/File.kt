package io.camassia.spring.dbunit.api.annotations

annotation class File(
    val name: String,
    vararg val overrides: Override
)