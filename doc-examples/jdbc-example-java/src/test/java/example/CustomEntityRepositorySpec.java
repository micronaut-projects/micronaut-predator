package example;

import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Slice;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@MicronautTest
class CustomEntityRepositorySpec {

    @Inject
    CustomEntityRepository repository;

    @Test
    void testSaveAndFind() {
        CustomEntity entity = repository.save(new CustomEntity(null, "Entity1"));
        CustomEntity found = repository.findById(entity.id()).orElse(null);
        Assertions.assertNotNull(found);
        Assertions.assertEquals(entity.name(), found.name());
        Assertions.assertEquals(1, repository.count());
        Assertions.assertFalse(repository.findAll().isEmpty());

        repository.save(new CustomEntity(null, "Entity2"));
        repository.save(new CustomEntity(null, "Entity3"));
        Slice<CustomEntity> slice = repository.findAll(Pageable.from(0, 2));
        Assertions.assertEquals(2, slice.getSize());

        repository.deleteAll();
    }
}
