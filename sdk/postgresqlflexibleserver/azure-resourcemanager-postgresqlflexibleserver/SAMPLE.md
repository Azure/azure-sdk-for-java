# Code snippets and samples


## AdministratorsMicrosoftEntra

- [CreateOrUpdate](#administratorsmicrosoftentra_createorupdate)
- [Delete](#administratorsmicrosoftentra_delete)
- [Get](#administratorsmicrosoftentra_get)
- [ListByServer](#administratorsmicrosoftentra_listbyserver)

## AdvancedThreatProtectionSettings

- [Get](#advancedthreatprotectionsettings_get)
- [ListByServer](#advancedthreatprotectionsettings_listbyserver)

## BackupsAutomaticAndOnDemand

- [Create](#backupsautomaticandondemand_create)
- [Delete](#backupsautomaticandondemand_delete)
- [Get](#backupsautomaticandondemand_get)
- [ListByServer](#backupsautomaticandondemand_listbyserver)

## BackupsLongTermRetention

- [CheckPrerequisites](#backupslongtermretention_checkprerequisites)
- [Get](#backupslongtermretention_get)
- [ListByServer](#backupslongtermretention_listbyserver)
- [Start](#backupslongtermretention_start)

## CapabilitiesByLocation

- [List](#capabilitiesbylocation_list)

## CapabilitiesByServer

- [List](#capabilitiesbyserver_list)

## CapturedLogs

- [ListByServer](#capturedlogs_listbyserver)

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

## Migrations

- [Cancel](#migrations_cancel)
- [CheckNameAvailability](#migrations_checknameavailability)
- [Create](#migrations_create)
- [Get](#migrations_get)
- [ListByTargetServer](#migrations_listbytargetserver)
- [Update](#migrations_update)

## NameAvailability

- [CheckGlobally](#nameavailability_checkglobally)
- [CheckWithLocation](#nameavailability_checkwithlocation)

## Operations

- [List](#operations_list)

## PrivateDnsZoneSuffix

- [Get](#privatednszonesuffix_get)

## PrivateEndpointConnections

- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [ListByServer](#privateendpointconnections_listbyserver)
- [Update](#privateendpointconnections_update)

## PrivateLinkResources

- [Get](#privatelinkresources_get)
- [ListByServer](#privatelinkresources_listbyserver)

## QuotaUsages

- [List](#quotausages_list)

## Replicas

- [ListByServer](#replicas_listbyserver)

## Servers

- [CreateOrUpdate](#servers_createorupdate)
- [Delete](#servers_delete)
- [GetByResourceGroup](#servers_getbyresourcegroup)
- [List](#servers_list)
- [ListByResourceGroup](#servers_listbyresourcegroup)
- [Restart](#servers_restart)
- [Start](#servers_start)
- [Stop](#servers_stop)
- [Update](#servers_update)

## TuningOptionsOperation

- [Get](#tuningoptionsoperation_get)
- [ListByServer](#tuningoptionsoperation_listbyserver)
- [ListRecommendations](#tuningoptionsoperation_listrecommendations)

## VirtualEndpoints

- [Create](#virtualendpoints_create)
- [Delete](#virtualendpoints_delete)
- [Get](#virtualendpoints_get)
- [ListByServer](#virtualendpoints_listbyserver)
- [Update](#virtualendpoints_update)

## VirtualNetworkSubnetUsage

- [List](#virtualnetworksubnetusage_list)
### AdministratorsMicrosoftEntra_CreateOrUpdate

```java
import com.azure.resourcemanager.postgresqlflexibleserver.models.PrincipalType;

/**
 * Samples for AdministratorsMicrosoftEntra CreateOrUpdate.
 */
public final class AdministratorsMicrosoftEntraCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * AdministratorsMicrosoftEntraAdd.json
     */
    /**
     * Sample code: Add a server administrator associated to a Microsoft Entra principal.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void addAServerAdministratorAssociatedToAMicrosoftEntraPrincipal(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.administratorsMicrosoftEntras()
            .define("oooooooo-oooo-oooo-oooo-oooooooooooo")
            .withExistingFlexibleServer("exampleresourcegroup", "exampleserver")
            .withPrincipalType(PrincipalType.USER)
            .withPrincipalName("exampleuser@contoso.com")
            .withTenantId("tttttttt-tttt-tttt-tttt-tttttttttttt")
            .create();
    }
}
```

### AdministratorsMicrosoftEntra_Delete

```java
/**
 * Samples for AdministratorsMicrosoftEntra Delete.
 */
public final class AdministratorsMicrosoftEntraDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * AdministratorsMicrosoftEntraDelete.json
     */
    /**
     * Sample code: Delete a server administrator associated to a Microsoft Entra principal.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void deleteAServerAdministratorAssociatedToAMicrosoftEntraPrincipal(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.administratorsMicrosoftEntras()
            .delete("exampleresourcegroup", "exampleserver", "oooooooo-oooo-oooo-oooo-oooooooooooo",
                com.azure.core.util.Context.NONE);
    }
}
```

### AdministratorsMicrosoftEntra_Get

```java
/**
 * Samples for AdministratorsMicrosoftEntra Get.
 */
public final class AdministratorsMicrosoftEntraGetSamples {
    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * AdministratorsMicrosoftEntraGet.json
     */
    /**
     * Sample code: Get information about a server administrator associated to a Microsoft Entra principal.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void getInformationAboutAServerAdministratorAssociatedToAMicrosoftEntraPrincipal(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.administratorsMicrosoftEntras()
            .getWithResponse("exampleresourcegroup", "exampleserver", "oooooooo-oooo-oooo-oooo-oooooooooooo",
                com.azure.core.util.Context.NONE);
    }
}
```

### AdministratorsMicrosoftEntra_ListByServer

```java
/**
 * Samples for AdministratorsMicrosoftEntra ListByServer.
 */
public final class AdministratorsMicrosoftEntraListByServerSamples {
    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * AdministratorsMicrosoftEntraListByServer.json
     */
    /**
     * Sample code: List information about all server administrators associated to Microsoft Entra principals.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void listInformationAboutAllServerAdministratorsAssociatedToMicrosoftEntraPrincipals(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.administratorsMicrosoftEntras()
            .listByServer("exampleresourcegroup", "exampleserver", com.azure.core.util.Context.NONE);
    }
}
```

### AdvancedThreatProtectionSettings_Get

```java
import com.azure.resourcemanager.postgresqlflexibleserver.models.ThreatProtectionName;

/**
 * Samples for AdvancedThreatProtectionSettings Get.
 */
public final class AdvancedThreatProtectionSettingsGetSamples {
    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * AdvancedThreatProtectionSettingsGet.json
     */
    /**
     * Sample code: Get state of advanced threat protection settings for a server.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void getStateOfAdvancedThreatProtectionSettingsForAServer(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.advancedThreatProtectionSettings()
            .getWithResponse("exampleresourcegroup", "exampleserver", ThreatProtectionName.DEFAULT,
                com.azure.core.util.Context.NONE);
    }
}
```

### AdvancedThreatProtectionSettings_ListByServer

```java
/**
 * Samples for AdvancedThreatProtectionSettings ListByServer.
 */
public final class AdvancedThreatProtectionSettingsListByServerSamples {
    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * AdvancedThreatProtectionSettingsListByServer.json
     */
    /**
     * Sample code: List state of advanced threat protection settings for a server.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void listStateOfAdvancedThreatProtectionSettingsForAServer(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.advancedThreatProtectionSettings()
            .listByServer("exampleresourcegroup", "exampleserver", com.azure.core.util.Context.NONE);
    }
}
```

### BackupsAutomaticAndOnDemand_Create

```java
/**
 * Samples for BackupsAutomaticAndOnDemand Create.
 */
public final class BackupsAutomaticAndOnDemandCreateSamples {
    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * BackupsAutomaticAndOnDemandCreate.json
     */
    /**
     * Sample code: Create an on demand backup of a server.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void
        createAnOnDemandBackupOfAServer(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.backupsAutomaticAndOnDemands()
            .create("exampleresourcegroup", "exampleserver", "ondemandbackup-20250601T183022",
                com.azure.core.util.Context.NONE);
    }
}
```

### BackupsAutomaticAndOnDemand_Delete

```java
/**
 * Samples for BackupsAutomaticAndOnDemand Delete.
 */
public final class BackupsAutomaticAndOnDemandDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * BackupsAutomaticAndOnDemandDelete.json
     */
    /**
     * Sample code: Delete an on demand backup, given its name.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void deleteAnOnDemandBackupGivenItsName(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.backupsAutomaticAndOnDemands()
            .delete("exampleresourcegroup", "exampleserver", "ondemandbackup-20250601T183022",
                com.azure.core.util.Context.NONE);
    }
}
```

### BackupsAutomaticAndOnDemand_Get

```java
/**
 * Samples for BackupsAutomaticAndOnDemand Get.
 */
public final class BackupsAutomaticAndOnDemandGetSamples {
    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * BackupsAutomaticAndOnDemandGet.json
     */
    /**
     * Sample code: Get an on demand backup, given its name.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void
        getAnOnDemandBackupGivenItsName(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.backupsAutomaticAndOnDemands()
            .getWithResponse("exampleresourcegroup", "exampleserver", "backup_638830782181266873",
                com.azure.core.util.Context.NONE);
    }
}
```

### BackupsAutomaticAndOnDemand_ListByServer

```java
/**
 * Samples for BackupsAutomaticAndOnDemand ListByServer.
 */
public final class BackupsAutomaticAndOnDemandListByServerSamples {
    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * BackupsAutomaticAndOnDemandListByServer.json
     */
    /**
     * Sample code: List all available backups of a server.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void
        listAllAvailableBackupsOfAServer(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.backupsAutomaticAndOnDemands()
            .listByServer("exampleresourcegroup", "exampleserver", com.azure.core.util.Context.NONE);
    }
}
```

### BackupsLongTermRetention_CheckPrerequisites

```java
import com.azure.resourcemanager.postgresqlflexibleserver.models.BackupSettings;
import com.azure.resourcemanager.postgresqlflexibleserver.models.LtrPreBackupRequest;

/**
 * Samples for BackupsLongTermRetention CheckPrerequisites.
 */
public final class BackupsLongTermRetentionCheckPrerequisitesSamples {
    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * BackupsLongTermRetentionCheckPrerequisites.json
     */
    /**
     * Sample code: Perform all checks required for a long term retention backup operation to succeed.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void performAllChecksRequiredForALongTermRetentionBackupOperationToSucceed(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.backupsLongTermRetentions()
            .checkPrerequisitesWithResponse("exampleresourcegroup", "exampleserver",
                new LtrPreBackupRequest().withBackupSettings(new BackupSettings().withBackupName("exampleltrbackup")),
                com.azure.core.util.Context.NONE);
    }
}
```

### BackupsLongTermRetention_Get

```java
/**
 * Samples for BackupsLongTermRetention Get.
 */
public final class BackupsLongTermRetentionGetSamples {
    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * BackupsLongTermRetentionGet.json
     */
    /**
     * Sample code: Get the results of a long retention backup operation for a server.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void getTheResultsOfALongRetentionBackupOperationForAServer(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.backupsLongTermRetentions()
            .getWithResponse("exampleresourcegroup", "exampleserver", "exampleltrbackup",
                com.azure.core.util.Context.NONE);
    }
}
```

### BackupsLongTermRetention_ListByServer

```java
/**
 * Samples for BackupsLongTermRetention ListByServer.
 */
public final class BackupsLongTermRetentionListByServerSamples {
    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * BackupsLongTermRetentionListByServer.json
     */
    /**
     * Sample code: List the results of the long term retention backup operations for a server.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void listTheResultsOfTheLongTermRetentionBackupOperationsForAServer(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.backupsLongTermRetentions()
            .listByServer("exampleresourcegroup", "exampleserver", com.azure.core.util.Context.NONE);
    }
}
```

### BackupsLongTermRetention_Start

```java
import com.azure.resourcemanager.postgresqlflexibleserver.models.BackupSettings;
import com.azure.resourcemanager.postgresqlflexibleserver.models.BackupStoreDetails;
import com.azure.resourcemanager.postgresqlflexibleserver.models.BackupsLongTermRetentionRequest;
import java.util.Arrays;

/**
 * Samples for BackupsLongTermRetention Start.
 */
public final class BackupsLongTermRetentionStartSamples {
    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * BackupsLongTermRetentionStart.json
     */
    /**
     * Sample code: Initiate a long term retention backup.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void
        initiateALongTermRetentionBackup(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.backupsLongTermRetentions()
            .start("exampleresourcegroup", "exampleserver",
                new BackupsLongTermRetentionRequest()
                    .withBackupSettings(new BackupSettings().withBackupName("exampleltrbackup"))
                    .withTargetDetails(new BackupStoreDetails().withSasUriList(Arrays.asList("sasuri"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### CapabilitiesByLocation_List

```java
/**
 * Samples for CapabilitiesByLocation List.
 */
public final class CapabilitiesByLocationListSamples {
    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * CapabilitiesByLocationList.json
     */
    /**
     * Sample code: List the capabilities available in a given location for a specific subscription.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void listTheCapabilitiesAvailableInAGivenLocationForASpecificSubscription(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.capabilitiesByLocations().list("eastus", com.azure.core.util.Context.NONE);
    }
}
```

### CapabilitiesByServer_List

```java
/**
 * Samples for CapabilitiesByServer List.
 */
public final class CapabilitiesByServerListSamples {
    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * CapabilitiesByServerList.json
     */
    /**
     * Sample code: List the capabilities available for a given server.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void listTheCapabilitiesAvailableForAGivenServer(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.capabilitiesByServers().list("exampleresourcegroup", "exampleserver", com.azure.core.util.Context.NONE);
    }
}
```

### CapturedLogs_ListByServer

```java
/**
 * Samples for CapturedLogs ListByServer.
 */
public final class CapturedLogsListByServerSamples {
    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * CapturedLogsListByServer.json
     */
    /**
     * Sample code: List all captured logs for download in a server.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void listAllCapturedLogsForDownloadInAServer(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.capturedLogs().listByServer("exampleresourcegroup", "exampleserver", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/ConfigurationsGet.
     * json
     */
    /**
     * Sample code: Get information about a specific configuration (also known as server parameter) of a server.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void getInformationAboutASpecificConfigurationAlsoKnownAsServerParameterOfAServer(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.configurations()
            .getWithResponse("exampleresourcegroup", "exampleserver", "array_nulls", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * ConfigurationsListByServer.json
     */
    /**
     * Sample code: List all configurations (also known as server parameters) of a server.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void listAllConfigurationsAlsoKnownAsServerParametersOfAServer(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.configurations()
            .listByServer("exampleresourcegroup", "exampleserver", com.azure.core.util.Context.NONE);
    }
}
```

### Configurations_Put

```java
/**
 * Samples for Configurations Put.
 */
public final class ConfigurationsPutSamples {
    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * ConfigurationsUpdateUsingPut.json
     */
    /**
     * Sample code: Update, using Put verb, the value assigned to a specific modifiable configuration (also known as
     * server parameter) of a server.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void
        updateUsingPutVerbTheValueAssignedToASpecificModifiableConfigurationAlsoKnownAsServerParameterOfAServer(
            com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.configurations()
            .define("constraint_exclusion")
            .withExistingFlexibleServer("exampleresourcegroup", "exampleserver")
            .withValue("on")
            .withSource("user-override")
            .create();
    }
}
```

### Configurations_Update

```java
import com.azure.resourcemanager.postgresqlflexibleserver.models.Configuration;

/**
 * Samples for Configurations Update.
 */
public final class ConfigurationsUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * ConfigurationsUpdate.json
     */
    /**
     * Sample code: Update the value assigned to a specific modifiable configuration (also known as server parameter) of
     * a server.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void updateTheValueAssignedToASpecificModifiableConfigurationAlsoKnownAsServerParameterOfAServer(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        Configuration resource = manager.configurations()
            .getWithResponse("exampleresourcegroup", "exampleserver", "constraint_exclusion",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withValue("on").withSource("user-override").apply();
    }
}
```

### Databases_Create

```java
/**
 * Samples for Databases Create.
 */
public final class DatabasesCreateSamples {
    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/DatabasesCreate.
     * json
     */
    /**
     * Sample code: Create a database.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void createADatabase(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.databases()
            .define("exampledatabase")
            .withExistingFlexibleServer("exampleresourcegroup", "exampleserver")
            .withCharset("utf8")
            .withCollation("en_US.utf8")
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
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/DatabasesDelete.
     * json
     */
    /**
     * Sample code: Delete an existing database.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void
        deleteAnExistingDatabase(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.databases()
            .delete("exampleresourcegroup", "exampleserver", "exampledatabase", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/DatabasesGet.json
     */
    /**
     * Sample code: Get information about an existing database.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void getInformationAboutAnExistingDatabase(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.databases()
            .getWithResponse("exampleresourcegroup", "exampleserver", "exampledatabase",
                com.azure.core.util.Context.NONE);
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
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * DatabasesListByServer.json
     */
    /**
     * Sample code: List all databases in a server.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void
        listAllDatabasesInAServer(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.databases().listByServer("exampleresourcegroup", "exampleserver", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * FirewallRulesCreateOrUpdate.json
     */
    /**
     * Sample code: Create a new firewall rule or update an existing firewall rule.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void createANewFirewallRuleOrUpdateAnExistingFirewallRule(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.firewallRules()
            .define("examplefirewallrule")
            .withExistingFlexibleServer("exampleresourcegroup", "exampleserver")
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
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * FirewallRulesDelete.json
     */
    /**
     * Sample code: Delete an existing firewall rule.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void
        deleteAnExistingFirewallRule(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.firewallRules()
            .delete("exampleresourcegroup", "exampleserver", "examplefirewallrule", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/FirewallRulesGet.
     * json
     */
    /**
     * Sample code: Get information about a firewall rule in a server.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void getInformationAboutAFirewallRuleInAServer(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.firewallRules()
            .getWithResponse("exampleresourcegroup", "exampleserver", "examplefirewallrule",
                com.azure.core.util.Context.NONE);
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
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * FirewallRulesListByServer.json
     */
    /**
     * Sample code: List information about all firewall rules in a server.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void listInformationAboutAllFirewallRulesInAServer(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.firewallRules().listByServer("exampleresourcegroup", "exampleserver", com.azure.core.util.Context.NONE);
    }
}
```

### Migrations_Cancel

```java
/**
 * Samples for Migrations Cancel.
 */
public final class MigrationsCancelSamples {
    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/MigrationsCancel.
     * json
     */
    /**
     * Sample code: Cancel an active migration.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void
        cancelAnActiveMigration(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.migrations()
            .cancelWithResponse("exampleresourcegroup", "exampleserver", "examplemigration",
                com.azure.core.util.Context.NONE);
    }
}
```

### Migrations_CheckNameAvailability

```java
import com.azure.resourcemanager.postgresqlflexibleserver.fluent.models.MigrationNameAvailabilityInner;

/**
 * Samples for Migrations CheckNameAvailability.
 */
public final class MigrationsCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * MigrationsCheckNameAvailability.json
     */
    /**
     * Sample code: Check the validity and availability of the given name, to assign it to a new migration.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void checkTheValidityAndAvailabilityOfTheGivenNameToAssignItToANewMigration(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.migrations()
            .checkNameAvailabilityWithResponse("exampleresourcegroup", "exampleserver",
                new MigrationNameAvailabilityInner().withName("examplemigration")
                    .withType("Microsoft.DBforPostgreSQL/flexibleServers/migrations"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Migrations_Create

```java
import com.azure.resourcemanager.postgresqlflexibleserver.models.AdminCredentials;
import com.azure.resourcemanager.postgresqlflexibleserver.models.MigrateRolesAndPermissions;
import com.azure.resourcemanager.postgresqlflexibleserver.models.MigrationMode;
import com.azure.resourcemanager.postgresqlflexibleserver.models.MigrationOption;
import com.azure.resourcemanager.postgresqlflexibleserver.models.MigrationSecretParameters;
import com.azure.resourcemanager.postgresqlflexibleserver.models.OverwriteDatabasesOnTargetServer;
import com.azure.resourcemanager.postgresqlflexibleserver.models.SourceType;
import com.azure.resourcemanager.postgresqlflexibleserver.models.SslMode;
import java.util.Arrays;

/**
 * Samples for Migrations Create.
 */
public final class MigrationsCreateSamples {
    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * MigrationsCreateWithOtherUsers.json
     */
    /**
     * Sample code: Create a migration specifying user names.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void createAMigrationSpecifyingUserNames(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.migrations()
            .define("examplemigration")
            .withRegion("eastus")
            .withExistingFlexibleServer("exampleresourcegroup", "exampleserver")
            .withMigrationMode(MigrationMode.OFFLINE)
            .withSourceDbServerResourceId(
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/exampleresourcegroup/providers/Microsoft.DBForPostgreSql/servers/examplesource")
            .withSecretParameters(new MigrationSecretParameters()
                .withAdminCredentials(new AdminCredentials().withSourceServerPassword("fakeTokenPlaceholder")
                    .withTargetServerPassword("fakeTokenPlaceholder"))
                .withSourceServerUsername("newadmin@examplesource")
                .withTargetServerUsername("targetadmin"))
            .withDbsToMigrate(
                Arrays.asList("exampledatabase1", "exampledatabase2", "exampledatabase3", "exampledatabase4"))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * MigrationsCreateOtherSourceTypesValidateMigrate.json
     */
    /**
     * Sample code: Create a migration with other source type for validating and migrating.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void createAMigrationWithOtherSourceTypeForValidatingAndMigrating(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.migrations()
            .define("examplemigration")
            .withRegion("eastus")
            .withExistingFlexibleServer("exampleresourcegroup", "exampleserver")
            .withMigrationMode(MigrationMode.OFFLINE)
            .withMigrationOption(MigrationOption.VALIDATE_AND_MIGRATE)
            .withSourceType(SourceType.ON_PREMISES)
            .withSslMode(SslMode.PREFER)
            .withSourceDbServerResourceId("examplesource:5432@exampleuser")
            .withSecretParameters(new MigrationSecretParameters()
                .withAdminCredentials(new AdminCredentials().withSourceServerPassword("fakeTokenPlaceholder")
                    .withTargetServerPassword("fakeTokenPlaceholder")))
            .withDbsToMigrate(
                Arrays.asList("exampledatabase1", "exampledatabase2", "exampledatabase3", "exampledatabase4"))
            .withOverwriteDbsInTarget(OverwriteDatabasesOnTargetServer.TRUE)
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * MigrationsCreateWithPrivateEndpointServers.json
     */
    /**
     * Sample code: Create a migration with private endpoint.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void createAMigrationWithPrivateEndpoint(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.migrations()
            .define("examplemigration")
            .withRegion("eastus")
            .withExistingFlexibleServer("exampleresourcegroup", "exampleserver")
            .withMigrationInstanceResourceId(
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/exampleresourcegroup/providers/Microsoft.DBForPostgreSql/flexibleServers/examplesourcemigration")
            .withMigrationMode(MigrationMode.OFFLINE)
            .withSourceDbServerResourceId(
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/exampleresourcegroup/providers/Microsoft.DBForPostgreSql/servers/examplesource")
            .withSecretParameters(new MigrationSecretParameters()
                .withAdminCredentials(new AdminCredentials().withSourceServerPassword("fakeTokenPlaceholder")
                    .withTargetServerPassword("fakeTokenPlaceholder")))
            .withDbsToMigrate(
                Arrays.asList("exampledatabase1", "exampledatabase2", "exampledatabase3", "exampledatabase4"))
            .withOverwriteDbsInTarget(OverwriteDatabasesOnTargetServer.TRUE)
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/MigrationsCreate.
     * json
     */
    /**
     * Sample code: Create a migration.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void createAMigration(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.migrations()
            .define("examplemigration")
            .withRegion("eastus")
            .withExistingFlexibleServer("exampleresourcegroup", "exampleserver")
            .withMigrationMode(MigrationMode.OFFLINE)
            .withSourceDbServerResourceId(
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/exampleresourcegroup/providers/Microsoft.DBForPostgreSql/servers/examplesource")
            .withSecretParameters(new MigrationSecretParameters()
                .withAdminCredentials(new AdminCredentials().withSourceServerPassword("fakeTokenPlaceholder")
                    .withTargetServerPassword("fakeTokenPlaceholder")))
            .withDbsToMigrate(
                Arrays.asList("exampledatabase1", "exampledatabase2", "exampledatabase3", "exampledatabase4"))
            .withOverwriteDbsInTarget(OverwriteDatabasesOnTargetServer.TRUE)
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * MigrationsCreateWithRoles.json
     */
    /**
     * Sample code: Create a migration with roles.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void
        createAMigrationWithRoles(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.migrations()
            .define("examplemigration")
            .withRegion("eastus")
            .withExistingFlexibleServer("exampleresourcegroup", "exampleserver")
            .withMigrationMode(MigrationMode.OFFLINE)
            .withSourceDbServerResourceId(
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/exampleresourcegroup/providers/Microsoft.DBForPostgreSql/servers/examplesource")
            .withSecretParameters(new MigrationSecretParameters()
                .withAdminCredentials(new AdminCredentials().withSourceServerPassword("fakeTokenPlaceholder")
                    .withTargetServerPassword("fakeTokenPlaceholder")))
            .withDbsToMigrate(
                Arrays.asList("exampledatabase1", "exampledatabase2", "exampledatabase3", "exampledatabase4"))
            .withOverwriteDbsInTarget(OverwriteDatabasesOnTargetServer.TRUE)
            .withMigrateRoles(MigrateRolesAndPermissions.TRUE)
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * MigrationsCreateWithFullyQualifiedDomainName.json
     */
    /**
     * Sample code: Create a migration with fully qualified domain names for source and target servers.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void createAMigrationWithFullyQualifiedDomainNamesForSourceAndTargetServers(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.migrations()
            .define("examplemigration")
            .withRegion("eastus")
            .withExistingFlexibleServer("exampleresourcegroup", "exampleserver")
            .withMigrationMode(MigrationMode.OFFLINE)
            .withSourceDbServerResourceId(
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/exampleresourcegroup/providers/Microsoft.DBForPostgreSql/servers/examplesource")
            .withSourceDbServerFullyQualifiedDomainName("examplesource.contoso.com")
            .withTargetDbServerFullyQualifiedDomainName("exampletarget.contoso.com")
            .withSecretParameters(new MigrationSecretParameters()
                .withAdminCredentials(new AdminCredentials().withSourceServerPassword("fakeTokenPlaceholder")
                    .withTargetServerPassword("fakeTokenPlaceholder")))
            .withDbsToMigrate(
                Arrays.asList("exampledatabase1", "exampledatabase2", "exampledatabase3", "exampledatabase4"))
            .withOverwriteDbsInTarget(OverwriteDatabasesOnTargetServer.TRUE)
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * MigrationsCreateValidateOnly.json
     */
    /**
     * Sample code: Create a migration for validating only.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void createAMigrationForValidatingOnly(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.migrations()
            .define("examplemigration")
            .withRegion("eastus")
            .withExistingFlexibleServer("exampleresourcegroup", "exampleserver")
            .withMigrationMode(MigrationMode.OFFLINE)
            .withMigrationOption(MigrationOption.VALIDATE)
            .withSourceDbServerResourceId(
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/exampleresourcegroup/providers/Microsoft.DBForPostgreSql/servers/examplesource")
            .withSecretParameters(new MigrationSecretParameters()
                .withAdminCredentials(new AdminCredentials().withSourceServerPassword("fakeTokenPlaceholder")
                    .withTargetServerPassword("fakeTokenPlaceholder")))
            .withDbsToMigrate(
                Arrays.asList("exampledatabase1", "exampledatabase2", "exampledatabase3", "exampledatabase4"))
            .withOverwriteDbsInTarget(OverwriteDatabasesOnTargetServer.TRUE)
            .create();
    }
}
```

### Migrations_Get

```java
/**
 * Samples for Migrations Get.
 */
public final class MigrationsGetSamples {
    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * MigrationsGetMigrationWithSuccessfulValidationOnly.json
     */
    /**
     * Sample code: Get information about a migration with successful validation only.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void getInformationAboutAMigrationWithSuccessfulValidationOnly(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.migrations()
            .getWithResponse("exampleresourcegroup", "exampleserver", "examplemigration",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * MigrationsGetMigrationWithSuccessfulValidationAndMigration.json
     */
    /**
     * Sample code: Get information about a migration with successful validation and successful migration.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void getInformationAboutAMigrationWithSuccessfulValidationAndSuccessfulMigration(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.migrations()
            .getWithResponse("exampleresourcegroup", "exampleserver", "examplemigration",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * MigrationsGetMigrationWithSuccessfulValidationButMigrationFailure.json
     */
    /**
     * Sample code: Get information about a migration with successful validation but failed migration.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void getInformationAboutAMigrationWithSuccessfulValidationButFailedMigration(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.migrations()
            .getWithResponse("exampleresourcegroup", "exampleserver", "examplemigration",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * MigrationsGetMigrationWithValidationFailures.json
     */
    /**
     * Sample code: Get information about a migration with validation failures.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void getInformationAboutAMigrationWithValidationFailures(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.migrations()
            .getWithResponse("exampleresourcegroup", "exampleserver", "examplemigration",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/MigrationsGet.json
     */
    /**
     * Sample code: Get information about a migration.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void
        getInformationAboutAMigration(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.migrations()
            .getWithResponse("exampleresourcegroup", "exampleserver", "examplemigration",
                com.azure.core.util.Context.NONE);
    }
}
```

### Migrations_ListByTargetServer

```java
import com.azure.resourcemanager.postgresqlflexibleserver.models.MigrationListFilter;

/**
 * Samples for Migrations ListByTargetServer.
 */
public final class MigrationsListByTargetServerSamples {
    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * MigrationsListByTargetServer.json
     */
    /**
     * Sample code: List all migrations of a target flexible server.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void listAllMigrationsOfATargetFlexibleServer(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.migrations()
            .listByTargetServer("exampleresourcegroup", "exampleserver", MigrationListFilter.ALL,
                com.azure.core.util.Context.NONE);
    }
}
```

### Migrations_Update

```java
import com.azure.resourcemanager.postgresqlflexibleserver.models.LogicalReplicationOnSourceServer;
import com.azure.resourcemanager.postgresqlflexibleserver.models.Migration;

/**
 * Samples for Migrations Update.
 */
public final class MigrationsUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/MigrationsUpdate.
     * json
     */
    /**
     * Sample code: Update an existing migration.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void
        updateAnExistingMigration(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        Migration resource = manager.migrations()
            .getWithResponse("exampleresourcegroup", "exampleserver", "examplemigration",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withSetupLogicalReplicationOnSourceDbIfNeeded(LogicalReplicationOnSourceServer.TRUE).apply();
    }
}
```

### NameAvailability_CheckGlobally

```java
import com.azure.resourcemanager.postgresqlflexibleserver.models.CheckNameAvailabilityRequest;

/**
 * Samples for NameAvailability CheckGlobally.
 */
public final class NameAvailabilityCheckGloballySamples {
    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * NameAvailabilityCheckGlobally.json
     */
    /**
     * Sample code: Check the validity and availability of the given name, to assign it to a new server or to use it as
     * the base name of a new pair of virtual endpoints.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void
        checkTheValidityAndAvailabilityOfTheGivenNameToAssignItToANewServerOrToUseItAsTheBaseNameOfANewPairOfVirtualEndpoints(
            com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.nameAvailabilities()
            .checkGloballyWithResponse(new CheckNameAvailabilityRequest().withName("exampleserver")
                .withType("Microsoft.DBforPostgreSQL/flexibleServers"), com.azure.core.util.Context.NONE);
    }
}
```

### NameAvailability_CheckWithLocation

```java
import com.azure.resourcemanager.postgresqlflexibleserver.models.CheckNameAvailabilityRequest;

/**
 * Samples for NameAvailability CheckWithLocation.
 */
public final class NameAvailabilityCheckWithLocationSamples {
    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * NameAvailabilityCheckWithLocation.json
     */
    /**
     * Sample code: Check the validity and availability of the given name, in the given location, to assign it to a new
     * server or to use it as the base name of a new pair of virtual endpoints.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void
        checkTheValidityAndAvailabilityOfTheGivenNameInTheGivenLocationToAssignItToANewServerOrToUseItAsTheBaseNameOfANewPairOfVirtualEndpoints(
            com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.nameAvailabilities()
            .checkWithLocationWithResponse("eastus", new CheckNameAvailabilityRequest().withName("exampleserver")
                .withType("Microsoft.DBforPostgreSQL/flexibleServers"), com.azure.core.util.Context.NONE);
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
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/OperationsList.
     * json
     */
    /**
     * Sample code: List all available REST API operations.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void listAllAvailableRESTAPIOperations(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### PrivateDnsZoneSuffix_Get

```java
/**
 * Samples for PrivateDnsZoneSuffix Get.
 */
public final class PrivateDnsZoneSuffixGetSamples {
    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * PrivateDnsZoneSuffixGet.json
     */
    /**
     * Sample code: Get the private DNS suffix.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void
        getThePrivateDNSSuffix(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.privateDnsZoneSuffixes().getWithResponse(com.azure.core.util.Context.NONE);
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
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * PrivateEndpointConnectionsDelete.json
     */
    /**
     * Sample code: Delete a private endpoint connection.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void
        deleteAPrivateEndpointConnection(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.privateEndpointConnections()
            .delete("exampleresourcegroup", "exampleserver",
                "private-endpoint-connection-name.1fa229cd-bf3f-47f0-8c49-afb36723997e",
                com.azure.core.util.Context.NONE);
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
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * PrivateEndpointConnectionsGet.json
     */
    /**
     * Sample code: Get a private endpoint connection.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void
        getAPrivateEndpointConnection(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.privateEndpointConnections()
            .getWithResponse("exampleresourcegroup", "exampleserver",
                "private-endpoint-connection-name.1fa229cd-bf3f-47f0-8c49-afb36723997e",
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
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * PrivateEndpointConnectionsList.json
     */
    /**
     * Sample code: List all private endpoint connections on a server.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void listAllPrivateEndpointConnectionsOnAServer(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.privateEndpointConnections()
            .listByServer("exampleresourcegroup", "exampleserver", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_Update

```java
import com.azure.resourcemanager.postgresqlflexibleserver.fluent.models.PrivateEndpointConnectionInner;
import com.azure.resourcemanager.postgresqlflexibleserver.models.PrivateEndpointServiceConnectionStatus;
import com.azure.resourcemanager.postgresqlflexibleserver.models.PrivateLinkServiceConnectionState;

/**
 * Samples for PrivateEndpointConnections Update.
 */
public final class PrivateEndpointConnectionsUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * PrivateEndpointConnectionsUpdate.json
     */
    /**
     * Sample code: Approve or reject a private endpoint connection.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void approveOrRejectAPrivateEndpointConnection(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.privateEndpointConnections()
            .update("exampleresourcegroup", "exampleserver",
                "private-endpoint-connection-name.1fa229cd-bf3f-47f0-8c49-afb36723997e",
                new PrivateEndpointConnectionInner().withPrivateLinkServiceConnectionState(
                    new PrivateLinkServiceConnectionState().withStatus(PrivateEndpointServiceConnectionStatus.APPROVED)
                        .withDescription("Approved by johndoe@contoso.com")),
                com.azure.core.util.Context.NONE);
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
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * PrivateLinkResourcesGet.json
     */
    /**
     * Sample code: Gets a private link resource for PostgreSQL.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void getsAPrivateLinkResourceForPostgreSQL(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.privateLinkResources()
            .getWithResponse("exampleresourcegroup", "exampleserver", "exampleprivatelink",
                com.azure.core.util.Context.NONE);
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
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * PrivateLinkResourcesList.json
     */
    /**
     * Sample code: Gets private link resources for PostgreSQL.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void getsPrivateLinkResourcesForPostgreSQL(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.privateLinkResources()
            .listByServer("exampleresourcegroup", "exampleserver", com.azure.core.util.Context.NONE);
    }
}
```

### QuotaUsages_List

```java
/**
 * Samples for QuotaUsages List.
 */
public final class QuotaUsagesListSamples {
    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * QuotaUsagesForFlexibleServers.json
     */
    /**
     * Sample code: List of quota usages for servers.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void
        listOfQuotaUsagesForServers(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.quotaUsages().list("eastus", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * ReplicasListByServer.json
     */
    /**
     * Sample code: List all read replicas of a server.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void
        listAllReadReplicasOfAServer(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.replicas().listByServer("exampleresourcegroup", "exampleserver", com.azure.core.util.Context.NONE);
    }
}
```

### Servers_CreateOrUpdate

```java
import com.azure.resourcemanager.postgresqlflexibleserver.models.AuthConfig;
import com.azure.resourcemanager.postgresqlflexibleserver.models.AzureManagedDiskPerformanceTier;
import com.azure.resourcemanager.postgresqlflexibleserver.models.Backup;
import com.azure.resourcemanager.postgresqlflexibleserver.models.Cluster;
import com.azure.resourcemanager.postgresqlflexibleserver.models.CreateMode;
import com.azure.resourcemanager.postgresqlflexibleserver.models.DataEncryption;
import com.azure.resourcemanager.postgresqlflexibleserver.models.DataEncryptionType;
import com.azure.resourcemanager.postgresqlflexibleserver.models.GeographicallyRedundantBackup;
import com.azure.resourcemanager.postgresqlflexibleserver.models.HighAvailability;
import com.azure.resourcemanager.postgresqlflexibleserver.models.HighAvailabilityMode;
import com.azure.resourcemanager.postgresqlflexibleserver.models.IdentityType;
import com.azure.resourcemanager.postgresqlflexibleserver.models.MicrosoftEntraAuth;
import com.azure.resourcemanager.postgresqlflexibleserver.models.Network;
import com.azure.resourcemanager.postgresqlflexibleserver.models.PasswordBasedAuth;
import com.azure.resourcemanager.postgresqlflexibleserver.models.PostgresMajorVersion;
import com.azure.resourcemanager.postgresqlflexibleserver.models.ServerPublicNetworkAccessState;
import com.azure.resourcemanager.postgresqlflexibleserver.models.Sku;
import com.azure.resourcemanager.postgresqlflexibleserver.models.SkuTier;
import com.azure.resourcemanager.postgresqlflexibleserver.models.Storage;
import com.azure.resourcemanager.postgresqlflexibleserver.models.StorageAutoGrow;
import com.azure.resourcemanager.postgresqlflexibleserver.models.UserAssignedIdentity;
import com.azure.resourcemanager.postgresqlflexibleserver.models.UserIdentity;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Servers CreateOrUpdate.
 */
public final class ServersCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * ServersCreateReviveDropped.json
     */
    /**
     * Sample code: Create a new server using a backup of a server that was deleted or dropped recently.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void createANewServerUsingABackupOfAServerThatWasDeletedOrDroppedRecently(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.servers()
            .define("exampleserver")
            .withRegion("eastus")
            .withExistingResourceGroup("exampleresourcegroup")
            .withSourceServerResourceId(
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/exampleresourcegroup/providers/Microsoft.DBforPostgreSQL/flexibleServers/exampledeletedserver")
            .withPointInTimeUtc(OffsetDateTime.parse("2025-06-01T18:30:22.123456Z"))
            .withCreateMode(CreateMode.REVIVE_DROPPED)
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * ServersCreateInMicrosoftOwnedVirtualNetworkWithZoneRedundantHighAvailability.json
     */
    /**
     * Sample code: Create a new server in Microsoft owned virtual network with zone redundant high availability.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void createANewServerInMicrosoftOwnedVirtualNetworkWithZoneRedundantHighAvailability(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.servers()
            .define("exampleserver")
            .withRegion("eastus")
            .withExistingResourceGroup("exampleresourcegroup")
            .withTags(mapOf("InCustomerVnet", "false", "InMicrosoftVnet", "true"))
            .withSku(new Sku().withName("Standard_D4ds_v5").withTier(SkuTier.GENERAL_PURPOSE))
            .withAdministratorLogin("exampleadministratorlogin")
            .withAdministratorLoginPassword("examplepassword")
            .withVersion(PostgresMajorVersion.ONE_SEVEN)
            .withStorage(new Storage().withStorageSizeGB(512)
                .withAutoGrow(StorageAutoGrow.DISABLED)
                .withTier(AzureManagedDiskPerformanceTier.P20))
            .withBackup(
                new Backup().withBackupRetentionDays(7).withGeoRedundantBackup(GeographicallyRedundantBackup.ENABLED))
            .withNetwork(new Network().withPublicNetworkAccess(ServerPublicNetworkAccessState.ENABLED))
            .withHighAvailability(new HighAvailability().withMode(HighAvailabilityMode.ZONE_REDUNDANT))
            .withAvailabilityZone("1")
            .withCreateMode(CreateMode.CREATE)
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * ServersCreateGeoRestoreWithDataEncryptionEnabledAutoUpdate.json
     */
    /**
     * Sample code: Create a new server using a restore of a geographically redundant backup of an existing server, with
     * data encryption based on customer managed key with automatic key version update.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void
        createANewServerUsingARestoreOfAGeographicallyRedundantBackupOfAnExistingServerWithDataEncryptionBasedOnCustomerManagedKeyWithAutomaticKeyVersionUpdate(
            com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.servers()
            .define("exampleserver")
            .withRegion("eastus")
            .withExistingResourceGroup("exampleresourcegroup")
            .withIdentity(new UserAssignedIdentity().withUserAssignedIdentities(mapOf(
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/exampleresourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/examplegeoredundantidentity",
                new UserIdentity(),
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/exampleresourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/exampleprimaryidentity",
                new UserIdentity())).withType(IdentityType.USER_ASSIGNED))
            .withDataEncryption(new DataEncryption().withPrimaryKeyUri("fakeTokenPlaceholder")
                .withPrimaryUserAssignedIdentityId(
                    "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/exampleresourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/exampleprimaryidentity")
                .withGeoBackupKeyUri("fakeTokenPlaceholder")
                .withGeoBackupUserAssignedIdentityId(
                    "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/exampleresourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/examplegeoredundantidentity")
                .withType(DataEncryptionType.AZURE_KEY_VAULT))
            .withSourceServerResourceId(
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/exampleresourcegroup/providers/Microsoft.DBforPostgreSQL/flexibleServers/examplesourceserver")
            .withPointInTimeUtc(OffsetDateTime.parse("2025-06-01T18:35:22.123456Z"))
            .withCreateMode(CreateMode.GEO_RESTORE)
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * ServersCreateWithDataEncryptionEnabled.json
     */
    /**
     * Sample code: Create a new server with data encryption based on customer managed key.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void createANewServerWithDataEncryptionBasedOnCustomerManagedKey(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.servers()
            .define("exampleserver")
            .withRegion("eastus")
            .withExistingResourceGroup("exampleresourcegroup")
            .withSku(new Sku().withName("Standard_D4ds_v5").withTier(SkuTier.GENERAL_PURPOSE))
            .withIdentity(new UserAssignedIdentity().withUserAssignedIdentities(mapOf(
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/exampleresourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/exampleprimaryidentity",
                new UserIdentity())).withType(IdentityType.USER_ASSIGNED))
            .withAdministratorLogin("exampleadministratorlogin")
            .withAdministratorLoginPassword("examplepassword")
            .withVersion(PostgresMajorVersion.ONE_SEVEN)
            .withStorage(new Storage().withStorageSizeGB(512)
                .withAutoGrow(StorageAutoGrow.DISABLED)
                .withTier(AzureManagedDiskPerformanceTier.P20))
            .withDataEncryption(new DataEncryption().withPrimaryKeyUri("fakeTokenPlaceholder")
                .withPrimaryUserAssignedIdentityId(
                    "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/exampleresourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/exampleprimaryidentity")
                .withGeoBackupKeyUri("fakeTokenPlaceholder")
                .withGeoBackupUserAssignedIdentityId("")
                .withType(DataEncryptionType.AZURE_KEY_VAULT))
            .withBackup(
                new Backup().withBackupRetentionDays(7).withGeoRedundantBackup(GeographicallyRedundantBackup.DISABLED))
            .withNetwork(new Network().withDelegatedSubnetResourceId(
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/exampleresourcegroup/providers/Microsoft.Network/virtualNetworks/examplevirtualnetwork/subnets/examplesubnet")
                .withPrivateDnsZoneArmResourceId(
                    "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourcegroups/exampleresourcegroup/providers/Microsoft.Network/privateDnsZones/exampleprivatednszone.postgres.database.azure.com"))
            .withHighAvailability(new HighAvailability().withMode(HighAvailabilityMode.ZONE_REDUNDANT))
            .withAvailabilityZone("1")
            .withCreateMode(CreateMode.CREATE)
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * ServersCreateGeoRestoreWithDataEncryptionEnabled.json
     */
    /**
     * Sample code: Create a new server using a restore of a geographically redundant backup of an existing server, with
     * data encryption based on customer managed key.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void
        createANewServerUsingARestoreOfAGeographicallyRedundantBackupOfAnExistingServerWithDataEncryptionBasedOnCustomerManagedKey(
            com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.servers()
            .define("exampleserver")
            .withRegion("eastus")
            .withExistingResourceGroup("exampleresourcegroup")
            .withIdentity(new UserAssignedIdentity().withUserAssignedIdentities(mapOf(
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/exampleresourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/examplegeoredundantidentity",
                new UserIdentity(),
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/exampleresourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/exampleprimaryidentity",
                new UserIdentity())).withType(IdentityType.USER_ASSIGNED))
            .withDataEncryption(new DataEncryption().withPrimaryKeyUri("fakeTokenPlaceholder")
                .withPrimaryUserAssignedIdentityId(
                    "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/exampleresourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/exampleprimaryidentity")
                .withGeoBackupKeyUri("fakeTokenPlaceholder")
                .withGeoBackupUserAssignedIdentityId(
                    "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/exampleresourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/examplegeoredundantidentity")
                .withType(DataEncryptionType.AZURE_KEY_VAULT))
            .withSourceServerResourceId(
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/exampleresourcegroup/providers/Microsoft.DBforPostgreSQL/flexibleServers/examplesourceserver")
            .withPointInTimeUtc(OffsetDateTime.parse("2025-06-01T18:35:22.123456Z"))
            .withCreateMode(CreateMode.GEO_RESTORE)
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * ServersCreateReplica.json
     */
    /**
     * Sample code: Create a read replica of an existing server.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void createAReadReplicaOfAnExistingServer(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.servers()
            .define("exampleserver")
            .withRegion("eastus")
            .withExistingResourceGroup("exampleresourcegroup")
            .withIdentity(new UserAssignedIdentity().withUserAssignedIdentities(mapOf(
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/exampleresourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/exampleprimaryidentity",
                new UserIdentity())).withType(IdentityType.USER_ASSIGNED))
            .withDataEncryption(new DataEncryption().withPrimaryKeyUri("fakeTokenPlaceholder")
                .withPrimaryUserAssignedIdentityId(
                    "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/exampleresourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/exampleprimaryidentity")
                .withGeoBackupKeyUri("fakeTokenPlaceholder")
                .withGeoBackupUserAssignedIdentityId("")
                .withType(DataEncryptionType.AZURE_KEY_VAULT))
            .withSourceServerResourceId(
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/exampleresourcegroup/providers/Microsoft.DBforPostgreSQL/flexibleServers/examplesourceserver")
            .withPointInTimeUtc(OffsetDateTime.parse("2025-06-01T18:35:22.123456Z"))
            .withCreateMode(CreateMode.REPLICA)
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * ServersCreateInYourOwnVirtualNetworkWithSameZoneHighAvailability.json
     */
    /**
     * Sample code: Create a new server in your own virtual network with same zone high availability.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void createANewServerInYourOwnVirtualNetworkWithSameZoneHighAvailability(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.servers()
            .define("exampleserver")
            .withRegion("eastus")
            .withExistingResourceGroup("exampleresourcegroup")
            .withTags(mapOf("InCustomerVnet", "true", "InMicrosoftVnet", "false"))
            .withSku(new Sku().withName("Standard_D4ds_v5").withTier(SkuTier.GENERAL_PURPOSE))
            .withAdministratorLogin("exampleadministratorlogin")
            .withAdministratorLoginPassword("examplepassword")
            .withVersion(PostgresMajorVersion.ONE_SEVEN)
            .withStorage(new Storage().withStorageSizeGB(512)
                .withAutoGrow(StorageAutoGrow.DISABLED)
                .withTier(AzureManagedDiskPerformanceTier.P20))
            .withBackup(
                new Backup().withBackupRetentionDays(7).withGeoRedundantBackup(GeographicallyRedundantBackup.ENABLED))
            .withNetwork(new Network().withDelegatedSubnetResourceId(
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/exampleresourcegroup/providers/Microsoft.Network/virtualNetworks/examplevirtualnetwork/subnets/examplesubnet")
                .withPrivateDnsZoneArmResourceId(
                    "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/exampleresourcegroup/providers/Microsoft.Network/privateDnsZones/exampleprivatednszone.private.postgres.database"))
            .withHighAvailability(new HighAvailability().withMode(HighAvailabilityMode.SAME_ZONE))
            .withAvailabilityZone("1")
            .withCreateMode(CreateMode.CREATE)
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * ServersClusterCreate.json
     */
    /**
     * Sample code: Create a new elastic cluster.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void
        createANewElasticCluster(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.servers()
            .define("exampleserver")
            .withRegion("eastus")
            .withExistingResourceGroup("exampleresourcegroup")
            .withSku(new Sku().withName("Standard_D4ds_v5").withTier(SkuTier.GENERAL_PURPOSE))
            .withAdministratorLogin("examplelogin")
            .withAdministratorLoginPassword("examplepassword")
            .withVersion(PostgresMajorVersion.ONE_SIX)
            .withStorage(new Storage().withStorageSizeGB(256)
                .withAutoGrow(StorageAutoGrow.DISABLED)
                .withTier(AzureManagedDiskPerformanceTier.P15))
            .withBackup(
                new Backup().withBackupRetentionDays(7).withGeoRedundantBackup(GeographicallyRedundantBackup.DISABLED))
            .withNetwork(new Network().withPublicNetworkAccess(ServerPublicNetworkAccessState.DISABLED))
            .withHighAvailability(new HighAvailability().withMode(HighAvailabilityMode.fromString("Disabled")))
            .withCreateMode(CreateMode.CREATE)
            .withCluster(new Cluster().withClusterSize(2).withDefaultDatabaseName("clusterdb"))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * ServersCreatePointInTimeRestore.json
     */
    /**
     * Sample code: Create a new server using a point in time restore of a backup of an existing server.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void createANewServerUsingAPointInTimeRestoreOfABackupOfAnExistingServer(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.servers()
            .define("exampleserver")
            .withRegion("eastus")
            .withExistingResourceGroup("exampleresourcegroup")
            .withSourceServerResourceId(
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/exampleresourcegroup/providers/Microsoft.DBforPostgreSQL/flexibleServers/examplesourceserver")
            .withPointInTimeUtc(OffsetDateTime.parse("2025-06-01T18:35:22.123456Z"))
            .withCreateMode(CreateMode.POINT_IN_TIME_RESTORE)
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * ServersCreateWithMicrosoftEntraEnabledInYourOwnVirtualNetworkWithoutHighAvailability.json
     */
    /**
     * Sample code: Create a new server with Microsoft Entra authentication enabled in your own virtual network and
     * without high availability.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void
        createANewServerWithMicrosoftEntraAuthenticationEnabledInYourOwnVirtualNetworkAndWithoutHighAvailability(
            com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.servers()
            .define("exampleserver")
            .withRegion("eastus")
            .withExistingResourceGroup("exampleresourcegroup")
            .withSku(new Sku().withName("Standard_D4ds_v5").withTier(SkuTier.GENERAL_PURPOSE))
            .withAdministratorLogin("exampleadministratorlogin")
            .withAdministratorLoginPassword("examplepassword")
            .withVersion(PostgresMajorVersion.ONE_SEVEN)
            .withStorage(new Storage().withStorageSizeGB(512)
                .withAutoGrow(StorageAutoGrow.DISABLED)
                .withTier(AzureManagedDiskPerformanceTier.P20))
            .withAuthConfig(new AuthConfig().withActiveDirectoryAuth(MicrosoftEntraAuth.ENABLED)
                .withPasswordAuth(PasswordBasedAuth.ENABLED)
                .withTenantId("tttttt-tttt-tttt-tttt-tttttttttttt"))
            .withDataEncryption(new DataEncryption().withType(DataEncryptionType.SYSTEM_MANAGED))
            .withBackup(
                new Backup().withBackupRetentionDays(7).withGeoRedundantBackup(GeographicallyRedundantBackup.DISABLED))
            .withNetwork(new Network().withDelegatedSubnetResourceId(
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/exampleresourcegroup/providers/Microsoft.Network/virtualNetworks/examplevirtualnetwork/subnets/examplesubnet")
                .withPrivateDnsZoneArmResourceId(
                    "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourcegroups/exampleresourcegroup/providers/Microsoft.Network/privateDnsZones/exampleprivatednszone.postgres.database.azure.com"))
            .withHighAvailability(new HighAvailability().withMode(HighAvailabilityMode.fromString("Disabled")))
            .withAvailabilityZone("1")
            .withCreateMode(CreateMode.CREATE)
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * ServersCreateWithDataEncryptionEnabledAutoUpdate.json
     */
    /**
     * Sample code: Create a new server with data encryption based on customer managed key with automatic key version
     * update.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void createANewServerWithDataEncryptionBasedOnCustomerManagedKeyWithAutomaticKeyVersionUpdate(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.servers()
            .define("exampleserver")
            .withRegion("eastus")
            .withExistingResourceGroup("exampleresourcegroup")
            .withSku(new Sku().withName("Standard_D4ds_v5").withTier(SkuTier.GENERAL_PURPOSE))
            .withIdentity(new UserAssignedIdentity().withUserAssignedIdentities(mapOf(
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/exampleresourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/exampleprimaryidentity",
                new UserIdentity())).withType(IdentityType.USER_ASSIGNED))
            .withAdministratorLogin("exampleadministratorlogin")
            .withAdministratorLoginPassword("examplepassword")
            .withVersion(PostgresMajorVersion.ONE_SEVEN)
            .withStorage(new Storage().withStorageSizeGB(512)
                .withAutoGrow(StorageAutoGrow.DISABLED)
                .withTier(AzureManagedDiskPerformanceTier.P20))
            .withDataEncryption(new DataEncryption().withPrimaryKeyUri("fakeTokenPlaceholder")
                .withPrimaryUserAssignedIdentityId(
                    "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/exampleresourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/exampleprimaryidentity")
                .withGeoBackupKeyUri("fakeTokenPlaceholder")
                .withGeoBackupUserAssignedIdentityId("")
                .withType(DataEncryptionType.AZURE_KEY_VAULT))
            .withBackup(
                new Backup().withBackupRetentionDays(7).withGeoRedundantBackup(GeographicallyRedundantBackup.DISABLED))
            .withNetwork(new Network().withDelegatedSubnetResourceId(
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/exampleresourcegroup/providers/Microsoft.Network/virtualNetworks/examplevirtualnetwork/subnets/examplesubnet")
                .withPrivateDnsZoneArmResourceId(
                    "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourcegroups/exampleresourcegroup/providers/Microsoft.Network/privateDnsZones/exampleprivatednszone.postgres.database.azure.com"))
            .withHighAvailability(new HighAvailability().withMode(HighAvailabilityMode.ZONE_REDUNDANT))
            .withAvailabilityZone("1")
            .withCreateMode(CreateMode.CREATE)
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
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/ServersDelete.json
     */
    /**
     * Sample code: Delete or drop an existing server.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void
        deleteOrDropAnExistingServer(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.servers().delete("exampleresourcegroup", "exampleserver", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * ServersGetWithPrivateEndpoints.json
     */
    /**
     * Sample code: Get information about an existing server that isn't integrated into a virtual network provided by
     * customer and has private endpoint connections.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void
        getInformationAboutAnExistingServerThatIsnTIntegratedIntoAVirtualNetworkProvidedByCustomerAndHasPrivateEndpointConnections(
            com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.servers()
            .getByResourceGroupWithResponse("exampleresourcegroup", "exampleserver", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/ServersGetWithVnet
     * .json
     */
    /**
     * Sample code: Get information about an existing server that is integrated into a virtual network provided by
     * customer.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void getInformationAboutAnExistingServerThatIsIntegratedIntoAVirtualNetworkProvidedByCustomer(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.servers()
            .getByResourceGroupWithResponse("exampleresourcegroup", "exampleserver", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/ServersGet.json
     */
    /**
     * Sample code: Get information about an existing server.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void getInformationAboutAnExistingServer(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.servers()
            .getByResourceGroupWithResponse("exampleresourcegroup", "exampleserver", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * ServersListBySubscription.json
     */
    /**
     * Sample code: List all servers in a subscription.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void
        listAllServersInASubscription(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
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
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * ServersListByResourceGroup.json
     */
    /**
     * Sample code: List all servers in a resource group.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void
        listAllServersInAResourceGroup(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.servers().listByResourceGroup("exampleresourcegroup", com.azure.core.util.Context.NONE);
    }
}
```

### Servers_Restart

```java
import com.azure.resourcemanager.postgresqlflexibleserver.models.FailoverMode;
import com.azure.resourcemanager.postgresqlflexibleserver.models.RestartParameter;

/**
 * Samples for Servers Restart.
 */
public final class ServersRestartSamples {
    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * ServersRestartWithFailover.json
     */
    /**
     * Sample code: Restart PostgreSQL database engine in a server with a forced failover to standby server.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void restartPostgreSQLDatabaseEngineInAServerWithAForcedFailoverToStandbyServer(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.servers()
            .restart("exampleresourcegroup", "exampleserver",
                new RestartParameter().withRestartWithFailover(true).withFailoverMode(FailoverMode.FORCED_FAILOVER),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/ServersRestart.
     * json
     */
    /**
     * Sample code: Restart PostgreSQL database engine in a server.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void restartPostgreSQLDatabaseEngineInAServer(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.servers().restart("exampleresourcegroup", "exampleserver", null, com.azure.core.util.Context.NONE);
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
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/ServersStart.json
     */
    /**
     * Sample code: Start a stopped server.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void
        startAStoppedServer(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.servers().start("exampleresourcegroup", "exampleserver", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/ServersStop.json
     */
    /**
     * Sample code: Stop a server.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void stopAServer(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.servers().stop("exampleresourcegroup", "exampleserver", com.azure.core.util.Context.NONE);
    }
}
```

### Servers_Update

```java
import com.azure.resourcemanager.postgresqlflexibleserver.models.AuthConfigForPatch;
import com.azure.resourcemanager.postgresqlflexibleserver.models.AzureManagedDiskPerformanceTier;
import com.azure.resourcemanager.postgresqlflexibleserver.models.BackupForPatch;
import com.azure.resourcemanager.postgresqlflexibleserver.models.CreateModeForPatch;
import com.azure.resourcemanager.postgresqlflexibleserver.models.DataEncryption;
import com.azure.resourcemanager.postgresqlflexibleserver.models.DataEncryptionType;
import com.azure.resourcemanager.postgresqlflexibleserver.models.IdentityType;
import com.azure.resourcemanager.postgresqlflexibleserver.models.MaintenanceWindowForPatch;
import com.azure.resourcemanager.postgresqlflexibleserver.models.MicrosoftEntraAuth;
import com.azure.resourcemanager.postgresqlflexibleserver.models.PasswordBasedAuth;
import com.azure.resourcemanager.postgresqlflexibleserver.models.PostgresMajorVersion;
import com.azure.resourcemanager.postgresqlflexibleserver.models.ReadReplicaPromoteMode;
import com.azure.resourcemanager.postgresqlflexibleserver.models.ReadReplicaPromoteOption;
import com.azure.resourcemanager.postgresqlflexibleserver.models.Replica;
import com.azure.resourcemanager.postgresqlflexibleserver.models.Server;
import com.azure.resourcemanager.postgresqlflexibleserver.models.SkuForPatch;
import com.azure.resourcemanager.postgresqlflexibleserver.models.SkuTier;
import com.azure.resourcemanager.postgresqlflexibleserver.models.Storage;
import com.azure.resourcemanager.postgresqlflexibleserver.models.StorageAutoGrow;
import com.azure.resourcemanager.postgresqlflexibleserver.models.UserAssignedIdentity;
import com.azure.resourcemanager.postgresqlflexibleserver.models.UserIdentity;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Servers Update.
 */
public final class ServersUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * ServersPromoteReplicaAsForcedSwitchover.json
     */
    /**
     * Sample code: Switch over a read replica to primary server with forced data synchronization. Meaning that it
     * doesn't wait for data in the read replica to be synchronized with its source server before it initiates the
     * switching of roles between the read replica and the primary server.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void
        switchOverAReadReplicaToPrimaryServerWithForcedDataSynchronizationMeaningThatItDoesnTWaitForDataInTheReadReplicaToBeSynchronizedWithItsSourceServerBeforeItInitiatesTheSwitchingOfRolesBetweenTheReadReplicaAndThePrimaryServer(
            com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        Server resource = manager.servers()
            .getByResourceGroupWithResponse("exampleresourcegroup", "exampleserver", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withReplica(new Replica().withPromoteMode(ReadReplicaPromoteMode.SWITCHOVER)
                .withPromoteOption(ReadReplicaPromoteOption.FORCED))
            .apply();
    }

    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * ServersPromoteReplicaAsPlannedSwitchover.json
     */
    /**
     * Sample code: Switch over a read replica to primary server with planned data synchronization. Meaning that it
     * waits for data in the read replica to be fully synchronized with its source server before it initiates the
     * switching of roles between the read replica and the primary server.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void
        switchOverAReadReplicaToPrimaryServerWithPlannedDataSynchronizationMeaningThatItWaitsForDataInTheReadReplicaToBeFullySynchronizedWithItsSourceServerBeforeItInitiatesTheSwitchingOfRolesBetweenTheReadReplicaAndThePrimaryServer(
            com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        Server resource = manager.servers()
            .getByResourceGroupWithResponse("exampleresourcegroup", "exampleserver", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withReplica(new Replica().withPromoteMode(ReadReplicaPromoteMode.SWITCHOVER)
                .withPromoteOption(ReadReplicaPromoteOption.PLANNED))
            .apply();
    }

    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * ServersUpdateWithMicrosoftEntraEnabled.json
     */
    /**
     * Sample code: Update an existing server with Microsoft Entra authentication enabled.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void updateAnExistingServerWithMicrosoftEntraAuthenticationEnabled(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        Server resource = manager.servers()
            .getByResourceGroupWithResponse("exampleresourcegroup", "exampleserver", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withSku(new SkuForPatch().withName("Standard_D8s_v3").withTier(SkuTier.GENERAL_PURPOSE))
            .withAdministratorLoginPassword("examplenewpassword")
            .withStorage(new Storage().withStorageSizeGB(1024)
                .withAutoGrow(StorageAutoGrow.DISABLED)
                .withTier(AzureManagedDiskPerformanceTier.P30))
            .withBackup(new BackupForPatch().withBackupRetentionDays(20))
            .withAuthConfig(new AuthConfigForPatch().withActiveDirectoryAuth(MicrosoftEntraAuth.ENABLED)
                .withPasswordAuth(PasswordBasedAuth.ENABLED)
                .withTenantId("tttttt-tttt-tttt-tttt-tttttttttttt"))
            .withCreateMode(CreateModeForPatch.UPDATE)
            .apply();
    }

    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * ServersUpdateWithDataEncryptionEnabledAutoUpdate.json
     */
    /**
     * Sample code: Update an existing server with data encryption based on customer managed key with automatic key
     * version update.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void updateAnExistingServerWithDataEncryptionBasedOnCustomerManagedKeyWithAutomaticKeyVersionUpdate(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        Server resource = manager.servers()
            .getByResourceGroupWithResponse("exampleresourcegroup", "exampleserver", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withSku(new SkuForPatch().withName("Standard_D8s_v3").withTier(SkuTier.GENERAL_PURPOSE))
            .withIdentity(new UserAssignedIdentity().withUserAssignedIdentities(mapOf(
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/exampleresourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/examplegeoredundantidentity",
                new UserIdentity(),
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/exampleresourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/exampleprimaryidentity",
                new UserIdentity())).withType(IdentityType.USER_ASSIGNED))
            .withAdministratorLoginPassword("examplenewpassword")
            .withBackup(new BackupForPatch().withBackupRetentionDays(20))
            .withDataEncryption(new DataEncryption().withPrimaryKeyUri("fakeTokenPlaceholder")
                .withPrimaryUserAssignedIdentityId(
                    "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/exampleresourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/exampleprimaryidentity")
                .withGeoBackupKeyUri("fakeTokenPlaceholder")
                .withGeoBackupUserAssignedIdentityId(
                    "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/exampleresourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/examplegeoredundantidentity")
                .withType(DataEncryptionType.AZURE_KEY_VAULT))
            .withCreateMode(CreateModeForPatch.UPDATE)
            .apply();
    }

    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * ServersUpdateWithMajorVersionUpgrade.json
     */
    /**
     * Sample code: Update an existing server to upgrade the major version of PostgreSQL database engine.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void updateAnExistingServerToUpgradeTheMajorVersionOfPostgreSQLDatabaseEngine(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        Server resource = manager.servers()
            .getByResourceGroupWithResponse("exampleresourcegroup", "exampleserver", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withVersion(PostgresMajorVersion.ONE_SEVEN).withCreateMode(CreateModeForPatch.UPDATE).apply();
    }

    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * ServersUpdateWithCustomMaintenanceWindow.json
     */
    /**
     * Sample code: Update an existing server with custom maintenance window.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void updateAnExistingServerWithCustomMaintenanceWindow(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        Server resource = manager.servers()
            .getByResourceGroupWithResponse("exampleresourcegroup", "exampleserver", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withMaintenanceWindow(new MaintenanceWindowForPatch().withCustomWindow("Enabled")
                .withStartHour(8)
                .withStartMinute(0)
                .withDayOfWeek(0))
            .withCreateMode(CreateModeForPatch.UPDATE)
            .apply();
    }

    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * ServersUpdateWithDataEncryptionEnabled.json
     */
    /**
     * Sample code: Update an existing server with data encryption based on customer managed key.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void updateAnExistingServerWithDataEncryptionBasedOnCustomerManagedKey(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        Server resource = manager.servers()
            .getByResourceGroupWithResponse("exampleresourcegroup", "exampleserver", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withSku(new SkuForPatch().withName("Standard_D8s_v3").withTier(SkuTier.GENERAL_PURPOSE))
            .withIdentity(new UserAssignedIdentity().withUserAssignedIdentities(mapOf(
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/exampleresourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/examplegeoredundantidentity",
                new UserIdentity(),
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/exampleresourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/exampleprimaryidentity",
                new UserIdentity())).withType(IdentityType.USER_ASSIGNED))
            .withAdministratorLoginPassword("examplenewpassword")
            .withBackup(new BackupForPatch().withBackupRetentionDays(20))
            .withDataEncryption(new DataEncryption().withPrimaryKeyUri("fakeTokenPlaceholder")
                .withPrimaryUserAssignedIdentityId(
                    "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/exampleresourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/exampleprimaryidentity")
                .withGeoBackupKeyUri("fakeTokenPlaceholder")
                .withGeoBackupUserAssignedIdentityId(
                    "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/exampleresourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/examplegeoredundantidentity")
                .withType(DataEncryptionType.AZURE_KEY_VAULT))
            .withCreateMode(CreateModeForPatch.UPDATE)
            .apply();
    }

    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/ServersUpdate.json
     */
    /**
     * Sample code: Update an existing server.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void
        updateAnExistingServer(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        Server resource = manager.servers()
            .getByResourceGroupWithResponse("exampleresourcegroup", "exampleserver", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withSku(new SkuForPatch().withName("Standard_D8s_v3").withTier(SkuTier.GENERAL_PURPOSE))
            .withAdministratorLoginPassword("examplenewpassword")
            .withStorage(new Storage().withStorageSizeGB(1024)
                .withAutoGrow(StorageAutoGrow.ENABLED)
                .withTier(AzureManagedDiskPerformanceTier.P30))
            .withBackup(new BackupForPatch().withBackupRetentionDays(20))
            .withCreateMode(CreateModeForPatch.UPDATE)
            .apply();
    }

    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * ServersPromoteReplicaAsForcedStandaloneServer.json
     */
    /**
     * Sample code: Promote a read replica to a standalone server with forced data synchronization. Meaning that it
     * doesn't wait for data in the read replica to be synchronized with its source server before it initiates the
     * promotion to a standalone server.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void
        promoteAReadReplicaToAStandaloneServerWithForcedDataSynchronizationMeaningThatItDoesnTWaitForDataInTheReadReplicaToBeSynchronizedWithItsSourceServerBeforeItInitiatesThePromotionToAStandaloneServer(
            com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        Server resource = manager.servers()
            .getByResourceGroupWithResponse("exampleresourcegroup", "exampleserver", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withReplica(new Replica().withPromoteMode(ReadReplicaPromoteMode.STANDALONE)
                .withPromoteOption(ReadReplicaPromoteOption.FORCED))
            .apply();
    }

    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * ServersPromoteReplicaAsPlannedStandaloneServer.json
     */
    /**
     * Sample code: Promote a read replica to a standalone server with planned data synchronization. Meaning that it
     * waits for data in the read replica to be fully synchronized with its source server before it initiates the
     * promotion to a standalone server.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void
        promoteAReadReplicaToAStandaloneServerWithPlannedDataSynchronizationMeaningThatItWaitsForDataInTheReadReplicaToBeFullySynchronizedWithItsSourceServerBeforeItInitiatesThePromotionToAStandaloneServer(
            com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        Server resource = manager.servers()
            .getByResourceGroupWithResponse("exampleresourcegroup", "exampleserver", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withReplica(new Replica().withPromoteMode(ReadReplicaPromoteMode.STANDALONE)
                .withPromoteOption(ReadReplicaPromoteOption.PLANNED))
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

### TuningOptionsOperation_Get

```java
import com.azure.resourcemanager.postgresqlflexibleserver.models.TuningOptionParameterEnum;

/**
 * Samples for TuningOptionsOperation Get.
 */
public final class TuningOptionsOperationGetSamples {
    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/TuningOptionsGet.
     * json
     */
    /**
     * Sample code: Get the tuning options of a server.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void
        getTheTuningOptionsOfAServer(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.tuningOptionsOperations()
            .getWithResponse("exampleresourcegroup", "exampleserver", TuningOptionParameterEnum.INDEX,
                com.azure.core.util.Context.NONE);
    }
}
```

### TuningOptionsOperation_ListByServer

```java
/**
 * Samples for TuningOptionsOperation ListByServer.
 */
public final class TuningOptionsOperationListByServerSamples {
    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * TuningOptionsListByServer.json
     */
    /**
     * Sample code: List the tuning options of a server.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void
        listTheTuningOptionsOfAServer(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.tuningOptionsOperations()
            .listByServer("exampleresourcegroup", "exampleserver", com.azure.core.util.Context.NONE);
    }
}
```

### TuningOptionsOperation_ListRecommendations

```java
import com.azure.resourcemanager.postgresqlflexibleserver.models.RecommendationTypeParameterEnum;
import com.azure.resourcemanager.postgresqlflexibleserver.models.TuningOptionParameterEnum;

/**
 * Samples for TuningOptionsOperation ListRecommendations.
 */
public final class TuningOptionsOperationListRecommendationsSamples {
    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * TuningOptionsListIndexRecommendations.json
     */
    /**
     * Sample code: List available index recommendations.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void listAvailableIndexRecommendations(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.tuningOptionsOperations()
            .listRecommendations("exampleresourcegroup", "exampleserver", TuningOptionParameterEnum.INDEX, null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * TuningOptionsListTableRecommendations.json
     */
    /**
     * Sample code: List available table recommendations.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void listAvailableTableRecommendations(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.tuningOptionsOperations()
            .listRecommendations("exampleresourcegroup", "exampleserver", TuningOptionParameterEnum.TABLE, null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * TuningOptionsListIndexRecommendationsFilteredForCreateIndex.json
     */
    /**
     * Sample code: List available index recommendations, filtered to exclusively get those of CREATE INDEX type.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void listAvailableIndexRecommendationsFilteredToExclusivelyGetThoseOfCREATEINDEXType(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.tuningOptionsOperations()
            .listRecommendations("exampleresourcegroup", "exampleserver", TuningOptionParameterEnum.INDEX,
                RecommendationTypeParameterEnum.CREATE_INDEX, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * TuningOptionsListTableRecommendationsFilteredForAnalyzeTable.json
     */
    /**
     * Sample code: List available table recommendations, filtered to exclusively get those of ANALYZE TABLE type.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void listAvailableTableRecommendationsFilteredToExclusivelyGetThoseOfANALYZETABLEType(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.tuningOptionsOperations()
            .listRecommendations("exampleresourcegroup", "exampleserver", TuningOptionParameterEnum.TABLE,
                RecommendationTypeParameterEnum.ANALYZE_TABLE, com.azure.core.util.Context.NONE);
    }
}
```

### VirtualEndpoints_Create

```java
import com.azure.resourcemanager.postgresqlflexibleserver.models.VirtualEndpointType;
import java.util.Arrays;

/**
 * Samples for VirtualEndpoints Create.
 */
public final class VirtualEndpointsCreateSamples {
    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * VirtualEndpointCreate.json
     */
    /**
     * Sample code: Create a pair of virtual endpoints for a server.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void createAPairOfVirtualEndpointsForAServer(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.virtualEndpoints()
            .define("examplebasename")
            .withExistingFlexibleServer("exampleresourcegroup", "exampleserver")
            .withEndpointType(VirtualEndpointType.READ_WRITE)
            .withMembers(Arrays.asList("exampleprimaryserver"))
            .create();
    }
}
```

### VirtualEndpoints_Delete

```java
/**
 * Samples for VirtualEndpoints Delete.
 */
public final class VirtualEndpointsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * VirtualEndpointDelete.json
     */
    /**
     * Sample code: Delete a pair of virtual endpoints.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void
        deleteAPairOfVirtualEndpoints(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.virtualEndpoints()
            .delete("exampleresourcegroup", "exampleserver", "examplebasename", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualEndpoints_Get

```java
/**
 * Samples for VirtualEndpoints Get.
 */
public final class VirtualEndpointsGetSamples {
    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * VirtualEndpointsGet.json
     */
    /**
     * Sample code: Get information about a pair of virtual endpoints.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void getInformationAboutAPairOfVirtualEndpoints(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.virtualEndpoints()
            .getWithResponse("exampleresourcegroup", "exampleserver", "examplebasename",
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualEndpoints_ListByServer

```java
/**
 * Samples for VirtualEndpoints ListByServer.
 */
public final class VirtualEndpointsListByServerSamples {
    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * VirtualEndpointsListByServer.json
     */
    /**
     * Sample code: List pair of virtual endpoints associated to a server.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void listPairOfVirtualEndpointsAssociatedToAServer(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.virtualEndpoints()
            .listByServer("exampleresourcegroup", "exampleserver", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualEndpoints_Update

```java
import com.azure.resourcemanager.postgresqlflexibleserver.models.VirtualEndpoint;
import com.azure.resourcemanager.postgresqlflexibleserver.models.VirtualEndpointType;
import java.util.Arrays;

/**
 * Samples for VirtualEndpoints Update.
 */
public final class VirtualEndpointsUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * VirtualEndpointUpdate.json
     */
    /**
     * Sample code: Update a pair of virtual endpoints for a server.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void updateAPairOfVirtualEndpointsForAServer(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        VirtualEndpoint resource = manager.virtualEndpoints()
            .getWithResponse("exampleresourcegroup", "exampleserver", "examplebasename",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withEndpointType(VirtualEndpointType.READ_WRITE)
            .withMembers(Arrays.asList("exampleprimaryserver"))
            .apply();
    }
}
```

### VirtualNetworkSubnetUsage_List

```java
import com.azure.resourcemanager.postgresqlflexibleserver.models.VirtualNetworkSubnetUsageParameter;

/**
 * Samples for VirtualNetworkSubnetUsage List.
 */
public final class VirtualNetworkSubnetUsageListSamples {
    /*
     * x-ms-original-file:
     * specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2025-08-01/examples/
     * VirtualNetworkSubnetUsageList.json
     */
    /**
     * Sample code: List the virtual network subnet usage for a given virtual network.
     * 
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void listTheVirtualNetworkSubnetUsageForAGivenVirtualNetwork(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.virtualNetworkSubnetUsages()
            .listWithResponse("eastus", new VirtualNetworkSubnetUsageParameter().withVirtualNetworkArmResourceId(
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/exampleresourcegroup/providers/Microsoft.Network/virtualNetworks/examplevirtualnetwork"),
                com.azure.core.util.Context.NONE);
    }
}
```

