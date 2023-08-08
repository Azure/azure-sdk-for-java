# Code snippets and samples


## MachineExtensions

- [CreateOrUpdate](#machineextensions_createorupdate)
- [Delete](#machineextensions_delete)
- [Get](#machineextensions_get)
- [List](#machineextensions_list)
- [Update](#machineextensions_update)

## Machines

- [Delete](#machines_delete)
- [GetByResourceGroup](#machines_getbyresourcegroup)
- [List](#machines_list)
- [ListByResourceGroup](#machines_listbyresourcegroup)

## PrivateEndpointConnections

- [CreateOrUpdate](#privateendpointconnections_createorupdate)
- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [ListByPrivateLinkScope](#privateendpointconnections_listbyprivatelinkscope)

## PrivateLinkResources

- [Get](#privatelinkresources_get)
- [ListByPrivateLinkScope](#privatelinkresources_listbyprivatelinkscope)

## PrivateLinkScopes

- [CreateOrUpdate](#privatelinkscopes_createorupdate)
- [Delete](#privatelinkscopes_delete)
- [GetByResourceGroup](#privatelinkscopes_getbyresourcegroup)
- [GetValidationDetails](#privatelinkscopes_getvalidationdetails)
- [GetValidationDetailsForMachine](#privatelinkscopes_getvalidationdetailsformachine)
- [List](#privatelinkscopes_list)
- [ListByResourceGroup](#privatelinkscopes_listbyresourcegroup)
- [UpdateTags](#privatelinkscopes_updatetags)
### MachineExtensions_CreateOrUpdate

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.hybridcompute.models.MachineExtensionProperties;
import java.io.IOException;

/** Samples for MachineExtensions CreateOrUpdate. */
public final class MachineExtensionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2021-03-25-preview/examples/PUTExtension.json
     */
    /**
     * Sample code: Create or Update a Machine Extension.
     *
     * @param manager Entry point to HybridComputeManager.
     */
    public static void createOrUpdateAMachineExtension(
        com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) throws IOException {
        manager
            .machineExtensions()
            .define("CustomScriptExtension")
            .withRegion("eastus2euap")
            .withExistingMachine("myResourceGroup", "myMachine")
            .withProperties(
                new MachineExtensionProperties()
                    .withPublisher("Microsoft.Compute")
                    .withType("CustomScriptExtension")
                    .withTypeHandlerVersion("1.10")
                    .withSettings(
                        SerializerFactory
                            .createDefaultManagementSerializerAdapter()
                            .deserialize(
                                "{\"commandToExecute\":\"powershell.exe -c \\\"Get-Process | Where-Object { $_.CPU -gt"
                                    + " 10000 }\\\"\"}",
                                Object.class,
                                SerializerEncoding.JSON)))
            .create();
    }
}
```

### MachineExtensions_Delete

```java
/** Samples for MachineExtensions Delete. */
public final class MachineExtensionsDeleteSamples {
    /*
     * x-ms-original-file: specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2021-03-25-preview/examples/DELETEExtension.json
     */
    /**
     * Sample code: Delete a Machine Extension.
     *
     * @param manager Entry point to HybridComputeManager.
     */
    public static void deleteAMachineExtension(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.machineExtensions().delete("myResourceGroup", "myMachine", "MMA", com.azure.core.util.Context.NONE);
    }
}
```

### MachineExtensions_Get

```java
/** Samples for MachineExtensions Get. */
public final class MachineExtensionsGetSamples {
    /*
     * x-ms-original-file: specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2021-03-25-preview/examples/GETExtension.json
     */
    /**
     * Sample code: GET Machine Extension.
     *
     * @param manager Entry point to HybridComputeManager.
     */
    public static void gETMachineExtension(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager
            .machineExtensions()
            .getWithResponse("myResourceGroup", "myMachine", "CustomScriptExtension", com.azure.core.util.Context.NONE);
    }
}
```

### MachineExtensions_List

```java
/** Samples for MachineExtensions List. */
public final class MachineExtensionsListSamples {
    /*
     * x-ms-original-file: specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2021-03-25-preview/examples/LISTExtension.json
     */
    /**
     * Sample code: GET all Machine Extensions.
     *
     * @param manager Entry point to HybridComputeManager.
     */
    public static void gETAllMachineExtensions(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.machineExtensions().list("myResourceGroup", "myMachine", null, com.azure.core.util.Context.NONE);
    }
}
```

### MachineExtensions_Update

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.hybridcompute.models.MachineExtension;
import com.azure.resourcemanager.hybridcompute.models.MachineExtensionUpdateProperties;
import java.io.IOException;

/** Samples for MachineExtensions Update. */
public final class MachineExtensionsUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2021-03-25-preview/examples/UpdateExtension.json
     */
    /**
     * Sample code: Create or Update a Machine Extension.
     *
     * @param manager Entry point to HybridComputeManager.
     */
    public static void createOrUpdateAMachineExtension(
        com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) throws IOException {
        MachineExtension resource =
            manager
                .machineExtensions()
                .getWithResponse(
                    "myResourceGroup", "myMachine", "CustomScriptExtension", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withProperties(
                new MachineExtensionUpdateProperties()
                    .withPublisher("Microsoft.Compute")
                    .withType("CustomScriptExtension")
                    .withTypeHandlerVersion("1.10")
                    .withSettings(
                        SerializerFactory
                            .createDefaultManagementSerializerAdapter()
                            .deserialize(
                                "{\"commandToExecute\":\"powershell.exe -c \\\"Get-Process | Where-Object { $_.CPU -lt"
                                    + " 100 }\\\"\"}",
                                Object.class,
                                SerializerEncoding.JSON)))
            .apply();
    }
}
```

### Machines_Delete

```java
/** Samples for Machines Delete. */
public final class MachinesDeleteSamples {
    /*
     * x-ms-original-file: specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2021-03-25-preview/examples/Machines_Delete.json
     */
    /**
     * Sample code: Delete a Machine.
     *
     * @param manager Entry point to HybridComputeManager.
     */
    public static void deleteAMachine(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager
            .machines()
            .deleteByResourceGroupWithResponse("myResourceGroup", "myMachine", com.azure.core.util.Context.NONE);
    }
}
```

### Machines_GetByResourceGroup

```java
/** Samples for Machines GetByResourceGroup. */
public final class MachinesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2021-03-25-preview/examples/Machines_Get.json
     */
    /**
     * Sample code: Get Machine.
     *
     * @param manager Entry point to HybridComputeManager.
     */
    public static void getMachine(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager
            .machines()
            .getByResourceGroupWithResponse("myResourceGroup", "myMachine", null, com.azure.core.util.Context.NONE);
    }
}
```

### Machines_List

```java
/** Samples for Machines List. */
public final class MachinesListSamples {
    /*
     * x-ms-original-file: specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2021-03-25-preview/examples/Machines_ListBySubscription.json
     */
    /**
     * Sample code: List Machines by resource group.
     *
     * @param manager Entry point to HybridComputeManager.
     */
    public static void listMachinesByResourceGroup(
        com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.machines().list(com.azure.core.util.Context.NONE);
    }
}
```

### Machines_ListByResourceGroup

```java
/** Samples for Machines ListByResourceGroup. */
public final class MachinesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2021-03-25-preview/examples/Machines_ListByResourceGroup.json
     */
    /**
     * Sample code: List Machines by resource group.
     *
     * @param manager Entry point to HybridComputeManager.
     */
    public static void listMachinesByResourceGroup(
        com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.machines().listByResourceGroup("myResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_CreateOrUpdate

```java
import com.azure.resourcemanager.hybridcompute.models.PrivateEndpointConnectionProperties;
import com.azure.resourcemanager.hybridcompute.models.PrivateLinkServiceConnectionStateProperty;

/** Samples for PrivateEndpointConnections CreateOrUpdate. */
public final class PrivateEndpointConnectionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2021-03-25-preview/examples/PrivateEndpointConnectionUpdate.json
     */
    /**
     * Sample code: Approve or reject a private endpoint connection with a given name.
     *
     * @param manager Entry point to HybridComputeManager.
     */
    public static void approveOrRejectAPrivateEndpointConnectionWithAGivenName(
        com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager
            .privateEndpointConnections()
            .define("private-endpoint-connection-name")
            .withExistingPrivateLinkScope("myResourceGroup", "myPrivateLinkScope")
            .withProperties(
                new PrivateEndpointConnectionProperties()
                    .withPrivateLinkServiceConnectionState(
                        new PrivateLinkServiceConnectionStateProperty()
                            .withStatus("Approved")
                            .withDescription("Approved by johndoe@contoso.com")))
            .create();
    }
}
```

### PrivateEndpointConnections_Delete

```java
/** Samples for PrivateEndpointConnections Delete. */
public final class PrivateEndpointConnectionsDeleteSamples {
    /*
     * x-ms-original-file: specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2021-03-25-preview/examples/PrivateEndpointConnectionDelete.json
     */
    /**
     * Sample code: Deletes a private endpoint connection with a given name.
     *
     * @param manager Entry point to HybridComputeManager.
     */
    public static void deletesAPrivateEndpointConnectionWithAGivenName(
        com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager
            .privateEndpointConnections()
            .delete(
                "myResourceGroup",
                "myPrivateLinkScope",
                "private-endpoint-connection-name",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_Get

```java
/** Samples for PrivateEndpointConnections Get. */
public final class PrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file: specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2021-03-25-preview/examples/PrivateEndpointConnectionGet.json
     */
    /**
     * Sample code: Gets private endpoint connection.
     *
     * @param manager Entry point to HybridComputeManager.
     */
    public static void getsPrivateEndpointConnection(
        com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager
            .privateEndpointConnections()
            .getWithResponse(
                "myResourceGroup",
                "myPrivateLinkScope",
                "private-endpoint-connection-name",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_ListByPrivateLinkScope

```java
/** Samples for PrivateEndpointConnections ListByPrivateLinkScope. */
public final class PrivateEndpointConnectionsListByPrivateLinkScopeSamples {
    /*
     * x-ms-original-file: specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2021-03-25-preview/examples/PrivateEndpointConnectionList.json
     */
    /**
     * Sample code: Gets list of private endpoint connections on a private link scope.
     *
     * @param manager Entry point to HybridComputeManager.
     */
    public static void getsListOfPrivateEndpointConnectionsOnAPrivateLinkScope(
        com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager
            .privateEndpointConnections()
            .listByPrivateLinkScope("myResourceGroup", "myPrivateLinkScope", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkResources_Get

```java
/** Samples for PrivateLinkResources Get. */
public final class PrivateLinkResourcesGetSamples {
    /*
     * x-ms-original-file: specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2021-03-25-preview/examples/PrivateLinkScopePrivateLinkResourceGet.json
     */
    /**
     * Sample code: Gets private endpoint connection.
     *
     * @param manager Entry point to HybridComputeManager.
     */
    public static void getsPrivateEndpointConnection(
        com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager
            .privateLinkResources()
            .getWithResponse(
                "myResourceGroup", "myPrivateLinkScope", "hybridcompute", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkResources_ListByPrivateLinkScope

```java
/** Samples for PrivateLinkResources ListByPrivateLinkScope. */
public final class PrivateLinkResourcesListByPrivateLinkScopeSamples {
    /*
     * x-ms-original-file: specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2021-03-25-preview/examples/PrivateLinkScopePrivateLinkResourceListGet.json
     */
    /**
     * Sample code: Gets private endpoint connection.
     *
     * @param manager Entry point to HybridComputeManager.
     */
    public static void getsPrivateEndpointConnection(
        com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager
            .privateLinkResources()
            .listByPrivateLinkScope("myResourceGroup", "myPrivateLinkScope", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkScopes_CreateOrUpdate

```java
import java.util.HashMap;
import java.util.Map;

/** Samples for PrivateLinkScopes CreateOrUpdate. */
public final class PrivateLinkScopesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2021-03-25-preview/examples/PrivateLinkScopesCreate.json
     */
    /**
     * Sample code: PrivateLinkScopeCreate.
     *
     * @param manager Entry point to HybridComputeManager.
     */
    public static void privateLinkScopeCreate(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager
            .privateLinkScopes()
            .define("my-privatelinkscope")
            .withRegion("westus")
            .withExistingResourceGroup("my-resource-group")
            .create();
    }

    /*
     * x-ms-original-file: specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2021-03-25-preview/examples/PrivateLinkScopesUpdate.json
     */
    /**
     * Sample code: PrivateLinkScopeUpdate.
     *
     * @param manager Entry point to HybridComputeManager.
     */
    public static void privateLinkScopeUpdate(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager
            .privateLinkScopes()
            .define("my-privatelinkscope")
            .withRegion("westus")
            .withExistingResourceGroup("my-resource-group")
            .withTags(mapOf("Tag1", "Value1"))
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

### PrivateLinkScopes_Delete

```java
/** Samples for PrivateLinkScopes Delete. */
public final class PrivateLinkScopesDeleteSamples {
    /*
     * x-ms-original-file: specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2021-03-25-preview/examples/PrivateLinkScopesDelete.json
     */
    /**
     * Sample code: PrivateLinkScopesDelete.
     *
     * @param manager Entry point to HybridComputeManager.
     */
    public static void privateLinkScopesDelete(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager
            .privateLinkScopes()
            .delete("my-resource-group", "my-privatelinkscope", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkScopes_GetByResourceGroup

```java
/** Samples for PrivateLinkScopes GetByResourceGroup. */
public final class PrivateLinkScopesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2021-03-25-preview/examples/PrivateLinkScopesGet.json
     */
    /**
     * Sample code: PrivateLinkScopeGet.
     *
     * @param manager Entry point to HybridComputeManager.
     */
    public static void privateLinkScopeGet(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager
            .privateLinkScopes()
            .getByResourceGroupWithResponse(
                "my-resource-group", "my-privatelinkscope", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkScopes_GetValidationDetails

```java
/** Samples for PrivateLinkScopes GetValidationDetails. */
public final class PrivateLinkScopesGetValidationDetailsSamples {
    /*
     * x-ms-original-file: specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2021-03-25-preview/examples/PrivateLinkScopesGetValidation.json
     */
    /**
     * Sample code: PrivateLinkScopeGet.
     *
     * @param manager Entry point to HybridComputeManager.
     */
    public static void privateLinkScopeGet(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager
            .privateLinkScopes()
            .getValidationDetailsWithResponse(
                "wus2", "f5dc51d3-92ed-4d7e-947a-775ea79b4919", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkScopes_GetValidationDetailsForMachine

```java
/** Samples for PrivateLinkScopes GetValidationDetailsForMachine. */
public final class PrivateLinkScopesGetValidationDetailsForMachineSamples {
    /*
     * x-ms-original-file: specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2021-03-25-preview/examples/PrivateLinkScopesGetValidationForMachine.json
     */
    /**
     * Sample code: PrivateLinkScopeGet.
     *
     * @param manager Entry point to HybridComputeManager.
     */
    public static void privateLinkScopeGet(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager
            .privateLinkScopes()
            .getValidationDetailsForMachineWithResponse(
                "my-resource-group", "machineName", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkScopes_List

```java
/** Samples for PrivateLinkScopes List. */
public final class PrivateLinkScopesListSamples {
    /*
     * x-ms-original-file: specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2021-03-25-preview/examples/PrivateLinkScopesList.json
     */
    /**
     * Sample code: PrivateLinkScopesList.json.
     *
     * @param manager Entry point to HybridComputeManager.
     */
    public static void privateLinkScopesListJson(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.privateLinkScopes().list(com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkScopes_ListByResourceGroup

```java
/** Samples for PrivateLinkScopes ListByResourceGroup. */
public final class PrivateLinkScopesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2021-03-25-preview/examples/PrivateLinkScopesListByResourceGroup.json
     */
    /**
     * Sample code: PrivateLinkScopeListByResourceGroup.
     *
     * @param manager Entry point to HybridComputeManager.
     */
    public static void privateLinkScopeListByResourceGroup(
        com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.privateLinkScopes().listByResourceGroup("my-resource-group", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkScopes_UpdateTags

```java
import com.azure.resourcemanager.hybridcompute.models.HybridComputePrivateLinkScope;
import java.util.HashMap;
import java.util.Map;

/** Samples for PrivateLinkScopes UpdateTags. */
public final class PrivateLinkScopesUpdateTagsSamples {
    /*
     * x-ms-original-file: specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2021-03-25-preview/examples/PrivateLinkScopesUpdateTagsOnly.json
     */
    /**
     * Sample code: PrivateLinkScopeUpdateTagsOnly.
     *
     * @param manager Entry point to HybridComputeManager.
     */
    public static void privateLinkScopeUpdateTagsOnly(
        com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        HybridComputePrivateLinkScope resource =
            manager
                .privateLinkScopes()
                .getByResourceGroupWithResponse(
                    "my-resource-group", "my-privatelinkscope", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("Tag1", "Value1", "Tag2", "Value2")).apply();
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

