/*
 * Copyright 2017-2020 original authors
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
package io.micronaut.data.jdbc.h2;

import io.micronaut.data.exceptions.EmptyResultException;
import io.micronaut.data.jdbc.runtime.JdbcOperations;
import io.micronaut.data.tck.entities.Author;
import io.micronaut.data.tck.entities.Book;
import jakarta.inject.Singleton;

import jakarta.transaction.Transactional;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

@Singleton
public class H2BookService {
    private final JdbcOperations jdbcOperations;

    public H2BookService(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    @Transactional
    public Author findByName(String name) {
        return jdbcOperations.prepareStatement("SELECT author_.id,author_.name,author_.nick_name,author_books_.id AS _books_id,author_books_.author_id AS _books_author_id,author_books_.title AS _books_title,author_books_.total_pages AS _books_total_pages,author_books_.publisher_id AS _books_publisher_id,author_books_.last_updated AS _books_last_updated, author_books_genre_.id AS _books_genre_id FROM author AS author_ INNER JOIN book author_books_ ON author_.id=author_books_.author_id LEFT JOIN genre author_books_genre_ ON author_books_genre_.id = author_books_.genre_id  WHERE (author_.name = ?)", statement -> {
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Author author = jdbcOperations.readEntity(resultSet, Author.class);
                Set<Book> books = new HashSet<>();
                do {
                    books.add(jdbcOperations.readEntity("_books_", resultSet, Book.class));
                } while (resultSet.next());
                author.setBooks(books);
                return author;
            }
            throw new EmptyResultException();
        });
    }

}
