// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.repository.integration;

import com.azure.spring.data.cosmos.ReactiveIntegrationTestCollectionManager;
import com.azure.spring.data.cosmos.core.ReactiveCosmosTemplate;
import com.azure.spring.data.cosmos.domain.ReactiveTeacher;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.ReactiveTeacherRepository;
import org.junit.Before;
import org.junit.ClassRule;
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

    @ClassRule
    public static final ReactiveIntegrationTestCollectionManager collectionManager = new ReactiveIntegrationTestCollectionManager();

    @Autowired
    private ReactiveCosmosTemplate template;

    @Autowired
    private ReactiveTeacherRepository repository;

    @Before
    public void setUp() {
        collectionManager.ensureContainersCreatedAndEmpty(template, ReactiveTeacher.class);
        final Flux<ReactiveTeacher> savedFlux = repository.saveAll(Arrays.asList(TEACHER_1));
        StepVerifier.create(savedFlux).thenConsumeWhile(ReactiveTeacher -> true).expectComplete().verify();
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
