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

    protected F factoryWithClientSecretTokenCredentialConfigured(P properties) {
        P credentialProperties = createClientSecretTokenCredentialAwareServiceProperties(properties);
        return createClientBuilderFactoryWithMockBuilder(credentialProperties);
    }

    protected F factoryWithClientCertificateTokenCredentialConfigured(P properties) {
        P credentialProperties = createClientCertificateTokenCredentialAwareServiceProperties(properties);
        return createClientBuilderFactoryWithMockBuilder(credentialProperties);
    }

    protected F factoryWithUsernamePasswordTokenCredentialConfigured(P properties) {
        P credentialProperties = createUsernamePasswordTokenCredentialAwareServiceProperties(properties);
        return createClientBuilderFactoryWithMockBuilder(credentialProperties);
    }

    protected F factoryWithManagedIdentityTokenCredentialConfigured(P properties) {
        P credentialProperties = createManagedIdentityCredentialAwareServiceProperties(properties);
        return createClientBuilderFactoryWithMockBuilder(credentialProperties);
    }

    private P createClientSecretTokenCredentialAwareServiceProperties(P properties) {
        properties.getCredential().setClientId("test-client");
        properties.getCredential().setClientSecret("test-secret");
        properties.getProfile().setTenantId("test-tenant");
        return properties;
    }

    private P createClientCertificateTokenCredentialAwareServiceProperties(P properties) {
        properties.getCredential().setClientId("test-client");
        properties.getCredential().setClientCertificatePath("test-cert-path");
        properties.getCredential().setClientCertificatePassword("test-cert-password");
        properties.getProfile().setTenantId("test-tenant");
        return properties;
    }

    private P createUsernamePasswordTokenCredentialAwareServiceProperties(P properties) {
        properties.getCredential().setClientId("test-client");
        properties.getCredential().setUsername("test-username");
        properties.getCredential().setPassword("test-password");
        properties.getProfile().setTenantId("test-tenant");
        return properties;
    }

    private P createManagedIdentityCredentialAwareServiceProperties(P properties) {
        properties.getCredential().setManagedIdentityEnabled(true);
        return properties;
    }

}
