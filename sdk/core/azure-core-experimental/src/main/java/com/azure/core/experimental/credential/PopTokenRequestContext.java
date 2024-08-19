// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.credential;

import com.azure.core.http.HttpRequest;
import com.azure.core.util.logging.ClientLogger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * The context for a Proof of Possession token request.
 */
public final class PopTokenRequestContext {
    private static final ClientLogger LOGGER = new ClientLogger(PopTokenRequestContext.class);

    private final List<String> scopes = new ArrayList();
    private String parentRequestId;
    private String claims;
    private String tenantId;
    private boolean isCaeEnabled;
    private String proofOfPossessionNonce;
    private HttpRequest request;



    /**
     * Creates a new instance of the PopTokenRequestContext.
     * @param scopes The scopes required for the token.
     * @param parentRequestId The parent request id.
     * @param claims The claims required for the token.
     * @param tenantId The tenant id.
     * @param isCaeEnabled Whether the client is enabled for Certificate Authority Encryption.
     * @param proofOfPossessionNonce The proof of possession nonce.
     * @param request The HTTP request.
     */
    public PopTokenRequestContext(List<String> scopes, String parentRequestId, String claims, String tenantId,
                                  boolean isCaeEnabled, String proofOfPossessionNonce, HttpRequest request) {
        Objects.requireNonNull(scopes, "'scopes' cannot be null.");
        this.scopes.clear();
        this.scopes.addAll(scopes);
        this.parentRequestId = parentRequestId;
        this.claims = claims;
        this.tenantId = tenantId;
        this.isCaeEnabled = isCaeEnabled;
        this.proofOfPossessionNonce = proofOfPossessionNonce;
        this.request = request;
    }

    /**
     * Creates a new instance of the PopTokenRequestContext.
     * @param scopes The scopes required for the token.
     */
    public PopTokenRequestContext(List<String> scopes) {
        Objects.requireNonNull(scopes, "'scopes' cannot be null.");
        this.scopes.clear();
        this.scopes.addAll(scopes);
    }


    /**
     * Creates a new instance of the PopTokenRequestContext.
     * @return A new instance of the PopTokenRequestContext.
     */
    public TokenRequestContext toTokenRequestContext() {
        return new TokenRequestContext(scopes)
            .setCaeEnabled(isCaeEnabled)
            .setClaims(claims)
            .setTenantId(tenantId)
            .setParentRequestId(parentRequestId);
    }

    /**
     * Creates a new instance of the PopTokenRequestContext from a TokenRequestContext.
     * @param context The TokenRequestContext.
     * @param request The HTTP request.
     * @return A new instance of the PopTokenRequestContext.
     */
    public static PopTokenRequestContext fromTokenRequestContext(TokenRequestContext context, HttpRequest request) {
        return new PopTokenRequestContext(context.getScopes())
            .setParentRequestId(context.getParentRequestId())
            .setClaims(context.getClaims())
            .setTenantId(context.getTenantId())
            .setCaeEnabled(context.isCaeEnabled());
    }

    /**
     * Creates a new instance of the PopTokenRequestContext from a TokenRequestContext.
     * @param context The TokenRequestContext.
     * @return A new instance of the PopTokenRequestContext.
     */
    public static TokenRequestContext toTokenRequestContext(PopTokenRequestContext context) {
        return context.toTokenRequestContext();
    }

    /**
     * Sets the scopes required for the token.
     * @param parentRequestId The parent request id.
     * @return The updated PopTokenRequestContext object.
     */
    public PopTokenRequestContext setParentRequestId(String parentRequestId) {
        this.parentRequestId = parentRequestId;
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
     * Sets the scopes required for the token.
     * @param request The HTTP request.
     * @return The updated PopTokenRequestContext object.
     */
    public PopTokenRequestContext setRequest(HttpRequest request) {
        this.request = request;
        return this;
    }

    /**
     * Gets the scopes required for the token.
     * @return The scopes required for the token.
     */
    public List<String> getScopes() {
        return scopes;
    }

    /**
     * Gets the parent request id.
     * @return The parent request id.
     */
    public String getParentRequestId() {
        return parentRequestId;
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
     * Gets the HTTP request.
     * @return The HTTP request.
     */
    public HttpRequest getRequest() {
        return request;
    }

    /**
     * Gets the HTTP method.
     * @return The HTTP method.
     */
    public String getHttpMethod() {
        return request != null ? request.getHttpMethod().toString() : null;
    }

    /**
     * Gets the URI.
     * @return The URI.
     */
    public URI getUri() {
        try {
            return request != null ? request.getUrl().toURI() : null;
        } catch (URISyntaxException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }
}

/**
 * The context for a token request.
 */
class TokenRequestContext {
    private final List<String> scopes = new ArrayList();
    private String parentRequestId;
    private String claims;
    private String tenantId;
    private boolean isCaeEnabled;

    TokenRequestContext(List<String> scopes) {
        this.scopes.addAll(scopes);
    }

    public List<String> getScopes() {
        return scopes;
    }

    public String getParentRequestId() {
        return parentRequestId;
    }

    public String getClaims() {
        return claims;
    }

    public String getTenantId() {
        return tenantId;
    }

    public boolean isCaeEnabled() {
        return isCaeEnabled;
    }

    public TokenRequestContext addScopes(String... scopes) {
        this.scopes.addAll(Arrays.asList(scopes));
        return this;
    }

    public TokenRequestContext setClaims(String claims) {
        this.claims = claims;
        return this;
    }
    public TokenRequestContext setTenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    public TokenRequestContext setCaeEnabled(boolean enableCae) {
        this.isCaeEnabled = enableCae;
        return this;
    }

    public TokenRequestContext setParentRequestId(String parentRequestId) {
        this.parentRequestId = parentRequestId;
        return this;
    }
}

