# Code snippets and samples


## OccurrenceExtension

- [ListOccurrenceByVms](#occurrenceextension_listoccurrencebyvms)

## Occurrences

- [Cancel](#occurrences_cancel)
- [Delay](#occurrences_delay)
- [Get](#occurrences_get)
- [ListByScheduledAction](#occurrences_listbyscheduledaction)
- [ListResources](#occurrences_listresources)

## Operations

- [List](#operations_list)

## ScheduledActionExtension

- [ListByVms](#scheduledactionextension_listbyvms)

## ScheduledActions

- [AttachResources](#scheduledactions_attachresources)
- [CancelNextOccurrence](#scheduledactions_cancelnextoccurrence)
- [CreateOrUpdate](#scheduledactions_createorupdate)
- [Delete](#scheduledactions_delete)
- [DetachResources](#scheduledactions_detachresources)
- [Disable](#scheduledactions_disable)
- [Enable](#scheduledactions_enable)
- [GetByResourceGroup](#scheduledactions_getbyresourcegroup)
- [List](#scheduledactions_list)
- [ListByResourceGroup](#scheduledactions_listbyresourcegroup)
- [ListResources](#scheduledactions_listresources)
- [PatchResources](#scheduledactions_patchresources)
- [TriggerManualOccurrence](#scheduledactions_triggermanualoccurrence)
- [Update](#scheduledactions_update)
- [VirtualMachinesCancelOperations](#scheduledactions_virtualmachinescanceloperations)
- [VirtualMachinesExecuteCreate](#scheduledactions_virtualmachinesexecutecreate)
- [VirtualMachinesExecuteDeallocate](#scheduledactions_virtualmachinesexecutedeallocate)
- [VirtualMachinesExecuteDelete](#scheduledactions_virtualmachinesexecutedelete)
- [VirtualMachinesExecuteHibernate](#scheduledactions_virtualmachinesexecutehibernate)
- [VirtualMachinesExecuteStart](#scheduledactions_virtualmachinesexecutestart)
- [VirtualMachinesGetOperationErrors](#scheduledactions_virtualmachinesgetoperationerrors)
- [VirtualMachinesGetOperationStatus](#scheduledactions_virtualmachinesgetoperationstatus)
- [VirtualMachinesSubmitDeallocate](#scheduledactions_virtualmachinessubmitdeallocate)
- [VirtualMachinesSubmitHibernate](#scheduledactions_virtualmachinessubmithibernate)
- [VirtualMachinesSubmitStart](#scheduledactions_virtualmachinessubmitstart)
### OccurrenceExtension_ListOccurrenceByVms

```java
/**
 * Samples for OccurrenceExtension ListOccurrenceByVms.
 */
public final class OccurrenceExtensionListOccurrenceByVmsSamples {
    /*
     * x-ms-original-file: 2025-04-15-preview/OccurrenceExtension_ListOccurrenceByVms_MaximumSet_Gen.json
     */
    /**
     * Sample code: OccurrenceExtension_ListOccurrenceByVms_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void occurrenceExtensionListOccurrenceByVmsMaximumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.occurrenceExtensions().listOccurrenceByVms("sazvpabfud", com.azure.core.util.Context.NONE);
    }
}
```

### Occurrences_Cancel

```java
import com.azure.resourcemanager.computeschedule.models.CancelOccurrenceRequest;
import java.util.Arrays;

/**
 * Samples for Occurrences Cancel.
 */
public final class OccurrencesCancelSamples {
    /*
     * x-ms-original-file: 2025-04-15-preview/Occurrences_Cancel_MaximumSet_Gen.json
     */
    /**
     * Sample code: Occurrences_Cancel_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void
        occurrencesCancelMaximumSet(com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.occurrences()
            .cancelWithResponse("rgcomputeschedule", "myScheduledAction", "CB26D7CB-3E27-465F-99C8-EAF7A4118245",
                new CancelOccurrenceRequest().withResourceIds(Arrays.asList(
                    "/subscriptions/CB26D7CB-3E27-465F-99C8-EAF7A4118245/resourceGroups/myRg/providers/Microsoft.Compute/virtualMachines/myVm")),
                com.azure.core.util.Context.NONE);
    }
}
```

### Occurrences_Delay

```java
import com.azure.resourcemanager.computeschedule.models.DelayRequest;
import java.time.OffsetDateTime;
import java.util.Arrays;

/**
 * Samples for Occurrences Delay.
 */
public final class OccurrencesDelaySamples {
    /*
     * x-ms-original-file: 2025-04-15-preview/Occurrences_Delay_MaximumSet_Gen.json
     */
    /**
     * Sample code: Occurrences_Delay_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void
        occurrencesDelayMaximumSet(com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.occurrences()
            .delay("rgcomputeschedule", "myScheduledAction", "CB26D7CB-3E27-465F-99C8-EAF7A4118245", new DelayRequest()
                .withDelay(OffsetDateTime.parse("2025-05-22T17:00:00.000-07:00"))
                .withResourceIds(Arrays.asList(
                    "/subscriptions/CB26D7CB-3E27-465F-99C8-EAF7A4118245/resourceGroups/myRg/providers/Microsoft.Compute/virtualMachines/myVm")),
                com.azure.core.util.Context.NONE);
    }
}
```

### Occurrences_Get

```java
/**
 * Samples for Occurrences Get.
 */
public final class OccurrencesGetSamples {
    /*
     * x-ms-original-file: 2025-04-15-preview/Occurrences_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Occurrences_Get_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void
        occurrencesGetMaximumSet(com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.occurrences()
            .getWithResponse("rgcomputeschedule", "myScheduledAction", "67b5bada-4772-43fc-8dbb-402476d98a45",
                com.azure.core.util.Context.NONE);
    }
}
```

### Occurrences_ListByScheduledAction

```java
/**
 * Samples for Occurrences ListByScheduledAction.
 */
public final class OccurrencesListByScheduledActionSamples {
    /*
     * x-ms-original-file: 2025-04-15-preview/Occurrences_ListByScheduledAction_MaximumSet_Gen.json
     */
    /**
     * Sample code: Occurrences_ListByScheduledAction_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void occurrencesListByScheduledActionMaximumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.occurrences()
            .listByScheduledAction("rgcomputeschedule", "myScheduledAction", com.azure.core.util.Context.NONE);
    }
}
```

### Occurrences_ListResources

```java
/**
 * Samples for Occurrences ListResources.
 */
public final class OccurrencesListResourcesSamples {
    /*
     * x-ms-original-file: 2025-04-15-preview/Occurrences_ListResources_MaximumSet_Gen.json
     */
    /**
     * Sample code: Occurrences_ListResources_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void
        occurrencesListResourcesMaximumSet(com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.occurrences()
            .listResources("rgcomputeschedule", "myScheduledAction", "CB26D7CB-3E27-465F-99C8-EAF7A4118245",
                com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2025-04-15-preview/Operations_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_MinimumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void
        operationsListMinimumSet(com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-04-15-preview/Operations_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void
        operationsListMaximumSet(com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActionExtension_ListByVms

```java
/**
 * Samples for ScheduledActionExtension ListByVms.
 */
public final class ScheduledActionExtensionListByVmsSamples {
    /*
     * x-ms-original-file: 2025-04-15-preview/ScheduledActionExtension_ListByVms_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActionExtension_ListByVms_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionExtensionListByVmsMaximumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActionExtensions().listByVms("sazvpabfud", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-04-15-preview/ScheduledActionExtension_ListByVms_MinimumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActionExtension_ListByVms_MinimumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionExtensionListByVmsMinimumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActionExtensions().listByVms("sazvpabfud", com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_AttachResources

```java
import com.azure.resourcemanager.computeschedule.fluent.models.ScheduledActionResourceInner;
import com.azure.resourcemanager.computeschedule.models.Language;
import com.azure.resourcemanager.computeschedule.models.NotificationProperties;
import com.azure.resourcemanager.computeschedule.models.NotificationType;
import com.azure.resourcemanager.computeschedule.models.ResourceAttachRequest;
import java.util.Arrays;

/**
 * Samples for ScheduledActions AttachResources.
 */
public final class ScheduledActionsAttachResourcesSamples {
    /*
     * x-ms-original-file: 2025-04-15-preview/ScheduledActions_AttachResources_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_AttachResources_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsAttachResourcesMaximumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .attachResourcesWithResponse("rgcomputeschedule", "myScheduledAction", new ResourceAttachRequest()
                .withResources(Arrays.asList(new ScheduledActionResourceInner().withResourceId(
                    "/subscriptions/1d04e8f1-ee04-4056-b0b2-718f5bb45b04/resourceGroups/myRg/providers/Microsoft.Compute/virtualMachines/myVm")
                    .withNotificationSettings(
                        Arrays.asList(new NotificationProperties().withDestination("wbhryycyolvnypjxzlawwvb")
                            .withType(NotificationType.EMAIL)
                            .withLanguage(Language.EN_US)
                            .withDisabled(true))))),
                com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_CancelNextOccurrence

```java
import com.azure.resourcemanager.computeschedule.models.CancelOccurrenceRequest;
import java.util.Arrays;

/**
 * Samples for ScheduledActions CancelNextOccurrence.
 */
public final class ScheduledActionsCancelNextOccurrenceSamples {
    /*
     * x-ms-original-file: 2025-04-15-preview/ScheduledActions_CancelNextOccurrence_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_CancelNextOccurrence_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsCancelNextOccurrenceMaximumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .cancelNextOccurrenceWithResponse("rgcomputeschedule", "myScheduledAction",
                new CancelOccurrenceRequest().withResourceIds(Arrays.asList(
                    "/subscriptions/1d04e8f1-ee04-4056-b0b2-718f5bb45b04/resourceGroups/myRg/providers/Microsoft.Compute/virtualMachines/myVm")),
                com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_CreateOrUpdate

```java
import com.azure.resourcemanager.computeschedule.models.DeadlineType;
import com.azure.resourcemanager.computeschedule.models.ExecutionParameters;
import com.azure.resourcemanager.computeschedule.models.Language;
import com.azure.resourcemanager.computeschedule.models.Month;
import com.azure.resourcemanager.computeschedule.models.NotificationProperties;
import com.azure.resourcemanager.computeschedule.models.NotificationType;
import com.azure.resourcemanager.computeschedule.models.OptimizationPreference;
import com.azure.resourcemanager.computeschedule.models.ResourceType;
import com.azure.resourcemanager.computeschedule.models.RetryPolicy;
import com.azure.resourcemanager.computeschedule.models.ScheduledActionProperties;
import com.azure.resourcemanager.computeschedule.models.ScheduledActionType;
import com.azure.resourcemanager.computeschedule.models.ScheduledActionsSchedule;
import com.azure.resourcemanager.computeschedule.models.WeekDay;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ScheduledActions CreateOrUpdate.
 */
public final class ScheduledActionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-04-15-preview/ScheduledActions_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsCreateOrUpdateMaximumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .define("myScheduledAction")
            .withRegion("vmuhgdgipeypkcv")
            .withExistingResourceGroup("rgcomputeschedule")
            .withTags(mapOf("key2102", "fakeTokenPlaceholder"))
            .withProperties(new ScheduledActionProperties().withResourceType(ResourceType.VIRTUAL_MACHINE)
                .withActionType(ScheduledActionType.START)
                .withStartTime(OffsetDateTime.parse("2025-04-17T00:23:55.281Z"))
                .withEndTime(OffsetDateTime.parse("2025-04-17T00:23:55.286Z"))
                .withSchedule(new ScheduledActionsSchedule().withScheduledTime("19:00:00")
                    .withTimeZone("g")
                    .withRequestedWeekDays(Arrays.asList(WeekDay.MONDAY))
                    .withRequestedMonths(Arrays.asList(Month.JANUARY))
                    .withRequestedDaysOfTheMonth(Arrays.asList(15))
                    .withExecutionParameters(
                        new ExecutionParameters().withOptimizationPreference(OptimizationPreference.COST)
                            .withRetryPolicy(new RetryPolicy().withRetryCount(17).withRetryWindowInMinutes(29)))
                    .withDeadlineType(DeadlineType.UNKNOWN))
                .withNotificationSettings(
                    Arrays.asList(new NotificationProperties().withDestination("wbhryycyolvnypjxzlawwvb")
                        .withType(NotificationType.EMAIL)
                        .withLanguage(Language.EN_US)
                        .withDisabled(true)))
                .withDisabled(true))
            .create();
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

### ScheduledActions_Delete

```java
/**
 * Samples for ScheduledActions Delete.
 */
public final class ScheduledActionsDeleteSamples {
    /*
     * x-ms-original-file: 2025-04-15-preview/ScheduledActions_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_Delete_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void
        scheduledActionsDeleteMaximumSet(com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions().delete("rgcomputeschedule", "myScheduledAction", com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_DetachResources

```java
import com.azure.resourcemanager.computeschedule.models.ResourceDetachRequest;
import java.util.Arrays;

/**
 * Samples for ScheduledActions DetachResources.
 */
public final class ScheduledActionsDetachResourcesSamples {
    /*
     * x-ms-original-file: 2025-04-15-preview/ScheduledActions_DetachResources_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_DetachResources_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsDetachResourcesMaximumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .detachResourcesWithResponse("rgcomputeschedule", "myScheduledAction",
                new ResourceDetachRequest().withResources(Arrays.asList(
                    "/subscriptions/1d04e8f1-ee04-4056-b0b2-718f5bb45b04/resourceGroups/myRg/providers/Microsoft.Compute/virtualMachines/myVm")),
                com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_Disable

```java
/**
 * Samples for ScheduledActions Disable.
 */
public final class ScheduledActionsDisableSamples {
    /*
     * x-ms-original-file: 2025-04-15-preview/ScheduledActions_Disable_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_Disable_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void
        scheduledActionsDisableMaximumSet(com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .disableWithResponse("rgcomputeschedule", "myScheduledAction", com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_Enable

```java
/**
 * Samples for ScheduledActions Enable.
 */
public final class ScheduledActionsEnableSamples {
    /*
     * x-ms-original-file: 2025-04-15-preview/ScheduledActions_Enable_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_Enable_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void
        scheduledActionsEnableMaximumSet(com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .enableWithResponse("rgcomputeschedule", "myScheduledAction", com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_GetByResourceGroup

```java
/**
 * Samples for ScheduledActions GetByResourceGroup.
 */
public final class ScheduledActionsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-04-15-preview/ScheduledActions_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_Get_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void
        scheduledActionsGetMaximumSet(com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .getByResourceGroupWithResponse("rgcomputeschedule", "myScheduledAction", com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_List

```java
/**
 * Samples for ScheduledActions List.
 */
public final class ScheduledActionsListSamples {
    /*
     * x-ms-original-file: 2025-04-15-preview/ScheduledActions_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_ListBySubscription_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsListBySubscriptionMaximumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-04-15-preview/ScheduledActions_ListBySubscription_MinimumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_ListBySubscription_MinimumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsListBySubscriptionMinimumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions().list(com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_ListByResourceGroup

```java
/**
 * Samples for ScheduledActions ListByResourceGroup.
 */
public final class ScheduledActionsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-04-15-preview/ScheduledActions_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_ListByResourceGroup_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsListByResourceGroupMaximumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions().listByResourceGroup("rgcomputeschedule", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-04-15-preview/ScheduledActions_ListByResourceGroup_MinimumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_ListByResourceGroup_MinimumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsListByResourceGroupMinimumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions().listByResourceGroup("rgcomputeschedule", com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_ListResources

```java
/**
 * Samples for ScheduledActions ListResources.
 */
public final class ScheduledActionsListResourcesSamples {
    /*
     * x-ms-original-file: 2025-04-15-preview/ScheduledActions_ListResources_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_ListResources_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsListResourcesMaximumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .listResources("rgcomputeschedule", "myScheduledAction", com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_PatchResources

```java
import com.azure.resourcemanager.computeschedule.fluent.models.ScheduledActionResourceInner;
import com.azure.resourcemanager.computeschedule.models.Language;
import com.azure.resourcemanager.computeschedule.models.NotificationProperties;
import com.azure.resourcemanager.computeschedule.models.NotificationType;
import com.azure.resourcemanager.computeschedule.models.ResourcePatchRequest;
import java.util.Arrays;

/**
 * Samples for ScheduledActions PatchResources.
 */
public final class ScheduledActionsPatchResourcesSamples {
    /*
     * x-ms-original-file: 2025-04-15-preview/ScheduledActions_PatchResources_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_PatchResources_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsPatchResourcesMaximumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .patchResourcesWithResponse("rgcomputeschedule", "myScheduledAction", new ResourcePatchRequest()
                .withResources(Arrays.asList(new ScheduledActionResourceInner().withResourceId(
                    "/subscriptions/1d04e8f1-ee04-4056-b0b2-718f5bb45b04/resourceGroups/myRg/providers/Microsoft.Compute/virtualMachines/myVm")
                    .withNotificationSettings(
                        Arrays.asList(new NotificationProperties().withDestination("wbhryycyolvnypjxzlawwvb")
                            .withType(NotificationType.EMAIL)
                            .withLanguage(Language.EN_US)
                            .withDisabled(true))))),
                com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_TriggerManualOccurrence

```java
/**
 * Samples for ScheduledActions TriggerManualOccurrence.
 */
public final class ScheduledActionsTriggerManualOccurrenceSamples {
    /*
     * x-ms-original-file: 2025-04-15-preview/ScheduledActions_TriggerManualOccurrence_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_TriggerManualOccurrence_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsTriggerManualOccurrenceMaximumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .triggerManualOccurrenceWithResponse("rgcomputeschedule", "myScheduledAction",
                com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_Update

```java
import com.azure.resourcemanager.computeschedule.models.DeadlineType;
import com.azure.resourcemanager.computeschedule.models.ExecutionParameters;
import com.azure.resourcemanager.computeschedule.models.Language;
import com.azure.resourcemanager.computeschedule.models.Month;
import com.azure.resourcemanager.computeschedule.models.NotificationProperties;
import com.azure.resourcemanager.computeschedule.models.NotificationType;
import com.azure.resourcemanager.computeschedule.models.OptimizationPreference;
import com.azure.resourcemanager.computeschedule.models.ResourceType;
import com.azure.resourcemanager.computeschedule.models.RetryPolicy;
import com.azure.resourcemanager.computeschedule.models.ScheduledAction;
import com.azure.resourcemanager.computeschedule.models.ScheduledActionType;
import com.azure.resourcemanager.computeschedule.models.ScheduledActionUpdateProperties;
import com.azure.resourcemanager.computeschedule.models.ScheduledActionsSchedule;
import com.azure.resourcemanager.computeschedule.models.WeekDay;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ScheduledActions Update.
 */
public final class ScheduledActionsUpdateSamples {
    /*
     * x-ms-original-file: 2025-04-15-preview/ScheduledActions_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_Update_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void
        scheduledActionsUpdateMaximumSet(com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        ScheduledAction resource = manager.scheduledActions()
            .getByResourceGroupWithResponse("rgcomputeschedule", "myScheduledAction", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key9989", "fakeTokenPlaceholder"))
            .withProperties(new ScheduledActionUpdateProperties().withResourceType(ResourceType.VIRTUAL_MACHINE)
                .withActionType(ScheduledActionType.START)
                .withStartTime(OffsetDateTime.parse("2025-04-17T00:23:58.149Z"))
                .withEndTime(OffsetDateTime.parse("2025-04-17T00:23:58.149Z"))
                .withSchedule(new ScheduledActionsSchedule().withScheduledTime("19:00:00")
                    .withTimeZone("bni")
                    .withRequestedWeekDays(Arrays.asList(WeekDay.MONDAY))
                    .withRequestedMonths(Arrays.asList(Month.JANUARY))
                    .withRequestedDaysOfTheMonth(Arrays.asList(15))
                    .withExecutionParameters(
                        new ExecutionParameters().withOptimizationPreference(OptimizationPreference.COST)
                            .withRetryPolicy(new RetryPolicy().withRetryCount(17).withRetryWindowInMinutes(29)))
                    .withDeadlineType(DeadlineType.UNKNOWN))
                .withNotificationSettings(
                    Arrays.asList(new NotificationProperties().withDestination("wbhryycyolvnypjxzlawwvb")
                        .withType(NotificationType.EMAIL)
                        .withLanguage(Language.EN_US)
                        .withDisabled(true)))
                .withDisabled(true))
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

### ScheduledActions_VirtualMachinesCancelOperations

```java
import com.azure.resourcemanager.computeschedule.models.CancelOperationsRequest;
import java.util.Arrays;

/**
 * Samples for ScheduledActions VirtualMachinesCancelOperations.
 */
public final class ScheduledActionsVirtualMachinesCancelOperationsSamples {
    /*
     * x-ms-original-file: 2025-04-15-preview/ScheduledActions_VirtualMachinesCancelOperations_MinimumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_VirtualMachinesCancelOperations_MinimumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsVirtualMachinesCancelOperationsMinimumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .virtualMachinesCancelOperationsWithResponse("fhdunfivmjiltaiakwhhwdgemfcld",
                new CancelOperationsRequest().withOperationIds(Arrays.asList("b211f086-4b91-4686-a453-2f5c012e4d80"))
                    .withCorrelationid("b211f086-4b91-4686-a453-2f5c012e4d80"),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-04-15-preview/ScheduledActions_VirtualMachinesCancelOperations_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_VirtualMachinesCancelOperations_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsVirtualMachinesCancelOperationsMaximumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .virtualMachinesCancelOperationsWithResponse("nivsvluajruxhmsfgmxjnl",
                new CancelOperationsRequest().withOperationIds(Arrays.asList("b211f086-4b91-4686-a453-2f5c012e4d80"))
                    .withCorrelationid("b211f086-4b91-4686-a453-2f5c012e4d80"),
                com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_VirtualMachinesExecuteCreate

```java
import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.computeschedule.models.ExecuteCreateRequest;
import com.azure.resourcemanager.computeschedule.models.ExecutionParameters;
import com.azure.resourcemanager.computeschedule.models.ResourceProvisionPayload;
import com.azure.resourcemanager.computeschedule.models.RetryPolicy;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ScheduledActions VirtualMachinesExecuteCreate.
 */
public final class ScheduledActionsVirtualMachinesExecuteCreateSamples {
    /*
     * x-ms-original-file: 2025-04-15-preview/ScheduledActions_VirtualMachinesExecuteCreate_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_VirtualMachinesExecuteCreate_MaximumSet_Gen - generated by [MaximumSet] rule.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsVirtualMachinesExecuteCreateMaximumSetGenGeneratedByMaximumSetRule(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .virtualMachinesExecuteCreateWithResponse("oslhbouzgevzpeydssyelhw", new ExecuteCreateRequest()
                .withResourceConfigParameters(new ResourceProvisionPayload().withBaseProfile(mapOf("hardwareProfile",
                    BinaryData.fromBytes("{name=F1}".getBytes(StandardCharsets.UTF_8)), "provisioningState",
                    BinaryData.fromBytes("0".getBytes(StandardCharsets.UTF_8)), "storageProfile",
                    BinaryData.fromBytes("{osDisk={osType=0}}".getBytes(StandardCharsets.UTF_8)), "vmExtensions",
                    BinaryData.fromBytes(
                        "[{autoUpgradeMinorVersion=true, protectedSettings=SomeDecryptedSecretValue, provisioningState=0, enableAutomaticUpgrade=true, publisher=Microsoft.Azure.Monitor, type=AzureMonitorLinuxAgent, typeHandlerVersion=1.0}, {name=myExtensionName}]"
                            .getBytes(StandardCharsets.UTF_8)),
                    "resourcegroupName",
                    BinaryData.fromBytes("RG5ABF491C-3164-42A6-8CB5-BF3CB53B018B".getBytes(StandardCharsets.UTF_8)),
                    "computeApiVersion", BinaryData.fromBytes("2024-07-01".getBytes(StandardCharsets.UTF_8))))
                    .withResourceOverrides(Arrays.asList(mapOf("name",
                        BinaryData.fromBytes("myFleet_523".getBytes(StandardCharsets.UTF_8)), "location",
                        BinaryData.fromBytes("LocalDev".getBytes(StandardCharsets.UTF_8)), "properties",
                        BinaryData.fromBytes(
                            "{hardwareProfile={vmSize=Standard_F1s}, provisioningState=0, osProfile={computerName=myFleet000000, adminUsername=adminUser, windowsConfiguration={additionalUnattendContent=[{passName=someValue, content=}, {passName=someOtherValue, content=SomeDecryptedSecretValue}]}, adminPassword=SomeDecryptedSecretValue}, priority=0}"
                                .getBytes(StandardCharsets.UTF_8)),
                        "zones", BinaryData.fromBytes("[1]".getBytes(StandardCharsets.UTF_8))),
                        mapOf("name", BinaryData.fromBytes("myFleet_524".getBytes(StandardCharsets.UTF_8)), "location",
                            BinaryData.fromBytes("LocalDev".getBytes(StandardCharsets.UTF_8)), "properties",
                            BinaryData.fromBytes(
                                "{hardwareProfile={vmSize=Standard_G1s}, provisioningState=0, osProfile={computerName=myFleet000000, adminUsername=adminUser, windowsConfiguration={additionalUnattendContent=[{passName=someValue, content=}, {passName=someOtherValue, content=SomeDecryptedSecretValue}]}, adminPassword=SomeDecryptedSecretValue}, priority=0}"
                                    .getBytes(StandardCharsets.UTF_8)),
                            "zones", BinaryData.fromBytes("[2]".getBytes(StandardCharsets.UTF_8)))))
                    .withResourceCount(2)
                    .withResourcePrefix("TL1"))
                .withExecutionParameters(new ExecutionParameters()
                    .withRetryPolicy(new RetryPolicy().withRetryCount(5).withRetryWindowInMinutes(40)))
                .withCorrelationid("dfe927c5-16a6-40b7-a0f7-8524975ed642"), com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-04-15-preview/ScheduledActions_VirtualMachinesExecuteCreate_MinimumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_VirtualMachinesExecuteCreate_MinimumSet_Gen - generated by [MinimumSet] rule.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsVirtualMachinesExecuteCreateMinimumSetGenGeneratedByMinimumSetRule(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .virtualMachinesExecuteCreateWithResponse("useast", new ExecuteCreateRequest()
                .withResourceConfigParameters(new ResourceProvisionPayload().withBaseProfile(mapOf("hardwareProfile",
                    BinaryData.fromBytes("{name=F1}".getBytes(StandardCharsets.UTF_8)), "provisioningState",
                    BinaryData.fromBytes("0".getBytes(StandardCharsets.UTF_8)), "storageProfile",
                    BinaryData.fromBytes("{osDisk={osType=0}}".getBytes(StandardCharsets.UTF_8)), "vmExtensions",
                    BinaryData.fromBytes(
                        "[{autoUpgradeMinorVersion=true, protectedSettings=SomeDecryptedSecretValue, provisioningState=0, enableAutomaticUpgrade=true, publisher=Microsoft.Azure.Monitor, type=AzureMonitorLinuxAgent, typeHandlerVersion=1.0}, {name=myExtensionName}]"
                            .getBytes(StandardCharsets.UTF_8)),
                    "resourcegroupName",
                    BinaryData.fromBytes("RG5ABF491C-3164-42A6-8CB5-BF3CB53B018B".getBytes(StandardCharsets.UTF_8)),
                    "computeApiVersion", BinaryData.fromBytes("2024-07-01".getBytes(StandardCharsets.UTF_8))))
                    .withResourceOverrides(Arrays.asList(mapOf("name",
                        BinaryData.fromBytes("myFleet_523".getBytes(StandardCharsets.UTF_8)), "location",
                        BinaryData.fromBytes("LocalDev".getBytes(StandardCharsets.UTF_8)), "properties",
                        BinaryData.fromBytes(
                            "{hardwareProfile={vmSize=Standard_F1s}, provisioningState=0, osProfile={computerName=myFleet000000, adminUsername=adminUser, windowsConfiguration={additionalUnattendContent=[{passName=someValue, content=}, {passName=someOtherValue, content=SomeDecryptedSecretValue}]}, adminPassword=SomeDecryptedSecretValue}, priority=0}"
                                .getBytes(StandardCharsets.UTF_8)),
                        "zones", BinaryData.fromBytes("[1]".getBytes(StandardCharsets.UTF_8))),
                        mapOf("name", BinaryData.fromBytes("myFleet_524".getBytes(StandardCharsets.UTF_8)), "location",
                            BinaryData.fromBytes("LocalDev".getBytes(StandardCharsets.UTF_8)), "properties",
                            BinaryData.fromBytes(
                                "{hardwareProfile={vmSize=Standard_G1s}, provisioningState=0, osProfile={computerName=myFleet000000, adminUsername=adminUser, windowsConfiguration={additionalUnattendContent=[{passName=someValue, content=}, {passName=someOtherValue, content=SomeDecryptedSecretValue}]}, adminPassword=SomeDecryptedSecretValue}, priority=0}"
                                    .getBytes(StandardCharsets.UTF_8)),
                            "zones", BinaryData.fromBytes("[2]".getBytes(StandardCharsets.UTF_8)))))
                    .withResourceCount(2))
                .withExecutionParameters(new ExecutionParameters()), com.azure.core.util.Context.NONE);
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

### ScheduledActions_VirtualMachinesExecuteDeallocate

```java
import com.azure.resourcemanager.computeschedule.models.ExecuteDeallocateRequest;
import com.azure.resourcemanager.computeschedule.models.ExecutionParameters;
import com.azure.resourcemanager.computeschedule.models.OptimizationPreference;
import com.azure.resourcemanager.computeschedule.models.Resources;
import com.azure.resourcemanager.computeschedule.models.RetryPolicy;
import java.util.Arrays;

/**
 * Samples for ScheduledActions VirtualMachinesExecuteDeallocate.
 */
public final class ScheduledActionsVirtualMachinesExecuteDeallocateSamples {
    /*
     * x-ms-original-file: 2025-04-15-preview/ScheduledActions_VirtualMachinesExecuteDeallocate_MinimumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_VirtualMachinesExecuteDeallocate_MinimumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsVirtualMachinesExecuteDeallocateMinimumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .virtualMachinesExecuteDeallocateWithResponse("qqfrkswrovcice", new ExecuteDeallocateRequest()
                .withExecutionParameters(new ExecutionParameters())
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/YourSubscriptionId/resourceGroups/YourResourceGroupName/providers/Microsoft.Compute/virtualMachines/testResource4")))
                .withCorrelationid("b211f086-4b91-4686-a453-2f5c012e4d80"), com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-04-15-preview/ScheduledActions_VirtualMachinesExecuteDeallocate_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_VirtualMachinesExecuteDeallocate_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsVirtualMachinesExecuteDeallocateMaximumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .virtualMachinesExecuteDeallocateWithResponse("ykcaptgboliddcfyaiuimj", new ExecuteDeallocateRequest()
                .withExecutionParameters(
                    new ExecutionParameters().withOptimizationPreference(OptimizationPreference.COST)
                        .withRetryPolicy(new RetryPolicy().withRetryCount(17).withRetryWindowInMinutes(29)))
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/YourSubscriptionId/resourceGroups/YourResourceGroupName/providers/Microsoft.Compute/virtualMachines/testResource4")))
                .withCorrelationid("b211f086-4b91-4686-a453-2f5c012e4d80"), com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_VirtualMachinesExecuteDelete

```java
import com.azure.resourcemanager.computeschedule.models.ExecuteDeleteRequest;
import com.azure.resourcemanager.computeschedule.models.ExecutionParameters;
import com.azure.resourcemanager.computeschedule.models.Resources;
import com.azure.resourcemanager.computeschedule.models.RetryPolicy;
import java.util.Arrays;

/**
 * Samples for ScheduledActions VirtualMachinesExecuteDelete.
 */
public final class ScheduledActionsVirtualMachinesExecuteDeleteSamples {
    /*
     * x-ms-original-file: 2025-04-15-preview/ScheduledActions_VirtualMachinesExecuteDelete_MinimumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_VirtualMachinesExecuteDelete_MinimumSet_Gen - generated by [MinimumSet] rule.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsVirtualMachinesExecuteDeleteMinimumSetGenGeneratedByMinimumSetRule(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .virtualMachinesExecuteDeleteWithResponse("east", new ExecuteDeleteRequest()
                .withExecutionParameters(new ExecutionParameters())
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/YourSubscriptionId/resourceGroups/YourResourceGroupName/providers/Microsoft.Compute/virtualMachines/testResource3",
                    "/subscriptions/YourSubscriptionId/resourceGroups/YourResourceGroupName/providers/Microsoft.Compute/virtualMachines/testResource4"))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-04-15-preview/ScheduledActions_VirtualMachinesExecuteDelete_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_VirtualMachinesExecuteDelete_MaximumSet_Gen - generated by [MaximumSet] rule.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsVirtualMachinesExecuteDeleteMaximumSetGenGeneratedByMaximumSetRule(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .virtualMachinesExecuteDeleteWithResponse("east", new ExecuteDeleteRequest()
                .withExecutionParameters(new ExecutionParameters()
                    .withRetryPolicy(new RetryPolicy().withRetryCount(2).withRetryWindowInMinutes(4)))
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/YourSubscriptionId/resourceGroups/YourResourceGroupName/providers/Microsoft.Compute/virtualMachines/testResource3",
                    "/subscriptions/YourSubscriptionId/resourceGroups/YourResourceGroupName/providers/Microsoft.Compute/virtualMachines/testResource4")))
                .withCorrelationid("dfe927c5-16a6-40b7-a0f7-8524975ed642")
                .withForceDeletion(false), com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_VirtualMachinesExecuteHibernate

```java
import com.azure.resourcemanager.computeschedule.models.ExecuteHibernateRequest;
import com.azure.resourcemanager.computeschedule.models.ExecutionParameters;
import com.azure.resourcemanager.computeschedule.models.OptimizationPreference;
import com.azure.resourcemanager.computeschedule.models.Resources;
import com.azure.resourcemanager.computeschedule.models.RetryPolicy;
import java.util.Arrays;

/**
 * Samples for ScheduledActions VirtualMachinesExecuteHibernate.
 */
public final class ScheduledActionsVirtualMachinesExecuteHibernateSamples {
    /*
     * x-ms-original-file: 2025-04-15-preview/ScheduledActions_VirtualMachinesExecuteHibernate_MinimumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_VirtualMachinesExecuteHibernate_MinimumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsVirtualMachinesExecuteHibernateMinimumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .virtualMachinesExecuteHibernateWithResponse("xtmm", new ExecuteHibernateRequest()
                .withExecutionParameters(new ExecutionParameters())
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/YourSubscriptionId/resourceGroups/YourResourceGroupName/providers/Microsoft.Compute/virtualMachines/testResource4")))
                .withCorrelationid("b211f086-4b91-4686-a453-2f5c012e4d80"), com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-04-15-preview/ScheduledActions_VirtualMachinesExecuteHibernate_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_VirtualMachinesExecuteHibernate_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsVirtualMachinesExecuteHibernateMaximumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .virtualMachinesExecuteHibernateWithResponse("gztd", new ExecuteHibernateRequest()
                .withExecutionParameters(
                    new ExecutionParameters().withOptimizationPreference(OptimizationPreference.COST)
                        .withRetryPolicy(new RetryPolicy().withRetryCount(17).withRetryWindowInMinutes(29)))
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/YourSubscriptionId/resourceGroups/YourResourceGroupName/providers/Microsoft.Compute/virtualMachines/testResource4")))
                .withCorrelationid("b211f086-4b91-4686-a453-2f5c012e4d80"), com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_VirtualMachinesExecuteStart

```java
import com.azure.resourcemanager.computeschedule.models.ExecuteStartRequest;
import com.azure.resourcemanager.computeschedule.models.ExecutionParameters;
import com.azure.resourcemanager.computeschedule.models.OptimizationPreference;
import com.azure.resourcemanager.computeschedule.models.Resources;
import com.azure.resourcemanager.computeschedule.models.RetryPolicy;
import java.util.Arrays;

/**
 * Samples for ScheduledActions VirtualMachinesExecuteStart.
 */
public final class ScheduledActionsVirtualMachinesExecuteStartSamples {
    /*
     * x-ms-original-file: 2025-04-15-preview/ScheduledActions_VirtualMachinesExecuteStart_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_VirtualMachinesExecuteStart_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsVirtualMachinesExecuteStartMaximumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .virtualMachinesExecuteStartWithResponse("qk", new ExecuteStartRequest()
                .withExecutionParameters(
                    new ExecutionParameters().withOptimizationPreference(OptimizationPreference.COST)
                        .withRetryPolicy(new RetryPolicy().withRetryCount(17).withRetryWindowInMinutes(29)))
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/YourSubscriptionId/resourceGroups/YourResourceGroupName/providers/Microsoft.Compute/virtualMachines/testResource4")))
                .withCorrelationid("b211f086-4b91-4686-a453-2f5c012e4d80"), com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-04-15-preview/ScheduledActions_VirtualMachinesExecuteStart_MinimumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_VirtualMachinesExecuteStart_MinimumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsVirtualMachinesExecuteStartMinimumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .virtualMachinesExecuteStartWithResponse("fbdewllahrteoavajbomjc", new ExecuteStartRequest()
                .withExecutionParameters(new ExecutionParameters())
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/YourSubscriptionId/resourceGroups/YourResourceGroupName/providers/Microsoft.Compute/virtualMachines/testResource4")))
                .withCorrelationid("b211f086-4b91-4686-a453-2f5c012e4d80"), com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2025-04-15-preview/ScheduledActions_VirtualMachinesGetOperationErrors_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_VirtualMachinesGetOperationErrors_MaximumSet_Gen - generated by [MaximumSet] rule.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsVirtualMachinesGetOperationErrorsMaximumSetGenGeneratedByMaximumSetRule(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .virtualMachinesGetOperationErrorsWithResponse("ennweqswbghorrgzbet",
                new GetOperationErrorsRequest().withOperationIds(Arrays.asList("ksufjznokhsbowdupyt")),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-04-15-preview/ScheduledActions_VirtualMachinesGetOperationErrors_MinimumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_VirtualMachinesGetOperationErrors_MinimumSet_Gen - generated by [MinimumSet] rule.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsVirtualMachinesGetOperationErrorsMinimumSetGenGeneratedByMinimumSetRule(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .virtualMachinesGetOperationErrorsWithResponse("gcdqwzmxtcn",
                new GetOperationErrorsRequest().withOperationIds(Arrays.asList("ksufjznokhsbowdupyt")),
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
     * x-ms-original-file: 2025-04-15-preview/ScheduledActions_VirtualMachinesGetOperationStatus_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_VirtualMachinesGetOperationStatus_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsVirtualMachinesGetOperationStatusMaximumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .virtualMachinesGetOperationStatusWithResponse("ntfcikxsmthfkdhdcjpevmydzu",
                new GetOperationStatusRequest().withOperationIds(Arrays.asList("b211f086-4b91-4686-a453-2f5c012e4d80"))
                    .withCorrelationid("b211f086-4b91-4686-a453-2f5c012e4d80"),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-04-15-preview/ScheduledActions_VirtualMachinesGetOperationStatus_MinimumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_VirtualMachinesGetOperationStatus_MinimumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsVirtualMachinesGetOperationStatusMinimumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .virtualMachinesGetOperationStatusWithResponse("ykvvjfoopmkwznctgaiblzvea",
                new GetOperationStatusRequest().withOperationIds(Arrays.asList("duhqnwosjzexcfwfhryvy"))
                    .withCorrelationid("b211f086-4b91-4686-a453-2f5c012e4d80"),
                com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_VirtualMachinesSubmitDeallocate

```java
import com.azure.resourcemanager.computeschedule.models.DeadlineType;
import com.azure.resourcemanager.computeschedule.models.ExecutionParameters;
import com.azure.resourcemanager.computeschedule.models.OptimizationPreference;
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
     * x-ms-original-file: 2025-04-15-preview/ScheduledActions_VirtualMachinesSubmitDeallocate_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_VirtualMachinesSubmitDeallocate_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsVirtualMachinesSubmitDeallocateMaximumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .virtualMachinesSubmitDeallocateWithResponse("ycipx", new SubmitDeallocateRequest()
                .withSchedule(new Schedule().withDeadLine(OffsetDateTime.parse("2025-04-17T00:23:56.803Z"))
                    .withTimeZone("aigbjdnldtzkteqi")
                    .withDeadlineType(DeadlineType.UNKNOWN))
                .withExecutionParameters(
                    new ExecutionParameters().withOptimizationPreference(OptimizationPreference.COST)
                        .withRetryPolicy(new RetryPolicy().withRetryCount(17).withRetryWindowInMinutes(29)))
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/YourSubscriptionId/resourceGroups/YourResourceGroupName/providers/Microsoft.Compute/virtualMachines/testResource4")))
                .withCorrelationid("b211f086-4b91-4686-a453-2f5c012e4d80"), com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-04-15-preview/ScheduledActions_VirtualMachinesSubmitDeallocate_MinimumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_VirtualMachinesSubmitDeallocate_MinimumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsVirtualMachinesSubmitDeallocateMinimumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .virtualMachinesSubmitDeallocateWithResponse("zrcmkxsbuxsxxulky", new SubmitDeallocateRequest()
                .withSchedule(new Schedule().withDeadLine(OffsetDateTime.parse("2025-04-17T00:23:56.803Z"))
                    .withTimeZone("aigbjdnldtzkteqi")
                    .withDeadlineType(DeadlineType.UNKNOWN))
                .withExecutionParameters(new ExecutionParameters())
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/YourSubscriptionId/resourceGroups/YourResourceGroupName/providers/Microsoft.Compute/virtualMachines/testResource4")))
                .withCorrelationid("b211f086-4b91-4686-a453-2f5c012e4d80"), com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_VirtualMachinesSubmitHibernate

```java
import com.azure.resourcemanager.computeschedule.models.DeadlineType;
import com.azure.resourcemanager.computeschedule.models.ExecutionParameters;
import com.azure.resourcemanager.computeschedule.models.OptimizationPreference;
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
     * x-ms-original-file: 2025-04-15-preview/ScheduledActions_VirtualMachinesSubmitHibernate_MinimumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_VirtualMachinesSubmitHibernate_MinimumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsVirtualMachinesSubmitHibernateMinimumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .virtualMachinesSubmitHibernateWithResponse("zuevcqpgdohzbjodhachtr", new SubmitHibernateRequest()
                .withSchedule(new Schedule().withDeadLine(OffsetDateTime.parse("2025-04-17T00:23:56.803Z"))
                    .withTimeZone("aigbjdnldtzkteqi")
                    .withDeadlineType(DeadlineType.UNKNOWN))
                .withExecutionParameters(new ExecutionParameters())
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/YourSubscriptionId/resourceGroups/YourResourceGroupName/providers/Microsoft.Compute/virtualMachines/testResource4")))
                .withCorrelationid("b211f086-4b91-4686-a453-2f5c012e4d80"), com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-04-15-preview/ScheduledActions_VirtualMachinesSubmitHibernate_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_VirtualMachinesSubmitHibernate_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsVirtualMachinesSubmitHibernateMaximumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .virtualMachinesSubmitHibernateWithResponse("rhadyapnyvmobwg", new SubmitHibernateRequest()
                .withSchedule(new Schedule().withDeadLine(OffsetDateTime.parse("2025-04-17T00:23:56.803Z"))
                    .withTimeZone("aigbjdnldtzkteqi")
                    .withDeadlineType(DeadlineType.UNKNOWN))
                .withExecutionParameters(
                    new ExecutionParameters().withOptimizationPreference(OptimizationPreference.COST)
                        .withRetryPolicy(new RetryPolicy().withRetryCount(17).withRetryWindowInMinutes(29)))
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/YourSubscriptionId/resourceGroups/YourResourceGroupName/providers/Microsoft.Compute/virtualMachines/testResource4")))
                .withCorrelationid("b211f086-4b91-4686-a453-2f5c012e4d80"), com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_VirtualMachinesSubmitStart

```java
import com.azure.resourcemanager.computeschedule.models.DeadlineType;
import com.azure.resourcemanager.computeschedule.models.ExecutionParameters;
import com.azure.resourcemanager.computeschedule.models.OptimizationPreference;
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
     * x-ms-original-file: 2025-04-15-preview/ScheduledActions_VirtualMachinesSubmitStart_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_VirtualMachinesSubmitStart_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsVirtualMachinesSubmitStartMaximumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .virtualMachinesSubmitStartWithResponse("pxtvvk", new SubmitStartRequest()
                .withSchedule(new Schedule().withDeadLine(OffsetDateTime.parse("2025-04-17T00:23:56.803Z"))
                    .withTimeZone("aigbjdnldtzkteqi")
                    .withDeadlineType(DeadlineType.UNKNOWN))
                .withExecutionParameters(
                    new ExecutionParameters().withOptimizationPreference(OptimizationPreference.COST)
                        .withRetryPolicy(new RetryPolicy().withRetryCount(17).withRetryWindowInMinutes(29)))
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/YourSubscriptionId/resourceGroups/YourResourceGroupName/providers/Microsoft.Compute/virtualMachines/testResource4")))
                .withCorrelationid("b211f086-4b91-4686-a453-2f5c012e4d80"), com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-04-15-preview/ScheduledActions_VirtualMachinesSubmitStart_MinimumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_VirtualMachinesSubmitStart_MinimumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsVirtualMachinesSubmitStartMinimumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .virtualMachinesSubmitStartWithResponse("ufrcsuw", new SubmitStartRequest()
                .withSchedule(new Schedule().withDeadLine(OffsetDateTime.parse("2025-04-17T00:23:56.803Z"))
                    .withTimeZone("aigbjdnldtzkteqi")
                    .withDeadlineType(DeadlineType.UNKNOWN))
                .withExecutionParameters(new ExecutionParameters())
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/YourSubscriptionId/resourceGroups/YourResourceGroupName/providers/Microsoft.Compute/virtualMachines/testResource4")))
                .withCorrelationid("b211f086-4b91-4686-a453-2f5c012e4d80"), com.azure.core.util.Context.NONE);
    }
}
```

