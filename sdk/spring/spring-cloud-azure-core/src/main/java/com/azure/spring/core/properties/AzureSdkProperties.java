// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties;

import com.azure.spring.core.properties.client.ClientProperties;
import com.azure.spring.core.properties.credential.TokenCredentialProperties;
import com.azure.spring.core.properties.profile.AzureProfile;
import com.azure.spring.core.properties.proxy.ProxyProperties;
import com.azure.spring.core.properties.retry.RetryProperties;

/**
 * Unified properties for Azure SDK clients.
 */
public abstract class AzureSdkProperties implements AzureProperties {

    private ClientProperties client = new ClientProperties();
    private ProxyProperties proxy = new ProxyProperties();
    private RetryProperties retry = new RetryProperties();
    private TokenCredentialProperties credential = new TokenCredentialProperties();
    private AzureProfile profile = new AzureProfile();

    @Override
    public ClientProperties getClient() {
        return client;
    }

    public void setClient(ClientProperties client) {
        this.client = client;
    }

    @Override
    public ProxyProperties getProxy() {
        return proxy;
    }

    public void setProxy(ProxyProperties proxy) {
        this.proxy = proxy;
    }

    @Override
    public RetryProperties getRetry() {
        return retry;
    }

    public void setRetry(RetryProperties retry) {
        this.retry = retry;
    }

    @Override
    public TokenCredentialProperties getCredential() {
        return credential;
    }

    public void setCredential(TokenCredentialProperties credential) {
        this.credential = credential;
    }

    @Override
    public AzureProfile getProfile() {
        return profile;
    }

    public void setProfile(AzureProfile profile) {
        this.profile = profile;
    }
}
