# Code snippets and samples


## MoveCollections

- [BulkRemove](#movecollections_bulkremove)
- [Commit](#movecollections_commit)
- [Create](#movecollections_create)
- [Delete](#movecollections_delete)
- [Discard](#movecollections_discard)
- [GetByResourceGroup](#movecollections_getbyresourcegroup)
- [InitiateMove](#movecollections_initiatemove)
- [List](#movecollections_list)
- [ListByResourceGroup](#movecollections_listbyresourcegroup)
- [ListRequiredFor](#movecollections_listrequiredfor)
- [Prepare](#movecollections_prepare)
- [ResolveDependencies](#movecollections_resolvedependencies)
- [Update](#movecollections_update)

## MoveResources

- [Create](#moveresources_create)
- [Delete](#moveresources_delete)
- [Get](#moveresources_get)
- [List](#moveresources_list)

## OperationsDiscovery

- [Get](#operationsdiscovery_get)

## UnresolvedDependencies

- [Get](#unresolveddependencies_get)
### MoveCollections_BulkRemove

```java
import com.azure.resourcemanager.resourcemover.models.BulkRemoveRequest;
import java.util.Arrays;

/** Samples for MoveCollections BulkRemove. */
public final class MoveCollectionsBulkRemoveSamples {
    /*
     * x-ms-original-file: specification/resourcemover/resource-manager/Microsoft.Migrate/stable/2022-08-01/examples/MoveCollections_BulkRemove.json
     */
    /**
     * Sample code: MoveCollections_BulkRemove.
     *
     * @param manager Entry point to ResourceMoverManager.
     */
    public static void moveCollectionsBulkRemove(com.azure.resourcemanager.resourcemover.ResourceMoverManager manager) {
        manager
            .moveCollections()
            .bulkRemove(
                "rg1",
                "movecollection1",
                new BulkRemoveRequest()
                    .withValidateOnly(false)
                    .withMoveResources(
                        Arrays
                            .asList(
                                "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.Migrate/MoveCollections/movecollection1/MoveResources/moveresource1")),
                com.azure.core.util.Context.NONE);
    }
}
```

### MoveCollections_Commit

```java
import com.azure.resourcemanager.resourcemover.models.CommitRequest;
import java.util.Arrays;

/** Samples for MoveCollections Commit. */
public final class MoveCollectionsCommitSamples {
    /*
     * x-ms-original-file: specification/resourcemover/resource-manager/Microsoft.Migrate/stable/2022-08-01/examples/MoveCollections_Commit.json
     */
    /**
     * Sample code: MoveCollections_Commit.
     *
     * @param manager Entry point to ResourceMoverManager.
     */
    public static void moveCollectionsCommit(com.azure.resourcemanager.resourcemover.ResourceMoverManager manager) {
        manager
            .moveCollections()
            .commit(
                "rg1",
                "movecollection1",
                new CommitRequest()
                    .withValidateOnly(false)
                    .withMoveResources(
                        Arrays
                            .asList(
                                "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.Migrate/MoveCollections/movecollection1/MoveResources/moveresource1")),
                com.azure.core.util.Context.NONE);
    }
}
```

### MoveCollections_Create

```java
import com.azure.resourcemanager.resourcemover.models.Identity;
import com.azure.resourcemanager.resourcemover.models.MoveCollectionProperties;
import com.azure.resourcemanager.resourcemover.models.ResourceIdentityType;

/** Samples for MoveCollections Create. */
public final class MoveCollectionsCreateSamples {
    /*
     * x-ms-original-file: specification/resourcemover/resource-manager/Microsoft.Migrate/stable/2022-08-01/examples/MoveCollections_Create.json
     */
    /**
     * Sample code: MoveCollections_Create.
     *
     * @param manager Entry point to ResourceMoverManager.
     */
    public static void moveCollectionsCreate(com.azure.resourcemanager.resourcemover.ResourceMoverManager manager) {
        manager
            .moveCollections()
            .define("movecollection1")
            .withRegion("eastus2")
            .withExistingResourceGroup("rg1")
            .withIdentity(new Identity().withType(ResourceIdentityType.SYSTEM_ASSIGNED))
            .withProperties(new MoveCollectionProperties().withSourceRegion("eastus").withTargetRegion("westus"))
            .create();
    }
}
```

### MoveCollections_Delete

```java
/** Samples for MoveCollections Delete. */
public final class MoveCollectionsDeleteSamples {
    /*
     * x-ms-original-file: specification/resourcemover/resource-manager/Microsoft.Migrate/stable/2022-08-01/examples/MoveCollections_Delete.json
     */
    /**
     * Sample code: MoveCollections_Delete.
     *
     * @param manager Entry point to ResourceMoverManager.
     */
    public static void moveCollectionsDelete(com.azure.resourcemanager.resourcemover.ResourceMoverManager manager) {
        manager.moveCollections().delete("rg1", "movecollection1", com.azure.core.util.Context.NONE);
    }
}
```

### MoveCollections_Discard

```java
import com.azure.resourcemanager.resourcemover.models.DiscardRequest;
import java.util.Arrays;

/** Samples for MoveCollections Discard. */
public final class MoveCollectionsDiscardSamples {
    /*
     * x-ms-original-file: specification/resourcemover/resource-manager/Microsoft.Migrate/stable/2022-08-01/examples/MoveCollections_Discard.json
     */
    /**
     * Sample code: MoveCollections_Discard.
     *
     * @param manager Entry point to ResourceMoverManager.
     */
    public static void moveCollectionsDiscard(com.azure.resourcemanager.resourcemover.ResourceMoverManager manager) {
        manager
            .moveCollections()
            .discard(
                "rg1",
                "movecollection1",
                new DiscardRequest()
                    .withValidateOnly(false)
                    .withMoveResources(
                        Arrays
                            .asList(
                                "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.Migrate/MoveCollections/movecollection1/MoveResources/moveresource1")),
                com.azure.core.util.Context.NONE);
    }
}
```

### MoveCollections_GetByResourceGroup

```java
/** Samples for MoveCollections GetByResourceGroup. */
public final class MoveCollectionsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/resourcemover/resource-manager/Microsoft.Migrate/stable/2022-08-01/examples/MoveCollections_Get.json
     */
    /**
     * Sample code: MoveCollections_Get.
     *
     * @param manager Entry point to ResourceMoverManager.
     */
    public static void moveCollectionsGet(com.azure.resourcemanager.resourcemover.ResourceMoverManager manager) {
        manager
            .moveCollections()
            .getByResourceGroupWithResponse("rg1", "movecollection1", com.azure.core.util.Context.NONE);
    }
}
```

### MoveCollections_InitiateMove

```java
import com.azure.resourcemanager.resourcemover.models.ResourceMoveRequest;
import java.util.Arrays;

/** Samples for MoveCollections InitiateMove. */
public final class MoveCollectionsInitiateMoveSamples {
    /*
     * x-ms-original-file: specification/resourcemover/resource-manager/Microsoft.Migrate/stable/2022-08-01/examples/MoveCollections_InitiateMove.json
     */
    /**
     * Sample code: MoveCollections_InitiateMove.
     *
     * @param manager Entry point to ResourceMoverManager.
     */
    public static void moveCollectionsInitiateMove(
        com.azure.resourcemanager.resourcemover.ResourceMoverManager manager) {
        manager
            .moveCollections()
            .initiateMove(
                "rg1",
                "movecollection1",
                new ResourceMoveRequest()
                    .withValidateOnly(false)
                    .withMoveResources(
                        Arrays
                            .asList(
                                "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.Migrate/MoveCollections/movecollection1/MoveResources/moveresource1")),
                com.azure.core.util.Context.NONE);
    }
}
```

### MoveCollections_List

```java
/** Samples for MoveCollections List. */
public final class MoveCollectionsListSamples {
    /*
     * x-ms-original-file: specification/resourcemover/resource-manager/Microsoft.Migrate/stable/2022-08-01/examples/MoveCollections_ListMoveCollectionsBySubscription.json
     */
    /**
     * Sample code: MoveCollections_ListMoveCollectionsBySubscription.
     *
     * @param manager Entry point to ResourceMoverManager.
     */
    public static void moveCollectionsListMoveCollectionsBySubscription(
        com.azure.resourcemanager.resourcemover.ResourceMoverManager manager) {
        manager.moveCollections().list(com.azure.core.util.Context.NONE);
    }
}
```

### MoveCollections_ListByResourceGroup

```java
/** Samples for MoveCollections ListByResourceGroup. */
public final class MoveCollectionsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/resourcemover/resource-manager/Microsoft.Migrate/stable/2022-08-01/examples/MoveCollections_ListMoveCollectionsByResourceGroup.json
     */
    /**
     * Sample code: MoveCollections_ListMoveCollectionsByResourceGroup.
     *
     * @param manager Entry point to ResourceMoverManager.
     */
    public static void moveCollectionsListMoveCollectionsByResourceGroup(
        com.azure.resourcemanager.resourcemover.ResourceMoverManager manager) {
        manager.moveCollections().listByResourceGroup("rg1", com.azure.core.util.Context.NONE);
    }
}
```

### MoveCollections_ListRequiredFor

```java
/** Samples for MoveCollections ListRequiredFor. */
public final class MoveCollectionsListRequiredForSamples {
    /*
     * x-ms-original-file: specification/resourcemover/resource-manager/Microsoft.Migrate/stable/2022-08-01/examples/RequiredFor_Get.json
     */
    /**
     * Sample code: RequiredFor_Get.
     *
     * @param manager Entry point to ResourceMoverManager.
     */
    public static void requiredForGet(com.azure.resourcemanager.resourcemover.ResourceMoverManager manager) {
        manager
            .moveCollections()
            .listRequiredForWithResponse(
                "rg1",
                "movecollection1",
                "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.Network/virtualNetworks/nic1",
                com.azure.core.util.Context.NONE);
    }
}
```

### MoveCollections_Prepare

```java
import com.azure.resourcemanager.resourcemover.models.PrepareRequest;
import java.util.Arrays;

/** Samples for MoveCollections Prepare. */
public final class MoveCollectionsPrepareSamples {
    /*
     * x-ms-original-file: specification/resourcemover/resource-manager/Microsoft.Migrate/stable/2022-08-01/examples/MoveCollections_Prepare.json
     */
    /**
     * Sample code: MoveCollections_Prepare.
     *
     * @param manager Entry point to ResourceMoverManager.
     */
    public static void moveCollectionsPrepare(com.azure.resourcemanager.resourcemover.ResourceMoverManager manager) {
        manager
            .moveCollections()
            .prepare(
                "rg1",
                "movecollection1",
                new PrepareRequest()
                    .withValidateOnly(false)
                    .withMoveResources(
                        Arrays
                            .asList(
                                "/subscriptions/subid/resourceGroups/rg1/providers/Microsoft.Migrate/MoveCollections/movecollection1/MoveResources/moveresource1")),
                com.azure.core.util.Context.NONE);
    }
}
```

### MoveCollections_ResolveDependencies

```java
/** Samples for MoveCollections ResolveDependencies. */
public final class MoveCollectionsResolveDependenciesSamples {
    /*
     * x-ms-original-file: specification/resourcemover/resource-manager/Microsoft.Migrate/stable/2022-08-01/examples/MoveCollections_ResolveDependencies.json
     */
    /**
     * Sample code: MoveCollections_ResolveDependencies.
     *
     * @param manager Entry point to ResourceMoverManager.
     */
    public static void moveCollectionsResolveDependencies(
        com.azure.resourcemanager.resourcemover.ResourceMoverManager manager) {
        manager.moveCollections().resolveDependencies("rg1", "movecollection1", com.azure.core.util.Context.NONE);
    }
}
```

### MoveCollections_Update

```java
import com.azure.resourcemanager.resourcemover.models.Identity;
import com.azure.resourcemanager.resourcemover.models.MoveCollection;
import com.azure.resourcemanager.resourcemover.models.ResourceIdentityType;
import java.util.HashMap;
import java.util.Map;

/** Samples for MoveCollections Update. */
public final class MoveCollectionsUpdateSamples {
    /*
     * x-ms-original-file: specification/resourcemover/resource-manager/Microsoft.Migrate/stable/2022-08-01/examples/MoveCollections_Update.json
     */
    /**
     * Sample code: MoveCollections_Update.
     *
     * @param manager Entry point to ResourceMoverManager.
     */
    public static void moveCollectionsUpdate(com.azure.resourcemanager.resourcemover.ResourceMoverManager manager) {
        MoveCollection resource =
            manager
                .moveCollections()
                .getByResourceGroupWithResponse("rg1", "movecollection1", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("key1", "mc1"))
            .withIdentity(new Identity().withType(ResourceIdentityType.SYSTEM_ASSIGNED))
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

### MoveResources_Create

```java
import com.azure.resourcemanager.resourcemover.models.MoveResourceDependencyOverride;
import com.azure.resourcemanager.resourcemover.models.MoveResourceProperties;
import com.azure.resourcemanager.resourcemover.models.TargetAvailabilityZone;
import com.azure.resourcemanager.resourcemover.models.VirtualMachineResourceSettings;
import java.util.Arrays;

/** Samples for MoveResources Create. */
public final class MoveResourcesCreateSamples {
    /*
     * x-ms-original-file: specification/resourcemover/resource-manager/Microsoft.Migrate/stable/2022-08-01/examples/MoveResources_Create.json
     */
    /**
     * Sample code: MoveResources_Create.
     *
     * @param manager Entry point to ResourceMoverManager.
     */
    public static void moveResourcesCreate(com.azure.resourcemanager.resourcemover.ResourceMoverManager manager) {
        manager
            .moveResources()
            .define("moveresourcename1")
            .withExistingMoveCollection("rg1", "movecollection1")
            .withProperties(
                new MoveResourceProperties()
                    .withSourceId(
                        "/subscriptions/subid/resourceGroups/eastusRG/providers/Microsoft.Compute/virtualMachines/eastusvm1")
                    .withResourceSettings(
                        new VirtualMachineResourceSettings()
                            .withTargetResourceName("westusvm1")
                            .withUserManagedIdentities(
                                Arrays
                                    .asList(
                                        "/subscriptions/subid/resourceGroups/eastusRG/providers/Microsoft.ManagedIdentity/userAssignedIdentities/umi1"))
                            .withTargetAvailabilityZone(TargetAvailabilityZone.TWO)
                            .withTargetAvailabilitySetId(
                                "/subscriptions/subid/resourceGroups/eastusRG/providers/Microsoft.Compute/availabilitySets/avset1"))
                    .withDependsOnOverrides(
                        Arrays
                            .asList(
                                new MoveResourceDependencyOverride()
                                    .withId(
                                        "/subscriptions/c4488a3f-a7f7-4ad4-aa72-0e1f4d9c0756/resourceGroups/eastusRG/providers/Microsoft.Network/networkInterfaces/eastusvm140")
                                    .withTargetId(
                                        "/subscriptions/c4488a3f-a7f7-4ad4-aa72-0e1f4d9c0756/resourceGroups/westusRG/providers/Microsoft.Network/networkInterfaces/eastusvm140"))))
            .create();
    }
}
```

### MoveResources_Delete

```java
/** Samples for MoveResources Delete. */
public final class MoveResourcesDeleteSamples {
    /*
     * x-ms-original-file: specification/resourcemover/resource-manager/Microsoft.Migrate/stable/2022-08-01/examples/MoveResources_Delete.json
     */
    /**
     * Sample code: MoveResources_Delete.
     *
     * @param manager Entry point to ResourceMoverManager.
     */
    public static void moveResourcesDelete(com.azure.resourcemanager.resourcemover.ResourceMoverManager manager) {
        manager.moveResources().delete("rg1", "movecollection1", "moveresourcename1", com.azure.core.util.Context.NONE);
    }
}
```

### MoveResources_Get

```java
/** Samples for MoveResources Get. */
public final class MoveResourcesGetSamples {
    /*
     * x-ms-original-file: specification/resourcemover/resource-manager/Microsoft.Migrate/stable/2022-08-01/examples/MoveResources_Get.json
     */
    /**
     * Sample code: MoveResources_Get.
     *
     * @param manager Entry point to ResourceMoverManager.
     */
    public static void moveResourcesGet(com.azure.resourcemanager.resourcemover.ResourceMoverManager manager) {
        manager
            .moveResources()
            .getWithResponse("rg1", "movecollection1", "moveresourcename1", com.azure.core.util.Context.NONE);
    }
}
```

### MoveResources_List

```java
/** Samples for MoveResources List. */
public final class MoveResourcesListSamples {
    /*
     * x-ms-original-file: specification/resourcemover/resource-manager/Microsoft.Migrate/stable/2022-08-01/examples/MoveResources_List.json
     */
    /**
     * Sample code: MoveResources_List.
     *
     * @param manager Entry point to ResourceMoverManager.
     */
    public static void moveResourcesList(com.azure.resourcemanager.resourcemover.ResourceMoverManager manager) {
        manager.moveResources().list("rg1", "movecollection1", null, com.azure.core.util.Context.NONE);
    }
}
```

### OperationsDiscovery_Get

```java
/** Samples for OperationsDiscovery Get. */
public final class OperationsDiscoveryGetSamples {
    /*
     * x-ms-original-file: specification/resourcemover/resource-manager/Microsoft.Migrate/stable/2022-08-01/examples/OperationsDiscovery_Get.json
     */
    /**
     * Sample code: OperationsDiscovery_Get.
     *
     * @param manager Entry point to ResourceMoverManager.
     */
    public static void operationsDiscoveryGet(com.azure.resourcemanager.resourcemover.ResourceMoverManager manager) {
        manager.operationsDiscoveries().getWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

### UnresolvedDependencies_Get

```java
/** Samples for UnresolvedDependencies Get. */
public final class UnresolvedDependenciesGetSamples {
    /*
     * x-ms-original-file: specification/resourcemover/resource-manager/Microsoft.Migrate/stable/2022-08-01/examples/UnresolvedDependencies_Get.json
     */
    /**
     * Sample code: UnresolvedDependencies_Get.
     *
     * @param manager Entry point to ResourceMoverManager.
     */
    public static void unresolvedDependenciesGet(com.azure.resourcemanager.resourcemover.ResourceMoverManager manager) {
        manager
            .unresolvedDependencies()
            .get("rg1", "movecollection1", null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

