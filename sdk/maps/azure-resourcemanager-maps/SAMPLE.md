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

- [ListOperations](#maps_listoperations)

## OperationResult

- [Get](#operationresult_get)

## OperationStatus

- [Get](#operationstatus_get)

## PrivateEndpointConnections

- [Create](#privateendpointconnections_create)
- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [ListByAccount](#privateendpointconnections_listbyaccount)

## PrivateLinkResources

- [Get](#privatelinkresources_get)
- [ListByAccount](#privatelinkresources_listbyaccount)
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
import com.azure.resourcemanager.maps.models.LocationsItem;
import com.azure.resourcemanager.maps.models.ManagedServiceIdentity;
import com.azure.resourcemanager.maps.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.maps.models.Name;
import com.azure.resourcemanager.maps.models.Sku;
import com.azure.resourcemanager.maps.models.UserAssignedIdentity;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Accounts CreateOrUpdate.
 */
public final class AccountsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-10-01-preview/CreateAccountGen2.json
     */
    /**
     * Sample code: Create Gen2 Account.
     * 
     * @param manager Entry point to AzureMapsManager.
     */
    public static void createGen2Account(com.azure.resourcemanager.maps.AzureMapsManager manager) {
        manager.accounts()
            .define("myMapsAccount")
            .withRegion("eastus")
            .withExistingResourceGroup("myResourceGroup")
            .withSku(new Sku().withName(Name.G2))
            .withTags(mapOf("test", "true"))
            .withProperties(new MapsAccountProperties().withDisableLocalAuth(true)
                .withCors(new CorsRules().withCorsRules(Arrays.asList(new CorsRule()
                    .withAllowedOrigins(Arrays.asList("http://www.contoso.com", "http://www.fabrikam.com")))))
                .withLocations(Arrays.asList(new LocationsItem().withLocationName("northeurope"))))
            .withKind(Kind.GEN2)
            .create();
    }

    /*
     * x-ms-original-file: 2025-10-01-preview/CreateAccountEncryption.json
     */
    /**
     * Sample code: Create Account with Encryption.
     * 
     * @param manager Entry point to AzureMapsManager.
     */
    public static void createAccountWithEncryption(com.azure.resourcemanager.maps.AzureMapsManager manager) {
        manager.accounts()
            .define("myMapsAccount")
            .withRegion("eastus")
            .withExistingResourceGroup("myResourceGroup")
            .withSku(new Sku().withName(Name.G2))
            .withProperties(new MapsAccountProperties()
                .withEncryption(new Encryption().withCustomerManagedKeyEncryption(new CustomerManagedKeyEncryption()
                    .withKeyEncryptionKeyIdentity(new CustomerManagedKeyEncryptionKeyIdentity()
                        .withIdentityType(IdentityType.USER_ASSIGNED_IDENTITY)
                        .withUserAssignedIdentityResourceId(
                            "/subscriptions/21a9967a-e8a9-4656-a70b-96ff1c4d05a0/resourceGroups/myResourceGroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/identityName"))
                    .withKeyEncryptionKeyUrl("fakeTokenPlaceholder"))))
            .withKind(Kind.GEN2)
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/21a9967a-e8a9-4656-a70b-96ff1c4d05a0/resourceGroups/myResourceGroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/identityName",
                    new UserAssignedIdentity())))
            .create();
    }

    /*
     * x-ms-original-file: 2025-10-01-preview/CreateAccountManagedIdentity.json
     */
    /**
     * Sample code: Create Account with Managed Identities.
     * 
     * @param manager Entry point to AzureMapsManager.
     */
    public static void createAccountWithManagedIdentities(com.azure.resourcemanager.maps.AzureMapsManager manager) {
        manager.accounts()
            .define("myMapsAccount")
            .withRegion("eastus")
            .withExistingResourceGroup("myResourceGroup")
            .withSku(new Sku().withName(Name.G2))
            .withTags(mapOf("test", "true"))
            .withProperties(new MapsAccountProperties().withDisableLocalAuth(false)
                .withLinkedResources(Arrays.asList(new LinkedResource().withUniqueName("myBatchStorageAccount")
                    .withId(
                        "/subscriptions/21a9967a-e8a9-4656-a70b-96ff1c4d05a0/resourceGroups/myResourceGroup/providers/Microsoft.Storage/accounts/mystorageacc"),
                    new LinkedResource().withUniqueName("myBlobDataSource")
                        .withId(
                            "/subscriptions/21a9967a-e8a9-4656-a70b-96ff1c4d05a0/resourceGroups/myResourceGroup/providers/Microsoft.Storage/accounts/mystorageacc"))))
            .withKind(Kind.GEN2)
            .withIdentity(new ManagedServiceIdentity()
                .withType(ManagedServiceIdentityType.fromString("SystemAssigned, UserAssigned"))
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/21a9967a-e8a9-4656-a70b-96ff1c4d05a0/resourceGroups/myResourceGroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/identityName",
                    new UserAssignedIdentity())))
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
/**
 * Samples for Accounts Delete.
 */
public final class AccountsDeleteSamples {
    /*
     * x-ms-original-file: 2025-10-01-preview/DeleteAccount.json
     */
    /**
     * Sample code: DeleteAccount.
     * 
     * @param manager Entry point to AzureMapsManager.
     */
    public static void deleteAccount(com.azure.resourcemanager.maps.AzureMapsManager manager) {
        manager.accounts()
            .deleteByResourceGroupWithResponse("myResourceGroup", "myMapsAccount", com.azure.core.util.Context.NONE);
    }
}
```

### Accounts_GetByResourceGroup

```java
/**
 * Samples for Accounts GetByResourceGroup.
 */
public final class AccountsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-10-01-preview/GetAccount.json
     */
    /**
     * Sample code: GetAccount.
     * 
     * @param manager Entry point to AzureMapsManager.
     */
    public static void getAccount(com.azure.resourcemanager.maps.AzureMapsManager manager) {
        manager.accounts()
            .getByResourceGroupWithResponse("myResourceGroup", "myMapsAccount", com.azure.core.util.Context.NONE);
    }
}
```

### Accounts_List

```java
/**
 * Samples for Accounts List.
 */
public final class AccountsListSamples {
    /*
     * x-ms-original-file: 2025-10-01-preview/ListAccountsBySubscription.json
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
/**
 * Samples for Accounts ListByResourceGroup.
 */
public final class AccountsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-10-01-preview/ListAccountsByResourceGroup.json
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
/**
 * Samples for Accounts ListKeys.
 */
public final class AccountsListKeysSamples {
    /*
     * x-ms-original-file: 2025-10-01-preview/ListKeys.json
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

/**
 * Samples for Accounts ListSas.
 */
public final class AccountsListSasSamples {
    /*
     * x-ms-original-file: 2025-10-01-preview/AccountListSAS.json
     */
    /**
     * Sample code: List Account Sas.
     * 
     * @param manager Entry point to AzureMapsManager.
     */
    public static void listAccountSas(com.azure.resourcemanager.maps.AzureMapsManager manager) {
        manager.accounts()
            .listSasWithResponse("myResourceGroup", "myMapsAccount",
                new AccountSasParameters().withSigningKey(SigningKey.PRIMARY_KEY)
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

/**
 * Samples for Accounts RegenerateKeys.
 */
public final class AccountsRegenerateKeysSamples {
    /*
     * x-ms-original-file: 2025-10-01-preview/RegenerateKey.json
     */
    /**
     * Sample code: Regenerate Key.
     * 
     * @param manager Entry point to AzureMapsManager.
     */
    public static void regenerateKey(com.azure.resourcemanager.maps.AzureMapsManager manager) {
        manager.accounts()
            .regenerateKeysWithResponse("myResourceGroup", "myMapsAccount",
                new MapsKeySpecification().withKeyType(KeyType.PRIMARY), com.azure.core.util.Context.NONE);
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

/**
 * Samples for Accounts Update.
 */
public final class AccountsUpdateSamples {
    /*
     * x-ms-original-file: 2025-10-01-preview/UpdateAccountGen2.json
     */
    /**
     * Sample code: Update to Gen2 Account.
     * 
     * @param manager Entry point to AzureMapsManager.
     */
    public static void updateToGen2Account(com.azure.resourcemanager.maps.AzureMapsManager manager) {
        MapsAccount resource = manager.accounts()
            .getByResourceGroupWithResponse("myResourceGroup", "myMapsAccount", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withKind(Kind.GEN2).withSku(new Sku().withName(Name.G2)).apply();
    }

    /*
     * x-ms-original-file: 2025-10-01-preview/UpdateAccount.json
     */
    /**
     * Sample code: Update Account Tags.
     * 
     * @param manager Entry point to AzureMapsManager.
     */
    public static void updateAccountTags(com.azure.resourcemanager.maps.AzureMapsManager manager) {
        MapsAccount resource = manager.accounts()
            .getByResourceGroupWithResponse("myResourceGroup", "myMapsAccount", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("specialTag", "true")).apply();
    }

    /*
     * x-ms-original-file: 2025-10-01-preview/UpdateAccountManagedIdentity.json
     */
    /**
     * Sample code: Update Account Managed Identities.
     * 
     * @param manager Entry point to AzureMapsManager.
     */
    public static void updateAccountManagedIdentities(com.azure.resourcemanager.maps.AzureMapsManager manager) {
        MapsAccount resource = manager.accounts()
            .getByResourceGroupWithResponse("myResourceGroup", "myMapsAccount", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withKind(Kind.GEN2)
            .withSku(new Sku().withName(Name.G2))
            .withIdentity(new ManagedServiceIdentity()
                .withType(ManagedServiceIdentityType.fromString("SystemAssigned, UserAssigned"))
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/21a9967a-e8a9-4656-a70b-96ff1c4d05a0/resourceGroups/myResourceGroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/identityName",
                    new UserAssignedIdentity())))
            .withLinkedResources(Arrays.asList(new LinkedResource().withUniqueName("myBatchStorageAccount")
                .withId(
                    "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Storage/accounts/{storageName}")))
            .apply();
    }

    /*
     * x-ms-original-file: 2025-10-01-preview/UpdateAccountEncryption.json
     */
    /**
     * Sample code: Update Account Encryption.
     * 
     * @param manager Entry point to AzureMapsManager.
     */
    public static void updateAccountEncryption(com.azure.resourcemanager.maps.AzureMapsManager manager) {
        MapsAccount resource = manager.accounts()
            .getByResourceGroupWithResponse("myResourceGroup", "myMapsAccount", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED)
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/21a9967a-e8a9-4656-a70b-96ff1c4d05a0/resourceGroups/myResourceGroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/identityName",
                    null)))
            .withEncryption(new Encryption().withCustomerManagedKeyEncryption(new CustomerManagedKeyEncryption()
                .withKeyEncryptionKeyIdentity(new CustomerManagedKeyEncryptionKeyIdentity()
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

/**
 * Samples for Creators CreateOrUpdate.
 */
public final class CreatorsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-10-01-preview/CreateMapsCreator.json
     */
    /**
     * Sample code: Create Creator Resource.
     * 
     * @param manager Entry point to AzureMapsManager.
     */
    public static void createCreatorResource(com.azure.resourcemanager.maps.AzureMapsManager manager) {
        manager.creators()
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
/**
 * Samples for Creators Delete.
 */
public final class CreatorsDeleteSamples {
    /*
     * x-ms-original-file: 2025-10-01-preview/DeleteMapsCreator.json
     */
    /**
     * Sample code: Delete Creator Resource.
     * 
     * @param manager Entry point to AzureMapsManager.
     */
    public static void deleteCreatorResource(com.azure.resourcemanager.maps.AzureMapsManager manager) {
        manager.creators()
            .deleteWithResponse("myResourceGroup", "myMapsAccount", "myCreator", com.azure.core.util.Context.NONE);
    }
}
```

### Creators_Get

```java
/**
 * Samples for Creators Get.
 */
public final class CreatorsGetSamples {
    /*
     * x-ms-original-file: 2025-10-01-preview/GetMapsCreator.json
     */
    /**
     * Sample code: Get Creator Resource.
     * 
     * @param manager Entry point to AzureMapsManager.
     */
    public static void getCreatorResource(com.azure.resourcemanager.maps.AzureMapsManager manager) {
        manager.creators()
            .getWithResponse("myResourceGroup", "myMapsAccount", "myCreator", com.azure.core.util.Context.NONE);
    }
}
```

### Creators_ListByAccount

```java
/**
 * Samples for Creators ListByAccount.
 */
public final class CreatorsListByAccountSamples {
    /*
     * x-ms-original-file: 2025-10-01-preview/ListMapsCreatorsByAccount.json
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

/**
 * Samples for Creators Update.
 */
public final class CreatorsUpdateSamples {
    /*
     * x-ms-original-file: 2025-10-01-preview/UpdateMapsCreator.json
     */
    /**
     * Sample code: Update Creator Resource.
     * 
     * @param manager Entry point to AzureMapsManager.
     */
    public static void updateCreatorResource(com.azure.resourcemanager.maps.AzureMapsManager manager) {
        Creator resource = manager.creators()
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

### Maps_ListOperations

```java
/**
 * Samples for Maps ListOperations.
 */
public final class MapsListOperationsSamples {
    /*
     * x-ms-original-file: 2025-10-01-preview/GetOperations.json
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

### OperationResult_Get

```java
/**
 * Samples for OperationResult Get.
 */
public final class OperationResultGetSamples {
    /*
     * x-ms-original-file: 2025-10-01-preview/GetOperationResult.json
     */
    /**
     * Sample code: OperationResult_Get.
     * 
     * @param manager Entry point to AzureMapsManager.
     */
    public static void operationResultGet(com.azure.resourcemanager.maps.AzureMapsManager manager) {
        manager.operationResults()
            .get("eastus", "01234567-89ab-4def-0123-456789abcdef", com.azure.core.util.Context.NONE);
    }
}
```

### OperationStatus_Get

```java
/**
 * Samples for OperationStatus Get.
 */
public final class OperationStatusGetSamples {
    /*
     * x-ms-original-file: 2025-10-01-preview/GetOperationStatus.json
     */
    /**
     * Sample code: OperationStatus_Get.
     * 
     * @param manager Entry point to AzureMapsManager.
     */
    public static void operationStatusGet(com.azure.resourcemanager.maps.AzureMapsManager manager) {
        manager.operationStatus()
            .getWithResponse("eastus", "01234567-89ab-4def-0123-456789abcdef", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_Create

```java
import com.azure.resourcemanager.maps.models.PrivateEndpointServiceConnectionStatus;
import com.azure.resourcemanager.maps.models.PrivateLinkServiceConnectionState;

/**
 * Samples for PrivateEndpointConnections Create.
 */
public final class PrivateEndpointConnectionsCreateSamples {
    /*
     * x-ms-original-file: 2025-10-01-preview/PrivateEndpointConnections_Update.json
     */
    /**
     * Sample code: PrivateEndpointConnections_Create.
     * 
     * @param manager Entry point to AzureMapsManager.
     */
    public static void privateEndpointConnectionsCreate(com.azure.resourcemanager.maps.AzureMapsManager manager) {
        manager.privateEndpointConnections()
            .define("privateEndpointConnectionName")
            .withExistingAccount("myResourceGroup", "myMapsAccount")
            .withPrivateLinkServiceConnectionState(
                new PrivateLinkServiceConnectionState().withStatus(PrivateEndpointServiceConnectionStatus.APPROVED)
                    .withDescription("Auto-Approved"))
            .create();
    }
}
```

### PrivateEndpointConnections_Delete

```java
/**
 * Samples for PrivateEndpointConnections Delete.
 */
public final class PrivateEndpointConnectionsDeleteSamples {
    /*
     * x-ms-original-file: 2025-10-01-preview/PrivateEndpointConnections_Delete.json
     */
    /**
     * Sample code: PrivateEndpointConnections_Delete.
     * 
     * @param manager Entry point to AzureMapsManager.
     */
    public static void privateEndpointConnectionsDelete(com.azure.resourcemanager.maps.AzureMapsManager manager) {
        manager.privateEndpointConnections()
            .delete("myResourceGroup", "myMapsAccount", "privateEndpointConnectionName",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_Get

```java
/**
 * Samples for PrivateEndpointConnections Get.
 */
public final class PrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file: 2025-10-01-preview/PrivateEndpointConnections_Get.json
     */
    /**
     * Sample code: PrivateEndpointConnections_Get.
     * 
     * @param manager Entry point to AzureMapsManager.
     */
    public static void privateEndpointConnectionsGet(com.azure.resourcemanager.maps.AzureMapsManager manager) {
        manager.privateEndpointConnections()
            .getWithResponse("myResourceGroup", "myMapsAccount", "privateEndpointConnectionName",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_ListByAccount

```java
/**
 * Samples for PrivateEndpointConnections ListByAccount.
 */
public final class PrivateEndpointConnectionsListByAccountSamples {
    /*
     * x-ms-original-file: 2025-10-01-preview/PrivateEndpointConnections_ListByAccount.json
     */
    /**
     * Sample code: PrivateEndpointConnections_ListByAccount.
     * 
     * @param manager Entry point to AzureMapsManager.
     */
    public static void
        privateEndpointConnectionsListByAccount(com.azure.resourcemanager.maps.AzureMapsManager manager) {
        manager.privateEndpointConnections()
            .listByAccount("myResourceGroup", "myMapsAccount", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkResources_Get

```java
/**
 * Samples for PrivateLinkResources Get.
 */
public final class PrivateLinkResourcesGetSamples {
    /*
     * x-ms-original-file: 2025-10-01-preview/PrivateLinkResources_Get.json
     */
    /**
     * Sample code: Get a private link resource.
     * 
     * @param manager Entry point to AzureMapsManager.
     */
    public static void getAPrivateLinkResource(com.azure.resourcemanager.maps.AzureMapsManager manager) {
        manager.privateLinkResources()
            .getWithResponse("myResourceGroup", "myMapsAccount", "mapsAccount", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkResources_ListByAccount

```java
/**
 * Samples for PrivateLinkResources ListByAccount.
 */
public final class PrivateLinkResourcesListByAccountSamples {
    /*
     * x-ms-original-file: 2025-10-01-preview/PrivateLinkResources_List.json
     */
    /**
     * Sample code: PrivateLinkResources_List.
     * 
     * @param manager Entry point to AzureMapsManager.
     */
    public static void privateLinkResourcesList(com.azure.resourcemanager.maps.AzureMapsManager manager) {
        manager.privateLinkResources()
            .listByAccount("myResourceGroup", "myMapsAccount", com.azure.core.util.Context.NONE);
    }
}
```

