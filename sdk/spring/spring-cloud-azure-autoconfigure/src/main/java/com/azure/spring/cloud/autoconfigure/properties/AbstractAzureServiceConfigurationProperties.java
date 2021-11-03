// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.properties;

import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.core.properties.credential.TokenCredentialProperties;
import com.azure.spring.core.properties.profile.AzureProfile;
import com.azure.spring.core.properties.resource.AzureResourceMetadata;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Configuration properties base class for all Azure clients.
 */
public abstract class AbstractAzureServiceConfigurationProperties implements AzureProperties {

    protected boolean enabled = true;

    @NestedConfigurationProperty
    protected final TokenCredentialProperties credential = new TokenCredentialProperties();

    @NestedConfigurationProperty
    protected final AzureProfile profile = new AzureProfile();

    @NestedConfigurationProperty
    protected final AzureResourceMetadata resource = new AzureResourceMetadata();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public TokenCredentialProperties getCredential() {
        return credential;
    }

    @Override
    public AzureProfile getProfile() {
        return profile;
    }

    public AzureResourceMetadata getResource() {
        return resource;
    }

}
