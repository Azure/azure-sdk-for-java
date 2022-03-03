// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.keyvault.secret;

import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.identity.ClientCertificateCredential;
import com.azure.identity.ClientSecretCredential;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.SecretServiceVersion;
import com.azure.spring.cloud.service.implementation.AzureHttpClientBuilderFactoryBaseTests;
import com.azure.spring.cloud.service.implementation.core.http.TestHttpClient;
import com.azure.spring.cloud.service.implementation.keyvault.secrets.SecretClientBuilderFactory;
import org.junit.jupiter.api.Test;
import org.mockito.verification.VerificationMode;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 *
 */
class SecretClientBuilderFactoryTests extends AzureHttpClientBuilderFactoryBaseTests<SecretClientBuilder,
    AzureKeyVaultSecretTestProperties, SecretClientBuilderFactory> {

    private static final String ENDPOINT = "https://abc.vault.azure.net/";


    @Override
    protected AzureKeyVaultSecretTestProperties createMinimalServiceProperties() {
        return new AzureKeyVaultSecretTestProperties();
    }

    @Test
    void testServiceVersionConfigured() {
        AzureKeyVaultSecretTestProperties properties = new AzureKeyVaultSecretTestProperties();
        properties.setServiceVersion(SecretServiceVersion.V7_0);

        final SecretClientBuilderFactoryExt factoryExt = new SecretClientBuilderFactoryExt(properties);
        final SecretClientBuilder builder = factoryExt.build();
        verify(builder, times(1)).serviceVersion(SecretServiceVersion.V7_0);
    }

    @Test
    void testEndpointConfigured() {
        AzureKeyVaultSecretTestProperties properties = new AzureKeyVaultSecretTestProperties();
        properties.setEndpoint(ENDPOINT);

        final SecretClientBuilderFactoryExt factoryExt = new SecretClientBuilderFactoryExt(properties);
        final SecretClientBuilder builder = factoryExt.build();
        verify(builder, times(1)).vaultUrl(ENDPOINT);
    }


    @Test
    void testClientSecretTokenCredentialConfigured() {
        AzureKeyVaultSecretTestProperties properties = createMinimalServiceProperties();

        properties.getCredential().setClientId("test-client");
        properties.getCredential().setClientSecret("test-secret");
        properties.getProfile().setTenantId("test-tenant");

        final SecretClientBuilderFactoryExt factoryExt = new SecretClientBuilderFactoryExt(properties);
        final SecretClientBuilder builder = factoryExt.build();

        verify(builder, times(1)).credential(any(ClientSecretCredential.class));
    }

    @Test
    void testClientCertificateTokenCredentialConfigured() {
        AzureKeyVaultSecretTestProperties properties = createMinimalServiceProperties();

        properties.getCredential().setClientId("test-client");
        properties.getCredential().setClientCertificatePath("test-cert-path");
        properties.getCredential().setClientCertificatePassword("test-cert-password");
        properties.getProfile().setTenantId("test-tenant");

        final SecretClientBuilderFactoryExt factoryExt = new SecretClientBuilderFactoryExt(properties);
        final SecretClientBuilder builder = factoryExt.build();

        verify(builder, times(1)).credential(any(ClientCertificateCredential.class));
    }

    @Override
    protected SecretClientBuilderFactory getClientBuilderFactoryWithMockBuilder(AzureKeyVaultSecretTestProperties properties) {
        return new SecretClientBuilderFactoryExt(properties);
    }

    @Override
    protected void verifyHttpClientCalled(SecretClientBuilder builder, VerificationMode mode) {
        verify(builder, mode).httpClient(any(TestHttpClient.class));
    }

    @Override
    protected void verifyHttpPipelinePolicyAdded(SecretClientBuilder builder, HttpPipelinePolicy policy, VerificationMode mode) {
        verify(builder, mode).addPolicy(policy);
    }

    static class SecretClientBuilderFactoryExt extends SecretClientBuilderFactory {

        SecretClientBuilderFactoryExt(AzureKeyVaultSecretTestProperties secretProperties) {
            super(secretProperties);
        }

        @Override
        protected SecretClientBuilder createBuilderInstance() {
            return mock(SecretClientBuilder.class);
        }
    }
}

