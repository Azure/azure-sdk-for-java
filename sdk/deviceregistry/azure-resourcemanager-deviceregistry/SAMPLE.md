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

## AsyncOperationStatus

- [Get](#asyncoperationstatus_get)

## BillingContainers

- [Get](#billingcontainers_get)
- [List](#billingcontainers_list)

## CertificateAuthorities

- [Activate](#certificateauthorities_activate)
- [CreateOrReplace](#certificateauthorities_createorreplace)
- [Delete](#certificateauthorities_delete)
- [Get](#certificateauthorities_get)
- [ListByNamespace](#certificateauthorities_listbynamespace)
- [RevokeAndRotate](#certificateauthorities_revokeandrotate)
- [Update](#certificateauthorities_update)

## CertificatePolicies

- [CreateOrReplace](#certificatepolicies_createorreplace)
- [Delete](#certificatepolicies_delete)
- [Get](#certificatepolicies_get)
- [ListByCertificateAuthority](#certificatepolicies_listbycertificateauthority)
- [Update](#certificatepolicies_update)

## NamespaceAssets

- [CreateOrReplace](#namespaceassets_createorreplace)
- [Delete](#namespaceassets_delete)
- [ExecuteAction](#namespaceassets_executeaction)
- [Get](#namespaceassets_get)
- [ListByNamespace](#namespaceassets_listbynamespace)
- [Update](#namespaceassets_update)

## NamespaceDevices

- [CreateOrReplace](#namespacedevices_createorreplace)
- [Delete](#namespacedevices_delete)
- [Get](#namespacedevices_get)
- [ListByNamespace](#namespacedevices_listbynamespace)
- [Update](#namespacedevices_update)

## NamespaceDiscoveredAssets

- [CreateOrReplace](#namespacediscoveredassets_createorreplace)
- [Delete](#namespacediscoveredassets_delete)
- [Get](#namespacediscoveredassets_get)
- [ListByNamespace](#namespacediscoveredassets_listbynamespace)
- [Update](#namespacediscoveredassets_update)

## NamespaceDiscoveredDevices

- [CreateOrReplace](#namespacediscovereddevices_createorreplace)
- [Delete](#namespacediscovereddevices_delete)
- [Get](#namespacediscovereddevices_get)
- [ListByNamespace](#namespacediscovereddevices_listbynamespace)
- [Update](#namespacediscovereddevices_update)

## Namespaces

- [CreateOrReplace](#namespaces_createorreplace)
- [Delete](#namespaces_delete)
- [GetByResourceGroup](#namespaces_getbyresourcegroup)
- [List](#namespaces_list)
- [ListByResourceGroup](#namespaces_listbyresourcegroup)
- [Migrate](#namespaces_migrate)
- [Update](#namespaces_update)

## OperationStatus

- [Get](#operationstatus_get)

## Operations

- [List](#operations_list)

## RegistryDevices

- [CreateOrReplace](#registrydevices_createorreplace)
- [Delete](#registrydevices_delete)
- [Get](#registrydevices_get)
- [ListByNamespace](#registrydevices_listbynamespace)
- [Update](#registrydevices_update)

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
     * x-ms-original-file: 2026-11-01/CreateOrReplace_AssetEndpointProfile_With_DiscoveredAepRef.json
     */
    /**
     * Sample code: CreateOrReplace_AssetEndpointProfile_With_DiscoveredAepRef.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void createOrReplaceAssetEndpointProfileWithDiscoveredAepRef(
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
     * x-ms-original-file: 2026-11-01/CreateOrReplace_AssetEndpointProfile.json
     */
    /**
     * Sample code: CreateOrReplace_AssetEndpointProfile.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        createOrReplaceAssetEndpointProfile(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
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
     * x-ms-original-file: 2026-11-01/Delete_AssetEndpointProfile.json
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
     * x-ms-original-file: 2026-11-01/Get_AssetEndpointProfile.json
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
     * x-ms-original-file: 2026-11-01/Get_AssetEndpointProfile_With_SyncStatus.json
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
     * x-ms-original-file: 2026-11-01/List_AssetEndpointProfiles_BySubscription.json
     */
    /**
     * Sample code: List_AssetEndpointProfiles_BySubscription.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void listAssetEndpointProfilesBySubscription(
        com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
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
     * x-ms-original-file: 2026-11-01/List_AssetEndpointProfiles_ByResourceGroup.json
     */
    /**
     * Sample code: List_AssetEndpointProfiles_ByResourceGroup.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void listAssetEndpointProfilesByResourceGroup(
        com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
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
     * x-ms-original-file: 2026-11-01/Update_AssetEndpointProfile.json
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
     * x-ms-original-file: 2026-11-01/CreateOrReplace_Asset_Without_DisplayName.json
     */
    /**
     * Sample code: CreateOrReplace_Asset_Without_DisplayName.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        createOrReplaceAssetWithoutDisplayName(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
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

    /*
     * x-ms-original-file: 2026-11-01/CreateOrReplace_Asset_With_DiscoveredAssetRef.json
     */
    /**
     * Sample code: CreateOrReplace_Asset_With_DiscoveredAssetRefs.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void createOrReplaceAssetWithDiscoveredAssetRefs(
        com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
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
     * x-ms-original-file: 2026-11-01/CreateOrReplace_Asset_Without_ExternalAssetId.json
     */
    /**
     * Sample code: CreateOrReplace_Asset_Without_ExternalAssetId.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void createOrReplaceAssetWithoutExternalAssetId(
        com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
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
     * x-ms-original-file: 2026-11-01/CreateOrReplace_Asset_With_ExternalAssetId.json
     */
    /**
     * Sample code: CreateOrReplace_Asset_With_ExternalAssetId.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void createOrReplaceAssetWithExternalAssetId(
        com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
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
     * x-ms-original-file: 2026-11-01/Delete_Asset.json
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
     * x-ms-original-file: 2026-11-01/Get_Asset_With_SyncStatus.json
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
     * x-ms-original-file: 2026-11-01/Get_Asset.json
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
     * x-ms-original-file: 2026-11-01/List_Assets_BySubscription.json
     */
    /**
     * Sample code: List_Assets_BySubscription.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        listAssetsBySubscription(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
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
     * x-ms-original-file: 2026-11-01/List_Assets_ByResourceGroup.json
     */
    /**
     * Sample code: List_Assets_ByResourceGroup.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        listAssetsByResourceGroup(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
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
     * x-ms-original-file: 2026-11-01/Update_Asset.json
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

### AsyncOperationStatus_Get

```java
/**
 * Samples for AsyncOperationStatus Get.
 */
public final class AsyncOperationStatusGetSamples {
    /*
     * x-ms-original-file: 2026-11-01/Get_AsyncOperationStatus.json
     */
    /**
     * Sample code: Get_AsyncOperationStatus.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void getAsyncOperationStatus(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.asyncOperationStatus()
            .getWithResponse("eastus", "00000000-0000-0000-0000-000000000001", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-11-01/Get_BillingContainer.json
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
     * x-ms-original-file: 2026-11-01/List_BillingContainers_BySubscription.json
     */
    /**
     * Sample code: List_BillingContainers_BySubscription.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        listBillingContainersBySubscription(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.billingContainers().list(com.azure.core.util.Context.NONE);
    }
}
```

### CertificateAuthorities_Activate

```java
import com.azure.resourcemanager.deviceregistry.models.ActivateCertificateAuthorityRequest;

/**
 * Samples for CertificateAuthorities Activate.
 */
public final class CertificateAuthoritiesActivateSamples {
    /*
     * x-ms-original-file: 2026-11-01/Activate_CertificateAuthority.json
     */
    /**
     * Sample code: Activate a Certificate Authority.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        activateACertificateAuthority(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.certificateAuthorities()
            .activate("rgdeviceregistry", "mynamespace", "myexternalica",
                new ActivateCertificateAuthorityRequest()
                    .withCertificateChain("-----BEGIN CERTIFICATE-----\\nMIID...\\n-----END CERTIFICATE-----"),
                com.azure.core.util.Context.NONE);
    }
}
```

### CertificateAuthorities_CreateOrReplace

```java
import com.azure.resourcemanager.deviceregistry.models.CertificateAuthorityKeyType;
import com.azure.resourcemanager.deviceregistry.models.ExternalCertificateAuthorityIssuer;
import com.azure.resourcemanager.deviceregistry.models.IntermediateCertificateAuthorityProperties;
import com.azure.resourcemanager.deviceregistry.models.MicrosoftCertificateAuthorityIssuer;
import com.azure.resourcemanager.deviceregistry.models.RootCertificateAuthorityProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for CertificateAuthorities CreateOrReplace.
 */
public final class CertificateAuthoritiesCreateOrReplaceSamples {
    /*
     * x-ms-original-file: 2026-11-01/CreateOrReplace_CertificateAuthority_Root.json
     */
    /**
     * Sample code: Create or Replace a Root Certificate Authority.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void createOrReplaceARootCertificateAuthority(
        com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.certificateAuthorities()
            .define("myrootca")
            .withRegion("East US 2")
            .withExistingNamespace("rgdeviceregistry", "mynamespace")
            .withTags(mapOf("environment", "production"))
            .withProperties(new RootCertificateAuthorityProperties().withKeyType(CertificateAuthorityKeyType.ECC))
            .create();
    }

    /*
     * x-ms-original-file: 2026-11-01/CreateOrReplace_CertificateAuthority_ICAExternalIssuer.json
     */
    /**
     * Sample code: Create or Replace an External Intermediate Certificate Authority.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void createOrReplaceAnExternalIntermediateCertificateAuthority(
        com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.certificateAuthorities()
            .define("myexternalica")
            .withRegion("East US 2")
            .withExistingNamespace("rgdeviceregistry", "mynamespace")
            .withTags(mapOf("environment", "production"))
            .withProperties(
                new IntermediateCertificateAuthorityProperties().withKeyType(CertificateAuthorityKeyType.ECC)
                    .withIssuer(new ExternalCertificateAuthorityIssuer()))
            .create();
    }

    /*
     * x-ms-original-file: 2026-11-01/CreateOrReplace_CertificateAuthority_ICAInternalIssuer.json
     */
    /**
     * Sample code: Create or Replace an Internal Intermediate Certificate Authority.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void createOrReplaceAnInternalIntermediateCertificateAuthority(
        com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.certificateAuthorities()
            .define("myica")
            .withRegion("East US 2")
            .withExistingNamespace("rgdeviceregistry", "mynamespace")
            .withTags(mapOf("environment", "production"))
            .withProperties(new IntermediateCertificateAuthorityProperties()
                .withKeyType(CertificateAuthorityKeyType.ECC)
                .withIssuer(new MicrosoftCertificateAuthorityIssuer().withCertificateAuthorityResourceId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rgdeviceregistry/providers/Microsoft.DeviceRegistry/namespaces/mynamespace/certificateAuthorities/myrootca")))
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

### CertificateAuthorities_Delete

```java
/**
 * Samples for CertificateAuthorities Delete.
 */
public final class CertificateAuthoritiesDeleteSamples {
    /*
     * x-ms-original-file: 2026-11-01/Delete_CertificateAuthority.json
     */
    /**
     * Sample code: Delete a Certificate Authority.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        deleteACertificateAuthority(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.certificateAuthorities()
            .delete("rgdeviceregistry", "mynamespace", "myrootca", com.azure.core.util.Context.NONE);
    }
}
```

### CertificateAuthorities_Get

```java
/**
 * Samples for CertificateAuthorities Get.
 */
public final class CertificateAuthoritiesGetSamples {
    /*
     * x-ms-original-file: 2026-11-01/Get_CertificateAuthority_ICAInternalIssuer.json
     */
    /**
     * Sample code: Get an Internal Intermediate Certificate Authority.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void getAnInternalIntermediateCertificateAuthority(
        com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.certificateAuthorities()
            .getWithResponse("rgdeviceregistry", "mynamespace", "myica", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-11-01/Get_CertificateAuthority_Root.json
     */
    /**
     * Sample code: Get a Root Certificate Authority.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        getARootCertificateAuthority(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.certificateAuthorities()
            .getWithResponse("rgdeviceregistry", "mynamespace", "myrootca", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-11-01/Get_CertificateAuthority_ICAExternalIssuer.json
     */
    /**
     * Sample code: Get an External Intermediate Certificate Authority.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void getAnExternalIntermediateCertificateAuthority(
        com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.certificateAuthorities()
            .getWithResponse("rgdeviceregistry", "mynamespace", "myexternalica", com.azure.core.util.Context.NONE);
    }
}
```

### CertificateAuthorities_ListByNamespace

```java
/**
 * Samples for CertificateAuthorities ListByNamespace.
 */
public final class CertificateAuthoritiesListByNamespaceSamples {
    /*
     * x-ms-original-file: 2026-11-01/List_CertificateAuthorities_ByNamespace.json
     */
    /**
     * Sample code: List_CertificateAuthorities_ByNamespace.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        listCertificateAuthoritiesByNamespace(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.certificateAuthorities()
            .listByNamespace("rgdeviceregistry", "mynamespace", com.azure.core.util.Context.NONE);
    }
}
```

### CertificateAuthorities_RevokeAndRotate

```java
/**
 * Samples for CertificateAuthorities RevokeAndRotate.
 */
public final class CertificateAuthoritiesRevokeAndRotateSamples {
    /*
     * x-ms-original-file: 2026-11-01/RevokeAndRotate_CertificateAuthority.json
     */
    /**
     * Sample code: Revoke and rotate a Certificate Authority.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        revokeAndRotateACertificateAuthority(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.certificateAuthorities()
            .revokeAndRotate("rgdeviceregistry", "mynamespace", "myica", com.azure.core.util.Context.NONE);
    }
}
```

### CertificateAuthorities_Update

```java
import com.azure.resourcemanager.deviceregistry.models.CertificateAuthority;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for CertificateAuthorities Update.
 */
public final class CertificateAuthoritiesUpdateSamples {
    /*
     * x-ms-original-file: 2026-11-01/Update_CertificateAuthority.json
     */
    /**
     * Sample code: Update a Certificate Authority.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        updateACertificateAuthority(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        CertificateAuthority resource = manager.certificateAuthorities()
            .getWithResponse("rgdeviceregistry", "mynamespace", "myrootca", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("environment", "production")).apply();
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

### CertificatePolicies_CreateOrReplace

```java
import com.azure.resourcemanager.deviceregistry.models.CertificatePolicyConfiguration;
import com.azure.resourcemanager.deviceregistry.models.CertificatePolicyProperties;

/**
 * Samples for CertificatePolicies CreateOrReplace.
 */
public final class CertificatePoliciesCreateOrReplaceSamples {
    /*
     * x-ms-original-file: 2026-11-01/CreateOrReplace_CertificatePolicy.json
     */
    /**
     * Sample code: Create or Replace a Certificate Policy.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        createOrReplaceACertificatePolicy(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.certificatePolicies()
            .define("mycertificatepolicy")
            .withRegion("East US 2")
            .withExistingCertificateAuthority("rgdeviceregistry", "mynamespace", "myrootca")
            .withProperties(new CertificatePolicyProperties()
                .withCertificate(new CertificatePolicyConfiguration().withValidityPeriodInDays(30)))
            .create();
    }
}
```

### CertificatePolicies_Delete

```java
/**
 * Samples for CertificatePolicies Delete.
 */
public final class CertificatePoliciesDeleteSamples {
    /*
     * x-ms-original-file: 2026-11-01/Delete_CertificatePolicy.json
     */
    /**
     * Sample code: Delete a Certificate Policy.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        deleteACertificatePolicy(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.certificatePolicies()
            .delete("rgdeviceregistry", "mynamespace", "myrootca", "mycertificatepolicy",
                com.azure.core.util.Context.NONE);
    }
}
```

### CertificatePolicies_Get

```java
/**
 * Samples for CertificatePolicies Get.
 */
public final class CertificatePoliciesGetSamples {
    /*
     * x-ms-original-file: 2026-11-01/Get_CertificatePolicy.json
     */
    /**
     * Sample code: Get a Certificate Policy.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void getACertificatePolicy(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.certificatePolicies()
            .getWithResponse("rgdeviceregistry", "mynamespace", "myrootca", "mycertificatepolicy",
                com.azure.core.util.Context.NONE);
    }
}
```

### CertificatePolicies_ListByCertificateAuthority

```java
/**
 * Samples for CertificatePolicies ListByCertificateAuthority.
 */
public final class CertificatePoliciesListByCertificateAuthoritySamples {
    /*
     * x-ms-original-file: 2026-11-01/List_CertificatePolicies_ByCertificateAuthority.json
     */
    /**
     * Sample code: List Certificate Policies for a Certificate Authority.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void listCertificatePoliciesForACertificateAuthority(
        com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.certificatePolicies()
            .listByCertificateAuthority("rgdeviceregistry", "mynamespace", "myrootca",
                com.azure.core.util.Context.NONE);
    }
}
```

### CertificatePolicies_Update

```java
import com.azure.resourcemanager.deviceregistry.models.CertificatePolicy;
import com.azure.resourcemanager.deviceregistry.models.CertificatePolicyUpdateProperties;
import com.azure.resourcemanager.deviceregistry.models.OptionalPropertiesCertificatePolicyConfiguration;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for CertificatePolicies Update.
 */
public final class CertificatePoliciesUpdateSamples {
    /*
     * x-ms-original-file: 2026-11-01/Update_CertificatePolicy.json
     */
    /**
     * Sample code: Update a Certificate Policy.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        updateACertificatePolicy(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        CertificatePolicy resource = manager.certificatePolicies()
            .getWithResponse("rgdeviceregistry", "mynamespace", "myrootca", "mycertificatepolicy",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("environment", "production"))
            .withProperties(new CertificatePolicyUpdateProperties()
                .withCertificate(new OptionalPropertiesCertificatePolicyConfiguration().withValidityPeriodInDays(60)))
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

### NamespaceAssets_CreateOrReplace

```java
import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.deviceregistry.models.BrokerStateStoreDestinationConfiguration;
import com.azure.resourcemanager.deviceregistry.models.DatasetBrokerStateStoreDestination;
import com.azure.resourcemanager.deviceregistry.models.DeviceRef;
import com.azure.resourcemanager.deviceregistry.models.EventMqttDestination;
import com.azure.resourcemanager.deviceregistry.models.EventStorageDestination;
import com.azure.resourcemanager.deviceregistry.models.ExtendedLocation;
import com.azure.resourcemanager.deviceregistry.models.ManagementAction;
import com.azure.resourcemanager.deviceregistry.models.ManagementActionType;
import com.azure.resourcemanager.deviceregistry.models.ManagementGroup;
import com.azure.resourcemanager.deviceregistry.models.MqttDestinationConfiguration;
import com.azure.resourcemanager.deviceregistry.models.MqttDestinationQos;
import com.azure.resourcemanager.deviceregistry.models.NamespaceAssetProperties;
import com.azure.resourcemanager.deviceregistry.models.NamespaceDataset;
import com.azure.resourcemanager.deviceregistry.models.NamespaceDatasetDataPoint;
import com.azure.resourcemanager.deviceregistry.models.NamespaceEvent;
import com.azure.resourcemanager.deviceregistry.models.NamespaceEventGroup;
import com.azure.resourcemanager.deviceregistry.models.NamespaceStream;
import com.azure.resourcemanager.deviceregistry.models.StorageDestinationConfiguration;
import com.azure.resourcemanager.deviceregistry.models.StreamMqttDestination;
import com.azure.resourcemanager.deviceregistry.models.StreamStorageDestination;
import com.azure.resourcemanager.deviceregistry.models.TopicRetainType;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for NamespaceAssets CreateOrReplace.
 */
public final class NamespaceAssetsCreateOrReplaceSamples {
    /*
     * x-ms-original-file: 2026-11-01/CreateOrReplace_NamespaceAsset.json
     */
    /**
     * Sample code: CreateOrReplace_NamespaceAsset.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        createOrReplaceNamespaceAsset(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.namespaceAssets()
            .define("my-asset-1")
            .withRegion("West Europe")
            .withExistingNamespace("myResourceGroup", "my-namespace-1")
            .withExtendedLocation(new ExtendedLocation().withType("CustomLocation")
                .withName(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/myResourceGroup/providers/microsoft.extendedlocation/customlocations/location1"))
            .withTags(mapOf("site", "building-1"))
            .withProperties(new NamespaceAssetProperties().withEnabled(true)
                .withExternalAssetId("8ZBA6LRHU0A458969")
                .withDisplayName("AssetDisplayName")
                .withDescription("This is a sample Asset")
                .withDeviceRef(new DeviceRef().withDeviceName("device1").withEndpointName("opcuaendpointname"))
                .withAssetTypeRefs(Arrays.asList("myAssetTypeRef1"))
                .withManufacturer("Contoso")
                .withManufacturerUri("https://www.contoso.com/manufacturerUri")
                .withModel("ContosoModel")
                .withProductCode("fakeTokenPlaceholder")
                .withHardwareRevision("1.0")
                .withSoftwareRevision("2.0")
                .withDocumentationUri("https://www.example.com/manual")
                .withSerialNumber("64-103816-519918-8")
                .withAttributes(mapOf("floor", BinaryData.fromBytes("1".getBytes(StandardCharsets.UTF_8))))
                .withDiscoveredAssetRefs(Arrays.asList("discoveredAsset1"))
                .withDefaultDatasetsConfiguration(
                    "{\"publishingInterval\":10,\"samplingInterval\":15,\"queueSize\":20}")
                .withDefaultEventsConfiguration("{\"publishingInterval\":10,\"samplingInterval\":15,\"queueSize\":20}")
                .withDefaultStreamsConfiguration("{\"publishingInterval\":10,\"samplingInterval\":15,\"queueSize\":20}")
                .withDefaultManagementGroupsConfiguration("{\"retryCount\":10,\"retryBackoffInterval\":15}")
                .withDefaultDatasetsDestinations(Arrays.asList(new DatasetBrokerStateStoreDestination()
                    .withConfiguration(new BrokerStateStoreDestinationConfiguration().withKey("fakeTokenPlaceholder"))))
                .withDefaultEventsDestinations(Arrays.asList(new EventStorageDestination()
                    .withConfiguration(new StorageDestinationConfiguration().withPath("/tmp"))))
                .withDefaultStreamsDestinations(Arrays.asList(new StreamMqttDestination()
                    .withConfiguration(new MqttDestinationConfiguration().withTopic("/contoso/test")
                        .withRetain(TopicRetainType.NEVER)
                        .withQos(MqttDestinationQos.QOS0)
                        .withTtl(3600L))))
                .withDatasets(Arrays.asList(new NamespaceDataset().withName("dataset1")
                    .withDataSource("nsu=http://microsoft.com/Opc/OpcPlc/Oven;i=5")
                    .withTypeRef("dataset1TypeRef")
                    .withDatasetConfiguration("{\"publishingInterval\":10,\"samplingInterval\":15,\"queueSize\":20}")
                    .withDestinations(Arrays.asList(new DatasetBrokerStateStoreDestination().withConfiguration(
                        new BrokerStateStoreDestinationConfiguration().withKey("fakeTokenPlaceholder"))))
                    .withDataPoints(Arrays.asList(new NamespaceDatasetDataPoint().withName("dataset1DataPoint1")
                        .withDataSource("nsu=http://microsoft.com/Opc/OpcPlc/;s=FastUInt3")
                        .withDataPointConfiguration("{\"publishingInterval\":8,\"samplingInterval\":8,\"queueSize\":4}")
                        .withTypeRef("dataset1DataPoint1TypeRef")))))
                .withEventGroups(Arrays.asList(new NamespaceEventGroup().withName("default")
                    .withEvents(Arrays.asList(new NamespaceEvent().withName("event1")
                        .withDataSource("nsu=http://microsoft.com/Opc/OpcPlc/;s=FastUInt5")
                        .withEventConfiguration("{\"publishingInterval\":7,\"samplingInterval\":1,\"queueSize\":8}")
                        .withDestinations(Arrays.asList(new EventMqttDestination()
                            .withConfiguration(new MqttDestinationConfiguration().withTopic("/contoso/testEvent1")
                                .withRetain(TopicRetainType.KEEP)
                                .withQos(MqttDestinationQos.QOS0)
                                .withTtl(7200L))))
                        .withTypeRef("event1Ref")))))
                .withStreams(Arrays.asList(new NamespaceStream().withName("stream1")
                    .withStreamConfiguration("{\"publishingInterval\":8,\"samplingInterval\":8,\"queueSize\":4}")
                    .withTypeRef("stream1TypeRef")
                    .withDestinations(Arrays.asList(new StreamStorageDestination()
                        .withConfiguration(new StorageDestinationConfiguration().withPath("/tmp/stream1"))))))
                .withManagementGroups(Arrays.asList(new ManagementGroup().withName("managementGroup1")
                    .withManagementGroupConfiguration("{\"retryCount\":10,\"retryBackoffInterval\":15}")
                    .withTypeRef("managementGroup1TypeRef")
                    .withDefaultTopic("/contoso/managementGroup1")
                    .withDefaultTimeoutInSeconds(100)
                    .withActions(Arrays.asList(new ManagementAction().withName("action1")
                        .withActionConfiguration("{\"retryCount\":5,\"retryBackoffInterval\":5}")
                        .withTargetUri("/onvif/device_service?ONVIFProfile=Profile1")
                        .withTypeRef("action1TypeRef")
                        .withTopic("/contoso/managementGroup1/action1")
                        .withActionType(ManagementActionType.CALL)
                        .withTimeoutInSeconds(60))))))
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

### NamespaceAssets_Delete

```java
/**
 * Samples for NamespaceAssets Delete.
 */
public final class NamespaceAssetsDeleteSamples {
    /*
     * x-ms-original-file: 2026-11-01/Delete_NamespaceAsset.json
     */
    /**
     * Sample code: Delete_NamespaceAsset.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void deleteNamespaceAsset(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.namespaceAssets()
            .delete("myResourceGroup", "adr-namespace-gbk0925-n01", "adr-asset-gbk0925-n01",
                com.azure.core.util.Context.NONE);
    }
}
```

### NamespaceAssets_ExecuteAction

```java
import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.deviceregistry.models.NamespaceAssetExecuteActionRequest;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for NamespaceAssets ExecuteAction.
 */
public final class NamespaceAssetsExecuteActionSamples {
    /*
     * x-ms-original-file: 2026-11-01/ExecuteAction_Assets_Namespace.json
     */
    /**
     * Sample code: Namespace Asset Execute Action.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        namespaceAssetExecuteAction(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.namespaceAssets()
            .executeAction("myResourceGroup", "my-namespace-1", "my-asset-1",
                new NamespaceAssetExecuteActionRequest().withManagementActionName("my-asset-action-1")
                    .withManagementGroupName("my-asset-group-1")
                    .withPayload(mapOf("prop1", BinaryData.fromBytes("value1".getBytes(StandardCharsets.UTF_8)),
                        "prop2", BinaryData.fromBytes("123".getBytes(StandardCharsets.UTF_8)), "prop3",
                        BinaryData.fromBytes("true".getBytes(StandardCharsets.UTF_8)), "prop4",
                        BinaryData.fromBytes("{subProp1=subValue1}".getBytes(StandardCharsets.UTF_8)))),
                com.azure.core.util.Context.NONE);
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

### NamespaceAssets_Get

```java
/**
 * Samples for NamespaceAssets Get.
 */
public final class NamespaceAssetsGetSamples {
    /*
     * x-ms-original-file: 2026-11-01/Get_NamespaceAsset.json
     */
    /**
     * Sample code: Get_NamespaceAsset.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void getNamespaceAsset(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.namespaceAssets()
            .getWithResponse("myResourceGroup", "my-namespace-1", "my-asset-1", com.azure.core.util.Context.NONE);
    }
}
```

### NamespaceAssets_ListByNamespace

```java
/**
 * Samples for NamespaceAssets ListByNamespace.
 */
public final class NamespaceAssetsListByNamespaceSamples {
    /*
     * x-ms-original-file: 2026-11-01/List_NamespaceAssets_ByNamespace.json
     */
    /**
     * Sample code: List_NamespaceAssets_ByNamespace.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        listNamespaceAssetsByNamespace(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.namespaceAssets()
            .listByNamespace("myResourceGroup", "adr-namespace-gbk0925-n01", com.azure.core.util.Context.NONE);
    }
}
```

### NamespaceAssets_Update

```java
import com.azure.resourcemanager.deviceregistry.models.NamespaceAsset;
import com.azure.resourcemanager.deviceregistry.models.NamespaceAssetUpdateProperties;

/**
 * Samples for NamespaceAssets Update.
 */
public final class NamespaceAssetsUpdateSamples {
    /*
     * x-ms-original-file: 2026-11-01/Update_NamespaceAsset.json
     */
    /**
     * Sample code: Update_NamespaceAssets.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void updateNamespaceAssets(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        NamespaceAsset resource = manager.namespaceAssets()
            .getWithResponse("myResourceGroup", "my-namespace-1", "my-asset-1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new NamespaceAssetUpdateProperties().withEnabled(true)
                .withDisplayName("AssetDisplayNameUpdate")
                .withDescription("This is a sample updated Asset"))
            .apply();
    }
}
```

### NamespaceDevices_CreateOrReplace

```java
import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.deviceregistry.models.AuthenticationMethod;
import com.azure.resourcemanager.deviceregistry.models.DeviceMessagingEndpoint;
import com.azure.resourcemanager.deviceregistry.models.ExtendedLocation;
import com.azure.resourcemanager.deviceregistry.models.HostAuthentication;
import com.azure.resourcemanager.deviceregistry.models.InboundEndpoints;
import com.azure.resourcemanager.deviceregistry.models.MessagingEndpoints;
import com.azure.resourcemanager.deviceregistry.models.NamespaceDeviceProperties;
import com.azure.resourcemanager.deviceregistry.models.OutboundEndpoints;
import com.azure.resourcemanager.deviceregistry.models.TrustSettings;
import com.azure.resourcemanager.deviceregistry.models.UsernamePasswordCredentials;
import com.azure.resourcemanager.deviceregistry.models.X509CertificateCredentials;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for NamespaceDevices CreateOrReplace.
 */
public final class NamespaceDevicesCreateOrReplaceSamples {
    /*
     * x-ms-original-file: 2026-11-01/CreateOrReplace_NamespaceDevice_Edge_x509.json
     */
    /**
     * Sample code: Create edge enabled device with x509 inbound authentication.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void createEdgeEnabledDeviceWithX509InboundAuthentication(
        com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.namespaceDevices()
            .define("namespace-device-on-edge")
            .withRegion("West Europe")
            .withExistingNamespace("myResourceGroup", "adr-namespace-gbk0925-n01")
            .withProperties(
                new NamespaceDeviceProperties().withEnabled(true)
                    .withExternalDeviceId("unique-edge-device-identifier")
                    .withEndpoints(
                        new MessagingEndpoints().withInbound(mapOf("theV1OPCUAEndpoint",
                            new InboundEndpoints().withEndpointType("microsoft.opcua")
                                .withAddress("opc.tcp://192.168.86.23:51211/UA/SampleServer")
                                .withVersion("2")
                                .withAuthentication(new HostAuthentication()
                                    .withMethod(AuthenticationMethod.CERTIFICATE)
                                    .withX509Credentials(new X509CertificateCredentials()
                                        .withCertificateSecretName("fakeTokenPlaceholder")
                                        .withKeySecretName("fakeTokenPlaceholder")
                                        .withIntermediateCertificatesSecretName("fakeTokenPlaceholder"))),
                            "theV2OPCUAEndpoint",
                            new InboundEndpoints().withEndpointType("microsoft.opcua")
                                .withAddress("opc.tcp://192.168.86.23:51211/UA/SampleServer")
                                .withVersion("2")
                                .withAuthentication(
                                    new HostAuthentication().withMethod(AuthenticationMethod.CERTIFICATE)
                                        .withX509Credentials(new X509CertificateCredentials()
                                            .withCertificateSecretName("fakeTokenPlaceholder")))
                                .withTrustSettings(new TrustSettings().withTrustList("trust-secret-reference")))))
                    .withAttributes(
                        mapOf("deviceType", BinaryData.fromBytes("OPCUAServers".getBytes(StandardCharsets.UTF_8)),
                            "deviceOwner", BinaryData.fromBytes("OT".getBytes(StandardCharsets.UTF_8)),
                            "deviceCategory", BinaryData.fromBytes("16".getBytes(StandardCharsets.UTF_8)))))
            .withExtendedLocation(new ExtendedLocation().withType("CustomLocation")
                .withName(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/myResourceGroup/providers/microsoft.extendedlocation/customlocations/location1"))
            .create();
    }

    /*
     * x-ms-original-file: 2026-11-01/CreateOrReplace_NamespaceDevice_Edge_Anonymous.json
     */
    /**
     * Sample code: Create edge enabled device with anonymous host authentication.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void createEdgeEnabledDeviceWithAnonymousHostAuthentication(
        com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.namespaceDevices()
            .define("namespace-device-on-edge")
            .withRegion("West Europe")
            .withExistingNamespace("myResourceGroup", "adr-namespace-gbk0925-n01")
            .withProperties(new NamespaceDeviceProperties().withEnabled(true)
                .withExternalDeviceId("unique-edge-device-identifier")
                .withEndpoints(new MessagingEndpoints().withInbound(mapOf("theOnlyOPCUABroker",
                    new InboundEndpoints().withEndpointType("microsoft.opcua")
                        .withAddress("opc.tcp://192.168.86.23:51211/UA/SampleServer")
                        .withVersion("2")
                        .withAuthentication(new HostAuthentication().withMethod(AuthenticationMethod.ANONYMOUS)))))
                .withAttributes(
                    mapOf("deviceType", BinaryData.fromBytes("dough-maker".getBytes(StandardCharsets.UTF_8)),
                        "deviceOwner", BinaryData.fromBytes("OT".getBytes(StandardCharsets.UTF_8)), "deviceCategory",
                        BinaryData.fromBytes("16".getBytes(StandardCharsets.UTF_8)))))
            .withExtendedLocation(new ExtendedLocation().withType("CustomLocation")
                .withName(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/myResourceGroup/providers/microsoft.extendedlocation/customlocations/location1"))
            .create();
    }

    /*
     * x-ms-original-file: 2026-11-01/CreateOrReplace_NamespaceDevice_Edge_UsernamePass.json
     */
    /**
     * Sample code: Create edge enabled device with UsernamesPassword inbound authentication.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void createEdgeEnabledDeviceWithUsernamesPasswordInboundAuthentication(
        com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.namespaceDevices()
            .define("namespace-device-on-edge")
            .withRegion("West Europe")
            .withExistingNamespace("myResourceGroup", "adr-namespace-gbk0925-n01")
            .withProperties(new NamespaceDeviceProperties().withEnabled(true)
                .withExternalDeviceId("unique-edge-device-identifier")
                .withEndpoints(new MessagingEndpoints().withInbound(mapOf("theOnlyOPCUABroker",
                    new InboundEndpoints().withEndpointType("microsoft.opcua")
                        .withAddress("opc.tcp://192.168.86.23:51211/UA/SampleServer")
                        .withVersion("2")
                        .withAuthentication(new HostAuthentication().withMethod(AuthenticationMethod.USERNAME_PASSWORD)
                            .withUsernamePasswordCredentials(
                                new UsernamePasswordCredentials().withUsernameSecretName("fakeTokenPlaceholder")
                                    .withPasswordSecretName("fakeTokenPlaceholder"))))))
                .withAttributes(mapOf("deviceType", BinaryData.fromBytes("sensor".getBytes(StandardCharsets.UTF_8)),
                    "deviceOwner", BinaryData.fromBytes("IT".getBytes(StandardCharsets.UTF_8)), "deviceCategory",
                    BinaryData.fromBytes("16".getBytes(StandardCharsets.UTF_8)))))
            .withExtendedLocation(new ExtendedLocation().withType("CustomLocation")
                .withName(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/myResourceGroup/providers/microsoft.extendedlocation/customlocations/location1"))
            .create();
    }

    /*
     * x-ms-original-file: 2026-11-01/CreateOrReplace_NamespaceDevice.json
     */
    /**
     * Sample code: CreateOrReplace_NamespaceDevices.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        createOrReplaceNamespaceDevices(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.namespaceDevices()
            .define("dev-namespace-gbk0925-n01")
            .withRegion("West Europe")
            .withExistingNamespace("myResourceGroup", "adr-namespace-gbk0925-n01")
            .withProperties(new NamespaceDeviceProperties().withEnabled(true)
                .withExternalDeviceId("adr-smart-device3-7a848b15-af47-40a7-8c06-a3f43314d44f")
                .withEndpoints(new MessagingEndpoints()
                    .withOutbound(new OutboundEndpoints().withAssigned(mapOf("eventGridEndpoint",
                        new DeviceMessagingEndpoint().withEndpointType("Microsoft.Devices")
                            .withAddress("https://myeventgridtopic.westeurope-1.eventgrid.azure.net/api/events")))))
                .withAttributes(mapOf("deviceType", BinaryData.fromBytes("sensor".getBytes(StandardCharsets.UTF_8)),
                    "deviceOwner", BinaryData.fromBytes("IT".getBytes(StandardCharsets.UTF_8)), "deviceCategory",
                    BinaryData.fromBytes("16".getBytes(StandardCharsets.UTF_8)))))
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

### NamespaceDevices_Delete

```java
/**
 * Samples for NamespaceDevices Delete.
 */
public final class NamespaceDevicesDeleteSamples {
    /*
     * x-ms-original-file: 2026-11-01/Delete_NamespaceDevice.json
     */
    /**
     * Sample code: Delete_NamespaceDevice.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void deleteNamespaceDevice(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.namespaceDevices()
            .delete("myResourceGroup", "adr-namespace-gbk0925-n01", "adr-device-gbk0925-n01",
                com.azure.core.util.Context.NONE);
    }
}
```

### NamespaceDevices_Get

```java
/**
 * Samples for NamespaceDevices Get.
 */
public final class NamespaceDevicesGetSamples {
    /*
     * x-ms-original-file: 2026-11-01/Get_NamespaceDeviceWithEndpointErrorStatus.json
     */
    /**
     * Sample code: Get NamespaceDevice with Endpoint Error Status.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void getNamespaceDeviceWithEndpointErrorStatus(
        com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.namespaceDevices()
            .getWithResponse("myResourceGroup", "my-namespace-1", "my-device-name", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-11-01/Get_NamespaceDevice.json
     */
    /**
     * Sample code: Get_NamespaceDevice.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void getNamespaceDevice(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.namespaceDevices()
            .getWithResponse("myResourceGroup", "my-namespace-1", "my-device-name", com.azure.core.util.Context.NONE);
    }
}
```

### NamespaceDevices_ListByNamespace

```java
/**
 * Samples for NamespaceDevices ListByNamespace.
 */
public final class NamespaceDevicesListByNamespaceSamples {
    /*
     * x-ms-original-file: 2026-11-01/List_NamespaceDevices_ByNamespace.json
     */
    /**
     * Sample code: List_NamespaceDevices_ByNamespace.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        listNamespaceDevicesByNamespace(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.namespaceDevices()
            .listByNamespace("myResourceGroup", "adr-namespace-gbk0925-n01", com.azure.core.util.Context.NONE);
    }
}
```

### NamespaceDevices_Update

```java
import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.deviceregistry.models.DeviceMessagingEndpoint;
import com.azure.resourcemanager.deviceregistry.models.MessagingEndpoints;
import com.azure.resourcemanager.deviceregistry.models.NamespaceDevice;
import com.azure.resourcemanager.deviceregistry.models.NamespaceDeviceUpdateProperties;
import com.azure.resourcemanager.deviceregistry.models.OutboundEndpoints;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for NamespaceDevices Update.
 */
public final class NamespaceDevicesUpdateSamples {
    /*
     * x-ms-original-file: 2026-11-01/Update_NamespaceDevice.json
     */
    /**
     * Sample code: Update_NamespaceDevices.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void updateNamespaceDevices(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        NamespaceDevice resource = manager.namespaceDevices()
            .getWithResponse("myResourceGroup", "adr-namespace-gbk0925-n01", "dev-namespace-gbk0925-n01",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new NamespaceDeviceUpdateProperties()
                .withEndpoints(new MessagingEndpoints()
                    .withOutbound(new OutboundEndpoints().withAssigned(mapOf("eventGridEndpoint",
                        new DeviceMessagingEndpoint().withEndpointType("Microsoft.Devices")
                            .withAddress("https://myeventgridtopic.westeurope-1.eventgrid.azure.net/api/events")))))
                .withAttributes(mapOf("deviceType", BinaryData.fromBytes("sensor".getBytes(StandardCharsets.UTF_8)),
                    "deviceOwner", BinaryData.fromBytes("IT".getBytes(StandardCharsets.UTF_8)), "deviceCategory",
                    BinaryData.fromBytes("16".getBytes(StandardCharsets.UTF_8))))
                .withEnabled(true))
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

### NamespaceDiscoveredAssets_CreateOrReplace

```java
import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.deviceregistry.models.BrokerStateStoreDestinationConfiguration;
import com.azure.resourcemanager.deviceregistry.models.DatasetBrokerStateStoreDestination;
import com.azure.resourcemanager.deviceregistry.models.DeviceRef;
import com.azure.resourcemanager.deviceregistry.models.EventMqttDestination;
import com.azure.resourcemanager.deviceregistry.models.EventStorageDestination;
import com.azure.resourcemanager.deviceregistry.models.ExtendedLocation;
import com.azure.resourcemanager.deviceregistry.models.MqttDestinationConfiguration;
import com.azure.resourcemanager.deviceregistry.models.MqttDestinationQos;
import com.azure.resourcemanager.deviceregistry.models.NamespaceDiscoveredAssetProperties;
import com.azure.resourcemanager.deviceregistry.models.NamespaceDiscoveredDataset;
import com.azure.resourcemanager.deviceregistry.models.NamespaceDiscoveredDatasetDataPoint;
import com.azure.resourcemanager.deviceregistry.models.NamespaceDiscoveredEvent;
import com.azure.resourcemanager.deviceregistry.models.NamespaceDiscoveredEventGroup;
import com.azure.resourcemanager.deviceregistry.models.NamespaceDiscoveredManagementAction;
import com.azure.resourcemanager.deviceregistry.models.NamespaceDiscoveredManagementActionType;
import com.azure.resourcemanager.deviceregistry.models.NamespaceDiscoveredManagementGroup;
import com.azure.resourcemanager.deviceregistry.models.NamespaceDiscoveredStream;
import com.azure.resourcemanager.deviceregistry.models.StorageDestinationConfiguration;
import com.azure.resourcemanager.deviceregistry.models.StreamMqttDestination;
import com.azure.resourcemanager.deviceregistry.models.StreamStorageDestination;
import com.azure.resourcemanager.deviceregistry.models.TopicRetainType;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for NamespaceDiscoveredAssets CreateOrReplace.
 */
public final class NamespaceDiscoveredAssetsCreateOrReplaceSamples {
    /*
     * x-ms-original-file: 2026-11-01/CreateOrReplace_NamespaceDiscoveredAsset.json
     */
    /**
     * Sample code: CreateOrReplace_NamespaceDiscoveredAsset.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void createOrReplaceNamespaceDiscoveredAsset(
        com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.namespaceDiscoveredAssets()
            .define("my-discoveredasset-1")
            .withRegion("West Europe")
            .withExistingNamespace("myResourceGroup", "my-namespace-1")
            .withExtendedLocation(new ExtendedLocation().withType("CustomLocation")
                .withName(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/myResourceGroup/providers/microsoft.extendedlocation/customlocations/location1"))
            .withTags(mapOf("site", "building-1"))
            .withProperties(new NamespaceDiscoveredAssetProperties()
                .withDeviceRef(new DeviceRef().withDeviceName("myDevice").withEndpointName("opcuaendpointname"))
                .withAssetTypeRefs(Arrays.asList("myAssetTypeRef1"))
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
                .withAttributes(mapOf("floor", BinaryData.fromBytes("1".getBytes(StandardCharsets.UTF_8))))
                .withDefaultDatasetsConfiguration(
                    "{\"publishingInterval\":10,\"samplingInterval\":15,\"queueSize\":20}")
                .withDefaultEventsConfiguration("{\"publishingInterval\":10,\"samplingInterval\":15,\"queueSize\":20}")
                .withDefaultStreamsConfiguration("{\"publishingInterval\":10,\"samplingInterval\":15,\"queueSize\":20}")
                .withDefaultManagementGroupsConfiguration("{\"retryCount\":10,\"retryBackoffInterval\":15}")
                .withDefaultDatasetsDestinations(Arrays.asList(new DatasetBrokerStateStoreDestination()
                    .withConfiguration(new BrokerStateStoreDestinationConfiguration().withKey("fakeTokenPlaceholder"))))
                .withDefaultEventsDestinations(Arrays.asList(new EventStorageDestination()
                    .withConfiguration(new StorageDestinationConfiguration().withPath("/tmp"))))
                .withDefaultStreamsDestinations(Arrays.asList(new StreamMqttDestination()
                    .withConfiguration(new MqttDestinationConfiguration().withTopic("/contoso/test")
                        .withRetain(TopicRetainType.NEVER)
                        .withQos(MqttDestinationQos.QOS0)
                        .withTtl(3600L))))
                .withDatasets(Arrays.asList(new NamespaceDiscoveredDataset().withName("dataset1")
                    .withDataSource("nsu=http://microsoft.com/Opc/OpcPlc/Boiler;i=5")
                    .withTypeRef("dataset1TypeRef")
                    .withDatasetConfiguration("{\"publishingInterval\":10,\"samplingInterval\":15,\"queueSize\":20}")
                    .withDestinations(Arrays.asList(new DatasetBrokerStateStoreDestination().withConfiguration(
                        new BrokerStateStoreDestinationConfiguration().withKey("fakeTokenPlaceholder"))))
                    .withDataPoints(Arrays.asList(new NamespaceDiscoveredDatasetDataPoint()
                        .withName("dataset1DataPoint1")
                        .withDataSource("nsu=http://microsoft.com/Opc/OpcPlc/;s=FastUInt3")
                        .withDataPointConfiguration("{\"publishingInterval\":8,\"samplingInterval\":8,\"queueSize\":4}")
                        .withLastUpdatedOn(OffsetDateTime.parse("2024-04-09T14:20:00.52Z"))
                        .withTypeRef("dataset1DataPoint1TypeRef")))
                    .withLastUpdatedOn(OffsetDateTime.parse("2024-04-09T14:20:00.52Z"))))
                .withEventGroups(Arrays.asList(new NamespaceDiscoveredEventGroup().withName("default")
                    .withEvents(Arrays.asList(new NamespaceDiscoveredEvent().withName("event1")
                        .withDataSource("nsu=http://microsoft.com/Opc/OpcPlc/;s=FastUInt3")
                        .withEventConfiguration("{\"publishingInterval\":7,\"samplingInterval\":1,\"queueSize\":8}")
                        .withDestinations(Arrays.asList(new EventMqttDestination()
                            .withConfiguration(new MqttDestinationConfiguration().withTopic("/contoso/testEvent1")
                                .withRetain(TopicRetainType.KEEP)
                                .withQos(MqttDestinationQos.QOS0)
                                .withTtl(7200L))))
                        .withTypeRef("event1Ref")
                        .withLastUpdatedOn(OffsetDateTime.parse("2024-04-09T14:20:00.52Z"))))))
                .withStreams(Arrays.asList(new NamespaceDiscoveredStream().withName("stream1")
                    .withStreamConfiguration("{\"publishingInterval\":8,\"samplingInterval\":8,\"queueSize\":4}")
                    .withTypeRef("stream1TypeRef")
                    .withDestinations(Arrays.asList(new StreamStorageDestination()
                        .withConfiguration(new StorageDestinationConfiguration().withPath("/tmp/stream1"))))
                    .withLastUpdatedOn(OffsetDateTime.parse("2024-04-09T14:20:00.52Z"))))
                .withManagementGroups(
                    Arrays.asList(new NamespaceDiscoveredManagementGroup().withName("managementGroup1")
                        .withManagementGroupConfiguration("{\"retryCount\":10,\"retryBackoffInterval\":15}")
                        .withTypeRef("managementGroup1TypeRef")
                        .withDefaultTopic("/contoso/managementGroup1")
                        .withDefaultTimeoutInSeconds(100)
                        .withActions(Arrays.asList(new NamespaceDiscoveredManagementAction().withName("action1")
                            .withActionConfiguration("{\"retryCount\":5,\"retryBackoffInterval\":5}")
                            .withTargetUri("/onvif/device_service?ONVIFProfile=Profile1")
                            .withTypeRef("action1TypeRef")
                            .withTopic("/contoso/managementGroup1/action1")
                            .withActionType(NamespaceDiscoveredManagementActionType.CALL)
                            .withTimeoutInSeconds(60)
                            .withLastUpdatedOn(OffsetDateTime.parse("2024-04-09T14:20:00.52Z"))))
                        .withLastUpdatedOn(OffsetDateTime.parse("2024-04-09T14:20:00.52Z")))))
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

### NamespaceDiscoveredAssets_Delete

```java
/**
 * Samples for NamespaceDiscoveredAssets Delete.
 */
public final class NamespaceDiscoveredAssetsDeleteSamples {
    /*
     * x-ms-original-file: 2026-11-01/Delete_NamespaceDiscoveredAsset.json
     */
    /**
     * Sample code: Delete_NamespaceDiscoveredAsset.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        deleteNamespaceDiscoveredAsset(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.namespaceDiscoveredAssets()
            .delete("myResourceGroup", "my-namespace-1", "my-discoveredasset-1", com.azure.core.util.Context.NONE);
    }
}
```

### NamespaceDiscoveredAssets_Get

```java
/**
 * Samples for NamespaceDiscoveredAssets Get.
 */
public final class NamespaceDiscoveredAssetsGetSamples {
    /*
     * x-ms-original-file: 2026-11-01/Get_NamespaceDiscoveredAsset.json
     */
    /**
     * Sample code: Get_NamespaceDiscoveredAsset.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        getNamespaceDiscoveredAsset(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.namespaceDiscoveredAssets()
            .getWithResponse("myResourceGroup", "my-namespace-1", "my-discoveredasset-1",
                com.azure.core.util.Context.NONE);
    }
}
```

### NamespaceDiscoveredAssets_ListByNamespace

```java
/**
 * Samples for NamespaceDiscoveredAssets ListByNamespace.
 */
public final class NamespaceDiscoveredAssetsListByNamespaceSamples {
    /*
     * x-ms-original-file: 2026-11-01/List_NamespaceDiscoveredAssets_ByNamespace.json
     */
    /**
     * Sample code: List_NamespaceDiscoveredAssets_ByNamespace.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void listNamespaceDiscoveredAssetsByNamespace(
        com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.namespaceDiscoveredAssets()
            .listByNamespace("myResourceGroup", "my-namespace-1", com.azure.core.util.Context.NONE);
    }
}
```

### NamespaceDiscoveredAssets_Update

```java
import com.azure.resourcemanager.deviceregistry.models.NamespaceDiscoveredAsset;
import com.azure.resourcemanager.deviceregistry.models.NamespaceDiscoveredAssetUpdateProperties;

/**
 * Samples for NamespaceDiscoveredAssets Update.
 */
public final class NamespaceDiscoveredAssetsUpdateSamples {
    /*
     * x-ms-original-file: 2026-11-01/Update_NamespaceDiscoveredAsset.json
     */
    /**
     * Sample code: Update_NamespaceDiscoveredAsset.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        updateNamespaceDiscoveredAsset(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        NamespaceDiscoveredAsset resource = manager.namespaceDiscoveredAssets()
            .getWithResponse("myResourceGroup", "my-namespace-1", "my-discoveredasset-1",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(
                new NamespaceDiscoveredAssetUpdateProperties().withDocumentationUri("https://www.example.com/manual-2"))
            .apply();
    }
}
```

### NamespaceDiscoveredDevices_CreateOrReplace

```java
import com.azure.resourcemanager.deviceregistry.models.DeviceMessagingEndpoint;
import com.azure.resourcemanager.deviceregistry.models.DiscoveredMessagingEndpoints;
import com.azure.resourcemanager.deviceregistry.models.DiscoveredOutboundEndpoints;
import com.azure.resourcemanager.deviceregistry.models.ExtendedLocation;
import com.azure.resourcemanager.deviceregistry.models.NamespaceDiscoveredDeviceProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for NamespaceDiscoveredDevices CreateOrReplace.
 */
public final class NamespaceDiscoveredDevicesCreateOrReplaceSamples {
    /*
     * x-ms-original-file: 2026-11-01/CreateOrReplace_NamespaceDiscoveredDevice.json
     */
    /**
     * Sample code: CreateOrReplace_NamespaceDiscoveredDevice.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void createOrReplaceNamespaceDiscoveredDevice(
        com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.namespaceDiscoveredDevices()
            .define("my-discovereddevice-1")
            .withRegion("West Europe")
            .withExistingNamespace("myResourceGroup", "my-namespace-1")
            .withExtendedLocation(new ExtendedLocation().withType("CustomLocation")
                .withName(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/myResourceGroup/providers/microsoft.extendedlocation/customlocations/location1"))
            .withTags(mapOf("site", "building-1"))
            .withProperties(new NamespaceDiscoveredDeviceProperties()
                .withEndpoints(new DiscoveredMessagingEndpoints()
                    .withOutbound(new DiscoveredOutboundEndpoints().withAssigned(mapOf("eventGridEndpoint",
                        new DeviceMessagingEndpoint().withEndpointType("Microsoft.Devices")
                            .withAddress("https://myeventgridtopic.westeurope-1.eventgrid.azure.net/api/events")))))
                .withDiscoveryId("discoveryId1")
                .withVersion(1L))
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

### NamespaceDiscoveredDevices_Delete

```java
/**
 * Samples for NamespaceDiscoveredDevices Delete.
 */
public final class NamespaceDiscoveredDevicesDeleteSamples {
    /*
     * x-ms-original-file: 2026-11-01/Delete_NamespaceDiscoveredDevice.json
     */
    /**
     * Sample code: Delete_NamespaceDiscoveredDevice.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        deleteNamespaceDiscoveredDevice(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.namespaceDiscoveredDevices()
            .delete("myResourceGroup", "my-namespace-1", "my-discovereddevice-1", com.azure.core.util.Context.NONE);
    }
}
```

### NamespaceDiscoveredDevices_Get

```java
/**
 * Samples for NamespaceDiscoveredDevices Get.
 */
public final class NamespaceDiscoveredDevicesGetSamples {
    /*
     * x-ms-original-file: 2026-11-01/Get_NamespaceDiscoveredDevice.json
     */
    /**
     * Sample code: Get_NamespaceDiscoveredDevice.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        getNamespaceDiscoveredDevice(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.namespaceDiscoveredDevices()
            .getWithResponse("myResourceGroup", "my-namespace-1", "my-discovereddevice-1",
                com.azure.core.util.Context.NONE);
    }
}
```

### NamespaceDiscoveredDevices_ListByNamespace

```java
/**
 * Samples for NamespaceDiscoveredDevices ListByNamespace.
 */
public final class NamespaceDiscoveredDevicesListByNamespaceSamples {
    /*
     * x-ms-original-file: 2026-11-01/List_NamespaceDiscoveredDevices_ByNamespace.json
     */
    /**
     * Sample code: List_NamespaceDiscoveredDevices_ByNamespace.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void listNamespaceDiscoveredDevicesByNamespace(
        com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.namespaceDiscoveredDevices()
            .listByNamespace("myResourceGroup", "my-namespace-1", com.azure.core.util.Context.NONE);
    }
}
```

### NamespaceDiscoveredDevices_Update

```java
import com.azure.resourcemanager.deviceregistry.models.DeviceMessagingEndpoint;
import com.azure.resourcemanager.deviceregistry.models.DiscoveredMessagingEndpoints;
import com.azure.resourcemanager.deviceregistry.models.DiscoveredOutboundEndpoints;
import com.azure.resourcemanager.deviceregistry.models.NamespaceDiscoveredDevice;
import com.azure.resourcemanager.deviceregistry.models.NamespaceDiscoveredDeviceUpdateProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for NamespaceDiscoveredDevices Update.
 */
public final class NamespaceDiscoveredDevicesUpdateSamples {
    /*
     * x-ms-original-file: 2026-11-01/Update_NamespaceDiscoveredDevice.json
     */
    /**
     * Sample code: Update_NamespaceDiscoveredDevice.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        updateNamespaceDiscoveredDevice(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        NamespaceDiscoveredDevice resource = manager.namespaceDiscoveredDevices()
            .getWithResponse("myResourceGroup", "my-namespace-1", "my-discovereddevice-1",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(
                new NamespaceDiscoveredDeviceUpdateProperties().withEndpoints(new DiscoveredMessagingEndpoints()
                    .withOutbound(new DiscoveredOutboundEndpoints().withAssigned(mapOf("newEventGridEndpoint",
                        new DeviceMessagingEndpoint().withEndpointType("Microsoft.Devices")
                            .withAddress("https://myneweventgridtopic.westeurope-1.eventgrid.azure.net/api/events"))))))
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

### Namespaces_CreateOrReplace

```java
import com.azure.resourcemanager.deviceregistry.models.InboundCallerIdentity;
import com.azure.resourcemanager.deviceregistry.models.InboundCallerIdentityType;
import com.azure.resourcemanager.deviceregistry.models.ManagedServiceIdentity;
import com.azure.resourcemanager.deviceregistry.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.deviceregistry.models.Management;
import com.azure.resourcemanager.deviceregistry.models.ManagementEndpoint;
import com.azure.resourcemanager.deviceregistry.models.Messaging;
import com.azure.resourcemanager.deviceregistry.models.MessagingEndpoint;
import com.azure.resourcemanager.deviceregistry.models.NamespaceProperties;
import com.azure.resourcemanager.deviceregistry.models.NamespaceProvisioning;
import com.azure.resourcemanager.deviceregistry.models.ProvisioningEndpoint;
import com.azure.resourcemanager.deviceregistry.models.ProvisioningEndpointType;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Namespaces CreateOrReplace.
 */
public final class NamespacesCreateOrReplaceSamples {
    /*
     * x-ms-original-file: 2026-11-01/CreateOrReplace_Namespace_With_MessagingAndProvisioningEndpoints.json
     */
    /**
     * Sample code: Create or Replace a Namespace with linked Messaging and Provisioning Endpoints.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void createOrReplaceANamespaceWithLinkedMessagingAndProvisioningEndpoints(
        com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.namespaces()
            .define("mynamespace")
            .withRegion("northeurope")
            .withExistingResourceGroup("myResourceGroup")
            .withProperties(new NamespaceProperties()
                .withMessaging(new Messaging().withEndpoints(mapOf("myPrimaryIotHubEndpoint", new MessagingEndpoint()
                    .withEndpointType("Microsoft.Devices/IotHubs")
                    .withResourceId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.Devices/IotHubs/myIotHub1")
                    .withInboundCallerIdentity(
                        new InboundCallerIdentity().withType(InboundCallerIdentityType.SYSTEM_ASSIGNED)),
                    "mySecondaryIotHubEndpoint",
                    new MessagingEndpoint().withEndpointType("Microsoft.Devices/IotHubs")
                        .withResourceId(
                            "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.Devices/IotHubs/myIotHub2")
                        .withInboundCallerIdentity(new InboundCallerIdentity()
                            .withType(InboundCallerIdentityType.USER_ASSIGNED)
                            .withUserAssignedIdentity(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/myAdrCallerUami")))))
                .withProvisioning(new NamespaceProvisioning().withEndpoints(mapOf("myDpsEndpoint",
                    new ProvisioningEndpoint().withEndpointType(ProvisioningEndpointType.DPS)
                        .withResourceId(
                            "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.Devices/provisioningServices/myDps")
                        .withInboundCallerIdentity(
                            new InboundCallerIdentity().withType(InboundCallerIdentityType.SYSTEM_ASSIGNED))))))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED))
            .create();
    }

    /*
     * x-ms-original-file: 2026-11-01/CreateOrReplace_Namespace_With_ManagementEndpoints.json
     */
    /**
     * Sample code: Create or Replace a Namespace with Management Endpoints.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void createOrReplaceANamespaceWithManagementEndpoints(
        com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.namespaces()
            .define("adr-namespace-gbk0925-n01")
            .withRegion("North Europe")
            .withExistingResourceGroup("myResourceGroup")
            .withProperties(new NamespaceProperties().withManagement(new Management().withEndpoints(mapOf(
                "customLocation1",
                new ManagementEndpoint().withEndpointType("Microsoft.EventGrid/Namespaces")
                    .withAddress("eg-for-adr.eastus2-1.ts.eventgrid.azure.net")
                    .withScopeId("scope-id-for-management-endpoint-1")
                    .withResourceId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.EventGrid/Namespaces/eg-for-adr"),
                "customLocation2",
                new ManagementEndpoint().withEndpointType("Microsoft.EventGrid/Namespaces")
                    .withAddress("eg-for-adr1.eastus2-1.ts.eventgrid.azure.net")
                    .withScopeId("scope-id-for-management-endpoint-2")
                    .withResourceId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.EventGrid/Namespaces/eg-for-adr1")))))
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

### Namespaces_Delete

```java
/**
 * Samples for Namespaces Delete.
 */
public final class NamespacesDeleteSamples {
    /*
     * x-ms-original-file: 2026-11-01/Delete_Namespace.json
     */
    /**
     * Sample code: Delete a Namespace.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void deleteANamespace(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.namespaces().delete("myResourceGroup", "adr-namespace-gbk0925-n01", com.azure.core.util.Context.NONE);
    }
}
```

### Namespaces_GetByResourceGroup

```java
/**
 * Samples for Namespaces GetByResourceGroup.
 */
public final class NamespacesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-11-01/Get_Namespace_With_FailedLinkingEndpoint.json
     */
    /**
     * Sample code: Get a Namespace with a Failed Linking Endpoint.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void getANamespaceWithAFailedLinkingEndpoint(
        com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.namespaces()
            .getByResourceGroupWithResponse("myResourceGroup", "my-namespace", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-11-01/Get_Namespace.json
     */
    /**
     * Sample code: Get a Namespace.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void getANamespace(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.namespaces()
            .getByResourceGroupWithResponse("myResourceGroup", "mynamespace", com.azure.core.util.Context.NONE);
    }
}
```

### Namespaces_List

```java
/**
 * Samples for Namespaces List.
 */
public final class NamespacesListSamples {
    /*
     * x-ms-original-file: 2026-11-01/List_Namespace_BySubscription.json
     */
    /**
     * Sample code: List the Namespaces in a Subscription.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        listTheNamespacesInASubscription(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.namespaces().list(com.azure.core.util.Context.NONE);
    }
}
```

### Namespaces_ListByResourceGroup

```java
/**
 * Samples for Namespaces ListByResourceGroup.
 */
public final class NamespacesListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-11-01/List_Namespace_ByResourceGroup.json
     */
    /**
     * Sample code: List the Namespaces in a Resource Group.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        listTheNamespacesInAResourceGroup(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.namespaces().listByResourceGroup("myResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### Namespaces_Migrate

```java
import com.azure.resourcemanager.deviceregistry.models.NamespaceMigrateRequest;
import com.azure.resourcemanager.deviceregistry.models.Scope;
import java.util.Arrays;

/**
 * Samples for Namespaces Migrate.
 */
public final class NamespacesMigrateSamples {
    /*
     * x-ms-original-file: 2026-11-01/Migrate_Assets_Namespace.json
     */
    /**
     * Sample code: Namespace Migrate.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void namespaceMigrate(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.namespaces()
            .migrate("myResourceGroup", "my-namespace-1", new NamespaceMigrateRequest().withScope(Scope.RESOURCES)
                .withResourceIds(Arrays.asList(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.DeviceRegistry/assets/my-asset-1",
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.DeviceRegistry/assets/my-asset-2",
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.DeviceRegistry/assets/my-asset-3")),
                com.azure.core.util.Context.NONE);
    }
}
```

### Namespaces_Update

```java
import com.azure.resourcemanager.deviceregistry.models.InboundCallerIdentity;
import com.azure.resourcemanager.deviceregistry.models.InboundCallerIdentityType;
import com.azure.resourcemanager.deviceregistry.models.Management;
import com.azure.resourcemanager.deviceregistry.models.ManagementEndpoint;
import com.azure.resourcemanager.deviceregistry.models.Messaging;
import com.azure.resourcemanager.deviceregistry.models.MessagingEndpoint;
import com.azure.resourcemanager.deviceregistry.models.MessagingEndpointAvailability;
import com.azure.resourcemanager.deviceregistry.models.MessagingEndpointProvisioning;
import com.azure.resourcemanager.deviceregistry.models.Namespace;
import com.azure.resourcemanager.deviceregistry.models.NamespaceProvisioning;
import com.azure.resourcemanager.deviceregistry.models.NamespaceUpdateProperties;
import com.azure.resourcemanager.deviceregistry.models.ProvisioningEndpoint;
import com.azure.resourcemanager.deviceregistry.models.ProvisioningEndpointType;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Namespaces Update.
 */
public final class NamespacesUpdateSamples {
    /*
     * x-ms-original-file: 2026-11-01/Update_Namespace_ManagementEndpoints.json
     */
    /**
     * Sample code: Link a Namespace to a Management Endpoint.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        linkANamespaceToAManagementEndpoint(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        Namespace resource = manager.namespaces()
            .getByResourceGroupWithResponse("myResourceGroup", "adr-namespace-gbk0925-n01",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new NamespaceUpdateProperties().withManagement(new Management().withEndpoints(mapOf(
                "customLocation1",
                new ManagementEndpoint().withEndpointType("Microsoft.EventGrid/Namespaces")
                    .withAddress("eg-for-adr.eastus2-1.ts.eventgrid.azure.net")
                    .withScopeId("scope-id-for-management-endpoint-1")
                    .withResourceId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.EventGrid/Namespaces/eg-for-adr")))))
            .apply();
    }

    /*
     * x-ms-original-file: 2026-11-01/Update_Namespace_ProvisioningEndpoints.json
     */
    /**
     * Sample code: Link a Namespace to a Provisioning Endpoint.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        linkANamespaceToAProvisioningEndpoint(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        Namespace resource = manager.namespaces()
            .getByResourceGroupWithResponse("myResourceGroup", "mynamespace", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new NamespaceUpdateProperties().withProvisioning(new NamespaceProvisioning().withEndpoints(
                mapOf("myDpsEndpoint", new ProvisioningEndpoint().withEndpointType(ProvisioningEndpointType.DPS)
                    .withResourceId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.Devices/provisioningServices/myDps")
                    .withInboundCallerIdentity(
                        new InboundCallerIdentity().withType(InboundCallerIdentityType.SYSTEM_ASSIGNED))))))
            .apply();
    }

    /*
     * x-ms-original-file: 2026-11-01/Update_Namespace_MessagingEndpoints.json
     */
    /**
     * Sample code: Link a Namespace to a Messaging Endpoint.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        linkANamespaceToAMessagingEndpoint(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        Namespace resource = manager.namespaces()
            .getByResourceGroupWithResponse("myResourceGroup", "mynamespace", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new NamespaceUpdateProperties().withMessaging(new Messaging().withEndpoints(mapOf(
                "myPrimaryIotHubEndpoint",
                new MessagingEndpoint().withEndpointType("Microsoft.Devices/IotHubs")
                    .withResourceId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.Devices/IotHubs/myIotHub1")
                    .withInboundCallerIdentity(
                        new InboundCallerIdentity().withType(InboundCallerIdentityType.SYSTEM_ASSIGNED))
                    .withProvisioning(
                        new MessagingEndpointProvisioning().withAvailability(MessagingEndpointAvailability.AVAILABLE)
                            .withAllocationWeight(1)),
                "mySecondaryIotHubEndpoint",
                new MessagingEndpoint().withEndpointType("Microsoft.Devices/IotHubs")
                    .withResourceId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.Devices/IotHubs/myIotHub2")
                    .withInboundCallerIdentity(
                        new InboundCallerIdentity().withType(InboundCallerIdentityType.SYSTEM_ASSIGNED))
                    .withProvisioning(
                        new MessagingEndpointProvisioning().withAvailability(MessagingEndpointAvailability.AVAILABLE)
                            .withAllocationWeight(1))))))
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

### OperationStatus_Get

```java
/**
 * Samples for OperationStatus Get.
 */
public final class OperationStatusGetSamples {
    /*
     * x-ms-original-file: 2026-11-01/Get_OperationStatus.json
     */
    /**
     * Sample code: Get_OperationStatus.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void getOperationStatus(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.operationStatus()
            .getWithResponse("eastus", "00000000-0000-0000-0000-000000000001", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-11-01/List_Operations.json
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

### RegistryDevices_CreateOrReplace

```java
import com.azure.resourcemanager.deviceregistry.models.RegistryDeviceEnablementState;
import com.azure.resourcemanager.deviceregistry.models.RegistryDeviceProperties;

/**
 * Samples for RegistryDevices CreateOrReplace.
 */
public final class RegistryDevicesCreateOrReplaceSamples {
    /*
     * x-ms-original-file: 2026-11-01/CreateOrReplace_RegistryDevice.json
     */
    /**
     * Sample code: Create or Replace a Device.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void createOrReplaceADevice(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.registryDevices()
            .define("adr-smart-device3-f191f536-f652-4eb4-b9a0-1a9d43300cab")
            .withRegion("North Europe")
            .withExistingNamespace("myResourceGroup", "my-namespace-1")
            .withProperties(new RegistryDeviceProperties().withEnablementState(RegistryDeviceEnablementState.ENABLED)
                .withExternalDeviceId("adr-smart-device3-f191f536-f652-4eb4-b9a0-1a9d43300cab")
                .withManufacturer("Contoso")
                .withModel("SmartSensor")
                .withHardwareRevision("1.0")
                .withSoftwareRevision("5.15"))
            .create();
    }
}
```

### RegistryDevices_Delete

```java
/**
 * Samples for RegistryDevices Delete.
 */
public final class RegistryDevicesDeleteSamples {
    /*
     * x-ms-original-file: 2026-11-01/Delete_RegistryDevice.json
     */
    /**
     * Sample code: Delete a Device.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void deleteADevice(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.registryDevices()
            .delete("myResourceGroup", "my-namespace-1", "adr-smart-device3-f191f536-f652-4eb4-b9a0-1a9d43300cab",
                com.azure.core.util.Context.NONE);
    }
}
```

### RegistryDevices_Get

```java
/**
 * Samples for RegistryDevices Get.
 */
public final class RegistryDevicesGetSamples {
    /*
     * x-ms-original-file: 2026-11-01/Get_RegistryDevice.json
     */
    /**
     * Sample code: Get a Device.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void getADevice(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.registryDevices()
            .getWithResponse("myResourceGroup", "my-namespace-1",
                "adr-smart-device3-f191f536-f652-4eb4-b9a0-1a9d43300cab", com.azure.core.util.Context.NONE);
    }
}
```

### RegistryDevices_ListByNamespace

```java
/**
 * Samples for RegistryDevices ListByNamespace.
 */
public final class RegistryDevicesListByNamespaceSamples {
    /*
     * x-ms-original-file: 2026-11-01/List_RegistryDevices_ByNamespace.json
     */
    /**
     * Sample code: List Devices for a Namespace.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        listDevicesForANamespace(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.registryDevices()
            .listByNamespace("myResourceGroup", "my-namespace-1", com.azure.core.util.Context.NONE);
    }
}
```

### RegistryDevices_Update

```java
import com.azure.resourcemanager.deviceregistry.models.RegistryDevice;
import com.azure.resourcemanager.deviceregistry.models.RegistryDeviceEnablementState;
import com.azure.resourcemanager.deviceregistry.models.RegistryDeviceUpdateProperties;

/**
 * Samples for RegistryDevices Update.
 */
public final class RegistryDevicesUpdateSamples {
    /*
     * x-ms-original-file: 2026-11-01/Update_RegistryDevice.json
     */
    /**
     * Sample code: Update a Device.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void updateADevice(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        RegistryDevice resource = manager.registryDevices()
            .getWithResponse("myResourceGroup", "my-namespace-1",
                "adr-smart-device3-f191f536-f652-4eb4-b9a0-1a9d43300cab", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(
                new RegistryDeviceUpdateProperties().withEnablementState(RegistryDeviceEnablementState.ENABLED)
                    .withManufacturer("Contoso")
                    .withModel("SmartSensorV2")
                    .withHardwareRevision("1.0")
                    .withSoftwareRevision("6.1"))
            .apply();
    }
}
```

### SchemaRegistries_CreateOrReplace

```java
import com.azure.resourcemanager.deviceregistry.models.SchemaRegistryProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for SchemaRegistries CreateOrReplace.
 */
public final class SchemaRegistriesCreateOrReplaceSamples {
    /*
     * x-ms-original-file: 2026-11-01/CreateOrReplace_SchemaRegistry.json
     */
    /**
     * Sample code: CreateOrReplace_SchemaRegistry.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        createOrReplaceSchemaRegistry(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.schemaRegistries()
            .define("my-schema-registry")
            .withRegion("West Europe")
            .withExistingResourceGroup("myResourceGroup")
            .withTags(mapOf())
            .withProperties(new SchemaRegistryProperties().withNamespace("sr-namespace-001")
                .withDisplayName("Schema Registry namespace 001")
                .withDescription("This is a sample Schema Registry")
                .withStorageAccountContainerUrl("https://my-blob-storage.blob.core.windows.net/my-container"))
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
     * x-ms-original-file: 2026-11-01/Delete_SchemaRegistry.json
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
     * x-ms-original-file: 2026-11-01/Get_SchemaRegistry.json
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
     * x-ms-original-file: 2026-11-01/List_SchemaRegistries_BySubscription.json
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
     * x-ms-original-file: 2026-11-01/List_SchemaRegistries_ByResourceGroup.json
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
     * x-ms-original-file: 2026-11-01/Update_SchemaRegistry.json
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
     * x-ms-original-file: 2026-11-01/CreateOrReplace_SchemaVersion.json
     */
    /**
     * Sample code: CreateOrReplace_SchemaVersion.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void
        createOrReplaceSchemaVersion(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
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
     * x-ms-original-file: 2026-11-01/Delete_SchemaVersion.json
     */
    /**
     * Sample code: Delete_SchemaVersion.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void deleteSchemaVersion(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.schemaVersions()
            .delete("myResourceGroup", "my-schema-registry", "my-schema", "1", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-11-01/Get_SchemaVersion.json
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
     * x-ms-original-file: 2026-11-01/List_SchemaVersions_BySchema.json
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
     * x-ms-original-file: 2026-11-01/CreateOrReplace_Schema.json
     */
    /**
     * Sample code: CreateOrReplace_Schema.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void createOrReplaceSchema(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
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
     * x-ms-original-file: 2026-11-01/Delete_Schema.json
     */
    /**
     * Sample code: Delete_Schema.
     * 
     * @param manager Entry point to DeviceRegistryManager.
     */
    public static void deleteSchema(com.azure.resourcemanager.deviceregistry.DeviceRegistryManager manager) {
        manager.schemas()
            .delete("myResourceGroup", "my-schema-registry", "my-schema", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-11-01/Get_Schema.json
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
     * x-ms-original-file: 2026-11-01/List_Schemas_BySchemaRegistry.json
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

