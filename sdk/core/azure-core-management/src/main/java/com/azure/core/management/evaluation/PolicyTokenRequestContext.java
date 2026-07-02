// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.evaluation;

import com.azure.core.http.HttpMethod;
import com.azure.core.util.BinaryData;

/**
 * <p>
 * The {@link PolicyTokenRequestContext} carries the details of the resource operation that requires a policy token,
 * used when acquiring a policy token from the Azure Policy external evaluation ("Invoke") flow.
 * </p>
 *
 * <p>
 * When a resource operation is disallowed by policy because an external evaluation policy token is missing, the SDK
 * captures the original operation's request URI, HTTP method and payload, together with the subscription that the
 * operation targets, and passes them to a {@link PolicyTokenCredential} implementation to acquire a
 * {@link PolicyToken}.
 * </p>
 *
 * <p>
 * The {@code uri}, {@code httpMethod} and {@code content} correspond to the {@code PolicyTokenOperation} accepted by
 * the {@code acquirePolicyToken} REST operation. The {@code content} must be a byte-for-byte copy of the original
 * operation's request body so that the service can validate it.
 * </p>
 *
 * @see PolicyToken
 * @see PolicyTokenCredential
 */
public final class PolicyTokenRequestContext {
    private String uri;
    private HttpMethod httpMethod;
    private BinaryData content;
    private String subscriptionId;

    /**
     * Creates a policy token request context instance.
     */
    public PolicyTokenRequestContext() {
    }

    /**
     * Gets the request URI of the resource operation that requires a policy token.
     *
     * @return the request URI of the resource operation.
     */
    public String getUri() {
        return uri;
    }

    /**
     * Sets the request URI of the resource operation that requires a policy token.
     *
     * @param uri the request URI of the resource operation.
     * @return the updated {@link PolicyTokenRequestContext} itself.
     */
    public PolicyTokenRequestContext setUri(String uri) {
        this.uri = uri;
        return this;
    }

    /**
     * Gets the HTTP method of the resource operation that requires a policy token.
     *
     * @return the HTTP method of the resource operation.
     */
    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    /**
     * Sets the HTTP method of the resource operation that requires a policy token.
     *
     * @param httpMethod the HTTP method of the resource operation.
     * @return the updated {@link PolicyTokenRequestContext} itself.
     */
    public PolicyTokenRequestContext setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
        return this;
    }

    /**
     * Gets the payload of the resource operation that requires a policy token.
     * <p>
     * The content is expected to be a byte-for-byte copy of the original operation's request body.
     *
     * @return the payload of the resource operation, may be {@code null} for operations without a body.
     */
    public BinaryData getContent() {
        return content;
    }

    /**
     * Sets the payload of the resource operation that requires a policy token.
     * <p>
     * The content is expected to be a byte-for-byte copy of the original operation's request body.
     *
     * @param content the payload of the resource operation.
     * @return the updated {@link PolicyTokenRequestContext} itself.
     */
    public PolicyTokenRequestContext setContent(BinaryData content) {
        this.content = content;
        return this;
    }

    /**
     * Gets the subscription ID that the resource operation targets.
     * <p>
     * The subscription ID is used to build the {@code acquirePolicyToken} request URL.
     *
     * @return the subscription ID that the resource operation targets.
     */
    public String getSubscriptionId() {
        return subscriptionId;
    }

    /**
     * Sets the subscription ID that the resource operation targets.
     * <p>
     * The subscription ID is used to build the {@code acquirePolicyToken} request URL.
     *
     * @param subscriptionId the subscription ID that the resource operation targets.
     * @return the updated {@link PolicyTokenRequestContext} itself.
     */
    public PolicyTokenRequestContext setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
        return this;
    }
}
