// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.utils;

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.SyncAsyncExtension;
import com.azure.core.test.annotation.SyncAsyncTest;
import com.azure.core.test.http.NoOpHttpClient;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Assertions;

import java.net.URL;

/**
 * Tests for {@link MockTokenCredential}.
 */
public class MockTokenCredentialTests {

    @SyncAsyncTest
    public void basicRetrieveToken() {
        MockTokenCredential credential = new MockTokenCredential();

        HttpPipelinePolicy auditorPolicy =  (context, next) -> {
            String headerValue = context.getHttpRequest().getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
            Assertions.assertTrue(headerValue != null && headerValue.startsWith("mockToken"));
            return next.process();
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient())
            .policies((context, next) -> credential.getToken(new TokenRequestContext())
                .flatMap(token -> {
                    context.getHttpRequest().getHeaders().set(HttpHeaderName.AUTHORIZATION, token.getToken());
                    return next.process();
                }), auditorPolicy)
            .build();

        SyncAsyncExtension.execute(
            () -> pipeline.sendSync(new HttpRequest(HttpMethod.GET, new URL("http://localhost")), Context.NONE),
            () -> pipeline.send(new HttpRequest(HttpMethod.GET, new URL("http://localhost")))
        );
    }
}
