// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.programmableconnectivity;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.util.Arrays;

public class ProgrammableConnectivityClientTestBase extends TestProxyTestBase {
    protected DeviceLocationClient deviceLocationClient;
    protected DeviceNetworkClient deviceNetworkClient;
    protected NumberVerificationClient numberVerificationClient;
    protected SimSwapClient simSwapClient;

    // Add the HeaderCapturePolicy field
    protected final HeaderCapturePolicy headerCapturePolicy = new HeaderCapturePolicy();

    @Override
    protected void beforeTest() {
        super.beforeTest();

        // Standard sanitizers for URL paths with fixed regex patterns
        if (!interceptorManager.isLiveMode()) {
            // Use fixed regex patterns that won't cause JSON escaping issues
            interceptorManager.addSanitizers(Arrays.asList(
                // Use simpler patterns without potentially problematic regex constructs
                new TestProxySanitizer("/subscriptions/[a-zA-Z0-9-]+/", "/subscriptions/sanitized-subscription-id/",
                    TestProxySanitizerType.URL),
                new TestProxySanitizer("/resourceGroups/[a-zA-Z0-9-]+/", "/resourceGroups/sanitized-resource-group/",
                    TestProxySanitizerType.URL),
                new TestProxySanitizer("/gateways/[a-zA-Z0-9-]+", "/gateways/sanitized-gateway",
                    TestProxySanitizerType.URL)));
        }

        // Set up clients with the sanitization policy
        ProgrammableConnectivityClientBuilder deviceLocationClientbuilder = new ProgrammableConnectivityClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
            .httpClient(getHttpClientOrUsePlayback(getHttpClients().findFirst().orElse(null)))
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));

        if (getTestMode() == TestMode.PLAYBACK) {
            deviceLocationClientbuilder.addPolicy(new PlaybackHeaderConsistencyPolicy())
                .credential(new MockTokenCredential());
        } else if (getTestMode() == TestMode.RECORD) {
            deviceLocationClientbuilder.addPolicy(interceptorManager.getRecordPolicy())
                .credential(new DefaultAzureCredentialBuilder().build());
        } else if (getTestMode() == TestMode.LIVE) {
            deviceLocationClientbuilder.credential(new DefaultAzureCredentialBuilder().build());
        }
        deviceLocationClient = deviceLocationClientbuilder.buildDeviceLocationClient();

        // Apply the same pattern to all other clients
        // For deviceNetworkClient:
        ProgrammableConnectivityClientBuilder deviceNetworkClientbuilder = new ProgrammableConnectivityClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
            .httpClient(getHttpClientOrUsePlayback(getHttpClients().findFirst().orElse(null)))
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));

        if (getTestMode() == TestMode.PLAYBACK) {
            deviceNetworkClientbuilder.addPolicy(new PlaybackHeaderConsistencyPolicy())
                .credential(new MockTokenCredential());
        } else if (getTestMode() == TestMode.RECORD) {
            deviceNetworkClientbuilder.addPolicy(interceptorManager.getRecordPolicy())
                .credential(new DefaultAzureCredentialBuilder().build());
        } else if (getTestMode() == TestMode.LIVE) {
            deviceNetworkClientbuilder.credential(new DefaultAzureCredentialBuilder().build());
        }
        deviceNetworkClient = deviceNetworkClientbuilder.buildDeviceNetworkClient();

        // Apply to numberVerificationClient:
        ProgrammableConnectivityClientBuilder numberVerificationClientbuilder
            = new ProgrammableConnectivityClientBuilder()
                .endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
                .httpClient(getHttpClientOrUsePlayback(getHttpClients().findFirst().orElse(null)))
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));

        if (getTestMode() == TestMode.PLAYBACK) {
            numberVerificationClientbuilder.addPolicy(new PlaybackHeaderConsistencyPolicy())
                .credential(new MockTokenCredential());
        } else if (getTestMode() == TestMode.RECORD) {
            numberVerificationClientbuilder.addPolicy(interceptorManager.getRecordPolicy())
                .credential(new DefaultAzureCredentialBuilder().build());
        } else if (getTestMode() == TestMode.LIVE) {
            numberVerificationClientbuilder.credential(new DefaultAzureCredentialBuilder().build());
        }
        numberVerificationClient = numberVerificationClientbuilder.buildNumberVerificationClient();

        // And finally for simSwapClient:
        ProgrammableConnectivityClientBuilder simSwapClientbuilder = new ProgrammableConnectivityClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
            .httpClient(getHttpClientOrUsePlayback(getHttpClients().findFirst().orElse(null)))
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));

        if (getTestMode() == TestMode.PLAYBACK) {
            simSwapClientbuilder.addPolicy(new PlaybackHeaderConsistencyPolicy()).credential(new MockTokenCredential());
        } else if (getTestMode() == TestMode.RECORD) {
            simSwapClientbuilder.addPolicy(interceptorManager.getRecordPolicy())
                .credential(new DefaultAzureCredentialBuilder().build());
        } else if (getTestMode() == TestMode.LIVE) {
            simSwapClientbuilder.credential(new DefaultAzureCredentialBuilder().build());
        }

        // Add the headerCapturePolicy to the client pipeline -TODO
        simSwapClientbuilder.addPolicy(headerCapturePolicy);

        simSwapClient = simSwapClientbuilder.buildSimSwapClient();
    }

    /**
     * Policy that ensures headers match sanitized values in playback mode.
     * This is required for playback to work with sanitized recordings.
     */
    private static class PlaybackHeaderConsistencyPolicy implements HttpPipelinePolicy {
        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            // In PLAYBACK mode, use the same sanitized header values that are in the recording
            if (context.getHttpRequest().getHeaders().getValue("apc-gateway-id") != null) {
                context.getHttpRequest().getHeaders().set("apc-gateway-id", "sanitized-gateway-id");
            }

            return next.process();
        }
    }

    /**
     * Policy that captures HTTP response headers for inspection in tests.
     */
    public static class HeaderCapturePolicy implements HttpPipelinePolicy {
        private HttpHeaders lastResponseHeaders;

        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            return next.process().map(response -> {
                // Save the headers from the response
                lastResponseHeaders = response.getHeaders();
                return response;
            });
        }

        /**
         * Gets the headers from the last HTTP response.
         * @return The HTTP headers from the last response.
         */
        public HttpHeaders getLastResponseHeaders() {
            return lastResponseHeaders;
        }

        /**
         * Gets a specific header value from the last HTTP response.
         * @param headerName The name of the header to retrieve.
         * @return The header value, or null if the header doesn't exist.
         */
        public String getHeaderValue(String headerName) {
            return lastResponseHeaders != null ? lastResponseHeaders.getValue(headerName) : null;
        }
    }
}
