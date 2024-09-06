// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.models.GeoPosition;
import com.azure.core.util.Context;
import com.azure.maps.search.implementation.models.Boundary;
import com.azure.maps.search.implementation.models.BoundaryResultTypeEnum;
import com.azure.maps.search.implementation.models.GeoJsonGeometryCollection;
import com.azure.maps.search.implementation.models.GeocodingBatchRequestBody;
import com.azure.maps.search.implementation.models.GeocodingBatchRequestItem;
import com.azure.maps.search.implementation.models.GeocodingBatchResponse;
import com.azure.maps.search.implementation.models.GeocodingResponse;
import com.azure.maps.search.implementation.models.ResolutionEnum;
import com.azure.maps.search.implementation.models.ReverseGeocodingBatchRequestBody;
import com.azure.maps.search.implementation.models.ReverseGeocodingBatchRequestItem;
import com.azure.maps.search.implementation.models.ReverseGeocodingResultTypeEnum;
import com.azure.maps.search.models.BaseSearchOptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MapsSearchClientTest extends MapsSearchClientTestBase {

    private MapsSearchClient client;
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";

    private MapsSearchClient getMapsSearchClient(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) {
        return getMapsSearchAsyncClientBuilder(httpClient, serviceVersion).buildClient();
    }

    // Test get polygons
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testGetPolygons(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) {
        client = getMapsSearchClient(httpClient, serviceVersion);
        GeoPosition coordinates = new GeoPosition(-122.204141, 47.61256);
        Boundary response = client.getPolygons(coordinates, null, BoundaryResultTypeEnum.LOCALITY, ResolutionEnum.SMALL);
        assertNotNull(response);
        assertEquals(response.getType().toString(), "Feature");
        assertEquals(((GeoJsonGeometryCollection) response.getGeometry()).getGeometries().size(), 1);
    }

    // Test get polygons with response
    // Case 1: Response 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testGetPolygonsWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) {
        client = getMapsSearchClient(httpClient, serviceVersion);
        GeoPosition coordinates = new GeoPosition(-122.204141, 47.61256);
        Response<Boundary> response = client.getPolygonsWithResponse(coordinates, null, BoundaryResultTypeEnum.LOCALITY, ResolutionEnum.SMALL, Context.NONE);
        assertEquals(200, response.getStatusCode());
    }


    // Case 2: Response 400, incorrect input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testGetInvalidInputPolygonsWithResponse(HttpClient httpClient,
        MapsSearchServiceVersion serviceVersion) {
        client = getMapsSearchClient(httpClient, serviceVersion);
        GeoPosition coordinates = new GeoPosition(47.61256, -122.204141);
        final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
            () -> client.getPolygonsWithResponse(coordinates, null, BoundaryResultTypeEnum.LOCALITY, ResolutionEnum.SMALL, Context.NONE));
        assertEquals(400, httpResponseException.getResponse().getStatusCode());
    }


    // Test getGeocode
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testGetGeocode(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) {
        client = getMapsSearchClient(httpClient, serviceVersion);
        BaseSearchOptions options = new BaseSearchOptions();
        options.setQuery("1 Microsoft Way, Redmond, WA 98052");
        GeocodingResponse response = client.getGeocoding(options);
        assertEquals("1 Microsoft Way", response.getFeatures().getFirst().getProperties().getAddress().getAddressLine());
        assertEquals("98052", response.getFeatures().getFirst().getProperties().getAddress().getPostalCode());
        assertEquals("Redmond", response.getFeatures().getFirst().getProperties().getAddress().getLocality());
    }



    // Test getGeocode with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testGetGeocodeWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) {
        client = getMapsSearchClient(httpClient, serviceVersion);
        BaseSearchOptions options = new BaseSearchOptions();
        options.setQuery("1 Microsoft Way, Redmond, WA 98052");
        Response<GeocodingResponse> response = client.getGeocodingWithResponse(options, Context.NONE);
        assertEquals(200, response.getStatusCode());
    }



    // Case 2: 400 incorrect input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testInvalidGetGeocodeWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) {
        client = getMapsSearchClient(httpClient, serviceVersion);
        final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
            () -> client.getGeocodingWithResponse(new BaseSearchOptions(), Context.NONE));
        assertEquals(400, httpResponseException.getResponse().getStatusCode());
    }



    // Test getGeocodeBatch
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testGetGeocodingBatch(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) {
        client = getMapsSearchClient(httpClient, serviceVersion);
        GeocodingBatchRequestBody body = new GeocodingBatchRequestBody();
        GeocodingBatchRequestItem item1 = new GeocodingBatchRequestItem();
        item1.setAddressLine("400 Broad St");
        GeocodingBatchRequestItem item2 = new GeocodingBatchRequestItem();
        item2.setQuery("15171 NE 24th St, Redmond, WA 98052, United States");
        body.setBatchItems(Arrays.asList(item1, item2));
        GeocodingBatchResponse response = client.getGeocodingBatch(body);
        assertEquals(2,response.getSummary().getSuccessfulRequests());
        assertEquals(2,response.getSummary().getTotalRequests());
    }


    // Test getGeocodeBatch with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testGetGeocodingBatchWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) {
        client = getMapsSearchClient(httpClient, serviceVersion);
        GeocodingBatchRequestBody body = new GeocodingBatchRequestBody();
        GeocodingBatchRequestItem item1 = new GeocodingBatchRequestItem();
        item1.setAddressLine("400 Broad St");
        GeocodingBatchRequestItem item2 = new GeocodingBatchRequestItem();
        item2.setQuery("15171 NE 24th St, Redmond, WA 98052, United States");
        body.setBatchItems(Arrays.asList(item1, item2));
        Response<GeocodingBatchResponse> response = client.getGeocodingBatchWithResponse(body, Context.NONE);
        assertEquals(200, response.getStatusCode());
    }



    // Case 2: 400 incorrect input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testInvalidGetGeocodingBatchWithResponse(HttpClient httpClient,
        MapsSearchServiceVersion serviceVersion) {
        client = getMapsSearchClient(httpClient, serviceVersion);
        final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
            () -> client.getGeocodingBatchWithResponse(
                new GeocodingBatchRequestBody(), Context.NONE));
        assertEquals(400, httpResponseException.getResponse().getStatusCode());
    }



    // Test getReverseGeocoding
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testGetReverseGeocoding(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) {
        client = getMapsSearchClient(httpClient, serviceVersion);
        GeoPosition coordinates = new GeoPosition(-122.34255, 47.0);
        GeocodingResponse response = client.getReverseGeocoding(coordinates, Arrays.asList(ReverseGeocodingResultTypeEnum.ADDRESS), null);
        assertEquals("Graham", response.getFeatures().getFirst().getProperties().getAddress().getLocality());
    }



    // Test getReverseGeocoding with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testGetReverseGeocodingWithResponse(HttpClient httpClient,
        MapsSearchServiceVersion serviceVersion) {
        client = getMapsSearchClient(httpClient, serviceVersion);
        GeoPosition coordinates = new GeoPosition(-122.34255, 47.0);
        Response<GeocodingResponse> response = client.getReverseGeocodingWithResponse(coordinates, Arrays.asList(ReverseGeocodingResultTypeEnum.ADDRESS), null, Context.NONE);
        assertEquals(200, response.getStatusCode());
    }



    // Case 2: 400 incorrect input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testInvalidGetReverseGeocodingWithResponse(HttpClient httpClient,
        MapsSearchServiceVersion serviceVersion) {
        client = getMapsSearchClient(httpClient, serviceVersion);
        final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
            () -> client.getReverseGeocodingWithResponse(
                new GeoPosition(-100, -100), Arrays.asList(ReverseGeocodingResultTypeEnum.ADDRESS), null, Context.NONE));
        assertEquals(400, httpResponseException.getResponse().getStatusCode());
    }


    // Test GetReverseGeocodingBatch
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testGetReverseGeocodingBatch(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) {
        client = getMapsSearchClient(httpClient, serviceVersion);
        ReverseGeocodingBatchRequestBody body = new ReverseGeocodingBatchRequestBody();
        ReverseGeocodingBatchRequestItem item1 = new ReverseGeocodingBatchRequestItem();
        ReverseGeocodingBatchRequestItem item2 = new ReverseGeocodingBatchRequestItem();
        item1.setCoordinates(new GeoPosition(-122.34255, 47.0));
        item2.setCoordinates(new GeoPosition(-122.34255, 47.0));
        body.setBatchItems(Arrays.asList(item1, item2));
        GeocodingBatchResponse response = client.getReverseGeocodingBatch(body);
        assertEquals(2, response.getSummary().getTotalRequests());
        assertEquals(2, response.getSummary().getSuccessfulRequests());
    }



    // Test testGetReverseGeocodingBatch with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testGetReverseGeocodingBatchWithResponse(HttpClient httpClient,
        MapsSearchServiceVersion serviceVersion) {
        client = getMapsSearchClient(httpClient, serviceVersion);
        ReverseGeocodingBatchRequestBody body = new ReverseGeocodingBatchRequestBody();
        ReverseGeocodingBatchRequestItem item1 = new ReverseGeocodingBatchRequestItem();
        ReverseGeocodingBatchRequestItem item2 = new ReverseGeocodingBatchRequestItem();
        item1.setCoordinates(new GeoPosition(-122.34255, 47.0));
        item2.setCoordinates(new GeoPosition(-122.34255, 47.0));
        body.setBatchItems(Arrays.asList(item1, item2));
        Response<GeocodingBatchResponse> response = client.getReverseGeocodingBatchWithResponse(body, Context.NONE);
        assertEquals(200, response.getStatusCode());
    }



    // Case 2: 400 incorrect input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testInvalidGetReverseGeocodingBatchWithResponse(HttpClient httpClient,
        MapsSearchServiceVersion serviceVersion) {
        client = getMapsSearchClient(httpClient, serviceVersion);
        final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
            () -> client.getReverseGeocodingBatchWithResponse(
                new ReverseGeocodingBatchRequestBody(), Context.NONE));
        assertEquals(400, httpResponseException.getResponse().getStatusCode());
    }
}
