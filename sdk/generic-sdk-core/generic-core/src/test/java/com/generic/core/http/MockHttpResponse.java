// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http;

import com.generic.core.http.models.HttpHeaderName;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.HttpResponse;
import com.generic.core.implementation.http.serializer.DefaultJsonSerializer;
import com.generic.core.models.BinaryData;
import com.generic.core.models.Headers;
import com.generic.core.util.serializer.ObjectSerializer;

import java.io.ByteArrayOutputStream;

import static com.generic.core.CoreTestUtils.cloneByteArray;

public class MockHttpResponse extends HttpResponse {
    private static final ObjectSerializer SERIALIZER = new DefaultJsonSerializer();

    private final int statusCode;

    private final Headers headers;
    private final byte[] bodyBytes;

    /**
     * Creates a HTTP response associated with a {@code request}, returns the {@code statusCode}, and has an empty
     * response body.
     *
     * @param request HttpRequest associated with the response.
     * @param statusCode Status code of the response.
     */
    public MockHttpResponse(HttpRequest request, int statusCode) {
        this(request, statusCode, new byte[0]);
    }

    /**
     * Creates an HTTP response associated with a {@code request}, returns the {@code statusCode}, and response body of
     * {@code bodyBytes}.
     *
     * @param request HttpRequest associated with the response.
     * @param statusCode Status code of the response.
     * @param bodyBytes Contents of the response.
     */
    public MockHttpResponse(HttpRequest request, int statusCode, byte[] bodyBytes) {
        this(request, statusCode, new Headers(), bodyBytes);
    }

    /**
     * Creates an HTTP response associated with a {@code request}, returns the {@code statusCode}, and http headers.
     *
     * @param request HttpRequest associated with the response.
     * @param statusCode Status code of the response.
     * @param headers Headers of the response.
     */
    public MockHttpResponse(HttpRequest request, int statusCode, Headers headers) {
        this(request, statusCode, headers, new byte[0]);
    }

    /**
     * Creates an HTTP response associated with a {@code request}, returns the {@code statusCode}, contains the
     * {@code headers}, and response body of {@code bodyBytes}.
     *
     * @param request HttpRequest associated with the response.
     * @param statusCode Status code of the response.
     * @param headers HttpHeaders of the response.
     * @param bodyBytes Contents of the response.
     */
    public MockHttpResponse(HttpRequest request, int statusCode, Headers headers, byte[] bodyBytes) {
        super(request, cloneByteArray(bodyBytes));

        this.statusCode = statusCode;
        this.headers = headers;
        this.bodyBytes = bodyBytes;
    }

    /**
     * Creates an HTTP response associated with a {@code request}, returns the {@code statusCode}, contains the given
     * {@code headers}, and response body that is JSON serialized from {@code serializable}.
     *
     * @param request HttpRequest associated with the response.
     * @param headers HttpHeaders of the response.
     * @param statusCode Status code of the response.
     * @param serializable Contents to be serialized into JSON for the response.
     */
    public MockHttpResponse(HttpRequest request, int statusCode, Headers headers, Object serializable) {
        this(request, statusCode, headers, serialize(serializable));
    }

    /**
     * Creates an HTTP response associated with a {@code request}, returns the {@code statusCode}, and response body
     * that is JSON serialized from {@code serializable}.
     *
     * @param request HttpRequest associated with the response.
     * @param statusCode Status code of the response.
     * @param serializable Contents to be serialized into JSON for the response.
     */
    public MockHttpResponse(HttpRequest request, int statusCode, Object serializable) {
        this(request, statusCode, new Headers(), serialize(serializable));
    }

    private static byte[] serialize(Object serializable) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        SERIALIZER.serialize(stream, serializable);

        return stream.toByteArray();
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String getHeaderValue(HttpHeaderName headerName) {
        return headers.getValue(headerName);
    }

    @Override
    public Headers getHeaders() {
        return this.headers;
    }

    @Override
    public BinaryData getBody() {
        return BinaryData.fromBytes(bodyBytes);
    }
}
