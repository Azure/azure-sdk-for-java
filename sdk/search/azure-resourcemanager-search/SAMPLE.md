# Code snippets and samples


## AdminKeys

- [Get](#adminkeys_get)
- [Regenerate](#adminkeys_regenerate)

## Operations

- [List](#operations_list)

## PrivateEndpointConnections

- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [ListByService](#privateendpointconnections_listbyservice)
- [Update](#privateendpointconnections_update)

## PrivateLinkResources

- [ListSupported](#privatelinkresources_listsupported)

## QueryKeys

- [Create](#querykeys_create)
- [Delete](#querykeys_delete)
- [ListBySearchService](#querykeys_listbysearchservice)

## Services

- [CheckNameAvailability](#services_checknameavailability)
- [CreateOrUpdate](#services_createorupdate)
- [Delete](#services_delete)
- [GetByResourceGroup](#services_getbyresourcegroup)
- [List](#services_list)
- [ListByResourceGroup](#services_listbyresourcegroup)
- [Update](#services_update)

## SharedPrivateLinkResources

- [CreateOrUpdate](#sharedprivatelinkresources_createorupdate)
- [Delete](#sharedprivatelinkresources_delete)
- [Get](#sharedprivatelinkresources_get)
- [ListByService](#sharedprivatelinkresources_listbyservice)
### AdminKeys_Get

```java
/** Samples for AdminKeys Get. */
public final class AdminKeysGetSamples {
    /*
     * x-ms-original-file: specification/search/resource-manager/Microsoft.Search/stable/2022-09-01/examples/SearchGetAdminKeys.json
     */
    /**
     * Sample code: SearchGetAdminKeys.
     *
     * @param manager Entry point to SearchManager.
     */
    public static void searchGetAdminKeys(com.azure.resourcemanager.search.SearchManager manager) {
        manager.adminKeys().getWithResponse("rg1", "mysearchservice", null, com.azure.core.util.Context.NONE);
    }
}
```

### AdminKeys_Regenerate

```java
import com.azure.resourcemanager.search.models.AdminKeyKind;

/** Samples for AdminKeys Regenerate. */
public final class AdminKeysRegenerateSamples {
    /*
     * x-ms-original-file: specification/search/resource-manager/Microsoft.Search/stable/2022-09-01/examples/SearchRegenerateAdminKey.json
     */
    /**
     * Sample code: SearchRegenerateAdminKey.
     *
     * @param manager Entry point to SearchManager.
     */
    public static void searchRegenerateAdminKey(com.azure.resourcemanager.search.SearchManager manager) {
        manager
            .adminKeys()
            .regenerateWithResponse(
                "rg1", "mysearchservice", AdminKeyKind.PRIMARY, null, com.azure.core.util.Context.NONE);
    }
}
```

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/search/resource-manager/Microsoft.Search/stable/2022-09-01/examples/OperationsList.json
     */
    /**
     * Sample code: OperationsList.
     *
     * @param manager Entry point to SearchManager.
     */
    public static void operationsList(com.azure.resourcemanager.search.SearchManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_Delete

```java
/** Samples for PrivateEndpointConnections Delete. */
public final class PrivateEndpointConnectionsDeleteSamples {
    /*
     * x-ms-original-file: specification/search/resource-manager/Microsoft.Search/stable/2022-09-01/examples/DeletePrivateEndpointConnection.json
     */
    /**
     * Sample code: PrivateEndpointConnectionDelete.
     *
     * @param manager Entry point to SearchManager.
     */
    public static void privateEndpointConnectionDelete(com.azure.resourcemanager.search.SearchManager manager) {
        manager
            .privateEndpointConnections()
            .deleteWithResponse(
                "rg1",
                "mysearchservice",
                "testEndpoint.50bf4fbe-d7c1-4b48-a642-4f5892642546",
                null,
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_Get

```java
/** Samples for PrivateEndpointConnections Get. */
public final class PrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file: specification/search/resource-manager/Microsoft.Search/stable/2022-09-01/examples/GetPrivateEndpointConnection.json
     */
    /**
     * Sample code: PrivateEndpointConnectionGet.
     *
     * @param manager Entry point to SearchManager.
     */
    public static void privateEndpointConnectionGet(com.azure.resourcemanager.search.SearchManager manager) {
        manager
            .privateEndpointConnections()
            .getWithResponse(
                "rg1",
                "mysearchservice",
                "testEndpoint.50bf4fbe-d7c1-4b48-a642-4f5892642546",
                null,
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_ListByService

```java
/** Samples for PrivateEndpointConnections ListByService. */
public final class PrivateEndpointConnectionsListByServiceSamples {
    /*
     * x-ms-original-file: specification/search/resource-manager/Microsoft.Search/stable/2022-09-01/examples/ListPrivateEndpointConnectionsByService.json
     */
    /**
     * Sample code: ListPrivateEndpointConnectionsByService.
     *
     * @param manager Entry point to SearchManager.
     */
    public static void listPrivateEndpointConnectionsByService(com.azure.resourcemanager.search.SearchManager manager) {
        manager
            .privateEndpointConnections()
            .listByService("rg1", "mysearchservice", null, com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_Update

```java
import com.azure.resourcemanager.search.fluent.models.PrivateEndpointConnectionInner;
import com.azure.resourcemanager.search.models.PrivateEndpointConnectionProperties;
import com.azure.resourcemanager.search.models.PrivateEndpointConnectionPropertiesPrivateLinkServiceConnectionState;
import com.azure.resourcemanager.search.models.PrivateLinkServiceConnectionStatus;

/** Samples for PrivateEndpointConnections Update. */
public final class PrivateEndpointConnectionsUpdateSamples {
    /*
     * x-ms-original-file: specification/search/resource-manager/Microsoft.Search/stable/2022-09-01/examples/UpdatePrivateEndpointConnection.json
     */
    /**
     * Sample code: PrivateEndpointConnectionUpdate.
     *
     * @param manager Entry point to SearchManager.
     */
    public static void privateEndpointConnectionUpdate(com.azure.resourcemanager.search.SearchManager manager) {
        manager
            .privateEndpointConnections()
            .updateWithResponse(
                "rg1",
                "mysearchservice",
                "testEndpoint.50bf4fbe-d7c1-4b48-a642-4f5892642546",
                new PrivateEndpointConnectionInner()
                    .withProperties(
                        new PrivateEndpointConnectionProperties()
                            .withPrivateLinkServiceConnectionState(
                                new PrivateEndpointConnectionPropertiesPrivateLinkServiceConnectionState()
                                    .withStatus(PrivateLinkServiceConnectionStatus.REJECTED)
                                    .withDescription("Rejected for some reason"))),
                null,
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkResources_ListSupported

```java
/** Samples for PrivateLinkResources ListSupported. */
public final class PrivateLinkResourcesListSupportedSamples {
    /*
     * x-ms-original-file: specification/search/resource-manager/Microsoft.Search/stable/2022-09-01/examples/ListSupportedPrivateLinkResources.json
     */
    /**
     * Sample code: ListSupportedPrivateLinkResources.
     *
     * @param manager Entry point to SearchManager.
     */
    public static void listSupportedPrivateLinkResources(com.azure.resourcemanager.search.SearchManager manager) {
        manager.privateLinkResources().listSupported("rg1", "mysearchservice", null, com.azure.core.util.Context.NONE);
    }
}
```

### QueryKeys_Create

```java
/** Samples for QueryKeys Create. */
public final class QueryKeysCreateSamples {
    /*
     * x-ms-original-file: specification/search/resource-manager/Microsoft.Search/stable/2022-09-01/examples/SearchCreateQueryKey.json
     */
    /**
     * Sample code: SearchCreateQueryKey.
     *
     * @param manager Entry point to SearchManager.
     */
    public static void searchCreateQueryKey(com.azure.resourcemanager.search.SearchManager manager) {
        manager
            .queryKeys()
            .createWithResponse(
                "rg1",
                "mysearchservice",
                "Query key for browser-based clients",
                null,
                com.azure.core.util.Context.NONE);
    }
}
```

### QueryKeys_Delete

```java
/** Samples for QueryKeys Delete. */
public final class QueryKeysDeleteSamples {
    /*
     * x-ms-original-file: specification/search/resource-manager/Microsoft.Search/stable/2022-09-01/examples/SearchDeleteQueryKey.json
     */
    /**
     * Sample code: SearchDeleteQueryKey.
     *
     * @param manager Entry point to SearchManager.
     */
    public static void searchDeleteQueryKey(com.azure.resourcemanager.search.SearchManager manager) {
        manager
            .queryKeys()
            .deleteWithResponse("rg1", "mysearchservice", "<a query API key>", null, com.azure.core.util.Context.NONE);
    }
}
```

### QueryKeys_ListBySearchService

```java
/** Samples for QueryKeys ListBySearchService. */
public final class QueryKeysListBySearchServiceSamples {
    /*
     * x-ms-original-file: specification/search/resource-manager/Microsoft.Search/stable/2022-09-01/examples/SearchListQueryKeysBySearchService.json
     */
    /**
     * Sample code: SearchListQueryKeysBySearchService.
     *
     * @param manager Entry point to SearchManager.
     */
    public static void searchListQueryKeysBySearchService(com.azure.resourcemanager.search.SearchManager manager) {
        manager.queryKeys().listBySearchService("rg1", "mysearchservice", null, com.azure.core.util.Context.NONE);
    }
}
```

### Services_CheckNameAvailability

```java
import com.azure.resourcemanager.search.models.CheckNameAvailabilityInput;

/** Samples for Services CheckNameAvailability. */
public final class ServicesCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/search/resource-manager/Microsoft.Search/stable/2022-09-01/examples/SearchCheckNameAvailability.json
     */
    /**
     * Sample code: SearchCheckNameAvailability.
     *
     * @param manager Entry point to SearchManager.
     */
    public static void searchCheckNameAvailability(com.azure.resourcemanager.search.SearchManager manager) {
        manager
            .services()
            .checkNameAvailabilityWithResponse(
                new CheckNameAvailabilityInput().withName("mysearchservice"), null, com.azure.core.util.Context.NONE);
    }
}
```

### Services_CreateOrUpdate

```java
import com.azure.resourcemanager.search.models.AadAuthFailureMode;
import com.azure.resourcemanager.search.models.DataPlaneAadOrApiKeyAuthOption;
import com.azure.resourcemanager.search.models.DataPlaneAuthOptions;
import com.azure.resourcemanager.search.models.EncryptionWithCmk;
import com.azure.resourcemanager.search.models.HostingMode;
import com.azure.resourcemanager.search.models.Identity;
import com.azure.resourcemanager.search.models.IdentityType;
import com.azure.resourcemanager.search.models.IpRule;
import com.azure.resourcemanager.search.models.NetworkRuleSet;
import com.azure.resourcemanager.search.models.PublicNetworkAccess;
import com.azure.resourcemanager.search.models.SearchEncryptionWithCmk;
import com.azure.resourcemanager.search.models.Sku;
import com.azure.resourcemanager.search.models.SkuName;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Services CreateOrUpdate. */
public final class ServicesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/search/resource-manager/Microsoft.Search/stable/2022-09-01/examples/SearchCreateOrUpdateServiceToAllowAccessFromPrivateEndpoints.json
     */
    /**
     * Sample code: SearchCreateOrUpdateServiceToAllowAccessFromPrivateEndpoints.
     *
     * @param manager Entry point to SearchManager.
     */
    public static void searchCreateOrUpdateServiceToAllowAccessFromPrivateEndpoints(
        com.azure.resourcemanager.search.SearchManager manager) {
        manager
            .services()
            .define("mysearchservice")
            .withRegion("westus")
            .withExistingResourceGroup("rg1")
            .withTags(mapOf("app-name", "My e-commerce app"))
            .withSku(new Sku().withName(SkuName.STANDARD))
            .withReplicaCount(3)
            .withPartitionCount(1)
            .withHostingMode(HostingMode.DEFAULT)
            .withPublicNetworkAccess(PublicNetworkAccess.DISABLED)
            .create();
    }

    /*
     * x-ms-original-file: specification/search/resource-manager/Microsoft.Search/stable/2022-09-01/examples/SearchCreateOrUpdateService.json
     */
    /**
     * Sample code: SearchCreateOrUpdateService.
     *
     * @param manager Entry point to SearchManager.
     */
    public static void searchCreateOrUpdateService(com.azure.resourcemanager.search.SearchManager manager) {
        manager
            .services()
            .define("mysearchservice")
            .withRegion("westus")
            .withExistingResourceGroup("rg1")
            .withTags(mapOf("app-name", "My e-commerce app"))
            .withSku(new Sku().withName(SkuName.STANDARD))
            .withReplicaCount(3)
            .withPartitionCount(1)
            .withHostingMode(HostingMode.DEFAULT)
            .create();
    }

    /*
     * x-ms-original-file: specification/search/resource-manager/Microsoft.Search/stable/2022-09-01/examples/SearchCreateOrUpdateServiceAuthOptions.json
     */
    /**
     * Sample code: SearchCreateOrUpdateServiceAuthOptions.
     *
     * @param manager Entry point to SearchManager.
     */
    public static void searchCreateOrUpdateServiceAuthOptions(com.azure.resourcemanager.search.SearchManager manager) {
        manager
            .services()
            .define("mysearchservice")
            .withRegion("westus")
            .withExistingResourceGroup("rg1")
            .withTags(mapOf("app-name", "My e-commerce app"))
            .withSku(new Sku().withName(SkuName.STANDARD))
            .withReplicaCount(3)
            .withPartitionCount(1)
            .withHostingMode(HostingMode.DEFAULT)
            .withAuthOptions(
                new DataPlaneAuthOptions()
                    .withAadOrApiKey(
                        new DataPlaneAadOrApiKeyAuthOption()
                            .withAadAuthFailureMode(AadAuthFailureMode.HTTP401WITH_BEARER_CHALLENGE)))
            .create();
    }

    /*
     * x-ms-original-file: specification/search/resource-manager/Microsoft.Search/stable/2022-09-01/examples/SearchCreateOrUpdateServiceWithIdentity.json
     */
    /**
     * Sample code: SearchCreateOrUpdateServiceWithIdentity.
     *
     * @param manager Entry point to SearchManager.
     */
    public static void searchCreateOrUpdateServiceWithIdentity(com.azure.resourcemanager.search.SearchManager manager) {
        manager
            .services()
            .define("mysearchservice")
            .withRegion("westus")
            .withExistingResourceGroup("rg1")
            .withTags(mapOf("app-name", "My e-commerce app"))
            .withSku(new Sku().withName(SkuName.STANDARD))
            .withIdentity(new Identity().withType(IdentityType.SYSTEM_ASSIGNED))
            .withReplicaCount(3)
            .withPartitionCount(1)
            .withHostingMode(HostingMode.DEFAULT)
            .create();
    }

    /*
     * x-ms-original-file: specification/search/resource-manager/Microsoft.Search/stable/2022-09-01/examples/SearchCreateOrUpdateServiceDisableLocalAuth.json
     */
    /**
     * Sample code: SearchCreateOrUpdateServiceDisableLocalAuth.
     *
     * @param manager Entry point to SearchManager.
     */
    public static void searchCreateOrUpdateServiceDisableLocalAuth(
        com.azure.resourcemanager.search.SearchManager manager) {
        manager
            .services()
            .define("mysearchservice")
            .withRegion("westus")
            .withExistingResourceGroup("rg1")
            .withTags(mapOf("app-name", "My e-commerce app"))
            .withSku(new Sku().withName(SkuName.STANDARD))
            .withReplicaCount(3)
            .withPartitionCount(1)
            .withHostingMode(HostingMode.DEFAULT)
            .withDisableLocalAuth(true)
            .create();
    }

    /*
     * x-ms-original-file: specification/search/resource-manager/Microsoft.Search/stable/2022-09-01/examples/SearchCreateOrUpdateServiceToAllowAccessFromPublicCustomIPs.json
     */
    /**
     * Sample code: SearchCreateOrUpdateServiceToAllowAccessFromPublicCustomIPs.
     *
     * @param manager Entry point to SearchManager.
     */
    public static void searchCreateOrUpdateServiceToAllowAccessFromPublicCustomIPs(
        com.azure.resourcemanager.search.SearchManager manager) {
        manager
            .services()
            .define("mysearchservice")
            .withRegion("westus")
            .withExistingResourceGroup("rg1")
            .withTags(mapOf("app-name", "My e-commerce app"))
            .withSku(new Sku().withName(SkuName.STANDARD))
            .withReplicaCount(1)
            .withPartitionCount(1)
            .withHostingMode(HostingMode.DEFAULT)
            .withNetworkRuleSet(
                new NetworkRuleSet()
                    .withIpRules(
                        Arrays.asList(new IpRule().withValue("123.4.5.6"), new IpRule().withValue("123.4.6.0/18"))))
            .create();
    }

    /*
     * x-ms-original-file: specification/search/resource-manager/Microsoft.Search/stable/2022-09-01/examples/SearchCreateOrUpdateServiceWithCmkEnforcement.json
     */
    /**
     * Sample code: SearchCreateOrUpdateServiceWithCmkEnforcement.
     *
     * @param manager Entry point to SearchManager.
     */
    public static void searchCreateOrUpdateServiceWithCmkEnforcement(
        com.azure.resourcemanager.search.SearchManager manager) {
        manager
            .services()
            .define("mysearchservice")
            .withRegion("westus")
            .withExistingResourceGroup("rg1")
            .withTags(mapOf("app-name", "My e-commerce app"))
            .withSku(new Sku().withName(SkuName.STANDARD))
            .withReplicaCount(3)
            .withPartitionCount(1)
            .withHostingMode(HostingMode.DEFAULT)
            .withEncryptionWithCmk(new EncryptionWithCmk().withEnforcement(SearchEncryptionWithCmk.ENABLED))
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

### Services_Delete

```java
/** Samples for Services Delete. */
public final class ServicesDeleteSamples {
    /*
     * x-ms-original-file: specification/search/resource-manager/Microsoft.Search/stable/2022-09-01/examples/SearchDeleteService.json
     */
    /**
     * Sample code: SearchDeleteService.
     *
     * @param manager Entry point to SearchManager.
     */
    public static void searchDeleteService(com.azure.resourcemanager.search.SearchManager manager) {
        manager.services().deleteWithResponse("rg1", "mysearchservice", null, com.azure.core.util.Context.NONE);
    }
}
```

### Services_GetByResourceGroup

```java
/** Samples for Services GetByResourceGroup. */
public final class ServicesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/search/resource-manager/Microsoft.Search/stable/2022-09-01/examples/SearchGetService.json
     */
    /**
     * Sample code: SearchGetService.
     *
     * @param manager Entry point to SearchManager.
     */
    public static void searchGetService(com.azure.resourcemanager.search.SearchManager manager) {
        manager
            .services()
            .getByResourceGroupWithResponse("rg1", "mysearchservice", null, com.azure.core.util.Context.NONE);
    }
}
```

### Services_List

```java
/** Samples for Services List. */
public final class ServicesListSamples {
    /*
     * x-ms-original-file: specification/search/resource-manager/Microsoft.Search/stable/2022-09-01/examples/SearchListServicesBySubscription.json
     */
    /**
     * Sample code: SearchListServicesBySubscription.
     *
     * @param manager Entry point to SearchManager.
     */
    public static void searchListServicesBySubscription(com.azure.resourcemanager.search.SearchManager manager) {
        manager.services().list(null, com.azure.core.util.Context.NONE);
    }
}
```

### Services_ListByResourceGroup

```java
/** Samples for Services ListByResourceGroup. */
public final class ServicesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/search/resource-manager/Microsoft.Search/stable/2022-09-01/examples/SearchListServicesByResourceGroup.json
     */
    /**
     * Sample code: SearchListServicesByResourceGroup.
     *
     * @param manager Entry point to SearchManager.
     */
    public static void searchListServicesByResourceGroup(com.azure.resourcemanager.search.SearchManager manager) {
        manager.services().listByResourceGroup("rg1", null, com.azure.core.util.Context.NONE);
    }
}
```

### Services_Update

```java
import com.azure.resourcemanager.search.models.AadAuthFailureMode;
import com.azure.resourcemanager.search.models.DataPlaneAadOrApiKeyAuthOption;
import com.azure.resourcemanager.search.models.DataPlaneAuthOptions;
import com.azure.resourcemanager.search.models.EncryptionWithCmk;
import com.azure.resourcemanager.search.models.Identity;
import com.azure.resourcemanager.search.models.IdentityType;
import com.azure.resourcemanager.search.models.IpRule;
import com.azure.resourcemanager.search.models.NetworkRuleSet;
import com.azure.resourcemanager.search.models.PublicNetworkAccess;
import com.azure.resourcemanager.search.models.SearchEncryptionWithCmk;
import com.azure.resourcemanager.search.models.SearchService;
import com.azure.resourcemanager.search.models.Sku;
import com.azure.resourcemanager.search.models.SkuName;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Services Update. */
public final class ServicesUpdateSamples {
    /*
     * x-ms-original-file: specification/search/resource-manager/Microsoft.Search/stable/2022-09-01/examples/SearchUpdateServiceDisableLocalAuth.json
     */
    /**
     * Sample code: SearchUpdateServiceDisableLocalAuth.
     *
     * @param manager Entry point to SearchManager.
     */
    public static void searchUpdateServiceDisableLocalAuth(com.azure.resourcemanager.search.SearchManager manager) {
        SearchService resource =
            manager
                .services()
                .getByResourceGroupWithResponse("rg1", "mysearchservice", null, com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("app-name", "My e-commerce app", "new-tag", "Adding a new tag"))
            .withReplicaCount(2)
            .withDisableLocalAuth(true)
            .apply();
    }

    /*
     * x-ms-original-file: specification/search/resource-manager/Microsoft.Search/stable/2022-09-01/examples/SearchUpdateServiceWithCmkEnforcement.json
     */
    /**
     * Sample code: SearchUpdateServiceWithCmkEnforcement.
     *
     * @param manager Entry point to SearchManager.
     */
    public static void searchUpdateServiceWithCmkEnforcement(com.azure.resourcemanager.search.SearchManager manager) {
        SearchService resource =
            manager
                .services()
                .getByResourceGroupWithResponse("rg1", "mysearchservice", null, com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("app-name", "My e-commerce app", "new-tag", "Adding a new tag"))
            .withReplicaCount(2)
            .withEncryptionWithCmk(new EncryptionWithCmk().withEnforcement(SearchEncryptionWithCmk.ENABLED))
            .apply();
    }

    /*
     * x-ms-original-file: specification/search/resource-manager/Microsoft.Search/stable/2022-09-01/examples/SearchUpdateServiceToRemoveIdentity.json
     */
    /**
     * Sample code: SearchUpdateServiceToRemoveIdentity.
     *
     * @param manager Entry point to SearchManager.
     */
    public static void searchUpdateServiceToRemoveIdentity(com.azure.resourcemanager.search.SearchManager manager) {
        SearchService resource =
            manager
                .services()
                .getByResourceGroupWithResponse("rg1", "mysearchservice", null, com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withSku(new Sku().withName(SkuName.STANDARD))
            .withIdentity(new Identity().withType(IdentityType.NONE))
            .apply();
    }

    /*
     * x-ms-original-file: specification/search/resource-manager/Microsoft.Search/stable/2022-09-01/examples/SearchUpdateServiceToAllowAccessFromPrivateEndpoints.json
     */
    /**
     * Sample code: SearchUpdateServiceToAllowAccessFromPrivateEndpoints.
     *
     * @param manager Entry point to SearchManager.
     */
    public static void searchUpdateServiceToAllowAccessFromPrivateEndpoints(
        com.azure.resourcemanager.search.SearchManager manager) {
        SearchService resource =
            manager
                .services()
                .getByResourceGroupWithResponse("rg1", "mysearchservice", null, com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withReplicaCount(1)
            .withPartitionCount(1)
            .withPublicNetworkAccess(PublicNetworkAccess.DISABLED)
            .apply();
    }

    /*
     * x-ms-original-file: specification/search/resource-manager/Microsoft.Search/stable/2022-09-01/examples/SearchUpdateServiceToAllowAccessFromPublicCustomIPs.json
     */
    /**
     * Sample code: SearchUpdateServiceToAllowAccessFromPublicCustomIPs.
     *
     * @param manager Entry point to SearchManager.
     */
    public static void searchUpdateServiceToAllowAccessFromPublicCustomIPs(
        com.azure.resourcemanager.search.SearchManager manager) {
        SearchService resource =
            manager
                .services()
                .getByResourceGroupWithResponse("rg1", "mysearchservice", null, com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withReplicaCount(3)
            .withPartitionCount(1)
            .withPublicNetworkAccess(PublicNetworkAccess.ENABLED)
            .withNetworkRuleSet(
                new NetworkRuleSet()
                    .withIpRules(
                        Arrays.asList(new IpRule().withValue("123.4.5.6"), new IpRule().withValue("123.4.6.0/18"))))
            .apply();
    }

    /*
     * x-ms-original-file: specification/search/resource-manager/Microsoft.Search/stable/2022-09-01/examples/SearchUpdateServiceAuthOptions.json
     */
    /**
     * Sample code: SearchUpdateServiceAuthOptions.
     *
     * @param manager Entry point to SearchManager.
     */
    public static void searchUpdateServiceAuthOptions(com.azure.resourcemanager.search.SearchManager manager) {
        SearchService resource =
            manager
                .services()
                .getByResourceGroupWithResponse("rg1", "mysearchservice", null, com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("app-name", "My e-commerce app", "new-tag", "Adding a new tag"))
            .withReplicaCount(2)
            .withAuthOptions(
                new DataPlaneAuthOptions()
                    .withAadOrApiKey(
                        new DataPlaneAadOrApiKeyAuthOption()
                            .withAadAuthFailureMode(AadAuthFailureMode.HTTP401WITH_BEARER_CHALLENGE)))
            .apply();
    }

    /*
     * x-ms-original-file: specification/search/resource-manager/Microsoft.Search/stable/2022-09-01/examples/SearchUpdateService.json
     */
    /**
     * Sample code: SearchUpdateService.
     *
     * @param manager Entry point to SearchManager.
     */
    public static void searchUpdateService(com.azure.resourcemanager.search.SearchManager manager) {
        SearchService resource =
            manager
                .services()
                .getByResourceGroupWithResponse("rg1", "mysearchservice", null, com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("app-name", "My e-commerce app", "new-tag", "Adding a new tag"))
            .withReplicaCount(2)
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

### SharedPrivateLinkResources_CreateOrUpdate

```java
import com.azure.resourcemanager.search.models.SharedPrivateLinkResourceProperties;

/** Samples for SharedPrivateLinkResources CreateOrUpdate. */
public final class SharedPrivateLinkResourcesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/search/resource-manager/Microsoft.Search/stable/2022-09-01/examples/CreateOrUpdateSharedPrivateLinkResource.json
     */
    /**
     * Sample code: SharedPrivateLinkResourceCreateOrUpdate.
     *
     * @param manager Entry point to SearchManager.
     */
    public static void sharedPrivateLinkResourceCreateOrUpdate(com.azure.resourcemanager.search.SearchManager manager) {
        manager
            .sharedPrivateLinkResources()
            .define("testResource")
            .withExistingSearchService("rg1", "mysearchservice")
            .withProperties(
                new SharedPrivateLinkResourceProperties()
                    .withPrivateLinkResourceId(
                        "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.Storage/storageAccounts/storageAccountName")
                    .withGroupId("blob")
                    .withRequestMessage("please approve"))
            .create();
    }
}
```

### SharedPrivateLinkResources_Delete

```java
/** Samples for SharedPrivateLinkResources Delete. */
public final class SharedPrivateLinkResourcesDeleteSamples {
    /*
     * x-ms-original-file: specification/search/resource-manager/Microsoft.Search/stable/2022-09-01/examples/DeleteSharedPrivateLinkResource.json
     */
    /**
     * Sample code: SharedPrivateLinkResourceDelete.
     *
     * @param manager Entry point to SearchManager.
     */
    public static void sharedPrivateLinkResourceDelete(com.azure.resourcemanager.search.SearchManager manager) {
        manager
            .sharedPrivateLinkResources()
            .delete("rg1", "mysearchservice", "testResource", null, com.azure.core.util.Context.NONE);
    }
}
```

### SharedPrivateLinkResources_Get

```java
/** Samples for SharedPrivateLinkResources Get. */
public final class SharedPrivateLinkResourcesGetSamples {
    /*
     * x-ms-original-file: specification/search/resource-manager/Microsoft.Search/stable/2022-09-01/examples/GetSharedPrivateLinkResource.json
     */
    /**
     * Sample code: SharedPrivateLinkResourceGet.
     *
     * @param manager Entry point to SearchManager.
     */
    public static void sharedPrivateLinkResourceGet(com.azure.resourcemanager.search.SearchManager manager) {
        manager
            .sharedPrivateLinkResources()
            .getWithResponse("rg1", "mysearchservice", "testResource", null, com.azure.core.util.Context.NONE);
    }
}
```

### SharedPrivateLinkResources_ListByService

```java
/** Samples for SharedPrivateLinkResources ListByService. */
public final class SharedPrivateLinkResourcesListByServiceSamples {
    /*
     * x-ms-original-file: specification/search/resource-manager/Microsoft.Search/stable/2022-09-01/examples/ListSharedPrivateLinkResourcesByService.json
     */
    /**
     * Sample code: ListSharedPrivateLinkResourcesByService.
     *
     * @param manager Entry point to SearchManager.
     */
    public static void listSharedPrivateLinkResourcesByService(com.azure.resourcemanager.search.SearchManager manager) {
        manager
            .sharedPrivateLinkResources()
            .listByService("rg1", "mysearchservice", null, com.azure.core.util.Context.NONE);
    }
}
```

