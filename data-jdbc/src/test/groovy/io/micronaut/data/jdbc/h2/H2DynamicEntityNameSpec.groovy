package io.micronaut.data.jdbc.h2

import io.micronaut.context.annotation.Property
import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Shared
import spock.lang.Specification


@MicronautTest
@H2DBProperties
@Property(name = "datasources.default.packages", value = "io.micronaut.data.jdbc.h2")
@Property(name = "entity-prefix", value = "simple_robot_")
class H2DynamicEntityNameSpec extends Specification {

    @Inject
    @Shared
    CustomEntityRepository repository

    void "test cascade save"() {
        when:
        def entity = repository.save(new CustomEntity(name: "Entity1"))
        def opt = repository.findById(entity.id)
        then:
        opt.present
        def loadedEntity = opt.get()
        loadedEntity.name == "Entity1"
        repository.count() == 1
        !repository.findAll().empty
        cleanup:
        repository.deleteAll()
    }
}

@MappedEntity("\${entity-prefix}entity")
class CustomEntity {
    @Id
    @GeneratedValue
    Long id

    String name
}

@JdbcRepository(dialect = Dialect.H2)
interface CustomEntityRepository extends CrudRepository<CustomEntity, Long> {
}
