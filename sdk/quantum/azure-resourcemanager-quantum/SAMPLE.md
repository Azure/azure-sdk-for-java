# Code snippets and samples


## Offerings

- [List](#offerings_list)

## Operations

- [List](#operations_list)

## WorkspaceOperation

- [CheckNameAvailability](#workspaceoperation_checknameavailability)

## Workspaces

- [CreateOrUpdate](#workspaces_createorupdate)
- [Delete](#workspaces_delete)
- [GetByResourceGroup](#workspaces_getbyresourcegroup)
- [List](#workspaces_list)
- [ListByResourceGroup](#workspaces_listbyresourcegroup)
- [UpdateTags](#workspaces_updatetags)
### Offerings_List

```java
/** Samples for Offerings List. */
public final class OfferingsListSamples {
    /*
     * x-ms-original-file: specification/quantum/resource-manager/Microsoft.Quantum/preview/2022-01-10-preview/examples/offeringsList.json
     */
    /**
     * Sample code: OfferingsList.
     *
     * @param manager Entry point to AzureQuantumManager.
     */
    public static void offeringsList(com.azure.resourcemanager.quantum.AzureQuantumManager manager) {
        manager.offerings().list("westus2", com.azure.core.util.Context.NONE);
    }
}
```

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/quantum/resource-manager/Microsoft.Quantum/preview/2022-01-10-preview/examples/operations.json
     */
    /**
     * Sample code: Operations.
     *
     * @param manager Entry point to AzureQuantumManager.
     */
    public static void operations(com.azure.resourcemanager.quantum.AzureQuantumManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### WorkspaceOperation_CheckNameAvailability

```java
import com.azure.resourcemanager.quantum.models.CheckNameAvailabilityParameters;

/** Samples for WorkspaceOperation CheckNameAvailability. */
public final class WorkspaceOperationCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/quantum/resource-manager/Microsoft.Quantum/preview/2022-01-10-preview/examples/quantumWorkspacesCheckNameAvailability.json
     */
    /**
     * Sample code: QuantumWorkspacesCheckNameAvailability.
     *
     * @param manager Entry point to AzureQuantumManager.
     */
    public static void quantumWorkspacesCheckNameAvailability(
        com.azure.resourcemanager.quantum.AzureQuantumManager manager) {
        manager
            .workspaceOperations()
            .checkNameAvailabilityWithResponse(
                "westus2",
                new CheckNameAvailabilityParameters()
                    .withName("sample-workspace-name")
                    .withType("Microsoft.Quantum/Workspaces"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_CreateOrUpdate

```java
import com.azure.resourcemanager.quantum.models.Provider;
import java.util.Arrays;

/** Samples for Workspaces CreateOrUpdate. */
public final class WorkspacesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/quantum/resource-manager/Microsoft.Quantum/preview/2022-01-10-preview/examples/quantumWorkspacesPut.json
     */
    /**
     * Sample code: QuantumWorkspacesPut.
     *
     * @param manager Entry point to AzureQuantumManager.
     */
    public static void quantumWorkspacesPut(com.azure.resourcemanager.quantum.AzureQuantumManager manager) {
        manager
            .workspaces()
            .define("quantumworkspace1")
            .withRegion("West US")
            .withExistingResourceGroup("quantumResourcegroup")
            .withProviders(
                Arrays
                    .asList(
                        new Provider().withProviderId("Honeywell").withProviderSku("Basic"),
                        new Provider().withProviderId("IonQ").withProviderSku("Basic"),
                        new Provider().withProviderId("OneQBit").withProviderSku("Basic")))
            .withStorageAccount(
                "/subscriptions/1C4B2828-7D49-494F-933D-061373BE28C2/resourceGroups/quantumResourcegroup/providers/Microsoft.Storage/storageAccounts/testStorageAccount")
            .create();
    }
}
```

### Workspaces_Delete

```java
/** Samples for Workspaces Delete. */
public final class WorkspacesDeleteSamples {
    /*
     * x-ms-original-file: specification/quantum/resource-manager/Microsoft.Quantum/preview/2022-01-10-preview/examples/quantumWorkspacesDelete.json
     */
    /**
     * Sample code: QuantumWorkspacesDelete.
     *
     * @param manager Entry point to AzureQuantumManager.
     */
    public static void quantumWorkspacesDelete(com.azure.resourcemanager.quantum.AzureQuantumManager manager) {
        manager.workspaces().delete("quantumResourcegroup", "quantumworkspace1", com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_GetByResourceGroup

```java
/** Samples for Workspaces GetByResourceGroup. */
public final class WorkspacesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/quantum/resource-manager/Microsoft.Quantum/preview/2022-01-10-preview/examples/quantumWorkspacesGet.json
     */
    /**
     * Sample code: QuantumWorkspacesGet.
     *
     * @param manager Entry point to AzureQuantumManager.
     */
    public static void quantumWorkspacesGet(com.azure.resourcemanager.quantum.AzureQuantumManager manager) {
        manager
            .workspaces()
            .getByResourceGroupWithResponse(
                "quantumResourcegroup", "quantumworkspace1", com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_List

```java
/** Samples for Workspaces List. */
public final class WorkspacesListSamples {
    /*
     * x-ms-original-file: specification/quantum/resource-manager/Microsoft.Quantum/preview/2022-01-10-preview/examples/quantumWorkspacesListSubscription.json
     */
    /**
     * Sample code: QuantumWorkspacesListBySubscription.
     *
     * @param manager Entry point to AzureQuantumManager.
     */
    public static void quantumWorkspacesListBySubscription(
        com.azure.resourcemanager.quantum.AzureQuantumManager manager) {
        manager.workspaces().list(com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_ListByResourceGroup

```java
/** Samples for Workspaces ListByResourceGroup. */
public final class WorkspacesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/quantum/resource-manager/Microsoft.Quantum/preview/2022-01-10-preview/examples/quantumWorkspacesListResourceGroup.json
     */
    /**
     * Sample code: QuantumWorkspacesListByResourceGroup.
     *
     * @param manager Entry point to AzureQuantumManager.
     */
    public static void quantumWorkspacesListByResourceGroup(
        com.azure.resourcemanager.quantum.AzureQuantumManager manager) {
        manager.workspaces().listByResourceGroup("quantumResourcegroup", com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_UpdateTags

```java
import com.azure.resourcemanager.quantum.models.QuantumWorkspace;
import java.util.HashMap;
import java.util.Map;

/** Samples for Workspaces UpdateTags. */
public final class WorkspacesUpdateTagsSamples {
    /*
     * x-ms-original-file: specification/quantum/resource-manager/Microsoft.Quantum/preview/2022-01-10-preview/examples/quantumWorkspacesPatch.json
     */
    /**
     * Sample code: QuantumWorkspacesPatchTags.
     *
     * @param manager Entry point to AzureQuantumManager.
     */
    public static void quantumWorkspacesPatchTags(com.azure.resourcemanager.quantum.AzureQuantumManager manager) {
        QuantumWorkspace resource =
            manager
                .workspaces()
                .getByResourceGroupWithResponse(
                    "quantumResourcegroup", "quantumworkspace1", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

