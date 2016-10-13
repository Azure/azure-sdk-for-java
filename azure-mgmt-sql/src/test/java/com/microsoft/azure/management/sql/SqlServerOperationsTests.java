/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

public class SqlServerOperationsTests extends SqlServerTestBase {
    private static final String RG_NAME = "javasqlserver123";
    private static final String SQL_SERVER_NAME = "javasqlserver123";

    @BeforeClass
    public static void setup() throws Exception {
        createClients();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        resourceManager.resourceGroups().delete(RG_NAME);
    }

    @Test
    public void canCRUDSqlServer() throws Exception {
        // Create
        SqlServer sqlServer = sqlServerManager.sqlServers()
                .define(SQL_SERVER_NAME)
                .withRegion(Region.US_CENTRAL)
                .withNewResourceGroup(RG_NAME)
                .withAdminUserName("userName")
                .withPassword("P@ssword~1")
                .withVersion("12.0")
                .createAsync()
                .toBlocking().last();
        Assert.assertEquals(RG_NAME, sqlServer.resourceGroupName());
        Assert.assertNotNull(sqlServer.fullyQualifiedDomainName());
        Assert.assertEquals("12.0", sqlServer.version());
        Assert.assertEquals("userName", sqlServer.adminLogin());

        sqlServer.update().withPassword("P@ssword~2").apply();

        // List
        List<SqlServer> accounts = sqlServerManager.sqlServers().listByGroup(RG_NAME);
        boolean found = false;
        for (SqlServer account : accounts) {
            if (account.name().equals(SQL_SERVER_NAME)) {
                found = true;
            }
        }
        Assert.assertTrue(found);
        // Get
        sqlServer = sqlServerManager.sqlServers().getByGroup(RG_NAME, SQL_SERVER_NAME);
        Assert.assertNotNull(sqlServer);

        sqlServerManager.sqlServers().delete(sqlServer.resourceGroupName(), sqlServer.name());
        try {
            sqlServerManager.sqlServers().getById(sqlServer.id());
            Assert.assertTrue(false);
        }
        catch (CloudException exception) {
            Assert.assertEquals(exception.getResponse().code(), 404);
        }
    }


}
