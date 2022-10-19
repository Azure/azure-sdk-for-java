// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.providers.jdbc.implementation.token;


import com.azure.identity.providers.jdbc.implementation.enums.AuthProperty;

import java.util.Properties;


/**
 * Contains details of a request to get a token.
 */
public class AccessTokenResolverOptions {

    private String claims;
    private String tenantId;
    private String[] scopes;

    public AccessTokenResolverOptions() {

    }

    public AccessTokenResolverOptions(Properties properties) {
        this.tenantId = AuthProperty.TENANT_ID.get(properties);
        this.claims = AuthProperty.CLAIMS.get(properties);

        String scopeProperty = AuthProperty.SCOPES.get(properties);
        this.scopes = scopeProperty == null ?  new String[0] : scopeProperty.split(",");
    }

    public String getClaims() {
        return claims;
    }

    public void setClaims(String claims) {
        this.claims = claims;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String[] getScopes() {
        return scopes.clone();
    }

    public void setScopes(String[] scopes) {
        this.scopes = scopes.clone();
    }

}
