# Code snippets and samples


## Accounts

- [Create](#accounts_create)
- [Delete](#accounts_delete)
- [GetByResourceGroup](#accounts_getbyresourcegroup)
- [Head](#accounts_head)
- [List](#accounts_list)
- [ListByResourceGroup](#accounts_listbyresourcegroup)
- [Update](#accounts_update)

## Instances

- [Create](#instances_create)
- [Delete](#instances_delete)
- [Get](#instances_get)
- [Head](#instances_head)
- [ListByAccount](#instances_listbyaccount)
- [Update](#instances_update)

## Operations

- [List](#operations_list)

## PrivateEndpointConnectionProxies

- [CreateOrUpdate](#privateendpointconnectionproxies_createorupdate)
- [Delete](#privateendpointconnectionproxies_delete)
- [Get](#privateendpointconnectionproxies_get)
- [ListByAccount](#privateendpointconnectionproxies_listbyaccount)
- [UpdatePrivateEndpointProperties](#privateendpointconnectionproxies_updateprivateendpointproperties)
- [Validate](#privateendpointconnectionproxies_validate)

## PrivateEndpointConnections

- [CreateOrUpdate](#privateendpointconnections_createorupdate)
- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [ListByAccount](#privateendpointconnections_listbyaccount)

## PrivateLinkResources

- [Get](#privatelinkresources_get)
- [ListByAccount](#privatelinkresources_listbyaccount)

## ResourceProvider

- [CheckNameAvailability](#resourceprovider_checknameavailability)
### Accounts_Create

```java
/** Samples for Accounts Create. */
public final class AccountsCreateSamples {
    /*
     * x-ms-original-file: specification/deviceupdate/resource-manager/Microsoft.DeviceUpdate/preview/2022-04-01-preview/examples/Accounts/Accounts_Create.json
     */
    /**
     * Sample code: Creates or updates Account.
     *
     * @param manager Entry point to DeviceUpdateManager.
     */
    public static void createsOrUpdatesAccount(com.azure.resourcemanager.deviceupdate.DeviceUpdateManager manager) {
        manager.accounts().define("contoso").withRegion("westus2").withExistingResourceGroup("test-rg").create();
    }
}
```

### Accounts_Delete

```java
import com.azure.core.util.Context;

/** Samples for Accounts Delete. */
public final class AccountsDeleteSamples {
    /*
     * x-ms-original-file: specification/deviceupdate/resource-manager/Microsoft.DeviceUpdate/preview/2022-04-01-preview/examples/Accounts/Accounts_Delete.json
     */
    /**
     * Sample code: Deletes an account.
     *
     * @param manager Entry point to DeviceUpdateManager.
     */
    public static void deletesAnAccount(com.azure.resourcemanager.deviceupdate.DeviceUpdateManager manager) {
        manager.accounts().delete("test-rg", "contoso", Context.NONE);
    }
}
```

### Accounts_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Accounts GetByResourceGroup. */
public final class AccountsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/deviceupdate/resource-manager/Microsoft.DeviceUpdate/preview/2022-04-01-preview/examples/Accounts/Accounts_Get.json
     */
    /**
     * Sample code: Gets Account details.
     *
     * @param manager Entry point to DeviceUpdateManager.
     */
    public static void getsAccountDetails(com.azure.resourcemanager.deviceupdate.DeviceUpdateManager manager) {
        manager.accounts().getByResourceGroupWithResponse("test-rg", "contoso", Context.NONE);
    }
}
```

### Accounts_Head

```java
import com.azure.core.util.Context;

/** Samples for Accounts Head. */
public final class AccountsHeadSamples {
    /*
     * x-ms-original-file: specification/deviceupdate/resource-manager/Microsoft.DeviceUpdate/preview/2022-04-01-preview/examples/Accounts/Accounts_Head.json
     */
    /**
     * Sample code: Checks whether account exists.
     *
     * @param manager Entry point to DeviceUpdateManager.
     */
    public static void checksWhetherAccountExists(com.azure.resourcemanager.deviceupdate.DeviceUpdateManager manager) {
        manager.accounts().headWithResponse("test-rg", "contoso", Context.NONE);
    }
}
```

### Accounts_List

```java
import com.azure.core.util.Context;

/** Samples for Accounts List. */
public final class AccountsListSamples {
    /*
     * x-ms-original-file: specification/deviceupdate/resource-manager/Microsoft.DeviceUpdate/preview/2022-04-01-preview/examples/Accounts/Accounts_List.json
     */
    /**
     * Sample code: Get list of Accounts.
     *
     * @param manager Entry point to DeviceUpdateManager.
     */
    public static void getListOfAccounts(com.azure.resourcemanager.deviceupdate.DeviceUpdateManager manager) {
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
     * x-ms-original-file: specification/deviceupdate/resource-manager/Microsoft.DeviceUpdate/preview/2022-04-01-preview/examples/Accounts/Accounts_List.json
     */
    /**
     * Sample code: Gets list of Accounts.
     *
     * @param manager Entry point to DeviceUpdateManager.
     */
    public static void getsListOfAccounts(com.azure.resourcemanager.deviceupdate.DeviceUpdateManager manager) {
        manager.accounts().listByResourceGroup("test-rg", Context.NONE);
    }
}
```

### Accounts_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.deviceupdate.models.Account;
import java.util.HashMap;
import java.util.Map;

/** Samples for Accounts Update. */
public final class AccountsUpdateSamples {
    /*
     * x-ms-original-file: specification/deviceupdate/resource-manager/Microsoft.DeviceUpdate/preview/2022-04-01-preview/examples/Accounts/Accounts_Update.json
     */
    /**
     * Sample code: Updates Account.
     *
     * @param manager Entry point to DeviceUpdateManager.
     */
    public static void updatesAccount(com.azure.resourcemanager.deviceupdate.DeviceUpdateManager manager) {
        Account resource =
            manager.accounts().getByResourceGroupWithResponse("test-rg", "contoso", Context.NONE).getValue();
        resource.update().withTags(mapOf("tagKey", "tagValue")).apply();
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

### Instances_Create

```java
import com.azure.resourcemanager.deviceupdate.models.AuthenticationType;
import com.azure.resourcemanager.deviceupdate.models.DiagnosticStorageProperties;
import com.azure.resourcemanager.deviceupdate.models.IotHubSettings;
import java.util.Arrays;

/** Samples for Instances Create. */
public final class InstancesCreateSamples {
    /*
     * x-ms-original-file: specification/deviceupdate/resource-manager/Microsoft.DeviceUpdate/preview/2022-04-01-preview/examples/Instances/Instances_Create.json
     */
    /**
     * Sample code: Creates or updates Instance.
     *
     * @param manager Entry point to DeviceUpdateManager.
     */
    public static void createsOrUpdatesInstance(com.azure.resourcemanager.deviceupdate.DeviceUpdateManager manager) {
        manager
            .instances()
            .define("blue")
            .withRegion("westus2")
            .withExistingAccount("test-rg", "contoso")
            .withIotHubs(
                Arrays
                    .asList(
                        new IotHubSettings()
                            .withResourceId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.Devices/IotHubs/blue-contoso-hub")))
            .withEnableDiagnostics(false)
            .withDiagnosticStorageProperties(
                new DiagnosticStorageProperties()
                    .withAuthenticationType(AuthenticationType.KEY_BASED)
                    .withConnectionString("string")
                    .withResourceId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/adu-resource-group/providers/Microsoft.Storage/storageAccounts/testAccount"))
            .create();
    }
}
```

### Instances_Delete

```java
import com.azure.core.util.Context;

/** Samples for Instances Delete. */
public final class InstancesDeleteSamples {
    /*
     * x-ms-original-file: specification/deviceupdate/resource-manager/Microsoft.DeviceUpdate/preview/2022-04-01-preview/examples/Instances/Instances_Delete.json
     */
    /**
     * Sample code: Deletes instance.
     *
     * @param manager Entry point to DeviceUpdateManager.
     */
    public static void deletesInstance(com.azure.resourcemanager.deviceupdate.DeviceUpdateManager manager) {
        manager.instances().delete("test-rg", "contoso", "blue", Context.NONE);
    }
}
```

### Instances_Get

```java
import com.azure.core.util.Context;

/** Samples for Instances Get. */
public final class InstancesGetSamples {
    /*
     * x-ms-original-file: specification/deviceupdate/resource-manager/Microsoft.DeviceUpdate/preview/2022-04-01-preview/examples/Instances/Instances_Get.json
     */
    /**
     * Sample code: Gets list of Instances.
     *
     * @param manager Entry point to DeviceUpdateManager.
     */
    public static void getsListOfInstances(com.azure.resourcemanager.deviceupdate.DeviceUpdateManager manager) {
        manager.instances().getWithResponse("test-rg", "contoso", "blue", Context.NONE);
    }
}
```

### Instances_Head

```java
import com.azure.core.util.Context;

/** Samples for Instances Head. */
public final class InstancesHeadSamples {
    /*
     * x-ms-original-file: specification/deviceupdate/resource-manager/Microsoft.DeviceUpdate/preview/2022-04-01-preview/examples/Instances/Instances_Head.json
     */
    /**
     * Sample code: Checks whether instance exists.
     *
     * @param manager Entry point to DeviceUpdateManager.
     */
    public static void checksWhetherInstanceExists(com.azure.resourcemanager.deviceupdate.DeviceUpdateManager manager) {
        manager.instances().headWithResponse("test-rg", "contoso", "blue", Context.NONE);
    }
}
```

### Instances_ListByAccount

```java
import com.azure.core.util.Context;

/** Samples for Instances ListByAccount. */
public final class InstancesListByAccountSamples {
    /*
     * x-ms-original-file: specification/deviceupdate/resource-manager/Microsoft.DeviceUpdate/preview/2022-04-01-preview/examples/Instances/Instances_ListByAccount.json
     */
    /**
     * Sample code: Gets list of Instances by Account.
     *
     * @param manager Entry point to DeviceUpdateManager.
     */
    public static void getsListOfInstancesByAccount(
        com.azure.resourcemanager.deviceupdate.DeviceUpdateManager manager) {
        manager.instances().listByAccount("test-rg", "contoso", Context.NONE);
    }
}
```

### Instances_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.deviceupdate.models.Instance;
import java.util.HashMap;
import java.util.Map;

/** Samples for Instances Update. */
public final class InstancesUpdateSamples {
    /*
     * x-ms-original-file: specification/deviceupdate/resource-manager/Microsoft.DeviceUpdate/preview/2022-04-01-preview/examples/Instances/Instances_Update.json
     */
    /**
     * Sample code: Updates Instance.
     *
     * @param manager Entry point to DeviceUpdateManager.
     */
    public static void updatesInstance(com.azure.resourcemanager.deviceupdate.DeviceUpdateManager manager) {
        Instance resource = manager.instances().getWithResponse("test-rg", "contoso", "blue", Context.NONE).getValue();
        resource.update().withTags(mapOf("tagKey", "tagValue")).apply();
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
     * x-ms-original-file: specification/deviceupdate/resource-manager/Microsoft.DeviceUpdate/preview/2022-04-01-preview/examples/Operations_List.json
     */
    /**
     * Sample code: Gets list of Operations.
     *
     * @param manager Entry point to DeviceUpdateManager.
     */
    public static void getsListOfOperations(com.azure.resourcemanager.deviceupdate.DeviceUpdateManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### PrivateEndpointConnectionProxies_CreateOrUpdate

```java
import com.azure.resourcemanager.deviceupdate.models.PrivateLinkServiceConnection;
import com.azure.resourcemanager.deviceupdate.models.PrivateLinkServiceProxy;
import com.azure.resourcemanager.deviceupdate.models.RemotePrivateEndpoint;
import java.util.Arrays;

/** Samples for PrivateEndpointConnectionProxies CreateOrUpdate. */
public final class PrivateEndpointConnectionProxiesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/deviceupdate/resource-manager/Microsoft.DeviceUpdate/preview/2022-04-01-preview/examples/PrivateEndpointConnectionProxies/PrivateEndpointConnectionProxy_CreateOrUpdate.json
     */
    /**
     * Sample code: PrivateEndpointConnectionProxyCreateOrUpdate.
     *
     * @param manager Entry point to DeviceUpdateManager.
     */
    public static void privateEndpointConnectionProxyCreateOrUpdate(
        com.azure.resourcemanager.deviceupdate.DeviceUpdateManager manager) {
        manager
            .privateEndpointConnectionProxies()
            .define("peexample01")
            .withExistingAccount("test-rg", "contoso")
            .withRemotePrivateEndpoint(
                new RemotePrivateEndpoint()
                    .withId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.Network/privateEndpoints/{peName}")
                    .withLocation("westus2")
                    .withImmutableSubscriptionId("00000000-0000-0000-0000-000000000000")
                    .withImmutableResourceId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.Network/privateEndpoints/{peName}")
                    .withManualPrivateLinkServiceConnections(
                        Arrays
                            .asList(
                                new PrivateLinkServiceConnection()
                                    .withName("{privateEndpointConnectionProxyId}")
                                    .withGroupIds(Arrays.asList("DeviceUpdate"))
                                    .withRequestMessage("Please approve my connection, thanks.")))
                    .withPrivateLinkServiceProxies(
                        Arrays
                            .asList(
                                new PrivateLinkServiceProxy()
                                    .withId(
                                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.Network/privateEndpoints/{privateEndpointConnectionProxyId}/privateLinkServiceProxies/{privateEndpointConnectionProxyId}")
                                    .withGroupConnectivityInformation(Arrays.asList()))))
            .create();
    }
}
```

### PrivateEndpointConnectionProxies_Delete

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnectionProxies Delete. */
public final class PrivateEndpointConnectionProxiesDeleteSamples {
    /*
     * x-ms-original-file: specification/deviceupdate/resource-manager/Microsoft.DeviceUpdate/preview/2022-04-01-preview/examples/PrivateEndpointConnectionProxies/PrivateEndpointConnectionProxy_Delete.json
     */
    /**
     * Sample code: PrivateEndpointConnectionProxyDelete.
     *
     * @param manager Entry point to DeviceUpdateManager.
     */
    public static void privateEndpointConnectionProxyDelete(
        com.azure.resourcemanager.deviceupdate.DeviceUpdateManager manager) {
        manager.privateEndpointConnectionProxies().delete("test-rg", "contoso", "peexample01", Context.NONE);
    }
}
```

### PrivateEndpointConnectionProxies_Get

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnectionProxies Get. */
public final class PrivateEndpointConnectionProxiesGetSamples {
    /*
     * x-ms-original-file: specification/deviceupdate/resource-manager/Microsoft.DeviceUpdate/preview/2022-04-01-preview/examples/PrivateEndpointConnectionProxies/PrivateEndpointConnectionProxy_Get.json
     */
    /**
     * Sample code: PrivateEndpointConnectionProxyGet.
     *
     * @param manager Entry point to DeviceUpdateManager.
     */
    public static void privateEndpointConnectionProxyGet(
        com.azure.resourcemanager.deviceupdate.DeviceUpdateManager manager) {
        manager.privateEndpointConnectionProxies().getWithResponse("test-rg", "contoso", "peexample01", Context.NONE);
    }
}
```

### PrivateEndpointConnectionProxies_ListByAccount

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnectionProxies ListByAccount. */
public final class PrivateEndpointConnectionProxiesListByAccountSamples {
    /*
     * x-ms-original-file: specification/deviceupdate/resource-manager/Microsoft.DeviceUpdate/preview/2022-04-01-preview/examples/PrivateEndpointConnectionProxies/PrivateEndpointConnectionProxy_ListByAccount.json
     */
    /**
     * Sample code: PrivateEndpointConnectionProxyList.
     *
     * @param manager Entry point to DeviceUpdateManager.
     */
    public static void privateEndpointConnectionProxyList(
        com.azure.resourcemanager.deviceupdate.DeviceUpdateManager manager) {
        manager.privateEndpointConnectionProxies().listByAccount("test-rg", "contoso", Context.NONE);
    }
}
```

### PrivateEndpointConnectionProxies_UpdatePrivateEndpointProperties

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.deviceupdate.models.PrivateEndpointUpdate;

/** Samples for PrivateEndpointConnectionProxies UpdatePrivateEndpointProperties. */
public final class PrivateEndpointConnectionProxiesUpdatePrivateEndpointPropertiesSamples {
    /*
     * x-ms-original-file: specification/deviceupdate/resource-manager/Microsoft.DeviceUpdate/preview/2022-04-01-preview/examples/PrivateEndpointConnectionProxies/PrivateEndpointConnectionProxy_PrivateEndpointUpdate.json
     */
    /**
     * Sample code: PrivateEndpointConnectionProxyPrivateEndpointUpdate.
     *
     * @param manager Entry point to DeviceUpdateManager.
     */
    public static void privateEndpointConnectionProxyPrivateEndpointUpdate(
        com.azure.resourcemanager.deviceupdate.DeviceUpdateManager manager) {
        manager
            .privateEndpointConnectionProxies()
            .updatePrivateEndpointPropertiesWithResponse(
                "test-rg",
                "contoso",
                "peexample01",
                new PrivateEndpointUpdate()
                    .withId(
                        "/subscriptions/11111111-1111-1111-1111-111111111111/resourceGroups/test-rg/providers/Microsoft.Network/privateEndpoints/{peName}")
                    .withLocation("westus2")
                    .withImmutableSubscriptionId("00000000-0000-0000-0000-000000000000")
                    .withImmutableResourceId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.Network/privateEndpoints/{peName}")
                    .withVnetTrafficTag("12345678"),
                Context.NONE);
    }
}
```

### PrivateEndpointConnectionProxies_Validate

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.deviceupdate.fluent.models.PrivateEndpointConnectionProxyInner;
import com.azure.resourcemanager.deviceupdate.models.PrivateLinkServiceConnection;
import com.azure.resourcemanager.deviceupdate.models.PrivateLinkServiceProxy;
import com.azure.resourcemanager.deviceupdate.models.RemotePrivateEndpoint;
import java.util.Arrays;

/** Samples for PrivateEndpointConnectionProxies Validate. */
public final class PrivateEndpointConnectionProxiesValidateSamples {
    /*
     * x-ms-original-file: specification/deviceupdate/resource-manager/Microsoft.DeviceUpdate/preview/2022-04-01-preview/examples/PrivateEndpointConnectionProxies/PrivateEndpointConnectionProxy_Validate.json
     */
    /**
     * Sample code: PrivateEndpointConnectionProxyValidate.
     *
     * @param manager Entry point to DeviceUpdateManager.
     */
    public static void privateEndpointConnectionProxyValidate(
        com.azure.resourcemanager.deviceupdate.DeviceUpdateManager manager) {
        manager
            .privateEndpointConnectionProxies()
            .validateWithResponse(
                "test-rg",
                "contoso",
                "peexample01",
                new PrivateEndpointConnectionProxyInner()
                    .withRemotePrivateEndpoint(
                        new RemotePrivateEndpoint()
                            .withId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.Network/privateEndpoints/{privateEndpointConnectionProxyId}")
                            .withLocation("westus2")
                            .withImmutableSubscriptionId("00000000-0000-0000-0000-000000000000")
                            .withImmutableResourceId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.Network/privateEndpoints/{peName}")
                            .withManualPrivateLinkServiceConnections(
                                Arrays
                                    .asList(
                                        new PrivateLinkServiceConnection()
                                            .withName("{privateEndpointConnectionProxyId}")
                                            .withGroupIds(Arrays.asList("DeviceUpdate"))
                                            .withRequestMessage("Please approve my connection, thanks.")))
                            .withPrivateLinkServiceProxies(
                                Arrays
                                    .asList(
                                        new PrivateLinkServiceProxy()
                                            .withId(
                                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.Network/privateEndpoints/{privateEndpointConnectionProxyId}/privateLinkServiceProxies/{privateEndpointConnectionProxyId}")
                                            .withGroupConnectivityInformation(Arrays.asList())))),
                Context.NONE);
    }
}
```

### PrivateEndpointConnections_CreateOrUpdate

```java
import com.azure.resourcemanager.deviceupdate.models.PrivateEndpointServiceConnectionStatus;
import com.azure.resourcemanager.deviceupdate.models.PrivateLinkServiceConnectionState;

/** Samples for PrivateEndpointConnections CreateOrUpdate. */
public final class PrivateEndpointConnectionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/deviceupdate/resource-manager/Microsoft.DeviceUpdate/preview/2022-04-01-preview/examples/PrivateEndpointConnections/PrivateEndpointConnection_CreateOrUpdate.json
     */
    /**
     * Sample code: PrivateEndpointConnectionCreateOrUpdate.
     *
     * @param manager Entry point to DeviceUpdateManager.
     */
    public static void privateEndpointConnectionCreateOrUpdate(
        com.azure.resourcemanager.deviceupdate.DeviceUpdateManager manager) {
        manager
            .privateEndpointConnections()
            .define("peexample01")
            .withExistingAccount("test-rg", "contoso")
            .withPrivateLinkServiceConnectionState(
                new PrivateLinkServiceConnectionState()
                    .withStatus(PrivateEndpointServiceConnectionStatus.APPROVED)
                    .withDescription("Auto-Approved"))
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
     * x-ms-original-file: specification/deviceupdate/resource-manager/Microsoft.DeviceUpdate/preview/2022-04-01-preview/examples/PrivateEndpointConnections/PrivateEndpointConnection_Delete.json
     */
    /**
     * Sample code: PrivateEndpointConnectionDelete.
     *
     * @param manager Entry point to DeviceUpdateManager.
     */
    public static void privateEndpointConnectionDelete(
        com.azure.resourcemanager.deviceupdate.DeviceUpdateManager manager) {
        manager.privateEndpointConnections().delete("test-rg", "contoso", "peexample01", Context.NONE);
    }
}
```

### PrivateEndpointConnections_Get

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections Get. */
public final class PrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file: specification/deviceupdate/resource-manager/Microsoft.DeviceUpdate/preview/2022-04-01-preview/examples/PrivateEndpointConnections/PrivateEndpointConnection_Get.json
     */
    /**
     * Sample code: PrivateEndpointConnectionGet.
     *
     * @param manager Entry point to DeviceUpdateManager.
     */
    public static void privateEndpointConnectionGet(
        com.azure.resourcemanager.deviceupdate.DeviceUpdateManager manager) {
        manager.privateEndpointConnections().getWithResponse("test-rg", "contoso", "peexample01", Context.NONE);
    }
}
```

### PrivateEndpointConnections_ListByAccount

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections ListByAccount. */
public final class PrivateEndpointConnectionsListByAccountSamples {
    /*
     * x-ms-original-file: specification/deviceupdate/resource-manager/Microsoft.DeviceUpdate/preview/2022-04-01-preview/examples/PrivateEndpointConnections/PrivateEndpointConnection_ListByAccount.json
     */
    /**
     * Sample code: PrivateEndpointConnectionList.
     *
     * @param manager Entry point to DeviceUpdateManager.
     */
    public static void privateEndpointConnectionList(
        com.azure.resourcemanager.deviceupdate.DeviceUpdateManager manager) {
        manager.privateEndpointConnections().listByAccount("test-rg", "contoso", Context.NONE);
    }
}
```

### PrivateLinkResources_Get

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkResources Get. */
public final class PrivateLinkResourcesGetSamples {
    /*
     * x-ms-original-file: specification/deviceupdate/resource-manager/Microsoft.DeviceUpdate/preview/2022-04-01-preview/examples/PrivateLinkResources/PrivateLinkResources_Get.json
     */
    /**
     * Sample code: PrivateLinkResourcesGet.
     *
     * @param manager Entry point to DeviceUpdateManager.
     */
    public static void privateLinkResourcesGet(com.azure.resourcemanager.deviceupdate.DeviceUpdateManager manager) {
        manager.privateLinkResources().getWithResponse("test-rg", "contoso", "adu", Context.NONE);
    }
}
```

### PrivateLinkResources_ListByAccount

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkResources ListByAccount. */
public final class PrivateLinkResourcesListByAccountSamples {
    /*
     * x-ms-original-file: specification/deviceupdate/resource-manager/Microsoft.DeviceUpdate/preview/2022-04-01-preview/examples/PrivateLinkResources/PrivateLinkResources_ListByAccount.json
     */
    /**
     * Sample code: PrivateLinkResourcesList.
     *
     * @param manager Entry point to DeviceUpdateManager.
     */
    public static void privateLinkResourcesList(com.azure.resourcemanager.deviceupdate.DeviceUpdateManager manager) {
        manager.privateLinkResources().listByAccount("test-rg", "contoso", Context.NONE);
    }
}
```

### ResourceProvider_CheckNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.deviceupdate.models.CheckNameAvailabilityRequest;

/** Samples for ResourceProvider CheckNameAvailability. */
public final class ResourceProviderCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/deviceupdate/resource-manager/Microsoft.DeviceUpdate/preview/2022-04-01-preview/examples/CheckNameAvailability_AlreadyExists.json
     */
    /**
     * Sample code: CheckNameAvailability_AlreadyExists.
     *
     * @param manager Entry point to DeviceUpdateManager.
     */
    public static void checkNameAvailabilityAlreadyExists(
        com.azure.resourcemanager.deviceupdate.DeviceUpdateManager manager) {
        manager
            .resourceProviders()
            .checkNameAvailabilityWithResponse(
                new CheckNameAvailabilityRequest().withName("contoso").withType("Microsoft.DeviceUpdate/accounts"),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/deviceupdate/resource-manager/Microsoft.DeviceUpdate/preview/2022-04-01-preview/examples/CheckNameAvailability_Available.json
     */
    /**
     * Sample code: CheckNameAvailability_Available.
     *
     * @param manager Entry point to DeviceUpdateManager.
     */
    public static void checkNameAvailabilityAvailable(
        com.azure.resourcemanager.deviceupdate.DeviceUpdateManager manager) {
        manager
            .resourceProviders()
            .checkNameAvailabilityWithResponse(
                new CheckNameAvailabilityRequest().withName("contoso").withType("Microsoft.DeviceUpdate/accounts"),
                Context.NONE);
    }
}
```

