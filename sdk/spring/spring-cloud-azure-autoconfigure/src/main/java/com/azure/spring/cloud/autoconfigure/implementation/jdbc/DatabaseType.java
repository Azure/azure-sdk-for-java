// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jdbc;

import org.springframework.util.ClassUtils;

/**
 * The type of database URL.
 *
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

    public boolean isDatabasePluginEnabled() {
        if (DatabaseType.POSTGRESQL == this) {
            return isPostgresqlPluginEnabled();
        } else if (DatabaseType.MYSQL == this) {
            return isMySqlPluginEnabled();
        }
        return false;
    }

    private static boolean isPostgresqlPluginEnabled() {
        return isOnClasspath(JdbcPropertyConstants.POSTGRES_AUTH_PLUGIN_CLASS_NAME)
            && isOnClasspath(JdbcPropertyConstants.POSTGRES_AUTH_PLUGIN_INTERFACE_CLASS_NAME)
            && isOnClasspath(JdbcPropertyConstants.POSTGRES_DRIVER_CLASS_NAME);
    }

    private static boolean isMySqlPluginEnabled() {
        return isOnClasspath(JdbcPropertyConstants.MYSQL_AUTH_PLUGIN_CLASS_NAME)
            && isOnClasspath(JdbcPropertyConstants.MYSQL_AUTH_PLUGIN_INTERFACE_CLASS_NAME)
            && isOnClasspath(JdbcPropertyConstants.MYSQL_DRIVER_CLASS_NAME);
    }

    private static boolean isOnClasspath(String className) {
        return ClassUtils.isPresent(className, null);
    }
}
