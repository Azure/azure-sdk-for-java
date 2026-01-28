// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer;

import com.azure.analytics.planetarycomputer.models.*;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.util.Arrays;
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

        GetPreviewOptions options = new GetPreviewOptions().setWidth(512)
            .setHeight(512)
            .setAssets(Arrays.asList("image"))
            .setAssetBandIndices("image|1,2,3");

        BinaryData imageData = dataClient.getPreviewWithFormat(collectionId, itemId, "jpg", options, "image/jpeg");

        byte[] imageBytes = imageData.toBytes();
        byte[] jpegMagic = new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF };
        assertTrue(imageBytes.length > 100);
        for (int i = 0; i < jpegMagic.length; i++)
            assertEquals(jpegMagic[i], imageBytes[i]);
    }

    @Test
    @Tag("TileJson")
    public void test06_17_GetTileJson() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId();

        GetTileJsonOptions options = new GetTileJsonOptions().setAssets(Arrays.asList("image"))
            .setAssetBandIndices("image|1,2,3")
            .setTileScale(1)
            .setMinZoom(9)
            .setTileFormat(TilerImageFormat.PNG);

        TileJsonMetadata tileJson = dataClient.getTileJson(collectionId, itemId, "WebMercatorQuad", options);

        assertNotNull(tileJson);
        assertNotNull(tileJson.getTileJson());
        assertNotNull(tileJson.getTiles());
        assertTrue(tileJson.getTiles().size() > 0);
        System.out.println("TileJSON version: " + tileJson.getTileJson());
    }

    @Test
    @Tag("Tile")
    public void test06_18_GetTile() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId();

        GetTileOptions options
            = new GetTileOptions().setAssets(Arrays.asList("image")).setAssetBandIndices("image|1,2,3");

        BinaryData imageData = dataClient.getTile(collectionId, itemId, "WebMercatorQuad", 13, 2174, 3282, 1, "png",
            options, "image/png");

        byte[] imageBytes = imageData.toBytes();
        byte[] pngMagic = new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
        assertTrue(imageBytes.length > 100);
        for (int i = 0; i < pngMagic.length; i++)
            assertEquals(pngMagic[i], imageBytes[i]);
        System.out.println("Tile image retrieved: " + imageBytes.length + " bytes");
    }

    @Test
    @Tag("Assets")
    public void test06_19_ListAvailableAssets() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId();

        List<String> assets = dataClient.listAvailableAssets(collectionId, itemId);

        assertNotNull(assets);
        assertTrue(assets.size() > 0);
        System.out.println("Number of available assets: " + assets.size());
        System.out
            .println("Available assets: " + String.join(", ", assets.stream().limit(10).collect(Collectors.toList())));
    }
}
