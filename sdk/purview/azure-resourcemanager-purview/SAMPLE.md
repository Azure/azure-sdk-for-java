# Code snippets and samples


## Accounts

- [AddRootCollectionAdmin](#accounts_addrootcollectionadmin)
- [CheckNameAvailability](#accounts_checknameavailability)
- [CreateOrUpdate](#accounts_createorupdate)
- [Delete](#accounts_delete)
- [GetByResourceGroup](#accounts_getbyresourcegroup)
- [List](#accounts_list)
- [ListByResourceGroup](#accounts_listbyresourcegroup)
- [ListKeys](#accounts_listkeys)
- [Update](#accounts_update)

## DefaultAccounts

- [Get](#defaultaccounts_get)
- [Remove](#defaultaccounts_remove)
- [Set](#defaultaccounts_set)

## Operations

- [List](#operations_list)

## PrivateEndpointConnections

- [CreateOrUpdate](#privateendpointconnections_createorupdate)
- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [ListByAccount](#privateendpointconnections_listbyaccount)

## PrivateLinkResources

- [GetByGroupId](#privatelinkresources_getbygroupid)
- [ListByAccount](#privatelinkresources_listbyaccount)
### Accounts_AddRootCollectionAdmin

```java
import com.azure.resourcemanager.purview.models.CollectionAdminUpdate;

/** Samples for Accounts AddRootCollectionAdmin. */
public final class AccountsAddRootCollectionAdminSamples {
    /*
     * x-ms-original-file: specification/purview/resource-manager/Microsoft.Purview/stable/2021-07-01/examples/Accounts_AddRootCollectionAdmin.json
     */
    /**
     * Sample code: Accounts_AddRootCollectionAdmin.
     *
     * @param manager Entry point to PurviewManager.
     */
    public static void accountsAddRootCollectionAdmin(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager
            .accounts()
            .addRootCollectionAdminWithResponse(
                "SampleResourceGroup",
                "account1",
                new CollectionAdminUpdate().withObjectId("7e8de0e7-2bfc-4e1f-9659-2a5785e4356f"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Accounts_CheckNameAvailability

```java
import com.azure.resourcemanager.purview.models.CheckNameAvailabilityRequest;

/** Samples for Accounts CheckNameAvailability. */
public final class AccountsCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/purview/resource-manager/Microsoft.Purview/stable/2021-07-01/examples/Accounts_CheckNameAvailability.json
     */
    /**
     * Sample code: Accounts_CheckNameAvailability.
     *
     * @param manager Entry point to PurviewManager.
     */
    public static void accountsCheckNameAvailability(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager
            .accounts()
            .checkNameAvailabilityWithResponse(
                new CheckNameAvailabilityRequest().withName("account1").withType("Microsoft.Purview/accounts"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Accounts_CreateOrUpdate

```java
/** Samples for Accounts CreateOrUpdate. */
public final class AccountsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/purview/resource-manager/Microsoft.Purview/stable/2021-07-01/examples/Accounts_CreateOrUpdate.json
     */
    /**
     * Sample code: Accounts_CreateOrUpdate.
     *
     * @param manager Entry point to PurviewManager.
     */
    public static void accountsCreateOrUpdate(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager
            .accounts()
            .define("account1")
            .withRegion("West US 2")
            .withExistingResourceGroup("SampleResourceGroup")
            .withManagedResourceGroupName("custom-rgname")
            .create();
    }
}
```

### Accounts_Delete

```java
/** Samples for Accounts Delete. */
public final class AccountsDeleteSamples {
    /*
     * x-ms-original-file: specification/purview/resource-manager/Microsoft.Purview/stable/2021-07-01/examples/Accounts_Delete.json
     */
    /**
     * Sample code: Accounts_Delete.
     *
     * @param manager Entry point to PurviewManager.
     */
    public static void accountsDelete(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager.accounts().delete("SampleResourceGroup", "account1", com.azure.core.util.Context.NONE);
    }
}
```

### Accounts_GetByResourceGroup

```java
/** Samples for Accounts GetByResourceGroup. */
public final class AccountsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/purview/resource-manager/Microsoft.Purview/stable/2021-07-01/examples/Accounts_Get.json
     */
    /**
     * Sample code: Accounts_Get.
     *
     * @param manager Entry point to PurviewManager.
     */
    public static void accountsGet(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager
            .accounts()
            .getByResourceGroupWithResponse("SampleResourceGroup", "account1", com.azure.core.util.Context.NONE);
    }
}
```

### Accounts_List

```java
/** Samples for Accounts List. */
public final class AccountsListSamples {
    /*
     * x-ms-original-file: specification/purview/resource-manager/Microsoft.Purview/stable/2021-07-01/examples/Accounts_ListBySubscription.json
     */
    /**
     * Sample code: Accounts_ListBySubscription.
     *
     * @param manager Entry point to PurviewManager.
     */
    public static void accountsListBySubscription(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager.accounts().list(null, com.azure.core.util.Context.NONE);
    }
}
```

### Accounts_ListByResourceGroup

```java
/** Samples for Accounts ListByResourceGroup. */
public final class AccountsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/purview/resource-manager/Microsoft.Purview/stable/2021-07-01/examples/Accounts_ListByResourceGroup.json
     */
    /**
     * Sample code: Accounts_ListByResourceGroup.
     *
     * @param manager Entry point to PurviewManager.
     */
    public static void accountsListByResourceGroup(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager.accounts().listByResourceGroup("SampleResourceGroup", null, com.azure.core.util.Context.NONE);
    }
}
```

### Accounts_ListKeys

```java
/** Samples for Accounts ListKeys. */
public final class AccountsListKeysSamples {
    /*
     * x-ms-original-file: specification/purview/resource-manager/Microsoft.Purview/stable/2021-07-01/examples/Accounts_ListKeys.json
     */
    /**
     * Sample code: Accounts_ListKeys.
     *
     * @param manager Entry point to PurviewManager.
     */
    public static void accountsListKeys(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager.accounts().listKeysWithResponse("SampleResourceGroup", "account1", com.azure.core.util.Context.NONE);
    }
}
```

### Accounts_Update

```java
import com.azure.resourcemanager.purview.models.Account;
import java.util.HashMap;
import java.util.Map;

/** Samples for Accounts Update. */
public final class AccountsUpdateSamples {
    /*
     * x-ms-original-file: specification/purview/resource-manager/Microsoft.Purview/stable/2021-07-01/examples/Accounts_Update.json
     */
    /**
     * Sample code: Accounts_Update.
     *
     * @param manager Entry point to PurviewManager.
     */
    public static void accountsUpdate(com.azure.resourcemanager.purview.PurviewManager manager) {
        Account resource =
            manager
                .accounts()
                .getByResourceGroupWithResponse("SampleResourceGroup", "account1", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("newTag", "New tag value.")).apply();
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

### DefaultAccounts_Get

```java
import com.azure.resourcemanager.purview.models.ScopeType;
import java.util.UUID;

/** Samples for DefaultAccounts Get. */
public final class DefaultAccountsGetSamples {
    /*
     * x-ms-original-file: specification/purview/resource-manager/Microsoft.Purview/stable/2021-07-01/examples/DefaultAccounts_Get.json
     */
    /**
     * Sample code: DefaultAccounts_Get.
     *
     * @param manager Entry point to PurviewManager.
     */
    public static void defaultAccountsGet(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager
            .defaultAccounts()
            .getWithResponse(
                UUID.fromString("11733A4E-BA84-46FF-91D1-AFF1A3215A90"),
                ScopeType.TENANT,
                "11733A4E-BA84-46FF-91D1-AFF1A3215A90",
                com.azure.core.util.Context.NONE);
    }
}
```

### DefaultAccounts_Remove

```java
import com.azure.resourcemanager.purview.models.ScopeType;
import java.util.UUID;

/** Samples for DefaultAccounts Remove. */
public final class DefaultAccountsRemoveSamples {
    /*
     * x-ms-original-file: specification/purview/resource-manager/Microsoft.Purview/stable/2021-07-01/examples/DefaultAccounts_Remove.json
     */
    /**
     * Sample code: DefaultAccounts_Remove.
     *
     * @param manager Entry point to PurviewManager.
     */
    public static void defaultAccountsRemove(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager
            .defaultAccounts()
            .removeWithResponse(
                UUID.fromString("11733A4E-BA84-46FF-91D1-AFF1A3215A90"),
                ScopeType.TENANT,
                "11733A4E-BA84-46FF-91D1-AFF1A3215A90",
                com.azure.core.util.Context.NONE);
    }
}
```

### DefaultAccounts_Set

```java
import com.azure.resourcemanager.purview.fluent.models.DefaultAccountPayloadInner;
import com.azure.resourcemanager.purview.models.ScopeType;

/** Samples for DefaultAccounts Set. */
public final class DefaultAccountsSetSamples {
    /*
     * x-ms-original-file: specification/purview/resource-manager/Microsoft.Purview/stable/2021-07-01/examples/DefaultAccounts_Set.json
     */
    /**
     * Sample code: DefaultAccounts_Set.
     *
     * @param manager Entry point to PurviewManager.
     */
    public static void defaultAccountsSet(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager
            .defaultAccounts()
            .setWithResponse(
                new DefaultAccountPayloadInner()
                    .withAccountName("myDefaultAccount")
                    .withResourceGroupName("rg-1")
                    .withScope("11733A4E-BA84-46FF-91D1-AFF1A3215A90")
                    .withScopeTenantId("11733A4E-BA84-46FF-91D1-AFF1A3215A90")
                    .withScopeType(ScopeType.TENANT)
                    .withSubscriptionId("12345678-1234-1234-12345678aaa"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/purview/resource-manager/Microsoft.Purview/stable/2021-07-01/examples/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     *
     * @param manager Entry point to PurviewManager.
     */
    public static void operationsList(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_CreateOrUpdate

```java
import com.azure.resourcemanager.purview.models.PrivateLinkServiceConnectionState;
import com.azure.resourcemanager.purview.models.Status;

/** Samples for PrivateEndpointConnections CreateOrUpdate. */
public final class PrivateEndpointConnectionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/purview/resource-manager/Microsoft.Purview/stable/2021-07-01/examples/PrivateEndpointConnections_CreateOrUpdate.json
     */
    /**
     * Sample code: PrivateEndpointConnections_CreateOrUpdate.
     *
     * @param manager Entry point to PurviewManager.
     */
    public static void privateEndpointConnectionsCreateOrUpdate(
        com.azure.resourcemanager.purview.PurviewManager manager) {
        manager
            .privateEndpointConnections()
            .define("privateEndpointConnection1")
            .withExistingAccount("SampleResourceGroup", "account1")
            .withPrivateLinkServiceConnectionState(
                new PrivateLinkServiceConnectionState()
                    .withDescription("Approved by johndoe@company.com")
                    .withStatus(Status.APPROVED))
            .create();
    }
}
```

### PrivateEndpointConnections_Delete

```java
/** Samples for PrivateEndpointConnections Delete. */
public final class PrivateEndpointConnectionsDeleteSamples {
    /*
     * x-ms-original-file: specification/purview/resource-manager/Microsoft.Purview/stable/2021-07-01/examples/PrivateEndpointConnections_Delete.json
     */
    /**
     * Sample code: PrivateEndpointConnections_Delete.
     *
     * @param manager Entry point to PurviewManager.
     */
    public static void privateEndpointConnectionsDelete(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager
            .privateEndpointConnections()
            .delete("SampleResourceGroup", "account1", "privateEndpointConnection1", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_Get

```java
/** Samples for PrivateEndpointConnections Get. */
public final class PrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file: specification/purview/resource-manager/Microsoft.Purview/stable/2021-07-01/examples/PrivateEndpointConnections_Get.json
     */
    /**
     * Sample code: PrivateEndpointConnections_Get.
     *
     * @param manager Entry point to PurviewManager.
     */
    public static void privateEndpointConnectionsGet(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager
            .privateEndpointConnections()
            .getWithResponse(
                "SampleResourceGroup", "account1", "privateEndpointConnection1", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_ListByAccount

```java
/** Samples for PrivateEndpointConnections ListByAccount. */
public final class PrivateEndpointConnectionsListByAccountSamples {
    /*
     * x-ms-original-file: specification/purview/resource-manager/Microsoft.Purview/stable/2021-07-01/examples/PrivateEndpointConnections_ListByAccount.json
     */
    /**
     * Sample code: PrivateEndpointConnections_ListByAccount.
     *
     * @param manager Entry point to PurviewManager.
     */
    public static void privateEndpointConnectionsListByAccount(
        com.azure.resourcemanager.purview.PurviewManager manager) {
        manager
            .privateEndpointConnections()
            .listByAccount("SampleResourceGroup", "account1", null, com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkResources_GetByGroupId

```java
/** Samples for PrivateLinkResources GetByGroupId. */
public final class PrivateLinkResourcesGetByGroupIdSamples {
    /*
     * x-ms-original-file: specification/purview/resource-manager/Microsoft.Purview/stable/2021-07-01/examples/PrivateLinkResources_GetByGroupId.json
     */
    /**
     * Sample code: PrivateLinkResources_GetByGroupId.
     *
     * @param manager Entry point to PurviewManager.
     */
    public static void privateLinkResourcesGetByGroupId(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager
            .privateLinkResources()
            .getByGroupIdWithResponse("SampleResourceGroup", "account1", "group1", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkResources_ListByAccount

```java
/** Samples for PrivateLinkResources ListByAccount. */
public final class PrivateLinkResourcesListByAccountSamples {
    /*
     * x-ms-original-file: specification/purview/resource-manager/Microsoft.Purview/stable/2021-07-01/examples/PrivateLinkResources_ListByAccount.json
     */
    /**
     * Sample code: PrivateLinkResources_ListByAccount.
     *
     * @param manager Entry point to PurviewManager.
     */
    public static void privateLinkResourcesListByAccount(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager
            .privateLinkResources()
            .listByAccount("SampleResourceGroup", "account1", com.azure.core.util.Context.NONE);
    }
}
```

