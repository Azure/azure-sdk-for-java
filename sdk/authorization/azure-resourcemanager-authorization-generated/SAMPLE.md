# Code snippets and samples


## AccessReviewDefaultSettingsOperation

- [Get](#accessreviewdefaultsettingsoperation_get)
- [Put](#accessreviewdefaultsettingsoperation_put)

## AccessReviewHistoryDefinitionInstance

- [GenerateDownloadUri](#accessreviewhistorydefinitioninstance_generatedownloaduri)

## AccessReviewHistoryDefinitionInstancesOperation

- [List](#accessreviewhistorydefinitioninstancesoperation_list)

## AccessReviewHistoryDefinitionOperation

- [Create](#accessreviewhistorydefinitionoperation_create)
- [DeleteById](#accessreviewhistorydefinitionoperation_deletebyid)

## AccessReviewHistoryDefinitions

- [GetById](#accessreviewhistorydefinitions_getbyid)
- [List](#accessreviewhistorydefinitions_list)

## AccessReviewInstanceContactedReviewers

- [List](#accessreviewinstancecontactedreviewers_list)

## AccessReviewInstanceDecisions

- [List](#accessreviewinstancedecisions_list)

## AccessReviewInstanceMyDecisions

- [GetById](#accessreviewinstancemydecisions_getbyid)
- [List](#accessreviewinstancemydecisions_list)
- [Patch](#accessreviewinstancemydecisions_patch)

## AccessReviewInstanceOperation

- [AcceptRecommendations](#accessreviewinstanceoperation_acceptrecommendations)
- [ApplyDecisions](#accessreviewinstanceoperation_applydecisions)
- [ResetDecisions](#accessreviewinstanceoperation_resetdecisions)
- [SendReminders](#accessreviewinstanceoperation_sendreminders)
- [Stop](#accessreviewinstanceoperation_stop)

## AccessReviewInstances

- [Create](#accessreviewinstances_create)
- [GetById](#accessreviewinstances_getbyid)
- [List](#accessreviewinstances_list)

## AccessReviewInstancesAssignedForMyApproval

- [GetById](#accessreviewinstancesassignedformyapproval_getbyid)
- [List](#accessreviewinstancesassignedformyapproval_list)

## AccessReviewScheduleDefinitions

- [CreateOrUpdateById](#accessreviewscheduledefinitions_createorupdatebyid)
- [DeleteById](#accessreviewscheduledefinitions_deletebyid)
- [GetById](#accessreviewscheduledefinitions_getbyid)
- [List](#accessreviewscheduledefinitions_list)
- [Stop](#accessreviewscheduledefinitions_stop)

## AccessReviewScheduleDefinitionsAssignedForMyApproval

- [List](#accessreviewscheduledefinitionsassignedformyapproval_list)

## TenantLevelAccessReviewInstanceContactedReviewers

- [List](#tenantlevelaccessreviewinstancecontactedreviewers_list)
### AccessReviewDefaultSettingsOperation_Get

```java
import com.azure.core.util.Context;

/** Samples for AccessReviewDefaultSettingsOperation Get. */
public final class AccessReviewDefaultSettingsOperationGetSamples {
    /*
     * x-ms-original-file: specification/authorization/resource-manager/Microsoft.Authorization/preview/2021-11-16-preview/examples/GetAccessReviewDefaultSettings.json
     */
    /**
     * Sample code: GetAccessReviewDefaultSettings.
     *
     * @param manager Entry point to AuthorizationManager.
     */
    public static void getAccessReviewDefaultSettings(
        com.azure.resourcemanager.authorization.generated.AuthorizationManager manager) {
        manager.accessReviewDefaultSettingsOperations().getWithResponse(Context.NONE);
    }
}
```

### AccessReviewDefaultSettingsOperation_Put

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.authorization.generated.fluent.models.AccessReviewScheduleSettings;

/** Samples for AccessReviewDefaultSettingsOperation Put. */
public final class AccessReviewDefaultSettingsOperationPutSamples {
    /*
     * x-ms-original-file: specification/authorization/resource-manager/Microsoft.Authorization/preview/2021-11-16-preview/examples/PutAccessReviewDefaultSettings.json
     */
    /**
     * Sample code: PutAccessReviewDefaultSettings.
     *
     * @param manager Entry point to AuthorizationManager.
     */
    public static void putAccessReviewDefaultSettings(
        com.azure.resourcemanager.authorization.generated.AuthorizationManager manager) {
        manager
            .accessReviewDefaultSettingsOperations()
            .putWithResponse(new AccessReviewScheduleSettings(), Context.NONE);
    }
}
```

### AccessReviewHistoryDefinitionInstance_GenerateDownloadUri

```java
import com.azure.core.util.Context;

/** Samples for AccessReviewHistoryDefinitionInstance GenerateDownloadUri. */
public final class AccessReviewHistoryDefinitionInstanceGenerateDownloadUriSamples {
    /*
     * x-ms-original-file: specification/authorization/resource-manager/Microsoft.Authorization/preview/2021-11-16-preview/examples/PostAccessReviewHistoryDefinitionInstance.json
     */
    /**
     * Sample code: PostAccessReviewHistoryDefinitionInstance.
     *
     * @param manager Entry point to AuthorizationManager.
     */
    public static void postAccessReviewHistoryDefinitionInstance(
        com.azure.resourcemanager.authorization.generated.AuthorizationManager manager) {
        manager
            .accessReviewHistoryDefinitionInstances()
            .generateDownloadUriWithResponse(
                "44724910-d7a5-4c29-b28f-db73e717165a", "9038f4f3-3d8d-43c3-8ede-669ea082c43b", Context.NONE);
    }
}
```

### AccessReviewHistoryDefinitionInstancesOperation_List

```java
import com.azure.core.util.Context;

/** Samples for AccessReviewHistoryDefinitionInstancesOperation List. */
public final class AccessReviewHistoryDefinitionInstancesOperationListSamples {
    /*
     * x-ms-original-file: specification/authorization/resource-manager/Microsoft.Authorization/preview/2021-11-16-preview/examples/GetAccessReviewHistoryDefinitionInstances.json
     */
    /**
     * Sample code: GetAccessReviewHistoryDefinitionInstances.
     *
     * @param manager Entry point to AuthorizationManager.
     */
    public static void getAccessReviewHistoryDefinitionInstances(
        com.azure.resourcemanager.authorization.generated.AuthorizationManager manager) {
        manager
            .accessReviewHistoryDefinitionInstancesOperations()
            .list("44724910-d7a5-4c29-b28f-db73e717165a", Context.NONE);
    }
}
```

### AccessReviewHistoryDefinitionOperation_Create

```java
/** Samples for AccessReviewHistoryDefinitionOperation Create. */
public final class AccessReviewHistoryDefinitionOperationCreateSamples {
    /*
     * x-ms-original-file: specification/authorization/resource-manager/Microsoft.Authorization/preview/2021-11-16-preview/examples/PutAccessReviewHistoryDefinition.json
     */
    /**
     * Sample code: PutAccessReviewHistoryDefinition.
     *
     * @param manager Entry point to AuthorizationManager.
     */
    public static void putAccessReviewHistoryDefinition(
        com.azure.resourcemanager.authorization.generated.AuthorizationManager manager) {
        manager.accessReviewHistoryDefinitionOperations().define("44724910-d7a5-4c29-b28f-db73e717165a").create();
    }
}
```

### AccessReviewHistoryDefinitionOperation_DeleteById

```java
import com.azure.core.util.Context;

/** Samples for AccessReviewHistoryDefinitionOperation DeleteById. */
public final class AccessReviewHistoryDefinitionOperationDeleteByIdSamples {
    /*
     * x-ms-original-file: specification/authorization/resource-manager/Microsoft.Authorization/preview/2021-11-16-preview/examples/DeleteAccessReviewHistoryDefinition.json
     */
    /**
     * Sample code: DeleteAccessReview.
     *
     * @param manager Entry point to AuthorizationManager.
     */
    public static void deleteAccessReview(
        com.azure.resourcemanager.authorization.generated.AuthorizationManager manager) {
        manager
            .accessReviewHistoryDefinitionOperations()
            .deleteByIdWithResponse("fa73e90b-5bf1-45fd-a182-35ce5fc0674d", Context.NONE);
    }
}
```

### AccessReviewHistoryDefinitions_GetById

```java
import com.azure.core.util.Context;

/** Samples for AccessReviewHistoryDefinitions GetById. */
public final class AccessReviewHistoryDefinitionsGetByIdSamples {
    /*
     * x-ms-original-file: specification/authorization/resource-manager/Microsoft.Authorization/preview/2021-11-16-preview/examples/GetAccessReviewHistoryDefinition.json
     */
    /**
     * Sample code: GetAccessReviewHistoryDefinition.
     *
     * @param manager Entry point to AuthorizationManager.
     */
    public static void getAccessReviewHistoryDefinition(
        com.azure.resourcemanager.authorization.generated.AuthorizationManager manager) {
        manager
            .accessReviewHistoryDefinitions()
            .getByIdWithResponse("44724910-d7a5-4c29-b28f-db73e717165a", Context.NONE);
    }
}
```

### AccessReviewHistoryDefinitions_List

```java
import com.azure.core.util.Context;

/** Samples for AccessReviewHistoryDefinitions List. */
public final class AccessReviewHistoryDefinitionsListSamples {
    /*
     * x-ms-original-file: specification/authorization/resource-manager/Microsoft.Authorization/preview/2021-11-16-preview/examples/GetAccessReviewHistoryDefinitions.json
     */
    /**
     * Sample code: GetAccessReviewHistoryDefinitions.
     *
     * @param manager Entry point to AuthorizationManager.
     */
    public static void getAccessReviewHistoryDefinitions(
        com.azure.resourcemanager.authorization.generated.AuthorizationManager manager) {
        manager.accessReviewHistoryDefinitions().list(null, Context.NONE);
    }
}
```

### AccessReviewInstanceContactedReviewers_List

```java
import com.azure.core.util.Context;

/** Samples for AccessReviewInstanceContactedReviewers List. */
public final class AccessReviewInstanceContactedReviewersListSamples {
    /*
     * x-ms-original-file: specification/authorization/resource-manager/Microsoft.Authorization/preview/2021-11-16-preview/examples/GetAccessReviewInstanceContactedReviewers.json
     */
    /**
     * Sample code: GetAccessReviewInstanceContactedReviewers.
     *
     * @param manager Entry point to AuthorizationManager.
     */
    public static void getAccessReviewInstanceContactedReviewers(
        com.azure.resourcemanager.authorization.generated.AuthorizationManager manager) {
        manager
            .accessReviewInstanceContactedReviewers()
            .list("265785a7-a81f-4201-8a18-bb0db95982b7", "f25ed880-9c31-4101-bc57-825d8df3b58c", Context.NONE);
    }
}
```

### AccessReviewInstanceDecisions_List

```java
import com.azure.core.util.Context;

/** Samples for AccessReviewInstanceDecisions List. */
public final class AccessReviewInstanceDecisionsListSamples {
    /*
     * x-ms-original-file: specification/authorization/resource-manager/Microsoft.Authorization/preview/2021-11-16-preview/examples/GetAccessReviewInstanceDecisions.json
     */
    /**
     * Sample code: GetAccessReviewInstanceDecisions.
     *
     * @param manager Entry point to AuthorizationManager.
     */
    public static void getAccessReviewInstanceDecisions(
        com.azure.resourcemanager.authorization.generated.AuthorizationManager manager) {
        manager
            .accessReviewInstanceDecisions()
            .list("265785a7-a81f-4201-8a18-bb0db95982b7", "f25ed880-9c31-4101-bc57-825d8df3b58c", null, Context.NONE);
    }
}
```

### AccessReviewInstanceMyDecisions_GetById

```java
import com.azure.core.util.Context;

/** Samples for AccessReviewInstanceMyDecisions GetById. */
public final class AccessReviewInstanceMyDecisionsGetByIdSamples {
    /*
     * x-ms-original-file: specification/authorization/resource-manager/Microsoft.Authorization/preview/2021-11-16-preview/examples/GetAccessReviewInstanceMyDecisionById.json
     */
    /**
     * Sample code: GetAccessReviewMyInstanceDecision.
     *
     * @param manager Entry point to AuthorizationManager.
     */
    public static void getAccessReviewMyInstanceDecision(
        com.azure.resourcemanager.authorization.generated.AuthorizationManager manager) {
        manager
            .accessReviewInstanceMyDecisions()
            .getByIdWithResponse(
                "488a6d0e-0a63-4946-86e3-1f5bbc934661",
                "4135f961-be78-4005-8101-c72a5af307a2",
                "fa73e90b-5bf1-45fd-a182-35ce5fc0674d",
                Context.NONE);
    }
}
```

### AccessReviewInstanceMyDecisions_List

```java
import com.azure.core.util.Context;

/** Samples for AccessReviewInstanceMyDecisions List. */
public final class AccessReviewInstanceMyDecisionsListSamples {
    /*
     * x-ms-original-file: specification/authorization/resource-manager/Microsoft.Authorization/preview/2021-11-16-preview/examples/GetAccessReviewInstanceMyDecisions.json
     */
    /**
     * Sample code: GetAccessReviewMyInstanceDecisions.
     *
     * @param manager Entry point to AuthorizationManager.
     */
    public static void getAccessReviewMyInstanceDecisions(
        com.azure.resourcemanager.authorization.generated.AuthorizationManager manager) {
        manager
            .accessReviewInstanceMyDecisions()
            .list("488a6d0e-0a63-4946-86e3-1f5bbc934661", "4135f961-be78-4005-8101-c72a5af307a2", null, Context.NONE);
    }
}
```

### AccessReviewInstanceMyDecisions_Patch

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.authorization.generated.fluent.models.AccessReviewDecisionProperties;

/** Samples for AccessReviewInstanceMyDecisions Patch. */
public final class AccessReviewInstanceMyDecisionsPatchSamples {
    /*
     * x-ms-original-file: specification/authorization/resource-manager/Microsoft.Authorization/preview/2021-11-16-preview/examples/PatchAccessReviewInstanceMyDecisionById.json
     */
    /**
     * Sample code: PatchAccessReviewMyInstanceDecision.
     *
     * @param manager Entry point to AuthorizationManager.
     */
    public static void patchAccessReviewMyInstanceDecision(
        com.azure.resourcemanager.authorization.generated.AuthorizationManager manager) {
        manager
            .accessReviewInstanceMyDecisions()
            .patchWithResponse(
                "488a6d0e-0a63-4946-86e3-1f5bbc934661",
                "4135f961-be78-4005-8101-c72a5af307a2",
                "fa73e90b-5bf1-45fd-a182-35ce5fc0674d",
                new AccessReviewDecisionProperties(),
                Context.NONE);
    }
}
```

### AccessReviewInstanceOperation_AcceptRecommendations

```java
import com.azure.core.util.Context;

/** Samples for AccessReviewInstanceOperation AcceptRecommendations. */
public final class AccessReviewInstanceOperationAcceptRecommendationsSamples {
    /*
     * x-ms-original-file: specification/authorization/resource-manager/Microsoft.Authorization/preview/2021-11-16-preview/examples/AccessReviewInstanceAcceptRecommendations.json
     */
    /**
     * Sample code: AccessReviewInstanceAcceptRecommmendations.
     *
     * @param manager Entry point to AuthorizationManager.
     */
    public static void accessReviewInstanceAcceptRecommmendations(
        com.azure.resourcemanager.authorization.generated.AuthorizationManager manager) {
        manager
            .accessReviewInstanceOperations()
            .acceptRecommendationsWithResponse(
                "488a6d0e-0a63-4946-86e3-1f5bbc934661", "d9b9e056-7004-470b-bf21-1635e98487da", Context.NONE);
    }
}
```

### AccessReviewInstanceOperation_ApplyDecisions

```java
import com.azure.core.util.Context;

/** Samples for AccessReviewInstanceOperation ApplyDecisions. */
public final class AccessReviewInstanceOperationApplyDecisionsSamples {
    /*
     * x-ms-original-file: specification/authorization/resource-manager/Microsoft.Authorization/preview/2021-11-16-preview/examples/AccessReviewInstanceApplyDecisions.json
     */
    /**
     * Sample code: AccessReviewInstanceApplyDecisions.
     *
     * @param manager Entry point to AuthorizationManager.
     */
    public static void accessReviewInstanceApplyDecisions(
        com.azure.resourcemanager.authorization.generated.AuthorizationManager manager) {
        manager
            .accessReviewInstanceOperations()
            .applyDecisionsWithResponse(
                "fa73e90b-5bf1-45fd-a182-35ce5fc0674d", "d9b9e056-7004-470b-bf21-1635e98487da", Context.NONE);
    }
}
```

### AccessReviewInstanceOperation_ResetDecisions

```java
import com.azure.core.util.Context;

/** Samples for AccessReviewInstanceOperation ResetDecisions. */
public final class AccessReviewInstanceOperationResetDecisionsSamples {
    /*
     * x-ms-original-file: specification/authorization/resource-manager/Microsoft.Authorization/preview/2021-11-16-preview/examples/AccessReviewInstanceResetDecisions.json
     */
    /**
     * Sample code: AccessReviewInstanceResetDecisions.
     *
     * @param manager Entry point to AuthorizationManager.
     */
    public static void accessReviewInstanceResetDecisions(
        com.azure.resourcemanager.authorization.generated.AuthorizationManager manager) {
        manager
            .accessReviewInstanceOperations()
            .resetDecisionsWithResponse(
                "fa73e90b-5bf1-45fd-a182-35ce5fc0674d", "d9b9e056-7004-470b-bf21-1635e98487da", Context.NONE);
    }
}
```

### AccessReviewInstanceOperation_SendReminders

```java
import com.azure.core.util.Context;

/** Samples for AccessReviewInstanceOperation SendReminders. */
public final class AccessReviewInstanceOperationSendRemindersSamples {
    /*
     * x-ms-original-file: specification/authorization/resource-manager/Microsoft.Authorization/preview/2021-11-16-preview/examples/AccessReviewInstanceSendReminders.json
     */
    /**
     * Sample code: AccessReviewInstanceSendReminders.
     *
     * @param manager Entry point to AuthorizationManager.
     */
    public static void accessReviewInstanceSendReminders(
        com.azure.resourcemanager.authorization.generated.AuthorizationManager manager) {
        manager
            .accessReviewInstanceOperations()
            .sendRemindersWithResponse(
                "fa73e90b-5bf1-45fd-a182-35ce5fc0674d", "d9b9e056-7004-470b-bf21-1635e98487da", Context.NONE);
    }
}
```

### AccessReviewInstanceOperation_Stop

```java
import com.azure.core.util.Context;

/** Samples for AccessReviewInstanceOperation Stop. */
public final class AccessReviewInstanceOperationStopSamples {
    /*
     * x-ms-original-file: specification/authorization/resource-manager/Microsoft.Authorization/preview/2021-11-16-preview/examples/StopAccessReviewInstance.json
     */
    /**
     * Sample code: AccessReviewInstanceStop.
     *
     * @param manager Entry point to AuthorizationManager.
     */
    public static void accessReviewInstanceStop(
        com.azure.resourcemanager.authorization.generated.AuthorizationManager manager) {
        manager
            .accessReviewInstanceOperations()
            .stopWithResponse(
                "fa73e90b-5bf1-45fd-a182-35ce5fc0674d", "d9b9e056-7004-470b-bf21-1635e98487da", Context.NONE);
    }
}
```

### AccessReviewInstances_Create

```java
/** Samples for AccessReviewInstances Create. */
public final class AccessReviewInstancesCreateSamples {
    /*
     * x-ms-original-file: specification/authorization/resource-manager/Microsoft.Authorization/preview/2021-11-16-preview/examples/PutAccessReviewInstance.json
     */
    /**
     * Sample code: PutAccessReviewInstance.
     *
     * @param manager Entry point to AuthorizationManager.
     */
    public static void putAccessReviewInstance(
        com.azure.resourcemanager.authorization.generated.AuthorizationManager manager) {
        manager
            .accessReviewInstances()
            .define("4135f961-be78-4005-8101-c72a5af307a2")
            .withExistingAccessReviewScheduleDefinition("fa73e90b-5bf1-45fd-a182-35ce5fc0674d")
            .create();
    }
}
```

### AccessReviewInstances_GetById

```java
import com.azure.core.util.Context;

/** Samples for AccessReviewInstances GetById. */
public final class AccessReviewInstancesGetByIdSamples {
    /*
     * x-ms-original-file: specification/authorization/resource-manager/Microsoft.Authorization/preview/2021-11-16-preview/examples/GetAccessReviewInstance.json
     */
    /**
     * Sample code: GetAccessReviewInstance.
     *
     * @param manager Entry point to AuthorizationManager.
     */
    public static void getAccessReviewInstance(
        com.azure.resourcemanager.authorization.generated.AuthorizationManager manager) {
        manager
            .accessReviewInstances()
            .getByIdWithResponse(
                "fa73e90b-5bf1-45fd-a182-35ce5fc0674d", "4135f961-be78-4005-8101-c72a5af307a2", Context.NONE);
    }
}
```

### AccessReviewInstances_List

```java
import com.azure.core.util.Context;

/** Samples for AccessReviewInstances List. */
public final class AccessReviewInstancesListSamples {
    /*
     * x-ms-original-file: specification/authorization/resource-manager/Microsoft.Authorization/preview/2021-11-16-preview/examples/GetAccessReviewInstances.json
     */
    /**
     * Sample code: GetAccessReviewInstances.
     *
     * @param manager Entry point to AuthorizationManager.
     */
    public static void getAccessReviewInstances(
        com.azure.resourcemanager.authorization.generated.AuthorizationManager manager) {
        manager.accessReviewInstances().list("265785a7-a81f-4201-8a18-bb0db95982b7", null, Context.NONE);
    }
}
```

### AccessReviewInstancesAssignedForMyApproval_GetById

```java
import com.azure.core.util.Context;

/** Samples for AccessReviewInstancesAssignedForMyApproval GetById. */
public final class AccessReviewInstancesAssignedForMyApprovalGetByIdSamples {
    /*
     * x-ms-original-file: specification/authorization/resource-manager/Microsoft.Authorization/preview/2021-11-16-preview/examples/GetAccessReviewInstanceAssignedForMyApproval.json
     */
    /**
     * Sample code: GetAccessReviewInstanceAssignedForMyApproval.
     *
     * @param manager Entry point to AuthorizationManager.
     */
    public static void getAccessReviewInstanceAssignedForMyApproval(
        com.azure.resourcemanager.authorization.generated.AuthorizationManager manager) {
        manager
            .accessReviewInstancesAssignedForMyApprovals()
            .getByIdWithResponse(
                "488a6d0e-0a63-4946-86e3-1f5bbc934661", "4135f961-be78-4005-8101-c72a5af307a2", Context.NONE);
    }
}
```

### AccessReviewInstancesAssignedForMyApproval_List

```java
import com.azure.core.util.Context;

/** Samples for AccessReviewInstancesAssignedForMyApproval List. */
public final class AccessReviewInstancesAssignedForMyApprovalListSamples {
    /*
     * x-ms-original-file: specification/authorization/resource-manager/Microsoft.Authorization/preview/2021-11-16-preview/examples/GetAccessReviewInstancesAssignedForMyApproval.json
     */
    /**
     * Sample code: GetAccessReviewInstancesAssignedForMyApproval.
     *
     * @param manager Entry point to AuthorizationManager.
     */
    public static void getAccessReviewInstancesAssignedForMyApproval(
        com.azure.resourcemanager.authorization.generated.AuthorizationManager manager) {
        manager
            .accessReviewInstancesAssignedForMyApprovals()
            .list("488a6d0e-0a63-4946-86e3-1f5bbc934661", "assignedToMeToReview()", Context.NONE);
    }
}
```

### AccessReviewScheduleDefinitions_CreateOrUpdateById

```java
/** Samples for AccessReviewScheduleDefinitions CreateOrUpdateById. */
public final class AccessReviewScheduleDefinitionsCreateOrUpdateByIdSamples {
    /*
     * x-ms-original-file: specification/authorization/resource-manager/Microsoft.Authorization/preview/2021-11-16-preview/examples/PutAccessReviewScheduleDefinition.json
     */
    /**
     * Sample code: PutAccessReview.
     *
     * @param manager Entry point to AuthorizationManager.
     */
    public static void putAccessReview(com.azure.resourcemanager.authorization.generated.AuthorizationManager manager) {
        manager.accessReviewScheduleDefinitions().define("fa73e90b-5bf1-45fd-a182-35ce5fc0674d").create();
    }
}
```

### AccessReviewScheduleDefinitions_DeleteById

```java
import com.azure.core.util.Context;

/** Samples for AccessReviewScheduleDefinitions DeleteById. */
public final class AccessReviewScheduleDefinitionsDeleteByIdSamples {
    /*
     * x-ms-original-file: specification/authorization/resource-manager/Microsoft.Authorization/preview/2021-11-16-preview/examples/DeleteAccessReviewScheduleDefinition.json
     */
    /**
     * Sample code: DeleteAccessReview.
     *
     * @param manager Entry point to AuthorizationManager.
     */
    public static void deleteAccessReview(
        com.azure.resourcemanager.authorization.generated.AuthorizationManager manager) {
        manager
            .accessReviewScheduleDefinitions()
            .deleteByIdWithResponse("fa73e90b-5bf1-45fd-a182-35ce5fc0674d", Context.NONE);
    }
}
```

### AccessReviewScheduleDefinitions_GetById

```java
import com.azure.core.util.Context;

/** Samples for AccessReviewScheduleDefinitions GetById. */
public final class AccessReviewScheduleDefinitionsGetByIdSamples {
    /*
     * x-ms-original-file: specification/authorization/resource-manager/Microsoft.Authorization/preview/2021-11-16-preview/examples/GetAccessReviewScheduleDefinition.json
     */
    /**
     * Sample code: GetAccessReviewScheduleDefinition.
     *
     * @param manager Entry point to AuthorizationManager.
     */
    public static void getAccessReviewScheduleDefinition(
        com.azure.resourcemanager.authorization.generated.AuthorizationManager manager) {
        manager
            .accessReviewScheduleDefinitions()
            .getByIdWithResponse("fa73e90b-5bf1-45fd-a182-35ce5fc0674d", Context.NONE);
    }
}
```

### AccessReviewScheduleDefinitions_List

```java
import com.azure.core.util.Context;

/** Samples for AccessReviewScheduleDefinitions List. */
public final class AccessReviewScheduleDefinitionsListSamples {
    /*
     * x-ms-original-file: specification/authorization/resource-manager/Microsoft.Authorization/preview/2021-11-16-preview/examples/GetAccessReviewScheduleDefinitions.json
     */
    /**
     * Sample code: GetAccessReviewScheduleDefinitions.
     *
     * @param manager Entry point to AuthorizationManager.
     */
    public static void getAccessReviewScheduleDefinitions(
        com.azure.resourcemanager.authorization.generated.AuthorizationManager manager) {
        manager.accessReviewScheduleDefinitions().list(null, Context.NONE);
    }
}
```

### AccessReviewScheduleDefinitions_Stop

```java
import com.azure.core.util.Context;

/** Samples for AccessReviewScheduleDefinitions Stop. */
public final class AccessReviewScheduleDefinitionsStopSamples {
    /*
     * x-ms-original-file: specification/authorization/resource-manager/Microsoft.Authorization/preview/2021-11-16-preview/examples/StopAccessReviewScheduleDefinition.json
     */
    /**
     * Sample code: AccessReviewScheduleDefinitionStop.
     *
     * @param manager Entry point to AuthorizationManager.
     */
    public static void accessReviewScheduleDefinitionStop(
        com.azure.resourcemanager.authorization.generated.AuthorizationManager manager) {
        manager
            .accessReviewScheduleDefinitions()
            .stopWithResponse("fa73e90b-5bf1-45fd-a182-35ce5fc0674d", Context.NONE);
    }
}
```

### AccessReviewScheduleDefinitionsAssignedForMyApproval_List

```java
import com.azure.core.util.Context;

/** Samples for AccessReviewScheduleDefinitionsAssignedForMyApproval List. */
public final class AccessReviewScheduleDefinitionsAssignedForMyApprovalListSamples {
    /*
     * x-ms-original-file: specification/authorization/resource-manager/Microsoft.Authorization/preview/2021-11-16-preview/examples/GetAccessReviewScheduleDefinitionsAssignedForMyApproval.json
     */
    /**
     * Sample code: GetAccessReviewScheduleDefinitionsAssignedForMyApproval.
     *
     * @param manager Entry point to AuthorizationManager.
     */
    public static void getAccessReviewScheduleDefinitionsAssignedForMyApproval(
        com.azure.resourcemanager.authorization.generated.AuthorizationManager manager) {
        manager.accessReviewScheduleDefinitionsAssignedForMyApprovals().list("assignedToMeToReview()", Context.NONE);
    }
}
```

### TenantLevelAccessReviewInstanceContactedReviewers_List

```java
import com.azure.core.util.Context;

/** Samples for TenantLevelAccessReviewInstanceContactedReviewers List. */
public final class TenantLevelAccessReviewInstanceContactedReviewersListSamples {
    /*
     * x-ms-original-file: specification/authorization/resource-manager/Microsoft.Authorization/preview/2021-11-16-preview/examples/TenantLevelGetAccessReviewInstanceContactedReviewers.json
     */
    /**
     * Sample code: TenantLevelGetAccessReviewInstanceContactedReviewers.
     *
     * @param manager Entry point to AuthorizationManager.
     */
    public static void tenantLevelGetAccessReviewInstanceContactedReviewers(
        com.azure.resourcemanager.authorization.generated.AuthorizationManager manager) {
        manager
            .tenantLevelAccessReviewInstanceContactedReviewers()
            .list("265785a7-a81f-4201-8a18-bb0db95982b7", "f25ed880-9c31-4101-bc57-825d8df3b58c", Context.NONE);
    }
}
```

