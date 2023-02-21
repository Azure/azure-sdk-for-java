// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.passwordless;

import com.azure.spring.cloud.core.properties.PasswordlessProperties;
import com.azure.spring.cloud.core.properties.authentication.TokenCredentialProperties;
import com.azure.spring.cloud.core.properties.profile.AzureProfileProperties;

/**
 * Configuration properties for passwordless connections with Azure ServiceBus.
 */
public class AzureKafkaPasswordlessProperties implements PasswordlessProperties {

    private boolean passwordlessEnabled = false;

    private AzureProfileProperties profile = new AzureProfileProperties();

    private TokenCredentialProperties credential = new TokenCredentialProperties();

    @Override
    public String getScopes() {
        return null;
    }

    @Override
    public void setScopes(String scopes) {
        throw new RuntimeException("This method is not available in AzureKafkaPasswordlessProperties");
    }

    @Override
    public boolean isPasswordlessEnabled() {
        return this.passwordlessEnabled;
    }

    @Override
    public void setPasswordlessEnabled(boolean passwordlessEnabled) {
        this.passwordlessEnabled = passwordlessEnabled;
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
