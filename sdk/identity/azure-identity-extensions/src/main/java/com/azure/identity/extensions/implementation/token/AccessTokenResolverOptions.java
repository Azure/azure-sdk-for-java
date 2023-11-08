// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.extensions.implementation.token;


import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.AzureAuthorityHosts;
import com.azure.identity.extensions.implementation.enums.AuthProperty;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Contains details of a request to get a token.
 */
public class AccessTokenResolverOptions {
    private static final ClientLogger LOGGER = new ClientLogger(AccessTokenResolverOptions.class);
    private static final Map<String, String> OSS_RDBMS_SCOPE_MAP = new HashMap<String, String>() {
        {
            put(AzureAuthorityHosts.AZURE_PUBLIC_CLOUD,  "https://ossrdbms-aad.database.windows.net/.default");
            put(AzureAuthorityHosts.AZURE_CHINA, "https://ossrdbms-aad.database.chinacloudapi.cn/.default");
            put(AzureAuthorityHosts.AZURE_GERMANY, "https://ossrdbms-aad.database.cloudapi.de/.default");
            put(AzureAuthorityHosts.AZURE_GOVERNMENT, "https://ossrdbms-aad.database.usgovcloudapi.net/.default");
        }
    };
    private String claims;
    private String tenantId;
    private String[] scopes;

    public AccessTokenResolverOptions() {

    }

    public AccessTokenResolverOptions(Properties properties) {
        this.tenantId = AuthProperty.TENANT_ID.get(properties);
        this.claims = AuthProperty.CLAIMS.get(properties);

        String scopeProperty = AuthProperty.SCOPES.get(properties);
        if (scopeProperty == null) {
            scopeProperty = getDefaultScope(properties);
        }
        this.scopes = scopeProperty.split(",");
    }

    private String getDefaultScope(Properties properties) {
        String ossrdbmsScope = OSS_RDBMS_SCOPE_MAP.get(AzureAuthorityHosts.AZURE_PUBLIC_CLOUD);
        String authorityHost = AuthProperty.AUTHORITY_HOST.get(properties);
        if (AzureAuthorityHosts.AZURE_PUBLIC_CLOUD.startsWith(authorityHost)) {
            ossrdbmsScope = OSS_RDBMS_SCOPE_MAP.get(AzureAuthorityHosts.AZURE_PUBLIC_CLOUD);
        } else if (AzureAuthorityHosts.AZURE_CHINA.startsWith(authorityHost)) {
            ossrdbmsScope = OSS_RDBMS_SCOPE_MAP.get(AzureAuthorityHosts.AZURE_CHINA);
        } else if (AzureAuthorityHosts.AZURE_GERMANY.startsWith(authorityHost)) {
            ossrdbmsScope = OSS_RDBMS_SCOPE_MAP.get(AzureAuthorityHosts.AZURE_GERMANY);
        } else if (AzureAuthorityHosts.AZURE_GOVERNMENT.startsWith(authorityHost)) {
            ossrdbmsScope = OSS_RDBMS_SCOPE_MAP.get(AzureAuthorityHosts.AZURE_GOVERNMENT);
        }
        LOGGER.info("Ossrdbms scope set to {}.", ossrdbmsScope);
        return ossrdbmsScope;
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
        return scopes == null ? null : scopes.clone();
    }

    public void setScopes(String[] scopes) {
        this.scopes = scopes.clone();
    }

}
