package io.camassia.spring.dbunit

import io.camassia.spring.dbunit.api.DatabaseTester
import io.camassia.spring.dbunit.api.connection.DataSourceConnectionSupplier
import io.camassia.spring.dbunit.api.customization.ConnectionModifier
import io.camassia.spring.dbunit.api.customization.TableDefaults
import io.camassia.spring.dbunit.api.dataset.DataSetLoader
import io.camassia.spring.dbunit.api.dataset.xml.XmlLocalResourceDataSetLoader
import org.dbunit.database.DatabaseConfig
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan

@TestConfiguration
@ComponentScan(basePackageClasses = [DatabaseTester::class])
class SpringBootTestDbUnitConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun defaultDatabaseConfig() = DatabaseConfig()

    @Bean
    @ConditionalOnMissingBean
    fun defaultConnectionModifier() = ConnectionModifier { /* No-Op */ }

    @Bean
    @ConditionalOnMissingBean
    fun defaultDataSetLoader() = XmlLocalResourceDataSetLoader()

    @Bean
    @ConditionalOnMissingBean
    fun defaultDatabaseTester(
        connectionSupplier: DataSourceConnectionSupplier,
        config: DatabaseConfig,
        connectionModifier: ConnectionModifier,
        dataSetLoader: DataSetLoader,
        defaults: List<TableDefaults>
    ): DatabaseTester = DatabaseTester(
        connectionSupplier,
        config,
        connectionModifier,
        dataSetLoader,
        null,
        defaults
    )

}