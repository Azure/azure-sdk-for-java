/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.policy;

import com.microsoft.rest.v2.http.HttpPipelineLogLevel;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import com.microsoft.rest.v2.http.UrlBuilder;
import io.reactivex.Single;

import java.net.MalformedURLException;

/**
 * Creates a RequestPolicy that adds the provided host to each HttpRequest.
 */
public class HostPolicyFactory implements RequestPolicyFactory {
    private final String host;

    /**
     * Create a new HostPolicyFactory object.
     * @param host The host to set on every HttpRequest.
     */
    public HostPolicyFactory(String host) {
        this.host = host;
    }

    @Override
    public HostPolicy create(RequestPolicy next, RequestPolicyOptions options) {
        return new HostPolicy(next, options);
    }

    private final class HostPolicy extends AbstractRequestPolicy {
        private HostPolicy(RequestPolicy nextPolicy, RequestPolicyOptions options) {
            super(nextPolicy, options);
        }

        @Override
        public Single<HttpResponse> sendAsync(HttpRequest request) {
            if (shouldLog(HttpPipelineLogLevel.INFO)) {
                log(HttpPipelineLogLevel.INFO, "Setting host to {0}", host);
            }

            Single<HttpResponse> result;
            final UrlBuilder urlBuilder = UrlBuilder.parse(request.url());
            try {
                request.withUrl(urlBuilder.withHost(host).toURL());
                result = nextPolicy().sendAsync(request);
            } catch (MalformedURLException e) {
                result = Single.error(e);
            }
            return result;
        }
    }
}
