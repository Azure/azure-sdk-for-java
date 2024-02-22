package com.generic.core.http.models;

import com.generic.core.http.Response;
import com.generic.core.models.BinaryData;
import com.generic.core.models.Headers;

import java.io.Closeable;

public class HttpResponse implements Response<BinaryData>, Closeable {
    private final BinaryData value;
    private final Headers headers;
    private final HttpRequest request;
    private final int statusCode;

    /**
     * Creates a {@link HttpResponse}.
     *
     * @param request The request which resulted in this response.
     * @param statusCode The status code of the HTTP response.
     * @param value The deserialized value of the HTTP response.
     */
    public HttpResponse(HttpRequest request, int statusCode, BinaryData value) {
        this.request = request;
        this.statusCode = statusCode;
        this.headers = request == null ? null : request.getHeaders();
        this.value = value;
    }

    /**
     * Creates a {@link HttpResponse}.
     *
     * @param request The request which resulted in this response.
     * @param statusCode The status code of the HTTP response.
     * @param headers The headers of the HTTP response.
     * @param value The deserialized value of the HTTP response.
     */
    public HttpResponse(HttpRequest request, int statusCode, Headers headers, BinaryData value) {
        this.request = request;
        this.statusCode = statusCode;
        this.headers = headers;
        this.value = value;
    }

    /**
     * Gets the HTTP response status code.
     *
     * @return The status code of the HTTP response.
     */
    public int getStatusCode() {
        return this.statusCode;
    }

    /**
     * Gets the headers from the HTTP response.
     *
     * @return The HTTP response headers.
     */
    public Headers getHeaders() {
        return this.headers;
    }

    /**
     * Gets the HTTP request which resulted in this response.
     *
     * @return The HTTP request.
     */
    public HttpRequest getRequest() {
        return this.request;
    }

    /**
     * Gets the value of the HTTP response.
     *
     * @return The value of the HTTP response.
     */
    public BinaryData getValue() {
        return value;
    }

    /**
     * Closes the response content stream, if any.
     */
    @Override
    public void close() {
    }
}
