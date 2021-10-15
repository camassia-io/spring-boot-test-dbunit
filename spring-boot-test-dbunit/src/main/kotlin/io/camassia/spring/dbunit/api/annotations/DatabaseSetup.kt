package io.camassia.spring.dbunit.api.annotations

import io.camassia.spring.dbunit.api.customization.DatabaseOperation

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class DatabaseSetup(
    /**
     * A list of filenames
     * e.g. for src/main/resources/demo.xml
     * DatabaseSetup("demo.xml")
     *
     * Note you can register a custom dataset loader to load from different locations, hide the file extension etc
     */
    vararg val value: String = [],

    /**
     * A list of files
     * e.g. for src/main/resources/demo.xml
     * DatabaseSetup(files = [File("demo.xml", ...)])
     *
     * Note you can register a custom dataset loader to load from different locations, hide the file extension etc
     */
    val files: Array<File> = [],

    /**
     * A list of tables
     * DatabaseSetup(
     *   Table(
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
     * The operation DB Unit should use when persisting this data set
     */
    val operation: DatabaseOperation = DatabaseOperation.CLEAN_INSERT
)
