// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer;

import com.azure.analytics.planetarycomputer.models.TilerMosaicSearchRegistrationResponse;
import com.azure.analytics.planetarycomputer.models.TilerStacSearchRegistration;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Mosaics Tiler operations (Group 05a: Tests 01-05).
 */
@Tag("Mosaics")
public class TestPlanetaryComputer05aMosaicsTilerTests extends PlanetaryComputerTestBase {

    @Test
    @Tag("RegisterSearch")
    public void test05_01_RegisterMosaicsSearch() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();

        System.out.println("Input - collection_id: " + collectionId);

        String searchId = registerSearch(dataClient, collectionId);

        assertNotNull(searchId, "Search ID should not be null");
        assertTrue(searchId.length() > 0, "Search ID should not be empty");
        System.out.println("Search ID: " + searchId);
    }

    @Test
    @Tag("SearchInfo")
    public void test05_02_GetMosaicsSearchInfo() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();

        String searchId = registerSearch(dataClient, collectionId);
        System.out.println("Registered Search ID: " + searchId);

        TilerStacSearchRegistration searchInfo = dataClient.getSearchInfo(searchId);

        assertNotNull(searchInfo, "Search info should not be null");
        System.out.println("Search info retrieved successfully");
    }

    @Test
    @Tag("TileJson")
    public void test05_03_GetMosaicsTileJson() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();

        String searchId = registerSearch(dataClient, collectionId);
        System.out.println("Using search ID: " + searchId);

        // Use protocol method to pass required assets
        RequestOptions tileJsonOptions = new RequestOptions();
        tileJsonOptions.addQueryParam("assets", "image", false);
        tileJsonOptions.addQueryParam("asset_bidx", "image|1,2,3", false);
        Response<BinaryData> tileJsonResponse = dataClient.getSearchTileJsonWithResponse(searchId, tileJsonOptions);

        assertNotNull(tileJsonResponse, "TileJSON response should not be null");
        assertTrue(tileJsonResponse.getStatusCode() >= 200 && tileJsonResponse.getStatusCode() < 300);
        System.out.println("TileJSON retrieved successfully");
    }

    @Test
    @Tag("Tile")
    public void test05_04_GetMosaicsTile() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();

        String searchId = registerSearch(dataClient, collectionId);
        System.out.println("Using search ID: " + searchId);

        // Use protocol method to pass required assets
        RequestOptions tileOptions = new RequestOptions();
        tileOptions.addQueryParam("assets", "image", false);
        tileOptions.addQueryParam("asset_bidx", "image|1,2,3", false);
        BinaryData imageData = dataClient
            .getSearchTileByFormatWithResponse(searchId, "WebMercatorQuad", 13.0, 2174.0, 3282.0, "png", tileOptions)
            .getValue();

        byte[] imageBytes = imageData.toBytes();
        System.out.println("Image size: " + imageBytes.length + " bytes");

        byte[] pngMagic = new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
        assertTrue(imageBytes.length > 100, "Image should be substantial");
        for (int i = 0; i < pngMagic.length; i++) {
            assertEquals(pngMagic[i], imageBytes[i], String.format("PNG magic byte %d mismatch", i));
        }
        System.out.println("PNG magic bytes verified successfully");
    }

    @Test
    @Tag("WMTS")
    public void test05_05_GetMosaicsWmtsCapabilities() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();

        String searchId = registerSearch(dataClient, collectionId);
        System.out.println("Using search ID: " + searchId);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.addQueryParam("tile_format", "png", false);
        requestOptions.addQueryParam("tile_scale", "1", false);
        requestOptions.addQueryParam("minzoom", "7", false);
        requestOptions.addQueryParam("maxzoom", "13", false);
        requestOptions.addQueryParam("assets", "image", false);
        requestOptions.addQueryParam("asset_bidx", "image|1,2,3", false);
        byte[] xmlBytes
            = dataClient.getSearchWmtsCapabilitiesByTmsWithResponse(searchId, "WebMercatorQuad", requestOptions)
                .getValue()
                .toBytes();

        String xmlString = new String(xmlBytes, StandardCharsets.UTF_8);
        assertTrue(xmlBytes.length > 0, "XML bytes should not be empty");
        assertTrue(xmlString.contains("Capabilities"), "Response should contain Capabilities element");
        assertTrue(xmlString.toLowerCase().contains("wmts"), "Response should reference WMTS");
        System.out.println("WMTS capabilities XML validated successfully");
    }

    /**
     * Registers a mosaics search using protocol method with raw JSON filter.
     */
    protected String registerSearch(DataClient dataClient, String collectionId) {
        String requestBody = "{\"filter\":" + createCqlFilterJson(collectionId) + ",\"filter-lang\":\"cql2-json\""
            + ",\"sortby\":[{\"field\":\"datetime\",\"direction\":\"desc\"}]}";

        Response<BinaryData> response
            = dataClient.registerMosaicsSearchWithResponse(BinaryData.fromString(requestBody), new RequestOptions());

        TilerMosaicSearchRegistrationResponse result
            = response.getValue().toObject(TilerMosaicSearchRegistrationResponse.class);
        return result.getSearchId();
    }

    /**
     * Creates a CQL2-JSON filter as raw JSON string.
     */
    protected String createCqlFilterJson(String collectionId) {
        return "{\"op\":\"and\",\"args\":[" + "{\"op\":\"=\",\"args\":[{\"property\":\"collection\"},\"" + collectionId
            + "\"]}," + "{\"op\":\">=\",\"args\":[{\"property\":\"datetime\"},\"2021-01-01T00:00:00Z\"]},"
            + "{\"op\":\"<=\",\"args\":[{\"property\":\"datetime\"},\"2022-12-31T23:59:59Z\"]}" + "]}";
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
