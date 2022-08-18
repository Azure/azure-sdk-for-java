// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jdbc;

import com.azure.identity.providers.mysql.AzureIdentityMysqlAuthenticationPlugin;
import com.azure.identity.providers.postgresql.AzureIdentityPostgresqlAuthenticationPlugin;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class JdbcPropertyConstantsTest {

    private static final String POSTGRES_DRIVER_CLASS_NAME = "org.postgresql.Driver";
    private static final String POSTGRES_AUTH_PLUGIN_INTERFACE_CLASS_NAME = "org.postgresql.plugin.AuthenticationPlugin";

    private static final String MYSQL_DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";
    private static final String MYSQL_AUTH_PLUGIN_INTERFACE_CLASS_NAME = "com.mysql.cj.protocol.AuthenticationPlugin";
    @Test
    void testPropertiesInJdbcPropertyConstants() {
        assertEquals(JdbcPropertyConstants.POSTGRES_AUTH_PLUGIN_CLASS_NAME, AzureIdentityPostgresqlAuthenticationPlugin.class.getName());
        assertEquals(JdbcPropertyConstants.POSTGRES_DRIVER_CLASS_NAME, POSTGRES_DRIVER_CLASS_NAME);
        assertEquals(JdbcPropertyConstants.POSTGRES_AUTH_PLUGIN_INTERFACE_CLASS_NAME, POSTGRES_AUTH_PLUGIN_INTERFACE_CLASS_NAME);

        assertEquals(JdbcPropertyConstants.MYSQL_AUTH_PLUGIN_CLASS_NAME, AzureIdentityMysqlAuthenticationPlugin.class.getName());
        assertEquals(JdbcPropertyConstants.MYSQL_DRIVER_CLASS_NAME, MYSQL_DRIVER_CLASS_NAME);
        assertEquals(JdbcPropertyConstants.MYSQL_AUTH_PLUGIN_INTERFACE_CLASS_NAME, MYSQL_AUTH_PLUGIN_INTERFACE_CLASS_NAME);
    }

}
