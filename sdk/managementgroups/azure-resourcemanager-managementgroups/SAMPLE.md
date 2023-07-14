# Code snippets and samples


## Entities

- [List](#entities_list)

## HierarchySettingsOperation

- [CreateOrUpdate](#hierarchysettingsoperation_createorupdate)
- [Delete](#hierarchysettingsoperation_delete)
- [Get](#hierarchysettingsoperation_get)
- [List](#hierarchysettingsoperation_list)
- [Update](#hierarchysettingsoperation_update)

## ManagementGroupSubscriptions

- [Create](#managementgroupsubscriptions_create)
- [Delete](#managementgroupsubscriptions_delete)
- [GetSubscription](#managementgroupsubscriptions_getsubscription)
- [GetSubscriptionsUnderManagementGroup](#managementgroupsubscriptions_getsubscriptionsundermanagementgroup)

## ManagementGroups

- [CreateOrUpdate](#managementgroups_createorupdate)
- [Delete](#managementgroups_delete)
- [Get](#managementgroups_get)
- [GetDescendants](#managementgroups_getdescendants)
- [List](#managementgroups_list)
- [Update](#managementgroups_update)

## Operations

- [List](#operations_list)

## ResourceProvider

- [CheckNameAvailability](#resourceprovider_checknameavailability)
- [StartTenantBackfill](#resourceprovider_starttenantbackfill)
- [TenantBackfillStatus](#resourceprovider_tenantbackfillstatus)
### Entities_List

```java
/** Samples for Entities List. */
public final class EntitiesListSamples {
    /*
     * x-ms-original-file: specification/managementgroups/resource-manager/Microsoft.Management/stable/2021-04-01/examples/GetEntities.json
     */
    /**
     * Sample code: GetEntities.
     *
     * @param manager Entry point to ManagementGroupsManager.
     */
    public static void getEntities(com.azure.resourcemanager.managementgroups.ManagementGroupsManager manager) {
        manager.entities().list(null, null, null, null, null, null, null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### HierarchySettingsOperation_CreateOrUpdate

```java
import com.azure.resourcemanager.managementgroups.models.CreateOrUpdateSettingsRequest;

/** Samples for HierarchySettingsOperation CreateOrUpdate. */
public final class HierarchySettingsOperationCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/managementgroups/resource-manager/Microsoft.Management/stable/2021-04-01/examples/PutHierarchySettings.json
     */
    /**
     * Sample code: GetGroupSettings.
     *
     * @param manager Entry point to ManagementGroupsManager.
     */
    public static void getGroupSettings(com.azure.resourcemanager.managementgroups.ManagementGroupsManager manager) {
        manager
            .hierarchySettingsOperations()
            .createOrUpdateWithResponse(
                "root",
                new CreateOrUpdateSettingsRequest()
                    .withRequireAuthorizationForGroupCreation(true)
                    .withDefaultManagementGroup("/providers/Microsoft.Management/managementGroups/DefaultGroup"),
                com.azure.core.util.Context.NONE);
    }
}
```

### HierarchySettingsOperation_Delete

```java
/** Samples for HierarchySettingsOperation Delete. */
public final class HierarchySettingsOperationDeleteSamples {
    /*
     * x-ms-original-file: specification/managementgroups/resource-manager/Microsoft.Management/stable/2021-04-01/examples/DeleteHierarchySettings.json
     */
    /**
     * Sample code: GetGroupSettings.
     *
     * @param manager Entry point to ManagementGroupsManager.
     */
    public static void getGroupSettings(com.azure.resourcemanager.managementgroups.ManagementGroupsManager manager) {
        manager.hierarchySettingsOperations().deleteWithResponse("root", com.azure.core.util.Context.NONE);
    }
}
```

### HierarchySettingsOperation_Get

```java
/** Samples for HierarchySettingsOperation Get. */
public final class HierarchySettingsOperationGetSamples {
    /*
     * x-ms-original-file: specification/managementgroups/resource-manager/Microsoft.Management/stable/2021-04-01/examples/GetHierarchySettings.json
     */
    /**
     * Sample code: GetGroupSettings.
     *
     * @param manager Entry point to ManagementGroupsManager.
     */
    public static void getGroupSettings(com.azure.resourcemanager.managementgroups.ManagementGroupsManager manager) {
        manager.hierarchySettingsOperations().getWithResponse("root", com.azure.core.util.Context.NONE);
    }
}
```

### HierarchySettingsOperation_List

```java
/** Samples for HierarchySettingsOperation List. */
public final class HierarchySettingsOperationListSamples {
    /*
     * x-ms-original-file: specification/managementgroups/resource-manager/Microsoft.Management/stable/2021-04-01/examples/ListHierarchySettings.json
     */
    /**
     * Sample code: ListGroupSettings.
     *
     * @param manager Entry point to ManagementGroupsManager.
     */
    public static void listGroupSettings(com.azure.resourcemanager.managementgroups.ManagementGroupsManager manager) {
        manager.hierarchySettingsOperations().listWithResponse("root", com.azure.core.util.Context.NONE);
    }
}
```

### HierarchySettingsOperation_Update

```java
import com.azure.resourcemanager.managementgroups.models.CreateOrUpdateSettingsRequest;

/** Samples for HierarchySettingsOperation Update. */
public final class HierarchySettingsOperationUpdateSamples {
    /*
     * x-ms-original-file: specification/managementgroups/resource-manager/Microsoft.Management/stable/2021-04-01/examples/PatchHierarchySettings.json
     */
    /**
     * Sample code: GetGroupSettings.
     *
     * @param manager Entry point to ManagementGroupsManager.
     */
    public static void getGroupSettings(com.azure.resourcemanager.managementgroups.ManagementGroupsManager manager) {
        manager
            .hierarchySettingsOperations()
            .updateWithResponse(
                "root",
                new CreateOrUpdateSettingsRequest()
                    .withRequireAuthorizationForGroupCreation(true)
                    .withDefaultManagementGroup("/providers/Microsoft.Management/managementGroups/DefaultGroup"),
                com.azure.core.util.Context.NONE);
    }
}
```

### ManagementGroupSubscriptions_Create

```java
/** Samples for ManagementGroupSubscriptions Create. */
public final class ManagementGroupSubscriptionsCreateSamples {
    /*
     * x-ms-original-file: specification/managementgroups/resource-manager/Microsoft.Management/stable/2021-04-01/examples/AddManagementGroupSubscription.json
     */
    /**
     * Sample code: AddSubscriptionToManagementGroup.
     *
     * @param manager Entry point to ManagementGroupsManager.
     */
    public static void addSubscriptionToManagementGroup(
        com.azure.resourcemanager.managementgroups.ManagementGroupsManager manager) {
        manager
            .managementGroupSubscriptions()
            .createWithResponse(
                "Group", "728bcbe4-8d56-4510-86c2-4921b8beefbc", "no-cache", com.azure.core.util.Context.NONE);
    }
}
```

### ManagementGroupSubscriptions_Delete

```java
/** Samples for ManagementGroupSubscriptions Delete. */
public final class ManagementGroupSubscriptionsDeleteSamples {
    /*
     * x-ms-original-file: specification/managementgroups/resource-manager/Microsoft.Management/stable/2021-04-01/examples/RemoveManagementGroupSubscription.json
     */
    /**
     * Sample code: DeleteSubscriptionFromManagementGroup.
     *
     * @param manager Entry point to ManagementGroupsManager.
     */
    public static void deleteSubscriptionFromManagementGroup(
        com.azure.resourcemanager.managementgroups.ManagementGroupsManager manager) {
        manager
            .managementGroupSubscriptions()
            .deleteWithResponse(
                "Group", "728bcbe4-8d56-4510-86c2-4921b8beefbc", "no-cache", com.azure.core.util.Context.NONE);
    }
}
```

### ManagementGroupSubscriptions_GetSubscription

```java
/** Samples for ManagementGroupSubscriptions GetSubscription. */
public final class ManagementGroupSubscriptionsGetSubscriptionSamples {
    /*
     * x-ms-original-file: specification/managementgroups/resource-manager/Microsoft.Management/stable/2021-04-01/examples/GetSubscriptionFromManagementGroup.json
     */
    /**
     * Sample code: GetSubscriptionFromManagementGroup.
     *
     * @param manager Entry point to ManagementGroupsManager.
     */
    public static void getSubscriptionFromManagementGroup(
        com.azure.resourcemanager.managementgroups.ManagementGroupsManager manager) {
        manager
            .managementGroupSubscriptions()
            .getSubscriptionWithResponse(
                "Group", "728bcbe4-8d56-4510-86c2-4921b8beefbc", "no-cache", com.azure.core.util.Context.NONE);
    }
}
```

### ManagementGroupSubscriptions_GetSubscriptionsUnderManagementGroup

```java
/** Samples for ManagementGroupSubscriptions GetSubscriptionsUnderManagementGroup. */
public final class ManagementGroupSubscriptionsGetSubscriptionsUnderManagementGroupSamples {
    /*
     * x-ms-original-file: specification/managementgroups/resource-manager/Microsoft.Management/stable/2021-04-01/examples/GetAllSubscriptionsFromManagementGroup.json
     */
    /**
     * Sample code: GetAllSubscriptionsFromManagementGroup.
     *
     * @param manager Entry point to ManagementGroupsManager.
     */
    public static void getAllSubscriptionsFromManagementGroup(
        com.azure.resourcemanager.managementgroups.ManagementGroupsManager manager) {
        manager
            .managementGroupSubscriptions()
            .getSubscriptionsUnderManagementGroup("Group", null, com.azure.core.util.Context.NONE);
    }
}
```

### ManagementGroups_CreateOrUpdate

```java
import com.azure.resourcemanager.managementgroups.models.CreateManagementGroupDetails;
import com.azure.resourcemanager.managementgroups.models.CreateManagementGroupRequest;
import com.azure.resourcemanager.managementgroups.models.CreateParentGroupInfo;

/** Samples for ManagementGroups CreateOrUpdate. */
public final class ManagementGroupsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/managementgroups/resource-manager/Microsoft.Management/stable/2021-04-01/examples/PutManagementGroup.json
     */
    /**
     * Sample code: PutManagementGroup.
     *
     * @param manager Entry point to ManagementGroupsManager.
     */
    public static void putManagementGroup(com.azure.resourcemanager.managementgroups.ManagementGroupsManager manager) {
        manager
            .managementGroups()
            .createOrUpdate(
                "ChildGroup",
                new CreateManagementGroupRequest()
                    .withDisplayName("ChildGroup")
                    .withDetails(
                        new CreateManagementGroupDetails()
                            .withParent(
                                new CreateParentGroupInfo()
                                    .withId("/providers/Microsoft.Management/managementGroups/RootGroup"))),
                "no-cache",
                com.azure.core.util.Context.NONE);
    }
}
```

### ManagementGroups_Delete

```java
/** Samples for ManagementGroups Delete. */
public final class ManagementGroupsDeleteSamples {
    /*
     * x-ms-original-file: specification/managementgroups/resource-manager/Microsoft.Management/stable/2021-04-01/examples/DeleteManagementGroup.json
     */
    /**
     * Sample code: DeleteManagementGroup.
     *
     * @param manager Entry point to ManagementGroupsManager.
     */
    public static void deleteManagementGroup(
        com.azure.resourcemanager.managementgroups.ManagementGroupsManager manager) {
        manager.managementGroups().delete("GroupToDelete", "no-cache", com.azure.core.util.Context.NONE);
    }
}
```

### ManagementGroups_Get

```java
import com.azure.resourcemanager.managementgroups.models.ManagementGroupExpandType;

/** Samples for ManagementGroups Get. */
public final class ManagementGroupsGetSamples {
    /*
     * x-ms-original-file: specification/managementgroups/resource-manager/Microsoft.Management/stable/2021-04-01/examples/GetManagementGroupWithPath.json
     */
    /**
     * Sample code: GetManagementGroupWithPath.
     *
     * @param manager Entry point to ManagementGroupsManager.
     */
    public static void getManagementGroupWithPath(
        com.azure.resourcemanager.managementgroups.ManagementGroupsManager manager) {
        manager
            .managementGroups()
            .getWithResponse(
                "20000000-0001-0000-0000-000000000000",
                ManagementGroupExpandType.PATH,
                null,
                null,
                "no-cache",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/managementgroups/resource-manager/Microsoft.Management/stable/2021-04-01/examples/GetManagementGroupWithAncestors.json
     */
    /**
     * Sample code: GetManagementGroupWithAncestors.
     *
     * @param manager Entry point to ManagementGroupsManager.
     */
    public static void getManagementGroupWithAncestors(
        com.azure.resourcemanager.managementgroups.ManagementGroupsManager manager) {
        manager
            .managementGroups()
            .getWithResponse(
                "20000000-0001-0000-0000-00000000000",
                ManagementGroupExpandType.ANCESTORS,
                null,
                null,
                "no-cache",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/managementgroups/resource-manager/Microsoft.Management/stable/2021-04-01/examples/GetManagementGroupWithExpand.json
     */
    /**
     * Sample code: GetManagementGroupWithExpand.
     *
     * @param manager Entry point to ManagementGroupsManager.
     */
    public static void getManagementGroupWithExpand(
        com.azure.resourcemanager.managementgroups.ManagementGroupsManager manager) {
        manager
            .managementGroups()
            .getWithResponse(
                "20000000-0001-0000-0000-000000000000",
                ManagementGroupExpandType.CHILDREN,
                null,
                null,
                "no-cache",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/managementgroups/resource-manager/Microsoft.Management/stable/2021-04-01/examples/GetManagementGroup.json
     */
    /**
     * Sample code: GetManagementGroup.
     *
     * @param manager Entry point to ManagementGroupsManager.
     */
    public static void getManagementGroup(com.azure.resourcemanager.managementgroups.ManagementGroupsManager manager) {
        manager
            .managementGroups()
            .getWithResponse(
                "20000000-0001-0000-0000-000000000000", null, null, null, "no-cache", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/managementgroups/resource-manager/Microsoft.Management/stable/2021-04-01/examples/GetManagementGroupWithExpandAndRecurse.json
     */
    /**
     * Sample code: GetManagementGroupsWithExpandAndRecurse.
     *
     * @param manager Entry point to ManagementGroupsManager.
     */
    public static void getManagementGroupsWithExpandAndRecurse(
        com.azure.resourcemanager.managementgroups.ManagementGroupsManager manager) {
        manager
            .managementGroups()
            .getWithResponse(
                "20000000-0001-0000-0000-000000000000",
                ManagementGroupExpandType.CHILDREN,
                true,
                null,
                "no-cache",
                com.azure.core.util.Context.NONE);
    }
}
```

### ManagementGroups_GetDescendants

```java
/** Samples for ManagementGroups GetDescendants. */
public final class ManagementGroupsGetDescendantsSamples {
    /*
     * x-ms-original-file: specification/managementgroups/resource-manager/Microsoft.Management/stable/2021-04-01/examples/GetDescendants.json
     */
    /**
     * Sample code: GetDescendants.
     *
     * @param manager Entry point to ManagementGroupsManager.
     */
    public static void getDescendants(com.azure.resourcemanager.managementgroups.ManagementGroupsManager manager) {
        manager
            .managementGroups()
            .getDescendants("20000000-0000-0000-0000-000000000000", null, null, com.azure.core.util.Context.NONE);
    }
}
```

### ManagementGroups_List

```java
/** Samples for ManagementGroups List. */
public final class ManagementGroupsListSamples {
    /*
     * x-ms-original-file: specification/managementgroups/resource-manager/Microsoft.Management/stable/2021-04-01/examples/ListManagementGroups.json
     */
    /**
     * Sample code: ListManagementGroups.
     *
     * @param manager Entry point to ManagementGroupsManager.
     */
    public static void listManagementGroups(
        com.azure.resourcemanager.managementgroups.ManagementGroupsManager manager) {
        manager.managementGroups().list("no-cache", null, com.azure.core.util.Context.NONE);
    }
}
```

### ManagementGroups_Update

```java
import com.azure.resourcemanager.managementgroups.models.PatchManagementGroupRequest;

/** Samples for ManagementGroups Update. */
public final class ManagementGroupsUpdateSamples {
    /*
     * x-ms-original-file: specification/managementgroups/resource-manager/Microsoft.Management/stable/2021-04-01/examples/PatchManagementGroup.json
     */
    /**
     * Sample code: PatchManagementGroup.
     *
     * @param manager Entry point to ManagementGroupsManager.
     */
    public static void patchManagementGroup(
        com.azure.resourcemanager.managementgroups.ManagementGroupsManager manager) {
        manager
            .managementGroups()
            .updateWithResponse(
                "ChildGroup",
                new PatchManagementGroupRequest()
                    .withDisplayName("AlternateDisplayName")
                    .withParentGroupId("/providers/Microsoft.Management/managementGroups/AlternateRootGroup"),
                "no-cache",
                com.azure.core.util.Context.NONE);
    }
}
```

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/managementgroups/resource-manager/Microsoft.Management/stable/2021-04-01/examples/ListOperations.json
     */
    /**
     * Sample code: List Operations.
     *
     * @param manager Entry point to ManagementGroupsManager.
     */
    public static void listOperations(com.azure.resourcemanager.managementgroups.ManagementGroupsManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_CheckNameAvailability

```java
import com.azure.resourcemanager.managementgroups.models.CheckNameAvailabilityRequest;
import com.azure.resourcemanager.managementgroups.models.Type;

/** Samples for ResourceProvider CheckNameAvailability. */
public final class ResourceProviderCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/managementgroups/resource-manager/Microsoft.Management/stable/2021-04-01/examples/CheckManagementGroupNameAvailability.json
     */
    /**
     * Sample code: CheckManagementGroupNameAvailability.
     *
     * @param manager Entry point to ManagementGroupsManager.
     */
    public static void checkManagementGroupNameAvailability(
        com.azure.resourcemanager.managementgroups.ManagementGroupsManager manager) {
        manager
            .resourceProviders()
            .checkNameAvailabilityWithResponse(
                new CheckNameAvailabilityRequest()
                    .withName("nameTocheck")
                    .withType(Type.MICROSOFT_MANAGEMENT_MANAGEMENT_GROUPS),
                com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_StartTenantBackfill

```java
/** Samples for ResourceProvider StartTenantBackfill. */
public final class ResourceProviderStartTenantBackfillSamples {
    /*
     * x-ms-original-file: specification/managementgroups/resource-manager/Microsoft.Management/stable/2021-04-01/examples/StartTenantBackfillRequest.json
     */
    /**
     * Sample code: StartTenantBackfill.
     *
     * @param manager Entry point to ManagementGroupsManager.
     */
    public static void startTenantBackfill(com.azure.resourcemanager.managementgroups.ManagementGroupsManager manager) {
        manager.resourceProviders().startTenantBackfillWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_TenantBackfillStatus

```java
/** Samples for ResourceProvider TenantBackfillStatus. */
public final class ResourceProviderTenantBackfillStatusSamples {
    /*
     * x-ms-original-file: specification/managementgroups/resource-manager/Microsoft.Management/stable/2021-04-01/examples/TenantBackfillStatusRequest.json
     */
    /**
     * Sample code: TenantBackfillStatus.
     *
     * @param manager Entry point to ManagementGroupsManager.
     */
    public static void tenantBackfillStatus(
        com.azure.resourcemanager.managementgroups.ManagementGroupsManager manager) {
        manager.resourceProviders().tenantBackfillStatusWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

