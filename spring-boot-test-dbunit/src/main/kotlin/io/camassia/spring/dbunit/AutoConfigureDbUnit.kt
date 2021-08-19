package io.camassia.spring.dbunit

import io.camassia.spring.dbunit.api.wiring.DatabaseSetupAndTeardownTestExecutionListener
import org.springframework.context.annotation.Import
import org.springframework.test.context.TestExecutionListeners

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Import(SpringBootTestDbUnitConfiguration::class)
@TestExecutionListeners(DatabaseSetupAndTeardownTestExecutionListener::class)
annotation class AutoConfigureDbUnit
