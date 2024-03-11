package io.camassia.spring.dbunit

import io.camassia.spring.dbunit.api.DatabaseTester
import io.camassia.spring.dbunit.api.connection.DataSourceConnectionSupplier
import io.camassia.spring.dbunit.api.customization.TableDefaults
import io.camassia.spring.dbunit.api.dataset.Cell
import io.camassia.spring.dbunit.api.dataset.Row
import io.camassia.spring.dbunit.api.dataset.Table
import org.assertj.core.api.Assertions.assertThat
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
        inner class WithProgrammaticDataSet {

            @Test
            fun `should handle simple dataset`() {
                dbunit.givenDataSet(
                    Table(
                        "demo",
                        Row(Cell("id", "1"), Cell("name", "Test1"))
                    )
                )

                val result1 = selectAllFrom("demo")
                assertThat(result1).hasSize(1)
                assertThat(result1[0].component1()).isEqualTo(1)
                assertThat(result1[0].component2()).isEqualTo("Test1")
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
        fun tableDefault() = TableDefaults("demo", Cell("name", "default"))

    }
}
