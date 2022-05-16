# Code snippets and samples


## ApiPortalCustomDomains

- [CreateOrUpdate](#apiportalcustomdomains_createorupdate)
- [Delete](#apiportalcustomdomains_delete)
- [Get](#apiportalcustomdomains_get)
- [List](#apiportalcustomdomains_list)

## ApiPortals

- [CreateOrUpdate](#apiportals_createorupdate)
- [Delete](#apiportals_delete)
- [Get](#apiportals_get)
- [List](#apiportals_list)
- [ValidateDomain](#apiportals_validatedomain)

## Apps

- [CreateOrUpdate](#apps_createorupdate)
- [Delete](#apps_delete)
- [Get](#apps_get)
- [GetResourceUploadUrl](#apps_getresourceuploadurl)
- [List](#apps_list)
- [SetActiveDeployments](#apps_setactivedeployments)
- [Update](#apps_update)
- [ValidateDomain](#apps_validatedomain)

## Bindings

- [CreateOrUpdate](#bindings_createorupdate)
- [Delete](#bindings_delete)
- [Get](#bindings_get)
- [List](#bindings_list)
- [Update](#bindings_update)

## BuildService

- [CreateOrUpdateBuild](#buildservice_createorupdatebuild)
- [GetBuild](#buildservice_getbuild)
- [GetBuildResult](#buildservice_getbuildresult)
- [GetBuildResultLog](#buildservice_getbuildresultlog)
- [GetBuildService](#buildservice_getbuildservice)
- [GetResourceUploadUrl](#buildservice_getresourceuploadurl)
- [GetSupportedBuildpack](#buildservice_getsupportedbuildpack)
- [GetSupportedStack](#buildservice_getsupportedstack)
- [ListBuildResults](#buildservice_listbuildresults)
- [ListBuildServices](#buildservice_listbuildservices)
- [ListBuilds](#buildservice_listbuilds)
- [ListSupportedBuildpacks](#buildservice_listsupportedbuildpacks)
- [ListSupportedStacks](#buildservice_listsupportedstacks)

## BuildServiceAgentPool

- [Get](#buildserviceagentpool_get)
- [List](#buildserviceagentpool_list)
- [UpdatePut](#buildserviceagentpool_updateput)

## BuildServiceBuilder

- [CreateOrUpdate](#buildservicebuilder_createorupdate)
- [Delete](#buildservicebuilder_delete)
- [Get](#buildservicebuilder_get)
- [List](#buildservicebuilder_list)

## BuildpackBinding

- [CreateOrUpdate](#buildpackbinding_createorupdate)
- [Delete](#buildpackbinding_delete)
- [Get](#buildpackbinding_get)
- [List](#buildpackbinding_list)

## Certificates

- [CreateOrUpdate](#certificates_createorupdate)
- [Delete](#certificates_delete)
- [Get](#certificates_get)
- [List](#certificates_list)

## ConfigServers

- [Get](#configservers_get)
- [UpdatePatch](#configservers_updatepatch)
- [UpdatePut](#configservers_updateput)
- [Validate](#configservers_validate)

## ConfigurationServices

- [CreateOrUpdate](#configurationservices_createorupdate)
- [Delete](#configurationservices_delete)
- [Get](#configurationservices_get)
- [List](#configurationservices_list)
- [Validate](#configurationservices_validate)

## CustomDomains

- [CreateOrUpdate](#customdomains_createorupdate)
- [Delete](#customdomains_delete)
- [Get](#customdomains_get)
- [List](#customdomains_list)
- [Update](#customdomains_update)

## Deployments

- [CreateOrUpdate](#deployments_createorupdate)
- [Delete](#deployments_delete)
- [GenerateHeapDump](#deployments_generateheapdump)
- [GenerateThreadDump](#deployments_generatethreaddump)
- [Get](#deployments_get)
- [GetLogFileUrl](#deployments_getlogfileurl)
- [List](#deployments_list)
- [ListForCluster](#deployments_listforcluster)
- [Restart](#deployments_restart)
- [Start](#deployments_start)
- [StartJfr](#deployments_startjfr)
- [Stop](#deployments_stop)
- [Update](#deployments_update)

## GatewayCustomDomains

- [CreateOrUpdate](#gatewaycustomdomains_createorupdate)
- [Delete](#gatewaycustomdomains_delete)
- [Get](#gatewaycustomdomains_get)
- [List](#gatewaycustomdomains_list)

## GatewayRouteConfigs

- [CreateOrUpdate](#gatewayrouteconfigs_createorupdate)
- [Delete](#gatewayrouteconfigs_delete)
- [Get](#gatewayrouteconfigs_get)
- [List](#gatewayrouteconfigs_list)

## Gateways

- [CreateOrUpdate](#gateways_createorupdate)
- [Delete](#gateways_delete)
- [Get](#gateways_get)
- [List](#gateways_list)
- [ValidateDomain](#gateways_validatedomain)

## MonitoringSettings

- [Get](#monitoringsettings_get)
- [UpdatePatch](#monitoringsettings_updatepatch)
- [UpdatePut](#monitoringsettings_updateput)

## Operations

- [List](#operations_list)

## RuntimeVersions

- [ListRuntimeVersions](#runtimeversions_listruntimeversions)

## ServiceRegistries

- [CreateOrUpdate](#serviceregistries_createorupdate)
- [Delete](#serviceregistries_delete)
- [Get](#serviceregistries_get)
- [List](#serviceregistries_list)

## Services

- [CheckNameAvailability](#services_checknameavailability)
- [CreateOrUpdate](#services_createorupdate)
- [Delete](#services_delete)
- [DisableTestEndpoint](#services_disabletestendpoint)
- [EnableTestEndpoint](#services_enabletestendpoint)
- [GetByResourceGroup](#services_getbyresourcegroup)
- [List](#services_list)
- [ListByResourceGroup](#services_listbyresourcegroup)
- [ListTestKeys](#services_listtestkeys)
- [RegenerateTestKey](#services_regeneratetestkey)
- [Start](#services_start)
- [Stop](#services_stop)
- [Update](#services_update)

## Skus

- [List](#skus_list)

## Storages

- [CreateOrUpdate](#storages_createorupdate)
- [Delete](#storages_delete)
- [Get](#storages_get)
- [List](#storages_list)
### ApiPortalCustomDomains_CreateOrUpdate

```java
import com.azure.resourcemanager.appplatform.generated.models.ApiPortalCustomDomainProperties;

/** Samples for ApiPortalCustomDomains CreateOrUpdate. */
public final class ApiPortalCustomDomainsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/ApiPortalCustomDomains_CreateOrUpdate.json
     */
    /**
     * Sample code: ApiPortalCustomDomains_CreateOrUpdate.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void apiPortalCustomDomainsCreateOrUpdate(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .apiPortalCustomDomains()
            .define("myDomainName")
            .withExistingApiPortal("myResourceGroup", "myservice", "default")
            .withProperties(new ApiPortalCustomDomainProperties().withThumbprint("*"))
            .create();
    }
}
```

### ApiPortalCustomDomains_Delete

```java
import com.azure.core.util.Context;

/** Samples for ApiPortalCustomDomains Delete. */
public final class ApiPortalCustomDomainsDeleteSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/ApiPortalCustomDomains_Delete.json
     */
    /**
     * Sample code: ApiPortalCustomDomains_Delete.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void apiPortalCustomDomainsDelete(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .apiPortalCustomDomains()
            .delete("myResourceGroup", "myservice", "default", "myDomainName", Context.NONE);
    }
}
```

### ApiPortalCustomDomains_Get

```java
import com.azure.core.util.Context;

/** Samples for ApiPortalCustomDomains Get. */
public final class ApiPortalCustomDomainsGetSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/ApiPortalCustomDomains_Get.json
     */
    /**
     * Sample code: ApiPortalCustomDomains_Get.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void apiPortalCustomDomainsGet(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .apiPortalCustomDomains()
            .getWithResponse("myResourceGroup", "myservice", "default", "myDomainName", Context.NONE);
    }
}
```

### ApiPortalCustomDomains_List

```java
import com.azure.core.util.Context;

/** Samples for ApiPortalCustomDomains List. */
public final class ApiPortalCustomDomainsListSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/ApiPortalCustomDomains_List.json
     */
    /**
     * Sample code: ApiPortalCustomDomains_List.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void apiPortalCustomDomainsList(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.apiPortalCustomDomains().list("myResourceGroup", "myservice", "default", Context.NONE);
    }
}
```

### ApiPortals_CreateOrUpdate

```java
import com.azure.resourcemanager.appplatform.generated.models.ApiPortalProperties;
import com.azure.resourcemanager.appplatform.generated.models.Sku;
import java.util.Arrays;

/** Samples for ApiPortals CreateOrUpdate. */
public final class ApiPortalsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/ApiPortals_CreateOrUpdate.json
     */
    /**
     * Sample code: ApiPortals_CreateOrUpdate.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void apiPortalsCreateOrUpdate(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .apiPortals()
            .define("default")
            .withExistingSpring("myResourceGroup", "myservice")
            .withProperties(
                new ApiPortalProperties()
                    .withPublicProperty(true)
                    .withGatewayIds(
                        Arrays
                            .asList(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.AppPlatform/Spring/myservice/gateways/default")))
            .withSku(new Sku().withName("E0").withTier("Enterprise").withCapacity(2))
            .create();
    }
}
```

### ApiPortals_Delete

```java
import com.azure.core.util.Context;

/** Samples for ApiPortals Delete. */
public final class ApiPortalsDeleteSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/ApiPortals_Delete.json
     */
    /**
     * Sample code: ApiPortals_Delete.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void apiPortalsDelete(com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.apiPortals().delete("myResourceGroup", "myservice", "default", Context.NONE);
    }
}
```

### ApiPortals_Get

```java
import com.azure.core.util.Context;

/** Samples for ApiPortals Get. */
public final class ApiPortalsGetSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/ApiPortals_Get.json
     */
    /**
     * Sample code: ApiPortals_Get.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void apiPortalsGet(com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.apiPortals().getWithResponse("myResourceGroup", "myservice", "default", Context.NONE);
    }
}
```

### ApiPortals_List

```java
import com.azure.core.util.Context;

/** Samples for ApiPortals List. */
public final class ApiPortalsListSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/ApiPortals_List.json
     */
    /**
     * Sample code: ApiPortals_List.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void apiPortalsList(com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.apiPortals().list("myResourceGroup", "myservice", Context.NONE);
    }
}
```

### ApiPortals_ValidateDomain

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.appplatform.generated.models.CustomDomainValidatePayload;

/** Samples for ApiPortals ValidateDomain. */
public final class ApiPortalsValidateDomainSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/ApiPortals_ValidateDomain.json
     */
    /**
     * Sample code: ApiPortals_ValidateDomain.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void apiPortalsValidateDomain(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .apiPortals()
            .validateDomainWithResponse(
                "myResourceGroup",
                "myservice",
                "default",
                new CustomDomainValidatePayload().withName("mydomain.io"),
                Context.NONE);
    }
}
```

### Apps_CreateOrUpdate

```java
import com.azure.resourcemanager.appplatform.generated.models.AppResourceProperties;
import com.azure.resourcemanager.appplatform.generated.models.AppVNetAddons;
import com.azure.resourcemanager.appplatform.generated.models.AzureFileVolume;
import com.azure.resourcemanager.appplatform.generated.models.CustomPersistentDiskResource;
import com.azure.resourcemanager.appplatform.generated.models.LoadedCertificate;
import com.azure.resourcemanager.appplatform.generated.models.ManagedIdentityProperties;
import com.azure.resourcemanager.appplatform.generated.models.ManagedIdentityType;
import com.azure.resourcemanager.appplatform.generated.models.PersistentDisk;
import com.azure.resourcemanager.appplatform.generated.models.TemporaryDisk;
import com.azure.resourcemanager.appplatform.generated.models.UserAssignedManagedIdentity;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Apps CreateOrUpdate. */
public final class AppsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Apps_CreateOrUpdate.json
     */
    /**
     * Sample code: Apps_CreateOrUpdate.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void appsCreateOrUpdate(com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .apps()
            .define("myapp")
            .withExistingSpring("myResourceGroup", "myservice")
            .withRegion("eastus")
            .withProperties(
                new AppResourceProperties()
                    .withPublicProperty(true)
                    .withAddonConfigs(
                        mapOf(
                            "ApplicationConfigurationService",
                            mapOf(
                                "resourceId",
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.AppPlatform/Spring/myservice/configurationServices/myacs"),
                            "ServiceRegistry",
                            mapOf(
                                "resourceId",
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.AppPlatform/Spring/myservice/serviceRegistries/myServiceRegistry")))
                    .withFqdn("myapp.mydomain.com")
                    .withHttpsOnly(false)
                    .withTemporaryDisk(new TemporaryDisk().withSizeInGB(2).withMountPath("/mytemporarydisk"))
                    .withPersistentDisk(new PersistentDisk().withSizeInGB(2).withMountPath("/mypersistentdisk"))
                    .withCustomPersistentDisks(
                        Arrays
                            .asList(
                                new CustomPersistentDiskResource()
                                    .withCustomPersistentDiskProperties(
                                        new AzureFileVolume()
                                            .withMountPath("/mypath1/mypath2")
                                            .withMountOptions(
                                                Arrays.asList("uid=0", "gid=0", "dir_mode=0777", "file_mode=0777"))
                                            .withShareName("myFileShare"))
                                    .withStorageId("myASCStorageID")))
                    .withEnableEndToEndTls(false)
                    .withLoadedCertificates(
                        Arrays
                            .asList(
                                new LoadedCertificate()
                                    .withResourceId(
                                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.AppPlatform/Spring/myservice/certificates/mycert1")
                                    .withLoadTrustStore(false),
                                new LoadedCertificate()
                                    .withResourceId(
                                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.AppPlatform/Spring/myservice/certificates/mycert2")
                                    .withLoadTrustStore(true))))
            .withIdentity(
                new ManagedIdentityProperties()
                    .withType(ManagedIdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/samplegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id1",
                            new UserAssignedManagedIdentity(),
                            "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/samplegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id2",
                            new UserAssignedManagedIdentity())))
            .create();
    }

    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Apps_CreateOrUpdate_VNetInjection.json
     */
    /**
     * Sample code: Apps_CreateOrUpdate_VNetInjection.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void appsCreateOrUpdateVNetInjection(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .apps()
            .define("myapp")
            .withExistingSpring("myResourceGroup", "myservice")
            .withRegion("eastus")
            .withProperties(
                new AppResourceProperties()
                    .withPublicProperty(true)
                    .withAddonConfigs(
                        mapOf(
                            "ApplicationConfigurationService",
                            mapOf(
                                "resourceId",
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.AppPlatform/Spring/myservice/configurationServices/myacs"),
                            "ServiceRegistry",
                            mapOf(
                                "resourceId",
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.AppPlatform/Spring/myservice/serviceRegistries/myServiceRegistry")))
                    .withFqdn("myapp.private.mydomain.com")
                    .withHttpsOnly(false)
                    .withTemporaryDisk(new TemporaryDisk().withSizeInGB(2).withMountPath("/mytemporarydisk"))
                    .withPersistentDisk(new PersistentDisk().withSizeInGB(2).withMountPath("/mypersistentdisk"))
                    .withCustomPersistentDisks(
                        Arrays
                            .asList(
                                new CustomPersistentDiskResource()
                                    .withCustomPersistentDiskProperties(
                                        new AzureFileVolume()
                                            .withMountPath("/mypath1/mypath2")
                                            .withMountOptions(
                                                Arrays.asList("uid=0", "gid=0", "dir_mode=0777", "file_mode=0777"))
                                            .withShareName("myFileShare"))
                                    .withStorageId("myASCStorageID")))
                    .withEnableEndToEndTls(false)
                    .withLoadedCertificates(
                        Arrays
                            .asList(
                                new LoadedCertificate()
                                    .withResourceId(
                                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.AppPlatform/Spring/myservice/certificates/mycert1")
                                    .withLoadTrustStore(false),
                                new LoadedCertificate()
                                    .withResourceId(
                                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.AppPlatform/Spring/myservice/certificates/mycert2")
                                    .withLoadTrustStore(true)))
                    .withVnetAddons(new AppVNetAddons().withPublicEndpoint(true)))
            .withIdentity(
                new ManagedIdentityProperties()
                    .withType(ManagedIdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/samplegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id1",
                            new UserAssignedManagedIdentity(),
                            "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/samplegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id2",
                            new UserAssignedManagedIdentity())))
            .create();
    }

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

### Apps_Delete

```java
import com.azure.core.util.Context;

/** Samples for Apps Delete. */
public final class AppsDeleteSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Apps_Delete.json
     */
    /**
     * Sample code: Apps_Delete.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void appsDelete(com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.apps().delete("myResourceGroup", "myservice", "myapp", Context.NONE);
    }
}
```

### Apps_Get

```java
import com.azure.core.util.Context;

/** Samples for Apps Get. */
public final class AppsGetSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Apps_Get_VNetInjection.json
     */
    /**
     * Sample code: Apps_Get_VNetInjection.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void appsGetVNetInjection(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.apps().getWithResponse("myResourceGroup", "myservice", "myapp", null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Apps_Get.json
     */
    /**
     * Sample code: Apps_Get.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void appsGet(com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.apps().getWithResponse("myResourceGroup", "myservice", "myapp", null, Context.NONE);
    }
}
```

### Apps_GetResourceUploadUrl

```java
import com.azure.core.util.Context;

/** Samples for Apps GetResourceUploadUrl. */
public final class AppsGetResourceUploadUrlSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Apps_GetResourceUploadUrl.json
     */
    /**
     * Sample code: Apps_GetResourceUploadUrl.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void appsGetResourceUploadUrl(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.apps().getResourceUploadUrlWithResponse("myResourceGroup", "myservice", "myapp", Context.NONE);
    }
}
```

### Apps_List

```java
import com.azure.core.util.Context;

/** Samples for Apps List. */
public final class AppsListSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Apps_List.json
     */
    /**
     * Sample code: Apps_List.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void appsList(com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.apps().list("myResourceGroup", "myservice", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Apps_List_VNetInjection.json
     */
    /**
     * Sample code: Apps_List_VNetInjection.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void appsListVNetInjection(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.apps().list("myResourceGroup", "myservice", Context.NONE);
    }
}
```

### Apps_SetActiveDeployments

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.appplatform.generated.models.ActiveDeploymentCollection;
import java.util.Arrays;

/** Samples for Apps SetActiveDeployments. */
public final class AppsSetActiveDeploymentsSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Apps_SetActiveDeployments.json
     */
    /**
     * Sample code: Apps_SetActiveDeployments.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void appsSetActiveDeployments(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .apps()
            .setActiveDeployments(
                "myResourceGroup",
                "myservice",
                "myapp",
                new ActiveDeploymentCollection().withActiveDeploymentNames(Arrays.asList("default")),
                Context.NONE);
    }
}
```

### Apps_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.appplatform.generated.models.AppResource;
import com.azure.resourcemanager.appplatform.generated.models.AppResourceProperties;
import com.azure.resourcemanager.appplatform.generated.models.AppVNetAddons;
import com.azure.resourcemanager.appplatform.generated.models.AzureFileVolume;
import com.azure.resourcemanager.appplatform.generated.models.CustomPersistentDiskResource;
import com.azure.resourcemanager.appplatform.generated.models.ManagedIdentityProperties;
import com.azure.resourcemanager.appplatform.generated.models.ManagedIdentityType;
import com.azure.resourcemanager.appplatform.generated.models.PersistentDisk;
import com.azure.resourcemanager.appplatform.generated.models.TemporaryDisk;
import com.azure.resourcemanager.appplatform.generated.models.UserAssignedManagedIdentity;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Apps Update. */
public final class AppsUpdateSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Apps_Update_VNetInjection.json
     */
    /**
     * Sample code: Apps_Update_VNetInjection.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void appsUpdateVNetInjection(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        AppResource resource =
            manager.apps().getWithResponse("myResourceGroup", "myservice", "myapp", null, Context.NONE).getValue();
        resource
            .update()
            .withProperties(
                new AppResourceProperties()
                    .withPublicProperty(true)
                    .withFqdn("myapp.private.mydomain.com")
                    .withHttpsOnly(false)
                    .withTemporaryDisk(new TemporaryDisk().withSizeInGB(2).withMountPath("/mytemporarydisk"))
                    .withPersistentDisk(new PersistentDisk().withSizeInGB(2).withMountPath("/mypersistentdisk"))
                    .withCustomPersistentDisks(
                        Arrays
                            .asList(
                                new CustomPersistentDiskResource()
                                    .withCustomPersistentDiskProperties(
                                        new AzureFileVolume()
                                            .withMountPath("/mypath1/mypath2")
                                            .withMountOptions(Arrays.asList())
                                            .withShareName("myFileShare"))
                                    .withStorageId("myASCStorageID")))
                    .withEnableEndToEndTls(false)
                    .withVnetAddons(new AppVNetAddons().withPublicEndpoint(true)))
            .withIdentity(
                new ManagedIdentityProperties()
                    .withType(ManagedIdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/samplegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id1",
                            new UserAssignedManagedIdentity(),
                            "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/samplegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id2",
                            new UserAssignedManagedIdentity())))
            .apply();
    }

    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Apps_Update.json
     */
    /**
     * Sample code: Apps_Update.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void appsUpdate(com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        AppResource resource =
            manager.apps().getWithResponse("myResourceGroup", "myservice", "myapp", null, Context.NONE).getValue();
        resource
            .update()
            .withProperties(
                new AppResourceProperties()
                    .withPublicProperty(true)
                    .withFqdn("myapp.mydomain.com")
                    .withHttpsOnly(false)
                    .withTemporaryDisk(new TemporaryDisk().withSizeInGB(2).withMountPath("/mytemporarydisk"))
                    .withPersistentDisk(new PersistentDisk().withSizeInGB(2).withMountPath("/mypersistentdisk"))
                    .withCustomPersistentDisks(
                        Arrays
                            .asList(
                                new CustomPersistentDiskResource()
                                    .withCustomPersistentDiskProperties(
                                        new AzureFileVolume()
                                            .withMountPath("/mypath1/mypath2")
                                            .withMountOptions(Arrays.asList())
                                            .withShareName("myFileShare"))
                                    .withStorageId("myASCStorageID")))
                    .withEnableEndToEndTls(false))
            .withIdentity(
                new ManagedIdentityProperties()
                    .withType(ManagedIdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/samplegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id1",
                            new UserAssignedManagedIdentity(),
                            "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/samplegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id2",
                            new UserAssignedManagedIdentity())))
            .apply();
    }

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

### Apps_ValidateDomain

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.appplatform.generated.models.CustomDomainValidatePayload;

/** Samples for Apps ValidateDomain. */
public final class AppsValidateDomainSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Apps_ValidateDomain.json
     */
    /**
     * Sample code: Apps_ValidateDomain.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void appsValidateDomain(com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .apps()
            .validateDomainWithResponse(
                "myResourceGroup",
                "myservice",
                "myapp",
                new CustomDomainValidatePayload().withName("mydomain.io"),
                Context.NONE);
    }
}
```

### Bindings_CreateOrUpdate

```java
import com.azure.resourcemanager.appplatform.generated.models.BindingResourceProperties;
import java.util.HashMap;
import java.util.Map;

/** Samples for Bindings CreateOrUpdate. */
public final class BindingsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Bindings_CreateOrUpdate.json
     */
    /**
     * Sample code: Bindings_CreateOrUpdate.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void bindingsCreateOrUpdate(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .bindings()
            .define("mybinding")
            .withExistingApp("myResourceGroup", "myservice", "myapp")
            .withProperties(
                new BindingResourceProperties()
                    .withResourceId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.DocumentDB/databaseAccounts/my-cosmosdb-1")
                    .withKey("xxxx")
                    .withBindingParameters(mapOf("apiType", "SQL", "databaseName", "db1")))
            .create();
    }

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

### Bindings_Delete

```java
import com.azure.core.util.Context;

/** Samples for Bindings Delete. */
public final class BindingsDeleteSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Bindings_Delete.json
     */
    /**
     * Sample code: Bindings_Delete.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void bindingsDelete(com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.bindings().delete("myResourceGroup", "myservice", "myapp", "mybinding", Context.NONE);
    }
}
```

### Bindings_Get

```java
import com.azure.core.util.Context;

/** Samples for Bindings Get. */
public final class BindingsGetSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Bindings_Get.json
     */
    /**
     * Sample code: Bindings_Get.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void bindingsGet(com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.bindings().getWithResponse("myResourceGroup", "myservice", "myapp", "mybinding", Context.NONE);
    }
}
```

### Bindings_List

```java
import com.azure.core.util.Context;

/** Samples for Bindings List. */
public final class BindingsListSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Bindings_List.json
     */
    /**
     * Sample code: Bindings_List.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void bindingsList(com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.bindings().list("myResourceGroup", "myservice", "myapp", Context.NONE);
    }
}
```

### Bindings_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.appplatform.generated.models.BindingResource;
import com.azure.resourcemanager.appplatform.generated.models.BindingResourceProperties;
import java.util.HashMap;
import java.util.Map;

/** Samples for Bindings Update. */
public final class BindingsUpdateSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Bindings_Update.json
     */
    /**
     * Sample code: Bindings_Update.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void bindingsUpdate(com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        BindingResource resource =
            manager
                .bindings()
                .getWithResponse("myResourceGroup", "myservice", "myapp", "mybinding", Context.NONE)
                .getValue();
        resource
            .update()
            .withProperties(
                new BindingResourceProperties()
                    .withKey("xxxx")
                    .withBindingParameters(mapOf("apiType", "SQL", "databaseName", "db1")))
            .apply();
    }

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

### BuildService_CreateOrUpdateBuild

```java
import com.azure.resourcemanager.appplatform.generated.models.BuildProperties;
import com.azure.resourcemanager.appplatform.generated.models.BuildResourceRequests;
import java.util.HashMap;
import java.util.Map;

/** Samples for BuildService CreateOrUpdateBuild. */
public final class BuildServiceCreateOrUpdateBuildSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/BuildService_CreateOrUpdateBuild.json
     */
    /**
     * Sample code: BuildService_CreateOrUpdateBuild.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void buildServiceCreateOrUpdateBuild(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .buildServices()
            .define("mybuild")
            .withExistingBuildService("myResourceGroup", "myservice", "default")
            .withProperties(
                new BuildProperties()
                    .withRelativePath(
                        "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855-20210601-3ed9f4a2-986b-4bbd-b833-a42dccb2f777")
                    .withBuilder(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.AppPlatform/Spring/myservice/buildServices/default/builders/default")
                    .withAgentPool(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.AppPlatform/Spring/myservice/buildServices/default/agentPools/default")
                    .withEnv(mapOf("environmentVariable", "test"))
                    .withResourceRequests(new BuildResourceRequests().withCpu("1").withMemory("2Gi")))
            .create();
    }

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

### BuildService_GetBuild

```java
import com.azure.core.util.Context;

/** Samples for BuildService GetBuild. */
public final class BuildServiceGetBuildSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/BuildService_GetBuild.json
     */
    /**
     * Sample code: BuildService_GetBuild.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void buildServiceGetBuild(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .buildServices()
            .getBuildWithResponse("myResourceGroup", "myservice", "default", "mybuild", Context.NONE);
    }
}
```

### BuildService_GetBuildResult

```java
import com.azure.core.util.Context;

/** Samples for BuildService GetBuildResult. */
public final class BuildServiceGetBuildResultSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/BuildService_GetBuildResult.json
     */
    /**
     * Sample code: BuildService_GetBuildResult.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void buildServiceGetBuildResult(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .buildServices()
            .getBuildResultWithResponse("myResourceGroup", "myservice", "default", "mybuild", "123", Context.NONE);
    }
}
```

### BuildService_GetBuildResultLog

```java
import com.azure.core.util.Context;

/** Samples for BuildService GetBuildResultLog. */
public final class BuildServiceGetBuildResultLogSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/BuildService_GetBuildResultLog.json
     */
    /**
     * Sample code: BuildService_GetBuildResultLog.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void buildServiceGetBuildResultLog(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .buildServices()
            .getBuildResultLogWithResponse("myResourceGroup", "myservice", "default", "mybuild", "123", Context.NONE);
    }
}
```

### BuildService_GetBuildService

```java
import com.azure.core.util.Context;

/** Samples for BuildService GetBuildService. */
public final class BuildServiceGetBuildServiceSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/BuildService_GetBuildService.json
     */
    /**
     * Sample code: BuildService_GetBuildService.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void buildServiceGetBuildService(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.buildServices().getBuildServiceWithResponse("myResourceGroup", "myservice", "default", Context.NONE);
    }
}
```

### BuildService_GetResourceUploadUrl

```java
import com.azure.core.util.Context;

/** Samples for BuildService GetResourceUploadUrl. */
public final class BuildServiceGetResourceUploadUrlSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/BuildService_GetResourceUploadUrl.json
     */
    /**
     * Sample code: BuildService_GetResourceUploadUrl.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void buildServiceGetResourceUploadUrl(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .buildServices()
            .getResourceUploadUrlWithResponse("myResourceGroup", "myservice", "default", Context.NONE);
    }
}
```

### BuildService_GetSupportedBuildpack

```java
import com.azure.core.util.Context;

/** Samples for BuildService GetSupportedBuildpack. */
public final class BuildServiceGetSupportedBuildpackSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/BuildService_GetSupportedBuildpack.json
     */
    /**
     * Sample code: BuildService_GetSupportedBuildpack.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void buildServiceGetSupportedBuildpack(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .buildServices()
            .getSupportedBuildpackWithResponse(
                "myResourceGroup", "myservice", "default", "tanzu-buildpacks-java-azure", Context.NONE);
    }
}
```

### BuildService_GetSupportedStack

```java
import com.azure.core.util.Context;

/** Samples for BuildService GetSupportedStack. */
public final class BuildServiceGetSupportedStackSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/BuildService_GetSupportedStack.json
     */
    /**
     * Sample code: BuildService_GetSupportedStack.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void buildServiceGetSupportedStack(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .buildServices()
            .getSupportedStackWithResponse(
                "myResourceGroup", "myservice", "default", "io.buildpacks.stacks.bionic-base", Context.NONE);
    }
}
```

### BuildService_ListBuildResults

```java
import com.azure.core.util.Context;

/** Samples for BuildService ListBuildResults. */
public final class BuildServiceListBuildResultsSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/BuildService_ListBuildResults.json
     */
    /**
     * Sample code: BuildService_ListBuildResults.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void buildServiceListBuildResults(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.buildServices().listBuildResults("myResourceGroup", "myservice", "default", "mybuild", Context.NONE);
    }
}
```

### BuildService_ListBuildServices

```java
import com.azure.core.util.Context;

/** Samples for BuildService ListBuildServices. */
public final class BuildServiceListBuildServicesSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/BuildService_ListBuildServices.json
     */
    /**
     * Sample code: BuildService_ListBuildServices.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void buildServiceListBuildServices(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.buildServices().listBuildServices("myResourceGroup", "myservice", Context.NONE);
    }
}
```

### BuildService_ListBuilds

```java
import com.azure.core.util.Context;

/** Samples for BuildService ListBuilds. */
public final class BuildServiceListBuildsSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/BuildService_ListBuilds.json
     */
    /**
     * Sample code: BuildService_ListBuilds.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void buildServiceListBuilds(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.buildServices().listBuilds("myResourceGroup", "myservice", "default", Context.NONE);
    }
}
```

### BuildService_ListSupportedBuildpacks

```java
import com.azure.core.util.Context;

/** Samples for BuildService ListSupportedBuildpacks. */
public final class BuildServiceListSupportedBuildpacksSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/BuildService_ListSupportedBuildpacks.json
     */
    /**
     * Sample code: BuildService_ListSupportedBuildpacks.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void buildServiceListSupportedBuildpacks(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .buildServices()
            .listSupportedBuildpacksWithResponse("myResourceGroup", "myservice", "default", Context.NONE);
    }
}
```

### BuildService_ListSupportedStacks

```java
import com.azure.core.util.Context;

/** Samples for BuildService ListSupportedStacks. */
public final class BuildServiceListSupportedStacksSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/BuildService_ListSupportedStacks.json
     */
    /**
     * Sample code: BuildService_ListSupportedStacks.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void buildServiceListSupportedStacks(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .buildServices()
            .listSupportedStacksWithResponse("myResourceGroup", "myservice", "default", Context.NONE);
    }
}
```

### BuildServiceAgentPool_Get

```java
import com.azure.core.util.Context;

/** Samples for BuildServiceAgentPool Get. */
public final class BuildServiceAgentPoolGetSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/BuildServiceAgentPool_Get.json
     */
    /**
     * Sample code: BuildServiceAgentPool_Get.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void buildServiceAgentPoolGet(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .buildServiceAgentPools()
            .getWithResponse("myResourceGroup", "myservice", "default", "default", Context.NONE);
    }
}
```

### BuildServiceAgentPool_List

```java
import com.azure.core.util.Context;

/** Samples for BuildServiceAgentPool List. */
public final class BuildServiceAgentPoolListSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/BuildServiceAgentPool_List.json
     */
    /**
     * Sample code: BuildServiceAgentPool_List.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void buildServiceAgentPoolList(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.buildServiceAgentPools().list("myResourceGroup", "myservice", "default", Context.NONE);
    }
}
```

### BuildServiceAgentPool_UpdatePut

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.appplatform.generated.fluent.models.BuildServiceAgentPoolResourceInner;
import com.azure.resourcemanager.appplatform.generated.models.BuildServiceAgentPoolProperties;
import com.azure.resourcemanager.appplatform.generated.models.BuildServiceAgentPoolSizeProperties;

/** Samples for BuildServiceAgentPool UpdatePut. */
public final class BuildServiceAgentPoolUpdatePutSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/BuildServiceAgentPool_UpdatePut.json
     */
    /**
     * Sample code: BuildServiceAgentPool_UpdatePut.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void buildServiceAgentPoolUpdatePut(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .buildServiceAgentPools()
            .updatePut(
                "myResourceGroup",
                "myservice",
                "default",
                "default",
                new BuildServiceAgentPoolResourceInner()
                    .withProperties(
                        new BuildServiceAgentPoolProperties()
                            .withPoolSize(new BuildServiceAgentPoolSizeProperties().withName("S3"))),
                Context.NONE);
    }
}
```

### BuildServiceBuilder_CreateOrUpdate

```java
import com.azure.resourcemanager.appplatform.generated.models.BuilderProperties;
import com.azure.resourcemanager.appplatform.generated.models.BuildpackProperties;
import com.azure.resourcemanager.appplatform.generated.models.BuildpacksGroupProperties;
import com.azure.resourcemanager.appplatform.generated.models.StackProperties;
import java.util.Arrays;

/** Samples for BuildServiceBuilder CreateOrUpdate. */
public final class BuildServiceBuilderCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/BuildServiceBuilder_CreateOrUpdate.json
     */
    /**
     * Sample code: BuildServiceBuilder_CreateOrUpdate.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void buildServiceBuilderCreateOrUpdate(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .buildServiceBuilders()
            .define("mybuilder")
            .withExistingBuildService("myResourceGroup", "myservice", "default")
            .withProperties(
                new BuilderProperties()
                    .withStack(new StackProperties().withId("io.buildpacks.stacks.bionic").withVersion("base"))
                    .withBuildpackGroups(
                        Arrays
                            .asList(
                                new BuildpacksGroupProperties()
                                    .withName("mix")
                                    .withBuildpacks(
                                        Arrays
                                            .asList(new BuildpackProperties().withId("tanzu-buildpacks/java-azure"))))))
            .create();
    }
}
```

### BuildServiceBuilder_Delete

```java
import com.azure.core.util.Context;

/** Samples for BuildServiceBuilder Delete. */
public final class BuildServiceBuilderDeleteSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/BuildServiceBuilder_Delete.json
     */
    /**
     * Sample code: BuildServiceBuilder_Delete.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void buildServiceBuilderDelete(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.buildServiceBuilders().delete("myResourceGroup", "myservice", "default", "mybuilder", Context.NONE);
    }
}
```

### BuildServiceBuilder_Get

```java
import com.azure.core.util.Context;

/** Samples for BuildServiceBuilder Get. */
public final class BuildServiceBuilderGetSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/BuildServiceBuilder_Get.json
     */
    /**
     * Sample code: BuildServiceBuilder_Get.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void buildServiceBuilderGet(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .buildServiceBuilders()
            .getWithResponse("myResourceGroup", "myservice", "default", "mybuilder", Context.NONE);
    }
}
```

### BuildServiceBuilder_List

```java
import com.azure.core.util.Context;

/** Samples for BuildServiceBuilder List. */
public final class BuildServiceBuilderListSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/BuildServiceBuilder_List.json
     */
    /**
     * Sample code: BuildServiceBuilder_List.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void buildServiceBuilderList(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.buildServiceBuilders().list("myResourceGroup", "myservice", "default", Context.NONE);
    }
}
```

### BuildpackBinding_CreateOrUpdate

```java
import com.azure.resourcemanager.appplatform.generated.models.BindingType;
import com.azure.resourcemanager.appplatform.generated.models.BuildpackBindingLaunchProperties;
import com.azure.resourcemanager.appplatform.generated.models.BuildpackBindingProperties;
import java.util.HashMap;
import java.util.Map;

/** Samples for BuildpackBinding CreateOrUpdate. */
public final class BuildpackBindingCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/BuildpackBinding_CreateOrUpdate.json
     */
    /**
     * Sample code: BuildpackBinding_CreateOrUpdate.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void buildpackBindingCreateOrUpdate(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .buildpackBindings()
            .define("myBuildpackBinding")
            .withExistingBuilder("myResourceGroup", "myservice", "default", "default")
            .withProperties(
                new BuildpackBindingProperties()
                    .withBindingType(BindingType.APPLICATION_INSIGHTS)
                    .withLaunchProperties(
                        new BuildpackBindingLaunchProperties()
                            .withProperties(mapOf("abc", "def", "any-string", "any-string", "sampling-rate", "12.0"))
                            .withSecrets(
                                mapOf(
                                    "connection-string",
                                    "XXXXXXXXXXXXXXXXX=XXXXXXXXXXXXX-XXXXXXXXXXXXXXXXXXX;XXXXXXXXXXXXXXXXX=XXXXXXXXXXXXXXXXXXX"))))
            .create();
    }

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

### BuildpackBinding_Delete

```java
import com.azure.core.util.Context;

/** Samples for BuildpackBinding Delete. */
public final class BuildpackBindingDeleteSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/BuildpackBinding_Delete.json
     */
    /**
     * Sample code: BuildpackBinding_Delete.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void buildpackBindingDelete(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .buildpackBindings()
            .delete("myResourceGroup", "myservice", "default", "default", "myBuildpackBinding", Context.NONE);
    }
}
```

### BuildpackBinding_Get

```java
import com.azure.core.util.Context;

/** Samples for BuildpackBinding Get. */
public final class BuildpackBindingGetSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/BuildpackBinding_Get.json
     */
    /**
     * Sample code: BuildpackBinding_Get.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void buildpackBindingGet(com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .buildpackBindings()
            .getWithResponse("myResourceGroup", "myservice", "default", "default", "myBuildpackBinding", Context.NONE);
    }
}
```

### BuildpackBinding_List

```java
import com.azure.core.util.Context;

/** Samples for BuildpackBinding List. */
public final class BuildpackBindingListSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/BuildpackBinding_List.json
     */
    /**
     * Sample code: BuildpackBinding_Get.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void buildpackBindingGet(com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.buildpackBindings().list("myResourceGroup", "myservice", "default", "default", Context.NONE);
    }
}
```

### Certificates_CreateOrUpdate

```java
import com.azure.resourcemanager.appplatform.generated.models.KeyVaultCertificateProperties;

/** Samples for Certificates CreateOrUpdate. */
public final class CertificatesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Certificates_CreateOrUpdate.json
     */
    /**
     * Sample code: Certificates_CreateOrUpdate.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void certificatesCreateOrUpdate(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .certificates()
            .define("mycertificate")
            .withExistingSpring("myResourceGroup", "myservice")
            .withProperties(
                new KeyVaultCertificateProperties()
                    .withVaultUri("https://myvault.vault.azure.net")
                    .withKeyVaultCertName("mycert")
                    .withCertVersion("08a219d06d874795a96db47e06fbb01e"))
            .create();
    }
}
```

### Certificates_Delete

```java
import com.azure.core.util.Context;

/** Samples for Certificates Delete. */
public final class CertificatesDeleteSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Certificates_Delete.json
     */
    /**
     * Sample code: Certificates_Delete.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void certificatesDelete(com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.certificates().delete("myResourceGroup", "myservice", "mycertificate", Context.NONE);
    }
}
```

### Certificates_Get

```java
import com.azure.core.util.Context;

/** Samples for Certificates Get. */
public final class CertificatesGetSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Certificates_Get.json
     */
    /**
     * Sample code: Certificates_Get.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void certificatesGet(com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.certificates().getWithResponse("myResourceGroup", "myservice", "mycertificate", Context.NONE);
    }
}
```

### Certificates_List

```java
import com.azure.core.util.Context;

/** Samples for Certificates List. */
public final class CertificatesListSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Certificates_List.json
     */
    /**
     * Sample code: Certificates_List.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void certificatesList(com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.certificates().list("myResourceGroup", "myService", Context.NONE);
    }
}
```

### ConfigServers_Get

```java
import com.azure.core.util.Context;

/** Samples for ConfigServers Get. */
public final class ConfigServersGetSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/ConfigServers_Get.json
     */
    /**
     * Sample code: ConfigServers_Get.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void configServersGet(com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.configServers().getWithResponse("myResourceGroup", "myservice", Context.NONE);
    }
}
```

### ConfigServers_UpdatePatch

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.appplatform.generated.fluent.models.ConfigServerResourceInner;
import com.azure.resourcemanager.appplatform.generated.models.ConfigServerGitProperty;
import com.azure.resourcemanager.appplatform.generated.models.ConfigServerProperties;
import com.azure.resourcemanager.appplatform.generated.models.ConfigServerSettings;
import java.util.Arrays;

/** Samples for ConfigServers UpdatePatch. */
public final class ConfigServersUpdatePatchSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/ConfigServers_UpdatePatch.json
     */
    /**
     * Sample code: ConfigServers_UpdatePatch.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void configServersUpdatePatch(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .configServers()
            .updatePatch(
                "myResourceGroup",
                "myservice",
                new ConfigServerResourceInner()
                    .withProperties(
                        new ConfigServerProperties()
                            .withConfigServer(
                                new ConfigServerSettings()
                                    .withGitProperty(
                                        new ConfigServerGitProperty()
                                            .withUri("https://github.com/fake-user/fake-repository.git")
                                            .withLabel("master")
                                            .withSearchPaths(Arrays.asList("/"))))),
                Context.NONE);
    }
}
```

### ConfigServers_UpdatePut

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.appplatform.generated.fluent.models.ConfigServerResourceInner;
import com.azure.resourcemanager.appplatform.generated.models.ConfigServerGitProperty;
import com.azure.resourcemanager.appplatform.generated.models.ConfigServerProperties;
import com.azure.resourcemanager.appplatform.generated.models.ConfigServerSettings;
import java.util.Arrays;

/** Samples for ConfigServers UpdatePut. */
public final class ConfigServersUpdatePutSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/ConfigServers_UpdatePut.json
     */
    /**
     * Sample code: ConfigServers_UpdatePut.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void configServersUpdatePut(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .configServers()
            .updatePut(
                "myResourceGroup",
                "myservice",
                new ConfigServerResourceInner()
                    .withProperties(
                        new ConfigServerProperties()
                            .withConfigServer(
                                new ConfigServerSettings()
                                    .withGitProperty(
                                        new ConfigServerGitProperty()
                                            .withUri("https://github.com/fake-user/fake-repository.git")
                                            .withLabel("master")
                                            .withSearchPaths(Arrays.asList("/"))))),
                Context.NONE);
    }
}
```

### ConfigServers_Validate

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.appplatform.generated.models.ConfigServerGitProperty;
import com.azure.resourcemanager.appplatform.generated.models.ConfigServerSettings;
import java.util.Arrays;

/** Samples for ConfigServers Validate. */
public final class ConfigServersValidateSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/ConfigServers_Validate.json
     */
    /**
     * Sample code: ConfigServers_Validate.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void configServersValidate(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .configServers()
            .validate(
                "myResourceGroup",
                "myservice",
                new ConfigServerSettings()
                    .withGitProperty(
                        new ConfigServerGitProperty()
                            .withUri("https://github.com/fake-user/fake-repository.git")
                            .withLabel("master")
                            .withSearchPaths(Arrays.asList("/"))),
                Context.NONE);
    }
}
```

### ConfigurationServices_CreateOrUpdate

```java
import com.azure.resourcemanager.appplatform.generated.models.ConfigurationServiceGitProperty;
import com.azure.resourcemanager.appplatform.generated.models.ConfigurationServiceGitRepository;
import com.azure.resourcemanager.appplatform.generated.models.ConfigurationServiceProperties;
import com.azure.resourcemanager.appplatform.generated.models.ConfigurationServiceSettings;
import java.util.Arrays;

/** Samples for ConfigurationServices CreateOrUpdate. */
public final class ConfigurationServicesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/ConfigurationServices_CreateOrUpdate.json
     */
    /**
     * Sample code: ConfigurationServices_CreateOrUpdate.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void configurationServicesCreateOrUpdate(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .configurationServices()
            .define("default")
            .withExistingSpring("myResourceGroup", "myservice")
            .withProperties(
                new ConfigurationServiceProperties()
                    .withSettings(
                        new ConfigurationServiceSettings()
                            .withGitProperty(
                                new ConfigurationServiceGitProperty()
                                    .withRepositories(
                                        Arrays
                                            .asList(
                                                new ConfigurationServiceGitRepository()
                                                    .withName("fake")
                                                    .withPatterns(Arrays.asList("app/dev"))
                                                    .withUri("https://github.com/fake-user/fake-repository")
                                                    .withLabel("master"))))))
            .create();
    }
}
```

### ConfigurationServices_Delete

```java
import com.azure.core.util.Context;

/** Samples for ConfigurationServices Delete. */
public final class ConfigurationServicesDeleteSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/ConfigurationServices_Delete.json
     */
    /**
     * Sample code: ConfigurationServices_Delete.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void configurationServicesDelete(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.configurationServices().delete("myResourceGroup", "myservice", "default", Context.NONE);
    }
}
```

### ConfigurationServices_Get

```java
import com.azure.core.util.Context;

/** Samples for ConfigurationServices Get. */
public final class ConfigurationServicesGetSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/ConfigurationServices_Get.json
     */
    /**
     * Sample code: ConfigurationServices_Get.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void configurationServicesGet(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.configurationServices().getWithResponse("myResourceGroup", "myservice", "default", Context.NONE);
    }
}
```

### ConfigurationServices_List

```java
import com.azure.core.util.Context;

/** Samples for ConfigurationServices List. */
public final class ConfigurationServicesListSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/ConfigurationServices_List.json
     */
    /**
     * Sample code: ConfigurationServices_List.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void configurationServicesList(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.configurationServices().list("myResourceGroup", "myservice", Context.NONE);
    }
}
```

### ConfigurationServices_Validate

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.appplatform.generated.models.ConfigurationServiceGitProperty;
import com.azure.resourcemanager.appplatform.generated.models.ConfigurationServiceGitRepository;
import com.azure.resourcemanager.appplatform.generated.models.ConfigurationServiceSettings;
import java.util.Arrays;

/** Samples for ConfigurationServices Validate. */
public final class ConfigurationServicesValidateSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/ConfigurationServices_Validate.json
     */
    /**
     * Sample code: ConfigurationServices_Validate.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void configurationServicesValidate(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .configurationServices()
            .validate(
                "myResourceGroup",
                "myservice",
                "default",
                new ConfigurationServiceSettings()
                    .withGitProperty(
                        new ConfigurationServiceGitProperty()
                            .withRepositories(
                                Arrays
                                    .asList(
                                        new ConfigurationServiceGitRepository()
                                            .withName("fake")
                                            .withPatterns(Arrays.asList("app/dev"))
                                            .withUri("https://github.com/fake-user/fake-repository")
                                            .withLabel("master")))),
                Context.NONE);
    }
}
```

### CustomDomains_CreateOrUpdate

```java
import com.azure.resourcemanager.appplatform.generated.models.CustomDomainProperties;

/** Samples for CustomDomains CreateOrUpdate. */
public final class CustomDomainsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/CustomDomains_CreateOrUpdate.json
     */
    /**
     * Sample code: CustomDomains_CreateOrUpdate.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void customDomainsCreateOrUpdate(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .customDomains()
            .define("mydomain.com")
            .withExistingApp("myResourceGroup", "myservice", "myapp")
            .withProperties(
                new CustomDomainProperties()
                    .withThumbprint("934367bf1c97033f877db0f15cb1b586957d3133")
                    .withCertName("mycert"))
            .create();
    }
}
```

### CustomDomains_Delete

```java
import com.azure.core.util.Context;

/** Samples for CustomDomains Delete. */
public final class CustomDomainsDeleteSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/CustomDomains_Delete.json
     */
    /**
     * Sample code: CustomDomains_Delete.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void customDomainsDelete(com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.customDomains().delete("myResourceGroup", "myservice", "myapp", "mydomain.com", Context.NONE);
    }
}
```

### CustomDomains_Get

```java
import com.azure.core.util.Context;

/** Samples for CustomDomains Get. */
public final class CustomDomainsGetSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/CustomDomains_Get.json
     */
    /**
     * Sample code: CustomDomains_Get.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void customDomainsGet(com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.customDomains().getWithResponse("myResourceGroup", "myservice", "myapp", "mydomain.com", Context.NONE);
    }
}
```

### CustomDomains_List

```java
import com.azure.core.util.Context;

/** Samples for CustomDomains List. */
public final class CustomDomainsListSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/CustomDomains_List.json
     */
    /**
     * Sample code: CustomDomains_List.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void customDomainsList(com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.customDomains().list("myResourceGroup", "myservice", "myapp", Context.NONE);
    }
}
```

### CustomDomains_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.appplatform.generated.models.CustomDomainProperties;
import com.azure.resourcemanager.appplatform.generated.models.CustomDomainResource;

/** Samples for CustomDomains Update. */
public final class CustomDomainsUpdateSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/CustomDomains_Update.json
     */
    /**
     * Sample code: CustomDomains_Update.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void customDomainsUpdate(com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        CustomDomainResource resource =
            manager
                .customDomains()
                .getWithResponse("myResourceGroup", "myservice", "myapp", "mydomain.com", Context.NONE)
                .getValue();
        resource
            .update()
            .withProperties(
                new CustomDomainProperties()
                    .withThumbprint("934367bf1c97033f877db0f15cb1b586957d3133")
                    .withCertName("mycert"))
            .apply();
    }
}
```

### Deployments_CreateOrUpdate

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.appplatform.generated.models.CustomContainer;
import com.azure.resourcemanager.appplatform.generated.models.CustomContainerUserSourceInfo;
import com.azure.resourcemanager.appplatform.generated.models.DeploymentResourceProperties;
import com.azure.resourcemanager.appplatform.generated.models.DeploymentSettings;
import com.azure.resourcemanager.appplatform.generated.models.HttpGetAction;
import com.azure.resourcemanager.appplatform.generated.models.HttpSchemeType;
import com.azure.resourcemanager.appplatform.generated.models.ImageRegistryCredential;
import com.azure.resourcemanager.appplatform.generated.models.Probe;
import com.azure.resourcemanager.appplatform.generated.models.ResourceRequests;
import com.azure.resourcemanager.appplatform.generated.models.Sku;
import com.azure.resourcemanager.appplatform.generated.models.SourceUploadedUserSourceInfo;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Deployments CreateOrUpdate. */
public final class DeploymentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Deployments_CreateOrUpdate.json
     */
    /**
     * Sample code: Deployments_CreateOrUpdate.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void deploymentsCreateOrUpdate(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) throws IOException {
        manager
            .deployments()
            .define("mydeployment")
            .withExistingApp("myResourceGroup", "myservice", "myapp")
            .withProperties(
                new DeploymentResourceProperties()
                    .withSource(
                        new SourceUploadedUserSourceInfo()
                            .withVersion("1.0")
                            .withRelativePath(
                                "resources/a172cedcae47474b615c54d510a5d84a8dea3032e958587430b413538be3f333-2019082605-e3095339-1723-44b7-8b5e-31b1003978bc")
                            .withArtifactSelector("sub-module-1"))
                    .withDeploymentSettings(
                        new DeploymentSettings()
                            .withResourceRequests(new ResourceRequests().withCpu("1000m").withMemory("3Gi"))
                            .withEnvironmentVariables(mapOf("env", "test"))
                            .withAddonConfigs(
                                mapOf(
                                    "ApplicationConfigurationService",
                                    mapOf(
                                        "patterns",
                                        SerializerFactory
                                            .createDefaultManagementSerializerAdapter()
                                            .deserialize("[\"mypattern\"]", Object.class, SerializerEncoding.JSON))))
                            .withLivenessProbe(
                                new Probe()
                                    .withProbeAction(
                                        new HttpGetAction().withPath("/health").withScheme(HttpSchemeType.HTTP))
                                    .withDisableProbe(false)
                                    .withInitialDelaySeconds(30)
                                    .withPeriodSeconds(10)
                                    .withFailureThreshold(3))
                            .withReadinessProbe(
                                new Probe()
                                    .withProbeAction(
                                        new HttpGetAction().withPath("/health").withScheme(HttpSchemeType.HTTP))
                                    .withDisableProbe(false)
                                    .withInitialDelaySeconds(30)
                                    .withPeriodSeconds(10)
                                    .withFailureThreshold(3))
                            .withTerminationGracePeriodSeconds(30)))
            .withSku(new Sku().withName("S0").withTier("Standard").withCapacity(1))
            .create();
    }

    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Deployments_CreateOrUpdate_CustomContainer.json
     */
    /**
     * Sample code: Deployments_CreateOrUpdate_CustomContainer.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void deploymentsCreateOrUpdateCustomContainer(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .deployments()
            .define("mydeployment")
            .withExistingApp("myResourceGroup", "myservice", "myapp")
            .withProperties(
                new DeploymentResourceProperties()
                    .withSource(
                        new CustomContainerUserSourceInfo()
                            .withCustomContainer(
                                new CustomContainer()
                                    .withServer("myacr.azurecr.io")
                                    .withContainerImage("myContainerImage:v1")
                                    .withCommand(Arrays.asList("/bin/sh"))
                                    .withArgs(Arrays.asList("-c", "while true; do echo hello; sleep 10;done"))
                                    .withImageRegistryCredential(
                                        new ImageRegistryCredential()
                                            .withUsername("myUsername")
                                            .withPassword("myPassword"))
                                    .withLanguageFramework("springboot")))
                    .withDeploymentSettings(
                        new DeploymentSettings()
                            .withResourceRequests(new ResourceRequests().withCpu("1000m").withMemory("3Gi"))
                            .withEnvironmentVariables(mapOf("env", "test"))
                            .withLivenessProbe(
                                new Probe()
                                    .withProbeAction(
                                        new HttpGetAction().withPath("/health").withScheme(HttpSchemeType.HTTP))
                                    .withDisableProbe(false)
                                    .withInitialDelaySeconds(30)
                                    .withPeriodSeconds(10)
                                    .withFailureThreshold(3))
                            .withReadinessProbe(
                                new Probe()
                                    .withProbeAction(
                                        new HttpGetAction().withPath("/health").withScheme(HttpSchemeType.HTTP))
                                    .withDisableProbe(false)
                                    .withInitialDelaySeconds(30)
                                    .withPeriodSeconds(10)
                                    .withFailureThreshold(3))
                            .withTerminationGracePeriodSeconds(30)))
            .create();
    }

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

### Deployments_Delete

```java
import com.azure.core.util.Context;

/** Samples for Deployments Delete. */
public final class DeploymentsDeleteSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Deployments_Delete.json
     */
    /**
     * Sample code: Deployments_Delete.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void deploymentsDelete(com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.deployments().delete("myResourceGroup", "myservice", "myapp", "mydeployment", Context.NONE);
    }
}
```

### Deployments_GenerateHeapDump

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.appplatform.generated.models.DiagnosticParameters;

/** Samples for Deployments GenerateHeapDump. */
public final class DeploymentsGenerateHeapDumpSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Deployments_GenerateHeapDump.json
     */
    /**
     * Sample code: Deployments_GenerateHeapDump.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void deploymentsGenerateHeapDump(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .deployments()
            .generateHeapDump(
                "myResourceGroup",
                "myservice",
                "myapp",
                "mydeployment",
                new DiagnosticParameters().withAppInstance("myappinstance").withFilePath("/byos/diagnose"),
                Context.NONE);
    }
}
```

### Deployments_GenerateThreadDump

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.appplatform.generated.models.DiagnosticParameters;

/** Samples for Deployments GenerateThreadDump. */
public final class DeploymentsGenerateThreadDumpSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Deployments_GenerateThreadDump.json
     */
    /**
     * Sample code: Deployments_GenerateHeapDump.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void deploymentsGenerateHeapDump(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .deployments()
            .generateThreadDump(
                "myResourceGroup",
                "myservice",
                "myapp",
                "mydeployment",
                new DiagnosticParameters().withAppInstance("myappinstance").withFilePath("/byos/diagnose"),
                Context.NONE);
    }
}
```

### Deployments_Get

```java
import com.azure.core.util.Context;

/** Samples for Deployments Get. */
public final class DeploymentsGetSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Deployments_Get.json
     */
    /**
     * Sample code: Deployments_Get.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void deploymentsGet(com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.deployments().getWithResponse("myResourceGroup", "myservice", "myapp", "mydeployment", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Deployments_Get_CustomContainer.json
     */
    /**
     * Sample code: Deployments_Get_CustomContainer.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void deploymentsGetCustomContainer(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.deployments().getWithResponse("myResourceGroup", "myservice", "myapp", "mydeployment", Context.NONE);
    }
}
```

### Deployments_GetLogFileUrl

```java
import com.azure.core.util.Context;

/** Samples for Deployments GetLogFileUrl. */
public final class DeploymentsGetLogFileUrlSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Deployments_GetLogFileUrl.json
     */
    /**
     * Sample code: Deployments_GetLogFileUrl.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void deploymentsGetLogFileUrl(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .deployments()
            .getLogFileUrlWithResponse("myResourceGroup", "myservice", "myapp", "mydeployment", Context.NONE);
    }
}
```

### Deployments_List

```java
import com.azure.core.util.Context;

/** Samples for Deployments List. */
public final class DeploymentsListSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Deployments_List.json
     */
    /**
     * Sample code: Deployments_List.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void deploymentsList(com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.deployments().list("myResourceGroup", "myservice", "myapp", null, Context.NONE);
    }
}
```

### Deployments_ListForCluster

```java
import com.azure.core.util.Context;

/** Samples for Deployments ListForCluster. */
public final class DeploymentsListForClusterSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Deployments_ListForCluster.json
     */
    /**
     * Sample code: Deployments_ListForCluster.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void deploymentsListForCluster(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.deployments().listForCluster("myResourceGroup", "myservice", null, Context.NONE);
    }
}
```

### Deployments_Restart

```java
import com.azure.core.util.Context;

/** Samples for Deployments Restart. */
public final class DeploymentsRestartSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Deployments_Restart.json
     */
    /**
     * Sample code: Deployments_Restart.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void deploymentsRestart(com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.deployments().restart("myResourceGroup", "myservice", "myapp", "mydeployment", Context.NONE);
    }
}
```

### Deployments_Start

```java
import com.azure.core.util.Context;

/** Samples for Deployments Start. */
public final class DeploymentsStartSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Deployments_Start.json
     */
    /**
     * Sample code: Deployments_Start.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void deploymentsStart(com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.deployments().start("myResourceGroup", "myservice", "myapp", "mydeployment", Context.NONE);
    }
}
```

### Deployments_StartJfr

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.appplatform.generated.models.DiagnosticParameters;

/** Samples for Deployments StartJfr. */
public final class DeploymentsStartJfrSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Deployments_StartJFR.json
     */
    /**
     * Sample code: Deployments_StartJFR.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void deploymentsStartJFR(com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .deployments()
            .startJfr(
                "myResourceGroup",
                "myservice",
                "myapp",
                "mydeployment",
                new DiagnosticParameters()
                    .withAppInstance("myappinstance")
                    .withFilePath("/byos/diagnose")
                    .withDuration("60s"),
                Context.NONE);
    }
}
```

### Deployments_Stop

```java
import com.azure.core.util.Context;

/** Samples for Deployments Stop. */
public final class DeploymentsStopSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Deployments_Stop.json
     */
    /**
     * Sample code: Deployments_Stop.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void deploymentsStop(com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.deployments().stop("myResourceGroup", "myservice", "myapp", "mydeployment", Context.NONE);
    }
}
```

### Deployments_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.appplatform.generated.models.CustomContainer;
import com.azure.resourcemanager.appplatform.generated.models.CustomContainerUserSourceInfo;
import com.azure.resourcemanager.appplatform.generated.models.DeploymentResource;
import com.azure.resourcemanager.appplatform.generated.models.DeploymentResourceProperties;
import com.azure.resourcemanager.appplatform.generated.models.ImageRegistryCredential;
import com.azure.resourcemanager.appplatform.generated.models.SourceUploadedUserSourceInfo;
import java.util.Arrays;

/** Samples for Deployments Update. */
public final class DeploymentsUpdateSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Deployments_Update.json
     */
    /**
     * Sample code: Deployments_Update.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void deploymentsUpdate(com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        DeploymentResource resource =
            manager
                .deployments()
                .getWithResponse("myResourceGroup", "myservice", "myapp", "mydeployment", Context.NONE)
                .getValue();
        resource
            .update()
            .withProperties(
                new DeploymentResourceProperties()
                    .withSource(
                        new SourceUploadedUserSourceInfo()
                            .withVersion("1.0")
                            .withRelativePath(
                                "resources/a172cedcae47474b615c54d510a5d84a8dea3032e958587430b413538be3f333-2019082605-e3095339-1723-44b7-8b5e-31b1003978bc")
                            .withArtifactSelector("sub-module-1")))
            .apply();
    }

    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Deployments_Update_CustomContainer.json
     */
    /**
     * Sample code: Deployments_Update_CustomContainer.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void deploymentsUpdateCustomContainer(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        DeploymentResource resource =
            manager
                .deployments()
                .getWithResponse("myResourceGroup", "myservice", "myapp", "mydeployment", Context.NONE)
                .getValue();
        resource
            .update()
            .withProperties(
                new DeploymentResourceProperties()
                    .withSource(
                        new CustomContainerUserSourceInfo()
                            .withCustomContainer(
                                new CustomContainer()
                                    .withServer("mynewacr.azurecr.io")
                                    .withContainerImage("myNewContainerImage:v1")
                                    .withCommand(Arrays.asList("/bin/sh"))
                                    .withArgs(Arrays.asList("-c", "while true; do echo hello; sleep 10;done"))
                                    .withImageRegistryCredential(
                                        new ImageRegistryCredential()
                                            .withUsername("myNewUsername")
                                            .withPassword("<myNewPassword>")))))
            .apply();
    }
}
```

### GatewayCustomDomains_CreateOrUpdate

```java
import com.azure.resourcemanager.appplatform.generated.models.GatewayCustomDomainProperties;

/** Samples for GatewayCustomDomains CreateOrUpdate. */
public final class GatewayCustomDomainsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/GatewayCustomDomains_CreateOrUpdate.json
     */
    /**
     * Sample code: GatewayCustomDomains_CreateOrUpdate.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void gatewayCustomDomainsCreateOrUpdate(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .gatewayCustomDomains()
            .define("myDomainName")
            .withExistingGateway("myResourceGroup", "myservice", "default")
            .withProperties(new GatewayCustomDomainProperties().withThumbprint("*"))
            .create();
    }
}
```

### GatewayCustomDomains_Delete

```java
import com.azure.core.util.Context;

/** Samples for GatewayCustomDomains Delete. */
public final class GatewayCustomDomainsDeleteSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/GatewayCustomDomains_Delete.json
     */
    /**
     * Sample code: GatewayCustomDomains_Delete.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void gatewayCustomDomainsDelete(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.gatewayCustomDomains().delete("myResourceGroup", "myservice", "default", "myDomainName", Context.NONE);
    }
}
```

### GatewayCustomDomains_Get

```java
import com.azure.core.util.Context;

/** Samples for GatewayCustomDomains Get. */
public final class GatewayCustomDomainsGetSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/GatewayCustomDomains_Get.json
     */
    /**
     * Sample code: GatewayCustomDomains_Get.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void gatewayCustomDomainsGet(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .gatewayCustomDomains()
            .getWithResponse("myResourceGroup", "myservice", "default", "myDomainName", Context.NONE);
    }
}
```

### GatewayCustomDomains_List

```java
import com.azure.core.util.Context;

/** Samples for GatewayCustomDomains List. */
public final class GatewayCustomDomainsListSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/GatewayCustomDomains_List.json
     */
    /**
     * Sample code: GatewayCustomDomains_List.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void gatewayCustomDomainsList(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.gatewayCustomDomains().list("myResourceGroup", "myservice", "default", Context.NONE);
    }
}
```

### GatewayRouteConfigs_CreateOrUpdate

```java
import com.azure.resourcemanager.appplatform.generated.models.GatewayApiRoute;
import com.azure.resourcemanager.appplatform.generated.models.GatewayRouteConfigOpenApiProperties;
import com.azure.resourcemanager.appplatform.generated.models.GatewayRouteConfigProperties;
import java.util.Arrays;

/** Samples for GatewayRouteConfigs CreateOrUpdate. */
public final class GatewayRouteConfigsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/GatewayRouteConfigs_CreateOrUpdate.json
     */
    /**
     * Sample code: GatewayRouteConfigs_CreateOrUpdate.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void gatewayRouteConfigsCreateOrUpdate(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .gatewayRouteConfigs()
            .define("myRouteConfig")
            .withExistingGateway("myResourceGroup", "myservice", "default")
            .withProperties(
                new GatewayRouteConfigProperties()
                    .withAppResourceId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.AppPlatform/Spring/myservice/apps/myApp")
                    .withOpenApi(
                        new GatewayRouteConfigOpenApiProperties()
                            .withUri(
                                "https://raw.githubusercontent.com/OAI/OpenAPI-Specification/main/examples/v3.0/petstore.json"))
                    .withRoutes(
                        Arrays
                            .asList(
                                new GatewayApiRoute()
                                    .withTitle("myApp route config")
                                    .withSsoEnabled(true)
                                    .withPredicates(Arrays.asList("Path=/api5/customer/**"))
                                    .withFilters(Arrays.asList("StripPrefix=2", "RateLimit=1,1s")))))
            .create();
    }
}
```

### GatewayRouteConfigs_Delete

```java
import com.azure.core.util.Context;

/** Samples for GatewayRouteConfigs Delete. */
public final class GatewayRouteConfigsDeleteSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/GatewayRouteConfigs_Delete.json
     */
    /**
     * Sample code: GatewayRouteConfigs_Delete.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void gatewayRouteConfigsDelete(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.gatewayRouteConfigs().delete("myResourceGroup", "myservice", "default", "myRouteConfig", Context.NONE);
    }
}
```

### GatewayRouteConfigs_Get

```java
import com.azure.core.util.Context;

/** Samples for GatewayRouteConfigs Get. */
public final class GatewayRouteConfigsGetSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/GatewayRouteConfigs_Get.json
     */
    /**
     * Sample code: GatewayRouteConfigs_Get.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void gatewayRouteConfigsGet(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .gatewayRouteConfigs()
            .getWithResponse("myResourceGroup", "myservice", "default", "myRouteConfig", Context.NONE);
    }
}
```

### GatewayRouteConfigs_List

```java
import com.azure.core.util.Context;

/** Samples for GatewayRouteConfigs List. */
public final class GatewayRouteConfigsListSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/GatewayRouteConfigs_List.json
     */
    /**
     * Sample code: GatewayRouteConfigs_List.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void gatewayRouteConfigsList(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.gatewayRouteConfigs().list("myResourceGroup", "myservice", "default", Context.NONE);
    }
}
```

### Gateways_CreateOrUpdate

```java
import com.azure.resourcemanager.appplatform.generated.models.GatewayProperties;
import com.azure.resourcemanager.appplatform.generated.models.GatewayResourceRequests;
import com.azure.resourcemanager.appplatform.generated.models.Sku;

/** Samples for Gateways CreateOrUpdate. */
public final class GatewaysCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Gateways_CreateOrUpdate.json
     */
    /**
     * Sample code: Gateways_CreateOrUpdate.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void gatewaysCreateOrUpdate(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .gateways()
            .define("default")
            .withExistingSpring("myResourceGroup", "myservice")
            .withProperties(
                new GatewayProperties()
                    .withPublicProperty(true)
                    .withResourceRequests(new GatewayResourceRequests().withCpu("1").withMemory("1G")))
            .withSku(new Sku().withName("E0").withTier("Enterprise").withCapacity(2))
            .create();
    }
}
```

### Gateways_Delete

```java
import com.azure.core.util.Context;

/** Samples for Gateways Delete. */
public final class GatewaysDeleteSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Gateways_Delete.json
     */
    /**
     * Sample code: Gateways_Delete.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void gatewaysDelete(com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.gateways().delete("myResourceGroup", "myservice", "default", Context.NONE);
    }
}
```

### Gateways_Get

```java
import com.azure.core.util.Context;

/** Samples for Gateways Get. */
public final class GatewaysGetSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Gateways_Get.json
     */
    /**
     * Sample code: Gateways_Get.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void gatewaysGet(com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.gateways().getWithResponse("myResourceGroup", "myservice", "default", Context.NONE);
    }
}
```

### Gateways_List

```java
import com.azure.core.util.Context;

/** Samples for Gateways List. */
public final class GatewaysListSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Gateways_List.json
     */
    /**
     * Sample code: Gateways_List.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void gatewaysList(com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.gateways().list("myResourceGroup", "myservice", Context.NONE);
    }
}
```

### Gateways_ValidateDomain

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.appplatform.generated.models.CustomDomainValidatePayload;

/** Samples for Gateways ValidateDomain. */
public final class GatewaysValidateDomainSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Gateways_ValidateDomain.json
     */
    /**
     * Sample code: Gateways_ValidateDomain.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void gatewaysValidateDomain(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .gateways()
            .validateDomainWithResponse(
                "myResourceGroup",
                "myservice",
                "default",
                new CustomDomainValidatePayload().withName("mydomain.io"),
                Context.NONE);
    }
}
```

### MonitoringSettings_Get

```java
import com.azure.core.util.Context;

/** Samples for MonitoringSettings Get. */
public final class MonitoringSettingsGetSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/MonitoringSettings_Get.json
     */
    /**
     * Sample code: MonitoringSettings_Get.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void monitoringSettingsGet(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.monitoringSettings().getWithResponse("myResourceGroup", "myservice", Context.NONE);
    }
}
```

### MonitoringSettings_UpdatePatch

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.appplatform.generated.fluent.models.MonitoringSettingResourceInner;
import com.azure.resourcemanager.appplatform.generated.models.MonitoringSettingProperties;

/** Samples for MonitoringSettings UpdatePatch. */
public final class MonitoringSettingsUpdatePatchSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/MonitoringSettings_UpdatePatch.json
     */
    /**
     * Sample code: MonitoringSettings_UpdatePatch.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void monitoringSettingsUpdatePatch(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .monitoringSettings()
            .updatePatch(
                "myResourceGroup",
                "myservice",
                new MonitoringSettingResourceInner()
                    .withProperties(
                        new MonitoringSettingProperties()
                            .withTraceEnabled(true)
                            .withAppInsightsInstrumentationKey("00000000-0000-0000-0000-000000000000")
                            .withAppInsightsSamplingRate(10.0)),
                Context.NONE);
    }
}
```

### MonitoringSettings_UpdatePut

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.appplatform.generated.fluent.models.MonitoringSettingResourceInner;
import com.azure.resourcemanager.appplatform.generated.models.MonitoringSettingProperties;

/** Samples for MonitoringSettings UpdatePut. */
public final class MonitoringSettingsUpdatePutSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/MonitoringSettings_UpdatePut.json
     */
    /**
     * Sample code: MonitoringSettings_UpdatePut.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void monitoringSettingsUpdatePut(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .monitoringSettings()
            .updatePut(
                "myResourceGroup",
                "myservice",
                new MonitoringSettingResourceInner()
                    .withProperties(
                        new MonitoringSettingProperties()
                            .withTraceEnabled(true)
                            .withAppInsightsInstrumentationKey("00000000-0000-0000-0000-000000000000")
                            .withAppInsightsSamplingRate(10.0)),
                Context.NONE);
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void operationsList(com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### RuntimeVersions_ListRuntimeVersions

```java
import com.azure.core.util.Context;

/** Samples for RuntimeVersions ListRuntimeVersions. */
public final class RuntimeVersionsListRuntimeVersionsSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/RuntimeVersions_ListRuntimeVersions.json
     */
    /**
     * Sample code: RuntimeVersions_ListRuntimeVersions.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void runtimeVersionsListRuntimeVersions(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.runtimeVersions().listRuntimeVersionsWithResponse(Context.NONE);
    }
}
```

### ServiceRegistries_CreateOrUpdate

```java
import com.azure.core.util.Context;

/** Samples for ServiceRegistries CreateOrUpdate. */
public final class ServiceRegistriesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/ServiceRegistries_CreateOrUpdate.json
     */
    /**
     * Sample code: ServiceRegistries_CreateOrUpdate.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void serviceRegistriesCreateOrUpdate(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.serviceRegistries().createOrUpdate("myResourceGroup", "myservice", "default", Context.NONE);
    }
}
```

### ServiceRegistries_Delete

```java
import com.azure.core.util.Context;

/** Samples for ServiceRegistries Delete. */
public final class ServiceRegistriesDeleteSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/ServiceRegistries_Delete.json
     */
    /**
     * Sample code: ServiceRegistries_Delete.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void serviceRegistriesDelete(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.serviceRegistries().delete("myResourceGroup", "myservice", "default", Context.NONE);
    }
}
```

### ServiceRegistries_Get

```java
import com.azure.core.util.Context;

/** Samples for ServiceRegistries Get. */
public final class ServiceRegistriesGetSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/ServiceRegistries_Get.json
     */
    /**
     * Sample code: ServiceRegistries_Get.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void serviceRegistriesGet(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.serviceRegistries().getWithResponse("myResourceGroup", "myservice", "default", Context.NONE);
    }
}
```

### ServiceRegistries_List

```java
import com.azure.core.util.Context;

/** Samples for ServiceRegistries List. */
public final class ServiceRegistriesListSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/ServiceRegistries_List.json
     */
    /**
     * Sample code: ServiceRegistries_List.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void serviceRegistriesList(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.serviceRegistries().list("myResourceGroup", "myservice", Context.NONE);
    }
}
```

### Services_CheckNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.appplatform.generated.models.NameAvailabilityParameters;

/** Samples for Services CheckNameAvailability. */
public final class ServicesCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Services_CheckNameAvailability.json
     */
    /**
     * Sample code: Services_CheckNameAvailability.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void servicesCheckNameAvailability(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .services()
            .checkNameAvailabilityWithResponse(
                "eastus",
                new NameAvailabilityParameters().withType("Microsoft.AppPlatform/Spring").withName("myservice"),
                Context.NONE);
    }
}
```

### Services_CreateOrUpdate

```java
import com.azure.resourcemanager.appplatform.generated.models.ClusterResourceProperties;
import com.azure.resourcemanager.appplatform.generated.models.IngressConfig;
import com.azure.resourcemanager.appplatform.generated.models.MarketplaceResource;
import com.azure.resourcemanager.appplatform.generated.models.NetworkProfile;
import com.azure.resourcemanager.appplatform.generated.models.ServiceVNetAddons;
import com.azure.resourcemanager.appplatform.generated.models.Sku;
import java.util.HashMap;
import java.util.Map;

/** Samples for Services CreateOrUpdate. */
public final class ServicesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Services_CreateOrUpdate_VNetInjection.json
     */
    /**
     * Sample code: Services_CreateOrUpdate_VNetInjection.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void servicesCreateOrUpdateVNetInjection(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .services()
            .define("myservice")
            .withRegion("eastus")
            .withExistingResourceGroup("myResourceGroup")
            .withTags(mapOf("key1", "value1"))
            .withProperties(
                new ClusterResourceProperties()
                    .withNetworkProfile(
                        new NetworkProfile()
                            .withServiceRuntimeSubnetId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.Network/virtualNetworks/myVirtualNetwork/subnets/serviceRuntime")
                            .withAppSubnetId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.Network/virtualNetworks/myVirtualNetwork/subnets/apps")
                            .withServiceCidr("10.8.0.0/16,10.244.0.0/16,10.245.0.1/16")
                            .withServiceRuntimeNetworkResourceGroup("my-service-runtime-network-rg")
                            .withAppNetworkResourceGroup("my-app-network-rg")
                            .withIngressConfig(new IngressConfig().withReadTimeoutInSeconds(300)))
                    .withVnetAddons(new ServiceVNetAddons().withLogStreamPublicEndpoint(true)))
            .withSku(new Sku().withName("S0").withTier("Standard"))
            .create();
    }

    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Services_CreateOrUpdate.json
     */
    /**
     * Sample code: Services_CreateOrUpdate.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void servicesCreateOrUpdate(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .services()
            .define("myservice")
            .withRegion("eastus")
            .withExistingResourceGroup("myResourceGroup")
            .withTags(mapOf("key1", "value1"))
            .withProperties(new ClusterResourceProperties())
            .withSku(new Sku().withName("S0").withTier("Standard"))
            .create();
    }

    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Services_CreateOrUpdate_Enterprise.json
     */
    /**
     * Sample code: Services_CreateOrUpdate_Enterprise.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void servicesCreateOrUpdateEnterprise(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .services()
            .define("myservice")
            .withRegion("eastus")
            .withExistingResourceGroup("myResourceGroup")
            .withTags(mapOf("key1", "value1"))
            .withProperties(
                new ClusterResourceProperties()
                    .withMarketplaceResource(
                        new MarketplaceResource()
                            .withPlan("tanzu-asc-ent-mtr")
                            .withPublisher("vmware-inc")
                            .withProduct("azure-spring-cloud-vmware-tanzu-2")))
            .withSku(new Sku().withName("E0").withTier("Enterprise"))
            .create();
    }

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

### Services_Delete

```java
import com.azure.core.util.Context;

/** Samples for Services Delete. */
public final class ServicesDeleteSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Services_Delete.json
     */
    /**
     * Sample code: Services_Delete.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void servicesDelete(com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.services().delete("myResourceGroup", "myservice", Context.NONE);
    }
}
```

### Services_DisableTestEndpoint

```java
import com.azure.core.util.Context;

/** Samples for Services DisableTestEndpoint. */
public final class ServicesDisableTestEndpointSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Services_DisableTestEndpoint.json
     */
    /**
     * Sample code: Services_DisableTestEndpoint.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void servicesDisableTestEndpoint(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.services().disableTestEndpointWithResponse("myResourceGroup", "myservice", Context.NONE);
    }
}
```

### Services_EnableTestEndpoint

```java
import com.azure.core.util.Context;

/** Samples for Services EnableTestEndpoint. */
public final class ServicesEnableTestEndpointSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Services_EnableTestEndpoint.json
     */
    /**
     * Sample code: Services_EnableTestEndpoint.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void servicesEnableTestEndpoint(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.services().enableTestEndpointWithResponse("myResourceGroup", "myservice", Context.NONE);
    }
}
```

### Services_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Services GetByResourceGroup. */
public final class ServicesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Services_Get.json
     */
    /**
     * Sample code: Services_Get.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void servicesGet(com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.services().getByResourceGroupWithResponse("myResourceGroup", "myservice", Context.NONE);
    }
}
```

### Services_List

```java
import com.azure.core.util.Context;

/** Samples for Services List. */
public final class ServicesListSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Services_ListBySubscription.json
     */
    /**
     * Sample code: Services_ListBySubscription.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void servicesListBySubscription(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.services().list(Context.NONE);
    }
}
```

### Services_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Services ListByResourceGroup. */
public final class ServicesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Services_List.json
     */
    /**
     * Sample code: Services_List.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void servicesList(com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.services().listByResourceGroup("myResourceGroup", Context.NONE);
    }
}
```

### Services_ListTestKeys

```java
import com.azure.core.util.Context;

/** Samples for Services ListTestKeys. */
public final class ServicesListTestKeysSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Services_ListTestKeys.json
     */
    /**
     * Sample code: Services_ListTestKeys.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void servicesListTestKeys(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.services().listTestKeysWithResponse("myResourceGroup", "myservice", Context.NONE);
    }
}
```

### Services_RegenerateTestKey

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.appplatform.generated.models.RegenerateTestKeyRequestPayload;
import com.azure.resourcemanager.appplatform.generated.models.TestKeyType;

/** Samples for Services RegenerateTestKey. */
public final class ServicesRegenerateTestKeySamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Services_RegenerateTestKey.json
     */
    /**
     * Sample code: Services_RegenerateTestKey.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void servicesRegenerateTestKey(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .services()
            .regenerateTestKeyWithResponse(
                "myResourceGroup",
                "myservice",
                new RegenerateTestKeyRequestPayload().withKeyType(TestKeyType.PRIMARY),
                Context.NONE);
    }
}
```

### Services_Start

```java
import com.azure.core.util.Context;

/** Samples for Services Start. */
public final class ServicesStartSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Services_Start.json
     */
    /**
     * Sample code: Services_Start.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void servicesStart(com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.services().start("myResourceGroup", "myservice", Context.NONE);
    }
}
```

### Services_Stop

```java
import com.azure.core.util.Context;

/** Samples for Services Stop. */
public final class ServicesStopSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Services_Stop.json
     */
    /**
     * Sample code: Services_Stop.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void servicesStop(com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.services().stop("myResourceGroup", "myservice", Context.NONE);
    }
}
```

### Services_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.appplatform.generated.models.ClusterResourceProperties;
import com.azure.resourcemanager.appplatform.generated.models.ServiceResource;
import com.azure.resourcemanager.appplatform.generated.models.Sku;
import java.util.HashMap;
import java.util.Map;

/** Samples for Services Update. */
public final class ServicesUpdateSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Services_Update.json
     */
    /**
     * Sample code: Services_Update.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void servicesUpdate(com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        ServiceResource resource =
            manager.services().getByResourceGroupWithResponse("myResourceGroup", "myservice", Context.NONE).getValue();
        resource
            .update()
            .withTags(mapOf("key1", "value1"))
            .withProperties(new ClusterResourceProperties())
            .withSku(new Sku().withName("S0").withTier("Standard"))
            .apply();
    }

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

### Skus_List

```java
import com.azure.core.util.Context;

/** Samples for Skus List. */
public final class SkusListSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Skus_List.json
     */
    /**
     * Sample code: Skus_List.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void skusList(com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.skus().list(Context.NONE);
    }
}
```

### Storages_CreateOrUpdate

```java
import com.azure.resourcemanager.appplatform.generated.models.StorageAccount;

/** Samples for Storages CreateOrUpdate. */
public final class StoragesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Storages_CreateOrUpdate.json
     */
    /**
     * Sample code: Storages_CreateOrUpdate.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void storagesCreateOrUpdate(
        com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager
            .storages()
            .define("mystorage")
            .withExistingSpring("myResourceGroup", "myservice")
            .withProperties(
                new StorageAccount()
                    .withAccountName("storage-account-name")
                    .withAccountKey("account-key-of-storage-account"))
            .create();
    }
}
```

### Storages_Delete

```java
import com.azure.core.util.Context;

/** Samples for Storages Delete. */
public final class StoragesDeleteSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Storages_Delete.json
     */
    /**
     * Sample code: Storages_Delete.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void storagesDelete(com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.storages().delete("myResourceGroup", "myservice", "mystorage", Context.NONE);
    }
}
```

### Storages_Get

```java
import com.azure.core.util.Context;

/** Samples for Storages Get. */
public final class StoragesGetSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Storages_Get.json
     */
    /**
     * Sample code: Storages_Get.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void storagesGet(com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.storages().getWithResponse("myResourceGroup", "myservice", "mystorage", Context.NONE);
    }
}
```

### Storages_List

```java
import com.azure.core.util.Context;

/** Samples for Storages List. */
public final class StoragesListSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/preview/2022-05-01-preview/examples/Storages_List.json
     */
    /**
     * Sample code: Storages_List.
     *
     * @param manager Entry point to AppPlatformManager.
     */
    public static void storagesList(com.azure.resourcemanager.appplatform.generated.AppPlatformManager manager) {
        manager.storages().list("myResourceGroup", "myService", Context.NONE);
    }
}
```

