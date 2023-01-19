// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.http.policy.HttpPipelinePolicy;

/**
 * Metadata associated with the outgoing {@link HttpRequest}.
 * <p>
 * HTTP request metadata can be consumed by {@link HttpClient HttpClients} and
 * {@link HttpPipelinePolicy HttpPipelinePolicies} to better inform their runtime behavior. For example, the HTTP
 * request metadata can indicate if the {@link HttpClient} should eagerly read the response body from the network in
 * cases where the content will be deserialized, helping prevent cases where a connection error occurs in the middle
 * of processing.
 */
public final class HttpRequestMetadata {
    private String callerMethod;
    private boolean eagerlyReadResponse;
    private boolean ignoreResponseBody;
    private boolean eagerlyConvertHeaders;

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
     * Sets the method that initiated the HTTP request.
     * <p>
     * Used in scenarios such as logging and tracing to appropriately associate logs and tracing spans to the caller
     * method.
     *
     * @param callerMethod The method that initiated the HTTP request.
     * @return The updated object to chain operations.
     */
    public HttpRequestMetadata setCallerMethod(String callerMethod) {
        this.callerMethod = callerMethod;
        return this;
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
     * Sets whether the network response should be eagerly read into memory.
     * <p>
     * Used in scenarios such as when the response is known to be converted into an object to help ensure that there are
     * no network connectivity issues while deserializing.
     *
     * @param eagerlyReadResponse Whether the network response should be eagerly read into memory.
     * @return The updated object to chain operations.
     */
    public HttpRequestMetadata setResponseEagerlyRead(boolean eagerlyReadResponse) {
        this.eagerlyReadResponse = eagerlyReadResponse;
        return this;
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
     * Sets whether the network response should be ignored.
     * <p>
     * Used in scenarios such as HEAD requests or when the returned type is {@link Void} so that the {@link HttpClient}
     * knows to close the connection eagerly.
     *
     * @param ignoreResponseBody Whether the network response should be ignored.
     * @return The updated object to chain operations.
     */
    public HttpRequestMetadata setResponseBodyIgnored(boolean ignoreResponseBody) {
        this.ignoreResponseBody = ignoreResponseBody;
        return this;
    }

    /**
     * Whether the HTTP header typed used by the {@link HttpClient} implementation should be eagerly converted into
     * {@link HttpHeaders Azure Core HttpHeaders}.
     * <p>
     * Used in scenarios such as when the HTTP headers will be used to created strongly typed HTTP header objects or
     * when it's known there will be many HTTP header lookups.
     *
     * @return Whether HTTP headers should be eagerly converted into {@link HttpHeaders}.
     */
    public boolean isHeadersEagerlyConverted() {
        return eagerlyConvertHeaders;
    }

    /**
     * Sets whether the HTTP header typed used by the {@link HttpClient} implementation should be eagerly converted into
     * {@link HttpHeaders Azure Core HttpHeaders}.
     * <p>
     * Used in scenarios such as when the HTTP headers will be used to created strongly typed HTTP header objects or
     * when it's known there will be many HTTP header lookups.
     *
     * @param eagerlyConvertHeaders Whether HTTP headers should be eagerly converted into {@link HttpHeaders}.
     * @return The updated object to chain operations.
     */
    public HttpRequestMetadata setHeadersEagerlyConverted(boolean eagerlyConvertHeaders) {
        this.eagerlyConvertHeaders = eagerlyConvertHeaders;
        return this;
    }

    HttpRequestMetadata copy() {
        return new HttpRequestMetadata()
            .setCallerMethod(callerMethod)
            .setResponseEagerlyRead(eagerlyReadResponse)
            .setResponseBodyIgnored(ignoreResponseBody)
            .setHeadersEagerlyConverted(eagerlyConvertHeaders);
    }
}
