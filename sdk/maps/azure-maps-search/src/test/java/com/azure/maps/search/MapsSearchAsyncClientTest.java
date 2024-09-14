// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search;

import com.azure.core.http.HttpClient;
import com.azure.core.models.GeoPosition;
import com.azure.core.util.Context;
import com.azure.maps.search.implementation.models.GeocodingBatchRequestItem;
import com.azure.maps.search.implementation.models.ReverseGeocodingBatchRequestItem;
import com.azure.maps.search.implementation.models.ReverseGeocodingResultTypeEnum;
import com.azure.maps.search.models.BaseSearchOptions;
import com.azure.maps.search.models.BoundaryResultTypeEnum;
import com.azure.maps.search.models.GeocodingBatchRequestBody;
import com.azure.maps.search.models.ResolutionEnum;
import com.azure.maps.search.models.ReverseGeocodingBatchRequestBody;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Arrays;

public class MapsSearchAsyncClientTest extends MapsSearchClientTestBase {
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    private MapsSearchAsyncClient getMapsSearchAsyncClient(HttpClient httpClient,
                                                           MapsSearchServiceVersion serviceVersion) {
        return getMapsSearchAsyncClientBuilder(httpClient, serviceVersion).buildAsyncClient();
    }

    // Test get polygons
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testGetPolygons(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) {
        MapsSearchAsyncClient client = getMapsSearchAsyncClient(httpClient, serviceVersion);
        GeoPosition coordinates = new GeoPosition(-122.204141, 47.61256);
        StepVerifier.create(client.getPolygons(coordinates, null, BoundaryResultTypeEnum.LOCALITY, ResolutionEnum.SMALL))
            .assertNext(MapsSearchClientTestBase::validateGetPolygons)
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Test getGeocode with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testGetGeocodeWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) {
        MapsSearchAsyncClient client = getMapsSearchAsyncClient(httpClient, serviceVersion);
        BaseSearchOptions options = new BaseSearchOptions();
        options.setQuery("1 Microsoft Way, Redmond, WA 98052");
        StepVerifier.create(client.getGeocodingWithBaseResponse(options, Context.NONE))
            .assertNext(MapsSearchClientTestBase::validateGetGeocode)
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Test getGeocodeBatch
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testGetGeocodingBatch(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) {
        MapsSearchAsyncClient client = getMapsSearchAsyncClient(httpClient, serviceVersion);
        GeocodingBatchRequestBody body = new GeocodingBatchRequestBody();
        GeocodingBatchRequestItem item1 = new GeocodingBatchRequestItem();
        item1.setAddressLine("400 Broad St");
        GeocodingBatchRequestItem item2 = new GeocodingBatchRequestItem();
        item2.setQuery("15171 NE 24th St, Redmond, WA 98052, United States");
        body.setBatchItems(Arrays.asList(item1, item2));
        StepVerifier.create(client.getGeocodingBatch(body))
            .assertNext(MapsSearchClientTestBase::validateGetGeocodeBatch)
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Test getReverseGeocoding with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testGetReverseGeocodingWithResponse(HttpClient httpClient,
                                                    MapsSearchServiceVersion serviceVersion) {
        MapsSearchAsyncClient client = getMapsSearchAsyncClient(httpClient, serviceVersion);
        GeoPosition coordinates = new GeoPosition(-122.34255, 47.0);
        StepVerifier.create(client.getReverseGeocodingWithResponse(coordinates, Arrays.asList(ReverseGeocodingResultTypeEnum.ADDRESS), null, Context.NONE))
            .assertNext(MapsSearchClientTestBase::validateGetReverseGeocoding)
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Test GetReverseGeocodingBatch
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testGetReverseGeocodingBatch(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) {
        MapsSearchAsyncClient client = getMapsSearchAsyncClient(httpClient, serviceVersion);
        ReverseGeocodingBatchRequestBody body = new ReverseGeocodingBatchRequestBody();
        ReverseGeocodingBatchRequestItem item1 = new ReverseGeocodingBatchRequestItem();
        ReverseGeocodingBatchRequestItem item2 = new ReverseGeocodingBatchRequestItem();
        item1.setCoordinates(new GeoPosition(-122.34255, 47.0));
        item2.setCoordinates(new GeoPosition(-122.34255, 47.0));
        body.setBatchItems(Arrays.asList(item1, item2));
        StepVerifier.create(client.getReverseGeocodingBatch(body))
            .assertNext(MapsSearchClientTestBase::validateGetReverseGeocodingBatch)
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

}
