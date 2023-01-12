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

## GetPrivateDnsZoneSuffix

- [Execute](#getprivatednszonesuffix_execute)

## LocationBasedCapabilities

- [Execute](#locationbasedcapabilities_execute)

## Operations

- [List](#operations_list)

## Replicas

- [ListByServer](#replicas_listbyserver)

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

## VirtualNetworkSubnetUsage

- [Execute](#virtualnetworksubnetusage_execute)
### Administrators_Create

```java
import com.azure.resourcemanager.postgresqlflexibleserver.models.PrincipalType;

/** Samples for Administrators Create. */
public final class AdministratorsCreateSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-12-01/examples/AdministratorAdd.json
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
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-12-01/examples/AdministratorDelete.json
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
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-12-01/examples/AdministratorGet.json
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
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-12-01/examples/AdministratorsListByServer.json
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
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-12-01/examples/BackupGet.json
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
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-12-01/examples/BackupListByServer.json
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
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-12-01/examples/CheckNameAvailability.json
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
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-12-01/examples/CheckNameAvailabilityLocationBased.json
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
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-12-01/examples/ConfigurationGet.json
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
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-12-01/examples/ConfigurationListByServer.json
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
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-12-01/examples/ConfigurationUpdate.json
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
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-12-01/examples/ConfigurationUpdate.json
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
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-12-01/examples/DatabaseCreate.json
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
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-12-01/examples/DatabaseDelete.json
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
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-12-01/examples/DatabaseGet.json
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
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-12-01/examples/DatabasesListByServer.json
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
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-12-01/examples/FirewallRuleCreate.json
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
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-12-01/examples/FirewallRuleDelete.json
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
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-12-01/examples/FirewallRuleGet.json
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
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-12-01/examples/FirewallRuleListByServer.json
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

### GetPrivateDnsZoneSuffix_Execute

```java
/** Samples for GetPrivateDnsZoneSuffix Execute. */
public final class GetPrivateDnsZoneSuffixExecuteSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-12-01/examples/GetPrivateDnsZoneSuffix.json
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
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-12-01/examples/CapabilitiesByLocation.json
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

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-12-01/examples/OperationList.json
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

### Replicas_ListByServer

```java
/** Samples for Replicas ListByServer. */
public final class ReplicasListByServerSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-12-01/examples/ReplicasListByServer.json
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

### Servers_Create

```java
import com.azure.resourcemanager.postgresqlflexibleserver.models.ActiveDirectoryAuthEnum;
import com.azure.resourcemanager.postgresqlflexibleserver.models.ArmServerKeyType;
import com.azure.resourcemanager.postgresqlflexibleserver.models.AuthConfig;
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
import com.azure.resourcemanager.postgresqlflexibleserver.models.UserAssignedIdentity;
import com.azure.resourcemanager.postgresqlflexibleserver.models.UserIdentity;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/** Samples for Servers Create. */
public final class ServersCreateSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-12-01/examples/ServerCreateWithDataEncryptionEnabled.json
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
            .withStorage(new Storage().withStorageSizeGB(512))
            .withDataEncryption(
                new DataEncryption()
                    .withPrimaryKeyUri("fakeTokenPlaceholder")
                    .withPrimaryUserAssignedIdentityId(
                        "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/testresourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/test-usermanagedidentity")
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
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-12-01/examples/ServerCreateGeoRestore.json
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
            .withSourceServerResourceId(
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/testrg/providers/Microsoft.DBforPostgreSQL/flexibleServers/sourcepgservername")
            .withPointInTimeUtc(OffsetDateTime.parse("2021-06-27T00:04:59.4078005+00:00"))
            .withCreateMode(CreateMode.GEO_RESTORE)
            .create();
    }

    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-12-01/examples/ServerCreate.json
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
            .withStorage(new Storage().withStorageSizeGB(512))
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
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-12-01/examples/ServerCreateWithAadAuthEnabled.json
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
            .withStorage(new Storage().withStorageSizeGB(512))
            .withAuthConfig(
                new AuthConfig()
                    .withActiveDirectoryAuth(ActiveDirectoryAuthEnum.ENABLED)
                    .withPasswordAuth(PasswordAuthEnum.ENABLED)
                    .withTenantId("tttttt-tttt-tttt-tttt-tttttttttttt"))
            .withDataEncryption(new DataEncryption().withType(ArmServerKeyType.fromString("SystemManaged")))
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
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-12-01/examples/ServerCreateReplica.json
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
            .withSourceServerResourceId(
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/testrg/providers/Microsoft.DBforPostgreSQL/flexibleServers/sourcepgservername")
            .withPointInTimeUtc(OffsetDateTime.parse("2021-06-27T00:04:59.4078005+00:00"))
            .withCreateMode(CreateMode.REPLICA)
            .create();
    }

    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-12-01/examples/ServerCreatePointInTimeRestore.json
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
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-12-01/examples/ServerDelete.json
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
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-12-01/examples/ServerGet.json
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
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-12-01/examples/ServerGetWithVnet.json
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
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-12-01/examples/ServerList.json
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
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-12-01/examples/ServerListByResourceGroup.json
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
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-12-01/examples/ServerRestart.json
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
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-12-01/examples/ServerRestartWithFailover.json
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
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-12-01/examples/ServerStart.json
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
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-12-01/examples/ServerStop.json
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
import com.azure.resourcemanager.postgresqlflexibleserver.models.Backup;
import com.azure.resourcemanager.postgresqlflexibleserver.models.CreateModeForUpdate;
import com.azure.resourcemanager.postgresqlflexibleserver.models.DataEncryption;
import com.azure.resourcemanager.postgresqlflexibleserver.models.IdentityType;
import com.azure.resourcemanager.postgresqlflexibleserver.models.MaintenanceWindow;
import com.azure.resourcemanager.postgresqlflexibleserver.models.PasswordAuthEnum;
import com.azure.resourcemanager.postgresqlflexibleserver.models.Server;
import com.azure.resourcemanager.postgresqlflexibleserver.models.ServerVersion;
import com.azure.resourcemanager.postgresqlflexibleserver.models.Sku;
import com.azure.resourcemanager.postgresqlflexibleserver.models.SkuTier;
import com.azure.resourcemanager.postgresqlflexibleserver.models.Storage;
import com.azure.resourcemanager.postgresqlflexibleserver.models.UserAssignedIdentity;
import com.azure.resourcemanager.postgresqlflexibleserver.models.UserIdentity;
import java.util.HashMap;
import java.util.Map;

/** Samples for Servers Update. */
public final class ServersUpdateSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-12-01/examples/ServerUpdateWithAadAuthEnabled.json
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
            .withStorage(new Storage().withStorageSizeGB(1024))
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
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-12-01/examples/ServerUpdateWithDataEncryptionEnabled.json
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
                            "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/testresourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/test-usermanagedidentity",
                            new UserIdentity()))
                    .withType(IdentityType.USER_ASSIGNED))
            .withAdministratorLoginPassword("newpassword")
            .withStorage(new Storage().withStorageSizeGB(1024))
            .withBackup(new Backup().withBackupRetentionDays(20))
            .withDataEncryption(
                new DataEncryption()
                    .withPrimaryKeyUri("fakeTokenPlaceholder")
                    .withPrimaryUserAssignedIdentityId(
                        "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/testresourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/test-usermanagedidentity")
                    .withType(ArmServerKeyType.AZURE_KEY_VAULT))
            .withCreateMode(CreateModeForUpdate.UPDATE)
            .apply();
    }

    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-12-01/examples/ServerUpdate.json
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
            .withStorage(new Storage().withStorageSizeGB(1024))
            .withBackup(new Backup().withBackupRetentionDays(20))
            .withCreateMode(CreateModeForUpdate.UPDATE)
            .apply();
    }

    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-12-01/examples/ServerUpdateWithMajorVersionUpgrade.json
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
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-12-01/examples/ServerUpdateWithCustomerMaintenanceWindow.json
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

### VirtualNetworkSubnetUsage_Execute

```java
import com.azure.resourcemanager.postgresqlflexibleserver.models.VirtualNetworkSubnetUsageParameter;

/** Samples for VirtualNetworkSubnetUsage Execute. */
public final class VirtualNetworkSubnetUsageExecuteSamples {
    /*
     * x-ms-original-file: specification/postgresql/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-12-01/examples/VirtualNetworkSubnetUsage.json
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

