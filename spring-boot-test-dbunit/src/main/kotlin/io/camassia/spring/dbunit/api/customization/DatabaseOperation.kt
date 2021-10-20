package io.camassia.spring.dbunit.api.customization

import org.dbunit.operation.DatabaseOperation as Delegate

/**
 * The underlying DatabaseOperation class cannot be used within annotations, hence this enum class.
 */
enum class DatabaseOperation(val underlying: Delegate) {
    UPDATE(Delegate.UPDATE),
    INSERT(Delegate.INSERT),
    REFRESH(Delegate.REFRESH),
    DELETE(Delegate.DELETE),
    DELETE_ALL(Delegate.DELETE_ALL),
    TRUNCATE_TABLE(Delegate.TRUNCATE_TABLE),
    CLEAN_INSERT(Delegate.CLEAN_INSERT)
}
