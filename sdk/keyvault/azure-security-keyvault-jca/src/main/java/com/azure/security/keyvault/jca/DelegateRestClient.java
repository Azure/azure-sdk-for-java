// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import java.util.Map;

/**
 * A RestClient that delegates to another RestClient.
 */
class DelegateRestClient implements RestClient {

    /**
     * Stores the delegate.
     */
    private RestClient delegate;

    /**
     * Constructor.
     */
    public DelegateRestClient(RestClient delegate) {
        this.delegate = delegate;
    }

    @Override
    public String get(String url, Map<String, String> headers) {
        return delegate.get(url, headers);
    }

    /**
     * Get the delegate.
     *
     * @return the delegate.
     */
    public RestClient getDelegate() {
        return delegate;
    }

    @Override
    public String post(String url, String body, String contentType) {
        return delegate.post(url, body, contentType);
    }

    /**
     * Set the delegate.
     *
     * @param delegate the delegate.
     */
    public void setDelegate(RestClient delegate) {
        this.delegate = delegate;
    }
}
