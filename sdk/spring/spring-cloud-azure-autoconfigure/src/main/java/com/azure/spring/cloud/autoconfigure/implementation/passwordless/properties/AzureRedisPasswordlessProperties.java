// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.passwordless.properties;

import com.azure.spring.cloud.core.properties.PasswordlessProperties;
import com.azure.spring.cloud.core.properties.authentication.TokenCredentialProperties;
import com.azure.spring.cloud.core.properties.profile.AzureProfileProperties;
import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration properties for passwordless connections with Azure Redis.
 */
public class AzureRedisPasswordlessProperties implements PasswordlessProperties {

    private static final String REDIS_SCOPE_AZURE = "https://redis.azure.com/.default";
    private static final String REDIS_SCOPE_AZURE_CHINA = "https://*.cacheinfra.windows.net.china:10225/appid/.default";
    private static final String REDIS_SCOPE_AZURE_US_GOVERNMENT = "https://*.cacheinfra.windows.us.government.net:10225/appid/.default";

    private static final Map<CloudType, String> REDIS_SCOPE_MAP = new HashMap<CloudType, String>() {
        {
            put(AzureProfileOptionsProvider.CloudType.AZURE, REDIS_SCOPE_AZURE);
            put(AzureProfileOptionsProvider.CloudType.AZURE_CHINA, REDIS_SCOPE_AZURE_CHINA);
            put(AzureProfileOptionsProvider.CloudType.AZURE_US_GOVERNMENT, REDIS_SCOPE_AZURE_US_GOVERNMENT);
        }
    };

    private AzureProfileProperties profile = new AzureProfileProperties();

    private String scopes;

    /**
     * Whether to enable supporting azure identity token credentials, by default is false.
     *
     * If the passwordlessEnabled is true, but the redis password properties is not null, it will still use username/password to authenticate connections.
     */
    private boolean passwordlessEnabled = false;

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
    @Override
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
        return REDIS_SCOPE_MAP.getOrDefault(getProfile().getCloudType(), REDIS_SCOPE_AZURE);
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
