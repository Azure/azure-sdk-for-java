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

## DeploymentSettings

- [CreateOrUpdate](#deploymentsettings_createorupdate)
- [Delete](#deploymentsettings_delete)
- [Get](#deploymentsettings_get)
- [ListByClusters](#deploymentsettings_listbyclusters)

## EdgeDevices

- [CreateOrUpdate](#edgedevices_createorupdate)
- [Delete](#edgedevices_delete)
- [Get](#edgedevices_get)
- [List](#edgedevices_list)
- [Validate](#edgedevices_validate)

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

## Offers

- [Get](#offers_get)
- [ListByCluster](#offers_listbycluster)
- [ListByPublisher](#offers_listbypublisher)

## Operations

- [List](#operations_list)

## Publishers

- [Get](#publishers_get)
- [ListByCluster](#publishers_listbycluster)

## SecuritySettings

- [CreateOrUpdate](#securitysettings_createorupdate)
- [Delete](#securitysettings_delete)
- [Get](#securitysettings_get)
- [ListByClusters](#securitysettings_listbyclusters)

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

## VirtualMachineInstances

- [CreateOrUpdate](#virtualmachineinstances_createorupdate)
- [Delete](#virtualmachineinstances_delete)
- [Get](#virtualmachineinstances_get)
- [List](#virtualmachineinstances_list)
- [Restart](#virtualmachineinstances_restart)
- [Start](#virtualmachineinstances_start)
- [Stop](#virtualmachineinstances_stop)
- [Update](#virtualmachineinstances_update)
### ArcSettings_ConsentAndInstallDefaultExtensions

```java
/**
 * Samples for ArcSettings ConsentAndInstallDefaultExtensions.
 */
public final class ArcSettingsConsentAndInstallDefaultExtensionsSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * ConsentAndInstallDefaultExtensions.json
     */
    /**
     * Sample code: Consent And Install Default Extensions.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void
        consentAndInstallDefaultExtensions(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.arcSettings()
            .consentAndInstallDefaultExtensionsWithResponse("test-rg", "myCluster", "default",
                com.azure.core.util.Context.NONE);
    }
}
```

### ArcSettings_Create

```java
/**
 * Samples for ArcSettings Create.
 */
public final class ArcSettingsCreateSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/PutArcSetting.
     * json
     */
    /**
     * Sample code: Create ArcSetting.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void createArcSetting(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.arcSettings().define("default").withExistingCluster("test-rg", "myCluster").create();
    }
}
```

### ArcSettings_CreateIdentity

```java
/**
 * Samples for ArcSettings CreateIdentity.
 */
public final class ArcSettingsCreateIdentitySamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/CreateArcIdentity
     * .json
     */
    /**
     * Sample code: Create Arc Identity.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void createArcIdentity(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.arcSettings().createIdentity("test-rg", "myCluster", "default", com.azure.core.util.Context.NONE);
    }
}
```

### ArcSettings_Delete

```java
/**
 * Samples for ArcSettings Delete.
 */
public final class ArcSettingsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/DeleteArcSetting.
     * json
     */
    /**
     * Sample code: Delete ArcSetting.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void deleteArcSetting(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.arcSettings().delete("test-rg", "myCluster", "default", com.azure.core.util.Context.NONE);
    }
}
```

### ArcSettings_GeneratePassword

```java
/**
 * Samples for ArcSettings GeneratePassword.
 */
public final class ArcSettingsGeneratePasswordSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/GeneratePassword.
     * json
     */
    /**
     * Sample code: Generate Password.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void generatePassword(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.arcSettings()
            .generatePasswordWithResponse("test-rg", "myCluster", "default", com.azure.core.util.Context.NONE);
    }
}
```

### ArcSettings_Get

```java
/**
 * Samples for ArcSettings Get.
 */
public final class ArcSettingsGetSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/GetArcSetting.
     * json
     */
    /**
     * Sample code: Get ArcSetting.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void getArcSetting(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.arcSettings().getWithResponse("test-rg", "myCluster", "default", com.azure.core.util.Context.NONE);
    }
}
```

### ArcSettings_InitializeDisableProcess

```java
/**
 * Samples for ArcSettings InitializeDisableProcess.
 */
public final class ArcSettingsInitializeDisableProcessSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * InitializeDisableProcess.json
     */
    /**
     * Sample code: Trigger ARC Disable.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void triggerARCDisable(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.arcSettings()
            .initializeDisableProcess("test-rg", "myCluster", "default", com.azure.core.util.Context.NONE);
    }
}
```

### ArcSettings_ListByCluster

```java
/**
 * Samples for ArcSettings ListByCluster.
 */
public final class ArcSettingsListByClusterSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * ListArcSettingsByCluster.json
     */
    /**
     * Sample code: List ArcSetting resources by HCI Cluster.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void
        listArcSettingResourcesByHCICluster(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
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

/**
 * Samples for ArcSettings Update.
 */
public final class ArcSettingsUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/PatchArcSetting.
     * json
     */
    /**
     * Sample code: Patch ArcSetting.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void patchArcSetting(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager)
        throws IOException {
        ArcSetting resource = manager.arcSettings()
            .getWithResponse("test-rg", "myCluster", "default", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withConnectivityProperties(SerializerFactory.createDefaultManagementSerializerAdapter()
                .deserialize("{\"enabled\":true,\"serviceConfigurations\":[{\"port\":6516,\"serviceName\":\"WAC\"}]}",
                    Object.class, SerializerEncoding.JSON))
            .apply();
    }
}
```

### Clusters_Create

```java
import com.azure.resourcemanager.azurestackhci.models.ManagedServiceIdentityType;

/**
 * Samples for Clusters Create.
 */
public final class ClustersCreateSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/CreateCluster.
     * json
     */
    /**
     * Sample code: Create cluster.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void createCluster(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.clusters()
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
/**
 * Samples for Clusters CreateIdentity.
 */
public final class ClustersCreateIdentitySamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * CreateClusterIdentity.json
     */
    /**
     * Sample code: Create cluster Identity.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void createClusterIdentity(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.clusters().createIdentity("test-rg", "myCluster", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_Delete

```java
/**
 * Samples for Clusters Delete.
 */
public final class ClustersDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/DeleteCluster.
     * json
     */
    /**
     * Sample code: Delete cluster.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void deleteCluster(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.clusters().delete("test-rg", "myCluster", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_ExtendSoftwareAssuranceBenefit

```java
import com.azure.resourcemanager.azurestackhci.models.SoftwareAssuranceChangeRequest;
import com.azure.resourcemanager.azurestackhci.models.SoftwareAssuranceChangeRequestProperties;
import com.azure.resourcemanager.azurestackhci.models.SoftwareAssuranceIntent;

/**
 * Samples for Clusters ExtendSoftwareAssuranceBenefit.
 */
public final class ClustersExtendSoftwareAssuranceBenefitSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * ExtendSoftwareAssuranceBenefit.json
     */
    /**
     * Sample code: Create cluster Identity.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void createClusterIdentity(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.clusters()
            .extendSoftwareAssuranceBenefit("test-rg", "myCluster",
                new SoftwareAssuranceChangeRequest().withProperties(new SoftwareAssuranceChangeRequestProperties()
                    .withSoftwareAssuranceIntent(SoftwareAssuranceIntent.ENABLE)),
                com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_GetByResourceGroup

```java
/**
 * Samples for Clusters GetByResourceGroup.
 */
public final class ClustersGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/GetCluster.json
     */
    /**
     * Sample code: Get cluster.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void getCluster(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.clusters().getByResourceGroupWithResponse("test-rg", "myCluster", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_List

```java
/**
 * Samples for Clusters List.
 */
public final class ClustersListSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * ListClustersBySubscription.json
     */
    /**
     * Sample code: List clusters in a given subscription.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void
        listClustersInAGivenSubscription(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.clusters().list(com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_ListByResourceGroup

```java
/**
 * Samples for Clusters ListByResourceGroup.
 */
public final class ClustersListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * ListClustersByResourceGroup.json
     */
    /**
     * Sample code: List clusters in a given resource group.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void
        listClustersInAGivenResourceGroup(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
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

/**
 * Samples for Clusters Update.
 */
public final class ClustersUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/UpdateCluster.
     * json
     */
    /**
     * Sample code: Update cluster.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void updateCluster(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        Cluster resource = manager.clusters()
            .getByResourceGroupWithResponse("test-rg", "myCluster", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("tag1", "value1", "tag2", "value2"))
            .withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED)
            .withCloudManagementEndpoint("https://98294836-31be-4668-aeae-698667faf99b.waconazure.com")
            .withDesiredProperties(
                new ClusterDesiredProperties().withWindowsServerSubscription(WindowsServerSubscription.ENABLED)
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

/**
 * Samples for Clusters UploadCertificate.
 */
public final class ClustersUploadCertificateSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/UploadCertificate
     * .json
     */
    /**
     * Sample code: Upload certificate.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void uploadCertificate(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.clusters()
            .uploadCertificate("test-rg", "myCluster",
                new UploadCertificateRequest().withProperties(
                    new RawCertificateData().withCertificates(Arrays.asList("base64cert", "base64cert"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### DeploymentSettings_CreateOrUpdate

```java
import com.azure.resourcemanager.azurestackhci.models.AdapterPropertyOverrides;
import com.azure.resourcemanager.azurestackhci.models.DeploymentCluster;
import com.azure.resourcemanager.azurestackhci.models.DeploymentConfiguration;
import com.azure.resourcemanager.azurestackhci.models.DeploymentData;
import com.azure.resourcemanager.azurestackhci.models.DeploymentMode;
import com.azure.resourcemanager.azurestackhci.models.DeploymentSecuritySettings;
import com.azure.resourcemanager.azurestackhci.models.HostNetwork;
import com.azure.resourcemanager.azurestackhci.models.InfrastructureNetwork;
import com.azure.resourcemanager.azurestackhci.models.Intents;
import com.azure.resourcemanager.azurestackhci.models.IpPools;
import com.azure.resourcemanager.azurestackhci.models.Observability;
import com.azure.resourcemanager.azurestackhci.models.OptionalServices;
import com.azure.resourcemanager.azurestackhci.models.PhysicalNodes;
import com.azure.resourcemanager.azurestackhci.models.QosPolicyOverrides;
import com.azure.resourcemanager.azurestackhci.models.ScaleUnits;
import com.azure.resourcemanager.azurestackhci.models.Storage;
import com.azure.resourcemanager.azurestackhci.models.StorageNetworks;
import com.azure.resourcemanager.azurestackhci.models.VirtualSwitchConfigurationOverrides;
import java.util.Arrays;

/**
 * Samples for DeploymentSettings CreateOrUpdate.
 */
public final class DeploymentSettingsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * PutDeploymentSettings.json
     */
    /**
     * Sample code: Create Deployment Settings.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void createDeploymentSettings(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.deploymentSettings()
            .define("default")
            .withExistingCluster("test-rg", "myCluster")
            .withArcNodeResourceIds(Arrays.asList(
                "/subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/ArcInstance-rg/providers/Microsoft.HybridCompute/machines/Node-1",
                "/subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/ArcInstance-rg/providers/Microsoft.HybridCompute/machines/Node-2"))
            .withDeploymentMode(DeploymentMode.DEPLOY)
            .withDeploymentConfiguration(new DeploymentConfiguration().withVersion("string")
                .withScaleUnits(Arrays.asList(new ScaleUnits().withDeploymentData(new DeploymentData()
                    .withSecuritySettings(new DeploymentSecuritySettings().withHvciProtection(true)
                        .withDrtmProtection(true)
                        .withDriftControlEnforced(true)
                        .withCredentialGuardEnforced(false)
                        .withSmbSigningEnforced(true)
                        .withSmbClusterEncryption(false)
                        .withSideChannelMitigationEnforced(true)
                        .withBitlockerBootVolume(true)
                        .withBitlockerDataVolumes(true)
                        .withWdacEnforced(true))
                    .withObservability(new Observability().withStreamingDataClient(true)
                        .withEuLocation(false)
                        .withEpisodicDataUpload(true))
                    .withCluster(new DeploymentCluster().withName("testHCICluster")
                        .withWitnessType("Cloud")
                        .withWitnessPath("Cloud")
                        .withCloudAccountName("myasestoragacct")
                        .withAzureServiceEndpoint("core.windows.net"))
                    .withStorage(new Storage().withConfigurationMode("Express"))
                    .withNamingPrefix("ms169")
                    .withDomainFqdn("ASZ1PLab8.nttest.microsoft.com")
                    .withInfrastructureNetwork(Arrays.asList(new InfrastructureNetwork().withSubnetMask("255.255.248.0")
                        .withGateway("255.255.248.0")
                        .withIpPools(Arrays
                            .asList(new IpPools().withStartingAddress("10.57.48.60").withEndingAddress("10.57.48.66")))
                        .withDnsServers(Arrays.asList("10.57.50.90"))))
                    .withPhysicalNodes(
                        Arrays.asList(new PhysicalNodes().withName("ms169host").withIpv4Address("10.57.51.224"),
                            new PhysicalNodes().withName("ms154host").withIpv4Address("10.57.53.236")))
                    .withHostNetwork(new HostNetwork()
                        .withIntents(Arrays.asList(new Intents().withName("Compute_Management")
                            .withTrafficType(Arrays.asList("Compute", "Management"))
                            .withAdapter(Arrays.asList("Port2"))
                            .withOverrideVirtualSwitchConfiguration(false)
                            .withVirtualSwitchConfigurationOverrides(
                                new VirtualSwitchConfigurationOverrides().withEnableIov("True")
                                    .withLoadBalancingAlgorithm("HyperVPort"))
                            .withOverrideQosPolicy(false)
                            .withQosPolicyOverrides(new QosPolicyOverrides().withPriorityValue8021ActionCluster("7")
                                .withPriorityValue8021ActionSmb("3")
                                .withBandwidthPercentageSmb("50"))
                            .withOverrideAdapterProperty(false)
                            .withAdapterPropertyOverrides(new AdapterPropertyOverrides().withJumboPacket("1514")
                                .withNetworkDirect("Enabled")
                                .withNetworkDirectTechnology("iWARP"))))
                        .withStorageNetworks(Arrays.asList(new StorageNetworks().withName("Storage1Network")
                            .withNetworkAdapterName("Port3")
                            .withVlanId("5")))
                        .withStorageConnectivitySwitchless(true))
                    .withAdouPath("OU=ms169,DC=ASZ1PLab8,DC=nttest,DC=microsoft,DC=com")
                    .withSecretsLocation("fakeTokenPlaceholder")
                    .withOptionalServices(new OptionalServices().withCustomLocation("customLocationName"))))))
            .create();
    }
}
```

### DeploymentSettings_Delete

```java
/**
 * Samples for DeploymentSettings Delete.
 */
public final class DeploymentSettingsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * DeleteDeploymentSettings.json
     */
    /**
     * Sample code: Delete Deployment Settings.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void deleteDeploymentSettings(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.deploymentSettings().delete("test-rg", "myCluster", "default", com.azure.core.util.Context.NONE);
    }
}
```

### DeploymentSettings_Get

```java
/**
 * Samples for DeploymentSettings Get.
 */
public final class DeploymentSettingsGetSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * GetDeploymentSettings.json
     */
    /**
     * Sample code: Get Deployment Settings.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void getDeploymentSettings(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.deploymentSettings()
            .getWithResponse("test-rg", "myCluster", "default", com.azure.core.util.Context.NONE);
    }
}
```

### DeploymentSettings_ListByClusters

```java
/**
 * Samples for DeploymentSettings ListByClusters.
 */
public final class DeploymentSettingsListByClustersSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * ListDeploymentSettingsByCluster.json
     */
    /**
     * Sample code: List Deployment Settings.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void listDeploymentSettings(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.deploymentSettings().listByClusters("test-rg", "myCluster", com.azure.core.util.Context.NONE);
    }
}
```

### EdgeDevices_CreateOrUpdate

```java
import com.azure.resourcemanager.azurestackhci.models.DeviceConfiguration;
import com.azure.resourcemanager.azurestackhci.models.NicDetail;
import java.util.Arrays;

/**
 * Samples for EdgeDevices CreateOrUpdate.
 */
public final class EdgeDevicesCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/PutEdgeDevices.
     * json
     */
    /**
     * Sample code: Create Edge Device.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void createEdgeDevice(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.edgeDevices()
            .define("default")
            .withExistingResourceUri(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/ArcInstance-rg/providers/Microsoft.HybridCompute/machines/Node-1")
            .withDeviceConfiguration(
                new DeviceConfiguration().withNicDetails(Arrays.asList(new NicDetail().withAdapterName("ethernet")
                    .withInterfaceDescription("NDIS 6.70 ")
                    .withComponentId("VMBUS{f8615163-df3e-46c5-913f-f2d2f965ed0g} ")
                    .withDriverVersion("10.0.20348.1547 ")
                    .withIp4Address("10.10.10.10")
                    .withSubnetMask("255.255.255.0")
                    .withDefaultGateway("10.10.10.1")
                    .withDnsServers(Arrays.asList("100.10.10.1"))
                    .withDefaultIsolationId("0"))).withDeviceMetadata(""))
            .create();
    }
}
```

### EdgeDevices_Delete

```java
/**
 * Samples for EdgeDevices Delete.
 */
public final class EdgeDevicesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/DeleteEdgeDevices
     * .json
     */
    /**
     * Sample code: Delete Edge Devices.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void deleteEdgeDevices(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.edgeDevices()
            .delete(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/ArcInstance-rg/providers/Microsoft.HybridCompute/machines/Node-1",
                "default", com.azure.core.util.Context.NONE);
    }
}
```

### EdgeDevices_Get

```java
/**
 * Samples for EdgeDevices Get.
 */
public final class EdgeDevicesGetSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/GetEdgeDevices.
     * json
     */
    /**
     * Sample code: Get Edge Device.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void getEdgeDevice(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.edgeDevices()
            .getWithResponse(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/ArcInstance-rg/providers/Microsoft.HybridCompute/machines/Node-1",
                "default", com.azure.core.util.Context.NONE);
    }
}
```

### EdgeDevices_List

```java
/**
 * Samples for EdgeDevices List.
 */
public final class EdgeDevicesListSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/ListEdgeDevices.
     * json
     */
    /**
     * Sample code: List Edge Devices.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void listEdgeDevices(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.edgeDevices()
            .list(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/ArcInstance-rg/providers/Microsoft.HybridCompute/machines/Node-1",
                com.azure.core.util.Context.NONE);
    }
}
```

### EdgeDevices_Validate

```java
import com.azure.resourcemanager.azurestackhci.models.ValidateRequest;
import java.util.Arrays;

/**
 * Samples for EdgeDevices Validate.
 */
public final class EdgeDevicesValidateSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * ValidateEdgeDevices.json
     */
    /**
     * Sample code: Validate Edge Devices.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void validateEdgeDevices(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.edgeDevices()
            .validate(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/ArcInstance-rg/providers/Microsoft.HybridCompute/machines/Node-1",
                "default",
                new ValidateRequest().withEdgeDeviceIds(Arrays.asList(
                    "/subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/ArcInstance-rg/providers/Microsoft.HybridCompute/machines/Node-1/edgeDevices/default",
                    "/subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/ArcInstance-rg/providers/Microsoft.HybridCompute/machines/Node-2/edgeDevices/default"))
                    .withAdditionalInfo("test"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Extensions_Create

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import java.io.IOException;

/**
 * Samples for Extensions Create.
 */
public final class ExtensionsCreateSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/PutExtension.json
     */
    /**
     * Sample code: Create Arc Extension.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void createArcExtension(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager)
        throws IOException {
        manager.extensions()
            .define("MicrosoftMonitoringAgent")
            .withExistingArcSetting("test-rg", "myCluster", "default")
            .withPublisher("Microsoft.Compute")
            .withTypePropertiesType("MicrosoftMonitoringAgent")
            .withTypeHandlerVersion("1.10")
            .withSettings(SerializerFactory.createDefaultManagementSerializerAdapter()
                .deserialize("{\"workspaceId\":\"xx\"}", Object.class, SerializerEncoding.JSON))
            .withProtectedSettings(SerializerFactory.createDefaultManagementSerializerAdapter()
                .deserialize("{\"workspaceKey\":\"xx\"}", Object.class, SerializerEncoding.JSON))
            .withEnableAutomaticUpgrade(false)
            .create();
    }
}
```

### Extensions_Delete

```java
/**
 * Samples for Extensions Delete.
 */
public final class ExtensionsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/DeleteExtension.
     * json
     */
    /**
     * Sample code: Delete Arc Extension.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void deleteArcExtension(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.extensions()
            .delete("test-rg", "myCluster", "default", "MicrosoftMonitoringAgent", com.azure.core.util.Context.NONE);
    }
}
```

### Extensions_Get

```java
/**
 * Samples for Extensions Get.
 */
public final class ExtensionsGetSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/GetExtension.json
     */
    /**
     * Sample code: Get ArcSettings Extension.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void getArcSettingsExtension(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.extensions()
            .getWithResponse("test-rg", "myCluster", "default", "MicrosoftMonitoringAgent",
                com.azure.core.util.Context.NONE);
    }
}
```

### Extensions_ListByArcSetting

```java
/**
 * Samples for Extensions ListByArcSetting.
 */
public final class ExtensionsListByArcSettingSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * ListExtensionsByArcSetting.json
     */
    /**
     * Sample code: List Extensions under ArcSetting resource.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void
        listExtensionsUnderArcSettingResource(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.extensions().listByArcSetting("test-rg", "myCluster", "default", com.azure.core.util.Context.NONE);
    }
}
```

### Extensions_Update

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.azurestackhci.models.Extension;
import com.azure.resourcemanager.azurestackhci.models.ExtensionPatchParameters;
import java.io.IOException;

/**
 * Samples for Extensions Update.
 */
public final class ExtensionsUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/PatchExtension.
     * json
     */
    /**
     * Sample code: Update Arc Extension.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void updateArcExtension(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager)
        throws IOException {
        Extension resource = manager.extensions()
            .getWithResponse("test-rg", "myCluster", "default", "MicrosoftMonitoringAgent",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withExtensionParameters(new ExtensionPatchParameters().withTypeHandlerVersion("1.10")
                .withEnableAutomaticUpgrade(false)
                .withSettings(SerializerFactory.createDefaultManagementSerializerAdapter()
                    .deserialize("{\"workspaceId\":\"xx\"}", Object.class, SerializerEncoding.JSON))
                .withProtectedSettings(SerializerFactory.createDefaultManagementSerializerAdapter()
                    .deserialize("{\"workspaceKey\":\"xx\"}", Object.class, SerializerEncoding.JSON)))
            .apply();
    }
}
```

### Extensions_Upgrade

```java
import com.azure.resourcemanager.azurestackhci.models.ExtensionUpgradeParameters;

/**
 * Samples for Extensions Upgrade.
 */
public final class ExtensionsUpgradeSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * Extensions_Upgrade.json
     */
    /**
     * Sample code: Upgrade Machine Extensions.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void upgradeMachineExtensions(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.extensions()
            .upgrade("test-rg", "myCluster", "default", "MicrosoftMonitoringAgent",
                new ExtensionUpgradeParameters().withTargetVersion("1.0.18062.0"), com.azure.core.util.Context.NONE);
    }
}
```

### GalleryImagesOperation_CreateOrUpdate

```java
import com.azure.resourcemanager.azurestackhci.models.ExtendedLocation;
import com.azure.resourcemanager.azurestackhci.models.ExtendedLocationTypes;
import com.azure.resourcemanager.azurestackhci.models.OperatingSystemTypes;

/**
 * Samples for GalleryImagesOperation CreateOrUpdate.
 */
public final class GalleryImagesOperationCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/PutGalleryImage.
     * json
     */
    /**
     * Sample code: PutGalleryImage.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void putGalleryImage(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.galleryImagesOperations()
            .define("test-gallery-image")
            .withRegion("West US2")
            .withExistingResourceGroup("test-rg")
            .withExtendedLocation(new ExtendedLocation().withName(
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
/**
 * Samples for GalleryImagesOperation Delete.
 */
public final class GalleryImagesOperationDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * DeleteGalleryImage.json
     */
    /**
     * Sample code: DeleteGalleryImage.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void deleteGalleryImage(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.galleryImagesOperations().delete("test-rg", "test-gallery-image", com.azure.core.util.Context.NONE);
    }
}
```

### GalleryImagesOperation_GetByResourceGroup

```java
/**
 * Samples for GalleryImagesOperation GetByResourceGroup.
 */
public final class GalleryImagesOperationGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/GetGalleryImage.
     * json
     */
    /**
     * Sample code: GetGalleryImage.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void getGalleryImage(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.galleryImagesOperations()
            .getByResourceGroupWithResponse("test-rg", "test-gallery-image", com.azure.core.util.Context.NONE);
    }
}
```

### GalleryImagesOperation_List

```java
/**
 * Samples for GalleryImagesOperation List.
 */
public final class GalleryImagesOperationListSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * ListGalleryImageBySubscription.json
     */
    /**
     * Sample code: ListGalleryImageBySubscription.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void
        listGalleryImageBySubscription(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.galleryImagesOperations().list(com.azure.core.util.Context.NONE);
    }
}
```

### GalleryImagesOperation_ListByResourceGroup

```java
/**
 * Samples for GalleryImagesOperation ListByResourceGroup.
 */
public final class GalleryImagesOperationListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * ListGalleryImageByResourceGroup.json
     */
    /**
     * Sample code: ListGalleryImageByResourceGroup.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void
        listGalleryImageByResourceGroup(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.galleryImagesOperations().listByResourceGroup("test-rg", com.azure.core.util.Context.NONE);
    }
}
```

### GalleryImagesOperation_Update

```java
import com.azure.resourcemanager.azurestackhci.models.GalleryImages;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for GalleryImagesOperation Update.
 */
public final class GalleryImagesOperationUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * UpdateGalleryImage.json
     */
    /**
     * Sample code: UpdateGalleryImage.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void updateGalleryImage(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        GalleryImages resource = manager.galleryImagesOperations()
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

/**
 * Samples for GuestAgent Create.
 */
public final class GuestAgentCreateSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/CreateGuestAgent.
     * json
     */
    /**
     * Sample code: CreateGuestAgent.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void createGuestAgent(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.guestAgents()
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
/**
 * Samples for GuestAgent Delete.
 */
public final class GuestAgentDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/DeleteGuestAgent.
     * json
     */
    /**
     * Sample code: DeleteGuestAgent.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void deleteGuestAgent(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.guestAgents()
            .delete(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/Microsoft.HybridCompute/machines/DemoVM",
                com.azure.core.util.Context.NONE);
    }
}
```

### GuestAgent_Get

```java
/**
 * Samples for GuestAgent Get.
 */
public final class GuestAgentGetSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/GetGuestAgent.
     * json
     */
    /**
     * Sample code: GetGuestAgent.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void getGuestAgent(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.guestAgents()
            .getWithResponse(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/Microsoft.HybridCompute/machines/DemoVM",
                com.azure.core.util.Context.NONE);
    }
}
```

### GuestAgentsOperation_List

```java
/**
 * Samples for GuestAgentsOperation List.
 */
public final class GuestAgentsOperationListSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/GuestAgent_List.
     * json
     */
    /**
     * Sample code: GuestAgentListByVirtualMachineInstances.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void
        guestAgentListByVirtualMachineInstances(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.guestAgentsOperations()
            .list(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/Microsoft.HybridCompute/machines/DemoVM",
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
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * GetHybridIdentityMetadata.json
     */
    /**
     * Sample code: GetHybridIdentityMetadata.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void getHybridIdentityMetadata(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.hybridIdentityMetadatas()
            .getWithResponse(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/Microsoft.HybridCompute/machines/DemoVM",
                com.azure.core.util.Context.NONE);
    }
}
```

### HybridIdentityMetadata_List

```java
/**
 * Samples for HybridIdentityMetadata List.
 */
public final class HybridIdentityMetadataListSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * HybridIdentityMetadata_List.json
     */
    /**
     * Sample code: HybridIdentityMetadataListByVirtualMachineInstances.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void hybridIdentityMetadataListByVirtualMachineInstances(
        com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.hybridIdentityMetadatas()
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

/**
 * Samples for LogicalNetworksOperation CreateOrUpdate.
 */
public final class LogicalNetworksOperationCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/PutLogicalNetwork
     * .json
     */
    /**
     * Sample code: PutLogicalNetwork.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void putLogicalNetwork(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.logicalNetworksOperations()
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

### LogicalNetworksOperation_Delete

```java
/**
 * Samples for LogicalNetworksOperation Delete.
 */
public final class LogicalNetworksOperationDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * DeleteLogicalNetwork.json
     */
    /**
     * Sample code: DeleteLogicalNetwork.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void deleteLogicalNetwork(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.logicalNetworksOperations().delete("test-rg", "test-lnet", com.azure.core.util.Context.NONE);
    }
}
```

### LogicalNetworksOperation_GetByResourceGroup

```java
/**
 * Samples for LogicalNetworksOperation GetByResourceGroup.
 */
public final class LogicalNetworksOperationGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/GetLogicalNetwork
     * .json
     */
    /**
     * Sample code: GetLogicalNetwork.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void getLogicalNetwork(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.logicalNetworksOperations()
            .getByResourceGroupWithResponse("test-rg", "test-lnet", com.azure.core.util.Context.NONE);
    }
}
```

### LogicalNetworksOperation_List

```java
/**
 * Samples for LogicalNetworksOperation List.
 */
public final class LogicalNetworksOperationListSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * ListLogicalNetworkBySubscription.json
     */
    /**
     * Sample code: ListLogicalNetworkBySubscription.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void
        listLogicalNetworkBySubscription(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.logicalNetworksOperations().list(com.azure.core.util.Context.NONE);
    }
}
```

### LogicalNetworksOperation_ListByResourceGroup

```java
/**
 * Samples for LogicalNetworksOperation ListByResourceGroup.
 */
public final class LogicalNetworksOperationListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * ListLogicalNetworkByResourceGroup.json
     */
    /**
     * Sample code: ListLogicalNetworkByResourceGroup.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void
        listLogicalNetworkByResourceGroup(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.logicalNetworksOperations().listByResourceGroup("test-rg", com.azure.core.util.Context.NONE);
    }
}
```

### LogicalNetworksOperation_Update

```java
import com.azure.resourcemanager.azurestackhci.models.LogicalNetworks;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for LogicalNetworksOperation Update.
 */
public final class LogicalNetworksOperationUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * UpdateLogicalNetwork.json
     */
    /**
     * Sample code: UpdateLogicalNetwork.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void updateLogicalNetwork(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        LogicalNetworks resource = manager.logicalNetworksOperations()
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

/**
 * Samples for MarketplaceGalleryImagesOperation CreateOrUpdate.
 */
public final class MarketplaceGalleryImagesOperationCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * PutMarketplaceGalleryImage.json
     */
    /**
     * Sample code: PutMarketplaceGalleryImage.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void
        putMarketplaceGalleryImage(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.marketplaceGalleryImagesOperations()
            .define("test-marketplace-gallery-image")
            .withRegion("West US2")
            .withExistingResourceGroup("test-rg")
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.ExtendedLocation/customLocations/dogfood-location")
                .withType(ExtendedLocationTypes.CUSTOM_LOCATION))
            .withContainerId(
                "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.AzureStackHCI/storageContainers/test-storage-container")
            .withOsType(OperatingSystemTypes.WINDOWS)
            .withCloudInitDataSource(CloudInitDataSource.AZURE)
            .withHyperVGeneration(HyperVGeneration.V1)
            .withIdentifier(new GalleryImageIdentifier().withPublisher("myPublisherName")
                .withOffer("myOfferName")
                .withSku("mySkuName"))
            .withVersion(new GalleryImageVersion().withName("1.0.0"))
            .create();
    }
}
```

### MarketplaceGalleryImagesOperation_Delete

```java
/**
 * Samples for MarketplaceGalleryImagesOperation Delete.
 */
public final class MarketplaceGalleryImagesOperationDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * DeleteMarketplaceGalleryImage.json
     */
    /**
     * Sample code: DeleteMarketplaceGalleryImage.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void
        deleteMarketplaceGalleryImage(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.marketplaceGalleryImagesOperations()
            .delete("test-rg", "test-marketplace-gallery-image", com.azure.core.util.Context.NONE);
    }
}
```

### MarketplaceGalleryImagesOperation_GetByResourceGroup

```java
/**
 * Samples for MarketplaceGalleryImagesOperation GetByResourceGroup.
 */
public final class MarketplaceGalleryImagesOperationGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * GetMarketplaceGalleryImage.json
     */
    /**
     * Sample code: GetMarketplaceGalleryImage.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void
        getMarketplaceGalleryImage(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.marketplaceGalleryImagesOperations()
            .getByResourceGroupWithResponse("test-rg", "test-marketplace-gallery-image",
                com.azure.core.util.Context.NONE);
    }
}
```

### MarketplaceGalleryImagesOperation_List

```java
/**
 * Samples for MarketplaceGalleryImagesOperation List.
 */
public final class MarketplaceGalleryImagesOperationListSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * ListMarketplaceGalleryImageBySubscription.json
     */
    /**
     * Sample code: ListMarketplaceGalleryImageBySubscription.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void listMarketplaceGalleryImageBySubscription(
        com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.marketplaceGalleryImagesOperations().list(com.azure.core.util.Context.NONE);
    }
}
```

### MarketplaceGalleryImagesOperation_ListByResourceGroup

```java
/**
 * Samples for MarketplaceGalleryImagesOperation ListByResourceGroup.
 */
public final class MarketplaceGalleryImagesOperationListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * ListMarketplaceGalleryImageByResourceGroup.json
     */
    /**
     * Sample code: ListMarketplaceGalleryImageByResourceGroup.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void listMarketplaceGalleryImageByResourceGroup(
        com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.marketplaceGalleryImagesOperations().listByResourceGroup("test-rg", com.azure.core.util.Context.NONE);
    }
}
```

### MarketplaceGalleryImagesOperation_Update

```java
import com.azure.resourcemanager.azurestackhci.models.MarketplaceGalleryImages;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for MarketplaceGalleryImagesOperation Update.
 */
public final class MarketplaceGalleryImagesOperationUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * UpdateMarketplaceGalleryImage.json
     */
    /**
     * Sample code: UpdateMarketplaceGalleryImage.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void
        updateMarketplaceGalleryImage(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        MarketplaceGalleryImages resource = manager.marketplaceGalleryImagesOperations()
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

### NetworkInterfacesOperation_CreateOrUpdate

```java
import com.azure.resourcemanager.azurestackhci.models.ExtendedLocation;
import com.azure.resourcemanager.azurestackhci.models.ExtendedLocationTypes;
import com.azure.resourcemanager.azurestackhci.models.IpConfiguration;
import com.azure.resourcemanager.azurestackhci.models.IpConfigurationProperties;
import com.azure.resourcemanager.azurestackhci.models.IpConfigurationPropertiesSubnet;
import java.util.Arrays;

/**
 * Samples for NetworkInterfacesOperation CreateOrUpdate.
 */
public final class NetworkInterfacesOperationCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * PutNetworkInterface.json
     */
    /**
     * Sample code: PutNetworkInterface.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void putNetworkInterface(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.networkInterfacesOperations()
            .define("test-nic")
            .withRegion("West US2")
            .withExistingResourceGroup("test-rg")
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.ExtendedLocation/customLocations/dogfood-location")
                .withType(ExtendedLocationTypes.CUSTOM_LOCATION))
            .withIpConfigurations(Arrays.asList(new IpConfiguration().withName("ipconfig-sample")
                .withProperties(new IpConfigurationProperties()
                    .withSubnet(new IpConfigurationPropertiesSubnet().withId("test-lnet")))))
            .create();
    }
}
```

### NetworkInterfacesOperation_Delete

```java
/**
 * Samples for NetworkInterfacesOperation Delete.
 */
public final class NetworkInterfacesOperationDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * DeleteNetworkInterface.json
     */
    /**
     * Sample code: DeleteNetworkInterface.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void deleteNetworkInterface(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.networkInterfacesOperations().delete("test-rg", "test-nic", com.azure.core.util.Context.NONE);
    }
}
```

### NetworkInterfacesOperation_GetByResourceGroup

```java
/**
 * Samples for NetworkInterfacesOperation GetByResourceGroup.
 */
public final class NetworkInterfacesOperationGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * GetNetworkInterface.json
     */
    /**
     * Sample code: GetNetworkInterface.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void getNetworkInterface(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.networkInterfacesOperations()
            .getByResourceGroupWithResponse("test-rg", "test-nic", com.azure.core.util.Context.NONE);
    }
}
```

### NetworkInterfacesOperation_List

```java
/**
 * Samples for NetworkInterfacesOperation List.
 */
public final class NetworkInterfacesOperationListSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * ListNetworkInterfaceBySubscription.json
     */
    /**
     * Sample code: ListNetworkInterfaceBySubscription.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void
        listNetworkInterfaceBySubscription(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.networkInterfacesOperations().list(com.azure.core.util.Context.NONE);
    }
}
```

### NetworkInterfacesOperation_ListByResourceGroup

```java
/**
 * Samples for NetworkInterfacesOperation ListByResourceGroup.
 */
public final class NetworkInterfacesOperationListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * ListNetworkInterfaceByResourceGroup.json
     */
    /**
     * Sample code: ListNetworkInterfaceByResourceGroup.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void
        listNetworkInterfaceByResourceGroup(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.networkInterfacesOperations().listByResourceGroup("test-rg", com.azure.core.util.Context.NONE);
    }
}
```

### NetworkInterfacesOperation_Update

```java
import com.azure.resourcemanager.azurestackhci.models.NetworkInterfaces;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for NetworkInterfacesOperation Update.
 */
public final class NetworkInterfacesOperationUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * UpdateNetworkInterface.json
     */
    /**
     * Sample code: UpdateNetworkInterface.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void updateNetworkInterface(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        NetworkInterfaces resource = manager.networkInterfacesOperations()
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
/**
 * Samples for Offers Get.
 */
public final class OffersGetSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/GetOffer.json
     */
    /**
     * Sample code: Get Offer.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void getOffer(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.offers()
            .getWithResponse("test-rg", "myCluster", "publisher1", "offer1", null, com.azure.core.util.Context.NONE);
    }
}
```

### Offers_ListByCluster

```java
/**
 * Samples for Offers ListByCluster.
 */
public final class OffersListByClusterSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * ListOffersByCluster.json
     */
    /**
     * Sample code: List Offer resources by HCI Cluster.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void
        listOfferResourcesByHCICluster(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.offers().listByCluster("test-rg", "myCluster", null, com.azure.core.util.Context.NONE);
    }
}
```

### Offers_ListByPublisher

```java
/**
 * Samples for Offers ListByPublisher.
 */
public final class OffersListByPublisherSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * ListOffersByPublisher.json
     */
    /**
     * Sample code: List Offer resources by publisher for the HCI Cluster.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void listOfferResourcesByPublisherForTheHCICluster(
        com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.offers().listByPublisher("test-rg", "myCluster", "publisher1", null, com.azure.core.util.Context.NONE);
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
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/ListOperations.
     * json
     */
    /**
     * Sample code: Create cluster.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void createCluster(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.operations().listWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

### Publishers_Get

```java
/**
 * Samples for Publishers Get.
 */
public final class PublishersGetSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/GetPublisher.json
     */
    /**
     * Sample code: Get Publisher.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void getPublisher(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.publishers().getWithResponse("test-rg", "myCluster", "publisher1", com.azure.core.util.Context.NONE);
    }
}
```

### Publishers_ListByCluster

```java
/**
 * Samples for Publishers ListByCluster.
 */
public final class PublishersListByClusterSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * ListPublishersByCluster.json
     */
    /**
     * Sample code: List Publisher resources by HCI Cluster.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void
        listPublisherResourcesByHCICluster(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.publishers().listByCluster("test-rg", "myCluster", com.azure.core.util.Context.NONE);
    }
}
```

### SecuritySettings_CreateOrUpdate

```java
import com.azure.resourcemanager.azurestackhci.models.ComplianceAssignmentType;

/**
 * Samples for SecuritySettings CreateOrUpdate.
 */
public final class SecuritySettingsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * PutSecuritySettings.json
     */
    /**
     * Sample code: Create Security Settings.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void createSecuritySettings(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.securitySettings()
            .define("default")
            .withExistingCluster("test-rg", "myCluster")
            .withSecuredCoreComplianceAssignment(ComplianceAssignmentType.AUDIT)
            .create();
    }
}
```

### SecuritySettings_Delete

```java
/**
 * Samples for SecuritySettings Delete.
 */
public final class SecuritySettingsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * DeleteSecuritySettings.json
     */
    /**
     * Sample code: Delete Security Settings.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void deleteSecuritySettings(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.securitySettings().delete("test-rg", "myCluster", "default", com.azure.core.util.Context.NONE);
    }
}
```

### SecuritySettings_Get

```java
/**
 * Samples for SecuritySettings Get.
 */
public final class SecuritySettingsGetSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * GetSecuritySettings.json
     */
    /**
     * Sample code: Get Security Settings.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void getSecuritySettings(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.securitySettings().getWithResponse("test-rg", "myCluster", "default", com.azure.core.util.Context.NONE);
    }
}
```

### SecuritySettings_ListByClusters

```java
/**
 * Samples for SecuritySettings ListByClusters.
 */
public final class SecuritySettingsListByClustersSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * ListSecuritySettingsByCluster.json
     */
    /**
     * Sample code: List Security Settings.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void listSecuritySettings(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.securitySettings().listByClusters("test-rg", "myCluster", com.azure.core.util.Context.NONE);
    }
}
```

### Skus_Get

```java
/**
 * Samples for Skus Get.
 */
public final class SkusGetSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/GetSku.json
     */
    /**
     * Sample code: Get Sku.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void getSku(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.skus()
            .getWithResponse("test-rg", "myCluster", "publisher1", "offer1", "sku1", null,
                com.azure.core.util.Context.NONE);
    }
}
```

### Skus_ListByOffer

```java
/**
 * Samples for Skus ListByOffer.
 */
public final class SkusListByOfferSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/ListSkusByOffer.
     * json
     */
    /**
     * Sample code: List SKU resources by offer for the HCI Cluster.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void
        listSKUResourcesByOfferForTheHCICluster(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.skus()
            .listByOffer("test-rg", "myCluster", "publisher1", "offer1", null, com.azure.core.util.Context.NONE);
    }
}
```

### StorageContainersOperation_CreateOrUpdate

```java
import com.azure.resourcemanager.azurestackhci.models.ExtendedLocation;
import com.azure.resourcemanager.azurestackhci.models.ExtendedLocationTypes;

/**
 * Samples for StorageContainersOperation CreateOrUpdate.
 */
public final class StorageContainersOperationCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * PutStorageContainer.json
     */
    /**
     * Sample code: PutStorageContainer.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void putStorageContainer(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.storageContainersOperations()
            .define("Default_Container")
            .withRegion("West US2")
            .withExistingResourceGroup("test-rg")
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.ExtendedLocation/customLocations/dogfood-location")
                .withType(ExtendedLocationTypes.CUSTOM_LOCATION))
            .withPath("C:\\container_storage")
            .create();
    }
}
```

### StorageContainersOperation_Delete

```java
/**
 * Samples for StorageContainersOperation Delete.
 */
public final class StorageContainersOperationDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * DeleteStorageContainer.json
     */
    /**
     * Sample code: DeleteStorageContainer.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void deleteStorageContainer(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.storageContainersOperations().delete("test-rg", "Default_Container", com.azure.core.util.Context.NONE);
    }
}
```

### StorageContainersOperation_GetByResourceGroup

```java
/**
 * Samples for StorageContainersOperation GetByResourceGroup.
 */
public final class StorageContainersOperationGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * GetStorageContainer.json
     */
    /**
     * Sample code: GetStorageContainer.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void getStorageContainer(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.storageContainersOperations()
            .getByResourceGroupWithResponse("test-rg", "Default_Container", com.azure.core.util.Context.NONE);
    }
}
```

### StorageContainersOperation_List

```java
/**
 * Samples for StorageContainersOperation List.
 */
public final class StorageContainersOperationListSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * ListStorageContainerBySubscription.json
     */
    /**
     * Sample code: ListStorageContainerBySubscription.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void
        listStorageContainerBySubscription(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.storageContainersOperations().list(com.azure.core.util.Context.NONE);
    }
}
```

### StorageContainersOperation_ListByResourceGroup

```java
/**
 * Samples for StorageContainersOperation ListByResourceGroup.
 */
public final class StorageContainersOperationListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * ListStorageContainerByResourceGroup.json
     */
    /**
     * Sample code: ListStorageContainerByResourceGroup.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void
        listStorageContainerByResourceGroup(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.storageContainersOperations().listByResourceGroup("test-rg", com.azure.core.util.Context.NONE);
    }
}
```

### StorageContainersOperation_Update

```java
import com.azure.resourcemanager.azurestackhci.models.StorageContainers;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for StorageContainersOperation Update.
 */
public final class StorageContainersOperationUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * UpdateStorageContainer.json
     */
    /**
     * Sample code: UpdateStorageContainer.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void updateStorageContainer(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        StorageContainers resource = manager.storageContainersOperations()
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
/**
 * Samples for UpdateRuns Delete.
 */
public final class UpdateRunsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/DeleteUpdateRuns.
     * json
     */
    /**
     * Sample code: Delete an Update.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void deleteAnUpdate(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.updateRuns()
            .delete("testrg", "testcluster", "Microsoft4.2203.2.32", "23b779ba-0d52-4a80-8571-45ca74664ec3",
                com.azure.core.util.Context.NONE);
    }
}
```

### UpdateRuns_Get

```java
/**
 * Samples for UpdateRuns Get.
 */
public final class UpdateRunsGetSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/GetUpdateRuns.
     * json
     */
    /**
     * Sample code: Get Update runs under cluster resource.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void
        getUpdateRunsUnderClusterResource(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.updateRuns()
            .getWithResponse("testrg", "testcluster", "Microsoft4.2203.2.32", "23b779ba-0d52-4a80-8571-45ca74664ec3",
                com.azure.core.util.Context.NONE);
    }
}
```

### UpdateRuns_List

```java
/**
 * Samples for UpdateRuns List.
 */
public final class UpdateRunsListSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/ListUpdateRuns.
     * json
     */
    /**
     * Sample code: List Update runs under cluster resource.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void
        listUpdateRunsUnderClusterResource(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
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

/**
 * Samples for UpdateRuns Put.
 */
public final class UpdateRunsPutSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/PutUpdateRuns.
     * json
     */
    /**
     * Sample code: Get Update runs under cluster resource.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void
        getUpdateRunsUnderClusterResource(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        UpdateRun resource = manager.updateRuns()
            .getWithResponse("testrg", "testcluster", "Microsoft4.2203.2.32", "23b779ba-0d52-4a80-8571-45ca74664ec3",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withNamePropertiesName("Unnamed step")
            .withDescription("Update Azure Stack.")
            .withErrorMessage("")
            .withStatus("Success")
            .withStartTimeUtc(OffsetDateTime.parse("2022-04-06T01:36:33.3876751+00:00"))
            .withEndTimeUtc(OffsetDateTime.parse("2022-04-06T13:58:42.969006+00:00"))
            .withLastUpdatedTimeUtc(OffsetDateTime.parse("2022-04-06T13:58:42.969006+00:00"))
            .withSteps(Arrays.asList(new Step().withName("PreUpdate Cloud")
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
/**
 * Samples for UpdateSummariesOperation Delete.
 */
public final class UpdateSummariesOperationDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * DeleteUpdateSummaries.json
     */
    /**
     * Sample code: Delete an Update.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void deleteAnUpdate(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.updateSummariesOperations().delete("testrg", "testcluster", com.azure.core.util.Context.NONE);
    }
}
```

### UpdateSummariesOperation_Get

```java
/**
 * Samples for UpdateSummariesOperation Get.
 */
public final class UpdateSummariesOperationGetSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * GetUpdateSummaries.json
     */
    /**
     * Sample code: Get Update summaries under cluster resource.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void
        getUpdateSummariesUnderClusterResource(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.updateSummariesOperations().getWithResponse("testrg", "testcluster", com.azure.core.util.Context.NONE);
    }
}
```

### UpdateSummariesOperation_List

```java
/**
 * Samples for UpdateSummariesOperation List.
 */
public final class UpdateSummariesOperationListSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * ListUpdateSummaries.json
     */
    /**
     * Sample code: Get Update summaries under cluster resource.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void
        getUpdateSummariesUnderClusterResource(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.updateSummariesOperations().list("testrg", "testcluster", com.azure.core.util.Context.NONE);
    }
}
```

### UpdateSummariesOperation_Put

```java
import com.azure.resourcemanager.azurestackhci.fluent.models.UpdateSummariesInner;
import com.azure.resourcemanager.azurestackhci.models.UpdateSummariesPropertiesState;
import java.time.OffsetDateTime;

/**
 * Samples for UpdateSummariesOperation Put.
 */
public final class UpdateSummariesOperationPutSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * PutUpdateSummaries.json
     */
    /**
     * Sample code: Put Update summaries under cluster resource.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void
        putUpdateSummariesUnderClusterResource(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.updateSummariesOperations()
            .putWithResponse("testrg", "testcluster",
                new UpdateSummariesInner().withOemFamily("DellEMC")
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
/**
 * Samples for Updates Delete.
 */
public final class UpdatesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/DeleteUpdates.
     * json
     */
    /**
     * Sample code: Delete an Update.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void deleteAnUpdate(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.updates().delete("testrg", "testcluster", "Microsoft4.2203.2.32", com.azure.core.util.Context.NONE);
    }
}
```

### Updates_Get

```java
/**
 * Samples for Updates Get.
 */
public final class UpdatesGetSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/GetUpdates.json
     */
    /**
     * Sample code: Get a specific update.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void getASpecificUpdate(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.updates()
            .getWithResponse("testrg", "testcluster", "Microsoft4.2203.2.32", com.azure.core.util.Context.NONE);
    }
}
```

### Updates_List

```java
/**
 * Samples for Updates List.
 */
public final class UpdatesListSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/ListUpdates.json
     */
    /**
     * Sample code: List available updates.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void listAvailableUpdates(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.updates().list("testrg", "testcluster", com.azure.core.util.Context.NONE);
    }
}
```

### Updates_Post

```java
/**
 * Samples for Updates Post.
 */
public final class UpdatesPostSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/PostUpdates.json
     */
    /**
     * Sample code: List available updates.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void listAvailableUpdates(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
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

/**
 * Samples for Updates Put.
 */
public final class UpdatesPutSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/PutUpdates.json
     */
    /**
     * Sample code: Put a specific update.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void putASpecificUpdate(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        HciUpdate resource = manager.updates()
            .getWithResponse("testrg", "testcluster", "Microsoft4.2203.2.32", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withInstalledDate(OffsetDateTime.parse("2022-04-06T14:08:18.254Z"))
            .withDescription("AzS Update 4.2203.2.32")
            .withState(State.INSTALLED)
            .withPrerequisites(Arrays.asList(new UpdatePrerequisite().withUpdateType("update type")
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

/**
 * Samples for VirtualHardDisksOperation CreateOrUpdate.
 */
public final class VirtualHardDisksOperationCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * PutVirtualHardDisk.json
     */
    /**
     * Sample code: PutVirtualHardDisk.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void putVirtualHardDisk(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.virtualHardDisksOperations()
            .define("test-vhd")
            .withRegion("West US2")
            .withExistingResourceGroup("test-rg")
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.ExtendedLocation/customLocations/dogfood-location")
                .withType(ExtendedLocationTypes.CUSTOM_LOCATION))
            .withDiskSizeGB(32L)
            .create();
    }
}
```

### VirtualHardDisksOperation_Delete

```java
/**
 * Samples for VirtualHardDisksOperation Delete.
 */
public final class VirtualHardDisksOperationDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * DeleteVirtualHardDisk.json
     */
    /**
     * Sample code: DeleteVirtualHardDisk.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void deleteVirtualHardDisk(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.virtualHardDisksOperations().delete("test-rg", "test-vhd", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualHardDisksOperation_GetByResourceGroup

```java
/**
 * Samples for VirtualHardDisksOperation GetByResourceGroup.
 */
public final class VirtualHardDisksOperationGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * GetVirtualHardDisk.json
     */
    /**
     * Sample code: GetVirtualHardDisk.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void getVirtualHardDisk(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.virtualHardDisksOperations()
            .getByResourceGroupWithResponse("test-rg", "test-vhd", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualHardDisksOperation_List

```java
/**
 * Samples for VirtualHardDisksOperation List.
 */
public final class VirtualHardDisksOperationListSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * ListVirtualHardDiskBySubscription.json
     */
    /**
     * Sample code: ListVirtualHardDiskBySubscription.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void
        listVirtualHardDiskBySubscription(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.virtualHardDisksOperations().list(com.azure.core.util.Context.NONE);
    }
}
```

### VirtualHardDisksOperation_ListByResourceGroup

```java
/**
 * Samples for VirtualHardDisksOperation ListByResourceGroup.
 */
public final class VirtualHardDisksOperationListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * ListVirtualHardDiskByResourceGroup.json
     */
    /**
     * Sample code: ListVirtualHardDiskByResourceGroup.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void
        listVirtualHardDiskByResourceGroup(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.virtualHardDisksOperations().listByResourceGroup("test-rg", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualHardDisksOperation_Update

```java
import com.azure.resourcemanager.azurestackhci.models.VirtualHardDisks;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for VirtualHardDisksOperation Update.
 */
public final class VirtualHardDisksOperationUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * UpdateVirtualHardDisk.json
     */
    /**
     * Sample code: UpdateVirtualHardDisk.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void updateVirtualHardDisk(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        VirtualHardDisks resource = manager.virtualHardDisksOperations()
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

/**
 * Samples for VirtualMachineInstances CreateOrUpdate.
 */
public final class VirtualMachineInstancesCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * PutVirtualMachineInstanceWithGalleryImage.json
     */
    /**
     * Sample code: PutVirtualMachineInstanceWithGalleryImage.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void putVirtualMachineInstanceWithGalleryImage(
        com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.virtualMachineInstances()
            .createOrUpdate(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/Microsoft.HybridCompute/machines/DemoVM",
                new VirtualMachineInstanceInner().withExtendedLocation(new ExtendedLocation().withName(
                    "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.ExtendedLocation/customLocations/dogfood-location")
                    .withType(ExtendedLocationTypes.CUSTOM_LOCATION))
                    .withHardwareProfile(
                        new VirtualMachineInstancePropertiesHardwareProfile().withVmSize(VmSizeEnum.DEFAULT))
                    .withNetworkProfile(new VirtualMachineInstancePropertiesNetworkProfile().withNetworkInterfaces(
                        Arrays.asList(new VirtualMachineInstancePropertiesNetworkProfileNetworkInterfacesItem()
                            .withId("test-nic"))))
                    .withOsProfile(
                        new VirtualMachineInstancePropertiesOsProfile().withAdminPassword("fakeTokenPlaceholder")
                            .withAdminUsername("localadmin")
                            .withComputerName("luamaster"))
                    .withSecurityProfile(new VirtualMachineInstancePropertiesSecurityProfile().withEnableTpm(true)
                        .withUefiSettings(new VirtualMachineInstancePropertiesSecurityProfileUefiSettings()
                            .withSecureBootEnabled(true)))
                    .withStorageProfile(new VirtualMachineInstancePropertiesStorageProfile()
                        .withImageReference(new VirtualMachineInstancePropertiesStorageProfileImageReference().withId(
                            "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.AzureStackHCI/galleryImages/test-gallery-image"))
                        .withVmConfigStoragePathId(
                            "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.AzureStackHCI/storageContainers/test-container")),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * PutVirtualMachineInstanceWithOsDisk.json
     */
    /**
     * Sample code: PutVirtualMachineInstanceWithOsDisk.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void
        putVirtualMachineInstanceWithOsDisk(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.virtualMachineInstances()
            .createOrUpdate(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/Microsoft.HybridCompute/machines/DemoVM",
                new VirtualMachineInstanceInner().withExtendedLocation(new ExtendedLocation().withName(
                    "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.ExtendedLocation/customLocations/dogfood-location")
                    .withType(ExtendedLocationTypes.CUSTOM_LOCATION))
                    .withHardwareProfile(
                        new VirtualMachineInstancePropertiesHardwareProfile().withVmSize(VmSizeEnum.DEFAULT))
                    .withNetworkProfile(new VirtualMachineInstancePropertiesNetworkProfile().withNetworkInterfaces(
                        Arrays.asList(new VirtualMachineInstancePropertiesNetworkProfileNetworkInterfacesItem()
                            .withId("test-nic"))))
                    .withSecurityProfile(new VirtualMachineInstancePropertiesSecurityProfile().withEnableTpm(true)
                        .withUefiSettings(new VirtualMachineInstancePropertiesSecurityProfileUefiSettings()
                            .withSecureBootEnabled(true)))
                    .withStorageProfile(new VirtualMachineInstancePropertiesStorageProfile()
                        .withOsDisk(new VirtualMachineInstancePropertiesStorageProfileOsDisk().withId(
                            "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.AzureStackHCI/virtualHardDisks/test-vhd"))
                        .withVmConfigStoragePathId(
                            "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.AzureStackHCI/storageContainers/test-container")),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * PutVirtualMachineInstanceWithMarketplaceGalleryImage.json
     */
    /**
     * Sample code: PutVirtualMachineInstanceWithMarketplaceGalleryImage.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void putVirtualMachineInstanceWithMarketplaceGalleryImage(
        com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.virtualMachineInstances()
            .createOrUpdate(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/Microsoft.HybridCompute/machines/DemoVM",
                new VirtualMachineInstanceInner().withExtendedLocation(new ExtendedLocation().withName(
                    "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.ExtendedLocation/customLocations/dogfood-location")
                    .withType(ExtendedLocationTypes.CUSTOM_LOCATION))
                    .withHardwareProfile(
                        new VirtualMachineInstancePropertiesHardwareProfile().withVmSize(VmSizeEnum.DEFAULT))
                    .withNetworkProfile(new VirtualMachineInstancePropertiesNetworkProfile().withNetworkInterfaces(
                        Arrays.asList(new VirtualMachineInstancePropertiesNetworkProfileNetworkInterfacesItem()
                            .withId("test-nic"))))
                    .withOsProfile(
                        new VirtualMachineInstancePropertiesOsProfile().withAdminPassword("fakeTokenPlaceholder")
                            .withAdminUsername("localadmin")
                            .withComputerName("luamaster"))
                    .withSecurityProfile(new VirtualMachineInstancePropertiesSecurityProfile().withEnableTpm(true)
                        .withUefiSettings(new VirtualMachineInstancePropertiesSecurityProfileUefiSettings()
                            .withSecureBootEnabled(true)))
                    .withStorageProfile(new VirtualMachineInstancePropertiesStorageProfile()
                        .withImageReference(new VirtualMachineInstancePropertiesStorageProfileImageReference().withId(
                            "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.AzureStackHCI/marketplaceGalleryImages/test-marketplace-gallery-image"))
                        .withVmConfigStoragePathId(
                            "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.AzureStackHCI/storageContainers/test-container")),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * PutVirtualMachineInstanceWithVMConfigAgent.json
     */
    /**
     * Sample code: PutVirtualMachineInstanceWithVMConfigAgent.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void putVirtualMachineInstanceWithVMConfigAgent(
        com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.virtualMachineInstances()
            .createOrUpdate(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/Microsoft.HybridCompute/machines/DemoVM",
                new VirtualMachineInstanceInner().withExtendedLocation(new ExtendedLocation().withName(
                    "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.ExtendedLocation/customLocations/dogfood-location")
                    .withType(ExtendedLocationTypes.CUSTOM_LOCATION))
                    .withHardwareProfile(
                        new VirtualMachineInstancePropertiesHardwareProfile().withVmSize(VmSizeEnum.DEFAULT))
                    .withNetworkProfile(new VirtualMachineInstancePropertiesNetworkProfile().withNetworkInterfaces(
                        Arrays.asList(new VirtualMachineInstancePropertiesNetworkProfileNetworkInterfacesItem()
                            .withId("test-nic"))))
                    .withOsProfile(new VirtualMachineInstancePropertiesOsProfile()
                        .withAdminPassword("fakeTokenPlaceholder")
                        .withAdminUsername("localadmin")
                        .withComputerName("luamaster")
                        .withWindowsConfiguration(new VirtualMachineInstancePropertiesOsProfileWindowsConfiguration()
                            .withProvisionVMConfigAgent(true)))
                    .withSecurityProfile(new VirtualMachineInstancePropertiesSecurityProfile().withEnableTpm(true)
                        .withUefiSettings(new VirtualMachineInstancePropertiesSecurityProfileUefiSettings()
                            .withSecureBootEnabled(true)))
                    .withStorageProfile(new VirtualMachineInstancePropertiesStorageProfile()
                        .withImageReference(new VirtualMachineInstancePropertiesStorageProfileImageReference().withId(
                            "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.AzureStackHCI/galleryImages/test-gallery-image"))
                        .withVmConfigStoragePathId(
                            "/subscriptions/a95612cb-f1fa-4daa-a4fd-272844fa512c/resourceGroups/dogfoodarc/providers/Microsoft.AzureStackHCI/storageContainers/test-container")),
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
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * DeleteVirtualMachineInstance.json
     */
    /**
     * Sample code: DeleteVirtualMachine.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void deleteVirtualMachine(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.virtualMachineInstances()
            .delete(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/Microsoft.HybridCompute/machines/DemoVM",
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
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * GetVirtualMachineInstance.json
     */
    /**
     * Sample code: GetVirtualMachineInstance.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void getVirtualMachineInstance(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.virtualMachineInstances()
            .getWithResponse(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/Microsoft.HybridCompute/machines/DemoVM",
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
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * ListVirtualMachineInstances.json
     */
    /**
     * Sample code: ListVirtualMachineInstances.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void
        listVirtualMachineInstances(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.virtualMachineInstances()
            .list(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/Microsoft.HybridCompute/machines/DemoVM",
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
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * RestartVirtualMachineInstance.json
     */
    /**
     * Sample code: RestartVirtualMachine.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void restartVirtualMachine(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.virtualMachineInstances()
            .restart(
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
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * StartVirtualMachineInstance.json
     */
    /**
     * Sample code: StartVirtualMachine.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void startVirtualMachine(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.virtualMachineInstances()
            .start(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/Microsoft.HybridCompute/machines/DemoVM/providers/Microsoft.AzureStackHCI/virtualMachineInstances/default",
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
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * StopVirtualMachineInstance.json
     */
    /**
     * Sample code: StopVirtualMachine.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void stopVirtualMachine(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.virtualMachineInstances()
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

/**
 * Samples for VirtualMachineInstances Update.
 */
public final class VirtualMachineInstancesUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2024-01-01/examples/
     * UpdateVirtualMachineInstance.json
     */
    /**
     * Sample code: UpdateVirtualMachine.
     * 
     * @param manager Entry point to AzurestackhciManager.
     */
    public static void updateVirtualMachine(com.azure.resourcemanager.azurestackhci.AzurestackhciManager manager) {
        manager.virtualMachineInstances()
            .update(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/Microsoft.HybridCompute/machines/DemoVM",
                new VirtualMachineInstanceUpdateRequest().withProperties(
                    new VirtualMachineInstanceUpdateProperties().withStorageProfile(new StorageProfileUpdate()
                        .withDataDisks(Arrays.asList(new StorageProfileUpdateDataDisksItem().withId(
                            "/subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/testrg/providers/Microsoft.AzureStackHCI/virtualHardDisks/test-vhd"))))),
                com.azure.core.util.Context.NONE);
    }
}
```

