package example;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Slice;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;

@JdbcRepository(dialect = Dialect.H2)
public interface CustomEntityRepository extends CrudRepository<CustomEntity, Long> {
    Slice<CustomEntity> findAll(Pageable pageable);

    Page<CustomEntity> findByNameIn(List<String> names, Pageable pageable);
}
