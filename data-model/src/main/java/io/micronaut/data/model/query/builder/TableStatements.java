/*
 * Copyright 2017-2023 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.data.model.query.builder;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The model holding table create or drop statements (including indexes, foreign keys etc.).
 *
 * @author radovanradic
 * @since 4.0.0
 */
@Internal
public final class TableStatements {

    // If true then creating table, otherwise dropping
    private final boolean create;

    // Table statements for creating or dropping tables. Can contain also sequence creation, index creation and other kind of statements.
    private final List<String> statements = new ArrayList<>();

    // Separately kept foreign key statements since need to be executed prior dropping or after creation
    private final List<String> foreignKeyStatements = new ArrayList<>();

    /**
     * Default constructor.
     *
     * @param create flag indicating whether table is being created or dropped
     */
    public TableStatements(boolean create) {
        this.create = create;
    }

    /**
     * Adds the statement. Can be statement for schema, table, sequence or index.
     * @param statements the table statement(s)
     */
    public void addStatement(@NonNull String... statements) {
        this.statements.addAll(Arrays.asList(statements));
    }

    /**
     * Adds the foreign key statement.
     * @param statements the foreign key statement(s)
     */
    public void addForeignKeyStatement(@NonNull String... statements) {
        this.foreignKeyStatements.addAll(Arrays.asList(statements));
    }

    /**
     * Gets the table statements.
     * @return the table create or drop statements
     */
    public String[] getStatements() {
        return this.statements.toArray(new String[0]);
    }

    /**
     * Gets the foreign key statements.
     * @return the foreign key create or drop statements
     */
    public String[] getForeignKeyStatements() {
        return this.foreignKeyStatements.toArray(new String[0]);
    }

    /**
     * @return all statements for both table and foreign key creation (if present)
     */
    public String[] getAllStatements() {
        List<String> result = new ArrayList<>();
        if (this.create) {
            // For create first create statements and then foreign key
            result.addAll(this.statements);
            result.addAll(this.foreignKeyStatements);
        } else {
            // For drop - inverse
            result.addAll(this.foreignKeyStatements);
            result.addAll(this.statements);
        }
        return result.toArray(new String[0]);
    }
}