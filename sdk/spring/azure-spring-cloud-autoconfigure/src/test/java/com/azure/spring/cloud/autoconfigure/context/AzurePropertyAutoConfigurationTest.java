// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.context;

import com.azure.core.management.AzureEnvironment;
import com.azure.spring.cloud.autoconfigure.properties.AzureConfigurationProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class AzurePropertyAutoConfigurationTest {

    private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzurePropertyAutoConfiguration.class));

    @Test
    void testAutoConfiguration() {
        this.contextRunner.run(context -> {
            assertThat(context).hasSingleBean(AzurePropertyAutoConfiguration.class);
            assertThat(context).hasSingleBean(AzureConfigurationProperties.class);
        });
    }

    @Test
    void testAzureProperties() {
        this.contextRunner.withPropertyValues(
            "spring.cloud.azure.client.application-id=fake-application-id",
            "spring.cloud.azure.credential.client-id=fake-client-id",
            "spring.cloud.azure.credential.client-secret=fake-client-secret",
            "spring.cloud.azure.credential.username=fake-username",
            "spring.cloud.azure.credential.password=fake-password",
            "spring.cloud.azure.proxy.hostname=proxy-host",
            "spring.cloud.azure.proxy.port=8888",
            "spring.cloud.azure.retry.timeout=200s",
            "spring.cloud.azure.retry.backoff.delay=20s",
            "spring.cloud.azure.profile.tenant-id=fake-tenant-id",
            "spring.cloud.azure.profile.subscription-id=fake-sub-id",
            "spring.cloud.azure.profile.cloud=azure_china"
            )
            .run(context -> {
                final AzureConfigurationProperties azureProperties = context.getBean(AzureConfigurationProperties.class);
                assertThat(azureProperties).extracting("client.applicationId").isEqualTo("fake-application-id");
                assertThat(azureProperties).extracting("credential.clientId").isEqualTo("fake-client-id");
                assertThat(azureProperties).extracting("credential.clientSecret").isEqualTo("fake-client-secret");
                assertThat(azureProperties).extracting("credential.username").isEqualTo("fake-username");
                assertThat(azureProperties).extracting("credential.password").isEqualTo("fake-password");
                assertThat(azureProperties).extracting("proxy.hostname").isEqualTo("proxy-host");
                assertThat(azureProperties).extracting("proxy.port").isEqualTo(8888);
                assertThat(azureProperties).extracting("retry.timeout").isEqualTo(Duration.ofSeconds(200));
                assertThat(azureProperties).extracting("retry.backoff.delay").isEqualTo(Duration.ofSeconds(20));
                assertThat(azureProperties).extracting("profile.tenantId").isEqualTo("fake-tenant-id");
                assertThat(azureProperties).extracting("profile.subscriptionId").isEqualTo("fake-sub-id");
                assertThat(azureProperties).extracting("profile.cloud").isEqualTo("azure_china");
                assertThat(azureProperties).extracting("profile.environment.activeDirectoryEndpoint").isEqualTo(
                    AzureEnvironment.AZURE_CHINA.getActiveDirectoryEndpoint());
            });
    }
}
