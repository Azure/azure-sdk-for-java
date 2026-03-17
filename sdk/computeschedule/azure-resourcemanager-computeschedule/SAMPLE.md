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
- [VirtualMachinesExecuteCreateFlex](#scheduledactions_virtualmachinesexecutecreateflex)
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
     * x-ms-original-file: 2026-03-01-preview/OccurrenceExtension_ListOccurrenceByVms_MaximumSet_Gen.json
     */
    /**
     * Sample code: OccurrenceExtension_ListOccurrenceByVms_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void occurrenceExtensionListOccurrenceByVmsMaximumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.occurrenceExtensions().listOccurrenceByVms("rgdhjh", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-03-01-preview/Occurrences_Cancel_MaximumSet_Gen.json
     */
    /**
     * Sample code: Occurrences_Cancel_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void
        occurrencesCancelMaximumSet(com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.occurrences()
            .cancelWithResponse("rgcomputeschedule", "scheduled-action-01", "11111111-1111-1111-1111-111111111111",
                new CancelOccurrenceRequest().withResourceIds(Arrays.asList(
                    "/subscriptions/11111111-1111-1111-1111-111111111111/resourceGroups/rgcomputeschedule/providers/Microsoft.Compute/virtualMachines/vm1")),
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
     * x-ms-original-file: 2026-03-01-preview/Occurrences_Delay_MaximumSet_Gen.json
     */
    /**
     * Sample code: Occurrences_Delay_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void
        occurrencesDelayMaximumSet(com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.occurrences()
            .delay("rgcomputeschedule", "scheduled-action-01", "11111111-1111-1111-1111-111111111111",
                new DelayRequest().withDelay(OffsetDateTime.parse("2026-03-12T02:39:48.148Z"))
                    .withResourceIds(Arrays.asList(
                        "/subscriptions/11111111-1111-1111-1111-111111111111/resourceGroups/rgcomputeschedule/providers/Microsoft.Compute/virtualMachines/vm1")),
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
     * x-ms-original-file: 2026-03-01-preview/Occurrences_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Occurrences_Get_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void
        occurrencesGetMaximumSet(com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.occurrences()
            .getWithResponse("rgcomputeschedule", "scheduled-action-01", "11111111-1111-1111-1111-111111111111",
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
     * x-ms-original-file: 2026-03-01-preview/Occurrences_ListByScheduledAction_MaximumSet_Gen.json
     */
    /**
     * Sample code: Occurrences_ListByScheduledAction_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void occurrencesListByScheduledActionMaximumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.occurrences()
            .listByScheduledAction("rgcomputeschedule", "scheduled-action-01", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-03-01-preview/Occurrences_ListResources_MaximumSet_Gen.json
     */
    /**
     * Sample code: Occurrences_ListResources_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void
        occurrencesListResourcesMaximumSet(com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.occurrences()
            .listResources("rgcomputeschedule", "scheduled-action-01", "11111111-1111-1111-1111-111111111111",
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
     * x-ms-original-file: 2026-03-01-preview/Operations_List_MinimumSet_Gen.json
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
     * x-ms-original-file: 2026-03-01-preview/Operations_List_MaximumSet_Gen.json
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
     * x-ms-original-file: 2026-03-01-preview/ScheduledActionExtension_ListByVms_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActionExtension_ListByVms_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionExtensionListByVmsMaximumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActionExtensions().listByVms("rgdhjh", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-03-01-preview/ScheduledActions_AttachResources_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_AttachResources_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsAttachResourcesMaximumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .attachResourcesWithResponse("rgcomputeschedule", "scheduled-action-01", new ResourceAttachRequest()
                .withResources(Arrays.asList(new ScheduledActionResourceInner().withResourceId(
                    "/subscriptions/1d04e8f1-ee04-4056-b0b2-718f5bb45b04/resourceGroups/myRg/providers/Microsoft.Compute/virtualMachines/myVm")
                    .withNotificationSettings(
                        Arrays.asList(new NotificationProperties().withDestination("zaaoabozbhyuhejwsrennfsxowp")
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
     * x-ms-original-file: 2026-03-01-preview/ScheduledActions_CancelNextOccurrence_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_CancelNextOccurrence_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsCancelNextOccurrenceMaximumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .cancelNextOccurrenceWithResponse("rgcomputeschedule", "scheduled-action-01",
                new CancelOccurrenceRequest().withResourceIds(Arrays.asList(
                    "/subscriptions/11111111-1111-1111-1111-111111111111/resourceGroups/rgcomputeschedule/providers/Microsoft.Compute/virtualMachines/vm1")),
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
import com.azure.resourcemanager.computeschedule.models.ResourceOperationType;
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
     * x-ms-original-file: 2026-03-01-preview/ScheduledActions_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsCreateOrUpdateMaximumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .define("scheduled-action-01")
            .withRegion("hwhsvnvuwsjwu")
            .withExistingResourceGroup("rgcomputeschedule")
            .withTags(mapOf("key1458", "fakeTokenPlaceholder"))
            .withProperties(new ScheduledActionProperties().withResourceType(ResourceType.VIRTUAL_MACHINE)
                .withActionType(ScheduledActionType.START)
                .withStartTime(OffsetDateTime.parse("2026-03-12T02:39:45.061Z"))
                .withEndTime(OffsetDateTime.parse("2026-03-12T02:39:45.062Z"))
                .withSchedule(new ScheduledActionsSchedule().withScheduledTime("12:00:00")
                    .withTimeZone("ywgarwvwqegtdsdvv")
                    .withRequestedWeekDays(Arrays.asList(WeekDay.MONDAY))
                    .withRequestedMonths(Arrays.asList(Month.JANUARY))
                    .withRequestedDaysOfTheMonth(Arrays.asList(18))
                    .withExecutionParameters(
                        new ExecutionParameters().withOptimizationPreference(OptimizationPreference.COST)
                            .withRetryPolicy(new RetryPolicy().withRetryCount(19)
                                .withRetryWindowInMinutes(3)
                                .withOnFailureAction(ResourceOperationType.UNKNOWN)))
                    .withDeadlineType(DeadlineType.UNKNOWN))
                .withNotificationSettings(
                    Arrays.asList(new NotificationProperties().withDestination("zaaoabozbhyuhejwsrennfsxowp")
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
     * x-ms-original-file: 2026-03-01-preview/ScheduledActions_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_Delete_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void
        scheduledActionsDeleteMaximumSet(com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions().delete("rgcomputeschedule", "scheduled-action-01", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-03-01-preview/ScheduledActions_DetachResources_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_DetachResources_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsDetachResourcesMaximumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .detachResourcesWithResponse("rgcomputeschedule", "scheduled-action-01",
                new ResourceDetachRequest().withResources(Arrays.asList(
                    "/subscriptions/11111111-1111-1111-1111-111111111111/resourceGroups/rgcomputeschedule/providers/Microsoft.Compute/virtualMachines/vm1")),
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
     * x-ms-original-file: 2026-03-01-preview/ScheduledActions_Disable_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_Disable_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void
        scheduledActionsDisableMaximumSet(com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .disableWithResponse("rgcomputeschedule", "scheduled-action-01", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-03-01-preview/ScheduledActions_Enable_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_Enable_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void
        scheduledActionsEnableMaximumSet(com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .enableWithResponse("rgcomputeschedule", "scheduled-action-01", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-03-01-preview/ScheduledActions_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_Get_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void
        scheduledActionsGetMaximumSet(com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .getByResourceGroupWithResponse("rgcomputeschedule", "scheduled-action-01",
                com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-03-01-preview/ScheduledActions_ListBySubscription_MaximumSet_Gen.json
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
}
```

### ScheduledActions_ListByResourceGroup

```java
/**
 * Samples for ScheduledActions ListByResourceGroup.
 */
public final class ScheduledActionsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-03-01-preview/ScheduledActions_ListByResourceGroup_MaximumSet_Gen.json
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
}
```

### ScheduledActions_ListResources

```java
/**
 * Samples for ScheduledActions ListResources.
 */
public final class ScheduledActionsListResourcesSamples {
    /*
     * x-ms-original-file: 2026-03-01-preview/ScheduledActions_ListResources_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_ListResources_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsListResourcesMaximumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .listResources("rgcomputeschedule", "scheduled-action-01", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-03-01-preview/ScheduledActions_PatchResources_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_PatchResources_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsPatchResourcesMaximumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .patchResourcesWithResponse("rgcomputeschedule", "scheduled-action-01", new ResourcePatchRequest()
                .withResources(Arrays.asList(new ScheduledActionResourceInner().withResourceId(
                    "/subscriptions/1d04e8f1-ee04-4056-b0b2-718f5bb45b04/resourceGroups/myRg/providers/Microsoft.Compute/virtualMachines/myVm")
                    .withNotificationSettings(
                        Arrays.asList(new NotificationProperties().withDestination("zaaoabozbhyuhejwsrennfsxowp")
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
     * x-ms-original-file: 2026-03-01-preview/ScheduledActions_TriggerManualOccurrence_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_TriggerManualOccurrence_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsTriggerManualOccurrenceMaximumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .triggerManualOccurrenceWithResponse("rgcomputeschedule", "scheduled-action-01",
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
import com.azure.resourcemanager.computeschedule.models.ResourceOperationType;
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
     * x-ms-original-file: 2026-03-01-preview/ScheduledActions_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_Update_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void
        scheduledActionsUpdateMaximumSet(com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        ScheduledAction resource = manager.scheduledActions()
            .getByResourceGroupWithResponse("rgcomputeschedule", "scheduled-action-01",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key100", "fakeTokenPlaceholder"))
            .withProperties(new ScheduledActionUpdateProperties().withResourceType(ResourceType.VIRTUAL_MACHINE)
                .withActionType(ScheduledActionType.START)
                .withStartTime(OffsetDateTime.parse("2026-03-12T02:39:45.818Z"))
                .withEndTime(OffsetDateTime.parse("2026-03-12T02:39:45.818Z"))
                .withSchedule(new ScheduledActionsSchedule().withScheduledTime("12:00:00")
                    .withTimeZone("gythvwybzwqubqtmatezbxj")
                    .withRequestedWeekDays(Arrays.asList(WeekDay.MONDAY))
                    .withRequestedMonths(Arrays.asList(Month.JANUARY))
                    .withRequestedDaysOfTheMonth(Arrays.asList(18))
                    .withExecutionParameters(
                        new ExecutionParameters().withOptimizationPreference(OptimizationPreference.COST)
                            .withRetryPolicy(new RetryPolicy().withRetryCount(19)
                                .withRetryWindowInMinutes(3)
                                .withOnFailureAction(ResourceOperationType.UNKNOWN)))
                    .withDeadlineType(DeadlineType.UNKNOWN))
                .withNotificationSettings(
                    Arrays.asList(new NotificationProperties().withDestination("zaaoabozbhyuhejwsrennfsxowp")
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
import com.azure.resourcemanager.computeschedule.models.CancelOperationsContent;
import java.util.Arrays;

/**
 * Samples for ScheduledActions VirtualMachinesCancelOperations.
 */
public final class ScheduledActionsVirtualMachinesCancelOperationsSamples {
    /*
     * x-ms-original-file: 2026-03-01-preview/ScheduledActions_VirtualMachinesCancelOperations_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_VirtualMachinesCancelOperations_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsVirtualMachinesCancelOperationsMaximumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .virtualMachinesCancelOperationsWithResponse("msbzqfwhqmtrxafwwh",
                new CancelOperationsContent().withOperationIds(Arrays.asList("xgnpiititgkgsewft"))
                    .withCorrelationid("xqlibhgsczjzsbjvc"),
                com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_VirtualMachinesExecuteCreate

```java
import com.azure.resourcemanager.computeschedule.models.ExecuteCreateContent;
import com.azure.resourcemanager.computeschedule.models.ExecutionParameters;
import com.azure.resourcemanager.computeschedule.models.OptimizationPreference;
import com.azure.resourcemanager.computeschedule.models.ResourceOperationType;
import com.azure.resourcemanager.computeschedule.models.ResourceProvisionPayload;
import com.azure.resourcemanager.computeschedule.models.RetryPolicy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ScheduledActions VirtualMachinesExecuteCreate.
 */
public final class ScheduledActionsVirtualMachinesExecuteCreateSamples {
    /*
     * x-ms-original-file: 2026-03-01-preview/ScheduledActions_VirtualMachinesExecuteCreate_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_VirtualMachinesExecuteCreate_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsVirtualMachinesExecuteCreateMaximumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .virtualMachinesExecuteCreateWithResponse("tcvpodlxbyg",
                new ExecuteCreateContent()
                    .withResourceConfigParameters(new ResourceProvisionPayload().withBaseProfile(mapOf())
                        .withResourceOverrides(Arrays.asList(mapOf()))
                        .withResourceCount(19)
                        .withResourcePrefix("ilgcqhoqrpwfkprxok"))
                    .withExecutionParameters(
                        new ExecutionParameters().withOptimizationPreference(OptimizationPreference.COST)
                            .withRetryPolicy(new RetryPolicy().withRetryCount(19)
                                .withRetryWindowInMinutes(3)
                                .withOnFailureAction(ResourceOperationType.UNKNOWN)))
                    .withCorrelationid("pkm"),
                com.azure.core.util.Context.NONE);
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

### ScheduledActions_VirtualMachinesExecuteCreateFlex

```java
import com.azure.resourcemanager.computeschedule.models.AllocationStrategy;
import com.azure.resourcemanager.computeschedule.models.DistributionStrategy;
import com.azure.resourcemanager.computeschedule.models.ExecuteCreateFlexRequest;
import com.azure.resourcemanager.computeschedule.models.ExecutionParameters;
import com.azure.resourcemanager.computeschedule.models.FlexProperties;
import com.azure.resourcemanager.computeschedule.models.OptimizationPreference;
import com.azure.resourcemanager.computeschedule.models.OsType;
import com.azure.resourcemanager.computeschedule.models.PriorityProfile;
import com.azure.resourcemanager.computeschedule.models.PriorityType;
import com.azure.resourcemanager.computeschedule.models.ResourceOperationType;
import com.azure.resourcemanager.computeschedule.models.ResourceProvisionFlexPayload;
import com.azure.resourcemanager.computeschedule.models.RetryPolicy;
import com.azure.resourcemanager.computeschedule.models.VmSizeProfile;
import com.azure.resourcemanager.computeschedule.models.ZoneAllocationPolicy;
import com.azure.resourcemanager.computeschedule.models.ZonePreference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ScheduledActions VirtualMachinesExecuteCreateFlex.
 */
public final class ScheduledActionsVirtualMachinesExecuteCreateFlexSamples {
    /*
     * x-ms-original-file: 2026-03-01-preview/ScheduledActions_VirtualMachinesExecuteCreateFlex_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_VirtualMachinesExecuteCreateFlex_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsVirtualMachinesExecuteCreateFlexMaximumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .virtualMachinesExecuteCreateFlexWithResponse("bnadpwmpjyfduxue",
                new ExecuteCreateFlexRequest()
                    .withResourceConfigParameters(
                        new ResourceProvisionFlexPayload().withBaseProfile(mapOf())
                            .withResourceOverrides(Arrays.asList(mapOf()))
                            .withResourceCount(24)
                            .withResourcePrefix("irskthgchddyuddpqaxlw")
                            .withFlexProperties(new FlexProperties()
                                .withVmSizeProfiles(Arrays.asList(new VmSizeProfile().withName("bmzylbvz").withRank(24),
                                    new VmSizeProfile().withName("bmzylbvz").withRank(24)))
                                .withOsType(OsType.WINDOWS)
                                .withPriorityProfile(new PriorityProfile().withType(PriorityType.REGULAR)
                                    .withAllocationStrategy(AllocationStrategy.LOWEST_PRICE))
                                .withZoneAllocationPolicy(new ZoneAllocationPolicy()
                                    .withDistributionStrategy(DistributionStrategy.BEST_EFFORT_SINGLE_ZONE)
                                    .withZonePreferences(Arrays
                                        .asList(new ZonePreference().withZone("kmcvkimwmqwrhfznrqs").withRank(21))))))
                    .withExecutionParameters(
                        new ExecutionParameters().withOptimizationPreference(OptimizationPreference.COST)
                            .withRetryPolicy(new RetryPolicy().withRetryCount(19)
                                .withRetryWindowInMinutes(3)
                                .withOnFailureAction(ResourceOperationType.UNKNOWN)))
                    .withCorrelationid("dnncbbqrhnrwcfqrathnexdpcryo"),
                com.azure.core.util.Context.NONE);
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
import com.azure.resourcemanager.computeschedule.models.ExecuteDeallocateContent;
import com.azure.resourcemanager.computeschedule.models.ExecutionParameters;
import com.azure.resourcemanager.computeschedule.models.OptimizationPreference;
import com.azure.resourcemanager.computeschedule.models.ResourceOperationType;
import com.azure.resourcemanager.computeschedule.models.Resources;
import com.azure.resourcemanager.computeschedule.models.RetryPolicy;
import java.util.Arrays;

/**
 * Samples for ScheduledActions VirtualMachinesExecuteDeallocate.
 */
public final class ScheduledActionsVirtualMachinesExecuteDeallocateSamples {
    /*
     * x-ms-original-file: 2026-03-01-preview/ScheduledActions_VirtualMachinesExecuteDeallocate_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_VirtualMachinesExecuteDeallocate_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsVirtualMachinesExecuteDeallocateMaximumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .virtualMachinesExecuteDeallocateWithResponse("tobscvse", new ExecuteDeallocateContent()
                .withExecutionParameters(
                    new ExecutionParameters().withOptimizationPreference(OptimizationPreference.COST)
                        .withRetryPolicy(new RetryPolicy().withRetryCount(19)
                            .withRetryWindowInMinutes(3)
                            .withOnFailureAction(ResourceOperationType.UNKNOWN)))
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/11111111-1111-1111-1111-111111111111/resourceGroups/rgcomputeschedule/providers/Microsoft.Compute/virtualMachines/vm1")))
                .withCorrelationid("ruwskrvse"), com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_VirtualMachinesExecuteDelete

```java
import com.azure.resourcemanager.computeschedule.models.ExecuteDeleteContent;
import com.azure.resourcemanager.computeschedule.models.ExecutionParameters;
import com.azure.resourcemanager.computeschedule.models.OptimizationPreference;
import com.azure.resourcemanager.computeschedule.models.ResourceOperationType;
import com.azure.resourcemanager.computeschedule.models.Resources;
import com.azure.resourcemanager.computeschedule.models.RetryPolicy;
import java.util.Arrays;

/**
 * Samples for ScheduledActions VirtualMachinesExecuteDelete.
 */
public final class ScheduledActionsVirtualMachinesExecuteDeleteSamples {
    /*
     * x-ms-original-file: 2026-03-01-preview/ScheduledActions_VirtualMachinesExecuteDelete_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_VirtualMachinesExecuteDelete_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsVirtualMachinesExecuteDeleteMaximumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .virtualMachinesExecuteDeleteWithResponse("swchjs", new ExecuteDeleteContent()
                .withExecutionParameters(
                    new ExecutionParameters().withOptimizationPreference(OptimizationPreference.COST)
                        .withRetryPolicy(new RetryPolicy().withRetryCount(19)
                            .withRetryWindowInMinutes(3)
                            .withOnFailureAction(ResourceOperationType.UNKNOWN)))
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/11111111-1111-1111-1111-111111111111/resourceGroups/rgcomputeschedule/providers/Microsoft.Compute/virtualMachines/vm1")))
                .withCorrelationid("upxtzubhqqzhk")
                .withForceDeletion(true), com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_VirtualMachinesExecuteHibernate

```java
import com.azure.resourcemanager.computeschedule.models.ExecuteHibernateContent;
import com.azure.resourcemanager.computeschedule.models.ExecutionParameters;
import com.azure.resourcemanager.computeschedule.models.OptimizationPreference;
import com.azure.resourcemanager.computeschedule.models.ResourceOperationType;
import com.azure.resourcemanager.computeschedule.models.Resources;
import com.azure.resourcemanager.computeschedule.models.RetryPolicy;
import java.util.Arrays;

/**
 * Samples for ScheduledActions VirtualMachinesExecuteHibernate.
 */
public final class ScheduledActionsVirtualMachinesExecuteHibernateSamples {
    /*
     * x-ms-original-file: 2026-03-01-preview/ScheduledActions_VirtualMachinesExecuteHibernate_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_VirtualMachinesExecuteHibernate_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsVirtualMachinesExecuteHibernateMaximumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .virtualMachinesExecuteHibernateWithResponse("zydcprtgybcyilgugfwtfp", new ExecuteHibernateContent()
                .withExecutionParameters(
                    new ExecutionParameters().withOptimizationPreference(OptimizationPreference.COST)
                        .withRetryPolicy(new RetryPolicy().withRetryCount(19)
                            .withRetryWindowInMinutes(3)
                            .withOnFailureAction(ResourceOperationType.UNKNOWN)))
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/11111111-1111-1111-1111-111111111111/resourceGroups/rgcomputeschedule/providers/Microsoft.Compute/virtualMachines/vm1")))
                .withCorrelationid("miqcvaudnngdedocspkthnoo"), com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_VirtualMachinesExecuteStart

```java
import com.azure.resourcemanager.computeschedule.models.ExecuteStartContent;
import com.azure.resourcemanager.computeschedule.models.ExecutionParameters;
import com.azure.resourcemanager.computeschedule.models.OptimizationPreference;
import com.azure.resourcemanager.computeschedule.models.ResourceOperationType;
import com.azure.resourcemanager.computeschedule.models.Resources;
import com.azure.resourcemanager.computeschedule.models.RetryPolicy;
import java.util.Arrays;

/**
 * Samples for ScheduledActions VirtualMachinesExecuteStart.
 */
public final class ScheduledActionsVirtualMachinesExecuteStartSamples {
    /*
     * x-ms-original-file: 2026-03-01-preview/ScheduledActions_VirtualMachinesExecuteStart_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_VirtualMachinesExecuteStart_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsVirtualMachinesExecuteStartMaximumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .virtualMachinesExecuteStartWithResponse("vpcshwavxibrshqepqktfyu", new ExecuteStartContent()
                .withExecutionParameters(
                    new ExecutionParameters().withOptimizationPreference(OptimizationPreference.COST)
                        .withRetryPolicy(new RetryPolicy().withRetryCount(19)
                            .withRetryWindowInMinutes(3)
                            .withOnFailureAction(ResourceOperationType.UNKNOWN)))
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/11111111-1111-1111-1111-111111111111/resourceGroups/rgcomputeschedule/providers/Microsoft.Compute/virtualMachines/vm1")))
                .withCorrelationid("fmjvelcuicsoqykbyercnrkda"), com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_VirtualMachinesGetOperationErrors

```java
import com.azure.resourcemanager.computeschedule.models.GetOperationErrorsContent;
import java.util.Arrays;

/**
 * Samples for ScheduledActions VirtualMachinesGetOperationErrors.
 */
public final class ScheduledActionsVirtualMachinesGetOperationErrorsSamples {
    /*
     * x-ms-original-file: 2026-03-01-preview/ScheduledActions_VirtualMachinesGetOperationErrors_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_VirtualMachinesGetOperationErrors_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsVirtualMachinesGetOperationErrorsMaximumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .virtualMachinesGetOperationErrorsWithResponse("hvevnodfnj",
                new GetOperationErrorsContent().withOperationIds(Arrays.asList("enrmeatzaw")),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-03-01-preview/ScheduledActions_VirtualMachinesGetOperationErrors_MinimumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_VirtualMachinesGetOperationErrors_MinimumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsVirtualMachinesGetOperationErrorsMinimumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .virtualMachinesGetOperationErrorsWithResponse("vkkri",
                new GetOperationErrorsContent().withOperationIds(Arrays.asList("enrmeatzaw")),
                com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_VirtualMachinesGetOperationStatus

```java
import com.azure.resourcemanager.computeschedule.models.GetOperationStatusContent;
import java.util.Arrays;

/**
 * Samples for ScheduledActions VirtualMachinesGetOperationStatus.
 */
public final class ScheduledActionsVirtualMachinesGetOperationStatusSamples {
    /*
     * x-ms-original-file: 2026-03-01-preview/ScheduledActions_VirtualMachinesGetOperationStatus_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_VirtualMachinesGetOperationStatus_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsVirtualMachinesGetOperationStatusMaximumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .virtualMachinesGetOperationStatusWithResponse("gcbtdawfspg",
                new GetOperationStatusContent().withOperationIds(Arrays.asList("ehesndkmbcncsvkqvxwkbmidmaxhx"))
                    .withCorrelationid("hzpmcjplnbokebrbxrtuvxpp"),
                com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_VirtualMachinesSubmitDeallocate

```java
import com.azure.resourcemanager.computeschedule.models.DeadlineType;
import com.azure.resourcemanager.computeschedule.models.ExecutionParameters;
import com.azure.resourcemanager.computeschedule.models.OptimizationPreference;
import com.azure.resourcemanager.computeschedule.models.ResourceOperationType;
import com.azure.resourcemanager.computeschedule.models.Resources;
import com.azure.resourcemanager.computeschedule.models.RetryPolicy;
import com.azure.resourcemanager.computeschedule.models.Schedule;
import com.azure.resourcemanager.computeschedule.models.SubmitDeallocateContent;
import java.time.OffsetDateTime;
import java.util.Arrays;

/**
 * Samples for ScheduledActions VirtualMachinesSubmitDeallocate.
 */
public final class ScheduledActionsVirtualMachinesSubmitDeallocateSamples {
    /*
     * x-ms-original-file: 2026-03-01-preview/ScheduledActions_VirtualMachinesSubmitDeallocate_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_VirtualMachinesSubmitDeallocate_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsVirtualMachinesSubmitDeallocateMaximumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .virtualMachinesSubmitDeallocateWithResponse("wtkfymadciksjsvamowmbiqdltdzw", new SubmitDeallocateContent()
                .withSchedule(new Schedule().withDeadline(OffsetDateTime.parse("2026-03-12T02:39:44.444Z"))
                    .withDeadLine(OffsetDateTime.parse("2026-03-12T02:39:44.444Z"))
                    .withTimezone("ehbjytlhnykfkjxujca")
                    .withTimeZone("feiohhavpmmjrf")
                    .withDeadlineType(DeadlineType.UNKNOWN))
                .withExecutionParameters(
                    new ExecutionParameters().withOptimizationPreference(OptimizationPreference.COST)
                        .withRetryPolicy(new RetryPolicy().withRetryCount(19)
                            .withRetryWindowInMinutes(3)
                            .withOnFailureAction(ResourceOperationType.UNKNOWN)))
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/11111111-1111-1111-1111-111111111111/resourceGroups/rgcomputeschedule/providers/Microsoft.Compute/virtualMachines/vm1")))
                .withCorrelationid("rsi"), com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_VirtualMachinesSubmitHibernate

```java
import com.azure.resourcemanager.computeschedule.models.DeadlineType;
import com.azure.resourcemanager.computeschedule.models.ExecutionParameters;
import com.azure.resourcemanager.computeschedule.models.OptimizationPreference;
import com.azure.resourcemanager.computeschedule.models.ResourceOperationType;
import com.azure.resourcemanager.computeschedule.models.Resources;
import com.azure.resourcemanager.computeschedule.models.RetryPolicy;
import com.azure.resourcemanager.computeschedule.models.Schedule;
import com.azure.resourcemanager.computeschedule.models.SubmitHibernateContent;
import java.time.OffsetDateTime;
import java.util.Arrays;

/**
 * Samples for ScheduledActions VirtualMachinesSubmitHibernate.
 */
public final class ScheduledActionsVirtualMachinesSubmitHibernateSamples {
    /*
     * x-ms-original-file: 2026-03-01-preview/ScheduledActions_VirtualMachinesSubmitHibernate_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_VirtualMachinesSubmitHibernate_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsVirtualMachinesSubmitHibernateMaximumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .virtualMachinesSubmitHibernateWithResponse("aoewv", new SubmitHibernateContent()
                .withSchedule(new Schedule().withDeadline(OffsetDateTime.parse("2026-03-12T02:39:44.444Z"))
                    .withDeadLine(OffsetDateTime.parse("2026-03-12T02:39:44.444Z"))
                    .withTimezone("ehbjytlhnykfkjxujca")
                    .withTimeZone("feiohhavpmmjrf")
                    .withDeadlineType(DeadlineType.UNKNOWN))
                .withExecutionParameters(
                    new ExecutionParameters().withOptimizationPreference(OptimizationPreference.COST)
                        .withRetryPolicy(new RetryPolicy().withRetryCount(19)
                            .withRetryWindowInMinutes(3)
                            .withOnFailureAction(ResourceOperationType.UNKNOWN)))
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/11111111-1111-1111-1111-111111111111/resourceGroups/rgcomputeschedule/providers/Microsoft.Compute/virtualMachines/vm1")))
                .withCorrelationid("lgpkqjojane"), com.azure.core.util.Context.NONE);
    }
}
```

### ScheduledActions_VirtualMachinesSubmitStart

```java
import com.azure.resourcemanager.computeschedule.models.DeadlineType;
import com.azure.resourcemanager.computeschedule.models.ExecutionParameters;
import com.azure.resourcemanager.computeschedule.models.OptimizationPreference;
import com.azure.resourcemanager.computeschedule.models.ResourceOperationType;
import com.azure.resourcemanager.computeschedule.models.Resources;
import com.azure.resourcemanager.computeschedule.models.RetryPolicy;
import com.azure.resourcemanager.computeschedule.models.Schedule;
import com.azure.resourcemanager.computeschedule.models.SubmitStartContent;
import java.time.OffsetDateTime;
import java.util.Arrays;

/**
 * Samples for ScheduledActions VirtualMachinesSubmitStart.
 */
public final class ScheduledActionsVirtualMachinesSubmitStartSamples {
    /*
     * x-ms-original-file: 2026-03-01-preview/ScheduledActions_VirtualMachinesSubmitStart_MaximumSet_Gen.json
     */
    /**
     * Sample code: ScheduledActions_VirtualMachinesSubmitStart_MaximumSet.
     * 
     * @param manager Entry point to ComputeScheduleManager.
     */
    public static void scheduledActionsVirtualMachinesSubmitStartMaximumSet(
        com.azure.resourcemanager.computeschedule.ComputeScheduleManager manager) {
        manager.scheduledActions()
            .virtualMachinesSubmitStartWithResponse("cabm", new SubmitStartContent()
                .withSchedule(new Schedule().withDeadline(OffsetDateTime.parse("2026-03-12T02:39:44.444Z"))
                    .withDeadLine(OffsetDateTime.parse("2026-03-12T02:39:44.444Z"))
                    .withTimezone("ehbjytlhnykfkjxujca")
                    .withTimeZone("feiohhavpmmjrf")
                    .withDeadlineType(DeadlineType.UNKNOWN))
                .withExecutionParameters(
                    new ExecutionParameters().withOptimizationPreference(OptimizationPreference.COST)
                        .withRetryPolicy(new RetryPolicy().withRetryCount(19)
                            .withRetryWindowInMinutes(3)
                            .withOnFailureAction(ResourceOperationType.UNKNOWN)))
                .withResources(new Resources().withIds(Arrays.asList(
                    "/subscriptions/11111111-1111-1111-1111-111111111111/resourceGroups/rgcomputeschedule/providers/Microsoft.Compute/virtualMachines/vm1")))
                .withCorrelationid("ttlsvgmsykwowrajtmnztcm"), com.azure.core.util.Context.NONE);
    }
}
```

