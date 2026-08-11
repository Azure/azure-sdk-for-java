// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared.session;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * Mock transport that returns a CreateSession response and records the requested container.
 */
public final class CreateSessionMockHttpClient implements HttpClient {
    private volatile String requestedContainer;

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        String path = request.getUrl().getPath();
        requestedContainer = path.startsWith("/") ? path.substring(1) : path;

        String body = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "<CreateSessionResult>"
            + "<Id>test-session-id</Id>" + "<Expiration>Wed, 09 Sep 2099 00:00:00 GMT</Expiration>"
            + "<AuthenticationType>HMAC</AuthenticationType>" + "<Credentials>" + "<SessionToken>session-token-for-"
            + requestedContainer + "</SessionToken>" + "<SessionKey>" + SessionTestHelper.TEST_SESSION_KEY
            + "</SessionKey>" + "</Credentials>" + "</CreateSessionResult>";
        HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/xml");
        return Mono.just(new MockHttpResponse(request, 201, headers, body.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Gets the container from the most recent CreateSession request.
     *
     * @return The requested container.
     */
    public String getRequestedContainer() {
        return requestedContainer;
    }
}
