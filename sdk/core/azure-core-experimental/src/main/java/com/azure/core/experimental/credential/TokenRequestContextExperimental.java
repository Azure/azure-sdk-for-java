// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.credential;

import com.azure.core.credential.TokenRequestContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Contains details of a request to get a token.
 */
public class TokenRequestContextExperimental extends TokenRequestContext {
    private String tenantId;

    /**
     * Creates a token request instance.
     */
    public TokenRequestContextExperimental() {
        super();
    }

    /**
     * Set the tenant to be used in the authentication request.
     *
     * @param tenantId the tenant to be used when retrieving the token.
     * @return the updated TokenRequestContext itself
     */
    public TokenRequestContext setTenantId(String tenantId) {
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


    @Override
    public TokenRequestContextExperimental setClaims(String claims) {
        super.setClaims(claims);
        return this;
    }

    @Override
    public TokenRequestContextExperimental setScopes(List<String> scopes) {
        super.setScopes(scopes);
        return this;
    }
}
