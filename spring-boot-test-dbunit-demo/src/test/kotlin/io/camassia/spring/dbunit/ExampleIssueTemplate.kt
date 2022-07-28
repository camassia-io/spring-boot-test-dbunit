package io.camassia.spring.dbunit

import io.camassia.spring.dbunit.api.DatabaseTester
import io.camassia.spring.dbunit.api.connection.DataSourceConnectionSupplier
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlGroup
import javax.sql.DataSource

@SpringBootTest(
    classes = [
        ExampleIssueTemplate.DemoTestConfiguration::class
    ]
)
@AutoConfigureDbUnit
class ExampleIssueTemplate @Autowired constructor(
    private val jdbc: JdbcTemplate,
    private val dbTester: DatabaseTester
) {

    @Test
    @SqlGroup(
        Sql(
            statements = [
                "CREATE TABLE demo (id BIGINT NOT NULL, name VARCHAR(50), CONSTRAINT demo_pk PRIMARY KEY (id))",
            ],
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
        ),
        Sql(
            statements = [
                "DROP TABLE demo",
            ],
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
        )
    )
    fun `issue replication`() {
        // Some code that replicates the issue
    }

    @SpringBootApplication
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