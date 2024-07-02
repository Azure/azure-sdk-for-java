// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jdbc;

import com.azure.identity.extensions.implementation.enums.AuthProperty;
import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import com.azure.spring.cloud.service.implementation.identity.credential.provider.SpringTokenCredentialProvider;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.test.context.FilteredClassLoader;

import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.POSTGRESQL_PROPERTY_NAME_APPLICATION_NAME;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.POSTGRESQL_PROPERTY_NAME_ASSUME_MIN_SERVER_VERSION;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.POSTGRESQL_PROPERTY_NAME_AUTHENTICATION_PLUGIN_CLASSNAME;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.POSTGRESQL_PROPERTY_NAME_SSL_MODE;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.POSTGRESQL_PROPERTY_VALUE_ASSUME_MIN_SERVER_VERSION;
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

    private static final String POSTGRESQL_USER_AGENT = POSTGRESQL_PROPERTY_NAME_APPLICATION_NAME + "="
        + AzureSpringIdentifier.AZURE_SPRING_POSTGRESQL_OAUTH;
    private static final String POSTGRESQL_ASSUME_MIN_SERVER_VERSION = POSTGRESQL_PROPERTY_NAME_ASSUME_MIN_SERVER_VERSION + "="
        + POSTGRESQL_PROPERTY_VALUE_ASSUME_MIN_SERVER_VERSION;

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
                String expectedUrl = JdbcConnectionStringUtils.enhanceJdbcUrl(
                    DatabaseType.POSTGRESQL,
                    false,
                    connectionString,
                    PUBLIC_TOKEN_CREDENTIAL_BEAN_NAME_STRING,
                    PUBLIC_AUTHORITY_HOST_STRING,
                    POSTGRESQL_USER_AGENT,
                    AUTHPROPERTY_TOKENCREDENTIALPROVIDERCLASSNAME_PROPERTY,
                    POSTGRESQL_ASSUME_MIN_SERVER_VERSION
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
                String expectedUrl = JdbcConnectionStringUtils.enhanceJdbcUrl(
                    DatabaseType.POSTGRESQL,
                    false,
                    connectionString,
                    PUBLIC_AUTHORITY_HOST_STRING,
                    AUTHPROPERTY_CREDENTIAL_BEAN_NAME,
                    AUTHPROPERTY_TOKENCREDENTIALPROVIDERCLASSNAME_PROPERTY,
                    POSTGRESQL_USER_AGENT,
                    POSTGRESQL_ASSUME_MIN_SERVER_VERSION
                );
                assertEquals(expectedUrl, dataSourceProperties.getUrl());
            });
    }
}
