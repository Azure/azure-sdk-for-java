// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer;

import com.azure.analytics.planetarycomputer.models.*;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for STAC Item Tiler operations (Group 06b: Tests 06-10).
 */
@Tag("ItemTiler")
public class TestPlanetaryComputer06bStacItemTilerTests extends PlanetaryComputerTestBase {

    @Test
    @Tag("Info")
    public void test06_06_GetInfoGeoJson() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId();

        System.out.println("Input - collection_id: " + collectionId);
        System.out.println("Input - item_id: " + itemId);

        TilerInfoGeoJsonFeature data = dataClient.getInfoGeoJson(collectionId, itemId, Arrays.asList("image"));

        assertNotNull(data, "Response data should not be null");
        System.out.println("Info GeoJSON retrieved successfully");
    }

    @Test
    @Tag("Statistics")
    public void test06_07_ListStatistics() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId();

        System.out.println("Input - collection_id: " + collectionId);
        System.out.println("Input - item_id: " + itemId);

        GetStatisticsOptions options = new GetStatisticsOptions().setAssets(Arrays.asList("image"));
        TilerStacItemStatistics statistics = dataClient.listStatistics(collectionId, itemId, options);

        assertNotNull(statistics, "Statistics should not be null");
        System.out.println("Statistics retrieved successfully");
    }

    @Test
    @Tag("WMTS")
    public void test06_08_GetWmtsCapabilities() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId();

        System.out.println("Input - collection_id: " + collectionId);
        System.out.println("Input - item_id: " + itemId);

        GetWmtsCapabilitiesOptions options = new GetWmtsCapabilitiesOptions().setTileFormat(TilerImageFormat.PNG)
            .setTileScale(1)
            .setMinZoom(7)
            .setMaxZoom(14)
            .setAssets(Arrays.asList("image"))
            .setExpression("image|1,2,3");
        byte[] xmlBytes = dataClient.getWmtsCapabilities(collectionId, itemId, "WebMercatorQuad", options);

        String xmlString = new String(xmlBytes, StandardCharsets.UTF_8);

        System.out.println("XML size: " + xmlBytes.length + " bytes");
        System.out.println("XML first 200 chars: " + xmlString.substring(0, Math.min(200, xmlString.length())));

        assertTrue(xmlBytes.length > 0, "XML bytes should not be empty");
        assertTrue(xmlString.contains("Capabilities"), "Response should contain Capabilities element");
        assertTrue(xmlString.toLowerCase().contains("wmts"), "Response should reference WMTS");
        assertTrue(xmlString.contains("TileMatrix"), "Response should contain TileMatrix information");

        System.out.println("WMTS capabilities XML validated successfully");
    }

    @Test
    @Disabled("Missing session recording - needs to be recorded")
    @Tag("Statistics")
    public void test06_09_GetAssetStatistics() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId();

        System.out.println("Input - collection_id: " + collectionId);
        System.out.println("Input - item_id: " + itemId);

        GetAssetStatisticsOptions options = new GetAssetStatisticsOptions().setAssets(Arrays.asList("image"));
        Map<String, Map<String, BandStatistics>> statistics
            = dataClient.getAssetStatistics(collectionId, itemId, options);

        assertNotNull(statistics, "Statistics should not be null");
        assertTrue(statistics.containsKey("image"), "Should contain statistics for 'image' asset");

        System.out.println("Number of assets with statistics: " + statistics.size());
        System.out.println("Asset statistics retrieved successfully");
    }

    @Test
    @Disabled("Recording has body mismatch - needs to be re-recorded")
    @Tag("Crop")
    public void test06_10_CropGeoJson() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId();

        List<List<List<Double>>> coordinates
            = Arrays.asList(Arrays.asList(Arrays.asList(-84.3906, 33.6714), Arrays.asList(-84.3814, 33.6714),
                Arrays.asList(-84.3814, 33.6806), Arrays.asList(-84.3906, 33.6806), Arrays.asList(-84.3906, 33.6714)));
        Polygon geometry = new Polygon().setCoordinates(coordinates);
        Feature feature = new Feature(geometry, FeatureType.FEATURE);

        System.out.println("Input - collection_id: " + collectionId);
        System.out.println("Input - item_id: " + itemId);

        CropGeoJsonOptions options
            = new CropGeoJsonOptions().setAssets(Arrays.asList("image")).setAssetBandIndices("image|1,2,3");

        BinaryData imageData = dataClient.cropGeoJson(collectionId, itemId, "crop.png", options, feature, "image/png");

        byte[] imageBytes = imageData.toBytes();
        System.out.println("Image size: " + imageBytes.length + " bytes");

        byte[] pngMagic = new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
        assertTrue(imageBytes.length > 100, "Image should be substantial");
        for (int i = 0; i < pngMagic.length; i++) {
            assertEquals(pngMagic[i], imageBytes[i]);
        }

        System.out.println("PNG magic bytes verified successfully");
    }
}
