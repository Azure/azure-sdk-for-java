// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.jdbc;

import com.azure.identity.extensions.implementation.enums.AuthProperty;
import com.azure.identity.extensions.implementation.template.AzureAuthenticationTemplate;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.context.AzureTokenCredentialAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.jdbc.SpringTokenCredentialProviderContextProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class AbstractAzureJdbcAutoConfigurationTest {

    static final String PUBLIC_AUTHORITY_HOST_STRING = AuthProperty.AUTHORITY_HOST.getPropertyKey() + "=" + "https://login.microsoftonline.com/";
    static final String PUBLIC_TOKEN_CREDENTIAL_BEAN_NAME_STRING = AuthProperty.TOKEN_CREDENTIAL_BEAN_NAME.getPropertyKey() + "=" + "passwordlessTokenCredential";
    abstract String getPluginClassName();
    abstract String getWrongJdbcUrl();
    abstract String getCorrectJdbcUrl();
    abstract String getCorrectJdbcUrlWithProperties(Map<String, String> properties);
    abstract String getExpectedEnhancedUrlWithDefaultCredential(String baseUrlWithoutProperties);
    abstract String getExpectedEnhancedUrlWithCustomizedCredential(String baseUrlWithoutProperties);

    protected final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureJdbcAutoConfiguration.class,
            AzureTokenCredentialAutoConfiguration.class,
            AzureGlobalPropertiesAutoConfiguration.class,
            DataSourceAutoConfiguration.class));

    @Test
    void testEnhanceUrlDefaultCredential() {
        String connectionString = getCorrectJdbcUrl();
        this.contextRunner
            .withPropertyValues(
                "spring.datasource.url=" + connectionString,
                "spring.datasource.azure.passwordlessEnabled=true"
            )
            .run((context) -> {
                DataSourceProperties dataSourceProperties = context.getBean(DataSourceProperties.class);

                String expectedUrl = getExpectedEnhancedUrlWithDefaultCredential(connectionString);
                assertEquals(expectedUrl, dataSourceProperties.getUrl());
            });
    }

    @Test
    void testEnhanceUrlWithCustomCredential() {
        String connectionString = getCorrectJdbcUrl();
        this.contextRunner
            .withPropertyValues(
                "spring.datasource.url=" + connectionString,
                "spring.datasource.azure.passwordlessEnabled=true",
                "spring.datasource.azure.profile.tenantId=fake-tenantId",
                "spring.datasource.azure.credential.clientSecret=fake-clientSecret",
                "spring.datasource.azure.credential.clientId=fake-clientId"
            )
            .run((context) -> {
                DataSourceProperties dataSourceProperties = context.getBean(DataSourceProperties.class);
                String expectedUrl = getExpectedEnhancedUrlWithCustomizedCredential(connectionString);
                assertEquals(expectedUrl, dataSourceProperties.getUrl());
            });
    }

    @Test
    void testJdbcPluginNotOnClasspathShouldNotPostProcess() {
        String connectionString = getCorrectJdbcUrl();
        this.contextRunner
            .withPropertyValues(
                "spring.datasource.url=" + connectionString,
                "spring.datasource.azure.passwordlessEnabled=true"
            )
            .withClassLoader(new FilteredClassLoader(getPluginClassName()))
            .run((context) -> {
                DataSourceProperties dataSourceProperties = context.getBean(DataSourceProperties.class);
                assertEquals(connectionString, dataSourceProperties.getUrl());
            });
    }

    @Test
    void testWrongJdbcUrlShouldNotPostProcess() {
        String connectionString = getWrongJdbcUrl();
        this.contextRunner
            .withPropertyValues(
                "spring.datasource.url=" + connectionString,
                "spring.datasource.azure.passwordlessEnabled=true"
            )
            .run((context) -> {
                DataSourceProperties dataSourceProperties = context.getBean(DataSourceProperties.class);
                assertEquals(connectionString, dataSourceProperties.getUrl());
            });
    }

    @Test
    void testProvidedTokenCredentialBeanShouldBeHonoured() {
        Map<String, String> properties = new HashMap<>();
        properties.put(AuthProperty.TOKEN_CREDENTIAL_BEAN_NAME.getPropertyKey(), "mybean");
        this.contextRunner
            .withPropertyValues(
                "spring.datasource.url=" + getCorrectJdbcUrlWithProperties(properties),
                "spring.datasource.azure.passwordlessEnabled=true"
            )
            .run(context -> {
                DataSourceProperties dataSourceProperties = context.getBean(DataSourceProperties.class);
                assertTrue(dataSourceProperties.getUrl().contains(AuthProperty.TOKEN_CREDENTIAL_BEAN_NAME.getPropertyKey() + "=mybean"));
            });
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
            .withPropertyValues("spring.datasource.azure.passwordlessEnabled=true")
            .run((context) -> {
                assertThat(context).doesNotHaveBean(JdbcPropertiesBeanPostProcessor.class);
                assertThat(context).doesNotHaveBean(SpringTokenCredentialProviderContextProvider.class);
            });
    }

    @Test
    void testNoDataSourcePropertiesBean() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(AzureAuthenticationTemplate.class))
            .withPropertyValues("spring.datasource.azure.passwordlessEnabled=true")
            .run((context) -> {
                assertThat(context).doesNotHaveBean(JdbcPropertiesBeanPostProcessor.class);
                assertThat(context).doesNotHaveBean(SpringTokenCredentialProviderContextProvider.class);
            });
    }

    @Test
    void testUnSupportDatabaseType() {
        this.contextRunner
            .withPropertyValues("spring.datasource.url=jdbc:h2:~/test,sa,password")
            .run((context) -> {
                DataSourceProperties dataSourceProperties = context.getBean(DataSourceProperties.class);
                assertEquals("jdbc:h2:~/test,sa,password", dataSourceProperties.getUrl());
            });
    }

}
