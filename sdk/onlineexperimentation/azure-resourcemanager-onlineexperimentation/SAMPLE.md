# Code snippets and samples


## OnlineExperimentWorkspaces

- [CreateOrUpdate](#onlineexperimentworkspaces_createorupdate)
- [Delete](#onlineexperimentworkspaces_delete)
- [GetByResourceGroup](#onlineexperimentworkspaces_getbyresourcegroup)
- [List](#onlineexperimentworkspaces_list)
- [ListByResourceGroup](#onlineexperimentworkspaces_listbyresourcegroup)
- [Update](#onlineexperimentworkspaces_update)

## Operations

- [List](#operations_list)
### OnlineExperimentWorkspaces_CreateOrUpdate

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2025-05-31-preview/OnlineExperimentWorkspaces_OperationsList.json
     */
    /**
     * Sample code: List Online Experiment Workspaces operations.
     * 
     * @param manager Entry point to OnlineExperimentationManager.
     */
    public static void listOnlineExperimentWorkspacesOperations(
        com.azure.resourcemanager.onlineexperimentation.OnlineExperimentationManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### OnlineExperimentWorkspaces_Delete

```java
/**
 * Samples for OnlineExperimentWorkspaces Delete.
 */
public final class OnlineExperimentWorkspacesDeleteSamples {
    /*
     * x-ms-original-file: 2025-05-31-preview/OnlineExperimentWorkspaces_Delete.json
     */
    /**
     * Sample code: Delete an Online Experiment Workspace.
     * 
     * @param manager Entry point to OnlineExperimentationManager.
     */
    public static void deleteAnOnlineExperimentWorkspace(
        com.azure.resourcemanager.onlineexperimentation.OnlineExperimentationManager manager) {
        manager.onlineExperimentWorkspaces().delete("res9871", "expworkspace3", com.azure.core.util.Context.NONE);
    }
}
```

### OnlineExperimentWorkspaces_GetByResourceGroup

```java
/**
 * Samples for OnlineExperimentWorkspaces List.
 */
public final class OnlineExperimentWorkspacesListSamples {
    /*
     * x-ms-original-file: 2025-05-31-preview/OnlineExperimentWorkspaces_ListBySubscription.json
     */
    /**
     * Sample code: List Online Experiment Workspaces in a subscription.
     * 
     * @param manager Entry point to OnlineExperimentationManager.
     */
    public static void listOnlineExperimentWorkspacesInASubscription(
        com.azure.resourcemanager.onlineexperimentation.OnlineExperimentationManager manager) {
        manager.onlineExperimentWorkspaces().list(com.azure.core.util.Context.NONE);
    }
}
```

### OnlineExperimentWorkspaces_List

```java
import com.azure.resourcemanager.onlineexperimentation.models.CustomerManagedKeyEncryption;
import com.azure.resourcemanager.onlineexperimentation.models.KeyEncryptionKeyIdentity;
import com.azure.resourcemanager.onlineexperimentation.models.KeyEncryptionKeyIdentityType;
import com.azure.resourcemanager.onlineexperimentation.models.ManagedServiceIdentity;
import com.azure.resourcemanager.onlineexperimentation.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.onlineexperimentation.models.OnlineExperimentWorkspaceProperties;
import com.azure.resourcemanager.onlineexperimentation.models.OnlineExperimentationWorkspaceSku;
import com.azure.resourcemanager.onlineexperimentation.models.OnlineExperimentationWorkspaceSkuName;
import com.azure.resourcemanager.onlineexperimentation.models.ResourceEncryptionConfiguration;
import com.azure.resourcemanager.onlineexperimentation.models.UserAssignedIdentity;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for OnlineExperimentWorkspaces CreateOrUpdate.
 */
public final class OnlineExperimentWorkspacesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-05-31-preview/OnlineExperimentWorkspaces_CreateOrUpdate.json
     */
    /**
     * Sample code: Create or update an OnlineExperimentWorkspace with Free sku.
     * 
     * @param manager Entry point to OnlineExperimentationManager.
     */
    public static void createOrUpdateAnOnlineExperimentWorkspaceWithFreeSku(
        com.azure.resourcemanager.onlineexperimentation.OnlineExperimentationManager manager) {
        manager.onlineExperimentWorkspaces()
            .define("expworkspace7")
            .withRegion("eastus2")
            .withExistingResourceGroup("res9871")
            .withTags(mapOf("newKey", "fakeTokenPlaceholder"))
            .withProperties(new OnlineExperimentWorkspaceProperties().withLogAnalyticsWorkspaceResourceId(
                "/subscriptions/fa5fc227-a624-475e-b696-cdd604c735bc/resourceGroups/res9871/providers/Microsoft.OperationalInsights/workspaces/log9871")
                .withLogsExporterStorageAccountResourceId(
                    "/subscriptions/fa5fc227-a624-475e-b696-cdd604c735bc/resourceGroups/res9871/providers/Microsoft.Storage/storageAccounts/sto9871")
                .withAppConfigurationResourceId(
                    "/subscriptions/fa5fc227-a624-475e-b696-cdd604c735bc/resourceGroups/res9871/providers/Microsoft.AppConfiguration/configurationStores/appconfig9871"))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/fa5fc227-a624-475e-b696-cdd604c735bc/resourceGroups/eu2cgroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id1",
                    new UserAssignedIdentity(),
                    "/subscriptions/fa5fc227-a624-475e-b696-cdd604c735bc/resourceGroups/eu2cgroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id2",
                    new UserAssignedIdentity())))
            .withSku(new OnlineExperimentationWorkspaceSku().withName(OnlineExperimentationWorkspaceSkuName.F0))
            .create();
    }

    /*
     * x-ms-original-file: 2025-05-31-preview/OnlineExperimentWorkspaces_CreateOrUpdateWithEncryption.json
     */
    /**
     * Sample code: Create or update an OnlineExperimentWorkspace with Free sku and customer managed key.
     * 
     * @param manager Entry point to OnlineExperimentationManager.
     */
    public static void createOrUpdateAnOnlineExperimentWorkspaceWithFreeSkuAndCustomerManagedKey(
        com.azure.resourcemanager.onlineexperimentation.OnlineExperimentationManager manager) {
        manager.onlineExperimentWorkspaces()
            .define("expworkspace7")
            .withRegion("eastus2")
            .withExistingResourceGroup("res9871")
            .withTags(mapOf("newKey", "fakeTokenPlaceholder"))
            .withProperties(new OnlineExperimentWorkspaceProperties().withLogAnalyticsWorkspaceResourceId(
                "/subscriptions/fa5fc227-a624-475e-b696-cdd604c735bc/resourceGroups/res9871/providers/Microsoft.OperationalInsights/workspaces/log9871")
                .withLogsExporterStorageAccountResourceId(
                    "/subscriptions/fa5fc227-a624-475e-b696-cdd604c735bc/resourceGroups/res9871/providers/Microsoft.Storage/storageAccounts/sto9871")
                .withAppConfigurationResourceId(
                    "/subscriptions/fa5fc227-a624-475e-b696-cdd604c735bc/resourceGroups/res9871/providers/Microsoft.AppConfiguration/configurationStores/appconfig9871")
                .withEncryption(new ResourceEncryptionConfiguration().withCustomerManagedKeyEncryption(
                    new CustomerManagedKeyEncryption().withKeyEncryptionKeyIdentity(new KeyEncryptionKeyIdentity()
                        .withIdentityType(KeyEncryptionKeyIdentityType.USER_ASSIGNED_IDENTITY)
                        .withUserAssignedIdentityResourceId(
                            "/subscriptions/fa5fc227-a624-475e-b696-cdd604c735bc/resourceGroups/eu2cgroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id1"))
                        .withKeyEncryptionKeyUrl("fakeTokenPlaceholder"))))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/fa5fc227-a624-475e-b696-cdd604c735bc/resourceGroups/eu2cgroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id1",
                    new UserAssignedIdentity(),
                    "/subscriptions/fa5fc227-a624-475e-b696-cdd604c735bc/resourceGroups/eu2cgroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id2",
                    new UserAssignedIdentity())))
            .withSku(new OnlineExperimentationWorkspaceSku().withName(OnlineExperimentationWorkspaceSkuName.F0))
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

### OnlineExperimentWorkspaces_ListByResourceGroup

```java
/**
 * Samples for OnlineExperimentWorkspaces GetByResourceGroup.
 */
public final class OnlineExperimentWorkspacesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-05-31-preview/OnlineExperimentWorkspaces_Get.json
     */
    /**
     * Sample code: Get a single OnlineExperimentWorkspace.
     * 
     * @param manager Entry point to OnlineExperimentationManager.
     */
    public static void getASingleOnlineExperimentWorkspace(
        com.azure.resourcemanager.onlineexperimentation.OnlineExperimentationManager manager) {
        manager.onlineExperimentWorkspaces()
            .getByResourceGroupWithResponse("res9871", "expworkspace3", com.azure.core.util.Context.NONE);
    }
}
```

### OnlineExperimentWorkspaces_Update

```java
/**
 * Samples for OnlineExperimentWorkspaces ListByResourceGroup.
 */
public final class OnlineExperimentWorkspacesListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-05-31-preview/OnlineExperimentWorkspaces_ListByResourceGroup.json
     */
    /**
     * Sample code: List OnlineExperimentWorkspaces in a resource group.
     * 
     * @param manager Entry point to OnlineExperimentationManager.
     */
    public static void listOnlineExperimentWorkspacesInAResourceGroup(
        com.azure.resourcemanager.onlineexperimentation.OnlineExperimentationManager manager) {
        manager.onlineExperimentWorkspaces().listByResourceGroup("res9871", com.azure.core.util.Context.NONE);
    }
}
```

### Operations_List

```java
import com.azure.resourcemanager.onlineexperimentation.models.CustomerManagedKeyEncryption;
import com.azure.resourcemanager.onlineexperimentation.models.KeyEncryptionKeyIdentity;
import com.azure.resourcemanager.onlineexperimentation.models.KeyEncryptionKeyIdentityType;
import com.azure.resourcemanager.onlineexperimentation.models.ManagedServiceIdentity;
import com.azure.resourcemanager.onlineexperimentation.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.onlineexperimentation.models.OnlineExperimentWorkspace;
import com.azure.resourcemanager.onlineexperimentation.models.OnlineExperimentWorkspaceProperties;
import com.azure.resourcemanager.onlineexperimentation.models.ResourceEncryptionConfiguration;
import com.azure.resourcemanager.onlineexperimentation.models.UserAssignedIdentity;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for OnlineExperimentWorkspaces Update.
 */
public final class OnlineExperimentWorkspacesUpdateSamples {
    /*
     * x-ms-original-file: 2025-05-31-preview/OnlineExperimentWorkspaces_Update.json
     */
    /**
     * Sample code: Update an Online Experiment Workspace.
     * 
     * @param manager Entry point to OnlineExperimentationManager.
     */
    public static void updateAnOnlineExperimentWorkspace(
        com.azure.resourcemanager.onlineexperimentation.OnlineExperimentationManager manager) {
        OnlineExperimentWorkspace resource = manager.onlineExperimentWorkspaces()
            .getByResourceGroupWithResponse("res9871", "expworkspace3", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("newKey", "fakeTokenPlaceholder"))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/fa5fc227-a624-475e-b696-cdd604c735bc/resourceGroups/eu2cgroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id1",
                    new UserAssignedIdentity(),
                    "/subscriptions/fa5fc227-a624-475e-b696-cdd604c735bc/resourceGroups/eu2cgroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id2",
                    new UserAssignedIdentity())))
            .apply();
    }

    /*
     * x-ms-original-file: 2025-05-31-preview/OnlineExperimentWorkspaces_UpdateWithEncryption.json
     */
    /**
     * Sample code: Update an Online Experiment Workspace with customer managed encryption key.
     * 
     * @param manager Entry point to OnlineExperimentationManager.
     */
    public static void updateAnOnlineExperimentWorkspaceWithCustomerManagedEncryptionKey(
        com.azure.resourcemanager.onlineexperimentation.OnlineExperimentationManager manager) {
        OnlineExperimentWorkspace resource = manager.onlineExperimentWorkspaces()
            .getByResourceGroupWithResponse("res9871", "expworkspace3", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("newKey", "fakeTokenPlaceholder"))
            .withProperties(new OnlineExperimentWorkspaceProperties().withLogAnalyticsWorkspaceResourceId(
                "/subscriptions/fa5fc227-a624-475e-b696-cdd604c735bc/resourceGroups/res9871/providers/Microsoft.OperationalInsights/workspaces/log9871")
                .withLogsExporterStorageAccountResourceId(
                    "/subscriptions/fa5fc227-a624-475e-b696-cdd604c735bc/resourceGroups/res9871/providers/Microsoft.Storage/storageAccounts/sto9871")
                .withEncryption(new ResourceEncryptionConfiguration().withCustomerManagedKeyEncryption(
                    new CustomerManagedKeyEncryption().withKeyEncryptionKeyIdentity(new KeyEncryptionKeyIdentity()
                        .withIdentityType(KeyEncryptionKeyIdentityType.USER_ASSIGNED_IDENTITY)
                        .withUserAssignedIdentityResourceId(
                            "/subscriptions/fa5fc227-a624-475e-b696-cdd604c735bc/resourceGroups/eu2cgroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id1"))
                        .withKeyEncryptionKeyUrl("fakeTokenPlaceholder"))))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/fa5fc227-a624-475e-b696-cdd604c735bc/resourceGroups/eu2cgroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id1",
                    new UserAssignedIdentity(),
                    "/subscriptions/fa5fc227-a624-475e-b696-cdd604c735bc/resourceGroups/eu2cgroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id2",
                    new UserAssignedIdentity())))
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

