// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.core.properties;

import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.core.properties.authentication.TokenCredentialProperties;
import com.azure.spring.core.properties.client.ClientProperties;
import com.azure.spring.core.properties.profile.AzureProfile;
import com.azure.spring.core.properties.proxy.ProxyProperties;
import com.azure.spring.core.properties.retry.RetryProperties;

/**
 * Configuration properties base class for all Azure clients.
 */
abstract class AbstractServiceProperties implements AzureProperties {

    protected final TokenCredentialProperties credential = new TokenCredentialProperties();

    protected final AzureProfile profile = new AzureProfile();

    protected RetryProperties retry;

    protected ProxyProperties proxy;

    protected ClientProperties client;

    @Override
    public TokenCredentialProperties getCredential() {
        return credential;
    }

    @Override
    public AzureProfile getProfile() {
        return profile;
    }

}
