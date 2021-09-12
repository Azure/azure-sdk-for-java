// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure;

import com.azure.spring.core.factory.AzureServiceClientBuilderFactory;
import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.core.properties.credential.TokenCredentialProperties;

public abstract class AzureServiceClientBuilderFactoryTestBase<B, P extends AzureProperties,
                                                                  T extends AzureServiceClientBuilderFactory<B>> {

    protected abstract P createMinimalServiceProperties();
    
    protected TokenCredentialProperties buildClientSecretTokenCredentialProperties() {
        TokenCredentialProperties properties = new TokenCredentialProperties();
        properties.setTenantId("test-tenant");
        properties.setClientId("test-client");
        properties.setClientSecret("test-secret");

        return properties;
    }

    protected TokenCredentialProperties buildClientCertificateTokenCredentialProperties() {
        TokenCredentialProperties properties = new TokenCredentialProperties();
        properties.setTenantId("test-tenant");
        properties.setClientId("test-client");
        properties.setClientCertificatePath("test-cert-path");
        properties.setClientCertificatePassword("test-cert-password");

        return properties;
    }

}
