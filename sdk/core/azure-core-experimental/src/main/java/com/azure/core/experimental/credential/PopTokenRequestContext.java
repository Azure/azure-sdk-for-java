// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.credential;

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpMethod;
import com.azure.core.util.logging.ClientLogger;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The context for a Proof of Possession token request.
 */
public final class PopTokenRequestContext extends TokenRequestContext {
    private static final ClientLogger LOGGER = new ClientLogger(PopTokenRequestContext.class);

    private final List<String> scopes = new ArrayList<String>();
    private String claims;
    private String tenantId;
    private boolean isCaeEnabled;
    private String proofOfPossessionNonce;
    private boolean isProofOfPossessionEnabled;
    private URL requestUrl;
    private HttpMethod requestMethod;

    /**
     * Creates a new instance of the PopTokenRequestContext.
     */
    public PopTokenRequestContext() {
    }

    /**
     * Adds scopes required for the token.
     * @param scopes The scopes required for the token.
     * @return The updated PopTokenRequestContext object.
     */
    public PopTokenRequestContext addScopes(String... scopes) {
        this.scopes.addAll(Arrays.asList(scopes));
        return this;
    }

    /**
     * Sets the scopes required for the token.
     * @param claims The claims required for the token.
     * @return The updated PopTokenRequestContext object.
     */
    public PopTokenRequestContext setClaims(String claims) {
        this.claims = claims;
        return this;
    }

    /**
     * Sets the scopes required for the token.
     * @param tenantId The tenant id.
     * @return The updated PopTokenRequestContext object.
     */
    public PopTokenRequestContext setTenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    /**
     * Sets the scopes required for the token.
     * @param caeEnabled Whether the client is enabled for Certificate Authority Encryption.
     * @return The updated PopTokenRequestContext object.
     */
    public PopTokenRequestContext setCaeEnabled(boolean caeEnabled) {
        isCaeEnabled = caeEnabled;
        return this;
    }

    /**
     * Sets the scopes required for the token.
     * @param proofOfPossessionNonce The proof of possession nonce.
     * @return The updated PopTokenRequestContext object.
     */
    public PopTokenRequestContext setProofOfPossessionNonce(String proofOfPossessionNonce) {
        this.proofOfPossessionNonce = proofOfPossessionNonce;
        return this;
    }

    /**
     * Gets the claims required for the token.
     * @return The claims required for the token.
     */
    public String getClaims() {
        return claims;
    }

    /**
     * Gets the tenant id.
     * @return The tenant id.
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * Gets whether the client is enabled for Certificate Authority Encryption.
     * @return Whether the client is enabled for Certificate Authority Encryption.
     */
    public boolean isCaeEnabled() {
        return isCaeEnabled;
    }

    /**
     * Gets the proof of possession nonce.
     * @return The proof of possession nonce.
     */
    public String getProofOfPossessionNonce() {
        return proofOfPossessionNonce;
    }

    /**
     * Gets the HTTP method.
     * @return The HTTP method.
     */
    public String getResourceRequestMethod() {
        return requestMethod.toString();
    }

    /**
     * Sets the HTTP method for the resource.
     *
     * @param resourceRequestMethod the HTTP method to set
     * @return The updated PopTokenRequestContext
     */
    public PopTokenRequestContext setResourceRequestMethod(HttpMethod resourceRequestMethod) {
        this.requestMethod = resourceRequestMethod;
        return this;
    }

    /**
     * Gets the Request resource URL for PoP authentication flow.
     * @return The URL.
     */
    public URL getResourceRequestUrl() {
        return requestUrl;
    }

    /**
     * Sets the Request resource URL for PoP authentication flow.
     *
     * @param resourceRequestUrl the request URL to set.
     * @return The updated PopTokenRequestContext.
     */
    public PopTokenRequestContext setResourceRequestUrl(URL resourceRequestUrl) {
        this.requestUrl = resourceRequestUrl;
        return this;
    }

    /**
     * Sets the proof of possession enabled flag.
     *
     * @param enableProofOfPossession the flag indicating whether proof of possession is enabled or not.
     * @return the current instance of TokenRequestContext.
     */
    public TokenRequestContext setProofOfPossessionEnabled(boolean enableProofOfPossession) {
        this.isProofOfPossessionEnabled = enableProofOfPossession;
        return this;
    }

    /**
     * Checks if the proof of possession is enabled.
     *
     * @return true if the proof of possession is enabled, false otherwise.
     */
    public boolean isProofOfPossessionEnabled() {
        return this.isProofOfPossessionEnabled;
    }
}
