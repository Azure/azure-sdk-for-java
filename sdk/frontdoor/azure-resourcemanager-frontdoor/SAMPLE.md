# Code snippets and samples


## Endpoints

- [PurgeContent](#endpoints_purgecontent)

## Experiments

- [CreateOrUpdate](#experiments_createorupdate)
- [Delete](#experiments_delete)
- [Get](#experiments_get)
- [ListByProfile](#experiments_listbyprofile)
- [Update](#experiments_update)

## FrontDoorNameAvailability

- [Check](#frontdoornameavailability_check)

## FrontDoorNameAvailabilityWithSubscription

- [Check](#frontdoornameavailabilitywithsubscription_check)

## FrontDoors

- [CreateOrUpdate](#frontdoors_createorupdate)
- [Delete](#frontdoors_delete)
- [GetByResourceGroup](#frontdoors_getbyresourcegroup)
- [List](#frontdoors_list)
- [ListByResourceGroup](#frontdoors_listbyresourcegroup)
- [ValidateCustomDomain](#frontdoors_validatecustomdomain)

## FrontendEndpoints

- [DisableHttps](#frontendendpoints_disablehttps)
- [EnableHttps](#frontendendpoints_enablehttps)
- [Get](#frontendendpoints_get)
- [ListByFrontDoor](#frontendendpoints_listbyfrontdoor)

## ManagedRuleSets

- [List](#managedrulesets_list)

## NetworkExperimentProfiles

- [CreateOrUpdate](#networkexperimentprofiles_createorupdate)
- [Delete](#networkexperimentprofiles_delete)
- [GetByResourceGroup](#networkexperimentprofiles_getbyresourcegroup)
- [List](#networkexperimentprofiles_list)
- [ListByResourceGroup](#networkexperimentprofiles_listbyresourcegroup)
- [Update](#networkexperimentprofiles_update)

## Policies

- [CreateOrUpdate](#policies_createorupdate)
- [Delete](#policies_delete)
- [GetByResourceGroup](#policies_getbyresourcegroup)
- [List](#policies_list)
- [ListByResourceGroup](#policies_listbyresourcegroup)
- [Update](#policies_update)

## PreconfiguredEndpoints

- [List](#preconfiguredendpoints_list)

## Reports

- [GetLatencyScorecards](#reports_getlatencyscorecards)
- [GetTimeseries](#reports_gettimeseries)

## RulesEngines

- [CreateOrUpdate](#rulesengines_createorupdate)
- [Delete](#rulesengines_delete)
- [Get](#rulesengines_get)
- [ListByFrontDoor](#rulesengines_listbyfrontdoor)
### Endpoints_PurgeContent

```java
import com.azure.resourcemanager.frontdoor.models.PurgeParameters;
import java.util.Arrays;

/**
 * Samples for Endpoints PurgeContent.
 */
public final class EndpointsPurgeContentSamples {
    /*
     * x-ms-original-file: specification/frontdoor/resource-manager/Microsoft.Network/stable/2021-06-01/examples/FrontdoorPurgeContent.json
     */
    /**
     * Sample code: Purge content from Front Door.
     * 
     * @param manager Entry point to FrontDoorManager.
     */
    public static void purgeContentFromFrontDoor(com.azure.resourcemanager.frontdoor.FrontDoorManager manager) {
        manager.endpoints()
            .purgeContent("rg1", "frontDoor1",
                new PurgeParameters().withContentPaths(Arrays.asList("/pictures.aspx", "/pictures/*")),
                com.azure.core.util.Context.NONE);
    }
}
```

### Experiments_CreateOrUpdate

```java
import com.azure.resourcemanager.frontdoor.models.Endpoint;
import com.azure.resourcemanager.frontdoor.models.State;

/**
 * Samples for Experiments CreateOrUpdate.
 */
public final class ExperimentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/frontdoor/resource-manager/Microsoft.Network/stable/2019-11-01/examples/NetworkExperimentCreateExperiment.json
     */
    /**
     * Sample code: Creates an Experiment.
     * 
     * @param manager Entry point to FrontDoorManager.
     */
    public static void createsAnExperiment(com.azure.resourcemanager.frontdoor.FrontDoorManager manager) {
        manager.experiments()
            .define("MyExperiment")
            .withRegion((String) null)
            .withExistingNetworkExperimentProfile("MyResourceGroup", "MyProfile")
            .withDescription("this is my first experiment!")
            .withEndpointA(new Endpoint().withName("endpoint A").withEndpoint("endpointA.net"))
            .withEndpointB(new Endpoint().withName("endpoint B").withEndpoint("endpointB.net"))
            .withEnabledState(State.ENABLED)
            .create();
    }
}
```

### Experiments_Delete

```java
/**
 * Samples for Experiments Delete.
 */
public final class ExperimentsDeleteSamples {
    /*
     * x-ms-original-file: specification/frontdoor/resource-manager/Microsoft.Network/stable/2019-11-01/examples/NetworkExperimentDeleteExperiment.json
     */
    /**
     * Sample code: Deletes an Experiment.
     * 
     * @param manager Entry point to FrontDoorManager.
     */
    public static void deletesAnExperiment(com.azure.resourcemanager.frontdoor.FrontDoorManager manager) {
        manager.experiments().delete("MyResourceGroup", "MyProfile", "MyExperiment", com.azure.core.util.Context.NONE);
    }
}
```

### Experiments_Get

```java
/**
 * Samples for Experiments Get.
 */
public final class ExperimentsGetSamples {
    /*
     * x-ms-original-file: specification/frontdoor/resource-manager/Microsoft.Network/stable/2019-11-01/examples/NetworkExperimentGetExperiment.json
     */
    /**
     * Sample code: Gets an Experiment by ExperimentName.
     * 
     * @param manager Entry point to FrontDoorManager.
     */
    public static void getsAnExperimentByExperimentName(com.azure.resourcemanager.frontdoor.FrontDoorManager manager) {
        manager.experiments()
            .getWithResponse("MyResourceGroup", "MyProfile", "MyExperiment", com.azure.core.util.Context.NONE);
    }
}
```

### Experiments_ListByProfile

```java
/**
 * Samples for Experiments ListByProfile.
 */
public final class ExperimentsListByProfileSamples {
    /*
     * x-ms-original-file: specification/frontdoor/resource-manager/Microsoft.Network/stable/2019-11-01/examples/NetworkExperimentListExperiments.json
     */
    /**
     * Sample code: Gets a list of Experiments.
     * 
     * @param manager Entry point to FrontDoorManager.
     */
    public static void getsAListOfExperiments(com.azure.resourcemanager.frontdoor.FrontDoorManager manager) {
        manager.experiments().listByProfile("MyResourceGroup", "MyProfile", com.azure.core.util.Context.NONE);
    }
}
```

### Experiments_Update

```java
import com.azure.resourcemanager.frontdoor.models.Experiment;
import com.azure.resourcemanager.frontdoor.models.State;

/**
 * Samples for Experiments Update.
 */
public final class ExperimentsUpdateSamples {
    /*
     * x-ms-original-file: specification/frontdoor/resource-manager/Microsoft.Network/stable/2019-11-01/examples/NetworkExperimentUpdateExperiment.json
     */
    /**
     * Sample code: Updates an Experiment.
     * 
     * @param manager Entry point to FrontDoorManager.
     */
    public static void updatesAnExperiment(com.azure.resourcemanager.frontdoor.FrontDoorManager manager) {
        Experiment resource = manager.experiments()
            .getWithResponse("MyResourceGroup", "MyProfile", "MyExperiment", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withDescription("string").withEnabledState(State.ENABLED).apply();
    }
}
```

### FrontDoorNameAvailability_Check

```java
import com.azure.resourcemanager.frontdoor.models.CheckNameAvailabilityInput;
import com.azure.resourcemanager.frontdoor.models.ResourceType;

/**
 * Samples for FrontDoorNameAvailability Check.
 */
public final class FrontDoorNameAvailabilityCheckSamples {
    /*
     * x-ms-original-file: specification/frontdoor/resource-manager/Microsoft.Network/stable/2021-06-01/examples/CheckFrontdoorNameAvailability.json
     */
    /**
     * Sample code: CheckNameAvailability.
     * 
     * @param manager Entry point to FrontDoorManager.
     */
    public static void checkNameAvailability(com.azure.resourcemanager.frontdoor.FrontDoorManager manager) {
        manager.frontDoorNameAvailabilities()
            .checkWithResponse(new CheckNameAvailabilityInput().withName("sampleName")
                .withType(ResourceType.MICROSOFT_NETWORK_FRONT_DOORS), com.azure.core.util.Context.NONE);
    }
}
```

### FrontDoorNameAvailabilityWithSubscription_Check

```java
import com.azure.resourcemanager.frontdoor.models.CheckNameAvailabilityInput;
import com.azure.resourcemanager.frontdoor.models.ResourceType;

/**
 * Samples for FrontDoorNameAvailabilityWithSubscription Check.
 */
public final class FrontDoorNameAvailabilityWithSubscriptionCheckSamples {
    /*
     * x-ms-original-file: specification/frontdoor/resource-manager/Microsoft.Network/stable/2021-06-01/examples/CheckFrontdoorNameAvailabilityWithSubscription.json
     */
    /**
     * Sample code: CheckNameAvailabilityWithSubscription.
     * 
     * @param manager Entry point to FrontDoorManager.
     */
    public static void
        checkNameAvailabilityWithSubscription(com.azure.resourcemanager.frontdoor.FrontDoorManager manager) {
        manager.frontDoorNameAvailabilityWithSubscriptions()
            .checkWithResponse(
                new CheckNameAvailabilityInput().withName("sampleName")
                    .withType(ResourceType.MICROSOFT_NETWORK_FRONT_DOORS_FRONTEND_ENDPOINTS),
                com.azure.core.util.Context.NONE);
    }
}
```

### FrontDoors_CreateOrUpdate

```java
import com.azure.core.management.SubResource;
import com.azure.resourcemanager.frontdoor.fluent.models.FrontendEndpointInner;
import com.azure.resourcemanager.frontdoor.models.Backend;
import com.azure.resourcemanager.frontdoor.models.BackendPool;
import com.azure.resourcemanager.frontdoor.models.BackendPoolsSettings;
import com.azure.resourcemanager.frontdoor.models.EnforceCertificateNameCheckEnabledState;
import com.azure.resourcemanager.frontdoor.models.ForwardingConfiguration;
import com.azure.resourcemanager.frontdoor.models.FrontDoorEnabledState;
import com.azure.resourcemanager.frontdoor.models.FrontDoorHealthProbeMethod;
import com.azure.resourcemanager.frontdoor.models.FrontDoorProtocol;
import com.azure.resourcemanager.frontdoor.models.FrontendEndpointUpdateParametersWebApplicationFirewallPolicyLink;
import com.azure.resourcemanager.frontdoor.models.HealthProbeEnabled;
import com.azure.resourcemanager.frontdoor.models.HealthProbeSettingsModel;
import com.azure.resourcemanager.frontdoor.models.LoadBalancingSettingsModel;
import com.azure.resourcemanager.frontdoor.models.RoutingRule;
import com.azure.resourcemanager.frontdoor.models.RoutingRuleEnabledState;
import com.azure.resourcemanager.frontdoor.models.RoutingRuleUpdateParametersWebApplicationFirewallPolicyLink;
import com.azure.resourcemanager.frontdoor.models.SessionAffinityEnabledState;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for FrontDoors CreateOrUpdate.
 */
public final class FrontDoorsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/frontdoor/resource-manager/Microsoft.Network/stable/2021-06-01/examples/FrontdoorCreate.json
     */
    /**
     * Sample code: Create or update specific Front Door.
     * 
     * @param manager Entry point to FrontDoorManager.
     */
    public static void createOrUpdateSpecificFrontDoor(com.azure.resourcemanager.frontdoor.FrontDoorManager manager) {
        manager.frontDoors()
            .define("frontDoor1")
            .withRegion("westus")
            .withExistingResourceGroup("rg1")
            .withTags(mapOf("tag1", "value1", "tag2", "value2"))
            .withRoutingRules(Arrays.asList(new RoutingRule().withName("routingRule1")
                .withFrontendEndpoints(Arrays.asList(new SubResource().withId(
                    "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.Network/frontDoors/frontDoor1/frontendEndpoints/frontendEndpoint1"),
                    new SubResource().withId(
                        "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.Network/frontDoors/frontDoor1/frontendEndpoints/default")))
                .withAcceptedProtocols(Arrays.asList(FrontDoorProtocol.HTTP))
                .withPatternsToMatch(Arrays.asList("/*"))
                .withEnabledState(RoutingRuleEnabledState.ENABLED)
                .withRouteConfiguration(new ForwardingConfiguration().withBackendPool(new SubResource().withId(
                    "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.Network/frontDoors/frontDoor1/backendPools/backendPool1")))
                .withRulesEngine(new SubResource().withId(
                    "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.Network/frontDoors/frontDoor1/rulesEngines/rulesEngine1"))
                .withWebApplicationFirewallPolicyLink(
                    new RoutingRuleUpdateParametersWebApplicationFirewallPolicyLink().withId(
                        "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.Network/frontDoorWebApplicationFirewallPolicies/policy1"))))
            .withLoadBalancingSettings(Arrays.asList(new LoadBalancingSettingsModel().withName("loadBalancingSettings1")
                .withSampleSize(4)
                .withSuccessfulSamplesRequired(2)))
            .withHealthProbeSettings(Arrays.asList(new HealthProbeSettingsModel().withName("healthProbeSettings1")
                .withPath("/")
                .withProtocol(FrontDoorProtocol.HTTP)
                .withIntervalInSeconds(120)
                .withHealthProbeMethod(FrontDoorHealthProbeMethod.HEAD)
                .withEnabledState(HealthProbeEnabled.ENABLED)))
            .withBackendPools(Arrays.asList(new BackendPool().withName("backendPool1")
                .withBackends(Arrays.asList(
                    new Backend().withAddress("w3.contoso.com")
                        .withHttpPort(80)
                        .withHttpsPort(443)
                        .withPriority(2)
                        .withWeight(1),
                    new Backend().withAddress("contoso.com.website-us-west-2.othercloud.net")
                        .withPrivateLinkResourceId(
                            "/subscriptions/subid/resourcegroups/rg1/providers/Microsoft.Network/privateLinkServices/pls1")
                        .withPrivateLinkLocation("eastus")
                        .withPrivateLinkApprovalMessage("Please approve the connection request for this Private Link")
                        .withHttpPort(80)
                        .withHttpsPort(443)
                        .withPriority(1)
                        .withWeight(2),
                    new Backend().withAddress("10.0.1.5")
                        .withPrivateLinkAlias(
                            "APPSERVER.d84e61f0-0870-4d24-9746-7438fa0019d1.westus2.azure.privatelinkservice")
                        .withPrivateLinkApprovalMessage("Please approve this request to connect to the Private Link")
                        .withHttpPort(80)
                        .withHttpsPort(443)
                        .withPriority(1)
                        .withWeight(1)))
                .withLoadBalancingSettings(new SubResource().withId(
                    "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.Network/frontDoors/frontDoor1/loadBalancingSettings/loadBalancingSettings1"))
                .withHealthProbeSettings(new SubResource().withId(
                    "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.Network/frontDoors/frontDoor1/healthProbeSettings/healthProbeSettings1"))))
            .withFrontendEndpoints(Arrays.asList(new FrontendEndpointInner().withName("frontendEndpoint1")
                .withHostname("www.contoso.com")
                .withSessionAffinityEnabledState(SessionAffinityEnabledState.ENABLED)
                .withSessionAffinityTtlSeconds(60)
                .withWebApplicationFirewallPolicyLink(
                    new FrontendEndpointUpdateParametersWebApplicationFirewallPolicyLink().withId(
                        "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.Network/frontDoorWebApplicationFirewallPolicies/policy1")),
                new FrontendEndpointInner().withName("default").withHostname("frontDoor1.azurefd.net")))
            .withBackendPoolsSettings(new BackendPoolsSettings()
                .withEnforceCertificateNameCheck(EnforceCertificateNameCheckEnabledState.ENABLED)
                .withSendRecvTimeoutSeconds(60))
            .withEnabledState(FrontDoorEnabledState.ENABLED)
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

### FrontDoors_Delete

```java
/**
 * Samples for FrontDoors Delete.
 */
public final class FrontDoorsDeleteSamples {
    /*
     * x-ms-original-file: specification/frontdoor/resource-manager/Microsoft.Network/stable/2021-06-01/examples/FrontdoorDelete.json
     */
    /**
     * Sample code: Delete Front Door.
     * 
     * @param manager Entry point to FrontDoorManager.
     */
    public static void deleteFrontDoor(com.azure.resourcemanager.frontdoor.FrontDoorManager manager) {
        manager.frontDoors().delete("rg1", "frontDoor1", com.azure.core.util.Context.NONE);
    }
}
```

### FrontDoors_GetByResourceGroup

```java
/**
 * Samples for FrontDoors GetByResourceGroup.
 */
public final class FrontDoorsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/frontdoor/resource-manager/Microsoft.Network/stable/2021-06-01/examples/FrontdoorGet.json
     */
    /**
     * Sample code: Get Front Door.
     * 
     * @param manager Entry point to FrontDoorManager.
     */
    public static void getFrontDoor(com.azure.resourcemanager.frontdoor.FrontDoorManager manager) {
        manager.frontDoors().getByResourceGroupWithResponse("rg1", "frontDoor1", com.azure.core.util.Context.NONE);
    }
}
```

### FrontDoors_List

```java
/**
 * Samples for FrontDoors List.
 */
public final class FrontDoorsListSamples {
    /*
     * x-ms-original-file: specification/frontdoor/resource-manager/Microsoft.Network/stable/2021-06-01/examples/FrontdoorListAll.json
     */
    /**
     * Sample code: List all Front Doors.
     * 
     * @param manager Entry point to FrontDoorManager.
     */
    public static void listAllFrontDoors(com.azure.resourcemanager.frontdoor.FrontDoorManager manager) {
        manager.frontDoors().list(com.azure.core.util.Context.NONE);
    }
}
```

### FrontDoors_ListByResourceGroup

```java
/**
 * Samples for FrontDoors ListByResourceGroup.
 */
public final class FrontDoorsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/frontdoor/resource-manager/Microsoft.Network/stable/2021-06-01/examples/FrontdoorList.json
     */
    /**
     * Sample code: List Front Doors in a Resource Group.
     * 
     * @param manager Entry point to FrontDoorManager.
     */
    public static void listFrontDoorsInAResourceGroup(com.azure.resourcemanager.frontdoor.FrontDoorManager manager) {
        manager.frontDoors().listByResourceGroup("rg1", com.azure.core.util.Context.NONE);
    }
}
```

### FrontDoors_ValidateCustomDomain

```java
import com.azure.resourcemanager.frontdoor.models.ValidateCustomDomainInput;

/**
 * Samples for FrontDoors ValidateCustomDomain.
 */
public final class FrontDoorsValidateCustomDomainSamples {
    /*
     * x-ms-original-file: specification/frontdoor/resource-manager/Microsoft.Network/stable/2021-06-01/examples/FrontdoorValidateCustomDomain.json
     */
    /**
     * Sample code: FrontDoor_ValidateCustomDomain.
     * 
     * @param manager Entry point to FrontDoorManager.
     */
    public static void frontDoorValidateCustomDomain(com.azure.resourcemanager.frontdoor.FrontDoorManager manager) {
        manager.frontDoors()
            .validateCustomDomainWithResponse("rg1", "frontDoor1",
                new ValidateCustomDomainInput().withHostname("www.someDomain.com"), com.azure.core.util.Context.NONE);
    }
}
```

### FrontendEndpoints_DisableHttps

```java
/**
 * Samples for FrontendEndpoints DisableHttps.
 */
public final class FrontendEndpointsDisableHttpsSamples {
    /*
     * x-ms-original-file: specification/frontdoor/resource-manager/Microsoft.Network/stable/2021-06-01/examples/FrontdoorDisableHttps.json
     */
    /**
     * Sample code: FrontendEndpoints_DisableHttps.
     * 
     * @param manager Entry point to FrontDoorManager.
     */
    public static void frontendEndpointsDisableHttps(com.azure.resourcemanager.frontdoor.FrontDoorManager manager) {
        manager.frontendEndpoints()
            .disableHttps("rg1", "frontDoor1", "frontendEndpoint1", com.azure.core.util.Context.NONE);
    }
}
```

### FrontendEndpoints_EnableHttps

```java
import com.azure.resourcemanager.frontdoor.models.CustomHttpsConfiguration;
import com.azure.resourcemanager.frontdoor.models.FrontDoorCertificateSource;
import com.azure.resourcemanager.frontdoor.models.FrontDoorTlsProtocolType;
import com.azure.resourcemanager.frontdoor.models.KeyVaultCertificateSourceParametersVault;
import com.azure.resourcemanager.frontdoor.models.MinimumTlsVersion;

/**
 * Samples for FrontendEndpoints EnableHttps.
 */
public final class FrontendEndpointsEnableHttpsSamples {
    /*
     * x-ms-original-file: specification/frontdoor/resource-manager/Microsoft.Network/stable/2021-06-01/examples/FrontdoorEnableHttps.json
     */
    /**
     * Sample code: FrontendEndpoints_EnableHttps.
     * 
     * @param manager Entry point to FrontDoorManager.
     */
    public static void frontendEndpointsEnableHttps(com.azure.resourcemanager.frontdoor.FrontDoorManager manager) {
        manager.frontendEndpoints()
            .enableHttps("rg1", "frontDoor1", "frontendEndpoint1",
                new CustomHttpsConfiguration().withCertificateSource(FrontDoorCertificateSource.AZURE_KEY_VAULT)
                    .withProtocolType(FrontDoorTlsProtocolType.SERVER_NAME_INDICATION)
                    .withMinimumTlsVersion(MinimumTlsVersion.ONE_ZERO)
                    .withVault(new KeyVaultCertificateSourceParametersVault()
                        .withId("/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.KeyVault/vaults/vault1"))
                    .withSecretName("fakeTokenPlaceholder")
                    .withSecretVersion("fakeTokenPlaceholder"),
                com.azure.core.util.Context.NONE);
    }
}
```

### FrontendEndpoints_Get

```java
/**
 * Samples for FrontendEndpoints Get.
 */
public final class FrontendEndpointsGetSamples {
    /*
     * x-ms-original-file: specification/frontdoor/resource-manager/Microsoft.Network/stable/2021-06-01/examples/FrontdoorFrontendEndpointGet.json
     */
    /**
     * Sample code: Get Frontend Endpoint.
     * 
     * @param manager Entry point to FrontDoorManager.
     */
    public static void getFrontendEndpoint(com.azure.resourcemanager.frontdoor.FrontDoorManager manager) {
        manager.frontendEndpoints()
            .getWithResponse("rg1", "frontDoor1", "frontendEndpoint1", com.azure.core.util.Context.NONE);
    }
}
```

### FrontendEndpoints_ListByFrontDoor

```java
/**
 * Samples for FrontendEndpoints ListByFrontDoor.
 */
public final class FrontendEndpointsListByFrontDoorSamples {
    /*
     * x-ms-original-file: specification/frontdoor/resource-manager/Microsoft.Network/stable/2021-06-01/examples/FrontdoorFrontendEndpointList.json
     */
    /**
     * Sample code: List Frontend endpoints in a Front Door.
     * 
     * @param manager Entry point to FrontDoorManager.
     */
    public static void listFrontendEndpointsInAFrontDoor(com.azure.resourcemanager.frontdoor.FrontDoorManager manager) {
        manager.frontendEndpoints().listByFrontDoor("rg1", "frontDoor1", com.azure.core.util.Context.NONE);
    }
}
```

### ManagedRuleSets_List

```java
/**
 * Samples for ManagedRuleSets List.
 */
public final class ManagedRuleSetsListSamples {
    /*
     * x-ms-original-file: specification/frontdoor/resource-manager/Microsoft.Network/stable/2024-02-01/examples/WafListManagedRuleSets.json
     */
    /**
     * Sample code: List Policies ManagedRuleSets in a Resource Group.
     * 
     * @param manager Entry point to FrontDoorManager.
     */
    public static void
        listPoliciesManagedRuleSetsInAResourceGroup(com.azure.resourcemanager.frontdoor.FrontDoorManager manager) {
        manager.managedRuleSets().list(com.azure.core.util.Context.NONE);
    }
}
```

### NetworkExperimentProfiles_CreateOrUpdate

```java
import com.azure.resourcemanager.frontdoor.models.State;

/**
 * Samples for NetworkExperimentProfiles CreateOrUpdate.
 */
public final class NetworkExperimentProfilesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/frontdoor/resource-manager/Microsoft.Network/stable/2019-11-01/examples/NetworkExperimentCreateProfile.json
     */
    /**
     * Sample code: Creates an NetworkExperiment Profile in a Resource Group.
     * 
     * @param manager Entry point to FrontDoorManager.
     */
    public static void createsAnNetworkExperimentProfileInAResourceGroup(
        com.azure.resourcemanager.frontdoor.FrontDoorManager manager) {
        manager.networkExperimentProfiles()
            .define("MyProfile")
            .withRegion("WestUs")
            .withExistingResourceGroup("MyResourceGroup")
            .withEnabledState(State.ENABLED)
            .create();
    }
}
```

### NetworkExperimentProfiles_Delete

```java
/**
 * Samples for NetworkExperimentProfiles Delete.
 */
public final class NetworkExperimentProfilesDeleteSamples {
    /*
     * x-ms-original-file: specification/frontdoor/resource-manager/Microsoft.Network/stable/2019-11-01/examples/NetworkExperimentDeleteProfile.json
     */
    /**
     * Sample code: Deletes an NetworkExperiment Profile by ProfileName.
     * 
     * @param manager Entry point to FrontDoorManager.
     */
    public static void
        deletesAnNetworkExperimentProfileByProfileName(com.azure.resourcemanager.frontdoor.FrontDoorManager manager) {
        manager.networkExperimentProfiles().delete("MyResourceGroup", "MyProfile", com.azure.core.util.Context.NONE);
    }
}
```

### NetworkExperimentProfiles_GetByResourceGroup

```java
/**
 * Samples for NetworkExperimentProfiles GetByResourceGroup.
 */
public final class NetworkExperimentProfilesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/frontdoor/resource-manager/Microsoft.Network/stable/2019-11-01/examples/NetworkExperimentGetProfile.json
     */
    /**
     * Sample code: Gets an NetworkExperiment Profile by Profile Id.
     * 
     * @param manager Entry point to FrontDoorManager.
     */
    public static void
        getsAnNetworkExperimentProfileByProfileId(com.azure.resourcemanager.frontdoor.FrontDoorManager manager) {
        manager.networkExperimentProfiles()
            .getByResourceGroupWithResponse("MyResourceGroup", "MyProfile", com.azure.core.util.Context.NONE);
    }
}
```

### NetworkExperimentProfiles_List

```java
/**
 * Samples for NetworkExperimentProfiles List.
 */
public final class NetworkExperimentProfilesListSamples {
    /*
     * x-ms-original-file: specification/frontdoor/resource-manager/Microsoft.Network/stable/2019-11-01/examples/NetworkExperimentListProfiles.json
     */
    /**
     * Sample code: List NetworkExperiment Profiles in a Resource Group.
     * 
     * @param manager Entry point to FrontDoorManager.
     */
    public static void
        listNetworkExperimentProfilesInAResourceGroup(com.azure.resourcemanager.frontdoor.FrontDoorManager manager) {
        manager.networkExperimentProfiles().list(com.azure.core.util.Context.NONE);
    }
}
```

### NetworkExperimentProfiles_ListByResourceGroup

```java
/**
 * Samples for NetworkExperimentProfiles ListByResourceGroup.
 */
public final class NetworkExperimentProfilesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/frontdoor/resource-manager/Microsoft.Network/stable/2019-11-01/examples/NetworkExperimentListProfiles.json
     */
    /**
     * Sample code: List NetworkExperiment Profiles in a Resource Group.
     * 
     * @param manager Entry point to FrontDoorManager.
     */
    public static void
        listNetworkExperimentProfilesInAResourceGroup(com.azure.resourcemanager.frontdoor.FrontDoorManager manager) {
        manager.networkExperimentProfiles().listByResourceGroup("MyResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### NetworkExperimentProfiles_Update

```java
import com.azure.resourcemanager.frontdoor.models.Profile;
import com.azure.resourcemanager.frontdoor.models.State;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for NetworkExperimentProfiles Update.
 */
public final class NetworkExperimentProfilesUpdateSamples {
    /*
     * x-ms-original-file: specification/frontdoor/resource-manager/Microsoft.Network/stable/2019-11-01/examples/NetworkExperimentUpdateProfile.json
     */
    /**
     * Sample code: Updates an Experiment.
     * 
     * @param manager Entry point to FrontDoorManager.
     */
    public static void updatesAnExperiment(com.azure.resourcemanager.frontdoor.FrontDoorManager manager) {
        Profile resource = manager.networkExperimentProfiles()
            .getByResourceGroupWithResponse("MyResourceGroup", "MyProfile", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key1", "fakeTokenPlaceholder", "key2", "fakeTokenPlaceholder"))
            .withEnabledState(State.ENABLED)
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

### Policies_CreateOrUpdate

```java
import com.azure.resourcemanager.frontdoor.models.ActionType;
import com.azure.resourcemanager.frontdoor.models.CustomRule;
import com.azure.resourcemanager.frontdoor.models.CustomRuleList;
import com.azure.resourcemanager.frontdoor.models.ManagedRuleEnabledState;
import com.azure.resourcemanager.frontdoor.models.ManagedRuleExclusion;
import com.azure.resourcemanager.frontdoor.models.ManagedRuleExclusionMatchVariable;
import com.azure.resourcemanager.frontdoor.models.ManagedRuleExclusionSelectorMatchOperator;
import com.azure.resourcemanager.frontdoor.models.ManagedRuleGroupOverride;
import com.azure.resourcemanager.frontdoor.models.ManagedRuleOverride;
import com.azure.resourcemanager.frontdoor.models.ManagedRuleSet;
import com.azure.resourcemanager.frontdoor.models.ManagedRuleSetActionType;
import com.azure.resourcemanager.frontdoor.models.ManagedRuleSetList;
import com.azure.resourcemanager.frontdoor.models.MatchCondition;
import com.azure.resourcemanager.frontdoor.models.MatchVariable;
import com.azure.resourcemanager.frontdoor.models.Operator;
import com.azure.resourcemanager.frontdoor.models.PolicyEnabledState;
import com.azure.resourcemanager.frontdoor.models.PolicyMode;
import com.azure.resourcemanager.frontdoor.models.PolicyRequestBodyCheck;
import com.azure.resourcemanager.frontdoor.models.PolicySettings;
import com.azure.resourcemanager.frontdoor.models.RuleType;
import com.azure.resourcemanager.frontdoor.models.ScrubbingRuleEntryMatchOperator;
import com.azure.resourcemanager.frontdoor.models.ScrubbingRuleEntryMatchVariable;
import com.azure.resourcemanager.frontdoor.models.ScrubbingRuleEntryState;
import com.azure.resourcemanager.frontdoor.models.Sku;
import com.azure.resourcemanager.frontdoor.models.SkuName;
import com.azure.resourcemanager.frontdoor.models.TransformType;
import com.azure.resourcemanager.frontdoor.models.WebApplicationFirewallScrubbingRules;
import com.azure.resourcemanager.frontdoor.models.WebApplicationFirewallScrubbingState;
import java.util.Arrays;

/**
 * Samples for Policies CreateOrUpdate.
 */
public final class PoliciesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/frontdoor/resource-manager/Microsoft.Network/stable/2024-02-01/examples/WafPolicyCreateOrUpdate.json
     */
    /**
     * Sample code: Creates specific policy.
     * 
     * @param manager Entry point to FrontDoorManager.
     */
    public static void createsSpecificPolicy(com.azure.resourcemanager.frontdoor.FrontDoorManager manager) {
        manager.policies()
            .define("Policy1")
            .withRegion("WestUs")
            .withExistingResourceGroup("rg1")
            .withSku(new Sku().withName(SkuName.PREMIUM_AZURE_FRONT_DOOR))
            .withPolicySettings(new PolicySettings().withEnabledState(PolicyEnabledState.ENABLED)
                .withMode(PolicyMode.PREVENTION)
                .withRedirectUrl("http://www.bing.com")
                .withCustomBlockResponseStatusCode(429)
                .withCustomBlockResponseBody(
                    "PGh0bWw+CjxoZWFkZXI+PHRpdGxlPkhlbGxvPC90aXRsZT48L2hlYWRlcj4KPGJvZHk+CkhlbGxvIHdvcmxkCjwvYm9keT4KPC9odG1sPg==")
                .withRequestBodyCheck(PolicyRequestBodyCheck.DISABLED)
                .withJavascriptChallengeExpirationInMinutes(30)
                .withState(WebApplicationFirewallScrubbingState.ENABLED)
                .withScrubbingRules(Arrays.asList(new WebApplicationFirewallScrubbingRules()
                    .withMatchVariable(ScrubbingRuleEntryMatchVariable.REQUEST_IPADDRESS)
                    .withSelectorMatchOperator(ScrubbingRuleEntryMatchOperator.EQUALS_ANY)
                    .withState(ScrubbingRuleEntryState.ENABLED))))
            .withCustomRules(new CustomRuleList().withRules(Arrays.asList(
                new CustomRule().withName("Rule1")
                    .withPriority(1)
                    .withRuleType(RuleType.RATE_LIMIT_RULE)
                    .withRateLimitThreshold(1000)
                    .withMatchConditions(Arrays.asList(new MatchCondition().withMatchVariable(MatchVariable.REMOTE_ADDR)
                        .withOperator(Operator.IPMATCH)
                        .withMatchValue(Arrays.asList("192.168.1.0/24", "10.0.0.0/24"))))
                    .withAction(ActionType.BLOCK),
                new CustomRule().withName("Rule2")
                    .withPriority(2)
                    .withRuleType(RuleType.MATCH_RULE)
                    .withMatchConditions(Arrays.asList(
                        new MatchCondition().withMatchVariable(MatchVariable.REMOTE_ADDR)
                            .withOperator(Operator.GEO_MATCH)
                            .withMatchValue(Arrays.asList("CH")),
                        new MatchCondition().withMatchVariable(MatchVariable.REQUEST_HEADER)
                            .withSelector("UserAgent")
                            .withOperator(Operator.CONTAINS)
                            .withMatchValue(Arrays.asList("windows"))
                            .withTransforms(Arrays.asList(TransformType.LOWERCASE))))
                    .withAction(ActionType.BLOCK))))
            .withManagedRules(
                new ManagedRuleSetList()
                    .withManagedRuleSets(
                        Arrays
                            .asList(
                                new ManagedRuleSet().withRuleSetType("DefaultRuleSet")
                                    .withRuleSetVersion("1.0")
                                    .withRuleSetAction(ManagedRuleSetActionType.BLOCK)
                                    .withExclusions(Arrays.asList(new ManagedRuleExclusion()
                                        .withMatchVariable(ManagedRuleExclusionMatchVariable.REQUEST_HEADER_NAMES)
                                        .withSelectorMatchOperator(ManagedRuleExclusionSelectorMatchOperator.EQUALS)
                                        .withSelector("User-Agent")))
                                    .withRuleGroupOverrides(Arrays.asList(new ManagedRuleGroupOverride()
                                        .withRuleGroupName("SQLI")
                                        .withExclusions(Arrays.asList(new ManagedRuleExclusion()
                                            .withMatchVariable(ManagedRuleExclusionMatchVariable.REQUEST_COOKIE_NAMES)
                                            .withSelectorMatchOperator(
                                                ManagedRuleExclusionSelectorMatchOperator.STARTS_WITH)
                                            .withSelector("token")))
                                        .withRules(Arrays.asList(
                                            new ManagedRuleOverride().withRuleId("942100")
                                                .withEnabledState(ManagedRuleEnabledState.ENABLED)
                                                .withAction(ActionType.REDIRECT)
                                                .withExclusions(Arrays.asList(new ManagedRuleExclusion()
                                                    .withMatchVariable(
                                                        ManagedRuleExclusionMatchVariable.QUERY_STRING_ARG_NAMES)
                                                    .withSelectorMatchOperator(
                                                        ManagedRuleExclusionSelectorMatchOperator.EQUALS)
                                                    .withSelector("query"))),
                                            new ManagedRuleOverride().withRuleId("942110")
                                                .withEnabledState(ManagedRuleEnabledState.DISABLED))))))))
            .create();
    }
}
```

### Policies_Delete

```java
/**
 * Samples for Policies Delete.
 */
public final class PoliciesDeleteSamples {
    /*
     * x-ms-original-file: specification/frontdoor/resource-manager/Microsoft.Network/stable/2024-02-01/examples/WafPolicyDelete.json
     */
    /**
     * Sample code: Delete protection policy.
     * 
     * @param manager Entry point to FrontDoorManager.
     */
    public static void deleteProtectionPolicy(com.azure.resourcemanager.frontdoor.FrontDoorManager manager) {
        manager.policies().delete("rg1", "Policy1", com.azure.core.util.Context.NONE);
    }
}
```

### Policies_GetByResourceGroup

```java
/**
 * Samples for Policies GetByResourceGroup.
 */
public final class PoliciesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/frontdoor/resource-manager/Microsoft.Network/stable/2024-02-01/examples/WafPolicyGet.json
     */
    /**
     * Sample code: Get Policy.
     * 
     * @param manager Entry point to FrontDoorManager.
     */
    public static void getPolicy(com.azure.resourcemanager.frontdoor.FrontDoorManager manager) {
        manager.policies().getByResourceGroupWithResponse("rg1", "Policy1", com.azure.core.util.Context.NONE);
    }
}
```

### Policies_List

```java
/**
 * Samples for Policies List.
 */
public final class PoliciesListSamples {
    /*
     * x-ms-original-file: specification/frontdoor/resource-manager/Microsoft.Network/stable/2024-02-01/examples/WafListPoliciesUnderSubscription.json
     */
    /**
     * Sample code: Get all Policies in a Resource Group.
     * 
     * @param manager Entry point to FrontDoorManager.
     */
    public static void getAllPoliciesInAResourceGroup(com.azure.resourcemanager.frontdoor.FrontDoorManager manager) {
        manager.policies().list(com.azure.core.util.Context.NONE);
    }
}
```

### Policies_ListByResourceGroup

```java
/**
 * Samples for Policies ListByResourceGroup.
 */
public final class PoliciesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/frontdoor/resource-manager/Microsoft.Network/stable/2024-02-01/examples/WafListPolicies.json
     */
    /**
     * Sample code: Get all Policies in a Resource Group.
     * 
     * @param manager Entry point to FrontDoorManager.
     */
    public static void getAllPoliciesInAResourceGroup(com.azure.resourcemanager.frontdoor.FrontDoorManager manager) {
        manager.policies().listByResourceGroup("rg1", com.azure.core.util.Context.NONE);
    }
}
```

### Policies_Update

```java
import com.azure.resourcemanager.frontdoor.models.WebApplicationFirewallPolicy;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Policies Update.
 */
public final class PoliciesUpdateSamples {
    /*
     * x-ms-original-file: specification/frontdoor/resource-manager/Microsoft.Network/stable/2024-02-01/examples/WafPolicyPatch.json
     */
    /**
     * Sample code: Patches specific policy.
     * 
     * @param manager Entry point to FrontDoorManager.
     */
    public static void patchesSpecificPolicy(com.azure.resourcemanager.frontdoor.FrontDoorManager manager) {
        WebApplicationFirewallPolicy resource = manager.policies()
            .getByResourceGroupWithResponse("rg1", "Policy1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("key1", "fakeTokenPlaceholder", "key2", "fakeTokenPlaceholder")).apply();
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

### PreconfiguredEndpoints_List

```java
/**
 * Samples for PreconfiguredEndpoints List.
 */
public final class PreconfiguredEndpointsListSamples {
    /*
     * x-ms-original-file: specification/frontdoor/resource-manager/Microsoft.Network/stable/2019-11-01/examples/NetworkExperimentGetPreconfiguredEndpoints.json
     */
    /**
     * Sample code: Gets a list of Preconfigured Endpoints.
     * 
     * @param manager Entry point to FrontDoorManager.
     */
    public static void getsAListOfPreconfiguredEndpoints(com.azure.resourcemanager.frontdoor.FrontDoorManager manager) {
        manager.preconfiguredEndpoints().list("MyResourceGroup", "MyProfile", com.azure.core.util.Context.NONE);
    }
}
```

### Reports_GetLatencyScorecards

```java
import com.azure.resourcemanager.frontdoor.models.LatencyScorecardAggregationInterval;

/**
 * Samples for Reports GetLatencyScorecards.
 */
public final class ReportsGetLatencyScorecardsSamples {
    /*
     * x-ms-original-file: specification/frontdoor/resource-manager/Microsoft.Network/stable/2019-11-01/examples/NetworkExperimentGetLatencyScorecard.json
     */
    /**
     * Sample code: Gets a Latency Scorecard for a given Experiment.
     * 
     * @param manager Entry point to FrontDoorManager.
     */
    public static void
        getsALatencyScorecardForAGivenExperiment(com.azure.resourcemanager.frontdoor.FrontDoorManager manager) {
        manager.reports()
            .getLatencyScorecardsWithResponse("MyResourceGroup", "MyProfile", "MyExperiment",
                LatencyScorecardAggregationInterval.DAILY, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### Reports_GetTimeseries

```java
import com.azure.resourcemanager.frontdoor.models.TimeseriesAggregationInterval;
import com.azure.resourcemanager.frontdoor.models.TimeseriesType;
import java.time.OffsetDateTime;

/**
 * Samples for Reports GetTimeseries.
 */
public final class ReportsGetTimeseriesSamples {
    /*
     * x-ms-original-file: specification/frontdoor/resource-manager/Microsoft.Network/stable/2019-11-01/examples/NetworkExperimentGetTimeseries.json
     */
    /**
     * Sample code: Gets a Timeseries for a given Experiment.
     * 
     * @param manager Entry point to FrontDoorManager.
     */
    public static void
        getsATimeseriesForAGivenExperiment(com.azure.resourcemanager.frontdoor.FrontDoorManager manager) {
        manager.reports()
            .getTimeseriesWithResponse("MyResourceGroup", "MyProfile", "MyExperiment",
                OffsetDateTime.parse("2019-07-21T17:32:28Z"), OffsetDateTime.parse("2019-09-21T17:32:28Z"),
                TimeseriesAggregationInterval.HOURLY, TimeseriesType.MEASUREMENT_COUNTS, null, null,
                com.azure.core.util.Context.NONE);
    }
}
```

### RulesEngines_CreateOrUpdate

```java
import com.azure.core.management.SubResource;
import com.azure.resourcemanager.frontdoor.models.CacheConfiguration;
import com.azure.resourcemanager.frontdoor.models.DynamicCompressionEnabled;
import com.azure.resourcemanager.frontdoor.models.ForwardingConfiguration;
import com.azure.resourcemanager.frontdoor.models.FrontDoorForwardingProtocol;
import com.azure.resourcemanager.frontdoor.models.FrontDoorQuery;
import com.azure.resourcemanager.frontdoor.models.FrontDoorRedirectProtocol;
import com.azure.resourcemanager.frontdoor.models.FrontDoorRedirectType;
import com.azure.resourcemanager.frontdoor.models.HeaderAction;
import com.azure.resourcemanager.frontdoor.models.HeaderActionType;
import com.azure.resourcemanager.frontdoor.models.MatchProcessingBehavior;
import com.azure.resourcemanager.frontdoor.models.RedirectConfiguration;
import com.azure.resourcemanager.frontdoor.models.RulesEngineAction;
import com.azure.resourcemanager.frontdoor.models.RulesEngineMatchCondition;
import com.azure.resourcemanager.frontdoor.models.RulesEngineMatchVariable;
import com.azure.resourcemanager.frontdoor.models.RulesEngineOperator;
import com.azure.resourcemanager.frontdoor.models.RulesEngineRule;
import com.azure.resourcemanager.frontdoor.models.Transform;
import java.time.Duration;
import java.util.Arrays;

/**
 * Samples for RulesEngines CreateOrUpdate.
 */
public final class RulesEnginesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/frontdoor/resource-manager/Microsoft.Network/stable/2021-06-01/examples/FrontdoorRulesEngineCreate.json
     */
    /**
     * Sample code: Create or update a specific Rules Engine Configuration.
     * 
     * @param manager Entry point to FrontDoorManager.
     */
    public static void
        createOrUpdateASpecificRulesEngineConfiguration(com.azure.resourcemanager.frontdoor.FrontDoorManager manager) {
        manager.rulesEngines()
            .define("rulesEngine1")
            .withExistingFrontDoor("rg1", "frontDoor1")
            .withRules(Arrays.asList(
                new RulesEngineRule().withName("Rule1")
                    .withPriority(1)
                    .withAction(new RulesEngineAction().withRouteConfigurationOverride(
                        new RedirectConfiguration().withRedirectType(FrontDoorRedirectType.MOVED)
                            .withRedirectProtocol(FrontDoorRedirectProtocol.HTTPS_ONLY)
                            .withCustomHost("www.bing.com")
                            .withCustomPath("/api")
                            .withCustomFragment("fragment")
                            .withCustomQueryString("a=b")))
                    .withMatchConditions(Arrays.asList(new RulesEngineMatchCondition()
                        .withRulesEngineMatchVariable(RulesEngineMatchVariable.REMOTE_ADDR)
                        .withRulesEngineOperator(RulesEngineOperator.GEO_MATCH)
                        .withRulesEngineMatchValue(Arrays.asList("CH"))))
                    .withMatchProcessingBehavior(MatchProcessingBehavior.STOP),
                new RulesEngineRule().withName("Rule2")
                    .withPriority(2)
                    .withAction(new RulesEngineAction().withResponseHeaderActions(
                        Arrays.asList(new HeaderAction().withHeaderActionType(HeaderActionType.OVERWRITE)
                            .withHeaderName("Cache-Control")
                            .withValue("public, max-age=31536000"))))
                    .withMatchConditions(Arrays.asList(new RulesEngineMatchCondition()
                        .withRulesEngineMatchVariable(RulesEngineMatchVariable.REQUEST_FILENAME_EXTENSION)
                        .withRulesEngineOperator(RulesEngineOperator.EQUAL)
                        .withRulesEngineMatchValue(Arrays.asList("jpg"))
                        .withTransforms(Arrays.asList(Transform.LOWERCASE)))),
                new RulesEngineRule().withName("Rule3")
                    .withPriority(3)
                    .withAction(new RulesEngineAction().withRouteConfigurationOverride(
                        new ForwardingConfiguration().withForwardingProtocol(FrontDoorForwardingProtocol.HTTPS_ONLY)
                            .withCacheConfiguration(new CacheConfiguration()
                                .withQueryParameterStripDirective(FrontDoorQuery.STRIP_ONLY)
                                .withQueryParameters("a=b,p=q")
                                .withDynamicCompression(DynamicCompressionEnabled.DISABLED)
                                .withCacheDuration(Duration.parse("P1DT12H20M30S")))
                            .withBackendPool(new SubResource().withId(
                                "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.Network/frontDoors/frontDoor1/backendPools/backendPool1"))))
                    .withMatchConditions(Arrays.asList(new RulesEngineMatchCondition()
                        .withRulesEngineMatchVariable(RulesEngineMatchVariable.REQUEST_HEADER)
                        .withSelector("Rules-Engine-Route-Forward")
                        .withRulesEngineOperator(RulesEngineOperator.EQUAL)
                        .withNegateCondition(false)
                        .withRulesEngineMatchValue(Arrays.asList("allowoverride"))
                        .withTransforms(Arrays.asList(Transform.LOWERCASE))))))
            .create();
    }
}
```

### RulesEngines_Delete

```java
/**
 * Samples for RulesEngines Delete.
 */
public final class RulesEnginesDeleteSamples {
    /*
     * x-ms-original-file: specification/frontdoor/resource-manager/Microsoft.Network/stable/2021-06-01/examples/FrontdoorRulesEngineDelete.json
     */
    /**
     * Sample code: Delete Rules Engine Configuration.
     * 
     * @param manager Entry point to FrontDoorManager.
     */
    public static void deleteRulesEngineConfiguration(com.azure.resourcemanager.frontdoor.FrontDoorManager manager) {
        manager.rulesEngines().delete("rg1", "frontDoor1", "rulesEngine1", com.azure.core.util.Context.NONE);
    }
}
```

### RulesEngines_Get

```java
/**
 * Samples for RulesEngines Get.
 */
public final class RulesEnginesGetSamples {
    /*
     * x-ms-original-file: specification/frontdoor/resource-manager/Microsoft.Network/stable/2021-06-01/examples/FrontdoorRulesEngineGet.json
     */
    /**
     * Sample code: Get Rules Engine Configuration.
     * 
     * @param manager Entry point to FrontDoorManager.
     */
    public static void getRulesEngineConfiguration(com.azure.resourcemanager.frontdoor.FrontDoorManager manager) {
        manager.rulesEngines().getWithResponse("rg1", "frontDoor1", "rulesEngine1", com.azure.core.util.Context.NONE);
    }
}
```

### RulesEngines_ListByFrontDoor

```java
/**
 * Samples for RulesEngines ListByFrontDoor.
 */
public final class RulesEnginesListByFrontDoorSamples {
    /*
     * x-ms-original-file: specification/frontdoor/resource-manager/Microsoft.Network/stable/2021-06-01/examples/FrontdoorRulesEngineList.json
     */
    /**
     * Sample code: List Rules Engine Configurations in a Front Door.
     * 
     * @param manager Entry point to FrontDoorManager.
     */
    public static void
        listRulesEngineConfigurationsInAFrontDoor(com.azure.resourcemanager.frontdoor.FrontDoorManager manager) {
        manager.rulesEngines().listByFrontDoor("rg1", "frontDoor1", com.azure.core.util.Context.NONE);
    }
}
```

