package example;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Slice;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

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

        Page<CustomEntity> page = repository.findByNameIn(List.of("Entity1", "Entity2", "Entity3"),
            Pageable.from(0, 2));
        Assertions.assertEquals(2, page.getSize());
        Assertions.assertEquals(2, page.getTotalPages());
        Assertions.assertEquals(3, page.getTotalSize());

        page = repository.findByNameIn(List.of("Entity1", "Entity2"),
            Pageable.from(0, 2));
        Assertions.assertEquals(2, page.getSize());
        Assertions.assertEquals(1, page.getTotalPages());
        Assertions.assertEquals(2, page.getTotalSize());

        repository.deleteAll();
    }
}
