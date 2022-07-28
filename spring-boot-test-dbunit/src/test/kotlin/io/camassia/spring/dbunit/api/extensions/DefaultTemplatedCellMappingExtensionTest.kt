package io.camassia.spring.dbunit.api.extensions

import io.camassia.spring.dbunit.api.DbUnitException
import io.camassia.spring.dbunit.api.dataset.Cell
import io.camassia.spring.dbunit.api.dataset.Overrides
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

internal class DefaultTemplatedCellMappingExtensionTest {

    private val defaults = mockk<Defaults>()
    private val extension = DefaultTemplatedCellMappingExtension(defaults)

    @Test
    fun `should leave other values untouched`() {
        val output = extension.applyTo("Table", Cell("Column", "123"), Overrides())

        assertThat(output).isEqualTo(Cell("Column", "123"))
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

    @Test
    fun `should map to default override`() {
        every { defaults.forColumn("Table", "Column") } returns Cell("Column", "Override")

        val output = extension.applyTo("Table", Cell("Column", "[Value]"), Overrides())

        assertThat(output).isEqualTo(Cell("Column", "Override"))
    }

    @Test
    fun `should map to default override when null`() {
        every { defaults.forColumn("Table", "Column") } returns Cell("Column", null)

        val output = extension.applyTo("Table", Cell("Column", "[Value]"), Overrides())

        assertThat(output).isEqualTo(Cell("Column", null))
    }

    @Test
    fun `should throw exception when no override or default is available`() {
        every { defaults.forColumn("Table", "Column") } returns null

        assertThatThrownBy {
            extension.applyTo("Table", Cell("Column", "[Value]"), Overrides())
        }
            .isInstanceOf(DbUnitException::class.java)
            .hasMessage(
                "Expected an Override for [Value] but there wasn't one configured. Overrides available were: []"
            )

    }
}