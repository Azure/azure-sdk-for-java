// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.integration;

import com.azure.spring.data.cosmos.common.TestConstants;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.domain.Book;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.BookRepository;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class BookRepositoryIT {

    private static final Book TEST_BOOK_1 = new Book(TestConstants.ID_1, UUID.randomUUID().toString(),
                                                     "title1");
    private static final Book TEST_BOOK_2 = new Book(TestConstants.ID_2, UUID.randomUUID().toString(),
                                                     "title2");

    private static final CosmosEntityInformation<Book, String> entityInformation =
        new CosmosEntityInformation<>(Book.class);

    private static CosmosTemplate staticTemplate;
    private static boolean isSetupDone;

    @Autowired
    private CosmosTemplate template;

    @Autowired
    private BookRepository repository;

    @AfterClass
    public static void afterClassCleanup() {
        staticTemplate.deleteContainer(entityInformation.getContainerName());
    }

    @Before
    public void setUp() {
        if (!isSetupDone) {
            staticTemplate = template;
            template.createContainerIfNotExists(entityInformation);
        }
        repository.saveAll(Arrays.asList(TEST_BOOK_1, TEST_BOOK_2));
        isSetupDone = true;
    }

    @After
    public void cleanup() {
        repository.deleteAll();
    }

    @Test
    public void testAnnotatedQuery() {
        final List<Book> result = repository.annotatedFindBookById(TEST_BOOK_1.getId());
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getId()).isEqualTo(TEST_BOOK_1.getId());
    }
}
