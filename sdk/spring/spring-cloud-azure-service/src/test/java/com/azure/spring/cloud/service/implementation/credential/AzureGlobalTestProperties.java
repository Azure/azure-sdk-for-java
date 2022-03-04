// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.credential;

import com.azure.spring.cloud.core.aware.RetryOptionsAware;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.core.properties.authentication.TokenCredentialProperties;
import com.azure.spring.cloud.core.properties.client.ClientProperties;
import com.azure.spring.cloud.core.properties.profile.AzureProfileProperties;
import com.azure.spring.cloud.core.properties.proxy.ProxyProperties;
import com.azure.spring.cloud.core.properties.retry.RetryProperties;

public class AzureGlobalTestProperties implements AzureProperties, RetryOptionsAware {

    private final TokenCredentialProperties credential = new TokenCredentialProperties();
    private final AzureProfileProperties profile = new AzureProfileProperties();
    private final ClientProperties client = new ClientProperties();
    private final ProxyProperties proxy = new ProxyProperties();
    private final RetryProperties retry = new RetryProperties();

    @Override
    public TokenCredentialProperties getCredential() {
        return credential;
    }

    @Override
    public AzureProfileProperties getProfile() {
        return profile;
    }

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
}
