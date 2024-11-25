# Code snippets and samples


## CacheNodesOperations

- [CreateorUpdate](#cachenodesoperations_createorupdate)
- [Delete](#cachenodesoperations_delete)
- [GetByResourceGroup](#cachenodesoperations_getbyresourcegroup)
- [List](#cachenodesoperations_list)
- [ListByResourceGroup](#cachenodesoperations_listbyresourcegroup)
- [Update](#cachenodesoperations_update)

## EnterpriseCustomerOperations

- [CreateOrUpdate](#enterprisecustomeroperations_createorupdate)
- [Delete](#enterprisecustomeroperations_delete)
- [GetByResourceGroup](#enterprisecustomeroperations_getbyresourcegroup)
- [List](#enterprisecustomeroperations_list)
- [ListByResourceGroup](#enterprisecustomeroperations_listbyresourcegroup)
- [Update](#enterprisecustomeroperations_update)

## EnterpriseMccCacheNodesOperations

- [CreateOrUpdate](#enterprisemcccachenodesoperations_createorupdate)
- [Delete](#enterprisemcccachenodesoperations_delete)
- [Get](#enterprisemcccachenodesoperations_get)
- [GetCacheNodeInstallDetails](#enterprisemcccachenodesoperations_getcachenodeinstalldetails)
- [ListByEnterpriseMccCustomerResource](#enterprisemcccachenodesoperations_listbyenterprisemcccustomerresource)
- [Update](#enterprisemcccachenodesoperations_update)

## EnterpriseMccCustomers

- [CreateOrUpdate](#enterprisemcccustomers_createorupdate)
- [Delete](#enterprisemcccustomers_delete)
- [GetByResourceGroup](#enterprisemcccustomers_getbyresourcegroup)
- [List](#enterprisemcccustomers_list)
- [ListByResourceGroup](#enterprisemcccustomers_listbyresourcegroup)
- [Update](#enterprisemcccustomers_update)

## IspCacheNodesOperations

- [CreateOrUpdate](#ispcachenodesoperations_createorupdate)
- [Delete](#ispcachenodesoperations_delete)
- [Get](#ispcachenodesoperations_get)
- [GetBgpCidrs](#ispcachenodesoperations_getbgpcidrs)
- [GetCacheNodeInstallDetails](#ispcachenodesoperations_getcachenodeinstalldetails)
- [ListByIspCustomerResource](#ispcachenodesoperations_listbyispcustomerresource)
- [Update](#ispcachenodesoperations_update)

## IspCustomers

- [CreateOrUpdate](#ispcustomers_createorupdate)
- [Delete](#ispcustomers_delete)
- [GetByResourceGroup](#ispcustomers_getbyresourcegroup)
- [List](#ispcustomers_list)
- [ListByResourceGroup](#ispcustomers_listbyresourcegroup)
- [Update](#ispcustomers_update)

## Operations

- [List](#operations_list)
### CacheNodesOperations_CreateorUpdate

```java
import com.azure.core.management.exception.ManagementError;
import com.azure.resourcemanager.connectedcache.models.CacheNodeOldResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for CacheNodesOperations CreateorUpdate.
 */
public final class CacheNodesOperationsCreateorUpdateSamples {
    /*
     * x-ms-original-file: 2023-05-01-preview/CacheNodesOperations_CreateorUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: CacheNodesOperations_CreateorUpdate.
     * 
     * @param manager Entry point to ConnectedCacheManager.
     */
    public static void
        cacheNodesOperationsCreateorUpdate(com.azure.resourcemanager.connectedcache.ConnectedCacheManager manager) {
        manager.cacheNodesOperations()
            .define("lwrsyhvfpcfrwrim")
            .withRegion("westus")
            .withExistingResourceGroup("rgConnectedCache")
            .withTags(mapOf("key8256", "fakeTokenPlaceholder"))
            .withProperties(new CacheNodeOldResponse().withStatusCode("fakeTokenPlaceholder")
                .withStatusText("bjnsrpzaofjntleoesjwammgbi")
                .withStatusDetails("quuziibkwtgf")
                .withError(new ManagementError()))
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

### CacheNodesOperations_Delete

```java
/**
 * Samples for CacheNodesOperations Delete.
 */
public final class CacheNodesOperationsDeleteSamples {
    /*
     * x-ms-original-file: 2023-05-01-preview/CacheNodesOperations_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: CacheNodesOperations_Delete.
     * 
     * @param manager Entry point to ConnectedCacheManager.
     */
    public static void
        cacheNodesOperationsDelete(com.azure.resourcemanager.connectedcache.ConnectedCacheManager manager) {
        manager.cacheNodesOperations()
            .deleteByResourceGroupWithResponse("rgConnectedCache",
                "otvtvhmovthjwzjzbsqkbnmpcmmeianpqxmmaspvdczmrenquxigrtuarmlcmvsnaclhcbw",
                com.azure.core.util.Context.NONE);
    }
}
```

### CacheNodesOperations_GetByResourceGroup

```java
/**
 * Samples for CacheNodesOperations GetByResourceGroup.
 */
public final class CacheNodesOperationsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2023-05-01-preview/CacheNodesOperations_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: CacheNodesOperations_Get.
     * 
     * @param manager Entry point to ConnectedCacheManager.
     */
    public static void cacheNodesOperationsGet(com.azure.resourcemanager.connectedcache.ConnectedCacheManager manager) {
        manager.cacheNodesOperations()
            .getByResourceGroupWithResponse("rgConnectedCache", "nqoxkgorhuzbhjwcegymzqbeydzjupemekt",
                com.azure.core.util.Context.NONE);
    }
}
```

### CacheNodesOperations_List

```java
/**
 * Samples for CacheNodesOperations List.
 */
public final class CacheNodesOperationsListSamples {
    /*
     * x-ms-original-file: 2023-05-01-preview/CacheNodesOperations_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: CacheNodes Private Preview(Legacy) resource List by Subscription - generated by [MaximumSet] rule.
     * 
     * @param manager Entry point to ConnectedCacheManager.
     */
    public static void cacheNodesPrivatePreviewLegacyResourceListBySubscriptionGeneratedByMaximumSetRule(
        com.azure.resourcemanager.connectedcache.ConnectedCacheManager manager) {
        manager.cacheNodesOperations().list(com.azure.core.util.Context.NONE);
    }
}
```

### CacheNodesOperations_ListByResourceGroup

```java
/**
 * Samples for CacheNodesOperations ListByResourceGroup.
 */
public final class CacheNodesOperationsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2023-05-01-preview/CacheNodesOperations_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: CacheNodes Private Preview(Legacy) Get Operation List by Resource group - generated by [MaximumSet]
     * rule.
     * 
     * @param manager Entry point to ConnectedCacheManager.
     */
    public static void cacheNodesPrivatePreviewLegacyGetOperationListByResourceGroupGeneratedByMaximumSetRule(
        com.azure.resourcemanager.connectedcache.ConnectedCacheManager manager) {
        manager.cacheNodesOperations().listByResourceGroup("rgConnectedCache", com.azure.core.util.Context.NONE);
    }
}
```

### CacheNodesOperations_Update

```java
import com.azure.resourcemanager.connectedcache.models.CacheNodePreviewResource;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for CacheNodesOperations Update.
 */
public final class CacheNodesOperationsUpdateSamples {
    /*
     * x-ms-original-file: 2023-05-01-preview/CacheNodesOperations_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: CacheNodes Private Preview(Legacy) Update Operation - generated by [MaximumSet] rule.
     * 
     * @param manager Entry point to ConnectedCacheManager.
     */
    public static void cacheNodesPrivatePreviewLegacyUpdateOperationGeneratedByMaximumSetRule(
        com.azure.resourcemanager.connectedcache.ConnectedCacheManager manager) {
        CacheNodePreviewResource resource = manager.cacheNodesOperations()
            .getByResourceGroupWithResponse("rgConnectedCache", "wlrwpdbcv", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("key5032", "fakeTokenPlaceholder")).apply();
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

### EnterpriseCustomerOperations_CreateOrUpdate

```java
import com.azure.core.management.exception.ManagementError;
import com.azure.resourcemanager.connectedcache.models.CacheNodeOldResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for EnterpriseCustomerOperations CreateOrUpdate.
 */
public final class EnterpriseCustomerOperationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2023-05-01-preview/EnterpriseCustomerOperations_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: EnterpriseCustomerOperations_CreateOrUpdate.
     * 
     * @param manager Entry point to ConnectedCacheManager.
     */
    public static void enterpriseCustomerOperationsCreateOrUpdate(
        com.azure.resourcemanager.connectedcache.ConnectedCacheManager manager) {
        manager.enterpriseCustomerOperations()
            .define("l")
            .withRegion("zdzhhkjyogrqxwihkifnmeyhwpujbr")
            .withExistingResourceGroup("rgConnectedCache")
            .withTags(mapOf("key4215", "fakeTokenPlaceholder"))
            .withProperties(new CacheNodeOldResponse().withStatusCode("fakeTokenPlaceholder")
                .withStatusText("bs")
                .withStatusDetails("lhwvcz")
                .withError(new ManagementError()))
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

### EnterpriseCustomerOperations_Delete

```java
/**
 * Samples for EnterpriseCustomerOperations Delete.
 */
public final class EnterpriseCustomerOperationsDeleteSamples {
    /*
     * x-ms-original-file: 2023-05-01-preview/EnterpriseCustomerOperations_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: EnterpriseCustomerOperations_Delete.
     * 
     * @param manager Entry point to ConnectedCacheManager.
     */
    public static void
        enterpriseCustomerOperationsDelete(com.azure.resourcemanager.connectedcache.ConnectedCacheManager manager) {
        manager.enterpriseCustomerOperations()
            .deleteByResourceGroupWithResponse("rgConnectedCache",
                "jeubxmhiaihcusgnahblvvckbdcetacvqgwbohlrqucodtlwuyefpejskvamgrdnwgucziodcfpjhqy",
                com.azure.core.util.Context.NONE);
    }
}
```

### EnterpriseCustomerOperations_GetByResourceGroup

```java
/**
 * Samples for EnterpriseCustomerOperations GetByResourceGroup.
 */
public final class EnterpriseCustomerOperationsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2023-05-01-preview/EnterpriseCustomerOperations_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: EnterpriseCustomerOperations_Get.
     * 
     * @param manager Entry point to ConnectedCacheManager.
     */
    public static void
        enterpriseCustomerOperationsGet(com.azure.resourcemanager.connectedcache.ConnectedCacheManager manager) {
        manager.enterpriseCustomerOperations()
            .getByResourceGroupWithResponse("rgConnectedCache", "MCCTPTest2", com.azure.core.util.Context.NONE);
    }
}
```

### EnterpriseCustomerOperations_List

```java
/**
 * Samples for EnterpriseCustomerOperations List.
 */
public final class EnterpriseCustomerOperationsListSamples {
    /*
     * x-ms-original-file: 2023-05-01-preview/EnterpriseCustomerOperations_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: EnterpriseCustomerOperations_ListBySubscription.
     * 
     * @param manager Entry point to ConnectedCacheManager.
     */
    public static void enterpriseCustomerOperationsListBySubscription(
        com.azure.resourcemanager.connectedcache.ConnectedCacheManager manager) {
        manager.enterpriseCustomerOperations().list(com.azure.core.util.Context.NONE);
    }
}
```

### EnterpriseCustomerOperations_ListByResourceGroup

```java
/**
 * Samples for EnterpriseCustomerOperations ListByResourceGroup.
 */
public final class EnterpriseCustomerOperationsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2023-05-01-preview/EnterpriseCustomerOperations_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: EnterpriseCustomerOperations_ListByResourceGroup.
     * 
     * @param manager Entry point to ConnectedCacheManager.
     */
    public static void enterpriseCustomerOperationsListByResourceGroup(
        com.azure.resourcemanager.connectedcache.ConnectedCacheManager manager) {
        manager.enterpriseCustomerOperations()
            .listByResourceGroup("rgConnectedCache", com.azure.core.util.Context.NONE);
    }
}
```

### EnterpriseCustomerOperations_Update

```java
import com.azure.resourcemanager.connectedcache.models.EnterprisePreviewResource;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for EnterpriseCustomerOperations Update.
 */
public final class EnterpriseCustomerOperationsUpdateSamples {
    /*
     * x-ms-original-file: 2023-05-01-preview/EnterpriseCustomerOperations_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: EnterpriseCustomerOperations_Update.
     * 
     * @param manager Entry point to ConnectedCacheManager.
     */
    public static void
        enterpriseCustomerOperationsUpdate(com.azure.resourcemanager.connectedcache.ConnectedCacheManager manager) {
        EnterprisePreviewResource resource = manager.enterpriseCustomerOperations()
            .getByResourceGroupWithResponse("rgConnectedCache", "MCCTPTest2", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("key1653", "fakeTokenPlaceholder")).apply();
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

### EnterpriseMccCacheNodesOperations_CreateOrUpdate

```java
import com.azure.core.management.exception.ManagementError;
import com.azure.resourcemanager.connectedcache.models.AdditionalCacheNodeProperties;
import com.azure.resourcemanager.connectedcache.models.BgpConfiguration;
import com.azure.resourcemanager.connectedcache.models.CacheNodeDriveConfiguration;
import com.azure.resourcemanager.connectedcache.models.CacheNodeEntity;
import com.azure.resourcemanager.connectedcache.models.CacheNodeProperty;
import com.azure.resourcemanager.connectedcache.models.ProxyUrlConfiguration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for EnterpriseMccCacheNodesOperations CreateOrUpdate.
 */
public final class EnterpriseMccCacheNodesOperationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2023-05-01-preview/EnterpriseMccCacheNodesOperations_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: Creates an enterpriseMccCacheNode resource - generated by [MaximumSet] rule.
     * 
     * @param manager Entry point to ConnectedCacheManager.
     */
    public static void createsAnEnterpriseMccCacheNodeResourceGeneratedByMaximumSetRule(
        com.azure.resourcemanager.connectedcache.ConnectedCacheManager manager) {
        manager.enterpriseMccCacheNodesOperations()
            .define("fgduqdovidpemlnmhelomffuafdrbgaasqznvrdkbvspfzsnrhncdtqquhijhdpwyzwleukqldpceyxqhqlftqrr")
            .withRegion("westus")
            .withExistingEnterpriseMccCustomer("rgConnectedCache", "nhdkvstdrrtsxxuz")
            .withTags(mapOf("key259", "fakeTokenPlaceholder"))
            .withProperties(new CacheNodeProperty()
                .withCacheNode(new CacheNodeEntity().withFullyQualifiedResourceId("yeinlleavzbehelhsucb")
                    .withCustomerName("zsklcocjfjhkcpmzyefzkwamdzc")
                    .withIpAddress("gbfkdhloyphnpnhemwrcrxlk")
                    .withCustomerIndex("vafvezmelfgmjsrccjukrhppljvipg")
                    .withCacheNodeId("fmrjefyddfn")
                    .withCacheNodeName("qppvqxliajjfoalzjbgmxggr")
                    .withCustomerAsn(25)
                    .withIsEnabled(true)
                    .withMaxAllowableEgressInMbps(27)
                    .withIsEnterpriseManaged(true)
                    .withCidrCsv(Arrays.asList("nlqlvrthafvvljuupcbcw"))
                    .withShouldMigrate(true)
                    .withCidrSelectionType(11))
                .withAdditionalCacheNodeProperties(new AdditionalCacheNodeProperties()
                    .withCacheNodePropertiesDetailsIssuesList(Arrays.asList("ennbzfpuszgalzpawmwicaofqcwcj"))
                    .withDriveConfiguration(
                        Arrays.asList(new CacheNodeDriveConfiguration().withPhysicalPath("pcbkezoofhamkycot")
                            .withSizeInGb(14)
                            .withCacheNumber(11)
                            .withNginxMapping("cirlpkpuxg")))
                    .withBgpConfiguration(new BgpConfiguration().withAsnToIpAddressMapping("fjbggfvumrn"))
                    .withProxyUrlConfiguration(new ProxyUrlConfiguration().withProxyUrl("hplstyg"))
                    .withProxyUrl("ihkzxlzvpcywtzrogupqozkdud")
                    .withOptionalProperty1("ph")
                    .withOptionalProperty2("soqqgpgcbhb")
                    .withOptionalProperty3("fpnycrbagptsujiotnjfuhlm")
                    .withOptionalProperty4("gesqugrxvhxlxxyvatgrautxwlmxsf")
                    .withOptionalProperty5("zknjgzpaqtvdqjydd"))
                .withStatusCode("fakeTokenPlaceholder")
                .withStatusText("Success")
                .withStatusDetails("lgljxmyyoakw")
                .withError(new ManagementError()))
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

### EnterpriseMccCacheNodesOperations_Delete

```java
/**
 * Samples for EnterpriseMccCacheNodesOperations Delete.
 */
public final class EnterpriseMccCacheNodesOperationsDeleteSamples {
    /*
     * x-ms-original-file: 2023-05-01-preview/EnterpriseMccCacheNodesOperations_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: EnterpriseMccCacheNodesOperations Delete Operation - generated by [MaximumSet] rule.
     * 
     * @param manager Entry point to ConnectedCacheManager.
     */
    public static void enterpriseMccCacheNodesOperationsDeleteOperationGeneratedByMaximumSetRule(
        com.azure.resourcemanager.connectedcache.ConnectedCacheManager manager) {
        manager.enterpriseMccCacheNodesOperations()
            .delete("rgConnectedCache", "hsincngmssuzeyispnufqwinpopadvhsbsemisguxgovwdjwviqexocelijvuyr",
                "vwtrhdoxvkrunpliwcao", com.azure.core.util.Context.NONE);
    }
}
```

### EnterpriseMccCacheNodesOperations_Get

```java
/**
 * Samples for EnterpriseMccCacheNodesOperations Get.
 */
public final class EnterpriseMccCacheNodesOperationsGetSamples {
    /*
     * x-ms-original-file: 2023-05-01-preview/EnterpriseMccCacheNodesOperations_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Gets enterpriseMccCacheNode resource information of an enterprise mcc customer parent resource -
     * generated by [MaximumSet] rule.
     * 
     * @param manager Entry point to ConnectedCacheManager.
     */
    public static void
        getsEnterpriseMccCacheNodeResourceInformationOfAnEnterpriseMccCustomerParentResourceGeneratedByMaximumSetRule(
            com.azure.resourcemanager.connectedcache.ConnectedCacheManager manager) {
        manager.enterpriseMccCacheNodesOperations()
            .getWithResponse("rgConnectedCache", "cygqjgtcetihsajgyqwwrbclssqsvhgltrboemcqqtpoxdbhykqxblaihmrumyhbsx",
                "fqxfadsultwjfzdwlqkvneakfsbyhratytmssmiukpbnus", com.azure.core.util.Context.NONE);
    }
}
```

### EnterpriseMccCacheNodesOperations_GetCacheNodeInstallDetails

```java
/**
 * Samples for EnterpriseMccCacheNodesOperations GetCacheNodeInstallDetails.
 */
public final class EnterpriseMccCacheNodesOperationsGetCacheNodeInstallDetailsSamples {
    /*
     * x-ms-original-file:
     * 2023-05-01-preview/EnterpriseMccCacheNodesOperations_GetCacheNodeInstallDetails_MaximumSet_Gen.json
     */
    /**
     * Sample code: Gets required properties for enterprise Mcc CacheNode resource install key details - generated by
     * [MaximumSet] rule.
     * 
     * @param manager Entry point to ConnectedCacheManager.
     */
    public static void
        getsRequiredPropertiesForEnterpriseMccCacheNodeResourceInstallKeyDetailsGeneratedByMaximumSetRule(
            com.azure.resourcemanager.connectedcache.ConnectedCacheManager manager) {
        manager.enterpriseMccCacheNodesOperations()
            .getCacheNodeInstallDetailsWithResponse("rgConnectedCache", "fzwxcjmdpxxzayecabqqlh",
                "ccexmqqttritxvtctivraso", com.azure.core.util.Context.NONE);
    }
}
```

### EnterpriseMccCacheNodesOperations_ListByEnterpriseMccCustomerResource

```java
/**
 * Samples for EnterpriseMccCacheNodesOperations ListByEnterpriseMccCustomerResource.
 */
public final class EnterpriseMccCacheNodesOperationsListByEnterpriseMccCustomerResourceSamples {
    /*
     * x-ms-original-file:
     * 2023-05-01-preview/EnterpriseMccCacheNodesOperations_ListByEnterpriseMccCustomerResource_MaximumSet_Gen.json
     */
    /**
     * Sample code: EnterpriseMccCacheNodesOperations_ListByEnterpriseMccCustomerResource.
     * 
     * @param manager Entry point to ConnectedCacheManager.
     */
    public static void enterpriseMccCacheNodesOperationsListByEnterpriseMccCustomerResource(
        com.azure.resourcemanager.connectedcache.ConnectedCacheManager manager) {
        manager.enterpriseMccCacheNodesOperations()
            .listByEnterpriseMccCustomerResource("rgConnectedCache", "syjjjzk", com.azure.core.util.Context.NONE);
    }
}
```

### EnterpriseMccCacheNodesOperations_Update

```java
import com.azure.resourcemanager.connectedcache.models.EnterpriseMccCacheNodeResource;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for EnterpriseMccCacheNodesOperations Update.
 */
public final class EnterpriseMccCacheNodesOperationsUpdateSamples {
    /*
     * x-ms-original-file: 2023-05-01-preview/EnterpriseMccCacheNodesOperations_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: EnterpriseMccCacheNodesOperations Update - generated by [MaximumSet] rule.
     * 
     * @param manager Entry point to ConnectedCacheManager.
     */
    public static void enterpriseMccCacheNodesOperationsUpdateGeneratedByMaximumSetRule(
        com.azure.resourcemanager.connectedcache.ConnectedCacheManager manager) {
        EnterpriseMccCacheNodeResource resource = manager.enterpriseMccCacheNodesOperations()
            .getWithResponse("rgConnectedCache", "qanjqtvrxzjkljdysdjvdiqcxkttskpdzykzuefhbhz",
                "kllmlvazrcxmfjfozulzqnsgvspgwmhogcafvauchunlgfr", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("key1653", "fakeTokenPlaceholder")).apply();
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

### EnterpriseMccCustomers_CreateOrUpdate

```java
import com.azure.resourcemanager.connectedcache.models.AdditionalCustomerProperties;
import com.azure.resourcemanager.connectedcache.models.CustomerEntity;
import com.azure.resourcemanager.connectedcache.models.CustomerProperty;
import com.azure.resourcemanager.connectedcache.models.CustomerTransitState;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for EnterpriseMccCustomers CreateOrUpdate.
 */
public final class EnterpriseMccCustomersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2023-05-01-preview/EnterpriseMccCustomers_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: EnterpriseMccCustomers_CreateOrUpdate.
     * 
     * @param manager Entry point to ConnectedCacheManager.
     */
    public static void
        enterpriseMccCustomersCreateOrUpdate(com.azure.resourcemanager.connectedcache.ConnectedCacheManager manager) {
        manager.enterpriseMccCustomers()
            .define("MccRPTest1")
            .withRegion("westus")
            .withExistingResourceGroup("rgConnectedCache")
            .withTags(mapOf("key3379", "fakeTokenPlaceholder"))
            .withProperties(new CustomerProperty()
                .withCustomer(new CustomerEntity().withFullyQualifiedResourceId("uqsbtgae")
                    .withCustomerName("mkpzynfqihnjfdbaqbqwyhd")
                    .withContactEmail("xquos")
                    .withContactPhone("vue")
                    .withContactName("wxyqjoyoscmvimgwhpitxky")
                    .withIsEntitled(true)
                    .withReleaseVersion(20)
                    .withClientTenantId("fproidkpgvpdnac")
                    .withIsEnterpriseManaged(true)
                    .withShouldMigrate(true)
                    .withResendSignupCode(true)
                    .withVerifySignupCode(true)
                    .withVerifySignupPhrase("tprjvttkgmrqlsyicnidhm"))
                .withAdditionalCustomerProperties(new AdditionalCustomerProperties().withCustomerEmail("zdjgibsidydyzm")
                    .withCustomerTransitAsn("habgklnxqzmozqpazoyejwiphezpi")
                    .withCustomerTransitState(CustomerTransitState.fromString("voblixkxfejbmhxilb"))
                    .withCustomerAsn("hgrelgnrtdkleisnepfolu")
                    .withCustomerEntitlementSkuId("b")
                    .withCustomerEntitlementSkuGuid("rvzmdpxyflgqetvpwupnfaxsweiiz")
                    .withCustomerEntitlementSkuName("waaqfijr")
                    .withCustomerEntitlementExpiration(OffsetDateTime.parse("2024-01-30T00:54:04.773Z"))
                    .withOptionalProperty1("qhmwxza")
                    .withOptionalProperty2("l")
                    .withOptionalProperty3("mblwwvbie")
                    .withOptionalProperty4("vzuek")
                    .withOptionalProperty5("fzjodscdfcdr")))
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

### EnterpriseMccCustomers_Delete

```java
/**
 * Samples for EnterpriseMccCustomers Delete.
 */
public final class EnterpriseMccCustomersDeleteSamples {
    /*
     * x-ms-original-file: 2023-05-01-preview/EnterpriseMccCustomers_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: EnterpriseMccCustomers_Delete.
     * 
     * @param manager Entry point to ConnectedCacheManager.
     */
    public static void
        enterpriseMccCustomersDelete(com.azure.resourcemanager.connectedcache.ConnectedCacheManager manager) {
        manager.enterpriseMccCustomers().delete("rgConnectedCache", "zktb", com.azure.core.util.Context.NONE);
    }
}
```

### EnterpriseMccCustomers_GetByResourceGroup

```java
/**
 * Samples for EnterpriseMccCustomers GetByResourceGroup.
 */
public final class EnterpriseMccCustomersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2023-05-01-preview/EnterpriseMccCustomers_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: EnterpriseMccCustomers_Get.
     * 
     * @param manager Entry point to ConnectedCacheManager.
     */
    public static void
        enterpriseMccCustomersGet(com.azure.resourcemanager.connectedcache.ConnectedCacheManager manager) {
        manager.enterpriseMccCustomers()
            .getByResourceGroupWithResponse("rgConnectedCache", "pvilvqkofbjbykupeewgvzlmjao",
                com.azure.core.util.Context.NONE);
    }
}
```

### EnterpriseMccCustomers_List

```java
/**
 * Samples for EnterpriseMccCustomers List.
 */
public final class EnterpriseMccCustomersListSamples {
    /*
     * x-ms-original-file: 2023-05-01-preview/EnterpriseMccCustomers_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: EnterpriseMccCustomers_ListBySubscription.
     * 
     * @param manager Entry point to ConnectedCacheManager.
     */
    public static void enterpriseMccCustomersListBySubscription(
        com.azure.resourcemanager.connectedcache.ConnectedCacheManager manager) {
        manager.enterpriseMccCustomers().list(com.azure.core.util.Context.NONE);
    }
}
```

### EnterpriseMccCustomers_ListByResourceGroup

```java
/**
 * Samples for EnterpriseMccCustomers ListByResourceGroup.
 */
public final class EnterpriseMccCustomersListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2023-05-01-preview/EnterpriseMccCustomers_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: EnterpriseMccCustomers_ListByResourceGroup.
     * 
     * @param manager Entry point to ConnectedCacheManager.
     */
    public static void enterpriseMccCustomersListByResourceGroup(
        com.azure.resourcemanager.connectedcache.ConnectedCacheManager manager) {
        manager.enterpriseMccCustomers().listByResourceGroup("rgConnectedCache", com.azure.core.util.Context.NONE);
    }
}
```

### EnterpriseMccCustomers_Update

```java
import com.azure.resourcemanager.connectedcache.models.EnterpriseMccCustomerResource;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for EnterpriseMccCustomers Update.
 */
public final class EnterpriseMccCustomersUpdateSamples {
    /*
     * x-ms-original-file: 2023-05-01-preview/EnterpriseMccCustomers_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: EnterpriseMccCustomers_Update.
     * 
     * @param manager Entry point to ConnectedCacheManager.
     */
    public static void
        enterpriseMccCustomersUpdate(com.azure.resourcemanager.connectedcache.ConnectedCacheManager manager) {
        EnterpriseMccCustomerResource resource = manager.enterpriseMccCustomers()
            .getByResourceGroupWithResponse("rgConnectedCache", "MccRPTest1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("key1878", "fakeTokenPlaceholder")).apply();
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

### IspCacheNodesOperations_CreateOrUpdate

```java
import com.azure.core.management.exception.ManagementError;
import com.azure.resourcemanager.connectedcache.models.AdditionalCacheNodeProperties;
import com.azure.resourcemanager.connectedcache.models.BgpConfiguration;
import com.azure.resourcemanager.connectedcache.models.CacheNodeDriveConfiguration;
import com.azure.resourcemanager.connectedcache.models.CacheNodeEntity;
import com.azure.resourcemanager.connectedcache.models.CacheNodeProperty;
import com.azure.resourcemanager.connectedcache.models.ProxyUrlConfiguration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for IspCacheNodesOperations CreateOrUpdate.
 */
public final class IspCacheNodesOperationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2023-05-01-preview/IspCacheNodesOperations_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: IspCacheNodesOperations_CreateOrUpdate.
     * 
     * @param manager Entry point to ConnectedCacheManager.
     */
    public static void
        ispCacheNodesOperationsCreateOrUpdate(com.azure.resourcemanager.connectedcache.ConnectedCacheManager manager) {
        manager.ispCacheNodesOperations()
            .define("cabakm")
            .withRegion("westus")
            .withExistingIspCustomer("rgConnectedCache", "zpqzbmanlljgmkrthtydrtneavhlnlqkdmviq")
            .withTags(mapOf("key4171", "fakeTokenPlaceholder"))
            .withProperties(new CacheNodeProperty()
                .withCacheNode(new CacheNodeEntity().withFullyQualifiedResourceId("hskxkpbiqbrbjiwdzrxndru")
                    .withCustomerName("xwyqk")
                    .withIpAddress("voctagljcwqgcpnionqdcbjk")
                    .withCustomerIndex("qtoiglqaswivmkjhzogburcxtszmek")
                    .withCacheNodeId("xjzffjftwcgsehanoxsl")
                    .withCacheNodeName("mfjxb")
                    .withCustomerAsn(4)
                    .withIsEnabled(true)
                    .withMaxAllowableEgressInMbps(29)
                    .withIsEnterpriseManaged(true)
                    .withCidrCsv(Arrays.asList("nlqlvrthafvvljuupcbcw"))
                    .withShouldMigrate(true)
                    .withCidrSelectionType(4))
                .withAdditionalCacheNodeProperties(
                    new AdditionalCacheNodeProperties().withCacheNodePropertiesDetailsIssuesList(Arrays.asList("ex"))
                        .withDriveConfiguration(Arrays.asList(new CacheNodeDriveConfiguration().withPhysicalPath("/mcc")
                            .withSizeInGb(500)
                            .withCacheNumber(1)
                            .withNginxMapping("lijygenjq")))
                        .withBgpConfiguration(new BgpConfiguration().withAsnToIpAddressMapping("pafcimhoog"))
                        .withProxyUrlConfiguration(new ProxyUrlConfiguration().withProxyUrl("hplstyg"))
                        .withProxyUrl("qhux")
                        .withOptionalProperty1("hvpmt")
                        .withOptionalProperty2("talanelmsgxvksrzoeeontqkjzbpv")
                        .withOptionalProperty3("bxkoxq")
                        .withOptionalProperty4("pqlkcekupusoc")
                        .withOptionalProperty5("nyvvmrjigqdufzjdvazdca"))
                .withStatusCode("fakeTokenPlaceholder")
                .withStatusText("Success")
                .withStatusDetails("djruqvptzxak")
                .withError(new ManagementError()))
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

### IspCacheNodesOperations_Delete

```java
/**
 * Samples for IspCacheNodesOperations Delete.
 */
public final class IspCacheNodesOperationsDeleteSamples {
    /*
     * x-ms-original-file: 2023-05-01-preview/IspCacheNodesOperations_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: IspCacheNodes delete Operation - generated by [MaximumSet] rule.
     * 
     * @param manager Entry point to ConnectedCacheManager.
     */
    public static void ispCacheNodesDeleteOperationGeneratedByMaximumSetRule(
        com.azure.resourcemanager.connectedcache.ConnectedCacheManager manager) {
        manager.ispCacheNodesOperations()
            .delete("rgConnectedCache", "lvpcosvbfxtpzscakewx",
                "wsiruvexelltpbouqxvsogqpxdizcwqwfowybncvjunlakjwcpgmqbdbgzjrsmxlkczxnsxfonhnqqa",
                com.azure.core.util.Context.NONE);
    }
}
```

### IspCacheNodesOperations_Get

```java
/**
 * Samples for IspCacheNodesOperations Get.
 */
public final class IspCacheNodesOperationsGetSamples {
    /*
     * x-ms-original-file: 2023-05-01-preview/IspCacheNodesOperations_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: IspCacheNodes Get resource - generated by [MaximumSet] rule.
     * 
     * @param manager Entry point to ConnectedCacheManager.
     */
    public static void ispCacheNodesGetResourceGeneratedByMaximumSetRule(
        com.azure.resourcemanager.connectedcache.ConnectedCacheManager manager) {
        manager.ispCacheNodesOperations()
            .getWithResponse("rgConnectedCache",
                "sgtqjsitdrskmgyrrkntszwrrjjkretscpjgaezraucvcwececlelcsorfunrnvxyxcsrg", "lbsziwmudcjnwnwy",
                com.azure.core.util.Context.NONE);
    }
}
```

### IspCacheNodesOperations_GetBgpCidrs

```java
/**
 * Samples for IspCacheNodesOperations GetBgpCidrs.
 */
public final class IspCacheNodesOperationsGetBgpCidrsSamples {
    /*
     * x-ms-original-file: 2023-05-01-preview/IspCacheNodesOperations_GetBgpCidrs_MaximumSet_Gen.json
     */
    /**
     * Sample code: IspCacheNodes resource BgpCidrs details - generated by [MaximumSet] rule.
     * 
     * @param manager Entry point to ConnectedCacheManager.
     */
    public static void ispCacheNodesResourceBgpCidrsDetailsGeneratedByMaximumSetRule(
        com.azure.resourcemanager.connectedcache.ConnectedCacheManager manager) {
        manager.ispCacheNodesOperations()
            .getBgpCidrsWithResponse("rgConnectedCache", "MccRPTest1", "MCCCachenode1",
                com.azure.core.util.Context.NONE);
    }
}
```

### IspCacheNodesOperations_GetCacheNodeInstallDetails

```java
/**
 * Samples for IspCacheNodesOperations GetCacheNodeInstallDetails.
 */
public final class IspCacheNodesOperationsGetCacheNodeInstallDetailsSamples {
    /*
     * x-ms-original-file: 2023-05-01-preview/IspCacheNodesOperations_GetCacheNodeInstallDetails_MaximumSet_Gen.json
     */
    /**
     * Sample code: ispCacheNode resource get install details - generated by [MaximumSet] rule.
     * 
     * @param manager Entry point to ConnectedCacheManager.
     */
    public static void ispCacheNodeResourceGetInstallDetailsGeneratedByMaximumSetRule(
        com.azure.resourcemanager.connectedcache.ConnectedCacheManager manager) {
        manager.ispCacheNodesOperations()
            .getCacheNodeInstallDetailsWithResponse("rgConnectedCache", "MccRPTest1", "MCCCachenode1",
                com.azure.core.util.Context.NONE);
    }
}
```

### IspCacheNodesOperations_ListByIspCustomerResource

```java
/**
 * Samples for IspCacheNodesOperations ListByIspCustomerResource.
 */
public final class IspCacheNodesOperationsListByIspCustomerResourceSamples {
    /*
     * x-ms-original-file: 2023-05-01-preview/IspCacheNodesOperations_ListByIspCustomerResource_MaximumSet_Gen.json
     */
    /**
     * Sample code: IspCacheNodesOperations_ListByIspCustomerResource.
     * 
     * @param manager Entry point to ConnectedCacheManager.
     */
    public static void ispCacheNodesOperationsListByIspCustomerResource(
        com.azure.resourcemanager.connectedcache.ConnectedCacheManager manager) {
        manager.ispCacheNodesOperations()
            .listByIspCustomerResource("rgConnectedCache", "MccRPTest1", com.azure.core.util.Context.NONE);
    }
}
```

### IspCacheNodesOperations_Update

```java
import com.azure.resourcemanager.connectedcache.models.IspCacheNodeResource;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for IspCacheNodesOperations Update.
 */
public final class IspCacheNodesOperationsUpdateSamples {
    /*
     * x-ms-original-file: 2023-05-01-preview/IspCacheNodesOperations_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: IspCacheNodesOperations_Update.
     * 
     * @param manager Entry point to ConnectedCacheManager.
     */
    public static void
        ispCacheNodesOperationsUpdate(com.azure.resourcemanager.connectedcache.ConnectedCacheManager manager) {
        IspCacheNodeResource resource = manager.ispCacheNodesOperations()
            .getWithResponse("rgConnectedCache", "MccRPTest1", "MCCCachenode1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("key1653", "fakeTokenPlaceholder")).apply();
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

### IspCustomers_CreateOrUpdate

```java
import com.azure.resourcemanager.connectedcache.models.AdditionalCustomerProperties;
import com.azure.resourcemanager.connectedcache.models.CustomerEntity;
import com.azure.resourcemanager.connectedcache.models.CustomerProperty;
import com.azure.resourcemanager.connectedcache.models.CustomerTransitState;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for IspCustomers CreateOrUpdate.
 */
public final class IspCustomersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2023-05-01-preview/IspCustomers_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: ispCustomer CreateOrUpdate - generated by [MaximumSet] rule.
     * 
     * @param manager Entry point to ConnectedCacheManager.
     */
    public static void ispCustomerCreateOrUpdateGeneratedByMaximumSetRule(
        com.azure.resourcemanager.connectedcache.ConnectedCacheManager manager) {
        manager.ispCustomers()
            .define("MccRPTest2")
            .withRegion("westus")
            .withExistingResourceGroup("rgConnectedCache")
            .withTags(mapOf("key1878", "fakeTokenPlaceholder"))
            .withProperties(new CustomerProperty()
                .withCustomer(new CustomerEntity().withFullyQualifiedResourceId("uqsbtgae")
                    .withCustomerName("mkpzynfqihnjfdbaqbqwyhd")
                    .withContactEmail("xquos")
                    .withContactPhone("vue")
                    .withContactName("wxyqjoyoscmvimgwhpitxky")
                    .withIsEntitled(true)
                    .withReleaseVersion(20)
                    .withClientTenantId("fproidkpgvpdnac")
                    .withIsEnterpriseManaged(true)
                    .withShouldMigrate(true)
                    .withResendSignupCode(true)
                    .withVerifySignupCode(true)
                    .withVerifySignupPhrase("tprjvttkgmrqlsyicnidhm"))
                .withAdditionalCustomerProperties(new AdditionalCustomerProperties().withCustomerEmail("zdjgibsidydyzm")
                    .withCustomerTransitAsn("habgklnxqzmozqpazoyejwiphezpi")
                    .withCustomerTransitState(CustomerTransitState.fromString("voblixkxfejbmhxilb"))
                    .withCustomerAsn("hgrelgnrtdkleisnepfolu")
                    .withCustomerEntitlementSkuId("b")
                    .withCustomerEntitlementSkuGuid("rvzmdpxyflgqetvpwupnfaxsweiiz")
                    .withCustomerEntitlementSkuName("waaqfijr")
                    .withCustomerEntitlementExpiration(OffsetDateTime.parse("2024-01-30T00:54:04.773Z"))
                    .withOptionalProperty1("qhmwxza")
                    .withOptionalProperty2("l")
                    .withOptionalProperty3("mblwwvbie")
                    .withOptionalProperty4("vzuek")
                    .withOptionalProperty5("fzjodscdfcdr")))
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

### IspCustomers_Delete

```java
/**
 * Samples for IspCustomers Delete.
 */
public final class IspCustomersDeleteSamples {
    /*
     * x-ms-original-file: 2023-05-01-preview/IspCustomers_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: IspCustomers Delete - generated by [MaximumSet] rule.
     * 
     * @param manager Entry point to ConnectedCacheManager.
     */
    public static void ispCustomersDeleteGeneratedByMaximumSetRule(
        com.azure.resourcemanager.connectedcache.ConnectedCacheManager manager) {
        manager.ispCustomers()
            .delete("rgConnectedCache", "hdontfoythjsaeyjhrakckgimgchxwzttbcnvntpvdsgeumxpgnjurptd",
                com.azure.core.util.Context.NONE);
    }
}
```

### IspCustomers_GetByResourceGroup

```java
/**
 * Samples for IspCustomers GetByResourceGroup.
 */
public final class IspCustomersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2023-05-01-preview/IspCustomers_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: IspCustomers Get - generated by [MaximumSet] rule.
     * 
     * @param manager Entry point to ConnectedCacheManager.
     */
    public static void ispCustomersGetGeneratedByMaximumSetRule(
        com.azure.resourcemanager.connectedcache.ConnectedCacheManager manager) {
        manager.ispCustomers()
            .getByResourceGroupWithResponse("rgConnectedCache", "cmcjfueweicolcjkwmsuvcj",
                com.azure.core.util.Context.NONE);
    }
}
```

### IspCustomers_List

```java
/**
 * Samples for IspCustomers List.
 */
public final class IspCustomersListSamples {
    /*
     * x-ms-original-file: 2023-05-01-preview/IspCustomers_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: ispCustomer List by Subscription - generated by [MaximumSet] rule.
     * 
     * @param manager Entry point to ConnectedCacheManager.
     */
    public static void ispCustomerListBySubscriptionGeneratedByMaximumSetRule(
        com.azure.resourcemanager.connectedcache.ConnectedCacheManager manager) {
        manager.ispCustomers().list(com.azure.core.util.Context.NONE);
    }
}
```

### IspCustomers_ListByResourceGroup

```java
/**
 * Samples for IspCustomers ListByResourceGroup.
 */
public final class IspCustomersListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2023-05-01-preview/IspCustomers_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: IspCustomers resource List by Resource group - generated by [MaximumSet] rule.
     * 
     * @param manager Entry point to ConnectedCacheManager.
     */
    public static void ispCustomersResourceListByResourceGroupGeneratedByMaximumSetRule(
        com.azure.resourcemanager.connectedcache.ConnectedCacheManager manager) {
        manager.ispCustomers().listByResourceGroup("rgConnectedCache", com.azure.core.util.Context.NONE);
    }
}
```

### IspCustomers_Update

```java
import com.azure.resourcemanager.connectedcache.models.IspCustomerResource;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for IspCustomers Update.
 */
public final class IspCustomersUpdateSamples {
    /*
     * x-ms-original-file: 2023-05-01-preview/IspCustomers_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: ispCustomer Update details - generated by [MaximumSet] rule.
     * 
     * @param manager Entry point to ConnectedCacheManager.
     */
    public static void ispCustomerUpdateDetailsGeneratedByMaximumSetRule(
        com.azure.resourcemanager.connectedcache.ConnectedCacheManager manager) {
        IspCustomerResource resource = manager.ispCustomers()
            .getByResourceGroupWithResponse("rgConnectedCache", "MccRPTest2", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("key1653", "fakeTokenPlaceholder")).apply();
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

### Operations_List

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2023-05-01-preview/Operations_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: List the operations for the provider - generated by [MaximumSet] rule.
     * 
     * @param manager Entry point to ConnectedCacheManager.
     */
    public static void listTheOperationsForTheProviderGeneratedByMaximumSetRule(
        com.azure.resourcemanager.connectedcache.ConnectedCacheManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

