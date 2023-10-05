package io.camassia.spring.dbunit

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlGroup

@SqlGroup(
    Sql(
        statements = [
            "CREATE TABLE demo1 (id BIGINT NOT NULL, name VARCHAR(50), CONSTRAINT demo1_pk PRIMARY KEY (id))",
            "CREATE TABLE demo2 (id BIGINT NOT NULL, name VARCHAR(50), CONSTRAINT demo2_pk PRIMARY KEY (id))",
            "CREATE TABLE demo3 (id BIGINT NOT NULL, name VARCHAR(50), CONSTRAINT demo3_pk PRIMARY KEY (id))",
        ],
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    ),
    Sql(
        statements = [
            "DROP TABLE demo1",
            "DROP TABLE demo2",
            "DROP TABLE demo3",
        ],
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
)
abstract class RepositoryTest {

    @Autowired
    private lateinit var jdbc: JdbcTemplate

    fun insertInto(table: String, id: Long, name: String) = jdbc.update("INSERT INTO $table (id, name) VALUES (?, ?)",id, name)

    fun selectAllFrom(table: String) = jdbc.query("SELECT id, name FROM $table", mapper)

    companion object {
        private val mapper = RowMapper<Pair<Long, String>> { rs, _ ->
            Pair(
                rs.getLong("id"),
                rs.getString("name")
            )
        }
    }

}
