# Code snippets and samples


## HorizonDbClusters

- [CreateOrUpdate](#horizondbclusters_createorupdate)
- [Delete](#horizondbclusters_delete)
- [GetByResourceGroup](#horizondbclusters_getbyresourcegroup)
- [List](#horizondbclusters_list)
- [ListByResourceGroup](#horizondbclusters_listbyresourcegroup)
- [Update](#horizondbclusters_update)

## HorizonDbFirewallRules

- [CreateOrUpdate](#horizondbfirewallrules_createorupdate)
- [Delete](#horizondbfirewallrules_delete)
- [Get](#horizondbfirewallrules_get)
- [List](#horizondbfirewallrules_list)

## HorizonDbParameterGroups

- [CreateOrUpdate](#horizondbparametergroups_createorupdate)
- [Delete](#horizondbparametergroups_delete)
- [GetByResourceGroup](#horizondbparametergroups_getbyresourcegroup)
- [List](#horizondbparametergroups_list)
- [ListByResourceGroup](#horizondbparametergroups_listbyresourcegroup)
- [ListConnections](#horizondbparametergroups_listconnections)
- [ListVersions](#horizondbparametergroups_listversions)
- [Update](#horizondbparametergroups_update)

## HorizonDbPools

- [Get](#horizondbpools_get)
- [List](#horizondbpools_list)

## HorizonDbPrivateEndpointConnections

- [Delete](#horizondbprivateendpointconnections_delete)
- [Get](#horizondbprivateendpointconnections_get)
- [List](#horizondbprivateendpointconnections_list)
- [Update](#horizondbprivateendpointconnections_update)

## HorizonDbPrivateLinkResources

- [Get](#horizondbprivatelinkresources_get)
- [List](#horizondbprivatelinkresources_list)

## HorizonDbReplicas

- [CreateOrUpdate](#horizondbreplicas_createorupdate)
- [Delete](#horizondbreplicas_delete)
- [Get](#horizondbreplicas_get)
- [List](#horizondbreplicas_list)
- [Update](#horizondbreplicas_update)

## Operations

- [List](#operations_list)
### HorizonDbClusters_CreateOrUpdate

```java
import com.azure.resourcemanager.horizondb.models.CreateModeCluster;
import com.azure.resourcemanager.horizondb.models.HorizonDbClusterProperties;
import com.azure.resourcemanager.horizondb.models.ZonePlacementPolicy;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for HorizonDbClusters CreateOrUpdate.
 */
public final class HorizonDbClustersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-01-20-preview/Clusters_CreateOrUpdate.json
     */
    /**
     * Sample code: Create or update a HorizonDb cluster.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void createOrUpdateAHorizonDbCluster(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.horizonDbClusters()
            .define("examplecluster")
            .withRegion("westus2")
            .withExistingResourceGroup("exampleresourcegroup")
            .withTags(mapOf("env", "dev"))
            .withProperties(new HorizonDbClusterProperties().withAdministratorLogin("exampleadministratorlogin")
                .withAdministratorLoginPassword("fakeTokenPlaceholder")
                .withVersion("17")
                .withCreateMode(CreateModeCluster.CREATE)
                .withSourceClusterResourceId(
                    "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/examplesourceresourcegroup/providers/Microsoft.HorizonDb/clusters/examplesourcecluster")
                .withReplicaCount(2)
                .withVCores(4)
                .withZonePlacementPolicy(ZonePlacementPolicy.STRICT))
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

### HorizonDbClusters_Delete

```java
/**
 * Samples for HorizonDbClusters Delete.
 */
public final class HorizonDbClustersDeleteSamples {
    /*
     * x-ms-original-file: 2026-01-20-preview/Clusters_Delete.json
     */
    /**
     * Sample code: Delete a HorizonDb cluster.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void deleteAHorizonDbCluster(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.horizonDbClusters().delete("exampleresourcegroup", "examplecluster", com.azure.core.util.Context.NONE);
    }
}
```

### HorizonDbClusters_GetByResourceGroup

```java
/**
 * Samples for HorizonDbClusters GetByResourceGroup.
 */
public final class HorizonDbClustersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-01-20-preview/Clusters_Get.json
     */
    /**
     * Sample code: Get a HorizonDb cluster.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void getAHorizonDbCluster(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.horizonDbClusters()
            .getByResourceGroupWithResponse("exampleresourcegroup", "examplecluster", com.azure.core.util.Context.NONE);
    }
}
```

### HorizonDbClusters_List

```java
/**
 * Samples for HorizonDbClusters List.
 */
public final class HorizonDbClustersListSamples {
    /*
     * x-ms-original-file: 2026-01-20-preview/Clusters_ListBySubscription.json
     */
    /**
     * Sample code: List HorizonDb clusters by subscription.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        listHorizonDbClustersBySubscription(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.horizonDbClusters().list(com.azure.core.util.Context.NONE);
    }
}
```

### HorizonDbClusters_ListByResourceGroup

```java
/**
 * Samples for HorizonDbClusters ListByResourceGroup.
 */
public final class HorizonDbClustersListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-01-20-preview/Clusters_ListByResourceGroup.json
     */
    /**
     * Sample code: List HorizonDb clusters in a resource group.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        listHorizonDbClustersInAResourceGroup(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.horizonDbClusters().listByResourceGroup("exampleresourcegroup", com.azure.core.util.Context.NONE);
    }
}
```

### HorizonDbClusters_Update

```java
import com.azure.resourcemanager.horizondb.models.HorizonDbCluster;
import com.azure.resourcemanager.horizondb.models.HorizonDbClusterPropertiesForPatchUpdate;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for HorizonDbClusters Update.
 */
public final class HorizonDbClustersUpdateSamples {
    /*
     * x-ms-original-file: 2026-01-20-preview/Clusters_Update.json
     */
    /**
     * Sample code: Update a HorizonDb cluster.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void updateAHorizonDbCluster(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        HorizonDbCluster resource = manager.horizonDbClusters()
            .getByResourceGroupWithResponse("exampleresourcegroup", "examplecluster", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("team", "updated-data-platform"))
            .withProperties(
                new HorizonDbClusterPropertiesForPatchUpdate().withAdministratorLoginPassword("fakeTokenPlaceholder")
                    .withVCores(8))
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

### HorizonDbFirewallRules_CreateOrUpdate

```java
import com.azure.resourcemanager.horizondb.models.HorizonDbFirewallRuleProperties;

/**
 * Samples for HorizonDbFirewallRules CreateOrUpdate.
 */
public final class HorizonDbFirewallRulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-01-20-preview/FirewallRules_CreateOrUpdate.json
     */
    /**
     * Sample code: Create or update a HorizonDb firewall rule.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        createOrUpdateAHorizonDbFirewallRule(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.horizonDbFirewallRules()
            .define("examplefirewallrule")
            .withExistingPool("exampleresourcegroup", "examplecluster", "examplepool")
            .withProperties(new HorizonDbFirewallRuleProperties().withStartIpAddress("10.0.0.1")
                .withEndIpAddress("10.0.0.10")
                .withDescription("Allow access from corporate network"))
            .create();
    }
}
```

### HorizonDbFirewallRules_Delete

```java
/**
 * Samples for HorizonDbFirewallRules Delete.
 */
public final class HorizonDbFirewallRulesDeleteSamples {
    /*
     * x-ms-original-file: 2026-01-20-preview/FirewallRules_Delete.json
     */
    /**
     * Sample code: Delete a HorizonDb firewall rule.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void deleteAHorizonDbFirewallRule(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.horizonDbFirewallRules()
            .delete("exampleresourcegroup", "examplecluster", "examplepool", "examplefirewallrule",
                com.azure.core.util.Context.NONE);
    }
}
```

### HorizonDbFirewallRules_Get

```java
/**
 * Samples for HorizonDbFirewallRules Get.
 */
public final class HorizonDbFirewallRulesGetSamples {
    /*
     * x-ms-original-file: 2026-01-20-preview/FirewallRules_Get.json
     */
    /**
     * Sample code: Get a HorizonDb firewall rule.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void getAHorizonDbFirewallRule(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.horizonDbFirewallRules()
            .getWithResponse("exampleresourcegroup", "examplecluster", "examplepool", "examplefirewallrule",
                com.azure.core.util.Context.NONE);
    }
}
```

### HorizonDbFirewallRules_List

```java
/**
 * Samples for HorizonDbFirewallRules List.
 */
public final class HorizonDbFirewallRulesListSamples {
    /*
     * x-ms-original-file: 2026-01-20-preview/FirewallRules_List.json
     */
    /**
     * Sample code: List HorizonDb firewall rules in a pool.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void listHorizonDbFirewallRulesInAPool(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.horizonDbFirewallRules()
            .list("exampleresourcegroup", "examplecluster", "examplepool", com.azure.core.util.Context.NONE);
    }
}
```

### HorizonDbParameterGroups_CreateOrUpdate

```java
import com.azure.resourcemanager.horizondb.models.HorizonDbParameterGroupProperties;
import com.azure.resourcemanager.horizondb.models.ParameterProperties;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for HorizonDbParameterGroups CreateOrUpdate.
 */
public final class HorizonDbParameterGroupsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-01-20-preview/ParameterGroups_CreateOrUpdate.json
     */
    /**
     * Sample code: Create or update a HorizonDb parameter group.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        createOrUpdateAHorizonDbParameterGroup(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.horizonDbParameterGroups()
            .define("exampleparametergroup")
            .withRegion("westus2")
            .withExistingResourceGroup("exampleresourcegroup")
            .withTags(mapOf("env", "dev", "team", "data-platform"))
            .withProperties(new HorizonDbParameterGroupProperties()
                .withParameters(Arrays.asList(new ParameterProperties().withName("max_connections").withValue("200"),
                    new ParameterProperties().withName("log_min_error_statement").withValue("error"),
                    new ParameterProperties().withName("shared_buffers").withValue("2000")))
                .withDescription("Parameter group for high-throughput workloads")
                .withPgVersion(17)
                .withApplyImmediately(true))
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

### HorizonDbParameterGroups_Delete

```java
/**
 * Samples for HorizonDbParameterGroups Delete.
 */
public final class HorizonDbParameterGroupsDeleteSamples {
    /*
     * x-ms-original-file: 2026-01-20-preview/ParameterGroups_Delete.json
     */
    /**
     * Sample code: Delete a HorizonDb parameter group.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void deleteAHorizonDbParameterGroup(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.horizonDbParameterGroups()
            .delete("exampleresourcegroup", "exampleparametergroup", com.azure.core.util.Context.NONE);
    }
}
```

### HorizonDbParameterGroups_GetByResourceGroup

```java
/**
 * Samples for HorizonDbParameterGroups GetByResourceGroup.
 */
public final class HorizonDbParameterGroupsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-01-20-preview/ParameterGroups_Get.json
     */
    /**
     * Sample code: Get a HorizonDb parameter group.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void getAHorizonDbParameterGroup(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.horizonDbParameterGroups()
            .getByResourceGroupWithResponse("exampleresourcegroup", "exampleparametergroup",
                com.azure.core.util.Context.NONE);
    }
}
```

### HorizonDbParameterGroups_List

```java
/**
 * Samples for HorizonDbParameterGroups List.
 */
public final class HorizonDbParameterGroupsListSamples {
    /*
     * x-ms-original-file: 2026-01-20-preview/ParameterGroups_ListBySubscription.json
     */
    /**
     * Sample code: List HorizonDb parameter groups in a subscription.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        listHorizonDbParameterGroupsInASubscription(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.horizonDbParameterGroups().list(com.azure.core.util.Context.NONE);
    }
}
```

### HorizonDbParameterGroups_ListByResourceGroup

```java
/**
 * Samples for HorizonDbParameterGroups ListByResourceGroup.
 */
public final class HorizonDbParameterGroupsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-01-20-preview/ParameterGroups_ListByResourceGroup.json
     */
    /**
     * Sample code: List HorizonDb parameter groups in a resource group.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        listHorizonDbParameterGroupsInAResourceGroup(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.horizonDbParameterGroups()
            .listByResourceGroup("exampleresourcegroup", com.azure.core.util.Context.NONE);
    }
}
```

### HorizonDbParameterGroups_ListConnections

```java
/**
 * Samples for HorizonDbParameterGroups ListConnections.
 */
public final class HorizonDbParameterGroupsListConnectionsSamples {
    /*
     * x-ms-original-file: 2026-01-20-preview/ParameterGroups_ListConnections.json
     */
    /**
     * Sample code: List connections for a HorizonDb parameter group.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        listConnectionsForAHorizonDbParameterGroup(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.horizonDbParameterGroups()
            .listConnections("exampleresourcegroup", "exampleparametergroup", com.azure.core.util.Context.NONE);
    }
}
```

### HorizonDbParameterGroups_ListVersions

```java
/**
 * Samples for HorizonDbParameterGroups ListVersions.
 */
public final class HorizonDbParameterGroupsListVersionsSamples {
    /*
     * x-ms-original-file: 2026-01-20-preview/ParameterGroups_ListVersions.json
     */
    /**
     * Sample code: List parameter groups filtered by version.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        listParameterGroupsFilteredByVersion(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.horizonDbParameterGroups()
            .listVersions("exampleresourcegroup", "exampleparametergroup", 22, com.azure.core.util.Context.NONE);
    }
}
```

### HorizonDbParameterGroups_Update

```java
import com.azure.resourcemanager.horizondb.models.HorizonDbParameterGroup;
import com.azure.resourcemanager.horizondb.models.HorizonDbParameterGroupPropertiesForPatchUpdate;
import com.azure.resourcemanager.horizondb.models.ParameterProperties;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for HorizonDbParameterGroups Update.
 */
public final class HorizonDbParameterGroupsUpdateSamples {
    /*
     * x-ms-original-file: 2026-01-20-preview/ParameterGroups_Update.json
     */
    /**
     * Sample code: Update a HorizonDb parameter group.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void updateAHorizonDbParameterGroup(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        HorizonDbParameterGroup resource = manager.horizonDbParameterGroups()
            .getByResourceGroupWithResponse("exampleresourcegroup", "exampleparametergroup",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("team", "updated-data-platform"))
            .withProperties(new HorizonDbParameterGroupPropertiesForPatchUpdate()
                .withParameters(Arrays.asList(new ParameterProperties().withName("max_connections").withValue("300"),
                    new ParameterProperties().withName("log_min_error_statement").withValue("warning")))
                .withDescription("Updated parameter group for high-throughput workloads")
                .withApplyImmediately(true))
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

### HorizonDbPools_Get

```java
/**
 * Samples for HorizonDbPools Get.
 */
public final class HorizonDbPoolsGetSamples {
    /*
     * x-ms-original-file: 2026-01-20-preview/Pools_Get.json
     */
    /**
     * Sample code: Get a HorizonDb pool.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void getAHorizonDbPool(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.horizonDbPools()
            .getWithResponse("exampleresourcegroup", "examplecluster", "examplepool", com.azure.core.util.Context.NONE);
    }
}
```

### HorizonDbPools_List

```java
/**
 * Samples for HorizonDbPools List.
 */
public final class HorizonDbPoolsListSamples {
    /*
     * x-ms-original-file: 2026-01-20-preview/Pools_List.json
     */
    /**
     * Sample code: List HorizonDb pools in a cluster.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void listHorizonDbPoolsInACluster(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.horizonDbPools().list("exampleresourcegroup", "examplecluster", com.azure.core.util.Context.NONE);
    }
}
```

### HorizonDbPrivateEndpointConnections_Delete

```java
/**
 * Samples for HorizonDbPrivateEndpointConnections Delete.
 */
public final class HorizonDbPrivateEndpointConnectionsDeleteSamples {
    /*
     * x-ms-original-file: 2026-01-20-preview/PrivateEndpointConnections_Delete.json
     */
    /**
     * Sample code: Delete a private endpoint connection.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void deleteAPrivateEndpointConnection(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.horizonDbPrivateEndpointConnections()
            .delete("exampleresourcegroup", "exampleprivateendpointconnection.1fa229cd-bf3f-47f0-8c49-afb36723997e",
                com.azure.core.util.Context.NONE);
    }
}
```

### HorizonDbPrivateEndpointConnections_Get

```java
/**
 * Samples for HorizonDbPrivateEndpointConnections Get.
 */
public final class HorizonDbPrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file: 2026-01-20-preview/PrivateEndpointConnections_Get.json
     */
    /**
     * Sample code: Get a private endpoint connection.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void getAPrivateEndpointConnection(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.horizonDbPrivateEndpointConnections()
            .getWithResponse("exampleresourcegroup", "examplecluster",
                "exampleprivateendpointconnection.1fa229cd-bf3f-47f0-8c49-afb36723997e",
                com.azure.core.util.Context.NONE);
    }
}
```

### HorizonDbPrivateEndpointConnections_List

```java
/**
 * Samples for HorizonDbPrivateEndpointConnections List.
 */
public final class HorizonDbPrivateEndpointConnectionsListSamples {
    /*
     * x-ms-original-file: 2026-01-20-preview/PrivateEndpointConnections_List.json
     */
    /**
     * Sample code: List all private endpoint connections on a cluster.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        listAllPrivateEndpointConnectionsOnACluster(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.horizonDbPrivateEndpointConnections()
            .list("exampleresourcegroup", "examplecluster", com.azure.core.util.Context.NONE);
    }
}
```

### HorizonDbPrivateEndpointConnections_Update

```java
import com.azure.resourcemanager.horizondb.models.OptionalPropertiesUpdateableProperties;
import com.azure.resourcemanager.horizondb.models.PrivateEndpointConnectionUpdate;
import com.azure.resourcemanager.horizondb.models.PrivateEndpointServiceConnectionStatus;
import com.azure.resourcemanager.horizondb.models.PrivateLinkServiceConnectionState;

/**
 * Samples for HorizonDbPrivateEndpointConnections Update.
 */
public final class HorizonDbPrivateEndpointConnectionsUpdateSamples {
    /*
     * x-ms-original-file: 2026-01-20-preview/PrivateEndpointConnections_Update.json
     */
    /**
     * Sample code: Approve or reject a private endpoint connection.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        approveOrRejectAPrivateEndpointConnection(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.horizonDbPrivateEndpointConnections()
            .update("exampleresourcegroup", "exampleprivateendpointconnection.1fa229cd-bf3f-47f0-8c49-afb36723997e",
                new PrivateEndpointConnectionUpdate().withProperties(new OptionalPropertiesUpdateableProperties()
                    .withPrivateLinkServiceConnectionState(new PrivateLinkServiceConnectionState()
                        .withStatus(PrivateEndpointServiceConnectionStatus.APPROVED)
                        .withDescription("Approved by johndoe@contoso.com"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### HorizonDbPrivateLinkResources_Get

```java
/**
 * Samples for HorizonDbPrivateLinkResources Get.
 */
public final class HorizonDbPrivateLinkResourcesGetSamples {
    /*
     * x-ms-original-file: 2026-01-20-preview/PrivateLinkResources_Get.json
     */
    /**
     * Sample code: Gets a private link resource for HorizonDb.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        getsAPrivateLinkResourceForHorizonDb(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.horizonDbPrivateLinkResources()
            .getWithResponse("exampleresourcegroup", "examplecluster", "default", com.azure.core.util.Context.NONE);
    }
}
```

### HorizonDbPrivateLinkResources_List

```java
/**
 * Samples for HorizonDbPrivateLinkResources List.
 */
public final class HorizonDbPrivateLinkResourcesListSamples {
    /*
     * x-ms-original-file: 2026-01-20-preview/PrivateLinkResources_List.json
     */
    /**
     * Sample code: Gets private link resources for HorizonDb.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        getsPrivateLinkResourcesForHorizonDb(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.horizonDbPrivateLinkResources()
            .list("exampleresourcegroup", "examplecluster", com.azure.core.util.Context.NONE);
    }
}
```

### HorizonDbReplicas_CreateOrUpdate

```java
import com.azure.resourcemanager.horizondb.models.HorizonDbReplicaProperties;
import com.azure.resourcemanager.horizondb.models.ReplicaRole;

/**
 * Samples for HorizonDbReplicas CreateOrUpdate.
 */
public final class HorizonDbReplicasCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-01-20-preview/Replicas_CreateOrUpdate.json
     */
    /**
     * Sample code: Create or update a HorizonDb replica.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void createOrUpdateAHorizonDbReplica(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.horizonDbReplicas()
            .define("examplereplica")
            .withExistingPool("exampleresourcegroup", "examplecluster", "examplepool")
            .withProperties(new HorizonDbReplicaProperties().withRole(ReplicaRole.READ).withAvailabilityZone("1"))
            .create();
    }
}
```

### HorizonDbReplicas_Delete

```java
/**
 * Samples for HorizonDbReplicas Delete.
 */
public final class HorizonDbReplicasDeleteSamples {
    /*
     * x-ms-original-file: 2026-01-20-preview/Replicas_Delete.json
     */
    /**
     * Sample code: Delete a HorizonDb replica.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void deleteAHorizonDbReplica(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.horizonDbReplicas()
            .delete("exampleresourcegroup", "examplecluster", "examplepool", "examplereplica",
                com.azure.core.util.Context.NONE);
    }
}
```

### HorizonDbReplicas_Get

```java
/**
 * Samples for HorizonDbReplicas Get.
 */
public final class HorizonDbReplicasGetSamples {
    /*
     * x-ms-original-file: 2026-01-20-preview/Replicas_Get.json
     */
    /**
     * Sample code: Get a HorizonDb replica.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void getAHorizonDbReplica(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.horizonDbReplicas()
            .getWithResponse("exampleresourcegroup", "examplecluster", "examplepool", "examplereplica",
                com.azure.core.util.Context.NONE);
    }
}
```

### HorizonDbReplicas_List

```java
/**
 * Samples for HorizonDbReplicas List.
 */
public final class HorizonDbReplicasListSamples {
    /*
     * x-ms-original-file: 2026-01-20-preview/Replicas_List.json
     */
    /**
     * Sample code: List HorizonDb replicas in a pool.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void listHorizonDbReplicasInAPool(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.horizonDbReplicas()
            .list("exampleresourcegroup", "examplecluster", "examplepool", com.azure.core.util.Context.NONE);
    }
}
```

### HorizonDbReplicas_Update

```java
import com.azure.resourcemanager.horizondb.models.HorizonDbReplica;
import com.azure.resourcemanager.horizondb.models.HorizonDbReplicaPropertiesForPatchUpdate;
import com.azure.resourcemanager.horizondb.models.ReplicaRole;

/**
 * Samples for HorizonDbReplicas Update.
 */
public final class HorizonDbReplicasUpdateSamples {
    /*
     * x-ms-original-file: 2026-01-20-preview/Replicas_Update.json
     */
    /**
     * Sample code: Update a HorizonDb replica.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void updateAHorizonDbReplica(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        HorizonDbReplica resource = manager.horizonDbReplicas()
            .getWithResponse("exampleresourcegroup", "examplecluster", "examplepool", "examplereplica",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new HorizonDbReplicaPropertiesForPatchUpdate().withRole(ReplicaRole.READ_WRITE))
            .apply();
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
     * x-ms-original-file: 2026-01-20-preview/Operations_List.json
     */
    /**
     * Sample code: List operations for Microsoft.HorizonDb.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        listOperationsForMicrosoftHorizonDb(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

