# Code snippets and samples


## Operations

- [List](#operations_list)

## Usages

- [List](#usages_list)

## WebPubSub

- [CheckNameAvailability](#webpubsub_checknameavailability)
- [CreateOrUpdate](#webpubsub_createorupdate)
- [Delete](#webpubsub_delete)
- [GetByResourceGroup](#webpubsub_getbyresourcegroup)
- [List](#webpubsub_list)
- [ListByResourceGroup](#webpubsub_listbyresourcegroup)
- [ListKeys](#webpubsub_listkeys)
- [ListReplicaSkus](#webpubsub_listreplicaskus)
- [ListSkus](#webpubsub_listskus)
- [RegenerateKey](#webpubsub_regeneratekey)
- [Restart](#webpubsub_restart)
- [Update](#webpubsub_update)

## WebPubSubCustomCertificates

- [CreateOrUpdate](#webpubsubcustomcertificates_createorupdate)
- [Delete](#webpubsubcustomcertificates_delete)
- [Get](#webpubsubcustomcertificates_get)
- [List](#webpubsubcustomcertificates_list)

## WebPubSubCustomDomains

- [CreateOrUpdate](#webpubsubcustomdomains_createorupdate)
- [Delete](#webpubsubcustomdomains_delete)
- [Get](#webpubsubcustomdomains_get)
- [List](#webpubsubcustomdomains_list)

## WebPubSubHubs

- [CreateOrUpdate](#webpubsubhubs_createorupdate)
- [Delete](#webpubsubhubs_delete)
- [Get](#webpubsubhubs_get)
- [List](#webpubsubhubs_list)

## WebPubSubPrivateEndpointConnections

- [Delete](#webpubsubprivateendpointconnections_delete)
- [Get](#webpubsubprivateendpointconnections_get)
- [List](#webpubsubprivateendpointconnections_list)
- [Update](#webpubsubprivateendpointconnections_update)

## WebPubSubPrivateLinkResources

- [List](#webpubsubprivatelinkresources_list)

## WebPubSubReplicaSharedPrivateLinkResources

- [CreateOrUpdate](#webpubsubreplicasharedprivatelinkresources_createorupdate)
- [Get](#webpubsubreplicasharedprivatelinkresources_get)
- [List](#webpubsubreplicasharedprivatelinkresources_list)

## WebPubSubReplicas

- [CreateOrUpdate](#webpubsubreplicas_createorupdate)
- [Delete](#webpubsubreplicas_delete)
- [Get](#webpubsubreplicas_get)
- [List](#webpubsubreplicas_list)
- [Restart](#webpubsubreplicas_restart)
- [Update](#webpubsubreplicas_update)

## WebPubSubSharedPrivateLinkResources

- [CreateOrUpdate](#webpubsubsharedprivatelinkresources_createorupdate)
- [Delete](#webpubsubsharedprivatelinkresources_delete)
- [Get](#webpubsubsharedprivatelinkresources_get)
- [List](#webpubsubsharedprivatelinkresources_list)
### Operations_List

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file:
     * specification/webpubsub/resource-manager/Microsoft.SignalRService/stable/2024-03-01/examples/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to WebPubSubManager.
     */
    public static void operationsList(com.azure.resourcemanager.webpubsub.WebPubSubManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Usages_List

```java
/**
 * Samples for Usages List.
 */
public final class UsagesListSamples {
    /*
     * x-ms-original-file:
     * specification/webpubsub/resource-manager/Microsoft.SignalRService/stable/2024-03-01/examples/Usages_List.json
     */
    /**
     * Sample code: Usages_List.
     * 
     * @param manager Entry point to WebPubSubManager.
     */
    public static void usagesList(com.azure.resourcemanager.webpubsub.WebPubSubManager manager) {
        manager.usages().list("eastus", com.azure.core.util.Context.NONE);
    }
}
```

### WebPubSub_CheckNameAvailability

```java
import com.azure.resourcemanager.webpubsub.models.NameAvailabilityParameters;

/**
 * Samples for WebPubSub CheckNameAvailability.
 */
public final class WebPubSubCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/webpubsub/resource-manager/Microsoft.SignalRService/stable/2024-03-01/examples/
     * WebPubSub_CheckNameAvailability.json
     */
    /**
     * Sample code: WebPubSub_CheckNameAvailability.
     * 
     * @param manager Entry point to WebPubSubManager.
     */
    public static void webPubSubCheckNameAvailability(com.azure.resourcemanager.webpubsub.WebPubSubManager manager) {
        manager.webPubSubs()
            .checkNameAvailabilityWithResponse("eastus",
                new NameAvailabilityParameters().withType("Microsoft.SignalRService/WebPubSub")
                    .withName("myWebPubSubService"),
                com.azure.core.util.Context.NONE);
    }
}
```

### WebPubSub_CreateOrUpdate

```java
import com.azure.resourcemanager.webpubsub.models.AclAction;
import com.azure.resourcemanager.webpubsub.models.LiveTraceCategory;
import com.azure.resourcemanager.webpubsub.models.LiveTraceConfiguration;
import com.azure.resourcemanager.webpubsub.models.ManagedIdentity;
import com.azure.resourcemanager.webpubsub.models.ManagedIdentityType;
import com.azure.resourcemanager.webpubsub.models.NetworkAcl;
import com.azure.resourcemanager.webpubsub.models.PrivateEndpointAcl;
import com.azure.resourcemanager.webpubsub.models.ResourceSku;
import com.azure.resourcemanager.webpubsub.models.ServiceKind;
import com.azure.resourcemanager.webpubsub.models.WebPubSubNetworkACLs;
import com.azure.resourcemanager.webpubsub.models.WebPubSubRequestType;
import com.azure.resourcemanager.webpubsub.models.WebPubSubSkuTier;
import com.azure.resourcemanager.webpubsub.models.WebPubSubSocketIOSettings;
import com.azure.resourcemanager.webpubsub.models.WebPubSubTlsSettings;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for WebPubSub CreateOrUpdate.
 */
public final class WebPubSubCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/webpubsub/resource-manager/Microsoft.SignalRService/stable/2024-03-01/examples/
     * WebPubSub_CreateOrUpdate.json
     */
    /**
     * Sample code: WebPubSub_CreateOrUpdate.
     * 
     * @param manager Entry point to WebPubSubManager.
     */
    public static void webPubSubCreateOrUpdate(com.azure.resourcemanager.webpubsub.WebPubSubManager manager) {
        manager.webPubSubs()
            .define("myWebPubSubService")
            .withRegion("eastus")
            .withExistingResourceGroup("myResourceGroup")
            .withTags(mapOf("key1", "fakeTokenPlaceholder"))
            .withSku(new ResourceSku().withName("Premium_P1").withTier(WebPubSubSkuTier.PREMIUM).withCapacity(1))
            .withKind(ServiceKind.WEB_PUB_SUB)
            .withIdentity(new ManagedIdentity().withType(ManagedIdentityType.SYSTEM_ASSIGNED))
            .withTls(new WebPubSubTlsSettings().withClientCertEnabled(false))
            .withLiveTraceConfiguration(new LiveTraceConfiguration().withEnabled("false")
                .withCategories(
                    Arrays.asList(new LiveTraceCategory().withName("ConnectivityLogs").withEnabled("true"))))
            .withNetworkACLs(new WebPubSubNetworkACLs().withDefaultAction(AclAction.DENY)
                .withPublicNetwork(new NetworkAcl().withAllow(Arrays.asList(WebPubSubRequestType.CLIENT_CONNECTION)))
                .withPrivateEndpoints(Arrays
                    .asList(new PrivateEndpointAcl().withAllow(Arrays.asList(WebPubSubRequestType.SERVER_CONNECTION))
                        .withName("mywebpubsubservice.1fa229cd-bf3f-47f0-8c49-afb36723997e"))))
            .withPublicNetworkAccess("Enabled")
            .withDisableLocalAuth(false)
            .withDisableAadAuth(false)
            .withSocketIO(new WebPubSubSocketIOSettings().withServiceMode("Serverless"))
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

### WebPubSub_Delete

```java
/**
 * Samples for WebPubSub Delete.
 */
public final class WebPubSubDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/webpubsub/resource-manager/Microsoft.SignalRService/stable/2024-03-01/examples/WebPubSub_Delete.
     * json
     */
    /**
     * Sample code: WebPubSub_Delete.
     * 
     * @param manager Entry point to WebPubSubManager.
     */
    public static void webPubSubDelete(com.azure.resourcemanager.webpubsub.WebPubSubManager manager) {
        manager.webPubSubs().delete("myResourceGroup", "myWebPubSubService", com.azure.core.util.Context.NONE);
    }
}
```

### WebPubSub_GetByResourceGroup

```java
/**
 * Samples for WebPubSub GetByResourceGroup.
 */
public final class WebPubSubGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/webpubsub/resource-manager/Microsoft.SignalRService/stable/2024-03-01/examples/WebPubSub_Get.json
     */
    /**
     * Sample code: WebPubSub_Get.
     * 
     * @param manager Entry point to WebPubSubManager.
     */
    public static void webPubSubGet(com.azure.resourcemanager.webpubsub.WebPubSubManager manager) {
        manager.webPubSubs()
            .getByResourceGroupWithResponse("myResourceGroup", "myWebPubSubService", com.azure.core.util.Context.NONE);
    }
}
```

### WebPubSub_List

```java
/**
 * Samples for WebPubSub List.
 */
public final class WebPubSubListSamples {
    /*
     * x-ms-original-file: specification/webpubsub/resource-manager/Microsoft.SignalRService/stable/2024-03-01/examples/
     * WebPubSub_ListBySubscription.json
     */
    /**
     * Sample code: WebPubSub_ListBySubscription.
     * 
     * @param manager Entry point to WebPubSubManager.
     */
    public static void webPubSubListBySubscription(com.azure.resourcemanager.webpubsub.WebPubSubManager manager) {
        manager.webPubSubs().list(com.azure.core.util.Context.NONE);
    }
}
```

### WebPubSub_ListByResourceGroup

```java
/**
 * Samples for WebPubSub ListByResourceGroup.
 */
public final class WebPubSubListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/webpubsub/resource-manager/Microsoft.SignalRService/stable/2024-03-01/examples/
     * WebPubSub_ListByResourceGroup.json
     */
    /**
     * Sample code: WebPubSub_ListByResourceGroup.
     * 
     * @param manager Entry point to WebPubSubManager.
     */
    public static void webPubSubListByResourceGroup(com.azure.resourcemanager.webpubsub.WebPubSubManager manager) {
        manager.webPubSubs().listByResourceGroup("myResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### WebPubSub_ListKeys

```java
/**
 * Samples for WebPubSub ListKeys.
 */
public final class WebPubSubListKeysSamples {
    /*
     * x-ms-original-file:
     * specification/webpubsub/resource-manager/Microsoft.SignalRService/stable/2024-03-01/examples/WebPubSub_ListKeys.
     * json
     */
    /**
     * Sample code: WebPubSub_ListKeys.
     * 
     * @param manager Entry point to WebPubSubManager.
     */
    public static void webPubSubListKeys(com.azure.resourcemanager.webpubsub.WebPubSubManager manager) {
        manager.webPubSubs()
            .listKeysWithResponse("myResourceGroup", "myWebPubSubService", com.azure.core.util.Context.NONE);
    }
}
```

### WebPubSub_ListReplicaSkus

```java
/**
 * Samples for WebPubSub ListReplicaSkus.
 */
public final class WebPubSubListReplicaSkusSamples {
    /*
     * x-ms-original-file: specification/webpubsub/resource-manager/Microsoft.SignalRService/stable/2024-03-01/examples/
     * WebPubSub_ListReplicaSkus.json
     */
    /**
     * Sample code: WebPubSub_ListReplicaSkus.
     * 
     * @param manager Entry point to WebPubSubManager.
     */
    public static void webPubSubListReplicaSkus(com.azure.resourcemanager.webpubsub.WebPubSubManager manager) {
        manager.webPubSubs()
            .listReplicaSkusWithResponse("myResourceGroup", "myWebPubSubService", "myWebPubSubService-eastus",
                com.azure.core.util.Context.NONE);
    }
}
```

### WebPubSub_ListSkus

```java
/**
 * Samples for WebPubSub ListSkus.
 */
public final class WebPubSubListSkusSamples {
    /*
     * x-ms-original-file:
     * specification/webpubsub/resource-manager/Microsoft.SignalRService/stable/2024-03-01/examples/WebPubSub_ListSkus.
     * json
     */
    /**
     * Sample code: WebPubSub_ListSkus.
     * 
     * @param manager Entry point to WebPubSubManager.
     */
    public static void webPubSubListSkus(com.azure.resourcemanager.webpubsub.WebPubSubManager manager) {
        manager.webPubSubs()
            .listSkusWithResponse("myResourceGroup", "myWebPubSubService", com.azure.core.util.Context.NONE);
    }
}
```

### WebPubSub_RegenerateKey

```java
import com.azure.resourcemanager.webpubsub.models.KeyType;
import com.azure.resourcemanager.webpubsub.models.RegenerateKeyParameters;

/**
 * Samples for WebPubSub RegenerateKey.
 */
public final class WebPubSubRegenerateKeySamples {
    /*
     * x-ms-original-file: specification/webpubsub/resource-manager/Microsoft.SignalRService/stable/2024-03-01/examples/
     * WebPubSub_RegenerateKey.json
     */
    /**
     * Sample code: WebPubSub_RegenerateKey.
     * 
     * @param manager Entry point to WebPubSubManager.
     */
    public static void webPubSubRegenerateKey(com.azure.resourcemanager.webpubsub.WebPubSubManager manager) {
        manager.webPubSubs()
            .regenerateKey("myResourceGroup", "myWebPubSubService",
                new RegenerateKeyParameters().withKeyType(KeyType.PRIMARY), com.azure.core.util.Context.NONE);
    }
}
```

### WebPubSub_Restart

```java
/**
 * Samples for WebPubSub Restart.
 */
public final class WebPubSubRestartSamples {
    /*
     * x-ms-original-file:
     * specification/webpubsub/resource-manager/Microsoft.SignalRService/stable/2024-03-01/examples/WebPubSub_Restart.
     * json
     */
    /**
     * Sample code: WebPubSub_Restart.
     * 
     * @param manager Entry point to WebPubSubManager.
     */
    public static void webPubSubRestart(com.azure.resourcemanager.webpubsub.WebPubSubManager manager) {
        manager.webPubSubs().restart("myResourceGroup", "myWebPubSubService", com.azure.core.util.Context.NONE);
    }
}
```

### WebPubSub_Update

```java
import com.azure.resourcemanager.webpubsub.models.AclAction;
import com.azure.resourcemanager.webpubsub.models.LiveTraceCategory;
import com.azure.resourcemanager.webpubsub.models.LiveTraceConfiguration;
import com.azure.resourcemanager.webpubsub.models.ManagedIdentity;
import com.azure.resourcemanager.webpubsub.models.ManagedIdentityType;
import com.azure.resourcemanager.webpubsub.models.NetworkAcl;
import com.azure.resourcemanager.webpubsub.models.PrivateEndpointAcl;
import com.azure.resourcemanager.webpubsub.models.ResourceSku;
import com.azure.resourcemanager.webpubsub.models.WebPubSubNetworkACLs;
import com.azure.resourcemanager.webpubsub.models.WebPubSubRequestType;
import com.azure.resourcemanager.webpubsub.models.WebPubSubResource;
import com.azure.resourcemanager.webpubsub.models.WebPubSubSkuTier;
import com.azure.resourcemanager.webpubsub.models.WebPubSubSocketIOSettings;
import com.azure.resourcemanager.webpubsub.models.WebPubSubTlsSettings;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for WebPubSub Update.
 */
public final class WebPubSubUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/webpubsub/resource-manager/Microsoft.SignalRService/stable/2024-03-01/examples/WebPubSub_Update.
     * json
     */
    /**
     * Sample code: WebPubSub_Update.
     * 
     * @param manager Entry point to WebPubSubManager.
     */
    public static void webPubSubUpdate(com.azure.resourcemanager.webpubsub.WebPubSubManager manager) {
        WebPubSubResource resource = manager.webPubSubs()
            .getByResourceGroupWithResponse("myResourceGroup", "myWebPubSubService", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key1", "fakeTokenPlaceholder"))
            .withSku(new ResourceSku().withName("Premium_P1").withTier(WebPubSubSkuTier.PREMIUM).withCapacity(1))
            .withIdentity(new ManagedIdentity().withType(ManagedIdentityType.SYSTEM_ASSIGNED))
            .withTls(new WebPubSubTlsSettings().withClientCertEnabled(false))
            .withLiveTraceConfiguration(new LiveTraceConfiguration().withEnabled("false")
                .withCategories(
                    Arrays.asList(new LiveTraceCategory().withName("ConnectivityLogs").withEnabled("true"))))
            .withNetworkACLs(new WebPubSubNetworkACLs().withDefaultAction(AclAction.DENY)
                .withPublicNetwork(new NetworkAcl().withAllow(Arrays.asList(WebPubSubRequestType.CLIENT_CONNECTION)))
                .withPrivateEndpoints(Arrays
                    .asList(new PrivateEndpointAcl().withAllow(Arrays.asList(WebPubSubRequestType.SERVER_CONNECTION))
                        .withName("mywebpubsubservice.1fa229cd-bf3f-47f0-8c49-afb36723997e"))))
            .withPublicNetworkAccess("Enabled")
            .withDisableLocalAuth(false)
            .withDisableAadAuth(false)
            .withSocketIO(new WebPubSubSocketIOSettings().withServiceMode("Serverless"))
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

### WebPubSubCustomCertificates_CreateOrUpdate

```java
/**
 * Samples for WebPubSubCustomCertificates CreateOrUpdate.
 */
public final class WebPubSubCustomCertificatesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/webpubsub/resource-manager/Microsoft.SignalRService/stable/2024-03-01/examples/
     * WebPubSubCustomCertificates_CreateOrUpdate.json
     */
    /**
     * Sample code: WebPubSubCustomCertificates_CreateOrUpdate.
     * 
     * @param manager Entry point to WebPubSubManager.
     */
    public static void
        webPubSubCustomCertificatesCreateOrUpdate(com.azure.resourcemanager.webpubsub.WebPubSubManager manager) {
        manager.webPubSubCustomCertificates()
            .define("myCert")
            .withExistingWebPubSub("myResourceGroup", "myWebPubSubService")
            .withKeyVaultBaseUri("https://myvault.keyvault.azure.net/")
            .withKeyVaultSecretName("mycert")
            .withKeyVaultSecretVersion("bb6a44b2743f47f68dad0d6cc9756432")
            .create();
    }
}
```

### WebPubSubCustomCertificates_Delete

```java
/**
 * Samples for WebPubSubCustomCertificates Delete.
 */
public final class WebPubSubCustomCertificatesDeleteSamples {
    /*
     * x-ms-original-file: specification/webpubsub/resource-manager/Microsoft.SignalRService/stable/2024-03-01/examples/
     * WebPubSubCustomCertificates_Delete.json
     */
    /**
     * Sample code: WebPubSubCustomCertificates_Delete.
     * 
     * @param manager Entry point to WebPubSubManager.
     */
    public static void webPubSubCustomCertificatesDelete(com.azure.resourcemanager.webpubsub.WebPubSubManager manager) {
        manager.webPubSubCustomCertificates()
            .deleteWithResponse("myResourceGroup", "myWebPubSubService", "myCert", com.azure.core.util.Context.NONE);
    }
}
```

### WebPubSubCustomCertificates_Get

```java
/**
 * Samples for WebPubSubCustomCertificates Get.
 */
public final class WebPubSubCustomCertificatesGetSamples {
    /*
     * x-ms-original-file: specification/webpubsub/resource-manager/Microsoft.SignalRService/stable/2024-03-01/examples/
     * WebPubSubCustomCertificates_Get.json
     */
    /**
     * Sample code: WebPubSubCustomCertificates_Get.
     * 
     * @param manager Entry point to WebPubSubManager.
     */
    public static void webPubSubCustomCertificatesGet(com.azure.resourcemanager.webpubsub.WebPubSubManager manager) {
        manager.webPubSubCustomCertificates()
            .getWithResponse("myResourceGroup", "myWebPubSubService", "myCert", com.azure.core.util.Context.NONE);
    }
}
```

### WebPubSubCustomCertificates_List

```java
/**
 * Samples for WebPubSubCustomCertificates List.
 */
public final class WebPubSubCustomCertificatesListSamples {
    /*
     * x-ms-original-file: specification/webpubsub/resource-manager/Microsoft.SignalRService/stable/2024-03-01/examples/
     * WebPubSubCustomCertificates_List.json
     */
    /**
     * Sample code: WebPubSubCustomCertificates_List.
     * 
     * @param manager Entry point to WebPubSubManager.
     */
    public static void webPubSubCustomCertificatesList(com.azure.resourcemanager.webpubsub.WebPubSubManager manager) {
        manager.webPubSubCustomCertificates()
            .list("myResourceGroup", "myWebPubSubService", com.azure.core.util.Context.NONE);
    }
}
```

### WebPubSubCustomDomains_CreateOrUpdate

```java
import com.azure.resourcemanager.webpubsub.models.ResourceReference;

/**
 * Samples for WebPubSubCustomDomains CreateOrUpdate.
 */
public final class WebPubSubCustomDomainsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/webpubsub/resource-manager/Microsoft.SignalRService/stable/2024-03-01/examples/
     * WebPubSubCustomDomains_CreateOrUpdate.json
     */
    /**
     * Sample code: WebPubSubCustomDomains_CreateOrUpdate.
     * 
     * @param manager Entry point to WebPubSubManager.
     */
    public static void
        webPubSubCustomDomainsCreateOrUpdate(com.azure.resourcemanager.webpubsub.WebPubSubManager manager) {
        manager.webPubSubCustomDomains()
            .define("myDomain")
            .withExistingWebPubSub("myResourceGroup", "myWebPubSubService")
            .withDomainName("example.com")
            .withCustomCertificate(new ResourceReference().withId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/myResourceGroup/providers/Microsoft.SignalRService/WebPubSub/myWebPubSubService/customCertificates/myCert"))
            .create();
    }
}
```

### WebPubSubCustomDomains_Delete

```java
/**
 * Samples for WebPubSubCustomDomains Delete.
 */
public final class WebPubSubCustomDomainsDeleteSamples {
    /*
     * x-ms-original-file: specification/webpubsub/resource-manager/Microsoft.SignalRService/stable/2024-03-01/examples/
     * WebPubSubCustomDomains_Delete.json
     */
    /**
     * Sample code: WebPubSubCustomDomains_Delete.
     * 
     * @param manager Entry point to WebPubSubManager.
     */
    public static void webPubSubCustomDomainsDelete(com.azure.resourcemanager.webpubsub.WebPubSubManager manager) {
        manager.webPubSubCustomDomains()
            .delete("myResourceGroup", "myWebPubSubService", "example", com.azure.core.util.Context.NONE);
    }
}
```

### WebPubSubCustomDomains_Get

```java
/**
 * Samples for WebPubSubCustomDomains Get.
 */
public final class WebPubSubCustomDomainsGetSamples {
    /*
     * x-ms-original-file: specification/webpubsub/resource-manager/Microsoft.SignalRService/stable/2024-03-01/examples/
     * WebPubSubCustomDomains_Get.json
     */
    /**
     * Sample code: WebPubSubCustomDomains_Get.
     * 
     * @param manager Entry point to WebPubSubManager.
     */
    public static void webPubSubCustomDomainsGet(com.azure.resourcemanager.webpubsub.WebPubSubManager manager) {
        manager.webPubSubCustomDomains()
            .getWithResponse("myResourceGroup", "myWebPubSubService", "example", com.azure.core.util.Context.NONE);
    }
}
```

### WebPubSubCustomDomains_List

```java
/**
 * Samples for WebPubSubCustomDomains List.
 */
public final class WebPubSubCustomDomainsListSamples {
    /*
     * x-ms-original-file: specification/webpubsub/resource-manager/Microsoft.SignalRService/stable/2024-03-01/examples/
     * WebPubSubCustomDomains_List.json
     */
    /**
     * Sample code: WebPubSubCustomDomains_List.
     * 
     * @param manager Entry point to WebPubSubManager.
     */
    public static void webPubSubCustomDomainsList(com.azure.resourcemanager.webpubsub.WebPubSubManager manager) {
        manager.webPubSubCustomDomains()
            .list("myResourceGroup", "myWebPubSubService", com.azure.core.util.Context.NONE);
    }
}
```

### WebPubSubHubs_CreateOrUpdate

```java
import com.azure.resourcemanager.webpubsub.models.EventHandler;
import com.azure.resourcemanager.webpubsub.models.EventHubEndpoint;
import com.azure.resourcemanager.webpubsub.models.EventListener;
import com.azure.resourcemanager.webpubsub.models.EventNameFilter;
import com.azure.resourcemanager.webpubsub.models.ManagedIdentitySettings;
import com.azure.resourcemanager.webpubsub.models.UpstreamAuthSettings;
import com.azure.resourcemanager.webpubsub.models.UpstreamAuthType;
import com.azure.resourcemanager.webpubsub.models.WebPubSubHubProperties;
import java.util.Arrays;

/**
 * Samples for WebPubSubHubs CreateOrUpdate.
 */
public final class WebPubSubHubsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/webpubsub/resource-manager/Microsoft.SignalRService/stable/2024-03-01/examples/
     * WebPubSubHubs_CreateOrUpdate.json
     */
    /**
     * Sample code: WebPubSubHubs_CreateOrUpdate.
     * 
     * @param manager Entry point to WebPubSubManager.
     */
    public static void webPubSubHubsCreateOrUpdate(com.azure.resourcemanager.webpubsub.WebPubSubManager manager) {
        manager.webPubSubHubs()
            .define("exampleHub")
            .withExistingWebPubSub("myResourceGroup", "myWebPubSubService")
            .withProperties(new WebPubSubHubProperties()
                .withEventHandlers(Arrays.asList(new EventHandler().withUrlTemplate("http://host.com")
                    .withUserEventPattern("*")
                    .withSystemEvents(Arrays.asList("connect", "connected"))
                    .withAuth(new UpstreamAuthSettings().withType(UpstreamAuthType.MANAGED_IDENTITY)
                        .withManagedIdentity(new ManagedIdentitySettings().withResource("abc")))))
                .withEventListeners(Arrays.asList(new EventListener()
                    .withFilter(new EventNameFilter().withSystemEvents(Arrays.asList("connected", "disconnected"))
                        .withUserEventPattern("*"))
                    .withEndpoint(new EventHubEndpoint().withFullyQualifiedNamespace("example.servicebus.windows.net")
                        .withEventHubName("eventHubName1"))))
                .withAnonymousConnectPolicy("allow")
                .withWebSocketKeepAliveIntervalInSeconds(50))
            .create();
    }
}
```

### WebPubSubHubs_Delete

```java
/**
 * Samples for WebPubSubHubs Delete.
 */
public final class WebPubSubHubsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/webpubsub/resource-manager/Microsoft.SignalRService/stable/2024-03-01/examples/WebPubSubHubs_Delete
     * .json
     */
    /**
     * Sample code: WebPubSubHubs_Delete.
     * 
     * @param manager Entry point to WebPubSubManager.
     */
    public static void webPubSubHubsDelete(com.azure.resourcemanager.webpubsub.WebPubSubManager manager) {
        manager.webPubSubHubs()
            .delete("exampleHub", "myResourceGroup", "myWebPubSubService", com.azure.core.util.Context.NONE);
    }
}
```

### WebPubSubHubs_Get

```java
/**
 * Samples for WebPubSubHubs Get.
 */
public final class WebPubSubHubsGetSamples {
    /*
     * x-ms-original-file:
     * specification/webpubsub/resource-manager/Microsoft.SignalRService/stable/2024-03-01/examples/WebPubSubHubs_Get.
     * json
     */
    /**
     * Sample code: WebPubSubHubs_Get.
     * 
     * @param manager Entry point to WebPubSubManager.
     */
    public static void webPubSubHubsGet(com.azure.resourcemanager.webpubsub.WebPubSubManager manager) {
        manager.webPubSubHubs()
            .getWithResponse("exampleHub", "myResourceGroup", "myWebPubSubService", com.azure.core.util.Context.NONE);
    }
}
```

### WebPubSubHubs_List

```java
/**
 * Samples for WebPubSubHubs List.
 */
public final class WebPubSubHubsListSamples {
    /*
     * x-ms-original-file:
     * specification/webpubsub/resource-manager/Microsoft.SignalRService/stable/2024-03-01/examples/WebPubSubHubs_List.
     * json
     */
    /**
     * Sample code: WebPubSubHubs_List.
     * 
     * @param manager Entry point to WebPubSubManager.
     */
    public static void webPubSubHubsList(com.azure.resourcemanager.webpubsub.WebPubSubManager manager) {
        manager.webPubSubHubs().list("myResourceGroup", "myWebPubSubService", com.azure.core.util.Context.NONE);
    }
}
```

### WebPubSubPrivateEndpointConnections_Delete

```java
/**
 * Samples for WebPubSubPrivateEndpointConnections Delete.
 */
public final class WebPubSubPrivateEndpointConnectionsDeleteSamples {
    /*
     * x-ms-original-file: specification/webpubsub/resource-manager/Microsoft.SignalRService/stable/2024-03-01/examples/
     * WebPubSubPrivateEndpointConnections_Delete.json
     */
    /**
     * Sample code: WebPubSubPrivateEndpointConnections_Delete.
     * 
     * @param manager Entry point to WebPubSubManager.
     */
    public static void
        webPubSubPrivateEndpointConnectionsDelete(com.azure.resourcemanager.webpubsub.WebPubSubManager manager) {
        manager.webPubSubPrivateEndpointConnections()
            .delete("mywebpubsubservice.1fa229cd-bf3f-47f0-8c49-afb36723997e", "myResourceGroup", "myWebPubSubService",
                com.azure.core.util.Context.NONE);
    }
}
```

### WebPubSubPrivateEndpointConnections_Get

```java
/**
 * Samples for WebPubSubPrivateEndpointConnections Get.
 */
public final class WebPubSubPrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file: specification/webpubsub/resource-manager/Microsoft.SignalRService/stable/2024-03-01/examples/
     * WebPubSubPrivateEndpointConnections_Get.json
     */
    /**
     * Sample code: WebPubSubPrivateEndpointConnections_Get.
     * 
     * @param manager Entry point to WebPubSubManager.
     */
    public static void
        webPubSubPrivateEndpointConnectionsGet(com.azure.resourcemanager.webpubsub.WebPubSubManager manager) {
        manager.webPubSubPrivateEndpointConnections()
            .getWithResponse("mywebpubsubservice.1fa229cd-bf3f-47f0-8c49-afb36723997e", "myResourceGroup",
                "myWebPubSubService", com.azure.core.util.Context.NONE);
    }
}
```

### WebPubSubPrivateEndpointConnections_List

```java
/**
 * Samples for WebPubSubPrivateEndpointConnections List.
 */
public final class WebPubSubPrivateEndpointConnectionsListSamples {
    /*
     * x-ms-original-file: specification/webpubsub/resource-manager/Microsoft.SignalRService/stable/2024-03-01/examples/
     * WebPubSubPrivateEndpointConnections_List.json
     */
    /**
     * Sample code: WebPubSubPrivateEndpointConnections_List.
     * 
     * @param manager Entry point to WebPubSubManager.
     */
    public static void
        webPubSubPrivateEndpointConnectionsList(com.azure.resourcemanager.webpubsub.WebPubSubManager manager) {
        manager.webPubSubPrivateEndpointConnections()
            .list("myResourceGroup", "myWebPubSubService", com.azure.core.util.Context.NONE);
    }
}
```

### WebPubSubPrivateEndpointConnections_Update

```java
import com.azure.resourcemanager.webpubsub.fluent.models.PrivateEndpointConnectionInner;
import com.azure.resourcemanager.webpubsub.models.PrivateEndpoint;
import com.azure.resourcemanager.webpubsub.models.PrivateLinkServiceConnectionState;
import com.azure.resourcemanager.webpubsub.models.PrivateLinkServiceConnectionStatus;

/**
 * Samples for WebPubSubPrivateEndpointConnections Update.
 */
public final class WebPubSubPrivateEndpointConnectionsUpdateSamples {
    /*
     * x-ms-original-file: specification/webpubsub/resource-manager/Microsoft.SignalRService/stable/2024-03-01/examples/
     * WebPubSubPrivateEndpointConnections_Update.json
     */
    /**
     * Sample code: WebPubSubPrivateEndpointConnections_Update.
     * 
     * @param manager Entry point to WebPubSubManager.
     */
    public static void
        webPubSubPrivateEndpointConnectionsUpdate(com.azure.resourcemanager.webpubsub.WebPubSubManager manager) {
        manager.webPubSubPrivateEndpointConnections()
            .updateWithResponse("mywebpubsubservice.1fa229cd-bf3f-47f0-8c49-afb36723997e", "myResourceGroup",
                "myWebPubSubService",
                new PrivateEndpointConnectionInner().withPrivateEndpoint(new PrivateEndpoint())
                    .withPrivateLinkServiceConnectionState(
                        new PrivateLinkServiceConnectionState().withStatus(PrivateLinkServiceConnectionStatus.APPROVED)
                            .withActionsRequired("None")),
                com.azure.core.util.Context.NONE);
    }
}
```

### WebPubSubPrivateLinkResources_List

```java
/**
 * Samples for WebPubSubPrivateLinkResources List.
 */
public final class WebPubSubPrivateLinkResourcesListSamples {
    /*
     * x-ms-original-file: specification/webpubsub/resource-manager/Microsoft.SignalRService/stable/2024-03-01/examples/
     * WebPubSubPrivateLinkResources_List.json
     */
    /**
     * Sample code: WebPubSubPrivateLinkResources_List.
     * 
     * @param manager Entry point to WebPubSubManager.
     */
    public static void webPubSubPrivateLinkResourcesList(com.azure.resourcemanager.webpubsub.WebPubSubManager manager) {
        manager.webPubSubPrivateLinkResources()
            .list("myResourceGroup", "myWebPubSubService", com.azure.core.util.Context.NONE);
    }
}
```

### WebPubSubReplicaSharedPrivateLinkResources_CreateOrUpdate

```java
/**
 * Samples for WebPubSubReplicaSharedPrivateLinkResources CreateOrUpdate.
 */
public final class WebPubSubReplicaSharedPrivateLinkResourcesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/webpubsub/resource-manager/Microsoft.SignalRService/stable/2024-03-01/examples/
     * WebPubSubReplicaSharedPrivateLinkResources_CreateOrUpdate.json
     */
    /**
     * Sample code: WebPubSubReplicaSharedPrivateLinkResources_CreateOrUpdate.
     * 
     * @param manager Entry point to WebPubSubManager.
     */
    public static void webPubSubReplicaSharedPrivateLinkResourcesCreateOrUpdate(
        com.azure.resourcemanager.webpubsub.WebPubSubManager manager) {
        manager.webPubSubReplicaSharedPrivateLinkResources()
            .define("upstream")
            .withExistingReplica("myResourceGroup", "myWebPubSubService", "myWebPubSubService-eastus")
            .withGroupId("sites")
            .withPrivateLinkResourceId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/myResourceGroup/providers/Microsoft.Web/sites/myWebApp")
            .withRequestMessage("Please approve")
            .create();
    }
}
```

### WebPubSubReplicaSharedPrivateLinkResources_Get

```java
/**
 * Samples for WebPubSubReplicaSharedPrivateLinkResources Get.
 */
public final class WebPubSubReplicaSharedPrivateLinkResourcesGetSamples {
    /*
     * x-ms-original-file: specification/webpubsub/resource-manager/Microsoft.SignalRService/stable/2024-03-01/examples/
     * WebPubSubReplicaSharedPrivateLinkResources_Get.json
     */
    /**
     * Sample code: WebPubSubReplicaSharedPrivateLinkResources_Get.
     * 
     * @param manager Entry point to WebPubSubManager.
     */
    public static void
        webPubSubReplicaSharedPrivateLinkResourcesGet(com.azure.resourcemanager.webpubsub.WebPubSubManager manager) {
        manager.webPubSubReplicaSharedPrivateLinkResources()
            .getWithResponse("myResourceGroup", "myWebPubSubService", "myWebPubSubService-eastus", "upstream",
                com.azure.core.util.Context.NONE);
    }
}
```

### WebPubSubReplicaSharedPrivateLinkResources_List

```java
/**
 * Samples for WebPubSubReplicaSharedPrivateLinkResources List.
 */
public final class WebPubSubReplicaSharedPrivateLinkResourcesListSamples {
    /*
     * x-ms-original-file: specification/webpubsub/resource-manager/Microsoft.SignalRService/stable/2024-03-01/examples/
     * WebPubSubReplicaSharedPrivateLinkResources_List.json
     */
    /**
     * Sample code: WebPubSubReplicaSharedPrivateLinkResources_List.
     * 
     * @param manager Entry point to WebPubSubManager.
     */
    public static void
        webPubSubReplicaSharedPrivateLinkResourcesList(com.azure.resourcemanager.webpubsub.WebPubSubManager manager) {
        manager.webPubSubReplicaSharedPrivateLinkResources()
            .list("myResourceGroup", "myWebPubSubService", "myWebPubSubService-eastus",
                com.azure.core.util.Context.NONE);
    }
}
```

### WebPubSubReplicas_CreateOrUpdate

```java
import com.azure.resourcemanager.webpubsub.models.ResourceSku;
import com.azure.resourcemanager.webpubsub.models.WebPubSubSkuTier;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for WebPubSubReplicas CreateOrUpdate.
 */
public final class WebPubSubReplicasCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/webpubsub/resource-manager/Microsoft.SignalRService/stable/2024-03-01/examples/
     * WebPubSubReplicas_CreateOrUpdate.json
     */
    /**
     * Sample code: WebPubSubReplicas_CreateOrUpdate.
     * 
     * @param manager Entry point to WebPubSubManager.
     */
    public static void webPubSubReplicasCreateOrUpdate(com.azure.resourcemanager.webpubsub.WebPubSubManager manager) {
        manager.webPubSubReplicas()
            .define("myWebPubSubService-eastus")
            .withRegion("eastus")
            .withExistingWebPubSub("myResourceGroup", "myWebPubSubService")
            .withTags(mapOf("key1", "fakeTokenPlaceholder"))
            .withSku(new ResourceSku().withName("Premium_P1").withTier(WebPubSubSkuTier.PREMIUM).withCapacity(1))
            .withResourceStopped("false")
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

### WebPubSubReplicas_Delete

```java
/**
 * Samples for WebPubSubReplicas Delete.
 */
public final class WebPubSubReplicasDeleteSamples {
    /*
     * x-ms-original-file: specification/webpubsub/resource-manager/Microsoft.SignalRService/stable/2024-03-01/examples/
     * WebPubSubReplicas_Delete.json
     */
    /**
     * Sample code: WebPubSubReplicas_Delete.
     * 
     * @param manager Entry point to WebPubSubManager.
     */
    public static void webPubSubReplicasDelete(com.azure.resourcemanager.webpubsub.WebPubSubManager manager) {
        manager.webPubSubReplicas()
            .deleteWithResponse("myResourceGroup", "myWebPubSubService", "myWebPubSubService-eastus",
                com.azure.core.util.Context.NONE);
    }
}
```

### WebPubSubReplicas_Get

```java
/**
 * Samples for WebPubSubReplicas Get.
 */
public final class WebPubSubReplicasGetSamples {
    /*
     * x-ms-original-file: specification/webpubsub/resource-manager/Microsoft.SignalRService/stable/2024-03-01/examples/
     * WebPubSubReplicas_Get.json
     */
    /**
     * Sample code: WebPubSubReplicas_Get.
     * 
     * @param manager Entry point to WebPubSubManager.
     */
    public static void webPubSubReplicasGet(com.azure.resourcemanager.webpubsub.WebPubSubManager manager) {
        manager.webPubSubReplicas()
            .getWithResponse("myResourceGroup", "myWebPubSubService", "myWebPubSubService-eastus",
                com.azure.core.util.Context.NONE);
    }
}
```

### WebPubSubReplicas_List

```java
/**
 * Samples for WebPubSubReplicas List.
 */
public final class WebPubSubReplicasListSamples {
    /*
     * x-ms-original-file: specification/webpubsub/resource-manager/Microsoft.SignalRService/stable/2024-03-01/examples/
     * WebPubSubReplicas_List.json
     */
    /**
     * Sample code: WebPubSubReplicas_List.
     * 
     * @param manager Entry point to WebPubSubManager.
     */
    public static void webPubSubReplicasList(com.azure.resourcemanager.webpubsub.WebPubSubManager manager) {
        manager.webPubSubReplicas().list("myResourceGroup", "myWebPubSubService", com.azure.core.util.Context.NONE);
    }
}
```

### WebPubSubReplicas_Restart

```java
/**
 * Samples for WebPubSubReplicas Restart.
 */
public final class WebPubSubReplicasRestartSamples {
    /*
     * x-ms-original-file: specification/webpubsub/resource-manager/Microsoft.SignalRService/stable/2024-03-01/examples/
     * WebPubSubReplicas_Restart.json
     */
    /**
     * Sample code: WebPubSubReplicas_Restart.
     * 
     * @param manager Entry point to WebPubSubManager.
     */
    public static void webPubSubReplicasRestart(com.azure.resourcemanager.webpubsub.WebPubSubManager manager) {
        manager.webPubSubReplicas()
            .restart("myResourceGroup", "myWebPubSubService", "myWebPubSubService-eastus",
                com.azure.core.util.Context.NONE);
    }
}
```

### WebPubSubReplicas_Update

```java
import com.azure.resourcemanager.webpubsub.models.Replica;
import com.azure.resourcemanager.webpubsub.models.ResourceSku;
import com.azure.resourcemanager.webpubsub.models.WebPubSubSkuTier;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for WebPubSubReplicas Update.
 */
public final class WebPubSubReplicasUpdateSamples {
    /*
     * x-ms-original-file: specification/webpubsub/resource-manager/Microsoft.SignalRService/stable/2024-03-01/examples/
     * WebPubSubReplicas_Update.json
     */
    /**
     * Sample code: WebPubSubReplicas_Update.
     * 
     * @param manager Entry point to WebPubSubManager.
     */
    public static void webPubSubReplicasUpdate(com.azure.resourcemanager.webpubsub.WebPubSubManager manager) {
        Replica resource = manager.webPubSubReplicas()
            .getWithResponse("myResourceGroup", "myWebPubSubService", "myWebPubSubService-eastus",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key1", "fakeTokenPlaceholder"))
            .withSku(new ResourceSku().withName("Premium_P1").withTier(WebPubSubSkuTier.PREMIUM).withCapacity(1))
            .withResourceStopped("false")
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

### WebPubSubSharedPrivateLinkResources_CreateOrUpdate

```java
import com.azure.resourcemanager.webpubsub.fluent.models.SharedPrivateLinkResourceInner;

/**
 * Samples for WebPubSubSharedPrivateLinkResources CreateOrUpdate.
 */
public final class WebPubSubSharedPrivateLinkResourcesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/webpubsub/resource-manager/Microsoft.SignalRService/stable/2024-03-01/examples/
     * WebPubSubSharedPrivateLinkResources_CreateOrUpdate.json
     */
    /**
     * Sample code: WebPubSubSharedPrivateLinkResources_CreateOrUpdate.
     * 
     * @param manager Entry point to WebPubSubManager.
     */
    public static void webPubSubSharedPrivateLinkResourcesCreateOrUpdate(
        com.azure.resourcemanager.webpubsub.WebPubSubManager manager) {
        manager.webPubSubSharedPrivateLinkResources()
            .createOrUpdate("upstream", "myResourceGroup", "myWebPubSubService", new SharedPrivateLinkResourceInner()
                .withGroupId("sites")
                .withPrivateLinkResourceId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/myResourceGroup/providers/Microsoft.Web/sites/myWebApp")
                .withRequestMessage("Please approve"), com.azure.core.util.Context.NONE);
    }
}
```

### WebPubSubSharedPrivateLinkResources_Delete

```java
/**
 * Samples for WebPubSubSharedPrivateLinkResources Delete.
 */
public final class WebPubSubSharedPrivateLinkResourcesDeleteSamples {
    /*
     * x-ms-original-file: specification/webpubsub/resource-manager/Microsoft.SignalRService/stable/2024-03-01/examples/
     * WebPubSubSharedPrivateLinkResources_Delete.json
     */
    /**
     * Sample code: WebPubSubSharedPrivateLinkResources_Delete.
     * 
     * @param manager Entry point to WebPubSubManager.
     */
    public static void
        webPubSubSharedPrivateLinkResourcesDelete(com.azure.resourcemanager.webpubsub.WebPubSubManager manager) {
        manager.webPubSubSharedPrivateLinkResources()
            .delete("upstream", "myResourceGroup", "myWebPubSubService", com.azure.core.util.Context.NONE);
    }
}
```

### WebPubSubSharedPrivateLinkResources_Get

```java
/**
 * Samples for WebPubSubSharedPrivateLinkResources Get.
 */
public final class WebPubSubSharedPrivateLinkResourcesGetSamples {
    /*
     * x-ms-original-file: specification/webpubsub/resource-manager/Microsoft.SignalRService/stable/2024-03-01/examples/
     * WebPubSubSharedPrivateLinkResources_Get.json
     */
    /**
     * Sample code: WebPubSubSharedPrivateLinkResources_Get.
     * 
     * @param manager Entry point to WebPubSubManager.
     */
    public static void
        webPubSubSharedPrivateLinkResourcesGet(com.azure.resourcemanager.webpubsub.WebPubSubManager manager) {
        manager.webPubSubSharedPrivateLinkResources()
            .getWithResponse("upstream", "myResourceGroup", "myWebPubSubService", com.azure.core.util.Context.NONE);
    }
}
```

### WebPubSubSharedPrivateLinkResources_List

```java
/**
 * Samples for WebPubSubSharedPrivateLinkResources List.
 */
public final class WebPubSubSharedPrivateLinkResourcesListSamples {
    /*
     * x-ms-original-file: specification/webpubsub/resource-manager/Microsoft.SignalRService/stable/2024-03-01/examples/
     * WebPubSubSharedPrivateLinkResources_List.json
     */
    /**
     * Sample code: WebPubSubSharedPrivateLinkResources_List.
     * 
     * @param manager Entry point to WebPubSubManager.
     */
    public static void
        webPubSubSharedPrivateLinkResourcesList(com.azure.resourcemanager.webpubsub.WebPubSubManager manager) {
        manager.webPubSubSharedPrivateLinkResources()
            .list("myResourceGroup", "myWebPubSubService", com.azure.core.util.Context.NONE);
    }
}
```

