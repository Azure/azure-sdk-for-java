# Code snippets and samples


## Endpoints

- [CreateOrUpdate](#endpoints_createorupdate)
- [Delete](#endpoints_delete)
- [Get](#endpoints_get)
- [ListByParent](#endpoints_listbyparent)
- [Update](#endpoints_update)

## HealthPolicies

- [CreateOrUpdate](#healthpolicies_createorupdate)
- [Delete](#healthpolicies_delete)
- [Get](#healthpolicies_get)
- [ListByParent](#healthpolicies_listbyparent)

## Operations

- [List](#operations_list)

## ProfileProbingGateways

- [CreateOrUpdate](#profileprobinggateways_createorupdate)
- [Delete](#profileprobinggateways_delete)
- [Get](#profileprobinggateways_get)
- [ListByParent](#profileprobinggateways_listbyparent)
- [Update](#profileprobinggateways_update)

## Profiles

- [CreateOrUpdate](#profiles_createorupdate)
- [Delete](#profiles_delete)
- [GetByResourceGroup](#profiles_getbyresourcegroup)
- [List](#profiles_list)
- [ListByResourceGroup](#profiles_listbyresourcegroup)
- [Update](#profiles_update)

## Sites

- [CreateOrUpdate](#sites_createorupdate)
- [Delete](#sites_delete)
- [Get](#sites_get)
- [ListByParent](#sites_listbyparent)
- [Update](#sites_update)

## TopologyMaps

- [CreateOrUpdate](#topologymaps_createorupdate)
- [Delete](#topologymaps_delete)
- [GetByResourceGroup](#topologymaps_getbyresourcegroup)
- [List](#topologymaps_list)
- [ListByResourceGroup](#topologymaps_listbyresourcegroup)
- [Update](#topologymaps_update)
### Endpoints_CreateOrUpdate

```java
import com.azure.resourcemanager.privatetrafficmanager.models.AdministrativeStatus;
import com.azure.resourcemanager.privatetrafficmanager.models.AlwaysServe;
import com.azure.resourcemanager.privatetrafficmanager.models.EndpointProperties;
import com.azure.resourcemanager.privatetrafficmanager.models.EndpointsKind;

/**
 * Samples for Endpoints CreateOrUpdate.
 */
public final class EndpointsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-02-09-preview/Endpoints_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: Endpoints_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to PrivateTrafficManagerManager.
     */
    public static void endpointsCreateOrUpdateMaximumSet(
        com.azure.resourcemanager.privatetrafficmanager.PrivateTrafficManagerManager manager) {
        manager.endpoints()
            .define("myEndpoint")
            .withExistingPrivateTrafficManagerProfile("rgprivateTrafficManager", "myProfile")
            .withProperties(new EndpointProperties().withTarget("10.0.0.1")
                .withMonitoringTarget("10.0.0.1")
                .withEndpointStatus(AdministrativeStatus.ENABLED)
                .withKind(EndpointsKind.ENDPOINT)
                .withWeight(100L)
                .withPriority(10L)
                .withAlwaysServe(AlwaysServe.ENABLED)
                .withHealthPolicyId(
                    "/subscriptions/10B6D88D-ADF4-4281-B3D0-B5A6702DEEDA/resourceGroups/rgprivateTrafficManager/providers/Microsoft.Network/privateTrafficManagerProfiles/myProfile/healthPolicies/myHealthPolicy"))
            .create();
    }
}
```

### Endpoints_Delete

```java
/**
 * Samples for Endpoints Delete.
 */
public final class EndpointsDeleteSamples {
    /*
     * x-ms-original-file: 2026-02-09-preview/Endpoints_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Endpoints_Delete_MaximumSet.
     * 
     * @param manager Entry point to PrivateTrafficManagerManager.
     */
    public static void endpointsDeleteMaximumSet(
        com.azure.resourcemanager.privatetrafficmanager.PrivateTrafficManagerManager manager) {
        manager.endpoints()
            .delete("rgprivateTrafficManager", "myProfile", "myEndpoint", com.azure.core.util.Context.NONE);
    }
}
```

### Endpoints_Get

```java
/**
 * Samples for Endpoints Get.
 */
public final class EndpointsGetSamples {
    /*
     * x-ms-original-file: 2026-02-09-preview/Endpoints_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Endpoints_Get_MaximumSet.
     * 
     * @param manager Entry point to PrivateTrafficManagerManager.
     */
    public static void
        endpointsGetMaximumSet(com.azure.resourcemanager.privatetrafficmanager.PrivateTrafficManagerManager manager) {
        manager.endpoints()
            .getWithResponse("rgprivateTrafficManager", "myProfile", "myEndpoint", com.azure.core.util.Context.NONE);
    }
}
```

### Endpoints_ListByParent

```java
/**
 * Samples for Endpoints ListByParent.
 */
public final class EndpointsListByParentSamples {
    /*
     * x-ms-original-file: 2026-02-09-preview/Endpoints_ListByParent_MaximumSet_Gen.json
     */
    /**
     * Sample code: Endpoints_ListByParent_MaximumSet.
     * 
     * @param manager Entry point to PrivateTrafficManagerManager.
     */
    public static void endpointsListByParentMaximumSet(
        com.azure.resourcemanager.privatetrafficmanager.PrivateTrafficManagerManager manager) {
        manager.endpoints().listByParent("rgprivateTrafficManager", "myProfile", com.azure.core.util.Context.NONE);
    }
}
```

### Endpoints_Update

```java
import com.azure.resourcemanager.privatetrafficmanager.models.AdministrativeStatus;
import com.azure.resourcemanager.privatetrafficmanager.models.AlwaysServe;
import com.azure.resourcemanager.privatetrafficmanager.models.Endpoint;
import com.azure.resourcemanager.privatetrafficmanager.models.EndpointUpdateProperties;

/**
 * Samples for Endpoints Update.
 */
public final class EndpointsUpdateSamples {
    /*
     * x-ms-original-file: 2026-02-09-preview/Endpoints_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: Endpoints_Update_MaximumSet.
     * 
     * @param manager Entry point to PrivateTrafficManagerManager.
     */
    public static void endpointsUpdateMaximumSet(
        com.azure.resourcemanager.privatetrafficmanager.PrivateTrafficManagerManager manager) {
        Endpoint resource = manager.endpoints()
            .getWithResponse("rgprivateTrafficManager", "myProfile", "myEndpoint", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new EndpointUpdateProperties().withTarget("10.0.0.2")
                .withMonitoringTarget("10.0.0.2")
                .withEndpointStatus(AdministrativeStatus.ENABLED)
                .withWeight(150L)
                .withPriority(5L)
                .withAlwaysServe(AlwaysServe.DISABLED)
                .withHealthPolicyId(
                    "/subscriptions/10B6D88D-ADF4-4281-B3D0-B5A6702DEEDA/resourceGroups/rgprivateTrafficManager/providers/Microsoft.Network/privateTrafficManagerProfiles/myProfile/healthPolicies/myHealthPolicy"))
            .apply();
    }
}
```

### HealthPolicies_CreateOrUpdate

```java
import com.azure.resourcemanager.privatetrafficmanager.models.HealthPolicyProperties;
import com.azure.resourcemanager.privatetrafficmanager.models.ProbeConfig;
import com.azure.resourcemanager.privatetrafficmanager.models.ProbeHealthPolicy;
import com.azure.resourcemanager.privatetrafficmanager.models.Protocol;

/**
 * Samples for HealthPolicies CreateOrUpdate.
 */
public final class HealthPoliciesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-02-09-preview/HealthPolicies_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: HealthPolicies_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to PrivateTrafficManagerManager.
     */
    public static void healthPoliciesCreateOrUpdateMaximumSet(
        com.azure.resourcemanager.privatetrafficmanager.PrivateTrafficManagerManager manager) {
        manager.healthPolicies()
            .createOrUpdate("rgprivateTrafficManager", "myProfile", "myHealthPolicy",
                new ProbeHealthPolicy().withProperties(
                    new HealthPolicyProperties().withProbeConfig(new ProbeConfig().withProtocol(Protocol.HTTPS)
                        .withPort(443L)
                        .withPath("/health")
                        .withIntervalInSeconds(30L)
                        .withTimeoutInSeconds(10L)
                        .withToleratedNumberOfFailures(3L))),
                com.azure.core.util.Context.NONE);
    }
}
```

### HealthPolicies_Delete

```java
/**
 * Samples for HealthPolicies Delete.
 */
public final class HealthPoliciesDeleteSamples {
    /*
     * x-ms-original-file: 2026-02-09-preview/HealthPolicies_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: HealthPolicies_Delete_MaximumSet.
     * 
     * @param manager Entry point to PrivateTrafficManagerManager.
     */
    public static void healthPoliciesDeleteMaximumSet(
        com.azure.resourcemanager.privatetrafficmanager.PrivateTrafficManagerManager manager) {
        manager.healthPolicies()
            .delete("rgprivateTrafficManager", "myProfile", "myHealthPolicy", com.azure.core.util.Context.NONE);
    }
}
```

### HealthPolicies_Get

```java
/**
 * Samples for HealthPolicies Get.
 */
public final class HealthPoliciesGetSamples {
    /*
     * x-ms-original-file: 2026-02-09-preview/HealthPolicies_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: HealthPolicies_Get_MaximumSet.
     * 
     * @param manager Entry point to PrivateTrafficManagerManager.
     */
    public static void healthPoliciesGetMaximumSet(
        com.azure.resourcemanager.privatetrafficmanager.PrivateTrafficManagerManager manager) {
        manager.healthPolicies()
            .getWithResponse("rgprivateTrafficManager", "myProfile", "myHealthPolicy",
                com.azure.core.util.Context.NONE);
    }
}
```

### HealthPolicies_ListByParent

```java
/**
 * Samples for HealthPolicies ListByParent.
 */
public final class HealthPoliciesListByParentSamples {
    /*
     * x-ms-original-file: 2026-02-09-preview/HealthPolicies_ListByParent_MaximumSet_Gen.json
     */
    /**
     * Sample code: HealthPolicies_ListByParent_MaximumSet.
     * 
     * @param manager Entry point to PrivateTrafficManagerManager.
     */
    public static void healthPoliciesListByParentMaximumSet(
        com.azure.resourcemanager.privatetrafficmanager.PrivateTrafficManagerManager manager) {
        manager.healthPolicies().listByParent("rgprivateTrafficManager", "myProfile", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-02-09-preview/Operations_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_MinimumSet.
     * 
     * @param manager Entry point to PrivateTrafficManagerManager.
     */
    public static void
        operationsListMinimumSet(com.azure.resourcemanager.privatetrafficmanager.PrivateTrafficManagerManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-02-09-preview/Operations_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_MaximumSet.
     * 
     * @param manager Entry point to PrivateTrafficManagerManager.
     */
    public static void
        operationsListMaximumSet(com.azure.resourcemanager.privatetrafficmanager.PrivateTrafficManagerManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### ProfileProbingGateways_CreateOrUpdate

```java
import com.azure.resourcemanager.privatetrafficmanager.models.ProfileProbingGatewayProperties;

/**
 * Samples for ProfileProbingGateways CreateOrUpdate.
 */
public final class ProfileProbingGatewaysCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-02-09-preview/ProfileProbingGateways_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: ProfileProbingGateways_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to PrivateTrafficManagerManager.
     */
    public static void profileProbingGatewaysCreateOrUpdateMaximumSet(
        com.azure.resourcemanager.privatetrafficmanager.PrivateTrafficManagerManager manager) {
        manager.profileProbingGateways()
            .define("myProbingGateway")
            .withExistingPrivateTrafficManagerProfile("rgprivateTrafficManager", "myProfile")
            .withProperties(new ProfileProbingGatewayProperties().withProbingGatewayId(
                "/subscriptions/A1B2C3D4-E5F6-7890-ABCD-1234567890EF/resourceGroups/rgProbingInfra/providers/Microsoft.Network/probingGateways/pgw-westus-001"))
            .create();
    }
}
```

### ProfileProbingGateways_Delete

```java
/**
 * Samples for ProfileProbingGateways Delete.
 */
public final class ProfileProbingGatewaysDeleteSamples {
    /*
     * x-ms-original-file: 2026-02-09-preview/ProfileProbingGateways_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: ProfileProbingGateways_Delete_MaximumSet.
     * 
     * @param manager Entry point to PrivateTrafficManagerManager.
     */
    public static void profileProbingGatewaysDeleteMaximumSet(
        com.azure.resourcemanager.privatetrafficmanager.PrivateTrafficManagerManager manager) {
        manager.profileProbingGateways()
            .delete("rgprivateTrafficManager", "myProfile", "myProbingGateway", com.azure.core.util.Context.NONE);
    }
}
```

### ProfileProbingGateways_Get

```java
/**
 * Samples for ProfileProbingGateways Get.
 */
public final class ProfileProbingGatewaysGetSamples {
    /*
     * x-ms-original-file: 2026-02-09-preview/ProfileProbingGateways_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: ProfileProbingGateways_Get_MaximumSet.
     * 
     * @param manager Entry point to PrivateTrafficManagerManager.
     */
    public static void profileProbingGatewaysGetMaximumSet(
        com.azure.resourcemanager.privatetrafficmanager.PrivateTrafficManagerManager manager) {
        manager.profileProbingGateways()
            .getWithResponse("rgprivateTrafficManager", "myProfile", "myProbingGateway",
                com.azure.core.util.Context.NONE);
    }
}
```

### ProfileProbingGateways_ListByParent

```java
/**
 * Samples for ProfileProbingGateways ListByParent.
 */
public final class ProfileProbingGatewaysListByParentSamples {
    /*
     * x-ms-original-file: 2026-02-09-preview/ProfileProbingGateways_ListByParent_MaximumSet_Gen.json
     */
    /**
     * Sample code: ProfileProbingGateways_ListByParent_MaximumSet.
     * 
     * @param manager Entry point to PrivateTrafficManagerManager.
     */
    public static void profileProbingGatewaysListByParentMaximumSet(
        com.azure.resourcemanager.privatetrafficmanager.PrivateTrafficManagerManager manager) {
        manager.profileProbingGateways()
            .listByParent("rgprivateTrafficManager", "myProfile", com.azure.core.util.Context.NONE);
    }
}
```

### ProfileProbingGateways_Update

```java
import com.azure.resourcemanager.privatetrafficmanager.models.ProfileProbingGateway;
import com.azure.resourcemanager.privatetrafficmanager.models.ProfileProbingGatewayUpdateProperties;

/**
 * Samples for ProfileProbingGateways Update.
 */
public final class ProfileProbingGatewaysUpdateSamples {
    /*
     * x-ms-original-file: 2026-02-09-preview/ProfileProbingGateways_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: ProfileProbingGateways_Update_MaximumSet.
     * 
     * @param manager Entry point to PrivateTrafficManagerManager.
     */
    public static void profileProbingGatewaysUpdateMaximumSet(
        com.azure.resourcemanager.privatetrafficmanager.PrivateTrafficManagerManager manager) {
        ProfileProbingGateway resource = manager.profileProbingGateways()
            .getWithResponse("rgprivateTrafficManager", "myProfile", "myProbingGateway",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new ProfileProbingGatewayUpdateProperties().withProbingGatewayId(
                "/subscriptions/A1B2C3D4-E5F6-7890-ABCD-1234567890EF/resourceGroups/rgProbingInfra/providers/Microsoft.Network/probingGateways/pgw-westus-001"))
            .apply();
    }
}
```

### Profiles_CreateOrUpdate

```java
import com.azure.resourcemanager.privatetrafficmanager.models.AdministrativeStatus;
import com.azure.resourcemanager.privatetrafficmanager.models.AlwaysServe;
import com.azure.resourcemanager.privatetrafficmanager.models.CustomTopologyMapMode;
import com.azure.resourcemanager.privatetrafficmanager.models.DnsConfig;
import com.azure.resourcemanager.privatetrafficmanager.models.EndpointsKind;
import com.azure.resourcemanager.privatetrafficmanager.models.ProfileEndpoint;
import com.azure.resourcemanager.privatetrafficmanager.models.ProfileProperties;
import com.azure.resourcemanager.privatetrafficmanager.models.ProfileStatus;
import com.azure.resourcemanager.privatetrafficmanager.models.RecordType;
import com.azure.resourcemanager.privatetrafficmanager.models.TrafficRoutingMethod;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Profiles CreateOrUpdate.
 */
public final class ProfilesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-02-09-preview/Profiles_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: Profiles_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to PrivateTrafficManagerManager.
     */
    public static void profilesCreateOrUpdateMaximumSet(
        com.azure.resourcemanager.privatetrafficmanager.PrivateTrafficManagerManager manager) {
        manager.profiles()
            .define("myProfile")
            .withRegion("eastus")
            .withExistingResourceGroup("rgprivateTrafficManager")
            .withTags(mapOf("environment", "production"))
            .withProperties(new ProfileProperties().withCustomTopologyMapMode(CustomTopologyMapMode.DISABLED)
                .withDnsConfig(new DnsConfig().withRecordType(RecordType.A).withTtl(10L))
                .withTopologyMapId(
                    "/subscriptions/10B6D88D-ADF4-4281-B3D0-B5A6702DEEDA/resourceGroups/rgprivateTrafficManager/providers/Microsoft.Network/topologyMaps/myTopologyMap")
                .withProfileStatus(ProfileStatus.ENABLED)
                .withTrafficRoutingMethod(TrafficRoutingMethod.PRIORITY)
                .withEndpoints(Arrays.asList(new ProfileEndpoint().withName("eelctkkre")
                    .withTarget("10.0.0.1")
                    .withMonitoringTarget("10.0.0.1")
                    .withEndpointStatus(AdministrativeStatus.ENABLED)
                    .withKind(EndpointsKind.ENDPOINT)
                    .withWeight(100L)
                    .withPriority(10L)
                    .withAlwaysServe(AlwaysServe.ENABLED)
                    .withHealthPolicyId(
                        "/subscriptions/10B6D88D-ADF4-4281-B3D0-B5A6702DEEDA/resourceGroups/rgprivateTrafficManager/providers/Microsoft.Network/privateTrafficManagerProfiles/myProfile/healthPolicies/myHealthPolicy"))))
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

### Profiles_Delete

```java
/**
 * Samples for Profiles Delete.
 */
public final class ProfilesDeleteSamples {
    /*
     * x-ms-original-file: 2026-02-09-preview/Profiles_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Profiles_Delete_MaximumSet.
     * 
     * @param manager Entry point to PrivateTrafficManagerManager.
     */
    public static void
        profilesDeleteMaximumSet(com.azure.resourcemanager.privatetrafficmanager.PrivateTrafficManagerManager manager) {
        manager.profiles().delete("rgprivateTrafficManager", "myProfile", com.azure.core.util.Context.NONE);
    }
}
```

### Profiles_GetByResourceGroup

```java
/**
 * Samples for Profiles GetByResourceGroup.
 */
public final class ProfilesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-02-09-preview/Profiles_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Profiles_Get_MaximumSet - generated by [MaximumSet] rule.
     * 
     * @param manager Entry point to PrivateTrafficManagerManager.
     */
    public static void profilesGetMaximumSetGeneratedByMaximumSetRule(
        com.azure.resourcemanager.privatetrafficmanager.PrivateTrafficManagerManager manager) {
        manager.profiles()
            .getByResourceGroupWithResponse("rgprivateTrafficManager", "myProfile", com.azure.core.util.Context.NONE);
    }
}
```

### Profiles_List

```java
/**
 * Samples for Profiles List.
 */
public final class ProfilesListSamples {
    /*
     * x-ms-original-file: 2026-02-09-preview/Profiles_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: Profiles_ListBySubscription_MaximumSet.
     * 
     * @param manager Entry point to PrivateTrafficManagerManager.
     */
    public static void profilesListBySubscriptionMaximumSet(
        com.azure.resourcemanager.privatetrafficmanager.PrivateTrafficManagerManager manager) {
        manager.profiles().list(com.azure.core.util.Context.NONE);
    }
}
```

### Profiles_ListByResourceGroup

```java
/**
 * Samples for Profiles ListByResourceGroup.
 */
public final class ProfilesListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-02-09-preview/Profiles_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: Profiles_ListByResourceGroup_MaximumSet.
     * 
     * @param manager Entry point to PrivateTrafficManagerManager.
     */
    public static void profilesListByResourceGroupMaximumSet(
        com.azure.resourcemanager.privatetrafficmanager.PrivateTrafficManagerManager manager) {
        manager.profiles().listByResourceGroup("rgprivateTrafficManager", com.azure.core.util.Context.NONE);
    }
}
```

### Profiles_Update

```java
import com.azure.resourcemanager.privatetrafficmanager.models.AdministrativeStatus;
import com.azure.resourcemanager.privatetrafficmanager.models.AlwaysServe;
import com.azure.resourcemanager.privatetrafficmanager.models.CustomTopologyMapMode;
import com.azure.resourcemanager.privatetrafficmanager.models.DnsConfig;
import com.azure.resourcemanager.privatetrafficmanager.models.EndpointsKind;
import com.azure.resourcemanager.privatetrafficmanager.models.PrivateTrafficManagerProfile;
import com.azure.resourcemanager.privatetrafficmanager.models.PrivateTrafficManagerProfileUpdateProperties;
import com.azure.resourcemanager.privatetrafficmanager.models.ProfileEndpoint;
import com.azure.resourcemanager.privatetrafficmanager.models.ProfileStatus;
import com.azure.resourcemanager.privatetrafficmanager.models.RecordType;
import com.azure.resourcemanager.privatetrafficmanager.models.TrafficRoutingMethod;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Profiles Update.
 */
public final class ProfilesUpdateSamples {
    /*
     * x-ms-original-file: 2026-02-09-preview/Profiles_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: Profiles_Update_MaximumSet.
     * 
     * @param manager Entry point to PrivateTrafficManagerManager.
     */
    public static void
        profilesUpdateMaximumSet(com.azure.resourcemanager.privatetrafficmanager.PrivateTrafficManagerManager manager) {
        PrivateTrafficManagerProfile resource = manager.profiles()
            .getByResourceGroupWithResponse("rgprivateTrafficManager", "myProfile", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("environment", "staging"))
            .withProperties(new PrivateTrafficManagerProfileUpdateProperties()
                .withCustomTopologyMapMode(CustomTopologyMapMode.DISABLED)
                .withDnsConfig(new DnsConfig().withRecordType(RecordType.A).withTtl(30L))
                .withTopologyMapId(
                    "/subscriptions/10B6D88D-ADF4-4281-B3D0-B5A6702DEEDA/resourceGroups/rgprivateTrafficManager/providers/Microsoft.Network/topologyMaps/myTopologyMap")
                .withProfileStatus(ProfileStatus.ENABLED)
                .withTrafficRoutingMethod(TrafficRoutingMethod.PRIORITY)
                .withEndpoints(Arrays.asList(new ProfileEndpoint().withName("kbxbzdqlpxpwhdekpfudgyvchk")
                    .withTarget("10.0.0.1")
                    .withMonitoringTarget("10.0.0.1")
                    .withEndpointStatus(AdministrativeStatus.ENABLED)
                    .withKind(EndpointsKind.ENDPOINT)
                    .withWeight(100L)
                    .withPriority(10L)
                    .withAlwaysServe(AlwaysServe.ENABLED)
                    .withHealthPolicyId(
                        "/subscriptions/10B6D88D-ADF4-4281-B3D0-B5A6702DEEDA/resourceGroups/rgprivateTrafficManager/providers/Microsoft.Network/privateTrafficManagerProfiles/myProfile/healthPolicies/myHealthPolicy"))))
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

### Sites_CreateOrUpdate

```java
import com.azure.resourcemanager.privatetrafficmanager.models.SiteProperties;
import java.util.Arrays;

/**
 * Samples for Sites CreateOrUpdate.
 */
public final class SitesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-02-09-preview/Sites_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: Sites_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to PrivateTrafficManagerManager.
     */
    public static void sitesCreateOrUpdateMaximumSet(
        com.azure.resourcemanager.privatetrafficmanager.PrivateTrafficManagerManager manager) {
        manager.sites()
            .define("mySite")
            .withExistingTopologyMap("rgprivateTrafficManager", "myTopologyMap")
            .withProperties(new SiteProperties().withProbingGatewayIds(Arrays.asList(
                "/subscriptions/10B6D88D-ADF4-4281-B3D0-B5A6702DEEDA/resourceGroups/rgprivateTrafficManager/providers/Microsoft.Network/probingGateways/myProbingGateway"))
                .withVirtualNetworkIds(Arrays.asList(
                    "/subscriptions/10B6D88D-ADF4-4281-B3D0-B5A6702DEEDA/resourceGroups/rgprivateTrafficManager/providers/Microsoft.Network/virtualNetworks/myVirtualNetwork")))
            .create();
    }
}
```

### Sites_Delete

```java
/**
 * Samples for Sites Delete.
 */
public final class SitesDeleteSamples {
    /*
     * x-ms-original-file: 2026-02-09-preview/Sites_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Sites_Delete_MaximumSet.
     * 
     * @param manager Entry point to PrivateTrafficManagerManager.
     */
    public static void
        sitesDeleteMaximumSet(com.azure.resourcemanager.privatetrafficmanager.PrivateTrafficManagerManager manager) {
        manager.sites().delete("rgprivateTrafficManager", "myTopologyMap", "mySite", com.azure.core.util.Context.NONE);
    }
}
```

### Sites_Get

```java
/**
 * Samples for Sites Get.
 */
public final class SitesGetSamples {
    /*
     * x-ms-original-file: 2026-02-09-preview/Sites_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Sites_Get_MaximumSet.
     * 
     * @param manager Entry point to PrivateTrafficManagerManager.
     */
    public static void
        sitesGetMaximumSet(com.azure.resourcemanager.privatetrafficmanager.PrivateTrafficManagerManager manager) {
        manager.sites()
            .getWithResponse("rgprivateTrafficManager", "myTopologyMap", "mySite", com.azure.core.util.Context.NONE);
    }
}
```

### Sites_ListByParent

```java
/**
 * Samples for Sites ListByParent.
 */
public final class SitesListByParentSamples {
    /*
     * x-ms-original-file: 2026-02-09-preview/Sites_ListByParent_MaximumSet_Gen.json
     */
    /**
     * Sample code: Sites_ListByParent_MaximumSet.
     * 
     * @param manager Entry point to PrivateTrafficManagerManager.
     */
    public static void sitesListByParentMaximumSet(
        com.azure.resourcemanager.privatetrafficmanager.PrivateTrafficManagerManager manager) {
        manager.sites().listByParent("rgprivateTrafficManager", "myTopologyMap", com.azure.core.util.Context.NONE);
    }
}
```

### Sites_Update

```java
import com.azure.resourcemanager.privatetrafficmanager.models.Site;
import com.azure.resourcemanager.privatetrafficmanager.models.SiteUpdateProperties;
import java.util.Arrays;

/**
 * Samples for Sites Update.
 */
public final class SitesUpdateSamples {
    /*
     * x-ms-original-file: 2026-02-09-preview/Sites_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: Sites_Update_MaximumSet.
     * 
     * @param manager Entry point to PrivateTrafficManagerManager.
     */
    public static void
        sitesUpdateMaximumSet(com.azure.resourcemanager.privatetrafficmanager.PrivateTrafficManagerManager manager) {
        Site resource = manager.sites()
            .getWithResponse("rgprivateTrafficManager", "myTopologyMap", "mySite", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new SiteUpdateProperties().withProbingGatewayIds(Arrays.asList(
                "/subscriptions/10B6D88D-ADF4-4281-B3D0-B5A6702DEEDA/resourceGroups/rgprivateTrafficManager/providers/Microsoft.Network/probingGateways/myProbingGateway",
                "/subscriptions/10B6D88D-ADF4-4281-B3D0-B5A6702DEEDA/resourceGroups/rgprivateTrafficManager/providers/Microsoft.Network/probingGateways/myProbingGateway2"))
                .withVirtualNetworkIds(Arrays.asList(
                    "/subscriptions/10B6D88D-ADF4-4281-B3D0-B5A6702DEEDA/resourceGroups/rgprivateTrafficManager/providers/Microsoft.Network/virtualNetworks/myVirtualNetwork")))
            .apply();
    }
}
```

### TopologyMaps_CreateOrUpdate

```java
import com.azure.resourcemanager.privatetrafficmanager.models.SiteProperties;
import com.azure.resourcemanager.privatetrafficmanager.models.TopologyMapInlineSite;
import com.azure.resourcemanager.privatetrafficmanager.models.TopologyMapProperties;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for TopologyMaps CreateOrUpdate.
 */
public final class TopologyMapsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-02-09-preview/TopologyMaps_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: TopologyMaps_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to PrivateTrafficManagerManager.
     */
    public static void topologyMapsCreateOrUpdateMaximumSet(
        com.azure.resourcemanager.privatetrafficmanager.PrivateTrafficManagerManager manager) {
        manager.topologyMaps()
            .define("myTopologyMap")
            .withRegion("eastus")
            .withExistingResourceGroup("rgprivateTrafficManager")
            .withTags(mapOf("environment", "production"))
            .withProperties(new TopologyMapProperties().withSites(Arrays.asList(new TopologyMapInlineSite()
                .withName("mySite")
                .withProperties(new SiteProperties().withProbingGatewayIds(Arrays.asList(
                    "/subscriptions/10B6D88D-ADF4-4281-B3D0-B5A6702DEEDA/resourceGroups/rgprivateTrafficManager/providers/Microsoft.Network/probingGateways/myProbingGateway"))
                    .withVirtualNetworkIds(Arrays.asList(
                        "/subscriptions/10B6D88D-ADF4-4281-B3D0-B5A6702DEEDA/resourceGroups/rgprivateTrafficManager/providers/Microsoft.Network/virtualNetworks/myVirtualNetwork")))))
                .withCatchAllSiteName("mySite"))
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

### TopologyMaps_Delete

```java
/**
 * Samples for TopologyMaps Delete.
 */
public final class TopologyMapsDeleteSamples {
    /*
     * x-ms-original-file: 2026-02-09-preview/TopologyMaps_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: TopologyMaps_Delete_MaximumSet - generated by [MaximumSet] rule.
     * 
     * @param manager Entry point to PrivateTrafficManagerManager.
     */
    public static void topologyMapsDeleteMaximumSetGeneratedByMaximumSetRule(
        com.azure.resourcemanager.privatetrafficmanager.PrivateTrafficManagerManager manager) {
        manager.topologyMaps().delete("rgprivateTrafficManager", "myTopologyMap", com.azure.core.util.Context.NONE);
    }
}
```

### TopologyMaps_GetByResourceGroup

```java
/**
 * Samples for TopologyMaps GetByResourceGroup.
 */
public final class TopologyMapsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-02-09-preview/TopologyMaps_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: TopologyMaps_Get_MaximumSet.
     * 
     * @param manager Entry point to PrivateTrafficManagerManager.
     */
    public static void topologyMapsGetMaximumSet(
        com.azure.resourcemanager.privatetrafficmanager.PrivateTrafficManagerManager manager) {
        manager.topologyMaps()
            .getByResourceGroupWithResponse("rgprivateTrafficManager", "myTopologyMap",
                com.azure.core.util.Context.NONE);
    }
}
```

### TopologyMaps_List

```java
/**
 * Samples for TopologyMaps List.
 */
public final class TopologyMapsListSamples {
    /*
     * x-ms-original-file: 2026-02-09-preview/TopologyMaps_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: TopologyMaps_ListBySubscription_MaximumSet.
     * 
     * @param manager Entry point to PrivateTrafficManagerManager.
     */
    public static void topologyMapsListBySubscriptionMaximumSet(
        com.azure.resourcemanager.privatetrafficmanager.PrivateTrafficManagerManager manager) {
        manager.topologyMaps().list(com.azure.core.util.Context.NONE);
    }
}
```

### TopologyMaps_ListByResourceGroup

```java
/**
 * Samples for TopologyMaps ListByResourceGroup.
 */
public final class TopologyMapsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-02-09-preview/TopologyMaps_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: TopologyMaps_ListByResourceGroup_MaximumSet.
     * 
     * @param manager Entry point to PrivateTrafficManagerManager.
     */
    public static void topologyMapsListByResourceGroupMaximumSet(
        com.azure.resourcemanager.privatetrafficmanager.PrivateTrafficManagerManager manager) {
        manager.topologyMaps().listByResourceGroup("rgprivateTrafficManager", com.azure.core.util.Context.NONE);
    }
}
```

### TopologyMaps_Update

```java
import com.azure.resourcemanager.privatetrafficmanager.models.TopologyMap;
import com.azure.resourcemanager.privatetrafficmanager.models.TopologyMapPatchProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for TopologyMaps Update.
 */
public final class TopologyMapsUpdateSamples {
    /*
     * x-ms-original-file: 2026-02-09-preview/TopologyMaps_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: TopologyMaps_Update_MaximumSet.
     * 
     * @param manager Entry point to PrivateTrafficManagerManager.
     */
    public static void topologyMapsUpdateMaximumSet(
        com.azure.resourcemanager.privatetrafficmanager.PrivateTrafficManagerManager manager) {
        TopologyMap resource = manager.topologyMaps()
            .getByResourceGroupWithResponse("rgprivateTrafficManager", "myTopologyMap",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("environment", "staging"))
            .withProperties(new TopologyMapPatchProperties().withCatchAllSiteName("mySite"))
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

