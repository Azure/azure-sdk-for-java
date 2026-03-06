# Code snippets and samples


## HybridConnections

- [CreateOrUpdate](#hybridconnections_createorupdate)
- [CreateOrUpdateAuthorizationRule](#hybridconnections_createorupdateauthorizationrule)
- [Delete](#hybridconnections_delete)
- [DeleteAuthorizationRule](#hybridconnections_deleteauthorizationrule)
- [Get](#hybridconnections_get)
- [GetAuthorizationRule](#hybridconnections_getauthorizationrule)
- [ListAuthorizationRules](#hybridconnections_listauthorizationrules)
- [ListByNamespace](#hybridconnections_listbynamespace)
- [ListKeys](#hybridconnections_listkeys)
- [RegenerateKeys](#hybridconnections_regeneratekeys)

## Namespaces

- [CheckNameAvailability](#namespaces_checknameavailability)
- [CreateOrUpdate](#namespaces_createorupdate)
- [CreateOrUpdateAuthorizationRule](#namespaces_createorupdateauthorizationrule)
- [CreateOrUpdateNetworkRuleSet](#namespaces_createorupdatenetworkruleset)
- [Delete](#namespaces_delete)
- [DeleteAuthorizationRule](#namespaces_deleteauthorizationrule)
- [GetAuthorizationRule](#namespaces_getauthorizationrule)
- [GetByResourceGroup](#namespaces_getbyresourcegroup)
- [GetNetworkRuleSet](#namespaces_getnetworkruleset)
- [List](#namespaces_list)
- [ListAuthorizationRules](#namespaces_listauthorizationrules)
- [ListByResourceGroup](#namespaces_listbyresourcegroup)
- [ListKeys](#namespaces_listkeys)
- [RegenerateKeys](#namespaces_regeneratekeys)
- [Update](#namespaces_update)

## Operations

- [List](#operations_list)

## PrivateEndpointConnections

- [CreateOrUpdate](#privateendpointconnections_createorupdate)
- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [List](#privateendpointconnections_list)

## PrivateLinkResources

- [Get](#privatelinkresources_get)
- [List](#privatelinkresources_list)

## WCFRelays

- [CreateOrUpdate](#wcfrelays_createorupdate)
- [CreateOrUpdateAuthorizationRule](#wcfrelays_createorupdateauthorizationrule)
- [Delete](#wcfrelays_delete)
- [DeleteAuthorizationRule](#wcfrelays_deleteauthorizationrule)
- [Get](#wcfrelays_get)
- [GetAuthorizationRule](#wcfrelays_getauthorizationrule)
- [ListAuthorizationRules](#wcfrelays_listauthorizationrules)
- [ListByNamespace](#wcfrelays_listbynamespace)
- [ListKeys](#wcfrelays_listkeys)
- [RegenerateKeys](#wcfrelays_regeneratekeys)
### HybridConnections_CreateOrUpdate

```java
/**
 * Samples for HybridConnections CreateOrUpdate.
 */
public final class HybridConnectionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2024-01-01/HybridConnection/RelayHybridConnectionCreate.json
     */
    /**
     * Sample code: RelayHybridConnectionCreate.
     * 
     * @param manager Entry point to RelayManager.
     */
    public static void relayHybridConnectionCreate(com.azure.resourcemanager.relay.RelayManager manager) {
        manager.hybridConnections()
            .define("example-Relay-Hybrid-01")
            .withExistingNamespace("resourcegroup", "example-RelayNamespace-01")
            .withRequiresClientAuthorization(true)
            .create();
    }
}
```

### HybridConnections_CreateOrUpdateAuthorizationRule

```java
import com.azure.resourcemanager.relay.fluent.models.AuthorizationRuleInner;
import com.azure.resourcemanager.relay.models.AccessRights;
import java.util.Arrays;

/**
 * Samples for HybridConnections CreateOrUpdateAuthorizationRule.
 */
public final class HybridConnectionsCreateOrUpdateAuthorizationRuleSamples {
    /*
     * x-ms-original-file: 2024-01-01/HybridConnection/RelayHybridConnectionAuthorizationRuleCreate.json
     */
    /**
     * Sample code: RelayHybridConnectionAuthorizationRuleCreate.
     * 
     * @param manager Entry point to RelayManager.
     */
    public static void
        relayHybridConnectionAuthorizationRuleCreate(com.azure.resourcemanager.relay.RelayManager manager) {
        manager.hybridConnections()
            .createOrUpdateAuthorizationRuleWithResponse("resourcegroup", "example-RelayNamespace-01",
                "example-Relay-Hybrid-01", "example-RelayAuthRules-01",
                new AuthorizationRuleInner().withRights(Arrays.asList(AccessRights.LISTEN, AccessRights.SEND)),
                com.azure.core.util.Context.NONE);
    }
}
```

### HybridConnections_Delete

```java
/**
 * Samples for HybridConnections Delete.
 */
public final class HybridConnectionsDeleteSamples {
    /*
     * x-ms-original-file: 2024-01-01/HybridConnection/RelayHybridconnectionDelete.json
     */
    /**
     * Sample code: RelayHybridconnectionDelete.
     * 
     * @param manager Entry point to RelayManager.
     */
    public static void relayHybridconnectionDelete(com.azure.resourcemanager.relay.RelayManager manager) {
        manager.hybridConnections()
            .deleteWithResponse("resourcegroup", "example-RelayNamespace-01", "example-Relay-Hybrid-01",
                com.azure.core.util.Context.NONE);
    }
}
```

### HybridConnections_DeleteAuthorizationRule

```java
/**
 * Samples for HybridConnections DeleteAuthorizationRule.
 */
public final class HybridConnectionsDeleteAuthorizationRuleSamples {
    /*
     * x-ms-original-file: 2024-01-01/HybridConnection/RelayHybridConnectionAuthorizationRuleDelete.json
     */
    /**
     * Sample code: RelayHybridConnectionAuthorizationRuleDelete.
     * 
     * @param manager Entry point to RelayManager.
     */
    public static void
        relayHybridConnectionAuthorizationRuleDelete(com.azure.resourcemanager.relay.RelayManager manager) {
        manager.hybridConnections()
            .deleteAuthorizationRuleWithResponse("resourcegroup", "example-RelayNamespace-01",
                "example-Relay-Hybrid-01", "example-RelayAuthRules-01", com.azure.core.util.Context.NONE);
    }
}
```

### HybridConnections_Get

```java
/**
 * Samples for HybridConnections Get.
 */
public final class HybridConnectionsGetSamples {
    /*
     * x-ms-original-file: 2024-01-01/HybridConnection/RelayHybridConnectionGet.json
     */
    /**
     * Sample code: RelayHybridConnectionGet.
     * 
     * @param manager Entry point to RelayManager.
     */
    public static void relayHybridConnectionGet(com.azure.resourcemanager.relay.RelayManager manager) {
        manager.hybridConnections()
            .getWithResponse("resourcegroup", "example-RelayNamespace-01", "example-Relay-Hybrid-01",
                com.azure.core.util.Context.NONE);
    }
}
```

### HybridConnections_GetAuthorizationRule

```java
/**
 * Samples for HybridConnections GetAuthorizationRule.
 */
public final class HybridConnectionsGetAuthorizationRuleSamples {
    /*
     * x-ms-original-file: 2024-01-01/HybridConnection/RelayHybridConnectionAuthorizationRuleGet.json
     */
    /**
     * Sample code: RelayHybridConnectionAuthorizationRuleGet.
     * 
     * @param manager Entry point to RelayManager.
     */
    public static void relayHybridConnectionAuthorizationRuleGet(com.azure.resourcemanager.relay.RelayManager manager) {
        manager.hybridConnections()
            .getAuthorizationRuleWithResponse("resourcegroup", "example-RelayNamespace-01", "example-Relay-Hybrid-01",
                "example-RelayAuthRules-01", com.azure.core.util.Context.NONE);
    }
}
```

### HybridConnections_ListAuthorizationRules

```java
/**
 * Samples for HybridConnections ListAuthorizationRules.
 */
public final class HybridConnectionsListAuthorizationRulesSamples {
    /*
     * x-ms-original-file: 2024-01-01/HybridConnection/RelayHybridConnectionAuthorizationRuleListAll.json
     */
    /**
     * Sample code: RelayHybridConnectionAuthorizationRuleListAll.
     * 
     * @param manager Entry point to RelayManager.
     */
    public static void
        relayHybridConnectionAuthorizationRuleListAll(com.azure.resourcemanager.relay.RelayManager manager) {
        manager.hybridConnections()
            .listAuthorizationRules("resourcegroup", "example-RelayNamespace-01", "example-Relay-Hybrid-01",
                com.azure.core.util.Context.NONE);
    }
}
```

### HybridConnections_ListByNamespace

```java
/**
 * Samples for HybridConnections ListByNamespace.
 */
public final class HybridConnectionsListByNamespaceSamples {
    /*
     * x-ms-original-file: 2024-01-01/HybridConnection/RelayHybridConnectionListAll.json
     */
    /**
     * Sample code: RelayHybridConnectionListAll.
     * 
     * @param manager Entry point to RelayManager.
     */
    public static void relayHybridConnectionListAll(com.azure.resourcemanager.relay.RelayManager manager) {
        manager.hybridConnections()
            .listByNamespace("resourcegroup", "example-RelayNamespace-01", com.azure.core.util.Context.NONE);
    }
}
```

### HybridConnections_ListKeys

```java
/**
 * Samples for HybridConnections ListKeys.
 */
public final class HybridConnectionsListKeysSamples {
    /*
     * x-ms-original-file: 2024-01-01/HybridConnection/RelayHybridConnectionAuthorizationRuleListKey.json
     */
    /**
     * Sample code: RelayHybridConnectionAuthorizationRuleListKey.
     * 
     * @param manager Entry point to RelayManager.
     */
    public static void
        relayHybridConnectionAuthorizationRuleListKey(com.azure.resourcemanager.relay.RelayManager manager) {
        manager.hybridConnections()
            .listKeysWithResponse("resourcegroup", "example-RelayNamespace-01", "example-Relay-Hybrid-01",
                "example-RelayAuthRules-01", com.azure.core.util.Context.NONE);
    }
}
```

### HybridConnections_RegenerateKeys

```java
import com.azure.resourcemanager.relay.models.KeyType;
import com.azure.resourcemanager.relay.models.RegenerateAccessKeyParameters;

/**
 * Samples for HybridConnections RegenerateKeys.
 */
public final class HybridConnectionsRegenerateKeysSamples {
    /*
     * x-ms-original-file: 2024-01-01/HybridConnection/RelayHybridConnectionAuthorizationRuleRegenerateKey.json
     */
    /**
     * Sample code: RelayHybridConnectionAuthorizationRuleRegenerateKey.
     * 
     * @param manager Entry point to RelayManager.
     */
    public static void
        relayHybridConnectionAuthorizationRuleRegenerateKey(com.azure.resourcemanager.relay.RelayManager manager) {
        manager.hybridConnections()
            .regenerateKeysWithResponse("resourcegroup", "example-RelayNamespace-01", "example-Relay-Hybrid-01",
                "example-RelayAuthRules-01", new RegenerateAccessKeyParameters().withKeyType(KeyType.PRIMARY_KEY),
                com.azure.core.util.Context.NONE);
    }
}
```

### Namespaces_CheckNameAvailability

```java
import com.azure.resourcemanager.relay.models.CheckNameAvailability;

/**
 * Samples for Namespaces CheckNameAvailability.
 */
public final class NamespacesCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: 2024-01-01/NameSpaces/RelayNameSpaceCheckNameAvailability.json
     */
    /**
     * Sample code: RelayCheckNameAvailability.
     * 
     * @param manager Entry point to RelayManager.
     */
    public static void relayCheckNameAvailability(com.azure.resourcemanager.relay.RelayManager manager) {
        manager.namespaces()
            .checkNameAvailabilityWithResponse(new CheckNameAvailability().withName("example-RelayNamespace1321"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Namespaces_CreateOrUpdate

```java
import com.azure.resourcemanager.relay.models.Sku;
import com.azure.resourcemanager.relay.models.SkuName;
import com.azure.resourcemanager.relay.models.SkuTier;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Namespaces CreateOrUpdate.
 */
public final class NamespacesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2024-01-01/NameSpaces/RelayNameSpaceCreate.json
     */
    /**
     * Sample code: RelayNamespaceCreate.
     * 
     * @param manager Entry point to RelayManager.
     */
    public static void relayNamespaceCreate(com.azure.resourcemanager.relay.RelayManager manager) {
        manager.namespaces()
            .define("example-RelayNamespace-5849")
            .withRegion("South Central US")
            .withExistingResourceGroup("resourcegroup")
            .withTags(mapOf("tag1", "value1", "tag2", "value2"))
            .withSku(new Sku().withName(SkuName.STANDARD).withTier(SkuTier.STANDARD))
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

### Namespaces_CreateOrUpdateAuthorizationRule

```java
import com.azure.resourcemanager.relay.models.AccessRights;
import java.util.Arrays;

/**
 * Samples for Namespaces CreateOrUpdateAuthorizationRule.
 */
public final class NamespacesCreateOrUpdateAuthorizationRuleSamples {
    /*
     * x-ms-original-file: 2024-01-01/NameSpaces/RelayNameSpaceAuthorizationRuleCreate.json
     */
    /**
     * Sample code: RelayNameSpaceAuthorizationRuleCreate.
     * 
     * @param manager Entry point to RelayManager.
     */
    public static void relayNameSpaceAuthorizationRuleCreate(com.azure.resourcemanager.relay.RelayManager manager) {
        manager.namespaces()
            .defineAuthorizationRule("example-RelayAuthRules-01")
            .withExistingNamespace("resourcegroup", "example-RelayNamespace-01")
            .withRights(Arrays.asList(AccessRights.LISTEN, AccessRights.SEND))
            .create();
    }
}
```

### Namespaces_CreateOrUpdateNetworkRuleSet

```java
import com.azure.resourcemanager.relay.fluent.models.NetworkRuleSetInner;
import com.azure.resourcemanager.relay.models.DefaultAction;
import com.azure.resourcemanager.relay.models.NWRuleSetIpRules;
import com.azure.resourcemanager.relay.models.NetworkRuleIPAction;
import java.util.Arrays;

/**
 * Samples for Namespaces CreateOrUpdateNetworkRuleSet.
 */
public final class NamespacesCreateOrUpdateNetworkRuleSetSamples {
    /*
     * x-ms-original-file: 2024-01-01/VirtualNetworkRules/RelayNetworkRuleSetCreate.json
     */
    /**
     * Sample code: NameSpaceNetworkRuleSetCreate.
     * 
     * @param manager Entry point to RelayManager.
     */
    public static void nameSpaceNetworkRuleSetCreate(com.azure.resourcemanager.relay.RelayManager manager) {
        manager.namespaces()
            .createOrUpdateNetworkRuleSetWithResponse("ResourceGroup", "example-RelayNamespace-6019",
                new NetworkRuleSetInner().withTrustedServiceAccessEnabled(false)
                    .withDefaultAction(DefaultAction.DENY)
                    .withIpRules(Arrays.asList(
                        new NWRuleSetIpRules().withIpMask("1.1.1.1").withAction(NetworkRuleIPAction.ALLOW),
                        new NWRuleSetIpRules().withIpMask("1.1.1.2").withAction(NetworkRuleIPAction.ALLOW),
                        new NWRuleSetIpRules().withIpMask("1.1.1.3").withAction(NetworkRuleIPAction.ALLOW),
                        new NWRuleSetIpRules().withIpMask("1.1.1.4").withAction(NetworkRuleIPAction.ALLOW),
                        new NWRuleSetIpRules().withIpMask("1.1.1.5").withAction(NetworkRuleIPAction.ALLOW))),
                com.azure.core.util.Context.NONE);
    }
}
```

### Namespaces_Delete

```java
/**
 * Samples for Namespaces Delete.
 */
public final class NamespacesDeleteSamples {
    /*
     * x-ms-original-file: 2024-01-01/NameSpaces/RelayNameSpaceDelete.json
     */
    /**
     * Sample code: RelayNameSpaceDelete.
     * 
     * @param manager Entry point to RelayManager.
     */
    public static void relayNameSpaceDelete(com.azure.resourcemanager.relay.RelayManager manager) {
        manager.namespaces().delete("SouthCentralUS", "example-RelayNamespace-5849", com.azure.core.util.Context.NONE);
    }
}
```

### Namespaces_DeleteAuthorizationRule

```java
/**
 * Samples for Namespaces DeleteAuthorizationRule.
 */
public final class NamespacesDeleteAuthorizationRuleSamples {
    /*
     * x-ms-original-file: 2024-01-01/NameSpaces/RelayNameSpaceAuthorizationRuleDelete.json
     */
    /**
     * Sample code: RelayNameSpaceAuthorizationRuleDelete.
     * 
     * @param manager Entry point to RelayManager.
     */
    public static void relayNameSpaceAuthorizationRuleDelete(com.azure.resourcemanager.relay.RelayManager manager) {
        manager.namespaces()
            .deleteAuthorizationRuleWithResponse("resourcegroup", "example-RelayNamespace-01",
                "example-RelayAuthRules-01", com.azure.core.util.Context.NONE);
    }
}
```

### Namespaces_GetAuthorizationRule

```java
/**
 * Samples for Namespaces GetAuthorizationRule.
 */
public final class NamespacesGetAuthorizationRuleSamples {
    /*
     * x-ms-original-file: 2024-01-01/NameSpaces/RelayNameSpaceAuthorizationRuleGet.json
     */
    /**
     * Sample code: RelayNameSpaceAuthorizationRuleGet.
     * 
     * @param manager Entry point to RelayManager.
     */
    public static void relayNameSpaceAuthorizationRuleGet(com.azure.resourcemanager.relay.RelayManager manager) {
        manager.namespaces()
            .getAuthorizationRuleWithResponse("resourcegroup", "example-RelayNamespace-01", "example-RelayAuthRules-01",
                com.azure.core.util.Context.NONE);
    }
}
```

### Namespaces_GetByResourceGroup

```java
/**
 * Samples for Namespaces GetByResourceGroup.
 */
public final class NamespacesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-01-01/NameSpaces/RelayNameSpaceGet.json
     */
    /**
     * Sample code: RelayNameSpaceGet.
     * 
     * @param manager Entry point to RelayManager.
     */
    public static void relayNameSpaceGet(com.azure.resourcemanager.relay.RelayManager manager) {
        manager.namespaces()
            .getByResourceGroupWithResponse("RG-eg", "example-RelayRelayNamespace-01",
                com.azure.core.util.Context.NONE);
    }
}
```

### Namespaces_GetNetworkRuleSet

```java
/**
 * Samples for Namespaces GetNetworkRuleSet.
 */
public final class NamespacesGetNetworkRuleSetSamples {
    /*
     * x-ms-original-file: 2024-01-01/VirtualNetworkRules/RelayNetworkRuleSetGet.json
     */
    /**
     * Sample code: NameSpaceNetworkRuleSetGet.
     * 
     * @param manager Entry point to RelayManager.
     */
    public static void nameSpaceNetworkRuleSetGet(com.azure.resourcemanager.relay.RelayManager manager) {
        manager.namespaces()
            .getNetworkRuleSetWithResponse("ResourceGroup", "example-RelayNamespace-6019",
                com.azure.core.util.Context.NONE);
    }
}
```

### Namespaces_List

```java
/**
 * Samples for Namespaces List.
 */
public final class NamespacesListSamples {
    /*
     * x-ms-original-file: 2024-01-01/NameSpaces/RelayNameSpaceListBySubscription.json
     */
    /**
     * Sample code: RelayNameSpaceListBySubscription.
     * 
     * @param manager Entry point to RelayManager.
     */
    public static void relayNameSpaceListBySubscription(com.azure.resourcemanager.relay.RelayManager manager) {
        manager.namespaces().list(com.azure.core.util.Context.NONE);
    }
}
```

### Namespaces_ListAuthorizationRules

```java
/**
 * Samples for Namespaces ListAuthorizationRules.
 */
public final class NamespacesListAuthorizationRulesSamples {
    /*
     * x-ms-original-file: 2024-01-01/NameSpaces/RelayNameSpaceAuthorizationRuleListAll.json
     */
    /**
     * Sample code: RelayNameSpaceAuthorizationRuleListAll.
     * 
     * @param manager Entry point to RelayManager.
     */
    public static void relayNameSpaceAuthorizationRuleListAll(com.azure.resourcemanager.relay.RelayManager manager) {
        manager.namespaces()
            .listAuthorizationRules("resourcegroup", "example-RelayNamespace-01", com.azure.core.util.Context.NONE);
    }
}
```

### Namespaces_ListByResourceGroup

```java
/**
 * Samples for Namespaces ListByResourceGroup.
 */
public final class NamespacesListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-01-01/NameSpaces/RelayNameSpaceListByResourceGroup.json
     */
    /**
     * Sample code: RelayNameSpaceListByResourceGroup.
     * 
     * @param manager Entry point to RelayManager.
     */
    public static void relayNameSpaceListByResourceGroup(com.azure.resourcemanager.relay.RelayManager manager) {
        manager.namespaces().listByResourceGroup("resourcegroup", com.azure.core.util.Context.NONE);
    }
}
```

### Namespaces_ListKeys

```java
/**
 * Samples for Namespaces ListKeys.
 */
public final class NamespacesListKeysSamples {
    /*
     * x-ms-original-file: 2024-01-01/NameSpaces/RelayNameSpaceAuthorizationRuleListKey.json
     */
    /**
     * Sample code: RelayNameSpaceAuthorizationRuleListKey.
     * 
     * @param manager Entry point to RelayManager.
     */
    public static void relayNameSpaceAuthorizationRuleListKey(com.azure.resourcemanager.relay.RelayManager manager) {
        manager.namespaces()
            .listKeysWithResponse("resourcegroup", "example-RelayNamespace-01", "example-RelayAuthRules-01",
                com.azure.core.util.Context.NONE);
    }
}
```

### Namespaces_RegenerateKeys

```java
import com.azure.resourcemanager.relay.models.KeyType;
import com.azure.resourcemanager.relay.models.RegenerateAccessKeyParameters;

/**
 * Samples for Namespaces RegenerateKeys.
 */
public final class NamespacesRegenerateKeysSamples {
    /*
     * x-ms-original-file: 2024-01-01/NameSpaces/RelayNameSpaceAuthorizationRuleRegenerateKey.json
     */
    /**
     * Sample code: RelayNameSpaceAuthorizationRuleRegenerateKey.
     * 
     * @param manager Entry point to RelayManager.
     */
    public static void
        relayNameSpaceAuthorizationRuleRegenerateKey(com.azure.resourcemanager.relay.RelayManager manager) {
        manager.namespaces()
            .regenerateKeysWithResponse("resourcegroup", "example-RelayNamespace-01", "example-RelayAuthRules-01",
                new RegenerateAccessKeyParameters().withKeyType(KeyType.PRIMARY_KEY), com.azure.core.util.Context.NONE);
    }
}
```

### Namespaces_Update

```java
import com.azure.resourcemanager.relay.models.RelayNamespace;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Namespaces Update.
 */
public final class NamespacesUpdateSamples {
    /*
     * x-ms-original-file: 2024-01-01/NameSpaces/RelayNameSpaceUpdate.json
     */
    /**
     * Sample code: RelayNameSpaceUpdate.
     * 
     * @param manager Entry point to RelayManager.
     */
    public static void relayNameSpaceUpdate(com.azure.resourcemanager.relay.RelayManager manager) {
        RelayNamespace resource = manager.namespaces()
            .getByResourceGroupWithResponse("RG-eg", "example-RelayRelayNamespace-01", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("tag3", "value3", "tag4", "value4", "tag5", "value5", "tag6", "value6"))
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

### Operations_List

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2024-01-01/RelayOperations_List.json
     */
    /**
     * Sample code: RelayOperationsList.
     * 
     * @param manager Entry point to RelayManager.
     */
    public static void relayOperationsList(com.azure.resourcemanager.relay.RelayManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_CreateOrUpdate

```java
import com.azure.resourcemanager.relay.models.ConnectionState;
import com.azure.resourcemanager.relay.models.PrivateEndpoint;
import com.azure.resourcemanager.relay.models.PrivateLinkConnectionStatus;

/**
 * Samples for PrivateEndpointConnections CreateOrUpdate.
 */
public final class PrivateEndpointConnectionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2024-01-01/PrivateEndpointConnections/PrivateEndpointConnectionsCreate.json
     */
    /**
     * Sample code: NameSpacePrivateEndPointConnectionCreate.
     * 
     * @param manager Entry point to RelayManager.
     */
    public static void nameSpacePrivateEndPointConnectionCreate(com.azure.resourcemanager.relay.RelayManager manager) {
        manager.privateEndpointConnections()
            .define("{privateEndpointConnection name}")
            .withExistingNamespace("resourcegroup", "example-RelayNamespace-5849")
            .withPrivateEndpoint(new PrivateEndpoint().withId(
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/resourcegroup/providers/Microsoft.Network/privateEndpoints/ali-relay-pve-1"))
            .withPrivateLinkServiceConnectionState(
                new ConnectionState().withStatus(PrivateLinkConnectionStatus.APPROVED).withDescription("You may pass"))
            .create();
    }
}
```

### PrivateEndpointConnections_Delete

```java
/**
 * Samples for PrivateEndpointConnections Delete.
 */
public final class PrivateEndpointConnectionsDeleteSamples {
    /*
     * x-ms-original-file: 2024-01-01/PrivateEndpointConnections/PrivateEndpointConnectionsDelete.json
     */
    /**
     * Sample code: NameSpacePrivateEndPointConnectionDelete.
     * 
     * @param manager Entry point to RelayManager.
     */
    public static void nameSpacePrivateEndPointConnectionDelete(com.azure.resourcemanager.relay.RelayManager manager) {
        manager.privateEndpointConnections()
            .delete("myResourceGroup", "example-RelayNamespace-5849", "{privateEndpointConnection name}",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_Get

```java
/**
 * Samples for PrivateEndpointConnections Get.
 */
public final class PrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file: 2024-01-01/PrivateEndpointConnections/PrivateEndpointConnectionsGet.json
     */
    /**
     * Sample code: NameSpacePrivateEndPointConnectionGet.
     * 
     * @param manager Entry point to RelayManager.
     */
    public static void nameSpacePrivateEndPointConnectionGet(com.azure.resourcemanager.relay.RelayManager manager) {
        manager.privateEndpointConnections()
            .getWithResponse("myResourceGroup", "example-RelayNamespace-5849", "{privateEndpointConnection name}",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_List

```java
/**
 * Samples for PrivateEndpointConnections List.
 */
public final class PrivateEndpointConnectionsListSamples {
    /*
     * x-ms-original-file: 2024-01-01/PrivateEndpointConnections/PrivateEndpointConnectionsList.json
     */
    /**
     * Sample code: PrivateEndpointConnectionsList.
     * 
     * @param manager Entry point to RelayManager.
     */
    public static void privateEndpointConnectionsList(com.azure.resourcemanager.relay.RelayManager manager) {
        manager.privateEndpointConnections()
            .list("myResourceGroup", "example-RelayNamespace-5849", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkResources_Get

```java
/**
 * Samples for PrivateLinkResources Get.
 */
public final class PrivateLinkResourcesGetSamples {
    /*
     * x-ms-original-file: 2024-01-01/PrivateEndpointConnections/PrivateLinkResourcesGet.json
     */
    /**
     * Sample code: NameSpacePrivateEndPointConnectionGet.
     * 
     * @param manager Entry point to RelayManager.
     */
    public static void nameSpacePrivateEndPointConnectionGet(com.azure.resourcemanager.relay.RelayManager manager) {
        manager.privateLinkResources()
            .getWithResponse("resourcegroup", "example-RelayNamespace-5849", "{PrivateLinkResource name}",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkResources_List

```java
/**
 * Samples for PrivateLinkResources List.
 */
public final class PrivateLinkResourcesListSamples {
    /*
     * x-ms-original-file: 2024-01-01/PrivateEndpointConnections/PrivateLinkResourcesList.json
     */
    /**
     * Sample code: NameSpacePrivateLinkResourcesGet.
     * 
     * @param manager Entry point to RelayManager.
     */
    public static void nameSpacePrivateLinkResourcesGet(com.azure.resourcemanager.relay.RelayManager manager) {
        manager.privateLinkResources()
            .listWithResponse("resourcegroup", "example-RelayNamespace-5849", com.azure.core.util.Context.NONE);
    }
}
```

### WCFRelays_CreateOrUpdate

```java
import com.azure.resourcemanager.relay.models.Relaytype;

/**
 * Samples for WCFRelays CreateOrUpdate.
 */
public final class WCFRelaysCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2024-01-01/Relay/RelayCreate.json
     */
    /**
     * Sample code: RelayCreate.
     * 
     * @param manager Entry point to RelayManager.
     */
    public static void relayCreate(com.azure.resourcemanager.relay.RelayManager manager) {
        manager.wCFRelays()
            .define("example-Relay-Wcf-1194")
            .withExistingNamespace("resourcegroup", "example-RelayNamespace-9953")
            .withRelayType(Relaytype.NET_TCP)
            .withRequiresClientAuthorization(true)
            .withRequiresTransportSecurity(true)
            .create();
    }
}
```

### WCFRelays_CreateOrUpdateAuthorizationRule

```java
import com.azure.resourcemanager.relay.fluent.models.AuthorizationRuleInner;
import com.azure.resourcemanager.relay.models.AccessRights;
import java.util.Arrays;

/**
 * Samples for WCFRelays CreateOrUpdateAuthorizationRule.
 */
public final class WCFRelaysCreateOrUpdateAuthorizationRuleSamples {
    /*
     * x-ms-original-file: 2024-01-01/Relay/RelayAuthorizationRuleCreate.json
     */
    /**
     * Sample code: RelayAuthorizationRuleCreate.
     * 
     * @param manager Entry point to RelayManager.
     */
    public static void relayAuthorizationRuleCreate(com.azure.resourcemanager.relay.RelayManager manager) {
        manager.wCFRelays()
            .createOrUpdateAuthorizationRuleWithResponse("resourcegroup", "example-RelayNamespace-01",
                "example-Relay-wcf-01", "example-RelayAuthRules-01",
                new AuthorizationRuleInner().withRights(Arrays.asList(AccessRights.LISTEN, AccessRights.SEND)),
                com.azure.core.util.Context.NONE);
    }
}
```

### WCFRelays_Delete

```java
/**
 * Samples for WCFRelays Delete.
 */
public final class WCFRelaysDeleteSamples {
    /*
     * x-ms-original-file: 2024-01-01/Relay/RelayDelete.json
     */
    /**
     * Sample code: RelayDelete.
     * 
     * @param manager Entry point to RelayManager.
     */
    public static void relayDelete(com.azure.resourcemanager.relay.RelayManager manager) {
        manager.wCFRelays()
            .deleteWithResponse("resourcegroup", "example-RelayNamespace-01", "example-Relay-wcf-01",
                com.azure.core.util.Context.NONE);
    }
}
```

### WCFRelays_DeleteAuthorizationRule

```java
/**
 * Samples for WCFRelays DeleteAuthorizationRule.
 */
public final class WCFRelaysDeleteAuthorizationRuleSamples {
    /*
     * x-ms-original-file: 2024-01-01/Relay/RelayAuthorizationRuleDelete.json
     */
    /**
     * Sample code: RelayAuthorizationRuleDelete.
     * 
     * @param manager Entry point to RelayManager.
     */
    public static void relayAuthorizationRuleDelete(com.azure.resourcemanager.relay.RelayManager manager) {
        manager.wCFRelays()
            .deleteAuthorizationRuleWithResponse("resourcegroup", "example-RelayNamespace-01", "example-Relay-wcf-01",
                "example-RelayAuthRules-01", com.azure.core.util.Context.NONE);
    }
}
```

### WCFRelays_Get

```java
/**
 * Samples for WCFRelays Get.
 */
public final class WCFRelaysGetSamples {
    /*
     * x-ms-original-file: 2024-01-01/Relay/RelayGet.json
     */
    /**
     * Sample code: RelayGet.
     * 
     * @param manager Entry point to RelayManager.
     */
    public static void relayGet(com.azure.resourcemanager.relay.RelayManager manager) {
        manager.wCFRelays()
            .getWithResponse("resourcegroup", "example-RelayNamespace-9953", "example-Relay-Wcf-1194",
                com.azure.core.util.Context.NONE);
    }
}
```

### WCFRelays_GetAuthorizationRule

```java
/**
 * Samples for WCFRelays GetAuthorizationRule.
 */
public final class WCFRelaysGetAuthorizationRuleSamples {
    /*
     * x-ms-original-file: 2024-01-01/Relay/RelayAuthorizationRuleGet.json
     */
    /**
     * Sample code: RelayAuthorizationRuleGet.
     * 
     * @param manager Entry point to RelayManager.
     */
    public static void relayAuthorizationRuleGet(com.azure.resourcemanager.relay.RelayManager manager) {
        manager.wCFRelays()
            .getAuthorizationRuleWithResponse("resourcegroup", "example-RelayNamespace-01", "example-Relay-wcf-01",
                "example-RelayAuthRules-01", com.azure.core.util.Context.NONE);
    }
}
```

### WCFRelays_ListAuthorizationRules

```java
/**
 * Samples for WCFRelays ListAuthorizationRules.
 */
public final class WCFRelaysListAuthorizationRulesSamples {
    /*
     * x-ms-original-file: 2024-01-01/Relay/RelayAuthorizationRuleListAll.json
     */
    /**
     * Sample code: RelayAuthorizationRuleListAll.
     * 
     * @param manager Entry point to RelayManager.
     */
    public static void relayAuthorizationRuleListAll(com.azure.resourcemanager.relay.RelayManager manager) {
        manager.wCFRelays()
            .listAuthorizationRules("resourcegroup", "example-RelayNamespace-01", "example-Relay-Wcf-01",
                com.azure.core.util.Context.NONE);
    }
}
```

### WCFRelays_ListByNamespace

```java
/**
 * Samples for WCFRelays ListByNamespace.
 */
public final class WCFRelaysListByNamespaceSamples {
    /*
     * x-ms-original-file: 2024-01-01/Relay/RelayListAll.json
     */
    /**
     * Sample code: RelayListAll.
     * 
     * @param manager Entry point to RelayManager.
     */
    public static void relayListAll(com.azure.resourcemanager.relay.RelayManager manager) {
        manager.wCFRelays()
            .listByNamespace("resourcegroup", "example-RelayNamespace-01", com.azure.core.util.Context.NONE);
    }
}
```

### WCFRelays_ListKeys

```java
/**
 * Samples for WCFRelays ListKeys.
 */
public final class WCFRelaysListKeysSamples {
    /*
     * x-ms-original-file: 2024-01-01/Relay/RelayAuthorizationRuleListKey.json
     */
    /**
     * Sample code: RelayAuthorizationRuleListKey.json.
     * 
     * @param manager Entry point to RelayManager.
     */
    public static void relayAuthorizationRuleListKeyJson(com.azure.resourcemanager.relay.RelayManager manager) {
        manager.wCFRelays()
            .listKeysWithResponse("resourcegroup", "example-RelayNamespace-01", "example-Relay-wcf-01",
                "example-RelayAuthRules-01", com.azure.core.util.Context.NONE);
    }
}
```

### WCFRelays_RegenerateKeys

```java
import com.azure.resourcemanager.relay.models.KeyType;
import com.azure.resourcemanager.relay.models.RegenerateAccessKeyParameters;

/**
 * Samples for WCFRelays RegenerateKeys.
 */
public final class WCFRelaysRegenerateKeysSamples {
    /*
     * x-ms-original-file: 2024-01-01/Relay/RelayAuthorizationRuleRegenerateKey.json
     */
    /**
     * Sample code: RelayAuthorizationRuleRegenerateKey.json.
     * 
     * @param manager Entry point to RelayManager.
     */
    public static void relayAuthorizationRuleRegenerateKeyJson(com.azure.resourcemanager.relay.RelayManager manager) {
        manager.wCFRelays()
            .regenerateKeysWithResponse("resourcegroup", "example-RelayNamespace-01", "example-Relay-wcf-01",
                "example-RelayAuthRules-01", new RegenerateAccessKeyParameters().withKeyType(KeyType.PRIMARY_KEY),
                com.azure.core.util.Context.NONE);
    }
}
```

