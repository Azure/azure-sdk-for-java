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

## DiscoveredAssetEndpointProfiles

- [CreateOrReplace](#discoveredassetendpointprofiles_createorreplace)
- [Delete](#discoveredassetendpointprofiles_delete)
- [GetByResourceGroup](#discoveredassetendpointprofiles_getbyresourcegroup)
- [List](#discoveredassetendpointprofiles_list)
- [ListByResourceGroup](#discoveredassetendpointprofiles_listbyresourcegroup)
- [Update](#discoveredassetendpointprofiles_update)

## DiscoveredAssets

- [CreateOrReplace](#discoveredassets_createorreplace)
- [Delete](#discoveredassets_delete)
- [GetByResourceGroup](#discoveredassets_getbyresourcegroup)
- [List](#discoveredassets_list)
- [ListByResourceGroup](#discoveredassets_listbyresourcegroup)
- [Update](#discoveredassets_update)

## OperationStatus

- [Get](#operationstatus_get)

## Operations

- [List](#operations_list)

## SchemaRegistries

- [CreateOrReplace](#schemaregistries_createorreplace)
- [Delete](#schemaregistries_delete)
- [GetByResourceGroup](#schemaregistries_getbyresourcegroup)
- [List](#schemaregistries_list)
- [ListByResourceGroup](#schemaregistries_listbyresourcegroup)
- [Update](#schemaregistries_update)

## SchemaVersions

- [CreateOrReplace](#schemaversions_createorreplace)
- [Delete](#schemaversions_delete)
- [Get](#schemaversions_get)
- [ListBySchema](#schemaversions_listbyschema)

## Schemas

- [CreateOrReplace](#schemas_createorreplace)
- [Delete](#schemas_delete)
- [Get](#schemas_get)
- [ListBySchemaRegistry](#schemas_listbyschemaregistry)
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
     * x-ms-original-file: 2024-09-01-preview/Create_AssetEndpointProfile_With_DiscoveredAepRef.json
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
     * x-ms-original-file: 2024-09-01-preview/Create_AssetEndpointProfile.json
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
     * x-ms-original-file: 2024-09-01-preview/Delete_AssetEndpointProfile.json
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
     * x-ms-original-file: 2024-09-01-preview/Get_AssetEndpointProfile.json
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
     * x-ms-original-file: 2024-09-01-preview/Get_AssetEndpointProfile_With_SyncStatus.json
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
     * x-ms-original-file: 2024-09-01-preview/List_AssetEndpointProfiles_Subscription.json
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
     * x-ms-original-file: 2024-09-01-preview/List_AssetEndpointProfiles_ResourceGroup.json
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
     * x-ms-original-file: 2024-09-01-preview/Update_AssetEndpointProfile.json
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
     * x-ms-original-file: 2024-09-01-preview/Create_Asset_With_DiscoveredAssetRef.json
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
     * x-ms-original-file: 2024-09-01-preview/Create_Asset_Without_ExternalAssetId.json
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
     * x-ms-original-file: 2024-09-01-preview/Create_Asset_With_ExternalAssetId.json
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
     * x-ms-original-file: 2024-09-01-preview/Create_Asset_Without_DisplayName.json
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
     * x-ms-original-file: 2024-09-01-preview/Delete_Asset.json
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
     * x-ms-original-file: 2024-09-01-preview/Get_Asset_With_SyncStatus.json
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
     * x-ms-original-file: 2024-09-01-preview/Get_Asset.json
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
     * x-ms-original-file: 2024-09-01-preview/List_Assets_Subscription.json
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
     * x-ms-original-file: 2024-09-01-preview/List_Assets_ResourceGroup.json
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
     * x-ms-original-file: 2024-09-01-preview/Update_Asset.json
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
     * x-ms-original-file: 2024-09-01-preview/Get_BillingContainer.json
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
     * x-ms-original-file: 2024-09-01-preview/List_BillingContainers_Subscription.json
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

### DiscoveredAssetEndpointProfiles_CreateOrReplace

```java
import com.azure.resourcemanager.deviceregistry.models.AuthenticationMethod;
import com.azure.resourcemanager.deviceregistry.models.DiscoveredAssetEndpointProfileProperties;
import com.azure.resourcemanager.deviceregistry.models.ExtendedLocation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for DiscoveredAssetEndpointProfiles CreateOrReplace.
 */
public final class DiscoveredAssetEndpointProfilesCreateOrReplaceSamples {
    /*
     * x-ms-original-file: 2024-09-01-preview/Create_DiscoveredAssetEndpointProfile.json
     */
    /**
     * Sample code: Create_DiscoveredAssetEndpointProfile.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        createDiscoveredAssetEndpointProfile(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.discoveredAssetEndpointProfiles()
            .define("my-discoveredassetendpointprofile")
            .withRegion("West Europe")
            .withExistingResourceGroup("myResourceGroup")
            .withExtendedLocation(new ExtendedLocation().withType("CustomLocation")
                .withName(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/myResourceGroup/providers/microsoft.extendedlocation/customlocations/location1"))
            .withTags(mapOf("site", "building-1"))
            .withProperties(new DiscoveredAssetEndpointProfileProperties()
                .withTargetAddress("https://www.example.com/myTargetAddress")
                .withAdditionalConfiguration("{\"foo\": \"bar\"}")
                .withSupportedAuthenticationMethods(Arrays.asList(AuthenticationMethod.ANONYMOUS,
                    AuthenticationMethod.CERTIFICATE, AuthenticationMethod.USERNAME_PASSWORD))
                .withEndpointProfileType("myEndpointProfileType")
                .withDiscoveryId("11111111-1111-1111-1111-111111111111")
                .withVersion(73766L))
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

### DiscoveredAssetEndpointProfiles_Delete

```java
/**
 * Samples for DiscoveredAssetEndpointProfiles Delete.
 */
public final class DiscoveredAssetEndpointProfilesDeleteSamples {
    /*
     * x-ms-original-file: 2024-09-01-preview/Delete_DiscoveredAssetEndpointProfile.json
     */
    /**
     * Sample code: Delete_DiscoveredAssetEndpointProfile.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        deleteDiscoveredAssetEndpointProfile(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.discoveredAssetEndpointProfiles()
            .delete("myResourceGroup", "my-discoveredassetendpointprofile", com.azure.core.util.Context.NONE);
    }
}
```

### DiscoveredAssetEndpointProfiles_GetByResourceGroup

```java
/**
 * Samples for DiscoveredAssetEndpointProfiles GetByResourceGroup.
 */
public final class DiscoveredAssetEndpointProfilesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-09-01-preview/Get_DiscoveredAssetEndpointProfile.json
     */
    /**
     * Sample code: Get_DiscoveredAssetEndpointProfile.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        getDiscoveredAssetEndpointProfile(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.discoveredAssetEndpointProfiles()
            .getByResourceGroupWithResponse("myResourceGroup", "my-discoveredassetendpointprofile",
                com.azure.core.util.Context.NONE);
    }
}
```

### DiscoveredAssetEndpointProfiles_List

```java
/**
 * Samples for DiscoveredAssetEndpointProfiles List.
 */
public final class DiscoveredAssetEndpointProfilesListSamples {
    /*
     * x-ms-original-file: 2024-09-01-preview/List_DiscoveredAssetEndpointProfiles_Subscription.json
     */
    /**
     * Sample code: List_DiscoveredAssetEndpointProfiles_Subscription.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void listDiscoveredAssetEndpointProfilesSubscription(
        com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.discoveredAssetEndpointProfiles().list(com.azure.core.util.Context.NONE);
    }
}
```

### DiscoveredAssetEndpointProfiles_ListByResourceGroup

```java
/**
 * Samples for DiscoveredAssetEndpointProfiles ListByResourceGroup.
 */
public final class DiscoveredAssetEndpointProfilesListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-09-01-preview/List_DiscoveredAssetEndpointProfiles_ResourceGroup.json
     */
    /**
     * Sample code: List_DiscoveredAssetEndpointProfiles_ResourceGroup.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void listDiscoveredAssetEndpointProfilesResourceGroup(
        com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.discoveredAssetEndpointProfiles()
            .listByResourceGroup("myResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### DiscoveredAssetEndpointProfiles_Update

```java
import com.azure.resourcemanager.deviceregistry.models.AuthenticationMethod;
import com.azure.resourcemanager.deviceregistry.models.DiscoveredAssetEndpointProfile;
import com.azure.resourcemanager.deviceregistry.models.DiscoveredAssetEndpointProfileUpdateProperties;
import java.util.Arrays;

/**
 * Samples for DiscoveredAssetEndpointProfiles Update.
 */
public final class DiscoveredAssetEndpointProfilesUpdateSamples {
    /*
     * x-ms-original-file: 2024-09-01-preview/Update_DiscoveredAssetEndpointProfile.json
     */
    /**
     * Sample code: Update_DiscoveredAssetEndpointProfile.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        updateDiscoveredAssetEndpointProfile(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        DiscoveredAssetEndpointProfile resource = manager.discoveredAssetEndpointProfiles()
            .getByResourceGroupWithResponse("myResourceGroup", "my-discoveredassetendpointprofile",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new DiscoveredAssetEndpointProfileUpdateProperties()
                .withTargetAddress("https://www.example.com/myTargetAddress")
                .withAdditionalConfiguration("{\"foo\": \"bar\"}")
                .withSupportedAuthenticationMethods(
                    Arrays.asList(AuthenticationMethod.ANONYMOUS, AuthenticationMethod.CERTIFICATE))
                .withEndpointProfileType("myEndpointProfileType")
                .withDiscoveryId("11111111-1111-1111-1111-111111111111")
                .withVersion(73766L))
            .apply();
    }
}
```

### DiscoveredAssets_CreateOrReplace

```java
import com.azure.resourcemanager.deviceregistry.models.DiscoveredAssetProperties;
import com.azure.resourcemanager.deviceregistry.models.DiscoveredDataPoint;
import com.azure.resourcemanager.deviceregistry.models.DiscoveredDataset;
import com.azure.resourcemanager.deviceregistry.models.DiscoveredEvent;
import com.azure.resourcemanager.deviceregistry.models.ExtendedLocation;
import com.azure.resourcemanager.deviceregistry.models.Topic;
import com.azure.resourcemanager.deviceregistry.models.TopicRetainType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for DiscoveredAssets CreateOrReplace.
 */
public final class DiscoveredAssetsCreateOrReplaceSamples {
    /*
     * x-ms-original-file: 2024-09-01-preview/Create_DiscoveredAsset.json
     */
    /**
     * Sample code: Create_DiscoveredAsset.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void createDiscoveredAsset(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.discoveredAssets()
            .define("my-discoveredasset")
            .withRegion("West Europe")
            .withExistingResourceGroup("myResourceGroup")
            .withExtendedLocation(new ExtendedLocation().withType("CustomLocation")
                .withName(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/myResourceGroup/providers/microsoft.extendedlocation/customlocations/location1"))
            .withTags(mapOf("site", "building-1"))
            .withProperties(new DiscoveredAssetProperties().withAssetEndpointProfileRef("myAssetEndpointProfile")
                .withDiscoveryId("11111111-1111-1111-1111-111111111111")
                .withVersion(73766L)
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
                .withDatasets(Arrays.asList(new DiscoveredDataset().withName("dataset1")
                    .withDatasetConfiguration("{\"publishingInterval\":10,\"samplingInterval\":15,\"queueSize\":20}")
                    .withTopic(new Topic().withPath("/path/dataset1").withRetain(TopicRetainType.KEEP))
                    .withDataPoints(Arrays.asList(
                        new DiscoveredDataPoint().withName("dataPoint1")
                            .withDataSource("nsu=http://microsoft.com/Opc/OpcPlc/;s=FastUInt1")
                            .withDataPointConfiguration(
                                "{\"publishingInterval\":8,\"samplingInterval\":8,\"queueSize\":4}"),
                        new DiscoveredDataPoint().withName("dataPoint2")
                            .withDataSource("nsu=http://microsoft.com/Opc/OpcPlc/;s=FastUInt2")
                            .withDataPointConfiguration(
                                "{\"publishingInterval\":4,\"samplingInterval\":4,\"queueSize\":7}")))))
                .withEvents(Arrays.asList(
                    new DiscoveredEvent().withName("event1")
                        .withEventNotifier("nsu=http://microsoft.com/Opc/OpcPlc/;s=FastUInt3")
                        .withEventConfiguration("{\"publishingInterval\":7,\"samplingInterval\":1,\"queueSize\":8}")
                        .withTopic(new Topic().withPath("/path/event1").withRetain(TopicRetainType.KEEP)),
                    new DiscoveredEvent().withName("event2")
                        .withEventNotifier("nsu=http://microsoft.com/Opc/OpcPlc/;s=FastUInt4")
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

### DiscoveredAssets_Delete

```java
/**
 * Samples for DiscoveredAssets Delete.
 */
public final class DiscoveredAssetsDeleteSamples {
    /*
     * x-ms-original-file: 2024-09-01-preview/Delete_DiscoveredAsset.json
     */
    /**
     * Sample code: Delete_DiscoveredAsset.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void deleteDiscoveredAsset(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.discoveredAssets().delete("myResourceGroup", "my-discoveredasset", com.azure.core.util.Context.NONE);
    }
}
```

### DiscoveredAssets_GetByResourceGroup

```java
/**
 * Samples for DiscoveredAssets GetByResourceGroup.
 */
public final class DiscoveredAssetsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-09-01-preview/Get_DiscoveredAsset.json
     */
    /**
     * Sample code: Get_DiscoveredAsset.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void getDiscoveredAsset(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.discoveredAssets()
            .getByResourceGroupWithResponse("myResourceGroup", "my-discoveredasset", com.azure.core.util.Context.NONE);
    }
}
```

### DiscoveredAssets_List

```java
/**
 * Samples for DiscoveredAssets List.
 */
public final class DiscoveredAssetsListSamples {
    /*
     * x-ms-original-file: 2024-09-01-preview/List_DiscoveredAssets_Subscription.json
     */
    /**
     * Sample code: List_DiscoveredAssets_Subscription.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        listDiscoveredAssetsSubscription(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.discoveredAssets().list(com.azure.core.util.Context.NONE);
    }
}
```

### DiscoveredAssets_ListByResourceGroup

```java
/**
 * Samples for DiscoveredAssets ListByResourceGroup.
 */
public final class DiscoveredAssetsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-09-01-preview/List_DiscoveredAssets_ResourceGroup.json
     */
    /**
     * Sample code: List_DiscoveredAssets_ResourceGroup.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        listDiscoveredAssetsResourceGroup(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.discoveredAssets().listByResourceGroup("myResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### DiscoveredAssets_Update

```java
import com.azure.resourcemanager.deviceregistry.models.DiscoveredAsset;
import com.azure.resourcemanager.deviceregistry.models.DiscoveredAssetUpdateProperties;
import com.azure.resourcemanager.deviceregistry.models.Topic;
import com.azure.resourcemanager.deviceregistry.models.TopicRetainType;

/**
 * Samples for DiscoveredAssets Update.
 */
public final class DiscoveredAssetsUpdateSamples {
    /*
     * x-ms-original-file: 2024-09-01-preview/Update_DiscoveredAsset.json
     */
    /**
     * Sample code: Update_DiscoveredAsset.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void updateDiscoveredAsset(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        DiscoveredAsset resource = manager.discoveredAssets()
            .getByResourceGroupWithResponse("myResourceGroup", "my-discoveredasset", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(
                new DiscoveredAssetUpdateProperties().withDocumentationUri("https://www.example.com/manual-2")
                    .withDefaultTopic(new Topic().withPath("/path/defaultTopic").withRetain(TopicRetainType.NEVER)))
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
     * x-ms-original-file: 2024-09-01-preview/Get_OperationStatus.json
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
     * x-ms-original-file: 2024-09-01-preview/List_Operations.json
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

### SchemaRegistries_CreateOrReplace

```java
import com.azure.resourcemanager.deviceregistry.models.SchemaRegistryProperties;
import com.azure.resourcemanager.deviceregistry.models.SystemAssignedServiceIdentity;
import com.azure.resourcemanager.deviceregistry.models.SystemAssignedServiceIdentityType;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for SchemaRegistries CreateOrReplace.
 */
public final class SchemaRegistriesCreateOrReplaceSamples {
    /*
     * x-ms-original-file: 2024-09-01-preview/Create_SchemaRegistry.json
     */
    /**
     * Sample code: Create_SchemaRegistry.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void createSchemaRegistry(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.schemaRegistries()
            .define("my-schema-registry")
            .withRegion("West Europe")
            .withExistingResourceGroup("myResourceGroup")
            .withTags(mapOf())
            .withProperties(new SchemaRegistryProperties().withNamespace("sr-namespace-001")
                .withDisplayName("Schema Registry namespace 001")
                .withDescription("This is a sample Schema Registry")
                .withStorageAccountContainerUrl("my-blob-storage.blob.core.windows.net/my-container"))
            .withIdentity(new SystemAssignedServiceIdentity().withType(SystemAssignedServiceIdentityType.NONE))
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

### SchemaRegistries_Delete

```java
/**
 * Samples for SchemaRegistries Delete.
 */
public final class SchemaRegistriesDeleteSamples {
    /*
     * x-ms-original-file: 2024-09-01-preview/Delete_SchemaRegistry.json
     */
    /**
     * Sample code: Delete_SchemaRegistry.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void deleteSchemaRegistry(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.schemaRegistries().delete("myResourceGroup", "my-schema-registry", com.azure.core.util.Context.NONE);
    }
}
```

### SchemaRegistries_GetByResourceGroup

```java
/**
 * Samples for SchemaRegistries GetByResourceGroup.
 */
public final class SchemaRegistriesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-09-01-preview/Get_SchemaRegistry.json
     */
    /**
     * Sample code: Get_SchemaRegistry.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void getSchemaRegistry(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.schemaRegistries()
            .getByResourceGroupWithResponse("myResourceGroup", "my-schema-registry", com.azure.core.util.Context.NONE);
    }
}
```

### SchemaRegistries_List

```java
/**
 * Samples for SchemaRegistries List.
 */
public final class SchemaRegistriesListSamples {
    /*
     * x-ms-original-file: 2024-09-01-preview/List_SchemaRegistries_Subscription.json
     */
    /**
     * Sample code: List_SchemaRegistries_Subscription.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        listSchemaRegistriesSubscription(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.schemaRegistries().list(com.azure.core.util.Context.NONE);
    }
}
```

### SchemaRegistries_ListByResourceGroup

```java
/**
 * Samples for SchemaRegistries ListByResourceGroup.
 */
public final class SchemaRegistriesListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-09-01-preview/List_SchemaRegistries_ResourceGroup.json
     */
    /**
     * Sample code: List_SchemaRegistries_ResourceGroup.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        listSchemaRegistriesResourceGroup(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.schemaRegistries().listByResourceGroup("myResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### SchemaRegistries_Update

```java
import com.azure.resourcemanager.deviceregistry.models.SchemaRegistry;
import com.azure.resourcemanager.deviceregistry.models.SchemaRegistryUpdateProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for SchemaRegistries Update.
 */
public final class SchemaRegistriesUpdateSamples {
    /*
     * x-ms-original-file: 2024-09-01-preview/Update_SchemaRegistry.json
     */
    /**
     * Sample code: Update_SchemaRegistry.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void updateSchemaRegistry(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        SchemaRegistry resource = manager.schemaRegistries()
            .getByResourceGroupWithResponse("myResourceGroup", "my-schema-registry", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf())
            .withProperties(new SchemaRegistryUpdateProperties().withDisplayName("Schema Registry namespace 001")
                .withDescription("This is a sample Schema Registry"))
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

### SchemaVersions_CreateOrReplace

```java
import com.azure.resourcemanager.deviceregistry.models.SchemaVersionProperties;

/**
 * Samples for SchemaVersions CreateOrReplace.
 */
public final class SchemaVersionsCreateOrReplaceSamples {
    /*
     * x-ms-original-file: 2024-09-01-preview/Create_SchemaVersion.json
     */
    /**
     * Sample code: Create_SchemaVersion.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void createSchemaVersion(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.schemaVersions()
            .define("1")
            .withExistingSchema("myResourceGroup", "my-schema-registry", "my-schema")
            .withProperties(new SchemaVersionProperties().withDescription("Schema version 1")
                .withSchemaContent(
                    "{\"$schema\": \"http://json-schema.org/draft-07/schema#\",\"type\": \"object\",\"properties\": {\"humidity\": {\"type\": \"string\"},\"temperature\": {\"type\":\"number\"}}}"))
            .create();
    }
}
```

### SchemaVersions_Delete

```java
/**
 * Samples for SchemaVersions Delete.
 */
public final class SchemaVersionsDeleteSamples {
    /*
     * x-ms-original-file: 2024-09-01-preview/Delete_SchemaVersion.json
     */
    /**
     * Sample code: Delete_SchemaVersion.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void deleteSchemaVersion(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.schemaVersions()
            .deleteWithResponse("myResourceGroup", "my-schema-registry", "my-schema", "1",
                com.azure.core.util.Context.NONE);
    }
}
```

### SchemaVersions_Get

```java
/**
 * Samples for SchemaVersions Get.
 */
public final class SchemaVersionsGetSamples {
    /*
     * x-ms-original-file: 2024-09-01-preview/Get_SchemaVersion.json
     */
    /**
     * Sample code: Get_SchemaVersion.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void getSchemaVersion(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.schemaVersions()
            .getWithResponse("myResourceGroup", "my-schema-registry", "my-schema", "1",
                com.azure.core.util.Context.NONE);
    }
}
```

### SchemaVersions_ListBySchema

```java
/**
 * Samples for SchemaVersions ListBySchema.
 */
public final class SchemaVersionsListBySchemaSamples {
    /*
     * x-ms-original-file: 2024-09-01-preview/List_SchemaVersions_Schema.json
     */
    /**
     * Sample code: List_SchemaVersions_Schema.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        listSchemaVersionsSchema(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.schemaVersions()
            .listBySchema("myResourceGroup", "my-schema-registry", "my-schema", com.azure.core.util.Context.NONE);
    }
}
```

### Schemas_CreateOrReplace

```java
import com.azure.resourcemanager.deviceregistry.models.Format;
import com.azure.resourcemanager.deviceregistry.models.SchemaProperties;
import com.azure.resourcemanager.deviceregistry.models.SchemaType;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Schemas CreateOrReplace.
 */
public final class SchemasCreateOrReplaceSamples {
    /*
     * x-ms-original-file: 2024-09-01-preview/Create_Schema.json
     */
    /**
     * Sample code: Create_Schema.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void createSchema(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.schemas()
            .define("my-schema")
            .withExistingSchemaRegistry("myResourceGroup", "my-schema-registry")
            .withProperties(new SchemaProperties().withDisplayName("My Schema")
                .withDescription("This is a sample Schema")
                .withFormat(Format.JSON_SCHEMA_DRAFT7)
                .withSchemaType(SchemaType.MESSAGE_SCHEMA)
                .withTags(mapOf("sampleKey", "fakeTokenPlaceholder")))
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

### Schemas_Delete

```java
/**
 * Samples for Schemas Delete.
 */
public final class SchemasDeleteSamples {
    /*
     * x-ms-original-file: 2024-09-01-preview/Delete_Schema.json
     */
    /**
     * Sample code: Delete_Schema.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void deleteSchema(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.schemas()
            .deleteWithResponse("myResourceGroup", "my-schema-registry", "my-schema", com.azure.core.util.Context.NONE);
    }
}
```

### Schemas_Get

```java
/**
 * Samples for Schemas Get.
 */
public final class SchemasGetSamples {
    /*
     * x-ms-original-file: 2024-09-01-preview/Get_Schema.json
     */
    /**
     * Sample code: Schemas_Get.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void schemasGet(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.schemas()
            .getWithResponse("myResourceGroup", "my-schema-registry", "my-schema", com.azure.core.util.Context.NONE);
    }
}
```

### Schemas_ListBySchemaRegistry

```java
/**
 * Samples for Schemas ListBySchemaRegistry.
 */
public final class SchemasListBySchemaRegistrySamples {
    /*
     * x-ms-original-file: 2024-09-01-preview/List_Schemas_SchemaRegistry.json
     */
    /**
     * Sample code: List_Schemas_SchemaRegistry.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        listSchemasSchemaRegistry(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.schemas()
            .listBySchemaRegistry("myResourceGroup", "my-schema-registry", com.azure.core.util.Context.NONE);
    }
}
```

