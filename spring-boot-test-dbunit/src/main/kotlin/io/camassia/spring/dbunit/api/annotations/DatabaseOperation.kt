package io.camassia.spring.dbunit.api.annotations

import org.dbunit.operation.DatabaseOperation as Delegate

enum class DatabaseOperation(val underlying: Delegate) {
    UPDATE(Delegate.UPDATE),
    INSERT(Delegate.INSERT),
    REFRESH(Delegate.REFRESH),
    DELETE(Delegate.DELETE),
    DELETE_ALL(Delegate.DELETE_ALL),
    TRUNCATE_TABLE(Delegate.TRUNCATE_TABLE),
    CLEAN_INSERT(Delegate.CLEAN_INSERT)
}
