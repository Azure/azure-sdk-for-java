// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.repository.integration;

import com.azure.spring.data.cosmos.core.ReactiveCosmosTemplate;
import com.azure.spring.data.cosmos.domain.ReactiveTeacher;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.ReactiveTeacherRepository;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class ReactiveTeacherRepositoryIT {

    private static final String TEACHER_ID_1 = "1";

    private static final String TEACHER_FIRST_NAME_1 = "FirstName1";

    private static final String DEPARTMENT_LAST_NAME_1 = "LastName1";

    private static final ReactiveTeacher TEACHER_1 = new ReactiveTeacher(TEACHER_ID_1, TEACHER_FIRST_NAME_1, DEPARTMENT_LAST_NAME_1);

    private static final CosmosEntityInformation<ReactiveTeacher, String> entityInformation =
        new CosmosEntityInformation<>(ReactiveTeacher.class);

    private static ReactiveCosmosTemplate staticTemplate;
    private static boolean isSetupDone;

    @Autowired
    private ReactiveCosmosTemplate template;

    @Autowired
    private ReactiveTeacherRepository repository;

    @Before
    public void setUp() {
        if (!isSetupDone) {
            staticTemplate = template;
            template.createContainerIfNotExists(entityInformation);
        }
        final Flux<ReactiveTeacher> savedFlux = repository.saveAll(Arrays.asList(TEACHER_1));
        StepVerifier.create(savedFlux).thenConsumeWhile(ReactiveTeacher -> true).expectComplete().verify();
        isSetupDone = true;
    }

    @After
    public void cleanup() {
        final Mono<Void> deletedMono = repository.deleteAll();
        StepVerifier.create(deletedMono).thenAwait().verifyComplete();
    }

    @AfterClass
    public static void afterClassCleanup() {
        staticTemplate.deleteContainer(entityInformation.getContainerName());
    }

    @Test
    public void testSaveWithSuppressedNullValue() {
        final Mono<Void> deletedMono = repository.deleteAll();
        StepVerifier.create(deletedMono).thenAwait().verifyComplete();
        String teacherId = TEACHER_ID_1 + "-Other";
        final ReactiveTeacher teacher = new ReactiveTeacher(teacherId, TEACHER_FIRST_NAME_1, null);
        final Mono<ReactiveTeacher> saveSecond = repository.save(teacher);
        StepVerifier.create(saveSecond).expectNext(teacher).verifyComplete();

        final Mono<ReactiveTeacher> idMono = repository.findById(teacherId);
        StepVerifier.create(idMono).expectNext(teacher).expectComplete().verify();

        final Mono<Boolean> existFirstNameMono = repository.existsByFirstNameIsNotNull();
        StepVerifier.create(existFirstNameMono).expectNext(true).expectComplete().verify();

        final Mono<Boolean> existLastNameMono = repository.existsByLastNameIsNull();
        StepVerifier.create(existLastNameMono).expectNext(false).expectComplete().verify();
    }
}
