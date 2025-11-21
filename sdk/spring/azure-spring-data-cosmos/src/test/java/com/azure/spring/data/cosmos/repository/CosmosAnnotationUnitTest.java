// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository;

import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.models.UniqueKeyPolicy;
import com.azure.spring.data.cosmos.common.TestConstants;
import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.CosmosIndexingPolicy;
import com.azure.spring.data.cosmos.domain.NoDBAnnotationPerson;
import com.azure.spring.data.cosmos.domain.Role;
import com.azure.spring.data.cosmos.domain.TimeToLiveSample;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;


public class CosmosAnnotationUnitTest {
    private CosmosEntityInformation<NoDBAnnotationPerson, String> personInfo;
    private CosmosEntityInformation<Role, String> roleInfo;

    @BeforeEach
    public void setUp() {
        personInfo = new CosmosEntityInformation<>(NoDBAnnotationPerson.class);
        roleInfo = new CosmosEntityInformation<>(Role.class);
    }

    @Test
    public void testDefaultUniqueKeyPolicyAnnotation() {
        final UniqueKeyPolicy uniqueKeyPolicy = personInfo.getUniqueKeyPolicy();
        Assertions.isNull(uniqueKeyPolicy, "NoDBAnnotationPerson class should not have CosmosUniqueKeyPolicy annotation");
    }

    @Test
    public void testDefaultIndexingPolicyAnnotation() {
        final IndexingPolicy policy = personInfo.getIndexingPolicy();
        final Container containerAnnotation = NoDBAnnotationPerson.class.getAnnotation(Container.class);
        final CosmosIndexingPolicy policyAnnotation =
                NoDBAnnotationPerson.class.getAnnotation(CosmosIndexingPolicy.class);

        Assertions.isNull(containerAnnotation, "NoDBAnnotationPerson class should not have Container annotation");
        Assertions.isNull(policyAnnotation, "NoDBAnnotationPerson class should not have CosmosIndexingPolicy annotation");
        Assertions.notNull(policy, "NoDBAnnotationPerson class collection policy should not be null");

        // ContainerName, RequestUnit, Automatic and IndexingMode
        Assertions.isTrue(personInfo.getContainerName().equals(NoDBAnnotationPerson.class.getSimpleName()),
                "should be default collection name");
        Assertions.isTrue(policy.isAutomatic() == TestConstants.DEFAULT_INDEXING_POLICY_AUTOMATIC,
                "should be default indexing policy automatic");
        Assertions.isTrue(policy.getIndexingMode() == TestConstants.DEFAULT_INDEXING_POLICY_MODE,
                "should be default indexing policy mode");

        // IncludedPaths and ExcludedPaths
        // We do not use testIndexingPolicyPathsEquals generic here, for unit test do not create cosmosdb instance,
        // and the paths of policy will never be set from azure service.
        Assertions.isTrue(policy.getIncludedPaths().isEmpty(), "default includedpaths size must be 0");
        Assertions.isTrue(policy.getExcludedPaths().isEmpty(), "default excludedpaths size must be 0");
    }

    @Test
    public void testIndexingPolicyAnnotation() {
        final IndexingPolicy policy = roleInfo.getIndexingPolicy();
        final Container containerAnnotation = Role.class.getAnnotation(Container.class);
        final CosmosIndexingPolicy policyAnnotation = Role.class.getAnnotation(CosmosIndexingPolicy.class);

        // ContainerName, RequestUnit, Automatic and IndexingMode
        Assertions.notNull(containerAnnotation, "Role class should have Container annotation");
        Assertions.notNull(policyAnnotation, "Role class should have CosmosIndexingPolicy annotation");
        Assertions.notNull(policy, "Role class collection policy should not be null");

        Assertions.isTrue(roleInfo.getContainerName().equals(TestConstants.ROLE_COLLECTION_NAME),
                "should be Role(class) collection name");
        Assertions.isTrue(policy.getIndexingMode() == TestConstants.INDEXING_POLICY_MODE,
                "should be Role(class) indexing policy mode");
    }

    @Test
    public void testAutoCreateCollectionAnnotation() {
        final boolean autoCreateCollectionRoleInfo = roleInfo.isAutoCreateContainer();
        final boolean autoCreateCollectionPersonInfo = personInfo.isAutoCreateContainer();

        Assertions.isTrue(!autoCreateCollectionRoleInfo, "autoCreateContainer in role should be false");
        Assertions.isTrue(autoCreateCollectionPersonInfo, "autoCreateContainer in person should be true");
    }

    @Test
    public void testDefaultContainerAnnotationTimeToLive() {
        final Integer timeToLive = personInfo.getTimeToLive();

        Assertions.notNull(timeToLive, "timeToLive should not be null");
        Assertions.isTrue(timeToLive == TestConstants.DEFAULT_TIME_TO_LIVE, "should be default time to live");
    }

    @Test
    public void testContainerAnnotationTimeToLive() {
        final CosmosEntityInformation<TimeToLiveSample, String> info =
                new CosmosEntityInformation<>(TimeToLiveSample.class);
        final Integer timeToLive = info.getTimeToLive();

        Assertions.notNull(timeToLive, "timeToLive should not be null");
        Assertions.isTrue(timeToLive == TestConstants.TIME_TO_LIVE, "should be the same time to live");
    }
}

