# Code snippets and samples


## Detectors

- [Get](#detectors_get)
- [ListByScheduler](#detectors_listbyscheduler)

## FailoverInstances

- [CreateOrReplace](#failoverinstances_createorreplace)
- [Delete](#failoverinstances_delete)
- [Failover](#failoverinstances_failover)
- [ForceFailoverAllowDataLoss](#failoverinstances_forcefailoverallowdataloss)
- [Get](#failoverinstances_get)
- [ListByScheduler](#failoverinstances_listbyscheduler)

## Operations

- [List](#operations_list)

## RetentionPolicies

- [CreateOrReplace](#retentionpolicies_createorreplace)
- [Delete](#retentionpolicies_delete)
- [Get](#retentionpolicies_get)
- [ListByScheduler](#retentionpolicies_listbyscheduler)
- [Update](#retentionpolicies_update)

## Schedulers

- [CreateOrUpdate](#schedulers_createorupdate)
- [CreateOrUpdatePrivateEndpointConnection](#schedulers_createorupdateprivateendpointconnection)
- [Delete](#schedulers_delete)
- [DeletePrivateEndpointConnection](#schedulers_deleteprivateendpointconnection)
- [GetByResourceGroup](#schedulers_getbyresourcegroup)
- [GetPrivateEndpointConnection](#schedulers_getprivateendpointconnection)
- [GetPrivateLink](#schedulers_getprivatelink)
- [List](#schedulers_list)
- [ListByResourceGroup](#schedulers_listbyresourcegroup)
- [ListPrivateEndpointConnections](#schedulers_listprivateendpointconnections)
- [ListPrivateLinks](#schedulers_listprivatelinks)
- [Restart](#schedulers_restart)
- [Update](#schedulers_update)
- [UpdatePrivateEndpointConnection](#schedulers_updateprivateendpointconnection)

## TaskHubs

- [CreateOrUpdate](#taskhubs_createorupdate)
- [Delete](#taskhubs_delete)
- [Get](#taskhubs_get)
- [ListByScheduler](#taskhubs_listbyscheduler)

## TaskQueues

- [CreateOrUpdate](#taskqueues_createorupdate)
- [Delete](#taskqueues_delete)
- [Get](#taskqueues_get)
- [ListByTaskHub](#taskqueues_listbytaskhub)
- [Update](#taskqueues_update)

## TransparentDataEncryptions

- [CreateOrReplace](#transparentdataencryptions_createorreplace)
- [Delete](#transparentdataencryptions_delete)
- [Get](#transparentdataencryptions_get)
- [ListByScheduler](#transparentdataencryptions_listbyscheduler)
### Detectors_Get

```java
/**
 * Samples for Detectors Get.
 */
public final class DetectorsGetSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/Detectors_Get.json
     */
    /**
     * Sample code: Detectors_Get.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void detectorsGet(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.detectors()
            .getWithResponse("rgopenapi", "testscheduler", "cpuhealth", com.azure.core.util.Context.NONE);
    }
}
```

### Detectors_ListByScheduler

```java
/**
 * Samples for Detectors ListByScheduler.
 */
public final class DetectorsListBySchedulerSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/Detectors_ListByScheduler.json
     */
    /**
     * Sample code: Detectors_ListByScheduler.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void detectorsListByScheduler(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.detectors().listByScheduler("rgopenapi", "testscheduler", com.azure.core.util.Context.NONE);
    }
}
```

### FailoverInstances_CreateOrReplace

```java
import com.azure.resourcemanager.durabletask.fluent.models.FailoverInstanceInner;
import com.azure.resourcemanager.durabletask.models.FailoverInstanceProperties;

/**
 * Samples for FailoverInstances CreateOrReplace.
 */
public final class FailoverInstancesCreateOrReplaceSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/FailoverInstances_CreateOrReplace_MaximumSet_Gen.json
     */
    /**
     * Sample code: FailoverInstances_CreateOrReplace_MaximumSet.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void
        failoverInstancesCreateOrReplaceMaximumSet(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.failoverInstances()
            .createOrReplace("rgdurabletask", "testscheduler", new FailoverInstanceInner()
                .withProperties(new FailoverInstanceProperties().withPartnerSchedulerResourceId(
                    "/subscriptions/194D3C1E-462F-4738-9025-092A628C06EB/resourceGroups/rgdurabletask-secondary/providers/Microsoft.DurableTask/schedulers/testscheduler-secondary")),
                com.azure.core.util.Context.NONE);
    }
}
```

### FailoverInstances_Delete

```java
/**
 * Samples for FailoverInstances Delete.
 */
public final class FailoverInstancesDeleteSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/FailoverInstances_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: FailoverInstances_Delete_MaximumSet.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void
        failoverInstancesDeleteMaximumSet(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.failoverInstances().delete("rgdurabletask", "testscheduler", com.azure.core.util.Context.NONE);
    }
}
```

### FailoverInstances_Failover

```java
/**
 * Samples for FailoverInstances Failover.
 */
public final class FailoverInstancesFailoverSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/FailoverInstances_Failover.json
     */
    /**
     * Sample code: FailoverInstances_Failover.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void failoverInstancesFailover(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.failoverInstances().failover("rgdurabletask", "testscheduler", com.azure.core.util.Context.NONE);
    }
}
```

### FailoverInstances_ForceFailoverAllowDataLoss

```java
/**
 * Samples for FailoverInstances ForceFailoverAllowDataLoss.
 */
public final class FailoverInstancesForceFailoverAllowDataLossSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/FailoverInstances_ForceFailoverAllowDataLoss.json
     */
    /**
     * Sample code: FailoverInstances_ForceFailoverAllowDataLoss.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void
        failoverInstancesForceFailoverAllowDataLoss(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.failoverInstances()
            .forceFailoverAllowDataLoss("rgdurabletask", "testscheduler", com.azure.core.util.Context.NONE);
    }
}
```

### FailoverInstances_Get

```java
/**
 * Samples for FailoverInstances Get.
 */
public final class FailoverInstancesGetSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/FailoverInstances_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: FailoverInstances_Get_MaximumSet.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void
        failoverInstancesGetMaximumSet(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.failoverInstances().getWithResponse("rgdurabletask", "testscheduler", com.azure.core.util.Context.NONE);
    }
}
```

### FailoverInstances_ListByScheduler

```java
/**
 * Samples for FailoverInstances ListByScheduler.
 */
public final class FailoverInstancesListBySchedulerSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/FailoverInstances_ListByScheduler_MaximumSet_Gen.json
     */
    /**
     * Sample code: FailoverInstances_ListByScheduler_MaximumSet.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void
        failoverInstancesListBySchedulerMaximumSet(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.failoverInstances().listByScheduler("rgdurabletask", "testscheduler", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-07-01-preview/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void operationsList(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### RetentionPolicies_CreateOrReplace

```java
import com.azure.resourcemanager.durabletask.fluent.models.RetentionPolicyInner;
import com.azure.resourcemanager.durabletask.models.PurgeableOrchestrationState;
import com.azure.resourcemanager.durabletask.models.RetentionPolicyDetails;
import com.azure.resourcemanager.durabletask.models.RetentionPolicyProperties;
import java.util.Arrays;

/**
 * Samples for RetentionPolicies CreateOrReplace.
 */
public final class RetentionPoliciesCreateOrReplaceSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/RetentionPolicies_CreateOrReplace_MaximumSet_Gen.json
     */
    /**
     * Sample code: RetentionPolicies_CreateOrReplace_MaximumSet.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void
        retentionPoliciesCreateOrReplaceMaximumSet(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.retentionPolicies()
            .createOrReplace("rgdurabletask", "testscheduler",
                new RetentionPolicyInner().withProperties(new RetentionPolicyProperties()
                    .withRetentionPolicies(Arrays.asList(new RetentionPolicyDetails().withRetentionPeriodInDays(30),
                        new RetentionPolicyDetails().withRetentionPeriodInDays(10)
                            .withOrchestrationState(PurgeableOrchestrationState.FAILED)))),
                com.azure.core.util.Context.NONE);
    }
}
```

### RetentionPolicies_Delete

```java
/**
 * Samples for RetentionPolicies Delete.
 */
public final class RetentionPoliciesDeleteSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/RetentionPolicies_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: RetentionPolicies_Delete_MaximumSet.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void
        retentionPoliciesDeleteMaximumSet(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.retentionPolicies().delete("rgdurabletask", "testcheduler", com.azure.core.util.Context.NONE);
    }
}
```

### RetentionPolicies_Get

```java
/**
 * Samples for RetentionPolicies Get.
 */
public final class RetentionPoliciesGetSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/RetentionPolicies_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: RetentionPolicies_Get_MaximumSet.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void
        retentionPoliciesGetMaximumSet(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.retentionPolicies().getWithResponse("rgdurabletask", "testscheduler", com.azure.core.util.Context.NONE);
    }
}
```

### RetentionPolicies_ListByScheduler

```java
/**
 * Samples for RetentionPolicies ListByScheduler.
 */
public final class RetentionPoliciesListBySchedulerSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/RetentionPolicies_ListByScheduler_MaximumSet_Gen.json
     */
    /**
     * Sample code: RetentionPolicies_ListByScheduler_MaximumSet.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void
        retentionPoliciesListBySchedulerMaximumSet(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.retentionPolicies().listByScheduler("rgdurabletask", "myscheduler", com.azure.core.util.Context.NONE);
    }
}
```

### RetentionPolicies_Update

```java
import com.azure.resourcemanager.durabletask.fluent.models.RetentionPolicyInner;
import com.azure.resourcemanager.durabletask.models.PurgeableOrchestrationState;
import com.azure.resourcemanager.durabletask.models.RetentionPolicyDetails;
import com.azure.resourcemanager.durabletask.models.RetentionPolicyProperties;
import java.util.Arrays;

/**
 * Samples for RetentionPolicies Update.
 */
public final class RetentionPoliciesUpdateSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/RetentionPolicies_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: RetentionPolicies_Update_MaximumSet.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void
        retentionPoliciesUpdateMaximumSet(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.retentionPolicies()
            .update("rgdurabletask", "testscheduler",
                new RetentionPolicyInner().withProperties(new RetentionPolicyProperties()
                    .withRetentionPolicies(Arrays.asList(new RetentionPolicyDetails().withRetentionPeriodInDays(30),
                        new RetentionPolicyDetails().withRetentionPeriodInDays(10)
                            .withOrchestrationState(PurgeableOrchestrationState.FAILED),
                        new RetentionPolicyDetails().withRetentionPeriodInDays(24)
                            .withOrchestrationState(PurgeableOrchestrationState.COMPLETED)))),
                com.azure.core.util.Context.NONE);
    }
}
```

### Schedulers_CreateOrUpdate

```java
import com.azure.resourcemanager.durabletask.models.ManagedServiceIdentity;
import com.azure.resourcemanager.durabletask.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.durabletask.models.SchedulerProperties;
import com.azure.resourcemanager.durabletask.models.SchedulerSku;
import com.azure.resourcemanager.durabletask.models.SchedulerSkuName;
import com.azure.resourcemanager.durabletask.models.UserAssignedIdentity;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Schedulers CreateOrUpdate.
 */
public final class SchedulersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/Schedulers_CreateOrUpdate.json
     */
    /**
     * Sample code: Schedulers_CreateOrUpdate.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void schedulersCreateOrUpdate(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.schedulers()
            .define("testscheduler")
            .withRegion("northcentralus")
            .withExistingResourceGroup("rgopenapi")
            .withTags(mapOf("department", "research", "development", "true"))
            .withProperties(new SchedulerProperties().withIpAllowlist(Arrays.asList("10.0.0.0/8"))
                .withSku(new SchedulerSku().withName(SchedulerSkuName.DEDICATED)))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/EE9BD735-67CE-4A90-89C4-439D3F6A4C93/resourceGroups/rgopenapi/providers/Microsoft.ManagedIdentity/userAssignedIdentities/testidentity",
                    new UserAssignedIdentity())))
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

### Schedulers_CreateOrUpdatePrivateEndpointConnection

```java
import com.azure.resourcemanager.durabletask.models.PrivateEndpoint;
import com.azure.resourcemanager.durabletask.models.PrivateEndpointConnectionProperties;
import com.azure.resourcemanager.durabletask.models.PrivateEndpointServiceConnectionStatus;
import com.azure.resourcemanager.durabletask.models.PrivateLinkServiceConnectionState;

/**
 * Samples for Schedulers CreateOrUpdatePrivateEndpointConnection.
 */
public final class SchedulersCreateOrUpdatePrivateEndpointConnectionSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/PrivateEndpointConnections_Create_MaximumSet_Gen.json
     */
    /**
     * Sample code: PrivateEndpointConnections_Create_MaximumSet.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void
        privateEndpointConnectionsCreateMaximumSet(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.schedulers()
            .definePrivateEndpointConnection("spzckqrbhfnabu")
            .withExistingScheduler("rgdurabletask", "testscheduler")
            .withProperties(
                new PrivateEndpointConnectionProperties().withPrivateEndpoint(new PrivateEndpoint())
                    .withPrivateLinkServiceConnectionState(new PrivateLinkServiceConnectionState()
                        .withStatus(PrivateEndpointServiceConnectionStatus.PENDING)
                        .withDescription("ujdcsoyxljivwsgfkexhotaxcmzq")
                        .withActionsRequired("mxymqfbbmpwjxsroldlsd")))
            .create();
    }
}
```

### Schedulers_Delete

```java
/**
 * Samples for Schedulers Delete.
 */
public final class SchedulersDeleteSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/Schedulers_Delete.json
     */
    /**
     * Sample code: Schedulers_Delete.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void schedulersDelete(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.schedulers().delete("rgopenapi", "testscheduler", com.azure.core.util.Context.NONE);
    }
}
```

### Schedulers_DeletePrivateEndpointConnection

```java
/**
 * Samples for Schedulers DeletePrivateEndpointConnection.
 */
public final class SchedulersDeletePrivateEndpointConnectionSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/PrivateEndpointConnections_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: PrivateEndpointConnections_Delete_MaximumSet.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void
        privateEndpointConnectionsDeleteMaximumSet(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.schedulers()
            .deletePrivateEndpointConnection("rgdurabletask", "testscheduler", "spzckqrbhfnabu",
                com.azure.core.util.Context.NONE);
    }
}
```

### Schedulers_GetByResourceGroup

```java
/**
 * Samples for Schedulers GetByResourceGroup.
 */
public final class SchedulersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/Schedulers_Get.json
     */
    /**
     * Sample code: Schedulers_Get.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void schedulersGet(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.schedulers()
            .getByResourceGroupWithResponse("rgopenapi", "testscheduler", com.azure.core.util.Context.NONE);
    }
}
```

### Schedulers_GetPrivateEndpointConnection

```java
/**
 * Samples for Schedulers GetPrivateEndpointConnection.
 */
public final class SchedulersGetPrivateEndpointConnectionSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/PrivateEndpointConnections_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: PrivateEndpointConnections_Get_MaximumSet.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void
        privateEndpointConnectionsGetMaximumSet(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.schedulers()
            .getPrivateEndpointConnectionWithResponse("rgdurabletask", "testscheduler", "spzckqrbhfnabu",
                com.azure.core.util.Context.NONE);
    }
}
```

### Schedulers_GetPrivateLink

```java
/**
 * Samples for Schedulers GetPrivateLink.
 */
public final class SchedulersGetPrivateLinkSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/PrivateLinkResources_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: PrivateLinkResources_Get_MaximumSet.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void
        privateLinkResourcesGetMaximumSet(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.schedulers()
            .getPrivateLinkWithResponse("rgdurabletask", "testscheduler", "ulbdiqhrmwnkejje",
                com.azure.core.util.Context.NONE);
    }
}
```

### Schedulers_List

```java
/**
 * Samples for Schedulers List.
 */
public final class SchedulersListSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/Schedulers_ListBySubscription.json
     */
    /**
     * Sample code: Schedulers_ListBySubscription.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void schedulersListBySubscription(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.schedulers().list(com.azure.core.util.Context.NONE);
    }
}
```

### Schedulers_ListByResourceGroup

```java
/**
 * Samples for Schedulers ListByResourceGroup.
 */
public final class SchedulersListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/Schedulers_ListByResourceGroup.json
     */
    /**
     * Sample code: Schedulers_ListByResourceGroup.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void schedulersListByResourceGroup(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.schedulers().listByResourceGroup("rgopenapi", com.azure.core.util.Context.NONE);
    }
}
```

### Schedulers_ListPrivateEndpointConnections

```java
/**
 * Samples for Schedulers ListPrivateEndpointConnections.
 */
public final class SchedulersListPrivateEndpointConnectionsSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/PrivateEndpointConnections_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: PrivateEndpointConnections_List_MaximumSet.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void
        privateEndpointConnectionsListMaximumSet(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.schedulers()
            .listPrivateEndpointConnections("rgdurabletask", "testscheduler", com.azure.core.util.Context.NONE);
    }
}
```

### Schedulers_ListPrivateLinks

```java
/**
 * Samples for Schedulers ListPrivateLinks.
 */
public final class SchedulersListPrivateLinksSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/PrivateLinkResources_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: PrivateLinkResources_List_MaximumSet.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void
        privateLinkResourcesListMaximumSet(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.schedulers().listPrivateLinks("rgdurabletask", "testscheduler", com.azure.core.util.Context.NONE);
    }
}
```

### Schedulers_Restart

```java
/**
 * Samples for Schedulers Restart.
 */
public final class SchedulersRestartSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/Schedulers_Restart.json
     */
    /**
     * Sample code: Schedulers_Restart.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void schedulersRestart(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.schedulers().restartWithResponse("rgopenapi", "testscheduler", com.azure.core.util.Context.NONE);
    }
}
```

### Schedulers_Update

```java
import com.azure.resourcemanager.durabletask.models.ManagedServiceIdentity;
import com.azure.resourcemanager.durabletask.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.durabletask.models.Scheduler;
import com.azure.resourcemanager.durabletask.models.SchedulerPropertiesUpdate;
import com.azure.resourcemanager.durabletask.models.SchedulerSkuName;
import com.azure.resourcemanager.durabletask.models.SchedulerSkuUpdate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Schedulers Update.
 */
public final class SchedulersUpdateSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/Schedulers_Update.json
     */
    /**
     * Sample code: Schedulers_Update.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void schedulersUpdate(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        Scheduler resource = manager.schedulers()
            .getByResourceGroupWithResponse("rgopenapi", "testscheduler", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("hello", "world"))
            .withProperties(new SchedulerPropertiesUpdate().withIpAllowlist(Arrays.asList("10.0.0.0/8"))
                .withSku(new SchedulerSkuUpdate().withName(SchedulerSkuName.DEDICATED).withCapacity(3)))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.NONE))
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

### Schedulers_UpdatePrivateEndpointConnection

```java
import com.azure.resourcemanager.durabletask.models.OptionalPropertiesUpdateableProperties;
import com.azure.resourcemanager.durabletask.models.PrivateEndpointConnection;
import com.azure.resourcemanager.durabletask.models.PrivateEndpointServiceConnectionStatus;
import com.azure.resourcemanager.durabletask.models.PrivateLinkServiceConnectionState;

/**
 * Samples for Schedulers UpdatePrivateEndpointConnection.
 */
public final class SchedulersUpdatePrivateEndpointConnectionSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/PrivateEndpointConnections_Update.json
     */
    /**
     * Sample code: PrivateEndpointConnections_Update.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void
        privateEndpointConnectionsUpdate(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        PrivateEndpointConnection resource = manager.schedulers()
            .getPrivateEndpointConnectionWithResponse("rgdurabletask", "testscheduler", "spzckqrbhfnabu",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new OptionalPropertiesUpdateableProperties().withPrivateLinkServiceConnectionState(
                new PrivateLinkServiceConnectionState().withStatus(PrivateEndpointServiceConnectionStatus.APPROVED)))
            .apply();
    }
}
```

### TaskHubs_CreateOrUpdate

```java
import com.azure.resourcemanager.durabletask.models.TaskHubProperties;
import java.util.Arrays;

/**
 * Samples for TaskHubs CreateOrUpdate.
 */
public final class TaskHubsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/TaskHubs_CreateOrUpdate.json
     */
    /**
     * Sample code: TaskHubs_CreateOrUpdate.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void taskHubsCreateOrUpdate(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.taskHubs()
            .define("testtaskhub")
            .withExistingScheduler("rgopenapi", "testscheduler")
            .withProperties(new TaskHubProperties().withCapabilities(Arrays.asList("ExampleFutureCapability")))
            .create();
    }
}
```

### TaskHubs_Delete

```java
/**
 * Samples for TaskHubs Delete.
 */
public final class TaskHubsDeleteSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/TaskHubs_Delete.json
     */
    /**
     * Sample code: TaskHubs_Delete.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void taskHubsDelete(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.taskHubs().delete("rgopenapi", "testscheduler", "testtaskhub", com.azure.core.util.Context.NONE);
    }
}
```

### TaskHubs_Get

```java
/**
 * Samples for TaskHubs Get.
 */
public final class TaskHubsGetSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/TaskHubs_Get.json
     */
    /**
     * Sample code: TaskHubs_Get.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void taskHubsGet(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.taskHubs()
            .getWithResponse("rgopenapi", "testscheduler", "testtaskhub", com.azure.core.util.Context.NONE);
    }
}
```

### TaskHubs_ListByScheduler

```java
/**
 * Samples for TaskHubs ListByScheduler.
 */
public final class TaskHubsListBySchedulerSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/TaskHubs_ListByScheduler.json
     */
    /**
     * Sample code: TaskHubs_ListByScheduler.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void taskHubsListByScheduler(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.taskHubs().listByScheduler("rgopenapi", "testscheduler", com.azure.core.util.Context.NONE);
    }
}
```

### TaskQueues_CreateOrUpdate

```java
import com.azure.resourcemanager.durabletask.models.TaskQueueProperties;
import com.azure.resourcemanager.durabletask.models.TaskQueueThroughput;

/**
 * Samples for TaskQueues CreateOrUpdate.
 */
public final class TaskQueuesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/TaskQueues_CreateOrUpdate.json
     */
    /**
     * Sample code: TaskQueues_CreateOrUpdate.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void taskQueuesCreateOrUpdate(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.taskQueues()
            .define("QueueA")
            .withExistingTaskHub("rgopenapi", "testscheduler", "testtaskhub")
            .withProperties(
                new TaskQueueProperties().withThroughput(new TaskQueueThroughput().withActionsPerSecond(40)))
            .create();
    }

    /*
     * x-ms-original-file: 2026-07-01-preview/TaskQueues_CreateOrUpdate_MinimumSet.json
     */
    /**
     * Sample code: TaskQueues_CreateOrUpdate_MinimumSet.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void
        taskQueuesCreateOrUpdateMinimumSet(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.taskQueues()
            .define("QueueA")
            .withExistingTaskHub("rgopenapi", "testscheduler", "testtaskhub")
            .withProperties(new TaskQueueProperties())
            .create();
    }
}
```

### TaskQueues_Delete

```java
/**
 * Samples for TaskQueues Delete.
 */
public final class TaskQueuesDeleteSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/TaskQueues_Delete.json
     */
    /**
     * Sample code: TaskQueues_Delete.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void taskQueuesDelete(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.taskQueues()
            .delete("rgopenapi", "testscheduler", "testtaskhub", "QueueA", com.azure.core.util.Context.NONE);
    }
}
```

### TaskQueues_Get

```java
/**
 * Samples for TaskQueues Get.
 */
public final class TaskQueuesGetSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/TaskQueues_Get.json
     */
    /**
     * Sample code: TaskQueues_Get.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void taskQueuesGet(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.taskQueues()
            .getWithResponse("rgopenapi", "testscheduler", "testtaskhub", "QueueA", com.azure.core.util.Context.NONE);
    }
}
```

### TaskQueues_ListByTaskHub

```java
/**
 * Samples for TaskQueues ListByTaskHub.
 */
public final class TaskQueuesListByTaskHubSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/TaskQueues_ListByTaskHub.json
     */
    /**
     * Sample code: TaskQueues_ListByTaskHub.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void taskQueuesListByTaskHub(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.taskQueues()
            .listByTaskHub("rgopenapi", "testscheduler", "testtaskhub", com.azure.core.util.Context.NONE);
    }
}
```

### TaskQueues_Update

```java
import com.azure.resourcemanager.durabletask.models.TaskQueue;
import com.azure.resourcemanager.durabletask.models.TaskQueueProperties;
import com.azure.resourcemanager.durabletask.models.TaskQueueThroughput;

/**
 * Samples for TaskQueues Update.
 */
public final class TaskQueuesUpdateSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/TaskQueues_Update.json
     */
    /**
     * Sample code: TaskQueues_Update.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void taskQueuesUpdate(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        TaskQueue resource = manager.taskQueues()
            .getWithResponse("rgopenapi", "testscheduler", "testtaskhub", "QueueA", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(
                new TaskQueueProperties().withThroughput(new TaskQueueThroughput().withActionsPerSecond(80)))
            .apply();
    }
}
```

### TransparentDataEncryptions_CreateOrReplace

```java
import com.azure.resourcemanager.durabletask.fluent.models.TransparentDataEncryptionInner;
import com.azure.resourcemanager.durabletask.models.TransparentDataEncryptionKeySource;
import com.azure.resourcemanager.durabletask.models.TransparentDataEncryptionProperties;

/**
 * Samples for TransparentDataEncryptions CreateOrReplace.
 */
public final class TransparentDataEncryptionsCreateOrReplaceSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/TransparentDataEncryptions_CreateOrReplace_MaximumSet_Gen.json
     */
    /**
     * Sample code: TransparentDataEncryptions_CreateOrReplace_MaximumSet.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void transparentDataEncryptionsCreateOrReplaceMaximumSet(
        com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.transparentDataEncryptions()
            .createOrReplace("rgdurabletask", "testscheduler",
                new TransparentDataEncryptionInner().withProperties(new TransparentDataEncryptionProperties()
                    .withKeySource(TransparentDataEncryptionKeySource.CUSTOMER_MANAGED)
                    .withKeyVaultKeyUri("fakeTokenPlaceholder")
                    .withAutoRotationEnabled(true)),
                com.azure.core.util.Context.NONE);
    }
}
```

### TransparentDataEncryptions_Delete

```java
/**
 * Samples for TransparentDataEncryptions Delete.
 */
public final class TransparentDataEncryptionsDeleteSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/TransparentDataEncryptions_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: TransparentDataEncryptions_Delete_MaximumSet.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void
        transparentDataEncryptionsDeleteMaximumSet(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.transparentDataEncryptions().delete("rgdurabletask", "testscheduler", com.azure.core.util.Context.NONE);
    }
}
```

### TransparentDataEncryptions_Get

```java
/**
 * Samples for TransparentDataEncryptions Get.
 */
public final class TransparentDataEncryptionsGetSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/TransparentDataEncryptions_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: TransparentDataEncryptions_Get_MaximumSet.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void
        transparentDataEncryptionsGetMaximumSet(com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.transparentDataEncryptions()
            .getWithResponse("rgdurabletask", "testscheduler", com.azure.core.util.Context.NONE);
    }
}
```

### TransparentDataEncryptions_ListByScheduler

```java
/**
 * Samples for TransparentDataEncryptions ListByScheduler.
 */
public final class TransparentDataEncryptionsListBySchedulerSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/TransparentDataEncryptions_ListByScheduler_MaximumSet_Gen.json
     */
    /**
     * Sample code: TransparentDataEncryptions_ListByScheduler_MaximumSet.
     * 
     * @param manager Entry point to DurableTaskManager.
     */
    public static void transparentDataEncryptionsListBySchedulerMaximumSet(
        com.azure.resourcemanager.durabletask.DurableTaskManager manager) {
        manager.transparentDataEncryptions()
            .listByScheduler("rgdurabletask", "testscheduler", com.azure.core.util.Context.NONE);
    }
}
```

