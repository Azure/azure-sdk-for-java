# Code snippets and samples


## Addons

- [CreateOrUpdate](#addons_createorupdate)
- [Delete](#addons_delete)
- [Get](#addons_get)
- [List](#addons_list)

## Authorizations

- [CreateOrUpdate](#authorizations_createorupdate)
- [Delete](#authorizations_delete)
- [Get](#authorizations_get)
- [List](#authorizations_list)

## CloudLinks

- [CreateOrUpdate](#cloudlinks_createorupdate)
- [Delete](#cloudlinks_delete)
- [Get](#cloudlinks_get)
- [List](#cloudlinks_list)

## Clusters

- [CreateOrUpdate](#clusters_createorupdate)
- [Delete](#clusters_delete)
- [Get](#clusters_get)
- [List](#clusters_list)
- [ListZones](#clusters_listzones)
- [Update](#clusters_update)

## Datastores

- [CreateOrUpdate](#datastores_createorupdate)
- [Delete](#datastores_delete)
- [Get](#datastores_get)
- [List](#datastores_list)

## GlobalReachConnections

- [CreateOrUpdate](#globalreachconnections_createorupdate)
- [Delete](#globalreachconnections_delete)
- [Get](#globalreachconnections_get)
- [List](#globalreachconnections_list)

## HcxEnterpriseSites

- [CreateOrUpdate](#hcxenterprisesites_createorupdate)
- [Delete](#hcxenterprisesites_delete)
- [Get](#hcxenterprisesites_get)
- [List](#hcxenterprisesites_list)

## IscsiPaths

- [CreateOrUpdate](#iscsipaths_createorupdate)
- [Delete](#iscsipaths_delete)
- [Get](#iscsipaths_get)
- [ListByPrivateCloud](#iscsipaths_listbyprivatecloud)

## Locations

- [CheckQuotaAvailability](#locations_checkquotaavailability)
- [CheckTrialAvailability](#locations_checktrialavailability)

## Operations

- [List](#operations_list)

## PlacementPolicies

- [CreateOrUpdate](#placementpolicies_createorupdate)
- [Delete](#placementpolicies_delete)
- [Get](#placementpolicies_get)
- [List](#placementpolicies_list)
- [Update](#placementpolicies_update)

## PrivateClouds

- [CreateOrUpdate](#privateclouds_createorupdate)
- [Delete](#privateclouds_delete)
- [GetByResourceGroup](#privateclouds_getbyresourcegroup)
- [List](#privateclouds_list)
- [ListAdminCredentials](#privateclouds_listadmincredentials)
- [ListByResourceGroup](#privateclouds_listbyresourcegroup)
- [RotateNsxtPassword](#privateclouds_rotatensxtpassword)
- [RotateVcenterPassword](#privateclouds_rotatevcenterpassword)
- [Update](#privateclouds_update)

## ScriptCmdlets

- [Get](#scriptcmdlets_get)
- [List](#scriptcmdlets_list)

## ScriptExecutions

- [CreateOrUpdate](#scriptexecutions_createorupdate)
- [Delete](#scriptexecutions_delete)
- [Get](#scriptexecutions_get)
- [GetExecutionLogs](#scriptexecutions_getexecutionlogs)
- [List](#scriptexecutions_list)

## ScriptPackages

- [Get](#scriptpackages_get)
- [List](#scriptpackages_list)

## VirtualMachines

- [Get](#virtualmachines_get)
- [List](#virtualmachines_list)
- [RestrictMovement](#virtualmachines_restrictmovement)

## WorkloadNetworks

- [CreateDhcp](#workloadnetworks_createdhcp)
- [CreateDnsService](#workloadnetworks_creatednsservice)
- [CreateDnsZone](#workloadnetworks_creatednszone)
- [CreatePortMirroring](#workloadnetworks_createportmirroring)
- [CreatePublicIP](#workloadnetworks_createpublicip)
- [CreateSegment](#workloadnetworks_createsegment)
- [CreateVMGroup](#workloadnetworks_createvmgroup)
- [DeleteDhcp](#workloadnetworks_deletedhcp)
- [DeleteDnsService](#workloadnetworks_deletednsservice)
- [DeleteDnsZone](#workloadnetworks_deletednszone)
- [DeletePortMirroring](#workloadnetworks_deleteportmirroring)
- [DeletePublicIP](#workloadnetworks_deletepublicip)
- [DeleteSegment](#workloadnetworks_deletesegment)
- [DeleteVMGroup](#workloadnetworks_deletevmgroup)
- [Get](#workloadnetworks_get)
- [GetDhcp](#workloadnetworks_getdhcp)
- [GetDnsService](#workloadnetworks_getdnsservice)
- [GetDnsZone](#workloadnetworks_getdnszone)
- [GetGateway](#workloadnetworks_getgateway)
- [GetPortMirroring](#workloadnetworks_getportmirroring)
- [GetPublicIP](#workloadnetworks_getpublicip)
- [GetSegment](#workloadnetworks_getsegment)
- [GetVMGroup](#workloadnetworks_getvmgroup)
- [GetVirtualMachine](#workloadnetworks_getvirtualmachine)
- [List](#workloadnetworks_list)
- [ListDhcp](#workloadnetworks_listdhcp)
- [ListDnsServices](#workloadnetworks_listdnsservices)
- [ListDnsZones](#workloadnetworks_listdnszones)
- [ListGateways](#workloadnetworks_listgateways)
- [ListPortMirroring](#workloadnetworks_listportmirroring)
- [ListPublicIPs](#workloadnetworks_listpublicips)
- [ListSegments](#workloadnetworks_listsegments)
- [ListVMGroups](#workloadnetworks_listvmgroups)
- [ListVirtualMachines](#workloadnetworks_listvirtualmachines)
- [UpdateDnsService](#workloadnetworks_updatednsservice)
- [UpdateDnsZone](#workloadnetworks_updatednszone)
- [UpdatePortMirroring](#workloadnetworks_updateportmirroring)
- [UpdateSegment](#workloadnetworks_updatesegment)
- [UpdateVMGroup](#workloadnetworks_updatevmgroup)
### Addons_CreateOrUpdate

```java
import com.azure.resourcemanager.avs.models.AddonArcProperties;

/**
 * Samples for Addons CreateOrUpdate.
 */
public final class AddonsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/Addons_CreateOrUpdate_ArcReg.json
     */
    /**
     * Sample code: Addons_CreateOrUpdate_ArcReg.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void addonsCreateOrUpdateArcReg(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.addons()
            .define("arc")
            .withExistingPrivateCloud("group1", "cloud1")
            .withProperties(new AddonArcProperties().withVCenter(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg_test/providers/Microsoft.ConnectedVMwarevSphere/VCenters/test-vcenter"))
            .create();
    }
}
```

### Addons_Delete

```java
/**
 * Samples for Addons Delete.
 */
public final class AddonsDeleteSamples {
    /*
     * x-ms-original-file: specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/Addons_Delete.json
     */
    /**
     * Sample code: Addons_Delete.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void addonsDelete(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.addons().delete("group1", "cloud1", "srm", com.azure.core.util.Context.NONE);
    }
}
```

### Addons_Get

```java
/**
 * Samples for Addons Get.
 */
public final class AddonsGetSamples {
    /*
     * x-ms-original-file: specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/Addons_Get_ArcReg.json
     */
    /**
     * Sample code: Addons_Get_ArcReg.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void addonsGetArcReg(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.addons().getWithResponse("group1", "cloud1", "arc", com.azure.core.util.Context.NONE);
    }
}
```

### Addons_List

```java
/**
 * Samples for Addons List.
 */
public final class AddonsListSamples {
    /*
     * x-ms-original-file: specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/Addons_List.json
     */
    /**
     * Sample code: Addons_List.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void addonsList(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.addons().list("group1", "cloud1", com.azure.core.util.Context.NONE);
    }
}
```

### Authorizations_CreateOrUpdate

```java
/**
 * Samples for Authorizations CreateOrUpdate.
 */
public final class AuthorizationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/Authorizations_CreateOrUpdate.json
     */
    /**
     * Sample code: Authorizations_CreateOrUpdate.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void authorizationsCreateOrUpdate(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.authorizations().define("authorization1").withExistingPrivateCloud("group1", "cloud1").create();
    }
}
```

### Authorizations_Delete

```java
/**
 * Samples for Authorizations Delete.
 */
public final class AuthorizationsDeleteSamples {
    /*
     * x-ms-original-file: specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/Authorizations_Delete.json
     */
    /**
     * Sample code: Authorizations_Delete.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void authorizationsDelete(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.authorizations().delete("group1", "cloud1", "authorization1", com.azure.core.util.Context.NONE);
    }
}
```

### Authorizations_Get

```java
/**
 * Samples for Authorizations Get.
 */
public final class AuthorizationsGetSamples {
    /*
     * x-ms-original-file: specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/Authorizations_Get.json
     */
    /**
     * Sample code: Authorizations_Get.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void authorizationsGet(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.authorizations()
            .getWithResponse("group1", "cloud1", "authorization1", com.azure.core.util.Context.NONE);
    }
}
```

### Authorizations_List

```java
/**
 * Samples for Authorizations List.
 */
public final class AuthorizationsListSamples {
    /*
     * x-ms-original-file: specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/Authorizations_List.json
     */
    /**
     * Sample code: Authorizations_List.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void authorizationsList(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.authorizations().list("group1", "cloud1", com.azure.core.util.Context.NONE);
    }
}
```

### CloudLinks_CreateOrUpdate

```java
/**
 * Samples for CloudLinks CreateOrUpdate.
 */
public final class CloudLinksCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/CloudLinks_CreateOrUpdate.json
     */
    /**
     * Sample code: CloudLinks_CreateOrUpdate.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void cloudLinksCreateOrUpdate(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.cloudLinks()
            .define("cloudLink1")
            .withExistingPrivateCloud("group1", "cloud1")
            .withLinkedCloud(
                "/subscriptions/12341234-1234-1234-1234-123412341234/resourceGroups/mygroup/providers/Microsoft.AVS/privateClouds/cloud2")
            .create();
    }
}
```

### CloudLinks_Delete

```java
/**
 * Samples for CloudLinks Delete.
 */
public final class CloudLinksDeleteSamples {
    /*
     * x-ms-original-file: specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/CloudLinks_Delete.json
     */
    /**
     * Sample code: CloudLinks_Delete.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void cloudLinksDelete(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.cloudLinks().delete("group1", "cloud1", "cloudLink1", com.azure.core.util.Context.NONE);
    }
}
```

### CloudLinks_Get

```java
/**
 * Samples for CloudLinks Get.
 */
public final class CloudLinksGetSamples {
    /*
     * x-ms-original-file: specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/CloudLinks_Get.json
     */
    /**
     * Sample code: CloudLinks_Get.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void cloudLinksGet(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.cloudLinks().getWithResponse("group1", "cloud1", "cloudLink1", com.azure.core.util.Context.NONE);
    }
}
```

### CloudLinks_List

```java
/**
 * Samples for CloudLinks List.
 */
public final class CloudLinksListSamples {
    /*
     * x-ms-original-file: specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/CloudLinks_List.json
     */
    /**
     * Sample code: CloudLinks_List.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void cloudLinksList(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.cloudLinks().list("group1", "cloud1", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_CreateOrUpdate

```java
import com.azure.resourcemanager.avs.models.Sku;

/**
 * Samples for Clusters CreateOrUpdate.
 */
public final class ClustersCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/Clusters_CreateOrUpdate.json
     */
    /**
     * Sample code: Clusters_CreateOrUpdate.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void clustersCreateOrUpdate(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.clusters()
            .define("cluster1")
            .withExistingPrivateCloud("group1", "cloud1")
            .withSku(new Sku().withName("AV20"))
            .withClusterSize(3)
            .create();
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
     * x-ms-original-file: specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/Clusters_Delete.json
     */
    /**
     * Sample code: Clusters_Delete.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void clustersDelete(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.clusters().delete("group1", "cloud1", "cluster1", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_Get

```java
/**
 * Samples for Clusters Get.
 */
public final class ClustersGetSamples {
    /*
     * x-ms-original-file: specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/Clusters_Get.json
     */
    /**
     * Sample code: Clusters_Get.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void clustersGet(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.clusters().getWithResponse("group1", "cloud1", "cluster1", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/Clusters_List.json
     */
    /**
     * Sample code: Clusters_List.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void clustersList(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.clusters().list("group1", "cloud1", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_ListZones

```java
/**
 * Samples for Clusters ListZones.
 */
public final class ClustersListZonesSamples {
    /*
     * x-ms-original-file: specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/Clusters_ListZones.json
     */
    /**
     * Sample code: Clusters_ListZones.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void clustersListZones(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.clusters().listZonesWithResponse("group1", "cloud1", "cluster1", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_Update

```java
import com.azure.resourcemanager.avs.models.Cluster;

/**
 * Samples for Clusters Update.
 */
public final class ClustersUpdateSamples {
    /*
     * x-ms-original-file: specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/Clusters_Update.json
     */
    /**
     * Sample code: Clusters_Update.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void clustersUpdate(com.azure.resourcemanager.avs.AvsManager manager) {
        Cluster resource = manager.clusters()
            .getWithResponse("group1", "cloud1", "cluster1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withClusterSize(4).apply();
    }
}
```

### Datastores_CreateOrUpdate

```java
import com.azure.resourcemanager.avs.models.NetAppVolume;

/**
 * Samples for Datastores CreateOrUpdate.
 */
public final class DatastoresCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/Datastores_CreateOrUpdate.json
     */
    /**
     * Sample code: Datastores_CreateOrUpdate.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void datastoresCreateOrUpdate(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.datastores()
            .define("datastore1")
            .withExistingCluster("group1", "cloud1", "cluster1")
            .withNetAppVolume(new NetAppVolume().withId(
                "/subscriptions/11111111-1111-1111-1111-111111111111/resourceGroups/ResourceGroup1/providers/Microsoft.NetApp/netAppAccounts/NetAppAccount1/capacityPools/CapacityPool1/volumes/NFSVol1"))
            .create();
    }
}
```

### Datastores_Delete

```java
/**
 * Samples for Datastores Delete.
 */
public final class DatastoresDeleteSamples {
    /*
     * x-ms-original-file: specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/Datastores_Delete.json
     */
    /**
     * Sample code: Datastores_Delete.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void datastoresDelete(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.datastores().delete("group1", "cloud1", "cluster1", "datastore1", com.azure.core.util.Context.NONE);
    }
}
```

### Datastores_Get

```java
/**
 * Samples for Datastores Get.
 */
public final class DatastoresGetSamples {
    /*
     * x-ms-original-file: specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/Datastores_Get.json
     */
    /**
     * Sample code: Datastores_Get.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void datastoresGet(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.datastores()
            .getWithResponse("group1", "cloud1", "cluster1", "datastore1", com.azure.core.util.Context.NONE);
    }
}
```

### Datastores_List

```java
/**
 * Samples for Datastores List.
 */
public final class DatastoresListSamples {
    /*
     * x-ms-original-file: specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/Datastores_List.json
     */
    /**
     * Sample code: Datastores_List.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void datastoresList(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.datastores().list("group1", "cloud1", "cluster1", com.azure.core.util.Context.NONE);
    }
}
```

### GlobalReachConnections_CreateOrUpdate

```java
/**
 * Samples for GlobalReachConnections CreateOrUpdate.
 */
public final class GlobalReachConnectionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/GlobalReachConnections_CreateOrUpdate.json
     */
    /**
     * Sample code: GlobalReachConnections_CreateOrUpdate.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void globalReachConnectionsCreateOrUpdate(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.globalReachConnections()
            .define("connection1")
            .withExistingPrivateCloud("group1", "cloud1")
            .withAuthorizationKey("01010101-0101-0101-0101-010101010101")
            .withPeerExpressRouteCircuit(
                "/subscriptions/12341234-1234-1234-1234-123412341234/resourceGroups/mygroup/providers/Microsoft.Network/expressRouteCircuits/mypeer")
            .create();
    }
}
```

### GlobalReachConnections_Delete

```java
/**
 * Samples for GlobalReachConnections Delete.
 */
public final class GlobalReachConnectionsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/GlobalReachConnections_Delete.json
     */
    /**
     * Sample code: GlobalReachConnections_Delete.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void globalReachConnectionsDelete(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.globalReachConnections().delete("group1", "cloud1", "connection1", com.azure.core.util.Context.NONE);
    }
}
```

### GlobalReachConnections_Get

```java
/**
 * Samples for GlobalReachConnections Get.
 */
public final class GlobalReachConnectionsGetSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/GlobalReachConnections_Get.json
     */
    /**
     * Sample code: GlobalReachConnections_Get.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void globalReachConnectionsGet(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.globalReachConnections()
            .getWithResponse("group1", "cloud1", "connection1", com.azure.core.util.Context.NONE);
    }
}
```

### GlobalReachConnections_List

```java
/**
 * Samples for GlobalReachConnections List.
 */
public final class GlobalReachConnectionsListSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/GlobalReachConnections_List.json
     */
    /**
     * Sample code: GlobalReachConnections_List.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void globalReachConnectionsList(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.globalReachConnections().list("group1", "cloud1", com.azure.core.util.Context.NONE);
    }
}
```

### HcxEnterpriseSites_CreateOrUpdate

```java
/**
 * Samples for HcxEnterpriseSites CreateOrUpdate.
 */
public final class HcxEnterpriseSitesCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/HcxEnterpriseSites_CreateOrUpdate.json
     */
    /**
     * Sample code: HcxEnterpriseSites_CreateOrUpdate.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void hcxEnterpriseSitesCreateOrUpdate(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.hcxEnterpriseSites().define("site1").withExistingPrivateCloud("group1", "cloud1").create();
    }
}
```

### HcxEnterpriseSites_Delete

```java
/**
 * Samples for HcxEnterpriseSites Delete.
 */
public final class HcxEnterpriseSitesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/HcxEnterpriseSites_Delete.json
     */
    /**
     * Sample code: HcxEnterpriseSites_Delete.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void hcxEnterpriseSitesDelete(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.hcxEnterpriseSites().deleteWithResponse("group1", "cloud1", "site1", com.azure.core.util.Context.NONE);
    }
}
```

### HcxEnterpriseSites_Get

```java
/**
 * Samples for HcxEnterpriseSites Get.
 */
public final class HcxEnterpriseSitesGetSamples {
    /*
     * x-ms-original-file: specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/HcxEnterpriseSites_Get.json
     */
    /**
     * Sample code: HcxEnterpriseSites_Get.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void hcxEnterpriseSitesGet(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.hcxEnterpriseSites().getWithResponse("group1", "cloud1", "site1", com.azure.core.util.Context.NONE);
    }
}
```

### HcxEnterpriseSites_List

```java
/**
 * Samples for HcxEnterpriseSites List.
 */
public final class HcxEnterpriseSitesListSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/HcxEnterpriseSites_List.json
     */
    /**
     * Sample code: HcxEnterpriseSites_List.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void hcxEnterpriseSitesList(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.hcxEnterpriseSites().list("group1", "cloud1", com.azure.core.util.Context.NONE);
    }
}
```

### IscsiPaths_CreateOrUpdate

```java
import com.azure.resourcemanager.avs.fluent.models.IscsiPathInner;

/**
 * Samples for IscsiPaths CreateOrUpdate.
 */
public final class IscsiPathsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/IscsiPaths_CreateOrUpdate.json
     */
    /**
     * Sample code: IscsiPaths_CreateOrUpdate.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void iscsiPathsCreateOrUpdate(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.iscsiPaths()
            .createOrUpdate("group1", "cloud1", new IscsiPathInner().withNetworkBlock("192.168.0.0/24"),
                com.azure.core.util.Context.NONE);
    }
}
```

### IscsiPaths_Delete

```java
/**
 * Samples for IscsiPaths Delete.
 */
public final class IscsiPathsDeleteSamples {
    /*
     * x-ms-original-file: specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/IscsiPaths_Delete.json
     */
    /**
     * Sample code: IscsiPaths_Delete.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void iscsiPathsDelete(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.iscsiPaths().delete("group1", "cloud1", com.azure.core.util.Context.NONE);
    }
}
```

### IscsiPaths_Get

```java
/**
 * Samples for IscsiPaths Get.
 */
public final class IscsiPathsGetSamples {
    /*
     * x-ms-original-file: specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/IscsiPaths_Get.json
     */
    /**
     * Sample code: IscsiPaths_Get.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void iscsiPathsGet(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.iscsiPaths().getWithResponse("group1", "cloud1", com.azure.core.util.Context.NONE);
    }
}
```

### IscsiPaths_ListByPrivateCloud

```java
/**
 * Samples for IscsiPaths ListByPrivateCloud.
 */
public final class IscsiPathsListByPrivateCloudSamples {
    /*
     * x-ms-original-file: specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/IscsiPaths_List.json
     */
    /**
     * Sample code: IscsiPaths_ListByPrivateCloud.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void iscsiPathsListByPrivateCloud(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.iscsiPaths().listByPrivateCloud("group1", "cloud1", com.azure.core.util.Context.NONE);
    }
}
```

### Locations_CheckQuotaAvailability

```java
/**
 * Samples for Locations CheckQuotaAvailability.
 */
public final class LocationsCheckQuotaAvailabilitySamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/Locations_CheckQuotaAvailability.json
     */
    /**
     * Sample code: Locations_CheckQuotaAvailability.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void locationsCheckQuotaAvailability(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.locations().checkQuotaAvailabilityWithResponse("eastus", com.azure.core.util.Context.NONE);
    }
}
```

### Locations_CheckTrialAvailability

```java

/**
 * Samples for Locations CheckTrialAvailability.
 */
public final class LocationsCheckTrialAvailabilitySamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/Locations_CheckTrialAvailability.json
     */
    /**
     * Sample code: Locations_CheckTrialAvailability.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void locationsCheckTrialAvailability(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.locations().checkTrialAvailabilityWithResponse("eastus", null, com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void operationsList(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### PlacementPolicies_CreateOrUpdate

```java
import com.azure.resourcemanager.avs.models.AffinityStrength;
import com.azure.resourcemanager.avs.models.AffinityType;
import com.azure.resourcemanager.avs.models.AzureHybridBenefitType;
import com.azure.resourcemanager.avs.models.VmHostPlacementPolicyProperties;
import java.util.Arrays;

/**
 * Samples for PlacementPolicies CreateOrUpdate.
 */
public final class PlacementPoliciesCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/PlacementPolicies_CreateOrUpdate.json
     */
    /**
     * Sample code: PlacementPolicies_CreateOrUpdate.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void placementPoliciesCreateOrUpdate(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.placementPolicies()
            .define("policy1")
            .withExistingCluster("group1", "cloud1", "cluster1")
            .withProperties(new VmHostPlacementPolicyProperties().withVmMembers(Arrays.asList(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/group1/providers/Microsoft.AVS/privateClouds/cloud1/clusters/cluster1/virtualMachines/vm-128",
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/group1/providers/Microsoft.AVS/privateClouds/cloud1/clusters/cluster1/virtualMachines/vm-256"))
                .withHostMembers(Arrays.asList("fakehost22.nyc1.kubernetes.center", "fakehost23.nyc1.kubernetes.center",
                    "fakehost24.nyc1.kubernetes.center"))
                .withAffinityType(AffinityType.ANTI_AFFINITY)
                .withAffinityStrength(AffinityStrength.MUST)
                .withAzureHybridBenefitType(AzureHybridBenefitType.SQL_HOST))
            .create();
    }
}
```

### PlacementPolicies_Delete

```java
/**
 * Samples for PlacementPolicies Delete.
 */
public final class PlacementPoliciesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/PlacementPolicies_Delete.json
     */
    /**
     * Sample code: PlacementPolicies_Delete.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void placementPoliciesDelete(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.placementPolicies().delete("group1", "cloud1", "cluster1", "policy1", com.azure.core.util.Context.NONE);
    }
}
```

### PlacementPolicies_Get

```java
/**
 * Samples for PlacementPolicies Get.
 */
public final class PlacementPoliciesGetSamples {
    /*
     * x-ms-original-file: specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/PlacementPolicies_Get.json
     */
    /**
     * Sample code: PlacementPolicies_Get.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void placementPoliciesGet(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.placementPolicies()
            .getWithResponse("group1", "cloud1", "cluster1", "policy1", com.azure.core.util.Context.NONE);
    }
}
```

### PlacementPolicies_List

```java
/**
 * Samples for PlacementPolicies List.
 */
public final class PlacementPoliciesListSamples {
    /*
     * x-ms-original-file: specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/PlacementPolicies_List.json
     */
    /**
     * Sample code: PlacementPolicies_List.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void placementPoliciesList(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.placementPolicies().list("group1", "cloud1", "cluster1", com.azure.core.util.Context.NONE);
    }
}
```

### PlacementPolicies_Update

```java
import com.azure.resourcemanager.avs.models.AffinityStrength;
import com.azure.resourcemanager.avs.models.AzureHybridBenefitType;
import com.azure.resourcemanager.avs.models.PlacementPolicy;
import com.azure.resourcemanager.avs.models.PlacementPolicyState;
import java.util.Arrays;

/**
 * Samples for PlacementPolicies Update.
 */
public final class PlacementPoliciesUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/PlacementPolicies_Update.json
     */
    /**
     * Sample code: PlacementPolicies_Update.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void placementPoliciesUpdate(com.azure.resourcemanager.avs.AvsManager manager) {
        PlacementPolicy resource = manager.placementPolicies()
            .getWithResponse("group1", "cloud1", "cluster1", "policy1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withState(PlacementPolicyState.DISABLED)
            .withVmMembers(Arrays.asList(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/group1/providers/Microsoft.AVS/privateClouds/cloud1/clusters/cluster1/virtualMachines/vm-128",
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/group1/providers/Microsoft.AVS/privateClouds/cloud1/clusters/cluster1/virtualMachines/vm-256"))
            .withHostMembers(Arrays.asList("fakehost22.nyc1.kubernetes.center", "fakehost23.nyc1.kubernetes.center",
                "fakehost24.nyc1.kubernetes.center"))
            .withAffinityStrength(AffinityStrength.MUST)
            .withAzureHybridBenefitType(AzureHybridBenefitType.SQL_HOST)
            .apply();
    }
}
```

### PrivateClouds_CreateOrUpdate

```java
import com.azure.resourcemanager.avs.models.ManagementCluster;
import com.azure.resourcemanager.avs.models.PrivateCloudIdentity;
import com.azure.resourcemanager.avs.models.Sku;
import com.azure.resourcemanager.avs.models.SystemAssignedServiceIdentityType;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for PrivateClouds CreateOrUpdate.
 */
public final class PrivateCloudsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/PrivateClouds_CreateOrUpdate.json
     */
    /**
     * Sample code: PrivateClouds_CreateOrUpdate.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void privateCloudsCreateOrUpdate(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.privateClouds()
            .define("cloud1")
            .withRegion("eastus2")
            .withExistingResourceGroup("group1")
            .withSku(new Sku().withName("AV36"))
            .withTags(mapOf())
            .withIdentity(new PrivateCloudIdentity().withType(SystemAssignedServiceIdentityType.SYSTEM_ASSIGNED))
            .withManagementCluster(new ManagementCluster().withClusterSize(4))
            .withNetworkBlock("192.168.48.0/22")
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

### PrivateClouds_Delete

```java
/**
 * Samples for PrivateClouds Delete.
 */
public final class PrivateCloudsDeleteSamples {
    /*
     * x-ms-original-file: specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/PrivateClouds_Delete.json
     */
    /**
     * Sample code: PrivateClouds_Delete.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void privateCloudsDelete(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.privateClouds().delete("group1", "cloud1", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateClouds_GetByResourceGroup

```java
/**
 * Samples for PrivateClouds GetByResourceGroup.
 */
public final class PrivateCloudsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/PrivateClouds_Get.json
     */
    /**
     * Sample code: PrivateClouds_Get.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void privateCloudsGet(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.privateClouds().getByResourceGroupWithResponse("group1", "cloud1", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateClouds_List

```java
/**
 * Samples for PrivateClouds List.
 */
public final class PrivateCloudsListSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/PrivateClouds_ListInSubscription.json
     */
    /**
     * Sample code: PrivateClouds_ListInSubscription.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void privateCloudsListInSubscription(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.privateClouds().list(com.azure.core.util.Context.NONE);
    }
}
```

### PrivateClouds_ListAdminCredentials

```java
/**
 * Samples for PrivateClouds ListAdminCredentials.
 */
public final class PrivateCloudsListAdminCredentialsSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/PrivateClouds_ListAdminCredentials.json
     */
    /**
     * Sample code: PrivateClouds_ListAdminCredentials.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void privateCloudsListAdminCredentials(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.privateClouds().listAdminCredentialsWithResponse("group1", "cloud1", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateClouds_ListByResourceGroup

```java
/**
 * Samples for PrivateClouds ListByResourceGroup.
 */
public final class PrivateCloudsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/PrivateClouds_List.json
     */
    /**
     * Sample code: PrivateClouds_List.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void privateCloudsList(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.privateClouds().listByResourceGroup("group1", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateClouds_RotateNsxtPassword

```java
/**
 * Samples for PrivateClouds RotateNsxtPassword.
 */
public final class PrivateCloudsRotateNsxtPasswordSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/PrivateClouds_RotateNsxtPassword.json
     */
    /**
     * Sample code: PrivateClouds_RotateNsxtPassword.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void privateCloudsRotateNsxtPassword(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.privateClouds().rotateNsxtPassword("group1", "cloud1", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateClouds_RotateVcenterPassword

```java
/**
 * Samples for PrivateClouds RotateVcenterPassword.
 */
public final class PrivateCloudsRotateVcenterPasswordSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/PrivateClouds_RotateVcenterPassword.json
     */
    /**
     * Sample code: PrivateClouds_RotateVcenterPassword.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void privateCloudsRotateVcenterPassword(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.privateClouds().rotateVcenterPassword("group1", "cloud1", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateClouds_Update

```java
import com.azure.resourcemanager.avs.models.Encryption;
import com.azure.resourcemanager.avs.models.EncryptionKeyVaultProperties;
import com.azure.resourcemanager.avs.models.EncryptionState;
import com.azure.resourcemanager.avs.models.ManagementCluster;
import com.azure.resourcemanager.avs.models.PrivateCloud;
import com.azure.resourcemanager.avs.models.PrivateCloudIdentity;
import com.azure.resourcemanager.avs.models.SystemAssignedServiceIdentityType;

/**
 * Samples for PrivateClouds Update.
 */
public final class PrivateCloudsUpdateSamples {
    /*
     * x-ms-original-file: specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/PrivateClouds_Update.json
     */
    /**
     * Sample code: PrivateClouds_Update.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void privateCloudsUpdate(com.azure.resourcemanager.avs.AvsManager manager) {
        PrivateCloud resource = manager.privateClouds()
            .getByResourceGroupWithResponse("group1", "cloud1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withIdentity(new PrivateCloudIdentity().withType(SystemAssignedServiceIdentityType.NONE))
            .withManagementCluster(new ManagementCluster().withClusterSize(4))
            .withEncryption(new Encryption().withStatus(EncryptionState.ENABLED)
                .withKeyVaultProperties(new EncryptionKeyVaultProperties().withKeyName("fakeTokenPlaceholder")
                    .withKeyVersion("fakeTokenPlaceholder")
                    .withKeyVaultUrl("fakeTokenPlaceholder")))
            .apply();
    }
}
```

### ScriptCmdlets_Get

```java
/**
 * Samples for ScriptCmdlets Get.
 */
public final class ScriptCmdletsGetSamples {
    /*
     * x-ms-original-file: specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/ScriptCmdlets_Get.json
     */
    /**
     * Sample code: ScriptCmdlets_Get.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void scriptCmdletsGet(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.scriptCmdlets()
            .getWithResponse("group1", "cloud1", "package@1.0.2", "New-ExternalSsoDomain",
                com.azure.core.util.Context.NONE);
    }
}
```

### ScriptCmdlets_List

```java
/**
 * Samples for ScriptCmdlets List.
 */
public final class ScriptCmdletsListSamples {
    /*
     * x-ms-original-file: specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/ScriptCmdlets_List.json
     */
    /**
     * Sample code: ScriptCmdlets_List.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void scriptCmdletsList(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.scriptCmdlets().list("group1", "cloud1", "package@1.0.2", com.azure.core.util.Context.NONE);
    }
}
```

### ScriptExecutions_CreateOrUpdate

```java
import com.azure.resourcemanager.avs.models.ScriptSecureStringExecutionParameter;
import com.azure.resourcemanager.avs.models.ScriptStringExecutionParameter;
import java.util.Arrays;

/**
 * Samples for ScriptExecutions CreateOrUpdate.
 */
public final class ScriptExecutionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/ScriptExecutions_CreateOrUpdate.json
     */
    /**
     * Sample code: ScriptExecutions_CreateOrUpdate.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void scriptExecutionsCreateOrUpdate(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.scriptExecutions()
            .define("addSsoServer")
            .withExistingPrivateCloud("group1", "cloud1")
            .withScriptCmdletId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/group1/providers/Microsoft.AVS/privateClouds/cloud1/scriptPackages/AVS.PowerCommands@1.0.0/scriptCmdlets/New-SsoExternalIdentitySource")
            .withParameters(Arrays.asList(
                new ScriptStringExecutionParameter().withName("DomainName").withValue("placeholderDomain.local"),
                new ScriptStringExecutionParameter().withName("BaseUserDN")
                    .withValue("DC=placeholder, DC=placeholder")))
            .withHiddenParameters(Arrays.asList(
                new ScriptSecureStringExecutionParameter().withName("Password").withSecureValue("PlaceholderPassword")))
            .withTimeout("P0Y0M0DT0H60M60S")
            .withRetention("P0Y0M60DT0H60M60S")
            .create();
    }
}
```

### ScriptExecutions_Delete

```java
/**
 * Samples for ScriptExecutions Delete.
 */
public final class ScriptExecutionsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/ScriptExecutions_Delete.json
     */
    /**
     * Sample code: ScriptExecutions_Delete.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void scriptExecutionsDelete(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.scriptExecutions().delete("group1", "cloud1", "addSsoServer", com.azure.core.util.Context.NONE);
    }
}
```

### ScriptExecutions_Get

```java
/**
 * Samples for ScriptExecutions Get.
 */
public final class ScriptExecutionsGetSamples {
    /*
     * x-ms-original-file: specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/ScriptExecutions_Get.json
     */
    /**
     * Sample code: ScriptExecutions_Get.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void scriptExecutionsGet(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.scriptExecutions()
            .getWithResponse("group1", "cloud1", "addSsoServer", com.azure.core.util.Context.NONE);
    }
}
```

### ScriptExecutions_GetExecutionLogs

```java
import com.azure.resourcemanager.avs.models.ScriptOutputStreamType;
import java.util.Arrays;

/**
 * Samples for ScriptExecutions GetExecutionLogs.
 */
public final class ScriptExecutionsGetExecutionLogsSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/ScriptExecutions_GetExecutionLogs.json
     */
    /**
     * Sample code: ScriptExecutions_GetExecutionLogs.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void scriptExecutionsGetExecutionLogs(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.scriptExecutions()
            .getExecutionLogsWithResponse("group1", "cloud1", "addSsoServer",
                Arrays.asList(ScriptOutputStreamType.INFORMATION, ScriptOutputStreamType.fromString("Warnings"),
                    ScriptOutputStreamType.fromString("Errors"), ScriptOutputStreamType.OUTPUT),
                com.azure.core.util.Context.NONE);
    }
}
```

### ScriptExecutions_List

```java
/**
 * Samples for ScriptExecutions List.
 */
public final class ScriptExecutionsListSamples {
    /*
     * x-ms-original-file: specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/ScriptExecutions_List.json
     */
    /**
     * Sample code: ScriptExecutions_List.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void scriptExecutionsList(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.scriptExecutions().list("group1", "cloud1", com.azure.core.util.Context.NONE);
    }
}
```

### ScriptPackages_Get

```java
/**
 * Samples for ScriptPackages Get.
 */
public final class ScriptPackagesGetSamples {
    /*
     * x-ms-original-file: specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/ScriptPackages_Get.json
     */
    /**
     * Sample code: ScriptPackages_Get.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void scriptPackagesGet(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.scriptPackages()
            .getWithResponse("group1", "cloud1", "Microsoft.AVS.Management@3.0.48", com.azure.core.util.Context.NONE);
    }
}
```

### ScriptPackages_List

```java
/**
 * Samples for ScriptPackages List.
 */
public final class ScriptPackagesListSamples {
    /*
     * x-ms-original-file: specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/ScriptPackages_List.json
     */
    /**
     * Sample code: ScriptPackages_List.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void scriptPackagesList(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.scriptPackages().list("group1", "cloud1", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachines_Get

```java
/**
 * Samples for VirtualMachines Get.
 */
public final class VirtualMachinesGetSamples {
    /*
     * x-ms-original-file: specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/VirtualMachines_Get.json
     */
    /**
     * Sample code: VirtualMachines_Get.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void virtualMachinesGet(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.virtualMachines()
            .getWithResponse("group1", "cloud1", "cluster1", "vm-209", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachines_List

```java
/**
 * Samples for VirtualMachines List.
 */
public final class VirtualMachinesListSamples {
    /*
     * x-ms-original-file: specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/VirtualMachines_List.json
     */
    /**
     * Sample code: VirtualMachines_List.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void virtualMachinesList(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.virtualMachines().list("group1", "cloud1", "cluster1", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachines_RestrictMovement

```java
import com.azure.resourcemanager.avs.models.VirtualMachineRestrictMovement;
import com.azure.resourcemanager.avs.models.VirtualMachineRestrictMovementState;

/**
 * Samples for VirtualMachines RestrictMovement.
 */
public final class VirtualMachinesRestrictMovementSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/VirtualMachines_RestrictMovement.json
     */
    /**
     * Sample code: VirtualMachines_RestrictMovement.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void virtualMachinesRestrictMovement(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.virtualMachines()
            .restrictMovement("group1", "cloud1", "cluster1", "vm-209",
                new VirtualMachineRestrictMovement().withRestrictMovement(VirtualMachineRestrictMovementState.ENABLED),
                com.azure.core.util.Context.NONE);
    }
}
```

### WorkloadNetworks_CreateDhcp

```java
import com.azure.resourcemanager.avs.models.WorkloadNetworkDhcpServer;

/**
 * Samples for WorkloadNetworks CreateDhcp.
 */
public final class WorkloadNetworksCreateDhcpSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/WorkloadNetworks_CreateDhcp.json
     */
    /**
     * Sample code: WorkloadNetworks_CreateDhcp.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void workloadNetworksCreateDhcp(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.workloadNetworks()
            .defineDhcp("dhcp1")
            .withExistingPrivateCloud("group1", "cloud1")
            .withProperties(new WorkloadNetworkDhcpServer().withDisplayName("dhcpConfigurations1")
                .withRevision(1L)
                .withServerAddress("40.1.5.1/24")
                .withLeaseTime(86400L))
            .create();
    }
}
```

### WorkloadNetworks_CreateDnsService

```java
import com.azure.resourcemanager.avs.models.DnsServiceLogLevelEnum;
import java.util.Arrays;

/**
 * Samples for WorkloadNetworks CreateDnsService.
 */
public final class WorkloadNetworksCreateDnsServiceSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/WorkloadNetworks_CreateDnsService.json
     */
    /**
     * Sample code: WorkloadNetworks_CreateDnsService.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void workloadNetworksCreateDnsService(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.workloadNetworks()
            .defineDnsService("dnsService1")
            .withExistingPrivateCloud("group1", "cloud1")
            .withDisplayName("dnsService1")
            .withDnsServiceIp("5.5.5.5")
            .withDefaultDnsZone("defaultDnsZone1")
            .withFqdnZones(Arrays.asList("fqdnZone1"))
            .withLogLevel(DnsServiceLogLevelEnum.INFO)
            .withRevision(1L)
            .create();
    }
}
```

### WorkloadNetworks_CreateDnsZone

```java
import java.util.Arrays;

/**
 * Samples for WorkloadNetworks CreateDnsZone.
 */
public final class WorkloadNetworksCreateDnsZoneSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/WorkloadNetworks_CreateDnsZone.json
     */
    /**
     * Sample code: WorkloadNetworks_CreateDnsZone.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void workloadNetworksCreateDnsZone(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.workloadNetworks()
            .defineDnsZone("dnsZone1")
            .withExistingPrivateCloud("group1", "cloud1")
            .withDisplayName("dnsZone1")
            .withDomain(Arrays.asList())
            .withDnsServerIps(Arrays.asList("1.1.1.1"))
            .withSourceIp("8.8.8.8")
            .withRevision(1L)
            .create();
    }
}
```

### WorkloadNetworks_CreatePortMirroring

```java
import com.azure.resourcemanager.avs.models.PortMirroringDirectionEnum;

/**
 * Samples for WorkloadNetworks CreatePortMirroring.
 */
public final class WorkloadNetworksCreatePortMirroringSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/WorkloadNetworks_CreatePortMirroring.json
     */
    /**
     * Sample code: WorkloadNetworks_CreatePortMirroring.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void workloadNetworksCreatePortMirroring(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.workloadNetworks()
            .definePortMirroring("portMirroring1")
            .withExistingPrivateCloud("group1", "cloud1")
            .withDisplayName("portMirroring1")
            .withDirection(PortMirroringDirectionEnum.BIDIRECTIONAL)
            .withSource("vmGroup1")
            .withDestination("vmGroup2")
            .withRevision(1L)
            .create();
    }
}
```

### WorkloadNetworks_CreatePublicIP

```java
/**
 * Samples for WorkloadNetworks CreatePublicIP.
 */
public final class WorkloadNetworksCreatePublicIPSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/WorkloadNetworks_CreatePublicIP.json
     */
    /**
     * Sample code: WorkloadNetworks_CreatePublicIP.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void workloadNetworksCreatePublicIP(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.workloadNetworks()
            .definePublicIP("publicIP1")
            .withExistingPrivateCloud("group1", "cloud1")
            .withDisplayName("publicIP1")
            .withNumberOfPublicIPs(32L)
            .create();
    }
}
```

### WorkloadNetworks_CreateSegment

```java
import com.azure.resourcemanager.avs.models.WorkloadNetworkSegmentSubnet;
import java.util.Arrays;

/**
 * Samples for WorkloadNetworks CreateSegment.
 */
public final class WorkloadNetworksCreateSegmentSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/WorkloadNetworks_CreateSegments.json
     */
    /**
     * Sample code: WorkloadNetworks_CreateSegments.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void workloadNetworksCreateSegments(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.workloadNetworks()
            .defineSegment("segment1")
            .withExistingPrivateCloud("group1", "cloud1")
            .withDisplayName("segment1")
            .withConnectedGateway("/infra/tier-1s/gateway")
            .withSubnet(new WorkloadNetworkSegmentSubnet().withDhcpRanges(Arrays.asList("40.20.0.0-40.20.0.1"))
                .withGatewayAddress("40.20.20.20/16"))
            .withRevision(1L)
            .create();
    }
}
```

### WorkloadNetworks_CreateVMGroup

```java
/**
 * Samples for WorkloadNetworks CreateVMGroup.
 */
public final class WorkloadNetworksCreateVMGroupSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/WorkloadNetworks_CreateVMGroup.json
     */
    /**
     * Sample code: WorkloadNetworks_CreateVMGroup.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void workloadNetworksCreateVMGroup(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.workloadNetworks().defineVMGroup("vmGroup1").withExistingPrivateCloud("group1", "cloud1").create();
    }
}
```

### WorkloadNetworks_DeleteDhcp

```java
/**
 * Samples for WorkloadNetworks DeleteDhcp.
 */
public final class WorkloadNetworksDeleteDhcpSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/WorkloadNetworks_DeleteDhcp.json
     */
    /**
     * Sample code: WorkloadNetworks_DeleteDhcp.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void workloadNetworksDeleteDhcp(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.workloadNetworks().deleteDhcp("group1", "cloud1", "dhcp1", com.azure.core.util.Context.NONE);
    }
}
```

### WorkloadNetworks_DeleteDnsService

```java
/**
 * Samples for WorkloadNetworks DeleteDnsService.
 */
public final class WorkloadNetworksDeleteDnsServiceSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/WorkloadNetworks_DeleteDnsService.json
     */
    /**
     * Sample code: WorkloadNetworks_DeleteDnsService.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void workloadNetworksDeleteDnsService(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.workloadNetworks()
            .deleteDnsService("group1", "dnsService1", "cloud1", com.azure.core.util.Context.NONE);
    }
}
```

### WorkloadNetworks_DeleteDnsZone

```java
/**
 * Samples for WorkloadNetworks DeleteDnsZone.
 */
public final class WorkloadNetworksDeleteDnsZoneSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/WorkloadNetworks_DeleteDnsZone.json
     */
    /**
     * Sample code: WorkloadNetworks_DeleteDnsZone.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void workloadNetworksDeleteDnsZone(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.workloadNetworks().deleteDnsZone("group1", "dnsZone1", "cloud1", com.azure.core.util.Context.NONE);
    }
}
```

### WorkloadNetworks_DeletePortMirroring

```java
/**
 * Samples for WorkloadNetworks DeletePortMirroring.
 */
public final class WorkloadNetworksDeletePortMirroringSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/WorkloadNetworks_DeletePortMirroring.json
     */
    /**
     * Sample code: WorkloadNetworks_DeletePortMirroring.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void workloadNetworksDeletePortMirroring(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.workloadNetworks()
            .deletePortMirroring("group1", "portMirroring1", "cloud1", com.azure.core.util.Context.NONE);
    }
}
```

### WorkloadNetworks_DeletePublicIP

```java
/**
 * Samples for WorkloadNetworks DeletePublicIP.
 */
public final class WorkloadNetworksDeletePublicIPSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/WorkloadNetworks_DeletePublicIP.json
     */
    /**
     * Sample code: WorkloadNetworks_DeletePublicIP.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void workloadNetworksDeletePublicIP(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.workloadNetworks().deletePublicIP("group1", "publicIP1", "cloud1", com.azure.core.util.Context.NONE);
    }
}
```

### WorkloadNetworks_DeleteSegment

```java
/**
 * Samples for WorkloadNetworks DeleteSegment.
 */
public final class WorkloadNetworksDeleteSegmentSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/WorkloadNetworks_DeleteSegment.json
     */
    /**
     * Sample code: WorkloadNetworks_DeleteSegment.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void workloadNetworksDeleteSegment(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.workloadNetworks().deleteSegment("group1", "cloud1", "segment1", com.azure.core.util.Context.NONE);
    }
}
```

### WorkloadNetworks_DeleteVMGroup

```java
/**
 * Samples for WorkloadNetworks DeleteVMGroup.
 */
public final class WorkloadNetworksDeleteVMGroupSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/WorkloadNetworks_DeleteVMGroup.json
     */
    /**
     * Sample code: WorkloadNetworks_DeleteVMGroup.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void workloadNetworksDeleteVMGroup(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.workloadNetworks().deleteVMGroup("group1", "vmGroup1", "cloud1", com.azure.core.util.Context.NONE);
    }
}
```

### WorkloadNetworks_Get

```java
/**
 * Samples for WorkloadNetworks Get.
 */
public final class WorkloadNetworksGetSamples {
    /*
     * x-ms-original-file: specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/WorkloadNetworks_Get.json
     */
    /**
     * Sample code: WorkloadNetworks_Get.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void workloadNetworksGet(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.workloadNetworks().getWithResponse("group1", "cloud1", com.azure.core.util.Context.NONE);
    }
}
```

### WorkloadNetworks_GetDhcp

```java
/**
 * Samples for WorkloadNetworks GetDhcp.
 */
public final class WorkloadNetworksGetDhcpSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/WorkloadNetworks_GetDhcp.json
     */
    /**
     * Sample code: WorkloadNetworks_GetDhcp.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void workloadNetworksGetDhcp(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.workloadNetworks().getDhcpWithResponse("group1", "dhcp1", "cloud1", com.azure.core.util.Context.NONE);
    }
}
```

### WorkloadNetworks_GetDnsService

```java
/**
 * Samples for WorkloadNetworks GetDnsService.
 */
public final class WorkloadNetworksGetDnsServiceSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/WorkloadNetworks_GetDnsService.json
     */
    /**
     * Sample code: WorkloadNetworks_GetDnsService.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void workloadNetworksGetDnsService(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.workloadNetworks()
            .getDnsServiceWithResponse("group1", "cloud1", "dnsService1", com.azure.core.util.Context.NONE);
    }
}
```

### WorkloadNetworks_GetDnsZone

```java
/**
 * Samples for WorkloadNetworks GetDnsZone.
 */
public final class WorkloadNetworksGetDnsZoneSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/WorkloadNetworks_GetDnsZone.json
     */
    /**
     * Sample code: WorkloadNetworks_GetDnsZone.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void workloadNetworksGetDnsZone(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.workloadNetworks()
            .getDnsZoneWithResponse("group1", "cloud1", "dnsZone1", com.azure.core.util.Context.NONE);
    }
}
```

### WorkloadNetworks_GetGateway

```java
/**
 * Samples for WorkloadNetworks GetGateway.
 */
public final class WorkloadNetworksGetGatewaySamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/WorkloadNetworks_GetGateway.json
     */
    /**
     * Sample code: WorkloadNetworks_GetGateway.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void workloadNetworksGetGateway(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.workloadNetworks()
            .getGatewayWithResponse("group1", "cloud1", "gateway1", com.azure.core.util.Context.NONE);
    }
}
```

### WorkloadNetworks_GetPortMirroring

```java
/**
 * Samples for WorkloadNetworks GetPortMirroring.
 */
public final class WorkloadNetworksGetPortMirroringSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/WorkloadNetworks_GetPortMirroring.json
     */
    /**
     * Sample code: WorkloadNetworks_GetPortMirroring.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void workloadNetworksGetPortMirroring(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.workloadNetworks()
            .getPortMirroringWithResponse("group1", "cloud1", "portMirroring1", com.azure.core.util.Context.NONE);
    }
}
```

### WorkloadNetworks_GetPublicIP

```java
/**
 * Samples for WorkloadNetworks GetPublicIP.
 */
public final class WorkloadNetworksGetPublicIPSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/WorkloadNetworks_GetPublicIP.json
     */
    /**
     * Sample code: WorkloadNetworks_GetPublicIP.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void workloadNetworksGetPublicIP(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.workloadNetworks()
            .getPublicIPWithResponse("group1", "cloud1", "publicIP1", com.azure.core.util.Context.NONE);
    }
}
```

### WorkloadNetworks_GetSegment

```java
/**
 * Samples for WorkloadNetworks GetSegment.
 */
public final class WorkloadNetworksGetSegmentSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/WorkloadNetworks_GetSegment.json
     */
    /**
     * Sample code: WorkloadNetworks_GetSegment.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void workloadNetworksGetSegment(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.workloadNetworks()
            .getSegmentWithResponse("group1", "cloud1", "segment1", com.azure.core.util.Context.NONE);
    }
}
```

### WorkloadNetworks_GetVMGroup

```java
/**
 * Samples for WorkloadNetworks GetVMGroup.
 */
public final class WorkloadNetworksGetVMGroupSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/WorkloadNetworks_GetVMGroup.json
     */
    /**
     * Sample code: WorkloadNetworks_GetVMGroup.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void workloadNetworksGetVMGroup(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.workloadNetworks()
            .getVMGroupWithResponse("group1", "cloud1", "vmGroup1", com.azure.core.util.Context.NONE);
    }
}
```

### WorkloadNetworks_GetVirtualMachine

```java
/**
 * Samples for WorkloadNetworks GetVirtualMachine.
 */
public final class WorkloadNetworksGetVirtualMachineSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/WorkloadNetworks_GetVirtualMachine.json
     */
    /**
     * Sample code: WorkloadNetworks_GetVirtualMachine.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void workloadNetworksGetVirtualMachine(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.workloadNetworks()
            .getVirtualMachineWithResponse("group1", "cloud1", "vm1", com.azure.core.util.Context.NONE);
    }
}
```

### WorkloadNetworks_List

```java
/**
 * Samples for WorkloadNetworks List.
 */
public final class WorkloadNetworksListSamples {
    /*
     * x-ms-original-file: specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/WorkloadNetworks_List.json
     */
    /**
     * Sample code: WorkloadNetworks_List.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void workloadNetworksList(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.workloadNetworks().list("group1", "cloud1", com.azure.core.util.Context.NONE);
    }
}
```

### WorkloadNetworks_ListDhcp

```java
/**
 * Samples for WorkloadNetworks ListDhcp.
 */
public final class WorkloadNetworksListDhcpSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/WorkloadNetworks_ListDhcp.json
     */
    /**
     * Sample code: WorkloadNetworks_ListDhcp.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void workloadNetworksListDhcp(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.workloadNetworks().listDhcp("group1", "cloud1", com.azure.core.util.Context.NONE);
    }
}
```

### WorkloadNetworks_ListDnsServices

```java
/**
 * Samples for WorkloadNetworks ListDnsServices.
 */
public final class WorkloadNetworksListDnsServicesSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/WorkloadNetworks_ListDnsServices.json
     */
    /**
     * Sample code: WorkloadNetworks_ListDnsServices.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void workloadNetworksListDnsServices(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.workloadNetworks().listDnsServices("group1", "cloud1", com.azure.core.util.Context.NONE);
    }
}
```

### WorkloadNetworks_ListDnsZones

```java
/**
 * Samples for WorkloadNetworks ListDnsZones.
 */
public final class WorkloadNetworksListDnsZonesSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/WorkloadNetworks_ListDnsZones.json
     */
    /**
     * Sample code: WorkloadNetworks_ListDnsZones.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void workloadNetworksListDnsZones(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.workloadNetworks().listDnsZones("group1", "cloud1", com.azure.core.util.Context.NONE);
    }
}
```

### WorkloadNetworks_ListGateways

```java
/**
 * Samples for WorkloadNetworks ListGateways.
 */
public final class WorkloadNetworksListGatewaysSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/WorkloadNetworks_ListGateways.json
     */
    /**
     * Sample code: WorkloadNetworks_ListGateways.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void workloadNetworksListGateways(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.workloadNetworks().listGateways("group1", "cloud1", com.azure.core.util.Context.NONE);
    }
}
```

### WorkloadNetworks_ListPortMirroring

```java
/**
 * Samples for WorkloadNetworks ListPortMirroring.
 */
public final class WorkloadNetworksListPortMirroringSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/WorkloadNetworks_ListPortMirroring.json
     */
    /**
     * Sample code: WorkloadNetworks_ListPortMirroring.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void workloadNetworksListPortMirroring(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.workloadNetworks().listPortMirroring("group1", "cloud1", com.azure.core.util.Context.NONE);
    }
}
```

### WorkloadNetworks_ListPublicIPs

```java
/**
 * Samples for WorkloadNetworks ListPublicIPs.
 */
public final class WorkloadNetworksListPublicIPsSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/WorkloadNetworks_ListPublicIPs.json
     */
    /**
     * Sample code: WorkloadNetworks_ListPublicIPs.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void workloadNetworksListPublicIPs(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.workloadNetworks().listPublicIPs("group1", "cloud1", com.azure.core.util.Context.NONE);
    }
}
```

### WorkloadNetworks_ListSegments

```java
/**
 * Samples for WorkloadNetworks ListSegments.
 */
public final class WorkloadNetworksListSegmentsSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/WorkloadNetworks_ListSegments.json
     */
    /**
     * Sample code: WorkloadNetworks_ListSegments.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void workloadNetworksListSegments(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.workloadNetworks().listSegments("group1", "cloud1", com.azure.core.util.Context.NONE);
    }
}
```

### WorkloadNetworks_ListVMGroups

```java
/**
 * Samples for WorkloadNetworks ListVMGroups.
 */
public final class WorkloadNetworksListVMGroupsSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/WorkloadNetworks_ListVMGroups.json
     */
    /**
     * Sample code: WorkloadNetworks_ListVMGroups.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void workloadNetworksListVMGroups(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.workloadNetworks().listVMGroups("group1", "cloud1", com.azure.core.util.Context.NONE);
    }
}
```

### WorkloadNetworks_ListVirtualMachines

```java
/**
 * Samples for WorkloadNetworks ListVirtualMachines.
 */
public final class WorkloadNetworksListVirtualMachinesSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/WorkloadNetworks_ListVirtualMachines.json
     */
    /**
     * Sample code: WorkloadNetworks_ListVirtualMachines.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void workloadNetworksListVirtualMachines(com.azure.resourcemanager.avs.AvsManager manager) {
        manager.workloadNetworks().listVirtualMachines("group1", "cloud1", com.azure.core.util.Context.NONE);
    }
}
```

### WorkloadNetworks_UpdateDnsService

```java
import com.azure.resourcemanager.avs.models.DnsServiceLogLevelEnum;
import com.azure.resourcemanager.avs.models.WorkloadNetworkDnsService;
import java.util.Arrays;

/**
 * Samples for WorkloadNetworks UpdateDnsService.
 */
public final class WorkloadNetworksUpdateDnsServiceSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/WorkloadNetworks_UpdateDnsService.json
     */
    /**
     * Sample code: WorkloadNetworks_UpdateDnsService.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void workloadNetworksUpdateDnsService(com.azure.resourcemanager.avs.AvsManager manager) {
        WorkloadNetworkDnsService resource = manager.workloadNetworks()
            .getDnsServiceWithResponse("group1", "cloud1", "dnsService1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withDisplayName("dnsService1")
            .withDnsServiceIp("5.5.5.5")
            .withDefaultDnsZone("defaultDnsZone1")
            .withFqdnZones(Arrays.asList("fqdnZone1"))
            .withLogLevel(DnsServiceLogLevelEnum.INFO)
            .withRevision(1L)
            .apply();
    }
}
```

### WorkloadNetworks_UpdateDnsZone

```java
import com.azure.resourcemanager.avs.models.WorkloadNetworkDnsZone;
import java.util.Arrays;

/**
 * Samples for WorkloadNetworks UpdateDnsZone.
 */
public final class WorkloadNetworksUpdateDnsZoneSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/WorkloadNetworks_UpdateDnsZone.json
     */
    /**
     * Sample code: WorkloadNetworks_UpdateDnsZone.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void workloadNetworksUpdateDnsZone(com.azure.resourcemanager.avs.AvsManager manager) {
        WorkloadNetworkDnsZone resource = manager.workloadNetworks()
            .getDnsZoneWithResponse("group1", "cloud1", "dnsZone1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withDisplayName("dnsZone1")
            .withDomain(Arrays.asList())
            .withDnsServerIps(Arrays.asList("1.1.1.1"))
            .withSourceIp("8.8.8.8")
            .withRevision(1L)
            .apply();
    }
}
```

### WorkloadNetworks_UpdatePortMirroring

```java
import com.azure.resourcemanager.avs.models.PortMirroringDirectionEnum;
import com.azure.resourcemanager.avs.models.WorkloadNetworkPortMirroring;

/**
 * Samples for WorkloadNetworks UpdatePortMirroring.
 */
public final class WorkloadNetworksUpdatePortMirroringSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/WorkloadNetworks_UpdatePortMirroring.json
     */
    /**
     * Sample code: WorkloadNetworks_UpdatePortMirroring.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void workloadNetworksUpdatePortMirroring(com.azure.resourcemanager.avs.AvsManager manager) {
        WorkloadNetworkPortMirroring resource = manager.workloadNetworks()
            .getPortMirroringWithResponse("group1", "cloud1", "portMirroring1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withDirection(PortMirroringDirectionEnum.BIDIRECTIONAL)
            .withSource("vmGroup1")
            .withDestination("vmGroup2")
            .withRevision(1L)
            .apply();
    }
}
```

### WorkloadNetworks_UpdateSegment

```java
import com.azure.resourcemanager.avs.models.WorkloadNetworkSegment;

/**
 * Samples for WorkloadNetworks UpdateSegment.
 */
public final class WorkloadNetworksUpdateSegmentSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/WorkloadNetworks_UpdateSegments.json
     */
    /**
     * Sample code: WorkloadNetworks_UpdateSegments.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void workloadNetworksUpdateSegments(com.azure.resourcemanager.avs.AvsManager manager) {
        WorkloadNetworkSegment resource = manager.workloadNetworks()
            .getSegmentWithResponse("group1", "cloud1", "segment1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().apply();
    }
}
```

### WorkloadNetworks_UpdateVMGroup

```java
import com.azure.resourcemanager.avs.models.WorkloadNetworkVMGroup;
import java.util.Arrays;

/**
 * Samples for WorkloadNetworks UpdateVMGroup.
 */
public final class WorkloadNetworksUpdateVMGroupSamples {
    /*
     * x-ms-original-file:
     * specification/vmware/Microsoft.AVS.Management/examples/2023-09-01/WorkloadNetworks_UpdateVMGroup.json
     */
    /**
     * Sample code: WorkloadNetworks_UpdateVMGroup.
     * 
     * @param manager Entry point to AvsManager.
     */
    public static void workloadNetworksUpdateVMGroup(com.azure.resourcemanager.avs.AvsManager manager) {
        WorkloadNetworkVMGroup resource = manager.workloadNetworks()
            .getVMGroupWithResponse("group1", "cloud1", "vmGroup1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withMembers(Arrays.asList("564d43da-fefc-2a3b-1d92-42855622fa50")).withRevision(1L).apply();
    }
}
```

