/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import org.joda.time.DateTime;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

public class SqlServerOperationsTests extends SqlServerTestBase {
    private static final String RG_NAME = "javasqlserver1237";
    private static final String SQL_SERVER_NAME = "javasqlserver1237";
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
    public void canListRecommendedElasticPools() throws Exception {
        SqlServer sqlServer = sqlServerManager.sqlServers().getByGroup("ans", "ans-secondary");
        sqlServer.databases().list().get(0).listServiceTierAdvisors().get(0).serviceLevelObjectiveUsageMetrics();
        List<RecommendedElasticPool> recommendedElasticPools = sqlServer.recommendedElasticPools().list();
        Assert.assertNotNull(recommendedElasticPools);
        Assert.assertNotNull(sqlServer.databases().list().get(0).getUpgradeHint());
    }


    @Test
    public void canCRUDSqlServer() throws Exception {
        // Create
        SqlServer sqlServer = createSqlServer();

        validateSqlServer(sqlServer);

        List<ServiceObjective> serviceObjectives = sqlServer.listServiceObjectives();

        Assert.assertNotEquals(serviceObjectives.size(), 0);
        Assert.assertNotNull(serviceObjectives.get(0).refresh());
        Assert.assertNotNull(sqlServer.getServiceObjective("d1737d22-a8ea-4de7-9bd0-33395d2a7419"));

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
                .withoutElasticPool()
                .withoutSourceDatabaseId()
                .withCollation(COLLATION)
                .withEdition(DatabaseEditions.STANDARD)
                .createAsync().toBlocking().first();

        validateSqlDatabase(sqlDatabase, SQL_DATABASE_NAME);

        // Test transparent data encryption settings.
        TransparentDataEncryption transparentDataEncryption = sqlDatabase.getTransparentDataEncryption();
        Assert.assertNotNull(transparentDataEncryption.status());

        List<TransparentDataEncryptionActivity> transparentDataEncryptionActivities = transparentDataEncryption.listActivity();
        Assert.assertNotNull(transparentDataEncryptionActivities);

        transparentDataEncryption = transparentDataEncryption.updateState(TransparentDataEncryptionStates.ENABLED);
        Assert.assertNotNull(transparentDataEncryption);
        Assert.assertEquals(transparentDataEncryption.status(), TransparentDataEncryptionStates.ENABLED);

        transparentDataEncryptionActivities = transparentDataEncryption.listActivity();
        Assert.assertNotNull(transparentDataEncryptionActivities);

        Thread.sleep(10000);
        transparentDataEncryption = sqlDatabase.getTransparentDataEncryption().updateState(TransparentDataEncryptionStates.DISABLED);
        Assert.assertNotNull(transparentDataEncryption);
        Assert.assertEquals(transparentDataEncryption.status(), TransparentDataEncryptionStates.DISABLED);
        Assert.assertEquals(transparentDataEncryption.sqlServerName(), SQL_SERVER_NAME);
        Assert.assertEquals(transparentDataEncryption.databaseName(), SQL_DATABASE_NAME);
        Assert.assertNotNull(transparentDataEncryption.name());
        Assert.assertNotNull(transparentDataEncryption.id());
        // Done testing with encryption settings.

        Assert.assertNotNull(sqlDatabase.getUpgradeHint());

        // Test Service tier advisors.
        List<ServiceTierAdvisor> serviceTierAdvisors = sqlDatabase.listServiceTierAdvisors();
        Assert.assertNotNull(serviceTierAdvisors);
        Assert.assertNotNull(serviceTierAdvisors.get(0).serviceLevelObjectiveUsageMetrics());
        Assert.assertNotEquals(serviceTierAdvisors.size(), 0);

        Assert.assertNotNull(serviceTierAdvisors.get(0).refresh());
        Assert.assertNotNull(serviceTierAdvisors.get(0).serviceLevelObjectiveUsageMetrics());
        // End of testing service tier advisors.

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
                .withoutElasticPool()
                .withoutSourceDatabaseId()
                .withCollation(COLLATION)
                .withEdition(DatabaseEditions.STANDARD)
                .createAsync().toBlocking().first();
        sqlServer.databases().delete(sqlDatabase.name());

        sqlServerManager.sqlServers().deleteByGroup(sqlServer.resourceGroupName(), sqlServer.name());
        validateSqlServerNotFound(sqlServer);
    }

    @Test
    public void canManageReplicationLinks() throws Exception {
        // Create
        String anotherSqlServerName = SQL_SERVER_NAME + "another";
        SqlServer sqlServer1 = createSqlServer();
        SqlServer sqlServer2 = createSqlServer(anotherSqlServerName);

        SqlDatabase databaseInServer1 = sqlServer1.databases()
                .define(SQL_DATABASE_NAME)
                .withoutElasticPool()
                .withoutSourceDatabaseId()
                .withCollation(COLLATION)
                .withEdition(DatabaseEditions.STANDARD)
                .createAsync().toBlocking().first();

        validateSqlDatabase(databaseInServer1, SQL_DATABASE_NAME);
        SqlDatabase databaseInServer2 = sqlServer2.databases()
                .define(SQL_DATABASE_NAME)
                .withoutElasticPool()
                .withSourceDatabaseId(databaseInServer1.id())
                .withCreateMode(CreateMode.ONLINE_SECONDARY)
                .create();
        Thread.sleep(2000);
        List<ReplicationLink> replicationLinksInDb1 = databaseInServer1.replicationLinks().list();

        Assert.assertEquals(replicationLinksInDb1.size() , 1);
        Assert.assertEquals(replicationLinksInDb1.get(0).partnerDatabase(), databaseInServer2.name());
        Assert.assertEquals(replicationLinksInDb1.get(0).partnerServer(), databaseInServer2.sqlServerName());

        List<ReplicationLink> replicationLinksInDb2 = databaseInServer2.replicationLinks().list();

        Assert.assertEquals(replicationLinksInDb2.size() , 1);
        Assert.assertEquals(replicationLinksInDb2.get(0).partnerDatabase(), databaseInServer1.name());
        Assert.assertEquals(replicationLinksInDb2.get(0).partnerServer(), databaseInServer1.sqlServerName());

        Assert.assertNotNull(replicationLinksInDb1.get(0).refresh());
        Assert.assertNotNull(databaseInServer1.replicationLinks().get(replicationLinksInDb1.get(0).name()));

        databaseInServer2.replicationLinks().delete(replicationLinksInDb2.get(0).name());

        Assert.assertEquals(databaseInServer2.replicationLinks().list().size(), 0);

        Thread.sleep(5000);
        Assert.assertEquals(databaseInServer1.replicationLinks().list().size(), 0);

        sqlServer1.databases().delete(databaseInServer1.name());
        sqlServer2.databases().delete(databaseInServer2.name());

        sqlServerManager.sqlServers().deleteByGroup(sqlServer2.resourceGroupName(), sqlServer2.name());
        validateSqlServerNotFound(sqlServer2);
        sqlServerManager.sqlServers().deleteByGroup(sqlServer1.resourceGroupName(), sqlServer1.name());
        validateSqlServerNotFound(sqlServer1);
    }

    @Test
    public void canDoOperationsOnDataWarehouse() throws Exception {
        // Create
        SqlServer sqlServer = createSqlServer();

        validateSqlServer(sqlServer);

        // List usages for the server.
        Assert.assertNotNull(sqlServer.listUsages());

        SqlDatabase dataWarehouse = sqlServer.databases()
                .define(SQL_DATABASE_NAME)
                .withoutElasticPool()
                .withoutSourceDatabaseId()
                .withCollation(COLLATION)
                .withEdition(DatabaseEditions.DATA_WAREHOUSE)
                .createAsync().toBlocking().first();

        // Get
        dataWarehouse = sqlServer.databases().get(SQL_DATABASE_NAME);

        Assert.assertNotNull(dataWarehouse);
        Assert.assertEquals(dataWarehouse.name(), SQL_DATABASE_NAME);
        Assert.assertEquals(dataWarehouse.edition(), DatabaseEditions.DATA_WAREHOUSE);

        // TODO - ans - Get Restore points.
        Assert.assertNotNull(dataWarehouse.listRestorePoints());
        // Get usages.
        Assert.assertNotNull(dataWarehouse.listUsages());

        // Pause warehouse
        dataWarehouse.pauseDataWarehouse();

        // Resume warehouse
        dataWarehouse.resumeDataWarehouse();

        sqlServer.databases().delete(SQL_DATABASE_NAME);

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
        SqlElasticPool elasticPool = sqlServer.elasticPools().get(SQL_ELASTIC_POOL_NAME);
        validateSqlElasticPool(elasticPool);

        // Get
        validateSqlDatabaseWithElasticPool(sqlServer.databases().get(SQL_DATABASE_NAME), SQL_DATABASE_NAME);

        // List
        validateListSqlDatabase(sqlServer.databases().list());

        // Remove database from elastic pools.
        sqlDatabase.update()
                .withoutElasticPool()
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

        // List Activity in elastic pool
        Assert.assertNotNull(elasticPool.listActivity());

        // List Database activity in elastic pool.
        Assert.assertNotNull(elasticPool.listDatabaseActivity());

        // List databases in elastic pool.
        List<SqlDatabase> databasesInElasticPool = elasticPool.listDatabases();
        Assert.assertNotNull(databasesInElasticPool);
        Assert.assertEquals(databasesInElasticPool.size(), 1);

        // Get a particular database in elastic pool.
        SqlDatabase databaseInElasticPool = elasticPool.getDatabase(SQL_DATABASE_NAME);
        validateSqlDatabase(databaseInElasticPool, SQL_DATABASE_NAME);

        // Refresh works on the database got from elastic pool.
        databaseInElasticPool.refresh();

        // Validate that trying to get an invalid database from elastic pool returns null.
        try {
            elasticPool.getDatabase("does_not_exist");
            Assert.assertNotNull(null);
        }
        catch(Exception ex) {
        }

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

        sqlServer.elasticPools().delete(SQL_ELASTIC_POOL_NAME);
        sqlServerManager.sqlServers().deleteByGroup(sqlServer.resourceGroupName(), sqlServer.name());
        validateSqlServerNotFound(sqlServer);
    }

    @Test
    public void canUpgradeSqlServer() throws Exception {
        // Create
        SqlServer sqlServer = sqlServerManager.sqlServers()
                .define(SQL_SERVER_NAME)
                .withRegion(Region.US_CENTRAL)
                .withNewResourceGroup(RG_NAME)
                .withAdministratorLogin("userName")
                .withAdministratorPassword("P@ssword~1")
                .withVersion(ServerVersion.TWO_FULL_STOP_ZERO)
                .create();
        sqlServer.refresh();

        SqlElasticPool elasticPool = sqlServer.elasticPools()
                .define(SQL_ELASTIC_POOL_NAME)
                .withEdition(ElasticPoolEditions.STANDARD).create();
        sqlServer.refresh();
        SqlDatabase sqlDatabase = sqlServer.databases()
                .define(SQL_DATABASE_NAME)
                .withExistingElasticPool(elasticPool)
                .withoutSourceDatabaseId()
                .withCollation(COLLATION)
                .withEdition(DatabaseEditions.STANDARD)
                .withServiceObjective(ServiceObjectiveName.S1)
                .createAsync().toBlocking().first();
        sqlServer.refresh();

        validateSqlDatabaseWithElasticPool(sqlDatabase, SQL_DATABASE_NAME);
        validateSqlDatabaseWithElasticPool(sqlServer.databases().get(SQL_DATABASE_NAME), SQL_DATABASE_NAME);

        sqlServer =  sqlServerManager.sqlServers().getByGroup(RG_NAME, SQL_SERVER_NAME);
        validateSqlServer(sqlServer);

        // Get Elastic pool
        elasticPool = sqlServer.elasticPools().get(SQL_ELASTIC_POOL_NAME);
        validateSqlElasticPool(elasticPool);

        // Upgrade
        sqlServer.scheduledUpgrade()
                .withScheduleUpgradeAfterUtcDateTime(DateTime.now().minusMinutes(-10))
                .updateDatabase(SQL_DATABASE_NAME)
                    .withTargetEdition(TargetDatabaseEditions.PREMIUM)
                    .withTargetServiceLevelObjective(ServiceObjectiveName.P3.toString())
                    .attach()
                .updateElasticPool(SQL_ELASTIC_POOL_NAME)
                    .withEdition(TargetElasticPoolEditions.PREMIUM)
                    .withDtu(100)
                    .withDatabaseDtuMax(20)
                    .withDatabaseDtuMin(10)
                    .attach()
                .schedule();
        Assert.assertNotNull(sqlServer.getUpgrade());
        sqlServer.cancelUpgrade();

         // Delete
        sqlServer.databases().delete(SQL_DATABASE_NAME);
        validateSqlDatabaseNotFound(SQL_DATABASE_NAME);
        sqlServer.elasticPools().delete(SQL_ELASTIC_POOL_NAME);
        validateSqlElasticPoolNotFound(sqlServer, SQL_ELASTIC_POOL_NAME);

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
        return createSqlServer(SQL_SERVER_NAME);
    }
    private static SqlServer createSqlServer(String SQL_SERVER_NAME) {
        return sqlServerManager.sqlServers()
                .define(SQL_SERVER_NAME)
                .withRegion(Region.US_CENTRAL)
                .withNewResourceGroup(RG_NAME)
                .withAdministratorLogin("userName")
                .withAdministratorPassword("P@ssword~1")
                .withVersion(ServerVersion.ONE_TWO_FULL_STOP_ZERO)
                .create();
    }
    private static void validateListSqlFirewallRule(List<SqlFirewallRule> sqlFirewallRules) {
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

    private static void validateListSqlElasticPool(List<SqlElasticPool> sqlElasticPools) {
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
