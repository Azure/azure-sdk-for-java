# Code snippets and samples


## AdvancedThreatProtectionSettings

- [Get](#advancedthreatprotectionsettings_get)
- [List](#advancedthreatprotectionsettings_list)
- [Update](#advancedthreatprotectionsettings_update)
- [UpdatePut](#advancedthreatprotectionsettings_updateput)

## AzureADAdministrators

- [CreateOrUpdate](#azureadadministrators_createorupdate)
- [Delete](#azureadadministrators_delete)
- [Get](#azureadadministrators_get)
- [ListByServer](#azureadadministrators_listbyserver)

## BackupAndExport

- [Create](#backupandexport_create)
- [ValidateBackup](#backupandexport_validatebackup)

## Backups

- [Get](#backups_get)
- [ListByServer](#backups_listbyserver)
- [Put](#backups_put)

## CheckNameAvailability

- [Execute](#checknameavailability_execute)

## CheckNameAvailabilityWithoutLocation

- [Execute](#checknameavailabilitywithoutlocation_execute)

## CheckVirtualNetworkSubnetUsage

- [Execute](#checkvirtualnetworksubnetusage_execute)

## Configurations

- [BatchUpdate](#configurations_batchupdate)
- [CreateOrUpdate](#configurations_createorupdate)
- [Get](#configurations_get)
- [ListByServer](#configurations_listbyserver)
- [Update](#configurations_update)

## Databases

- [CreateOrUpdate](#databases_createorupdate)
- [Delete](#databases_delete)
- [Get](#databases_get)
- [ListByServer](#databases_listbyserver)

## FirewallRules

- [CreateOrUpdate](#firewallrules_createorupdate)
- [Delete](#firewallrules_delete)
- [Get](#firewallrules_get)
- [ListByServer](#firewallrules_listbyserver)

## GetPrivateDnsZoneSuffix

- [Execute](#getprivatednszonesuffix_execute)

## LocationBasedCapabilities

- [List](#locationbasedcapabilities_list)

## LocationBasedCapabilitySet

- [Get](#locationbasedcapabilityset_get)
- [List](#locationbasedcapabilityset_list)

## LogFiles

- [ListByServer](#logfiles_listbyserver)

## LongRunningBackupOperation

- [Create](#longrunningbackupoperation_create)

## LongRunningBackups

- [Get](#longrunningbackups_get)
- [List](#longrunningbackups_list)

## Maintenances

- [List](#maintenances_list)
- [Read](#maintenances_read)
- [Update](#maintenances_update)

## OperationProgress

- [Get](#operationprogress_get)

## OperationResults

- [Get](#operationresults_get)

## Operations

- [List](#operations_list)

## PrivateEndpointConnections

- [CreateOrUpdate](#privateendpointconnections_createorupdate)
- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [ListByServer](#privateendpointconnections_listbyserver)

## PrivateLinkResources

- [Get](#privatelinkresources_get)
- [ListByServer](#privatelinkresources_listbyserver)

## Replicas

- [ListByServer](#replicas_listbyserver)

## Servers

- [Create](#servers_create)
- [Delete](#servers_delete)
- [DetachVNet](#servers_detachvnet)
- [Failover](#servers_failover)
- [GetByResourceGroup](#servers_getbyresourcegroup)
- [List](#servers_list)
- [ListByResourceGroup](#servers_listbyresourcegroup)
- [ResetGtid](#servers_resetgtid)
- [Restart](#servers_restart)
- [Start](#servers_start)
- [Stop](#servers_stop)
- [Update](#servers_update)
- [ValidateEstimateHighAvailability](#servers_validateestimatehighavailability)

## ServersMigration

- [CutoverMigration](#serversmigration_cutovermigration)
### AdvancedThreatProtectionSettings_Get

```java
import com.azure.resourcemanager.mysqlflexibleserver.models.AdvancedThreatProtectionName;

/**
 * Samples for AdvancedThreatProtectionSettings Get.
 */
public final class AdvancedThreatProtectionSettingsGetSamples {
    /*
     * x-ms-original-file: 2024-12-30/AdvancedThreatProtectionSettingsGet.json
     */
    /**
     * Sample code: Get a server's Advanced Threat Protection settings.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void getAServerSAdvancedThreatProtectionSettings(
        com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.advancedThreatProtectionSettings()
            .getWithResponse("threatprotection-6852", "threatprotection-2080", AdvancedThreatProtectionName.DEFAULT,
                com.azure.core.util.Context.NONE);
    }
}
```

### AdvancedThreatProtectionSettings_List

```java
/**
 * Samples for AdvancedThreatProtectionSettings List.
 */
public final class AdvancedThreatProtectionSettingsListSamples {
    /*
     * x-ms-original-file: 2024-12-30/AdvancedThreatProtectionSettingsList.json
     */
    /**
     * Sample code: Get list of server's Advanced Threat Protection settings.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void getListOfServerSAdvancedThreatProtectionSettings(
        com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.advancedThreatProtectionSettings()
            .list("threatprotection-6852", "threatprotection-2080", com.azure.core.util.Context.NONE);
    }
}
```

### AdvancedThreatProtectionSettings_Update

```java
import com.azure.resourcemanager.mysqlflexibleserver.models.AdvancedThreatProtectionForUpdate;
import com.azure.resourcemanager.mysqlflexibleserver.models.AdvancedThreatProtectionName;
import com.azure.resourcemanager.mysqlflexibleserver.models.AdvancedThreatProtectionState;

/**
 * Samples for AdvancedThreatProtectionSettings Update.
 */
public final class AdvancedThreatProtectionSettingsUpdateSamples {
    /*
     * x-ms-original-file: 2024-12-30/AdvancedThreatProtectionSettingsPatchDisabled.json
     */
    /**
     * Sample code: Disable a server's Advanced Threat Protection settings with all parameters.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void disableAServerSAdvancedThreatProtectionSettingsWithAllParameters(
        com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.advancedThreatProtectionSettings()
            .update("threatprotection-4799", "threatprotection-6440", AdvancedThreatProtectionName.DEFAULT,
                new AdvancedThreatProtectionForUpdate().withState(AdvancedThreatProtectionState.DISABLED),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2024-12-30/AdvancedThreatProtectionSettingsPatchEnabled.json
     */
    /**
     * Sample code: Enable a server's Advanced Threat Protection settings.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void enableAServerSAdvancedThreatProtectionSettings(
        com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.advancedThreatProtectionSettings()
            .update("threatprotection-4799", "threatprotection-6440", AdvancedThreatProtectionName.DEFAULT,
                new AdvancedThreatProtectionForUpdate().withState(AdvancedThreatProtectionState.ENABLED),
                com.azure.core.util.Context.NONE);
    }
}
```

### AdvancedThreatProtectionSettings_UpdatePut

```java
import com.azure.resourcemanager.mysqlflexibleserver.fluent.models.AdvancedThreatProtectionInner;
import com.azure.resourcemanager.mysqlflexibleserver.models.AdvancedThreatProtectionName;
import com.azure.resourcemanager.mysqlflexibleserver.models.AdvancedThreatProtectionState;

/**
 * Samples for AdvancedThreatProtectionSettings UpdatePut.
 */
public final class AdvancedThreatProtectionSettingsUpdatePutSamples {
    /*
     * x-ms-original-file: 2024-12-30/AdvancedThreatProtectionSettingsPutDisabled.json
     */
    /**
     * Sample code: Disable a server's Advanced Threat Protection settings with all parameters using PUT.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void disableAServerSAdvancedThreatProtectionSettingsWithAllParametersUsingPUT(
        com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.advancedThreatProtectionSettings()
            .updatePut("threatprotection-4799", "threatprotection-6440", AdvancedThreatProtectionName.DEFAULT,
                new AdvancedThreatProtectionInner().withState(AdvancedThreatProtectionState.DISABLED),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2024-12-30/AdvancedThreatProtectionSettingsPutEnabled.json
     */
    /**
     * Sample code: Enable a server's Advanced Threat Protection settings using PUT.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void enableAServerSAdvancedThreatProtectionSettingsUsingPUT(
        com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.advancedThreatProtectionSettings()
            .updatePut("threatprotection-4799", "threatprotection-6440", AdvancedThreatProtectionName.DEFAULT,
                new AdvancedThreatProtectionInner().withState(AdvancedThreatProtectionState.ENABLED),
                com.azure.core.util.Context.NONE);
    }
}
```

### AzureADAdministrators_CreateOrUpdate

```java
import com.azure.resourcemanager.mysqlflexibleserver.models.AdministratorName;
import com.azure.resourcemanager.mysqlflexibleserver.models.AdministratorType;

/**
 * Samples for AzureADAdministrators CreateOrUpdate.
 */
public final class AzureADAdministratorsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2024-12-30/AzureADAdministratorCreate.json
     */
    /**
     * Sample code: Create an azure ad administrator.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void
        createAnAzureAdAdministrator(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.azureADAdministrators()
            .define(AdministratorName.ACTIVE_DIRECTORY)
            .withExistingFlexibleServer("testrg", "mysqltestsvc4")
            .withAdministratorType(AdministratorType.ACTIVE_DIRECTORY)
            .withLogin("bob@contoso.com")
            .withSid("c6b82b90-a647-49cb-8a62-0d2d3cb7ac7c")
            .withTenantId("c12b7025-bfe2-46c1-b463-993b5e4cd467")
            .withIdentityResourceId(
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/test-group/providers/Microsoft.ManagedIdentity/userAssignedIdentities/test-umi")
            .create();
    }
}
```

### AzureADAdministrators_Delete

```java
import com.azure.resourcemanager.mysqlflexibleserver.models.AdministratorName;

/**
 * Samples for AzureADAdministrators Delete.
 */
public final class AzureADAdministratorsDeleteSamples {
    /*
     * x-ms-original-file: 2024-12-30/AzureADAdministratorDelete.json
     */
    /**
     * Sample code: Delete an azure ad administrator.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void
        deleteAnAzureAdAdministrator(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.azureADAdministrators()
            .delete("testrg", "mysqltestsvc4", AdministratorName.ACTIVE_DIRECTORY, com.azure.core.util.Context.NONE);
    }
}
```

### AzureADAdministrators_Get

```java
import com.azure.resourcemanager.mysqlflexibleserver.models.AdministratorName;

/**
 * Samples for AzureADAdministrators Get.
 */
public final class AzureADAdministratorsGetSamples {
    /*
     * x-ms-original-file: 2024-12-30/AzureADAdministratorGet.json
     */
    /**
     * Sample code: Get an azure ad administrator.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void getAnAzureAdAdministrator(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.azureADAdministrators()
            .getWithResponse("testrg", "mysqltestsvc4", AdministratorName.ACTIVE_DIRECTORY,
                com.azure.core.util.Context.NONE);
    }
}
```

### AzureADAdministrators_ListByServer

```java
/**
 * Samples for AzureADAdministrators ListByServer.
 */
public final class AzureADAdministratorsListByServerSamples {
    /*
     * x-ms-original-file: 2024-12-30/AzureADAdministratorsListByServer.json
     */
    /**
     * Sample code: List Azure AD administrators in a server.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void
        listAzureADAdministratorsInAServer(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.azureADAdministrators().listByServer("testrg", "mysqltestsvc4", com.azure.core.util.Context.NONE);
    }
}
```

### BackupAndExport_Create

```java
import com.azure.resourcemanager.mysqlflexibleserver.models.BackupAndExportRequest;
import com.azure.resourcemanager.mysqlflexibleserver.models.BackupSettings;
import com.azure.resourcemanager.mysqlflexibleserver.models.FullBackupStoreDetails;
import java.util.Arrays;

/**
 * Samples for BackupAndExport Create.
 */
public final class BackupAndExportCreateSamples {
    /*
     * x-ms-original-file: 2024-12-30/BackupAndExport.json
     */
    /**
     * Sample code: Create and Export Backup.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void createAndExportBackup(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.backupAndExports()
            .create("TestGroup", "mysqltestserver", new BackupAndExportRequest()
                .withBackupSettings(new BackupSettings().withBackupName("customer-backup-name"))
                .withTargetDetails(new FullBackupStoreDetails().withSasUriList(Arrays.asList("sasuri1", "sasuri2"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### BackupAndExport_ValidateBackup

```java
/**
 * Samples for BackupAndExport ValidateBackup.
 */
public final class BackupAndExportValidateBackupSamples {
    /*
     * x-ms-original-file: 2024-12-30/ValidateBackup.json
     */
    /**
     * Sample code: Validate Backup.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void validateBackup(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.backupAndExports()
            .validateBackupWithResponse("TestGroup", "mysqltestserver", com.azure.core.util.Context.NONE);
    }
}
```

### Backups_Get

```java
/**
 * Samples for Backups Get.
 */
public final class BackupsGetSamples {
    /*
     * x-ms-original-file: 2024-12-30/BackupGet.json
     */
    /**
     * Sample code: Get a backup for a server.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void getABackupForAServer(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.backups()
            .getWithResponse("TestGroup", "mysqltestserver", "daily_20210615T160516", com.azure.core.util.Context.NONE);
    }
}
```

### Backups_ListByServer

```java
/**
 * Samples for Backups ListByServer.
 */
public final class BackupsListByServerSamples {
    /*
     * x-ms-original-file: 2024-12-30/BackupsListByServer.json
     */
    /**
     * Sample code: List backups for a server.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void listBackupsForAServer(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.backups().listByServer("TestGroup", "mysqltestserver", com.azure.core.util.Context.NONE);
    }
}
```

### Backups_Put

```java
/**
 * Samples for Backups Put.
 */
public final class BackupsPutSamples {
    /*
     * x-ms-original-file: 2024-12-30/BackupPut.json
     */
    /**
     * Sample code: Create backup for a server.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void createBackupForAServer(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.backups().putWithResponse("TestGroup", "mysqltestserver", "mybackup", com.azure.core.util.Context.NONE);
    }
}
```

### CheckNameAvailability_Execute

```java
import com.azure.resourcemanager.mysqlflexibleserver.models.NameAvailabilityRequest;

/**
 * Samples for CheckNameAvailability Execute.
 */
public final class CheckNameAvailabilityExecuteSamples {
    /*
     * x-ms-original-file: 2024-12-30/CheckNameAvailability.json
     */
    /**
     * Sample code: Check name availability.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void checkNameAvailability(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.checkNameAvailabilities()
            .executeWithResponse("SouthEastAsia",
                new NameAvailabilityRequest().withName("name1").withType("Microsoft.DBforMySQL/flexibleServers"),
                com.azure.core.util.Context.NONE);
    }
}
```

### CheckNameAvailabilityWithoutLocation_Execute

```java
import com.azure.resourcemanager.mysqlflexibleserver.models.NameAvailabilityRequest;

/**
 * Samples for CheckNameAvailabilityWithoutLocation Execute.
 */
public final class CheckNameAvailabilityWithoutLocationExecuteSamples {
    /*
     * x-ms-original-file: 2024-12-30/CheckNameAvailabilityWithoutLocation.json
     */
    /**
     * Sample code: Check name availability.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void checkNameAvailability(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.checkNameAvailabilityWithoutLocations()
            .executeWithResponse(
                new NameAvailabilityRequest().withName("name1").withType("Microsoft.DBforMySQL/flexibleServers"),
                com.azure.core.util.Context.NONE);
    }
}
```

### CheckVirtualNetworkSubnetUsage_Execute

```java
import com.azure.resourcemanager.mysqlflexibleserver.models.VirtualNetworkSubnetUsageParameter;

/**
 * Samples for CheckVirtualNetworkSubnetUsage Execute.
 */
public final class CheckVirtualNetworkSubnetUsageExecuteSamples {
    /*
     * x-ms-original-file: 2024-12-30/CheckVirtualNetworkSubnetUsage.json
     */
    /**
     * Sample code: CheckVirtualNetworkSubnetUsage.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void
        checkVirtualNetworkSubnetUsage(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.checkVirtualNetworkSubnetUsages()
            .executeWithResponse("WestUS", new VirtualNetworkSubnetUsageParameter().withVirtualNetworkResourceId(
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/testrg/providers/Microsoft.Network/virtualNetworks/testvnet"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Configurations_BatchUpdate

```java
import com.azure.resourcemanager.mysqlflexibleserver.models.ConfigurationForBatchUpdate;
import com.azure.resourcemanager.mysqlflexibleserver.models.ConfigurationListForBatchUpdate;
import com.azure.resourcemanager.mysqlflexibleserver.models.ResetAllToDefault;
import java.util.Arrays;

/**
 * Samples for Configurations BatchUpdate.
 */
public final class ConfigurationsBatchUpdateSamples {
    /*
     * x-ms-original-file: 2024-12-30/ConfigurationsBatchUpdate.json
     */
    /**
     * Sample code: ConfigurationList.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void configurationList(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.configurations()
            .batchUpdate("testrg", "mysqltestserver", new ConfigurationListForBatchUpdate()
                .withValue(Arrays.asList(new ConfigurationForBatchUpdate().withName("event_scheduler").withValue("OFF"),
                    new ConfigurationForBatchUpdate().withName("div_precision_increment").withValue("8")))
                .withResetAllToDefault(ResetAllToDefault.FALSE), com.azure.core.util.Context.NONE);
    }
}
```

### Configurations_CreateOrUpdate

```java
import com.azure.resourcemanager.mysqlflexibleserver.models.ConfigurationSource;

/**
 * Samples for Configurations CreateOrUpdate.
 */
public final class ConfigurationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2024-12-30/ConfigurationCreateOrUpdate.json
     */
    /**
     * Sample code: ConfigurationCreateOrUpdate.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void configurationCreateOrUpdate(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.configurations()
            .define("event_scheduler")
            .withExistingFlexibleServer("TestGroup", "testserver")
            .withValue("off")
            .withSource(ConfigurationSource.USER_OVERRIDE)
            .create();
    }
}
```

### Configurations_Get

```java
/**
 * Samples for Configurations Get.
 */
public final class ConfigurationsGetSamples {
    /*
     * x-ms-original-file: 2024-12-30/ConfigurationGet.json
     */
    /**
     * Sample code: Get a configuration.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void getAConfiguration(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.configurations()
            .getWithResponse("TestGroup", "testserver", "event_scheduler", com.azure.core.util.Context.NONE);
    }
}
```

### Configurations_ListByServer

```java
/**
 * Samples for Configurations ListByServer.
 */
public final class ConfigurationsListByServerSamples {
    /*
     * x-ms-original-file: 2024-12-30/ConfigurationsListByServer.json
     */
    /**
     * Sample code: List all configurations for a server.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void
        listAllConfigurationsForAServer(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.configurations()
            .listByServer("testrg", "mysqltestserver", null, null, 1, 8, com.azure.core.util.Context.NONE);
    }
}
```

### Configurations_Update

```java
import com.azure.resourcemanager.mysqlflexibleserver.models.Configuration;
import com.azure.resourcemanager.mysqlflexibleserver.models.ConfigurationSource;

/**
 * Samples for Configurations Update.
 */
public final class ConfigurationsUpdateSamples {
    /*
     * x-ms-original-file: 2024-12-30/ConfigurationUpdate.json
     */
    /**
     * Sample code: Update a user configuration.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void updateAUserConfiguration(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        Configuration resource = manager.configurations()
            .getWithResponse("testrg", "testserver", "event_scheduler", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withValue("on").withSource(ConfigurationSource.USER_OVERRIDE).apply();
    }
}
```

### Databases_CreateOrUpdate

```java
/**
 * Samples for Databases CreateOrUpdate.
 */
public final class DatabasesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2024-12-30/DatabaseCreate.json
     */
    /**
     * Sample code: Create a database.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void createADatabase(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.databases()
            .define("db1")
            .withExistingFlexibleServer("TestGroup", "testserver")
            .withCharset("utf8")
            .withCollation("utf8_general_ci")
            .create();
    }
}
```

### Databases_Delete

```java
/**
 * Samples for Databases Delete.
 */
public final class DatabasesDeleteSamples {
    /*
     * x-ms-original-file: 2024-12-30/DatabaseDelete.json
     */
    /**
     * Sample code: Delete a database.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void deleteADatabase(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.databases().delete("TestGroup", "testserver", "db1", com.azure.core.util.Context.NONE);
    }
}
```

### Databases_Get

```java
/**
 * Samples for Databases Get.
 */
public final class DatabasesGetSamples {
    /*
     * x-ms-original-file: 2024-12-30/DatabaseGet.json
     */
    /**
     * Sample code: Get a database.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void getADatabase(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.databases().getWithResponse("TestGroup", "testserver", "db1", com.azure.core.util.Context.NONE);
    }
}
```

### Databases_ListByServer

```java
/**
 * Samples for Databases ListByServer.
 */
public final class DatabasesListByServerSamples {
    /*
     * x-ms-original-file: 2024-12-30/DatabasesListByServer.json
     */
    /**
     * Sample code: List databases in a server.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void listDatabasesInAServer(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.databases().listByServer("TestGroup", "testserver", com.azure.core.util.Context.NONE);
    }
}
```

### FirewallRules_CreateOrUpdate

```java
/**
 * Samples for FirewallRules CreateOrUpdate.
 */
public final class FirewallRulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2024-12-30/FirewallRuleCreate.json
     */
    /**
     * Sample code: Create a firewall rule.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void createAFirewallRule(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.firewallRules()
            .define("rule1")
            .withExistingFlexibleServer("TestGroup", "testserver")
            .withStartIpAddress("0.0.0.0")
            .withEndIpAddress("255.255.255.255")
            .create();
    }
}
```

### FirewallRules_Delete

```java
/**
 * Samples for FirewallRules Delete.
 */
public final class FirewallRulesDeleteSamples {
    /*
     * x-ms-original-file: 2024-12-30/FirewallRuleDelete.json
     */
    /**
     * Sample code: Delete a firewall rule.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void deleteAFirewallRule(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.firewallRules().delete("TestGroup", "testserver", "rule1", com.azure.core.util.Context.NONE);
    }
}
```

### FirewallRules_Get

```java
/**
 * Samples for FirewallRules Get.
 */
public final class FirewallRulesGetSamples {
    /*
     * x-ms-original-file: 2024-12-30/FirewallRuleGet.json
     */
    /**
     * Sample code: Get a firewall rule.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void getAFirewallRule(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.firewallRules().getWithResponse("TestGroup", "testserver", "rule1", com.azure.core.util.Context.NONE);
    }
}
```

### FirewallRules_ListByServer

```java
/**
 * Samples for FirewallRules ListByServer.
 */
public final class FirewallRulesListByServerSamples {
    /*
     * x-ms-original-file: 2024-12-30/FirewallRulesListByServer.json
     */
    /**
     * Sample code: List all firewall rules in a server.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void
        listAllFirewallRulesInAServer(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.firewallRules().listByServer("TestGroup", "testserver", com.azure.core.util.Context.NONE);
    }
}
```

### GetPrivateDnsZoneSuffix_Execute

```java
/**
 * Samples for GetPrivateDnsZoneSuffix Execute.
 */
public final class GetPrivateDnsZoneSuffixExecuteSamples {
    /*
     * x-ms-original-file: 2024-12-30/GetPrivateDnsZoneSuffix.json
     */
    /**
     * Sample code: GetPrivateDnsZoneSuffix.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void getPrivateDnsZoneSuffix(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.getPrivateDnsZoneSuffixes().executeWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

### LocationBasedCapabilities_List

```java
/**
 * Samples for LocationBasedCapabilities List.
 */
public final class LocationBasedCapabilitiesListSamples {
    /*
     * x-ms-original-file: 2024-12-30/CapabilitiesByLocationList.json
     */
    /**
     * Sample code: CapabilitiesList.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void capabilitiesList(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.locationBasedCapabilities().list("WestUS", com.azure.core.util.Context.NONE);
    }
}
```

### LocationBasedCapabilitySet_Get

```java
/**
 * Samples for LocationBasedCapabilitySet Get.
 */
public final class LocationBasedCapabilitySetGetSamples {
    /*
     * x-ms-original-file: 2024-12-30/CapabilitySetByLocation.json
     */
    /**
     * Sample code: CapabilityResult.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void capabilityResult(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.locationBasedCapabilitySets().getWithResponse("WestUS", "default", com.azure.core.util.Context.NONE);
    }
}
```

### LocationBasedCapabilitySet_List

```java
/**
 * Samples for LocationBasedCapabilitySet List.
 */
public final class LocationBasedCapabilitySetListSamples {
    /*
     * x-ms-original-file: 2024-12-30/CapabilitySetListByLocation.json
     */
    /**
     * Sample code: CapabilitySetsResult.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void capabilitySetsResult(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.locationBasedCapabilitySets().list("WestUS", com.azure.core.util.Context.NONE);
    }
}
```

### LogFiles_ListByServer

```java
/**
 * Samples for LogFiles ListByServer.
 */
public final class LogFilesListByServerSamples {
    /*
     * x-ms-original-file: 2024-12-30/LogFilesListByServer.json
     */
    /**
     * Sample code: List all server log files for a server.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void
        listAllServerLogFilesForAServer(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.logFiles().listByServer("testrg", "mysqltestsvc1", com.azure.core.util.Context.NONE);
    }
}
```

### LongRunningBackupOperation_Create

```java
/**
 * Samples for LongRunningBackupOperation Create.
 */
public final class LongRunningBackupOperationCreateSamples {
    /*
     * x-ms-original-file: 2024-12-30/LongRunningBackup.json
     */
    /**
     * Sample code: Create backup for a server.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void createBackupForAServer(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.longRunningBackupOperations()
            .define("testback")
            .withExistingFlexibleServer("TestGroup", "mysqltestserver")
            .create();
    }
}
```

### LongRunningBackups_Get

```java
/**
 * Samples for LongRunningBackups Get.
 */
public final class LongRunningBackupsGetSamples {
    /*
     * x-ms-original-file: 2024-12-30/LongRunningBackupGet.json
     */
    /**
     * Sample code: Get a backup for a server.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void getABackupForAServer(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.longRunningBackups()
            .getWithResponse("TestGroup", "mysqltestserver", "daily_20210615T160516", com.azure.core.util.Context.NONE);
    }
}
```

### LongRunningBackups_List

```java
/**
 * Samples for LongRunningBackups List.
 */
public final class LongRunningBackupsListSamples {
    /*
     * x-ms-original-file: 2024-12-30/LongRunningBackupsListByServer.json
     */
    /**
     * Sample code: List backups for a server.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void listBackupsForAServer(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.longRunningBackups().list("TestGroup", "mysqltestserver", com.azure.core.util.Context.NONE);
    }
}
```

### Maintenances_List

```java
/**
 * Samples for Maintenances List.
 */
public final class MaintenancesListSamples {
    /*
     * x-ms-original-file: 2024-12-30/MaintenancesListByServer.json
     */
    /**
     * Sample code: List maintenances on a server.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void listMaintenancesOnAServer(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.maintenances().list("TestGroup", "testserver", com.azure.core.util.Context.NONE);
    }
}
```

### Maintenances_Read

```java
/**
 * Samples for Maintenances Read.
 */
public final class MaintenancesReadSamples {
    /*
     * x-ms-original-file: 2024-12-30/MaintenanceRead.json
     */
    /**
     * Sample code: Read a maintenance.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void readAMaintenance(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.maintenances()
            .readWithResponse("TestGroup", "testserver", "_T9Q-TS8", com.azure.core.util.Context.NONE);
    }
}
```

### Maintenances_Update

```java
import com.azure.resourcemanager.mysqlflexibleserver.models.MaintenanceUpdate;
import java.time.OffsetDateTime;

/**
 * Samples for Maintenances Update.
 */
public final class MaintenancesUpdateSamples {
    /*
     * x-ms-original-file: 2024-12-30/MaintenanceUpdate.json
     */
    /**
     * Sample code: Update maintenance on a server.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void updateMaintenanceOnAServer(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.maintenances()
            .update("TestGroup", "testserver", "_T9Q-TS8",
                new MaintenanceUpdate().withMaintenanceStartTime(OffsetDateTime.parse("2024-01-20T00:00:00")),
                com.azure.core.util.Context.NONE);
    }
}
```

### OperationProgress_Get

```java
/**
 * Samples for OperationProgress Get.
 */
public final class OperationProgressGetSamples {
    /*
     * x-ms-original-file: 2024-12-30/OperationProgress_Get_BackupAndExport.json
     */
    /**
     * Sample code: OperationProgress_Get BackupAndExport.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void
        operationProgressGetBackupAndExport(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.operationProgress()
            .getWithResponse("westus", "00000000-0000-0000-0000-000000000000", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2024-12-30/OperationProgress_Get_ImportFromStorage.json
     */
    /**
     * Sample code: OperationProgress_Get ImportFromStorage.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void
        operationProgressGetImportFromStorage(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.operationProgress()
            .getWithResponse("westus", "00000000-0000-0000-0000-000000000000", com.azure.core.util.Context.NONE);
    }
}
```

### OperationResults_Get

```java
/**
 * Samples for OperationResults Get.
 */
public final class OperationResultsGetSamples {
    /*
     * x-ms-original-file: 2024-12-30/OperationResults_Get.json
     */
    /**
     * Sample code: OperationResults_Get.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void operationResultsGet(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.operationResults()
            .getWithResponse("westus", "00000000-0000-0000-0000-000000000000", com.azure.core.util.Context.NONE);
    }
}
```

### Operations_List

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2024-12-30/OperationsList.json
     */
    /**
     * Sample code: OperationList.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void operationList(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_CreateOrUpdate

```java
import com.azure.resourcemanager.mysqlflexibleserver.models.PrivateEndpointServiceConnectionStatus;
import com.azure.resourcemanager.mysqlflexibleserver.models.PrivateLinkServiceConnectionState;

/**
 * Samples for PrivateEndpointConnections CreateOrUpdate.
 */
public final class PrivateEndpointConnectionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2024-12-30/PrivateEndpointConnectionUpdate.json
     */
    /**
     * Sample code: Approve or reject a private endpoint connection with a given name.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void approveOrRejectAPrivateEndpointConnectionWithAGivenName(
        com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.privateEndpointConnections()
            .define("private-endpoint-connection-name")
            .withExistingFlexibleServer("Default", "test-svr")
            .withPrivateLinkServiceConnectionState(
                new PrivateLinkServiceConnectionState().withStatus(PrivateEndpointServiceConnectionStatus.APPROVED)
                    .withDescription("Approved by johndoe@contoso.com"))
            .create();
    }
}
```

### PrivateEndpointConnections_Delete

```java
/**
 * Samples for PrivateEndpointConnections Delete.
 */
public final class PrivateEndpointConnectionsDeleteSamples {
    /*
     * x-ms-original-file: 2024-12-30/PrivateEndpointConnectionDelete.json
     */
    /**
     * Sample code: Deletes a private endpoint connection with a given name.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void deletesAPrivateEndpointConnectionWithAGivenName(
        com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.privateEndpointConnections()
            .delete("Default", "test-svr", "private-endpoint-connection-name", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_Get

```java
/**
 * Samples for PrivateEndpointConnections Get.
 */
public final class PrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file: 2024-12-30/PrivateEndpointConnectionGet.json
     */
    /**
     * Sample code: Gets private endpoint connection.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void
        getsPrivateEndpointConnection(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.privateEndpointConnections()
            .getWithResponse("Default", "test-svr", "private-endpoint-connection-name",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_ListByServer

```java
/**
 * Samples for PrivateEndpointConnections ListByServer.
 */
public final class PrivateEndpointConnectionsListByServerSamples {
    /*
     * x-ms-original-file: 2024-12-30/PrivateEndpointConnectionList.json
     */
    /**
     * Sample code: Gets list of private endpoint connections on a server.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void getsListOfPrivateEndpointConnectionsOnAServer(
        com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.privateEndpointConnections()
            .listByServerWithResponse("Default", "test-svr", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkResources_Get

```java
/**
 * Samples for PrivateLinkResources Get.
 */
public final class PrivateLinkResourcesGetSamples {
    /*
     * x-ms-original-file: 2024-12-30/PrivateLinkResourcesGet.json
     */
    /**
     * Sample code: Gets a private link resource for MySQL.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void
        getsAPrivateLinkResourceForMySQL(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.privateLinkResources().getWithResponse("Default", "test-svr", "plr", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkResources_ListByServer

```java
/**
 * Samples for PrivateLinkResources ListByServer.
 */
public final class PrivateLinkResourcesListByServerSamples {
    /*
     * x-ms-original-file: 2024-12-30/PrivateLinkResourcesList.json
     */
    /**
     * Sample code: Gets private link resources for MySQL.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void
        getsPrivateLinkResourcesForMySQL(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.privateLinkResources().listByServer("Default", "test-svr", com.azure.core.util.Context.NONE);
    }
}
```

### Replicas_ListByServer

```java
/**
 * Samples for Replicas ListByServer.
 */
public final class ReplicasListByServerSamples {
    /*
     * x-ms-original-file: 2024-12-30/ReplicasListByServer.json
     */
    /**
     * Sample code: List replicas for a server.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void listReplicasForAServer(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.replicas().listByServer("TestGroup", "mysqltestserver", com.azure.core.util.Context.NONE);
    }
}
```

### Servers_Create

```java
import com.azure.resourcemanager.mysqlflexibleserver.models.Backup;
import com.azure.resourcemanager.mysqlflexibleserver.models.CreateMode;
import com.azure.resourcemanager.mysqlflexibleserver.models.DataEncryption;
import com.azure.resourcemanager.mysqlflexibleserver.models.DataEncryptionType;
import com.azure.resourcemanager.mysqlflexibleserver.models.EnableStatusEnum;
import com.azure.resourcemanager.mysqlflexibleserver.models.HighAvailability;
import com.azure.resourcemanager.mysqlflexibleserver.models.HighAvailabilityMode;
import com.azure.resourcemanager.mysqlflexibleserver.models.Identity;
import com.azure.resourcemanager.mysqlflexibleserver.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.mysqlflexibleserver.models.ServerVersion;
import com.azure.resourcemanager.mysqlflexibleserver.models.Sku;
import com.azure.resourcemanager.mysqlflexibleserver.models.SkuTier;
import com.azure.resourcemanager.mysqlflexibleserver.models.Storage;
import com.azure.resourcemanager.mysqlflexibleserver.models.StorageRedundancyEnum;
import com.azure.resourcemanager.mysqlflexibleserver.models.UserAssignedIdentity;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Servers Create.
 */
public final class ServersCreateSamples {
    /*
     * x-ms-original-file: 2024-12-30/ServerCreateReplica.json
     */
    /**
     * Sample code: Create a replica server.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void createAReplicaServer(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.servers()
            .define("replica-server")
            .withRegion("SoutheastAsia")
            .withExistingResourceGroup("testgr")
            .withCreateMode(CreateMode.REPLICA)
            .withSourceServerResourceId(
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/testgr/providers/Microsoft.DBforMySQL/flexibleServers/source-server")
            .create();
    }

    /*
     * x-ms-original-file: 2024-12-30/ServerCreateWithDatabasePort.json
     */
    /**
     * Sample code: Create a server with non-default database port.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void
        createAServerWithNonDefaultDatabasePort(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.servers()
            .define("mysqltestserver")
            .withRegion("southeastasia")
            .withExistingResourceGroup("testrg")
            .withTags(mapOf("num", "1"))
            .withSku(new Sku().withName("Standard_D2ds_v4").withTier(SkuTier.GENERAL_PURPOSE))
            .withAdministratorLogin("cloudsa")
            .withAdministratorLoginPassword("your_password")
            .withVersion(ServerVersion.FIVE_SEVEN)
            .withAvailabilityZone("1")
            .withCreateMode(CreateMode.DEFAULT)
            .withDatabasePort(8888)
            .withStorage(new Storage().withStorageSizeGB(100)
                .withIops(600)
                .withAutoGrow(EnableStatusEnum.DISABLED)
                .withStorageRedundancy(StorageRedundancyEnum.LOCAL_REDUNDANCY))
            .withBackup(new Backup().withBackupRetentionDays(7)
                .withBackupIntervalHours(24)
                .withGeoRedundantBackup(EnableStatusEnum.DISABLED))
            .withHighAvailability(
                new HighAvailability().withMode(HighAvailabilityMode.ZONE_REDUNDANT).withStandbyAvailabilityZone("3"))
            .create();
    }

    /*
     * x-ms-original-file: 2024-12-30/ServerCreate.json
     */
    /**
     * Sample code: Create a new server.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void createANewServer(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.servers()
            .define("mysqltestserver")
            .withRegion("southeastasia")
            .withExistingResourceGroup("testrg")
            .withTags(mapOf("num", "1"))
            .withSku(new Sku().withName("Standard_D2ds_v4").withTier(SkuTier.GENERAL_PURPOSE))
            .withAdministratorLogin("cloudsa")
            .withAdministratorLoginPassword("your_password")
            .withVersion(ServerVersion.FIVE_SEVEN)
            .withAvailabilityZone("1")
            .withCreateMode(CreateMode.DEFAULT)
            .withStorage(new Storage().withStorageSizeGB(100)
                .withIops(600)
                .withAutoGrow(EnableStatusEnum.DISABLED)
                .withStorageRedundancy(StorageRedundancyEnum.ZONE_REDUNDANCY))
            .withBackup(new Backup().withBackupRetentionDays(7)
                .withBackupIntervalHours(24)
                .withGeoRedundantBackup(EnableStatusEnum.DISABLED))
            .withHighAvailability(
                new HighAvailability().withMode(HighAvailabilityMode.ZONE_REDUNDANT).withStandbyAvailabilityZone("3"))
            .create();
    }

    /*
     * x-ms-original-file: 2024-12-30/ServerCreateWithBYOK.json
     */
    /**
     * Sample code: Create a server with byok.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void createAServerWithByok(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.servers()
            .define("mysqltestserver")
            .withRegion("southeastasia")
            .withExistingResourceGroup("testrg")
            .withTags(mapOf("num", "1"))
            .withIdentity(new Identity().withType(ManagedServiceIdentityType.USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/testrg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/test-identity",
                    new UserAssignedIdentity())))
            .withSku(new Sku().withName("Standard_D2ds_v4").withTier(SkuTier.GENERAL_PURPOSE))
            .withAdministratorLogin("cloudsa")
            .withAdministratorLoginPassword("your_password")
            .withVersion(ServerVersion.FIVE_SEVEN)
            .withAvailabilityZone("1")
            .withCreateMode(CreateMode.DEFAULT)
            .withDataEncryption(new DataEncryption().withPrimaryUserAssignedIdentityId(
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/testrg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/test-identity")
                .withPrimaryKeyUri("fakeTokenPlaceholder")
                .withGeoBackupUserAssignedIdentityId(
                    "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/testrg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/test-geo-identity")
                .withGeoBackupKeyUri("fakeTokenPlaceholder")
                .withType(DataEncryptionType.AZURE_KEY_VAULT))
            .withStorage(new Storage().withStorageSizeGB(100)
                .withIops(600)
                .withAutoGrow(EnableStatusEnum.DISABLED)
                .withStorageRedundancy(StorageRedundancyEnum.LOCAL_REDUNDANCY))
            .withBackup(new Backup().withBackupRetentionDays(7)
                .withBackupIntervalHours(24)
                .withGeoRedundantBackup(EnableStatusEnum.DISABLED))
            .withHighAvailability(
                new HighAvailability().withMode(HighAvailabilityMode.ZONE_REDUNDANT).withStandbyAvailabilityZone("3"))
            .create();
    }

    /*
     * x-ms-original-file: 2024-12-30/ServerCreateWithPointInTimeRestore.json
     */
    /**
     * Sample code: Create a server as a point in time restore.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void
        createAServerAsAPointInTimeRestore(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.servers()
            .define("targetserver")
            .withRegion("SoutheastAsia")
            .withExistingResourceGroup("TargetResourceGroup")
            .withTags(mapOf("num", "1"))
            .withSku(new Sku().withName("Standard_D14_v2").withTier(SkuTier.GENERAL_PURPOSE))
            .withCreateMode(CreateMode.POINT_IN_TIME_RESTORE)
            .withSourceServerResourceId(
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/SourceResourceGroup/providers/Microsoft.DBforMySQL/flexibleServers/sourceserver")
            .withRestorePointInTime(OffsetDateTime.parse("2021-06-24T00:00:37.467Z"))
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
/**
 * Samples for Servers Delete.
 */
public final class ServersDeleteSamples {
    /*
     * x-ms-original-file: 2024-12-30/ServerDelete.json
     */
    /**
     * Sample code: Delete a server.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void deleteAServer(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.servers().delete("TestGroup", "testserver", com.azure.core.util.Context.NONE);
    }
}
```

### Servers_DetachVNet

```java
import com.azure.resourcemanager.mysqlflexibleserver.models.EnableStatusEnum;
import com.azure.resourcemanager.mysqlflexibleserver.models.ServerDetachVNetParameter;

/**
 * Samples for Servers DetachVNet.
 */
public final class ServersDetachVNetSamples {
    /*
     * x-ms-original-file: 2024-12-30/ServerDetachVNet.json
     */
    /**
     * Sample code: Detach VNet on a server.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void detachVNetOnAServer(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.servers()
            .detachVNet("TestGroup", "testserver",
                new ServerDetachVNetParameter().withPublicNetworkAccess(EnableStatusEnum.ENABLED),
                com.azure.core.util.Context.NONE);
    }
}
```

### Servers_Failover

```java
/**
 * Samples for Servers Failover.
 */
public final class ServersFailoverSamples {
    /*
     * x-ms-original-file: 2024-12-30/ServerFailover.json
     */
    /**
     * Sample code: Restart a server.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void restartAServer(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.servers().failover("TestGroup", "testserver", com.azure.core.util.Context.NONE);
    }
}
```

### Servers_GetByResourceGroup

```java
/**
 * Samples for Servers GetByResourceGroup.
 */
public final class ServersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-12-30/ServerGetWithVnet.json
     */
    /**
     * Sample code: Get a server with vnet.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void getAServerWithVnet(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.servers().getByResourceGroupWithResponse("testrg", "mysqltestserver", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2024-12-30/ServerGet.json
     */
    /**
     * Sample code: Get a server.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void getAServer(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.servers().getByResourceGroupWithResponse("testrg", "mysqltestserver", com.azure.core.util.Context.NONE);
    }
}
```

### Servers_List

```java
/**
 * Samples for Servers List.
 */
public final class ServersListSamples {
    /*
     * x-ms-original-file: 2024-12-30/ServersList.json
     */
    /**
     * Sample code: List servers in a subscription.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void listServersInASubscription(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.servers().list(com.azure.core.util.Context.NONE);
    }
}
```

### Servers_ListByResourceGroup

```java
/**
 * Samples for Servers ListByResourceGroup.
 */
public final class ServersListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-12-30/ServersListByResourceGroup.json
     */
    /**
     * Sample code: List servers in a resource group.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void listServersInAResourceGroup(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.servers().listByResourceGroup("TestGroup", com.azure.core.util.Context.NONE);
    }
}
```

### Servers_ResetGtid

```java
import com.azure.resourcemanager.mysqlflexibleserver.models.ServerGtidSetParameter;

/**
 * Samples for Servers ResetGtid.
 */
public final class ServersResetGtidSamples {
    /*
     * x-ms-original-file: 2024-12-30/ServerResetGtid.json
     */
    /**
     * Sample code: Reset GTID on a server.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void resetGTIDOnAServer(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.servers()
            .resetGtid("TestGroup", "testserver",
                new ServerGtidSetParameter().withGtidSet("4aff5b51-97ba-11ed-a955-002248036acc:1-16"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Servers_Restart

```java
import com.azure.resourcemanager.mysqlflexibleserver.models.EnableStatusEnum;
import com.azure.resourcemanager.mysqlflexibleserver.models.ServerRestartParameter;

/**
 * Samples for Servers Restart.
 */
public final class ServersRestartSamples {
    /*
     * x-ms-original-file: 2024-12-30/ServerRestart.json
     */
    /**
     * Sample code: Restart a server.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void restartAServer(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.servers()
            .restart("TestGroup", "testserver",
                new ServerRestartParameter().withRestartWithFailover(EnableStatusEnum.ENABLED)
                    .withMaxFailoverSeconds(60),
                com.azure.core.util.Context.NONE);
    }
}
```

### Servers_Start

```java
/**
 * Samples for Servers Start.
 */
public final class ServersStartSamples {
    /*
     * x-ms-original-file: 2024-12-30/ServerStart.json
     */
    /**
     * Sample code: Start a server.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void startAServer(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.servers().start("TestGroup", "testserver", com.azure.core.util.Context.NONE);
    }
}
```

### Servers_Stop

```java
/**
 * Samples for Servers Stop.
 */
public final class ServersStopSamples {
    /*
     * x-ms-original-file: 2024-12-30/ServerStop.json
     */
    /**
     * Sample code: Stop a server.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void stopAServer(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.servers().stop("TestGroup", "testserver", com.azure.core.util.Context.NONE);
    }
}
```

### Servers_Update

```java
import com.azure.resourcemanager.mysqlflexibleserver.models.BatchOfMaintenance;
import com.azure.resourcemanager.mysqlflexibleserver.models.DataEncryption;
import com.azure.resourcemanager.mysqlflexibleserver.models.DataEncryptionType;
import com.azure.resourcemanager.mysqlflexibleserver.models.EnableStatusEnum;
import com.azure.resourcemanager.mysqlflexibleserver.models.Identity;
import com.azure.resourcemanager.mysqlflexibleserver.models.MaintenancePolicy;
import com.azure.resourcemanager.mysqlflexibleserver.models.MaintenanceWindow;
import com.azure.resourcemanager.mysqlflexibleserver.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.mysqlflexibleserver.models.Network;
import com.azure.resourcemanager.mysqlflexibleserver.models.PatchStrategy;
import com.azure.resourcemanager.mysqlflexibleserver.models.Server;
import com.azure.resourcemanager.mysqlflexibleserver.models.Storage;
import com.azure.resourcemanager.mysqlflexibleserver.models.StorageRedundancyEnum;
import com.azure.resourcemanager.mysqlflexibleserver.models.UserAssignedIdentity;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Servers Update.
 */
public final class ServersUpdateSamples {
    /*
     * x-ms-original-file: 2024-12-30/ServerUpdateWithCustomerMaintenanceWindow.json
     */
    /**
     * Sample code: Update server customer maintenance window.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void
        updateServerCustomerMaintenanceWindow(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        Server resource = manager.servers()
            .getByResourceGroupWithResponse("testrg", "mysqltestserver", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withMaintenanceWindow(new MaintenanceWindow().withCustomWindow("Enabled")
                .withStartHour(8)
                .withStartMinute(0)
                .withDayOfWeek(1)
                .withBatchOfMaintenance(BatchOfMaintenance.BATCH1))
            .apply();
    }

    /*
     * x-ms-original-file: 2024-12-30/MaintenancePolicyPatchOptInVirtualCanary.json
     */
    /**
     * Sample code: Update server to opt in virtual canary.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void
        updateServerToOptInVirtualCanary(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        Server resource = manager.servers()
            .getByResourceGroupWithResponse("testrg", "mysqltestserver", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withMaintenancePolicy(new MaintenancePolicy().withPatchStrategy(PatchStrategy.VIRTUAL_CANARY))
            .apply();
    }

    /*
     * x-ms-original-file: 2024-12-30/MaintenancePolicyPatchOptOutVirtualCanary.json
     */
    /**
     * Sample code: Update server to opt out virtual canary.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void
        updateServerToOptOutVirtualCanary(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        Server resource = manager.servers()
            .getByResourceGroupWithResponse("testrg", "mysqltestserver", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withMaintenancePolicy(new MaintenancePolicy().withPatchStrategy(PatchStrategy.fromString("Default")))
            .apply();
    }

    /*
     * x-ms-original-file: 2024-12-30/ServerUpdateWithBYOK.json
     */
    /**
     * Sample code: Update server with byok.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void updateServerWithByok(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        Server resource = manager.servers()
            .getByResourceGroupWithResponse("testrg", "mysqltestserver", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withIdentity(new Identity().withType(ManagedServiceIdentityType.USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/testrg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/test-identity",
                    new UserAssignedIdentity())))
            .withDataEncryption(new DataEncryption().withPrimaryUserAssignedIdentityId(
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/testrg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/test-identity")
                .withPrimaryKeyUri("fakeTokenPlaceholder")
                .withGeoBackupUserAssignedIdentityId(
                    "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/testrg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/test-geo-identity")
                .withGeoBackupKeyUri("fakeTokenPlaceholder")
                .withType(DataEncryptionType.AZURE_KEY_VAULT))
            .apply();
    }

    /*
     * x-ms-original-file: 2024-12-30/ServerUpdate.json
     */
    /**
     * Sample code: Update a server.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void updateAServer(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        Server resource = manager.servers()
            .getByResourceGroupWithResponse("testrg", "mysqltestserver", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withStorage(new Storage().withStorageSizeGB(30)
                .withIops(200)
                .withAutoGrow(EnableStatusEnum.DISABLED)
                .withAutoIoScaling(EnableStatusEnum.DISABLED)
                .withStorageRedundancy(StorageRedundancyEnum.LOCAL_REDUNDANCY))
            .withNetwork(new Network().withPublicNetworkAccess(EnableStatusEnum.DISABLED))
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

### Servers_ValidateEstimateHighAvailability

```java
import com.azure.resourcemanager.mysqlflexibleserver.fluent.models.HighAvailabilityValidationEstimationInner;

/**
 * Samples for Servers ValidateEstimateHighAvailability.
 */
public final class ServersValidateEstimateHighAvailabilitySamples {
    /*
     * x-ms-original-file: 2024-12-30/ServerValidateEstimateHighAvailability.json
     */
    /**
     * Sample code: Validate a validation and estimation of high availability.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void validateAValidationAndEstimationOfHighAvailability(
        com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.servers()
            .validateEstimateHighAvailabilityWithResponse("TestGroup", "testserver",
                new HighAvailabilityValidationEstimationInner().withExpectedStandbyAvailabilityZone("1"),
                com.azure.core.util.Context.NONE);
    }
}
```

### ServersMigration_CutoverMigration

```java
/**
 * Samples for ServersMigration CutoverMigration.
 */
public final class ServersMigrationCutoverMigrationSamples {
    /*
     * x-ms-original-file: 2024-12-30/CutoverMigration.json
     */
    /**
     * Sample code: Cutover migration for MySQL import.
     * 
     * @param manager Entry point to MySqlManager.
     */
    public static void
        cutoverMigrationForMySQLImport(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.serversMigrations().cutoverMigration("testrg", "mysqltestserver", com.azure.core.util.Context.NONE);
    }
}
```

