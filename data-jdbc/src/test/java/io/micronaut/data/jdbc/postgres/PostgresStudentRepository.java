package io.micronaut.data.jdbc.postgres;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.tck.repositories.StudentRepository;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface PostgresStudentRepository extends StudentRepository {
}
