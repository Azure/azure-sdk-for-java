# Code snippets and samples


## PrivateZones

- [CreateOrUpdate](#privatezones_createorupdate)
- [Delete](#privatezones_delete)
- [GetByResourceGroup](#privatezones_getbyresourcegroup)
- [List](#privatezones_list)
- [ListByResourceGroup](#privatezones_listbyresourcegroup)
- [Update](#privatezones_update)

## RecordSets

- [CreateOrUpdate](#recordsets_createorupdate)
- [Delete](#recordsets_delete)
- [Get](#recordsets_get)
- [List](#recordsets_list)
- [ListByType](#recordsets_listbytype)
- [Update](#recordsets_update)

## VirtualNetworkLinks

- [CreateOrUpdate](#virtualnetworklinks_createorupdate)
- [Delete](#virtualnetworklinks_delete)
- [Get](#virtualnetworklinks_get)
- [List](#virtualnetworklinks_list)
- [Update](#virtualnetworklinks_update)
### PrivateZones_CreateOrUpdate

```java
import java.util.HashMap;
import java.util.Map;

/** Samples for PrivateZones CreateOrUpdate. */
public final class PrivateZonesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/PrivateZonePut.json
     */
    /**
     * Sample code: PUT Private DNS Zone.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void pUTPrivateDNSZone(com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager
            .privateZones()
            .define("privatezone1.com")
            .withRegion("Global")
            .withExistingResourceGroup("resourceGroup1")
            .withTags(mapOf("key1", "value1"))
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

### PrivateZones_Delete

```java
import com.azure.core.util.Context;

/** Samples for PrivateZones Delete. */
public final class PrivateZonesDeleteSamples {
    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/PrivateZoneDelete.json
     */
    /**
     * Sample code: DELETE Private DNS Zone.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void dELETEPrivateDNSZone(com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager.privateZones().delete("resourceGroup1", "privatezone1.com", null, Context.NONE);
    }
}
```

### PrivateZones_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for PrivateZones GetByResourceGroup. */
public final class PrivateZonesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/PrivateZoneGet.json
     */
    /**
     * Sample code: GET Private DNS Zone.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void gETPrivateDNSZone(com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager.privateZones().getByResourceGroupWithResponse("resourceGroup1", "privatezone1.com", Context.NONE);
    }
}
```

### PrivateZones_List

```java
import com.azure.core.util.Context;

/** Samples for PrivateZones List. */
public final class PrivateZonesListSamples {
    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/PrivateZoneListInSubscription.json
     */
    /**
     * Sample code: GET Private DNS Zone by Subscription.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void gETPrivateDNSZoneBySubscription(
        com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager.privateZones().list(null, Context.NONE);
    }
}
```

### PrivateZones_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for PrivateZones ListByResourceGroup. */
public final class PrivateZonesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/PrivateZoneListInResourceGroup.json
     */
    /**
     * Sample code: GET Private DNS Zone by Resource Group.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void gETPrivateDNSZoneByResourceGroup(
        com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager.privateZones().listByResourceGroup("resourceGroup1", null, Context.NONE);
    }
}
```

### PrivateZones_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.privatedns.generated.models.PrivateZone;
import java.util.HashMap;
import java.util.Map;

/** Samples for PrivateZones Update. */
public final class PrivateZonesUpdateSamples {
    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/PrivateZonePatch.json
     */
    /**
     * Sample code: PATCH Private DNS Zone.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void pATCHPrivateDNSZone(com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        PrivateZone resource =
            manager
                .privateZones()
                .getByResourceGroupWithResponse("resourceGroup1", "privatezone1.com", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("key2", "value2")).apply();
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

### RecordSets_CreateOrUpdate

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.privatedns.generated.fluent.models.RecordSetInner;
import com.azure.resourcemanager.privatedns.generated.models.ARecord;
import com.azure.resourcemanager.privatedns.generated.models.AaaaRecord;
import com.azure.resourcemanager.privatedns.generated.models.CnameRecord;
import com.azure.resourcemanager.privatedns.generated.models.MxRecord;
import com.azure.resourcemanager.privatedns.generated.models.PtrRecord;
import com.azure.resourcemanager.privatedns.generated.models.RecordType;
import com.azure.resourcemanager.privatedns.generated.models.SoaRecord;
import com.azure.resourcemanager.privatedns.generated.models.SrvRecord;
import com.azure.resourcemanager.privatedns.generated.models.TxtRecord;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for RecordSets CreateOrUpdate. */
public final class RecordSetsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/RecordSetTXTPut.json
     */
    /**
     * Sample code: PUT Private DNS Zone TXT Record Set.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void pUTPrivateDNSZoneTXTRecordSet(
        com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager
            .recordSets()
            .createOrUpdateWithResponse(
                "resourceGroup1",
                "privatezone1.com",
                RecordType.TXT,
                "recordTXT",
                new RecordSetInner()
                    .withMetadata(mapOf("key1", "value1"))
                    .withTtl(3600L)
                    .withTxtRecords(Arrays.asList(new TxtRecord().withValue(Arrays.asList("string1", "string2")))),
                null,
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/RecordSetAAAAPut.json
     */
    /**
     * Sample code: PUT Private DNS Zone AAAA Record Set.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void pUTPrivateDNSZoneAAAARecordSet(
        com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager
            .recordSets()
            .createOrUpdateWithResponse(
                "resourceGroup1",
                "privatezone1.com",
                RecordType.AAAA,
                "recordAAAA",
                new RecordSetInner()
                    .withMetadata(mapOf("key1", "value1"))
                    .withTtl(3600L)
                    .withAaaaRecords(Arrays.asList(new AaaaRecord().withIpv6Address("::1"))),
                null,
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/RecordSetSOAPut.json
     */
    /**
     * Sample code: PUT Private DNS Zone SOA Record Set.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void pUTPrivateDNSZoneSOARecordSet(
        com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager
            .recordSets()
            .createOrUpdateWithResponse(
                "resourceGroup1",
                "privatezone1.com",
                RecordType.SOA,
                "@",
                new RecordSetInner()
                    .withMetadata(mapOf("key1", "value1"))
                    .withTtl(3600L)
                    .withSoaRecord(
                        new SoaRecord()
                            .withHost("azureprivatedns.net")
                            .withEmail("azureprivatedns-hostmaster.microsoft.com")
                            .withSerialNumber(1L)
                            .withRefreshTime(3600L)
                            .withRetryTime(300L)
                            .withExpireTime(2419200L)),
                null,
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/RecordSetMXPut.json
     */
    /**
     * Sample code: PUT Private DNS Zone MX Record Set.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void pUTPrivateDNSZoneMXRecordSet(
        com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager
            .recordSets()
            .createOrUpdateWithResponse(
                "resourceGroup1",
                "privatezone1.com",
                RecordType.MX,
                "recordMX",
                new RecordSetInner()
                    .withMetadata(mapOf("key1", "value1"))
                    .withTtl(3600L)
                    .withMxRecords(
                        Arrays.asList(new MxRecord().withPreference(0).withExchange("mail.privatezone1.com"))),
                null,
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/RecordSetCNAMEPut.json
     */
    /**
     * Sample code: PUT Private DNS Zone CNAME Record Set.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void pUTPrivateDNSZoneCNAMERecordSet(
        com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager
            .recordSets()
            .createOrUpdateWithResponse(
                "resourceGroup1",
                "privatezone1.com",
                RecordType.CNAME,
                "recordCNAME",
                new RecordSetInner()
                    .withMetadata(mapOf("key1", "value1"))
                    .withTtl(3600L)
                    .withCnameRecord(new CnameRecord().withCname("contoso.com")),
                null,
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/RecordSetPTRPut.json
     */
    /**
     * Sample code: PUT Private DNS Zone PTR Record Set.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void pUTPrivateDNSZonePTRRecordSet(
        com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager
            .recordSets()
            .createOrUpdateWithResponse(
                "resourceGroup1",
                "0.0.127.in-addr.arpa",
                RecordType.PTR,
                "1",
                new RecordSetInner()
                    .withMetadata(mapOf("key1", "value1"))
                    .withTtl(3600L)
                    .withPtrRecords(Arrays.asList(new PtrRecord().withPtrdname("localhost"))),
                null,
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/RecordSetAPut.json
     */
    /**
     * Sample code: PUT Private DNS Zone A Record Set.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void pUTPrivateDNSZoneARecordSet(
        com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager
            .recordSets()
            .createOrUpdateWithResponse(
                "resourceGroup1",
                "privatezone1.com",
                RecordType.A,
                "recordA",
                new RecordSetInner()
                    .withMetadata(mapOf("key1", "value1"))
                    .withTtl(3600L)
                    .withARecords(Arrays.asList(new ARecord().withIpv4Address("1.2.3.4"))),
                null,
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/RecordSetSRVPut.json
     */
    /**
     * Sample code: PUT Private DNS Zone SRV Record Set.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void pUTPrivateDNSZoneSRVRecordSet(
        com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager
            .recordSets()
            .createOrUpdateWithResponse(
                "resourceGroup1",
                "privatezone1.com",
                RecordType.SRV,
                "recordSRV",
                new RecordSetInner()
                    .withMetadata(mapOf("key1", "value1"))
                    .withTtl(3600L)
                    .withSrvRecords(
                        Arrays
                            .asList(
                                new SrvRecord().withPriority(0).withWeight(10).withPort(80).withTarget("contoso.com"))),
                null,
                null,
                Context.NONE);
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

### RecordSets_Delete

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.privatedns.generated.models.RecordType;

/** Samples for RecordSets Delete. */
public final class RecordSetsDeleteSamples {
    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/RecordSetTXTDelete.json
     */
    /**
     * Sample code: DELETE Private DNS Zone TXT Record Set.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void dELETEPrivateDNSZoneTXTRecordSet(
        com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager
            .recordSets()
            .deleteWithResponse("resourceGroup1", "privatezone1.com", RecordType.TXT, "recordTXT", null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/RecordSetMXDelete.json
     */
    /**
     * Sample code: DELETE Private DNS Zone MX Record Set.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void dELETEPrivateDNSZoneMXRecordSet(
        com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager
            .recordSets()
            .deleteWithResponse("resourceGroup1", "privatezone1.com", RecordType.MX, "recordMX", null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/RecordSetCNAMEDelete.json
     */
    /**
     * Sample code: DELETE Private DNS Zone CNAME Record Set.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void dELETEPrivateDNSZoneCNAMERecordSet(
        com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager
            .recordSets()
            .deleteWithResponse(
                "resourceGroup1", "privatezone1.com", RecordType.CNAME, "recordCNAME", null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/RecordSetAAAADelete.json
     */
    /**
     * Sample code: DELETE Private DNS Zone AAAA Record Set.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void dELETEPrivateDNSZoneAAAARecordSet(
        com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager
            .recordSets()
            .deleteWithResponse(
                "resourceGroup1", "privatezone1.com", RecordType.AAAA, "recordAAAA", null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/RecordSetSRVDelete.json
     */
    /**
     * Sample code: DELETE Private DNS Zone SRV Record Set.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void dELETEPrivateDNSZoneSRVRecordSet(
        com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager
            .recordSets()
            .deleteWithResponse("resourceGroup1", "privatezone1.com", RecordType.SRV, "recordSRV", null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/RecordSetADelete.json
     */
    /**
     * Sample code: DELETE Private DNS Zone A Record Set.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void dELETEPrivateDNSZoneARecordSet(
        com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager
            .recordSets()
            .deleteWithResponse("resourceGroup1", "privatezone1.com", RecordType.A, "recordA", null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/RecordSetPTRDelete.json
     */
    /**
     * Sample code: DELETE Private DNS Zone PTR Record Set.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void dELETEPrivateDNSZonePTRRecordSet(
        com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager
            .recordSets()
            .deleteWithResponse("resourceGroup1", "0.0.127.in-addr.arpa", RecordType.PTR, "1", null, Context.NONE);
    }
}
```

### RecordSets_Get

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.privatedns.generated.models.RecordType;

/** Samples for RecordSets Get. */
public final class RecordSetsGetSamples {
    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/RecordSetPTRGet.json
     */
    /**
     * Sample code: GET Private DNS Zone PTR Record Set.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void gETPrivateDNSZonePTRRecordSet(
        com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager
            .recordSets()
            .getWithResponse("resourceGroup1", "0.0.127.in-addr.arpa", RecordType.PTR, "1", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/RecordSetMXGet.json
     */
    /**
     * Sample code: GET Private DNS Zone MX Record Set.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void gETPrivateDNSZoneMXRecordSet(
        com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager
            .recordSets()
            .getWithResponse("resourceGroup1", "privatezone1.com", RecordType.MX, "recordMX", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/RecordSetAAAAGet.json
     */
    /**
     * Sample code: GET Private DNS Zone AAAA Record Set.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void gETPrivateDNSZoneAAAARecordSet(
        com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager
            .recordSets()
            .getWithResponse("resourceGroup1", "privatezone1.com", RecordType.AAAA, "recordAAAA", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/RecordSetSOAGet.json
     */
    /**
     * Sample code: GET Private DNS Zone SOA Record Set.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void gETPrivateDNSZoneSOARecordSet(
        com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager.recordSets().getWithResponse("resourceGroup1", "privatezone1.com", RecordType.SOA, "@", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/RecordSetSRVGet.json
     */
    /**
     * Sample code: GET Private DNS Zone SRV Record Set.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void gETPrivateDNSZoneSRVRecordSet(
        com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager
            .recordSets()
            .getWithResponse("resourceGroup1", "privatezone1.com", RecordType.SRV, "recordSRV", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/RecordSetAGet.json
     */
    /**
     * Sample code: GET Private DNS Zone A Record Set.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void gETPrivateDNSZoneARecordSet(
        com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager
            .recordSets()
            .getWithResponse("resourceGroup1", "privatezone1.com", RecordType.A, "recordA", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/RecordSetCNAMEGet.json
     */
    /**
     * Sample code: GET Private DNS Zone CNAME Record Set.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void gETPrivateDNSZoneCNAMERecordSet(
        com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager
            .recordSets()
            .getWithResponse("resourceGroup1", "privatezone1.com", RecordType.CNAME, "recordCNAME", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/RecordSetTXTGet.json
     */
    /**
     * Sample code: GET Private DNS Zone TXT Record Set.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void gETPrivateDNSZoneTXTRecordSet(
        com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager
            .recordSets()
            .getWithResponse("resourceGroup1", "privatezone1.com", RecordType.TXT, "recordTXT", Context.NONE);
    }
}
```

### RecordSets_List

```java
import com.azure.core.util.Context;

/** Samples for RecordSets List. */
public final class RecordSetsListSamples {
    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/RecordSetALLList.json
     */
    /**
     * Sample code: GET Private DNS Zone ALL Record Sets.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void gETPrivateDNSZoneALLRecordSets(
        com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager.recordSets().list("resourceGroup1", "privatezone1.com", null, null, Context.NONE);
    }
}
```

### RecordSets_ListByType

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.privatedns.generated.models.RecordType;

/** Samples for RecordSets ListByType. */
public final class RecordSetsListByTypeSamples {
    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/RecordSetCNAMEList.json
     */
    /**
     * Sample code: GET Private DNS Zone CNAME Record Sets.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void gETPrivateDNSZoneCNAMERecordSets(
        com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager
            .recordSets()
            .listByType("resourceGroup1", "privatezone1.com", RecordType.CNAME, null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/RecordSetSOAList.json
     */
    /**
     * Sample code: GET Private DNS Zone SOA Record Sets.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void gETPrivateDNSZoneSOARecordSets(
        com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager.recordSets().listByType("resourceGroup1", "privatezone1.com", RecordType.SOA, null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/RecordSetAAAAList.json
     */
    /**
     * Sample code: GET Private DNS Zone AAAA Record Sets.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void gETPrivateDNSZoneAAAARecordSets(
        com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager
            .recordSets()
            .listByType("resourceGroup1", "privatezone1.com", RecordType.AAAA, null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/RecordSetAList.json
     */
    /**
     * Sample code: GET Private DNS Zone A Record Sets.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void gETPrivateDNSZoneARecordSets(
        com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager.recordSets().listByType("resourceGroup1", "privatezone1.com", RecordType.A, null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/RecordSetSRVList.json
     */
    /**
     * Sample code: GET Private DNS Zone SRV Record Sets.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void gETPrivateDNSZoneSRVRecordSets(
        com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager.recordSets().listByType("resourceGroup1", "privatezone1.com", RecordType.SRV, null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/RecordSetMXList.json
     */
    /**
     * Sample code: GET Private DNS Zone MX Record Sets.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void gETPrivateDNSZoneMXRecordSets(
        com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager.recordSets().listByType("resourceGroup1", "privatezone1.com", RecordType.MX, null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/RecordSetTXTList.json
     */
    /**
     * Sample code: GET Private DNS Zone TXT Record Sets.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void gETPrivateDNSZoneTXTRecordSets(
        com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager.recordSets().listByType("resourceGroup1", "privatezone1.com", RecordType.TXT, null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/RecordSetPTRList.json
     */
    /**
     * Sample code: GET Private DNS Zone PTR Record Sets.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void gETPrivateDNSZonePTRRecordSets(
        com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager
            .recordSets()
            .listByType("resourceGroup1", "0.0.127.in-addr.arpa", RecordType.PTR, null, null, Context.NONE);
    }
}
```

### RecordSets_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.privatedns.generated.fluent.models.RecordSetInner;
import com.azure.resourcemanager.privatedns.generated.models.RecordType;
import java.util.HashMap;
import java.util.Map;

/** Samples for RecordSets Update. */
public final class RecordSetsUpdateSamples {
    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/RecordSetTXTPatch.json
     */
    /**
     * Sample code: PATCH Private DNS Zone TXT Record Set.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void pATCHPrivateDNSZoneTXTRecordSet(
        com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager
            .recordSets()
            .updateWithResponse(
                "resourceGroup1",
                "privatezone1.com",
                RecordType.TXT,
                "recordTXT",
                new RecordSetInner().withMetadata(mapOf("key2", "value2")),
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/RecordSetSOAPatch.json
     */
    /**
     * Sample code: PATCH Private DNS Zone SOA Record Set.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void pATCHPrivateDNSZoneSOARecordSet(
        com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager
            .recordSets()
            .updateWithResponse(
                "resourceGroup1",
                "privatezone1.com",
                RecordType.SOA,
                "@",
                new RecordSetInner().withMetadata(mapOf("key2", "value2")),
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/RecordSetAPatch.json
     */
    /**
     * Sample code: PATCH Private DNS Zone A Record Set.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void pATCHPrivateDNSZoneARecordSet(
        com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager
            .recordSets()
            .updateWithResponse(
                "resourceGroup1",
                "privatezone1.com",
                RecordType.A,
                "recordA",
                new RecordSetInner().withMetadata(mapOf("key2", "value2")),
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/RecordSetCNAMEPatch.json
     */
    /**
     * Sample code: PATCH Private DNS Zone CNAME Record Set.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void pATCHPrivateDNSZoneCNAMERecordSet(
        com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager
            .recordSets()
            .updateWithResponse(
                "resourceGroup1",
                "privatezone1.com",
                RecordType.CNAME,
                "recordCNAME",
                new RecordSetInner().withMetadata(mapOf("key2", "value2")),
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/RecordSetPTRPatch.json
     */
    /**
     * Sample code: PATCH Private DNS Zone PTR Record Set.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void pATCHPrivateDNSZonePTRRecordSet(
        com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager
            .recordSets()
            .updateWithResponse(
                "resourceGroup1",
                "0.0.127.in-addr.arpa",
                RecordType.PTR,
                "1",
                new RecordSetInner().withMetadata(mapOf("key2", "value2")),
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/RecordSetSRVPatch.json
     */
    /**
     * Sample code: PATCH Private DNS Zone SRV Record Set.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void pATCHPrivateDNSZoneSRVRecordSet(
        com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager
            .recordSets()
            .updateWithResponse(
                "resourceGroup1",
                "privatezone1.com",
                RecordType.SRV,
                "recordSRV",
                new RecordSetInner().withMetadata(mapOf("key2", "value2")),
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/RecordSetAAAAPatch.json
     */
    /**
     * Sample code: PATCH Private DNS Zone AAAA Record Set.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void pATCHPrivateDNSZoneAAAARecordSet(
        com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager
            .recordSets()
            .updateWithResponse(
                "resourceGroup1",
                "privatezone1.com",
                RecordType.AAAA,
                "recordAAAA",
                new RecordSetInner().withMetadata(mapOf("key2", "value2")),
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/RecordSetMXPatch.json
     */
    /**
     * Sample code: PATCH Private DNS Zone MX Record Set.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void pATCHPrivateDNSZoneMXRecordSet(
        com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager
            .recordSets()
            .updateWithResponse(
                "resourceGroup1",
                "privatezone1.com",
                RecordType.MX,
                "recordMX",
                new RecordSetInner().withMetadata(mapOf("key2", "value2")),
                null,
                Context.NONE);
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

### VirtualNetworkLinks_CreateOrUpdate

```java
import com.azure.core.management.SubResource;
import java.util.HashMap;
import java.util.Map;

/** Samples for VirtualNetworkLinks CreateOrUpdate. */
public final class VirtualNetworkLinksCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/VirtualNetworkLinkPut.json
     */
    /**
     * Sample code: PUT Private DNS Zone Virtual Network Link.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void pUTPrivateDNSZoneVirtualNetworkLink(
        com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager
            .virtualNetworkLinks()
            .define("virtualNetworkLink1")
            .withRegion("Global")
            .withExistingPrivateDnsZone("resourceGroup1", "privatezone1.com")
            .withTags(mapOf("key1", "value1"))
            .withVirtualNetwork(
                new SubResource()
                    .withId(
                        "/subscriptions/virtualNetworkSubscriptionId/resourceGroups/virtualNetworkResourceGroup/providers/Microsoft.Network/virtualNetworks/virtualNetworkName"))
            .withRegistrationEnabled(false)
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

### VirtualNetworkLinks_Delete

```java
import com.azure.core.util.Context;

/** Samples for VirtualNetworkLinks Delete. */
public final class VirtualNetworkLinksDeleteSamples {
    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/VirtualNetworkLinkDelete.json
     */
    /**
     * Sample code: DELETE Private DNS Zone Virtual Network Link.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void dELETEPrivateDNSZoneVirtualNetworkLink(
        com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager
            .virtualNetworkLinks()
            .delete("resourceGroup1", "privatezone1.com", "virtualNetworkLink1", null, Context.NONE);
    }
}
```

### VirtualNetworkLinks_Get

```java
import com.azure.core.util.Context;

/** Samples for VirtualNetworkLinks Get. */
public final class VirtualNetworkLinksGetSamples {
    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/VirtualNetworkLinkGet.json
     */
    /**
     * Sample code: GET Private DNS Zone Virtual Network Link.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void gETPrivateDNSZoneVirtualNetworkLink(
        com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager
            .virtualNetworkLinks()
            .getWithResponse("resourceGroup1", "privatezone1.com", "virtualNetworkLink1", Context.NONE);
    }
}
```

### VirtualNetworkLinks_List

```java
import com.azure.core.util.Context;

/** Samples for VirtualNetworkLinks List. */
public final class VirtualNetworkLinksListSamples {
    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/VirtualNetworkLinkList.json
     */
    /**
     * Sample code: Get Private DNS Zone Virtual Network Links.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void getPrivateDNSZoneVirtualNetworkLinks(
        com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        manager.virtualNetworkLinks().list("resourceGroup1", "privatezone1.com", null, Context.NONE);
    }
}
```

### VirtualNetworkLinks_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.privatedns.generated.models.VirtualNetworkLink;
import java.util.HashMap;
import java.util.Map;

/** Samples for VirtualNetworkLinks Update. */
public final class VirtualNetworkLinksUpdateSamples {
    /*
     * x-ms-original-file: specification/privatedns/resource-manager/Microsoft.Network/stable/2020-06-01/examples/VirtualNetworkLinkPatch.json
     */
    /**
     * Sample code: PATCH Private DNS Zone Virtual Network Link.
     *
     * @param manager Entry point to PrivateDnsManager.
     */
    public static void pATCHPrivateDNSZoneVirtualNetworkLink(
        com.azure.resourcemanager.privatedns.generated.PrivateDnsManager manager) {
        VirtualNetworkLink resource =
            manager
                .virtualNetworkLinks()
                .getWithResponse("resourceGroup1", "privatezone1.com", "virtualNetworkLink1", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("key2", "value2")).withRegistrationEnabled(true).apply();
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

