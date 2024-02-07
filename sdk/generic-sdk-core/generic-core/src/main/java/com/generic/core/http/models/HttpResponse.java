package com.generic.core.http.models;

import com.generic.core.models.BinaryData;
import com.generic.core.models.Headers;

import java.io.Closeable;

public class HttpResponse<T> implements Closeable {
    protected final T body;
    protected final Headers headers;
    protected final HttpRequest request;
    protected final int statusCode;
    protected BinaryData bodyBinaryData = null;

    /**
     * Creates a {@link HttpResponse}.
     *
     * @param request The request which resulted in this response.
     * @param statusCode The status code of the HTTP response.
     * @param body The deserialized value of the HTTP response.
     */
    public HttpResponse(HttpRequest request, int statusCode, T body) {
        this.request = request;
        this.statusCode = statusCode;
        this.headers = request == null ? null : request.getHeaders();
        this.body = body;
    }

    /**
     * Creates a {@link HttpResponse}.
     *
     * @param request The request which resulted in this response.
     * @param statusCode The status code of the HTTP response.
     * @param headers The headers of the HTTP response.
     * @param body The deserialized value of the HTTP response.
     */
    public HttpResponse(HttpRequest request, int statusCode, Headers headers, T body) {
        this.request = request;
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
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
    public T getBody() {
        return body;
    };

    /**
     * Gets the {@link BinaryData} that represents the body of the response.
     *
     * <p>Subclasses should override this method.</p>
     *
     * @return The {@link BinaryData} response body.
     */
    public BinaryData getBodyAsBinaryData() {
        // We shouldn't create multiple binary data instances for a single stream.
        if (bodyBinaryData == null && body != null) {
            bodyBinaryData = BinaryData.fromObject(body);
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
