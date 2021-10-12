package io.camassia.spring.dbunit.api.wiring

import io.camassia.spring.dbunit.api.DatabaseTester
import io.camassia.spring.dbunit.api.DbUnitException
import io.camassia.spring.dbunit.api.annotations.DatabaseSetup
import io.camassia.spring.dbunit.api.annotations.DatabaseTeardown
import io.camassia.spring.dbunit.api.annotations.TemplatedDatabaseSetup
import io.camassia.spring.dbunit.api.annotations.TemplatedDatabaseTeardown
import io.camassia.spring.dbunit.api.dataset.File
import org.springframework.core.Ordered
import org.springframework.test.context.TestContext
import org.springframework.test.context.TestExecutionListener
import kotlin.reflect.KClass
import io.camassia.spring.dbunit.api.annotations.File as FileAnnotation

class DatabaseSetupAndTeardownTestExecutionListener : TestExecutionListener, Ordered {

    override fun getOrder(): Int = Ordered.LOWEST_PRECEDENCE

    override fun beforeTestExecution(ctx: TestContext) {
        val annotations = ctx.annotations()
        val standard = annotations.find(DatabaseSetup::class)?.toFiles()
        val templated = annotations.find(TemplatedDatabaseSetup::class)?.toFiles()
        if(standard != null && templated != null) throw DbUnitException("Cannot use both @DatabaseSetup & @TemplatedDatabaseSetup at the same time")
        (standard ?: templated)
            ?.let { setups ->
                val dbUnit = ctx.dbUnit()
                dbUnit.givenDataSet(ctx.testClass, setups)
            }
    }

    override fun afterTestExecution(ctx: TestContext) {
        val annotations = ctx.annotations()
        val standard = annotations.find(DatabaseTeardown::class)?.toFiles()
        val templated = annotations.find(TemplatedDatabaseTeardown::class)?.toFiles()
        if(standard != null && templated != null) throw DbUnitException("Cannot use both @DatabaseTeardown & @TemplatedDatabaseTeardown at the same time")
        (standard ?: templated)
            ?.let { setups ->
                val dbUnit = ctx.dbUnit()
                dbUnit.givenDataSet(ctx.testClass, setups)
            }
    }

    private fun DatabaseSetup.toFiles() = this.files.map { File(it) }
    private fun DatabaseTeardown.toFiles() = this.files.map { File(it) }
    private fun TemplatedDatabaseSetup.toFiles() = this.files.map { it.toFile() }
    private fun TemplatedDatabaseTeardown.toFiles() = this.files.map { it.toFile() }
    private fun FileAnnotation.toFile() = File(
        this.name,
        this.overrides.map { override ->
            File.CellOverride(
                override.key,
                override.value
            )
        }.toSet()
    )

    private fun TestContext.annotations() = (this.testClass.annotations + this.testMethod.annotations)
    private fun TestContext.dbUnit() = applicationContext.getBean(DatabaseTester::class.java)
    private fun <T : Any> Array<Annotation>.find(clazz: KClass<T>): T? = filterIsInstance(clazz.java).firstOrNull()
}