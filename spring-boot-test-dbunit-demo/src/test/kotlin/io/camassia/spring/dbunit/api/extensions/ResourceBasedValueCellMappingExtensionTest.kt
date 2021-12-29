package io.camassia.spring.dbunit.api.extensions

import io.camassia.spring.dbunit.api.dataset.Cell
import io.camassia.spring.dbunit.api.dataset.Row
import io.camassia.spring.dbunit.api.io.ResourceLoader
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ResourceBasedValueCellMappingExtensionTest {

    private val loader = mockk<ResourceLoader>{
        every { getResourceAsString(any(), any()) } returns "content"
    }
    private val extension = ResourceBasedValueCellMappingExtension(loader)

    private val table = "table"
    private val row = mockk<Row>()

    @Test
    fun shouldMapFilenames() {
        assertThat(extension.applyTo(table, Cell("key", "[file:abc.txt]"))).isEqualTo(Cell("key", "content"))
        assertThat(extension.applyTo(table, Cell("key", "[file:/abc.json]"))).isEqualTo(Cell("key", "content"))
        assertThat(extension.applyTo(table, Cell("key", "[file:abc-123.json]"))).isEqualTo(Cell("key", "content"))
        assertThat(extension.applyTo(table, Cell("key", "[file:abc_123.xml]"))).isEqualTo(Cell("key", "content"))
    }

    @Test
    fun shouldNotMapOtherValues() {
        assertThat(extension.applyTo(table, Cell("key", "[null]"))).isEqualTo(Cell("key", "[null]"))
        assertThat(extension.applyTo(table, Cell("key", "other"))).isEqualTo(Cell("key", "other"))
        assertThat(extension.applyTo(table, Cell("key", "other.txt"))).isEqualTo(Cell("key", "other.txt"))
    }
}