// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer;

import com.azure.analytics.planetarycomputer.models.Feature;
import com.azure.analytics.planetarycomputer.models.FeatureType;
import com.azure.analytics.planetarycomputer.models.Polygon;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.util.Arrays;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Mosaics Tiler additional operations (Group 05c: Tests 08-09).
 * Covers: getSearchBboxCrop, cropSearchFeature.
 * JS equivalent: 05_mosaicsTiler.spec.ts (bbox crop and feature crop tests)
 */
@Tag("Mosaics")
public class TestPlanetaryComputer05cMosaicsTilerTests extends TestPlanetaryComputer05aMosaicsTilerTests {

    @Test
    @Tag("BboxCrop")
    public void test05_08_GetSearchBboxCrop() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();

        double minx = -84.39;
        double miny = 33.68;
        double maxx = -84.385;
        double maxy = 33.685;

        System.out.println(String.format("Input - bbox: [%f, %f, %f, %f]", minx, miny, maxx, maxy));

        String searchId = registerSearch(dataClient, collectionId);
        System.out.println("Using search ID: " + searchId);

        // Use protocol method to pass required assets
        com.azure.core.http.rest.RequestOptions requestOptions = new com.azure.core.http.rest.RequestOptions();
        requestOptions.addQueryParam("assets", "image", false);
        requestOptions.addQueryParam("asset_bidx", "image|1,2,3", false);
        BinaryData imageData
            = dataClient.getSearchBboxCropWithResponse(searchId, minx, miny, maxx, maxy, "png", requestOptions)
                .getValue();

        byte[] imageBytes = imageData.toBytes();
        System.out.println("Image size: " + imageBytes.length + " bytes");

        assertTrue(imageBytes.length > 0, "Bbox crop image should not be empty");
        System.out.println("Search bbox crop retrieved successfully");
    }

    @Test
    @Tag("FeatureCrop")
    public void test05_09_CropSearchFeature() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();

        Polygon geometry = new Polygon()
            .setCoordinates(Arrays.asList(Arrays.asList(Arrays.asList(-84.39, 33.68), Arrays.asList(-84.385, 33.68),
                Arrays.asList(-84.385, 33.685), Arrays.asList(-84.39, 33.685), Arrays.asList(-84.39, 33.68))));
        Feature feature = new Feature(geometry, FeatureType.FEATURE).setProperties(new HashMap<>());

        System.out.println("Testing cropSearchFeature with polygon");

        String searchId = registerSearch(dataClient, collectionId);
        System.out.println("Using search ID: " + searchId);

        // Use protocol method to pass required assets
        com.azure.core.http.rest.RequestOptions requestOptions = new com.azure.core.http.rest.RequestOptions();
        requestOptions.addQueryParam("assets", "image", false);
        requestOptions.addQueryParam("asset_bidx", "image|1,2,3", false);
        BinaryData imageData
            = dataClient.cropSearchFeatureWithResponse(searchId, BinaryData.fromObject(feature), requestOptions)
                .getValue();

        byte[] imageBytes = imageData.toBytes();
        System.out.println("Image size: " + imageBytes.length + " bytes");

        assertTrue(imageBytes.length > 0, "Cropped image should not be empty");
        System.out.println("Search feature crop retrieved successfully");
    }
}
