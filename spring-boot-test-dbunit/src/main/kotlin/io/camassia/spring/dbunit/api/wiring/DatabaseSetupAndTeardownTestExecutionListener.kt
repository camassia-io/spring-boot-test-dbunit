package io.camassia.spring.dbunit.api.wiring

import io.camassia.spring.dbunit.api.DatabaseSetup
import io.camassia.spring.dbunit.api.DatabaseTeardown
import io.camassia.spring.dbunit.api.DatabaseTester
import org.springframework.core.Ordered
import org.springframework.test.context.TestContext
import org.springframework.test.context.TestExecutionListener
import kotlin.reflect.KClass

class DatabaseSetupAndTeardownTestExecutionListener : TestExecutionListener, Ordered {

    override fun getOrder(): Int = Ordered.LOWEST_PRECEDENCE

    override fun beforeTestExecution(ctx: TestContext) {
        (ctx.testClass.annotations + ctx.testMethod.annotations)
            .find(DatabaseSetup::class)
            ?.let { annotation ->
                val dbUnit = ctx.dbUnit()
                dbUnit.givenDataSet(ctx.testClass, annotation.files)
            }
    }

    override fun afterTestExecution(ctx: TestContext) {
        (ctx.testClass.annotations + ctx.testMethod.annotations)
            .find(DatabaseTeardown::class)
            ?.let { annotation ->
                val dbUnit = ctx.dbUnit()
                dbUnit.givenDataSet(ctx.testClass, annotation.files)
            }
    }

    private fun TestContext.dbUnit() = applicationContext.getBean(DatabaseTester::class.java)
    private fun <T : Any> Array<Annotation>.find(clazz: KClass<T>): T? = filterIsInstance(clazz.java).firstOrNull()
}