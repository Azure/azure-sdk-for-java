// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.cloud.config.web;

import static com.microsoft.azure.spring.cloud.config.web.TestConstants.CONN_STRING_PROP;
import static com.microsoft.azure.spring.cloud.config.web.TestConstants.STORE_ENDPOINT_PROP;
import static com.microsoft.azure.spring.cloud.config.web.TestConstants.TEST_CONN_STRING;
import static com.microsoft.azure.spring.cloud.config.web.TestConstants.TEST_STORE_NAME;
import static com.microsoft.azure.spring.cloud.config.web.TestUtils.propPair;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.microsoft.azure.spring.cloud.config.AppConfigurationAutoConfiguration;
import com.microsoft.azure.spring.cloud.config.AppConfigurationBootstrapConfiguration;

public class AppConfigurationWebAutoConfigurationTest {
    private static final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues(propPair(CONN_STRING_PROP, TEST_CONN_STRING),
                    propPair(STORE_ENDPOINT_PROP, TEST_STORE_NAME))
            .withConfiguration(AutoConfigurations.of(AppConfigurationBootstrapConfiguration.class,
                    AppConfigurationAutoConfiguration.class, AppConfigurationWebAutoConfiguration.class));

    @Test
    public void watchEnabledNotConfiguredShouldNotCreateWatch() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(ConfigListener.class);
        });
    }
}
