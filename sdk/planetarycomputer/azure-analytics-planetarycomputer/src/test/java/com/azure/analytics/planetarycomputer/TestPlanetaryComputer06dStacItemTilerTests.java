// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer;

import com.azure.analytics.planetarycomputer.models.*;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for STAC Item Tiler operations (Group 06d: Tests 16-19).
 */
@Tag("ItemTiler")
public class TestPlanetaryComputer06dStacItemTilerTests extends PlanetaryComputerTestBase {

    @Test
    @Tag("Preview")
    public void test06_16_GetPreviewWithFormat() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId();

        // Use protocol method to pass assets (required by server)
        com.azure.core.http.rest.RequestOptions requestOptions = new com.azure.core.http.rest.RequestOptions();
        requestOptions.addQueryParam("assets", "image", false);
        requestOptions.addQueryParam("format", "jpg", false);
        BinaryData imageData
            = dataClient.getItemPreviewWithFormatWithResponse(collectionId, itemId, "jpg", requestOptions).getValue();

        byte[] imageBytes = imageData.toBytes();
        byte[] jpegMagic = new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF };
        assertTrue(imageBytes.length > 100);
        for (int i = 0; i < jpegMagic.length; i++) {
            assertEquals(jpegMagic[i], imageBytes[i]);
        }
    }

    @Test
    @Tag("TileJson")
    public void test06_17_GetTileJson() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId();

        // Use protocol method to pass required assets parameter
        com.azure.core.http.rest.RequestOptions requestOptions = new com.azure.core.http.rest.RequestOptions();
        requestOptions.addQueryParam("assets", "image", false);
        requestOptions.addQueryParam("asset_bidx", "image|1,2,3", false);
        com.azure.core.http.rest.Response<com.azure.core.util.BinaryData> response
            = dataClient.getItemTileJsonWithResponse(collectionId, itemId, requestOptions);

        assertNotNull(response);
        assertTrue(response.getStatusCode() >= 200 && response.getStatusCode() < 300);
        TileJsonMetadata tileJson = response.getValue().toObject(TileJsonMetadata.class);

        assertNotNull(tileJson);
        System.out.println("TileJSON retrieved successfully");
    }

    @Test
    @Tag("Tile")
    public void test06_18_GetTile() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId();

        // Tile coordinates within NAIP item bounds (z=14, Atlanta area)
        com.azure.core.http.rest.RequestOptions requestOptions = new com.azure.core.http.rest.RequestOptions();
        requestOptions.addQueryParam("assets", "image", false);
        requestOptions.addQueryParam("asset_bidx", "image|1,2,3", false);
        BinaryData imageData = dataClient
            .getTileWithTmsByFormatWithResponse(collectionId, itemId, "WebMercatorQuad", 14, 4349, 6564, "png",
                requestOptions)
            .getValue();

        byte[] imageBytes = imageData.toBytes();
        byte[] pngMagic = new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
        assertTrue(imageBytes.length > 100);
        for (int i = 0; i < pngMagic.length; i++) {
            assertEquals(pngMagic[i], imageBytes[i]);
        }
        System.out.println("Tile image retrieved: " + imageBytes.length + " bytes");
    }

    @Test
    @Tag("Assets")
    public void test06_19_ListAvailableAssets() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId();

        List<String> assets = dataClient.getItemAvailableAssets(collectionId, itemId);

        assertNotNull(assets);
        assertTrue(assets.size() > 0);
        System.out.println("Number of available assets: " + assets.size());
        System.out
            .println("Available assets: " + String.join(", ", assets.stream().limit(10).collect(Collectors.toList())));
    }
}
