# Code snippets and samples


## FabricCapacities

- [CheckNameAvailability](#fabriccapacities_checknameavailability)
- [CreateOrUpdate](#fabriccapacities_createorupdate)
- [Delete](#fabriccapacities_delete)
- [GetByResourceGroup](#fabriccapacities_getbyresourcegroup)
- [List](#fabriccapacities_list)
- [ListByResourceGroup](#fabriccapacities_listbyresourcegroup)
- [ListSkus](#fabriccapacities_listskus)
- [ListSkusForCapacity](#fabriccapacities_listskusforcapacity)
- [Resume](#fabriccapacities_resume)
- [Suspend](#fabriccapacities_suspend)
- [Update](#fabriccapacities_update)

## Operations

- [List](#operations_list)
### FabricCapacities_CheckNameAvailability

```java
import com.azure.resourcemanager.fabric.models.CheckNameAvailabilityRequest;

/**
 * Samples for FabricCapacities CheckNameAvailability.
 */
public final class FabricCapacitiesCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: 2023-11-01/FabricCapacities_CheckNameAvailability.json
     */
    /**
     * Sample code: Check name availability of a capacity.
     * 
     * @param manager Entry point to FabricManager.
     */
    public static void checkNameAvailabilityOfACapacity(com.azure.resourcemanager.fabric.FabricManager manager) {
        manager.fabricCapacities()
            .checkNameAvailabilityWithResponse("westcentralus",
                new CheckNameAvailabilityRequest().withName("azsdktest").withType("Microsoft.Fabric/capacities"),
                com.azure.core.util.Context.NONE);
    }
}
```

### FabricCapacities_CreateOrUpdate

```java
import com.azure.resourcemanager.fabric.models.CapacityAdministration;
import com.azure.resourcemanager.fabric.models.FabricCapacityProperties;
import com.azure.resourcemanager.fabric.models.RpSku;
import com.azure.resourcemanager.fabric.models.RpSkuTier;
import java.util.Arrays;

/**
 * Samples for FabricCapacities CreateOrUpdate.
 */
public final class FabricCapacitiesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2023-11-01/FabricCapacities_CreateOrUpdate.json
     */
    /**
     * Sample code: Create or update a capacity.
     * 
     * @param manager Entry point to FabricManager.
     */
    public static void createOrUpdateACapacity(com.azure.resourcemanager.fabric.FabricManager manager) {
        manager.fabricCapacities()
            .define("azsdktest")
            .withRegion("westcentralus")
            .withExistingResourceGroup("TestRG")
            .withProperties(new FabricCapacityProperties().withAdministration(new CapacityAdministration()
                .withMembers(Arrays.asList("azsdktest@microsoft.com", "azsdktest2@microsoft.com"))))
            .withSku(new RpSku().withName("F2").withTier(RpSkuTier.FABRIC))
            .create();
    }
}
```

### FabricCapacities_Delete

```java
/**
 * Samples for FabricCapacities Delete.
 */
public final class FabricCapacitiesDeleteSamples {
    /*
     * x-ms-original-file: 2023-11-01/FabricCapacities_Delete.json
     */
    /**
     * Sample code: Delete a capacity.
     * 
     * @param manager Entry point to FabricManager.
     */
    public static void deleteACapacity(com.azure.resourcemanager.fabric.FabricManager manager) {
        manager.fabricCapacities().delete("TestRG", "azsdktest", com.azure.core.util.Context.NONE);
    }
}
```

### FabricCapacities_GetByResourceGroup

```java
/**
 * Samples for FabricCapacities GetByResourceGroup.
 */
public final class FabricCapacitiesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2023-11-01/FabricCapacities_Get.json
     */
    /**
     * Sample code: Get a capacity.
     * 
     * @param manager Entry point to FabricManager.
     */
    public static void getACapacity(com.azure.resourcemanager.fabric.FabricManager manager) {
        manager.fabricCapacities()
            .getByResourceGroupWithResponse("TestRG", "azsdktest", com.azure.core.util.Context.NONE);
    }
}
```

### FabricCapacities_List

```java
/**
 * Samples for FabricCapacities List.
 */
public final class FabricCapacitiesListSamples {
    /*
     * x-ms-original-file: 2023-11-01/FabricCapacities_ListBySubscription.json
     */
    /**
     * Sample code: List capacities by subscription.
     * 
     * @param manager Entry point to FabricManager.
     */
    public static void listCapacitiesBySubscription(com.azure.resourcemanager.fabric.FabricManager manager) {
        manager.fabricCapacities().list(com.azure.core.util.Context.NONE);
    }
}
```

### FabricCapacities_ListByResourceGroup

```java
/**
 * Samples for FabricCapacities ListByResourceGroup.
 */
public final class FabricCapacitiesListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2023-11-01/FabricCapacities_ListByResourceGroup.json
     */
    /**
     * Sample code: List capacities by resource group.
     * 
     * @param manager Entry point to FabricManager.
     */
    public static void listCapacitiesByResourceGroup(com.azure.resourcemanager.fabric.FabricManager manager) {
        manager.fabricCapacities().listByResourceGroup("TestRG", com.azure.core.util.Context.NONE);
    }
}
```

### FabricCapacities_ListSkus

```java
/**
 * Samples for FabricCapacities ListSkus.
 */
public final class FabricCapacitiesListSkusSamples {
    /*
     * x-ms-original-file: 2023-11-01/FabricCapacities_ListSkus.json
     */
    /**
     * Sample code: List eligible SKUs for a new capacity.
     * 
     * @param manager Entry point to FabricManager.
     */
    public static void listEligibleSKUsForANewCapacity(com.azure.resourcemanager.fabric.FabricManager manager) {
        manager.fabricCapacities().listSkus(com.azure.core.util.Context.NONE);
    }
}
```

### FabricCapacities_ListSkusForCapacity

```java
/**
 * Samples for FabricCapacities ListSkusForCapacity.
 */
public final class FabricCapacitiesListSkusForCapacitySamples {
    /*
     * x-ms-original-file: 2023-11-01/FabricCapacities_ListSkusForCapacity.json
     */
    /**
     * Sample code: List eligible SKUs for an existing capacity.
     * 
     * @param manager Entry point to FabricManager.
     */
    public static void listEligibleSKUsForAnExistingCapacity(com.azure.resourcemanager.fabric.FabricManager manager) {
        manager.fabricCapacities().listSkusForCapacity("TestRG", "azsdktest", com.azure.core.util.Context.NONE);
    }
}
```

### FabricCapacities_Resume

```java
/**
 * Samples for FabricCapacities Resume.
 */
public final class FabricCapacitiesResumeSamples {
    /*
     * x-ms-original-file: 2023-11-01/FabricCapacities_Resume.json
     */
    /**
     * Sample code: Resume capacity.
     * 
     * @param manager Entry point to FabricManager.
     */
    public static void resumeCapacity(com.azure.resourcemanager.fabric.FabricManager manager) {
        manager.fabricCapacities().resume("TestRG", "azsdktest", com.azure.core.util.Context.NONE);
    }
}
```

### FabricCapacities_Suspend

```java
/**
 * Samples for FabricCapacities Suspend.
 */
public final class FabricCapacitiesSuspendSamples {
    /*
     * x-ms-original-file: 2023-11-01/FabricCapacities_Suspend.json
     */
    /**
     * Sample code: Suspend capacity.
     * 
     * @param manager Entry point to FabricManager.
     */
    public static void suspendCapacity(com.azure.resourcemanager.fabric.FabricManager manager) {
        manager.fabricCapacities().suspend("TestRG", "azsdktest", com.azure.core.util.Context.NONE);
    }
}
```

### FabricCapacities_Update

```java
import com.azure.resourcemanager.fabric.models.CapacityAdministration;
import com.azure.resourcemanager.fabric.models.FabricCapacity;
import com.azure.resourcemanager.fabric.models.FabricCapacityUpdateProperties;
import com.azure.resourcemanager.fabric.models.RpSku;
import com.azure.resourcemanager.fabric.models.RpSkuTier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for FabricCapacities Update.
 */
public final class FabricCapacitiesUpdateSamples {
    /*
     * x-ms-original-file: 2023-11-01/FabricCapacities_Update.json
     */
    /**
     * Sample code: Update capacity properties.
     * 
     * @param manager Entry point to FabricManager.
     */
    public static void updateCapacityProperties(com.azure.resourcemanager.fabric.FabricManager manager) {
        FabricCapacity resource = manager.fabricCapacities()
            .getByResourceGroupWithResponse("TestRG", "azsdktest", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("testKey", "fakeTokenPlaceholder"))
            .withSku(new RpSku().withName("F8").withTier(RpSkuTier.FABRIC))
            .withProperties(new FabricCapacityUpdateProperties().withAdministration(
                new CapacityAdministration().withMembers(Arrays.asList("azsdktest2@microsoft.com"))))
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
     * x-ms-original-file: 2023-11-01/Operations_List.json
     */
    /**
     * Sample code: List operations.
     * 
     * @param manager Entry point to FabricManager.
     */
    public static void listOperations(com.azure.resourcemanager.fabric.FabricManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

