// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.jca.implementation.http.client;

import java.util.Map;

public class DelegateRestClient implements RestClient {

    private RestClient delegate;

    public DelegateRestClient(RestClient delegate) {
        this.delegate = delegate;
    }

    @Override
    public String get(String url, Map<String, String> headers) {
        return delegate.get(url, headers);
    }

    public RestClient getDelegate() {
        return delegate;
    }

    @Override
    public String post(String url, String body, String contentType) {
        return delegate.post(url, body, contentType);
    }

    public void setDelegate(RestClient delegate) {
        this.delegate = delegate;
    }
}
