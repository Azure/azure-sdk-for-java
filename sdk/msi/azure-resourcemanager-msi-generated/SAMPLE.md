# Code snippets and samples


## Operations

- [List](#operations_list)

## SystemAssignedIdentities

- [GetByScope](#systemassignedidentities_getbyscope)

## UserAssignedIdentities

- [CreateOrUpdate](#userassignedidentities_createorupdate)
- [Delete](#userassignedidentities_delete)
- [GetByResourceGroup](#userassignedidentities_getbyresourcegroup)
- [List](#userassignedidentities_list)
- [ListAssociatedResources](#userassignedidentities_listassociatedresources)
- [ListByResourceGroup](#userassignedidentities_listbyresourcegroup)
- [Update](#userassignedidentities_update)
### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/msi/resource-manager/Microsoft.ManagedIdentity/preview/2021-09-30-preview/examples/MsiOperationsList.json
     */
    /**
     * Sample code: MsiOperationsList.
     *
     * @param manager Entry point to ManagedServiceIdentityManager.
     */
    public static void msiOperationsList(
        com.azure.resourcemanager.msi.generated.ManagedServiceIdentityManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### SystemAssignedIdentities_GetByScope

```java
import com.azure.core.util.Context;

/** Samples for SystemAssignedIdentities GetByScope. */
public final class SystemAssignedIdentitiesGetByScopeSamples {
    /*
     * x-ms-original-file: specification/msi/resource-manager/Microsoft.ManagedIdentity/preview/2021-09-30-preview/examples/SystemAssignedIdentityGet.json
     */
    /**
     * Sample code: MsiOperationsList.
     *
     * @param manager Entry point to ManagedServiceIdentityManager.
     */
    public static void msiOperationsList(
        com.azure.resourcemanager.msi.generated.ManagedServiceIdentityManager manager) {
        manager.systemAssignedIdentities().getByScopeWithResponse("scope", Context.NONE);
    }
}
```

### UserAssignedIdentities_CreateOrUpdate

```java
import java.util.HashMap;
import java.util.Map;

/** Samples for UserAssignedIdentities CreateOrUpdate. */
public final class UserAssignedIdentitiesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/msi/resource-manager/Microsoft.ManagedIdentity/preview/2021-09-30-preview/examples/IdentityCreate.json
     */
    /**
     * Sample code: IdentityCreate.
     *
     * @param manager Entry point to ManagedServiceIdentityManager.
     */
    public static void identityCreate(com.azure.resourcemanager.msi.generated.ManagedServiceIdentityManager manager) {
        manager
            .userAssignedIdentities()
            .define("resourceName")
            .withRegion("eastus")
            .withExistingResourceGroup("rgName")
            .withTags(mapOf("key1", "value1", "key2", "value2"))
            .create();
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

### UserAssignedIdentities_Delete

```java
import com.azure.core.util.Context;

/** Samples for UserAssignedIdentities Delete. */
public final class UserAssignedIdentitiesDeleteSamples {
    /*
     * x-ms-original-file: specification/msi/resource-manager/Microsoft.ManagedIdentity/preview/2021-09-30-preview/examples/IdentityDelete.json
     */
    /**
     * Sample code: IdentityDelete.
     *
     * @param manager Entry point to ManagedServiceIdentityManager.
     */
    public static void identityDelete(com.azure.resourcemanager.msi.generated.ManagedServiceIdentityManager manager) {
        manager.userAssignedIdentities().deleteWithResponse("rgName", "resourceName", Context.NONE);
    }
}
```

### UserAssignedIdentities_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for UserAssignedIdentities GetByResourceGroup. */
public final class UserAssignedIdentitiesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/msi/resource-manager/Microsoft.ManagedIdentity/preview/2021-09-30-preview/examples/IdentityGet.json
     */
    /**
     * Sample code: IdentityGet.
     *
     * @param manager Entry point to ManagedServiceIdentityManager.
     */
    public static void identityGet(com.azure.resourcemanager.msi.generated.ManagedServiceIdentityManager manager) {
        manager.userAssignedIdentities().getByResourceGroupWithResponse("rgName", "resourceName", Context.NONE);
    }
}
```

### UserAssignedIdentities_List

```java
import com.azure.core.util.Context;

/** Samples for UserAssignedIdentities List. */
public final class UserAssignedIdentitiesListSamples {
    /*
     * x-ms-original-file: specification/msi/resource-manager/Microsoft.ManagedIdentity/preview/2021-09-30-preview/examples/IdentityListBySubscription.json
     */
    /**
     * Sample code: IdentityListBySubscription.
     *
     * @param manager Entry point to ManagedServiceIdentityManager.
     */
    public static void identityListBySubscription(
        com.azure.resourcemanager.msi.generated.ManagedServiceIdentityManager manager) {
        manager.userAssignedIdentities().list(Context.NONE);
    }
}
```

### UserAssignedIdentities_ListAssociatedResources

```java
import com.azure.core.util.Context;

/** Samples for UserAssignedIdentities ListAssociatedResources. */
public final class UserAssignedIdentitiesListAssociatedResourcesSamples {
    /*
     * x-ms-original-file: specification/msi/resource-manager/Microsoft.ManagedIdentity/preview/2021-09-30-preview/examples/IdentityListAssociatedResources.json
     */
    /**
     * Sample code: IdentityListAssociatedResources.
     *
     * @param manager Entry point to ManagedServiceIdentityManager.
     */
    public static void identityListAssociatedResources(
        com.azure.resourcemanager.msi.generated.ManagedServiceIdentityManager manager) {
        manager
            .userAssignedIdentities()
            .listAssociatedResources(
                "testrg", "testid", "contains(name, 'test')", "name asc", 10, 1, null, Context.NONE);
    }
}
```

### UserAssignedIdentities_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for UserAssignedIdentities ListByResourceGroup. */
public final class UserAssignedIdentitiesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/msi/resource-manager/Microsoft.ManagedIdentity/preview/2021-09-30-preview/examples/IdentityListByResourceGroup.json
     */
    /**
     * Sample code: IdentityListByResourceGroup.
     *
     * @param manager Entry point to ManagedServiceIdentityManager.
     */
    public static void identityListByResourceGroup(
        com.azure.resourcemanager.msi.generated.ManagedServiceIdentityManager manager) {
        manager.userAssignedIdentities().listByResourceGroup("rgName", Context.NONE);
    }
}
```

### UserAssignedIdentities_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.msi.generated.models.Identity;
import java.util.HashMap;
import java.util.Map;

/** Samples for UserAssignedIdentities Update. */
public final class UserAssignedIdentitiesUpdateSamples {
    /*
     * x-ms-original-file: specification/msi/resource-manager/Microsoft.ManagedIdentity/preview/2021-09-30-preview/examples/IdentityUpdate.json
     */
    /**
     * Sample code: IdentityUpdate.
     *
     * @param manager Entry point to ManagedServiceIdentityManager.
     */
    public static void identityUpdate(com.azure.resourcemanager.msi.generated.ManagedServiceIdentityManager manager) {
        Identity resource =
            manager
                .userAssignedIdentities()
                .getByResourceGroupWithResponse("rgName", "resourceName", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("key1", "value1", "key2", "value2")).apply();
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

