# Code snippets and samples


## Slis

- [CreateOrUpdate](#slis_createorupdate)
- [Delete](#slis_delete)
- [Get](#slis_get)
- [ListByParent](#slis_listbyparent)
### Slis_CreateOrUpdate

```java
import com.azure.resourcemanager.monitor.slis.fluent.models.SliInner;
import com.azure.resourcemanager.monitor.slis.models.AmwAccount;
import com.azure.resourcemanager.monitor.slis.models.Baseline;
import com.azure.resourcemanager.monitor.slis.models.BaselineProperties;
import com.azure.resourcemanager.monitor.slis.models.Category;
import com.azure.resourcemanager.monitor.slis.models.Condition;
import com.azure.resourcemanager.monitor.slis.models.ConditionOperator;
import com.azure.resourcemanager.monitor.slis.models.EvaluationCalculationType;
import com.azure.resourcemanager.monitor.slis.models.EvaluationType;
import com.azure.resourcemanager.monitor.slis.models.Signal;
import com.azure.resourcemanager.monitor.slis.models.SignalSource;
import com.azure.resourcemanager.monitor.slis.models.SliProperties;
import com.azure.resourcemanager.monitor.slis.models.SliResource;
import com.azure.resourcemanager.monitor.slis.models.SpatialAggregation;
import com.azure.resourcemanager.monitor.slis.models.SpatialAggregationType;
import com.azure.resourcemanager.monitor.slis.models.TemporalAggregation;
import com.azure.resourcemanager.monitor.slis.models.TemporalAggregationType;
import com.azure.resourcemanager.monitor.slis.models.WindowUptimeCriteria;
import com.azure.resourcemanager.monitor.slis.models.WindowUptimeCriteriaComparator;
import java.util.Arrays;

/**
 * Samples for Slis CreateOrUpdate.
 */
public final class SlisCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-03-01-preview/Slis_CreateOrUpdate.json
     */
    /**
     * Sample code: CreateSli.
     * 
     * @param manager Entry point to SlisManager.
     */
    public static void createSli(com.azure.resourcemanager.monitor.slis.SlisManager manager) {
        manager.slis()
            .createOrUpdateWithResponse("testSG", "testSli", new SliInner().withProperties(new SliResource()
                .withDescription("Measures the performance characteristics of the GetContosoUsers() API. ")
                .withCategory(Category.LATENCY)
                .withEvaluationType(EvaluationType.WINDOW_BASED)
                .withDestinationAmwAccounts(Arrays.asList(new AmwAccount()
                    .withResourceId(
                        "/subscriptions/<subId>/resourcegroups/<rgId>/providers/microsoft.monitor/accounts/<dest>")
                    .withIdentity(
                        "/subscriptions/<subId>/resourcegroups/<rgId>/providers/Microsoft.ManagedIdentity/userAssignedIdentities/<idName>")))
                .withBaselineProperties(new BaselineProperties().withBaseline(new Baseline().withValue(99.0)
                    .withEvaluationPeriodDays(30)
                    .withEvaluationCalculationType(EvaluationCalculationType.CALENDAR_DAYS)))
                .withEnableAlert(true)
                .withSliProperties(new SliProperties().withSignals(new Signal().withSignalSources(Arrays.asList(
                    new SignalSource().withSignalSourceId("A")
                        .withSourceAmwAccountManagedIdentity(
                            "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/myIdentity")
                        .withSourceAmwAccountResourceId(
                            "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/microsoft.monitor/accounts/myAccount")
                        .withMetricNamespace("ContosoMetricsWest")
                        .withMetricName("Stamp1Latency")
                        .withFilters(Arrays.asList(new Condition().withDimensionName("ApiName")
                            .withOperator(ConditionOperator.EQUAL)
                            .withValue("GetContosoUsers")))
                        .withSpatialAggregation(new SpatialAggregation().withType(SpatialAggregationType.AVERAGE)
                            .withDimensions(Arrays.asList("Region", "ResponseCode")))
                        .withTemporalAggregation(new TemporalAggregation().withType(TemporalAggregationType.AVERAGE)
                            .withWindowSizeMinutes(5)),
                    new SignalSource().withSignalSourceId("B")
                        .withSourceAmwAccountManagedIdentity(
                            "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/myIdentity")
                        .withSourceAmwAccountResourceId(
                            "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/microsoft.monitor/accounts/myAccount")
                        .withMetricNamespace("ContosoMetricsEast")
                        .withMetricName("Stamp2Latency")
                        .withFilters(Arrays.asList(new Condition().withDimensionName("ApiName")
                            .withOperator(ConditionOperator.EQUAL)
                            .withValue("GetContosoUsers")))
                        .withSpatialAggregation(new SpatialAggregation().withType(SpatialAggregationType.AVERAGE)
                            .withDimensions(Arrays.asList("Region", "ResponseCode")))
                        .withTemporalAggregation(new TemporalAggregation().withType(TemporalAggregationType.AVERAGE)
                            .withWindowSizeMinutes(5))))
                    .withSignalFormula("(A + B) /2"))
                    .withWindowUptimeCriteria(new WindowUptimeCriteria().withTarget(95.0)
                        .withComparator(WindowUptimeCriteriaComparator.GREATER_THAN_OR_EQUAL)))),
                com.azure.core.util.Context.NONE);
    }
}
```

### Slis_Delete

```java
/**
 * Samples for Slis Delete.
 */
public final class SlisDeleteSamples {
    /*
     * x-ms-original-file: 2025-03-01-preview/Slis_Delete.json
     */
    /**
     * Sample code: DeleteSli.
     * 
     * @param manager Entry point to SlisManager.
     */
    public static void deleteSli(com.azure.resourcemanager.monitor.slis.SlisManager manager) {
        manager.slis().deleteByResourceGroupWithResponse("testSG", "testSli", com.azure.core.util.Context.NONE);
    }
}
```

### Slis_Get

```java
/**
 * Samples for Slis Get.
 */
public final class SlisGetSamples {
    /*
     * x-ms-original-file: 2025-03-01-preview/Slis_Get.json
     */
    /**
     * Sample code: GetSli.
     * 
     * @param manager Entry point to SlisManager.
     */
    public static void getSli(com.azure.resourcemanager.monitor.slis.SlisManager manager) {
        manager.slis().getWithResponse("testSG", "testSli", com.azure.core.util.Context.NONE);
    }
}
```

### Slis_ListByParent

```java
/**
 * Samples for Slis ListByParent.
 */
public final class SlisListByParentSamples {
    /*
     * x-ms-original-file: 2025-03-01-preview/Slis_ListByParent.json
     */
    /**
     * Sample code: SlisListByParent.
     * 
     * @param manager Entry point to SlisManager.
     */
    public static void slisListByParent(com.azure.resourcemanager.monitor.slis.SlisManager manager) {
        manager.slis().listByParent("testSG", com.azure.core.util.Context.NONE);
    }
}
```

