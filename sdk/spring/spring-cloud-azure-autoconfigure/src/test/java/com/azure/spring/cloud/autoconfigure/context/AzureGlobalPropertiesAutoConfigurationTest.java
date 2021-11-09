// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.context;

import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AzureGlobalPropertiesAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureGlobalPropertiesAutoConfiguration.class));

    @Test
    void testAutoConfiguration() {
        this.contextRunner.run(context -> {
            assertThat(context).hasSingleBean(AzureGlobalPropertiesAutoConfiguration.class);
            assertThat(context).hasSingleBean(AzureGlobalProperties.class);
        });
    }

}
