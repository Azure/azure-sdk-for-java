/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest;

import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import com.microsoft.rest.v2.http.MockHttpClient;
import com.microsoft.rest.v2.http.MockHttpResponse;
import com.microsoft.rest.v2.policy.RequestPolicy;
import com.microsoft.rest.v2.policy.RequestPolicyChain;
import com.microsoft.rest.v2.policy.SendRequestPolicyFactory;
import com.microsoft.rest.v2.policy.UserAgentPolicy;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Response;
import retrofit2.http.HTTP;
import rx.Single;

public class UserAgentTests {
    @Test
    public void defaultUserAgentTests() throws Exception {
        RequestPolicyChain chain = new RequestPolicyChain(
                new UserAgentPolicy.Factory("AutoRest-Java"),
                new RequestPolicy.Factory() {
                    @Override
                    public RequestPolicy create(final RequestPolicy next) {
                        return new RequestPolicy() {
                            @Override
                            public Single<HttpResponse> sendAsync(HttpRequest request) {
                                Assert.assertEquals(
                                        request.headers().value("User-Agent"),
                                        "AutoRest-Java");
                                return Single.<HttpResponse>just(new MockHttpResponse(200));
                            }
                        };
                    }
                });

        HttpResponse response = chain.sendRequest(new HttpRequest(
                "defaultUserAgentTests",
                "GET", "http://localhost"));

        Assert.assertEquals(200, response.statusCode());
    }

    @Test
    public void customUserAgentTests() throws Exception {
        RequestPolicyChain chain = new RequestPolicyChain(
                new UserAgentPolicy.Factory("Awesome"),
                new RequestPolicy.Factory() {
                    @Override
                    public RequestPolicy create(final RequestPolicy next) {
                        return new RequestPolicy() {
                            @Override
                            public Single<HttpResponse> sendAsync(HttpRequest request) {
                                String header = request.headers().value("User-Agent");
                                Assert.assertEquals("Awesome", header);
                                return Single.<HttpResponse>just(new MockHttpResponse(200));
                            }
                        };
                    }
                }
        );

        HttpResponse response = chain.sendRequest(new HttpRequest("customUserAgentTests", "GET", "http://localhost"));
        Assert.assertEquals(200, response.statusCode());
    }
}
