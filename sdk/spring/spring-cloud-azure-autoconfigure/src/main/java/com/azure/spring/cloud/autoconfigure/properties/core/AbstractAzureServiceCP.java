// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.properties.core;

import com.azure.spring.cloud.autoconfigure.properties.core.authentication.TokenCredentialCP;
import com.azure.spring.cloud.autoconfigure.properties.core.profile.AzureProfileCP;
import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.core.properties.resource.AzureResourceMetadata;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Configuration properties base class for all Azure Service clients.
 */
public abstract class AbstractAzureServiceCP implements AzureProperties {

    /**
     * Whether an Azure Service is enabled.
     */
    protected boolean enabled = true;

    @NestedConfigurationProperty
    protected final TokenCredentialCP credential = new TokenCredentialCP();

    @NestedConfigurationProperty
    protected final AzureProfileCP profile = new AzureProfileCP();

    @NestedConfigurationProperty
    protected final AzureResourceMetadata resource = new AzureResourceMetadata();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public TokenCredentialCP getCredential() {
        return credential;
    }

    @Override
    public AzureProfileCP getProfile() {
        return profile;
    }

    public AzureResourceMetadata getResource() {
        return resource;
    }
}
