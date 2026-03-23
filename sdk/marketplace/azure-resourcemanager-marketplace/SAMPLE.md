# Code snippets and samples


## Operations

- [List](#operations_list)

## PrivateStore

- [AcknowledgeOfferNotification](#privatestore_acknowledgeoffernotification)
- [AdminRequestApprovalsList](#privatestore_adminrequestapprovalslist)
- [AnyExistingOffersInTheCollections](#privatestore_anyexistingoffersinthecollections)
- [BillingAccounts](#privatestore_billingaccounts)
- [BulkCollectionsAction](#privatestore_bulkcollectionsaction)
- [CollectionsToSubscriptionsMapping](#privatestore_collectionstosubscriptionsmapping)
- [CreateApprovalRequest](#privatestore_createapprovalrequest)
- [CreateOrUpdate](#privatestore_createorupdate)
- [Delete](#privatestore_delete)
- [FetchAllSubscriptionsInTenant](#privatestore_fetchallsubscriptionsintenant)
- [Get](#privatestore_get)
- [GetAdminRequestApproval](#privatestore_getadminrequestapproval)
- [GetApprovalRequestsList](#privatestore_getapprovalrequestslist)
- [GetRequestApproval](#privatestore_getrequestapproval)
- [List](#privatestore_list)
- [ListNewPlansNotifications](#privatestore_listnewplansnotifications)
- [ListStopSellOffersPlansNotifications](#privatestore_liststopselloffersplansnotifications)
- [ListSubscriptionsContext](#privatestore_listsubscriptionscontext)
- [QueryApprovedPlans](#privatestore_queryapprovedplans)
- [QueryNotificationsState](#privatestore_querynotificationsstate)
- [QueryOffers](#privatestore_queryoffers)
- [QueryRequestApproval](#privatestore_queryrequestapproval)
- [QueryUserOffers](#privatestore_queryuseroffers)
- [UpdateAdminRequestApproval](#privatestore_updateadminrequestapproval)
- [WithdrawPlan](#privatestore_withdrawplan)

## PrivateStoreCollection

- [ApproveAllItems](#privatestorecollection_approveallitems)
- [CreateOrUpdate](#privatestorecollection_createorupdate)
- [Delete](#privatestorecollection_delete)
- [DisableApproveAllItems](#privatestorecollection_disableapproveallitems)
- [Get](#privatestorecollection_get)
- [List](#privatestorecollection_list)
- [TransferOffers](#privatestorecollection_transferoffers)

## PrivateStoreCollectionOffer

- [ContextsView](#privatestorecollectionoffer_contextsview)
- [CreateOrUpdate](#privatestorecollectionoffer_createorupdate)
- [Delete](#privatestorecollectionoffer_delete)
- [Get](#privatestorecollectionoffer_get)
- [List](#privatestorecollectionoffer_list)
- [ListByContexts](#privatestorecollectionoffer_listbycontexts)
- [UpsertOfferWithMultiContext](#privatestorecollectionoffer_upsertofferwithmulticontext)

## ResourceProvider

- [QueryRules](#resourceprovider_queryrules)
- [QueryUserRules](#resourceprovider_queryuserrules)
- [SetCollectionRules](#resourceprovider_setcollectionrules)
### Operations_List

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2025-01-01/GetOperations.json
     */
    /**
     * Sample code: GetOperations.
     * 
     * @param manager Entry point to MarketplaceManager.
     */
    public static void getOperations(com.azure.resourcemanager.marketplace.MarketplaceManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### PrivateStore_AcknowledgeOfferNotification

```java
import com.azure.resourcemanager.marketplace.models.AcknowledgeOfferNotificationProperties;
import java.util.Arrays;

/**
 * Samples for PrivateStore AcknowledgeOfferNotification.
 */
public final class PrivateStoreAcknowledgeOfferNotificationSamples {
    /*
     * x-ms-original-file: 2025-01-01/AcknowledgeNotification.json
     */
    /**
     * Sample code: AcknowledgeNotification.
     * 
     * @param manager Entry point to MarketplaceManager.
     */
    public static void acknowledgeNotification(com.azure.resourcemanager.marketplace.MarketplaceManager manager) {
        manager.privateStores()
            .acknowledgeOfferNotificationWithResponse("a0e28e55-90c4-41d8-8e34-bb7ef7775406",
                "marketplacetestthirdparty.md-test-third-party-2",
                new AcknowledgeOfferNotificationProperties().withAcknowledge(false)
                    .withDismiss(false)
                    .withRemoveOffer(false)
                    .withRemovePlans(Arrays.asList("testPlanA")),
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateStore_AdminRequestApprovalsList

```java
/**
 * Samples for PrivateStore AdminRequestApprovalsList.
 */
public final class PrivateStoreAdminRequestApprovalsListSamples {
    /*
     * x-ms-original-file: 2025-01-01/AdminRequestApprovalsList.json
     */
    /**
     * Sample code: AdminRequestApprovalsList.
     * 
     * @param manager Entry point to MarketplaceManager.
     */
    public static void adminRequestApprovalsList(com.azure.resourcemanager.marketplace.MarketplaceManager manager) {
        manager.privateStores()
            .adminRequestApprovalsListWithResponse("a0e28e55-90c4-41d8-8e34-bb7ef7775406",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateStore_AnyExistingOffersInTheCollections

```java
/**
 * Samples for PrivateStore AnyExistingOffersInTheCollections.
 */
public final class PrivateStoreAnyExistingOffersInTheCollectionsSamples {
    /*
     * x-ms-original-file: 2025-01-01/AnyExistingOffersInTheCollections.json
     */
    /**
     * Sample code: AnyExistingOffersInTheCollections.
     * 
     * @param manager Entry point to MarketplaceManager.
     */
    public static void
        anyExistingOffersInTheCollections(com.azure.resourcemanager.marketplace.MarketplaceManager manager) {
        manager.privateStores()
            .anyExistingOffersInTheCollectionsWithResponse("a0e28e55-90c4-41d8-8e34-bb7ef7775406",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateStore_BillingAccounts

```java
/**
 * Samples for PrivateStore BillingAccounts.
 */
public final class PrivateStoreBillingAccountsSamples {
    /*
     * x-ms-original-file: 2025-01-01/BillingAccounts.json
     */
    /**
     * Sample code: BillingAccounts.
     * 
     * @param manager Entry point to MarketplaceManager.
     */
    public static void billingAccounts(com.azure.resourcemanager.marketplace.MarketplaceManager manager) {
        manager.privateStores()
            .billingAccountsWithResponse("a0e28e55-90c4-41d8-8e34-bb7ef7775406", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateStore_BulkCollectionsAction

```java
import com.azure.resourcemanager.marketplace.models.BulkCollectionsPayload;
import java.util.Arrays;

/**
 * Samples for PrivateStore BulkCollectionsAction.
 */
public final class PrivateStoreBulkCollectionsActionSamples {
    /*
     * x-ms-original-file: 2025-01-01/BulkCollectionsAction.json
     */
    /**
     * Sample code: BulkCollectionsAction.
     * 
     * @param manager Entry point to MarketplaceManager.
     */
    public static void bulkCollectionsAction(com.azure.resourcemanager.marketplace.MarketplaceManager manager) {
        manager.privateStores()
            .bulkCollectionsActionWithResponse("a0e28e55-90c4-41d8-8e34-bb7ef7775406",
                new BulkCollectionsPayload()
                    .withCollectionIds(
                        Arrays.asList("c752f021-1c37-4af5-b82f-74c51c27b44a", "f47ef1c7-e908-4f39-ae29-db181634ad8d"))
                    .withAction("EnableCollections"),
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateStore_CollectionsToSubscriptionsMapping

```java
import com.azure.resourcemanager.marketplace.models.CollectionsToSubscriptionsMappingPayload;
import com.azure.resourcemanager.marketplace.models.CollectionsToSubscriptionsMappingProperties;
import java.util.Arrays;

/**
 * Samples for PrivateStore CollectionsToSubscriptionsMapping.
 */
public final class PrivateStoreCollectionsToSubscriptionsMappingSamples {
    /*
     * x-ms-original-file: 2025-01-01/CollectionsToSubscriptionsMapping.json
     */
    /**
     * Sample code: CollectionsToSubscriptionsMapping.
     * 
     * @param manager Entry point to MarketplaceManager.
     */
    public static void
        collectionsToSubscriptionsMapping(com.azure.resourcemanager.marketplace.MarketplaceManager manager) {
        manager.privateStores()
            .collectionsToSubscriptionsMappingWithResponse("a0e28e55-90c4-41d8-8e34-bb7ef7775406",
                new CollectionsToSubscriptionsMappingPayload()
                    .withProperties(new CollectionsToSubscriptionsMappingProperties().withSubscriptionIds(
                        Arrays.asList("b340914e-353d-453a-85fb-8f9b65b51f91", "f2baa04d-5bfc-461b-b6d8-61b403c9ec48"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateStore_CreateApprovalRequest

```java
import com.azure.resourcemanager.marketplace.fluent.models.RequestApprovalResourceInner;
import com.azure.resourcemanager.marketplace.models.PlanDetails;
import java.util.Arrays;

/**
 * Samples for PrivateStore CreateApprovalRequest.
 */
public final class PrivateStoreCreateApprovalRequestSamples {
    /*
     * x-ms-original-file: 2025-01-01/CreateApprovalRequest.json
     */
    /**
     * Sample code: CreateApprovalRequest.
     * 
     * @param manager Entry point to MarketplaceManager.
     */
    public static void createApprovalRequest(com.azure.resourcemanager.marketplace.MarketplaceManager manager) {
        manager.privateStores()
            .createApprovalRequestWithResponse("a0e28e55-90c4-41d8-8e34-bb7ef7775406",
                "marketplacetestthirdparty.md-test-third-party-2",
                new RequestApprovalResourceInner().withPublisherId("marketplacetestthirdparty")
                    .withPlansDetails(Arrays.asList(
                        new PlanDetails().withPlanId("testPlanA")
                            .withJustification("Because I want to....")
                            .withSubscriptionId("4ca4753c-5a1e-4913-b849-2c68880e03c2")
                            .withSubscriptionName("Test subscription 2"),
                        new PlanDetails().withPlanId("*")
                            .withJustification("try me :)")
                            .withSubscriptionId("4ca4753c-5a1e-4913-b849-2c68880e03c2")
                            .withSubscriptionName("Test subscription 2"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateStore_CreateOrUpdate

```java
import com.azure.resourcemanager.marketplace.fluent.models.PrivateStoreInner;
import com.azure.resourcemanager.marketplace.models.Availability;

/**
 * Samples for PrivateStore CreateOrUpdate.
 */
public final class PrivateStoreCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-01-01/PrivateStores_update.json
     */
    /**
     * Sample code: PrivateStores_update.
     * 
     * @param manager Entry point to MarketplaceManager.
     */
    public static void privateStoresUpdate(com.azure.resourcemanager.marketplace.MarketplaceManager manager) {
        manager.privateStores()
            .createOrUpdateWithResponse("a0e28e55-90c4-41d8-8e34-bb7ef7775406",
                new PrivateStoreInner().withAvailability(Availability.DISABLED)
                    .withETag("\"9301f4fd-0000-0100-0000-5e248b350345\""),
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateStore_Delete

```java
/**
 * Samples for PrivateStore Delete.
 */
public final class PrivateStoreDeleteSamples {
    /*
     * x-ms-original-file: 2025-01-01/DeletePrivateStore.json
     */
    /**
     * Sample code: DeletePrivateStores.
     * 
     * @param manager Entry point to MarketplaceManager.
     */
    public static void deletePrivateStores(com.azure.resourcemanager.marketplace.MarketplaceManager manager) {
        manager.privateStores()
            .deleteWithResponse("a0e28e55-90c4-41d8-8e34-bb7ef7775406", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateStore_FetchAllSubscriptionsInTenant

```java
/**
 * Samples for PrivateStore FetchAllSubscriptionsInTenant.
 */
public final class PrivateStoreFetchAllSubscriptionsInTenantSamples {
    /*
     * x-ms-original-file: 2025-01-01/FetchAllSubscriptionsInTenant.json
     */
    /**
     * Sample code: FetchAllSubscriptionsInTenant.
     * 
     * @param manager Entry point to MarketplaceManager.
     */
    public static void fetchAllSubscriptionsInTenant(com.azure.resourcemanager.marketplace.MarketplaceManager manager) {
        manager.privateStores()
            .fetchAllSubscriptionsInTenantWithResponse("a0e28e55-90c4-41d8-8e34-bb7ef7775406", null,
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateStore_Get

```java
/**
 * Samples for PrivateStore Get.
 */
public final class PrivateStoreGetSamples {
    /*
     * x-ms-original-file: 2025-01-01/GetPrivateStore.json
     */
    /**
     * Sample code: GetPrivateStore.
     * 
     * @param manager Entry point to MarketplaceManager.
     */
    public static void getPrivateStore(com.azure.resourcemanager.marketplace.MarketplaceManager manager) {
        manager.privateStores()
            .getWithResponse("a0e28e55-90c4-41d8-8e34-bb7ef7775406", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateStore_GetAdminRequestApproval

```java
/**
 * Samples for PrivateStore GetAdminRequestApproval.
 */
public final class PrivateStoreGetAdminRequestApprovalSamples {
    /*
     * x-ms-original-file: 2025-01-01/GetAdminRequestApproval.json
     */
    /**
     * Sample code: GetAdminRequestApproval.
     * 
     * @param manager Entry point to MarketplaceManager.
     */
    public static void getAdminRequestApproval(com.azure.resourcemanager.marketplace.MarketplaceManager manager) {
        manager.privateStores()
            .getAdminRequestApprovalWithResponse("a0e28e55-90c4-41d8-8e34-bb7ef7775406",
                "marketplacetestthirdparty.md-test-third-party-2", "marketplacetestthirdparty",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateStore_GetApprovalRequestsList

```java
/**
 * Samples for PrivateStore GetApprovalRequestsList.
 */
public final class PrivateStoreGetApprovalRequestsListSamples {
    /*
     * x-ms-original-file: 2025-01-01/GetApprovalRequestsList.json
     */
    /**
     * Sample code: GetApprovalRequestsList.
     * 
     * @param manager Entry point to MarketplaceManager.
     */
    public static void getApprovalRequestsList(com.azure.resourcemanager.marketplace.MarketplaceManager manager) {
        manager.privateStores()
            .getApprovalRequestsListWithResponse("a0e28e55-90c4-41d8-8e34-bb7ef7775406",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateStore_GetRequestApproval

```java
/**
 * Samples for PrivateStore GetRequestApproval.
 */
public final class PrivateStoreGetRequestApprovalSamples {
    /*
     * x-ms-original-file: 2025-01-01/GetRequestApproval.json
     */
    /**
     * Sample code: GetApprovalRequest.
     * 
     * @param manager Entry point to MarketplaceManager.
     */
    public static void getApprovalRequest(com.azure.resourcemanager.marketplace.MarketplaceManager manager) {
        manager.privateStores()
            .getRequestApprovalWithResponse("a0e28e55-90c4-41d8-8e34-bb7ef7775406",
                "marketplacetestthirdparty.md-test-third-party-2", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateStore_List

```java
/**
 * Samples for PrivateStore List.
 */
public final class PrivateStoreListSamples {
    /*
     * x-ms-original-file: 2025-01-01/GetPrivateStores.json
     */
    /**
     * Sample code: GetPrivateStores.
     * 
     * @param manager Entry point to MarketplaceManager.
     */
    public static void getPrivateStores(com.azure.resourcemanager.marketplace.MarketplaceManager manager) {
        manager.privateStores().list(null, com.azure.core.util.Context.NONE);
    }
}
```

### PrivateStore_ListNewPlansNotifications

```java
/**
 * Samples for PrivateStore ListNewPlansNotifications.
 */
public final class PrivateStoreListNewPlansNotificationsSamples {
    /*
     * x-ms-original-file: 2025-01-01/ListNewPlansNotifications.json
     */
    /**
     * Sample code: ListNewPlansNotifications.
     * 
     * @param manager Entry point to MarketplaceManager.
     */
    public static void listNewPlansNotifications(com.azure.resourcemanager.marketplace.MarketplaceManager manager) {
        manager.privateStores()
            .listNewPlansNotificationsWithResponse("a0e28e55-90c4-41d8-8e34-bb7ef7775406",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateStore_ListStopSellOffersPlansNotifications

```java

/**
 * Samples for PrivateStore ListStopSellOffersPlansNotifications.
 */
public final class PrivateStoreListStopSellOffersPlansNotificationsSamples {
    /*
     * x-ms-original-file: 2025-01-01/ListStopSellOffersPlansNotifications.json
     */
    /**
     * Sample code: ListStopSellOffersPlansNotifications.
     * 
     * @param manager Entry point to MarketplaceManager.
     */
    public static void
        listStopSellOffersPlansNotifications(com.azure.resourcemanager.marketplace.MarketplaceManager manager) {
        manager.privateStores()
            .listStopSellOffersPlansNotificationsWithResponse("a0e28e55-90c4-41d8-8e34-bb7ef7775406", null,
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateStore_ListSubscriptionsContext

```java
/**
 * Samples for PrivateStore ListSubscriptionsContext.
 */
public final class PrivateStoreListSubscriptionsContextSamples {
    /*
     * x-ms-original-file: 2025-01-01/ListSubscriptionsContext.json
     */
    /**
     * Sample code: ListSubscriptionsContext.
     * 
     * @param manager Entry point to MarketplaceManager.
     */
    public static void listSubscriptionsContext(com.azure.resourcemanager.marketplace.MarketplaceManager manager) {
        manager.privateStores()
            .listSubscriptionsContextWithResponse("a0e28e55-90c4-41d8-8e34-bb7ef7775406",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateStore_QueryApprovedPlans

```java
import com.azure.resourcemanager.marketplace.models.QueryApprovedPlansPayload;
import java.util.Arrays;

/**
 * Samples for PrivateStore QueryApprovedPlans.
 */
public final class PrivateStoreQueryApprovedPlansSamples {
    /*
     * x-ms-original-file: 2025-01-01/QueryApprovedPlans.json
     */
    /**
     * Sample code: QueryApprovedPlans.
     * 
     * @param manager Entry point to MarketplaceManager.
     */
    public static void queryApprovedPlans(com.azure.resourcemanager.marketplace.MarketplaceManager manager) {
        manager.privateStores()
            .queryApprovedPlansWithResponse("a0e28e55-90c4-41d8-8e34-bb7ef7775406",
                new QueryApprovedPlansPayload().withOfferId("marketplacetestthirdparty.md-test-third-party-2")
                    .withPlanIds(Arrays.asList("testPlanA", "testPlanB", "testPlanC"))
                    .withSubscriptionIds(
                        Arrays.asList("85e3e079-c718-4e4c-abbe-f72fceba8305", "7752d461-4bf1-4185-8b56-8a3f11486ac6")),
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateStore_QueryNotificationsState

```java
/**
 * Samples for PrivateStore QueryNotificationsState.
 */
public final class PrivateStoreQueryNotificationsStateSamples {
    /*
     * x-ms-original-file: 2025-01-01/NotificationsState.json
     */
    /**
     * Sample code: NotificationsState.
     * 
     * @param manager Entry point to MarketplaceManager.
     */
    public static void notificationsState(com.azure.resourcemanager.marketplace.MarketplaceManager manager) {
        manager.privateStores()
            .queryNotificationsStateWithResponse("a0e28e55-90c4-41d8-8e34-bb7ef7775406",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateStore_QueryOffers

```java
/**
 * Samples for PrivateStore QueryOffers.
 */
public final class PrivateStoreQueryOffersSamples {
    /*
     * x-ms-original-file: 2025-01-01/QueryOffers.json
     */
    /**
     * Sample code: QueryOffers.
     * 
     * @param manager Entry point to MarketplaceManager.
     */
    public static void queryOffers(com.azure.resourcemanager.marketplace.MarketplaceManager manager) {
        manager.privateStores()
            .queryOffersWithResponse("a0e28e55-90c4-41d8-8e34-bb7ef7775406", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateStore_QueryRequestApproval

```java
import com.azure.resourcemanager.marketplace.models.QueryRequestApprovalProperties;
import com.azure.resourcemanager.marketplace.models.RequestDetails;
import java.util.Arrays;

/**
 * Samples for PrivateStore QueryRequestApproval.
 */
public final class PrivateStoreQueryRequestApprovalSamples {
    /*
     * x-ms-original-file: 2025-01-01/QueryRequestApproval.json
     */
    /**
     * Sample code: QueryRequestApproval.
     * 
     * @param manager Entry point to MarketplaceManager.
     */
    public static void queryRequestApproval(com.azure.resourcemanager.marketplace.MarketplaceManager manager) {
        manager.privateStores()
            .queryRequestApprovalWithResponse("a0e28e55-90c4-41d8-8e34-bb7ef7775406",
                "marketplacetestthirdparty.md-test-third-party-2",
                new QueryRequestApprovalProperties()
                    .withProperties(new RequestDetails().withPublisherId("marketplacetestthirdparty")
                        .withPlanIds(Arrays.asList("testPlanA", "testPlanB", "*"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateStore_QueryUserOffers

```java
import com.azure.resourcemanager.marketplace.models.QueryUserOffersProperties;
import java.util.Arrays;

/**
 * Samples for PrivateStore QueryUserOffers.
 */
public final class PrivateStoreQueryUserOffersSamples {
    /*
     * x-ms-original-file: 2025-01-01/QueryUserOffers.json
     */
    /**
     * Sample code: QueryUserOffers.
     * 
     * @param manager Entry point to MarketplaceManager.
     */
    public static void queryUserOffers(com.azure.resourcemanager.marketplace.MarketplaceManager manager) {
        manager.privateStores()
            .queryUserOffersWithResponse("a0e28e55-90c4-41d8-8e34-bb7ef7775406",
                new QueryUserOffersProperties().withOfferIds(Arrays.asList("contoso.logger", "contoso.monitor"))
                    .withSubscriptionIds(Arrays.asList("b340914e-353d-453a-85fb-8f9b65b51f91")),
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateStore_UpdateAdminRequestApproval

```java
import com.azure.resourcemanager.marketplace.fluent.models.AdminRequestApprovalsResourceInner;
import com.azure.resourcemanager.marketplace.models.AdminAction;
import java.util.Arrays;

/**
 * Samples for PrivateStore UpdateAdminRequestApproval.
 */
public final class PrivateStoreUpdateAdminRequestApprovalSamples {
    /*
     * x-ms-original-file: 2025-01-01/UpdateAdminRequestApproval.json
     */
    /**
     * Sample code: UpdateAdminRequestApproval.
     * 
     * @param manager Entry point to MarketplaceManager.
     */
    public static void updateAdminRequestApproval(com.azure.resourcemanager.marketplace.MarketplaceManager manager) {
        manager.privateStores()
            .updateAdminRequestApprovalWithResponse("a0e28e55-90c4-41d8-8e34-bb7ef7775406",
                "marketplacetestthirdparty.md-test-third-party-2",
                new AdminRequestApprovalsResourceInner().withPublisherId("marketplacetestthirdparty")
                    .withAdminAction(AdminAction.APPROVED)
                    .withApprovedPlans(Arrays.asList("testPlan"))
                    .withComment("I'm ok with that")
                    .withCollectionIds(
                        Arrays.asList("f8ee227e-85d7-477d-abbf-854d6decaf70", "39246ad6-c521-4fed-8de7-77dede2e873f")),
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateStore_WithdrawPlan

```java
import com.azure.resourcemanager.marketplace.models.WithdrawProperties;

/**
 * Samples for PrivateStore WithdrawPlan.
 */
public final class PrivateStoreWithdrawPlanSamples {
    /*
     * x-ms-original-file: 2025-01-01/WithdrawPlan.json
     */
    /**
     * Sample code: WithdrawPlan.
     * 
     * @param manager Entry point to MarketplaceManager.
     */
    public static void withdrawPlan(com.azure.resourcemanager.marketplace.MarketplaceManager manager) {
        manager.privateStores()
            .withdrawPlanWithResponse("a0e28e55-90c4-41d8-8e34-bb7ef7775406",
                "marketplacetestthirdparty.md-test-third-party-2",
                new WithdrawProperties().withPlanId("*").withPublisherId("marketplacetestthirdparty"),
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateStoreCollection_ApproveAllItems

```java
/**
 * Samples for PrivateStoreCollection ApproveAllItems.
 */
public final class PrivateStoreCollectionApproveAllItemsSamples {
    /*
     * x-ms-original-file: 2025-01-01/ApproveAllItems.json
     */
    /**
     * Sample code: ApproveAllItems.
     * 
     * @param manager Entry point to MarketplaceManager.
     */
    public static void approveAllItems(com.azure.resourcemanager.marketplace.MarketplaceManager manager) {
        manager.privateStoreCollections()
            .approveAllItemsWithResponse("a0e28e55-90c4-41d8-8e34-bb7ef7775406", "56a1a02d-8cf8-45df-bf37-d5f7120fcb3d",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateStoreCollection_CreateOrUpdate

```java
import com.azure.resourcemanager.marketplace.fluent.models.CollectionInner;
import java.util.Arrays;

/**
 * Samples for PrivateStoreCollection CreateOrUpdate.
 */
public final class PrivateStoreCollectionCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-01-01/CreatePrivateStoreCollection.json
     */
    /**
     * Sample code: CreatePrivateStoreCollection.
     * 
     * @param manager Entry point to MarketplaceManager.
     */
    public static void createPrivateStoreCollection(com.azure.resourcemanager.marketplace.MarketplaceManager manager) {
        manager.privateStoreCollections()
            .createOrUpdateWithResponse("a0e28e55-90c4-41d8-8e34-bb7ef7775406", "d0f5aa2c-ecc3-4d87-906a-f8c486dcc4f1",
                new CollectionInner().withCollectionName("Test Collection")
                    .withClaim("")
                    .withAllSubscriptions(false)
                    .withSubscriptionsList(
                        Arrays.asList("b340914e-353d-453a-85fb-8f9b65b51f91", "f2baa04d-5bfc-461b-b6d8-61b403c9ec48")),
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateStoreCollection_Delete

```java
/**
 * Samples for PrivateStoreCollection Delete.
 */
public final class PrivateStoreCollectionDeleteSamples {
    /*
     * x-ms-original-file: 2025-01-01/DeletePrivateStoreCollection.json
     */
    /**
     * Sample code: DeletePrivateStoreCollection.
     * 
     * @param manager Entry point to MarketplaceManager.
     */
    public static void deletePrivateStoreCollection(com.azure.resourcemanager.marketplace.MarketplaceManager manager) {
        manager.privateStoreCollections()
            .deleteByResourceGroupWithResponse("a0e28e55-90c4-41d8-8e34-bb7ef7775406",
                "d0f5aa2c-ecc3-4d87-906a-f8c486dcc4f1", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateStoreCollection_DisableApproveAllItems

```java
/**
 * Samples for PrivateStoreCollection DisableApproveAllItems.
 */
public final class PrivateStoreCollectionDisableApproveAllItemsSamples {
    /*
     * x-ms-original-file: 2025-01-01/DisableApproveAllItems.json
     */
    /**
     * Sample code: DisableApproveAllItems.
     * 
     * @param manager Entry point to MarketplaceManager.
     */
    public static void disableApproveAllItems(com.azure.resourcemanager.marketplace.MarketplaceManager manager) {
        manager.privateStoreCollections()
            .disableApproveAllItemsWithResponse("a0e28e55-90c4-41d8-8e34-bb7ef7775406",
                "56a1a02d-8cf8-45df-bf37-d5f7120fcb3d", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateStoreCollection_Get

```java
/**
 * Samples for PrivateStoreCollection Get.
 */
public final class PrivateStoreCollectionGetSamples {
    /*
     * x-ms-original-file: 2025-01-01/GetPrivateStoreCollection.json
     */
    /**
     * Sample code: GetPrivateStoreCollection.
     * 
     * @param manager Entry point to MarketplaceManager.
     */
    public static void getPrivateStoreCollection(com.azure.resourcemanager.marketplace.MarketplaceManager manager) {
        manager.privateStoreCollections()
            .getWithResponse("a0e28e55-90c4-41d8-8e34-bb7ef7775406", "56a1a02d-8cf8-45df-bf37-d5f7120fcb3d",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateStoreCollection_List

```java
/**
 * Samples for PrivateStoreCollection List.
 */
public final class PrivateStoreCollectionListSamples {
    /*
     * x-ms-original-file: 2025-01-01/GetPrivateStoreCollectionsList.json
     */
    /**
     * Sample code: GetPrivateStoreCollectionsList.
     * 
     * @param manager Entry point to MarketplaceManager.
     */
    public static void
        getPrivateStoreCollectionsList(com.azure.resourcemanager.marketplace.MarketplaceManager manager) {
        manager.privateStoreCollections()
            .listWithResponse("a0e28e55-90c4-41d8-8e34-bb7ef7775406", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateStoreCollection_TransferOffers

```java
import com.azure.resourcemanager.marketplace.models.TransferOffersProperties;
import java.util.Arrays;

/**
 * Samples for PrivateStoreCollection TransferOffers.
 */
public final class PrivateStoreCollectionTransferOffersSamples {
    /*
     * x-ms-original-file: 2025-01-01/TransferOffers.json
     */
    /**
     * Sample code: TransferOffers.
     * 
     * @param manager Entry point to MarketplaceManager.
     */
    public static void transferOffers(com.azure.resourcemanager.marketplace.MarketplaceManager manager) {
        manager.privateStoreCollections()
            .transferOffersWithResponse("a0e28e55-90c4-41d8-8e34-bb7ef7775406", "56a1a02d-8cf8-45df-bf37-d5f7120fcb3d",
                new TransferOffersProperties()
                    .withTargetCollections(
                        Arrays.asList("c752f021-1c37-4af5-b82f-74c51c27b44a", "f47ef1c7-e908-4f39-ae29-db181634ad8d"))
                    .withOperation("copy")
                    .withOfferIdsList(Arrays.asList("marketplacetestthirdparty.md-test-third-party-2",
                        "marketplacetestthirdparty.md-test-third-party-3")),
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateStoreCollectionOffer_ContextsView

```java
import com.azure.resourcemanager.marketplace.models.CollectionOffersByAllContextsPayload;
import java.util.Arrays;

/**
 * Samples for PrivateStoreCollectionOffer ContextsView.
 */
public final class PrivateStoreCollectionOfferContextsViewSamples {
    /*
     * x-ms-original-file: 2025-01-01/GetPrivateStoreCollectionOfferContextsView.json
     */
    /**
     * Sample code: GetPrivateStoreCollectionOfferContextsView.
     * 
     * @param manager Entry point to MarketplaceManager.
     */
    public static void
        getPrivateStoreCollectionOfferContextsView(com.azure.resourcemanager.marketplace.MarketplaceManager manager) {
        manager.privateStoreCollectionOffers()
            .contextsViewWithResponse("a0e28e55-90c4-41d8-8e34-bb7ef7775406", "56a1a02d-8cf8-45df-bf37-d5f7120fcb3d",
                "mktp3pp.kuku-buku",
                new CollectionOffersByAllContextsPayload().withSubscriptionIds(
                    Arrays.asList("b340914e-353d-453a-85fb-8f9b65b51f91", "f2baa04d-5bfc-461b-b6d8-61b403c9ec48")),
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateStoreCollectionOffer_CreateOrUpdate

```java
import com.azure.resourcemanager.marketplace.fluent.models.OfferInner;
import java.util.Arrays;

/**
 * Samples for PrivateStoreCollectionOffer CreateOrUpdate.
 */
public final class PrivateStoreCollectionOfferCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-01-01/PrivateStoreOffer_update.json
     */
    /**
     * Sample code: PrivateStoreOffer_update.
     * 
     * @param manager Entry point to MarketplaceManager.
     */
    public static void privateStoreOfferUpdate(com.azure.resourcemanager.marketplace.MarketplaceManager manager) {
        manager.privateStoreCollectionOffers()
            .createOrUpdateWithResponse("a0e28e55-90c4-41d8-8e34-bb7ef7775406", "56a1a02d-8cf8-45df-bf37-d5f7120fcb3d",
                "marketplacetestthirdparty.md-test-third-party-2",
                new OfferInner().withETag("\"9301f4fd-0000-0100-0000-5e248b350666\"")
                    .withSpecificPlanIdsLimitation(Arrays.asList("0001", "0002")),
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateStoreCollectionOffer_Delete

```java
/**
 * Samples for PrivateStoreCollectionOffer Delete.
 */
public final class PrivateStoreCollectionOfferDeleteSamples {
    /*
     * x-ms-original-file: 2025-01-01/DeletePrivateStoreOffer.json
     */
    /**
     * Sample code: DeletePrivateStoreOffer.
     * 
     * @param manager Entry point to MarketplaceManager.
     */
    public static void deletePrivateStoreOffer(com.azure.resourcemanager.marketplace.MarketplaceManager manager) {
        manager.privateStoreCollectionOffers()
            .deleteWithResponse("a0e28e55-90c4-41d8-8e34-bb7ef7775406", "56a1a02d-8cf8-45df-bf37-d5f7120fcb3d",
                "marketplacetestthirdparty.md-test-third-party-2", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateStoreCollectionOffer_Get

```java
/**
 * Samples for PrivateStoreCollectionOffer Get.
 */
public final class PrivateStoreCollectionOfferGetSamples {
    /*
     * x-ms-original-file: 2025-01-01/GetPrivateStoreCollectionOffer.json
     */
    /**
     * Sample code: GetPrivateStoreCollectionOffer.
     * 
     * @param manager Entry point to MarketplaceManager.
     */
    public static void
        getPrivateStoreCollectionOffer(com.azure.resourcemanager.marketplace.MarketplaceManager manager) {
        manager.privateStoreCollectionOffers()
            .getWithResponse("a0e28e55-90c4-41d8-8e34-bb7ef7775406", "56a1a02d-8cf8-45df-bf37-d5f7120fcb3d",
                "marketplacetestthirdparty.md-test-third-party-2", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateStoreCollectionOffer_List

```java
/**
 * Samples for PrivateStoreCollectionOffer List.
 */
public final class PrivateStoreCollectionOfferListSamples {
    /*
     * x-ms-original-file: 2025-01-01/GetPrivateStoreOffers.json
     */
    /**
     * Sample code: GetPrivateStoreOffers.
     * 
     * @param manager Entry point to MarketplaceManager.
     */
    public static void getPrivateStoreOffers(com.azure.resourcemanager.marketplace.MarketplaceManager manager) {
        manager.privateStoreCollectionOffers()
            .list("a0e28e55-90c4-41d8-8e34-bb7ef7775406", "56a1a02d-8cf8-45df-bf37-d5f7120fcb3d",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateStoreCollectionOffer_ListByContexts

```java
import com.azure.resourcemanager.marketplace.models.CollectionOffersByAllContextsPayload;
import java.util.Arrays;

/**
 * Samples for PrivateStoreCollectionOffer ListByContexts.
 */
public final class PrivateStoreCollectionOfferListByContextsSamples {
    /*
     * x-ms-original-file: 2025-01-01/GetPrivateStoreCollectionOffersWithFullContext.json
     */
    /**
     * Sample code: GetPrivateStoreCollectionOffersWithFullContext.
     * 
     * @param manager Entry point to MarketplaceManager.
     */
    public static void getPrivateStoreCollectionOffersWithFullContext(
        com.azure.resourcemanager.marketplace.MarketplaceManager manager) {
        manager.privateStoreCollectionOffers()
            .listByContexts("a0e28e55-90c4-41d8-8e34-bb7ef7775406", "56a1a02d-8cf8-45df-bf37-d5f7120fcb3d",
                new CollectionOffersByAllContextsPayload().withSubscriptionIds(
                    Arrays.asList("b340914e-353d-453a-85fb-8f9b65b51f91", "f2baa04d-5bfc-461b-b6d8-61b403c9ec48")),
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateStoreCollectionOffer_UpsertOfferWithMultiContext

```java
import com.azure.resourcemanager.marketplace.models.ContextAndPlansDetails;
import com.azure.resourcemanager.marketplace.models.MultiContextAndPlansPayload;
import java.util.Arrays;

/**
 * Samples for PrivateStoreCollectionOffer UpsertOfferWithMultiContext.
 */
public final class PrivateStoreCollectionOfferUpsertOfferWithMultiContextSamples {
    /*
     * x-ms-original-file: 2025-01-01/UpsertOfferWithMultiContext.json
     */
    /**
     * Sample code: UpsertOfferWithMultiContext.
     * 
     * @param manager Entry point to MarketplaceManager.
     */
    public static void upsertOfferWithMultiContext(com.azure.resourcemanager.marketplace.MarketplaceManager manager) {
        manager.privateStoreCollectionOffers()
            .upsertOfferWithMultiContextWithResponse("a0e28e55-90c4-41d8-8e34-bb7ef7775406",
                "56a1a02d-8cf8-45df-bf37-d5f7120fcb3d", "contoso.logger",
                new MultiContextAndPlansPayload().withOfferId("contoso.logger")
                    .withETag("\"9301f4fd-0000-0100-0000-5e248b350332\"")
                    .withPlansContext(Arrays.asList(
                        new ContextAndPlansDetails().withContext("a5edbe7d-9f73-47fd-834a-0d6142f4c7a1")
                            .withPlanIds(Arrays.asList("log4db", "log4file")),
                        new ContextAndPlansDetails().withContext("45b604af-19bb-448e-a761-4a6be7374b2f")
                            .withPlanIds(Arrays.asList("log4web")))),
                com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_QueryRules

```java
/**
 * Samples for ResourceProvider QueryRules.
 */
public final class ResourceProviderQueryRulesSamples {
    /*
     * x-ms-original-file: 2025-01-01/GetCollectionRules.json
     */
    /**
     * Sample code: GetCollectionRules.
     * 
     * @param manager Entry point to MarketplaceManager.
     */
    public static void getCollectionRules(com.azure.resourcemanager.marketplace.MarketplaceManager manager) {
        manager.resourceProviders()
            .queryRulesWithResponse("a0e28e55-90c4-41d8-8e34-bb7ef7775406", "56a1a02d-8cf8-45df-bf37-d5f7120fcb3d",
                com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_QueryUserRules

```java
import com.azure.resourcemanager.marketplace.models.QueryUserRulesProperties;
import java.util.Arrays;

/**
 * Samples for ResourceProvider QueryUserRules.
 */
public final class ResourceProviderQueryUserRulesSamples {
    /*
     * x-ms-original-file: 2025-01-01/QueryUserRules.json
     */
    /**
     * Sample code: QueryUserRules.
     * 
     * @param manager Entry point to MarketplaceManager.
     */
    public static void queryUserRules(com.azure.resourcemanager.marketplace.MarketplaceManager manager) {
        manager.resourceProviders()
            .queryUserRulesWithResponse("a0e28e55-90c4-41d8-8e34-bb7ef7775406", new QueryUserRulesProperties()
                .withSubscriptionIds(Arrays.asList("b340914e-353d-453a-85fb-8f9b65b51f91")),
                com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_SetCollectionRules

```java
import com.azure.resourcemanager.marketplace.models.Rule;
import com.azure.resourcemanager.marketplace.models.RuleType;
import com.azure.resourcemanager.marketplace.models.SetRulesRequest;
import java.util.Arrays;

/**
 * Samples for ResourceProvider SetCollectionRules.
 */
public final class ResourceProviderSetCollectionRulesSamples {
    /*
     * x-ms-original-file: 2025-01-01/SetCollectionRules.json
     */
    /**
     * Sample code: SetCollectionRules.
     * 
     * @param manager Entry point to MarketplaceManager.
     */
    public static void setCollectionRules(com.azure.resourcemanager.marketplace.MarketplaceManager manager) {
        manager.resourceProviders()
            .setCollectionRulesWithResponse("a0e28e55-90c4-41d8-8e34-bb7ef7775406",
                "56a1a02d-8cf8-45df-bf37-d5f7120fcb3d",
                new SetRulesRequest().withValue(Arrays.asList(new Rule().withType(RuleType.PRIVATE_PRODUCTS))),
                com.azure.core.util.Context.NONE);
    }
}
```

