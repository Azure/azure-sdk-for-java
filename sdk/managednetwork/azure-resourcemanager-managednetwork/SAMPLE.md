# Code snippets and samples


## ManagedNetworkGroups

- [CreateOrUpdate](#managednetworkgroups_createorupdate)
- [Delete](#managednetworkgroups_delete)
- [Get](#managednetworkgroups_get)
- [ListByManagedNetwork](#managednetworkgroups_listbymanagednetwork)

## ManagedNetworkPeeringPolicies

- [CreateOrUpdate](#managednetworkpeeringpolicies_createorupdate)
- [Delete](#managednetworkpeeringpolicies_delete)
- [Get](#managednetworkpeeringpolicies_get)
- [ListByManagedNetwork](#managednetworkpeeringpolicies_listbymanagednetwork)

## ManagedNetworks

- [CreateOrUpdate](#managednetworks_createorupdate)
- [Delete](#managednetworks_delete)
- [GetByResourceGroup](#managednetworks_getbyresourcegroup)
- [List](#managednetworks_list)
- [ListByResourceGroup](#managednetworks_listbyresourcegroup)
- [Update](#managednetworks_update)

## ScopeAssignments

- [CreateOrUpdate](#scopeassignments_createorupdate)
- [Delete](#scopeassignments_delete)
- [Get](#scopeassignments_get)
- [List](#scopeassignments_list)
### ManagedNetworkGroups_CreateOrUpdate

```java
import com.azure.resourcemanager.managednetwork.models.ResourceId;
import java.util.Arrays;

/** Samples for ManagedNetworkGroups CreateOrUpdate. */
public final class ManagedNetworkGroupsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/managednetwork/resource-manager/Microsoft.ManagedNetwork/preview/2019-06-01-preview/examples/ManagedNetworkGroup/ManagedNetworkGroupsPut.json
     */
    /**
     * Sample code: ManagementNetworkGroupsPut.
     *
     * @param manager Entry point to ManagedNetworkManager.
     */
    public static void managementNetworkGroupsPut(
        com.azure.resourcemanager.managednetwork.ManagedNetworkManager manager) {
        manager
            .managedNetworkGroups()
            .define("myManagedNetworkGroup1")
            .withExistingManagedNetwork("myResourceGroup", "myManagedNetwork")
            .withManagementGroups(Arrays.asList())
            .withSubscriptions(Arrays.asList())
            .withVirtualNetworks(
                Arrays
                    .asList(
                        new ResourceId()
                            .withId(
                                "/subscriptionB/resourceGroups/myResourceGroup/providers/Microsoft.Network/virtualNetworks/VnetA"),
                        new ResourceId()
                            .withId(
                                "/subscriptionB/resourceGroups/myResourceGroup/providers/Microsoft.Network/virtualNetworks/VnetB")))
            .withSubnets(
                Arrays
                    .asList(
                        new ResourceId()
                            .withId(
                                "/subscriptionB/resourceGroups/myResourceGroup/providers/Microsoft.Network/virtualNetworks/VnetA/subnets/subnetA")))
            .create();
    }
}
```

### ManagedNetworkGroups_Delete

```java
import com.azure.core.util.Context;

/** Samples for ManagedNetworkGroups Delete. */
public final class ManagedNetworkGroupsDeleteSamples {
    /*
     * x-ms-original-file: specification/managednetwork/resource-manager/Microsoft.ManagedNetwork/preview/2019-06-01-preview/examples/ManagedNetworkGroup/ManagedNetworkGroupsDelete.json
     */
    /**
     * Sample code: ManagementNetworkGroupsDelete.
     *
     * @param manager Entry point to ManagedNetworkManager.
     */
    public static void managementNetworkGroupsDelete(
        com.azure.resourcemanager.managednetwork.ManagedNetworkManager manager) {
        manager
            .managedNetworkGroups()
            .delete("myResourceGroup", "myManagedNetwork", "myManagedNetworkGroup1", Context.NONE);
    }
}
```

### ManagedNetworkGroups_Get

```java
import com.azure.core.util.Context;

/** Samples for ManagedNetworkGroups Get. */
public final class ManagedNetworkGroupsGetSamples {
    /*
     * x-ms-original-file: specification/managednetwork/resource-manager/Microsoft.ManagedNetwork/preview/2019-06-01-preview/examples/ManagedNetworkGroup/ManagedNetworkGroupsGet.json
     */
    /**
     * Sample code: ManagementNetworkGroupsGet.
     *
     * @param manager Entry point to ManagedNetworkManager.
     */
    public static void managementNetworkGroupsGet(
        com.azure.resourcemanager.managednetwork.ManagedNetworkManager manager) {
        manager
            .managedNetworkGroups()
            .getWithResponse("myResourceGroup", "myManagedNetwork", "myManagedNetworkGroup1", Context.NONE);
    }
}
```

### ManagedNetworkGroups_ListByManagedNetwork

```java
import com.azure.core.util.Context;

/** Samples for ManagedNetworkGroups ListByManagedNetwork. */
public final class ManagedNetworkGroupsListByManagedNetworkSamples {
    /*
     * x-ms-original-file: specification/managednetwork/resource-manager/Microsoft.ManagedNetwork/preview/2019-06-01-preview/examples/ManagedNetworkGroup/ManagedNetworkGroupsListByManagedNetwork.json
     */
    /**
     * Sample code: ManagedNetworksGroupsListByManagedNetwork.
     *
     * @param manager Entry point to ManagedNetworkManager.
     */
    public static void managedNetworksGroupsListByManagedNetwork(
        com.azure.resourcemanager.managednetwork.ManagedNetworkManager manager) {
        manager
            .managedNetworkGroups()
            .listByManagedNetwork("myResourceGroup", "myManagedNetwork", null, null, Context.NONE);
    }
}
```

### ManagedNetworkPeeringPolicies_CreateOrUpdate

```java
import com.azure.resourcemanager.managednetwork.models.ManagedNetworkPeeringPolicyProperties;
import com.azure.resourcemanager.managednetwork.models.ResourceId;
import com.azure.resourcemanager.managednetwork.models.Type;
import java.util.Arrays;

/** Samples for ManagedNetworkPeeringPolicies CreateOrUpdate. */
public final class ManagedNetworkPeeringPoliciesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/managednetwork/resource-manager/Microsoft.ManagedNetwork/preview/2019-06-01-preview/examples/ManagedNetworkPeeringPolicy/ManagedNetworkPeeringPoliciesPut.json
     */
    /**
     * Sample code: ManagedNetworkPeeringPoliciesPut.
     *
     * @param manager Entry point to ManagedNetworkManager.
     */
    public static void managedNetworkPeeringPoliciesPut(
        com.azure.resourcemanager.managednetwork.ManagedNetworkManager manager) {
        manager
            .managedNetworkPeeringPolicies()
            .define("myHubAndSpoke")
            .withExistingManagedNetwork("myResourceGroup", "myManagedNetwork")
            .withProperties(
                new ManagedNetworkPeeringPolicyProperties()
                    .withType(Type.HUB_AND_SPOKE_TOPOLOGY)
                    .withHub(
                        new ResourceId()
                            .withId(
                                "/subscriptionB/resourceGroups/myResourceGroup/providers/Microsoft.Network/virtualNetworks/myHubVnet"))
                    .withSpokes(
                        Arrays
                            .asList(
                                new ResourceId()
                                    .withId(
                                        "/subscriptionB/resourceGroups/myResourceGroup/providers/Microsoft.ManagedNetwork/managedNetworks/myManagedNetwork/managedNetworkGroups/myManagedNetworkGroup1"))))
            .create();
    }
}
```

### ManagedNetworkPeeringPolicies_Delete

```java
import com.azure.core.util.Context;

/** Samples for ManagedNetworkPeeringPolicies Delete. */
public final class ManagedNetworkPeeringPoliciesDeleteSamples {
    /*
     * x-ms-original-file: specification/managednetwork/resource-manager/Microsoft.ManagedNetwork/preview/2019-06-01-preview/examples/ManagedNetworkPeeringPolicy/ManagedNetworkPeeringPoliciesDelete.json
     */
    /**
     * Sample code: ManagedNetworkPeeringPoliciesDelete.
     *
     * @param manager Entry point to ManagedNetworkManager.
     */
    public static void managedNetworkPeeringPoliciesDelete(
        com.azure.resourcemanager.managednetwork.ManagedNetworkManager manager) {
        manager
            .managedNetworkPeeringPolicies()
            .delete("myResourceGroup", "myManagedNetwork", "myHubAndSpoke", Context.NONE);
    }
}
```

### ManagedNetworkPeeringPolicies_Get

```java
import com.azure.core.util.Context;

/** Samples for ManagedNetworkPeeringPolicies Get. */
public final class ManagedNetworkPeeringPoliciesGetSamples {
    /*
     * x-ms-original-file: specification/managednetwork/resource-manager/Microsoft.ManagedNetwork/preview/2019-06-01-preview/examples/ManagedNetworkPeeringPolicy/ManagedNetworkPeeringPoliciesGet.json
     */
    /**
     * Sample code: ManagedNetworkPeeringPoliciesGet.
     *
     * @param manager Entry point to ManagedNetworkManager.
     */
    public static void managedNetworkPeeringPoliciesGet(
        com.azure.resourcemanager.managednetwork.ManagedNetworkManager manager) {
        manager
            .managedNetworkPeeringPolicies()
            .getWithResponse("myResourceGroup", "myManagedNetwork", "myHubAndSpoke", Context.NONE);
    }
}
```

### ManagedNetworkPeeringPolicies_ListByManagedNetwork

```java
import com.azure.core.util.Context;

/** Samples for ManagedNetworkPeeringPolicies ListByManagedNetwork. */
public final class ManagedNetworkPeeringPoliciesListByManagedNetworkSamples {
    /*
     * x-ms-original-file: specification/managednetwork/resource-manager/Microsoft.ManagedNetwork/preview/2019-06-01-preview/examples/ManagedNetworkPeeringPolicy/ManagedNetworkPeeringPoliciesListByManagedNetwork.json
     */
    /**
     * Sample code: ManagedNetworkPeeringPoliciesListByManagedNetwork.
     *
     * @param manager Entry point to ManagedNetworkManager.
     */
    public static void managedNetworkPeeringPoliciesListByManagedNetwork(
        com.azure.resourcemanager.managednetwork.ManagedNetworkManager manager) {
        manager
            .managedNetworkPeeringPolicies()
            .listByManagedNetwork("myResourceGroup", "myManagedNetwork", null, null, Context.NONE);
    }
}
```

### ManagedNetworks_CreateOrUpdate

```java
import com.azure.resourcemanager.managednetwork.models.ResourceId;
import com.azure.resourcemanager.managednetwork.models.Scope;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for ManagedNetworks CreateOrUpdate. */
public final class ManagedNetworksCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/managednetwork/resource-manager/Microsoft.ManagedNetwork/preview/2019-06-01-preview/examples/ManagedNetwork/ManagedNetworksPut.json
     */
    /**
     * Sample code: ManagedNetworksPut.
     *
     * @param manager Entry point to ManagedNetworkManager.
     */
    public static void managedNetworksPut(com.azure.resourcemanager.managednetwork.ManagedNetworkManager manager) {
        manager
            .managedNetworks()
            .define("myManagedNetwork")
            .withRegion("eastus")
            .withExistingResourceGroup("myResourceGroup")
            .withTags(mapOf())
            .withScope(
                new Scope()
                    .withManagementGroups(
                        Arrays
                            .asList(
                                new ResourceId()
                                    .withId(
                                        "/providers/Microsoft.Management/managementGroups/20000000-0001-0000-0000-000000000000"),
                                new ResourceId()
                                    .withId(
                                        "/providers/Microsoft.Management/managementGroups/20000000-0002-0000-0000-000000000000")))
                    .withSubscriptions(
                        Arrays
                            .asList(new ResourceId().withId("subscriptionA"), new ResourceId().withId("subscriptionB")))
                    .withVirtualNetworks(
                        Arrays
                            .asList(
                                new ResourceId()
                                    .withId(
                                        "/subscriptions/subscriptionC/resourceGroups/myResourceGroup/providers/Microsoft.Network/virtualNetworks/VnetA"),
                                new ResourceId()
                                    .withId(
                                        "/subscriptions/subscriptionC/resourceGroups/myResourceGroup/providers/Microsoft.Network/virtualNetworks/VnetB")))
                    .withSubnets(
                        Arrays
                            .asList(
                                new ResourceId()
                                    .withId(
                                        "/subscriptions/subscriptionC/resourceGroups/myResourceGroup/providers/Microsoft.Network/virtualNetworks/VnetC/subnets/subnetA"),
                                new ResourceId()
                                    .withId(
                                        "/subscriptions/subscriptionC/resourceGroups/myResourceGroup/providers/Microsoft.Network/virtualNetworks/VnetC/subnets/subnetB"))))
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

### ManagedNetworks_Delete

```java
import com.azure.core.util.Context;

/** Samples for ManagedNetworks Delete. */
public final class ManagedNetworksDeleteSamples {
    /*
     * x-ms-original-file: specification/managednetwork/resource-manager/Microsoft.ManagedNetwork/preview/2019-06-01-preview/examples/ManagedNetwork/ManagedNetworksDelete.json
     */
    /**
     * Sample code: ManagedNetworksDelete.
     *
     * @param manager Entry point to ManagedNetworkManager.
     */
    public static void managedNetworksDelete(com.azure.resourcemanager.managednetwork.ManagedNetworkManager manager) {
        manager.managedNetworks().delete("myResourceGroup", "myManagedNetwork", Context.NONE);
    }
}
```

### ManagedNetworks_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for ManagedNetworks GetByResourceGroup. */
public final class ManagedNetworksGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/managednetwork/resource-manager/Microsoft.ManagedNetwork/preview/2019-06-01-preview/examples/ManagedNetwork/ManagedNetworksGet.json
     */
    /**
     * Sample code: ManagedNetworksGet.
     *
     * @param manager Entry point to ManagedNetworkManager.
     */
    public static void managedNetworksGet(com.azure.resourcemanager.managednetwork.ManagedNetworkManager manager) {
        manager.managedNetworks().getByResourceGroupWithResponse("myResourceGroup", "myManagedNetwork", Context.NONE);
    }
}
```

### ManagedNetworks_List

```java
import com.azure.core.util.Context;

/** Samples for ManagedNetworks List. */
public final class ManagedNetworksListSamples {
    /*
     * x-ms-original-file: specification/managednetwork/resource-manager/Microsoft.ManagedNetwork/preview/2019-06-01-preview/examples/ManagedNetwork/ManagedNetworksListBySubscription.json
     */
    /**
     * Sample code: ManagedNetworksListBySubscription.
     *
     * @param manager Entry point to ManagedNetworkManager.
     */
    public static void managedNetworksListBySubscription(
        com.azure.resourcemanager.managednetwork.ManagedNetworkManager manager) {
        manager.managedNetworks().list(null, null, Context.NONE);
    }
}
```

### ManagedNetworks_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for ManagedNetworks ListByResourceGroup. */
public final class ManagedNetworksListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/managednetwork/resource-manager/Microsoft.ManagedNetwork/preview/2019-06-01-preview/examples/ManagedNetwork/ManagedNetworksListByResourceGroup.json
     */
    /**
     * Sample code: ManagedNetworksListByResourceGroup.
     *
     * @param manager Entry point to ManagedNetworkManager.
     */
    public static void managedNetworksListByResourceGroup(
        com.azure.resourcemanager.managednetwork.ManagedNetworkManager manager) {
        manager.managedNetworks().listByResourceGroup("myResourceGroup", null, null, Context.NONE);
    }
}
```

### ManagedNetworks_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.managednetwork.models.ManagedNetwork;
import java.util.HashMap;
import java.util.Map;

/** Samples for ManagedNetworks Update. */
public final class ManagedNetworksUpdateSamples {
    /*
     * x-ms-original-file: specification/managednetwork/resource-manager/Microsoft.ManagedNetwork/preview/2019-06-01-preview/examples/ManagedNetwork/ManagedNetworksPatch.json
     */
    /**
     * Sample code: ManagedNetworksPatch.
     *
     * @param manager Entry point to ManagedNetworkManager.
     */
    public static void managedNetworksPatch(com.azure.resourcemanager.managednetwork.ManagedNetworkManager manager) {
        ManagedNetwork resource =
            manager
                .managedNetworks()
                .getByResourceGroupWithResponse("myResourceGroup", "myManagedNetwork", Context.NONE)
                .getValue();
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

### ScopeAssignments_CreateOrUpdate

```java
/** Samples for ScopeAssignments CreateOrUpdate. */
public final class ScopeAssignmentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/managednetwork/resource-manager/Microsoft.ManagedNetwork/preview/2019-06-01-preview/examples/ScopeAssignment/ScopeAssignmentsPut.json
     */
    /**
     * Sample code: ScopeAssignmentsPut.
     *
     * @param manager Entry point to ManagedNetworkManager.
     */
    public static void scopeAssignmentsPut(com.azure.resourcemanager.managednetwork.ManagedNetworkManager manager) {
        manager
            .scopeAssignments()
            .define("subscriptionCAssignment")
            .withExistingScope("subscriptions/subscriptionC")
            .withAssignedManagedNetwork(
                "/subscriptions/subscriptionA/resourceGroups/myResourceGroup/providers/Microsoft.ManagedNetwork/managedNetworks/myManagedNetwork")
            .create();
    }
}
```

### ScopeAssignments_Delete

```java
import com.azure.core.util.Context;

/** Samples for ScopeAssignments Delete. */
public final class ScopeAssignmentsDeleteSamples {
    /*
     * x-ms-original-file: specification/managednetwork/resource-manager/Microsoft.ManagedNetwork/preview/2019-06-01-preview/examples/ScopeAssignment/ScopeAssignmentsDelete.json
     */
    /**
     * Sample code: ScopeAssignmentsDelete.
     *
     * @param manager Entry point to ManagedNetworkManager.
     */
    public static void scopeAssignmentsDelete(com.azure.resourcemanager.managednetwork.ManagedNetworkManager manager) {
        manager
            .scopeAssignments()
            .deleteWithResponse("subscriptions/subscriptionC", "subscriptionCAssignment", Context.NONE);
    }
}
```

### ScopeAssignments_Get

```java
import com.azure.core.util.Context;

/** Samples for ScopeAssignments Get. */
public final class ScopeAssignmentsGetSamples {
    /*
     * x-ms-original-file: specification/managednetwork/resource-manager/Microsoft.ManagedNetwork/preview/2019-06-01-preview/examples/ScopeAssignment/ScopeAssignmentsGet.json
     */
    /**
     * Sample code: ScopeAssignmentsGet.
     *
     * @param manager Entry point to ManagedNetworkManager.
     */
    public static void scopeAssignmentsGet(com.azure.resourcemanager.managednetwork.ManagedNetworkManager manager) {
        manager
            .scopeAssignments()
            .getWithResponse("subscriptions/subscriptionC", "subscriptionCAssignment", Context.NONE);
    }
}
```

### ScopeAssignments_List

```java
import com.azure.core.util.Context;

/** Samples for ScopeAssignments List. */
public final class ScopeAssignmentsListSamples {
    /*
     * x-ms-original-file: specification/managednetwork/resource-manager/Microsoft.ManagedNetwork/preview/2019-06-01-preview/examples/ScopeAssignment/ScopeAssignmentsList.json
     */
    /**
     * Sample code: ScopeAssignmentsList.
     *
     * @param manager Entry point to ManagedNetworkManager.
     */
    public static void scopeAssignmentsList(com.azure.resourcemanager.managednetwork.ManagedNetworkManager manager) {
        manager.scopeAssignments().list("subscriptions/subscriptionC", Context.NONE);
    }
}
```

