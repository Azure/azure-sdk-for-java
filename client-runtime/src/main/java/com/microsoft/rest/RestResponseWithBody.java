package com.microsoft.rest;

/**
 * The REST response object that is a result of making a REST request.
 * @param <TBody> The deserialized type of the response body.
 * @param <THeaders> The deserialized type of the response headers.
 */
public class RestResponseWithBody<THeaders,TBody> extends RestResponse<THeaders> {
    private final TBody body;

    public RestResponseWithBody(int statusCode, THeaders headers, TBody body) {
        super(statusCode, headers);

        this.body = body;
    }

    /**
     * The deserialized body of the HTTP response.
     * @return The deserialized body of the HTTP response.
     */
    public TBody body() {
        return body;
    }
}
