# Code snippets and samples


## ExtensionTypes

- [ClusterGetVersion](#extensiontypes_clustergetversion)
- [ClusterListVersions](#extensiontypes_clusterlistversions)
- [Get](#extensiontypes_get)
- [GetVersion](#extensiontypes_getversion)
- [List](#extensiontypes_list)
- [ListVersions](#extensiontypes_listversions)
- [LocationGet](#extensiontypes_locationget)
- [LocationList](#extensiontypes_locationlist)
### ExtensionTypes_ClusterGetVersion

```java
/**
 * Samples for ExtensionTypes LocationGet.
 */
public final class ExtensionTypesLocationGetSamples {
    /*
     * x-ms-original-file:
     * specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/extensionTypes/preview/
     * 2024-11-01-preview/examples/GetExtensionTypeByLocation.json
     */
    /**
     * Sample code: Get Extension Type.
     * 
     * @param manager Entry point to ExtensionTypesManager.
     */
    public static void getExtensionType(
        com.azure.resourcemanager.kubernetesconfiguration.extensiontypes.ExtensionTypesManager manager) {
        manager.extensionTypes().locationGetWithResponse("westus2", "extensionType1", com.azure.core.util.Context.NONE);
    }
}
```

### ExtensionTypes_ClusterListVersions

```java
/**
 * Samples for ExtensionTypes Get.
 */
public final class ExtensionTypesGetSamples {
    /*
     * x-ms-original-file:
     * specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/extensionTypes/preview/
     * 2024-11-01-preview/examples/GetExtensionType.json
     */
    /**
     * Sample code: Get Extension Types.
     * 
     * @param manager Entry point to ExtensionTypesManager.
     */
    public static void getExtensionTypes(
        com.azure.resourcemanager.kubernetesconfiguration.extensiontypes.ExtensionTypesManager manager) {
        manager.extensionTypes()
            .getWithResponse("rg1", "Microsoft.Kubernetes", "connectedClusters", "my-cluster", "my-extension-type",
                com.azure.core.util.Context.NONE);
    }
}
```

### ExtensionTypes_Get

```java
/**
 * Samples for ExtensionTypes ClusterGetVersion.
 */
public final class ExtensionTypesClusterGetVersiSamples {
    /*
     * x-ms-original-file:
     * specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/extensionTypes/preview/
     * 2024-11-01-preview/examples/GetExtensionTypeVersion.json
     */
    /**
     * Sample code: List Extension Type Versions.
     * 
     * @param manager Entry point to ExtensionTypesManager.
     */
    public static void listExtensionTypeVersions(
        com.azure.resourcemanager.kubernetesconfiguration.extensiontypes.ExtensionTypesManager manager) {
        manager.extensionTypes()
            .clusterGetVersionWithResponse("rg1", "Microsoft.Kubernetes", "connectedClusters", "my-cluster",
                "my-extension-type", "v1.3.2", com.azure.core.util.Context.NONE);
    }
}
```

### ExtensionTypes_GetVersion

```java
/**
 * Samples for ExtensionTypes ListVersions.
 */
public final class ExtensionTypesListVersionsSamples {
    /*
     * x-ms-original-file:
     * specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/extensionTypes/preview/
     * 2024-11-01-preview/examples/ListExtensionTypeVersionsByLocation.json
     */
    /**
     * Sample code: List Extension Type Versions.
     * 
     * @param manager Entry point to ExtensionTypesManager.
     */
    public static void listExtensionTypeVersions(
        com.azure.resourcemanager.kubernetesconfiguration.extensiontypes.ExtensionTypesManager manager) {
        manager.extensionTypes()
            .listVersions("westus", "extensionType1", "stable", "connectedCluster", "2", true,
                com.azure.core.util.Context.NONE);
    }
}
```

### ExtensionTypes_List

```java
/**
 * Samples for ExtensionTypes LocationList.
 */
public final class ExtensionTypesLocationListSamples {
    /*
     * x-ms-original-file:
     * specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/extensionTypes/preview/
     * 2024-11-01-preview/examples/ListExtensionTypesByLocation.json
     */
    /**
     * Sample code: List Extension Types.
     * 
     * @param manager Entry point to ExtensionTypesManager.
     */
    public static void listExtensionTypes(
        com.azure.resourcemanager.kubernetesconfiguration.extensiontypes.ExtensionTypesManager manager) {
        manager.extensionTypes()
            .locationList("westus2", "myPublisherId", "myOfferId", "myPlanId", "stable", "connectedCluster",
                com.azure.core.util.Context.NONE);
    }
}
```

### ExtensionTypes_ListVersions

```java
/**
 * Samples for ExtensionTypes ClusterListVersions.
 */
public final class ExtensionTypesClusterListVersSamples {
    /*
     * x-ms-original-file:
     * specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/extensionTypes/preview/
     * 2024-11-01-preview/examples/ListExtensionTypeVersions.json
     */
    /**
     * Sample code: List Extension Type Versions.
     * 
     * @param manager Entry point to ExtensionTypesManager.
     */
    public static void listExtensionTypeVersions(
        com.azure.resourcemanager.kubernetesconfiguration.extensiontypes.ExtensionTypesManager manager) {
        manager.extensionTypes()
            .clusterListVersions("rg1", "Microsoft.Kubernetes", "connectedClusters", "my-cluster", "my-extension-type",
                "stable", "2", true, com.azure.core.util.Context.NONE);
    }
}
```

### ExtensionTypes_LocationGet

```java
/**
 * Samples for ExtensionTypes GetVersion.
 */
public final class ExtensionTypesGetVersionSamples {
    /*
     * x-ms-original-file:
     * specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/extensionTypes/preview/
     * 2024-11-01-preview/examples/GetExtensionTypeVersionByLocation.json
     */
    /**
     * Sample code: List Extension Type Versions.
     * 
     * @param manager Entry point to ExtensionTypesManager.
     */
    public static void listExtensionTypeVersions(
        com.azure.resourcemanager.kubernetesconfiguration.extensiontypes.ExtensionTypesManager manager) {
        manager.extensionTypes()
            .getVersionWithResponse("westus", "extensionType1", "1.20.0", com.azure.core.util.Context.NONE);
    }
}
```

### ExtensionTypes_LocationList

```java
/**
 * Samples for ExtensionTypes List.
 */
public final class ExtensionTypesListSamples {
    /*
     * x-ms-original-file:
     * specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/extensionTypes/preview/
     * 2024-11-01-preview/examples/ListExtensionTypes.json
     */
    /**
     * Sample code: Get Extension Types.
     * 
     * @param manager Entry point to ExtensionTypesManager.
     */
    public static void getExtensionTypes(
        com.azure.resourcemanager.kubernetesconfiguration.extensiontypes.ExtensionTypesManager manager) {
        manager.extensionTypes()
            .list("rg1", "Microsoft.Kubernetes", "connectedClusters", "my-cluster", "myPublisherId", "myOfferId",
                "myPlanId", "stable", com.azure.core.util.Context.NONE);
    }
}
```

