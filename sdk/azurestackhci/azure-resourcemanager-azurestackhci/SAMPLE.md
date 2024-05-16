# Code snippets and samples


## GalleryImagesOperation

- [CreateOrUpdate](#galleryimagesoperation_createorupdate)
- [Delete](#galleryimagesoperation_delete)
- [GetByResourceGroup](#galleryimagesoperation_getbyresourcegroup)
- [List](#galleryimagesoperation_list)
- [ListByResourceGroup](#galleryimagesoperation_listbyresourcegroup)
- [Update](#galleryimagesoperation_update)

## GuestAgent

- [Create](#guestagent_create)
- [Delete](#guestagent_delete)
- [Get](#guestagent_get)

## GuestAgentsOperation

- [List](#guestagentsoperation_list)

## HybridIdentityMetadata

- [Get](#hybrididentitymetadata_get)
- [List](#hybrididentitymetadata_list)

## LogicalNetworksOperation

- [CreateOrUpdate](#logicalnetworksoperation_createorupdate)
- [Delete](#logicalnetworksoperation_delete)
- [GetByResourceGroup](#logicalnetworksoperation_getbyresourcegroup)
- [List](#logicalnetworksoperation_list)
- [ListByResourceGroup](#logicalnetworksoperation_listbyresourcegroup)
- [Update](#logicalnetworksoperation_update)

## MarketplaceGalleryImagesOperation

- [CreateOrUpdate](#marketplacegalleryimagesoperation_createorupdate)
- [Delete](#marketplacegalleryimagesoperation_delete)
- [GetByResourceGroup](#marketplacegalleryimagesoperation_getbyresourcegroup)
- [List](#marketplacegalleryimagesoperation_list)
- [ListByResourceGroup](#marketplacegalleryimagesoperation_listbyresourcegroup)
- [Update](#marketplacegalleryimagesoperation_update)

## NetworkInterfacesOperation

- [CreateOrUpdate](#networkinterfacesoperation_createorupdate)
- [Delete](#networkinterfacesoperation_delete)
- [GetByResourceGroup](#networkinterfacesoperation_getbyresourcegroup)
- [List](#networkinterfacesoperation_list)
- [ListByResourceGroup](#networkinterfacesoperation_listbyresourcegroup)
- [Update](#networkinterfacesoperation_update)

## Operations

- [List](#operations_list)

## StorageContainersOperation

- [CreateOrUpdate](#storagecontainersoperation_createorupdate)
- [Delete](#storagecontainersoperation_delete)
- [GetByResourceGroup](#storagecontainersoperation_getbyresourcegroup)
- [List](#storagecontainersoperation_list)
- [ListByResourceGroup](#storagecontainersoperation_listbyresourcegroup)
- [Update](#storagecontainersoperation_update)

## VirtualHardDisksOperation

- [CreateOrUpdate](#virtualharddisksoperation_createorupdate)
- [Delete](#virtualharddisksoperation_delete)
- [GetByResourceGroup](#virtualharddisksoperation_getbyresourcegroup)
- [List](#virtualharddisksoperation_list)
- [ListByResourceGroup](#virtualharddisksoperation_listbyresourcegroup)
- [Update](#virtualharddisksoperation_update)

## VirtualMachineInstances

- [CreateOrUpdate](#virtualmachineinstances_createorupdate)
- [Delete](#virtualmachineinstances_delete)
- [Get](#virtualmachineinstances_get)
- [List](#virtualmachineinstances_list)
- [Restart](#virtualmachineinstances_restart)
- [Start](#virtualmachineinstances_start)
- [Stop](#virtualmachineinstances_stop)
- [Update](#virtualmachineinstances_update)
### GalleryImagesOperation_CreateOrUpdate

```java
import com.azure.resourcemanager.azurestackhci.models.ExtendedLocation;
import com.azure.resourcemanager.azurestackhci.models.ExtendedLocationTypes;
import com.azure.resourcemanager.azurestackhci.models.OperatingSystemTypes;

/** Samples for GalleryImagesOperation CreateOrUpdate. */
public final class GalleryImagesOperationCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/PutGalleryImage.json
     */
    /**
     * Sample code: PutGalleryImage.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void putGalleryImage(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .galleryImagesOperations()
            .define("test-gallery-image")
            .withRegion("West US2")
            .withExistingResourceGroup("test-rg")
            .withExtendedLocation(
                new ExtendedLocation()
                    .withName(
                        "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.ExtendedLocation/customLocations/dogfood-location")
                    .withType(ExtendedLocationTypes.CUSTOM_LOCATION))
            .withContainerId(
                "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.AzureStackHCI/storageContainers/test-storage-container")
            .withImagePath("C:\\test.vhdx")
            .withOsType(OperatingSystemTypes.LINUX)
            .create();
    }
}
```

### GalleryImagesOperation_Delete

```java
/** Samples for GalleryImagesOperation Delete. */
public final class GalleryImagesOperationDeleteSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/DeleteGalleryImage.json
     */
    /**
     * Sample code: DeleteGalleryImage.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void deleteGalleryImage(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.galleryImagesOperations().delete("test-rg", "test-gallery-image", com.azure.core.util.Context.NONE);
    }
}
```

### GalleryImagesOperation_GetByResourceGroup

```java
/** Samples for GalleryImagesOperation GetByResourceGroup. */
public final class GalleryImagesOperationGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/GetGalleryImage.json
     */
    /**
     * Sample code: GetGalleryImage.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void getGalleryImage(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .galleryImagesOperations()
            .getByResourceGroupWithResponse("test-rg", "test-gallery-image", com.azure.core.util.Context.NONE);
    }
}
```

### GalleryImagesOperation_List

```java
/** Samples for GalleryImagesOperation List. */
public final class GalleryImagesOperationListSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/ListGalleryImageBySubscription.json
     */
    /**
     * Sample code: ListGalleryImageBySubscription.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void listGalleryImageBySubscription(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.galleryImagesOperations().list(com.azure.core.util.Context.NONE);
    }
}
```

### GalleryImagesOperation_ListByResourceGroup

```java
/** Samples for GalleryImagesOperation ListByResourceGroup. */
public final class GalleryImagesOperationListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/ListGalleryImageByResourceGroup.json
     */
    /**
     * Sample code: ListGalleryImageByResourceGroup.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void listGalleryImageByResourceGroup(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.galleryImagesOperations().listByResourceGroup("test-rg", com.azure.core.util.Context.NONE);
    }
}
```

### GalleryImagesOperation_Update

```java
import com.azure.resourcemanager.azurestackhci.models.GalleryImages;
import java.util.HashMap;
import java.util.Map;

/** Samples for GalleryImagesOperation Update. */
public final class GalleryImagesOperationUpdateSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/UpdateGalleryImage.json
     */
    /**
     * Sample code: UpdateGalleryImage.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void updateGalleryImage(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        GalleryImages resource =
            manager
                .galleryImagesOperations()
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

### GuestAgent_Create

```java
import com.azure.resourcemanager.azurestackhci.fluent.models.GuestAgentInner;
import com.azure.resourcemanager.azurestackhci.models.GuestCredential;
import com.azure.resourcemanager.azurestackhci.models.ProvisioningAction;

/** Samples for GuestAgent Create. */
public final class GuestAgentCreateSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/CreateGuestAgent.json
     */
    /**
     * Sample code: CreateGuestAgent.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void createGuestAgent(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .guestAgents()
            .create(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/Microsoft.HybridCompute/machines/DemoVM",
                new GuestAgentInner()
                    .withCredentials(
                        new GuestCredential().withUsername("tempuser").withPassword("fakeTokenPlaceholder"))
                    .withProvisioningAction(ProvisioningAction.INSTALL),
                com.azure.core.util.Context.NONE);
    }
}
```

### GuestAgent_Delete

```java
/** Samples for GuestAgent Delete. */
public final class GuestAgentDeleteSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/DeleteGuestAgent.json
     */
    /**
     * Sample code: DeleteGuestAgent.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void deleteGuestAgent(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .guestAgents()
            .delete(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/Microsoft.HybridCompute/machines/DemoVM",
                com.azure.core.util.Context.NONE);
    }
}
```

### GuestAgent_Get

```java
/** Samples for GuestAgent Get. */
public final class GuestAgentGetSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/GetGuestAgent.json
     */
    /**
     * Sample code: GetGuestAgent.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void getGuestAgent(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .guestAgents()
            .getWithResponse(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/Microsoft.HybridCompute/machines/DemoVM",
                com.azure.core.util.Context.NONE);
    }
}
```

### GuestAgentsOperation_List

```java
/** Samples for GuestAgentsOperation List. */
public final class GuestAgentsOperationListSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/GuestAgent_List.json
     */
    /**
     * Sample code: GuestAgentListByVirtualMachineInstances.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void guestAgentListByVirtualMachineInstances(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .guestAgentsOperations()
            .list(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/Microsoft.HybridCompute/machines/DemoVM",
                com.azure.core.util.Context.NONE);
    }
}
```

### HybridIdentityMetadata_Get

```java
/** Samples for HybridIdentityMetadata Get. */
public final class HybridIdentityMetadataGetSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/GetHybridIdentityMetadata.json
     */
    /**
     * Sample code: GetHybridIdentityMetadata.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void getHybridIdentityMetadata(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .hybridIdentityMetadatas()
            .getWithResponse(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/Microsoft.HybridCompute/machines/DemoVM",
                com.azure.core.util.Context.NONE);
    }
}
```

### HybridIdentityMetadata_List

```java
/** Samples for HybridIdentityMetadata List. */
public final class HybridIdentityMetadataListSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/HybridIdentityMetadata_List.json
     */
    /**
     * Sample code: HybridIdentityMetadataListByVirtualMachineInstances.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void hybridIdentityMetadataListByVirtualMachineInstances(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .hybridIdentityMetadatas()
            .list(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/Microsoft.HybridCompute/machines/DemoVM",
                com.azure.core.util.Context.NONE);
    }
}
```

### LogicalNetworksOperation_CreateOrUpdate

```java
import com.azure.resourcemanager.azurestackhci.models.ExtendedLocation;
import com.azure.resourcemanager.azurestackhci.models.ExtendedLocationTypes;

/** Samples for LogicalNetworksOperation CreateOrUpdate. */
public final class LogicalNetworksOperationCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/PutLogicalNetwork.json
     */
    /**
     * Sample code: PutLogicalNetwork.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void putLogicalNetwork(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .logicalNetworksOperations()
            .define("test-lnet")
            .withRegion("West US2")
            .withExistingResourceGroup("test-rg")
            .withExtendedLocation(
                new ExtendedLocation()
                    .withName(
                        "/subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/dogfoodarc/providers/Microsoft.ExtendedLocation/customLocations/dogfood-location")
                    .withType(ExtendedLocationTypes.CUSTOM_LOCATION))
            .create();
    }
}
```

### LogicalNetworksOperation_Delete

```java
/** Samples for LogicalNetworksOperation Delete. */
public final class LogicalNetworksOperationDeleteSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/DeleteLogicalNetwork.json
     */
    /**
     * Sample code: DeleteLogicalNetwork.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void deleteLogicalNetwork(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.logicalNetworksOperations().delete("test-rg", "test-lnet", com.azure.core.util.Context.NONE);
    }
}
```

### LogicalNetworksOperation_GetByResourceGroup

```java
/** Samples for LogicalNetworksOperation GetByResourceGroup. */
public final class LogicalNetworksOperationGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/GetLogicalNetwork.json
     */
    /**
     * Sample code: GetLogicalNetwork.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void getLogicalNetwork(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .logicalNetworksOperations()
            .getByResourceGroupWithResponse("test-rg", "test-lnet", com.azure.core.util.Context.NONE);
    }
}
```

### LogicalNetworksOperation_List

```java
/** Samples for LogicalNetworksOperation List. */
public final class LogicalNetworksOperationListSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/ListLogicalNetworkBySubscription.json
     */
    /**
     * Sample code: ListLogicalNetworkBySubscription.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void listLogicalNetworkBySubscription(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.logicalNetworksOperations().list(com.azure.core.util.Context.NONE);
    }
}
```

### LogicalNetworksOperation_ListByResourceGroup

```java
/** Samples for LogicalNetworksOperation ListByResourceGroup. */
public final class LogicalNetworksOperationListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/ListLogicalNetworkByResourceGroup.json
     */
    /**
     * Sample code: ListLogicalNetworkByResourceGroup.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void listLogicalNetworkByResourceGroup(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.logicalNetworksOperations().listByResourceGroup("test-rg", com.azure.core.util.Context.NONE);
    }
}
```

### LogicalNetworksOperation_Update

```java
import com.azure.resourcemanager.azurestackhci.models.LogicalNetworks;
import java.util.HashMap;
import java.util.Map;

/** Samples for LogicalNetworksOperation Update. */
public final class LogicalNetworksOperationUpdateSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/UpdateLogicalNetwork.json
     */
    /**
     * Sample code: UpdateLogicalNetwork.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void updateLogicalNetwork(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        LogicalNetworks resource =
            manager
                .logicalNetworksOperations()
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

### MarketplaceGalleryImagesOperation_CreateOrUpdate

```java
import com.azure.resourcemanager.azurestackhci.models.CloudInitDataSource;
import com.azure.resourcemanager.azurestackhci.models.ExtendedLocation;
import com.azure.resourcemanager.azurestackhci.models.ExtendedLocationTypes;
import com.azure.resourcemanager.azurestackhci.models.GalleryImageIdentifier;
import com.azure.resourcemanager.azurestackhci.models.GalleryImageVersion;
import com.azure.resourcemanager.azurestackhci.models.HyperVGeneration;
import com.azure.resourcemanager.azurestackhci.models.OperatingSystemTypes;

/** Samples for MarketplaceGalleryImagesOperation CreateOrUpdate. */
public final class MarketplaceGalleryImagesOperationCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/PutMarketplaceGalleryImage.json
     */
    /**
     * Sample code: PutMarketplaceGalleryImage.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void putMarketplaceGalleryImage(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .marketplaceGalleryImagesOperations()
            .define("test-marketplace-gallery-image")
            .withRegion("West US2")
            .withExistingResourceGroup("test-rg")
            .withExtendedLocation(
                new ExtendedLocation()
                    .withName(
                        "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.ExtendedLocation/customLocations/dogfood-location")
                    .withType(ExtendedLocationTypes.CUSTOM_LOCATION))
            .withContainerId(
                "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.AzureStackHCI/storageContainers/test-storage-container")
            .withOsType(OperatingSystemTypes.WINDOWS)
            .withCloudInitDataSource(CloudInitDataSource.AZURE)
            .withHyperVGeneration(HyperVGeneration.V1)
            .withIdentifier(
                new GalleryImageIdentifier()
                    .withPublisher("myPublisherName")
                    .withOffer("myOfferName")
                    .withSku("mySkuName"))
            .withVersion(new GalleryImageVersion().withName("1.0.0"))
            .create();
    }
}
```

### MarketplaceGalleryImagesOperation_Delete

```java
/** Samples for MarketplaceGalleryImagesOperation Delete. */
public final class MarketplaceGalleryImagesOperationDeleteSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/DeleteMarketplaceGalleryImage.json
     */
    /**
     * Sample code: DeleteMarketplaceGalleryImage.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void deleteMarketplaceGalleryImage(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .marketplaceGalleryImagesOperations()
            .delete("test-rg", "test-marketplace-gallery-image", com.azure.core.util.Context.NONE);
    }
}
```

### MarketplaceGalleryImagesOperation_GetByResourceGroup

```java
/** Samples for MarketplaceGalleryImagesOperation GetByResourceGroup. */
public final class MarketplaceGalleryImagesOperationGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/GetMarketplaceGalleryImage.json
     */
    /**
     * Sample code: GetMarketplaceGalleryImage.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void getMarketplaceGalleryImage(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .marketplaceGalleryImagesOperations()
            .getByResourceGroupWithResponse(
                "test-rg", "test-marketplace-gallery-image", com.azure.core.util.Context.NONE);
    }
}
```

### MarketplaceGalleryImagesOperation_List

```java
/** Samples for MarketplaceGalleryImagesOperation List. */
public final class MarketplaceGalleryImagesOperationListSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/ListMarketplaceGalleryImageBySubscription.json
     */
    /**
     * Sample code: ListMarketplaceGalleryImageBySubscription.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void listMarketplaceGalleryImageBySubscription(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.marketplaceGalleryImagesOperations().list(com.azure.core.util.Context.NONE);
    }
}
```

### MarketplaceGalleryImagesOperation_ListByResourceGroup

```java
/** Samples for MarketplaceGalleryImagesOperation ListByResourceGroup. */
public final class MarketplaceGalleryImagesOperationListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/ListMarketplaceGalleryImageByResourceGroup.json
     */
    /**
     * Sample code: ListMarketplaceGalleryImageByResourceGroup.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void listMarketplaceGalleryImageByResourceGroup(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.marketplaceGalleryImagesOperations().listByResourceGroup("test-rg", com.azure.core.util.Context.NONE);
    }
}
```

### MarketplaceGalleryImagesOperation_Update

```java
import com.azure.resourcemanager.azurestackhci.models.MarketplaceGalleryImages;
import java.util.HashMap;
import java.util.Map;

/** Samples for MarketplaceGalleryImagesOperation Update. */
public final class MarketplaceGalleryImagesOperationUpdateSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/UpdateMarketplaceGalleryImage.json
     */
    /**
     * Sample code: UpdateMarketplaceGalleryImage.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void updateMarketplaceGalleryImage(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        MarketplaceGalleryImages resource =
            manager
                .marketplaceGalleryImagesOperations()
                .getByResourceGroupWithResponse(
                    "test-rg", "test-marketplce-gallery-image", com.azure.core.util.Context.NONE)
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

### NetworkInterfacesOperation_CreateOrUpdate

```java
import com.azure.resourcemanager.azurestackhci.models.ExtendedLocation;
import com.azure.resourcemanager.azurestackhci.models.ExtendedLocationTypes;
import com.azure.resourcemanager.azurestackhci.models.IpConfiguration;
import com.azure.resourcemanager.azurestackhci.models.IpConfigurationProperties;
import com.azure.resourcemanager.azurestackhci.models.IpConfigurationPropertiesSubnet;
import java.util.Arrays;

/** Samples for NetworkInterfacesOperation CreateOrUpdate. */
public final class NetworkInterfacesOperationCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/PutNetworkInterface.json
     */
    /**
     * Sample code: PutNetworkInterface.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void putNetworkInterface(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .networkInterfacesOperations()
            .define("test-nic")
            .withRegion("West US2")
            .withExistingResourceGroup("test-rg")
            .withExtendedLocation(
                new ExtendedLocation()
                    .withName(
                        "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.ExtendedLocation/customLocations/dogfood-location")
                    .withType(ExtendedLocationTypes.CUSTOM_LOCATION))
            .withIpConfigurations(
                Arrays
                    .asList(
                        new IpConfiguration()
                            .withName("ipconfig-sample")
                            .withProperties(
                                new IpConfigurationProperties()
                                    .withSubnet(new IpConfigurationPropertiesSubnet().withId("test-lnet")))))
            .create();
    }
}
```

### NetworkInterfacesOperation_Delete

```java
/** Samples for NetworkInterfacesOperation Delete. */
public final class NetworkInterfacesOperationDeleteSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/DeleteNetworkInterface.json
     */
    /**
     * Sample code: DeleteNetworkInterface.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void deleteNetworkInterface(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.networkInterfacesOperations().delete("test-rg", "test-nic", com.azure.core.util.Context.NONE);
    }
}
```

### NetworkInterfacesOperation_GetByResourceGroup

```java
/** Samples for NetworkInterfacesOperation GetByResourceGroup. */
public final class NetworkInterfacesOperationGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/GetNetworkInterface.json
     */
    /**
     * Sample code: GetNetworkInterface.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void getNetworkInterface(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .networkInterfacesOperations()
            .getByResourceGroupWithResponse("test-rg", "test-nic", com.azure.core.util.Context.NONE);
    }
}
```

### NetworkInterfacesOperation_List

```java
/** Samples for NetworkInterfacesOperation List. */
public final class NetworkInterfacesOperationListSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/ListNetworkInterfaceBySubscription.json
     */
    /**
     * Sample code: ListNetworkInterfaceBySubscription.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void listNetworkInterfaceBySubscription(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.networkInterfacesOperations().list(com.azure.core.util.Context.NONE);
    }
}
```

### NetworkInterfacesOperation_ListByResourceGroup

```java
/** Samples for NetworkInterfacesOperation ListByResourceGroup. */
public final class NetworkInterfacesOperationListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/ListNetworkInterfaceByResourceGroup.json
     */
    /**
     * Sample code: ListNetworkInterfaceByResourceGroup.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void listNetworkInterfaceByResourceGroup(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.networkInterfacesOperations().listByResourceGroup("test-rg", com.azure.core.util.Context.NONE);
    }
}
```

### NetworkInterfacesOperation_Update

```java
import com.azure.resourcemanager.azurestackhci.models.NetworkInterfaces;
import java.util.HashMap;
import java.util.Map;

/** Samples for NetworkInterfacesOperation Update. */
public final class NetworkInterfacesOperationUpdateSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/UpdateNetworkInterface.json
     */
    /**
     * Sample code: UpdateNetworkInterface.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void updateNetworkInterface(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        NetworkInterfaces resource =
            manager
                .networkInterfacesOperations()
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

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/ListOperations.json
     */
    /**
     * Sample code: ListOperations.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void listOperations(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### StorageContainersOperation_CreateOrUpdate

```java
import com.azure.resourcemanager.azurestackhci.models.ExtendedLocation;
import com.azure.resourcemanager.azurestackhci.models.ExtendedLocationTypes;

/** Samples for StorageContainersOperation CreateOrUpdate. */
public final class StorageContainersOperationCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/PutStorageContainer.json
     */
    /**
     * Sample code: PutStorageContainer.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void putStorageContainer(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .storageContainersOperations()
            .define("Default_Container")
            .withRegion("West US2")
            .withExistingResourceGroup("test-rg")
            .withExtendedLocation(
                new ExtendedLocation()
                    .withName(
                        "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.ExtendedLocation/customLocations/dogfood-location")
                    .withType(ExtendedLocationTypes.CUSTOM_LOCATION))
            .withPath("C:\\container_storage")
            .create();
    }
}
```

### StorageContainersOperation_Delete

```java
/** Samples for StorageContainersOperation Delete. */
public final class StorageContainersOperationDeleteSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/DeleteStorageContainer.json
     */
    /**
     * Sample code: DeleteStorageContainer.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void deleteStorageContainer(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.storageContainersOperations().delete("test-rg", "Default_Container", com.azure.core.util.Context.NONE);
    }
}
```

### StorageContainersOperation_GetByResourceGroup

```java
/** Samples for StorageContainersOperation GetByResourceGroup. */
public final class StorageContainersOperationGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/GetStorageContainer.json
     */
    /**
     * Sample code: GetStorageContainer.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void getStorageContainer(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .storageContainersOperations()
            .getByResourceGroupWithResponse("test-rg", "Default_Container", com.azure.core.util.Context.NONE);
    }
}
```

### StorageContainersOperation_List

```java
/** Samples for StorageContainersOperation List. */
public final class StorageContainersOperationListSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/ListStorageContainerBySubscription.json
     */
    /**
     * Sample code: ListStorageContainerBySubscription.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void listStorageContainerBySubscription(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.storageContainersOperations().list(com.azure.core.util.Context.NONE);
    }
}
```

### StorageContainersOperation_ListByResourceGroup

```java
/** Samples for StorageContainersOperation ListByResourceGroup. */
public final class StorageContainersOperationListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/ListStorageContainerByResourceGroup.json
     */
    /**
     * Sample code: ListStorageContainerByResourceGroup.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void listStorageContainerByResourceGroup(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.storageContainersOperations().listByResourceGroup("test-rg", com.azure.core.util.Context.NONE);
    }
}
```

### StorageContainersOperation_Update

```java
import com.azure.resourcemanager.azurestackhci.models.StorageContainers;
import java.util.HashMap;
import java.util.Map;

/** Samples for StorageContainersOperation Update. */
public final class StorageContainersOperationUpdateSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/UpdateStorageContainer.json
     */
    /**
     * Sample code: UpdateStorageContainer.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void updateStorageContainer(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        StorageContainers resource =
            manager
                .storageContainersOperations()
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

### VirtualHardDisksOperation_CreateOrUpdate

```java
import com.azure.resourcemanager.azurestackhci.models.ExtendedLocation;
import com.azure.resourcemanager.azurestackhci.models.ExtendedLocationTypes;

/** Samples for VirtualHardDisksOperation CreateOrUpdate. */
public final class VirtualHardDisksOperationCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/PutVirtualHardDisk.json
     */
    /**
     * Sample code: PutVirtualHardDisk.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void putVirtualHardDisk(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .virtualHardDisksOperations()
            .define("test-vhd")
            .withRegion("West US2")
            .withExistingResourceGroup("test-rg")
            .withExtendedLocation(
                new ExtendedLocation()
                    .withName(
                        "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.ExtendedLocation/customLocations/dogfood-location")
                    .withType(ExtendedLocationTypes.CUSTOM_LOCATION))
            .withDiskSizeGB(32L)
            .create();
    }
}
```

### VirtualHardDisksOperation_Delete

```java
/** Samples for VirtualHardDisksOperation Delete. */
public final class VirtualHardDisksOperationDeleteSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/DeleteVirtualHardDisk.json
     */
    /**
     * Sample code: DeleteVirtualHardDisk.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void deleteVirtualHardDisk(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.virtualHardDisksOperations().delete("test-rg", "test-vhd", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualHardDisksOperation_GetByResourceGroup

```java
/** Samples for VirtualHardDisksOperation GetByResourceGroup. */
public final class VirtualHardDisksOperationGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/GetVirtualHardDisk.json
     */
    /**
     * Sample code: GetVirtualHardDisk.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void getVirtualHardDisk(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .virtualHardDisksOperations()
            .getByResourceGroupWithResponse("test-rg", "test-vhd", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualHardDisksOperation_List

```java
/** Samples for VirtualHardDisksOperation List. */
public final class VirtualHardDisksOperationListSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/ListVirtualHardDiskBySubscription.json
     */
    /**
     * Sample code: ListVirtualHardDiskBySubscription.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void listVirtualHardDiskBySubscription(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.virtualHardDisksOperations().list(com.azure.core.util.Context.NONE);
    }
}
```

### VirtualHardDisksOperation_ListByResourceGroup

```java
/** Samples for VirtualHardDisksOperation ListByResourceGroup. */
public final class VirtualHardDisksOperationListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/ListVirtualHardDiskByResourceGroup.json
     */
    /**
     * Sample code: ListVirtualHardDiskByResourceGroup.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void listVirtualHardDiskByResourceGroup(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.virtualHardDisksOperations().listByResourceGroup("test-rg", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualHardDisksOperation_Update

```java
import com.azure.resourcemanager.azurestackhci.models.VirtualHardDisks;
import java.util.HashMap;
import java.util.Map;

/** Samples for VirtualHardDisksOperation Update. */
public final class VirtualHardDisksOperationUpdateSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/UpdateVirtualHardDisk.json
     */
    /**
     * Sample code: UpdateVirtualHardDisk.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void updateVirtualHardDisk(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        VirtualHardDisks resource =
            manager
                .virtualHardDisksOperations()
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

### VirtualMachineInstances_CreateOrUpdate

```java
import com.azure.resourcemanager.azurestackhci.fluent.models.VirtualMachineInstanceInner;
import com.azure.resourcemanager.azurestackhci.models.ExtendedLocation;
import com.azure.resourcemanager.azurestackhci.models.ExtendedLocationTypes;
import com.azure.resourcemanager.azurestackhci.models.VirtualMachineInstancePropertiesHardwareProfile;
import com.azure.resourcemanager.azurestackhci.models.VirtualMachineInstancePropertiesNetworkProfile;
import com.azure.resourcemanager.azurestackhci.models.VirtualMachineInstancePropertiesNetworkProfileNetworkInterfacesItem;
import com.azure.resourcemanager.azurestackhci.models.VirtualMachineInstancePropertiesOsProfile;
import com.azure.resourcemanager.azurestackhci.models.VirtualMachineInstancePropertiesOsProfileWindowsConfiguration;
import com.azure.resourcemanager.azurestackhci.models.VirtualMachineInstancePropertiesSecurityProfile;
import com.azure.resourcemanager.azurestackhci.models.VirtualMachineInstancePropertiesSecurityProfileUefiSettings;
import com.azure.resourcemanager.azurestackhci.models.VirtualMachineInstancePropertiesStorageProfile;
import com.azure.resourcemanager.azurestackhci.models.VirtualMachineInstancePropertiesStorageProfileImageReference;
import com.azure.resourcemanager.azurestackhci.models.VirtualMachineInstancePropertiesStorageProfileOsDisk;
import com.azure.resourcemanager.azurestackhci.models.VmSizeEnum;
import java.util.Arrays;

/** Samples for VirtualMachineInstances CreateOrUpdate. */
public final class VirtualMachineInstancesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/PutVirtualMachineInstanceWithGalleryImage.json
     */
    /**
     * Sample code: PutVirtualMachineInstanceWithGalleryImage.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void putVirtualMachineInstanceWithGalleryImage(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .virtualMachineInstances()
            .createOrUpdate(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/Microsoft.HybridCompute/machines/DemoVM",
                new VirtualMachineInstanceInner()
                    .withExtendedLocation(
                        new ExtendedLocation()
                            .withName(
                                "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.ExtendedLocation/customLocations/dogfood-location")
                            .withType(ExtendedLocationTypes.CUSTOM_LOCATION))
                    .withHardwareProfile(
                        new VirtualMachineInstancePropertiesHardwareProfile().withVmSize(VmSizeEnum.DEFAULT))
                    .withNetworkProfile(
                        new VirtualMachineInstancePropertiesNetworkProfile()
                            .withNetworkInterfaces(
                                Arrays
                                    .asList(
                                        new VirtualMachineInstancePropertiesNetworkProfileNetworkInterfacesItem()
                                            .withId("test-nic"))))
                    .withOsProfile(
                        new VirtualMachineInstancePropertiesOsProfile()
                            .withAdminPassword("fakeTokenPlaceholder")
                            .withAdminUsername("localadmin")
                            .withComputerName("luamaster"))
                    .withSecurityProfile(
                        new VirtualMachineInstancePropertiesSecurityProfile()
                            .withEnableTpm(true)
                            .withUefiSettings(
                                new VirtualMachineInstancePropertiesSecurityProfileUefiSettings()
                                    .withSecureBootEnabled(true)))
                    .withStorageProfile(
                        new VirtualMachineInstancePropertiesStorageProfile()
                            .withImageReference(
                                new VirtualMachineInstancePropertiesStorageProfileImageReference()
                                    .withId(
                                        "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.AzureStackHCI/galleryImages/test-gallery-image"))
                            .withVmConfigStoragePathId(
                                "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.AzureStackHCI/storageContainers/test-container")),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/PutVirtualMachineInstanceWithOsDisk.json
     */
    /**
     * Sample code: PutVirtualMachineInstanceWithOsDisk.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void putVirtualMachineInstanceWithOsDisk(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .virtualMachineInstances()
            .createOrUpdate(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/Microsoft.HybridCompute/machines/DemoVM",
                new VirtualMachineInstanceInner()
                    .withExtendedLocation(
                        new ExtendedLocation()
                            .withName(
                                "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.ExtendedLocation/customLocations/dogfood-location")
                            .withType(ExtendedLocationTypes.CUSTOM_LOCATION))
                    .withHardwareProfile(
                        new VirtualMachineInstancePropertiesHardwareProfile().withVmSize(VmSizeEnum.DEFAULT))
                    .withNetworkProfile(
                        new VirtualMachineInstancePropertiesNetworkProfile()
                            .withNetworkInterfaces(
                                Arrays
                                    .asList(
                                        new VirtualMachineInstancePropertiesNetworkProfileNetworkInterfacesItem()
                                            .withId("test-nic"))))
                    .withSecurityProfile(
                        new VirtualMachineInstancePropertiesSecurityProfile()
                            .withEnableTpm(true)
                            .withUefiSettings(
                                new VirtualMachineInstancePropertiesSecurityProfileUefiSettings()
                                    .withSecureBootEnabled(true)))
                    .withStorageProfile(
                        new VirtualMachineInstancePropertiesStorageProfile()
                            .withOsDisk(
                                new VirtualMachineInstancePropertiesStorageProfileOsDisk()
                                    .withId(
                                        "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.AzureStackHCI/virtualHardDisks/test-vhd"))
                            .withVmConfigStoragePathId(
                                "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.AzureStackHCI/storageContainers/test-container")),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/PutVirtualMachineInstanceWithMarketplaceGalleryImage.json
     */
    /**
     * Sample code: PutVirtualMachineInstanceWithMarketplaceGalleryImage.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void putVirtualMachineInstanceWithMarketplaceGalleryImage(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .virtualMachineInstances()
            .createOrUpdate(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/Microsoft.HybridCompute/machines/DemoVM",
                new VirtualMachineInstanceInner()
                    .withExtendedLocation(
                        new ExtendedLocation()
                            .withName(
                                "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.ExtendedLocation/customLocations/dogfood-location")
                            .withType(ExtendedLocationTypes.CUSTOM_LOCATION))
                    .withHardwareProfile(
                        new VirtualMachineInstancePropertiesHardwareProfile().withVmSize(VmSizeEnum.DEFAULT))
                    .withNetworkProfile(
                        new VirtualMachineInstancePropertiesNetworkProfile()
                            .withNetworkInterfaces(
                                Arrays
                                    .asList(
                                        new VirtualMachineInstancePropertiesNetworkProfileNetworkInterfacesItem()
                                            .withId("test-nic"))))
                    .withOsProfile(
                        new VirtualMachineInstancePropertiesOsProfile()
                            .withAdminPassword("fakeTokenPlaceholder")
                            .withAdminUsername("localadmin")
                            .withComputerName("luamaster"))
                    .withSecurityProfile(
                        new VirtualMachineInstancePropertiesSecurityProfile()
                            .withEnableTpm(true)
                            .withUefiSettings(
                                new VirtualMachineInstancePropertiesSecurityProfileUefiSettings()
                                    .withSecureBootEnabled(true)))
                    .withStorageProfile(
                        new VirtualMachineInstancePropertiesStorageProfile()
                            .withImageReference(
                                new VirtualMachineInstancePropertiesStorageProfileImageReference()
                                    .withId(
                                        "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.AzureStackHCI/marketplaceGalleryImages/test-marketplace-gallery-image"))
                            .withVmConfigStoragePathId(
                                "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.AzureStackHCI/storageContainers/test-container")),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/PutVirtualMachineInstanceWithVMConfigAgent.json
     */
    /**
     * Sample code: PutVirtualMachineInstanceWithVMConfigAgent.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void putVirtualMachineInstanceWithVMConfigAgent(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .virtualMachineInstances()
            .createOrUpdate(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/Microsoft.HybridCompute/machines/DemoVM",
                new VirtualMachineInstanceInner()
                    .withExtendedLocation(
                        new ExtendedLocation()
                            .withName(
                                "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.ExtendedLocation/customLocations/dogfood-location")
                            .withType(ExtendedLocationTypes.CUSTOM_LOCATION))
                    .withHardwareProfile(
                        new VirtualMachineInstancePropertiesHardwareProfile().withVmSize(VmSizeEnum.DEFAULT))
                    .withNetworkProfile(
                        new VirtualMachineInstancePropertiesNetworkProfile()
                            .withNetworkInterfaces(
                                Arrays
                                    .asList(
                                        new VirtualMachineInstancePropertiesNetworkProfileNetworkInterfacesItem()
                                            .withId("test-nic"))))
                    .withOsProfile(
                        new VirtualMachineInstancePropertiesOsProfile()
                            .withAdminPassword("fakeTokenPlaceholder")
                            .withAdminUsername("localadmin")
                            .withComputerName("luamaster")
                            .withWindowsConfiguration(
                                new VirtualMachineInstancePropertiesOsProfileWindowsConfiguration()
                                    .withProvisionVMConfigAgent(true)))
                    .withSecurityProfile(
                        new VirtualMachineInstancePropertiesSecurityProfile()
                            .withEnableTpm(true)
                            .withUefiSettings(
                                new VirtualMachineInstancePropertiesSecurityProfileUefiSettings()
                                    .withSecureBootEnabled(true)))
                    .withStorageProfile(
                        new VirtualMachineInstancePropertiesStorageProfile()
                            .withImageReference(
                                new VirtualMachineInstancePropertiesStorageProfileImageReference()
                                    .withId(
                                        "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.AzureStackHCI/galleryImages/test-gallery-image"))
                            .withVmConfigStoragePathId(
                                "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.AzureStackHCI/storageContainers/test-container")),
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstances_Delete

```java
/** Samples for VirtualMachineInstances Delete. */
public final class VirtualMachineInstancesDeleteSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/DeleteVirtualMachineInstance.json
     */
    /**
     * Sample code: DeleteVirtualMachine.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void deleteVirtualMachine(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .virtualMachineInstances()
            .delete(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/Microsoft.HybridCompute/machines/DemoVM",
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstances_Get

```java
/** Samples for VirtualMachineInstances Get. */
public final class VirtualMachineInstancesGetSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/GetVirtualMachineInstance.json
     */
    /**
     * Sample code: GetVirtualMachineInstance.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void getVirtualMachineInstance(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .virtualMachineInstances()
            .getWithResponse(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/Microsoft.HybridCompute/machines/DemoVM",
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstances_List

```java
/** Samples for VirtualMachineInstances List. */
public final class VirtualMachineInstancesListSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/ListVirtualMachineInstances.json
     */
    /**
     * Sample code: ListVirtualMachineInstances.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void listVirtualMachineInstances(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .virtualMachineInstances()
            .list(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/Microsoft.HybridCompute/machines/DemoVM",
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstances_Restart

```java
/** Samples for VirtualMachineInstances Restart. */
public final class VirtualMachineInstancesRestartSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/RestartVirtualMachineInstance.json
     */
    /**
     * Sample code: RestartVirtualMachine.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void restartVirtualMachine(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .virtualMachineInstances()
            .restart(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/Microsoft.HybridCompute/machines/DemoVM/providers/Microsoft.AzureStackHCI/virtualMachineInstances/default",
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstances_Start

```java
/** Samples for VirtualMachineInstances Start. */
public final class VirtualMachineInstancesStartSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/StartVirtualMachineInstance.json
     */
    /**
     * Sample code: StartVirtualMachine.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void startVirtualMachine(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .virtualMachineInstances()
            .start(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/Microsoft.HybridCompute/machines/DemoVM/providers/Microsoft.AzureStackHCI/virtualMachineInstances/default",
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstances_Stop

```java
/** Samples for VirtualMachineInstances Stop. */
public final class VirtualMachineInstancesStopSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/StopVirtualMachineInstance.json
     */
    /**
     * Sample code: StopVirtualMachine.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void stopVirtualMachine(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .virtualMachineInstances()
            .stop(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/Microsoft.HybridCompute/machines/DemoVM/providers/Microsoft.AzureStackHCI/virtualMachineInstances/default",
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachineInstances_Update

```java
import com.azure.resourcemanager.azurestackhci.models.StorageProfileUpdate;
import com.azure.resourcemanager.azurestackhci.models.StorageProfileUpdateDataDisksItem;
import com.azure.resourcemanager.azurestackhci.models.VirtualMachineInstanceUpdateProperties;
import com.azure.resourcemanager.azurestackhci.models.VirtualMachineInstanceUpdateRequest;
import java.util.Arrays;

/** Samples for VirtualMachineInstances Update. */
public final class VirtualMachineInstancesUpdateSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2023-09-01-preview/examples/UpdateVirtualMachineInstance.json
     */
    /**
     * Sample code: UpdateVirtualMachine.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void updateVirtualMachine(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .virtualMachineInstances()
            .update(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/Microsoft.HybridCompute/machines/DemoVM",
                new VirtualMachineInstanceUpdateRequest()
                    .withProperties(
                        new VirtualMachineInstanceUpdateProperties()
                            .withStorageProfile(
                                new StorageProfileUpdate()
                                    .withDataDisks(
                                        Arrays
                                            .asList(
                                                new StorageProfileUpdateDataDisksItem()
                                                    .withId(
                                                        "/subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.AzureStackHCI/virtualHardDisks/test-vhd"))))),
                com.azure.core.util.Context.NONE);
    }
}
```

