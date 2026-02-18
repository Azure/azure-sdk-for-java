// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer;

import com.azure.analytics.planetarycomputer.models.*;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for STAC Item Tiler operations (Group 06c: Tests 11-15).
 */
@Tag("ItemTiler")
public class TestPlanetaryComputer06cStacItemTilerTests extends PlanetaryComputerTestBase {

    @Test
    @Disabled("Recording has body mismatch - needs to be re-recorded")
    @Tag("Crop")
    public void test06_11_CropGeoJsonWithDimensions() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId();

        Polygon polygon = new Polygon().setCoordinates(
            Arrays.asList(Arrays.asList(Arrays.asList(-84.3906, 33.6714), Arrays.asList(-84.3814, 33.6714),
                Arrays.asList(-84.3814, 33.6806), Arrays.asList(-84.3906, 33.6806), Arrays.asList(-84.3906, 33.6714))));
        Feature feature = new Feature(polygon, FeatureType.FEATURE);

        CropGeoJsonOptions options
            = new CropGeoJsonOptions().setAssets(Arrays.asList("image")).setAssetBandIndices("image|1,2,3");

        BinaryData imageData
            = dataClient.cropGeoJsonWithDimensions(collectionId, itemId, 512, 512, "crop.png", options, feature, null);

        byte[] imageBytes = imageData.toBytes();
        byte[] pngMagic = new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
        assertTrue(imageBytes.length > 100);
        for (int i = 0; i < pngMagic.length; i++) {
            assertEquals(pngMagic[i], imageBytes[i]);
        }
    }

    @Test
    @Disabled("Recording has body mismatch - needs to be re-recorded")
    @Tag("Statistics")
    public void test06_12_GetGeoJsonStatistics() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId();

        Polygon polygon = new Polygon().setCoordinates(
            Arrays.asList(Arrays.asList(Arrays.asList(-84.3906, 33.6714), Arrays.asList(-84.3814, 33.6714),
                Arrays.asList(-84.3814, 33.6806), Arrays.asList(-84.3906, 33.6806), Arrays.asList(-84.3906, 33.6714))));
        Feature feature = new Feature(polygon, FeatureType.FEATURE);

        GetGeoJsonStatisticsOptions options = new GetGeoJsonStatisticsOptions().setAssets(Arrays.asList("image"));

        StacItemStatisticsGeoJson statistics = dataClient.getGeoJsonStatistics(collectionId, itemId, options, feature);

        assertNotNull(statistics);
        assertNotNull(statistics.getProperties());
        System.out.println("GeoJSON statistics retrieved successfully");
    }

    @Test
    @Disabled("SDK codegen bug: double path parameters serialized incorrectly - server returns validation error")
    @Tag("Part")
    public void test06_13_GetPart() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId();

        // Coordinates matching the recording
        double minx = -77.1, miny = 38.88, maxx = -77.07, maxy = 38.92;

        GetPartOptions options = new GetPartOptions().setAssets(Arrays.asList("image"));

        BinaryData imageData
            = dataClient.getPart(collectionId, itemId, minx, miny, maxx, maxy, "image.png", options, null);

        byte[] imageBytes = imageData.toBytes();
        byte[] pngMagic = new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
        assertTrue(imageBytes.length > 100);
        for (int i = 0; i < pngMagic.length; i++) {
            assertEquals(pngMagic[i], imageBytes[i]);
        }
    }

    @Test
    @Disabled("SDK codegen bug: double path parameters serialized incorrectly - server returns validation error")
    @Tag("Part")
    public void test06_14_GetPartWithDimensions() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId();

        // Coordinates matching the recording
        double minx = -77.1, miny = 38.88, maxx = -77.07, maxy = 38.92;

        GetPartOptions options = new GetPartOptions().setAssets(Arrays.asList("image"));

        BinaryData imageData = dataClient.getPartWithDimensions(collectionId, itemId, minx, miny, maxx, maxy, 512, 512,
            "image.png", options, null);

        byte[] imageBytes = imageData.toBytes();
        byte[] pngMagic = new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
        assertTrue(imageBytes.length > 100);
        for (int i = 0; i < pngMagic.length; i++) {
            assertEquals(pngMagic[i], imageBytes[i]);
        }
    }

    @Test
    @Disabled("SDK codegen bug: double path parameters serialized incorrectly")
    @Tag("Point")
    public void test06_15_GetPoint() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId();

        // Coordinates matching the recording
        double longitude = -77.09;
        double latitude = 38.9;

        TilerCoreModelsResponsesPoint pointData = dataClient.getPoint(collectionId, itemId, longitude, latitude,
            Arrays.asList("image"), null, null, null, null, null, null, null);

        assertNotNull(pointData);
        assertNotNull(pointData.getBandNames());
        System.out.println("Point data retrieved successfully");
    }
}
