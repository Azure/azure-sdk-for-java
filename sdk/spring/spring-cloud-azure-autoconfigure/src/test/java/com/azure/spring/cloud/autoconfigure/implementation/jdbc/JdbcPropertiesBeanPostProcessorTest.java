// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jdbc;

import com.azure.identity.extensions.implementation.enums.AuthProperty;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.core.authentication.TokenCredentialConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.core.profile.AzureProfileConfigurationProperties;
import com.azure.spring.cloud.core.implementation.credential.resolver.AzureTokenCredentialResolver;
import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;
import com.azure.spring.cloud.service.implementation.identity.credential.provider.SpringTokenCredentialProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.env.MockEnvironment;

import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcConnectionStringUtils.enhanceJdbcUrl;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.POSTGRESQL_PROPERTY_NAME_ASSUME_MIN_SERVER_VERSION;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.POSTGRESQL_PROPERTY_VALUE_ASSUME_MIN_SERVER_VERSION;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.MySqlAzureJdbcAutoConfigurationTest.MYSQL_USER_AGENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.postgresql.PGProperty.APPLICATION_NAME;

class JdbcPropertiesBeanPostProcessorTest {

    private static final String MYSQL_CONNECTION_STRING = "jdbc:mysql://host/database?enableSwitch1&property1=value1";
    private static final String POSTGRESQL_CONNECTION_STRING = "jdbc:postgresql://host/database?enableSwitch1&property1=value1";
    private static final String PASSWORD = "password";
    private static final String US_AUTHORITY_HOST_STRING = AuthProperty.AUTHORITY_HOST.getPropertyKey() + "=" + "https://login.microsoftonline.us/";
    public static final String PUBLIC_TOKEN_CREDENTIAL_BEAN_NAME_STRING = AuthProperty.TOKEN_CREDENTIAL_BEAN_NAME.getPropertyKey() + "=" + "passwordlessTokenCredential";
    private static final String POSTGRESQL_ASSUME_MIN_SERVER_VERSION = POSTGRESQL_PROPERTY_NAME_ASSUME_MIN_SERVER_VERSION + "="
        + POSTGRESQL_PROPERTY_VALUE_ASSUME_MIN_SERVER_VERSION;

    private MockEnvironment mockEnvironment;

    private ApplicationContext applicationContext;
    private AzureGlobalProperties azureGlobalProperties;
    private JdbcPropertiesBeanPostProcessor jdbcPropertiesBeanPostProcessor;

    @BeforeEach
    void beforeEach() {
        this.mockEnvironment = new MockEnvironment();
        this.applicationContext = mock(GenericApplicationContext.class);
        this.azureGlobalProperties = mock(AzureGlobalProperties.class);
        when(this.azureGlobalProperties.getProfile()).thenReturn(new AzureProfileConfigurationProperties());
        when(this.azureGlobalProperties.getCredential()).thenReturn(new TokenCredentialConfigurationProperties());
        when(this.applicationContext.getBean(AzureTokenCredentialResolver.class)).thenReturn(new AzureTokenCredentialResolver());
        when(this.applicationContext.getBean(AzureGlobalProperties.class)).thenReturn(azureGlobalProperties);
        this.jdbcPropertiesBeanPostProcessor = new JdbcPropertiesBeanPostProcessor();
        jdbcPropertiesBeanPostProcessor.setEnvironment(this.mockEnvironment);
        jdbcPropertiesBeanPostProcessor.setApplicationContext(this.applicationContext);
    }

    @Test
    void testProvidePassword() {
        DataSourceProperties dataSourceProperties = new DataSourceProperties();
        dataSourceProperties.setPassword(PASSWORD);

        this.jdbcPropertiesBeanPostProcessor.postProcessBeforeInitialization(dataSourceProperties, "dataSourceProperties");

        assertNull(dataSourceProperties.getUrl());
    }

    @Test
    void testNoURL() {
        DataSourceProperties dataSourceProperties = new DataSourceProperties();

        this.jdbcPropertiesBeanPostProcessor.postProcessBeforeInitialization(dataSourceProperties, "dataSourceProperties");

        assertNull(dataSourceProperties.getUrl());
    }

    @Test
    void shouldNotPostprocessWhenSwitchOff() {
        DataSourceProperties dataSourceProperties = new DataSourceProperties();
        dataSourceProperties.setUrl(MYSQL_CONNECTION_STRING);

        this.jdbcPropertiesBeanPostProcessor.postProcessBeforeInitialization(dataSourceProperties, "dataSourceProperties");

        assertEquals(MYSQL_CONNECTION_STRING, dataSourceProperties.getUrl());
    }

    @Test
    void shouldPostprocessWhenSwitchOn() {
        DataSourceProperties dataSourceProperties = new DataSourceProperties();
        dataSourceProperties.setUrl(MYSQL_CONNECTION_STRING);

        this.mockEnvironment.setProperty("spring.datasource.azure.passwordless-enabled", "true");
        this.jdbcPropertiesBeanPostProcessor.postProcessBeforeInitialization(dataSourceProperties, "dataSourceProperties");

        String expectedJdbcUrl = enhanceJdbcUrl(
            DatabaseType.MYSQL,
            MYSQL_CONNECTION_STRING,
            PUBLIC_TOKEN_CREDENTIAL_BEAN_NAME_STRING,
            MYSQL_USER_AGENT,
            AuthProperty.TOKEN_CREDENTIAL_PROVIDER_CLASS_NAME.getPropertyKey() + "=" + SpringTokenCredentialProvider.class.getName()
        );

        assertEquals(expectedJdbcUrl, dataSourceProperties.getUrl());
    }

    @Test
    void shouldGetCloudTypeFromAzureUsGov() {
        AzureProfileConfigurationProperties azureProfileConfigurationProperties = new AzureProfileConfigurationProperties();
        azureProfileConfigurationProperties.setCloudType(AzureProfileOptionsProvider.CloudType.AZURE_US_GOVERNMENT);
        when(this.azureGlobalProperties.getProfile()).thenReturn(azureProfileConfigurationProperties);

        DataSourceProperties dataSourceProperties = new DataSourceProperties();
        dataSourceProperties.setUrl(MYSQL_CONNECTION_STRING);

        this.mockEnvironment.setProperty("spring.datasource.azure.passwordless-enabled", "true");
        this.jdbcPropertiesBeanPostProcessor.postProcessBeforeInitialization(dataSourceProperties, "dataSourceProperties");

        String expectedJdbcUrl = enhanceJdbcUrl(
                DatabaseType.MYSQL,
                MYSQL_CONNECTION_STRING,
                MYSQL_USER_AGENT,
                AuthProperty.TOKEN_CREDENTIAL_PROVIDER_CLASS_NAME.getPropertyKey() + "=" + SpringTokenCredentialProvider.class.getName(),
                US_AUTHORITY_HOST_STRING,
                PUBLIC_TOKEN_CREDENTIAL_BEAN_NAME_STRING
        );

        assertEquals(expectedJdbcUrl, dataSourceProperties.getUrl());
    }

    @Test
    void mySqlUserAgentShouldConfigureIfConnectionAttributesIsEmpty() {
        DataSourceProperties dataSourceProperties = new DataSourceProperties();
        dataSourceProperties.setUrl(MYSQL_CONNECTION_STRING);

        this.mockEnvironment.setProperty("spring.datasource.azure.passwordless-enabled", "true");
        this.jdbcPropertiesBeanPostProcessor.postProcessBeforeInitialization(dataSourceProperties, "dataSourceProperties");

        String expectedJdbcUrl = enhanceJdbcUrl(
            DatabaseType.MYSQL,
            MYSQL_CONNECTION_STRING,
            PUBLIC_TOKEN_CREDENTIAL_BEAN_NAME_STRING,
            MYSQL_USER_AGENT,
            AuthProperty.TOKEN_CREDENTIAL_PROVIDER_CLASS_NAME.getPropertyKey() + "=" + SpringTokenCredentialProvider.class.getName()
        );

        assertEquals(expectedJdbcUrl, dataSourceProperties.getUrl());
    }

    @Test
    void mySqlUserAgentShouldConfigureIfConnectionAttributesIsNotEmpty() {
        DataSourceProperties dataSourceProperties = new DataSourceProperties();
        String baseUrl = MYSQL_CONNECTION_STRING
            + DatabaseType.MYSQL.getQueryDelimiter()
            + "connectionAttributes=attr1:val1";
        dataSourceProperties.setUrl(baseUrl);

        this.mockEnvironment.setProperty("spring.datasource.azure.passwordless-enabled", "true");
        this.jdbcPropertiesBeanPostProcessor.postProcessBeforeInitialization(dataSourceProperties, "dataSourceProperties");

        String expectedJdbcUrl = enhanceJdbcUrl(
            DatabaseType.MYSQL,
            baseUrl + ",_extension_version:" + AzureSpringIdentifier.AZURE_SPRING_MYSQL_OAUTH,
            AuthProperty.TOKEN_CREDENTIAL_PROVIDER_CLASS_NAME.getPropertyKey() + "=" + SpringTokenCredentialProvider.class.getName(),
            PUBLIC_TOKEN_CREDENTIAL_BEAN_NAME_STRING
        );

        assertEquals(expectedJdbcUrl, dataSourceProperties.getUrl());
    }

    @Test
    void mySqlUserAgentShouldConfigureIfConnectionAttributes() {
        DataSourceProperties dataSourceProperties = new DataSourceProperties();
        String baseUrl = MYSQL_CONNECTION_STRING
            + DatabaseType.MYSQL.getQueryDelimiter()
            + "connectionAttributes=attr1:val1";
        dataSourceProperties.setUrl(baseUrl);

        this.mockEnvironment.setProperty("spring.datasource.azure.passwordless-enabled", "true");
        this.jdbcPropertiesBeanPostProcessor.postProcessBeforeInitialization(dataSourceProperties, "dataSourceProperties");

        String expectedJdbcUrl = enhanceJdbcUrl(
            DatabaseType.MYSQL,
            baseUrl + ",_extension_version:" + AzureSpringIdentifier.AZURE_SPRING_MYSQL_OAUTH,
            AuthProperty.TOKEN_CREDENTIAL_PROVIDER_CLASS_NAME.getPropertyKey() + "=" + SpringTokenCredentialProvider.class.getName(),
            PUBLIC_TOKEN_CREDENTIAL_BEAN_NAME_STRING
        );
        assertEquals(expectedJdbcUrl, dataSourceProperties.getUrl());
    }

    @Test
    void postgreSqlUserAgentShouldConfigureIfNonApplicationNameProvided() {
        DataSourceProperties dataSourceProperties = new DataSourceProperties();
        String baseUrl = POSTGRESQL_CONNECTION_STRING;
        dataSourceProperties.setUrl(baseUrl);

        this.mockEnvironment.setProperty("spring.datasource.azure.passwordless-enabled", "true");
        this.jdbcPropertiesBeanPostProcessor.postProcessBeforeInitialization(dataSourceProperties, "dataSourceProperties");

        String expectedJdbcUrl = enhanceJdbcUrl(
            DatabaseType.POSTGRESQL,
            baseUrl,
            AuthProperty.TOKEN_CREDENTIAL_PROVIDER_CLASS_NAME.getPropertyKey() + "=" + SpringTokenCredentialProvider.class.getName(),
            APPLICATION_NAME.getName() + "=" + AzureSpringIdentifier.AZURE_SPRING_POSTGRESQL_OAUTH,
            PUBLIC_TOKEN_CREDENTIAL_BEAN_NAME_STRING,
            POSTGRESQL_ASSUME_MIN_SERVER_VERSION
        );

        assertEquals(expectedJdbcUrl, dataSourceProperties.getUrl());
    }

    @Test
    void postgreSqlUserAgentShouldNotConfigureIfApplicationNameExists() {
        DataSourceProperties dataSourceProperties = new DataSourceProperties();
        String baseUrl = POSTGRESQL_CONNECTION_STRING
            + DatabaseType.POSTGRESQL.getQueryDelimiter()
            + APPLICATION_NAME.getName() + "=" + APPLICATION_NAME.getDefaultValue();
        dataSourceProperties.setUrl(baseUrl);

        this.mockEnvironment.setProperty("spring.datasource.azure.passwordless-enabled", "true");
        this.jdbcPropertiesBeanPostProcessor.postProcessBeforeInitialization(dataSourceProperties, "dataSourceProperties");

        String expectedJdbcUrl = enhanceJdbcUrl(
            DatabaseType.POSTGRESQL,
            baseUrl,
            AuthProperty.TOKEN_CREDENTIAL_PROVIDER_CLASS_NAME.getPropertyKey() + "=" + SpringTokenCredentialProvider.class.getName(),
            PUBLIC_TOKEN_CREDENTIAL_BEAN_NAME_STRING,
            POSTGRESQL_ASSUME_MIN_SERVER_VERSION
        );

        assertEquals(expectedJdbcUrl, dataSourceProperties.getUrl());
    }

}
