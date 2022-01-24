package io.camassia.spring.dbunit.api.extensions

import io.camassia.spring.dbunit.api.dataset.Cell
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class NullCellMappingExtensionTest {

    @Test
    fun `should map null`() {
        val output = NullCellMappingExtension.applyTo("Table", Cell("Column", "[null]"), emptyMap())
        assertThat(output).isEqualTo(Cell("Column", null))
    }

    @Test
    fun `should leave other values untouched`() {
        val output = NullCellMappingExtension.applyTo("Table", Cell("Column", "[other]"), emptyMap())
        assertThat(output).isEqualTo(Cell("Column", "[other]"))

    }
}