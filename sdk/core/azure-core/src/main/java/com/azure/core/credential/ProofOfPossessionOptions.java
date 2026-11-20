// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.credential;

import com.azure.core.http.HttpMethod;

import java.net.URL;

/**
 * <p>
 * Specifies options for proof-of-possession (PoP) token authentication.
 * </p>
 *
 * <p>
 * Setting these options on a {@link TokenRequestContext} asks the credential for a PoP token instead of a bearer
 * token. Each credential decides which kind of PoP token it returns:
 * </p>
 *
 * <ul>
 * <li>A token signed over the request, for example from a broker. It's only valid for the request URL, method and
 * nonce set in these options.</li>
 * <li>A token bound to a TLS client certificate (mutual TLS, or mTLS, proof-of-possession), for example for a managed
 * identity. It's only valid over a TLS connection that presents that certificate, which the {@link AccessToken}
 * carries in {@link AccessToken#getClientCertificateBinding()}. It isn't tied to the request URL, method or
 * nonce.</li>
 * </ul>
 *
 * <p>
 * A credential that can't return a PoP token may return a bearer token instead.
 * </p>
 */
public class ProofOfPossessionOptions {

    private String proofOfPossessionNonce;
    private URL requestUrl;
    private HttpMethod requestMethod;

    /**
     * Creates a new instance of {@link ProofOfPossessionOptions}.
     */
    public ProofOfPossessionOptions() {
    }

    /**
     * Gets the proof of possession nonce.
     *
     * @return A string representing the proof of possession nonce.
     */
    public String getProofOfPossessionNonce() {
        return proofOfPossessionNonce;
    }

    /**
     * Sets the proof of possession nonce.
     *
     * @param proofOfPossessionNonce A string representing the proof of possession nonce.
     * @return The updated instance of ProofOfPossessionOptions.
     */
    public ProofOfPossessionOptions setProofOfPossessionNonce(String proofOfPossessionNonce) {
        this.proofOfPossessionNonce = proofOfPossessionNonce;
        return this;
    }

    /**
     * Gets the request URL.
     *
     * @return A URL representing the request URL.
     */
    public URL getRequestUrl() {
        return requestUrl;
    }

    /**
     * Sets the request URL.
     *
     * @param requestUrl A URL representing the request URL.
     * @return The updated instance of ProofOfPossessionOptions.
     */
    public ProofOfPossessionOptions setRequestUrl(URL requestUrl) {
        this.requestUrl = requestUrl;
        return this;
    }

    /**
     * Gets the request method.
     *
     * @return An HttpMethod representing the request method.
     */
    public HttpMethod getRequestMethod() {
        return requestMethod;
    }

    /**
     * Sets the request method.
     *
     * @param requestMethod An HttpMethod representing the request method.
     * @return The updated instance of ProofOfPossessionOptions.
     */
    public ProofOfPossessionOptions setRequestMethod(HttpMethod requestMethod) {
        this.requestMethod = requestMethod;
        return this;
    }
}
