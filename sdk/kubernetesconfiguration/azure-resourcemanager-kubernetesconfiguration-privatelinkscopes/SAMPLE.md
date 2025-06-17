# Code snippets and samples


## PrivateEndpointConnections

- [CreateOrUpdate](#privateendpointconnections_createorupdate)
- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [ListByPrivateLinkScope](#privateendpointconnections_listbyprivatelinkscope)

## PrivateLinkResources

- [Get](#privatelinkresources_get)
- [ListByPrivateLinkScope](#privatelinkresources_listbyprivatelinkscope)

## Scopes

- [CreateOrUpdate](#scopes_createorupdate)
- [Delete](#scopes_delete)
- [GetByResourceGroup](#scopes_getbyresourcegroup)
- [List](#scopes_list)
- [ListByResourceGroup](#scopes_listbyresourcegroup)
- [UpdateTags](#scopes_updatetags)
### PrivateEndpointConnections_CreateOrUpdate

```java
/**
 * Samples for Scopes GetByResourceGroup.
 */
public final class ScopesGetByResourceGrouSamples {
    /*
     * x-ms-original-file:
     * 0861aeb051bde6dfd0612cb37d7b600b449a97ee/specification/kubernetesconfiguration/resource-manager/Microsoft.
     * KubernetesConfiguration/privateLinkScopes/preview/2024-11-01-preview/examples/PrivateLinkScopesGet.json
     */
    /**
     * Sample code: PrivateLinkScopeGet.
     * 
     * @param manager Entry point to ScopeManager.
     */
    public static void
        privateLinkScopeGet(com.azure.resourcemanager.kubernetesconfiguration.privatelinkscopes.ScopeManager manager) {
        manager.scopes()
            .getByResourceGroupWithResponse("my-resource-group", "my-privatelinkscope",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_Delete

```java
/**
 * Samples for Scopes List.
 */
public final class ScopesListSamples {
    /*
     * x-ms-original-file:
     * 0861aeb051bde6dfd0612cb37d7b600b449a97ee/specification/kubernetesconfiguration/resource-manager/Microsoft.
     * KubernetesConfiguration/privateLinkScopes/preview/2024-11-01-preview/examples/PrivateLinkScopesList.json
     */
    /**
     * Sample code: PrivateLinkScopesList.json.
     * 
     * @param manager Entry point to ScopeManager.
     */
    public static void privateLinkScopesListJson(
        com.azure.resourcemanager.kubernetesconfiguration.privatelinkscopes.ScopeManager manager) {
        manager.scopes().list(com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_Get

```java
/**
 * Samples for PrivateLinkResources ListByPrivateLinkScope.
 */
public final class PrivateLinkResourcesLisSamples {
    /*
     * x-ms-original-file:
     * 0861aeb051bde6dfd0612cb37d7b600b449a97ee/specification/kubernetesconfiguration/resource-manager/Microsoft.
     * KubernetesConfiguration/privateLinkScopes/preview/2024-11-01-preview/examples/
     * PrivateLinkScopePrivateLinkResourceListGet.json
     */
    /**
     * Sample code: Gets private endpoint connection.
     * 
     * @param manager Entry point to ScopeManager.
     */
    public static void getsPrivateEndpointConnection(
        com.azure.resourcemanager.kubernetesconfiguration.privatelinkscopes.ScopeManager manager) {
        manager.privateLinkResources()
            .listByPrivateLinkScopeWithResponse("myResourceGroup", "myPrivateLinkScope",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_ListByPrivateLinkScope

```java
/**
 * Samples for Scopes Delete.
 */
public final class ScopesDeleteSamples {
    /*
     * x-ms-original-file:
     * 0861aeb051bde6dfd0612cb37d7b600b449a97ee/specification/kubernetesconfiguration/resource-manager/Microsoft.
     * KubernetesConfiguration/privateLinkScopes/preview/2024-11-01-preview/examples/PrivateLinkScopesDelete.json
     */
    /**
     * Sample code: PrivateLinkScopesDelete.
     * 
     * @param manager Entry point to ScopeManager.
     */
    public static void privateLinkScopesDelete(
        com.azure.resourcemanager.kubernetesconfiguration.privatelinkscopes.ScopeManager manager) {
        manager.scopes()
            .deleteByResourceGroupWithResponse("my-resource-group", "my-privatelinkscope",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkResources_Get

```java
/**
 * Samples for Scopes ListByResourceGroup.
 */
public final class ScopesListByResourceGroSamples {
    /*
     * x-ms-original-file:
     * 0861aeb051bde6dfd0612cb37d7b600b449a97ee/specification/kubernetesconfiguration/resource-manager/Microsoft.
     * KubernetesConfiguration/privateLinkScopes/preview/2024-11-01-preview/examples/
     * PrivateLinkScopesListByResourceGroup.json
     */
    /**
     * Sample code: PrivateLinkScopeListByResourceGroup.
     * 
     * @param manager Entry point to ScopeManager.
     */
    public static void privateLinkScopeListByResourceGroup(
        com.azure.resourcemanager.kubernetesconfiguration.privatelinkscopes.ScopeManager manager) {
        manager.scopes().listByResourceGroup("my-resource-group", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkResources_ListByPrivateLinkScope

```java
import com.azure.resourcemanager.kubernetesconfiguration.privatelinkscopes.models.PrivateLinkScope;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Scopes UpdateTags.
 */
public final class ScopesUpdateTagsSamples {
    /*
     * x-ms-original-file:
     * 0861aeb051bde6dfd0612cb37d7b600b449a97ee/specification/kubernetesconfiguration/resource-manager/Microsoft.
     * KubernetesConfiguration/privateLinkScopes/preview/2024-11-01-preview/examples/PrivateLinkScopesUpdateTagsOnly.
     * json
     */
    /**
     * Sample code: PrivateLinkScopeUpdateTagsOnly.
     * 
     * @param manager Entry point to ScopeManager.
     */
    public static void privateLinkScopeUpdateTagsOnly(
        com.azure.resourcemanager.kubernetesconfiguration.privatelinkscopes.ScopeManager manager) {
        PrivateLinkScope resource = manager.scopes()
            .getByResourceGroupWithResponse("my-resource-group", "my-privatelinkscope",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("Tag1", "Value1", "Tag2", "Value2")).apply();
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

### Scopes_CreateOrUpdate

```java
/**
 * Samples for PrivateLinkResources Get.
 */
public final class PrivateLinkResourcesGetSamples {
    /*
     * x-ms-original-file:
     * 0861aeb051bde6dfd0612cb37d7b600b449a97ee/specification/kubernetesconfiguration/resource-manager/Microsoft.
     * KubernetesConfiguration/privateLinkScopes/preview/2024-11-01-preview/examples/
     * PrivateLinkScopePrivateLinkResourceGet.json
     */
    /**
     * Sample code: Gets private endpoint connection.
     * 
     * @param manager Entry point to ScopeManager.
     */
    public static void getsPrivateEndpointConnection(
        com.azure.resourcemanager.kubernetesconfiguration.privatelinkscopes.ScopeManager manager) {
        manager.privateLinkResources()
            .getWithResponse("myResourceGroup", "myPrivateLinkScope", "KubernetesConfiguration",
                com.azure.core.util.Context.NONE);
    }
}
```

### Scopes_Delete

```java
import com.azure.resourcemanager.kubernetesconfiguration.privatelinkscopes.models.PrivateEndpointServiceConnectionStatus;
import com.azure.resourcemanager.kubernetesconfiguration.privatelinkscopes.models.PrivateLinkServiceConnectionState;

/**
 * Samples for PrivateEndpointConnections CreateOrUpdate.
 */
public final class PrivateEndpointConnectiSamples {
    /*
     * x-ms-original-file:
     * 0861aeb051bde6dfd0612cb37d7b600b449a97ee/specification/kubernetesconfiguration/resource-manager/Microsoft.
     * KubernetesConfiguration/privateLinkScopes/preview/2024-11-01-preview/examples/PrivateEndpointConnectionUpdate.
     * json
     */
    /**
     * Sample code: Approve or reject a private endpoint connection with a given name.
     * 
     * @param manager Entry point to ScopeManager.
     */
    public static void approveOrRejectAPrivateEndpointConnectionWithAGivenName(
        com.azure.resourcemanager.kubernetesconfiguration.privatelinkscopes.ScopeManager manager) {
        manager.privateEndpointConnections()
            .define("private-endpoint-connection-name")
            .withExistingPrivateLinkScope("myResourceGroup", "myPrivateLinkScope")
            .withPrivateLinkServiceConnectionState(
                new PrivateLinkServiceConnectionState().withStatus(PrivateEndpointServiceConnectionStatus.APPROVED)
                    .withDescription("Approved by johndoe@contoso.com"))
            .create();
    }
}
```

### Scopes_GetByResourceGroup

```java
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Scopes CreateOrUpdate.
 */
public final class ScopesCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * 0861aeb051bde6dfd0612cb37d7b600b449a97ee/specification/kubernetesconfiguration/resource-manager/Microsoft.
     * KubernetesConfiguration/privateLinkScopes/preview/2024-11-01-preview/examples/PrivateLinkScopesCreate.json
     */
    /**
     * Sample code: PrivateLinkScopeCreate.
     * 
     * @param manager Entry point to ScopeManager.
     */
    public static void privateLinkScopeCreate(
        com.azure.resourcemanager.kubernetesconfiguration.privatelinkscopes.ScopeManager manager) {
        manager.scopes()
            .define("my-privatelinkscope")
            .withRegion("westus")
            .withExistingResourceGroup("my-resource-group")
            .create();
    }

    /*
     * x-ms-original-file:
     * 0861aeb051bde6dfd0612cb37d7b600b449a97ee/specification/kubernetesconfiguration/resource-manager/Microsoft.
     * KubernetesConfiguration/privateLinkScopes/preview/2024-11-01-preview/examples/PrivateLinkScopesUpdate.json
     */
    /**
     * Sample code: PrivateLinkScopeUpdate.
     * 
     * @param manager Entry point to ScopeManager.
     */
    public static void privateLinkScopeUpdate(
        com.azure.resourcemanager.kubernetesconfiguration.privatelinkscopes.ScopeManager manager) {
        manager.scopes()
            .define("my-privatelinkscope")
            .withRegion("westus")
            .withExistingResourceGroup("my-resource-group")
            .withTags(mapOf("Tag1", "Value1"))
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

