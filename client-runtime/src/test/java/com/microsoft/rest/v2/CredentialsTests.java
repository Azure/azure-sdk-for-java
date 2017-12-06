/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2;

import com.microsoft.rest.v2.credentials.BasicAuthenticationCredentials;
import com.microsoft.rest.v2.credentials.TokenCredentials;

import com.microsoft.rest.v2.http.HttpPipeline;
import com.microsoft.rest.v2.policy.CredentialsPolicy;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import com.microsoft.rest.v2.http.MockHttpClient;
import com.microsoft.rest.v2.policy.RequestPolicy;
import org.junit.Assert;
import org.junit.Test;
import io.reactivex.Single;

public class CredentialsTests {

    @Test
    public void basicCredentialsTest() throws Exception {
        BasicAuthenticationCredentials credentials = new BasicAuthenticationCredentials("user", "pass");

        RequestPolicy.Factory auditorFactory = new RequestPolicy.Factory() {
            @Override
            public RequestPolicy create(final RequestPolicy next, RequestPolicy.Options options) {
                return new RequestPolicy() {
                    @Override
                    public Single<HttpResponse> sendAsync(HttpRequest request) {
                        String headerValue = request.headers().value("Authorization");
                        Assert.assertEquals("Basic dXNlcjpwYXNz", headerValue);
                        return next.sendAsync(request);
                    }
                };
            }
        };

        final HttpPipeline pipeline = HttpPipeline.build(
                new MockHttpClient(),
                new CredentialsPolicy.Factory(credentials),
                auditorFactory);

        HttpRequest request = new HttpRequest("basicCredentialsTest", "GET", "http://localhost");
        pipeline.sendRequestAsync(request).blockingGet();
    }

    @Test
    public void tokenCredentialsTest() throws Exception {
        TokenCredentials credentials = new TokenCredentials(null, "this_is_a_token");

        RequestPolicy.Factory auditorFactory = new RequestPolicy.Factory() {
            @Override
            public RequestPolicy create(final RequestPolicy next, RequestPolicy.Options options) {
                return new RequestPolicy() {
                    @Override
                    public Single<HttpResponse> sendAsync(HttpRequest request) {
                        String headerValue = request.headers().value("Authorization");
                        Assert.assertEquals("Bearer this_is_a_token", headerValue);
                        return next.sendAsync(request);
                    }
                };
            }
        };

        HttpPipeline pipeline = HttpPipeline.build(
                new MockHttpClient(),
                new CredentialsPolicy.Factory(credentials),
                auditorFactory);

        HttpRequest request = new HttpRequest("basicCredentialsTest", "GET", "http://localhost");
        pipeline.sendRequestAsync(request).blockingGet();
    }
}
