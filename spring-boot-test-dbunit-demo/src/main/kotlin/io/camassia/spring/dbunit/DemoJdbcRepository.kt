package io.camassia.spring.dbunit

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component

@Component
class DemoJdbcRepository(private val jdbc: JdbcTemplate) {

    fun createTable() = jdbc.execute("CREATE TABLE demo (id BIGINT NOT NULL, name VARCHAR(50) NOT NULL, CONSTRAINT demo_pk PRIMARY KEY (id))")

    fun dropTable() = jdbc.execute("DROP TABLE demo")

    fun insert(value: DemoDao) = jdbc.update("INSERT INTO demo (id, name) VALUES (?, ?)", value.id, value.name)

    fun selectAll() = jdbc.query(
        "SELECT id, name FROM demo",
        RowMapper<DemoDao> { rs, _ ->
            DemoDao(
                rs.getLong("id"),
                rs.getString("name")
            )
        }
    )

    class DemoDao(val id: Long, val name: String)
}