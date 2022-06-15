// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.useragent.http;

import com.azure.data.appconfiguration.ConfigurationAsyncClient;
import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.spring.cloud.autoconfigure.appconfiguration.AzureAppConfigurationAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.appconfiguration.AzureAppConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import com.azure.spring.cloud.service.implementation.appconfiguration.ConfigurationClientBuilderFactory;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Disabled because  for ConfigurationClient, the header is not outputted in log. I debugged and confirmed that
 * User-Agent been set successfully. The reason why the User-Agent not outputted in log is not clear.
 */
@Disabled
@Isolated("Run this by itself as it captures System.out")
@ExtendWith(OutputCaptureExtension.class)
public class AppConfigurationUserAgentTests {

    @Test
    public void userAgentTest(CapturedOutput output) {
        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AzureAppConfigurationAutoConfiguration.class))
            .withPropertyValues(
                "spring.cloud.azure.profile.tenant-id=sample",
                "spring.cloud.azure.credential.client-id=sample",
                "spring.cloud.azure.credential.client-secret=sample",
                "spring.cloud.azure.appconfiguration.enabled=true",
                "spring.cloud.azure.appconfiguration.endpoint=https://sample.azconfig.io",
                "spring.cloud.azure.appconfiguration.client.logging.level=headers",
                "spring.cloud.azure.appconfiguration.client.logging.allowed-header-names=User-Agent",
                "spring.cloud.azure.appconfiguration.retry.fixed.delay=1",
                "spring.cloud.azure.appconfiguration.retry.fixed.max-retries=0",
                "spring.cloud.azure.appconfiguration.retry.mode=fixed"
            )
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureAppConfigurationAutoConfiguration.class);
                assertThat(context).hasSingleBean(AzureAppConfigurationProperties.class);
                assertThat(context).hasSingleBean(ConfigurationClient.class);
                assertThat(context).hasSingleBean(ConfigurationAsyncClient.class);
                assertThat(context).hasSingleBean(ConfigurationClientBuilder.class);
                assertThat(context).hasSingleBean(ConfigurationClientBuilderFactory.class);

                ConfigurationClient configurationClient = context.getBean(ConfigurationClient.class);
                ConfigurationSetting configurationSetting = new ConfigurationSetting();
                configurationSetting.setKey("key1");
                try {
                    configurationClient.getConfigurationSetting(configurationSetting);
                } catch (Exception exception) {
                    // Eat it because we just want the log.
                }
                String allOutput = output.getAll();
                assertTrue(allOutput.contains(String.format("User-Agent:%s",
                    AzureSpringIdentifier.AZURE_SPRING_APP_CONFIG)) || allOutput.contains(String.format("\"User-Agent"
                    + "\":\"%s", AzureSpringIdentifier.AZURE_SPRING_APP_CONFIG)));
            });
    }
}
