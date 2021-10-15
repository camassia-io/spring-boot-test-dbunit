package io.camassia.spring.dbunit

import io.camassia.spring.dbunit.api.DatabaseTester
import io.camassia.spring.dbunit.api.connection.ConnectionSupplier
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

    /**
     * Used for enabling DBUnit features e.g. escape pattern etc.
     */
    @Bean
    @ConditionalOnMissingBean
    fun defaultDatabaseConfig() = DatabaseConfig()

    /**
     * Used for modifying the connection *after* the connection provider has supplied it.
     * e.g. Applying post-connection-creation settings.
     */
    @Bean
    @ConditionalOnMissingBean
    fun defaultConnectionModifier() = ConnectionModifier { /* No-Op */ }

    /**
     * Used for modifying the way SpringDbUnit loads dataset files.
     *
     * The default bean loads XML files from src/main/resources.
     */
    @Bean
    @ConditionalOnMissingBean
    fun defaultDataSetLoader() = XmlLocalResourceDataSetLoader()

    /**
     * Used for modifying the underlying DatabaseTester
     *
     * @param connectionSupplier A supplier of a java.sql.Connection.
     * @param config Optional: DbUnit Database Config.
     * @param connectionModifier Optional: A post-creation connection modifier.
     * @param dataSetLoader Optional: A file based dataset loader.
     * @param defaults Optional: A list of Table defaults which are used as fallbacks, when a column isn't set on a particular row, on a particular table.
     *
     * Note: This default implementation sets the DBUnit Schema to null. This may not be desired, and can be overridden by registering your own DatabaseTester Bean.
     */
    @Bean
    @ConditionalOnMissingBean
    fun defaultDatabaseTester(
        connectionSupplier: ConnectionSupplier,
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