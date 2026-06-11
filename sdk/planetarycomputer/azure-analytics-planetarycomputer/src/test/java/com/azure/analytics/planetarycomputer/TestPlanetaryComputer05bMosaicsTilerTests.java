// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer;

import com.azure.analytics.planetarycomputer.models.StacItemPointAsset;
import com.azure.analytics.planetarycomputer.models.TilerAssetGeoJson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Mosaics Tiler operations (Group 05b: Tests 06-07).
 */
@Tag("Mosaics")
public class TestPlanetaryComputer05bMosaicsTilerTests extends TestPlanetaryComputer05aMosaicsTilerTests {

    @Test
    @Tag("Assets")
    public void test05_06_GetMosaicsAssetsForPoint() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();

        double longitude = -84.43202751899601;
        double latitude = 33.639647639722273;

        System.out.println(String.format("Input - point: longitude=%f, latitude=%f", longitude, latitude));

        String searchId = registerSearch(dataClient, collectionId);
        System.out.println("Using search ID: " + searchId);

        List<StacItemPointAsset> assets = dataClient.getSearchPointWithAssets(searchId, longitude, latitude);

        assertNotNull(assets, "Assets list should not be null");
        System.out.println("Number of assets: " + assets.size());

        if (assets.size() > 0) {
            StacItemPointAsset firstAsset = assets.get(0);
            assertNotNull(firstAsset, "First asset should not be null");
            assertNotNull(firstAsset.getId(), "Asset ID should not be null");
            assertFalse(firstAsset.getId().isEmpty(), "Asset ID should not be empty");
            System.out.println("First asset ID: " + firstAsset.getId());
        }
    }

    @Test
    @Tag("Assets")
    public void test05_07_GetMosaicsAssetsForTile() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();

        System.out.println("Input - tile coordinates: z=13, x=2174, y=3282");

        String searchId = registerSearch(dataClient, collectionId);
        System.out.println("Using search ID: " + searchId);

        List<TilerAssetGeoJson> assets
            = dataClient.getSearchAssetsForTile(searchId, "WebMercatorQuad", collectionId, 13.0, 2174.0, 3282.0);

        assertNotNull(assets, "Assets list should not be null");
        System.out.println("Number of assets: " + assets.size());
    }
}
