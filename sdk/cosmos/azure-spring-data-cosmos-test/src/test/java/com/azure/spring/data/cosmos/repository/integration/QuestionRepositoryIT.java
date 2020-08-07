// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.integration;

import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.domain.Question;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.QuestionRepository;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class QuestionRepositoryIT {

    private static final String QUESTION_ID = "question-id";

    private static final String QUESTION_URL = "http://xxx.html";

    private static final Question QUESTION = new Question(QUESTION_ID, QUESTION_URL);

    private static final CosmosEntityInformation<Question, String> entityInformation =
            new CosmosEntityInformation<>(Question.class);

    private static CosmosTemplate staticTemplate;
    private static boolean isSetupDone;

    @Autowired
    private CosmosTemplate template;

    @Autowired
    private QuestionRepository repository;

    @Before
    public void setUp() {
        if (!isSetupDone) {
            staticTemplate = template;
            template.createContainerIfNotExists(entityInformation);
        }
        this.repository.save(QUESTION);
        isSetupDone = true;
    }

    @After
    public void cleanup() {
        this.repository.deleteAll();
    }

    @AfterClass
    public static void afterClassCleanup() {
        staticTemplate.deleteContainer(entityInformation.getContainerName());
    }

    @Test
    public void testFindById() {
        final Optional<Question> optional = this.repository.findById(QUESTION_ID);

        Assert.assertTrue(optional.isPresent());
        Assert.assertEquals(QUESTION, optional.get());
    }

    @Test
    public void testFindByIdNull() {
        final Optional<Question> byId = this.repository.findById(QUESTION_URL);
        Assert.assertFalse(byId.isPresent());
    }

    @Test
    public void testFindAll() {
        final List<Question> questions = Lists.newArrayList(this.repository.findAll());

        Assert.assertEquals(Collections.singletonList(QUESTION), questions);
    }

    @Test
    public void testDelete() {
        Optional<Question> optional = this.repository.findById(QUESTION_ID);

        Assert.assertTrue(optional.isPresent());
        Assert.assertEquals(QUESTION, optional.get());

        this.repository.delete(QUESTION);
        optional = this.repository.findById(QUESTION_ID);

        Assert.assertFalse(optional.isPresent());
    }
}

