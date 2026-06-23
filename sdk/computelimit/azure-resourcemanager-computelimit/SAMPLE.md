# Code snippets and samples


## Features

- [Disable](#features_disable)
- [Enable](#features_enable)
- [Get](#features_get)
- [ListBySubscriptionLocationResource](#features_listbysubscriptionlocationresource)

## GuestSubscriptions

- [Create](#guestsubscriptions_create)
- [Delete](#guestsubscriptions_delete)
- [Get](#guestsubscriptions_get)
- [ListBySubscriptionLocationResource](#guestsubscriptions_listbysubscriptionlocationresource)

## MemberCapOverrides

- [CreateOrUpdate](#membercapoverrides_createorupdate)
- [Delete](#membercapoverrides_delete)
- [Get](#membercapoverrides_get)
- [ListByParent](#membercapoverrides_listbyparent)

## Operations

- [List](#operations_list)

## SharedLimitCaps

- [CreateOrUpdate](#sharedlimitcaps_createorupdate)
- [Delete](#sharedlimitcaps_delete)
- [Get](#sharedlimitcaps_get)
- [ListBySubscriptionLocationResource](#sharedlimitcaps_listbysubscriptionlocationresource)
- [SetMemberCapOverrides](#sharedlimitcaps_setmembercapoverrides)

## SharedLimits

- [Create](#sharedlimits_create)
- [Delete](#sharedlimits_delete)
- [Get](#sharedlimits_get)
- [ListBySubscriptionLocationResource](#sharedlimits_listbysubscriptionlocationresource)

## VmFamilies

- [Get](#vmfamilies_get)
- [ListBySubscriptionLocationResource](#vmfamilies_listbysubscriptionlocationresource)
### Features_Disable

```java
/**
 * Samples for Features Disable.
 */
public final class FeaturesDisableSamples {
    /*
     * x-ms-original-file: 2026-07-01/Features_Disable.json
     */
    /**
     * Sample code: Disable feature.
     * 
     * @param manager Entry point to ComputeLimitManager.
     */
    public static void disableFeature(com.azure.resourcemanager.computelimit.ComputeLimitManager manager) {
        manager.features().disable("eastus", "VmCategoryQuota", com.azure.core.util.Context.NONE);
    }
}
```

### Features_Enable

```java
import com.azure.resourcemanager.computelimit.models.FeatureEnableRequest;

/**
 * Samples for Features Enable.
 */
public final class FeaturesEnableSamples {
    /*
     * x-ms-original-file: 2026-07-01/Features_Enable.json
     */
    /**
     * Sample code: Enable feature.
     * 
     * @param manager Entry point to ComputeLimitManager.
     */
    public static void enableFeature(com.azure.resourcemanager.computelimit.ComputeLimitManager manager) {
        manager.features()
            .enable("eastus", "VmCategoryQuota",
                new FeatureEnableRequest().withServiceTreeId("a1b2c3d4-5678-90ab-cdef-1234567890ab"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Features_Get

```java
/**
 * Samples for Features Get.
 */
public final class FeaturesGetSamples {
    /*
     * x-ms-original-file: 2026-07-01/Features_Get_SharedLimit.json
     */
    /**
     * Sample code: Get SharedLimit feature.
     * 
     * @param manager Entry point to ComputeLimitManager.
     */
    public static void getSharedLimitFeature(com.azure.resourcemanager.computelimit.ComputeLimitManager manager) {
        manager.features().getWithResponse("eastus", "SharedLimit", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-07-01/Features_Get.json
     */
    /**
     * Sample code: Get feature.
     * 
     * @param manager Entry point to ComputeLimitManager.
     */
    public static void getFeature(com.azure.resourcemanager.computelimit.ComputeLimitManager manager) {
        manager.features().getWithResponse("eastus", "VmCategoryQuota", com.azure.core.util.Context.NONE);
    }
}
```

### Features_ListBySubscriptionLocationResource

```java
/**
 * Samples for Features ListBySubscriptionLocationResource.
 */
public final class FeaturesListBySubscriptionLocationResourceSamples {
    /*
     * x-ms-original-file: 2026-07-01/Features_List.json
     */
    /**
     * Sample code: List features.
     * 
     * @param manager Entry point to ComputeLimitManager.
     */
    public static void listFeatures(com.azure.resourcemanager.computelimit.ComputeLimitManager manager) {
        manager.features().listBySubscriptionLocationResource("eastus", com.azure.core.util.Context.NONE);
    }
}
```

### GuestSubscriptions_Create

```java
import com.azure.resourcemanager.computelimit.models.GuestSubscriptionProperties;

/**
 * Samples for GuestSubscriptions Create.
 */
public final class GuestSubscriptionsCreateSamples {
    /*
     * x-ms-original-file: 2026-07-01/GuestSubscriptions_Create.json
     */
    /**
     * Sample code: Create a guest subscription.
     * 
     * @param manager Entry point to ComputeLimitManager.
     */
    public static void createAGuestSubscription(com.azure.resourcemanager.computelimit.ComputeLimitManager manager) {
        manager.guestSubscriptions()
            .define("11111111-1111-1111-1111-111111111111")
            .withExistingLocation("eastus")
            .withProperties(new GuestSubscriptionProperties())
            .create();
    }
}
```

### GuestSubscriptions_Delete

```java
/**
 * Samples for GuestSubscriptions Delete.
 */
public final class GuestSubscriptionsDeleteSamples {
    /*
     * x-ms-original-file: 2026-07-01/GuestSubscriptions_Delete.json
     */
    /**
     * Sample code: Delete a guest subscription.
     * 
     * @param manager Entry point to ComputeLimitManager.
     */
    public static void deleteAGuestSubscription(com.azure.resourcemanager.computelimit.ComputeLimitManager manager) {
        manager.guestSubscriptions()
            .deleteByResourceGroupWithResponse("eastus", "11111111-1111-1111-1111-111111111111",
                com.azure.core.util.Context.NONE);
    }
}
```

### GuestSubscriptions_Get

```java
/**
 * Samples for GuestSubscriptions Get.
 */
public final class GuestSubscriptionsGetSamples {
    /*
     * x-ms-original-file: 2026-07-01/GuestSubscriptions_Get.json
     */
    /**
     * Sample code: Get a guest subscription.
     * 
     * @param manager Entry point to ComputeLimitManager.
     */
    public static void getAGuestSubscription(com.azure.resourcemanager.computelimit.ComputeLimitManager manager) {
        manager.guestSubscriptions()
            .getWithResponse("eastus", "11111111-1111-1111-1111-111111111111", com.azure.core.util.Context.NONE);
    }
}
```

### GuestSubscriptions_ListBySubscriptionLocationResource

```java
/**
 * Samples for GuestSubscriptions ListBySubscriptionLocationResource.
 */
public final class GuestSubscriptionsListBySubscriptionLocationResourceSamples {
    /*
     * x-ms-original-file: 2026-07-01/GuestSubscriptions_List.json
     */
    /**
     * Sample code: List guest subscriptions for a scope.
     * 
     * @param manager Entry point to ComputeLimitManager.
     */
    public static void
        listGuestSubscriptionsForAScope(com.azure.resourcemanager.computelimit.ComputeLimitManager manager) {
        manager.guestSubscriptions().listBySubscriptionLocationResource("eastus", com.azure.core.util.Context.NONE);
    }
}
```

### MemberCapOverrides_CreateOrUpdate

```java
import com.azure.resourcemanager.computelimit.models.MemberCapOverrideProperties;

/**
 * Samples for MemberCapOverrides CreateOrUpdate.
 */
public final class MemberCapOverridesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-07-01/MemberCapOverrides_CreateOrUpdate.json
     */
    /**
     * Sample code: Create or update a single member cap override.
     * 
     * @param manager Entry point to ComputeLimitManager.
     */
    public static void
        createOrUpdateASingleMemberCapOverride(com.azure.resourcemanager.computelimit.ComputeLimitManager manager) {
        manager.memberCapOverrides()
            .define("11111111-1111-1111-1111-111111111111")
            .withExistingSharedLimitCap("eastus", "StandardDSv3Family")
            .withProperties(new MemberCapOverrideProperties().withCap(250))
            .create();
    }
}
```

### MemberCapOverrides_Delete

```java
/**
 * Samples for MemberCapOverrides Delete.
 */
public final class MemberCapOverridesDeleteSamples {
    /*
     * x-ms-original-file: 2026-07-01/MemberCapOverrides_Delete.json
     */
    /**
     * Sample code: Delete a single member cap override.
     * 
     * @param manager Entry point to ComputeLimitManager.
     */
    public static void
        deleteASingleMemberCapOverride(com.azure.resourcemanager.computelimit.ComputeLimitManager manager) {
        manager.memberCapOverrides()
            .deleteWithResponse("eastus", "StandardDSv3Family", "11111111-1111-1111-1111-111111111111",
                com.azure.core.util.Context.NONE);
    }
}
```

### MemberCapOverrides_Get

```java
/**
 * Samples for MemberCapOverrides Get.
 */
public final class MemberCapOverridesGetSamples {
    /*
     * x-ms-original-file: 2026-07-01/MemberCapOverrides_Get.json
     */
    /**
     * Sample code: Get a single member cap override.
     * 
     * @param manager Entry point to ComputeLimitManager.
     */
    public static void getASingleMemberCapOverride(com.azure.resourcemanager.computelimit.ComputeLimitManager manager) {
        manager.memberCapOverrides()
            .getWithResponse("eastus", "StandardDSv3Family", "11111111-1111-1111-1111-111111111111",
                com.azure.core.util.Context.NONE);
    }
}
```

### MemberCapOverrides_ListByParent

```java
/**
 * Samples for MemberCapOverrides ListByParent.
 */
public final class MemberCapOverridesListByParentSamples {
    /*
     * x-ms-original-file: 2026-07-01/MemberCapOverrides_ListByParent.json
     */
    /**
     * Sample code: List all member cap overrides under a shared limit cap.
     * 
     * @param manager Entry point to ComputeLimitManager.
     */
    public static void listAllMemberCapOverridesUnderASharedLimitCap(
        com.azure.resourcemanager.computelimit.ComputeLimitManager manager) {
        manager.memberCapOverrides().listByParent("eastus", "StandardDSv3Family", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-07-01/Operations_List.json
     */
    /**
     * Sample code: List operations.
     * 
     * @param manager Entry point to ComputeLimitManager.
     */
    public static void listOperations(com.azure.resourcemanager.computelimit.ComputeLimitManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### SharedLimitCaps_CreateOrUpdate

```java
import com.azure.resourcemanager.computelimit.models.SharedLimitCapProperties;

/**
 * Samples for SharedLimitCaps CreateOrUpdate.
 */
public final class SharedLimitCapsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-07-01/SharedLimitCaps_CreateOrUpdate.json
     */
    /**
     * Sample code: Create or update a shared limit cap for a VM family.
     * 
     * @param manager Entry point to ComputeLimitManager.
     */
    public static void
        createOrUpdateASharedLimitCapForAVMFamily(com.azure.resourcemanager.computelimit.ComputeLimitManager manager) {
        manager.sharedLimitCaps()
            .define("StandardDSv3Family")
            .withExistingLocation("eastus")
            .withProperties(new SharedLimitCapProperties().withDefaultMemberCap(100).withIsBoundedCap(true))
            .create();
    }
}
```

### SharedLimitCaps_Delete

```java
/**
 * Samples for SharedLimitCaps Delete.
 */
public final class SharedLimitCapsDeleteSamples {
    /*
     * x-ms-original-file: 2026-07-01/SharedLimitCaps_Delete.json
     */
    /**
     * Sample code: Delete the shared limit cap for a VM family.
     * 
     * @param manager Entry point to ComputeLimitManager.
     */
    public static void
        deleteTheSharedLimitCapForAVMFamily(com.azure.resourcemanager.computelimit.ComputeLimitManager manager) {
        manager.sharedLimitCaps()
            .deleteByResourceGroupWithResponse("eastus", "StandardDSv3Family", com.azure.core.util.Context.NONE);
    }
}
```

### SharedLimitCaps_Get

```java
/**
 * Samples for SharedLimitCaps Get.
 */
public final class SharedLimitCapsGetSamples {
    /*
     * x-ms-original-file: 2026-07-01/SharedLimitCaps_Get.json
     */
    /**
     * Sample code: Get a shared limit cap for a VM family.
     * 
     * @param manager Entry point to ComputeLimitManager.
     */
    public static void
        getASharedLimitCapForAVMFamily(com.azure.resourcemanager.computelimit.ComputeLimitManager manager) {
        manager.sharedLimitCaps().getWithResponse("eastus", "StandardDSv3Family", com.azure.core.util.Context.NONE);
    }
}
```

### SharedLimitCaps_ListBySubscriptionLocationResource

```java
/**
 * Samples for SharedLimitCaps ListBySubscriptionLocationResource.
 */
public final class SharedLimitCapsListBySubscriptionLocationResourceSamples {
    /*
     * x-ms-original-file: 2026-07-01/SharedLimitCaps_List.json
     */
    /**
     * Sample code: List shared limit caps in a region for the caller's subscription.
     * 
     * @param manager Entry point to ComputeLimitManager.
     */
    public static void listSharedLimitCapsInARegionForTheCallerSSubscription(
        com.azure.resourcemanager.computelimit.ComputeLimitManager manager) {
        manager.sharedLimitCaps().listBySubscriptionLocationResource("eastus", com.azure.core.util.Context.NONE);
    }
}
```

### SharedLimitCaps_SetMemberCapOverrides

```java
import com.azure.resourcemanager.computelimit.models.MemberCap;
import com.azure.resourcemanager.computelimit.models.SetMemberCapOverridesRequest;
import java.util.Arrays;

/**
 * Samples for SharedLimitCaps SetMemberCapOverrides.
 */
public final class SharedLimitCapsSetMemberCapOverridesSamples {
    /*
     * x-ms-original-file: 2026-07-01/SharedLimitCaps_SetMemberCapOverrides.json
     */
    /**
     * Sample code: Replace the full set of member cap overrides for a shared limit cap.
     * 
     * @param manager Entry point to ComputeLimitManager.
     */
    public static void replaceTheFullSetOfMemberCapOverridesForASharedLimitCap(
        com.azure.resourcemanager.computelimit.ComputeLimitManager manager) {
        manager.sharedLimitCaps()
            .setMemberCapOverridesWithResponse("eastus", "StandardDSv3Family",
                new SetMemberCapOverridesRequest().withMemberCapOverrides(Arrays.asList(
                    new MemberCap().withSubscriptionId("11111111-1111-1111-1111-111111111111").withCap(200),
                    new MemberCap().withSubscriptionId("22222222-2222-2222-2222-222222222222").withCap(150))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-07-01/SharedLimitCaps_SetMemberCapOverrides_ClearAll.json
     */
    /**
     * Sample code: Clear all member cap overrides (supply an empty array).
     * 
     * @param manager Entry point to ComputeLimitManager.
     */
    public static void clearAllMemberCapOverridesSupplyAnEmptyArray(
        com.azure.resourcemanager.computelimit.ComputeLimitManager manager) {
        manager.sharedLimitCaps()
            .setMemberCapOverridesWithResponse("eastus", "StandardDSv3Family",
                new SetMemberCapOverridesRequest().withMemberCapOverrides(Arrays.asList()),
                com.azure.core.util.Context.NONE);
    }
}
```

### SharedLimits_Create

```java
import com.azure.resourcemanager.computelimit.models.SharedLimitProperties;

/**
 * Samples for SharedLimits Create.
 */
public final class SharedLimitsCreateSamples {
    /*
     * x-ms-original-file: 2026-07-01/SharedLimits_Create.json
     */
    /**
     * Sample code: Create a shared limit.
     * 
     * @param manager Entry point to ComputeLimitManager.
     */
    public static void createASharedLimit(com.azure.resourcemanager.computelimit.ComputeLimitManager manager) {
        manager.sharedLimits()
            .define("StandardDSv3Family")
            .withExistingLocation("eastus")
            .withProperties(new SharedLimitProperties())
            .create();
    }
}
```

### SharedLimits_Delete

```java
/**
 * Samples for SharedLimits Delete.
 */
public final class SharedLimitsDeleteSamples {
    /*
     * x-ms-original-file: 2026-07-01/SharedLimits_Delete.json
     */
    /**
     * Sample code: Delete a shared limit.
     * 
     * @param manager Entry point to ComputeLimitManager.
     */
    public static void deleteASharedLimit(com.azure.resourcemanager.computelimit.ComputeLimitManager manager) {
        manager.sharedLimits()
            .deleteByResourceGroupWithResponse("eastus", "StandardDSv3Family", com.azure.core.util.Context.NONE);
    }
}
```

### SharedLimits_Get

```java
/**
 * Samples for SharedLimits Get.
 */
public final class SharedLimitsGetSamples {
    /*
     * x-ms-original-file: 2026-07-01/SharedLimits_Get.json
     */
    /**
     * Sample code: Get a shared limit.
     * 
     * @param manager Entry point to ComputeLimitManager.
     */
    public static void getASharedLimit(com.azure.resourcemanager.computelimit.ComputeLimitManager manager) {
        manager.sharedLimits().getWithResponse("eastus", "StandardDSv3Family", com.azure.core.util.Context.NONE);
    }
}
```

### SharedLimits_ListBySubscriptionLocationResource

```java
/**
 * Samples for SharedLimits ListBySubscriptionLocationResource.
 */
public final class SharedLimitsListBySubscriptionLocationResourceSamples {
    /*
     * x-ms-original-file: 2026-07-01/SharedLimits_List.json
     */
    /**
     * Sample code: List all shared limits for a scope.
     * 
     * @param manager Entry point to ComputeLimitManager.
     */
    public static void
        listAllSharedLimitsForAScope(com.azure.resourcemanager.computelimit.ComputeLimitManager manager) {
        manager.sharedLimits().listBySubscriptionLocationResource("eastus", com.azure.core.util.Context.NONE);
    }
}
```

### VmFamilies_Get

```java
/**
 * Samples for VmFamilies Get.
 */
public final class VmFamiliesGetSamples {
    /*
     * x-ms-original-file: 2026-07-01/VmFamilies_Get.json
     */
    /**
     * Sample code: Get a VM family.
     * 
     * @param manager Entry point to ComputeLimitManager.
     */
    public static void getAVMFamily(com.azure.resourcemanager.computelimit.ComputeLimitManager manager) {
        manager.vmFamilies().getWithResponse("eastus", "standardDSv2Family", com.azure.core.util.Context.NONE);
    }
}
```

### VmFamilies_ListBySubscriptionLocationResource

```java
/**
 * Samples for VmFamilies ListBySubscriptionLocationResource.
 */
public final class VmFamiliesListBySubscriptionLocationResourceSamples {
    /*
     * x-ms-original-file: 2026-07-01/VmFamilies_List.json
     */
    /**
     * Sample code: List VM families.
     * 
     * @param manager Entry point to ComputeLimitManager.
     */
    public static void listVMFamilies(com.azure.resourcemanager.computelimit.ComputeLimitManager manager) {
        manager.vmFamilies().listBySubscriptionLocationResource("eastus", null, com.azure.core.util.Context.NONE);
    }
}
```

