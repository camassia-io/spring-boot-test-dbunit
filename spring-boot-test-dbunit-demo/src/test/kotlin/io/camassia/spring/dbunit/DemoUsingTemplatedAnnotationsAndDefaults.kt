package io.camassia.spring.dbunit

import io.camassia.spring.dbunit.api.DatabaseTester
import io.camassia.spring.dbunit.api.annotations.File
import io.camassia.spring.dbunit.api.annotations.Override
import io.camassia.spring.dbunit.api.annotations.TemplatedDatabaseSetup
import io.camassia.spring.dbunit.api.annotations.TemplatedDatabaseTeardown
import io.camassia.spring.dbunit.api.connection.DataSourceConnectionSupplier
import io.camassia.spring.dbunit.api.customization.TableDefaults
import io.camassia.spring.dbunit.api.dataset.File.CellOverride
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
        DemoUsingTemplatedAnnotationsAndDefaults.DemoTestConfiguration::class
    ]
)
@AutoConfigureDbUnit
class DemoUsingTemplatedAnnotationsAndDefaults @Autowired constructor(
    private val dbUnit: DatabaseTester,
    private val repository: DemoJdbcRepository
) {

    @BeforeEach
    fun createTheTable() {
        repository.createTable()
    }

    @Test
    @TemplatedDatabaseSetup(
        File("/TemplatedDemo.xml", Override("[ID]", "123"))
    )
    @TemplatedDatabaseTeardown(
        File("/Empty.xml")
    )
    fun `when using string overrides`() {
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

        @Bean
        fun demoDefaults() = TableDefaults(
            "demo",
            CellOverride("[NAME]", "Test")
        )
    }
}