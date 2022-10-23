// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.jdbc;

import com.azure.identity.providers.jdbc.implementation.template.AzureAuthenticationTemplate;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.context.AzureTokenCredentialAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.jdbc.SpringTokenCredentialProviderContextProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

abstract class AbstractAzureJdbcAutoConfigurationTest {

    abstract void pluginNotOnClassPath();
    abstract void wrongJdbcUrl();
    abstract void enhanceUrlWithDefaultCredential();
    abstract void enhanceUrlWithCustomCredential();

    protected final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureJdbcAutoConfiguration.class,
            AzureTokenCredentialAutoConfiguration.class,
            DataSourceAutoConfiguration.class,
            AzureGlobalProperties.class));

    @Test
    void testEnhanceUrlDefaultCredential() {
        enhanceUrlWithDefaultCredential();
    }

    @Test
    void testEnhanceUrlWithCustomCredential() {
        enhanceUrlWithCustomCredential();
    }

    @Test
    void testJdbcPluginNotOnClassPath() {
        pluginNotOnClassPath();
    }

    @Test
    void testWrongJdbcUrl() {
        wrongJdbcUrl();
    }

    @Test
    void testHasSingleBean() {
        this.contextRunner
            .run((context) -> {
                assertThat(context).hasSingleBean(JdbcPropertiesBeanPostProcessor.class);
                assertThat(context).hasSingleBean(SpringTokenCredentialProviderContextProvider.class);
            });
    }

    @Test
    void testNoAzureAuthenticationTemplate() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(AzureAuthenticationTemplate.class))
            .run((context) -> {
                assertThat(context).doesNotHaveBean(JdbcPropertiesBeanPostProcessor.class);
                assertThat(context).doesNotHaveBean(SpringTokenCredentialProviderContextProvider.class);
            });
    }

    @Test
    void testNoDataSourcePropertiesBean() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(AzureAuthenticationTemplate.class))
            .run((context) -> {
                assertThat(context).doesNotHaveBean(JdbcPropertiesBeanPostProcessor.class);
                assertThat(context).doesNotHaveBean(SpringTokenCredentialProviderContextProvider.class);
            });
    }

    @Test
    void testUnSupportDatabaseType() {
        this.contextRunner
            .withPropertyValues("spring.datasource.url = jdbc:h2:~/test,sa,password")
            .run((context) -> {
                DataSourceProperties dataSourceProperties = context.getBean(DataSourceProperties.class);
                assertEquals("jdbc:h2:~/test,sa,password", dataSourceProperties.getUrl());
            });
    }

}
