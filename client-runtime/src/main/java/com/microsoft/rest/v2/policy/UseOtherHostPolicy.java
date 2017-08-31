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
 * Type representing RequestPolicy that can send request to a different host.
 */
public class UseOtherHostPolicy implements RequestPolicy {
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

        String newURL = "https://httpbin.org/" + url.getPath() + url.getQuery();

        HttpRequest newRequest = new HttpRequest(request.callerMethod(), request.httpMethod(), newURL);
        newRequest.withBody(request.body(), request.mimeType());

        return next.sendAsync(newRequest);
    }
}
