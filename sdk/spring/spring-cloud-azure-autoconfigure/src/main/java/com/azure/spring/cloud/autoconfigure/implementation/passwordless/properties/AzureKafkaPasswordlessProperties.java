// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.passwordless.properties;

import com.azure.spring.cloud.core.properties.PasswordlessProperties;
import com.azure.spring.cloud.core.properties.authentication.TokenCredentialProperties;
import com.azure.spring.cloud.core.properties.profile.AzureProfileProperties;

/**
 * Configuration properties for passwordless connections with Azure Event Hubs Kafka.
 */
public class AzureKafkaPasswordlessProperties implements PasswordlessProperties {

    private boolean passwordlessEnabled = false;

    private AzureProfileProperties profile = new AzureProfileProperties();

    private TokenCredentialProperties credential = new TokenCredentialProperties();

    /**
     * Get the scopes required for the access token.
     * This method is not available in AzureKafkaPasswordlessProperties, will always return null.
     *
     * @return null
     */
    @Override
    public String getScopes() {
        return null;
    }

    /**
     * Set the scopes required for the access token.
     *
     * This method is not available in AzureKafkaPasswordlessProperties
     */
    @Override
    public void setScopes(String scopes) {
        throw new RuntimeException("This method is not available in AzureKafkaPasswordlessProperties");
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
