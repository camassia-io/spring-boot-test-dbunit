package io.camassia.spring.dbunit.api.dataset.xml

import io.camassia.spring.dbunit.api.dataset.LocalResourceDataSetLoader
import org.dbunit.dataset.IDataSet
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder

open class XmlLocalResourceDataSetLoader : LocalResourceDataSetLoader() {

    override fun loadDataSet(clazz: Class<*>, location: String): IDataSet? = underlying.build(getResourceUrl(clazz, location))

    companion object {
        private val underlying = FlatXmlDataSetBuilder()
            .also {
                it.isColumnSensing = true
            }
    }

}