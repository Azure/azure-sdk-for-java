# Code snippets and samples


## CheckNameAvailability

- [Execute](#checknameavailability_execute)

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
### CheckNameAvailability_Execute

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.postgresqlflexibleserver.models.NameAvailabilityRequest;

/** Samples for CheckNameAvailability Execute. */
public final class CheckNameAvailabilityExecuteSamples {
    /*
     * operationId: CheckNameAvailability_Execute
     * api-version: 2021-06-01
     * x-ms-examples: NameAvailability
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
                new NameAvailabilityRequest().withName("name1").withType("Microsoft.DBforPostgreSQL/flexibleServers"),
                Context.NONE);
    }
}
```

### Configurations_Get

```java
import com.azure.core.util.Context;

/** Samples for Configurations Get. */
public final class ConfigurationsGetSamples {
    /*
     * operationId: Configurations_Get
     * api-version: 2021-06-01
     * x-ms-examples: ConfigurationGet
     */
    /**
     * Sample code: ConfigurationGet.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void configurationGet(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.configurations().getWithResponse("testrg", "testserver", "array_nulls", Context.NONE);
    }
}
```

### Configurations_ListByServer

```java
import com.azure.core.util.Context;

/** Samples for Configurations ListByServer. */
public final class ConfigurationsListByServerSamples {
    /*
     * operationId: Configurations_ListByServer
     * api-version: 2021-06-01
     * x-ms-examples: ConfigurationList
     */
    /**
     * Sample code: ConfigurationList.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void configurationList(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.configurations().listByServer("testrg", "testserver", Context.NONE);
    }
}
```

### Configurations_Put

```java
/** Samples for Configurations Put. */
public final class ConfigurationsPutSamples {
    /*
     * operationId: Configurations_Put
     * api-version: 2021-06-01
     * x-ms-examples: Update a user configuration
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
import com.azure.core.util.Context;
import com.azure.resourcemanager.postgresqlflexibleserver.models.Configuration;

/** Samples for Configurations Update. */
public final class ConfigurationsUpdateSamples {
    /*
     * operationId: Configurations_Update
     * api-version: 2021-06-01
     * x-ms-examples: Update a user configuration
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
                .getWithResponse("testrg", "testserver", "event_scheduler", Context.NONE)
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
     * operationId: Databases_Create
     * api-version: 2021-06-01
     * x-ms-examples: Create a database
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
import com.azure.core.util.Context;

/** Samples for Databases Delete. */
public final class DatabasesDeleteSamples {
    /*
     * operationId: Databases_Delete
     * api-version: 2021-06-01
     * x-ms-examples: Delete a database
     */
    /**
     * Sample code: Delete a database.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void deleteADatabase(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.databases().delete("TestGroup", "testserver", "db1", Context.NONE);
    }
}
```

### Databases_Get

```java
import com.azure.core.util.Context;

/** Samples for Databases Get. */
public final class DatabasesGetSamples {
    /*
     * operationId: Databases_Get
     * api-version: 2021-06-01
     * x-ms-examples: Get a database
     */
    /**
     * Sample code: Get a database.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void getADatabase(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.databases().getWithResponse("TestGroup", "testserver", "db1", Context.NONE);
    }
}
```

### Databases_ListByServer

```java
import com.azure.core.util.Context;

/** Samples for Databases ListByServer. */
public final class DatabasesListByServerSamples {
    /*
     * operationId: Databases_ListByServer
     * api-version: 2021-06-01
     * x-ms-examples: List databases in a server
     */
    /**
     * Sample code: List databases in a server.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void listDatabasesInAServer(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.databases().listByServer("TestGroup", "testserver", Context.NONE);
    }
}
```

### FirewallRules_CreateOrUpdate

```java
/** Samples for FirewallRules CreateOrUpdate. */
public final class FirewallRulesCreateOrUpdateSamples {
    /*
     * operationId: FirewallRules_CreateOrUpdate
     * api-version: 2021-06-01
     * x-ms-examples: FirewallRuleCreate
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
import com.azure.core.util.Context;

/** Samples for FirewallRules Delete. */
public final class FirewallRulesDeleteSamples {
    /*
     * operationId: FirewallRules_Delete
     * api-version: 2021-06-01
     * x-ms-examples: FirewallRuleDelete
     */
    /**
     * Sample code: FirewallRuleDelete.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void firewallRuleDelete(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.firewallRules().delete("testrg", "testserver", "rule1", Context.NONE);
    }
}
```

### FirewallRules_Get

```java
import com.azure.core.util.Context;

/** Samples for FirewallRules Get. */
public final class FirewallRulesGetSamples {
    /*
     * operationId: FirewallRules_Get
     * api-version: 2021-06-01
     * x-ms-examples: FirewallRuleList
     */
    /**
     * Sample code: FirewallRuleList.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void firewallRuleList(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.firewallRules().getWithResponse("testrg", "testserver", "rule1", Context.NONE);
    }
}
```

### FirewallRules_ListByServer

```java
import com.azure.core.util.Context;

/** Samples for FirewallRules ListByServer. */
public final class FirewallRulesListByServerSamples {
    /*
     * operationId: FirewallRules_ListByServer
     * api-version: 2021-06-01
     * x-ms-examples: FirewallRuleList
     */
    /**
     * Sample code: FirewallRuleList.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void firewallRuleList(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.firewallRules().listByServer("testrg", "testserver", Context.NONE);
    }
}
```

### GetPrivateDnsZoneSuffix_Execute

```java
import com.azure.core.util.Context;

/** Samples for GetPrivateDnsZoneSuffix Execute. */
public final class GetPrivateDnsZoneSuffixExecuteSamples {
    /*
     * operationId: GetPrivateDnsZoneSuffix_Execute
     * api-version: 2021-06-01
     * x-ms-examples: GetPrivateDnsZoneSuffix
     */
    /**
     * Sample code: GetPrivateDnsZoneSuffix.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void getPrivateDnsZoneSuffix(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.getPrivateDnsZoneSuffixes().executeWithResponse(Context.NONE);
    }
}
```

### LocationBasedCapabilities_Execute

```java
import com.azure.core.util.Context;

/** Samples for LocationBasedCapabilities Execute. */
public final class LocationBasedCapabilitiesExecuteSamples {
    /*
     * operationId: LocationBasedCapabilities_Execute
     * api-version: 2021-06-01
     * x-ms-examples: CapabilitiesList
     */
    /**
     * Sample code: CapabilitiesList.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void capabilitiesList(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.locationBasedCapabilities().execute("westus", Context.NONE);
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * operationId: Operations_List
     * api-version: 2021-06-01
     * x-ms-examples: OperationList
     */
    /**
     * Sample code: OperationList.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void operationList(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.operations().listWithResponse(Context.NONE);
    }
}
```

### Servers_Create

```java
import com.azure.resourcemanager.postgresqlflexibleserver.models.Backup;
import com.azure.resourcemanager.postgresqlflexibleserver.models.CreateMode;
import com.azure.resourcemanager.postgresqlflexibleserver.models.GeoRedundantBackupEnum;
import com.azure.resourcemanager.postgresqlflexibleserver.models.HighAvailability;
import com.azure.resourcemanager.postgresqlflexibleserver.models.HighAvailabilityMode;
import com.azure.resourcemanager.postgresqlflexibleserver.models.Network;
import com.azure.resourcemanager.postgresqlflexibleserver.models.ServerVersion;
import com.azure.resourcemanager.postgresqlflexibleserver.models.Sku;
import com.azure.resourcemanager.postgresqlflexibleserver.models.SkuTier;
import com.azure.resourcemanager.postgresqlflexibleserver.models.Storage;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/** Samples for Servers Create. */
public final class ServersCreateSamples {
    /*
     * operationId: Servers_Create
     * api-version: 2021-06-01
     * x-ms-examples: Create a new server
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
     * operationId: Servers_Create
     * api-version: 2021-06-01
     * x-ms-examples: Create a database as a point in time restore
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
import com.azure.core.util.Context;

/** Samples for Servers Delete. */
public final class ServersDeleteSamples {
    /*
     * operationId: Servers_Delete
     * api-version: 2021-06-01
     * x-ms-examples: ServerDelete
     */
    /**
     * Sample code: ServerDelete.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void serverDelete(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.servers().delete("testrg", "testserver", Context.NONE);
    }
}
```

### Servers_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Servers GetByResourceGroup. */
public final class ServersGetByResourceGroupSamples {
    /*
     * operationId: Servers_Get
     * api-version: 2021-06-01
     * x-ms-examples: ServerGet
     */
    /**
     * Sample code: ServerGet.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void serverGet(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.servers().getByResourceGroupWithResponse("testrg", "pgtestsvc1", Context.NONE);
    }

    /*
     * operationId: Servers_Get
     * api-version: 2021-06-01
     * x-ms-examples: ServerGetWithVnet
     */
    /**
     * Sample code: ServerGetWithVnet.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void serverGetWithVnet(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.servers().getByResourceGroupWithResponse("testrg", "pgtestsvc4", Context.NONE);
    }
}
```

### Servers_List

```java
import com.azure.core.util.Context;

/** Samples for Servers List. */
public final class ServersListSamples {
    /*
     * operationId: Servers_List
     * api-version: 2021-06-01
     * x-ms-examples: ServerList
     */
    /**
     * Sample code: ServerList.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void serverList(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.servers().list(Context.NONE);
    }
}
```

### Servers_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Servers ListByResourceGroup. */
public final class ServersListByResourceGroupSamples {
    /*
     * operationId: Servers_ListByResourceGroup
     * api-version: 2021-06-01
     * x-ms-examples: ServerListByResourceGroup
     */
    /**
     * Sample code: ServerListByResourceGroup.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void serverListByResourceGroup(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.servers().listByResourceGroup("testrg", Context.NONE);
    }
}
```

### Servers_Restart

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.postgresqlflexibleserver.models.FailoverMode;
import com.azure.resourcemanager.postgresqlflexibleserver.models.RestartParameter;

/** Samples for Servers Restart. */
public final class ServersRestartSamples {
    /*
     * operationId: Servers_Restart
     * api-version: 2021-06-01
     * x-ms-examples: ServerRestart
     */
    /**
     * Sample code: ServerRestart.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void serverRestart(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.servers().restart("testrg", "testserver", null, Context.NONE);
    }

    /*
     * operationId: Servers_Restart
     * api-version: 2021-06-01
     * x-ms-examples: ServerRestartWithFailover
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
                Context.NONE);
    }
}
```

### Servers_Start

```java
import com.azure.core.util.Context;

/** Samples for Servers Start. */
public final class ServersStartSamples {
    /*
     * operationId: Servers_Start
     * api-version: 2021-06-01
     * x-ms-examples: ServerStart
     */
    /**
     * Sample code: ServerStart.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void serverStart(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.servers().start("testrg", "testserver", Context.NONE);
    }
}
```

### Servers_Stop

```java
import com.azure.core.util.Context;

/** Samples for Servers Stop. */
public final class ServersStopSamples {
    /*
     * operationId: Servers_Stop
     * api-version: 2021-06-01
     * x-ms-examples: ServerStop
     */
    /**
     * Sample code: ServerStop.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void serverStop(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        manager.servers().stop("testrg", "testserver", Context.NONE);
    }
}
```

### Servers_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.postgresqlflexibleserver.models.Backup;
import com.azure.resourcemanager.postgresqlflexibleserver.models.CreateModeForUpdate;
import com.azure.resourcemanager.postgresqlflexibleserver.models.MaintenanceWindow;
import com.azure.resourcemanager.postgresqlflexibleserver.models.Server;
import com.azure.resourcemanager.postgresqlflexibleserver.models.Sku;
import com.azure.resourcemanager.postgresqlflexibleserver.models.SkuTier;
import com.azure.resourcemanager.postgresqlflexibleserver.models.Storage;

/** Samples for Servers Update. */
public final class ServersUpdateSamples {
    /*
     * operationId: Servers_Update
     * api-version: 2021-06-01
     * x-ms-examples: ServerUpdate
     */
    /**
     * Sample code: ServerUpdate.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void serverUpdate(com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        Server resource =
            manager.servers().getByResourceGroupWithResponse("TestGroup", "pgtestsvc4", Context.NONE).getValue();
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
     * operationId: Servers_Update
     * api-version: 2021-06-01
     * x-ms-examples: ServerUpdateWithCustomerMaintenanceWindow
     */
    /**
     * Sample code: ServerUpdateWithCustomerMaintenanceWindow.
     *
     * @param manager Entry point to PostgreSqlManager.
     */
    public static void serverUpdateWithCustomerMaintenanceWindow(
        com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager manager) {
        Server resource =
            manager.servers().getByResourceGroupWithResponse("testrg", "pgtestsvc4", Context.NONE).getValue();
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
}
```

### VirtualNetworkSubnetUsage_Execute

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.postgresqlflexibleserver.models.VirtualNetworkSubnetUsageParameter;

/** Samples for VirtualNetworkSubnetUsage Execute. */
public final class VirtualNetworkSubnetUsageExecuteSamples {
    /*
     * operationId: VirtualNetworkSubnetUsage_Execute
     * api-version: 2021-06-01
     * x-ms-examples: VirtualNetworkSubnetUsageList
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
                Context.NONE);
    }
}
```

