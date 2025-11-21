// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.integration;

import com.azure.spring.data.cosmos.IntegrationTestCollectionManager;
import com.azure.spring.data.cosmos.common.ResponseDiagnosticsTestUtils;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.domain.Question;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.QuestionRepository;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class QuestionRepositoryIT {

    private static final String QUESTION_ID = "question-id";

    private static final String QUESTION_URL = "http://xxx.html";

    private static final String NULL_ID = "null-id";

    private static final Question QUESTION = new Question(QUESTION_ID, QUESTION_URL);

    
    public static final IntegrationTestCollectionManager collectionManager = new IntegrationTestCollectionManager();

    @Autowired
    private CosmosTemplate template;

    @Autowired
    private QuestionRepository repository;

    @Autowired
    private ResponseDiagnosticsTestUtils responseDiagnosticsTestUtils;

    @BeforeEach
    public void setUp() {
        collectionManager.ensureContainersCreatedAndEmpty(template, Question.class);
        this.repository.save(QUESTION);
    }

    @Test
    public void testFindById() {
        final Optional<Question> optional = this.repository.findById(QUESTION_ID);
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNull();
        Assertions.assertTrue(optional.isPresent());
        Assertions.assertEquals(QUESTION, optional.get());
    }

    @Test
    public void testFindByIdNull() {
        final Optional<Question> byId = this.repository.findById(NULL_ID);
        Assertions.assertFalse(byId.isPresent());
    }

    @Test
    public void testFindAll() {
        final List<Question> questions = Lists.newArrayList(this.repository.findAll());

        Assertions.assertEquals(Collections.singletonList(QUESTION), questions);
    }

    @Test
    public void testDelete() {
        Optional<Question> optional = this.repository.findById(QUESTION_ID);

        Assertions.assertTrue(optional.isPresent());
        Assertions.assertEquals(QUESTION, optional.get());

        this.repository.delete(QUESTION);
        optional = this.repository.findById(QUESTION_ID);

        Assertions.assertFalse(optional.isPresent());
    }
}

