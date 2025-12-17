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
- [Reconcile](#arcsettings_reconcile)
- [Update](#arcsettings_update)

## Clusters

- [ChangeRing](#clusters_changering)
- [ConfigureRemoteSupport](#clusters_configureremotesupport)
- [Create](#clusters_create)
- [CreateIdentity](#clusters_createidentity)
- [Delete](#clusters_delete)
- [ExtendSoftwareAssuranceBenefit](#clusters_extendsoftwareassurancebenefit)
- [GetByResourceGroup](#clusters_getbyresourcegroup)
- [List](#clusters_list)
- [ListByResourceGroup](#clusters_listbyresourcegroup)
- [TriggerLogCollection](#clusters_triggerlogcollection)
- [Update](#clusters_update)
- [UpdateSecretsLocations](#clusters_updatesecretslocations)
- [UploadCertificate](#clusters_uploadcertificate)

## DeploymentSettings

- [CreateOrUpdate](#deploymentsettings_createorupdate)
- [Delete](#deploymentsettings_delete)
- [Get](#deploymentsettings_get)
- [ListByClusters](#deploymentsettings_listbyclusters)

## EdgeDeviceJobs

- [CreateOrUpdate](#edgedevicejobs_createorupdate)
- [Delete](#edgedevicejobs_delete)
- [Get](#edgedevicejobs_get)
- [ListByEdgeDevice](#edgedevicejobs_listbyedgedevice)

## EdgeDevices

- [CreateOrUpdate](#edgedevices_createorupdate)
- [Delete](#edgedevices_delete)
- [Get](#edgedevices_get)
- [List](#edgedevices_list)
- [Validate](#edgedevices_validate)

## EdgeMachineJobs

- [CreateOrUpdate](#edgemachinejobs_createorupdate)
- [Delete](#edgemachinejobs_delete)
- [Get](#edgemachinejobs_get)
- [List](#edgemachinejobs_list)

## EdgeMachines

- [CreateOrUpdate](#edgemachines_createorupdate)
- [Delete](#edgemachines_delete)
- [GetByResourceGroup](#edgemachines_getbyresourcegroup)
- [List](#edgemachines_list)
- [ListByResourceGroup](#edgemachines_listbyresourcegroup)
- [Update](#edgemachines_update)

## Extensions

- [Create](#extensions_create)
- [Delete](#extensions_delete)
- [Get](#extensions_get)
- [ListByArcSetting](#extensions_listbyarcsetting)
- [Update](#extensions_update)
- [Upgrade](#extensions_upgrade)

## KubernetesVersions

- [ListBySubscriptionLocationResource](#kubernetesversions_listbysubscriptionlocationresource)

## Offers

- [Get](#offers_get)
- [ListByCluster](#offers_listbycluster)
- [ListByPublisher](#offers_listbypublisher)

## Operations

- [List](#operations_list)

## OsImages

- [Get](#osimages_get)
- [ListBySubscriptionLocationResource](#osimages_listbysubscriptionlocationresource)

## OwnershipVouchers

- [Validate](#ownershipvouchers_validate)

## PlatformUpdates

- [Get](#platformupdates_get)
- [List](#platformupdates_list)

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

## UpdateContents

- [Get](#updatecontents_get)
- [List](#updatecontents_list)

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

## ValidatedSolutionRecipes

- [Get](#validatedsolutionrecipes_get)
- [ListBySubscriptionLocationResource](#validatedsolutionrecipes_listbysubscriptionlocationresource)
### ArcSettings_ConsentAndInstallDefaultExtensions

```java
/**
 * Samples for ArcSettings ConsentAndInstallDefaultExtensions.
 */
public final class ArcSettingsConsentAndInstallDefaultExtensionsSamples {
    /*
     * x-ms-original-file: 2025-12-01-preview/ConsentAndInstallDefaultExtensions.json
     */
    /**
     * Sample code: Consent And Install Default Extensions.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void
        consentAndInstallDefaultExtensions(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
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
     * x-ms-original-file: 2025-12-01-preview/PutArcSetting.json
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
/**
 * Samples for ArcSettings CreateIdentity.
 */
public final class ArcSettingsCreateIdentitySamples {
    /*
     * x-ms-original-file: 2025-12-01-preview/CreateArcIdentity.json
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
/**
 * Samples for ArcSettings Delete.
 */
public final class ArcSettingsDeleteSamples {
    /*
     * x-ms-original-file: 2025-12-01-preview/DeleteArcSetting.json
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
/**
 * Samples for ArcSettings GeneratePassword.
 */
public final class ArcSettingsGeneratePasswordSamples {
    /*
     * x-ms-original-file: 2025-12-01-preview/GeneratePassword.json
     */
    /**
     * Sample code: Generate Password.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void generatePassword(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
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
     * x-ms-original-file: 2025-12-01-preview/GetArcSetting.json
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
/**
 * Samples for ArcSettings InitializeDisableProcess.
 */
public final class ArcSettingsInitializeDisableProcessSamples {
    /*
     * x-ms-original-file: 2025-12-01-preview/InitializeDisableProcess.json
     */
    /**
     * Sample code: Trigger ARC Disable.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void triggerARCDisable(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
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
     * x-ms-original-file: 2025-12-01-preview/ListArcSettingsByCluster.json
     */
    /**
     * Sample code: List ArcSetting resources by HCI Cluster.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void
        listArcSettingResourcesByHCICluster(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.arcSettings().listByCluster("test-rg", "myCluster", com.azure.core.util.Context.NONE);
    }
}
```

### ArcSettings_Reconcile

```java
import com.azure.resourcemanager.azurestackhci.models.ReconcileArcSettingsRequest;
import com.azure.resourcemanager.azurestackhci.models.ReconcileArcSettingsRequestProperties;
import java.util.Arrays;

/**
 * Samples for ArcSettings Reconcile.
 */
public final class ArcSettingsReconcileSamples {
    /*
     * x-ms-original-file: 2025-12-01-preview/reconcileArcSettings.json
     */
    /**
     * Sample code: Reconcile Arc Settings.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void reconcileArcSettings(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.arcSettings()
            .reconcile("test-rg", "myCluster", "default",
                new ReconcileArcSettingsRequest()
                    .withProperties(new ReconcileArcSettingsRequestProperties().withClusterNodes(Arrays.asList(
                        "/subscriptions/sub1/resourceGroup/res1/providers/Microsoft.HybridCompute/machines/m1",
                        "/subscriptions/sub1/resourceGroup/res1/providers/Microsoft.HybridCompute/machines/m2"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### ArcSettings_Update

```java
import com.azure.resourcemanager.azurestackhci.models.ArcConnectivityProperties;
import com.azure.resourcemanager.azurestackhci.models.ArcSetting;
import com.azure.resourcemanager.azurestackhci.models.ServiceConfiguration;
import com.azure.resourcemanager.azurestackhci.models.ServiceName;
import java.util.Arrays;

/**
 * Samples for ArcSettings Update.
 */
public final class ArcSettingsUpdateSamples {
    /*
     * x-ms-original-file: 2025-12-01-preview/PatchArcSetting.json
     */
    /**
     * Sample code: Patch ArcSetting.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void patchArcSetting(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        ArcSetting resource = manager.arcSettings()
            .getWithResponse("test-rg", "myCluster", "default", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withConnectivityProperties(new ArcConnectivityProperties().withEnabled(true)
                .withServiceConfigurations(
                    Arrays.asList(new ServiceConfiguration().withServiceName(ServiceName.WAC).withPort(6516L))))
            .apply();
    }
}
```

### Clusters_ChangeRing

```java
import com.azure.resourcemanager.azurestackhci.models.ChangeRingRequest;
import com.azure.resourcemanager.azurestackhci.models.ChangeRingRequestProperties;

/**
 * Samples for Clusters ChangeRing.
 */
public final class ClustersChangeRingSamples {
    /*
     * x-ms-original-file: 2025-12-01-preview/ChangeClusterRing.json
     */
    /**
     * Sample code: Change cluster ring.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void changeClusterRing(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.clusters()
            .changeRing("test-rg", "myCluster",
                new ChangeRingRequest().withProperties(new ChangeRingRequestProperties().withTargetRing("Insider")),
                com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_ConfigureRemoteSupport

```java
import com.azure.resourcemanager.azurestackhci.models.RemoteSupportRequest;
import com.azure.resourcemanager.azurestackhci.models.RemoteSupportRequestProperties;
import com.azure.resourcemanager.azurestackhci.models.RemoteSupportType;
import java.time.OffsetDateTime;

/**
 * Samples for Clusters ConfigureRemoteSupport.
 */
public final class ClustersConfigureRemoteSupportSamples {
    /*
     * x-ms-original-file: 2025-12-01-preview/ConfigureRemoteSupport.json
     */
    /**
     * Sample code: Configure Remote Support.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void configureRemoteSupport(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.clusters()
            .configureRemoteSupport("test-rg", "mycluster",
                new RemoteSupportRequest().withProperties(new RemoteSupportRequestProperties()
                    .withExpirationTimestamp(OffsetDateTime.parse("2020-01-01T17:18:19.1234567Z"))
                    .withRemoteSupportType(RemoteSupportType.ENABLE)),
                com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2025-12-01-preview/CreateCluster.json
     */
    /**
     * Sample code: Create cluster.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void createCluster(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.clusters()
            .define("myCluster")
            .withRegion("East US")
            .withExistingResourceGroup("test-rg")
            .withCloudManagementEndpoint("https://98294836-31be-4668-aeae-698667faf99b.waconazure.com")
            .withAadClientId("24a6e53d-04e5-44d2-b7cc-1b732a847dfc")
            .withAadTenantId("7e589cc1-a8b6-4dff-91bd-5ec0fa18db94")
            .withTypeIdentityType(ManagedServiceIdentityType.SYSTEM_ASSIGNED)
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
     * x-ms-original-file: 2025-12-01-preview/CreateClusterIdentity.json
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
/**
 * Samples for Clusters Delete.
 */
public final class ClustersDeleteSamples {
    /*
     * x-ms-original-file: 2025-12-01-preview/DeleteCluster.json
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

/**
 * Samples for Clusters ExtendSoftwareAssuranceBenefit.
 */
public final class ClustersExtendSoftwareAssuranceBenefitSamples {
    /*
     * x-ms-original-file: 2025-12-01-preview/ExtendSoftwareAssuranceBenefit.json
     */
    /**
     * Sample code: Create cluster Identity.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void createClusterIdentity(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
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
     * x-ms-original-file: 2025-12-01-preview/GetCluster.json
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
/**
 * Samples for Clusters List.
 */
public final class ClustersListSamples {
    /*
     * x-ms-original-file: 2025-12-01-preview/ListClustersBySubscription.json
     */
    /**
     * Sample code: List clusters in a given subscription.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void
        listClustersInAGivenSubscription(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
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
     * x-ms-original-file: 2025-12-01-preview/ListClustersByResourceGroup.json
     */
    /**
     * Sample code: List clusters in a given resource group.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void
        listClustersInAGivenResourceGroup(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.clusters().listByResourceGroup("test-rg", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_TriggerLogCollection

```java
import com.azure.resourcemanager.azurestackhci.models.LogCollectionRequest;
import com.azure.resourcemanager.azurestackhci.models.LogCollectionRequestProperties;
import java.time.OffsetDateTime;

/**
 * Samples for Clusters TriggerLogCollection.
 */
public final class ClustersTriggerLogCollectionSamples {
    /*
     * x-ms-original-file: 2025-12-01-preview/TriggerLogCollection.json
     */
    /**
     * Sample code: Trigger Log Collection.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void triggerLogCollection(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.clusters()
            .triggerLogCollection("test-rg", "mycluster",
                new LogCollectionRequest().withProperties(new LogCollectionRequestProperties()
                    .withFromDate(OffsetDateTime.parse("2020-01-01T17:18:19.1234567Z"))
                    .withToDate(OffsetDateTime.parse("2021-01-01T17:18:19.1234567Z"))),
                com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2025-12-01-preview/UpdateCluster.json
     */
    /**
     * Sample code: Update cluster.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void updateCluster(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
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

### Clusters_UpdateSecretsLocations

```java
import com.azure.resourcemanager.azurestackhci.models.SecretsLocationDetails;
import com.azure.resourcemanager.azurestackhci.models.SecretsLocationsChangeRequest;
import com.azure.resourcemanager.azurestackhci.models.SecretsType;
import java.util.Arrays;

/**
 * Samples for Clusters UpdateSecretsLocations.
 */
public final class ClustersUpdateSecretsLocationsSamples {
    /*
     * x-ms-original-file: 2025-12-01-preview/Clusters_UpdateSecretsLocations.json
     */
    /**
     * Sample code: Update secrets locations for a Cluster.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void
        updateSecretsLocationsForACluster(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.clusters()
            .updateSecretsLocations("test-rg", "myCluster",
                new SecretsLocationsChangeRequest().withProperties(
                    Arrays.asList(new SecretsLocationDetails().withSecretsType(SecretsType.BACKUP_SECRETS)
                        .withSecretsLocation("fakeTokenPlaceholder"))),
                com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2025-12-01-preview/UploadCertificate.json
     */
    /**
     * Sample code: Upload certificate.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void uploadCertificate(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
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
import com.azure.resourcemanager.azurestackhci.models.DeploymentCluster;
import com.azure.resourcemanager.azurestackhci.models.DeploymentConfiguration;
import com.azure.resourcemanager.azurestackhci.models.DeploymentData;
import com.azure.resourcemanager.azurestackhci.models.DeploymentMode;
import com.azure.resourcemanager.azurestackhci.models.DeploymentSecuritySettings;
import com.azure.resourcemanager.azurestackhci.models.DeploymentSettingAdapterPropertyOverrides;
import com.azure.resourcemanager.azurestackhci.models.DeploymentSettingHostNetwork;
import com.azure.resourcemanager.azurestackhci.models.DeploymentSettingIntents;
import com.azure.resourcemanager.azurestackhci.models.DeploymentSettingStorageAdapterIpInfo;
import com.azure.resourcemanager.azurestackhci.models.DeploymentSettingStorageNetworks;
import com.azure.resourcemanager.azurestackhci.models.DeploymentSettingVirtualSwitchConfigurationOverrides;
import com.azure.resourcemanager.azurestackhci.models.DnsServerConfig;
import com.azure.resourcemanager.azurestackhci.models.DnsZones;
import com.azure.resourcemanager.azurestackhci.models.EceDeploymentSecrets;
import com.azure.resourcemanager.azurestackhci.models.EceSecrets;
import com.azure.resourcemanager.azurestackhci.models.IdentityProvider;
import com.azure.resourcemanager.azurestackhci.models.InfrastructureNetwork;
import com.azure.resourcemanager.azurestackhci.models.IpPools;
import com.azure.resourcemanager.azurestackhci.models.NetworkController;
import com.azure.resourcemanager.azurestackhci.models.Observability;
import com.azure.resourcemanager.azurestackhci.models.OperationType;
import com.azure.resourcemanager.azurestackhci.models.OptionalServices;
import com.azure.resourcemanager.azurestackhci.models.PhysicalNodes;
import com.azure.resourcemanager.azurestackhci.models.QosPolicyOverrides;
import com.azure.resourcemanager.azurestackhci.models.SbeCredentials;
import com.azure.resourcemanager.azurestackhci.models.SbeDeploymentInfo;
import com.azure.resourcemanager.azurestackhci.models.SbePartnerInfo;
import com.azure.resourcemanager.azurestackhci.models.SbePartnerProperties;
import com.azure.resourcemanager.azurestackhci.models.ScaleUnits;
import com.azure.resourcemanager.azurestackhci.models.SdnIntegration;
import com.azure.resourcemanager.azurestackhci.models.Storage;
import java.time.OffsetDateTime;
import java.util.Arrays;

/**
 * Samples for DeploymentSettings CreateOrUpdate.
 */
public final class DeploymentSettingsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-12-01-preview/PutDeploymentSettingsWithADLess.json
     */
    /**
     * Sample code: Create Deployment Settings Without Active Directory Integration.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void createDeploymentSettingsWithoutActiveDirectoryIntegration(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.deploymentSettings()
            .define("default")
            .withExistingCluster("test-rg", "myCluster")
            .withArcNodeResourceIds(Arrays.asList(
                "/subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/ArcInstance-rg/providers/Microsoft.HybridCompute/machines/Node-1",
                "/subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/ArcInstance-rg/providers/Microsoft.HybridCompute/machines/Node-2"))
            .withDeploymentMode(DeploymentMode.DEPLOY)
            .withOperationType(OperationType.CLUSTER_PROVISIONING)
            .withDeploymentConfiguration(
                new DeploymentConfiguration().withVersion("string")
                    .withScaleUnits(Arrays.asList(new ScaleUnits()
                        .withDeploymentData(new DeploymentData()
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
                            .withIdentityProvider(IdentityProvider.LOCAL_IDENTITY)
                            .withStorage(new Storage().withConfigurationMode("Express"))
                            .withNamingPrefix("ms169")
                            .withDomainFqdn("ASZ1PLab8.nttest.microsoft.com")
                            .withInfrastructureNetwork(Arrays.asList(new InfrastructureNetwork()
                                .withSubnetMask("255.255.248.0")
                                .withGateway("255.255.248.0")
                                .withIpPools(Arrays.asList(
                                    new IpPools().withStartingAddress("10.57.48.60").withEndingAddress("10.57.48.66")))
                                .withDnsZones(Arrays.asList(new DnsZones().withDnsZoneName("contoso.com")
                                    .withDnsForwarder(Arrays.asList("192.168.1.1"))))))
                            .withPhysicalNodes(Arrays.asList(
                                new PhysicalNodes().withName("ms169host").withIpv4Address("10.57.51.224"),
                                new PhysicalNodes().withName("ms154host").withIpv4Address("10.57.53.236")))
                            .withHostNetwork(new DeploymentSettingHostNetwork()
                                .withIntents(Arrays.asList(new DeploymentSettingIntents().withName("Compute_Management")
                                    .withTrafficType(Arrays.asList("Compute", "Management"))
                                    .withAdapter(Arrays.asList("Port2"))
                                    .withOverrideVirtualSwitchConfiguration(false)
                                    .withVirtualSwitchConfigurationOverrides(
                                        new DeploymentSettingVirtualSwitchConfigurationOverrides().withEnableIov("True")
                                            .withLoadBalancingAlgorithm("HyperVPort"))
                                    .withOverrideQosPolicy(false)
                                    .withQosPolicyOverrides(
                                        new QosPolicyOverrides().withPriorityValue8021ActionCluster("7")
                                            .withPriorityValue8021ActionSmb("3")
                                            .withBandwidthPercentageSmb("50"))
                                    .withOverrideAdapterProperty(false)
                                    .withAdapterPropertyOverrides(
                                        new DeploymentSettingAdapterPropertyOverrides().withJumboPacket("1514")
                                            .withNetworkDirect("Enabled")
                                            .withNetworkDirectTechnology("iWARP"))))
                                .withStorageNetworks(
                                    Arrays.asList(new DeploymentSettingStorageNetworks().withName("Storage1Network")
                                        .withNetworkAdapterName("Port3")
                                        .withVlanId("5")
                                        .withStorageAdapterIpInfo(Arrays.asList(
                                            new DeploymentSettingStorageAdapterIpInfo().withPhysicalNode("string")
                                                .withIpv4Address("10.57.48.60")
                                                .withSubnetMask("255.255.248.0")))))
                                .withStorageConnectivitySwitchless(true)
                                .withEnableStorageAutoIp(false))
                            .withSdnIntegration(new SdnIntegration().withNetworkController(
                                new NetworkController().withMacAddressPoolStart("00-0D-3A-1B-C7-21")
                                    .withMacAddressPoolStop("00-0D-3A-1B-C7-29")
                                    .withNetworkVirtualizationEnabled(true)))
                            .withAdouPath("OU=ms169,DC=ASZ1PLab8,DC=nttest,DC=microsoft,DC=com")
                            .withSecretsLocation("fakeTokenPlaceholder")
                            .withSecrets(Arrays.asList(
                                new EceDeploymentSecrets().withSecretName("fakeTokenPlaceholder")
                                    .withEceSecretName(EceSecrets.fromString("BMCAdminUserCred"))
                                    .withSecretLocation("fakeTokenPlaceholder"),
                                new EceDeploymentSecrets().withSecretName("fakeTokenPlaceholder")
                                    .withEceSecretName(EceSecrets.AZURE_STACK_LCMUSER_CREDENTIAL)
                                    .withSecretLocation("fakeTokenPlaceholder")))
                            .withOptionalServices(new OptionalServices().withCustomLocation("customLocationName")))
                        .withSbePartnerInfo(new SbePartnerInfo()
                            .withSbeDeploymentInfo(new SbeDeploymentInfo().withVersion("4.0.2309.13")
                                .withFamily("Gen5")
                                .withPublisher("Contoso")
                                .withSbeManifestSource("default")
                                .withSbeManifestCreationDate(OffsetDateTime.parse("2023-07-25T02:40:33Z")))
                            .withPartnerProperties(
                                Arrays.asList(new SbePartnerProperties().withName("EnableBMCIpV6").withValue("false"),
                                    new SbePartnerProperties().withName("PhoneHomePort").withValue("1653"),
                                    new SbePartnerProperties().withName("BMCSecurityState").withValue("HighSecurity")))
                            .withCredentialList(
                                Arrays.asList(new SbeCredentials().withSecretName("fakeTokenPlaceholder")
                                    .withEceSecretName("fakeTokenPlaceholder")
                                    .withSecretLocation("fakeTokenPlaceholder")))))))
            .create();
    }

    /*
     * x-ms-original-file: 2025-12-01-preview/PutDeploymentSettings.json
     */
    /**
     * Sample code: Create Deployment Settings.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void createDeploymentSettings(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.deploymentSettings()
            .define("default")
            .withExistingCluster("test-rg", "myCluster")
            .withArcNodeResourceIds(Arrays.asList(
                "/subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/ArcInstance-rg/providers/Microsoft.HybridCompute/machines/Node-1",
                "/subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/ArcInstance-rg/providers/Microsoft.HybridCompute/machines/Node-2"))
            .withDeploymentMode(DeploymentMode.DEPLOY)
            .withOperationType(OperationType.CLUSTER_PROVISIONING)
            .withDeploymentConfiguration(
                new DeploymentConfiguration().withVersion("string")
                    .withScaleUnits(Arrays.asList(new ScaleUnits()
                        .withDeploymentData(new DeploymentData()
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
                            .withInfrastructureNetwork(Arrays.asList(new InfrastructureNetwork()
                                .withSubnetMask("255.255.248.0")
                                .withGateway("255.255.248.0")
                                .withIpPools(Arrays.asList(
                                    new IpPools().withStartingAddress("10.57.48.60").withEndingAddress("10.57.48.66")))
                                .withDnsServerConfig(DnsServerConfig.USE_DNS_SERVER)
                                .withDnsServers(Arrays.asList("10.57.50.90"))))
                            .withPhysicalNodes(Arrays.asList(
                                new PhysicalNodes().withName("ms169host").withIpv4Address("10.57.51.224"),
                                new PhysicalNodes().withName("ms154host").withIpv4Address("10.57.53.236")))
                            .withHostNetwork(new DeploymentSettingHostNetwork()
                                .withIntents(Arrays.asList(new DeploymentSettingIntents().withName("Compute_Management")
                                    .withTrafficType(Arrays.asList("Compute", "Management"))
                                    .withAdapter(Arrays.asList("Port2"))
                                    .withOverrideVirtualSwitchConfiguration(false)
                                    .withVirtualSwitchConfigurationOverrides(
                                        new DeploymentSettingVirtualSwitchConfigurationOverrides().withEnableIov("True")
                                            .withLoadBalancingAlgorithm("HyperVPort"))
                                    .withOverrideQosPolicy(false)
                                    .withQosPolicyOverrides(
                                        new QosPolicyOverrides().withPriorityValue8021ActionCluster("7")
                                            .withPriorityValue8021ActionSmb("3")
                                            .withBandwidthPercentageSmb("50"))
                                    .withOverrideAdapterProperty(false)
                                    .withAdapterPropertyOverrides(
                                        new DeploymentSettingAdapterPropertyOverrides().withJumboPacket("1514")
                                            .withNetworkDirect("Enabled")
                                            .withNetworkDirectTechnology("iWARP"))))
                                .withStorageNetworks(
                                    Arrays.asList(new DeploymentSettingStorageNetworks().withName("Storage1Network")
                                        .withNetworkAdapterName("Port3")
                                        .withVlanId("5")
                                        .withStorageAdapterIpInfo(Arrays.asList(
                                            new DeploymentSettingStorageAdapterIpInfo().withPhysicalNode("string")
                                                .withIpv4Address("10.57.48.60")
                                                .withSubnetMask("255.255.248.0")))))
                                .withStorageConnectivitySwitchless(true)
                                .withEnableStorageAutoIp(false))
                            .withSdnIntegration(new SdnIntegration().withNetworkController(
                                new NetworkController().withMacAddressPoolStart("00-0D-3A-1B-C7-21")
                                    .withMacAddressPoolStop("00-0D-3A-1B-C7-29")
                                    .withNetworkVirtualizationEnabled(true)))
                            .withIsManagementCluster(true)
                            .withAdouPath("OU=ms169,DC=ASZ1PLab8,DC=nttest,DC=microsoft,DC=com")
                            .withSecretsLocation("fakeTokenPlaceholder")
                            .withSecrets(Arrays.asList(
                                new EceDeploymentSecrets().withSecretName("fakeTokenPlaceholder")
                                    .withEceSecretName(EceSecrets.fromString("BMCAdminUserCred"))
                                    .withSecretLocation("fakeTokenPlaceholder"),
                                new EceDeploymentSecrets().withSecretName("fakeTokenPlaceholder")
                                    .withEceSecretName(EceSecrets.AZURE_STACK_LCMUSER_CREDENTIAL)
                                    .withSecretLocation("fakeTokenPlaceholder")))
                            .withOptionalServices(new OptionalServices().withCustomLocation("customLocationName")))
                        .withSbePartnerInfo(new SbePartnerInfo()
                            .withSbeDeploymentInfo(new SbeDeploymentInfo().withVersion("4.0.2309.13")
                                .withFamily("Gen5")
                                .withPublisher("Contoso")
                                .withSbeManifestSource("default")
                                .withSbeManifestCreationDate(OffsetDateTime.parse("2023-07-25T02:40:33Z")))
                            .withPartnerProperties(
                                Arrays.asList(new SbePartnerProperties().withName("EnableBMCIpV6").withValue("false"),
                                    new SbePartnerProperties().withName("PhoneHomePort").withValue("1653"),
                                    new SbePartnerProperties().withName("BMCSecurityState").withValue("HighSecurity")))
                            .withCredentialList(
                                Arrays.asList(new SbeCredentials().withSecretName("fakeTokenPlaceholder")
                                    .withEceSecretName("fakeTokenPlaceholder")
                                    .withSecretLocation("fakeTokenPlaceholder")))))))
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
     * x-ms-original-file: 2025-12-01-preview/DeleteDeploymentSettings.json
     */
    /**
     * Sample code: Delete Deployment Settings.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void deleteDeploymentSettings(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
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
     * x-ms-original-file: 2025-12-01-preview/GetDeploymentSettings.json
     */
    /**
     * Sample code: Get Deployment Settings.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void getDeploymentSettings(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
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
     * x-ms-original-file: 2025-12-01-preview/ListDeploymentSettingsByCluster.json
     */
    /**
     * Sample code: List Deployment Settings.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void listDeploymentSettings(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.deploymentSettings().listByClusters("test-rg", "myCluster", com.azure.core.util.Context.NONE);
    }
}
```

### EdgeDeviceJobs_CreateOrUpdate

```java
import com.azure.resourcemanager.azurestackhci.models.HciCollectLogJobProperties;
import com.azure.resourcemanager.azurestackhci.models.HciEdgeDeviceJob;
import com.azure.resourcemanager.azurestackhci.models.HciRemoteSupportJobProperties;
import com.azure.resourcemanager.azurestackhci.models.RemoteSupportAccessLevel;
import com.azure.resourcemanager.azurestackhci.models.RemoteSupportType;
import java.time.OffsetDateTime;

/**
 * Samples for EdgeDeviceJobs CreateOrUpdate.
 */
public final class EdgeDeviceJobsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-12-01-preview/EdgeDeviceJobs_CreateOrUpdate_RemoteSupport.json
     */
    /**
     * Sample code: EdgeDeviceJobs_CreateOrUpdate_RemoteSupport.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void edgeDeviceJobsCreateOrUpdateRemoteSupport(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.edgeDeviceJobs()
            .createOrUpdate(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/ArcInstance-rg/providers/Microsoft.HybridCompute/machines/Node-1",
                "default", "collectLog",
                new HciEdgeDeviceJob().withProperties(
                    new HciRemoteSupportJobProperties().withAccessLevel(RemoteSupportAccessLevel.DIAGNOSTICS)
                        .withExpirationTimestamp(OffsetDateTime.parse("2024-01-29T10:43:27.9471574Z"))
                        .withType(RemoteSupportType.ENABLE)),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-12-01-preview/EdgeDeviceJobs_CreateOrUpdate_CollectLog.json
     */
    /**
     * Sample code: EdgeDeviceJobs_CreateOrUpdate_CollectLog.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void
        edgeDeviceJobsCreateOrUpdateCollectLog(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.edgeDeviceJobs()
            .createOrUpdate(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/ArcInstance-rg/providers/Microsoft.HybridCompute/machines/Node-1",
                "default", "collectLog",
                new HciEdgeDeviceJob().withProperties(
                    new HciCollectLogJobProperties().withFromDate(OffsetDateTime.parse("2024-01-29T10:43:27.9471574Z"))
                        .withToDate(OffsetDateTime.parse("2024-01-29T10:43:27.9471574Z"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### EdgeDeviceJobs_Delete

```java
/**
 * Samples for EdgeDeviceJobs Delete.
 */
public final class EdgeDeviceJobsDeleteSamples {
    /*
     * x-ms-original-file: 2025-12-01-preview/EdgeDeviceJobs_Delete.json
     */
    /**
     * Sample code: EdgeDeviceJobs_Delete.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void edgeDeviceJobsDelete(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.edgeDeviceJobs()
            .delete(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/ArcInstance-rg/providers/Microsoft.HybridCompute/machines/Node-1",
                "lAq", "Ihlm3R-bZ4vTC4ABA456", com.azure.core.util.Context.NONE);
    }
}
```

### EdgeDeviceJobs_Get

```java
/**
 * Samples for EdgeDeviceJobs Get.
 */
public final class EdgeDeviceJobsGetSamples {
    /*
     * x-ms-original-file: 2025-12-01-preview/EdgeDeviceJobs_Get_RemoteSupport.json
     */
    /**
     * Sample code: EdgeDeviceJobs_Get_RemoteSupport.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void
        edgeDeviceJobsGetRemoteSupport(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.edgeDeviceJobs()
            .getWithResponse(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/ArcInstance-rg/providers/Microsoft.HybridCompute/machines/Node-1",
                "-5M1G7G10OZ-o5b-HS3-c72", "-oUxs", com.azure.core.util.Context.NONE);
    }
}
```

### EdgeDeviceJobs_ListByEdgeDevice

```java
/**
 * Samples for EdgeDeviceJobs ListByEdgeDevice.
 */
public final class EdgeDeviceJobsListByEdgeDeviceSamples {
    /*
     * x-ms-original-file: 2025-12-01-preview/EdgeDeviceJobs_ListByEdgeDevice.json
     */
    /**
     * Sample code: EdgeDeviceJobs_ListByEdgeDevice.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void
        edgeDeviceJobsListByEdgeDevice(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.edgeDeviceJobs()
            .listByEdgeDevice(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/ArcInstance-rg/providers/Microsoft.HybridCompute/machines/Node-1",
                "YE-855IEIN585-", com.azure.core.util.Context.NONE);
    }
}
```

### EdgeDevices_CreateOrUpdate

```java
import com.azure.resourcemanager.azurestackhci.models.DeviceConfiguration;
import com.azure.resourcemanager.azurestackhci.models.HciEdgeDevice;
import com.azure.resourcemanager.azurestackhci.models.HciEdgeDeviceProperties;
import com.azure.resourcemanager.azurestackhci.models.NicDetail;
import java.util.Arrays;

/**
 * Samples for EdgeDevices CreateOrUpdate.
 */
public final class EdgeDevicesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-12-01-preview/CreateHciEdgeDevice.json
     */
    /**
     * Sample code: Create HCI Edge Device.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void createHCIEdgeDevice(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.edgeDevices()
            .createOrUpdate(
                "subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/ArcInstance-rg/providers/Microsoft.HybridCompute/machines/Node-1",
                "default",
                new HciEdgeDevice().withProperties(new HciEdgeDeviceProperties().withDeviceConfiguration(
                    new DeviceConfiguration().withNicDetails(Arrays.asList(new NicDetail().withAdapterName("ethernet")
                        .withInterfaceDescription("NDIS 6.70 ")
                        .withComponentId("VMBUS{f8615163-df3e-46c5-913f-f2d2f965ed0g} ")
                        .withDriverVersion("10.0.20348.1547 ")
                        .withIp4Address("10.10.10.10")
                        .withSubnetMask("255.255.255.0")
                        .withDefaultGateway("10.10.10.1")
                        .withDnsServers(Arrays.asList("100.10.10.1"))
                        .withDefaultIsolationId("0"))).withDeviceMetadata(""))),
                com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2025-12-01-preview/DeleteEdgeDevices.json
     */
    /**
     * Sample code: Delete Edge Devices.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void deleteEdgeDevices(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
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
     * x-ms-original-file: 2025-12-01-preview/GetEdgeDevices.json
     */
    /**
     * Sample code: Get Edge Device.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void getEdgeDevice(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
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
     * x-ms-original-file: 2025-12-01-preview/ListEdgeDevices.json
     */
    /**
     * Sample code: List Edge Devices.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void listEdgeDevices(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
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
     * x-ms-original-file: 2025-12-01-preview/ValidateEdgeDevices.json
     */
    /**
     * Sample code: Validate Edge Devices.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void validateEdgeDevices(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
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

### EdgeMachineJobs_CreateOrUpdate

```java
import com.azure.resourcemanager.azurestackhci.models.DeploymentMode;
import com.azure.resourcemanager.azurestackhci.models.DownloadOsJobProperties;
import com.azure.resourcemanager.azurestackhci.models.DownloadOsProfile;
import com.azure.resourcemanager.azurestackhci.models.DownloadRequest;
import com.azure.resourcemanager.azurestackhci.models.EdgeMachineJobProperties;
import com.azure.resourcemanager.azurestackhci.models.EdgeMachineRemoteSupportJobProperties;
import com.azure.resourcemanager.azurestackhci.models.IpAddressRange;
import com.azure.resourcemanager.azurestackhci.models.IpAssignmentType;
import com.azure.resourcemanager.azurestackhci.models.NetworkAdapter;
import com.azure.resourcemanager.azurestackhci.models.NetworkConfiguration;
import com.azure.resourcemanager.azurestackhci.models.OSOperationType;
import com.azure.resourcemanager.azurestackhci.models.OnboardingConfiguration;
import com.azure.resourcemanager.azurestackhci.models.OnboardingResourceType;
import com.azure.resourcemanager.azurestackhci.models.OsProvisionProfile;
import com.azure.resourcemanager.azurestackhci.models.ProvisionOsJobProperties;
import com.azure.resourcemanager.azurestackhci.models.ProvisioningOsType;
import com.azure.resourcemanager.azurestackhci.models.ProvisioningRequest;
import com.azure.resourcemanager.azurestackhci.models.RemoteSupportAccessLevel;
import com.azure.resourcemanager.azurestackhci.models.RemoteSupportType;
import com.azure.resourcemanager.azurestackhci.models.SecretType;
import com.azure.resourcemanager.azurestackhci.models.StorageConfiguration;
import com.azure.resourcemanager.azurestackhci.models.TargetDeviceConfiguration;
import com.azure.resourcemanager.azurestackhci.models.TimeConfiguration;
import com.azure.resourcemanager.azurestackhci.models.UserDetails;
import com.azure.resourcemanager.azurestackhci.models.WebProxyConfiguration;
import java.time.OffsetDateTime;
import java.util.Arrays;

/**
 * Samples for EdgeMachineJobs CreateOrUpdate.
 */
public final class EdgeMachineJobsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-12-01-preview/EdgeMachineJobs_CreateOrUpdate_DownloadOs.json
     */
    /**
     * Sample code: EdgeMachineJobs_CreateOrUpdate_DownloadOs.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void
        edgeMachineJobsCreateOrUpdateDownloadOs(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.edgeMachineJobs()
            .define("DownloadOs")
            .withExistingEdgeMachine("ArcInstance-rg", "machine1")
            .withProperties(new DownloadOsJobProperties().withDeploymentMode(DeploymentMode.DEPLOY)
                .withDownloadRequest(new DownloadRequest().withTarget(ProvisioningOsType.AZURE_LINUX)
                    .withOsProfile(new DownloadOsProfile().withOsName("AzureLinux")
                        .withOsType("AzureLinux")
                        .withOsVersion("3.0")
                        .withOsImageLocation("https://aka.ms/aep/azlinux3.0")
                        .withVsrVersion("1.0.0")
                        .withImageHash("sha256:a8b1c2d3e4f5a6b7c8d9e0f1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1")
                        .withGpgPubKey("fakeTokenPlaceholder"))))
            .create();
    }

    /*
     * x-ms-original-file: 2025-12-01-preview/EdgeMachineJobs_CreateOrUpdate_ProvisionOs.json
     */
    /**
     * Sample code: EdgeMachineJobs_CreateOrUpdate_ProvisionOs.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void
        edgeMachineJobsCreateOrUpdateProvisionOs(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.edgeMachineJobs()
            .define("ProvisionOs")
            .withExistingEdgeMachine("ArcInstance-rg", "machine1")
            .withProperties(new ProvisionOsJobProperties().withDeploymentMode(DeploymentMode.DEPLOY)
                .withProvisioningRequest(new ProvisioningRequest().withTarget(ProvisioningOsType.AZURE_LINUX)
                    .withOsProfile(new OsProvisionProfile().withOsName("AzureLinux")
                        .withOsType("AzureLinux")
                        .withOsVersion("3.0")
                        .withOsImageLocation("https://aka.ms/aep/azlinux3.0")
                        .withVsrVersion("1.0.0")
                        .withImageHash("sha256:a1b2c3d4e5f6789012345678901234567890abcdef1234567890abcdef123456")
                        .withGpgPubKey("fakeTokenPlaceholder")
                        .withOperationType(OSOperationType.PROVISION))
                    .withUserDetails(Arrays.asList(new UserDetails().withUserName("edgeuser")
                        .withSecretType(SecretType.KEY_VAULT)
                        .withSecretLocation("fakeTokenPlaceholder")
                        .withSshPubKey(
                            Arrays.asList("ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQC7... edgeuser@example.com"))))
                    .withOnboardingConfiguration(new OnboardingConfiguration()
                        .withType(OnboardingResourceType.HYBRID_COMPUTE_MACHINE)
                        .withResourceId(
                            "/subscriptions/ff0aa6da-20f8-44fe-9aee-381c8e8a4aeb/resourceGroups/bhukumar-test-rg/providers/Microsoft.HybridCompute/machines/bkumar-t1")
                        .withLocation("eastus")
                        .withTenantId("72f988bf-86f1-41af-91ab-2d7cd011db47")
                        .withArcVirtualMachineId("634b9db8-83e1-46ed-b391-c1614e2d0097"))
                    .withDeviceConfiguration(new TargetDeviceConfiguration()
                        .withNetwork(new NetworkConfiguration().withNetworkAdapters(
                            Arrays.asList(new NetworkAdapter().withIpAssignmentType(IpAssignmentType.AUTOMATIC)
                                .withIpAddress("")
                                .withIpAddressRange(new IpAddressRange().withStartIp("").withEndIp(""))
                                .withGateway("")
                                .withSubnetMask("")
                                .withDnsAddressArray(Arrays.asList("8.8.8.8"))
                                .withVlanId("0"))))
                        .withHostName("634b9db8-83e1-46ed-b391-c1614e2d0097")
                        .withWebProxy(new WebProxyConfiguration().withConnectionUri("https://microsoft.com/a")
                            .withPort("")
                            .withBypassList(Arrays.asList()))
                        .withTime(new TimeConfiguration().withPrimaryTimeServer("")
                            .withSecondaryTimeServer("")
                            .withTimeZone("UTC"))
                        .withStorage(new StorageConfiguration().withPartitionSize("30GB")))
                    .withCustomConfiguration("eyJjdXN0b21Db25maWciOiAiZXhhbXBsZSBiYXNlNjQgZW5jb2RlZCBjb25maWcifQ==")))
            .create();
    }

    /*
     * x-ms-original-file: 2025-12-01-preview/EdgeMachineJobs_CreateOrUpdate_UpdateOs.json
     */
    /**
     * Sample code: EdgeMachineJobs_CreateOrUpdate_UpdateOs.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void
        edgeMachineJobsCreateOrUpdateUpdateOs(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.edgeMachineJobs()
            .define("UpdateOs")
            .withExistingEdgeMachine("ArcInstance-rg", "machine1")
            .withProperties(new ProvisionOsJobProperties().withDeploymentMode(DeploymentMode.DEPLOY)
                .withProvisioningRequest(new ProvisioningRequest().withTarget(ProvisioningOsType.AZURE_LINUX)
                    .withOsProfile(new OsProvisionProfile().withOsName("AzureLinux")
                        .withOsType("AzureLinux")
                        .withOsVersion("3.1")
                        .withOsImageLocation("https://aka.ms/aep/azlinux3.1")
                        .withVsrVersion("1.1.0")
                        .withImageHash("sha256:b2c3d4e5f6789012345678901234567890abcdef1234567890abcdef12345678")
                        .withGpgPubKey("fakeTokenPlaceholder")
                        .withOperationType(OSOperationType.UPDATE))
                    .withUserDetails(Arrays.asList(new UserDetails().withUserName("edgeuser")
                        .withSecretType(SecretType.KEY_VAULT)
                        .withSecretLocation("fakeTokenPlaceholder")
                        .withSshPubKey(
                            Arrays.asList("ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQC7... edgeuser@example.com"))))
                    .withOnboardingConfiguration(new OnboardingConfiguration()
                        .withType(OnboardingResourceType.HYBRID_COMPUTE_MACHINE)
                        .withResourceId(
                            "/subscriptions/ff0aa6da-20f8-44fe-9aee-381c8e8a4aeb/resourceGroups/bhukumar-test-rg/providers/Microsoft.HybridCompute/machines/bkumar-t1")
                        .withLocation("eastus")
                        .withTenantId("72f988bf-86f1-41af-91ab-2d7cd011db47")
                        .withArcVirtualMachineId("634b9db8-83e1-46ed-b391-c1614e2d0097"))
                    .withDeviceConfiguration(new TargetDeviceConfiguration()
                        .withNetwork(new NetworkConfiguration().withNetworkAdapters(
                            Arrays.asList(new NetworkAdapter().withIpAssignmentType(IpAssignmentType.AUTOMATIC)
                                .withIpAddress("")
                                .withIpAddressRange(new IpAddressRange().withStartIp("").withEndIp(""))
                                .withGateway("")
                                .withSubnetMask("")
                                .withDnsAddressArray(Arrays.asList("8.8.8.8"))
                                .withVlanId("0"))))
                        .withHostName("634b9db8-83e1-46ed-b391-c1614e2d0097")
                        .withWebProxy(new WebProxyConfiguration().withConnectionUri("https://microsoft.com/a")
                            .withPort("")
                            .withBypassList(Arrays.asList()))
                        .withTime(new TimeConfiguration().withPrimaryTimeServer("")
                            .withSecondaryTimeServer("")
                            .withTimeZone("UTC")))
                    .withCustomConfiguration("eyJjdXN0b21Db25maWciOiAiZXhhbXBsZSBiYXNlNjQgZW5jb2RlZCBjb25maWcifQ==")))
            .create();
    }

    /*
     * x-ms-original-file: 2025-12-01-preview/EdgeMachineJobs_CreateOrUpdate_CollectLog.json
     */
    /**
     * Sample code: EdgeMachineJobs_CreateOrUpdate_CollectLog.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void
        edgeMachineJobsCreateOrUpdateCollectLog(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.edgeMachineJobs()
            .define("triggerLogCollection")
            .withExistingEdgeMachine("ArcInstance-rg", "machine1")
            .withProperties(new EdgeMachineJobProperties().withDeploymentMode(DeploymentMode.VALIDATE))
            .create();
    }

    /*
     * x-ms-original-file: 2025-12-01-preview/EdgeMachineJobs_CreateOrUpdate_RemoteSupport.json
     */
    /**
     * Sample code: EdgeMachineJobs_CreateOrUpdate_RemoteSupport.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void edgeMachineJobsCreateOrUpdateRemoteSupport(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.edgeMachineJobs()
            .define("RemoteSupport")
            .withExistingEdgeMachine("ArcInstance-rg", "machine1")
            .withProperties(
                new EdgeMachineRemoteSupportJobProperties().withAccessLevel(RemoteSupportAccessLevel.DIAGNOSTICS)
                    .withExpirationTimestamp(OffsetDateTime.parse("2024-01-29T10:43:27.9471574Z"))
                    .withType(RemoteSupportType.ENABLE))
            .create();
    }
}
```

### EdgeMachineJobs_Delete

```java
/**
 * Samples for EdgeMachineJobs Delete.
 */
public final class EdgeMachineJobsDeleteSamples {
    /*
     * x-ms-original-file: 2025-12-01-preview/EdgeMachineJobs_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: EdgeMachineJobs_Delete_MaximumSet.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void
        edgeMachineJobsDeleteMaximumSet(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.edgeMachineJobs()
            .delete("ArcInstance-rg", "machine1", "triggerLogCollection", com.azure.core.util.Context.NONE);
    }
}
```

### EdgeMachineJobs_Get

```java
/**
 * Samples for EdgeMachineJobs Get.
 */
public final class EdgeMachineJobsGetSamples {
    /*
     * x-ms-original-file: 2025-12-01-preview/EdgeMachineJobs_Get_ProvisionOs.json
     */
    /**
     * Sample code: EdgeMachineJobs_Get_ProvisionOs.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void
        edgeMachineJobsGetProvisionOs(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.edgeMachineJobs()
            .getWithResponse("ArcInstance-rg", "machine1", "ProvisionOs", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-12-01-preview/EdgeMachineJobs_Get_CollectLog.json
     */
    /**
     * Sample code: EdgeMachineJobs_Get_CollectLog.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void
        edgeMachineJobsGetCollectLog(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.edgeMachineJobs()
            .getWithResponse("ArcInstance-rg", "machine1", "collectLog", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-12-01-preview/EdgeMachineJobs_Get_RemoteSupport.json
     */
    /**
     * Sample code: EdgeMachineJobs_Get_RemoteSupport.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void
        edgeMachineJobsGetRemoteSupport(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.edgeMachineJobs()
            .getWithResponse("ArcInstance-rg", "machine1", "RemoteSupport", com.azure.core.util.Context.NONE);
    }
}
```

### EdgeMachineJobs_List

```java
/**
 * Samples for EdgeMachineJobs List.
 */
public final class EdgeMachineJobsListSamples {
    /*
     * x-ms-original-file: 2025-12-01-preview/EdgeMachineJobs_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: EdgeMachineJobs_List_MaximumSet.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void
        edgeMachineJobsListMaximumSet(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.edgeMachineJobs().list("ArcInstance-rg", "machine1", com.azure.core.util.Context.NONE);
    }
}
```

### EdgeMachines_CreateOrUpdate

```java
import com.azure.resourcemanager.azurestackhci.models.EdgeMachineProperties;

/**
 * Samples for EdgeMachines CreateOrUpdate.
 */
public final class EdgeMachinesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-12-01-preview/EdgeMachines_CreateOrUpdate.json
     */
    /**
     * Sample code: EdgeMachines_CreateOrUpdate.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void
        edgeMachinesCreateOrUpdate(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.edgeMachines()
            .define("machine-1")
            .withRegion("eastus")
            .withExistingResourceGroup("ArcInstance-rg")
            .withProperties(new EdgeMachineProperties()
                .withArcMachineResourceGroupId(
                    "/subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/ArcInstance-rg")
                .withArcMachineResourceId(
                    "/subscriptions/fd3c3665-1729-4b7b-9a38-238e83b0f98b/resourceGroups/ArcInstance-rg/providers/Microsoft.HybridCompute/machines/Node-1"))
            .create();
    }
}
```

### EdgeMachines_Delete

```java
/**
 * Samples for EdgeMachines Delete.
 */
public final class EdgeMachinesDeleteSamples {
    /*
     * x-ms-original-file: 2025-12-01-preview/EdgeMachines_Delete.json
     */
    /**
     * Sample code: EdgeMachines_Delete_MaximumSet.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void
        edgeMachinesDeleteMaximumSet(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.edgeMachines().delete("ArcInstance-rg", "machine-1", com.azure.core.util.Context.NONE);
    }
}
```

### EdgeMachines_GetByResourceGroup

```java
/**
 * Samples for EdgeMachines GetByResourceGroup.
 */
public final class EdgeMachinesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-12-01-preview/EdgeMachines_Get.json
     */
    /**
     * Sample code: EdgeMachines_Get_MaximumSet.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void edgeMachinesGetMaximumSet(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.edgeMachines()
            .getByResourceGroupWithResponse("ArcInstance-rg", "machine-1", com.azure.core.util.Context.NONE);
    }
}
```

### EdgeMachines_List

```java
/**
 * Samples for EdgeMachines List.
 */
public final class EdgeMachinesListSamples {
    /*
     * x-ms-original-file: 2025-12-01-preview/EdgeMachines_ListBySubscription.json
     */
    /**
     * Sample code: List edge machines in a given subscription.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void
        listEdgeMachinesInAGivenSubscription(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.edgeMachines().list(com.azure.core.util.Context.NONE);
    }
}
```

### EdgeMachines_ListByResourceGroup

```java
/**
 * Samples for EdgeMachines ListByResourceGroup.
 */
public final class EdgeMachinesListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-12-01-preview/EdgeMachines_ListByResourceGroup.json
     */
    /**
     * Sample code: List edge machines in a given resource group.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void
        listEdgeMachinesInAGivenResourceGroup(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.edgeMachines().listByResourceGroup("ArcInstance-rg", com.azure.core.util.Context.NONE);
    }
}
```

### EdgeMachines_Update

```java
import com.azure.resourcemanager.azurestackhci.models.EdgeMachine;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for EdgeMachines Update.
 */
public final class EdgeMachinesUpdateSamples {
    /*
     * x-ms-original-file: 2025-12-01-preview/EdgeMachines_Update.json
     */
    /**
     * Sample code: EdgeMachines_Update.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void edgeMachinesUpdate(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        EdgeMachine resource = manager.edgeMachines()
            .getByResourceGroupWithResponse("ArcInstance-rg", "machine-1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("key2335", "fakeTokenPlaceholder")).apply();
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
     * x-ms-original-file: 2025-12-01-preview/PutExtension.json
     */
    /**
     * Sample code: Create Arc Extension.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void createArcExtension(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager)
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
     * x-ms-original-file: 2025-12-01-preview/DeleteExtension.json
     */
    /**
     * Sample code: Delete Arc Extension.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void deleteArcExtension(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
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
     * x-ms-original-file: 2025-12-01-preview/GetExtension.json
     */
    /**
     * Sample code: Get ArcSettings Extension.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void getArcSettingsExtension(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
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
     * x-ms-original-file: 2025-12-01-preview/ListExtensionsByArcSetting.json
     */
    /**
     * Sample code: List Extensions under ArcSetting resource.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void
        listExtensionsUnderArcSettingResource(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
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
     * x-ms-original-file: 2025-12-01-preview/PatchExtension.json
     */
    /**
     * Sample code: Update Arc Extension.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void updateArcExtension(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager)
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
     * x-ms-original-file: 2025-12-01-preview/Extensions_Upgrade.json
     */
    /**
     * Sample code: Upgrade Machine Extensions.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void upgradeMachineExtensions(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.extensions()
            .upgrade("test-rg", "myCluster", "default", "MicrosoftMonitoringAgent",
                new ExtensionUpgradeParameters().withTargetVersion("1.0.18062.0"), com.azure.core.util.Context.NONE);
    }
}
```

### KubernetesVersions_ListBySubscriptionLocationResource

```java
/**
 * Samples for KubernetesVersions ListBySubscriptionLocationResource.
 */
public final class KubernetesVersionsListBySubscriptionLocationResourceSamples {
    /*
     * x-ms-original-file: 2025-12-01-preview/KubernetesVersions_ListBySubscriptionLocationResource_MaximumSet_Gen.json
     */
    /**
     * Sample code: KubernetesVersions_ListBySubscriptionLocationResource_MaximumSet.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void kubernetesVersionsListBySubscriptionLocationResourceMaximumSet(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.kubernetesVersions().listBySubscriptionLocationResource("westus2", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2025-12-01-preview/GetOffer.json
     */
    /**
     * Sample code: Get Offer.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void getOffer(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
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
     * x-ms-original-file: 2025-12-01-preview/ListOffersByCluster.json
     */
    /**
     * Sample code: List Offer resources by HCI Cluster.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void
        listOfferResourcesByHCICluster(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
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
     * x-ms-original-file: 2025-12-01-preview/ListOffersByPublisher.json
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
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2025-12-01-preview/Operations_List.json
     */
    /**
     * Sample code: List the operations for the provider.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void
        listTheOperationsForTheProvider(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### OsImages_Get

```java
/**
 * Samples for OsImages Get.
 */
public final class OsImagesGetSamples {
    /*
     * x-ms-original-file: 2025-12-01-preview/OsImages_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: OsImages_Get_MaximumSet.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void osImagesGetMaximumSet(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.osImages().getWithResponse("arowdcr", "10.2408.0.1", com.azure.core.util.Context.NONE);
    }
}
```

### OsImages_ListBySubscriptionLocationResource

```java
/**
 * Samples for OsImages ListBySubscriptionLocationResource.
 */
public final class OsImagesListBySubscriptionLocationResourceSamples {
    /*
     * x-ms-original-file: 2025-12-01-preview/OsImages_ListBySubscriptionLocationResource_MaximumSet_Gen.json
     */
    /**
     * Sample code: OsImages_ListBySubscriptionLocationResource_MaximumSet.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void osImagesListBySubscriptionLocationResourceMaximumSet(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.osImages().listBySubscriptionLocationResource("westus2", com.azure.core.util.Context.NONE);
    }
}
```

### OwnershipVouchers_Validate

```java
import com.azure.resourcemanager.azurestackhci.models.OwnerKeyType;
import com.azure.resourcemanager.azurestackhci.models.OwnershipVoucherDetails;
import com.azure.resourcemanager.azurestackhci.models.ValidateOwnershipVouchersRequest;
import java.util.Arrays;

/**
 * Samples for OwnershipVouchers Validate.
 */
public final class OwnershipVouchersValidateSamples {
    /*
     * x-ms-original-file: 2025-12-01-preview/ValidateOwnershipVouchers_ByResourceGroup.json
     */
    /**
     * Sample code: Validate ownership vouchers in a given resource group.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void validateOwnershipVouchersInAGivenResourceGroup(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.ownershipVouchers()
            .validateWithResponse("ArcInstance-rg", "westus",
                new ValidateOwnershipVouchersRequest().withOwnershipVoucherDetails(
                    Arrays.asList(new OwnershipVoucherDetails().withOwnershipVoucher("Device Model Ownership content")
                        .withOwnerKeyType(OwnerKeyType.MICROSOFT_MANAGED))),
                com.azure.core.util.Context.NONE);
    }
}
```

### PlatformUpdates_Get

```java
/**
 * Samples for PlatformUpdates Get.
 */
public final class PlatformUpdatesGetSamples {
    /*
     * x-ms-original-file: 2025-12-01-preview/PlatformUpdates_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: PlatformUpdates_Get_MaximumSet.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void
        platformUpdatesGetMaximumSet(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.platformUpdates().getWithResponse("westus2", "10.2408.0.1", com.azure.core.util.Context.NONE);
    }
}
```

### PlatformUpdates_List

```java
/**
 * Samples for PlatformUpdates List.
 */
public final class PlatformUpdatesListSamples {
    /*
     * x-ms-original-file: 2025-12-01-preview/PlatformUpdates_ListByLocation_MaximumSet_Gen.json
     */
    /**
     * Sample code: PlatformUpdates_ListByLocation_MaximumSet.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void
        platformUpdatesListByLocationMaximumSet(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.platformUpdates().list("westus2", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2025-12-01-preview/GetPublisher.json
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
/**
 * Samples for Publishers ListByCluster.
 */
public final class PublishersListByClusterSamples {
    /*
     * x-ms-original-file: 2025-12-01-preview/ListPublishersByCluster.json
     */
    /**
     * Sample code: List Publisher resources by HCI Cluster.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void
        listPublisherResourcesByHCICluster(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
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
     * x-ms-original-file: 2025-12-01-preview/PutSecuritySettings.json
     */
    /**
     * Sample code: Create Security Settings.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void createSecuritySettings(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.securitySettings()
            .define("default")
            .withExistingCluster("test-rg", "myCluster")
            .withSecuredCoreComplianceAssignment(ComplianceAssignmentType.AUDIT)
            .withWdacComplianceAssignment(ComplianceAssignmentType.APPLY_AND_AUTO_CORRECT)
            .withSmbEncryptionForIntraClusterTrafficComplianceAssignment(ComplianceAssignmentType.AUDIT)
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
     * x-ms-original-file: 2025-12-01-preview/DeleteSecuritySettings.json
     */
    /**
     * Sample code: Delete Security Settings.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void deleteSecuritySettings(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
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
     * x-ms-original-file: 2025-12-01-preview/GetSecuritySettings.json
     */
    /**
     * Sample code: Get Security Settings.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void getSecuritySettings(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
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
     * x-ms-original-file: 2025-12-01-preview/ListSecuritySettingsByCluster.json
     */
    /**
     * Sample code: List Security Settings.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void listSecuritySettings(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
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
     * x-ms-original-file: 2025-12-01-preview/GetSku.json
     */
    /**
     * Sample code: Get Sku.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void getSku(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
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
     * x-ms-original-file: 2025-12-01-preview/ListSkusByOffer.json
     */
    /**
     * Sample code: List SKU resources by offer for the HCI Cluster.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void
        listSKUResourcesByOfferForTheHCICluster(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.skus()
            .listByOffer("test-rg", "myCluster", "publisher1", "offer1", null, com.azure.core.util.Context.NONE);
    }
}
```

### UpdateContents_Get

```java
/**
 * Samples for UpdateContents Get.
 */
public final class UpdateContentsGetSamples {
    /*
     * x-ms-original-file: 2025-12-01-preview/UpdateContents_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: UpdateContents_Get_MaximumSet.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void
        updateContentsGetMaximumSet(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.updateContents().getWithResponse("westus2", "12.2510.0.1", com.azure.core.util.Context.NONE);
    }
}
```

### UpdateContents_List

```java
/**
 * Samples for UpdateContents List.
 */
public final class UpdateContentsListSamples {
    /*
     * x-ms-original-file: 2025-12-01-preview/UpdateContents_ListByLocation_MaximumSet_Gen.json
     */
    /**
     * Sample code: UpdateContents_ListByLocation_MaximumSet.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void
        updateContentsListByLocationMaximumSet(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.updateContents().list("westus2", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2025-12-01-preview/DeleteUpdateRuns.json
     */
    /**
     * Sample code: Delete an Update.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void deleteAnUpdate(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
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
     * x-ms-original-file: 2025-12-01-preview/GetUpdateRuns.json
     */
    /**
     * Sample code: Get Update runs under cluster resource.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void
        getUpdateRunsUnderClusterResource(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
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
     * x-ms-original-file: 2025-12-01-preview/ListUpdateRuns.json
     */
    /**
     * Sample code: List Update runs under cluster resource.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void
        listUpdateRunsUnderClusterResource(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
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
     * x-ms-original-file: 2025-12-01-preview/PutUpdateRuns.json
     */
    /**
     * Sample code: Get Update runs under cluster resource.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void
        getUpdateRunsUnderClusterResource(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
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
     * x-ms-original-file: 2025-12-01-preview/DeleteUpdateSummaries.json
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
/**
 * Samples for UpdateSummariesOperation Get.
 */
public final class UpdateSummariesOperationGetSamples {
    /*
     * x-ms-original-file: 2025-12-01-preview/GetUpdateSummaries.json
     */
    /**
     * Sample code: Get Update summaries under cluster resource.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void
        getUpdateSummariesUnderClusterResource(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
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
     * x-ms-original-file: 2025-12-01-preview/ListUpdateSummaries.json
     */
    /**
     * Sample code: Get Update summaries under cluster resource.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void
        getUpdateSummariesUnderClusterResource(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
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
     * x-ms-original-file: 2025-12-01-preview/PutUpdateSummaries.json
     */
    /**
     * Sample code: Put Update summaries under cluster resource.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void
        putUpdateSummariesUnderClusterResource(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
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
     * x-ms-original-file: 2025-12-01-preview/DeleteUpdates.json
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
/**
 * Samples for Updates Get.
 */
public final class UpdatesGetSamples {
    /*
     * x-ms-original-file: 2025-12-01-preview/GetUpdates.json
     */
    /**
     * Sample code: Get a specific update.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void getASpecificUpdate(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
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
     * x-ms-original-file: 2025-12-01-preview/ListUpdates.json
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
/**
 * Samples for Updates Post.
 */
public final class UpdatesPostSamples {
    /*
     * x-ms-original-file: 2025-12-01-preview/PostUpdates.json
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

/**
 * Samples for Updates Put.
 */
public final class UpdatesPutSamples {
    /*
     * x-ms-original-file: 2025-12-01-preview/PutUpdates.json
     */
    /**
     * Sample code: Put a specific update.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void putASpecificUpdate(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
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
            .withPackageSizeInMb(18858.0D)
            .withDisplayName("AzS Update - 4.2203.2.32")
            .withVersion("4.2203.2.32")
            .withPublisher("Microsoft")
            .withReleaseLink("https://docs.microsoft.com/azure-stack/operator/release-notes?view=azs-2203")
            .withAvailabilityType(AvailabilityType.LOCAL)
            .withPackageType("Infrastructure")
            .withAdditionalProperties("additional properties")
            .withProgressPercentage(0.0D)
            .withNotifyMessage("Brief message with instructions for updates of AvailabilityType Notify")
            .apply();
    }
}
```

### ValidatedSolutionRecipes_Get

```java
/**
 * Samples for ValidatedSolutionRecipes Get.
 */
public final class ValidatedSolutionRecipesGetSamples {
    /*
     * x-ms-original-file: 2025-12-01-preview/ValidatedSolutionRecipes_Get.json
     */
    /**
     * Sample code: ValidatedSolutionRecipes_Get.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void
        validatedSolutionRecipesGet(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.validatedSolutionRecipes().getWithResponse("westus2", "10.2408.0", com.azure.core.util.Context.NONE);
    }
}
```

### ValidatedSolutionRecipes_ListBySubscriptionLocationResource

```java
/**
 * Samples for ValidatedSolutionRecipes ListBySubscriptionLocationResource.
 */
public final class ValidatedSolutionRecipesListBySubscriptionLocationResourceSamples {
    /*
     * x-ms-original-file: 2025-12-01-preview/ValidatedSolutionRecipes_ListBySubscriptionLocationResource.json
     */
    /**
     * Sample code: ValidatedSolutionRecipes_ListBySubscriptionLocationResource.
     * 
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void validatedSolutionRecipesListBySubscriptionLocationResource(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.validatedSolutionRecipes()
            .listBySubscriptionLocationResource("westus2", com.azure.core.util.Context.NONE);
    }
}
```

