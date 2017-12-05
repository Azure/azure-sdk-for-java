/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.policy;


import com.microsoft.rest.v2.http.HttpPipeline;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import com.microsoft.rest.v2.http.UrlBuilder;
import io.reactivex.Single;

/**
 * A RequestPolicy that adds the provided protocol/scheme to each HttpRequest.
 */
public class ProtocolPolicy extends AbstractRequestPolicy {
    private final String protocol;
    private final boolean overwrite;

    ProtocolPolicy(RequestPolicy nextPolicy, Options options, String protocol, boolean overwrite) {
        super(nextPolicy, options);
        this.protocol = protocol;
        this.overwrite = overwrite;
    }

    @Override
    public Single<HttpResponse> sendAsync(HttpRequest request) {
        final UrlBuilder urlBuilder = UrlBuilder.parse(request.url());
        if (overwrite || urlBuilder.scheme() == null) {
            log(HttpPipeline.LogLevel.INFO, "Setting protocol to {0}", protocol);
            request.withUrl(urlBuilder.withScheme(protocol).toString());
        }
        return nextPolicy().sendAsync(request);
    }

    /**
     * A RequestPolicy.Factory class that creates ProtocolPolicy objects.
     */
    public static class Factory implements RequestPolicy.Factory {
        private final String protocol;
        private final boolean overwrite;

        /**
         * Create a new ProtocolPolicy.Factory object.
         * @param protocol The protocol to set on every HttpRequest.
         */
        public Factory(String protocol) {
            this(protocol, true);
        }

        /**
         * Create a new ProtocolPolicy.Factory object.
         * @param protocol The protocol to set.
         * @param overwrite Whether or not to overwrite a HttpRequest's protocol if it already has one.
         */
        public Factory(String protocol, boolean overwrite) {
            this.protocol = protocol;
            this.overwrite = overwrite;
        }

        @Override
        public ProtocolPolicy create(RequestPolicy next, Options options) {
            return new ProtocolPolicy(next, options, protocol, overwrite);
        }
    }
}
