# Code snippets and samples


## AccessConnectors

- [CreateOrUpdate](#accessconnectors_createorupdate)
- [Delete](#accessconnectors_delete)
- [GetByResourceGroup](#accessconnectors_getbyresourcegroup)
- [List](#accessconnectors_list)
- [ListByResourceGroup](#accessconnectors_listbyresourcegroup)
- [Update](#accessconnectors_update)

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
### AccessConnectors_CreateOrUpdate

```java
/**
 * Samples for AccessConnectors CreateOrUpdate.
 */
public final class AccessConnectorsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/databricks/resource-manager/Microsoft.Databricks/preview/2024-09-01-preview/examples/
     * AccessConnectorCreateOrUpdate.json
     */
    /**
     * Sample code: Create an Azure Databricks Access Connector with SystemAssigned Identity.
     * 
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void createAnAzureDatabricksAccessConnectorWithSystemAssignedIdentity(
        com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager.accessConnectors()
            .define("myAccessConnector")
            .withRegion("westus")
            .withExistingResourceGroup("rg")
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/databricks/resource-manager/Microsoft.Databricks/preview/2024-09-01-preview/examples/
     * AccessConnectorCreateOrUpdateWithUserAssigned.json
     */
    /**
     * Sample code: Create an Azure Databricks Access Connector with UserAssigned Identity.
     * 
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void createAnAzureDatabricksAccessConnectorWithUserAssignedIdentity(
        com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager.accessConnectors()
            .define("myAccessConnector")
            .withRegion("westus")
            .withExistingResourceGroup("rg")
            .create();
    }
}
```

### AccessConnectors_Delete

```java
/**
 * Samples for AccessConnectors Delete.
 */
public final class AccessConnectorsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/databricks/resource-manager/Microsoft.Databricks/preview/2024-09-01-preview/examples/
     * AccessConnectorDelete.json
     */
    /**
     * Sample code: Delete an Azure Databricks Access Connector.
     * 
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void
        deleteAnAzureDatabricksAccessConnector(com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager.accessConnectors().delete("rg", "myAccessConnector", com.azure.core.util.Context.NONE);
    }
}
```

### AccessConnectors_GetByResourceGroup

```java
/**
 * Samples for AccessConnectors GetByResourceGroup.
 */
public final class AccessConnectorsGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/databricks/resource-manager/Microsoft.Databricks/preview/2024-09-01-preview/examples/
     * AccessConnectorGet.json
     */
    /**
     * Sample code: Get an Azure Databricks Access Connector.
     * 
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void
        getAnAzureDatabricksAccessConnector(com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager.accessConnectors()
            .getByResourceGroupWithResponse("rg", "myAccessConnector", com.azure.core.util.Context.NONE);
    }
}
```

### AccessConnectors_List

```java
/**
 * Samples for AccessConnectors List.
 */
public final class AccessConnectorsListSamples {
    /*
     * x-ms-original-file:
     * specification/databricks/resource-manager/Microsoft.Databricks/preview/2024-09-01-preview/examples/
     * AccessConnectorsListBySubscriptionId.json
     */
    /**
     * Sample code: Lists all the Azure Databricks Access Connectors within a subscription.
     * 
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void listsAllTheAzureDatabricksAccessConnectorsWithinASubscription(
        com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager.accessConnectors().list(com.azure.core.util.Context.NONE);
    }
}
```

### AccessConnectors_ListByResourceGroup

```java
/**
 * Samples for AccessConnectors ListByResourceGroup.
 */
public final class AccessConnectorsListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/databricks/resource-manager/Microsoft.Databricks/preview/2024-09-01-preview/examples/
     * AccessConnectorsListByResourceGroup.json
     */
    /**
     * Sample code: Lists Azure Databricks Access Connectors within a resource group.
     * 
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void listsAzureDatabricksAccessConnectorsWithinAResourceGroup(
        com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager.accessConnectors().listByResourceGroup("rg", com.azure.core.util.Context.NONE);
    }
}
```

### AccessConnectors_Update

```java
import com.azure.resourcemanager.databricks.models.AccessConnector;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for AccessConnectors Update.
 */
public final class AccessConnectorsUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/databricks/resource-manager/Microsoft.Databricks/preview/2024-09-01-preview/examples/
     * AccessConnectorPatchUpdate.json
     */
    /**
     * Sample code: Update an Azure Databricks Access Connector.
     * 
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void
        updateAnAzureDatabricksAccessConnector(com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        AccessConnector resource = manager.accessConnectors()
            .getByResourceGroupWithResponse("rg", "myAccessConnector", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("key1", "fakeTokenPlaceholder")).apply();
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
     * x-ms-original-file:
     * specification/databricks/resource-manager/Microsoft.Databricks/preview/2024-09-01-preview/examples/OperationsList
     * .json
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
/**
 * Samples for OutboundNetworkDependenciesEndpoints List.
 */
public final class OutboundNetworkDependenciesEndpointsListSamples {
    /*
     * x-ms-original-file:
     * specification/databricks/resource-manager/Microsoft.Databricks/preview/2024-09-01-preview/examples/
     * OutboundNetworkDependenciesEndpointsList.json
     */
    /**
     * Sample code: List OutboundNetworkDependenciesEndpoints by Workspace.
     * 
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void listOutboundNetworkDependenciesEndpointsByWorkspace(
        com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager.outboundNetworkDependenciesEndpoints()
            .listWithResponse("myResourceGroup", "myWorkspace", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_Create

```java
import com.azure.resourcemanager.databricks.models.PrivateEndpointConnectionProperties;
import com.azure.resourcemanager.databricks.models.PrivateLinkServiceConnectionState;
import com.azure.resourcemanager.databricks.models.PrivateLinkServiceConnectionStatus;

/**
 * Samples for PrivateEndpointConnections Create.
 */
public final class PrivateEndpointConnectionsCreateSamples {
    /*
     * x-ms-original-file:
     * specification/databricks/resource-manager/Microsoft.Databricks/preview/2024-09-01-preview/examples/
     * PrivateEndpointConnectionsUpdate.json
     */
    /**
     * Sample code: Update a private endpoint connection.
     * 
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void
        updateAPrivateEndpointConnection(com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager.privateEndpointConnections()
            .define("myWorkspace.23456789-1111-1111-1111-111111111111")
            .withExistingWorkspace("myResourceGroup", "myWorkspace")
            .withProperties(new PrivateEndpointConnectionProperties().withPrivateLinkServiceConnectionState(
                new PrivateLinkServiceConnectionState().withStatus(PrivateLinkServiceConnectionStatus.APPROVED)
                    .withDescription("Approved by databricksadmin@contoso.com")))
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
     * x-ms-original-file:
     * specification/databricks/resource-manager/Microsoft.Databricks/preview/2024-09-01-preview/examples/
     * PrivateEndpointConnectionsDelete.json
     */
    /**
     * Sample code: Remove a private endpoint connection.
     * 
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void
        removeAPrivateEndpointConnection(com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager.privateEndpointConnections()
            .delete("myResourceGroup", "myWorkspace", "myWorkspace.23456789-1111-1111-1111-111111111111",
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
     * x-ms-original-file:
     * specification/databricks/resource-manager/Microsoft.Databricks/preview/2024-09-01-preview/examples/
     * PrivateEndpointConnectionsGet.json
     */
    /**
     * Sample code: Get a private endpoint connection.
     * 
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void
        getAPrivateEndpointConnection(com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager.privateEndpointConnections()
            .getWithResponse("myResourceGroup", "myWorkspace", "myWorkspace.23456789-1111-1111-1111-111111111111",
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
     * x-ms-original-file:
     * specification/databricks/resource-manager/Microsoft.Databricks/preview/2024-09-01-preview/examples/
     * ListPrivateEndpointConnections.json
     */
    /**
     * Sample code: List private endpoint connections.
     * 
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void
        listPrivateEndpointConnections(com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager.privateEndpointConnections().list("myResourceGroup", "myWorkspace", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file:
     * specification/databricks/resource-manager/Microsoft.Databricks/preview/2024-09-01-preview/examples/
     * PrivateLinkResourcesGet.json
     */
    /**
     * Sample code: Get a private link resource.
     * 
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void getAPrivateLinkResource(com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager.privateLinkResources()
            .getWithResponse("myResourceGroup", "myWorkspace", "databricks_ui_api", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file:
     * specification/databricks/resource-manager/Microsoft.Databricks/preview/2024-09-01-preview/examples/
     * ListPrivateLinkResources.json
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

/**
 * Samples for VNetPeering CreateOrUpdate.
 */
public final class VNetPeeringCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/databricks/resource-manager/Microsoft.Databricks/preview/2024-09-01-preview/examples/
     * WorkspaceVirtualNetworkPeeringCreateOrUpdate.json
     */
    /**
     * Sample code: Create vNet Peering for Workspace.
     * 
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void
        createVNetPeeringForWorkspace(com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager.vNetPeerings()
            .define("vNetPeeringTest")
            .withExistingWorkspace("subramantest", "adbworkspace")
            .withRemoteVirtualNetwork(new VirtualNetworkPeeringPropertiesFormatRemoteVirtualNetwork().withId(
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
/**
 * Samples for VNetPeering Delete.
 */
public final class VNetPeeringDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/databricks/resource-manager/Microsoft.Databricks/preview/2024-09-01-preview/examples/
     * WorkspaceVirtualNetworkPeeringDelete.json
     */
    /**
     * Sample code: Delete a workspace vNet Peering.
     * 
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void
        deleteAWorkspaceVNetPeering(com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager.vNetPeerings()
            .delete("subramantest", "adbworkspace", "vNetPeeringTest", com.azure.core.util.Context.NONE);
    }
}
```

### VNetPeering_Get

```java
/**
 * Samples for VNetPeering Get.
 */
public final class VNetPeeringGetSamples {
    /*
     * x-ms-original-file:
     * specification/databricks/resource-manager/Microsoft.Databricks/preview/2024-09-01-preview/examples/
     * WorkspaceVirtualNetPeeringGet.json
     */
    /**
     * Sample code: Get a workspace with vNet Peering Configured.
     * 
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void
        getAWorkspaceWithVNetPeeringConfigured(com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager.vNetPeerings()
            .getWithResponse("subramantest", "adbworkspace", "vNetPeeringTest", com.azure.core.util.Context.NONE);
    }
}
```

### VNetPeering_ListByWorkspace

```java
/**
 * Samples for VNetPeering ListByWorkspace.
 */
public final class VNetPeeringListByWorkspaceSamples {
    /*
     * x-ms-original-file:
     * specification/databricks/resource-manager/Microsoft.Databricks/preview/2024-09-01-preview/examples/
     * WorkspaceVirtualNetPeeringList.json
     */
    /**
     * Sample code: List all vNet Peerings for the workspace.
     * 
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void
        listAllVNetPeeringsForTheWorkspace(com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager.vNetPeerings().listByWorkspace("subramantest", "adbworkspace", com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_CreateOrUpdate

```java
import com.azure.resourcemanager.databricks.models.AutomaticClusterUpdateDefinition;
import com.azure.resourcemanager.databricks.models.AutomaticClusterUpdateValue;
import com.azure.resourcemanager.databricks.models.ComplianceSecurityProfileDefinition;
import com.azure.resourcemanager.databricks.models.ComplianceSecurityProfileValue;
import com.azure.resourcemanager.databricks.models.ComplianceStandard;
import com.azure.resourcemanager.databricks.models.DefaultCatalogProperties;
import com.azure.resourcemanager.databricks.models.DefaultStorageFirewall;
import com.azure.resourcemanager.databricks.models.Encryption;
import com.azure.resourcemanager.databricks.models.EncryptionEntitiesDefinition;
import com.azure.resourcemanager.databricks.models.EncryptionKeySource;
import com.azure.resourcemanager.databricks.models.EnhancedSecurityComplianceDefinition;
import com.azure.resourcemanager.databricks.models.EnhancedSecurityMonitoringDefinition;
import com.azure.resourcemanager.databricks.models.EnhancedSecurityMonitoringValue;
import com.azure.resourcemanager.databricks.models.IdentityType;
import com.azure.resourcemanager.databricks.models.InitialType;
import com.azure.resourcemanager.databricks.models.KeySource;
import com.azure.resourcemanager.databricks.models.ManagedDiskEncryption;
import com.azure.resourcemanager.databricks.models.ManagedDiskEncryptionKeyVaultProperties;
import com.azure.resourcemanager.databricks.models.WorkspaceCustomBooleanParameter;
import com.azure.resourcemanager.databricks.models.WorkspaceCustomParameters;
import com.azure.resourcemanager.databricks.models.WorkspaceCustomStringParameter;
import com.azure.resourcemanager.databricks.models.WorkspaceEncryptionParameter;
import com.azure.resourcemanager.databricks.models.WorkspacePropertiesAccessConnector;
import com.azure.resourcemanager.databricks.models.WorkspacePropertiesEncryption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Workspaces CreateOrUpdate.
 */
public final class WorkspacesCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/databricks/resource-manager/Microsoft.Databricks/preview/2024-09-01-preview/examples/
     * WorkspaceManagedDiskEncryptionCreate.json
     */
    /**
     * Sample code: Create a workspace with Customer-Managed Key (CMK) encryption for Managed Disks.
     * 
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void createAWorkspaceWithCustomerManagedKeyCMKEncryptionForManagedDisks(
        com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager.workspaces()
            .define("myWorkspace")
            .withRegion("westus")
            .withExistingResourceGroup("rg")
            .withManagedResourceGroupId(
                "/subscriptions/11111111-1111-1111-1111-111111111111/resourceGroups/myManagedRG")
            .withEncryption(
                new WorkspacePropertiesEncryption()
                    .withEntities(
                        new EncryptionEntitiesDefinition().withManagedDisk(
                            new ManagedDiskEncryption().withKeySource(EncryptionKeySource.MICROSOFT_KEYVAULT)
                                .withKeyVaultProperties(new ManagedDiskEncryptionKeyVaultProperties()
                                    .withKeyVaultUri("fakeTokenPlaceholder")
                                    .withKeyName("fakeTokenPlaceholder")
                                    .withKeyVersion("fakeTokenPlaceholder"))
                                .withRotationToLatestKeyVersionEnabled(true))))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/databricks/resource-manager/Microsoft.Databricks/preview/2024-09-01-preview/examples/
     * WorkspaceManagedDiskEncryptionUpdate.json
     */
    /**
     * Sample code: Update a workspace with Customer-Managed Key (CMK) encryption for Managed Disks.
     * 
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void updateAWorkspaceWithCustomerManagedKeyCMKEncryptionForManagedDisks(
        com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager.workspaces()
            .define("myWorkspace")
            .withRegion("westus")
            .withExistingResourceGroup("rg")
            .withManagedResourceGroupId(
                "/subscriptions/11111111-1111-1111-1111-111111111111/resourceGroups/myManagedRG")
            .withTags(mapOf("mytag1", "myvalue1"))
            .withEncryption(
                new WorkspacePropertiesEncryption()
                    .withEntities(
                        new EncryptionEntitiesDefinition().withManagedDisk(
                            new ManagedDiskEncryption().withKeySource(EncryptionKeySource.MICROSOFT_KEYVAULT)
                                .withKeyVaultProperties(new ManagedDiskEncryptionKeyVaultProperties()
                                    .withKeyVaultUri("fakeTokenPlaceholder")
                                    .withKeyName("fakeTokenPlaceholder")
                                    .withKeyVersion("fakeTokenPlaceholder"))
                                .withRotationToLatestKeyVersionEnabled(true))))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/databricks/resource-manager/Microsoft.Databricks/preview/2024-09-01-preview/examples/
     * DisableEncryption.json
     */
    /**
     * Sample code: Revert Customer-Managed Key (CMK) encryption to Microsoft Managed Keys encryption on a workspace.
     * 
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void revertCustomerManagedKeyCMKEncryptionToMicrosoftManagedKeysEncryptionOnAWorkspace(
        com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager.workspaces()
            .define("myWorkspace")
            .withRegion("westus")
            .withExistingResourceGroup("rg")
            .withManagedResourceGroupId(
                "/subscriptions/11111111-1111-1111-1111-111111111111/resourceGroups/myManagedRG")
            .withParameters(new WorkspaceCustomParameters().withEncryption(
                new WorkspaceEncryptionParameter().withValue(new Encryption().withKeySource(KeySource.DEFAULT))))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/databricks/resource-manager/Microsoft.Databricks/preview/2024-09-01-preview/examples/
     * PrepareEncryption.json
     */
    /**
     * Sample code: Create a workspace which is ready for Customer-Managed Key (CMK) encryption.
     * 
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void createAWorkspaceWhichIsReadyForCustomerManagedKeyCMKEncryption(
        com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager.workspaces()
            .define("myWorkspace")
            .withRegion("westus")
            .withExistingResourceGroup("rg")
            .withManagedResourceGroupId(
                "/subscriptions/11111111-1111-1111-1111-111111111111/resourceGroups/myManagedRG")
            .withParameters(new WorkspaceCustomParameters()
                .withPrepareEncryption(new WorkspaceCustomBooleanParameter().withValue(true)))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/databricks/resource-manager/Microsoft.Databricks/preview/2024-09-01-preview/examples/
     * WorkspaceEnhancedSecurityComplianceCreateOrUpdate.json
     */
    /**
     * Sample code: Create or update a workspace with Enhanced Security &amp; Compliance Add-On.
     * 
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void createOrUpdateAWorkspaceWithEnhancedSecurityComplianceAddOn(
        com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager.workspaces()
            .define("myWorkspace")
            .withRegion("eastus2")
            .withExistingResourceGroup("rg")
            .withManagedResourceGroupId(
                "/subscriptions/11111111-1111-1111-1111-111111111111/resourceGroups/myManagedRG")
            .withEnhancedSecurityCompliance(new EnhancedSecurityComplianceDefinition()
                .withAutomaticClusterUpdate(
                    new AutomaticClusterUpdateDefinition().withValue(AutomaticClusterUpdateValue.ENABLED))
                .withComplianceSecurityProfile(new ComplianceSecurityProfileDefinition()
                    .withComplianceStandards(Arrays.asList(ComplianceStandard.PCI_DSS, ComplianceStandard.HIPAA))
                    .withValue(ComplianceSecurityProfileValue.ENABLED))
                .withEnhancedSecurityMonitoring(
                    new EnhancedSecurityMonitoringDefinition().withValue(EnhancedSecurityMonitoringValue.ENABLED)))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/databricks/resource-manager/Microsoft.Databricks/preview/2024-09-01-preview/examples/
     * WorkspaceCreateWithParameters.json
     */
    /**
     * Sample code: Create or update workspace with custom parameters.
     * 
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void createOrUpdateWorkspaceWithCustomParameters(
        com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager.workspaces()
            .define("myWorkspace")
            .withRegion("westus")
            .withExistingResourceGroup("rg")
            .withManagedResourceGroupId(
                "/subscriptions/11111111-1111-1111-1111-111111111111/resourceGroups/myManagedRG")
            .withParameters(new WorkspaceCustomParameters()
                .withCustomVirtualNetworkId(new WorkspaceCustomStringParameter().withValue(
                    "/subscriptions/11111111-1111-1111-1111-111111111111/resourceGroups/rg/providers/Microsoft.Network/virtualNetworks/myNetwork"))
                .withCustomPublicSubnetName(new WorkspaceCustomStringParameter().withValue("myPublicSubnet"))
                .withCustomPrivateSubnetName(new WorkspaceCustomStringParameter().withValue("myPrivateSubnet")))
            .withDefaultCatalog(
                new DefaultCatalogProperties().withInitialType(InitialType.HIVE_METASTORE).withInitialName(""))
            .withAccessConnector(new WorkspacePropertiesAccessConnector().withId(
                "/subscriptions/11111111-1111-1111-1111-111111111111/resourceGroups/adbrg/providers/Microsoft.Databricks/accessConnectors/myAccessConnector")
                .withIdentityType(IdentityType.USER_ASSIGNED)
                .withUserAssignedIdentityId(
                    "/subscriptions/11111111-1111-1111-1111-111111111111/resourceGroups/rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/myIdentity"))
            .withDefaultStorageFirewall(DefaultStorageFirewall.ENABLED)
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/databricks/resource-manager/Microsoft.Databricks/preview/2024-09-01-preview/examples/
     * WorkspaceCreate.json
     */
    /**
     * Sample code: Create or update workspace.
     * 
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void createOrUpdateWorkspace(com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager.workspaces()
            .define("myWorkspace")
            .withRegion("westus")
            .withExistingResourceGroup("rg")
            .withManagedResourceGroupId(
                "/subscriptions/11111111-1111-1111-1111-111111111111/resourceGroups/myManagedRG")
            .withDefaultCatalog(
                new DefaultCatalogProperties().withInitialType(InitialType.UNITY_CATALOG).withInitialName(""))
            .withAccessConnector(new WorkspacePropertiesAccessConnector().withId(
                "/subscriptions/11111111-1111-1111-1111-111111111111/resourceGroups/adbrg/providers/Microsoft.Databricks/accessConnectors/myAccessConnector")
                .withIdentityType(IdentityType.SYSTEM_ASSIGNED))
            .withDefaultStorageFirewall(DefaultStorageFirewall.ENABLED)
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/databricks/resource-manager/Microsoft.Databricks/preview/2024-09-01-preview/examples/
     * EnableEncryption.json
     */
    /**
     * Sample code: Enable Customer-Managed Key (CMK) encryption on a workspace which is prepared for encryption.
     * 
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void enableCustomerManagedKeyCMKEncryptionOnAWorkspaceWhichIsPreparedForEncryption(
        com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager.workspaces()
            .define("myWorkspace")
            .withRegion("westus")
            .withExistingResourceGroup("rg")
            .withManagedResourceGroupId(
                "/subscriptions/11111111-1111-1111-1111-111111111111/resourceGroups/myManagedRG")
            .withParameters(new WorkspaceCustomParameters()
                .withPrepareEncryption(new WorkspaceCustomBooleanParameter().withValue(true))
                .withEncryption(new WorkspaceEncryptionParameter()
                    .withValue(new Encryption().withKeySource(KeySource.MICROSOFT_KEYVAULT)
                        .withKeyName("fakeTokenPlaceholder")
                        .withKeyVersion("fakeTokenPlaceholder")
                        .withKeyVaultUri("fakeTokenPlaceholder"))))
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

### Workspaces_Delete

```java
/**
 * Samples for Workspaces Delete.
 */
public final class WorkspacesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/databricks/resource-manager/Microsoft.Databricks/preview/2024-09-01-preview/examples/
     * WorkspaceDeleteForceDeletion.json
     */
    /**
     * Sample code: Delete a workspace with forceDeletion parameter.
     * 
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void deleteAWorkspaceWithForceDeletionParameter(
        com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager.workspaces().delete("rg", "myWorkspace", false, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/databricks/resource-manager/Microsoft.Databricks/preview/2024-09-01-preview/examples/
     * WorkspaceDelete.json
     */
    /**
     * Sample code: Delete a workspace.
     * 
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void deleteAWorkspace(com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager.workspaces().delete("rg", "myWorkspace", null, com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_GetByResourceGroup

```java
/**
 * Samples for Workspaces GetByResourceGroup.
 */
public final class WorkspacesGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/databricks/resource-manager/Microsoft.Databricks/preview/2024-09-01-preview/examples/WorkspaceGet.
     * json
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
     * x-ms-original-file:
     * specification/databricks/resource-manager/Microsoft.Databricks/preview/2024-09-01-preview/examples/
     * WorkspaceManagedDiskEncryptionGet.json
     */
    /**
     * Sample code: Get a workspace with Customer-Managed Key (CMK) encryption for Managed Disks.
     * 
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void getAWorkspaceWithCustomerManagedKeyCMKEncryptionForManagedDisks(
        com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager.workspaces().getByResourceGroupWithResponse("rg", "myWorkspace", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/databricks/resource-manager/Microsoft.Databricks/preview/2024-09-01-preview/examples/
     * WorkspaceEnhancedSecurityComplianceGet.json
     */
    /**
     * Sample code: Get a workspace with Enhanced Security &amp; Compliance Add-On.
     * 
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void getAWorkspaceWithEnhancedSecurityComplianceAddOn(
        com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager.workspaces().getByResourceGroupWithResponse("rg", "myWorkspace", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/databricks/resource-manager/Microsoft.Databricks/preview/2024-09-01-preview/examples/
     * WorkspaceGetParameters.json
     */
    /**
     * Sample code: Get a workspace with custom parameters.
     * 
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void
        getAWorkspaceWithCustomParameters(com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        manager.workspaces().getByResourceGroupWithResponse("rg", "myWorkspace", com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_List

```java
/**
 * Samples for Workspaces List.
 */
public final class WorkspacesListSamples {
    /*
     * x-ms-original-file:
     * specification/databricks/resource-manager/Microsoft.Databricks/preview/2024-09-01-preview/examples/
     * WorkspacesListBySubscription.json
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
/**
 * Samples for Workspaces ListByResourceGroup.
 */
public final class WorkspacesListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/databricks/resource-manager/Microsoft.Databricks/preview/2024-09-01-preview/examples/
     * WorkspacesListByResourceGroup.json
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

/**
 * Samples for Workspaces Update.
 */
public final class WorkspacesUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/databricks/resource-manager/Microsoft.Databricks/preview/2024-09-01-preview/examples/
     * WorkspaceUpdate.json
     */
    /**
     * Sample code: Update a workspace's tags.
     * 
     * @param manager Entry point to AzureDatabricksManager.
     */
    public static void updateAWorkspaceSTags(com.azure.resourcemanager.databricks.AzureDatabricksManager manager) {
        Workspace resource = manager.workspaces()
            .getByResourceGroupWithResponse("rg", "myWorkspace", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("mytag1", "myvalue1")).apply();
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

