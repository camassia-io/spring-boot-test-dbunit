package io.camassia.spring.dbunit

import io.camassia.spring.dbunit.api.DatabaseTester
import io.camassia.spring.dbunit.api.connection.ConnectionSupplier
import io.camassia.spring.dbunit.api.customization.TableDefaults
import io.camassia.spring.dbunit.api.dataset.DataSetParser
import io.camassia.spring.dbunit.api.dataset.xml.XmlDataSetParser
import io.camassia.spring.dbunit.api.extensions.Extensions
import io.camassia.spring.dbunit.api.extensions.FinalTemplatedCellMappingExtension
import io.camassia.spring.dbunit.api.extensions.InitialTemplatedCellMappingExtension
import io.camassia.spring.dbunit.api.extensions.NullCellMappingExtension
import io.camassia.spring.dbunit.api.extensions.ResourceBasedValueCellMappingExtension
import io.camassia.spring.dbunit.api.io.DefaultLocalResourceLoader
import io.camassia.spring.dbunit.api.io.ResourceLoader
import org.dbunit.database.DatabaseConfig
import org.springframework.beans.factory.annotation.Value
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
    fun defaultDatabaseConfig(): DatabaseConfig = DatabaseConfig()

    /**
     * Used for modifying the way SpringDbUnit loads files.
     *
     * The default bean loads files from src/main/resources.
     */
    @Bean
    @ConditionalOnMissingBean
    fun defaultResourceLoader(): ResourceLoader = DefaultLocalResourceLoader()

    /**
     * Used for modifying the way SpringDbUnit parses string datasets.
     *
     * The default bean treats them as XML.
     */
    @Bean
    @ConditionalOnMissingBean
    fun defaultDataSetParser(): DataSetParser = XmlDataSetParser()

    /**
     * Configurable extensions e.g. Mapping values of cells
     * The order here is important
     */
    @Bean
    @ConditionalOnMissingBean
    fun extensions(
        resourceLoader: ResourceLoader, defaults: List<TableDefaults>
    ): Extensions = Extensions(
        listOf(
            // Resolves all known templates and ignores unknowns for further processing
            InitialTemplatedCellMappingExtension,
            // Replaces all [null]s will null
            NullCellMappingExtension,
            // Resolves all file replacements
            ResourceBasedValueCellMappingExtension(resourceLoader),
            // Resolves all remaining templates and nulls any leftover
            FinalTemplatedCellMappingExtension
        ),
        defaults
    )

    /**
     * Used for modifying the underlying DatabaseTester
     *
     * @param connectionSupplier A supplier of a java.sql.Connection.
     * @param config Optional: DbUnit Database Config.
     * @param resourceLoader Optional: A custom loader for resources e.g. file/web based. The default loader will look for files in src/main/resources.
     * @param dataSetParser Optional: A String to IDataSet parser
     * @param extensions Optional: Custom extensions, e.g. handling for \[null], etc
     * @param schema Optional: The Database Schema DbUnit should use, defaults to null.
     *
     * Note: This default implementation sets the DBUnit Schema to null. This may not be desired, and can be overridden by registering your own DatabaseTester Bean.
     */
    @Bean
    @ConditionalOnMissingBean
    fun defaultDatabaseTester(
        connectionSupplier: ConnectionSupplier,
        config: DatabaseConfig,
        resourceLoader: ResourceLoader,
        dataSetParser: DataSetParser,
        extensions: Extensions,
        @Value("\${spring.dbunit.schema:#{null}}") schema: String?
    ): DatabaseTester = DatabaseTester(
        connectionSupplier,
        config,
        resourceLoader,
        dataSetParser,
        extensions,
        schema
    )

}