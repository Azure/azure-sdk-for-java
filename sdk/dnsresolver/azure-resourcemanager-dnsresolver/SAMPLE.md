# Code snippets and samples


## DnsForwardingRulesets

- [CreateOrUpdate](#dnsforwardingrulesets_createorupdate)
- [Delete](#dnsforwardingrulesets_delete)
- [GetByResourceGroup](#dnsforwardingrulesets_getbyresourcegroup)
- [List](#dnsforwardingrulesets_list)
- [ListByResourceGroup](#dnsforwardingrulesets_listbyresourcegroup)
- [ListByVirtualNetwork](#dnsforwardingrulesets_listbyvirtualnetwork)
- [Update](#dnsforwardingrulesets_update)

## DnsResolverDomainLists

- [Bulk](#dnsresolverdomainlists_bulk)
- [CreateOrUpdate](#dnsresolverdomainlists_createorupdate)
- [Delete](#dnsresolverdomainlists_delete)
- [GetByResourceGroup](#dnsresolverdomainlists_getbyresourcegroup)
- [List](#dnsresolverdomainlists_list)
- [ListByResourceGroup](#dnsresolverdomainlists_listbyresourcegroup)
- [Update](#dnsresolverdomainlists_update)

## DnsResolverPolicies

- [CreateOrUpdate](#dnsresolverpolicies_createorupdate)
- [Delete](#dnsresolverpolicies_delete)
- [GetByResourceGroup](#dnsresolverpolicies_getbyresourcegroup)
- [List](#dnsresolverpolicies_list)
- [ListByResourceGroup](#dnsresolverpolicies_listbyresourcegroup)
- [ListByVirtualNetwork](#dnsresolverpolicies_listbyvirtualnetwork)
- [Update](#dnsresolverpolicies_update)

## DnsResolverPolicyVirtualNetworkLinks

- [CreateOrUpdate](#dnsresolverpolicyvirtualnetworklinks_createorupdate)
- [Delete](#dnsresolverpolicyvirtualnetworklinks_delete)
- [Get](#dnsresolverpolicyvirtualnetworklinks_get)
- [List](#dnsresolverpolicyvirtualnetworklinks_list)
- [Update](#dnsresolverpolicyvirtualnetworklinks_update)

## DnsResolvers

- [CreateOrUpdate](#dnsresolvers_createorupdate)
- [Delete](#dnsresolvers_delete)
- [GetByResourceGroup](#dnsresolvers_getbyresourcegroup)
- [List](#dnsresolvers_list)
- [ListByResourceGroup](#dnsresolvers_listbyresourcegroup)
- [ListByVirtualNetwork](#dnsresolvers_listbyvirtualnetwork)
- [Update](#dnsresolvers_update)

## DnsSecurityRules

- [CreateOrUpdate](#dnssecurityrules_createorupdate)
- [Delete](#dnssecurityrules_delete)
- [Get](#dnssecurityrules_get)
- [List](#dnssecurityrules_list)
- [Update](#dnssecurityrules_update)

## ForwardingRules

- [CreateOrUpdate](#forwardingrules_createorupdate)
- [Delete](#forwardingrules_delete)
- [Get](#forwardingrules_get)
- [List](#forwardingrules_list)
- [Update](#forwardingrules_update)

## InboundEndpoints

- [CreateOrUpdate](#inboundendpoints_createorupdate)
- [Delete](#inboundendpoints_delete)
- [Get](#inboundendpoints_get)
- [List](#inboundendpoints_list)
- [Update](#inboundendpoints_update)

## OutboundEndpoints

- [CreateOrUpdate](#outboundendpoints_createorupdate)
- [Delete](#outboundendpoints_delete)
- [Get](#outboundendpoints_get)
- [List](#outboundendpoints_list)
- [Update](#outboundendpoints_update)

## VirtualNetworkLinks

- [CreateOrUpdate](#virtualnetworklinks_createorupdate)
- [Delete](#virtualnetworklinks_delete)
- [Get](#virtualnetworklinks_get)
- [List](#virtualnetworklinks_list)
- [Update](#virtualnetworklinks_update)
### DnsForwardingRulesets_CreateOrUpdate

```java
/**
 * Samples for InboundEndpoints Get.
 */
public final class InboundEndpointsGetSamples {
    /*
     * x-ms-original-file:
     * specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/InboundEndpoint_Get.json
     */
    /**
     * Sample code: Retrieve inbound endpoint for DNS resolver.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void
        retrieveInboundEndpointForDNSResolver(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.inboundEndpoints()
            .getWithResponse("sampleResourceGroup", "sampleDnsResolver", "sampleInboundEndpoint",
                com.azure.core.util.Context.NONE);
    }
}
```

### DnsForwardingRulesets_Delete

```java
/**
 * Samples for DnsForwardingRulesets Delete.
 */
public final class DnsForwardingRulesetsDeleteSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/
     * DnsForwardingRuleset_Delete.json
     */
    /**
     * Sample code: Delete DNS forwarding ruleset.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void deleteDNSForwardingRuleset(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.dnsForwardingRulesets()
            .delete("sampleResourceGroup", "samplednsForwardingRulesetName", null, com.azure.core.util.Context.NONE);
    }
}
```

### DnsForwardingRulesets_GetByResourceGroup

```java
/**
 * Samples for VirtualNetworkLinks Delete.
 */
public final class VirtualNetworkLinksDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/VirtualNetworkLink_Delete
     * .json
     */
    /**
     * Sample code: Delete virtual network link to a DNS forwarding ruleset.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void deleteVirtualNetworkLinkToADNSForwardingRuleset(
        com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.virtualNetworkLinks()
            .delete("sampleResourceGroup", "sampleDnsForwardingRuleset", "sampleVirtualNetworkLink", null,
                com.azure.core.util.Context.NONE);
    }
}
```

### DnsForwardingRulesets_List

```java
/**
 * Samples for DnsResolverPolicyVirtualNetworkLinks Delete.
 */
public final class DnsResolverPolicyVirtualNetworkLinksDeleteSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/
     * DnsResolverPolicyVirtualNetworkLink_Delete.json
     */
    /**
     * Sample code: Delete DNS resolver policy virtual network link.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void
        deleteDNSResolverPolicyVirtualNetworkLink(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.dnsResolverPolicyVirtualNetworkLinks()
            .delete("sampleResourceGroup", "sampleDnsResolverPolicy", "sampleVirtualNetworkLink", null,
                com.azure.core.util.Context.NONE);
    }
}
```

### DnsForwardingRulesets_ListByResourceGroup

```java
/**
 * Samples for InboundEndpoints List.
 */
public final class InboundEndpointsListSamples {
    /*
     * x-ms-original-file:
     * specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/InboundEndpoint_List.json
     */
    /**
     * Sample code: List inbound endpoints by DNS resolver.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void
        listInboundEndpointsByDNSResolver(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.inboundEndpoints()
            .list("sampleResourceGroup", "sampleDnsResolver", null, com.azure.core.util.Context.NONE);
    }
}
```

### DnsForwardingRulesets_ListByVirtualNetwork

```java
/**
 * Samples for VirtualNetworkLinks Get.
 */
public final class VirtualNetworkLinksGetSamples {
    /*
     * x-ms-original-file:
     * specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/VirtualNetworkLink_Get.
     * json
     */
    /**
     * Sample code: Retrieve virtual network link to a DNS forwarding ruleset.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void retrieveVirtualNetworkLinkToADNSForwardingRuleset(
        com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.virtualNetworkLinks()
            .getWithResponse("sampleResourceGroup", "sampleDnsForwardingRuleset", "sampleVirtualNetworkLink",
                com.azure.core.util.Context.NONE);
    }
}
```

### DnsForwardingRulesets_Update

```java
/**
 * Samples for DnsForwardingRulesets List.
 */
public final class DnsForwardingRulesetsListSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/
     * DnsForwardingRuleset_ListBySubscription.json
     */
    /**
     * Sample code: List DNS forwarding rulesets by subscription.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void
        listDNSForwardingRulesetsBySubscription(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.dnsForwardingRulesets().list(null, com.azure.core.util.Context.NONE);
    }
}
```

### DnsResolverDomainLists_Bulk

```java
import com.azure.resourcemanager.dnsresolver.models.OutboundEndpoint;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for OutboundEndpoints Update.
 */
public final class OutboundEndpointsUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/OutboundEndpoint_Patch.
     * json
     */
    /**
     * Sample code: Update outbound endpoint for DNS resolver.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void
        updateOutboundEndpointForDNSResolver(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        OutboundEndpoint resource = manager.outboundEndpoints()
            .getWithResponse("sampleResourceGroup", "sampleDnsResolver", "sampleOutboundEndpoint",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("key1", "fakeTokenPlaceholder")).apply();
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

### DnsResolverDomainLists_CreateOrUpdate

```java
/**
 * Samples for InboundEndpoints Delete.
 */
public final class InboundEndpointsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/InboundEndpoint_Delete.
     * json
     */
    /**
     * Sample code: Delete inbound endpoint for DNS resolver.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void
        deleteInboundEndpointForDNSResolver(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.inboundEndpoints()
            .delete("sampleResourceGroup", "sampleDnsResolver", "sampleInboundEndpoint", null,
                com.azure.core.util.Context.NONE);
    }
}
```

### DnsResolverDomainLists_Delete

```java
/**
 * Samples for DnsResolvers List.
 */
public final class DnsResolversListSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/
     * DnsResolver_ListBySubscription.json
     */
    /**
     * Sample code: List DNS resolvers by subscription.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void
        listDNSResolversBySubscription(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.dnsResolvers().list(null, com.azure.core.util.Context.NONE);
    }
}
```

### DnsResolverDomainLists_GetByResourceGroup

```java
import com.azure.core.management.SubResource;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for DnsResolvers CreateOrUpdate.
 */
public final class DnsResolversCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/DnsResolver_Put.json
     */
    /**
     * Sample code: Upsert DNS resolver.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void upsertDNSResolver(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.dnsResolvers()
            .define("sampleDnsResolver")
            .withRegion("westus2")
            .withExistingResourceGroup("sampleResourceGroup")
            .withVirtualNetwork(new SubResource().withId(
                "/subscriptions/cbb1387e-4b03-44f2-ad41-58d4677b9873/resourceGroups/virtualNetworkResourceGroup/providers/Microsoft.Network/virtualNetworks/sampleVirtualNetwork"))
            .withTags(mapOf("key1", "fakeTokenPlaceholder"))
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

### DnsResolverDomainLists_List

```java
import com.azure.resourcemanager.dnsresolver.models.DnsResolverPolicy;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for DnsResolverPolicies Update.
 */
public final class DnsResolverPoliciesUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/DnsResolverPolicy_Patch.
     * json
     */
    /**
     * Sample code: Update DNS resolver policy.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void updateDNSResolverPolicy(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        DnsResolverPolicy resource = manager.dnsResolverPolicies()
            .getByResourceGroupWithResponse("sampleResourceGroup", "sampleDnsResolverPolicy",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("key1", "fakeTokenPlaceholder")).apply();
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

### DnsResolverDomainLists_ListByResourceGroup

```java
/**
 * Samples for ForwardingRules Get.
 */
public final class ForwardingRulesGetSamples {
    /*
     * x-ms-original-file:
     * specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/ForwardingRule_Get.json
     */
    /**
     * Sample code: Retrieve forwarding rule in a DNS forwarding ruleset.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void retrieveForwardingRuleInADNSForwardingRuleset(
        com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.forwardingRules()
            .getWithResponse("sampleResourceGroup", "sampleDnsForwardingRuleset", "sampleForwardingRule",
                com.azure.core.util.Context.NONE);
    }
}
```

### DnsResolverDomainLists_Update

```java
import com.azure.resourcemanager.dnsresolver.models.Action;
import com.azure.resourcemanager.dnsresolver.models.DnsResolverDomainListBulk;

/**
 * Samples for DnsResolverDomainLists Bulk.
 */
public final class DnsResolverDomainListsBulkSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/
     * DnsResolverDomainList_BulkUpload.json
     */
    /**
     * Sample code: Upload DNS resolver domain list domains.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void
        uploadDNSResolverDomainListDomains(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.dnsResolverDomainLists()
            .bulk("sampleResourceGroup", "sampleDnsResolverDomainList", new DnsResolverDomainListBulk().withStorageUrl(
                "https://sampleStorageAccount.blob.core.windows.net/sample-container/sampleBlob.txt?sv=2022-11-02&sr=b&sig=39Up9jzHkxhUIhFEjEh9594DJxe7w6cIRCgOV6ICGS0%3A377&sp=rcw")
                .withAction(Action.UPLOAD), null, null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/
     * DnsResolverDomainList_BulkDownload.json
     */
    /**
     * Sample code: Download DNS resolver domain list domains.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void
        downloadDNSResolverDomainListDomains(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.dnsResolverDomainLists()
            .bulk("sampleResourceGroup", "sampleDnsResolverDomainList", new DnsResolverDomainListBulk().withStorageUrl(
                "https://sampleStorageAccount.blob.core.windows.net/sample-container/sampleBlob.txt?sv=2022-11-02&sr=b&sig=39Up9jzHkxhUIhFEjEh9594DJxe7w6cIRCgOV6ICGS0%3A377&sp=rcw")
                .withAction(Action.DOWNLOAD), null, null, com.azure.core.util.Context.NONE);
    }
}
```

### DnsResolverPolicies_CreateOrUpdate

```java
import com.azure.core.management.SubResource;
import com.azure.resourcemanager.dnsresolver.models.ActionType;
import com.azure.resourcemanager.dnsresolver.models.DnsSecurityRuleAction;
import com.azure.resourcemanager.dnsresolver.models.DnsSecurityRuleState;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for DnsSecurityRules CreateOrUpdate.
 */
public final class DnsSecurityRulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/DnsSecurityRule_Put.json
     */
    /**
     * Sample code: Upsert DNS security rule.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void upsertDNSSecurityRule(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.dnsSecurityRules()
            .define("sampleDnsSecurityRule")
            .withRegion("westus2")
            .withExistingDnsResolverPolicy("sampleResourceGroup", "sampleDnsResolverPolicy")
            .withPriority(100)
            .withAction(new DnsSecurityRuleAction().withActionType(ActionType.BLOCK))
            .withDnsResolverDomainLists(Arrays.asList(new SubResource().withId(
                "/subscriptions/abdd4249-9f34-4cc6-8e42-c2e32110603e/resourceGroups/sampleResourceGroup/providers/Microsoft.Network/dnsResolverDomainLists/sampleDnsResolverDomainList")))
            .withTags(mapOf("key1", "fakeTokenPlaceholder"))
            .withDnsSecurityRuleState(DnsSecurityRuleState.ENABLED)
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

### DnsResolverPolicies_Delete

```java
import com.azure.resourcemanager.dnsresolver.models.DnsResolverPolicyVirtualNetworkLink;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for DnsResolverPolicyVirtualNetworkLinks Update.
 */
public final class DnsResolverPolicyVirtualNetworkLinksUpdateSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/
     * DnsResolverPolicyVirtualNetworkLink_Patch.json
     */
    /**
     * Sample code: Update DNS resolver policy virtual network link.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void
        updateDNSResolverPolicyVirtualNetworkLink(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        DnsResolverPolicyVirtualNetworkLink resource = manager.dnsResolverPolicyVirtualNetworkLinks()
            .getWithResponse("sampleResourceGroup", "sampleDnsResolverPolicy", "sampleVirtualNetworkLink",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("key1", "fakeTokenPlaceholder")).apply();
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

### DnsResolverPolicies_GetByResourceGroup

```java
import com.azure.resourcemanager.dnsresolver.models.ForwardingRule;
import com.azure.resourcemanager.dnsresolver.models.ForwardingRuleState;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ForwardingRules Update.
 */
public final class ForwardingRulesUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/ForwardingRule_Patch.json
     */
    /**
     * Sample code: Update forwarding rule in a DNS forwarding ruleset.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void
        updateForwardingRuleInADNSForwardingRuleset(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        ForwardingRule resource = manager.forwardingRules()
            .getWithResponse("sampleResourceGroup", "sampleDnsForwardingRuleset", "sampleForwardingRule",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withMetadata(mapOf("additionalProp2", "value2"))
            .withForwardingRuleState(ForwardingRuleState.DISABLED)
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

### DnsResolverPolicies_List

```java
import com.azure.resourcemanager.dnsresolver.models.VirtualNetworkLink;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for VirtualNetworkLinks Update.
 */
public final class VirtualNetworkLinksUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/VirtualNetworkLink_Patch.
     * json
     */
    /**
     * Sample code: Update virtual network link to a DNS forwarding ruleset.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void updateVirtualNetworkLinkToADNSForwardingRuleset(
        com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        VirtualNetworkLink resource = manager.virtualNetworkLinks()
            .getWithResponse("sampleResourceGroup", "sampleDnsForwardingRuleset", "sampleVirtualNetworkLink",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withMetadata(mapOf("additionalProp1", "value1")).apply();
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

### DnsResolverPolicies_ListByResourceGroup

```java
/**
 * Samples for DnsResolvers Delete.
 */
public final class DnsResolversDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/DnsResolver_Delete.json
     */
    /**
     * Sample code: Delete DNS resolver.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void deleteDNSResolver(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.dnsResolvers()
            .delete("sampleResourceGroup", "sampleDnsResolver", null, com.azure.core.util.Context.NONE);
    }
}
```

### DnsResolverPolicies_ListByVirtualNetwork

```java
/**
 * Samples for DnsForwardingRulesets GetByResourceGroup.
 */
public final class DnsForwardingRulesetsGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/DnsForwardingRuleset_Get.
     * json
     */
    /**
     * Sample code: Retrieve DNS forwarding ruleset.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void retrieveDNSForwardingRuleset(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.dnsForwardingRulesets()
            .getByResourceGroupWithResponse("sampleResourceGroup", "sampleDnsForwardingRuleset",
                com.azure.core.util.Context.NONE);
    }
}
```

### DnsResolverPolicies_Update

```java
/**
 * Samples for VirtualNetworkLinks List.
 */
public final class VirtualNetworkLinksListSamples {
    /*
     * x-ms-original-file:
     * specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/VirtualNetworkLink_List.
     * json
     */
    /**
     * Sample code: List virtual network links to a DNS forwarding ruleset.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void listVirtualNetworkLinksToADNSForwardingRuleset(
        com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.virtualNetworkLinks()
            .list("sampleResourceGroup", "sampleDnsForwardingRuleset", null, com.azure.core.util.Context.NONE);
    }
}
```

### DnsResolverPolicyVirtualNetworkLinks_CreateOrUpdate

```java
/**
 * Samples for DnsResolvers GetByResourceGroup.
 */
public final class DnsResolversGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/DnsResolver_Get.json
     */
    /**
     * Sample code: Retrieve DNS resolver.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void retrieveDNSResolver(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.dnsResolvers()
            .getByResourceGroupWithResponse("sampleResourceGroup", "sampleDnsResolver",
                com.azure.core.util.Context.NONE);
    }
}
```

### DnsResolverPolicyVirtualNetworkLinks_Delete

```java
/**
 * Samples for DnsResolverDomainLists GetByResourceGroup.
 */
public final class DnsResolverDomainListsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/
     * DnsResolverDomainList_BulkDomains_Get.json
     */
    /**
     * Sample code: Retrieve DNS resolver domain list with bulk number of domains.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void retrieveDNSResolverDomainListWithBulkNumberOfDomains(
        com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.dnsResolverDomainLists()
            .getByResourceGroupWithResponse("sampleResourceGroup", "sampleDnsResolverDomainList",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/DnsResolverDomainList_Get
     * .json
     */
    /**
     * Sample code: Retrieve DNS resolver domain list with less than 1000 domains.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void retrieveDNSResolverDomainListWithLessThan1000Domains(
        com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.dnsResolverDomainLists()
            .getByResourceGroupWithResponse("sampleResourceGroup", "sampleDnsResolverDomainList",
                com.azure.core.util.Context.NONE);
    }
}
```

### DnsResolverPolicyVirtualNetworkLinks_Get

```java
import com.azure.core.management.SubResource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for DnsForwardingRulesets CreateOrUpdate.
 */
public final class DnsForwardingRulesetsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/DnsForwardingRuleset_Put.
     * json
     */
    /**
     * Sample code: Upsert DNS forwarding ruleset.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void upsertDNSForwardingRuleset(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.dnsForwardingRulesets()
            .define("samplednsForwardingRuleset")
            .withRegion("westus2")
            .withExistingResourceGroup("sampleResourceGroup")
            .withDnsResolverOutboundEndpoints(Arrays.asList(new SubResource().withId(
                "/subscriptions/abdd4249-9f34-4cc6-8e42-c2e32110603e/resourceGroups/sampleResourceGroup/providers/Microsoft.Network/dnsResolvers/sampleDnsResolver/outboundEndpoints/sampleOutboundEndpoint0"),
                new SubResource().withId(
                    "/subscriptions/abdd4249-9f34-4cc6-8e42-c2e32110603e/resourceGroups/sampleResourceGroup/providers/Microsoft.Network/dnsResolvers/sampleDnsResolver/outboundEndpoints/sampleOutboundEndpoint1")))
            .withTags(mapOf("key1", "fakeTokenPlaceholder"))
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

### DnsResolverPolicyVirtualNetworkLinks_List

```java
import com.azure.core.management.SubResource;
import com.azure.resourcemanager.dnsresolver.models.IpAllocationMethod;
import com.azure.resourcemanager.dnsresolver.models.IpConfiguration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for InboundEndpoints CreateOrUpdate.
 */
public final class InboundEndpointsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/InboundEndpoint_Put.json
     */
    /**
     * Sample code: Upsert inbound endpoint for DNS resolver.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void
        upsertInboundEndpointForDNSResolver(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.inboundEndpoints()
            .define("sampleInboundEndpoint")
            .withRegion("westus2")
            .withExistingDnsResolver("sampleResourceGroup", "sampleDnsResolver")
            .withIpConfigurations(Arrays.asList(new IpConfiguration().withSubnet(new SubResource().withId(
                "/subscriptions/0403cfa9-9659-4f33-9f30-1f191c51d111/resourceGroups/sampleVnetResourceGroupName/providers/Microsoft.Network/virtualNetworks/sampleVirtualNetwork/subnets/sampleSubnet"))
                .withPrivateIpAllocationMethod(IpAllocationMethod.DYNAMIC)))
            .withTags(mapOf("key1", "fakeTokenPlaceholder"))
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

### DnsResolverPolicyVirtualNetworkLinks_Update

```java
/**
 * Samples for DnsForwardingRulesets ListByVirtualNetwork.
 */
public final class DnsForwardingRulesetsListByVirtualNetworkSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/
     * DnsForwardingRuleset_ListByVirtualNetwork.json
     */
    /**
     * Sample code: List DNS forwarding rulesets by virtual network.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void
        listDNSForwardingRulesetsByVirtualNetwork(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.dnsForwardingRulesets()
            .listByVirtualNetwork("sampleResourceGroup", "sampleVirtualNetwork", null,
                com.azure.core.util.Context.NONE);
    }
}
```

### DnsResolvers_CreateOrUpdate

```java
/**
 * Samples for DnsResolverPolicyVirtualNetworkLinks Get.
 */
public final class DnsResolverPolicyVirtualNetworkLinksGetSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/
     * DnsResolverPolicyVirtualNetworkLink_Get.json
     */
    /**
     * Sample code: Retrieve DNS resolver policy virtual network link.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void
        retrieveDNSResolverPolicyVirtualNetworkLink(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.dnsResolverPolicyVirtualNetworkLinks()
            .getWithResponse("sampleResourceGroup", "sampleDnsResolverPolicy", "sampleVirtualNetworkLink",
                com.azure.core.util.Context.NONE);
    }
}
```

### DnsResolvers_Delete

```java
import com.azure.resourcemanager.dnsresolver.models.ForwardingRuleState;
import com.azure.resourcemanager.dnsresolver.models.TargetDnsServer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ForwardingRules CreateOrUpdate.
 */
public final class ForwardingRulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/ForwardingRule_Put.json
     */
    /**
     * Sample code: Upsert forwarding rule in a DNS forwarding ruleset.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void
        upsertForwardingRuleInADNSForwardingRuleset(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.forwardingRules()
            .define("sampleForwardingRule")
            .withExistingDnsForwardingRuleset("sampleResourceGroup", "sampleDnsForwardingRuleset")
            .withDomainName("contoso.com.")
            .withTargetDnsServers(Arrays.asList(new TargetDnsServer().withIpAddress("10.0.0.1").withPort(53),
                new TargetDnsServer().withIpAddress("10.0.0.2").withPort(53)))
            .withMetadata(mapOf("additionalProp1", "value1"))
            .withForwardingRuleState(ForwardingRuleState.ENABLED)
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

### DnsResolvers_GetByResourceGroup

```java
import com.azure.core.management.SubResource;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for OutboundEndpoints CreateOrUpdate.
 */
public final class OutboundEndpointsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/OutboundEndpoint_Put.json
     */
    /**
     * Sample code: Upsert outbound endpoint for DNS resolver.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void
        upsertOutboundEndpointForDNSResolver(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.outboundEndpoints()
            .define("sampleOutboundEndpoint")
            .withRegion("westus2")
            .withExistingDnsResolver("sampleResourceGroup", "sampleDnsResolver")
            .withSubnet(new SubResource().withId(
                "/subscriptions/0403cfa9-9659-4f33-9f30-1f191c51d111/resourceGroups/sampleVnetResourceGroupName/providers/Microsoft.Network/virtualNetworks/sampleVirtualNetwork/subnets/sampleSubnet"))
            .withTags(mapOf("key1", "fakeTokenPlaceholder"))
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

### DnsResolvers_List

```java
/**
 * Samples for DnsForwardingRulesets ListByResourceGroup.
 */
public final class DnsForwardingRulesetsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/
     * DnsForwardingRuleset_ListByResourceGroup.json
     */
    /**
     * Sample code: List DNS forwarding rulesets by resource group.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void
        listDNSForwardingRulesetsByResourceGroup(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.dnsForwardingRulesets()
            .listByResourceGroup("sampleResourceGroup", null, com.azure.core.util.Context.NONE);
    }
}
```

### DnsResolvers_ListByResourceGroup

```java
import com.azure.core.management.SubResource;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for VirtualNetworkLinks CreateOrUpdate.
 */
public final class VirtualNetworkLinksCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/VirtualNetworkLink_Put.
     * json
     */
    /**
     * Sample code: Upsert virtual network link to a DNS forwarding ruleset.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void upsertVirtualNetworkLinkToADNSForwardingRuleset(
        com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.virtualNetworkLinks()
            .define("sampleVirtualNetworkLink")
            .withExistingDnsForwardingRuleset("sampleResourceGroup", "sampleDnsForwardingRuleset")
            .withVirtualNetwork(new SubResource().withId(
                "/subscriptions/0403cfa9-9659-4f33-9f30-1f191c51d111/resourceGroups/sampleVnetResourceGroupName/providers/Microsoft.Network/virtualNetworks/sampleVirtualNetwork"))
            .withMetadata(mapOf("additionalProp1", "value1"))
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

### DnsResolvers_ListByVirtualNetwork

```java
/**
 * Samples for DnsSecurityRules Get.
 */
public final class DnsSecurityRulesGetSamples {
    /*
     * x-ms-original-file:
     * specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/DnsSecurityRule_Get.json
     */
    /**
     * Sample code: Retrieve DNS security rule for DNS resolver policy.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void
        retrieveDNSSecurityRuleForDNSResolverPolicy(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.dnsSecurityRules()
            .getWithResponse("sampleResourceGroup", "sampleDnsResolverPolicy", "sampleDnsSecurityRule",
                com.azure.core.util.Context.NONE);
    }
}
```

### DnsResolvers_Update

```java
/**
 * Samples for DnsResolverPolicyVirtualNetworkLinks List.
 */
public final class DnsResolverPolicyVirtualNetworkLinksListSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/
     * DnsResolverPolicyVirtualNetworkLink_List.json
     */
    /**
     * Sample code: List DNS resolver policy virtual network links by DNS resolver policy.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void listDNSResolverPolicyVirtualNetworkLinksByDNSResolverPolicy(
        com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.dnsResolverPolicyVirtualNetworkLinks()
            .list("sampleResourceGroup", "sampleDnsResolverPolicy", null, com.azure.core.util.Context.NONE);
    }
}
```

### DnsSecurityRules_CreateOrUpdate

```java
/**
 * Samples for DnsResolverPolicies ListByResourceGroup.
 */
public final class DnsResolverPoliciesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/
     * DnsResolverPolicy_ListByResourceGroup.json
     */
    /**
     * Sample code: List DNS resolver policies by resource group.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void
        listDNSResolverPoliciesByResourceGroup(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.dnsResolverPolicies()
            .listByResourceGroup("sampleResourceGroup", null, com.azure.core.util.Context.NONE);
    }
}
```

### DnsSecurityRules_Delete

```java
/**
 * Samples for ForwardingRules Delete.
 */
public final class ForwardingRulesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/ForwardingRule_Delete.
     * json
     */
    /**
     * Sample code: Delete forwarding rule in a DNS forwarding ruleset.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void
        deleteForwardingRuleInADNSForwardingRuleset(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.forwardingRules()
            .deleteWithResponse("sampleResourceGroup", "sampleDnsForwardingRuleset", "sampleForwardingRule", null,
                com.azure.core.util.Context.NONE);
    }
}
```

### DnsSecurityRules_Get

```java
/**
 * Samples for ForwardingRules List.
 */
public final class ForwardingRulesListSamples {
    /*
     * x-ms-original-file:
     * specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/ForwardingRule_List.json
     */
    /**
     * Sample code: List forwarding rules in a DNS forwarding ruleset.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void
        listForwardingRulesInADNSForwardingRuleset(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.forwardingRules()
            .list("sampleResourceGroup", "sampleDnsForwardingRuleset", null, com.azure.core.util.Context.NONE);
    }
}
```

### DnsSecurityRules_List

```java
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for DnsResolverPolicies CreateOrUpdate.
 */
public final class DnsResolverPoliciesCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/DnsResolverPolicy_Put.
     * json
     */
    /**
     * Sample code: Upsert DNS resolver policy.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void upsertDNSResolverPolicy(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.dnsResolverPolicies()
            .define("sampleDnsResolverPolicy")
            .withRegion("westus2")
            .withExistingResourceGroup("sampleResourceGroup")
            .withTags(mapOf("key1", "fakeTokenPlaceholder"))
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

### DnsSecurityRules_Update

```java
/**
 * Samples for DnsSecurityRules Delete.
 */
public final class DnsSecurityRulesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/DnsSecurityRule_Delete.
     * json
     */
    /**
     * Sample code: Delete DNS security rule for DNS resolver policy.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void
        deleteDNSSecurityRuleForDNSResolverPolicy(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.dnsSecurityRules()
            .delete("sampleResourceGroup", "sampleDnsDnsResolverPolicy", "sampleDnsSecurityRule", null,
                com.azure.core.util.Context.NONE);
    }
}
```

### ForwardingRules_CreateOrUpdate

```java
/**
 * Samples for DnsResolvers ListByVirtualNetwork.
 */
public final class DnsResolversListByVirtualNetworkSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/
     * DnsResolver_ListByVirtualNetwork.json
     */
    /**
     * Sample code: List DNS resolvers by virtual network.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void
        listDNSResolversByVirtualNetwork(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.dnsResolvers()
            .listByVirtualNetwork("sampleResourceGroup", "sampleVirtualNetwork", null,
                com.azure.core.util.Context.NONE);
    }
}
```

### ForwardingRules_Delete

```java
/**
 * Samples for DnsResolverPolicies List.
 */
public final class DnsResolverPoliciesListSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/
     * DnsResolverPolicy_ListBySubscription.json
     */
    /**
     * Sample code: List DNS resolver policies by subscription.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void
        listDNSResolverPoliciesBySubscription(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.dnsResolverPolicies().list(null, com.azure.core.util.Context.NONE);
    }
}
```

### ForwardingRules_Get

```java
/**
 * Samples for DnsResolverDomainLists ListByResourceGroup.
 */
public final class DnsResolverDomainListsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/
     * DnsResolverDomainList_ListByResourceGroup.json
     */
    /**
     * Sample code: List DNS resolver domain lists by resource group.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void
        listDNSResolverDomainListsByResourceGroup(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.dnsResolverDomainLists()
            .listByResourceGroup("sampleResourceGroup", null, com.azure.core.util.Context.NONE);
    }
}
```

### ForwardingRules_List

```java
import com.azure.resourcemanager.dnsresolver.models.DnsResolver;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for DnsResolvers Update.
 */
public final class DnsResolversUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/DnsResolver_Patch.json
     */
    /**
     * Sample code: Update DNS resolver.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void updateDNSResolver(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        DnsResolver resource = manager.dnsResolvers()
            .getByResourceGroupWithResponse("sampleResourceGroup", "sampleDnsResolver",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("key1", "fakeTokenPlaceholder")).apply();
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

### ForwardingRules_Update

```java
import com.azure.resourcemanager.dnsresolver.models.DnsResolverDomainList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for DnsResolverDomainLists Update.
 */
public final class DnsResolverDomainListsUpdateSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/
     * DnsResolverDomainList_Patch.json
     */
    /**
     * Sample code: Update DNS resolver domain list.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void updateDNSResolverDomainList(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        DnsResolverDomainList resource = manager.dnsResolverDomainLists()
            .getByResourceGroupWithResponse("sampleResourceGroup", "sampleDnsResolverDomainList",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key1", "fakeTokenPlaceholder"))
            .withDomains(Arrays.asList("contoso.com"))
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

### InboundEndpoints_CreateOrUpdate

```java
/**
 * Samples for DnsResolverPolicies GetByResourceGroup.
 */
public final class DnsResolverPoliciesGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/DnsResolverPolicy_Get.
     * json
     */
    /**
     * Sample code: Retrieve DNS resolver policy.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void retrieveDNSResolverPolicy(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.dnsResolverPolicies()
            .getByResourceGroupWithResponse("sampleResourceGroup", "sampleDnsResolverPolicy",
                com.azure.core.util.Context.NONE);
    }
}
```

### InboundEndpoints_Delete

```java
import com.azure.resourcemanager.dnsresolver.models.DnsForwardingRuleset;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for DnsForwardingRulesets Update.
 */
public final class DnsForwardingRulesetsUpdateSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/
     * DnsForwardingRuleset_Patch.json
     */
    /**
     * Sample code: Update DNS forwarding ruleset.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void updateDNSForwardingRuleset(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        DnsForwardingRuleset resource = manager.dnsForwardingRulesets()
            .getByResourceGroupWithResponse("sampleResourceGroup", "sampleDnsForwardingRuleset",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("key1", "fakeTokenPlaceholder")).apply();
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

### InboundEndpoints_Get

```java
/**
 * Samples for DnsResolverPolicies ListByVirtualNetwork.
 */
public final class DnsResolverPoliciesListByVirtualNetworkSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/
     * DnsResolverPolicy_ListByVirtualNetwork.json
     */
    /**
     * Sample code: List DNS resolver policies by virtual network.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void
        listDNSResolverPoliciesByVirtualNetwork(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.dnsResolverPolicies()
            .listByVirtualNetwork("sampleResourceGroup", "sampleVirtualNetwork", com.azure.core.util.Context.NONE);
    }
}
```

### InboundEndpoints_List

```java
import com.azure.resourcemanager.dnsresolver.models.DnsSecurityRule;
import com.azure.resourcemanager.dnsresolver.models.DnsSecurityRuleState;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for DnsSecurityRules Update.
 */
public final class DnsSecurityRulesUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/DnsSecurityRule_Patch.
     * json
     */
    /**
     * Sample code: Update DNS security rule for DNS resolver policy.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void
        updateDNSSecurityRuleForDNSResolverPolicy(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        DnsSecurityRule resource = manager.dnsSecurityRules()
            .getWithResponse("sampleResourceGroup", "sampleDnsResolverPolicy", "sampleDnsSecurityRule",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key1", "fakeTokenPlaceholder"))
            .withDnsSecurityRuleState(DnsSecurityRuleState.DISABLED)
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

### InboundEndpoints_Update

```java
import com.azure.core.management.SubResource;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for DnsResolverPolicyVirtualNetworkLinks CreateOrUpdate.
 */
public final class DnsResolverPolicyVirtualNetworkLinksCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/
     * DnsResolverPolicyVirtualNetworkLink_Put.json
     */
    /**
     * Sample code: Upsert DNS resolver policy virtual network link.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void
        upsertDNSResolverPolicyVirtualNetworkLink(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.dnsResolverPolicyVirtualNetworkLinks()
            .define("sampleVirtualNetworkLink")
            .withRegion("westus2")
            .withExistingDnsResolverPolicy("sampleResourceGroup", "sampleDnsResolverPolicy")
            .withVirtualNetwork(new SubResource().withId(
                "/subscriptions/0403cfa9-9659-4f33-9f30-1f191c51d111/resourceGroups/sampleVnetResourceGroupName/providers/Microsoft.Network/virtualNetworks/sampleVirtualNetwork"))
            .withTags(mapOf("key1", "fakeTokenPlaceholder"))
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

### OutboundEndpoints_CreateOrUpdate

```java
/**
 * Samples for DnsResolvers ListByResourceGroup.
 */
public final class DnsResolversListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/
     * DnsResolver_ListByResourceGroup.json
     */
    /**
     * Sample code: List DNS resolvers by resource group.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void
        listDNSResolversByResourceGroup(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.dnsResolvers().listByResourceGroup("sampleResourceGroup", null, com.azure.core.util.Context.NONE);
    }
}
```

### OutboundEndpoints_Delete

```java
/**
 * Samples for OutboundEndpoints Delete.
 */
public final class OutboundEndpointsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/OutboundEndpoint_Delete.
     * json
     */
    /**
     * Sample code: Delete outbound endpoint for DNS resolver.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void
        deleteOutboundEndpointForDNSResolver(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.outboundEndpoints()
            .delete("sampleResourceGroup", "sampleDnsResolver", "sampleOutboundEndpoint", null,
                com.azure.core.util.Context.NONE);
    }
}
```

### OutboundEndpoints_Get

```java
/**
 * Samples for DnsResolverPolicies Delete.
 */
public final class DnsResolverPoliciesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/DnsResolverPolicy_Delete.
     * json
     */
    /**
     * Sample code: Delete DNS resolver policy.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void deleteDNSResolverPolicy(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.dnsResolverPolicies()
            .delete("sampleResourceGroup", "sampleDnsResolverPolicy", null, com.azure.core.util.Context.NONE);
    }
}
```

### OutboundEndpoints_List

```java
/**
 * Samples for DnsSecurityRules List.
 */
public final class DnsSecurityRulesListSamples {
    /*
     * x-ms-original-file:
     * specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/DnsSecurityRule_List.json
     */
    /**
     * Sample code: List DNS security rules by DNS resolver policy.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void
        listDNSSecurityRulesByDNSResolverPolicy(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.dnsSecurityRules()
            .list("sampleResourceGroup", "sampleDnsResolverPolicy", null, com.azure.core.util.Context.NONE);
    }
}
```

### OutboundEndpoints_Update

```java
import com.azure.resourcemanager.dnsresolver.models.InboundEndpoint;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for InboundEndpoints Update.
 */
public final class InboundEndpointsUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/InboundEndpoint_Patch.
     * json
     */
    /**
     * Sample code: Update inbound endpoint for DNS resolver.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void
        updateInboundEndpointForDNSResolver(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        InboundEndpoint resource = manager.inboundEndpoints()
            .getWithResponse("sampleResourceGroup", "sampleDnsResolver", "sampleInboundEndpoint",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("key1", "fakeTokenPlaceholder")).apply();
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

### VirtualNetworkLinks_CreateOrUpdate

```java
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for DnsResolverDomainLists CreateOrUpdate.
 */
public final class DnsResolverDomainListsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/
     * DnsResolverDomainList_BulkDomains_Put.json
     */
    /**
     * Sample code: Upsert DNS resolver domain list with bulk number of domains.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void upsertDNSResolverDomainListWithBulkNumberOfDomains(
        com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.dnsResolverDomainLists()
            .define("sampleDnsResolverDomainList")
            .withRegion("westus2")
            .withExistingResourceGroup("sampleResourceGroup")
            .withTags(mapOf("key1", "fakeTokenPlaceholder"))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/DnsResolverDomainList_Put
     * .json
     */
    /**
     * Sample code: Upsert DNS resolver domain list with less than 1000 domains.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void upsertDNSResolverDomainListWithLessThan1000Domains(
        com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.dnsResolverDomainLists()
            .define("sampleDnsResolverDomainList")
            .withRegion("westus2")
            .withExistingResourceGroup("sampleResourceGroup")
            .withTags(mapOf("key1", "fakeTokenPlaceholder"))
            .withDomains(Arrays.asList("contoso.com"))
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

### VirtualNetworkLinks_Delete

```java
/**
 * Samples for DnsResolverDomainLists Delete.
 */
public final class DnsResolverDomainListsDeleteSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/
     * DnsResolverDomainList_Delete.json
     */
    /**
     * Sample code: Delete DNS resolver domain list.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void deleteDNSResolverDomainList(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.dnsResolverDomainLists()
            .delete("sampleResourceGroup", "sampleDnsResolverDomainList", null, com.azure.core.util.Context.NONE);
    }
}
```

### VirtualNetworkLinks_Get

```java
/**
 * Samples for DnsResolverDomainLists List.
 */
public final class DnsResolverDomainListsListSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/
     * DnsResolverDomainList_ListBySubscription.json
     */
    /**
     * Sample code: List DNS resolver domain lists by subscription.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void
        listDNSResolverDomainListsBySubscription(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.dnsResolverDomainLists().list(null, com.azure.core.util.Context.NONE);
    }
}
```

### VirtualNetworkLinks_List

```java
/**
 * Samples for OutboundEndpoints List.
 */
public final class OutboundEndpointsListSamples {
    /*
     * x-ms-original-file:
     * specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/OutboundEndpoint_List.
     * json
     */
    /**
     * Sample code: List outbound endpoints by DNS resolver.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void
        listOutboundEndpointsByDNSResolver(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.outboundEndpoints()
            .list("sampleResourceGroup", "sampleDnsResolver", null, com.azure.core.util.Context.NONE);
    }
}
```

### VirtualNetworkLinks_Update

```java
/**
 * Samples for OutboundEndpoints Get.
 */
public final class OutboundEndpointsGetSamples {
    /*
     * x-ms-original-file:
     * specification/dnsresolver/resource-manager/Microsoft.Network/stable/2025-05-01/examples/OutboundEndpoint_Get.json
     */
    /**
     * Sample code: Retrieve outbound endpoint for DNS resolver.
     * 
     * @param manager Entry point to DnsResolverManager.
     */
    public static void
        retrieveOutboundEndpointForDNSResolver(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.outboundEndpoints()
            .getWithResponse("sampleResourceGroup", "sampleDnsResolver", "sampleOutboundEndpoint",
                com.azure.core.util.Context.NONE);
    }
}
```

