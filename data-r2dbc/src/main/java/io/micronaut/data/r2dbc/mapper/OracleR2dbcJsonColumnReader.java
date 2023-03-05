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
package io.micronaut.data.r2dbc.mapper;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Internal;
import io.micronaut.data.exceptions.DataAccessException;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.runtime.mapper.ResultReader;
import io.micronaut.data.runtime.mapper.sql.SqlJsonColumnReader;
import io.micronaut.serde.oracle.jdbc.json.OracleJdbcJsonTextObjectMapper;
import io.r2dbc.spi.Row;
import jakarta.inject.Singleton;
import oracle.sql.json.OracleJsonObject;

/**
 * The Oracle R2DBC json column reader.
 *
 * @author radovanradic
 * @since 4.0.0
 *
 */
@Singleton
@Requires(classes = OracleJdbcJsonTextObjectMapper.class)
@Internal
class OracleR2dbcJsonColumnReader extends SqlJsonColumnReader<Row> {

    /**
     * The default constructor.
     *
     * @param oracleJdbcJsonTextObjectMapper the oracle JSON object mapper
     */
    public OracleR2dbcJsonColumnReader(OracleJdbcJsonTextObjectMapper oracleJdbcJsonTextObjectMapper) {
        super(oracleJdbcJsonTextObjectMapper);
    }

    @Override
    public Dialect getDialect() {
        return Dialect.ORACLE;
    }

    @Override
    public <T> T readJsonColumn(ResultReader<Row, String> resultReader, Row resultSet, String columnName, Class<T> type) {
        try {
            OracleJsonObject oracleJsonObject = resultSet.get(columnName, OracleJsonObject.class);
            byte[] content = objectMapper.writeValueAsBytes(oracleJsonObject);
            return objectMapper.readValue(content, type);
        } catch (Exception e) {
            throw new DataAccessException("Failed to read from JSON field [" + columnName + "].", e);
        }
    }
}
