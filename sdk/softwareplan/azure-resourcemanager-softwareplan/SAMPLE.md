# Code snippets and samples


## HybridUseBenefit

- [Create](#hybridusebenefit_create)
- [Delete](#hybridusebenefit_delete)
- [Get](#hybridusebenefit_get)
- [List](#hybridusebenefit_list)
- [Update](#hybridusebenefit_update)

## HybridUseBenefitRevision

- [List](#hybridusebenefitrevision_list)

## Operations

- [List](#operations_list)

## SoftwarePlan

- [Register](#softwareplan_register)
### HybridUseBenefit_Create

```java
import com.azure.resourcemanager.softwareplan.models.Sku;

/** Samples for HybridUseBenefit Create. */
public final class HybridUseBenefitCreateSamples {
    /*
     * x-ms-original-file: specification/softwareplan/resource-manager/Microsoft.SoftwarePlan/stable/2019-12-01/examples/PutHybridUseBenefit.json
     */
    /**
     * Sample code: HybridUseBenefit.
     *
     * @param manager Entry point to SoftwarePlanManager.
     */
    public static void hybridUseBenefit(com.azure.resourcemanager.softwareplan.SoftwarePlanManager manager) {
        manager
            .hybridUseBenefits()
            .define("94f46eda-45f8-493a-8425-251921463a89")
            .withExistingScope(
                "subscriptions/{sub-id}/resourceGroups/{rg-name}/providers/Microsoft.Compute/HostGroups/{host-group-name}/hosts/{host-name}")
            .withSku(new Sku().withName("SQL_Server_Perpetual"))
            .create();
    }
}
```

### HybridUseBenefit_Delete

```java
import com.azure.core.util.Context;

/** Samples for HybridUseBenefit Delete. */
public final class HybridUseBenefitDeleteSamples {
    /*
     * x-ms-original-file: specification/softwareplan/resource-manager/Microsoft.SoftwarePlan/stable/2019-12-01/examples/DeleteHybridUseBenefit.json
     */
    /**
     * Sample code: HybridUseBenefit.
     *
     * @param manager Entry point to SoftwarePlanManager.
     */
    public static void hybridUseBenefit(com.azure.resourcemanager.softwareplan.SoftwarePlanManager manager) {
        manager
            .hybridUseBenefits()
            .deleteByResourceGroupWithResponse(
                "subscriptions/{sub-id}/resourceGroups/{rg-name}/providers/Microsoft.Compute/HostGroups/{host-group-name}/hosts/{host-name}",
                "94f46eda-45f8-493a-8425-251921463a89",
                Context.NONE);
    }
}
```

### HybridUseBenefit_Get

```java
import com.azure.core.util.Context;

/** Samples for HybridUseBenefit Get. */
public final class HybridUseBenefitGetSamples {
    /*
     * x-ms-original-file: specification/softwareplan/resource-manager/Microsoft.SoftwarePlan/stable/2019-12-01/examples/GetSingleHybridUseBenefit.json
     */
    /**
     * Sample code: HybridUseBenefit.
     *
     * @param manager Entry point to SoftwarePlanManager.
     */
    public static void hybridUseBenefit(com.azure.resourcemanager.softwareplan.SoftwarePlanManager manager) {
        manager
            .hybridUseBenefits()
            .getWithResponse(
                "subscriptions/{sub-id}/resourceGroups/{rg-name}/providers/Microsoft.Compute/HostGroups/{host-group-name}/hosts/{host-name}",
                "94f46eda-45f8-493a-8425-251921463a89",
                Context.NONE);
    }
}
```

### HybridUseBenefit_List

```java
import com.azure.core.util.Context;

/** Samples for HybridUseBenefit List. */
public final class HybridUseBenefitListSamples {
    /*
     * x-ms-original-file: specification/softwareplan/resource-manager/Microsoft.SoftwarePlan/stable/2019-12-01/examples/GetAllHybridUseBenefits.json
     */
    /**
     * Sample code: HybridUseBenefitListResult.
     *
     * @param manager Entry point to SoftwarePlanManager.
     */
    public static void hybridUseBenefitListResult(com.azure.resourcemanager.softwareplan.SoftwarePlanManager manager) {
        manager
            .hybridUseBenefits()
            .list(
                "subscriptions/{sub-id}/resourceGroups/{rg-name}/providers/Microsoft.Compute/HostGroups/{host-group-name}/hosts/{host-name}",
                "SQL_Server_EE_AHB",
                Context.NONE);
    }
}
```

### HybridUseBenefit_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.softwareplan.models.HybridUseBenefitModel;
import com.azure.resourcemanager.softwareplan.models.Sku;

/** Samples for HybridUseBenefit Update. */
public final class HybridUseBenefitUpdateSamples {
    /*
     * x-ms-original-file: specification/softwareplan/resource-manager/Microsoft.SoftwarePlan/stable/2019-12-01/examples/PatchHybridUseBenefit.json
     */
    /**
     * Sample code: HybridUseBenefit.
     *
     * @param manager Entry point to SoftwarePlanManager.
     */
    public static void hybridUseBenefit(com.azure.resourcemanager.softwareplan.SoftwarePlanManager manager) {
        HybridUseBenefitModel resource =
            manager
                .hybridUseBenefits()
                .getWithResponse(
                    "subscriptions/{sub-id}/resourceGroups/{rg-name}/providers/Microsoft.Compute/HostGroups/{host-group-name}/hosts/{host-name}",
                    "94f46eda-45f8-493a-8425-251921463a89",
                    Context.NONE)
                .getValue();
        resource.update().withSku(new Sku().withName("SQLBYOLStandardForADH")).apply();
    }
}
```

### HybridUseBenefitRevision_List

```java
import com.azure.core.util.Context;

/** Samples for HybridUseBenefitRevision List. */
public final class HybridUseBenefitRevisionListSamples {
    /*
     * x-ms-original-file: specification/softwareplan/resource-manager/Microsoft.SoftwarePlan/stable/2019-12-01/examples/GetHybridUseBenefitRevisions.json
     */
    /**
     * Sample code: HybridUseBenefitRevisionsResponse.
     *
     * @param manager Entry point to SoftwarePlanManager.
     */
    public static void hybridUseBenefitRevisionsResponse(
        com.azure.resourcemanager.softwareplan.SoftwarePlanManager manager) {
        manager
            .hybridUseBenefitRevisions()
            .list(
                "subscriptions/{sub-id}/resourceGroups/{rg-name}/providers/Microsoft.Compute/HostGroups/{host-group-name}/hosts/{host-name}",
                "94f46eda-45f8-493a-8425-251921463a89",
                Context.NONE);
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/softwareplan/resource-manager/Microsoft.SoftwarePlan/stable/2019-12-01/examples/GetOperations.json
     */
    /**
     * Sample code: GetOperations.
     *
     * @param manager Entry point to SoftwarePlanManager.
     */
    public static void getOperations(com.azure.resourcemanager.softwareplan.SoftwarePlanManager manager) {
        manager
            .operations()
            .list(
                "subscriptions/{sub-id}/resourceGroups/{rg-name}/providers/Microsoft.Compute/HostGroups/{host-group-name}/hosts/{host-name}",
                Context.NONE);
    }
}
```

### SoftwarePlan_Register

```java
import com.azure.core.util.Context;

/** Samples for SoftwarePlan Register. */
public final class SoftwarePlanRegisterSamples {
    /*
     * x-ms-original-file: specification/softwareplan/resource-manager/Microsoft.SoftwarePlan/stable/2019-12-01/examples/RegisterSubscription.json
     */
    /**
     * Sample code: HybridUseBenefitListResult.
     *
     * @param manager Entry point to SoftwarePlanManager.
     */
    public static void hybridUseBenefitListResult(com.azure.resourcemanager.softwareplan.SoftwarePlanManager manager) {
        manager.softwarePlans().registerWithResponse(Context.NONE);
    }
}
```

