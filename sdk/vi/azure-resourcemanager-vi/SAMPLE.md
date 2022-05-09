# Code snippets and samples


## Accounts

- [CheckNameAvailability](#accounts_checknameavailability)
- [CreateOrUpdate](#accounts_createorupdate)
- [Delete](#accounts_delete)
- [GetByResourceGroup](#accounts_getbyresourcegroup)
- [List](#accounts_list)
- [ListByResourceGroup](#accounts_listbyresourcegroup)
- [Update](#accounts_update)

## ClassicAccounts

- [GetDetails](#classicaccounts_getdetails)

## Generate

- [AccessToken](#generate_accesstoken)

## Operations

- [List](#operations_list)

## UserClassicAccounts

- [List](#userclassicaccounts_list)
### Accounts_CheckNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.vi.models.AccountCheckNameAvailabilityParameters;
import com.azure.resourcemanager.vi.models.Type;

/** Samples for Accounts CheckNameAvailability. */
public final class AccountsCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/vi/resource-manager/Microsoft.VideoIndexer/preview/2022-04-13-preview/examples/ViAccountCheckNameAvailabilityFree.json
     */
    /**
     * Sample code: Check free account name availability.
     *
     * @param manager Entry point to ViManager.
     */
    public static void checkFreeAccountNameAvailability(com.azure.resourcemanager.vi.ViManager manager) {
        manager
            .accounts()
            .checkNameAvailabilityWithResponse(
                new AccountCheckNameAvailabilityParameters()
                    .withName("vi1")
                    .withType(Type.MICROSOFT_VIDEO_INDEXER_ACCOUNTS),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/vi/resource-manager/Microsoft.VideoIndexer/preview/2022-04-13-preview/examples/ViAccountCheckNameAvailabilityTaken.json
     */
    /**
     * Sample code: Check taken account name availability.
     *
     * @param manager Entry point to ViManager.
     */
    public static void checkTakenAccountNameAvailability(com.azure.resourcemanager.vi.ViManager manager) {
        manager
            .accounts()
            .checkNameAvailabilityWithResponse(
                new AccountCheckNameAvailabilityParameters()
                    .withName("vi1")
                    .withType(Type.MICROSOFT_VIDEO_INDEXER_ACCOUNTS),
                Context.NONE);
    }
}
```

### Accounts_CreateOrUpdate

```java
import com.azure.resourcemanager.vi.models.ManagedServiceIdentity;
import com.azure.resourcemanager.vi.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.vi.models.MediaServicesForPutRequest;
import com.azure.resourcemanager.vi.models.UserAssignedIdentity;
import java.util.HashMap;
import java.util.Map;

/** Samples for Accounts CreateOrUpdate. */
public final class AccountsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/vi/resource-manager/Microsoft.VideoIndexer/preview/2022-04-13-preview/examples/ViAccountPut/ViAccountPut10.json
     */
    /**
     * Sample code: Put example #10.
     *
     * @param manager Entry point to ViManager.
     */
    public static void putExample10(com.azure.resourcemanager.vi.ViManager manager) {
        manager
            .accounts()
            .define("contosto-videoanalyzer")
            .withRegion("NorthEurope")
            .withExistingResourceGroup("contosto-videoanalyzer-rg")
            .withIdentity(
                new ManagedServiceIdentity()
                    .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentities(mapOf()))
            .withMediaServices(
                new MediaServicesForPutRequest()
                    .withResourceId(
                        "/subscriptions/xxx/resourceGroups/contoso-videoanalyzer-rg/providers/Microsoft.Media/mediaservices/contoso-videoanalyzer-ms")
                    .withUserAssignedIdentity(
                        "/subscriptions/xxx/resourceGroups/contoso-videoanalyzer-rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/contoso-videoanalyzer-mi"))
            .create();
    }

    /*
     * x-ms-original-file: specification/vi/resource-manager/Microsoft.VideoIndexer/preview/2022-04-13-preview/examples/ViAccountPut/ViAccountPut8.json
     */
    /**
     * Sample code: Put example #8.
     *
     * @param manager Entry point to ViManager.
     */
    public static void putExample8(com.azure.resourcemanager.vi.ViManager manager) {
        manager
            .accounts()
            .define("contosto-videoanalyzer")
            .withRegion("NorthEurope")
            .withExistingResourceGroup("contosto-videoanalyzer-rg")
            .withIdentity(
                new ManagedServiceIdentity()
                    .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "subscriptions/24237b72-8546-4da5-b204-8c3cb76dd930/resourceGroups/uratzmon-rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/talshoham",
                            new UserAssignedIdentity())))
            .withMediaServices(
                new MediaServicesForPutRequest()
                    .withResourceId(
                        "/subscriptions/24237b72-8546-4da5-b204-8c3cb76dd930/resourceGroups/uratzmon-rg/providers/Microsoft.Media/mediaservices/talshoham"))
            .create();
    }

    /*
     * x-ms-original-file: specification/vi/resource-manager/Microsoft.VideoIndexer/preview/2022-04-13-preview/examples/ViAccountPut/ViAccountPut9.json
     */
    /**
     * Sample code: Put example #9.
     *
     * @param manager Entry point to ViManager.
     */
    public static void putExample9(com.azure.resourcemanager.vi.ViManager manager) {
        manager
            .accounts()
            .define("contosto-videoanalyzer")
            .withRegion((String) null)
            .withExistingResourceGroup("contosto-videoanalyzer-rg")
            .create();
    }

    /*
     * x-ms-original-file: specification/vi/resource-manager/Microsoft.VideoIndexer/preview/2022-04-13-preview/examples/ViAccountPut/ViAccountPut4.json
     */
    /**
     * Sample code: Put example #4.
     *
     * @param manager Entry point to ViManager.
     */
    public static void putExample4(com.azure.resourcemanager.vi.ViManager manager) {
        manager
            .accounts()
            .define("contosto-videoanalyzer")
            .withRegion("NorthEurope")
            .withExistingResourceGroup("contosto-videoanalyzer-rg")
            .withIdentity(
                new ManagedServiceIdentity()
                    .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/xxx/resourceGroups/contoso-videoanalyzer-rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/contoso-videoanalyzer-mi",
                            new UserAssignedIdentity())))
            .withMediaServices(
                new MediaServicesForPutRequest()
                    .withResourceId(
                        "/subscriptions/xxx/resourceGroups/contoso-videoanalyzer-rg/providers/Microsoft.Media/mediaservices/contoso-videoanalyzer-ms")
                    .withUserAssignedIdentity(
                        "/subscriptions/xxx/resourceGroups/contoso-videoanalyzer-rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/contoso-videoanalyzer-mi"))
            .create();
    }

    /*
     * x-ms-original-file: specification/vi/resource-manager/Microsoft.VideoIndexer/preview/2022-04-13-preview/examples/ViAccountPut/ViAccountPut5.json
     */
    /**
     * Sample code: Put example #5.
     *
     * @param manager Entry point to ViManager.
     */
    public static void putExample5(com.azure.resourcemanager.vi.ViManager manager) {
        manager
            .accounts()
            .define("contosto-videoanalyzer")
            .withRegion("NorthEurope")
            .withExistingResourceGroup("contosto-videoanalyzer-rg")
            .withIdentity(
                new ManagedServiceIdentity()
                    .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/xxx/resourceGroups/contoso-videoanalyzer-rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/contoso-videoanalyzer-mi",
                            new UserAssignedIdentity())))
            .withMediaServices(
                new MediaServicesForPutRequest()
                    .withResourceId(
                        "/subscriptions/xxx/resourceGroups/contoso-videoanalyzer-rg/providers/Microsoft.Media/mediaservices/contoso-videoanalyzer-ms")
                    .withUserAssignedIdentity(
                        "/subscriptions/xxx/resourceGroups/contoso-videoanalyzer-rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/contoso-videoanalyzer-mi"))
            .create();
    }

    /*
     * x-ms-original-file: specification/vi/resource-manager/Microsoft.VideoIndexer/preview/2022-04-13-preview/examples/ViAccountPut/ViAccountPut6.json
     */
    /**
     * Sample code: Put example #6.
     *
     * @param manager Entry point to ViManager.
     */
    public static void putExample6(com.azure.resourcemanager.vi.ViManager manager) {
        manager
            .accounts()
            .define("contosto-videoanalyzer")
            .withRegion("NorthEurope")
            .withExistingResourceGroup("contosto-videoanalyzer-rg")
            .withIdentity(
                new ManagedServiceIdentity()
                    .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/xxx/resourceGroups/contoso-videoanalyzer-rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/contoso-videoanalyzer-mi",
                            new UserAssignedIdentity())))
            .withMediaServices(
                new MediaServicesForPutRequest()
                    .withResourceId(
                        "/subscriptions/xxx/resourceGroups/contoso-videoanalyzer-rg/providers/Microsoft.Media/mediaservices/contoso-videoanalyzer-ms")
                    .withUserAssignedIdentity(
                        "/subscriptions/xxx/resourceGroups/contoso-videoanalyzer-rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/contoso-videoanalyzer-mi"))
            .create();
    }

    /*
     * x-ms-original-file: specification/vi/resource-manager/Microsoft.VideoIndexer/preview/2022-04-13-preview/examples/ViAccountPut/ViAccountPut7.json
     */
    /**
     * Sample code: Put example #7.
     *
     * @param manager Entry point to ViManager.
     */
    public static void putExample7(com.azure.resourcemanager.vi.ViManager manager) {
        manager
            .accounts()
            .define("contosto-videoanalyzer")
            .withRegion("NorthEurope")
            .withExistingResourceGroup("contosto-videoanalyzer-rg")
            .withIdentity(
                new ManagedServiceIdentity()
                    .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/xxx/resourceGroups/contoso-videoanalyzer-rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/contoso-videoanalyzer-mi",
                            new UserAssignedIdentity())))
            .withMediaServices(
                new MediaServicesForPutRequest()
                    .withResourceId(
                        "/subscriptions/xxx/resourceGroups/contoso-videoanalyzer-rg/providers/Microsoft.Media/mediaservices/contoso-videoanalyzer-ms")
                    .withUserAssignedIdentity(
                        "/subscriptions/xxx/resourceGroups/contoso-videoanalyzer-rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/contoso-videoanalyzer-mi"))
            .create();
    }

    /*
     * x-ms-original-file: specification/vi/resource-manager/Microsoft.VideoIndexer/preview/2022-04-13-preview/examples/ViAccountPut/ViAccountPut1.json
     */
    /**
     * Sample code: Put example #1.
     *
     * @param manager Entry point to ViManager.
     */
    public static void putExample1(com.azure.resourcemanager.vi.ViManager manager) {
        manager
            .accounts()
            .define("contosto-videoanalyzer")
            .withRegion("NorthEurope")
            .withExistingResourceGroup("contosto-videoanalyzer-rg")
            .withIdentity(
                new ManagedServiceIdentity()
                    .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/xxx/resourceGroups/contoso-videoanalyzer-rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/contoso-videoanalyzer-mi",
                            new UserAssignedIdentity())))
            .withMediaServices(
                new MediaServicesForPutRequest()
                    .withResourceId(
                        "/subscriptions/xxx/resourceGroups/contoso-videoanalyzer-rg/providers/Microsoft.Media/mediaservices/contoso-videoanalyzer-ms")
                    .withUserAssignedIdentity(
                        "/subscriptions/xxx/resourceGroups/contoso-videoanalyzer-rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/contoso-videoanalyzer-mi"))
            .create();
    }

    /*
     * x-ms-original-file: specification/vi/resource-manager/Microsoft.VideoIndexer/preview/2022-04-13-preview/examples/ViAccountPut/ViAccountPut2.json
     */
    /**
     * Sample code: Put example #2.
     *
     * @param manager Entry point to ViManager.
     */
    public static void putExample2(com.azure.resourcemanager.vi.ViManager manager) {
        manager
            .accounts()
            .define("contosto-videoanalyzer")
            .withRegion("NorthEurope")
            .withExistingResourceGroup("contosto-videoanalyzer-rg")
            .withIdentity(
                new ManagedServiceIdentity()
                    .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/xxx/resourceGroups/contoso-videoanalyzer-rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/contoso-videoanalyzer-mi",
                            new UserAssignedIdentity())))
            .withMediaServices(
                new MediaServicesForPutRequest()
                    .withResourceId(
                        "/subscriptions/xxx/resourceGroups/contoso-videoanalyzer-rg/providers/Microsoft.Media/mediaservices/contoso-videoanalyzer-ms")
                    .withUserAssignedIdentity(
                        "/subscriptions/xxx/resourceGroups/contoso-videoanalyzer-rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/contoso-videoanalyzer-mi"))
            .create();
    }

    /*
     * x-ms-original-file: specification/vi/resource-manager/Microsoft.VideoIndexer/preview/2022-04-13-preview/examples/ViAccountPut/ViAccountPut3.json
     */
    /**
     * Sample code: Put example #3.
     *
     * @param manager Entry point to ViManager.
     */
    public static void putExample3(com.azure.resourcemanager.vi.ViManager manager) {
        manager
            .accounts()
            .define("contosto-videoanalyzer")
            .withRegion("NorthEurope")
            .withExistingResourceGroup("contosto-videoanalyzer-rg")
            .withIdentity(
                new ManagedServiceIdentity()
                    .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/xxx/resourceGroups/contoso-videoanalyzer-rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/contoso-videoanalyzer-mi",
                            new UserAssignedIdentity())))
            .withMediaServices(
                new MediaServicesForPutRequest()
                    .withResourceId(
                        "/subscriptions/xxx/resourceGroups/contoso-videoanalyzer-rg/providers/Microsoft.Media/mediaservices/contoso-videoanalyzer-ms")
                    .withUserAssignedIdentity(
                        "/subscriptions/xxx/resourceGroups/contoso-videoanalyzer-rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/contoso-videoanalyzer-mi"))
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

### Accounts_Delete

```java
import com.azure.core.util.Context;

/** Samples for Accounts Delete. */
public final class AccountsDeleteSamples {
    /*
     * x-ms-original-file: specification/vi/resource-manager/Microsoft.VideoIndexer/preview/2022-04-13-preview/examples/ViAccountDelete.json
     */
    /**
     * Sample code: Delete account.
     *
     * @param manager Entry point to ViManager.
     */
    public static void deleteAccount(com.azure.resourcemanager.vi.ViManager manager) {
        manager.accounts().deleteWithResponse("contoso-rg", "contosto-videoanalyzer", Context.NONE);
    }
}
```

### Accounts_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Accounts GetByResourceGroup. */
public final class AccountsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/vi/resource-manager/Microsoft.VideoIndexer/preview/2022-04-13-preview/examples/ViAccountGet.json
     */
    /**
     * Sample code: Get account.
     *
     * @param manager Entry point to ViManager.
     */
    public static void getAccount(com.azure.resourcemanager.vi.ViManager manager) {
        manager.accounts().getByResourceGroupWithResponse("contoso-rg", "contosto-videoanalyzer", Context.NONE);
    }
}
```

### Accounts_List

```java
import com.azure.core.util.Context;

/** Samples for Accounts List. */
public final class AccountsListSamples {
    /*
     * x-ms-original-file: specification/vi/resource-manager/Microsoft.VideoIndexer/preview/2022-04-13-preview/examples/ViAccountsList.json
     */
    /**
     * Sample code: List accounts.
     *
     * @param manager Entry point to ViManager.
     */
    public static void listAccounts(com.azure.resourcemanager.vi.ViManager manager) {
        manager.accounts().list(Context.NONE);
    }
}
```

### Accounts_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Accounts ListByResourceGroup. */
public final class AccountsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/vi/resource-manager/Microsoft.VideoIndexer/preview/2022-04-13-preview/examples/ViAccountsListByResourceGroup.json
     */
    /**
     * Sample code: List accounts by resource group.
     *
     * @param manager Entry point to ViManager.
     */
    public static void listAccountsByResourceGroup(com.azure.resourcemanager.vi.ViManager manager) {
        manager.accounts().listByResourceGroup("contoso-videoanalyzer-rg", Context.NONE);
    }
}
```

### Accounts_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.vi.models.Account;
import com.azure.resourcemanager.vi.models.ManagedServiceIdentity;
import com.azure.resourcemanager.vi.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.vi.models.MediaServicesForPatchRequest;
import com.azure.resourcemanager.vi.models.UserAssignedIdentity;
import java.util.HashMap;
import java.util.Map;

/** Samples for Accounts Update. */
public final class AccountsUpdateSamples {
    /*
     * x-ms-original-file: specification/vi/resource-manager/Microsoft.VideoIndexer/preview/2022-04-13-preview/examples/ViAccountPatch/ViAccountPatch3.json
     */
    /**
     * Sample code: Patch example #3.
     *
     * @param manager Entry point to ViManager.
     */
    public static void patchExample3(com.azure.resourcemanager.vi.ViManager manager) {
        Account resource =
            manager
                .accounts()
                .getByResourceGroupWithResponse("contosto-videoanalyzer-rg", "contosto-videoanalyzer", Context.NONE)
                .getValue();
        resource
            .update()
            .withIdentity(
                new ManagedServiceIdentity()
                    .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/xxx/resourceGroups/contoso-videoanalyzer-rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/contoso-videoanalyzer-mi",
                            new UserAssignedIdentity())))
            .withMediaServices(
                new MediaServicesForPatchRequest()
                    .withUserAssignedIdentity(
                        "/subscriptions/xxx/resourceGroups/contoso-videoanalyzer-rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/contoso-videoanalyzer-mi"))
            .apply();
    }

    /*
     * x-ms-original-file: specification/vi/resource-manager/Microsoft.VideoIndexer/preview/2022-04-13-preview/examples/ViAccountPatch/ViAccountPatch4.json
     */
    /**
     * Sample code: Patch example #4.
     *
     * @param manager Entry point to ViManager.
     */
    public static void patchExample4(com.azure.resourcemanager.vi.ViManager manager) {
        Account resource =
            manager
                .accounts()
                .getByResourceGroupWithResponse("contosto-videoanalyzer-rg", "contosto-videoanalyzer", Context.NONE)
                .getValue();
        resource
            .update()
            .withIdentity(
                new ManagedServiceIdentity()
                    .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/xxx/resourceGroups/contoso-videoanalyzer-rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/contoso-videoanalyzer-mi",
                            new UserAssignedIdentity())))
            .withMediaServices(
                new MediaServicesForPatchRequest()
                    .withUserAssignedIdentity(
                        "/subscriptions/xxx/resourceGroups/contoso-videoanalyzer-rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/contoso-videoanalyzer-mi"))
            .apply();
    }

    /*
     * x-ms-original-file: specification/vi/resource-manager/Microsoft.VideoIndexer/preview/2022-04-13-preview/examples/ViAccountPatch/ViAccountPatch1.json
     */
    /**
     * Sample code: Patch example #1.
     *
     * @param manager Entry point to ViManager.
     */
    public static void patchExample1(com.azure.resourcemanager.vi.ViManager manager) {
        Account resource =
            manager
                .accounts()
                .getByResourceGroupWithResponse("contosto-videoanalyzer-rg", "contosto-videoanalyzer", Context.NONE)
                .getValue();
        resource
            .update()
            .withIdentity(
                new ManagedServiceIdentity()
                    .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/xxx/resourceGroups/contoso-videoanalyzer-rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/contoso-videoanalyzer-mi",
                            new UserAssignedIdentity())))
            .withMediaServices(
                new MediaServicesForPatchRequest()
                    .withUserAssignedIdentity(
                        "/subscriptions/xxx/resourceGroups/contoso-videoanalyzer-rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/contoso-videoanalyzer-mi"))
            .apply();
    }

    /*
     * x-ms-original-file: specification/vi/resource-manager/Microsoft.VideoIndexer/preview/2022-04-13-preview/examples/ViAccountPatch/ViAccountPatch2.json
     */
    /**
     * Sample code: Patch example #2.
     *
     * @param manager Entry point to ViManager.
     */
    public static void patchExample2(com.azure.resourcemanager.vi.ViManager manager) {
        Account resource =
            manager
                .accounts()
                .getByResourceGroupWithResponse("contosto-videoanalyzer-rg", "contosto-videoanalyzer", Context.NONE)
                .getValue();
        resource
            .update()
            .withIdentity(
                new ManagedServiceIdentity()
                    .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/xxx/resourceGroups/contoso-videoanalyzer-rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/contoso-videoanalyzer-mi",
                            new UserAssignedIdentity())))
            .withMediaServices(
                new MediaServicesForPatchRequest()
                    .withUserAssignedIdentity(
                        "/subscriptions/xxx/resourceGroups/contoso-videoanalyzer-rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/contoso-videoanalyzer-mi"))
            .apply();
    }

    /*
     * x-ms-original-file: specification/vi/resource-manager/Microsoft.VideoIndexer/preview/2022-04-13-preview/examples/ViAccountPatch/ViAccountPatch7.json
     */
    /**
     * Sample code: Patch example #7.
     *
     * @param manager Entry point to ViManager.
     */
    public static void patchExample7(com.azure.resourcemanager.vi.ViManager manager) {
        Account resource =
            manager
                .accounts()
                .getByResourceGroupWithResponse("contosto-videoanalyzer-rg", "contosto-videoanalyzer", Context.NONE)
                .getValue();
        resource
            .update()
            .withIdentity(
                new ManagedServiceIdentity()
                    .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/xxx/resourceGroups/contoso-videoanalyzer-rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/contoso-videoanalyzer-mi",
                            new UserAssignedIdentity())))
            .apply();
    }

    /*
     * x-ms-original-file: specification/vi/resource-manager/Microsoft.VideoIndexer/preview/2022-04-13-preview/examples/ViAccountPatch/ViAccountPatch8.json
     */
    /**
     * Sample code: Patch example #8.
     *
     * @param manager Entry point to ViManager.
     */
    public static void patchExample8(com.azure.resourcemanager.vi.ViManager manager) {
        Account resource =
            manager
                .accounts()
                .getByResourceGroupWithResponse("contosto-videoanalyzer-rg", "contosto-videoanalyzer", Context.NONE)
                .getValue();
        resource
            .update()
            .withIdentity(
                new ManagedServiceIdentity()
                    .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentities(mapOf()))
            .withMediaServices(
                new MediaServicesForPatchRequest()
                    .withUserAssignedIdentity(
                        "/subscriptions/xxx/resourceGroups/contoso-videoanalyzer-rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/contoso-videoanalyzer-mi"))
            .apply();
    }

    /*
     * x-ms-original-file: specification/vi/resource-manager/Microsoft.VideoIndexer/preview/2022-04-13-preview/examples/ViAccountPatch/ViAccountPatch5.json
     */
    /**
     * Sample code: Patch example #5.
     *
     * @param manager Entry point to ViManager.
     */
    public static void patchExample5(com.azure.resourcemanager.vi.ViManager manager) {
        Account resource =
            manager
                .accounts()
                .getByResourceGroupWithResponse("contosto-videoanalyzer-rg", "contosto-videoanalyzer", Context.NONE)
                .getValue();
        resource
            .update()
            .withIdentity(
                new ManagedServiceIdentity()
                    .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/xxx/resourceGroups/contoso-videoanalyzer-rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/contoso-videoanalyzer-mi",
                            new UserAssignedIdentity())))
            .withMediaServices(
                new MediaServicesForPatchRequest()
                    .withUserAssignedIdentity(
                        "/subscriptions/xxx/resourceGroups/contoso-videoanalyzer-rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/contoso-videoanalyzer-mi"))
            .apply();
    }

    /*
     * x-ms-original-file: specification/vi/resource-manager/Microsoft.VideoIndexer/preview/2022-04-13-preview/examples/ViAccountPatch/ViAccountPatch6.json
     */
    /**
     * Sample code: Patch example #6.
     *
     * @param manager Entry point to ViManager.
     */
    public static void patchExample6(com.azure.resourcemanager.vi.ViManager manager) {
        Account resource =
            manager
                .accounts()
                .getByResourceGroupWithResponse("contosto-videoanalyzer-rg", "contosto-videoanalyzer", Context.NONE)
                .getValue();
        resource.update().apply();
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

### ClassicAccounts_GetDetails

```java
import com.azure.core.util.Context;

/** Samples for ClassicAccounts GetDetails. */
public final class ClassicAccountsGetDetailsSamples {
    /*
     * x-ms-original-file: specification/vi/resource-manager/Microsoft.VideoIndexer/preview/2022-04-13-preview/examples/ViClassicAccounts.json
     */
    /**
     * Sample code: Get account.
     *
     * @param manager Entry point to ViManager.
     */
    public static void getAccount(com.azure.resourcemanager.vi.ViManager manager) {
        manager.classicAccounts().getDetailsWithResponse("NorthEurope", "contosto-videoanalyzer", Context.NONE);
    }
}
```

### Generate_AccessToken

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.vi.models.GenerateAccessTokenParameters;
import com.azure.resourcemanager.vi.models.PermissionType;
import com.azure.resourcemanager.vi.models.Scope;

/** Samples for Generate AccessToken. */
public final class GenerateAccessTokenSamples {
    /*
     * x-ms-original-file: specification/vi/resource-manager/Microsoft.VideoIndexer/preview/2022-04-13-preview/examples/ViGenerateAccessToken/ViGenerateVideoContributerAccessToken1.json
     */
    /**
     * Sample code: Generate accessToken for video contributor #1.
     *
     * @param manager Entry point to ViManager.
     */
    public static void generateAccessTokenForVideoContributor1(com.azure.resourcemanager.vi.ViManager manager) {
        manager
            .generates()
            .accessTokenWithResponse(
                "contosto-videoanalyzer-rg",
                "contosto-videoanalyzer",
                new GenerateAccessTokenParameters()
                    .withPermissionType(PermissionType.CONTRIBUTOR)
                    .withScope(Scope.VIDEO)
                    .withVideoId("07ec9e38d4"),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/vi/resource-manager/Microsoft.VideoIndexer/preview/2022-04-13-preview/examples/ViGenerateAccessToken/ViGenerateProjectReaderAccessToken1.json
     */
    /**
     * Sample code: Generate accessToken for project reader #1.
     *
     * @param manager Entry point to ViManager.
     */
    public static void generateAccessTokenForProjectReader1(com.azure.resourcemanager.vi.ViManager manager) {
        manager
            .generates()
            .accessTokenWithResponse(
                "contosto-videoanalyzer-rg",
                "contosto-videoanalyzer",
                new GenerateAccessTokenParameters()
                    .withPermissionType(PermissionType.READER)
                    .withScope(Scope.PROJECT)
                    .withProjectId("07ec9e38d4"),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/vi/resource-manager/Microsoft.VideoIndexer/preview/2022-04-13-preview/examples/ViGenerateAccessToken/ViGenerateVideoContributerAccessToken2.json
     */
    /**
     * Sample code: Generate accessToken for video contributor #2.
     *
     * @param manager Entry point to ViManager.
     */
    public static void generateAccessTokenForVideoContributor2(com.azure.resourcemanager.vi.ViManager manager) {
        manager
            .generates()
            .accessTokenWithResponse(
                "contosto-videoanalyzer-rg",
                "contosto-videoanalyzer",
                new GenerateAccessTokenParameters()
                    .withPermissionType(PermissionType.CONTRIBUTOR)
                    .withScope(Scope.VIDEO)
                    .withVideoId("07ec9e38d4"),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/vi/resource-manager/Microsoft.VideoIndexer/preview/2022-04-13-preview/examples/ViGenerateAccessToken/ViGenerateAccountContributerAccessToken.json
     */
    /**
     * Sample code: Generate accessToken for account contributor.
     *
     * @param manager Entry point to ViManager.
     */
    public static void generateAccessTokenForAccountContributor(com.azure.resourcemanager.vi.ViManager manager) {
        manager
            .generates()
            .accessTokenWithResponse(
                "contosto-videoanalyzer-rg",
                "contosto-videoanalyzer",
                new GenerateAccessTokenParameters()
                    .withPermissionType(PermissionType.CONTRIBUTOR)
                    .withScope(Scope.ACCOUNT),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/vi/resource-manager/Microsoft.VideoIndexer/preview/2022-04-13-preview/examples/ViGenerateAccessToken/ViGenerateProjectContributerAccessToken2.json
     */
    /**
     * Sample code: Generate accessToken for project contributor #2.
     *
     * @param manager Entry point to ViManager.
     */
    public static void generateAccessTokenForProjectContributor2(com.azure.resourcemanager.vi.ViManager manager) {
        manager
            .generates()
            .accessTokenWithResponse(
                "contosto-videoanalyzer-rg",
                "contosto-videoanalyzer",
                new GenerateAccessTokenParameters()
                    .withPermissionType(PermissionType.CONTRIBUTOR)
                    .withScope(Scope.PROJECT)
                    .withProjectId("07ec9e38d4"),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/vi/resource-manager/Microsoft.VideoIndexer/preview/2022-04-13-preview/examples/ViGenerateAccessToken/ViGenerateAccountReaderAccessToken.json
     */
    /**
     * Sample code: Generate accessToken for account reader.
     *
     * @param manager Entry point to ViManager.
     */
    public static void generateAccessTokenForAccountReader(com.azure.resourcemanager.vi.ViManager manager) {
        manager
            .generates()
            .accessTokenWithResponse(
                "contosto-videoanalyzer-rg",
                "contosto-videoanalyzer",
                new GenerateAccessTokenParameters().withPermissionType(PermissionType.READER).withScope(Scope.ACCOUNT),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/vi/resource-manager/Microsoft.VideoIndexer/preview/2022-04-13-preview/examples/ViGenerateAccessToken/ViGenerateVideoReaderAccessToken1.json
     */
    /**
     * Sample code: Generate accessToken for video reader #1.
     *
     * @param manager Entry point to ViManager.
     */
    public static void generateAccessTokenForVideoReader1(com.azure.resourcemanager.vi.ViManager manager) {
        manager
            .generates()
            .accessTokenWithResponse(
                "contosto-videoanalyzer-rg",
                "contosto-videoanalyzer",
                new GenerateAccessTokenParameters()
                    .withPermissionType(PermissionType.READER)
                    .withScope(Scope.VIDEO)
                    .withVideoId("07ec9e38d4"),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/vi/resource-manager/Microsoft.VideoIndexer/preview/2022-04-13-preview/examples/ViGenerateAccessToken/ViGenerateProjectContributerAccessToken1.json
     */
    /**
     * Sample code: Generate accessToken for project contributor #1.
     *
     * @param manager Entry point to ViManager.
     */
    public static void generateAccessTokenForProjectContributor1(com.azure.resourcemanager.vi.ViManager manager) {
        manager
            .generates()
            .accessTokenWithResponse(
                "contosto-videoanalyzer-rg",
                "contosto-videoanalyzer",
                new GenerateAccessTokenParameters()
                    .withPermissionType(PermissionType.CONTRIBUTOR)
                    .withScope(Scope.PROJECT)
                    .withProjectId("07ec9e38d4"),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/vi/resource-manager/Microsoft.VideoIndexer/preview/2022-04-13-preview/examples/ViGenerateAccessToken/ViGenerateVideoReaderAccessToken2.json
     */
    /**
     * Sample code: Generate accessToken for video reader #2.
     *
     * @param manager Entry point to ViManager.
     */
    public static void generateAccessTokenForVideoReader2(com.azure.resourcemanager.vi.ViManager manager) {
        manager
            .generates()
            .accessTokenWithResponse(
                "contosto-videoanalyzer-rg",
                "contosto-videoanalyzer",
                new GenerateAccessTokenParameters()
                    .withPermissionType(PermissionType.READER)
                    .withScope(Scope.VIDEO)
                    .withVideoId("07ec9e38d4"),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/vi/resource-manager/Microsoft.VideoIndexer/preview/2022-04-13-preview/examples/ViGenerateAccessToken/ViGenerateProjectReaderAccessToken2.json
     */
    /**
     * Sample code: Generate accessToken for project reader #2.
     *
     * @param manager Entry point to ViManager.
     */
    public static void generateAccessTokenForProjectReader2(com.azure.resourcemanager.vi.ViManager manager) {
        manager
            .generates()
            .accessTokenWithResponse(
                "contosto-videoanalyzer-rg",
                "contosto-videoanalyzer",
                new GenerateAccessTokenParameters()
                    .withPermissionType(PermissionType.READER)
                    .withScope(Scope.PROJECT)
                    .withProjectId("07ec9e38d4"),
                Context.NONE);
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/vi/resource-manager/Microsoft.VideoIndexer/preview/2022-04-13-preview/examples/ViOperationsList.json
     */
    /**
     * Sample code: List operations.
     *
     * @param manager Entry point to ViManager.
     */
    public static void listOperations(com.azure.resourcemanager.vi.ViManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### UserClassicAccounts_List

```java
import com.azure.core.util.Context;

/** Samples for UserClassicAccounts List. */
public final class UserClassicAccountsListSamples {
    /*
     * x-ms-original-file: specification/vi/resource-manager/Microsoft.VideoIndexer/preview/2022-04-13-preview/examples/ViListUserClassicAccounts.json
     */
    /**
     * Sample code: List accounts.
     *
     * @param manager Entry point to ViManager.
     */
    public static void listAccounts(com.azure.resourcemanager.vi.ViManager manager) {
        manager.userClassicAccounts().list("NorthEurope", Context.NONE);
    }
}
```

