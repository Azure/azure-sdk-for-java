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
import com.azure.core.util.Context;
import com.azure.resourcemanager.purview.models.CollectionAdminUpdate;

/** Samples for Accounts AddRootCollectionAdmin. */
public final class AccountsAddRootCollectionAdminSamples {
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
                Context.NONE);
    }
}
```

### Accounts_CheckNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.purview.models.CheckNameAvailabilityRequest;

/** Samples for Accounts CheckNameAvailability. */
public final class AccountsCheckNameAvailabilitySamples {
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
                Context.NONE);
    }
}
```

### Accounts_CreateOrUpdate

```java
import com.azure.resourcemanager.purview.models.AccountSku;
import com.azure.resourcemanager.purview.models.Name;

/** Samples for Accounts CreateOrUpdate. */
public final class AccountsCreateOrUpdateSamples {
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
            .withSku(new AccountSku().withCapacity(4).withName(Name.STANDARD))
            .withManagedResourceGroupName("custom-rgname")
            .create();
    }
}
```

### Accounts_Delete

```java
import com.azure.core.util.Context;

/** Samples for Accounts Delete. */
public final class AccountsDeleteSamples {
    /**
     * Sample code: Accounts_Delete.
     *
     * @param manager Entry point to PurviewManager.
     */
    public static void accountsDelete(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager.accounts().delete("SampleResourceGroup", "account1", Context.NONE);
    }
}
```

### Accounts_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Accounts GetByResourceGroup. */
public final class AccountsGetByResourceGroupSamples {
    /**
     * Sample code: Accounts_Get.
     *
     * @param manager Entry point to PurviewManager.
     */
    public static void accountsGet(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager.accounts().getByResourceGroupWithResponse("SampleResourceGroup", "account1", Context.NONE);
    }
}
```

### Accounts_List

```java
import com.azure.core.util.Context;

/** Samples for Accounts List. */
public final class AccountsListSamples {
    /**
     * Sample code: Accounts_ListBySubscription.
     *
     * @param manager Entry point to PurviewManager.
     */
    public static void accountsListBySubscription(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager.accounts().list(null, Context.NONE);
    }
}
```

### Accounts_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Accounts ListByResourceGroup. */
public final class AccountsListByResourceGroupSamples {
    /**
     * Sample code: Accounts_ListByResourceGroup.
     *
     * @param manager Entry point to PurviewManager.
     */
    public static void accountsListByResourceGroup(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager.accounts().listByResourceGroup("SampleResourceGroup", null, Context.NONE);
    }
}
```

### Accounts_ListKeys

```java
import com.azure.core.util.Context;

/** Samples for Accounts ListKeys. */
public final class AccountsListKeysSamples {
    /**
     * Sample code: Accounts_ListKeys.
     *
     * @param manager Entry point to PurviewManager.
     */
    public static void accountsListKeys(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager.accounts().listKeysWithResponse("SampleResourceGroup", "account1", Context.NONE);
    }
}
```

### Accounts_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.purview.models.Account;
import java.util.HashMap;
import java.util.Map;

/** Samples for Accounts Update. */
public final class AccountsUpdateSamples {
    /**
     * Sample code: Accounts_Update.
     *
     * @param manager Entry point to PurviewManager.
     */
    public static void accountsUpdate(com.azure.resourcemanager.purview.PurviewManager manager) {
        Account resource =
            manager
                .accounts()
                .getByResourceGroupWithResponse("SampleResourceGroup", "account1", Context.NONE)
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
import com.azure.core.util.Context;
import com.azure.resourcemanager.purview.models.ScopeType;
import java.util.UUID;

/** Samples for DefaultAccounts Get. */
public final class DefaultAccountsGetSamples {
    /**
     * Sample code: DefaultAccounts_Get.
     *
     * @param manager Entry point to PurviewManager.
     */
    public static void defaultAccountsGet(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager
            .defaultAccounts()
            .getWithResponse(
                UUID.fromString("12345678-1234-1234-12345678abc"),
                ScopeType.TENANT,
                "12345678-1234-1234-12345678abc",
                Context.NONE);
    }
}
```

### DefaultAccounts_Remove

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.purview.models.ScopeType;
import java.util.UUID;

/** Samples for DefaultAccounts Remove. */
public final class DefaultAccountsRemoveSamples {
    /**
     * Sample code: DefaultAccounts_Remove.
     *
     * @param manager Entry point to PurviewManager.
     */
    public static void defaultAccountsRemove(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager
            .defaultAccounts()
            .removeWithResponse(
                UUID.fromString("12345678-1234-1234-12345678abc"),
                ScopeType.TENANT,
                "12345678-1234-1234-12345678abc",
                Context.NONE);
    }
}
```

### DefaultAccounts_Set

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.purview.fluent.models.DefaultAccountPayloadInner;
import com.azure.resourcemanager.purview.models.ScopeType;

/** Samples for DefaultAccounts Set. */
public final class DefaultAccountsSetSamples {
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
                    .withScope("12345678-1234-1234-12345678abc")
                    .withScopeTenantId("12345678-1234-1234-12345678abc")
                    .withScopeType(ScopeType.TENANT)
                    .withSubscriptionId("12345678-1234-1234-12345678aaa"),
                Context.NONE);
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /**
     * Sample code: Operations_List.
     *
     * @param manager Entry point to PurviewManager.
     */
    public static void operationsList(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### PrivateEndpointConnections_CreateOrUpdate

```java
import com.azure.resourcemanager.purview.models.PrivateLinkServiceConnectionState;
import com.azure.resourcemanager.purview.models.Status;

/** Samples for PrivateEndpointConnections CreateOrUpdate. */
public final class PrivateEndpointConnectionsCreateOrUpdateSamples {
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
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections Delete. */
public final class PrivateEndpointConnectionsDeleteSamples {
    /**
     * Sample code: PrivateEndpointConnections_Delete.
     *
     * @param manager Entry point to PurviewManager.
     */
    public static void privateEndpointConnectionsDelete(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager
            .privateEndpointConnections()
            .delete("SampleResourceGroup", "account1", "privateEndpointConnection1", Context.NONE);
    }
}
```

### PrivateEndpointConnections_Get

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections Get. */
public final class PrivateEndpointConnectionsGetSamples {
    /**
     * Sample code: PrivateEndpointConnections_Get.
     *
     * @param manager Entry point to PurviewManager.
     */
    public static void privateEndpointConnectionsGet(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager
            .privateEndpointConnections()
            .getWithResponse("SampleResourceGroup", "account1", "privateEndpointConnection1", Context.NONE);
    }
}
```

### PrivateEndpointConnections_ListByAccount

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections ListByAccount. */
public final class PrivateEndpointConnectionsListByAccountSamples {
    /**
     * Sample code: PrivateEndpointConnections_ListByAccount.
     *
     * @param manager Entry point to PurviewManager.
     */
    public static void privateEndpointConnectionsListByAccount(
        com.azure.resourcemanager.purview.PurviewManager manager) {
        manager.privateEndpointConnections().listByAccount("SampleResourceGroup", "account1", null, Context.NONE);
    }
}
```

### PrivateLinkResources_GetByGroupId

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkResources GetByGroupId. */
public final class PrivateLinkResourcesGetByGroupIdSamples {
    /**
     * Sample code: PrivateLinkResources_GetByGroupId.
     *
     * @param manager Entry point to PurviewManager.
     */
    public static void privateLinkResourcesGetByGroupId(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager
            .privateLinkResources()
            .getByGroupIdWithResponse("SampleResourceGroup", "account1", "group1", Context.NONE);
    }
}
```

### PrivateLinkResources_ListByAccount

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkResources ListByAccount. */
public final class PrivateLinkResourcesListByAccountSamples {
    /**
     * Sample code: PrivateLinkResources_ListByAccount.
     *
     * @param manager Entry point to PurviewManager.
     */
    public static void privateLinkResourcesListByAccount(com.azure.resourcemanager.purview.PurviewManager manager) {
        manager.privateLinkResources().listByAccount("SampleResourceGroup", "account1", Context.NONE);
    }
}
```

