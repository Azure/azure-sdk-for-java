/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

public class SqlServerOperationsTests extends SqlServerTestBase {
    private static final String RG_NAME = "javasqlserver1238";
    private static final String SQL_SERVER_NAME = "javasqlserver1238";
    private static final String SQL_DATABASE_NAME = "myTestDatabase2";
    private static final String COLLATION = "SQL_Latin1_General_CP1_CI_AS";
    private static final String SQL_ELASTIC_POOL_NAME = "testElasticPool";
    private static final String SQL_FIREWALLRULE_NAME = "firewallrule1";
    private static final String START_IPADDRESS = "10.102.1.10";
    private static final String END_IPADDRESS = "10.102.1.12";


    @BeforeClass
    public static void setup() throws Exception {
        createClients();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        resourceManager.resourceGroups().deleteByName(RG_NAME);
    }

    @Test
    public void canCRUDSqlServer() throws Exception {
        // Create
        SqlServer sqlServer = createSqlServer();

        validateSqlServer(sqlServer);
        sqlServer.update().withAdministratorPassword("P@ssword~2").apply();

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

        sqlServerManager.sqlServers().deleteByGroup(sqlServer.resourceGroupName(), sqlServer.name());
        validateSqlServerNotFound(sqlServer);
    }

    @Test
    public void canCRUDSqlDatabase() throws Exception {
        // Create
        SqlServer sqlServer = createSqlServer();

        SqlDatabase sqlDatabase = sqlServer.databases()
                .define(SQL_DATABASE_NAME)
                .withoutExistingElasticPool()
                .withoutSourceDatabaseId()
                .withCollation(COLLATION)
                .withEdition(DatabaseEditions.STANDARD)
                .createAsync().toBlocking().first();

        validateSqlDatabase(sqlDatabase, SQL_DATABASE_NAME);

        sqlServer =  sqlServerManager.sqlServers().getByGroup(RG_NAME, SQL_SERVER_NAME);
        validateSqlServer(sqlServer);

        // Create another database with above created database as source database.
        Creatable<SqlElasticPool> sqlElasticPoolCreatable = sqlServer.elasticPools()
                .define(SQL_ELASTIC_POOL_NAME)
                .withEdition(ElasticPoolEditions.STANDARD);
        String anotherDatabaseName = "anotherDatabase";
        SqlDatabase anotherDatabase = sqlServer.databases()
                .define(anotherDatabaseName)
                .withNewElasticPool(sqlElasticPoolCreatable)
                .withSourceDatabaseId(sqlDatabase.id())
                .withCreateMode(CreateMode.COPY)
                .create();

        validateSqlDatabaseWithElasticPool(anotherDatabase, anotherDatabaseName);
        sqlServer.databases().delete(anotherDatabase.name());

        // Get
        validateSqlDatabase(sqlServer.databases().get(SQL_DATABASE_NAME), SQL_DATABASE_NAME);

        // List
        validateListSqlDatabase(sqlServer.databases().list());

        // Delete
        sqlServer.databases().delete(SQL_DATABASE_NAME);
        validateSqlDatabaseNotFound(SQL_DATABASE_NAME);

        // Add another database to the server
        sqlDatabase = sqlServer.databases()
                .define("newDatabase")
                .withoutExistingElasticPool()
                .withoutSourceDatabaseId()
                .withCollation(COLLATION)
                .withEdition(DatabaseEditions.STANDARD)
                .createAsync().toBlocking().first();
        sqlServer.databases().delete(sqlDatabase.name());

        sqlServerManager.sqlServers().deleteByGroup(sqlServer.resourceGroupName(), sqlServer.name());
        validateSqlServerNotFound(sqlServer);
    }

    @Test
    public void canCRUDSqlDatabaseWithElasticPool() throws Exception {
        // Create
        SqlServer sqlServer = createSqlServer();

        Creatable<SqlElasticPool> sqlElasticPoolCreatable = sqlServer.elasticPools()
                .define(SQL_ELASTIC_POOL_NAME)
                .withEdition(ElasticPoolEditions.STANDARD);

        SqlDatabase sqlDatabase = sqlServer.databases()
                .define(SQL_DATABASE_NAME)
                .withNewElasticPool(sqlElasticPoolCreatable)
                .withoutSourceDatabaseId()
                .withCollation(COLLATION)
                .withEdition(DatabaseEditions.STANDARD)
                .withServiceObjective(ServiceObjectiveName.S1)
                .createAsync().toBlocking().first();

        validateSqlDatabase(sqlDatabase, SQL_DATABASE_NAME);

        sqlServer =  sqlServerManager.sqlServers().getByGroup(RG_NAME, SQL_SERVER_NAME);
        validateSqlServer(sqlServer);

        // Get Elastic pool
        validateSqlElasticPool(sqlServer.elasticPools().get(SQL_ELASTIC_POOL_NAME));

        // Get
        validateSqlDatabaseWithElasticPool(sqlServer.databases().get(SQL_DATABASE_NAME), SQL_DATABASE_NAME);

        // List
        validateListSqlDatabase(sqlServer.databases().list());

        // Remove database from elastic pools.
        sqlDatabase.update()
                .withoutExistingElasticPool()
                .withEdition(DatabaseEditions.STANDARD)
                .withServiceObjective(ServiceObjectiveName.S3)
            .apply();
        sqlDatabase = sqlServer.databases().get(SQL_DATABASE_NAME);
        Assert.assertNull(sqlDatabase.elasticPoolName());

        // Update edition of the SQL database
        sqlDatabase.update()
                .withEdition(DatabaseEditions.PREMIUM)
                .withServiceObjective(ServiceObjectiveName.P1)
                .apply();
        sqlDatabase = sqlServer.databases().get(SQL_DATABASE_NAME);
        Assert.assertEquals(sqlDatabase.edition(), DatabaseEditions.PREMIUM);
        Assert.assertEquals(sqlDatabase.serviceLevelObjective(), ServiceObjectiveName.P1);

        // Update just the service level objective for database.
        sqlDatabase.update().withServiceObjective(ServiceObjectiveName.P2).apply();
        sqlDatabase = sqlServer.databases().get(SQL_DATABASE_NAME);
        Assert.assertEquals(sqlDatabase.serviceLevelObjective(), ServiceObjectiveName.P2);
        Assert.assertEquals(sqlDatabase.requestedServiceObjectiveName(), ServiceObjectiveName.P2);

        // Update max size bytes of the database.
        sqlDatabase.update()
                .withMaxSizeBytes(268435456000L)
                .apply();

        sqlDatabase = sqlServer.databases().get(SQL_DATABASE_NAME);
        Assert.assertEquals(sqlDatabase.maxSizeBytes(), 268435456000L);

        // Put the database back in elastic pool.
        sqlDatabase.update()
                .withExistingElasticPool(SQL_ELASTIC_POOL_NAME)
                .apply();

        sqlDatabase = sqlServer.databases().get(SQL_DATABASE_NAME);
        Assert.assertEquals(sqlDatabase.elasticPoolName(), SQL_ELASTIC_POOL_NAME);

        // Delete
        sqlServer.databases().delete(SQL_DATABASE_NAME);
        validateSqlDatabaseNotFound(SQL_DATABASE_NAME);

        SqlElasticPool sqlElasticPool = sqlServer.elasticPools().get(SQL_ELASTIC_POOL_NAME);

        // Add another database to the server and pool.
        sqlDatabase = sqlServer.databases()
                .define("newDatabase")
                .withExistingElasticPool(sqlElasticPool)
                .withoutSourceDatabaseId()
                .withCollation(COLLATION)
                .withEdition(DatabaseEditions.STANDARD)
                .createAsync().toBlocking().first();
        sqlServer.databases().delete(sqlDatabase.name());
        validateSqlDatabaseNotFound("newDatabase");

        sqlServerManager.sqlServers().deleteByGroup(sqlServer.resourceGroupName(), sqlServer.name());
        validateSqlServerNotFound(sqlServer);
    }

    @Test
    public void canCRUDSqlElasticPool() throws Exception {
        // Create
        SqlServer sqlServer = createSqlServer();

        sqlServer =  sqlServerManager.sqlServers().getByGroup(RG_NAME, SQL_SERVER_NAME);
        validateSqlServer(sqlServer);

        SqlElasticPool sqlElasticPool = sqlServer.elasticPools()
                .define(SQL_ELASTIC_POOL_NAME)
                .withEdition(ElasticPoolEditions.STANDARD)
                .createAsync().toBlocking().first();

        sqlElasticPool = sqlElasticPool.update()
                .withDtu(100)
                .withDatabaseDtuMax(20)
                .withDatabaseDtuMin(10)
                .withStorageCapacity(102400).apply();

        validateSqlElasticPool(sqlElasticPool);

        // Get
        validateSqlElasticPool(sqlServer.elasticPools().get(SQL_ELASTIC_POOL_NAME));

        // List
        validateListSqlElasticPool(sqlServer.elasticPools().list());

        // Delete
        sqlServer.elasticPools().delete(SQL_ELASTIC_POOL_NAME);
        validateSqlElasticPoolNotFound(sqlServer, SQL_ELASTIC_POOL_NAME);

        // Add another database to the server
        sqlElasticPool = sqlServer.elasticPools()
                .define("newElasticPool")
                .withEdition(ElasticPoolEditions.STANDARD)
                .createAsync().toBlocking().first();

        sqlServer.elasticPools().delete(sqlElasticPool.name());
        validateSqlElasticPoolNotFound(sqlServer, "newElasticPool");

        sqlServerManager.sqlServers().deleteByGroup(sqlServer.resourceGroupName(), sqlServer.name());
        validateSqlServerNotFound(sqlServer);
    }

    @Test
    public void canCRUDSqlFirewallRule() throws Exception {
        // Create
        SqlServer sqlServer = createSqlServer();

        sqlServer =  sqlServerManager.sqlServers().getByGroup(RG_NAME, SQL_SERVER_NAME);
        validateSqlServer(sqlServer);

        SqlFirewallRule sqlFirewallRule = sqlServer.firewallRules()
                .define(SQL_FIREWALLRULE_NAME)
                .withStartIpAddress(START_IPADDRESS)
                .withEndIpAddress(END_IPADDRESS)
                .createAsync().toBlocking().first();

        validateSqlFirewallRule(sqlFirewallRule, SQL_FIREWALLRULE_NAME);
        validateSqlFirewallRule(sqlServer.firewallRules().get(SQL_FIREWALLRULE_NAME), SQL_FIREWALLRULE_NAME);


        String secondFirewallRuleName = "secondFireWallRule";
        SqlFirewallRule secondFirewallRule = sqlServer.firewallRules()
                .define(secondFirewallRuleName)
                .withStartIpAddress(START_IPADDRESS)
                .withEndIpAddress(END_IPADDRESS)
                .create();

        Assert.assertNotNull(sqlServer.firewallRules().get(secondFirewallRuleName));
        validateSqlFirewallRule(secondFirewallRule, secondFirewallRuleName);
        sqlServer.firewallRules().delete(secondFirewallRuleName);
        Assert.assertNull(sqlServer.firewallRules().get(secondFirewallRuleName));

        // Get
        sqlFirewallRule = sqlServer.firewallRules().get(SQL_FIREWALLRULE_NAME);
        validateSqlFirewallRule(sqlFirewallRule, SQL_FIREWALLRULE_NAME);

        // Update
        // Making start and end IP address same.
        sqlFirewallRule.update().withEndIpAddress(START_IPADDRESS).apply();
        sqlFirewallRule = sqlServer.firewallRules().get(SQL_FIREWALLRULE_NAME);
        Assert.assertEquals(sqlFirewallRule.endIpAddress(), START_IPADDRESS);

        // List
        validateListSqlFirewallRule(sqlServer.firewallRules().list());

        // Delete
        sqlServer.firewallRules().delete(sqlFirewallRule.name());
        validateSqlFirewallRuleNotFound();

        // Delete server
        sqlServerManager.sqlServers().deleteByGroup(sqlServer.resourceGroupName(), sqlServer.name());
        validateSqlServerNotFound(sqlServer);
    }

    private static void validateSqlFirewallRuleNotFound() {
        Assert.assertNull(sqlServerManager.sqlServers().getByGroup(RG_NAME, SQL_SERVER_NAME).firewallRules().get(SQL_FIREWALLRULE_NAME));
    }


    private static void validateSqlElasticPoolNotFound(SqlServer sqlServer, String elasticPoolName) {
        Assert.assertNull(sqlServer.elasticPools().get(elasticPoolName));
    }

    private static void validateSqlDatabaseNotFound(String newDatabase) {
        Assert.assertNull(sqlServerManager.sqlServers().getByGroup(RG_NAME, SQL_SERVER_NAME).databases().get(newDatabase));
    }


    private static void validateSqlServerNotFound(SqlServer sqlServer) {
        Assert.assertNull(sqlServerManager.sqlServers().getById(sqlServer.id()));
    }

    private static SqlServer createSqlServer() {
        return sqlServerManager.sqlServers()
                .define(SQL_SERVER_NAME)
                .withRegion(Region.US_CENTRAL)
                .withNewResourceGroup(RG_NAME)
                .withAdministratorLogin("userName")
                .withAdministratorPassword("P@ssword~1")
                .withVersion(ServerVersion.ONE_TWO_FULL_STOP_ZERO)
                .create();
    }
    private static void validateListSqlFirewallRule(PagedList<SqlFirewallRule> sqlFirewallRules) {
        boolean found = false;
        for (SqlFirewallRule firewallRule: sqlFirewallRules) {
            if (firewallRule.name().equals(SQL_FIREWALLRULE_NAME)) {
                found = true;
            }
        }
        Assert.assertTrue(found);
    }

    private static void validateSqlFirewallRule(SqlFirewallRule sqlFirewallRule, String firewallName) {
        Assert.assertNotNull(sqlFirewallRule);
        Assert.assertEquals(firewallName, sqlFirewallRule.name());
        Assert.assertEquals(SQL_SERVER_NAME, sqlFirewallRule.sqlServerName());
        Assert.assertEquals(START_IPADDRESS, sqlFirewallRule.startIpAddress());
        Assert.assertEquals(END_IPADDRESS, sqlFirewallRule.endIpAddress());
        Assert.assertEquals(RG_NAME, sqlFirewallRule.resourceGroupName());
        Assert.assertEquals(SQL_SERVER_NAME, sqlFirewallRule.sqlServerName());
        Assert.assertEquals(Region.US_CENTRAL, sqlFirewallRule.region());
    }

    private static void validateListSqlElasticPool(PagedList<SqlElasticPool> sqlElasticPools) {
        boolean found = false;
        for (SqlElasticPool elasticPool : sqlElasticPools) {
            if (elasticPool.name().equals(SQL_ELASTIC_POOL_NAME)) {
                found = true;
            }
        }
        Assert.assertTrue(found);
    }

    private static void validateSqlElasticPool(SqlElasticPool sqlElasticPool) {
        Assert.assertNotNull(sqlElasticPool);
        Assert.assertEquals(RG_NAME, sqlElasticPool.resourceGroupName());
        Assert.assertEquals(SQL_ELASTIC_POOL_NAME, sqlElasticPool.name());
        Assert.assertEquals(SQL_SERVER_NAME, sqlElasticPool.sqlServerName());
        Assert.assertEquals(ElasticPoolEditions.STANDARD, sqlElasticPool.edition());
        Assert.assertNotNull(sqlElasticPool.creationDate());
        Assert.assertNotEquals(0, sqlElasticPool.databaseDtuMax());
        Assert.assertNotEquals(0, sqlElasticPool.dtu());
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

    private static void validateSqlServer(SqlServer sqlServer) {
        Assert.assertNotNull(sqlServer);
        Assert.assertEquals(RG_NAME, sqlServer.resourceGroupName());
        Assert.assertNotNull(sqlServer.fullyQualifiedDomainName());
        Assert.assertEquals(ServerVersion.ONE_TWO_FULL_STOP_ZERO, sqlServer.version());
        Assert.assertEquals("userName", sqlServer.administratorLogin());
    }

    private static void validateSqlDatabase(SqlDatabase sqlDatabase, String databaseName) {
        Assert.assertNotNull(sqlDatabase);
        Assert.assertEquals(sqlDatabase.name(), databaseName);
        Assert.assertEquals(SQL_SERVER_NAME, sqlDatabase.sqlServerName());
        Assert.assertEquals(sqlDatabase.collation(), COLLATION);
        Assert.assertEquals(sqlDatabase.edition(), DatabaseEditions.STANDARD);
    }


    private static void validateSqlDatabaseWithElasticPool(SqlDatabase sqlDatabase, String databaseName) {
        validateSqlDatabase(sqlDatabase, databaseName);
        Assert.assertEquals(SQL_ELASTIC_POOL_NAME, sqlDatabase.elasticPoolName());
    }
}
