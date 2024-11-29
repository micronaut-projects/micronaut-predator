package example;

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
        repository.deleteAll();
    }
}
