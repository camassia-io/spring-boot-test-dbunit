package io.camassia.spring.dbunit

import io.camassia.spring.dbunit.api.wiring.DatabaseSetupAndTeardownTestExecutionListener
import org.springframework.context.annotation.Import
import org.springframework.test.context.TestExecutionListeners

/**
 * Enables DB Unit in your Spring Boot Application
 *
 * This works by:
 * - importing the SpringBootTestDbUnitConfiguration configuration file + it's component scan config
 * - adding the DatabaseSetupAndTeardownTestExecutionListener to ensure the text runner picks up DatabaseSetup & DatabaseTeardown annotations
 *
 * This annotation can be applied to a Test class or any superclass it extends providing the class in question is a SpringBootTest
 *
 * All you need to do in addition to using this annotation, is to register a Bean of type ConnectionSupplier
 *
 * @see SpringBootTestDbUnitConfiguration for customisation options
 * @see DatabaseSetupAndTeardownTestExecutionListener for how the DatabaseSetup & DatabaseTeardown annotations are handled
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Import(SpringBootTestDbUnitConfiguration::class)
@TestExecutionListeners(
    DatabaseSetupAndTeardownTestExecutionListener::class,
    mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
annotation class AutoConfigureDbUnit
