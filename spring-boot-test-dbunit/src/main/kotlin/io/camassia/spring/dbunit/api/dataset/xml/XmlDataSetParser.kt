package io.camassia.spring.dbunit.api.dataset.xml

import io.camassia.spring.dbunit.api.dataset.DataSetParser
import org.dbunit.dataset.IDataSet
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder

open class XmlDataSetParser: DataSetParser {

    override fun parseDataSet(content: String): IDataSet = underlying.build(
        """
        <?xml version="1.0" encoding="UTF-8"?>
        <dataset>
            $content
        </dataset>
        """
            .trimIndent()
            .byteInputStream()
    )

    companion object {
        private val underlying = FlatXmlDataSetBuilder()
            .also {
                it.isColumnSensing = true
            }
    }

}