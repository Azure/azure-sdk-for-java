// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer;

import com.azure.analytics.planetarycomputer.models.GetPreviewOptions;
import com.azure.analytics.planetarycomputer.models.StacItemBounds;
import com.azure.analytics.planetarycomputer.models.TileMatrix;
import com.azure.analytics.planetarycomputer.models.TileMatrixSet;
import com.azure.analytics.planetarycomputer.models.TilerImageFormat;
import com.azure.analytics.planetarycomputer.models.TilerInfo;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for STAC Item Tiler operations (Group 06a: Tests 01-05).
 * Ported from TestPlanetaryComputer06aStacItemTilerTests.cs
 */
@Tag("ItemTiler")
public class TestPlanetaryComputer06aStacItemTilerTests extends PlanetaryComputerTestBase {

    @Test
    @Tag("TileMatrices")
    @Tag("TileMatrixDefinitions")
    public void test06_01_GetTileMatrixDefinitions() {
        DataClient dataClient = getDataClient();
        String tileMatrixSetId = "WebMercatorQuad";

        System.out.println("Input - tile_matrix_set_id: " + tileMatrixSetId);

        TileMatrixSet tileMatrixSet = dataClient.getTileMatrixDefinitions(tileMatrixSetId);

        assertNotNull(tileMatrixSet, "TileMatrixSet should not be null");
        assertNotNull(tileMatrixSet.getId(), "TileMatrixSet ID should not be null");
        assertNotNull(tileMatrixSet.getTileMatrices(), "TileMatrices should not be null");

        System.out.println("TileMatrixSet ID: " + tileMatrixSet.getId());

        assertTrue(tileMatrixSet.getTileMatrices().size() > 0, "Should have at least one tile matrix");
        System.out.println("Number of tile matrices: " + tileMatrixSet.getTileMatrices().size());

        TileMatrix firstMatrix = tileMatrixSet.getTileMatrices().get(0);
        assertNotNull(firstMatrix, "First tile matrix should not be null");
        validateNotNullOrEmpty(firstMatrix.getId(), "Tile matrix id");
        assertNotNull(firstMatrix.getScaleDenominator(), "Tile matrix should have 'scaleDenominator'");

        int tileWidth = firstMatrix.getTileWidth();
        int tileHeight = firstMatrix.getTileHeight();

        assertEquals(256, tileWidth, "Standard tile width should be 256");
        assertEquals(256, tileHeight, "Standard tile height should be 256");

        System.out.println("First matrix ID: " + firstMatrix.getId());
        System.out.println("Tile dimensions: " + tileWidth + "x" + tileHeight);
        System.out.println("Scale denominator: " + firstMatrix.getScaleDenominator());
    }

    @Test
    @Tag("TileMatrices")
    public void test06_02_ListTileMatrices() {
        DataClient dataClient = getDataClient();

        System.out.println("Testing getTileMatrices to get all available tile matrix set IDs");

        List<String> tileMatrixIds = dataClient.listTileMatrices();

        assertNotNull(tileMatrixIds, "Tile matrix IDs should not be null");
        System.out.println("Number of tile matrices: " + tileMatrixIds.size());

        assertTrue(tileMatrixIds.contains("WebMercatorQuad"), "Should include WebMercatorQuad");
        assertTrue(tileMatrixIds.contains("WorldCRS84Quad"), "Should include WorldCRS84Quad");

        System.out.println("Found tile matrices: " + String.join(", ", tileMatrixIds));
    }

    @Test
    @Disabled("Missing session recording - needs to be recorded")
    @Tag("Assets")
    public void test06_03_GetItemAssetDetails() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId();

        System.out.println("Input - collection_id: " + collectionId);
        System.out.println("Input - item_id: " + itemId);

        Map<String, TilerInfo> assets = dataClient.getItemAssetDetails(collectionId, itemId);

        assertNotNull(assets, "Assets dictionary should not be null");
        assertTrue(assets.size() > 0, "Should have at least one asset");

        System.out.println("Number of assets: " + assets.size());
        System.out.println(
            "Available assets: " + String.join(", ", assets.keySet().stream().limit(10).collect(Collectors.toList())));

        if (assets.size() > 0) {
            Map.Entry<String, TilerInfo> firstEntry = assets.entrySet().iterator().next();
            System.out.println("First asset name: " + firstEntry.getKey());
            System.out.println("First asset info: " + firstEntry.getValue());
        }
    }

    @Test
    @Tag("Bounds")
    public void test06_04_GetBounds() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId();

        System.out.println("Input - collection_id: " + collectionId);
        System.out.println("Input - item_id: " + itemId);

        StacItemBounds boundsResult = dataClient.getBounds(collectionId, itemId);

        assertNotNull(boundsResult, "Bounds result should not be null");
        assertNotNull(boundsResult.getBounds(), "Bounds array should not be null");
        assertEquals(4, boundsResult.getBounds().size(), "Bounds should have 4 coordinates [minx, miny, maxx, maxy]");

        List<Double> bounds = boundsResult.getBounds();
        double minx = bounds.get(0);
        double miny = bounds.get(1);
        double maxx = bounds.get(2);
        double maxy = bounds.get(3);

        System.out.println(String.format("Bounds: [%f, %f, %f, %f]", minx, miny, maxx, maxy));

        assertTrue(minx < maxx, "minx should be less than maxx");
        assertTrue(miny < maxy, "miny should be less than maxy");
    }

    @Test
    @Tag("Preview")
    public void test06_05_GetPreview() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId();

        System.out.println("Input - collection_id: " + collectionId);
        System.out.println("Input - item_id: " + itemId);
        System.out.println("Input - dimensions: 512x512");

        GetPreviewOptions options = new GetPreviewOptions().setWidth(512)
            .setHeight(512)
            .setAssets(Arrays.asList("image"))
            .setExpression("image|1,2,3");
        BinaryData imageData = dataClient.getPreview(collectionId, itemId, options, "image/png");

        byte[] imageBytes = imageData.toBytes();

        System.out.println("Image size: " + imageBytes.length + " bytes");
        System.out.println(
            "First 16 bytes (hex): " + bytesToHex(Arrays.copyOfRange(imageBytes, 0, Math.min(16, imageBytes.length))));

        byte[] pngMagic = new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
        assertTrue(imageBytes.length > 0, "Image bytes should not be empty");
        assertTrue(imageBytes.length > 100,
            String.format("Image should be substantial, got only %d bytes", imageBytes.length));

        for (int i = 0; i < pngMagic.length; i++) {
            assertEquals(pngMagic[i], imageBytes[i], String.format("PNG magic byte %d mismatch", i));
        }

        System.out.println("PNG magic bytes verified successfully");
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
