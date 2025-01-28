# Code snippets and samples


## Operations

- [List](#operations_list)

## ScheduledActions

- [VirtualMachinesCancelOperations](#scheduledactions_virtualmachinescanceloperations)
- [VirtualMachinesExecuteDeallocate](#scheduledactions_virtualmachinesexecutedeallocate)
- [VirtualMachinesExecuteHibernate](#scheduledactions_virtualmachinesexecutehibernate)
- [VirtualMachinesExecuteStart](#scheduledactions_virtualmachinesexecutestart)
- [VirtualMachinesGetOperationErrors](#scheduledactions_virtualmachinesgetoperationerrors)
- [VirtualMachinesGetOperationStatus](#scheduledactions_virtualmachinesgetoperationstatus)
- [VirtualMachinesSubmitDeallocate](#scheduledactions_virtualmachinessubmitdeallocate)
- [VirtualMachinesSubmitHibernate](#scheduledactions_virtualmachinessubmithibernate)
- [VirtualMachinesSubmitStart](#scheduledactions_virtualmachinessubmitstart)
### Operations_List

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2024-10-01/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void operationsList(com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_VirtualMachinesCancelOperations

```java
import com.azure.resourcemanager.computeschedule.models.CancelOperationsRequest;
import java.util.Arrays;

/**
 * Samples for ScheduledActions VirtualMachinesCancelOperations.
 */
public final class ScheduledActionsVirtualMachinesCancelOperationsSamples {
    /*
     * x-ms-original-file: 2024-10-01/ScheduledActions_VirtualMachinesCancelOperations.json
     */
    /**
     * Sample code: ScheduledActions_VirtualMachinesCancelOperations.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsVirtualMachinesCancelOperations(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .virtualMachinesCancelOperationsWithResponse("eastus2euap",
                new CancelOperationsRequest().withOperationIds(Arrays.asList("23480d2f-1dca-4610-afb4-dd25eec1f34r"))
                    .withCorrelationid("23480d2f-1dca-4610-afb4-gg25eec1f34r"),
                com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_VirtualMachinesExecuteDeallocate

```java
import com.azure.resourcemanager.computeschedule.models.ExecuteDeallocateRequest;
import com.azure.resourcemanager.computeschedule.models.ExecutionParameters;
import com.azure.resourcemanager.computeschedule.models.Resources;
import com.azure.resourcemanager.computeschedule.models.RetryPolicy;
import java.util.Arrays;

/**
 * Samples for ScheduledActions VirtualMachinesExecuteDeallocate.
 */
public final class ScheduledActionsVirtualMachinesExecuteDeallocateSamples {
    /*
     * x-ms-original-file: 2024-10-01/ScheduledActions_VirtualMachinesExecuteDeallocate.json
     */
    /**
     * Sample code: ScheduledActions_VirtualMachinesExecuteDeallocate.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsVirtualMachinesExecuteDeallocate(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .virtualMachinesExecuteDeallocateWithResponse("eastus2euap", new ExecuteDeallocateRequest()
                .withExecutionParameters(new ExecutionParameters()
                    .withRetryPolicy(new RetryPolicy().withRetryCount(4).withRetryWindowInMinutes(27)))
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/YourSubscriptionId/resourceGroups/YourResourceGroupName/providers/Microsoft.Compute/virtualMachines/testResource3")))
                .withCorrelationid("23480d2f-1dca-4610-afb4-dd25eec1f34r"), com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_VirtualMachinesExecuteHibernate

```java
import com.azure.resourcemanager.computeschedule.models.ExecuteHibernateRequest;
import com.azure.resourcemanager.computeschedule.models.ExecutionParameters;
import com.azure.resourcemanager.computeschedule.models.Resources;
import com.azure.resourcemanager.computeschedule.models.RetryPolicy;
import java.util.Arrays;

/**
 * Samples for ScheduledActions VirtualMachinesExecuteHibernate.
 */
public final class ScheduledActionsVirtualMachinesExecuteHibernateSamples {
    /*
     * x-ms-original-file: 2024-10-01/ScheduledActions_VirtualMachinesExecuteHibernate.json
     */
    /**
     * Sample code: ScheduledActions_VirtualMachinesExecuteHibernate.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsVirtualMachinesExecuteHibernate(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .virtualMachinesExecuteHibernateWithResponse("eastus2euap", new ExecuteHibernateRequest()
                .withExecutionParameters(new ExecutionParameters()
                    .withRetryPolicy(new RetryPolicy().withRetryCount(5).withRetryWindowInMinutes(27)))
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/YourSubscriptionId/resourceGroups/YourResourceGroupName/providers/Microsoft.Compute/virtualMachines/testResource3")))
                .withCorrelationid("23480d2f-1dca-4610-afb4-dd25eec1f34r"), com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_VirtualMachinesExecuteStart

```java
import com.azure.resourcemanager.computeschedule.models.ExecuteStartRequest;
import com.azure.resourcemanager.computeschedule.models.ExecutionParameters;
import com.azure.resourcemanager.computeschedule.models.Resources;
import com.azure.resourcemanager.computeschedule.models.RetryPolicy;
import java.util.Arrays;

/**
 * Samples for ScheduledActions VirtualMachinesExecuteStart.
 */
public final class ScheduledActionsVirtualMachinesExecuteStartSamples {
    /*
     * x-ms-original-file: 2024-10-01/ScheduledActions_VirtualMachinesExecuteStart.json
     */
    /**
     * Sample code: ScheduledActions_VirtualMachinesExecuteStart.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsVirtualMachinesExecuteStart(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .virtualMachinesExecuteStartWithResponse("eastus2euap", new ExecuteStartRequest()
                .withExecutionParameters(new ExecutionParameters()
                    .withRetryPolicy(new RetryPolicy().withRetryCount(2).withRetryWindowInMinutes(27)))
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/YourSubscriptionId/resourceGroups/YourResourceGroupName/providers/Microsoft.Compute/virtualMachines/testResource3")))
                .withCorrelationid("23480d2f-1dca-4610-afb4-dd25eec1f34r"), com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_VirtualMachinesGetOperationErrors

```java
import com.azure.resourcemanager.computeschedule.models.GetOperationErrorsRequest;
import java.util.Arrays;

/**
 * Samples for ScheduledActions VirtualMachinesGetOperationErrors.
 */
public final class ScheduledActionsVirtualMachinesGetOperationErrorsSamples {
    /*
     * x-ms-original-file: 2024-10-01/ScheduledActions_VirtualMachinesGetOperationErrors.json
     */
    /**
     * Sample code: ScheduledActions_VirtualMachinesGetOperationErrors.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsVirtualMachinesGetOperationErrors(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .virtualMachinesGetOperationErrorsWithResponse("eastus2euap",
                new GetOperationErrorsRequest().withOperationIds(Arrays.asList("23480d2f-1dca-4610-afb4-dd25eec1f34r")),
                com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_VirtualMachinesGetOperationStatus

```java
import com.azure.resourcemanager.computeschedule.models.GetOperationStatusRequest;
import java.util.Arrays;

/**
 * Samples for ScheduledActions VirtualMachinesGetOperationStatus.
 */
public final class ScheduledActionsVirtualMachinesGetOperationStatusSamples {
    /*
     * x-ms-original-file: 2024-10-01/ScheduledActions_VirtualMachinesGetOperationStatus.json
     */
    /**
     * Sample code: ScheduledActions_VirtualMachinesGetOperationStatus.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsVirtualMachinesGetOperationStatus(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .virtualMachinesGetOperationStatusWithResponse("eastus2euap",
                new GetOperationStatusRequest().withOperationIds(Arrays.asList("23480d2f-1dca-4610-afb4-dd25eec1f34r"))
                    .withCorrelationid("35780d2f-1dca-4610-afb4-dd25eec1f34r"),
                com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_VirtualMachinesSubmitDeallocate

```java
import com.azure.resourcemanager.computeschedule.models.DeadlineType;
import com.azure.resourcemanager.computeschedule.models.ExecutionParameters;
import com.azure.resourcemanager.computeschedule.models.Resources;
import com.azure.resourcemanager.computeschedule.models.RetryPolicy;
import com.azure.resourcemanager.computeschedule.models.Schedule;
import com.azure.resourcemanager.computeschedule.models.SubmitDeallocateRequest;
import java.time.OffsetDateTime;
import java.util.Arrays;

/**
 * Samples for ScheduledActions VirtualMachinesSubmitDeallocate.
 */
public final class ScheduledActionsVirtualMachinesSubmitDeallocateSamples {
    /*
     * x-ms-original-file: 2024-10-01/ScheduledActions_VirtualMachinesSubmitDeallocate.json
     */
    /**
     * Sample code: ScheduledActions_VirtualMachinesSubmitDeallocate.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsVirtualMachinesSubmitDeallocate(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .virtualMachinesSubmitDeallocateWithResponse("eastus2euap", new SubmitDeallocateRequest()
                .withSchedule(new Schedule().withDeadline(OffsetDateTime.parse("2024-11-01T17:52:54.215Z"))
                    .withTimezone("UTC")
                    .withDeadlineType(DeadlineType.INITIATE_AT))
                .withExecutionParameters(new ExecutionParameters()
                    .withRetryPolicy(new RetryPolicy().withRetryCount(4).withRetryWindowInMinutes(27)))
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/YourSubscriptionId/resourceGroups/YourResourceGroupName/providers/Microsoft.Compute/virtualMachines/testResource3")))
                .withCorrelationid("23480d2f-1dca-4610-afb4-dd25eec1f34r"), com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_VirtualMachinesSubmitHibernate

```java
import com.azure.resourcemanager.computeschedule.models.DeadlineType;
import com.azure.resourcemanager.computeschedule.models.ExecutionParameters;
import com.azure.resourcemanager.computeschedule.models.Resources;
import com.azure.resourcemanager.computeschedule.models.RetryPolicy;
import com.azure.resourcemanager.computeschedule.models.Schedule;
import com.azure.resourcemanager.computeschedule.models.SubmitHibernateRequest;
import java.time.OffsetDateTime;
import java.util.Arrays;

/**
 * Samples for ScheduledActions VirtualMachinesSubmitHibernate.
 */
public final class ScheduledActionsVirtualMachinesSubmitHibernateSamples {
    /*
     * x-ms-original-file: 2024-10-01/ScheduledActions_VirtualMachinesSubmitHibernate.json
     */
    /**
     * Sample code: ScheduledActions_VirtualMachinesSubmitHibernate.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsVirtualMachinesSubmitHibernate(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .virtualMachinesSubmitHibernateWithResponse("eastus2euap", new SubmitHibernateRequest()
                .withSchedule(new Schedule().withDeadline(OffsetDateTime.parse("2024-11-01T17:52:54.215Z"))
                    .withTimezone("UTC")
                    .withDeadlineType(DeadlineType.INITIATE_AT))
                .withExecutionParameters(new ExecutionParameters()
                    .withRetryPolicy(new RetryPolicy().withRetryCount(2).withRetryWindowInMinutes(27)))
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/YourSubscriptionId/resourceGroups/YourResourceGroupName/providers/Microsoft.Compute/virtualMachines/testResource3")))
                .withCorrelationid("23480d2f-1dca-4610-afb4-dd25eec1f34r"), com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_VirtualMachinesSubmitStart

```java
import com.azure.resourcemanager.computeschedule.models.DeadlineType;
import com.azure.resourcemanager.computeschedule.models.ExecutionParameters;
import com.azure.resourcemanager.computeschedule.models.Resources;
import com.azure.resourcemanager.computeschedule.models.RetryPolicy;
import com.azure.resourcemanager.computeschedule.models.Schedule;
import com.azure.resourcemanager.computeschedule.models.SubmitStartRequest;
import java.time.OffsetDateTime;
import java.util.Arrays;

/**
 * Samples for ScheduledActions VirtualMachinesSubmitStart.
 */
public final class ScheduledActionsVirtualMachinesSubmitStartSamples {
    /*
     * x-ms-original-file: 2024-10-01/ScheduledActions_VirtualMachinesSubmitStart.json
     */
    /**
     * Sample code: ScheduledActions_VirtualMachinesSubmitStart.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsVirtualMachinesSubmitStart(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .virtualMachinesSubmitStartWithResponse("eastus2euap", new SubmitStartRequest()
                .withSchedule(new Schedule().withDeadline(OffsetDateTime.parse("2024-11-01T17:52:54.215Z"))
                    .withTimezone("UTC")
                    .withDeadlineType(DeadlineType.INITIATE_AT))
                .withExecutionParameters(new ExecutionParameters()
                    .withRetryPolicy(new RetryPolicy().withRetryCount(5).withRetryWindowInMinutes(27)))
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/YourSubscriptionId/resourceGroups/YourResourceGroupName/providers/Microsoft.Compute/virtualMachines/testResource3")))
                .withCorrelationid("23480d2f-1dca-4610-afb4-dd25eec1f34r"), com.azure.core.util.Context.NONE);
    }
}
```

