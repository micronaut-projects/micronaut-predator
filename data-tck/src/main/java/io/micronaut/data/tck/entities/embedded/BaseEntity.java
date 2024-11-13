package io.micronaut.data.tck.entities.embedded;

public interface BaseEntity<I, S extends Enum<S>> {

    I id();

    ResourceEntity<S> resource();
}
