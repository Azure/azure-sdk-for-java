// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.identity.api.token;

import com.azure.core.util.Configuration;
import com.azure.spring.cloud.service.implementation.identity.api.AuthProperty;

import java.util.Properties;


/**
 * This is better because it can construct using {@link Configuration}
 */
public class AccessTokenResolverOptions {

    private String claims;
    private String tenantId;
    private String[] scopes;
    private boolean cacheAccessToken;

    public AccessTokenResolverOptions() {

    }

    public AccessTokenResolverOptions(Properties properties) {
        this.tenantId = AuthProperty.TENANT_ID.get(properties);
        this.claims = AuthProperty.CLAIMS.get(properties);

        String scopeProperty = AuthProperty.SCOPES.get(properties);
        this.scopes = scopeProperty == null ?  new String[0] : scopeProperty.split(",");
        this.cacheAccessToken = Boolean.TRUE.equals(AuthProperty.CACHE_ENABLED.getBoolean(properties));
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
        return scopes;
    }

    public void setScopes(String[] scopes) {
        this.scopes = scopes;
    }

    public boolean isCacheAccessToken() {
        return cacheAccessToken;
    }

    public void setCacheAccessToken(boolean cacheAccessToken) {
        this.cacheAccessToken = cacheAccessToken;
    }

}
