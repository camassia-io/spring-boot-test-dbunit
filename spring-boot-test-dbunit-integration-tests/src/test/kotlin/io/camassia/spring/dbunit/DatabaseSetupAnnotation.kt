package io.camassia.spring.dbunit

import io.camassia.spring.dbunit.api.annotations.DatabaseSetup
import io.camassia.spring.dbunit.api.annotations.File
import io.camassia.spring.dbunit.api.annotations.Override
import io.camassia.spring.dbunit.api.annotations.Table
import io.camassia.spring.dbunit.api.annotations.Table.Cell
import io.camassia.spring.dbunit.api.annotations.Table.Row
import io.camassia.spring.dbunit.api.connection.DataSourceConnectionSupplier
import io.camassia.spring.dbunit.api.customization.DatabaseOperation
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.core.JdbcTemplate
import javax.sql.DataSource

@SpringBootTest(
    classes = [
        TestApp::class,
        DatabaseSetupAnnotation.DemoTestConfiguration::class
    ]
)
@AutoConfigureDbUnit
class DatabaseSetupAnnotation : RepositoryTest() {

    @Nested
    inner class WithFilenames {
        @Test
        @DatabaseSetup("/Demo-WithOneRowInMultipleTables.xml")
        fun `should handle files with one row in multiple tables`() {
            val result1 = selectAllFrom("demo1")
            assertThat(result1).hasSize(1)
            assertThat(result1[0].component1()).isEqualTo(1)
            assertThat(result1[0].component2()).isEqualTo("Test1")

            val result2 = selectAllFrom("demo2")
            assertThat(result2).hasSize(1)
            assertThat(result2[0].component1()).isEqualTo(2)
            assertThat(result2[0].component2()).isEqualTo("Test2")
        }

        @Test
        @DatabaseSetup("/Demo-WithMultipleRowsInOneTable.xml")
        fun `should handle files with multiple rows in one table`() {
            val result = selectAllFrom("demo1")
            assertThat(result).hasSize(2)
            assertThat(result[0].component1()).isEqualTo(1)
            assertThat(result[0].component2()).isEqualTo("Test1")
            assertThat(result[1].component1()).isEqualTo(2)
            assertThat(result[1].component2()).isEqualTo("Test2")
        }

        @Test
        @DatabaseSetup("/Demo1.xml", "/Demo2.xml")
        fun `should handle multiple files`() {
            val result1 = selectAllFrom("demo1")
            assertThat(result1).hasSize(1)
            assertThat(result1[0].component1()).isEqualTo(1)
            assertThat(result1[0].component2()).isEqualTo("Test1")

            val result2 = selectAllFrom("demo2")
            assertThat(result2).hasSize(1)
            assertThat(result2[0].component1()).isEqualTo(2)
            assertThat(result2[0].component2()).isEqualTo("Test2")
        }

        @Nested
        @DatabaseSetup("/Demo1.xml")
        inner class ShouldHandleMultipleLevelsOfAnnotations {
            @Test
            @DatabaseSetup("/Demo2.xml")
            fun `when using inner classes`() {
                val result1 = selectAllFrom("demo1")
                assertThat(result1).hasSize(1)
                assertThat(result1[0].component1()).isEqualTo(1)
                assertThat(result1[0].component2()).isEqualTo("Test1")

                val result2 = selectAllFrom("demo2")
                assertThat(result2).hasSize(1)
                assertThat(result2[0].component1()).isEqualTo(2)
                assertThat(result2[0].component2()).isEqualTo("Test2")
            }
        }
    }

    @Nested
    inner class WithTemplatedFiles {
        @Test
        @DatabaseSetup(files = [
            File("/TemplatedDemo-WithOneRowInMultipleTables.xml",
                Override("[ID1]", "1"), Override("[NAME1]", "Test1"),
                Override("[ID2]", "2"), Override("[NAME2]", "Test2")
            )
        ])
        fun `should handle files with one row in multiple tables`() {
            val result1 = selectAllFrom("demo1")
            assertThat(result1).hasSize(1)
            assertThat(result1[0].component1()).isEqualTo(1)
            assertThat(result1[0].component2()).isEqualTo("Test1")

            val result2 = selectAllFrom("demo2")
            assertThat(result2).hasSize(1)
            assertThat(result2[0].component1()).isEqualTo(2)
            assertThat(result2[0].component2()).isEqualTo("Test2")
        }

        @Test
        @DatabaseSetup(files = [
            File("/TemplatedDemo-WithMultipleRowsInOneTable.xml",
                Override("[ID1]", "1"), Override("[NAME1]", "Test1"),
                Override("[ID2]", "2"), Override("[NAME2]", "Test2")
            )
        ])
        fun `should handle files with multiple rows in one table`() {
            val result = selectAllFrom("demo1")
            assertThat(result).hasSize(2)
            assertThat(result[0].component1()).isEqualTo(1)
            assertThat(result[0].component2()).isEqualTo("Test1")
            assertThat(result[1].component1()).isEqualTo(2)
            assertThat(result[1].component2()).isEqualTo("Test2")
        }

        @Test
        @DatabaseSetup(files = [
            File("/TemplatedDemo1.xml",
                Override("[ID]", "1"), Override("[NAME]", "Test1")
            ),
            File("/TemplatedDemo2.xml",
                Override("[ID]", "2"), Override("[NAME]", "Test2")
            )
        ])
        fun `should handle multiple files`() {
            val result1 = selectAllFrom("demo1")
            assertThat(result1).hasSize(1)
            assertThat(result1[0].component1()).isEqualTo(1)
            assertThat(result1[0].component2()).isEqualTo("Test1")

            val result2 = selectAllFrom("demo2")
            assertThat(result2).hasSize(1)
            assertThat(result2[0].component1()).isEqualTo(2)
            assertThat(result2[0].component2()).isEqualTo("Test2")
        }

        @Nested
        @DatabaseSetup(files = [
            File("/TemplatedDemo1.xml",
                Override("[ID]", "1"), Override("[NAME]", "Test1")
            )
        ])
        inner class ShouldHandleMultipleLevelsOfAnnotations {
            @Test
            @DatabaseSetup(files = [
                File("/TemplatedDemo2.xml",
                    Override("[ID]", "2"), Override("[NAME]", "Test2")
                )
            ])
            fun `when using inner classes`() {
                val result1 = selectAllFrom("demo1")
                assertThat(result1).hasSize(1)
                assertThat(result1[0].component1()).isEqualTo(1)
                assertThat(result1[0].component2()).isEqualTo("Test1")

                val result2 = selectAllFrom("demo2")
                assertThat(result2).hasSize(1)
                assertThat(result2[0].component1()).isEqualTo(2)
                assertThat(result2[0].component2()).isEqualTo("Test2")
            }
        }
    }

    @Nested
    inner class WithProgrammaticDataSet {
        @Test
        @DatabaseSetup(tables = [
            Table("demo1",
                Row(Cell("id", "1"), Cell("name", "Test1"))
            ),
            Table("demo2",
                Row(Cell("id", "2"), Cell("name", "Test2"))
            )
        ])
        fun `should handle datasets with one row in multiple tables`() {
            val result1 = selectAllFrom("demo1")
            assertThat(result1).hasSize(1)
            assertThat(result1[0].component1()).isEqualTo(1)
            assertThat(result1[0].component2()).isEqualTo("Test1")

            val result2 = selectAllFrom("demo2")
            assertThat(result2).hasSize(1)
            assertThat(result2[0].component1()).isEqualTo(2)
            assertThat(result2[0].component2()).isEqualTo("Test2")
        }

        @Test
        @DatabaseSetup(tables = [
            Table("demo1",
                Row(Cell("id", "1"), Cell("name", "Test1")),
                Row(Cell("id", "2"), Cell("name", "Test2"))
            )
        ])
        fun `should handle datasets with multiple rows in one table`() {
            val result = selectAllFrom("demo1")
            assertThat(result).hasSize(2)
            assertThat(result[0].component1()).isEqualTo(1)
            assertThat(result[0].component2()).isEqualTo("Test1")
            assertThat(result[1].component1()).isEqualTo(2)
            assertThat(result[1].component2()).isEqualTo("Test2")
        }

        @Nested
        @DatabaseSetup(tables = [
            Table("demo1",
                Row(Cell("id", "1"), Cell("name", "Test1"))
            )
        ])
        inner class ShouldHandleMultipleLevelsOfAnnotations {

            @Test
            @DatabaseSetup(tables = [
                Table("demo1",
                    Row(Cell("id", "2"), Cell("name", "Test2"))
                )
            ], operation = DatabaseOperation.INSERT)
            fun `when using inner classes for the same table`() {
                val result = selectAllFrom("demo1")
                assertThat(result).hasSize(2)
                assertThat(result[0].component1()).isEqualTo(1)
                assertThat(result[0].component2()).isEqualTo("Test1")
                assertThat(result[1].component1()).isEqualTo(2)
                assertThat(result[1].component2()).isEqualTo("Test2")
            }

            @Test
            @DatabaseSetup(tables = [
                Table("demo2",
                    Row(Cell("id", "2"), Cell("name", "Test2"))
                )
            ])
            fun `when using inner classes for different tables`() {
                val result1 = selectAllFrom("demo1")
                assertThat(result1).hasSize(1)
                assertThat(result1[0].component1()).isEqualTo(1)
                assertThat(result1[0].component2()).isEqualTo("Test1")

                val result2 = selectAllFrom("demo2")
                assertThat(result2).hasSize(1)
                assertThat(result2[0].component1()).isEqualTo(2)
                assertThat(result2[0].component2()).isEqualTo("Test2")
            }
        }
    }

    @TestConfiguration
    class DemoTestConfiguration {

        @Bean
        fun dataSource(): DataSource = DataSourceBuilder
            .create()
            .driverClassName("org.h2.Driver")
            .url("jdbc:h2:mem:dbunit")
            .username("sa")
            .password("sa")
            .build()

        @Bean
        fun jdbc(ds: DataSource) = JdbcTemplate(ds)

        @Bean
        fun connectionSupplier(ds: DataSource) = DataSourceConnectionSupplier(ds)
    }
}