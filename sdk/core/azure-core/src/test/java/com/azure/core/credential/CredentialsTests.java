// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.credential;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.clients.NoOpHttpClient;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URL;
import java.time.OffsetDateTime;

public class CredentialsTests {

    @Test
    public void basicCredentialsTest() throws Exception {
        BasicAuthenticationCredential credentials = new BasicAuthenticationCredential("user", "pass");

        HttpPipelinePolicy auditorPolicy =  (context, next) -> {
            String headerValue = context.getHttpRequest().getHeaders().getValue("Authorization");
            Assertions.assertEquals("Basic dXNlcjpwYXNz", headerValue);
            return next.process();
        };
        //
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient())
            .policies((context, next) -> credentials.getToken(new TokenRequestContext().addScopes("scope./default"))
                .flatMap(token -> {
                    context.getHttpRequest().getHeaders().put("Authorization", "Basic " + token.getToken());
                    return next.process();
                }), auditorPolicy)
            .build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, new URL("http://localhost"));
        pipeline.send(request).block();
    }

    @Test
    public void tokenCredentialTest() throws Exception {
        TokenCredential credentials = request -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX));

        HttpPipelinePolicy auditorPolicy =  (context, next) -> {
            String headerValue = context.getHttpRequest().getHeaders().getValue("Authorization");
            Assertions.assertEquals("Bearer this_is_a_token", headerValue);
            return next.process();
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder()
                .httpClient(new NoOpHttpClient())
                .policies(new BearerTokenAuthenticationPolicy(credentials, "scope./default"), auditorPolicy)
                .build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, new URL("https://localhost"));
        pipeline.send(request).block();
    }

    @Test
    public void tokenCredentialHttpSchemeTest() throws Exception {
        TokenCredential credentials = request -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX));

        HttpPipelinePolicy auditorPolicy =  (context, next) -> {
            String headerValue = context.getHttpRequest().getHeaders().getValue("Authorization");
            Assertions.assertEquals("Bearer this_is_a_token", headerValue);
            return next.process();
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder()
                .httpClient(new NoOpHttpClient())
                .policies(new BearerTokenAuthenticationPolicy(credentials, "scope./default"), auditorPolicy)
                .build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, new URL("http://localhost"));
        StepVerifier.create(pipeline.send(request))
                .expectErrorMessage("token credentials require a URL using the HTTPS protocol scheme")
                .verify();
    }
}
