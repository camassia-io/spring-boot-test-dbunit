package io.camassia.spring.dbunit

import io.camassia.spring.dbunit.api.DatabaseTester
import io.camassia.spring.dbunit.api.annotations.DatabaseSetup
import io.camassia.spring.dbunit.api.annotations.DatabaseTeardown
import io.camassia.spring.dbunit.api.connection.DataSourceConnectionSupplier
import io.camassia.spring.dbunit.api.io.DefaultLocalResourceLoader
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
import java.io.InputStream
import java.net.URL
import javax.sql.DataSource

/**
 * Lets say all your xml data sets were in a certain directory, and all had file extension .xml
 * You might want to reduce the noise in your tests by getting rid of the preceding / and .xml from DatabaseSetup/DatabaseTeardown annotations
 * You can achieve this easily by creating a custom DataSetLoader Bean.
 */
@SpringBootTest(
    classes = [
        DemoJdbcRepository::class,
        DemoUsingAnnotationsWithCustomDataSetLoader.DemoTestConfiguration::class
    ]
)
@AutoConfigureDbUnit
class DemoUsingAnnotationsWithCustomDataSetLoader @Autowired constructor(
    private val dbUnit: DatabaseTester,
    private val repository: DemoJdbcRepository
) {

    @BeforeEach
    fun createTheTable() {
        repository.createTable()
    }

    @Test
    @DatabaseSetup("Demo")
    @DatabaseTeardown("Empty")
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

        @Bean
        fun resourceLoader() = object : DefaultLocalResourceLoader() {
            override fun getResourceUrl(clazz: Class<*>, path: String): URL = super.getResourceUrl(clazz, "/${path}.xml")
            override fun getResourceInputStream(clazz: Class<*>, path: String): InputStream = super.getResourceInputStream(clazz, "/${path}.xml")
        }
    }
}