/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

public class SqlServerOperationsTests extends SqlServerTestBase {
    private static final String RG_NAME = "javasqlserver1234";
    private static final String SQL_SERVER_NAME = "javasqlserver1234";
    private static final String SQL_DATABASE_NAME = "myTestDatabase";
    private static final String COLLATION = "SQL_Latin1_General_CP1_CI_AS";

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
                .withVersion(ServerVersion.ONE_TWO_FULL_STOP_ZERO)
                .createAsync()
                .toBlocking().last();
        validateSqlServer(sqlServer);
        sqlServer.update().withPassword("P@ssword~2").apply();

        // List
        List<SqlServer> sqlServers = sqlServerManager.sqlServers().listByGroup(RG_NAME);
        boolean found = false;
        for (SqlServer server : sqlServers) {
            if (server.name().equals(SQL_SERVER_NAME)) {
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

    @Test
    public void canCRUDSqlDatabase() throws Exception {
        // Create
        Creatable<SqlServer> sqlServerCreatable = sqlServerManager.sqlServers()
                .define(SQL_SERVER_NAME)
                .withRegion(Region.US_CENTRAL)
                .withNewResourceGroup(RG_NAME)
                .withAdminUserName("userName")
                .withPassword("P@ssword~1")
                .withVersion(ServerVersion.ONE_TWO_FULL_STOP_ZERO);

        SqlDatabase sqlDatabase = sqlServerManager.sqlDatabases()
                .define(SQL_DATABASE_NAME)
                .withCollation(COLLATION)
                .withEdition(DatabaseEditions.FREE)
                .withNewParentResource(sqlServerCreatable)
                .createAsync().toBlocking().first();

        validateSqlDatabase(sqlDatabase);

        SqlServer sqlServer =  sqlServerManager.sqlServers().getByGroup(RG_NAME, SQL_SERVER_NAME);
        validateSqlServer(sqlServer);

        // Get
        validateSqlDatabase(sqlServerManager.sqlDatabases().getById(sqlDatabase.id()));
        validateSqlDatabase(sqlServerManager.sqlDatabases().getBySqlServer(sqlServer, SQL_DATABASE_NAME));
        validateSqlDatabase(sqlServerManager.sqlDatabases().getBySqlServer(sqlServer.resourceGroupName(), sqlServer.name(), SQL_DATABASE_NAME));

        // List
        validateListSqlDatabase(sqlServerManager.sqlDatabases().listBySqlServer(sqlServer.resourceGroupName(), sqlServer.name()));
        validateListSqlDatabase(sqlServerManager.sqlDatabases().listBySqlServer(sqlServer));

        sqlServerManager.sqlDatabases().delete(sqlDatabase.id());

        sqlDatabase = sqlServerManager.sqlDatabases()
                .define("newDatabase")
                .withCollation(COLLATION)
                .withEdition(DatabaseEditions.FREE)
                .withNewParentResource(sqlServerCreatable)
                .createAsync().toBlocking().first();
        sqlServerManager.sqlDatabases().delete(sqlDatabase.resourceGroupName(), sqlDatabase.sqlServerName(), sqlDatabase.name());

        sqlServerManager.sqlServers().delete(sqlServer.resourceGroupName(), sqlServer.name());
        try {
            sqlServerManager.sqlServers().getById(sqlServer.id());
            Assert.assertTrue(false);
        }
        catch (CloudException exception) {
            Assert.assertEquals(exception.getResponse().code(), 404);
        }
    }

    private static void validateListSqlDatabase(List<SqlDatabase> sqlDatabases) {
        boolean found = false;
        for (SqlDatabase database : sqlDatabases) {
            if (database.name().equals(SQL_DATABASE_NAME)) {
                found = true;
            }
        }
        Assert.assertTrue(found);
    }

    static void validateSqlServer(SqlServer sqlServer) {
        Assert.assertNotNull(sqlServer);
        Assert.assertEquals(RG_NAME, sqlServer.resourceGroupName());
        Assert.assertNotNull(sqlServer.fullyQualifiedDomainName());
        Assert.assertEquals(ServerVersion.ONE_TWO_FULL_STOP_ZERO, sqlServer.version());
        Assert.assertEquals("userName", sqlServer.adminLogin());
    }

    static void validateSqlDatabase(SqlDatabase sqlDatabase) {
        Assert.assertNotNull(sqlDatabase);
        Assert.assertEquals(sqlDatabase.name(), SQL_DATABASE_NAME);
        Assert.assertEquals(sqlDatabase.collation(), COLLATION);
        Assert.assertEquals(sqlDatabase.edition(), DatabaseEditions.FREE);
    }


}
