// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.utils;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthenticatedEndpointPolicyTests {

    @Test
    void shouldRejectUntrustedEndpointBeforeRequestingToken() throws Exception {
        AtomicBoolean tokenRequested = new AtomicBoolean();
        AtomicBoolean requestSent = new AtomicBoolean();
        TokenCredential credential = request -> {
            tokenRequested.set(true);
            return Mono.just(new AccessToken("token", OffsetDateTime.now().plusHours(1)));
        };
        HttpClient client = request -> {
            requestSent.set(true);
            return Mono.error(new AssertionError("Request must not be sent"));
        };
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new AuthenticatedEndpointPolicy(new URL("https://dc.services.visualstudio.com/")),
                new BearerTokenAuthenticationPolicy(credential, "https://monitor.azure.com//.default"))
            .httpClient(client)
            .build();

        assertThatThrownBy(
            () -> pipeline.send(new HttpRequest(HttpMethod.POST, new URL("https://127.0.0.1:9443/v2.1/track"))).block())
                .hasMessageContaining("untrusted endpoint");
        assertThat(tokenRequested).isFalse();
        assertThat(requestSent).isFalse();
    }
}
