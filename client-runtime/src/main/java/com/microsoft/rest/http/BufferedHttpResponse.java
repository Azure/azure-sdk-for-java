/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.http;

import com.google.common.base.Charsets;
import rx.Single;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * HTTP response which contains already-buffered body content.
 */
public final class BufferedHttpResponse extends HttpResponse {
    private final int statusCode;
    private final HttpHeaders headers;
    private final String body;

    /**
     * Creates a buffered HTTP response.
     * @param statusCode The HTTP response status code.
     * @param headers The HTTP response headers.
     * @param body The HTTP response body as a string.
     */
    public BufferedHttpResponse(int statusCode, HttpHeaders headers, String body) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
    }

    @Override
    public int statusCode() {
        return statusCode;
    }

    @Override
    public String headerValue(String headerName) {
        return headers.value(headerName);
    }

    @Override
    public HttpHeaders headers() {
        return headers;
    }

    @Override
    public Single<? extends InputStream> bodyAsInputStreamAsync() {
        return Single.just(new ByteArrayInputStream(body.getBytes(Charsets.UTF_8)));
    }

    @Override
    public Single<byte[]> bodyAsByteArrayAsync() {
        return Single.just(body.getBytes(Charsets.UTF_8));
    }

    @Override
    public Single<String> bodyAsStringAsync() {
        return Single.just(body);
    }

    @Override
    public Single<BufferedHttpResponse> buffer() {
        return Single.just(this);
    }

    /**
     * @return The buffered HTTP response body. Will not cause blocking.
     */
    public String body() {
        return body;
    }
}
