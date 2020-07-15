/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.web;

import static com.microsoft.azure.spring.cloud.config.web.TestConstants.CONN_STRING_PROP;
import static com.microsoft.azure.spring.cloud.config.web.TestConstants.STORE_ENDPOINT_PROP;
import static com.microsoft.azure.spring.cloud.config.web.TestConstants.TEST_CONN_STRING;
import static com.microsoft.azure.spring.cloud.config.web.TestConstants.TEST_STORE_NAME;
import static com.microsoft.azure.spring.cloud.config.web.TestUtils.propPair;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.cloud.bus.BusProperties;
import org.springframework.cloud.bus.event.RefreshRemoteApplicationEvent;
import org.springframework.cloud.endpoint.RefreshEndpoint;

import com.microsoft.azure.spring.cloud.config.AppConfigurationAutoConfiguration;
import com.microsoft.azure.spring.cloud.config.AppConfigurationBootstrapConfiguration;

public class AppConfigurationWebAutoConfigurationTest {
    private static final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues(propPair(CONN_STRING_PROP, TEST_CONN_STRING),
                    propPair(STORE_ENDPOINT_PROP, TEST_STORE_NAME))
            .withConfiguration(AutoConfigurations.of(AppConfigurationBootstrapConfiguration.class,
                    AppConfigurationAutoConfiguration.class, AppConfigurationWebAutoConfiguration.class,
                    RefreshAutoConfiguration.class))
            .withUserConfiguration(BusProperties.class);

    @Test
    public void refreshMissing() {
        contextRunner
                .withClassLoader(new FilteredClassLoader(WebEndpointProperties.class))
                .run(context -> {
                    assertThat(context)
                            .doesNotHaveBean("appConfigurationRefreshBusEndpoint");
                    assertThat(context)
                            .doesNotHaveBean("appConfigurationRefreshEndpoint");
                    assertThat(context)
                            .hasBean("configListener");
                });
    }

    @Test
    public void busRefreshMissing() {
        contextRunner
                .withClassLoader(new FilteredClassLoader(RefreshRemoteApplicationEvent.class))
                .run(context -> {
                    assertThat(context)
                            .doesNotHaveBean("appConfigurationBusRefreshEndpoint");
                    assertThat(context)
                            .hasBean("appConfigurationRefreshEndpoint");
                    assertThat(context)
                            .hasBean("configListener");
                });
    }

    @Test
    public void pullRefreshListenerMissing() {
        contextRunner.withClassLoader(new FilteredClassLoader(RefreshEndpoint.class))
                .run(context -> assertThat(context)
                        .doesNotHaveBean("configListener"));
    }

    @Test
    public void fullRefresh() {
        contextRunner
                .run(context -> {
                    assertThat(context)
                            .hasBean("configListener");
                    assertThat(context)
                            .hasBean("appConfigurationRefreshEndpoint");
                });
    }
}
