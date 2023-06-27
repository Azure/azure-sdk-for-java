// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.passwordless.properties;

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
    private static final String JDBC_SCOPE_AZURE_US_GOVERNMENT = "https://ossrdbms-aad.database.usgovcloudapi.net/.default";

    private static final Map<CloudType, String> JDBC_SCOPE_MAP = new HashMap<CloudType, String>() {
        {
            put(CloudType.AZURE, JDBC_SCOPE_AZURE);
            put(CloudType.AZURE_CHINA, JDBC_SCOPE_AZURE_CHINA);
            put(CloudType.AZURE_US_GOVERNMENT, JDBC_SCOPE_AZURE_US_GOVERNMENT);
        }
    };

    /**
     * Whether to enable supporting azure identity token credentials, by default is false.
     *
     * If the passwordlessEnabled is true, but the 'spring.datasource.password' property is not empty, it will still use username/password to authenticate connections.
     * To use passwordless connections, you need to remove 'spring.datasource.password' property.
     */
    private boolean passwordlessEnabled = false;

    private AzureProfileProperties profile = new AzureProfileProperties();

    private String scopes;

    private TokenCredentialProperties credential = new TokenCredentialProperties();

    /**
     * Get the scopes required for the access token.
     *
     * @return scopes required for the access token
     */
    @Override
    public String getScopes() {
        return this.scopes == null ? getDefaultScopes() : this.scopes;
    }

    /**
     * Set the scopes required for the access token.
     *
     * @param scopes the scopes required for the access token
     */
    public void setScopes(String scopes) {
        this.scopes = scopes;
    }

    /**
     * Whether to enable connections authenticating with Azure AD, default is false.
     *
     * @return enable connections authenticating with Azure AD if true, otherwise false.
     */
    @Override
    public boolean isPasswordlessEnabled() {
        return this.passwordlessEnabled;
    }

    /**
     * Set the value to enable/disable connections authenticating with Azure AD.
     * If not set, by default the value is false.
     *
     * @param passwordlessEnabled the passwordlessEnabled
     */
    @Override
    public void setPasswordlessEnabled(boolean passwordlessEnabled) {
        this.passwordlessEnabled = passwordlessEnabled;
    }

    private String getDefaultScopes() {
        return JDBC_SCOPE_MAP.getOrDefault(getProfile().getCloudType(), JDBC_SCOPE_AZURE);
    }

    /**
     * Get the profile
     * @return the profile
     */
    @Override
    public AzureProfileProperties getProfile() {
        return profile;
    }

    /**
     * Set the profile
     * @param profile the profile properties related to an Azure subscription
     */
    public void setProfile(AzureProfileProperties profile) {
        this.profile = profile;
    }

    /**
     * Get the credential properties.
     *
     * @return the credential properties.
     */
    @Override
    public TokenCredentialProperties getCredential() {
        return credential;
    }

    /**
     * Set the credential properties.
     *
     * @param credential the credential properties
     */
    public void setCredential(TokenCredentialProperties credential) {
        this.credential = credential;
    }
}
