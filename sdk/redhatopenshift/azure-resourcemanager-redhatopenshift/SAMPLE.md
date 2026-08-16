# Code snippets and samples


## OpenShiftClusters

- [CreateOrUpdate](#openshiftclusters_createorupdate)
- [Delete](#openshiftclusters_delete)
- [GetByResourceGroup](#openshiftclusters_getbyresourcegroup)
- [List](#openshiftclusters_list)
- [ListAdminCredentials](#openshiftclusters_listadmincredentials)
- [ListByResourceGroup](#openshiftclusters_listbyresourcegroup)
- [ListCredentials](#openshiftclusters_listcredentials)
- [Update](#openshiftclusters_update)

## OpenShiftVersions

- [Get](#openshiftversions_get)
- [List](#openshiftversions_list)

## Operations

- [List](#operations_list)

## PlatformWorkloadIdentityRoleSetOperation

- [Get](#platformworkloadidentityrolesetoperation_get)

## PlatformWorkloadIdentityRoleSets

- [List](#platformworkloadidentityrolesets_list)
### OpenShiftClusters_CreateOrUpdate

```java
import com.azure.resourcemanager.redhatopenshift.models.ApiServerProfile;
import com.azure.resourcemanager.redhatopenshift.models.ClusterProfile;
import com.azure.resourcemanager.redhatopenshift.models.ConsoleProfile;
import com.azure.resourcemanager.redhatopenshift.models.EncryptionAtHost;
import com.azure.resourcemanager.redhatopenshift.models.FipsValidatedModules;
import com.azure.resourcemanager.redhatopenshift.models.IngressProfile;
import com.azure.resourcemanager.redhatopenshift.models.LoadBalancerProfile;
import com.azure.resourcemanager.redhatopenshift.models.ManagedOutboundIPs;
import com.azure.resourcemanager.redhatopenshift.models.ManagedServiceIdentity;
import com.azure.resourcemanager.redhatopenshift.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.redhatopenshift.models.MasterProfile;
import com.azure.resourcemanager.redhatopenshift.models.NetworkProfile;
import com.azure.resourcemanager.redhatopenshift.models.PlatformWorkloadIdentity;
import com.azure.resourcemanager.redhatopenshift.models.PlatformWorkloadIdentityProfile;
import com.azure.resourcemanager.redhatopenshift.models.PreconfiguredNsg;
import com.azure.resourcemanager.redhatopenshift.models.ServicePrincipalProfile;
import com.azure.resourcemanager.redhatopenshift.models.UserAssignedIdentity;
import com.azure.resourcemanager.redhatopenshift.models.Visibility;
import com.azure.resourcemanager.redhatopenshift.models.WorkerProfile;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for OpenShiftClusters CreateOrUpdate.
 */
public final class OpenShiftClustersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-07-25/OpenShiftClusters_CreateOrUpdate.json
     */
    /**
     * Sample code: Creates or updates a OpenShift cluster with the specified subscription, resource group and resource
     * name.
     * 
     * @param manager Entry point to RedHatOpenShiftManager.
     */
    public static void createsOrUpdatesAOpenShiftClusterWithTheSpecifiedSubscriptionResourceGroupAndResourceName(
        com.azure.resourcemanager.redhatopenshift.RedHatOpenShiftManager manager) {
        manager.openShiftClusters()
            .define("resourceName")
            .withRegion("location")
            .withExistingResourceGroup("resourceGroup")
            .withTags(mapOf("key", "fakeTokenPlaceholder"))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf("", new UserAssignedIdentity())))
            .withClusterProfile(new ClusterProfile().withPullSecret("fakeTokenPlaceholder")
                .withDomain("cluster.location.aroapp.io")
                .withResourceGroupId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/clusterResourceGroup")
                .withFipsValidatedModules(FipsValidatedModules.ENABLED))
            .withConsoleProfile(new ConsoleProfile())
            .withServicePrincipalProfile(
                new ServicePrincipalProfile().withClientId("clientId").withClientSecret("fakeTokenPlaceholder"))
            .withPlatformWorkloadIdentityProfile(new PlatformWorkloadIdentityProfile()
                .withPlatformWorkloadIdentities(mapOf("", new PlatformWorkloadIdentity())))
            .withNetworkProfile(new NetworkProfile().withPodCidr("10.128.0.0/14")
                .withServiceCidr("172.30.0.0/16")
                .withLoadBalancerProfile(
                    new LoadBalancerProfile().withManagedOutboundIps(new ManagedOutboundIPs().withCount(1)))
                .withPreconfiguredNsg(PreconfiguredNsg.DISABLED))
            .withMasterProfile(new MasterProfile().withVmSize("Standard_D8s_v3")
                .withSubnetId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/vnetResourceGroup/providers/Microsoft.Network/virtualNetworks/vnet/subnets/master")
                .withEncryptionAtHost(EncryptionAtHost.ENABLED))
            .withWorkerProfiles(Arrays.asList(new WorkerProfile().withName("worker")
                .withVmSize("Standard_D2s_v3")
                .withDiskSizeGB(128)
                .withSubnetId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/vnetResourceGroup/providers/Microsoft.Network/virtualNetworks/vnet/subnets/worker")
                .withCount(3)))
            .withApiserverProfile(new ApiServerProfile().withVisibility(Visibility.PUBLIC))
            .withIngressProfiles(
                Arrays.asList(new IngressProfile().withName("default").withVisibility(Visibility.PUBLIC)))
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

### OpenShiftClusters_Delete

```java
/**
 * Samples for OpenShiftClusters Delete.
 */
public final class OpenShiftClustersDeleteSamples {
    /*
     * x-ms-original-file: 2025-07-25/OpenShiftClusters_Delete.json
     */
    /**
     * Sample code: Deletes a OpenShift cluster with the specified subscription, resource group and resource name.
     * 
     * @param manager Entry point to RedHatOpenShiftManager.
     */
    public static void deletesAOpenShiftClusterWithTheSpecifiedSubscriptionResourceGroupAndResourceName(
        com.azure.resourcemanager.redhatopenshift.RedHatOpenShiftManager manager) {
        manager.openShiftClusters().delete("resourceGroup", "resourceName", com.azure.core.util.Context.NONE);
    }
}
```

### OpenShiftClusters_GetByResourceGroup

```java
/**
 * Samples for OpenShiftClusters GetByResourceGroup.
 */
public final class OpenShiftClustersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-07-25/OpenShiftClusters_Get.json
     */
    /**
     * Sample code: Gets a OpenShift cluster with the specified subscription, resource group and resource name.
     * 
     * @param manager Entry point to RedHatOpenShiftManager.
     */
    public static void getsAOpenShiftClusterWithTheSpecifiedSubscriptionResourceGroupAndResourceName(
        com.azure.resourcemanager.redhatopenshift.RedHatOpenShiftManager manager) {
        manager.openShiftClusters()
            .getByResourceGroupWithResponse("resourceGroup", "resourceName", com.azure.core.util.Context.NONE);
    }
}
```

### OpenShiftClusters_List

```java
/**
 * Samples for OpenShiftClusters List.
 */
public final class OpenShiftClustersListSamples {
    /*
     * x-ms-original-file: 2025-07-25/OpenShiftClusters_List.json
     */
    /**
     * Sample code: Lists OpenShift clusters in the specified subscription.
     * 
     * @param manager Entry point to RedHatOpenShiftManager.
     */
    public static void listsOpenShiftClustersInTheSpecifiedSubscription(
        com.azure.resourcemanager.redhatopenshift.RedHatOpenShiftManager manager) {
        manager.openShiftClusters().list(com.azure.core.util.Context.NONE);
    }
}
```

### OpenShiftClusters_ListAdminCredentials

```java
/**
 * Samples for OpenShiftClusters ListAdminCredentials.
 */
public final class OpenShiftClustersListAdminCredentialsSamples {
    /*
     * x-ms-original-file: 2025-07-25/OpenShiftClusters_ListAdminCredentials.json
     */
    /**
     * Sample code: Lists admin kubeconfig of an OpenShift cluster with the specified subscription, resource group and
     * resource name.
     * 
     * @param manager Entry point to RedHatOpenShiftManager.
     */
    public static void listsAdminKubeconfigOfAnOpenShiftClusterWithTheSpecifiedSubscriptionResourceGroupAndResourceName(
        com.azure.resourcemanager.redhatopenshift.RedHatOpenShiftManager manager) {
        manager.openShiftClusters()
            .listAdminCredentialsWithResponse("resourceGroup", "resourceName", com.azure.core.util.Context.NONE);
    }
}
```

### OpenShiftClusters_ListByResourceGroup

```java
/**
 * Samples for OpenShiftClusters ListByResourceGroup.
 */
public final class OpenShiftClustersListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-07-25/OpenShiftClusters_ListByResourceGroup.json
     */
    /**
     * Sample code: Lists OpenShift clusters in the specified subscription and resource group.
     * 
     * @param manager Entry point to RedHatOpenShiftManager.
     */
    public static void listsOpenShiftClustersInTheSpecifiedSubscriptionAndResourceGroup(
        com.azure.resourcemanager.redhatopenshift.RedHatOpenShiftManager manager) {
        manager.openShiftClusters().listByResourceGroup("resourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### OpenShiftClusters_ListCredentials

```java
/**
 * Samples for OpenShiftClusters ListCredentials.
 */
public final class OpenShiftClustersListCredentialsSamples {
    /*
     * x-ms-original-file: 2025-07-25/OpenShiftClusters_ListCredentials.json
     */
    /**
     * Sample code: Lists credentials of an OpenShift cluster with the specified subscription, resource group and
     * resource name.
     * 
     * @param manager Entry point to RedHatOpenShiftManager.
     */
    public static void listsCredentialsOfAnOpenShiftClusterWithTheSpecifiedSubscriptionResourceGroupAndResourceName(
        com.azure.resourcemanager.redhatopenshift.RedHatOpenShiftManager manager) {
        manager.openShiftClusters()
            .listCredentialsWithResponse("resourceGroup", "resourceName", com.azure.core.util.Context.NONE);
    }
}
```

### OpenShiftClusters_Update

```java
import com.azure.resourcemanager.redhatopenshift.models.ApiServerProfile;
import com.azure.resourcemanager.redhatopenshift.models.ClusterProfile;
import com.azure.resourcemanager.redhatopenshift.models.ConsoleProfile;
import com.azure.resourcemanager.redhatopenshift.models.EncryptionAtHost;
import com.azure.resourcemanager.redhatopenshift.models.FipsValidatedModules;
import com.azure.resourcemanager.redhatopenshift.models.IngressProfile;
import com.azure.resourcemanager.redhatopenshift.models.LoadBalancerProfile;
import com.azure.resourcemanager.redhatopenshift.models.ManagedOutboundIPs;
import com.azure.resourcemanager.redhatopenshift.models.ManagedServiceIdentity;
import com.azure.resourcemanager.redhatopenshift.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.redhatopenshift.models.MasterProfile;
import com.azure.resourcemanager.redhatopenshift.models.NetworkProfile;
import com.azure.resourcemanager.redhatopenshift.models.OpenShiftCluster;
import com.azure.resourcemanager.redhatopenshift.models.PlatformWorkloadIdentity;
import com.azure.resourcemanager.redhatopenshift.models.PlatformWorkloadIdentityProfile;
import com.azure.resourcemanager.redhatopenshift.models.PreconfiguredNsg;
import com.azure.resourcemanager.redhatopenshift.models.ServicePrincipalProfile;
import com.azure.resourcemanager.redhatopenshift.models.UserAssignedIdentity;
import com.azure.resourcemanager.redhatopenshift.models.Visibility;
import com.azure.resourcemanager.redhatopenshift.models.WorkerProfile;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for OpenShiftClusters Update.
 */
public final class OpenShiftClustersUpdateSamples {
    /*
     * x-ms-original-file: 2025-07-25/OpenShiftClusters_Update.json
     */
    /**
     * Sample code: Creates or updates a OpenShift cluster with the specified subscription, resource group and resource
     * name.
     * 
     * @param manager Entry point to RedHatOpenShiftManager.
     */
    public static void createsOrUpdatesAOpenShiftClusterWithTheSpecifiedSubscriptionResourceGroupAndResourceName(
        com.azure.resourcemanager.redhatopenshift.RedHatOpenShiftManager manager) {
        OpenShiftCluster resource = manager.openShiftClusters()
            .getByResourceGroupWithResponse("resourceGroup", "resourceName", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key", "fakeTokenPlaceholder"))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf("", new UserAssignedIdentity())))
            .withClusterProfile(new ClusterProfile().withPullSecret("fakeTokenPlaceholder")
                .withDomain("cluster.location.aroapp.io")
                .withResourceGroupId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/clusterResourceGroup")
                .withFipsValidatedModules(FipsValidatedModules.ENABLED))
            .withConsoleProfile(new ConsoleProfile())
            .withServicePrincipalProfile(
                new ServicePrincipalProfile().withClientId("clientId").withClientSecret("fakeTokenPlaceholder"))
            .withPlatformWorkloadIdentityProfile(new PlatformWorkloadIdentityProfile()
                .withPlatformWorkloadIdentities(mapOf("", new PlatformWorkloadIdentity())))
            .withNetworkProfile(new NetworkProfile().withPodCidr("10.128.0.0/14")
                .withServiceCidr("172.30.0.0/16")
                .withLoadBalancerProfile(
                    new LoadBalancerProfile().withManagedOutboundIps(new ManagedOutboundIPs().withCount(1)))
                .withPreconfiguredNsg(PreconfiguredNsg.DISABLED))
            .withMasterProfile(new MasterProfile().withVmSize("Standard_D8s_v3")
                .withSubnetId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/vnetResourceGroup/providers/Microsoft.Network/virtualNetworks/vnet/subnets/master")
                .withEncryptionAtHost(EncryptionAtHost.ENABLED))
            .withWorkerProfiles(Arrays.asList(new WorkerProfile().withName("worker")
                .withVmSize("Standard_D2s_v3")
                .withDiskSizeGB(128)
                .withSubnetId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/vnetResourceGroup/providers/Microsoft.Network/virtualNetworks/vnet/subnets/worker")
                .withCount(3)))
            .withApiserverProfile(new ApiServerProfile().withVisibility(Visibility.PUBLIC))
            .withIngressProfiles(
                Arrays.asList(new IngressProfile().withName("default").withVisibility(Visibility.PUBLIC)))
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

### OpenShiftVersions_Get

```java
/**
 * Samples for OpenShiftVersions Get.
 */
public final class OpenShiftVersionsGetSamples {
    /*
     * x-ms-original-file: 2025-07-25/OpenShiftVersions_Get.json
     */
    /**
     * Sample code: Gets an available OpenShift version to install in the specified location.
     * 
     * @param manager Entry point to RedHatOpenShiftManager.
     */
    public static void getsAnAvailableOpenShiftVersionToInstallInTheSpecifiedLocation(
        com.azure.resourcemanager.redhatopenshift.RedHatOpenShiftManager manager) {
        manager.openShiftVersions().getWithResponse("location", "4.14.40", com.azure.core.util.Context.NONE);
    }
}
```

### OpenShiftVersions_List

```java
/**
 * Samples for OpenShiftVersions List.
 */
public final class OpenShiftVersionsListSamples {
    /*
     * x-ms-original-file: 2025-07-25/OpenShiftVersions_List.json
     */
    /**
     * Sample code: Lists all OpenShift versions available to install in the specified location.
     * 
     * @param manager Entry point to RedHatOpenShiftManager.
     */
    public static void listsAllOpenShiftVersionsAvailableToInstallInTheSpecifiedLocation(
        com.azure.resourcemanager.redhatopenshift.RedHatOpenShiftManager manager) {
        manager.openShiftVersions().list("location", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2025-07-25/Operations_List.json
     */
    /**
     * Sample code: Lists all of the available RP operations.
     * 
     * @param manager Entry point to RedHatOpenShiftManager.
     */
    public static void
        listsAllOfTheAvailableRPOperations(com.azure.resourcemanager.redhatopenshift.RedHatOpenShiftManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### PlatformWorkloadIdentityRoleSetOperation_Get

```java
/**
 * Samples for PlatformWorkloadIdentityRoleSetOperation Get.
 */
public final class PlatformWorkloadIdentityRoleSetOperationGetSamples {
    /*
     * x-ms-original-file: 2025-07-25/PlatformWorkloadIdentityRoleSet_Get.json
     */
    /**
     * Sample code: Gets a mapping of an OpenShift version to identity requirements, which includes operatorName,
     * roleDefinitionName, roleDefinitionId, and serviceAccounts.
     * 
     * @param manager Entry point to RedHatOpenShiftManager.
     */
    public static void
        getsAMappingOfAnOpenShiftVersionToIdentityRequirementsWhichIncludesOperatorNameRoleDefinitionNameRoleDefinitionIdAndServiceAccounts(
            com.azure.resourcemanager.redhatopenshift.RedHatOpenShiftManager manager) {
        manager.platformWorkloadIdentityRoleSetOperations()
            .getWithResponse("location", "4.14", com.azure.core.util.Context.NONE);
    }
}
```

### PlatformWorkloadIdentityRoleSets_List

```java
/**
 * Samples for PlatformWorkloadIdentityRoleSets List.
 */
public final class PlatformWorkloadIdentityRoleSetsListSamples {
    /*
     * x-ms-original-file: 2025-07-25/PlatformWorkloadIdentityRoleSets_List.json
     */
    /**
     * Sample code: Lists a mapping of OpenShift versions to identity requirements, which include operatorName,
     * roleDefinitionName, roleDefinitionId, and serviceAccounts.
     * 
     * @param manager Entry point to RedHatOpenShiftManager.
     */
    public static void
        listsAMappingOfOpenShiftVersionsToIdentityRequirementsWhichIncludeOperatorNameRoleDefinitionNameRoleDefinitionIdAndServiceAccounts(
            com.azure.resourcemanager.redhatopenshift.RedHatOpenShiftManager manager) {
        manager.platformWorkloadIdentityRoleSets().list("location", com.azure.core.util.Context.NONE);
    }
}
```

