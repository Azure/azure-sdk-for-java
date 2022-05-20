# Code snippets and samples


## PrivateEndpointConnections

- [Create](#privateendpointconnections_create)
- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [ListByPolicyName](#privateendpointconnections_listbypolicyname)

## PrivateLinkForAzureAd

- [Create](#privatelinkforazuread_create)
- [Delete](#privatelinkforazuread_delete)
- [GetByResourceGroup](#privatelinkforazuread_getbyresourcegroup)
- [List](#privatelinkforazuread_list)
- [ListByResourceGroup](#privatelinkforazuread_listbyresourcegroup)
- [Update](#privatelinkforazuread_update)

## PrivateLinkResources

- [Get](#privatelinkresources_get)
- [ListByPrivateLinkPolicy](#privatelinkresources_listbyprivatelinkpolicy)
### PrivateEndpointConnections_Create

```java
import com.azure.resourcemanager.azureactivedirectory.models.PrivateEndpoint;
import com.azure.resourcemanager.azureactivedirectory.models.PrivateEndpointServiceConnectionStatus;
import com.azure.resourcemanager.azureactivedirectory.models.PrivateLinkServiceConnectionState;

/** Samples for PrivateEndpointConnections Create. */
public final class PrivateEndpointConnectionsCreateSamples {
    /*
     * x-ms-original-file: specification/azureactivedirectory/resource-manager/Microsoft.Aadiam/stable/2020-03-01/examples/AzureADPrivateEndpointConnectionsCreate.json
     */
    /**
     * Sample code: AadiamPutPrivateEndpointConnection.
     *
     * @param manager Entry point to AzureactivedirectoryManager.
     */
    public static void aadiamPutPrivateEndpointConnection(
        com.azure.resourcemanager.azureactivedirectory.AzureactivedirectoryManager manager) {
        manager
            .privateEndpointConnections()
            .define("{privateEndpointConnection name}")
            .withExistingPrivateLinkForAzureAd("resourcegroup", "example-policy-5849")
            .withPrivateEndpoint(
                new PrivateEndpoint()
                    .withId(
                        "subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/Default/providers/microsoft.aadiam/privateLinkForAzureAD/ddb1/privateLinkConnections/{privateEndpointConnection"
                            + " name}"))
            .withPrivateLinkServiceConnectionState(
                new PrivateLinkServiceConnectionState()
                    .withStatus(PrivateEndpointServiceConnectionStatus.APPROVED)
                    .withDescription("You may pass")
                    .withActionsRequired("None"))
            .create();
    }
}
```

### PrivateEndpointConnections_Delete

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections Delete. */
public final class PrivateEndpointConnectionsDeleteSamples {
    /*
     * x-ms-original-file: specification/azureactivedirectory/resource-manager/Microsoft.Aadiam/stable/2020-03-01/examples/AzureADPrivateEndpointConnectionsDelete.json
     */
    /**
     * Sample code: AadiamDeletePrivateEndpointConnections.
     *
     * @param manager Entry point to AzureactivedirectoryManager.
     */
    public static void aadiamDeletePrivateEndpointConnections(
        com.azure.resourcemanager.azureactivedirectory.AzureactivedirectoryManager manager) {
        manager
            .privateEndpointConnections()
            .delete("myResourceGroup", "example-policy-5849", "{privateEndpointConnection name}", Context.NONE);
    }
}
```

### PrivateEndpointConnections_Get

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections Get. */
public final class PrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file: specification/azureactivedirectory/resource-manager/Microsoft.Aadiam/stable/2020-03-01/examples/AzureADPrivateEndpointConnectionsGet.json
     */
    /**
     * Sample code: AadiamGetPrivateEndpointConnections.
     *
     * @param manager Entry point to AzureactivedirectoryManager.
     */
    public static void aadiamGetPrivateEndpointConnections(
        com.azure.resourcemanager.azureactivedirectory.AzureactivedirectoryManager manager) {
        manager
            .privateEndpointConnections()
            .getWithResponse(
                "myResourceGroup", "example-policy-5849", "{privateEndpointConnection name}", Context.NONE);
    }
}
```

### PrivateEndpointConnections_ListByPolicyName

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections ListByPolicyName. */
public final class PrivateEndpointConnectionsListByPolicyNameSamples {
    /*
     * x-ms-original-file: specification/azureactivedirectory/resource-manager/Microsoft.Aadiam/stable/2020-03-01/examples/AzureADPrivateEndpointConnectionsList.json
     */
    /**
     * Sample code: AadiamListPrivateEndpointConnections.
     *
     * @param manager Entry point to AzureactivedirectoryManager.
     */
    public static void aadiamListPrivateEndpointConnections(
        com.azure.resourcemanager.azureactivedirectory.AzureactivedirectoryManager manager) {
        manager.privateEndpointConnections().listByPolicyName("myResourceGroup", "example-policy-5849", Context.NONE);
    }
}
```

### PrivateLinkForAzureAd_Create

```java
import java.util.Arrays;

/** Samples for PrivateLinkForAzureAd Create. */
public final class PrivateLinkForAzureAdCreateSamples {
    /*
     * x-ms-original-file: specification/azureactivedirectory/resource-manager/Microsoft.Aadiam/stable/2020-03-01/examples/AzureADPrivateLinkPolicyCreate.json
     */
    /**
     * Sample code: privateLinkPolicyCreate.
     *
     * @param manager Entry point to AzureactivedirectoryManager.
     */
    public static void privateLinkPolicyCreate(
        com.azure.resourcemanager.azureactivedirectory.AzureactivedirectoryManager manager) {
        manager
            .privateLinkForAzureAds()
            .define("ddb1")
            .withExistingResourceGroup("rg1")
            .withName("myOrgPrivateLinkPolicy")
            .withOwnerTenantId("950f8bca-bf4d-4a41-ad10-034e792a243d")
            .withAllTenants(false)
            .withTenants(Arrays.asList("3616657d-1c80-41ae-9d83-2a2776f2c9be", "727b6ef1-18ab-4627-ac95-3f9cd945ed87"))
            .withResourceName("myOrgVnetPrivateLink")
            .withSubscriptionId("57849194-ea1f-470b-abda-d195b25634c1")
            .withResourceGroup("myOrgVnetRG")
            .create();
    }

    /*
     * x-ms-original-file: specification/azureactivedirectory/resource-manager/Microsoft.Aadiam/stable/2020-03-01/examples/AzureADPrivateLinkPolicyMinCreate.json
     */
    /**
     * Sample code: privateLinkPolicyMinCreate.
     *
     * @param manager Entry point to AzureactivedirectoryManager.
     */
    public static void privateLinkPolicyMinCreate(
        com.azure.resourcemanager.azureactivedirectory.AzureactivedirectoryManager manager) {
        manager
            .privateLinkForAzureAds()
            .define("ddb1")
            .withExistingResourceGroup("rg1")
            .withName("myOrgPrivateLinkPolicy")
            .withOwnerTenantId("950f8bca-bf4d-4a41-ad10-034e792a243d")
            .withAllTenants(false)
            .withTenants(Arrays.asList("3616657d-1c80-41ae-9d83-2a2776f2c9be", "727b6ef1-18ab-4627-ac95-3f9cd945ed87"))
            .withResourceName("myOrgVnetPrivateLink")
            .withSubscriptionId("57849194-ea1f-470b-abda-d195b25634c1")
            .withResourceGroup("myOrgVnetRG")
            .create();
    }
}
```

### PrivateLinkForAzureAd_Delete

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkForAzureAd Delete. */
public final class PrivateLinkForAzureAdDeleteSamples {
    /*
     * x-ms-original-file: specification/azureactivedirectory/resource-manager/Microsoft.Aadiam/stable/2020-03-01/examples/AzureADPrivateLinkPolicyDelete.json
     */
    /**
     * Sample code: privateLinkPolicyDelete.
     *
     * @param manager Entry point to AzureactivedirectoryManager.
     */
    public static void privateLinkPolicyDelete(
        com.azure.resourcemanager.azureactivedirectory.AzureactivedirectoryManager manager) {
        manager.privateLinkForAzureAds().deleteWithResponse("rg1", "ddb1", Context.NONE);
    }
}
```

### PrivateLinkForAzureAd_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkForAzureAd GetByResourceGroup. */
public final class PrivateLinkForAzureAdGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azureactivedirectory/resource-manager/Microsoft.Aadiam/stable/2020-03-01/examples/AzureADPrivateLinkPolicyGet.json
     */
    /**
     * Sample code: privateLinkPolicyGet.
     *
     * @param manager Entry point to AzureactivedirectoryManager.
     */
    public static void privateLinkPolicyGet(
        com.azure.resourcemanager.azureactivedirectory.AzureactivedirectoryManager manager) {
        manager.privateLinkForAzureAds().getByResourceGroupWithResponse("rg1", "ddb1", Context.NONE);
    }
}
```

### PrivateLinkForAzureAd_List

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkForAzureAd List. */
public final class PrivateLinkForAzureAdListSamples {
    /*
     * x-ms-original-file: specification/azureactivedirectory/resource-manager/Microsoft.Aadiam/stable/2020-03-01/examples/AzureADPrivateLinkPolicyListBySubscription.json
     */
    /**
     * Sample code: privateLinkPolicyListBySubscription.
     *
     * @param manager Entry point to AzureactivedirectoryManager.
     */
    public static void privateLinkPolicyListBySubscription(
        com.azure.resourcemanager.azureactivedirectory.AzureactivedirectoryManager manager) {
        manager.privateLinkForAzureAds().list(Context.NONE);
    }
}
```

### PrivateLinkForAzureAd_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkForAzureAd ListByResourceGroup. */
public final class PrivateLinkForAzureAdListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azureactivedirectory/resource-manager/Microsoft.Aadiam/stable/2020-03-01/examples/AzureADPrivateLinkPolicyList.json
     */
    /**
     * Sample code: privateLinkPolicyGetList.
     *
     * @param manager Entry point to AzureactivedirectoryManager.
     */
    public static void privateLinkPolicyGetList(
        com.azure.resourcemanager.azureactivedirectory.AzureactivedirectoryManager manager) {
        manager.privateLinkForAzureAds().listByResourceGroup("rg1", Context.NONE);
    }
}
```

### PrivateLinkForAzureAd_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.azureactivedirectory.models.PrivateLinkPolicy;
import java.util.HashMap;
import java.util.Map;

/** Samples for PrivateLinkForAzureAd Update. */
public final class PrivateLinkForAzureAdUpdateSamples {
    /*
     * x-ms-original-file: specification/azureactivedirectory/resource-manager/Microsoft.Aadiam/stable/2020-03-01/examples/AzureADPrivateLinkPolicyUpdate.json
     */
    /**
     * Sample code: privateLinkPolicyUpdate.
     *
     * @param manager Entry point to AzureactivedirectoryManager.
     */
    public static void privateLinkPolicyUpdate(
        com.azure.resourcemanager.azureactivedirectory.AzureactivedirectoryManager manager) {
        PrivateLinkPolicy resource =
            manager.privateLinkForAzureAds().getByResourceGroupWithResponse("rg1", "ddb1", Context.NONE).getValue();
        resource.update().withTags(mapOf()).apply();
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

### PrivateLinkResources_Get

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkResources Get. */
public final class PrivateLinkResourcesGetSamples {
    /*
     * x-ms-original-file: specification/azureactivedirectory/resource-manager/Microsoft.Aadiam/stable/2020-03-01/examples/AzureADPrivateLinkResourceGet.json
     */
    /**
     * Sample code: Gets private endpoint connection by subscription id, resource group name, policy name, and group
     * name.
     *
     * @param manager Entry point to AzureactivedirectoryManager.
     */
    public static void getsPrivateEndpointConnectionBySubscriptionIdResourceGroupNamePolicyNameAndGroupName(
        com.azure.resourcemanager.azureactivedirectory.AzureactivedirectoryManager manager) {
        manager.privateLinkResources().getWithResponse("rg1", "ddb1", "azureactivedirectory", Context.NONE);
    }
}
```

### PrivateLinkResources_ListByPrivateLinkPolicy

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkResources ListByPrivateLinkPolicy. */
public final class PrivateLinkResourcesListByPrivateLinkPolicySamples {
    /*
     * x-ms-original-file: specification/azureactivedirectory/resource-manager/Microsoft.Aadiam/stable/2020-03-01/examples/AzureADPrivateLinkResourceListGet.json
     */
    /**
     * Sample code: Gets private endpoint connection by subscription id, resource group name, and policy name.
     *
     * @param manager Entry point to AzureactivedirectoryManager.
     */
    public static void getsPrivateEndpointConnectionBySubscriptionIdResourceGroupNameAndPolicyName(
        com.azure.resourcemanager.azureactivedirectory.AzureactivedirectoryManager manager) {
        manager.privateLinkResources().listByPrivateLinkPolicy("rg1", "ddb1", Context.NONE);
    }
}
```

