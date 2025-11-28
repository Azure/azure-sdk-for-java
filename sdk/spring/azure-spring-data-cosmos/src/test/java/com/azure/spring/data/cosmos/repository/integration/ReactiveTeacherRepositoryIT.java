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
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("deprecation")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class ReactiveTeacherRepositoryIT {

    private static final String TEACHER_ID_1 = "1";

    private static final String TEACHER_ID_2 = "2";

    private static final String TEACHER_ID_3 = "3";

    private static final String TEACHER_FIRST_NAME_1 = "FirstName1";

    private static final String TEACHER_FIRST_NAME_2 = "FirstName2";

    private static final String DEPARTMENT_LAST_NAME_1 = "LastName1";

    private static final String DEPARTMENT_LAST_NAME_2 = "LastName2";

    private static final ReactiveTeacher TEACHER_1 = new ReactiveTeacher(TEACHER_ID_1, TEACHER_FIRST_NAME_1, DEPARTMENT_LAST_NAME_1);

    private static final ReactiveTeacher TEACHER_2 = new ReactiveTeacher(TEACHER_ID_2, TEACHER_FIRST_NAME_1, DEPARTMENT_LAST_NAME_2);

    private static final ReactiveTeacher TEACHER_3 = new ReactiveTeacher(TEACHER_ID_3, TEACHER_FIRST_NAME_2, DEPARTMENT_LAST_NAME_1);

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

    @Test
    public void testAnnotatedQueryWithArrayContains() {
        final Mono<Void> deletedMono = repository.deleteAll();
        StepVerifier.create(deletedMono).thenAwait().verifyComplete();
        final Flux<ReactiveTeacher> savedFlux = repository.saveAll(Arrays.asList(TEACHER_1, TEACHER_2, TEACHER_3));
        StepVerifier.create(savedFlux).thenConsumeWhile(ReactiveTeacher -> true).expectComplete().verify();

        List<String> firstNames = new ArrayList<>();
        firstNames.add(TEACHER_FIRST_NAME_1);
        final Flux<ReactiveTeacher> resultsAsc = repository.annotatedFindByFirstNames(firstNames);
        StepVerifier.create(resultsAsc)
            .expectNextMatches(teacher -> teacher.getId().equals(TEACHER_ID_1))
            .expectNextMatches(teacher -> teacher.getId().equals(TEACHER_ID_2))
            .verifyComplete();

        List<String> firstNames2 = new ArrayList<>();
        firstNames2.add(TEACHER_FIRST_NAME_1);
        firstNames2.add(TEACHER_FIRST_NAME_2);
        final Flux<ReactiveTeacher> resultsAsc2 = repository.annotatedFindByFirstNames(firstNames2);
        StepVerifier.create(resultsAsc2)
            .expectNextMatches(teacher -> teacher.getId().equals(TEACHER_ID_1))
            .expectNextMatches(teacher -> teacher.getId().equals(TEACHER_ID_2))
            .expectNextMatches(teacher -> teacher.getId().equals(TEACHER_ID_3))
            .verifyComplete();

    }

    @Test
    public void testAnnotatedQueryWithArrayContainsAndSort() {
        final Mono<Void> deletedMono = repository.deleteAll();
        StepVerifier.create(deletedMono).thenAwait().verifyComplete();
        final Flux<ReactiveTeacher> savedFlux = repository.saveAll(Arrays.asList(TEACHER_1, TEACHER_2, TEACHER_3));
        StepVerifier.create(savedFlux).thenConsumeWhile(ReactiveTeacher -> true).expectComplete().verify();

        List<String> firstNames = new ArrayList<>();
        firstNames.add(TEACHER_FIRST_NAME_1);
        final Flux<ReactiveTeacher> resultsAsc = repository.annotatedFindByFirstNamesWithSort(firstNames, Sort.by(Sort.Direction.DESC, "id"));
        StepVerifier.create(resultsAsc)
            .expectNextMatches(teacher -> teacher.getId().equals(TEACHER_ID_2))
            .expectNextMatches(teacher -> teacher.getId().equals(TEACHER_ID_1))
            .verifyComplete();

        List<String> firstNames2 = new ArrayList<>();
        firstNames2.add(TEACHER_FIRST_NAME_1);
        firstNames2.add(TEACHER_FIRST_NAME_2);
        final Flux<ReactiveTeacher> resultsAsc2 = repository.annotatedFindByFirstNamesWithSort(firstNames2, Sort.by(Sort.Direction.DESC, "id"));
        StepVerifier.create(resultsAsc2)
            .expectNextMatches(teacher -> teacher.getId().equals(TEACHER_ID_3))
            .expectNextMatches(teacher -> teacher.getId().equals(TEACHER_ID_2))
            .expectNextMatches(teacher -> teacher.getId().equals(TEACHER_ID_1))
            .verifyComplete();

    }
}
