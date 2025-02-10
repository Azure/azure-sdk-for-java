// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault.environment;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.secrets.properties.AzureKeyVaultSecretProperties;
import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import com.azure.spring.cloud.core.provider.RetryOptionsProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.boot.logging.DeferredLogs;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Isolated("Run this by itself as it captures System.out")
@ExtendWith(OutputCaptureExtension.class)
class KeyVaultSecretPropertySourceUserAgentTests {

    @Test
    public void userAgentTest(CapturedOutput output) {
        AzureKeyVaultSecretProperties properties = new AzureKeyVaultSecretProperties();
        properties.setEndpoint("https://sample-propertysource.vault.azure.net/");
        properties.getClient().getLogging().setLevel(HttpLogDetailLevel.HEADERS);
        properties.getClient().getLogging().getAllowedHeaderNames().add("User-Agent");
        properties.getRetry().setMode(RetryOptionsProvider.RetryMode.FIXED);
        properties.getRetry().getFixed().setDelay(Duration.ofSeconds(1));
        properties.getRetry().getFixed().setMaxRetries(0);

        KeyVaultEnvironmentPostProcessor environmentPostProcessor = new KeyVaultEnvironmentPostProcessor(new DeferredLogs(), null);
        SecretClient secretClient = environmentPostProcessor.buildSecretClient(properties);
        try {
            secretClient.getSecret("property-source-name1");
        } catch (Exception exception) {
            // Eat it because we just want the log.
        }
        String allOutput = output.getAll();
        String format1 = String.format("User-Agent:%s", AzureSpringIdentifier.AZURE_SPRING_KEY_VAULT_SECRETS);
        String format2 = String.format("\"User-Agent\":\"%s", AzureSpringIdentifier.AZURE_SPRING_KEY_VAULT_SECRETS);
        assertTrue(allOutput.contains(format1) || allOutput.contains(format2));
    }
}
