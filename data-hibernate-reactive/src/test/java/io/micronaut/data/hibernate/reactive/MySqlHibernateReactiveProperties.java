package io.micronaut.data.hibernate.reactive;

import io.micronaut.test.support.TestPropertyProvider;

import java.util.Map;

public interface MySqlHibernateReactiveProperties extends TestPropertyProvider {

    @Override
    default Map<String, String> getProperties() {
        return Map.of(
            "jpa.default.properties.hibernate.hbm2ddl.auto", "create-drop",
            "jpa.default.reactive", "true",
            "jpa.default.properties.hibernate.connection.db-type", "mysql"
        );
    }
}
