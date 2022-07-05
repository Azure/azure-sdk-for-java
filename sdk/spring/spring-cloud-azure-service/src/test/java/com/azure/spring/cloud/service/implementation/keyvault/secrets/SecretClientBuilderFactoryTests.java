// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.keyvault.secrets;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.HttpClientOptions;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.SecretServiceVersion;
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
class SecretClientBuilderFactoryTests extends
    AzureHttpClientBuilderFactoryBaseTests<
        SecretClientBuilder,
        AzureKeyVaultSecretTestProperties,
        SecretClientBuilderFactoryTests.SecretClientBuilderFactoryExt> {

    private static final String ENDPOINT = "https://abc.vault.azure.net/";

    @Override
    protected AzureKeyVaultSecretTestProperties createMinimalServiceProperties() {
        return new AzureKeyVaultSecretTestProperties();
    }

    @Override
    protected SecretClientBuilderFactoryExt createClientBuilderFactoryWithMockBuilder(AzureKeyVaultSecretTestProperties properties) {
        return new SecretClientBuilderFactoryExt(properties);
    }

    @Override
    protected void buildClient(SecretClientBuilder builder) {
        builder.buildClient();
    }

    @Override
    protected void verifyCredentialCalled(SecretClientBuilder builder,
                                          Class<? extends TokenCredential> tokenCredentialClass,
                                          VerificationMode mode) {
        verify(builder, mode).credential(any(tokenCredentialClass));
    }

    @Override
    protected void verifyHttpClientCalled(SecretClientBuilder builder, VerificationMode mode) {
        verify(builder, mode).httpClient(any(TestHttpClient.class));
    }

    @Override
    protected void verifyRetryOptionsCalled(SecretClientBuilder builder, AzureKeyVaultSecretTestProperties properties, VerificationMode mode) {
        verify(builder, mode).retryPolicy(any(RetryPolicy.class));
    }

    @Override
    protected HttpClientOptions getHttpClientOptions(SecretClientBuilderFactoryExt builderFactory) {
        return builderFactory.getHttpClientOptions();
    }

    @Override
    protected List<HttpPipelinePolicy> getHttpPipelinePolicies(SecretClientBuilderFactoryExt builderFactory) {
        return builderFactory.getHttpPipelinePolicies();
    }

    @Override
    protected void verifyServicePropertiesConfigured() {
        AzureKeyVaultSecretTestProperties properties = new AzureKeyVaultSecretTestProperties();
        properties.setServiceVersion(SecretServiceVersion.V7_0);
        properties.setEndpoint(ENDPOINT);

        final SecretClientBuilderFactoryExt factoryExt = new SecretClientBuilderFactoryExt(properties);
        final SecretClientBuilder builder = factoryExt.build();

        verify(builder, times(1)).vaultUrl(ENDPOINT);
        verify(builder, times(1)).serviceVersion(SecretServiceVersion.V7_0);
    }

    static class SecretClientBuilderFactoryExt extends SecretClientBuilderFactory {

        SecretClientBuilderFactoryExt(AzureKeyVaultSecretTestProperties secretProperties) {
            super(secretProperties);
        }

        @Override
        protected SecretClientBuilder createBuilderInstance() {
            return mock(SecretClientBuilder.class);
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

