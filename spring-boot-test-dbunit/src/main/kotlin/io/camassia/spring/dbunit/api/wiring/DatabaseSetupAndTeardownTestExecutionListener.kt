package io.camassia.spring.dbunit.api.wiring

import io.camassia.spring.dbunit.api.DatabaseTester
import io.camassia.spring.dbunit.api.DbUnitException
import io.camassia.spring.dbunit.api.annotations.DatabaseSetup
import io.camassia.spring.dbunit.api.annotations.DatabaseTeardown
import io.camassia.spring.dbunit.api.customization.DatabaseOperation
import io.camassia.spring.dbunit.api.dataset.Cell
import io.camassia.spring.dbunit.api.dataset.File
import io.camassia.spring.dbunit.api.dataset.Table
import org.springframework.core.Ordered
import org.springframework.test.context.TestContext
import org.springframework.test.context.TestExecutionListener
import kotlin.reflect.KClass
import io.camassia.spring.dbunit.api.annotations.File as FileAnnotation
import io.camassia.spring.dbunit.api.annotations.Table as TableAnnotation

class DatabaseSetupAndTeardownTestExecutionListener : TestExecutionListener, Ordered {

    override fun getOrder(): Int = Ordered.LOWEST_PRECEDENCE

    override fun beforeTestExecution(ctx: TestContext) {
        val annotations: Array<Annotation> = ctx.annotations()
        val annotation: DatabaseSetup? = annotations.find(DatabaseSetup::class)
        if (annotation != null) {
            if (!annotation.isValid()) throw DbUnitException("Must use only one type of @DatabaseSetup Migration (value/files/tables)")
            val files: List<File> = annotation.value.map { File(it) }.takeIf { it.isNotEmpty() } ?: annotation.files.map { it.toFile() }
            val tables = annotation.tables.map { it.toTable() }
            givenDataSet(ctx, files, tables, annotation.operation)
        }
    }

    override fun afterTestExecution(ctx: TestContext) {
        val annotations: Array<Annotation> = ctx.annotations()
        val annotation: DatabaseTeardown? = annotations.find(DatabaseTeardown::class)
        if (annotation != null) {
            if (!annotation.isValid()) throw DbUnitException("Must use only one type of @DatabaseTeardown Migration (value/files/tables)")
            val files: List<File> = annotation.value.map { File(it) }.takeIf { it.isNotEmpty() } ?: annotation.files.map { it.toFile() }
            val tables = annotation.tables.map { it.toTable() }
            givenDataSet(ctx, files, tables, annotation.operation)
        }
    }

    private fun givenDataSet(ctx: TestContext, files: Collection<File>, tables: Collection<Table>, operation: DatabaseOperation) {
        files.takeIf { it.isNotEmpty() }
            ?.let {
                val dbUnit = ctx.dbUnit()
                dbUnit.givenDataSet(ctx.testClass, it, operation)
            }
        tables.takeIf { it.isNotEmpty() }
            ?.let {
                val dbUnit = ctx.dbUnit()
                dbUnit.givenDataSet(it, operation)
            }
    }

    private fun DatabaseSetup.isValid() = listOf(
        this.value, this.files, this.tables
    ).filter { it.isNotEmpty() }.size == 1

    private fun DatabaseTeardown.isValid() = listOf(
        this.value, this.files, this.tables
    ).filter { it.isNotEmpty() }.size == 1

    private fun FileAnnotation.toFile() = File(
        this.name,
        this.overrides.map { override ->
            Cell(
                override.key,
                override.value
            )
        }.toSet()
    )

    private fun TableAnnotation.toTable() = Table(
        this.name,
        this.rows.map { row ->
            Table.Row(row.cells.associate { cell -> cell.name to cell.value })
        }
    )

    private fun TestContext.annotations() = (this.testClass.annotations + this.testMethod.annotations)
    private fun TestContext.dbUnit() = applicationContext.getBean(DatabaseTester::class.java)
    private fun <T : Any> Array<Annotation>.find(clazz: KClass<T>): T? = filterIsInstance(clazz.java).firstOrNull()
}
