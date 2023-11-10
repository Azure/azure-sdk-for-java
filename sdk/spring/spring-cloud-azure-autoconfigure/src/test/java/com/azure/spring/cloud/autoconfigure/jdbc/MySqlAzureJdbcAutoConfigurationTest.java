// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.jdbc;

import com.azure.identity.extensions.implementation.enums.AuthProperty;
import com.azure.spring.cloud.autoconfigure.implementation.jdbc.DatabaseType;
import com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcConnectionStringUtils;
import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import com.azure.spring.cloud.service.implementation.identity.credential.provider.SpringTokenCredentialProvider;

import java.util.Map;

import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.MYSQL_PROPERTY_CONNECTION_ATTRIBUTES_ATTRIBUTE_EXTENSION_VERSION;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.MYSQL_PROPERTY_CONNECTION_ATTRIBUTES_KV_DELIMITER;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.MYSQL_PROPERTY_NAME_CONNECTION_ATTRIBUTES;

class MySqlAzureJdbcAutoConfigurationTest extends AbstractAzureJdbcAutoConfigurationTest {

    private static final String AUTHPROPERTY_TOKENCREDENTIALPROVIDERCLASSNAME_PROPERTY
        = AuthProperty.TOKEN_CREDENTIAL_PROVIDER_CLASS_NAME.getPropertyKey() + "=" + SpringTokenCredentialProvider.class.getName();

    private static final String AUTHPROPERTY_CREDENTIAL_BEAN_NAME
        = AuthProperty.TOKEN_CREDENTIAL_BEAN_NAME.getPropertyKey() + "=" + "passwordlessTokenCredential";

    public static final String MYSQL_USER_AGENT = MYSQL_PROPERTY_NAME_CONNECTION_ATTRIBUTES + "="
        + MYSQL_PROPERTY_CONNECTION_ATTRIBUTES_ATTRIBUTE_EXTENSION_VERSION
        + MYSQL_PROPERTY_CONNECTION_ATTRIBUTES_KV_DELIMITER
        + AzureSpringIdentifier.AZURE_SPRING_MYSQL_OAUTH;

    @Override
    String getPluginClassName() {
        return "com.mysql.cj.protocol.AuthenticationPlugin";
    }

    @Override
    String getWrongJdbcUrl() {
        return "jdbc:mys://myql:5432/test";
    }

    @Override
    String getCorrectJdbcUrl() {
        return "jdbc:mysql://mysql:1234/test";
    }

    @Override
    String getCorrectJdbcUrlWithProperties(Map<String, String> properties) {
        return JdbcConnectionStringUtils.enhanceJdbcUrl(
            DatabaseType.MYSQL,
            false,
            getCorrectJdbcUrl(),
            properties
        );
    }

    @Override
    String getExpectedEnhancedUrlWithDefaultCredential(String baseUrlWithoutProperties) {
        return JdbcConnectionStringUtils.enhanceJdbcUrl(
            DatabaseType.MYSQL,
            false,
            baseUrlWithoutProperties,
            PUBLIC_AUTHORITY_HOST_STRING,
            PUBLIC_TOKEN_CREDENTIAL_BEAN_NAME_STRING,
            AUTHPROPERTY_TOKENCREDENTIALPROVIDERCLASSNAME_PROPERTY,
            MYSQL_USER_AGENT
        );
    }

    @Override
    String getExpectedEnhancedUrlWithCustomizedCredential(String baseUrlWithoutProperties) {
        return JdbcConnectionStringUtils.enhanceJdbcUrl(
            DatabaseType.MYSQL,
            false,
            baseUrlWithoutProperties,
            PUBLIC_AUTHORITY_HOST_STRING,
            AUTHPROPERTY_CREDENTIAL_BEAN_NAME,
            AUTHPROPERTY_TOKENCREDENTIALPROVIDERCLASSNAME_PROPERTY,
            MYSQL_USER_AGENT
        );
    }
}
