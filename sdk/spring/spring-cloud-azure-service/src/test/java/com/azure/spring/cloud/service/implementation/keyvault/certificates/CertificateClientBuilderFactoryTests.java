// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.keyvault.certificates;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.HttpClientOptions;
import com.azure.security.keyvault.certificates.CertificateClientBuilder;
import com.azure.security.keyvault.certificates.CertificateServiceVersion;
import com.azure.spring.cloud.service.implementation.AzureHttpClientBuilderFactoryBaseTests;
import com.azure.spring.cloud.service.implementation.core.http.TestHttpClient;
import org.mockito.verification.VerificationMode;

import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 *
 */
class CertificateClientBuilderFactoryTests extends 
    AzureHttpClientBuilderFactoryBaseTests<
        CertificateClientBuilder, 
        AzureKeyVaultCertificateTestProperties,
        CertificateClientBuilderFactoryTests.CertificateClientBuilderFactoryExt> {

    private static final String ENDPOINT = "https://abc.vault.azure.net/";

    @Override
    protected AzureKeyVaultCertificateTestProperties createMinimalServiceProperties() {
        return new AzureKeyVaultCertificateTestProperties();
    }

    @Override
    protected CertificateClientBuilderFactoryExt createClientBuilderFactoryWithMockBuilder(
        AzureKeyVaultCertificateTestProperties properties) {
        return new CertificateClientBuilderFactoryExt(properties);
    }

    @Override
    protected void buildClient(CertificateClientBuilder builder) {
        builder.buildClient();
    }

    @Override
    protected void verifyServicePropertiesConfigured() {
        AzureKeyVaultCertificateTestProperties properties = new AzureKeyVaultCertificateTestProperties();
        properties.setServiceVersion(CertificateServiceVersion.V7_0);
        properties.setEndpoint(ENDPOINT);

        final CertificateClientBuilderFactoryExt factoryExt = new CertificateClientBuilderFactoryExt(properties);
        final CertificateClientBuilder builder = factoryExt.build();
        verify(builder, times(1)).serviceVersion(CertificateServiceVersion.V7_0);
        verify(builder, times(1)).vaultUrl(ENDPOINT);
    }

    @Override
    protected void verifyCredentialCalled(CertificateClientBuilder builder,
                                          Class<? extends TokenCredential> tokenCredentialClass,
                                          VerificationMode mode) {
        verify(builder, mode).credential(any(tokenCredentialClass));
    }

    @Override
    protected void verifyRetryOptionsCalled(CertificateClientBuilder builder,
                                            AzureKeyVaultCertificateTestProperties properties,
                                            VerificationMode mode) {
        // TODO (xiada) change this when the CertificateClientBuilder support RetryOptions
        verify(builder, mode).retryPolicy(any(RetryPolicy.class));
    }

    @Override
    protected void verifyHttpClientCalled(CertificateClientBuilder builder, VerificationMode mode) {
        verify(builder, mode).httpClient(any(TestHttpClient.class));
    }

    @Override
    protected HttpClientOptions getHttpClientOptions(CertificateClientBuilderFactoryExt builderFactory) {
        return builderFactory.getHttpClientOptions();
    }

    @Override
    protected List<HttpPipelinePolicy> getHttpPipelinePolicies(CertificateClientBuilderFactoryExt builderFactory) {
        return builderFactory.getHttpPipelinePolicies();
    }
    

    static class CertificateClientBuilderFactoryExt extends CertificateClientBuilderFactory {

        CertificateClientBuilderFactoryExt(AzureKeyVaultCertificateTestProperties secretProperties) {
            super(secretProperties);
        }

        @Override
        protected CertificateClientBuilder createBuilderInstance() {
            return mock(CertificateClientBuilder.class);
        }

        @Override
        public HttpClientOptions getHttpClientOptions() {
            return super.getHttpClientOptions();
        }

        @Override
        public List<HttpPipelinePolicy> getHttpPipelinePolicies() {
            return super.getHttpPipelinePolicies();
        }
    }
}

