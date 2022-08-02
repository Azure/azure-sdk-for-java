package com.azure.spring.cloud.service.implementation.identity.api.token;

import com.azure.core.util.Configuration;
import com.azure.spring.cloud.service.implementation.identity.api.AuthProperty;

import java.util.Arrays;
import java.util.Objects;

/**
 * TODO use this or use {@link com.azure.core.credential.TokenRequestContext} instead
 * this is better because it can construct using {@link Configuration}
 */
public class AccessTokenResolverOptions {

    private String claims;
    private String tenantId;
    private String[] scopes;
    private boolean cacheAccessToken;

    public AccessTokenResolverOptions() {

    }

    public AccessTokenResolverOptions(Configuration configuration) {
        this.tenantId = AuthProperty.TENANT_ID.get(configuration);
        this.claims = AuthProperty.CLAIMS.get(configuration);

        String scopeProperty = AuthProperty.SCOPES.get(configuration);
        this.scopes = scopeProperty == null ?  new String[0] : scopeProperty.split(",");
        this.cacheAccessToken = Boolean.TRUE.equals(AuthProperty.CACHE_ENABLED.getBoolean(configuration));
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AccessTokenResolverOptions)) return false;
        AccessTokenResolverOptions that = (AccessTokenResolverOptions) o;
        return cacheAccessToken == that.cacheAccessToken && Objects.equals(claims, that.claims) && Objects.equals(tenantId, that.tenantId) && Arrays.equals(scopes, that.scopes);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(claims, tenantId, cacheAccessToken);
        result = 31 * result + Arrays.hashCode(scopes);
        return result;
    }
}
