// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.resources.core.TestUtilities;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;
import com.azure.resourcemanager.sql.models.AdministratorType;
import com.azure.resourcemanager.sql.models.AutomaticTuningMode;
import com.azure.resourcemanager.sql.models.AutomaticTuningOptionModeActual;
import com.azure.resourcemanager.sql.models.AutomaticTuningOptionModeDesired;
import com.azure.resourcemanager.sql.models.AutomaticTuningServerMode;
import com.azure.resourcemanager.sql.models.CheckNameAvailabilityReason;
import com.azure.resourcemanager.sql.models.CheckNameAvailabilityResult;
import com.azure.resourcemanager.sql.models.CreateMode;
import com.azure.resourcemanager.sql.models.DatabaseEdition;
import com.azure.resourcemanager.sql.models.ElasticPoolEdition;
import com.azure.resourcemanager.sql.models.FailoverGroupReplicationRole;
import com.azure.resourcemanager.sql.models.ReadOnlyEndpointFailoverPolicy;
import com.azure.resourcemanager.sql.models.ReadWriteEndpointFailoverPolicy;
import com.azure.resourcemanager.sql.models.RecommendedElasticPool;
import com.azure.resourcemanager.sql.models.RegionCapabilities;
import com.azure.resourcemanager.sql.models.ReplicationLink;
import com.azure.resourcemanager.sql.models.SampleName;
import com.azure.resourcemanager.sql.models.ServiceObjective;
import com.azure.resourcemanager.sql.models.ServiceObjectiveName;
import com.azure.resourcemanager.sql.models.ServiceTierAdvisor;
import com.azure.resourcemanager.sql.models.SqlActiveDirectoryAdministrator;
import com.azure.resourcemanager.sql.models.SqlDatabase;
import com.azure.resourcemanager.sql.models.SqlDatabaseAutomaticTuning;
import com.azure.resourcemanager.sql.models.SqlDatabaseImportExportResponse;
import com.azure.resourcemanager.sql.models.SqlDatabaseStandardServiceObjective;
import com.azure.resourcemanager.sql.models.SqlElasticPool;
import com.azure.resourcemanager.sql.models.SqlFailoverGroup;
import com.azure.resourcemanager.sql.models.SqlFirewallRule;
import com.azure.resourcemanager.sql.models.SqlServer;
import com.azure.resourcemanager.sql.models.SqlServerAutomaticTuning;
import com.azure.resourcemanager.sql.models.SqlServerDnsAlias;
import com.azure.resourcemanager.sql.models.SqlSyncGroup;
import com.azure.resourcemanager.sql.models.SqlSyncMember;
import com.azure.resourcemanager.sql.models.SqlWarehouse;
import com.azure.resourcemanager.sql.models.SyncDirection;
import com.azure.resourcemanager.sql.models.SyncMemberDbType;
import com.azure.resourcemanager.sql.models.TransparentDataEncryption;
import com.azure.resourcemanager.sql.models.TransparentDataEncryptionActivity;
import com.azure.resourcemanager.sql.models.TransparentDataEncryptionStatus;
import com.azure.resourcemanager.storage.models.StorageAccount;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

public class SqlServerOperationsTests extends SqlServerTest {
    private static final String SQL_DATABASE_NAME = "myTestDatabase2";
    private static final String COLLATION = "SQL_Latin1_General_CP1_CI_AS";
    private static final String SQL_ELASTIC_POOL_NAME = "testElasticPool";
    private static final String SQL_FIREWALLRULE_NAME = "firewallrule1";
    private static final String START_IPADDRESS = "10.102.1.10";
    private static final String END_IPADDRESS = "10.102.1.12";

    @Test
    public void canCRUDSqlSyncMember() throws Exception {
        if (isPlaybackMode()) {
            return; // TODO: fix playback random fail
        }
        final String dbName = "dbSample";
        final String dbSyncName = "dbSync";
        final String dbMemberName = "dbMember";
        final String syncGroupName = "groupName";
        final String syncMemberName = "memberName";
        final String administratorLogin = "sqladmin";
        final String administratorPassword = password();

        // Create
        SqlServer sqlPrimaryServer =
            sqlServerManager
                .sqlServers()
                .define(sqlServerName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgName)
                .withAdministratorLogin(administratorLogin)
                .withAdministratorPassword(administratorPassword)
                .defineDatabase(dbName)
                .fromSample(SampleName.ADVENTURE_WORKS_LT)
                .withStandardEdition(SqlDatabaseStandardServiceObjective.S0)
                .attach()
                .defineDatabase(dbSyncName)
                .withStandardEdition(SqlDatabaseStandardServiceObjective.S0)
                .attach()
                .defineDatabase(dbMemberName)
                .withStandardEdition(SqlDatabaseStandardServiceObjective.S0)
                .attach()
                .create();

        SqlDatabase dbSource = sqlPrimaryServer.databases().get(dbName);
        SqlDatabase dbSync = sqlPrimaryServer.databases().get(dbSyncName);
        SqlDatabase dbMember = sqlPrimaryServer.databases().get(dbMemberName);

        SqlSyncGroup sqlSyncGroup =
            dbSync
                .syncGroups()
                .define(syncGroupName)
                .withSyncDatabaseId(dbSource.id())
                .withDatabaseUserName(administratorLogin)
                .withDatabasePassword(administratorPassword)
                .withConflictResolutionPolicyHubWins()
                .withInterval(-1)
                .create();
        Assertions.assertNotNull(sqlSyncGroup);

        SqlSyncMember sqlSyncMember =
            sqlSyncGroup
                .syncMembers()
                .define(syncMemberName)
                .withMemberSqlDatabase(dbMember)
                .withMemberUserName(administratorLogin)
                .withMemberPassword(administratorPassword)
                .withMemberDatabaseType(SyncMemberDbType.AZURE_SQL_DATABASE)
                .withDatabaseType(SyncDirection.ONE_WAY_MEMBER_TO_HUB)
                .create();
        Assertions.assertNotNull(sqlSyncMember);

        sqlSyncMember
            .update()
            .withDatabaseType(SyncDirection.BIDIRECTIONAL)
            .withMemberUserName(administratorLogin)
            .withMemberPassword(administratorPassword)
            .withMemberDatabaseType(SyncMemberDbType.AZURE_SQL_DATABASE)
            .apply();

        Assertions.assertFalse(sqlSyncGroup.syncMembers().list().isEmpty());

        sqlSyncMember =
            sqlServerManager
                .sqlServers()
                .syncMembers()
                .getBySqlServer(rgName, sqlServerName, dbSyncName, syncGroupName, syncMemberName);
        Assertions.assertNotNull(sqlSyncMember);

        sqlSyncMember.delete();

        sqlSyncGroup.delete();
    }

    @Test
    public void canCRUDSqlSyncGroup() throws Exception {
        if (isPlaybackMode()) {
            return; // TODO: fix playback random fail
        }
        final String dbName = "dbSample";
        final String dbSyncName = "dbSync";
        final String syncGroupName = "groupName";
        final String administratorLogin = "sqladmin";
        final String administratorPassword = password();

        // Create
        SqlServer sqlPrimaryServer =
            sqlServerManager
                .sqlServers()
                .define(sqlServerName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgName)
                .withAdministratorLogin(administratorLogin)
                .withAdministratorPassword(administratorPassword)
                .defineDatabase(dbName)
                .fromSample(SampleName.ADVENTURE_WORKS_LT)
                .withStandardEdition(SqlDatabaseStandardServiceObjective.S0)
                .attach()
                .defineDatabase(dbSyncName)
                .withStandardEdition(SqlDatabaseStandardServiceObjective.S0)
                .attach()
                .create();

        SqlDatabase dbSource = sqlPrimaryServer.databases().get(dbName);
        SqlDatabase dbSync = sqlPrimaryServer.databases().get(dbSyncName);

        SqlSyncGroup sqlSyncGroup =
            dbSync
                .syncGroups()
                .define(syncGroupName)
                .withSyncDatabaseId(dbSource.id())
                .withDatabaseUserName(administratorLogin)
                .withDatabasePassword(administratorPassword)
                .withConflictResolutionPolicyHubWins()
                .withInterval(-1)
                .create();

        Assertions.assertNotNull(sqlSyncGroup);

        sqlSyncGroup.update().withInterval(600).withConflictResolutionPolicyMemberWins().apply();

        Assertions
            .assertTrue(
                sqlServerManager
                    .sqlServers()
                    .syncGroups()
                    .listSyncDatabaseIds(Region.US_EAST)
                    .stream()
                    .findAny()
                    .isPresent());
        Assertions.assertFalse(dbSync.syncGroups().list().isEmpty());

        sqlSyncGroup =
            sqlServerManager.sqlServers().syncGroups().getBySqlServer(rgName, sqlServerName, dbSyncName, syncGroupName);
        Assertions.assertNotNull(sqlSyncGroup);

        sqlSyncGroup.delete();
    }

    @Test
    public void canCopySqlDatabase() throws Exception {
        final String sqlPrimaryServerName = sdkContext.randomResourceName("sqlpri", 22);
        final String sqlSecondaryServerName = sdkContext.randomResourceName("sqlsec", 22);
        final String epName = "epSample";
        final String dbName = "dbSample";
        final String administratorLogin = "sqladmin";
        final String administratorPassword = password();

        // Create
        SqlServer sqlPrimaryServer =
            sqlServerManager
                .sqlServers()
                .define(sqlPrimaryServerName)
                .withRegion(Region.US_EAST2)
                .withNewResourceGroup(rgName)
                .withAdministratorLogin(administratorLogin)
                .withAdministratorPassword(administratorPassword)
                .defineElasticPool(epName)
                .withPremiumPool()
                .attach()
                .defineDatabase(dbName)
                .withExistingElasticPool(epName)
                .fromSample(SampleName.ADVENTURE_WORKS_LT)
                .attach()
                .create();

        SqlServer sqlSecondaryServer =
            sqlServerManager
                .sqlServers()
                .define(sqlSecondaryServerName)
                .withRegion(Region.US_EAST)
                .withExistingResourceGroup(rgName)
                .withAdministratorLogin(administratorLogin)
                .withAdministratorPassword(administratorPassword)
                .create();

        SqlDatabase dbSample = sqlPrimaryServer.databases().get(dbName);

        SqlDatabase dbCopy =
            sqlSecondaryServer
                .databases()
                .define("dbCopy")
                .withSourceDatabase(dbSample)
                .withMode(CreateMode.COPY)
                .withServiceObjective(ServiceObjectiveName.P1)
                .create();

        Assertions.assertNotNull(dbCopy);
    }

    @Test
    public void canCRUDSqlFailoverGroup() throws Exception {
        final String sqlPrimaryServerName = sdkContext.randomResourceName("sqlpri", 22);
        final String sqlSecondaryServerName = sdkContext.randomResourceName("sqlsec", 22);
        final String sqlOtherServerName = sdkContext.randomResourceName("sql000", 22);
        final String failoverGroupName = sdkContext.randomResourceName("fg", 22);
        final String failoverGroupName2 = sdkContext.randomResourceName("fg2", 22);
        final String dbName = "dbSample";
        final String administratorLogin = "sqladmin";
        final String administratorPassword = password();

        // Create
        SqlServer sqlPrimaryServer =
            sqlServerManager
                .sqlServers()
                .define(sqlPrimaryServerName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgName)
                .withAdministratorLogin(administratorLogin)
                .withAdministratorPassword(administratorPassword)
                .defineDatabase(dbName)
                .fromSample(SampleName.ADVENTURE_WORKS_LT)
                .withStandardEdition(SqlDatabaseStandardServiceObjective.S0)
                .attach()
                .create();

        SqlServer sqlSecondaryServer =
            sqlServerManager
                .sqlServers()
                .define(sqlSecondaryServerName)
                .withRegion(Region.US_EAST2)
                .withExistingResourceGroup(rgName)
                .withAdministratorLogin(administratorLogin)
                .withAdministratorPassword(administratorPassword)
                .create();

        SqlServer sqlOtherServer =
            sqlServerManager
                .sqlServers()
                .define(sqlOtherServerName)
                .withRegion(Region.US_SOUTH_CENTRAL)
                .withExistingResourceGroup(rgName)
                .withAdministratorLogin(administratorLogin)
                .withAdministratorPassword(administratorPassword)
                .create();

        SqlFailoverGroup failoverGroup =
            sqlPrimaryServer
                .failoverGroups()
                .define(failoverGroupName)
                .withManualReadWriteEndpointPolicy()
                .withPartnerServerId(sqlSecondaryServer.id())
                .withReadOnlyEndpointPolicyDisabled()
                .create();
        Assertions.assertNotNull(failoverGroup);
        Assertions.assertEquals(failoverGroupName, failoverGroup.name());
        Assertions.assertEquals(rgName, failoverGroup.resourceGroupName());
        Assertions.assertEquals(sqlPrimaryServerName, failoverGroup.sqlServerName());
        Assertions.assertEquals(FailoverGroupReplicationRole.PRIMARY, failoverGroup.replicationRole());
        Assertions.assertEquals(1, failoverGroup.partnerServers().size());
        Assertions.assertEquals(sqlSecondaryServer.id(), failoverGroup.partnerServers().get(0).id());
        Assertions
            .assertEquals(
                FailoverGroupReplicationRole.SECONDARY, failoverGroup.partnerServers().get(0).replicationRole());
        Assertions.assertEquals(0, failoverGroup.databases().size());
        Assertions.assertEquals(0, failoverGroup.readWriteEndpointDataLossGracePeriodMinutes());
        Assertions.assertEquals(ReadWriteEndpointFailoverPolicy.MANUAL, failoverGroup.readWriteEndpointPolicy());
        Assertions.assertEquals(ReadOnlyEndpointFailoverPolicy.DISABLED, failoverGroup.readOnlyEndpointPolicy());

        SqlFailoverGroup failoverGroupOnPartner = sqlSecondaryServer.failoverGroups().get(failoverGroup.name());
        Assertions.assertEquals(failoverGroupName, failoverGroupOnPartner.name());
        Assertions.assertEquals(rgName, failoverGroupOnPartner.resourceGroupName());
        Assertions.assertEquals(sqlSecondaryServerName, failoverGroupOnPartner.sqlServerName());
        Assertions.assertEquals(FailoverGroupReplicationRole.SECONDARY, failoverGroupOnPartner.replicationRole());
        Assertions.assertEquals(1, failoverGroupOnPartner.partnerServers().size());
        Assertions.assertEquals(sqlPrimaryServer.id(), failoverGroupOnPartner.partnerServers().get(0).id());
        Assertions
            .assertEquals(
                FailoverGroupReplicationRole.PRIMARY, failoverGroupOnPartner.partnerServers().get(0).replicationRole());
        Assertions.assertEquals(0, failoverGroupOnPartner.databases().size());
        Assertions.assertEquals(0, failoverGroupOnPartner.readWriteEndpointDataLossGracePeriodMinutes());
        Assertions
            .assertEquals(ReadWriteEndpointFailoverPolicy.MANUAL, failoverGroupOnPartner.readWriteEndpointPolicy());
        Assertions
            .assertEquals(ReadOnlyEndpointFailoverPolicy.DISABLED, failoverGroupOnPartner.readOnlyEndpointPolicy());

        SqlFailoverGroup failoverGroup2 =
            sqlPrimaryServer
                .failoverGroups()
                .define(failoverGroupName2)
                .withAutomaticReadWriteEndpointPolicyAndDataLossGracePeriod(120)
                .withPartnerServerId(sqlOtherServer.id())
                .withReadOnlyEndpointPolicyEnabled()
                .create();
        Assertions.assertNotNull(failoverGroup2);
        Assertions.assertEquals(failoverGroupName2, failoverGroup2.name());
        Assertions.assertEquals(rgName, failoverGroup2.resourceGroupName());
        Assertions.assertEquals(sqlPrimaryServerName, failoverGroup2.sqlServerName());
        Assertions.assertEquals(FailoverGroupReplicationRole.PRIMARY, failoverGroup2.replicationRole());
        Assertions.assertEquals(1, failoverGroup2.partnerServers().size());
        Assertions.assertEquals(sqlOtherServer.id(), failoverGroup2.partnerServers().get(0).id());
        Assertions
            .assertEquals(
                FailoverGroupReplicationRole.SECONDARY, failoverGroup2.partnerServers().get(0).replicationRole());
        Assertions.assertEquals(0, failoverGroup2.databases().size());
        Assertions.assertEquals(120, failoverGroup2.readWriteEndpointDataLossGracePeriodMinutes());
        Assertions.assertEquals(ReadWriteEndpointFailoverPolicy.AUTOMATIC, failoverGroup2.readWriteEndpointPolicy());
        Assertions.assertEquals(ReadOnlyEndpointFailoverPolicy.ENABLED, failoverGroup2.readOnlyEndpointPolicy());

        failoverGroup
            .update()
            .withAutomaticReadWriteEndpointPolicyAndDataLossGracePeriod(120)
            .withReadOnlyEndpointPolicyEnabled()
            .withTag("tag1", "value1")
            .apply();
        Assertions.assertEquals(120, failoverGroup.readWriteEndpointDataLossGracePeriodMinutes());
        Assertions.assertEquals(ReadWriteEndpointFailoverPolicy.AUTOMATIC, failoverGroup.readWriteEndpointPolicy());
        Assertions.assertEquals(ReadOnlyEndpointFailoverPolicy.ENABLED, failoverGroup.readOnlyEndpointPolicy());

        SqlDatabase db = sqlPrimaryServer.databases().get(dbName);
        failoverGroup
            .update()
            .withManualReadWriteEndpointPolicy()
            .withReadOnlyEndpointPolicyDisabled()
            .withNewDatabaseId(db.id())
            .apply();
        Assertions.assertEquals(1, failoverGroup.databases().size());
        Assertions.assertEquals(db.id(), failoverGroup.databases().get(0));
        Assertions.assertEquals(0, failoverGroup.readWriteEndpointDataLossGracePeriodMinutes());
        Assertions.assertEquals(ReadWriteEndpointFailoverPolicy.MANUAL, failoverGroup.readWriteEndpointPolicy());
        Assertions.assertEquals(ReadOnlyEndpointFailoverPolicy.DISABLED, failoverGroup.readOnlyEndpointPolicy());

        List<SqlFailoverGroup> failoverGroupsList = sqlPrimaryServer.failoverGroups().list();
        Assertions.assertEquals(2, failoverGroupsList.size());

        failoverGroupsList = sqlSecondaryServer.failoverGroups().list();
        Assertions.assertEquals(1, failoverGroupsList.size());

        sqlPrimaryServer.failoverGroups().delete(failoverGroup2.name());
    }

    @Test
    public void canChangeSqlServerAndDatabaseAutomaticTuning() throws Exception {
        String sqlServerAdminName = "sqladmin";
        String sqlServerAdminPassword = password();
        String databaseName = "db-from-sample";
        String id = sdkContext.randomUuid();
        String storageName = sdkContext.randomResourceName(sqlServerName, 22);

        // Create
        SqlServer sqlServer =
            sqlServerManager
                .sqlServers()
                .define(sqlServerName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgName)
                .withAdministratorLogin(sqlServerAdminName)
                .withAdministratorPassword(sqlServerAdminPassword)
                .defineDatabase(databaseName)
                .fromSample(SampleName.ADVENTURE_WORKS_LT)
                .withBasicEdition()
                .attach()
                .create();
        SqlDatabase dbFromSample = sqlServer.databases().get(databaseName);
        Assertions.assertNotNull(dbFromSample);
        Assertions.assertEquals(DatabaseEdition.BASIC, dbFromSample.edition());

        SqlServerAutomaticTuning serverAutomaticTuning = sqlServer.getServerAutomaticTuning();
        Assertions.assertEquals(AutomaticTuningServerMode.AUTO, serverAutomaticTuning.desiredState());
        Assertions.assertEquals(AutomaticTuningServerMode.AUTO, serverAutomaticTuning.actualState());
        Assertions.assertEquals(4, serverAutomaticTuning.tuningOptions().size());

        serverAutomaticTuning
            .update()
            .withAutomaticTuningMode(AutomaticTuningServerMode.AUTO)
            .withAutomaticTuningOption("createIndex", AutomaticTuningOptionModeDesired.OFF)
            .withAutomaticTuningOption("dropIndex", AutomaticTuningOptionModeDesired.ON)
            .withAutomaticTuningOption("forceLastGoodPlan", AutomaticTuningOptionModeDesired.DEFAULT)
            .apply();
        Assertions.assertEquals(AutomaticTuningServerMode.AUTO, serverAutomaticTuning.desiredState());
        Assertions.assertEquals(AutomaticTuningServerMode.AUTO, serverAutomaticTuning.actualState());
        Assertions
            .assertEquals(
                AutomaticTuningOptionModeDesired.OFF,
                serverAutomaticTuning.tuningOptions().get("createIndex").desiredState());
        Assertions
            .assertEquals(
                AutomaticTuningOptionModeActual.OFF,
                serverAutomaticTuning.tuningOptions().get("createIndex").actualState());
        Assertions
            .assertEquals(
                AutomaticTuningOptionModeDesired.ON,
                serverAutomaticTuning.tuningOptions().get("dropIndex").desiredState());
        Assertions
            .assertEquals(
                AutomaticTuningOptionModeActual.ON,
                serverAutomaticTuning.tuningOptions().get("dropIndex").actualState());
        Assertions
            .assertEquals(
                AutomaticTuningOptionModeDesired.DEFAULT,
                serverAutomaticTuning.tuningOptions().get("forceLastGoodPlan").desiredState());

        SqlDatabaseAutomaticTuning databaseAutomaticTuning = dbFromSample.getDatabaseAutomaticTuning();
        Assertions.assertEquals(4, databaseAutomaticTuning.tuningOptions().size());

        // The following results in "InternalServerError" at the moment
        databaseAutomaticTuning
            .update()
            .withAutomaticTuningMode(AutomaticTuningMode.AUTO)
            .withAutomaticTuningOption("createIndex", AutomaticTuningOptionModeDesired.OFF)
            .withAutomaticTuningOption("dropIndex", AutomaticTuningOptionModeDesired.ON)
            .withAutomaticTuningOption("forceLastGoodPlan", AutomaticTuningOptionModeDesired.DEFAULT)
            .apply();
        Assertions.assertEquals(AutomaticTuningMode.AUTO, databaseAutomaticTuning.desiredState());
        Assertions.assertEquals(AutomaticTuningMode.AUTO, databaseAutomaticTuning.actualState());
        Assertions
            .assertEquals(
                AutomaticTuningOptionModeDesired.OFF,
                databaseAutomaticTuning.tuningOptions().get("createIndex").desiredState());
        Assertions
            .assertEquals(
                AutomaticTuningOptionModeActual.OFF,
                databaseAutomaticTuning.tuningOptions().get("createIndex").actualState());
        Assertions
            .assertEquals(
                AutomaticTuningOptionModeDesired.ON,
                databaseAutomaticTuning.tuningOptions().get("dropIndex").desiredState());
        Assertions
            .assertEquals(
                AutomaticTuningOptionModeActual.ON,
                databaseAutomaticTuning.tuningOptions().get("dropIndex").actualState());
        Assertions
            .assertEquals(
                AutomaticTuningOptionModeDesired.DEFAULT,
                databaseAutomaticTuning.tuningOptions().get("forceLastGoodPlan").desiredState());

        // cleanup
        dbFromSample.delete();
        sqlServerManager.sqlServers().deleteByResourceGroup(rgName, sqlServerName);
    }

    @Test
    public void canCreateAndAquireServerDnsAlias() throws Exception {
        String sqlServerName1 = sqlServerName + "1";
        String sqlServerName2 = sqlServerName + "2";
        String sqlServerAdminName = "sqladmin";
        String sqlServerAdminPassword = password();

        // Create
        SqlServer sqlServer1 =
            sqlServerManager
                .sqlServers()
                .define(sqlServerName1)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgName)
                .withAdministratorLogin(sqlServerAdminName)
                .withAdministratorPassword(sqlServerAdminPassword)
                .create();
        Assertions.assertNotNull(sqlServer1);

        SqlServerDnsAlias dnsAlias = sqlServer1.dnsAliases().define(sqlServerName).create();

        Assertions.assertNotNull(dnsAlias);
        Assertions.assertEquals(rgName, dnsAlias.resourceGroupName());
        Assertions.assertEquals(sqlServerName1, dnsAlias.sqlServerName());

        dnsAlias = sqlServerManager.sqlServers().dnsAliases().getBySqlServer(rgName, sqlServerName1, sqlServerName);
        Assertions.assertNotNull(dnsAlias);
        Assertions.assertEquals(rgName, dnsAlias.resourceGroupName());
        Assertions.assertEquals(sqlServerName1, dnsAlias.sqlServerName());

        Assertions.assertEquals(1, sqlServer1.databases().list().size());

        SqlServer sqlServer2 =
            sqlServerManager
                .sqlServers()
                .define(sqlServerName2)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgName)
                .withAdministratorLogin(sqlServerAdminName)
                .withAdministratorPassword(sqlServerAdminPassword)
                .create();
        Assertions.assertNotNull(sqlServer2);

        sqlServer2.dnsAliases().acquire(sqlServerName, sqlServer1.id());
        SdkContext.sleep(3 * 60 * 1000);

        dnsAlias = sqlServer2.dnsAliases().get(sqlServerName);
        Assertions.assertNotNull(dnsAlias);
        Assertions.assertEquals(rgName, dnsAlias.resourceGroupName());
        Assertions.assertEquals(sqlServerName2, dnsAlias.sqlServerName());

        // cleanup
        dnsAlias.delete();

        sqlServerManager.sqlServers().deleteByResourceGroup(rgName, sqlServerName1);
        sqlServerManager.sqlServers().deleteByResourceGroup(rgName, sqlServerName2);
    }

    @Test
    public void canGetSqlServerCapabilitiesAndCreateIdentity() throws Exception {
        String sqlServerAdminName = "sqladmin";
        String sqlServerAdminPassword = password();
        String databaseName = "db-from-sample";

        RegionCapabilities regionCapabilities = sqlServerManager.sqlServers().getCapabilitiesByRegion(Region.US_EAST);
        Assertions.assertNotNull(regionCapabilities);
        Assertions.assertNotNull(regionCapabilities.supportedCapabilitiesByServerVersion().get("12.0"));
        Assertions
            .assertTrue(
                regionCapabilities.supportedCapabilitiesByServerVersion().get("12.0").supportedEditions().size() > 0);
        Assertions
            .assertTrue(
                regionCapabilities
                        .supportedCapabilitiesByServerVersion()
                        .get("12.0")
                        .supportedElasticPoolEditions()
                        .size()
                    > 0);

        // Create
        SqlServer sqlServer =
            sqlServerManager
                .sqlServers()
                .define(sqlServerName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgName)
                .withAdministratorLogin(sqlServerAdminName)
                .withAdministratorPassword(sqlServerAdminPassword)
                .withSystemAssignedManagedServiceIdentity()
                .defineDatabase(databaseName)
                .fromSample(SampleName.ADVENTURE_WORKS_LT)
                .withBasicEdition()
                .attach()
                .create();
        SqlDatabase dbFromSample = sqlServer.databases().get(databaseName);
        Assertions.assertNotNull(dbFromSample);
        Assertions.assertEquals(DatabaseEdition.BASIC, dbFromSample.edition());

        Assertions.assertTrue(sqlServer.isManagedServiceIdentityEnabled());
        Assertions.assertEquals(sqlServerManager.tenantId(), sqlServer.systemAssignedManagedServiceIdentityTenantId());
        Assertions.assertNotNull(sqlServer.systemAssignedManagedServiceIdentityPrincipalId());

        sqlServer.update().withSystemAssignedManagedServiceIdentity().apply();
        Assertions.assertTrue(sqlServer.isManagedServiceIdentityEnabled());
        Assertions.assertEquals(sqlServerManager.tenantId(), sqlServer.systemAssignedManagedServiceIdentityTenantId());
        Assertions.assertNotNull(sqlServer.systemAssignedManagedServiceIdentityPrincipalId());

        // cleanup
        dbFromSample.delete();
        sqlServerManager.sqlServers().deleteByResourceGroup(rgName, sqlServerName);
    }

    @Test
    public void canCRUDSqlServerWithImportDatabase() throws Exception {
        if (isPlaybackMode()) {
            // The test makes calls to the Azure Storage data plane APIs which are not mocked at this time.
            return;
        }
        // Create

        String sqlServerAdminName = "sqladmin";
        String sqlServerAdminPassword = password();
        String id = sdkContext.randomUuid();
        String storageName = sdkContext.randomResourceName(sqlServerName, 22);

        SqlServer sqlServer =
            sqlServerManager
                .sqlServers()
                .define(sqlServerName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgName)
                .withAdministratorLogin(sqlServerAdminName)
                .withAdministratorPassword(sqlServerAdminPassword)
                .withActiveDirectoryAdministrator("DSEng", id)
                .create();

        SqlDatabase dbFromSample =
            sqlServer
                .databases()
                .define("db-from-sample")
                .fromSample(SampleName.ADVENTURE_WORKS_LT)
                .withBasicEdition()
                .withTag("tag1", "value1")
                .create();
        Assertions.assertNotNull(dbFromSample);
        Assertions.assertEquals(DatabaseEdition.BASIC, dbFromSample.edition());

        SqlDatabaseImportExportResponse exportedDB;
        StorageAccount storageAccount = null;
        try {
            storageAccount =
                storageManager.storageAccounts().getByResourceGroup(sqlServer.resourceGroupName(), storageName);
        } catch (ManagementException e) {
            Assertions.assertEquals(404, e.getResponse().getStatusCode());
        }
        if (storageAccount == null) {
            Creatable<StorageAccount> storageAccountCreatable =
                storageManager
                    .storageAccounts()
                    .define(storageName)
                    .withRegion(sqlServer.regionName())
                    .withExistingResourceGroup(sqlServer.resourceGroupName());

            exportedDB =
                dbFromSample
                    .exportTo(storageAccountCreatable, "from-sample", "dbfromsample.bacpac")
                    .withSqlAdministratorLoginAndPassword(sqlServerAdminName, sqlServerAdminPassword)
                    .execute();
            storageAccount =
                storageManager.storageAccounts().getByResourceGroup(sqlServer.resourceGroupName(), storageName);
        } else {
            exportedDB =
                dbFromSample
                    .exportTo(storageAccount, "from-sample", "dbfromsample.bacpac")
                    .withSqlAdministratorLoginAndPassword(sqlServerAdminName, sqlServerAdminPassword)
                    .execute();
        }

        SqlDatabase dbFromImport =
            sqlServer
                .databases()
                .define("db-from-import")
                .defineElasticPool("ep1")
                .withBasicPool()
                .attach()
                .importFrom(storageAccount, "from-sample", "dbfromsample.bacpac")
                .withSqlAdministratorLoginAndPassword(sqlServerAdminName, sqlServerAdminPassword)
                .withTag("tag2", "value2")
                .create();
        Assertions.assertNotNull(dbFromImport);
        Assertions.assertEquals("ep1", dbFromImport.elasticPoolName());

        dbFromImport.delete();
        dbFromSample.delete();
        sqlServer.elasticPools().delete("ep1");
        sqlServerManager.sqlServers().deleteByResourceGroup(rgName, sqlServerName);
    }

    @Test
    public void canCRUDSqlServerWithFirewallRule() throws Exception {
        // Create
        String sqlServerAdminName = "sqladmin";
        String id = sdkContext.randomUuid();

        SqlServer sqlServer =
            sqlServerManager
                .sqlServers()
                .define(sqlServerName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgName)
                .withAdministratorLogin(sqlServerAdminName)
                .withAdministratorPassword(password())
                .withActiveDirectoryAdministrator("DSEng", id)
                .withoutAccessFromAzureServices()
                .defineFirewallRule("somefirewallrule1")
                .withIpAddress("0.0.0.1")
                .attach()
                .withTag("tag1", "value1")
                .create();
        Assertions.assertEquals(sqlServerAdminName, sqlServer.administratorLogin());
        Assertions.assertEquals("v12.0", sqlServer.kind());
        Assertions.assertEquals("12.0", sqlServer.version());

        sqlServer = sqlServerManager.sqlServers().getByResourceGroup(rgName, sqlServerName);
        Assertions.assertEquals(sqlServerAdminName, sqlServer.administratorLogin());
        Assertions.assertEquals("v12.0", sqlServer.kind());
        Assertions.assertEquals("12.0", sqlServer.version());

        SqlActiveDirectoryAdministrator sqlADAdmin = sqlServer.getActiveDirectoryAdministrator();
        Assertions.assertNotNull(sqlADAdmin);
        Assertions.assertEquals("DSEng", sqlADAdmin.signInName());
        Assertions.assertNotNull(sqlADAdmin.id());
        Assertions.assertEquals(AdministratorType.ACTIVE_DIRECTORY, sqlADAdmin.administratorType());

        sqlADAdmin = sqlServer.setActiveDirectoryAdministrator("DSEngAll", id);
        Assertions.assertNotNull(sqlADAdmin);
        Assertions.assertEquals("DSEngAll", sqlADAdmin.signInName());
        Assertions.assertNotNull(sqlADAdmin.id());
        Assertions.assertEquals(AdministratorType.ACTIVE_DIRECTORY, sqlADAdmin.administratorType());
        sqlServer.removeActiveDirectoryAdministrator();

        final SqlServer finalSqlServer = sqlServer;
        validateResourceNotFound(() -> finalSqlServer.getActiveDirectoryAdministrator());

        SqlFirewallRule firewallRule =
            sqlServerManager.sqlServers().firewallRules().getBySqlServer(rgName, sqlServerName, "somefirewallrule1");
        Assertions.assertEquals("0.0.0.1", firewallRule.startIpAddress());
        Assertions.assertEquals("0.0.0.1", firewallRule.endIpAddress());

        validateResourceNotFound(
            () ->
                sqlServerManager
                    .sqlServers()
                    .firewallRules()
                    .getBySqlServer(rgName, sqlServerName, "AllowAllWindowsAzureIps"));

        sqlServer.enableAccessFromAzureServices();
        firewallRule =
            sqlServerManager
                .sqlServers()
                .firewallRules()
                .getBySqlServer(rgName, sqlServerName, "AllowAllWindowsAzureIps");
        Assertions.assertEquals("0.0.0.0", firewallRule.startIpAddress());
        Assertions.assertEquals("0.0.0.0", firewallRule.endIpAddress());

        sqlServer.update().withNewFirewallRule("0.0.0.2", "0.0.0.2", "newFirewallRule1").apply();
        sqlServer.firewallRules().delete("newFirewallRule2");

        final SqlServer finalSqlServer1 = sqlServer;
        validateResourceNotFound(() -> finalSqlServer1.firewallRules().get("newFirewallRule2"));

        firewallRule =
            sqlServerManager
                .sqlServers()
                .firewallRules()
                .define("newFirewallRule2")
                .withExistingSqlServer(rgName, sqlServerName)
                .withIpAddress("0.0.0.3")
                .create();

        Assertions.assertEquals("0.0.0.3", firewallRule.startIpAddress());
        Assertions.assertEquals("0.0.0.3", firewallRule.endIpAddress());

        firewallRule = firewallRule.update().withStartIpAddress("0.0.0.1").apply();

        Assertions.assertEquals("0.0.0.1", firewallRule.startIpAddress());
        Assertions.assertEquals("0.0.0.3", firewallRule.endIpAddress());

        sqlServer.firewallRules().delete("somefirewallrule1");
        validateResourceNotFound(
            () ->
                sqlServerManager
                    .sqlServers()
                    .firewallRules()
                    .getBySqlServer(rgName, sqlServerName, "somefirewallrule1"));

        firewallRule = sqlServer.firewallRules().define("somefirewallrule2").withIpAddress("0.0.0.4").create();

        Assertions.assertEquals("0.0.0.4", firewallRule.startIpAddress());
        Assertions.assertEquals("0.0.0.4", firewallRule.endIpAddress());

        firewallRule.delete();
    }

    @Disabled("Depends on the existing SQL server")
    @Test
    public void canListRecommendedElasticPools() throws Exception {
        SqlServer sqlServer = sqlServerManager.sqlServers().getByResourceGroup("ans", "ans-secondary");
        sqlServer
            .databases()
            .list()
            .get(0)
            .listServiceTierAdvisors()
            .values()
            .iterator()
            .next()
            .serviceLevelObjectiveUsageMetric();
        Map<String, RecommendedElasticPool> recommendedElasticPools = sqlServer.listRecommendedElasticPools();
        Assertions.assertNotNull(recommendedElasticPools);
    }

    @Test
    public void canCRUDSqlServer() throws Exception {

        // Check if the name is available
        CheckNameAvailabilityResult checkNameResult =
            sqlServerManager.sqlServers().checkNameAvailability(sqlServerName);
        Assertions.assertTrue(checkNameResult.isAvailable());

        // Create
        SqlServer sqlServer = createSqlServer();

        validateSqlServer(sqlServer);

        // Confirm the server name is unavailable
        checkNameResult = sqlServerManager.sqlServers().checkNameAvailability(sqlServerName);
        Assertions.assertFalse(checkNameResult.isAvailable());
        Assertions
            .assertEquals(
                CheckNameAvailabilityReason.ALREADY_EXISTS.toString(), checkNameResult.unavailabilityReason());

        List<ServiceObjective> serviceObjectives = sqlServer.listServiceObjectives();

        Assertions.assertNotEquals(serviceObjectives.size(), 0);
        Assertions.assertNotNull(serviceObjectives.get(0).refresh());
        Assertions.assertNotNull(sqlServer.getServiceObjective("d1737d22-a8ea-4de7-9bd0-33395d2a7419"));

        sqlServer.update().withAdministratorPassword("P@ssword~2").apply();

        // List
        PagedIterable<SqlServer> sqlServers = sqlServerManager.sqlServers().listByResourceGroup(rgName);
        boolean found = false;
        for (SqlServer server : sqlServers) {
            if (server.name().equals(sqlServerName)) {
                found = true;
            }
        }
        Assertions.assertTrue(found);
        // Get
        sqlServer = sqlServerManager.sqlServers().getByResourceGroup(rgName, sqlServerName);
        Assertions.assertNotNull(sqlServer);

        sqlServerManager.sqlServers().deleteByResourceGroup(sqlServer.resourceGroupName(), sqlServer.name());
        validateSqlServerNotFound(sqlServer);
    }

    @Test
    public void canUseCoolShortcutsForResourceCreation() throws Exception {
        if (isPlaybackMode()) {
            return; // TODO: fix playback random fail
        }
        String database2Name = "database2";
        String database1InEPName = "database1InEP";
        String database2InEPName = "database2InEP";
        String elasticPool2Name = "elasticPool2";
        String elasticPool3Name = "elasticPool3";
        String elasticPool1Name = SQL_ELASTIC_POOL_NAME;

        // Create
        SqlServer sqlServer =
            sqlServerManager
                .sqlServers()
                .define(sqlServerName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgName)
                .withAdministratorLogin("userName")
                .withAdministratorPassword("Password~1")
                .withoutAccessFromAzureServices()
                .withNewDatabase(SQL_DATABASE_NAME)
                .withNewDatabase(database2Name)
                .withNewElasticPool(elasticPool1Name, ElasticPoolEdition.STANDARD)
                .withNewElasticPool(elasticPool2Name, ElasticPoolEdition.PREMIUM, database1InEPName, database2InEPName)
                .withNewElasticPool(elasticPool3Name, ElasticPoolEdition.STANDARD)
                .withNewFirewallRule(START_IPADDRESS, END_IPADDRESS, SQL_FIREWALLRULE_NAME)
                .withNewFirewallRule(START_IPADDRESS, END_IPADDRESS)
                .withNewFirewallRule(START_IPADDRESS)
                .create();

        validateMultiCreation(
            database2Name,
            database1InEPName,
            database2InEPName,
            elasticPool1Name,
            elasticPool2Name,
            elasticPool3Name,
            sqlServer,
            false);
        elasticPool1Name = SQL_ELASTIC_POOL_NAME + " U";
        database2Name = "database2U";
        database1InEPName = "database1InEPU";
        database2InEPName = "database2InEPU";
        elasticPool2Name = "elasticPool2U";
        elasticPool3Name = "elasticPool3U";

        // Update
        sqlServer =
            sqlServer
                .update()
                .withNewDatabase(SQL_DATABASE_NAME)
                .withNewDatabase(database2Name)
                .withNewElasticPool(elasticPool1Name, ElasticPoolEdition.STANDARD)
                .withNewElasticPool(elasticPool2Name, ElasticPoolEdition.PREMIUM, database1InEPName, database2InEPName)
                .withNewElasticPool(elasticPool3Name, ElasticPoolEdition.STANDARD)
                .withNewFirewallRule(START_IPADDRESS, END_IPADDRESS, SQL_FIREWALLRULE_NAME)
                .withNewFirewallRule(START_IPADDRESS, END_IPADDRESS)
                .withNewFirewallRule(START_IPADDRESS)
                .withTag("tag2", "value2")
                .apply();

        validateMultiCreation(
            database2Name,
            database1InEPName,
            database2InEPName,
            elasticPool1Name,
            elasticPool2Name,
            elasticPool3Name,
            sqlServer,
            true);

        sqlServer.refresh();
        Assertions.assertEquals(sqlServer.elasticPools().list().size(), 0);

        // List
        PagedIterable<SqlServer> sqlServers = sqlServerManager.sqlServers().listByResourceGroup(rgName);
        boolean found = false;
        for (SqlServer server : sqlServers) {
            if (server.name().equals(sqlServerName)) {
                found = true;
            }
        }

        Assertions.assertTrue(found);
        // Get
        sqlServer = sqlServerManager.sqlServers().getByResourceGroup(rgName, sqlServerName);
        Assertions.assertNotNull(sqlServer);

        sqlServerManager.sqlServers().deleteByResourceGroup(sqlServer.resourceGroupName(), sqlServer.name());
        validateSqlServerNotFound(sqlServer);
    }

    @Test
    public void canCRUDSqlDatabase() throws Exception {
        // Create
        SqlServer sqlServer = createSqlServer();
        Flux<Indexable> resourceStream =
            sqlServer.databases().define(SQL_DATABASE_NAME).withEdition(DatabaseEdition.STANDARD).createAsync();

        SqlDatabase sqlDatabase = Utils.<SqlDatabase>rootResource(resourceStream.last()).block();

        validateSqlDatabase(sqlDatabase, SQL_DATABASE_NAME);
        Assertions.assertTrue(sqlServer.databases().list().size() > 0);

        // Test transparent data encryption settings.
        TransparentDataEncryption transparentDataEncryption = sqlDatabase.getTransparentDataEncryption();
        Assertions.assertNotNull(transparentDataEncryption.status());

        List<TransparentDataEncryptionActivity> transparentDataEncryptionActivities =
            transparentDataEncryption.listActivities();
        Assertions.assertNotNull(transparentDataEncryptionActivities);

        transparentDataEncryption = transparentDataEncryption.updateStatus(TransparentDataEncryptionStatus.ENABLED);
        Assertions.assertNotNull(transparentDataEncryption);
        Assertions.assertEquals(transparentDataEncryption.status(), TransparentDataEncryptionStatus.ENABLED);

        transparentDataEncryptionActivities = transparentDataEncryption.listActivities();
        Assertions.assertNotNull(transparentDataEncryptionActivities);

        TestUtilities.sleep(10000, isRecordMode());
        transparentDataEncryption =
            sqlDatabase.getTransparentDataEncryption().updateStatus(TransparentDataEncryptionStatus.DISABLED);
        Assertions.assertNotNull(transparentDataEncryption);
        Assertions.assertEquals(transparentDataEncryption.status(), TransparentDataEncryptionStatus.DISABLED);
        Assertions.assertEquals(transparentDataEncryption.sqlServerName(), sqlServerName);
        Assertions.assertEquals(transparentDataEncryption.databaseName(), SQL_DATABASE_NAME);
        Assertions.assertNotNull(transparentDataEncryption.name());
        Assertions.assertNotNull(transparentDataEncryption.id());
        // Done testing with encryption settings.

        // Assertions.assertNotNull(sqlDatabase.getUpgradeHint()); // This property is null

        // Test Service tier advisors.
        Map<String, ServiceTierAdvisor> serviceTierAdvisors = sqlDatabase.listServiceTierAdvisors();
        Assertions.assertNotNull(serviceTierAdvisors);
        Assertions.assertNotNull(serviceTierAdvisors.values().iterator().next().serviceLevelObjectiveUsageMetric());
        Assertions.assertNotEquals(serviceTierAdvisors.size(), 0);

        Assertions.assertNotNull(serviceTierAdvisors.values().iterator().next().refresh());
        Assertions.assertNotNull(serviceTierAdvisors.values().iterator().next().serviceLevelObjectiveUsageMetric());
        // End of testing service tier advisors.

        sqlServer = sqlServerManager.sqlServers().getByResourceGroup(rgName, sqlServerName);
        validateSqlServer(sqlServer);

        // Create another database with above created database as source database.
        Creatable<SqlElasticPool> sqlElasticPoolCreatable =
            sqlServer.elasticPools().define(SQL_ELASTIC_POOL_NAME).withEdition(ElasticPoolEdition.STANDARD);
        String anotherDatabaseName = "anotherDatabase";
        SqlDatabase anotherDatabase =
            sqlServer
                .databases()
                .define(anotherDatabaseName)
                .withNewElasticPool(sqlElasticPoolCreatable)
                .withSourceDatabase(sqlDatabase.id())
                .withMode(CreateMode.COPY)
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
        resourceStream =
            sqlServer
                .databases()
                .define("newDatabase")
                .withEdition(DatabaseEdition.STANDARD)
                .withCollation(COLLATION)
                .createAsync();

        sqlDatabase = Utils.<SqlDatabase>rootResource(resourceStream.last()).block();

        // Rename the database
        sqlDatabase = sqlDatabase.rename("renamedDatabase");
        validateSqlDatabase(sqlDatabase, "renamedDatabase");

        sqlServer.databases().delete(sqlDatabase.name());

        sqlServerManager.sqlServers().deleteByResourceGroup(sqlServer.resourceGroupName(), sqlServer.name());
        validateSqlServerNotFound(sqlServer);
    }

    @Test
    public void canManageReplicationLinks() throws Exception {
        // Create
        String anotherSqlServerName = sqlServerName + "another";
        SqlServer sqlServer1 = createSqlServer();
        SqlServer sqlServer2 = createSqlServer(anotherSqlServerName);

        Flux<Indexable> resourceStream =
            sqlServer1
                .databases()
                .define(SQL_DATABASE_NAME)
                .withEdition(DatabaseEdition.STANDARD)
                .withCollation(COLLATION)
                .createAsync();

        SqlDatabase databaseInServer1 = Utils.<SqlDatabase>rootResource(resourceStream.last()).block();

        validateSqlDatabase(databaseInServer1, SQL_DATABASE_NAME);
        SqlDatabase databaseInServer2 =
            sqlServer2
                .databases()
                .define(SQL_DATABASE_NAME)
                .withSourceDatabase(databaseInServer1.id())
                .withMode(CreateMode.ONLINE_SECONDARY)
                .create();
        TestUtilities.sleep(2000, isRecordMode());
        List<ReplicationLink> replicationLinksInDb1 =
            new ArrayList<>(databaseInServer1.listReplicationLinks().values());

        Assertions.assertEquals(replicationLinksInDb1.size(), 1);
        Assertions.assertEquals(replicationLinksInDb1.get(0).partnerDatabase(), databaseInServer2.name());
        Assertions.assertEquals(replicationLinksInDb1.get(0).partnerServer(), databaseInServer2.sqlServerName());

        List<ReplicationLink> replicationLinksInDb2 =
            new ArrayList<>(databaseInServer2.listReplicationLinks().values());

        Assertions.assertEquals(replicationLinksInDb2.size(), 1);
        Assertions.assertEquals(replicationLinksInDb2.get(0).partnerDatabase(), databaseInServer1.name());
        Assertions.assertEquals(replicationLinksInDb2.get(0).partnerServer(), databaseInServer1.sqlServerName());

        Assertions.assertNotNull(replicationLinksInDb1.get(0).refresh());

        // Failover
        replicationLinksInDb2.get(0).failover();
        replicationLinksInDb2.get(0).refresh();
        TestUtilities.sleep(30000, isRecordMode());
        // Force failover
        replicationLinksInDb1.get(0).forceFailoverAllowDataLoss();
        replicationLinksInDb1.get(0).refresh();

        TestUtilities.sleep(30000, isRecordMode());

        replicationLinksInDb2.get(0).delete();
        Assertions.assertEquals(databaseInServer2.listReplicationLinks().size(), 0);

        sqlServer1.databases().delete(databaseInServer1.name());
        sqlServer2.databases().delete(databaseInServer2.name());

        sqlServerManager.sqlServers().deleteByResourceGroup(sqlServer2.resourceGroupName(), sqlServer2.name());
        validateSqlServerNotFound(sqlServer2);
        sqlServerManager.sqlServers().deleteByResourceGroup(sqlServer1.resourceGroupName(), sqlServer1.name());
        validateSqlServerNotFound(sqlServer1);
    }

    @Test
    public void canDoOperationsOnDataWarehouse() throws Exception {
        // Create
        SqlServer sqlServer = createSqlServer();

        validateSqlServer(sqlServer);

        // List usages for the server.
        Assertions.assertNotNull(sqlServer.listUsageMetrics());

        Flux<Indexable> resourceStream =
            sqlServer
                .databases()
                .define(SQL_DATABASE_NAME)
                .withEdition(DatabaseEdition.DATA_WAREHOUSE)
                .withServiceObjective(ServiceObjectiveName.fromString("DW1000C"))
                .withCollation(COLLATION)
                .createAsync();

        SqlDatabase sqlDatabase = Utils.<SqlDatabase>rootResource(resourceStream.last()).block();
        Assertions.assertNotNull(sqlDatabase);

        sqlDatabase = sqlServer.databases().get(SQL_DATABASE_NAME);
        Assertions.assertNotNull(sqlDatabase);
        Assertions.assertTrue(sqlDatabase.isDataWarehouse());

        // Get
        SqlWarehouse dataWarehouse = sqlServer.databases().get(SQL_DATABASE_NAME).asWarehouse();

        Assertions.assertNotNull(dataWarehouse);
        Assertions.assertEquals(dataWarehouse.name(), SQL_DATABASE_NAME);
        Assertions.assertEquals(dataWarehouse.edition(), DatabaseEdition.DATA_WAREHOUSE);

        // List Restore points.
        Assertions.assertNotNull(dataWarehouse.listRestorePoints());
        // Get usages.
        Assertions.assertNotNull(dataWarehouse.listUsageMetrics());

        // Pause warehouse
        dataWarehouse.pauseDataWarehouse();

        // Resume warehouse
        dataWarehouse.resumeDataWarehouse();

        sqlServer.databases().delete(SQL_DATABASE_NAME);

        sqlServerManager.sqlServers().deleteByResourceGroup(sqlServer.resourceGroupName(), sqlServer.name());
        validateSqlServerNotFound(sqlServer);
    }

    @Test
    public void canCRUDSqlDatabaseWithElasticPool() throws Exception {
        // Create
        SqlServer sqlServer = createSqlServer();

        Creatable<SqlElasticPool> sqlElasticPoolCreatable =
            sqlServer
                .elasticPools()
                .define(SQL_ELASTIC_POOL_NAME)
                .withEdition(ElasticPoolEdition.STANDARD)
                .withTag("tag1", "value1");

        Flux<Indexable> resourceStream =
            sqlServer
                .databases()
                .define(SQL_DATABASE_NAME)
                .withNewElasticPool(sqlElasticPoolCreatable)
                .withCollation(COLLATION)
                .createAsync();

        SqlDatabase sqlDatabase = Utils.<SqlDatabase>rootResource(resourceStream.last()).block();

        validateSqlDatabase(sqlDatabase, SQL_DATABASE_NAME);

        sqlServer = sqlServerManager.sqlServers().getByResourceGroup(rgName, sqlServerName);
        validateSqlServer(sqlServer);

        // Get Elastic pool
        SqlElasticPool elasticPool = sqlServer.elasticPools().get(SQL_ELASTIC_POOL_NAME);
        validateSqlElasticPool(elasticPool);

        // Get
        validateSqlDatabaseWithElasticPool(sqlServer.databases().get(SQL_DATABASE_NAME), SQL_DATABASE_NAME);

        // List
        validateListSqlDatabase(sqlServer.databases().list());

        // Remove database from elastic pools.
        sqlDatabase
            .update()
            .withoutElasticPool()
            .withEdition(DatabaseEdition.STANDARD)
            .withServiceObjective(ServiceObjectiveName.S3)
            .apply();
        sqlDatabase = sqlServer.databases().get(SQL_DATABASE_NAME);
        Assertions.assertNull(sqlDatabase.elasticPoolName());

        // Update edition of the SQL database
        sqlDatabase.update().withEdition(DatabaseEdition.PREMIUM).withServiceObjective(ServiceObjectiveName.P1).apply();
        sqlDatabase = sqlServer.databases().get(SQL_DATABASE_NAME);
        Assertions.assertEquals(sqlDatabase.edition(), DatabaseEdition.PREMIUM);
        Assertions.assertEquals(sqlDatabase.currentServiceObjectiveName(), ServiceObjectiveName.P1.toString());

        // Update just the service level objective for database.
        sqlDatabase.update().withServiceObjective(ServiceObjectiveName.P2).apply();
        sqlDatabase = sqlServer.databases().get(SQL_DATABASE_NAME);
        Assertions.assertEquals(sqlDatabase.currentServiceObjectiveName(), ServiceObjectiveName.P2.toString());
        Assertions.assertEquals(sqlDatabase.requestedServiceObjectiveName(), ServiceObjectiveName.P2.toString());

        // Update max size bytes of the database.
        sqlDatabase.update().withMaxSizeBytes(268435456000L).apply();

        sqlDatabase = sqlServer.databases().get(SQL_DATABASE_NAME);
        Assertions.assertEquals(sqlDatabase.maxSizeBytes(), 268435456000L);

        // Put the database back in elastic pool.
        sqlDatabase.update().withExistingElasticPool(SQL_ELASTIC_POOL_NAME).apply();

        sqlDatabase = sqlServer.databases().get(SQL_DATABASE_NAME);
        Assertions.assertEquals(sqlDatabase.elasticPoolName(), SQL_ELASTIC_POOL_NAME);

        // List Activity in elastic pool
        Assertions.assertNotNull(elasticPool.listActivities());

        // List Database activity in elastic pool.
        Assertions.assertNotNull(elasticPool.listDatabaseActivities());

        // List databases in elastic pool.
        List<SqlDatabase> databasesInElasticPool = elasticPool.listDatabases();
        Assertions.assertNotNull(databasesInElasticPool);
        Assertions.assertEquals(databasesInElasticPool.size(), 1);

        // Get a particular database in elastic pool.
        SqlDatabase databaseInElasticPool = elasticPool.getDatabase(SQL_DATABASE_NAME);
        validateSqlDatabase(databaseInElasticPool, SQL_DATABASE_NAME);

        // Refresh works on the database got from elastic pool.
        databaseInElasticPool.refresh();

        // Validate that trying to get an invalid database from elastic pool returns null.
        validateResourceNotFound(() -> elasticPool.getDatabase("does_not_exist"));

        // Delete
        sqlServer.databases().delete(SQL_DATABASE_NAME);
        validateSqlDatabaseNotFound(SQL_DATABASE_NAME);

        SqlElasticPool sqlElasticPool = sqlServer.elasticPools().get(SQL_ELASTIC_POOL_NAME);

        // Add another database to the server and pool.
        resourceStream =
            sqlServer
                .databases()
                .define("newDatabase")
                .withExistingElasticPool(sqlElasticPool)
                .withCollation(COLLATION)
                .createAsync();

        sqlDatabase = Utils.<SqlDatabase>rootResource(resourceStream.last()).block();
        sqlServer.databases().delete(sqlDatabase.name());
        validateSqlDatabaseNotFound("newDatabase");

        sqlServer.elasticPools().delete(SQL_ELASTIC_POOL_NAME);
        sqlServerManager.sqlServers().deleteByResourceGroup(sqlServer.resourceGroupName(), sqlServer.name());
        validateSqlServerNotFound(sqlServer);
    }

    @Test
    public void canCRUDSqlElasticPool() throws Exception {
        // Create
        SqlServer sqlServer = createSqlServer();

        sqlServer = sqlServerManager.sqlServers().getByResourceGroup(rgName, sqlServerName);
        validateSqlServer(sqlServer);

        Flux<Indexable> resourceStream =
            sqlServer
                .elasticPools()
                .define(SQL_ELASTIC_POOL_NAME)
                .withEdition(ElasticPoolEdition.STANDARD)
                .withTag("tag1", "value1")
                .createAsync();
        SqlElasticPool sqlElasticPool = Utils.<SqlElasticPool>rootResource(resourceStream.last()).block();
        validateSqlElasticPool(sqlElasticPool);
        Assertions.assertEquals(sqlElasticPool.listDatabases().size(), 0);

        sqlElasticPool =
            sqlElasticPool
                .update()
                .withDtu(100)
                .withDatabaseDtuMax(20)
                .withDatabaseDtuMin(10)
                .withStorageCapacity(102400 * 1024 * 1024L)
                .withNewDatabase(SQL_DATABASE_NAME)
                .withTag("tag2", "value2")
                .apply();

        validateSqlElasticPool(sqlElasticPool);
        Assertions.assertEquals(sqlElasticPool.listDatabases().size(), 1);
        Assertions.assertNotNull(sqlElasticPool.getDatabase(SQL_DATABASE_NAME));

        // Get
        validateSqlElasticPool(sqlServer.elasticPools().get(SQL_ELASTIC_POOL_NAME));

        // List
        validateListSqlElasticPool(sqlServer.elasticPools().list());

        // Delete
        sqlServer.databases().delete(SQL_DATABASE_NAME);
        sqlServer.elasticPools().delete(SQL_ELASTIC_POOL_NAME);
        validateSqlElasticPoolNotFound(sqlServer, SQL_ELASTIC_POOL_NAME);

        // Add another database to the server
        resourceStream =
            sqlServer.elasticPools().define("newElasticPool").withEdition(ElasticPoolEdition.STANDARD).createAsync();

        sqlElasticPool = Utils.<SqlElasticPool>rootResource(resourceStream.last()).block();

        sqlServer.elasticPools().delete(sqlElasticPool.name());
        validateSqlElasticPoolNotFound(sqlServer, "newElasticPool");

        sqlServerManager.sqlServers().deleteByResourceGroup(sqlServer.resourceGroupName(), sqlServer.name());
        validateSqlServerNotFound(sqlServer);
    }

    @Test
    public void canCRUDSqlFirewallRule() throws Exception {
        // Create
        SqlServer sqlServer = createSqlServer();

        sqlServer = sqlServerManager.sqlServers().getByResourceGroup(rgName, sqlServerName);
        validateSqlServer(sqlServer);

        Flux<Indexable> resourceStream =
            sqlServer
                .firewallRules()
                .define(SQL_FIREWALLRULE_NAME)
                .withIpAddressRange(START_IPADDRESS, END_IPADDRESS)
                .createAsync();

        SqlFirewallRule sqlFirewallRule = Utils.<SqlFirewallRule>rootResource(resourceStream.last()).block();

        validateSqlFirewallRule(sqlFirewallRule, SQL_FIREWALLRULE_NAME);
        validateSqlFirewallRule(sqlServer.firewallRules().get(SQL_FIREWALLRULE_NAME), SQL_FIREWALLRULE_NAME);

        String secondFirewallRuleName = "secondFireWallRule";
        SqlFirewallRule secondFirewallRule =
            sqlServer.firewallRules().define(secondFirewallRuleName).withIpAddress(START_IPADDRESS).create();
        Assertions.assertNotNull(secondFirewallRule);

        secondFirewallRule = sqlServer.firewallRules().get(secondFirewallRuleName);
        Assertions.assertNotNull(secondFirewallRule);
        Assertions.assertEquals(START_IPADDRESS, secondFirewallRule.endIpAddress());

        secondFirewallRule = secondFirewallRule.update().withEndIpAddress(END_IPADDRESS).apply();

        validateSqlFirewallRule(secondFirewallRule, secondFirewallRuleName);
        sqlServer.firewallRules().delete(secondFirewallRuleName);

        final SqlServer finalSqlServer = sqlServer;
        validateResourceNotFound(() -> finalSqlServer.firewallRules().get(secondFirewallRuleName));

        // Get
        sqlFirewallRule = sqlServer.firewallRules().get(SQL_FIREWALLRULE_NAME);
        validateSqlFirewallRule(sqlFirewallRule, SQL_FIREWALLRULE_NAME);

        // Update
        // Making start and end IP address same.
        sqlFirewallRule.update().withEndIpAddress(START_IPADDRESS).apply();
        sqlFirewallRule = sqlServer.firewallRules().get(SQL_FIREWALLRULE_NAME);
        Assertions.assertEquals(sqlFirewallRule.endIpAddress(), START_IPADDRESS);

        // List
        validateListSqlFirewallRule(sqlServer.firewallRules().list());

        // Delete
        sqlServer.firewallRules().delete(sqlFirewallRule.name());
        validateSqlFirewallRuleNotFound();

        // Delete server
        sqlServerManager.sqlServers().deleteByResourceGroup(sqlServer.resourceGroupName(), sqlServer.name());
        validateSqlServerNotFound(sqlServer);
    }

    private void validateMultiCreation(
        String database2Name,
        String database1InEPName,
        String database2InEPName,
        String elasticPool1Name,
        String elasticPool2Name,
        String elasticPool3Name,
        SqlServer sqlServer,
        boolean deleteUsingUpdate) {
        validateSqlServer(sqlServer);
        validateSqlServer(sqlServerManager.sqlServers().getByResourceGroup(rgName, sqlServerName));
        validateSqlDatabase(sqlServer.databases().get(SQL_DATABASE_NAME), SQL_DATABASE_NAME);
        validateSqlFirewallRule(sqlServer.firewallRules().get(SQL_FIREWALLRULE_NAME), SQL_FIREWALLRULE_NAME);

        List<SqlFirewallRule> firewalls = sqlServer.firewallRules().list();
        Assertions.assertEquals(3, firewalls.size());

        int startIPAddress = 0;
        int endIPAddress = 0;

        for (SqlFirewallRule firewall : firewalls) {
            if (!firewall.name().equalsIgnoreCase(SQL_FIREWALLRULE_NAME)) {
                Assertions.assertEquals(firewall.startIpAddress(), START_IPADDRESS);
                if (firewall.endIpAddress().equalsIgnoreCase(START_IPADDRESS)) {
                    startIPAddress++;
                } else if (firewall.endIpAddress().equalsIgnoreCase(END_IPADDRESS)) {
                    endIPAddress++;
                }
            }
        }

        Assertions.assertEquals(startIPAddress, 1);
        Assertions.assertEquals(endIPAddress, 1);

        Assertions.assertNotNull(sqlServer.databases().get(database2Name));
        Assertions.assertNotNull(sqlServer.databases().get(database1InEPName));
        Assertions.assertNotNull(sqlServer.databases().get(database2InEPName));

        SqlElasticPool ep1 = sqlServer.elasticPools().get(elasticPool1Name);
        validateSqlElasticPool(ep1, elasticPool1Name);
        SqlElasticPool ep2 = sqlServer.elasticPools().get(elasticPool2Name);

        Assertions.assertNotNull(ep2);
        Assertions.assertEquals(ep2.edition(), ElasticPoolEdition.PREMIUM);
        Assertions.assertEquals(ep2.listDatabases().size(), 2);
        Assertions.assertNotNull(ep2.getDatabase(database1InEPName));
        Assertions.assertNotNull(ep2.getDatabase(database2InEPName));

        SqlElasticPool ep3 = sqlServer.elasticPools().get(elasticPool3Name);

        Assertions.assertNotNull(ep3);
        Assertions.assertEquals(ep3.edition(), ElasticPoolEdition.STANDARD);

        if (!deleteUsingUpdate) {
            sqlServer.databases().delete(database2Name);
            sqlServer.databases().delete(database1InEPName);
            sqlServer.databases().delete(database2InEPName);
            sqlServer.databases().delete(SQL_DATABASE_NAME);

            Assertions.assertEquals(ep1.listDatabases().size(), 0);
            Assertions.assertEquals(ep2.listDatabases().size(), 0);
            Assertions.assertEquals(ep3.listDatabases().size(), 0);

            sqlServer.elasticPools().delete(elasticPool1Name);
            sqlServer.elasticPools().delete(elasticPool2Name);
            sqlServer.elasticPools().delete(elasticPool3Name);

            firewalls = sqlServer.firewallRules().list();

            for (SqlFirewallRule firewallRule : firewalls) {
                firewallRule.delete();
            }
        } else {
            sqlServer
                .update()
                .withoutDatabase(database2Name)
                .withoutElasticPool(elasticPool1Name)
                .withoutElasticPool(elasticPool2Name)
                .withoutElasticPool(elasticPool3Name)
                .withoutDatabase(database1InEPName)
                .withoutDatabase(SQL_DATABASE_NAME)
                .withoutDatabase(database2InEPName)
                .withoutFirewallRule(SQL_FIREWALLRULE_NAME)
                .apply();

            Assertions.assertEquals(sqlServer.elasticPools().list().size(), 0);

            firewalls = sqlServer.firewallRules().list();
            Assertions.assertEquals(firewalls.size(), 2);
            for (SqlFirewallRule firewallRule : firewalls) {
                firewallRule.delete();
            }
        }

        Assertions.assertEquals(sqlServer.elasticPools().list().size(), 0);
        // Only master database is remaining in the SQLServer.
        Assertions.assertEquals(sqlServer.databases().list().size(), 1);
    }

    private void validateSqlFirewallRuleNotFound() {
        validateResourceNotFound(
            () ->
                sqlServerManager
                    .sqlServers()
                    .getByResourceGroup(rgName, sqlServerName)
                    .firewallRules()
                    .get(SQL_FIREWALLRULE_NAME));
    }

    private void validateSqlElasticPoolNotFound(SqlServer sqlServer, String elasticPoolName) {
        validateResourceNotFound(() -> sqlServer.elasticPools().get(elasticPoolName));
    }

    private void validateSqlDatabaseNotFound(String newDatabase) {
        validateResourceNotFound(
            () -> sqlServerManager.sqlServers().getByResourceGroup(rgName, sqlServerName).databases().get(newDatabase));
    }

    private void validateSqlServerNotFound(SqlServer sqlServer) {
        validateResourceNotFound(() -> sqlServerManager.sqlServers().getById(sqlServer.id()));
    }

    private void validateResourceNotFound(Supplier<Object> fetchResource) {
        try {
            Object result = fetchResource.get();
            Assertions.assertNull(result);
        } catch (ManagementException e) {
            Assertions.assertEquals(404, e.getResponse().getStatusCode());
        }
    }

    private SqlServer createSqlServer() {
        return createSqlServer(sqlServerName);
    }

    private SqlServer createSqlServer(String sqlServerName) {
        return sqlServerManager
            .sqlServers()
            .define(sqlServerName)
            .withRegion(Region.US_EAST)
            .withNewResourceGroup(rgName)
            .withAdministratorLogin("userName")
            .withAdministratorPassword("P@ssword~1")
            .create();
    }

    private static void validateListSqlFirewallRule(List<SqlFirewallRule> sqlFirewallRules) {
        boolean found = false;
        for (SqlFirewallRule firewallRule : sqlFirewallRules) {
            if (firewallRule.name().equals(SQL_FIREWALLRULE_NAME)) {
                found = true;
            }
        }
        Assertions.assertTrue(found);
    }

    private void validateSqlFirewallRule(SqlFirewallRule sqlFirewallRule, String firewallName) {
        Assertions.assertNotNull(sqlFirewallRule);
        Assertions.assertEquals(firewallName, sqlFirewallRule.name());
        Assertions.assertEquals(sqlServerName, sqlFirewallRule.sqlServerName());
        Assertions.assertEquals(START_IPADDRESS, sqlFirewallRule.startIpAddress());
        Assertions.assertEquals(END_IPADDRESS, sqlFirewallRule.endIpAddress());
        Assertions.assertEquals(rgName, sqlFirewallRule.resourceGroupName());
        Assertions.assertEquals(sqlServerName, sqlFirewallRule.sqlServerName());
        Assertions.assertEquals(Region.US_EAST, sqlFirewallRule.region());
    }

    private static void validateListSqlElasticPool(List<SqlElasticPool> sqlElasticPools) {
        boolean found = false;
        for (SqlElasticPool elasticPool : sqlElasticPools) {
            if (elasticPool.name().equals(SQL_ELASTIC_POOL_NAME)) {
                found = true;
            }
        }
        Assertions.assertTrue(found);
    }

    private void validateSqlElasticPool(SqlElasticPool sqlElasticPool) {
        validateSqlElasticPool(sqlElasticPool, SQL_ELASTIC_POOL_NAME);
    }

    private void validateSqlElasticPool(SqlElasticPool sqlElasticPool, String elasticPoolName) {
        Assertions.assertNotNull(sqlElasticPool);
        Assertions.assertEquals(rgName, sqlElasticPool.resourceGroupName());
        Assertions.assertEquals(elasticPoolName, sqlElasticPool.name());
        Assertions.assertEquals(sqlServerName, sqlElasticPool.sqlServerName());
        Assertions.assertEquals(ElasticPoolEdition.STANDARD, sqlElasticPool.edition());
        Assertions.assertNotNull(sqlElasticPool.creationDate());
        Assertions.assertNotEquals(0, sqlElasticPool.databaseDtuMax());
        Assertions.assertNotEquals(0, sqlElasticPool.dtu());
    }

    private static void validateListSqlDatabase(List<SqlDatabase> sqlDatabases) {
        boolean found = false;
        for (SqlDatabase database : sqlDatabases) {
            if (database.name().equals(SQL_DATABASE_NAME)) {
                found = true;
            }
        }
        Assertions.assertTrue(found);
    }

    private void validateSqlServer(SqlServer sqlServer) {
        Assertions.assertNotNull(sqlServer);
        Assertions.assertEquals(rgName, sqlServer.resourceGroupName());
        Assertions.assertNotNull(sqlServer.fullyQualifiedDomainName());
        //        Assertions.assertEquals(ServerVersion.ONE_TWO_FULL_STOP_ZERO, sqlServer.version());
        Assertions.assertEquals("userName", sqlServer.administratorLogin());
    }

    private void validateSqlDatabase(SqlDatabase sqlDatabase, String databaseName) {
        Assertions.assertNotNull(sqlDatabase);
        Assertions.assertEquals(sqlDatabase.name(), databaseName);
        Assertions.assertEquals(sqlServerName, sqlDatabase.sqlServerName());
        Assertions.assertEquals(sqlDatabase.collation(), COLLATION);
        Assertions.assertEquals(sqlDatabase.edition(), DatabaseEdition.STANDARD);
    }

    private void validateSqlDatabaseWithElasticPool(SqlDatabase sqlDatabase, String databaseName) {
        validateSqlDatabase(sqlDatabase, databaseName);
        Assertions.assertEquals(SQL_ELASTIC_POOL_NAME, sqlDatabase.elasticPoolName());
    }
}
