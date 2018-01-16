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
 * Creates a RequestPolicy that adds the provided port to each HttpRequest.
 */
public class PortPolicyFactory implements RequestPolicyFactory {
    private final int port;
    private final boolean overwrite;

    /**
     * Create a new PortPolicyFactory object.
     * @param port The port to set on every HttpRequest.
     */
    public PortPolicyFactory(int port) {
        this(port, true);
    }

    /**
     * Create a new PortPolicyFactory object.
     * @param port The port to set.
     * @param overwrite Whether or not to overwrite a HttpRequest's port if it already has one.
     */
    public PortPolicyFactory(int port, boolean overwrite) {
        this.port = port;
        this.overwrite = overwrite;
    }

    @Override
    public RequestPolicy create(RequestPolicy next, RequestPolicyOptions options) {
        return new PortPolicy(next, options);
    }

    private class PortPolicy extends AbstractRequestPolicy {
        PortPolicy(RequestPolicy nextPolicy, RequestPolicyOptions options) {
            super(nextPolicy, options);
        }

        @Override
        public Single<HttpResponse> sendAsync(HttpRequest request) {
            final UrlBuilder urlBuilder = UrlBuilder.parse(request.url());
            if (overwrite || urlBuilder.port() == null) {
                if (shouldLog(HttpPipelineLogLevel.INFO)) {
                    log(HttpPipelineLogLevel.INFO, "Changing port to {0}", port);
                }
                try {
                    request.withUrl(urlBuilder.withPort(port).toURL());
                } catch (MalformedURLException e) {
                    return Single.error(e);
                }
            }
            return nextPolicy().sendAsync(request);
        }
    }

}