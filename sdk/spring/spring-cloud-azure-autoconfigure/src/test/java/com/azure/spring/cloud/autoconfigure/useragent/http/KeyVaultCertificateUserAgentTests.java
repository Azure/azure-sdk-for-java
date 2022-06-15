// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.useragent.http;

import com.azure.security.keyvault.certificates.CertificateAsyncClient;
import com.azure.security.keyvault.certificates.CertificateClient;
import com.azure.security.keyvault.certificates.CertificateClientBuilder;
import com.azure.spring.cloud.autoconfigure.keyvault.certificates.AzureKeyVaultCertificateAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.certificates.properties.AzureKeyVaultCertificateProperties;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import com.azure.spring.cloud.service.implementation.keyvault.certificates.CertificateClientBuilderFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;

@Isolated("Run this by itself as it captures System.out")
@ExtendWith(OutputCaptureExtension.class)
public class KeyVaultCertificateUserAgentTests {

    @Test
    public void userAgentTest(CapturedOutput output) {
        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AzureKeyVaultCertificateAutoConfiguration.class))
            .withPropertyValues(
                "spring.cloud.azure.profile.tenant-id=sample",
                "spring.cloud.azure.credential.client-id=sample",
                "spring.cloud.azure.credential.client-secret=sample",
                "spring.cloud.azure.keyvault.certificate.enabled=true",
                "spring.cloud.azure.keyvault.certificate.endpoint=https://sample.vault.azure.net/",
                "spring.cloud.azure.keyvault.certificate.client.logging.level=headers",
                "spring.cloud.azure.keyvault.certificate.client.logging.allowed-header-names=User-Agent",
                "spring.cloud.azure.keyvault.certificate.retry.fixed.delay=1s",
                "spring.cloud.azure.keyvault.certificate.retry.fixed.max-retries=0",
                "spring.cloud.azure.keyvault.certificate.retry.mode=fixed"
            )
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureKeyVaultCertificateAutoConfiguration.class);
                assertThat(context).hasSingleBean(AzureKeyVaultCertificateProperties.class);
                assertThat(context).hasSingleBean(CertificateClient.class);
                assertThat(context).hasSingleBean(CertificateAsyncClient.class);
                assertThat(context).hasSingleBean(CertificateClientBuilder.class);
                assertThat(context).hasSingleBean(CertificateClientBuilderFactory.class);

                CertificateClient certificateClient = context.getBean(CertificateClient.class);
                try {
                    certificateClient.getCertificate("test");
                } catch (Exception exception) {
                    // Eat it because we just want the log.
                }
                String allOutput = output.getAll();
                assertTrue(allOutput.contains(String.format("User-Agent:%s",
                    AzureSpringIdentifier.AZURE_SPRING_KEY_VAULT_CERTIFICATES)) || allOutput.contains(String.format(
                        "\"User-Agent\":\"%s", AzureSpringIdentifier.AZURE_SPRING_KEY_VAULT_CERTIFICATES)));
            });
    }
}
