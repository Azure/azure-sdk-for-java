# Code snippets and samples


## Clusters

- [CheckNameAvailability](#clusters_checknameavailability)
- [Create](#clusters_create)
- [Delete](#clusters_delete)
- [GetByResourceGroup](#clusters_getbyresourcegroup)
- [List](#clusters_list)
- [ListByResourceGroup](#clusters_listbyresourcegroup)
- [PromoteReadReplica](#clusters_promotereadreplica)
- [Restart](#clusters_restart)
- [Start](#clusters_start)
- [Stop](#clusters_stop)
- [Update](#clusters_update)

## Configurations

- [Get](#configurations_get)
- [GetCoordinator](#configurations_getcoordinator)
- [GetNode](#configurations_getnode)
- [ListByCluster](#configurations_listbycluster)
- [ListByServer](#configurations_listbyserver)
- [UpdateOnCoordinator](#configurations_updateoncoordinator)
- [UpdateOnNode](#configurations_updateonnode)

## FirewallRules

- [CreateOrUpdate](#firewallrules_createorupdate)
- [Delete](#firewallrules_delete)
- [Get](#firewallrules_get)
- [ListByCluster](#firewallrules_listbycluster)

## Operations

- [List](#operations_list)

## PrivateEndpointConnections

- [CreateOrUpdate](#privateendpointconnections_createorupdate)
- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [ListByCluster](#privateendpointconnections_listbycluster)

## PrivateLinkResources

- [Get](#privatelinkresources_get)
- [ListByCluster](#privatelinkresources_listbycluster)

## Roles

- [Create](#roles_create)
- [Delete](#roles_delete)
- [Get](#roles_get)
- [ListByCluster](#roles_listbycluster)

## Servers

- [Get](#servers_get)
- [ListByCluster](#servers_listbycluster)
### Clusters_CheckNameAvailability

```java
import com.azure.resourcemanager.cosmosdbforpostgresql.models.NameAvailabilityRequest;

/** Samples for Clusters CheckNameAvailability. */
public final class ClustersCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-11-08/examples/CheckNameAvailability.json
     */
    /**
     * Sample code: Check name availability.
     *
     * @param manager Entry point to CosmosDBForPostgreSqlManager.
     */
    public static void checkNameAvailability(
        com.azure.resourcemanager.cosmosdbforpostgresql.CosmosDBForPostgreSqlManager manager) {
        manager
            .clusters()
            .checkNameAvailabilityWithResponse(
                new NameAvailabilityRequest().withName("name1"), com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_Create

```java
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/** Samples for Clusters Create. */
public final class ClustersCreateSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-11-08/examples/ClusterCreateSingleNode.json
     */
    /**
     * Sample code: Create a new single node cluster.
     *
     * @param manager Entry point to CosmosDBForPostgreSqlManager.
     */
    public static void createANewSingleNodeCluster(
        com.azure.resourcemanager.cosmosdbforpostgresql.CosmosDBForPostgreSqlManager manager) {
        manager
            .clusters()
            .define("testcluster-singlenode")
            .withRegion("westus")
            .withExistingResourceGroup("TestGroup")
            .withTags(mapOf("owner", "JohnDoe"))
            .withAdministratorLoginPassword("password")
            .withPostgresqlVersion("15")
            .withCitusVersion("11.3")
            .withPreferredPrimaryZone("1")
            .withEnableShardsOnCoordinator(true)
            .withEnableHa(true)
            .withCoordinatorServerEdition("GeneralPurpose")
            .withCoordinatorStorageQuotaInMb(131072)
            .withCoordinatorVCores(8)
            .withCoordinatorEnablePublicIpAccess(true)
            .withNodeCount(0)
            .create();
    }

    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-11-08/examples/ClusterCreateBurstablev1.json
     */
    /**
     * Sample code: Create a new single node Burstable 1 vCore cluster.
     *
     * @param manager Entry point to CosmosDBForPostgreSqlManager.
     */
    public static void createANewSingleNodeBurstable1VCoreCluster(
        com.azure.resourcemanager.cosmosdbforpostgresql.CosmosDBForPostgreSqlManager manager) {
        manager
            .clusters()
            .define("testcluster-burstablev1")
            .withRegion("westus")
            .withExistingResourceGroup("TestGroup")
            .withTags(mapOf("owner", "JohnDoe"))
            .withAdministratorLoginPassword("password")
            .withPostgresqlVersion("15")
            .withCitusVersion("11.3")
            .withPreferredPrimaryZone("1")
            .withEnableShardsOnCoordinator(true)
            .withEnableHa(false)
            .withCoordinatorServerEdition("BurstableMemoryOptimized")
            .withCoordinatorStorageQuotaInMb(131072)
            .withCoordinatorVCores(1)
            .withCoordinatorEnablePublicIpAccess(true)
            .withNodeCount(0)
            .create();
    }

    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-11-08/examples/ClusterCreateBurstablev2.json
     */
    /**
     * Sample code: Create a new single node Burstable 2 vCores cluster.
     *
     * @param manager Entry point to CosmosDBForPostgreSqlManager.
     */
    public static void createANewSingleNodeBurstable2VCoresCluster(
        com.azure.resourcemanager.cosmosdbforpostgresql.CosmosDBForPostgreSqlManager manager) {
        manager
            .clusters()
            .define("testcluster-burstablev2")
            .withRegion("westus")
            .withExistingResourceGroup("TestGroup")
            .withTags(mapOf("owner", "JohnDoe"))
            .withAdministratorLoginPassword("password")
            .withPostgresqlVersion("15")
            .withCitusVersion("11.3")
            .withPreferredPrimaryZone("1")
            .withEnableShardsOnCoordinator(true)
            .withEnableHa(false)
            .withCoordinatorServerEdition("BurstableGeneralPurpose")
            .withCoordinatorStorageQuotaInMb(131072)
            .withCoordinatorVCores(2)
            .withCoordinatorEnablePublicIpAccess(true)
            .withNodeCount(0)
            .create();
    }

    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-11-08/examples/ClusterCreateMultiNode.json
     */
    /**
     * Sample code: Create a new multi-node cluster.
     *
     * @param manager Entry point to CosmosDBForPostgreSqlManager.
     */
    public static void createANewMultiNodeCluster(
        com.azure.resourcemanager.cosmosdbforpostgresql.CosmosDBForPostgreSqlManager manager) {
        manager
            .clusters()
            .define("testcluster-multinode")
            .withRegion("westus")
            .withExistingResourceGroup("TestGroup")
            .withTags(mapOf())
            .withAdministratorLoginPassword("password")
            .withPostgresqlVersion("15")
            .withCitusVersion("11.1")
            .withPreferredPrimaryZone("1")
            .withEnableShardsOnCoordinator(false)
            .withEnableHa(true)
            .withCoordinatorServerEdition("GeneralPurpose")
            .withCoordinatorStorageQuotaInMb(524288)
            .withCoordinatorVCores(4)
            .withCoordinatorEnablePublicIpAccess(true)
            .withNodeServerEdition("MemoryOptimized")
            .withNodeCount(3)
            .withNodeStorageQuotaInMb(524288)
            .withNodeVCores(8)
            .withNodeEnablePublicIpAccess(false)
            .create();
    }

    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-11-08/examples/ClusterCreateReadReplica.json
     */
    /**
     * Sample code: Create a new cluster as a read replica.
     *
     * @param manager Entry point to CosmosDBForPostgreSqlManager.
     */
    public static void createANewClusterAsAReadReplica(
        com.azure.resourcemanager.cosmosdbforpostgresql.CosmosDBForPostgreSqlManager manager) {
        manager
            .clusters()
            .define("testcluster")
            .withRegion("westus")
            .withExistingResourceGroup("TestGroup")
            .withSourceResourceId(
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/TestResourceGroup/providers/Microsoft.DBforPostgreSQL/serverGroupsv2/sourcecluster")
            .withSourceLocation("westus")
            .create();
    }

    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-11-08/examples/ClusterCreatePITR.json
     */
    /**
     * Sample code: Create a new cluster as a point in time restore.
     *
     * @param manager Entry point to CosmosDBForPostgreSqlManager.
     */
    public static void createANewClusterAsAPointInTimeRestore(
        com.azure.resourcemanager.cosmosdbforpostgresql.CosmosDBForPostgreSqlManager manager) {
        manager
            .clusters()
            .define("testcluster")
            .withRegion("westus")
            .withExistingResourceGroup("TestGroup")
            .withSourceResourceId(
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/TestResourceGroup/providers/Microsoft.DBforPostgreSQL/serverGroupsv2/source-cluster")
            .withSourceLocation("westus")
            .withPointInTimeUtc(OffsetDateTime.parse("2017-12-14T00:00:37.467Z"))
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

### Clusters_Delete

```java
/** Samples for Clusters Delete. */
public final class ClustersDeleteSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-11-08/examples/ClusterDelete.json
     */
    /**
     * Sample code: Delete the cluster.
     *
     * @param manager Entry point to CosmosDBForPostgreSqlManager.
     */
    public static void deleteTheCluster(
        com.azure.resourcemanager.cosmosdbforpostgresql.CosmosDBForPostgreSqlManager manager) {
        manager.clusters().delete("TestGroup", "testcluster", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_GetByResourceGroup

```java
/** Samples for Clusters GetByResourceGroup. */
public final class ClustersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-11-08/examples/ClusterGet.json
     */
    /**
     * Sample code: Get the cluster.
     *
     * @param manager Entry point to CosmosDBForPostgreSqlManager.
     */
    public static void getTheCluster(
        com.azure.resourcemanager.cosmosdbforpostgresql.CosmosDBForPostgreSqlManager manager) {
        manager
            .clusters()
            .getByResourceGroupWithResponse("TestGroup", "testcluster1", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_List

```java
/** Samples for Clusters List. */
public final class ClustersListSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-11-08/examples/ClusterList.json
     */
    /**
     * Sample code: List all the clusters.
     *
     * @param manager Entry point to CosmosDBForPostgreSqlManager.
     */
    public static void listAllTheClusters(
        com.azure.resourcemanager.cosmosdbforpostgresql.CosmosDBForPostgreSqlManager manager) {
        manager.clusters().list(com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_ListByResourceGroup

```java
/** Samples for Clusters ListByResourceGroup. */
public final class ClustersListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-11-08/examples/ClusterListByResourceGroup.json
     */
    /**
     * Sample code: List the clusters by resource group.
     *
     * @param manager Entry point to CosmosDBForPostgreSqlManager.
     */
    public static void listTheClustersByResourceGroup(
        com.azure.resourcemanager.cosmosdbforpostgresql.CosmosDBForPostgreSqlManager manager) {
        manager.clusters().listByResourceGroup("TestGroup", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_PromoteReadReplica

```java
/** Samples for Clusters PromoteReadReplica. */
public final class ClustersPromoteReadReplicaSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-11-08/examples/ClusterPromoteReadReplica.json
     */
    /**
     * Sample code: Promote read replica cluster to an independent read-write cluster.
     *
     * @param manager Entry point to CosmosDBForPostgreSqlManager.
     */
    public static void promoteReadReplicaClusterToAnIndependentReadWriteCluster(
        com.azure.resourcemanager.cosmosdbforpostgresql.CosmosDBForPostgreSqlManager manager) {
        manager.clusters().promoteReadReplica("TestGroup", "testcluster1", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_Restart

```java
/** Samples for Clusters Restart. */
public final class ClustersRestartSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-11-08/examples/ClusterRestart.json
     */
    /**
     * Sample code: Restart all servers in the cluster.
     *
     * @param manager Entry point to CosmosDBForPostgreSqlManager.
     */
    public static void restartAllServersInTheCluster(
        com.azure.resourcemanager.cosmosdbforpostgresql.CosmosDBForPostgreSqlManager manager) {
        manager.clusters().restart("TestGroup", "testcluster1", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_Start

```java
/** Samples for Clusters Start. */
public final class ClustersStartSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-11-08/examples/ClusterStart.json
     */
    /**
     * Sample code: Start all servers in the cluster.
     *
     * @param manager Entry point to CosmosDBForPostgreSqlManager.
     */
    public static void startAllServersInTheCluster(
        com.azure.resourcemanager.cosmosdbforpostgresql.CosmosDBForPostgreSqlManager manager) {
        manager.clusters().start("TestGroup", "testcluster1", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_Stop

```java
/** Samples for Clusters Stop. */
public final class ClustersStopSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-11-08/examples/ClusterStop.json
     */
    /**
     * Sample code: Stop all servers in the cluster.
     *
     * @param manager Entry point to CosmosDBForPostgreSqlManager.
     */
    public static void stopAllServersInTheCluster(
        com.azure.resourcemanager.cosmosdbforpostgresql.CosmosDBForPostgreSqlManager manager) {
        manager.clusters().stop("TestGroup", "testcluster1", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_Update

```java
import com.azure.resourcemanager.cosmosdbforpostgresql.models.Cluster;
import com.azure.resourcemanager.cosmosdbforpostgresql.models.MaintenanceWindow;

/** Samples for Clusters Update. */
public final class ClustersUpdateSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-11-08/examples/ClusterScaleStorage.json
     */
    /**
     * Sample code: Scale up storage.
     *
     * @param manager Entry point to CosmosDBForPostgreSqlManager.
     */
    public static void scaleUpStorage(
        com.azure.resourcemanager.cosmosdbforpostgresql.CosmosDBForPostgreSqlManager manager) {
        Cluster resource =
            manager
                .clusters()
                .getByResourceGroupWithResponse("TestGroup", "testcluster", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withNodeStorageQuotaInMb(2097152).apply();
    }

    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-11-08/examples/ClusterUpdate.json
     */
    /**
     * Sample code: Update multiple configuration settings of the cluster.
     *
     * @param manager Entry point to CosmosDBForPostgreSqlManager.
     */
    public static void updateMultipleConfigurationSettingsOfTheCluster(
        com.azure.resourcemanager.cosmosdbforpostgresql.CosmosDBForPostgreSqlManager manager) {
        Cluster resource =
            manager
                .clusters()
                .getByResourceGroupWithResponse("TestGroup", "testcluster", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withAdministratorLoginPassword("newpassword")
            .withCoordinatorVCores(16)
            .withNodeCount(4)
            .withNodeVCores(16)
            .apply();
    }

    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-11-08/examples/ClusterScaleCompute.json
     */
    /**
     * Sample code: Scale compute up or down.
     *
     * @param manager Entry point to CosmosDBForPostgreSqlManager.
     */
    public static void scaleComputeUpOrDown(
        com.azure.resourcemanager.cosmosdbforpostgresql.CosmosDBForPostgreSqlManager manager) {
        Cluster resource =
            manager
                .clusters()
                .getByResourceGroupWithResponse("TestGroup", "testcluster", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withNodeVCores(16).apply();
    }

    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-11-08/examples/ClusterAddNode.json
     */
    /**
     * Sample code: Scale out: Add new worker nodes.
     *
     * @param manager Entry point to CosmosDBForPostgreSqlManager.
     */
    public static void scaleOutAddNewWorkerNodes(
        com.azure.resourcemanager.cosmosdbforpostgresql.CosmosDBForPostgreSqlManager manager) {
        Cluster resource =
            manager
                .clusters()
                .getByResourceGroupWithResponse("TestGroup", "testcluster", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withNodeCount(2).apply();
    }

    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-11-08/examples/ClusterUpdateMaintenanceWindow.json
     */
    /**
     * Sample code: Update or define maintenance window.
     *
     * @param manager Entry point to CosmosDBForPostgreSqlManager.
     */
    public static void updateOrDefineMaintenanceWindow(
        com.azure.resourcemanager.cosmosdbforpostgresql.CosmosDBForPostgreSqlManager manager) {
        Cluster resource =
            manager
                .clusters()
                .getByResourceGroupWithResponse("TestGroup", "testcluster", com.azure.core.util.Context.NONE)
                .getValue();
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
}
```

### Configurations_Get

```java
/** Samples for Configurations Get. */
public final class ConfigurationsGetSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-11-08/examples/ConfigurationGet.json
     */
    /**
     * Sample code: Get configuration details.
     *
     * @param manager Entry point to CosmosDBForPostgreSqlManager.
     */
    public static void getConfigurationDetails(
        com.azure.resourcemanager.cosmosdbforpostgresql.CosmosDBForPostgreSqlManager manager) {
        manager
            .configurations()
            .getWithResponse("TestResourceGroup", "testcluster", "client_encoding", com.azure.core.util.Context.NONE);
    }
}
```

### Configurations_GetCoordinator

```java
/** Samples for Configurations GetCoordinator. */
public final class ConfigurationsGetCoordinatorSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-11-08/examples/ConfigurationGetCoordinator.json
     */
    /**
     * Sample code: Get configuration details for coordinator.
     *
     * @param manager Entry point to CosmosDBForPostgreSqlManager.
     */
    public static void getConfigurationDetailsForCoordinator(
        com.azure.resourcemanager.cosmosdbforpostgresql.CosmosDBForPostgreSqlManager manager) {
        manager
            .configurations()
            .getCoordinatorWithResponse(
                "TestResourceGroup", "testcluster", "array_nulls", com.azure.core.util.Context.NONE);
    }
}
```

### Configurations_GetNode

```java
/** Samples for Configurations GetNode. */
public final class ConfigurationsGetNodeSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-11-08/examples/ConfigurationGetNode.json
     */
    /**
     * Sample code: Get configuration details for node.
     *
     * @param manager Entry point to CosmosDBForPostgreSqlManager.
     */
    public static void getConfigurationDetailsForNode(
        com.azure.resourcemanager.cosmosdbforpostgresql.CosmosDBForPostgreSqlManager manager) {
        manager
            .configurations()
            .getNodeWithResponse("TestResourceGroup", "testcluster", "array_nulls", com.azure.core.util.Context.NONE);
    }
}
```

### Configurations_ListByCluster

```java
/** Samples for Configurations ListByCluster. */
public final class ConfigurationsListByClusterSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-11-08/examples/ConfigurationListByCluster.json
     */
    /**
     * Sample code: List configurations of the cluster.
     *
     * @param manager Entry point to CosmosDBForPostgreSqlManager.
     */
    public static void listConfigurationsOfTheCluster(
        com.azure.resourcemanager.cosmosdbforpostgresql.CosmosDBForPostgreSqlManager manager) {
        manager.configurations().listByCluster("TestResourceGroup", "testcluster", com.azure.core.util.Context.NONE);
    }
}
```

### Configurations_ListByServer

```java
/** Samples for Configurations ListByServer. */
public final class ConfigurationsListByServerSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-11-08/examples/ConfigurationListByServer.json
     */
    /**
     * Sample code: List configurations of the server that in the cluster.
     *
     * @param manager Entry point to CosmosDBForPostgreSqlManager.
     */
    public static void listConfigurationsOfTheServerThatInTheCluster(
        com.azure.resourcemanager.cosmosdbforpostgresql.CosmosDBForPostgreSqlManager manager) {
        manager
            .configurations()
            .listByServer("TestResourceGroup", "testcluster", "testserver", com.azure.core.util.Context.NONE);
    }
}
```

### Configurations_UpdateOnCoordinator

```java
import com.azure.resourcemanager.cosmosdbforpostgresql.fluent.models.ServerConfigurationInner;

/** Samples for Configurations UpdateOnCoordinator. */
public final class ConfigurationsUpdateOnCoordinatorSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-11-08/examples/ConfigurationUpdateCoordinator.json
     */
    /**
     * Sample code: Update single configuration of coordinator.
     *
     * @param manager Entry point to CosmosDBForPostgreSqlManager.
     */
    public static void updateSingleConfigurationOfCoordinator(
        com.azure.resourcemanager.cosmosdbforpostgresql.CosmosDBForPostgreSqlManager manager) {
        manager
            .configurations()
            .updateOnCoordinator(
                "TestResourceGroup",
                "testcluster",
                "array_nulls",
                new ServerConfigurationInner().withValue("on"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Configurations_UpdateOnNode

```java
import com.azure.resourcemanager.cosmosdbforpostgresql.fluent.models.ServerConfigurationInner;

/** Samples for Configurations UpdateOnNode. */
public final class ConfigurationsUpdateOnNodeSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-11-08/examples/ConfigurationUpdateNode.json
     */
    /**
     * Sample code: Update single configuration of nodes.
     *
     * @param manager Entry point to CosmosDBForPostgreSqlManager.
     */
    public static void updateSingleConfigurationOfNodes(
        com.azure.resourcemanager.cosmosdbforpostgresql.CosmosDBForPostgreSqlManager manager) {
        manager
            .configurations()
            .updateOnNode(
                "TestResourceGroup",
                "testcluster",
                "array_nulls",
                new ServerConfigurationInner().withValue("off"),
                com.azure.core.util.Context.NONE);
    }
}
```

### FirewallRules_CreateOrUpdate

```java
/** Samples for FirewallRules CreateOrUpdate. */
public final class FirewallRulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-11-08/examples/FirewallRuleCreate.json
     */
    /**
     * Sample code: Create a firewall rule of the cluster.
     *
     * @param manager Entry point to CosmosDBForPostgreSqlManager.
     */
    public static void createAFirewallRuleOfTheCluster(
        com.azure.resourcemanager.cosmosdbforpostgresql.CosmosDBForPostgreSqlManager manager) {
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
/** Samples for FirewallRules Delete. */
public final class FirewallRulesDeleteSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-11-08/examples/FirewallRuleDelete.json
     */
    /**
     * Sample code: Delete the firewall rule of the cluster.
     *
     * @param manager Entry point to CosmosDBForPostgreSqlManager.
     */
    public static void deleteTheFirewallRuleOfTheCluster(
        com.azure.resourcemanager.cosmosdbforpostgresql.CosmosDBForPostgreSqlManager manager) {
        manager.firewallRules().delete("TestGroup", "pgtestsvc4", "rule1", com.azure.core.util.Context.NONE);
    }
}
```

### FirewallRules_Get

```java
/** Samples for FirewallRules Get. */
public final class FirewallRulesGetSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-11-08/examples/FirewallRuleGet.json
     */
    /**
     * Sample code: Get the firewall rule of the cluster.
     *
     * @param manager Entry point to CosmosDBForPostgreSqlManager.
     */
    public static void getTheFirewallRuleOfTheCluster(
        com.azure.resourcemanager.cosmosdbforpostgresql.CosmosDBForPostgreSqlManager manager) {
        manager.firewallRules().getWithResponse("TestGroup", "pgtestsvc4", "rule1", com.azure.core.util.Context.NONE);
    }
}
```

### FirewallRules_ListByCluster

```java
/** Samples for FirewallRules ListByCluster. */
public final class FirewallRulesListByClusterSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-11-08/examples/FirewallRuleListByCluster.json
     */
    /**
     * Sample code: List firewall rules of the cluster.
     *
     * @param manager Entry point to CosmosDBForPostgreSqlManager.
     */
    public static void listFirewallRulesOfTheCluster(
        com.azure.resourcemanager.cosmosdbforpostgresql.CosmosDBForPostgreSqlManager manager) {
        manager.firewallRules().listByCluster("TestGroup", "pgtestsvc4", com.azure.core.util.Context.NONE);
    }
}
```

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-11-08/examples/OperationList.json
     */
    /**
     * Sample code: List all available operations.
     *
     * @param manager Entry point to CosmosDBForPostgreSqlManager.
     */
    public static void listAllAvailableOperations(
        com.azure.resourcemanager.cosmosdbforpostgresql.CosmosDBForPostgreSqlManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_CreateOrUpdate

```java
import com.azure.resourcemanager.cosmosdbforpostgresql.models.PrivateEndpointServiceConnectionStatus;
import com.azure.resourcemanager.cosmosdbforpostgresql.models.PrivateLinkServiceConnectionState;

/** Samples for PrivateEndpointConnections CreateOrUpdate. */
public final class PrivateEndpointConnectionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-11-08/examples/PrivateEndpointConnectionCreateOrUpdate.json
     */
    /**
     * Sample code: Approves or Rejects a Private Endpoint Connection with a given name.
     *
     * @param manager Entry point to CosmosDBForPostgreSqlManager.
     */
    public static void approvesOrRejectsAPrivateEndpointConnectionWithAGivenName(
        com.azure.resourcemanager.cosmosdbforpostgresql.CosmosDBForPostgreSqlManager manager) {
        manager
            .privateEndpointConnections()
            .define("private-endpoint-connection-name")
            .withExistingServerGroupsv2("TestGroup", "testcluster")
            .withPrivateLinkServiceConnectionState(
                new PrivateLinkServiceConnectionState()
                    .withStatus(PrivateEndpointServiceConnectionStatus.APPROVED)
                    .withDescription("Approved by johndoe@contoso.com"))
            .create();
    }
}
```

### PrivateEndpointConnections_Delete

```java
/** Samples for PrivateEndpointConnections Delete. */
public final class PrivateEndpointConnectionsDeleteSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-11-08/examples/PrivateEndpointConnectionsDelete.json
     */
    /**
     * Sample code: Deletes a private endpoint connection with a given name.
     *
     * @param manager Entry point to CosmosDBForPostgreSqlManager.
     */
    public static void deletesAPrivateEndpointConnectionWithAGivenName(
        com.azure.resourcemanager.cosmosdbforpostgresql.CosmosDBForPostgreSqlManager manager) {
        manager
            .privateEndpointConnections()
            .delete("TestGroup", "testcluster", "private-endpoint-connection-name", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_Get

```java
/** Samples for PrivateEndpointConnections Get. */
public final class PrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-11-08/examples/PrivateEndpointConnectionsGet.json
     */
    /**
     * Sample code: Gets private endpoint connection.
     *
     * @param manager Entry point to CosmosDBForPostgreSqlManager.
     */
    public static void getsPrivateEndpointConnection(
        com.azure.resourcemanager.cosmosdbforpostgresql.CosmosDBForPostgreSqlManager manager) {
        manager
            .privateEndpointConnections()
            .getWithResponse(
                "TestGroup", "testcluster", "private-endpoint-connection-name", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_ListByCluster

```java
/** Samples for PrivateEndpointConnections ListByCluster. */
public final class PrivateEndpointConnectionsListByClusterSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-11-08/examples/PrivateEndpointConnectionsListByCluster.json
     */
    /**
     * Sample code: Gets list of private endpoint connections on a cluster.
     *
     * @param manager Entry point to CosmosDBForPostgreSqlManager.
     */
    public static void getsListOfPrivateEndpointConnectionsOnACluster(
        com.azure.resourcemanager.cosmosdbforpostgresql.CosmosDBForPostgreSqlManager manager) {
        manager
            .privateEndpointConnections()
            .listByCluster("TestResourceGroup", "testcluster", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkResources_Get

```java
/** Samples for PrivateLinkResources Get. */
public final class PrivateLinkResourcesGetSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-11-08/examples/PrivateLinkResourcesGet.json
     */
    /**
     * Sample code: Gets a private link resource for cluster.
     *
     * @param manager Entry point to CosmosDBForPostgreSqlManager.
     */
    public static void getsAPrivateLinkResourceForCluster(
        com.azure.resourcemanager.cosmosdbforpostgresql.CosmosDBForPostgreSqlManager manager) {
        manager
            .privateLinkResources()
            .getWithResponse("TestGroup", "testcluster", "plr", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkResources_ListByCluster

```java
/** Samples for PrivateLinkResources ListByCluster. */
public final class PrivateLinkResourcesListByClusterSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-11-08/examples/PrivateLinkResourceListByCluster.json
     */
    /**
     * Sample code: Gets the private link resources for cluster.
     *
     * @param manager Entry point to CosmosDBForPostgreSqlManager.
     */
    public static void getsThePrivateLinkResourcesForCluster(
        com.azure.resourcemanager.cosmosdbforpostgresql.CosmosDBForPostgreSqlManager manager) {
        manager
            .privateLinkResources()
            .listByCluster("TestResourceGroup", "testcluster", com.azure.core.util.Context.NONE);
    }
}
```

### Roles_Create

```java
/** Samples for Roles Create. */
public final class RolesCreateSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-11-08/examples/RoleCreate.json
     */
    /**
     * Sample code: RoleCreate.
     *
     * @param manager Entry point to CosmosDBForPostgreSqlManager.
     */
    public static void roleCreate(
        com.azure.resourcemanager.cosmosdbforpostgresql.CosmosDBForPostgreSqlManager manager) {
        manager
            .roles()
            .define("role1")
            .withExistingServerGroupsv2("TestGroup", "pgtestsvc4")
            .withPassword("password")
            .create();
    }
}
```

### Roles_Delete

```java
/** Samples for Roles Delete. */
public final class RolesDeleteSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-11-08/examples/RoleDelete.json
     */
    /**
     * Sample code: RoleDelete.
     *
     * @param manager Entry point to CosmosDBForPostgreSqlManager.
     */
    public static void roleDelete(
        com.azure.resourcemanager.cosmosdbforpostgresql.CosmosDBForPostgreSqlManager manager) {
        manager.roles().delete("TestGroup", "pgtestsvc4", "role1", com.azure.core.util.Context.NONE);
    }
}
```

### Roles_Get

```java
/** Samples for Roles Get. */
public final class RolesGetSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-11-08/examples/RoleGet.json
     */
    /**
     * Sample code: Get the role of the cluster.
     *
     * @param manager Entry point to CosmosDBForPostgreSqlManager.
     */
    public static void getTheRoleOfTheCluster(
        com.azure.resourcemanager.cosmosdbforpostgresql.CosmosDBForPostgreSqlManager manager) {
        manager.roles().getWithResponse("TestGroup", "pgtestsvc4", "role1", com.azure.core.util.Context.NONE);
    }
}
```

### Roles_ListByCluster

```java
/** Samples for Roles ListByCluster. */
public final class RolesListByClusterSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-11-08/examples/RoleListByCluster.json
     */
    /**
     * Sample code: RoleList.
     *
     * @param manager Entry point to CosmosDBForPostgreSqlManager.
     */
    public static void roleList(com.azure.resourcemanager.cosmosdbforpostgresql.CosmosDBForPostgreSqlManager manager) {
        manager.roles().listByCluster("TestGroup", "pgtestsvc4", com.azure.core.util.Context.NONE);
    }
}
```

### Servers_Get

```java
/** Samples for Servers Get. */
public final class ServersGetSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-11-08/examples/ServerGet.json
     */
    /**
     * Sample code: Get the server of cluster.
     *
     * @param manager Entry point to CosmosDBForPostgreSqlManager.
     */
    public static void getTheServerOfCluster(
        com.azure.resourcemanager.cosmosdbforpostgresql.CosmosDBForPostgreSqlManager manager) {
        manager
            .servers()
            .getWithResponse("TestGroup", "testcluster1", "testcluster1-c", com.azure.core.util.Context.NONE);
    }
}
```

### Servers_ListByCluster

```java
/** Samples for Servers ListByCluster. */
public final class ServersListByClusterSamples {
    /*
     * x-ms-original-file: specification/postgresqlhsc/resource-manager/Microsoft.DBforPostgreSQL/stable/2022-11-08/examples/ServerListByCluster.json
     */
    /**
     * Sample code: List servers of the cluster.
     *
     * @param manager Entry point to CosmosDBForPostgreSqlManager.
     */
    public static void listServersOfTheCluster(
        com.azure.resourcemanager.cosmosdbforpostgresql.CosmosDBForPostgreSqlManager manager) {
        manager.servers().listByCluster("TestGroup", "testcluster1", com.azure.core.util.Context.NONE);
    }
}
```

