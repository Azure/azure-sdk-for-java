// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.context;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredential;
import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 */
class AzureDefaultTokenCredentialAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureDefaultTokenCredentialAutoConfiguration.class));

    @Test
    void byDefaultShouldConfigure() {
        contextRunner
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureCredentialBuilderFactory.class);
                assertThat(context).hasSingleBean(TokenCredential.class);

                final TokenCredential credential = context.getBean(TokenCredential.class);
                Assertions.assertTrue(credential instanceof DefaultAzureCredential);

            });
    }

}
