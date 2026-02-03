# Code snippets and samples


## AuthorizedApplications

- [CreateOrUpdate](#authorizedapplications_createorupdate)
- [Delete](#authorizedapplications_delete)
- [Get](#authorizedapplications_get)
- [List](#authorizedapplications_list)

## CustomRollouts

- [CreateOrUpdate](#customrollouts_createorupdate)
- [Delete](#customrollouts_delete)
- [Get](#customrollouts_get)
- [ListByProviderRegistration](#customrollouts_listbyproviderregistration)
- [Stop](#customrollouts_stop)

## DefaultRollouts

- [CreateOrUpdate](#defaultrollouts_createorupdate)
- [Delete](#defaultrollouts_delete)
- [Get](#defaultrollouts_get)
- [ListByProviderRegistration](#defaultrollouts_listbyproviderregistration)
- [Stop](#defaultrollouts_stop)

## NewRegionFrontloadRelease

- [CreateOrUpdate](#newregionfrontloadrelease_createorupdate)
- [GenerateManifest](#newregionfrontloadrelease_generatemanifest)
- [Get](#newregionfrontloadrelease_get)
- [Stop](#newregionfrontloadrelease_stop)

## NotificationRegistrations

- [CreateOrUpdate](#notificationregistrations_createorupdate)
- [Delete](#notificationregistrations_delete)
- [Get](#notificationregistrations_get)
- [ListByProviderRegistration](#notificationregistrations_listbyproviderregistration)

## Operations

- [CreateOrUpdate](#operations_createorupdate)
- [Delete](#operations_delete)
- [List](#operations_list)
- [ListByProviderRegistration](#operations_listbyproviderregistration)

## ProviderMonitorSettings

- [Create](#providermonitorsettings_create)
- [Delete](#providermonitorsettings_delete)
- [GetByResourceGroup](#providermonitorsettings_getbyresourcegroup)
- [List](#providermonitorsettings_list)
- [ListByResourceGroup](#providermonitorsettings_listbyresourcegroup)
- [Update](#providermonitorsettings_update)

## ProviderRegistrations

- [CreateOrUpdate](#providerregistrations_createorupdate)
- [Delete](#providerregistrations_delete)
- [GenerateOperations](#providerregistrations_generateoperations)
- [Get](#providerregistrations_get)
- [List](#providerregistrations_list)

## ResourceActions

- [DeleteResources](#resourceactions_deleteresources)

## ResourceProvider

- [CheckinManifest](#resourceprovider_checkinmanifest)
- [GenerateManifest](#resourceprovider_generatemanifest)

## ResourceTypeRegistrations

- [CreateOrUpdate](#resourcetyperegistrations_createorupdate)
- [Delete](#resourcetyperegistrations_delete)
- [Get](#resourcetyperegistrations_get)
- [ListByProviderRegistration](#resourcetyperegistrations_listbyproviderregistration)

## Skus

- [CreateOrUpdate](#skus_createorupdate)
- [CreateOrUpdateNestedResourceTypeFirst](#skus_createorupdatenestedresourcetypefirst)
- [CreateOrUpdateNestedResourceTypeSecond](#skus_createorupdatenestedresourcetypesecond)
- [CreateOrUpdateNestedResourceTypeThird](#skus_createorupdatenestedresourcetypethird)
- [Delete](#skus_delete)
- [DeleteNestedResourceTypeFirst](#skus_deletenestedresourcetypefirst)
- [DeleteNestedResourceTypeSecond](#skus_deletenestedresourcetypesecond)
- [DeleteNestedResourceTypeThird](#skus_deletenestedresourcetypethird)
- [Get](#skus_get)
- [GetNestedResourceTypeFirst](#skus_getnestedresourcetypefirst)
- [GetNestedResourceTypeSecond](#skus_getnestedresourcetypesecond)
- [GetNestedResourceTypeThird](#skus_getnestedresourcetypethird)
- [ListByResourceTypeRegistrations](#skus_listbyresourcetyperegistrations)
- [ListByResourceTypeRegistrationsNestedResourceTypeFirst](#skus_listbyresourcetyperegistrationsnestedresourcetypefirst)
- [ListByResourceTypeRegistrationsNestedResourceTypeSecond](#skus_listbyresourcetyperegistrationsnestedresourcetypesecond)
- [ListByResourceTypeRegistrationsNestedResourceTypeThird](#skus_listbyresourcetyperegistrationsnestedresourcetypethird)
### AuthorizedApplications_CreateOrUpdate

```java
import com.azure.resourcemanager.providerhub.models.ApplicationDataAuthorization;
import com.azure.resourcemanager.providerhub.models.ApplicationProviderAuthorization;
import com.azure.resourcemanager.providerhub.models.AuthorizedApplicationProperties;
import com.azure.resourcemanager.providerhub.models.Role;
import java.util.Arrays;
import java.util.UUID;

/**
 * Samples for AuthorizedApplications CreateOrUpdate.
 */
public final class AuthorizedApplicationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * AuthorizedApplications_CreateOrUpdate.json
     */
    /**
     * Sample code: AuthorizedApplications_CreateOrUpdate.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void
        authorizedApplicationsCreateOrUpdate(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.authorizedApplications()
            .define(UUID.fromString("760505bf-dcfa-4311-b890-18da392a00b2"))
            .withExistingProviderRegistration("Microsoft.Contoso")
            .withProperties(new AuthorizedApplicationProperties()
                .withProviderAuthorization(
                    new ApplicationProviderAuthorization().withRoleDefinitionId("123456bf-gkur-2098-b890-98da392a00b2")
                        .withManagedByRoleDefinitionId("1a3b5c7d-8e9f-10g1-1h12-i13j14k1"))
                .withDataAuthorizations(Arrays.asList(new ApplicationDataAuthorization().withRole(Role.SERVICE_OWNER)
                    .withResourceTypes(Arrays.asList("*")))))
            .create();
    }
}
```

### AuthorizedApplications_Delete

```java
import java.util.UUID;

/**
 * Samples for AuthorizedApplications Delete.
 */
public final class AuthorizedApplicationsDeleteSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * AuthorizedApplications_Delete.json
     */
    /**
     * Sample code: AuthorizedApplications_Delete.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void authorizedApplicationsDelete(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.authorizedApplications()
            .deleteWithResponse("Microsoft.Contoso", UUID.fromString("760505bf-dcfa-4311-b890-18da392a00b2"),
                com.azure.core.util.Context.NONE);
    }
}
```

### AuthorizedApplications_Get

```java
import java.util.UUID;

/**
 * Samples for AuthorizedApplications Get.
 */
public final class AuthorizedApplicationsGetSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * AuthorizedApplications_Get.json
     */
    /**
     * Sample code: AuthorizedApplications_Get.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void authorizedApplicationsGet(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.authorizedApplications()
            .getWithResponse("Microsoft.Contoso", UUID.fromString("760505bf-dcfa-4311-b890-18da392a00b2"),
                com.azure.core.util.Context.NONE);
    }
}
```

### AuthorizedApplications_List

```java
/**
 * Samples for AuthorizedApplications List.
 */
public final class AuthorizedApplicationsListSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * AuthorizedApplications_List.json
     */
    /**
     * Sample code: AuthorizedApplications_List.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void authorizedApplicationsList(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.authorizedApplications().list("Microsoft.Contoso", com.azure.core.util.Context.NONE);
    }
}
```

### CustomRollouts_CreateOrUpdate

```java
import com.azure.resourcemanager.providerhub.models.CustomRolloutProperties;
import com.azure.resourcemanager.providerhub.models.CustomRolloutPropertiesSpecification;
import com.azure.resourcemanager.providerhub.models.CustomRolloutSpecificationAutoProvisionConfig;
import com.azure.resourcemanager.providerhub.models.CustomRolloutSpecificationCanary;
import java.util.Arrays;

/**
 * Samples for CustomRollouts CreateOrUpdate.
 */
public final class CustomRolloutsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * CustomRollouts_CreateOrUpdate.json
     */
    /**
     * Sample code: CustomRollouts_CreateOrUpdate.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void customRolloutsCreateOrUpdate(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.customRollouts()
            .define("brazilUsShoeBoxTesting")
            .withExistingProviderRegistration("Microsoft.Contoso")
            .withProperties(new CustomRolloutProperties().withSpecification(new CustomRolloutPropertiesSpecification()
                .withAutoProvisionConfig(
                    new CustomRolloutSpecificationAutoProvisionConfig().withStorage(true).withResourceGraph(true))
                .withCanary(new CustomRolloutSpecificationCanary().withRegions(Arrays.asList("brazilus")))
                .withRefreshSubscriptionRegistration(true)))
            .create();
    }
}
```

### CustomRollouts_Delete

```java
/**
 * Samples for CustomRollouts Delete.
 */
public final class CustomRolloutsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/CustomRollouts_Delete
     * .json
     */
    /**
     * Sample code: providerReleases_Delete.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void providerReleasesDelete(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.customRollouts()
            .deleteByResourceGroupWithResponse("Microsoft.Contoso", "2020week10", com.azure.core.util.Context.NONE);
    }
}
```

### CustomRollouts_Get

```java
/**
 * Samples for CustomRollouts Get.
 */
public final class CustomRolloutsGetSamples {
    /*
     * x-ms-original-file:
     * specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/CustomRollouts_Get.
     * json
     */
    /**
     * Sample code: CustomRollouts_Get.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void customRolloutsGet(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.customRollouts()
            .getWithResponse("Microsoft.Contoso", "canaryTesting99", com.azure.core.util.Context.NONE);
    }
}
```

### CustomRollouts_ListByProviderRegistration

```java
/**
 * Samples for CustomRollouts ListByProviderRegistration.
 */
public final class CustomRolloutsListByProviderRegistrationSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * CustomRollouts_ListByProviderRegistration.json
     */
    /**
     * Sample code: CustomRollouts_ListByProviderRegistration.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void
        customRolloutsListByProviderRegistration(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.customRollouts().listByProviderRegistration("Microsoft.Contoso", com.azure.core.util.Context.NONE);
    }
}
```

### CustomRollouts_Stop

```java
/**
 * Samples for CustomRollouts Stop.
 */
public final class CustomRolloutsStopSamples {
    /*
     * x-ms-original-file:
     * specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/CustomRollouts_Stop.
     * json
     */
    /**
     * Sample code: CustomRollouts_Stop.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void customRolloutsStop(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.customRollouts().stopWithResponse("Microsoft.Contoso", "2020week10", com.azure.core.util.Context.NONE);
    }
}
```

### DefaultRollouts_CreateOrUpdate

```java
import com.azure.resourcemanager.providerhub.models.DefaultRolloutProperties;
import com.azure.resourcemanager.providerhub.models.DefaultRolloutPropertiesSpecification;
import com.azure.resourcemanager.providerhub.models.DefaultRolloutSpecificationCanary;
import com.azure.resourcemanager.providerhub.models.DefaultRolloutSpecificationExpeditedRollout;
import com.azure.resourcemanager.providerhub.models.DefaultRolloutSpecificationRestOfTheWorldGroupTwo;
import java.time.Duration;
import java.util.Arrays;

/**
 * Samples for DefaultRollouts CreateOrUpdate.
 */
public final class DefaultRolloutsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * DefaultRollouts_CreateOrUpdate.json
     */
    /**
     * Sample code: DefaultRollouts_CreateOrUpdate.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void defaultRolloutsCreateOrUpdate(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.defaultRollouts()
            .define("2020week10")
            .withExistingProviderRegistration("Microsoft.Contoso")
            .withProperties(
                new DefaultRolloutProperties().withSpecification(new DefaultRolloutPropertiesSpecification()
                    .withExpeditedRollout(new DefaultRolloutSpecificationExpeditedRollout().withEnabled(true))
                    .withCanary(new DefaultRolloutSpecificationCanary().withSkipRegions(Arrays.asList("eastus2euap")))
                    .withRestOfTheWorldGroupTwo(new DefaultRolloutSpecificationRestOfTheWorldGroupTwo()
                        .withWaitDuration(Duration.parse("PT4H")))))
            .create();
    }
}
```

### DefaultRollouts_Delete

```java
/**
 * Samples for DefaultRollouts Delete.
 */
public final class DefaultRolloutsDeleteSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * DefaultRollouts_Delete.json
     */
    /**
     * Sample code: DefaultRollouts_Delete.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void defaultRolloutsDelete(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.defaultRollouts()
            .deleteByResourceGroupWithResponse("Microsoft.Contoso", "2020week10", com.azure.core.util.Context.NONE);
    }
}
```

### DefaultRollouts_Get

```java
/**
 * Samples for DefaultRollouts Get.
 */
public final class DefaultRolloutsGetSamples {
    /*
     * x-ms-original-file:
     * specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/DefaultRollouts_Get.
     * json
     */
    /**
     * Sample code: DefaultRollouts_Get.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void defaultRolloutsGet(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.defaultRollouts().getWithResponse("Microsoft.Contoso", "2020week10", com.azure.core.util.Context.NONE);
    }
}
```

### DefaultRollouts_ListByProviderRegistration

```java
/**
 * Samples for DefaultRollouts ListByProviderRegistration.
 */
public final class DefaultRolloutsListByProviderRegistrationSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * DefaultRollouts_ListByProviderRegistration.json
     */
    /**
     * Sample code: DefaultRollouts_ListByProviderRegistration.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void
        defaultRolloutsListByProviderRegistration(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.defaultRollouts().listByProviderRegistration("Microsoft.Contoso", com.azure.core.util.Context.NONE);
    }
}
```

### DefaultRollouts_Stop

```java
/**
 * Samples for DefaultRollouts Stop.
 */
public final class DefaultRolloutsStopSamples {
    /*
     * x-ms-original-file:
     * specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/DefaultRollouts_Stop.
     * json
     */
    /**
     * Sample code: DefaultRollouts_Stop.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void defaultRolloutsStop(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.defaultRollouts().stopWithResponse("Microsoft.Contoso", "2020week10", com.azure.core.util.Context.NONE);
    }
}
```

### NewRegionFrontloadRelease_CreateOrUpdate

```java
import com.azure.resourcemanager.providerhub.models.AvailableCheckInManifestEnvironment;
import com.azure.resourcemanager.providerhub.models.EndpointType;
import com.azure.resourcemanager.providerhub.models.FrontloadPayload;
import com.azure.resourcemanager.providerhub.models.FrontloadPayloadProperties;
import com.azure.resourcemanager.providerhub.models.FrontloadPayloadPropertiesOverrideEndpointLevelFields;
import com.azure.resourcemanager.providerhub.models.FrontloadPayloadPropertiesOverrideManifestLevelFields;
import com.azure.resourcemanager.providerhub.models.ResourceHydrationAccount;
import com.azure.resourcemanager.providerhub.models.ResourceTypeEndpointBaseDstsConfiguration;
import com.azure.resourcemanager.providerhub.models.ResourceTypeEndpointBaseFeaturesRule;
import com.azure.resourcemanager.providerhub.models.ServiceFeatureFlagAction;
import java.time.Duration;
import java.util.Arrays;

/**
 * Samples for NewRegionFrontloadRelease CreateOrUpdate.
 */
public final class NewRegionFrontloadReleaseCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * NewRegionFrontloadRelease_CreateOrUpdate.json
     */
    /**
     * Sample code: NewRegionFrontloadRelease_CreateOrUpdate.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void
        newRegionFrontloadReleaseCreateOrUpdate(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.newRegionFrontloadReleases()
            .createOrUpdateWithResponse("Microsoft.Contoso", "2020week10",
                new FrontloadPayload().withProperties(new FrontloadPayloadProperties().withOperationType("Rollout")
                    .withProviderNamespace("Microsoft.Contoso")
                    .withFrontloadLocation("Israel Central")
                    .withCopyFromLocation("eastus")
                    .withEnvironmentType(AvailableCheckInManifestEnvironment.PROD)
                    .withServiceFeatureFlag(ServiceFeatureFlagAction.DO_NOT_CREATE)
                    .withIncludeResourceTypes(Arrays.asList("servers"))
                    .withExcludeResourceTypes(Arrays.asList("monitors"))
                    .withOverrideManifestLevelFields(
                        new FrontloadPayloadPropertiesOverrideManifestLevelFields().withResourceHydrationAccounts(
                            Arrays.asList(new ResourceHydrationAccount().withAccountName("classichydrationprodsn01")
                                .withSubscriptionId("e4eae963-2d15-43e6-a097-98bd75b33edd"))))
                    .withOverrideEndpointLevelFields(
                        new FrontloadPayloadPropertiesOverrideEndpointLevelFields().withEnabled(true)
                            .withApiVersions(Arrays.asList("2024-04-01-preview"))
                            .withEndpointUri("https://resource-endpoint.com/")
                            .withLocations(Arrays.asList("East US"))
                            .withRequiredFeatures(Arrays.asList("<feature flag>"))
                            .withFeaturesRule(new ResourceTypeEndpointBaseFeaturesRule())
                            .withTimeout(Duration.parse("PT20S"))
                            .withEndpointType(EndpointType.PRODUCTION)
                            .withDstsConfiguration(
                                new ResourceTypeEndpointBaseDstsConfiguration().withServiceName("resourceprovider")
                                    .withServiceDnsName("messaging.azure-ppe.net"))
                            .withSkuLink("http://endpointuri/westus/skus")
                            .withApiVersion("2024-04-01-preview")
                            .withZones(Arrays.asList("zone1")))
                    .withIgnoreFields(Arrays.asList("apiversion"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### NewRegionFrontloadRelease_GenerateManifest

```java
import com.azure.resourcemanager.providerhub.models.AvailableCheckInManifestEnvironment;
import com.azure.resourcemanager.providerhub.models.EndpointType;
import com.azure.resourcemanager.providerhub.models.FrontloadPayload;
import com.azure.resourcemanager.providerhub.models.FrontloadPayloadProperties;
import com.azure.resourcemanager.providerhub.models.FrontloadPayloadPropertiesOverrideEndpointLevelFields;
import com.azure.resourcemanager.providerhub.models.FrontloadPayloadPropertiesOverrideManifestLevelFields;
import com.azure.resourcemanager.providerhub.models.ResourceHydrationAccount;
import com.azure.resourcemanager.providerhub.models.ResourceTypeEndpointBaseDstsConfiguration;
import com.azure.resourcemanager.providerhub.models.ResourceTypeEndpointBaseFeaturesRule;
import com.azure.resourcemanager.providerhub.models.ServiceFeatureFlagAction;
import java.time.Duration;
import java.util.Arrays;

/**
 * Samples for NewRegionFrontloadRelease GenerateManifest.
 */
public final class NewRegionFrontloadReleaseGenerateManifestSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * NewRegionFrontloadRelease_GenerateManifest.json
     */
    /**
     * Sample code: NewRegionFrontloadRelease_GenerateManifest.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void
        newRegionFrontloadReleaseGenerateManifest(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.newRegionFrontloadReleases()
            .generateManifestWithResponse("Microsoft.Contoso",
                new FrontloadPayload().withProperties(new FrontloadPayloadProperties().withOperationType("Rollout")
                    .withProviderNamespace("Microsoft.Contoso")
                    .withFrontloadLocation("Israel Central")
                    .withCopyFromLocation("eastus")
                    .withEnvironmentType(AvailableCheckInManifestEnvironment.PROD)
                    .withServiceFeatureFlag(ServiceFeatureFlagAction.DO_NOT_CREATE)
                    .withIncludeResourceTypes(Arrays.asList("servers"))
                    .withExcludeResourceTypes(Arrays.asList("monitors"))
                    .withOverrideManifestLevelFields(
                        new FrontloadPayloadPropertiesOverrideManifestLevelFields().withResourceHydrationAccounts(
                            Arrays.asList(new ResourceHydrationAccount().withAccountName("classichydrationprodsn01")
                                .withSubscriptionId("e4eae963-2d15-43e6-a097-98bd75b33edd"))))
                    .withOverrideEndpointLevelFields(
                        new FrontloadPayloadPropertiesOverrideEndpointLevelFields().withEnabled(true)
                            .withApiVersions(Arrays.asList("2024-04-01-preview"))
                            .withEndpointUri("https://resource-endpoint.com/")
                            .withLocations(Arrays.asList("East US"))
                            .withRequiredFeatures(Arrays.asList("<feature flag>"))
                            .withFeaturesRule(new ResourceTypeEndpointBaseFeaturesRule())
                            .withTimeout(Duration.parse("PT20S"))
                            .withEndpointType(EndpointType.PRODUCTION)
                            .withDstsConfiguration(
                                new ResourceTypeEndpointBaseDstsConfiguration().withServiceName("resourceprovider")
                                    .withServiceDnsName("messaging.azure-ppe.net"))
                            .withSkuLink("http://endpointuri/westus/skus")
                            .withApiVersion("2024-04-01-preview")
                            .withZones(Arrays.asList("zone1")))
                    .withIgnoreFields(Arrays.asList("apiversion"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### NewRegionFrontloadRelease_Get

```java
/**
 * Samples for NewRegionFrontloadRelease Get.
 */
public final class NewRegionFrontloadReleaseGetSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * NewRegionFrontloadRelease_Get.json
     */
    /**
     * Sample code: NewRegionFrontloadRelease_Get.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void newRegionFrontloadReleaseGet(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.newRegionFrontloadReleases()
            .getWithResponse("Microsoft.Contoso", "2020week10", com.azure.core.util.Context.NONE);
    }
}
```

### NewRegionFrontloadRelease_Stop

```java
/**
 * Samples for NewRegionFrontloadRelease Stop.
 */
public final class NewRegionFrontloadReleaseStopSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * NewRegionFrontloadRelease_Stop.json
     */
    /**
     * Sample code: NewRegionFrontloadRelease_Stop.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void newRegionFrontloadReleaseStop(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.newRegionFrontloadReleases()
            .stopWithResponse("Microsoft.Contoso", "2020week10", com.azure.core.util.Context.NONE);
    }
}
```

### NotificationRegistrations_CreateOrUpdate

```java
import com.azure.resourcemanager.providerhub.models.MessageScope;
import com.azure.resourcemanager.providerhub.models.NotificationEndpoint;
import com.azure.resourcemanager.providerhub.models.NotificationMode;
import com.azure.resourcemanager.providerhub.models.NotificationRegistrationProperties;
import java.util.Arrays;

/**
 * Samples for NotificationRegistrations CreateOrUpdate.
 */
public final class NotificationRegistrationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * NotificationRegistrations_CreateOrUpdate.json
     */
    /**
     * Sample code: NotificationRegistrations_CreateOrUpdate.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void
        notificationRegistrationsCreateOrUpdate(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.notificationRegistrations()
            .define("fooNotificationRegistration")
            .withExistingProviderRegistration("Microsoft.Contoso")
            .withProperties(new NotificationRegistrationProperties().withNotificationMode(NotificationMode.EVENT_HUB)
                .withMessageScope(MessageScope.REGISTERED_SUBSCRIPTIONS)
                .withIncludedEvents(Arrays.asList("*/write", "Microsoft.Contoso/employees/delete"))
                .withNotificationEndpoints(Arrays.asList(new NotificationEndpoint().withNotificationDestination(
                    "/subscriptions/ac6bcfb5-3dc1-491f-95a6-646b89bf3e88/resourceGroups/mgmtexp-eastus/providers/Microsoft.EventHub/namespaces/unitedstates-mgmtexpint/eventhubs/armlinkednotifications")
                    .withLocations(Arrays.asList("", "East US")),
                    new NotificationEndpoint().withNotificationDestination(
                        "/subscriptions/ac6bcfb5-3dc1-491f-95a6-646b89bf3e88/resourceGroups/mgmtexp-northeurope/providers/Microsoft.EventHub/namespaces/europe-mgmtexpint/eventhubs/armlinkednotifications")
                        .withLocations(Arrays.asList("North Europe")))))
            .create();
    }
}
```

### NotificationRegistrations_Delete

```java
/**
 * Samples for NotificationRegistrations Delete.
 */
public final class NotificationRegistrationsDeleteSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * NotificationRegistrations_Delete.json
     */
    /**
     * Sample code: NotificationRegistrations_Delete.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void
        notificationRegistrationsDelete(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.notificationRegistrations()
            .deleteByResourceGroupWithResponse("Microsoft.Contoso", "fooNotificationRegistration",
                com.azure.core.util.Context.NONE);
    }
}
```

### NotificationRegistrations_Get

```java
/**
 * Samples for NotificationRegistrations Get.
 */
public final class NotificationRegistrationsGetSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * NotificationRegistrations_Get.json
     */
    /**
     * Sample code: NotificationRegistrations_Get.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void notificationRegistrationsGet(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.notificationRegistrations()
            .getWithResponse("Microsoft.Contoso", "fooNotificationRegistration", com.azure.core.util.Context.NONE);
    }
}
```

### NotificationRegistrations_ListByProviderRegistration

```java
/**
 * Samples for NotificationRegistrations ListByProviderRegistration.
 */
public final class NotificationRegistrationsListByProviderRegistrationSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * NotificationRegistrations_ListByProviderRegistration.json
     */
    /**
     * Sample code: NotificationRegistrations_ListByProviderRegistration.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void notificationRegistrationsListByProviderRegistration(
        com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.notificationRegistrations()
            .listByProviderRegistration("Microsoft.Contoso", com.azure.core.util.Context.NONE);
    }
}
```

### Operations_CreateOrUpdate

```java
import com.azure.resourcemanager.providerhub.fluent.models.OperationsPutContentInner;
import com.azure.resourcemanager.providerhub.models.LocalizedOperationDefinition;
import com.azure.resourcemanager.providerhub.models.LocalizedOperationDefinitionDisplay;
import com.azure.resourcemanager.providerhub.models.LocalizedOperationDisplayDefinitionDefault;
import com.azure.resourcemanager.providerhub.models.LocalizedOperationDisplayDefinitionEn;
import com.azure.resourcemanager.providerhub.models.OperationActionType;
import com.azure.resourcemanager.providerhub.models.OperationOrigins;
import com.azure.resourcemanager.providerhub.models.OperationsPutContentProperties;
import java.util.Arrays;

/**
 * Samples for Operations CreateOrUpdate.
 */
public final class OperationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * Operations_CreateOrUpdate.json
     */
    /**
     * Sample code: Operations_CreateOrUpdate.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void operationsCreateOrUpdate(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.operations()
            .createOrUpdateWithResponse("Microsoft.Contoso", new OperationsPutContentInner().withProperties(
                new OperationsPutContentProperties().withContents(Arrays.asList(new LocalizedOperationDefinition()
                    .withName("RP.69C09791/register/action")
                    .withIsDataAction(true)
                    .withDisplay(new LocalizedOperationDefinitionDisplay().withDefaultProperty(
                        new LocalizedOperationDisplayDefinitionDefault().withProvider("RP.69C09791")
                            .withResource("Register")
                            .withOperation("Registers the RP.69C09791 Resource Provider")
                            .withDescription(
                                "Registers the subscription for the RP.69C09791 resource provider and enables the creation of RP.69C09791.")))
                    .withActionType(OperationActionType.INTERNAL),
                    new LocalizedOperationDefinition().withName("RP.69C09791/unregister/action")
                        .withIsDataAction(false)
                        .withOrigin(OperationOrigins.SYSTEM)
                        .withDisplay(new LocalizedOperationDefinitionDisplay()
                            .withDefaultProperty(new LocalizedOperationDisplayDefinitionDefault()
                                .withProvider("RP.69C09791")
                                .withResource("Unregister")
                                .withOperation("Unregisters the RP.69C09791 Resource Provider")
                                .withDescription(
                                    "Unregisters the subscription for the RP.69C09791 resource provider and enables the creation of RP.69C09791."))
                            .withEn(new LocalizedOperationDisplayDefinitionEn().withProvider("RP.69C09791")
                                .withResource("2e1803d4-417f-492c-b305-148da38b211e")
                                .withOperation("d31623d6-8765-42fb-aca2-5a58303e52dd")
                                .withDescription("ece249f5-b5b9-492d-ac68-b4e1be1677bc")))))),
                com.azure.core.util.Context.NONE);
    }
}
```

### Operations_Delete

```java
/**
 * Samples for Operations Delete.
 */
public final class OperationsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/Operations_Delete.
     * json
     */
    /**
     * Sample code: Operations_Delete.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void operationsDelete(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.operations().deleteWithResponse("Microsoft.Contoso", com.azure.core.util.Context.NONE);
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
     * specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void operationsList(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Operations_ListByProviderRegistration

```java
/**
 * Samples for Operations ListByProviderRegistration.
 */
public final class OperationsListByProviderRegistrationSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * Operations_ListByProviderRegistration.json
     */
    /**
     * Sample code: Operations_ListByProviderRegistration.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void
        operationsListByProviderRegistration(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.operations()
            .listByProviderRegistrationWithResponse("Microsoft.Contoso", com.azure.core.util.Context.NONE);
    }
}
```

### ProviderMonitorSettings_Create

```java
/**
 * Samples for ProviderMonitorSettings Create.
 */
public final class ProviderMonitorSettingsCreateSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * ProviderMonitorSettings_Create.json
     */
    /**
     * Sample code: ProviderMonitorSettings_Create.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void providerMonitorSettingsCreate(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.providerMonitorSettings()
            .define("ContosoMonitorSetting")
            .withRegion("eastus")
            .withExistingResourceGroup("default")
            .create();
    }
}
```

### ProviderMonitorSettings_Delete

```java
/**
 * Samples for ProviderMonitorSettings Delete.
 */
public final class ProviderMonitorSettingsDeleteSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * ProviderMonitorSettings_Delete.json
     */
    /**
     * Sample code: ProviderMonitorSettings_Delete.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void providerMonitorSettingsDelete(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.providerMonitorSettings()
            .deleteByResourceGroupWithResponse("default", "ContosoMonitorSetting", com.azure.core.util.Context.NONE);
    }
}
```

### ProviderMonitorSettings_GetByResourceGroup

```java
/**
 * Samples for ProviderMonitorSettings GetByResourceGroup.
 */
public final class ProviderMonitorSettingsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * ProviderMonitorSettings_Get.json
     */
    /**
     * Sample code: ProviderMonitorSettings_Get.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void providerMonitorSettingsGet(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.providerMonitorSettings()
            .getByResourceGroupWithResponse("default", "ContosoMonitorSetting", com.azure.core.util.Context.NONE);
    }
}
```

### ProviderMonitorSettings_List

```java
/**
 * Samples for ProviderMonitorSettings List.
 */
public final class ProviderMonitorSettingsListSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * ProviderMonitorSettings_ListBySubscription.json
     */
    /**
     * Sample code: ProviderMonitorSettings_ListBySubscription.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void
        providerMonitorSettingsListBySubscription(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.providerMonitorSettings().list(com.azure.core.util.Context.NONE);
    }
}
```

### ProviderMonitorSettings_ListByResourceGroup

```java
/**
 * Samples for ProviderMonitorSettings ListByResourceGroup.
 */
public final class ProviderMonitorSettingsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * ProviderMonitorSettings_ListByResourceGroup.json
     */
    /**
     * Sample code: ProviderMonitorSettings_ListByResourceGroup.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void
        providerMonitorSettingsListByResourceGroup(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.providerMonitorSettings().listByResourceGroup("default", com.azure.core.util.Context.NONE);
    }
}
```

### ProviderMonitorSettings_Update

```java
/**
 * Samples for ProviderMonitorSettings Update.
 */
public final class ProviderMonitorSettingsUpdateSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * ProviderMonitorSettings_Update.json
     */
    /**
     * Sample code: ProviderMonitorSettings_Update.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void providerMonitorSettingsUpdate(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.providerMonitorSettings()
            .updateWithResponse("default", "ContosoMonitorSetting", com.azure.core.util.Context.NONE);
    }
}
```

### ProviderRegistrations_CreateOrUpdate

```java
import com.azure.resourcemanager.providerhub.fluent.models.ProviderRegistrationInner;
import com.azure.resourcemanager.providerhub.models.BlockActionVerb;
import com.azure.resourcemanager.providerhub.models.CrossTenantTokenValidation;
import com.azure.resourcemanager.providerhub.models.EndpointInformation;
import com.azure.resourcemanager.providerhub.models.ExpeditedRolloutIntent;
import com.azure.resourcemanager.providerhub.models.FilterRule;
import com.azure.resourcemanager.providerhub.models.NotificationEndpointType;
import com.azure.resourcemanager.providerhub.models.NotificationOptions;
import com.azure.resourcemanager.providerhub.models.ProviderRegistrationKind;
import com.azure.resourcemanager.providerhub.models.ProviderRegistrationProperties;
import com.azure.resourcemanager.providerhub.models.Readiness;
import com.azure.resourcemanager.providerhub.models.ResourceHydrationAccount;
import com.azure.resourcemanager.providerhub.models.ResourceProviderCapabilities;
import com.azure.resourcemanager.providerhub.models.ResourceProviderCapabilitiesEffect;
import com.azure.resourcemanager.providerhub.models.ResourceProviderEndpoint;
import com.azure.resourcemanager.providerhub.models.ResourceProviderManagementErrorResponseMessageOptions;
import com.azure.resourcemanager.providerhub.models.ResourceProviderManagementExpeditedRolloutMetadata;
import com.azure.resourcemanager.providerhub.models.ResourceProviderManifestPropertiesDstsConfiguration;
import com.azure.resourcemanager.providerhub.models.ResourceProviderManifestPropertiesManagement;
import com.azure.resourcemanager.providerhub.models.ResourceProviderManifestPropertiesNotificationSettings;
import com.azure.resourcemanager.providerhub.models.ResourceProviderManifestPropertiesResourceGroupLockOptionDuringMove;
import com.azure.resourcemanager.providerhub.models.ResourceProviderManifestPropertiesResponseOptions;
import com.azure.resourcemanager.providerhub.models.ResourceProviderService;
import com.azure.resourcemanager.providerhub.models.ResourceProviderType;
import com.azure.resourcemanager.providerhub.models.ServerFailureResponseMessageType;
import com.azure.resourcemanager.providerhub.models.ServiceClientOptionsType;
import com.azure.resourcemanager.providerhub.models.ServiceStatus;
import com.azure.resourcemanager.providerhub.models.ServiceTreeInfo;
import com.azure.resourcemanager.providerhub.models.SubscriberSetting;
import java.util.Arrays;

/**
 * Samples for ProviderRegistrations CreateOrUpdate.
 */
public final class ProviderRegistrationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * DirectProviderRegistrations_CreateOrUpdate.json
     */
    /**
     * Sample code: DirectProviderRegistrations_CreateOrUpdate.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void
        directProviderRegistrationsCreateOrUpdate(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.providerRegistrations()
            .createOrUpdate("Microsoft.Contoso",
                new ProviderRegistrationInner()
                    .withProperties(new ProviderRegistrationProperties()
                        .withServices(Arrays.asList(
                            new ResourceProviderService().withServiceName("tags").withStatus(ServiceStatus.INACTIVE)))
                        .withServiceName("root")
                        .withProviderVersion("2.0")
                        .withProviderType(ResourceProviderType.INTERNAL)
                        .withManagement(new ResourceProviderManifestPropertiesManagement()
                            .withIncidentRoutingService("Contoso Resource Provider")
                            .withIncidentRoutingTeam("Contoso Triage")
                            .withIncidentContactEmail("helpme@contoso.com")
                            .withServiceTreeInfos(Arrays
                                .asList(new ServiceTreeInfo().withServiceId("d1b7d8ba-05e2-48e6-90d6-d781b99c6e69")
                                    .withComponentId("d1b7d8ba-05e2-48e6-90d6-d781b99c6e69")
                                    .withReadiness(Readiness.IN_DEVELOPMENT))))
                        .withCapabilities(Arrays.asList(
                            new ResourceProviderCapabilities().withQuotaId("CSP_2015-05-01")
                                .withEffect(ResourceProviderCapabilitiesEffect.ALLOW),
                            new ResourceProviderCapabilities().withQuotaId("CSP_MG_2017-12-01")
                                .withEffect(ResourceProviderCapabilitiesEffect.ALLOW)))
                        .withDstsConfiguration(
                            new ResourceProviderManifestPropertiesDstsConfiguration().withServiceName("prds-shim")
                                .withServiceDnsName("prds.sparta.azure.com"))
                        .withNotificationOptions(NotificationOptions.EMIT_SPENDING_LIMIT)
                        .withResourceHydrationAccounts(Arrays
                            .asList(new ResourceHydrationAccount().withAccountName("classichydrationprodsn01")
                                .withSubscriptionId("e4eae963-2d15-43e6-a097-98bd75b33edd"),
                                new ResourceHydrationAccount()
                                    .withAccountName("classichydrationprodch01")
                                    .withSubscriptionId("69e69ecb-e69c-41d4-99b8-87dd12781067")))
                        .withNotificationSettings(new ResourceProviderManifestPropertiesNotificationSettings()
                            .withSubscriberSettings(Arrays.asList(
                                new SubscriberSetting().withFilterRules(Arrays.asList(new FilterRule().withFilterQuery(
                                    "Resources | where event.eventType in ('Microsoft.Network/IpAddresses/write', 'Microsoft.KeyVault/vaults/move/action')")
                                    .withEndpointInformation(Arrays.asList(
                                        new EndpointInformation().withEndpoint("https://userrp.azure.com/arnnotify")
                                            .withEndpointType(NotificationEndpointType.WEBHOOK)
                                            .withSchemaVersion("3.0"),
                                        new EndpointInformation().withEndpoint("https://userrp.azure.com/arnnotify")
                                            .withEndpointType(NotificationEndpointType.EVENTHUB)
                                            .withSchemaVersion("3.0"))))))))
                        .withManagementGroupGlobalNotificationEndpoints(Arrays.asList(new ResourceProviderEndpoint()
                            .withEndpointUri("{your_management_group_notification_endpoint}")))
                        .withOptionalFeatures(Arrays.asList("Microsoft.Resources/PlatformSubscription"))
                        .withResourceGroupLockOptionDuringMove(
                            new ResourceProviderManifestPropertiesResourceGroupLockOptionDuringMove()
                                .withBlockActionVerb(BlockActionVerb.ACTION))
                        .withResponseOptions(new ResourceProviderManifestPropertiesResponseOptions()
                            .withServiceClientOptionsType(ServiceClientOptionsType.DISABLE_AUTOMATIC_DECOMPRESSION))
                        .withLegacyNamespace("legacyNamespace")
                        .withLegacyRegistrations(Arrays.asList("legacyRegistration"))
                        .withCustomManifestVersion("2.0"))
                    .withKind(ProviderRegistrationKind.DIRECT),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * ProviderRegistrations_CreateOrUpdate.json
     */
    /**
     * Sample code: ProviderRegistrations_CreateOrUpdate.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void
        providerRegistrationsCreateOrUpdate(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.providerRegistrations()
            .createOrUpdate("Microsoft.Contoso",
                new ProviderRegistrationInner().withProperties(new ProviderRegistrationProperties()
                    .withServices(Arrays.asList(
                        new ResourceProviderService().withServiceName("tags").withStatus(ServiceStatus.INACTIVE)))
                    .withServiceName("root")
                    .withProviderVersion("2.0")
                    .withProviderType(ResourceProviderType.INTERNAL)
                    .withManagement(new ResourceProviderManifestPropertiesManagement()
                        .withIncidentRoutingService("Contoso Resource Provider")
                        .withIncidentRoutingTeam("Contoso Triage")
                        .withIncidentContactEmail("helpme@contoso.com")
                        .withServiceTreeInfos(
                            Arrays.asList(new ServiceTreeInfo().withServiceId("d1b7d8ba-05e2-48e6-90d6-d781b99c6e69")
                                .withComponentId("d1b7d8ba-05e2-48e6-90d6-d781b99c6e69")
                                .withReadiness(Readiness.IN_DEVELOPMENT)))
                        .withExpeditedRolloutSubmitters(Arrays.asList("SPARTA-PlatformServiceOperator"))
                        .withErrorResponseMessageOptions(new ResourceProviderManagementErrorResponseMessageOptions()
                            .withServerFailureResponseMessageType(ServerFailureResponseMessageType.OUTAGE_REPORTING))
                        .withExpeditedRolloutMetadata(
                            new ResourceProviderManagementExpeditedRolloutMetadata().withEnabled(false)
                                .withExpeditedRolloutIntent(ExpeditedRolloutIntent.HOTFIX))
                        .withCanaryManifestOwners(Arrays.asList("SPARTA-PlatformServiceAdmin"))
                        .withPcCode("fakeTokenPlaceholder")
                        .withProfitCenterProgramId("1234"))
                    .withCapabilities(Arrays.asList(
                        new ResourceProviderCapabilities().withQuotaId("CSP_2015-05-01")
                            .withEffect(ResourceProviderCapabilitiesEffect.ALLOW),
                        new ResourceProviderCapabilities().withQuotaId("CSP_MG_2017-12-01")
                            .withEffect(ResourceProviderCapabilitiesEffect.ALLOW)))
                    .withCrossTenantTokenValidation(CrossTenantTokenValidation.ENSURE_SECURE_VALIDATION)),
                com.azure.core.util.Context.NONE);
    }
}
```

### ProviderRegistrations_Delete

```java
/**
 * Samples for ProviderRegistrations Delete.
 */
public final class ProviderRegistrationsDeleteSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * ProviderRegistrations_Delete.json
     */
    /**
     * Sample code: ProviderRegistrations_Delete.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void providerRegistrationsDelete(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.providerRegistrations().deleteWithResponse("Microsoft.Contoso", com.azure.core.util.Context.NONE);
    }
}
```

### ProviderRegistrations_GenerateOperations

```java
/**
 * Samples for ProviderRegistrations GenerateOperations.
 */
public final class ProviderRegistrationsGenerateOperationsSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * ProviderRegistrations_GenerateOperations.json
     */
    /**
     * Sample code: ProviderRegistrations_GenerateOperations.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void
        providerRegistrationsGenerateOperations(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.providerRegistrations()
            .generateOperationsWithResponse("Microsoft.Contoso", com.azure.core.util.Context.NONE);
    }
}
```

### ProviderRegistrations_Get

```java
/**
 * Samples for ProviderRegistrations Get.
 */
public final class ProviderRegistrationsGetSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * ProviderRegistrations_Get.json
     */
    /**
     * Sample code: ProviderRegistrations_Get.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void providerRegistrationsGet(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.providerRegistrations().getWithResponse("Microsoft.Contoso", com.azure.core.util.Context.NONE);
    }
}
```

### ProviderRegistrations_List

```java
/**
 * Samples for ProviderRegistrations List.
 */
public final class ProviderRegistrationsListSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * ProviderRegistrations_List.json
     */
    /**
     * Sample code: ProviderRegistrations_List.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void providerRegistrationsList(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.providerRegistrations().list(com.azure.core.util.Context.NONE);
    }
}
```

### ResourceActions_DeleteResources

```java
import com.azure.resourcemanager.providerhub.models.ResourceManagementAction;
import com.azure.resourcemanager.providerhub.models.ResourceManagementEntity;
import java.util.Arrays;

/**
 * Samples for ResourceActions DeleteResources.
 */
public final class ResourceActionsDeleteResourcesSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * ResourceActions_DeleteResources.json
     */
    /**
     * Sample code: ResourceActions_DeleteResources.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void
        resourceActionsDeleteResources(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.resourceActions()
            .deleteResources("Microsoft.Contoso", "default",
                new ResourceManagementAction().withResources(Arrays.asList(new ResourceManagementEntity()
                    .withResourceId(
                        "/subscriptions/ab7a8701-f7ef-471a-a2f4-d0ebbf494f77/providers/Microsoft.Contoso/employee/test")
                    .withHomeTenantId("11111111-f7ef-471a-a2f4-d0ebbf494f77")
                    .withLocation("southeastasia"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_CheckinManifest

```java
import com.azure.resourcemanager.providerhub.models.CheckinManifestParams;

/**
 * Samples for ResourceProvider CheckinManifest.
 */
public final class ResourceProviderCheckinManifestSamples {
    /*
     * x-ms-original-file:
     * specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/CheckinManifest.json
     */
    /**
     * Sample code: CheckinManifest.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void checkinManifest(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.resourceProviders()
            .checkinManifestWithResponse("Microsoft.Contoso",
                new CheckinManifestParams().withEnvironment("Prod").withBaselineArmManifestLocation("EastUS2EUAP"),
                com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_GenerateManifest

```java
/**
 * Samples for ResourceProvider GenerateManifest.
 */
public final class ResourceProviderGenerateManifestSamples {
    /*
     * x-ms-original-file:
     * specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/GenerateManifest.json
     */
    /**
     * Sample code: GenerateManifest.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void generateManifest(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.resourceProviders().generateManifestWithResponse("Microsoft.Contoso", com.azure.core.util.Context.NONE);
    }
}
```

### ResourceTypeRegistrations_CreateOrUpdate

```java
import com.azure.resourcemanager.providerhub.models.AdditionalOptionsResourceTypeRegistration;
import com.azure.resourcemanager.providerhub.models.AllowedResourceName;
import com.azure.resourcemanager.providerhub.models.ApiProfile;
import com.azure.resourcemanager.providerhub.models.AsyncTimeoutRule;
import com.azure.resourcemanager.providerhub.models.AvailabilityZonePolicy;
import com.azure.resourcemanager.providerhub.models.CapacityPolicy;
import com.azure.resourcemanager.providerhub.models.CommonApiVersionsMergeMode;
import com.azure.resourcemanager.providerhub.models.CrossTenantTokenValidation;
import com.azure.resourcemanager.providerhub.models.DeleteDependency;
import com.azure.resourcemanager.providerhub.models.FilterOption;
import com.azure.resourcemanager.providerhub.models.LegacyDisallowedCondition;
import com.azure.resourcemanager.providerhub.models.LegacyOperation;
import com.azure.resourcemanager.providerhub.models.LinkedAction;
import com.azure.resourcemanager.providerhub.models.LinkedOperation;
import com.azure.resourcemanager.providerhub.models.LinkedOperationRule;
import com.azure.resourcemanager.providerhub.models.Notification;
import com.azure.resourcemanager.providerhub.models.NotificationType;
import com.azure.resourcemanager.providerhub.models.OpenApiConfiguration;
import com.azure.resourcemanager.providerhub.models.OpenApiValidation;
import com.azure.resourcemanager.providerhub.models.OptOutHeaderType;
import com.azure.resourcemanager.providerhub.models.Policy;
import com.azure.resourcemanager.providerhub.models.PolicyExecutionType;
import com.azure.resourcemanager.providerhub.models.Readiness;
import com.azure.resourcemanager.providerhub.models.Regionality;
import com.azure.resourcemanager.providerhub.models.ResourceAccessPolicy;
import com.azure.resourcemanager.providerhub.models.ResourceConcurrencyControlOption;
import com.azure.resourcemanager.providerhub.models.ResourceTypeEndpoint;
import com.azure.resourcemanager.providerhub.models.ResourceTypeRegistrationProperties;
import com.azure.resourcemanager.providerhub.models.ResourceTypeRegistrationPropertiesAvailabilityZoneRule;
import com.azure.resourcemanager.providerhub.models.ResourceTypeRegistrationPropertiesCapacityRule;
import com.azure.resourcemanager.providerhub.models.ResourceTypeRegistrationPropertiesDstsConfiguration;
import com.azure.resourcemanager.providerhub.models.ResourceTypeRegistrationPropertiesLegacyPolicy;
import com.azure.resourcemanager.providerhub.models.ResourceTypeRegistrationPropertiesManagement;
import com.azure.resourcemanager.providerhub.models.ResourceTypeRegistrationPropertiesMarketplaceOptions;
import com.azure.resourcemanager.providerhub.models.ResourceTypeRegistrationPropertiesRequestHeaderOptions;
import com.azure.resourcemanager.providerhub.models.ResourceTypeRegistrationPropertiesResourceCache;
import com.azure.resourcemanager.providerhub.models.ResourceTypeRegistrationPropertiesResourceGraphConfiguration;
import com.azure.resourcemanager.providerhub.models.ResourceTypeRegistrationPropertiesResourceManagementOptions;
import com.azure.resourcemanager.providerhub.models.ResourceTypeRegistrationPropertiesResourceManagementOptionsBatchProvisioningSupport;
import com.azure.resourcemanager.providerhub.models.ResourceTypeRegistrationPropertiesResourceQueryManagement;
import com.azure.resourcemanager.providerhub.models.ResourceTypeRegistrationPropertiesResourceTypeCommonAttributeManagement;
import com.azure.resourcemanager.providerhub.models.ResourceTypeRegistrationPropertiesRoutingRule;
import com.azure.resourcemanager.providerhub.models.ResourceTypeRegistrationPropertiesTemplateDeploymentPolicy;
import com.azure.resourcemanager.providerhub.models.RoutingType;
import com.azure.resourcemanager.providerhub.models.ServiceTreeInfo;
import com.azure.resourcemanager.providerhub.models.SkipNotifications;
import com.azure.resourcemanager.providerhub.models.SupportedOperations;
import com.azure.resourcemanager.providerhub.models.SwaggerSpecification;
import com.azure.resourcemanager.providerhub.models.TemplateDeploymentCapabilities;
import com.azure.resourcemanager.providerhub.models.TemplateDeploymentPreflightNotifications;
import com.azure.resourcemanager.providerhub.models.TemplateDeploymentPreflightOptions;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ResourceTypeRegistrations CreateOrUpdate.
 */
public final class ResourceTypeRegistrationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * ResourceTypeRegistrations_CreateOrUpdate.json
     */
    /**
     * Sample code: ResourceTypeRegistrations_CreateOrUpdate.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void
        resourceTypeRegistrationsCreateOrUpdate(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.resourceTypeRegistrations()
            .define("employees")
            .withExistingProviderRegistration("Microsoft.Contoso")
            .withProperties(new ResourceTypeRegistrationProperties().withRoutingType(RoutingType.DEFAULT)
                .withCrossTenantTokenValidation(CrossTenantTokenValidation.ENSURE_SECURE_VALIDATION)
                .withRegionality(Regionality.REGIONAL)
                .withEndpoints(
                    Arrays.asList(new ResourceTypeEndpoint().withApiVersions(Arrays.asList("2020-06-01-preview"))
                        .withLocations(Arrays.asList("West US", "East US", "North Europe"))
                        .withRequiredFeatures(Arrays.asList("<feature flag>"))))
                .withSwaggerSpecifications(Arrays.asList(new SwaggerSpecification()
                    .withApiVersions(Arrays.asList("2020-06-01-preview"))
                    .withSwaggerSpecFolderUri(
                        "https://github.com/Azure/azure-rest-api-specs/blob/feature/azure/contoso/specification/contoso/resource-manager/Microsoft.SampleRP/")))
                .withRequestHeaderOptions(new ResourceTypeRegistrationPropertiesRequestHeaderOptions()
                    .withOptOutHeaders(OptOutHeaderType.SYSTEM_DATA_CREATED_BY_LAST_MODIFIED_BY))
                .withResourceConcurrencyControlOptions(mapOf("patch",
                    new ResourceConcurrencyControlOption().withPolicy(Policy.SYNCHRONIZE_BEGIN_EXTENSION), "post",
                    new ResourceConcurrencyControlOption().withPolicy(Policy.SYNCHRONIZE_BEGIN_EXTENSION), "put",
                    new ResourceConcurrencyControlOption().withPolicy(Policy.SYNCHRONIZE_BEGIN_EXTENSION)))
                .withResourceGraphConfiguration(
                    new ResourceTypeRegistrationPropertiesResourceGraphConfiguration().withEnabled(true)
                        .withApiVersion("2019-01-01"))
                .withManagement(new ResourceTypeRegistrationPropertiesManagement()
                    .withManifestOwners(Arrays.asList("SPARTA-PlatformServiceAdministrator"))
                    .withAuthorizationOwners(Arrays.asList("RPAAS-PlatformServiceAdministrator"))
                    .withIncidentRoutingService("")
                    .withIncidentRoutingTeam("")
                    .withIncidentContactEmail("helpme@contoso.com")
                    .withServiceTreeInfos(
                        Arrays.asList(new ServiceTreeInfo().withServiceId("d1b7d8ba-05e2-48e6-90d6-d781b99c6e69")
                            .withComponentId("d1b7d8ba-05e2-48e6-90d6-d781b99c6e69")
                            .withReadiness(Readiness.IN_DEVELOPMENT)))
                    .withResourceAccessPolicy(ResourceAccessPolicy.NOT_SPECIFIED))
                .withOpenApiConfiguration(new OpenApiConfiguration()
                    .withValidation(new OpenApiValidation().withAllowNoncompliantCollectionResponse(true)))
                .withMetadata(mapOf())
                .withNotifications(
                    Arrays.asList(new Notification().withNotificationType(NotificationType.SUBSCRIPTION_NOTIFICATION)
                        .withSkipNotifications(SkipNotifications.DISABLED))))
            .create();
    }

    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * DirectResourceTypeRegistrations_CreateOrUpdate.json
     */
    /**
     * Sample code: DirectResourceTypeRegistrations_CreateOrUpdate.json.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void directResourceTypeRegistrationsCreateOrUpdateJson(
        com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.resourceTypeRegistrations()
            .define("employees")
            .withExistingProviderRegistration("Microsoft.Contoso")
            .withProperties(new ResourceTypeRegistrationProperties().withRoutingType(RoutingType.DEFAULT)
                .withAdditionalOptions(AdditionalOptionsResourceTypeRegistration.PROTECTED_ASYNC_OPERATION_POLLING)
                .withRegionality(Regionality.REGIONAL)
                .withEndpoints(
                    Arrays.asList(new ResourceTypeEndpoint().withApiVersions(Arrays.asList("2020-06-01-preview"))
                        .withLocations(Arrays.asList("West US", "East US", "North Europe"))
                        .withRequiredFeatures(Arrays.asList("<feature flag>"))))
                .withSwaggerSpecifications(Arrays.asList(new SwaggerSpecification()
                    .withApiVersions(Arrays.asList("2020-06-01-preview"))
                    .withSwaggerSpecFolderUri(
                        "https://github.com/Azure/azure-rest-api-specs/blob/feature/azure/contoso/specification/contoso/resource-manager/Microsoft.SampleRP/")))
                .withRequestHeaderOptions(new ResourceTypeRegistrationPropertiesRequestHeaderOptions()
                    .withOptOutHeaders(OptOutHeaderType.SYSTEM_DATA_CREATED_BY_LAST_MODIFIED_BY))
                .withResourceConcurrencyControlOptions(mapOf("patch",
                    new ResourceConcurrencyControlOption().withPolicy(Policy.SYNCHRONIZE_BEGIN_EXTENSION), "post",
                    new ResourceConcurrencyControlOption().withPolicy(Policy.SYNCHRONIZE_BEGIN_EXTENSION), "put",
                    new ResourceConcurrencyControlOption().withPolicy(Policy.SYNCHRONIZE_BEGIN_EXTENSION)))
                .withResourceGraphConfiguration(
                    new ResourceTypeRegistrationPropertiesResourceGraphConfiguration().withEnabled(true)
                        .withApiVersion("2019-01-01"))
                .withManagement(new ResourceTypeRegistrationPropertiesManagement()
                    .withManifestOwners(Arrays.asList("SPARTA-PlatformServiceAdministrator"))
                    .withAuthorizationOwners(Arrays.asList("RPAAS-PlatformServiceAdministrator"))
                    .withIncidentRoutingService("")
                    .withIncidentRoutingTeam("")
                    .withIncidentContactEmail("helpme@contoso.com")
                    .withServiceTreeInfos(
                        Arrays.asList(new ServiceTreeInfo().withServiceId("d1b7d8ba-05e2-48e6-90d6-d781b99c6e69")
                            .withComponentId("d1b7d8ba-05e2-48e6-90d6-d781b99c6e69")
                            .withReadiness(Readiness.IN_DEVELOPMENT)))
                    .withResourceAccessPolicy(ResourceAccessPolicy.NOT_SPECIFIED))
                .withOpenApiConfiguration(new OpenApiConfiguration()
                    .withValidation(new OpenApiValidation().withAllowNoncompliantCollectionResponse(true)))
                .withMetadata(mapOf())
                .withNotifications(
                    Arrays.asList(new Notification().withNotificationType(NotificationType.SUBSCRIPTION_NOTIFICATION)
                        .withSkipNotifications(SkipNotifications.DISABLED)))
                .withTemplateDeploymentPolicy(new ResourceTypeRegistrationPropertiesTemplateDeploymentPolicy()
                    .withCapabilities(TemplateDeploymentCapabilities.PREFLIGHT)
                    .withPreflightOptions(
                        TemplateDeploymentPreflightOptions.fromString("ValidationRequests, DeploymentRequests"))
                    .withPreflightNotifications(TemplateDeploymentPreflightNotifications.NONE))
                .withAllowEmptyRoleAssignments(false)
                .withPolicyExecutionType(PolicyExecutionType.BYPASS_POLICIES)
                .withAvailabilityZoneRule(new ResourceTypeRegistrationPropertiesAvailabilityZoneRule()
                    .withAvailabilityZonePolicy(AvailabilityZonePolicy.MULTI_ZONED))
                .withDstsConfiguration(
                    new ResourceTypeRegistrationPropertiesDstsConfiguration().withServiceName("prds-shim")
                        .withServiceDnsName("prds.sparta.azure.com"))
                .withAsyncTimeoutRules(
                    Arrays.asList(new AsyncTimeoutRule().withActionName("Microsoft.ClassicCompute/domainNames/write")
                        .withTimeout("PT12H")))
                .withCommonApiVersions(Arrays.asList("2021-01-01"))
                .withApiProfiles(
                    Arrays.asList(new ApiProfile().withProfileVersion("2018-03-01-hybrid").withApiVersion("2018-02-01"),
                        new ApiProfile().withProfileVersion("2019-03-01-hybrid").withApiVersion("2016-06-01")))
                .withLinkedOperationRules(Arrays.asList(
                    new LinkedOperationRule().withLinkedOperation(LinkedOperation.CROSS_SUBSCRIPTION_RESOURCE_MOVE)
                        .withLinkedAction(LinkedAction.BLOCKED),
                    new LinkedOperationRule().withLinkedOperation(LinkedOperation.CROSS_RESOURCE_GROUP_RESOURCE_MOVE)
                        .withLinkedAction(LinkedAction.VALIDATE)))
                .withLegacyName("legacyName")
                .withLegacyNames(Arrays.asList("legacyName"))
                .withAllowedTemplateDeploymentReferenceActions(Arrays.asList("ListKeys", "ListSAS"))
                .withLegacyPolicy(new ResourceTypeRegistrationPropertiesLegacyPolicy()
                    .withDisallowedLegacyOperations(Arrays.asList(LegacyOperation.CREATE))
                    .withDisallowedConditions(Arrays.asList(new LegacyDisallowedCondition()
                        .withDisallowedLegacyOperations(Arrays.asList(LegacyOperation.CREATE, LegacyOperation.DELETE))
                        .withFeature("Microsoft.RP/ArmOnlyJobCollections"))))
                .withManifestLink("https://azure.com")
                .withCapacityRule(
                    new ResourceTypeRegistrationPropertiesCapacityRule().withCapacityPolicy(CapacityPolicy.RESTRICTED)
                        .withSkuAlias("incorrectAlias"))
                .withMarketplaceOptions(
                    new ResourceTypeRegistrationPropertiesMarketplaceOptions().withAddOnPlanConversionAllowed(true))
                .withAllowedResourceNames(
                    Arrays.asList(new AllowedResourceName().withName("name1").withGetActionVerb("list"),
                        new AllowedResourceName().withName("name2")))
                .withResourceCache(new ResourceTypeRegistrationPropertiesResourceCache().withEnableResourceCache(true)
                    .withResourceCacheExpirationTimespan("PT2M"))
                .withResourceQueryManagement(new ResourceTypeRegistrationPropertiesResourceQueryManagement()
                    .withFilterOption(FilterOption.ENABLE_SUBSCRIPTION_FILTER_ON_TENANT))
                .withSupportsTags(true)
                .withResourceManagementOptions(new ResourceTypeRegistrationPropertiesResourceManagementOptions()
                    .withBatchProvisioningSupport(
                        new ResourceTypeRegistrationPropertiesResourceManagementOptionsBatchProvisioningSupport()
                            .withSupportedOperations(SupportedOperations.fromString("Get, Delete")))
                    .withDeleteDependencies(Arrays
                        .asList(new DeleteDependency().withLinkedProperty("properties.edgeProfile.subscription.id"))))
                .withGroupingTag("groupingTag")
                .withAddResourceListTargetLocations(true)
                .withResourceTypeCommonAttributeManagement(
                    new ResourceTypeRegistrationPropertiesResourceTypeCommonAttributeManagement()
                        .withCommonApiVersionsMergeMode(CommonApiVersionsMergeMode.MERGE))
                .withRoutingRule(
                    new ResourceTypeRegistrationPropertiesRoutingRule().withHostResourceType("servers/databases")))
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

### ResourceTypeRegistrations_Delete

```java
/**
 * Samples for ResourceTypeRegistrations Delete.
 */
public final class ResourceTypeRegistrationsDeleteSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * ResourceTypeRegistrations_Delete.json
     */
    /**
     * Sample code: ResourceTypeRegistrations_Delete.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void
        resourceTypeRegistrationsDelete(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.resourceTypeRegistrations()
            .delete("Microsoft.Contoso", "testResourceType", com.azure.core.util.Context.NONE);
    }
}
```

### ResourceTypeRegistrations_Get

```java
/**
 * Samples for ResourceTypeRegistrations Get.
 */
public final class ResourceTypeRegistrationsGetSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * ResourceTypeRegistrations_Get.json
     */
    /**
     * Sample code: ResourceTypeRegistrations_Get.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void resourceTypeRegistrationsGet(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.resourceTypeRegistrations()
            .getWithResponse("Microsoft.Contoso", "employees", com.azure.core.util.Context.NONE);
    }
}
```

### ResourceTypeRegistrations_ListByProviderRegistration

```java
/**
 * Samples for ResourceTypeRegistrations ListByProviderRegistration.
 */
public final class ResourceTypeRegistrationsListByProviderRegistrationSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * ResourceTypeRegistrations_ListByProviderRegistration.json
     */
    /**
     * Sample code: ResourceTypeRegistrations_ListByProviderRegistration.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void resourceTypeRegistrationsListByProviderRegistration(
        com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.resourceTypeRegistrations()
            .listByProviderRegistration("Microsoft.Contoso", com.azure.core.util.Context.NONE);
    }
}
```

### Skus_CreateOrUpdate

```java
import com.azure.resourcemanager.providerhub.models.SkuCost;
import com.azure.resourcemanager.providerhub.models.SkuResourceProperties;
import com.azure.resourcemanager.providerhub.models.SkuSetting;
import java.util.Arrays;

/**
 * Samples for Skus CreateOrUpdate.
 */
public final class SkusCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/Skus_CreateOrUpdate.
     * json
     */
    /**
     * Sample code: Skus_CreateOrUpdate.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void skusCreateOrUpdate(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.skus()
            .define("testSku")
            .withExistingResourcetypeRegistration("Microsoft.Contoso", "testResourceType")
            .withProperties(new SkuResourceProperties().withSkuSettings(
                Arrays.asList(new SkuSetting().withName("freeSku").withTier("Tier1").withKind("Standard"),
                    new SkuSetting().withName("premiumSku")
                        .withTier("Tier2")
                        .withKind("Premium")
                        .withCosts(Arrays.asList(new SkuCost().withMeterId("xxx"))))))
            .create();
    }
}
```

### Skus_CreateOrUpdateNestedResourceTypeFirst

```java
import com.azure.resourcemanager.providerhub.fluent.models.SkuResourceInner;
import com.azure.resourcemanager.providerhub.models.SkuCost;
import com.azure.resourcemanager.providerhub.models.SkuResourceProperties;
import com.azure.resourcemanager.providerhub.models.SkuSetting;
import java.util.Arrays;

/**
 * Samples for Skus CreateOrUpdateNestedResourceTypeFirst.
 */
public final class SkusCreateOrUpdateNestedResourceTypeFirstSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * Skus_CreateOrUpdateNestedResourceTypeFirst.json
     */
    /**
     * Sample code: Skus_CreateOrUpdateNestedResourceTypeFirst.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void
        skusCreateOrUpdateNestedResourceTypeFirst(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.skus()
            .createOrUpdateNestedResourceTypeFirstWithResponse("Microsoft.Contoso", "testResourceType",
                "nestedResourceTypeFirst", "testSku",
                new SkuResourceInner().withProperties(new SkuResourceProperties().withSkuSettings(
                    Arrays.asList(new SkuSetting().withName("freeSku").withTier("Tier1").withKind("Standard"),
                        new SkuSetting().withName("premiumSku")
                            .withTier("Tier2")
                            .withKind("Premium")
                            .withCosts(Arrays.asList(new SkuCost().withMeterId("xxx")))))),
                com.azure.core.util.Context.NONE);
    }
}
```

### Skus_CreateOrUpdateNestedResourceTypeSecond

```java
import com.azure.resourcemanager.providerhub.fluent.models.SkuResourceInner;
import com.azure.resourcemanager.providerhub.models.SkuCost;
import com.azure.resourcemanager.providerhub.models.SkuResourceProperties;
import com.azure.resourcemanager.providerhub.models.SkuSetting;
import java.util.Arrays;

/**
 * Samples for Skus CreateOrUpdateNestedResourceTypeSecond.
 */
public final class SkusCreateOrUpdateNestedResourceTypeSecondSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * Skus_CreateOrUpdateNestedResourceTypeSecond.json
     */
    /**
     * Sample code: Skus_CreateOrUpdateNestedResourceTypeSecond.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void
        skusCreateOrUpdateNestedResourceTypeSecond(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.skus()
            .createOrUpdateNestedResourceTypeSecondWithResponse("Microsoft.Contoso", "testResourceType",
                "nestedResourceTypeFirst", "nestedResourceTypeSecond", "testSku",
                new SkuResourceInner().withProperties(new SkuResourceProperties().withSkuSettings(
                    Arrays.asList(new SkuSetting().withName("freeSku").withTier("Tier1").withKind("Standard"),
                        new SkuSetting().withName("premiumSku")
                            .withTier("Tier2")
                            .withKind("Premium")
                            .withCosts(Arrays.asList(new SkuCost().withMeterId("xxx")))))),
                com.azure.core.util.Context.NONE);
    }
}
```

### Skus_CreateOrUpdateNestedResourceTypeThird

```java
import com.azure.resourcemanager.providerhub.fluent.models.SkuResourceInner;
import com.azure.resourcemanager.providerhub.models.SkuCost;
import com.azure.resourcemanager.providerhub.models.SkuResourceProperties;
import com.azure.resourcemanager.providerhub.models.SkuSetting;
import java.util.Arrays;

/**
 * Samples for Skus CreateOrUpdateNestedResourceTypeThird.
 */
public final class SkusCreateOrUpdateNestedResourceTypeThirdSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * Skus_CreateOrUpdateNestedResourceTypeThird.json
     */
    /**
     * Sample code: Skus_CreateOrUpdateNestedResourceTypeThird.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void
        skusCreateOrUpdateNestedResourceTypeThird(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.skus()
            .createOrUpdateNestedResourceTypeThirdWithResponse("Microsoft.Contoso", "testResourceType",
                "nestedResourceTypeFirst", "nestedResourceTypeSecond", "nestedResourceTypeThird", "testSku",
                new SkuResourceInner().withProperties(new SkuResourceProperties().withSkuSettings(
                    Arrays.asList(new SkuSetting().withName("freeSku").withTier("Tier1").withKind("Standard"),
                        new SkuSetting().withName("premiumSku")
                            .withTier("Tier2")
                            .withKind("Premium")
                            .withCosts(Arrays.asList(new SkuCost().withMeterId("xxx")))))),
                com.azure.core.util.Context.NONE);
    }
}
```

### Skus_Delete

```java
/**
 * Samples for Skus Delete.
 */
public final class SkusDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/Skus_Delete.json
     */
    /**
     * Sample code: Skus_Delete.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void skusDelete(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.skus()
            .deleteWithResponse("Microsoft.Contoso", "testResourceType", "testSku", com.azure.core.util.Context.NONE);
    }
}
```

### Skus_DeleteNestedResourceTypeFirst

```java
/**
 * Samples for Skus DeleteNestedResourceTypeFirst.
 */
public final class SkusDeleteNestedResourceTypeFirstSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * Skus_DeleteNestedResourceTypeFirst.json
     */
    /**
     * Sample code: Skus_DeleteNestedResourceTypeFirst.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void
        skusDeleteNestedResourceTypeFirst(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.skus()
            .deleteNestedResourceTypeFirstWithResponse("Microsoft.Contoso", "testResourceType",
                "nestedResourceTypeFirst", "testSku", com.azure.core.util.Context.NONE);
    }
}
```

### Skus_DeleteNestedResourceTypeSecond

```java
/**
 * Samples for Skus DeleteNestedResourceTypeSecond.
 */
public final class SkusDeleteNestedResourceTypeSecondSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * Skus_DeleteNestedResourceTypeSecond.json
     */
    /**
     * Sample code: Skus_DeleteNestedResourceTypeSecond.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void
        skusDeleteNestedResourceTypeSecond(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.skus()
            .deleteNestedResourceTypeSecondWithResponse("Microsoft.Contoso", "testResourceType",
                "nestedResourceTypeFirst", "nestedResourceTypeSecond", "testSku", com.azure.core.util.Context.NONE);
    }
}
```

### Skus_DeleteNestedResourceTypeThird

```java
/**
 * Samples for Skus DeleteNestedResourceTypeThird.
 */
public final class SkusDeleteNestedResourceTypeThirdSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * Skus_DeleteNestedResourceTypeThird.json
     */
    /**
     * Sample code: Skus_DeleteNestedResourceTypeThird.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void
        skusDeleteNestedResourceTypeThird(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.skus()
            .deleteNestedResourceTypeThirdWithResponse("Microsoft.Contoso", "testResourceType",
                "nestedResourceTypeFirst", "nestedResourceTypeSecond", "nestedResourceTypeThird", "testSku",
                com.azure.core.util.Context.NONE);
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
     * specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/Skus_Get.json
     */
    /**
     * Sample code: Skus_Get.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void skusGet(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.skus()
            .getWithResponse("Microsoft.Contoso", "testResourceType", "testSku", com.azure.core.util.Context.NONE);
    }
}
```

### Skus_GetNestedResourceTypeFirst

```java
/**
 * Samples for Skus GetNestedResourceTypeFirst.
 */
public final class SkusGetNestedResourceTypeFirstSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * Skus_GetNestedResourceTypeFirst.json
     */
    /**
     * Sample code: Skus_GetNestedResourceTypeFirst.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void
        skusGetNestedResourceTypeFirst(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.skus()
            .getNestedResourceTypeFirstWithResponse("Microsoft.Contoso", "testResourceType", "nestedResourceTypeFirst",
                "testSku", com.azure.core.util.Context.NONE);
    }
}
```

### Skus_GetNestedResourceTypeSecond

```java
/**
 * Samples for Skus GetNestedResourceTypeSecond.
 */
public final class SkusGetNestedResourceTypeSecondSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * Skus_GetNestedResourceTypeSecond.json
     */
    /**
     * Sample code: Skus_GetNestedResourceTypeSecond.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void
        skusGetNestedResourceTypeSecond(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.skus()
            .getNestedResourceTypeSecondWithResponse("Microsoft.Contoso", "testResourceType", "nestedResourceTypeFirst",
                "nestedResourceTypeSecond", "testSku", com.azure.core.util.Context.NONE);
    }
}
```

### Skus_GetNestedResourceTypeThird

```java
/**
 * Samples for Skus GetNestedResourceTypeThird.
 */
public final class SkusGetNestedResourceTypeThirdSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * Skus_GetNestedResourceTypeThird.json
     */
    /**
     * Sample code: Skus_GetNestedResourceTypeThird.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void
        skusGetNestedResourceTypeThird(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.skus()
            .getNestedResourceTypeThirdWithResponse("Microsoft.Contoso", "testResourceType", "nestedResourceTypeFirst",
                "nestedResourceTypeSecond", "nestedResourceTypeThird", "testSku", com.azure.core.util.Context.NONE);
    }
}
```

### Skus_ListByResourceTypeRegistrations

```java
/**
 * Samples for Skus ListByResourceTypeRegistrations.
 */
public final class SkusListByResourceTypeRegistrationsSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * Skus_ListByResourceTypeRegistrations.json
     */
    /**
     * Sample code: Skus_ListByResourceTypeRegistrations.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void
        skusListByResourceTypeRegistrations(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.skus()
            .listByResourceTypeRegistrations("Microsoft.Contoso", "testResourceType", com.azure.core.util.Context.NONE);
    }
}
```

### Skus_ListByResourceTypeRegistrationsNestedResourceTypeFirst

```java
/**
 * Samples for Skus ListByResourceTypeRegistrationsNestedResourceTypeFirst.
 */
public final class SkusListByResourceTypeRegistrationsNestedResourceTypeFirstSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * Skus_ListByResourceTypeRegistrationsNestedResourceTypeFirst.json
     */
    /**
     * Sample code: Skus_ListByResourceTypeRegistrationsNestedResourceTypeFirst.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void skusListByResourceTypeRegistrationsNestedResourceTypeFirst(
        com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.skus()
            .listByResourceTypeRegistrationsNestedResourceTypeFirst("Microsoft.Contoso", "testResourceType",
                "nestedResourceTypeFirst", com.azure.core.util.Context.NONE);
    }
}
```

### Skus_ListByResourceTypeRegistrationsNestedResourceTypeSecond

```java
/**
 * Samples for Skus ListByResourceTypeRegistrationsNestedResourceTypeSecond.
 */
public final class SkusListByResourceTypeRegistrationsNestedResourceTypeSecondSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * Skus_ListByResourceTypeRegistrationsNestedResourceTypeSecond.json
     */
    /**
     * Sample code: Skus_ListByResourceTypeRegistrationsNestedResourceTypeSecond.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void skusListByResourceTypeRegistrationsNestedResourceTypeSecond(
        com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.skus()
            .listByResourceTypeRegistrationsNestedResourceTypeSecond("Microsoft.Contoso", "testResourceType",
                "nestedResourceTypeFirst", "nestedResourceTypeSecond", com.azure.core.util.Context.NONE);
    }
}
```

### Skus_ListByResourceTypeRegistrationsNestedResourceTypeThird

```java
/**
 * Samples for Skus ListByResourceTypeRegistrationsNestedResourceTypeThird.
 */
public final class SkusListByResourceTypeRegistrationsNestedResourceTypeThirdSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2024-09-01/examples/
     * Skus_ListByResourceTypeRegistrationsNestedResourceTypeThird.json
     */
    /**
     * Sample code: Skus_ListByResourceTypeRegistrationsNestedResourceTypeThird.
     * 
     * @param manager Entry point to ProviderHubManager.
     */
    public static void skusListByResourceTypeRegistrationsNestedResourceTypeThird(
        com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.skus()
            .listByResourceTypeRegistrationsNestedResourceTypeThird("Microsoft.Contoso", "testResourceType",
                "nestedResourceTypeFirst", "nestedResourceTypeSecond", "nestedResourceTypeThird",
                com.azure.core.util.Context.NONE);
    }
}
```

