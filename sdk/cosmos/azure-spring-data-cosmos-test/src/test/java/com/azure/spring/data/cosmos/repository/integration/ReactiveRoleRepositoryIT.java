// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.integration;

import com.azure.spring.data.cosmos.common.TestConstants;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.domain.Role;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.ReactiveRoleRepository;
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
public class ReactiveRoleRepositoryIT {

    private static final Role TEST_ROLE_1 = new Role(TestConstants.ID_1, true, TestConstants.LEVEL,
                                                     TestConstants.ROLE_NAME);
    private static final Role TEST_ROLE_2 = new Role(TestConstants.ID_2, false, TestConstants.LEVEL,
                                                     TestConstants.ROLE_NAME);

    private static final CosmosEntityInformation<Role, String> entityInformation =
        new CosmosEntityInformation<>(Role.class);
    private static CosmosTemplate staticTemplate;
    private static boolean isSetupDone;
    @Autowired
    private CosmosTemplate template;
    @Autowired
    private ReactiveRoleRepository repository;

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
        final Flux<Role> savedFlux = repository.saveAll(Arrays.asList(TEST_ROLE_1, TEST_ROLE_2));
        StepVerifier.create(savedFlux).thenConsumeWhile(role -> true).expectComplete().verify();
        isSetupDone = true;
    }

    @After
    public void cleanup() {
        final Mono<Void> deletedMono = repository.deleteAll();
        StepVerifier.create(deletedMono).thenAwait().verifyComplete();
    }

    @Test
    public void testAnnotatedQuery() {
        Flux<Role> roleFlux = repository.annotatedFindRoleById(TestConstants.ID_1);
        StepVerifier.create(roleFlux).expectNext(TEST_ROLE_1).verifyComplete();
    }
}
