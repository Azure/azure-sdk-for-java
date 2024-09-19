// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.implementation;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.SyncAsyncExtension;
import com.azure.core.test.annotation.SyncAsyncTest;
import com.azure.core.test.http.NoOpHttpClient;
import com.azure.core.util.Context;
import reactor.core.publisher.Mono;

import static com.azure.data.appconfiguration.implementation.ConfigurationClientCredentials.X_MS_CONTENT_SHA256;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConfigurationCredentialPolicyTest {
    private static final String CONNECTION_STRING = "Endpoint=http://localhost:8080;Id=0000000000000;Secret=MDAwMDAw";
    private static final String HMAC_SHA256 = "HMAC-SHA256 Credential=";

    @SyncAsyncTest
    public void addsRequiredHeadersTest() {
        ConfigurationCredentialsPolicy configurationCredentialsPolicy
            = new ConfigurationCredentialsPolicy(new ConfigurationClientCredentials(CONNECTION_STRING));

        HttpPipelinePolicy auditorPolicy = (context, next) -> {
            final HttpHeaders headers = context.getHttpRequest().getHeaders();
            assertEquals("localhost", headers.get(HttpHeaderName.HOST).getValue());
            assertNotNull(headers.get(X_MS_CONTENT_SHA256).getValue());
            assertNotNull(headers.get(HttpHeaderName.DATE).getValue());
            assertTrue(headers.get(HttpHeaderName.AUTHORIZATION).getValue().contains(HMAC_SHA256));
            return next.process();
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient())
            .policies(configurationCredentialsPolicy, auditorPolicy)
            .build();

        SyncAsyncExtension.execute(() -> sendRequestSync(pipeline), () -> sendRequest(pipeline));
    }

    private Mono<HttpResponse> sendRequest(HttpPipeline pipeline) {
        return pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/"));
    }

    private HttpResponse sendRequestSync(HttpPipeline pipeline) {
        return pipeline.sendSync(new HttpRequest(HttpMethod.GET, "http://localhost/"), Context.NONE);
    }

}
