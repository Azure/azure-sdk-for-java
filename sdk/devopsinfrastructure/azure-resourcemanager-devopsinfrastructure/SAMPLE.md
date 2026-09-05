# Code snippets and samples


## ImageVersions

- [ListByImage](#imageversions_listbyimage)

## Operations

- [List](#operations_list)

## Pools

- [CheckNameAvailability](#pools_checknameavailability)
- [CreateOrUpdate](#pools_createorupdate)
- [Delete](#pools_delete)
- [DeleteResources](#pools_deleteresources)
- [GetByResourceGroup](#pools_getbyresourcegroup)
- [List](#pools_list)
- [ListByResourceGroup](#pools_listbyresourcegroup)
- [Update](#pools_update)

## ResourceDetails

- [ListByPool](#resourcedetails_listbypool)

## Sku

- [ListByLocation](#sku_listbylocation)

## SubscriptionUsages

- [Usages](#subscriptionusages_usages)
### ImageVersions_ListByImage

```java
/**
 * Samples for ImageVersions ListByImage.
 */
public final class ImageVersionsListByImageSamples {
    /*
     * x-ms-original-file: 2026-07-03-preview/ImageVersions_ListByImage.json
     */
    /**
     * Sample code: ImageVersions_ListByImage.
     * 
     * @param manager Entry point to DevOpsInfrastructureManager.
     */
    public static void
        imageVersionsListByImage(com.azure.resourcemanager.devopsinfrastructure.DevOpsInfrastructureManager manager) {
        manager.imageVersions().listByImage("my-resource-group", "windows-2022", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-07-03-preview/ListOperations.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to DevOpsInfrastructureManager.
     */
    public static void
        operationsList(com.azure.resourcemanager.devopsinfrastructure.DevOpsInfrastructureManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Pools_CheckNameAvailability

```java
import com.azure.resourcemanager.devopsinfrastructure.models.CheckNameAvailability;
import com.azure.resourcemanager.devopsinfrastructure.models.DevOpsInfrastructureResourceType;

/**
 * Samples for Pools CheckNameAvailability.
 */
public final class PoolsCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: 2026-07-03-preview/Pools_CheckNameAvailability.json
     */
    /**
     * Sample code: Pools_CheckNameAvailability.
     * 
     * @param manager Entry point to DevOpsInfrastructureManager.
     */
    public static void
        poolsCheckNameAvailability(com.azure.resourcemanager.devopsinfrastructure.DevOpsInfrastructureManager manager) {
        manager.pools()
            .checkNameAvailabilityWithResponse(
                new CheckNameAvailability().withName("mydevopspool")
                    .withType(DevOpsInfrastructureResourceType.MICROSOFT_DEV_OPS_INFRASTRUCTURE_POOLS),
                com.azure.core.util.Context.NONE);
    }
}
```

### Pools_CreateOrUpdate

```java
import com.azure.resourcemanager.devopsinfrastructure.models.AzureDevOpsOrganizationProfile;
import com.azure.resourcemanager.devopsinfrastructure.models.CertificateStoreNameOption;
import com.azure.resourcemanager.devopsinfrastructure.models.DevOpsAzureSku;
import com.azure.resourcemanager.devopsinfrastructure.models.EphemeralType;
import com.azure.resourcemanager.devopsinfrastructure.models.NetworkProfile;
import com.azure.resourcemanager.devopsinfrastructure.models.Organization;
import com.azure.resourcemanager.devopsinfrastructure.models.OsProfile;
import com.azure.resourcemanager.devopsinfrastructure.models.PoolImage;
import com.azure.resourcemanager.devopsinfrastructure.models.PoolProperties;
import com.azure.resourcemanager.devopsinfrastructure.models.ProvisioningState;
import com.azure.resourcemanager.devopsinfrastructure.models.SecretsManagementSettings;
import com.azure.resourcemanager.devopsinfrastructure.models.StatelessAgentProfile;
import com.azure.resourcemanager.devopsinfrastructure.models.VmSize;
import com.azure.resourcemanager.devopsinfrastructure.models.VmssFabricProfile;
import java.util.Arrays;

/**
 * Samples for Pools CreateOrUpdate.
 */
public final class PoolsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-07-03-preview/CreateOrUpdatePool_InstanceMix.json
     */
    /**
     * Sample code: Pools_CreateOrUpdate_InstanceMix.
     * 
     * @param manager Entry point to DevOpsInfrastructureManager.
     */
    public static void poolsCreateOrUpdateInstanceMix(
        com.azure.resourcemanager.devopsinfrastructure.DevOpsInfrastructureManager manager) {
        manager.pools()
            .define("pool")
            .withRegion("eastus")
            .withExistingResourceGroup("rg")
            .withProperties(new PoolProperties().withProvisioningState(ProvisioningState.SUCCEEDED)
                .withMaximumConcurrency(10)
                .withOrganizationProfile(
                    new AzureDevOpsOrganizationProfile().withDescription("Managed by Managed DevOps Pools")
                        .withUpdateDescription(true)
                        .withOrganizations(Arrays
                            .asList(new Organization().withUrl("https://mseng.visualstudio.com").withOpenAccess(true))))
                .withAgentProfile(new StatelessAgentProfile())
                .withFabricProfile(new VmssFabricProfile()
                    .withSku(new DevOpsAzureSku().withName("Mix")
                        .withVmSizes(Arrays.asList(new VmSize().withName("Standard_E2ads_v5"),
                            new VmSize().withName("Standard_D2ads_v5"))))
                    .withImages(Arrays.asList(new PoolImage()
                        .withResourceId("/MicrosoftWindowsServer/WindowsServer/2019-Datacenter/latest")
                        .withEphemeralType(EphemeralType.AUTOMATIC)
                        .withProvisioningScriptStorageAccountResourceId(
                            "/subscriptions/a2e95d27-c161-4b61-bda4-11512c14c2c2/resourceGroups/rg/providers/Microsoft.Storage/storageAccounts/provisioningscriptsa")
                        .withProvisioningScriptManagedIdentityClientId("0f8fad5b-d9cb-469f-a165-70867728950e")
                        .withProvisioningScriptShouldRestart(true)
                        .withProvisioningScriptEntryPoint("scripts/setup-agent.ps1")))
                    .withOsProfile(new OsProfile().withSecretsManagementSettings(
                        new SecretsManagementSettings().withCertificateStoreName(CertificateStoreNameOption.ROOT)
                            .withObservedCertificates(Arrays.asList("https://abc.vault.azure.net/secrets/one"))
                            .withKeyExportable(false)))
                    .withNetworkProfile(new NetworkProfile().withSubnetId(
                        "/subscriptions/a2e95d27-c161-4b61-bda4-11512c14c2c2/resourceGroups/rg/providers/Microsoft.Network/virtualNetworks/vnet/subnets/subnet")
                        .withStaticIpAddressCount(2)))
                .withDevCenterProjectResourceId(
                    "/subscriptions/222e81d0-cf38-4dab-baa5-289bf16baaa4/resourceGroups/rg-1es-devcenter/providers/Microsoft.DevCenter/projects/1ES"))
            .create();
    }

    /*
     * x-ms-original-file: 2026-07-03-preview/CreateOrUpdatePool.json
     */
    /**
     * Sample code: Pools_CreateOrUpdate.
     * 
     * @param manager Entry point to DevOpsInfrastructureManager.
     */
    public static void
        poolsCreateOrUpdate(com.azure.resourcemanager.devopsinfrastructure.DevOpsInfrastructureManager manager) {
        manager.pools()
            .define("pool")
            .withRegion("eastus")
            .withExistingResourceGroup("rg")
            .withProperties(
                new PoolProperties().withProvisioningState(ProvisioningState.SUCCEEDED)
                    .withMaximumConcurrency(10)
                    .withOrganizationProfile(new AzureDevOpsOrganizationProfile()
                        .withDescription("Managed by Managed DevOps Pools")
                        .withUpdateDescription(true)
                        .withOrganizations(Arrays.asList(new Organization()
                            .withUrl("https://mseng.visualstudio.com")
                            .withOpenAccess(true))))
                    .withAgentProfile(new StatelessAgentProfile())
                    .withFabricProfile(new VmssFabricProfile()
                        .withSku(new DevOpsAzureSku().withName("Standard_D4ads_v5"))
                        .withImages(Arrays.asList(new PoolImage()
                            .withResourceId("/MicrosoftWindowsServer/WindowsServer/2019-Datacenter/latest")
                            .withEphemeralType(EphemeralType.NVME_DISK)
                            .withProvisioningScriptStorageAccountResourceId(
                                "/subscriptions/a2e95d27-c161-4b61-bda4-11512c14c2c2/resourceGroups/rg/providers/Microsoft.Storage/storageAccounts/provisioningscriptsa")
                            .withProvisioningScriptManagedIdentityClientId("0f8fad5b-d9cb-469f-a165-70867728950e")
                            .withProvisioningScriptShouldRestart(true)
                            .withProvisioningScriptEntryPoint("scripts/setup-agent.ps1")))
                        .withOsProfile(
                            new OsProfile().withSecretsManagementSettings(new SecretsManagementSettings()
                                .withCertificateStoreName(CertificateStoreNameOption.ROOT)
                                .withObservedCertificates(Arrays.asList("https://abc.vault.azure.net/secrets/one"))
                                .withKeyExportable(false)))
                        .withNetworkProfile(new NetworkProfile()
                            .withSubnetId(
                                "/subscriptions/a2e95d27-c161-4b61-bda4-11512c14c2c2/resourceGroups/rg/providers/Microsoft.Network/virtualNetworks/vnet/subnets/subnet")
                            .withStaticIpAddressCount(2)))
                    .withDevCenterProjectResourceId(
                        "/subscriptions/222e81d0-cf38-4dab-baa5-289bf16baaa4/resourceGroups/rg-1es-devcenter/providers/Microsoft.DevCenter/projects/1ES"))
            .create();
    }
}
```

### Pools_Delete

```java
/**
 * Samples for Pools Delete.
 */
public final class PoolsDeleteSamples {
    /*
     * x-ms-original-file: 2026-07-03-preview/DeletePool.json
     */
    /**
     * Sample code: Pools_Delete.
     * 
     * @param manager Entry point to DevOpsInfrastructureManager.
     */
    public static void poolsDelete(com.azure.resourcemanager.devopsinfrastructure.DevOpsInfrastructureManager manager) {
        manager.pools().delete("rg", "pool", com.azure.core.util.Context.NONE);
    }
}
```

### Pools_DeleteResources

```java
import com.azure.resourcemanager.devopsinfrastructure.models.DeleteResourcesDetails;
import java.util.Arrays;

/**
 * Samples for Pools DeleteResources.
 */
public final class PoolsDeleteResourcesSamples {
    /*
     * x-ms-original-file: 2026-07-03-preview/Pools_DeleteResources.json
     */
    /**
     * Sample code: Pools_DeleteResources.
     * 
     * @param manager Entry point to DevOpsInfrastructureManager.
     */
    public static void
        poolsDeleteResources(com.azure.resourcemanager.devopsinfrastructure.DevOpsInfrastructureManager manager) {
        manager.pools()
            .deleteResourcesWithResponse("my-resource-group", "my-dev-ops-pool",
                new DeleteResourcesDetails().withResourceIds(Arrays.asList(
                    "/subscriptions/a2e95d27-c161-4b61-bda4-11512c14c2c2/resourceGroups/my-resource-group/providers/Microsoft.DevOpsInfrastructure/pools/my-dev-ops-pool/resources/dd8cc705c_0",
                    "/subscriptions/a2e95d27-c161-4b61-bda4-11512c14c2c2/resourceGroups/my-resource-group/providers/Microsoft.DevOpsInfrastructure/pools/my-dev-ops-pool/resources/dd8cc705c_1")),
                com.azure.core.util.Context.NONE);
    }
}
```

### Pools_GetByResourceGroup

```java
/**
 * Samples for Pools GetByResourceGroup.
 */
public final class PoolsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-07-03-preview/GetPool.json
     */
    /**
     * Sample code: Pools_Get.
     * 
     * @param manager Entry point to DevOpsInfrastructureManager.
     */
    public static void poolsGet(com.azure.resourcemanager.devopsinfrastructure.DevOpsInfrastructureManager manager) {
        manager.pools().getByResourceGroupWithResponse("rg", "pool", com.azure.core.util.Context.NONE);
    }
}
```

### Pools_List

```java
/**
 * Samples for Pools List.
 */
public final class PoolsListSamples {
    /*
     * x-ms-original-file: 2026-07-03-preview/ListPoolsBySubscription.json
     */
    /**
     * Sample code: Pools_ListBySubscription.
     * 
     * @param manager Entry point to DevOpsInfrastructureManager.
     */
    public static void
        poolsListBySubscription(com.azure.resourcemanager.devopsinfrastructure.DevOpsInfrastructureManager manager) {
        manager.pools().list(com.azure.core.util.Context.NONE);
    }
}
```

### Pools_ListByResourceGroup

```java
/**
 * Samples for Pools ListByResourceGroup.
 */
public final class PoolsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-07-03-preview/ListPoolsBySubscriptionAndResourceGroup.json
     */
    /**
     * Sample code: Pools_ListByResourceGroup.
     * 
     * @param manager Entry point to DevOpsInfrastructureManager.
     */
    public static void
        poolsListByResourceGroup(com.azure.resourcemanager.devopsinfrastructure.DevOpsInfrastructureManager manager) {
        manager.pools().listByResourceGroup("rg", com.azure.core.util.Context.NONE);
    }
}
```

### Pools_Update

```java
import com.azure.resourcemanager.devopsinfrastructure.models.Pool;

/**
 * Samples for Pools Update.
 */
public final class PoolsUpdateSamples {
    /*
     * x-ms-original-file: 2026-07-03-preview/UpdatePool.json
     */
    /**
     * Sample code: Pools_Update.
     * 
     * @param manager Entry point to DevOpsInfrastructureManager.
     */
    public static void poolsUpdate(com.azure.resourcemanager.devopsinfrastructure.DevOpsInfrastructureManager manager) {
        Pool resource
            = manager.pools().getByResourceGroupWithResponse("rg", "pool", com.azure.core.util.Context.NONE).getValue();
        resource.update().apply();
    }
}
```

### ResourceDetails_ListByPool

```java
/**
 * Samples for ResourceDetails ListByPool.
 */
public final class ResourceDetailsListByPoolSamples {
    /*
     * x-ms-original-file: 2026-07-03-preview/ResourceDetails_ListByPool.json
     */
    /**
     * Sample code: ResourceDetails_ListByPool.
     * 
     * @param manager Entry point to DevOpsInfrastructureManager.
     */
    public static void
        resourceDetailsListByPool(com.azure.resourcemanager.devopsinfrastructure.DevOpsInfrastructureManager manager) {
        manager.resourceDetails().listByPool("my-resource-group", "my-dev-ops-pool", com.azure.core.util.Context.NONE);
    }
}
```

### Sku_ListByLocation

```java
/**
 * Samples for Sku ListByLocation.
 */
public final class SkuListByLocationSamples {
    /*
     * x-ms-original-file: 2026-07-03-preview/Sku_ListByLocation.json
     */
    /**
     * Sample code: Sku_ListByLocation.
     * 
     * @param manager Entry point to DevOpsInfrastructureManager.
     */
    public static void
        skuListByLocation(com.azure.resourcemanager.devopsinfrastructure.DevOpsInfrastructureManager manager) {
        manager.skus().listByLocation("eastus", com.azure.core.util.Context.NONE);
    }
}
```

### SubscriptionUsages_Usages

```java
/**
 * Samples for SubscriptionUsages Usages.
 */
public final class SubscriptionUsagesUsagesSamples {
    /*
     * x-ms-original-file: 2026-07-03-preview/SubscriptionUsages_Usages.json
     */
    /**
     * Sample code: SubscriptionUsages_Usages.
     * 
     * @param manager Entry point to DevOpsInfrastructureManager.
     */
    public static void
        subscriptionUsagesUsages(com.azure.resourcemanager.devopsinfrastructure.DevOpsInfrastructureManager manager) {
        manager.subscriptionUsages().usages("eastus", com.azure.core.util.Context.NONE);
    }
}
```

