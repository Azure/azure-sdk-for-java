// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.unity;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class AzurePropertyAutoConfigurationTest {
    private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzurePropertyAutoConfiguration.class));

    @Test
    public void testAutoConfiguration() {
        this.contextRunner.run(context -> {
            assertThat(context).hasSingleBean(AzurePropertyAutoConfiguration.class);
            assertThat(context).hasSingleBean(AzureProperties.class);
        });
    }

    @Test
    public void testAzureProperties() {
        this.contextRunner.withPropertyValues(
            "spring.cloud.azure.credential.client-id=fake-client-id",
            "spring.cloud.azure.credential.client_secret=fake-client-secret",
            "spring.cloud.azure.environment.authorityHost=fake-authority-host",
            "spring.cloud.azure.environment.GRAPH_BASE_URI=fake-graph-base-uri"
            )
            .run(context -> {
                final AzureProperties azureProperties = context.getBean(AzureProperties.class);
                assertThat(azureProperties.getCredential().getClientId()).isEqualTo("fake-client-id");
                assertThat(azureProperties.getCredential().getClientSecret()).isEqualTo("fake-client-secret");
                assertThat(azureProperties.getEnvironment().getAuthorityHost()).isEqualTo("fake-authority-host");
                assertThat(azureProperties.getEnvironment().getGraphBaseUri()).isEqualTo("fake-graph-base-uri");
            });
    }
}
