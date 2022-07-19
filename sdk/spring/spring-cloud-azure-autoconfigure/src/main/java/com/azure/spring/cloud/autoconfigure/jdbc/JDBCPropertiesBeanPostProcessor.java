// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.jdbc;

import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.jdbc.AzureJDBCProperties;
import com.azure.spring.cloud.autoconfigure.implementation.jdbc.AzureSpringJDBCPropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static com.azure.spring.cloud.autoconfigure.jdbc.JdbcConnectionStringPropertyConstants.MYSQL_PLUGIN_CLASS_NAME;
import static com.azure.spring.cloud.autoconfigure.jdbc.JdbcConnectionStringPropertyConstants.POSTGRES_PLUGIN_CLASS_NAME;
import static com.azure.spring.cloud.autoconfigure.jdbc.JdbcConnectionStringPropertyConstants.PROPERTY_MYSQL_AUTHENTICATION_PLUGINS;
import static com.azure.spring.cloud.autoconfigure.jdbc.JdbcConnectionStringPropertyConstants.PROPERTY_MYSQL_DEFAULT_AUTHENTICATION_PLUGIN;
import static com.azure.spring.cloud.autoconfigure.jdbc.JdbcConnectionStringPropertyConstants.PROPERTY_MYSQL_SSL_MODE;
import static com.azure.spring.cloud.autoconfigure.jdbc.JdbcConnectionStringPropertyConstants.PROPERTY_MYSQL_USE_SSL;
import static com.azure.spring.cloud.autoconfigure.jdbc.JdbcConnectionStringPropertyConstants.PROPERTY_POSTGRESQL_AUTHENTICATION_PLUGIN_CLASSNAME;
import static com.azure.spring.cloud.autoconfigure.jdbc.JdbcConnectionStringPropertyConstants.PROPERTY_POSTGRESQL_SSL_MODE;
import static com.azure.spring.cloud.autoconfigure.jdbc.JdbcConnectionStringPropertyConstants.VALUE_MYSQL_SSL_MODE;
import static com.azure.spring.cloud.autoconfigure.jdbc.JdbcConnectionStringPropertyConstants.VALUE_MYSQL_USE_SSL;
import static com.azure.spring.cloud.autoconfigure.jdbc.JdbcConnectionStringPropertyConstants.VALUE_POSTGRESQL_SSL_MODE;
import static com.azure.spring.cloud.core.implementation.util.AzurePropertiesUtils.copyPropertiesIgnoreNull;

/**
 */
class JDBCPropertiesBeanPostProcessor implements BeanPostProcessor, EnvironmentAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(JDBCPropertiesBeanPostProcessor.class);

    private final AzureGlobalProperties azureGlobalProperties;

    private Environment environment;

    public JDBCPropertiesBeanPostProcessor(AzureGlobalProperties azureGlobalProperties) {
        this.azureGlobalProperties = azureGlobalProperties;
    }

    private static final Map<String, String> POSTGRES_ENHANCED_PROPERTIES = new TreeMap<>();
    private static final Map<String, String> MYSQL_ENHANCED_PROPERTIES = new TreeMap<>();
    static final Map<DatabaseType, Map<String, String>> ENHANCED_PROPERTIES = new HashMap<>();

    static {
        POSTGRES_ENHANCED_PROPERTIES.put(PROPERTY_POSTGRESQL_AUTHENTICATION_PLUGIN_CLASSNAME, POSTGRES_PLUGIN_CLASS_NAME);
        POSTGRES_ENHANCED_PROPERTIES.put(PROPERTY_POSTGRESQL_SSL_MODE, VALUE_POSTGRESQL_SSL_MODE);

        MYSQL_ENHANCED_PROPERTIES.put(PROPERTY_MYSQL_SSL_MODE, VALUE_MYSQL_SSL_MODE);
        MYSQL_ENHANCED_PROPERTIES.put(PROPERTY_MYSQL_USE_SSL, VALUE_MYSQL_USE_SSL);
        MYSQL_ENHANCED_PROPERTIES.put(PROPERTY_MYSQL_DEFAULT_AUTHENTICATION_PLUGIN, MYSQL_PLUGIN_CLASS_NAME);
        MYSQL_ENHANCED_PROPERTIES.put(PROPERTY_MYSQL_AUTHENTICATION_PLUGINS, MYSQL_PLUGIN_CLASS_NAME);

        ENHANCED_PROPERTIES.put(DatabaseType.MYSQL, MYSQL_ENHANCED_PROPERTIES);
        ENHANCED_PROPERTIES.put(DatabaseType.POSTGRESQL, POSTGRES_ENHANCED_PROPERTIES);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DataSourceProperties) {
            // what if this is sqlserver
            DataSourceProperties dataSourceProperties = Binder.get(environment).bindOrCreate("spring.datasource", DataSourceProperties.class);
            boolean isPasswordProvided = StringUtils.hasText(dataSourceProperties.getPassword());

            if (isPasswordProvided) {
                LOGGER.debug("'spring.datasource.password' provided, skip JDBCPropertiesBeanPostProcessor.");
                return bean;
            }

            String url = dataSourceProperties.getUrl();
            if (!StringUtils.hasText(url)) {
                LOGGER.debug("No 'spring.datasource.url' provided, skip JDBCPropertiesBeanPostProcessor.");
                return bean;
            }

            JdbcConnectionString connectionString = new JdbcConnectionString(url);
            DatabaseType databaseType = connectionString.getDatabaseType();
            if (!isDatabasePluginEnabled(databaseType)) {
                LOGGER.info("The jdbc plugin with provided jdbc schema is not on the classpath , skip JDBCPropertiesBeanPostProcessor");
            }

            try {
                if (ENHANCED_PROPERTIES.containsKey(databaseType)) {
                    AzureJDBCProperties azureJDBCProperties = Binder.get(environment).bindOrCreate("spring.datasource.azure", AzureJDBCProperties.class);
                    copyPropertiesIgnoreNull(azureGlobalProperties.getProfile(), azureJDBCProperties.getProfile());
                    copyPropertiesIgnoreNull(azureGlobalProperties.getCredential(), azureJDBCProperties.getCredential());

                    Map<String, String> configMap = new HashMap<>();
                    AzureSpringJDBCPropertiesUtils.convertAzurePropertiesToConfigMap(azureJDBCProperties, configMap);
                    configMap.putAll(ENHANCED_PROPERTIES.get(databaseType));
                    String enhancedUrl = connectionString.enhanceConnectionString(configMap);
                    LOGGER.debug("Enhanced url is " + enhancedUrl);

                    ((DataSourceProperties) bean).setUrl(enhancedUrl);
                }
            } catch (IllegalStateException e) {
                LOGGER.debug("Inconsistent properties detected, skip JDBCPropertiesBeanPostProcessor");
            }
        }
        return bean;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    private boolean isDatabasePluginEnabled(DatabaseType databaseType){
        if (DatabaseType.POSTGRESQL.equals(databaseType)) {
            return isPostgresqlPluginEnabled();
        }else if (DatabaseType.MYSQL.equals(databaseType)){
            return isMySqlPluginEnabled();
        }
        return false;
    }

    private boolean isPostgresqlPluginEnabled() {
        return isOnClasspath("com.azure.spring.cloud.autoconfigure.jdbc.extension.postgresql.AzureIdentityPostgresqlAuthenticationPlugin")
            && isOnClasspath("org.postgresql.Driver");
    }

    private boolean isMySqlPluginEnabled() {
        return isOnClasspath("com.azure.spring.cloud.autoconfigure.jdbc.extension.mysql.AzureIdentityMysqlAuthenticationPlugin")
            && isOnClasspath("com.mysql.cj.jdbc.Driver");
    }

    private boolean isOnClasspath(String className) {
        return ClassUtils.isPresent(className, null);
    }
}
