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
import reactor.test.StepVerifier;

import java.util.Arrays;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class ReactiveRoleRepositoryIT {

    private static final Role TEST_ROLE_1 = new Role(TestConstants.ID_1, true,
                                                     TestConstants.ROLE_NAME, TestConstants.LEVEL);
    private static final Role TEST_ROLE_2 = new Role(TestConstants.ID_2, false,
                                                     TestConstants.ROLE_NAME, TestConstants.LEVEL);
    private static final Role TEST_ROLE_3 = new Role(TestConstants.ID_3, true,
                                                     TestConstants.ROLE_NAME, TestConstants.LEVEL);

    @ClassRule
    public static final IntegrationTestCollectionManager collectionManager = new IntegrationTestCollectionManager();

    @Autowired
    private CosmosTemplate template;
    @Autowired
    private ReactiveRoleRepository repository;

    @Before
    public void setUp() {
        collectionManager.ensureContainersCreatedAndEmpty(template, Role.class);
        final Flux<Role> savedFlux = repository.saveAll(Arrays.asList(TEST_ROLE_1, TEST_ROLE_2, TEST_ROLE_3));
        StepVerifier.create(savedFlux).thenConsumeWhile(role -> true).expectComplete().verify();
    }

    @Test
    public void testAnnotatedQuery() {
        Flux<Role> roleFlux = repository.annotatedFindRoleById(TestConstants.ID_1);
        StepVerifier.create(roleFlux).expectNext(TEST_ROLE_1).verifyComplete();
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

}
