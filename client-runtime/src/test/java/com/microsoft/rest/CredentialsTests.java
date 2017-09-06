/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest;

import com.microsoft.rest.credentials.BasicAuthenticationCredentials;
import com.microsoft.rest.credentials.TokenCredentials;

import com.microsoft.rest.v2.http.*;
import com.microsoft.rest.v2.policy.CredentialsPolicy;
import com.microsoft.rest.v2.policy.RequestPolicy;
import com.microsoft.rest.v2.policy.RequestPolicyChain;
import com.microsoft.rest.v2.policy.SendRequestPolicyFactory;
import org.junit.Assert;
import org.junit.Test;
import rx.Single;

public class CredentialsTests {

    @Test
    public void basicCredentialsTest() throws Exception {
        BasicAuthenticationCredentials credentials = new BasicAuthenticationCredentials("user", "pass");

        RequestPolicy.Factory auditorFactory = new RequestPolicy.Factory() {
            @Override
            public RequestPolicy create(final RequestPolicy next) {
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

        HttpClient client = new RequestPolicyChain(
                new CredentialsPolicy.Factory(credentials),
                auditorFactory,
                new SendRequestPolicyFactory(new MockHttpClient()));

        HttpRequest request = new HttpRequest("basicCredentialsTest", "GET", "http://localhost");
        client.sendRequest(request);
    }

    @Test
    public void tokenCredentialsTest() throws Exception {
        TokenCredentials credentials = new TokenCredentials(null, "this_is_a_token");

        RequestPolicy.Factory auditorFactory = new RequestPolicy.Factory() {
            @Override
            public RequestPolicy create(final RequestPolicy next) {
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

        HttpClient client = new RequestPolicyChain(
                new CredentialsPolicy.Factory(credentials),
                auditorFactory,
                new SendRequestPolicyFactory(new MockHttpClient()));

        HttpRequest request = new HttpRequest("basicCredentialsTest", "GET", "http://localhost");
        client.sendRequest(request);
    }
}
