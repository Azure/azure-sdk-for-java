// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.useragent.http;

import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.spring.cloud.autoconfigure.keyvault.secrets.AzureKeyVaultSecretAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.secrets.properties.AzureKeyVaultSecretProperties;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import com.azure.spring.cloud.service.implementation.keyvault.secrets.SecretClientBuilderFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Isolated("Run this by itself as it captures System.out")
@ExtendWith(OutputCaptureExtension.class)
public class KeyVaultSecretUserAgentTests {

    private static final String format1 = String.format("User-Agent:%s",
        AzureSpringIdentifier.AZURE_SPRING_KEY_VAULT_SECRETS);
    private static final String format2 = String.format("\"User-Agent\":\"%s",
        AzureSpringIdentifier.AZURE_SPRING_KEY_VAULT_SECRETS);

    @Test
    public void userAgentTest(CapturedOutput output) {
        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AzureKeyVaultSecretAutoConfiguration.class))
            .withPropertyValues(
                "spring.cloud.azure.profile.tenant-id=sample",
                "spring.cloud.azure.credential.client-id=sample",
                "spring.cloud.azure.credential.client-secret=sample",
                "spring.cloud.azure.keyvault.secret.enabled=true",
                "spring.cloud.azure.keyvault.secret.endpoint=https://sample.vault.azure.net/",
                "spring.cloud.azure.keyvault.secret.client.logging.level=headers",
                "spring.cloud.azure.keyvault.secret.client.logging.allowed-header-names=User-Agent",
                "spring.cloud.azure.keyvault.secret.retry.fixed.delay=1",
                "spring.cloud.azure.keyvault.secret.retry.fixed.max-retries=0",
                "spring.cloud.azure.keyvault.secret.retry.mode=fixed"
            )
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureKeyVaultSecretAutoConfiguration.class);
                assertThat(context).hasSingleBean(AzureKeyVaultSecretProperties.class);
                assertThat(context).hasSingleBean(SecretClientBuilderFactory.class);
                assertThat(context).hasSingleBean(SecretClientBuilder.class);
                assertThat(context).hasSingleBean(SecretClient.class);
                assertThat(context).hasSingleBean(SecretAsyncClient.class);

                SecretClient secretClient = context.getBean(SecretClient.class);
                try {
                    secretClient.getSecret("name1");
                } catch (Exception exception) {
                    // Eat it because we just want the log.
                }
                String allOutput = output.getAll();
                assertTrue(allOutput.contains(format1) || allOutput.contains(format2));
            });
    }
}
