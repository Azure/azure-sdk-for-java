// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.repository;

import com.azure.spring.data.gremlin.common.GremlinEntityType;
import com.azure.spring.data.gremlin.common.TestRepositoryConfiguration;
import com.azure.spring.data.gremlin.common.domain.Book;
import com.azure.spring.data.gremlin.common.repository.BookRepository;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestRepositoryConfiguration.class)
public class BookRepositoryIT {

    private static final Integer ID_0 = 0;
    private static final Integer ID_1 = 1;
    private static final Integer ID_2 = 2;
    private static final Integer NO_EXIST_ID = -1;

    private static final String NAME_0 = "name-0";
    private static final String NAME_1 = "name-1";
    private static final String NAME_2 = "name-2";
    private static final String NO_EXIST_NAME = "no-exist-name";

    private static final Double PRINCE_0 = 3.4;
    private static final Double PRINCE_1 = 72.0;
    private static final Double PRINCE_2 = 102.82;
    private static final Double NO_EXIST_PRICE = -144.23;

    private static final Book BOOK_0 = new Book(ID_0, NAME_0, PRINCE_0);
    private static final Book BOOK_1 = new Book(ID_1, NAME_1, PRINCE_1);
    private static final Book BOOK_2 = new Book(ID_2, NAME_2, PRINCE_2);

    private static final List<Book> BOOKS = Arrays.asList(BOOK_0, BOOK_1, BOOK_2);

    @Autowired
    private BookRepository repository;

    @BeforeEach
    public void setup() {
        this.repository.deleteAll();
    }

    private void assertDomainListEquals(@NonNull List<Book> found, @NonNull List<Book> expected) {
        found.sort(Comparator.comparing(Book::getSerialNumber));
        expected.sort(Comparator.comparing(Book::getSerialNumber));

        Assertions.assertEquals(found.size(), expected.size());
        Assertions.assertEquals(found, expected);
    }

    @Test
    public void testDeleteAll() {
        repository.saveAll(BOOKS);

        Assertions.assertTrue(repository.findAll().iterator().hasNext());

        repository.deleteAll();

        Assertions.assertFalse(repository.findAll().iterator().hasNext());
    }

    @Test
    public void testDeleteAllOnType() {
        repository.saveAll(BOOKS);

        Assertions.assertTrue(repository.findAll().iterator().hasNext());

        repository.deleteAll(GremlinEntityType.VERTEX);

        Assertions.assertFalse(repository.findAll().iterator().hasNext());
    }

    @Test
    public void testDeleteAllOnDomain() {
        repository.saveAll(BOOKS);

        Assertions.assertTrue(repository.findAll().iterator().hasNext());

        repository.deleteAll(Book.class);

        Assertions.assertFalse(repository.findAll().iterator().hasNext());
    }

    @Test
    public void testSave() {
        repository.save(BOOK_0);

        Assertions.assertTrue(repository.findById(BOOK_0.getSerialNumber()).isPresent());
        Assertions.assertFalse(repository.findById(BOOK_1.getSerialNumber()).isPresent());
    }

    @Test
    public void testSaveAll() {
        repository.saveAll(BOOKS);

        final List<Book> found = Lists.newArrayList(repository.findAll());

        assertDomainListEquals(found, BOOKS);
    }

    @Test
    public void testFindById() {
        repository.saveAll(BOOKS);

        Optional<Book> optional = repository.findById(BOOK_0.getSerialNumber());

        Assertions.assertTrue(optional.isPresent());
        Assertions.assertEquals(optional.get(), BOOK_0);

        optional = repository.findById(NO_EXIST_ID);

        Assertions.assertFalse(optional.isPresent());
    }

    @Test
    public void testExistsById() {
        repository.saveAll(BOOKS);

        Assertions.assertTrue(repository.existsById(BOOK_0.getSerialNumber()));
        Assertions.assertFalse(repository.existsById(NO_EXIST_ID));
    }

    @Test
    public void testFindAllById() {
        final List<Book> expected = Arrays.asList(BOOK_0, BOOK_1);
        final List<Integer> ids = Arrays.asList(BOOK_0.getSerialNumber(), BOOK_1.getSerialNumber(), NO_EXIST_ID);

        repository.saveAll(BOOKS);

        final List<Book> found = Lists.newArrayList(repository.findAllById(ids));

        assertDomainListEquals(found, expected);

        Assertions.assertFalse(repository.findAllById(Collections.singleton(NO_EXIST_ID)).iterator().hasNext());
    }

    @Test
    public void testCount() {
        repository.saveAll(BOOKS);

        Assertions.assertEquals(repository.count(), BOOKS.size());

        repository.deleteAll();

        Assertions.assertEquals(repository.count(), 0);
    }

    @Test
    public void testDeleteById() {
        repository.saveAll(BOOKS);

        Assertions.assertTrue(repository.findById(BOOK_0.getSerialNumber()).isPresent());

        repository.deleteById(BOOK_0.getSerialNumber());

        Assertions.assertFalse(repository.findById(BOOK_0.getSerialNumber()).isPresent());
    }

    @Test
    public void testVertexCount() {
        repository.saveAll(BOOKS);

        Assertions.assertEquals(repository.vertexCount(), BOOKS.size());
    }

    @Test
    public void testEdgeCount() {
        repository.saveAll(BOOKS);

        Assertions.assertEquals(repository.edgeCount(), 0);
    }

    @Test
    public void testFindByNameOrPrince() {
        repository.saveAll(BOOKS);

        List<Book> found = repository.findByNameOrPrice(BOOK_0.getName(), BOOK_1.getPrice());

        assertDomainListEquals(found, Arrays.asList(BOOK_0, BOOK_1));

        found = repository.findByNameOrPrice(NO_EXIST_NAME, NO_EXIST_PRICE);

        Assertions.assertTrue(found.isEmpty());
    }

    @Test
    public void testFindAllIncomplete() {
        final Book book = new Book(ID_0, null, 2.34);
        final Book bookNullName = new Book(ID_1, "null", 243.34);

        repository.save(book);
        repository.save(bookNullName);

        final Optional<Book> optional = repository.findById(book.getSerialNumber());

        Assertions.assertTrue(optional.isPresent());
        Assertions.assertEquals(optional.get(), book);

        final Optional<Book> optionalNullName = repository.findById(bookNullName.getSerialNumber());

        Assertions.assertTrue(optionalNullName.isPresent());
        Assertions.assertEquals(optionalNullName.get(), bookNullName);

        final List<Book> books = Lists.newArrayList(repository.findAll());

        assertDomainListEquals(books, Arrays.asList(book, bookNullName));
    }
}
