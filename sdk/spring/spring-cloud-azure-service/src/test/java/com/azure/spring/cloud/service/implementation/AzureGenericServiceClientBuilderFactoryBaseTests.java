// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation;

import com.azure.spring.cloud.core.implementation.factory.AzureServiceClientBuilderFactory;
import com.azure.spring.cloud.core.implementation.properties.AzureSdkProperties;

public abstract class AzureGenericServiceClientBuilderFactoryBaseTests<P extends AzureSdkProperties, F extends AzureServiceClientBuilderFactory<?>> {

    protected abstract P createMinimalServiceProperties();
    protected abstract F createClientBuilderFactoryWithMockBuilder(P properties);

    protected F factoryWithMinimalSettings() {
        P properties = createMinimalServiceProperties();
        return createClientBuilderFactoryWithMockBuilder(properties);
    }

    protected F factoryWithClientSecretTokenCredentialConfigured() {
        P properties = createClientSecretTokenCredentialAwareServiceProperties();
        return createClientBuilderFactoryWithMockBuilder(properties);
    }

    protected F factoryWithClientCertificateTokenCredentialConfigured() {
        P properties = createClientCertificateTokenCredentialAwareServiceProperties();
        return createClientBuilderFactoryWithMockBuilder(properties);
    }

    protected F factoryWithManagedIdentityTokenCredentialConfigured() {
        P properties = createManagedIdentityCredentialAwareServiceProperties();
        return createClientBuilderFactoryWithMockBuilder(properties);
    }

    private P createClientSecretTokenCredentialAwareServiceProperties() {
        P properties = createMinimalServiceProperties();
        properties.getCredential().setClientId("test-client");
        properties.getCredential().setClientSecret("test-secret");
        properties.getProfile().setTenantId("test-tenant");
        return properties;
    }

    private P createClientCertificateTokenCredentialAwareServiceProperties() {
        P properties = createMinimalServiceProperties();
        properties.getCredential().setClientId("test-client");
        properties.getCredential().setClientCertificatePath("test-cert-path");
        properties.getCredential().setClientCertificatePassword("test-cert-password");
        properties.getProfile().setTenantId("test-tenant");
        return properties;
    }

    private P createManagedIdentityCredentialAwareServiceProperties() {
        P properties = createMinimalServiceProperties();
        properties.getCredential().setManagedIdentityEnabled(true);
        return properties;
    }

}
