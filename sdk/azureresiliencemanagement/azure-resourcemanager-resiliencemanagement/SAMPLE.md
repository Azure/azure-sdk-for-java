# Code snippets and samples


## DrillResources

- [Get](#drillresources_get)
- [List](#drillresources_list)

## DrillRunResources

- [Get](#drillrunresources_get)
- [List](#drillrunresources_list)

## DrillRuns

- [AddNotes](#drillruns_addnotes)
- [FailOver](#drillruns_failover)
- [Get](#drillruns_get)
- [List](#drillruns_list)
- [MarkAsComplete](#drillruns_markascomplete)
- [Reprotect](#drillruns_reprotect)
- [Resume](#drillruns_resume)

## Drills

- [AddOrUpdateResources](#drills_addorupdateresources)
- [Create](#drills_create)
- [Delete](#drills_delete)
- [End](#drills_end)
- [Get](#drills_get)
- [List](#drills_list)
- [ResyncReadinessCheck](#drills_resyncreadinesscheck)
- [Start](#drills_start)
- [Update](#drills_update)
- [ValidateForExecution](#drills_validateforexecution)

## Enrollments

- [CreateOrUpdate](#enrollments_createorupdate)
- [Delete](#enrollments_delete)
- [Get](#enrollments_get)
- [List](#enrollments_list)

## GoalAssignments

- [CreateOrUpdate](#goalassignments_createorupdate)
- [Delete](#goalassignments_delete)
- [Get](#goalassignments_get)
- [List](#goalassignments_list)
- [RecommendCapacity](#goalassignments_recommendcapacity)
- [RefreshGoalResources](#goalassignments_refreshgoalresources)
- [Update](#goalassignments_update)
- [UpdateGoalResources](#goalassignments_updategoalresources)

## GoalResources

- [Get](#goalresources_get)
- [List](#goalresources_list)

## GoalTemplates

- [CreateOrUpdate](#goaltemplates_createorupdate)
- [Delete](#goaltemplates_delete)
- [Get](#goaltemplates_get)
- [List](#goaltemplates_list)
- [Update](#goaltemplates_update)

## OperationStatus

- [Get](#operationstatus_get)

## Operations

- [List](#operations_list)

## RecoveryJobResources

- [Get](#recoveryjobresources_get)
- [List](#recoveryjobresources_list)

## RecoveryJobs

- [Cancel](#recoveryjobs_cancel)
- [Get](#recoveryjobs_get)
- [List](#recoveryjobs_list)
- [Resume](#recoveryjobs_resume)
- [Retry](#recoveryjobs_retry)

## RecoveryPlanActions

- [CheckReadiness](#recoveryplanactions_checkreadiness)
- [Failover](#recoveryplanactions_failover)
- [FailoverCommit](#recoveryplanactions_failovercommit)
- [Finalize](#recoveryplanactions_finalize)
- [Reprotect](#recoveryplanactions_reprotect)
- [TestFailover](#recoveryplanactions_testfailover)
- [TestFailoverCleanup](#recoveryplanactions_testfailovercleanup)
- [UpdateResources](#recoveryplanactions_updateresources)
- [ValidateForFailover](#recoveryplanactions_validateforfailover)
- [ValidateForFailoverCommit](#recoveryplanactions_validateforfailovercommit)
- [ValidateForOperation](#recoveryplanactions_validateforoperation)
- [ValidateForReprotect](#recoveryplanactions_validateforreprotect)
- [ValidateForTestFailover](#recoveryplanactions_validatefortestfailover)
- [ValidateForTestFailoverCleanup](#recoveryplanactions_validatefortestfailovercleanup)

## RecoveryPlans

- [CreateOrUpdate](#recoveryplans_createorupdate)
- [Delete](#recoveryplans_delete)
- [Get](#recoveryplans_get)
- [List](#recoveryplans_list)
- [Update](#recoveryplans_update)

## RecoveryResources

- [Get](#recoveryresources_get)
- [List](#recoveryresources_list)

## UnifiedResilienceItems

- [Get](#unifiedresilienceitems_get)
- [List](#unifiedresilienceitems_list)

## UsagePlans

- [CreateOrUpdate](#usageplans_createorupdate)
- [Delete](#usageplans_delete)
- [GetByResourceGroup](#usageplans_getbyresourcegroup)
- [List](#usageplans_list)
- [ListByResourceGroup](#usageplans_listbyresourcegroup)
- [Update](#usageplans_update)
### DrillResources_Get

```java
/**
 * Samples for DrillResources Get.
 */
public final class DrillResourcesGetSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/DrillResources_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: DrillResources_Get_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void drillResourcesGetMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.drillResources()
            .getWithResponse("sampleServiceGroupName", "drill1", "b6378181-9dc0-4a43-8e09-97a8b08aabaa",
                com.azure.core.util.Context.NONE);
    }
}
```

### DrillResources_List

```java
/**
 * Samples for DrillResources List.
 */
public final class DrillResourcesListSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/DrillResources_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: DrillResources_List_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void drillResourcesListMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.drillResources()
            .list("sampleServiceGroupName", "drill1", "xntbyoswztnmvitj", 69, com.azure.core.util.Context.NONE);
    }
}
```

### DrillRunResources_Get

```java
/**
 * Samples for DrillRunResources Get.
 */
public final class DrillRunResourcesGetSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/DrillRunResources_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: DrillRunResources_Get_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void drillRunResourcesGetMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.drillRunResources()
            .getWithResponse("sampleServiceGroupName", "drill1", "ca92602e-53bf-43d2-ae62-d3fc940474b3",
                "56f942da-a30e-43c0-b5f0-1c22e44f2d94", com.azure.core.util.Context.NONE);
    }
}
```

### DrillRunResources_List

```java
/**
 * Samples for DrillRunResources List.
 */
public final class DrillRunResourcesListSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/DrillRunResources_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: DrillRunResources_List_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void drillRunResourcesListMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.drillRunResources()
            .list("sampleServiceGroupName", "drill1", "ca92602e-53bf-43d2-ae62-d3fc940474b3",
                com.azure.core.util.Context.NONE);
    }
}
```

### DrillRuns_AddNotes

```java
import com.azure.resourcemanager.resiliencemanagement.models.DrillRunAddNotesRequest;

/**
 * Samples for DrillRuns AddNotes.
 */
public final class DrillRunsAddNotesSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/DrillRuns_AddNotes_MaximumSet_Gen.json
     */
    /**
     * Sample code: DrillRuns_AddNotes_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void drillRunsAddNotesMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.drillRuns()
            .addNotes("sampleServiceGroupName", "qmn", "drill1", "ca92602e-53bf-43d2-ae62-d3fc940474b3",
                new DrillRunAddNotesRequest().withNotes("wubqjajveatmwcglo"), com.azure.core.util.Context.NONE);
    }
}
```

### DrillRuns_FailOver

```java
import com.azure.resourcemanager.resiliencemanagement.models.AutoFailover;
import com.azure.resourcemanager.resiliencemanagement.models.DrillRunFailoverRequest;
import com.azure.resourcemanager.resiliencemanagement.models.FailoverDirectionTypes;
import com.azure.resourcemanager.resiliencemanagement.models.FailoverRequest;
import com.azure.resourcemanager.resiliencemanagement.models.FailoverRequestProperties;
import java.util.Arrays;

/**
 * Samples for DrillRuns FailOver.
 */
public final class DrillRunsFailOverSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/DrillRuns_FailOver_MaximumSet_Gen.json
     */
    /**
     * Sample code: DrillRuns_FailOver_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void drillRunsFailOverMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.drillRuns()
            .failOver("sampleServiceGroupName", "qmn", "drill1", "ca92602e-53bf-43d2-ae62-d3fc940474b3",
                new DrillRunFailoverRequest().withAutoFailover(AutoFailover.ENABLE)
                    .withFailoverProperties(
                        new FailoverRequest().withFailoverDirection(FailoverDirectionTypes.FROM_SPECIFIC_LOCATIONS)
                            .withFailoverRequestProperties(
                                new FailoverRequestProperties().withSourceLocations(Arrays.asList("westus")))),
                com.azure.core.util.Context.NONE);
    }
}
```

### DrillRuns_Get

```java
/**
 * Samples for DrillRuns Get.
 */
public final class DrillRunsGetSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/DrillRuns_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: DrillRuns_Get_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void
        drillRunsGetMaximumSet(com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.drillRuns()
            .getWithResponse("sampleServiceGroupName", "drill1", "ca92602e-53bf-43d2-ae62-d3fc940474b3",
                com.azure.core.util.Context.NONE);
    }
}
```

### DrillRuns_List

```java
/**
 * Samples for DrillRuns List.
 */
public final class DrillRunsListSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/DrillRuns_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: DrillRuns_List_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void
        drillRunsListMaximumSet(com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.drillRuns().list("sampleServiceGroupName", "drill1", com.azure.core.util.Context.NONE);
    }
}
```

### DrillRuns_MarkAsComplete

```java
import com.azure.resourcemanager.resiliencemanagement.models.DrillRunSubtasks;
import com.azure.resourcemanager.resiliencemanagement.models.MarkAsCompleteRequest;

/**
 * Samples for DrillRuns MarkAsComplete.
 */
public final class DrillRunsMarkAsCompleteSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/DrillRuns_MarkAsComplete_MaximumSet_Gen.json
     */
    /**
     * Sample code: DrillRuns_MarkAsComplete_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void drillRunsMarkAsCompleteMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.drillRuns()
            .markAsComplete("sampleServiceGroupName", "qmn", "drill1", "ca92602e-53bf-43d2-ae62-d3fc940474b3",
                new MarkAsCompleteRequest().withDrillRunStage(DrillRunSubtasks.fromString("Fault")),
                com.azure.core.util.Context.NONE);
    }
}
```

### DrillRuns_Reprotect

```java
/**
 * Samples for DrillRuns Reprotect.
 */
public final class DrillRunsReprotectSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/DrillRuns_Reprotect_MaximumSet_Gen.json
     */
    /**
     * Sample code: DrillRuns_Reprotect_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void drillRunsReprotectMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.drillRuns()
            .reprotect("sampleServiceGroupName", "qmn", "drill1", "ca92602e-53bf-43d2-ae62-d3fc940474b3",
                com.azure.core.util.Context.NONE);
    }
}
```

### DrillRuns_Resume

```java
/**
 * Samples for DrillRuns Resume.
 */
public final class DrillRunsResumeSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/DrillRuns_Resume_MaximumSet_Gen.json
     */
    /**
     * Sample code: DrillRuns_Resume_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void
        drillRunsResumeMaximumSet(com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.drillRuns()
            .resume("sampleServiceGroupName", "qmn", "drill1", "ca92602e-53bf-43d2-ae62-d3fc940474b3",
                com.azure.core.util.Context.NONE);
    }
}
```

### Drills_AddOrUpdateResources

```java
import com.azure.resourcemanager.resiliencemanagement.models.AddOrUpdateResourcesRequest;
import com.azure.resourcemanager.resiliencemanagement.models.CustomFaultDetails;
import com.azure.resourcemanager.resiliencemanagement.models.FaultDetails;
import com.azure.resourcemanager.resiliencemanagement.models.FaultProperties;
import com.azure.resourcemanager.resiliencemanagement.models.ForceInclusionAndUpdate;
import com.azure.resourcemanager.resiliencemanagement.models.IncludeOrUpdateResource;
import com.azure.resourcemanager.resiliencemanagement.models.ResourceLists;
import java.util.Arrays;

/**
 * Samples for Drills AddOrUpdateResources.
 */
public final class DrillsAddOrUpdateResourcesSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/Drills_AddOrUpdateResources_MaximumSet_Gen.json
     */
    /**
     * Sample code: Drills_AddOrUpdateResources_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void drillsAddOrUpdateResourcesMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.drills()
            .addOrUpdateResources("sampleServiceGroupName", "qmn", "drill1", new AddOrUpdateResourcesRequest()
                .withFaultDurationInMin(0)
                .withResourceLists(new ResourceLists().withIncludeResources(Arrays.asList(new IncludeOrUpdateResource()
                    .withId(
                        "/providers/Microsoft.Management/serviceGroups/sampleServiceGroupName/providers/Microsoft.AzureResilienceManagement/drills/drill1/drillResources/2c9b3a1f-f96e-42c2-98fe-15005da8a133")
                    .withFaultProperties(new FaultProperties().withOverriddenDefaultFault(new FaultDetails()
                        .withFaultUrn("urn:csci:microsoft:virtualMachine:shutdown/1.0")
                        .withFaultName("shutdown")
                        .withTargetResourceId(
                            "/subscriptions/f2edfd5d-5496-4683-b94f-b3588c579009/resourceGroups/testRG/providers/Microsoft.Compute/virtualMachines/vm1"))
                        .withCustomFault(new CustomFaultDetails().withFaultName(
                            "umofuzwgczqwyzcoakmrdrkjknykdonhypxibwrweggltsmjayvnlzroxdfalwkfsqvuqtfwhhzcnemndbgxdiciqs")
                            .withScriptResourceId(
                                "/subscriptions/191973cd-9c54-41e0-ac19-25dd9a92d5a8/resourceGroups/abhinkRG/providers/Microsoft.Automation/automationAccounts/abhinkAcc/runbooks/viveksi")))))
                    .withExcludeResources(Arrays.asList(
                        "/providers/Microsoft.Management/serviceGroups/sampleServiceGroupName/providers/Microsoft.AzureResilienceManagement/drills/drill1/drillResources/c2191964-be24-4849-8faf-d9569576c708"))
                    .withUpdateResources(Arrays.asList(new IncludeOrUpdateResource().withId(
                        "/providers/Microsoft.Management/serviceGroups/sampleServiceGroupName/providers/Microsoft.AzureResilienceManagement/drills/drill1/drillResources/c26bea42-c34c-4e6f-8cf4-15043e18c8bc")
                        .withFaultProperties(new FaultProperties().withOverriddenDefaultFault(new FaultDetails()
                            .withFaultUrn("urn:csci:microsoft:virtualMachine:shutdown/1.0")
                            .withFaultName("shutdown")
                            .withTargetResourceId(
                                "/subscriptions/f2edfd5d-5496-4683-b94f-b3588c579009/resourceGroups/testRG/providers/Microsoft.Compute/virtualMachines/vm1"))
                            .withCustomFault(new CustomFaultDetails().withFaultName(
                                "umofuzwgczqwyzcoakmrdrkjknykdonhypxibwrweggltsmjayvnlzroxdfalwkfsqvuqtfwhhzcnemndbgxdiciqs")
                                .withScriptResourceId(
                                    "/subscriptions/191973cd-9c54-41e0-ac19-25dd9a92d5a8/resourceGroups/abhinkRG/providers/Microsoft.Automation/automationAccounts/abhinkAcc/runbooks/viveksi"))))))
                .withForceInclusionAndUpdate(ForceInclusionAndUpdate.ENABLE), com.azure.core.util.Context.NONE);
    }
}
```

### Drills_Create

```java
import com.azure.resourcemanager.resiliencemanagement.fluent.models.DrillInner;
import com.azure.resourcemanager.resiliencemanagement.models.AssetPropertiesOfDrill;
import com.azure.resourcemanager.resiliencemanagement.models.AssociatedIdentity;
import com.azure.resourcemanager.resiliencemanagement.models.ChaosResourcePropertiesOfDrill;
import com.azure.resourcemanager.resiliencemanagement.models.DrillProperties;
import com.azure.resourcemanager.resiliencemanagement.models.ManagedServiceIdentity;
import com.azure.resourcemanager.resiliencemanagement.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.resiliencemanagement.models.MonitoringPropertiesOfDrill;
import com.azure.resourcemanager.resiliencemanagement.models.RBACSetupMode;
import com.azure.resourcemanager.resiliencemanagement.models.RecoveryPlanPropertiesOfDrill;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Drills Create.
 */
public final class DrillsCreateSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/Drills_Create_MaximumSet_Gen.json
     */
    /**
     * Sample code: Drills_Create_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void
        drillsCreateMaximumSet(com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.drills()
            .create("sampleServiceGroupName", "drill1", new DrillInner().withProperties(new DrillProperties()
                .withRecoveryPlanProperties(new RecoveryPlanPropertiesOfDrill().withIdentity(new AssociatedIdentity()
                    .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentity(
                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")))
                .withDrillAssetProperties(
                    new AssetPropertiesOfDrill().withSubscription("4e88bed3-114f-443d-9975-28f64122ec5e")
                        .withRegion("eastus")
                        .withResourceGroup("customDrillResourceGroup"))
                .withChaosResourceProperties(new ChaosResourcePropertiesOfDrill().withIdentity(new AssociatedIdentity()
                    .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentity(
                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1"))
                    .withChaosResourceIdentityForFaults(new AssociatedIdentity()
                        .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                        .withUserAssignedIdentity(
                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")))
                .withRbacSetupMode(RBACSetupMode.AUTOMATED_CUSTOM_ROLE)
                .withMonitoringProperties(new MonitoringPropertiesOfDrill().withIdentity(new AssociatedIdentity()
                    .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentity(
                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1"))))
                .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.NONE)
                    .withUserAssignedIdentities(mapOf())),
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

### Drills_Delete

```java
/**
 * Samples for Drills Delete.
 */
public final class DrillsDeleteSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/Drills_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Drills_Delete_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void
        drillsDeleteMaximumSet(com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.drills().delete("sampleServiceGroupName", "drill1", com.azure.core.util.Context.NONE);
    }
}
```

### Drills_End

```java
import com.azure.resourcemanager.resiliencemanagement.models.DrillAttestation;
import com.azure.resourcemanager.resiliencemanagement.models.DrillEndRequest;

/**
 * Samples for Drills End.
 */
public final class DrillsEndSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/Drills_End_MaximumSet_Gen.json
     */
    /**
     * Sample code: Drills_End_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void
        drillsEndMaximumSet(com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.drills()
            .end("sampleServiceGroupName", "qmn", "drill1",
                new DrillEndRequest().withAttestation(DrillAttestation.ATTESTED_SUCCESS)
                    .withAttestationNotes("ycnqvrgduotohgycsapckhixwqwgp"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Drills_Get

```java
/**
 * Samples for Drills Get.
 */
public final class DrillsGetSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/Drills_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Drills_Get_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void
        drillsGetMaximumSet(com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.drills().getWithResponse("sampleServiceGroupName", "drill1", com.azure.core.util.Context.NONE);
    }
}
```

### Drills_List

```java
/**
 * Samples for Drills List.
 */
public final class DrillsListSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/Drills_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Drills_List_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void
        drillsListMaximumSet(com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.drills().list("sampleServiceGroupName", "xntbyoswztnmvitj", 69, com.azure.core.util.Context.NONE);
    }
}
```

### Drills_ResyncReadinessCheck

```java
/**
 * Samples for Drills ResyncReadinessCheck.
 */
public final class DrillsResyncReadinessCheckSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/Drills_ResyncReadinessCheck_MaximumSet_Gen.json
     */
    /**
     * Sample code: Drills_ResyncReadinessCheck_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void drillsResyncReadinessCheckMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.drills()
            .resyncReadinessCheck("sampleServiceGroupName", "qmn", "drill1", com.azure.core.util.Context.NONE);
    }
}
```

### Drills_Start

```java
import com.azure.resourcemanager.resiliencemanagement.models.DrillMode;
import com.azure.resourcemanager.resiliencemanagement.models.DrillStartRequest;

/**
 * Samples for Drills Start.
 */
public final class DrillsStartSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/Drills_Start_MaximumSet_Gen.json
     */
    /**
     * Sample code: Drills_Start_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void
        drillsStartMaximumSet(com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.drills()
            .start("sampleServiceGroupName", "qmn", "drill1", new DrillStartRequest().withMode(DrillMode.FAILOVER),
                com.azure.core.util.Context.NONE);
    }
}
```

### Drills_Update

```java
import com.azure.resourcemanager.resiliencemanagement.models.AssetPropertiesOfDrill;
import com.azure.resourcemanager.resiliencemanagement.models.AssociatedIdentity;
import com.azure.resourcemanager.resiliencemanagement.models.ChaosResourcePropertiesOfDrill;
import com.azure.resourcemanager.resiliencemanagement.models.DrillUpdate;
import com.azure.resourcemanager.resiliencemanagement.models.DrillUpdateProperties;
import com.azure.resourcemanager.resiliencemanagement.models.ManagedServiceIdentity;
import com.azure.resourcemanager.resiliencemanagement.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.resiliencemanagement.models.MonitoringPropertiesOfDrill;
import com.azure.resourcemanager.resiliencemanagement.models.RBACSetupMode;
import com.azure.resourcemanager.resiliencemanagement.models.RecoveryPlanPropertiesOfDrill;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Drills Update.
 */
public final class DrillsUpdateSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/Drills_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: Drills_Update_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void
        drillsUpdateMaximumSet(com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.drills()
            .update("sampleServiceGroupName", "drill1", new DrillUpdate()
                .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.NONE)
                    .withUserAssignedIdentities(mapOf()))
                .withProperties(new DrillUpdateProperties().withRecoveryPlanProperties(
                    new RecoveryPlanPropertiesOfDrill().withIdentity(new AssociatedIdentity()
                        .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                        .withUserAssignedIdentity(
                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")))
                    .withDrillAssetProperties(new AssetPropertiesOfDrill().withSubscription("pxlmwjuhcif")
                        .withRegion("zuvwzxnbqyzdkthrewruw"))
                    .withChaosResourceProperties(new ChaosResourcePropertiesOfDrill()
                        .withIdentity(new AssociatedIdentity().withType(ManagedServiceIdentityType.USER_ASSIGNED)
                            .withUserAssignedIdentity(
                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1"))
                        .withChaosResourceIdentityForFaults(new AssociatedIdentity()
                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                            .withUserAssignedIdentity(
                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")))
                    .withRbacSetupMode(RBACSetupMode.AUTOMATED_CUSTOM_ROLE)
                    .withMonitoringProperties(new MonitoringPropertiesOfDrill().withIdentity(new AssociatedIdentity()
                        .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                        .withUserAssignedIdentity(
                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")))),
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

### Drills_ValidateForExecution

```java
import com.azure.resourcemanager.resiliencemanagement.models.ValidateForExecutionProperties;
import com.azure.resourcemanager.resiliencemanagement.models.ValidateForExecutionRequest;
import java.util.Arrays;

/**
 * Samples for Drills ValidateForExecution.
 */
public final class DrillsValidateForExecutionSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/Drills_ValidateForExecution_MaximumSet_Gen.json
     */
    /**
     * Sample code: Drills_ValidateForExecution_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void drillsValidateForExecutionMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.drills()
            .validateForExecution("sampleServiceGroupName", "qmn", "drill1",
                new ValidateForExecutionRequest().withValidateForExecutionProperties(
                    new ValidateForExecutionProperties().withSourceLocations(Arrays.asList("eastus2-az1"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### Enrollments_CreateOrUpdate

```java
import com.azure.resourcemanager.resiliencemanagement.models.EnrollmentProperties;

/**
 * Samples for Enrollments CreateOrUpdate.
 */
public final class EnrollmentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/Enrollments_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: Enrollments_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void enrollmentsCreateOrUpdateMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.enrollments()
            .define("sg1-enrollment")
            .withExistingUsagePlan("MyResourceGroup", "myUsagePlan")
            .withProperties(
                new EnrollmentProperties().withServiceGroupId("/providers/Microsoft.Management/serviceGroups/sg1"))
            .create();
    }
}
```

### Enrollments_Delete

```java
/**
 * Samples for Enrollments Delete.
 */
public final class EnrollmentsDeleteSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/Enrollments_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Enrollments_Delete_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void enrollmentsDeleteMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.enrollments()
            .delete("MyResourceGroup", "myUsagePlan", "sg1-enrollment", com.azure.core.util.Context.NONE);
    }
}
```

### Enrollments_Get

```java
/**
 * Samples for Enrollments Get.
 */
public final class EnrollmentsGetSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/Enrollments_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Enrollments_Get_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void
        enrollmentsGetMaximumSet(com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.enrollments()
            .getWithResponse("MyResourceGroup", "myUsagePlan", "sg1-enrollment", com.azure.core.util.Context.NONE);
    }
}
```

### Enrollments_List

```java
/**
 * Samples for Enrollments List.
 */
public final class EnrollmentsListSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/Enrollments_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Enrollments_List_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void
        enrollmentsListMaximumSet(com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.enrollments().list("MyResourceGroup", "myUsagePlan", com.azure.core.util.Context.NONE);
    }
}
```

### GoalAssignments_CreateOrUpdate

```java
import com.azure.resourcemanager.resiliencemanagement.fluent.models.GoalAssignmentInner;
import com.azure.resourcemanager.resiliencemanagement.models.GoalAssignmentProperties;
import com.azure.resourcemanager.resiliencemanagement.models.GoalAssignmentType;
import com.azure.resourcemanager.resiliencemanagement.models.ServiceLevelResource;
import java.util.Arrays;

/**
 * Samples for GoalAssignments CreateOrUpdate.
 */
public final class GoalAssignmentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/GoalAssignments_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: GoalAssignments_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void goalAssignmentsCreateOrUpdateMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.goalAssignments()
            .createOrUpdate("sg1", "ga1", new GoalAssignmentInner().withProperties(new GoalAssignmentProperties()
                .withGoalTemplateId("/providers/Microsoft.AzureResilienceManagement/goaltemplates/gt1")
                .withGoalAssignmentType(GoalAssignmentType.RESILIENCY)
                .withServiceLevelResources(Arrays.asList(new ServiceLevelResource().withServiceLevelIndicatorResourceId(
                    "/subscriptions/12345678-1234-1234-1234-123456789012/resourceGroups/MyResourceGroup/providers/Microsoft.Compute/virtualMachines/MyVirtualMachine")
                    .withServiceLevelObjectiveResourceId(
                        "/subscriptions/12345678-1234-1234-1234-123456789012/resourceGroups/MyResourceGroup/providers/Microsoft.Compute/virtualMachines/MyVirtualMachine")))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-04-01-preview/GoalAssignments_CreateOrUpdate_MinimumSet_Gen.json
     */
    /**
     * Sample code: GoalAssignments_CreateOrUpdate_MinimumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void goalAssignmentsCreateOrUpdateMinimumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.goalAssignments()
            .createOrUpdate("sg1", "ga1",
                new GoalAssignmentInner().withProperties(new GoalAssignmentProperties()
                    .withGoalTemplateId("/providers/Microsoft.AzureResilienceManagement/goaltemplates/gt1")
                    .withGoalAssignmentType(GoalAssignmentType.RESILIENCY)),
                com.azure.core.util.Context.NONE);
    }
}
```

### GoalAssignments_Delete

```java
/**
 * Samples for GoalAssignments Delete.
 */
public final class GoalAssignmentsDeleteSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/GoalAssignments_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: GoalAssignments_Delete_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void goalAssignmentsDeleteMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.goalAssignments().delete("sg1", "ga1", com.azure.core.util.Context.NONE);
    }
}
```

### GoalAssignments_Get

```java
/**
 * Samples for GoalAssignments Get.
 */
public final class GoalAssignmentsGetSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/GoalAssignments_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: GoalAssignments_Get_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void goalAssignmentsGetMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.goalAssignments().getWithResponse("sg1", "ga1", com.azure.core.util.Context.NONE);
    }
}
```

### GoalAssignments_List

```java
/**
 * Samples for GoalAssignments List.
 */
public final class GoalAssignmentsListSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/GoalAssignments_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: GoalAssignments_List_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void goalAssignmentsListMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.goalAssignments().list("zldmpkvqzifygkqau", "xntbyoswztnmvitj", 69, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-04-01-preview/GoalAssignments_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: GoalAssignments_List_MinimumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void goalAssignmentsListMinimumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.goalAssignments().list("sg1", null, null, com.azure.core.util.Context.NONE);
    }
}
```

### GoalAssignments_RecommendCapacity

```java
import com.azure.resourcemanager.resiliencemanagement.models.RecommendCapacityRequest;
import java.util.Arrays;

/**
 * Samples for GoalAssignments RecommendCapacity.
 */
public final class GoalAssignmentsRecommendCapacitySamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/GoalAssignments_RecommendCapacity_MaximumSet_Gen.json
     */
    /**
     * Sample code: GoalAssignments_RecommendCapacity_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void goalAssignmentsRecommendCapacityMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.goalAssignments()
            .recommendCapacity("sg1", "ga1", new RecommendCapacityRequest().withResourceIds(Arrays.asList(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myRg/providers/Microsoft.Compute/virtualMachines/vm1",
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myRg/providers/Microsoft.Storage/storageAccounts/sa1")),
                com.azure.core.util.Context.NONE);
    }
}
```

### GoalAssignments_RefreshGoalResources

```java
/**
 * Samples for GoalAssignments RefreshGoalResources.
 */
public final class GoalAssignmentsRefreshGoalResourcesSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/GoalAssignments_RefreshGoalResources_MaximumSet_Gen.json
     */
    /**
     * Sample code: GoalAssignments_RefreshGoalResources_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void goalAssignmentsRefreshGoalResourcesMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.goalAssignments().refreshGoalResources("sg1", "ga1", com.azure.core.util.Context.NONE);
    }
}
```

### GoalAssignments_Update

```java
import com.azure.resourcemanager.resiliencemanagement.fluent.models.GoalAssignmentInner;
import com.azure.resourcemanager.resiliencemanagement.models.GoalAssignmentProperties;
import com.azure.resourcemanager.resiliencemanagement.models.GoalAssignmentType;
import com.azure.resourcemanager.resiliencemanagement.models.ServiceLevelResource;
import java.util.Arrays;

/**
 * Samples for GoalAssignments Update.
 */
public final class GoalAssignmentsUpdateSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/GoalAssignments_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: GoalAssignments_Update_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void goalAssignmentsUpdateMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.goalAssignments()
            .update("sg1", "ga1", new GoalAssignmentInner().withProperties(new GoalAssignmentProperties()
                .withGoalTemplateId("/providers/Microsoft.AzureResilienceManagement/goaltemplates/gt1")
                .withGoalAssignmentType(GoalAssignmentType.RESILIENCY)
                .withServiceLevelResources(Arrays.asList(new ServiceLevelResource().withServiceLevelIndicatorResourceId(
                    "/subscriptions/12345678-1234-1234-1234-123456789012/resourceGroups/MyResourceGroup/providers/Microsoft.Compute/virtualMachines/MyVirtualMachine")
                    .withServiceLevelObjectiveResourceId(
                        "/subscriptions/12345678-1234-1234-1234-123456789012/resourceGroups/MyResourceGroup/providers/Microsoft.Compute/virtualMachines/MyVirtualMachine")))),
                com.azure.core.util.Context.NONE);
    }
}
```

### GoalAssignments_UpdateGoalResources

```java
import com.azure.resourcemanager.resiliencemanagement.fluent.models.GoalResourceInner;
import com.azure.resourcemanager.resiliencemanagement.models.AttestationState;
import com.azure.resourcemanager.resiliencemanagement.models.ExclusionState;
import com.azure.resourcemanager.resiliencemanagement.models.GoalResourceProperties;
import com.azure.resourcemanager.resiliencemanagement.models.UpdateGoalResourceRequest;
import java.util.Arrays;

/**
 * Samples for GoalAssignments UpdateGoalResources.
 */
public final class GoalAssignmentsUpdateGoalResourcesSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/GoalAssignments_UpdateGoalResources_MaximumSet_Gen.json
     */
    /**
     * Sample code: GoalAssignments_UpdateGoalResources_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void goalAssignmentsUpdateGoalResourcesMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.goalAssignments()
            .updateGoalResources("sg1", "ga1", new UpdateGoalResourceRequest().withResources(Arrays.asList(
                new GoalResourceInner().withProperties(new GoalResourceProperties().withResourceArmId(
                    "/subscriptions/12345678-1234-1234-1234-123456789012/resourceGroups/MyResourceGroup/providers/Microsoft.Compute/virtualMachines/MyVirtualMachine")
                    .withHighAvailabilityGoalParticipation(ExclusionState.EXCLUDED)
                    .withHighAvailabilityAttestationStatus(AttestationState.MANUALLY_ATTESTED)
                    .withDisasterRecoveryGoalParticipation(ExclusionState.EXCLUDED)
                    .withDisasterRecoveryAttestationStatus(AttestationState.MANUALLY_ATTESTED)),
                new GoalResourceInner().withProperties(new GoalResourceProperties().withResourceArmId(
                    "/subscriptions/12345678-1234-1234-1234-123456789012/resourceGroups/MyResourceGroup/providers/Microsoft.Compute/virtualMachines/MyVirtualMachine1")
                    .withHighAvailabilityGoalParticipation(ExclusionState.EXCLUDED)
                    .withHighAvailabilityAttestationStatus(AttestationState.MANUALLY_ATTESTED)
                    .withDisasterRecoveryGoalParticipation(ExclusionState.EXCLUDED)
                    .withDisasterRecoveryAttestationStatus(AttestationState.MANUALLY_ATTESTED)))),
                com.azure.core.util.Context.NONE);
    }
}
```

### GoalResources_Get

```java
/**
 * Samples for GoalResources Get.
 */
public final class GoalResourcesGetSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/GoalResources_Get_Complete_Example.json
     */
    /**
     * Sample code: GoalResources_Get_Complete_Example.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void goalResourcesGetCompleteExample(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.goalResources()
            .getWithResponse("production-sg", "resiliencyGoalAssignment", "web-app-resource",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-04-01-preview/GoalResources_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: GoalResources_Get_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void
        goalResourcesGetMaximumSet(com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.goalResources()
            .getWithResponse("umyghwnfpzsgrhpczizcn", "ga1", "gr1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-04-01-preview/GoalResources_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: GoalResources_Get_MinimumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void
        goalResourcesGetMinimumSet(com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.goalResources().getWithResponse("sg1", "ga1", "gr1", com.azure.core.util.Context.NONE);
    }
}
```

### GoalResources_List

```java
/**
 * Samples for GoalResources List.
 */
public final class GoalResourcesListSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/GoalResources_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: GoalResources_List_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void goalResourcesListMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.goalResources().list("sg1", "ga1", "xntbyoswztnmvitj", 69, com.azure.core.util.Context.NONE);
    }
}
```

### GoalTemplates_CreateOrUpdate

```java
import com.azure.resourcemanager.resiliencemanagement.fluent.models.GoalTemplateInner;
import com.azure.resourcemanager.resiliencemanagement.models.GoalTemplateProperties;
import com.azure.resourcemanager.resiliencemanagement.models.GoalType;
import com.azure.resourcemanager.resiliencemanagement.models.RequirementSelected;

/**
 * Samples for GoalTemplates CreateOrUpdate.
 */
public final class GoalTemplatesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/GoalTemplates_CreateOrUpdate_MinimumSet_Gen.json
     */
    /**
     * Sample code: GoalTemplates_CreateOrUpdate_MinimumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void goalTemplatesCreateOrUpdateMinimumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.goalTemplates()
            .createOrUpdate("sg1", "gt1",
                new GoalTemplateInner().withProperties(new GoalTemplateProperties().withGoalType(GoalType.RESILIENCY)),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-04-01-preview/GoalTemplates_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: GoalTemplates_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void goalTemplatesCreateOrUpdateMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.goalTemplates()
            .createOrUpdate("zumt", "gt1",
                new GoalTemplateInner().withProperties(
                    new GoalTemplateProperties().withRequireHighAvailability(RequirementSelected.REQUIRED)
                        .withRequireDisasterRecovery(RequirementSelected.NOT_REQUIRED)
                        .withRegionalRecoveryPointObjective("PT15M")
                        .withRegionalRecoveryTimeObjective("PT30M")
                        .withGoalType(GoalType.RESILIENCY)),
                com.azure.core.util.Context.NONE);
    }
}
```

### GoalTemplates_Delete

```java
/**
 * Samples for GoalTemplates Delete.
 */
public final class GoalTemplatesDeleteSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/GoalTemplates_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: GoalTemplates_Delete_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void goalTemplatesDeleteMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.goalTemplates().delete("ajsvdpsdgp", "gt1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-04-01-preview/GoalTemplates_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: GoalTemplates_Delete_MinimumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void goalTemplatesDeleteMinimumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.goalTemplates().delete("sg1", "gt1", com.azure.core.util.Context.NONE);
    }
}
```

### GoalTemplates_Get

```java
/**
 * Samples for GoalTemplates Get.
 */
public final class GoalTemplatesGetSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/GoalTemplates_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: GoalTemplates_Get_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void
        goalTemplatesGetMaximumSet(com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.goalTemplates().getWithResponse("qsqjquhxpermcblvegajq", "gt1", com.azure.core.util.Context.NONE);
    }
}
```

### GoalTemplates_List

```java
/**
 * Samples for GoalTemplates List.
 */
public final class GoalTemplatesListSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/GoalTemplates_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: GoalTemplates_List_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void goalTemplatesListMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.goalTemplates().list("vmmacokmkuxzy", "xntbyoswztnmvitj", 69, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-04-01-preview/GoalTemplates_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: GoalTemplates_List_MinimumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void goalTemplatesListMinimumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.goalTemplates().list("sg1", null, null, com.azure.core.util.Context.NONE);
    }
}
```

### GoalTemplates_Update

```java
import com.azure.resourcemanager.resiliencemanagement.fluent.models.GoalTemplateInner;
import com.azure.resourcemanager.resiliencemanagement.models.GoalTemplateProperties;
import com.azure.resourcemanager.resiliencemanagement.models.GoalType;
import com.azure.resourcemanager.resiliencemanagement.models.RequirementSelected;

/**
 * Samples for GoalTemplates Update.
 */
public final class GoalTemplatesUpdateSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/GoalTemplates_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: GoalTemplates_Update_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void goalTemplatesUpdateMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.goalTemplates()
            .update("ipvrpvfcsfwltkmalhklsyg", "gt1",
                new GoalTemplateInner().withProperties(
                    new GoalTemplateProperties().withRequireHighAvailability(RequirementSelected.REQUIRED)
                        .withRequireDisasterRecovery(RequirementSelected.NOT_REQUIRED)
                        .withRegionalRecoveryPointObjective("PT15M")
                        .withRegionalRecoveryTimeObjective("PT30M")
                        .withGoalType(GoalType.RESILIENCY)),
                com.azure.core.util.Context.NONE);
    }
}
```

### OperationStatus_Get

```java
/**
 * Samples for OperationStatus Get.
 */
public final class OperationStatusGetSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/OperationStatus_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: OperationStatus_Get_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void operationStatusGetMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.operationStatus()
            .getWithResponse("eastus", "12345678-1234-1234-1234-123456789012", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-04-01-preview/Operations_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_MinimumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void
        operationsListMinimumSet(com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-04-01-preview/Operations_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void
        operationsListMaximumSet(com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### RecoveryJobResources_Get

```java
/**
 * Samples for RecoveryJobResources Get.
 */
public final class RecoveryJobResourcesGetSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/RecoveryJobResources_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: RecoveryJobResources_Get_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void recoveryJobResourcesGetMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.recoveryJobResources()
            .getWithResponse("sampleServiceGroupName", "samplePlanName", "c56888ef-9ced-4001-a6d4-7145a0309bdb",
                "56f942da-a30e-43c0-b5f0-1c22e44f2d94", com.azure.core.util.Context.NONE);
    }
}
```

### RecoveryJobResources_List

```java
/**
 * Samples for RecoveryJobResources List.
 */
public final class RecoveryJobResourcesListSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/RecoveryJobResources_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: RecoveryJobResources_List_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void recoveryJobResourcesListMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.recoveryJobResources()
            .list("sampleServiceGroupName", "samplePlanName", "c56888ef-9ced-4001-a6d4-7145a0309bdb",
                com.azure.core.util.Context.NONE);
    }
}
```

### RecoveryJobs_Cancel

```java
import com.azure.resourcemanager.resiliencemanagement.models.RecoveryActionRequest;

/**
 * Samples for RecoveryJobs Cancel.
 */
public final class RecoveryJobsCancelSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/RecoveryJobs_Cancel_MaximumSet_Gen.json
     */
    /**
     * Sample code: RecoveryJobs_Cancel_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void recoveryJobsCancelMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.recoveryJobs()
            .cancel("sampleServiceGroup", null, "samplePlanName", "c56888ef-9ced-4001-a6d4-7145a0309bdb",
                new RecoveryActionRequest().withDescription("Cancelling the recovery job due to user request"),
                com.azure.core.util.Context.NONE);
    }
}
```

### RecoveryJobs_Get

```java
/**
 * Samples for RecoveryJobs Get.
 */
public final class RecoveryJobsGetSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/RecoveryJobs_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: RecoveryJobs_Get_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void
        recoveryJobsGetMaximumSet(com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.recoveryJobs()
            .getWithResponse("sampleServiceGroupName", "samplePlanName", "c56888ef-9ced-4001-a6d4-7145a0309bdb",
                com.azure.core.util.Context.NONE);
    }
}
```

### RecoveryJobs_List

```java
/**
 * Samples for RecoveryJobs List.
 */
public final class RecoveryJobsListSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/RecoveryJobs_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: RecoveryJobs_List_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void
        recoveryJobsListMaximumSet(com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.recoveryJobs().list("sampleServiceGroupName", "samplePlanName", com.azure.core.util.Context.NONE);
    }
}
```

### RecoveryJobs_Resume

```java
import com.azure.resourcemanager.resiliencemanagement.models.RecoveryActionRequest;

/**
 * Samples for RecoveryJobs Resume.
 */
public final class RecoveryJobsResumeSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/RecoveryJobs_Resume_MaximumSet_Gen.json
     */
    /**
     * Sample code: RecoveryJobs_Resume_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void recoveryJobsResumeMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.recoveryJobs()
            .resume("sampleServiceGroupName", null, "samplePlanName", "c56888ef-9ced-4001-a6d4-7145a0309bdb",
                new RecoveryActionRequest().withDescription("Resuming the recovery job after user verification"),
                com.azure.core.util.Context.NONE);
    }
}
```

### RecoveryJobs_Retry

```java
/**
 * Samples for RecoveryJobs Retry.
 */
public final class RecoveryJobsRetrySamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/RecoveryJobs_Retry_MaximumSet_Gen.json
     */
    /**
     * Sample code: RecoveryJobs_Retry_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void recoveryJobsRetryMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.recoveryJobs()
            .retry("sampleServiceGroupName", null, "samplePlanName", "c56888ef-9ced-4001-a6d4-7145a0309bdb",
                com.azure.core.util.Context.NONE);
    }
}
```

### RecoveryPlanActions_CheckReadiness

```java
/**
 * Samples for RecoveryPlanActions CheckReadiness.
 */
public final class RecoveryPlanActionsCheckReadinessSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/RecoveryPlanActions_CheckReadiness_MaximumSet_Gen.json
     */
    /**
     * Sample code: RecoveryPlanActions_CheckReadiness_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void recoveryPlanActionsCheckReadinessMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.recoveryPlanActions()
            .checkReadiness("sampleServiceGroupName", "qmn", "samplePlanName", com.azure.core.util.Context.NONE);
    }
}
```

### RecoveryPlanActions_Failover

```java
import com.azure.resourcemanager.resiliencemanagement.models.ExecutionConfigurations;
import com.azure.resourcemanager.resiliencemanagement.models.FailoverDirectionTypes;
import com.azure.resourcemanager.resiliencemanagement.models.FailoverRequest;
import com.azure.resourcemanager.resiliencemanagement.models.FailoverRequestProperties;
import com.azure.resourcemanager.resiliencemanagement.models.UserConsent;
import java.util.Arrays;

/**
 * Samples for RecoveryPlanActions Failover.
 */
public final class RecoveryPlanActionsFailoverSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/RecoveryPlanActions_Failover_MaximumSet_Gen.json
     */
    /**
     * Sample code: RecoveryPlanActions_Failover_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void recoveryPlanActionsFailoverMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.recoveryPlanActions()
            .failover("sampleServiceGroupName", null, "samplePlanName", new FailoverRequest()
                .withFailoverDirection(FailoverDirectionTypes.FROM_SPECIFIC_LOCATIONS)
                .withFailoverRequestProperties(new FailoverRequestProperties()
                    .withSourceLocations(Arrays.asList("westus"))
                    .withSelectedResourceIds(Arrays.asList(
                        "/providers/Microsoft.Management/serviceGroups/sampleServiceGroupName/providers/Microsoft.AzureResilienceManagement/recoveryPlans/samplePlanName/recoveryResources/12345678-9012-3456-7890-123456789012"))
                    .withExecutionConfigurations(new ExecutionConfigurations().withUserConsent(UserConsent.ALLOWED))),
                com.azure.core.util.Context.NONE);
    }
}
```

### RecoveryPlanActions_FailoverCommit

```java
/**
 * Samples for RecoveryPlanActions FailoverCommit.
 */
public final class RecoveryPlanActionsFailoverCommitSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/RecoveryPlanActions_FailoverCommit_MaximumSet_Gen.json
     */
    /**
     * Sample code: RecoveryPlanActions_FailoverCommit_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void recoveryPlanActionsFailoverCommitMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.recoveryPlanActions()
            .failoverCommit("sampleServiceGroupName", null, "samplePlanName", com.azure.core.util.Context.NONE);
    }
}
```

### RecoveryPlanActions_Finalize

```java
/**
 * Samples for RecoveryPlanActions Finalize.
 */
public final class RecoveryPlanActionsFinalizeSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/RecoveryPlanActions_Finalize_MaximumSet_Gen.json
     */
    /**
     * Sample code: RecoveryPlanActions_Finalize_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void recoveryPlanActionsFinalizeMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.recoveryPlanActions()
            .finalize("sampleServiceGroupName", null, "samplePlanName", com.azure.core.util.Context.NONE);
    }
}
```

### RecoveryPlanActions_Reprotect

```java
import com.azure.resourcemanager.resiliencemanagement.models.ReprotectRequest;
import com.azure.resourcemanager.resiliencemanagement.models.ReprotectRequestProperties;
import java.util.Arrays;

/**
 * Samples for RecoveryPlanActions Reprotect.
 */
public final class RecoveryPlanActionsReprotectSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/RecoveryPlanActions_Reprotect_MaximumSet_Gen.json
     */
    /**
     * Sample code: RecoveryPlanActions_Reprotect_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void recoveryPlanActionsReprotectMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.recoveryPlanActions()
            .reprotect("sampleServiceGroupName", null, "samplePlanName", new ReprotectRequest()
                .withReprotectRequestProperties(new ReprotectRequestProperties().withSelectedResourceIds(Arrays.asList(
                    "/providers/Microsoft.Management/serviceGroups/sampleServiceGroupName/providers/Microsoft.AzureResilienceManagement/recoveryPlans/samplePlanName/recoveryResources/12345678-9012-3456-7890-123456789012"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### RecoveryPlanActions_TestFailover

```java
import com.azure.resourcemanager.resiliencemanagement.models.ExecutionConfigurations;
import com.azure.resourcemanager.resiliencemanagement.models.FailoverDirectionTypes;
import com.azure.resourcemanager.resiliencemanagement.models.FailoverRequest;
import com.azure.resourcemanager.resiliencemanagement.models.FailoverRequestProperties;
import com.azure.resourcemanager.resiliencemanagement.models.UserConsent;
import java.util.Arrays;

/**
 * Samples for RecoveryPlanActions TestFailover.
 */
public final class RecoveryPlanActionsTestFailoverSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/RecoveryPlanActions_TestFailover_MaximumSet_Gen.json
     */
    /**
     * Sample code: RecoveryPlanActions_TestFailover_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void recoveryPlanActionsTestFailoverMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.recoveryPlanActions()
            .testFailover("sampleServiceGroupName", "qmn", "samplePlanName", new FailoverRequest()
                .withFailoverDirection(FailoverDirectionTypes.FROM_SPECIFIC_LOCATIONS)
                .withFailoverRequestProperties(new FailoverRequestProperties()
                    .withSourceLocations(Arrays.asList("westus"))
                    .withSelectedResourceIds(Arrays.asList(
                        "/providers/Microsoft.Management/serviceGroups/sampleServiceGroupName/providers/Microsoft.AzureResilienceManagement/recoveryPlans/samplePlanName/recoveryResources/12345678-9012-3456-7890-123456789012"))
                    .withExecutionConfigurations(new ExecutionConfigurations().withUserConsent(UserConsent.ALLOWED))),
                com.azure.core.util.Context.NONE);
    }
}
```

### RecoveryPlanActions_TestFailoverCleanup

```java
import com.azure.resourcemanager.resiliencemanagement.models.TestFailoverCleanupRequest;

/**
 * Samples for RecoveryPlanActions TestFailoverCleanup.
 */
public final class RecoveryPlanActionsTestFailoverCleanupSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/RecoveryPlanActions_TestFailoverCleanup_MaximumSet_Gen.json
     */
    /**
     * Sample code: RecoveryPlanActions_TestFailoverCleanup_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void recoveryPlanActionsTestFailoverCleanupMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.recoveryPlanActions()
            .testFailoverCleanup("sampleServiceGroupName", null, "samplePlanName",
                new TestFailoverCleanupRequest().withComments("Test failover clean-up comments"),
                com.azure.core.util.Context.NONE);
    }
}
```

### RecoveryPlanActions_UpdateResources

```java
import com.azure.resourcemanager.resiliencemanagement.fluent.models.RecoveryResourceInner;
import com.azure.resourcemanager.resiliencemanagement.models.AssociatedIdentity;
import com.azure.resourcemanager.resiliencemanagement.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.resiliencemanagement.models.RecoveryResourceProperties;
import com.azure.resourcemanager.resiliencemanagement.models.ResourceInclusionState;
import com.azure.resourcemanager.resiliencemanagement.models.ResourceNativeProtectionSolutionSetting;
import com.azure.resourcemanager.resiliencemanagement.models.ResourceProtectionSolutionType;
import com.azure.resourcemanager.resiliencemanagement.models.UpdateRecoveryResourcesRequest;
import java.util.Arrays;

/**
 * Samples for RecoveryPlanActions UpdateResources.
 */
public final class RecoveryPlanActionsUpdateResourcesSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/RecoveryPlanActions_UpdateResources_MaximumSet_Gen.json
     */
    /**
     * Sample code: RecoveryPlanActions_UpdateResources_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void recoveryPlanActionsUpdateResourcesMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.recoveryPlanActions()
            .updateResources("sampleServiceGroupName", null, "samplePlanName", new UpdateRecoveryResourcesRequest()
                .withResourcesToUpdate(
                    Arrays.asList(new RecoveryResourceInner().withProperties(new RecoveryResourceProperties()
                        .withRecoveryResourceUniqueId("e2a7b8d1-4c3f-4e2b-9a1c-7f6e2d8b5c4a")
                        .withInclusionState(ResourceInclusionState.INCLUDED)
                        .withSelectedProtectionSolutionType(ResourceProtectionSolutionType.AZURE_NATIVE)
                        .withSelectedProtectionSolutionSetting(new ResourceNativeProtectionSolutionSetting())
                        .withRecoveryGroupId("11111111-1111-1111-1111-123456789012")
                        .withAssociatedIdentity(new AssociatedIdentity()
                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                            .withUserAssignedIdentity(
                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")))))
                .withResourcesToRemove(Arrays.asList(
                    "/providers/Microsoft.Management/serviceGroups/sampleServiceGroupName/providers/Microsoft.AzureResilienceManagement/recoveryPlans/samplePlanName/recoveryResources/12345678-9012-3456-7890-123456789012")),
                com.azure.core.util.Context.NONE);
    }
}
```

### RecoveryPlanActions_ValidateForFailover

```java
import com.azure.resourcemanager.resiliencemanagement.models.FailoverDirectionTypes;
import com.azure.resourcemanager.resiliencemanagement.models.FailoverRequest;
import com.azure.resourcemanager.resiliencemanagement.models.FailoverRequestProperties;
import java.util.Arrays;

/**
 * Samples for RecoveryPlanActions ValidateForFailover.
 */
public final class RecoveryPlanActionsValidateForFailoverSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/RecoveryPlanActions_ValidateForFailover_MaximumSet_Gen.json
     */
    /**
     * Sample code: RecoveryPlanActions_ValidateForFailover_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void recoveryPlanActionsValidateForFailoverMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.recoveryPlanActions()
            .validateForFailover("sampleServiceGroupName", null, "samplePlanName",
                new FailoverRequest().withFailoverDirection(FailoverDirectionTypes.FROM_SPECIFIC_LOCATIONS)
                    .withFailoverRequestProperties(
                        new FailoverRequestProperties().withSourceLocations(Arrays.asList("westus"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### RecoveryPlanActions_ValidateForFailoverCommit

```java
/**
 * Samples for RecoveryPlanActions ValidateForFailoverCommit.
 */
public final class RecoveryPlanActionsValidateForFailoverCommitSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/RecoveryPlanActions_ValidateForFailoverCommit_MaximumSet_Gen.json
     */
    /**
     * Sample code: RecoveryPlanActions_ValidateForFailoverCommit_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void recoveryPlanActionsValidateForFailoverCommitMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.recoveryPlanActions()
            .validateForFailoverCommit("sampleServiceGroupName", null, "samplePlanName",
                com.azure.core.util.Context.NONE);
    }
}
```

### RecoveryPlanActions_ValidateForOperation

```java
import com.azure.resourcemanager.resiliencemanagement.models.RecoveryOperationNames;
import com.azure.resourcemanager.resiliencemanagement.models.ValidateForOperationRequest;

/**
 * Samples for RecoveryPlanActions ValidateForOperation.
 */
public final class RecoveryPlanActionsValidateForOperationSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/RecoveryPlanActions_ValidateForOperation_MaximumSet_Gen.json
     */
    /**
     * Sample code: RecoveryPlanActions_ValidateForOperation_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void recoveryPlanActionsValidateForOperationMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.recoveryPlanActions()
            .validateForOperation("sampleServiceGroupName", null, "samplePlanName",
                new ValidateForOperationRequest().withOperationName(RecoveryOperationNames.FAILOVER),
                com.azure.core.util.Context.NONE);
    }
}
```

### RecoveryPlanActions_ValidateForReprotect

```java
import com.azure.resourcemanager.resiliencemanagement.models.ReprotectRequest;
import com.azure.resourcemanager.resiliencemanagement.models.ReprotectRequestProperties;
import java.util.Arrays;

/**
 * Samples for RecoveryPlanActions ValidateForReprotect.
 */
public final class RecoveryPlanActionsValidateForReprotectSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/RecoveryPlanActions_ValidateForReprotect_MaximumSet_Gen.json
     */
    /**
     * Sample code: RecoveryPlanActions_ValidateForReprotect_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void recoveryPlanActionsValidateForReprotectMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.recoveryPlanActions()
            .validateForReprotect("nrhlfd", null, "samplePlanName", new ReprotectRequest()
                .withReprotectRequestProperties(new ReprotectRequestProperties().withSelectedResourceIds(Arrays.asList(
                    "/providers/Microsoft.Management/serviceGroups/sampleServiceGroupName/providers/Microsoft.AzureResilienceManagement/recoveryPlans/samplePlanName/recoveryResources/12345678-9012-3456-7890-123456789012"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### RecoveryPlanActions_ValidateForTestFailover

```java
import com.azure.resourcemanager.resiliencemanagement.models.FailoverDirectionTypes;
import com.azure.resourcemanager.resiliencemanagement.models.FailoverRequest;
import com.azure.resourcemanager.resiliencemanagement.models.FailoverRequestProperties;
import java.util.Arrays;

/**
 * Samples for RecoveryPlanActions ValidateForTestFailover.
 */
public final class RecoveryPlanActionsValidateForTestFailoverSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/RecoveryPlanActions_ValidateForTestFailover_MaximumSet_Gen.json
     */
    /**
     * Sample code: RecoveryPlanActions_ValidateForTestFailover_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void recoveryPlanActionsValidateForTestFailoverMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.recoveryPlanActions()
            .validateForTestFailover("sampleServiceGroupName", null, "samplePlanName",
                new FailoverRequest().withFailoverDirection(FailoverDirectionTypes.FROM_SPECIFIC_LOCATIONS)
                    .withFailoverRequestProperties(
                        new FailoverRequestProperties().withSourceLocations(Arrays.asList("westus"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### RecoveryPlanActions_ValidateForTestFailoverCleanup

```java
/**
 * Samples for RecoveryPlanActions ValidateForTestFailoverCleanup.
 */
public final class RecoveryPlanActionsValidateForTestFailoverCleanupSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/RecoveryPlanActions_ValidateForTestFailoverCleanup_MaximumSet_Gen.json
     */
    /**
     * Sample code: RecoveryPlanActions_ValidateForTestFailoverCleanup_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void recoveryPlanActionsValidateForTestFailoverCleanupMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.recoveryPlanActions()
            .validateForTestFailoverCleanup("sampleServiceGroupName", null, "samplePlanName",
                com.azure.core.util.Context.NONE);
    }
}
```

### RecoveryPlans_CreateOrUpdate

```java
import com.azure.resourcemanager.resiliencemanagement.fluent.models.RecoveryPlanInner;
import com.azure.resourcemanager.resiliencemanagement.models.AssociatedIdentity;
import com.azure.resourcemanager.resiliencemanagement.models.ManagedServiceIdentity;
import com.azure.resourcemanager.resiliencemanagement.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.resiliencemanagement.models.RecoveryGroup;
import com.azure.resourcemanager.resiliencemanagement.models.RecoveryGroupCustomRunbookAction;
import com.azure.resourcemanager.resiliencemanagement.models.RecoveryGroupProperties;
import com.azure.resourcemanager.resiliencemanagement.models.RecoveryGroupsSetting;
import com.azure.resourcemanager.resiliencemanagement.models.RecoveryPlanProperties;
import com.azure.resourcemanager.resiliencemanagement.models.RecoveryPlanType;
import com.azure.resourcemanager.resiliencemanagement.models.UserAssignedIdentity;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for RecoveryPlans CreateOrUpdate.
 */
public final class RecoveryPlansCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/RecoveryPlans_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: RecoveryPlans_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void recoveryPlansCreateOrUpdateMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.recoveryPlans()
            .createOrUpdate("sampleServiceGroupName", "samplePlanName", new RecoveryPlanInner()
                .withProperties(new RecoveryPlanProperties().withPlanType(RecoveryPlanType.REGIONAL)
                    .withPlanDescription("Sample Plan")
                    .withRecoveryGroupsSetting(new RecoveryGroupsSetting()
                        .withDefaultGroup(new RecoveryGroup().withProperties(new RecoveryGroupProperties()
                            .withGroupUniqueId("b7e2a1c4-9f3b-4e2d-8c6a-2f7e4d1b5a9f")
                            .withOrderId(0)
                            .withDescription("sample recoverygroup")
                            .withPreActions(Arrays.asList(new RecoveryGroupCustomRunbookAction()
                                .withName("sample-group-action")
                                .withDescription("sample group action instructions")
                                .withTimeoutInMinutes(29)
                                .withActionResourceId(
                                    "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                .withAssociatedIdentity(new AssociatedIdentity()
                                    .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                    .withUserAssignedIdentity(
                                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1"))))
                            .withPostActions(Arrays.asList(new RecoveryGroupCustomRunbookAction()
                                .withName("sample-group-action")
                                .withTimeoutInMinutes(29)
                                .withActionResourceId(
                                    "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                .withAssociatedIdentity(new AssociatedIdentity()
                                    .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                    .withUserAssignedIdentity(
                                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                    .withTimeoutInMinutes(29)
                                    .withActionResourceId(
                                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                    .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                    .withAssociatedIdentity(new AssociatedIdentity()
                                        .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                        .withUserAssignedIdentity(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                    .withTimeoutInMinutes(29)
                                    .withActionResourceId(
                                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                    .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                    .withAssociatedIdentity(new AssociatedIdentity()
                                        .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                        .withUserAssignedIdentity(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                    .withTimeoutInMinutes(29)
                                    .withActionResourceId(
                                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                    .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                    .withAssociatedIdentity(new AssociatedIdentity()
                                        .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                        .withUserAssignedIdentity(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                    .withTimeoutInMinutes(29)
                                    .withActionResourceId(
                                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                    .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                    .withAssociatedIdentity(new AssociatedIdentity()
                                        .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                        .withUserAssignedIdentity(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                    .withTimeoutInMinutes(29)
                                    .withActionResourceId(
                                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                    .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                    .withAssociatedIdentity(new AssociatedIdentity()
                                        .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                        .withUserAssignedIdentity(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                    .withTimeoutInMinutes(29)
                                    .withActionResourceId(
                                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                    .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                    .withAssociatedIdentity(new AssociatedIdentity()
                                        .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                        .withUserAssignedIdentity(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                    .withTimeoutInMinutes(29)
                                    .withActionResourceId(
                                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                    .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                    .withAssociatedIdentity(new AssociatedIdentity()
                                        .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                        .withUserAssignedIdentity(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                    .withTimeoutInMinutes(29)
                                    .withActionResourceId(
                                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                    .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                    .withAssociatedIdentity(new AssociatedIdentity()
                                        .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                        .withUserAssignedIdentity(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1"))))))
                        .withAdditionalGroups(Arrays.asList(
                            new RecoveryGroup().withProperties(new RecoveryGroupProperties()
                                .withGroupUniqueId("b7e2a1c4-9f3b-4e2d-8c6a-2f7e4d1b5a9f")
                                .withOrderId(1)
                                .withDescription("sample recoverygroup")
                                .withPreActions(Arrays.asList(new RecoveryGroupCustomRunbookAction()
                                    .withName("sample-group-action")
                                    .withTimeoutInMinutes(29)
                                    .withActionResourceId(
                                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                    .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                    .withAssociatedIdentity(new AssociatedIdentity()
                                        .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                        .withUserAssignedIdentity(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1"))))
                                .withPostActions(Arrays.asList(new RecoveryGroupCustomRunbookAction()
                                    .withName("sample-group-action")
                                    .withTimeoutInMinutes(29)
                                    .withActionResourceId(
                                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                    .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                    .withAssociatedIdentity(new AssociatedIdentity()
                                        .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                        .withUserAssignedIdentity(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1"))))),
                            new RecoveryGroup().withProperties(new RecoveryGroupProperties()
                                .withGroupUniqueId("b7e2a1c4-9f3b-4e2d-8c6a-2f7e4d1b5a9f")
                                .withOrderId(1)
                                .withDescription("sample recoverygroup")
                                .withPreActions(Arrays.asList(new RecoveryGroupCustomRunbookAction()
                                    .withName("sample-group-action")
                                    .withTimeoutInMinutes(29)
                                    .withActionResourceId(
                                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                    .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                    .withAssociatedIdentity(new AssociatedIdentity()
                                        .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                        .withUserAssignedIdentity(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1"))))
                                .withPostActions(Arrays.asList(new RecoveryGroupCustomRunbookAction()
                                    .withName("sample-group-action")
                                    .withTimeoutInMinutes(29)
                                    .withActionResourceId(
                                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                    .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                    .withAssociatedIdentity(new AssociatedIdentity()
                                        .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                        .withUserAssignedIdentity(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1"))))),
                            new RecoveryGroup().withProperties(new RecoveryGroupProperties()
                                .withGroupUniqueId("b7e2a1c4-9f3b-4e2d-8c6a-2f7e4d1b5a9f")
                                .withOrderId(1)
                                .withDescription("sample recoverygroup")
                                .withPreActions(Arrays.asList(new RecoveryGroupCustomRunbookAction()
                                    .withName("sample-group-action")
                                    .withTimeoutInMinutes(29)
                                    .withActionResourceId(
                                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                    .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                    .withAssociatedIdentity(new AssociatedIdentity()
                                        .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                        .withUserAssignedIdentity(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1"))))
                                .withPostActions(Arrays.asList(new RecoveryGroupCustomRunbookAction()
                                    .withName("sample-group-action")
                                    .withTimeoutInMinutes(29)
                                    .withActionResourceId(
                                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                    .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                    .withAssociatedIdentity(new AssociatedIdentity()
                                        .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                        .withUserAssignedIdentity(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")))))))))
                .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentities(mapOf(
                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1",
                        new UserAssignedIdentity()))),
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

### RecoveryPlans_Delete

```java
/**
 * Samples for RecoveryPlans Delete.
 */
public final class RecoveryPlansDeleteSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/RecoveryPlans_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: RecoveryPlans_Delete_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void recoveryPlansDeleteMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.recoveryPlans().delete("sampleServiceGroupName", "samplePlanName", com.azure.core.util.Context.NONE);
    }
}
```

### RecoveryPlans_Get

```java
/**
 * Samples for RecoveryPlans Get.
 */
public final class RecoveryPlansGetSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/RecoveryPlans_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: RecoveryPlans_Get_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void
        recoveryPlansGetMaximumSet(com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.recoveryPlans()
            .getWithResponse("sampleServiceGroupName", "samplePlanName", com.azure.core.util.Context.NONE);
    }
}
```

### RecoveryPlans_List

```java
/**
 * Samples for RecoveryPlans List.
 */
public final class RecoveryPlansListSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/RecoveryPlans_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: RecoveryPlans_List_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void recoveryPlansListMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.recoveryPlans().list("sampleServiceGroupName", "jfpmvvhtt", 44, com.azure.core.util.Context.NONE);
    }
}
```

### RecoveryPlans_Update

```java
import com.azure.resourcemanager.resiliencemanagement.fluent.models.RecoveryPlanInner;
import com.azure.resourcemanager.resiliencemanagement.models.AssociatedIdentity;
import com.azure.resourcemanager.resiliencemanagement.models.ManagedServiceIdentity;
import com.azure.resourcemanager.resiliencemanagement.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.resiliencemanagement.models.RecoveryGroup;
import com.azure.resourcemanager.resiliencemanagement.models.RecoveryGroupCustomRunbookAction;
import com.azure.resourcemanager.resiliencemanagement.models.RecoveryGroupProperties;
import com.azure.resourcemanager.resiliencemanagement.models.RecoveryGroupsSetting;
import com.azure.resourcemanager.resiliencemanagement.models.RecoveryPlanProperties;
import com.azure.resourcemanager.resiliencemanagement.models.UserAssignedIdentity;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for RecoveryPlans Update.
 */
public final class RecoveryPlansUpdateSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/RecoveryPlans_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: RecoveryPlans_Update_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void recoveryPlansUpdateMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.recoveryPlans()
            .update("sampleServiceGroupName", "samplePlanName", new RecoveryPlanInner()
                .withProperties(new RecoveryPlanProperties().withPlanDescription("my sample recovery plan")
                    .withRecoveryGroupsSetting(new RecoveryGroupsSetting()
                        .withDefaultGroup(new RecoveryGroup().withProperties(new RecoveryGroupProperties()
                            .withGroupUniqueId("b7e2a1c4-9f3b-4e2d-8c6a-2f7e4d1b5a9f")
                            .withOrderId(3)
                            .withDescription("sample-recoverygroup")
                            .withPreActions(Arrays.asList(
                                new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                    .withTimeoutInMinutes(29),
                                new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                    .withTimeoutInMinutes(29)
                                    .withActionResourceId(
                                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                    .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                    .withAssociatedIdentity(new AssociatedIdentity()
                                        .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                        .withUserAssignedIdentity(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1"))))
                            .withPostActions(Arrays.asList(
                                new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                    .withTimeoutInMinutes(29),
                                new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                    .withTimeoutInMinutes(29)
                                    .withActionResourceId(
                                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                    .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                    .withAssociatedIdentity(new AssociatedIdentity()
                                        .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                        .withUserAssignedIdentity(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1"))))))
                        .withAdditionalGroups(Arrays.asList(
                            new RecoveryGroup().withProperties(new RecoveryGroupProperties()
                                .withGroupUniqueId("b7e2a1c4-9f3b-4e2d-8c6a-2f7e4d1b5a9f")
                                .withOrderId(1)
                                .withDescription("sample recoverygroup")
                                .withPreActions(Arrays.asList(new RecoveryGroupCustomRunbookAction()
                                    .withName("sample-group-action")
                                    .withTimeoutInMinutes(29)
                                    .withActionResourceId(
                                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                    .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                    .withAssociatedIdentity(new AssociatedIdentity()
                                        .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                        .withUserAssignedIdentity(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1"))))
                                .withPostActions(Arrays.asList(new RecoveryGroupCustomRunbookAction()
                                    .withName("sample-group-action")
                                    .withTimeoutInMinutes(29)
                                    .withActionResourceId(
                                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                    .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                    .withAssociatedIdentity(new AssociatedIdentity()
                                        .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                        .withUserAssignedIdentity(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1"))))),
                            new RecoveryGroup().withProperties(new RecoveryGroupProperties()
                                .withGroupUniqueId("b7e2a1c4-9f3b-4e2d-8c6a-2f7e4d1b5a9f")
                                .withOrderId(1)
                                .withDescription("sample recoverygroup")
                                .withPreActions(Arrays.asList(new RecoveryGroupCustomRunbookAction()
                                    .withName("sample-group-action")
                                    .withTimeoutInMinutes(29)
                                    .withActionResourceId(
                                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                    .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                    .withAssociatedIdentity(new AssociatedIdentity()
                                        .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                        .withUserAssignedIdentity(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1"))))
                                .withPostActions(Arrays.asList(new RecoveryGroupCustomRunbookAction()
                                    .withName("sample-group-action")
                                    .withTimeoutInMinutes(29)
                                    .withActionResourceId(
                                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                    .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                    .withAssociatedIdentity(new AssociatedIdentity()
                                        .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                        .withUserAssignedIdentity(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1"))))),
                            new RecoveryGroup().withProperties(new RecoveryGroupProperties()
                                .withGroupUniqueId("b7e2a1c4-9f3b-4e2d-8c6a-2f7e4d1b5a9f")
                                .withOrderId(1)
                                .withDescription("sample recoverygroup")
                                .withPreActions(Arrays.asList(new RecoveryGroupCustomRunbookAction()
                                    .withName("sample-group-action")
                                    .withTimeoutInMinutes(29)
                                    .withActionResourceId(
                                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                    .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                    .withAssociatedIdentity(new AssociatedIdentity()
                                        .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                        .withUserAssignedIdentity(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1"))))
                                .withPostActions(Arrays.asList(new RecoveryGroupCustomRunbookAction()
                                    .withName("sample-group-action")
                                    .withTimeoutInMinutes(29)
                                    .withActionResourceId(
                                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                    .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                    .withAssociatedIdentity(new AssociatedIdentity()
                                        .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                        .withUserAssignedIdentity(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1"))))),
                            new RecoveryGroup().withProperties(new RecoveryGroupProperties()
                                .withGroupUniqueId("b7e2a1c4-9f3b-4e2d-8c6a-2f7e4d1b5a9f")
                                .withOrderId(1)
                                .withDescription("sample recoverygroup")
                                .withPreActions(Arrays.asList(new RecoveryGroupCustomRunbookAction()
                                    .withName("sample-group-action")
                                    .withTimeoutInMinutes(29)
                                    .withActionResourceId(
                                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                    .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                    .withAssociatedIdentity(new AssociatedIdentity()
                                        .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                        .withUserAssignedIdentity(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1"))))
                                .withPostActions(Arrays.asList(new RecoveryGroupCustomRunbookAction()
                                    .withName("sample-group-action")
                                    .withTimeoutInMinutes(29)
                                    .withActionResourceId(
                                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                    .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                    .withAssociatedIdentity(new AssociatedIdentity()
                                        .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                        .withUserAssignedIdentity(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1"))))),
                            new RecoveryGroup().withProperties(new RecoveryGroupProperties()
                                .withGroupUniqueId("b7e2a1c4-9f3b-4e2d-8c6a-2f7e4d1b5a9f")
                                .withOrderId(1)
                                .withDescription("sample recoverygroup")
                                .withPreActions(Arrays.asList(new RecoveryGroupCustomRunbookAction()
                                    .withName("sample-group-action")
                                    .withTimeoutInMinutes(29)
                                    .withActionResourceId(
                                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                    .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                    .withAssociatedIdentity(new AssociatedIdentity()
                                        .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                        .withUserAssignedIdentity(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1"))))
                                .withPostActions(Arrays.asList(new RecoveryGroupCustomRunbookAction()
                                    .withName("sample-group-action")
                                    .withTimeoutInMinutes(29)
                                    .withActionResourceId(
                                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                    .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                    .withAssociatedIdentity(new AssociatedIdentity()
                                        .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                        .withUserAssignedIdentity(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1"))))),
                            new RecoveryGroup().withProperties(new RecoveryGroupProperties()
                                .withGroupUniqueId("b7e2a1c4-9f3b-4e2d-8c6a-2f7e4d1b5a9f")
                                .withOrderId(1)
                                .withDescription("sample recoverygroup")
                                .withPreActions(Arrays.asList(new RecoveryGroupCustomRunbookAction()
                                    .withName("sample-group-action")
                                    .withTimeoutInMinutes(29)
                                    .withActionResourceId(
                                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                    .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                    .withAssociatedIdentity(new AssociatedIdentity()
                                        .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                        .withUserAssignedIdentity(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1"))))
                                .withPostActions(Arrays.asList(new RecoveryGroupCustomRunbookAction()
                                    .withName("sample-group-action")
                                    .withTimeoutInMinutes(29)
                                    .withActionResourceId(
                                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                    .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                    .withAssociatedIdentity(new AssociatedIdentity()
                                        .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                        .withUserAssignedIdentity(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1"))))),
                            new RecoveryGroup().withProperties(new RecoveryGroupProperties()
                                .withGroupUniqueId("b7e2a1c4-9f3b-4e2d-8c6a-2f7e4d1b5a9f")
                                .withOrderId(1)
                                .withDescription("sample recoverygroup")
                                .withPreActions(Arrays.asList(new RecoveryGroupCustomRunbookAction()
                                    .withName("sample-group-action")
                                    .withTimeoutInMinutes(29)
                                    .withActionResourceId(
                                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                    .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                    .withAssociatedIdentity(new AssociatedIdentity()
                                        .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                        .withUserAssignedIdentity(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1"))))
                                .withPostActions(Arrays.asList(new RecoveryGroupCustomRunbookAction()
                                    .withName("sample-group-action")
                                    .withTimeoutInMinutes(29)
                                    .withActionResourceId(
                                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                    .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                    .withAssociatedIdentity(new AssociatedIdentity()
                                        .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                        .withUserAssignedIdentity(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1"))))),
                            new RecoveryGroup().withProperties(new RecoveryGroupProperties()
                                .withGroupUniqueId("b7e2a1c4-9f3b-4e2d-8c6a-2f7e4d1b5a9f")
                                .withOrderId(1)
                                .withDescription("sample recoverygroup")
                                .withPreActions(Arrays.asList(new RecoveryGroupCustomRunbookAction()
                                    .withName("sample-group-action")
                                    .withTimeoutInMinutes(29)
                                    .withActionResourceId(
                                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                    .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                    .withAssociatedIdentity(new AssociatedIdentity()
                                        .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                        .withUserAssignedIdentity(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1"))))
                                .withPostActions(Arrays.asList(new RecoveryGroupCustomRunbookAction()
                                    .withName("sample-group-action")
                                    .withTimeoutInMinutes(29)
                                    .withActionResourceId(
                                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                    .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                    .withAssociatedIdentity(new AssociatedIdentity()
                                        .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                        .withUserAssignedIdentity(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1"))))),
                            new RecoveryGroup().withProperties(new RecoveryGroupProperties()
                                .withGroupUniqueId("b7e2a1c4-9f3b-4e2d-8c6a-2f7e4d1b5a9f")
                                .withOrderId(1)
                                .withDescription("sample recoverygroup")
                                .withPreActions(Arrays.asList(new RecoveryGroupCustomRunbookAction()
                                    .withName("sample-group-action")
                                    .withTimeoutInMinutes(29)
                                    .withActionResourceId(
                                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                    .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                    .withAssociatedIdentity(new AssociatedIdentity()
                                        .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                        .withUserAssignedIdentity(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1"))))
                                .withPostActions(Arrays.asList(new RecoveryGroupCustomRunbookAction()
                                    .withName("sample-group-action")
                                    .withTimeoutInMinutes(29)
                                    .withActionResourceId(
                                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                    .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                    .withAssociatedIdentity(new AssociatedIdentity()
                                        .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                        .withUserAssignedIdentity(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1"))))),
                            new RecoveryGroup().withProperties(new RecoveryGroupProperties()
                                .withGroupUniqueId("b7e2a1c4-9f3b-4e2d-8c6a-2f7e4d1b5a9f")
                                .withOrderId(1)
                                .withDescription("sample recoverygroup")
                                .withPreActions(Arrays.asList(new RecoveryGroupCustomRunbookAction()
                                    .withName("sample-group-action")
                                    .withTimeoutInMinutes(29)
                                    .withActionResourceId(
                                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                    .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                    .withAssociatedIdentity(new AssociatedIdentity()
                                        .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                        .withUserAssignedIdentity(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1"))))
                                .withPostActions(Arrays.asList(new RecoveryGroupCustomRunbookAction()
                                    .withName("sample-group-action")
                                    .withTimeoutInMinutes(29)
                                    .withActionResourceId(
                                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                    .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                    .withAssociatedIdentity(new AssociatedIdentity()
                                        .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                        .withUserAssignedIdentity(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1"))))),
                            new RecoveryGroup().withProperties(new RecoveryGroupProperties()
                                .withGroupUniqueId("b7e2a1c4-9f3b-4e2d-8c6a-2f7e4d1b5a9f")
                                .withOrderId(1)
                                .withDescription("sample recoverygroup")
                                .withPreActions(Arrays.asList(new RecoveryGroupCustomRunbookAction()
                                    .withName("sample-group-action")
                                    .withTimeoutInMinutes(29)
                                    .withActionResourceId(
                                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                    .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                    .withAssociatedIdentity(new AssociatedIdentity()
                                        .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                        .withUserAssignedIdentity(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1"))))
                                .withPostActions(Arrays.asList(new RecoveryGroupCustomRunbookAction()
                                    .withName("sample-group-action")
                                    .withTimeoutInMinutes(29)
                                    .withActionResourceId(
                                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                    .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                    .withAssociatedIdentity(new AssociatedIdentity()
                                        .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                        .withUserAssignedIdentity(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1"))))),
                            new RecoveryGroup().withProperties(new RecoveryGroupProperties()
                                .withGroupUniqueId("b7e2a1c4-9f3b-4e2d-8c6a-2f7e4d1b5a9f")
                                .withOrderId(1)
                                .withDescription("sample recoverygroup")
                                .withPreActions(Arrays.asList(new RecoveryGroupCustomRunbookAction()
                                    .withName("sample-group-action")
                                    .withTimeoutInMinutes(29)
                                    .withActionResourceId(
                                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                    .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                    .withAssociatedIdentity(new AssociatedIdentity()
                                        .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                        .withUserAssignedIdentity(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1"))))
                                .withPostActions(Arrays.asList(new RecoveryGroupCustomRunbookAction()
                                    .withName("sample-group-action")
                                    .withTimeoutInMinutes(29)
                                    .withActionResourceId(
                                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                    .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                    .withAssociatedIdentity(new AssociatedIdentity()
                                        .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                        .withUserAssignedIdentity(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1"))))),
                            new RecoveryGroup().withProperties(new RecoveryGroupProperties()
                                .withGroupUniqueId("b7e2a1c4-9f3b-4e2d-8c6a-2f7e4d1b5a9f")
                                .withOrderId(1)
                                .withDescription("sample recoverygroup")
                                .withPreActions(Arrays.asList(new RecoveryGroupCustomRunbookAction()
                                    .withName("sample-group-action")
                                    .withTimeoutInMinutes(29)
                                    .withActionResourceId(
                                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                    .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                    .withAssociatedIdentity(new AssociatedIdentity()
                                        .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                        .withUserAssignedIdentity(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1"))))
                                .withPostActions(Arrays.asList(new RecoveryGroupCustomRunbookAction()
                                    .withName("sample-group-action")
                                    .withTimeoutInMinutes(29)
                                    .withActionResourceId(
                                        "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                    .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                    .withAssociatedIdentity(new AssociatedIdentity()
                                        .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                        .withUserAssignedIdentity(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")),
                                    new RecoveryGroupCustomRunbookAction().withName("sample-group-action")
                                        .withTimeoutInMinutes(29)
                                        .withActionResourceId(
                                            "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.Automation/automationAccounts/sampleAccount/runbooks/sameplRunbooks1")
                                        .withParameters(mapOf("key7795", "fakeTokenPlaceholder"))
                                        .withAssociatedIdentity(new AssociatedIdentity()
                                            .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                                            .withUserAssignedIdentity(
                                                "/subscriptions/4e88bed3-114f-443d-9975-28f64122ec5e/resourcegroups/resourceGroup1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami1")))))))))
                .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentities(mapOf("key7088", new UserAssignedIdentity()))),
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

### RecoveryResources_Get

```java
/**
 * Samples for RecoveryResources Get.
 */
public final class RecoveryResourcesGetSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/RecoveryResources_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: RecoveryResources_Get_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void recoveryResourcesGetMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.recoveryResources()
            .getWithResponse("sampleServiceGroupName", "samplePlanName", "12345678-9012-3456-7890-123456789012",
                com.azure.core.util.Context.NONE);
    }
}
```

### RecoveryResources_List

```java
/**
 * Samples for RecoveryResources List.
 */
public final class RecoveryResourcesListSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/RecoveryResources_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: RecoveryResources_List_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void recoveryResourcesListMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.recoveryResources().list("sampleServiceGroupName", "plan1", com.azure.core.util.Context.NONE);
    }
}
```

### UnifiedResilienceItems_Get

```java
/**
 * Samples for UnifiedResilienceItems Get.
 */
public final class UnifiedResilienceItemsGetSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/UnifiedResilienceItems_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: UnifiedResilienceItems_Get_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void unifiedResilienceItemsGetMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.unifiedResilienceItems().getWithResponse("sg1", "uri1", com.azure.core.util.Context.NONE);
    }
}
```

### UnifiedResilienceItems_List

```java
/**
 * Samples for UnifiedResilienceItems List.
 */
public final class UnifiedResilienceItemsListSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/UnifiedResilienceItems_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: UnifiedResilienceItems_List_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void unifiedResilienceItemsListMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.unifiedResilienceItems()
            .list("zldmpkvqzifygkqau", "xntbyoswztnmvitj", 69, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-04-01-preview/UnifiedResilienceItems_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: UnifiedResilienceItems_List_MaximumSet - generated by [MinimumSet] rule.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void unifiedResilienceItemsListMaximumSetGeneratedByMinimumSetRule(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.unifiedResilienceItems().list("sampleServiceGroupName", null, null, com.azure.core.util.Context.NONE);
    }
}
```

### UsagePlans_CreateOrUpdate

```java
import com.azure.resourcemanager.resiliencemanagement.models.UsagePlanProperties;
import com.azure.resourcemanager.resiliencemanagement.models.UsagePlanType;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for UsagePlans CreateOrUpdate.
 */
public final class UsagePlansCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/UsagePlans_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: UsagePlans_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void usagePlansCreateOrUpdateMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.usagePlans()
            .define("myUsagePlan")
            .withRegion("global")
            .withExistingResourceGroup("MyResourceGroup")
            .withTags(mapOf("environment", "production"))
            .withProperties(new UsagePlanProperties().withPlanType(UsagePlanType.STANDARD))
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

### UsagePlans_Delete

```java
/**
 * Samples for UsagePlans Delete.
 */
public final class UsagePlansDeleteSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/UsagePlans_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: UsagePlans_Delete_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void
        usagePlansDeleteMaximumSet(com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.usagePlans().delete("MyResourceGroup", "myUsagePlan", com.azure.core.util.Context.NONE);
    }
}
```

### UsagePlans_GetByResourceGroup

```java
/**
 * Samples for UsagePlans GetByResourceGroup.
 */
public final class UsagePlansGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/UsagePlans_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: UsagePlans_Get_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void
        usagePlansGetMaximumSet(com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.usagePlans()
            .getByResourceGroupWithResponse("MyResourceGroup", "myUsagePlan", com.azure.core.util.Context.NONE);
    }
}
```

### UsagePlans_List

```java
/**
 * Samples for UsagePlans List.
 */
public final class UsagePlansListSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/UsagePlans_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: UsagePlans_ListBySubscription_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void usagePlansListBySubscriptionMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.usagePlans().list(com.azure.core.util.Context.NONE);
    }
}
```

### UsagePlans_ListByResourceGroup

```java
/**
 * Samples for UsagePlans ListByResourceGroup.
 */
public final class UsagePlansListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/UsagePlans_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: UsagePlans_ListByResourceGroup_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void usagePlansListByResourceGroupMaximumSet(
        com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        manager.usagePlans().listByResourceGroup("MyResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### UsagePlans_Update

```java
import com.azure.resourcemanager.resiliencemanagement.models.UsagePlan;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for UsagePlans Update.
 */
public final class UsagePlansUpdateSamples {
    /*
     * x-ms-original-file: 2026-04-01-preview/UsagePlans_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: UsagePlans_Update_MaximumSet.
     * 
     * @param manager Entry point to ResilienceManagementManager.
     */
    public static void
        usagePlansUpdateMaximumSet(com.azure.resourcemanager.resiliencemanagement.ResilienceManagementManager manager) {
        UsagePlan resource = manager.usagePlans()
            .getByResourceGroupWithResponse("MyResourceGroup", "myUsagePlan", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("environment", "staging", "costCenter", "12345")).apply();
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

