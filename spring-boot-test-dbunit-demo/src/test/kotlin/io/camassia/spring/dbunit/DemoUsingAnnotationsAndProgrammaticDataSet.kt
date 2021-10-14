package io.camassia.spring.dbunit

import io.camassia.spring.dbunit.api.DatabaseTester
import io.camassia.spring.dbunit.api.annotations.DatabaseSetup
import io.camassia.spring.dbunit.api.annotations.DatabaseTeardown
import io.camassia.spring.dbunit.api.annotations.Table
import io.camassia.spring.dbunit.api.annotations.Table.Cell
import io.camassia.spring.dbunit.api.annotations.Table.Row
import io.camassia.spring.dbunit.api.connection.DataSourceConnectionSupplier
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
import javax.sql.DataSource

@SpringBootTest(
    classes = [
        DemoJdbcRepository::class,
        DemoUsingAnnotationsAndProgrammaticDataSet.DemoTestConfiguration::class
    ]
)
@AutoConfigureDbUnit
class DemoUsingAnnotationsAndProgrammaticDataSet @Autowired constructor(
    private val dbUnit: DatabaseTester,
    private val repository: DemoJdbcRepository
) {

    @BeforeEach
    fun createTheTable() {
        repository.createTable()
    }

    @Test
    @DatabaseSetup(tables = [
        Table(
            "demo",
            Row(Cell("ID", "123"), Cell("NAME", "Test"))
        )
    ])
    @DatabaseTeardown(tables = [Table("demo")])
    fun `repository should query successfully`() {
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

        @Bean
        fun connectionSupplier(ds: DataSource) = DataSourceConnectionSupplier(ds)
    }
}