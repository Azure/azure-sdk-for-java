# Code snippets and samples


## ManagedOps

- [CreateOrUpdate](#managedops_createorupdate)
- [Delete](#managedops_delete)
- [Get](#managedops_get)
- [List](#managedops_list)
- [Update](#managedops_update)

## Operations

- [List](#operations_list)
### ManagedOps_CreateOrUpdate

```java
import com.azure.resourcemanager.managedops.models.AzureMonitorConfiguration;
import com.azure.resourcemanager.managedops.models.ChangeTrackingConfiguration;
import com.azure.resourcemanager.managedops.models.DesiredConfiguration;
import com.azure.resourcemanager.managedops.models.ManagedOpsProperties;

/**
 * Samples for ManagedOps CreateOrUpdate.
 */
public final class ManagedOpsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-07-28-preview/ManagedOps_CreateOrUpdate.json
     */
    /**
     * Sample code: ManagedOps_CreateOrUpdate.
     * 
     * @param manager Entry point to ManagedOpsManager.
     */
    public static void managedOpsCreateOrUpdate(com.azure.resourcemanager.managedops.ManagedOpsManager manager) {
        manager.managedOps()
            .define("default")
            .withProperties(new ManagedOpsProperties().withDesiredConfiguration(new DesiredConfiguration()
                .withChangeTrackingAndInventory(new ChangeTrackingConfiguration().withLogAnalyticsWorkspaceId(
                    "/subscriptions/11809CA1-E126-4017-945E-AA795CD5C5A9/resourceGroups/myResourceGroup/providers/Microsoft.OperationalInsights/workspaces/00000000-0000-0000-0000-000000000000-Default"))
                .withAzureMonitorInsights(new AzureMonitorConfiguration().withAzureMonitorWorkspaceId(
                    "/subscriptions/11809CA1-E126-4017-945E-AA795CD5C5A9/resourceGroups/myResourceGroup/providers/Microsoft.Monitor/accounts/example"))
                .withUserAssignedManagedIdentityId(
                    "/subscriptions/11809CA1-E126-4017-945E-AA795CD5C5A9/resourceGroups/myResourceGroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/myManagedIdentity")))
            .create();
    }
}
```

### ManagedOps_Delete

```java
/**
 * Samples for ManagedOps Delete.
 */
public final class ManagedOpsDeleteSamples {
    /*
     * x-ms-original-file: 2025-07-28-preview/ManagedOps_Delete.json
     */
    /**
     * Sample code: ManagedOps_Delete.
     * 
     * @param manager Entry point to ManagedOpsManager.
     */
    public static void managedOpsDelete(com.azure.resourcemanager.managedops.ManagedOpsManager manager) {
        manager.managedOps().delete("default", com.azure.core.util.Context.NONE);
    }
}
```

### ManagedOps_Get

```java
/**
 * Samples for ManagedOps Get.
 */
public final class ManagedOpsGetSamples {
    /*
     * x-ms-original-file: 2025-07-28-preview/ManagedOps_Get.json
     */
    /**
     * Sample code: ManagedOps_Get.
     * 
     * @param manager Entry point to ManagedOpsManager.
     */
    public static void managedOpsGet(com.azure.resourcemanager.managedops.ManagedOpsManager manager) {
        manager.managedOps().getWithResponse("default", com.azure.core.util.Context.NONE);
    }
}
```

### ManagedOps_List

```java
/**
 * Samples for ManagedOps List.
 */
public final class ManagedOpsListSamples {
    /*
     * x-ms-original-file: 2025-07-28-preview/ManagedOps_List.json
     */
    /**
     * Sample code: ManagedOps_List.
     * 
     * @param manager Entry point to ManagedOpsManager.
     */
    public static void managedOpsList(com.azure.resourcemanager.managedops.ManagedOpsManager manager) {
        manager.managedOps().list(com.azure.core.util.Context.NONE);
    }
}
```

### ManagedOps_Update

```java
import com.azure.resourcemanager.managedops.models.ManagedOp;

/**
 * Samples for ManagedOps Update.
 */
public final class ManagedOpsUpdateSamples {
    /*
     * x-ms-original-file: 2025-07-28-preview/ManagedOps_Update.json
     */
    /**
     * Sample code: ManagedOps_Update.
     * 
     * @param manager Entry point to ManagedOpsManager.
     */
    public static void managedOpsUpdate(com.azure.resourcemanager.managedops.ManagedOpsManager manager) {
        ManagedOp resource
            = manager.managedOps().getWithResponse("default", com.azure.core.util.Context.NONE).getValue();
        resource.update().apply();
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
     * x-ms-original-file: 2025-07-28-preview/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to ManagedOpsManager.
     */
    public static void operationsList(com.azure.resourcemanager.managedops.ManagedOpsManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

