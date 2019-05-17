// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core;

import com.azure.core.credentials.BasicAuthenticationCredentials;
import com.azure.core.credentials.TokenCredentials;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.MockHttpClient;
import com.azure.core.http.policy.CredentialsPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import org.junit.Assert;
import org.junit.Test;

import java.net.URL;

public class CredentialsTests {

    @Test
    public void basicCredentialsTest() throws Exception {
        BasicAuthenticationCredentials credentials = new BasicAuthenticationCredentials("user", "pass");

        HttpPipelinePolicy auditorPolicy =  (context, next) -> {
            String headerValue = context.httpRequest().headers().value("Authorization");
            Assert.assertEquals("Basic dXNlcjpwYXNz", headerValue);
            return next.process();
        };
        //
        final HttpPipeline pipeline = HttpPipeline.builder()
            .httpClient(new MockHttpClient())
            .policies(new CredentialsPolicy(credentials), auditorPolicy)
            .build();


        HttpRequest request = new HttpRequest(HttpMethod.GET, new URL("http://localhost"));
        pipeline.send(request).block();
    }

    @Test
    public void tokenCredentialsTest() throws Exception {
        TokenCredentials credentials = new TokenCredentials(null, "this_is_a_token");

        HttpPipelinePolicy auditorPolicy =  (context, next) -> {
            String headerValue = context.httpRequest().headers().value("Authorization");
            Assert.assertEquals("Bearer this_is_a_token", headerValue);
            return next.process();
        };

        final HttpPipeline pipeline = HttpPipeline.builder()
            .httpClient(new MockHttpClient())
            .policies(new CredentialsPolicy(credentials), auditorPolicy)
            .build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, new URL("http://localhost"));
        pipeline.send(request).block();
    }
}
