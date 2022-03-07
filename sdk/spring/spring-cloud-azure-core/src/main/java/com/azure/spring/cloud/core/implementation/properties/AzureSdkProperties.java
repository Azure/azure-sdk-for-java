// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.properties;

import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.core.properties.authentication.TokenCredentialProperties;
import com.azure.spring.cloud.core.properties.profile.AzureProfileProperties;

/**
 * Unified properties for Azure SDK clients.
 */
public abstract class AzureSdkProperties implements AzureProperties {

    private final TokenCredentialProperties credential = new TokenCredentialProperties();
    private final AzureProfileProperties profile = new AzureProfileProperties();

    @Override
    public TokenCredentialProperties getCredential() {
        return credential;
    }

    @Override
    public AzureProfileProperties getProfile() {
        return profile;
    }
}
