// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jdbc;

import com.azure.identity.extensions.implementation.enums.AuthProperty;
import com.azure.identity.extensions.implementation.template.AzureAuthenticationTemplate;
import com.azure.spring.cloud.autoconfigure.implementation.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.context.AzureTokenCredentialAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

abstract class AbstractAzureJdbcAutoConfigurationTest {

    public static final String PUBLIC_AUTHORITY_HOST_STRING = AuthProperty.AUTHORITY_HOST.getPropertyKey() + "=" + "https://login.microsoftonline.com/";
    public static final String PUBLIC_TOKEN_CREDENTIAL_BEAN_NAME_STRING = AuthProperty.TOKEN_CREDENTIAL_BEAN_NAME.getPropertyKey() + "=" + "passwordlessTokenCredential";
    abstract void pluginNotOnClassPath();
    abstract void wrongJdbcUrl();
    abstract void enhanceUrlWithDefaultCredential();
    abstract void enhanceUrlWithCustomCredential();

    protected final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureJdbcAutoConfiguration.class,
            AzureTokenCredentialAutoConfiguration.class,
            AzureGlobalPropertiesAutoConfiguration.class,
            DataSourceAutoConfiguration.class));

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
