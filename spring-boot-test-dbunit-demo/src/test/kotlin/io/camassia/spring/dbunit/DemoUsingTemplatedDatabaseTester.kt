package io.camassia.spring.dbunit

import io.camassia.spring.dbunit.api.DatabaseTester
import io.camassia.spring.dbunit.api.connection.DataSourceConnectionSupplier
import io.camassia.spring.dbunit.api.dataset.Cell
import io.camassia.spring.dbunit.api.dataset.File
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.core.JdbcTemplate
import javax.sql.DataSource

@SpringBootTest(
    classes = [
        DemoJdbcRepository::class,
        DemoUsingTemplatedDatabaseTester.DemoTestConfiguration::class
    ]
)
@AutoConfigureDbUnit
class DemoUsingTemplatedDatabaseTester @Autowired constructor(
    private val dbUnit: DatabaseTester,
    private val repository: DemoJdbcRepository
) {

    @BeforeEach
    fun createTheTable() {
        repository.createTable()
    }

    @Nested
    inner class RepositoryShouldQuerySuccessfully {
        @Test
        fun `when using overrides`() {
            dbUnit.givenDataSet(
                DemoUsingTemplatedDatabaseTester::class.java,
                File("/TemplatedDemo.xml", Cell("[ID]", 123), Cell("[NAME]", "Test"))
            )

            val result = repository.selectAll()
            assertThat(result).hasSize(1)
            assertThat(result[0].id).isEqualTo(123)
            assertThat(result[0].name).isEqualTo("Test")
        }

        @Test
        fun `when using null overrides`() {
            dbUnit.givenDataSet(
                DemoUsingTemplatedDatabaseTester::class.java,
                File("/TemplatedDemo.xml", Cell("[ID]", 123), Cell("[NAME]", null))
            )

            val result = repository.selectAll()
            assertThat(result).hasSize(1)
            assertThat(result[0].id).isEqualTo(123)
            assertThat(result[0].name).isEqualTo(null)
        }
    }

    @AfterEach
    fun dropTheTable() {
        repository.dropTable()
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