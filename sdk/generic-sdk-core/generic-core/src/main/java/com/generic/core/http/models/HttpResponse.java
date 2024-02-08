package com.generic.core.http.models;

import com.generic.core.http.Response;
import com.generic.core.models.BinaryData;
import com.generic.core.models.Headers;

import java.io.Closeable;

public class HttpResponse<T> implements Response<T>, Closeable {
    protected final T value;
    protected final Headers headers;
    protected final HttpRequest request;
    protected final int statusCode;
    protected BinaryData bodyBinaryData = null;

    /**
     * Creates a {@link HttpResponse}.
     *
     * @param request The request which resulted in this response.
     * @param statusCode The status code of the HTTP response.
     * @param value The deserialized value of the HTTP response.
     */
    public HttpResponse(HttpRequest request, int statusCode, T value) {
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
    public HttpResponse(HttpRequest request, int statusCode, Headers headers, T value) {
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
    };

    /**
     * Gets the headers from the HTTP response.
     *
     * @return The HTTP response headers.
     */
    public Headers getHeaders() {
        return this.headers;
    };

    /**
     * Gets the HTTP request which resulted in this response.
     *
     * @return The HTTP request.
     */
    public HttpRequest getRequest() {
        return this.request;
    }

    /**
     * Gets the deserialized value of the HTTP response.
     *
     * @return The deserialized value of the HTTP response.
     */
    public T getValue() {
        return value;
    };

    /**
     * Gets the {@link BinaryData} that represents the body of the response.
     *
     * <p>Subclasses should override this method.</p>
     *
     * @return The {@link BinaryData} response body.
     */
    public BinaryData getBody() {
        // We shouldn't create multiple binary data instances for a single stream.
        if (bodyBinaryData == null && value != null) {
            bodyBinaryData = BinaryData.fromObject(value);
        }

        return bodyBinaryData;
    }

    /**
     * Closes the response content stream, if any.
     */
    @Override
    public void close() {
    }
}
