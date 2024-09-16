package com.azure.core.credential;

import com.azure.core.http.HttpMethod;

import java.net.URL;

/**
 * Specifies Options for Pop Token authentication.
 */
public class ProofOfPossessionOptions {

    private String proofOfPossessionNonce;
    private boolean isProofOfPossessionEnabled;
    private URL requestUrl;
    private HttpMethod requestMethod;

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
     * Checks if proof of possession is enabled.
     *
     * @return A boolean indicating whether proof of possession is enabled.
     */
    public boolean isProofOfPossessionEnabled() {
        return isProofOfPossessionEnabled;
    }

    /**
     * Sets the proof of possession enabled status.
     *
     * @param proofOfPossessionEnabled A boolean indicating whether proof of possession should be enabled.
     * @return The updated instance of ProofOfPossessionOptions.
     */
    public ProofOfPossessionOptions setProofOfPossessionEnabled(boolean proofOfPossessionEnabled) {
        isProofOfPossessionEnabled = proofOfPossessionEnabled;
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
