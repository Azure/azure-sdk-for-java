// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.passwordless;

import com.azure.spring.cloud.core.properties.PasswordlessProperties;
import com.azure.spring.cloud.core.properties.authentication.TokenCredentialProperties;
import com.azure.spring.cloud.core.properties.profile.AzureProfileProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration properties for passwordless connections with Azure Database.
 */
public class AzureJdbcPasswordlessProperties implements PasswordlessProperties {

    private static final String JDBC_SCOPE_AZURE = "https://ossrdbms-aad.database.windows.net/.default";
    private static final String JDBC_SCOPE_AZURE_CHINA = "https://ossrdbms-aad.database.chinacloudapi.cn/.default";
    private static final String JDBC_SCOPE_AZURE_GERMANY = "https://ossrdbms-aad.database.cloudapi.de/.default";
    private static final String JDBC_SCOPE_AZURE_US_GOVERNMENT = "https://ossrdbms-aad.database.usgovcloudapi.net/.default";

    private static final Map<CloudType, String> JDBC_SCOPE_MAP = new HashMap<CloudType, String>() {
        {
            put(CloudType.AZURE, JDBC_SCOPE_AZURE);
            put(CloudType.AZURE_CHINA, JDBC_SCOPE_AZURE_CHINA);
            put(CloudType.AZURE_GERMANY, JDBC_SCOPE_AZURE_GERMANY);
            put(CloudType.AZURE_US_GOVERNMENT, JDBC_SCOPE_AZURE_US_GOVERNMENT);
        }
    };

    private boolean passwordlessEnabled = false;

    private AzureProfileProperties profile = new AzureProfileProperties();

    private String scopes;

    private TokenCredentialProperties credential = new TokenCredentialProperties();

    @Override
    public String getScopes() {
        return this.scopes == null ? getScopesFromMap() : this.scopes;
    }

    public void setScopes(String scopes) {
        this.scopes = scopes;
    }

    @Override
    public boolean isPasswordlessEnabled() {
        return this.passwordlessEnabled;
    }

    @Override
    public void setPasswordlessEnabled(boolean passwordlessEnabled) {
        this.passwordlessEnabled = passwordlessEnabled;
    }

    private String getScopesFromMap() {
        return JDBC_SCOPE_MAP.getOrDefault(getProfile().getCloudType(), JDBC_SCOPE_AZURE);
    }

    @Override
    public AzureProfileProperties getProfile() {
        return profile;
    }

    public void setProfile(AzureProfileProperties profile) {
        this.profile = profile;
    }

    @Override
    public TokenCredentialProperties getCredential() {
        return credential;
    }

    public void setCredential(TokenCredentialProperties credential) {
        this.credential = credential;
    }
}
