# Code snippets and samples


## Operations

- [List](#operations_list)

## SignalR

- [CheckNameAvailability](#signalr_checknameavailability)
- [CreateOrUpdate](#signalr_createorupdate)
- [Delete](#signalr_delete)
- [GetByResourceGroup](#signalr_getbyresourcegroup)
- [List](#signalr_list)
- [ListByResourceGroup](#signalr_listbyresourcegroup)
- [ListKeys](#signalr_listkeys)
- [ListSkus](#signalr_listskus)
- [RegenerateKey](#signalr_regeneratekey)
- [Restart](#signalr_restart)
- [Update](#signalr_update)

## SignalRCustomCertificates

- [CreateOrUpdate](#signalrcustomcertificates_createorupdate)
- [Delete](#signalrcustomcertificates_delete)
- [Get](#signalrcustomcertificates_get)
- [List](#signalrcustomcertificates_list)

## SignalRCustomDomains

- [CreateOrUpdate](#signalrcustomdomains_createorupdate)
- [Delete](#signalrcustomdomains_delete)
- [Get](#signalrcustomdomains_get)
- [List](#signalrcustomdomains_list)

## SignalRPrivateEndpointConnections

- [Delete](#signalrprivateendpointconnections_delete)
- [Get](#signalrprivateendpointconnections_get)
- [List](#signalrprivateendpointconnections_list)
- [Update](#signalrprivateendpointconnections_update)

## SignalRPrivateLinkResources

- [List](#signalrprivatelinkresources_list)

## SignalRSharedPrivateLinkResources

- [CreateOrUpdate](#signalrsharedprivatelinkresources_createorupdate)
- [Delete](#signalrsharedprivatelinkresources_delete)
- [Get](#signalrsharedprivatelinkresources_get)
- [List](#signalrsharedprivatelinkresources_list)

## Usages

- [List](#usages_list)
### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/signalr/resource-manager/Microsoft.SignalRService/preview/2022-08-01-preview/examples/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     *
     * @param manager Entry point to SignalRManager.
     */
    public static void operationsList(com.azure.resourcemanager.signalr.SignalRManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### SignalR_CheckNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.signalr.models.NameAvailabilityParameters;

/** Samples for SignalR CheckNameAvailability. */
public final class SignalRCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/signalr/resource-manager/Microsoft.SignalRService/preview/2022-08-01-preview/examples/SignalR_CheckNameAvailability.json
     */
    /**
     * Sample code: SignalR_CheckNameAvailability.
     *
     * @param manager Entry point to SignalRManager.
     */
    public static void signalRCheckNameAvailability(com.azure.resourcemanager.signalr.SignalRManager manager) {
        manager
            .signalRs()
            .checkNameAvailabilityWithResponse(
                "eastus",
                new NameAvailabilityParameters()
                    .withType("Microsoft.SignalRService/SignalR")
                    .withName("mySignalRService"),
                Context.NONE);
    }
}
```

### SignalR_CreateOrUpdate

```java
import com.azure.resourcemanager.signalr.models.AclAction;
import com.azure.resourcemanager.signalr.models.FeatureFlags;
import com.azure.resourcemanager.signalr.models.LiveTraceCategory;
import com.azure.resourcemanager.signalr.models.LiveTraceConfiguration;
import com.azure.resourcemanager.signalr.models.ManagedIdentity;
import com.azure.resourcemanager.signalr.models.ManagedIdentitySettings;
import com.azure.resourcemanager.signalr.models.ManagedIdentityType;
import com.azure.resourcemanager.signalr.models.NetworkAcl;
import com.azure.resourcemanager.signalr.models.PrivateEndpointAcl;
import com.azure.resourcemanager.signalr.models.ResourceSku;
import com.azure.resourcemanager.signalr.models.ServerlessSettings;
import com.azure.resourcemanager.signalr.models.ServerlessUpstreamSettings;
import com.azure.resourcemanager.signalr.models.ServiceKind;
import com.azure.resourcemanager.signalr.models.SignalRCorsSettings;
import com.azure.resourcemanager.signalr.models.SignalRFeature;
import com.azure.resourcemanager.signalr.models.SignalRNetworkACLs;
import com.azure.resourcemanager.signalr.models.SignalRRequestType;
import com.azure.resourcemanager.signalr.models.SignalRSkuTier;
import com.azure.resourcemanager.signalr.models.SignalRTlsSettings;
import com.azure.resourcemanager.signalr.models.UpstreamAuthSettings;
import com.azure.resourcemanager.signalr.models.UpstreamAuthType;
import com.azure.resourcemanager.signalr.models.UpstreamTemplate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for SignalR CreateOrUpdate. */
public final class SignalRCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/signalr/resource-manager/Microsoft.SignalRService/preview/2022-08-01-preview/examples/SignalR_CreateOrUpdate.json
     */
    /**
     * Sample code: SignalR_CreateOrUpdate.
     *
     * @param manager Entry point to SignalRManager.
     */
    public static void signalRCreateOrUpdate(com.azure.resourcemanager.signalr.SignalRManager manager) {
        manager
            .signalRs()
            .define("mySignalRService")
            .withRegion("eastus")
            .withExistingResourceGroup("myResourceGroup")
            .withTags(mapOf("key1", "value1"))
            .withSku(new ResourceSku().withName("Standard_S1").withTier(SignalRSkuTier.STANDARD).withCapacity(1))
            .withKind(ServiceKind.SIGNALR)
            .withIdentity(new ManagedIdentity().withType(ManagedIdentityType.SYSTEM_ASSIGNED))
            .withTls(new SignalRTlsSettings().withClientCertEnabled(false))
            .withFeatures(
                Arrays
                    .asList(
                        new SignalRFeature()
                            .withFlag(FeatureFlags.SERVICE_MODE)
                            .withValue("Serverless")
                            .withProperties(mapOf()),
                        new SignalRFeature()
                            .withFlag(FeatureFlags.ENABLE_CONNECTIVITY_LOGS)
                            .withValue("True")
                            .withProperties(mapOf()),
                        new SignalRFeature()
                            .withFlag(FeatureFlags.ENABLE_MESSAGING_LOGS)
                            .withValue("False")
                            .withProperties(mapOf()),
                        new SignalRFeature()
                            .withFlag(FeatureFlags.ENABLE_LIVE_TRACE)
                            .withValue("False")
                            .withProperties(mapOf())))
            .withLiveTraceConfiguration(
                new LiveTraceConfiguration()
                    .withEnabled("false")
                    .withCategories(
                        Arrays.asList(new LiveTraceCategory().withName("ConnectivityLogs").withEnabled("true"))))
            .withCors(new SignalRCorsSettings().withAllowedOrigins(Arrays.asList("https://foo.com", "https://bar.com")))
            .withServerless(new ServerlessSettings().withConnectionTimeoutInSeconds(5))
            .withUpstream(
                new ServerlessUpstreamSettings()
                    .withTemplates(
                        Arrays
                            .asList(
                                new UpstreamTemplate()
                                    .withHubPattern("*")
                                    .withEventPattern("connect,disconnect")
                                    .withCategoryPattern("*")
                                    .withUrlTemplate("https://example.com/chat/api/connect")
                                    .withAuth(
                                        new UpstreamAuthSettings()
                                            .withType(UpstreamAuthType.MANAGED_IDENTITY)
                                            .withManagedIdentity(
                                                new ManagedIdentitySettings().withResource("api://example"))))))
            .withNetworkACLs(
                new SignalRNetworkACLs()
                    .withDefaultAction(AclAction.DENY)
                    .withPublicNetwork(new NetworkAcl().withAllow(Arrays.asList(SignalRRequestType.CLIENT_CONNECTION)))
                    .withPrivateEndpoints(
                        Arrays
                            .asList(
                                new PrivateEndpointAcl()
                                    .withAllow(Arrays.asList(SignalRRequestType.SERVER_CONNECTION))
                                    .withName("mysignalrservice.1fa229cd-bf3f-47f0-8c49-afb36723997e"))))
            .withPublicNetworkAccess("Enabled")
            .withDisableLocalAuth(false)
            .withDisableAadAuth(false)
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

### SignalR_Delete

```java
import com.azure.core.util.Context;

/** Samples for SignalR Delete. */
public final class SignalRDeleteSamples {
    /*
     * x-ms-original-file: specification/signalr/resource-manager/Microsoft.SignalRService/preview/2022-08-01-preview/examples/SignalR_Delete.json
     */
    /**
     * Sample code: SignalR_Delete.
     *
     * @param manager Entry point to SignalRManager.
     */
    public static void signalRDelete(com.azure.resourcemanager.signalr.SignalRManager manager) {
        manager.signalRs().delete("myResourceGroup", "mySignalRService", Context.NONE);
    }
}
```

### SignalR_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for SignalR GetByResourceGroup. */
public final class SignalRGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/signalr/resource-manager/Microsoft.SignalRService/preview/2022-08-01-preview/examples/SignalR_Get.json
     */
    /**
     * Sample code: SignalR_Get.
     *
     * @param manager Entry point to SignalRManager.
     */
    public static void signalRGet(com.azure.resourcemanager.signalr.SignalRManager manager) {
        manager.signalRs().getByResourceGroupWithResponse("myResourceGroup", "mySignalRService", Context.NONE);
    }
}
```

### SignalR_List

```java
import com.azure.core.util.Context;

/** Samples for SignalR List. */
public final class SignalRListSamples {
    /*
     * x-ms-original-file: specification/signalr/resource-manager/Microsoft.SignalRService/preview/2022-08-01-preview/examples/SignalR_ListBySubscription.json
     */
    /**
     * Sample code: SignalR_ListBySubscription.
     *
     * @param manager Entry point to SignalRManager.
     */
    public static void signalRListBySubscription(com.azure.resourcemanager.signalr.SignalRManager manager) {
        manager.signalRs().list(Context.NONE);
    }
}
```

### SignalR_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for SignalR ListByResourceGroup. */
public final class SignalRListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/signalr/resource-manager/Microsoft.SignalRService/preview/2022-08-01-preview/examples/SignalR_ListByResourceGroup.json
     */
    /**
     * Sample code: SignalR_ListByResourceGroup.
     *
     * @param manager Entry point to SignalRManager.
     */
    public static void signalRListByResourceGroup(com.azure.resourcemanager.signalr.SignalRManager manager) {
        manager.signalRs().listByResourceGroup("myResourceGroup", Context.NONE);
    }
}
```

### SignalR_ListKeys

```java
import com.azure.core.util.Context;

/** Samples for SignalR ListKeys. */
public final class SignalRListKeysSamples {
    /*
     * x-ms-original-file: specification/signalr/resource-manager/Microsoft.SignalRService/preview/2022-08-01-preview/examples/SignalR_ListKeys.json
     */
    /**
     * Sample code: SignalR_ListKeys.
     *
     * @param manager Entry point to SignalRManager.
     */
    public static void signalRListKeys(com.azure.resourcemanager.signalr.SignalRManager manager) {
        manager.signalRs().listKeysWithResponse("myResourceGroup", "mySignalRService", Context.NONE);
    }
}
```

### SignalR_ListSkus

```java
import com.azure.core.util.Context;

/** Samples for SignalR ListSkus. */
public final class SignalRListSkusSamples {
    /*
     * x-ms-original-file: specification/signalr/resource-manager/Microsoft.SignalRService/preview/2022-08-01-preview/examples/SignalR_ListSkus.json
     */
    /**
     * Sample code: SignalR_ListSkus.
     *
     * @param manager Entry point to SignalRManager.
     */
    public static void signalRListSkus(com.azure.resourcemanager.signalr.SignalRManager manager) {
        manager.signalRs().listSkusWithResponse("myResourceGroup", "mySignalRService", Context.NONE);
    }
}
```

### SignalR_RegenerateKey

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.signalr.models.KeyType;
import com.azure.resourcemanager.signalr.models.RegenerateKeyParameters;

/** Samples for SignalR RegenerateKey. */
public final class SignalRRegenerateKeySamples {
    /*
     * x-ms-original-file: specification/signalr/resource-manager/Microsoft.SignalRService/preview/2022-08-01-preview/examples/SignalR_RegenerateKey.json
     */
    /**
     * Sample code: SignalR_RegenerateKey.
     *
     * @param manager Entry point to SignalRManager.
     */
    public static void signalRRegenerateKey(com.azure.resourcemanager.signalr.SignalRManager manager) {
        manager
            .signalRs()
            .regenerateKey(
                "myResourceGroup",
                "mySignalRService",
                new RegenerateKeyParameters().withKeyType(KeyType.PRIMARY),
                Context.NONE);
    }
}
```

### SignalR_Restart

```java
import com.azure.core.util.Context;

/** Samples for SignalR Restart. */
public final class SignalRRestartSamples {
    /*
     * x-ms-original-file: specification/signalr/resource-manager/Microsoft.SignalRService/preview/2022-08-01-preview/examples/SignalR_Restart.json
     */
    /**
     * Sample code: SignalR_Restart.
     *
     * @param manager Entry point to SignalRManager.
     */
    public static void signalRRestart(com.azure.resourcemanager.signalr.SignalRManager manager) {
        manager.signalRs().restart("myResourceGroup", "mySignalRService", Context.NONE);
    }
}
```

### SignalR_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.signalr.models.AclAction;
import com.azure.resourcemanager.signalr.models.FeatureFlags;
import com.azure.resourcemanager.signalr.models.LiveTraceCategory;
import com.azure.resourcemanager.signalr.models.LiveTraceConfiguration;
import com.azure.resourcemanager.signalr.models.ManagedIdentity;
import com.azure.resourcemanager.signalr.models.ManagedIdentitySettings;
import com.azure.resourcemanager.signalr.models.ManagedIdentityType;
import com.azure.resourcemanager.signalr.models.NetworkAcl;
import com.azure.resourcemanager.signalr.models.PrivateEndpointAcl;
import com.azure.resourcemanager.signalr.models.ResourceSku;
import com.azure.resourcemanager.signalr.models.ServerlessSettings;
import com.azure.resourcemanager.signalr.models.ServerlessUpstreamSettings;
import com.azure.resourcemanager.signalr.models.SignalRCorsSettings;
import com.azure.resourcemanager.signalr.models.SignalRFeature;
import com.azure.resourcemanager.signalr.models.SignalRNetworkACLs;
import com.azure.resourcemanager.signalr.models.SignalRRequestType;
import com.azure.resourcemanager.signalr.models.SignalRResource;
import com.azure.resourcemanager.signalr.models.SignalRSkuTier;
import com.azure.resourcemanager.signalr.models.SignalRTlsSettings;
import com.azure.resourcemanager.signalr.models.UpstreamAuthSettings;
import com.azure.resourcemanager.signalr.models.UpstreamAuthType;
import com.azure.resourcemanager.signalr.models.UpstreamTemplate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for SignalR Update. */
public final class SignalRUpdateSamples {
    /*
     * x-ms-original-file: specification/signalr/resource-manager/Microsoft.SignalRService/preview/2022-08-01-preview/examples/SignalR_Update.json
     */
    /**
     * Sample code: SignalR_Update.
     *
     * @param manager Entry point to SignalRManager.
     */
    public static void signalRUpdate(com.azure.resourcemanager.signalr.SignalRManager manager) {
        SignalRResource resource =
            manager
                .signalRs()
                .getByResourceGroupWithResponse("myResourceGroup", "mySignalRService", Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("key1", "value1"))
            .withSku(new ResourceSku().withName("Standard_S1").withTier(SignalRSkuTier.STANDARD).withCapacity(1))
            .withIdentity(new ManagedIdentity().withType(ManagedIdentityType.SYSTEM_ASSIGNED))
            .withTls(new SignalRTlsSettings().withClientCertEnabled(false))
            .withFeatures(
                Arrays
                    .asList(
                        new SignalRFeature()
                            .withFlag(FeatureFlags.SERVICE_MODE)
                            .withValue("Serverless")
                            .withProperties(mapOf()),
                        new SignalRFeature()
                            .withFlag(FeatureFlags.ENABLE_CONNECTIVITY_LOGS)
                            .withValue("True")
                            .withProperties(mapOf()),
                        new SignalRFeature()
                            .withFlag(FeatureFlags.ENABLE_MESSAGING_LOGS)
                            .withValue("False")
                            .withProperties(mapOf()),
                        new SignalRFeature()
                            .withFlag(FeatureFlags.ENABLE_LIVE_TRACE)
                            .withValue("False")
                            .withProperties(mapOf())))
            .withLiveTraceConfiguration(
                new LiveTraceConfiguration()
                    .withEnabled("false")
                    .withCategories(
                        Arrays.asList(new LiveTraceCategory().withName("ConnectivityLogs").withEnabled("true"))))
            .withCors(new SignalRCorsSettings().withAllowedOrigins(Arrays.asList("https://foo.com", "https://bar.com")))
            .withServerless(new ServerlessSettings().withConnectionTimeoutInSeconds(5))
            .withUpstream(
                new ServerlessUpstreamSettings()
                    .withTemplates(
                        Arrays
                            .asList(
                                new UpstreamTemplate()
                                    .withHubPattern("*")
                                    .withEventPattern("connect,disconnect")
                                    .withCategoryPattern("*")
                                    .withUrlTemplate("https://example.com/chat/api/connect")
                                    .withAuth(
                                        new UpstreamAuthSettings()
                                            .withType(UpstreamAuthType.MANAGED_IDENTITY)
                                            .withManagedIdentity(
                                                new ManagedIdentitySettings().withResource("api://example"))))))
            .withNetworkACLs(
                new SignalRNetworkACLs()
                    .withDefaultAction(AclAction.DENY)
                    .withPublicNetwork(new NetworkAcl().withAllow(Arrays.asList(SignalRRequestType.CLIENT_CONNECTION)))
                    .withPrivateEndpoints(
                        Arrays
                            .asList(
                                new PrivateEndpointAcl()
                                    .withAllow(Arrays.asList(SignalRRequestType.SERVER_CONNECTION))
                                    .withName("mysignalrservice.1fa229cd-bf3f-47f0-8c49-afb36723997e"))))
            .withPublicNetworkAccess("Enabled")
            .withDisableLocalAuth(false)
            .withDisableAadAuth(false)
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

### SignalRCustomCertificates_CreateOrUpdate

```java
/** Samples for SignalRCustomCertificates CreateOrUpdate. */
public final class SignalRCustomCertificatesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/signalr/resource-manager/Microsoft.SignalRService/preview/2022-08-01-preview/examples/SignalRCustomCertificates_CreateOrUpdate.json
     */
    /**
     * Sample code: SignalRCustomCertificates_CreateOrUpdate.
     *
     * @param manager Entry point to SignalRManager.
     */
    public static void signalRCustomCertificatesCreateOrUpdate(
        com.azure.resourcemanager.signalr.SignalRManager manager) {
        manager
            .signalRCustomCertificates()
            .define("myCert")
            .withExistingSignalR("myResourceGroup", "mySignalRService")
            .withKeyVaultBaseUri("https://myvault.keyvault.azure.net/")
            .withKeyVaultSecretName("mycert")
            .withKeyVaultSecretVersion("bb6a44b2743f47f68dad0d6cc9756432")
            .create();
    }
}
```

### SignalRCustomCertificates_Delete

```java
import com.azure.core.util.Context;

/** Samples for SignalRCustomCertificates Delete. */
public final class SignalRCustomCertificatesDeleteSamples {
    /*
     * x-ms-original-file: specification/signalr/resource-manager/Microsoft.SignalRService/preview/2022-08-01-preview/examples/SignalRCustomCertificates_Delete.json
     */
    /**
     * Sample code: SignalRCustomCertificates_Delete.
     *
     * @param manager Entry point to SignalRManager.
     */
    public static void signalRCustomCertificatesDelete(com.azure.resourcemanager.signalr.SignalRManager manager) {
        manager
            .signalRCustomCertificates()
            .deleteWithResponse("myResourceGroup", "mySignalRService", "myCert", Context.NONE);
    }
}
```

### SignalRCustomCertificates_Get

```java
import com.azure.core.util.Context;

/** Samples for SignalRCustomCertificates Get. */
public final class SignalRCustomCertificatesGetSamples {
    /*
     * x-ms-original-file: specification/signalr/resource-manager/Microsoft.SignalRService/preview/2022-08-01-preview/examples/SignalRCustomCertificates_Get.json
     */
    /**
     * Sample code: SignalRCustomCertificates_Get.
     *
     * @param manager Entry point to SignalRManager.
     */
    public static void signalRCustomCertificatesGet(com.azure.resourcemanager.signalr.SignalRManager manager) {
        manager
            .signalRCustomCertificates()
            .getWithResponse("myResourceGroup", "mySignalRService", "myCert", Context.NONE);
    }
}
```

### SignalRCustomCertificates_List

```java
import com.azure.core.util.Context;

/** Samples for SignalRCustomCertificates List. */
public final class SignalRCustomCertificatesListSamples {
    /*
     * x-ms-original-file: specification/signalr/resource-manager/Microsoft.SignalRService/preview/2022-08-01-preview/examples/SignalRCustomCertificates_List.json
     */
    /**
     * Sample code: SignalRCustomCertificates_List.
     *
     * @param manager Entry point to SignalRManager.
     */
    public static void signalRCustomCertificatesList(com.azure.resourcemanager.signalr.SignalRManager manager) {
        manager.signalRCustomCertificates().list("myResourceGroup", "mySignalRService", Context.NONE);
    }
}
```

### SignalRCustomDomains_CreateOrUpdate

```java
import com.azure.resourcemanager.signalr.models.ResourceReference;

/** Samples for SignalRCustomDomains CreateOrUpdate. */
public final class SignalRCustomDomainsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/signalr/resource-manager/Microsoft.SignalRService/preview/2022-08-01-preview/examples/SignalRCustomDomains_CreateOrUpdate.json
     */
    /**
     * Sample code: SignalRCustomDomains_CreateOrUpdate.
     *
     * @param manager Entry point to SignalRManager.
     */
    public static void signalRCustomDomainsCreateOrUpdate(com.azure.resourcemanager.signalr.SignalRManager manager) {
        manager
            .signalRCustomDomains()
            .define("myDomain")
            .withExistingSignalR("myResourceGroup", "mySignalRService")
            .withDomainName("example.com")
            .withCustomCertificate(
                new ResourceReference()
                    .withId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/myResourceGroup/providers/Microsoft.SignalRService/SignalR/mySignalRService/customCertificates/myCert"))
            .create();
    }
}
```

### SignalRCustomDomains_Delete

```java
import com.azure.core.util.Context;

/** Samples for SignalRCustomDomains Delete. */
public final class SignalRCustomDomainsDeleteSamples {
    /*
     * x-ms-original-file: specification/signalr/resource-manager/Microsoft.SignalRService/preview/2022-08-01-preview/examples/SignalRCustomDomains_Delete.json
     */
    /**
     * Sample code: SignalRCustomDomains_Delete.
     *
     * @param manager Entry point to SignalRManager.
     */
    public static void signalRCustomDomainsDelete(com.azure.resourcemanager.signalr.SignalRManager manager) {
        manager.signalRCustomDomains().delete("myResourceGroup", "mySignalRService", "example", Context.NONE);
    }
}
```

### SignalRCustomDomains_Get

```java
import com.azure.core.util.Context;

/** Samples for SignalRCustomDomains Get. */
public final class SignalRCustomDomainsGetSamples {
    /*
     * x-ms-original-file: specification/signalr/resource-manager/Microsoft.SignalRService/preview/2022-08-01-preview/examples/SignalRCustomDomains_Get.json
     */
    /**
     * Sample code: SignalRCustomDomains_Get.
     *
     * @param manager Entry point to SignalRManager.
     */
    public static void signalRCustomDomainsGet(com.azure.resourcemanager.signalr.SignalRManager manager) {
        manager.signalRCustomDomains().getWithResponse("myResourceGroup", "mySignalRService", "example", Context.NONE);
    }
}
```

### SignalRCustomDomains_List

```java
import com.azure.core.util.Context;

/** Samples for SignalRCustomDomains List. */
public final class SignalRCustomDomainsListSamples {
    /*
     * x-ms-original-file: specification/signalr/resource-manager/Microsoft.SignalRService/preview/2022-08-01-preview/examples/SignalRCustomDomains_List.json
     */
    /**
     * Sample code: SignalRCustomDomains_List.
     *
     * @param manager Entry point to SignalRManager.
     */
    public static void signalRCustomDomainsList(com.azure.resourcemanager.signalr.SignalRManager manager) {
        manager.signalRCustomDomains().list("myResourceGroup", "mySignalRService", Context.NONE);
    }
}
```

### SignalRPrivateEndpointConnections_Delete

```java
import com.azure.core.util.Context;

/** Samples for SignalRPrivateEndpointConnections Delete. */
public final class SignalRPrivateEndpointConnectionsDeleteSamples {
    /*
     * x-ms-original-file: specification/signalr/resource-manager/Microsoft.SignalRService/preview/2022-08-01-preview/examples/SignalRPrivateEndpointConnections_Delete.json
     */
    /**
     * Sample code: SignalRPrivateEndpointConnections_Delete.
     *
     * @param manager Entry point to SignalRManager.
     */
    public static void signalRPrivateEndpointConnectionsDelete(
        com.azure.resourcemanager.signalr.SignalRManager manager) {
        manager
            .signalRPrivateEndpointConnections()
            .delete(
                "mysignalrservice.1fa229cd-bf3f-47f0-8c49-afb36723997e",
                "myResourceGroup",
                "mySignalRService",
                Context.NONE);
    }
}
```

### SignalRPrivateEndpointConnections_Get

```java
import com.azure.core.util.Context;

/** Samples for SignalRPrivateEndpointConnections Get. */
public final class SignalRPrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file: specification/signalr/resource-manager/Microsoft.SignalRService/preview/2022-08-01-preview/examples/SignalRPrivateEndpointConnections_Get.json
     */
    /**
     * Sample code: SignalRPrivateEndpointConnections_Get.
     *
     * @param manager Entry point to SignalRManager.
     */
    public static void signalRPrivateEndpointConnectionsGet(com.azure.resourcemanager.signalr.SignalRManager manager) {
        manager
            .signalRPrivateEndpointConnections()
            .getWithResponse(
                "mysignalrservice.1fa229cd-bf3f-47f0-8c49-afb36723997e",
                "myResourceGroup",
                "mySignalRService",
                Context.NONE);
    }
}
```

### SignalRPrivateEndpointConnections_List

```java
import com.azure.core.util.Context;

/** Samples for SignalRPrivateEndpointConnections List. */
public final class SignalRPrivateEndpointConnectionsListSamples {
    /*
     * x-ms-original-file: specification/signalr/resource-manager/Microsoft.SignalRService/preview/2022-08-01-preview/examples/SignalRPrivateEndpointConnections_List.json
     */
    /**
     * Sample code: SignalRPrivateEndpointConnections_List.
     *
     * @param manager Entry point to SignalRManager.
     */
    public static void signalRPrivateEndpointConnectionsList(com.azure.resourcemanager.signalr.SignalRManager manager) {
        manager.signalRPrivateEndpointConnections().list("myResourceGroup", "mySignalRService", Context.NONE);
    }
}
```

### SignalRPrivateEndpointConnections_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.signalr.fluent.models.PrivateEndpointConnectionInner;
import com.azure.resourcemanager.signalr.models.PrivateEndpoint;
import com.azure.resourcemanager.signalr.models.PrivateLinkServiceConnectionState;
import com.azure.resourcemanager.signalr.models.PrivateLinkServiceConnectionStatus;

/** Samples for SignalRPrivateEndpointConnections Update. */
public final class SignalRPrivateEndpointConnectionsUpdateSamples {
    /*
     * x-ms-original-file: specification/signalr/resource-manager/Microsoft.SignalRService/preview/2022-08-01-preview/examples/SignalRPrivateEndpointConnections_Update.json
     */
    /**
     * Sample code: SignalRPrivateEndpointConnections_Update.
     *
     * @param manager Entry point to SignalRManager.
     */
    public static void signalRPrivateEndpointConnectionsUpdate(
        com.azure.resourcemanager.signalr.SignalRManager manager) {
        manager
            .signalRPrivateEndpointConnections()
            .updateWithResponse(
                "mysignalrservice.1fa229cd-bf3f-47f0-8c49-afb36723997e",
                "myResourceGroup",
                "mySignalRService",
                new PrivateEndpointConnectionInner()
                    .withPrivateEndpoint(
                        new PrivateEndpoint()
                            .withId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/myResourceGroup/providers/Microsoft.Network/privateEndpoints/myPrivateEndpoint"))
                    .withPrivateLinkServiceConnectionState(
                        new PrivateLinkServiceConnectionState()
                            .withStatus(PrivateLinkServiceConnectionStatus.APPROVED)
                            .withActionsRequired("None")),
                Context.NONE);
    }
}
```

### SignalRPrivateLinkResources_List

```java
import com.azure.core.util.Context;

/** Samples for SignalRPrivateLinkResources List. */
public final class SignalRPrivateLinkResourcesListSamples {
    /*
     * x-ms-original-file: specification/signalr/resource-manager/Microsoft.SignalRService/preview/2022-08-01-preview/examples/SignalRPrivateLinkResources_List.json
     */
    /**
     * Sample code: SignalRPrivateLinkResources_List.
     *
     * @param manager Entry point to SignalRManager.
     */
    public static void signalRPrivateLinkResourcesList(com.azure.resourcemanager.signalr.SignalRManager manager) {
        manager.signalRPrivateLinkResources().list("myResourceGroup", "mySignalRService", Context.NONE);
    }
}
```

### SignalRSharedPrivateLinkResources_CreateOrUpdate

```java
/** Samples for SignalRSharedPrivateLinkResources CreateOrUpdate. */
public final class SignalRSharedPrivateLinkResourcesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/signalr/resource-manager/Microsoft.SignalRService/preview/2022-08-01-preview/examples/SignalRSharedPrivateLinkResources_CreateOrUpdate.json
     */
    /**
     * Sample code: SignalRSharedPrivateLinkResources_CreateOrUpdate.
     *
     * @param manager Entry point to SignalRManager.
     */
    public static void signalRSharedPrivateLinkResourcesCreateOrUpdate(
        com.azure.resourcemanager.signalr.SignalRManager manager) {
        manager
            .signalRSharedPrivateLinkResources()
            .define("upstream")
            .withExistingSignalR("myResourceGroup", "mySignalRService")
            .withGroupId("sites")
            .withPrivateLinkResourceId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/myResourceGroup/providers/Microsoft.Web/sites/myWebApp")
            .withRequestMessage("Please approve")
            .create();
    }
}
```

### SignalRSharedPrivateLinkResources_Delete

```java
import com.azure.core.util.Context;

/** Samples for SignalRSharedPrivateLinkResources Delete. */
public final class SignalRSharedPrivateLinkResourcesDeleteSamples {
    /*
     * x-ms-original-file: specification/signalr/resource-manager/Microsoft.SignalRService/preview/2022-08-01-preview/examples/SignalRSharedPrivateLinkResources_Delete.json
     */
    /**
     * Sample code: SignalRSharedPrivateLinkResources_Delete.
     *
     * @param manager Entry point to SignalRManager.
     */
    public static void signalRSharedPrivateLinkResourcesDelete(
        com.azure.resourcemanager.signalr.SignalRManager manager) {
        manager
            .signalRSharedPrivateLinkResources()
            .delete("upstream", "myResourceGroup", "mySignalRService", Context.NONE);
    }
}
```

### SignalRSharedPrivateLinkResources_Get

```java
import com.azure.core.util.Context;

/** Samples for SignalRSharedPrivateLinkResources Get. */
public final class SignalRSharedPrivateLinkResourcesGetSamples {
    /*
     * x-ms-original-file: specification/signalr/resource-manager/Microsoft.SignalRService/preview/2022-08-01-preview/examples/SignalRSharedPrivateLinkResources_Get.json
     */
    /**
     * Sample code: SignalRSharedPrivateLinkResources_Get.
     *
     * @param manager Entry point to SignalRManager.
     */
    public static void signalRSharedPrivateLinkResourcesGet(com.azure.resourcemanager.signalr.SignalRManager manager) {
        manager
            .signalRSharedPrivateLinkResources()
            .getWithResponse("upstream", "myResourceGroup", "mySignalRService", Context.NONE);
    }
}
```

### SignalRSharedPrivateLinkResources_List

```java
import com.azure.core.util.Context;

/** Samples for SignalRSharedPrivateLinkResources List. */
public final class SignalRSharedPrivateLinkResourcesListSamples {
    /*
     * x-ms-original-file: specification/signalr/resource-manager/Microsoft.SignalRService/preview/2022-08-01-preview/examples/SignalRSharedPrivateLinkResources_List.json
     */
    /**
     * Sample code: SignalRSharedPrivateLinkResources_List.
     *
     * @param manager Entry point to SignalRManager.
     */
    public static void signalRSharedPrivateLinkResourcesList(com.azure.resourcemanager.signalr.SignalRManager manager) {
        manager.signalRSharedPrivateLinkResources().list("myResourceGroup", "mySignalRService", Context.NONE);
    }
}
```

### Usages_List

```java
import com.azure.core.util.Context;

/** Samples for Usages List. */
public final class UsagesListSamples {
    /*
     * x-ms-original-file: specification/signalr/resource-manager/Microsoft.SignalRService/preview/2022-08-01-preview/examples/Usages_List.json
     */
    /**
     * Sample code: Usages_List.
     *
     * @param manager Entry point to SignalRManager.
     */
    public static void usagesList(com.azure.resourcemanager.signalr.SignalRManager manager) {
        manager.usages().list("eastus", Context.NONE);
    }
}
```

