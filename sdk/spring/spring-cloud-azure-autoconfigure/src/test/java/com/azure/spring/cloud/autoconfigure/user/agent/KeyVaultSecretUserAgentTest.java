package com.azure.spring.cloud.autoconfigure.user.agent;

import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.spring.cloud.autoconfigure.keyvault.secrets.AzureKeyVaultSecretAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.keyvault.secrets.properties.AzureKeyVaultSecretProperties;
import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import com.azure.spring.service.keyvault.secrets.SecretClientBuilderFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
public class KeyVaultSecretUserAgentTest {

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
                "spring.cloud.azure.keyvault.secret.retry.delay=1",
                "spring.cloud.azure.keyvault.secret.retry.max-attempts=0",
                "spring.cloud.azure.keyvault.secret.retry.backoff.delay=0"
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
                assertThat(output).contains("User-Agent:az-sp-kv/");
            });
    }
}
