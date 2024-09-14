// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.render;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.ExponentialBackoff;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxyRequestMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.maps.render.models.Copyright;
import com.azure.maps.render.models.CopyrightCaption;
import com.azure.maps.render.models.MapAttribution;
import com.azure.maps.render.models.MapTileset;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MapsRenderClientTestBase extends TestProxyTestBase {
    MapsRenderClientBuilder getRenderAsyncClientBuilder(HttpClient httpClient,
        MapsRenderServiceVersion serviceVersion) {
        MapsRenderClientBuilder builder = modifyBuilder(httpClient, new MapsRenderClientBuilder()).serviceVersion(
            serviceVersion);

        if (interceptorManager.isPlaybackMode()) {
            builder.endpoint("https://localhost:8080");
        }

        return builder;
    }

    MapsRenderClientBuilder modifyBuilder(HttpClient httpClient, MapsRenderClientBuilder builder) {
        httpClient = interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient;

        if (interceptorManager.isRecordMode() || interceptorManager.isPlaybackMode()) {
            interceptorManager.addSanitizers(Collections.singletonList(
                new TestProxySanitizer("subscription-key", ".+", "REDACTED", TestProxySanitizerType.HEADER)));
            // Remove `name` sanitizers from the list of common sanitizers.
            interceptorManager.removeSanitizers("AZSDK3493");
        }

        if (interceptorManager.isPlaybackMode()) {
            List<TestProxyRequestMatcher> customMatchers = new ArrayList<>();

            customMatchers.add(
                new CustomMatcher().setHeadersKeyOnlyMatch(Collections.singletonList("subscription-key")));
            interceptorManager.addMatchers(customMatchers);
        }

        builder.retryPolicy(new RetryPolicy(new ExponentialBackoff(5, Duration.ofSeconds(2), Duration.ofSeconds(16))))
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        if (interceptorManager.isRecordMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
                .credential(new AzureKeyCredential(Configuration.getGlobalConfiguration().get("SUBSCRIPTION_KEY")));
        } else if (interceptorManager.isPlaybackMode()) {
            builder.credential(new AzureKeyCredential("REDACTED"));
        } else {
            builder.credential(new AzureKeyCredential(Configuration.getGlobalConfiguration().get("SUBSCRIPTION_KEY")));
        }

        return builder.httpClient(
            interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient);
    }

    static void validateGetMapTile(byte[] actual) {
        assertNotNull(actual);
        assertTrue(actual.length > 0);
    }

    static void validateGetMapTileWithResponse(Response<BinaryData> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateGetMapTile(response.getValue().toBytes());
    }

    static void validateGetMapTileset(MapTileset result) {
        assertNotNull(result);
        assertEquals("microsoft.base.hybrid", result.getName());
        assertEquals("xyz", result.getScheme());
    }

    static void validateGetMapTilesetWithResponse(Response<MapTileset> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
    }

    static void validateGetMapAttribution(MapAttribution expected, MapAttribution actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getCopyrights().size(), actual.getCopyrights().size());
    }

    static void validateGetMapAttributionWithResponse(MapAttribution expected, Response<MapAttribution> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateGetMapAttribution(expected, response.getValue());
    }

    static void validateGetCopyrightCaption(CopyrightCaption expected, CopyrightCaption actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getFormatVersion(), actual.getFormatVersion());
    }

    static void validateGetCopyrightCaptionWithResponse(CopyrightCaption expected,
        Response<CopyrightCaption> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateGetCopyrightCaption(expected, response.getValue());
    }

    static void validateGetCopyrightCaptionFromBoundingBox(Copyright result) {
        assertNotNull(result);
        assertEquals("0.0.1", result.getFormatVersion());
        assertEquals(3, result.getRegions().size());
    }

    static void validateGetCopyrightCaptionFromBoundingBoxWithResponse(Response<Copyright> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
    }

    static void validateGetMapStaticImage(byte[] actual) {
        assertNotNull(actual);
        assertTrue(actual.length > 0);
    }

    static void validateGetMapStaticImageWithResponse(Response<BinaryData> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateGetMapStaticImage(response.getValue().toBytes());
    }

    static void validateGetCopyrightForTile(Copyright result) {
        assertNotNull(result);
        assertEquals(6, result.getRegions().size());
        assertEquals("0.0.1", result.getFormatVersion());
    }

    static void validateGetCopyrightForTileWithResponse(Response<Copyright> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
    }

    static void validateGetCopyrightForWorld(Copyright actual) {
        assertNotNull(actual);
        assertEquals("0.0.1", actual.getFormatVersion());
        assertEquals(3, actual.getGeneralCopyrights().size());
    }

    static void validateGetCopyrightForWorldWithResponse(Response<Copyright> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
    }
}
