// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.render;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AzureKeyCredentialPolicy;
import com.azure.core.http.policy.ExponentialBackoff;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.test.InterceptorManager;
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
    private static final String SDK_NAME = "client_name";
    private static final String SDK_VERSION = "client_version";

    static final String FAKE_API_KEY = "fakeKeyPlaceholder";

    static InterceptorManager interceptorManagerTestBase;

    Duration durationTestMode;

    @Override
    protected void beforeTest() {
        if (interceptorManager.isPlaybackMode()) {
            durationTestMode = Duration.ofMillis(1);
        } else {
            durationTestMode = TestUtils.DEFAULT_POLL_INTERVAL;
        }
        interceptorManagerTestBase = interceptorManager;
    }

    MapsRenderClientBuilder getRenderAsyncClientBuilder(HttpClient httpClient, MapsRenderServiceVersion serviceVersion) {
        MapsRenderClientBuilder builder = new MapsRenderClientBuilder()
            .pipeline(getHttpPipeline(httpClient))
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .serviceVersion(serviceVersion);

        if (interceptorManager.isPlaybackMode()) {
            builder.endpoint("https://localhost:8080");
        }

        return builder;
    }

    HttpPipeline getHttpPipeline(HttpClient httpClient) {
        httpClient = interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient;

        if (interceptorManager.isRecordMode() || interceptorManager.isPlaybackMode()) {
            interceptorManager.addSanitizers(
                Collections.singletonList(
                    new TestProxySanitizer("subscription-key", ".+", "REDACTED", TestProxySanitizerType.HEADER)));
        }

        if (interceptorManager.isPlaybackMode()) {
            List<TestProxyRequestMatcher> customMatchers = new ArrayList<>();

            customMatchers.add(new CustomMatcher().setHeadersKeyOnlyMatch(Collections.singletonList("subscription-key")));
            interceptorManager.addMatchers(customMatchers);
        }

        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        policies.add(new UserAgentPolicy(null, SDK_NAME, SDK_VERSION, Configuration.getGlobalConfiguration().clone()));

        HttpPolicyProviders.addBeforeRetryPolicies(policies);

        policies.add(new RetryPolicy(new ExponentialBackoff(5, Duration.ofSeconds(2), Duration.ofSeconds(16))));
        policies.add(
            new AzureKeyCredentialPolicy(
                MapsRenderClientBuilder.RENDER_SUBSCRIPTION_KEY,
                new AzureKeyCredential(interceptorManager.isPlaybackMode()
                    ? FAKE_API_KEY
                    : Configuration.getGlobalConfiguration().get("SUBSCRIPTION_KEY"))));

        HttpPolicyProviders.addAfterRetryPolicies(policies);

        policies.add(new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)));

        if (interceptorManager.isRecordMode()) {
            policies.add(interceptorManager.getRecordPolicy());
        }

        return new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient)
            .build();
    }

    static void validateGetMapTile(byte[] actual) {
        assertNotNull(actual);
        assertTrue(actual.length > 0);
    }

    static void validateGetMapTileWithResponse(int expectedStatusCode, Response<BinaryData> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetMapTile(response.getValue().toBytes());
    }

    static void validateGetMapTileset(MapTileset expected, MapTileset actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getMinZoom(), actual.getMinZoom());
    }

    static void validateGetMapTilesetWithResponse(MapTileset expected, int expectedStatusCode,
                                                  Response<MapTileset> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetMapTileset(expected, response.getValue());
    }

    static void validateGetMapAttribution(MapAttribution expected, MapAttribution actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getCopyrights().size(), actual.getCopyrights().size());
    }

    static void validateGetMapAttributionWithResponse(MapAttribution expected, int expectedStatusCode,
                                                      Response<MapAttribution> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetMapAttribution(expected, response.getValue());
    }

    static void validateGetCopyrightCaption(CopyrightCaption expected, CopyrightCaption actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getFormatVersion(), actual.getFormatVersion());
    }

    static void validateGetCopyrightCaptionWithResponse(CopyrightCaption expected, int expectedStatusCode,
                                                        Response<CopyrightCaption> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetCopyrightCaption(expected, response.getValue());
    }

    static void validateGetCopyrightCaptionFromBoundingBox(Copyright expected, Copyright actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getFormatVersion(), actual.getFormatVersion());
        assertEquals(expected.getGeneralCopyrights().size(), actual.getGeneralCopyrights().size());
        assertEquals(expected.getRegions().size(), actual.getRegions().size());
    }

    static void validateGetCopyrightCaptionFromBoundingBoxWithResponse(Copyright expected, int expectedStatusCode,
                                                                       Response<Copyright> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetCopyrightCaptionFromBoundingBox(expected, response.getValue());
    }

    static void validateGetMapStaticImage(byte[] actual) {
        assertNotNull(actual);
        assertTrue(actual.length > 0);
    }

    static void validateGetMapStaticImageWithResponse(int expectedStatusCode, Response<BinaryData> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetMapStaticImage(response.getValue().toBytes());
    }

    static void validateGetCopyrightForTile(Copyright expected, Copyright actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getFormatVersion(), actual.getFormatVersion());
        assertEquals(expected.getGeneralCopyrights().size(), actual.getGeneralCopyrights().size());
        assertEquals(expected.getRegions().size(), actual.getRegions().size());
    }

    static void validateGetCopyrightForTileWithResponse(Copyright expected, int expectedStatusCode,
                                                        Response<Copyright> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetCopyrightForTile(expected, response.getValue());
    }

    static void validateGetCopyrightForWorld(Copyright expected, Copyright actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getFormatVersion(), actual.getFormatVersion());
        assertEquals(expected.getGeneralCopyrights().size(), actual.getGeneralCopyrights().size());
    }

    static void validateGetCopyrightForWorldWithResponse(Copyright expected, int expectedStatusCode,
                                                         Response<Copyright> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetCopyrightForWorld(expected, response.getValue());
    }
}
