// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository;

import com.azure.data.cosmos.IndexingPolicy;
import com.azure.spring.data.cosmos.core.mapping.Document;
import com.azure.spring.data.cosmos.core.mapping.DocumentIndexingPolicy;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import com.azure.spring.data.cosmos.common.TestConstants;
import com.azure.spring.data.cosmos.common.TestUtils;
import com.azure.spring.data.cosmos.domain.NoDBAnnotationPerson;
import com.azure.spring.data.cosmos.domain.Role;
import com.azure.spring.data.cosmos.domain.TimeToLiveSample;
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
        final Document documentAnnotation = NoDBAnnotationPerson.class.getAnnotation(Document.class);
        final DocumentIndexingPolicy policyAnnotation =
                NoDBAnnotationPerson.class.getAnnotation(DocumentIndexingPolicy.class);

        Assert.isNull(documentAnnotation, "NoDBAnnotationPerson class should not have Document annotation");
        Assert.isNull(policyAnnotation, "NoDBAnnotationPerson class should not have DocumentIndexingPolicy annotation");
        Assert.notNull(policy, "NoDBAnnotationPerson class collection policy should not be null");

        // ContainerName, RequestUnit, Automatic and IndexingMode
        Assert.isTrue(personInfo.getContainerName().equals(NoDBAnnotationPerson.class.getSimpleName()),
                "should be default collection name");
        Assert.isTrue(policy.automatic() == TestConstants.DEFAULT_INDEXINGPOLICY_AUTOMATIC,
                "should be default indexing policy automatic");
        Assert.isTrue(policy.indexingMode() == TestConstants.DEFAULT_INDEXINGPOLICY_MODE,
                "should be default indexing policy mode");

        // IncludedPaths and ExcludedPaths
        // We do not use testIndexingPolicyPathsEquals generic here, for unit test do not create cosmosdb instance,
        // and the paths of policy will never be set from azure service.
        // testIndexingPolicyPathsEquals(policy.getIncludedPaths(), TestConstants.DEFAULT_INCLUDEDPATHS);
        // testIndexingPolicyPathsEquals(policy.getExcludedPaths(), TestConstants.DEFAULT_EXCLUDEDPATHS);
        Assert.isTrue(policy.includedPaths().isEmpty(), "default includedpaths size must be 0");
        Assert.isTrue(policy.excludedPaths().isEmpty(), "default excludedpaths size must be 0");
    }

    @Test
    public void testIndexingPolicyAnnotation() {
        final IndexingPolicy policy = roleInfo.getIndexingPolicy();
        final Document documentAnnotation = Role.class.getAnnotation(Document.class);
        final DocumentIndexingPolicy policyAnnotation = Role.class.getAnnotation(DocumentIndexingPolicy.class);

        // ContainerName, RequestUnit, Automatic and IndexingMode
        Assert.notNull(documentAnnotation, "NoDBAnnotationPerson class should have Document annotation");
        Assert.notNull(policyAnnotation, "NoDBAnnotationPerson class should have DocumentIndexingPolicy annotation");
        Assert.notNull(policy, "NoDBAnnotationPerson class collection policy should not be null");

        Assert.isTrue(roleInfo.getContainerName().equals(TestConstants.ROLE_COLLECTION_NAME),
                "should be Role(class) collection name");
        Assert.isTrue(policy.automatic() == TestConstants.INDEXINGPOLICY_AUTOMATIC,
                "should be Role(class) indexing policy automatic");
        Assert.isTrue(policy.indexingMode() == TestConstants.INDEXINGPOLICY_MODE,
                "should be Role(class) indexing policy mode");

        // IncludedPaths and ExcludedPaths
        TestUtils.testIndexingPolicyPathsEquals(policy.includedPaths(), TestConstants.INCLUDEDPATHS);
        TestUtils.testIndexingPolicyPathsEquals(policy.excludedPaths(), TestConstants.EXCLUDEDPATHS);
    }

    @Test
    public void testAutoCreateCollectionAnnotation() {
        final boolean autoCreateCollectionRoleInfo = roleInfo.isAutoCreateContainer();
        final boolean autoCreateCollectionPersonInfo = personInfo.isAutoCreateContainer();

        Assert.isTrue(!autoCreateCollectionRoleInfo, "autoCreateContainer in role should be false");
        Assert.isTrue(autoCreateCollectionPersonInfo, "autoCreateContainer in person should be true");
    }

    @Test
    public void testDefaultDocumentAnnotationTimeToLive() {
        final Integer timeToLive = personInfo.getTimeToLive();

        Assert.notNull(timeToLive, "timeToLive should not be null");
        Assert.isTrue(timeToLive == TestConstants.DEFAULT_TIME_TO_LIVE, "should be default time to live");
    }

    @Test
    public void testDocumentAnnotationTimeToLive() {
        final CosmosEntityInformation<TimeToLiveSample, String> info =
                new CosmosEntityInformation<>(TimeToLiveSample.class);
        final Integer timeToLive = info.getTimeToLive();

        Assert.notNull(timeToLive, "timeToLive should not be null");
        Assert.isTrue(timeToLive == TestConstants.TIME_TO_LIVE, "should be the same time to live");
    }
}

