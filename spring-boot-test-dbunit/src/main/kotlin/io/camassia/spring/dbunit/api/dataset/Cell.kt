package io.camassia.spring.dbunit.api.dataset

class Cell(
    val key: String,
    value: Any?
) {
    val value: Any? = value?.takeIf { it != "[null]" }

    operator fun component1() = key
    operator fun component2() = value
}