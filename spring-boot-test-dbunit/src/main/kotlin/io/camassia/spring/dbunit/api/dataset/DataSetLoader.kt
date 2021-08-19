package io.camassia.spring.dbunit.api.dataset

import org.dbunit.dataset.IDataSet

fun interface DataSetLoader {
    fun loadDataSet(clazz: Class<*>, location: String): IDataSet?
}