// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.keyvault;

import com.azure.security.keyvault.certificates.CertificateAsyncClient;
import com.azure.security.keyvault.certificates.CertificateClient;
import com.azure.security.keyvault.certificates.CertificateClientBuilder;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.keyvault.certificates.AzureKeyVaultCertificateAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.keyvault.secrets.AzureKeyVaultSecretAutoConfiguration;
import com.azure.spring.cloud.service.implementation.keyvault.certificates.CertificateClientBuilderFactory;
import com.azure.spring.cloud.service.implementation.keyvault.secrets.SecretClientBuilderFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AzureKeyVaultAutoConfigurationTests {

    private static final String ENDPOINT = "https:/%s.vault.azure.net/";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
        .withConfiguration(AutoConfigurations.of(AzureKeyVaultCertificateAutoConfiguration.class))
        .withConfiguration(AutoConfigurations.of(AzureKeyVaultSecretAutoConfiguration.class));

    @Test
    void secretsConfigShouldWorkWithCertificatesConfig() {
        String endpoint = String.format(ENDPOINT, "mykv");
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.keyvault.secret.endpoint=" + endpoint,
                "spring.cloud.azure.keyvault.certificate.endpoint=" + endpoint
            )
            .run(context -> {
                assertThat(context).hasSingleBean(SecretClientBuilder.class);
                assertThat(context).hasSingleBean(CertificateClientBuilder.class);

                assertThat(context).hasSingleBean(SecretClient.class);
                assertThat(context).hasSingleBean(CertificateClient.class);

                assertThat(context).hasSingleBean(SecretAsyncClient.class);
                assertThat(context).hasSingleBean(CertificateAsyncClient.class);

                assertThat(context).hasSingleBean(SecretClientBuilderFactory.class);
                assertThat(context).hasSingleBean(CertificateClientBuilderFactory.class);
            });
    }

    @Test
    void secretsAndCertificatesShouldWorkWithGlobalConfig() {
        String endpoint = String.format(ENDPOINT, "mykv");
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.keyvault.endpoint=" + endpoint
            )
            .run(context -> {
                assertThat(context).hasSingleBean(SecretClient.class);
                assertThat(context).hasSingleBean(CertificateClient.class);

                assertThat(context).hasSingleBean(SecretAsyncClient.class);
                assertThat(context).hasSingleBean(CertificateAsyncClient.class);

                assertThat(context).hasSingleBean(SecretClientBuilder.class);
                assertThat(context).hasSingleBean(CertificateClientBuilder.class);

                assertThat(context).hasSingleBean(SecretClientBuilderFactory.class);
                assertThat(context).hasSingleBean(CertificateClientBuilderFactory.class);
            });
    }
}
