# Code snippets and samples


## AzureKeyVaultSecretProviderClasses

- [CreateOrUpdate](#azurekeyvaultsecretproviderclasses_createorupdate)
- [Delete](#azurekeyvaultsecretproviderclasses_delete)
- [GetByResourceGroup](#azurekeyvaultsecretproviderclasses_getbyresourcegroup)
- [List](#azurekeyvaultsecretproviderclasses_list)
- [ListByResourceGroup](#azurekeyvaultsecretproviderclasses_listbyresourcegroup)
- [Update](#azurekeyvaultsecretproviderclasses_update)

## Operations

- [List](#operations_list)

## SecretSyncs

- [CreateOrUpdate](#secretsyncs_createorupdate)
- [Delete](#secretsyncs_delete)
- [GetByResourceGroup](#secretsyncs_getbyresourcegroup)
- [List](#secretsyncs_list)
- [ListByResourceGroup](#secretsyncs_listbyresourcegroup)
- [Update](#secretsyncs_update)
### AzureKeyVaultSecretProviderClasses_CreateOrUpdate

```java
import com.azure.resourcemanager.secretsstoreextension.models.AzureKeyVaultSecretProviderClassProperties;
import com.azure.resourcemanager.secretsstoreextension.models.ExtendedLocation;
import com.azure.resourcemanager.secretsstoreextension.models.ExtendedLocationType;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for AzureKeyVaultSecretProviderClasses CreateOrUpdate.
 */
public final class AzureKeyVaultSecretProviderClassesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2024-08-21-preview/AzureKeyVaultSecretProviderClasses_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: AzureKeyVaultSecretProviderClasses_CreateOrUpdate.
     * 
     * @param manager Entry point to SecretsStoreExtensionManager.
     */
    public static void azureKeyVaultSecretProviderClassesCreateOrUpdate(
        com.azure.resourcemanager.secretsstoreextension.SecretsStoreExtensionManager manager) {
        manager.azureKeyVaultSecretProviderClasses()
            .define("akvspc-ssc-example")
            .withRegion("eastus")
            .withExistingResourceGroup("rg-ssc-example")
            .withTags(mapOf("example-tag", "example-tag-value"))
            .withProperties(new AzureKeyVaultSecretProviderClassProperties().withKeyvaultName("fakeTokenPlaceholder")
                .withClientId("00000000-0000-0000-0000-000000000000")
                .withTenantId("00000000-0000-0000-0000-000000000000")
                .withObjects(
                    "array: |\n  - |\n    objectName: my-secret-object\n    objectType: secret\n    objectVersionHistory: 1"))
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg-ssc-example/providers/Microsoft.ExtendedLocation/customLocations/example-custom-location")
                .withType(ExtendedLocationType.CUSTOM_LOCATION))
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

### AzureKeyVaultSecretProviderClasses_Delete

```java
/**
 * Samples for AzureKeyVaultSecretProviderClasses Delete.
 */
public final class AzureKeyVaultSecretProviderClassesDeleteSamples {
    /*
     * x-ms-original-file: 2024-08-21-preview/AzureKeyVaultSecretProviderClasses_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: AzureKeyVaultSecretProviderClasses_Delete.
     * 
     * @param manager Entry point to SecretsStoreExtensionManager.
     */
    public static void azureKeyVaultSecretProviderClassesDelete(
        com.azure.resourcemanager.secretsstoreextension.SecretsStoreExtensionManager manager) {
        manager.azureKeyVaultSecretProviderClasses()
            .delete("rg-ssc-example", "akvspc-ssc-example", com.azure.core.util.Context.NONE);
    }
}
```

### AzureKeyVaultSecretProviderClasses_GetByResourceGroup

```java
/**
 * Samples for AzureKeyVaultSecretProviderClasses GetByResourceGroup.
 */
public final class AzureKeyVaultSecretProviderClassesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-08-21-preview/AzureKeyVaultSecretProviderClasses_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: AzureKeyVaultSecretProviderClasses_Get.
     * 
     * @param manager Entry point to SecretsStoreExtensionManager.
     */
    public static void azureKeyVaultSecretProviderClassesGet(
        com.azure.resourcemanager.secretsstoreextension.SecretsStoreExtensionManager manager) {
        manager.azureKeyVaultSecretProviderClasses()
            .getByResourceGroupWithResponse("rg-ssc-example", "akvspc-ssc-example", com.azure.core.util.Context.NONE);
    }
}
```

### AzureKeyVaultSecretProviderClasses_List

```java
/**
 * Samples for AzureKeyVaultSecretProviderClasses List.
 */
public final class AzureKeyVaultSecretProviderClassesListSamples {
    /*
     * x-ms-original-file: 2024-08-21-preview/AzureKeyVaultSecretProviderClasses_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: AzureKeyVaultSecretProviderClasses_ListBySubscription.
     * 
     * @param manager Entry point to SecretsStoreExtensionManager.
     */
    public static void azureKeyVaultSecretProviderClassesListBySubscription(
        com.azure.resourcemanager.secretsstoreextension.SecretsStoreExtensionManager manager) {
        manager.azureKeyVaultSecretProviderClasses().list(com.azure.core.util.Context.NONE);
    }
}
```

### AzureKeyVaultSecretProviderClasses_ListByResourceGroup

```java
/**
 * Samples for AzureKeyVaultSecretProviderClasses ListByResourceGroup.
 */
public final class AzureKeyVaultSecretProviderClassesListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-08-21-preview/AzureKeyVaultSecretProviderClasses_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: AzureKeyVaultSecretProviderClasses_ListByResourceGroup.
     * 
     * @param manager Entry point to SecretsStoreExtensionManager.
     */
    public static void azureKeyVaultSecretProviderClassesListByResourceGroup(
        com.azure.resourcemanager.secretsstoreextension.SecretsStoreExtensionManager manager) {
        manager.azureKeyVaultSecretProviderClasses()
            .listByResourceGroup("rg-ssc-example", com.azure.core.util.Context.NONE);
    }
}
```

### AzureKeyVaultSecretProviderClasses_Update

```java
import com.azure.resourcemanager.secretsstoreextension.models.AzureKeyVaultSecretProviderClass;
import com.azure.resourcemanager.secretsstoreextension.models.AzureKeyVaultSecretProviderClassUpdateProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for AzureKeyVaultSecretProviderClasses Update.
 */
public final class AzureKeyVaultSecretProviderClassesUpdateSamples {
    /*
     * x-ms-original-file: 2024-08-21-preview/AzureKeyVaultSecretProviderClasses_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: AzureKeyVaultSecretProviderClasses_Update.
     * 
     * @param manager Entry point to SecretsStoreExtensionManager.
     */
    public static void azureKeyVaultSecretProviderClassesUpdate(
        com.azure.resourcemanager.secretsstoreextension.SecretsStoreExtensionManager manager) {
        AzureKeyVaultSecretProviderClass resource = manager.azureKeyVaultSecretProviderClasses()
            .getByResourceGroupWithResponse("rg-ssc-example", "akvspc-ssc-example", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("example-tag", "example-tag-value"))
            .withProperties(new AzureKeyVaultSecretProviderClassUpdateProperties()
                .withKeyvaultName("fakeTokenPlaceholder")
                .withClientId("00000000-0000-0000-0000-000000000000")
                .withTenantId("00000000-0000-0000-0000-000000000000")
                .withObjects(
                    "array: |\n  - |\n    objectName: my-secret-object\n    objectType: secret\n    objectVersionHistory: 1"))
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
     * x-ms-original-file: 2024-08-21-preview/Operations_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to SecretsStoreExtensionManager.
     */
    public static void
        operationsList(com.azure.resourcemanager.secretsstoreextension.SecretsStoreExtensionManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### SecretSyncs_CreateOrUpdate

```java
import com.azure.resourcemanager.secretsstoreextension.models.ExtendedLocation;
import com.azure.resourcemanager.secretsstoreextension.models.ExtendedLocationType;
import com.azure.resourcemanager.secretsstoreextension.models.KubernetesSecretObjectMapping;
import com.azure.resourcemanager.secretsstoreextension.models.KubernetesSecretType;
import com.azure.resourcemanager.secretsstoreextension.models.SecretSyncProperties;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for SecretSyncs CreateOrUpdate.
 */
public final class SecretSyncsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2024-08-21-preview/SecretSyncs_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: SecretSyncs_CreateOrUpdate.
     * 
     * @param manager Entry point to SecretsStoreExtensionManager.
     */
    public static void secretSyncsCreateOrUpdate(
        com.azure.resourcemanager.secretsstoreextension.SecretsStoreExtensionManager manager) {
        manager.secretSyncs()
            .define("secretsync-ssc-example")
            .withRegion("eastus")
            .withExistingResourceGroup("rg-ssc-example")
            .withTags(mapOf("example-tag", "example-tag-value"))
            .withProperties(new SecretSyncProperties().withSecretProviderClassName("fakeTokenPlaceholder")
                .withServiceAccountName("example-k8s-sa-name")
                .withKubernetesSecretType(KubernetesSecretType.OPAQUE)
                .withObjectSecretMapping(
                    Arrays.asList(new KubernetesSecretObjectMapping().withSourcePath("kv-secret-name/0")
                        .withTargetKey("fakeTokenPlaceholder"))))
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg-ssc-example/providers/Microsoft.ExtendedLocation/customLocations/example-custom-location")
                .withType(ExtendedLocationType.CUSTOM_LOCATION))
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

### SecretSyncs_Delete

```java
/**
 * Samples for SecretSyncs Delete.
 */
public final class SecretSyncsDeleteSamples {
    /*
     * x-ms-original-file: 2024-08-21-preview/SecretSyncs_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: SecretSyncs_Delete.
     * 
     * @param manager Entry point to SecretsStoreExtensionManager.
     */
    public static void
        secretSyncsDelete(com.azure.resourcemanager.secretsstoreextension.SecretsStoreExtensionManager manager) {
        manager.secretSyncs().delete("rg-ssc-example", "secretsync-ssc-example", com.azure.core.util.Context.NONE);
    }
}
```

### SecretSyncs_GetByResourceGroup

```java
/**
 * Samples for SecretSyncs GetByResourceGroup.
 */
public final class SecretSyncsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-08-21-preview/SecretSyncs_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: SecretSyncs_Get.
     * 
     * @param manager Entry point to SecretsStoreExtensionManager.
     */
    public static void
        secretSyncsGet(com.azure.resourcemanager.secretsstoreextension.SecretsStoreExtensionManager manager) {
        manager.secretSyncs()
            .getByResourceGroupWithResponse("rg-ssc-example", "secretsync-ssc-example",
                com.azure.core.util.Context.NONE);
    }
}
```

### SecretSyncs_List

```java
/**
 * Samples for SecretSyncs List.
 */
public final class SecretSyncsListSamples {
    /*
     * x-ms-original-file: 2024-08-21-preview/SecretSyncs_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: SecretSyncs_ListBySubscription.
     * 
     * @param manager Entry point to SecretsStoreExtensionManager.
     */
    public static void secretSyncsListBySubscription(
        com.azure.resourcemanager.secretsstoreextension.SecretsStoreExtensionManager manager) {
        manager.secretSyncs().list(com.azure.core.util.Context.NONE);
    }
}
```

### SecretSyncs_ListByResourceGroup

```java
/**
 * Samples for SecretSyncs ListByResourceGroup.
 */
public final class SecretSyncsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-08-21-preview/SecretSyncs_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: SecretSyncs_ListByResourceGroup.
     * 
     * @param manager Entry point to SecretsStoreExtensionManager.
     */
    public static void secretSyncsListByResourceGroup(
        com.azure.resourcemanager.secretsstoreextension.SecretsStoreExtensionManager manager) {
        manager.secretSyncs().listByResourceGroup("rg-ssc-example", com.azure.core.util.Context.NONE);
    }
}
```

### SecretSyncs_Update

```java
import com.azure.resourcemanager.secretsstoreextension.models.KubernetesSecretObjectMapping;
import com.azure.resourcemanager.secretsstoreextension.models.KubernetesSecretType;
import com.azure.resourcemanager.secretsstoreextension.models.SecretSync;
import com.azure.resourcemanager.secretsstoreextension.models.SecretSyncUpdateProperties;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for SecretSyncs Update.
 */
public final class SecretSyncsUpdateSamples {
    /*
     * x-ms-original-file: 2024-08-21-preview/SecretSyncs_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: SecretSyncs_Update.
     * 
     * @param manager Entry point to SecretsStoreExtensionManager.
     */
    public static void
        secretSyncsUpdate(com.azure.resourcemanager.secretsstoreextension.SecretsStoreExtensionManager manager) {
        SecretSync resource = manager.secretSyncs()
            .getByResourceGroupWithResponse("rg-ssc-example", "secretsync-ssc-example",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("example-tag", "example-tag-value"))
            .withProperties(new SecretSyncUpdateProperties().withSecretProviderClassName("fakeTokenPlaceholder")
                .withServiceAccountName(
                    "fcldqfdfpktndlntuoxicsftelhefevovmlycflfwzckvamiqjnjugandqaqqeccsbzztfmmeunvhsafgerbcsdbnmsyqivygornebbkusuvphwghgouxvcbvmbydqjzoxextnyowsnyymadniwdrrxtogeveldpejixmsrzzfqkquaxdpzwvecevqwasxgxxchrfa")
                .withKubernetesSecretType(KubernetesSecretType.OPAQUE)
                .withForceSynchronization("arbitrarystring")
                .withObjectSecretMapping(Arrays.asList(new KubernetesSecretObjectMapping().withSourcePath(
                    "ssrzmbvdiomkvzrdsyilwlfzicfydnbjwjsnohrppkukjddrunfslkrnexunuckmghixdssposvndpiqchpqrkjuqbapoisvqdvgstvdonsmlpsmticfvuhqlofpaxfdg")
                    .withTargetKey("fakeTokenPlaceholder"))))
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

