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
import com.microsoft.rest.v3.policy.CredentialsPolicyFactory;
import com.microsoft.rest.v3.http.HttpRequest;
import com.microsoft.rest.v3.http.HttpResponse;
import com.microsoft.rest.v3.http.MockHttpClient;
import com.microsoft.rest.v3.policy.RequestPolicy;
import com.microsoft.rest.v3.policy.RequestPolicyFactory;
import com.microsoft.rest.v3.policy.RequestPolicyOptions;
import org.junit.Assert;
import org.junit.Test;
import reactor.core.publisher.Mono;

import java.net.URL;

public class CredentialsTests {

    @Test
    public void basicCredentialsTest() throws Exception {
        BasicAuthenticationCredentials credentials = new BasicAuthenticationCredentials("user", "pass");

        RequestPolicyFactory auditorFactory = new RequestPolicyFactory() {
            @Override
            public RequestPolicy create(final RequestPolicy next, RequestPolicyOptions options) {
                return new RequestPolicy() {
                    @Override
                    public Mono<HttpResponse> sendAsync(HttpRequest request) {
                        String headerValue = request.headers().value("Authorization");
                        Assert.assertEquals("Basic dXNlcjpwYXNz", headerValue);
                        return next.sendAsync(request);
                    }
                };
            }
        };

        final HttpPipeline pipeline = HttpPipeline.build(
                new MockHttpClient(),
                new CredentialsPolicyFactory(credentials),
                auditorFactory);

        HttpRequest request = new HttpRequest("basicCredentialsTest", HttpMethod.GET, new URL("http://localhost"), null);
        pipeline.sendRequestAsync(request).block();
    }

    @Test
    public void tokenCredentialsTest() throws Exception {
        TokenCredentials credentials = new TokenCredentials(null, "this_is_a_token");

        RequestPolicyFactory auditorFactory = new RequestPolicyFactory() {
            @Override
            public RequestPolicy create(final RequestPolicy next, RequestPolicyOptions options) {
                return new RequestPolicy() {
                    @Override
                    public Mono<HttpResponse> sendAsync(HttpRequest request) {
                        String headerValue = request.headers().value("Authorization");
                        Assert.assertEquals("Bearer this_is_a_token", headerValue);
                        return next.sendAsync(request);
                    }
                };
            }
        };

        HttpPipeline pipeline = HttpPipeline.build(
                new MockHttpClient(),
                new CredentialsPolicyFactory(credentials),
                auditorFactory);

        HttpRequest request = new HttpRequest("basicCredentialsTest", HttpMethod.GET, new URL("http://localhost"), null);
        pipeline.sendRequestAsync(request).block();
    }
}
