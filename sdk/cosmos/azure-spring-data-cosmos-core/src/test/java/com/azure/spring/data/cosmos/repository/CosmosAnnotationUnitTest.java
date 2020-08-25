// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository;

import com.azure.cosmos.models.IndexingPolicy;
import com.azure.spring.data.cosmos.common.TestConstants;
import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.CosmosIndexingPolicy;
import com.azure.spring.data.cosmos.domain.NoDBAnnotationPerson;
import com.azure.spring.data.cosmos.domain.Role;
import com.azure.spring.data.cosmos.domain.TimeToLiveSample;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.Assert;


public class CosmosAnnotationUnitTest {
    private CosmosEntityInformation<NoDBAnnotationPerson, String> personInfo;
    private CosmosEntityInformation<Role, String> roleInfo;

    @Before
    public void setUp() {
        personInfo = new CosmosEntityInformation<>(NoDBAnnotationPerson.class);
        roleInfo = new CosmosEntityInformation<>(Role.class);
    }

    @Test
    public void testDefaultIndexingPolicyAnnotation() {
        final IndexingPolicy policy = personInfo.getIndexingPolicy();
        final Container containerAnnotation = NoDBAnnotationPerson.class.getAnnotation(Container.class);
        final CosmosIndexingPolicy policyAnnotation =
                NoDBAnnotationPerson.class.getAnnotation(CosmosIndexingPolicy.class);

        Assert.isNull(containerAnnotation, "NoDBAnnotationPerson class should not have Container annotation");
        Assert.isNull(policyAnnotation, "NoDBAnnotationPerson class should not have DocumentIndexingPolicy annotation");
        Assert.notNull(policy, "NoDBAnnotationPerson class collection policy should not be null");

        // ContainerName, RequestUnit, Automatic and IndexingMode
        Assert.isTrue(personInfo.getContainerName().equals(NoDBAnnotationPerson.class.getSimpleName()),
                "should be default collection name");
        Assert.isTrue(policy.isAutomatic() == TestConstants.DEFAULT_INDEXING_POLICY_AUTOMATIC,
                "should be default indexing policy automatic");
        Assert.isTrue(policy.getIndexingMode() == TestConstants.DEFAULT_INDEXING_POLICY_MODE,
                "should be default indexing policy mode");

        // IncludedPaths and ExcludedPaths
        // We do not use testIndexingPolicyPathsEquals generic here, for unit test do not create cosmosdb instance,
        // and the paths of policy will never be set from azure service.
        Assert.isTrue(policy.getIncludedPaths().isEmpty(), "default includedpaths size must be 0");
        Assert.isTrue(policy.getExcludedPaths().isEmpty(), "default excludedpaths size must be 0");
    }

    @Test
    public void testIndexingPolicyAnnotation() {
        final IndexingPolicy policy = roleInfo.getIndexingPolicy();
        final Container containerAnnotation = Role.class.getAnnotation(Container.class);
        final CosmosIndexingPolicy policyAnnotation = Role.class.getAnnotation(CosmosIndexingPolicy.class);

        // ContainerName, RequestUnit, Automatic and IndexingMode
        Assert.notNull(containerAnnotation, "NoDBAnnotationPerson class should have Container annotation");
        Assert.notNull(policyAnnotation, "NoDBAnnotationPerson class should have DocumentIndexingPolicy annotation");
        Assert.notNull(policy, "NoDBAnnotationPerson class collection policy should not be null");

        Assert.isTrue(roleInfo.getContainerName().equals(TestConstants.ROLE_COLLECTION_NAME),
                "should be Role(class) collection name");
        Assert.isTrue(policy.isAutomatic() == TestConstants.INDEXING_POLICY_AUTOMATIC,
                "should be Role(class) indexing policy automatic");
        Assert.isTrue(policy.getIndexingMode() == TestConstants.INDEXING_POLICY_MODE,
                "should be Role(class) indexing policy mode");
    }

    @Test
    public void testAutoCreateCollectionAnnotation() {
        final boolean autoCreateCollectionRoleInfo = roleInfo.isAutoCreateContainer();
        final boolean autoCreateCollectionPersonInfo = personInfo.isAutoCreateContainer();

        Assert.isTrue(!autoCreateCollectionRoleInfo, "autoCreateContainer in role should be false");
        Assert.isTrue(autoCreateCollectionPersonInfo, "autoCreateContainer in person should be true");
    }

    @Test
    public void testDefaultContainerAnnotationTimeToLive() {
        final Integer timeToLive = personInfo.getTimeToLive();

        Assert.notNull(timeToLive, "timeToLive should not be null");
        Assert.isTrue(timeToLive == TestConstants.DEFAULT_TIME_TO_LIVE, "should be default time to live");
    }

    @Test
    public void testContainerAnnotationTimeToLive() {
        final CosmosEntityInformation<TimeToLiveSample, String> info =
                new CosmosEntityInformation<>(TimeToLiveSample.class);
        final Integer timeToLive = info.getTimeToLive();

        Assert.notNull(timeToLive, "timeToLive should not be null");
        Assert.isTrue(timeToLive == TestConstants.TIME_TO_LIVE, "should be the same time to live");
    }
}

