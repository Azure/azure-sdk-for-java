/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.policy;

import com.microsoft.rest.v2.http.HttpClient;

/**
 * A request policy factory which sends a request using an HTTP client.
 * Generally should be placed at the end of a {@link RequestPolicyChain}.
 */
public class SendRequestPolicyFactory implements RequestPolicy.Factory {
    private final HttpClient client;

    /**
     * Creates a {@link SendRequestPolicyFactory} with the given {@link HttpClient}.
     * @param client the HTTP client to use when sending a request.
     */
    public SendRequestPolicyFactory(HttpClient client) {
        this.client = client;
    }

    @Override
    public RequestPolicy create(RequestPolicy next) {
        return client;
    }
}
