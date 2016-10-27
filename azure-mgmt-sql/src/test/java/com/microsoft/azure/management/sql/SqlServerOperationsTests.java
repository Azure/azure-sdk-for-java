/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

public class SqlServerOperationsTests extends SqlServerTestBase {
    private static final String RG_NAME = "javasqlserver1235";
    private static final String SQL_SERVER_NAME = "javasqlserver1235";
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

        sqlServerManager.sqlServers().deleteByGroup(sqlServer.resourceGroupName(), sqlServer.name());
        validateSqlServerNotFound(sqlServer);
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
                .withEdition(DatabaseEditions.STANDARD)
                .withNewSqlServer(sqlServerCreatable)
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

        sqlServerManager.sqlDatabases().deleteById(sqlDatabase.id());

        // Add another database to the server
        sqlDatabase = sqlServerManager.sqlDatabases()
                .define("newDatabase")
                .withCollation(COLLATION)
                .withEdition(DatabaseEditions.STANDARD)
                .withExistingSqlServer(sqlServer)
                .createAsync().toBlocking().first();
        sqlServerManager.sqlDatabases().deleteByParent(sqlDatabase.resourceGroupName(), sqlDatabase.sqlServerName(), sqlDatabase.name());

        sqlServerManager.sqlServers().deleteByGroup(sqlServer.resourceGroupName(), sqlServer.name());
        validateSqlServerNotFound(sqlServer);
    }

    @Test
    public void canCRUDSqlDatabaseWithElasticPool() throws Exception {
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
                .withEdition(DatabaseEditions.STANDARD)
                .withNewElasticPool(
                        sqlServerManager.sqlElasticPools()
                                .define(SQL_ELASTIC_POOL_NAME)
                                .withEdition(ElasticPoolEditions.STANDARD))
                .withNewSqlServer(sqlServerCreatable)
                .createAsync().toBlocking().first();

        validateSqlDatabase(sqlDatabase);

        SqlServer sqlServer =  sqlServerManager.sqlServers().getByGroup(RG_NAME, SQL_SERVER_NAME);
        validateSqlServer(sqlServer);

        // Get Elastic pool
        SqlElasticPool sqlElasticPool = sqlServerManager.sqlElasticPools().getBySqlServer(RG_NAME, SQL_SERVER_NAME, SQL_ELASTIC_POOL_NAME);
        validateSqlElasticPool(sqlElasticPool);

        validateSqlElasticPool(sqlServerManager.sqlElasticPools().getById(sqlElasticPool.id()));
        validateSqlElasticPool(sqlServerManager.sqlElasticPools().getBySqlServer(sqlServer, SQL_ELASTIC_POOL_NAME));
        validateSqlElasticPool(sqlServerManager.sqlElasticPools().getBySqlServer(sqlServer.resourceGroupName(), sqlServer.name(), SQL_ELASTIC_POOL_NAME));

        // Get
        validateSqlDatabaseWithElasticPool(sqlServerManager.sqlDatabases().getById(sqlDatabase.id()));
        validateSqlDatabaseWithElasticPool(sqlServerManager.sqlDatabases().getBySqlServer(sqlServer, SQL_DATABASE_NAME));
        validateSqlDatabaseWithElasticPool(sqlServerManager.sqlDatabases().getBySqlServer(sqlServer.resourceGroupName(), sqlServer.name(), SQL_DATABASE_NAME));

        // List
        validateListSqlDatabase(sqlServerManager.sqlDatabases().listBySqlServer(sqlServer.resourceGroupName(), sqlServer.name()));
        validateListSqlDatabase(sqlServerManager.sqlDatabases().listBySqlServer(sqlServer));

        sqlServerManager.sqlDatabases().deleteById(sqlDatabase.id());
        validateSqlDatabaseNotFound(SQL_DATABASE_NAME);

        // Add another database to the server
        sqlDatabase = sqlServerManager.sqlDatabases()
                .define("newDatabase")
                .withCollation(COLLATION)
                .withEdition(DatabaseEditions.STANDARD)
                .withExistingElasticPoolName(sqlElasticPool)
                .withExistingSqlServer(sqlServer)
                .createAsync().toBlocking().first();
        sqlServerManager.sqlDatabases().deleteByParent(sqlDatabase.resourceGroupName(), sqlDatabase.sqlServerName(), sqlDatabase.name());
        validateSqlDatabaseNotFound("newDatabase");

        sqlServerManager.sqlServers().deleteByGroup(sqlServer.resourceGroupName(), sqlServer.name());
        validateSqlServerNotFound(sqlServer);
    }

    @Test
    public void canCRUDSqlElasticPool() throws Exception {
        // Create
        Creatable<SqlServer> sqlServerCreatable = sqlServerManager.sqlServers()
                .define(SQL_SERVER_NAME)
                .withRegion(Region.US_CENTRAL)
                .withNewResourceGroup(RG_NAME)
                .withAdminUserName("userName")
                .withPassword("P@ssword~1")
                .withVersion(ServerVersion.ONE_TWO_FULL_STOP_ZERO);

        SqlElasticPool sqlElasticPool = sqlServerManager.sqlElasticPools()
                .define(SQL_ELASTIC_POOL_NAME)
                .withEdition(ElasticPoolEditions.STANDARD)
                .withNewSqlServer(sqlServerCreatable)
                .createAsync().toBlocking().first();

        validateSqlElasticPool(sqlElasticPool);

        SqlServer sqlServer =  sqlServerManager.sqlServers().getByGroup(RG_NAME, SQL_SERVER_NAME);
        validateSqlServer(sqlServer);

        // Get
        validateSqlElasticPool(sqlServerManager.sqlElasticPools().getById(sqlElasticPool.id()));
        validateSqlElasticPool(sqlServerManager.sqlElasticPools().getBySqlServer(sqlServer, SQL_ELASTIC_POOL_NAME));
        validateSqlElasticPool(sqlServerManager.sqlElasticPools().getBySqlServer(sqlServer.resourceGroupName(), sqlServer.name(), SQL_ELASTIC_POOL_NAME));

        // List
        validateListSqlElasticPool(sqlServerManager.sqlElasticPools().listBySqlServer(sqlServer.resourceGroupName(), sqlServer.name()));
        validateListSqlElasticPool(sqlServerManager.sqlElasticPools().listBySqlServer(sqlServer));

        sqlServerManager.sqlElasticPools().deleteById(sqlElasticPool.id());
        validateSqlElasticPoolNotFound(SQL_ELASTIC_POOL_NAME);

        // Add another database to the server
        sqlElasticPool = sqlServerManager.sqlElasticPools()
                .define("newElasticPool")
                .withEdition(ElasticPoolEditions.STANDARD)
                .withExistingSqlServer(sqlServer)
                .createAsync().toBlocking().first();
        sqlServerManager.sqlElasticPools().deleteByParent(sqlElasticPool.resourceGroupName(), sqlElasticPool.sqlServerName(), sqlElasticPool.name());
        validateSqlElasticPoolNotFound("newElasticPool");

        sqlServerManager.sqlServers().deleteByGroup(sqlServer.resourceGroupName(), sqlServer.name());
        validateSqlServerNotFound(sqlServer);
    }

    @Test
    public void canCRUDSqlFirewallRule() throws Exception {
        // Create
        Creatable<SqlServer> sqlServerCreatable = sqlServerManager.sqlServers()
                .define(SQL_SERVER_NAME)
                .withRegion(Region.US_CENTRAL)
                .withNewResourceGroup(RG_NAME)
                .withAdminUserName("userName")
                .withPassword("P@ssword~1")
                .withVersion(ServerVersion.ONE_TWO_FULL_STOP_ZERO);

        SqlFirewallRule sqlFirewallRule = sqlServerManager.sqlFirewallRules()
                .define(SQL_FIREWALLRULE_NAME)
                .withStartIpAddress(START_IPADDRESS)
                .withEndIpAddress(END_IPADDRESS)
                .withNewSqlServer(sqlServerCreatable)
                .createAsync().toBlocking().first();

        validateSqlFirewallRule(sqlFirewallRule, SQL_FIREWALLRULE_NAME);
        validateSqlFirewallRule(sqlServerManager.sqlFirewallRules().getBySqlServer(RG_NAME, SQL_SERVER_NAME, SQL_FIREWALLRULE_NAME), SQL_FIREWALLRULE_NAME);

        SqlServer sqlServer =  sqlServerManager.sqlServers().getByGroup(RG_NAME, SQL_SERVER_NAME);
        validateSqlServer(sqlServer);

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
        validateSqlFirewallRule(sqlServerManager.sqlFirewallRules().getBySqlServer(sqlServer, SQL_FIREWALLRULE_NAME), SQL_FIREWALLRULE_NAME);
        validateSqlFirewallRule(sqlServerManager.sqlFirewallRules().getById(sqlFirewallRule.id()), SQL_FIREWALLRULE_NAME);

        // List
        validateListSqlFirewallRule(sqlServerManager.sqlFirewallRules().listBySqlServer(sqlServer.resourceGroupName(), sqlServer.name()));
        validateListSqlFirewallRule(sqlServerManager.sqlFirewallRules().listBySqlServer(sqlServer));

        sqlServerManager.sqlFirewallRules().deleteById(sqlFirewallRule.id());
        validateSqlFirewallRuleNotFound();

        // Add firewall rule again with existing server.
        sqlFirewallRule = sqlServerManager.sqlFirewallRules()
                .define(SQL_FIREWALLRULE_NAME)
                .withStartIpAddress(START_IPADDRESS)
                .withEndIpAddress(END_IPADDRESS)
                .withExistingSqlServer(sqlServer)
                .createAsync().toBlocking().first();
        sqlServerManager.sqlFirewallRules().deleteByParent(sqlFirewallRule.resourceGroupName(), sqlFirewallRule.sqlServerName(), sqlFirewallRule.name());
        validateSqlFirewallRuleNotFound();

        // Add firewall rule again with existing server.
        sqlFirewallRule = sqlServerManager.sqlFirewallRules()
                .define(SQL_FIREWALLRULE_NAME)
                .withStartIpAddress(START_IPADDRESS)
                .withEndIpAddress(END_IPADDRESS)
                .withExistingSqlServer(sqlServer.resourceGroupName(), sqlServer.name())
                .createAsync().toBlocking().first();
        sqlServerManager.sqlFirewallRules().deleteByParent(sqlFirewallRule.resourceGroupName(), sqlFirewallRule.sqlServerName(), sqlFirewallRule.name());
        validateSqlFirewallRuleNotFound();

        sqlServerManager.sqlServers().deleteByGroup(sqlServer.resourceGroupName(), sqlServer.name());
        validateSqlServerNotFound(sqlServer);
    }

    private static void validateSqlFirewallRuleNotFound() {
        Assert.assertNull(sqlServerManager.sqlFirewallRules().getBySqlServer(RG_NAME, SQL_SERVER_NAME, SQL_FIREWALLRULE_NAME));
    }


    private static void validateSqlElasticPoolNotFound(String elasticPoolName) {
        Assert.assertNull(sqlServerManager.sqlElasticPools().getBySqlServer(RG_NAME, SQL_SERVER_NAME, elasticPoolName));
    }

    private static void validateSqlDatabaseNotFound(String newDatabase) {
        Assert.assertNull(sqlServerManager.sqlDatabases().getBySqlServer(RG_NAME, SQL_SERVER_NAME, newDatabase));
    }


    private static void validateSqlServerNotFound(SqlServer sqlServer) {
        Assert.assertNull(sqlServerManager.sqlServers().getById(sqlServer.id()));
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
        Assert.assertEquals("userName", sqlServer.adminLogin());
    }

    private static void validateSqlDatabase(SqlDatabase sqlDatabase) {
        Assert.assertNotNull(sqlDatabase);
        Assert.assertEquals(sqlDatabase.name(), SQL_DATABASE_NAME);
        Assert.assertEquals(SQL_SERVER_NAME, sqlDatabase.sqlServerName());
        Assert.assertEquals(sqlDatabase.collation(), COLLATION);
        Assert.assertEquals(sqlDatabase.edition(), DatabaseEditions.STANDARD);
    }


    private static void validateSqlDatabaseWithElasticPool(SqlDatabase sqlDatabase) {
        validateSqlDatabase(sqlDatabase);
        Assert.assertEquals(SQL_ELASTIC_POOL_NAME, sqlDatabase.elasticPoolName());
    }
}
