// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer.async;

import com.azure.analytics.planetarycomputer.PlanetaryComputerTestBase;
import com.azure.core.http.rest.RequestOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Async tests for STAC Collection operations (Groups 01a-01b).
 * Mirrors sync tests in TestPlanetaryComputer01aStacCollectionTests and 01b.
 */
@Tag("STAC")
@Tag("Async")
public class TestPlanetaryComputer01AsyncStacCollectionTests extends PlanetaryComputerTestBase {

    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    @Test
    public void test01_01_ListCollectionsAsync() {
        String collectionId = testEnvironment.getCollectionId();

        StepVerifier.create(stacAsyncClient.getCollections()).assertNext(collections -> {
            assertNotNull(collections);
            assertNotNull(collections.getCollections());
            assertTrue(collections.getCollections().size() > 0);
        }).verifyComplete();
    }

    @Test
    public void test01_02_GetConformanceClassAsync() {
        StepVerifier.create(stacAsyncClient.getConformanceClasses()).assertNext(conformance -> {
            assertNotNull(conformance);
            assertNotNull(conformance.getConformsTo());
            assertTrue(conformance.getConformsTo().size() > 0);
        }).verifyComplete();
    }

    @Test
    public void test01_03_GetCollectionAsync() {
        String collectionId = testEnvironment.getCollectionId();

        StepVerifier.create(stacAsyncClient.getCollection(collectionId)).assertNext(collection -> {
            assertNotNull(collection);
            assertEquals(collectionId, collection.getId());
            assertNotNull(collection.getExtent());
            assertNotNull(collection.getLicense());
        }).verifyComplete();
    }

    @Test
    public void test01_04_GetPartitionTypeAsync() {
        String collectionId = testEnvironment.getCollectionId();

        StepVerifier.create(stacAsyncClient.getPartitionType(collectionId)).assertNext(partitionType -> {
            assertNotNull(partitionType);
            assertNotNull(partitionType.getScheme());
        }).verifyComplete();
    }

    @Test
    public void test01_05_ListRenderOptionsAsync() {
        String collectionId = testEnvironment.getCollectionId();

        StepVerifier.create(stacAsyncClient.getRenderOptions(collectionId)).assertNext(renderOptions -> {
            assertNotNull(renderOptions);
        }).verifyComplete();
    }

    @Test
    public void test01_06_GetTileSettingsAsync() {
        String collectionId = testEnvironment.getCollectionId();

        StepVerifier.create(stacAsyncClient.getTileSettings(collectionId)).assertNext(tileSettings -> {
            assertNotNull(tileSettings);
        }).verifyComplete();
    }

    @Test
    public void test01_07_ListMosaicsAsync() {
        String collectionId = testEnvironment.getCollectionId();

        StepVerifier.create(stacAsyncClient.getMosaics(collectionId)).assertNext(mosaics -> {
            assertNotNull(mosaics);
        }).verifyComplete();
    }

    @Test
    public void test01_08_GetCollectionQueryablesAsync() {
        String collectionId = testEnvironment.getCollectionId();

        StepVerifier.create(stacAsyncClient.getCollectionQueryablesWithResponse(collectionId, new RequestOptions()))
            .assertNext(response -> {
                assertNotNull(response);
                assertTrue(response.getStatusCode() >= 200 && response.getStatusCode() < 300);
            })
            .verifyComplete();
    }

    @Test
    public void test01_09_GetCollectionConfigurationAsync() {
        String collectionId = testEnvironment.getCollectionId();

        StepVerifier.create(stacAsyncClient.getCollectionConfiguration(collectionId)).assertNext(config -> {
            assertNotNull(config);
        }).verifyComplete();
    }
}
