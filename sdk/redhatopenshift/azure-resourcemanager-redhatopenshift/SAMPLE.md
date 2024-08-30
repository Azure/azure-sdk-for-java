# Code snippets and samples


## MachinePools

- [CreateOrUpdate](#machinepools_createorupdate)
- [Delete](#machinepools_delete)
- [Get](#machinepools_get)
- [List](#machinepools_list)
- [Update](#machinepools_update)

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

- [List](#openshiftversions_list)

## Operations

- [List](#operations_list)

## Secrets

- [CreateOrUpdate](#secrets_createorupdate)
- [Delete](#secrets_delete)
- [Get](#secrets_get)
- [List](#secrets_list)
- [Update](#secrets_update)

## SyncIdentityProviders

- [CreateOrUpdate](#syncidentityproviders_createorupdate)
- [Delete](#syncidentityproviders_delete)
- [Get](#syncidentityproviders_get)
- [List](#syncidentityproviders_list)
- [Update](#syncidentityproviders_update)

## SyncSets

- [CreateOrUpdate](#syncsets_createorupdate)
- [Delete](#syncsets_delete)
- [Get](#syncsets_get)
- [List](#syncsets_list)
- [Update](#syncsets_update)
### MachinePools_CreateOrUpdate

```java
/**
 * Samples for MachinePools CreateOrUpdate.
 */
public final class MachinePoolsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/redhatopenshift/resource-manager/Microsoft.RedHatOpenShift/openshiftclusters/stable/2023-11-22/
     * examples/MachinePools_CreateOrUpdate.json
     */
    /**
     * Sample code: Creates or updates a MachinePool with the specified subscription, resource group and resource name.
     * 
     * @param manager Entry point to RedHatOpenShiftManager.
     */
    public static void createsOrUpdatesAMachinePoolWithTheSpecifiedSubscriptionResourceGroupAndResourceName(
        com.azure.resourcemanager.redhatopenshift.RedHatOpenShiftManager manager) {
        manager.machinePools()
            .define("childResourceName")
            .withExistingOpenshiftcluster("resourceGroup", "resourceName")
            .withResources(
                "ewogICAgImFwaVZlcnNpb24iOiAiaGl2ZS5vcGVuc2hpZnQuaW8vdjEiLAogICAgImtpbmQiOiAiTWFjaGluZVBvb2wiLAogICAgIm1ldGFkYXRhIjogewogICAgICAgICJuYW1lIjogInRlc3QtY2x1c3Rlci13b3JrZXIiLAogICAgICAgICJuYW1lc3BhY2UiOiAiYXJvLWY2MGFlOGEyLWJjYTEtNDk4Ny05MDU2LVhYWFhYWFhYWFhYWCIKICAgIH0sCiAgICAic3BlYyI6IHsKICAgICAgICAiY2x1c3RlckRlcGxveW1lbnRSZWYiOiB7CiAgICAgICAgICAgICJuYW1lIjogInRlc3QtY2x1c3RlciIKICAgICAgICB9LAogICAgICAgICJuYW1lIjogIndvcmtlciIsCiAgICAgICAgInBsYXRmb3JtIjogewogICAgICAgICAgICAiYXdzIjogewogICAgICAgICAgICAgICAgInJvb3RWb2x1bWUiOiB7CiAgICAgICAgICAgICAgICAgICAgImlvcHMiOiAwLAogICAgICAgICAgICAgICAgICAgICJzaXplIjogMzAwLAogICAgICAgICAgICAgICAgICAgICJ0eXBlIjogImdwMyIKICAgICAgICAgICAgICAgIH0sCiAgICAgICAgICAgICAgICAidHlwZSI6ICJtNS54bGFyZ2UiLAogICAgICAgICAgICAgICAgInpvbmVzIjogWwogICAgICAgICAgICAgICAgICAgICJ1cy1lYXN0LTFhIgogICAgICAgICAgICAgICAgXQogICAgICAgICAgICB9CiAgICAgICAgfSwKICAgICAgICAicmVwbGljYXMiOiAyCiAgICB9LAogICAgInN0YXR1cyI6IHsKICAgICAgICAiY29uZGl0aW9ucyI6IFsKICAgICAgICBdCiAgICB9Cn0K")
            .create();
    }
}
```

### MachinePools_Delete

```java
/**
 * Samples for MachinePools Delete.
 */
public final class MachinePoolsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/redhatopenshift/resource-manager/Microsoft.RedHatOpenShift/openshiftclusters/stable/2023-11-22/
     * examples/MachinePools_Delete.json
     */
    /**
     * Sample code: Deletes a MachinePool with the specified subscription, resource group and resource name.
     * 
     * @param manager Entry point to RedHatOpenShiftManager.
     */
    public static void deletesAMachinePoolWithTheSpecifiedSubscriptionResourceGroupAndResourceName(
        com.azure.resourcemanager.redhatopenshift.RedHatOpenShiftManager manager) {
        manager.machinePools()
            .deleteWithResponse("resourceGroup", "resourceName", "childResourceName", com.azure.core.util.Context.NONE);
    }
}
```

### MachinePools_Get

```java
/**
 * Samples for MachinePools Get.
 */
public final class MachinePoolsGetSamples {
    /*
     * x-ms-original-file:
     * specification/redhatopenshift/resource-manager/Microsoft.RedHatOpenShift/openshiftclusters/stable/2023-11-22/
     * examples/MachinePools_Get.json
     */
    /**
     * Sample code: Gets a MachinePool with the specified subscription, resource group and resource name.
     * 
     * @param manager Entry point to RedHatOpenShiftManager.
     */
    public static void getsAMachinePoolWithTheSpecifiedSubscriptionResourceGroupAndResourceName(
        com.azure.resourcemanager.redhatopenshift.RedHatOpenShiftManager manager) {
        manager.machinePools()
            .getWithResponse("resourceGroup", "resourceName", "childResourceName", com.azure.core.util.Context.NONE);
    }
}
```

### MachinePools_List

```java
/**
 * Samples for MachinePools List.
 */
public final class MachinePoolsListSamples {
    /*
     * x-ms-original-file:
     * specification/redhatopenshift/resource-manager/Microsoft.RedHatOpenShift/openshiftclusters/stable/2023-11-22/
     * examples/MachinePools_List.json
     */
    /**
     * Sample code: Lists MachinePools that belong to that Azure Red Hat OpenShift Cluster.
     * 
     * @param manager Entry point to RedHatOpenShiftManager.
     */
    public static void listsMachinePoolsThatBelongToThatAzureRedHatOpenShiftCluster(
        com.azure.resourcemanager.redhatopenshift.RedHatOpenShiftManager manager) {
        manager.machinePools().list("resourceGroup", "resourceName", com.azure.core.util.Context.NONE);
    }
}
```

### MachinePools_Update

```java
import com.azure.resourcemanager.redhatopenshift.models.MachinePool;

/**
 * Samples for MachinePools Update.
 */
public final class MachinePoolsUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/redhatopenshift/resource-manager/Microsoft.RedHatOpenShift/openshiftclusters/stable/2023-11-22/
     * examples/MachinePools_Update.json
     */
    /**
     * Sample code: Updates a MachinePool with the specified subscription, resource group and resource name.
     * 
     * @param manager Entry point to RedHatOpenShiftManager.
     */
    public static void updatesAMachinePoolWithTheSpecifiedSubscriptionResourceGroupAndResourceName(
        com.azure.resourcemanager.redhatopenshift.RedHatOpenShiftManager manager) {
        MachinePool resource = manager.machinePools()
            .getWithResponse("resourceGroup", "resourceName", "childResourceName", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withResources(
                "ewogICAgImFwaVZlcnNpb24iOiAiaGl2ZS5vcGVuc2hpZnQuaW8vdjEiLAogICAgImtpbmQiOiAiTWFjaGluZVBvb2wiLAogICAgIm1ldGFkYXRhIjogewogICAgICAgICJuYW1lIjogInRlc3QtY2x1c3Rlci13b3JrZXIiLAogICAgICAgICJuYW1lc3BhY2UiOiAiYXJvLWY2MGFlOGEyLWJjYTEtNDk4Ny05MDU2LVhYWFhYWFhYWFhYWCIKICAgIH0sCiAgICAic3BlYyI6IHsKICAgICAgICAiY2x1c3RlckRlcGxveW1lbnRSZWYiOiB7CiAgICAgICAgICAgICJuYW1lIjogInRlc3QtY2x1c3RlciIKICAgICAgICB9LAogICAgICAgICJuYW1lIjogIndvcmtlciIsCiAgICAgICAgInBsYXRmb3JtIjogewogICAgICAgICAgICAiYXdzIjogewogICAgICAgICAgICAgICAgInJvb3RWb2x1bWUiOiB7CiAgICAgICAgICAgICAgICAgICAgImlvcHMiOiAwLAogICAgICAgICAgICAgICAgICAgICJzaXplIjogMzAwLAogICAgICAgICAgICAgICAgICAgICJ0eXBlIjogImdwMyIKICAgICAgICAgICAgICAgIH0sCiAgICAgICAgICAgICAgICAidHlwZSI6ICJtNS54bGFyZ2UiLAogICAgICAgICAgICAgICAgInpvbmVzIjogWwogICAgICAgICAgICAgICAgICAgICJ1cy1lYXN0LTFhIgogICAgICAgICAgICAgICAgXQogICAgICAgICAgICB9CiAgICAgICAgfSwKICAgICAgICAicmVwbGljYXMiOiAyCiAgICB9LAogICAgInN0YXR1cyI6IHsKICAgICAgICAiY29uZGl0aW9ucyI6IFsKICAgICAgICBdCiAgICB9Cn0K")
            .apply();
    }
}
```

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
import com.azure.resourcemanager.redhatopenshift.models.MasterProfile;
import com.azure.resourcemanager.redhatopenshift.models.NetworkProfile;
import com.azure.resourcemanager.redhatopenshift.models.PreconfiguredNsg;
import com.azure.resourcemanager.redhatopenshift.models.ServicePrincipalProfile;
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
     * x-ms-original-file:
     * specification/redhatopenshift/resource-manager/Microsoft.RedHatOpenShift/openshiftclusters/stable/2023-11-22/
     * examples/OpenShiftClusters_CreateOrUpdate.json
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
            .withClusterProfile(new ClusterProfile().withPullSecret("fakeTokenPlaceholder")
                .withDomain("cluster.location.aroapp.io")
                .withResourceGroupId("/subscriptions/subscriptionId/resourceGroups/clusterResourceGroup")
                .withFipsValidatedModules(FipsValidatedModules.ENABLED))
            .withConsoleProfile(new ConsoleProfile())
            .withServicePrincipalProfile(
                new ServicePrincipalProfile().withClientId("clientId").withClientSecret("fakeTokenPlaceholder"))
            .withNetworkProfile(new NetworkProfile().withPodCidr("10.128.0.0/14")
                .withServiceCidr("172.30.0.0/16")
                .withLoadBalancerProfile(
                    new LoadBalancerProfile().withManagedOutboundIps(new ManagedOutboundIPs().withCount(1)))
                .withPreconfiguredNsg(PreconfiguredNsg.DISABLED))
            .withMasterProfile(new MasterProfile().withVmSize("Standard_D8s_v3")
                .withSubnetId(
                    "/subscriptions/subscriptionId/resourceGroups/vnetResourceGroup/providers/Microsoft.Network/virtualNetworks/vnet/subnets/master")
                .withEncryptionAtHost(EncryptionAtHost.ENABLED))
            .withWorkerProfiles(Arrays.asList(new WorkerProfile().withName("worker")
                .withVmSize("Standard_D2s_v3")
                .withDiskSizeGB(128)
                .withSubnetId(
                    "/subscriptions/subscriptionId/resourceGroups/vnetResourceGroup/providers/Microsoft.Network/virtualNetworks/vnet/subnets/worker")
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
     * x-ms-original-file:
     * specification/redhatopenshift/resource-manager/Microsoft.RedHatOpenShift/openshiftclusters/stable/2023-11-22/
     * examples/OpenShiftClusters_Delete.json
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
     * x-ms-original-file:
     * specification/redhatopenshift/resource-manager/Microsoft.RedHatOpenShift/openshiftclusters/stable/2023-11-22/
     * examples/OpenShiftClusters_Get.json
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
     * x-ms-original-file:
     * specification/redhatopenshift/resource-manager/Microsoft.RedHatOpenShift/openshiftclusters/stable/2023-11-22/
     * examples/OpenShiftClusters_List.json
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
     * x-ms-original-file:
     * specification/redhatopenshift/resource-manager/Microsoft.RedHatOpenShift/openshiftclusters/stable/2023-11-22/
     * examples/OpenShiftClusters_ListAdminCredentials.json
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
     * x-ms-original-file:
     * specification/redhatopenshift/resource-manager/Microsoft.RedHatOpenShift/openshiftclusters/stable/2023-11-22/
     * examples/OpenShiftClusters_ListByResourceGroup.json
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
     * x-ms-original-file:
     * specification/redhatopenshift/resource-manager/Microsoft.RedHatOpenShift/openshiftclusters/stable/2023-11-22/
     * examples/OpenShiftClusters_ListCredentials.json
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
import com.azure.resourcemanager.redhatopenshift.models.MasterProfile;
import com.azure.resourcemanager.redhatopenshift.models.NetworkProfile;
import com.azure.resourcemanager.redhatopenshift.models.OpenShiftCluster;
import com.azure.resourcemanager.redhatopenshift.models.PreconfiguredNsg;
import com.azure.resourcemanager.redhatopenshift.models.ServicePrincipalProfile;
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
     * x-ms-original-file:
     * specification/redhatopenshift/resource-manager/Microsoft.RedHatOpenShift/openshiftclusters/stable/2023-11-22/
     * examples/OpenShiftClusters_Update.json
     */
    /**
     * Sample code: Updates a OpenShift cluster with the specified subscription, resource group and resource name.
     * 
     * @param manager Entry point to RedHatOpenShiftManager.
     */
    public static void updatesAOpenShiftClusterWithTheSpecifiedSubscriptionResourceGroupAndResourceName(
        com.azure.resourcemanager.redhatopenshift.RedHatOpenShiftManager manager) {
        OpenShiftCluster resource = manager.openShiftClusters()
            .getByResourceGroupWithResponse("resourceGroup", "resourceName", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key", "fakeTokenPlaceholder"))
            .withClusterProfile(new ClusterProfile().withPullSecret("fakeTokenPlaceholder")
                .withDomain("cluster.location.aroapp.io")
                .withResourceGroupId("/subscriptions/subscriptionId/resourceGroups/clusterResourceGroup")
                .withFipsValidatedModules(FipsValidatedModules.ENABLED))
            .withConsoleProfile(new ConsoleProfile())
            .withServicePrincipalProfile(
                new ServicePrincipalProfile().withClientId("clientId").withClientSecret("fakeTokenPlaceholder"))
            .withNetworkProfile(new NetworkProfile().withPodCidr("10.128.0.0/14")
                .withServiceCidr("172.30.0.0/16")
                .withLoadBalancerProfile(
                    new LoadBalancerProfile().withManagedOutboundIps(new ManagedOutboundIPs().withCount(1)))
                .withPreconfiguredNsg(PreconfiguredNsg.DISABLED))
            .withMasterProfile(new MasterProfile().withVmSize("Standard_D8s_v3")
                .withSubnetId(
                    "/subscriptions/subscriptionId/resourceGroups/vnetResourceGroup/providers/Microsoft.Network/virtualNetworks/vnet/subnets/master")
                .withEncryptionAtHost(EncryptionAtHost.ENABLED))
            .withWorkerProfiles(Arrays.asList(new WorkerProfile().withName("worker")
                .withVmSize("Standard_D2s_v3")
                .withDiskSizeGB(128)
                .withSubnetId(
                    "/subscriptions/subscriptionId/resourceGroups/vnetResourceGroup/providers/Microsoft.Network/virtualNetworks/vnet/subnets/worker")
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

### OpenShiftVersions_List

```java
/**
 * Samples for OpenShiftVersions List.
 */
public final class OpenShiftVersionsListSamples {
    /*
     * x-ms-original-file:
     * specification/redhatopenshift/resource-manager/Microsoft.RedHatOpenShift/openshiftclusters/stable/2023-11-22/
     * examples/OpenShiftVersions_List.json
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
     * x-ms-original-file:
     * specification/redhatopenshift/resource-manager/Microsoft.RedHatOpenShift/openshiftclusters/stable/2023-11-22/
     * examples/Operations_List.json
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

### Secrets_CreateOrUpdate

```java
/**
 * Samples for Secrets CreateOrUpdate.
 */
public final class SecretsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/redhatopenshift/resource-manager/Microsoft.RedHatOpenShift/openshiftclusters/stable/2023-11-22/
     * examples/Secrets_CreateOrUpdate.json
     */
    /**
     * Sample code: Creates or updates a Secret with the specified subscription, resource group and resource name.
     * 
     * @param manager Entry point to RedHatOpenShiftManager.
     */
    public static void createsOrUpdatesASecretWithTheSpecifiedSubscriptionResourceGroupAndResourceName(
        com.azure.resourcemanager.redhatopenshift.RedHatOpenShiftManager manager) {
        manager.secrets()
            .define("childResourceName")
            .withExistingOpenshiftcluster("resourceGroup", "resourceName")
            .create();
    }
}
```

### Secrets_Delete

```java
/**
 * Samples for Secrets Delete.
 */
public final class SecretsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/redhatopenshift/resource-manager/Microsoft.RedHatOpenShift/openshiftclusters/stable/2023-11-22/
     * examples/Secrets_Delete.json
     */
    /**
     * Sample code: Deletes a Secret with the specified subscription, resource group and resource name.
     * 
     * @param manager Entry point to RedHatOpenShiftManager.
     */
    public static void deletesASecretWithTheSpecifiedSubscriptionResourceGroupAndResourceName(
        com.azure.resourcemanager.redhatopenshift.RedHatOpenShiftManager manager) {
        manager.secrets()
            .deleteWithResponse("resourceGroup", "resourceName", "childResourceName", com.azure.core.util.Context.NONE);
    }
}
```

### Secrets_Get

```java
/**
 * Samples for Secrets Get.
 */
public final class SecretsGetSamples {
    /*
     * x-ms-original-file:
     * specification/redhatopenshift/resource-manager/Microsoft.RedHatOpenShift/openshiftclusters/stable/2023-11-22/
     * examples/Secrets_Get.json
     */
    /**
     * Sample code: Gets a Secret with the specified subscription, resource group and resource name.
     * 
     * @param manager Entry point to RedHatOpenShiftManager.
     */
    public static void getsASecretWithTheSpecifiedSubscriptionResourceGroupAndResourceName(
        com.azure.resourcemanager.redhatopenshift.RedHatOpenShiftManager manager) {
        manager.secrets()
            .getWithResponse("resourceGroup", "resourceName", "childResourceName", com.azure.core.util.Context.NONE);
    }
}
```

### Secrets_List

```java
/**
 * Samples for Secrets List.
 */
public final class SecretsListSamples {
    /*
     * x-ms-original-file:
     * specification/redhatopenshift/resource-manager/Microsoft.RedHatOpenShift/openshiftclusters/stable/2023-11-22/
     * examples/Secrets_List.json
     */
    /**
     * Sample code: Lists Secrets that belong to that Azure Red Hat OpenShift Cluster.
     * 
     * @param manager Entry point to RedHatOpenShiftManager.
     */
    public static void listsSecretsThatBelongToThatAzureRedHatOpenShiftCluster(
        com.azure.resourcemanager.redhatopenshift.RedHatOpenShiftManager manager) {
        manager.secrets().list("resourceGroup", "resourceName", com.azure.core.util.Context.NONE);
    }
}
```

### Secrets_Update

```java
import com.azure.resourcemanager.redhatopenshift.models.Secret;

/**
 * Samples for Secrets Update.
 */
public final class SecretsUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/redhatopenshift/resource-manager/Microsoft.RedHatOpenShift/openshiftclusters/stable/2023-11-22/
     * examples/Secrets_Update.json
     */
    /**
     * Sample code: Updates a Secret with the specified subscription, resource group and resource name.
     * 
     * @param manager Entry point to RedHatOpenShiftManager.
     */
    public static void updatesASecretWithTheSpecifiedSubscriptionResourceGroupAndResourceName(
        com.azure.resourcemanager.redhatopenshift.RedHatOpenShiftManager manager) {
        Secret resource = manager.secrets()
            .getWithResponse("resourceGroup", "resourceName", "childResourceName", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().apply();
    }
}
```

### SyncIdentityProviders_CreateOrUpdate

```java
/**
 * Samples for SyncIdentityProviders CreateOrUpdate.
 */
public final class SyncIdentityProvidersCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/redhatopenshift/resource-manager/Microsoft.RedHatOpenShift/openshiftclusters/stable/2023-11-22/
     * examples/SyncIdentityProviders_CreateOrUpdate.json
     */
    /**
     * Sample code: Creates or updates a SyncIdentityProvider with the specified subscription, resource group and
     * resource name.
     * 
     * @param manager Entry point to RedHatOpenShiftManager.
     */
    public static void createsOrUpdatesASyncIdentityProviderWithTheSpecifiedSubscriptionResourceGroupAndResourceName(
        com.azure.resourcemanager.redhatopenshift.RedHatOpenShiftManager manager) {
        manager.syncIdentityProviders()
            .define("childResourceName")
            .withExistingOpenshiftcluster("resourceGroup", "resourceName")
            .withResources(
                "ewogICAgImFwaVZlcnNpb24iOiAiaGl2ZS5vcGVuc2hpZnQuaW8vdjEiLAogICAgImtpbmQiOiAiU3luY0lkZW50aXR5UHJvdmlkZXIiLAogICAgIm1ldGFkYXRhIjogewogICAgICAgICJuYW1lIjogInRlc3QtY2x1c3RlciIsCiAgICAgICAgIm5hbWVzcGFjZSI6ICJhcm8tZjYwYWU4YTItYmNhMS00OTg3LTkwNTYtWFhYWFhYWFhYWFhYIgogICAgfSwKICAgICJzcGVjIjogewogICAgICAgICJjbHVzdGVyRGVwbG95bWVudFJlZnMiOiBbCiAgICAgICAgICAgIHsKICAgICAgICAgICAgICAgICJuYW1lIjogInRlc3QtY2x1c3RlciIKICAgICAgICAgICAgfQogICAgICAgIF0sCiAgICAgICAgImlkZW50aXR5UHJvdmlkZXJzIjogWwogICAgICAgICAgICB7CiAgICAgICAgICAgICAgICAiaHRwYXNzd2QiOiB7CiAgICAgICAgICAgICAgICAgICAgImZpbGVEYXRhIjogewogICAgICAgICAgICAgICAgICAgICAgICAibmFtZSI6ICJodHBhc3N3ZC1zZWNyZXQiCiAgICAgICAgICAgICAgICAgICAgfQogICAgICAgICAgICAgICAgfSwKICAgICAgICAgICAgICAgICJtYXBwaW5nTWV0aG9kIjogImNsYWltIiwKICAgICAgICAgICAgICAgICJuYW1lIjogIkhUUGFzc3dkIiwKICAgICAgICAgICAgICAgICJ0eXBlIjogIkhUUGFzc3dkIgogICAgICAgICAgICB9CiAgICAgICAgXQogICAgfSwKICAgICJzdGF0dXMiOiB7fQp9Cg==")
            .create();
    }
}
```

### SyncIdentityProviders_Delete

```java
/**
 * Samples for SyncIdentityProviders Delete.
 */
public final class SyncIdentityProvidersDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/redhatopenshift/resource-manager/Microsoft.RedHatOpenShift/openshiftclusters/stable/2023-11-22/
     * examples/SyncIdentityProviders_Delete.json
     */
    /**
     * Sample code: Deletes a SyncIdentityProvider with the specified subscription, resource group and resource name.
     * 
     * @param manager Entry point to RedHatOpenShiftManager.
     */
    public static void deletesASyncIdentityProviderWithTheSpecifiedSubscriptionResourceGroupAndResourceName(
        com.azure.resourcemanager.redhatopenshift.RedHatOpenShiftManager manager) {
        manager.syncIdentityProviders()
            .deleteWithResponse("resourceGroup", "resourceName", "childResourceName", com.azure.core.util.Context.NONE);
    }
}
```

### SyncIdentityProviders_Get

```java
/**
 * Samples for SyncIdentityProviders Get.
 */
public final class SyncIdentityProvidersGetSamples {
    /*
     * x-ms-original-file:
     * specification/redhatopenshift/resource-manager/Microsoft.RedHatOpenShift/openshiftclusters/stable/2023-11-22/
     * examples/SyncIdentityProviders_Get.json
     */
    /**
     * Sample code: Gets a SyncIdentityProvider with the specified subscription, resource group and resource name.
     * 
     * @param manager Entry point to RedHatOpenShiftManager.
     */
    public static void getsASyncIdentityProviderWithTheSpecifiedSubscriptionResourceGroupAndResourceName(
        com.azure.resourcemanager.redhatopenshift.RedHatOpenShiftManager manager) {
        manager.syncIdentityProviders()
            .getWithResponse("resourceGroup", "resourceName", "childResourceName", com.azure.core.util.Context.NONE);
    }
}
```

### SyncIdentityProviders_List

```java
/**
 * Samples for SyncIdentityProviders List.
 */
public final class SyncIdentityProvidersListSamples {
    /*
     * x-ms-original-file:
     * specification/redhatopenshift/resource-manager/Microsoft.RedHatOpenShift/openshiftclusters/stable/2023-11-22/
     * examples/SyncIdentityProviders_List.json
     */
    /**
     * Sample code: Lists SyncIdentityProviders that belong to that Azure Red Hat OpenShift Cluster.
     * 
     * @param manager Entry point to RedHatOpenShiftManager.
     */
    public static void listsSyncIdentityProvidersThatBelongToThatAzureRedHatOpenShiftCluster(
        com.azure.resourcemanager.redhatopenshift.RedHatOpenShiftManager manager) {
        manager.syncIdentityProviders().list("resourceGroup", "resourceName", com.azure.core.util.Context.NONE);
    }
}
```

### SyncIdentityProviders_Update

```java
import com.azure.resourcemanager.redhatopenshift.models.SyncIdentityProvider;

/**
 * Samples for SyncIdentityProviders Update.
 */
public final class SyncIdentityProvidersUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/redhatopenshift/resource-manager/Microsoft.RedHatOpenShift/openshiftclusters/stable/2023-11-22/
     * examples/SyncIdentityProviders_Update.json
     */
    /**
     * Sample code: Updates a SyncIdentityProvider with the specified subscription, resource group and resource name.
     * 
     * @param manager Entry point to RedHatOpenShiftManager.
     */
    public static void updatesASyncIdentityProviderWithTheSpecifiedSubscriptionResourceGroupAndResourceName(
        com.azure.resourcemanager.redhatopenshift.RedHatOpenShiftManager manager) {
        SyncIdentityProvider resource = manager.syncIdentityProviders()
            .getWithResponse("resourceGroup", "resourceName", "childResourceName", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withResources(
                "ewogICAgImFwaVZlcnNpb24iOiAiaGl2ZS5vcGVuc2hpZnQuaW8vdjEiLAogICAgImtpbmQiOiAiU3luY0lkZW50aXR5UHJvdmlkZXIiLAogICAgIm1ldGFkYXRhIjogewogICAgICAgICJuYW1lIjogInRlc3QtY2x1c3RlciIsCiAgICAgICAgIm5hbWVzcGFjZSI6ICJhcm8tZjYwYWU4YTItYmNhMS00OTg3LTkwNTYtWFhYWFhYWFhYWFhYIgogICAgfSwKICAgICJzcGVjIjogewogICAgICAgICJjbHVzdGVyRGVwbG95bWVudFJlZnMiOiBbCiAgICAgICAgICAgIHsKICAgICAgICAgICAgICAgICJuYW1lIjogInRlc3QtY2x1c3RlciIKICAgICAgICAgICAgfQogICAgICAgIF0sCiAgICAgICAgImlkZW50aXR5UHJvdmlkZXJzIjogWwogICAgICAgICAgICB7CiAgICAgICAgICAgICAgICAiaHRwYXNzd2QiOiB7CiAgICAgICAgICAgICAgICAgICAgImZpbGVEYXRhIjogewogICAgICAgICAgICAgICAgICAgICAgICAibmFtZSI6ICJodHBhc3N3ZC1zZWNyZXQiCiAgICAgICAgICAgICAgICAgICAgfQogICAgICAgICAgICAgICAgfSwKICAgICAgICAgICAgICAgICJtYXBwaW5nTWV0aG9kIjogImNsYWltIiwKICAgICAgICAgICAgICAgICJuYW1lIjogIkhUUGFzc3dkIiwKICAgICAgICAgICAgICAgICJ0eXBlIjogIkhUUGFzc3dkIgogICAgICAgICAgICB9CiAgICAgICAgXQogICAgfSwKICAgICJzdGF0dXMiOiB7fQp9Cg==")
            .apply();
    }
}
```

### SyncSets_CreateOrUpdate

```java
/**
 * Samples for SyncSets CreateOrUpdate.
 */
public final class SyncSetsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/redhatopenshift/resource-manager/Microsoft.RedHatOpenShift/openshiftclusters/stable/2023-11-22/
     * examples/SyncSets_CreateOrUpdate.json
     */
    /**
     * Sample code: Creates or updates a SyncSet with the specified subscription, resource group and resource name.
     * 
     * @param manager Entry point to RedHatOpenShiftManager.
     */
    public static void createsOrUpdatesASyncSetWithTheSpecifiedSubscriptionResourceGroupAndResourceName(
        com.azure.resourcemanager.redhatopenshift.RedHatOpenShiftManager manager) {
        manager.syncSets()
            .define("childResourceName")
            .withExistingOpenshiftcluster("resourceGroup", "resourceName")
            .withResources(
                "eyAKICAiYXBpVmVyc2lvbiI6ICJoaXZlLm9wZW5zaGlmdC5pby92MSIsCiAgImtpbmQiOiAiU3luY1NldCIsCiAgIm1ldGFkYXRhIjogewogICAgIm5hbWUiOiAic2FtcGxlIiwKICAgICJuYW1lc3BhY2UiOiAiYXJvLWY2MGFlOGEyLWJjYTEtNDk4Ny05MDU2LWYyZjZhMTgzN2NhYSIKICB9LAogICJzcGVjIjogewogICAgImNsdXN0ZXJEZXBsb3ltZW50UmVmcyI6IFtdLAogICAgInJlc291cmNlcyI6IFsKICAgICAgewogICAgICAgICJhcGlWZXJzaW9uIjogInYxIiwKICAgICAgICAia2luZCI6ICJDb25maWdNYXAiLAogICAgICAgICJtZXRhZGF0YSI6IHsKICAgICAgICAgICJuYW1lIjogIm15Y29uZmlnbWFwIgogICAgICAgIH0KICAgICAgfQogICAgXQogIH0KfQo=")
            .create();
    }
}
```

### SyncSets_Delete

```java
/**
 * Samples for SyncSets Delete.
 */
public final class SyncSetsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/redhatopenshift/resource-manager/Microsoft.RedHatOpenShift/openshiftclusters/stable/2023-11-22/
     * examples/SyncSets_Delete.json
     */
    /**
     * Sample code: Deletes a SyncSet with the specified subscription, resource group and resource name.
     * 
     * @param manager Entry point to RedHatOpenShiftManager.
     */
    public static void deletesASyncSetWithTheSpecifiedSubscriptionResourceGroupAndResourceName(
        com.azure.resourcemanager.redhatopenshift.RedHatOpenShiftManager manager) {
        manager.syncSets()
            .deleteWithResponse("resourceGroup", "resourceName", "childResourceName", com.azure.core.util.Context.NONE);
    }
}
```

### SyncSets_Get

```java
/**
 * Samples for SyncSets Get.
 */
public final class SyncSetsGetSamples {
    /*
     * x-ms-original-file:
     * specification/redhatopenshift/resource-manager/Microsoft.RedHatOpenShift/openshiftclusters/stable/2023-11-22/
     * examples/SyncSets_Get.json
     */
    /**
     * Sample code: Gets a SyncSet with the specified subscription, resource group and resource name.
     * 
     * @param manager Entry point to RedHatOpenShiftManager.
     */
    public static void getsASyncSetWithTheSpecifiedSubscriptionResourceGroupAndResourceName(
        com.azure.resourcemanager.redhatopenshift.RedHatOpenShiftManager manager) {
        manager.syncSets()
            .getWithResponse("resourceGroup", "resourceName", "childResourceName", com.azure.core.util.Context.NONE);
    }
}
```

### SyncSets_List

```java
/**
 * Samples for SyncSets List.
 */
public final class SyncSetsListSamples {
    /*
     * x-ms-original-file:
     * specification/redhatopenshift/resource-manager/Microsoft.RedHatOpenShift/openshiftclusters/stable/2023-11-22/
     * examples/SyncSets_List.json
     */
    /**
     * Sample code: Lists SyncSets that belong to that Azure Red Hat OpenShift Cluster.
     * 
     * @param manager Entry point to RedHatOpenShiftManager.
     */
    public static void listsSyncSetsThatBelongToThatAzureRedHatOpenShiftCluster(
        com.azure.resourcemanager.redhatopenshift.RedHatOpenShiftManager manager) {
        manager.syncSets().list("resourceGroup", "resourceName", com.azure.core.util.Context.NONE);
    }
}
```

### SyncSets_Update

```java
import com.azure.resourcemanager.redhatopenshift.models.SyncSet;

/**
 * Samples for SyncSets Update.
 */
public final class SyncSetsUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/redhatopenshift/resource-manager/Microsoft.RedHatOpenShift/openshiftclusters/stable/2023-11-22/
     * examples/SyncSets_Update.json
     */
    /**
     * Sample code: Updates a SyncSet with the specified subscription, resource group and resource name.
     * 
     * @param manager Entry point to RedHatOpenShiftManager.
     */
    public static void updatesASyncSetWithTheSpecifiedSubscriptionResourceGroupAndResourceName(
        com.azure.resourcemanager.redhatopenshift.RedHatOpenShiftManager manager) {
        SyncSet resource = manager.syncSets()
            .getWithResponse("resourceGroup", "resourceName", "childResourceName", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withResources(
                "eyAKICAiYXBpVmVyc2lvbiI6ICJoaXZlLm9wZW5zaGlmdC5pby92MSIsCiAgImtpbmQiOiAiU3luY1NldCIsCiAgIm1ldGFkYXRhIjogewogICAgIm5hbWUiOiAic2FtcGxlIiwKICAgICJuYW1lc3BhY2UiOiAiYXJvLWY2MGFlOGEyLWJjYTEtNDk4Ny05MDU2LWYyZjZhMTgzN2NhYSIKICB9LAogICJzcGVjIjogewogICAgImNsdXN0ZXJEZXBsb3ltZW50UmVmcyI6IFtdLAogICAgInJlc291cmNlcyI6IFsKICAgICAgewogICAgICAgICJhcGlWZXJzaW9uIjogInYxIiwKICAgICAgICAia2luZCI6ICJDb25maWdNYXAiLAogICAgICAgICJtZXRhZGF0YSI6IHsKICAgICAgICAgICJuYW1lIjogIm15Y29uZmlnbWFwIgogICAgICAgIH0KICAgICAgfQogICAgXQogIH0KfQo=")
            .apply();
    }
}
```

