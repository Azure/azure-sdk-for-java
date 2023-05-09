# Code snippets and samples


## CustomRollouts

- [CreateOrUpdate](#customrollouts_createorupdate)
- [Get](#customrollouts_get)
- [ListByProviderRegistration](#customrollouts_listbyproviderregistration)

## DefaultRollouts

- [CreateOrUpdate](#defaultrollouts_createorupdate)
- [Delete](#defaultrollouts_delete)
- [Get](#defaultrollouts_get)
- [ListByProviderRegistration](#defaultrollouts_listbyproviderregistration)
- [Stop](#defaultrollouts_stop)

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

## ProviderRegistrations

- [CreateOrUpdate](#providerregistrations_createorupdate)
- [Delete](#providerregistrations_delete)
- [GenerateOperations](#providerregistrations_generateoperations)
- [Get](#providerregistrations_get)
- [List](#providerregistrations_list)

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
### CustomRollouts_CreateOrUpdate

```java
import com.azure.resourcemanager.providerhub.models.CustomRolloutProperties;
import com.azure.resourcemanager.providerhub.models.CustomRolloutPropertiesSpecification;
import com.azure.resourcemanager.providerhub.models.CustomRolloutSpecificationCanary;
import java.util.Arrays;

/** Samples for CustomRollouts CreateOrUpdate. */
public final class CustomRolloutsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2020-11-20/examples/CustomRollouts_CreateOrUpdate.json
     */
    /**
     * Sample code: CustomRollouts_CreateOrUpdate.
     *
     * @param manager Entry point to ProviderHubManager.
     */
    public static void customRolloutsCreateOrUpdate(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager
            .customRollouts()
            .define("brazilUsShoeBoxTesting")
            .withExistingProviderRegistration("Microsoft.Contoso")
            .withProperties(
                new CustomRolloutProperties()
                    .withSpecification(
                        new CustomRolloutPropertiesSpecification()
                            .withCanary(new CustomRolloutSpecificationCanary().withRegions(Arrays.asList("brazilus")))))
            .create();
    }
}
```

### CustomRollouts_Get

```java
/** Samples for CustomRollouts Get. */
public final class CustomRolloutsGetSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2020-11-20/examples/CustomRollouts_Get.json
     */
    /**
     * Sample code: CustomRollouts_Get.
     *
     * @param manager Entry point to ProviderHubManager.
     */
    public static void customRolloutsGet(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager
            .customRollouts()
            .getWithResponse("Microsoft.Contoso", "canaryTesting99", com.azure.core.util.Context.NONE);
    }
}
```

### CustomRollouts_ListByProviderRegistration

```java
/** Samples for CustomRollouts ListByProviderRegistration. */
public final class CustomRolloutsListByProviderRegistrationSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2020-11-20/examples/CustomRollouts_ListByProviderRegistration.json
     */
    /**
     * Sample code: CustomRollouts_ListByProviderRegistration.
     *
     * @param manager Entry point to ProviderHubManager.
     */
    public static void customRolloutsListByProviderRegistration(
        com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.customRollouts().listByProviderRegistration("Microsoft.Contoso", com.azure.core.util.Context.NONE);
    }
}
```

### DefaultRollouts_CreateOrUpdate

```java
import com.azure.resourcemanager.providerhub.models.DefaultRolloutProperties;
import com.azure.resourcemanager.providerhub.models.DefaultRolloutPropertiesSpecification;
import com.azure.resourcemanager.providerhub.models.DefaultRolloutSpecificationCanary;
import com.azure.resourcemanager.providerhub.models.DefaultRolloutSpecificationRestOfTheWorldGroupTwo;
import java.time.Duration;
import java.util.Arrays;

/** Samples for DefaultRollouts CreateOrUpdate. */
public final class DefaultRolloutsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2020-11-20/examples/DefaultRollouts_CreateOrUpdate.json
     */
    /**
     * Sample code: DefaultRollouts_CreateOrUpdate.
     *
     * @param manager Entry point to ProviderHubManager.
     */
    public static void defaultRolloutsCreateOrUpdate(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager
            .defaultRollouts()
            .define("2020week10")
            .withExistingProviderRegistration("Microsoft.Contoso")
            .withProperties(
                new DefaultRolloutProperties()
                    .withSpecification(
                        new DefaultRolloutPropertiesSpecification()
                            .withCanary(
                                new DefaultRolloutSpecificationCanary().withSkipRegions(Arrays.asList("eastus2euap")))
                            .withRestOfTheWorldGroupTwo(
                                new DefaultRolloutSpecificationRestOfTheWorldGroupTwo()
                                    .withWaitDuration(Duration.parse("PT4H")))))
            .create();
    }
}
```

### DefaultRollouts_Delete

```java
/** Samples for DefaultRollouts Delete. */
public final class DefaultRolloutsDeleteSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2020-11-20/examples/DefaultRollouts_Delete.json
     */
    /**
     * Sample code: DefaultRollouts_Delete.
     *
     * @param manager Entry point to ProviderHubManager.
     */
    public static void defaultRolloutsDelete(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager
            .defaultRollouts()
            .deleteByResourceGroupWithResponse("Microsoft.Contoso", "2020week10", com.azure.core.util.Context.NONE);
    }
}
```

### DefaultRollouts_Get

```java
/** Samples for DefaultRollouts Get. */
public final class DefaultRolloutsGetSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2020-11-20/examples/DefaultRollouts_Get.json
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
/** Samples for DefaultRollouts ListByProviderRegistration. */
public final class DefaultRolloutsListByProviderRegistrationSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2020-11-20/examples/DefaultRollouts_ListByProviderRegistration.json
     */
    /**
     * Sample code: DefaultRollouts_ListByProviderRegistration.
     *
     * @param manager Entry point to ProviderHubManager.
     */
    public static void defaultRolloutsListByProviderRegistration(
        com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager.defaultRollouts().listByProviderRegistration("Microsoft.Contoso", com.azure.core.util.Context.NONE);
    }
}
```

### DefaultRollouts_Stop

```java
/** Samples for DefaultRollouts Stop. */
public final class DefaultRolloutsStopSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2020-11-20/examples/DefaultRollouts_Stop.json
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

### NotificationRegistrations_CreateOrUpdate

```java
import com.azure.resourcemanager.providerhub.models.MessageScope;
import com.azure.resourcemanager.providerhub.models.NotificationEndpoint;
import com.azure.resourcemanager.providerhub.models.NotificationMode;
import com.azure.resourcemanager.providerhub.models.NotificationRegistrationProperties;
import java.util.Arrays;

/** Samples for NotificationRegistrations CreateOrUpdate. */
public final class NotificationRegistrationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2020-11-20/examples/NotificationRegistrations_CreateOrUpdate.json
     */
    /**
     * Sample code: NotificationRegistrations_CreateOrUpdate.
     *
     * @param manager Entry point to ProviderHubManager.
     */
    public static void notificationRegistrationsCreateOrUpdate(
        com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager
            .notificationRegistrations()
            .define("fooNotificationRegistration")
            .withExistingProviderRegistration("Microsoft.Contoso")
            .withProperties(
                new NotificationRegistrationProperties()
                    .withNotificationMode(NotificationMode.EVENT_HUB)
                    .withMessageScope(MessageScope.REGISTERED_SUBSCRIPTIONS)
                    .withIncludedEvents(Arrays.asList("*/write", "Microsoft.Contoso/employees/delete"))
                    .withNotificationEndpoints(
                        Arrays
                            .asList(
                                new NotificationEndpoint()
                                    .withNotificationDestination(
                                        "/subscriptions/ac6bcfb5-3dc1-491f-95a6-646b89bf3e88/resourceGroups/mgmtexp-eastus/providers/Microsoft.EventHub/namespaces/unitedstates-mgmtexpint/eventhubs/armlinkednotifications")
                                    .withLocations(Arrays.asList("", "East US")),
                                new NotificationEndpoint()
                                    .withNotificationDestination(
                                        "/subscriptions/ac6bcfb5-3dc1-491f-95a6-646b89bf3e88/resourceGroups/mgmtexp-northeurope/providers/Microsoft.EventHub/namespaces/europe-mgmtexpint/eventhubs/armlinkednotifications")
                                    .withLocations(Arrays.asList("North Europe")))))
            .create();
    }
}
```

### NotificationRegistrations_Delete

```java
/** Samples for NotificationRegistrations Delete. */
public final class NotificationRegistrationsDeleteSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2020-11-20/examples/NotificationRegistrations_Delete.json
     */
    /**
     * Sample code: NotificationRegistrations_Delete.
     *
     * @param manager Entry point to ProviderHubManager.
     */
    public static void notificationRegistrationsDelete(
        com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager
            .notificationRegistrations()
            .deleteByResourceGroupWithResponse(
                "Microsoft.Contoso", "fooNotificationRegistration", com.azure.core.util.Context.NONE);
    }
}
```

### NotificationRegistrations_Get

```java
/** Samples for NotificationRegistrations Get. */
public final class NotificationRegistrationsGetSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2020-11-20/examples/NotificationRegistrations_Get.json
     */
    /**
     * Sample code: NotificationRegistrations_Get.
     *
     * @param manager Entry point to ProviderHubManager.
     */
    public static void notificationRegistrationsGet(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager
            .notificationRegistrations()
            .getWithResponse("Microsoft.Contoso", "fooNotificationRegistration", com.azure.core.util.Context.NONE);
    }
}
```

### NotificationRegistrations_ListByProviderRegistration

```java
/** Samples for NotificationRegistrations ListByProviderRegistration. */
public final class NotificationRegistrationsListByProviderRegistrationSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2020-11-20/examples/NotificationRegistrations_ListByProviderRegistration.json
     */
    /**
     * Sample code: NotificationRegistrations_ListByProviderRegistration.
     *
     * @param manager Entry point to ProviderHubManager.
     */
    public static void notificationRegistrationsListByProviderRegistration(
        com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager
            .notificationRegistrations()
            .listByProviderRegistration("Microsoft.Contoso", com.azure.core.util.Context.NONE);
    }
}
```

### Operations_CreateOrUpdate

```java
import com.azure.resourcemanager.providerhub.fluent.models.OperationsDefinitionInner;
import com.azure.resourcemanager.providerhub.models.OperationsDefinitionDisplay;
import com.azure.resourcemanager.providerhub.models.OperationsPutContent;
import java.util.Arrays;

/** Samples for Operations CreateOrUpdate. */
public final class OperationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2020-11-20/examples/Operations_CreateOrUpdate.json
     */
    /**
     * Sample code: Operations_CreateOrUpdate.
     *
     * @param manager Entry point to ProviderHubManager.
     */
    public static void operationsCreateOrUpdate(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager
            .operations()
            .createOrUpdateWithResponse(
                "Microsoft.Contoso",
                new OperationsPutContent()
                    .withContents(
                        Arrays
                            .asList(
                                new OperationsDefinitionInner()
                                    .withName("Microsoft.Contoso/Employees/Read")
                                    .withDisplay(
                                        new OperationsDefinitionDisplay()
                                            .withProvider("Microsoft.Contoso")
                                            .withResource("Employees")
                                            .withOperation("Gets/List employee resources")
                                            .withDescription("Read employees")))),
                com.azure.core.util.Context.NONE);
    }
}
```

### Operations_Delete

```java
/** Samples for Operations Delete. */
public final class OperationsDeleteSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2020-11-20/examples/Operations_Delete.json
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
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2020-11-20/examples/Operations_List.json
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
/** Samples for Operations ListByProviderRegistration. */
public final class OperationsListByProviderRegistrationSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2020-11-20/examples/Operations_ListByProviderRegistration.json
     */
    /**
     * Sample code: Operations_ListByProviderRegistration.
     *
     * @param manager Entry point to ProviderHubManager.
     */
    public static void operationsListByProviderRegistration(
        com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager
            .operations()
            .listByProviderRegistrationWithResponse("Microsoft.Contoso", com.azure.core.util.Context.NONE);
    }
}
```

### ProviderRegistrations_CreateOrUpdate

```java
import com.azure.resourcemanager.providerhub.fluent.models.ProviderRegistrationInner;
import com.azure.resourcemanager.providerhub.models.ProviderRegistrationProperties;
import com.azure.resourcemanager.providerhub.models.ResourceProviderCapabilities;
import com.azure.resourcemanager.providerhub.models.ResourceProviderCapabilitiesEffect;
import com.azure.resourcemanager.providerhub.models.ResourceProviderManifestPropertiesManagement;
import com.azure.resourcemanager.providerhub.models.ResourceProviderType;
import java.util.Arrays;

/** Samples for ProviderRegistrations CreateOrUpdate. */
public final class ProviderRegistrationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2020-11-20/examples/ProviderRegistrations_CreateOrUpdate.json
     */
    /**
     * Sample code: ProviderRegistrations_CreateOrUpdate.
     *
     * @param manager Entry point to ProviderHubManager.
     */
    public static void providerRegistrationsCreateOrUpdate(
        com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager
            .providerRegistrations()
            .createOrUpdate(
                "Microsoft.Contoso",
                new ProviderRegistrationInner()
                    .withProperties(
                        new ProviderRegistrationProperties()
                            .withProviderVersion("2.0")
                            .withProviderType(ResourceProviderType.INTERNAL)
                            .withManagement(
                                new ResourceProviderManifestPropertiesManagement()
                                    .withIncidentRoutingService("Contoso Resource Provider")
                                    .withIncidentRoutingTeam("Contoso Triage")
                                    .withIncidentContactEmail("helpme@contoso.com"))
                            .withCapabilities(
                                Arrays
                                    .asList(
                                        new ResourceProviderCapabilities()
                                            .withQuotaId("CSP_2015-05-01")
                                            .withEffect(ResourceProviderCapabilitiesEffect.ALLOW),
                                        new ResourceProviderCapabilities()
                                            .withQuotaId("CSP_MG_2017-12-01")
                                            .withEffect(ResourceProviderCapabilitiesEffect.ALLOW)))),
                com.azure.core.util.Context.NONE);
    }
}
```

### ProviderRegistrations_Delete

```java
/** Samples for ProviderRegistrations Delete. */
public final class ProviderRegistrationsDeleteSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2020-11-20/examples/ProviderRegistrations_Delete.json
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
/** Samples for ProviderRegistrations GenerateOperations. */
public final class ProviderRegistrationsGenerateOperationsSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2020-11-20/examples/ProviderRegistrations_GenerateOperations.json
     */
    /**
     * Sample code: ProviderRegistrations_GenerateOperations.
     *
     * @param manager Entry point to ProviderHubManager.
     */
    public static void providerRegistrationsGenerateOperations(
        com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager
            .providerRegistrations()
            .generateOperationsWithResponse("Microsoft.Contoso", com.azure.core.util.Context.NONE);
    }
}
```

### ProviderRegistrations_Get

```java
/** Samples for ProviderRegistrations Get. */
public final class ProviderRegistrationsGetSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2020-11-20/examples/ProviderRegistrations_Get.json
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
/** Samples for ProviderRegistrations List. */
public final class ProviderRegistrationsListSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2020-11-20/examples/ProviderRegistrations_List.json
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

### ResourceProvider_CheckinManifest

```java
import com.azure.resourcemanager.providerhub.models.CheckinManifestParams;

/** Samples for ResourceProvider CheckinManifest. */
public final class ResourceProviderCheckinManifestSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2020-11-20/examples/CheckinManifest.json
     */
    /**
     * Sample code: CheckinManifest.
     *
     * @param manager Entry point to ProviderHubManager.
     */
    public static void checkinManifest(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager
            .resourceProviders()
            .checkinManifestWithResponse(
                "Microsoft.Contoso",
                new CheckinManifestParams().withEnvironment("Prod").withBaselineArmManifestLocation("EastUS2EUAP"),
                com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_GenerateManifest

```java
/** Samples for ResourceProvider GenerateManifest. */
public final class ResourceProviderGenerateManifestSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2020-11-20/examples/GenerateManifest.json
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
import com.azure.resourcemanager.providerhub.models.Regionality;
import com.azure.resourcemanager.providerhub.models.ResourceTypeEndpoint;
import com.azure.resourcemanager.providerhub.models.ResourceTypeRegistrationProperties;
import com.azure.resourcemanager.providerhub.models.RoutingType;
import com.azure.resourcemanager.providerhub.models.SwaggerSpecification;
import java.util.Arrays;

/** Samples for ResourceTypeRegistrations CreateOrUpdate. */
public final class ResourceTypeRegistrationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2020-11-20/examples/ResourceTypeRegistrations_CreateOrUpdate.json
     */
    /**
     * Sample code: ResourceTypeRegistrations_CreateOrUpdate.
     *
     * @param manager Entry point to ProviderHubManager.
     */
    public static void resourceTypeRegistrationsCreateOrUpdate(
        com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager
            .resourceTypeRegistrations()
            .define("employees")
            .withExistingProviderRegistration("Microsoft.Contoso")
            .withProperties(
                new ResourceTypeRegistrationProperties()
                    .withRoutingType(RoutingType.DEFAULT)
                    .withRegionality(Regionality.REGIONAL)
                    .withEndpoints(
                        Arrays
                            .asList(
                                new ResourceTypeEndpoint()
                                    .withApiVersions(Arrays.asList("2020-06-01-preview"))
                                    .withLocations(Arrays.asList("West US", "East US", "North Europe"))
                                    .withRequiredFeatures(Arrays.asList("<feature flag>"))))
                    .withSwaggerSpecifications(
                        Arrays
                            .asList(
                                new SwaggerSpecification()
                                    .withApiVersions(Arrays.asList("2020-06-01-preview"))
                                    .withSwaggerSpecFolderUri(
                                        "https://github.com/Azure/azure-rest-api-specs/blob/feature/azure/contoso/specification/contoso/resource-manager/Microsoft.SampleRP/"))))
            .create();
    }
}
```

### ResourceTypeRegistrations_Delete

```java
/** Samples for ResourceTypeRegistrations Delete. */
public final class ResourceTypeRegistrationsDeleteSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2020-11-20/examples/ResourceTypeRegistrations_Delete.json
     */
    /**
     * Sample code: ResourceTypeRegistrations_Delete.
     *
     * @param manager Entry point to ProviderHubManager.
     */
    public static void resourceTypeRegistrationsDelete(
        com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager
            .resourceTypeRegistrations()
            .deleteByResourceGroupWithResponse(
                "Microsoft.Contoso", "testResourceType", com.azure.core.util.Context.NONE);
    }
}
```

### ResourceTypeRegistrations_Get

```java
/** Samples for ResourceTypeRegistrations Get. */
public final class ResourceTypeRegistrationsGetSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2020-11-20/examples/ResourceTypeRegistrations_Get.json
     */
    /**
     * Sample code: ResourceTypeRegistrations_Get.
     *
     * @param manager Entry point to ProviderHubManager.
     */
    public static void resourceTypeRegistrationsGet(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager
            .resourceTypeRegistrations()
            .getWithResponse("Microsoft.Contoso", "employees", com.azure.core.util.Context.NONE);
    }
}
```

### ResourceTypeRegistrations_ListByProviderRegistration

```java
/** Samples for ResourceTypeRegistrations ListByProviderRegistration. */
public final class ResourceTypeRegistrationsListByProviderRegistrationSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2020-11-20/examples/ResourceTypeRegistrations_ListByProviderRegistration.json
     */
    /**
     * Sample code: ResourceTypeRegistrations_ListByProviderRegistration.
     *
     * @param manager Entry point to ProviderHubManager.
     */
    public static void resourceTypeRegistrationsListByProviderRegistration(
        com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager
            .resourceTypeRegistrations()
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

/** Samples for Skus CreateOrUpdate. */
public final class SkusCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2020-11-20/examples/Skus_CreateOrUpdate.json
     */
    /**
     * Sample code: Skus_CreateOrUpdate.
     *
     * @param manager Entry point to ProviderHubManager.
     */
    public static void skusCreateOrUpdate(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager
            .skus()
            .define("testSku")
            .withExistingResourcetypeRegistration("Microsoft.Contoso", "testResourceType")
            .withProperties(
                new SkuResourceProperties()
                    .withSkuSettings(
                        Arrays
                            .asList(
                                new SkuSetting().withName("freeSku").withTier("Tier1").withKind("Standard"),
                                new SkuSetting()
                                    .withName("premiumSku")
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

/** Samples for Skus CreateOrUpdateNestedResourceTypeFirst. */
public final class SkusCreateOrUpdateNestedResourceTypeFirstSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2020-11-20/examples/Skus_CreateOrUpdateNestedResourceTypeFirst.json
     */
    /**
     * Sample code: Skus_CreateOrUpdateNestedResourceTypeFirst.
     *
     * @param manager Entry point to ProviderHubManager.
     */
    public static void skusCreateOrUpdateNestedResourceTypeFirst(
        com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager
            .skus()
            .createOrUpdateNestedResourceTypeFirstWithResponse(
                "Microsoft.Contoso",
                "testResourceType",
                "nestedResourceTypeFirst",
                "testSku",
                new SkuResourceInner()
                    .withProperties(
                        new SkuResourceProperties()
                            .withSkuSettings(
                                Arrays
                                    .asList(
                                        new SkuSetting().withName("freeSku").withTier("Tier1").withKind("Standard"),
                                        new SkuSetting()
                                            .withName("premiumSku")
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

/** Samples for Skus CreateOrUpdateNestedResourceTypeSecond. */
public final class SkusCreateOrUpdateNestedResourceTypeSecondSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2020-11-20/examples/Skus_CreateOrUpdateNestedResourceTypeSecond.json
     */
    /**
     * Sample code: Skus_CreateOrUpdateNestedResourceTypeSecond.
     *
     * @param manager Entry point to ProviderHubManager.
     */
    public static void skusCreateOrUpdateNestedResourceTypeSecond(
        com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager
            .skus()
            .createOrUpdateNestedResourceTypeSecondWithResponse(
                "Microsoft.Contoso",
                "testResourceType",
                "nestedResourceTypeFirst",
                "nestedResourceTypeSecond",
                "testSku",
                new SkuResourceInner()
                    .withProperties(
                        new SkuResourceProperties()
                            .withSkuSettings(
                                Arrays
                                    .asList(
                                        new SkuSetting().withName("freeSku").withTier("Tier1").withKind("Standard"),
                                        new SkuSetting()
                                            .withName("premiumSku")
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

/** Samples for Skus CreateOrUpdateNestedResourceTypeThird. */
public final class SkusCreateOrUpdateNestedResourceTypeThirdSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2020-11-20/examples/Skus_CreateOrUpdateNestedResourceTypeThird.json
     */
    /**
     * Sample code: Skus_CreateOrUpdateNestedResourceTypeThird.
     *
     * @param manager Entry point to ProviderHubManager.
     */
    public static void skusCreateOrUpdateNestedResourceTypeThird(
        com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager
            .skus()
            .createOrUpdateNestedResourceTypeThirdWithResponse(
                "Microsoft.Contoso",
                "testResourceType",
                "nestedResourceTypeFirst",
                "nestedResourceTypeSecond",
                "nestedResourceTypeThird",
                "testSku",
                new SkuResourceInner()
                    .withProperties(
                        new SkuResourceProperties()
                            .withSkuSettings(
                                Arrays
                                    .asList(
                                        new SkuSetting().withName("freeSku").withTier("Tier1").withKind("Standard"),
                                        new SkuSetting()
                                            .withName("premiumSku")
                                            .withTier("Tier2")
                                            .withKind("Premium")
                                            .withCosts(Arrays.asList(new SkuCost().withMeterId("xxx")))))),
                com.azure.core.util.Context.NONE);
    }
}
```

### Skus_Delete

```java
/** Samples for Skus Delete. */
public final class SkusDeleteSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2020-11-20/examples/Skus_Delete.json
     */
    /**
     * Sample code: Skus_Delete.
     *
     * @param manager Entry point to ProviderHubManager.
     */
    public static void skusDelete(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager
            .skus()
            .deleteWithResponse("Microsoft.Contoso", "testResourceType", "testSku", com.azure.core.util.Context.NONE);
    }
}
```

### Skus_DeleteNestedResourceTypeFirst

```java
/** Samples for Skus DeleteNestedResourceTypeFirst. */
public final class SkusDeleteNestedResourceTypeFirstSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2020-11-20/examples/Skus_DeleteNestedResourceTypeFirst.json
     */
    /**
     * Sample code: Skus_DeleteNestedResourceTypeFirst.
     *
     * @param manager Entry point to ProviderHubManager.
     */
    public static void skusDeleteNestedResourceTypeFirst(
        com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager
            .skus()
            .deleteNestedResourceTypeFirstWithResponse(
                "Microsoft.Contoso",
                "testResourceType",
                "nestedResourceTypeFirst",
                "testSku",
                com.azure.core.util.Context.NONE);
    }
}
```

### Skus_DeleteNestedResourceTypeSecond

```java
/** Samples for Skus DeleteNestedResourceTypeSecond. */
public final class SkusDeleteNestedResourceTypeSecondSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2020-11-20/examples/Skus_DeleteNestedResourceTypeSecond.json
     */
    /**
     * Sample code: Skus_DeleteNestedResourceTypeSecond.
     *
     * @param manager Entry point to ProviderHubManager.
     */
    public static void skusDeleteNestedResourceTypeSecond(
        com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager
            .skus()
            .deleteNestedResourceTypeSecondWithResponse(
                "Microsoft.Contoso",
                "testResourceType",
                "nestedResourceTypeFirst",
                "nestedResourceTypeSecond",
                "testSku",
                com.azure.core.util.Context.NONE);
    }
}
```

### Skus_DeleteNestedResourceTypeThird

```java
/** Samples for Skus DeleteNestedResourceTypeThird. */
public final class SkusDeleteNestedResourceTypeThirdSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2020-11-20/examples/Skus_DeleteNestedResourceTypeThird.json
     */
    /**
     * Sample code: Skus_DeleteNestedResourceTypeThird.
     *
     * @param manager Entry point to ProviderHubManager.
     */
    public static void skusDeleteNestedResourceTypeThird(
        com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager
            .skus()
            .deleteNestedResourceTypeThirdWithResponse(
                "Microsoft.Contoso",
                "testResourceType",
                "nestedResourceTypeFirst",
                "nestedResourceTypeSecond",
                "nestedResourceTypeThird",
                "testSku",
                com.azure.core.util.Context.NONE);
    }
}
```

### Skus_Get

```java
/** Samples for Skus Get. */
public final class SkusGetSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2020-11-20/examples/Skus_Get.json
     */
    /**
     * Sample code: Skus_Get.
     *
     * @param manager Entry point to ProviderHubManager.
     */
    public static void skusGet(com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager
            .skus()
            .getWithResponse("Microsoft.Contoso", "testResourceType", "testSku", com.azure.core.util.Context.NONE);
    }
}
```

### Skus_GetNestedResourceTypeFirst

```java
/** Samples for Skus GetNestedResourceTypeFirst. */
public final class SkusGetNestedResourceTypeFirstSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2020-11-20/examples/Skus_GetNestedResourceTypeFirst.json
     */
    /**
     * Sample code: Skus_GetNestedResourceTypeFirst.
     *
     * @param manager Entry point to ProviderHubManager.
     */
    public static void skusGetNestedResourceTypeFirst(
        com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager
            .skus()
            .getNestedResourceTypeFirstWithResponse(
                "Microsoft.Contoso",
                "testResourceType",
                "nestedResourceTypeFirst",
                "testSku",
                com.azure.core.util.Context.NONE);
    }
}
```

### Skus_GetNestedResourceTypeSecond

```java
/** Samples for Skus GetNestedResourceTypeSecond. */
public final class SkusGetNestedResourceTypeSecondSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2020-11-20/examples/Skus_GetNestedResourceTypeSecond.json
     */
    /**
     * Sample code: Skus_GetNestedResourceTypeSecond.
     *
     * @param manager Entry point to ProviderHubManager.
     */
    public static void skusGetNestedResourceTypeSecond(
        com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager
            .skus()
            .getNestedResourceTypeSecondWithResponse(
                "Microsoft.Contoso",
                "testResourceType",
                "nestedResourceTypeFirst",
                "nestedResourceTypeSecond",
                "testSku",
                com.azure.core.util.Context.NONE);
    }
}
```

### Skus_GetNestedResourceTypeThird

```java
/** Samples for Skus GetNestedResourceTypeThird. */
public final class SkusGetNestedResourceTypeThirdSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2020-11-20/examples/Skus_GetNestedResourceTypeThird.json
     */
    /**
     * Sample code: Skus_GetNestedResourceTypeThird.
     *
     * @param manager Entry point to ProviderHubManager.
     */
    public static void skusGetNestedResourceTypeThird(
        com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager
            .skus()
            .getNestedResourceTypeThirdWithResponse(
                "Microsoft.Contoso",
                "testResourceType",
                "nestedResourceTypeFirst",
                "nestedResourceTypeSecond",
                "nestedResourceTypeThird",
                "testSku",
                com.azure.core.util.Context.NONE);
    }
}
```

### Skus_ListByResourceTypeRegistrations

```java
/** Samples for Skus ListByResourceTypeRegistrations. */
public final class SkusListByResourceTypeRegistrationsSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2020-11-20/examples/Skus_ListByResourceTypeRegistrations.json
     */
    /**
     * Sample code: Skus_ListByResourceTypeRegistrations.
     *
     * @param manager Entry point to ProviderHubManager.
     */
    public static void skusListByResourceTypeRegistrations(
        com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager
            .skus()
            .listByResourceTypeRegistrations("Microsoft.Contoso", "testResourceType", com.azure.core.util.Context.NONE);
    }
}
```

### Skus_ListByResourceTypeRegistrationsNestedResourceTypeFirst

```java
/** Samples for Skus ListByResourceTypeRegistrationsNestedResourceTypeFirst. */
public final class SkusListByResourceTypeRegistrationsNestedResourceTypeFirstSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2020-11-20/examples/Skus_ListByResourceTypeRegistrationsNestedResourceTypeFirst.json
     */
    /**
     * Sample code: Skus_ListByResourceTypeRegistrationsNestedResourceTypeFirst.
     *
     * @param manager Entry point to ProviderHubManager.
     */
    public static void skusListByResourceTypeRegistrationsNestedResourceTypeFirst(
        com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager
            .skus()
            .listByResourceTypeRegistrationsNestedResourceTypeFirst(
                "Microsoft.Contoso", "testResourceType", "nestedResourceTypeFirst", com.azure.core.util.Context.NONE);
    }
}
```

### Skus_ListByResourceTypeRegistrationsNestedResourceTypeSecond

```java
/** Samples for Skus ListByResourceTypeRegistrationsNestedResourceTypeSecond. */
public final class SkusListByResourceTypeRegistrationsNestedResourceTypeSecondSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2020-11-20/examples/Skus_ListByResourceTypeRegistrationsNestedResourceTypeSecond.json
     */
    /**
     * Sample code: Skus_ListByResourceTypeRegistrationsNestedResourceTypeSecond.
     *
     * @param manager Entry point to ProviderHubManager.
     */
    public static void skusListByResourceTypeRegistrationsNestedResourceTypeSecond(
        com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager
            .skus()
            .listByResourceTypeRegistrationsNestedResourceTypeSecond(
                "Microsoft.Contoso",
                "testResourceType",
                "nestedResourceTypeFirst",
                "nestedResourceTypeSecond",
                com.azure.core.util.Context.NONE);
    }
}
```

### Skus_ListByResourceTypeRegistrationsNestedResourceTypeThird

```java
/** Samples for Skus ListByResourceTypeRegistrationsNestedResourceTypeThird. */
public final class SkusListByResourceTypeRegistrationsNestedResourceTypeThirdSamples {
    /*
     * x-ms-original-file: specification/providerhub/resource-manager/Microsoft.ProviderHub/stable/2020-11-20/examples/Skus_ListByResourceTypeRegistrationsNestedResourceTypeThird.json
     */
    /**
     * Sample code: Skus_ListByResourceTypeRegistrationsNestedResourceTypeThird.
     *
     * @param manager Entry point to ProviderHubManager.
     */
    public static void skusListByResourceTypeRegistrationsNestedResourceTypeThird(
        com.azure.resourcemanager.providerhub.ProviderHubManager manager) {
        manager
            .skus()
            .listByResourceTypeRegistrationsNestedResourceTypeThird(
                "Microsoft.Contoso",
                "testResourceType",
                "nestedResourceTypeFirst",
                "nestedResourceTypeSecond",
                "nestedResourceTypeThird",
                com.azure.core.util.Context.NONE);
    }
}
```

