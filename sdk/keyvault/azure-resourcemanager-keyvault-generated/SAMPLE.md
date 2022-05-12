# Code snippets and samples


## Keys

- [CreateIfNotExist](#keys_createifnotexist)
- [Get](#keys_get)
- [GetVersion](#keys_getversion)
- [List](#keys_list)
- [ListVersions](#keys_listversions)

## ManagedHsms

- [CreateOrUpdate](#managedhsms_createorupdate)
- [Delete](#managedhsms_delete)
- [GetByResourceGroup](#managedhsms_getbyresourcegroup)
- [GetDeleted](#managedhsms_getdeleted)
- [List](#managedhsms_list)
- [ListByResourceGroup](#managedhsms_listbyresourcegroup)
- [ListDeleted](#managedhsms_listdeleted)
- [PurgeDeleted](#managedhsms_purgedeleted)
- [Update](#managedhsms_update)

## MhsmPrivateEndpointConnections

- [Delete](#mhsmprivateendpointconnections_delete)
- [Get](#mhsmprivateendpointconnections_get)
- [ListByResource](#mhsmprivateendpointconnections_listbyresource)
- [Put](#mhsmprivateendpointconnections_put)

## MhsmPrivateLinkResources

- [ListByMhsmResource](#mhsmprivatelinkresources_listbymhsmresource)

## Operations

- [List](#operations_list)

## PrivateEndpointConnections

- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [ListByResource](#privateendpointconnections_listbyresource)
- [Put](#privateendpointconnections_put)

## PrivateLinkResources

- [ListByVault](#privatelinkresources_listbyvault)

## Secrets

- [CreateOrUpdate](#secrets_createorupdate)
- [Get](#secrets_get)
- [List](#secrets_list)
- [Update](#secrets_update)

## Vaults

- [CheckNameAvailability](#vaults_checknameavailability)
- [CreateOrUpdate](#vaults_createorupdate)
- [Delete](#vaults_delete)
- [GetByResourceGroup](#vaults_getbyresourcegroup)
- [GetDeleted](#vaults_getdeleted)
- [List](#vaults_list)
- [ListByResourceGroup](#vaults_listbyresourcegroup)
- [ListBySubscription](#vaults_listbysubscription)
- [ListDeleted](#vaults_listdeleted)
- [PurgeDeleted](#vaults_purgedeleted)
- [Update](#vaults_update)
- [UpdateAccessPolicy](#vaults_updateaccesspolicy)
### Keys_CreateIfNotExist

```java
import com.azure.resourcemanager.keyvault.generated.fluent.models.KeyProperties;
import com.azure.resourcemanager.keyvault.generated.models.JsonWebKeyType;

/** Samples for Keys CreateIfNotExist. */
public final class KeysCreateIfNotExistSamples {
    /*
     * x-ms-original-file: specification/keyvault/resource-manager/Microsoft.KeyVault/preview/2021-11-01-preview/examples/createKey.json
     */
    /**
     * Sample code: Create a key.
     *
     * @param manager Entry point to KeyVaultManager.
     */
    public static void createAKey(com.azure.resourcemanager.keyvault.generated.KeyVaultManager manager) {
        manager
            .keys()
            .define("sample-key-name")
            .withExistingVault("sample-group", "sample-vault-name")
            .withProperties(new KeyProperties().withKty(JsonWebKeyType.RSA))
            .create();
    }
}
```

### Keys_Get

```java
import com.azure.core.util.Context;

/** Samples for Keys Get. */
public final class KeysGetSamples {
    /*
     * x-ms-original-file: specification/keyvault/resource-manager/Microsoft.KeyVault/preview/2021-11-01-preview/examples/getKey.json
     */
    /**
     * Sample code: Get a key.
     *
     * @param manager Entry point to KeyVaultManager.
     */
    public static void getAKey(com.azure.resourcemanager.keyvault.generated.KeyVaultManager manager) {
        manager.keys().getWithResponse("sample-group", "sample-vault-name", "sample-key-name", Context.NONE);
    }
}
```

### Keys_GetVersion

```java
import com.azure.core.util.Context;

/** Samples for Keys GetVersion. */
public final class KeysGetVersionSamples {
    /*
     * x-ms-original-file: specification/keyvault/resource-manager/Microsoft.KeyVault/preview/2021-11-01-preview/examples/getKeyVersion.json
     */
    /**
     * Sample code: Get a key version.
     *
     * @param manager Entry point to KeyVaultManager.
     */
    public static void getAKeyVersion(com.azure.resourcemanager.keyvault.generated.KeyVaultManager manager) {
        manager
            .keys()
            .getVersionWithResponse(
                "sample-group",
                "sample-vault-name",
                "sample-key-name",
                "fd618d9519b74f9aae94ade66b876acc",
                Context.NONE);
    }
}
```

### Keys_List

```java
import com.azure.core.util.Context;

/** Samples for Keys List. */
public final class KeysListSamples {
    /*
     * x-ms-original-file: specification/keyvault/resource-manager/Microsoft.KeyVault/preview/2021-11-01-preview/examples/listKeys.json
     */
    /**
     * Sample code: List keys in the vault.
     *
     * @param manager Entry point to KeyVaultManager.
     */
    public static void listKeysInTheVault(com.azure.resourcemanager.keyvault.generated.KeyVaultManager manager) {
        manager.keys().list("sample-group", "sample-vault-name", Context.NONE);
    }
}
```

### Keys_ListVersions

```java
import com.azure.core.util.Context;

/** Samples for Keys ListVersions. */
public final class KeysListVersionsSamples {
    /*
     * x-ms-original-file: specification/keyvault/resource-manager/Microsoft.KeyVault/preview/2021-11-01-preview/examples/listKeyVersions.json
     */
    /**
     * Sample code: List key versions in the vault.
     *
     * @param manager Entry point to KeyVaultManager.
     */
    public static void listKeyVersionsInTheVault(com.azure.resourcemanager.keyvault.generated.KeyVaultManager manager) {
        manager.keys().listVersions("sample-group", "sample-vault-name", "sample-key-name", Context.NONE);
    }
}
```

### ManagedHsms_CreateOrUpdate

```java
import com.azure.resourcemanager.keyvault.generated.models.ManagedHsmProperties;
import com.azure.resourcemanager.keyvault.generated.models.ManagedHsmSku;
import com.azure.resourcemanager.keyvault.generated.models.ManagedHsmSkuFamily;
import com.azure.resourcemanager.keyvault.generated.models.ManagedHsmSkuName;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Samples for ManagedHsms CreateOrUpdate. */
public final class ManagedHsmsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/keyvault/resource-manager/Microsoft.KeyVault/preview/2021-11-01-preview/examples/ManagedHsm_CreateOrUpdate.json
     */
    /**
     * Sample code: Create a new managed HSM Pool or update an existing managed HSM Pool.
     *
     * @param manager Entry point to KeyVaultManager.
     */
    public static void createANewManagedHSMPoolOrUpdateAnExistingManagedHSMPool(
        com.azure.resourcemanager.keyvault.generated.KeyVaultManager manager) {
        manager
            .managedHsms()
            .define("hsm1")
            .withRegion("westus")
            .withExistingResourceGroup("hsm-group")
            .withTags(mapOf("Dept", "hsm", "Environment", "dogfood"))
            .withSku(new ManagedHsmSku().withFamily(ManagedHsmSkuFamily.B).withName(ManagedHsmSkuName.STANDARD_B1))
            .withProperties(
                new ManagedHsmProperties()
                    .withTenantId(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                    .withInitialAdminObjectIds(Arrays.asList("00000000-0000-0000-0000-000000000000"))
                    .withEnableSoftDelete(true)
                    .withSoftDeleteRetentionInDays(90)
                    .withEnablePurgeProtection(true))
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

### ManagedHsms_Delete

```java
import com.azure.core.util.Context;

/** Samples for ManagedHsms Delete. */
public final class ManagedHsmsDeleteSamples {
    /*
     * x-ms-original-file: specification/keyvault/resource-manager/Microsoft.KeyVault/preview/2021-11-01-preview/examples/ManagedHsm_Delete.json
     */
    /**
     * Sample code: Delete a managed HSM Pool.
     *
     * @param manager Entry point to KeyVaultManager.
     */
    public static void deleteAManagedHSMPool(com.azure.resourcemanager.keyvault.generated.KeyVaultManager manager) {
        manager.managedHsms().delete("hsm-group", "hsm1", Context.NONE);
    }
}
```

### ManagedHsms_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for ManagedHsms GetByResourceGroup. */
public final class ManagedHsmsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/keyvault/resource-manager/Microsoft.KeyVault/preview/2021-11-01-preview/examples/ManagedHsm_Get.json
     */
    /**
     * Sample code: Retrieve a managed HSM Pool.
     *
     * @param manager Entry point to KeyVaultManager.
     */
    public static void retrieveAManagedHSMPool(com.azure.resourcemanager.keyvault.generated.KeyVaultManager manager) {
        manager.managedHsms().getByResourceGroupWithResponse("hsm-group", "hsm1", Context.NONE);
    }
}
```

### ManagedHsms_GetDeleted

```java
import com.azure.core.util.Context;

/** Samples for ManagedHsms GetDeleted. */
public final class ManagedHsmsGetDeletedSamples {
    /*
     * x-ms-original-file: specification/keyvault/resource-manager/Microsoft.KeyVault/preview/2021-11-01-preview/examples/DeletedManagedHsm_Get.json
     */
    /**
     * Sample code: Retrieve a deleted managed HSM.
     *
     * @param manager Entry point to KeyVaultManager.
     */
    public static void retrieveADeletedManagedHSM(
        com.azure.resourcemanager.keyvault.generated.KeyVaultManager manager) {
        manager.managedHsms().getDeletedWithResponse("hsm1", "westus", Context.NONE);
    }
}
```

### ManagedHsms_List

```java
import com.azure.core.util.Context;

/** Samples for ManagedHsms List. */
public final class ManagedHsmsListSamples {
    /*
     * x-ms-original-file: specification/keyvault/resource-manager/Microsoft.KeyVault/preview/2021-11-01-preview/examples/ManagedHsm_ListBySubscription.json
     */
    /**
     * Sample code: List managed HSM Pools in a subscription.
     *
     * @param manager Entry point to KeyVaultManager.
     */
    public static void listManagedHSMPoolsInASubscription(
        com.azure.resourcemanager.keyvault.generated.KeyVaultManager manager) {
        manager.managedHsms().list(null, Context.NONE);
    }
}
```

### ManagedHsms_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for ManagedHsms ListByResourceGroup. */
public final class ManagedHsmsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/keyvault/resource-manager/Microsoft.KeyVault/preview/2021-11-01-preview/examples/ManagedHsm_ListByResourceGroup.json
     */
    /**
     * Sample code: List managed HSM Pools in a resource group.
     *
     * @param manager Entry point to KeyVaultManager.
     */
    public static void listManagedHSMPoolsInAResourceGroup(
        com.azure.resourcemanager.keyvault.generated.KeyVaultManager manager) {
        manager.managedHsms().listByResourceGroup("hsm-group", null, Context.NONE);
    }
}
```

### ManagedHsms_ListDeleted

```java
import com.azure.core.util.Context;

/** Samples for ManagedHsms ListDeleted. */
public final class ManagedHsmsListDeletedSamples {
    /*
     * x-ms-original-file: specification/keyvault/resource-manager/Microsoft.KeyVault/preview/2021-11-01-preview/examples/DeletedManagedHsm_List.json
     */
    /**
     * Sample code: List deleted managed HSMs in the specified subscription.
     *
     * @param manager Entry point to KeyVaultManager.
     */
    public static void listDeletedManagedHSMsInTheSpecifiedSubscription(
        com.azure.resourcemanager.keyvault.generated.KeyVaultManager manager) {
        manager.managedHsms().listDeleted(Context.NONE);
    }
}
```

### ManagedHsms_PurgeDeleted

```java
import com.azure.core.util.Context;

/** Samples for ManagedHsms PurgeDeleted. */
public final class ManagedHsmsPurgeDeletedSamples {
    /*
     * x-ms-original-file: specification/keyvault/resource-manager/Microsoft.KeyVault/preview/2021-11-01-preview/examples/DeletedManagedHsm_Purge.json
     */
    /**
     * Sample code: Purge a managed HSM Pool.
     *
     * @param manager Entry point to KeyVaultManager.
     */
    public static void purgeAManagedHSMPool(com.azure.resourcemanager.keyvault.generated.KeyVaultManager manager) {
        manager.managedHsms().purgeDeleted("hsm1", "westus", Context.NONE);
    }
}
```

### ManagedHsms_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.keyvault.generated.models.ManagedHsm;
import java.util.HashMap;
import java.util.Map;

/** Samples for ManagedHsms Update. */
public final class ManagedHsmsUpdateSamples {
    /*
     * x-ms-original-file: specification/keyvault/resource-manager/Microsoft.KeyVault/preview/2021-11-01-preview/examples/ManagedHsm_Update.json
     */
    /**
     * Sample code: Update an existing managed HSM Pool.
     *
     * @param manager Entry point to KeyVaultManager.
     */
    public static void updateAnExistingManagedHSMPool(
        com.azure.resourcemanager.keyvault.generated.KeyVaultManager manager) {
        ManagedHsm resource =
            manager.managedHsms().getByResourceGroupWithResponse("hsm-group", "hsm1", Context.NONE).getValue();
        resource.update().withTags(mapOf("Dept", "hsm", "Environment", "dogfood", "Slice", "A")).apply();
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

### MhsmPrivateEndpointConnections_Delete

```java
import com.azure.core.util.Context;

/** Samples for MhsmPrivateEndpointConnections Delete. */
public final class MhsmPrivateEndpointConnectionsDeleteSamples {
    /*
     * x-ms-original-file: specification/keyvault/resource-manager/Microsoft.KeyVault/preview/2021-11-01-preview/examples/ManagedHsm_deletePrivateEndpointConnection.json
     */
    /**
     * Sample code: ManagedHsmDeletePrivateEndpointConnection.
     *
     * @param manager Entry point to KeyVaultManager.
     */
    public static void managedHsmDeletePrivateEndpointConnection(
        com.azure.resourcemanager.keyvault.generated.KeyVaultManager manager) {
        manager.mhsmPrivateEndpointConnections().delete("sample-group", "sample-mhsm", "sample-pec", Context.NONE);
    }
}
```

### MhsmPrivateEndpointConnections_Get

```java
import com.azure.core.util.Context;

/** Samples for MhsmPrivateEndpointConnections Get. */
public final class MhsmPrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file: specification/keyvault/resource-manager/Microsoft.KeyVault/preview/2021-11-01-preview/examples/ManagedHsm_getPrivateEndpointConnection.json
     */
    /**
     * Sample code: ManagedHsmGetPrivateEndpointConnection.
     *
     * @param manager Entry point to KeyVaultManager.
     */
    public static void managedHsmGetPrivateEndpointConnection(
        com.azure.resourcemanager.keyvault.generated.KeyVaultManager manager) {
        manager
            .mhsmPrivateEndpointConnections()
            .getWithResponse("sample-group", "sample-mhsm", "sample-pec", Context.NONE);
    }
}
```

### MhsmPrivateEndpointConnections_ListByResource

```java
import com.azure.core.util.Context;

/** Samples for MhsmPrivateEndpointConnections ListByResource. */
public final class MhsmPrivateEndpointConnectionsListByResourceSamples {
    /*
     * x-ms-original-file: specification/keyvault/resource-manager/Microsoft.KeyVault/preview/2021-11-01-preview/examples/ManagedHsm_ListPrivateEndpointConnectionsByResource.json
     */
    /**
     * Sample code: List managed HSM Pools in a subscription.
     *
     * @param manager Entry point to KeyVaultManager.
     */
    public static void listManagedHSMPoolsInASubscription(
        com.azure.resourcemanager.keyvault.generated.KeyVaultManager manager) {
        manager.mhsmPrivateEndpointConnections().listByResource("sample-group", "sample-mhsm", Context.NONE);
    }
}
```

### MhsmPrivateEndpointConnections_Put

```java
import com.azure.resourcemanager.keyvault.generated.models.MhsmPrivateLinkServiceConnectionState;
import com.azure.resourcemanager.keyvault.generated.models.PrivateEndpointServiceConnectionStatus;

/** Samples for MhsmPrivateEndpointConnections Put. */
public final class MhsmPrivateEndpointConnectionsPutSamples {
    /*
     * x-ms-original-file: specification/keyvault/resource-manager/Microsoft.KeyVault/preview/2021-11-01-preview/examples/ManagedHsm_putPrivateEndpointConnection.json
     */
    /**
     * Sample code: ManagedHsmPutPrivateEndpointConnection.
     *
     * @param manager Entry point to KeyVaultManager.
     */
    public static void managedHsmPutPrivateEndpointConnection(
        com.azure.resourcemanager.keyvault.generated.KeyVaultManager manager) {
        manager
            .mhsmPrivateEndpointConnections()
            .define("sample-pec")
            .withRegion((String) null)
            .withExistingManagedHSM("sample-group", "sample-mhsm")
            .withPrivateLinkServiceConnectionState(
                new MhsmPrivateLinkServiceConnectionState()
                    .withStatus(PrivateEndpointServiceConnectionStatus.APPROVED)
                    .withDescription("My name is Joe and I'm approving this."))
            .create();
    }
}
```

### MhsmPrivateLinkResources_ListByMhsmResource

```java
import com.azure.core.util.Context;

/** Samples for MhsmPrivateLinkResources ListByMhsmResource. */
public final class MhsmPrivateLinkResourcesListByMhsmResourceSamples {
    /*
     * x-ms-original-file: specification/keyvault/resource-manager/Microsoft.KeyVault/preview/2021-11-01-preview/examples/ManagedHsm_listPrivateLinkResources.json
     */
    /**
     * Sample code: KeyVaultListPrivateLinkResources.
     *
     * @param manager Entry point to KeyVaultManager.
     */
    public static void keyVaultListPrivateLinkResources(
        com.azure.resourcemanager.keyvault.generated.KeyVaultManager manager) {
        manager.mhsmPrivateLinkResources().listByMhsmResourceWithResponse("sample-group", "sample-mhsm", Context.NONE);
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/keyvault/resource-manager/Microsoft.KeyVault/preview/2021-11-01-preview/examples/listOperations.json
     */
    /**
     * Sample code: Lists available Rest API operations.
     *
     * @param manager Entry point to KeyVaultManager.
     */
    public static void listsAvailableRestAPIOperations(
        com.azure.resourcemanager.keyvault.generated.KeyVaultManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### PrivateEndpointConnections_Delete

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections Delete. */
public final class PrivateEndpointConnectionsDeleteSamples {
    /*
     * x-ms-original-file: specification/keyvault/resource-manager/Microsoft.KeyVault/preview/2021-11-01-preview/examples/deletePrivateEndpointConnection.json
     */
    /**
     * Sample code: KeyVaultDeletePrivateEndpointConnection.
     *
     * @param manager Entry point to KeyVaultManager.
     */
    public static void keyVaultDeletePrivateEndpointConnection(
        com.azure.resourcemanager.keyvault.generated.KeyVaultManager manager) {
        manager.privateEndpointConnections().delete("sample-group", "sample-vault", "sample-pec", Context.NONE);
    }
}
```

### PrivateEndpointConnections_Get

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections Get. */
public final class PrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file: specification/keyvault/resource-manager/Microsoft.KeyVault/preview/2021-11-01-preview/examples/getPrivateEndpointConnection.json
     */
    /**
     * Sample code: KeyVaultGetPrivateEndpointConnection.
     *
     * @param manager Entry point to KeyVaultManager.
     */
    public static void keyVaultGetPrivateEndpointConnection(
        com.azure.resourcemanager.keyvault.generated.KeyVaultManager manager) {
        manager
            .privateEndpointConnections()
            .getWithResponse("sample-group", "sample-vault", "sample-pec", Context.NONE);
    }
}
```

### PrivateEndpointConnections_ListByResource

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections ListByResource. */
public final class PrivateEndpointConnectionsListByResourceSamples {
    /*
     * x-ms-original-file: specification/keyvault/resource-manager/Microsoft.KeyVault/preview/2021-11-01-preview/examples/listPrivateEndpointConnection.json
     */
    /**
     * Sample code: KeyVaultListPrivateEndpointConnection.
     *
     * @param manager Entry point to KeyVaultManager.
     */
    public static void keyVaultListPrivateEndpointConnection(
        com.azure.resourcemanager.keyvault.generated.KeyVaultManager manager) {
        manager.privateEndpointConnections().listByResource("sample-group", "sample-vault", Context.NONE);
    }
}
```

### PrivateEndpointConnections_Put

```java
import com.azure.resourcemanager.keyvault.generated.models.PrivateEndpointServiceConnectionStatus;
import com.azure.resourcemanager.keyvault.generated.models.PrivateLinkServiceConnectionState;

/** Samples for PrivateEndpointConnections Put. */
public final class PrivateEndpointConnectionsPutSamples {
    /*
     * x-ms-original-file: specification/keyvault/resource-manager/Microsoft.KeyVault/preview/2021-11-01-preview/examples/putPrivateEndpointConnection.json
     */
    /**
     * Sample code: KeyVaultPutPrivateEndpointConnection.
     *
     * @param manager Entry point to KeyVaultManager.
     */
    public static void keyVaultPutPrivateEndpointConnection(
        com.azure.resourcemanager.keyvault.generated.KeyVaultManager manager) {
        manager
            .privateEndpointConnections()
            .define("sample-pec")
            .withRegion((String) null)
            .withExistingVault("sample-group", "sample-vault")
            .withEtag("")
            .withPrivateLinkServiceConnectionState(
                new PrivateLinkServiceConnectionState()
                    .withStatus(PrivateEndpointServiceConnectionStatus.APPROVED)
                    .withDescription("My name is Joe and I'm approving this."))
            .create();
    }
}
```

### PrivateLinkResources_ListByVault

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkResources ListByVault. */
public final class PrivateLinkResourcesListByVaultSamples {
    /*
     * x-ms-original-file: specification/keyvault/resource-manager/Microsoft.KeyVault/preview/2021-11-01-preview/examples/listPrivateLinkResources.json
     */
    /**
     * Sample code: KeyVaultListPrivateLinkResources.
     *
     * @param manager Entry point to KeyVaultManager.
     */
    public static void keyVaultListPrivateLinkResources(
        com.azure.resourcemanager.keyvault.generated.KeyVaultManager manager) {
        manager.privateLinkResources().listByVaultWithResponse("sample-group", "sample-vault", Context.NONE);
    }
}
```

### Secrets_CreateOrUpdate

```java
import com.azure.resourcemanager.keyvault.generated.models.SecretProperties;

/** Samples for Secrets CreateOrUpdate. */
public final class SecretsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/keyvault/resource-manager/Microsoft.KeyVault/preview/2021-11-01-preview/examples/createSecret.json
     */
    /**
     * Sample code: Create a secret.
     *
     * @param manager Entry point to KeyVaultManager.
     */
    public static void createASecret(com.azure.resourcemanager.keyvault.generated.KeyVaultManager manager) {
        manager
            .secrets()
            .define("secret-name")
            .withExistingVault("sample-group", "sample-vault")
            .withProperties(new SecretProperties().withValue("secret-value"))
            .create();
    }
}
```

### Secrets_Get

```java
import com.azure.core.util.Context;

/** Samples for Secrets Get. */
public final class SecretsGetSamples {
    /*
     * x-ms-original-file: specification/keyvault/resource-manager/Microsoft.KeyVault/preview/2021-11-01-preview/examples/getSecret.json
     */
    /**
     * Sample code: Get a secret.
     *
     * @param manager Entry point to KeyVaultManager.
     */
    public static void getASecret(com.azure.resourcemanager.keyvault.generated.KeyVaultManager manager) {
        manager.secrets().getWithResponse("sample-group", "sample-vault", "secret-name", Context.NONE);
    }
}
```

### Secrets_List

```java
import com.azure.core.util.Context;

/** Samples for Secrets List. */
public final class SecretsListSamples {
    /*
     * x-ms-original-file: specification/keyvault/resource-manager/Microsoft.KeyVault/preview/2021-11-01-preview/examples/listSecrets.json
     */
    /**
     * Sample code: List secrets in the vault.
     *
     * @param manager Entry point to KeyVaultManager.
     */
    public static void listSecretsInTheVault(com.azure.resourcemanager.keyvault.generated.KeyVaultManager manager) {
        manager.secrets().list("sample-group", "sample-vault", null, Context.NONE);
    }
}
```

### Secrets_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.keyvault.generated.models.Secret;
import com.azure.resourcemanager.keyvault.generated.models.SecretPatchProperties;

/** Samples for Secrets Update. */
public final class SecretsUpdateSamples {
    /*
     * x-ms-original-file: specification/keyvault/resource-manager/Microsoft.KeyVault/preview/2021-11-01-preview/examples/updateSecret.json
     */
    /**
     * Sample code: Update a secret.
     *
     * @param manager Entry point to KeyVaultManager.
     */
    public static void updateASecret(com.azure.resourcemanager.keyvault.generated.KeyVaultManager manager) {
        Secret resource =
            manager.secrets().getWithResponse("sample-group", "sample-vault", "secret-name", Context.NONE).getValue();
        resource.update().withProperties(new SecretPatchProperties().withValue("secret-value2")).apply();
    }
}
```

### Vaults_CheckNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.keyvault.generated.models.VaultCheckNameAvailabilityParameters;

/** Samples for Vaults CheckNameAvailability. */
public final class VaultsCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/keyvault/resource-manager/Microsoft.KeyVault/preview/2021-11-01-preview/examples/checkVaultNameAvailability.json
     */
    /**
     * Sample code: Validate a vault name.
     *
     * @param manager Entry point to KeyVaultManager.
     */
    public static void validateAVaultName(com.azure.resourcemanager.keyvault.generated.KeyVaultManager manager) {
        manager
            .vaults()
            .checkNameAvailabilityWithResponse(
                new VaultCheckNameAvailabilityParameters().withName("sample-vault"), Context.NONE);
    }
}
```

### Vaults_CreateOrUpdate

```java
import com.azure.resourcemanager.keyvault.generated.models.AccessPolicyEntry;
import com.azure.resourcemanager.keyvault.generated.models.CertificatePermissions;
import com.azure.resourcemanager.keyvault.generated.models.IpRule;
import com.azure.resourcemanager.keyvault.generated.models.KeyPermissions;
import com.azure.resourcemanager.keyvault.generated.models.NetworkRuleAction;
import com.azure.resourcemanager.keyvault.generated.models.NetworkRuleBypassOptions;
import com.azure.resourcemanager.keyvault.generated.models.NetworkRuleSet;
import com.azure.resourcemanager.keyvault.generated.models.Permissions;
import com.azure.resourcemanager.keyvault.generated.models.SecretPermissions;
import com.azure.resourcemanager.keyvault.generated.models.Sku;
import com.azure.resourcemanager.keyvault.generated.models.SkuFamily;
import com.azure.resourcemanager.keyvault.generated.models.SkuName;
import com.azure.resourcemanager.keyvault.generated.models.VaultProperties;
import com.azure.resourcemanager.keyvault.generated.models.VirtualNetworkRule;
import java.util.Arrays;
import java.util.UUID;

/** Samples for Vaults CreateOrUpdate. */
public final class VaultsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/keyvault/resource-manager/Microsoft.KeyVault/preview/2021-11-01-preview/examples/createVaultWithNetworkAcls.json
     */
    /**
     * Sample code: Create or update a vault with network acls.
     *
     * @param manager Entry point to KeyVaultManager.
     */
    public static void createOrUpdateAVaultWithNetworkAcls(
        com.azure.resourcemanager.keyvault.generated.KeyVaultManager manager) {
        manager
            .vaults()
            .define("sample-vault")
            .withRegion("westus")
            .withExistingResourceGroup("sample-resource-group")
            .withProperties(
                new VaultProperties()
                    .withTenantId(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                    .withSku(new Sku().withFamily(SkuFamily.A).withName(SkuName.STANDARD))
                    .withEnabledForDeployment(true)
                    .withEnabledForDiskEncryption(true)
                    .withEnabledForTemplateDeployment(true)
                    .withNetworkAcls(
                        new NetworkRuleSet()
                            .withBypass(NetworkRuleBypassOptions.AZURE_SERVICES)
                            .withDefaultAction(NetworkRuleAction.DENY)
                            .withIpRules(
                                Arrays
                                    .asList(
                                        new IpRule().withValue("124.56.78.91"),
                                        new IpRule().withValue("'10.91.4.0/24'")))
                            .withVirtualNetworkRules(
                                Arrays
                                    .asList(
                                        new VirtualNetworkRule()
                                            .withId(
                                                "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.Network/virtualNetworks/test-vnet/subnets/subnet1")))))
            .create();
    }

    /*
     * x-ms-original-file: specification/keyvault/resource-manager/Microsoft.KeyVault/preview/2021-11-01-preview/examples/createVault.json
     */
    /**
     * Sample code: Create a new vault or update an existing vault.
     *
     * @param manager Entry point to KeyVaultManager.
     */
    public static void createANewVaultOrUpdateAnExistingVault(
        com.azure.resourcemanager.keyvault.generated.KeyVaultManager manager) {
        manager
            .vaults()
            .define("sample-vault")
            .withRegion("westus")
            .withExistingResourceGroup("sample-resource-group")
            .withProperties(
                new VaultProperties()
                    .withTenantId(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                    .withSku(new Sku().withFamily(SkuFamily.A).withName(SkuName.STANDARD))
                    .withAccessPolicies(
                        Arrays
                            .asList(
                                new AccessPolicyEntry()
                                    .withTenantId(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                                    .withObjectId("00000000-0000-0000-0000-000000000000")
                                    .withPermissions(
                                        new Permissions()
                                            .withKeys(
                                                Arrays
                                                    .asList(
                                                        KeyPermissions.ENCRYPT,
                                                        KeyPermissions.DECRYPT,
                                                        KeyPermissions.WRAP_KEY,
                                                        KeyPermissions.UNWRAP_KEY,
                                                        KeyPermissions.SIGN,
                                                        KeyPermissions.VERIFY,
                                                        KeyPermissions.GET,
                                                        KeyPermissions.LIST,
                                                        KeyPermissions.CREATE,
                                                        KeyPermissions.UPDATE,
                                                        KeyPermissions.IMPORT,
                                                        KeyPermissions.DELETE,
                                                        KeyPermissions.BACKUP,
                                                        KeyPermissions.RESTORE,
                                                        KeyPermissions.RECOVER,
                                                        KeyPermissions.PURGE))
                                            .withSecrets(
                                                Arrays
                                                    .asList(
                                                        SecretPermissions.GET,
                                                        SecretPermissions.LIST,
                                                        SecretPermissions.SET,
                                                        SecretPermissions.DELETE,
                                                        SecretPermissions.BACKUP,
                                                        SecretPermissions.RESTORE,
                                                        SecretPermissions.RECOVER,
                                                        SecretPermissions.PURGE))
                                            .withCertificates(
                                                Arrays
                                                    .asList(
                                                        CertificatePermissions.GET,
                                                        CertificatePermissions.LIST,
                                                        CertificatePermissions.DELETE,
                                                        CertificatePermissions.CREATE,
                                                        CertificatePermissions.IMPORT,
                                                        CertificatePermissions.UPDATE,
                                                        CertificatePermissions.MANAGECONTACTS,
                                                        CertificatePermissions.GETISSUERS,
                                                        CertificatePermissions.LISTISSUERS,
                                                        CertificatePermissions.SETISSUERS,
                                                        CertificatePermissions.DELETEISSUERS,
                                                        CertificatePermissions.MANAGEISSUERS,
                                                        CertificatePermissions.RECOVER,
                                                        CertificatePermissions.PURGE)))))
                    .withEnabledForDeployment(true)
                    .withEnabledForDiskEncryption(true)
                    .withEnabledForTemplateDeployment(true)
                    .withPublicNetworkAccess("Enabled"))
            .create();
    }
}
```

### Vaults_Delete

```java
import com.azure.core.util.Context;

/** Samples for Vaults Delete. */
public final class VaultsDeleteSamples {
    /*
     * x-ms-original-file: specification/keyvault/resource-manager/Microsoft.KeyVault/preview/2021-11-01-preview/examples/deleteVault.json
     */
    /**
     * Sample code: Delete a vault.
     *
     * @param manager Entry point to KeyVaultManager.
     */
    public static void deleteAVault(com.azure.resourcemanager.keyvault.generated.KeyVaultManager manager) {
        manager.vaults().deleteWithResponse("sample-resource-group", "sample-vault", Context.NONE);
    }
}
```

### Vaults_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Vaults GetByResourceGroup. */
public final class VaultsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/keyvault/resource-manager/Microsoft.KeyVault/preview/2021-11-01-preview/examples/getVault.json
     */
    /**
     * Sample code: Retrieve a vault.
     *
     * @param manager Entry point to KeyVaultManager.
     */
    public static void retrieveAVault(com.azure.resourcemanager.keyvault.generated.KeyVaultManager manager) {
        manager.vaults().getByResourceGroupWithResponse("sample-resource-group", "sample-vault", Context.NONE);
    }
}
```

### Vaults_GetDeleted

```java
import com.azure.core.util.Context;

/** Samples for Vaults GetDeleted. */
public final class VaultsGetDeletedSamples {
    /*
     * x-ms-original-file: specification/keyvault/resource-manager/Microsoft.KeyVault/preview/2021-11-01-preview/examples/getDeletedVault.json
     */
    /**
     * Sample code: Retrieve a deleted vault.
     *
     * @param manager Entry point to KeyVaultManager.
     */
    public static void retrieveADeletedVault(com.azure.resourcemanager.keyvault.generated.KeyVaultManager manager) {
        manager.vaults().getDeletedWithResponse("sample-vault", "westus", Context.NONE);
    }
}
```

### Vaults_List

```java
import com.azure.core.util.Context;

/** Samples for Vaults List. */
public final class VaultsListSamples {
    /*
     * x-ms-original-file: specification/keyvault/resource-manager/Microsoft.KeyVault/preview/2021-11-01-preview/examples/listVault.json
     */
    /**
     * Sample code: List vaults in the specified subscription.
     *
     * @param manager Entry point to KeyVaultManager.
     */
    public static void listVaultsInTheSpecifiedSubscription(
        com.azure.resourcemanager.keyvault.generated.KeyVaultManager manager) {
        manager.vaults().list(1, Context.NONE);
    }
}
```

### Vaults_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Vaults ListByResourceGroup. */
public final class VaultsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/keyvault/resource-manager/Microsoft.KeyVault/preview/2021-11-01-preview/examples/listVaultByResourceGroup.json
     */
    /**
     * Sample code: List vaults in the specified resource group.
     *
     * @param manager Entry point to KeyVaultManager.
     */
    public static void listVaultsInTheSpecifiedResourceGroup(
        com.azure.resourcemanager.keyvault.generated.KeyVaultManager manager) {
        manager.vaults().listByResourceGroup("sample-group", 1, Context.NONE);
    }
}
```

### Vaults_ListBySubscription

```java
import com.azure.core.util.Context;

/** Samples for Vaults ListBySubscription. */
public final class VaultsListBySubscriptionSamples {
    /*
     * x-ms-original-file: specification/keyvault/resource-manager/Microsoft.KeyVault/preview/2021-11-01-preview/examples/listVaultBySubscription.json
     */
    /**
     * Sample code: List vaults in the specified subscription.
     *
     * @param manager Entry point to KeyVaultManager.
     */
    public static void listVaultsInTheSpecifiedSubscription(
        com.azure.resourcemanager.keyvault.generated.KeyVaultManager manager) {
        manager.vaults().listBySubscription(1, Context.NONE);
    }
}
```

### Vaults_ListDeleted

```java
import com.azure.core.util.Context;

/** Samples for Vaults ListDeleted. */
public final class VaultsListDeletedSamples {
    /*
     * x-ms-original-file: specification/keyvault/resource-manager/Microsoft.KeyVault/preview/2021-11-01-preview/examples/listDeletedVaults.json
     */
    /**
     * Sample code: List deleted vaults in the specified subscription.
     *
     * @param manager Entry point to KeyVaultManager.
     */
    public static void listDeletedVaultsInTheSpecifiedSubscription(
        com.azure.resourcemanager.keyvault.generated.KeyVaultManager manager) {
        manager.vaults().listDeleted(Context.NONE);
    }
}
```

### Vaults_PurgeDeleted

```java
import com.azure.core.util.Context;

/** Samples for Vaults PurgeDeleted. */
public final class VaultsPurgeDeletedSamples {
    /*
     * x-ms-original-file: specification/keyvault/resource-manager/Microsoft.KeyVault/preview/2021-11-01-preview/examples/purgeDeletedVault.json
     */
    /**
     * Sample code: Purge a deleted vault.
     *
     * @param manager Entry point to KeyVaultManager.
     */
    public static void purgeADeletedVault(com.azure.resourcemanager.keyvault.generated.KeyVaultManager manager) {
        manager.vaults().purgeDeleted("sample-vault", "westus", Context.NONE);
    }
}
```

### Vaults_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.keyvault.generated.models.AccessPolicyEntry;
import com.azure.resourcemanager.keyvault.generated.models.CertificatePermissions;
import com.azure.resourcemanager.keyvault.generated.models.KeyPermissions;
import com.azure.resourcemanager.keyvault.generated.models.Permissions;
import com.azure.resourcemanager.keyvault.generated.models.SecretPermissions;
import com.azure.resourcemanager.keyvault.generated.models.Sku;
import com.azure.resourcemanager.keyvault.generated.models.SkuFamily;
import com.azure.resourcemanager.keyvault.generated.models.SkuName;
import com.azure.resourcemanager.keyvault.generated.models.Vault;
import com.azure.resourcemanager.keyvault.generated.models.VaultPatchProperties;
import java.util.Arrays;
import java.util.UUID;

/** Samples for Vaults Update. */
public final class VaultsUpdateSamples {
    /*
     * x-ms-original-file: specification/keyvault/resource-manager/Microsoft.KeyVault/preview/2021-11-01-preview/examples/updateVault.json
     */
    /**
     * Sample code: Update an existing vault.
     *
     * @param manager Entry point to KeyVaultManager.
     */
    public static void updateAnExistingVault(com.azure.resourcemanager.keyvault.generated.KeyVaultManager manager) {
        Vault resource =
            manager
                .vaults()
                .getByResourceGroupWithResponse("sample-resource-group", "sample-vault", Context.NONE)
                .getValue();
        resource
            .update()
            .withProperties(
                new VaultPatchProperties()
                    .withTenantId(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                    .withSku(new Sku().withFamily(SkuFamily.A).withName(SkuName.STANDARD))
                    .withAccessPolicies(
                        Arrays
                            .asList(
                                new AccessPolicyEntry()
                                    .withTenantId(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                                    .withObjectId("00000000-0000-0000-0000-000000000000")
                                    .withPermissions(
                                        new Permissions()
                                            .withKeys(
                                                Arrays
                                                    .asList(
                                                        KeyPermissions.ENCRYPT,
                                                        KeyPermissions.DECRYPT,
                                                        KeyPermissions.WRAP_KEY,
                                                        KeyPermissions.UNWRAP_KEY,
                                                        KeyPermissions.SIGN,
                                                        KeyPermissions.VERIFY,
                                                        KeyPermissions.GET,
                                                        KeyPermissions.LIST,
                                                        KeyPermissions.CREATE,
                                                        KeyPermissions.UPDATE,
                                                        KeyPermissions.IMPORT,
                                                        KeyPermissions.DELETE,
                                                        KeyPermissions.BACKUP,
                                                        KeyPermissions.RESTORE,
                                                        KeyPermissions.RECOVER,
                                                        KeyPermissions.PURGE))
                                            .withSecrets(
                                                Arrays
                                                    .asList(
                                                        SecretPermissions.GET,
                                                        SecretPermissions.LIST,
                                                        SecretPermissions.SET,
                                                        SecretPermissions.DELETE,
                                                        SecretPermissions.BACKUP,
                                                        SecretPermissions.RESTORE,
                                                        SecretPermissions.RECOVER,
                                                        SecretPermissions.PURGE))
                                            .withCertificates(
                                                Arrays
                                                    .asList(
                                                        CertificatePermissions.GET,
                                                        CertificatePermissions.LIST,
                                                        CertificatePermissions.DELETE,
                                                        CertificatePermissions.CREATE,
                                                        CertificatePermissions.IMPORT,
                                                        CertificatePermissions.UPDATE,
                                                        CertificatePermissions.MANAGECONTACTS,
                                                        CertificatePermissions.GETISSUERS,
                                                        CertificatePermissions.LISTISSUERS,
                                                        CertificatePermissions.SETISSUERS,
                                                        CertificatePermissions.DELETEISSUERS,
                                                        CertificatePermissions.MANAGEISSUERS,
                                                        CertificatePermissions.RECOVER,
                                                        CertificatePermissions.PURGE)))))
                    .withEnabledForDeployment(true)
                    .withEnabledForDiskEncryption(true)
                    .withEnabledForTemplateDeployment(true)
                    .withPublicNetworkAccess("Enabled"))
            .apply();
    }
}
```

### Vaults_UpdateAccessPolicy

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.keyvault.generated.fluent.models.VaultAccessPolicyParametersInner;
import com.azure.resourcemanager.keyvault.generated.models.AccessPolicyEntry;
import com.azure.resourcemanager.keyvault.generated.models.AccessPolicyUpdateKind;
import com.azure.resourcemanager.keyvault.generated.models.CertificatePermissions;
import com.azure.resourcemanager.keyvault.generated.models.KeyPermissions;
import com.azure.resourcemanager.keyvault.generated.models.Permissions;
import com.azure.resourcemanager.keyvault.generated.models.SecretPermissions;
import com.azure.resourcemanager.keyvault.generated.models.VaultAccessPolicyProperties;
import java.util.Arrays;
import java.util.UUID;

/** Samples for Vaults UpdateAccessPolicy. */
public final class VaultsUpdateAccessPolicySamples {
    /*
     * x-ms-original-file: specification/keyvault/resource-manager/Microsoft.KeyVault/preview/2021-11-01-preview/examples/updateAccessPoliciesAdd.json
     */
    /**
     * Sample code: Add an access policy, or update an access policy with new permissions.
     *
     * @param manager Entry point to KeyVaultManager.
     */
    public static void addAnAccessPolicyOrUpdateAnAccessPolicyWithNewPermissions(
        com.azure.resourcemanager.keyvault.generated.KeyVaultManager manager) {
        manager
            .vaults()
            .updateAccessPolicyWithResponse(
                "sample-group",
                "sample-vault",
                AccessPolicyUpdateKind.ADD,
                new VaultAccessPolicyParametersInner()
                    .withProperties(
                        new VaultAccessPolicyProperties()
                            .withAccessPolicies(
                                Arrays
                                    .asList(
                                        new AccessPolicyEntry()
                                            .withTenantId(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                                            .withObjectId("00000000-0000-0000-0000-000000000000")
                                            .withPermissions(
                                                new Permissions()
                                                    .withKeys(Arrays.asList(KeyPermissions.ENCRYPT))
                                                    .withSecrets(Arrays.asList(SecretPermissions.GET))
                                                    .withCertificates(Arrays.asList(CertificatePermissions.GET)))))),
                Context.NONE);
    }
}
```

