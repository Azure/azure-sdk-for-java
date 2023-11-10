// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.jdbc;

import com.azure.identity.extensions.implementation.enums.AuthProperty;
import com.azure.spring.cloud.autoconfigure.implementation.jdbc.DatabaseType;
import com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcConnectionStringUtils;
import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import com.azure.spring.cloud.service.implementation.identity.credential.provider.SpringTokenCredentialProvider;

import java.util.Map;

import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.POSTGRESQL_PROPERTY_NAME_APPLICATION_NAME;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.POSTGRESQL_PROPERTY_NAME_ASSUME_MIN_SERVER_VERSION;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.POSTGRESQL_PROPERTY_NAME_AUTHENTICATION_PLUGIN_CLASSNAME;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.POSTGRESQL_PROPERTY_NAME_SSL_MODE;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.POSTGRESQL_PROPERTY_VALUE_ASSUME_MIN_SERVER_VERSION;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.POSTGRESQL_PROPERTY_VALUE_SSL_MODE;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.POSTGRES_AUTH_PLUGIN_CLASS_NAME;

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
    String getPluginClassName() {
        return "org.postgresql.plugin.AuthenticationPlugin";
    }

    @Override
    String getWrongJdbcUrl() {
        return "jdbc:postgr://postgre:5432/test";
    }

    @Override
    String getCorrectJdbcUrl() {
        return "jdbc:postgresql://postgre:5432/test";
    }

    @Override
    String getCorrectJdbcUrlWithProperties(Map<String, String> properties) {
        return JdbcConnectionStringUtils.enhanceJdbcUrl(
            DatabaseType.POSTGRESQL,
            false,
            getCorrectJdbcUrl(),
            properties
        );
    }

    @Override
    String getExpectedEnhancedUrlWithDefaultCredential(String baseUrlWithoutProperties) {
        return JdbcConnectionStringUtils.enhanceJdbcUrl(
            DatabaseType.POSTGRESQL,
            false,
            baseUrlWithoutProperties,
            PUBLIC_TOKEN_CREDENTIAL_BEAN_NAME_STRING,
            PUBLIC_AUTHORITY_HOST_STRING,
            POSTGRESQL_USER_AGENT,
            AUTHPROPERTY_TOKENCREDENTIALPROVIDERCLASSNAME_PROPERTY,
            POSTGRESQL_ASSUME_MIN_SERVER_VERSION
        );
    }

    @Override
    String getExpectedEnhancedUrlWithCustomizedCredential(String baseUrlWithoutProperties) {
        return JdbcConnectionStringUtils.enhanceJdbcUrl(
            DatabaseType.POSTGRESQL,
            false,
            baseUrlWithoutProperties,
            PUBLIC_AUTHORITY_HOST_STRING,
            AUTHPROPERTY_CREDENTIAL_BEAN_NAME,
            AUTHPROPERTY_TOKENCREDENTIALPROVIDERCLASSNAME_PROPERTY,
            POSTGRESQL_USER_AGENT,
            POSTGRESQL_ASSUME_MIN_SERVER_VERSION
        );
    }

}
