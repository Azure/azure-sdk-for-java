/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.policy;


import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import com.microsoft.rest.v2.http.UrlBuilder;
import io.reactivex.Single;

/**
 * A RequestPolicy that adds the provided host to each HttpRequest.
 */
public class HostPolicy implements RequestPolicy {
    private final RequestPolicy nextPolicy;
    private final String host;

    HostPolicy(RequestPolicy nextPolicy, String host) {
        this.nextPolicy = nextPolicy;
        this.host = host;
    }

    @Override
    public Single<HttpResponse> sendAsync(HttpRequest request) {
        final UrlBuilder urlBuilder = UrlBuilder.parse(request.url());
        request.withUrl(urlBuilder.withHost(host).toString());
        return nextPolicy.sendAsync(request);
    }

    /**
     * A RequestPolicy.Factory class that creates HostPolicy objects.
     */
    public static class Factory implements RequestPolicy.Factory {
        private final String host;

        /**
         * Create a new HostPolicy.Factory object.
         * @param host The host to set on every HttpRequest.
         */
        public Factory(String host) {
            this.host = host;
        }

        @Override
        public HostPolicy create(RequestPolicy next, Options options) {
            return new HostPolicy(next, host);
        }
    }
}
