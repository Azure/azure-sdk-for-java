// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer.async;

import com.azure.analytics.planetarycomputer.PlanetaryComputerTestBase;
import com.azure.analytics.planetarycomputer.models.AssetMetadata;
import com.azure.analytics.planetarycomputer.models.FileDetails;
import com.azure.analytics.planetarycomputer.models.StacAssetData;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Async tests for Collection Asset Lifecycle operations.
 * Combined into a single test to ensure correct ordering (create → replace → delete).
 * Mirrors sync TestPlanetaryComputer08bCollectionLifecycleTests.
 */
@Tag("CollectionLifecycle")
@Tag("Async")
public class TestPlanetaryComputer08AsyncCollectionLifecycleTests extends PlanetaryComputerTestBase {

    @Test
    @Tag("CollectionAsset")
    public void test08_AsyncCollectionAssetLifecycle() {
        String collectionId = testEnvironment.getCollectionId();
        String assetId = "test-asset-async";

        // Step 1: Clean up if exists
        stacAsyncClient.deleteCollectionAsset(collectionId, assetId)
            .onErrorResume(ex -> reactor.core.publisher.Mono.empty())
            .block();

        // Step 2: Create asset
        AssetMetadata createMetadata
            = new AssetMetadata(assetId, "text/plain", Arrays.asList("metadata"), "Test Asset Async", null);
        byte[] createContent = "Test asset content async".getBytes(StandardCharsets.UTF_8);
        FileDetails createFile
            = new FileDetails(BinaryData.fromBytes(createContent)).setFilename("test-asset-async.txt")
                .setContentType("text/plain");
        StacAssetData createData = new StacAssetData(createMetadata, createFile);

        StepVerifier.create(stacAsyncClient.createCollectionAsset(collectionId, createData)).assertNext(response -> {
            assertNotNull(response, "Create response should not be null");
            System.out.println("Async: Asset created");
        }).verifyComplete();

        // Step 3: Replace asset
        AssetMetadata replaceMetadata
            = new AssetMetadata(assetId, "text/plain", Arrays.asList("metadata"), "Test Asset Async - Updated", null);
        byte[] replaceContent = "Test asset content async - updated".getBytes(StandardCharsets.UTF_8);
        FileDetails replaceFile
            = new FileDetails(BinaryData.fromBytes(replaceContent)).setFilename("test-asset-async-updated.txt")
                .setContentType("text/plain");
        StacAssetData replaceData = new StacAssetData(replaceMetadata, replaceFile);

        StepVerifier.create(stacAsyncClient.replaceCollectionAsset(collectionId, assetId, replaceData))
            .assertNext(response -> {
                assertNotNull(response, "Replace response should not be null");
                System.out.println("Async: Asset replaced");
            })
            .verifyComplete();

        // Step 4: Delete asset
        StepVerifier.create(stacAsyncClient.deleteCollectionAsset(collectionId, assetId)).assertNext(response -> {
            assertNotNull(response, "Delete response should not be null");
            System.out.println("Async: Asset deleted");
        }).verifyComplete();

        System.out.println("Async: Collection asset lifecycle completed: create → replace → delete");
    }
}
