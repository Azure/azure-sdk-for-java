# Code snippets and samples


## Configurations

- [Get](#configurations_get)
- [ListByServer](#configurations_listbyserver)
- [ListByServerGroup](#configurations_listbyservergroup)
- [Update](#configurations_update)

## FirewallRules

- [CreateOrUpdate](#firewallrules_createorupdate)
- [Delete](#firewallrules_delete)
- [Get](#firewallrules_get)
- [ListByServerGroup](#firewallrules_listbyservergroup)

## Operations

- [List](#operations_list)

## Roles

- [Create](#roles_create)
- [Delete](#roles_delete)
- [ListByServerGroup](#roles_listbyservergroup)

## ServerGroups

- [CheckNameAvailability](#servergroups_checknameavailability)
- [CreateOrUpdate](#servergroups_createorupdate)
- [Delete](#servergroups_delete)
- [GetByResourceGroup](#servergroups_getbyresourcegroup)
- [List](#servergroups_list)
- [ListByResourceGroup](#servergroups_listbyresourcegroup)
- [Restart](#servergroups_restart)
- [Start](#servergroups_start)
- [Stop](#servergroups_stop)
- [Update](#servergroups_update)

## Servers

- [Get](#servers_get)
- [ListByServerGroup](#servers_listbyservergroup)
### Configurations_Get

```java
import com.azure.core.util.Context;

/** Samples for Configurations Get. */
public final class ConfigurationsGetSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/preview/2020-10-05-privatepreview/examples/ConfigurationGet.json
     */
    /**
     * Sample code: Get single configuration of the server group.
     *
     * @param manager Entry point to PostgresqlhscManager.
     */
    public static void getSingleConfigurationOfTheServerGroup(
        com.azure.resourcemanager.postgresqlhsc.PostgresqlhscManager manager) {
        manager.configurations().getWithResponse("TestResourceGroup", "hsctestsg", "array_nulls", Context.NONE);
    }
}
```

### Configurations_ListByServer

```java
import com.azure.core.util.Context;

/** Samples for Configurations ListByServer. */
public final class ConfigurationsListByServerSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/preview/2020-10-05-privatepreview/examples/ConfigurationListByServer.json
     */
    /**
     * Sample code: List configurations of the server that in the server group.
     *
     * @param manager Entry point to PostgresqlhscManager.
     */
    public static void listConfigurationsOfTheServerThatInTheServerGroup(
        com.azure.resourcemanager.postgresqlhsc.PostgresqlhscManager manager) {
        manager.configurations().listByServer("TestResourceGroup", "hsctestsg", "testserver", Context.NONE);
    }
}
```

### Configurations_ListByServerGroup

```java
import com.azure.core.util.Context;

/** Samples for Configurations ListByServerGroup. */
public final class ConfigurationsListByServerGroupSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/preview/2020-10-05-privatepreview/examples/ConfigurationListByServerGroup.json
     */
    /**
     * Sample code: List configurations of the server group.
     *
     * @param manager Entry point to PostgresqlhscManager.
     */
    public static void listConfigurationsOfTheServerGroup(
        com.azure.resourcemanager.postgresqlhsc.PostgresqlhscManager manager) {
        manager.configurations().listByServerGroup("TestResourceGroup", "hsctestsg", Context.NONE);
    }
}
```

### Configurations_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.postgresqlhsc.fluent.models.ServerGroupConfigurationInner;
import com.azure.resourcemanager.postgresqlhsc.models.ServerRole;
import com.azure.resourcemanager.postgresqlhsc.models.ServerRoleGroupConfiguration;
import java.util.Arrays;

/** Samples for Configurations Update. */
public final class ConfigurationsUpdateSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/preview/2020-10-05-privatepreview/examples/ConfigurationUpdate.json
     */
    /**
     * Sample code: Update single configuration of the server group.
     *
     * @param manager Entry point to PostgresqlhscManager.
     */
    public static void updateSingleConfigurationOfTheServerGroup(
        com.azure.resourcemanager.postgresqlhsc.PostgresqlhscManager manager) {
        manager
            .configurations()
            .update(
                "TestResourceGroup",
                "hsctestsg",
                "array_nulls",
                new ServerGroupConfigurationInner()
                    .withServerRoleGroupConfigurations(
                        Arrays
                            .asList(
                                new ServerRoleGroupConfiguration().withRole(ServerRole.COORDINATOR).withValue("on"),
                                new ServerRoleGroupConfiguration().withRole(ServerRole.WORKER).withValue("off"))),
                Context.NONE);
    }
}
```

### FirewallRules_CreateOrUpdate

```java
/** Samples for FirewallRules CreateOrUpdate. */
public final class FirewallRulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/preview/2020-10-05-privatepreview/examples/FirewallRuleCreate.json
     */
    /**
     * Sample code: Create a firewall rule of the server group.
     *
     * @param manager Entry point to PostgresqlhscManager.
     */
    public static void createAFirewallRuleOfTheServerGroup(
        com.azure.resourcemanager.postgresqlhsc.PostgresqlhscManager manager) {
        manager
            .firewallRules()
            .define("rule1")
            .withExistingServerGroupsv2("TestGroup", "pgtestsvc4")
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
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/preview/2020-10-05-privatepreview/examples/FirewallRuleDelete.json
     */
    /**
     * Sample code: Delete the firewall rule of the server group.
     *
     * @param manager Entry point to PostgresqlhscManager.
     */
    public static void deleteTheFirewallRuleOfTheServerGroup(
        com.azure.resourcemanager.postgresqlhsc.PostgresqlhscManager manager) {
        manager.firewallRules().delete("TestGroup", "pgtestsvc4", "rule1", Context.NONE);
    }
}
```

### FirewallRules_Get

```java
import com.azure.core.util.Context;

/** Samples for FirewallRules Get. */
public final class FirewallRulesGetSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/preview/2020-10-05-privatepreview/examples/FirewallRuleGet.json
     */
    /**
     * Sample code: Get the firewall rule of the server group.
     *
     * @param manager Entry point to PostgresqlhscManager.
     */
    public static void getTheFirewallRuleOfTheServerGroup(
        com.azure.resourcemanager.postgresqlhsc.PostgresqlhscManager manager) {
        manager.firewallRules().getWithResponse("TestGroup", "pgtestsvc4", "rule1", Context.NONE);
    }
}
```

### FirewallRules_ListByServerGroup

```java
import com.azure.core.util.Context;

/** Samples for FirewallRules ListByServerGroup. */
public final class FirewallRulesListByServerGroupSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/preview/2020-10-05-privatepreview/examples/FirewallRuleListByServerGroup.json
     */
    /**
     * Sample code: List firewall rules of the server group.
     *
     * @param manager Entry point to PostgresqlhscManager.
     */
    public static void listFirewallRulesOfTheServerGroup(
        com.azure.resourcemanager.postgresqlhsc.PostgresqlhscManager manager) {
        manager.firewallRules().listByServerGroup("TestGroup", "pgtestsvc4", Context.NONE);
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/preview/2020-10-05-privatepreview/examples/OperationList.json
     */
    /**
     * Sample code: List all available operations.
     *
     * @param manager Entry point to PostgresqlhscManager.
     */
    public static void listAllAvailableOperations(
        com.azure.resourcemanager.postgresqlhsc.PostgresqlhscManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### Roles_Create

```java
/** Samples for Roles Create. */
public final class RolesCreateSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/preview/2020-10-05-privatepreview/examples/RoleCreate.json
     */
    /**
     * Sample code: RoleCreate.
     *
     * @param manager Entry point to PostgresqlhscManager.
     */
    public static void roleCreate(com.azure.resourcemanager.postgresqlhsc.PostgresqlhscManager manager) {
        manager
            .roles()
            .define("role1")
            .withExistingServerGroupsv2("TestGroup", "pgtestsvc4")
            .withPassword("secret")
            .create();
    }
}
```

### Roles_Delete

```java
import com.azure.core.util.Context;

/** Samples for Roles Delete. */
public final class RolesDeleteSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/preview/2020-10-05-privatepreview/examples/RoleDelete.json
     */
    /**
     * Sample code: RoleDelete.
     *
     * @param manager Entry point to PostgresqlhscManager.
     */
    public static void roleDelete(com.azure.resourcemanager.postgresqlhsc.PostgresqlhscManager manager) {
        manager.roles().delete("TestGroup", "pgtestsvc4", "role1", Context.NONE);
    }
}
```

### Roles_ListByServerGroup

```java
import com.azure.core.util.Context;

/** Samples for Roles ListByServerGroup. */
public final class RolesListByServerGroupSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/preview/2020-10-05-privatepreview/examples/RoleListByServerGroup.json
     */
    /**
     * Sample code: RoleList.
     *
     * @param manager Entry point to PostgresqlhscManager.
     */
    public static void roleList(com.azure.resourcemanager.postgresqlhsc.PostgresqlhscManager manager) {
        manager.roles().listByServerGroup("TestGroup", "pgtestsvc4", Context.NONE);
    }
}
```

### ServerGroups_CheckNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.postgresqlhsc.models.NameAvailabilityRequest;

/** Samples for ServerGroups CheckNameAvailability. */
public final class ServerGroupsCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/preview/2020-10-05-privatepreview/examples/CheckNameAvailability.json
     */
    /**
     * Sample code: Check name availability.
     *
     * @param manager Entry point to PostgresqlhscManager.
     */
    public static void checkNameAvailability(com.azure.resourcemanager.postgresqlhsc.PostgresqlhscManager manager) {
        manager
            .serverGroups()
            .checkNameAvailabilityWithResponse(new NameAvailabilityRequest().withName("name1"), Context.NONE);
    }
}
```

### ServerGroups_CreateOrUpdate

```java
import com.azure.resourcemanager.postgresqlhsc.models.CitusVersion;
import com.azure.resourcemanager.postgresqlhsc.models.CreateMode;
import com.azure.resourcemanager.postgresqlhsc.models.PostgreSqlVersion;
import com.azure.resourcemanager.postgresqlhsc.models.ServerEdition;
import com.azure.resourcemanager.postgresqlhsc.models.ServerGroupPropertiesDelegatedSubnetArguments;
import com.azure.resourcemanager.postgresqlhsc.models.ServerGroupPropertiesPrivateDnsZoneArguments;
import com.azure.resourcemanager.postgresqlhsc.models.ServerRole;
import com.azure.resourcemanager.postgresqlhsc.models.ServerRoleGroup;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for ServerGroups CreateOrUpdate. */
public final class ServerGroupsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/preview/2020-10-05-privatepreview/examples/ServerGroupCreateReadReplica.json
     */
    /**
     * Sample code: Create a new server group as a read replica.
     *
     * @param manager Entry point to PostgresqlhscManager.
     */
    public static void createANewServerGroupAsAReadReplica(
        com.azure.resourcemanager.postgresqlhsc.PostgresqlhscManager manager) {
        manager
            .serverGroups()
            .define("hsctestsg")
            .withRegion("westus")
            .withExistingResourceGroup("TestGroup")
            .withCreateMode(CreateMode.READ_REPLICA)
            .withSourceSubscriptionId("dddddddd-dddd-dddd-dddd-dddddddddddd")
            .withSourceResourceGroupName("SourceGroup")
            .withSourceServerGroupName("pgtests-source-server-group")
            .withSourceLocation("eastus")
            .create();
    }

    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/preview/2020-10-05-privatepreview/examples/ServerGroupCreate.json
     */
    /**
     * Sample code: Create a new server group.
     *
     * @param manager Entry point to PostgresqlhscManager.
     */
    public static void createANewServerGroup(com.azure.resourcemanager.postgresqlhsc.PostgresqlhscManager manager) {
        manager
            .serverGroups()
            .define("hsctestsg")
            .withRegion("westus")
            .withExistingResourceGroup("TestGroup")
            .withTags(mapOf("ElasticServer", "1"))
            .withAdministratorLogin("citus")
            .withAdministratorLoginPassword("password")
            .withBackupRetentionDays(35)
            .withPostgresqlVersion(PostgreSqlVersion.ONE_TWO)
            .withCitusVersion(CitusVersion.NINE_FIVE)
            .withEnableMx(true)
            .withEnableZfs(false)
            .withServerRoleGroups(
                Arrays
                    .asList(
                        new ServerRoleGroup()
                            .withServerEdition(ServerEdition.GENERAL_PURPOSE)
                            .withStorageQuotaInMb(524288L)
                            .withVCores(4L)
                            .withEnableHa(true)
                            .withName("")
                            .withRole(ServerRole.COORDINATOR)
                            .withServerCount(1),
                        new ServerRoleGroup()
                            .withServerEdition(ServerEdition.MEMORY_OPTIMIZED)
                            .withStorageQuotaInMb(524288L)
                            .withVCores(4L)
                            .withEnableHa(false)
                            .withName("")
                            .withRole(ServerRole.WORKER)
                            .withServerCount(3)))
            .withAvailabilityZone("1")
            .withStandbyAvailabilityZone("2")
            .withDelegatedSubnetArguments(
                new ServerGroupPropertiesDelegatedSubnetArguments()
                    .withSubnetArmResourceId(
                        "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/testrg/providers/Microsoft.Network/virtualNetworks/test-vnet/subnets/test-vnet-subnet"))
            .withPrivateDnsZoneArguments(
                new ServerGroupPropertiesPrivateDnsZoneArguments()
                    .withPrivateDnsZoneArmResourceId(
                        "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/testrg/providers/Microsoft.Network/privateDnsZones/test-private-dns-zone"))
            .create();
    }

    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/preview/2020-10-05-privatepreview/examples/ServerGroupCreatePITR.json
     */
    /**
     * Sample code: Create a new server group as a point in time restore.
     *
     * @param manager Entry point to PostgresqlhscManager.
     */
    public static void createANewServerGroupAsAPointInTimeRestore(
        com.azure.resourcemanager.postgresqlhsc.PostgresqlhscManager manager) {
        manager
            .serverGroups()
            .define("hsctestsg")
            .withRegion("westus")
            .withExistingResourceGroup("TestGroup")
            .withCreateMode(CreateMode.POINT_IN_TIME_RESTORE)
            .withEnableMx(true)
            .withEnableZfs(false)
            .withSourceSubscriptionId("dddddddd-dddd-dddd-dddd-dddddddddddd")
            .withSourceResourceGroupName("SourceGroup")
            .withSourceServerGroupName("pgtests-source-server-group")
            .withSourceLocation("eastus")
            .withPointInTimeUtc(OffsetDateTime.parse("2017-12-14T00:00:37.467Z"))
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

### ServerGroups_Delete

```java
import com.azure.core.util.Context;

/** Samples for ServerGroups Delete. */
public final class ServerGroupsDeleteSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/preview/2020-10-05-privatepreview/examples/ServerGroupDelete.json
     */
    /**
     * Sample code: Delete the server group.
     *
     * @param manager Entry point to PostgresqlhscManager.
     */
    public static void deleteTheServerGroup(com.azure.resourcemanager.postgresqlhsc.PostgresqlhscManager manager) {
        manager.serverGroups().delete("TestGroup", "testservergroup", Context.NONE);
    }
}
```

### ServerGroups_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for ServerGroups GetByResourceGroup. */
public final class ServerGroupsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/preview/2020-10-05-privatepreview/examples/ServerGroupGet.json
     */
    /**
     * Sample code: Get the server group.
     *
     * @param manager Entry point to PostgresqlhscManager.
     */
    public static void getTheServerGroup(com.azure.resourcemanager.postgresqlhsc.PostgresqlhscManager manager) {
        manager.serverGroups().getByResourceGroupWithResponse("TestGroup", "hsctestsg1", Context.NONE);
    }
}
```

### ServerGroups_List

```java
import com.azure.core.util.Context;

/** Samples for ServerGroups List. */
public final class ServerGroupsListSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/preview/2020-10-05-privatepreview/examples/ServerGroupList.json
     */
    /**
     * Sample code: List all the server groups.
     *
     * @param manager Entry point to PostgresqlhscManager.
     */
    public static void listAllTheServerGroups(com.azure.resourcemanager.postgresqlhsc.PostgresqlhscManager manager) {
        manager.serverGroups().list(Context.NONE);
    }
}
```

### ServerGroups_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for ServerGroups ListByResourceGroup. */
public final class ServerGroupsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/preview/2020-10-05-privatepreview/examples/ServerGroupListByResourceGroup.json
     */
    /**
     * Sample code: List the server groups by resource group.
     *
     * @param manager Entry point to PostgresqlhscManager.
     */
    public static void listTheServerGroupsByResourceGroup(
        com.azure.resourcemanager.postgresqlhsc.PostgresqlhscManager manager) {
        manager.serverGroups().listByResourceGroup("TestGroup", Context.NONE);
    }
}
```

### ServerGroups_Restart

```java
import com.azure.core.util.Context;

/** Samples for ServerGroups Restart. */
public final class ServerGroupsRestartSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/preview/2020-10-05-privatepreview/examples/ServerGroupRestart.json
     */
    /**
     * Sample code: Restart all servers in the server group.
     *
     * @param manager Entry point to PostgresqlhscManager.
     */
    public static void restartAllServersInTheServerGroup(
        com.azure.resourcemanager.postgresqlhsc.PostgresqlhscManager manager) {
        manager.serverGroups().restart("TestGroup", "hsctestsg1", Context.NONE);
    }
}
```

### ServerGroups_Start

```java
import com.azure.core.util.Context;

/** Samples for ServerGroups Start. */
public final class ServerGroupsStartSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/preview/2020-10-05-privatepreview/examples/ServerGroupStart.json
     */
    /**
     * Sample code: Start all servers in the server group.
     *
     * @param manager Entry point to PostgresqlhscManager.
     */
    public static void startAllServersInTheServerGroup(
        com.azure.resourcemanager.postgresqlhsc.PostgresqlhscManager manager) {
        manager.serverGroups().start("TestGroup", "hsctestsg1", Context.NONE);
    }
}
```

### ServerGroups_Stop

```java
import com.azure.core.util.Context;

/** Samples for ServerGroups Stop. */
public final class ServerGroupsStopSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/preview/2020-10-05-privatepreview/examples/ServerGroupStop.json
     */
    /**
     * Sample code: Stop all servers in the server group.
     *
     * @param manager Entry point to PostgresqlhscManager.
     */
    public static void stopAllServersInTheServerGroup(
        com.azure.resourcemanager.postgresqlhsc.PostgresqlhscManager manager) {
        manager.serverGroups().stop("TestGroup", "hsctestsg1", Context.NONE);
    }
}
```

### ServerGroups_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.postgresqlhsc.models.MaintenanceWindow;
import com.azure.resourcemanager.postgresqlhsc.models.PostgreSqlVersion;
import com.azure.resourcemanager.postgresqlhsc.models.ServerEdition;
import com.azure.resourcemanager.postgresqlhsc.models.ServerGroup;
import com.azure.resourcemanager.postgresqlhsc.models.ServerRole;
import com.azure.resourcemanager.postgresqlhsc.models.ServerRoleGroup;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for ServerGroups Update. */
public final class ServerGroupsUpdateSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/preview/2020-10-05-privatepreview/examples/ServerGroupScaleStorage.json
     */
    /**
     * Sample code: Scale storage.
     *
     * @param manager Entry point to PostgresqlhscManager.
     */
    public static void scaleStorage(com.azure.resourcemanager.postgresqlhsc.PostgresqlhscManager manager) {
        ServerGroup resource =
            manager.serverGroups().getByResourceGroupWithResponse("TestGroup", "hsctestsg", Context.NONE).getValue();
        resource
            .update()
            .withServerRoleGroups(
                Arrays
                    .asList(
                        new ServerRoleGroup().withStorageQuotaInMb(8388608L).withName("").withRole(ServerRole.WORKER)))
            .apply();
    }

    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/preview/2020-10-05-privatepreview/examples/ServerGroupAddNode.json
     */
    /**
     * Sample code: Add new worker nodes.
     *
     * @param manager Entry point to PostgresqlhscManager.
     */
    public static void addNewWorkerNodes(com.azure.resourcemanager.postgresqlhsc.PostgresqlhscManager manager) {
        ServerGroup resource =
            manager.serverGroups().getByResourceGroupWithResponse("TestGroup", "hsctestsg", Context.NONE).getValue();
        resource
            .update()
            .withServerRoleGroups(
                Arrays.asList(new ServerRoleGroup().withName("").withRole(ServerRole.WORKER).withServerCount(10)))
            .apply();
    }

    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/preview/2020-10-05-privatepreview/examples/ServerGroupUpdateMaintenanceWindow.json
     */
    /**
     * Sample code: Update customer maintenance window.
     *
     * @param manager Entry point to PostgresqlhscManager.
     */
    public static void updateCustomerMaintenanceWindow(
        com.azure.resourcemanager.postgresqlhsc.PostgresqlhscManager manager) {
        ServerGroup resource =
            manager.serverGroups().getByResourceGroupWithResponse("TestGroup", "hsctestsg", Context.NONE).getValue();
        resource
            .update()
            .withMaintenanceWindow(
                new MaintenanceWindow()
                    .withCustomWindow("Enabled")
                    .withStartHour(8)
                    .withStartMinute(0)
                    .withDayOfWeek(0))
            .apply();
    }

    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/preview/2020-10-05-privatepreview/examples/ServerGroupScaleCompute.json
     */
    /**
     * Sample code: Scale compute.
     *
     * @param manager Entry point to PostgresqlhscManager.
     */
    public static void scaleCompute(com.azure.resourcemanager.postgresqlhsc.PostgresqlhscManager manager) {
        ServerGroup resource =
            manager.serverGroups().getByResourceGroupWithResponse("TestGroup", "hsctestsg", Context.NONE).getValue();
        resource
            .update()
            .withServerRoleGroups(
                Arrays.asList(new ServerRoleGroup().withVCores(16L).withName("").withRole(ServerRole.COORDINATOR)))
            .apply();
    }

    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/preview/2020-10-05-privatepreview/examples/ServerGroupUpdate.json
     */
    /**
     * Sample code: Update the server group.
     *
     * @param manager Entry point to PostgresqlhscManager.
     */
    public static void updateTheServerGroup(com.azure.resourcemanager.postgresqlhsc.PostgresqlhscManager manager) {
        ServerGroup resource =
            manager.serverGroups().getByResourceGroupWithResponse("TestGroup", "hsctestsg", Context.NONE).getValue();
        resource
            .update()
            .withTags(mapOf("ElasticServer", "2"))
            .withAdministratorLoginPassword("secret")
            .withBackupRetentionDays(30)
            .withPostgresqlVersion(PostgreSqlVersion.ONE_TWO)
            .withServerRoleGroups(
                Arrays
                    .asList(
                        new ServerRoleGroup()
                            .withServerEdition(ServerEdition.GENERAL_PURPOSE)
                            .withStorageQuotaInMb(1048576L)
                            .withVCores(8L)
                            .withEnableHa(false)
                            .withName("")
                            .withRole(ServerRole.COORDINATOR)
                            .withServerCount(1),
                        new ServerRoleGroup()
                            .withServerEdition(ServerEdition.MEMORY_OPTIMIZED)
                            .withStorageQuotaInMb(524288L)
                            .withVCores(4L)
                            .withEnableHa(true)
                            .withName("")
                            .withRole(ServerRole.WORKER)
                            .withServerCount(4)))
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

### Servers_Get

```java
import com.azure.core.util.Context;

/** Samples for Servers Get. */
public final class ServersGetSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/preview/2020-10-05-privatepreview/examples/ServerGet.json
     */
    /**
     * Sample code: Get the server of server group.
     *
     * @param manager Entry point to PostgresqlhscManager.
     */
    public static void getTheServerOfServerGroup(com.azure.resourcemanager.postgresqlhsc.PostgresqlhscManager manager) {
        manager.servers().getWithResponse("TestGroup", "hsctestsg1", "hsctestsg1-c", Context.NONE);
    }
}
```

### Servers_ListByServerGroup

```java
import com.azure.core.util.Context;

/** Samples for Servers ListByServerGroup. */
public final class ServersListByServerGroupSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/preview/2020-10-05-privatepreview/examples/ServerListByServerGroup.json
     */
    /**
     * Sample code: List servers of the server group.
     *
     * @param manager Entry point to PostgresqlhscManager.
     */
    public static void listServersOfTheServerGroup(
        com.azure.resourcemanager.postgresqlhsc.PostgresqlhscManager manager) {
        manager.servers().listByServerGroup("TestGroup", "hsctestsg1", Context.NONE);
    }
}
```

