package io.camassia.spring.dbunit.api.annotations

import io.camassia.spring.dbunit.api.customization.DatabaseOperation

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class DatabaseTeardown(
    /**
     * A list of filenames
     * e.g. for src/main/resources/demo.xml
     * DatabaseTeardown("demo.xml")
     *
     * Note you can register a custom dataset loader to load from different locations, hide the file extension etc
     */
    vararg val value: String = [],

    /**
     * A list of files
     * e.g. for src/main/resources/demo.xml
     * DatabaseTeardown(files = [File("demo.xml", ...)])
     *
     * Note you can register a custom dataset loader to load from different locations, hide the file extension etc
     */
    val files: Array<File> = [],

    /**
     * A list of tables
     * DatabaseTeardown(
     *   tables = Table(
     *     "demo",
     *     Row(
     *       Cell("id", "123")
     *     )
     *   )
     * )
     *
     * Note you can register a custom dataset loader to load from different locations, hide the file extension etc
     */
    val tables: Array<Table> = [],

    /**
     * An XML dataset
     * DatabaseTeardown(
     *   dataset = """
     *      <demo ID="123"/>
     *   """
     * )
     *
     * Note you can register a custom dataset loader to load from different locations, hide the file extension etc
     */
    val dataset: String = "",

    /**
     * The operation DB Unit should use when persisting this data set
     */
    val operation: DatabaseOperation = DatabaseOperation.CLEAN_INSERT
)
