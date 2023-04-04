# Code snippets and samples


## AutoScaleVCores

- [Create](#autoscalevcores_create)
- [Delete](#autoscalevcores_delete)
- [GetByResourceGroup](#autoscalevcores_getbyresourcegroup)
- [List](#autoscalevcores_list)
- [ListByResourceGroup](#autoscalevcores_listbyresourcegroup)
- [Update](#autoscalevcores_update)

## Capacities

- [CheckNameAvailability](#capacities_checknameavailability)
- [Create](#capacities_create)
- [Delete](#capacities_delete)
- [GetByResourceGroup](#capacities_getbyresourcegroup)
- [List](#capacities_list)
- [ListByResourceGroup](#capacities_listbyresourcegroup)
- [ListSkus](#capacities_listskus)
- [ListSkusForCapacity](#capacities_listskusforcapacity)
- [Resume](#capacities_resume)
- [Suspend](#capacities_suspend)
- [Update](#capacities_update)

## Operations

- [List](#operations_list)
### AutoScaleVCores_Create

```java
import com.azure.resourcemanager.powerbidedicated.models.AutoScaleVCoreSku;
import com.azure.resourcemanager.powerbidedicated.models.VCoreSkuTier;
import java.util.HashMap;
import java.util.Map;

/** Samples for AutoScaleVCores Create. */
public final class AutoScaleVCoresCreateSamples {
    /*
     * x-ms-original-file: specification/powerbidedicated/resource-manager/Microsoft.PowerBIdedicated/stable/2021-01-01/examples/createAutoScaleVCore.json
     */
    /**
     * Sample code: Create auto scale v-core.
     *
     * @param manager Entry point to PowerBIDedicatedManager.
     */
    public static void createAutoScaleVCore(
        com.azure.resourcemanager.powerbidedicated.PowerBIDedicatedManager manager) {
        manager
            .autoScaleVCores()
            .define("testvcore")
            .withRegion("West US")
            .withExistingResourceGroup("TestRG")
            .withSku(new AutoScaleVCoreSku().withName("AutoScale").withTier(VCoreSkuTier.AUTO_SCALE).withCapacity(0))
            .withTags(mapOf("testKey", "testValue"))
            .withCapacityObjectId("a28f00bd-5330-4572-88f1-fa883e074785")
            .withCapacityLimit(10)
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

### AutoScaleVCores_Delete

```java
/** Samples for AutoScaleVCores Delete. */
public final class AutoScaleVCoresDeleteSamples {
    /*
     * x-ms-original-file: specification/powerbidedicated/resource-manager/Microsoft.PowerBIdedicated/stable/2021-01-01/examples/deleteAutoScaleVCore.json
     */
    /**
     * Sample code: Delete an auto scale v-core.
     *
     * @param manager Entry point to PowerBIDedicatedManager.
     */
    public static void deleteAnAutoScaleVCore(
        com.azure.resourcemanager.powerbidedicated.PowerBIDedicatedManager manager) {
        manager
            .autoScaleVCores()
            .deleteByResourceGroupWithResponse("TestRG", "testvcore", com.azure.core.util.Context.NONE);
    }
}
```

### AutoScaleVCores_GetByResourceGroup

```java
/** Samples for AutoScaleVCores GetByResourceGroup. */
public final class AutoScaleVCoresGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/powerbidedicated/resource-manager/Microsoft.PowerBIdedicated/stable/2021-01-01/examples/getAutoScaleVCore.json
     */
    /**
     * Sample code: Get details of an auto scale v-core.
     *
     * @param manager Entry point to PowerBIDedicatedManager.
     */
    public static void getDetailsOfAnAutoScaleVCore(
        com.azure.resourcemanager.powerbidedicated.PowerBIDedicatedManager manager) {
        manager
            .autoScaleVCores()
            .getByResourceGroupWithResponse("TestRG", "testvcore", com.azure.core.util.Context.NONE);
    }
}
```

### AutoScaleVCores_List

```java
/** Samples for AutoScaleVCores List. */
public final class AutoScaleVCoresListSamples {
    /*
     * x-ms-original-file: specification/powerbidedicated/resource-manager/Microsoft.PowerBIdedicated/stable/2021-01-01/examples/listAutoScaleVCoresInSubscription.json
     */
    /**
     * Sample code: List auto scale v-cores in subscription.
     *
     * @param manager Entry point to PowerBIDedicatedManager.
     */
    public static void listAutoScaleVCoresInSubscription(
        com.azure.resourcemanager.powerbidedicated.PowerBIDedicatedManager manager) {
        manager.autoScaleVCores().list(com.azure.core.util.Context.NONE);
    }
}
```

### AutoScaleVCores_ListByResourceGroup

```java
/** Samples for AutoScaleVCores ListByResourceGroup. */
public final class AutoScaleVCoresListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/powerbidedicated/resource-manager/Microsoft.PowerBIdedicated/stable/2021-01-01/examples/listAutoScaleVCoresInResourceGroup.json
     */
    /**
     * Sample code: List auto scale v-cores in resource group.
     *
     * @param manager Entry point to PowerBIDedicatedManager.
     */
    public static void listAutoScaleVCoresInResourceGroup(
        com.azure.resourcemanager.powerbidedicated.PowerBIDedicatedManager manager) {
        manager.autoScaleVCores().listByResourceGroup("TestRG", com.azure.core.util.Context.NONE);
    }
}
```

### AutoScaleVCores_Update

```java
import com.azure.resourcemanager.powerbidedicated.models.AutoScaleVCore;
import com.azure.resourcemanager.powerbidedicated.models.AutoScaleVCoreSku;
import com.azure.resourcemanager.powerbidedicated.models.VCoreSkuTier;
import java.util.HashMap;
import java.util.Map;

/** Samples for AutoScaleVCores Update. */
public final class AutoScaleVCoresUpdateSamples {
    /*
     * x-ms-original-file: specification/powerbidedicated/resource-manager/Microsoft.PowerBIdedicated/stable/2021-01-01/examples/updateAutoScaleVCore.json
     */
    /**
     * Sample code: Update auto scale v-core parameters.
     *
     * @param manager Entry point to PowerBIDedicatedManager.
     */
    public static void updateAutoScaleVCoreParameters(
        com.azure.resourcemanager.powerbidedicated.PowerBIDedicatedManager manager) {
        AutoScaleVCore resource =
            manager
                .autoScaleVCores()
                .getByResourceGroupWithResponse("TestRG", "testvcore", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("testKey", "testValue"))
            .withSku(new AutoScaleVCoreSku().withName("AutoScale").withTier(VCoreSkuTier.AUTO_SCALE).withCapacity(0))
            .withCapacityLimit(20)
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

### Capacities_CheckNameAvailability

```java
import com.azure.resourcemanager.powerbidedicated.models.CheckCapacityNameAvailabilityParameters;

/** Samples for Capacities CheckNameAvailability. */
public final class CapacitiesCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/powerbidedicated/resource-manager/Microsoft.PowerBIdedicated/stable/2021-01-01/examples/checkNameAvailability.json
     */
    /**
     * Sample code: Check name availability of a capacity.
     *
     * @param manager Entry point to PowerBIDedicatedManager.
     */
    public static void checkNameAvailabilityOfACapacity(
        com.azure.resourcemanager.powerbidedicated.PowerBIDedicatedManager manager) {
        manager
            .capacities()
            .checkNameAvailabilityWithResponse(
                "West US",
                new CheckCapacityNameAvailabilityParameters()
                    .withName("azsdktest")
                    .withType("Microsoft.PowerBIDedicated/capacities"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Capacities_Create

```java
import com.azure.resourcemanager.powerbidedicated.models.CapacitySku;
import com.azure.resourcemanager.powerbidedicated.models.CapacitySkuTier;
import com.azure.resourcemanager.powerbidedicated.models.DedicatedCapacityAdministrators;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Capacities Create. */
public final class CapacitiesCreateSamples {
    /*
     * x-ms-original-file: specification/powerbidedicated/resource-manager/Microsoft.PowerBIdedicated/stable/2021-01-01/examples/createCapacity.json
     */
    /**
     * Sample code: Create capacity.
     *
     * @param manager Entry point to PowerBIDedicatedManager.
     */
    public static void createCapacity(com.azure.resourcemanager.powerbidedicated.PowerBIDedicatedManager manager) {
        manager
            .capacities()
            .define("azsdktest")
            .withRegion("West US")
            .withExistingResourceGroup("TestRG")
            .withSku(new CapacitySku().withName("A1").withTier(CapacitySkuTier.PBIE_AZURE))
            .withTags(mapOf("testKey", "testValue"))
            .withAdministration(
                new DedicatedCapacityAdministrators()
                    .withMembers(Arrays.asList("azsdktest@microsoft.com", "azsdktest2@microsoft.com")))
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

### Capacities_Delete

```java
/** Samples for Capacities Delete. */
public final class CapacitiesDeleteSamples {
    /*
     * x-ms-original-file: specification/powerbidedicated/resource-manager/Microsoft.PowerBIdedicated/stable/2021-01-01/examples/deleteCapacity.json
     */
    /**
     * Sample code: Get details of a capacity.
     *
     * @param manager Entry point to PowerBIDedicatedManager.
     */
    public static void getDetailsOfACapacity(
        com.azure.resourcemanager.powerbidedicated.PowerBIDedicatedManager manager) {
        manager.capacities().delete("TestRG", "azsdktest", com.azure.core.util.Context.NONE);
    }
}
```

### Capacities_GetByResourceGroup

```java
/** Samples for Capacities GetByResourceGroup. */
public final class CapacitiesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/powerbidedicated/resource-manager/Microsoft.PowerBIdedicated/stable/2021-01-01/examples/getCapacity.json
     */
    /**
     * Sample code: Get details of a capacity.
     *
     * @param manager Entry point to PowerBIDedicatedManager.
     */
    public static void getDetailsOfACapacity(
        com.azure.resourcemanager.powerbidedicated.PowerBIDedicatedManager manager) {
        manager.capacities().getByResourceGroupWithResponse("TestRG", "azsdktest", com.azure.core.util.Context.NONE);
    }
}
```

### Capacities_List

```java
/** Samples for Capacities List. */
public final class CapacitiesListSamples {
    /*
     * x-ms-original-file: specification/powerbidedicated/resource-manager/Microsoft.PowerBIdedicated/stable/2021-01-01/examples/listCapacitiesInSubscription.json
     */
    /**
     * Sample code: Get details of a capacity.
     *
     * @param manager Entry point to PowerBIDedicatedManager.
     */
    public static void getDetailsOfACapacity(
        com.azure.resourcemanager.powerbidedicated.PowerBIDedicatedManager manager) {
        manager.capacities().list(com.azure.core.util.Context.NONE);
    }
}
```

### Capacities_ListByResourceGroup

```java
/** Samples for Capacities ListByResourceGroup. */
public final class CapacitiesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/powerbidedicated/resource-manager/Microsoft.PowerBIdedicated/stable/2021-01-01/examples/listCapacitiesInResourceGroup.json
     */
    /**
     * Sample code: List capacities in resource group.
     *
     * @param manager Entry point to PowerBIDedicatedManager.
     */
    public static void listCapacitiesInResourceGroup(
        com.azure.resourcemanager.powerbidedicated.PowerBIDedicatedManager manager) {
        manager.capacities().listByResourceGroup("TestRG", com.azure.core.util.Context.NONE);
    }
}
```

### Capacities_ListSkus

```java
/** Samples for Capacities ListSkus. */
public final class CapacitiesListSkusSamples {
    /*
     * x-ms-original-file: specification/powerbidedicated/resource-manager/Microsoft.PowerBIdedicated/stable/2021-01-01/examples/listSKUsForNew.json
     */
    /**
     * Sample code: List eligible SKUs for a new capacity.
     *
     * @param manager Entry point to PowerBIDedicatedManager.
     */
    public static void listEligibleSKUsForANewCapacity(
        com.azure.resourcemanager.powerbidedicated.PowerBIDedicatedManager manager) {
        manager.capacities().listSkusWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

### Capacities_ListSkusForCapacity

```java
/** Samples for Capacities ListSkusForCapacity. */
public final class CapacitiesListSkusForCapacitySamples {
    /*
     * x-ms-original-file: specification/powerbidedicated/resource-manager/Microsoft.PowerBIdedicated/stable/2021-01-01/examples/listSKUsForExisting.json
     */
    /**
     * Sample code: List eligible SKUs for an existing capacity.
     *
     * @param manager Entry point to PowerBIDedicatedManager.
     */
    public static void listEligibleSKUsForAnExistingCapacity(
        com.azure.resourcemanager.powerbidedicated.PowerBIDedicatedManager manager) {
        manager.capacities().listSkusForCapacityWithResponse("TestRG", "azsdktest", com.azure.core.util.Context.NONE);
    }
}
```

### Capacities_Resume

```java
/** Samples for Capacities Resume. */
public final class CapacitiesResumeSamples {
    /*
     * x-ms-original-file: specification/powerbidedicated/resource-manager/Microsoft.PowerBIdedicated/stable/2021-01-01/examples/resumeCapacity.json
     */
    /**
     * Sample code: Get details of a capacity.
     *
     * @param manager Entry point to PowerBIDedicatedManager.
     */
    public static void getDetailsOfACapacity(
        com.azure.resourcemanager.powerbidedicated.PowerBIDedicatedManager manager) {
        manager.capacities().resume("TestRG", "azsdktest", com.azure.core.util.Context.NONE);
    }
}
```

### Capacities_Suspend

```java
/** Samples for Capacities Suspend. */
public final class CapacitiesSuspendSamples {
    /*
     * x-ms-original-file: specification/powerbidedicated/resource-manager/Microsoft.PowerBIdedicated/stable/2021-01-01/examples/suspendCapacity.json
     */
    /**
     * Sample code: Suspend capacity.
     *
     * @param manager Entry point to PowerBIDedicatedManager.
     */
    public static void suspendCapacity(com.azure.resourcemanager.powerbidedicated.PowerBIDedicatedManager manager) {
        manager.capacities().suspend("TestRG", "azsdktest", com.azure.core.util.Context.NONE);
    }
}
```

### Capacities_Update

```java
import com.azure.resourcemanager.powerbidedicated.models.CapacitySku;
import com.azure.resourcemanager.powerbidedicated.models.CapacitySkuTier;
import com.azure.resourcemanager.powerbidedicated.models.DedicatedCapacity;
import com.azure.resourcemanager.powerbidedicated.models.DedicatedCapacityAdministrators;
import com.azure.resourcemanager.powerbidedicated.models.Mode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Capacities Update. */
public final class CapacitiesUpdateSamples {
    /*
     * x-ms-original-file: specification/powerbidedicated/resource-manager/Microsoft.PowerBIdedicated/stable/2021-01-01/examples/updateToGen2.json
     */
    /**
     * Sample code: Update capacity to Generation 2.
     *
     * @param manager Entry point to PowerBIDedicatedManager.
     */
    public static void updateCapacityToGeneration2(
        com.azure.resourcemanager.powerbidedicated.PowerBIDedicatedManager manager) {
        DedicatedCapacity resource =
            manager
                .capacities()
                .getByResourceGroupWithResponse("TestRG", "azsdktest", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("testKey", "testValue"))
            .withSku(new CapacitySku().withName("A1").withTier(CapacitySkuTier.PBIE_AZURE))
            .withMode(Mode.GEN2)
            .apply();
    }

    /*
     * x-ms-original-file: specification/powerbidedicated/resource-manager/Microsoft.PowerBIdedicated/stable/2021-01-01/examples/updateCapacity.json
     */
    /**
     * Sample code: Update capacity parameters.
     *
     * @param manager Entry point to PowerBIDedicatedManager.
     */
    public static void updateCapacityParameters(
        com.azure.resourcemanager.powerbidedicated.PowerBIDedicatedManager manager) {
        DedicatedCapacity resource =
            manager
                .capacities()
                .getByResourceGroupWithResponse("TestRG", "azsdktest", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("testKey", "testValue"))
            .withSku(new CapacitySku().withName("A1").withTier(CapacitySkuTier.PBIE_AZURE))
            .withAdministration(
                new DedicatedCapacityAdministrators()
                    .withMembers(Arrays.asList("azsdktest@microsoft.com", "azsdktest2@microsoft.com")))
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

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/powerbidedicated/resource-manager/Microsoft.PowerBIdedicated/stable/2021-01-01/examples/operations.json
     */
    /**
     * Sample code: List operations.
     *
     * @param manager Entry point to PowerBIDedicatedManager.
     */
    public static void listOperations(com.azure.resourcemanager.powerbidedicated.PowerBIDedicatedManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

