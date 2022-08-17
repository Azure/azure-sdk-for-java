// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.jdbc;

import com.azure.identity.AzureAuthorityHosts;
import com.azure.identity.providers.jdbc.enums.AuthProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.test.context.FilteredClassLoader;

import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.MYSQL_AUTH_PLUGIN_CLASS_NAME;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.MYSQL_PROPERTY_NAME_AUTHENTICATION_PLUGINS;
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

    private static final String AUTHPROPERTY_MANAGEDIDENTITYENABLED_PROPERTY
        = AuthProperty.MANAGED_IDENTITY_ENABLED.getPropertyKey() + "=" + "false";

    private static final String AUTHPROPERTY_CACHEENABLED_PROPERTY
        = AuthProperty.CACHE_ENABLED.getPropertyKey() + "=" + "true";

    private static final String AUTHPROPERTY_AUTHORITYHOST_PROPERTY
        = AuthProperty.AUTHORITY_HOST.getPropertyKey() + "=" + AzureAuthorityHosts.AZURE_PUBLIC_CLOUD;


    @Override
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
    void enhanceUrl() {
        String connectionString = "jdbc:mysql://mysql:1234/test";

        this.contextRunner
            .withPropertyValues("spring.datasource.url = " + connectionString)
            .run((context) -> {
                DataSourceProperties dataSourceProperties = context.getBean(DataSourceProperties.class);

                String expectedUrl = String.format("%s?%s&%s&%s&%s&%s&%s&%s", connectionString,
                    AUTHPROPERTY_MANAGEDIDENTITYENABLED_PROPERTY,
                    MYSQL_AUTH_PLUGIN_PROPERTY,
                    MYSQL_DEFAULT_PLUGIN_PROPERTY,
                    AUTHPROPERTY_CACHEENABLED_PROPERTY,
                    AUTHPROPERTY_AUTHORITYHOST_PROPERTY,
                    MYSQL_SSL_MODE_PROPERTY,
                    MYSQL_USE_SSL_PROPERTY
                );
                assertEquals(expectedUrl, dataSourceProperties.getUrl());
            });
    }
}
