// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.jdbc;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.providers.jdbc.implementation.enums.AuthProperty;
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
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import java.util.HashMap;
import java.util.Map;

import static com.azure.spring.cloud.service.implementation.identity.credential.provider.SpringTokenCredentialProvider.CREDENTIAL_FREE_TOKEN_BEAN_NAME;


/**
 * {@link BeanPostProcessor} to enhance jdbc connection string.
 */
class JdbcPropertiesBeanPostProcessor implements BeanPostProcessor, EnvironmentAware, ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcPropertiesBeanPostProcessor.class);
    private static final String SPRING_TOKEN_CREDENTIAL_PROVIDER_CLASS_NAME = SpringTokenCredentialProvider.class.getName();
    private static final String SPRING_CLOUD_AZURE_DATASOURCE_PREFIX = "spring.datasource.azure";

    private GenericApplicationContext applicationContext;
    private Environment environment;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DataSourceProperties) {
            DataSourceProperties dataSourceProperties = (DataSourceProperties) bean;

            AzureCredentialFreeProperties properties = Binder.get(environment)
                .bindOrCreate(SPRING_CLOUD_AZURE_DATASOURCE_PREFIX, AzureCredentialFreeProperties.class);
            if (!properties.isCredentialFreeEnabled()) {
                LOGGER.debug("Feature credential free is not enabled, skip enhancing jdbc url.");
                return bean;
            }

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
                LOGGER.debug(
                    "If you are using Azure hosted services,"
                    + "it is encouraged to use the credential-free feature. "
                    + "Please refer to https://aka.ms/credentail-free.");
                return bean;
            }

            DatabaseType databaseType = connectionString.getDatabaseType();
            if (!databaseType.isDatabasePluginAvailable()) {
                LOGGER.debug("The jdbc plugin with provided jdbc schema is not on the classpath, skip enhancing jdbc url.");
                return bean;
            }

            try {
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
        AzureTokenCredentialResolver resolver = applicationContext.getBean(AzureTokenCredentialResolver.class);
        TokenCredential tokenCredential = resolver.resolve(properties);

        if (tokenCredential != null) {
            LOGGER.debug("Add SpringTokenCredentialProvider as the default token credential provider.");
            AuthProperty.TOKEN_CREDENTIAL_BEAN_NAME.setProperty(result, CREDENTIAL_FREE_TOKEN_BEAN_NAME);
            applicationContext.registerBean(CREDENTIAL_FREE_TOKEN_BEAN_NAME, TokenCredential.class, () -> tokenCredential);
        }

        AuthProperty.TOKEN_CREDENTIAL_PROVIDER_CLASS_NAME.setProperty(result, SPRING_TOKEN_CREDENTIAL_PROVIDER_CLASS_NAME);

        databaseType.setDefaultEnhancedProperties(result);

        return result;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (GenericApplicationContext) applicationContext;
    }
}
