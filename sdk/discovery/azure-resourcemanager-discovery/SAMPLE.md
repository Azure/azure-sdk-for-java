# Code snippets and samples


## BookshelfPrivateEndpointConnections

- [CreateOrUpdate](#bookshelfprivateendpointconnections_createorupdate)
- [Delete](#bookshelfprivateendpointconnections_delete)
- [Get](#bookshelfprivateendpointconnections_get)
- [ListByBookshelf](#bookshelfprivateendpointconnections_listbybookshelf)

## BookshelfPrivateLinkResources

- [Get](#bookshelfprivatelinkresources_get)
- [ListByBookshelf](#bookshelfprivatelinkresources_listbybookshelf)

## Bookshelves

- [CreateOrUpdate](#bookshelves_createorupdate)
- [Delete](#bookshelves_delete)
- [GetByResourceGroup](#bookshelves_getbyresourcegroup)
- [List](#bookshelves_list)
- [ListByResourceGroup](#bookshelves_listbyresourcegroup)
- [Update](#bookshelves_update)

## ChatModelDeployments

- [CreateOrUpdate](#chatmodeldeployments_createorupdate)
- [Delete](#chatmodeldeployments_delete)
- [Get](#chatmodeldeployments_get)
- [ListByWorkspace](#chatmodeldeployments_listbyworkspace)
- [Update](#chatmodeldeployments_update)

## NodePools

- [CreateOrUpdate](#nodepools_createorupdate)
- [Delete](#nodepools_delete)
- [Get](#nodepools_get)
- [ListBySupercomputer](#nodepools_listbysupercomputer)
- [Update](#nodepools_update)

## Operations

- [List](#operations_list)

## Projects

- [CreateOrUpdate](#projects_createorupdate)
- [Delete](#projects_delete)
- [Get](#projects_get)
- [ListByWorkspace](#projects_listbyworkspace)
- [Update](#projects_update)

## StorageAssets

- [CreateOrUpdate](#storageassets_createorupdate)
- [Delete](#storageassets_delete)
- [Get](#storageassets_get)
- [ListByStorageContainer](#storageassets_listbystoragecontainer)
- [Update](#storageassets_update)

## StorageContainers

- [CreateOrUpdate](#storagecontainers_createorupdate)
- [Delete](#storagecontainers_delete)
- [GetByResourceGroup](#storagecontainers_getbyresourcegroup)
- [List](#storagecontainers_list)
- [ListByResourceGroup](#storagecontainers_listbyresourcegroup)
- [Update](#storagecontainers_update)

## Supercomputers

- [CreateOrUpdate](#supercomputers_createorupdate)
- [Delete](#supercomputers_delete)
- [GetByResourceGroup](#supercomputers_getbyresourcegroup)
- [List](#supercomputers_list)
- [ListByResourceGroup](#supercomputers_listbyresourcegroup)
- [Update](#supercomputers_update)

## Tools

- [CreateOrUpdate](#tools_createorupdate)
- [Delete](#tools_delete)
- [GetByResourceGroup](#tools_getbyresourcegroup)
- [List](#tools_list)
- [ListByResourceGroup](#tools_listbyresourcegroup)
- [Update](#tools_update)

## WorkspacePrivateEndpointConnections

- [CreateOrUpdate](#workspaceprivateendpointconnections_createorupdate)
- [Delete](#workspaceprivateendpointconnections_delete)
- [Get](#workspaceprivateendpointconnections_get)
- [ListByWorkspace](#workspaceprivateendpointconnections_listbyworkspace)

## WorkspacePrivateLinkResources

- [Get](#workspaceprivatelinkresources_get)
- [ListByWorkspace](#workspaceprivatelinkresources_listbyworkspace)

## Workspaces

- [CreateOrUpdate](#workspaces_createorupdate)
- [Delete](#workspaces_delete)
- [GetByResourceGroup](#workspaces_getbyresourcegroup)
- [List](#workspaces_list)
- [ListByResourceGroup](#workspaces_listbyresourcegroup)
- [Update](#workspaces_update)
### BookshelfPrivateEndpointConnections_CreateOrUpdate

```java
import com.azure.resourcemanager.discovery.models.PrivateEndpoint;
import com.azure.resourcemanager.discovery.models.PrivateEndpointConnectionProperties;
import com.azure.resourcemanager.discovery.models.PrivateEndpointServiceConnectionStatus;
import com.azure.resourcemanager.discovery.models.PrivateLinkServiceConnectionState;

/**
 * Samples for BookshelfPrivateEndpointConnections CreateOrUpdate.
 */
public final class BookshelfPrivateEndpointConnectionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/BookshelfPrivateEndpointConnections_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: BookshelfPrivateEndpointConnections_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void bookshelfPrivateEndpointConnectionsCreateOrUpdateMaximumSet(
        com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.bookshelfPrivateEndpointConnections()
            .define("connection")
            .withExistingBookshelf("rgdiscovery", "a65f3c23bf2baa5bd4")
            .withProperties(
                new PrivateEndpointConnectionProperties().withPrivateEndpoint(new PrivateEndpoint())
                    .withPrivateLinkServiceConnectionState(new PrivateLinkServiceConnectionState()
                        .withStatus(PrivateEndpointServiceConnectionStatus.PENDING)
                        .withDescription("lknyprq")
                        .withActionsRequired("vgqhrxvmviabfgmafqtbej")))
            .create();
    }
}
```

### BookshelfPrivateEndpointConnections_Delete

```java
/**
 * Samples for BookshelfPrivateEndpointConnections Delete.
 */
public final class BookshelfPrivateEndpointConnectionsDeleteSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/BookshelfPrivateEndpointConnections_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: BookshelfPrivateEndpointConnections_Delete_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void bookshelfPrivateEndpointConnectionsDeleteMaximumSet(
        com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.bookshelfPrivateEndpointConnections()
            .delete("rgdiscovery", "9988c91bf62635cea5", "connection", com.azure.core.util.Context.NONE);
    }
}
```

### BookshelfPrivateEndpointConnections_Get

```java
/**
 * Samples for BookshelfPrivateEndpointConnections Get.
 */
public final class BookshelfPrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/BookshelfPrivateEndpointConnections_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: BookshelfPrivateEndpointConnections_Get_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void
        bookshelfPrivateEndpointConnectionsGetMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.bookshelfPrivateEndpointConnections()
            .getWithResponse("rgdiscovery", "ca2ea71fd0a5838c7f", "connection", com.azure.core.util.Context.NONE);
    }
}
```

### BookshelfPrivateEndpointConnections_ListByBookshelf

```java
/**
 * Samples for BookshelfPrivateEndpointConnections ListByBookshelf.
 */
public final class BookshelfPrivateEndpointConnectionsListByBookshelfSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/BookshelfPrivateEndpointConnections_ListByBookshelf_MaximumSet_Gen.json
     */
    /**
     * Sample code: BookshelfPrivateEndpointConnections_ListByBookshelf_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void bookshelfPrivateEndpointConnectionsListByBookshelfMaximumSet(
        com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.bookshelfPrivateEndpointConnections()
            .listByBookshelf("rgdiscovery", "d96263ffc8d8c904d4", com.azure.core.util.Context.NONE);
    }
}
```

### BookshelfPrivateLinkResources_Get

```java
/**
 * Samples for BookshelfPrivateLinkResources Get.
 */
public final class BookshelfPrivateLinkResourcesGetSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/BookshelfPrivateLinkResources_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: BookshelfPrivateLinkResources_Get_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void
        bookshelfPrivateLinkResourcesGetMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.bookshelfPrivateLinkResources()
            .getWithResponse("rgdiscovery", "9158657d63f4f9235f", "connection", com.azure.core.util.Context.NONE);
    }
}
```

### BookshelfPrivateLinkResources_ListByBookshelf

```java
/**
 * Samples for BookshelfPrivateLinkResources ListByBookshelf.
 */
public final class BookshelfPrivateLinkResourcesListByBookshelfSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/BookshelfPrivateLinkResources_ListByBookshelf_MaximumSet_Gen.json
     */
    /**
     * Sample code: BookshelfPrivateLinkResources_ListByBookshelf_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void bookshelfPrivateLinkResourcesListByBookshelfMaximumSet(
        com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.bookshelfPrivateLinkResources()
            .listByBookshelf("rgdiscovery", "cb4a7b7d5c4b6c3f78", com.azure.core.util.Context.NONE);
    }
}
```

### Bookshelves_CreateOrUpdate

```java
import com.azure.resourcemanager.discovery.models.BookshelfKeyVaultProperties;
import com.azure.resourcemanager.discovery.models.BookshelfProperties;
import com.azure.resourcemanager.discovery.models.CustomerManagedKeys;
import com.azure.resourcemanager.discovery.models.PublicNetworkAccess;
import com.azure.resourcemanager.discovery.models.UserAssignedIdentity;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Bookshelves CreateOrUpdate.
 */
public final class BookshelvesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/Bookshelves_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: Bookshelves_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void
        bookshelvesCreateOrUpdateMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.bookshelves()
            .define("21b8f5a6a47fa1fdcc")
            .withRegion("uksouth")
            .withExistingResourceGroup("rgdiscovery")
            .withTags(mapOf("key782", "fakeTokenPlaceholder"))
            .withProperties(new BookshelfProperties()
                .withWorkloadIdentities(mapOf("key8334", new UserAssignedIdentity()))
                .withCustomerManagedKeys(CustomerManagedKeys.ENABLED)
                .withKeyVaultProperties(new BookshelfKeyVaultProperties().withKeyVaultUri("fakeTokenPlaceholder")
                    .withKeyName("fakeTokenPlaceholder")
                    .withKeyVersion("fakeTokenPlaceholder")
                    .withIdentityClientId("00000011-1111-2222-2222-123456789111"))
                .withLogAnalyticsClusterId(
                    "/subscriptions/31735C59-6307-4464-8B80-3675223F23D2/providers/Microsoft.OperationalInsights/clusters/cluster1")
                .withPublicNetworkAccess(PublicNetworkAccess.ENABLED)
                .withPrivateEndpointSubnetId(
                    "/subscriptions/31735C59-6307-4464-8B80-3675223F23D2/providers/Microsoft.Network/virtualNetworks/virtualnetwork1/subnets/privateEndpointSubnet1")
                .withSearchSubnetId(
                    "/subscriptions/31735C59-6307-4464-8B80-3675223F23D2/providers/Microsoft.Network/virtualNetworks/virtualnetwork1/subnets/searchSubnet1"))
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

### Bookshelves_Delete

```java
/**
 * Samples for Bookshelves Delete.
 */
public final class BookshelvesDeleteSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/Bookshelves_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Bookshelves_Delete_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void bookshelvesDeleteMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.bookshelves().delete("rgdiscovery", "cdaa070c4d0ea7b9c9", com.azure.core.util.Context.NONE);
    }
}
```

### Bookshelves_GetByResourceGroup

```java
/**
 * Samples for Bookshelves GetByResourceGroup.
 */
public final class BookshelvesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/Bookshelves_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Bookshelves_Get_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void bookshelvesGetMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.bookshelves()
            .getByResourceGroupWithResponse("rgdiscovery", "85c2fc6e437c0b608b", com.azure.core.util.Context.NONE);
    }
}
```

### Bookshelves_List

```java
/**
 * Samples for Bookshelves List.
 */
public final class BookshelvesListSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/Bookshelves_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: Bookshelves_ListBySubscription_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void
        bookshelvesListBySubscriptionMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.bookshelves().list(com.azure.core.util.Context.NONE);
    }
}
```

### Bookshelves_ListByResourceGroup

```java
/**
 * Samples for Bookshelves ListByResourceGroup.
 */
public final class BookshelvesListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/Bookshelves_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: Bookshelves_ListByResourceGroup_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void
        bookshelvesListByResourceGroupMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.bookshelves().listByResourceGroup("rgdiscovery", com.azure.core.util.Context.NONE);
    }
}
```

### Bookshelves_Update

```java
import com.azure.resourcemanager.discovery.models.Bookshelf;
import com.azure.resourcemanager.discovery.models.BookshelfKeyVaultProperties;
import com.azure.resourcemanager.discovery.models.BookshelfProperties;
import com.azure.resourcemanager.discovery.models.PublicNetworkAccess;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Bookshelves Update.
 */
public final class BookshelvesUpdateSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/Bookshelves_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: Bookshelves_Update_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void bookshelvesUpdateMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        Bookshelf resource = manager.bookshelves()
            .getByResourceGroupWithResponse("rgdiscovery", "c6189a7b33260c4a72", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key1792", "fakeTokenPlaceholder"))
            .withProperties(new BookshelfProperties()
                .withKeyVaultProperties(new BookshelfKeyVaultProperties().withKeyName("fakeTokenPlaceholder")
                    .withKeyVersion("fakeTokenPlaceholder"))
                .withPublicNetworkAccess(PublicNetworkAccess.ENABLED))
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

### ChatModelDeployments_CreateOrUpdate

```java
import com.azure.resourcemanager.discovery.models.ChatModelDeploymentProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ChatModelDeployments CreateOrUpdate.
 */
public final class ChatModelDeploymentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/ChatModelDeployments_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: ChatModelDeployments_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void
        chatModelDeploymentsCreateOrUpdateMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.chatModelDeployments()
            .define("d1844ae17cc93bd299")
            .withRegion("uksouth")
            .withExistingWorkspace("rgdiscovery", "eb2204766409e111d9")
            .withTags(mapOf("key4822", "fakeTokenPlaceholder"))
            .withProperties(new ChatModelDeploymentProperties().withModelFormat("tcttsgevrsuflt")
                .withModelName("nvwdoluhukiachlyrdnpxusxsc"))
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

### ChatModelDeployments_Delete

```java
/**
 * Samples for ChatModelDeployments Delete.
 */
public final class ChatModelDeploymentsDeleteSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/ChatModelDeployments_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: ChatModelDeployments_Delete_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void
        chatModelDeploymentsDeleteMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.chatModelDeployments()
            .delete("rgdiscovery", "015403a79d07536250", "7a1ee53e20d918a13d", com.azure.core.util.Context.NONE);
    }
}
```

### ChatModelDeployments_Get

```java
/**
 * Samples for ChatModelDeployments Get.
 */
public final class ChatModelDeploymentsGetSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/ChatModelDeployments_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: ChatModelDeployments_Get_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void chatModelDeploymentsGetMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.chatModelDeployments()
            .getWithResponse("rgdiscovery", "715bd6631a63225578", "7938c93c6f61d31f7e",
                com.azure.core.util.Context.NONE);
    }
}
```

### ChatModelDeployments_ListByWorkspace

```java
/**
 * Samples for ChatModelDeployments ListByWorkspace.
 */
public final class ChatModelDeploymentsListByWorkspaceSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/ChatModelDeployments_ListByWorkspace_MaximumSet_Gen.json
     */
    /**
     * Sample code: ChatModelDeployments_ListByWorkspace_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void
        chatModelDeploymentsListByWorkspaceMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.chatModelDeployments()
            .listByWorkspace("rgdiscovery", "715794cf970dc53142", com.azure.core.util.Context.NONE);
    }
}
```

### ChatModelDeployments_Update

```java
import com.azure.resourcemanager.discovery.models.ChatModelDeployment;
import com.azure.resourcemanager.discovery.models.ChatModelDeploymentProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ChatModelDeployments Update.
 */
public final class ChatModelDeploymentsUpdateSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/ChatModelDeployments_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: ChatModelDeployments_Update_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void
        chatModelDeploymentsUpdateMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        ChatModelDeployment resource = manager.chatModelDeployments()
            .getWithResponse("rgdiscovery", "438970fd7f0137032c", "fd0837f1d866060b11",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key6223", "fakeTokenPlaceholder"))
            .withProperties(new ChatModelDeploymentProperties())
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

### NodePools_CreateOrUpdate

```java
import com.azure.resourcemanager.discovery.models.NodePoolProperties;
import com.azure.resourcemanager.discovery.models.ScaleSetPriority;
import com.azure.resourcemanager.discovery.models.VmSize;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for NodePools CreateOrUpdate.
 */
public final class NodePoolsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/NodePools_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: NodePools_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void nodePoolsCreateOrUpdateMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.nodePools()
            .define("5a88c24ec4e7091650")
            .withRegion("uksouth")
            .withExistingSupercomputer("rgdiscovery", "8074da5c77f95509a8")
            .withTags(mapOf("key6074", "fakeTokenPlaceholder"))
            .withProperties(new NodePoolProperties().withSubnetId(
                "/subscriptions/31735C59-6307-4464-8B80-3675223F23D2/providers/Microsoft.Network/virtualNetworks/virtualnetwork1/subnets/subnet1")
                .withVmSize(VmSize.STANDARD_NC24ADS_A100_V4)
                .withMaxNodeCount(4)
                .withMinNodeCount(0)
                .withScaleSetPriority(ScaleSetPriority.REGULAR))
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

### NodePools_Delete

```java
/**
 * Samples for NodePools Delete.
 */
public final class NodePoolsDeleteSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/NodePools_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: NodePools_Delete_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void nodePoolsDeleteMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.nodePools()
            .delete("rgdiscovery", "6ddaf20b09c36fc7ef", "6dcea29fcbc2279a3b", com.azure.core.util.Context.NONE);
    }
}
```

### NodePools_Get

```java
/**
 * Samples for NodePools Get.
 */
public final class NodePoolsGetSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/NodePools_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: NodePools_Get_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void nodePoolsGetMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.nodePools()
            .getWithResponse("rgdiscovery", "3d4fce3989a31db9c7", "80084da43e5c8bc50e",
                com.azure.core.util.Context.NONE);
    }
}
```

### NodePools_ListBySupercomputer

```java
/**
 * Samples for NodePools ListBySupercomputer.
 */
public final class NodePoolsListBySupercomputerSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/NodePools_ListBySupercomputer_MaximumSet_Gen.json
     */
    /**
     * Sample code: NodePools_ListBySupercomputer_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void
        nodePoolsListBySupercomputerMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.nodePools().listBySupercomputer("rgdiscovery", "7cc28f3db7c8fa0087", com.azure.core.util.Context.NONE);
    }
}
```

### NodePools_Update

```java
import com.azure.resourcemanager.discovery.models.NodePool;
import com.azure.resourcemanager.discovery.models.NodePoolProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for NodePools Update.
 */
public final class NodePoolsUpdateSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/NodePools_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: NodePools_Update_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void nodePoolsUpdateMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        NodePool resource = manager.nodePools()
            .getWithResponse("rgdiscovery", "f674a0697b0c54044e", "12ceb04d31658f1ec7",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key5366", "fakeTokenPlaceholder"))
            .withProperties(new NodePoolProperties().withMaxNodeCount(21).withMinNodeCount(0))
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

### Operations_List

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/Operations_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_MinimumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void operationsListMinimumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-02-01-preview/Operations_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void operationsListMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Projects_CreateOrUpdate

```java
import com.azure.resourcemanager.discovery.models.ProjectProperties;
import com.azure.resourcemanager.discovery.models.ProjectSettings;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Projects CreateOrUpdate.
 */
public final class ProjectsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/Projects_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: Projects_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void projectsCreateOrUpdateMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.projects()
            .define("55cdd016d5653ebf82")
            .withRegion("uksouth")
            .withExistingWorkspace("rgdiscovery", "1cf6d2a754acbb9655")
            .withTags(mapOf("key717", "fakeTokenPlaceholder"))
            .withProperties(new ProjectProperties().withStorageContainerIds(Arrays.asList(
                "/subscriptions/31735C59-6307-4464-8B80-3675223F23D2/resourceGroups/rgdiscovery/providers/Microsoft.Discovery/storageContainers/storageContainer12"))
                .withSettings(new ProjectSettings().withBehaviorPreferences(
                    "zjhzrtkwdmwbueseguplzpxeqvbqrknwvxxgtwtpldnguihcbprdwsihucbrmhyvmxuvldlgtrheqehrpcmdqtjknlyjnzdvphjicifbuvlsjgoaiaeunshmuupogxhwywntzhdvrdkknumgyofeltjqyenfiemerqsafaphhzgkwqrnuhbxklclishnnailctvcdjzvfroakitqkmllziocaolmyvytjmqhivljovriyicparifitswaynjsczcpfsgwyjsojiwqzauscpgmxqhznkofwydrjauiwkwkjrvclbufqmzyftfwjkymalhwkpiabljammjstpsknxqouabruobyznqnscucrvurarbbtefmaqiqfkyykuifojikmnkfgnjyagxwpjilfjyfpkqjdgrupitpqbvebmhsizeomzxqekqbsqqnlkefolbgbnfavtliixrvqxcbcxrxnpozucsvseddpjzsydiebyxxdehaniinfvfbflqwmyqelsjquigikebmfuuhdervonditfsummrsuokoqtessdmwptawejkqkkmtzgomamsbcbpviejbirvdwbcoenrsxeyayglvygsknetjuxdmbroritqklncrrnstwuaoohrqnypxfgbvfofsabnrgofobhdkktjyuhrxzmkrwglqczjlrfxbcbrplqmocjbphkjpdfxrfpkfrvlqqmiwftsuhxmjpimvngakbpkcvdevwubfqjjpnjmoiruabwxtzqehlwangolxjeqzjbfxltrgchuiginrgaeaztcqwacogzhvuhxcolvlzmoulikspebsdjyqlzgrtkqobkszfspnjftmnzsbyctxhkjsyemlehnqqhvvfdtrfarpjgaklmvbwukaykstipsnejnpnwaxskppilmgcdebupwpyyqajergevjizlhkiinvvqojrnegwbilhaktgjfkkefnwfbcxmjgylidekqjvgkxvnxdrttxzoyndupwqvlebbfgyiddgixrhcdbfkgakpausgcmjsgpqsjonrrrodgzzkworpvxzjzgtdbzqerqwjhjwoescduqwdwxsgmgyxolhlpdhuvscmzuuiynntspcyjlyngexpardjklyycnyquvbtfwlnqzxujowoljfvpnbyyoqiblxiiojiaeqpoblnrgttqmjqvqjaawevmbxgmbsaumjutbxhywkvhrljzcpitnunwfvtyiglqwlcngffnzdnfvccfqgdiazxoxgsepfqlyoxnofvogtsudsxarqpxhpidrwbykeypszgcbbyshljhlxperpdysagbmumexubjzimbetfayqfcbdkdpfsqrfuisioggdewxmeuihqitkwqfvmmwfppxfoigaoskgzedlhtmjagcbvwwnfkeyocceioccfjmxvtjutydhwapxkgdgjxuquujkdjtbkwhrlbxgclyqdcdnexitthelxajidteqtahvjwoovaripeuzycrdazmmzvpcecahnyentvdqdfteddmddcllromsumkucqhcgdelqsotimumwulnplxqeckhwgngafyvjlykdyotciabfkkdbqlfgwxkjreelzdswbaqzhiweqopubmxlestbvcrdimtiyleisfsvheanokuaipbniseceonfqlbrfofrhbuwtuirnrooflajdyrzwgcmpztgeyzrgvohijxrwrgsmfxagjuuygqtbyilneezxkmcbjoyhljlnesiuhgznvxzglpemwqfpixqlppehxeqzwhuxfcknkmpfttpywxaimzrsfalarhtoiwenlulyoadfdcyvsahzngcashxrchwrxknrvzhldforskqsktvltfuuxhummulwcfvezmfedfobzyfryrrzbypsvakkgppvhhicjydgzmnhcxsfqrdwgbmitmzzmjyzgvzusjigujfohqdlgmrtxmvvxoaxyfiyrgvfawkyynjykpmejzqmrpzitetxhqcjrqspglkzricplkrecaiumajvsobcddohclhmyinyteblkxqoqnoiyfwspencqszjbsnpyokusxgptshytgzqkynhncmqgrxaeugwyzxxqubfdkozfqrdeveopnhyvjcpjziogeyinlvcbhwltteivcnkfehvatnbvkfoqedljupaxholukhagwcasdgagdhpmkiumdclzstyexknhlojeoqkuejmnbuhdwgskgbqfomxvkgziun")))
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

### Projects_Delete

```java
/**
 * Samples for Projects Delete.
 */
public final class ProjectsDeleteSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/Projects_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Projects_Delete_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void projectsDeleteMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.projects()
            .delete("rgdiscovery", "5020af62f469b308c0", "9ae1e783de71d4e949", com.azure.core.util.Context.NONE);
    }
}
```

### Projects_Get

```java
/**
 * Samples for Projects Get.
 */
public final class ProjectsGetSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/Projects_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Projects_Get_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void projectsGetMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.projects()
            .getWithResponse("rgdiscovery", "aa8419d5add7095abd", "4884cf65356e9c3489",
                com.azure.core.util.Context.NONE);
    }
}
```

### Projects_ListByWorkspace

```java
/**
 * Samples for Projects ListByWorkspace.
 */
public final class ProjectsListByWorkspaceSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/Projects_ListByWorkspace_MaximumSet_Gen.json
     */
    /**
     * Sample code: Projects_ListByWorkspace_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void projectsListByWorkspaceMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.projects().listByWorkspace("rgdiscovery", "56c2d23d65c9121656", com.azure.core.util.Context.NONE);
    }
}
```

### Projects_Update

```java
import com.azure.resourcemanager.discovery.models.Project;
import com.azure.resourcemanager.discovery.models.ProjectProperties;
import com.azure.resourcemanager.discovery.models.ProjectSettings;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Projects Update.
 */
public final class ProjectsUpdateSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/Projects_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: Projects_Update_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void projectsUpdateMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        Project resource = manager.projects()
            .getWithResponse("rgdiscovery", "11e14e4bec2ea791b4", "1e7dd7aa730b25cabf",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key2596", "fakeTokenPlaceholder"))
            .withProperties(new ProjectProperties().withStorageContainerIds(Arrays.asList(
                "/subscriptions/31735C59-6307-4464-8B80-3675223F23D2/resourceGroups/rgdiscovery/providers/Microsoft.Discovery/storageContainers/storageContainer12"))
                .withSettings(new ProjectSettings().withBehaviorPreferences(
                    "zjhzrtkwdmwbueseguplzpxeqvbqrknwvxxgtwtpldnguihcbprdwsihucbrmhyvmxuvldlgtrheqehrpcmdqtjknlyjnzdvphjicifbuvlsjgoaiaeunshmuupogxhwywntzhdvrdkknumgyofeltjqyenfiemerqsafaphhzgkwqrnuhbxklclishnnailctvcdjzvfroakitqkmllziocaolmyvytjmqhivljovriyicparifitswaynjsczcpfsgwyjsojiwqzauscpgmxqhznkofwydrjauiwkwkjrvclbufqmzyftfwjkymalhwkpiabljammjstpsknxqouabruobyznqnscucrvurarbbtefmaqiqfkyykuifojikmnkfgnjyagxwpjilfjyfpkqjdgrupitpqbvebmhsizeomzxqekqbsqqnlkefolbgbnfavtliixrvqxcbcxrxnpozucsvseddpjzsydiebyxxdehaniinfvfbflqwmyqelsjquigikebmfuuhdervonditfsummrsuokoqtessdmwptawejkqkkmtzgomamsbcbpviejbirvdwbcoenrsxeyayglvygsknetjuxdmbroritqklncrrnstwuaoohrqnypxfgbvfofsabnrgofobhdkktjyuhrxzmkrwglqczjlrfxbcbrplqmocjbphkjpdfxrfpkfrvlqqmiwftsuhxmjpimvngakbpkcvdevwubfqjjpnjmoiruabwxtzqehlwangolxjeqzjbfxltrgchuiginrgaeaztcqwacogzhvuhxcolvlzmoulikspebsdjyqlzgrtkqobkszfspnjftmnzsbyctxhkjsyemlehnqqhvvfdtrfarpjgaklmvbwukaykstipsnejnpnwaxskppilmgcdebupwpyyqajergevjizlhkiinvvqojrnegwbilhaktgjfkkefnwfbcxmjgylidekqjvgkxvnxdrttxzoyndupwqvlebbfgyiddgixrhcdbfkgakpausgcmjsgpqsjonrrrodgzzkworpvxzjzgtdbzqerqwjhjwoescduqwdwxsgmgyxolhlpdhuvscmzuuiynntspcyjlyngexpardjklyycnyquvbtfwlnqzxujowoljfvpnbyyoqiblxiiojiaeqpoblnrgttqmjqvqjaawevmbxgmbsaumjutbxhywkvhrljzcpitnunwfvtyiglqwlcngffnzdnfvccfqgdiazxoxgsepfqlyoxnofvogtsudsxarqpxhpidrwbykeypszgcbbyshljhlxperpdysagbmumexubjzimbetfayqfcbdkdpfsqrfuisioggdewxmeuihqitkwqfvmmwfppxfoigaoskgzedlhtmjagcbvwwnfkeyocceioccfjmxvtjutydhwapxkgdgjxuquujkdjtbkwhrlbxgclyqdcdnexitthelxajidteqtahvjwoovaripeuzycrdazmmzvpcecahnyentvdqdfteddmddcllromsumkucqhcgdelqsotimumwulnplxqeckhwgngafyvjlykdyotciabfkkdbqlfgwxkjreelzdswbaqzhiweqopubmxlestbvcrdimtiyleisfsvheanokuaipbniseceonfqlbrfofrhbuwtuirnrooflajdyrzwgcmpztgeyzrgvohijxrwrgsmfxagjuuygqtbyilneezxkmcbjoyhljlnesiuhgznvxzglpemwqfpixqlppehxeqzwhuxfcknkmpfttpywxaimzrsfalarhtoiwenlulyoadfdcyvsahzngcashxrchwrxknrvzhldforskqsktvltfuuxhummulwcfvezmfedfobzyfryrrzbypsvakkgppvhhicjydgzmnhcxsfqrdwgbmitmzzmjyzgvzusjigujfohqdlgmrtxmvvxoaxyfiyrgvfawkyynjykpmejzqmrpzitetxhqcjrqspglkzricplkrecaiumajvsobcddohclhmyinyteblkxqoqnoiyfwspencqszjbsnpyokusxgptshytgzqkynhncmqgrxaeugwyzxxqubfdkozfqrdeveopnhyvjcpjziogeyinlvcbhwltteivcnkfehvatnbvkfoqedljupaxholukhagwcasdgagdhpmkiumdclzstyexknhlojeoqkuejmnbuhdwgskgbqfomxvkgziun")))
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

### StorageAssets_CreateOrUpdate

```java
import com.azure.resourcemanager.discovery.models.StorageAssetProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for StorageAssets CreateOrUpdate.
 */
public final class StorageAssetsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/StorageAssets_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: StorageAssets_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void
        storageAssetsCreateOrUpdateMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.storageAssets()
            .define("8fd30c31448f7b0f1a")
            .withRegion("uksouth")
            .withExistingStorageContainer("rgdiscovery", "106b8981ac9ca95890")
            .withTags(mapOf("key5443", "fakeTokenPlaceholder"))
            .withProperties(new StorageAssetProperties().withDescription("gwlk").withPath("qmvrklgqdif"))
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

### StorageAssets_Delete

```java
/**
 * Samples for StorageAssets Delete.
 */
public final class StorageAssetsDeleteSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/StorageAssets_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: StorageAssets_Delete_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void storageAssetsDeleteMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.storageAssets()
            .delete("rgdiscovery", "f7e7a03c675ccffe1a", "8f741ee4588dc823fc", com.azure.core.util.Context.NONE);
    }
}
```

### StorageAssets_Get

```java
/**
 * Samples for StorageAssets Get.
 */
public final class StorageAssetsGetSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/StorageAssets_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: StorageAssets_Get_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void storageAssetsGetMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.storageAssets()
            .getWithResponse("rgdiscovery", "edde0a4a016d7d6b2b", "5ea4bb40f40e2ef5e2",
                com.azure.core.util.Context.NONE);
    }
}
```

### StorageAssets_ListByStorageContainer

```java
/**
 * Samples for StorageAssets ListByStorageContainer.
 */
public final class StorageAssetsListByStorageContainerSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/StorageAssets_ListByStorageContainer_MaximumSet_Gen.json
     */
    /**
     * Sample code: StorageAssets_ListByStorageContainer_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void
        storageAssetsListByStorageContainerMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.storageAssets()
            .listByStorageContainer("rgdiscovery", "6b4fbcbb65873f18bf", com.azure.core.util.Context.NONE);
    }
}
```

### StorageAssets_Update

```java
import com.azure.resourcemanager.discovery.models.StorageAsset;
import com.azure.resourcemanager.discovery.models.StorageAssetProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for StorageAssets Update.
 */
public final class StorageAssetsUpdateSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/StorageAssets_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: StorageAssets_Update_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void storageAssetsUpdateMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        StorageAsset resource = manager.storageAssets()
            .getWithResponse("rgdiscovery", "d177d30241e3f8a27d", "6cd8920c03970ccdfe",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key5822", "fakeTokenPlaceholder"))
            .withProperties(new StorageAssetProperties().withDescription("tljmqqr"))
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

### StorageContainers_CreateOrUpdate

```java
import com.azure.resourcemanager.discovery.models.AzureStorageBlobStore;
import com.azure.resourcemanager.discovery.models.StorageContainerProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for StorageContainers CreateOrUpdate.
 */
public final class StorageContainersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/StorageContainers_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: StorageContainers_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void
        storageContainersCreateOrUpdateMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.storageContainers()
            .define("23ae33a54872c83164")
            .withRegion("uksouth")
            .withExistingResourceGroup("rgdiscovery")
            .withTags(mapOf("key9976", "fakeTokenPlaceholder"))
            .withProperties(
                new StorageContainerProperties().withStorageStore(new AzureStorageBlobStore().withStorageAccountId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg/providers/Microsoft.Storage/storageAccounts/storageaccount")))
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

### StorageContainers_Delete

```java
/**
 * Samples for StorageContainers Delete.
 */
public final class StorageContainersDeleteSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/StorageContainers_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: StorageContainers_Delete_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void storageContainersDeleteMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.storageContainers().delete("rgdiscovery", "861edbda8228e6d8c9", com.azure.core.util.Context.NONE);
    }
}
```

### StorageContainers_GetByResourceGroup

```java
/**
 * Samples for StorageContainers GetByResourceGroup.
 */
public final class StorageContainersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/StorageContainers_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: StorageContainers_Get_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void storageContainersGetMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.storageContainers()
            .getByResourceGroupWithResponse("rgdiscovery", "8f3eba3d81d78de900", com.azure.core.util.Context.NONE);
    }
}
```

### StorageContainers_List

```java
/**
 * Samples for StorageContainers List.
 */
public final class StorageContainersListSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/StorageContainers_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: StorageContainers_ListBySubscription_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void
        storageContainersListBySubscriptionMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.storageContainers().list(com.azure.core.util.Context.NONE);
    }
}
```

### StorageContainers_ListByResourceGroup

```java
/**
 * Samples for StorageContainers ListByResourceGroup.
 */
public final class StorageContainersListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/StorageContainers_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: StorageContainers_ListByResourceGroup_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void
        storageContainersListByResourceGroupMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.storageContainers().listByResourceGroup("rgdiscovery", com.azure.core.util.Context.NONE);
    }
}
```

### StorageContainers_Update

```java
import com.azure.resourcemanager.discovery.models.StorageContainer;
import com.azure.resourcemanager.discovery.models.StorageContainerProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for StorageContainers Update.
 */
public final class StorageContainersUpdateSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/StorageContainers_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: StorageContainers_Update_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void storageContainersUpdateMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        StorageContainer resource = manager.storageContainers()
            .getByResourceGroupWithResponse("rgdiscovery", "5c26ac8738c893ec11", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key5909", "fakeTokenPlaceholder"))
            .withProperties(new StorageContainerProperties())
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

### Supercomputers_CreateOrUpdate

```java
import com.azure.resourcemanager.discovery.models.CustomerManagedKeys;
import com.azure.resourcemanager.discovery.models.Identity;
import com.azure.resourcemanager.discovery.models.NetworkEgressType;
import com.azure.resourcemanager.discovery.models.SupercomputerIdentities;
import com.azure.resourcemanager.discovery.models.SupercomputerProperties;
import com.azure.resourcemanager.discovery.models.SystemSku;
import com.azure.resourcemanager.discovery.models.UserAssignedIdentity;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Supercomputers CreateOrUpdate.
 */
public final class SupercomputersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/Supercomputers_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: Supercomputers_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void
        supercomputersCreateOrUpdateMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.supercomputers()
            .define("85fd61f68e7207bbd3")
            .withRegion("uksouth")
            .withExistingResourceGroup("rgdiscovery")
            .withTags(mapOf("key5625", "fakeTokenPlaceholder"))
            .withProperties(new SupercomputerProperties().withSubnetId(
                "/subscriptions/31735C59-6307-4464-8B80-3675223F23D2/providers/Microsoft.Network/virtualNetworks/virtualnetwork1/subnets/subnet1")
                .withManagementSubnetId(
                    "/subscriptions/31735C59-6307-4464-8B80-3675223F23D2/providers/Microsoft.Network/virtualNetworks/virtualnetwork1/subnets/managementSubnet1")
                .withOutboundType(NetworkEgressType.LOAD_BALANCER)
                .withSystemSku(SystemSku.STANDARD_D4S_V6)
                .withIdentities(new SupercomputerIdentities().withClusterIdentity(new Identity().withId(
                    "/subscriptions/31735C59-6307-4464-8B80-3675223F23D2/providers/Microsoft.ManagedIdentity/userAssignedIdentities/managedid1"))
                    .withKubeletIdentity(new Identity().withId(
                        "/subscriptions/31735C59-6307-4464-8B80-3675223F23D2/providers/Microsoft.ManagedIdentity/userAssignedIdentities/managedid1"))
                    .withWorkloadIdentities(mapOf("key1149", new UserAssignedIdentity())))
                .withCustomerManagedKeys(CustomerManagedKeys.ENABLED)
                .withDiskEncryptionSetId(
                    "/subscriptions/31735C59-6307-4464-8B80-3675223F23D2/providers/Microsoft.Compute/diskEncryptionSets/diskencryptionset1")
                .withLogAnalyticsClusterId(
                    "/subscriptions/31735C59-6307-4464-8B80-3675223F23D2/providers/Microsoft.OperationalInsights/clusters/cluster1"))
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

### Supercomputers_Delete

```java
/**
 * Samples for Supercomputers Delete.
 */
public final class SupercomputersDeleteSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/Supercomputers_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Supercomputers_Delete_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void supercomputersDeleteMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.supercomputers().delete("rgdiscovery", "44f7621cf75873fb53", com.azure.core.util.Context.NONE);
    }
}
```

### Supercomputers_GetByResourceGroup

```java
/**
 * Samples for Supercomputers GetByResourceGroup.
 */
public final class SupercomputersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/Supercomputers_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Supercomputers_Get_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void supercomputersGetMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.supercomputers()
            .getByResourceGroupWithResponse("rgdiscovery", "b6807d2513b2fdb240", com.azure.core.util.Context.NONE);
    }
}
```

### Supercomputers_List

```java
/**
 * Samples for Supercomputers List.
 */
public final class SupercomputersListSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/Supercomputers_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: Supercomputers_ListBySubscription_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void
        supercomputersListBySubscriptionMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.supercomputers().list(com.azure.core.util.Context.NONE);
    }
}
```

### Supercomputers_ListByResourceGroup

```java
/**
 * Samples for Supercomputers ListByResourceGroup.
 */
public final class SupercomputersListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/Supercomputers_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: Supercomputers_ListByResourceGroup_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void
        supercomputersListByResourceGroupMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.supercomputers().listByResourceGroup("rgdiscovery", com.azure.core.util.Context.NONE);
    }
}
```

### Supercomputers_Update

```java
import com.azure.resourcemanager.discovery.models.Supercomputer;
import com.azure.resourcemanager.discovery.models.SupercomputerIdentities;
import com.azure.resourcemanager.discovery.models.SupercomputerProperties;
import com.azure.resourcemanager.discovery.models.UserAssignedIdentity;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Supercomputers Update.
 */
public final class SupercomputersUpdateSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/Supercomputers_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: Supercomputers_Update_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void supercomputersUpdateMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        Supercomputer resource = manager.supercomputers()
            .getByResourceGroupWithResponse("rgdiscovery", "a60016ec51d9d8e35d", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key9318", "fakeTokenPlaceholder"))
            .withProperties(new SupercomputerProperties().withIdentities(
                new SupercomputerIdentities().withWorkloadIdentities(mapOf("key3032", new UserAssignedIdentity()))))
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

### Tools_CreateOrUpdate

```java
import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.discovery.models.ToolProperties;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Tools CreateOrUpdate.
 */
public final class ToolsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/Tools_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: Tools_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void toolsCreateOrUpdateMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.tools()
            .define("b5d4239f788c20b58b")
            .withRegion("uksouth")
            .withExistingResourceGroup("rgdiscovery")
            .withTags(mapOf("key2611", "fakeTokenPlaceholder"))
            .withProperties(new ToolProperties().withVersion("sjepxewtq")
                .withEnvironmentVariables(mapOf("key5460", "fakeTokenPlaceholder"))
                .withDefinitionContent(mapOf("tool_id",
                    BinaryData.fromBytes("discovery-m1".getBytes(StandardCharsets.UTF_8)), "name",
                    BinaryData.fromBytes("discovery".getBytes(StandardCharsets.UTF_8)), "description",
                    BinaryData.fromBytes(
                        "Advanced DFT computational tools for molecular geometry optimization and property calculations"
                            .getBytes(StandardCharsets.UTF_8)),
                    "actions",
                    BinaryData.fromBytes(
                        "[{name=GeometryOptimization, description=Optimize geometry of 'xyz's from the input data asset. This is a prerequisite for all other discovery computations., input_schema={type=object, properties={inputDataAssetId={type=string, description=Identifier of the input data asset}, xyzColumnName={type=string, description=Column containing xyz data within the input data table asset}, outputDataAssetId={type=string, description=Identifier to use for the new output data asset which will be created.}, basisSet={type=string, description=Basis set. Must be one of the supported basis sets (e.g., def2-svp, def2-tzvp).}}, required=[inputDataAssetId, xyzColumnName]}, command=python3 submit_dft.py , environment_variables=[{name=OUTPUT_DIRECTORY_PATH, value={{ outputDataAssetId }}}]}]"
                            .getBytes(StandardCharsets.UTF_8)))))
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

### Tools_Delete

```java
/**
 * Samples for Tools Delete.
 */
public final class ToolsDeleteSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/Tools_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Tools_Delete_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void toolsDeleteMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.tools().delete("rgdiscovery", "d0e8e07484db1bb9a9", com.azure.core.util.Context.NONE);
    }
}
```

### Tools_GetByResourceGroup

```java
/**
 * Samples for Tools GetByResourceGroup.
 */
public final class ToolsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/Tools_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Tools_Get_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void toolsGetMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.tools()
            .getByResourceGroupWithResponse("rgdiscovery", "30ebfda6785888d26f", com.azure.core.util.Context.NONE);
    }
}
```

### Tools_List

```java
/**
 * Samples for Tools List.
 */
public final class ToolsListSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/Tools_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: Tools_ListBySubscription_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void toolsListBySubscriptionMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.tools().list(com.azure.core.util.Context.NONE);
    }
}
```

### Tools_ListByResourceGroup

```java
/**
 * Samples for Tools ListByResourceGroup.
 */
public final class ToolsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/Tools_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: Tools_ListByResourceGroup_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void
        toolsListByResourceGroupMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.tools().listByResourceGroup("rgdiscovery", com.azure.core.util.Context.NONE);
    }
}
```

### Tools_Update

```java
import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.discovery.models.Tool;
import com.azure.resourcemanager.discovery.models.ToolProperties;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Tools Update.
 */
public final class ToolsUpdateSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/Tools_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: Tools_Update_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void toolsUpdateMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        Tool resource = manager.tools()
            .getByResourceGroupWithResponse("rgdiscovery", "f1972d0fc9531d424c", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key4187", "fakeTokenPlaceholder"))
            .withProperties(new ToolProperties().withVersion("jittnzvso")
                .withEnvironmentVariables(mapOf("key3840", "fakeTokenPlaceholder"))
                .withDefinitionContent(mapOf("tool_id",
                    BinaryData.fromBytes("discovery-m1".getBytes(StandardCharsets.UTF_8)), "name",
                    BinaryData.fromBytes("discovery".getBytes(StandardCharsets.UTF_8)), "description",
                    BinaryData.fromBytes(
                        "Advanced DFT computational tools for molecular geometry optimization and property calculations"
                            .getBytes(StandardCharsets.UTF_8)),
                    "actions",
                    BinaryData.fromBytes(
                        "[{name=GeometryOptimization, description=Optimize geometry of 'xyz's from the input data asset. This is a prerequisite for all other discovery computations., input_schema={type=object, properties={inputDataAssetId={type=string, description=Identifier of the input data asset}, xyzColumnName={type=string, description=Column containing xyz data within the input data table asset}, outputDataAssetId={type=string, description=Identifier to use for the new output data asset which will be created.}, basisSet={type=string, description=Basis set. Must be one of the supported basis sets (e.g., def2-svp, def2-tzvp).}}, required=[inputDataAssetId, xyzColumnName]}, command=python3 submit_dft.py , environment_variables=[{name=OUTPUT_DIRECTORY_PATH, value={{ outputDataAssetId }}}]}]"
                            .getBytes(StandardCharsets.UTF_8)))))
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

### WorkspacePrivateEndpointConnections_CreateOrUpdate

```java
import com.azure.resourcemanager.discovery.models.PrivateEndpoint;
import com.azure.resourcemanager.discovery.models.PrivateEndpointConnectionProperties;
import com.azure.resourcemanager.discovery.models.PrivateEndpointServiceConnectionStatus;
import com.azure.resourcemanager.discovery.models.PrivateLinkServiceConnectionState;

/**
 * Samples for WorkspacePrivateEndpointConnections CreateOrUpdate.
 */
public final class WorkspacePrivateEndpointConnectionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/WorkspacePrivateEndpointConnections_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: WorkspacePrivateEndpointConnections_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void workspacePrivateEndpointConnectionsCreateOrUpdateMaximumSet(
        com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.workspacePrivateEndpointConnections()
            .define("connection")
            .withExistingWorkspace("rgdiscovery", "2b2ef1dfc273d99493")
            .withProperties(
                new PrivateEndpointConnectionProperties().withPrivateEndpoint(new PrivateEndpoint())
                    .withPrivateLinkServiceConnectionState(new PrivateLinkServiceConnectionState()
                        .withStatus(PrivateEndpointServiceConnectionStatus.PENDING)
                        .withDescription("lknyprq")
                        .withActionsRequired("vgqhrxvmviabfgmafqtbej")))
            .create();
    }
}
```

### WorkspacePrivateEndpointConnections_Delete

```java
/**
 * Samples for WorkspacePrivateEndpointConnections Delete.
 */
public final class WorkspacePrivateEndpointConnectionsDeleteSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/WorkspacePrivateEndpointConnections_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: WorkspacePrivateEndpointConnections_Delete_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void workspacePrivateEndpointConnectionsDeleteMaximumSet(
        com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.workspacePrivateEndpointConnections()
            .delete("rgdiscovery", "1e2a3df721db9f3406", "connection", com.azure.core.util.Context.NONE);
    }
}
```

### WorkspacePrivateEndpointConnections_Get

```java
/**
 * Samples for WorkspacePrivateEndpointConnections Get.
 */
public final class WorkspacePrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/WorkspacePrivateEndpointConnections_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: WorkspacePrivateEndpointConnections_Get_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void
        workspacePrivateEndpointConnectionsGetMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.workspacePrivateEndpointConnections()
            .getWithResponse("rgdiscovery", "16e7096454e0394819", "connection", com.azure.core.util.Context.NONE);
    }
}
```

### WorkspacePrivateEndpointConnections_ListByWorkspace

```java
/**
 * Samples for WorkspacePrivateEndpointConnections ListByWorkspace.
 */
public final class WorkspacePrivateEndpointConnectionsListByWorkspaceSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/WorkspacePrivateEndpointConnections_ListByWorkspace_MaximumSet_Gen.json
     */
    /**
     * Sample code: WorkspacePrivateEndpointConnections_ListByWorkspace_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void workspacePrivateEndpointConnectionsListByWorkspaceMaximumSet(
        com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.workspacePrivateEndpointConnections()
            .listByWorkspace("rgdiscovery", "cc28db0ff1bebbe39b", com.azure.core.util.Context.NONE);
    }
}
```

### WorkspacePrivateLinkResources_Get

```java
/**
 * Samples for WorkspacePrivateLinkResources Get.
 */
public final class WorkspacePrivateLinkResourcesGetSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/WorkspacePrivateLinkResources_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: WorkspacePrivateLinkResources_Get_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void
        workspacePrivateLinkResourcesGetMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.workspacePrivateLinkResources()
            .getWithResponse("rgdiscovery", "68b05b24fa2cc1a943", "connection", com.azure.core.util.Context.NONE);
    }
}
```

### WorkspacePrivateLinkResources_ListByWorkspace

```java
/**
 * Samples for WorkspacePrivateLinkResources ListByWorkspace.
 */
public final class WorkspacePrivateLinkResourcesListByWorkspaceSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/WorkspacePrivateLinkResources_ListByWorkspace_MaximumSet_Gen.json
     */
    /**
     * Sample code: WorkspacePrivateLinkResources_ListByWorkspace_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void workspacePrivateLinkResourcesListByWorkspaceMaximumSet(
        com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.workspacePrivateLinkResources()
            .listByWorkspace("rgdiscovery", "3a737dc9780bdefdff", com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_CreateOrUpdate

```java
import com.azure.resourcemanager.discovery.models.CustomerManagedKeys;
import com.azure.resourcemanager.discovery.models.Identity;
import com.azure.resourcemanager.discovery.models.KeyVaultProperties;
import com.azure.resourcemanager.discovery.models.PublicNetworkAccess;
import com.azure.resourcemanager.discovery.models.WorkspaceProperties;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Workspaces CreateOrUpdate.
 */
public final class WorkspacesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/Workspaces_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: Workspaces_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void
        workspacesCreateOrUpdateMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.workspaces()
            .define("b8d58cd85996a6dea3")
            .withRegion("uksouth")
            .withExistingResourceGroup("rgdiscovery")
            .withTags(mapOf("key5364", "fakeTokenPlaceholder"))
            .withProperties(new WorkspaceProperties().withSupercomputerIds(Arrays.asList(
                "/subscriptions/31735C59-6307-4464-8B80-3675223F23D2/resourceGroups/rgdiscovery/providers/Microsoft.Discovery/supercomputers/supercomputer12"))
                .withWorkspaceIdentity(new Identity().withId(
                    "/subscriptions/31735C59-6307-4464-8B80-3675223F23D2/providers/Microsoft.ManagedIdentity/userAssignedIdentities/managedid1"))
                .withCustomerManagedKeys(CustomerManagedKeys.ENABLED)
                .withKeyVaultProperties(new KeyVaultProperties().withKeyVaultUri("fakeTokenPlaceholder")
                    .withKeyName("fakeTokenPlaceholder")
                    .withKeyVersion("fakeTokenPlaceholder"))
                .withLogAnalyticsClusterId(
                    "/subscriptions/31735C59-6307-4464-8B80-3675223F23D2/providers/Microsoft.OperationalInsights/clusters/cluster1")
                .withPublicNetworkAccess(PublicNetworkAccess.ENABLED)
                .withAgentSubnetId(
                    "/subscriptions/31735C59-6307-4464-8B80-3675223F23D2/providers/Microsoft.Network/virtualNetworks/virtualnetwork1/subnets/agentSubnet1")
                .withPrivateEndpointSubnetId(
                    "/subscriptions/31735C59-6307-4464-8B80-3675223F23D2/providers/Microsoft.Network/virtualNetworks/virtualnetwork1/subnets/privateEndpointSubnet1")
                .withWorkspaceSubnetId(
                    "/subscriptions/31735C59-6307-4464-8B80-3675223F23D2/providers/Microsoft.Network/virtualNetworks/virtualnetwork1/subnets/workspaceSubnet1"))
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
     * x-ms-original-file: 2026-02-01-preview/Workspaces_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Workspaces_Delete_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void workspacesDeleteMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.workspaces().delete("rgdiscovery", "f1559ab1ef72a2eae5", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-02-01-preview/Workspaces_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Workspaces_Get_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void workspacesGetMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.workspaces()
            .getByResourceGroupWithResponse("rgdiscovery", "0e6a06e55e7efe8f07", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-02-01-preview/Workspaces_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: Workspaces_ListBySubscription_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void
        workspacesListBySubscriptionMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
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
     * x-ms-original-file: 2026-02-01-preview/Workspaces_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: Workspaces_ListByResourceGroup_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void
        workspacesListByResourceGroupMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        manager.workspaces().listByResourceGroup("rgdiscovery", com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_Update

```java
import com.azure.resourcemanager.discovery.models.KeyVaultProperties;
import com.azure.resourcemanager.discovery.models.PublicNetworkAccess;
import com.azure.resourcemanager.discovery.models.Workspace;
import com.azure.resourcemanager.discovery.models.WorkspaceProperties;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Workspaces Update.
 */
public final class WorkspacesUpdateSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/Workspaces_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: Workspaces_Update_MaximumSet.
     * 
     * @param manager Entry point to DiscoveryManager.
     */
    public static void workspacesUpdateMaximumSet(com.azure.resourcemanager.discovery.DiscoveryManager manager) {
        Workspace resource = manager.workspaces()
            .getByResourceGroupWithResponse("rgdiscovery", "43ac331aecf462b646", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key6612", "fakeTokenPlaceholder"))
            .withProperties(new WorkspaceProperties().withSupercomputerIds(Arrays.asList(
                "/subscriptions/31735C59-6307-4464-8B80-3675223F23D2/resourceGroups/rgdiscovery/providers/Microsoft.Discovery/supercomputers/supercomputer12"))
                .withKeyVaultProperties(
                    new KeyVaultProperties().withKeyName("fakeTokenPlaceholder").withKeyVersion("fakeTokenPlaceholder"))
                .withPublicNetworkAccess(PublicNetworkAccess.ENABLED))
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

