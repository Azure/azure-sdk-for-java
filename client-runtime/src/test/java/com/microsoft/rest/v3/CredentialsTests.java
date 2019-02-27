/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v3;

import com.microsoft.rest.v3.credentials.BasicAuthenticationCredentials;
import com.microsoft.rest.v3.credentials.TokenCredentials;

import com.microsoft.rest.v3.http.HttpMethod;
import com.microsoft.rest.v3.http.HttpPipeline;
import com.microsoft.rest.v3.http.policy.CredentialsPolicy;
import com.microsoft.rest.v3.http.policy.HttpPipelinePolicy;
import com.microsoft.rest.v3.http.HttpRequest;
import com.microsoft.rest.v3.http.MockHttpClient;
import com.microsoft.rest.v3.http.HttpPipelineOptions;
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
        final HttpPipeline pipeline = new HttpPipeline(new MockHttpClient(),
                new HttpPipelineOptions(null),
                new CredentialsPolicy(credentials),
                auditorPolicy);


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

        final HttpPipeline pipeline = new HttpPipeline(new MockHttpClient(),
                new HttpPipelineOptions(null),
                new CredentialsPolicy(credentials),
                auditorPolicy);

        HttpRequest request = new HttpRequest(HttpMethod.GET, new URL("http://localhost"));
        pipeline.send(request).block();
    }
}
