// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.credential;

import com.azure.core.credential.TokenRequestContext;


import java.util.Arrays;
import java.util.List;

/**
 * Contains details of a request to get a token.
 */
public class TokenRequestContextExperimental extends TokenRequestContext {
    private String tenantId;

    /**
     * Creates a token request experimental instance.
     */
    public TokenRequestContextExperimental() {
        super();
    }

    /**
     * Set the tenant to be used in the authentication request.
     *
     * @param tenantId the tenant to be used when retrieving the token.
     * @return the updated TokenRequestContextExperimental itself
     */
    public TokenRequestContextExperimental setTenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    /**
     * Get the tenant to be used in the authentication request.
     *
     * @return the configured tenant id.
     */
    public String getTenantId() {
        return this.tenantId;
    }

    /**
     * Set the additional claims to be included in the token.
     *
     * @see <a href="https://openid.net/specs/openid-connect-core-1_0-final.html#ClaimsParameter">
     *     https://openid.net/specs/openid-connect-core-1_0-final.html#ClaimsParameter</a>
     *
     * @param claims the additional claims to be included in the token.
     * @return the updated TokenRequestContextExperimental itself
     */
    @Override
    public TokenRequestContextExperimental setClaims(String claims) {
        super.setClaims(claims);
        return this;
    }

    /**
     * Sets the scopes required for the token.
     * @param scopes the scopes required for the token
     * @return the TokenRequestContextExperimental itself
     */
    @Override
    public TokenRequestContextExperimental setScopes(List<String> scopes) {
        super.setScopes(scopes);
        return this;
    }

    /**
     * Adds one or more scopes to the request scopes.
     * @param scopes one or more scopes to add
     * @return the TokenRequestContextExperimental itself
     */
    public TokenRequestContextExperimental addScopes(String... scopes) {
        super.addScopes(scopes);
        return this;
    }
}
