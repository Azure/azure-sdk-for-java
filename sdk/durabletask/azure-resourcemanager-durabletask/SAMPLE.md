# Code snippets and samples


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
- [Update](#schedulers_update)
- [UpdatePrivateEndpointConnection](#schedulers_updateprivateendpointconnection)

## TaskHubs

- [CreateOrUpdate](#taskhubs_createorupdate)
- [Delete](#taskhubs_delete)
- [Get](#taskhubs_get)
- [ListByScheduler](#taskhubs_listbyscheduler)
### Operations_List

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2026-02-01/Operations_List.json
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
     * x-ms-original-file: 2026-02-01/RetentionPolicies_CreateOrReplace_MaximumSet_Gen.json
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
     * x-ms-original-file: 2026-02-01/RetentionPolicies_Delete_MaximumSet_Gen.json
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
     * x-ms-original-file: 2026-02-01/RetentionPolicies_Get_MaximumSet_Gen.json
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
     * x-ms-original-file: 2026-02-01/RetentionPolicies_ListByScheduler_MaximumSet_Gen.json
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
     * x-ms-original-file: 2026-02-01/RetentionPolicies_Update_MaximumSet_Gen.json
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
import com.azure.resourcemanager.durabletask.models.SchedulerProperties;
import com.azure.resourcemanager.durabletask.models.SchedulerSku;
import com.azure.resourcemanager.durabletask.models.SchedulerSkuName;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Schedulers CreateOrUpdate.
 */
public final class SchedulersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-02-01/Schedulers_CreateOrUpdate.json
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
     * x-ms-original-file: 2026-02-01/PrivateEndpointConnections_Create_MaximumSet_Gen.json
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
     * x-ms-original-file: 2026-02-01/Schedulers_Delete.json
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
     * x-ms-original-file: 2026-02-01/PrivateEndpointConnections_Delete_MaximumSet_Gen.json
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
     * x-ms-original-file: 2026-02-01/Schedulers_Get.json
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
     * x-ms-original-file: 2026-02-01/PrivateEndpointConnections_Get_MaximumSet_Gen.json
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
     * x-ms-original-file: 2026-02-01/PrivateLinkResources_Get_MaximumSet_Gen.json
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
     * x-ms-original-file: 2026-02-01/Schedulers_ListBySubscription.json
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
     * x-ms-original-file: 2026-02-01/Schedulers_ListByResourceGroup.json
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
     * x-ms-original-file: 2026-02-01/PrivateEndpointConnections_List_MaximumSet_Gen.json
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
     * x-ms-original-file: 2026-02-01/PrivateLinkResources_List_MaximumSet_Gen.json
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

### Schedulers_Update

```java
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
     * x-ms-original-file: 2026-02-01/Schedulers_Update.json
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
     * x-ms-original-file: 2026-02-01/PrivateEndpointConnections_Update.json
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

/**
 * Samples for TaskHubs CreateOrUpdate.
 */
public final class TaskHubsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-02-01/TaskHubs_CreateOrUpdate.json
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
            .withProperties(new TaskHubProperties())
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
     * x-ms-original-file: 2026-02-01/TaskHubs_Delete.json
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
     * x-ms-original-file: 2026-02-01/TaskHubs_Get.json
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
     * x-ms-original-file: 2026-02-01/TaskHubs_ListByScheduler.json
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

