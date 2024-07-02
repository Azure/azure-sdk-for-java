// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jdbc;

import com.azure.identity.extensions.implementation.enums.AuthProperty;
import com.azure.identity.extensions.jdbc.mysql.AzureMysqlAuthenticationPlugin;
import com.azure.spring.cloud.autoconfigure.implementation.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.context.AzureTokenCredentialAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.service.implementation.identity.credential.provider.SpringTokenCredentialProvider;
import com.azure.spring.cloud.autoconfigure.implementation.passwordless.properties.AzureJdbcPasswordlessProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.env.ConfigurableEnvironment;

import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcConnectionStringUtils.enhanceJdbcUrl;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.MySqlAzureJdbcAutoConfigurationTest.MYSQL_USER_AGENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class JdbcPropertiesBeanPostProcessorWithApplicationContextRunnerTest {

    private static final String MYSQL_CONNECTION_STRING = "jdbc:mysql://host/database?enableSwitch1&property1=value1";
    private static final String PUBLIC_AUTHORITY_HOST_STRING = AuthProperty.AUTHORITY_HOST.getPropertyKey() + "=" + "https://login.microsoftonline.com/";
    public static final String PUBLIC_TOKEN_CREDENTIAL_BEAN_NAME_STRING = AuthProperty.TOKEN_CREDENTIAL_BEAN_NAME.getPropertyKey() + "=" + "passwordlessTokenCredential";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureJdbcAutoConfiguration.class,
            DataSourceProperties.class,
            AzureJdbcPasswordlessProperties.class,
            AzureGlobalPropertiesAutoConfiguration.class,
            AzureTokenCredentialAutoConfiguration.class));

    @Test
    void mySqlAuthPluginNotOnClassPath() {
        contextRunner
            .withClassLoader(new FilteredClassLoader(AzureMysqlAuthenticationPlugin.class))
            .withPropertyValues(
                "spring.datasource.azure.passwordless-enabled=true",
                "spring.datasource.url=" + MYSQL_CONNECTION_STRING
            )
            .run(
                context -> {
                    assertThat(context).hasSingleBean(AzureJdbcAutoConfiguration.class);
                    assertThat(context).hasSingleBean(JdbcPropertiesBeanPostProcessor.class);
                    assertThat(context).hasSingleBean(DataSourceProperties.class);
                    DataSourceProperties dataSourceProperties = context.getBean(DataSourceProperties.class);
                    assertEquals(MYSQL_CONNECTION_STRING, dataSourceProperties.getUrl());
                }
            );
    }

    @Test
    void mySqlAuthPluginOnClassPath() {
        contextRunner
            .withPropertyValues(
                "spring.datasource.azure.passwordless-enabled=true",
                "spring.datasource.url=" + MYSQL_CONNECTION_STRING
            )
            .run(
                context -> {
                    assertThat(context).hasSingleBean(AzureJdbcAutoConfiguration.class);
                    assertThat(context).hasSingleBean(JdbcPropertiesBeanPostProcessor.class);
                    assertThat(context).hasSingleBean(DataSourceProperties.class);
                    DataSourceProperties dataSourceProperties = context.getBean(DataSourceProperties.class);

                    String expectedJdbcUrl = enhanceJdbcUrl(
                        DatabaseType.MYSQL,
                        MYSQL_CONNECTION_STRING,
                        PUBLIC_TOKEN_CREDENTIAL_BEAN_NAME_STRING,
                        PUBLIC_AUTHORITY_HOST_STRING,
                        MYSQL_USER_AGENT,
                        AuthProperty.TOKEN_CREDENTIAL_PROVIDER_CLASS_NAME.getPropertyKey() + "=" + SpringTokenCredentialProvider.class.getName()
                    );
                    assertEquals(expectedJdbcUrl, dataSourceProperties.getUrl());
                }
            );
    }

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
                assertThat(context).hasSingleBean(SpringTokenCredentialProviderContextProvider.class);
            });
    }

    @Test
    void testBindSpringBootProperties() {
        this.contextRunner
            .withPropertyValues(
                 "spring.datasource.azure.credential.client-id=fake-jdbc-client-id",
                "spring.cloud.azure.credential.client-id=azure-client-id"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureJdbcAutoConfiguration.class);
                assertThat(context).hasSingleBean(JdbcPropertiesBeanPostProcessor.class);
                assertThat(context).hasSingleBean(SpringTokenCredentialProviderContextProvider.class);

                ConfigurableEnvironment environment = context.getEnvironment();
                AzureJdbcPasswordlessProperties properties = Binder.get(environment).bindOrCreate("spring.datasource.azure", AzureJdbcPasswordlessProperties.class);

                assertNotEquals("azure-client-id", properties.getCredential().getClientId());
                assertEquals("fake-jdbc-client-id", properties.getCredential().getClientId());
            });
    }

    @Test
    void testBindAzureGlobalProperties() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.credential.client-id=azure-client-id"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureJdbcAutoConfiguration.class);
                assertThat(context).hasSingleBean(JdbcPropertiesBeanPostProcessor.class);
                assertThat(context).hasSingleBean(SpringTokenCredentialProviderContextProvider.class);
                assertThat(context).hasSingleBean(AzureGlobalProperties.class);
                AzureGlobalProperties azureGlobalProperties = context.getBean(AzureGlobalProperties.class);

                assertEquals("azure-client-id", azureGlobalProperties.getCredential().getClientId());
            });
    }

}
