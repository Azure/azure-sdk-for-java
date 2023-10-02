// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.credential;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Contains details of a request to get a token.
 */
public class TokenRequestContext {
    private final List<String> scopes;
    private String claims;
    private String tenantId;
    private boolean enableCae;

    /**
     * Creates a token request instance.
     */
    public TokenRequestContext() {
        this.scopes = new ArrayList<>();
    }

    /**
     * Gets the scopes required for the token.
     * @return the scopes required for the token
     */
    public List<String> getScopes() {
        return scopes;
    }

    /**
     * Sets the scopes required for the token.
     * @param scopes the scopes required for the token
     * @return the TokenRequestContext itself
     */
    public TokenRequestContext setScopes(List<String> scopes) {
        Objects.requireNonNull(scopes, "'scopes' cannot be null.");
        this.scopes.clear();
        this.scopes.addAll(scopes);
        return this;
    }

    /**
     * Adds one or more scopes to the request scopes.
     * @param scopes one or more scopes to add
     * @return the TokenRequestContext itself
     */
    public TokenRequestContext addScopes(String... scopes) {
        this.scopes.addAll(Arrays.asList(scopes));
        return this;
    }

   /**
     * Set the additional claims to be included in the token.
     *
     * @see <a href="https://openid.net/specs/openid-connect-core-1_0-final.html#ClaimsParameter">
     *     https://openid.net/specs/openid-connect-core-1_0-final.html#ClaimsParameter</a>
     *
     * @param claims the additional claims to be included in the token.
     * @return the updated TokenRequestContext itself
     */
    public TokenRequestContext setClaims(String claims) {
        this.claims = claims;
        return this;
    }

    /**
     * Get the additional claims to be included in the token.
     *
     * @see <a href="https://openid.net/specs/openid-connect-core-1_0-final.html#ClaimsParameter">
     *     https://openid.net/specs/openid-connect-core-1_0-final.html#ClaimsParameter</a>
     *
     * @return the additional claims to be included in the token.
     */
    public String getClaims() {
        return this.claims;
    }

    /**
     * Set the tenant id to be used for the authentication request.
     *
     * @param tenantId the tenant to be used when requesting the token.
     * @return the updated TokenRequestContext itself
     */
    public TokenRequestContext setTenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    /**
     * Get the tenant id to be used for the authentication request.
     *
     * @return the configured tenant id.
     */
    public String getTenantId() {
        return this.tenantId;
    }


    /**
     * Indicates whether to enable Continuous Access Evaluation (CAE) for the requested token.
     *
     * <p> If a resource API implements CAE and your application declares it can handle CAE, your app receives
     * CAE tokens for that resource. For this reason, if you declare your app CAE ready, your application must handle
     * the CAE claim challenge for all resource APIs that accept Microsoft Identity access tokens. If you don't handle
     * CAE responses in these API calls, your app could end up in a loop retrying an API call with a token that is
     * still in the returned lifespan of the token but has been revoked due to CAE.</p>
     *
     * @param enableCae the flag indicating whether to enable Continuous Access Evaluation (CAE) for
     * the requested token.
     * @return the updated TokenRequestContext.
     */
    public TokenRequestContext setCaeEnabled(boolean enableCae) {
        this.enableCae = enableCae;
        return this;
    }

    /**
     * Get the status indicating whether Continuous Access Evaluation (CAE) is enabled for the requested token.
     *
     * @return the flag indicating whether CAE authentication should be used or not.
     */
    public boolean isCaeEnabled() {
        return this.enableCae;
    }
}
