// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jdbc;

import org.springframework.util.ClassUtils;
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
 * The type of database URL.
 * <p>
 * A connection string could be follow
 * <p>
 * <i>scheme://authority/path?query#fragment</i>
 * <p>
 * This results in splitting the connection string URL and processing its internal parts:
 * <dl>
 * <dt>scheme</dt>
 * <dd>The protocol and subprotocol identification. Usually "jdbc:mysql:" or "mysqlx:".</dd>
 * <dt>authority</dt>
 * <dd>Contains information about the user credentials and/or the host and port information. </dd>
 * <dt>path</dt>
 * <dd>Corresponds to the database identification.</dd>
 * <dt>query</dt>
 * <dd>The connection properties, written as "propertyName1[=[propertyValue1]][&amp;propertyName2[=[propertyValue2]]]..."</dd>
 * <dt>fragment</dt>
 * <dd>The fragment section is ignored in Connector/J connection strings.</dd>
 * </dl>
 */
public enum DatabaseType {

    MYSQL("jdbc:mysql", "?", "&"),
    POSTGRESQL("jdbc:postgresql", "?", "&"),
    SQLSERVER("jdbc:sqlserver", ";", ";");

    private final String schema;
    private final String pathQueryDelimiter;
    private final String queryDelimiter;

    DatabaseType(String schema, String pathQueryDelimiter, String queryDelimiter) {
        this.schema = schema;
        this.pathQueryDelimiter = pathQueryDelimiter;
        this.queryDelimiter = queryDelimiter;
    }

    public String getSchema() {
        return this.schema;
    }

    public String getPathQueryDelimiter() {
        return this.pathQueryDelimiter;
    }

    public String getQueryDelimiter() {
        return queryDelimiter;
    }

    // TODO (zhihaoguo): refactor with interfaces.
    public boolean isDatabasePluginAvailable() {
        if (DatabaseType.POSTGRESQL == this) {
            return isPostgresqlPluginAvailable();
        } else if (DatabaseType.MYSQL == this) {
            return isMySqlPluginAvailable();
        }
        return false;
    }

    public Map<String, String> getDefaultEnhancedProperties() {
        Map<String, String> result = new TreeMap<>();
        if (DatabaseType.POSTGRESQL == this) {
            result.put(PROPERTY_NAME_POSTGRESQL_AUTHENTICATION_PLUGIN_CLASSNAME, POSTGRES_AUTH_PLUGIN_CLASS_NAME);
            result.put(PROPERTY_NAME_POSTGRESQL_SSL_MODE, PROPERTY_VALUE_POSTGRESQL_SSL_MODE);
        } else if (DatabaseType.MYSQL == this) {
            result.put(PROPERTY_NAME_MYSQL_SSL_MODE, PROPERTY_VALUE_MYSQL_SSL_MODE);
            result.put(PROPERTY_NAME_MYSQL_USE_SSL, PROPERTY_VALUE_MYSQL_USE_SSL);
            result.put(PROPERTY_NAME_MYSQL_DEFAULT_AUTHENTICATION_PLUGIN, MYSQL_AUTH_PLUGIN_CLASS_NAME);
            result.put(PROPERTY_NAME_MYSQL_AUTHENTICATION_PLUGINS, MYSQL_AUTH_PLUGIN_CLASS_NAME);
        }
        return result;
    }

    public void setDefaultEnhancedProperties(Map<String, String> map) {
        map.putAll(getDefaultEnhancedProperties());
    }

    private boolean isPostgresqlPluginAvailable() {
        return isOnClasspath(JdbcPropertyConstants.POSTGRES_AUTH_PLUGIN_CLASS_NAME)
            && isOnClasspath(JdbcPropertyConstants.POSTGRES_AUTH_PLUGIN_INTERFACE_CLASS_NAME)
            && isOnClasspath(JdbcPropertyConstants.POSTGRES_DRIVER_CLASS_NAME);
    }

    private boolean isMySqlPluginAvailable() {
        return isOnClasspath(JdbcPropertyConstants.MYSQL_AUTH_PLUGIN_CLASS_NAME)
            && isOnClasspath(JdbcPropertyConstants.MYSQL_AUTH_PLUGIN_INTERFACE_CLASS_NAME)
            && isOnClasspath(JdbcPropertyConstants.MYSQL_DRIVER_CLASS_NAME);
    }

    private boolean isOnClasspath(String className) {
        return ClassUtils.isPresent(className, null);
    }
}
