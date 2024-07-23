# Code snippets and samples


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
            .withExistingWorkloadNetwork("group1", "cloud1", null)
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
            .withExistingWorkloadNetwork("group1", "cloud1", null)
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
            .withExistingWorkloadNetwork("group1", "cloud1", null)
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
            .withExistingWorkloadNetwork("group1", "cloud1", null)
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
            .withExistingWorkloadNetwork("group1", "cloud1", null)
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
            .withExistingWorkloadNetwork("group1", "cloud1", null)
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
        manager.workloadNetworks()
            .defineVMGroup("vmGroup1")
            .withExistingWorkloadNetwork("group1", "cloud1", null)
            .create();
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
        manager.workloadNetworks().deleteDhcp("group1", "cloud1", null, "dhcp1", com.azure.core.util.Context.NONE);
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
        manager.workloadNetworks()
            .deleteSegment("group1", "cloud1", null, "segment1", com.azure.core.util.Context.NONE);
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
        manager.workloadNetworks().getWithResponse("group1", "cloud1", null, com.azure.core.util.Context.NONE);
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
            .getDnsServiceWithResponse("group1", "cloud1", null, "dnsService1", com.azure.core.util.Context.NONE);
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
            .getDnsZoneWithResponse("group1", "cloud1", null, "dnsZone1", com.azure.core.util.Context.NONE);
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
            .getGatewayWithResponse("group1", "cloud1", null, "gateway1", com.azure.core.util.Context.NONE);
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
            .getPortMirroringWithResponse("group1", "cloud1", null, "portMirroring1", com.azure.core.util.Context.NONE);
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
            .getPublicIPWithResponse("group1", "cloud1", null, "publicIP1", com.azure.core.util.Context.NONE);
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
            .getSegmentWithResponse("group1", "cloud1", null, "segment1", com.azure.core.util.Context.NONE);
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
            .getVMGroupWithResponse("group1", "cloud1", null, "vmGroup1", com.azure.core.util.Context.NONE);
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
            .getVirtualMachineWithResponse("group1", "cloud1", null, "vm1", com.azure.core.util.Context.NONE);
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
        manager.workloadNetworks().listDhcp("group1", "cloud1", null, com.azure.core.util.Context.NONE);
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
        manager.workloadNetworks().listDnsServices("group1", "cloud1", null, com.azure.core.util.Context.NONE);
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
        manager.workloadNetworks().listDnsZones("group1", "cloud1", null, com.azure.core.util.Context.NONE);
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
        manager.workloadNetworks().listGateways("group1", "cloud1", null, com.azure.core.util.Context.NONE);
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
        manager.workloadNetworks().listPortMirroring("group1", "cloud1", null, com.azure.core.util.Context.NONE);
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
        manager.workloadNetworks().listPublicIPs("group1", "cloud1", null, com.azure.core.util.Context.NONE);
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
        manager.workloadNetworks().listSegments("group1", "cloud1", null, com.azure.core.util.Context.NONE);
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
        manager.workloadNetworks().listVMGroups("group1", "cloud1", null, com.azure.core.util.Context.NONE);
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
        manager.workloadNetworks().listVirtualMachines("group1", "cloud1", null, com.azure.core.util.Context.NONE);
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
            .getDnsServiceWithResponse("group1", "cloud1", null, "dnsService1", com.azure.core.util.Context.NONE)
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
            .getDnsZoneWithResponse("group1", "cloud1", null, "dnsZone1", com.azure.core.util.Context.NONE)
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
            .getPortMirroringWithResponse("group1", "cloud1", null, "portMirroring1", com.azure.core.util.Context.NONE)
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
            .getSegmentWithResponse("group1", "cloud1", null, "segment1", com.azure.core.util.Context.NONE)
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
            .getVMGroupWithResponse("group1", "cloud1", null, "vmGroup1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withMembers(Arrays.asList("564d43da-fefc-2a3b-1d92-42855622fa50")).withRevision(1L).apply();
    }
}
```

