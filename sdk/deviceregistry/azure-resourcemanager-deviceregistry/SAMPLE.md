# Code snippets and samples


## AssetEndpointProfiles

- [CreateOrReplace](#assetendpointprofiles_createorreplace)
- [Delete](#assetendpointprofiles_delete)
- [GetByResourceGroup](#assetendpointprofiles_getbyresourcegroup)
- [Update](#assetendpointprofiles_update)

## Assets

- [CreateOrReplace](#assets_createorreplace)
- [Delete](#assets_delete)
- [GetByResourceGroup](#assets_getbyresourcegroup)
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

### AssetEndpointProfiles_Update

```java
import com.azure.resourcemanager.deviceregistry.models.AssetEndpointProfile;
import com.azure.resourcemanager.deviceregistry.models.AssetEndpointProfileUpdateProperties;

/**
 * Samples for AssetEndpointProfiles Update.
 */
public final class AssetEndpointProfilesUpdateSamples {
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

### Assets_Update

```java
import com.azure.resourcemanager.deviceregistry.models.Asset;
import com.azure.resourcemanager.deviceregistry.models.AssetUpdateProperties;

/**
 * Samples for Assets Update.
 */
public final class AssetsUpdateSamples {
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

