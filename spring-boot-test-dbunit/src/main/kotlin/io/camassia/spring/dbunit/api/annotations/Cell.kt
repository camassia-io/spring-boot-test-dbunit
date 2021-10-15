package io.camassia.spring.dbunit.api.annotations

/**
 * A Column-Value pair for use with Row
 *
 * @param name The column name
 * @param value The value for that column on that Row
 * @see Row
 */
annotation class Cell(val name: String, val value: String)