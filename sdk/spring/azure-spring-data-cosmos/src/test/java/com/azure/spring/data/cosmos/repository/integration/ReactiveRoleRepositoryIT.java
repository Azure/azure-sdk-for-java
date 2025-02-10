// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.integration;

import com.azure.spring.data.cosmos.IntegrationTestCollectionManager;
import com.azure.spring.data.cosmos.common.TestConstants;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.domain.Role;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.ReactiveRoleRepository;
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
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class ReactiveRoleRepositoryIT {

    private static final Role TEST_ROLE_1 = new Role(TestConstants.ID_1, true,
                                                     TestConstants.ROLE_NAME, TestConstants.LEVEL);
    private static final Role TEST_ROLE_2 = new Role(TestConstants.ID_2, false,
                                                     TestConstants.ROLE_NAME, TestConstants.LEVEL);
    private static final Role TEST_ROLE_3 = new Role(TestConstants.ID_3, true,
                                                     TestConstants.ROLE_NAME, TestConstants.LEVEL);
    private static final Role TEST_ROLE_4 = new Role(TestConstants.ID_4, true,
                                                     TestConstants.ROLE_NAME, TestConstants.LEVEL_2);

    private static final Role TEST_ROLE_5 = new Role(TestConstants.ID_5, true,
        TestConstants.ROLE_NAME_2, TestConstants.LEVEL_2);

    @ClassRule
    public static final IntegrationTestCollectionManager collectionManager = new IntegrationTestCollectionManager();

    @Autowired
    private CosmosTemplate template;
    @Autowired
    private ReactiveRoleRepository repository;

    @Before
    public void setUp() {
        collectionManager.ensureContainersCreatedAndEmpty(template, Role.class);
        final Flux<Role> savedFlux = repository.saveAll(Arrays.asList(TEST_ROLE_1, TEST_ROLE_2, TEST_ROLE_3, TEST_ROLE_4));
        StepVerifier.create(savedFlux).thenConsumeWhile(role -> true).expectComplete().verify();
    }

    @Test
    public void testAnnotatedQuery() {
        Flux<Role> roleFlux = repository.annotatedFindRoleById(TestConstants.ID_1);
        StepVerifier.create(roleFlux).expectNext(TEST_ROLE_1).verifyComplete();
    }

    @Test
    public void testAnnotatedQueryWithOptionalParam() {
        Mono<Role> savedMono = repository.save(TEST_ROLE_5);
        StepVerifier.create(savedMono).thenConsumeWhile(role -> true).expectComplete().verify();

        Optional<String> name = Optional.ofNullable(TestConstants.ROLE_NAME_2);
        Flux<Role> resultFlux = repository.annotatedFindRoleByNameOptional(name);
        StepVerifier.create(resultFlux)
                .expectNext(TEST_ROLE_5)
                .verifyComplete();
    }

    @Test
    public void testAnnotatedQueryWithOptionalParamEmpty() {
        Mono<Role> savedMono = repository.save(TEST_ROLE_5);
        StepVerifier.create(savedMono).thenConsumeWhile(role -> true).expectComplete().verify();

        Flux<Role> resultFlux = repository.annotatedFindRoleByNameOptional(Optional.empty());
        StepVerifier.create(resultFlux)
            .expectNext(TEST_ROLE_1)
            .expectNext(TEST_ROLE_2)
            .expectNext(TEST_ROLE_3)
            .expectNext(TEST_ROLE_4)
            .expectNext(TEST_ROLE_5)
            .verifyComplete();
    }

    @Test
    public void testAnnotatedQueryWithSort() {
        final Flux<Role> roleAscFlux = repository.annotatedFindDeveloperByLevel(TestConstants.LEVEL, Sort.by(Sort.Direction.ASC, "id"));
        StepVerifier.create(roleAscFlux)
            .expectNext(TEST_ROLE_1)
            .expectNext(TEST_ROLE_3)
            .verifyComplete();

        final Flux<Role> roleDescFlux = repository.annotatedFindDeveloperByLevel(TestConstants.LEVEL, Sort.by(Sort.Direction.DESC, "id"));
        StepVerifier.create(roleDescFlux)
            .expectNext(TEST_ROLE_3)
            .expectNext(TEST_ROLE_1)
            .verifyComplete();
    }

    @Test
    public void testAnnotatedQueryWithNewLinesInQueryString() {
        Mono<Role> savedMono = repository.save(TEST_ROLE_5);
        StepVerifier.create(savedMono).thenConsumeWhile(role -> true).expectComplete().verify();

        final Flux<Role> roleAscFlux = repository.annotatedFindRoleByNameWithSort(TestConstants.ROLE_NAME, Sort.by(Sort.Direction.ASC, "id"));
        StepVerifier.create(roleAscFlux)
            .expectNext(TEST_ROLE_1)
            .expectNext(TEST_ROLE_2)
            .expectNext(TEST_ROLE_3)
            .expectNext(TEST_ROLE_4)
            .verifyComplete();
    }

    @Test
    public void testAnnotatedQueryWithNewLinesInQueryString2() {
        Mono<Role> savedMono = repository.save(TEST_ROLE_5);
        StepVerifier.create(savedMono).thenConsumeWhile(role -> true).expectComplete().verify();

        final Flux<Role> roleAscFlux = repository.annotatedFindRoleByNameWithSort2(TestConstants.ROLE_NAME, Sort.by(Sort.Direction.ASC, "id"));
        StepVerifier.create(roleAscFlux)
            .expectNext(TEST_ROLE_1)
            .expectNext(TEST_ROLE_2)
            .expectNext(TEST_ROLE_3)
            .expectNext(TEST_ROLE_4)
            .verifyComplete();
    }

    @Test
    public void testAnnotatedFindAllWithSortAsc() {
        final Flux<Role> roleAscFlux = repository.annotatedFindAllWithSort(Sort.by(Sort.Direction.ASC, "id"));
        StepVerifier.create(roleAscFlux)
            .expectNext(TEST_ROLE_1)
            .expectNext(TEST_ROLE_2)
            .expectNext(TEST_ROLE_3)
            .expectNext(TEST_ROLE_4)
            .verifyComplete();
    }

    @Test
    public void testAnnotatedFindAllWithSortDesc() {
        final Flux<Role> roleAscFlux = repository.annotatedFindAllWithSort(Sort.by(Sort.Direction.DESC, "id"));
        StepVerifier.create(roleAscFlux)
            .expectNext(TEST_ROLE_4)
            .expectNext(TEST_ROLE_3)
            .expectNext(TEST_ROLE_2)
            .expectNext(TEST_ROLE_1)
            .verifyComplete();
    }

    @Test
    public void testAnnotatedQueryWithMultipleLevels() {
        List<String> levels = new ArrayList<>();
        levels.add(TestConstants.LEVEL);
        final Flux<Role> roleAscFlux = repository.annotatedFindRoleByLevelIn(levels, Sort.by(Sort.Direction.ASC, "id"));
        StepVerifier.create(roleAscFlux)
            .expectNext(TEST_ROLE_1)
            .expectNext(TEST_ROLE_2)
            .expectNext(TEST_ROLE_3)
            .verifyComplete();

        List<String> levels2 = new ArrayList<>();
        levels2.add(TestConstants.LEVEL);
        levels2.add(TestConstants.LEVEL_2);
        final Flux<Role> roleAscFlux2 = repository.annotatedFindRoleByLevelIn(levels2, Sort.by(Sort.Direction.ASC, "id"));
        StepVerifier.create(roleAscFlux2)
            .expectNext(TEST_ROLE_1)
            .expectNext(TEST_ROLE_2)
            .expectNext(TEST_ROLE_3)
            .expectNext(TEST_ROLE_4)
            .verifyComplete();
    }

    @Test
    public void testSaveAllWithPublisher() {
        final Mono<Void> deleteAll = repository.deleteAll();
        StepVerifier.create(deleteAll).verifyComplete();
        Flux<Role> itemsToSave = Flux.fromIterable(Arrays.asList(TEST_ROLE_1, TEST_ROLE_2, TEST_ROLE_3, TEST_ROLE_4));
        final Flux<Role> savedFlux = repository.saveAll(itemsToSave);
        StepVerifier.create(savedFlux).thenConsumeWhile(role -> true).expectComplete().verify();
    }

    @Test
    public void testDeleteAllWithIterable() {
        final Mono<Void> deleteFlux = repository.deleteAll(Arrays.asList(TEST_ROLE_1, TEST_ROLE_2, TEST_ROLE_3, TEST_ROLE_4));
        StepVerifier.create(deleteFlux).verifyComplete();
        final Flux<Role> results = repository.findAll();
        StepVerifier.create(results).expectNextCount(0).verifyComplete();

    }
    @Test
    public void testDeleteAllWithPublisher() {
        Flux<Role> itemsToDelete = Flux.fromIterable(Arrays.asList(TEST_ROLE_1, TEST_ROLE_2, TEST_ROLE_3, TEST_ROLE_4));
        final Mono<Void> deleteFlux = repository.deleteAll(itemsToDelete);
        StepVerifier.create(deleteFlux).verifyComplete();
        final Flux<Role> results = repository.findAll();
        StepVerifier.create(results).expectNextCount(0).verifyComplete();

    }

}
