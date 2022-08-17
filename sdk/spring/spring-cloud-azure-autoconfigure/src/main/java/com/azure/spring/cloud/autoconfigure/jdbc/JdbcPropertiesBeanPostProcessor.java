// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.jdbc;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.providers.jdbc.enums.AuthProperty;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.jdbc.DatabaseType;
import com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcConnectionString;
import com.azure.spring.cloud.core.implementation.credential.resolver.AzureTokenCredentialResolver;
import com.azure.spring.cloud.service.implementation.credentialfree.AzureCredentialFreeProperties;
import com.azure.spring.cloud.service.implementation.identity.credential.provider.SpringTokenCredentialProvider;
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
import java.util.regex.Pattern;

import static com.azure.spring.cloud.core.implementation.util.AzurePropertiesUtils.copyPropertiesIgnoreNull;

/**
 * {@link BeanPostProcessor} to enhance jdbc connection string.
 */
class JdbcPropertiesBeanPostProcessor implements BeanPostProcessor, EnvironmentAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcPropertiesBeanPostProcessor.class);
    private static final String SPRING_TOKEN_CREDENTIAL_PROVIDER_CLASS_NAME = SpringTokenCredentialProvider.class.getName();
    private static final String SPRING_CLOUD_AZURE_DATASOURCE_PREFIX = "spring.datasource.azure";

    private final AzureGlobalProperties azureGlobalProperties;

    private Environment environment;

    JdbcPropertiesBeanPostProcessor(AzureGlobalProperties azureGlobalProperties) {
        this.azureGlobalProperties = azureGlobalProperties;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DataSourceProperties) {
            DataSourceProperties dataSourceProperties = (DataSourceProperties) bean;

            String url = dataSourceProperties.getUrl();
            if (!StringUtils.hasText(url)) {
                LOGGER.debug("No 'spring.datasource.url' provided, skip enhancing jdbc url.");
                return bean;
            }

            JdbcConnectionString connectionString = JdbcConnectionString.resolve(url);
            if (connectionString == null) {
                LOGGER.debug("Can not resolve jdbc connection string from provided {}, skip enhancing jdbc url.", url);
                return bean;
            }

            boolean isPasswordProvided = StringUtils.hasText(dataSourceProperties.getPassword());

            if (isPasswordProvided) {
                if (isAzureHostedDatabaseService(url)) {
                    LOGGER.info("Azure managed database services with password detected, it is encouraged to use the"
                        + "credential-free feature. Please refer to https://aka.ms/spring/credentail-free.");
                } else {
                    LOGGER.debug("Value of 'spring.datasource.password' is provided, skip enhancing jdbc url.");
                }
                return bean;
            }

            DatabaseType databaseType = connectionString.getDatabaseType();
            if (!databaseType.isDatabasePluginAvailable()) {
                LOGGER.debug("The jdbc plugin with provided jdbc schema is not on the classpath, skip enhancing jdbc url.");
                return bean;
            }

            try {
                AzureCredentialFreeProperties properties = Binder.get(environment)
                    .bindOrCreate(SPRING_CLOUD_AZURE_DATASOURCE_PREFIX, AzureCredentialFreeProperties.class);

                Map<String, String> enhancedProperties = buildEnhancedProperties(databaseType, properties);
                String enhancedUrl = connectionString.enhanceConnectionString(enhancedProperties);
                ((DataSourceProperties) bean).setUrl(enhancedUrl);
            } catch (IllegalArgumentException e) {
                LOGGER.debug("Inconsistent properties detected, skip enhancing jdbc url.");
            }
        }
        return bean;
    }

    private Map<String, String> buildEnhancedProperties(DatabaseType databaseType, AzureCredentialFreeProperties properties) {
        Map<String, String> result = new HashMap<>();
        TokenCredential globalTokenCredential = new AzureTokenCredentialResolver().resolve(azureGlobalProperties);
        TokenCredential credentialFreeTokenCredential = new AzureTokenCredentialResolver().resolve(properties);

        if (globalTokenCredential != null && credentialFreeTokenCredential == null) {
            LOGGER.debug("Add SpringTokenCredentialProvider as the default token credential provider.");
            AuthProperty.TOKEN_CREDENTIAL_PROVIDER_CLASS_NAME.setProperty(result, SPRING_TOKEN_CREDENTIAL_PROVIDER_CLASS_NAME);
        }

        copyPropertiesIgnoreNull(azureGlobalProperties.getProfile(), properties.getProfile());
        copyPropertiesIgnoreNull(azureGlobalProperties.getCredential(), properties.getCredential());

        AuthProperty.CACHE_ENABLED.setProperty(result, "true");
        AuthProperty.CLIENT_ID.setProperty(result, properties.getCredential().getClientId());
        AuthProperty.CLIENT_SECRET.setProperty(result, properties.getCredential().getClientSecret());
        AuthProperty.CLIENT_CERTIFICATE_PATH.setProperty(result, properties.getCredential().getClientCertificatePath());
        AuthProperty.CLIENT_CERTIFICATE_PASSWORD.setProperty(result, properties.getCredential().getClientCertificatePassword());
        AuthProperty.USERNAME.setProperty(result, properties.getCredential().getUsername());
        AuthProperty.PASSWORD.setProperty(result, properties.getCredential().getPassword());
        AuthProperty.MANAGED_IDENTITY_ENABLED.setProperty(result, String.valueOf(properties.getCredential().isManagedIdentityEnabled()));
        AuthProperty.AUTHORITY_HOST.setProperty(result, properties.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        AuthProperty.TENANT_ID.setProperty(result, properties.getProfile().getTenantId());

        databaseType.setDefaultEnhancedProperties(result);

        return result;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    private boolean isAzureHostedDatabaseService(String url) {
        //Global
        return Pattern.matches("^jdbc:mysql://.*.mysql.database.azure.com.*", url)
            || Pattern.matches("^jdbc:postgresql://.*.postgres.database.azure.com.*", url);
    }
}
