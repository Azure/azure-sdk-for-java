// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.integration;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.ExcludedPath;
import com.azure.cosmos.models.IncludedPath;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.models.PartitionKey;
import com.azure.spring.data.cosmos.CosmosFactory;
import com.azure.spring.data.cosmos.common.TestConstants;
import com.azure.spring.data.cosmos.common.TestUtils;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter;
import com.azure.spring.data.cosmos.core.mapping.CosmosMappingContext;
import com.azure.spring.data.cosmos.domain.Role;
import com.azure.spring.data.cosmos.domain.TimeToLiveSample;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.RoleRepository;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScanner;
import org.springframework.context.ApplicationContext;
import org.springframework.data.annotation.Persistent;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class CosmosAnnotationIT {

    private static final Role TEST_ROLE_1 = new Role(TestConstants.ID_1, true, TestConstants.LEVEL,
        TestConstants.ROLE_NAME);
    private static final Role TEST_ROLE_2 = new Role(TestConstants.ID_2, false, TestConstants.LEVEL,
        TestConstants.ROLE_NAME);
    private static final Role TEST_ROLE_3 = new Role(TestConstants.ID_3, true, TestConstants.LEVEL,
        TestConstants.ROLE_NAME);

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private CosmosConfig cosmosConfig;
    @Autowired
    private CosmosClientBuilder cosmosClientBuilder;
    @Autowired
    private RoleRepository repository;

    private static CosmosTemplate cosmosTemplate;
    private static CosmosContainerProperties collectionRole;
    private static CosmosContainerProperties collectionSample;
    private static CosmosEntityInformation<Role, String> roleInfo;
    private static CosmosEntityInformation<TimeToLiveSample, String> sampleInfo;

    private static boolean initialized;

    @Before
    public void setUp() throws ClassNotFoundException {
        if (!initialized) {
            CosmosAsyncClient client = CosmosFactory.createCosmosAsyncClient(cosmosClientBuilder);
            final CosmosFactory cosmosFactory = new CosmosFactory(client, TestConstants.DB_NAME);

            roleInfo = new CosmosEntityInformation<>(Role.class);
            sampleInfo = new CosmosEntityInformation<>(TimeToLiveSample.class);
            final CosmosMappingContext mappingContext = new CosmosMappingContext();

            mappingContext.setInitialEntitySet(new EntityScanner(this.applicationContext).scan(Persistent.class));

            final MappingCosmosConverter mappingConverter = new MappingCosmosConverter(mappingContext, null);

            cosmosTemplate = new CosmosTemplate(cosmosFactory, cosmosConfig, mappingConverter);
            collectionRole = cosmosTemplate.createContainerIfNotExists(roleInfo);
            collectionSample = cosmosTemplate.createContainerIfNotExists(sampleInfo);

            initialized = true;
        }

        repository.saveAll(Arrays.asList(TEST_ROLE_1, TEST_ROLE_2, TEST_ROLE_3)).collectList().block();
    }

    @After
    public void cleanUp() {
        repository.deleteAll().block();
    }

    @AfterClass
    public static void afterClassCleanup() {
        cosmosTemplate.deleteContainer(roleInfo.getContainerName());
        cosmosTemplate.deleteContainer(sampleInfo.getContainerName());
    }

    @Test
    public void testFindAll() {
        Flux<Role> findAll = repository.findAll();
        StepVerifier.create(findAll).expectNextCount(3).verifyComplete();
    }

    @Test
    public void testFindByPartitionKey() {
        Flux<Role> findAll = repository.findAll(new PartitionKey(false));
        StepVerifier.create(findAll).expectNext(TEST_ROLE_2).verifyComplete();
    }

    @Test
    public void testFindByIdAndPartitionKey() {
        Mono<Role> findAll = repository.findById(TEST_ROLE_2.getId(), new PartitionKey(false));
        StepVerifier.create(findAll).expectNext(TEST_ROLE_2).verifyComplete();
    }

    @Test
    public void testFindByIdAndPartitionKeyNotFound() {
        Mono<Role> findByIdNotFound = repository.findById(TEST_ROLE_2.getId(), new PartitionKey(true));
        StepVerifier.create(findByIdNotFound).expectNextCount(0).verifyComplete();
    }

    @Test
    public void testSave() {
        final Role testRole = new Role(TestConstants.ID_4, true, TestConstants.LEVEL,
            TestConstants.ROLE_NAME);
        Mono<Role> save = repository.save(testRole);
        StepVerifier.create(save).expectNext(testRole).verifyComplete();
    }

    @Test
    public void testDelete() {
        Mono<Void> delete = repository.delete(TEST_ROLE_2);
        StepVerifier.create(delete).verifyComplete();

        Flux<Role> findAll = repository.findAll(new PartitionKey(true));
        StepVerifier.create(findAll).expectNextCount(2).verifyComplete();
    }

    @Test
    public void testDeleteByIdAndPartitionKey() {
        Mono<Void> delete = repository.deleteById(TEST_ROLE_1.getId(), new PartitionKey(true));
        StepVerifier.create(delete).verifyComplete();

        Flux<Role> findAll = repository.findAll();
        StepVerifier.create(findAll).expectNextCount(2).verifyComplete();
    }

    @Test
    public void testTimeToLiveAnnotation() {
        Integer timeToLive = sampleInfo.getTimeToLive();
        assertThat(timeToLive).isEqualTo(collectionSample.getDefaultTimeToLiveInSeconds());

        timeToLive = roleInfo.getTimeToLive();
        assertThat(timeToLive).isEqualTo(collectionRole.getDefaultTimeToLiveInSeconds());
    }

    @Test
    public void testIndexingPolicyAnnotation() {
        final IndexingPolicy policy = collectionRole.getIndexingPolicy();

        Assert.isTrue(policy.getIndexingMode() == TestConstants.INDEXING_POLICY_MODE,
            "unmatched collection policy indexing mode of class Role");
        Assert.isTrue(policy.isAutomatic() == TestConstants.INDEXING_POLICY_AUTOMATIC,
            "unmatched collection policy automatic of class Role");

        TestUtils.testIndexingPolicyPathsEquals(policy.getIncludedPaths()
                                                      .stream()
                                                      .map(IncludedPath::getPath)
                                                      .collect(Collectors.toList()),
            TestConstants.INCLUDED_PATHS);
        TestUtils.testIndexingPolicyPathsEquals(policy.getExcludedPaths()
                                                      .stream()
                                                      .map(ExcludedPath::getPath)
                                                      .collect(Collectors.toList()),
            TestConstants.EXCLUDED_PATHS);
    }
}

