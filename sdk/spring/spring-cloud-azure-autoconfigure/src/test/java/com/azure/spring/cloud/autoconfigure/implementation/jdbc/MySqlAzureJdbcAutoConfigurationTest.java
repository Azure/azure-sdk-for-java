// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jdbc;

import com.azure.identity.extensions.implementation.enums.AuthProperty;
import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import com.azure.spring.cloud.service.implementation.identity.credential.provider.SpringTokenCredentialProvider;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.test.context.FilteredClassLoader;

import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.MYSQL_AUTH_PLUGIN_CLASS_NAME;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.MYSQL_PROPERTY_CONNECTION_ATTRIBUTES_ATTRIBUTE_EXTENSION_VERSION;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.MYSQL_PROPERTY_CONNECTION_ATTRIBUTES_KV_DELIMITER;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.MYSQL_PROPERTY_NAME_AUTHENTICATION_PLUGINS;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.MYSQL_PROPERTY_NAME_CONNECTION_ATTRIBUTES;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.MYSQL_PROPERTY_NAME_DEFAULT_AUTHENTICATION_PLUGIN;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.MYSQL_PROPERTY_NAME_SSL_MODE;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.MYSQL_PROPERTY_NAME_USE_SSL;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.MYSQL_PROPERTY_VALUE_SSL_MODE;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.MYSQL_PROPERTY_VALUE_USE_SSL;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MySqlAzureJdbcAutoConfigurationTest extends AbstractAzureJdbcAutoConfigurationTest {
    private static final String MYSQL_AUTH_PLUGIN_PROPERTY
        = MYSQL_PROPERTY_NAME_AUTHENTICATION_PLUGINS + "=" + MYSQL_AUTH_PLUGIN_CLASS_NAME;

    private static final String MYSQL_DEFAULT_PLUGIN_PROPERTY
        = MYSQL_PROPERTY_NAME_DEFAULT_AUTHENTICATION_PLUGIN + "=" + MYSQL_AUTH_PLUGIN_CLASS_NAME;

    private static final String MYSQL_SSL_MODE_PROPERTY
        = MYSQL_PROPERTY_NAME_SSL_MODE + "=" + MYSQL_PROPERTY_VALUE_SSL_MODE;

    private static final String MYSQL_USE_SSL_PROPERTY
        = MYSQL_PROPERTY_NAME_USE_SSL + "=" + MYSQL_PROPERTY_VALUE_USE_SSL;

    private static final String AUTHPROPERTY_TOKENCREDENTIALPROVIDERCLASSNAME_PROPERTY
        = AuthProperty.TOKEN_CREDENTIAL_PROVIDER_CLASS_NAME.getPropertyKey() + "=" + SpringTokenCredentialProvider.class.getName();

    private static final String AUTHPROPERTY_CREDENTIAL_BEAN_NAME
        = AuthProperty.TOKEN_CREDENTIAL_BEAN_NAME.getPropertyKey() + "=" + "passwordlessTokenCredential";

    public static final String MYSQL_USER_AGENT = MYSQL_PROPERTY_NAME_CONNECTION_ATTRIBUTES + "="
        + MYSQL_PROPERTY_CONNECTION_ATTRIBUTES_ATTRIBUTE_EXTENSION_VERSION
        + MYSQL_PROPERTY_CONNECTION_ATTRIBUTES_KV_DELIMITER
        + AzureSpringIdentifier.AZURE_SPRING_MYSQL_OAUTH;

    void pluginNotOnClassPath() {
        String connectionString = "jdbc:mysql://mysql:1234/test";

        this.contextRunner
            .withPropertyValues("spring.datasource.url = " + connectionString)
            .withClassLoader(new FilteredClassLoader("com.mysql.cj.protocol.AuthenticationPlugin"))
            .run((context) -> {
                DataSourceProperties dataSourceProperties = context.getBean(DataSourceProperties.class);
                assertEquals(connectionString, dataSourceProperties.getUrl());
            });
    }

    @Override
    void wrongJdbcUrl() {
        String connectionString = "jdbc:mys://myql:5432/test";
        this.contextRunner
            .withPropertyValues("spring.datasource.url = " + connectionString)
            .run((context) -> {
                DataSourceProperties dataSourceProperties = context.getBean(DataSourceProperties.class);
                assertEquals(connectionString, dataSourceProperties.getUrl());
            });
    }

    @Override
    void enhanceUrlWithDefaultCredential() {
        String connectionString = "jdbc:mysql://mysql:1234/test";

        this.contextRunner
            .withPropertyValues("spring.datasource.url = " + connectionString)
            .withPropertyValues("spring.datasource.azure.passwordlessEnabled = " + true)
            .run((context) -> {
                DataSourceProperties dataSourceProperties = context.getBean(DataSourceProperties.class);

                String expectedUrl = JdbcConnectionStringUtils.enhanceJdbcUrl(
                    DatabaseType.MYSQL,
                    false,
                    connectionString,
                    PUBLIC_TOKEN_CREDENTIAL_BEAN_NAME_STRING,
                    PUBLIC_AUTHORITY_HOST_STRING,
                    AUTHPROPERTY_TOKENCREDENTIALPROVIDERCLASSNAME_PROPERTY,
                    MYSQL_USER_AGENT
                );
                assertEquals(expectedUrl, dataSourceProperties.getUrl());
            });
    }

    @Override
    void enhanceUrlWithCustomCredential() {
        String connectionString = "jdbc:mysql://mysql:1234/test";

        this.contextRunner
            .withPropertyValues("spring.datasource.url = " + connectionString)
            .withPropertyValues("spring.datasource.azure.passwordlessEnabled = " + true)
            .withPropertyValues("spring.datasource.azure.profile.tenantId = " + "fake-tenantId")
            .withPropertyValues("spring.datasource.azure.credential.clientSecret = " + "fake-clientSecret")
            .withPropertyValues("spring.datasource.azure.credential.clientId = " + "fake-clientId")
            .run((context) -> {
                DataSourceProperties dataSourceProperties = context.getBean(DataSourceProperties.class);

                String expectedUrl = JdbcConnectionStringUtils.enhanceJdbcUrl(
                    DatabaseType.MYSQL,
                    false,
                    connectionString,
                    PUBLIC_AUTHORITY_HOST_STRING,
                    AUTHPROPERTY_CREDENTIAL_BEAN_NAME,
                    AUTHPROPERTY_TOKENCREDENTIALPROVIDERCLASSNAME_PROPERTY,
                    MYSQL_USER_AGENT
                );

                assertEquals(expectedUrl, dataSourceProperties.getUrl());
            });
    }

}
