// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.keyvault.certificates;

import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.security.keyvault.certificates.CertificateClientBuilder;
import com.azure.security.keyvault.certificates.CertificateServiceVersion;
import com.azure.spring.cloud.service.implementation.AzureHttpClientBuilderFactoryBaseTests;
import com.azure.spring.cloud.service.implementation.core.http.TestHttpClient;
import org.junit.jupiter.api.Test;
import org.mockito.verification.VerificationMode;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 *
 */
class CertificateClientBuilderFactoryTests extends AzureHttpClientBuilderFactoryBaseTests<CertificateClientBuilder,
    AzureKeyVaultCertificateTestProperties, CertificateClientBuilderFactory> {

    private static final String ENDPOINT = "https://abc.vault.azure.net/";


    @Override
    protected AzureKeyVaultCertificateTestProperties createMinimalServiceProperties() {
        return new AzureKeyVaultCertificateTestProperties();
    }

    @Test
    void testServiceVersionConfigured() {
        AzureKeyVaultCertificateTestProperties properties = new AzureKeyVaultCertificateTestProperties();
        properties.setServiceVersion(CertificateServiceVersion.V7_0);

        final CertificateClientBuilderFactoryExt factoryExt = new CertificateClientBuilderFactoryExt(properties);
        final CertificateClientBuilder builder = factoryExt.build();
        verify(builder, times(1)).serviceVersion(CertificateServiceVersion.V7_0);
    }

    @Test
    void testEndpointConfigured() {
        AzureKeyVaultCertificateTestProperties properties = new AzureKeyVaultCertificateTestProperties();
        properties.setEndpoint(ENDPOINT);

        final CertificateClientBuilderFactoryExt factoryExt = new CertificateClientBuilderFactoryExt(properties);
        final CertificateClientBuilder builder = factoryExt.build();
        verify(builder, times(1)).vaultUrl(ENDPOINT);
    }

    @Override
    protected CertificateClientBuilderFactory getClientBuilderFactoryWithMockBuilder(AzureKeyVaultCertificateTestProperties properties) {
        return new CertificateClientBuilderFactoryExt(properties);
    }

    @Override
    protected void verifyHttpClientCalled(CertificateClientBuilder builder, VerificationMode mode) {
        verify(builder, mode).httpClient(any(TestHttpClient.class));
    }

    @Override
    protected void verifyHttpPipelinePolicyAdded(CertificateClientBuilder builder, HttpPipelinePolicy policy, VerificationMode mode) {
        verify(builder, mode).addPolicy(policy);
    }

    static class CertificateClientBuilderFactoryExt extends CertificateClientBuilderFactory {

        CertificateClientBuilderFactoryExt(AzureKeyVaultCertificateTestProperties secretProperties) {
            super(secretProperties);
        }

        @Override
        protected CertificateClientBuilder createBuilderInstance() {
            return mock(CertificateClientBuilder.class);
        }
    }
}

