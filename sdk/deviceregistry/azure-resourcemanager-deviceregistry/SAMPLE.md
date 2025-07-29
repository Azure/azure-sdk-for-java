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

## BillingContainers

- [Get](#billingcontainers_get)
- [List](#billingcontainers_list)

## OperationStatus

- [Get](#operationstatus_get)

## Operations

- [List](#operations_list)
### AssetEndpointProfiles_CreateOrReplace

```java
import com.azure.resourcemanager.deviceregistry.models.AssetEndpointProfileProperties;
import com.azure.resourcemanager.deviceregistry.models.Authentication;
import com.azure.resourcemanager.deviceregistry.models.AuthenticationMethod;
import com.azure.resourcemanager.deviceregistry.models.ExtendedLocation;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for AssetEndpointProfiles CreateOrReplace.
 */
public final class AssetEndpointProfilesCreateOrReplaceSamples {
    /*
     * x-ms-original-file: 2024-11-01/Create_AssetEndpointProfile_With_DiscoveredAepRef.json
     */
    /**
     * Sample code: Create_AssetEndpointProfile_With_DiscoveredAepRef.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void createAssetEndpointProfileWithDiscoveredAepRef(
        com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
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
                    .withEndpointProfileType("myEndpointProfileType")
                    .withAuthentication(new Authentication().withMethod(AuthenticationMethod.ANONYMOUS))
                    .withDiscoveredAssetEndpointProfileRef("discoveredAssetEndpointProfile1"))
            .create();
    }

    /*
     * x-ms-original-file: 2024-11-01/Create_AssetEndpointProfile.json
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
                    .withEndpointProfileType("myEndpointProfileType")
                    .withAuthentication(new Authentication().withMethod(AuthenticationMethod.ANONYMOUS)))
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
     * x-ms-original-file: 2024-11-01/Delete_AssetEndpointProfile.json
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
     * x-ms-original-file: 2024-11-01/Get_AssetEndpointProfile.json
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

    /*
     * x-ms-original-file: 2024-11-01/Get_AssetEndpointProfile_With_SyncStatus.json
     */
    /**
     * Sample code: Get_AssetEndpointProfile_With_SyncStatus.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        getAssetEndpointProfileWithSyncStatus(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
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
     * x-ms-original-file: 2024-11-01/List_AssetEndpointProfiles_Subscription.json
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
     * x-ms-original-file: 2024-11-01/List_AssetEndpointProfiles_ResourceGroup.json
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
     * x-ms-original-file: 2024-11-01/Update_AssetEndpointProfile.json
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
import com.azure.resourcemanager.deviceregistry.models.DataPointObservabilityMode;
import com.azure.resourcemanager.deviceregistry.models.Dataset;
import com.azure.resourcemanager.deviceregistry.models.Event;
import com.azure.resourcemanager.deviceregistry.models.EventObservabilityMode;
import com.azure.resourcemanager.deviceregistry.models.ExtendedLocation;
import com.azure.resourcemanager.deviceregistry.models.Topic;
import com.azure.resourcemanager.deviceregistry.models.TopicRetainType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Assets CreateOrReplace.
 */
public final class AssetsCreateOrReplaceSamples {
    /*
     * x-ms-original-file: 2024-11-01/Create_Asset_With_DiscoveredAssetRef.json
     */
    /**
     * Sample code: Create_Asset_With_DiscoveredAssetRefs.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        createAssetWithDiscoveredAssetRefs(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.assets()
            .define("my-asset")
            .withRegion("West Europe")
            .withExistingResourceGroup("myResourceGroup")
            .withExtendedLocation(new ExtendedLocation().withType("CustomLocation")
                .withName(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/myResourceGroup/providers/microsoft.extendedlocation/customlocations/location1"))
            .withTags(mapOf("site", "building-1"))
            .withProperties(new AssetProperties().withEnabled(true)
                .withExternalAssetId("8ZBA6LRHU0A458969")
                .withDisplayName("AssetDisplayName")
                .withDescription("This is a sample Asset")
                .withAssetEndpointProfileRef("myAssetEndpointProfile")
                .withManufacturer("Contoso")
                .withManufacturerUri("https://www.contoso.com/manufacturerUri")
                .withModel("ContosoModel")
                .withProductCode("fakeTokenPlaceholder")
                .withHardwareRevision("1.0")
                .withSoftwareRevision("2.0")
                .withDocumentationUri("https://www.example.com/manual")
                .withSerialNumber("64-103816-519918-8")
                .withDiscoveredAssetRefs(Arrays.asList("discoveredAsset1", "discoveredAsset2"))
                .withDefaultDatasetsConfiguration(
                    "{\"publishingInterval\":10,\"samplingInterval\":15,\"queueSize\":20}")
                .withDefaultEventsConfiguration("{\"publishingInterval\":10,\"samplingInterval\":15,\"queueSize\":20}")
                .withDefaultTopic(new Topic().withPath("/path/defaultTopic").withRetain(TopicRetainType.KEEP))
                .withDatasets(Arrays.asList(new Dataset().withName("dataset1")
                    .withDatasetConfiguration("{\"publishingInterval\":10,\"samplingInterval\":15,\"queueSize\":20}")
                    .withTopic(new Topic().withPath("/path/dataset1").withRetain(TopicRetainType.KEEP))
                    .withDataPoints(Arrays.asList(
                        new DataPoint().withName("dataPoint1")
                            .withDataSource("nsu=http://microsoft.com/Opc/OpcPlc/;s=FastUInt1")
                            .withDataPointConfiguration(
                                "{\"publishingInterval\":8,\"samplingInterval\":8,\"queueSize\":4}")
                            .withObservabilityMode(DataPointObservabilityMode.COUNTER),
                        new DataPoint().withName("dataPoint2")
                            .withDataSource("nsu=http://microsoft.com/Opc/OpcPlc/;s=FastUInt2")
                            .withDataPointConfiguration(
                                "{\"publishingInterval\":4,\"samplingInterval\":4,\"queueSize\":7}")
                            .withObservabilityMode(DataPointObservabilityMode.NONE)))))
                .withEvents(
                    Arrays
                        .asList(
                            new Event().withName("event1")
                                .withEventNotifier("nsu=http://microsoft.com/Opc/OpcPlc/;s=FastUInt3")
                                .withEventConfiguration(
                                    "{\"publishingInterval\":7,\"samplingInterval\":1,\"queueSize\":8}")
                                .withTopic(new Topic().withPath("/path/event1").withRetain(TopicRetainType.KEEP))
                                .withObservabilityMode(EventObservabilityMode.NONE),
                            new Event().withName("event2")
                                .withEventNotifier("nsu=http://microsoft.com/Opc/OpcPlc/;s=FastUInt4")
                                .withEventConfiguration(
                                    "{\"publishingInterval\":7,\"samplingInterval\":8,\"queueSize\":4}")
                                .withObservabilityMode(EventObservabilityMode.LOG))))
            .create();
    }

    /*
     * x-ms-original-file: 2024-11-01/Create_Asset_Without_ExternalAssetId.json
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
            .withProperties(new AssetProperties().withEnabled(true)
                .withDisplayName("AssetDisplayName")
                .withDescription("This is a sample Asset")
                .withAssetEndpointProfileRef("myAssetEndpointProfile")
                .withManufacturer("Contoso")
                .withManufacturerUri("https://www.contoso.com/manufacturerUri")
                .withModel("ContosoModel")
                .withProductCode("fakeTokenPlaceholder")
                .withHardwareRevision("1.0")
                .withSoftwareRevision("2.0")
                .withDocumentationUri("https://www.example.com/manual")
                .withSerialNumber("64-103816-519918-8")
                .withDefaultDatasetsConfiguration(
                    "{\"publishingInterval\":10,\"samplingInterval\":15,\"queueSize\":20}")
                .withDefaultEventsConfiguration("{\"publishingInterval\":10,\"samplingInterval\":15,\"queueSize\":20}")
                .withDefaultTopic(new Topic().withPath("/path/defaultTopic").withRetain(TopicRetainType.KEEP))
                .withDatasets(Arrays.asList(new Dataset().withName("dataset1")
                    .withDatasetConfiguration("{\"publishingInterval\":10,\"samplingInterval\":15,\"queueSize\":20}")
                    .withTopic(new Topic().withPath("/path/dataset1").withRetain(TopicRetainType.KEEP))
                    .withDataPoints(Arrays.asList(
                        new DataPoint().withName("dataPoint1")
                            .withDataSource("nsu=http://microsoft.com/Opc/OpcPlc/;s=FastUInt1")
                            .withDataPointConfiguration(
                                "{\"publishingInterval\":8,\"samplingInterval\":8,\"queueSize\":4}")
                            .withObservabilityMode(DataPointObservabilityMode.COUNTER),
                        new DataPoint().withName("dataPoint2")
                            .withDataSource("nsu=http://microsoft.com/Opc/OpcPlc/;s=FastUInt2")
                            .withDataPointConfiguration(
                                "{\"publishingInterval\":4,\"samplingInterval\":4,\"queueSize\":7}")
                            .withObservabilityMode(DataPointObservabilityMode.NONE)))))
                .withEvents(
                    Arrays
                        .asList(
                            new Event().withName("event1")
                                .withEventNotifier("nsu=http://microsoft.com/Opc/OpcPlc/;s=FastUInt3")
                                .withEventConfiguration(
                                    "{\"publishingInterval\":7,\"samplingInterval\":1,\"queueSize\":8}")
                                .withTopic(new Topic().withPath("/path/event1").withRetain(TopicRetainType.KEEP))
                                .withObservabilityMode(EventObservabilityMode.NONE),
                            new Event().withName("event2")
                                .withEventNotifier("nsu=http://microsoft.com/Opc/OpcPlc/;s=FastUInt4")
                                .withEventConfiguration(
                                    "{\"publishingInterval\":7,\"samplingInterval\":8,\"queueSize\":4}")
                                .withObservabilityMode(EventObservabilityMode.LOG))))
            .create();
    }

    /*
     * x-ms-original-file: 2024-11-01/Create_Asset_With_ExternalAssetId.json
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
            .withProperties(new AssetProperties().withEnabled(true)
                .withExternalAssetId("8ZBA6LRHU0A458969")
                .withDisplayName("AssetDisplayName")
                .withDescription("This is a sample Asset")
                .withAssetEndpointProfileRef("myAssetEndpointProfile")
                .withManufacturer("Contoso")
                .withManufacturerUri("https://www.contoso.com/manufacturerUri")
                .withModel("ContosoModel")
                .withProductCode("fakeTokenPlaceholder")
                .withHardwareRevision("1.0")
                .withSoftwareRevision("2.0")
                .withDocumentationUri("https://www.example.com/manual")
                .withSerialNumber("64-103816-519918-8")
                .withDefaultDatasetsConfiguration(
                    "{\"publishingInterval\":10,\"samplingInterval\":15,\"queueSize\":20}")
                .withDefaultEventsConfiguration("{\"publishingInterval\":10,\"samplingInterval\":15,\"queueSize\":20}")
                .withDefaultTopic(new Topic().withPath("/path/defaultTopic").withRetain(TopicRetainType.KEEP))
                .withDatasets(Arrays.asList(new Dataset().withName("dataset1")
                    .withDatasetConfiguration("{\"publishingInterval\":10,\"samplingInterval\":15,\"queueSize\":20}")
                    .withTopic(new Topic().withPath("/path/dataset1").withRetain(TopicRetainType.KEEP))
                    .withDataPoints(Arrays.asList(
                        new DataPoint().withName("dataPoint1")
                            .withDataSource("nsu=http://microsoft.com/Opc/OpcPlc/;s=FastUInt1")
                            .withDataPointConfiguration(
                                "{\"publishingInterval\":8,\"samplingInterval\":8,\"queueSize\":4}")
                            .withObservabilityMode(DataPointObservabilityMode.COUNTER),
                        new DataPoint().withName("dataPoint2")
                            .withDataSource("nsu=http://microsoft.com/Opc/OpcPlc/;s=FastUInt2")
                            .withDataPointConfiguration(
                                "{\"publishingInterval\":4,\"samplingInterval\":4,\"queueSize\":7}")
                            .withObservabilityMode(DataPointObservabilityMode.NONE)))))
                .withEvents(
                    Arrays
                        .asList(
                            new Event().withName("event1")
                                .withEventNotifier("nsu=http://microsoft.com/Opc/OpcPlc/;s=FastUInt3")
                                .withEventConfiguration(
                                    "{\"publishingInterval\":7,\"samplingInterval\":1,\"queueSize\":8}")
                                .withTopic(new Topic().withPath("/path/event1").withRetain(TopicRetainType.KEEP))
                                .withObservabilityMode(EventObservabilityMode.NONE),
                            new Event().withName("event2")
                                .withEventNotifier("nsu=http://microsoft.com/Opc/OpcPlc/;s=FastUInt4")
                                .withEventConfiguration(
                                    "{\"publishingInterval\":7,\"samplingInterval\":8,\"queueSize\":4}")
                                .withObservabilityMode(EventObservabilityMode.LOG))))
            .create();
    }

    /*
     * x-ms-original-file: 2024-11-01/Create_Asset_Without_DisplayName.json
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
            .withProperties(new AssetProperties().withEnabled(true)
                .withExternalAssetId("8ZBA6LRHU0A458969")
                .withDescription("This is a sample Asset")
                .withAssetEndpointProfileRef("myAssetEndpointProfile")
                .withManufacturer("Contoso")
                .withManufacturerUri("https://www.contoso.com/manufacturerUri")
                .withModel("ContosoModel")
                .withProductCode("fakeTokenPlaceholder")
                .withHardwareRevision("1.0")
                .withSoftwareRevision("2.0")
                .withDocumentationUri("https://www.example.com/manual")
                .withSerialNumber("64-103816-519918-8")
                .withDefaultDatasetsConfiguration(
                    "{\"publishingInterval\":10,\"samplingInterval\":15,\"queueSize\":20}")
                .withDefaultEventsConfiguration("{\"publishingInterval\":10,\"samplingInterval\":15,\"queueSize\":20}")
                .withDefaultTopic(new Topic().withPath("/path/defaultTopic").withRetain(TopicRetainType.KEEP))
                .withDatasets(Arrays.asList(new Dataset().withName("dataset1")
                    .withDatasetConfiguration("{\"publishingInterval\":10,\"samplingInterval\":15,\"queueSize\":20}")
                    .withTopic(new Topic().withPath("/path/dataset1").withRetain(TopicRetainType.KEEP))
                    .withDataPoints(Arrays.asList(
                        new DataPoint().withName("dataPoint1")
                            .withDataSource("nsu=http://microsoft.com/Opc/OpcPlc/;s=FastUInt1")
                            .withDataPointConfiguration(
                                "{\"publishingInterval\":8,\"samplingInterval\":8,\"queueSize\":4}")
                            .withObservabilityMode(DataPointObservabilityMode.COUNTER),
                        new DataPoint().withName("dataPoint2")
                            .withDataSource("nsu=http://microsoft.com/Opc/OpcPlc/;s=FastUInt2")
                            .withDataPointConfiguration(
                                "{\"publishingInterval\":4,\"samplingInterval\":4,\"queueSize\":7}")
                            .withObservabilityMode(DataPointObservabilityMode.NONE)))))
                .withEvents(
                    Arrays
                        .asList(
                            new Event().withName("event1")
                                .withEventNotifier("nsu=http://microsoft.com/Opc/OpcPlc/;s=FastUInt3")
                                .withEventConfiguration(
                                    "{\"publishingInterval\":7,\"samplingInterval\":1,\"queueSize\":8}")
                                .withTopic(new Topic().withPath("/path/event1").withRetain(TopicRetainType.KEEP))
                                .withObservabilityMode(EventObservabilityMode.NONE),
                            new Event().withName("event2")
                                .withEventNotifier("nsu=http://microsoft.com/Opc/OpcPlc/;s=FastUInt4")
                                .withEventConfiguration(
                                    "{\"publishingInterval\":7,\"samplingInterval\":8,\"queueSize\":4}")
                                .withObservabilityMode(EventObservabilityMode.LOG))))
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
     * x-ms-original-file: 2024-11-01/Delete_Asset.json
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
     * x-ms-original-file: 2024-11-01/Get_Asset_With_SyncStatus.json
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
     * x-ms-original-file: 2024-11-01/Get_Asset.json
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
     * x-ms-original-file: 2024-11-01/List_Assets_Subscription.json
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
     * x-ms-original-file: 2024-11-01/List_Assets_ResourceGroup.json
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
     * x-ms-original-file: 2024-11-01/Update_Asset.json
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

### BillingContainers_Get

```java
/**
 * Samples for BillingContainers Get.
 */
public final class BillingContainersGetSamples {
    /*
     * x-ms-original-file: 2024-11-01/Get_BillingContainer.json
     */
    /**
     * Sample code: Get_BillingContainer.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void getBillingContainer(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.billingContainers().getWithResponse("my-billingContainer", com.azure.core.util.Context.NONE);
    }
}
```

### BillingContainers_List

```java
/**
 * Samples for BillingContainers List.
 */
public final class BillingContainersListSamples {
    /*
     * x-ms-original-file: 2024-11-01/List_BillingContainers_Subscription.json
     */
    /**
     * Sample code: List_BillingContainers_Subscription.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        listBillingContainersSubscription(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.billingContainers().list(com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2024-11-01/Get_OperationStatus.json
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
     * x-ms-original-file: 2024-11-01/List_Operations.json
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

