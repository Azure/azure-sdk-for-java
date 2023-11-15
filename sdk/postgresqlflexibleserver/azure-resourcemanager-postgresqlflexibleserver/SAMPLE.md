# Code snippets and samples


## Administrators

- [Create](#administrators_create)
- [Delete](#administrators_delete)
- [Get](#administrators_get)
- [ListByServer](#administrators_listbyserver)

## Backups

- [Get](#backups_get)
- [ListByServer](#backups_listbyserver)

## CheckNameAvailability

- [Execute](#checknameavailability_execute)

## CheckNameAvailabilityWithLocation

- [Execute](#checknameavailabilitywithlocation_execute)

## Configurations

- [Get](#configurations_get)
- [ListByServer](#configurations_listbyserver)
- [Put](#configurations_put)
- [Update](#configurations_update)

## Databases

- [Create](#databases_create)
- [Delete](#databases_delete)
- [Get](#databases_get)
- [ListByServer](#databases_listbyserver)

## FirewallRules

- [CreateOrUpdate](#firewallrules_createorupdate)
- [Delete](#firewallrules_delete)
- [Get](#firewallrules_get)
- [ListByServer](#firewallrules_listbyserver)

## FlexibleServer

- [StartLtrBackup](#flexibleserver_startltrbackup)
- [TriggerLtrPreBackup](#flexibleserver_triggerltrprebackup)

## GetPrivateDnsZoneSuffix

- [Execute](#getprivatednszonesuffix_execute)

## LocationBasedCapabilities

- [Execute](#locationbasedcapabilities_execute)

## LogFiles

- [ListByServer](#logfiles_listbyserver)

## LtrBackupOperations

- [Get](#ltrbackupoperations_get)
- [ListByServer](#ltrbackupoperations_listbyserver)

## Migrations

- [Create](#migrations_create)
- [Delete](#migrations_delete)
- [Get](#migrations_get)
- [ListByTargetServer](#migrations_listbytargetserver)
- [Update](#migrations_update)

## Operations

- [List](#operations_list)

## PrivateEndpointConnectionOperation

- [Delete](#privateendpointconnectionoperation_delete)
- [Update](#privateendpointconnectionoperation_update)

## PrivateEndpointConnections

- [Get](#privateendpointconnections_get)
- [ListByServer](#privateendpointconnections_listbyserver)

## PrivateLinkResources

- [Get](#privatelinkresources_get)
- [ListByServer](#privatelinkresources_listbyserver)

## QuotaUsages

- [List](#quotausages_list)

## Replicas

- [ListByServer](#replicas_listbyserver)

## ResourceProvider

- [CheckMigrationNameAvailability](#resourceprovider_checkmigrationnameavailability)

## ServerCapabilities

- [List](#servercapabilities_list)

## ServerThreatProtectionSettings

- [CreateOrUpdate](#serverthreatprotectionsettings_createorupdate)
- [Get](#serverthreatprotectionsettings_get)
- [ListByServer](#serverthreatprotectionsettings_listbyserver)

## Servers

- [Create](#servers_create)
- [Delete](#servers_delete)
- [GetByResourceGroup](#servers_getbyresourcegroup)
- [List](#servers_list)
- [ListByResourceGroup](#servers_listbyresourcegroup)
- [Restart](#servers_restart)
- [Start](#servers_start)
- [Stop](#servers_stop)
- [Update](#servers_update)

## VirtualEndpoints

- [Create](#virtualendpoints_create)
- [Delete](#virtualendpoints_delete)
- [Get](#virtualendpoints_get)
- [ListByServer](#virtualendpoints_listbyserver)
- [Update](#virtualendpoints_update)

## VirtualNetworkSubnetUsage

- [Execute](#virtualnetworksubnetusage_execute)
### Administrators_Create

```java
import com.azure.resourcemanager.postgresqlflexibleserver.models.PrincipalType;

/** Samples for Administrators Create. */
public final class AdministratorsCreateSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/AdministratorAdd.json
     */
    /**
     * Sample code: Adds an Active DIrectory Administrator for the server.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void addsAnActiveDIrectoryAdministratorForTheServer(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager
            .administrators()
            .define("oooooooo-oooo-oooo-oooo-oooooooooooo")
            .withExistingFlexibleServer("testrg", "testserver")
            .withPrincipalType(PrincipalType.USER)
            .withPrincipalName("testuser1@microsoft.com")
            .withTenantId("tttttttt-tttt-tttt-tttt-tttttttttttt")
            .create();
    }
}
```

### Administrators_Delete

```java
/** Samples for Administrators Delete. */
public final class AdministratorsDeleteSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/AdministratorDelete.json
     */
    /**
     * Sample code: AdministratorDelete.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void administratorDelete(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager
            .administrators()
            .delete("testrg", "testserver", "oooooooo-oooo-oooo-oooo-oooooooooooo", com.azure.core.util.Context.NONE);
    }
}
```

### Administrators_Get

```java
/** Samples for Administrators Get. */
public final class AdministratorsGetSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/AdministratorGet.json
     */
    /**
     * Sample code: ServerGet.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void serverGet(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager
            .administrators()
            .getWithResponse(
                "testrg", "pgtestsvc1", "oooooooo-oooo-oooo-oooo-oooooooooooo", com.azure.core.util.Context.NONE);
    }
}
```

### Administrators_ListByServer

```java
/** Samples for Administrators ListByServer. */
public final class AdministratorsListByServerSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/AdministratorsListByServer.json
     */
    /**
     * Sample code: AdministratorsListByServer.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void administratorsListByServer(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.administrators().listByServer("testrg", "pgtestsvc1", com.azure.core.util.Context.NONE);
    }
}
```

### Backups_Get

```java
/** Samples for Backups Get. */
public final class BackupsGetSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/BackupGet.json
     */
    /**
     * Sample code: Get a backup for a server.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void getABackupForAServer(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager
            .backups()
            .getWithResponse(
                "TestGroup", "postgresqltestserver", "daily_20210615T160516", com.azure.core.util.Context.NONE);
    }
}
```

### Backups_ListByServer

```java
/** Samples for Backups ListByServer. */
public final class BackupsListByServerSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/BackupListByServer.json
     */
    /**
     * Sample code: List backups for a server.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void listBackupsForAServer(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.backups().listByServer("TestGroup", "postgresqltestserver", com.azure.core.util.Context.NONE);
    }
}
```

### CheckNameAvailability_Execute

```java
import com.azure.resourcemanager.postgresqlflexibleserver.models.CheckNameAvailabilityRequest;

/** Samples for CheckNameAvailability Execute. */
public final class CheckNameAvailabilityExecuteSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/CheckNameAvailability.json
     */
    /**
     * Sample code: NameAvailability.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void nameAvailability(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager
            .checkNameAvailabilities()
            .executeWithResponse(
                new CheckNameAvailabilityRequest()
                    .withName("name1")
                    .withType("Microsoft.DBforPostgreSQL/flexibleServers"),
                com.azure.core.util.Context.NONE);
    }
}
```

### CheckNameAvailabilityWithLocation_Execute

```java
import com.azure.resourcemanager.postgresqlflexibleserver.models.CheckNameAvailabilityRequest;

/** Samples for CheckNameAvailabilityWithLocation Execute. */
public final class CheckNameAvailabilityWithLocationExecuteSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/CheckNameAvailabilityLocationBased.json
     */
    /**
     * Sample code: NameAvailability.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void nameAvailability(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager
            .checkNameAvailabilityWithLocations()
            .executeWithResponse(
                "westus",
                new CheckNameAvailabilityRequest()
                    .withName("name1")
                    .withType("Microsoft.DBforPostgreSQL/flexibleServers"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Configurations_Get

```java
/** Samples for Configurations Get. */
public final class ConfigurationsGetSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/ConfigurationGet.json
     */
    /**
     * Sample code: ConfigurationGet.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void configurationGet(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager
            .configurations()
            .getWithResponse("testrg", "testserver", "array_nulls", com.azure.core.util.Context.NONE);
    }
}
```

### Configurations_ListByServer

```java
/** Samples for Configurations ListByServer. */
public final class ConfigurationsListByServerSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/ConfigurationListByServer.json
     */
    /**
     * Sample code: ConfigurationList.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void configurationList(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.configurations().listByServer("testrg", "testserver", com.azure.core.util.Context.NONE);
    }
}
```

### Configurations_Put

```java
/** Samples for Configurations Put. */
public final class ConfigurationsPutSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/ConfigurationUpdate.json
     */
    /**
     * Sample code: Update a user configuration.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void updateAUserConfiguration(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager
            .configurations()
            .define("event_scheduler")
            .withExistingFlexibleServer("testrg", "testserver")
            .withValue("on")
            .withSource("user-override")
            .create();
    }
}
```

### Configurations_Update

```java
import com.azure.resourcemanager.postgresqlflexibleserver.models.Configuration;

/** Samples for Configurations Update. */
public final class ConfigurationsUpdateSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/ConfigurationUpdate.json
     */
    /**
     * Sample code: Update a user configuration.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void updateAUserConfiguration(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        Configuration resource =
            manager
                .configurations()
                .getWithResponse("testrg", "testserver", "event_scheduler", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withValue("on").withSource("user-override").apply();
    }
}
```

### Databases_Create

```java
/** Samples for Databases Create. */
public final class DatabasesCreateSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/DatabaseCreate.json
     */
    /**
     * Sample code: Create a database.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void createADatabase(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager
            .databases()
            .define("db1")
            .withExistingFlexibleServer("TestGroup", "testserver")
            .withCharset("utf8")
            .withCollation("en_US.utf8")
            .create();
    }
}
```

### Databases_Delete

```java
/** Samples for Databases Delete. */
public final class DatabasesDeleteSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/DatabaseDelete.json
     */
    /**
     * Sample code: Delete a database.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void deleteADatabase(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.databases().delete("TestGroup", "testserver", "db1", com.azure.core.util.Context.NONE);
    }
}
```

### Databases_Get

```java
/** Samples for Databases Get. */
public final class DatabasesGetSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/DatabaseGet.json
     */
    /**
     * Sample code: Get a database.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void getADatabase(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.databases().getWithResponse("TestGroup", "testserver", "db1", com.azure.core.util.Context.NONE);
    }
}
```

### Databases_ListByServer

```java
/** Samples for Databases ListByServer. */
public final class DatabasesListByServerSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/DatabasesListByServer.json
     */
    /**
     * Sample code: List databases in a server.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void listDatabasesInAServer(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.databases().listByServer("TestGroup", "testserver", com.azure.core.util.Context.NONE);
    }
}
```

### FirewallRules_CreateOrUpdate

```java
/** Samples for FirewallRules CreateOrUpdate. */
public final class FirewallRulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/FirewallRuleCreate.json
     */
    /**
     * Sample code: FirewallRuleCreate.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void firewallRuleCreate(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager
            .firewallRules()
            .define("rule1")
            .withExistingFlexibleServer("testrg", "testserver")
            .withStartIpAddress("0.0.0.0")
            .withEndIpAddress("255.255.255.255")
            .create();
    }
}
```

### FirewallRules_Delete

```java
/** Samples for FirewallRules Delete. */
public final class FirewallRulesDeleteSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/FirewallRuleDelete.json
     */
    /**
     * Sample code: FirewallRuleDelete.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void firewallRuleDelete(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.firewallRules().delete("testrg", "testserver", "rule1", com.azure.core.util.Context.NONE);
    }
}
```

### FirewallRules_Get

```java
/** Samples for FirewallRules Get. */
public final class FirewallRulesGetSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/FirewallRuleGet.json
     */
    /**
     * Sample code: FirewallRuleList.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void firewallRuleList(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.firewallRules().getWithResponse("testrg", "testserver", "rule1", com.azure.core.util.Context.NONE);
    }
}
```

### FirewallRules_ListByServer

```java
/** Samples for FirewallRules ListByServer. */
public final class FirewallRulesListByServerSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/FirewallRuleListByServer.json
     */
    /**
     * Sample code: FirewallRuleList.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void firewallRuleList(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.firewallRules().listByServer("testrg", "testserver", com.azure.core.util.Context.NONE);
    }
}
```

### FlexibleServer_StartLtrBackup

```java
import com.azure.resourcemanager.postgresqlflexibleserver.models.BackupSettings;
import com.azure.resourcemanager.postgresqlflexibleserver.models.BackupStoreDetails;
import com.azure.resourcemanager.postgresqlflexibleserver.models.LtrBackupRequest;
import java.util.Arrays;

/** Samples for FlexibleServer StartLtrBackup. */
public final class FlexibleServerStartLtrBackupSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/LongTermRetentionBackup.json
     */
    /**
     * Sample code: Sample_ExecuteBackup.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void sampleExecuteBackup(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager
            .flexibleServers()
            .startLtrBackup(
                "rgLongTermRetention",
                "pgsqlltrtestserver",
                new LtrBackupRequest()
                    .withBackupSettings(new BackupSettings().withBackupName("backup1"))
                    .withTargetDetails(new BackupStoreDetails().withSasUriList(Arrays.asList("sasuri"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### FlexibleServer_TriggerLtrPreBackup

```java
import com.azure.resourcemanager.postgresqlflexibleserver.models.BackupSettings;
import com.azure.resourcemanager.postgresqlflexibleserver.models.LtrPreBackupRequest;

/** Samples for FlexibleServer TriggerLtrPreBackup. */
public final class FlexibleServerTriggerLtrPreBackupSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/LongTermRetentionPreBackup.json
     */
    /**
     * Sample code: Sample_Prebackup.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void samplePrebackup(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager
            .flexibleServers()
            .triggerLtrPreBackupWithResponse(
                "rgLongTermRetention",
                "pgsqlltrtestserver",
                new LtrPreBackupRequest().withBackupSettings(new BackupSettings().withBackupName("backup1")),
                com.azure.core.util.Context.NONE);
    }
}
```

### GetPrivateDnsZoneSuffix_Execute

```java
/** Samples for GetPrivateDnsZoneSuffix Execute. */
public final class GetPrivateDnsZoneSuffixExecuteSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/GetPrivateDnsZoneSuffix.json
     */
    /**
     * Sample code: GetPrivateDnsZoneSuffix.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void getPrivateDnsZoneSuffix(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.getPrivateDnsZoneSuffixes().executeWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

### LocationBasedCapabilities_Execute

```java
/** Samples for LocationBasedCapabilities Execute. */
public final class LocationBasedCapabilitiesExecuteSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/CapabilitiesByLocation.json
     */
    /**
     * Sample code: CapabilitiesList.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void capabilitiesList(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.locationBasedCapabilities().execute("westus", com.azure.core.util.Context.NONE);
    }
}
```

### LogFiles_ListByServer

```java
/** Samples for LogFiles ListByServer. */
public final class LogFilesListByServerSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/LogFilesListByServer.json
     */
    /**
     * Sample code: List all server log files for a server.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void listAllServerLogFilesForAServer(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.logFiles().listByServer("testrg", "postgresqltestsvc1", com.azure.core.util.Context.NONE);
    }
}
```

### LtrBackupOperations_Get

```java
/** Samples for LtrBackupOperations Get. */
public final class LtrBackupOperationsGetSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/LongTermRetentionOperationGet.json
     */
    /**
     * Sample code: Sample.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void sample(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager
            .ltrBackupOperations()
            .getWithResponse("rgLongTermRetention", "pgsqlltrtestserver", "backup1", com.azure.core.util.Context.NONE);
    }
}
```

### LtrBackupOperations_ListByServer

```java
/** Samples for LtrBackupOperations ListByServer. */
public final class LtrBackupOperationsListByServerSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/LongTermRetentionOperationListByServer.json
     */
    /**
     * Sample code: Sample List of Long Tern Retention Operations by Flexible Server.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void sampleListOfLongTernRetentionOperationsByFlexibleServer(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager
            .ltrBackupOperations()
            .listByServer("rgLongTermRetention", "pgsqlltrtestserver", com.azure.core.util.Context.NONE);
    }
}
```

### Migrations_Create

```java
import com.azure.resourcemanager.postgresqlflexibleserver.models.AdminCredentials;
import com.azure.resourcemanager.postgresqlflexibleserver.models.MigrationMode;
import com.azure.resourcemanager.postgresqlflexibleserver.models.MigrationOption;
import com.azure.resourcemanager.postgresqlflexibleserver.models.MigrationSecretParameters;
import com.azure.resourcemanager.postgresqlflexibleserver.models.OverwriteDbsInTargetEnum;
import com.azure.resourcemanager.postgresqlflexibleserver.models.SourceType;
import com.azure.resourcemanager.postgresqlflexibleserver.models.SslMode;
import java.util.Arrays;

/** Samples for Migrations Create. */
public final class MigrationsCreateSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/Migrations_Create_Validate_Only.json
     */
    /**
     * Sample code: Create Pre-migration Validation.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void createPreMigrationValidation(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager
            .migrations()
            .define("testmigration")
            .withRegion("westus")
            .withExistingFlexibleServer("ffffffff-ffff-ffff-ffff-ffffffffffff", "testrg", "testtarget")
            .withMigrationMode(MigrationMode.OFFLINE)
            .withMigrationOption(MigrationOption.VALIDATE)
            .withSourceDbServerResourceId(
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/testrg/providers/Microsoft.DBForPostgreSql/servers/testsource")
            .withSecretParameters(
                new MigrationSecretParameters()
                    .withAdminCredentials(
                        new AdminCredentials()
                            .withSourceServerPassword("fakeTokenPlaceholder")
                            .withTargetServerPassword("fakeTokenPlaceholder")))
            .withDbsToMigrate(Arrays.asList("db1", "db2", "db3", "db4"))
            .withOverwriteDbsInTarget(OverwriteDbsInTargetEnum.TRUE)
            .create();
    }

    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/Migrations_Create_With_Other_Users.json
     */
    /**
     * Sample code: Migrations Create by passing user names.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void migrationsCreateByPassingUserNames(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager
            .migrations()
            .define("testmigration")
            .withRegion("westus")
            .withExistingFlexibleServer("ffffffff-ffff-ffff-ffff-ffffffffffff", "testrg", "testtarget")
            .withMigrationMode(MigrationMode.OFFLINE)
            .withSourceDbServerResourceId(
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/testrg/providers/Microsoft.DBForPostgreSql/servers/testsource")
            .withSecretParameters(
                new MigrationSecretParameters()
                    .withAdminCredentials(
                        new AdminCredentials()
                            .withSourceServerPassword("fakeTokenPlaceholder")
                            .withTargetServerPassword("fakeTokenPlaceholder"))
                    .withSourceServerUsername("newadmin@testsource")
                    .withTargetServerUsername("targetadmin"))
            .withDbsToMigrate(Arrays.asList("db1", "db2", "db3", "db4"))
            .create();
    }

    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/Migrations_Create.json
     */
    /**
     * Sample code: Migrations_Create.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void migrationsCreate(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager
            .migrations()
            .define("testmigration")
            .withRegion("westus")
            .withExistingFlexibleServer("ffffffff-ffff-ffff-ffff-ffffffffffff", "testrg", "testtarget")
            .withMigrationMode(MigrationMode.OFFLINE)
            .withSourceDbServerResourceId(
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/testrg/providers/Microsoft.DBForPostgreSql/servers/testsource")
            .withSecretParameters(
                new MigrationSecretParameters()
                    .withAdminCredentials(
                        new AdminCredentials()
                            .withSourceServerPassword("fakeTokenPlaceholder")
                            .withTargetServerPassword("fakeTokenPlaceholder")))
            .withDbsToMigrate(Arrays.asList("db1", "db2", "db3", "db4"))
            .withOverwriteDbsInTarget(OverwriteDbsInTargetEnum.TRUE)
            .create();
    }

    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/Migrations_Create_Other_SourceTypes_Validate_Migrate.json
     */
    /**
     * Sample code: Create Migration with other source types for Validate and Migrate.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void createMigrationWithOtherSourceTypesForValidateAndMigrate(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager
            .migrations()
            .define("testmigration")
            .withRegion("westus")
            .withExistingFlexibleServer("ffffffff-ffff-ffff-ffff-ffffffffffff", "testrg", "testtarget")
            .withMigrationMode(MigrationMode.OFFLINE)
            .withMigrationOption(MigrationOption.VALIDATE_AND_MIGRATE)
            .withSourceType(SourceType.ON_PREMISES)
            .withSslMode(SslMode.PREFER)
            .withSourceDbServerResourceId("testsource:5432@pguser")
            .withSecretParameters(
                new MigrationSecretParameters()
                    .withAdminCredentials(
                        new AdminCredentials()
                            .withSourceServerPassword("fakeTokenPlaceholder")
                            .withTargetServerPassword("fakeTokenPlaceholder")))
            .withDbsToMigrate(Arrays.asList("db1", "db2", "db3", "db4"))
            .withOverwriteDbsInTarget(OverwriteDbsInTargetEnum.TRUE)
            .create();
    }
}
```

### Migrations_Delete

```java
/** Samples for Migrations Delete. */
public final class MigrationsDeleteSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/Migrations_Delete.json
     */
    /**
     * Sample code: Migrations_Delete.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void migrationsDelete(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager
            .migrations()
            .deleteWithResponse(
                "ffffffff-ffff-ffff-ffff-ffffffffffff",
                "testrg",
                "testtarget",
                "testmigration",
                com.azure.core.util.Context.NONE);
    }
}
```

### Migrations_Get

```java
/** Samples for Migrations Get. */
public final class MigrationsGetSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/Migrations_GetMigrationWithSuccessfulValidationOnly.json
     */
    /**
     * Sample code: Migrations_GetMigrationWithSuccessfulValidationOnly.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void migrationsGetMigrationWithSuccessfulValidationOnly(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager
            .migrations()
            .getWithResponse(
                "ffffffff-ffff-ffff-ffff-ffffffffffff",
                "testrg",
                "testtarget",
                "testmigrationwithsuccessfulvalidationonly",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/Migrations_Get.json
     */
    /**
     * Sample code: Migrations_Get.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void migrationsGet(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager
            .migrations()
            .getWithResponse(
                "ffffffff-ffff-ffff-ffff-ffffffffffff",
                "testrg",
                "testtarget",
                "testmigration",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/Migrations_GetMigrationWithSuccessfulValidationButMigrationFailure.json
     */
    /**
     * Sample code: Migrations_GetMigrationWithSuccessfulValidationButMigrationFailure.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void migrationsGetMigrationWithSuccessfulValidationButMigrationFailure(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager
            .migrations()
            .getWithResponse(
                "ffffffff-ffff-ffff-ffff-ffffffffffff",
                "testrg",
                "testtarget",
                "testmigrationwithsuccessfulvalidationbutmigrationfailure",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/Migrations_GetMigrationWithSuccessfulValidationAndMigration.json
     */
    /**
     * Sample code: Migrations_GetMigrationWithSuccessfulValidationAndMigration.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void migrationsGetMigrationWithSuccessfulValidationAndMigration(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager
            .migrations()
            .getWithResponse(
                "ffffffff-ffff-ffff-ffff-ffffffffffff",
                "testrg",
                "testtarget",
                "testmigrationwithsuccessfulvalidationandmigration",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/Migrations_GetMigrationWithValidationFailures.json
     */
    /**
     * Sample code: Migrations_GetMigrationWithValidationFailures.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void migrationsGetMigrationWithValidationFailures(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager
            .migrations()
            .getWithResponse(
                "ffffffff-ffff-ffff-ffff-ffffffffffff",
                "testrg",
                "testtarget",
                "testmigrationwithvalidationfailure",
                com.azure.core.util.Context.NONE);
    }
}
```

### Migrations_ListByTargetServer

```java
import com.azure.resourcemanager.postgresqlflexibleserver.models.MigrationListFilter;

/** Samples for Migrations ListByTargetServer. */
public final class MigrationsListByTargetServerSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/Migrations_ListByTargetServer.json
     */
    /**
     * Sample code: Migrations_ListByTargetServer.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void migrationsListByTargetServer(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager
            .migrations()
            .listByTargetServer(
                "ffffffff-ffff-ffff-ffff-ffffffffffff",
                "testrg",
                "testtarget",
                MigrationListFilter.ALL,
                com.azure.core.util.Context.NONE);
    }
}
```

### Migrations_Update

```java
import com.azure.resourcemanager.postgresqlflexibleserver.models.CancelEnum;
import com.azure.resourcemanager.postgresqlflexibleserver.models.LogicalReplicationOnSourceDbEnum;
import com.azure.resourcemanager.postgresqlflexibleserver.models.MigrationResource;

/** Samples for Migrations Update. */
public final class MigrationsUpdateSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/Migrations_Cancel.json
     */
    /**
     * Sample code: Cancel migration.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void cancelMigration(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        MigrationResource resource =
            manager
                .migrations()
                .getWithResponse(
                    "ffffffff-ffff-ffff-ffff-ffffffffffff",
                    "testrg",
                    "testtarget",
                    "testmigration",
                    com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withCancel(CancelEnum.TRUE).apply();
    }

    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/Migrations_Update.json
     */
    /**
     * Sample code: Migrations_Update.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void migrationsUpdate(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        MigrationResource resource =
            manager
                .migrations()
                .getWithResponse(
                    "ffffffff-ffff-ffff-ffff-ffffffffffff",
                    "testrg",
                    "testtarget",
                    "testmigration",
                    com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withSetupLogicalReplicationOnSourceDbIfNeeded(LogicalReplicationOnSourceDbEnum.TRUE).apply();
    }
}
```

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/OperationList.json
     */
    /**
     * Sample code: OperationList.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void operationList(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.operations().listWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnectionOperation_Delete

```java
/** Samples for PrivateEndpointConnectionOperation Delete. */
public final class PrivateEndpointConnectionOperationDeleteSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/PrivateEndpointConnectionDelete.json
     */
    /**
     * Sample code: Deletes a private endpoint connection with a given name.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void deletesAPrivateEndpointConnectionWithAGivenName(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager
            .privateEndpointConnectionOperations()
            .delete(
                "Default",
                "test-svr",
                "private-endpoint-connection-name.1fa229cd-bf3f-47f0-8c49-afb36723997e",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnectionOperation_Update

```java
import com.azure.resourcemanager.postgresqlflexibleserver.fluent.models.PrivateEndpointConnectionInner;
import com.azure.resourcemanager.postgresqlflexibleserver.models.PrivateEndpointServiceConnectionStatus;
import com.azure.resourcemanager.postgresqlflexibleserver.models.PrivateLinkServiceConnectionState;

/** Samples for PrivateEndpointConnectionOperation Update. */
public final class PrivateEndpointConnectionOperationUpdateSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/PrivateEndpointConnectionUpdate.json
     */
    /**
     * Sample code: Approve or reject a private endpoint connection with a given name.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void approveOrRejectAPrivateEndpointConnectionWithAGivenName(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager
            .privateEndpointConnectionOperations()
            .update(
                "Default",
                "test-svr",
                "private-endpoint-connection-name.1fa229cd-bf3f-47f0-8c49-afb36723997e",
                new PrivateEndpointConnectionInner()
                    .withPrivateLinkServiceConnectionState(
                        new PrivateLinkServiceConnectionState()
                            .withStatus(PrivateEndpointServiceConnectionStatus.APPROVED)
                            .withDescription("Approved by johndoe@contoso.com")),
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_Get

```java
/** Samples for PrivateEndpointConnections Get. */
public final class PrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/PrivateEndpointConnectionGet.json
     */
    /**
     * Sample code: Gets private endpoint connection.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void getsPrivateEndpointConnection(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager
            .privateEndpointConnections()
            .getWithResponse(
                "Default",
                "test-svr",
                "private-endpoint-connection-name.1fa229cd-bf3f-47f0-8c49-afb36723997e",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_ListByServer

```java
/** Samples for PrivateEndpointConnections ListByServer. */
public final class PrivateEndpointConnectionsListByServerSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/PrivateEndpointConnectionList.json
     */
    /**
     * Sample code: Gets list of private endpoint connections on a server.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void getsListOfPrivateEndpointConnectionsOnAServer(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.privateEndpointConnections().listByServer("Default", "test-svr", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkResources_Get

```java
/** Samples for PrivateLinkResources Get. */
public final class PrivateLinkResourcesGetSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/PrivateLinkResourcesGet.json
     */
    /**
     * Sample code: Gets a private link resource for PostgreSQL.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void getsAPrivateLinkResourceForPostgreSQL(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.privateLinkResources().getWithResponse("Default", "test-svr", "plr", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkResources_ListByServer

```java
/** Samples for PrivateLinkResources ListByServer. */
public final class PrivateLinkResourcesListByServerSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/PrivateLinkResourcesList.json
     */
    /**
     * Sample code: Gets private link resources for PostgreSQL.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void getsPrivateLinkResourcesForPostgreSQL(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.privateLinkResources().listByServer("Default", "test-svr", com.azure.core.util.Context.NONE);
    }
}
```

### QuotaUsages_List

```java
/** Samples for QuotaUsages List. */
public final class QuotaUsagesListSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/QuotaUsagesForFlexibleServers.json
     */
    /**
     * Sample code: List of quota usages for flexible servers.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void listOfQuotaUsagesForFlexibleServers(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.quotaUsages().list("westus", com.azure.core.util.Context.NONE);
    }
}
```

### Replicas_ListByServer

```java
/** Samples for Replicas ListByServer. */
public final class ReplicasListByServerSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/ReplicasListByServer.json
     */
    /**
     * Sample code: ReplicasListByServer.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void replicasListByServer(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.replicas().listByServer("testrg", "sourcepgservername", com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_CheckMigrationNameAvailability

```java
import com.azure.resourcemanager.postgresqlflexibleserver.fluent.models.MigrationNameAvailabilityResourceInner;

/** Samples for ResourceProvider CheckMigrationNameAvailability. */
public final class ResourceProviderCheckMigrationNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/CheckMigrationNameAvailability.json
     */
    /**
     * Sample code: CheckMigrationNameAvailability.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void checkMigrationNameAvailability(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager
            .resourceProviders()
            .checkMigrationNameAvailabilityWithResponse(
                "ffffffff-ffff-ffff-ffff-ffffffffffff",
                "testrg",
                "testtarget",
                new MigrationNameAvailabilityResourceInner()
                    .withName("name1")
                    .withType("Microsoft.DBforPostgreSQL/flexibleServers/migrations"),
                com.azure.core.util.Context.NONE);
    }
}
```

### ServerCapabilities_List

```java
/** Samples for ServerCapabilities List. */
public final class ServerCapabilitiesListSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/ServerCapabilities.json
     */
    /**
     * Sample code: ServerCapabilitiesList.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void serverCapabilitiesList(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.serverCapabilities().list("testrg", "pgtestsvc4", com.azure.core.util.Context.NONE);
    }
}
```

### ServerThreatProtectionSettings_CreateOrUpdate

```java
import com.azure.resourcemanager.postgresqlflexibleserver.models.ServerThreatProtectionSettingsModel;
import com.azure.resourcemanager.postgresqlflexibleserver.models.ThreatProtectionName;
import com.azure.resourcemanager.postgresqlflexibleserver.models.ThreatProtectionState;

/** Samples for ServerThreatProtectionSettings CreateOrUpdate. */
public final class ServerThreatProtectionSettingsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/ServerThreatProtectionSettingsCreate.json
     */
    /**
     * Sample code: Update a server's Threat Protection settings.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void updateAServerSThreatProtectionSettings(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        ServerThreatProtectionSettingsModel resource =
            manager
                .serverThreatProtectionSettings()
                .getWithResponse(
                    "threatprotection-4799",
                    "threatprotection-6440",
                    ThreatProtectionName.DEFAULT,
                    com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withState(ThreatProtectionState.ENABLED).apply();
    }
}
```

### ServerThreatProtectionSettings_Get

```java
import com.azure.resourcemanager.postgresqlflexibleserver.models.ThreatProtectionName;

/** Samples for ServerThreatProtectionSettings Get. */
public final class ServerThreatProtectionSettingsGetSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/ServerThreatProtectionSettingsGet.json
     */
    /**
     * Sample code: Get a server's Threat Protection settings.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void getAServerSThreatProtectionSettings(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager
            .serverThreatProtectionSettings()
            .getWithResponse(
                "threatprotection-6852",
                "threatprotection-2080",
                ThreatProtectionName.DEFAULT,
                com.azure.core.util.Context.NONE);
    }
}
```

### ServerThreatProtectionSettings_ListByServer

```java
/** Samples for ServerThreatProtectionSettings ListByServer. */
public final class ServerThreatProtectionSettingsListByServerSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/ServerThreatProtectionSettingsListByServer.json
     */
    /**
     * Sample code: Get a server's Advanced Threat Protection settings.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void getAServerSAdvancedThreatProtectionSettings(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager
            .serverThreatProtectionSettings()
            .listByServer("threatprotection-6852", "threatprotection-2080", com.azure.core.util.Context.NONE);
    }
}
```

### Servers_Create

```java
import com.azure.resourcemanager.postgresqlflexibleserver.models.ActiveDirectoryAuthEnum;
import com.azure.resourcemanager.postgresqlflexibleserver.models.ArmServerKeyType;
import com.azure.resourcemanager.postgresqlflexibleserver.models.AuthConfig;
import com.azure.resourcemanager.postgresqlflexibleserver.models.AzureManagedDiskPerformanceTiers;
import com.azure.resourcemanager.postgresqlflexibleserver.models.Backup;
import com.azure.resourcemanager.postgresqlflexibleserver.models.CreateMode;
import com.azure.resourcemanager.postgresqlflexibleserver.models.DataEncryption;
import com.azure.resourcemanager.postgresqlflexibleserver.models.GeoRedundantBackupEnum;
import com.azure.resourcemanager.postgresqlflexibleserver.models.HighAvailability;
import com.azure.resourcemanager.postgresqlflexibleserver.models.HighAvailabilityMode;
import com.azure.resourcemanager.postgresqlflexibleserver.models.IdentityType;
import com.azure.resourcemanager.postgresqlflexibleserver.models.Network;
import com.azure.resourcemanager.postgresqlflexibleserver.models.PasswordAuthEnum;
import com.azure.resourcemanager.postgresqlflexibleserver.models.ServerVersion;
import com.azure.resourcemanager.postgresqlflexibleserver.models.Sku;
import com.azure.resourcemanager.postgresqlflexibleserver.models.SkuTier;
import com.azure.resourcemanager.postgresqlflexibleserver.models.Storage;
import com.azure.resourcemanager.postgresqlflexibleserver.models.StorageAutoGrow;
import com.azure.resourcemanager.postgresqlflexibleserver.models.UserAssignedIdentity;
import com.azure.resourcemanager.postgresqlflexibleserver.models.UserIdentity;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/** Samples for Servers Create. */
public final class ServersCreateSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/ServerCreateWithDataEncryptionEnabled.json
     */
    /**
     * Sample code: ServerCreateWithDataEncryptionEnabled.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void serverCreateWithDataEncryptionEnabled(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager
            .servers()
            .define("pgtestsvc4")
            .withRegion("westus")
            .withExistingResourceGroup("testrg")
            .withTags(mapOf("ElasticServer", "1"))
            .withSku(new Sku().withName("Standard_D4s_v3").withTier(SkuTier.GENERAL_PURPOSE))
            .withIdentity(
                new UserAssignedIdentity()
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/testresourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/test-usermanagedidentity",
                            new UserIdentity()))
                    .withType(IdentityType.USER_ASSIGNED))
            .withAdministratorLogin("cloudsa")
            .withAdministratorLoginPassword("password")
            .withVersion(ServerVersion.ONE_TWO)
            .withStorage(new Storage().withStorageSizeGB(512).withAutoGrow(StorageAutoGrow.DISABLED))
            .withDataEncryption(
                new DataEncryption()
                    .withPrimaryKeyUri("fakeTokenPlaceholder")
                    .withPrimaryUserAssignedIdentityId(
                        "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/testresourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/test-usermanagedidentity")
                    .withGeoBackupKeyUri("fakeTokenPlaceholder")
                    .withGeoBackupUserAssignedIdentityId("")
                    .withType(ArmServerKeyType.AZURE_KEY_VAULT))
            .withBackup(new Backup().withBackupRetentionDays(7).withGeoRedundantBackup(GeoRedundantBackupEnum.DISABLED))
            .withNetwork(
                new Network()
                    .withDelegatedSubnetResourceId(
                        "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/testrg/providers/Microsoft.Network/virtualNetworks/test-vnet/subnets/test-vnet-subnet")
                    .withPrivateDnsZoneArmResourceId(
                        "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourcegroups/testrg/providers/Microsoft.Network/privateDnsZones/test-private-dns-zone.postgres.database.azure.com"))
            .withHighAvailability(new HighAvailability().withMode(HighAvailabilityMode.ZONE_REDUNDANT))
            .withAvailabilityZone("1")
            .withCreateMode(CreateMode.CREATE)
            .create();
    }

    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/ServerCreateReviveDropped.json
     */
    /**
     * Sample code: ServerCreateReviveDropped.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void serverCreateReviveDropped(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager
            .servers()
            .define("pgtestsvc5-rev")
            .withRegion("westus")
            .withExistingResourceGroup("testrg")
            .withSourceServerResourceId(
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/testrg/providers/Microsoft.DBforPostgreSQL/flexibleServers/pgtestsvc5")
            .withPointInTimeUtc(OffsetDateTime.parse("2023-04-27T00:04:59.4078005+00:00"))
            .withCreateMode(CreateMode.REVIVE_DROPPED)
            .create();
    }

    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/ServerCreateGeoRestoreWithDataEncryptionEnabled.json
     */
    /**
     * Sample code: Create a database as a geo-restore in geo-paired location.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void createADatabaseAsAGeoRestoreInGeoPairedLocation(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager
            .servers()
            .define("pgtestsvc5geo")
            .withRegion("eastus")
            .withExistingResourceGroup("testrg")
            .withIdentity(
                new UserAssignedIdentity()
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/testresourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/test-geo-usermanagedidentity",
                            new UserIdentity(),
                            "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/testresourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/test-usermanagedidentity",
                            new UserIdentity()))
                    .withType(IdentityType.USER_ASSIGNED))
            .withDataEncryption(
                new DataEncryption()
                    .withPrimaryKeyUri("fakeTokenPlaceholder")
                    .withPrimaryUserAssignedIdentityId(
                        "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/testresourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/test-usermanagedidentity")
                    .withGeoBackupKeyUri("fakeTokenPlaceholder")
                    .withGeoBackupUserAssignedIdentityId(
                        "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/testresourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/test-geo-usermanagedidentity")
                    .withType(ArmServerKeyType.AZURE_KEY_VAULT))
            .withSourceServerResourceId(
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/testrg/providers/Microsoft.DBforPostgreSQL/flexibleServers/sourcepgservername")
            .withPointInTimeUtc(OffsetDateTime.parse("2021-06-27T00:04:59.4078005+00:00"))
            .withCreateMode(CreateMode.GEO_RESTORE)
            .create();
    }

    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/ServerCreate.json
     */
    /**
     * Sample code: Create a new server.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void createANewServer(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager
            .servers()
            .define("pgtestsvc4")
            .withRegion("westus")
            .withExistingResourceGroup("testrg")
            .withTags(mapOf("ElasticServer", "1"))
            .withSku(new Sku().withName("Standard_D4s_v3").withTier(SkuTier.GENERAL_PURPOSE))
            .withAdministratorLogin("cloudsa")
            .withAdministratorLoginPassword("password")
            .withVersion(ServerVersion.ONE_TWO)
            .withStorage(
                new Storage()
                    .withStorageSizeGB(512)
                    .withAutoGrow(StorageAutoGrow.DISABLED)
                    .withTier(AzureManagedDiskPerformanceTiers.P20))
            .withBackup(new Backup().withBackupRetentionDays(7).withGeoRedundantBackup(GeoRedundantBackupEnum.DISABLED))
            .withNetwork(
                new Network()
                    .withDelegatedSubnetResourceId(
                        "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/testrg/providers/Microsoft.Network/virtualNetworks/test-vnet/subnets/test-vnet-subnet")
                    .withPrivateDnsZoneArmResourceId(
                        "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourcegroups/testrg/providers/Microsoft.Network/privateDnsZones/test-private-dns-zone.postgres.database.azure.com"))
            .withHighAvailability(new HighAvailability().withMode(HighAvailabilityMode.ZONE_REDUNDANT))
            .withAvailabilityZone("1")
            .withCreateMode(CreateMode.CREATE)
            .create();
    }

    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/ServerCreateWithAadAuthEnabled.json
     */
    /**
     * Sample code: Create a new server with active directory authentication enabled.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void createANewServerWithActiveDirectoryAuthenticationEnabled(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager
            .servers()
            .define("pgtestsvc4")
            .withRegion("westus")
            .withExistingResourceGroup("testrg")
            .withTags(mapOf("ElasticServer", "1"))
            .withSku(new Sku().withName("Standard_D4s_v3").withTier(SkuTier.GENERAL_PURPOSE))
            .withAdministratorLogin("cloudsa")
            .withAdministratorLoginPassword("password")
            .withVersion(ServerVersion.ONE_TWO)
            .withStorage(
                new Storage()
                    .withStorageSizeGB(512)
                    .withAutoGrow(StorageAutoGrow.DISABLED)
                    .withTier(AzureManagedDiskPerformanceTiers.P20))
            .withAuthConfig(
                new AuthConfig()
                    .withActiveDirectoryAuth(ActiveDirectoryAuthEnum.ENABLED)
                    .withPasswordAuth(PasswordAuthEnum.ENABLED)
                    .withTenantId("tttttt-tttt-tttt-tttt-tttttttttttt"))
            .withDataEncryption(new DataEncryption().withType(ArmServerKeyType.SYSTEM_MANAGED))
            .withBackup(new Backup().withBackupRetentionDays(7).withGeoRedundantBackup(GeoRedundantBackupEnum.DISABLED))
            .withNetwork(
                new Network()
                    .withDelegatedSubnetResourceId(
                        "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/testrg/providers/Microsoft.Network/virtualNetworks/test-vnet/subnets/test-vnet-subnet")
                    .withPrivateDnsZoneArmResourceId(
                        "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourcegroups/testrg/providers/Microsoft.Network/privateDnsZones/test-private-dns-zone.postgres.database.azure.com"))
            .withHighAvailability(new HighAvailability().withMode(HighAvailabilityMode.ZONE_REDUNDANT))
            .withAvailabilityZone("1")
            .withCreateMode(CreateMode.CREATE)
            .create();
    }

    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/ServerCreateReplica.json
     */
    /**
     * Sample code: ServerCreateReplica.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void serverCreateReplica(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager
            .servers()
            .define("pgtestsvc5rep")
            .withRegion("westus")
            .withExistingResourceGroup("testrg")
            .withIdentity(
                new UserAssignedIdentity()
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/testresourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/test-usermanagedidentity",
                            new UserIdentity()))
                    .withType(IdentityType.USER_ASSIGNED))
            .withDataEncryption(
                new DataEncryption()
                    .withPrimaryKeyUri("fakeTokenPlaceholder")
                    .withPrimaryUserAssignedIdentityId(
                        "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/testresourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/test-usermanagedidentity")
                    .withGeoBackupKeyUri("fakeTokenPlaceholder")
                    .withGeoBackupUserAssignedIdentityId("")
                    .withType(ArmServerKeyType.AZURE_KEY_VAULT))
            .withSourceServerResourceId(
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/testrg/providers/Microsoft.DBforPostgreSQL/flexibleServers/sourcepgservername")
            .withPointInTimeUtc(OffsetDateTime.parse("2021-06-27T00:04:59.4078005+00:00"))
            .withCreateMode(CreateMode.REPLICA)
            .create();
    }

    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/ServerCreatePointInTimeRestore.json
     */
    /**
     * Sample code: Create a database as a point in time restore.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void createADatabaseAsAPointInTimeRestore(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager
            .servers()
            .define("pgtestsvc5")
            .withRegion("westus")
            .withExistingResourceGroup("testrg")
            .withSourceServerResourceId(
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/testrg/providers/Microsoft.DBforPostgreSQL/flexibleServers/sourcepgservername")
            .withPointInTimeUtc(OffsetDateTime.parse("2021-06-27T00:04:59.4078005+00:00"))
            .withCreateMode(CreateMode.POINT_IN_TIME_RESTORE)
            .create();
    }

    // Use "Map.of" if available
    @SuppressWarnings("unchecked")
    private static <T> Map<String, T> mapOf(Object... inputs) {
        Map<String, T> map = new HashMap<>();
        for (int i = 0; i < inputs.length; i += 2) {
            String key = (String) inputs[i];
            T value = (T) inputs[i + 1];
            map.put(key, value);
        }
        return map;
    }
}
```

### Servers_Delete

```java
/** Samples for Servers Delete. */
public final class ServersDeleteSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/ServerDelete.json
     */
    /**
     * Sample code: ServerDelete.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void serverDelete(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.servers().delete("testrg", "testserver", com.azure.core.util.Context.NONE);
    }
}
```

### Servers_GetByResourceGroup

```java
/** Samples for Servers GetByResourceGroup. */
public final class ServersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/ServerGetWithPrivateEndpoints.json
     */
    /**
     * Sample code: ServerGetWithPrivateEndpoints.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void serverGetWithPrivateEndpoints(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.servers().getByResourceGroupWithResponse("testrg", "pgtestsvc2", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/ServerGet.json
     */
    /**
     * Sample code: ServerGet.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void serverGet(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.servers().getByResourceGroupWithResponse("testrg", "pgtestsvc1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/ServerGetWithVnet.json
     */
    /**
     * Sample code: ServerGetWithVnet.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void serverGetWithVnet(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.servers().getByResourceGroupWithResponse("testrg", "pgtestsvc4", com.azure.core.util.Context.NONE);
    }
}
```

### Servers_List

```java
/** Samples for Servers List. */
public final class ServersListSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/ServerList.json
     */
    /**
     * Sample code: ServerList.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void serverList(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.servers().list(com.azure.core.util.Context.NONE);
    }
}
```

### Servers_ListByResourceGroup

```java
/** Samples for Servers ListByResourceGroup. */
public final class ServersListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/ServerListByResourceGroup.json
     */
    /**
     * Sample code: ServerListByResourceGroup.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void serverListByResourceGroup(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.servers().listByResourceGroup("testrg", com.azure.core.util.Context.NONE);
    }
}
```

### Servers_Restart

```java
import com.azure.resourcemanager.postgresqlflexibleserver.models.FailoverMode;
import com.azure.resourcemanager.postgresqlflexibleserver.models.RestartParameter;

/** Samples for Servers Restart. */
public final class ServersRestartSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/ServerRestart.json
     */
    /**
     * Sample code: ServerRestart.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void serverRestart(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.servers().restart("testrg", "testserver", null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/ServerRestartWithFailover.json
     */
    /**
     * Sample code: ServerRestartWithFailover.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void serverRestartWithFailover(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager
            .servers()
            .restart(
                "testrg",
                "testserver",
                new RestartParameter().withRestartWithFailover(true).withFailoverMode(FailoverMode.FORCED_FAILOVER),
                com.azure.core.util.Context.NONE);
    }
}
```

### Servers_Start

```java
/** Samples for Servers Start. */
public final class ServersStartSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/ServerStart.json
     */
    /**
     * Sample code: ServerStart.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void serverStart(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.servers().start("testrg", "testserver", com.azure.core.util.Context.NONE);
    }
}
```

### Servers_Stop

```java
/** Samples for Servers Stop. */
public final class ServersStopSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/ServerStop.json
     */
    /**
     * Sample code: ServerStop.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void serverStop(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.servers().stop("testrg", "testserver", com.azure.core.util.Context.NONE);
    }
}
```

### Servers_Update

```java
import com.azure.resourcemanager.postgresqlflexibleserver.models.ActiveDirectoryAuthEnum;
import com.azure.resourcemanager.postgresqlflexibleserver.models.ArmServerKeyType;
import com.azure.resourcemanager.postgresqlflexibleserver.models.AuthConfig;
import com.azure.resourcemanager.postgresqlflexibleserver.models.AzureManagedDiskPerformanceTiers;
import com.azure.resourcemanager.postgresqlflexibleserver.models.Backup;
import com.azure.resourcemanager.postgresqlflexibleserver.models.CreateModeForUpdate;
import com.azure.resourcemanager.postgresqlflexibleserver.models.DataEncryption;
import com.azure.resourcemanager.postgresqlflexibleserver.models.IdentityType;
import com.azure.resourcemanager.postgresqlflexibleserver.models.MaintenanceWindow;
import com.azure.resourcemanager.postgresqlflexibleserver.models.PasswordAuthEnum;
import com.azure.resourcemanager.postgresqlflexibleserver.models.ReadReplicaPromoteMode;
import com.azure.resourcemanager.postgresqlflexibleserver.models.Replica;
import com.azure.resourcemanager.postgresqlflexibleserver.models.ReplicationPromoteOption;
import com.azure.resourcemanager.postgresqlflexibleserver.models.Server;
import com.azure.resourcemanager.postgresqlflexibleserver.models.ServerVersion;
import com.azure.resourcemanager.postgresqlflexibleserver.models.Sku;
import com.azure.resourcemanager.postgresqlflexibleserver.models.SkuTier;
import com.azure.resourcemanager.postgresqlflexibleserver.models.Storage;
import com.azure.resourcemanager.postgresqlflexibleserver.models.StorageAutoGrow;
import com.azure.resourcemanager.postgresqlflexibleserver.models.UserAssignedIdentity;
import com.azure.resourcemanager.postgresqlflexibleserver.models.UserIdentity;
import java.util.HashMap;
import java.util.Map;

/** Samples for Servers Update. */
public final class ServersUpdateSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/ServerUpdateWithAadAuthEnabled.json
     */
    /**
     * Sample code: ServerUpdateWithAadAuthEnabled.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void serverUpdateWithAadAuthEnabled(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        Server resource =
            manager
                .servers()
                .getByResourceGroupWithResponse("TestGroup", "pgtestsvc4", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withSku(new Sku().withName("Standard_D8s_v3").withTier(SkuTier.GENERAL_PURPOSE))
            .withAdministratorLoginPassword("newpassword")
            .withStorage(
                new Storage()
                    .withStorageSizeGB(1024)
                    .withAutoGrow(StorageAutoGrow.DISABLED)
                    .withTier(AzureManagedDiskPerformanceTiers.P30))
            .withBackup(new Backup().withBackupRetentionDays(20))
            .withAuthConfig(
                new AuthConfig()
                    .withActiveDirectoryAuth(ActiveDirectoryAuthEnum.ENABLED)
                    .withPasswordAuth(PasswordAuthEnum.ENABLED)
                    .withTenantId("tttttt-tttt-tttt-tttt-tttttttttttt"))
            .withCreateMode(CreateModeForUpdate.UPDATE)
            .apply();
    }

    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/ServerUpdateWithDataEncryptionEnabled.json
     */
    /**
     * Sample code: ServerUpdateWithDataEncryptionEnabled.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void serverUpdateWithDataEncryptionEnabled(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        Server resource =
            manager
                .servers()
                .getByResourceGroupWithResponse("TestGroup", "pgtestsvc4", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withSku(new Sku().withName("Standard_D8s_v3").withTier(SkuTier.GENERAL_PURPOSE))
            .withIdentity(
                new UserAssignedIdentity()
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/testresourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/test-geo-usermanagedidentity",
                            new UserIdentity(),
                            "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/testresourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/test-usermanagedidentity",
                            new UserIdentity()))
                    .withType(IdentityType.USER_ASSIGNED))
            .withAdministratorLoginPassword("newpassword")
            .withBackup(new Backup().withBackupRetentionDays(20))
            .withDataEncryption(
                new DataEncryption()
                    .withPrimaryKeyUri("fakeTokenPlaceholder")
                    .withPrimaryUserAssignedIdentityId(
                        "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/testresourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/test-usermanagedidentity")
                    .withGeoBackupKeyUri("fakeTokenPlaceholder")
                    .withGeoBackupUserAssignedIdentityId(
                        "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/testresourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/test-geo-usermanagedidentity")
                    .withType(ArmServerKeyType.AZURE_KEY_VAULT))
            .withCreateMode(CreateModeForUpdate.UPDATE)
            .apply();
    }

    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/PromoteReplicaAsPlannedSwitchover.json
     */
    /**
     * Sample code: SwitchOver a replica server as planned, i.e. it will wait for replication to complete before
     * promoting replica as Primary and original primary as replica.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void
        switchOverAReplicaServerAsPlannedIEItWillWaitForReplicationToCompleteBeforePromotingReplicaAsPrimaryAndOriginalPrimaryAsReplica(
            com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        Server resource =
            manager
                .servers()
                .getByResourceGroupWithResponse(
                    "testResourceGroup", "pgtestsvc4-replica", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withReplica(
                new Replica()
                    .withPromoteMode(ReadReplicaPromoteMode.SWITCHOVER)
                    .withPromoteOption(ReplicationPromoteOption.PLANNED))
            .apply();
    }

    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/PromoteReplicaAsForcedSwitchover.json
     */
    /**
     * Sample code: SwitchOver a replica server as forced, i.e. it will replica as Primary and original primary as
     * replica immediately without waiting for primary and replica to be in sync.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void
        switchOverAReplicaServerAsForcedIEItWillReplicaAsPrimaryAndOriginalPrimaryAsReplicaImmediatelyWithoutWaitingForPrimaryAndReplicaToBeInSync(
            com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        Server resource =
            manager
                .servers()
                .getByResourceGroupWithResponse(
                    "testResourceGroup", "pgtestsvc4-replica", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withReplica(
                new Replica()
                    .withPromoteMode(ReadReplicaPromoteMode.SWITCHOVER)
                    .withPromoteOption(ReplicationPromoteOption.FORCED))
            .apply();
    }

    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/ServerUpdate.json
     */
    /**
     * Sample code: ServerUpdate.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void serverUpdate(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        Server resource =
            manager
                .servers()
                .getByResourceGroupWithResponse("TestGroup", "pgtestsvc4", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withSku(new Sku().withName("Standard_D8s_v3").withTier(SkuTier.GENERAL_PURPOSE))
            .withAdministratorLoginPassword("newpassword")
            .withStorage(
                new Storage()
                    .withStorageSizeGB(1024)
                    .withAutoGrow(StorageAutoGrow.ENABLED)
                    .withTier(AzureManagedDiskPerformanceTiers.P30))
            .withBackup(new Backup().withBackupRetentionDays(20))
            .withCreateMode(CreateModeForUpdate.UPDATE)
            .apply();
    }

    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/ServerUpdateWithMajorVersionUpgrade.json
     */
    /**
     * Sample code: ServerUpdateWithMajorVersionUpgrade.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void serverUpdateWithMajorVersionUpgrade(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        Server resource =
            manager
                .servers()
                .getByResourceGroupWithResponse("testrg", "pgtestsvc4", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withVersion(ServerVersion.ONE_FOUR).withCreateMode(CreateModeForUpdate.UPDATE).apply();
    }

    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/ServerUpdateWithCustomerMaintenanceWindow.json
     */
    /**
     * Sample code: ServerUpdateWithCustomerMaintenanceWindow.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void serverUpdateWithCustomerMaintenanceWindow(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        Server resource =
            manager
                .servers()
                .getByResourceGroupWithResponse("testrg", "pgtestsvc4", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withMaintenanceWindow(
                new MaintenanceWindow()
                    .withCustomWindow("Enabled")
                    .withStartHour(8)
                    .withStartMinute(0)
                    .withDayOfWeek(0))
            .withCreateMode(CreateModeForUpdate.UPDATE)
            .apply();
    }

    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/PromoteReplicaAsForcedStandaloneServer.json
     */
    /**
     * Sample code: Promote a replica server as a Standalone server as forced, i.e. it will promote a replica server
     * immediately without waiting for primary and replica to be in sync.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void
        promoteAReplicaServerAsAStandaloneServerAsForcedIEItWillPromoteAReplicaServerImmediatelyWithoutWaitingForPrimaryAndReplicaToBeInSync(
            com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        Server resource =
            manager
                .servers()
                .getByResourceGroupWithResponse(
                    "testResourceGroup", "pgtestsvc4-replica", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withReplica(
                new Replica()
                    .withPromoteMode(ReadReplicaPromoteMode.STANDALONE)
                    .withPromoteOption(ReplicationPromoteOption.FORCED))
            .apply();
    }

    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/PromoteReplicaAsPlannedStandaloneServer.json
     */
    /**
     * Sample code: Promote a replica server as a Standalone server as planned, i.e. it will wait for replication to
     * complete.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void promoteAReplicaServerAsAStandaloneServerAsPlannedIEItWillWaitForReplicationToComplete(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        Server resource =
            manager
                .servers()
                .getByResourceGroupWithResponse(
                    "testResourceGroup", "pgtestsvc4-replica", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withReplica(
                new Replica()
                    .withPromoteMode(ReadReplicaPromoteMode.STANDALONE)
                    .withPromoteOption(ReplicationPromoteOption.PLANNED))
            .apply();
    }

    // Use "Map.of" if available
    @SuppressWarnings("unchecked")
    private static <T> Map<String, T> mapOf(Object... inputs) {
        Map<String, T> map = new HashMap<>();
        for (int i = 0; i < inputs.length; i += 2) {
            String key = (String) inputs[i];
            T value = (T) inputs[i + 1];
            map.put(key, value);
        }
        return map;
    }
}
```

### VirtualEndpoints_Create

```java
import com.azure.resourcemanager.postgresqlflexibleserver.models.VirtualEndpointType;
import java.util.Arrays;

/** Samples for VirtualEndpoints Create. */
public final class VirtualEndpointsCreateSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/VirtualEndpointCreate.json
     */
    /**
     * Sample code: Create a new virtual endpoint for a flexible server.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void createANewVirtualEndpointForAFlexibleServer(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager
            .virtualEndpoints()
            .define("pgVirtualEndpoint1")
            .withExistingFlexibleServer("testrg", "pgtestsvc4")
            .withEndpointType(VirtualEndpointType.READ_WRITE)
            .withMembers(Arrays.asList("testPrimary1"))
            .create();
    }
}
```

### VirtualEndpoints_Delete

```java
/** Samples for VirtualEndpoints Delete. */
public final class VirtualEndpointsDeleteSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/VirtualEndpointDelete.json
     */
    /**
     * Sample code: Delete a virtual endpoint.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void deleteAVirtualEndpoint(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager
            .virtualEndpoints()
            .delete("testrg", "pgtestsvc4", "pgVirtualEndpoint1", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualEndpoints_Get

```java
/** Samples for VirtualEndpoints Get. */
public final class VirtualEndpointsGetSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/VirtualEndpointsGet.json
     */
    /**
     * Sample code: Get a virtual endpoint.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void getAVirtualEndpoint(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager
            .virtualEndpoints()
            .getWithResponse("testrg", "pgtestsvc4", "pgVirtualEndpoint1", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualEndpoints_ListByServer

```java
/** Samples for VirtualEndpoints ListByServer. */
public final class VirtualEndpointsListByServerSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/VirtualEndpointsListByServer.json
     */
    /**
     * Sample code: VirtualEndpointListByServer.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void virtualEndpointListByServer(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.virtualEndpoints().listByServer("testrg", "pgtestsvc4", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualEndpoints_Update

```java
import com.azure.resourcemanager.postgresqlflexibleserver.models.VirtualEndpointResource;
import com.azure.resourcemanager.postgresqlflexibleserver.models.VirtualEndpointType;
import java.util.Arrays;

/** Samples for VirtualEndpoints Update. */
public final class VirtualEndpointsUpdateSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/VirtualEndpointUpdate.json
     */
    /**
     * Sample code: Update a virtual endpoint for a server to update the.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void updateAVirtualEndpointForAServerToUpdateThe(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        VirtualEndpointResource resource =
            manager
                .virtualEndpoints()
                .getWithResponse("testrg", "pgtestsvc4", "pgVirtualEndpoint1", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withEndpointType(VirtualEndpointType.READ_WRITE)
            .withMembers(Arrays.asList("testReplica1"))
            .apply();
    }
}
```

### VirtualNetworkSubnetUsage_Execute

```java
import com.azure.resourcemanager.postgresqlflexibleserver.models.VirtualNetworkSubnetUsageParameter;

/** Samples for VirtualNetworkSubnetUsage Execute. */
public final class VirtualNetworkSubnetUsageExecuteSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/preview/2023-06-01-preview/examples/VirtualNetworkSubnetUsage.json
     */
    /**
     * Sample code: VirtualNetworkSubnetUsageList.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void virtualNetworkSubnetUsageList(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager
            .virtualNetworkSubnetUsages()
            .executeWithResponse(
                "westus",
                new VirtualNetworkSubnetUsageParameter()
                    .withVirtualNetworkArmResourceId(
                        "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/testrg/providers/Microsoft.Network/virtualNetworks/testvnet"),
                com.azure.core.util.Context.NONE);
    }
}
```

