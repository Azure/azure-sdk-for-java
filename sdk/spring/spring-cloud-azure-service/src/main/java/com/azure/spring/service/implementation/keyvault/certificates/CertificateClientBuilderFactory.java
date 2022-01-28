// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.implementation.keyvault.certificates;

import com.azure.core.util.ClientOptions;
import com.azure.security.keyvault.certificates.CertificateClientBuilder;
import com.azure.spring.core.factory.AbstractAzureHttpClientBuilderFactory;
import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.core.properties.PropertyMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    protected CertificateClientBuilder createBuilderInstance() {
        return new CertificateClientBuilder();
    }

    @Override
    protected AzureProperties getAzureProperties() {
        return this.certificateClientProperties;
    }

    @Override
    protected void configureService(CertificateClientBuilder builder) {
        PropertyMapper map = new PropertyMapper();
        map.from(certificateClientProperties.getEndpoint()).to(builder::vaultUrl);
        map.from(certificateClientProperties.getServiceVersion()).to(builder::serviceVersion);
    }
}
