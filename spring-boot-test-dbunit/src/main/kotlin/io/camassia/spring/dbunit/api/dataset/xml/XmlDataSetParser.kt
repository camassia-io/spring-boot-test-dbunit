package io.camassia.spring.dbunit.api.dataset.xml

import io.camassia.spring.dbunit.api.dataset.DataSetParser
import org.dbunit.dataset.IDataSet
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder

open class XmlDataSetParser : DataSetParser {

    override fun parseDataSet(content: String): IDataSet {
        val sanitisedContent = content
            .let {
                if(it.contains("<>") and it.contains("</>")) it.replace("<>", "<dataset>").replace("</>", "</dataset>") else it
            }
            .let {
                if (!it.contains("<dataset", ignoreCase = true)) "<dataset>$it</dataset>" else it
            }
            .let {
                if (!it.contains("<?xml", ignoreCase = true)) "<?xml version=\"1.0\" encoding=\"UTF-8\"?>$it" else it
            }

        return underlying.build(
            sanitisedContent
                .trimIndent()
                .byteInputStream()
        )
    }

    companion object {
        private val underlying = FlatXmlDataSetBuilder()
            .also {
                it.isColumnSensing = true
            }
    }

}