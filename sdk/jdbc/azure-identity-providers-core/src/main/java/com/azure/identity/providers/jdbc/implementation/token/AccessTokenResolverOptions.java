// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.providers.jdbc.implementation.token;


import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.AzureAuthorityHosts;
import com.azure.identity.providers.jdbc.implementation.enums.AuthProperty;

import java.util.Properties;


/**
 * Contains details of a request to get a token.
 */
public class AccessTokenResolverOptions {
    private static final ClientLogger LOGGER = new ClientLogger(AccessTokenResolverOptions.class);

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
            scopeProperty = getDefaultScopes(properties);
        }
        this.scopes = scopeProperty.split(",");
    }

    private String getDefaultScopes(Properties properties) {
        if (AzureAuthorityHosts.AZURE_PUBLIC_CLOUD.startsWith(AuthProperty.AUTHORITY_HOST.get(properties))) {
            // todo zhihaoguo update log
            LOGGER.info("AZURE_PUBLIC_CLOUD ossrdbms scopes set");
            return "https://ossrdbms-aad.database.windows.net/.default";
        } else if (AzureAuthorityHosts.AZURE_CHINA.startsWith(AuthProperty.AUTHORITY_HOST.get(properties))) {
            LOGGER.info("AZURE_CHINA ossrdbms scopes set");
            return "https://ossrdbms-aad.database.chinacloudapi.cn/.default";
        } else if (AzureAuthorityHosts.AZURE_GERMANY.startsWith(AuthProperty.AUTHORITY_HOST.get(properties))) {
            LOGGER.info("AZURE_GERMANY ossrdbms scopes set");
            return "https://ossrdbms-aad.database.cloudapi.de/.default";
        } else if (AzureAuthorityHosts.AZURE_GOVERNMENT.startsWith(AuthProperty.AUTHORITY_HOST.get(properties))) {
            LOGGER.info("AZURE_GOVERNMENT ossrdbms scopes set");
            return "https://ossrdbms-aad.database.usgovcloudapi.net/.default";
        }
        LOGGER.info("default ossrdbms scopes set");
        return "https://ossrdbms-aad.database.windows.net/.default";
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
