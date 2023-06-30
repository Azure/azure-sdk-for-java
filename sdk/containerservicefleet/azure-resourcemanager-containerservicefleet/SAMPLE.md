# Code snippets and samples


## FleetMembers

- [Create](#fleetmembers_create)
- [Delete](#fleetmembers_delete)
- [Get](#fleetmembers_get)
- [ListByFleet](#fleetmembers_listbyfleet)
- [Update](#fleetmembers_update)

## Fleets

- [CreateOrUpdate](#fleets_createorupdate)
- [Delete](#fleets_delete)
- [GetByResourceGroup](#fleets_getbyresourcegroup)
- [List](#fleets_list)
- [ListByResourceGroup](#fleets_listbyresourcegroup)
- [ListCredentials](#fleets_listcredentials)
- [Update](#fleets_update)

## Operations

- [List](#operations_list)

## UpdateRuns

- [CreateOrUpdate](#updateruns_createorupdate)
- [Delete](#updateruns_delete)
- [Get](#updateruns_get)
- [ListByFleet](#updateruns_listbyfleet)
- [Start](#updateruns_start)
- [Stop](#updateruns_stop)
### FleetMembers_Create

```java
/** Samples for FleetMembers Create. */
public final class FleetMembersCreateSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/fleet/preview/2023-03-15-preview/examples/FleetMembers_Create.json
     */
    /**
     * Sample code: Creates a FleetMember resource with a long running operation.
     *
     * @param manager Entry point to ContainerServiceFleetManager.
     */
    public static void createsAFleetMemberResourceWithALongRunningOperation(
        com.azure.resourcemanager.containerservicefleet.ContainerServiceFleetManager manager) {
        manager
            .fleetMembers()
            .define("member-1")
            .withExistingFleet("rg1", "fleet1")
            .withClusterResourceId(
                "/subscriptions/subid1/resourcegroups/rg1/providers/Microsoft.ContainerService/managedClusters/cluster-1")
            .create();
    }
}
```

### FleetMembers_Delete

```java
/** Samples for FleetMembers Delete. */
public final class FleetMembersDeleteSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/fleet/preview/2023-03-15-preview/examples/FleetMembers_Delete.json
     */
    /**
     * Sample code: Deletes a FleetMember resource asynchronously with a long running operation.
     *
     * @param manager Entry point to ContainerServiceFleetManager.
     */
    public static void deletesAFleetMemberResourceAsynchronouslyWithALongRunningOperation(
        com.azure.resourcemanager.containerservicefleet.ContainerServiceFleetManager manager) {
        manager.fleetMembers().delete("rg1", "fleet1", "member-1", null, com.azure.core.util.Context.NONE);
    }
}
```

### FleetMembers_Get

```java
/** Samples for FleetMembers Get. */
public final class FleetMembersGetSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/fleet/preview/2023-03-15-preview/examples/FleetMembers_Get.json
     */
    /**
     * Sample code: Gets a FleetMember resource.
     *
     * @param manager Entry point to ContainerServiceFleetManager.
     */
    public static void getsAFleetMemberResource(
        com.azure.resourcemanager.containerservicefleet.ContainerServiceFleetManager manager) {
        manager.fleetMembers().getWithResponse("rg1", "fleet1", "member-1", com.azure.core.util.Context.NONE);
    }
}
```

### FleetMembers_ListByFleet

```java
/** Samples for FleetMembers ListByFleet. */
public final class FleetMembersListByFleetSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/fleet/preview/2023-03-15-preview/examples/FleetMembers_ListByFleet.json
     */
    /**
     * Sample code: Lists the members of a Fleet.
     *
     * @param manager Entry point to ContainerServiceFleetManager.
     */
    public static void listsTheMembersOfAFleet(
        com.azure.resourcemanager.containerservicefleet.ContainerServiceFleetManager manager) {
        manager.fleetMembers().listByFleet("rg1", "fleet1", com.azure.core.util.Context.NONE);
    }
}
```

### FleetMembers_Update

```java
import com.azure.resourcemanager.containerservicefleet.models.FleetMember;

/** Samples for FleetMembers Update. */
public final class FleetMembersUpdateSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/fleet/preview/2023-03-15-preview/examples/FleetMembers_Update.json
     */
    /**
     * Sample code: Updates a FleetMember resource synchronously.
     *
     * @param manager Entry point to ContainerServiceFleetManager.
     */
    public static void updatesAFleetMemberResourceSynchronously(
        com.azure.resourcemanager.containerservicefleet.ContainerServiceFleetManager manager) {
        FleetMember resource =
            manager
                .fleetMembers()
                .getWithResponse("rg1", "fleet1", "member-1", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withGroup("staging").apply();
    }
}
```

### Fleets_CreateOrUpdate

```java
import com.azure.resourcemanager.containerservicefleet.models.FleetHubProfile;
import java.util.HashMap;
import java.util.Map;

/** Samples for Fleets CreateOrUpdate. */
public final class FleetsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/fleet/preview/2023-03-15-preview/examples/Fleets_CreateOrUpdate.json
     */
    /**
     * Sample code: Creates a Fleet resource with a long running operation.
     *
     * @param manager Entry point to ContainerServiceFleetManager.
     */
    public static void createsAFleetResourceWithALongRunningOperation(
        com.azure.resourcemanager.containerservicefleet.ContainerServiceFleetManager manager) {
        manager
            .fleets()
            .define("fleet1")
            .withRegion("East US")
            .withExistingResourceGroup("rg1")
            .withTags(mapOf("archv2", "", "tier", "production"))
            .withHubProfile(new FleetHubProfile().withDnsPrefix("dnsprefix1"))
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

### Fleets_Delete

```java
/** Samples for Fleets Delete. */
public final class FleetsDeleteSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/fleet/preview/2023-03-15-preview/examples/Fleets_Delete.json
     */
    /**
     * Sample code: Deletes a Fleet resource asynchronously with a long running operation.
     *
     * @param manager Entry point to ContainerServiceFleetManager.
     */
    public static void deletesAFleetResourceAsynchronouslyWithALongRunningOperation(
        com.azure.resourcemanager.containerservicefleet.ContainerServiceFleetManager manager) {
        manager.fleets().delete("rg1", "fleet1", null, com.azure.core.util.Context.NONE);
    }
}
```

### Fleets_GetByResourceGroup

```java
/** Samples for Fleets GetByResourceGroup. */
public final class FleetsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/fleet/preview/2023-03-15-preview/examples/Fleets_Get.json
     */
    /**
     * Sample code: Gets a Fleet resource.
     *
     * @param manager Entry point to ContainerServiceFleetManager.
     */
    public static void getsAFleetResource(
        com.azure.resourcemanager.containerservicefleet.ContainerServiceFleetManager manager) {
        manager.fleets().getByResourceGroupWithResponse("rg1", "fleet1", com.azure.core.util.Context.NONE);
    }
}
```

### Fleets_List

```java
/** Samples for Fleets List. */
public final class FleetsListSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/fleet/preview/2023-03-15-preview/examples/Fleets_ListBySub.json
     */
    /**
     * Sample code: Lists the Fleet resources in a subscription.
     *
     * @param manager Entry point to ContainerServiceFleetManager.
     */
    public static void listsTheFleetResourcesInASubscription(
        com.azure.resourcemanager.containerservicefleet.ContainerServiceFleetManager manager) {
        manager.fleets().list(com.azure.core.util.Context.NONE);
    }
}
```

### Fleets_ListByResourceGroup

```java
/** Samples for Fleets ListByResourceGroup. */
public final class FleetsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/fleet/preview/2023-03-15-preview/examples/Fleets_ListByResourceGroup.json
     */
    /**
     * Sample code: Lists the Fleet resources in a resource group.
     *
     * @param manager Entry point to ContainerServiceFleetManager.
     */
    public static void listsTheFleetResourcesInAResourceGroup(
        com.azure.resourcemanager.containerservicefleet.ContainerServiceFleetManager manager) {
        manager.fleets().listByResourceGroup("rg1", com.azure.core.util.Context.NONE);
    }
}
```

### Fleets_ListCredentials

```java
/** Samples for Fleets ListCredentials. */
public final class FleetsListCredentialsSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/fleet/preview/2023-03-15-preview/examples/Fleets_ListCredentialsResult.json
     */
    /**
     * Sample code: Lists the user credentials of a Fleet.
     *
     * @param manager Entry point to ContainerServiceFleetManager.
     */
    public static void listsTheUserCredentialsOfAFleet(
        com.azure.resourcemanager.containerservicefleet.ContainerServiceFleetManager manager) {
        manager.fleets().listCredentialsWithResponse("rg1", "fleet", com.azure.core.util.Context.NONE);
    }
}
```

### Fleets_Update

```java
import com.azure.resourcemanager.containerservicefleet.models.Fleet;
import java.util.HashMap;
import java.util.Map;

/** Samples for Fleets Update. */
public final class FleetsUpdateSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/fleet/preview/2023-03-15-preview/examples/Fleets_PatchTags.json
     */
    /**
     * Sample code: Update a Fleet.
     *
     * @param manager Entry point to ContainerServiceFleetManager.
     */
    public static void updateAFleet(
        com.azure.resourcemanager.containerservicefleet.ContainerServiceFleetManager manager) {
        Fleet resource =
            manager
                .fleets()
                .getByResourceGroupWithResponse("rg1", "fleet1", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("env", "prod", "tier", "secure")).withIfMatch("dfjkwelr7384").apply();
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
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/fleet/preview/2023-03-15-preview/examples/Operations_List.json
     */
    /**
     * Sample code: List the operations for the provider.
     *
     * @param manager Entry point to ContainerServiceFleetManager.
     */
    public static void listTheOperationsForTheProvider(
        com.azure.resourcemanager.containerservicefleet.ContainerServiceFleetManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### UpdateRuns_CreateOrUpdate

```java
import com.azure.resourcemanager.containerservicefleet.models.ManagedClusterUpdate;
import com.azure.resourcemanager.containerservicefleet.models.ManagedClusterUpgradeSpec;
import com.azure.resourcemanager.containerservicefleet.models.ManagedClusterUpgradeType;
import com.azure.resourcemanager.containerservicefleet.models.UpdateGroup;
import com.azure.resourcemanager.containerservicefleet.models.UpdateRunStrategy;
import com.azure.resourcemanager.containerservicefleet.models.UpdateStage;
import java.util.Arrays;

/** Samples for UpdateRuns CreateOrUpdate. */
public final class UpdateRunsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/fleet/preview/2023-03-15-preview/examples/UpdateRuns_CreateOrUpdate.json
     */
    /**
     * Sample code: Create an UpdateRun.
     *
     * @param manager Entry point to ContainerServiceFleetManager.
     */
    public static void createAnUpdateRun(
        com.azure.resourcemanager.containerservicefleet.ContainerServiceFleetManager manager) {
        manager
            .updateRuns()
            .define("run1")
            .withExistingFleet("rg1", "fleet1")
            .withStrategy(
                new UpdateRunStrategy()
                    .withStages(
                        Arrays
                            .asList(
                                new UpdateStage()
                                    .withName("stage1")
                                    .withGroups(Arrays.asList(new UpdateGroup().withName("group-a")))
                                    .withAfterStageWaitInSeconds(3600))))
            .withManagedClusterUpdate(
                new ManagedClusterUpdate()
                    .withUpgrade(
                        new ManagedClusterUpgradeSpec()
                            .withType(ManagedClusterUpgradeType.FULL)
                            .withKubernetesVersion("1.26.1")))
            .create();
    }
}
```

### UpdateRuns_Delete

```java
/** Samples for UpdateRuns Delete. */
public final class UpdateRunsDeleteSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/fleet/preview/2023-03-15-preview/examples/UpdateRuns_Delete.json
     */
    /**
     * Sample code: Delete an updateRun resource.
     *
     * @param manager Entry point to ContainerServiceFleetManager.
     */
    public static void deleteAnUpdateRunResource(
        com.azure.resourcemanager.containerservicefleet.ContainerServiceFleetManager manager) {
        manager.updateRuns().delete("rg1", "fleet1", "run1", null, com.azure.core.util.Context.NONE);
    }
}
```

### UpdateRuns_Get

```java
/** Samples for UpdateRuns Get. */
public final class UpdateRunsGetSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/fleet/preview/2023-03-15-preview/examples/UpdateRuns_Get.json
     */
    /**
     * Sample code: Gets an UpdateRun resource.
     *
     * @param manager Entry point to ContainerServiceFleetManager.
     */
    public static void getsAnUpdateRunResource(
        com.azure.resourcemanager.containerservicefleet.ContainerServiceFleetManager manager) {
        manager.updateRuns().getWithResponse("rg1", "fleet1", "run1", com.azure.core.util.Context.NONE);
    }
}
```

### UpdateRuns_ListByFleet

```java
/** Samples for UpdateRuns ListByFleet. */
public final class UpdateRunsListByFleetSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/fleet/preview/2023-03-15-preview/examples/UpdateRuns_ListByFleet.json
     */
    /**
     * Sample code: Lists the UpdateRun resources by fleet.
     *
     * @param manager Entry point to ContainerServiceFleetManager.
     */
    public static void listsTheUpdateRunResourcesByFleet(
        com.azure.resourcemanager.containerservicefleet.ContainerServiceFleetManager manager) {
        manager.updateRuns().listByFleet("rg1", "fleet1", com.azure.core.util.Context.NONE);
    }
}
```

### UpdateRuns_Start

```java
/** Samples for UpdateRuns Start. */
public final class UpdateRunsStartSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/fleet/preview/2023-03-15-preview/examples/UpdateRuns_Start.json
     */
    /**
     * Sample code: Starts an UpdateRun.
     *
     * @param manager Entry point to ContainerServiceFleetManager.
     */
    public static void startsAnUpdateRun(
        com.azure.resourcemanager.containerservicefleet.ContainerServiceFleetManager manager) {
        manager.updateRuns().start("rg1", "fleet1", "run1", null, com.azure.core.util.Context.NONE);
    }
}
```

### UpdateRuns_Stop

```java
/** Samples for UpdateRuns Stop. */
public final class UpdateRunsStopSamples {
    /*
     * x-ms-original-file: specification/containerservice/resource-manager/Microsoft.ContainerService/fleet/preview/2023-03-15-preview/examples/UpdateRuns_Stop.json
     */
    /**
     * Sample code: Stops an UpdateRun.
     *
     * @param manager Entry point to ContainerServiceFleetManager.
     */
    public static void stopsAnUpdateRun(
        com.azure.resourcemanager.containerservicefleet.ContainerServiceFleetManager manager) {
        manager.updateRuns().stop("rg1", "fleet1", "run1", null, com.azure.core.util.Context.NONE);
    }
}
```

