# Code snippets and samples


## DnsResourceReference

- [GetByTargetResources](#dnsresourcereference_getbytargetresources)

## RecordSets

- [CreateOrUpdate](#recordsets_createorupdate)
- [Delete](#recordsets_delete)
- [Get](#recordsets_get)
- [ListAllByDnsZone](#recordsets_listallbydnszone)
- [ListByDnsZone](#recordsets_listbydnszone)
- [ListByType](#recordsets_listbytype)
- [Update](#recordsets_update)

## Zones

- [CreateOrUpdate](#zones_createorupdate)
- [Delete](#zones_delete)
- [GetByResourceGroup](#zones_getbyresourcegroup)
- [List](#zones_list)
- [ListByResourceGroup](#zones_listbyresourcegroup)
- [Update](#zones_update)
### DnsResourceReference_GetByTargetResources

```java
import com.azure.core.management.SubResource;
import com.azure.core.util.Context;
import com.azure.resourcemanager.dns.generated.models.DnsResourceReferenceRequest;
import java.util.Arrays;

/** Samples for DnsResourceReference GetByTargetResources. */
public final class DnsResourceReferenceGetByTargetResourcesSamples {
    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/GetDnsResourceReference.json
     */
    /**
     * Sample code: List zones by resource group.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void listZonesByResourceGroup(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager
            .dnsResourceReferences()
            .getByTargetResourcesWithResponse(
                new DnsResourceReferenceRequest()
                    .withTargetResources(
                        Arrays
                            .asList(
                                new SubResource()
                                    .withId(
                                        "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.Network/trafficManagerProfiles/testpp2"))),
                Context.NONE);
    }
}
```

### RecordSets_CreateOrUpdate

```java
import com.azure.core.management.SubResource;
import com.azure.core.util.Context;
import com.azure.resourcemanager.dns.generated.fluent.models.RecordSetInner;
import com.azure.resourcemanager.dns.generated.models.ARecord;
import com.azure.resourcemanager.dns.generated.models.AaaaRecord;
import com.azure.resourcemanager.dns.generated.models.CaaRecord;
import com.azure.resourcemanager.dns.generated.models.CnameRecord;
import com.azure.resourcemanager.dns.generated.models.MxRecord;
import com.azure.resourcemanager.dns.generated.models.NsRecord;
import com.azure.resourcemanager.dns.generated.models.PtrRecord;
import com.azure.resourcemanager.dns.generated.models.RecordType;
import com.azure.resourcemanager.dns.generated.models.SoaRecord;
import com.azure.resourcemanager.dns.generated.models.SrvRecord;
import com.azure.resourcemanager.dns.generated.models.TxtRecord;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for RecordSets CreateOrUpdate. */
public final class RecordSetsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/CreateOrUpdateSRVRecordset.json
     */
    /**
     * Sample code: Create SRV recordset.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void createSRVRecordset(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager
            .recordSets()
            .createOrUpdateWithResponse(
                "rg1",
                "zone1",
                "record1",
                RecordType.SRV,
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

    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/CreateOrUpdateCNAMERecordset.json
     */
    /**
     * Sample code: Create CNAME recordset.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void createCNAMERecordset(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager
            .recordSets()
            .createOrUpdateWithResponse(
                "rg1",
                "zone1",
                "record1",
                RecordType.CNAME,
                new RecordSetInner()
                    .withMetadata(mapOf("key1", "value1"))
                    .withTtl(3600L)
                    .withCnameRecord(new CnameRecord().withCname("contoso.com")),
                null,
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/CreateOrUpdateTXTRecordset.json
     */
    /**
     * Sample code: Create TXT recordset.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void createTXTRecordset(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager
            .recordSets()
            .createOrUpdateWithResponse(
                "rg1",
                "zone1",
                "record1",
                RecordType.TXT,
                new RecordSetInner()
                    .withMetadata(mapOf("key1", "value1"))
                    .withTtl(3600L)
                    .withTxtRecords(Arrays.asList(new TxtRecord().withValue(Arrays.asList("string1", "string2")))),
                null,
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/CreateOrUpdateAAAARecordset.json
     */
    /**
     * Sample code: Create AAAA recordset.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void createAAAARecordset(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager
            .recordSets()
            .createOrUpdateWithResponse(
                "rg1",
                "zone1",
                "record1",
                RecordType.AAAA,
                new RecordSetInner()
                    .withMetadata(mapOf("key1", "value1"))
                    .withTtl(3600L)
                    .withAaaaRecords(Arrays.asList(new AaaaRecord().withIpv6Address("::1"))),
                null,
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/CreateOrUpdateCaaRecordset.json
     */
    /**
     * Sample code: Create CAA recordset.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void createCAARecordset(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager
            .recordSets()
            .createOrUpdateWithResponse(
                "rg1",
                "zone1",
                "record1",
                RecordType.CAA,
                new RecordSetInner()
                    .withMetadata(mapOf("key1", "value1"))
                    .withTtl(3600L)
                    .withCaaRecords(
                        Arrays.asList(new CaaRecord().withFlags(0).withTag("issue").withValue("ca.contoso.com"))),
                null,
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/CreateOrUpdatePTRRecordset.json
     */
    /**
     * Sample code: Create PTR recordset.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void createPTRRecordset(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager
            .recordSets()
            .createOrUpdateWithResponse(
                "rg1",
                "0.0.127.in-addr.arpa",
                "1",
                RecordType.PTR,
                new RecordSetInner()
                    .withMetadata(mapOf("key1", "value1"))
                    .withTtl(3600L)
                    .withPtrRecords(Arrays.asList(new PtrRecord().withPtrdname("localhost"))),
                null,
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/CreateOrUpdateMXRecordset.json
     */
    /**
     * Sample code: Create MX recordset.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void createMXRecordset(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager
            .recordSets()
            .createOrUpdateWithResponse(
                "rg1",
                "zone1",
                "record1",
                RecordType.MX,
                new RecordSetInner()
                    .withMetadata(mapOf("key1", "value1"))
                    .withTtl(3600L)
                    .withMxRecords(Arrays.asList(new MxRecord().withPreference(0).withExchange("mail.contoso.com"))),
                null,
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/CreateOrUpdateNSRecordset.json
     */
    /**
     * Sample code: Create NS recordset.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void createNSRecordset(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager
            .recordSets()
            .createOrUpdateWithResponse(
                "rg1",
                "zone1",
                "record1",
                RecordType.NS,
                new RecordSetInner()
                    .withMetadata(mapOf("key1", "value1"))
                    .withTtl(3600L)
                    .withNsRecords(Arrays.asList(new NsRecord().withNsdname("ns1.contoso.com"))),
                null,
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/CreateOrUpdateARecordsetAlias.json
     */
    /**
     * Sample code: Create A recordset with alias target resource.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void createARecordsetWithAliasTargetResource(
        com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager
            .recordSets()
            .createOrUpdateWithResponse(
                "rg1",
                "zone1",
                "record1",
                RecordType.A,
                new RecordSetInner()
                    .withMetadata(mapOf("key1", "value1"))
                    .withTtl(3600L)
                    .withTargetResource(
                        new SubResource()
                            .withId(
                                "/subscriptions/726f8cd6-6459-4db4-8e6d-2cd2716904e2/resourceGroups/test/providers/Microsoft.Network/trafficManagerProfiles/testpp2")),
                null,
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/CreateOrUpdateARecordset.json
     */
    /**
     * Sample code: Create A recordset.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void createARecordset(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager
            .recordSets()
            .createOrUpdateWithResponse(
                "rg1",
                "zone1",
                "record1",
                RecordType.A,
                new RecordSetInner()
                    .withMetadata(mapOf("key1", "value1"))
                    .withTtl(3600L)
                    .withARecords(Arrays.asList(new ARecord().withIpv4Address("127.0.0.1"))),
                null,
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/CreateOrUpdateSOARecordset.json
     */
    /**
     * Sample code: Create SOA recordset.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void createSOARecordset(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager
            .recordSets()
            .createOrUpdateWithResponse(
                "rg1",
                "zone1",
                "@",
                RecordType.SOA,
                new RecordSetInner()
                    .withMetadata(mapOf("key1", "value1"))
                    .withTtl(3600L)
                    .withSoaRecord(
                        new SoaRecord()
                            .withHost("ns1.contoso.com")
                            .withEmail("hostmaster.contoso.com")
                            .withSerialNumber(1L)
                            .withRefreshTime(3600L)
                            .withRetryTime(300L)
                            .withExpireTime(2419200L)
                            .withMinimumTtl(300L)),
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
import com.azure.resourcemanager.dns.generated.models.RecordType;

/** Samples for RecordSets Delete. */
public final class RecordSetsDeleteSamples {
    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/DeleteARecordset.json
     */
    /**
     * Sample code: Delete A recordset.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void deleteARecordset(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager.recordSets().deleteWithResponse("rg1", "zone1", "record1", RecordType.A, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/DeleteAAAARecordset.json
     */
    /**
     * Sample code: Delete AAAA recordset.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void deleteAAAARecordset(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager.recordSets().deleteWithResponse("rg1", "zone1", "record1", RecordType.AAAA, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/DeleteTXTRecordset.json
     */
    /**
     * Sample code: Delete TXT recordset.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void deleteTXTRecordset(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager.recordSets().deleteWithResponse("rg1", "zone1", "record1", RecordType.TXT, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/DeletePTRRecordset.json
     */
    /**
     * Sample code: Delete PTR recordset.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void deletePTRRecordset(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager.recordSets().deleteWithResponse("rg1", "0.0.127.in-addr.arpa", "1", RecordType.PTR, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/DeleteSRVRecordset.json
     */
    /**
     * Sample code: Delete SRV recordset.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void deleteSRVRecordset(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager.recordSets().deleteWithResponse("rg1", "zone1", "record1", RecordType.SRV, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/DeleteARecordset.json
     */
    /**
     * Sample code: Delete MX recordset.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void deleteMXRecordset(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager.recordSets().deleteWithResponse("rg1", "zone1", "record1", RecordType.A, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/DeleteCaaRecordset.json
     */
    /**
     * Sample code: Delete CAA recordset.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void deleteCAARecordset(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager.recordSets().deleteWithResponse("rg1", "zone1", "record1", RecordType.CAA, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/DeleteARecordset.json
     */
    /**
     * Sample code: Delete NS recordset.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void deleteNSRecordset(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager.recordSets().deleteWithResponse("rg1", "zone1", "record1", RecordType.A, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/DeleteARecordset.json
     */
    /**
     * Sample code: Delete CNAME recordset.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void deleteCNAMERecordset(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager.recordSets().deleteWithResponse("rg1", "zone1", "record1", RecordType.A, null, Context.NONE);
    }
}
```

### RecordSets_Get

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.dns.generated.models.RecordType;

/** Samples for RecordSets Get. */
public final class RecordSetsGetSamples {
    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/GetNSRecordset.json
     */
    /**
     * Sample code: Get NS recordset.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void getNSRecordset(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager.recordSets().getWithResponse("rg1", "zone1", "record1", RecordType.NS, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/GetAAAARecordset.json
     */
    /**
     * Sample code: Get AAAA recordset.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void getAAAARecordset(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager.recordSets().getWithResponse("rg1", "zone1", "record1", RecordType.AAAA, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/GetSRVRecordset.json
     */
    /**
     * Sample code: Get SRV recordset.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void getSRVRecordset(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager.recordSets().getWithResponse("rg1", "zone1", "record1", RecordType.SRV, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/GetCaaRecordset.json
     */
    /**
     * Sample code: Get CAA recordset.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void getCAARecordset(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager.recordSets().getWithResponse("rg1", "zone1", "record1", RecordType.CAA, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/GetCNAMERecordset.json
     */
    /**
     * Sample code: Get CNAME recordset.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void getCNAMERecordset(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager.recordSets().getWithResponse("rg1", "zone1", "record1", RecordType.CNAME, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/GetPTRRecordset.json
     */
    /**
     * Sample code: Get PTR recordset.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void getPTRRecordset(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager.recordSets().getWithResponse("rg1", "0.0.127.in-addr.arpa", "1", RecordType.PTR, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/GetTXTRecordset.json
     */
    /**
     * Sample code: Get TXT recordset.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void getTXTRecordset(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager.recordSets().getWithResponse("rg1", "zone1", "record1", RecordType.TXT, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/GetARecordset.json
     */
    /**
     * Sample code: Get A recordset.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void getARecordset(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager.recordSets().getWithResponse("rg1", "zone1", "record1", RecordType.A, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/GetMXRecordset.json
     */
    /**
     * Sample code: Get MX recordset.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void getMXRecordset(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager.recordSets().getWithResponse("rg1", "zone1", "record1", RecordType.MX, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/GetSOARecordset.json
     */
    /**
     * Sample code: Get SOA recordset.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void getSOARecordset(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager.recordSets().getWithResponse("rg1", "zone1", "@", RecordType.SOA, Context.NONE);
    }
}
```

### RecordSets_ListAllByDnsZone

```java
import com.azure.core.util.Context;

/** Samples for RecordSets ListAllByDnsZone. */
public final class RecordSetsListAllByDnsZoneSamples {
    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/ListRecordSetsByZone.json
     */
    /**
     * Sample code: List recordsets by zone.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void listRecordsetsByZone(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager.recordSets().listAllByDnsZone("rg1", "zone1", null, null, Context.NONE);
    }
}
```

### RecordSets_ListByDnsZone

```java
import com.azure.core.util.Context;

/** Samples for RecordSets ListByDnsZone. */
public final class RecordSetsListByDnsZoneSamples {
    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/ListRecordSetsByZone.json
     */
    /**
     * Sample code: List recordsets by zone.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void listRecordsetsByZone(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager.recordSets().listByDnsZone("rg1", "zone1", null, null, Context.NONE);
    }
}
```

### RecordSets_ListByType

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.dns.generated.models.RecordType;

/** Samples for RecordSets ListByType. */
public final class RecordSetsListByTypeSamples {
    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/ListAAAARecordset.json
     */
    /**
     * Sample code: List AAAA recordsets.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void listAAAARecordsets(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager.recordSets().listByType("rg1", "zone1", RecordType.AAAA, null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/ListSRVRecordset.json
     */
    /**
     * Sample code: List SRV recordsets.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void listSRVRecordsets(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager.recordSets().listByType("rg1", "zone1", RecordType.SRV, null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/ListPTRRecordset.json
     */
    /**
     * Sample code: List PTR recordsets.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void listPTRRecordsets(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager.recordSets().listByType("rg1", "0.0.127.in-addr.arpa", RecordType.PTR, null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/ListMXRecordset.json
     */
    /**
     * Sample code: List MX recordsets.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void listMXRecordsets(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager.recordSets().listByType("rg1", "zone1", RecordType.MX, null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/ListNSRecordset.json
     */
    /**
     * Sample code: List NS recordsets.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void listNSRecordsets(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager.recordSets().listByType("rg1", "zone1", RecordType.NS, null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/ListCNAMERecordset.json
     */
    /**
     * Sample code: List CNAME recordsets.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void listCNAMERecordsets(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager.recordSets().listByType("rg1", "zone1", RecordType.CNAME, null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/ListARecordset.json
     */
    /**
     * Sample code: List A recordsets.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void listARecordsets(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager.recordSets().listByType("rg1", "zone1", RecordType.A, null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/ListSOARecordset.json
     */
    /**
     * Sample code: List SOA recordsets.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void listSOARecordsets(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager.recordSets().listByType("rg1", "zone1", RecordType.SOA, null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/ListCaaRecordset.json
     */
    /**
     * Sample code: List CAA recordsets.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void listCAARecordsets(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager.recordSets().listByType("rg1", "zone1", RecordType.CAA, null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/ListTXTRecordset.json
     */
    /**
     * Sample code: List TXT recordsets.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void listTXTRecordsets(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager.recordSets().listByType("rg1", "zone1", RecordType.TXT, null, null, Context.NONE);
    }
}
```

### RecordSets_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.dns.generated.fluent.models.RecordSetInner;
import com.azure.resourcemanager.dns.generated.models.RecordType;
import java.util.HashMap;
import java.util.Map;

/** Samples for RecordSets Update. */
public final class RecordSetsUpdateSamples {
    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/PatchMXRecordset.json
     */
    /**
     * Sample code: Patch MX recordset.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void patchMXRecordset(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager
            .recordSets()
            .updateWithResponse(
                "rg1",
                "zone1",
                "record1",
                RecordType.MX,
                new RecordSetInner().withMetadata(mapOf("key2", "value2")),
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/PatchNSRecordset.json
     */
    /**
     * Sample code: Patch NS recordset.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void patchNSRecordset(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager
            .recordSets()
            .updateWithResponse(
                "rg1",
                "zone1",
                "record1",
                RecordType.NS,
                new RecordSetInner().withMetadata(mapOf("key2", "value2")),
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/PatchPTRRecordset.json
     */
    /**
     * Sample code: Patch PTR recordset.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void patchPTRRecordset(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager
            .recordSets()
            .updateWithResponse(
                "rg1",
                "0.0.127.in-addr.arpa",
                "1",
                RecordType.PTR,
                new RecordSetInner().withMetadata(mapOf("key2", "value2")),
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/PatchSOARecordset.json
     */
    /**
     * Sample code: Patch SOA recordset.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void patchSOARecordset(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager
            .recordSets()
            .updateWithResponse(
                "rg1",
                "zone1",
                "@",
                RecordType.SOA,
                new RecordSetInner().withMetadata(mapOf("key2", "value2")),
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/PatchARecordset.json
     */
    /**
     * Sample code: Patch A recordset.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void patchARecordset(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager
            .recordSets()
            .updateWithResponse(
                "rg1",
                "zone1",
                "record1",
                RecordType.A,
                new RecordSetInner().withMetadata(mapOf("key2", "value2")),
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/PatchTXTRecordset.json
     */
    /**
     * Sample code: Patch TXT recordset.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void patchTXTRecordset(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager
            .recordSets()
            .updateWithResponse(
                "rg1",
                "zone1",
                "record1",
                RecordType.TXT,
                new RecordSetInner().withMetadata(mapOf("key2", "value2")),
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/PatchCaaRecordset.json
     */
    /**
     * Sample code: Patch CAA recordset.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void patchCAARecordset(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager
            .recordSets()
            .updateWithResponse(
                "rg1",
                "zone1",
                "record1",
                RecordType.CAA,
                new RecordSetInner().withMetadata(mapOf("key2", "value2")),
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/PatchAAAARecordset.json
     */
    /**
     * Sample code: Patch AAAA recordset.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void patchAAAARecordset(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager
            .recordSets()
            .updateWithResponse(
                "rg1",
                "zone1",
                "record1",
                RecordType.AAAA,
                new RecordSetInner().withMetadata(mapOf("key2", "value2")),
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/PatchCNAMERecordset.json
     */
    /**
     * Sample code: Patch CNAME recordset.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void patchCNAMERecordset(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager
            .recordSets()
            .updateWithResponse(
                "rg1",
                "zone1",
                "record1",
                RecordType.CNAME,
                new RecordSetInner().withMetadata(mapOf("key2", "value2")),
                null,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/PatchSRVRecordset.json
     */
    /**
     * Sample code: Patch SRV recordset.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void patchSRVRecordset(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager
            .recordSets()
            .updateWithResponse(
                "rg1",
                "zone1",
                "record1",
                RecordType.SRV,
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

### Zones_CreateOrUpdate

```java
import java.util.HashMap;
import java.util.Map;

/** Samples for Zones CreateOrUpdate. */
public final class ZonesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/CreateOrUpdateZone.json
     */
    /**
     * Sample code: Create zone.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void createZone(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager
            .zones()
            .define("zone1")
            .withRegion("Global")
            .withExistingResourceGroup("rg1")
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

### Zones_Delete

```java
import com.azure.core.util.Context;

/** Samples for Zones Delete. */
public final class ZonesDeleteSamples {
    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/DeleteZone.json
     */
    /**
     * Sample code: Delete zone.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void deleteZone(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager.zones().delete("rg1", "zone1", null, Context.NONE);
    }
}
```

### Zones_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Zones GetByResourceGroup. */
public final class ZonesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/GetZone.json
     */
    /**
     * Sample code: Get zone.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void getZone(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager.zones().getByResourceGroupWithResponse("rg1", "zone1", Context.NONE);
    }
}
```

### Zones_List

```java
import com.azure.core.util.Context;

/** Samples for Zones List. */
public final class ZonesListSamples {
    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/ListZonesBySubscription.json
     */
    /**
     * Sample code: List zones by subscription.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void listZonesBySubscription(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager.zones().list(null, Context.NONE);
    }
}
```

### Zones_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Zones ListByResourceGroup. */
public final class ZonesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/ListZonesByResourceGroup.json
     */
    /**
     * Sample code: List zones by resource group.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void listZonesByResourceGroup(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        manager.zones().listByResourceGroup("rg1", null, Context.NONE);
    }
}
```

### Zones_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.dns.generated.models.Zone;
import java.util.HashMap;
import java.util.Map;

/** Samples for Zones Update. */
public final class ZonesUpdateSamples {
    /*
     * x-ms-original-file: specification/dns/resource-manager/Microsoft.Network/stable/2018-05-01/examples/PatchZone.json
     */
    /**
     * Sample code: Patch zone.
     *
     * @param manager Entry point to DnsManager.
     */
    public static void patchZone(com.azure.resourcemanager.dns.generated.DnsManager manager) {
        Zone resource = manager.zones().getByResourceGroupWithResponse("rg1", "zone1", Context.NONE).getValue();
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

