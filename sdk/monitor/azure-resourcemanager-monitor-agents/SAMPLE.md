# Code snippets and samples


## ObservabilityAgents

- [CreateOrUpdate](#observabilityagents_createorupdate)
- [Delete](#observabilityagents_delete)
- [GetByResourceGroup](#observabilityagents_getbyresourcegroup)
- [List](#observabilityagents_list)
- [ListByResourceGroup](#observabilityagents_listbyresourcegroup)
- [Update](#observabilityagents_update)

## Operations

- [List](#operations_list)
### ObservabilityAgents_CreateOrUpdate

```java
import com.azure.resourcemanager.monitor.agents.models.ManagedServiceIdentity;
import com.azure.resourcemanager.monitor.agents.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.monitor.agents.models.ObservabilityAgentProperties;
import com.azure.resourcemanager.monitor.agents.models.OperationEntry;
import com.azure.resourcemanager.monitor.agents.models.OperationMode;
import com.azure.resourcemanager.monitor.agents.models.OperationType;
import com.azure.resourcemanager.monitor.agents.models.UserAssignedIdentity;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ObservabilityAgents CreateOrUpdate.
 */
public final class ObservabilityAgentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/ObservabilityAgents_CreateOrUpdate.json
     */
    /**
     * Sample code: ObservabilityAgents_CreateOrUpdate.
     * 
     * @param manager Entry point to MonitorAgentsManager.
     */
    public static void
        observabilityAgentsCreateOrUpdate(com.azure.resourcemanager.monitor.agents.MonitorAgentsManager manager) {
        manager.observabilityAgents()
            .define("myObservabilityAgent")
            .withRegion("eastus")
            .withExistingResourceGroup("myResourceGroup")
            .withTags(mapOf("env", "dev"))
            .withProperties(new ObservabilityAgentProperties().withMonitoringAccountId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.Monitor/accounts/myAzureMonitorWorkspace")
                .withEnabled(true)
                .withOperations(Arrays.asList(
                    new OperationEntry().withType(OperationType.ISSUE_CREATION)
                        .withMode(OperationMode.AUTO)
                        .withInstructions("use includeAlertsFromGlobalRules"),
                    new OperationEntry().withType(OperationType.INVESTIGATION)
                        .withMode(OperationMode.AUTO)
                        .withInstructions("focus on recent issues"))))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/myIdentity",
                    new UserAssignedIdentity())))
            .create();
    }

    /*
     * x-ms-original-file: 2026-05-01-preview/ObservabilityAgents_CreateOrUpdate_MinimumSet.json
     */
    /**
     * Sample code: ObservabilityAgents_CreateOrUpdate_MinimumSet.
     * 
     * @param manager Entry point to MonitorAgentsManager.
     */
    public static void observabilityAgentsCreateOrUpdateMinimumSet(
        com.azure.resourcemanager.monitor.agents.MonitorAgentsManager manager) {
        manager.observabilityAgents()
            .define("myObservabilityAgent")
            .withRegion("eastus")
            .withExistingResourceGroup("myResourceGroup")
            .withProperties(new ObservabilityAgentProperties().withMonitoringAccountId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.Monitor/accounts/myAzureMonitorWorkspace"))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/myIdentity",
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

### ObservabilityAgents_Delete

```java
/**
 * Samples for ObservabilityAgents Delete.
 */
public final class ObservabilityAgentsDeleteSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/ObservabilityAgents_Delete.json
     */
    /**
     * Sample code: ObservabilityAgents_Delete.
     * 
     * @param manager Entry point to MonitorAgentsManager.
     */
    public static void
        observabilityAgentsDelete(com.azure.resourcemanager.monitor.agents.MonitorAgentsManager manager) {
        manager.observabilityAgents()
            .deleteByResourceGroupWithResponse("myResourceGroup", "myObservabilityAgent",
                com.azure.core.util.Context.NONE);
    }
}
```

### ObservabilityAgents_GetByResourceGroup

```java
/**
 * Samples for ObservabilityAgents GetByResourceGroup.
 */
public final class ObservabilityAgentsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/ObservabilityAgents_Get.json
     */
    /**
     * Sample code: ObservabilityAgents_Get.
     * 
     * @param manager Entry point to MonitorAgentsManager.
     */
    public static void observabilityAgentsGet(com.azure.resourcemanager.monitor.agents.MonitorAgentsManager manager) {
        manager.observabilityAgents()
            .getByResourceGroupWithResponse("myResourceGroup", "myObservabilityAgent",
                com.azure.core.util.Context.NONE);
    }
}
```

### ObservabilityAgents_List

```java
/**
 * Samples for ObservabilityAgents List.
 */
public final class ObservabilityAgentsListSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/ObservabilityAgents_ListBySubscription.json
     */
    /**
     * Sample code: ObservabilityAgents_ListBySubscription.
     * 
     * @param manager Entry point to MonitorAgentsManager.
     */
    public static void
        observabilityAgentsListBySubscription(com.azure.resourcemanager.monitor.agents.MonitorAgentsManager manager) {
        manager.observabilityAgents().list(com.azure.core.util.Context.NONE);
    }
}
```

### ObservabilityAgents_ListByResourceGroup

```java
/**
 * Samples for ObservabilityAgents ListByResourceGroup.
 */
public final class ObservabilityAgentsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/ObservabilityAgents_ListByResourceGroup.json
     */
    /**
     * Sample code: ObservabilityAgents_ListByResourceGroup.
     * 
     * @param manager Entry point to MonitorAgentsManager.
     */
    public static void
        observabilityAgentsListByResourceGroup(com.azure.resourcemanager.monitor.agents.MonitorAgentsManager manager) {
        manager.observabilityAgents().listByResourceGroup("myResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### ObservabilityAgents_Update

```java
import com.azure.resourcemanager.monitor.agents.models.ManagedServiceIdentity;
import com.azure.resourcemanager.monitor.agents.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.monitor.agents.models.ObservabilityAgentPropertiesUpdate;
import com.azure.resourcemanager.monitor.agents.models.ObservabilityAgentResource;
import com.azure.resourcemanager.monitor.agents.models.OperationEntry;
import com.azure.resourcemanager.monitor.agents.models.OperationMode;
import com.azure.resourcemanager.monitor.agents.models.OperationType;
import com.azure.resourcemanager.monitor.agents.models.UserAssignedIdentity;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ObservabilityAgents Update.
 */
public final class ObservabilityAgentsUpdateSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/ObservabilityAgents_Update_MinimumSet.json
     */
    /**
     * Sample code: ObservabilityAgents_Update_MinimumSet.
     * 
     * @param manager Entry point to MonitorAgentsManager.
     */
    public static void
        observabilityAgentsUpdateMinimumSet(com.azure.resourcemanager.monitor.agents.MonitorAgentsManager manager) {
        ObservabilityAgentResource resource = manager.observabilityAgents()
            .getByResourceGroupWithResponse("myResourceGroup", "myObservabilityAgent", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new ObservabilityAgentPropertiesUpdate().withMonitoringAccountId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.Monitor/accounts/myAzureMonitorWorkspace"))
            .apply();
    }

    /*
     * x-ms-original-file: 2026-05-01-preview/ObservabilityAgents_Update.json
     */
    /**
     * Sample code: ObservabilityAgents_Update.
     * 
     * @param manager Entry point to MonitorAgentsManager.
     */
    public static void
        observabilityAgentsUpdate(com.azure.resourcemanager.monitor.agents.MonitorAgentsManager manager) {
        ObservabilityAgentResource resource = manager.observabilityAgents()
            .getByResourceGroupWithResponse("myResourceGroup", "myObservabilityAgent", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("env", "prod"))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/myIdentity",
                    new UserAssignedIdentity())))
            .withProperties(new ObservabilityAgentPropertiesUpdate().withMonitoringAccountId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.Monitor/accounts/myAzureMonitorWorkspace")
                .withEnabled(false)
                .withOperations(Arrays.asList(
                    new OperationEntry().withType(OperationType.ISSUE_CREATION)
                        .withMode(OperationMode.MANUAL)
                        .withInstructions("Focus on storage and networking issues only."),
                    new OperationEntry().withType(OperationType.INVESTIGATION)
                        .withMode(OperationMode.AUTO)
                        .withInstructions("Focus on recent network issues."))))
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

### Operations_List

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2026-05-01-preview/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to MonitorAgentsManager.
     */
    public static void operationsList(com.azure.resourcemanager.monitor.agents.MonitorAgentsManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

