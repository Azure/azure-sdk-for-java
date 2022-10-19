// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.jdbc;

import com.azure.identity.providers.jdbc.implementation.enums.AuthProperty;
import com.azure.identity.providers.mysql.AzureIdentityMysqlAuthenticationPlugin;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.context.AzureTokenCredentialAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.jdbc.DatabaseType;
import com.azure.spring.cloud.autoconfigure.implementation.jdbc.SpringTokenCredentialProviderContextProvider;
import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import com.azure.spring.cloud.service.implementation.identity.credential.provider.SpringTokenCredentialProvider;
import com.azure.spring.cloud.service.implementation.passwordless.AzurePasswordlessProperties;
import com.mysql.cj.conf.PropertyKey;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.env.ConfigurableEnvironment;

import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcConnectionStringUtils.enhanceJdbcUrl;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class JdbcPropertiesBeanPostProcessorWithApplicationContextRunnerTest {

    private static final String MYSQL_CONNECTION_STRING = "jdbc:mysql://host/database?enableSwitch1&property1=value1";


    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureJdbcAutoConfiguration.class,
            DataSourceProperties.class,
            AzurePasswordlessProperties.class,
            AzureGlobalPropertiesAutoConfiguration.class,
            AzureTokenCredentialAutoConfiguration.class));

    @Test
    void mySqlAuthPluginNotOnClassPath() {
        contextRunner
            .withClassLoader(new FilteredClassLoader(AzureIdentityMysqlAuthenticationPlugin.class))
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
                        PropertyKey.connectionAttributes.getKeyName()  + "=_extension_version:" + AzureSpringIdentifier.AZURE_SPRING_MYSQL_OAUTH,
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
                AzurePasswordlessProperties properties = Binder.get(environment).bindOrCreate("spring.datasource.azure", AzurePasswordlessProperties.class);

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
