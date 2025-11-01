// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer;

import com.azure.analytics.planetarycomputer.models.*;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
        for (int i = 0; i < pngMagic.length; i++)
            assertEquals(pngMagic[i], imageBytes[i]);
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
    @Tag("Part")
    public void test06_13_GetPart() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId();

        double minx = -84.3906, miny = 33.6714, maxx = -84.3814, maxy = 33.6806;

        GetPartOptions options
            = new GetPartOptions().setAssets(Arrays.asList("image")).setAssetBandIndices("image|1,2,3");

        BinaryData imageData
            = dataClient.getPart(collectionId, itemId, minx, miny, maxx, maxy, "image.png", options, null);

        byte[] imageBytes = imageData.toBytes();
        byte[] pngMagic = new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
        assertTrue(imageBytes.length > 100);
        for (int i = 0; i < pngMagic.length; i++)
            assertEquals(pngMagic[i], imageBytes[i]);
    }

    @Test
    @Tag("Part")
    public void test06_14_GetPartWithDimensions() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId();

        double minx = -84.3906, miny = 33.6714, maxx = -84.3814, maxy = 33.6806;

        GetPartOptions options
            = new GetPartOptions().setAssets(Arrays.asList("image")).setAssetBandIndices("image|1,2,3");

        BinaryData imageData = dataClient.getPartWithDimensions(collectionId, itemId, minx, miny, maxx, maxy, 512, 512,
            "image.png", options, null);

        byte[] imageBytes = imageData.toBytes();
        byte[] pngMagic = new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
        assertTrue(imageBytes.length > 100);
        for (int i = 0; i < pngMagic.length; i++)
            assertEquals(pngMagic[i], imageBytes[i]);
    }

    @Test
    @Tag("Point")
    public void test06_15_GetPoint() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId();

        double longitude = -84.3860;
        double latitude = 33.6760;

        TilerCoreModelsResponsesPoint pointData = dataClient.getPoint(collectionId, itemId, longitude, latitude,
            Arrays.asList("image"), null, null, null, null, null, null, null);

        assertNotNull(pointData);
        assertNotNull(pointData.getBandNames());
        System.out.println("Point data retrieved successfully");
    }
}
