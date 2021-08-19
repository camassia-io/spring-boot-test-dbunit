package io.camassia.spring.dbunit

import io.camassia.spring.dbunit.api.DatabaseTester
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
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
        DemoJdbcRepository::class,
        DemoUsingDatabaseTester.DemoTestConfiguration::class
    ]
)
@AutoConfigureDbUnit
class DemoUsingDatabaseTester @Autowired constructor(
    private val dbUnit: DatabaseTester,
    private val repository: DemoJdbcRepository
) {

    @BeforeEach
    fun createTheTable() {
        repository.createTable()
    }

    @Test
    fun `repository should insert successfully`() {
        repository.insert(DemoJdbcRepository.DemoDao(123, "Test"))

        val demoTable = dbUnit.createTable("demo")
        assertThat(demoTable.rowCount).isOne
        assertThat(demoTable.getValue(0, "id")).isEqualTo(BigInteger.valueOf(123))
        assertThat(demoTable.getValue(0, "name")).isEqualTo("Test")
    }

    @Test
    fun `repository should query successfully`() {
        dbUnit.givenDataSet(DemoUsingDatabaseTester::class.java, "/Demo.xml")

        val result = repository.selectAll()
        assertThat(result).hasSize(1)
        assertThat(result[0].id).isEqualTo(123)
        assertThat(result[0].name).isEqualTo("Test")
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
    }
}