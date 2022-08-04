// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jdbc;

public final class JdbcPropertyConstants {

    private JdbcPropertyConstants() {
    }

    public static final String POSTGRES_DRIVER_CLASS_NAME = "org.postgresql.Driver";
    public static final String POSTGRES_AUTH_PLUGIN_CLASS_NAME = "com.azure.spring.cloud.service.implementation.identity.providers.postgresql.AzureIdentityPostgresqlAuthenticationPlugin";
    public static final String POSTGRES_AUTH_PLUGIN_INTERFACE_CLASS_NAME = "org.postgresql.plugin.AuthenticationPlugin";
    public static final String PROPERTY_NAME_POSTGRESQL_AUTHENTICATION_PLUGIN_CLASSNAME = "authenticationPluginClassName";
    public static final String PROPERTY_NAME_POSTGRESQL_SSL_MODE = "sslmode";
    public static final String PROPERTY_VALUE_POSTGRESQL_SSL_MODE = "require";

    public static final String MYSQL_DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";
    public static final String MYSQL_AUTH_PLUGIN_CLASS_NAME = "com.azure.spring.cloud.service.implementation.identity.providers.mysql.AzureIdentityMysqlAuthenticationPlugin";
    public static final String MYSQL_AUTH_PLUGIN_INTERFACE_CLASS_NAME = "com.mysql.cj.protocol.AuthenticationPlugin";
    public static final String PROPERTY_NAME_MYSQL_SSL_MODE = "sslMode";
    public static final String PROPERTY_VALUE_MYSQL_SSL_MODE = "REQUIRED";
    public static final String PROPERTY_NAME_MYSQL_USE_SSL = "useSSL";
    public static final String PROPERTY_VALUE_MYSQL_USE_SSL = "true";
    public static final String PROPERTY_NAME_MYSQL_DEFAULT_AUTHENTICATION_PLUGIN = "defaultAuthenticationPlugin";
    public static final String PROPERTY_NAME_MYSQL_AUTHENTICATION_PLUGINS = "authenticationPlugins";

    public static final String NONE_VALUE = "NONE_VALUE";
}
