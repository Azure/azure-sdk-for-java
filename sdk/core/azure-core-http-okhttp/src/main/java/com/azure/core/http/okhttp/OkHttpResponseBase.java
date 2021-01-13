// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.CoreUtils;
import okhttp3.Headers;
import okhttp3.Response;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;

/**
 * Base response class for OkHttp with implementations for response metadata.
 */
abstract class OkHttpResponseBase extends HttpResponse {
    private final int statusCode;
    private final HttpHeaders headers;

    OkHttpResponseBase(Response response, HttpRequest request) {
        super(request);
        this.statusCode = response.code();
        this.headers = fromOkHttpHeaders(response.headers());
    }

    @Override
    public final int getStatusCode() {
        return this.statusCode;
    }

    @Override
    public final String getHeaderValue(String name) {
        return this.headers.getValue(name);
    }

    @Override
    public final HttpHeaders getHeaders() {
        return this.headers;
    }

    @Override
    public final Mono<String> getBodyAsString() {
        return getBodyAsByteArray().map(bytes -> CoreUtils.bomAwareToString(bytes, getHeaderValue("Content-Type")));
    }

    @Override
    public final Mono<String> getBodyAsString(Charset charset) {
        return getBodyAsByteArray().map(bytes -> new String(bytes, charset));
    }

    /**
     * Creates azure-core HttpHeaders from okhttp headers.
     *
     * @param headers okhttp headers
     * @return azure-core HttpHeaders
     */
    private static HttpHeaders fromOkHttpHeaders(Headers headers) {
        HttpHeaders httpHeaders = new HttpHeaders();
        for (String headerName : headers.names()) {
            httpHeaders.put(headerName, headers.get(headerName));
        }
        return httpHeaders;
    }
}
