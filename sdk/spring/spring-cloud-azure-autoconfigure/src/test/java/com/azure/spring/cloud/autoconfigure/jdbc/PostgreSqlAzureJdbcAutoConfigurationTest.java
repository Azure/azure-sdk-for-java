// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.jdbc;

import com.azure.identity.providers.jdbc.implementation.enums.AuthProperty;
import com.azure.spring.cloud.service.implementation.identity.credential.provider.SpringTokenCredentialProvider;
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
    private static final String AUTHPROPERTY_TOKENCREDENTIALPROVIDERCLASSNAME_PROPERTY
        = AuthProperty.TOKEN_CREDENTIAL_PROVIDER_CLASS_NAME.getPropertyKey() + "=" + SpringTokenCredentialProvider.class.getName();
    private static final String AUTHPROPERTY_CREDENTIAL_BEAN_NAME
        = AuthProperty.TOKEN_CREDENTIAL_BEAN_NAME.getPropertyKey() + "=" + "passwordlessTokenCredential";


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
    void enhanceUrlWithDefaultCredential() {
        String connectionString = "jdbc:postgresql://postgre:5432/test";
        this.contextRunner
            .withPropertyValues("spring.datasource.url = " + connectionString)
            .withPropertyValues("spring.datasource.azure.passwordlessEnabled = " + true)
            .run((context) -> {
                DataSourceProperties dataSourceProperties = context.getBean(DataSourceProperties.class);
                String expectedUrl = String.format("%s?%s&%s&%s", connectionString,
                    POSTGRESQL_SSLMODE_PROPERTY,
                    POSTGRESQL_AUTHENTICATIONPLUGINCLASSNAME_PROPERTY,
                    AUTHPROPERTY_TOKENCREDENTIALPROVIDERCLASSNAME_PROPERTY
                );
                assertEquals(expectedUrl, dataSourceProperties.getUrl());
            });
    }

    @Override
    void enhanceUrlWithCustomCredential() {
        String connectionString = "jdbc:postgresql://postgre:5432/test";
        this.contextRunner
            .withPropertyValues("spring.datasource.url = " + connectionString)
            .withPropertyValues("spring.datasource.azure.passwordlessEnabled = " + true)
            .withPropertyValues("spring.datasource.azure.profile.tenantId = " + "fake-tenantId")
            .withPropertyValues("spring.datasource.azure.credential.clientSecret = " + "fake-clientSecret")
            .withPropertyValues("spring.datasource.azure.credential.clientId = " + "fake-clientId")
            .run((context) -> {
                DataSourceProperties dataSourceProperties = context.getBean(DataSourceProperties.class);
                String expectedUrl = String.format("%s?%s&%s&%s&%s", connectionString,
                    POSTGRESQL_SSLMODE_PROPERTY,
                    AUTHPROPERTY_CREDENTIAL_BEAN_NAME,
                    POSTGRESQL_AUTHENTICATIONPLUGINCLASSNAME_PROPERTY,
                    AUTHPROPERTY_TOKENCREDENTIALPROVIDERCLASSNAME_PROPERTY
                );
                assertEquals(expectedUrl, dataSourceProperties.getUrl());
            });
    }
}
