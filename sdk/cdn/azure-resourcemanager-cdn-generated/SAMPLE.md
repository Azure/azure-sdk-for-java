# Code snippets and samples


## AfdCustomDomains

- [Create](#afdcustomdomains_create)
- [Delete](#afdcustomdomains_delete)
- [Get](#afdcustomdomains_get)
- [ListByProfile](#afdcustomdomains_listbyprofile)
- [RefreshValidationToken](#afdcustomdomains_refreshvalidationtoken)
- [Update](#afdcustomdomains_update)

## AfdEndpoints

- [Create](#afdendpoints_create)
- [Delete](#afdendpoints_delete)
- [Get](#afdendpoints_get)
- [ListByProfile](#afdendpoints_listbyprofile)
- [ListResourceUsage](#afdendpoints_listresourceusage)
- [PurgeContent](#afdendpoints_purgecontent)
- [Update](#afdendpoints_update)
- [ValidateCustomDomain](#afdendpoints_validatecustomdomain)

## AfdOriginGroups

- [Create](#afdorigingroups_create)
- [Delete](#afdorigingroups_delete)
- [Get](#afdorigingroups_get)
- [ListByProfile](#afdorigingroups_listbyprofile)
- [ListResourceUsage](#afdorigingroups_listresourceusage)
- [Update](#afdorigingroups_update)

## AfdOrigins

- [Create](#afdorigins_create)
- [Delete](#afdorigins_delete)
- [Get](#afdorigins_get)
- [ListByOriginGroup](#afdorigins_listbyorigingroup)
- [Update](#afdorigins_update)

## AfdProfiles

- [CheckHostnameAvailability](#afdprofiles_checkhostnameavailability)
- [ListResourceUsage](#afdprofiles_listresourceusage)

## CustomDomains

- [Create](#customdomains_create)
- [Delete](#customdomains_delete)
- [DisableCustomHttps](#customdomains_disablecustomhttps)
- [EnableCustomHttps](#customdomains_enablecustomhttps)
- [Get](#customdomains_get)
- [ListByEndpoint](#customdomains_listbyendpoint)

## EdgeNodes

- [List](#edgenodes_list)

## Endpoints

- [Create](#endpoints_create)
- [Delete](#endpoints_delete)
- [Get](#endpoints_get)
- [ListByProfile](#endpoints_listbyprofile)
- [ListResourceUsage](#endpoints_listresourceusage)
- [LoadContent](#endpoints_loadcontent)
- [PurgeContent](#endpoints_purgecontent)
- [Start](#endpoints_start)
- [Stop](#endpoints_stop)
- [Update](#endpoints_update)
- [ValidateCustomDomain](#endpoints_validatecustomdomain)

## LogAnalytics

- [GetLogAnalyticsLocations](#loganalytics_getloganalyticslocations)
- [GetLogAnalyticsMetrics](#loganalytics_getloganalyticsmetrics)
- [GetLogAnalyticsRankings](#loganalytics_getloganalyticsrankings)
- [GetLogAnalyticsResources](#loganalytics_getloganalyticsresources)
- [GetWafLogAnalyticsMetrics](#loganalytics_getwafloganalyticsmetrics)
- [GetWafLogAnalyticsRankings](#loganalytics_getwafloganalyticsrankings)

## ManagedRuleSets

- [List](#managedrulesets_list)

## Operations

- [List](#operations_list)

## OriginGroups

- [Create](#origingroups_create)
- [Delete](#origingroups_delete)
- [Get](#origingroups_get)
- [ListByEndpoint](#origingroups_listbyendpoint)
- [Update](#origingroups_update)

## Origins

- [Create](#origins_create)
- [Delete](#origins_delete)
- [Get](#origins_get)
- [ListByEndpoint](#origins_listbyendpoint)
- [Update](#origins_update)

## Policies

- [CreateOrUpdate](#policies_createorupdate)
- [Delete](#policies_delete)
- [GetByResourceGroup](#policies_getbyresourcegroup)
- [ListByResourceGroup](#policies_listbyresourcegroup)
- [Update](#policies_update)

## Profiles

- [Create](#profiles_create)
- [Delete](#profiles_delete)
- [GenerateSsoUri](#profiles_generatessouri)
- [GetByResourceGroup](#profiles_getbyresourcegroup)
- [List](#profiles_list)
- [ListByResourceGroup](#profiles_listbyresourcegroup)
- [ListResourceUsage](#profiles_listresourceusage)
- [ListSupportedOptimizationTypes](#profiles_listsupportedoptimizationtypes)
- [Update](#profiles_update)

## ResourceProvider

- [CheckEndpointNameAvailability](#resourceprovider_checkendpointnameavailability)
- [CheckNameAvailability](#resourceprovider_checknameavailability)
- [CheckNameAvailabilityWithSubscription](#resourceprovider_checknameavailabilitywithsubscription)
- [ValidateProbe](#resourceprovider_validateprobe)

## ResourceUsage

- [List](#resourceusage_list)

## Routes

- [Create](#routes_create)
- [Delete](#routes_delete)
- [Get](#routes_get)
- [ListByEndpoint](#routes_listbyendpoint)
- [Update](#routes_update)

## RuleSets

- [Create](#rulesets_create)
- [Delete](#rulesets_delete)
- [Get](#rulesets_get)
- [ListByProfile](#rulesets_listbyprofile)
- [ListResourceUsage](#rulesets_listresourceusage)

## Rules

- [Create](#rules_create)
- [Delete](#rules_delete)
- [Get](#rules_get)
- [ListByRuleSet](#rules_listbyruleset)
- [Update](#rules_update)

## Secrets

- [Create](#secrets_create)
- [Delete](#secrets_delete)
- [Get](#secrets_get)
- [ListByProfile](#secrets_listbyprofile)

## SecurityPolicies

- [Create](#securitypolicies_create)
- [Delete](#securitypolicies_delete)
- [Get](#securitypolicies_get)
- [ListByProfile](#securitypolicies_listbyprofile)
- [Patch](#securitypolicies_patch)

## Validate

- [Secret](#validate_secret)
### AfdCustomDomains_Create

```java
import com.azure.resourcemanager.cdn.generated.models.AfdCertificateType;
import com.azure.resourcemanager.cdn.generated.models.AfdDomainHttpsParameters;
import com.azure.resourcemanager.cdn.generated.models.AfdMinimumTlsVersion;
import com.azure.resourcemanager.cdn.generated.models.ResourceReference;

/** Samples for AfdCustomDomains Create. */
public final class AfdCustomDomainsCreateSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/AFDCustomDomains_Create.json
     */
    /**
     * Sample code: AFDCustomDomains_Create.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void aFDCustomDomainsCreate(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager
            .afdCustomDomains()
            .define("domain1")
            .withExistingProfile("RG", "profile1")
            .withHostname("www.someDomain.net")
            .withTlsSettings(
                new AfdDomainHttpsParameters()
                    .withCertificateType(AfdCertificateType.MANAGED_CERTIFICATE)
                    .withMinimumTlsVersion(AfdMinimumTlsVersion.TLS12))
            .withAzureDnsZone(new ResourceReference().withId(""))
            .create();
    }
}
```

### AfdCustomDomains_Delete

```java
import com.azure.core.util.Context;

/** Samples for AfdCustomDomains Delete. */
public final class AfdCustomDomainsDeleteSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/AFDCustomDomains_Delete.json
     */
    /**
     * Sample code: AFDCustomDomains_Delete.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void aFDCustomDomainsDelete(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.afdCustomDomains().delete("RG", "profile1", "domain1", Context.NONE);
    }
}
```

### AfdCustomDomains_Get

```java
import com.azure.core.util.Context;

/** Samples for AfdCustomDomains Get. */
public final class AfdCustomDomainsGetSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/AFDCustomDomains_Get.json
     */
    /**
     * Sample code: AFDCustomDomains_Get.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void aFDCustomDomainsGet(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.afdCustomDomains().getWithResponse("RG", "profile1", "domain1", Context.NONE);
    }
}
```

### AfdCustomDomains_ListByProfile

```java
import com.azure.core.util.Context;

/** Samples for AfdCustomDomains ListByProfile. */
public final class AfdCustomDomainsListByProfileSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/AFDCustomDomains_ListByProfile.json
     */
    /**
     * Sample code: AFDCustomDomains_ListByProfile.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void aFDCustomDomainsListByProfile(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.afdCustomDomains().listByProfile("RG", "profile1", Context.NONE);
    }
}
```

### AfdCustomDomains_RefreshValidationToken

```java
import com.azure.core.util.Context;

/** Samples for AfdCustomDomains RefreshValidationToken. */
public final class AfdCustomDomainsRefreshValidationTokenSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/AFDCustomDomains_RefreshValidationToken.json
     */
    /**
     * Sample code: AFDCustomDomains_Delete.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void aFDCustomDomainsDelete(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.afdCustomDomains().refreshValidationToken("RG", "profile1", "domain1", Context.NONE);
    }
}
```

### AfdCustomDomains_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.cdn.generated.models.AfdCertificateType;
import com.azure.resourcemanager.cdn.generated.models.AfdDomain;
import com.azure.resourcemanager.cdn.generated.models.AfdDomainHttpsParameters;
import com.azure.resourcemanager.cdn.generated.models.AfdMinimumTlsVersion;
import com.azure.resourcemanager.cdn.generated.models.ResourceReference;

/** Samples for AfdCustomDomains Update. */
public final class AfdCustomDomainsUpdateSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/AFDCustomDomains_Update.json
     */
    /**
     * Sample code: AFDCustomDomains_Update.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void aFDCustomDomainsUpdate(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        AfdDomain resource =
            manager.afdCustomDomains().getWithResponse("RG", "profile1", "domain1", Context.NONE).getValue();
        resource
            .update()
            .withTlsSettings(
                new AfdDomainHttpsParameters()
                    .withCertificateType(AfdCertificateType.CUSTOMER_CERTIFICATE)
                    .withMinimumTlsVersion(AfdMinimumTlsVersion.TLS12))
            .withAzureDnsZone(new ResourceReference().withId(""))
            .apply();
    }
}
```

### AfdEndpoints_Create

```java
/** Samples for AfdEndpoints Create. */
public final class AfdEndpointsCreateSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/AFDEndpoints_Create.json
     */
    /**
     * Sample code: AFDEndpoints_Create.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void aFDEndpointsCreate(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager
            .afdEndpoints()
            .define("endpoint1")
            .withRegion((String) null)
            .withExistingProfile("RG", "profile1")
            .create();
    }
}
```

### AfdEndpoints_Delete

```java
import com.azure.core.util.Context;

/** Samples for AfdEndpoints Delete. */
public final class AfdEndpointsDeleteSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/AFDEndpoints_Delete.json
     */
    /**
     * Sample code: AFDEndpoints_Delete.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void aFDEndpointsDelete(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.afdEndpoints().delete("RG", "profile1", "endpoint1", Context.NONE);
    }
}
```

### AfdEndpoints_Get

```java
import com.azure.core.util.Context;

/** Samples for AfdEndpoints Get. */
public final class AfdEndpointsGetSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/AFDEndpoints_Get.json
     */
    /**
     * Sample code: AFDEndpoints_Get.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void aFDEndpointsGet(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.afdEndpoints().getWithResponse("RG", "profile1", "endpoint1", Context.NONE);
    }
}
```

### AfdEndpoints_ListByProfile

```java
import com.azure.core.util.Context;

/** Samples for AfdEndpoints ListByProfile. */
public final class AfdEndpointsListByProfileSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/AFDEndpoints_ListByProfile.json
     */
    /**
     * Sample code: AFDEndpoints_ListByProfile.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void aFDEndpointsListByProfile(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.afdEndpoints().listByProfile("RG", "profile1", Context.NONE);
    }
}
```

### AfdEndpoints_ListResourceUsage

```java
import com.azure.core.util.Context;

/** Samples for AfdEndpoints ListResourceUsage. */
public final class AfdEndpointsListResourceUsageSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/AFDEndpoints_ListResourceUsage.json
     */
    /**
     * Sample code: AFDEndpoints_ListResourceUsage.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void aFDEndpointsListResourceUsage(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.afdEndpoints().listResourceUsage("RG", "profile1", "endpoint1", Context.NONE);
    }
}
```

### AfdEndpoints_PurgeContent

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.cdn.generated.models.AfdPurgeParameters;
import java.util.Arrays;

/** Samples for AfdEndpoints PurgeContent. */
public final class AfdEndpointsPurgeContentSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/AFDEndpoints_PurgeContent.json
     */
    /**
     * Sample code: AFDEndpoints_PurgeContent.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void aFDEndpointsPurgeContent(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager
            .afdEndpoints()
            .purgeContent(
                "RG",
                "profile1",
                "endpoint1",
                new AfdPurgeParameters()
                    .withContentPaths(Arrays.asList("/folder1"))
                    .withDomains(Arrays.asList("endpoint1-abcdefghijklmnop.z01.azurefd.net")),
                Context.NONE);
    }
}
```

### AfdEndpoints_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.cdn.generated.models.AfdEndpoint;
import com.azure.resourcemanager.cdn.generated.models.EnabledState;
import java.util.HashMap;
import java.util.Map;

/** Samples for AfdEndpoints Update. */
public final class AfdEndpointsUpdateSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/AFDEndpoints_Update.json
     */
    /**
     * Sample code: AFDEndpoints_Update.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void aFDEndpointsUpdate(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        AfdEndpoint resource =
            manager.afdEndpoints().getWithResponse("RG", "profile1", "endpoint1", Context.NONE).getValue();
        resource.update().withTags(mapOf()).withEnabledState(EnabledState.ENABLED).apply();
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

### AfdEndpoints_ValidateCustomDomain

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.cdn.generated.models.ValidateCustomDomainInput;

/** Samples for AfdEndpoints ValidateCustomDomain. */
public final class AfdEndpointsValidateCustomDomainSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/AFDEndpoints_ValidateCustomDomain.json
     */
    /**
     * Sample code: Endpoints_ValidateCustomDomain.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void endpointsValidateCustomDomain(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager
            .afdEndpoints()
            .validateCustomDomainWithResponse(
                "RG",
                "profile1",
                "endpoint1",
                new ValidateCustomDomainInput().withHostname("www.someDomain.com"),
                Context.NONE);
    }
}
```

### AfdOriginGroups_Create

```java
import com.azure.resourcemanager.cdn.generated.models.HealthProbeParameters;
import com.azure.resourcemanager.cdn.generated.models.HealthProbeRequestType;
import com.azure.resourcemanager.cdn.generated.models.LoadBalancingSettingsParameters;
import com.azure.resourcemanager.cdn.generated.models.ProbeProtocol;

/** Samples for AfdOriginGroups Create. */
public final class AfdOriginGroupsCreateSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/AFDOriginGroups_Create.json
     */
    /**
     * Sample code: AFDOriginGroups_Create.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void aFDOriginGroupsCreate(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager
            .afdOriginGroups()
            .define("origingroup1")
            .withExistingProfile("RG", "profile1")
            .withLoadBalancingSettings(
                new LoadBalancingSettingsParameters()
                    .withSampleSize(3)
                    .withSuccessfulSamplesRequired(3)
                    .withAdditionalLatencyInMilliseconds(1000))
            .withHealthProbeSettings(
                new HealthProbeParameters()
                    .withProbePath("/path2")
                    .withProbeRequestType(HealthProbeRequestType.NOT_SET)
                    .withProbeProtocol(ProbeProtocol.NOT_SET)
                    .withProbeIntervalInSeconds(10))
            .withTrafficRestorationTimeToHealedOrNewEndpointsInMinutes(5)
            .create();
    }
}
```

### AfdOriginGroups_Delete

```java
import com.azure.core.util.Context;

/** Samples for AfdOriginGroups Delete. */
public final class AfdOriginGroupsDeleteSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/AFDOriginGroups_Delete.json
     */
    /**
     * Sample code: AFDOriginGroups_Delete.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void aFDOriginGroupsDelete(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.afdOriginGroups().delete("RG", "profile1", "origingroup1", Context.NONE);
    }
}
```

### AfdOriginGroups_Get

```java
import com.azure.core.util.Context;

/** Samples for AfdOriginGroups Get. */
public final class AfdOriginGroupsGetSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/AFDOriginGroups_Get.json
     */
    /**
     * Sample code: AFDOriginGroups_Get.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void aFDOriginGroupsGet(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.afdOriginGroups().getWithResponse("RG", "profile1", "origingroup1", Context.NONE);
    }
}
```

### AfdOriginGroups_ListByProfile

```java
import com.azure.core.util.Context;

/** Samples for AfdOriginGroups ListByProfile. */
public final class AfdOriginGroupsListByProfileSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/AFDOriginGroups_ListByProfile.json
     */
    /**
     * Sample code: AFDOriginGroups_ListByProfile.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void aFDOriginGroupsListByProfile(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.afdOriginGroups().listByProfile("RG", "profile1", Context.NONE);
    }
}
```

### AfdOriginGroups_ListResourceUsage

```java
import com.azure.core.util.Context;

/** Samples for AfdOriginGroups ListResourceUsage. */
public final class AfdOriginGroupsListResourceUsageSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/AFDOriginGroups_ListResourceUsage.json
     */
    /**
     * Sample code: AFDOriginGroups_ListResourceUsage.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void aFDOriginGroupsListResourceUsage(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.afdOriginGroups().listResourceUsage("RG", "profile1", "origingroup1", Context.NONE);
    }
}
```

### AfdOriginGroups_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.cdn.generated.models.AfdOriginGroup;
import com.azure.resourcemanager.cdn.generated.models.HealthProbeParameters;
import com.azure.resourcemanager.cdn.generated.models.HealthProbeRequestType;
import com.azure.resourcemanager.cdn.generated.models.LoadBalancingSettingsParameters;
import com.azure.resourcemanager.cdn.generated.models.ProbeProtocol;

/** Samples for AfdOriginGroups Update. */
public final class AfdOriginGroupsUpdateSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/AFDOriginGroups_Update.json
     */
    /**
     * Sample code: AFDOriginGroups_Update.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void aFDOriginGroupsUpdate(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        AfdOriginGroup resource =
            manager.afdOriginGroups().getWithResponse("RG", "profile1", "origingroup1", Context.NONE).getValue();
        resource
            .update()
            .withLoadBalancingSettings(
                new LoadBalancingSettingsParameters()
                    .withSampleSize(3)
                    .withSuccessfulSamplesRequired(3)
                    .withAdditionalLatencyInMilliseconds(1000))
            .withHealthProbeSettings(
                new HealthProbeParameters()
                    .withProbePath("/path2")
                    .withProbeRequestType(HealthProbeRequestType.NOT_SET)
                    .withProbeProtocol(ProbeProtocol.NOT_SET)
                    .withProbeIntervalInSeconds(10))
            .withTrafficRestorationTimeToHealedOrNewEndpointsInMinutes(5)
            .apply();
    }
}
```

### AfdOrigins_Create

```java
import com.azure.resourcemanager.cdn.generated.models.EnabledState;

/** Samples for AfdOrigins Create. */
public final class AfdOriginsCreateSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/AFDOrigins_Create.json
     */
    /**
     * Sample code: AFDOrigins_Create.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void aFDOriginsCreate(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager
            .afdOrigins()
            .define("origin1")
            .withExistingOriginGroup("RG", "profile1", "origingroup1")
            .withHostname("host1.blob.core.windows.net")
            .withHttpPort(80)
            .withHttpsPort(443)
            .withOriginHostHeader("host1.foo.com")
            .withEnabledState(EnabledState.ENABLED)
            .create();
    }
}
```

### AfdOrigins_Delete

```java
import com.azure.core.util.Context;

/** Samples for AfdOrigins Delete. */
public final class AfdOriginsDeleteSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/AFDOrigins_Delete.json
     */
    /**
     * Sample code: AFDOrigins_Delete.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void aFDOriginsDelete(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.afdOrigins().delete("RG", "profile1", "origingroup1", "origin1", Context.NONE);
    }
}
```

### AfdOrigins_Get

```java
import com.azure.core.util.Context;

/** Samples for AfdOrigins Get. */
public final class AfdOriginsGetSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/AFDOrigins_Get.json
     */
    /**
     * Sample code: AFDOrigins_Get.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void aFDOriginsGet(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.afdOrigins().getWithResponse("RG", "profile1", "origingroup1", "origin1", Context.NONE);
    }
}
```

### AfdOrigins_ListByOriginGroup

```java
import com.azure.core.util.Context;

/** Samples for AfdOrigins ListByOriginGroup. */
public final class AfdOriginsListByOriginGroupSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/AFDOrigins_ListByOriginGroup.json
     */
    /**
     * Sample code: AFDOrigins_ListByOriginGroup.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void aFDOriginsListByOriginGroup(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.afdOrigins().listByOriginGroup("RG", "profile1", "origingroup1", Context.NONE);
    }
}
```

### AfdOrigins_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.cdn.generated.models.AfdOrigin;
import com.azure.resourcemanager.cdn.generated.models.EnabledState;

/** Samples for AfdOrigins Update. */
public final class AfdOriginsUpdateSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/AFDOrigins_Update.json
     */
    /**
     * Sample code: AFDOrigins_Update.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void aFDOriginsUpdate(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        AfdOrigin resource =
            manager.afdOrigins().getWithResponse("RG", "profile1", "origingroup1", "origin1", Context.NONE).getValue();
        resource
            .update()
            .withHostname("host1.blob.core.windows.net")
            .withHttpPort(80)
            .withHttpsPort(443)
            .withEnabledState(EnabledState.ENABLED)
            .apply();
    }
}
```

### AfdProfiles_CheckHostnameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.cdn.generated.models.CheckHostnameAvailabilityInput;

/** Samples for AfdProfiles CheckHostnameAvailability. */
public final class AfdProfilesCheckHostnameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/AFDProfiles_CheckHostNameAvailability.json
     */
    /**
     * Sample code: AFDProfiles_CheckHostNameAvailability.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void aFDProfilesCheckHostNameAvailability(
        com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager
            .afdProfiles()
            .checkHostnameAvailabilityWithResponse(
                "RG",
                "profile1",
                new CheckHostnameAvailabilityInput().withHostname("www.someDomain.net"),
                Context.NONE);
    }
}
```

### AfdProfiles_ListResourceUsage

```java
import com.azure.core.util.Context;

/** Samples for AfdProfiles ListResourceUsage. */
public final class AfdProfilesListResourceUsageSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/AFDProfiles_ListResourceUsage.json
     */
    /**
     * Sample code: AFDProfiles_ListResourceUsage.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void aFDProfilesListResourceUsage(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.afdProfiles().listResourceUsage("RG", "profile1", Context.NONE);
    }
}
```

### CustomDomains_Create

```java
/** Samples for CustomDomains Create. */
public final class CustomDomainsCreateSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/CustomDomains_Create.json
     */
    /**
     * Sample code: CustomDomains_Create.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void customDomainsCreate(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager
            .customDomains()
            .define("www-someDomain-net")
            .withExistingEndpoint("RG", "profile1", "endpoint1")
            .withHostname("www.someDomain.net")
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
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/CustomDomains_Delete.json
     */
    /**
     * Sample code: CustomDomains_Delete.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void customDomainsDelete(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.customDomains().delete("RG", "profile1", "endpoint1", "www-someDomain-net", Context.NONE);
    }
}
```

### CustomDomains_DisableCustomHttps

```java
import com.azure.core.util.Context;

/** Samples for CustomDomains DisableCustomHttps. */
public final class CustomDomainsDisableCustomHttpsSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/CustomDomains_DisableCustomHttps.json
     */
    /**
     * Sample code: CustomDomains_DisableCustomHttps.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void customDomainsDisableCustomHttps(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager
            .customDomains()
            .disableCustomHttpsWithResponse("RG", "profile1", "endpoint1", "www-someDomain-net", Context.NONE);
    }
}
```

### CustomDomains_EnableCustomHttps

```java
import com.azure.core.util.Context;

/** Samples for CustomDomains EnableCustomHttps. */
public final class CustomDomainsEnableCustomHttpsSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/CustomDomains_EnableCustomHttpsUsingCDNManagedCertificate.json
     */
    /**
     * Sample code: CustomDomains_EnableCustomHttpsUsingCDNManagedCertificate.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void customDomainsEnableCustomHttpsUsingCDNManagedCertificate(
        com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager
            .customDomains()
            .enableCustomHttpsWithResponse("RG", "profile1", "endpoint1", "www-someDomain-net", null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/CustomDomains_EnableCustomHttpsUsingBYOC.json
     */
    /**
     * Sample code: CustomDomains_EnableCustomHttpsUsingYourOwnCertificate.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void customDomainsEnableCustomHttpsUsingYourOwnCertificate(
        com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager
            .customDomains()
            .enableCustomHttpsWithResponse("RG", "profile1", "endpoint1", "www-someDomain-net", null, Context.NONE);
    }
}
```

### CustomDomains_Get

```java
import com.azure.core.util.Context;

/** Samples for CustomDomains Get. */
public final class CustomDomainsGetSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/CustomDomains_Get.json
     */
    /**
     * Sample code: CustomDomains_Get.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void customDomainsGet(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.customDomains().getWithResponse("RG", "profile1", "endpoint1", "www-someDomain-net", Context.NONE);
    }
}
```

### CustomDomains_ListByEndpoint

```java
import com.azure.core.util.Context;

/** Samples for CustomDomains ListByEndpoint. */
public final class CustomDomainsListByEndpointSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/CustomDomains_ListByEndpoint.json
     */
    /**
     * Sample code: CustomDomains_ListByEndpoint.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void customDomainsListByEndpoint(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.customDomains().listByEndpoint("RG", "profile1", "endpoint1", Context.NONE);
    }
}
```

### EdgeNodes_List

```java
import com.azure.core.util.Context;

/** Samples for EdgeNodes List. */
public final class EdgeNodesListSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/EdgeNodes_List.json
     */
    /**
     * Sample code: EdgeNodes_List.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void edgeNodesList(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.edgeNodes().list(Context.NONE);
    }
}
```

### Endpoints_Create

```java
/** Samples for Endpoints Create. */
public final class EndpointsCreateSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/Endpoints_Create.json
     */
    /**
     * Sample code: Endpoints_Create.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void endpointsCreate(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager
            .endpoints()
            .define("endpoint1")
            .withRegion((String) null)
            .withExistingProfile("RG", "profile1")
            .create();
    }
}
```

### Endpoints_Delete

```java
import com.azure.core.util.Context;

/** Samples for Endpoints Delete. */
public final class EndpointsDeleteSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/Endpoints_Delete.json
     */
    /**
     * Sample code: Endpoints_Delete.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void endpointsDelete(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.endpoints().delete("RG", "profile1", "endpoint1", Context.NONE);
    }
}
```

### Endpoints_Get

```java
import com.azure.core.util.Context;

/** Samples for Endpoints Get. */
public final class EndpointsGetSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/Endpoints_Get.json
     */
    /**
     * Sample code: Endpoints_Get.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void endpointsGet(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.endpoints().getWithResponse("RG", "profile1", "endpoint1", Context.NONE);
    }
}
```

### Endpoints_ListByProfile

```java
import com.azure.core.util.Context;

/** Samples for Endpoints ListByProfile. */
public final class EndpointsListByProfileSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/Endpoints_ListByProfile.json
     */
    /**
     * Sample code: Endpoints_ListByProfile.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void endpointsListByProfile(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.endpoints().listByProfile("RG", "profile1", Context.NONE);
    }
}
```

### Endpoints_ListResourceUsage

```java
import com.azure.core.util.Context;

/** Samples for Endpoints ListResourceUsage. */
public final class EndpointsListResourceUsageSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/Endpoints_ListResourceUsage.json
     */
    /**
     * Sample code: Endpoints_ListResourceUsage.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void endpointsListResourceUsage(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.endpoints().listResourceUsage("RG", "profile1", "endpoint1", Context.NONE);
    }
}
```

### Endpoints_LoadContent

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.cdn.generated.models.LoadParameters;
import java.util.Arrays;

/** Samples for Endpoints LoadContent. */
public final class EndpointsLoadContentSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/Endpoints_LoadContent.json
     */
    /**
     * Sample code: Endpoints_LoadContent.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void endpointsLoadContent(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager
            .endpoints()
            .loadContent(
                "RG",
                "profile1",
                "endpoint1",
                new LoadParameters().withContentPaths(Arrays.asList("/folder1")),
                Context.NONE);
    }
}
```

### Endpoints_PurgeContent

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.cdn.generated.models.PurgeParameters;
import java.util.Arrays;

/** Samples for Endpoints PurgeContent. */
public final class EndpointsPurgeContentSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/Endpoints_PurgeContent.json
     */
    /**
     * Sample code: Endpoints_PurgeContent.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void endpointsPurgeContent(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager
            .endpoints()
            .purgeContent(
                "RG",
                "profile1",
                "endpoint1",
                new PurgeParameters().withContentPaths(Arrays.asList("/folder1")),
                Context.NONE);
    }
}
```

### Endpoints_Start

```java
import com.azure.core.util.Context;

/** Samples for Endpoints Start. */
public final class EndpointsStartSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/Endpoints_Start.json
     */
    /**
     * Sample code: Endpoints_Start.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void endpointsStart(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.endpoints().start("RG", "profile1", "endpoint1", Context.NONE);
    }
}
```

### Endpoints_Stop

```java
import com.azure.core.util.Context;

/** Samples for Endpoints Stop. */
public final class EndpointsStopSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/Endpoints_Stop.json
     */
    /**
     * Sample code: Endpoints_Stop.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void endpointsStop(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.endpoints().stop("RG", "profile1", "endpoint1", Context.NONE);
    }
}
```

### Endpoints_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.cdn.generated.models.Endpoint;
import java.util.HashMap;
import java.util.Map;

/** Samples for Endpoints Update. */
public final class EndpointsUpdateSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/Endpoints_Update.json
     */
    /**
     * Sample code: Endpoints_Update.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void endpointsUpdate(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        Endpoint resource = manager.endpoints().getWithResponse("RG", "profile1", "endpoint1", Context.NONE).getValue();
        resource.update().withTags(mapOf("additionalProperties", "Tag1")).apply();
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

### Endpoints_ValidateCustomDomain

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.cdn.generated.models.ValidateCustomDomainInput;

/** Samples for Endpoints ValidateCustomDomain. */
public final class EndpointsValidateCustomDomainSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/Endpoints_ValidateCustomDomain.json
     */
    /**
     * Sample code: Endpoints_ValidateCustomDomain.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void endpointsValidateCustomDomain(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager
            .endpoints()
            .validateCustomDomainWithResponse(
                "RG",
                "profile1",
                "endpoint1",
                new ValidateCustomDomainInput().withHostname("www.someDomain.com"),
                Context.NONE);
    }
}
```

### LogAnalytics_GetLogAnalyticsLocations

```java
import com.azure.core.util.Context;

/** Samples for LogAnalytics GetLogAnalyticsLocations. */
public final class LogAnalyticsGetLogAnalyticsLocationsSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/LogAnalytics_GetLogAnalyticsLocations.json
     */
    /**
     * Sample code: LogAnalytics_GetLogAnalyticsLocations.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void logAnalyticsGetLogAnalyticsLocations(
        com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.logAnalytics().getLogAnalyticsLocationsWithResponse("RG", "profile1", Context.NONE);
    }
}
```

### LogAnalytics_GetLogAnalyticsMetrics

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.cdn.generated.models.LogMetric;
import com.azure.resourcemanager.cdn.generated.models.LogMetricsGranularity;
import com.azure.resourcemanager.cdn.generated.models.LogMetricsGroupBy;
import java.time.OffsetDateTime;
import java.util.Arrays;

/** Samples for LogAnalytics GetLogAnalyticsMetrics. */
public final class LogAnalyticsGetLogAnalyticsMetricsSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/LogAnalytics_GetLogAnalyticsMetrics.json
     */
    /**
     * Sample code: LogAnalytics_GetLogAnalyticsMetrics.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void logAnalyticsGetLogAnalyticsMetrics(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager
            .logAnalytics()
            .getLogAnalyticsMetricsWithResponse(
                "RG",
                "profile1",
                Arrays.asList(LogMetric.CLIENT_REQUEST_COUNT),
                OffsetDateTime.parse("2020-11-04T04:30:00.000Z"),
                OffsetDateTime.parse("2020-11-04T05:00:00.000Z"),
                LogMetricsGranularity.PT5M,
                Arrays.asList("customdomain1.azurecdn.net", "customdomain2.azurecdn.net"),
                Arrays.asList("https"),
                Arrays.asList(LogMetricsGroupBy.PROTOCOL),
                null,
                null,
                Context.NONE);
    }
}
```

### LogAnalytics_GetLogAnalyticsRankings

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.cdn.generated.models.LogRanking;
import com.azure.resourcemanager.cdn.generated.models.LogRankingMetric;
import java.time.OffsetDateTime;
import java.util.Arrays;

/** Samples for LogAnalytics GetLogAnalyticsRankings. */
public final class LogAnalyticsGetLogAnalyticsRankingsSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/LogAnalytics_GetLogAnalyticsRankings.json
     */
    /**
     * Sample code: LogAnalytics_GetLogAnalyticsRankings.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void logAnalyticsGetLogAnalyticsRankings(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager
            .logAnalytics()
            .getLogAnalyticsRankingsWithResponse(
                "RG",
                "profile1",
                Arrays.asList(LogRanking.URL),
                Arrays.asList(LogRankingMetric.CLIENT_REQUEST_COUNT),
                5,
                OffsetDateTime.parse("2020-11-04T06:49:27.554Z"),
                OffsetDateTime.parse("2020-11-04T09:49:27.554Z"),
                null,
                Context.NONE);
    }
}
```

### LogAnalytics_GetLogAnalyticsResources

```java
import com.azure.core.util.Context;

/** Samples for LogAnalytics GetLogAnalyticsResources. */
public final class LogAnalyticsGetLogAnalyticsResourcesSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/LogAnalytics_GetLogAnalyticsResources.json
     */
    /**
     * Sample code: LogAnalytics_GetLogAnalyticsResources.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void logAnalyticsGetLogAnalyticsResources(
        com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.logAnalytics().getLogAnalyticsResourcesWithResponse("RG", "profile1", Context.NONE);
    }
}
```

### LogAnalytics_GetWafLogAnalyticsMetrics

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.cdn.generated.models.WafAction;
import com.azure.resourcemanager.cdn.generated.models.WafGranularity;
import com.azure.resourcemanager.cdn.generated.models.WafMetric;
import java.time.OffsetDateTime;
import java.util.Arrays;

/** Samples for LogAnalytics GetWafLogAnalyticsMetrics. */
public final class LogAnalyticsGetWafLogAnalyticsMetricsSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/LogAnalytics_GetWafLogAnalyticsMetrics.json
     */
    /**
     * Sample code: LogAnalytics_GetWafLogAnalyticsMetrics.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void logAnalyticsGetWafLogAnalyticsMetrics(
        com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager
            .logAnalytics()
            .getWafLogAnalyticsMetricsWithResponse(
                "RG",
                "profile1",
                Arrays.asList(WafMetric.CLIENT_REQUEST_COUNT),
                OffsetDateTime.parse("2020-11-04T06:49:27.554Z"),
                OffsetDateTime.parse("2020-11-04T09:49:27.554Z"),
                WafGranularity.PT5M,
                Arrays.asList(WafAction.BLOCK, WafAction.LOG),
                null,
                null,
                Context.NONE);
    }
}
```

### LogAnalytics_GetWafLogAnalyticsRankings

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.cdn.generated.models.WafMetric;
import com.azure.resourcemanager.cdn.generated.models.WafRankingType;
import java.time.OffsetDateTime;
import java.util.Arrays;

/** Samples for LogAnalytics GetWafLogAnalyticsRankings. */
public final class LogAnalyticsGetWafLogAnalyticsRankingsSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/LogAnalytics_GetWafLogAnalyticsRankings.json
     */
    /**
     * Sample code: LogAnalytics_GetWafLogAnalyticsRankings.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void logAnalyticsGetWafLogAnalyticsRankings(
        com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager
            .logAnalytics()
            .getWafLogAnalyticsRankingsWithResponse(
                "RG",
                "profile1",
                Arrays.asList(WafMetric.CLIENT_REQUEST_COUNT),
                OffsetDateTime.parse("2020-11-04T06:49:27.554Z"),
                OffsetDateTime.parse("2020-11-04T09:49:27.554Z"),
                5,
                Arrays.asList(WafRankingType.RULE_ID),
                null,
                null,
                Context.NONE);
    }
}
```

### ManagedRuleSets_List

```java
import com.azure.core.util.Context;

/** Samples for ManagedRuleSets List. */
public final class ManagedRuleSetsListSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/WafListManagedRuleSets.json
     */
    /**
     * Sample code: List Policies in a Resource Group.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void listPoliciesInAResourceGroup(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.managedRuleSets().list(Context.NONE);
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void operationsList(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### OriginGroups_Create

```java
import com.azure.resourcemanager.cdn.generated.models.HealthProbeParameters;
import com.azure.resourcemanager.cdn.generated.models.HealthProbeRequestType;
import com.azure.resourcemanager.cdn.generated.models.ProbeProtocol;
import com.azure.resourcemanager.cdn.generated.models.ResourceReference;
import com.azure.resourcemanager.cdn.generated.models.ResponseBasedDetectedErrorTypes;
import com.azure.resourcemanager.cdn.generated.models.ResponseBasedOriginErrorDetectionParameters;
import java.util.Arrays;

/** Samples for OriginGroups Create. */
public final class OriginGroupsCreateSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/OriginGroups_Create.json
     */
    /**
     * Sample code: OriginGroups_Create.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void originGroupsCreate(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager
            .originGroups()
            .define("origingroup1")
            .withExistingEndpoint("RG", "profile1", "endpoint1")
            .withHealthProbeSettings(
                new HealthProbeParameters()
                    .withProbePath("/health.aspx")
                    .withProbeRequestType(HealthProbeRequestType.GET)
                    .withProbeProtocol(ProbeProtocol.HTTP)
                    .withProbeIntervalInSeconds(120))
            .withOrigins(
                Arrays
                    .asList(
                        new ResourceReference()
                            .withId(
                                "/subscriptions/subid/resourceGroups/RG/providers/Microsoft.Cdn/profiles/profile1/endpoints/endpoint1/origins/origin1")))
            .withResponseBasedOriginErrorDetectionSettings(
                new ResponseBasedOriginErrorDetectionParameters()
                    .withResponseBasedDetectedErrorTypes(ResponseBasedDetectedErrorTypes.TCP_ERRORS_ONLY)
                    .withResponseBasedFailoverThresholdPercentage(10))
            .create();
    }
}
```

### OriginGroups_Delete

```java
import com.azure.core.util.Context;

/** Samples for OriginGroups Delete. */
public final class OriginGroupsDeleteSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/OriginGroups_Delete.json
     */
    /**
     * Sample code: OriginGroups_Delete.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void originGroupsDelete(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.originGroups().delete("RG", "profile1", "endpoint1", "originGroup1", Context.NONE);
    }
}
```

### OriginGroups_Get

```java
import com.azure.core.util.Context;

/** Samples for OriginGroups Get. */
public final class OriginGroupsGetSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/OriginGroups_Get.json
     */
    /**
     * Sample code: OriginGroups_Get.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void originGroupsGet(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.originGroups().getWithResponse("RG", "profile1", "endpoint1", "originGroup1", Context.NONE);
    }
}
```

### OriginGroups_ListByEndpoint

```java
import com.azure.core.util.Context;

/** Samples for OriginGroups ListByEndpoint. */
public final class OriginGroupsListByEndpointSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/OriginGroups_ListByEndpoint.json
     */
    /**
     * Sample code: OriginsGroups_ListByEndpoint.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void originsGroupsListByEndpoint(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.originGroups().listByEndpoint("RG", "profile1", "endpoint1", Context.NONE);
    }
}
```

### OriginGroups_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.cdn.generated.models.HealthProbeParameters;
import com.azure.resourcemanager.cdn.generated.models.HealthProbeRequestType;
import com.azure.resourcemanager.cdn.generated.models.OriginGroup;
import com.azure.resourcemanager.cdn.generated.models.ProbeProtocol;
import com.azure.resourcemanager.cdn.generated.models.ResourceReference;
import java.util.Arrays;

/** Samples for OriginGroups Update. */
public final class OriginGroupsUpdateSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/OriginGroups_Update.json
     */
    /**
     * Sample code: OriginGroups_Update.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void originGroupsUpdate(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        OriginGroup resource =
            manager
                .originGroups()
                .getWithResponse("RG", "profile1", "endpoint1", "originGroup1", Context.NONE)
                .getValue();
        resource
            .update()
            .withHealthProbeSettings(
                new HealthProbeParameters()
                    .withProbePath("/health.aspx")
                    .withProbeRequestType(HealthProbeRequestType.GET)
                    .withProbeProtocol(ProbeProtocol.HTTP)
                    .withProbeIntervalInSeconds(120))
            .withOrigins(
                Arrays
                    .asList(
                        new ResourceReference()
                            .withId(
                                "/subscriptions/subid/resourceGroups/RG/providers/Microsoft.Cdn/profiles/profile1/endpoints/endpoint1/origins/origin2")))
            .apply();
    }
}
```

### Origins_Create

```java
/** Samples for Origins Create. */
public final class OriginsCreateSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/Origins_Create.json
     */
    /**
     * Sample code: Origins_Create.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void originsCreate(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager
            .origins()
            .define("www-someDomain-net")
            .withExistingEndpoint("RG", "profile1", "endpoint1")
            .withHostname("www.someDomain.net")
            .withHttpPort(80)
            .withHttpsPort(443)
            .withOriginHostHeader("www.someDomain.net")
            .withPriority(1)
            .withWeight(50)
            .withEnabled(true)
            .withPrivateLinkResourceId(
                "/subscriptions/subid/resourcegroups/rg1/providers/Microsoft.Network/privateLinkServices/pls1")
            .withPrivateLinkLocation("eastus")
            .withPrivateLinkApprovalMessage("Please approve the connection request for this Private Link")
            .create();
    }
}
```

### Origins_Delete

```java
import com.azure.core.util.Context;

/** Samples for Origins Delete. */
public final class OriginsDeleteSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/Origins_Delete.json
     */
    /**
     * Sample code: Origins_Delete.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void originsDelete(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.origins().delete("RG", "profile1", "endpoint1", "origin1", Context.NONE);
    }
}
```

### Origins_Get

```java
import com.azure.core.util.Context;

/** Samples for Origins Get. */
public final class OriginsGetSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/Origins_Get.json
     */
    /**
     * Sample code: Origins_Get.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void originsGet(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.origins().getWithResponse("RG", "profile1", "endpoint1", "www-someDomain-net", Context.NONE);
    }
}
```

### Origins_ListByEndpoint

```java
import com.azure.core.util.Context;

/** Samples for Origins ListByEndpoint. */
public final class OriginsListByEndpointSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/Origins_ListByEndpoint.json
     */
    /**
     * Sample code: Origins_ListByEndpoint.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void originsListByEndpoint(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.origins().listByEndpoint("RG", "profile1", "endpoint1", Context.NONE);
    }
}
```

### Origins_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.cdn.generated.models.Origin;

/** Samples for Origins Update. */
public final class OriginsUpdateSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/Origins_Update.json
     */
    /**
     * Sample code: Origins_Update.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void originsUpdate(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        Origin resource =
            manager
                .origins()
                .getWithResponse("RG", "profile1", "endpoint1", "www-someDomain-net", Context.NONE)
                .getValue();
        resource
            .update()
            .withHttpPort(42)
            .withHttpsPort(43)
            .withOriginHostHeader("www.someDomain2.net")
            .withPriority(1)
            .withWeight(50)
            .withEnabled(true)
            .withPrivateLinkAlias("APPSERVER.d84e61f0-0870-4d24-9746-7438fa0019d1.westus2.azure.privatelinkservice")
            .apply();
    }
}
```

### Policies_CreateOrUpdate

```java
import com.azure.resourcemanager.cdn.generated.models.ActionType;
import com.azure.resourcemanager.cdn.generated.models.CustomRule;
import com.azure.resourcemanager.cdn.generated.models.CustomRuleEnabledState;
import com.azure.resourcemanager.cdn.generated.models.CustomRuleList;
import com.azure.resourcemanager.cdn.generated.models.ManagedRuleEnabledState;
import com.azure.resourcemanager.cdn.generated.models.ManagedRuleGroupOverride;
import com.azure.resourcemanager.cdn.generated.models.ManagedRuleOverride;
import com.azure.resourcemanager.cdn.generated.models.ManagedRuleSet;
import com.azure.resourcemanager.cdn.generated.models.ManagedRuleSetList;
import com.azure.resourcemanager.cdn.generated.models.MatchCondition;
import com.azure.resourcemanager.cdn.generated.models.Operator;
import com.azure.resourcemanager.cdn.generated.models.PolicySettings;
import com.azure.resourcemanager.cdn.generated.models.PolicySettingsDefaultCustomBlockResponseStatusCode;
import com.azure.resourcemanager.cdn.generated.models.RateLimitRule;
import com.azure.resourcemanager.cdn.generated.models.RateLimitRuleList;
import com.azure.resourcemanager.cdn.generated.models.Sku;
import com.azure.resourcemanager.cdn.generated.models.SkuName;
import com.azure.resourcemanager.cdn.generated.models.TransformType;
import com.azure.resourcemanager.cdn.generated.models.WafMatchVariable;
import java.util.Arrays;

/** Samples for Policies CreateOrUpdate. */
public final class PoliciesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/WafPolicyCreateOrUpdate.json
     */
    /**
     * Sample code: Creates specific policy.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void createsSpecificPolicy(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager
            .policies()
            .define("MicrosoftCdnWafPolicy")
            .withRegion("WestUs")
            .withExistingResourceGroup("rg1")
            .withSku(new Sku().withName(SkuName.STANDARD_MICROSOFT))
            .withPolicySettings(
                new PolicySettings()
                    .withDefaultRedirectUrl("http://www.bing.com")
                    .withDefaultCustomBlockResponseStatusCode(
                        PolicySettingsDefaultCustomBlockResponseStatusCode.TWO_ZERO_ZERO)
                    .withDefaultCustomBlockResponseBody(
                        "PGh0bWw+CjxoZWFkZXI+PHRpdGxlPkhlbGxvPC90aXRsZT48L2hlYWRlcj4KPGJvZHk+CkhlbGxvIHdvcmxkCjwvYm9keT4KPC9odG1sPg=="))
            .withRateLimitRules(
                new RateLimitRuleList()
                    .withRules(
                        Arrays
                            .asList(
                                new RateLimitRule()
                                    .withName("RateLimitRule1")
                                    .withEnabledState(CustomRuleEnabledState.ENABLED)
                                    .withPriority(1)
                                    .withMatchConditions(
                                        Arrays
                                            .asList(
                                                new MatchCondition()
                                                    .withMatchVariable(WafMatchVariable.REMOTE_ADDR)
                                                    .withOperator(Operator.IPMATCH)
                                                    .withNegateCondition(false)
                                                    .withMatchValue(Arrays.asList("192.168.1.0/24", "10.0.0.0/24"))
                                                    .withTransforms(Arrays.asList())))
                                    .withAction(ActionType.BLOCK)
                                    .withRateLimitThreshold(1000)
                                    .withRateLimitDurationInMinutes(0))))
            .withCustomRules(
                new CustomRuleList()
                    .withRules(
                        Arrays
                            .asList(
                                new CustomRule()
                                    .withName("CustomRule1")
                                    .withEnabledState(CustomRuleEnabledState.ENABLED)
                                    .withPriority(2)
                                    .withMatchConditions(
                                        Arrays
                                            .asList(
                                                new MatchCondition()
                                                    .withMatchVariable(WafMatchVariable.REMOTE_ADDR)
                                                    .withOperator(Operator.GEO_MATCH)
                                                    .withNegateCondition(false)
                                                    .withMatchValue(Arrays.asList("CH"))
                                                    .withTransforms(Arrays.asList()),
                                                new MatchCondition()
                                                    .withMatchVariable(WafMatchVariable.REQUEST_HEADER)
                                                    .withSelector("UserAgent")
                                                    .withOperator(Operator.CONTAINS)
                                                    .withNegateCondition(false)
                                                    .withMatchValue(Arrays.asList("windows"))
                                                    .withTransforms(Arrays.asList()),
                                                new MatchCondition()
                                                    .withMatchVariable(WafMatchVariable.QUERY_STRING)
                                                    .withSelector("search")
                                                    .withOperator(Operator.CONTAINS)
                                                    .withNegateCondition(false)
                                                    .withMatchValue(Arrays.asList("<?php", "?>"))
                                                    .withTransforms(
                                                        Arrays
                                                            .asList(
                                                                TransformType.URL_DECODE, TransformType.LOWERCASE))))
                                    .withAction(ActionType.BLOCK))))
            .withManagedRules(
                new ManagedRuleSetList()
                    .withManagedRuleSets(
                        Arrays
                            .asList(
                                new ManagedRuleSet()
                                    .withRuleSetType("DefaultRuleSet")
                                    .withRuleSetVersion("preview-1.0")
                                    .withRuleGroupOverrides(
                                        Arrays
                                            .asList(
                                                new ManagedRuleGroupOverride()
                                                    .withRuleGroupName("Group1")
                                                    .withRules(
                                                        Arrays
                                                            .asList(
                                                                new ManagedRuleOverride()
                                                                    .withRuleId("GROUP1-0001")
                                                                    .withEnabledState(ManagedRuleEnabledState.ENABLED)
                                                                    .withAction(ActionType.REDIRECT),
                                                                new ManagedRuleOverride()
                                                                    .withRuleId("GROUP1-0002")
                                                                    .withEnabledState(
                                                                        ManagedRuleEnabledState.DISABLED))))))))
            .create();
    }
}
```

### Policies_Delete

```java
import com.azure.core.util.Context;

/** Samples for Policies Delete. */
public final class PoliciesDeleteSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/WafPolicyDelete.json
     */
    /**
     * Sample code: Delete protection policy.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void deleteProtectionPolicy(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.policies().deleteWithResponse("rg1", "Policy1", Context.NONE);
    }
}
```

### Policies_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Policies GetByResourceGroup. */
public final class PoliciesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/WafPolicyGet.json
     */
    /**
     * Sample code: Get Policy.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void getPolicy(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.policies().getByResourceGroupWithResponse("rg1", "MicrosoftCdnWafPolicy", Context.NONE);
    }
}
```

### Policies_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Policies ListByResourceGroup. */
public final class PoliciesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/WafListPolicies.json
     */
    /**
     * Sample code: List Policies in a Resource Group.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void listPoliciesInAResourceGroup(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.policies().listByResourceGroup("rg1", Context.NONE);
    }
}
```

### Policies_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.cdn.generated.models.CdnWebApplicationFirewallPolicy;
import java.util.HashMap;
import java.util.Map;

/** Samples for Policies Update. */
public final class PoliciesUpdateSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/WafPatchPolicy.json
     */
    /**
     * Sample code: Creates specific policy.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void createsSpecificPolicy(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        CdnWebApplicationFirewallPolicy resource =
            manager.policies().getByResourceGroupWithResponse("rg1", "MicrosoftCdnWafPolicy", Context.NONE).getValue();
        resource.update().withTags(mapOf("foo", "bar")).apply();
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

### Profiles_Create

```java
import com.azure.resourcemanager.cdn.generated.models.Sku;
import com.azure.resourcemanager.cdn.generated.models.SkuName;

/** Samples for Profiles Create. */
public final class ProfilesCreateSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/Profiles_Create.json
     */
    /**
     * Sample code: Profiles_Create.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void profilesCreate(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager
            .profiles()
            .define("profile1")
            .withRegion("global")
            .withExistingResourceGroup("RG")
            .withSku(new Sku().withName(SkuName.PREMIUM_AZURE_FRONT_DOOR))
            .create();
    }
}
```

### Profiles_Delete

```java
import com.azure.core.util.Context;

/** Samples for Profiles Delete. */
public final class ProfilesDeleteSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/Profiles_Delete.json
     */
    /**
     * Sample code: Profiles_Delete.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void profilesDelete(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.profiles().delete("RG", "profile1", Context.NONE);
    }
}
```

### Profiles_GenerateSsoUri

```java
import com.azure.core.util.Context;

/** Samples for Profiles GenerateSsoUri. */
public final class ProfilesGenerateSsoUriSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/Profiles_GenerateSsoUri.json
     */
    /**
     * Sample code: Profiles_GenerateSsoUri.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void profilesGenerateSsoUri(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.profiles().generateSsoUriWithResponse("RG", "profile1", Context.NONE);
    }
}
```

### Profiles_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Profiles GetByResourceGroup. */
public final class ProfilesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/Profiles_Get.json
     */
    /**
     * Sample code: Profiles_Get.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void profilesGet(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.profiles().getByResourceGroupWithResponse("RG", "profile1", Context.NONE);
    }
}
```

### Profiles_List

```java
import com.azure.core.util.Context;

/** Samples for Profiles List. */
public final class ProfilesListSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/Profiles_List.json
     */
    /**
     * Sample code: Profiles_List.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void profilesList(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.profiles().list(Context.NONE);
    }
}
```

### Profiles_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Profiles ListByResourceGroup. */
public final class ProfilesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/Profiles_ListByResourceGroup.json
     */
    /**
     * Sample code: Profiles_ListByResourceGroup.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void profilesListByResourceGroup(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.profiles().listByResourceGroup("RG", Context.NONE);
    }
}
```

### Profiles_ListResourceUsage

```java
import com.azure.core.util.Context;

/** Samples for Profiles ListResourceUsage. */
public final class ProfilesListResourceUsageSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/Profiles_ListResourceUsage.json
     */
    /**
     * Sample code: Profiles_ListResourceUsage.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void profilesListResourceUsage(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.profiles().listResourceUsage("RG", "profile1", Context.NONE);
    }
}
```

### Profiles_ListSupportedOptimizationTypes

```java
import com.azure.core.util.Context;

/** Samples for Profiles ListSupportedOptimizationTypes. */
public final class ProfilesListSupportedOptimizationTypesSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/Profiles_ListSupportedOptimizationTypes.json
     */
    /**
     * Sample code: Profiles_ListSupportedOptimizationTypes.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void profilesListSupportedOptimizationTypes(
        com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.profiles().listSupportedOptimizationTypesWithResponse("RG", "profile1", Context.NONE);
    }
}
```

### Profiles_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.cdn.generated.models.Profile;
import java.util.HashMap;
import java.util.Map;

/** Samples for Profiles Update. */
public final class ProfilesUpdateSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/Profiles_Update.json
     */
    /**
     * Sample code: Profiles_Update.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void profilesUpdate(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        Profile resource = manager.profiles().getByResourceGroupWithResponse("RG", "profile1", Context.NONE).getValue();
        resource.update().withTags(mapOf("additionalProperties", "Tag1")).apply();
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

### ResourceProvider_CheckEndpointNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.cdn.generated.models.AutoGeneratedDomainNameLabelScope;
import com.azure.resourcemanager.cdn.generated.models.CheckEndpointNameAvailabilityInput;
import com.azure.resourcemanager.cdn.generated.models.ResourceType;

/** Samples for ResourceProvider CheckEndpointNameAvailability. */
public final class ResourceProviderCheckEndpointNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/CheckEndpointNameAvailability.json
     */
    /**
     * Sample code: CheckEndpointNameAvailability.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void checkEndpointNameAvailability(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager
            .resourceProviders()
            .checkEndpointNameAvailabilityWithResponse(
                "myResourceGroup",
                new CheckEndpointNameAvailabilityInput()
                    .withName("sampleName")
                    .withType(ResourceType.MICROSOFT_CDN_PROFILES_AFD_ENDPOINTS)
                    .withAutoGeneratedDomainNameLabelScope(AutoGeneratedDomainNameLabelScope.TENANT_REUSE),
                Context.NONE);
    }
}
```

### ResourceProvider_CheckNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.cdn.generated.models.CheckNameAvailabilityInput;
import com.azure.resourcemanager.cdn.generated.models.ResourceType;

/** Samples for ResourceProvider CheckNameAvailability. */
public final class ResourceProviderCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/CheckNameAvailability.json
     */
    /**
     * Sample code: CheckNameAvailability.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void checkNameAvailability(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager
            .resourceProviders()
            .checkNameAvailabilityWithResponse(
                new CheckNameAvailabilityInput()
                    .withName("sampleName")
                    .withType(ResourceType.MICROSOFT_CDN_PROFILES_ENDPOINTS),
                Context.NONE);
    }
}
```

### ResourceProvider_CheckNameAvailabilityWithSubscription

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.cdn.generated.models.CheckNameAvailabilityInput;
import com.azure.resourcemanager.cdn.generated.models.ResourceType;

/** Samples for ResourceProvider CheckNameAvailabilityWithSubscription. */
public final class ResourceProviderCheckNameAvailabilityWithSubscriptionSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/CheckNameAvailabilityWithSubscription.json
     */
    /**
     * Sample code: CheckNameAvailabilityWithSubscription.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void checkNameAvailabilityWithSubscription(
        com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager
            .resourceProviders()
            .checkNameAvailabilityWithSubscriptionWithResponse(
                new CheckNameAvailabilityInput()
                    .withName("sampleName")
                    .withType(ResourceType.MICROSOFT_CDN_PROFILES_ENDPOINTS),
                Context.NONE);
    }
}
```

### ResourceProvider_ValidateProbe

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.cdn.generated.models.ValidateProbeInput;

/** Samples for ResourceProvider ValidateProbe. */
public final class ResourceProviderValidateProbeSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/ValidateProbe.json
     */
    /**
     * Sample code: ValidateProbe.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void validateProbe(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager
            .resourceProviders()
            .validateProbeWithResponse(
                new ValidateProbeInput().withProbeUrl("https://www.bing.com/image"), Context.NONE);
    }
}
```

### ResourceUsage_List

```java
import com.azure.core.util.Context;

/** Samples for ResourceUsage List. */
public final class ResourceUsageListSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/ResourceUsage_List.json
     */
    /**
     * Sample code: ResourceUsage_List.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void resourceUsageList(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.resourceUsages().list(Context.NONE);
    }
}
```

### Routes_Create

```java
import com.azure.resourcemanager.cdn.generated.models.ActivatedResourceReference;
import com.azure.resourcemanager.cdn.generated.models.AfdEndpointProtocols;
import com.azure.resourcemanager.cdn.generated.models.AfdQueryStringCachingBehavior;
import com.azure.resourcemanager.cdn.generated.models.AfdRouteCacheConfiguration;
import com.azure.resourcemanager.cdn.generated.models.CompressionSettings;
import com.azure.resourcemanager.cdn.generated.models.EnabledState;
import com.azure.resourcemanager.cdn.generated.models.ForwardingProtocol;
import com.azure.resourcemanager.cdn.generated.models.HttpsRedirect;
import com.azure.resourcemanager.cdn.generated.models.LinkToDefaultDomain;
import com.azure.resourcemanager.cdn.generated.models.ResourceReference;
import java.util.Arrays;

/** Samples for Routes Create. */
public final class RoutesCreateSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/Routes_Create.json
     */
    /**
     * Sample code: Routes_Create.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void routesCreate(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager
            .routes()
            .define("route1")
            .withExistingAfdEndpoint("RG", "profile1", "endpoint1")
            .withCustomDomains(
                Arrays
                    .asList(
                        new ActivatedResourceReference()
                            .withId(
                                "/subscriptions/subid/resourceGroups/RG/providers/Microsoft.Cdn/profiles/profile1/customDomains/domain1")))
            .withOriginGroup(
                new ResourceReference()
                    .withId(
                        "/subscriptions/subid/resourceGroups/RG/providers/Microsoft.Cdn/profiles/profile1/originGroups/originGroup1"))
            .withRuleSets(
                Arrays
                    .asList(
                        new ResourceReference()
                            .withId(
                                "/subscriptions/subid/resourceGroups/RG/providers/Microsoft.Cdn/profiles/profile1/ruleSets/ruleSet1")))
            .withSupportedProtocols(Arrays.asList(AfdEndpointProtocols.HTTPS, AfdEndpointProtocols.HTTP))
            .withPatternsToMatch(Arrays.asList("/*"))
            .withCacheConfiguration(
                new AfdRouteCacheConfiguration()
                    .withQueryStringCachingBehavior(AfdQueryStringCachingBehavior.IGNORE_SPECIFIED_QUERY_STRINGS)
                    .withQueryParameters("querystring=test")
                    .withCompressionSettings(
                        new CompressionSettings()
                            .withContentTypesToCompress(Arrays.asList("text/html", "application/octet-stream"))
                            .withIsCompressionEnabled(true)))
            .withForwardingProtocol(ForwardingProtocol.MATCH_REQUEST)
            .withLinkToDefaultDomain(LinkToDefaultDomain.ENABLED)
            .withHttpsRedirect(HttpsRedirect.ENABLED)
            .withEnabledState(EnabledState.ENABLED)
            .create();
    }
}
```

### Routes_Delete

```java
import com.azure.core.util.Context;

/** Samples for Routes Delete. */
public final class RoutesDeleteSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/Routes_Delete.json
     */
    /**
     * Sample code: Routes_Delete.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void routesDelete(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.routes().delete("RG", "profile1", "endpoint1", "route1", Context.NONE);
    }
}
```

### Routes_Get

```java
import com.azure.core.util.Context;

/** Samples for Routes Get. */
public final class RoutesGetSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/Routes_Get.json
     */
    /**
     * Sample code: Routes_Get.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void routesGet(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.routes().getWithResponse("RG", "profile1", "endpoint1", "route1", Context.NONE);
    }
}
```

### Routes_ListByEndpoint

```java
import com.azure.core.util.Context;

/** Samples for Routes ListByEndpoint. */
public final class RoutesListByEndpointSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/Routes_ListByEndpoint.json
     */
    /**
     * Sample code: Routes_ListByEndpoint.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void routesListByEndpoint(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.routes().listByEndpoint("RG", "profile1", "endpoint1", Context.NONE);
    }
}
```

### Routes_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.cdn.generated.models.ActivatedResourceReference;
import com.azure.resourcemanager.cdn.generated.models.AfdEndpointProtocols;
import com.azure.resourcemanager.cdn.generated.models.AfdQueryStringCachingBehavior;
import com.azure.resourcemanager.cdn.generated.models.AfdRouteCacheConfiguration;
import com.azure.resourcemanager.cdn.generated.models.CompressionSettings;
import com.azure.resourcemanager.cdn.generated.models.EnabledState;
import com.azure.resourcemanager.cdn.generated.models.ForwardingProtocol;
import com.azure.resourcemanager.cdn.generated.models.HttpsRedirect;
import com.azure.resourcemanager.cdn.generated.models.LinkToDefaultDomain;
import com.azure.resourcemanager.cdn.generated.models.ResourceReference;
import com.azure.resourcemanager.cdn.generated.models.Route;
import java.util.Arrays;

/** Samples for Routes Update. */
public final class RoutesUpdateSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/Routes_Update.json
     */
    /**
     * Sample code: Routes_Update.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void routesUpdate(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        Route resource =
            manager.routes().getWithResponse("RG", "profile1", "endpoint1", "route1", Context.NONE).getValue();
        resource
            .update()
            .withCustomDomains(
                Arrays
                    .asList(
                        new ActivatedResourceReference()
                            .withId(
                                "/subscriptions/subid/resourceGroups/RG/providers/Microsoft.Cdn/profiles/profile1/customDomains/domain1")))
            .withOriginGroup(
                new ResourceReference()
                    .withId(
                        "/subscriptions/subid/resourceGroups/RG/providers/Microsoft.Cdn/profiles/profile1/originGroups/originGroup1"))
            .withRuleSets(
                Arrays
                    .asList(
                        new ResourceReference()
                            .withId(
                                "/subscriptions/subid/resourceGroups/RG/providers/Microsoft.Cdn/profiles/profile1/ruleSets/ruleSet1")))
            .withSupportedProtocols(Arrays.asList(AfdEndpointProtocols.HTTPS, AfdEndpointProtocols.HTTP))
            .withPatternsToMatch(Arrays.asList("/*"))
            .withCacheConfiguration(
                new AfdRouteCacheConfiguration()
                    .withQueryStringCachingBehavior(AfdQueryStringCachingBehavior.IGNORE_QUERY_STRING)
                    .withCompressionSettings(
                        new CompressionSettings()
                            .withContentTypesToCompress(Arrays.asList("text/html", "application/octet-stream"))
                            .withIsCompressionEnabled(true)))
            .withForwardingProtocol(ForwardingProtocol.MATCH_REQUEST)
            .withLinkToDefaultDomain(LinkToDefaultDomain.ENABLED)
            .withHttpsRedirect(HttpsRedirect.ENABLED)
            .withEnabledState(EnabledState.ENABLED)
            .apply();
    }
}
```

### RuleSets_Create

```java
import com.azure.core.util.Context;

/** Samples for RuleSets Create. */
public final class RuleSetsCreateSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/RuleSets_Create.json
     */
    /**
     * Sample code: RuleSets_Create.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void ruleSetsCreate(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.ruleSets().createWithResponse("RG", "profile1", "ruleSet1", Context.NONE);
    }
}
```

### RuleSets_Delete

```java
import com.azure.core.util.Context;

/** Samples for RuleSets Delete. */
public final class RuleSetsDeleteSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/RuleSets_Delete.json
     */
    /**
     * Sample code: RuleSets_Delete.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void ruleSetsDelete(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.ruleSets().delete("RG", "profile1", "ruleSet1", Context.NONE);
    }
}
```

### RuleSets_Get

```java
import com.azure.core.util.Context;

/** Samples for RuleSets Get. */
public final class RuleSetsGetSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/RuleSets_Get.json
     */
    /**
     * Sample code: RuleSets_Get.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void ruleSetsGet(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.ruleSets().getWithResponse("RG", "profile1", "ruleSet1", Context.NONE);
    }
}
```

### RuleSets_ListByProfile

```java
import com.azure.core.util.Context;

/** Samples for RuleSets ListByProfile. */
public final class RuleSetsListByProfileSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/RuleSets_ListByProfile.json
     */
    /**
     * Sample code: RuleSets_ListByProfile.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void ruleSetsListByProfile(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.ruleSets().listByProfile("RG", "profile1", Context.NONE);
    }
}
```

### RuleSets_ListResourceUsage

```java
import com.azure.core.util.Context;

/** Samples for RuleSets ListResourceUsage. */
public final class RuleSetsListResourceUsageSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/RuleSets_ListResourceUsage.json
     */
    /**
     * Sample code: RuleSets_ListResourceUsage.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void ruleSetsListResourceUsage(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.ruleSets().listResourceUsage("RG", "profile1", "ruleSet1", Context.NONE);
    }
}
```

### Rules_Create

```java
import com.azure.resourcemanager.cdn.generated.models.DeliveryRuleRequestMethodCondition;
import com.azure.resourcemanager.cdn.generated.models.DeliveryRuleResponseHeaderAction;
import com.azure.resourcemanager.cdn.generated.models.HeaderAction;
import com.azure.resourcemanager.cdn.generated.models.HeaderActionParameters;
import com.azure.resourcemanager.cdn.generated.models.RequestMethodMatchConditionParameters;
import com.azure.resourcemanager.cdn.generated.models.RequestMethodMatchConditionParametersMatchValuesItem;
import com.azure.resourcemanager.cdn.generated.models.RequestMethodOperator;
import java.util.Arrays;

/** Samples for Rules Create. */
public final class RulesCreateSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/Rules_Create.json
     */
    /**
     * Sample code: Rules_Create.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void rulesCreate(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager
            .rules()
            .define("rule1")
            .withExistingRuleSet("RG", "profile1", "ruleSet1")
            .withOrder(1)
            .withConditions(
                Arrays
                    .asList(
                        new DeliveryRuleRequestMethodCondition()
                            .withParameters(
                                new RequestMethodMatchConditionParameters()
                                    .withOperator(RequestMethodOperator.EQUAL)
                                    .withNegateCondition(false)
                                    .withMatchValues(
                                        Arrays.asList(RequestMethodMatchConditionParametersMatchValuesItem.GET)))))
            .withActions(
                Arrays
                    .asList(
                        new DeliveryRuleResponseHeaderAction()
                            .withParameters(
                                new HeaderActionParameters()
                                    .withHeaderAction(HeaderAction.OVERWRITE)
                                    .withHeaderName("X-CDN")
                                    .withValue("MSFT"))))
            .create();
    }
}
```

### Rules_Delete

```java
import com.azure.core.util.Context;

/** Samples for Rules Delete. */
public final class RulesDeleteSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/Rules_Delete.json
     */
    /**
     * Sample code: Rules_Delete.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void rulesDelete(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.rules().delete("RG", "profile1", "ruleSet1", "rule1", Context.NONE);
    }
}
```

### Rules_Get

```java
import com.azure.core.util.Context;

/** Samples for Rules Get. */
public final class RulesGetSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/Rules_Get.json
     */
    /**
     * Sample code: Rules_Get.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void rulesGet(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.rules().getWithResponse("RG", "profile1", "ruleSet1", "rule1", Context.NONE);
    }
}
```

### Rules_ListByRuleSet

```java
import com.azure.core.util.Context;

/** Samples for Rules ListByRuleSet. */
public final class RulesListByRuleSetSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/Rules_ListByRuleSet.json
     */
    /**
     * Sample code: Rules_ListByRuleSet.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void rulesListByRuleSet(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.rules().listByRuleSet("RG", "profile1", "ruleSet1", Context.NONE);
    }
}
```

### Rules_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.cdn.generated.models.DeliveryRuleResponseHeaderAction;
import com.azure.resourcemanager.cdn.generated.models.HeaderAction;
import com.azure.resourcemanager.cdn.generated.models.HeaderActionParameters;
import com.azure.resourcemanager.cdn.generated.models.Rule;
import java.util.Arrays;

/** Samples for Rules Update. */
public final class RulesUpdateSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/Rules_Update.json
     */
    /**
     * Sample code: Rules_Update.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void rulesUpdate(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        Rule resource = manager.rules().getWithResponse("RG", "profile1", "ruleSet1", "rule1", Context.NONE).getValue();
        resource
            .update()
            .withOrder(1)
            .withActions(
                Arrays
                    .asList(
                        new DeliveryRuleResponseHeaderAction()
                            .withParameters(
                                new HeaderActionParameters()
                                    .withHeaderAction(HeaderAction.OVERWRITE)
                                    .withHeaderName("X-CDN")
                                    .withValue("MSFT"))))
            .apply();
    }
}
```

### Secrets_Create

```java
import com.azure.resourcemanager.cdn.generated.models.CustomerCertificateParameters;
import com.azure.resourcemanager.cdn.generated.models.ResourceReference;

/** Samples for Secrets Create. */
public final class SecretsCreateSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/Secrets_Create.json
     */
    /**
     * Sample code: Secrets_Create.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void secretsCreate(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager
            .secrets()
            .define("secret1")
            .withExistingProfile("RG", "profile1")
            .withParameters(
                new CustomerCertificateParameters()
                    .withSecretSource(
                        new ResourceReference()
                            .withId(
                                "/subscriptions/subid/resourcegroups/RG/providers/Microsoft.KeyVault/vault/kvName/secrets/certificatename"))
                    .withSecretVersion("abcdef1234578900abcdef1234567890")
                    .withUseLatestVersion(false))
            .create();
    }
}
```

### Secrets_Delete

```java
import com.azure.core.util.Context;

/** Samples for Secrets Delete. */
public final class SecretsDeleteSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/Secrets_Delete.json
     */
    /**
     * Sample code: Secrets_Delete.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void secretsDelete(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.secrets().delete("RG", "profile1", "secret1", Context.NONE);
    }
}
```

### Secrets_Get

```java
import com.azure.core.util.Context;

/** Samples for Secrets Get. */
public final class SecretsGetSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/Secrets_Get.json
     */
    /**
     * Sample code: Secrets_Get.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void secretsGet(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.secrets().getWithResponse("RG", "profile1", "secret1", Context.NONE);
    }
}
```

### Secrets_ListByProfile

```java
import com.azure.core.util.Context;

/** Samples for Secrets ListByProfile. */
public final class SecretsListByProfileSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/Secrets_ListByProfile.json
     */
    /**
     * Sample code: Secrets_ListByProfile.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void secretsListByProfile(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.secrets().listByProfile("RG", "profile1", Context.NONE);
    }
}
```

### SecurityPolicies_Create

```java
import com.azure.resourcemanager.cdn.generated.models.ActivatedResourceReference;
import com.azure.resourcemanager.cdn.generated.models.ResourceReference;
import com.azure.resourcemanager.cdn.generated.models.SecurityPolicyWebApplicationFirewallAssociation;
import com.azure.resourcemanager.cdn.generated.models.SecurityPolicyWebApplicationFirewallParameters;
import java.util.Arrays;

/** Samples for SecurityPolicies Create. */
public final class SecurityPoliciesCreateSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/SecurityPolicies_Create.json
     */
    /**
     * Sample code: SecurityPolicies_Create.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void securityPoliciesCreate(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager
            .securityPolicies()
            .define("securityPolicy1")
            .withExistingProfile("RG", "profile1")
            .withParameters(
                new SecurityPolicyWebApplicationFirewallParameters()
                    .withWafPolicy(
                        new ResourceReference()
                            .withId(
                                "/subscriptions/subid/resourcegroups/RG/providers/Microsoft.Network/frontdoorwebapplicationfirewallpolicies/wafTest"))
                    .withAssociations(
                        Arrays
                            .asList(
                                new SecurityPolicyWebApplicationFirewallAssociation()
                                    .withDomains(
                                        Arrays
                                            .asList(
                                                new ActivatedResourceReference()
                                                    .withId(
                                                        "/subscriptions/subid/resourcegroups/RG/providers/Microsoft.Cdn/profiles/profile1/customdomains/testdomain1"),
                                                new ActivatedResourceReference()
                                                    .withId(
                                                        "/subscriptions/subid/resourcegroups/RG/providers/Microsoft.Cdn/profiles/profile1/customdomains/testdomain2")))
                                    .withPatternsToMatch(Arrays.asList("/*")))))
            .create();
    }
}
```

### SecurityPolicies_Delete

```java
import com.azure.core.util.Context;

/** Samples for SecurityPolicies Delete. */
public final class SecurityPoliciesDeleteSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/SecurityPolicies_Delete.json
     */
    /**
     * Sample code: SecurityPolicies_Delete.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void securityPoliciesDelete(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.securityPolicies().delete("RG", "profile1", "securityPolicy1", Context.NONE);
    }
}
```

### SecurityPolicies_Get

```java
import com.azure.core.util.Context;

/** Samples for SecurityPolicies Get. */
public final class SecurityPoliciesGetSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/SecurityPolicies_Get.json
     */
    /**
     * Sample code: SecurityPolicies_Get.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void securityPoliciesGet(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.securityPolicies().getWithResponse("RG", "profile1", "securityPolicy1", Context.NONE);
    }
}
```

### SecurityPolicies_ListByProfile

```java
import com.azure.core.util.Context;

/** Samples for SecurityPolicies ListByProfile. */
public final class SecurityPoliciesListByProfileSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/SecurityPolicies_ListByProfile.json
     */
    /**
     * Sample code: SecurityPolicies_ListByProfile.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void securityPoliciesListByProfile(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager.securityPolicies().listByProfile("RG", "profile1", Context.NONE);
    }
}
```

### SecurityPolicies_Patch

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.cdn.generated.models.ActivatedResourceReference;
import com.azure.resourcemanager.cdn.generated.models.ResourceReference;
import com.azure.resourcemanager.cdn.generated.models.SecurityPolicy;
import com.azure.resourcemanager.cdn.generated.models.SecurityPolicyWebApplicationFirewallAssociation;
import com.azure.resourcemanager.cdn.generated.models.SecurityPolicyWebApplicationFirewallParameters;
import java.util.Arrays;

/** Samples for SecurityPolicies Patch. */
public final class SecurityPoliciesPatchSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/SecurityPolicies_Patch.json
     */
    /**
     * Sample code: SecurityPolicies_Patch.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void securityPoliciesPatch(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        SecurityPolicy resource =
            manager.securityPolicies().getWithResponse("RG", "profile1", "securityPolicy1", Context.NONE).getValue();
        resource
            .update()
            .withParameters(
                new SecurityPolicyWebApplicationFirewallParameters()
                    .withWafPolicy(
                        new ResourceReference()
                            .withId(
                                "/subscriptions/subid/resourcegroups/RG/providers/Microsoft.Network/frontdoorwebapplicationfirewallpolicies/wafTest"))
                    .withAssociations(
                        Arrays
                            .asList(
                                new SecurityPolicyWebApplicationFirewallAssociation()
                                    .withDomains(
                                        Arrays
                                            .asList(
                                                new ActivatedResourceReference()
                                                    .withId(
                                                        "/subscriptions/subid/resourcegroups/RG/providers/Microsoft.Cdn/profiles/profile1/customdomains/testdomain1"),
                                                new ActivatedResourceReference()
                                                    .withId(
                                                        "/subscriptions/subid/resourcegroups/RG/providers/Microsoft.Cdn/profiles/profile1/customdomains/testdomain2")))
                                    .withPatternsToMatch(Arrays.asList("/*")))))
            .apply();
    }
}
```

### Validate_Secret

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.cdn.generated.models.ResourceReference;
import com.azure.resourcemanager.cdn.generated.models.SecretType;
import com.azure.resourcemanager.cdn.generated.models.ValidateSecretInput;

/** Samples for Validate Secret. */
public final class ValidateSecretSamples {
    /*
     * x-ms-original-file: specification/cdn/resource-manager/Microsoft.Cdn/stable/2021-06-01/examples/Validate_Secret.json
     */
    /**
     * Sample code: Validate_Secret.
     *
     * @param manager Entry point to CdnManager.
     */
    public static void validateSecret(com.azure.resourcemanager.cdn.generated.CdnManager manager) {
        manager
            .validates()
            .secretWithResponse(
                new ValidateSecretInput()
                    .withSecretType(SecretType.CUSTOMER_CERTIFICATE)
                    .withSecretSource(
                        new ResourceReference()
                            .withId(
                                "/subscriptions/subid/resourcegroups/RG/providers/Microsoft.KeyVault/vault/kvName/certificate/certName")),
                Context.NONE);
    }
}
```

