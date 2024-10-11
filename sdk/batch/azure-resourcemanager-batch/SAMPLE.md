# Code snippets and samples


## Application

- [Create](#application_create)
- [Delete](#application_delete)
- [Get](#application_get)
- [List](#application_list)
- [Update](#application_update)

## ApplicationPackage

- [Activate](#applicationpackage_activate)
- [Create](#applicationpackage_create)
- [Delete](#applicationpackage_delete)
- [Get](#applicationpackage_get)
- [List](#applicationpackage_list)

## BatchAccount

- [Create](#batchaccount_create)
- [Delete](#batchaccount_delete)
- [GetByResourceGroup](#batchaccount_getbyresourcegroup)
- [GetDetector](#batchaccount_getdetector)
- [GetKeys](#batchaccount_getkeys)
- [List](#batchaccount_list)
- [ListByResourceGroup](#batchaccount_listbyresourcegroup)
- [ListDetectors](#batchaccount_listdetectors)
- [ListOutboundNetworkDependenciesEndpoints](#batchaccount_listoutboundnetworkdependenciesendpoints)
- [RegenerateKey](#batchaccount_regeneratekey)
- [SynchronizeAutoStorageKeys](#batchaccount_synchronizeautostoragekeys)
- [Update](#batchaccount_update)

## Certificate

- [CancelDeletion](#certificate_canceldeletion)
- [Create](#certificate_create)
- [Delete](#certificate_delete)
- [Get](#certificate_get)
- [ListByBatchAccount](#certificate_listbybatchaccount)
- [Update](#certificate_update)

## Location

- [CheckNameAvailability](#location_checknameavailability)
- [GetQuotas](#location_getquotas)
- [ListSupportedVirtualMachineSkus](#location_listsupportedvirtualmachineskus)

## NetworkSecurityPerimeter

- [GetConfiguration](#networksecurityperimeter_getconfiguration)
- [ListConfigurations](#networksecurityperimeter_listconfigurations)
- [ReconcileConfiguration](#networksecurityperimeter_reconcileconfiguration)

## Operations

- [List](#operations_list)

## Pool

- [Create](#pool_create)
- [Delete](#pool_delete)
- [DisableAutoScale](#pool_disableautoscale)
- [Get](#pool_get)
- [ListByBatchAccount](#pool_listbybatchaccount)
- [StopResize](#pool_stopresize)
- [Update](#pool_update)

## PrivateEndpointConnection

- [Delete](#privateendpointconnection_delete)
- [Get](#privateendpointconnection_get)
- [ListByBatchAccount](#privateendpointconnection_listbybatchaccount)
- [Update](#privateendpointconnection_update)

## PrivateLinkResource

- [Get](#privatelinkresource_get)
- [ListByBatchAccount](#privatelinkresource_listbybatchaccount)
### Application_Create

```java
/**
 * Samples for Application Create.
 */
public final class ApplicationCreateSamples {
    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/ApplicationCreate.json
     */
    /**
     * Sample code: ApplicationCreate.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void applicationCreate(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.applications()
            .define("app1")
            .withExistingBatchAccount("default-azurebatch-japaneast", "sampleacct")
            .withDisplayName("myAppName")
            .withAllowUpdates(false)
            .create();
    }
}
```

### Application_Delete

```java
/**
 * Samples for Application Delete.
 */
public final class ApplicationDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/ApplicationDelete.json
     */
    /**
     * Sample code: ApplicationDelete.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void applicationDelete(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.applications()
            .deleteWithResponse("default-azurebatch-japaneast", "sampleacct", "app1", com.azure.core.util.Context.NONE);
    }
}
```

### Application_Get

```java
/**
 * Samples for Application Get.
 */
public final class ApplicationGetSamples {
    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/ApplicationGet.json
     */
    /**
     * Sample code: ApplicationGet.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void applicationGet(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.applications()
            .getWithResponse("default-azurebatch-japaneast", "sampleacct", "app1", com.azure.core.util.Context.NONE);
    }
}
```

### Application_List

```java
/**
 * Samples for Application List.
 */
public final class ApplicationListSamples {
    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/ApplicationList.json
     */
    /**
     * Sample code: ApplicationList.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void applicationList(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.applications()
            .list("default-azurebatch-japaneast", "sampleacct", null, com.azure.core.util.Context.NONE);
    }
}
```

### Application_Update

```java
import com.azure.resourcemanager.batch.models.Application;

/**
 * Samples for Application Update.
 */
public final class ApplicationUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/ApplicationUpdate.json
     */
    /**
     * Sample code: ApplicationUpdate.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void applicationUpdate(com.azure.resourcemanager.batch.BatchManager manager) {
        Application resource = manager.applications()
            .getWithResponse("default-azurebatch-japaneast", "sampleacct", "app1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withDisplayName("myAppName").withAllowUpdates(true).withDefaultVersion("2").apply();
    }
}
```

### ApplicationPackage_Activate

```java
import com.azure.resourcemanager.batch.models.ActivateApplicationPackageParameters;

/**
 * Samples for ApplicationPackage Activate.
 */
public final class ApplicationPackageActivateSamples {
    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/ApplicationPackageActivate.json
     */
    /**
     * Sample code: ApplicationPackageActivate.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void applicationPackageActivate(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.applicationPackages()
            .activateWithResponse("default-azurebatch-japaneast", "sampleacct", "app1", "1",
                new ActivateApplicationPackageParameters().withFormat("zip"), com.azure.core.util.Context.NONE);
    }
}
```

### ApplicationPackage_Create

```java
/**
 * Samples for ApplicationPackage Create.
 */
public final class ApplicationPackageCreateSamples {
    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/ApplicationPackageCreate.json
     */
    /**
     * Sample code: ApplicationPackageCreate.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void applicationPackageCreate(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.applicationPackages()
            .define("1")
            .withExistingApplication("default-azurebatch-japaneast", "sampleacct", "app1")
            .create();
    }
}
```

### ApplicationPackage_Delete

```java
/**
 * Samples for ApplicationPackage Delete.
 */
public final class ApplicationPackageDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/ApplicationPackageDelete.json
     */
    /**
     * Sample code: ApplicationPackageDelete.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void applicationPackageDelete(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.applicationPackages()
            .deleteWithResponse("default-azurebatch-japaneast", "sampleacct", "app1", "1",
                com.azure.core.util.Context.NONE);
    }
}
```

### ApplicationPackage_Get

```java
/**
 * Samples for ApplicationPackage Get.
 */
public final class ApplicationPackageGetSamples {
    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/ApplicationPackageGet.json
     */
    /**
     * Sample code: ApplicationPackageGet.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void applicationPackageGet(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.applicationPackages()
            .getWithResponse("default-azurebatch-japaneast", "sampleacct", "app1", "1",
                com.azure.core.util.Context.NONE);
    }
}
```

### ApplicationPackage_List

```java
/**
 * Samples for ApplicationPackage List.
 */
public final class ApplicationPackageListSamples {
    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/ApplicationPackageList.json
     */
    /**
     * Sample code: ApplicationPackageList.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void applicationPackageList(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.applicationPackages()
            .list("default-azurebatch-japaneast", "sampleacct", "app1", null, com.azure.core.util.Context.NONE);
    }
}
```

### BatchAccount_Create

```java
import com.azure.resourcemanager.batch.models.AutoStorageBaseProperties;
import com.azure.resourcemanager.batch.models.BatchAccountIdentity;
import com.azure.resourcemanager.batch.models.KeyVaultReference;
import com.azure.resourcemanager.batch.models.PoolAllocationMode;
import com.azure.resourcemanager.batch.models.PublicNetworkAccessType;
import com.azure.resourcemanager.batch.models.ResourceIdentityType;
import com.azure.resourcemanager.batch.models.UserAssignedIdentities;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for BatchAccount Create.
 */
public final class BatchAccountCreateSamples {
    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/BatchAccountCreate_BYOS.json
     */
    /**
     * Sample code: BatchAccountCreate_BYOS.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void batchAccountCreateBYOS(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.batchAccounts()
            .define("sampleacct")
            .withRegion("japaneast")
            .withExistingResourceGroup("default-azurebatch-japaneast")
            .withAutoStorage(new AutoStorageBaseProperties().withStorageAccountId(
                "/subscriptions/subid/resourceGroups/default-azurebatch-japaneast/providers/Microsoft.Storage/storageAccounts/samplestorage"))
            .withPoolAllocationMode(PoolAllocationMode.USER_SUBSCRIPTION)
            .withKeyVaultReference(new KeyVaultReference().withId(
                "/subscriptions/subid/resourceGroups/default-azurebatch-japaneast/providers/Microsoft.KeyVault/vaults/sample")
                .withUrl("http://sample.vault.azure.net/"))
            .create();
    }

    /*
     * x-ms-original-file: specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/
     * BatchAccountCreate_UserAssignedIdentity.json
     */
    /**
     * Sample code: BatchAccountCreate_UserAssignedIdentity.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void batchAccountCreateUserAssignedIdentity(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.batchAccounts()
            .define("sampleacct")
            .withRegion("japaneast")
            .withExistingResourceGroup("default-azurebatch-japaneast")
            .withIdentity(new BatchAccountIdentity().withType(ResourceIdentityType.USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/subid/resourceGroups/default-azurebatch-japaneast/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id1",
                    new UserAssignedIdentities())))
            .withAutoStorage(new AutoStorageBaseProperties().withStorageAccountId(
                "/subscriptions/subid/resourceGroups/default-azurebatch-japaneast/providers/Microsoft.Storage/storageAccounts/samplestorage"))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/PrivateBatchAccountCreate.json
     */
    /**
     * Sample code: PrivateBatchAccountCreate.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void privateBatchAccountCreate(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.batchAccounts()
            .define("sampleacct")
            .withRegion("japaneast")
            .withExistingResourceGroup("default-azurebatch-japaneast")
            .withAutoStorage(new AutoStorageBaseProperties().withStorageAccountId(
                "/subscriptions/subid/resourceGroups/default-azurebatch-japaneast/providers/Microsoft.Storage/storageAccounts/samplestorage"))
            .withKeyVaultReference(new KeyVaultReference().withId(
                "/subscriptions/subid/resourceGroups/default-azurebatch-japaneast/providers/Microsoft.KeyVault/vaults/sample")
                .withUrl("http://sample.vault.azure.net/"))
            .withPublicNetworkAccess(PublicNetworkAccessType.DISABLED)
            .create();
    }

    /*
     * x-ms-original-file: specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/
     * BatchAccountCreate_SystemAssignedIdentity.json
     */
    /**
     * Sample code: BatchAccountCreate_SystemAssignedIdentity.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void batchAccountCreateSystemAssignedIdentity(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.batchAccounts()
            .define("sampleacct")
            .withRegion("japaneast")
            .withExistingResourceGroup("default-azurebatch-japaneast")
            .withIdentity(new BatchAccountIdentity().withType(ResourceIdentityType.SYSTEM_ASSIGNED))
            .withAutoStorage(new AutoStorageBaseProperties().withStorageAccountId(
                "/subscriptions/subid/resourceGroups/default-azurebatch-japaneast/providers/Microsoft.Storage/storageAccounts/samplestorage"))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/BatchAccountCreate_Default.json
     */
    /**
     * Sample code: BatchAccountCreate_Default.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void batchAccountCreateDefault(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.batchAccounts()
            .define("sampleacct")
            .withRegion("japaneast")
            .withExistingResourceGroup("default-azurebatch-japaneast")
            .withAutoStorage(new AutoStorageBaseProperties().withStorageAccountId(
                "/subscriptions/subid/resourceGroups/default-azurebatch-japaneast/providers/Microsoft.Storage/storageAccounts/samplestorage"))
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

### BatchAccount_Delete

```java
/**
 * Samples for BatchAccount Delete.
 */
public final class BatchAccountDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/BatchAccountDelete.json
     */
    /**
     * Sample code: BatchAccountDelete.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void batchAccountDelete(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.batchAccounts().delete("default-azurebatch-japaneast", "sampleacct", com.azure.core.util.Context.NONE);
    }
}
```

### BatchAccount_GetByResourceGroup

```java
/**
 * Samples for BatchAccount GetByResourceGroup.
 */
public final class BatchAccountGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/PrivateBatchAccountGet.json
     */
    /**
     * Sample code: PrivateBatchAccountGet.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void privateBatchAccountGet(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.batchAccounts()
            .getByResourceGroupWithResponse("default-azurebatch-japaneast", "sampleacct",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/BatchAccountGet.json
     */
    /**
     * Sample code: BatchAccountGet.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void batchAccountGet(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.batchAccounts()
            .getByResourceGroupWithResponse("default-azurebatch-japaneast", "sampleacct",
                com.azure.core.util.Context.NONE);
    }
}
```

### BatchAccount_GetDetector

```java
/**
 * Samples for BatchAccount GetDetector.
 */
public final class BatchAccountGetDetectorSamples {
    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/DetectorGet.json
     */
    /**
     * Sample code: GetDetector.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void getDetector(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.batchAccounts()
            .getDetectorWithResponse("default-azurebatch-japaneast", "sampleacct", "poolsAndNodes",
                com.azure.core.util.Context.NONE);
    }
}
```

### BatchAccount_GetKeys

```java
/**
 * Samples for BatchAccount GetKeys.
 */
public final class BatchAccountGetKeysSamples {
    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/BatchAccountGetKeys.json
     */
    /**
     * Sample code: BatchAccountGetKeys.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void batchAccountGetKeys(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.batchAccounts()
            .getKeysWithResponse("default-azurebatch-japaneast", "sampleacct", com.azure.core.util.Context.NONE);
    }
}
```

### BatchAccount_List

```java
/**
 * Samples for BatchAccount List.
 */
public final class BatchAccountListSamples {
    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/BatchAccountList.json
     */
    /**
     * Sample code: BatchAccountList.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void batchAccountList(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.batchAccounts().list(com.azure.core.util.Context.NONE);
    }
}
```

### BatchAccount_ListByResourceGroup

```java
/**
 * Samples for BatchAccount ListByResourceGroup.
 */
public final class BatchAccountListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/BatchAccountListByResourceGroup.
     * json
     */
    /**
     * Sample code: BatchAccountListByResourceGroup.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void batchAccountListByResourceGroup(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.batchAccounts().listByResourceGroup("default-azurebatch-japaneast", com.azure.core.util.Context.NONE);
    }
}
```

### BatchAccount_ListDetectors

```java
/**
 * Samples for BatchAccount ListDetectors.
 */
public final class BatchAccountListDetectorsSamples {
    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/DetectorList.json
     */
    /**
     * Sample code: ListDetectors.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void listDetectors(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.batchAccounts()
            .listDetectors("default-azurebatch-japaneast", "sampleacct", com.azure.core.util.Context.NONE);
    }
}
```

### BatchAccount_ListOutboundNetworkDependenciesEndpoints

```java
/**
 * Samples for BatchAccount ListOutboundNetworkDependenciesEndpoints.
 */
public final class BatchAccountListOutboundNetworkDependenciesEndpointsSamples {
    /*
     * x-ms-original-file: specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/
     * BatchAccountListOutboundNetworkDependenciesEndpoints.json
     */
    /**
     * Sample code: ListOutboundNetworkDependencies.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void listOutboundNetworkDependencies(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.batchAccounts()
            .listOutboundNetworkDependenciesEndpoints("default-azurebatch-japaneast", "sampleacct",
                com.azure.core.util.Context.NONE);
    }
}
```

### BatchAccount_RegenerateKey

```java
import com.azure.resourcemanager.batch.models.AccountKeyType;
import com.azure.resourcemanager.batch.models.BatchAccountRegenerateKeyParameters;

/**
 * Samples for BatchAccount RegenerateKey.
 */
public final class BatchAccountRegenerateKeySamples {
    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/BatchAccountRegenerateKey.json
     */
    /**
     * Sample code: BatchAccountRegenerateKey.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void batchAccountRegenerateKey(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.batchAccounts()
            .regenerateKeyWithResponse("default-azurebatch-japaneast", "sampleacct",
                new BatchAccountRegenerateKeyParameters().withKeyName(AccountKeyType.PRIMARY),
                com.azure.core.util.Context.NONE);
    }
}
```

### BatchAccount_SynchronizeAutoStorageKeys

```java
/**
 * Samples for BatchAccount SynchronizeAutoStorageKeys.
 */
public final class BatchAccountSynchronizeAutoStorageKeysSamples {
    /*
     * x-ms-original-file: specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/
     * BatchAccountSynchronizeAutoStorageKeys.json
     */
    /**
     * Sample code: BatchAccountSynchronizeAutoStorageKeys.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void batchAccountSynchronizeAutoStorageKeys(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.batchAccounts()
            .synchronizeAutoStorageKeysWithResponse("default-azurebatch-japaneast", "sampleacct",
                com.azure.core.util.Context.NONE);
    }
}
```

### BatchAccount_Update

```java
import com.azure.resourcemanager.batch.models.AutoStorageBaseProperties;
import com.azure.resourcemanager.batch.models.BatchAccount;

/**
 * Samples for BatchAccount Update.
 */
public final class BatchAccountUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/BatchAccountUpdate.json
     */
    /**
     * Sample code: BatchAccountUpdate.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void batchAccountUpdate(com.azure.resourcemanager.batch.BatchManager manager) {
        BatchAccount resource = manager.batchAccounts()
            .getByResourceGroupWithResponse("default-azurebatch-japaneast", "sampleacct",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withAutoStorage(new AutoStorageBaseProperties().withStorageAccountId(
                "/subscriptions/subid/resourceGroups/default-azurebatch-japaneast/providers/Microsoft.Storage/storageAccounts/samplestorage"))
            .apply();
    }
}
```

### Certificate_CancelDeletion

```java
/**
 * Samples for Certificate CancelDeletion.
 */
public final class CertificateCancelDeletionSamples {
    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/CertificateCancelDeletion.json
     */
    /**
     * Sample code: CertificateCancelDeletion.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void certificateCancelDeletion(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.certificates()
            .cancelDeletionWithResponse("default-azurebatch-japaneast", "sampleacct",
                "sha1-0a0e4f50d51beadeac1d35afc5116098e7902e6e", com.azure.core.util.Context.NONE);
    }
}
```

### Certificate_Create

```java
import com.azure.resourcemanager.batch.models.CertificateFormat;

/**
 * Samples for Certificate Create.
 */
public final class CertificateCreateSamples {
    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/CertificateCreate_Full.json
     */
    /**
     * Sample code: CreateCertificate - Full.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void createCertificateFull(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.certificates()
            .define("sha1-0a0e4f50d51beadeac1d35afc5116098e7902e6e")
            .withExistingBatchAccount("default-azurebatch-japaneast", "sampleacct")
            .withPassword("<ExamplePassword>")
            .withThumbprintAlgorithm("sha1")
            .withThumbprint("0a0e4f50d51beadeac1d35afc5116098e7902e6e")
            .withFormat(CertificateFormat.PFX)
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/CertificateCreate_Minimal.json
     */
    /**
     * Sample code: CreateCertificate - Minimal Pfx.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void createCertificateMinimalPfx(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.certificates()
            .define("sha1-0a0e4f50d51beadeac1d35afc5116098e7902e6e")
            .withExistingBatchAccount("default-azurebatch-japaneast", "sampleacct")
            .withPassword("<ExamplePassword>")
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/CertificateCreate_MinimalCer.json
     */
    /**
     * Sample code: CreateCertificate - Minimal Cer.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void createCertificateMinimalCer(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.certificates()
            .define("sha1-0a0e4f50d51beadeac1d35afc5116098e7902e6e")
            .withExistingBatchAccount("default-azurebatch-japaneast", "sampleacct")
            .withFormat(CertificateFormat.CER)
            .create();
    }
}
```

### Certificate_Delete

```java
/**
 * Samples for Certificate Delete.
 */
public final class CertificateDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/CertificateDelete.json
     */
    /**
     * Sample code: CertificateDelete.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void certificateDelete(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.certificates()
            .delete("default-azurebatch-japaneast", "sampleacct", "sha1-0a0e4f50d51beadeac1d35afc5116098e7902e6e",
                com.azure.core.util.Context.NONE);
    }
}
```

### Certificate_Get

```java
/**
 * Samples for Certificate Get.
 */
public final class CertificateGetSamples {
    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/CertificateGetWithDeletionError.
     * json
     */
    /**
     * Sample code: Get Certificate with Deletion Error.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void getCertificateWithDeletionError(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.certificates()
            .getWithResponse("default-azurebatch-japaneast", "sampleacct",
                "sha1-0a0e4f50d51beadeac1d35afc5116098e7902e6e", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/CertificateGet.json
     */
    /**
     * Sample code: Get Certificate.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void getCertificate(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.certificates()
            .getWithResponse("default-azurebatch-japaneast", "sampleacct",
                "sha1-0a0e4f50d51beadeac1d35afc5116098e7902e6e", com.azure.core.util.Context.NONE);
    }
}
```

### Certificate_ListByBatchAccount

```java
/**
 * Samples for Certificate ListByBatchAccount.
 */
public final class CertificateListByBatchAccountSamples {
    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/CertificateListWithFilter.json
     */
    /**
     * Sample code: ListCertificates - Filter and Select.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void listCertificatesFilterAndSelect(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.certificates()
            .listByBatchAccount("default-azurebatch-japaneast", "sampleacct", null,
                "properties/format,properties/provisioningState",
                "properties/provisioningStateTransitionTime gt '2017-05-01' or properties/provisioningState eq 'Failed'",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/CertificateList.json
     */
    /**
     * Sample code: ListCertificates.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void listCertificates(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.certificates()
            .listByBatchAccount("default-azurebatch-japaneast", "sampleacct", 1, null, null,
                com.azure.core.util.Context.NONE);
    }
}
```

### Certificate_Update

```java
import com.azure.resourcemanager.batch.models.Certificate;

/**
 * Samples for Certificate Update.
 */
public final class CertificateUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/CertificateUpdate.json
     */
    /**
     * Sample code: UpdateCertificate.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void updateCertificate(com.azure.resourcemanager.batch.BatchManager manager) {
        Certificate resource = manager.certificates()
            .getWithResponse("default-azurebatch-japaneast", "sampleacct",
                "sha1-0a0e4f50d51beadeac1d35afc5116098e7902e6e", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withData("MIIJsgIBAzCCCW4GCSqGSIb3DQE...").withPassword("<ExamplePassword>").apply();
    }
}
```

### Location_CheckNameAvailability

```java
import com.azure.resourcemanager.batch.models.CheckNameAvailabilityParameters;

/**
 * Samples for Location CheckNameAvailability.
 */
public final class LocationCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/
     * LocationCheckNameAvailability_AlreadyExists.json
     */
    /**
     * Sample code: LocationCheckNameAvailability_AlreadyExists.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void
        locationCheckNameAvailabilityAlreadyExists(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.locations()
            .checkNameAvailabilityWithResponse("japaneast",
                new CheckNameAvailabilityParameters().withName("existingaccountname"),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/
     * LocationCheckNameAvailability_Available.json
     */
    /**
     * Sample code: LocationCheckNameAvailability_Available.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void locationCheckNameAvailabilityAvailable(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.locations()
            .checkNameAvailabilityWithResponse("japaneast",
                new CheckNameAvailabilityParameters().withName("newaccountname"), com.azure.core.util.Context.NONE);
    }
}
```

### Location_GetQuotas

```java
/**
 * Samples for Location GetQuotas.
 */
public final class LocationGetQuotasSamples {
    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/LocationGetQuotas.json
     */
    /**
     * Sample code: LocationGetQuotas.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void locationGetQuotas(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.locations().getQuotasWithResponse("japaneast", com.azure.core.util.Context.NONE);
    }
}
```

### Location_ListSupportedVirtualMachineSkus

```java
/**
 * Samples for Location ListSupportedVirtualMachineSkus.
 */
public final class LocationListSupportedVirtualMachineSkusSamples {
    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/LocationListVirtualMachineSkus.
     * json
     */
    /**
     * Sample code: LocationListVirtualMachineSkus.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void locationListVirtualMachineSkus(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.locations().listSupportedVirtualMachineSkus("japaneast", null, null, com.azure.core.util.Context.NONE);
    }
}
```

### NetworkSecurityPerimeter_GetConfiguration

```java
/**
 * Samples for NetworkSecurityPerimeter GetConfiguration.
 */
public final class NetworkSecurityPerimeterGetConfigurationSamples {
    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/NspConfigurationGet.json
     */
    /**
     * Sample code: GetNspConfiguration.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void getNspConfiguration(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.networkSecurityPerimeters()
            .getConfigurationWithResponse("default-azurebatch-japaneast", "sampleacct",
                "00000000-0000-0000-0000-000000000000.sampleassociation", com.azure.core.util.Context.NONE);
    }
}
```

### NetworkSecurityPerimeter_ListConfigurations

```java
/**
 * Samples for NetworkSecurityPerimeter ListConfigurations.
 */
public final class NetworkSecurityPerimeterListConfigurationsSamples {
    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/NspConfigurationsList.json
     */
    /**
     * Sample code: ListNspConfigurations.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void listNspConfigurations(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.networkSecurityPerimeters()
            .listConfigurations("default-azurebatch-japaneast", "sampleacct", com.azure.core.util.Context.NONE);
    }
}
```

### NetworkSecurityPerimeter_ReconcileConfiguration

```java
/**
 * Samples for NetworkSecurityPerimeter ReconcileConfiguration.
 */
public final class NetworkSecurityPerimeterReconcileConfigurationSamples {
    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/NspConfigurationReconcile.json
     */
    /**
     * Sample code: ReconcileNspConfiguration.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void reconcileNspConfiguration(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.networkSecurityPerimeters()
            .reconcileConfiguration("default-azurebatch-japaneast", "sampleacct",
                "00000000-0000-0000-0000-000000000000.sampleassociation", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/OperationsList.json
     */
    /**
     * Sample code: OperationsList.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void operationsList(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Pool_Create

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.batch.models.AutomaticOSUpgradePolicy;
import com.azure.resourcemanager.batch.models.AutoScaleSettings;
import com.azure.resourcemanager.batch.models.BatchPoolIdentity;
import com.azure.resourcemanager.batch.models.CachingType;
import com.azure.resourcemanager.batch.models.DataDisk;
import com.azure.resourcemanager.batch.models.DeploymentConfiguration;
import com.azure.resourcemanager.batch.models.DiffDiskPlacement;
import com.azure.resourcemanager.batch.models.DiffDiskSettings;
import com.azure.resourcemanager.batch.models.DiskEncryptionConfiguration;
import com.azure.resourcemanager.batch.models.DiskEncryptionTarget;
import com.azure.resourcemanager.batch.models.FixedScaleSettings;
import com.azure.resourcemanager.batch.models.ImageReference;
import com.azure.resourcemanager.batch.models.InboundEndpointProtocol;
import com.azure.resourcemanager.batch.models.InboundNatPool;
import com.azure.resourcemanager.batch.models.IpAddressProvisioningType;
import com.azure.resourcemanager.batch.models.ManagedDisk;
import com.azure.resourcemanager.batch.models.NetworkConfiguration;
import com.azure.resourcemanager.batch.models.NetworkSecurityGroupRule;
import com.azure.resourcemanager.batch.models.NetworkSecurityGroupRuleAccess;
import com.azure.resourcemanager.batch.models.NodeCommunicationMode;
import com.azure.resourcemanager.batch.models.NodePlacementConfiguration;
import com.azure.resourcemanager.batch.models.NodePlacementPolicyType;
import com.azure.resourcemanager.batch.models.OSDisk;
import com.azure.resourcemanager.batch.models.PoolEndpointConfiguration;
import com.azure.resourcemanager.batch.models.PoolIdentityType;
import com.azure.resourcemanager.batch.models.PublicIpAddressConfiguration;
import com.azure.resourcemanager.batch.models.RollingUpgradePolicy;
import com.azure.resourcemanager.batch.models.ScaleSettings;
import com.azure.resourcemanager.batch.models.SecurityProfile;
import com.azure.resourcemanager.batch.models.SecurityTypes;
import com.azure.resourcemanager.batch.models.ServiceArtifactReference;
import com.azure.resourcemanager.batch.models.StorageAccountType;
import com.azure.resourcemanager.batch.models.UefiSettings;
import com.azure.resourcemanager.batch.models.UpgradeMode;
import com.azure.resourcemanager.batch.models.UpgradePolicy;
import com.azure.resourcemanager.batch.models.UserAssignedIdentities;
import com.azure.resourcemanager.batch.models.VirtualMachineConfiguration;
import com.azure.resourcemanager.batch.models.VMExtension;
import com.azure.resourcemanager.batch.models.WindowsConfiguration;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Pool Create.
 */
public final class PoolCreateSamples {
    /*
     * x-ms-original-file: specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/
     * PoolCreate_VirtualMachineConfiguration_ServiceArtifactReference.json
     */
    /**
     * Sample code: CreatePool - VirtualMachineConfiguration ServiceArtifactReference.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void createPoolVirtualMachineConfigurationServiceArtifactReference(
        com.azure.resourcemanager.batch.BatchManager manager) {
        manager.pools()
            .define("testpool")
            .withExistingBatchAccount("default-azurebatch-japaneast", "sampleacct")
            .withVmSize("Standard_d4s_v3")
            .withDeploymentConfiguration(
                new DeploymentConfiguration().withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                    .withImageReference(new ImageReference().withPublisher("MicrosoftWindowsServer")
                        .withOffer("WindowsServer")
                        .withSku("2019-datacenter-smalldisk")
                        .withVersion("latest"))
                    .withNodeAgentSkuId("batch.node.windows amd64")
                    .withWindowsConfiguration(new WindowsConfiguration().withEnableAutomaticUpdates(false))
                    .withServiceArtifactReference(new ServiceArtifactReference().withId(
                        "/subscriptions/subid/resourceGroups/default-azurebatch-japaneast/providers/Microsoft.Compute/galleries/myGallery/serviceArtifacts/myServiceArtifact/vmArtifactsProfiles/vmArtifactsProfile"))))
            .withScaleSettings(new ScaleSettings()
                .withFixedScale(new FixedScaleSettings().withTargetDedicatedNodes(2).withTargetLowPriorityNodes(0)))
            .withUpgradePolicy(new UpgradePolicy().withMode(UpgradeMode.AUTOMATIC)
                .withAutomaticOSUpgradePolicy(new AutomaticOSUpgradePolicy().withEnableAutomaticOSUpgrade(true)))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/PoolCreate_SecurityProfile.json
     */
    /**
     * Sample code: CreatePool - SecurityProfile.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void createPoolSecurityProfile(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.pools()
            .define("testpool")
            .withExistingBatchAccount("default-azurebatch-japaneast", "sampleacct")
            .withVmSize("Standard_d4s_v3")
            .withDeploymentConfiguration(
                new DeploymentConfiguration().withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                    .withImageReference(new ImageReference().withPublisher("Canonical")
                        .withOffer("UbuntuServer")
                        .withSku("18_04-lts-gen2")
                        .withVersion("latest"))
                    .withNodeAgentSkuId("batch.node.ubuntu 18.04")
                    .withSecurityProfile(new SecurityProfile().withSecurityType(SecurityTypes.TRUSTED_LAUNCH)
                        .withEncryptionAtHost(true)
                        .withUefiSettings(new UefiSettings().withVTpmEnabled(false)))))
            .withScaleSettings(new ScaleSettings()
                .withFixedScale(new FixedScaleSettings().withTargetDedicatedNodes(1).withTargetLowPriorityNodes(0)))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/PoolCreate_Tags.json
     */
    /**
     * Sample code: CreatePool - Tags.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void createPoolTags(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.pools()
            .define("testpool")
            .withExistingBatchAccount("default-azurebatch-japaneast", "sampleacct")
            .withTags(mapOf("TagName1", "TagValue1", "TagName2", "TagValue2"))
            .withVmSize("Standard_d4s_v3")
            .withDeploymentConfiguration(new DeploymentConfiguration().withVirtualMachineConfiguration(
                new VirtualMachineConfiguration().withImageReference(new ImageReference().withPublisher("Canonical")
                    .withOffer("0001-com-ubuntu-server-jammy")
                    .withSku("22_04-lts")
                    .withVersion("latest")).withNodeAgentSkuId("batch.node.ubuntu 22.04")))
            .withScaleSettings(new ScaleSettings()
                .withFixedScale(new FixedScaleSettings().withTargetDedicatedNodes(1).withTargetLowPriorityNodes(0)))
            .create();
    }

    /*
     * x-ms-original-file: specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/
     * PoolCreate_VirtualMachineConfiguration_ManagedOSDisk.json
     */
    /**
     * Sample code: CreatePool - VirtualMachineConfiguration OSDisk.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void
        createPoolVirtualMachineConfigurationOSDisk(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.pools()
            .define("testpool")
            .withExistingBatchAccount("default-azurebatch-japaneast", "sampleacct")
            .withVmSize("Standard_d2s_v3")
            .withDeploymentConfiguration(
                new DeploymentConfiguration().withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                    .withImageReference(new ImageReference().withPublisher("microsoftwindowsserver")
                        .withOffer("windowsserver")
                        .withSku("2022-datacenter-smalldisk"))
                    .withNodeAgentSkuId("batch.node.windows amd64")
                    .withOsDisk(new OSDisk().withCaching(CachingType.READ_WRITE)
                        .withManagedDisk(new ManagedDisk().withStorageAccountType(StorageAccountType.STANDARD_SSD_LRS))
                        .withDiskSizeGB(100)
                        .withWriteAcceleratorEnabled(false))))
            .withScaleSettings(new ScaleSettings()
                .withFixedScale(new FixedScaleSettings().withTargetDedicatedNodes(1).withTargetLowPriorityNodes(0)))
            .create();
    }

    /*
     * x-ms-original-file: specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/
     * PoolCreate_MinimalVirtualMachineConfiguration.json
     */
    /**
     * Sample code: CreatePool - Minimal VirtualMachineConfiguration.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void
        createPoolMinimalVirtualMachineConfiguration(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.pools()
            .define("testpool")
            .withExistingBatchAccount("default-azurebatch-japaneast", "sampleacct")
            .withVmSize("STANDARD_D4")
            .withDeploymentConfiguration(new DeploymentConfiguration().withVirtualMachineConfiguration(
                new VirtualMachineConfiguration().withImageReference(new ImageReference().withPublisher("Canonical")
                    .withOffer("UbuntuServer")
                    .withSku("18.04-LTS")
                    .withVersion("latest")).withNodeAgentSkuId("batch.node.ubuntu 18.04")))
            .withScaleSettings(
                new ScaleSettings().withAutoScale(new AutoScaleSettings().withFormula("$TargetDedicatedNodes=1")
                    .withEvaluationInterval(Duration.parse("PT5M"))))
            .create();
    }

    /*
     * x-ms-original-file: specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/
     * PoolCreate_VirtualMachineConfiguration_Extensions.json
     */
    /**
     * Sample code: CreatePool - VirtualMachineConfiguration Extensions.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void createPoolVirtualMachineConfigurationExtensions(
        com.azure.resourcemanager.batch.BatchManager manager) throws IOException {
        manager.pools()
            .define("testpool")
            .withExistingBatchAccount("default-azurebatch-japaneast", "sampleacct")
            .withVmSize("STANDARD_D4")
            .withDeploymentConfiguration(
                new DeploymentConfiguration().withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                    .withImageReference(new ImageReference().withPublisher("Canonical")
                        .withOffer("0001-com-ubuntu-server-focal")
                        .withSku("20_04-lts"))
                    .withNodeAgentSkuId("batch.node.ubuntu 20.04")
                    .withExtensions(Arrays.asList(new VMExtension().withName("batchextension1")
                        .withPublisher("Microsoft.Azure.KeyVault")
                        .withType("KeyVaultForLinux")
                        .withTypeHandlerVersion("2.0")
                        .withAutoUpgradeMinorVersion(true)
                        .withEnableAutomaticUpgrade(true)
                        .withSettings(SerializerFactory.createDefaultManagementSerializerAdapter()
                            .deserialize(
                                "{\"authenticationSettingsKey\":\"authenticationSettingsValue\",\"secretsManagementSettingsKey\":\"secretsManagementSettingsValue\"}",
                                Object.class, SerializerEncoding.JSON))))))
            .withScaleSettings(
                new ScaleSettings().withAutoScale(new AutoScaleSettings().withFormula("$TargetDedicatedNodes=1")
                    .withEvaluationInterval(Duration.parse("PT5M"))))
            .withTargetNodeCommunicationMode(NodeCommunicationMode.DEFAULT)
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/PoolCreate_UserAssignedIdentities
     * .json
     */
    /**
     * Sample code: CreatePool - UserAssignedIdentities.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void createPoolUserAssignedIdentities(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.pools()
            .define("testpool")
            .withExistingBatchAccount("default-azurebatch-japaneast", "sampleacct")
            .withIdentity(new BatchPoolIdentity().withType(PoolIdentityType.USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/subid/resourceGroups/default-azurebatch-japaneast/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id1",
                    new UserAssignedIdentities(),
                    "/subscriptions/subid/resourceGroups/default-azurebatch-japaneast/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id2",
                    new UserAssignedIdentities())))
            .withVmSize("STANDARD_D4")
            .withDeploymentConfiguration(new DeploymentConfiguration().withVirtualMachineConfiguration(
                new VirtualMachineConfiguration().withImageReference(new ImageReference().withPublisher("Canonical")
                    .withOffer("UbuntuServer")
                    .withSku("18.04-LTS")
                    .withVersion("latest")).withNodeAgentSkuId("batch.node.ubuntu 18.04")))
            .withScaleSettings(
                new ScaleSettings().withAutoScale(new AutoScaleSettings().withFormula("$TargetDedicatedNodes=1")
                    .withEvaluationInterval(Duration.parse("PT5M"))))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/PoolCreate_UpgradePolicy.json
     */
    /**
     * Sample code: CreatePool - UpgradePolicy.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void createPoolUpgradePolicy(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.pools()
            .define("testpool")
            .withExistingBatchAccount("default-azurebatch-japaneast", "sampleacct")
            .withVmSize("Standard_d4s_v3")
            .withDeploymentConfiguration(
                new DeploymentConfiguration().withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                    .withImageReference(new ImageReference().withPublisher("MicrosoftWindowsServer")
                        .withOffer("WindowsServer")
                        .withSku("2019-datacenter-smalldisk")
                        .withVersion("latest"))
                    .withNodeAgentSkuId("batch.node.windows amd64")
                    .withWindowsConfiguration(new WindowsConfiguration().withEnableAutomaticUpdates(false))
                    .withNodePlacementConfiguration(
                        new NodePlacementConfiguration().withPolicy(NodePlacementPolicyType.ZONAL))))
            .withScaleSettings(new ScaleSettings()
                .withFixedScale(new FixedScaleSettings().withTargetDedicatedNodes(2).withTargetLowPriorityNodes(0)))
            .withUpgradePolicy(new UpgradePolicy().withMode(UpgradeMode.AUTOMATIC)
                .withAutomaticOSUpgradePolicy(new AutomaticOSUpgradePolicy().withDisableAutomaticRollback(true)
                    .withEnableAutomaticOSUpgrade(true)
                    .withUseRollingUpgradePolicy(true)
                    .withOsRollingUpgradeDeferral(true))
                .withRollingUpgradePolicy(new RollingUpgradePolicy().withEnableCrossZoneUpgrade(true)
                    .withMaxBatchInstancePercent(20)
                    .withMaxUnhealthyInstancePercent(20)
                    .withMaxUnhealthyUpgradedInstancePercent(20)
                    .withPauseTimeBetweenBatches("PT0S")
                    .withPrioritizeUnhealthyInstances(false)
                    .withRollbackFailedInstancesOnPolicyBreach(false)))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/PoolCreate_AcceleratedNetworking.
     * json
     */
    /**
     * Sample code: CreatePool - accelerated networking.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void createPoolAcceleratedNetworking(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.pools()
            .define("testpool")
            .withExistingBatchAccount("default-azurebatch-japaneast", "sampleacct")
            .withVmSize("STANDARD_D1_V2")
            .withDeploymentConfiguration(
                new DeploymentConfiguration().withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                    .withImageReference(new ImageReference().withPublisher("MicrosoftWindowsServer")
                        .withOffer("WindowsServer")
                        .withSku("2016-datacenter-smalldisk")
                        .withVersion("latest"))
                    .withNodeAgentSkuId("batch.node.windows amd64")))
            .withScaleSettings(new ScaleSettings()
                .withFixedScale(new FixedScaleSettings().withTargetDedicatedNodes(1).withTargetLowPriorityNodes(0)))
            .withNetworkConfiguration(new NetworkConfiguration().withSubnetId(
                "/subscriptions/subid/resourceGroups/rg1234/providers/Microsoft.Network/virtualNetworks/network1234/subnets/subnet123")
                .withEnableAcceleratedNetworking(true))
            .create();
    }

    /*
     * x-ms-original-file: specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/
     * PoolCreate_VirtualMachineConfiguration.json
     */
    /**
     * Sample code: CreatePool - Full VirtualMachineConfiguration.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void createPoolFullVirtualMachineConfiguration(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.pools()
            .define("testpool")
            .withExistingBatchAccount("default-azurebatch-japaneast", "sampleacct")
            .withVmSize("STANDARD_D4")
            .withDeploymentConfiguration(
                new DeploymentConfiguration().withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                    .withImageReference(new ImageReference().withPublisher("MicrosoftWindowsServer")
                        .withOffer("WindowsServer")
                        .withSku("2016-Datacenter-SmallDisk")
                        .withVersion("latest"))
                    .withNodeAgentSkuId("batch.node.windows amd64")
                    .withWindowsConfiguration(new WindowsConfiguration().withEnableAutomaticUpdates(false))
                    .withDataDisks(Arrays.asList(
                        new DataDisk().withLun(0)
                            .withCaching(CachingType.READ_WRITE)
                            .withDiskSizeGB(30)
                            .withStorageAccountType(StorageAccountType.PREMIUM_LRS),
                        new DataDisk().withLun(1)
                            .withCaching(CachingType.NONE)
                            .withDiskSizeGB(200)
                            .withStorageAccountType(StorageAccountType.STANDARD_LRS)))
                    .withLicenseType("Windows_Server")
                    .withDiskEncryptionConfiguration(new DiskEncryptionConfiguration()
                        .withTargets(Arrays.asList(DiskEncryptionTarget.OS_DISK, DiskEncryptionTarget.TEMPORARY_DISK)))
                    .withNodePlacementConfiguration(
                        new NodePlacementConfiguration().withPolicy(NodePlacementPolicyType.ZONAL))
                    .withOsDisk(new OSDisk().withEphemeralOSDiskSettings(
                        new DiffDiskSettings().withPlacement(DiffDiskPlacement.CACHE_DISK)))))
            .withScaleSettings(
                new ScaleSettings().withAutoScale(new AutoScaleSettings().withFormula("$TargetDedicatedNodes=1")
                    .withEvaluationInterval(Duration.parse("PT5M"))))
            .withNetworkConfiguration(
                new NetworkConfiguration().withEndpointConfiguration(new PoolEndpointConfiguration()
                    .withInboundNatPools(Arrays.asList(new InboundNatPool().withName("testnat")
                        .withProtocol(InboundEndpointProtocol.TCP)
                        .withBackendPort(12001)
                        .withFrontendPortRangeStart(15000)
                        .withFrontendPortRangeEnd(15100)
                        .withNetworkSecurityGroupRules(Arrays.asList(
                            new NetworkSecurityGroupRule().withPriority(150)
                                .withAccess(NetworkSecurityGroupRuleAccess.ALLOW)
                                .withSourceAddressPrefix("192.100.12.45")
                                .withSourcePortRanges(Arrays.asList("1", "2")),
                            new NetworkSecurityGroupRule().withPriority(3500)
                                .withAccess(NetworkSecurityGroupRuleAccess.DENY)
                                .withSourceAddressPrefix("*")
                                .withSourcePortRanges(Arrays.asList("*"))))))))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/PoolCreate_SharedImageGallery.
     * json
     */
    /**
     * Sample code: CreatePool - Custom Image.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void createPoolCustomImage(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.pools()
            .define("testpool")
            .withExistingBatchAccount("default-azurebatch-japaneast", "sampleacct")
            .withVmSize("STANDARD_D4")
            .withDeploymentConfiguration(new DeploymentConfiguration().withVirtualMachineConfiguration(
                new VirtualMachineConfiguration().withImageReference(new ImageReference().withId(
                    "/subscriptions/subid/resourceGroups/networking-group/providers/Microsoft.Compute/galleries/testgallery/images/testimagedef/versions/0.0.1"))
                    .withNodeAgentSkuId("batch.node.ubuntu 18.04")))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/PoolCreate_NoPublicIPAddresses.
     * json
     */
    /**
     * Sample code: CreatePool - No public IP.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void createPoolNoPublicIP(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.pools()
            .define("testpool")
            .withExistingBatchAccount("default-azurebatch-japaneast", "sampleacct")
            .withVmSize("STANDARD_D4")
            .withDeploymentConfiguration(new DeploymentConfiguration().withVirtualMachineConfiguration(
                new VirtualMachineConfiguration().withImageReference(new ImageReference().withId(
                    "/subscriptions/subid/resourceGroups/networking-group/providers/Microsoft.Compute/galleries/testgallery/images/testimagedef/versions/0.0.1"))
                    .withNodeAgentSkuId("batch.node.ubuntu 18.04")))
            .withNetworkConfiguration(new NetworkConfiguration().withSubnetId(
                "/subscriptions/subid/resourceGroups/rg1234/providers/Microsoft.Network/virtualNetworks/network1234/subnets/subnet123")
                .withPublicIpAddressConfiguration(
                    new PublicIpAddressConfiguration().withProvision(IpAddressProvisioningType.NO_PUBLIC_IPADDRESSES)))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/PoolCreate_ResourceTags.json
     */
    /**
     * Sample code: CreatePool - ResourceTags.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void createPoolResourceTags(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.pools()
            .define("testpool")
            .withExistingBatchAccount("default-azurebatch-japaneast", "sampleacct")
            .withVmSize("Standard_d4s_v3")
            .withDeploymentConfiguration(new DeploymentConfiguration().withVirtualMachineConfiguration(
                new VirtualMachineConfiguration().withImageReference(new ImageReference().withPublisher("Canonical")
                    .withOffer("UbuntuServer")
                    .withSku("18_04-lts-gen2")
                    .withVersion("latest")).withNodeAgentSkuId("batch.node.ubuntu 18.04")))
            .withScaleSettings(new ScaleSettings()
                .withFixedScale(new FixedScaleSettings().withTargetDedicatedNodes(1).withTargetLowPriorityNodes(0)))
            .withResourceTags(mapOf("TagName1", "TagValue1", "TagName2", "TagValue2"))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/PoolCreate_PublicIPs.json
     */
    /**
     * Sample code: CreatePool - Public IPs.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void createPoolPublicIPs(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.pools()
            .define("testpool")
            .withExistingBatchAccount("default-azurebatch-japaneast", "sampleacct")
            .withVmSize("STANDARD_D4")
            .withDeploymentConfiguration(new DeploymentConfiguration().withVirtualMachineConfiguration(
                new VirtualMachineConfiguration().withImageReference(new ImageReference().withId(
                    "/subscriptions/subid/resourceGroups/networking-group/providers/Microsoft.Compute/galleries/testgallery/images/testimagedef/versions/0.0.1"))
                    .withNodeAgentSkuId("batch.node.ubuntu 18.04")))
            .withNetworkConfiguration(new NetworkConfiguration().withSubnetId(
                "/subscriptions/subid/resourceGroups/rg1234/providers/Microsoft.Network/virtualNetworks/network1234/subnets/subnet123")
                .withPublicIpAddressConfiguration(new PublicIpAddressConfiguration()
                    .withProvision(IpAddressProvisioningType.USER_MANAGED)
                    .withIpAddressIds(Arrays.asList(
                        "/subscriptions/subid1/resourceGroups/rg13/providers/Microsoft.Network/publicIPAddresses/ip135"))))
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

### Pool_Delete

```java
/**
 * Samples for Pool Delete.
 */
public final class PoolDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/PoolDelete.json
     */
    /**
     * Sample code: DeletePool.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void deletePool(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.pools()
            .delete("default-azurebatch-japaneast", "sampleacct", "testpool", com.azure.core.util.Context.NONE);
    }
}
```

### Pool_DisableAutoScale

```java
/**
 * Samples for Pool DisableAutoScale.
 */
public final class PoolDisableAutoScaleSamples {
    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/PoolDisableAutoScale.json
     */
    /**
     * Sample code: Disable AutoScale.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void disableAutoScale(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.pools()
            .disableAutoScaleWithResponse("default-azurebatch-japaneast", "sampleacct", "testpool",
                com.azure.core.util.Context.NONE);
    }
}
```

### Pool_Get

```java
/**
 * Samples for Pool Get.
 */
public final class PoolGetSamples {
    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/PoolGet_SecurityProfile.json
     */
    /**
     * Sample code: GetPool - SecurityProfile.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void getPoolSecurityProfile(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.pools()
            .getWithResponse("default-azurebatch-japaneast", "sampleacct", "testpool",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/
     * PoolGet_VirtualMachineConfiguration_Extensions.json
     */
    /**
     * Sample code: GetPool - VirtualMachineConfiguration Extensions.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void
        getPoolVirtualMachineConfigurationExtensions(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.pools()
            .getWithResponse("default-azurebatch-japaneast", "sampleacct", "testpool",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/
     * PoolGet_VirtualMachineConfiguration_MangedOSDisk.json
     */
    /**
     * Sample code: GetPool - VirtualMachineConfiguration OSDisk.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void getPoolVirtualMachineConfigurationOSDisk(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.pools()
            .getWithResponse("default-azurebatch-japaneast", "sampleacct", "testpool",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/PoolGet_UpgradePolicy.json
     */
    /**
     * Sample code: GetPool - UpgradePolicy.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void getPoolUpgradePolicy(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.pools()
            .getWithResponse("default-azurebatch-japaneast", "sampleacct", "testpool",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/
     * PoolGet_VirtualMachineConfiguration_ServiceArtifactReference.json
     */
    /**
     * Sample code: GetPool - VirtualMachineConfiguration ServiceArtifactReference.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void getPoolVirtualMachineConfigurationServiceArtifactReference(
        com.azure.resourcemanager.batch.BatchManager manager) {
        manager.pools()
            .getWithResponse("default-azurebatch-japaneast", "sampleacct", "testpool",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/PoolGet_AcceleratedNetworking.
     * json
     */
    /**
     * Sample code: GetPool - AcceleratedNetworking.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void getPoolAcceleratedNetworking(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.pools()
            .getWithResponse("default-azurebatch-japaneast", "sampleacct", "testpool",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/PoolGet.json
     */
    /**
     * Sample code: GetPool.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void getPool(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.pools()
            .getWithResponse("default-azurebatch-japaneast", "sampleacct", "testpool",
                com.azure.core.util.Context.NONE);
    }
}
```

### Pool_ListByBatchAccount

```java
/**
 * Samples for Pool ListByBatchAccount.
 */
public final class PoolListByBatchAccountSamples {
    /*
     * x-ms-original-file: specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/PoolList.json
     */
    /**
     * Sample code: ListPool.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void listPool(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.pools()
            .listByBatchAccount("default-azurebatch-japaneast", "sampleacct", null, null, null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/PoolListWithFilter.json
     */
    /**
     * Sample code: ListPoolWithFilter.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void listPoolWithFilter(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.pools()
            .listByBatchAccount("default-azurebatch-japaneast", "sampleacct", 50,
                "properties/allocationState,properties/provisioningStateTransitionTime,properties/currentDedicatedNodes,properties/currentLowPriorityNodes",
                "startswith(name, 'po') or (properties/allocationState eq 'Steady' and properties/provisioningStateTransitionTime lt datetime'2017-02-02')",
                com.azure.core.util.Context.NONE);
    }
}
```

### Pool_StopResize

```java
/**
 * Samples for Pool StopResize.
 */
public final class PoolStopResizeSamples {
    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/PoolStopResize.json
     */
    /**
     * Sample code: StopPoolResize.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void stopPoolResize(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.pools()
            .stopResizeWithResponse("default-azurebatch-japaneast", "sampleacct", "testpool",
                com.azure.core.util.Context.NONE);
    }
}
```

### Pool_Update

```java
import com.azure.resourcemanager.batch.models.ApplicationPackageReference;
import com.azure.resourcemanager.batch.models.AutoScaleSettings;
import com.azure.resourcemanager.batch.models.CertificateReference;
import com.azure.resourcemanager.batch.models.CertificateStoreLocation;
import com.azure.resourcemanager.batch.models.ComputeNodeDeallocationOption;
import com.azure.resourcemanager.batch.models.FixedScaleSettings;
import com.azure.resourcemanager.batch.models.MetadataItem;
import com.azure.resourcemanager.batch.models.NodeCommunicationMode;
import com.azure.resourcemanager.batch.models.Pool;
import com.azure.resourcemanager.batch.models.ScaleSettings;
import com.azure.resourcemanager.batch.models.StartTask;
import java.time.Duration;
import java.util.Arrays;

/**
 * Samples for Pool Update.
 */
public final class PoolUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/PoolUpdate_EnableAutoScale.json
     */
    /**
     * Sample code: UpdatePool - Enable Autoscale.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void updatePoolEnableAutoscale(com.azure.resourcemanager.batch.BatchManager manager) {
        Pool resource = manager.pools()
            .getWithResponse("default-azurebatch-japaneast", "sampleacct", "testpool", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withScaleSettings(
                new ScaleSettings().withAutoScale(new AutoScaleSettings().withFormula("$TargetDedicatedNodes=34")))
            .apply();
    }

    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/PoolUpdate_RemoveStartTask.json
     */
    /**
     * Sample code: UpdatePool - Remove Start Task.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void updatePoolRemoveStartTask(com.azure.resourcemanager.batch.BatchManager manager) {
        Pool resource = manager.pools()
            .getWithResponse("default-azurebatch-japaneast", "sampleacct", "testpool", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withStartTask(new StartTask()).apply();
    }

    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/PoolUpdate_ResizePool.json
     */
    /**
     * Sample code: UpdatePool - Resize Pool.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void updatePoolResizePool(com.azure.resourcemanager.batch.BatchManager manager) {
        Pool resource = manager.pools()
            .getWithResponse("default-azurebatch-japaneast", "sampleacct", "testpool", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withScaleSettings(
                new ScaleSettings().withFixedScale(new FixedScaleSettings().withResizeTimeout(Duration.parse("PT8M"))
                    .withTargetDedicatedNodes(5)
                    .withTargetLowPriorityNodes(0)
                    .withNodeDeallocationOption(ComputeNodeDeallocationOption.TASK_COMPLETION)))
            .apply();
    }

    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/PoolUpdate_OtherProperties.json
     */
    /**
     * Sample code: UpdatePool - Other Properties.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void updatePoolOtherProperties(com.azure.resourcemanager.batch.BatchManager manager) {
        Pool resource = manager.pools()
            .getWithResponse("default-azurebatch-japaneast", "sampleacct", "testpool", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withMetadata(Arrays.asList(new MetadataItem().withName("key1").withValue("value1")))
            .withCertificates(Arrays.asList(new CertificateReference().withId(
                "/subscriptions/subid/resourceGroups/default-azurebatch-japaneast/providers/Microsoft.Batch/batchAccounts/sampleacct/pools/testpool/certificates/sha1-1234567")
                .withStoreLocation(CertificateStoreLocation.LOCAL_MACHINE)
                .withStoreName("MY")))
            .withApplicationPackages(Arrays.asList(new ApplicationPackageReference().withId(
                "/subscriptions/subid/resourceGroups/default-azurebatch-japaneast/providers/Microsoft.Batch/batchAccounts/sampleacct/pools/testpool/applications/app_1234"),
                new ApplicationPackageReference().withId(
                    "/subscriptions/subid/resourceGroups/default-azurebatch-japaneast/providers/Microsoft.Batch/batchAccounts/sampleacct/pools/testpool/applications/app_5678")
                    .withVersion("1.0")))
            .withTargetNodeCommunicationMode(NodeCommunicationMode.SIMPLIFIED)
            .apply();
    }
}
```

### PrivateEndpointConnection_Delete

```java
/**
 * Samples for PrivateEndpointConnection Delete.
 */
public final class PrivateEndpointConnectionDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/PrivateEndpointConnectionDelete.
     * json
     */
    /**
     * Sample code: PrivateEndpointConnectionDelete.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void privateEndpointConnectionDelete(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.privateEndpointConnections()
            .delete("default-azurebatch-japaneast", "sampleacct",
                "testprivateEndpointConnection5testprivateEndpointConnection5.24d6b4b5-e65c-4330-bbe9-3a290d62f8e0",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnection_Get

```java
/**
 * Samples for PrivateEndpointConnection Get.
 */
public final class PrivateEndpointConnectionGetSamples {
    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/PrivateEndpointConnectionGet.json
     */
    /**
     * Sample code: GetPrivateEndpointConnection.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void getPrivateEndpointConnection(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.privateEndpointConnections()
            .getWithResponse("default-azurebatch-japaneast", "sampleacct",
                "testprivateEndpointConnection5testprivateEndpointConnection5.24d6b4b5-e65c-4330-bbe9-3a290d62f8e0",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnection_ListByBatchAccount

```java
/**
 * Samples for PrivateEndpointConnection ListByBatchAccount.
 */
public final class PrivateEndpointConnectionListByBatchAccountSamples {
    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/PrivateEndpointConnectionsList.
     * json
     */
    /**
     * Sample code: ListPrivateEndpointConnections.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void listPrivateEndpointConnections(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.privateEndpointConnections()
            .listByBatchAccount("default-azurebatch-japaneast", "sampleacct", null, com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnection_Update

```java
import com.azure.resourcemanager.batch.fluent.models.PrivateEndpointConnectionInner;
import com.azure.resourcemanager.batch.models.PrivateLinkServiceConnectionState;
import com.azure.resourcemanager.batch.models.PrivateLinkServiceConnectionStatus;

/**
 * Samples for PrivateEndpointConnection Update.
 */
public final class PrivateEndpointConnectionUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/PrivateEndpointConnectionUpdate.
     * json
     */
    /**
     * Sample code: UpdatePrivateEndpointConnection.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void updatePrivateEndpointConnection(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.privateEndpointConnections()
            .update("default-azurebatch-japaneast", "sampleacct",
                "testprivateEndpointConnection5.24d6b4b5-e65c-4330-bbe9-3a290d62f8e0",
                new PrivateEndpointConnectionInner().withPrivateLinkServiceConnectionState(
                    new PrivateLinkServiceConnectionState().withStatus(PrivateLinkServiceConnectionStatus.APPROVED)
                        .withDescription("Approved by xyz.abc@company.com")),
                null, com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkResource_Get

```java
/**
 * Samples for PrivateLinkResource Get.
 */
public final class PrivateLinkResourceGetSamples {
    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/PrivateLinkResourceGet.json
     */
    /**
     * Sample code: GetPrivateLinkResource.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void getPrivateLinkResource(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.privateLinkResources()
            .getWithResponse("default-azurebatch-japaneast", "sampleacct", "batchAccount",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkResource_ListByBatchAccount

```java
/**
 * Samples for PrivateLinkResource ListByBatchAccount.
 */
public final class PrivateLinkResourceListByBatchAccountSamples {
    /*
     * x-ms-original-file:
     * specification/batch/resource-manager/Microsoft.Batch/stable/2024-07-01/examples/PrivateLinkResourcesList.json
     */
    /**
     * Sample code: ListPrivateLinkResource.
     * 
     * @param manager Entry point to BatchManager.
     */
    public static void listPrivateLinkResource(com.azure.resourcemanager.batch.BatchManager manager) {
        manager.privateLinkResources()
            .listByBatchAccount("default-azurebatch-japaneast", "sampleacct", null, com.azure.core.util.Context.NONE);
    }
}
```

