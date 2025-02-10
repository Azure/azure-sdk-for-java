// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.properties.core;

import com.azure.spring.cloud.autoconfigure.implementation.properties.core.authentication.TokenCredentialConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.core.profile.AzureProfileConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.resourcemanager.AzureResourceMetadataConfigurationProperties;
import com.azure.spring.cloud.core.properties.AzureProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Configuration properties base class for all Azure Service clients.
 */
public abstract class AbstractAzureServiceConfigurationProperties implements AzureProperties {

    /**
     * Whether an Azure Service is enabled.
     */
    protected boolean enabled = true;

    @NestedConfigurationProperty
    protected final TokenCredentialConfigurationProperties credential = new TokenCredentialConfigurationProperties();

    @NestedConfigurationProperty
    protected final AzureProfileConfigurationProperties profile = new AzureProfileConfigurationProperties();

    @NestedConfigurationProperty
    protected final AzureResourceMetadataConfigurationProperties resource = new AzureResourceMetadataConfigurationProperties();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public TokenCredentialConfigurationProperties getCredential() {
        return credential;
    }

    @Override
    public AzureProfileConfigurationProperties getProfile() {
        return profile;
    }

    public AzureResourceMetadataConfigurationProperties getResource() {
        return resource;
    }
}
