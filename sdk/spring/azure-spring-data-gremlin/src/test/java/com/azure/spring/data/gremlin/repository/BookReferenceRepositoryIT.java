// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.repository;

import com.azure.spring.data.gremlin.common.GremlinEntityType;
import com.azure.spring.data.gremlin.common.TestRepositoryConfiguration;
import com.azure.spring.data.gremlin.common.domain.Book;
import com.azure.spring.data.gremlin.common.domain.BookReference;
import com.azure.spring.data.gremlin.common.repository.BookReferenceRepository;
import com.azure.spring.data.gremlin.common.repository.BookRepository;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfiguration.class)
public class BookReferenceRepositoryIT {

    private static final Integer BOOK_ID_0 = 0;
    private static final Integer BOOK_ID_1 = 1;
    private static final Integer BOOK_ID_2 = 2;
    private static final Integer BOOK_REFERENCE_ID_0 = 3;
    private static final Integer BOOK_REFERENCE_ID_1 = 4;
    private static final Integer NO_EXIST_ID = -1;

    private static final String NAME_0 = "name-0";
    private static final String NAME_1 = "name-1";
    private static final String NAME_2 = "name-2";

    private static final Double PRINCE_0 = 3.4;
    private static final Double PRINCE_1 = 72.0;
    private static final Double PRINCE_2 = 102.82;

    private static final Book BOOK_0 = new Book(BOOK_ID_0, NAME_0, PRINCE_0);
    private static final Book BOOK_1 = new Book(BOOK_ID_1, NAME_1, PRINCE_1);
    private static final Book BOOK_2 = new Book(BOOK_ID_2, NAME_2, PRINCE_2);

    private static final BookReference BOOK_REFERENCE_0 = new BookReference(BOOK_REFERENCE_ID_0, BOOK_ID_0, BOOK_ID_2);
    private static final BookReference BOOK_REFERENCE_1 = new BookReference(BOOK_REFERENCE_ID_1, BOOK_ID_1, BOOK_ID_2);

    private static final List<Book> BOOKS = Arrays.asList(BOOK_0, BOOK_1, BOOK_2);
    private static final List<BookReference> BOOK_REFERENCES = Arrays.asList(BOOK_REFERENCE_0, BOOK_REFERENCE_1);

    @Autowired
    private BookReferenceRepository referenceRepository;

    @Autowired
    private BookRepository bookRepository;

    @Before
    public void setup() {
        this.referenceRepository.deleteAll();
        this.bookRepository.deleteAll();
    }

    private void assertDomainListEquals(@NonNull List<BookReference> found, @NonNull List<BookReference> expected) {
        found.sort(Comparator.comparing(BookReference::getId));
        expected.sort(Comparator.comparing(BookReference::getId));

        Assert.assertEquals(found.size(), expected.size());
        Assert.assertEquals(found, expected);
    }

    @Test
    public void testDeleteAll() {
        bookRepository.saveAll(BOOKS);
        referenceRepository.saveAll(BOOK_REFERENCES);

        Assert.assertTrue(referenceRepository.findAll().iterator().hasNext());

        referenceRepository.deleteAll();

        Assert.assertFalse(referenceRepository.findAll().iterator().hasNext());
    }

    @Test
    public void testDeleteAllOnType() {
        bookRepository.saveAll(BOOKS);
        referenceRepository.saveAll(BOOK_REFERENCES);

        Assert.assertTrue(referenceRepository.findAll().iterator().hasNext());

        referenceRepository.deleteAll(GremlinEntityType.EDGE);

        Assert.assertFalse(referenceRepository.findAll().iterator().hasNext());
    }

    @Test
    public void testDeleteAllOnDomain() {
        bookRepository.saveAll(BOOKS);
        referenceRepository.saveAll(BOOK_REFERENCES);

        Assert.assertTrue(referenceRepository.findAll().iterator().hasNext());

        referenceRepository.deleteAll(BookReference.class);

        Assert.assertFalse(referenceRepository.findAll().iterator().hasNext());
    }

    @Test
    public void testSave() {
        bookRepository.saveAll(BOOKS);
        referenceRepository.save(BOOK_REFERENCE_0);

        Assert.assertTrue(referenceRepository.findById(BOOK_REFERENCE_0.getId()).isPresent());
        Assert.assertFalse(referenceRepository.findById(BOOK_REFERENCE_1.getId()).isPresent());
    }

    @Test
    public void testSaveAll() {
        bookRepository.saveAll(BOOKS);
        referenceRepository.saveAll(BOOK_REFERENCES);

        final List<BookReference> found = Lists.newArrayList(referenceRepository.findAll());

        assertDomainListEquals(found, BOOK_REFERENCES);
    }

    @Test
    public void testFindById() {
        bookRepository.saveAll(BOOKS);
        referenceRepository.saveAll(BOOK_REFERENCES);

        Optional<BookReference> optional = referenceRepository.findById(BOOK_REFERENCE_0.getId());

        Assert.assertTrue(optional.isPresent());
        Assert.assertEquals(optional.get(), BOOK_REFERENCE_0);

        optional = referenceRepository.findById(NO_EXIST_ID);

        Assert.assertFalse(optional.isPresent());
    }

    @Test
    public void testExistsById() {
        bookRepository.saveAll(BOOKS);
        referenceRepository.saveAll(BOOK_REFERENCES);

        Assert.assertTrue(referenceRepository.existsById(BOOK_REFERENCE_0.getId()));
        Assert.assertFalse(referenceRepository.existsById(NO_EXIST_ID));
    }

    @Test
    public void testFindAllById() {
        bookRepository.saveAll(BOOKS);
        referenceRepository.saveAll(BOOK_REFERENCES);

        final List<Integer> ids = Arrays.asList(BOOK_REFERENCE_0.getId(), BOOK_REFERENCE_1.getId());
        final List<BookReference> found = Lists.newArrayList(referenceRepository.findAllById(ids));

        assertDomainListEquals(found, BOOK_REFERENCES);

        Assert.assertFalse(referenceRepository.findAllById(Collections.singleton(NO_EXIST_ID)).iterator().hasNext());
    }

    @Test
    public void testCount() {
        bookRepository.saveAll(BOOKS);
        referenceRepository.saveAll(BOOK_REFERENCES);

        Assert.assertEquals(referenceRepository.count(), BOOK_REFERENCES.size() + BOOKS.size());

        referenceRepository.deleteAll();

        Assert.assertEquals(referenceRepository.count(), 0);
    }

    @Test
    public void testDeleteById() {
        bookRepository.saveAll(BOOKS);
        referenceRepository.saveAll(BOOK_REFERENCES);

        Assert.assertTrue(referenceRepository.findById(BOOK_REFERENCE_0.getId()).isPresent());

        referenceRepository.deleteById(BOOK_REFERENCE_0.getId());

        Assert.assertFalse(referenceRepository.findById(BOOK_REFERENCE_0.getId()).isPresent());
    }

    @Test
    public void testEdgeCount() {
        bookRepository.saveAll(BOOKS);
        referenceRepository.saveAll(BOOK_REFERENCES);

        Assert.assertEquals(referenceRepository.edgeCount(), BOOK_REFERENCES.size());
    }
}
