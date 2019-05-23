// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core;

import com.azure.core.credentials.BasicAuthenticationCredential;
import com.azure.core.credentials.TokenCredential;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.MockHttpClient;
import com.azure.core.http.policy.TokenCredentialPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import org.junit.Assert;
import org.junit.Test;
import reactor.core.publisher.Mono;

import java.net.URL;

public class CredentialsTests {

    @Test
    public void basicCredentialsTest() throws Exception {
        BasicAuthenticationCredential credentials = new BasicAuthenticationCredential("user", "pass");

        HttpPipelinePolicy auditorPolicy =  (context, next) -> {
            String headerValue = context.httpRequest().headers().value("Authorization");
            Assert.assertEquals("Basic dXNlcjpwYXNz", headerValue);
            return next.process();
        };
        //
        final HttpPipeline pipeline = HttpPipeline.builder()
            .httpClient(new MockHttpClient())
            .policies(new TokenCredentialPolicy(credentials), auditorPolicy)
            .build();


        HttpRequest request = new HttpRequest(HttpMethod.GET, new URL("http://localhost"));
        pipeline.send(request).block();
    }

    @Test
    public void tokenCredentialTest() throws Exception {
        TokenCredential credentials = new TokenCredential("Bearer") {
            @Override
            public Mono<String> getTokenAsync(String resource) {
                return Mono.just("this_is_a_token");
            }
        };

        HttpPipelinePolicy auditorPolicy =  (context, next) -> {
            String headerValue = context.httpRequest().headers().value("Authorization");
            Assert.assertEquals("Bearer this_is_a_token", headerValue);
            return next.process();
        };

        final HttpPipeline pipeline = HttpPipeline.builder()
            .httpClient(new MockHttpClient())
            .policies(new TokenCredentialPolicy(credentials), auditorPolicy)
            .build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, new URL("http://localhost"));
        pipeline.send(request).block();
    }
}
