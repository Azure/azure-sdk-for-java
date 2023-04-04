# Code snippets and samples


## CalculateExchange

- [Post](#calculateexchange_post)

## CalculateRefund

- [Post](#calculaterefund_post)

## Exchange

- [Post](#exchange_post)

## Operation

- [List](#operation_list)

## Quota

- [CreateOrUpdate](#quota_createorupdate)
- [Get](#quota_get)
- [List](#quota_list)
- [Update](#quota_update)

## QuotaRequestStatus

- [Get](#quotarequeststatus_get)
- [List](#quotarequeststatus_list)

## Reservation

- [Archive](#reservation_archive)
- [AvailableScopes](#reservation_availablescopes)
- [Get](#reservation_get)
- [List](#reservation_list)
- [ListAll](#reservation_listall)
- [ListRevisions](#reservation_listrevisions)
- [Merge](#reservation_merge)
- [Split](#reservation_split)
- [Unarchive](#reservation_unarchive)
- [Update](#reservation_update)

## ReservationOrder

- [Calculate](#reservationorder_calculate)
- [ChangeDirectory](#reservationorder_changedirectory)
- [Get](#reservationorder_get)
- [List](#reservationorder_list)
- [Purchase](#reservationorder_purchase)

## ResourceProvider

- [GetAppliedReservationList](#resourceprovider_getappliedreservationlist)
- [List](#resourceprovider_list)

## Return

- [Post](#return_post)
### CalculateExchange_Post

```java
import com.azure.resourcemanager.reservations.models.AppliedScopeProperties;
import com.azure.resourcemanager.reservations.models.AppliedScopeType;
import com.azure.resourcemanager.reservations.models.CalculateExchangeRequest;
import com.azure.resourcemanager.reservations.models.CalculateExchangeRequestProperties;
import com.azure.resourcemanager.reservations.models.Commitment;
import com.azure.resourcemanager.reservations.models.CommitmentGrain;
import com.azure.resourcemanager.reservations.models.InstanceFlexibility;
import com.azure.resourcemanager.reservations.models.PurchaseRequest;
import com.azure.resourcemanager.reservations.models.PurchaseRequestPropertiesReservedResourceProperties;
import com.azure.resourcemanager.reservations.models.ReservationBillingPlan;
import com.azure.resourcemanager.reservations.models.ReservationTerm;
import com.azure.resourcemanager.reservations.models.ReservationToReturn;
import com.azure.resourcemanager.reservations.models.ReservedResourceType;
import com.azure.resourcemanager.reservations.models.SavingsPlanPurchaseRequest;
import com.azure.resourcemanager.reservations.models.SavingsPlanTerm;
import com.azure.resourcemanager.reservations.models.SkuName;
import java.util.Arrays;

/** Samples for CalculateExchange Post. */
public final class CalculateExchangePostSamples {
    /*
     * x-ms-original-file: specification/reservations/resource-manager/Microsoft.Capacity/stable/2022-11-01/examples/CalculateExchange.json
     */
    /**
     * Sample code: CalculateExchange.
     *
     * @param manager Entry point to ReservationsManager.
     */
    public static void calculateExchange(com.azure.resourcemanager.reservations.ReservationsManager manager) {
        manager
            .calculateExchanges()
            .post(
                new CalculateExchangeRequest()
                    .withProperties(
                        new CalculateExchangeRequestProperties()
                            .withReservationsToPurchase(
                                Arrays
                                    .asList(
                                        new PurchaseRequest()
                                            .withSku(new SkuName().withName("Standard_B1ls"))
                                            .withLocation("westus")
                                            .withReservedResourceType(ReservedResourceType.VIRTUAL_MACHINES)
                                            .withBillingScopeId("/subscriptions/ed3a1871-612d-abcd-a849-c2542a68be83")
                                            .withTerm(ReservationTerm.P1Y)
                                            .withBillingPlan(ReservationBillingPlan.UPFRONT)
                                            .withQuantity(1)
                                            .withDisplayName("testDisplayName")
                                            .withAppliedScopeType(AppliedScopeType.SHARED)
                                            .withRenew(false)
                                            .withReservedResourceProperties(
                                                new PurchaseRequestPropertiesReservedResourceProperties()
                                                    .withInstanceFlexibility(InstanceFlexibility.ON))))
                            .withSavingsPlansToPurchase(
                                Arrays
                                    .asList(
                                        new SavingsPlanPurchaseRequest()
                                            .withSku(new SkuName().withName("Compute_Savings_Plan"))
                                            .withDisplayName("ComputeSavingsPlan")
                                            .withBillingScopeId("/subscriptions/10000000-0000-0000-0000-000000000000")
                                            .withTerm(SavingsPlanTerm.P1Y)
                                            .withAppliedScopeType(AppliedScopeType.SINGLE)
                                            .withAppliedScopeProperties(
                                                new AppliedScopeProperties()
                                                    .withResourceGroupId(
                                                        "/subscriptions/10000000-0000-0000-0000-000000000000/resourceGroups/testrg"))
                                            .withCommitment(
                                                new Commitment()
                                                    .withCurrencyCode("fakeTokenPlaceholder")
                                                    .withAmount(15.23D)
                                                    .withGrain(CommitmentGrain.HOURLY))))
                            .withReservationsToExchange(
                                Arrays
                                    .asList(
                                        new ReservationToReturn()
                                            .withReservationId(
                                                "/providers/microsoft.capacity/reservationOrders/1f14354c-dc12-4c8d-8090-6f295a3a34aa/reservations/c8c926bd-fc5d-4e29-9d43-b68340ac23a6")
                                            .withQuantity(1)))),
                com.azure.core.util.Context.NONE);
    }
}
```

### CalculateRefund_Post

```java
import com.azure.resourcemanager.reservations.models.CalculateRefundRequest;
import com.azure.resourcemanager.reservations.models.CalculateRefundRequestProperties;
import com.azure.resourcemanager.reservations.models.ReservationToReturn;

/** Samples for CalculateRefund Post. */
public final class CalculateRefundPostSamples {
    /*
     * x-ms-original-file: specification/reservations/resource-manager/Microsoft.Capacity/stable/2022-11-01/examples/CalculateRefund.json
     */
    /**
     * Sample code: CalculateRefund.
     *
     * @param manager Entry point to ReservationsManager.
     */
    public static void calculateRefund(com.azure.resourcemanager.reservations.ReservationsManager manager) {
        manager
            .calculateRefunds()
            .postWithResponse(
                "276e7ae4-84d0-4da6-ab4b-d6b94f3557da",
                new CalculateRefundRequest()
                    .withId("/providers/microsoft.capacity/reservationOrders/50000000-aaaa-bbbb-cccc-100000000004")
                    .withProperties(
                        new CalculateRefundRequestProperties()
                            .withScope("Reservation")
                            .withReservationToReturn(
                                new ReservationToReturn()
                                    .withReservationId(
                                        "/providers/microsoft.capacity/reservationOrders/50000000-aaaa-bbbb-cccc-100000000004/reservations/40000000-aaaa-bbbb-cccc-100000000000")
                                    .withQuantity(1))),
                com.azure.core.util.Context.NONE);
    }
}
```

### Exchange_Post

```java
import com.azure.resourcemanager.reservations.models.ExchangeRequest;
import com.azure.resourcemanager.reservations.models.ExchangeRequestProperties;

/** Samples for Exchange Post. */
public final class ExchangePostSamples {
    /*
     * x-ms-original-file: specification/reservations/resource-manager/Microsoft.Capacity/stable/2022-11-01/examples/Exchange.json
     */
    /**
     * Sample code: Exchange.
     *
     * @param manager Entry point to ReservationsManager.
     */
    public static void exchange(com.azure.resourcemanager.reservations.ReservationsManager manager) {
        manager
            .exchanges()
            .post(
                new ExchangeRequest()
                    .withProperties(
                        new ExchangeRequestProperties().withSessionId("66e2ac8f-439e-4345-8235-6fef07608081")),
                com.azure.core.util.Context.NONE);
    }
}
```

### Operation_List

```java
/** Samples for Operation List. */
public final class OperationListSamples {
    /*
     * x-ms-original-file: specification/reservations/resource-manager/Microsoft.Capacity/stable/2022-11-01/examples/GetOperations.json
     */
    /**
     * Sample code: GetOperations.
     *
     * @param manager Entry point to ReservationsManager.
     */
    public static void getOperations(com.azure.resourcemanager.reservations.ReservationsManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Quota_CreateOrUpdate

```java
import com.azure.resourcemanager.reservations.models.QuotaProperties;
import com.azure.resourcemanager.reservations.models.ResourceName;
import com.azure.resourcemanager.reservations.models.ResourceType;

/** Samples for Quota CreateOrUpdate. */
public final class QuotaCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/reservations/resource-manager/Microsoft.Capacity/stable/2020-10-25/examples/putMachineLearningServicesQuotaRequestDedicated.json
     */
    /**
     * Sample code: Quotas_Request_PutForMachineLearningServices_DedicatedResource.
     *
     * @param manager Entry point to ReservationsManager.
     */
    public static void quotasRequestPutForMachineLearningServicesDedicatedResource(
        com.azure.resourcemanager.reservations.ReservationsManager manager) {
        manager
            .quotas()
            .define("StandardDv2Family")
            .withExistingLocation("D7EC67B3-7657-4966-BFFC-41EFD36BAAB3", "Microsoft.MachineLearningServices", "eastus")
            .withProperties(
                new QuotaProperties()
                    .withLimit(200)
                    .withUnit("Count")
                    .withName(new ResourceName().withValue("StandardDv2Family"))
                    .withResourceType(ResourceType.DEDICATED))
            .create();
    }

    /*
     * x-ms-original-file: specification/reservations/resource-manager/Microsoft.Capacity/stable/2020-10-25/examples/putMachineLearningServicesQuotaRequestLowPriority.json
     */
    /**
     * Sample code: Quotas_Request_PutForMachineLearningServices_LowPriorityResource.
     *
     * @param manager Entry point to ReservationsManager.
     */
    public static void quotasRequestPutForMachineLearningServicesLowPriorityResource(
        com.azure.resourcemanager.reservations.ReservationsManager manager) {
        manager
            .quotas()
            .define("TotalLowPriorityCores")
            .withExistingLocation("D7EC67B3-7657-4966-BFFC-41EFD36BAAB3", "Microsoft.MachineLearningServices", "eastus")
            .withProperties(
                new QuotaProperties()
                    .withLimit(200)
                    .withUnit("Count")
                    .withName(new ResourceName().withValue("TotalLowPriorityCores"))
                    .withResourceType(ResourceType.LOW_PRIORITY))
            .create();
    }

    /*
     * x-ms-original-file: specification/reservations/resource-manager/Microsoft.Capacity/stable/2020-10-25/examples/putComputeOneSkuQuotaRequest.json
     */
    /**
     * Sample code: Quotas_Request_PutForCompute.
     *
     * @param manager Entry point to ReservationsManager.
     */
    public static void quotasRequestPutForCompute(com.azure.resourcemanager.reservations.ReservationsManager manager) {
        manager
            .quotas()
            .define("standardFSv2Family")
            .withExistingLocation("D7EC67B3-7657-4966-BFFC-41EFD36BAAB3", "Microsoft.Compute", "eastus")
            .withProperties(
                new QuotaProperties()
                    .withLimit(200)
                    .withUnit("Count")
                    .withName(new ResourceName().withValue("standardFSv2Family")))
            .create();
    }
}
```

### Quota_Get

```java
/** Samples for Quota Get. */
public final class QuotaGetSamples {
    /*
     * x-ms-original-file: specification/reservations/resource-manager/Microsoft.Capacity/stable/2020-10-25/examples/getComputeOneSkuUsages.json
     */
    /**
     * Sample code: Quotas_Request_ForCompute.
     *
     * @param manager Entry point to ReservationsManager.
     */
    public static void quotasRequestForCompute(com.azure.resourcemanager.reservations.ReservationsManager manager) {
        manager
            .quotas()
            .getWithResponse(
                "00000000-0000-0000-0000-000000000000",
                "Microsoft.Compute",
                "eastus",
                "standardNDSFamily",
                com.azure.core.util.Context.NONE);
    }
}
```

### Quota_List

```java
/** Samples for Quota List. */
public final class QuotaListSamples {
    /*
     * x-ms-original-file: specification/reservations/resource-manager/Microsoft.Capacity/stable/2020-10-25/examples/getComputeUsages.json
     */
    /**
     * Sample code: Quotas_listUsagesForCompute.
     *
     * @param manager Entry point to ReservationsManager.
     */
    public static void quotasListUsagesForCompute(com.azure.resourcemanager.reservations.ReservationsManager manager) {
        manager
            .quotas()
            .list(
                "00000000-0000-0000-0000-000000000000",
                "Microsoft.Compute",
                "eastus",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/reservations/resource-manager/Microsoft.Capacity/stable/2020-10-25/examples/getMachineLearningServicesUsages.json
     */
    /**
     * Sample code: Quotas_listUsagesMachineLearningServices.
     *
     * @param manager Entry point to ReservationsManager.
     */
    public static void quotasListUsagesMachineLearningServices(
        com.azure.resourcemanager.reservations.ReservationsManager manager) {
        manager
            .quotas()
            .list(
                "00000000-0000-0000-0000-000000000000",
                "Microsoft.MachineLearningServices",
                "eastus",
                com.azure.core.util.Context.NONE);
    }
}
```

### Quota_Update

```java
import com.azure.resourcemanager.reservations.models.CurrentQuotaLimitBase;
import com.azure.resourcemanager.reservations.models.QuotaProperties;
import com.azure.resourcemanager.reservations.models.ResourceName;

/** Samples for Quota Update. */
public final class QuotaUpdateSamples {
    /*
     * x-ms-original-file: specification/reservations/resource-manager/Microsoft.Capacity/stable/2020-10-25/examples/patchComputeQuotaRequest.json
     */
    /**
     * Sample code: Quotas_Request_PatchForCompute.
     *
     * @param manager Entry point to ReservationsManager.
     */
    public static void quotasRequestPatchForCompute(
        com.azure.resourcemanager.reservations.ReservationsManager manager) {
        CurrentQuotaLimitBase resource =
            manager
                .quotas()
                .getWithResponse(
                    "D7EC67B3-7657-4966-BFFC-41EFD36BAAB3",
                    "Microsoft.Compute",
                    "eastus",
                    "standardFSv2Family",
                    com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withProperties(
                new QuotaProperties()
                    .withLimit(200)
                    .withUnit("Count")
                    .withName(new ResourceName().withValue("standardFSv2Family")))
            .apply();
    }
}
```

### QuotaRequestStatus_Get

```java
/** Samples for QuotaRequestStatus Get. */
public final class QuotaRequestStatusGetSamples {
    /*
     * x-ms-original-file: specification/reservations/resource-manager/Microsoft.Capacity/stable/2020-10-25/examples/getQuotaRequestStatusFailed.json
     */
    /**
     * Sample code: QuotaRequestFailed.
     *
     * @param manager Entry point to ReservationsManager.
     */
    public static void quotaRequestFailed(com.azure.resourcemanager.reservations.ReservationsManager manager) {
        manager
            .quotaRequestStatus()
            .getWithResponse(
                "00000000-0000-0000-0000-000000000000",
                "Microsoft.Compute",
                "eastus",
                "2B5C8515-37D8-4B6A-879B-CD641A2CF605",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/reservations/resource-manager/Microsoft.Capacity/stable/2020-10-25/examples/getQuotaRequestStatusById.json
     */
    /**
     * Sample code: QuotaRequestStatus.
     *
     * @param manager Entry point to ReservationsManager.
     */
    public static void quotaRequestStatus(com.azure.resourcemanager.reservations.ReservationsManager manager) {
        manager
            .quotaRequestStatus()
            .getWithResponse(
                "00000000-0000-0000-0000-000000000000",
                "Microsoft.Compute",
                "eastus",
                "2B5C8515-37D8-4B6A-879B-CD641A2CF605",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/reservations/resource-manager/Microsoft.Capacity/stable/2020-10-25/examples/getQuotaRequestStatusInProgress.json
     */
    /**
     * Sample code: QuotaRequestInProgress.
     *
     * @param manager Entry point to ReservationsManager.
     */
    public static void quotaRequestInProgress(com.azure.resourcemanager.reservations.ReservationsManager manager) {
        manager
            .quotaRequestStatus()
            .getWithResponse(
                "00000000-0000-0000-0000-000000000000",
                "Microsoft.Compute",
                "eastus",
                "2B5C8515-37D8-4B6A-879B-CD641A2CF605",
                com.azure.core.util.Context.NONE);
    }
}
```

### QuotaRequestStatus_List

```java
/** Samples for QuotaRequestStatus List. */
public final class QuotaRequestStatusListSamples {
    /*
     * x-ms-original-file: specification/reservations/resource-manager/Microsoft.Capacity/stable/2020-10-25/examples/getQuotaRequestsHistory.json
     */
    /**
     * Sample code: QuotaRequestHistory.
     *
     * @param manager Entry point to ReservationsManager.
     */
    public static void quotaRequestHistory(com.azure.resourcemanager.reservations.ReservationsManager manager) {
        manager
            .quotaRequestStatus()
            .list(
                "3f75fdf7-977e-44ad-990d-99f14f0f299f",
                "Microsoft.Compute",
                "eastus",
                null,
                null,
                null,
                com.azure.core.util.Context.NONE);
    }
}
```

### Reservation_Archive

```java
/** Samples for Reservation Archive. */
public final class ReservationArchiveSamples {
    /*
     * x-ms-original-file: specification/reservations/resource-manager/Microsoft.Capacity/stable/2022-11-01/examples/Archive.json
     */
    /**
     * Sample code: Archive.
     *
     * @param manager Entry point to ReservationsManager.
     */
    public static void archive(com.azure.resourcemanager.reservations.ReservationsManager manager) {
        manager
            .reservations()
            .archiveWithResponse(
                "276e7ae4-84d0-4da6-ab4b-d6b94f3557da",
                "356e7ae4-84d0-4da6-ab4b-d6b94f3557da",
                com.azure.core.util.Context.NONE);
    }
}
```

### Reservation_AvailableScopes

```java
import com.azure.resourcemanager.reservations.models.AvailableScopeRequest;
import com.azure.resourcemanager.reservations.models.AvailableScopeRequestProperties;
import java.util.Arrays;

/** Samples for Reservation AvailableScopes. */
public final class ReservationAvailableScopesSamples {
    /*
     * x-ms-original-file: specification/reservations/resource-manager/Microsoft.Capacity/stable/2022-11-01/examples/GetAvailableScope.json
     */
    /**
     * Sample code: AvailableScopes.
     *
     * @param manager Entry point to ReservationsManager.
     */
    public static void availableScopes(com.azure.resourcemanager.reservations.ReservationsManager manager) {
        manager
            .reservations()
            .availableScopes(
                "276e7ae4-84d0-4da6-ab4b-d6b94f3557da",
                "356e7ae4-84d0-4da6-ab4b-d6b94f3557da",
                new AvailableScopeRequest()
                    .withProperties(
                        new AvailableScopeRequestProperties()
                            .withScopes(Arrays.asList("/subscriptions/efc7c997-7700-4a74-b731-55aec16c15e9"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### Reservation_Get

```java
/** Samples for Reservation Get. */
public final class ReservationGetSamples {
    /*
     * x-ms-original-file: specification/reservations/resource-manager/Microsoft.Capacity/stable/2022-11-01/examples/GetReservationDetails.json
     */
    /**
     * Sample code: GetReservation.
     *
     * @param manager Entry point to ReservationsManager.
     */
    public static void getReservation(com.azure.resourcemanager.reservations.ReservationsManager manager) {
        manager
            .reservations()
            .getWithResponse(
                "276e7ae4-84d0-4da6-ab4b-d6b94f3557da",
                "6ef59113-3482-40da-8d79-787f823e34bc",
                "renewProperties",
                com.azure.core.util.Context.NONE);
    }
}
```

### Reservation_List

```java
/** Samples for Reservation List. */
public final class ReservationListSamples {
    /*
     * x-ms-original-file: specification/reservations/resource-manager/Microsoft.Capacity/stable/2022-11-01/examples/GetReservationsFromOrder.json
     */
    /**
     * Sample code: ReservationList.
     *
     * @param manager Entry point to ReservationsManager.
     */
    public static void reservationList(com.azure.resourcemanager.reservations.ReservationsManager manager) {
        manager.reservations().list("276e7ae4-84d0-4da6-ab4b-d6b94f3557da", com.azure.core.util.Context.NONE);
    }
}
```

### Reservation_ListAll

```java
/** Samples for Reservation ListAll. */
public final class ReservationListAllSamples {
    /*
     * x-ms-original-file: specification/reservations/resource-manager/Microsoft.Capacity/stable/2022-11-01/examples/GetReservations.json
     */
    /**
     * Sample code: ReservationListAll.
     *
     * @param manager Entry point to ReservationsManager.
     */
    public static void reservationListAll(com.azure.resourcemanager.reservations.ReservationsManager manager) {
        manager
            .reservations()
            .listAll(
                "(properties/archived eq false)",
                "properties/displayName asc",
                "true",
                50.0F,
                null,
                1.0F,
                com.azure.core.util.Context.NONE);
    }
}
```

### Reservation_ListRevisions

```java
/** Samples for Reservation ListRevisions. */
public final class ReservationListRevisionsSamples {
    /*
     * x-ms-original-file: specification/reservations/resource-manager/Microsoft.Capacity/stable/2022-11-01/examples/GetReservationRevisions.json
     */
    /**
     * Sample code: ReservationRevisions.
     *
     * @param manager Entry point to ReservationsManager.
     */
    public static void reservationRevisions(com.azure.resourcemanager.reservations.ReservationsManager manager) {
        manager
            .reservations()
            .listRevisions(
                "276e7ae4-84d0-4da6-ab4b-d6b94f3557da",
                "6ef59113-3482-40da-8d79-787f823e34bc",
                com.azure.core.util.Context.NONE);
    }
}
```

### Reservation_Merge

```java
import com.azure.resourcemanager.reservations.models.MergeRequest;
import java.util.Arrays;

/** Samples for Reservation Merge. */
public final class ReservationMergeSamples {
    /*
     * x-ms-original-file: specification/reservations/resource-manager/Microsoft.Capacity/stable/2022-11-01/examples/MergeReservations.json
     */
    /**
     * Sample code: Merge.
     *
     * @param manager Entry point to ReservationsManager.
     */
    public static void merge(com.azure.resourcemanager.reservations.ReservationsManager manager) {
        manager
            .reservations()
            .merge(
                "276e7ae4-84d0-4da6-ab4b-d6b94f3557da",
                new MergeRequest()
                    .withSources(
                        Arrays
                            .asList(
                                "/providers/Microsoft.Capacity/reservationOrders/c0565a8a-4491-4e77-b07b-5e6d66718e1c/reservations/cea04232-932e-47db-acb5-e29a945ecc73",
                                "/providers/Microsoft.Capacity/reservationOrders/c0565a8a-4491-4e77-b07b-5e6d66718e1c/reservations/5bf54dc7-dacd-4f46-a16b-7b78f4a59799")),
                com.azure.core.util.Context.NONE);
    }
}
```

### Reservation_Split

```java
import com.azure.resourcemanager.reservations.models.SplitRequest;
import java.util.Arrays;

/** Samples for Reservation Split. */
public final class ReservationSplitSamples {
    /*
     * x-ms-original-file: specification/reservations/resource-manager/Microsoft.Capacity/stable/2022-11-01/examples/SplitReservation.json
     */
    /**
     * Sample code: Split.
     *
     * @param manager Entry point to ReservationsManager.
     */
    public static void split(com.azure.resourcemanager.reservations.ReservationsManager manager) {
        manager
            .reservations()
            .split(
                "276e7ae4-84d0-4da6-ab4b-d6b94f3557da",
                new SplitRequest()
                    .withQuantities(Arrays.asList(1, 2))
                    .withReservationId(
                        "/providers/Microsoft.Capacity/reservationOrders/276e7ae4-84d0-4da6-ab4b-d6b94f3557da/reservations/bcae77cd-3119-4766-919f-b50d36c75c7a"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Reservation_Unarchive

```java
/** Samples for Reservation Unarchive. */
public final class ReservationUnarchiveSamples {
    /*
     * x-ms-original-file: specification/reservations/resource-manager/Microsoft.Capacity/stable/2022-11-01/examples/Unarchive.json
     */
    /**
     * Sample code: Unarchive.
     *
     * @param manager Entry point to ReservationsManager.
     */
    public static void unarchive(com.azure.resourcemanager.reservations.ReservationsManager manager) {
        manager
            .reservations()
            .unarchiveWithResponse(
                "276e7ae4-84d0-4da6-ab4b-d6b94f3557da",
                "356e7ae4-84d0-4da6-ab4b-d6b94f3557da",
                com.azure.core.util.Context.NONE);
    }
}
```

### Reservation_Update

```java
import com.azure.resourcemanager.reservations.models.AppliedScopeType;
import com.azure.resourcemanager.reservations.models.InstanceFlexibility;
import com.azure.resourcemanager.reservations.models.PatchModel;

/** Samples for Reservation Update. */
public final class ReservationUpdateSamples {
    /*
     * x-ms-original-file: specification/reservations/resource-manager/Microsoft.Capacity/stable/2022-11-01/examples/UpdateReservation.json
     */
    /**
     * Sample code: PatchReservation.
     *
     * @param manager Entry point to ReservationsManager.
     */
    public static void patchReservation(com.azure.resourcemanager.reservations.ReservationsManager manager) {
        manager
            .reservations()
            .update(
                "276e7ae4-84d0-4da6-ab4b-d6b94f3557da",
                "6ef59113-3482-40da-8d79-787f823e34bc",
                new PatchModel()
                    .withAppliedScopeType(AppliedScopeType.SHARED)
                    .withInstanceFlexibility(InstanceFlexibility.OFF),
                com.azure.core.util.Context.NONE);
    }
}
```

### ReservationOrder_Calculate

```java
import com.azure.resourcemanager.reservations.models.AppliedScopeType;
import com.azure.resourcemanager.reservations.models.InstanceFlexibility;
import com.azure.resourcemanager.reservations.models.PurchaseRequest;
import com.azure.resourcemanager.reservations.models.PurchaseRequestPropertiesReservedResourceProperties;
import com.azure.resourcemanager.reservations.models.ReservationBillingPlan;
import com.azure.resourcemanager.reservations.models.ReservationTerm;
import com.azure.resourcemanager.reservations.models.ReservedResourceType;
import com.azure.resourcemanager.reservations.models.SkuName;

/** Samples for ReservationOrder Calculate. */
public final class ReservationOrderCalculateSamples {
    /*
     * x-ms-original-file: specification/reservations/resource-manager/Microsoft.Capacity/stable/2022-11-01/examples/CalculateReservationOrder.json
     */
    /**
     * Sample code: CalculatePrice.
     *
     * @param manager Entry point to ReservationsManager.
     */
    public static void calculatePrice(com.azure.resourcemanager.reservations.ReservationsManager manager) {
        manager
            .reservationOrders()
            .calculateWithResponse(
                new PurchaseRequest()
                    .withSku(new SkuName().withName("standard_D1"))
                    .withLocation("westus")
                    .withReservedResourceType(ReservedResourceType.VIRTUAL_MACHINES)
                    .withBillingScopeId("/subscriptions/ed3a1871-612d-abcd-a849-c2542a68be83")
                    .withTerm(ReservationTerm.P1Y)
                    .withBillingPlan(ReservationBillingPlan.MONTHLY)
                    .withQuantity(1)
                    .withDisplayName("TestReservationOrder")
                    .withAppliedScopeType(AppliedScopeType.SHARED)
                    .withReservedResourceProperties(
                        new PurchaseRequestPropertiesReservedResourceProperties()
                            .withInstanceFlexibility(InstanceFlexibility.ON)),
                com.azure.core.util.Context.NONE);
    }
}
```

### ReservationOrder_ChangeDirectory

```java
import com.azure.resourcemanager.reservations.models.ChangeDirectoryRequest;

/** Samples for ReservationOrder ChangeDirectory. */
public final class ReservationOrderChangeDirectorySamples {
    /*
     * x-ms-original-file: specification/reservations/resource-manager/Microsoft.Capacity/stable/2022-11-01/examples/ChangeDirectoryReservationOrder.json
     */
    /**
     * Sample code: ChangeDirectory.
     *
     * @param manager Entry point to ReservationsManager.
     */
    public static void changeDirectory(com.azure.resourcemanager.reservations.ReservationsManager manager) {
        manager
            .reservationOrders()
            .changeDirectoryWithResponse(
                "a075419f-44cc-497f-b68a-14ee811d48b9",
                new ChangeDirectoryRequest().withDestinationTenantId("906655ea-30be-4587-9d12-b50e077b0f32"),
                com.azure.core.util.Context.NONE);
    }
}
```

### ReservationOrder_Get

```java
/** Samples for ReservationOrder Get. */
public final class ReservationOrderGetSamples {
    /*
     * x-ms-original-file: specification/reservations/resource-manager/Microsoft.Capacity/stable/2022-11-01/examples/GetReservationOrderDetailsWithExpandPlanInformation.json
     */
    /**
     * Sample code: GetReservationWithExpandPayments.
     *
     * @param manager Entry point to ReservationsManager.
     */
    public static void getReservationWithExpandPayments(
        com.azure.resourcemanager.reservations.ReservationsManager manager) {
        manager
            .reservationOrders()
            .getWithResponse("a075419f-44cc-497f-b68a-14ee811d48b9", "schedule", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/reservations/resource-manager/Microsoft.Capacity/stable/2022-11-01/examples/GetReservationOrderDetails.json
     */
    /**
     * Sample code: GetReservationOrder.
     *
     * @param manager Entry point to ReservationsManager.
     */
    public static void getReservationOrder(com.azure.resourcemanager.reservations.ReservationsManager manager) {
        manager
            .reservationOrders()
            .getWithResponse("a075419f-44cc-497f-b68a-14ee811d48b9", null, com.azure.core.util.Context.NONE);
    }
}
```

### ReservationOrder_List

```java
/** Samples for ReservationOrder List. */
public final class ReservationOrderListSamples {
    /*
     * x-ms-original-file: specification/reservations/resource-manager/Microsoft.Capacity/stable/2022-11-01/examples/GetReservationOrders.json
     */
    /**
     * Sample code: ReservationOrderList.
     *
     * @param manager Entry point to ReservationsManager.
     */
    public static void reservationOrderList(com.azure.resourcemanager.reservations.ReservationsManager manager) {
        manager.reservationOrders().list(com.azure.core.util.Context.NONE);
    }
}
```

### ReservationOrder_Purchase

```java
import com.azure.resourcemanager.reservations.models.AppliedScopeType;
import com.azure.resourcemanager.reservations.models.InstanceFlexibility;
import com.azure.resourcemanager.reservations.models.PurchaseRequest;
import com.azure.resourcemanager.reservations.models.PurchaseRequestPropertiesReservedResourceProperties;
import com.azure.resourcemanager.reservations.models.ReservationBillingPlan;
import com.azure.resourcemanager.reservations.models.ReservationTerm;
import com.azure.resourcemanager.reservations.models.ReservedResourceType;
import com.azure.resourcemanager.reservations.models.SkuName;

/** Samples for ReservationOrder Purchase. */
public final class ReservationOrderPurchaseSamples {
    /*
     * x-ms-original-file: specification/reservations/resource-manager/Microsoft.Capacity/stable/2022-11-01/examples/PurchaseReservationOrder.json
     */
    /**
     * Sample code: Purchase.
     *
     * @param manager Entry point to ReservationsManager.
     */
    public static void purchase(com.azure.resourcemanager.reservations.ReservationsManager manager) {
        manager
            .reservationOrders()
            .purchase(
                "a075419f-44cc-497f-b68a-14ee811d48b9",
                new PurchaseRequest()
                    .withSku(new SkuName().withName("standard_D1"))
                    .withLocation("westus")
                    .withReservedResourceType(ReservedResourceType.VIRTUAL_MACHINES)
                    .withBillingScopeId("/subscriptions/ed3a1871-612d-abcd-a849-c2542a68be83")
                    .withTerm(ReservationTerm.P1Y)
                    .withBillingPlan(ReservationBillingPlan.MONTHLY)
                    .withQuantity(1)
                    .withDisplayName("TestReservationOrder")
                    .withAppliedScopeType(AppliedScopeType.SHARED)
                    .withRenew(false)
                    .withReservedResourceProperties(
                        new PurchaseRequestPropertiesReservedResourceProperties()
                            .withInstanceFlexibility(InstanceFlexibility.ON)),
                com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_GetAppliedReservationList

```java
/** Samples for ResourceProvider GetAppliedReservationList. */
public final class ResourceProviderGetAppliedReservationListSamples {
    /*
     * x-ms-original-file: specification/reservations/resource-manager/Microsoft.Capacity/stable/2022-11-01/examples/GetAppliedReservations.json
     */
    /**
     * Sample code: AppliedReservationList.
     *
     * @param manager Entry point to ReservationsManager.
     */
    public static void appliedReservationList(com.azure.resourcemanager.reservations.ReservationsManager manager) {
        manager
            .resourceProviders()
            .getAppliedReservationListWithResponse(
                "23bc208b-083f-4901-ae85-4f98c0c3b4b6", com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_List

```java
/** Samples for ResourceProvider List. */
public final class ResourceProviderListSamples {
    /*
     * x-ms-original-file: specification/reservations/resource-manager/Microsoft.Capacity/stable/2022-11-01/examples/GetCatalog.json
     */
    /**
     * Sample code: Catalog.
     *
     * @param manager Entry point to ReservationsManager.
     */
    public static void catalog(com.azure.resourcemanager.reservations.ReservationsManager manager) {
        manager
            .resourceProviders()
            .list(
                "23bc208b-083f-4901-ae85-4f98c0c3b4b6",
                "VirtualMachines",
                "eastus",
                null,
                null,
                null,
                null,
                null,
                null,
                com.azure.core.util.Context.NONE);
    }
}
```

### Return_Post

```java
import com.azure.resourcemanager.reservations.models.RefundRequest;
import com.azure.resourcemanager.reservations.models.RefundRequestProperties;
import com.azure.resourcemanager.reservations.models.ReservationToReturn;

/** Samples for Return Post. */
public final class ReturnPostSamples {
    /*
     * x-ms-original-file: specification/reservations/resource-manager/Microsoft.Capacity/stable/2022-11-01/examples/Return.json
     */
    /**
     * Sample code: Return a reservation.
     *
     * @param manager Entry point to ReservationsManager.
     */
    public static void returnAReservation(com.azure.resourcemanager.reservations.ReservationsManager manager) {
        manager
            .returns()
            .post(
                "50000000-aaaa-bbbb-cccc-100000000004",
                new RefundRequest()
                    .withProperties(
                        new RefundRequestProperties()
                            .withSessionId("10000000-aaaa-bbbb-cccc-200000000000")
                            .withScope("Reservation")
                            .withReservationToReturn(
                                new ReservationToReturn()
                                    .withReservationId(
                                        "/providers/microsoft.capacity/reservationOrders/50000000-aaaa-bbbb-cccc-100000000004/reservations/40000000-aaaa-bbbb-cccc-100000000000")
                                    .withQuantity(1))
                            .withReturnReason("PurchasedWrongProduct")),
                com.azure.core.util.Context.NONE);
    }
}
```

