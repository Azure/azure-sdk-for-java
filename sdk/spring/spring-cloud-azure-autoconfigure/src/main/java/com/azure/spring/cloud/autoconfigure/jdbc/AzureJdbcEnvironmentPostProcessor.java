package com.azure.spring.cloud.autoconfigure.jdbc;

import com.azure.spring.cloud.autoconfigure.implementation.jdbc.AzureJDBCProperties;
import com.azure.spring.cloud.autoconfigure.implementation.jdbc.AzureJDBCPropertiesUtils;
import org.apache.commons.logging.Log;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
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

public class AzureJdbcEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

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

    private final Log logger;

    public AzureJdbcEnvironmentPostProcessor(Log logger) {
        this.logger = logger;
    }

    public AzureJdbcEnvironmentPostProcessor() {
        this.logger = new DeferredLog();
    }

    @Override
    public int getOrder() {
        return ConfigDataEnvironmentPostProcessor.ORDER + 100;
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application){

        DataSourceProperties dataSourceProperties = Binder.get(environment).bindOrCreate("spring.datasource", DataSourceProperties.class);
        boolean isPasswordProvided = StringUtils.hasText(dataSourceProperties.getPassword());

        if (isPasswordProvided) {
            logger.debug("'spring.datasource.password' provided, skip AzureJdbcEnvironmentPostProcessor.");
            return;
        }

        String url = dataSourceProperties.getUrl();
        if (!StringUtils.hasText(url)) {
            logger.debug("No 'spring.datasource.url' provided, skip AzureJdbcEnvironmentPostProcessor.");
            return;
        }

        JdbcConnectionString connectionString = new JdbcConnectionString(url);
        DatabaseType databaseType = connectionString.getDatabaseType();
        if (!isDatabasePluginEnabled(databaseType)) {
            logger.info("The jdbc plugin with provided jdbc schema is not on the classpath , skip AzureJdbcEnvironmentPostProcessor");
        }

        try {
            if (ENHANCED_PROPERTIES.containsKey(databaseType)) {
                AzureJDBCProperties azureJDBCProperties = Binder.get(environment).bindOrCreate("spring.datasource.azure", AzureJDBCProperties.class);
                Map<String, String> configMap = new HashMap<>();
                AzureJDBCPropertiesUtils.convertAzurePropertiesToConfigMap(azureJDBCProperties, configMap);
                configMap.putAll(ENHANCED_PROPERTIES.get(databaseType));
                String enhancedUrl = connectionString.enhanceConnectionString(configMap);
                logger.info("Enhanced url is " + enhancedUrl);

                Map<String, Object> propertyMap = new HashMap<>();
                propertyMap.put("spring.datasource.url", enhancedUrl);

                environment.getPropertySources().addFirst(new MapPropertySource("AZURE_DATABASE", propertyMap));
            }
        } catch (IllegalStateException e) {
            logger.debug("Inconsistent properties detected, skip AzureJdbcEnvironmentPostProcessor");
        }
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
