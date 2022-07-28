package io.camassia.spring.dbunit.api.extensions

import io.camassia.spring.dbunit.api.dataset.Cell
import io.camassia.spring.dbunit.api.dataset.Overrides
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TemplatedCellMappingExtensionTest {

    private val extension = TemplatedCellMappingExtension

    @Test
    fun `should leave other values untouched`() {
        val output = extension.applyTo("Table", Cell("Column", "123"), Overrides())

        assertThat(output).isEqualTo(Cell("Column", "123"))
    }

    @Test
    fun `should leave missing overrides untouched`() {
        val output = extension.applyTo("Table", Cell("Column", "[value]"), Overrides())

        assertThat(output).isEqualTo(Cell("Column", "[value]"))
    }

    @Test
    fun `should leave json arrays untouched`() {
        assertThat(extension.applyTo("Table", Cell("Column", """[]"""), Overrides())).isEqualTo(Cell("Column", """[]"""))
        assertThat(extension.applyTo("Table", Cell("Column", """["value"]"""), Overrides())).isEqualTo(Cell("Column", """["value"]"""))
        assertThat(extension.applyTo("Table", Cell("Column", """[{"field": "value"}]"""), Overrides())).isEqualTo(Cell("Column", """[{"field": "value"}]"""))
    }

    @Test
    fun `should map to template override`() {
        assertThat(extension.applyTo("Table", Cell("Column", "[value]"), Overrides("[value]" to "Override"))).isEqualTo(Cell("Column", "Override"))
        assertThat(extension.applyTo("Table", Cell("Column", "[VALUE]"), Overrides("[VALUE]" to "Override"))).isEqualTo(Cell("Column", "Override"))
        assertThat(extension.applyTo("Table", Cell("Column", "[VALUE123]"), Overrides("[VALUE123]" to "Override"))).isEqualTo(Cell("Column", "Override"))
        assertThat(extension.applyTo("Table", Cell("Column", "[VALUE_123]"), Overrides("[VALUE_123]" to "Override"))).isEqualTo(Cell("Column", "Override"))
        assertThat(extension.applyTo("Table", Cell("Column", "[VALUE-123]"), Overrides("[VALUE-123]" to "Override"))).isEqualTo(Cell("Column", "Override"))
    }
}