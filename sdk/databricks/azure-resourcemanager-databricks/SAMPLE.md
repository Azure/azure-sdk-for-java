# Code snippets and samples


## Operations

- [List](#operations_list)

## OutboundNetworkDependenciesEndpoints

- [List](#outboundnetworkdependenciesendpoints_list)

## PrivateEndpointConnections

- [Create](#privateendpointconnections_create)
- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [List](#privateendpointconnections_list)

## PrivateLinkResources

- [Get](#privatelinkresources_get)
- [List](#privatelinkresources_list)

## VNetPeering

- [CreateOrUpdate](#vnetpeering_createorupdate)
- [Delete](#vnetpeering_delete)
- [Get](#vnetpeering_get)
- [ListByWorkspace](#vnetpeering_listbyworkspace)

## Workspaces

- [CreateOrUpdate](#workspaces_createorupdate)
- [Delete](#workspaces_delete)
- [GetByResourceGroup](#workspaces_getbyresourcegroup)
- [List](#workspaces_list)
- [ListByResourceGroup](#workspaces_listbyresourcegroup)
- [Update](#workspaces_update)
### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/databricks/resource-manager/Microsoft.Databricks/preview/2021-04-01-preview/examples/OperationsList.json
     */
    /**
     * Sample code: Operations.
     *
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void operations(com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### OutboundNetworkDependenciesEndpoints_List

```java
/** Samples for OutboundNetworkDependenciesEndpoints List. */
public final class OutboundNetworkDependenciesEndpointsListSamples {
    /*
     * x-ms-original-file: specification/databricks/resource-manager/Microsoft.Databricks/preview/2021-04-01-preview/examples/OutboundNetworkDependenciesEndpointsList.json
     */
    /**
     * Sample code: List OutboundNetworkDependenciesEndpoints by Workspace.
     *
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void listOutboundNetworkDependenciesEndpointsByWorkspace(
        com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager
            .outboundNetworkDependenciesEndpoints()
            .listWithResponse("myResourceGroup", "myWorkspace", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_Create

```java
import com.azure.resourcemanager.databricks.models.PrivateEndpointConnectionProperties;
import com.azure.resourcemanager.databricks.models.PrivateLinkServiceConnectionState;
import com.azure.resourcemanager.databricks.models.PrivateLinkServiceConnectionStatus;

/** Samples for PrivateEndpointConnections Create. */
public final class PrivateEndpointConnectionsCreateSamples {
    /*
     * x-ms-original-file: specification/databricks/resource-manager/Microsoft.Databricks/preview/2021-04-01-preview/examples/PrivateEndpointConnectionsUpdate.json
     */
    /**
     * Sample code: Update a private endpoint connection.
     *
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void updateAPrivateEndpointConnection(
        com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager
            .privateEndpointConnections()
            .define("myWorkspace.23456789-1111-1111-1111-111111111111")
            .withExistingWorkspace("myResourceGroup", "myWorkspace")
            .withProperties(
                new PrivateEndpointConnectionProperties()
                    .withPrivateLinkServiceConnectionState(
                        new PrivateLinkServiceConnectionState()
                            .withStatus(PrivateLinkServiceConnectionStatus.APPROVED)
                            .withDescription("Approved by databricksadmin@contoso.com")))
            .create();
    }
}
```

### PrivateEndpointConnections_Delete

```java
/** Samples for PrivateEndpointConnections Delete. */
public final class PrivateEndpointConnectionsDeleteSamples {
    /*
     * x-ms-original-file: specification/databricks/resource-manager/Microsoft.Databricks/preview/2021-04-01-preview/examples/PrivateEndpointConnectionsDelete.json
     */
    /**
     * Sample code: Remove a private endpoint connection.
     *
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void removeAPrivateEndpointConnection(
        com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager
            .privateEndpointConnections()
            .delete(
                "myResourceGroup",
                "myWorkspace",
                "myWorkspace.23456789-1111-1111-1111-111111111111",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_Get

```java
/** Samples for PrivateEndpointConnections Get. */
public final class PrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file: specification/databricks/resource-manager/Microsoft.Databricks/preview/2021-04-01-preview/examples/PrivateEndpointConnectionsGet.json
     */
    /**
     * Sample code: Get a private endpoint connection.
     *
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void getAPrivateEndpointConnection(
        com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager
            .privateEndpointConnections()
            .getWithResponse(
                "myResourceGroup",
                "myWorkspace",
                "myWorkspace.23456789-1111-1111-1111-111111111111",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_List

```java
/** Samples for PrivateEndpointConnections List. */
public final class PrivateEndpointConnectionsListSamples {
    /*
     * x-ms-original-file: specification/databricks/resource-manager/Microsoft.Databricks/preview/2021-04-01-preview/examples/ListPrivateEndpointConnections.json
     */
    /**
     * Sample code: List private endpoint connections.
     *
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void listPrivateEndpointConnections(
        com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager.privateEndpointConnections().list("myResourceGroup", "myWorkspace", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkResources_Get

```java
/** Samples for PrivateLinkResources Get. */
public final class PrivateLinkResourcesGetSamples {
    /*
     * x-ms-original-file: specification/databricks/resource-manager/Microsoft.Databricks/preview/2021-04-01-preview/examples/PrivateLinkResourcesGet.json
     */
    /**
     * Sample code: Get a private link resource.
     *
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void getAPrivateLinkResource(com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager
            .privateLinkResources()
            .getWithResponse("myResourceGroup", "myWorkspace", "databricks_ui_api", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkResources_List

```java
/** Samples for PrivateLinkResources List. */
public final class PrivateLinkResourcesListSamples {
    /*
     * x-ms-original-file: specification/databricks/resource-manager/Microsoft.Databricks/preview/2021-04-01-preview/examples/ListPrivateLinkResources.json
     */
    /**
     * Sample code: List private link resources.
     *
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void listPrivateLinkResources(com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager.privateLinkResources().list("myResourceGroup", "myWorkspace", com.azure.core.util.Context.NONE);
    }
}
```

### VNetPeering_CreateOrUpdate

```java
import com.azure.resourcemanager.databricks.models.VirtualNetworkPeeringPropertiesFormatRemoteVirtualNetwork;

/** Samples for VNetPeering CreateOrUpdate. */
public final class VNetPeeringCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/databricks/resource-manager/Microsoft.Databricks/preview/2021-04-01-preview/examples/WorkspaceVirtualNetworkPeeringCreateOrUpdate.json
     */
    /**
     * Sample code: Create vNet Peering for Workspace.
     *
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void createVNetPeeringForWorkspace(
        com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager
            .vNetPeerings()
            .define("vNetPeeringTest")
            .withExistingWorkspace("rg", "myWorkspace")
            .withRemoteVirtualNetwork(
                new VirtualNetworkPeeringPropertiesFormatRemoteVirtualNetwork()
                    .withId(
                        "/subscriptions/0140911e-1040-48da-8bc9-b99fb3dd88a6/resourceGroups/subramantest/providers/Microsoft.Network/virtualNetworks/subramanvnet"))
            .withAllowVirtualNetworkAccess(true)
            .withAllowForwardedTraffic(false)
            .withAllowGatewayTransit(false)
            .withUseRemoteGateways(false)
            .create();
    }
}
```

### VNetPeering_Delete

```java
/** Samples for VNetPeering Delete. */
public final class VNetPeeringDeleteSamples {
    /*
     * x-ms-original-file: specification/databricks/resource-manager/Microsoft.Databricks/preview/2021-04-01-preview/examples/WorkspaceVirtualNetworkPeeringDelete.json
     */
    /**
     * Sample code: Delete a workspace vNet Peering.
     *
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void deleteAWorkspaceVNetPeering(
        com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager.vNetPeerings().delete("rg", "myWorkspace", "vNetPeering", com.azure.core.util.Context.NONE);
    }
}
```

### VNetPeering_Get

```java
/** Samples for VNetPeering Get. */
public final class VNetPeeringGetSamples {
    /*
     * x-ms-original-file: specification/databricks/resource-manager/Microsoft.Databricks/preview/2021-04-01-preview/examples/WorkspaceVirtualNetPeeringGet.json
     */
    /**
     * Sample code: Get a workspace with vNet Peering Configured.
     *
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void getAWorkspaceWithVNetPeeringConfigured(
        com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager.vNetPeerings().getWithResponse("rg", "myWorkspace", "vNetPeering", com.azure.core.util.Context.NONE);
    }
}
```

### VNetPeering_ListByWorkspace

```java
/** Samples for VNetPeering ListByWorkspace. */
public final class VNetPeeringListByWorkspaceSamples {
    /*
     * x-ms-original-file: specification/databricks/resource-manager/Microsoft.Databricks/preview/2021-04-01-preview/examples/WorkspaceVirtualNetPeeringList.json
     */
    /**
     * Sample code: List all vNet Peerings for the workspace.
     *
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void listAllVNetPeeringsForTheWorkspace(
        com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager.vNetPeerings().listByWorkspace("rg", "myWorkspace", com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_CreateOrUpdate

```java
import com.azure.resourcemanager.databricks.models.Encryption;
import com.azure.resourcemanager.databricks.models.KeySource;
import com.azure.resourcemanager.databricks.models.WorkspaceCustomBooleanParameter;
import com.azure.resourcemanager.databricks.models.WorkspaceCustomParameters;
import com.azure.resourcemanager.databricks.models.WorkspaceCustomStringParameter;
import com.azure.resourcemanager.databricks.models.WorkspaceEncryptionParameter;

/** Samples for Workspaces CreateOrUpdate. */
public final class WorkspacesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/databricks/resource-manager/Microsoft.Databricks/preview/2021-04-01-preview/examples/DisableEncryption.json
     */
    /**
     * Sample code: Revert Customer-Managed Key (CMK) encryption to Microsoft Managed Keys encryption on a workspace.
     *
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void revertCustomerManagedKeyCMKEncryptionToMicrosoftManagedKeysEncryptionOnAWorkspace(
        com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager
            .workspaces()
            .define("myWorkspace")
            .withRegion("westus")
            .withExistingResourceGroup("rg")
            .withManagedResourceGroupId("/subscriptions/subid/resourceGroups/myManagedRG")
            .withParameters(
                new WorkspaceCustomParameters()
                    .withEncryption(
                        new WorkspaceEncryptionParameter()
                            .withValue(new Encryption().withKeySource(KeySource.DEFAULT))))
            .create();
    }

    /*
     * x-ms-original-file: specification/databricks/resource-manager/Microsoft.Databricks/preview/2021-04-01-preview/examples/PrepareEncryption.json
     */
    /**
     * Sample code: Create a workspace which is ready for Customer-Managed Key (CMK) encryption.
     *
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void createAWorkspaceWhichIsReadyForCustomerManagedKeyCMKEncryption(
        com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager
            .workspaces()
            .define("myWorkspace")
            .withRegion("westus")
            .withExistingResourceGroup("rg")
            .withManagedResourceGroupId("/subscriptions/subid/resourceGroups/myManagedRG")
            .withParameters(
                new WorkspaceCustomParameters()
                    .withPrepareEncryption(new WorkspaceCustomBooleanParameter().withValue(true)))
            .create();
    }

    /*
     * x-ms-original-file: specification/databricks/resource-manager/Microsoft.Databricks/preview/2021-04-01-preview/examples/WorkspaceCreateWithParameters.json
     */
    /**
     * Sample code: Create or update workspace with custom parameters.
     *
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void createOrUpdateWorkspaceWithCustomParameters(
        com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager
            .workspaces()
            .define("myWorkspace")
            .withRegion("westus")
            .withExistingResourceGroup("rg")
            .withManagedResourceGroupId("/subscriptions/subid/resourceGroups/myManagedRG")
            .withParameters(
                new WorkspaceCustomParameters()
                    .withCustomVirtualNetworkId(
                        new WorkspaceCustomStringParameter()
                            .withValue(
                                "/subscriptions/subid/resourceGroups/rg/providers/Microsoft.Network/virtualNetworks/myNetwork"))
                    .withCustomPublicSubnetName(new WorkspaceCustomStringParameter().withValue("myPublicSubnet"))
                    .withCustomPrivateSubnetName(new WorkspaceCustomStringParameter().withValue("myPrivateSubnet")))
            .create();
    }

    /*
     * x-ms-original-file: specification/databricks/resource-manager/Microsoft.Databricks/preview/2021-04-01-preview/examples/WorkspaceCreate.json
     */
    /**
     * Sample code: Create or update workspace.
     *
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void createOrUpdateWorkspace(com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager
            .workspaces()
            .define("myWorkspace")
            .withRegion("westus")
            .withExistingResourceGroup("rg")
            .withManagedResourceGroupId("/subscriptions/subid/resourceGroups/myManagedRG")
            .create();
    }

    /*
     * x-ms-original-file: specification/databricks/resource-manager/Microsoft.Databricks/preview/2021-04-01-preview/examples/EnableEncryption.json
     */
    /**
     * Sample code: Enable Customer-Managed Key (CMK) encryption on a workspace which is prepared for encryption.
     *
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void enableCustomerManagedKeyCMKEncryptionOnAWorkspaceWhichIsPreparedForEncryption(
        com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager
            .workspaces()
            .define("myWorkspace")
            .withRegion("westus")
            .withExistingResourceGroup("rg")
            .withManagedResourceGroupId("/subscriptions/subid/resourceGroups/myManagedRG")
            .withParameters(
                new WorkspaceCustomParameters()
                    .withPrepareEncryption(new WorkspaceCustomBooleanParameter().withValue(true))
                    .withEncryption(
                        new WorkspaceEncryptionParameter()
                            .withValue(
                                new Encryption()
                                    .withKeySource(KeySource.MICROSOFT_KEYVAULT)
                                    .withKeyName("fakeTokenPlaceholder")
                                    .withKeyVersion("fakeTokenPlaceholder")
                                    .withKeyVaultUri("fakeTokenPlaceholder"))))
            .create();
    }
}
```

### Workspaces_Delete

```java
/** Samples for Workspaces Delete. */
public final class WorkspacesDeleteSamples {
    /*
     * x-ms-original-file: specification/databricks/resource-manager/Microsoft.Databricks/preview/2021-04-01-preview/examples/WorkspaceDelete.json
     */
    /**
     * Sample code: Delete a workspace.
     *
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void deleteAWorkspace(com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager.workspaces().delete("rg", "myWorkspace", com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_GetByResourceGroup

```java
/** Samples for Workspaces GetByResourceGroup. */
public final class WorkspacesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/databricks/resource-manager/Microsoft.Databricks/preview/2021-04-01-preview/examples/WorkspaceGet.json
     */
    /**
     * Sample code: Get a workspace.
     *
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void getAWorkspace(com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager.workspaces().getByResourceGroupWithResponse("rg", "myWorkspace", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/databricks/resource-manager/Microsoft.Databricks/preview/2021-04-01-preview/examples/WorkspaceGetParameters.json
     */
    /**
     * Sample code: Get a workspace with custom parameters.
     *
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void getAWorkspaceWithCustomParameters(
        com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager.workspaces().getByResourceGroupWithResponse("rg", "myWorkspace", com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_List

```java
/** Samples for Workspaces List. */
public final class WorkspacesListSamples {
    /*
     * x-ms-original-file: specification/databricks/resource-manager/Microsoft.Databricks/preview/2021-04-01-preview/examples/WorkspacesListBySubscription.json
     */
    /**
     * Sample code: Lists workspaces.
     *
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void listsWorkspaces(com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager.workspaces().list(com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_ListByResourceGroup

```java
/** Samples for Workspaces ListByResourceGroup. */
public final class WorkspacesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/databricks/resource-manager/Microsoft.Databricks/preview/2021-04-01-preview/examples/WorkspacesListByResourceGroup.json
     */
    /**
     * Sample code: Lists workspaces.
     *
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void listsWorkspaces(com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager.workspaces().listByResourceGroup("rg", com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_Update

```java
import com.azure.resourcemanager.databricks.models.Workspace;
import java.util.HashMap;
import java.util.Map;

/** Samples for Workspaces Update. */
public final class WorkspacesUpdateSamples {
    /*
     * x-ms-original-file: specification/databricks/resource-manager/Microsoft.Databricks/preview/2021-04-01-preview/examples/WorkspaceUpdate.json
     */
    /**
     * Sample code: Update a workspace's tags.
     *
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void updateAWorkspaceSTags(com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        Workspace resource =
            manager
                .workspaces()
                .getByResourceGroupWithResponse("rg", "myWorkspace", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("mytag1", "myvalue1")).apply();
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

