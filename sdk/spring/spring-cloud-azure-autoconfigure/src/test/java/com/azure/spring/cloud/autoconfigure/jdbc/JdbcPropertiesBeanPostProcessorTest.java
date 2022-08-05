// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.jdbc;

import com.azure.spring.cloud.autoconfigure.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.context.AzureTokenCredentialAutoConfiguration;
import com.azure.spring.cloud.service.implementation.identity.impl.credential.provider.SpringTokenCredentialProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class JdbcPropertiesBeanPostProcessorTest {

    protected final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureJdbcAutoConfiguration.class,
            AzureGlobalPropertiesAutoConfiguration.class, AzureTokenCredentialAutoConfiguration.class));


    @Test
    void shouldNotConfigureWithoutDataSourceProperties() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(DataSourceProperties.class))
            .run(context -> assertThat(context).doesNotHaveBean(AzureJdbcAutoConfiguration.class));
    }

    @Test
    void shouldConfigure() {
        this.contextRunner
            .run(context -> {
                assertThat(context).hasSingleBean(AzureJdbcAutoConfiguration.class);
                assertThat(context).hasSingleBean(JdbcPropertiesBeanPostProcessor.class);
                assertThat(context).hasSingleBean(SpringTokenCredentialProvider.class);
            });
    }
}
