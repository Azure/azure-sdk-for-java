// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.llc;

import com.azure.core.http.HttpRequest;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;

import java.util.function.Consumer;

public class RequestOptions {
    private final ClientLogger logger = new ClientLogger(RequestOptions.class);
    private Consumer<HttpRequest> requestCallback = request -> {};
    public Consumer<HttpRequest> getRequestCallback() {
        return this.requestCallback;
    }

    /**
     * Adds a header to the HTTP request.
     * @param header the header key
     * @param value the header value
     *
     * @return the modified RequestOptions object
     */
    public RequestOptions addHeader(String header, String value) {
        this.requestCallback = this.requestCallback.andThen(request ->
            request.getHeaders().set(header, value));
        return this;
    }

    /**
     * Adds a query parameter to the request URL.
     * @param parameterName the name of the query parameter
     * @param value the value of the query parameter
     *
     * @return the modified RequestOptions object
     */
    public RequestOptions addQueryParam(String parameterName, String value) {
        this.requestCallback = this.requestCallback.andThen(request -> {
            String url = request.getUrl().toString();
            request.setUrl(url + (url.contains("?") ? "&" : "?") + parameterName + "=" + value);
        });
        return this;
    }

    public RequestOptions setRequestCallback(Consumer<HttpRequest> requestCallback) {
        this.requestCallback = this.requestCallback.andThen(requestCallback);
        return this;
    }

    public RequestOptions setBody(BinaryData requestBody) {
        this.requestCallback = this.requestCallback.andThen(request -> {
            request.setBody(requestBody.toBytes());
        });
        return this;
    }
}
