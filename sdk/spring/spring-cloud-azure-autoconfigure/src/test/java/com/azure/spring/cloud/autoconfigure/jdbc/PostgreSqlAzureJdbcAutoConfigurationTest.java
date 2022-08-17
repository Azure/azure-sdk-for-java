// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.jdbc;

import com.azure.identity.AzureAuthorityHosts;
import com.azure.identity.providers.jdbc.enums.AuthProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.test.context.FilteredClassLoader;

import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.POSTGRESQL_PROPERTY_NAME_AUTHENTICATION_PLUGIN_CLASSNAME;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.POSTGRESQL_PROPERTY_NAME_SSL_MODE;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.POSTGRESQL_PROPERTY_VALUE_SSL_MODE;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.POSTGRES_AUTH_PLUGIN_CLASS_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PostgreSqlAzureJdbcAutoConfigurationTest extends AbstractAzureJdbcAutoConfigurationTest {
    private static final String POSTGRESQL_SSLMODE_PROPERTY
        = POSTGRESQL_PROPERTY_NAME_SSL_MODE + "=" + POSTGRESQL_PROPERTY_VALUE_SSL_MODE;
    private static final String POSTGRESQL_AUTHENTICATIONPLUGINCLASSNAME_PROPERTY
        = POSTGRESQL_PROPERTY_NAME_AUTHENTICATION_PLUGIN_CLASSNAME + "=" + POSTGRES_AUTH_PLUGIN_CLASS_NAME;
    private static final String AUTHPROPERTY_MANAGEDIDENTITYENABLED_PROPERTY
        = AuthProperty.MANAGED_IDENTITY_ENABLED.getPropertyKey() + "=" + "false";
    private static final String AUTHPROPERTY_CACHEENABLED_PROPERTY
        = AuthProperty.CACHE_ENABLED.getPropertyKey() + "=" + "true";
    private static final String AUTHPROPERTY_AUTHORITYHOST_PROPERTY
        = AuthProperty.AUTHORITY_HOST.getPropertyKey() + "=" + AzureAuthorityHosts.AZURE_PUBLIC_CLOUD;


    @Override
    void pluginNotOnClassPath() {

        String connectionString = "jdbc:postgresql://postgre:5432/test";

        this.contextRunner
            .withPropertyValues("spring.datasource.url = " + connectionString)
            .withClassLoader(new FilteredClassLoader("org.postgresql.plugin.AuthenticationPlugin"))
            .run((context) -> {
                DataSourceProperties dataSourceProperties = context.getBean(DataSourceProperties.class);
                assertEquals(connectionString, dataSourceProperties.getUrl());
            });
    }

    @Override
    void wrongJdbcUrl() {
        String connectionString = "jdbc:postgr://postgre:5432/test";
        this.contextRunner
            .withPropertyValues("spring.datasource.url = " + connectionString)
            .run((context) -> {
                DataSourceProperties dataSourceProperties = context.getBean(DataSourceProperties.class);
                assertEquals(connectionString, dataSourceProperties.getUrl());
            });
    }

    @Override
    void enhanceUrl() {
        String connectionString = "jdbc:postgresql://postgre:5432/test";
        this.contextRunner
            .withPropertyValues("spring.datasource.url = " + connectionString)
            .run((context) -> {
                DataSourceProperties dataSourceProperties = context.getBean(DataSourceProperties.class);
                String expectedUrl = String.format("%s?%s&%s&%s&%s&%s", connectionString,
                    POSTGRESQL_SSLMODE_PROPERTY,
                    POSTGRESQL_AUTHENTICATIONPLUGINCLASSNAME_PROPERTY,
                    AUTHPROPERTY_MANAGEDIDENTITYENABLED_PROPERTY,
                    AUTHPROPERTY_CACHEENABLED_PROPERTY,
                    AUTHPROPERTY_AUTHORITYHOST_PROPERTY
                );
                assertEquals(expectedUrl, dataSourceProperties.getUrl());
            });
    }
}
