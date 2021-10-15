package io.camassia.spring.dbunit.api.annotations

/**
 * A Key-Value pair override for use with File
 *
 * @param key The key in the file to override
 * @param value The value to override that key with
 * @see File
 */
annotation class Override(
    val key: String,
    val value: String
)