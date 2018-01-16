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
 * Creates a RequestPolicy that adds the provided protocol/scheme to each HttpRequest.
 */
public class ProtocolPolicyFactory implements RequestPolicyFactory {
    private final String protocol;
    private final boolean overwrite;

    /**
     * Create a new ProtocolPolicyFactory object.
     * @param protocol The protocol to set on every HttpRequest.
     */
    public ProtocolPolicyFactory(String protocol) {
        this(protocol, true);
    }

    /**
     * Create a new ProtocolPolicyFactory object.
     * @param protocol The protocol to set.
     * @param overwrite Whether or not to overwrite a HttpRequest's protocol if it already has one.
     */
    public ProtocolPolicyFactory(String protocol, boolean overwrite) {
        this.protocol = protocol;
        this.overwrite = overwrite;
    }

    @Override
    public ProtocolPolicy create(RequestPolicy next, RequestPolicyOptions options) {
        return new ProtocolPolicy(next, options);
    }

    private class ProtocolPolicy extends AbstractRequestPolicy {
        ProtocolPolicy(RequestPolicy nextPolicy, RequestPolicyOptions options) {
            super(nextPolicy, options);
        }

        @Override
        public Single<HttpResponse> sendAsync(HttpRequest request) {
            final UrlBuilder urlBuilder = UrlBuilder.parse(request.url());
            if (overwrite || urlBuilder.scheme() == null) {
                if (shouldLog(HttpPipelineLogLevel.INFO)) {
                    log(HttpPipelineLogLevel.INFO, "Setting protocol to {0}", protocol);
                }
                try {
                    request.withUrl(urlBuilder.withScheme(protocol).toURL());
                } catch (MalformedURLException e) {
                    return Single.error(e);
                }
            }
            return nextPolicy().sendAsync(request);
        }
    }
}
