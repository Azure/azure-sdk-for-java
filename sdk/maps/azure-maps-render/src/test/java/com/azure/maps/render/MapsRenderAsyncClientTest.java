// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.render;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.models.GeoBoundingBox;
import com.azure.maps.render.models.TileIndex;
import com.azure.maps.render.models.TilesetId;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MapsRenderAsyncClientTest extends MapsRenderClientTestBase {
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    private MapsRenderAsyncClient getRenderAsyncClient(HttpClient httpClient, MapsRenderServiceVersion serviceVersion) {
        return getRenderAsyncClientBuilder(httpClient, serviceVersion).buildAsyncClient();
    }

    // Test async get map tile set
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testAsyncGetMapTileset(HttpClient httpClient, MapsRenderServiceVersion serviceVersion) {
        MapsRenderAsyncClient client = getRenderAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getMapTileset(TilesetId.MICROSOFT_BASE_HYBRID))
            .assertNext(actualResults -> validateGetMapTileset(TestUtils.getExpectedMapTileset(), actualResults))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get map tile set with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testAsyncGetMapTilesetWithResponse(HttpClient httpClient, MapsRenderServiceVersion serviceVersion) {
        MapsRenderAsyncClient client = getRenderAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getMapTilesetWithResponse(TilesetId.MICROSOFT_BASE_HYBRID))
            .assertNext(response -> validateGetMapTilesetWithResponse(TestUtils.getExpectedMapTileset(), response))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testAsyncInvalidGetMapTilesetWithResponse(HttpClient httpClient,
        MapsRenderServiceVersion serviceVersion) {
        MapsRenderAsyncClient client = getRenderAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getMapTilesetWithResponse(new TilesetId())).expectErrorSatisfies(ex -> {
            final HttpResponseException httpResponseException = (HttpResponseException) ex;
            assertEquals(400, httpResponseException.getResponse().getStatusCode());
        }).verify(DEFAULT_TIMEOUT);
    }

    // Test async get map attribution
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testAsyncGetMapAttribution(HttpClient httpClient, MapsRenderServiceVersion serviceVersion) {
        MapsRenderAsyncClient client = getRenderAsyncClient(httpClient, serviceVersion);
        GeoBoundingBox bounds = new GeoBoundingBox(-122.414162, 47.57949, -122.247157, 47.668372);
        StepVerifier.create(client.getMapAttribution(TilesetId.MICROSOFT_BASE, 6, bounds))
            .assertNext(
                actualResults -> validateGetMapAttribution(TestUtils.getExpectedMapAttribution(), actualResults))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get map attribution with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testAsyncGetMapAttributionWithResponse(HttpClient httpClient, MapsRenderServiceVersion serviceVersion) {
        MapsRenderAsyncClient client = getRenderAsyncClient(httpClient, serviceVersion);
        GeoBoundingBox bounds = new GeoBoundingBox(-122.414162, 47.57949, -122.247157, 47.668372);
        StepVerifier.create(client.getMapAttributionWithResponse(TilesetId.MICROSOFT_BASE, 6, bounds))
            .assertNext(
                response -> validateGetMapAttributionWithResponse(TestUtils.getExpectedMapAttribution(), response))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testAsyncInvalidGetMapAttributionWithResponse(HttpClient httpClient,
        MapsRenderServiceVersion serviceVersion) {
        MapsRenderAsyncClient client = getRenderAsyncClient(httpClient, serviceVersion);
        GeoBoundingBox bounds = new GeoBoundingBox(-10000, 0, 0, 0);
        StepVerifier.create(client.getMapAttributionWithResponse(new TilesetId(), 6, bounds))
            .expectErrorSatisfies(ex -> {
                final HttpResponseException httpResponseException = (HttpResponseException) ex;
                assertEquals(400, httpResponseException.getResponse().getStatusCode());
            })
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get copyright caption
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testAsyncGetCopyrightCaption(HttpClient httpClient, MapsRenderServiceVersion serviceVersion) {
        MapsRenderAsyncClient client = getRenderAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getCopyrightCaption())
            .assertNext(
                actualResults -> validateGetCopyrightCaption(TestUtils.getExpectedCopyrightCaption(), actualResults))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get map copyright caption with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testAsyncGetCopyrightCaptionWithResponse(HttpClient httpClient,
        MapsRenderServiceVersion serviceVersion) {
        MapsRenderAsyncClient client = getRenderAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getCopyrightCaptionWithResponse())
            .assertNext(
                response -> validateGetCopyrightCaptionWithResponse(TestUtils.getExpectedCopyrightCaption(), response))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get copyright from bounding box
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testAsyncGetCopyrightFromBoundingBox(HttpClient httpClient, MapsRenderServiceVersion serviceVersion) {
        MapsRenderAsyncClient client = getRenderAsyncClient(httpClient, serviceVersion);
        GeoBoundingBox boundingBox = new GeoBoundingBox(52.41064, 4.84228, 52.41072, 4.84239);
        StepVerifier.create(client.getCopyrightFromBoundingBox(boundingBox, true))
            .assertNext(actualResults -> validateGetCopyrightCaptionFromBoundingBox(
                TestUtils.getExpectedCopyrightFromBoundingBox(), actualResults))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get copyright from bounding box with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testAsyncGetCopyrightFromBoundingBoxWithResponse(HttpClient httpClient,
        MapsRenderServiceVersion serviceVersion) {
        MapsRenderAsyncClient client = getRenderAsyncClient(httpClient, serviceVersion);
        GeoBoundingBox boundingBox = new GeoBoundingBox(52.41064, 4.84228, 52.41072, 4.84239);
        StepVerifier.create(client.getCopyrightFromBoundingBoxWithResponse(boundingBox, true))
            .assertNext(response -> validateGetCopyrightCaptionFromBoundingBoxWithResponse(
                TestUtils.getExpectedCopyrightFromBoundingBox(), response))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testAsyncInvalidGetCopyrightFromBoundingBoxWithResponse(HttpClient httpClient,
        MapsRenderServiceVersion serviceVersion) {
        MapsRenderAsyncClient client = getRenderAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(
                client.getCopyrightFromBoundingBoxWithResponse(new GeoBoundingBox(-100, -100, -100, -100), true))
            .expectErrorSatisfies(ex -> {
                final HttpResponseException httpResponseException = (HttpResponseException) ex;
                assertEquals(400, httpResponseException.getResponse().getStatusCode());
            })
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get copyright for title
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testAsyncGetCopyrightForTitle(HttpClient httpClient, MapsRenderServiceVersion serviceVersion) {
        MapsRenderAsyncClient client = getRenderAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getCopyrightForTile(new TileIndex().setX(9).setY(22).setZ(6), true))
            .assertNext(
                actualResults -> validateGetCopyrightForTile(TestUtils.getExpectedCopyrightForTile(), actualResults))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get copyright for title with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testAsyncGetCopyrightForTitleWithResponse(HttpClient httpClient,
        MapsRenderServiceVersion serviceVersion) {
        MapsRenderAsyncClient client = getRenderAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getCopyrightForTileWithResponse(new TileIndex().setX(9).setY(22).setZ(6), true))
            .assertNext(
                response -> validateGetCopyrightForTileWithResponse(TestUtils.getExpectedCopyrightForTile(), response))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testAsyncInvalidGetCopyrightForTitleWithResponse(HttpClient httpClient,
        MapsRenderServiceVersion serviceVersion) {
        MapsRenderAsyncClient client = getRenderAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getCopyrightForTileWithResponse(new TileIndex().setX(9).setY(22).setZ(-100), true))
            .expectErrorSatisfies(ex -> {
                final HttpResponseException httpResponseException = (HttpResponseException) ex;
                assertEquals(400, httpResponseException.getResponse().getStatusCode());
            })
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get copyright for world
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testAsyncGetCopyrightForWorld(HttpClient httpClient, MapsRenderServiceVersion serviceVersion) {
        MapsRenderAsyncClient client = getRenderAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getCopyrightForWorld(true))
            .assertNext(
                actualResults -> validateGetCopyrightForWorld(TestUtils.getExpectedCopyrightForWorld(), actualResults))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get copyright for world with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testAsyncGetCopyrightForWorldWithResponse(HttpClient httpClient,
        MapsRenderServiceVersion serviceVersion) {
        MapsRenderAsyncClient client = getRenderAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getCopyrightForWorldWithResponse(true))
            .assertNext(response -> validateGetCopyrightForWorldWithResponse(TestUtils.getExpectedCopyrightForWorld(),
                response))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }
}
