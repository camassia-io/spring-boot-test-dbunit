package io.camassia.spring.dbunit.api.dataset

import org.dbunit.dataset.IDataSet
import java.io.InputStream

interface DataSetParser {
    fun parseDataSet(content: String): IDataSet
    fun parseDataSet(inputStream: InputStream): IDataSet = parseDataSet(inputStream.bufferedReader().use { it.readText() })
}