package io.camassia.spring.dbunit

import io.camassia.spring.dbunit.api.DatabaseTester
import io.camassia.spring.dbunit.api.DbUnitException
import io.camassia.spring.dbunit.api.connection.DataSourceConnectionSupplier
import io.camassia.spring.dbunit.api.customization.DatabaseOperation
import io.camassia.spring.dbunit.api.customization.TableDefaults
import io.camassia.spring.dbunit.api.dataset.Cell
import io.camassia.spring.dbunit.api.dataset.File
import io.camassia.spring.dbunit.api.dataset.Row
import io.camassia.spring.dbunit.api.dataset.Table
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.dbunit.dataset.NoSuchColumnException
import org.dbunit.dataset.NoSuchTableException
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.core.JdbcTemplate
import java.math.BigInteger
import javax.sql.DataSource

@SpringBootTest(
    classes = [
        TestApp::class,
        DatabaseTesterTest.DemoTestConfiguration::class
    ]
)
@AutoConfigureDbUnit
class DatabaseTesterTest @Autowired constructor(
    private val dbunit: DatabaseTester
) : RepositoryTest() {

    @Nested
    inner class GivenDataSet {
        @Nested
        inner class WithFilenames {
            @Test
            fun `should handle database clear`() {
                dbunit.givenDataSet(DatabaseTesterTest::class, "/Empty123.xml")

                assertThat(selectAllFrom("demo1")).isEmpty()
                assertThat(selectAllFrom("demo2")).isEmpty()
                assertThat(selectAllFrom("demo3")).isEmpty()
            }

            @Test
            fun `should handle files with one row in multiple tables`() {
                dbunit.givenDataSet(DatabaseTesterTest::class, "/Demo-WithOneRowInMultipleTables.xml")

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
            fun `should handle files with multiple rows in one table`() {
                dbunit.givenDataSet(DatabaseTesterTest::class, "/Demo-WithMultipleRowsInOneTable.xml")

                val result = selectAllFrom("demo1")
                assertThat(result).hasSize(2)
                assertThat(result[0].component1()).isEqualTo(1)
                assertThat(result[0].component2()).isEqualTo("Test1")
                assertThat(result[1].component1()).isEqualTo(2)
                assertThat(result[1].component2()).isEqualTo("Test2")
            }

            @Test
            fun `should handle multiple files`() {
                dbunit.givenDataSet(DatabaseTesterTest::class, "/Demo1.xml", "/Demo2.xml")

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
            fun `should handle sequential setups`() {
                dbunit.givenDataSet(DatabaseTesterTest::class, "/Demo1.xml")
                dbunit.givenDataSet(DatabaseTesterTest::class, "/Demo2.xml")

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
            fun `should use defaults for missing fields`() {
                dbunit.givenDataSet(DatabaseTesterTest::class, "/Demo-WithDefaults.xml")

                val result = selectAllFrom("demo1")
                assertThat(result).hasSize(1)
                assertThat(result[0].component1()).isEqualTo(1)
                assertThat(result[0].component2()).isEqualTo("default")
            }

            @Test
            fun `should use defaults for empty row`() {
                dbunit.givenDataSet(DatabaseTesterTest::class, "/TemplatedDemo3.xml")

                val result = selectAllFrom("demo3")
                assertThat(result).hasSize(1)
                assertThat(result[0].component1()).isEqualTo(3)
                assertThat(result[0].component2()).isEqualTo("default")
            }

            @Test
            fun `should replace special file params`() {
                dbunit.givenDataSet(DatabaseTesterTest::class, "/Demo-WithFile.xml")

                val result = selectAllFrom("demo1")
                assertThat(result).hasSize(1)
                assertThat(result[0].component1()).isEqualTo(1)
                assertThat(result[0].component2()).isEqualTo("long-value")
            }
        }

        @Nested
        inner class WithTemplatedFiles {
            @Test
            fun `should handle database clear`() {
                dbunit.givenDataSet(
                    DatabaseTesterTest::class,
                    File("/Empty123.xml")
                )

                assertThat(selectAllFrom("demo1")).isEmpty()
                assertThat(selectAllFrom("demo2")).isEmpty()
                assertThat(selectAllFrom("demo3")).isEmpty()
            }

            @Test
            fun `should handle files with one row in multiple tables`() {
                dbunit.givenDataSet(
                    DatabaseTesterTest::class,
                    File(
                        "/TemplatedDemo-WithOneRowInMultipleTables.xml",
                        Cell("[ID1]", "1"), Cell("[NAME1]", "Test1"),
                        Cell("[ID2]", "2"), Cell("[NAME2]", "Test2")
                    )
                )

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
            fun `should handle files with multiple rows in one table`() {
                dbunit.givenDataSet(
                    DatabaseTesterTest::class,
                    File(
                        "/TemplatedDemo-WithMultipleRowsInOneTable.xml",
                        Cell("[ID1]", "1"), Cell("[NAME1]", "Test1"),
                        Cell("[ID2]", "2"), Cell("[NAME2]", "Test2")
                    )
                )

                val result = selectAllFrom("demo1")
                assertThat(result).hasSize(2)
                assertThat(result[0].component1()).isEqualTo(1)
                assertThat(result[0].component2()).isEqualTo("Test1")
                assertThat(result[1].component1()).isEqualTo(2)
                assertThat(result[1].component2()).isEqualTo("Test2")
            }

            @Test
            fun `should handle multiple files`() {
                dbunit.givenDataSet(
                    DatabaseTesterTest::class,
                    File(
                        "/TemplatedDemo1.xml",
                        Cell("[ID]", "1"), Cell("[NAME]", "Test1")
                    ),
                    File(
                        "/TemplatedDemo2.xml",
                        Cell("[ID]", "2"), Cell("[NAME]", "Test2")
                    )
                )

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
            fun `should handle multiple setups`() {
                dbunit.givenDataSet(
                    DatabaseTesterTest::class,
                    File(
                        "/TemplatedDemo1.xml",
                        Cell("[ID]", "1"), Cell("[NAME]", "Test1")
                    )
                )
                dbunit.givenDataSet(
                    DatabaseTesterTest::class,
                    File(
                        "/TemplatedDemo2.xml",
                        Cell("[ID]", "2"), Cell("[NAME]", "Test2")
                    )
                )

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
            fun `should use defaults for missing fields`() {
                dbunit.givenDataSet(
                    DatabaseTesterTest::class,
                    File(
                        "/TemplatedDemo-WithDefaults.xml",
                        Cell("[ID]", "1")
                    )
                )

                val result = selectAllFrom("demo1")
                assertThat(result).hasSize(1)
                assertThat(result[0].component1()).isEqualTo(1)
                assertThat(result[0].component2()).isEqualTo("default")
            }

            @Test
            fun `should use defaults for empty row`() {
                dbunit.givenDataSet(
                    DatabaseTesterTest::class,
                    File("/TemplatedDemo3.xml")
                )

                val result = selectAllFrom("demo3")
                assertThat(result).hasSize(1)
                assertThat(result[0].component1()).isEqualTo(3)
                assertThat(result[0].component2()).isEqualTo("default")
            }

            @Test
            fun `should use defaults for missing overrides if available`() {
                dbunit.givenDataSet(
                    DatabaseTesterTest::class,
                    File(
                        "/TemplatedDemo1.xml",
                        Cell("[ID]", "1")
                    )
                )

                val result = selectAllFrom("demo1")
                assertThat(result).hasSize(1)
                assertThat(result[0].component1()).isEqualTo(1)
                assertThat(result[0].component2()).isEqualTo("default")
            }

            @Test
            fun `should replace special file params`() {
                dbunit.givenDataSet(
                    DatabaseTesterTest::class,
                    File(
                        "/TemplatedDemo1.xml",
                        Cell("[ID]", "1"),
                        Cell("[NAME]", "[file:/long-value.txt]"),
                    )
                )

                val result = selectAllFrom("demo1")
                assertThat(result).hasSize(1)
                assertThat(result[0].component1()).isEqualTo(1)
                assertThat(result[0].component2()).isEqualTo("long-value")
            }

            @Test
            fun `should throw exception for unknown field`() {
                assertThatThrownBy {
                    dbunit.givenDataSet(
                        DatabaseTesterTest::class,
                        File(
                            "/TemplatedDemo1.xml",
                            Cell("[ID]", "1"),
                            Cell("[UNKNOWN]", "123"),
                        )
                    )
                }.isInstanceOf(DbUnitException::class.java)
            }
        }

        @Nested
        inner class WithProgrammaticDataSet {
            @Test
            fun `should handle database clear`() {
                dbunit.givenDataSet(
                    Table("demo1", emptyList()),
                    Table("demo2", emptyList()),
                    Table("demo3", emptyList())
                )

                assertThat(selectAllFrom("demo1")).isEmpty()
                assertThat(selectAllFrom("demo2")).isEmpty()
                assertThat(selectAllFrom("demo3")).isEmpty()
            }

            @Test
            fun `should handle datasets with one row in multiple tables`() {
                dbunit.givenDataSet(
                    Table(
                        "demo1",
                        Row(Cell("id", "1"), Cell("name", "Test1"))
                    ),
                    Table(
                        "demo2",
                        Row(Cell("id", "2"), Cell("name", "Test2"))
                    )
                )

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
            fun `should handle datasets with multiple rows in one table`() {
                dbunit.givenDataSet(
                    Table(
                        "demo1",
                        Row(Cell("id", "1"), Cell("name", "Test1")),
                        Row(Cell("id", "2"), Cell("name", "Test2"))
                    )
                )

                val result = selectAllFrom("demo1")
                assertThat(result).hasSize(2)
                assertThat(result[0].component1()).isEqualTo(1)
                assertThat(result[0].component2()).isEqualTo("Test1")
                assertThat(result[1].component1()).isEqualTo(2)
                assertThat(result[1].component2()).isEqualTo("Test2")
            }

            @Test
            fun `should use defaults for missing fields`() {
                dbunit.givenDataSet(
                    Table(
                        "demo1",
                        Row(Cell("id", "1"))
                    )
                )

                val result = selectAllFrom("demo1")
                assertThat(result).hasSize(1)
                assertThat(result[0].component1()).isEqualTo(1)
                assertThat(result[0].component2()).isEqualTo("default")
            }

            @Test
            fun `should use defaults for empty row`() {
                dbunit.givenDataSet(
                    Table(
                        "demo3",
                        Row(emptyList())
                    )
                )

                val result = selectAllFrom("demo3")
                assertThat(result).hasSize(1)
                assertThat(result[0].component1()).isEqualTo(3)
                assertThat(result[0].component2()).isEqualTo("default")
            }

            @Test
            fun `should handle datasets with nulls`() {
                dbunit.givenDataSet(
                    Table(
                        "demo1",
                        Row(Cell("id", "1"), Cell("name", "[null]"))
                    )
                )

                val result = selectAllFrom("demo1")
                assertThat(result).hasSize(1)
                assertThat(result[0].component1()).isEqualTo(1)
                assertThat(result[0].component2()).isNull()
            }

            @Test
            fun `should handle datasets with file cell`() {
                dbunit.givenDataSet(
                    Table(
                        "demo1",
                        Row(Cell("id", "1"), Cell("name", "[file:/long-value.txt]"))
                    )
                )

                val result = selectAllFrom("demo1")
                assertThat(result).hasSize(1)
                assertThat(result[0].component1()).isEqualTo(1)
                assertThat(result[0].component2()).isEqualTo("long-value")
            }

            @Test
            fun `should throw exception for unknown field`() {
                assertThatThrownBy {
                    dbunit.givenDataSet(
                        Table(
                            "demo1",
                            Row(Cell("id", "1"), Cell("unknown", "Test1"))
                        )
                    )
                }.hasCauseInstanceOf(NoSuchColumnException::class.java)
            }

            @Nested
            inner class ShouldHandleMultipleDataSetUps {

                @Test
                fun `when the same table`() {
                    dbunit.givenDataSet(
                        Table(
                            "demo1",
                            Row(Cell("id", "1"), Cell("name", "Test1"))
                        )
                    )
                    dbunit.givenDataSet(
                        Table(
                            "demo1",
                            Row(Cell("id", "2"), Cell("name", "Test2"))
                        ),
                        operation = DatabaseOperation.INSERT
                    )

                    val result = selectAllFrom("demo1")
                    assertThat(result).hasSize(2)
                    assertThat(result[0].component1()).isEqualTo(1)
                    assertThat(result[0].component2()).isEqualTo("Test1")
                    assertThat(result[1].component1()).isEqualTo(2)
                    assertThat(result[1].component2()).isEqualTo("Test2")
                }

                @Test
                fun `when different tables`() {
                    dbunit.givenDataSet(
                        Table(
                            "demo1",
                            Row(Cell("id", "1"), Cell("name", "Test1"))
                        )
                    )
                    dbunit.givenDataSet(
                        Table(
                            "demo2",
                            Row(Cell("id", "2"), Cell("name", "Test2"))
                        )
                    )

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

        @Test
        fun shouldThrowUsefulExceptionOnNoSuchTableException() {
            assertThatThrownBy {
                dbunit.givenDataSet(
                    Table("other1", Row(Cell("ID", "Value"))),
                    Table("other2", Row(Cell("ID", "Value"))),

                )
            }
                .isInstanceOf(DbUnitException::class.java)
                .hasMessage(
                    """
                    Could not load DataSet: The table [OTHER2] does not exist
                    
                    ****** table: OTHER1 ** row count: 1 ******
                    ID                  |
                    ====================|
                    Value               |
                    
                    ****** table: OTHER2 ** row count: 1 ******
                    ID                  |
                    ====================|
                    Value               |
                    """.trimIndent()
                )
                .hasCauseInstanceOf(NoSuchTableException::class.java)
        }

        @Test
        fun shouldThrowUsefulExceptionOnNoSuchColumnException() {
            assertThatThrownBy {
                dbunit.givenDataSet(
                    Table("demo1", Row(Cell("UNKNOWN_COLUMN", "Value")))
                )
            }
                .isInstanceOf(DbUnitException::class.java)
                .hasMessage(
                    """
                    Could not load DataSet: The column [UNKNOWN_COLUMN] on table [DEMO1] does not exist
                    
                    ****** table: DEMO1 ** row count: 1 ******
                    name                |UNKNOWN_COLUMN      |
                    ====================|====================|
                    default             |Value               |
                    """.trimIndent()
                )
                .hasCauseInstanceOf(NoSuchColumnException::class.java)
        }
    }

    @Nested
    inner class CreateDataSet {
        @Test
        fun `should build a dataset from the current db state`() {
            insertInto("demo1", 1, "name1")
            insertInto("demo2", 2, "name2")
            insertInto("demo3", 3, "name3")

            val dataSet = dbunit.createDataset()

            assertThat(dataSet.tableNames).containsExactlyInAnyOrder("DEMO1", "DEMO2", "DEMO3")
            val demo1 = dataSet.getTable("demo1")
            assertThat(demo1.rowCount).isOne
            assertThat(demo1.getValue(0, "id") as BigInteger).isEqualTo(1)
            assertThat(demo1.getValue(0, "name")).isEqualTo("name1")
            val demo2 = dataSet.getTable("demo2")
            assertThat(demo2.rowCount).isOne
            assertThat(demo2.getValue(0, "id") as BigInteger).isEqualTo(2)
            assertThat(demo2.getValue(0, "name")).isEqualTo("name2")
            val demo3 = dataSet.getTable("demo3")
            assertThat(demo3.rowCount).isOne
            assertThat(demo3.getValue(0, "id") as BigInteger).isEqualTo(3)
            assertThat(demo3.getValue(0, "name")).isEqualTo("name3")
        }

        @Test
        fun `should build a dataset from the current db state using table names`() {
            insertInto("demo1", 1, "name1")

            val dataSet = dbunit.createDataset("demo1")

            assertThat(dataSet.tableNames).containsExactly("DEMO1")
            val demo1 = dataSet.getTable("demo1")
            assertThat(demo1.rowCount).isOne
            assertThat(demo1.getValue(0, "id") as BigInteger).isEqualTo(1)
            assertThat(demo1.getValue(0, "name")).isEqualTo("name1")
        }
    }

    @Nested
    inner class CreateTable {
        @Test
        fun `should build table from the current db state`() {
            insertInto("demo1", 1, "name1")

            val demo1 = dbunit.createTable("demo1")
            assertThat(demo1.rowCount).isOne
            assertThat(demo1.getValue(0, "id") as BigInteger).isEqualTo(1)
            assertThat(demo1.getValue(0, "name")).isEqualTo("name1")
        }
    }

    @Nested
    inner class CreateQueryTable {
        @Test
        fun `should build table from the current db state`() {
            insertInto("demo1", 1, "name1")
            insertInto("demo1", 2, "name2")

            val demo1 = dbunit.createQueryTable("test", "select * from demo1 where id = 1")
            assertThat(demo1.rowCount).isOne
            assertThat(demo1.getValue(0, "id") as BigInteger).isEqualTo(1)
            assertThat(demo1.getValue(0, "name")).isEqualTo("name1")
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
        fun tableDefault1() = TableDefaults("demo1", Cell("name", "default"))

        @Bean
        fun tableDefault3() = TableDefaults("demo3", Cell("id", "3"), Cell("name", "default"))
    }
}
