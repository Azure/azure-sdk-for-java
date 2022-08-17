package com.azure.spring.cloud.autoconfigure.implementation.jdbc;


import com.azure.identity.providers.mysql.AzureIdentityMysqlAuthenticationPlugin;
import com.azure.identity.providers.postgresql.AzureIdentityPostgresqlAuthenticationPlugin;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class JdbcPropertyConstantsTest {

    // TODO add more test codes
    @Test
    void testPropertiesInJdbcPropertyConstants() {
        assertEquals(JdbcPropertyConstants.POSTGRES_AUTH_PLUGIN_CLASS_NAME, AzureIdentityPostgresqlAuthenticationPlugin.class.getName());
        assertEquals(JdbcPropertyConstants.MYSQL_AUTH_PLUGIN_CLASS_NAME, AzureIdentityMysqlAuthenticationPlugin.class.getName());

    }

}
