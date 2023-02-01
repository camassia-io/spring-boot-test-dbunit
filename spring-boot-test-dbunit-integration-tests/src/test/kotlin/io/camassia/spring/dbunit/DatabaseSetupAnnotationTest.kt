package io.camassia.spring.dbunit

import io.camassia.spring.dbunit.api.annotations.Cell
import io.camassia.spring.dbunit.api.annotations.DatabaseSetup
import io.camassia.spring.dbunit.api.annotations.File
import io.camassia.spring.dbunit.api.annotations.Row
import io.camassia.spring.dbunit.api.annotations.Table
import io.camassia.spring.dbunit.api.connection.DataSourceConnectionSupplier
import io.camassia.spring.dbunit.api.customization.DatabaseOperation
import io.camassia.spring.dbunit.api.customization.TableDefaults
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
        DatabaseSetupAnnotationTest.DemoTestConfiguration::class
    ]
)
@AutoConfigureDbUnit
class DatabaseSetupAnnotationTest : RepositoryTest() {

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

        @Test
        @DatabaseSetup("/Demo-WithDefaults.xml")
        fun `should use defaults for missing fields`() {
            val result = selectAllFrom("demo1")
            assertThat(result).hasSize(1)
            assertThat(result[0].component1()).isEqualTo(1)
            assertThat(result[0].component2()).isEqualTo("default-name")
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
                Cell("[ID1]", "1"), Cell("[NAME1]", "Test1"),
                Cell("[ID2]", "2"), Cell("[NAME2]", "Test2")
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
                Cell("[ID1]", "1"), Cell("[NAME1]", "Test1"),
                Cell("[ID2]", "2"), Cell("[NAME2]", "Test2")
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
                Cell("[ID]", "1"), Cell("[NAME]", "Test1")
            ),
            File("/TemplatedDemo2.xml",
                Cell("[ID]", "2"), Cell("[NAME]", "Test2")
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

        @Test
        @DatabaseSetup(files = [
            File("/TemplatedDemo-WithDefaults.xml",
                Cell("[ID]", "1")
            )
        ])
        fun `should use defaults for missing fields`() {
            val result = selectAllFrom("demo1")
            assertThat(result).hasSize(1)
            assertThat(result[0].component1()).isEqualTo(1)
            assertThat(result[0].component2()).isEqualTo("default-name")
        }

        @Nested
        @DatabaseSetup(files = [
            File("/TemplatedDemo1.xml",
                Cell("[ID]", "1"), Cell("[NAME]", "Test1")
            )
        ])
        inner class ShouldHandleMultipleLevelsOfAnnotations {
            @Test
            @DatabaseSetup(files = [
                File("/TemplatedDemo2.xml",
                    Cell("[ID]", "2"), Cell("[NAME]", "Test2")
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
    inner class WithStringDataSet {
        @Test
        @DatabaseSetup(dataset = """
            <demo1 id="1" name="Test1"/>
            <demo2 id="2" name="Test2"/>
        """)
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
        @DatabaseSetup(dataset = """
            <demo1 id="1" name="Test1"/>
            <demo1 id="2" name="Test2"/>
        """)
        fun `should handle files with multiple rows in one table`() {
            val result = selectAllFrom("demo1")
            assertThat(result).hasSize(2)
            assertThat(result[0].component1()).isEqualTo(1)
            assertThat(result[0].component2()).isEqualTo("Test1")
            assertThat(result[1].component1()).isEqualTo(2)
            assertThat(result[1].component2()).isEqualTo("Test2")
        }

        @Test
        @DatabaseSetup(dataset = """
            <demo1 id="1"/>
        """)
        fun `should use defaults for missing fields`() {
            val result = selectAllFrom("demo1")
            assertThat(result).hasSize(1)
            assertThat(result[0].component1()).isEqualTo(1)
            assertThat(result[0].component2()).isEqualTo("default-name")
        }

        @Nested
        @DatabaseSetup(dataset = """<demo1 id="1" name="Test1"/>""")
        inner class ShouldHandleMultipleLevelsOfAnnotations {
            @Test
            @DatabaseSetup(dataset = """<demo2 id="2" name="Test2"/>""")
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

        @Test
        @DatabaseSetup(tables = [
            Table("demo1",
                Row(Cell("id", "1"))
            )
        ])
        fun `should use defaults for missing fields`() {
            val result = selectAllFrom("demo1")
            assertThat(result).hasSize(1)
            assertThat(result[0].component1()).isEqualTo(1)
            assertThat(result[0].component2()).isEqualTo("default-name")
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

    @Nested
    @DatabaseSetup(tables = [
        Table("demo1",
            Row(Cell("id", "1"), Cell("name", "Test1"))
        )
    ])
    inner class WithMultipleLevelsOfAnnotations {
        @Nested
        @DatabaseSetup(tables = [
            Table("demo1",
                Row(Cell("id", "2"), Cell("name", "Test2"))
            )
        ], operation = DatabaseOperation.INSERT)
        inner class LevelTwo {
            @Nested
            @DatabaseSetup(tables = [
                Table("demo1",
                    Row(Cell("id", "3"), Cell("name", "Test3"))
                )
            ], operation = DatabaseOperation.INSERT)
            inner class LevelTwo {
                @Test
                @DatabaseSetup(tables = [
                    Table("demo1",
                        Row(Cell("id", "4"), Cell("name", "Test4"))
                    )
                ], operation = DatabaseOperation.INSERT)
                fun `should combine multiple levels of annotations`() {
                    val result = selectAllFrom("demo1")
                    assertThat(result).hasSize(4)
                    assertThat(result).containsExactly(
                        1L to "Test1",
                        2L to "Test2",
                        3L to "Test3",
                        4L to "Test4",
                    )
                }
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

        @Bean
        fun defaults() = TableDefaults("demo1", io.camassia.spring.dbunit.api.dataset.Cell("name", "default-name"))
    }
}
