// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.properties;

import com.azure.spring.core.properties.resource.AzureResourceMetadata;
import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.core.properties.client.ClientProperties;
import com.azure.spring.core.properties.credential.TokenCredentialProperties;
import com.azure.spring.core.properties.profile.AzureProfile;
import com.azure.spring.core.properties.proxy.ProxyProperties;
import com.azure.spring.core.properties.retry.RetryProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Configuration properties base class for all Azure Http clients.
 */
public abstract class AzureHttpConfigurationProperties implements AzureProperties {

    @NestedConfigurationProperty
    protected final ClientProperties client = new ClientProperties();

    @NestedConfigurationProperty
    protected final ProxyProperties proxy = new ProxyProperties();

    @NestedConfigurationProperty
    protected final RetryProperties retry = new RetryProperties();

    @NestedConfigurationProperty
    protected final TokenCredentialProperties credential = new TokenCredentialProperties();

    @NestedConfigurationProperty
    protected final AzureProfile profile = new AzureProfile();

    @NestedConfigurationProperty
    protected final AzureResourceMetadata resource = new AzureResourceMetadata();

    @Override
    public ClientProperties getClient() {
        return client;
    }

    @Override
    public ProxyProperties getProxy() {
        return proxy;
    }

    @Override
    public RetryProperties getRetry() {
        return retry;
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
