# Code snippets and samples


## AssetEndpointProfiles

- [CreateOrReplace](#assetendpointprofiles_createorreplace)
- [Delete](#assetendpointprofiles_delete)
- [GetByResourceGroup](#assetendpointprofiles_getbyresourcegroup)
- [List](#assetendpointprofiles_list)
- [ListByResourceGroup](#assetendpointprofiles_listbyresourcegroup)
- [Update](#assetendpointprofiles_update)

## Assets

- [CreateOrReplace](#assets_createorreplace)
- [Delete](#assets_delete)
- [GetByResourceGroup](#assets_getbyresourcegroup)
- [List](#assets_list)
- [ListByResourceGroup](#assets_listbyresourcegroup)
- [Update](#assets_update)

## OperationStatus

- [Get](#operationstatus_get)

## Operations

- [List](#operations_list)
### AssetEndpointProfiles_CreateOrReplace

```java
import com.azure.resourcemanager.deviceregistry.models.AssetEndpointProfileProperties;
import com.azure.resourcemanager.deviceregistry.models.ExtendedLocation;
import com.azure.resourcemanager.deviceregistry.models.UserAuthentication;
import com.azure.resourcemanager.deviceregistry.models.UserAuthenticationMode;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for AssetEndpointProfiles CreateOrReplace.
 */
public final class AssetEndpointProfilesCreateOrReplaceSamples {
    /*
     * x-ms-original-file: 2023-11-01-preview/Create_AssetEndpointProfile.json
     */
    /**
     * Sample code: Create_AssetEndpointProfile.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        createAssetEndpointProfile(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.assetEndpointProfiles()
            .define("my-assetendpointprofile")
            .withRegion("West Europe")
            .withExistingResourceGroup("myResourceGroup")
            .withExtendedLocation(new ExtendedLocation().withType("CustomLocation")
                .withName(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/myResourceGroup/providers/microsoft.extendedlocation/customlocations/location1"))
            .withTags(mapOf("site", "building-1"))
            .withProperties(
                new AssetEndpointProfileProperties().withTargetAddress("https://www.example.com/myTargetAddress")
                    .withUserAuthentication(new UserAuthentication().withMode(UserAuthenticationMode.ANONYMOUS)))
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

### AssetEndpointProfiles_Delete

```java
/**
 * Samples for AssetEndpointProfiles Delete.
 */
public final class AssetEndpointProfilesDeleteSamples {
    /*
     * x-ms-original-file: 2023-11-01-preview/Delete_AssetEndpointProfile.json
     */
    /**
     * Sample code: Delete_AssetEndpointProfile.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        deleteAssetEndpointProfile(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.assetEndpointProfiles()
            .delete("myResourceGroup", "my-assetendpointprofile", com.azure.core.util.Context.NONE);
    }
}
```

### AssetEndpointProfiles_GetByResourceGroup

```java
/**
 * Samples for AssetEndpointProfiles GetByResourceGroup.
 */
public final class AssetEndpointProfilesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2023-11-01-preview/Get_AssetEndpointProfile.json
     */
    /**
     * Sample code: Get_AssetEndpointProfile.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void getAssetEndpointProfile(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.assetEndpointProfiles()
            .getByResourceGroupWithResponse("myResourceGroup", "my-assetendpointprofile",
                com.azure.core.util.Context.NONE);
    }
}
```

### AssetEndpointProfiles_List

```java
/**
 * Samples for AssetEndpointProfiles List.
 */
public final class AssetEndpointProfilesListSamples {
    /*
     * x-ms-original-file: 2023-11-01-preview/List_AssetEndpointProfiles_Subscription.json
     */
    /**
     * Sample code: List_AssetEndpointProfiles_Subscription.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        listAssetEndpointProfilesSubscription(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.assetEndpointProfiles().list(com.azure.core.util.Context.NONE);
    }
}
```

### AssetEndpointProfiles_ListByResourceGroup

```java
/**
 * Samples for AssetEndpointProfiles ListByResourceGroup.
 */
public final class AssetEndpointProfilesListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2023-11-01-preview/List_AssetEndpointProfiles_ResourceGroup.json
     */
    /**
     * Sample code: List_AssetEndpointProfiles_ResourceGroup.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        listAssetEndpointProfilesResourceGroup(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.assetEndpointProfiles().listByResourceGroup("myResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### AssetEndpointProfiles_Update

```java
import com.azure.resourcemanager.deviceregistry.models.AssetEndpointProfile;
import com.azure.resourcemanager.deviceregistry.models.AssetEndpointProfileUpdateProperties;

/**
 * Samples for AssetEndpointProfiles Update.
 */
public final class AssetEndpointProfilesUpdateSamples {
    /*
     * x-ms-original-file: 2023-11-01-preview/Update_AssetEndpointProfile.json
     */
    /**
     * Sample code: Update_AssetEndpointProfile.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        updateAssetEndpointProfile(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        AssetEndpointProfile resource = manager.assetEndpointProfiles()
            .getByResourceGroupWithResponse("myResourceGroup", "my-assetendpointprofile",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(
                new AssetEndpointProfileUpdateProperties().withTargetAddress("https://www.example.com/myTargetAddress"))
            .apply();
    }
}
```

### Assets_CreateOrReplace

```java
import com.azure.resourcemanager.deviceregistry.models.AssetProperties;
import com.azure.resourcemanager.deviceregistry.models.DataPoint;
import com.azure.resourcemanager.deviceregistry.models.DataPointsObservabilityMode;
import com.azure.resourcemanager.deviceregistry.models.Event;
import com.azure.resourcemanager.deviceregistry.models.EventsObservabilityMode;
import com.azure.resourcemanager.deviceregistry.models.ExtendedLocation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Assets CreateOrReplace.
 */
public final class AssetsCreateOrReplaceSamples {
    /*
     * x-ms-original-file: 2023-11-01-preview/Create_Asset_Without_ExternalAssetId.json
     */
    /**
     * Sample code: Create_Asset_Without_ExternalAssetId.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        createAssetWithoutExternalAssetId(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.assets()
            .define("my-asset")
            .withRegion("West Europe")
            .withExistingResourceGroup("myResourceGroup")
            .withExtendedLocation(new ExtendedLocation().withType("CustomLocation")
                .withName(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/myResourceGroup/providers/microsoft.extendedlocation/customlocations/location1"))
            .withTags(mapOf("site", "building-1"))
            .withProperties(new AssetProperties().withAssetType("MyAssetType")
                .withEnabled(true)
                .withDisplayName("AssetDisplayName")
                .withDescription("This is a sample Asset")
                .withAssetEndpointProfileUri("https://www.example.com/myAssetEndpointProfile")
                .withManufacturer("Contoso")
                .withManufacturerUri("https://www.contoso.com/manufacturerUri")
                .withModel("ContosoModel")
                .withProductCode("fakeTokenPlaceholder")
                .withHardwareRevision("1.0")
                .withSoftwareRevision("2.0")
                .withDocumentationUri("https://www.example.com/manual")
                .withSerialNumber("64-103816-519918-8")
                .withDefaultDataPointsConfiguration(
                    "{\"publishingInterval\":10,\"samplingInterval\":15,\"queueSize\":20}")
                .withDefaultEventsConfiguration("{\"publishingInterval\":10,\"samplingInterval\":15,\"queueSize\":20}")
                .withDataPoints(Arrays.asList(
                    new DataPoint().withDataSource("nsu=http://microsoft.com/Opc/OpcPlc/;s=FastUInt1")
                        .withCapabilityId("dtmi:com:example:Thermostat:__temperature;1")
                        .withObservabilityMode(DataPointsObservabilityMode.COUNTER)
                        .withDataPointConfiguration(
                            "{\"publishingInterval\":8,\"samplingInterval\":8,\"queueSize\":4}"),
                    new DataPoint().withDataSource("nsu=http://microsoft.com/Opc/OpcPlc/;s=FastUInt2")
                        .withCapabilityId("dtmi:com:example:Thermostat:__pressure;1")
                        .withObservabilityMode(DataPointsObservabilityMode.NONE)
                        .withDataPointConfiguration(
                            "{\"publishingInterval\":4,\"samplingInterval\":4,\"queueSize\":7}")))
                .withEvents(Arrays.asList(
                    new Event().withEventNotifier("nsu=http://microsoft.com/Opc/OpcPlc/;s=FastUInt3")
                        .withCapabilityId("dtmi:com:example:Thermostat:__temperature;1")
                        .withObservabilityMode(EventsObservabilityMode.NONE)
                        .withEventConfiguration("{\"publishingInterval\":7,\"samplingInterval\":1,\"queueSize\":8}"),
                    new Event().withEventNotifier("nsu=http://microsoft.com/Opc/OpcPlc/;s=FastUInt4")
                        .withCapabilityId("dtmi:com:example:Thermostat:__pressure;1")
                        .withObservabilityMode(EventsObservabilityMode.LOG)
                        .withEventConfiguration("{\"publishingInterval\":7,\"samplingInterval\":8,\"queueSize\":4}"))))
            .create();
    }

    /*
     * x-ms-original-file: 2023-11-01-preview/Create_Asset_With_ExternalAssetId.json
     */
    /**
     * Sample code: Create_Asset_With_ExternalAssetId.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        createAssetWithExternalAssetId(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.assets()
            .define("my-asset")
            .withRegion("West Europe")
            .withExistingResourceGroup("myResourceGroup")
            .withExtendedLocation(new ExtendedLocation().withType("CustomLocation")
                .withName(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/myResourceGroup/providers/microsoft.extendedlocation/customlocations/location1"))
            .withTags(mapOf("site", "building-1"))
            .withProperties(new AssetProperties().withAssetType("MyAssetType")
                .withEnabled(true)
                .withExternalAssetId("8ZBA6LRHU0A458969")
                .withDisplayName("AssetDisplayName")
                .withDescription("This is a sample Asset")
                .withAssetEndpointProfileUri("https://www.example.com/myAssetEndpointProfile")
                .withManufacturer("Contoso")
                .withManufacturerUri("https://www.contoso.com/manufacturerUri")
                .withModel("ContosoModel")
                .withProductCode("fakeTokenPlaceholder")
                .withHardwareRevision("1.0")
                .withSoftwareRevision("2.0")
                .withDocumentationUri("https://www.example.com/manual")
                .withSerialNumber("64-103816-519918-8")
                .withDefaultDataPointsConfiguration(
                    "{\"publishingInterval\":10,\"samplingInterval\":15,\"queueSize\":20}")
                .withDefaultEventsConfiguration("{\"publishingInterval\":10,\"samplingInterval\":15,\"queueSize\":20}")
                .withDataPoints(Arrays.asList(
                    new DataPoint().withDataSource("nsu=http://microsoft.com/Opc/OpcPlc/;s=FastUInt1")
                        .withCapabilityId("dtmi:com:example:Thermostat:__temperature;1")
                        .withObservabilityMode(DataPointsObservabilityMode.COUNTER)
                        .withDataPointConfiguration(
                            "{\"publishingInterval\":8,\"samplingInterval\":8,\"queueSize\":4}"),
                    new DataPoint().withDataSource("nsu=http://microsoft.com/Opc/OpcPlc/;s=FastUInt2")
                        .withCapabilityId("dtmi:com:example:Thermostat:__pressure;1")
                        .withObservabilityMode(DataPointsObservabilityMode.NONE)
                        .withDataPointConfiguration(
                            "{\"publishingInterval\":4,\"samplingInterval\":4,\"queueSize\":7}")))
                .withEvents(Arrays.asList(
                    new Event().withEventNotifier("nsu=http://microsoft.com/Opc/OpcPlc/;s=FastUInt3")
                        .withCapabilityId("dtmi:com:example:Thermostat:__temperature;1")
                        .withObservabilityMode(EventsObservabilityMode.NONE)
                        .withEventConfiguration("{\"publishingInterval\":7,\"samplingInterval\":1,\"queueSize\":8}"),
                    new Event().withEventNotifier("nsu=http://microsoft.com/Opc/OpcPlc/;s=FastUInt4")
                        .withCapabilityId("dtmi:com:example:Thermostat:__pressure;1")
                        .withObservabilityMode(EventsObservabilityMode.LOG)
                        .withEventConfiguration("{\"publishingInterval\":7,\"samplingInterval\":8,\"queueSize\":4}"))))
            .create();
    }

    /*
     * x-ms-original-file: 2023-11-01-preview/Create_Asset_Without_DisplayName.json
     */
    /**
     * Sample code: Create_Asset_Without_DisplayName.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        createAssetWithoutDisplayName(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.assets()
            .define("my-asset")
            .withRegion("West Europe")
            .withExistingResourceGroup("myResourceGroup")
            .withExtendedLocation(new ExtendedLocation().withType("CustomLocation")
                .withName(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/myResourceGroup/providers/microsoft.extendedlocation/customlocations/location1"))
            .withTags(mapOf("site", "building-1"))
            .withProperties(new AssetProperties().withAssetType("MyAssetType")
                .withEnabled(true)
                .withExternalAssetId("8ZBA6LRHU0A458969")
                .withDescription("This is a sample Asset")
                .withAssetEndpointProfileUri("https://www.example.com/myAssetEndpointProfile")
                .withManufacturer("Contoso")
                .withManufacturerUri("https://www.contoso.com/manufacturerUri")
                .withModel("ContosoModel")
                .withProductCode("fakeTokenPlaceholder")
                .withHardwareRevision("1.0")
                .withSoftwareRevision("2.0")
                .withDocumentationUri("https://www.example.com/manual")
                .withSerialNumber("64-103816-519918-8")
                .withDefaultDataPointsConfiguration(
                    "{\"publishingInterval\":10,\"samplingInterval\":15,\"queueSize\":20}")
                .withDefaultEventsConfiguration("{\"publishingInterval\":10,\"samplingInterval\":15,\"queueSize\":20}")
                .withDataPoints(Arrays.asList(
                    new DataPoint().withDataSource("nsu=http://microsoft.com/Opc/OpcPlc/;s=FastUInt1")
                        .withCapabilityId("dtmi:com:example:Thermostat:__temperature;1")
                        .withObservabilityMode(DataPointsObservabilityMode.COUNTER)
                        .withDataPointConfiguration(
                            "{\"publishingInterval\":8,\"samplingInterval\":8,\"queueSize\":4}"),
                    new DataPoint().withDataSource("nsu=http://microsoft.com/Opc/OpcPlc/;s=FastUInt2")
                        .withCapabilityId("dtmi:com:example:Thermostat:__pressure;1")
                        .withObservabilityMode(DataPointsObservabilityMode.NONE)
                        .withDataPointConfiguration(
                            "{\"publishingInterval\":4,\"samplingInterval\":4,\"queueSize\":7}")))
                .withEvents(Arrays.asList(
                    new Event().withEventNotifier("nsu=http://microsoft.com/Opc/OpcPlc/;s=FastUInt3")
                        .withCapabilityId("dtmi:com:example:Thermostat:__temperature;1")
                        .withObservabilityMode(EventsObservabilityMode.NONE)
                        .withEventConfiguration("{\"publishingInterval\":7,\"samplingInterval\":1,\"queueSize\":8}"),
                    new Event().withEventNotifier("nsu=http://microsoft.com/Opc/OpcPlc/;s=FastUInt4")
                        .withCapabilityId("dtmi:com:example:Thermostat:__pressure;1")
                        .withObservabilityMode(EventsObservabilityMode.LOG)
                        .withEventConfiguration("{\"publishingInterval\":7,\"samplingInterval\":8,\"queueSize\":4}"))))
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

### Assets_Delete

```java
/**
 * Samples for Assets Delete.
 */
public final class AssetsDeleteSamples {
    /*
     * x-ms-original-file: 2023-11-01-preview/Delete_Asset.json
     */
    /**
     * Sample code: Delete_Asset.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void deleteAsset(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.assets().delete("myResourceGroup", "my-asset", com.azure.core.util.Context.NONE);
    }
}
```

### Assets_GetByResourceGroup

```java
/**
 * Samples for Assets GetByResourceGroup.
 */
public final class AssetsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2023-11-01-preview/Get_Asset_With_SyncStatus.json
     */
    /**
     * Sample code: Get_Asset_With_SyncStatus.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void getAssetWithSyncStatus(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.assets()
            .getByResourceGroupWithResponse("myResourceGroup", "my-asset", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2023-11-01-preview/Get_Asset.json
     */
    /**
     * Sample code: Get_Asset.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void getAsset(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.assets()
            .getByResourceGroupWithResponse("myResourceGroup", "my-asset", com.azure.core.util.Context.NONE);
    }
}
```

### Assets_List

```java
/**
 * Samples for Assets List.
 */
public final class AssetsListSamples {
    /*
     * x-ms-original-file: 2023-11-01-preview/List_Assets_Subscription.json
     */
    /**
     * Sample code: List_Assets_Subscription.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void listAssetsSubscription(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.assets().list(com.azure.core.util.Context.NONE);
    }
}
```

### Assets_ListByResourceGroup

```java
/**
 * Samples for Assets ListByResourceGroup.
 */
public final class AssetsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2023-11-01-preview/List_Assets_ResourceGroup.json
     */
    /**
     * Sample code: List_Assets_ResourceGroup.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void listAssetsResourceGroup(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.assets().listByResourceGroup("myResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### Assets_Update

```java
import com.azure.resourcemanager.deviceregistry.models.Asset;
import com.azure.resourcemanager.deviceregistry.models.AssetUpdateProperties;

/**
 * Samples for Assets Update.
 */
public final class AssetsUpdateSamples {
    /*
     * x-ms-original-file: 2023-11-01-preview/Update_Asset.json
     */
    /**
     * Sample code: Update_Asset.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void updateAsset(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        Asset resource = manager.assets()
            .getByResourceGroupWithResponse("myResourceGroup", "my-asset", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new AssetUpdateProperties().withEnabled(false).withDisplayName("NewAssetDisplayName"))
            .apply();
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
     * x-ms-original-file: 2023-11-01-preview/Get_OperationStatus.json
     */
    /**
     * Sample code: Get_OperationStatus.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void getOperationStatus(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.operationStatus()
            .getWithResponse("testLocation", "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2023-11-01-preview/List_Operations.json
     */
    /**
     * Sample code: List_Operations.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void listOperations(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

