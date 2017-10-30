/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.policy;

import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import rx.Single;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Type representing RequestPolicy that retries a request at a different host depending on the response.
 */
public final class UseOtherHostPolicy implements RequestPolicy {
    /**
     * Factory to create UseOtherHostPolicy.
     */
    public static class Factory implements RequestPolicy.Factory {
        @Override
        public RequestPolicy create(RequestPolicy next) {
            return new UseOtherHostPolicy(next);
        }
    }

    private final RequestPolicy next;
    private UseOtherHostPolicy(RequestPolicy next) {
        this.next = next;
    }

    @Override
    public Single<HttpResponse> sendAsync(HttpRequest request) {
        URL url;
        try {
           url = new URL(request.url());
        } catch (MalformedURLException e) {
            return Single.error(e);
        }

        // FIXME: check a response header or similar in order to reissue the request with a new host?
        // This is really just here to prove the concept right now
        String newURL = "https://httpbin.org/" + url.getPath() + url.getQuery();

        HttpRequest newRequest = new HttpRequest(request.callerMethod(), request.httpMethod(), newURL);
        newRequest.withBody(request.body());

        return next.sendAsync(newRequest);
    }
}
