// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.core.properties;

import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.core.properties.client.ClientProperties;
import com.azure.spring.core.properties.credential.TokenCredentialProperties;
import com.azure.spring.core.properties.profile.AzureProfile;
import com.azure.spring.core.properties.proxy.HttpProxyProperties;

public abstract class TestAzureHttpProperties implements AzureProperties {

    private final ClientProperties client = new ClientProperties();
    private final HttpProxyProperties proxy = new HttpProxyProperties();
    private final TokenCredentialProperties credential = new TokenCredentialProperties();
    private final AzureProfile profile = new AzureProfile();

    @Override
    public ClientProperties getClient() {
        return client;
    }

    @Override
    public HttpProxyProperties getProxy() {
        return proxy;
    }

    @Override
    public TokenCredentialProperties getCredential() {
        return credential;
    }

    @Override
    public AzureProfile getProfile() {
        return profile;
    }
}
