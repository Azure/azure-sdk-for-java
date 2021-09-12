// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.keyvault.certificates;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.Configuration;
import com.azure.security.keyvault.certificates.CertificateClientBuilder;
import com.azure.spring.core.credential.descriptor.AuthenticationDescriptor;
import com.azure.spring.core.credential.descriptor.TokenAuthenticationDescriptor;
import com.azure.spring.core.factory.AbstractAzureHttpClientBuilderFactory;
import com.azure.spring.core.properties.AzureProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.PropertyMapper;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Azure Key Vault certificate client builder factory, it builds the {@link CertificateClientBuilder}.
 */
public class CertificateClientBuilderFactory extends AbstractAzureHttpClientBuilderFactory<CertificateClientBuilder> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateClientBuilderFactory.class);

    private final AzureKeyVaultCertificateProperties certificateProperties;


    public CertificateClientBuilderFactory(AzureKeyVaultCertificateProperties certificateProperties) {
        this.certificateProperties = certificateProperties;
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
    protected CertificateClientBuilder createBuilderInstance() {
        return new CertificateClientBuilder();
    }

    @Override
    protected AzureProperties getAzureProperties() {
        return this.certificateProperties;
    }

    @Override
    protected List<AuthenticationDescriptor<?>> getAuthenticationDescriptors(CertificateClientBuilder builder) {
        return Arrays.asList(
            new TokenAuthenticationDescriptor(provider -> builder.credential(provider.getCredential()))
        );
    }

    @Override
    protected void configureService(CertificateClientBuilder builder) {
        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
        map.from(certificateProperties.getVaultUrl()).to(builder::vaultUrl);
        map.from(certificateProperties.getServiceVersion()).to(builder::serviceVersion);
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
}
