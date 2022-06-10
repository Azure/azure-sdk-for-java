# Code snippets and samples


## Accounts

- [CreateOrUpdate](#accounts_createorupdate)
- [Delete](#accounts_delete)
- [GetByResourceGroup](#accounts_getbyresourcegroup)
- [List](#accounts_list)
- [ListByResourceGroup](#accounts_listbyresourcegroup)
- [Update](#accounts_update)

## EnterprisePolicies

- [CreateOrUpdate](#enterprisepolicies_createorupdate)
- [Delete](#enterprisepolicies_delete)
- [GetByResourceGroup](#enterprisepolicies_getbyresourcegroup)
- [List](#enterprisepolicies_list)
- [ListByResourceGroup](#enterprisepolicies_listbyresourcegroup)
- [Update](#enterprisepolicies_update)

## Operations

- [List](#operations_list)

## PrivateEndpointConnections

- [CreateOrUpdate](#privateendpointconnections_createorupdate)
- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [ListByEnterprisePolicy](#privateendpointconnections_listbyenterprisepolicy)

## PrivateLinkResources

- [Get](#privatelinkresources_get)
- [ListByEnterprisePolicy](#privatelinkresources_listbyenterprisepolicy)
### Accounts_CreateOrUpdate

```java
import java.util.HashMap;
import java.util.Map;

/** Samples for Accounts CreateOrUpdate. */
public final class AccountsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/powerplatform/resource-manager/Microsoft.PowerPlatform/preview/2020-10-30-preview/examples/createOrUpdateAccount.json
     */
    /**
     * Sample code: Create or update account.
     *
     * @param manager Entry point to PowerPlatformManager.
     */
    public static void createOrUpdateAccount(com.azure.resourcemanager.powerplatform.PowerPlatformManager manager) {
        manager
            .accounts()
            .define("account")
            .withRegion("East US")
            .withExistingResourceGroup("resourceGroup")
            .withTags(mapOf("Organization", "Administration"))
            .withDescription("Description of the account.")
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

### Accounts_Delete

```java
import com.azure.core.util.Context;

/** Samples for Accounts Delete. */
public final class AccountsDeleteSamples {
    /*
     * x-ms-original-file: specification/powerplatform/resource-manager/Microsoft.PowerPlatform/preview/2020-10-30-preview/examples/deleteAccount.json
     */
    /**
     * Sample code: Delete account.
     *
     * @param manager Entry point to PowerPlatformManager.
     */
    public static void deleteAccount(com.azure.resourcemanager.powerplatform.PowerPlatformManager manager) {
        manager.accounts().deleteWithResponse("resourceGroup", "account", Context.NONE);
    }
}
```

### Accounts_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Accounts GetByResourceGroup. */
public final class AccountsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/powerplatform/resource-manager/Microsoft.PowerPlatform/preview/2020-10-30-preview/examples/getAccount.json
     */
    /**
     * Sample code: Get account.
     *
     * @param manager Entry point to PowerPlatformManager.
     */
    public static void getAccount(com.azure.resourcemanager.powerplatform.PowerPlatformManager manager) {
        manager.accounts().getByResourceGroupWithResponse("rg", "account", Context.NONE);
    }
}
```

### Accounts_List

```java
import com.azure.core.util.Context;

/** Samples for Accounts List. */
public final class AccountsListSamples {
    /*
     * x-ms-original-file: specification/powerplatform/resource-manager/Microsoft.PowerPlatform/preview/2020-10-30-preview/examples/listAccountsBySubscription.json
     */
    /**
     * Sample code: List accounts by subscription.
     *
     * @param manager Entry point to PowerPlatformManager.
     */
    public static void listAccountsBySubscription(
        com.azure.resourcemanager.powerplatform.PowerPlatformManager manager) {
        manager.accounts().list(Context.NONE);
    }
}
```

### Accounts_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Accounts ListByResourceGroup. */
public final class AccountsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/powerplatform/resource-manager/Microsoft.PowerPlatform/preview/2020-10-30-preview/examples/listAccountsByResourceGroup.json
     */
    /**
     * Sample code: List accounts by resource group.
     *
     * @param manager Entry point to PowerPlatformManager.
     */
    public static void listAccountsByResourceGroup(
        com.azure.resourcemanager.powerplatform.PowerPlatformManager manager) {
        manager.accounts().listByResourceGroup("rg", Context.NONE);
    }
}
```

### Accounts_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.powerplatform.models.Account;
import java.util.HashMap;
import java.util.Map;

/** Samples for Accounts Update. */
public final class AccountsUpdateSamples {
    /*
     * x-ms-original-file: specification/powerplatform/resource-manager/Microsoft.PowerPlatform/preview/2020-10-30-preview/examples/updateAccount.json
     */
    /**
     * Sample code: Update account.
     *
     * @param manager Entry point to PowerPlatformManager.
     */
    public static void updateAccount(com.azure.resourcemanager.powerplatform.PowerPlatformManager manager) {
        Account resource =
            manager.accounts().getByResourceGroupWithResponse("resourceGroup", "account", Context.NONE).getValue();
        resource
            .update()
            .withTags(mapOf("Organization", "Administration"))
            .withDescription("Description of account.")
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

### EnterprisePolicies_CreateOrUpdate

```java
import com.azure.resourcemanager.powerplatform.models.EnterprisePolicyIdentity;
import com.azure.resourcemanager.powerplatform.models.EnterprisePolicyKind;
import com.azure.resourcemanager.powerplatform.models.ResourceIdentityType;
import java.util.HashMap;
import java.util.Map;

/** Samples for EnterprisePolicies CreateOrUpdate. */
public final class EnterprisePoliciesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/powerplatform/resource-manager/Microsoft.PowerPlatform/preview/2020-10-30-preview/examples/createOrUpdateEnterprisePolicy.json
     */
    /**
     * Sample code: Create or update EnterprisePolicy.
     *
     * @param manager Entry point to PowerPlatformManager.
     */
    public static void createOrUpdateEnterprisePolicy(
        com.azure.resourcemanager.powerplatform.PowerPlatformManager manager) {
        manager
            .enterprisePolicies()
            .define("enterprisePolicy")
            .withRegion("East US")
            .withExistingResourceGroup("resourceGroup")
            .withKind(EnterprisePolicyKind.LOCKBOX)
            .withTags(mapOf("Organization", "Administration"))
            .withIdentity(new EnterprisePolicyIdentity().withType(ResourceIdentityType.SYSTEM_ASSIGNED))
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

### EnterprisePolicies_Delete

```java
import com.azure.core.util.Context;

/** Samples for EnterprisePolicies Delete. */
public final class EnterprisePoliciesDeleteSamples {
    /*
     * x-ms-original-file: specification/powerplatform/resource-manager/Microsoft.PowerPlatform/preview/2020-10-30-preview/examples/deleteEnterprisePolicy.json
     */
    /**
     * Sample code: Delete an EnterprisePolicy.
     *
     * @param manager Entry point to PowerPlatformManager.
     */
    public static void deleteAnEnterprisePolicy(com.azure.resourcemanager.powerplatform.PowerPlatformManager manager) {
        manager.enterprisePolicies().deleteWithResponse("resourceGroup", "enterprisePolicy", Context.NONE);
    }
}
```

### EnterprisePolicies_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for EnterprisePolicies GetByResourceGroup. */
public final class EnterprisePoliciesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/powerplatform/resource-manager/Microsoft.PowerPlatform/preview/2020-10-30-preview/examples/getEnterprisePolicy.json
     */
    /**
     * Sample code: Get an EnterprisePolicy.
     *
     * @param manager Entry point to PowerPlatformManager.
     */
    public static void getAnEnterprisePolicy(com.azure.resourcemanager.powerplatform.PowerPlatformManager manager) {
        manager.enterprisePolicies().getByResourceGroupWithResponse("rg", "enterprisePolicy", Context.NONE);
    }
}
```

### EnterprisePolicies_List

```java
import com.azure.core.util.Context;

/** Samples for EnterprisePolicies List. */
public final class EnterprisePoliciesListSamples {
    /*
     * x-ms-original-file: specification/powerplatform/resource-manager/Microsoft.PowerPlatform/preview/2020-10-30-preview/examples/listEnterprisePoliciesBySubscription.json
     */
    /**
     * Sample code: List EnterprisePolicies by subscription.
     *
     * @param manager Entry point to PowerPlatformManager.
     */
    public static void listEnterprisePoliciesBySubscription(
        com.azure.resourcemanager.powerplatform.PowerPlatformManager manager) {
        manager.enterprisePolicies().list(Context.NONE);
    }
}
```

### EnterprisePolicies_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for EnterprisePolicies ListByResourceGroup. */
public final class EnterprisePoliciesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/powerplatform/resource-manager/Microsoft.PowerPlatform/preview/2020-10-30-preview/examples/listEnterprisePoliciesByResourceGroup.json
     */
    /**
     * Sample code: List EnterprisePolicies by resource group.
     *
     * @param manager Entry point to PowerPlatformManager.
     */
    public static void listEnterprisePoliciesByResourceGroup(
        com.azure.resourcemanager.powerplatform.PowerPlatformManager manager) {
        manager.enterprisePolicies().listByResourceGroup("rg1", Context.NONE);
    }
}
```

### EnterprisePolicies_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.powerplatform.models.EnterprisePolicy;
import com.azure.resourcemanager.powerplatform.models.EnterprisePolicyIdentity;
import com.azure.resourcemanager.powerplatform.models.ResourceIdentityType;
import java.util.HashMap;
import java.util.Map;

/** Samples for EnterprisePolicies Update. */
public final class EnterprisePoliciesUpdateSamples {
    /*
     * x-ms-original-file: specification/powerplatform/resource-manager/Microsoft.PowerPlatform/preview/2020-10-30-preview/examples/updateEnterprisePolicy.json
     */
    /**
     * Sample code: Update EnterprisePolicy.
     *
     * @param manager Entry point to PowerPlatformManager.
     */
    public static void updateEnterprisePolicy(com.azure.resourcemanager.powerplatform.PowerPlatformManager manager) {
        EnterprisePolicy resource =
            manager
                .enterprisePolicies()
                .getByResourceGroupWithResponse("resourceGroup", "enterprisePolicy", Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("Organization", "Administration"))
            .withIdentity(new EnterprisePolicyIdentity().withType(ResourceIdentityType.SYSTEM_ASSIGNED))
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

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/powerplatform/resource-manager/Microsoft.PowerPlatform/preview/2020-10-30-preview/examples/listOperations.json
     */
    /**
     * Sample code: Lists all of the available PowerPlatform REST API operations.
     *
     * @param manager Entry point to PowerPlatformManager.
     */
    public static void listsAllOfTheAvailablePowerPlatformRESTAPIOperations(
        com.azure.resourcemanager.powerplatform.PowerPlatformManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### PrivateEndpointConnections_CreateOrUpdate

```java
import com.azure.resourcemanager.powerplatform.models.PrivateEndpointServiceConnectionStatus;
import com.azure.resourcemanager.powerplatform.models.PrivateLinkServiceConnectionState;

/** Samples for PrivateEndpointConnections CreateOrUpdate. */
public final class PrivateEndpointConnectionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/powerplatform/resource-manager/Microsoft.PowerPlatform/preview/2020-10-30-preview/examples/PrivateEndpointConnectionUpdate.json
     */
    /**
     * Sample code: Approve or reject a private endpoint connection with a given name.
     *
     * @param manager Entry point to PowerPlatformManager.
     */
    public static void approveOrRejectAPrivateEndpointConnectionWithAGivenName(
        com.azure.resourcemanager.powerplatform.PowerPlatformManager manager) {
        manager
            .privateEndpointConnections()
            .define("privateEndpointConnectionName")
            .withExistingEnterprisePolicy("rg1", "ddb1")
            .withPrivateLinkServiceConnectionState(
                new PrivateLinkServiceConnectionState()
                    .withStatus(PrivateEndpointServiceConnectionStatus.APPROVED)
                    .withDescription("Approved by johndoe@contoso.com"))
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
     * x-ms-original-file: specification/powerplatform/resource-manager/Microsoft.PowerPlatform/preview/2020-10-30-preview/examples/PrivateEndpointConnectionDelete.json
     */
    /**
     * Sample code: Deletes a private endpoint connection with a given name.
     *
     * @param manager Entry point to PowerPlatformManager.
     */
    public static void deletesAPrivateEndpointConnectionWithAGivenName(
        com.azure.resourcemanager.powerplatform.PowerPlatformManager manager) {
        manager.privateEndpointConnections().delete("rg1", "ddb1", "privateEndpointConnectionName", Context.NONE);
    }
}
```

### PrivateEndpointConnections_Get

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections Get. */
public final class PrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file: specification/powerplatform/resource-manager/Microsoft.PowerPlatform/preview/2020-10-30-preview/examples/PrivateEndpointConnectionGet.json
     */
    /**
     * Sample code: Gets private endpoint connection.
     *
     * @param manager Entry point to PowerPlatformManager.
     */
    public static void getsPrivateEndpointConnection(
        com.azure.resourcemanager.powerplatform.PowerPlatformManager manager) {
        manager
            .privateEndpointConnections()
            .getWithResponse("rg1", "ddb1", "privateEndpointConnectionName", Context.NONE);
    }
}
```

### PrivateEndpointConnections_ListByEnterprisePolicy

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections ListByEnterprisePolicy. */
public final class PrivateEndpointConnectionsListByEnterprisePolicySamples {
    /*
     * x-ms-original-file: specification/powerplatform/resource-manager/Microsoft.PowerPlatform/preview/2020-10-30-preview/examples/PrivateEndpointConnectionListGet.json
     */
    /**
     * Sample code: Gets private endpoint connection.
     *
     * @param manager Entry point to PowerPlatformManager.
     */
    public static void getsPrivateEndpointConnection(
        com.azure.resourcemanager.powerplatform.PowerPlatformManager manager) {
        manager.privateEndpointConnections().listByEnterprisePolicy("rg1", "ddb1", Context.NONE);
    }
}
```

### PrivateLinkResources_Get

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkResources Get. */
public final class PrivateLinkResourcesGetSamples {
    /*
     * x-ms-original-file: specification/powerplatform/resource-manager/Microsoft.PowerPlatform/preview/2020-10-30-preview/examples/PrivateLinkResourceGet.json
     */
    /**
     * Sample code: Gets private endpoint connection.
     *
     * @param manager Entry point to PowerPlatformManager.
     */
    public static void getsPrivateEndpointConnection(
        com.azure.resourcemanager.powerplatform.PowerPlatformManager manager) {
        manager.privateLinkResources().getWithResponse("rg1", "ddb1", "sql", Context.NONE);
    }
}
```

### PrivateLinkResources_ListByEnterprisePolicy

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkResources ListByEnterprisePolicy. */
public final class PrivateLinkResourcesListByEnterprisePolicySamples {
    /*
     * x-ms-original-file: specification/powerplatform/resource-manager/Microsoft.PowerPlatform/preview/2020-10-30-preview/examples/PrivateLinkResourceListGet.json
     */
    /**
     * Sample code: Gets private endpoint connection.
     *
     * @param manager Entry point to PowerPlatformManager.
     */
    public static void getsPrivateEndpointConnection(
        com.azure.resourcemanager.powerplatform.PowerPlatformManager manager) {
        manager.privateLinkResources().listByEnterprisePolicy("rg1", "ddb1", Context.NONE);
    }
}
```

