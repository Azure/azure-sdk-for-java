# Code snippets and samples


## ArcSettings

- [ConsentAndInstallDefaultExtensions](#arcsettings_consentandinstalldefaultextensions)
- [Create](#arcsettings_create)
- [CreateIdentity](#arcsettings_createidentity)
- [Delete](#arcsettings_delete)
- [GeneratePassword](#arcsettings_generatepassword)
- [Get](#arcsettings_get)
- [InitializeDisableProcess](#arcsettings_initializedisableprocess)
- [ListByCluster](#arcsettings_listbycluster)
- [Update](#arcsettings_update)

## Clusters

- [Create](#clusters_create)
- [CreateIdentity](#clusters_createidentity)
- [Delete](#clusters_delete)
- [ExtendSoftwareAssuranceBenefit](#clusters_extendsoftwareassurancebenefit)
- [GetByResourceGroup](#clusters_getbyresourcegroup)
- [List](#clusters_list)
- [ListByResourceGroup](#clusters_listbyresourcegroup)
- [Update](#clusters_update)
- [UploadCertificate](#clusters_uploadcertificate)

## Extensions

- [Create](#extensions_create)
- [Delete](#extensions_delete)
- [Get](#extensions_get)
- [ListByArcSetting](#extensions_listbyarcsetting)
- [Update](#extensions_update)
- [Upgrade](#extensions_upgrade)

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

- [ListByVirtualMachines](#guestagentsoperation_listbyvirtualmachines)

## HybridIdentityMetadata

- [Create](#hybrididentitymetadata_create)
- [Delete](#hybrididentitymetadata_delete)
- [Get](#hybrididentitymetadata_get)
- [ListByVirtualMachines](#hybrididentitymetadata_listbyvirtualmachines)

## MachineExtensions

- [CreateOrUpdate](#machineextensions_createorupdate)
- [Delete](#machineextensions_delete)
- [Get](#machineextensions_get)
- [ListByVirtualMachines](#machineextensions_listbyvirtualmachines)
- [Update](#machineextensions_update)

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

## Offers

- [Get](#offers_get)
- [ListByCluster](#offers_listbycluster)
- [ListByPublisher](#offers_listbypublisher)

## Operations

- [List](#operations_list)

## Publishers

- [Get](#publishers_get)
- [ListByCluster](#publishers_listbycluster)

## Skus

- [Get](#skus_get)
- [ListByOffer](#skus_listbyoffer)

## StorageContainersOperation

- [CreateOrUpdate](#storagecontainersoperation_createorupdate)
- [Delete](#storagecontainersoperation_delete)
- [GetByResourceGroup](#storagecontainersoperation_getbyresourcegroup)
- [List](#storagecontainersoperation_list)
- [ListByResourceGroup](#storagecontainersoperation_listbyresourcegroup)
- [Update](#storagecontainersoperation_update)

## UpdateRuns

- [Delete](#updateruns_delete)
- [Get](#updateruns_get)
- [List](#updateruns_list)
- [Put](#updateruns_put)

## UpdateSummariesOperation

- [Delete](#updatesummariesoperation_delete)
- [Get](#updatesummariesoperation_get)
- [List](#updatesummariesoperation_list)
- [Put](#updatesummariesoperation_put)

## Updates

- [Delete](#updates_delete)
- [Get](#updates_get)
- [List](#updates_list)
- [Post](#updates_post)
- [Put](#updates_put)

## VirtualHardDisksOperation

- [CreateOrUpdate](#virtualharddisksoperation_createorupdate)
- [Delete](#virtualharddisksoperation_delete)
- [GetByResourceGroup](#virtualharddisksoperation_getbyresourcegroup)
- [List](#virtualharddisksoperation_list)
- [ListByResourceGroup](#virtualharddisksoperation_listbyresourcegroup)
- [Update](#virtualharddisksoperation_update)

## VirtualMachinesOperation

- [CreateOrUpdate](#virtualmachinesoperation_createorupdate)
- [Delete](#virtualmachinesoperation_delete)
- [GetByResourceGroup](#virtualmachinesoperation_getbyresourcegroup)
- [List](#virtualmachinesoperation_list)
- [ListByResourceGroup](#virtualmachinesoperation_listbyresourcegroup)
- [Restart](#virtualmachinesoperation_restart)
- [Start](#virtualmachinesoperation_start)
- [Stop](#virtualmachinesoperation_stop)
- [Update](#virtualmachinesoperation_update)

## VirtualNetworksOperation

- [CreateOrUpdate](#virtualnetworksoperation_createorupdate)
- [Delete](#virtualnetworksoperation_delete)
- [GetByResourceGroup](#virtualnetworksoperation_getbyresourcegroup)
- [List](#virtualnetworksoperation_list)
- [ListByResourceGroup](#virtualnetworksoperation_listbyresourcegroup)
- [Update](#virtualnetworksoperation_update)
### ArcSettings_ConsentAndInstallDefaultExtensions

```java
/** Samples for ArcSettings ConsentAndInstallDefaultExtensions. */
public final class ArcSettingsConsentAndInstallDefaultExtensionsSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/ConsentAndInstallDefaultExtensions.json
     */
    /**
     * Sample code: Consent And Install Default Extensions.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void consentAndInstallDefaultExtensions(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .arcSettings()
            .consentAndInstallDefaultExtensionsWithResponse(
                "test-rg", "myCluster", "default", com.azure.core.util.Context.NONE);
    }
}
```

### ArcSettings_Create

```java
/** Samples for ArcSettings Create. */
public final class ArcSettingsCreateSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/PutArcSetting.json
     */
    /**
     * Sample code: Create ArcSetting.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void createArcSetting(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.arcSettings().define("default").withExistingCluster("test-rg", "myCluster").create();
    }
}
```

### ArcSettings_CreateIdentity

```java
/** Samples for ArcSettings CreateIdentity. */
public final class ArcSettingsCreateIdentitySamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/CreateArcIdentity.json
     */
    /**
     * Sample code: Create Arc Identity.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void createArcIdentity(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.arcSettings().createIdentity("test-rg", "myCluster", "default", com.azure.core.util.Context.NONE);
    }
}
```

### ArcSettings_Delete

```java
/** Samples for ArcSettings Delete. */
public final class ArcSettingsDeleteSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/DeleteArcSetting.json
     */
    /**
     * Sample code: Delete ArcSetting.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void deleteArcSetting(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.arcSettings().delete("test-rg", "myCluster", "default", com.azure.core.util.Context.NONE);
    }
}
```

### ArcSettings_GeneratePassword

```java
/** Samples for ArcSettings GeneratePassword. */
public final class ArcSettingsGeneratePasswordSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/GeneratePassword.json
     */
    /**
     * Sample code: Generate Password.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void generatePassword(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .arcSettings()
            .generatePasswordWithResponse("test-rg", "myCluster", "default", com.azure.core.util.Context.NONE);
    }
}
```

### ArcSettings_Get

```java
/** Samples for ArcSettings Get. */
public final class ArcSettingsGetSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/GetArcSetting.json
     */
    /**
     * Sample code: Get ArcSetting.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void getArcSetting(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.arcSettings().getWithResponse("test-rg", "myCluster", "default", com.azure.core.util.Context.NONE);
    }
}
```

### ArcSettings_InitializeDisableProcess

```java
/** Samples for ArcSettings InitializeDisableProcess. */
public final class ArcSettingsInitializeDisableProcessSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/InitializeDisableProcess.json
     */
    /**
     * Sample code: Trigger ARC Disable.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void triggerARCDisable(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .arcSettings()
            .initializeDisableProcess("test-rg", "myCluster", "default", com.azure.core.util.Context.NONE);
    }
}
```

### ArcSettings_ListByCluster

```java
/** Samples for ArcSettings ListByCluster. */
public final class ArcSettingsListByClusterSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/ListArcSettingsByCluster.json
     */
    /**
     * Sample code: List ArcSetting resources by HCI Cluster.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void listArcSettingResourcesByHCICluster(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.arcSettings().listByCluster("test-rg", "myCluster", com.azure.core.util.Context.NONE);
    }
}
```

### ArcSettings_Update

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.azurestackhci.models.ArcSetting;
import java.io.IOException;

/** Samples for ArcSettings Update. */
public final class ArcSettingsUpdateSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/PatchArcSetting.json
     */
    /**
     * Sample code: Patch ArcSetting.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void patchArcSetting(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager)
        throws IOException {
        ArcSetting resource =
            manager
                .arcSettings()
                .getWithResponse("test-rg", "myCluster", "default", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withConnectivityProperties(
                SerializerFactory
                    .createDefaultManagementSerializerAdapter()
                    .deserialize("{\"enabled\":true}", Object.class, SerializerEncoding.JSON))
            .apply();
    }
}
```

### Clusters_Create

```java
import com.azure.resourcemanager.azurestackhci.models.ManagedServiceIdentityType;

/** Samples for Clusters Create. */
public final class ClustersCreateSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/CreateCluster.json
     */
    /**
     * Sample code: Create cluster.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void createCluster(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .clusters()
            .define("myCluster")
            .withRegion("East US")
            .withExistingResourceGroup("test-rg")
            .withTypeIdentityType(ManagedServiceIdentityType.SYSTEM_ASSIGNED)
            .withCloudManagementEndpoint("https://98294836-31be-4668-aeae-698667faf99b.waconazure.com")
            .withAadClientId("24a6e53d-04e5-44d2-b7cc-1b732a847dfc")
            .withAadTenantId("7e589cc1-a8b6-4dff-91bd-5ec0fa18db94")
            .create();
    }
}
```

### Clusters_CreateIdentity

```java
/** Samples for Clusters CreateIdentity. */
public final class ClustersCreateIdentitySamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/CreateClusterIdentity.json
     */
    /**
     * Sample code: Create cluster Identity.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void createClusterIdentity(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.clusters().createIdentity("test-rg", "myCluster", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_Delete

```java
/** Samples for Clusters Delete. */
public final class ClustersDeleteSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/DeleteCluster.json
     */
    /**
     * Sample code: Delete cluster.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void deleteCluster(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.clusters().delete("test-rg", "myCluster", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_ExtendSoftwareAssuranceBenefit

```java
import com.azure.resourcemanager.azurestackhci.models.SoftwareAssuranceChangeRequest;
import com.azure.resourcemanager.azurestackhci.models.SoftwareAssuranceChangeRequestProperties;
import com.azure.resourcemanager.azurestackhci.models.SoftwareAssuranceIntent;

/** Samples for Clusters ExtendSoftwareAssuranceBenefit. */
public final class ClustersExtendSoftwareAssuranceBenefitSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/ExtendSoftwareAssuranceBenefit.json
     */
    /**
     * Sample code: Create cluster Identity.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void createClusterIdentity(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .clusters()
            .extendSoftwareAssuranceBenefit(
                "test-rg",
                "myCluster",
                new SoftwareAssuranceChangeRequest()
                    .withProperties(
                        new SoftwareAssuranceChangeRequestProperties()
                            .withSoftwareAssuranceIntent(SoftwareAssuranceIntent.ENABLE)),
                com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_GetByResourceGroup

```java
/** Samples for Clusters GetByResourceGroup. */
public final class ClustersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/GetCluster.json
     */
    /**
     * Sample code: Get cluster.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void getCluster(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.clusters().getByResourceGroupWithResponse("test-rg", "myCluster", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_List

```java
/** Samples for Clusters List. */
public final class ClustersListSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/ListClustersBySubscription.json
     */
    /**
     * Sample code: List clusters in a given subscription.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void listClustersInAGivenSubscription(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.clusters().list(com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_ListByResourceGroup

```java
/** Samples for Clusters ListByResourceGroup. */
public final class ClustersListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/ListClustersByResourceGroup.json
     */
    /**
     * Sample code: List clusters in a given resource group.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void listClustersInAGivenResourceGroup(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.clusters().listByResourceGroup("test-rg", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_Update

```java
import com.azure.resourcemanager.azurestackhci.models.Cluster;
import com.azure.resourcemanager.azurestackhci.models.ClusterDesiredProperties;
import com.azure.resourcemanager.azurestackhci.models.DiagnosticLevel;
import com.azure.resourcemanager.azurestackhci.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.azurestackhci.models.WindowsServerSubscription;
import java.util.HashMap;
import java.util.Map;

/** Samples for Clusters Update. */
public final class ClustersUpdateSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/UpdateCluster.json
     */
    /**
     * Sample code: Update cluster.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void updateCluster(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        Cluster resource =
            manager
                .clusters()
                .getByResourceGroupWithResponse("test-rg", "myCluster", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("tag1", "value1", "tag2", "value2"))
            .withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED)
            .withCloudManagementEndpoint("https://98294836-31be-4668-aeae-698667faf99b.waconazure.com")
            .withDesiredProperties(
                new ClusterDesiredProperties()
                    .withWindowsServerSubscription(WindowsServerSubscription.ENABLED)
                    .withDiagnosticLevel(DiagnosticLevel.BASIC))
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

### Clusters_UploadCertificate

```java
import com.azure.resourcemanager.azurestackhci.models.RawCertificateData;
import com.azure.resourcemanager.azurestackhci.models.UploadCertificateRequest;
import java.util.Arrays;

/** Samples for Clusters UploadCertificate. */
public final class ClustersUploadCertificateSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/UploadCertificate.json
     */
    /**
     * Sample code: Upload certificate.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void uploadCertificate(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .clusters()
            .uploadCertificate(
                "test-rg",
                "myCluster",
                new UploadCertificateRequest()
                    .withProperties(
                        new RawCertificateData().withCertificates(Arrays.asList("base64cert", "base64cert"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### Extensions_Create

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import java.io.IOException;

/** Samples for Extensions Create. */
public final class ExtensionsCreateSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/PutExtension.json
     */
    /**
     * Sample code: Create Arc Extension.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void createArcExtension(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager)
        throws IOException {
        manager
            .extensions()
            .define("MicrosoftMonitoringAgent")
            .withExistingArcSetting("test-rg", "myCluster", "default")
            .withPublisher("Microsoft.Compute")
            .withTypePropertiesType("MicrosoftMonitoringAgent")
            .withTypeHandlerVersion("1.10")
            .withSettings(
                SerializerFactory
                    .createDefaultManagementSerializerAdapter()
                    .deserialize("{\"workspaceId\":\"xx\"}", Object.class, SerializerEncoding.JSON))
            .withProtectedSettings(
                SerializerFactory
                    .createDefaultManagementSerializerAdapter()
                    .deserialize("{\"workspaceKey\":\"xx\"}", Object.class, SerializerEncoding.JSON))
            .withEnableAutomaticUpgrade(false)
            .create();
    }
}
```

### Extensions_Delete

```java
/** Samples for Extensions Delete. */
public final class ExtensionsDeleteSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/DeleteExtension.json
     */
    /**
     * Sample code: Delete Arc Extension.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void deleteArcExtension(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .extensions()
            .delete("test-rg", "myCluster", "default", "MicrosoftMonitoringAgent", com.azure.core.util.Context.NONE);
    }
}
```

### Extensions_Get

```java
/** Samples for Extensions Get. */
public final class ExtensionsGetSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/GetExtension.json
     */
    /**
     * Sample code: Get ArcSettings Extension.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void getArcSettingsExtension(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .extensions()
            .getWithResponse(
                "test-rg", "myCluster", "default", "MicrosoftMonitoringAgent", com.azure.core.util.Context.NONE);
    }
}
```

### Extensions_ListByArcSetting

```java
/** Samples for Extensions ListByArcSetting. */
public final class ExtensionsListByArcSettingSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/ListExtensionsByArcSetting.json
     */
    /**
     * Sample code: List Extensions under ArcSetting resource.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void listExtensionsUnderArcSettingResource(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.extensions().listByArcSetting("test-rg", "myCluster", "default", com.azure.core.util.Context.NONE);
    }
}
```

### Extensions_Update

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.azurestackhci.models.Extension;
import java.io.IOException;

/** Samples for Extensions Update. */
public final class ExtensionsUpdateSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/PatchExtension.json
     */
    /**
     * Sample code: Update Arc Extension.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void updateArcExtension(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager)
        throws IOException {
        Extension resource =
            manager
                .extensions()
                .getWithResponse(
                    "test-rg", "myCluster", "default", "MicrosoftMonitoringAgent", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withPublisher("Microsoft.Compute")
            .withTypePropertiesType("MicrosoftMonitoringAgent")
            .withTypeHandlerVersion("1.10")
            .withSettings(
                SerializerFactory
                    .createDefaultManagementSerializerAdapter()
                    .deserialize("{\"workspaceId\":\"xx\"}", Object.class, SerializerEncoding.JSON))
            .apply();
    }
}
```

### Extensions_Upgrade

```java
import com.azure.resourcemanager.azurestackhci.models.ExtensionUpgradeParameters;

/** Samples for Extensions Upgrade. */
public final class ExtensionsUpgradeSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/Extensions_Upgrade.json
     */
    /**
     * Sample code: Upgrade Machine Extensions.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void upgradeMachineExtensions(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .extensions()
            .upgrade(
                "test-rg",
                "myCluster",
                "default",
                "MicrosoftMonitoringAgent",
                new ExtensionUpgradeParameters().withTargetVersion("1.0.18062.0"),
                com.azure.core.util.Context.NONE);
    }
}
```

### GalleryImagesOperation_CreateOrUpdate

```java
import com.azure.resourcemanager.azurestackhci.models.ExtendedLocation;
import com.azure.resourcemanager.azurestackhci.models.ExtendedLocationTypes;

/** Samples for GalleryImagesOperation CreateOrUpdate. */
public final class GalleryImagesOperationCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/PutGalleryImage.json
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
            .withContainerName("Default_Container")
            .withImagePath("C:\\test.vhdx")
            .create();
    }
}
```

### GalleryImagesOperation_Delete

```java
/** Samples for GalleryImagesOperation Delete. */
public final class GalleryImagesOperationDeleteSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/DeleteGalleryImage.json
     */
    /**
     * Sample code: DeleteGalleryImage.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void deleteGalleryImage(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .galleryImagesOperations()
            .deleteByResourceGroupWithResponse("test-rg", "test-gallery-image", com.azure.core.util.Context.NONE);
    }
}
```

### GalleryImagesOperation_GetByResourceGroup

```java
/** Samples for GalleryImagesOperation GetByResourceGroup. */
public final class GalleryImagesOperationGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/GetGalleryImage.json
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
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/ListGalleryImageBySubscription.json
     */
    /**
     * Sample code: ListGalleryImageByResourceGroup.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void listGalleryImageByResourceGroup(
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
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/ListGalleryImageByResourceGroup.json
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
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/UpdateGalleryImage.json
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
import com.azure.resourcemanager.azurestackhci.models.GuestCredential;
import com.azure.resourcemanager.azurestackhci.models.ProvisioningAction;

/** Samples for GuestAgent Create. */
public final class GuestAgentCreateSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/CreateGuestAgent.json
     */
    /**
     * Sample code: CreateGuestAgent.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void createGuestAgent(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .guestAgents()
            .define("default")
            .withExistingVirtualMachine("testrg", "ContosoVm")
            .withCredentials(new GuestCredential().withUsername("tempuser").withPassword("fakeTokenPlaceholder"))
            .withProvisioningAction(ProvisioningAction.INSTALL)
            .create();
    }
}
```

### GuestAgent_Delete

```java
/** Samples for GuestAgent Delete. */
public final class GuestAgentDeleteSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/DeleteGuestAgent.json
     */
    /**
     * Sample code: DeleteGuestAgent.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void deleteGuestAgent(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.guestAgents().delete("testrg", "ContosoVm", "default", com.azure.core.util.Context.NONE);
    }
}
```

### GuestAgent_Get

```java
/** Samples for GuestAgent Get. */
public final class GuestAgentGetSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/GetGuestAgent.json
     */
    /**
     * Sample code: GetGuestAgent.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void getGuestAgent(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.guestAgents().getWithResponse("testrg", "ContosoVm", "default", com.azure.core.util.Context.NONE);
    }
}
```

### GuestAgentsOperation_ListByVirtualMachines

```java
/** Samples for GuestAgentsOperation ListByVirtualMachines. */
public final class GuestAgentsOperationListByVirtualMachinesSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/GuestAgent_ListByVirtualMachines.json
     */
    /**
     * Sample code: GuestAgentListByVirtualMachines.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void guestAgentListByVirtualMachines(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.guestAgentsOperations().listByVirtualMachines("testrg", "ContosoVm", com.azure.core.util.Context.NONE);
    }
}
```

### HybridIdentityMetadata_Create

```java
/** Samples for HybridIdentityMetadata Create. */
public final class HybridIdentityMetadataCreateSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/CreateHybridIdentityMetadata.json
     */
    /**
     * Sample code: CreateHybridIdentityMetadata.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void createHybridIdentityMetadata(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .hybridIdentityMetadatas()
            .define("default")
            .withExistingVirtualMachine("testrg", "ContosoVm")
            .withPublicKey("8ec7d60c-9700-40b1-8e6e-e5b2f6f477f2")
            .create();
    }
}
```

### HybridIdentityMetadata_Delete

```java
/** Samples for HybridIdentityMetadata Delete. */
public final class HybridIdentityMetadataDeleteSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/DeleteHybridIdentityMetadata.json
     */
    /**
     * Sample code: DeleteHybridIdentityMetadata.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void deleteHybridIdentityMetadata(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .hybridIdentityMetadatas()
            .deleteWithResponse("testrg", "ContosoVm", "default", com.azure.core.util.Context.NONE);
    }
}
```

### HybridIdentityMetadata_Get

```java
/** Samples for HybridIdentityMetadata Get. */
public final class HybridIdentityMetadataGetSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/GetHybridIdentityMetadata.json
     */
    /**
     * Sample code: GetHybridIdentityMetadata.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void getHybridIdentityMetadata(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .hybridIdentityMetadatas()
            .getWithResponse("testrg", "ContosoVm", "default", com.azure.core.util.Context.NONE);
    }
}
```

### HybridIdentityMetadata_ListByVirtualMachines

```java
/** Samples for HybridIdentityMetadata ListByVirtualMachines. */
public final class HybridIdentityMetadataListByVirtualMachinesSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/HybridIdentityMetadata_ListByVirtualMachines.json
     */
    /**
     * Sample code: HybridIdentityMetadataListByVirtualMachines.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void hybridIdentityMetadataListByVirtualMachines(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .hybridIdentityMetadatas()
            .listByVirtualMachines("testrg", "ContosoVm", com.azure.core.util.Context.NONE);
    }
}
```

### MachineExtensions_CreateOrUpdate

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import java.io.IOException;

/** Samples for MachineExtensions CreateOrUpdate. */
public final class MachineExtensionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/PutMachineExtension.json
     */
    /**
     * Sample code: Create or Update a Machine Extension (PUT).
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void createOrUpdateAMachineExtensionPUT(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) throws IOException {
        manager
            .machineExtensions()
            .define("CustomScriptExtension")
            .withRegion("eastus2euap")
            .withExistingVirtualMachine("myResourceGroup", "myMachine")
            .withPublisher("Microsoft.Compute")
            .withTypePropertiesType("CustomScriptExtension")
            .withTypeHandlerVersion("1.10")
            .withSettings(
                SerializerFactory
                    .createDefaultManagementSerializerAdapter()
                    .deserialize(
                        "{\"commandToExecute\":\"powershell.exe -c \\\"Get-Process | Where-Object { $_.CPU -gt 10000"
                            + " }\\\"\"}",
                        Object.class,
                        SerializerEncoding.JSON))
            .create();
    }
}
```

### MachineExtensions_Delete

```java
/** Samples for MachineExtensions Delete. */
public final class MachineExtensionsDeleteSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/DeleteMachineExtension.json
     */
    /**
     * Sample code: Delete a Machine Extension.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void deleteAMachineExtension(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.machineExtensions().delete("myResourceGroup", "myMachine", "MMA", com.azure.core.util.Context.NONE);
    }
}
```

### MachineExtensions_Get

```java
/** Samples for MachineExtensions Get. */
public final class MachineExtensionsGetSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/GetMachineExtension.json
     */
    /**
     * Sample code: Get Machine Extension.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void getMachineExtension(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .machineExtensions()
            .getWithResponse("myResourceGroup", "myMachine", "CustomScriptExtension", com.azure.core.util.Context.NONE);
    }
}
```

### MachineExtensions_ListByVirtualMachines

```java
/** Samples for MachineExtensions ListByVirtualMachines. */
public final class MachineExtensionsListByVirtualMachinesSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/ListMachineExtension.json
     */
    /**
     * Sample code: Get all Machine Extensions.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void getAllMachineExtensions(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .machineExtensions()
            .listByVirtualMachines("myResourceGroup", "myMachine", null, com.azure.core.util.Context.NONE);
    }
}
```

### MachineExtensions_Update

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.azurestackhci.models.MachineExtension;
import java.io.IOException;

/** Samples for MachineExtensions Update. */
public final class MachineExtensionsUpdateSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/UpdateMachineExtension.json
     */
    /**
     * Sample code: Create or Update a Machine Extension (PATCH).
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void createOrUpdateAMachineExtensionPATCH(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) throws IOException {
        MachineExtension resource =
            manager
                .machineExtensions()
                .getWithResponse(
                    "myResourceGroup", "myMachine", "CustomScriptExtension", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withPublisher("Microsoft.Compute")
            .withType("CustomScriptExtension")
            .withTypeHandlerVersion("1.10")
            .withSettings(
                SerializerFactory
                    .createDefaultManagementSerializerAdapter()
                    .deserialize(
                        "{\"commandToExecute\":\"powershell.exe -c \\\"Get-Process | Where-Object { $_.CPU -lt 100"
                            + " }\\\"\"}",
                        Object.class,
                        SerializerEncoding.JSON))
            .apply();
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
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/PutMarketplaceGalleryImage.json
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
            .withContainerName("Default_Container")
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
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/DeleteMarketplaceGalleryImage.json
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
            .deleteByResourceGroupWithResponse(
                "test-rg", "test-marketplace-gallery-image", com.azure.core.util.Context.NONE);
    }
}
```

### MarketplaceGalleryImagesOperation_GetByResourceGroup

```java
/** Samples for MarketplaceGalleryImagesOperation GetByResourceGroup. */
public final class MarketplaceGalleryImagesOperationGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/GetMarketplaceGalleryImage.json
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
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/ListMarketplaceGalleryImageBySubscription.json
     */
    /**
     * Sample code: ListMarketplaceGalleryImageByResourceGroup.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void listMarketplaceGalleryImageByResourceGroup(
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
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/ListMarketplaceGalleryImageByResourceGroup.json
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
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/UpdateMarketplaceGalleryImage.json
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
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/PutNetworkInterface.json
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
                                    .withSubnet(new IpConfigurationPropertiesSubnet().withId("test-vnet")))))
            .create();
    }
}
```

### NetworkInterfacesOperation_Delete

```java
/** Samples for NetworkInterfacesOperation Delete. */
public final class NetworkInterfacesOperationDeleteSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/DeleteNetworkInterface.json
     */
    /**
     * Sample code: DeleteNetworkInterface.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void deleteNetworkInterface(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .networkInterfacesOperations()
            .deleteByResourceGroupWithResponse("test-rg", "test-nic", com.azure.core.util.Context.NONE);
    }
}
```

### NetworkInterfacesOperation_GetByResourceGroup

```java
/** Samples for NetworkInterfacesOperation GetByResourceGroup. */
public final class NetworkInterfacesOperationGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/GetNetworkInterface.json
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
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/ListNetworkInterfaceBySubscription.json
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
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/ListNetworkInterfaceByResourceGroup.json
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
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/UpdateNetworkInterface.json
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

### Offers_Get

```java
/** Samples for Offers Get. */
public final class OffersGetSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/GetOffer.json
     */
    /**
     * Sample code: Get Offer.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void getOffer(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .offers()
            .getWithResponse("test-rg", "myCluster", "publisher1", "offer1", null, com.azure.core.util.Context.NONE);
    }
}
```

### Offers_ListByCluster

```java
/** Samples for Offers ListByCluster. */
public final class OffersListByClusterSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/ListOffersByCluster.json
     */
    /**
     * Sample code: List Offer resources by HCI Cluster.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void listOfferResourcesByHCICluster(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.offers().listByCluster("test-rg", "myCluster", null, com.azure.core.util.Context.NONE);
    }
}
```

### Offers_ListByPublisher

```java
/** Samples for Offers ListByPublisher. */
public final class OffersListByPublisherSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/ListOffersByPublisher.json
     */
    /**
     * Sample code: List Offer resources by publisher for the HCI Cluster.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void listOfferResourcesByPublisherForTheHCICluster(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.offers().listByPublisher("test-rg", "myCluster", "publisher1", null, com.azure.core.util.Context.NONE);
    }
}
```

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/ListOperations.json
     */
    /**
     * Sample code: Create cluster.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void createCluster(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.operations().listWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

### Publishers_Get

```java
/** Samples for Publishers Get. */
public final class PublishersGetSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/GetPublisher.json
     */
    /**
     * Sample code: Get Publisher.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void getPublisher(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.publishers().getWithResponse("test-rg", "myCluster", "publisher1", com.azure.core.util.Context.NONE);
    }
}
```

### Publishers_ListByCluster

```java
/** Samples for Publishers ListByCluster. */
public final class PublishersListByClusterSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/ListPublishersByCluster.json
     */
    /**
     * Sample code: List Publisher resources by HCI Cluster.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void listPublisherResourcesByHCICluster(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.publishers().listByCluster("test-rg", "myCluster", com.azure.core.util.Context.NONE);
    }
}
```

### Skus_Get

```java
/** Samples for Skus Get. */
public final class SkusGetSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/GetSku.json
     */
    /**
     * Sample code: Get Sku.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void getSku(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .skus()
            .getWithResponse(
                "test-rg", "myCluster", "publisher1", "offer1", "sku1", null, com.azure.core.util.Context.NONE);
    }
}
```

### Skus_ListByOffer

```java
/** Samples for Skus ListByOffer. */
public final class SkusListByOfferSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/ListSkusByOffer.json
     */
    /**
     * Sample code: List SKU resources by offer for the HCI Cluster.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void listSKUResourcesByOfferForTheHCICluster(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .skus()
            .listByOffer("test-rg", "myCluster", "publisher1", "offer1", null, com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/PutStorageContainer.json
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
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/DeleteStorageContainer.json
     */
    /**
     * Sample code: DeleteStorageContainer.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void deleteStorageContainer(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .storageContainersOperations()
            .deleteByResourceGroupWithResponse("test-rg", "Default_Container", com.azure.core.util.Context.NONE);
    }
}
```

### StorageContainersOperation_GetByResourceGroup

```java
/** Samples for StorageContainersOperation GetByResourceGroup. */
public final class StorageContainersOperationGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/GetStorageContainer.json
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
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/ListStorageContainerBySubscription.json
     */
    /**
     * Sample code: ListStorageContainerByResourceGroup.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void listStorageContainerByResourceGroup(
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
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/ListStorageContainerByResourceGroup.json
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
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/UpdateStorageContainer.json
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

### UpdateRuns_Delete

```java
/** Samples for UpdateRuns Delete. */
public final class UpdateRunsDeleteSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/DeleteUpdateRuns.json
     */
    /**
     * Sample code: Delete an Update.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void deleteAnUpdate(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .updateRuns()
            .delete(
                "testrg",
                "testcluster",
                "Microsoft4.2203.2.32",
                "23b779ba-0d52-4a80-8571-45ca74664ec3",
                com.azure.core.util.Context.NONE);
    }
}
```

### UpdateRuns_Get

```java
/** Samples for UpdateRuns Get. */
public final class UpdateRunsGetSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/GetUpdateRuns.json
     */
    /**
     * Sample code: Get Update runs under cluster resource.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void getUpdateRunsUnderClusterResource(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .updateRuns()
            .getWithResponse(
                "testrg",
                "testcluster",
                "Microsoft4.2203.2.32",
                "23b779ba-0d52-4a80-8571-45ca74664ec3",
                com.azure.core.util.Context.NONE);
    }
}
```

### UpdateRuns_List

```java
/** Samples for UpdateRuns List. */
public final class UpdateRunsListSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/ListUpdateRuns.json
     */
    /**
     * Sample code: List Update runs under cluster resource.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void listUpdateRunsUnderClusterResource(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.updateRuns().list("testrg", "testcluster", "Microsoft4.2203.2.32", com.azure.core.util.Context.NONE);
    }
}
```

### UpdateRuns_Put

```java
import com.azure.resourcemanager.azurestackhci.fluent.models.Step;
import com.azure.resourcemanager.azurestackhci.models.UpdateRun;
import java.time.OffsetDateTime;
import java.util.Arrays;

/** Samples for UpdateRuns Put. */
public final class UpdateRunsPutSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/PutUpdateRuns.json
     */
    /**
     * Sample code: Get Update runs under cluster resource.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void getUpdateRunsUnderClusterResource(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        UpdateRun resource =
            manager
                .updateRuns()
                .getWithResponse(
                    "testrg",
                    "testcluster",
                    "Microsoft4.2203.2.32",
                    "23b779ba-0d52-4a80-8571-45ca74664ec3",
                    com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withNamePropertiesName("Unnamed step")
            .withDescription("Update Azure Stack.")
            .withErrorMessage("")
            .withStatus("Success")
            .withStartTimeUtc(OffsetDateTime.parse("2022-04-06T01:36:33.3876751+00:00"))
            .withEndTimeUtc(OffsetDateTime.parse("2022-04-06T13:58:42.969006+00:00"))
            .withLastUpdatedTimeUtc(OffsetDateTime.parse("2022-04-06T13:58:42.969006+00:00"))
            .withSteps(
                Arrays
                    .asList(
                        new Step()
                            .withName("PreUpdate Cloud")
                            .withDescription("Prepare for SSU update")
                            .withErrorMessage("")
                            .withStatus("Success")
                            .withStartTimeUtc(OffsetDateTime.parse("2022-04-06T01:36:33.3876751+00:00"))
                            .withEndTimeUtc(OffsetDateTime.parse("2022-04-06T01:37:16.8728314+00:00"))
                            .withLastUpdatedTimeUtc(OffsetDateTime.parse("2022-04-06T01:37:16.8728314+00:00"))
                            .withSteps(Arrays.asList())))
            .apply();
    }
}
```

### UpdateSummariesOperation_Delete

```java
/** Samples for UpdateSummariesOperation Delete. */
public final class UpdateSummariesOperationDeleteSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/DeleteUpdateSummaries.json
     */
    /**
     * Sample code: Delete an Update.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void deleteAnUpdate(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.updateSummariesOperations().delete("testrg", "testcluster", com.azure.core.util.Context.NONE);
    }
}
```

### UpdateSummariesOperation_Get

```java
/** Samples for UpdateSummariesOperation Get. */
public final class UpdateSummariesOperationGetSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/GetUpdateSummaries.json
     */
    /**
     * Sample code: Get Update summaries under cluster resource.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void getUpdateSummariesUnderClusterResource(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.updateSummariesOperations().getWithResponse("testrg", "testcluster", com.azure.core.util.Context.NONE);
    }
}
```

### UpdateSummariesOperation_List

```java
/** Samples for UpdateSummariesOperation List. */
public final class UpdateSummariesOperationListSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/ListUpdateSummaries.json
     */
    /**
     * Sample code: Get Update summaries under cluster resource.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void getUpdateSummariesUnderClusterResource(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.updateSummariesOperations().list("testrg", "testcluster", com.azure.core.util.Context.NONE);
    }
}
```

### UpdateSummariesOperation_Put

```java
import com.azure.resourcemanager.azurestackhci.fluent.models.UpdateSummariesInner;
import com.azure.resourcemanager.azurestackhci.models.UpdateSummariesPropertiesState;
import java.time.OffsetDateTime;

/** Samples for UpdateSummariesOperation Put. */
public final class UpdateSummariesOperationPutSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/PutUpdateSummaries.json
     */
    /**
     * Sample code: Put Update summaries under cluster resource.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void putUpdateSummariesUnderClusterResource(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .updateSummariesOperations()
            .putWithResponse(
                "testrg",
                "testcluster",
                new UpdateSummariesInner()
                    .withOemFamily("DellEMC")
                    .withHardwareModel("PowerEdge R730xd")
                    .withCurrentVersion("4.2203.2.32")
                    .withLastUpdated(OffsetDateTime.parse("2022-04-06T14:08:18.254Z"))
                    .withLastChecked(OffsetDateTime.parse("2022-04-07T18:04:07Z"))
                    .withState(UpdateSummariesPropertiesState.APPLIED_SUCCESSFULLY),
                com.azure.core.util.Context.NONE);
    }
}
```

### Updates_Delete

```java
/** Samples for Updates Delete. */
public final class UpdatesDeleteSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/DeleteUpdates.json
     */
    /**
     * Sample code: Delete an Update.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void deleteAnUpdate(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.updates().delete("testrg", "testcluster", "Microsoft4.2203.2.32", com.azure.core.util.Context.NONE);
    }
}
```

### Updates_Get

```java
/** Samples for Updates Get. */
public final class UpdatesGetSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/GetUpdates.json
     */
    /**
     * Sample code: Get a specific update.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void getASpecificUpdate(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .updates()
            .getWithResponse("testrg", "testcluster", "Microsoft4.2203.2.32", com.azure.core.util.Context.NONE);
    }
}
```

### Updates_List

```java
/** Samples for Updates List. */
public final class UpdatesListSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/ListUpdates.json
     */
    /**
     * Sample code: List available updates.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void listAvailableUpdates(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.updates().list("testrg", "testcluster", com.azure.core.util.Context.NONE);
    }
}
```

### Updates_Post

```java
/** Samples for Updates Post. */
public final class UpdatesPostSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/PostUpdates.json
     */
    /**
     * Sample code: List available updates.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void listAvailableUpdates(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.updates().post("testrg", "testcluster", "Microsoft4.2203.2.32", com.azure.core.util.Context.NONE);
    }
}
```

### Updates_Put

```java
import com.azure.resourcemanager.azurestackhci.models.AvailabilityType;
import com.azure.resourcemanager.azurestackhci.models.HciUpdate;
import com.azure.resourcemanager.azurestackhci.models.State;
import com.azure.resourcemanager.azurestackhci.models.UpdatePrerequisite;
import java.time.OffsetDateTime;
import java.util.Arrays;

/** Samples for Updates Put. */
public final class UpdatesPutSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/PutUpdates.json
     */
    /**
     * Sample code: Put a specific update.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void putASpecificUpdate(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        HciUpdate resource =
            manager
                .updates()
                .getWithResponse("testrg", "testcluster", "Microsoft4.2203.2.32", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withInstalledDate(OffsetDateTime.parse("2022-04-06T14:08:18.254Z"))
            .withDescription("AzS Update 4.2203.2.32")
            .withState(State.INSTALLED)
            .withPrerequisites(
                Arrays
                    .asList(
                        new UpdatePrerequisite()
                            .withUpdateType("update type")
                            .withVersion("prerequisite version")
                            .withPackageName("update package name")))
            .withPackagePath("\\\\SU1FileServer\\SU1_Infrastructure_2\\Updates\\Packages\\Microsoft4.2203.2.32")
            .withPackageSizeInMb(18858.0F)
            .withDisplayName("AzS Update - 4.2203.2.32")
            .withVersion("4.2203.2.32")
            .withPublisher("Microsoft")
            .withReleaseLink("https://docs.microsoft.com/azure-stack/operator/release-notes?view=azs-2203")
            .withAvailabilityType(AvailabilityType.LOCAL)
            .withPackageType("Infrastructure")
            .withAdditionalProperties("additional properties")
            .withProgressPercentage(0.0F)
            .withNotifyMessage("Brief message with instructions for updates of AvailabilityType Notify")
            .apply();
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
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/PutVirtualHardDisk.json
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
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/DeleteVirtualHardDisk.json
     */
    /**
     * Sample code: DeleteVirtualHardDisk.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void deleteVirtualHardDisk(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .virtualHardDisksOperations()
            .deleteByResourceGroupWithResponse("test-rg", "test-vhd", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualHardDisksOperation_GetByResourceGroup

```java
/** Samples for VirtualHardDisksOperation GetByResourceGroup. */
public final class VirtualHardDisksOperationGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/GetVirtualHardDisk.json
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
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/ListVirtualHardDiskBySubscription.json
     */
    /**
     * Sample code: ListVirtualHardDiskByResourceGroup.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void listVirtualHardDiskByResourceGroup(
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
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/ListVirtualHardDiskByResourceGroup.json
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
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/UpdateVirtualHardDisk.json
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

### VirtualMachinesOperation_CreateOrUpdate

```java
import com.azure.resourcemanager.azurestackhci.models.ExtendedLocation;
import com.azure.resourcemanager.azurestackhci.models.ExtendedLocationTypes;
import com.azure.resourcemanager.azurestackhci.models.VirtualMachinePropertiesHardwareProfile;
import com.azure.resourcemanager.azurestackhci.models.VirtualMachinePropertiesNetworkProfile;
import com.azure.resourcemanager.azurestackhci.models.VirtualMachinePropertiesNetworkProfileNetworkInterfacesItem;
import com.azure.resourcemanager.azurestackhci.models.VirtualMachinePropertiesOsProfile;
import com.azure.resourcemanager.azurestackhci.models.VirtualMachinePropertiesSecurityProfile;
import com.azure.resourcemanager.azurestackhci.models.VirtualMachinePropertiesSecurityProfileUefiSettings;
import com.azure.resourcemanager.azurestackhci.models.VirtualMachinePropertiesStorageProfile;
import com.azure.resourcemanager.azurestackhci.models.VirtualMachinePropertiesStorageProfileImageReference;
import com.azure.resourcemanager.azurestackhci.models.VirtualMachinePropertiesStorageProfileOsDisk;
import com.azure.resourcemanager.azurestackhci.models.VmSizeEnum;
import java.util.Arrays;

/** Samples for VirtualMachinesOperation CreateOrUpdate. */
public final class VirtualMachinesOperationCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/PutVirtualMachineWithOsDisk.json
     */
    /**
     * Sample code: PutVirtualMachineWithOsDisk.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void putVirtualMachineWithOsDisk(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .virtualMachinesOperations()
            .define("test-vm")
            .withRegion("West US2")
            .withExistingResourceGroup("test-rg")
            .withExtendedLocation(
                new ExtendedLocation()
                    .withName(
                        "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.ExtendedLocation/customLocations/dogfood-location")
                    .withType(ExtendedLocationTypes.CUSTOM_LOCATION))
            .withHardwareProfile(new VirtualMachinePropertiesHardwareProfile().withVmSize(VmSizeEnum.DEFAULT))
            .withNetworkProfile(
                new VirtualMachinePropertiesNetworkProfile()
                    .withNetworkInterfaces(
                        Arrays
                            .asList(
                                new VirtualMachinePropertiesNetworkProfileNetworkInterfacesItem().withId("test-nic"))))
            .withSecurityProfile(
                new VirtualMachinePropertiesSecurityProfile()
                    .withEnableTpm(true)
                    .withUefiSettings(
                        new VirtualMachinePropertiesSecurityProfileUefiSettings().withSecureBootEnabled(true)))
            .withStorageProfile(
                new VirtualMachinePropertiesStorageProfile()
                    .withOsDisk(
                        new VirtualMachinePropertiesStorageProfileOsDisk()
                            .withId(
                                "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.AzureStackHCI/virtualHardDisks/test-vhd"))
                    .withVmConfigStoragePathId(
                        "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.AzureStackHCI/storageContainers/test-container"))
            .create();
    }

    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/PutVirtualMachineWithMarketplaceGalleryImage.json
     */
    /**
     * Sample code: PutVirtualMachineWithMarketplaceGalleryImage.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void putVirtualMachineWithMarketplaceGalleryImage(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .virtualMachinesOperations()
            .define("test-vm")
            .withRegion("West US2")
            .withExistingResourceGroup("test-rg")
            .withExtendedLocation(
                new ExtendedLocation()
                    .withName(
                        "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.ExtendedLocation/customLocations/dogfood-location")
                    .withType(ExtendedLocationTypes.CUSTOM_LOCATION))
            .withHardwareProfile(new VirtualMachinePropertiesHardwareProfile().withVmSize(VmSizeEnum.DEFAULT))
            .withNetworkProfile(
                new VirtualMachinePropertiesNetworkProfile()
                    .withNetworkInterfaces(
                        Arrays
                            .asList(
                                new VirtualMachinePropertiesNetworkProfileNetworkInterfacesItem().withId("test-nic"))))
            .withOsProfile(
                new VirtualMachinePropertiesOsProfile()
                    .withAdminPassword("fakeTokenPlaceholder")
                    .withAdminUsername("localadmin")
                    .withComputerName("luamaster"))
            .withSecurityProfile(
                new VirtualMachinePropertiesSecurityProfile()
                    .withEnableTpm(true)
                    .withUefiSettings(
                        new VirtualMachinePropertiesSecurityProfileUefiSettings().withSecureBootEnabled(true)))
            .withStorageProfile(
                new VirtualMachinePropertiesStorageProfile()
                    .withImageReference(
                        new VirtualMachinePropertiesStorageProfileImageReference()
                            .withId(
                                "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.AzureStackHCI/marketplaceGalleryImages/test-marketplace-gallery-image"))
                    .withVmConfigStoragePathId(
                        "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.AzureStackHCI/storageContainers/test-container"))
            .create();
    }

    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/PutVirtualMachineWithGalleryImage.json
     */
    /**
     * Sample code: PutVirtualMachineWithGalleryImage.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void putVirtualMachineWithGalleryImage(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .virtualMachinesOperations()
            .define("test-vm")
            .withRegion("West US2")
            .withExistingResourceGroup("test-rg")
            .withExtendedLocation(
                new ExtendedLocation()
                    .withName(
                        "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.ExtendedLocation/customLocations/dogfood-location")
                    .withType(ExtendedLocationTypes.CUSTOM_LOCATION))
            .withHardwareProfile(new VirtualMachinePropertiesHardwareProfile().withVmSize(VmSizeEnum.DEFAULT))
            .withNetworkProfile(
                new VirtualMachinePropertiesNetworkProfile()
                    .withNetworkInterfaces(
                        Arrays
                            .asList(
                                new VirtualMachinePropertiesNetworkProfileNetworkInterfacesItem().withId("test-nic"))))
            .withOsProfile(
                new VirtualMachinePropertiesOsProfile()
                    .withAdminPassword("fakeTokenPlaceholder")
                    .withAdminUsername("localadmin")
                    .withComputerName("luamaster"))
            .withSecurityProfile(
                new VirtualMachinePropertiesSecurityProfile()
                    .withEnableTpm(true)
                    .withUefiSettings(
                        new VirtualMachinePropertiesSecurityProfileUefiSettings().withSecureBootEnabled(true)))
            .withStorageProfile(
                new VirtualMachinePropertiesStorageProfile()
                    .withImageReference(
                        new VirtualMachinePropertiesStorageProfileImageReference()
                            .withId(
                                "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.AzureStackHCI/galleryImages/test-gallery-image"))
                    .withVmConfigStoragePathId(
                        "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.AzureStackHCI/storageContainers/test-container"))
            .create();
    }
}
```

### VirtualMachinesOperation_Delete

```java
/** Samples for VirtualMachinesOperation Delete. */
public final class VirtualMachinesOperationDeleteSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/DeleteVirtualMachine.json
     */
    /**
     * Sample code: DeleteVirtualMachine.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void deleteVirtualMachine(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .virtualMachinesOperations()
            .deleteByResourceGroupWithResponse("test-rg", "test-vm", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachinesOperation_GetByResourceGroup

```java
/** Samples for VirtualMachinesOperation GetByResourceGroup. */
public final class VirtualMachinesOperationGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/GetVirtualMachine.json
     */
    /**
     * Sample code: GetVirtualMachine.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void getVirtualMachine(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .virtualMachinesOperations()
            .getByResourceGroupWithResponse("test-rg", "test-vm", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachinesOperation_List

```java
/** Samples for VirtualMachinesOperation List. */
public final class VirtualMachinesOperationListSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/ListVirtualMachineBySubscription.json
     */
    /**
     * Sample code: ListVirtualMachineBySubscription.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void listVirtualMachineBySubscription(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.virtualMachinesOperations().list(com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachinesOperation_ListByResourceGroup

```java
/** Samples for VirtualMachinesOperation ListByResourceGroup. */
public final class VirtualMachinesOperationListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/ListVirtualMachineByResourceGroup.json
     */
    /**
     * Sample code: ListVirtualMachineByResourceGroup.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void listVirtualMachineByResourceGroup(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.virtualMachinesOperations().listByResourceGroup("test-rg", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachinesOperation_Restart

```java
/** Samples for VirtualMachinesOperation Restart. */
public final class VirtualMachinesOperationRestartSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/RestartVirtualMachine.json
     */
    /**
     * Sample code: RestartVirtualMachine.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void restartVirtualMachine(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.virtualMachinesOperations().restart("test-rg", "test-vm", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachinesOperation_Start

```java
/** Samples for VirtualMachinesOperation Start. */
public final class VirtualMachinesOperationStartSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/StartVirtualMachine.json
     */
    /**
     * Sample code: StartVirtualMachine.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void startVirtualMachine(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.virtualMachinesOperations().start("test-rg", "test-vm", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachinesOperation_Stop

```java
/** Samples for VirtualMachinesOperation Stop. */
public final class VirtualMachinesOperationStopSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/StopVirtualMachine.json
     */
    /**
     * Sample code: StopVirtualMachine.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void stopVirtualMachine(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.virtualMachinesOperations().stop("test-rg", "test-vm", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachinesOperation_Update

```java
import com.azure.resourcemanager.azurestackhci.models.VirtualMachines;
import java.util.HashMap;
import java.util.Map;

/** Samples for VirtualMachinesOperation Update. */
public final class VirtualMachinesOperationUpdateSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/UpdateVirtualMachine.json
     */
    /**
     * Sample code: UpdateVirtualMachine.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void updateVirtualMachine(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        VirtualMachines resource =
            manager
                .virtualMachinesOperations()
                .getByResourceGroupWithResponse("test-rg", "test-vm", com.azure.core.util.Context.NONE)
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

### VirtualNetworksOperation_CreateOrUpdate

```java
import com.azure.resourcemanager.azurestackhci.models.ExtendedLocation;
import com.azure.resourcemanager.azurestackhci.models.ExtendedLocationTypes;
import com.azure.resourcemanager.azurestackhci.models.NetworkTypeEnum;

/** Samples for VirtualNetworksOperation CreateOrUpdate. */
public final class VirtualNetworksOperationCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/PutVirtualNetwork.json
     */
    /**
     * Sample code: PutVirtualNetwork.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void putVirtualNetwork(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .virtualNetworksOperations()
            .define("test-vnet")
            .withRegion("West US2")
            .withExistingResourceGroup("test-rg")
            .withExtendedLocation(
                new ExtendedLocation()
                    .withName(
                        "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.ExtendedLocation/customLocations/dogfood-location")
                    .withType(ExtendedLocationTypes.CUSTOM_LOCATION))
            .withNetworkType(NetworkTypeEnum.TRANSPARENT)
            .create();
    }
}
```

### VirtualNetworksOperation_Delete

```java
/** Samples for VirtualNetworksOperation Delete. */
public final class VirtualNetworksOperationDeleteSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/DeleteVirtualNetwork.json
     */
    /**
     * Sample code: DeleteVirtualNetwork.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void deleteVirtualNetwork(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .virtualNetworksOperations()
            .deleteByResourceGroupWithResponse("test-rg", "test-vnet", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualNetworksOperation_GetByResourceGroup

```java
/** Samples for VirtualNetworksOperation GetByResourceGroup. */
public final class VirtualNetworksOperationGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/GetVirtualNetwork.json
     */
    /**
     * Sample code: GetVirtualNetwork.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void getVirtualNetwork(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .virtualNetworksOperations()
            .getByResourceGroupWithResponse("test-rg", "test-vnet", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualNetworksOperation_List

```java
/** Samples for VirtualNetworksOperation List. */
public final class VirtualNetworksOperationListSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/ListVirtualNetworkBySubscription.json
     */
    /**
     * Sample code: ListVirtualNetworkBySubscription.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void listVirtualNetworkBySubscription(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.virtualNetworksOperations().list(com.azure.core.util.Context.NONE);
    }
}
```

### VirtualNetworksOperation_ListByResourceGroup

```java
/** Samples for VirtualNetworksOperation ListByResourceGroup. */
public final class VirtualNetworksOperationListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/ListVirtualNetworkByResourceGroup.json
     */
    /**
     * Sample code: ListVirtualNetworkByResourceGroup.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void listVirtualNetworkByResourceGroup(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.virtualNetworksOperations().listByResourceGroup("test-rg", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualNetworksOperation_Update

```java
import com.azure.resourcemanager.azurestackhci.models.VirtualNetworks;
import java.util.HashMap;
import java.util.Map;

/** Samples for VirtualNetworksOperation Update. */
public final class VirtualNetworksOperationUpdateSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/preview/2022-12-15-preview/examples/UpdateVirtualNetwork.json
     */
    /**
     * Sample code: UpdateVirtualNetwork.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void updateVirtualNetwork(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        VirtualNetworks resource =
            manager
                .virtualNetworksOperations()
                .getByResourceGroupWithResponse("test-rg", "test-vnet", com.azure.core.util.Context.NONE)
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

