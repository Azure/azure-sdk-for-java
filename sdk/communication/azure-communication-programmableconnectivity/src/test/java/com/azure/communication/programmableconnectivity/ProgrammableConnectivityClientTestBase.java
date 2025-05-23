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
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.function.Function;

public class ProgrammableConnectivityClientTestBase extends TestProxyTestBase {
    protected DeviceLocationClient deviceLocationClient;
    protected DeviceNetworkClient deviceNetworkClient;
    protected NumberVerificationClient numberVerificationClient;
    protected SimSwapClient simSwapClient;
    protected SimSwapAsyncClient simSwapAsyncClient;
    protected DeviceNetworkAsyncClient deviceNetworkAsyncClient;
    protected DeviceLocationAsyncClient deviceLocationAsyncClient;
    protected NumberVerificationAsyncClient numberVerificationAsyncClient;

    protected final HeaderCapturePolicy headerCapturePolicy = new HeaderCapturePolicy();

    @Override
    protected void beforeTest() {
        super.beforeTest();

        if (!interceptorManager.isLiveMode()) {
            interceptorManager.addSanitizers(Arrays.asList(
                new TestProxySanitizer("/subscriptions/[a-zA-Z0-9-]+/", "/subscriptions/sanitized-subscription-id/",
                    TestProxySanitizerType.URL),
                new TestProxySanitizer("/resourceGroups/[a-zA-Z0-9-]+/", "/resourceGroups/sanitized-resource-group/",
                    TestProxySanitizerType.URL),
                new TestProxySanitizer("/gateways/[a-zA-Z0-9-]+", "/gateways/sanitized-gateway",
                    TestProxySanitizerType.URL)));
        }

        deviceLocationClient = createClient(ProgrammableConnectivityClientBuilder::buildDeviceLocationClient);
        deviceNetworkClient = createClient(ProgrammableConnectivityClientBuilder::buildDeviceNetworkClient);
        numberVerificationClient = createClient(ProgrammableConnectivityClientBuilder::buildNumberVerificationClient);
        simSwapClient = createClient(ProgrammableConnectivityClientBuilder::buildSimSwapClient);

        simSwapAsyncClient = createClient(ProgrammableConnectivityClientBuilder::buildSimSwapAsyncClient);
        deviceNetworkAsyncClient = createClient(ProgrammableConnectivityClientBuilder::buildDeviceNetworkAsyncClient);
        deviceLocationAsyncClient = createClient(ProgrammableConnectivityClientBuilder::buildDeviceLocationAsyncClient);
        numberVerificationAsyncClient
            = createClient(ProgrammableConnectivityClientBuilder::buildNumberVerificationAsyncClient);
    }

    /**
     * @return A configured builder instance ready for client-specific build methods
     */
    private ProgrammableConnectivityClientBuilder createConfiguredBuilder() {
        ProgrammableConnectivityClientBuilder builder = new ProgrammableConnectivityClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
            .httpClient(getHttpClientOrUsePlayback(getHttpClients().findFirst().orElse(null)))
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));

        if (getTestMode() == TestMode.PLAYBACK) {
            builder.addPolicy(new PlaybackHeaderConsistencyPolicy()).credential(new MockTokenCredential());
        } else if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
                .credential(new DefaultAzureCredentialBuilder().build());
        } else if (getTestMode() == TestMode.LIVE) {
            builder.credential(new DefaultAzureCredentialBuilder().build());
        }

        builder.addPolicy(headerCapturePolicy);

        return builder;
    }

    private <T> T createClient(Function<ProgrammableConnectivityClientBuilder, T> builderMethod) {
        ProgrammableConnectivityClientBuilder builder = createConfiguredBuilder();
        return builderMethod.apply(builder);
    }

    /**
     * Policy that ensures headers match sanitized values in playback mode.
     * This is required for playback to work with sanitized recordings.
     */
    private static class PlaybackHeaderConsistencyPolicy implements HttpPipelinePolicy {
        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            if (context.getHttpRequest().getHeaders().getValue("apc-gateway-id") != null) {
                context.getHttpRequest().getHeaders().set("apc-gateway-id", "sanitized-gateway-id");
            }

            return next.process();
        }
    }

    public static class HeaderCapturePolicy implements HttpPipelinePolicy {
        private HttpHeaders lastResponseHeaders;

        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            return next.process().map(response -> {
                lastResponseHeaders = response.getHeaders();
                return response;
            });
        }

        public HttpHeaders getLastResponseHeaders() {
            return lastResponseHeaders;
        }

        public String getHeaderValue(String headerName) {
            return lastResponseHeaders != null ? lastResponseHeaders.getValue(headerName) : null;
        }
    }
}
