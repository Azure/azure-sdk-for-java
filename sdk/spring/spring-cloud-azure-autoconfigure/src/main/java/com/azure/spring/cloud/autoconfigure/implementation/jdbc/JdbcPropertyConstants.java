// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jdbc;

public final class JdbcPropertyConstants {

    private JdbcPropertyConstants() {
    }

    public static final String POSTGRES_DRIVER_CLASS_NAME = "org.postgresql.Driver";
    public static final String POSTGRES_AUTH_PLUGIN_CLASS_NAME = "com.azure.identity.providers.postgresql.AzureIdentityPostgresqlAuthenticationPlugin";
    public static final String POSTGRES_AUTH_PLUGIN_INTERFACE_CLASS_NAME = "org.postgresql.plugin.AuthenticationPlugin";
    public static final String POSTGRESQL_PROPERTY_NAME_AUTHENTICATION_PLUGIN_CLASSNAME = "authenticationPluginClassName";
    public static final String POSTGRESQL_PROPERTY_NAME_SSL_MODE = "sslmode";
    public static final String POSTGRESQL_PROPERTY_VALUE_SSL_MODE = "require";
    public static final String POSTGRESQL_PROPERTY_NAME_APPLICATION_NAME = "ApplicationName";

    public static final String MYSQL_DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";
    public static final String MYSQL_AUTH_PLUGIN_CLASS_NAME = "com.azure.identity.providers.mysql.AzureIdentityMysqlAuthenticationPlugin";
    public static final String MYSQL_AUTH_PLUGIN_INTERFACE_CLASS_NAME = "com.mysql.cj.protocol.AuthenticationPlugin";

    public static final String MYSQL_PROPERTY_NAME_CONNECTION_ATTRIBUTES = "connectionAttributes";
    public static final String MYSQL_PROPERTY_CONNECTION_ATTRIBUTES_ATTRIBUTE_EXTENSION_VERSION = "_extension_version";
    public static final String MYSQL_PROPERTY_CONNECTION_ATTRIBUTES_DELIMITER = ",";
    public static final String MYSQL_PROPERTY_CONNECTION_ATTRIBUTES_KV_DELIMITER = ":";
    public static final String MYSQL_PROPERTY_NAME_SSL_MODE = "sslMode";
    public static final String MYSQL_PROPERTY_VALUE_SSL_MODE = "REQUIRED";
    public static final String MYSQL_PROPERTY_NAME_USE_SSL = "useSSL";
    public static final String MYSQL_PROPERTY_VALUE_USE_SSL = "true";
    public static final String MYSQL_PROPERTY_NAME_DEFAULT_AUTHENTICATION_PLUGIN = "defaultAuthenticationPlugin";
    public static final String MYSQL_PROPERTY_NAME_AUTHENTICATION_PLUGINS = "authenticationPlugins";

}
