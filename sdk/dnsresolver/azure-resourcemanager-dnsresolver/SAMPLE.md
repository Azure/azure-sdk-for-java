# Code snippets and samples


## DnsForwardingRulesets

- [CreateOrUpdate](#dnsforwardingrulesets_createorupdate)
- [Delete](#dnsforwardingrulesets_delete)
- [GetByResourceGroup](#dnsforwardingrulesets_getbyresourcegroup)
- [List](#dnsforwardingrulesets_list)
- [ListByResourceGroup](#dnsforwardingrulesets_listbyresourcegroup)
- [ListByVirtualNetwork](#dnsforwardingrulesets_listbyvirtualnetwork)
- [Update](#dnsforwardingrulesets_update)

## DnsResolvers

- [CreateOrUpdate](#dnsresolvers_createorupdate)
- [Delete](#dnsresolvers_delete)
- [GetByResourceGroup](#dnsresolvers_getbyresourcegroup)
- [List](#dnsresolvers_list)
- [ListByResourceGroup](#dnsresolvers_listbyresourcegroup)
- [ListByVirtualNetwork](#dnsresolvers_listbyvirtualnetwork)
- [Update](#dnsresolvers_update)

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
import com.azure.core.management.SubResource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for DnsForwardingRulesets CreateOrUpdate. */
public final class DnsForwardingRulesetsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2022-07-01/examples/DnsForwardingRuleset_Put.json
     */
    /**
     * Sample code: Upsert DNS forwarding ruleset.
     *
     * @param manager Entry point to DnsResolverManager.
     */
    public static void upsertDNSForwardingRuleset(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager
            .dnsForwardingRulesets()
            .define("samplednsForwardingRuleset")
            .withRegion("westus2")
            .withExistingResourceGroup("sampleResourceGroup")
            .withDnsResolverOutboundEndpoints(
                Arrays
                    .asList(
                        new SubResource()
                            .withId(
                                "/subscriptions/abdd4249-9f34-4cc6-8e42-c2e32110603e/resourceGroups/sampleResourceGroup/providers/Microsoft.Network/dnsResolvers/sampleDnsResolver/outboundEndpoints/sampleOutboundEndpoint0"),
                        new SubResource()
                            .withId(
                                "/subscriptions/abdd4249-9f34-4cc6-8e42-c2e32110603e/resourceGroups/sampleResourceGroup/providers/Microsoft.Network/dnsResolvers/sampleDnsResolver/outboundEndpoints/sampleOutboundEndpoint1")))
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

### DnsForwardingRulesets_Delete

```java
import com.azure.core.util.Context;

/** Samples for DnsForwardingRulesets Delete. */
public final class DnsForwardingRulesetsDeleteSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2022-07-01/examples/DnsForwardingRuleset_Delete.json
     */
    /**
     * Sample code: Delete DNS forwarding ruleset.
     *
     * @param manager Entry point to DnsResolverManager.
     */
    public static void deleteDNSForwardingRuleset(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager
            .dnsForwardingRulesets()
            .delete("sampleResourceGroup", "samplednsForwardingRulesetName", null, Context.NONE);
    }
}
```

### DnsForwardingRulesets_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for DnsForwardingRulesets GetByResourceGroup. */
public final class DnsForwardingRulesetsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2022-07-01/examples/DnsForwardingRuleset_Get.json
     */
    /**
     * Sample code: Retrieve DNS forwarding ruleset.
     *
     * @param manager Entry point to DnsResolverManager.
     */
    public static void retrieveDNSForwardingRuleset(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager
            .dnsForwardingRulesets()
            .getByResourceGroupWithResponse("sampleResourceGroup", "sampleDnsForwardingRuleset", Context.NONE);
    }
}
```

### DnsForwardingRulesets_List

```java
import com.azure.core.util.Context;

/** Samples for DnsForwardingRulesets List. */
public final class DnsForwardingRulesetsListSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2022-07-01/examples/DnsForwardingRuleset_ListBySubscription.json
     */
    /**
     * Sample code: List DNS forwarding rulesets by subscription.
     *
     * @param manager Entry point to DnsResolverManager.
     */
    public static void listDNSForwardingRulesetsBySubscription(
        com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.dnsForwardingRulesets().list(null, Context.NONE);
    }
}
```

### DnsForwardingRulesets_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for DnsForwardingRulesets ListByResourceGroup. */
public final class DnsForwardingRulesetsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2022-07-01/examples/DnsForwardingRuleset_ListByResourceGroup.json
     */
    /**
     * Sample code: List DNS forwarding rulesets by resource group.
     *
     * @param manager Entry point to DnsResolverManager.
     */
    public static void listDNSForwardingRulesetsByResourceGroup(
        com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.dnsForwardingRulesets().listByResourceGroup("sampleResourceGroup", null, Context.NONE);
    }
}
```

### DnsForwardingRulesets_ListByVirtualNetwork

```java
import com.azure.core.util.Context;

/** Samples for DnsForwardingRulesets ListByVirtualNetwork. */
public final class DnsForwardingRulesetsListByVirtualNetworkSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2022-07-01/examples/DnsForwardingRuleset_ListByVirtualNetwork.json
     */
    /**
     * Sample code: List DNS forwarding rulesets by virtual network.
     *
     * @param manager Entry point to DnsResolverManager.
     */
    public static void listDNSForwardingRulesetsByVirtualNetwork(
        com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager
            .dnsForwardingRulesets()
            .listByVirtualNetwork("sampleResourceGroup", "sampleVirtualNetwork", null, Context.NONE);
    }
}
```

### DnsForwardingRulesets_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.dnsresolver.models.DnsForwardingRuleset;
import java.util.HashMap;
import java.util.Map;

/** Samples for DnsForwardingRulesets Update. */
public final class DnsForwardingRulesetsUpdateSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2022-07-01/examples/DnsForwardingRuleset_Patch.json
     */
    /**
     * Sample code: Update DNS forwarding ruleset.
     *
     * @param manager Entry point to DnsResolverManager.
     */
    public static void updateDNSForwardingRuleset(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        DnsForwardingRuleset resource =
            manager
                .dnsForwardingRulesets()
                .getByResourceGroupWithResponse("sampleResourceGroup", "sampleDnsForwardingRuleset", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("key1", "value1")).apply();
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

### DnsResolvers_CreateOrUpdate

```java
import com.azure.core.management.SubResource;
import java.util.HashMap;
import java.util.Map;

/** Samples for DnsResolvers CreateOrUpdate. */
public final class DnsResolversCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2022-07-01/examples/DnsResolver_Put.json
     */
    /**
     * Sample code: Upsert DNS resolver.
     *
     * @param manager Entry point to DnsResolverManager.
     */
    public static void upsertDNSResolver(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager
            .dnsResolvers()
            .define("sampleDnsResolver")
            .withRegion("westus2")
            .withExistingResourceGroup("sampleResourceGroup")
            .withVirtualNetwork(
                new SubResource()
                    .withId(
                        "/subscriptions/cbb1387e-4b03-44f2-ad41-58d4677b9873/resourceGroups/virtualNetworkResourceGroup/providers/Microsoft.Network/virtualNetworks/sampleVirtualNetwork"))
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

### DnsResolvers_Delete

```java
import com.azure.core.util.Context;

/** Samples for DnsResolvers Delete. */
public final class DnsResolversDeleteSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2022-07-01/examples/DnsResolver_Delete.json
     */
    /**
     * Sample code: Delete DNS resolver.
     *
     * @param manager Entry point to DnsResolverManager.
     */
    public static void deleteDNSResolver(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.dnsResolvers().delete("sampleResourceGroup", "sampleDnsResolver", null, Context.NONE);
    }
}
```

### DnsResolvers_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for DnsResolvers GetByResourceGroup. */
public final class DnsResolversGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2022-07-01/examples/DnsResolver_Get.json
     */
    /**
     * Sample code: Retrieve DNS resolver.
     *
     * @param manager Entry point to DnsResolverManager.
     */
    public static void retrieveDNSResolver(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.dnsResolvers().getByResourceGroupWithResponse("sampleResourceGroup", "sampleDnsResolver", Context.NONE);
    }
}
```

### DnsResolvers_List

```java
import com.azure.core.util.Context;

/** Samples for DnsResolvers List. */
public final class DnsResolversListSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2022-07-01/examples/DnsResolver_ListBySubscription.json
     */
    /**
     * Sample code: List DNS resolvers by subscription.
     *
     * @param manager Entry point to DnsResolverManager.
     */
    public static void listDNSResolversBySubscription(
        com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.dnsResolvers().list(null, Context.NONE);
    }
}
```

### DnsResolvers_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for DnsResolvers ListByResourceGroup. */
public final class DnsResolversListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2022-07-01/examples/DnsResolver_ListByResourceGroup.json
     */
    /**
     * Sample code: List DNS resolvers by resource group.
     *
     * @param manager Entry point to DnsResolverManager.
     */
    public static void listDNSResolversByResourceGroup(
        com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.dnsResolvers().listByResourceGroup("sampleResourceGroup", null, Context.NONE);
    }
}
```

### DnsResolvers_ListByVirtualNetwork

```java
import com.azure.core.util.Context;

/** Samples for DnsResolvers ListByVirtualNetwork. */
public final class DnsResolversListByVirtualNetworkSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2022-07-01/examples/DnsResolver_ListByVirtualNetwork.json
     */
    /**
     * Sample code: List DNS resolvers by virtual network.
     *
     * @param manager Entry point to DnsResolverManager.
     */
    public static void listDNSResolversByVirtualNetwork(
        com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.dnsResolvers().listByVirtualNetwork("sampleResourceGroup", "sampleVirtualNetwork", null, Context.NONE);
    }
}
```

### DnsResolvers_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.dnsresolver.models.DnsResolver;
import java.util.HashMap;
import java.util.Map;

/** Samples for DnsResolvers Update. */
public final class DnsResolversUpdateSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2022-07-01/examples/DnsResolver_Patch.json
     */
    /**
     * Sample code: Update DNS resolver.
     *
     * @param manager Entry point to DnsResolverManager.
     */
    public static void updateDNSResolver(com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        DnsResolver resource =
            manager
                .dnsResolvers()
                .getByResourceGroupWithResponse("sampleResourceGroup", "sampleDnsResolver", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("key1", "value1")).apply();
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

### ForwardingRules_CreateOrUpdate

```java
import com.azure.resourcemanager.dnsresolver.models.ForwardingRuleState;
import com.azure.resourcemanager.dnsresolver.models.TargetDnsServer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for ForwardingRules CreateOrUpdate. */
public final class ForwardingRulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2022-07-01/examples/ForwardingRule_Put.json
     */
    /**
     * Sample code: Upsert forwarding rule in a DNS forwarding ruleset.
     *
     * @param manager Entry point to DnsResolverManager.
     */
    public static void upsertForwardingRuleInADNSForwardingRuleset(
        com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager
            .forwardingRules()
            .define("sampleForwardingRule")
            .withExistingDnsForwardingRuleset("sampleResourceGroup", "sampleDnsForwardingRuleset")
            .withDomainName("contoso.com.")
            .withTargetDnsServers(
                Arrays
                    .asList(
                        new TargetDnsServer().withIpAddress("10.0.0.1").withPort(53),
                        new TargetDnsServer().withIpAddress("10.0.0.2").withPort(53)))
            .withMetadata(mapOf("additionalProp1", "value1"))
            .withForwardingRuleState(ForwardingRuleState.ENABLED)
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

### ForwardingRules_Delete

```java
import com.azure.core.util.Context;

/** Samples for ForwardingRules Delete. */
public final class ForwardingRulesDeleteSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2022-07-01/examples/ForwardingRule_Delete.json
     */
    /**
     * Sample code: Delete forwarding rule in a DNS forwarding ruleset.
     *
     * @param manager Entry point to DnsResolverManager.
     */
    public static void deleteForwardingRuleInADNSForwardingRuleset(
        com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager
            .forwardingRules()
            .deleteWithResponse(
                "sampleResourceGroup", "sampleDnsForwardingRuleset", "sampleForwardingRule", null, Context.NONE);
    }
}
```

### ForwardingRules_Get

```java
import com.azure.core.util.Context;

/** Samples for ForwardingRules Get. */
public final class ForwardingRulesGetSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2022-07-01/examples/ForwardingRule_Get.json
     */
    /**
     * Sample code: Retrieve forwarding rule in a DNS forwarding ruleset.
     *
     * @param manager Entry point to DnsResolverManager.
     */
    public static void retrieveForwardingRuleInADNSForwardingRuleset(
        com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager
            .forwardingRules()
            .getWithResponse("sampleResourceGroup", "sampleDnsForwardingRuleset", "sampleForwardingRule", Context.NONE);
    }
}
```

### ForwardingRules_List

```java
import com.azure.core.util.Context;

/** Samples for ForwardingRules List. */
public final class ForwardingRulesListSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2022-07-01/examples/ForwardingRule_List.json
     */
    /**
     * Sample code: List forwarding rules in a DNS forwarding ruleset.
     *
     * @param manager Entry point to DnsResolverManager.
     */
    public static void listForwardingRulesInADNSForwardingRuleset(
        com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.forwardingRules().list("sampleResourceGroup", "sampleDnsForwardingRuleset", null, Context.NONE);
    }
}
```

### ForwardingRules_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.dnsresolver.models.ForwardingRule;
import com.azure.resourcemanager.dnsresolver.models.ForwardingRuleState;
import java.util.HashMap;
import java.util.Map;

/** Samples for ForwardingRules Update. */
public final class ForwardingRulesUpdateSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2022-07-01/examples/ForwardingRule_Patch.json
     */
    /**
     * Sample code: Update forwarding rule in a DNS forwarding ruleset.
     *
     * @param manager Entry point to DnsResolverManager.
     */
    public static void updateForwardingRuleInADNSForwardingRuleset(
        com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        ForwardingRule resource =
            manager
                .forwardingRules()
                .getWithResponse(
                    "sampleResourceGroup", "sampleDnsForwardingRuleset", "sampleForwardingRule", Context.NONE)
                .getValue();
        resource
            .update()
            .withMetadata(mapOf("additionalProp2", "value2"))
            .withForwardingRuleState(ForwardingRuleState.DISABLED)
            .apply();
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

### InboundEndpoints_CreateOrUpdate

```java
import com.azure.core.management.SubResource;
import com.azure.resourcemanager.dnsresolver.models.IpAllocationMethod;
import com.azure.resourcemanager.dnsresolver.models.IpConfiguration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for InboundEndpoints CreateOrUpdate. */
public final class InboundEndpointsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2022-07-01/examples/InboundEndpoint_Put.json
     */
    /**
     * Sample code: Upsert inbound endpoint for DNS resolver.
     *
     * @param manager Entry point to DnsResolverManager.
     */
    public static void upsertInboundEndpointForDNSResolver(
        com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager
            .inboundEndpoints()
            .define("sampleInboundEndpoint")
            .withRegion("westus2")
            .withExistingDnsResolver("sampleResourceGroup", "sampleDnsResolver")
            .withIpConfigurations(
                Arrays
                    .asList(
                        new IpConfiguration()
                            .withSubnet(
                                new SubResource()
                                    .withId(
                                        "/subscriptions/0403cfa9-9659-4f33-9f30-1f191c51d111/resourceGroups/sampleVnetResourceGroupName/providers/Microsoft.Network/virtualNetworks/sampleVirtualNetwork/subnets/sampleSubnet"))
                            .withPrivateIpAllocationMethod(IpAllocationMethod.DYNAMIC)))
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

### InboundEndpoints_Delete

```java
import com.azure.core.util.Context;

/** Samples for InboundEndpoints Delete. */
public final class InboundEndpointsDeleteSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2022-07-01/examples/InboundEndpoint_Delete.json
     */
    /**
     * Sample code: Delete inbound endpoint for DNS resolver.
     *
     * @param manager Entry point to DnsResolverManager.
     */
    public static void deleteInboundEndpointForDNSResolver(
        com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager
            .inboundEndpoints()
            .delete("sampleResourceGroup", "sampleDnsResolver", "sampleInboundEndpoint", null, Context.NONE);
    }
}
```

### InboundEndpoints_Get

```java
import com.azure.core.util.Context;

/** Samples for InboundEndpoints Get. */
public final class InboundEndpointsGetSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2022-07-01/examples/InboundEndpoint_Get.json
     */
    /**
     * Sample code: Retrieve inbound endpoint for DNS resolver.
     *
     * @param manager Entry point to DnsResolverManager.
     */
    public static void retrieveInboundEndpointForDNSResolver(
        com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager
            .inboundEndpoints()
            .getWithResponse("sampleResourceGroup", "sampleDnsResolver", "sampleInboundEndpoint", Context.NONE);
    }
}
```

### InboundEndpoints_List

```java
import com.azure.core.util.Context;

/** Samples for InboundEndpoints List. */
public final class InboundEndpointsListSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2022-07-01/examples/InboundEndpoint_List.json
     */
    /**
     * Sample code: List inbound endpoints by DNS resolver.
     *
     * @param manager Entry point to DnsResolverManager.
     */
    public static void listInboundEndpointsByDNSResolver(
        com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.inboundEndpoints().list("sampleResourceGroup", "sampleDnsResolver", null, Context.NONE);
    }
}
```

### InboundEndpoints_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.dnsresolver.models.InboundEndpoint;
import java.util.HashMap;
import java.util.Map;

/** Samples for InboundEndpoints Update. */
public final class InboundEndpointsUpdateSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2022-07-01/examples/InboundEndpoint_Patch.json
     */
    /**
     * Sample code: Update inbound endpoint for DNS resolver.
     *
     * @param manager Entry point to DnsResolverManager.
     */
    public static void updateInboundEndpointForDNSResolver(
        com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        InboundEndpoint resource =
            manager
                .inboundEndpoints()
                .getWithResponse("sampleResourceGroup", "sampleDnsResolver", "sampleInboundEndpoint", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("key1", "value1")).apply();
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

### OutboundEndpoints_CreateOrUpdate

```java
import com.azure.core.management.SubResource;
import java.util.HashMap;
import java.util.Map;

/** Samples for OutboundEndpoints CreateOrUpdate. */
public final class OutboundEndpointsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2022-07-01/examples/OutboundEndpoint_Put.json
     */
    /**
     * Sample code: Upsert outbound endpoint for DNS resolver.
     *
     * @param manager Entry point to DnsResolverManager.
     */
    public static void upsertOutboundEndpointForDNSResolver(
        com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager
            .outboundEndpoints()
            .define("sampleOutboundEndpoint")
            .withRegion("westus2")
            .withExistingDnsResolver("sampleResourceGroup", "sampleDnsResolver")
            .withSubnet(
                new SubResource()
                    .withId(
                        "/subscriptions/0403cfa9-9659-4f33-9f30-1f191c51d111/resourceGroups/sampleVnetResourceGroupName/providers/Microsoft.Network/virtualNetworks/sampleVirtualNetwork/subnets/sampleSubnet"))
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

### OutboundEndpoints_Delete

```java
import com.azure.core.util.Context;

/** Samples for OutboundEndpoints Delete. */
public final class OutboundEndpointsDeleteSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2022-07-01/examples/OutboundEndpoint_Delete.json
     */
    /**
     * Sample code: Delete outbound endpoint for DNS resolver.
     *
     * @param manager Entry point to DnsResolverManager.
     */
    public static void deleteOutboundEndpointForDNSResolver(
        com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager
            .outboundEndpoints()
            .delete("sampleResourceGroup", "sampleDnsResolver", "sampleOutboundEndpoint", null, Context.NONE);
    }
}
```

### OutboundEndpoints_Get

```java
import com.azure.core.util.Context;

/** Samples for OutboundEndpoints Get. */
public final class OutboundEndpointsGetSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2022-07-01/examples/OutboundEndpoint_Get.json
     */
    /**
     * Sample code: Retrieve outbound endpoint for DNS resolver.
     *
     * @param manager Entry point to DnsResolverManager.
     */
    public static void retrieveOutboundEndpointForDNSResolver(
        com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager
            .outboundEndpoints()
            .getWithResponse("sampleResourceGroup", "sampleDnsResolver", "sampleOutboundEndpoint", Context.NONE);
    }
}
```

### OutboundEndpoints_List

```java
import com.azure.core.util.Context;

/** Samples for OutboundEndpoints List. */
public final class OutboundEndpointsListSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2022-07-01/examples/OutboundEndpoint_List.json
     */
    /**
     * Sample code: List outbound endpoints by DNS resolver.
     *
     * @param manager Entry point to DnsResolverManager.
     */
    public static void listOutboundEndpointsByDNSResolver(
        com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.outboundEndpoints().list("sampleResourceGroup", "sampleDnsResolver", null, Context.NONE);
    }
}
```

### OutboundEndpoints_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.dnsresolver.models.OutboundEndpoint;
import java.util.HashMap;
import java.util.Map;

/** Samples for OutboundEndpoints Update. */
public final class OutboundEndpointsUpdateSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2022-07-01/examples/OutboundEndpoint_Patch.json
     */
    /**
     * Sample code: Update outbound endpoint for DNS resolver.
     *
     * @param manager Entry point to DnsResolverManager.
     */
    public static void updateOutboundEndpointForDNSResolver(
        com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        OutboundEndpoint resource =
            manager
                .outboundEndpoints()
                .getWithResponse("sampleResourceGroup", "sampleDnsResolver", "sampleOutboundEndpoint", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("key1", "value1")).apply();
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
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2022-07-01/examples/VirtualNetworkLink_Put.json
     */
    /**
     * Sample code: Upsert virtual network link to a DNS forwarding ruleset.
     *
     * @param manager Entry point to DnsResolverManager.
     */
    public static void upsertVirtualNetworkLinkToADNSForwardingRuleset(
        com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager
            .virtualNetworkLinks()
            .define("sampleVirtualNetworkLink")
            .withExistingDnsForwardingRuleset("sampleResourceGroup", "sampleDnsForwardingRuleset")
            .withVirtualNetwork(
                new SubResource()
                    .withId(
                        "/subscriptions/0403cfa9-9659-4f33-9f30-1f191c51d111/resourceGroups/sampleVnetResourceGroupName/providers/Microsoft.Network/virtualNetworks/sampleVirtualNetwork"))
            .withMetadata(mapOf("additionalProp1", "value1"))
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
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2022-07-01/examples/VirtualNetworkLink_Delete.json
     */
    /**
     * Sample code: Delete virtual network link to a DNS forwarding ruleset.
     *
     * @param manager Entry point to DnsResolverManager.
     */
    public static void deleteVirtualNetworkLinkToADNSForwardingRuleset(
        com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager
            .virtualNetworkLinks()
            .delete(
                "sampleResourceGroup", "sampleDnsForwardingRuleset", "sampleVirtualNetworkLink", null, Context.NONE);
    }
}
```

### VirtualNetworkLinks_Get

```java
import com.azure.core.util.Context;

/** Samples for VirtualNetworkLinks Get. */
public final class VirtualNetworkLinksGetSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2022-07-01/examples/VirtualNetworkLink_Get.json
     */
    /**
     * Sample code: Retrieve virtual network link to a DNS forwarding ruleset.
     *
     * @param manager Entry point to DnsResolverManager.
     */
    public static void retrieveVirtualNetworkLinkToADNSForwardingRuleset(
        com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager
            .virtualNetworkLinks()
            .getWithResponse(
                "sampleResourceGroup", "sampleDnsForwardingRuleset", "sampleVirtualNetworkLink", Context.NONE);
    }
}
```

### VirtualNetworkLinks_List

```java
import com.azure.core.util.Context;

/** Samples for VirtualNetworkLinks List. */
public final class VirtualNetworkLinksListSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2022-07-01/examples/VirtualNetworkLink_List.json
     */
    /**
     * Sample code: List virtual network links to a DNS forwarding ruleset.
     *
     * @param manager Entry point to DnsResolverManager.
     */
    public static void listVirtualNetworkLinksToADNSForwardingRuleset(
        com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        manager.virtualNetworkLinks().list("sampleResourceGroup", "sampleDnsForwardingRuleset", null, Context.NONE);
    }
}
```

### VirtualNetworkLinks_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.dnsresolver.models.VirtualNetworkLink;
import java.util.HashMap;
import java.util.Map;

/** Samples for VirtualNetworkLinks Update. */
public final class VirtualNetworkLinksUpdateSamples {
    /*
     * x-ms-original-file: specification/dnsresolver/resource-manager/Microsoft.Network/stable/2022-07-01/examples/VirtualNetworkLink_Patch.json
     */
    /**
     * Sample code: Update virtual network link to a DNS forwarding ruleset.
     *
     * @param manager Entry point to DnsResolverManager.
     */
    public static void updateVirtualNetworkLinkToADNSForwardingRuleset(
        com.azure.resourcemanager.dnsresolver.DnsResolverManager manager) {
        VirtualNetworkLink resource =
            manager
                .virtualNetworkLinks()
                .getWithResponse(
                    "sampleResourceGroup", "sampleDnsForwardingRuleset", "sampleVirtualNetworkLink", Context.NONE)
                .getValue();
        resource.update().withMetadata(mapOf("additionalProp1", "value1")).apply();
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

