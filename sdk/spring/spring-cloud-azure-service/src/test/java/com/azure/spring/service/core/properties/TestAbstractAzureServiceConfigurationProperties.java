// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.core.properties;

import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.core.properties.credential.TokenCredentialProperties;
import com.azure.spring.core.properties.profile.AzureProfile;
import com.azure.spring.core.properties.resource.AzureResourceMetadata;

/**
 * Configuration properties base class for all Azure clients.
 */
public abstract class TestAbstractAzureServiceConfigurationProperties implements AzureProperties {

    protected boolean enabled = true;

    protected final TokenCredentialProperties credential = new TokenCredentialProperties();

    protected final AzureProfile profile = new AzureProfile();

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
