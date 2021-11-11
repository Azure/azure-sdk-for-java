// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.properties;

import com.azure.spring.cloud.autoconfigure.properties.core.authentication.TokenCredentialCP;
import com.azure.spring.cloud.autoconfigure.properties.core.client.ClientCP;
import com.azure.spring.cloud.autoconfigure.properties.core.profile.AzureProfileCP;
import com.azure.spring.cloud.autoconfigure.properties.core.proxy.ProxyCP;
import com.azure.spring.cloud.autoconfigure.properties.core.retry.RetryCP;
import com.azure.spring.core.properties.AzureProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 *
 */
public class AzureGlobalProperties implements AzureProperties {

    public static final String PREFIX = "spring.cloud.azure";

    @NestedConfigurationProperty
    protected final ClientCP client = new ClientCP();

    @NestedConfigurationProperty
    protected final ProxyCP proxy = new ProxyCP();

    @NestedConfigurationProperty
    protected final RetryCP retry = new RetryCP();

    @NestedConfigurationProperty
    protected final TokenCredentialCP credential = new TokenCredentialCP();

    @NestedConfigurationProperty
    protected final AzureProfileCP profile = new AzureProfileCP();

    @Override
    public ClientCP getClient() {
        return client;
    }

    @Override
    public ProxyCP getProxy() {
        return proxy;
    }

    @Override
    public RetryCP getRetry() {
        return retry;
    }

    @Override
    public TokenCredentialCP getCredential() {
        return credential;
    }

    @Override
    public AzureProfileCP getProfile() {
        return profile;
    }
}
