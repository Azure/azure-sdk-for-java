# Code snippets and samples


## Offerings

- [List](#offerings_list)

## Operations

- [List](#operations_list)

## SuiteOffers

- [List](#suiteoffers_list)

## Workspaces

- [CreateOrUpdate](#workspaces_createorupdate)
- [Delete](#workspaces_delete)
- [GetByResourceGroup](#workspaces_getbyresourcegroup)
- [List](#workspaces_list)
- [ListByResourceGroup](#workspaces_listbyresourcegroup)
- [UpdateTags](#workspaces_updatetags)
### Offerings_List

```java
/**
 * Samples for Offerings List.
 */
public final class OfferingsListSamples {
    /*
     * x-ms-original-file: 2025-12-15-preview/Offerings_List.json
     */
    /**
     * Sample code: Offerings_List.
     * 
     * @param manager Entry point to AzureQuantumManager.
     */
    public static void offeringsList(com.azure.resourcemanager.quantum.AzureQuantumManager manager) {
        manager.offerings().list("westus2", null, com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2025-12-15-preview/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to AzureQuantumManager.
     */
    public static void operationsList(com.azure.resourcemanager.quantum.AzureQuantumManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### SuiteOffers_List

```java
/**
 * Samples for SuiteOffers List.
 */
public final class SuiteOffersListSamples {
    /*
     * x-ms-original-file: 2025-12-15-preview/SuiteOffers_ListBySubscription.json
     */
    /**
     * Sample code: SuiteOffers_ListBySubscription.
     * 
     * @param manager Entry point to AzureQuantumManager.
     */
    public static void suiteOffersListBySubscription(com.azure.resourcemanager.quantum.AzureQuantumManager manager) {
        manager.suiteOffers().list(com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_CreateOrUpdate

```java
import com.azure.resourcemanager.quantum.models.Provider;
import com.azure.resourcemanager.quantum.models.QuotaAllocations;
import com.azure.resourcemanager.quantum.models.WorkspaceResourceProperties;
import java.util.Arrays;

/**
 * Samples for Workspaces CreateOrUpdate.
 */
public final class WorkspacesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-12-15-preview/Workspaces_CreateOrUpdate.json
     */
    /**
     * Sample code: Workspaces_CreateOrUpdate.
     * 
     * @param manager Entry point to AzureQuantumManager.
     */
    public static void workspacesCreateOrUpdate(com.azure.resourcemanager.quantum.AzureQuantumManager manager) {
        manager.workspaces()
            .define("quantumworkspace1")
            .withRegion("West US")
            .withExistingResourceGroup("quantumResourcegroup")
            .withProperties(
                new WorkspaceResourceProperties()
                    .withProviders(
                        Arrays.asList(new Provider().withProviderId("Honeywell").withProviderSku("Basic"),
                            new Provider().withProviderId("IonQ").withProviderSku("Basic"),
                            new Provider().withProviderId("OneQBit").withProviderSku("Basic"),
                            new Provider().withProviderId("suiteProvider")
                                .withProviderSku("Basic")
                                .withQuotas(new QuotaAllocations()
                                    .withStandardMinutesLifetime(500)
                                    .withHighMinutesLifetime(50))))
                    .withStorageAccount(
                        "/subscriptions/1C4B2828-7D49-494F-933D-061373BE28C2/resourceGroups/quantumResourcegroup/providers/Microsoft.Storage/storageAccounts/testStorageAccount"))
            .create();
    }
}
```

### Workspaces_Delete

```java
/**
 * Samples for Workspaces Delete.
 */
public final class WorkspacesDeleteSamples {
    /*
     * x-ms-original-file: 2025-12-15-preview/Workspaces_Delete.json
     */
    /**
     * Sample code: Workspaces_Delete.
     * 
     * @param manager Entry point to AzureQuantumManager.
     */
    public static void workspacesDelete(com.azure.resourcemanager.quantum.AzureQuantumManager manager) {
        manager.workspaces().delete("quantumResourcegroup", "quantumworkspace1", com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_GetByResourceGroup

```java
/**
 * Samples for Workspaces GetByResourceGroup.
 */
public final class WorkspacesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-12-15-preview/Workspaces_Get.json
     */
    /**
     * Sample code: Workspaces_Get.
     * 
     * @param manager Entry point to AzureQuantumManager.
     */
    public static void workspacesGet(com.azure.resourcemanager.quantum.AzureQuantumManager manager) {
        manager.workspaces()
            .getByResourceGroupWithResponse("quantumResourcegroup", "quantumworkspace1",
                com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_List

```java
/**
 * Samples for Workspaces List.
 */
public final class WorkspacesListSamples {
    /*
     * x-ms-original-file: 2025-12-15-preview/Workspaces_ListBySubscription.json
     */
    /**
     * Sample code: Workspaces_ListBySubscription.
     * 
     * @param manager Entry point to AzureQuantumManager.
     */
    public static void workspacesListBySubscription(com.azure.resourcemanager.quantum.AzureQuantumManager manager) {
        manager.workspaces().list(com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_ListByResourceGroup

```java
/**
 * Samples for Workspaces ListByResourceGroup.
 */
public final class WorkspacesListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-12-15-preview/Workspaces_ListByResourceGroup.json
     */
    /**
     * Sample code: Workspaces_ListByResourceGroup.
     * 
     * @param manager Entry point to AzureQuantumManager.
     */
    public static void workspacesListByResourceGroup(com.azure.resourcemanager.quantum.AzureQuantumManager manager) {
        manager.workspaces().listByResourceGroup("quantumResourcegroup", com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_UpdateTags

```java
import com.azure.resourcemanager.quantum.models.QuantumWorkspace;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Workspaces UpdateTags.
 */
public final class WorkspacesUpdateTagsSamples {
    /*
     * x-ms-original-file: 2025-12-15-preview/Workspaces_UpdateTags.json
     */
    /**
     * Sample code: Workspaces_UpdateTags.
     * 
     * @param manager Entry point to AzureQuantumManager.
     */
    public static void workspacesUpdateTags(com.azure.resourcemanager.quantum.AzureQuantumManager manager) {
        QuantumWorkspace resource = manager.workspaces()
            .getByResourceGroupWithResponse("quantumResourcegroup", "quantumworkspace1",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

