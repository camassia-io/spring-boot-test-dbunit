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

    override fun beforeTestExecution(ctx: TestContext) = ctx.handleAnnotation(DatabaseSetup::class, { it.value }, { it.files }, { it.tables }, { it.operation })

    override fun afterTestExecution(ctx: TestContext) = ctx.handleAnnotation(DatabaseTeardown::class, { it.value }, { it.files }, { it.tables }, { it.operation })

    private fun <T : Any> TestContext.handleAnnotation(
        clazz: KClass<T>,
        filenamesOf: (T) -> Array<out String>,
        filesOf: (T) -> Array<FileAnnotation>,
        tablesOf: (T) -> Array<TableAnnotation>,
        operationOf: (T) -> DatabaseOperation
    ) {
        val annotations = this.annotations().find(clazz)
        if(annotations.isEmpty()) return

        // Validate Each Annotation uses a valid property combination
        if (annotations.any { a -> listOf(filenamesOf(a), filesOf(a), tablesOf(a)).filter { it.isNotEmpty() }.size != 1 }) {
            throw DbUnitException("Must use only one type of @${clazz.simpleName} Migration (value/files/tables)")
        }

        annotations.forEach { annotation ->
            val files: List<File> = filenamesOf(annotation).map { File(it) }.takeIf { it.isNotEmpty() } ?: filesOf(annotation).map { it.toFile() }
            val tables = tablesOf(annotation).map { it.toTable() }
            files.takeIf { it.isNotEmpty() }
                ?.let {
                    val dbUnit = dbUnit()
                    dbUnit.givenDataSet(testClass, it, operationOf(annotation))
                }
            tables.takeIf { it.isNotEmpty() }
                ?.let {
                    val dbUnit = dbUnit()
                    dbUnit.givenDataSet(it, operationOf(annotation))
                }
        }
    }

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
    private fun <T : Any> Array<Annotation>.find(clazz: KClass<T>): List<T> = filterIsInstance(clazz.java)
}
