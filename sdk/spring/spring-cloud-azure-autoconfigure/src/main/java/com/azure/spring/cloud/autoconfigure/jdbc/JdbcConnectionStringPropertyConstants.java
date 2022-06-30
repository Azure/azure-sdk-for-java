// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.jdbc;

/**
 *
 */
final class JdbcConnectionStringPropertyConstants {

    private JdbcConnectionStringPropertyConstants() {
    }

    static final String POSTGRES_PLUGIN_CLASS_NAME = "com.azure.spring.cloud.autoconfigure.jdbc.extension.postgresql.AzurePostgresqlMSIAuthenticationPlugin";
    static final String MYSQL_PLUGIN_CLASS_NAME = "com.azure.spring.cloud.autoconfigure.jdbc.extension.mysql.AzureMySqlMSIAuthenticationPlugin";

    static final String PROPERTY_POSTGRESQL_AUTHENTICATION_PLUGIN_CLASSNAME = "authenticationPluginClassName";
    static final String PROPERTY_POSTGRESQL_SSL_MODE = "sslmode";
    static final String VALUE_POSTGRESQL_SSL_MODE = "require";

    static final String PROPERTY_MYSQL_SSL_MODE="sslMode";
    static final String VALUE_MYSQL_SSL_MODE = "REQUIRED";

    static final String PROPERTY_MYSQL_USE_SSL="useSSL";
    static final String VALUE_MYSQL_USE_SSL="true";

    static final String PROPERTY_MYSQL_DEFAULT_AUTHENTICATION_PLUGIN="defaultAuthenticationPlugin";
    static final String PROPERTY_MYSQL_AUTHENTICATION_PLUGINS="authenticationPlugins";

    static final String NONE_VALUE = "NONE_VALUE";
}
