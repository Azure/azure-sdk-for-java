// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.credential;

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpRequest;
import com.azure.core.util.logging.ClientLogger;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
    private HttpRequest request;
    private boolean isProofOfPossessionEnabled;

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

    //    /**
    //     * Sets the scopes required for the token.
    //     * @param scopes The scopes required for the token.
    //     * @return The updated PopTokenRequestContext object.
    //     */
    //    public PopTokenRequestContext setScopes(List<String> scopes) {
    //        Objects.requireNonNull(scopes, "'scopes' cannot be null.");
    //        this.scopes.clear();
    //        this.scopes.addAll(scopes);
    //        return this;
    //    }

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
     * Sets the scopes required for the token.
     * @param request The HTTP request.
     * @return The updated PopTokenRequestContext object.
     */
    public PopTokenRequestContext setRequest(HttpRequest request) {
        this.request = request;
        return this;
    }

    //    /**
    //     * Gets the scopes required for the token.
    //     * @return The scopes required for the token.
    //     */
    //    public List<String> getScopes() {
    //        return scopes;
    //    }

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
    public String getResourceMethod() {
        return request != null ? request.getHttpMethod().toString() : null;
    }

    /**
     * Gets the Request resource URL for PoP authentication flow.
     * @return The URL.
     */
    public URL getRequestResourceUrl() {
        return request != null ? request.getUrl() : null;
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
