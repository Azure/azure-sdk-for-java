// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.properties;

import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.core.properties.authentication.TokenCredentialProperties;
import com.azure.spring.cloud.core.properties.profile.AzureProfileProperties;

/**
 * Implement {@link AzureProperties} for Spring Cloud Azure support for other third party services.
 */
public class AzureThirdPartyServiceProperties implements AzureProperties {

    private AzureProfileProperties profile = new AzureProfileProperties();

    private TokenCredentialProperties credential = new TokenCredentialProperties();

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
        return null;
    }

    @Override
    public ProxyOptions getProxy() {
        return null;
    }

}
