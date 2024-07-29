// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.passwordless;

import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.core.properties.PasswordlessProperties;
import com.azure.spring.cloud.core.properties.authentication.TokenCredentialProperties;
import com.azure.spring.cloud.core.properties.client.ClientProperties;
import com.azure.spring.cloud.core.properties.profile.AzureProfileProperties;
import com.azure.spring.cloud.core.properties.proxy.ProxyProperties;

/**
 * Implement {@link PasswordlessProperties} for Spring Cloud Azure support for other third party services.
 */
public class AzurePasswordlessProperties implements PasswordlessProperties, AzureProperties {

    private AzureProfileProperties profile = new AzureProfileProperties();

    private String scopes;

    private TokenCredentialProperties credential = new TokenCredentialProperties();

    // Use client options inside credential for azure identity
    private ClientProperties client = new ClientProperties();

    // Use proxy options inside credential for azure identity
    private ProxyProperties proxy = new ProxyProperties();

    // Whether to enable supporting azure identity token credentials
    private boolean passwordlessEnabled = false;

    @Override
    public AzureProfileProperties getProfile() {
        return profile;
    }

    public void setProfile(AzureProfileProperties profile) {
        this.profile = profile;
    }

    @Override
    public TokenCredentialProperties getCredential() {
        return credential;
    }

    public void setCredential(TokenCredentialProperties credential) {
        this.credential = credential;
    }

    @Override
    public ClientOptions getClient() {
        return client;
    }

    public void setClient(ClientProperties client) {
        this.client = client;
    }

    @Override
    public ProxyOptions getProxy() {
        return proxy;
    }

    public void setProxy(ProxyProperties proxy) {
        this.proxy = proxy;
    }

    public boolean isPasswordlessEnabled() {
        return passwordlessEnabled;
    }

    public void setPasswordlessEnabled(boolean passwordlessEnabled) {
        this.passwordlessEnabled = passwordlessEnabled;
    }

    public String getScopes() {
        return scopes;
    }

    public void setScopes(String scopes) {
        this.scopes = scopes;
    }

}
