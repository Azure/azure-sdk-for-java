# Code snippets and samples


## Accounts

- [CreateOrUpdate](#accounts_createorupdate)
- [Delete](#accounts_delete)
- [GetByResourceGroup](#accounts_getbyresourcegroup)
- [List](#accounts_list)
- [ListByResourceGroup](#accounts_listbyresourcegroup)
- [ListKeys](#accounts_listkeys)
- [ListSas](#accounts_listsas)
- [RegenerateKeys](#accounts_regeneratekeys)
- [Update](#accounts_update)

## Creators

- [CreateOrUpdate](#creators_createorupdate)
- [Delete](#creators_delete)
- [Get](#creators_get)
- [ListByAccount](#creators_listbyaccount)
- [Update](#creators_update)

## Maps

- [List](#maps_list)
- [ListOperations](#maps_listoperations)
### Accounts_CreateOrUpdate

```java
import com.azure.resourcemanager.maps.fluent.models.MapsAccountProperties;
import com.azure.resourcemanager.maps.models.CorsRule;
import com.azure.resourcemanager.maps.models.CorsRules;
import com.azure.resourcemanager.maps.models.CustomerManagedKeyEncryption;
import com.azure.resourcemanager.maps.models.CustomerManagedKeyEncryptionKeyIdentity;
import com.azure.resourcemanager.maps.models.Encryption;
import com.azure.resourcemanager.maps.models.IdentityType;
import com.azure.resourcemanager.maps.models.Kind;
import com.azure.resourcemanager.maps.models.LinkedResource;
import com.azure.resourcemanager.maps.models.ManagedServiceIdentity;
import com.azure.resourcemanager.maps.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.maps.models.Name;
import com.azure.resourcemanager.maps.models.Sku;
import com.azure.resourcemanager.maps.models.UserAssignedIdentity;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Accounts CreateOrUpdate. */
public final class AccountsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/maps/resource-manager/Microsoft.Maps/stable/2023-06-01/examples/CreateAccountGen2.json
     */
    /**
     * Sample code: Create Gen2 Account.
     *
     * @param manager Entry point to AzureMapsManager.
     */
    public static void createGen2Account(com.azure.resourcemanager.maps.AzureMapsManager manager) {
        manager
            .accounts()
            .define("myMapsAccount")
            .withRegion("eastus")
            .withExistingResourceGroup("myResourceGroup")
            .withSku(new Sku().withName(Name.G2))
            .withTags(mapOf("test", "true"))
            .withKind(Kind.GEN2)
            .withProperties(
                new MapsAccountProperties()
                    .withDisableLocalAuth(true)
                    .withCors(
                        new CorsRules()
                            .withCorsRules(
                                Arrays
                                    .asList(
                                        new CorsRule()
                                            .withAllowedOrigins(
                                                Arrays.asList("http://www.contoso.com", "http://www.fabrikam.com"))))))
            .create();
    }

    /*
     * x-ms-original-file: specification/maps/resource-manager/Microsoft.Maps/stable/2023-06-01/examples/CreateAccountEncryption.json
     */
    /**
     * Sample code: Create Account with Encryption.
     *
     * @param manager Entry point to AzureMapsManager.
     */
    public static void createAccountWithEncryption(com.azure.resourcemanager.maps.AzureMapsManager manager) {
        manager
            .accounts()
            .define("myMapsAccount")
            .withRegion("eastus")
            .withExistingResourceGroup("myResourceGroup")
            .withSku(new Sku().withName(Name.G2))
            .withKind(Kind.GEN2)
            .withIdentity(
                new ManagedServiceIdentity()
                    .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/21a9967a-e8a9-4656-a70b-96ff1c4d05a0/resourceGroups/myResourceGroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/identityName",
                            new UserAssignedIdentity())))
            .withProperties(
                new MapsAccountProperties()
                    .withEncryption(
                        new Encryption()
                            .withCustomerManagedKeyEncryption(
                                new CustomerManagedKeyEncryption()
                                    .withKeyEncryptionKeyIdentity(
                                        new CustomerManagedKeyEncryptionKeyIdentity()
                                            .withIdentityType(IdentityType.USER_ASSIGNED_IDENTITY)
                                            .withUserAssignedIdentityResourceId(
                                                "/subscriptions/21a9967a-e8a9-4656-a70b-96ff1c4d05a0/resourceGroups/myResourceGroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/identityName"))
                                    .withKeyEncryptionKeyUrl("fakeTokenPlaceholder"))))
            .create();
    }

    /*
     * x-ms-original-file: specification/maps/resource-manager/Microsoft.Maps/stable/2023-06-01/examples/CreateAccount.json
     */
    /**
     * Sample code: Create Gen1 Account.
     *
     * @param manager Entry point to AzureMapsManager.
     */
    public static void createGen1Account(com.azure.resourcemanager.maps.AzureMapsManager manager) {
        manager
            .accounts()
            .define("myMapsAccount")
            .withRegion("eastus")
            .withExistingResourceGroup("myResourceGroup")
            .withSku(new Sku().withName(Name.S0))
            .withTags(mapOf("test", "true"))
            .withKind(Kind.GEN1)
            .withProperties(
                new MapsAccountProperties()
                    .withDisableLocalAuth(false)
                    .withCors(
                        new CorsRules()
                            .withCorsRules(
                                Arrays
                                    .asList(
                                        new CorsRule()
                                            .withAllowedOrigins(
                                                Arrays.asList("http://www.contoso.com", "http://www.fabrikam.com"))))))
            .create();
    }

    /*
     * x-ms-original-file: specification/maps/resource-manager/Microsoft.Maps/stable/2023-06-01/examples/CreateAccountManagedIdentity.json
     */
    /**
     * Sample code: Create Account with Managed Identities.
     *
     * @param manager Entry point to AzureMapsManager.
     */
    public static void createAccountWithManagedIdentities(com.azure.resourcemanager.maps.AzureMapsManager manager) {
        manager
            .accounts()
            .define("myMapsAccount")
            .withRegion("eastus")
            .withExistingResourceGroup("myResourceGroup")
            .withSku(new Sku().withName(Name.G2))
            .withTags(mapOf("test", "true"))
            .withKind(Kind.GEN2)
            .withIdentity(
                new ManagedServiceIdentity()
                    .withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/21a9967a-e8a9-4656-a70b-96ff1c4d05a0/resourceGroups/myResourceGroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/identityName",
                            new UserAssignedIdentity())))
            .withProperties(
                new MapsAccountProperties()
                    .withDisableLocalAuth(false)
                    .withLinkedResources(
                        Arrays
                            .asList(
                                new LinkedResource()
                                    .withUniqueName("myBatchStorageAccount")
                                    .withId(
                                        "/subscriptions/21a9967a-e8a9-4656-a70b-96ff1c4d05a0/resourceGroups/myResourceGroup/providers/Microsoft.Storage/accounts/mystorageacc"),
                                new LinkedResource()
                                    .withUniqueName("myBlobDataSource")
                                    .withId(
                                        "/subscriptions/21a9967a-e8a9-4656-a70b-96ff1c4d05a0/resourceGroups/myResourceGroup/providers/Microsoft.Storage/accounts/mystorageacc"))))
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

### Accounts_Delete

```java
/** Samples for Accounts Delete. */
public final class AccountsDeleteSamples {
    /*
     * x-ms-original-file: specification/maps/resource-manager/Microsoft.Maps/stable/2023-06-01/examples/DeleteAccount.json
     */
    /**
     * Sample code: DeleteAccount.
     *
     * @param manager Entry point to AzureMapsManager.
     */
    public static void deleteAccount(com.azure.resourcemanager.maps.AzureMapsManager manager) {
        manager
            .accounts()
            .deleteByResourceGroupWithResponse("myResourceGroup", "myMapsAccount", com.azure.core.util.Context.NONE);
    }
}
```

### Accounts_GetByResourceGroup

```java
/** Samples for Accounts GetByResourceGroup. */
public final class AccountsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/maps/resource-manager/Microsoft.Maps/stable/2023-06-01/examples/GetAccount.json
     */
    /**
     * Sample code: GetAccount.
     *
     * @param manager Entry point to AzureMapsManager.
     */
    public static void getAccount(com.azure.resourcemanager.maps.AzureMapsManager manager) {
        manager
            .accounts()
            .getByResourceGroupWithResponse("myResourceGroup", "myMapsAccount", com.azure.core.util.Context.NONE);
    }
}
```

### Accounts_List

```java
/** Samples for Accounts List. */
public final class AccountsListSamples {
    /*
     * x-ms-original-file: specification/maps/resource-manager/Microsoft.Maps/stable/2023-06-01/examples/ListAccountsBySubscription.json
     */
    /**
     * Sample code: List Accounts By Subscription.
     *
     * @param manager Entry point to AzureMapsManager.
     */
    public static void listAccountsBySubscription(com.azure.resourcemanager.maps.AzureMapsManager manager) {
        manager.accounts().list(com.azure.core.util.Context.NONE);
    }
}
```

### Accounts_ListByResourceGroup

```java
/** Samples for Accounts ListByResourceGroup. */
public final class AccountsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/maps/resource-manager/Microsoft.Maps/stable/2023-06-01/examples/ListAccountsByResourceGroup.json
     */
    /**
     * Sample code: List Accounts By Resource Group.
     *
     * @param manager Entry point to AzureMapsManager.
     */
    public static void listAccountsByResourceGroup(com.azure.resourcemanager.maps.AzureMapsManager manager) {
        manager.accounts().listByResourceGroup("myResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### Accounts_ListKeys

```java
/** Samples for Accounts ListKeys. */
public final class AccountsListKeysSamples {
    /*
     * x-ms-original-file: specification/maps/resource-manager/Microsoft.Maps/stable/2023-06-01/examples/ListKeys.json
     */
    /**
     * Sample code: List Keys.
     *
     * @param manager Entry point to AzureMapsManager.
     */
    public static void listKeys(com.azure.resourcemanager.maps.AzureMapsManager manager) {
        manager.accounts().listKeysWithResponse("myResourceGroup", "myMapsAccount", com.azure.core.util.Context.NONE);
    }
}
```

### Accounts_ListSas

```java
import com.azure.resourcemanager.maps.models.AccountSasParameters;
import com.azure.resourcemanager.maps.models.SigningKey;
import java.util.Arrays;

/** Samples for Accounts ListSas. */
public final class AccountsListSasSamples {
    /*
     * x-ms-original-file: specification/maps/resource-manager/Microsoft.Maps/stable/2023-06-01/examples/AccountListSAS.json
     */
    /**
     * Sample code: List Account Sas.
     *
     * @param manager Entry point to AzureMapsManager.
     */
    public static void listAccountSas(com.azure.resourcemanager.maps.AzureMapsManager manager) {
        manager
            .accounts()
            .listSasWithResponse(
                "myResourceGroup",
                "myMapsAccount",
                new AccountSasParameters()
                    .withSigningKey(SigningKey.PRIMARY_KEY)
                    .withPrincipalId("e917f87b-324d-4728-98ed-e31d311a7d65")
                    .withRegions(Arrays.asList("eastus"))
                    .withMaxRatePerSecond(500)
                    .withStart("2017-05-24T10:42:03.1567373Z")
                    .withExpiry("2017-05-24T11:42:03.1567373Z"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Accounts_RegenerateKeys

```java
import com.azure.resourcemanager.maps.models.KeyType;
import com.azure.resourcemanager.maps.models.MapsKeySpecification;

/** Samples for Accounts RegenerateKeys. */
public final class AccountsRegenerateKeysSamples {
    /*
     * x-ms-original-file: specification/maps/resource-manager/Microsoft.Maps/stable/2023-06-01/examples/RegenerateKey.json
     */
    /**
     * Sample code: Regenerate Key.
     *
     * @param manager Entry point to AzureMapsManager.
     */
    public static void regenerateKey(com.azure.resourcemanager.maps.AzureMapsManager manager) {
        manager
            .accounts()
            .regenerateKeysWithResponse(
                "myResourceGroup",
                "myMapsAccount",
                new MapsKeySpecification().withKeyType(KeyType.PRIMARY),
                com.azure.core.util.Context.NONE);
    }
}
```

### Accounts_Update

```java
import com.azure.resourcemanager.maps.models.CustomerManagedKeyEncryption;
import com.azure.resourcemanager.maps.models.CustomerManagedKeyEncryptionKeyIdentity;
import com.azure.resourcemanager.maps.models.Encryption;
import com.azure.resourcemanager.maps.models.IdentityType;
import com.azure.resourcemanager.maps.models.Kind;
import com.azure.resourcemanager.maps.models.LinkedResource;
import com.azure.resourcemanager.maps.models.ManagedServiceIdentity;
import com.azure.resourcemanager.maps.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.maps.models.MapsAccount;
import com.azure.resourcemanager.maps.models.Name;
import com.azure.resourcemanager.maps.models.Sku;
import com.azure.resourcemanager.maps.models.UserAssignedIdentity;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Accounts Update. */
public final class AccountsUpdateSamples {
    /*
     * x-ms-original-file: specification/maps/resource-manager/Microsoft.Maps/stable/2023-06-01/examples/UpdateAccountGen2.json
     */
    /**
     * Sample code: Update to Gen2 Account.
     *
     * @param manager Entry point to AzureMapsManager.
     */
    public static void updateToGen2Account(com.azure.resourcemanager.maps.AzureMapsManager manager) {
        MapsAccount resource =
            manager
                .accounts()
                .getByResourceGroupWithResponse("myResourceGroup", "myMapsAccount", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withKind(Kind.GEN2).withSku(new Sku().withName(Name.G2)).apply();
    }

    /*
     * x-ms-original-file: specification/maps/resource-manager/Microsoft.Maps/stable/2023-06-01/examples/UpdateAccount.json
     */
    /**
     * Sample code: Update Account Tags.
     *
     * @param manager Entry point to AzureMapsManager.
     */
    public static void updateAccountTags(com.azure.resourcemanager.maps.AzureMapsManager manager) {
        MapsAccount resource =
            manager
                .accounts()
                .getByResourceGroupWithResponse("myResourceGroup", "myMapsAccount", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("specialTag", "true")).apply();
    }

    /*
     * x-ms-original-file: specification/maps/resource-manager/Microsoft.Maps/stable/2023-06-01/examples/UpdateAccountManagedIdentity.json
     */
    /**
     * Sample code: Update Account Managed Identities.
     *
     * @param manager Entry point to AzureMapsManager.
     */
    public static void updateAccountManagedIdentities(com.azure.resourcemanager.maps.AzureMapsManager manager) {
        MapsAccount resource =
            manager
                .accounts()
                .getByResourceGroupWithResponse("myResourceGroup", "myMapsAccount", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withKind(Kind.GEN2)
            .withSku(new Sku().withName(Name.G2))
            .withIdentity(
                new ManagedServiceIdentity()
                    .withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/21a9967a-e8a9-4656-a70b-96ff1c4d05a0/resourceGroups/myResourceGroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/identityName",
                            new UserAssignedIdentity())))
            .withLinkedResources(
                Arrays
                    .asList(
                        new LinkedResource()
                            .withUniqueName("myBatchStorageAccount")
                            .withId(
                                "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Storage/accounts/{storageName}")))
            .apply();
    }

    /*
     * x-ms-original-file: specification/maps/resource-manager/Microsoft.Maps/stable/2023-06-01/examples/UpdateAccountGen1.json
     */
    /**
     * Sample code: Update to Gen1 Account.
     *
     * @param manager Entry point to AzureMapsManager.
     */
    public static void updateToGen1Account(com.azure.resourcemanager.maps.AzureMapsManager manager) {
        MapsAccount resource =
            manager
                .accounts()
                .getByResourceGroupWithResponse("myResourceGroup", "myMapsAccount", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withKind(Kind.GEN1).withSku(new Sku().withName(Name.S1)).apply();
    }

    /*
     * x-ms-original-file: specification/maps/resource-manager/Microsoft.Maps/stable/2023-06-01/examples/UpdateAccountEncryption.json
     */
    /**
     * Sample code: Update Account Encryption.
     *
     * @param manager Entry point to AzureMapsManager.
     */
    public static void updateAccountEncryption(com.azure.resourcemanager.maps.AzureMapsManager manager) {
        MapsAccount resource =
            manager
                .accounts()
                .getByResourceGroupWithResponse("myResourceGroup", "myMapsAccount", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withIdentity(
                new ManagedServiceIdentity()
                    .withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/21a9967a-e8a9-4656-a70b-96ff1c4d05a0/resourceGroups/myResourceGroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/identityName",
                            null)))
            .withEncryption(
                new Encryption()
                    .withCustomerManagedKeyEncryption(
                        new CustomerManagedKeyEncryption()
                            .withKeyEncryptionKeyIdentity(
                                new CustomerManagedKeyEncryptionKeyIdentity()
                                    .withIdentityType(IdentityType.SYSTEM_ASSIGNED_IDENTITY))
                            .withKeyEncryptionKeyUrl("fakeTokenPlaceholder")))
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

### Creators_CreateOrUpdate

```java
import com.azure.resourcemanager.maps.fluent.models.CreatorProperties;
import java.util.HashMap;
import java.util.Map;

/** Samples for Creators CreateOrUpdate. */
public final class CreatorsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/maps/resource-manager/Microsoft.Maps/stable/2023-06-01/examples/CreateMapsCreator.json
     */
    /**
     * Sample code: Create Creator Resource.
     *
     * @param manager Entry point to AzureMapsManager.
     */
    public static void createCreatorResource(com.azure.resourcemanager.maps.AzureMapsManager manager) {
        manager
            .creators()
            .define("myCreator")
            .withRegion("eastus2")
            .withExistingAccount("myResourceGroup", "myMapsAccount")
            .withProperties(new CreatorProperties().withStorageUnits(5))
            .withTags(mapOf("test", "true"))
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

### Creators_Delete

```java
/** Samples for Creators Delete. */
public final class CreatorsDeleteSamples {
    /*
     * x-ms-original-file: specification/maps/resource-manager/Microsoft.Maps/stable/2023-06-01/examples/DeleteMapsCreator.json
     */
    /**
     * Sample code: Delete Creator Resource.
     *
     * @param manager Entry point to AzureMapsManager.
     */
    public static void deleteCreatorResource(com.azure.resourcemanager.maps.AzureMapsManager manager) {
        manager
            .creators()
            .deleteWithResponse("myResourceGroup", "myMapsAccount", "myCreator", com.azure.core.util.Context.NONE);
    }
}
```

### Creators_Get

```java
/** Samples for Creators Get. */
public final class CreatorsGetSamples {
    /*
     * x-ms-original-file: specification/maps/resource-manager/Microsoft.Maps/stable/2023-06-01/examples/GetMapsCreator.json
     */
    /**
     * Sample code: Get Creator Resource.
     *
     * @param manager Entry point to AzureMapsManager.
     */
    public static void getCreatorResource(com.azure.resourcemanager.maps.AzureMapsManager manager) {
        manager
            .creators()
            .getWithResponse("myResourceGroup", "myMapsAccount", "myCreator", com.azure.core.util.Context.NONE);
    }
}
```

### Creators_ListByAccount

```java
/** Samples for Creators ListByAccount. */
public final class CreatorsListByAccountSamples {
    /*
     * x-ms-original-file: specification/maps/resource-manager/Microsoft.Maps/stable/2023-06-01/examples/ListMapsCreatorsByAccount.json
     */
    /**
     * Sample code: List Creator Resources By Account.
     *
     * @param manager Entry point to AzureMapsManager.
     */
    public static void listCreatorResourcesByAccount(com.azure.resourcemanager.maps.AzureMapsManager manager) {
        manager.creators().listByAccount("myResourceGroup", "myMapsAccount", com.azure.core.util.Context.NONE);
    }
}
```

### Creators_Update

```java
import com.azure.resourcemanager.maps.models.Creator;
import java.util.HashMap;
import java.util.Map;

/** Samples for Creators Update. */
public final class CreatorsUpdateSamples {
    /*
     * x-ms-original-file: specification/maps/resource-manager/Microsoft.Maps/stable/2023-06-01/examples/UpdateMapsCreator.json
     */
    /**
     * Sample code: Update Creator Resource.
     *
     * @param manager Entry point to AzureMapsManager.
     */
    public static void updateCreatorResource(com.azure.resourcemanager.maps.AzureMapsManager manager) {
        Creator resource =
            manager
                .creators()
                .getWithResponse("myResourceGroup", "myMapsAccount", "myCreator", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("specialTag", "true")).withStorageUnits(10).apply();
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

### Maps_List

```java
/** Samples for Maps List. */
public final class MapsListSamples {
    /*
     * x-ms-original-file: specification/maps/resource-manager/Microsoft.Maps/stable/2023-06-01/examples/GetOperationsSubscription.json
     */
    /**
     * Sample code: Get Operations by Subscription.
     *
     * @param manager Entry point to AzureMapsManager.
     */
    public static void getOperationsBySubscription(com.azure.resourcemanager.maps.AzureMapsManager manager) {
        manager.maps().list(com.azure.core.util.Context.NONE);
    }
}
```

### Maps_ListOperations

```java
/** Samples for Maps ListOperations. */
public final class MapsListOperationsSamples {
    /*
     * x-ms-original-file: specification/maps/resource-manager/Microsoft.Maps/stable/2023-06-01/examples/GetOperations.json
     */
    /**
     * Sample code: Get Operations.
     *
     * @param manager Entry point to AzureMapsManager.
     */
    public static void getOperations(com.azure.resourcemanager.maps.AzureMapsManager manager) {
        manager.maps().listOperations(com.azure.core.util.Context.NONE);
    }
}
```

