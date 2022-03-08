// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.keyvault.certificates;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.security.keyvault.certificates.CertificateClientBuilder;
import com.azure.spring.cloud.core.implementation.credential.descriptor.AuthenticationDescriptor;
import com.azure.spring.cloud.core.implementation.credential.descriptor.TokenAuthenticationDescriptor;
import com.azure.spring.cloud.core.implementation.factory.AbstractAzureHttpClientBuilderFactory;
import com.azure.spring.cloud.core.implementation.properties.PropertyMapper;
import com.azure.spring.cloud.core.properties.AzureProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Azure Key Vault certificate client builder factory, it builds the {@link CertificateClientBuilder}.
 */
public class CertificateClientBuilderFactory extends AbstractAzureHttpClientBuilderFactory<CertificateClientBuilder> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateClientBuilderFactory.class);

    private final CertificateClientProperties certificateClientProperties;

    /**
     * Create a {@link CertificateClientBuilderFactory} with the {@link CertificateClientProperties}.
     * @param certificateClientProperties the properties of the certificate client.
     */
    public CertificateClientBuilderFactory(CertificateClientProperties certificateClientProperties) {
        this.certificateClientProperties = certificateClientProperties;
    }

    @Override
    protected BiConsumer<CertificateClientBuilder, ClientOptions> consumeClientOptions() {
        return CertificateClientBuilder::clientOptions;
    }

    @Override
    protected BiConsumer<CertificateClientBuilder, HttpClient> consumeHttpClient() {
        return CertificateClientBuilder::httpClient;
    }

    @Override
    protected BiConsumer<CertificateClientBuilder, HttpPipelinePolicy> consumeHttpPipelinePolicy() {
        return CertificateClientBuilder::addPolicy;
    }

    @Override
    protected BiConsumer<CertificateClientBuilder, HttpPipeline> consumeHttpPipeline() {
        return CertificateClientBuilder::pipeline;
    }

    @Override
    protected BiConsumer<CertificateClientBuilder, HttpLogOptions> consumeHttpLogOptions() {
        return CertificateClientBuilder::httpLogOptions;
    }

    @Override
    protected CertificateClientBuilder createBuilderInstance() {
        return new CertificateClientBuilder();
    }

    @Override
    protected AzureProperties getAzureProperties() {
        return this.certificateClientProperties;
    }

    @Override
    protected List<AuthenticationDescriptor<?>> getAuthenticationDescriptors(CertificateClientBuilder builder) {
        return Arrays.asList(
            new TokenAuthenticationDescriptor(this.tokenCredentialResolver, builder::credential)
        );
    }

    @Override
    protected void configureService(CertificateClientBuilder builder) {
        PropertyMapper map = new PropertyMapper();
        map.from(certificateClientProperties.getEndpoint()).to(builder::vaultUrl);
        map.from(certificateClientProperties.getServiceVersion()).to(builder::serviceVersion);
    }

    @Override
    protected BiConsumer<CertificateClientBuilder, Configuration> consumeConfiguration() {
        return CertificateClientBuilder::configuration;
    }

    @Override
    protected BiConsumer<CertificateClientBuilder, TokenCredential> consumeDefaultTokenCredential() {
        return CertificateClientBuilder::credential;
    }

    @Override
    protected BiConsumer<CertificateClientBuilder, String> consumeConnectionString() {
        LOGGER.debug("Connection string is not supported to configure in CertificateClientBuilder");
        return (a, b) -> { };
    }

    @Override
    protected BiConsumer<CertificateClientBuilder, RetryPolicy> consumeRetryPolicy() {
        return CertificateClientBuilder::retryPolicy;
    }
}
