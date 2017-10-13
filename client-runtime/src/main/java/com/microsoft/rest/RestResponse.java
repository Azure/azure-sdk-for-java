package com.microsoft.rest;

/**
 * The response object that is a result of making a REST request.
 * @param <THeaders> The deserialized type of the response headers.
 */
public class RestResponse<THeaders> {
    private final int statusCode;
    private final THeaders headers;

    /**
     * Create a new RestResponse object.
     * @param statusCode The status code of the HTTP response.
     * @param headers The deserialized headers of the HTTP response.
     */
    public RestResponse(int statusCode, THeaders headers) {
        this.statusCode = statusCode;
        this.headers = headers;
    }

    /**
     * The status code of the HTTP response.
     * @return The status code of the HTTP response.
     */
    public int statusCode() {
        return statusCode;
    }

    /**
     * The deserialized headers of the HTTP response.
     * @return The deserialized headers of the HTTP response.
     */
    public THeaders headers() {
        return headers;
    }
}
