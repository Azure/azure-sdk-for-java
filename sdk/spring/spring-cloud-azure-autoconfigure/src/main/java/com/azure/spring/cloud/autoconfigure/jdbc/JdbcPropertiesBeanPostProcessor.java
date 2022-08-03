// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.jdbc;

import com.azure.core.credential.TokenCredential;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.jdbc.DatabaseType;
import com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcConnectionString;
import com.azure.spring.cloud.core.implementation.credential.resolver.AzureTokenCredentialResolver;
import com.azure.spring.cloud.service.implementation.credentialfree.AzureCredentialFreeProperties;
import com.azure.spring.cloud.service.implementation.identity.api.AuthProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.MYSQL_AUTH_PLUGIN_CLASS_NAME;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.POSTGRES_AUTH_PLUGIN_CLASS_NAME;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.PROPERTY_NAME_MYSQL_AUTHENTICATION_PLUGINS;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.PROPERTY_NAME_MYSQL_DEFAULT_AUTHENTICATION_PLUGIN;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.PROPERTY_NAME_MYSQL_SSL_MODE;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.PROPERTY_NAME_MYSQL_USE_SSL;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.PROPERTY_NAME_POSTGRESQL_AUTHENTICATION_PLUGIN_CLASSNAME;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.PROPERTY_NAME_POSTGRESQL_SSL_MODE;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.PROPERTY_VALUE_MYSQL_SSL_MODE;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.PROPERTY_VALUE_MYSQL_USE_SSL;
import static com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcPropertyConstants.PROPERTY_VALUE_POSTGRESQL_SSL_MODE;

/**
 */
class JdbcPropertiesBeanPostProcessor implements BeanPostProcessor, EnvironmentAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcPropertiesBeanPostProcessor.class);
    private static final String SPRING_TOKEN_CREDENTIAL_PROVIDER = "com.azure.spring.cloud.service.implementation.identity.impl.credential.provider.SpringTokenCredentialProvider";

    private final AzureGlobalProperties azureGlobalProperties;

    private Environment environment;

    public JdbcPropertiesBeanPostProcessor(AzureGlobalProperties azureGlobalProperties) {
        this.azureGlobalProperties = azureGlobalProperties;
    }

    private static final Map<String, String> POSTGRES_ENHANCED_PROPERTIES = new TreeMap<>();
    private static final Map<String, String> MYSQL_ENHANCED_PROPERTIES = new TreeMap<>();
    static final Map<DatabaseType, Map<String, String>> DEFAULT_ENHANCED_PROPERTIES = new HashMap<>();

    static {
        POSTGRES_ENHANCED_PROPERTIES.put(PROPERTY_NAME_POSTGRESQL_AUTHENTICATION_PLUGIN_CLASSNAME, POSTGRES_AUTH_PLUGIN_CLASS_NAME);
        POSTGRES_ENHANCED_PROPERTIES.put(PROPERTY_NAME_POSTGRESQL_SSL_MODE, PROPERTY_VALUE_POSTGRESQL_SSL_MODE);

        MYSQL_ENHANCED_PROPERTIES.put(PROPERTY_NAME_MYSQL_SSL_MODE, PROPERTY_VALUE_MYSQL_SSL_MODE);
        MYSQL_ENHANCED_PROPERTIES.put(PROPERTY_NAME_MYSQL_USE_SSL, PROPERTY_VALUE_MYSQL_USE_SSL);
        MYSQL_ENHANCED_PROPERTIES.put(PROPERTY_NAME_MYSQL_DEFAULT_AUTHENTICATION_PLUGIN, MYSQL_AUTH_PLUGIN_CLASS_NAME);
        MYSQL_ENHANCED_PROPERTIES.put(PROPERTY_NAME_MYSQL_AUTHENTICATION_PLUGINS, MYSQL_AUTH_PLUGIN_CLASS_NAME);

        DEFAULT_ENHANCED_PROPERTIES.put(DatabaseType.MYSQL, MYSQL_ENHANCED_PROPERTIES);
        DEFAULT_ENHANCED_PROPERTIES.put(DatabaseType.POSTGRESQL, POSTGRES_ENHANCED_PROPERTIES);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DataSourceProperties) {
            DataSourceProperties dataSourceProperties = Binder.get(environment).bindOrCreate("spring.datasource", DataSourceProperties.class);
            boolean isPasswordProvided = StringUtils.hasText(dataSourceProperties.getPassword());

            if (isPasswordProvided) {
                LOGGER.debug("'spring.datasource.password' provided, skip JdbcPropertiesBeanPostProcessor.");
                return bean;
            }

            String url = dataSourceProperties.getUrl();
            if (!StringUtils.hasText(url)) {
                LOGGER.debug("No 'spring.datasource.url' provided, skip JdbcPropertiesBeanPostProcessor.");
                return bean;
            }

            JdbcConnectionString connectionString = JdbcConnectionString.resolve(url);
            if (connectionString == null) {
                LOGGER.debug("Can not resolve connection string from provided {}, skip JdbcPropertiesBeanPostProcessor.", url);
                return bean;
            }
            DatabaseType databaseType = connectionString.getDatabaseType();
            if (!databaseType.isDatabasePluginEnabled()) {
                LOGGER.info("The jdbc plugin with provided jdbc schema is not on the classpath , skip JdbcPropertiesBeanPostProcessor");
                return bean;
            }

            try {
                AzureCredentialFreeProperties properties = Binder.get(environment).bindOrCreate("spring.datasource.azure", AzureCredentialFreeProperties.class);

                Map<String, String> enhancedProperties = buildEnhancedProperties(databaseType, properties);
                String enhancedUrl = connectionString.enhanceConnectionString(enhancedProperties);
                ((DataSourceProperties) bean).setUrl(enhancedUrl);
            } catch (IllegalArgumentException e) {
                LOGGER.debug("Inconsistent properties detected, skip JdbcPropertiesBeanPostProcessor");
            }
        }
        return bean;
    }

    private Map<String, String> buildEnhancedProperties(DatabaseType databaseType, AzureCredentialFreeProperties properties) {
        Map<String, String> result = new HashMap<>();
        TokenCredential globalTokenCredential = new AzureTokenCredentialResolver().resolve(azureGlobalProperties);
        TokenCredential credentialFreeTokenCredential = new AzureTokenCredentialResolver().resolve(properties);

        if (globalTokenCredential != null && credentialFreeTokenCredential == null) {
            LOGGER.info("Add SpringTokenCredentialProvider as the default token credential provider.");
            AuthProperty.TOKEN_CREDENTIAL_PROVIDER_CLASS_NAME.setProperty(result, SPRING_TOKEN_CREDENTIAL_PROVIDER);
        }

        AuthProperty.CACHE_ENABLED.setProperty(result, "true");
        AuthProperty.CLIENT_ID.setProperty(result, properties.getCredential().getClientId());
        AuthProperty.CLIENT_SECRET.setProperty(result, properties.getCredential().getClientSecret());
        AuthProperty.CLIENT_CERTIFICATE_PATH.setProperty(result, properties.getCredential().getClientCertificatePath());
        AuthProperty.CLIENT_CERTIFICATE_PASSWORD.setProperty(result, properties.getCredential().getClientCertificatePassword());
        AuthProperty.USERNAME.setProperty(result, properties.getCredential().getUsername());
        AuthProperty.PASSWORD.setProperty(result, properties.getCredential().getPassword());
        AuthProperty.MANAGED_IDENTITY_ENABLED.setProperty(result, String.valueOf(properties.getCredential().isManagedIdentityEnabled()));
        AuthProperty.AUTHORITY_HOST.setProperty(result, String.valueOf(properties.getProfile().getEnvironment().getActiveDirectoryEndpoint()));
        AuthProperty.TENANT_ID.setProperty(result, String.valueOf(properties.getProfile().getTenantId()));

        if (DEFAULT_ENHANCED_PROPERTIES.get(databaseType) != null) {
            result.putAll(DEFAULT_ENHANCED_PROPERTIES.get(databaseType));
        }
        return result;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

}
