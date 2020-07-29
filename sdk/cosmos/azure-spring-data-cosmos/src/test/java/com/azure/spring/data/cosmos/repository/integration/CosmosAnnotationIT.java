// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.integration;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.spring.data.cosmos.CosmosFactory;
import com.azure.spring.data.cosmos.common.TestConstants;
import com.azure.spring.data.cosmos.common.TestUtils;
import com.azure.spring.data.cosmos.config.CosmosClientConfig;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter;
import com.azure.spring.data.cosmos.core.mapping.CosmosMappingContext;
import com.azure.spring.data.cosmos.domain.Role;
import com.azure.spring.data.cosmos.domain.TimeToLiveSample;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScanner;
import org.springframework.context.ApplicationContext;
import org.springframework.data.annotation.Persistent;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class CosmosAnnotationIT {
    private static final Role TEST_ROLE = new Role(TestConstants.ID_1, TestConstants.LEVEL,
            TestConstants.ROLE_NAME);

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private CosmosConfig cosmosConfig;
    @Autowired
    private CosmosClientConfig cosmosClientConfig;

    private static CosmosTemplate cosmosTemplate;
    private static CosmosContainerProperties collectionRole;
    private static CosmosContainerProperties collectionSample;
    private static CosmosEntityInformation<Role, String> roleInfo;
    private static CosmosEntityInformation<TimeToLiveSample, String> sampleInfo;

    private static boolean initialized;

    @Before
    public void setUp() throws ClassNotFoundException {
        if (!initialized) {
            CosmosAsyncClient client = CosmosFactory.createCosmosAsyncClient(cosmosClientConfig);
            final CosmosFactory cosmosFactory = new CosmosFactory(client, cosmosClientConfig.getDatabase());

            roleInfo = new CosmosEntityInformation<>(Role.class);
            sampleInfo = new CosmosEntityInformation<>(TimeToLiveSample.class);
            final CosmosMappingContext mappingContext = new CosmosMappingContext();

            mappingContext.setInitialEntitySet(new EntityScanner(this.applicationContext).scan(Persistent.class));

            final MappingCosmosConverter mappingConverter = new MappingCosmosConverter(mappingContext, null);

            cosmosTemplate = new CosmosTemplate(cosmosFactory, cosmosConfig, mappingConverter);
            initialized = true;
        }
        collectionRole = cosmosTemplate.createContainerIfNotExists(roleInfo);

        collectionSample = cosmosTemplate.createContainerIfNotExists(sampleInfo);

        cosmosTemplate.insert(roleInfo.getContainerName(), TEST_ROLE, null);
    }

    @AfterClass
    public static void afterClassCleanup() {
        cosmosTemplate.deleteContainer(roleInfo.getContainerName());
        cosmosTemplate.deleteContainer(sampleInfo.getContainerName());
    }

    @Test
    public void testTimeToLiveAnnotation() {
        Integer timeToLive = sampleInfo.getTimeToLive();
        assertThat(timeToLive).isEqualTo(collectionSample.getDefaultTimeToLiveInSeconds());

        timeToLive = roleInfo.getTimeToLive();
        assertThat(timeToLive).isEqualTo(collectionRole.getDefaultTimeToLiveInSeconds());
    }

    @Test
    @Ignore // TODO(kuthapar): Ignore this test case for now, will update this from service update.
    public void testIndexingPolicyAnnotation() {
        final IndexingPolicy policy = collectionRole.getIndexingPolicy();

        Assert.isTrue(policy.getIndexingMode() == TestConstants.INDEXING_POLICY_MODE,
                "unmatched collection policy indexing mode of class Role");
        Assert.isTrue(policy.isAutomatic() == TestConstants.INDEXING_POLICY_AUTOMATIC,
            "unmatched collection policy automatic of class Role");

        TestUtils.testIndexingPolicyPathsEquals(policy.getIncludedPaths(), TestConstants.INCLUDED_PATHS);
        TestUtils.testIndexingPolicyPathsEquals(policy.getExcludedPaths(), TestConstants.EXCLUDED_PATHS);
    }
}

