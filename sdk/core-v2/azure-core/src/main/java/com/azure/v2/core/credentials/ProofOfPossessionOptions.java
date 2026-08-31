// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.credentials;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;
import io.clientcore.core.http.models.HttpMethod;

import java.net.URI;
import java.util.Objects;

/**
 * Options used to request a Proof-of-Possession access token bound to an HTTP request.
 */
@Metadata(properties = MetadataProperties.FLUENT)
public final class ProofOfPossessionOptions {
    private String proofOfPossessionNonce;
    private URI requestUrl;
    private HttpMethod requestMethod;

    /**
     * Creates an instance of {@link ProofOfPossessionOptions}.
     */
    public ProofOfPossessionOptions() {
    }

    /**
     * Gets the nonce supplied by a Proof-of-Possession authentication challenge.
     *
     * @return The challenge nonce.
     */
    public String getProofOfPossessionNonce() {
        return proofOfPossessionNonce;
    }

    /**
     * Sets the nonce supplied by a Proof-of-Possession authentication challenge.
     *
     * @param proofOfPossessionNonce The challenge nonce.
     * @return The updated options.
     */
    public ProofOfPossessionOptions setProofOfPossessionNonce(String proofOfPossessionNonce) {
        this.proofOfPossessionNonce = proofOfPossessionNonce;
        return this;
    }

    /**
     * Gets the URI of the request the token is bound to.
     *
     * @return The request URI.
     */
    public URI getRequestUrl() {
        return requestUrl;
    }

    /**
     * Sets the URI of the request the token is bound to.
     *
     * @param requestUrl The request URI.
     * @return The updated options.
     */
    public ProofOfPossessionOptions setRequestUrl(URI requestUrl) {
        this.requestUrl = requestUrl;
        return this;
    }

    /**
     * Gets the HTTP method of the request the token is bound to.
     *
     * @return The request method.
     */
    public HttpMethod getRequestMethod() {
        return requestMethod;
    }

    /**
     * Sets the HTTP method of the request the token is bound to.
     *
     * @param requestMethod The request method.
     * @return The updated options.
     */
    public ProofOfPossessionOptions setRequestMethod(HttpMethod requestMethod) {
        this.requestMethod = requestMethod;
        return this;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ProofOfPossessionOptions)) {
            return false;
        }
        ProofOfPossessionOptions other = (ProofOfPossessionOptions) object;
        return Objects.equals(proofOfPossessionNonce, other.proofOfPossessionNonce)
            && Objects.equals(requestUrl, other.requestUrl)
            && requestMethod == other.requestMethod;
    }

    @Override
    public int hashCode() {
        return Objects.hash(proofOfPossessionNonce, requestUrl, requestMethod);
    }
}
