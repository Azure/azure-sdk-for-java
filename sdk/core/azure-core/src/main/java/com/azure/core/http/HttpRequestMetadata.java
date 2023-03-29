// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.logging.ClientLogger;

/**
 * Metadata associated with the outgoing {@link HttpRequest}.
 * <p>
 * HTTP request metadata can be consumed by {@link HttpClient HttpClients} and
 * {@link HttpPipelinePolicy HttpPipelinePolicies} to better inform their runtime behavior. For example, the HTTP
 * request metadata can indicate if the {@link HttpClient} should eagerly read the response body from the network in
 * cases where the content will be deserialized, helping prevent cases where a connection error occurs in the middle of
 * processing.
 */
public final class HttpRequestMetadata {
    private final String callerMethod;
    private final ClientLogger logger;
    private final boolean eagerlyReadResponse;
    private final boolean ignoreResponseBody;
    private int tryCount;

    /**
     * Creates a new instance of {@link HttpRequestMetadata}.
     *
     * @param callerMethod The method that initiated the HTTP request.
     * @param logger The {@link ClientLogger} associated with the request.
     * @param eagerlyReadResponse Whether the network response should be eagerly read into memory.
     * @param ignoreResponseBody Whether the network response should be ignored.
     */
    public HttpRequestMetadata(String callerMethod, ClientLogger logger, boolean eagerlyReadResponse,
        boolean ignoreResponseBody) {
        this(callerMethod, logger, eagerlyReadResponse, ignoreResponseBody, 1);
    }

    private HttpRequestMetadata(String callerMethod, ClientLogger logger, boolean eagerlyReadResponse,
        boolean ignoreResponseBody, int tryCount) {
        this.callerMethod = callerMethod;
        this.logger = logger;
        this.eagerlyReadResponse = eagerlyReadResponse;
        this.ignoreResponseBody = ignoreResponseBody;
        this.tryCount = tryCount;
    }

    /**
     * Gets the method that initiated the HTTP request.
     * <p>
     * Used in scenarios such as logging and tracing to appropriately associate logs and tracing spans to the caller
     * method.
     *
     * @return The method that initiated the HTTP request.
     */
    public String getCallerMethod() {
        return callerMethod;
    }

    /**
     * Gets the {@link ClientLogger} associated with the request.
     * <p>
     * Used in scenarios such as logging to appropriately associate logs to the caller method.
     *
     * @return The {@link ClientLogger} associated with the request.
     */
    public ClientLogger getLogger() {
        return logger;
    }

    /**
     * Whether the network response should be eagerly read into memory.
     * <p>
     * Used in scenarios such as when the response is known to be converted into an object to help ensure that there are
     * no network connectivity issues while deserializing.
     *
     * @return Whether the network response should be eagerly read into memory.
     */
    public boolean isResponseEagerlyRead() {
        return eagerlyReadResponse;
    }

    /**
     * Whether the network response should be ignored.
     * <p>
     * Used in scenarios such as HEAD requests or when the returned type is {@link Void} so that the {@link HttpClient}
     * knows to close the connection eagerly.
     *
     * @return Whether the network response should be ignored.
     */
    public boolean isResponseBodyIgnored() {
        return ignoreResponseBody;
    }

    /**
     * Gets the request try count.
     * <p>
     * Used in scenarios such as HTTP request logging where the attempt, or retry attempt, count is included in the log
     * message to further help troubleshooting.
     * <p>
     * The default value is one and the value will be incremented by one for each retry attempts. To get the retry count
     * subtract one from this value.
     *
     * @return The request try count.
     */
    public int getTryCount() {
        return tryCount;
    }

    /**
     * Increments the request try count.
     * <p>
     * Used in scenarios such as HTTP request logging where the attempt, or retry attempt, count is included in the log
     * message to further help troubleshooting.
     * <p>
     * This will increment the try count by one.
     *
     * @return The updated object to chain operations.
     */
    public HttpRequestMetadata incrementTryCount() {
        this.tryCount = this.tryCount + 1;
        return this;
    }

    HttpRequestMetadata copy() {
        return new HttpRequestMetadata(callerMethod, logger, eagerlyReadResponse, ignoreResponseBody, tryCount);
    }
}
