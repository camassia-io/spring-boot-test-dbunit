package io.camassia.spring.dbunit.api.dataset

import org.dbunit.dataset.IDataSet

interface DataSetParser {
    fun parseDataSet(content: String): IDataSet
}