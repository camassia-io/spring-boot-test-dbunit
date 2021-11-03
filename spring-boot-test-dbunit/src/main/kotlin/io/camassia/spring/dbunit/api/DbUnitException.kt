package io.camassia.spring.dbunit.api

class DbUnitException: AssertionError {

    constructor(message: String): super(message)
    constructor(message: String, cause: Throwable): super(message, cause)

}