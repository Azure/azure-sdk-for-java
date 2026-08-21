# Code snippets and samples


## GroupQuotaLimits

- [List](#groupquotalimits_list)

## GroupQuotaLimitsRequest

- [Get](#groupquotalimitsrequest_get)
- [List](#groupquotalimitsrequest_list)
- [Update](#groupquotalimitsrequest_update)

## GroupQuotaLocationSettings

- [CreateOrUpdate](#groupquotalocationsettings_createorupdate)
- [Get](#groupquotalocationsettings_get)
- [Update](#groupquotalocationsettings_update)

## GroupQuotaSubscriptionAllocation

- [List](#groupquotasubscriptionallocation_list)

## GroupQuotaSubscriptionAllocationRequest

- [Get](#groupquotasubscriptionallocationrequest_get)
- [List](#groupquotasubscriptionallocationrequest_list)
- [Update](#groupquotasubscriptionallocationrequest_update)

## GroupQuotaSubscriptionRequests

- [Get](#groupquotasubscriptionrequests_get)
- [List](#groupquotasubscriptionrequests_list)

## GroupQuotaSubscriptions

- [CreateOrUpdate](#groupquotasubscriptions_createorupdate)
- [Delete](#groupquotasubscriptions_delete)
- [Get](#groupquotasubscriptions_get)
- [List](#groupquotasubscriptions_list)
- [Update](#groupquotasubscriptions_update)

## GroupQuotaUsages

- [List](#groupquotausages_list)

## GroupQuotas

- [CreateOrUpdate](#groupquotas_createorupdate)
- [Delete](#groupquotas_delete)
- [Get](#groupquotas_get)
- [List](#groupquotas_list)
- [Update](#groupquotas_update)

## IncomingQuotaTransfers

- [Approve](#incomingquotatransfers_approve)
- [Get](#incomingquotatransfers_get)
- [List](#incomingquotatransfers_list)
- [ListBySubscription](#incomingquotatransfers_listbysubscription)
- [Reject](#incomingquotatransfers_reject)

## Quota

- [CreateOrUpdate](#quota_createorupdate)
- [Get](#quota_get)
- [List](#quota_list)
- [Update](#quota_update)

## QuotaOperation

- [List](#quotaoperation_list)

## QuotaRequestStatus

- [Get](#quotarequeststatus_get)
- [List](#quotarequeststatus_list)

## QuotaTransfers

- [Cancel](#quotatransfers_cancel)
- [CreateOrUpdate](#quotatransfers_createorupdate)
- [Delete](#quotatransfers_delete)
- [Get](#quotatransfers_get)
- [List](#quotatransfers_list)

## Usages

- [Get](#usages_get)
- [List](#usages_list)
### GroupQuotaLimits_List

```java
/**
 * Samples for GroupQuotaLimits List.
 */
public final class GroupQuotaLimitsListSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/GroupQuotaLimits/ListGroupQuotaLimits-Compute.json
     */
    /**
     * Sample code: GroupQuotaLimits_Get_Request_ForCompute.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void groupQuotaLimitsGetRequestForCompute(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.groupQuotaLimits()
            .listWithResponse("E7EC67B3-7657-4966-BFFC-41EFD36BAA09", "groupquota1", "Microsoft.Compute", "westus",
                com.azure.core.util.Context.NONE);
    }
}
```

### GroupQuotaLimitsRequest_Get

```java
/**
 * Samples for GroupQuotaLimitsRequest Get.
 */
public final class GroupQuotaLimitsRequestGetSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/GroupQuotaLimitsRequests/GroupQuotaLimitsRequests_Get.json
     */
    /**
     * Sample code: GroupQuotaLimitsRequests_Get.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void groupQuotaLimitsRequestsGet(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.groupQuotaLimitsRequests()
            .getWithResponse("E7EC67B3-7657-4966-BFFC-41EFD36BAA09", "groupquota1", "requestId",
                com.azure.core.util.Context.NONE);
    }
}
```

### GroupQuotaLimitsRequest_List

```java
/**
 * Samples for GroupQuotaLimitsRequest List.
 */
public final class GroupQuotaLimitsRequestListSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/GroupQuotaLimitsRequests/GroupQuotaLimitsRequests_List.json
     */
    /**
     * Sample code: GroupQuotaLimitsRequest_List.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void groupQuotaLimitsRequestList(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.groupQuotaLimitsRequests()
            .list("E7EC67B3-7657-4966-BFFC-41EFD36BAA09", "groupquota1", "Microsoft.Compute", "location eq westus",
                com.azure.core.util.Context.NONE);
    }
}
```

### GroupQuotaLimitsRequest_Update

```java
import com.azure.resourcemanager.quota.fluent.models.GroupQuotaLimitListInner;
import com.azure.resourcemanager.quota.models.GroupQuotaLimit;
import com.azure.resourcemanager.quota.models.GroupQuotaLimitListProperties;
import com.azure.resourcemanager.quota.models.GroupQuotaLimitProperties;
import java.util.Arrays;

/**
 * Samples for GroupQuotaLimitsRequest Update.
 */
public final class GroupQuotaLimitsRequestUpdateSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/GroupQuotaLimitsRequests/PatchGroupQuotaLimitsRequests-Compute.json
     */
    /**
     * Sample code: GroupQuotaLimitsRequests_Update.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void groupQuotaLimitsRequestsUpdate(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.groupQuotaLimitsRequests()
            .update("E7EC67B3-7657-4966-BFFC-41EFD36BAA09", "groupquota1", "Microsoft.Compute", "westus",
                new GroupQuotaLimitListInner()
                    .withProperties(new GroupQuotaLimitListProperties().withValue(Arrays.asList(
                        new GroupQuotaLimit()
                            .withProperties(new GroupQuotaLimitProperties().withResourceName("standardddv4family")
                                .withLimit(110L)
                                .withComment("Contoso requires more quota.")),
                        new GroupQuotaLimit()
                            .withProperties(new GroupQuotaLimitProperties().withResourceName("standardav2family")
                                .withLimit(110L)
                                .withComment("Contoso requires more quota."))))),
                com.azure.core.util.Context.NONE);
    }
}
```

### GroupQuotaLocationSettings_CreateOrUpdate

```java
import com.azure.resourcemanager.quota.fluent.models.GroupQuotasEnforcementStatusInner;
import com.azure.resourcemanager.quota.models.EnforcementState;
import com.azure.resourcemanager.quota.models.GroupQuotasEnforcementStatusProperties;

/**
 * Samples for GroupQuotaLocationSettings CreateOrUpdate.
 */
public final class GroupQuotaLocationSettingsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/GroupQuotasEnforcement/PutGroupQuotaEnforcement.json
     */
    /**
     * Sample code: GroupQuotaLocationSettings_CreateOrUpdate.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void groupQuotaLocationSettingsCreateOrUpdate(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.groupQuotaLocationSettings()
            .createOrUpdate("E7EC67B3-7657-4966-BFFC-41EFD36BAA09", "groupquota1", "Microsoft.Compute", "eastus",
                new GroupQuotasEnforcementStatusInner().withProperties(
                    new GroupQuotasEnforcementStatusProperties().withEnforcementEnabled(EnforcementState.ENABLED)),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-09-01-preview/GroupQuotasEnforcement/PutGroupQuotaEnforcementFailed.json
     */
    /**
     * Sample code: GroupQuotaLocationSettings_CreateOrUpdate_Failed.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void
        groupQuotaLocationSettingsCreateOrUpdateFailed(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.groupQuotaLocationSettings()
            .createOrUpdate("E7EC67B3-7657-4966-BFFC-41EFD36BAA09", "groupquota1", "Microsoft.Compute", "eastus",
                new GroupQuotasEnforcementStatusInner().withProperties(
                    new GroupQuotasEnforcementStatusProperties().withEnforcementEnabled(EnforcementState.ENABLED)),
                com.azure.core.util.Context.NONE);
    }
}
```

### GroupQuotaLocationSettings_Get

```java
/**
 * Samples for GroupQuotaLocationSettings Get.
 */
public final class GroupQuotaLocationSettingsGetSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/GroupQuotasEnforcement/GetGroupQuotaEnforcement.json
     */
    /**
     * Sample code: GroupQuotasEnforcement_Get.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void groupQuotasEnforcementGet(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.groupQuotaLocationSettings()
            .getWithResponse("E7EC67B3-7657-4966-BFFC-41EFD36BAA09", "groupquota1", "Microsoft.Compute", "eastus",
                com.azure.core.util.Context.NONE);
    }
}
```

### GroupQuotaLocationSettings_Update

```java
import com.azure.resourcemanager.quota.fluent.models.GroupQuotasEnforcementStatusInner;
import com.azure.resourcemanager.quota.models.EnforcementState;
import com.azure.resourcemanager.quota.models.GroupQuotasEnforcementStatusProperties;

/**
 * Samples for GroupQuotaLocationSettings Update.
 */
public final class GroupQuotaLocationSettingsUpdateSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/GroupQuotasEnforcement/PatchGroupQuotaEnforcement.json
     */
    /**
     * Sample code: GroupQuotaLocationSettings_Patch.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void groupQuotaLocationSettingsPatch(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.groupQuotaLocationSettings()
            .update("E7EC67B3-7657-4966-BFFC-41EFD36BAA09", "groupquota1", "Microsoft.Compute", "eastus",
                new GroupQuotasEnforcementStatusInner().withProperties(
                    new GroupQuotasEnforcementStatusProperties().withEnforcementEnabled(EnforcementState.ENABLED)),
                com.azure.core.util.Context.NONE);
    }
}
```

### GroupQuotaSubscriptionAllocation_List

```java
/**
 * Samples for GroupQuotaSubscriptionAllocation List.
 */
public final class GroupQuotaSubscriptionAllocationListSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/SubscriptionQuotaAllocation/SubscriptionQuotaAllocation_List-Compute.json
     */
    /**
     * Sample code: SubscriptionQuotaAllocation_List_ForCompute.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void subscriptionQuotaAllocationListForCompute(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.groupQuotaSubscriptionAllocations()
            .listWithResponse("E7EC67B3-7657-4966-BFFC-41EFD36BAA09", "groupquota1", "Microsoft.Compute", "westus",
                com.azure.core.util.Context.NONE);
    }
}
```

### GroupQuotaSubscriptionAllocationRequest_Get

```java
/**
 * Samples for GroupQuotaSubscriptionAllocationRequest Get.
 */
public final class GroupQuotaSubscriptionAllocationRequestGetSamples {
    /*
     * x-ms-original-file:
     * 2026-09-01-preview/SubscriptionQuotaAllocationRequests/SubscriptionQuotaAllocationRequests_Get-Compute.json
     */
    /**
     * Sample code: SubscriptionQuotaAllocationRequests_Get_Request_ForCompute.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void
        subscriptionQuotaAllocationRequestsGetRequestForCompute(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.groupQuotaSubscriptionAllocationRequests()
            .getWithResponse("E7EC67B3-7657-4966-BFFC-41EFD36BAA09", "groupquota1", "Microsoft.Compute",
                "AE000000-0000-0000-0000-00000000000A", com.azure.core.util.Context.NONE);
    }
}
```

### GroupQuotaSubscriptionAllocationRequest_List

```java
/**
 * Samples for GroupQuotaSubscriptionAllocationRequest List.
 */
public final class GroupQuotaSubscriptionAllocationRequestListSamples {
    /*
     * x-ms-original-file:
     * 2026-09-01-preview/SubscriptionQuotaAllocationRequests/SubscriptionQuotaAllocationRequests_List-Compute.json
     */
    /**
     * Sample code: SubscriptionQuotaAllocation_List_Request_ForCompute.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void
        subscriptionQuotaAllocationListRequestForCompute(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.groupQuotaSubscriptionAllocationRequests()
            .list("E7EC67B3-7657-4966-BFFC-41EFD36BAA09", "groupquota1", "Microsoft.Compute", "location eq westus",
                com.azure.core.util.Context.NONE);
    }
}
```

### GroupQuotaSubscriptionAllocationRequest_Update

```java
import com.azure.resourcemanager.quota.fluent.models.SubscriptionQuotaAllocationsListInner;
import com.azure.resourcemanager.quota.models.SubscriptionQuotaAllocations;
import com.azure.resourcemanager.quota.models.SubscriptionQuotaAllocationsListProperties;
import com.azure.resourcemanager.quota.models.SubscriptionQuotaAllocationsProperties;
import java.util.Arrays;

/**
 * Samples for GroupQuotaSubscriptionAllocationRequest Update.
 */
public final class GroupQuotaSubscriptionAllocationRequestUpdateSamples {
    /*
     * x-ms-original-file:
     * 2026-09-01-preview/SubscriptionQuotaAllocationRequests/PatchSubscriptionQuotaAllocationRequest-Compute.json
     */
    /**
     * Sample code: SubscriptionQuotaAllocation_Patch_Request_ForCompute.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void
        subscriptionQuotaAllocationPatchRequestForCompute(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.groupQuotaSubscriptionAllocationRequests()
            .update("E7EC67B3-7657-4966-BFFC-41EFD36BAA09", "groupquota1", "Microsoft.Compute", "westus",
                new SubscriptionQuotaAllocationsListInner()
                    .withProperties(new SubscriptionQuotaAllocationsListProperties().withValue(Arrays.asList(
                        new SubscriptionQuotaAllocations().withProperties(
                            new SubscriptionQuotaAllocationsProperties().withResourceName("standardddv4family")
                                .withLimit(110L)),
                        new SubscriptionQuotaAllocations().withProperties(
                            new SubscriptionQuotaAllocationsProperties().withResourceName("standardav2family")
                                .withLimit(110L))))),
                com.azure.core.util.Context.NONE);
    }
}
```

### GroupQuotaSubscriptionRequests_Get

```java
/**
 * Samples for GroupQuotaSubscriptionRequests Get.
 */
public final class GroupQuotaSubscriptionRequestsGetSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/SubscriptionRequests/SubscriptionRequests_Get.json
     */
    /**
     * Sample code: GroupQuotaSubscriptionRequests_Get.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void groupQuotaSubscriptionRequestsGet(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.groupQuotaSubscriptionRequests()
            .getWithResponse("E7EC67B3-7657-4966-BFFC-41EFD36BAA09", "groupquota1",
                "00000000-0000-0000-0000-000000000000", com.azure.core.util.Context.NONE);
    }
}
```

### GroupQuotaSubscriptionRequests_List

```java
/**
 * Samples for GroupQuotaSubscriptionRequests List.
 */
public final class GroupQuotaSubscriptionRequestsListSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/SubscriptionRequests/SubscriptionRequests_List.json
     */
    /**
     * Sample code: GroupQuotaSubscriptionRequests_List.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void groupQuotaSubscriptionRequestsList(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.groupQuotaSubscriptionRequests()
            .list("E7EC67B3-7657-4966-BFFC-41EFD36BAA09", "groupquota1", com.azure.core.util.Context.NONE);
    }
}
```

### GroupQuotaSubscriptions_CreateOrUpdate

```java
/**
 * Samples for GroupQuotaSubscriptions CreateOrUpdate.
 */
public final class GroupQuotaSubscriptionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/GroupQuotasSubscriptions/PutGroupQuotasSubscription.json
     */
    /**
     * Sample code: GroupQuotaSubscriptions_Put_Subscriptions.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void groupQuotaSubscriptionsPutSubscriptions(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.groupQuotaSubscriptions()
            .createOrUpdate("E7EC67B3-7657-4966-BFFC-41EFD36BAA09", "groupquota1", com.azure.core.util.Context.NONE);
    }
}
```

### GroupQuotaSubscriptions_Delete

```java
/**
 * Samples for GroupQuotaSubscriptions Delete.
 */
public final class GroupQuotaSubscriptionsDeleteSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/GroupQuotasSubscriptions/DeleteGroupQuotaSubscriptions.json
     */
    /**
     * Sample code: GroupQuotaSubscriptions_Delete_Subscriptions.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void
        groupQuotaSubscriptionsDeleteSubscriptions(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.groupQuotaSubscriptions()
            .delete("E7EC67B3-7657-4966-BFFC-41EFD36BAA09", "groupquota1", com.azure.core.util.Context.NONE);
    }
}
```

### GroupQuotaSubscriptions_Get

```java
/**
 * Samples for GroupQuotaSubscriptions Get.
 */
public final class GroupQuotaSubscriptionsGetSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/GroupQuotasSubscriptions/GetGroupQuotaSubscriptions.json
     */
    /**
     * Sample code: GroupQuotaSubscriptions_Get_Subscriptions.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void groupQuotaSubscriptionsGetSubscriptions(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.groupQuotaSubscriptions()
            .getWithResponse("E7EC67B3-7657-4966-BFFC-41EFD36BAA09", "groupquota1", com.azure.core.util.Context.NONE);
    }
}
```

### GroupQuotaSubscriptions_List

```java
/**
 * Samples for GroupQuotaSubscriptions List.
 */
public final class GroupQuotaSubscriptionsListSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/GroupQuotasSubscriptions/ListGroupQuotaSubscriptions.json
     */
    /**
     * Sample code: GroupQuotaSubscriptions_List_Subscriptions.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void groupQuotaSubscriptionsListSubscriptions(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.groupQuotaSubscriptions()
            .list("E7EC67B3-7657-4966-BFFC-41EFD36BAA09", "groupquota1", com.azure.core.util.Context.NONE);
    }
}
```

### GroupQuotaSubscriptions_Update

```java
/**
 * Samples for GroupQuotaSubscriptions Update.
 */
public final class GroupQuotaSubscriptionsUpdateSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/GroupQuotasSubscriptions/PatchGroupQuotasSubscription.json
     */
    /**
     * Sample code: GroupQuotaSubscriptions_Patch_Subscriptions.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void groupQuotaSubscriptionsPatchSubscriptions(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.groupQuotaSubscriptions()
            .update("E7EC67B3-7657-4966-BFFC-41EFD36BAA09", "groupquota1", com.azure.core.util.Context.NONE);
    }
}
```

### GroupQuotaUsages_List

```java
/**
 * Samples for GroupQuotaUsages List.
 */
public final class GroupQuotaUsagesListSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/GroupQuotaUsages/GetGroupQuotaUsages.json
     */
    /**
     * Sample code: GroupQuotasUsages_List.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void groupQuotasUsagesList(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.groupQuotaUsages()
            .list("E7EC67B3-7657-4966-BFFC-41EFD36BAA09", "groupquota1", "Microsoft.Compute", "westus",
                com.azure.core.util.Context.NONE);
    }
}
```

### GroupQuotas_CreateOrUpdate

```java
import com.azure.resourcemanager.quota.fluent.models.GroupQuotasEntityInner;
import com.azure.resourcemanager.quota.models.GroupQuotasEntityProperties;

/**
 * Samples for GroupQuotas CreateOrUpdate.
 */
public final class GroupQuotasCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/GroupQuotas/PutGroupQuotas.json
     */
    /**
     * Sample code: GroupQuotas_Put_Request_ForCompute.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void groupQuotasPutRequestForCompute(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.groupQuotas()
            .createOrUpdate("E7EC67B3-7657-4966-BFFC-41EFD36BAA09", "groupquota1",
                new GroupQuotasEntityInner()
                    .withProperties(new GroupQuotasEntityProperties().withDisplayName("GroupQuota1")),
                com.azure.core.util.Context.NONE);
    }
}
```

### GroupQuotas_Delete

```java
/**
 * Samples for GroupQuotas Delete.
 */
public final class GroupQuotasDeleteSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/GroupQuotas/DeleteGroupQuotas.json
     */
    /**
     * Sample code: GroupQuotas_Delete_Request_ForCompute.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void groupQuotasDeleteRequestForCompute(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.groupQuotas()
            .delete("E7EC67B3-7657-4966-BFFC-41EFD36BAA09", "groupquota1", com.azure.core.util.Context.NONE);
    }
}
```

### GroupQuotas_Get

```java
/**
 * Samples for GroupQuotas Get.
 */
public final class GroupQuotasGetSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/GroupQuotas/GetGroupQuotas.json
     */
    /**
     * Sample code: GroupQuotas_Get_Request_ForCompute.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void groupQuotasGetRequestForCompute(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.groupQuotas()
            .getWithResponse("E7EC67B3-7657-4966-BFFC-41EFD36BAA09", "groupquota1", com.azure.core.util.Context.NONE);
    }
}
```

### GroupQuotas_List

```java
/**
 * Samples for GroupQuotas List.
 */
public final class GroupQuotasListSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/GroupQuotas/ListGroupQuotas.json
     */
    /**
     * Sample code: GroupQuotas_List_Request_ForCompute.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void groupQuotasListRequestForCompute(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.groupQuotas().list("E7EC67B3-7657-4966-BFFC-41EFD36BAA09", com.azure.core.util.Context.NONE);
    }
}
```

### GroupQuotas_Update

```java
import com.azure.resourcemanager.quota.models.GroupQuotasEntityPatch;
import com.azure.resourcemanager.quota.models.GroupQuotasEntityPatchProperties;

/**
 * Samples for GroupQuotas Update.
 */
public final class GroupQuotasUpdateSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/GroupQuotas/PatchGroupQuotas.json
     */
    /**
     * Sample code: GroupQuotas_Patch_Request_ForCompute.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void groupQuotasPatchRequestForCompute(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.groupQuotas()
            .update("E7EC67B3-7657-4966-BFFC-41EFD36BAA09", "groupquota1",
                new GroupQuotasEntityPatch()
                    .withProperties(new GroupQuotasEntityPatchProperties().withDisplayName("UpdatedGroupQuota1")),
                com.azure.core.util.Context.NONE);
    }
}
```

### IncomingQuotaTransfers_Approve

```java
import com.azure.resourcemanager.quota.models.IncomingQuotaTransferApproveRequest;

/**
 * Samples for IncomingQuotaTransfers Approve.
 */
public final class IncomingQuotaTransfersApproveSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/IncomingQuotaTransfers/IncomingQuotaTransfers_Approve.json
     */
    /**
     * Sample code: IncomingQuotaTransfers_Approve.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void incomingQuotaTransfersApprove(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.incomingQuotaTransfers()
            .approve("Microsoft.Compute", "eastus", "12345678-1234-1234-1234-1234567890ab", "abc123",
                new IncomingQuotaTransferApproveRequest().withComment("Approved by capacity team."),
                com.azure.core.util.Context.NONE);
    }
}
```

### IncomingQuotaTransfers_Get

```java
/**
 * Samples for IncomingQuotaTransfers Get.
 */
public final class IncomingQuotaTransfersGetSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/IncomingQuotaTransfers/IncomingQuotaTransfers_Get.json
     */
    /**
     * Sample code: IncomingQuotaTransfers_Get.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void incomingQuotaTransfersGet(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.incomingQuotaTransfers()
            .getWithResponse("Microsoft.Compute", "eastus", "12345678-1234-1234-1234-1234567890ab",
                com.azure.core.util.Context.NONE);
    }
}
```

### IncomingQuotaTransfers_List

```java
/**
 * Samples for IncomingQuotaTransfers List.
 */
public final class IncomingQuotaTransfersListSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/IncomingQuotaTransfers/IncomingQuotaTransfers_List.json
     */
    /**
     * Sample code: IncomingQuotaTransfers_List.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void incomingQuotaTransfersList(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.incomingQuotaTransfers().list("Microsoft.Compute", "eastus", com.azure.core.util.Context.NONE);
    }
}
```

### IncomingQuotaTransfers_ListBySubscription

```java
/**
 * Samples for IncomingQuotaTransfers ListBySubscription.
 */
public final class IncomingQuotaTransfersListBySubscriptionSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/IncomingQuotaTransfers/IncomingQuotaTransfers_ListBySubscription.json
     */
    /**
     * Sample code: IncomingQuotaTransfers_ListBySubscription.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void incomingQuotaTransfersListBySubscription(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.incomingQuotaTransfers().listBySubscription(com.azure.core.util.Context.NONE);
    }
}
```

### IncomingQuotaTransfers_Reject

```java
import com.azure.resourcemanager.quota.models.IncomingQuotaTransferRejectRequest;

/**
 * Samples for IncomingQuotaTransfers Reject.
 */
public final class IncomingQuotaTransfersRejectSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/IncomingQuotaTransfers/IncomingQuotaTransfers_Reject.json
     */
    /**
     * Sample code: IncomingQuotaTransfers_Reject.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void incomingQuotaTransfersReject(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.incomingQuotaTransfers()
            .rejectWithResponse("Microsoft.Compute", "eastus", "12345678-1234-1234-1234-1234567890ab", "abc123",
                new IncomingQuotaTransferRejectRequest().withReason("Recipient capacity already satisfied."),
                com.azure.core.util.Context.NONE);
    }
}
```

### Quota_CreateOrUpdate

```java
import com.azure.resourcemanager.quota.models.LimitObject;
import com.azure.resourcemanager.quota.models.QuotaProperties;
import com.azure.resourcemanager.quota.models.ResourceName;

/**
 * Samples for Quota CreateOrUpdate.
 */
public final class QuotaCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/putMachineLearningServicesQuotaRequestLowPriority.json
     */
    /**
     * Sample code: Quotas_Request_ForMachineLearningServices_LowPriorityResource.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void quotasRequestForMachineLearningServicesLowPriorityResource(
        com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.quotas()
            .define("TotalLowPriorityCores")
            .withExistingScope(
                "subscriptions/D7EC67B3-7657-4966-BFFC-41EFD36BAAB3/providers/Microsoft.MachineLearningServices/locations/eastus")
            .withProperties(new QuotaProperties().withLimit(new LimitObject().withValue(10))
                .withName(new ResourceName().withValue("TotalLowPriorityCores"))
                .withResourceType("lowPriority"))
            .create();
    }

    /*
     * x-ms-original-file: 2026-09-01-preview/putComputeOneSkuQuotaRequest.json
     */
    /**
     * Sample code: Quotas_Put_Request_ForCompute.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void quotasPutRequestForCompute(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.quotas()
            .define("standardFSv2Family")
            .withExistingScope(
                "subscriptions/D7EC67B3-7657-4966-BFFC-41EFD36BAAB3/providers/Microsoft.Compute/locations/eastus")
            .withProperties(new QuotaProperties().withLimit(new LimitObject().withValue(10))
                .withName(new ResourceName().withValue("standardFSv2Family")))
            .create();
    }

    /*
     * x-ms-original-file: 2026-09-01-preview/putNetworkOneSkuQuotaRequestStandardSkuPublicIpAddresses.json
     */
    /**
     * Sample code: Quotas_PutRequest_ForNetwork_StandardSkuPublicIpAddressesResource.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void quotasPutRequestForNetworkStandardSkuPublicIpAddressesResource(
        com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.quotas()
            .define("StandardSkuPublicIpAddresses")
            .withExistingScope(
                "subscriptions/D7EC67B3-7657-4966-BFFC-41EFD36BAAB3/providers/Microsoft.Network/locations/eastus")
            .withProperties(new QuotaProperties().withLimit(new LimitObject().withValue(10))
                .withName(new ResourceName().withValue("StandardSkuPublicIpAddresses"))
                .withResourceType("PublicIpAddresses"))
            .create();
    }

    /*
     * x-ms-original-file: 2026-09-01-preview/putNetworkOneSkuQuotaRequest.json
     */
    /**
     * Sample code: Quotas_PutRequest_ForNetwork.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void quotasPutRequestForNetwork(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.quotas()
            .define("MinPublicIpInterNetworkPrefixLength")
            .withExistingScope(
                "subscriptions/D7EC67B3-7657-4966-BFFC-41EFD36BAAB3/providers/Microsoft.Network/locations/eastus")
            .withProperties(new QuotaProperties().withLimit(new LimitObject().withValue(10))
                .withName(new ResourceName().withValue("MinPublicIpInterNetworkPrefixLength"))
                .withResourceType("MinPublicIpInterNetworkPrefixLength"))
            .create();
    }
}
```

### Quota_Get

```java
/**
 * Samples for Quota Get.
 */
public final class QuotaGetSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/getNetworkOneSkuQuotaLimit.json
     */
    /**
     * Sample code: Quotas_UsagesRequest_ForNetwork.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void quotasUsagesRequestForNetwork(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.quotas()
            .getWithResponse("MinPublicIpInterNetworkPrefixLength",
                "subscriptions/00000000-0000-0000-0000-000000000000/providers/Microsoft.Network/locations/eastus",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-09-01-preview/getComputeOneSkuQuotaLimit.json
     */
    /**
     * Sample code: Quotas_Get_Request_ForCompute.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void quotasGetRequestForCompute(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.quotas()
            .getWithResponse("standardNDSFamily",
                "subscriptions/00000000-0000-0000-0000-000000000000/providers/Microsoft.Compute/locations/eastus",
                com.azure.core.util.Context.NONE);
    }
}
```

### Quota_List

```java
/**
 * Samples for Quota List.
 */
public final class QuotaListSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/getMachineLearningServicesQuotaLimits.json
     */
    /**
     * Sample code: Quotas_listQuotaLimitsMachineLearningServices.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void
        quotasListQuotaLimitsMachineLearningServices(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.quotas()
            .list(
                "subscriptions/00000000-0000-0000-0000-000000000000/providers/Microsoft.MachineLearningServices/locations/eastus",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-09-01-preview/getComputeQuotaLimits.json
     */
    /**
     * Sample code: Quotas_listQuotaLimitsForCompute.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void quotasListQuotaLimitsForCompute(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.quotas()
            .list("subscriptions/00000000-0000-0000-0000-000000000000/providers/Microsoft.Compute/locations/eastus",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-09-01-preview/getNetworkQuotaLimits.json
     */
    /**
     * Sample code: Quotas_listQuotaLimitsForNetwork.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void quotasListQuotaLimitsForNetwork(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.quotas()
            .list("subscriptions/00000000-0000-0000-0000-000000000000/providers/Microsoft.Network/locations/eastus",
                com.azure.core.util.Context.NONE);
    }
}
```

### Quota_Update

```java
import com.azure.resourcemanager.quota.models.CurrentQuotaLimitBase;
import com.azure.resourcemanager.quota.models.LimitObject;
import com.azure.resourcemanager.quota.models.QuotaProperties;
import com.azure.resourcemanager.quota.models.ResourceName;

/**
 * Samples for Quota Update.
 */
public final class QuotaUpdateSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/patchComputeQuotaRequest.json
     */
    /**
     * Sample code: Quotas_Request_PatchForCompute.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void quotasRequestPatchForCompute(com.azure.resourcemanager.quota.QuotaManager manager) {
        CurrentQuotaLimitBase resource = manager.quotas()
            .getWithResponse("standardFSv2Family",
                "subscriptions/D7EC67B3-7657-4966-BFFC-41EFD36BAAB3/providers/Microsoft.Compute/locations/eastus",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new QuotaProperties().withLimit(new LimitObject().withValue(10))
                .withName(new ResourceName().withValue("standardFSv2Family")))
            .apply();
    }

    /*
     * x-ms-original-file: 2026-09-01-preview/patchNetworkOneSkuQuotaRequest.json
     */
    /**
     * Sample code: Quotas_Request_PatchForNetwork.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void quotasRequestPatchForNetwork(com.azure.resourcemanager.quota.QuotaManager manager) {
        CurrentQuotaLimitBase resource = manager.quotas()
            .getWithResponse("MinPublicIpInterNetworkPrefixLength",
                "subscriptions/D7EC67B3-7657-4966-BFFC-41EFD36BAAB3/providers/Microsoft.Network/locations/eastus",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new QuotaProperties().withLimit(new LimitObject().withValue(10))
                .withName(new ResourceName().withValue("MinPublicIpInterNetworkPrefixLength"))
                .withResourceType("MinPublicIpInterNetworkPrefixLength"))
            .apply();
    }
}
```

### QuotaOperation_List

```java
/**
 * Samples for QuotaOperation List.
 */
public final class QuotaOperationListSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/GetOperations.json
     */
    /**
     * Sample code: GetOperations.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void getOperations(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.quotaOperations().list(com.azure.core.util.Context.NONE);
    }
}
```

### QuotaRequestStatus_Get

```java
/**
 * Samples for QuotaRequestStatus Get.
 */
public final class QuotaRequestStatusGetSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/getQuotaRequestStatusFailed.json
     */
    /**
     * Sample code: QuotaRequestFailed.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void quotaRequestFailed(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.quotaRequestStatus()
            .getWithResponse("2B5C8515-37D8-4B6A-879B-CD641A2CF605",
                "subscriptions/00000000-0000-0000-0000-000000000000/providers/Microsoft.Compute/locations/eastus",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-09-01-preview/getQuotaRequestStatusById.json
     */
    /**
     * Sample code: QuotaRequestStatus.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void quotaRequestStatus(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.quotaRequestStatus()
            .getWithResponse("2B5C8515-37D8-4B6A-879B-CD641A2CF605",
                "subscriptions/D7EC67B3-7657-4966-BFFC-41EFD36BAAB3/providers/Microsoft.Compute/locations/eastus",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-09-01-preview/getQuotaRequestStatusInProgress.json
     */
    /**
     * Sample code: QuotaRequestInProgress.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void quotaRequestInProgress(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.quotaRequestStatus()
            .getWithResponse("2B5C8515-37D8-4B6A-879B-CD641A2CF605",
                "subscriptions/00000000-0000-0000-0000-000000000000/providers/Microsoft.Compute/locations/eastus",
                com.azure.core.util.Context.NONE);
    }
}
```

### QuotaRequestStatus_List

```java
/**
 * Samples for QuotaRequestStatus List.
 */
public final class QuotaRequestStatusListSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/getQuotaRequestsHistory.json
     */
    /**
     * Sample code: QuotaRequestHistory.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void quotaRequestHistory(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.quotaRequestStatus()
            .list("subscriptions/D7EC67B3-7657-4966-BFFC-41EFD36BAAB3/providers/Microsoft.Compute/locations/eastus",
                null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### QuotaTransfers_Cancel

```java
import com.azure.resourcemanager.quota.models.QuotaTransferCancelRequest;

/**
 * Samples for QuotaTransfers Cancel.
 */
public final class QuotaTransfersCancelSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/QuotaTransfers/QuotaTransfers_Cancel.json
     */
    /**
     * Sample code: QuotaTransfers_Cancel.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void quotaTransfersCancel(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.quotaTransfers()
            .cancelWithResponse("Microsoft.Compute", "eastus", "compute-stdDv5-uplift-101",
                new QuotaTransferCancelRequest().withReason("Donor changed plans before recipient acted."),
                com.azure.core.util.Context.NONE);
    }
}
```

### QuotaTransfers_CreateOrUpdate

```java
import com.azure.resourcemanager.quota.models.QuotaTransferProperties;

/**
 * Samples for QuotaTransfers CreateOrUpdate.
 */
public final class QuotaTransfersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/QuotaTransfers/QuotaTransfers_CreateOrUpdate_AutoApprove.json
     */
    /**
     * Sample code: QuotaTransfers_CreateOrUpdate - autoApprove same-tenant.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void
        quotaTransfersCreateOrUpdateAutoApproveSameTenant(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.quotaTransfers()
            .define("compute-stdDv5-autoapprove-202")
            .withExistingLocation("Microsoft.Compute", "eastus")
            .withProperties(new QuotaTransferProperties().withDisplayName("Move 25 Dv5 vCPU - auto approved")
                .withDestinationSubscriptionId("aaaaaaaa-bbbb-cccc-dddd-000000000002")
                .withBillingAccountId("1234567890")
                .withResourceName("standardDv5Family")
                .withAmount(25L)
                .withAutoApprove(true))
            .create();
    }

    /*
     * x-ms-original-file: 2026-09-01-preview/QuotaTransfers/QuotaTransfers_CreateOrUpdate.json
     */
    /**
     * Sample code: QuotaTransfers_CreateOrUpdate - donor submit.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void quotaTransfersCreateOrUpdateDonorSubmit(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.quotaTransfers()
            .define("compute-stdDv5-uplift-101")
            .withExistingLocation("Microsoft.Compute", "eastus")
            .withProperties(new QuotaTransferProperties().withDisplayName("Move 50 Dv5 vCPU to recipient")
                .withComment("Backfill for new prod fleet rollout.")
                .withDestinationSubscriptionId("aaaaaaaa-bbbb-cccc-dddd-000000000002")
                .withBillingAccountId("1234567890")
                .withResourceName("standardDv5Family")
                .withAmount(50L)
                .withAutoApprove(false))
            .create();
    }
}
```

### QuotaTransfers_Delete

```java
/**
 * Samples for QuotaTransfers Delete.
 */
public final class QuotaTransfersDeleteSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/QuotaTransfers/QuotaTransfers_Delete.json
     */
    /**
     * Sample code: QuotaTransfers_Delete.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void quotaTransfersDelete(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.quotaTransfers()
            .deleteWithResponse("Microsoft.Compute", "eastus", "compute-stdDv5-uplift-101",
                com.azure.core.util.Context.NONE);
    }
}
```

### QuotaTransfers_Get

```java
/**
 * Samples for QuotaTransfers Get.
 */
public final class QuotaTransfersGetSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/QuotaTransfers/QuotaTransfers_Get.json
     */
    /**
     * Sample code: QuotaTransfers_Get.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void quotaTransfersGet(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.quotaTransfers()
            .getWithResponse("Microsoft.Compute", "eastus", "compute-stdDv5-uplift-101",
                com.azure.core.util.Context.NONE);
    }
}
```

### QuotaTransfers_List

```java
/**
 * Samples for QuotaTransfers List.
 */
public final class QuotaTransfersListSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/QuotaTransfers/QuotaTransfers_List.json
     */
    /**
     * Sample code: QuotaTransfers_List.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void quotaTransfersList(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.quotaTransfers().list("Microsoft.Compute", "eastus", com.azure.core.util.Context.NONE);
    }
}
```

### Usages_Get

```java
/**
 * Samples for Usages Get.
 */
public final class UsagesGetSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/getNetworkOneSkuUsages.json
     */
    /**
     * Sample code: Quotas_UsagesRequest_ForNetwork.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void quotasUsagesRequestForNetwork(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.usages()
            .getWithResponse("MinPublicIpInterNetworkPrefixLength",
                "subscriptions/00000000-0000-0000-0000-000000000000/providers/Microsoft.Network/locations/eastus",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-09-01-preview/getComputeOneSkuUsages.json
     */
    /**
     * Sample code: Quotas_UsagesRequest_ForCompute.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void quotasUsagesRequestForCompute(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.usages()
            .getWithResponse("standardNDSFamily",
                "subscriptions/00000000-0000-0000-0000-000000000000/providers/Microsoft.Compute/locations/eastus",
                com.azure.core.util.Context.NONE);
    }
}
```

### Usages_List

```java
/**
 * Samples for Usages List.
 */
public final class UsagesListSamples {
    /*
     * x-ms-original-file: 2026-09-01-preview/getComputeUsages.json
     */
    /**
     * Sample code: Quotas_listUsagesForCompute.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void quotasListUsagesForCompute(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.usages()
            .list("subscriptions/00000000-0000-0000-0000-000000000000/providers/Microsoft.Compute/locations/eastus",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-09-01-preview/getNetworkUsages.json
     */
    /**
     * Sample code: Quotas_listUsagesForNetwork.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void quotasListUsagesForNetwork(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.usages()
            .list("subscriptions/00000000-0000-0000-0000-000000000000/providers/Microsoft.Network/locations/eastus",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-09-01-preview/getMachineLearningServicesUsages.json
     */
    /**
     * Sample code: Quotas_listUsagesMachineLearningServices.
     * 
     * @param manager Entry point to QuotaManager.
     */
    public static void quotasListUsagesMachineLearningServices(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.usages()
            .list(
                "subscriptions/00000000-0000-0000-0000-000000000000/providers/Microsoft.MachineLearningServices/locations/eastus",
                com.azure.core.util.Context.NONE);
    }
}
```

