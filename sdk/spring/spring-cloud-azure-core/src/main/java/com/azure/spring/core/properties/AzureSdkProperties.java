// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties;

import com.azure.spring.core.properties.authentication.TokenCredentialProperties;
import com.azure.spring.core.properties.profile.AzureProfile;

/**
 * Unified properties for Azure SDK clients.
 */
public abstract class AzureSdkProperties implements AzureProperties {

    protected final TokenCredentialProperties credential = new TokenCredentialProperties();
    protected final AzureProfile profile = new AzureProfile();

    @Override
    public TokenCredentialProperties getCredential() {
        return credential;
    }

    @Override
    public AzureProfile getProfile() {
        return profile;
    }
}
