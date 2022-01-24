package io.camassia.spring.dbunit.api.extensions

import io.camassia.spring.dbunit.api.DbUnitException
import io.camassia.spring.dbunit.api.dataset.Cell
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
        val output = extension.applyTo("Table", Cell("Column", "123"), emptyMap())

        assertThat(output).isEqualTo(Cell("Column", "123"))
    }

    @Test
    fun `should map to template override`() {
        val output = extension.applyTo("Table", Cell("Column", "[Value]"), mapOf("[Value]" to "Override"))

        assertThat(output).isEqualTo(Cell("Column", "Override"))
    }

    @Test
    fun `should map to default override`() {
        every { defaults.forColumn("Table", "Column") } returns Cell("Column", "Override")

        val output = extension.applyTo("Table", Cell("Column", "[Value]"), emptyMap())

        assertThat(output).isEqualTo(Cell("Column", "Override"))
    }

    @Test
    fun `should map to default override when null`() {
        every { defaults.forColumn("Table", "Column") } returns Cell("Column", null)

        val output = extension.applyTo("Table", Cell("Column", "[Value]"), emptyMap())

        assertThat(output).isEqualTo(Cell("Column", null))
    }

    @Test
    fun `should throw exception when no override or default is available`() {
        every { defaults.forColumn("Table", "Column") } returns null

        assertThatThrownBy {
            extension.applyTo("Table", Cell("Column", "[Value]"), emptyMap())
        }
            .isInstanceOf(DbUnitException::class.java)
            .hasMessage(
                "Expected an Override for [Value] but there wasn't one configured. Overrides available were: []"
            )

    }
}