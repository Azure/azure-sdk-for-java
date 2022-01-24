# Code snippets and samples


## Backups

- [Get](#backups_get)
- [ListByServer](#backups_listbyserver)

## CheckNameAvailability

- [Execute](#checknameavailability_execute)

## CheckVirtualNetworkSubnetUsage

- [Execute](#checkvirtualnetworksubnetusage_execute)

## Configurations

- [BatchUpdate](#configurations_batchupdate)
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

## Operations

- [List](#operations_list)

## Replicas

- [ListByServer](#replicas_listbyserver)

## Servers

- [Create](#servers_create)
- [Delete](#servers_delete)
- [Failover](#servers_failover)
- [GetByResourceGroup](#servers_getbyresourcegroup)
- [List](#servers_list)
- [ListByResourceGroup](#servers_listbyresourcegroup)
- [Restart](#servers_restart)
- [Start](#servers_start)
- [Stop](#servers_stop)
- [Update](#servers_update)
### Backups_Get

```java
import com.azure.core.util.Context;

/** Samples for Backups Get. */
public final class BackupsGetSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2021-05-01/examples/BackupGet.json
     */
    /**
     * Sample code: Get a backup for a server.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void getABackupForAServer(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.backups().getWithResponse("TestGroup", "mysqltestserver", "daily_20210615T160516", Context.NONE);
    }
}
```

### Backups_ListByServer

```java
import com.azure.core.util.Context;

/** Samples for Backups ListByServer. */
public final class BackupsListByServerSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2021-05-01/examples/BackupsListByServer.json
     */
    /**
     * Sample code: List backups for a server.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void listBackupsForAServer(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.backups().listByServer("TestGroup", "mysqltestserver", Context.NONE);
    }
}
```

### CheckNameAvailability_Execute

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mysqlflexibleserver.models.NameAvailabilityRequest;

/** Samples for CheckNameAvailability Execute. */
public final class CheckNameAvailabilityExecuteSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2021-05-01/examples/CheckNameAvailability.json
     */
    /**
     * Sample code: Check name availability.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void checkNameAvailability(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager
            .checkNameAvailabilities()
            .executeWithResponse(
                "SouthEastAsia",
                new NameAvailabilityRequest().withName("name1").withType("Microsoft.DBforMySQL/flexibleServers"),
                Context.NONE);
    }
}
```

### CheckVirtualNetworkSubnetUsage_Execute

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mysqlflexibleserver.models.VirtualNetworkSubnetUsageParameter;

/** Samples for CheckVirtualNetworkSubnetUsage Execute. */
public final class CheckVirtualNetworkSubnetUsageExecuteSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2021-05-01/examples/CheckVirtualNetworkSubnetUsage.json
     */
    /**
     * Sample code: CheckVirtualNetworkSubnetUsage.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void checkVirtualNetworkSubnetUsage(
        com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager
            .checkVirtualNetworkSubnetUsages()
            .executeWithResponse(
                "WestUS",
                new VirtualNetworkSubnetUsageParameter()
                    .withVirtualNetworkResourceId(
                        "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/testrg/providers/Microsoft.Network/virtualNetworks/testvnet"),
                Context.NONE);
    }
}
```

### Configurations_BatchUpdate

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mysqlflexibleserver.models.ConfigurationForBatchUpdate;
import com.azure.resourcemanager.mysqlflexibleserver.models.ConfigurationListForBatchUpdate;
import java.util.Arrays;

/** Samples for Configurations BatchUpdate. */
public final class ConfigurationsBatchUpdateSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2021-05-01/examples/ConfigurationsBatchUpdate.json
     */
    /**
     * Sample code: ConfigurationList.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void configurationList(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager
            .configurations()
            .batchUpdate(
                "testrg",
                "mysqltestserver",
                new ConfigurationListForBatchUpdate()
                    .withValue(
                        Arrays
                            .asList(
                                new ConfigurationForBatchUpdate().withName("event_scheduler").withValue("OFF"),
                                new ConfigurationForBatchUpdate().withName("div_precision_increment").withValue("8"))),
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
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2021-05-01/examples/ConfigurationGet.json
     */
    /**
     * Sample code: Get a configuration.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void getAConfiguration(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.configurations().getWithResponse("TestGroup", "testserver", "event_scheduler", Context.NONE);
    }
}
```

### Configurations_ListByServer

```java
import com.azure.core.util.Context;

/** Samples for Configurations ListByServer. */
public final class ConfigurationsListByServerSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2021-05-01/examples/ConfigurationsListByServer.json
     */
    /**
     * Sample code: List all configurations for a server.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void listAllConfigurationsForAServer(
        com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.configurations().listByServer("testrg", "mysqltestserver", Context.NONE);
    }
}
```

### Configurations_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mysqlflexibleserver.fluent.models.ConfigurationInner;
import com.azure.resourcemanager.mysqlflexibleserver.models.ConfigurationSource;

/** Samples for Configurations Update. */
public final class ConfigurationsUpdateSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2021-05-01/examples/ConfigurationUpdate.json
     */
    /**
     * Sample code: Update a user configuration.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void updateAUserConfiguration(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager
            .configurations()
            .update(
                "testrg",
                "testserver",
                "event_scheduler",
                new ConfigurationInner().withValue("on").withSource(ConfigurationSource.USER_OVERRIDE),
                Context.NONE);
    }
}
```

### Databases_CreateOrUpdate

```java
/** Samples for Databases CreateOrUpdate. */
public final class DatabasesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2021-05-01/examples/DatabaseCreate.json
     */
    /**
     * Sample code: Create a database.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void createADatabase(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager
            .databases()
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
import com.azure.core.util.Context;

/** Samples for Databases Delete. */
public final class DatabasesDeleteSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2021-05-01/examples/DatabaseDelete.json
     */
    /**
     * Sample code: Delete a database.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void deleteADatabase(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
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
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2021-05-01/examples/DatabaseGet.json
     */
    /**
     * Sample code: Get a database.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void getADatabase(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
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
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2021-05-01/examples/DatabasesListByServer.json
     */
    /**
     * Sample code: List databases in a server.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void listDatabasesInAServer(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.databases().listByServer("TestGroup", "testserver", Context.NONE);
    }
}
```

### FirewallRules_CreateOrUpdate

```java
/** Samples for FirewallRules CreateOrUpdate. */
public final class FirewallRulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2021-05-01/examples/FirewallRuleCreate.json
     */
    /**
     * Sample code: Create a firewall rule.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void createAFirewallRule(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager
            .firewallRules()
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
import com.azure.core.util.Context;

/** Samples for FirewallRules Delete. */
public final class FirewallRulesDeleteSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2021-05-01/examples/FirewallRuleDelete.json
     */
    /**
     * Sample code: Delete a firewall rule.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void deleteAFirewallRule(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.firewallRules().delete("TestGroup", "testserver", "rule1", Context.NONE);
    }
}
```

### FirewallRules_Get

```java
import com.azure.core.util.Context;

/** Samples for FirewallRules Get. */
public final class FirewallRulesGetSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2021-05-01/examples/FirewallRuleGet.json
     */
    /**
     * Sample code: Get a firewall rule.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void getAFirewallRule(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.firewallRules().getWithResponse("TestGroup", "testserver", "rule1", Context.NONE);
    }
}
```

### FirewallRules_ListByServer

```java
import com.azure.core.util.Context;

/** Samples for FirewallRules ListByServer. */
public final class FirewallRulesListByServerSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2021-05-01/examples/FirewallRulesListByServer.json
     */
    /**
     * Sample code: List all firewall rules in a server.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void listAllFirewallRulesInAServer(
        com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.firewallRules().listByServer("TestGroup", "testserver", Context.NONE);
    }
}
```

### GetPrivateDnsZoneSuffix_Execute

```java
import com.azure.core.util.Context;

/** Samples for GetPrivateDnsZoneSuffix Execute. */
public final class GetPrivateDnsZoneSuffixExecuteSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2021-05-01/examples/GetPrivateDnsZoneSuffix.json
     */
    /**
     * Sample code: GetPrivateDnsZoneSuffix.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void getPrivateDnsZoneSuffix(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.getPrivateDnsZoneSuffixes().executeWithResponse(Context.NONE);
    }
}
```

### LocationBasedCapabilities_List

```java
import com.azure.core.util.Context;

/** Samples for LocationBasedCapabilities List. */
public final class LocationBasedCapabilitiesListSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2021-05-01/examples/CapabilitiesByLocationList.json
     */
    /**
     * Sample code: CapabilitiesList.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void capabilitiesList(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.locationBasedCapabilities().list("WestUS", Context.NONE);
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2021-05-01/examples/OperationsList.json
     */
    /**
     * Sample code: OperationList.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void operationList(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### Replicas_ListByServer

```java
import com.azure.core.util.Context;

/** Samples for Replicas ListByServer. */
public final class ReplicasListByServerSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2021-05-01/examples/ReplicasListByServer.json
     */
    /**
     * Sample code: List replicas for a server.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void listReplicasForAServer(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.replicas().listByServer("TestGroup", "mysqltestserver", Context.NONE);
    }
}
```

### Servers_Create

```java
import com.azure.resourcemanager.mysqlflexibleserver.models.Backup;
import com.azure.resourcemanager.mysqlflexibleserver.models.CreateMode;
import com.azure.resourcemanager.mysqlflexibleserver.models.EnableStatusEnum;
import com.azure.resourcemanager.mysqlflexibleserver.models.HighAvailability;
import com.azure.resourcemanager.mysqlflexibleserver.models.HighAvailabilityMode;
import com.azure.resourcemanager.mysqlflexibleserver.models.ServerVersion;
import com.azure.resourcemanager.mysqlflexibleserver.models.Sku;
import com.azure.resourcemanager.mysqlflexibleserver.models.SkuTier;
import com.azure.resourcemanager.mysqlflexibleserver.models.Storage;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/** Samples for Servers Create. */
public final class ServersCreateSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2021-05-01/examples/ServerCreateReplica.json
     */
    /**
     * Sample code: Create a replica server.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void createAReplicaServer(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager
            .servers()
            .define("replica-server")
            .withRegion("SoutheastAsia")
            .withExistingResourceGroup("testgr")
            .withCreateMode(CreateMode.REPLICA)
            .withSourceServerResourceId(
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/testgr/providers/Microsoft.DBforMySQL/flexibleServers/source-server")
            .create();
    }

    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2021-05-01/examples/ServerCreate.json
     */
    /**
     * Sample code: Create a new server.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void createANewServer(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager
            .servers()
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
            .withStorage(new Storage().withStorageSizeGB(100).withIops(600).withAutoGrow(EnableStatusEnum.DISABLED))
            .withBackup(new Backup().withBackupRetentionDays(7).withGeoRedundantBackup(EnableStatusEnum.DISABLED))
            .withHighAvailability(
                new HighAvailability().withMode(HighAvailabilityMode.ZONE_REDUNDANT).withStandbyAvailabilityZone("3"))
            .create();
    }

    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2021-05-01/examples/ServerCreateWithPointInTimeRestore.json
     */
    /**
     * Sample code: Create a server as a point in time restore.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void createAServerAsAPointInTimeRestore(
        com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager
            .servers()
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
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2021-05-01/examples/ServerDelete.json
     */
    /**
     * Sample code: Delete a server.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void deleteAServer(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.servers().delete("TestGroup", "testserver", Context.NONE);
    }
}
```

### Servers_Failover

```java
import com.azure.core.util.Context;

/** Samples for Servers Failover. */
public final class ServersFailoverSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2021-05-01/examples/ServerFailover.json
     */
    /**
     * Sample code: Restart a server.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void restartAServer(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.servers().failover("TestGroup", "testserver", Context.NONE);
    }
}
```

### Servers_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Servers GetByResourceGroup. */
public final class ServersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2021-05-01/examples/ServerGetWithVnet.json
     */
    /**
     * Sample code: Get a server with vnet.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void getAServerWithVnet(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.servers().getByResourceGroupWithResponse("testrg", "mysqltestserver", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2021-05-01/examples/ServerGet.json
     */
    /**
     * Sample code: Get a server.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void getAServer(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.servers().getByResourceGroupWithResponse("testrg", "mysqltestserver", Context.NONE);
    }
}
```

### Servers_List

```java
import com.azure.core.util.Context;

/** Samples for Servers List. */
public final class ServersListSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2021-05-01/examples/ServersList.json
     */
    /**
     * Sample code: List servers in a subscription.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void listServersInASubscription(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
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
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2021-05-01/examples/ServersListByResourceGroup.json
     */
    /**
     * Sample code: List servers in a resource group.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void listServersInAResourceGroup(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.servers().listByResourceGroup("TestGroup", Context.NONE);
    }
}
```

### Servers_Restart

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mysqlflexibleserver.models.EnableStatusEnum;
import com.azure.resourcemanager.mysqlflexibleserver.models.ServerRestartParameter;

/** Samples for Servers Restart. */
public final class ServersRestartSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2021-05-01/examples/ServerRestart.json
     */
    /**
     * Sample code: Restart a server.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void restartAServer(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager
            .servers()
            .restart(
                "TestGroup",
                "testserver",
                new ServerRestartParameter()
                    .withRestartWithFailover(EnableStatusEnum.ENABLED)
                    .withMaxFailoverSeconds(60),
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
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2021-05-01/examples/ServerStart.json
     */
    /**
     * Sample code: Start a server.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void startAServer(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.servers().start("TestGroup", "testserver", Context.NONE);
    }
}
```

### Servers_Stop

```java
import com.azure.core.util.Context;

/** Samples for Servers Stop. */
public final class ServersStopSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2021-05-01/examples/ServerStop.json
     */
    /**
     * Sample code: Stop a server.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void stopAServer(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.servers().stop("TestGroup", "testserver", Context.NONE);
    }
}
```

### Servers_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mysqlflexibleserver.models.EnableStatusEnum;
import com.azure.resourcemanager.mysqlflexibleserver.models.MaintenanceWindow;
import com.azure.resourcemanager.mysqlflexibleserver.models.Server;
import com.azure.resourcemanager.mysqlflexibleserver.models.Storage;

/** Samples for Servers Update. */
public final class ServersUpdateSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2021-05-01/examples/ServerUpdateWithCustomerMaintenanceWindow.json
     */
    /**
     * Sample code: Update server customer maintenance window.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void updateServerCustomerMaintenanceWindow(
        com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        Server resource =
            manager.servers().getByResourceGroupWithResponse("testrg", "mysqltestserver", Context.NONE).getValue();
        resource
            .update()
            .withMaintenanceWindow(
                new MaintenanceWindow()
                    .withCustomWindow("Enabled")
                    .withStartHour(8)
                    .withStartMinute(0)
                    .withDayOfWeek(1))
            .apply();
    }

    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2021-05-01/examples/ServerUpdate.json
     */
    /**
     * Sample code: Update a server.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void updateAServer(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        Server resource =
            manager.servers().getByResourceGroupWithResponse("testrg", "mysqltestserver", Context.NONE).getValue();
        resource
            .update()
            .withStorage(new Storage().withStorageSizeGB(30).withIops(200).withAutoGrow(EnableStatusEnum.DISABLED))
            .apply();
    }
}
```

