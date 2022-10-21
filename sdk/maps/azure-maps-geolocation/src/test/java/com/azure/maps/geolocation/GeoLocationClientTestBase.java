// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.geolocation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.test.InterceptorManager;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.identity.EnvironmentCredentialBuilder;
import com.azure.maps.geolocation.models.IpAddressToLocationResult;

public class GeoLocationClientTestBase extends TestBase {
    static final String FAKE_API_KEY = "1234567890";

    private final String endpoint = Configuration.getGlobalConfiguration().get("API-LEARN_ENDPOINT");
    Duration durationTestMode;
    static InterceptorManager interceptorManagerTestBase;

    @Override
    protected void beforeTest() {
        if (interceptorManager.isPlaybackMode()) {
            durationTestMode = Duration.ofMillis(1);
        } else {
            durationTestMode = TestUtils.DEFAULT_POLL_INTERVAL;
        }
        interceptorManagerTestBase = interceptorManager;
    }

    GeoLocationClientBuilder getGeoLocationAsyncClientBuilder(HttpClient httpClient, GeoLocationServiceVersion serviceVersion) {
        GeoLocationClientBuilder builder = new GeoLocationClientBuilder()
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .serviceVersion(serviceVersion);
        String endpoint = getEndpoint();
        if (getEndpoint() != null) {
            builder.endpoint(endpoint);
        }
        if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }
        if (getTestMode() == TestMode.PLAYBACK) {
            builder.credential(new AzureKeyCredential(FAKE_API_KEY)).httpClient(interceptorManager.getPlaybackClient());
        } else {
            builder.credential((new AzureKeyCredential(
                Configuration.getGlobalConfiguration().get("SUBSCRIPTION_KEY"))));
        }
        return builder;
    }

    HttpPipeline getHttpPipeline(HttpClient httpClient) {
        TokenCredential credential = null;

        if (!interceptorManager.isPlaybackMode()) {
            credential = new EnvironmentCredentialBuilder().httpClient(httpClient).build();
        }

        final List<HttpPipelinePolicy> policies = new ArrayList<>();
        if (credential != null) {
            policies.add(new BearerTokenAuthenticationPolicy(credential, endpoint.replaceFirst("/$", "") + "/.default"));
        }

        if (getTestMode() == TestMode.RECORD) {
            policies.add(interceptorManager.getRecordPolicy());
        }

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
            .build();

        return pipeline;
    }

    String getEndpoint() {
        return interceptorManager.isPlaybackMode()
            ? "https://localhost:8080"
            : endpoint;
    }

    static void validateGetLocation(IpAddressToLocationResult expected, IpAddressToLocationResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getCountryRegion().getIsoCode(), actual.getCountryRegion().getIsoCode());
        assertEquals(expected.getIpAddress(), actual.getIpAddress());
    }

    static void validateGetLocationWithResponse(IpAddressToLocationResult expected, int expectedStatusCode, Response<IpAddressToLocationResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetLocation(expected, response.getValue());
    }
}
