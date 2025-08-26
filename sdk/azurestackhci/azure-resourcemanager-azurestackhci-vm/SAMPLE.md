# Code snippets and samples


## AttestationStatuses

- [Get](#attestationstatuses_get)

## GalleryImages

- [CreateOrUpdate](#galleryimages_createorupdate)
- [Delete](#galleryimages_delete)
- [GetByResourceGroup](#galleryimages_getbyresourcegroup)
- [List](#galleryimages_list)
- [ListByResourceGroup](#galleryimages_listbyresourcegroup)
- [Update](#galleryimages_update)

## GuestAgents

- [Create](#guestagents_create)
- [Delete](#guestagents_delete)
- [Get](#guestagents_get)
- [ListByVirtualMachineInstance](#guestagents_listbyvirtualmachineinstance)

## HybridIdentityMetadata

- [Get](#hybrididentitymetadata_get)
- [ListByVirtualMachineInstance](#hybrididentitymetadata_listbyvirtualmachineinstance)

## LogicalNetworks

- [CreateOrUpdate](#logicalnetworks_createorupdate)
- [Delete](#logicalnetworks_delete)
- [GetByResourceGroup](#logicalnetworks_getbyresourcegroup)
- [List](#logicalnetworks_list)
- [ListByResourceGroup](#logicalnetworks_listbyresourcegroup)
- [Update](#logicalnetworks_update)

## MarketplaceGalleryImages

- [CreateOrUpdate](#marketplacegalleryimages_createorupdate)
- [Delete](#marketplacegalleryimages_delete)
- [GetByResourceGroup](#marketplacegalleryimages_getbyresourcegroup)
- [List](#marketplacegalleryimages_list)
- [ListByResourceGroup](#marketplacegalleryimages_listbyresourcegroup)
- [Update](#marketplacegalleryimages_update)

## NetworkInterfaces

- [CreateOrUpdate](#networkinterfaces_createorupdate)
- [Delete](#networkinterfaces_delete)
- [GetByResourceGroup](#networkinterfaces_getbyresourcegroup)
- [List](#networkinterfaces_list)
- [ListByResourceGroup](#networkinterfaces_listbyresourcegroup)
- [Update](#networkinterfaces_update)

## NetworkSecurityGroups

- [CreateOrUpdate](#networksecuritygroups_createorupdate)
- [Delete](#networksecuritygroups_delete)
- [GetByResourceGroup](#networksecuritygroups_getbyresourcegroup)
- [List](#networksecuritygroups_list)
- [ListByResourceGroup](#networksecuritygroups_listbyresourcegroup)
- [UpdateTags](#networksecuritygroups_updatetags)

## SecurityRules

- [CreateOrUpdate](#securityrules_createorupdate)
- [Delete](#securityrules_delete)
- [Get](#securityrules_get)
- [ListByNetworkSecurityGroup](#securityrules_listbynetworksecuritygroup)

## StorageContainers

- [CreateOrUpdate](#storagecontainers_createorupdate)
- [Delete](#storagecontainers_delete)
- [GetByResourceGroup](#storagecontainers_getbyresourcegroup)
- [List](#storagecontainers_list)
- [ListByResourceGroup](#storagecontainers_listbyresourcegroup)
- [Update](#storagecontainers_update)

## VirtualHardDisks

- [CreateOrUpdate](#virtualharddisks_createorupdate)
- [Delete](#virtualharddisks_delete)
- [GetByResourceGroup](#virtualharddisks_getbyresourcegroup)
- [List](#virtualharddisks_list)
- [ListByResourceGroup](#virtualharddisks_listbyresourcegroup)
- [Update](#virtualharddisks_update)
- [Upload](#virtualharddisks_upload)

## VirtualMachineInstances

- [CreateOrUpdate](#virtualmachineinstances_createorupdate)
- [Delete](#virtualmachineinstances_delete)
- [Get](#virtualmachineinstances_get)
- [List](#virtualmachineinstances_list)
- [Pause](#virtualmachineinstances_pause)
- [Restart](#virtualmachineinstances_restart)
- [Save](#virtualmachineinstances_save)
- [Start](#virtualmachineinstances_start)
- [Stop](#virtualmachineinstances_stop)
- [Update](#virtualmachineinstances_update)
### AttestationStatuses_Get

```java
/**
 * Samples for AttestationStatuses Get.
 */
public final class AttestationStatusesGetSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/AttestationStatuses_Get.json
     */
    /**
     * Sample code: GetAttestationStatus.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void getAttestationStatus(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.attestationStatuses()
            .getWithResponse(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                com.azure.core.util.Context.NONE);
    }
}
```

### GalleryImages_CreateOrUpdate

```java
import com.azure.resourcemanager.azurestackhci.vm.models.ExtendedLocation;
import com.azure.resourcemanager.azurestackhci.vm.models.ExtendedLocationTypes;
import com.azure.resourcemanager.azurestackhci.vm.models.GalleryImageProperties;
import com.azure.resourcemanager.azurestackhci.vm.models.OperatingSystemTypes;

/**
 * Samples for GalleryImages CreateOrUpdate.
 */
public final class GalleryImagesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/GalleryImages_CreateOrUpdate.json
     */
    /**
     * Sample code: PutGalleryImage.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void putGalleryImage(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.galleryImages()
            .define("test-gallery-image")
            .withRegion("West US2")
            .withExistingResourceGroup("test-rg")
            .withProperties(new GalleryImageProperties().withContainerId(
                "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.AzureStackHCI/storageContainers/test-storage-container")
                .withImagePath("C:\\test.vhdx")
                .withOsType(OperatingSystemTypes.LINUX))
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.ExtendedLocation/customLocations/dogfood-location")
                .withType(ExtendedLocationTypes.CUSTOM_LOCATION))
            .create();
    }
}
```

### GalleryImages_Delete

```java
/**
 * Samples for GalleryImages Delete.
 */
public final class GalleryImagesDeleteSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/GalleryImages_Delete.json
     */
    /**
     * Sample code: DeleteGalleryImage.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void deleteGalleryImage(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.galleryImages().delete("test-rg", "test-gallery-image", com.azure.core.util.Context.NONE);
    }
}
```

### GalleryImages_GetByResourceGroup

```java
/**
 * Samples for GalleryImages GetByResourceGroup.
 */
public final class GalleryImagesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/GalleryImages_Get.json
     */
    /**
     * Sample code: GetGalleryImage.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void getGalleryImage(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.galleryImages()
            .getByResourceGroupWithResponse("test-rg", "test-gallery-image", com.azure.core.util.Context.NONE);
    }
}
```

### GalleryImages_List

```java
/**
 * Samples for GalleryImages List.
 */
public final class GalleryImagesListSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/GalleryImages_ListAll.json
     */
    /**
     * Sample code: ListGalleryImageBySubscription.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void
        listGalleryImageBySubscription(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.galleryImages().list(com.azure.core.util.Context.NONE);
    }
}
```

### GalleryImages_ListByResourceGroup

```java
/**
 * Samples for GalleryImages ListByResourceGroup.
 */
public final class GalleryImagesListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/GalleryImages_ListByResourceGroup.json
     */
    /**
     * Sample code: ListGalleryImageByResourceGroup.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void
        listGalleryImageByResourceGroup(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.galleryImages().listByResourceGroup("test-rg", com.azure.core.util.Context.NONE);
    }
}
```

### GalleryImages_Update

```java
import com.azure.resourcemanager.azurestackhci.vm.models.GalleryImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for GalleryImages Update.
 */
public final class GalleryImagesUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/GalleryImages_Update.json
     */
    /**
     * Sample code: UpdateGalleryImage.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void updateGalleryImage(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        GalleryImage resource = manager.galleryImages()
            .getByResourceGroupWithResponse("test-rg", "test-gallery-image", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("additionalProperties", "sample")).apply();
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

### GuestAgents_Create

```java
import com.azure.resourcemanager.azurestackhci.vm.fluent.models.GuestAgentInner;
import com.azure.resourcemanager.azurestackhci.vm.models.GuestAgentProperties;
import com.azure.resourcemanager.azurestackhci.vm.models.GuestCredential;
import com.azure.resourcemanager.azurestackhci.vm.models.ProvisioningAction;

/**
 * Samples for GuestAgents Create.
 */
public final class GuestAgentsCreateSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/GuestAgents_Create.json
     */
    /**
     * Sample code: CreateGuestAgent.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void createGuestAgent(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.guestAgents()
            .create(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                new GuestAgentInner().withProperties(new GuestAgentProperties()
                    .withCredentials(
                        new GuestCredential().withUsername("tempuser").withPassword("fakeTokenPlaceholder"))
                    .withProvisioningAction(ProvisioningAction.INSTALL)),
                com.azure.core.util.Context.NONE);
    }
}
```

### GuestAgents_Delete

```java
/**
 * Samples for GuestAgents Delete.
 */
public final class GuestAgentsDeleteSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/GuestAgents_Delete.json
     */
    /**
     * Sample code: DeleteGuestAgent.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void deleteGuestAgent(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.guestAgents()
            .delete(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                com.azure.core.util.Context.NONE);
    }
}
```

### GuestAgents_Get

```java
/**
 * Samples for GuestAgents Get.
 */
public final class GuestAgentsGetSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/GuestAgents_Get.json
     */
    /**
     * Sample code: GetGuestAgent.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void getGuestAgent(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.guestAgents()
            .getWithResponse(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                com.azure.core.util.Context.NONE);
    }
}
```

### GuestAgents_ListByVirtualMachineInstance

```java
/**
 * Samples for GuestAgents ListByVirtualMachineInstance.
 */
public final class GuestAgentsListByVirtualMachineInstanceSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/GuestAgents_ListByVirtualMachineInstance.json
     */
    /**
     * Sample code: GuestAgentListByVirtualMachineInstances.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void guestAgentListByVirtualMachineInstances(
        com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.guestAgents()
            .listByVirtualMachineInstance(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                com.azure.core.util.Context.NONE);
    }
}
```

### HybridIdentityMetadata_Get

```java
/**
 * Samples for HybridIdentityMetadata Get.
 */
public final class HybridIdentityMetadataGetSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/HybridIdentityMetadataGroup_Get.json
     */
    /**
     * Sample code: GetHybridIdentityMetadata.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void
        getHybridIdentityMetadata(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.hybridIdentityMetadatas()
            .getWithResponse(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                com.azure.core.util.Context.NONE);
    }
}
```

### HybridIdentityMetadata_ListByVirtualMachineInstance

```java
/**
 * Samples for HybridIdentityMetadata ListByVirtualMachineInstance.
 */
public final class HybridIdentityMetadataListByVirtualMachineInstanceSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/HybridIdentityMetadata_ListByVirtualMachineInstance.json
     */
    /**
     * Sample code: HybridIdentityMetadataListByVirtualMachineInstances.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void hybridIdentityMetadataListByVirtualMachineInstances(
        com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.hybridIdentityMetadatas()
            .listByVirtualMachineInstance(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                com.azure.core.util.Context.NONE);
    }
}
```

### LogicalNetworks_CreateOrUpdate

```java
import com.azure.resourcemanager.azurestackhci.vm.models.ExtendedLocation;
import com.azure.resourcemanager.azurestackhci.vm.models.ExtendedLocationTypes;

/**
 * Samples for LogicalNetworks CreateOrUpdate.
 */
public final class LogicalNetworksCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/LogicalNetworks_CreateOrUpdate.json
     */
    /**
     * Sample code: PutLogicalNetwork.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void putLogicalNetwork(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.logicalNetworks()
            .define("test-lnet")
            .withRegion("West US2")
            .withExistingResourceGroup("test-rg")
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/dogfoodarc/providers/Microsoft.ExtendedLocation/customLocations/dogfood-location")
                .withType(ExtendedLocationTypes.CUSTOM_LOCATION))
            .create();
    }
}
```

### LogicalNetworks_Delete

```java
/**
 * Samples for LogicalNetworks Delete.
 */
public final class LogicalNetworksDeleteSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/LogicalNetworks_Delete.json
     */
    /**
     * Sample code: DeleteLogicalNetwork.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void deleteLogicalNetwork(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.logicalNetworks().delete("test-rg", "test-lnet", com.azure.core.util.Context.NONE);
    }
}
```

### LogicalNetworks_GetByResourceGroup

```java
/**
 * Samples for LogicalNetworks GetByResourceGroup.
 */
public final class LogicalNetworksGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/LogicalNetworks_Get.json
     */
    /**
     * Sample code: GetLogicalNetwork.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void getLogicalNetwork(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.logicalNetworks()
            .getByResourceGroupWithResponse("test-rg", "test-lnet", com.azure.core.util.Context.NONE);
    }
}
```

### LogicalNetworks_List

```java
/**
 * Samples for LogicalNetworks List.
 */
public final class LogicalNetworksListSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/LogicalNetworks_ListAll.json
     */
    /**
     * Sample code: ListLogicalNetworkBySubscription.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void
        listLogicalNetworkBySubscription(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.logicalNetworks().list(com.azure.core.util.Context.NONE);
    }
}
```

### LogicalNetworks_ListByResourceGroup

```java
/**
 * Samples for LogicalNetworks ListByResourceGroup.
 */
public final class LogicalNetworksListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/LogicalNetworks_ListByResourceGroup.json
     */
    /**
     * Sample code: ListLogicalNetworkByResourceGroup.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void
        listLogicalNetworkByResourceGroup(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.logicalNetworks().listByResourceGroup("test-rg", com.azure.core.util.Context.NONE);
    }
}
```

### LogicalNetworks_Update

```java
import com.azure.resourcemanager.azurestackhci.vm.models.LogicalNetwork;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for LogicalNetworks Update.
 */
public final class LogicalNetworksUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/LogicalNetworks_Update.json
     */
    /**
     * Sample code: UpdateLogicalNetwork.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void updateLogicalNetwork(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        LogicalNetwork resource = manager.logicalNetworks()
            .getByResourceGroupWithResponse("test-rg", "test-lnet", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("additionalProperties", "sample")).apply();
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

### MarketplaceGalleryImages_CreateOrUpdate

```java
import com.azure.resourcemanager.azurestackhci.vm.models.CloudInitDataSource;
import com.azure.resourcemanager.azurestackhci.vm.models.ExtendedLocation;
import com.azure.resourcemanager.azurestackhci.vm.models.ExtendedLocationTypes;
import com.azure.resourcemanager.azurestackhci.vm.models.GalleryImageIdentifier;
import com.azure.resourcemanager.azurestackhci.vm.models.GalleryImageVersion;
import com.azure.resourcemanager.azurestackhci.vm.models.HyperVGeneration;
import com.azure.resourcemanager.azurestackhci.vm.models.MarketplaceGalleryImageProperties;
import com.azure.resourcemanager.azurestackhci.vm.models.OperatingSystemTypes;

/**
 * Samples for MarketplaceGalleryImages CreateOrUpdate.
 */
public final class MarketplaceGalleryImagesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/MarketplaceGalleryImages_CreateOrUpdate.json
     */
    /**
     * Sample code: PutMarketplaceGalleryImage.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void
        putMarketplaceGalleryImage(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.marketplaceGalleryImages()
            .define("test-marketplace-gallery-image")
            .withRegion("West US2")
            .withExistingResourceGroup("test-rg")
            .withProperties(new MarketplaceGalleryImageProperties().withContainerId(
                "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.AzureStackHCI/storageContainers/test-storage-container")
                .withOsType(OperatingSystemTypes.WINDOWS)
                .withCloudInitDataSource(CloudInitDataSource.AZURE)
                .withHyperVGeneration(HyperVGeneration.V1)
                .withIdentifier(new GalleryImageIdentifier().withPublisher("myPublisherName")
                    .withOffer("myOfferName")
                    .withSku("mySkuName"))
                .withVersion(new GalleryImageVersion().withName("1.0.0")))
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.ExtendedLocation/customLocations/dogfood-location")
                .withType(ExtendedLocationTypes.CUSTOM_LOCATION))
            .create();
    }
}
```

### MarketplaceGalleryImages_Delete

```java
/**
 * Samples for MarketplaceGalleryImages Delete.
 */
public final class MarketplaceGalleryImagesDeleteSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/MarketplaceGalleryImages_Delete.json
     */
    /**
     * Sample code: DeleteMarketplaceGalleryImage.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void
        deleteMarketplaceGalleryImage(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.marketplaceGalleryImages()
            .delete("test-rg", "test-marketplace-gallery-image", com.azure.core.util.Context.NONE);
    }
}
```

### MarketplaceGalleryImages_GetByResourceGroup

```java
/**
 * Samples for MarketplaceGalleryImages GetByResourceGroup.
 */
public final class MarketplaceGalleryImagesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/MarketplaceGalleryImages_Get.json
     */
    /**
     * Sample code: GetMarketplaceGalleryImage.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void
        getMarketplaceGalleryImage(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.marketplaceGalleryImages()
            .getByResourceGroupWithResponse("test-rg", "test-marketplace-gallery-image",
                com.azure.core.util.Context.NONE);
    }
}
```

### MarketplaceGalleryImages_List

```java
/**
 * Samples for MarketplaceGalleryImages List.
 */
public final class MarketplaceGalleryImagesListSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/MarketplaceGalleryImages_ListAll.json
     */
    /**
     * Sample code: ListMarketplaceGalleryImageBySubscription.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void listMarketplaceGalleryImageBySubscription(
        com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.marketplaceGalleryImages().list(com.azure.core.util.Context.NONE);
    }
}
```

### MarketplaceGalleryImages_ListByResourceGroup

```java
/**
 * Samples for MarketplaceGalleryImages ListByResourceGroup.
 */
public final class MarketplaceGalleryImagesListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/MarketplaceGalleryImages_ListByResourceGroup.json
     */
    /**
     * Sample code: ListMarketplaceGalleryImageByResourceGroup.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void listMarketplaceGalleryImageByResourceGroup(
        com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.marketplaceGalleryImages().listByResourceGroup("test-rg", com.azure.core.util.Context.NONE);
    }
}
```

### MarketplaceGalleryImages_Update

```java
import com.azure.resourcemanager.azurestackhci.vm.models.MarketplaceGalleryImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for MarketplaceGalleryImages Update.
 */
public final class MarketplaceGalleryImagesUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/MarketplaceGalleryImages_Update.json
     */
    /**
     * Sample code: UpdateMarketplaceGalleryImage.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void
        updateMarketplaceGalleryImage(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        MarketplaceGalleryImage resource = manager.marketplaceGalleryImages()
            .getByResourceGroupWithResponse("test-rg", "test-marketplce-gallery-image",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("additionalProperties", "sample")).apply();
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

### NetworkInterfaces_CreateOrUpdate

```java
import com.azure.resourcemanager.azurestackhci.vm.models.ExtendedLocation;
import com.azure.resourcemanager.azurestackhci.vm.models.ExtendedLocationTypes;
import com.azure.resourcemanager.azurestackhci.vm.models.IPConfiguration;
import com.azure.resourcemanager.azurestackhci.vm.models.IPConfigurationProperties;
import com.azure.resourcemanager.azurestackhci.vm.models.LogicalNetworkArmReference;
import com.azure.resourcemanager.azurestackhci.vm.models.NetworkInterfaceProperties;
import java.util.Arrays;

/**
 * Samples for NetworkInterfaces CreateOrUpdate.
 */
public final class NetworkInterfacesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/NetworkInterfaces__CreateOrUpdate_CreateFromLocal.json
     */
    /**
     * Sample code: CreateNetworkInterfaceFromLocal.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void
        createNetworkInterfaceFromLocal(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.networkInterfaces()
            .define("test-nic")
            .withRegion("eastus")
            .withExistingResourceGroup("test-rg")
            .withProperties(new NetworkInterfaceProperties().withIpConfigurations(Arrays.asList(new IPConfiguration()
                .withName("ipconfig-sample")
                .withProperties(new IPConfigurationProperties().withSubnet(new LogicalNetworkArmReference().withId(
                    "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.AzureStackHCI/logicalNetworks/test-lnet")))))
                .withCreateFromLocal(true))
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.ExtendedLocation/customLocations/dogfood-location")
                .withType(ExtendedLocationTypes.CUSTOM_LOCATION))
            .create();
    }

    /*
     * x-ms-original-file: 2025-06-01-preview/NetworkInterfaces_CreateOrUpdate.json
     */
    /**
     * Sample code: PutNetworkInterface.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void putNetworkInterface(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.networkInterfaces()
            .define("test-nic")
            .withRegion("eastus")
            .withExistingResourceGroup("test-rg")
            .withProperties(new NetworkInterfaceProperties().withIpConfigurations(Arrays.asList(new IPConfiguration()
                .withName("ipconfig-sample")
                .withProperties(new IPConfigurationProperties().withSubnet(new LogicalNetworkArmReference().withId(
                    "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.AzureStackHCI/logicalNetworks/test-lnet"))))))
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.ExtendedLocation/customLocations/dogfood-location")
                .withType(ExtendedLocationTypes.CUSTOM_LOCATION))
            .create();
    }
}
```

### NetworkInterfaces_Delete

```java
/**
 * Samples for NetworkInterfaces Delete.
 */
public final class NetworkInterfacesDeleteSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/NetworkInterfaces_Delete.json
     */
    /**
     * Sample code: DeleteNetworkInterface.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void
        deleteNetworkInterface(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.networkInterfaces().delete("test-rg", "test-nic", com.azure.core.util.Context.NONE);
    }
}
```

### NetworkInterfaces_GetByResourceGroup

```java
/**
 * Samples for NetworkInterfaces GetByResourceGroup.
 */
public final class NetworkInterfacesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/NetworkInterfaces_Get.json
     */
    /**
     * Sample code: GetNetworkInterface.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void getNetworkInterface(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.networkInterfaces()
            .getByResourceGroupWithResponse("test-rg", "test-nic", com.azure.core.util.Context.NONE);
    }
}
```

### NetworkInterfaces_List

```java
/**
 * Samples for NetworkInterfaces List.
 */
public final class NetworkInterfacesListSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/NetworkInterfaces_ListAll.json
     */
    /**
     * Sample code: ListNetworkInterfaceBySubscription.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void
        listNetworkInterfaceBySubscription(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.networkInterfaces().list(com.azure.core.util.Context.NONE);
    }
}
```

### NetworkInterfaces_ListByResourceGroup

```java
/**
 * Samples for NetworkInterfaces ListByResourceGroup.
 */
public final class NetworkInterfacesListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/NetworkInterfaces_ListByResourceGroup.json
     */
    /**
     * Sample code: ListNetworkInterfaceByResourceGroup.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void
        listNetworkInterfaceByResourceGroup(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.networkInterfaces().listByResourceGroup("test-rg", com.azure.core.util.Context.NONE);
    }
}
```

### NetworkInterfaces_Update

```java
import com.azure.resourcemanager.azurestackhci.vm.models.NetworkInterface;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for NetworkInterfaces Update.
 */
public final class NetworkInterfacesUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/NetworkInterfaces_Update.json
     */
    /**
     * Sample code: UpdateNetworkInterface.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void
        updateNetworkInterface(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        NetworkInterface resource = manager.networkInterfaces()
            .getByResourceGroupWithResponse("test-rg", "test-nic", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("additionalProperties", "sample")).apply();
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

### NetworkSecurityGroups_CreateOrUpdate

```java
/**
 * Samples for NetworkSecurityGroups CreateOrUpdate.
 */
public final class NetworkSecurityGroupsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/NetworkSecurityGroups_CreateOrUpdate.json
     */
    /**
     * Sample code: CreateNetworkSecurityGroup.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void
        createNetworkSecurityGroup(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.networkSecurityGroups()
            .define("testnsg")
            .withRegion("eastus")
            .withExistingResourceGroup("testrg")
            .create();
    }
}
```

### NetworkSecurityGroups_Delete

```java
/**
 * Samples for NetworkSecurityGroups Delete.
 */
public final class NetworkSecurityGroupsDeleteSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/NetworkSecurityGroups_Delete.json
     */
    /**
     * Sample code: Delete network security group.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void
        deleteNetworkSecurityGroup(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.networkSecurityGroups().delete("test-rg", "testnsg", com.azure.core.util.Context.NONE);
    }
}
```

### NetworkSecurityGroups_GetByResourceGroup

```java
/**
 * Samples for NetworkSecurityGroups GetByResourceGroup.
 */
public final class NetworkSecurityGroupsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/NetworkSecurityGroups_Get.json
     */
    /**
     * Sample code: Get network security group.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void
        getNetworkSecurityGroup(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.networkSecurityGroups()
            .getByResourceGroupWithResponse("test-rg", "testnsg", com.azure.core.util.Context.NONE);
    }
}
```

### NetworkSecurityGroups_List

```java
/**
 * Samples for NetworkSecurityGroups List.
 */
public final class NetworkSecurityGroupsListSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/NetworkSecurityGroups_ListAll.json
     */
    /**
     * Sample code: List all network security groups.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void
        listAllNetworkSecurityGroups(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.networkSecurityGroups().list(com.azure.core.util.Context.NONE);
    }
}
```

### NetworkSecurityGroups_ListByResourceGroup

```java
/**
 * Samples for NetworkSecurityGroups ListByResourceGroup.
 */
public final class NetworkSecurityGroupsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/NetworkSecurityGroups_ListByResourceGroup.json
     */
    /**
     * Sample code: List network security groups in resource group.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void listNetworkSecurityGroupsInResourceGroup(
        com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.networkSecurityGroups().listByResourceGroup("testrg", com.azure.core.util.Context.NONE);
    }
}
```

### NetworkSecurityGroups_UpdateTags

```java
import com.azure.resourcemanager.azurestackhci.vm.models.NetworkSecurityGroup;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for NetworkSecurityGroups UpdateTags.
 */
public final class NetworkSecurityGroupsUpdateTagsSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/NetworkSecurityGroups_UpdateTags.json
     */
    /**
     * Sample code: Update network security group tags.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void
        updateNetworkSecurityGroupTags(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        NetworkSecurityGroup resource = manager.networkSecurityGroups()
            .getByResourceGroupWithResponse("testrg", "testnsg", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

### SecurityRules_CreateOrUpdate

```java
import com.azure.resourcemanager.azurestackhci.vm.models.ExtendedLocation;
import com.azure.resourcemanager.azurestackhci.vm.models.ExtendedLocationTypes;
import com.azure.resourcemanager.azurestackhci.vm.models.SecurityRuleAccess;
import com.azure.resourcemanager.azurestackhci.vm.models.SecurityRuleDirection;
import com.azure.resourcemanager.azurestackhci.vm.models.SecurityRuleProperties;
import com.azure.resourcemanager.azurestackhci.vm.models.SecurityRuleProtocol;
import java.util.Arrays;

/**
 * Samples for SecurityRules CreateOrUpdate.
 */
public final class SecurityRulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/SecurityRules_CreateOrUpdate.json
     */
    /**
     * Sample code: SecurityRulesCreateOrUpdate.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void
        securityRulesCreateOrUpdate(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.securityRules()
            .define("rule1")
            .withExistingNetworkSecurityGroup("testrg", "testnsg")
            .withProperties(new SecurityRuleProperties().withProtocol(SecurityRuleProtocol.ASTERISK)
                .withSourceAddressPrefixes(Arrays.asList("*"))
                .withDestinationAddressPrefixes(Arrays.asList("*"))
                .withSourcePortRanges(Arrays.asList("*"))
                .withDestinationPortRanges(Arrays.asList("80"))
                .withAccess(SecurityRuleAccess.ALLOW)
                .withPriority(130)
                .withDirection(SecurityRuleDirection.INBOUND))
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.ExtendedLocation/customLocations/dogfood-location")
                .withType(ExtendedLocationTypes.CUSTOM_LOCATION))
            .create();
    }
}
```

### SecurityRules_Delete

```java
/**
 * Samples for SecurityRules Delete.
 */
public final class SecurityRulesDeleteSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/SecurityRules_Delete.json
     */
    /**
     * Sample code: SecurityRulesDelete.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void securityRulesDelete(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.securityRules().delete("testrg", "testnsg", "rule1", com.azure.core.util.Context.NONE);
    }
}
```

### SecurityRules_Get

```java
/**
 * Samples for SecurityRules Get.
 */
public final class SecurityRulesGetSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/SecurityRules_Get.json
     */
    /**
     * Sample code: Get network security rule in network security group.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void getNetworkSecurityRuleInNetworkSecurityGroup(
        com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.securityRules().getWithResponse("testrg", "testnsg", "rule1", com.azure.core.util.Context.NONE);
    }
}
```

### SecurityRules_ListByNetworkSecurityGroup

```java
/**
 * Samples for SecurityRules ListByNetworkSecurityGroup.
 */
public final class SecurityRulesListByNetworkSecurityGroupSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/SecurityRules_ListByNetworkSecurityGroup.json
     */
    /**
     * Sample code: List network security rules in network security group.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void listNetworkSecurityRulesInNetworkSecurityGroup(
        com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.securityRules().listByNetworkSecurityGroup("testrg", "testnsg", com.azure.core.util.Context.NONE);
    }
}
```

### StorageContainers_CreateOrUpdate

```java
import com.azure.resourcemanager.azurestackhci.vm.models.ExtendedLocation;
import com.azure.resourcemanager.azurestackhci.vm.models.ExtendedLocationTypes;
import com.azure.resourcemanager.azurestackhci.vm.models.StorageContainerProperties;

/**
 * Samples for StorageContainers CreateOrUpdate.
 */
public final class StorageContainersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/StorageContainers_CreateOrUpdate.json
     */
    /**
     * Sample code: PutStorageContainer.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void putStorageContainer(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.storageContainers()
            .define("Default_Container")
            .withRegion("West US2")
            .withExistingResourceGroup("test-rg")
            .withProperties(new StorageContainerProperties().withPath("C:\\container_storage"))
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.ExtendedLocation/customLocations/dogfood-location")
                .withType(ExtendedLocationTypes.CUSTOM_LOCATION))
            .create();
    }
}
```

### StorageContainers_Delete

```java
/**
 * Samples for StorageContainers Delete.
 */
public final class StorageContainersDeleteSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/StorageContainers_Delete.json
     */
    /**
     * Sample code: DeleteStorageContainer.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void
        deleteStorageContainer(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.storageContainers().delete("test-rg", "Default_Container", com.azure.core.util.Context.NONE);
    }
}
```

### StorageContainers_GetByResourceGroup

```java
/**
 * Samples for StorageContainers GetByResourceGroup.
 */
public final class StorageContainersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/StorageContainers_Get.json
     */
    /**
     * Sample code: GetStorageContainer.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void getStorageContainer(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.storageContainers()
            .getByResourceGroupWithResponse("test-rg", "Default_Container", com.azure.core.util.Context.NONE);
    }
}
```

### StorageContainers_List

```java
/**
 * Samples for StorageContainers List.
 */
public final class StorageContainersListSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/StorageContainers_ListAll.json
     */
    /**
     * Sample code: ListStorageContainerBySubscription.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void
        listStorageContainerBySubscription(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.storageContainers().list(com.azure.core.util.Context.NONE);
    }
}
```

### StorageContainers_ListByResourceGroup

```java
/**
 * Samples for StorageContainers ListByResourceGroup.
 */
public final class StorageContainersListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/StorageContainers_ListByResourceGroup.json
     */
    /**
     * Sample code: ListStorageContainerByResourceGroup.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void
        listStorageContainerByResourceGroup(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.storageContainers().listByResourceGroup("test-rg", com.azure.core.util.Context.NONE);
    }
}
```

### StorageContainers_Update

```java
import com.azure.resourcemanager.azurestackhci.vm.models.StorageContainer;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for StorageContainers Update.
 */
public final class StorageContainersUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/StorageContainers_Update.json
     */
    /**
     * Sample code: UpdateStorageContainer.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void
        updateStorageContainer(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        StorageContainer resource = manager.storageContainers()
            .getByResourceGroupWithResponse("test-rg", "Default_Container", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("additionalProperties", "sample")).apply();
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

### VirtualHardDisks_CreateOrUpdate

```java
import com.azure.resourcemanager.azurestackhci.vm.models.ExtendedLocation;
import com.azure.resourcemanager.azurestackhci.vm.models.ExtendedLocationTypes;
import com.azure.resourcemanager.azurestackhci.vm.models.VirtualHardDiskProperties;

/**
 * Samples for VirtualHardDisks CreateOrUpdate.
 */
public final class VirtualHardDisksCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/VirtualHardDisks__CreateOrUpdate_CreateFromLocal.json
     */
    /**
     * Sample code: CreateVirtualHardDiskFromLocal.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void
        createVirtualHardDiskFromLocal(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.virtualHardDisks()
            .define("test-vhd")
            .withRegion("West US2")
            .withExistingResourceGroup("test-rg")
            .withProperties(new VirtualHardDiskProperties().withCreateFromLocal(true)
                .withContainerId(
                    "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.AzureStackHCI/storageContainers/test-storage-container"))
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.ExtendedLocation/customLocations/dogfood-location")
                .withType(ExtendedLocationTypes.CUSTOM_LOCATION))
            .create();
    }

    /*
     * x-ms-original-file: 2025-06-01-preview/VirtualHardDisks_CreateOrUpdate.json
     */
    /**
     * Sample code: PutVirtualHardDisk.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void putVirtualHardDisk(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.virtualHardDisks()
            .define("test-vhd")
            .withRegion("West US2")
            .withExistingResourceGroup("test-rg")
            .withProperties(new VirtualHardDiskProperties().withDiskSizeGB(32L))
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.ExtendedLocation/customLocations/dogfood-location")
                .withType(ExtendedLocationTypes.CUSTOM_LOCATION))
            .create();
    }
}
```

### VirtualHardDisks_Delete

```java
/**
 * Samples for VirtualHardDisks Delete.
 */
public final class VirtualHardDisksDeleteSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/VirtualHardDisks_Delete.json
     */
    /**
     * Sample code: DeleteVirtualHardDisk.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void
        deleteVirtualHardDisk(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.virtualHardDisks().delete("test-rg", "test-vhd", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualHardDisks_GetByResourceGroup

```java
/**
 * Samples for VirtualHardDisks GetByResourceGroup.
 */
public final class VirtualHardDisksGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/VirtualHardDisks_Get.json
     */
    /**
     * Sample code: GetVirtualHardDisk.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void getVirtualHardDisk(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.virtualHardDisks()
            .getByResourceGroupWithResponse("test-rg", "test-vhd", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualHardDisks_List

```java
/**
 * Samples for VirtualHardDisks List.
 */
public final class VirtualHardDisksListSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/VirtualHardDisks_ListAll.json
     */
    /**
     * Sample code: ListVirtualHardDiskBySubscription.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void
        listVirtualHardDiskBySubscription(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.virtualHardDisks().list(com.azure.core.util.Context.NONE);
    }
}
```

### VirtualHardDisks_ListByResourceGroup

```java
/**
 * Samples for VirtualHardDisks ListByResourceGroup.
 */
public final class VirtualHardDisksListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/VirtualHardDisks_ListByResourceGroup.json
     */
    /**
     * Sample code: ListVirtualHardDiskByResourceGroup.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void
        listVirtualHardDiskByResourceGroup(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.virtualHardDisks().listByResourceGroup("test-rg", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualHardDisks_Update

```java
import com.azure.resourcemanager.azurestackhci.vm.models.VirtualHardDisk;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for VirtualHardDisks Update.
 */
public final class VirtualHardDisksUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/VirtualHardDisks_Update.json
     */
    /**
     * Sample code: UpdateVirtualHardDisk.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void
        updateVirtualHardDisk(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        VirtualHardDisk resource = manager.virtualHardDisks()
            .getByResourceGroupWithResponse("test-rg", "test-vhd", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("additionalProperties", "sample")).apply();
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

### VirtualHardDisks_Upload

```java
import com.azure.resourcemanager.azurestackhci.vm.models.VirtualHardDiskUploadRequest;

/**
 * Samples for VirtualHardDisks Upload.
 */
public final class VirtualHardDisksUploadSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/VirtualHardDisks_Upload.json
     */
    /**
     * Sample code: UploadVirtualHardDisk.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void
        uploadVirtualHardDisk(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.virtualHardDisks()
            .upload("test-rg", "test-vhd", new VirtualHardDiskUploadRequest().withAzureManagedDiskUploadUrl(
                "https://YourStorageAccountName.blob.core.windows.net/YourContainerName/YourVHDBlobName.vhd?<sas-token>"),
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstances_CreateOrUpdate

```java
import com.azure.resourcemanager.azurestackhci.vm.fluent.models.VirtualMachineInstanceInner;
import com.azure.resourcemanager.azurestackhci.vm.models.ExtendedLocation;
import com.azure.resourcemanager.azurestackhci.vm.models.ExtendedLocationTypes;
import com.azure.resourcemanager.azurestackhci.vm.models.GpuAssignmentTypeEnum;
import com.azure.resourcemanager.azurestackhci.vm.models.ImageArmReference;
import com.azure.resourcemanager.azurestackhci.vm.models.NetworkInterfaceArmReference;
import com.azure.resourcemanager.azurestackhci.vm.models.VirtualHardDiskArmReference;
import com.azure.resourcemanager.azurestackhci.vm.models.VirtualMachineInstanceProperties;
import com.azure.resourcemanager.azurestackhci.vm.models.VirtualMachineInstancePropertiesHardwareProfile;
import com.azure.resourcemanager.azurestackhci.vm.models.VirtualMachineInstancePropertiesHardwareProfileVirtualMachineGPU;
import com.azure.resourcemanager.azurestackhci.vm.models.VirtualMachineInstancePropertiesNetworkProfile;
import com.azure.resourcemanager.azurestackhci.vm.models.VirtualMachineInstancePropertiesOsProfile;
import com.azure.resourcemanager.azurestackhci.vm.models.VirtualMachineInstancePropertiesOsProfileWindowsConfiguration;
import com.azure.resourcemanager.azurestackhci.vm.models.VirtualMachineInstancePropertiesSecurityProfile;
import com.azure.resourcemanager.azurestackhci.vm.models.VirtualMachineInstancePropertiesSecurityProfileUefiSettings;
import com.azure.resourcemanager.azurestackhci.vm.models.VirtualMachineInstancePropertiesStorageProfile;
import com.azure.resourcemanager.azurestackhci.vm.models.VirtualMachineInstancePropertiesStorageProfileOsDisk;
import com.azure.resourcemanager.azurestackhci.vm.models.VmSizeEnum;
import java.util.Arrays;

/**
 * Samples for VirtualMachineInstances CreateOrUpdate.
 */
public final class VirtualMachineInstancesCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * 2025-06-01-preview/VirtualMachineInstances_CreateOrUpdate_Put_Virtual_Machine_Instance_With_Gallery_Image.json
     */
    /**
     * Sample code: PutVirtualMachineInstanceWithGalleryImage.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void putVirtualMachineInstanceWithGalleryImage(
        com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.virtualMachineInstances()
            .createOrUpdate(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                new VirtualMachineInstanceInner().withProperties(new VirtualMachineInstanceProperties()
                    .withHardwareProfile(
                        new VirtualMachineInstancePropertiesHardwareProfile().withVmSize(VmSizeEnum.DEFAULT))
                    .withNetworkProfile(new VirtualMachineInstancePropertiesNetworkProfile()
                        .withNetworkInterfaces(Arrays.asList(new NetworkInterfaceArmReference().withId(
                            "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.AzureStackHCI/networkInterfaces/test-nic"))))
                    .withOsProfile(
                        new VirtualMachineInstancePropertiesOsProfile().withAdminPassword("fakeTokenPlaceholder")
                            .withAdminUsername("localadmin")
                            .withComputerName("luamaster"))
                    .withSecurityProfile(new VirtualMachineInstancePropertiesSecurityProfile().withEnableTPM(true)
                        .withUefiSettings(new VirtualMachineInstancePropertiesSecurityProfileUefiSettings()
                            .withSecureBootEnabled(true)))
                    .withStorageProfile(new VirtualMachineInstancePropertiesStorageProfile()
                        .withImageReference(new ImageArmReference().withId(
                            "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.AzureStackHCI/galleryImages/test-gallery-image"))
                        .withVmConfigStoragePathId(
                            "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.AzureStackHCI/storageContainers/test-container")))
                    .withExtendedLocation(new ExtendedLocation().withName(
                        "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.ExtendedLocation/customLocations/dogfood-location")
                        .withType(ExtendedLocationTypes.CUSTOM_LOCATION)),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * 2025-06-01-preview/VirtualMachineInstances_CreateOrUpdate_Put_Virtual_Machine_Instance_With_Os_Disk.json
     */
    /**
     * Sample code: PutVirtualMachineInstanceWithOsDisk.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void
        putVirtualMachineInstanceWithOsDisk(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.virtualMachineInstances()
            .createOrUpdate(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                new VirtualMachineInstanceInner().withProperties(new VirtualMachineInstanceProperties()
                    .withHardwareProfile(
                        new VirtualMachineInstancePropertiesHardwareProfile().withVmSize(VmSizeEnum.DEFAULT))
                    .withNetworkProfile(new VirtualMachineInstancePropertiesNetworkProfile()
                        .withNetworkInterfaces(Arrays.asList(new NetworkInterfaceArmReference().withId(
                            "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.AzureStackHCI/networkInterfaces/test-nic"))))
                    .withSecurityProfile(new VirtualMachineInstancePropertiesSecurityProfile().withEnableTPM(true)
                        .withUefiSettings(new VirtualMachineInstancePropertiesSecurityProfileUefiSettings()
                            .withSecureBootEnabled(true)))
                    .withStorageProfile(new VirtualMachineInstancePropertiesStorageProfile()
                        .withOsDisk(new VirtualMachineInstancePropertiesStorageProfileOsDisk().withId(
                            "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.AzureStackHCI/virtualHardDisks/test-vhd"))
                        .withVmConfigStoragePathId(
                            "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.AzureStackHCI/storageContainers/test-container")))
                    .withExtendedLocation(new ExtendedLocation().withName(
                        "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.ExtendedLocation/customLocations/dogfood-location")
                        .withType(ExtendedLocationTypes.CUSTOM_LOCATION)),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-06-01-preview/
     * VirtualMachineInstances_CreateOrUpdate_Put_Virtual_Machine_Instance_With_Marketplace_Gallery_Image.json
     */
    /**
     * Sample code: PutVirtualMachineInstanceWithMarketplaceGalleryImage.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void putVirtualMachineInstanceWithMarketplaceGalleryImage(
        com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.virtualMachineInstances()
            .createOrUpdate(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                new VirtualMachineInstanceInner().withProperties(new VirtualMachineInstanceProperties()
                    .withHardwareProfile(
                        new VirtualMachineInstancePropertiesHardwareProfile().withVmSize(VmSizeEnum.DEFAULT))
                    .withNetworkProfile(new VirtualMachineInstancePropertiesNetworkProfile()
                        .withNetworkInterfaces(Arrays.asList(new NetworkInterfaceArmReference().withId(
                            "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.AzureStackHCI/networkInterfaces/test-nic"))))
                    .withOsProfile(
                        new VirtualMachineInstancePropertiesOsProfile().withAdminPassword("fakeTokenPlaceholder")
                            .withAdminUsername("localadmin")
                            .withComputerName("luamaster"))
                    .withSecurityProfile(new VirtualMachineInstancePropertiesSecurityProfile().withEnableTPM(true)
                        .withUefiSettings(new VirtualMachineInstancePropertiesSecurityProfileUefiSettings()
                            .withSecureBootEnabled(true)))
                    .withStorageProfile(new VirtualMachineInstancePropertiesStorageProfile()
                        .withImageReference(new ImageArmReference().withId(
                            "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.AzureStackHCI/marketplaceGalleryImages/test-marketplace-gallery-image"))
                        .withVmConfigStoragePathId(
                            "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.AzureStackHCI/storageContainers/test-container")))
                    .withExtendedLocation(new ExtendedLocation().withName(
                        "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.ExtendedLocation/customLocations/dogfood-location")
                        .withType(ExtendedLocationTypes.CUSTOM_LOCATION)),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * 2025-06-01-preview/VirtualMachineInstances_CreateOrUpdate_Put_Virtual_Machine_Instance_With_Vm_Config_Agent.json
     */
    /**
     * Sample code: PutVirtualMachineInstanceWithVMConfigAgent.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void putVirtualMachineInstanceWithVMConfigAgent(
        com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.virtualMachineInstances()
            .createOrUpdate(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                new VirtualMachineInstanceInner().withProperties(new VirtualMachineInstanceProperties()
                    .withHardwareProfile(
                        new VirtualMachineInstancePropertiesHardwareProfile().withVmSize(VmSizeEnum.DEFAULT))
                    .withNetworkProfile(new VirtualMachineInstancePropertiesNetworkProfile()
                        .withNetworkInterfaces(Arrays.asList(new NetworkInterfaceArmReference().withId(
                            "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.AzureStackHCI/networkInterfaces/test-nic"))))
                    .withOsProfile(new VirtualMachineInstancePropertiesOsProfile()
                        .withAdminPassword("fakeTokenPlaceholder")
                        .withAdminUsername("localadmin")
                        .withComputerName("luamaster")
                        .withWindowsConfiguration(new VirtualMachineInstancePropertiesOsProfileWindowsConfiguration()
                            .withProvisionVMConfigAgent(true)))
                    .withSecurityProfile(new VirtualMachineInstancePropertiesSecurityProfile().withEnableTPM(true)
                        .withUefiSettings(new VirtualMachineInstancePropertiesSecurityProfileUefiSettings()
                            .withSecureBootEnabled(true)))
                    .withStorageProfile(new VirtualMachineInstancePropertiesStorageProfile()
                        .withImageReference(new ImageArmReference().withId(
                            "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.AzureStackHCI/galleryImages/test-gallery-image"))
                        .withVmConfigStoragePathId(
                            "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.AzureStackHCI/storageContainers/test-container")))
                    .withExtendedLocation(new ExtendedLocation().withName(
                        "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.ExtendedLocation/customLocations/dogfood-location")
                        .withType(ExtendedLocationTypes.CUSTOM_LOCATION)),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * 2025-06-01-preview/VirtualMachineInstances_CreateOrUpdate_Put_Virtual_Machine_Instance_With_Gpu.json
     */
    /**
     * Sample code: PutVirtualMachineInstanceWithGpu.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void
        putVirtualMachineInstanceWithGpu(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.virtualMachineInstances()
            .createOrUpdate(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                new VirtualMachineInstanceInner()
                    .withProperties(new VirtualMachineInstanceProperties()
                        .withHardwareProfile(
                            new VirtualMachineInstancePropertiesHardwareProfile().withVmSize(VmSizeEnum.DEFAULT)
                                .withVirtualMachineGPUs(Arrays
                                    .asList(new VirtualMachineInstancePropertiesHardwareProfileVirtualMachineGPU()
                                        .withAssignmentType(GpuAssignmentTypeEnum.GPU_DDA)
                                        .withPartitionSizeMB(0L))))
                        .withNetworkProfile(new VirtualMachineInstancePropertiesNetworkProfile()
                            .withNetworkInterfaces(Arrays.asList(new NetworkInterfaceArmReference().withId(
                                "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.AzureStackHCI/networkInterfaces/test-nic"))))
                        .withOsProfile(
                            new VirtualMachineInstancePropertiesOsProfile().withAdminPassword("fakeTokenPlaceholder")
                                .withAdminUsername("localadmin")
                                .withComputerName("luamaster"))
                        .withSecurityProfile(new VirtualMachineInstancePropertiesSecurityProfile()
                            .withEnableTPM(true)
                            .withUefiSettings(new VirtualMachineInstancePropertiesSecurityProfileUefiSettings()
                                .withSecureBootEnabled(true)))
                        .withStorageProfile(new VirtualMachineInstancePropertiesStorageProfile().withImageReference(
                            new ImageArmReference().withId(
                                "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.AzureStackHCI/galleryImages/test-gallery-image"))
                            .withVmConfigStoragePathId(
                                "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.AzureStackHCI/storageContainers/test-container")))
                    .withExtendedLocation(new ExtendedLocation().withName(
                        "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.ExtendedLocation/customLocations/dogfood-location")
                        .withType(ExtendedLocationTypes.CUSTOM_LOCATION)),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-06-01-preview/VirtualMachineInstances__CreateOrUpdate_CreateFromLocal.json
     */
    /**
     * Sample code: CreateVirtualMachineInstanceFromLocal.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void createVirtualMachineInstanceFromLocal(
        com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.virtualMachineInstances()
            .createOrUpdate(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                new VirtualMachineInstanceInner().withProperties(new VirtualMachineInstanceProperties()
                    .withNetworkProfile(new VirtualMachineInstancePropertiesNetworkProfile()
                        .withNetworkInterfaces(Arrays.asList(new NetworkInterfaceArmReference().withId(
                            "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.AzureStackHCI/networkInterfaces/test-nic"))))
                    .withStorageProfile(new VirtualMachineInstancePropertiesStorageProfile()
                        .withDataDisks(Arrays.asList(new VirtualHardDiskArmReference().withId(
                            "/subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.AzureStackHCI/virtualHardDisks/test-vhd"))))
                    .withCreateFromLocal(true))
                    .withExtendedLocation(new ExtendedLocation().withName(
                        "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.ExtendedLocation/customLocations/dogfood-location")
                        .withType(ExtendedLocationTypes.CUSTOM_LOCATION)),
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstances_Delete

```java
/**
 * Samples for VirtualMachineInstances Delete.
 */
public final class VirtualMachineInstancesDeleteSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/VirtualMachineInstances_Delete.json
     */
    /**
     * Sample code: DeleteVirtualMachine.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void deleteVirtualMachine(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.virtualMachineInstances()
            .delete(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstances_Get

```java
/**
 * Samples for VirtualMachineInstances Get.
 */
public final class VirtualMachineInstancesGetSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/VirtualMachineInstances_Get.json
     */
    /**
     * Sample code: GetVirtualMachineInstance.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void
        getVirtualMachineInstance(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.virtualMachineInstances()
            .getWithResponse(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstances_List

```java
/**
 * Samples for VirtualMachineInstances List.
 */
public final class VirtualMachineInstancesListSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/VirtualMachineInstances_List.json
     */
    /**
     * Sample code: ListVirtualMachineInstances.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void
        listVirtualMachineInstances(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.virtualMachineInstances()
            .list(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstances_Pause

```java
/**
 * Samples for VirtualMachineInstances Pause.
 */
public final class VirtualMachineInstancesPauseSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/VirtualMachineInstances_Pause.json
     */
    /**
     * Sample code: PauseVirtualMachine.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void pauseVirtualMachine(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.virtualMachineInstances()
            .pause(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/Microsoft.HybridCompute/machines/DemoVM/providers/Microsoft.AzureStackHCI/virtualMachineInstances/default",
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstances_Restart

```java
/**
 * Samples for VirtualMachineInstances Restart.
 */
public final class VirtualMachineInstancesRestartSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/VirtualMachineInstances_Restart.json
     */
    /**
     * Sample code: RestartVirtualMachine.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void
        restartVirtualMachine(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.virtualMachineInstances()
            .restart(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM/providers/Microsoft.AzureStackHCI/virtualMachineInstances/default",
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstances_Save

```java
/**
 * Samples for VirtualMachineInstances Save.
 */
public final class VirtualMachineInstancesSaveSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/VirtualMachineInstances_Save.json
     */
    /**
     * Sample code: SaveVirtualMachine.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void saveVirtualMachine(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.virtualMachineInstances()
            .save(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/Microsoft.HybridCompute/machines/DemoVM/providers/Microsoft.AzureStackHCI/virtualMachineInstances/default",
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstances_Start

```java
/**
 * Samples for VirtualMachineInstances Start.
 */
public final class VirtualMachineInstancesStartSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/VirtualMachineInstances_Start.json
     */
    /**
     * Sample code: StartVirtualMachine.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void startVirtualMachine(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.virtualMachineInstances()
            .start(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM/providers/Microsoft.AzureStackHCI/virtualMachineInstances/default",
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstances_Stop

```java
/**
 * Samples for VirtualMachineInstances Stop.
 */
public final class VirtualMachineInstancesStopSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/VirtualMachineInstances_Stop.json
     */
    /**
     * Sample code: StopVirtualMachine.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void stopVirtualMachine(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.virtualMachineInstances()
            .stop(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM/providers/Microsoft.AzureStackHCI/virtualMachineInstances/default",
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstances_Update

```java
import com.azure.resourcemanager.azurestackhci.vm.models.StorageProfileUpdate;
import com.azure.resourcemanager.azurestackhci.vm.models.VirtualHardDiskArmReference;
import com.azure.resourcemanager.azurestackhci.vm.models.VirtualMachineInstanceUpdateProperties;
import com.azure.resourcemanager.azurestackhci.vm.models.VirtualMachineInstanceUpdateRequest;
import java.util.Arrays;

/**
 * Samples for VirtualMachineInstances Update.
 */
public final class VirtualMachineInstancesUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-01-preview/VirtualMachineInstances_Update.json
     */
    /**
     * Sample code: UpdateVirtualMachine.
     * 
     * @param manager Entry point to AzureStackHciVmManager.
     */
    public static void updateVirtualMachine(com.azure.resourcemanager.azurestackhci.vm.AzureStackHciVmManager manager) {
        manager.virtualMachineInstances()
            .update(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.HybridCompute/machines/DemoVM",
                new VirtualMachineInstanceUpdateRequest()
                    .withProperties(new VirtualMachineInstanceUpdateProperties().withStorageProfile(
                        new StorageProfileUpdate().withDataDisks(Arrays.asList(new VirtualHardDiskArmReference().withId(
                            "/subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.AzureStackHCI/virtualHardDisks/test-vhd"))))),
                com.azure.core.util.Context.NONE);
    }
}
```

