# Code snippets and samples


## OnlineExperimentationWorkspaces

- [CreateOrUpdate](#onlineexperimentationworkspaces_createorupdate)
- [Delete](#onlineexperimentationworkspaces_delete)
- [GetByResourceGroup](#onlineexperimentationworkspaces_getbyresourcegroup)
- [List](#onlineexperimentationworkspaces_list)
- [ListByResourceGroup](#onlineexperimentationworkspaces_listbyresourcegroup)
- [Update](#onlineexperimentationworkspaces_update)

## Operations

- [List](#operations_list)
### OnlineExperimentationWorkspaces_CreateOrUpdate

```java
import com.azure.resourcemanager.onlineexperimentation.models.CustomerManagedKeyEncryption;
import com.azure.resourcemanager.onlineexperimentation.models.KeyEncryptionKeyIdentity;
import com.azure.resourcemanager.onlineexperimentation.models.KeyEncryptionKeyIdentityType;
import com.azure.resourcemanager.onlineexperimentation.models.ManagedServiceIdentity;
import com.azure.resourcemanager.onlineexperimentation.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.onlineexperimentation.models.OnlineExperimentationWorkspaceProperties;
import com.azure.resourcemanager.onlineexperimentation.models.OnlineExperimentationWorkspaceSku;
import com.azure.resourcemanager.onlineexperimentation.models.OnlineExperimentationWorkspaceSkuName;
import com.azure.resourcemanager.onlineexperimentation.models.ResourceEncryptionConfiguration;
import com.azure.resourcemanager.onlineexperimentation.models.UserAssignedIdentity;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for OnlineExperimentationWorkspaces CreateOrUpdate.
 */
public final class OnlineExperimentationWorkspacesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-05-31-preview/OnlineExperimentationWorkspaces_CreateOrUpdateWithEncryption.json
     */
    /**
     * Sample code: Create or update an OnlineExperimentationWorkspace with Free sku and customer managed key.
     * 
     * @param manager Entry point to OnlineExperimentationManager.
     */
    public static void createOrUpdateAnOnlineExperimentationWorkspaceWithFreeSkuAndCustomerManagedKey(
        com.azure.resourcemanager.onlineexperimentation.OnlineExperimentationManager manager) {
        manager.onlineExperimentationWorkspaces()
            .define("expworkspace7")
            .withRegion("eastus2")
            .withExistingResourceGroup("res9871")
            .withTags(mapOf("newKey", "fakeTokenPlaceholder"))
            .withProperties(new OnlineExperimentationWorkspaceProperties().withLogAnalyticsWorkspaceResourceId(
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

    /*
     * x-ms-original-file: 2025-05-31-preview/OnlineExperimentationWorkspaces_CreateOrUpdate.json
     */
    /**
     * Sample code: Create or update an OnlineExperimentationWorkspace with Free sku.
     * 
     * @param manager Entry point to OnlineExperimentationManager.
     */
    public static void createOrUpdateAnOnlineExperimentationWorkspaceWithFreeSku(
        com.azure.resourcemanager.onlineexperimentation.OnlineExperimentationManager manager) {
        manager.onlineExperimentationWorkspaces()
            .define("expworkspace7")
            .withRegion("eastus2")
            .withExistingResourceGroup("res9871")
            .withTags(mapOf("newKey", "fakeTokenPlaceholder"))
            .withProperties(new OnlineExperimentationWorkspaceProperties().withLogAnalyticsWorkspaceResourceId(
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

### OnlineExperimentationWorkspaces_Delete

```java
/**
 * Samples for OnlineExperimentationWorkspaces Delete.
 */
public final class OnlineExperimentationWorkspacesDeleteSamples {
    /*
     * x-ms-original-file: 2025-05-31-preview/OnlineExperimentationWorkspaces_Delete.json
     */
    /**
     * Sample code: Delete an Online Experimentation Workspace.
     * 
     * @param manager Entry point to OnlineExperimentationManager.
     */
    public static void deleteAnOnlineExperimentationWorkspace(
        com.azure.resourcemanager.onlineexperimentation.OnlineExperimentationManager manager) {
        manager.onlineExperimentationWorkspaces().delete("res9871", "expworkspace3", com.azure.core.util.Context.NONE);
    }
}
```

### OnlineExperimentationWorkspaces_GetByResourceGroup

```java
/**
 * Samples for OnlineExperimentationWorkspaces GetByResourceGroup.
 */
public final class OnlineExperimentationWorkspacesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-05-31-preview/OnlineExperimentationWorkspaces_Get.json
     */
    /**
     * Sample code: Get a single OnlineExperimentationWorkspace.
     * 
     * @param manager Entry point to OnlineExperimentationManager.
     */
    public static void getASingleOnlineExperimentationWorkspace(
        com.azure.resourcemanager.onlineexperimentation.OnlineExperimentationManager manager) {
        manager.onlineExperimentationWorkspaces()
            .getByResourceGroupWithResponse("res9871", "expworkspace3", com.azure.core.util.Context.NONE);
    }
}
```

### OnlineExperimentationWorkspaces_List

```java
/**
 * Samples for OnlineExperimentationWorkspaces List.
 */
public final class OnlineExperimentationWorkspacesListSamples {
    /*
     * x-ms-original-file: 2025-05-31-preview/OnlineExperimentationWorkspaces_ListBySubscription.json
     */
    /**
     * Sample code: List Online Experimentation Workspaces in a subscription.
     * 
     * @param manager Entry point to OnlineExperimentationManager.
     */
    public static void listOnlineExperimentationWorkspacesInASubscription(
        com.azure.resourcemanager.onlineexperimentation.OnlineExperimentationManager manager) {
        manager.onlineExperimentationWorkspaces().list(com.azure.core.util.Context.NONE);
    }
}
```

### OnlineExperimentationWorkspaces_ListByResourceGroup

```java
/**
 * Samples for OnlineExperimentationWorkspaces ListByResourceGroup.
 */
public final class OnlineExperimentationWorkspacesListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-05-31-preview/OnlineExperimentationWorkspaces_ListByResourceGroup.json
     */
    /**
     * Sample code: List OnlineExperimentationWorkspaces in a resource group.
     * 
     * @param manager Entry point to OnlineExperimentationManager.
     */
    public static void listOnlineExperimentationWorkspacesInAResourceGroup(
        com.azure.resourcemanager.onlineexperimentation.OnlineExperimentationManager manager) {
        manager.onlineExperimentationWorkspaces().listByResourceGroup("res9871", com.azure.core.util.Context.NONE);
    }
}
```

### OnlineExperimentationWorkspaces_Update

```java
import com.azure.resourcemanager.onlineexperimentation.models.CustomerManagedKeyEncryption;
import com.azure.resourcemanager.onlineexperimentation.models.KeyEncryptionKeyIdentity;
import com.azure.resourcemanager.onlineexperimentation.models.KeyEncryptionKeyIdentityType;
import com.azure.resourcemanager.onlineexperimentation.models.ManagedServiceIdentity;
import com.azure.resourcemanager.onlineexperimentation.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.onlineexperimentation.models.OnlineExperimentationWorkspace;
import com.azure.resourcemanager.onlineexperimentation.models.OnlineExperimentationWorkspacePatchProperties;
import com.azure.resourcemanager.onlineexperimentation.models.ResourceEncryptionConfiguration;
import com.azure.resourcemanager.onlineexperimentation.models.UserAssignedIdentity;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for OnlineExperimentationWorkspaces Update.
 */
public final class OnlineExperimentationWorkspacesUpdateSamples {
    /*
     * x-ms-original-file: 2025-05-31-preview/OnlineExperimentationWorkspaces_Update.json
     */
    /**
     * Sample code: Update an Online Experimentation Workspace.
     * 
     * @param manager Entry point to OnlineExperimentationManager.
     */
    public static void updateAnOnlineExperimentationWorkspace(
        com.azure.resourcemanager.onlineexperimentation.OnlineExperimentationManager manager) {
        OnlineExperimentationWorkspace resource = manager.onlineExperimentationWorkspaces()
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
     * x-ms-original-file: 2025-05-31-preview/OnlineExperimentationWorkspaces_UpdateWithEncryption.json
     */
    /**
     * Sample code: Update an Online Experimentation Workspace with customer managed encryption key.
     * 
     * @param manager Entry point to OnlineExperimentationManager.
     */
    public static void updateAnOnlineExperimentationWorkspaceWithCustomerManagedEncryptionKey(
        com.azure.resourcemanager.onlineexperimentation.OnlineExperimentationManager manager) {
        OnlineExperimentationWorkspace resource = manager.onlineExperimentationWorkspaces()
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
            .withProperties(new OnlineExperimentationWorkspacePatchProperties().withLogAnalyticsWorkspaceResourceId(
                "/subscriptions/fa5fc227-a624-475e-b696-cdd604c735bc/resourceGroups/res9871/providers/Microsoft.OperationalInsights/workspaces/log9871")
                .withLogsExporterStorageAccountResourceId(
                    "/subscriptions/fa5fc227-a624-475e-b696-cdd604c735bc/resourceGroups/res9871/providers/Microsoft.Storage/storageAccounts/sto9871")
                .withEncryption(new ResourceEncryptionConfiguration().withCustomerManagedKeyEncryption(
                    new CustomerManagedKeyEncryption().withKeyEncryptionKeyIdentity(new KeyEncryptionKeyIdentity()
                        .withIdentityType(KeyEncryptionKeyIdentityType.USER_ASSIGNED_IDENTITY)
                        .withUserAssignedIdentityResourceId(
                            "/subscriptions/fa5fc227-a624-475e-b696-cdd604c735bc/resourceGroups/eu2cgroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id1"))
                        .withKeyEncryptionKeyUrl("fakeTokenPlaceholder"))))
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
     * x-ms-original-file: 2025-05-31-preview/OnlineExperimentationWorkspaces_OperationsList.json
     */
    /**
     * Sample code: List Online Experimentation Workspaces operations.
     * 
     * @param manager Entry point to OnlineExperimentationManager.
     */
    public static void listOnlineExperimentationWorkspacesOperations(
        com.azure.resourcemanager.onlineexperimentation.OnlineExperimentationManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

