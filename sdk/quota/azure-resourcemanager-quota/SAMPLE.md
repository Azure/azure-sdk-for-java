# Code snippets and samples


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

## Usages

- [Get](#usages_get)
- [List](#usages_list)
### Operation_List

```java
import com.azure.core.util.Context;

/** Samples for Operation List. */
public final class OperationListSamples {
    /*
     * x-ms-original-file: specification/quota/resource-manager/Microsoft.Quota/preview/2021-03-15-preview/examples/GetOperations.json
     */
    /**
     * Sample code: GetOperations.
     *
     * @param manager Entry point to QuotaManager.
     */
    public static void getOperations(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### Quota_CreateOrUpdate

```java
import com.azure.resourcemanager.quota.models.LimitType;
import com.azure.resourcemanager.quota.models.LimitValue;
import com.azure.resourcemanager.quota.models.QuotaProperties;
import com.azure.resourcemanager.quota.models.ResourceName;

/** Samples for Quota CreateOrUpdate. */
public final class QuotaCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/quota/resource-manager/Microsoft.Quota/preview/2021-03-15-preview/examples/putMachineLearningServicesQuotaRequestLowPriority.json
     */
    /**
     * Sample code: Quotas_Request_ForMachineLearningServices_LowPriorityResource.
     *
     * @param manager Entry point to QuotaManager.
     */
    public static void quotasRequestForMachineLearningServicesLowPriorityResource(
        com.azure.resourcemanager.quota.QuotaManager manager) {
        manager
            .quotas()
            .define("TotalLowPriorityCores")
            .withExistingScope(
                "subscriptions/D7EC67B3-7657-4966-BFFC-41EFD36BAAB3/providers/Microsoft.MachineLearningServices/locations/eastus")
            .withProperties(
                new QuotaProperties()
                    .withLimit(new LimitValue().withValue(10).withLimitObjectType(LimitType.LIMIT_VALUE))
                    .withName(new ResourceName().withValue("TotalLowPriorityCores"))
                    .withResourceType("lowPriority"))
            .create();
    }

    /*
     * x-ms-original-file: specification/quota/resource-manager/Microsoft.Quota/preview/2021-03-15-preview/examples/putNetworkOneSkuQuotaRequestStandardSkuPublicIpAddresses.json
     */
    /**
     * Sample code: Quotas_PutRequest_ForNetwork_StandardSkuPublicIpAddressesResource.
     *
     * @param manager Entry point to QuotaManager.
     */
    public static void quotasPutRequestForNetworkStandardSkuPublicIpAddressesResource(
        com.azure.resourcemanager.quota.QuotaManager manager) {
        manager
            .quotas()
            .define("StandardSkuPublicIpAddresses")
            .withExistingScope(
                "subscriptions/D7EC67B3-7657-4966-BFFC-41EFD36BAAB3/providers/Microsoft.Network/locations/eastus")
            .withProperties(
                new QuotaProperties()
                    .withLimit(new LimitValue().withValue(10).withLimitObjectType(LimitType.LIMIT_VALUE))
                    .withName(new ResourceName().withValue("StandardSkuPublicIpAddresses"))
                    .withResourceType("PublicIpAddresses"))
            .create();
    }

    /*
     * x-ms-original-file: specification/quota/resource-manager/Microsoft.Quota/preview/2021-03-15-preview/examples/putComputeOneSkuQuotaRequest.json
     */
    /**
     * Sample code: Quotas_Put_Request_ForCompute.
     *
     * @param manager Entry point to QuotaManager.
     */
    public static void quotasPutRequestForCompute(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager
            .quotas()
            .define("standardFSv2Family")
            .withExistingScope(
                "subscriptions/D7EC67B3-7657-4966-BFFC-41EFD36BAAB3/providers/Microsoft.Compute/locations/eastus")
            .withProperties(
                new QuotaProperties()
                    .withLimit(new LimitValue().withValue(10).withLimitObjectType(LimitType.LIMIT_VALUE))
                    .withName(new ResourceName().withValue("standardFSv2Family")))
            .create();
    }

    /*
     * x-ms-original-file: specification/quota/resource-manager/Microsoft.Quota/preview/2021-03-15-preview/examples/putNetworkOneSkuQuotaRequest.json
     */
    /**
     * Sample code: Quotas_PutRequest_ForNetwork.
     *
     * @param manager Entry point to QuotaManager.
     */
    public static void quotasPutRequestForNetwork(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager
            .quotas()
            .define("MinPublicIpInterNetworkPrefixLength")
            .withExistingScope(
                "subscriptions/D7EC67B3-7657-4966-BFFC-41EFD36BAAB3/providers/Microsoft.Network/locations/eastus")
            .withProperties(
                new QuotaProperties()
                    .withLimit(new LimitValue().withValue(10).withLimitObjectType(LimitType.LIMIT_VALUE))
                    .withName(new ResourceName().withValue("MinPublicIpInterNetworkPrefixLength"))
                    .withResourceType("MinPublicIpInterNetworkPrefixLength"))
            .create();
    }
}
```

### Quota_Get

```java
import com.azure.core.util.Context;

/** Samples for Quota Get. */
public final class QuotaGetSamples {
    /*
     * x-ms-original-file: specification/quota/resource-manager/Microsoft.Quota/preview/2021-03-15-preview/examples/getNetworkOneSkuQuotaLimit.json
     */
    /**
     * Sample code: Quotas_UsagesRequest_ForNetwork.
     *
     * @param manager Entry point to QuotaManager.
     */
    public static void quotasUsagesRequestForNetwork(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager
            .quotas()
            .getWithResponse(
                "MinPublicIpInterNetworkPrefixLength",
                "subscriptions/00000000-0000-0000-0000-000000000000/providers/Microsoft.Network/locations/eastus",
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/quota/resource-manager/Microsoft.Quota/preview/2021-03-15-preview/examples/getComputeOneSkuQuotaLimit.json
     */
    /**
     * Sample code: Quotas_Get_Request_ForCompute.
     *
     * @param manager Entry point to QuotaManager.
     */
    public static void quotasGetRequestForCompute(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager
            .quotas()
            .getWithResponse(
                "standardNDSFamily",
                "subscriptions/00000000-0000-0000-0000-000000000000/providers/Microsoft.Compute/locations/eastus",
                Context.NONE);
    }
}
```

### Quota_List

```java
import com.azure.core.util.Context;

/** Samples for Quota List. */
public final class QuotaListSamples {
    /*
     * x-ms-original-file: specification/quota/resource-manager/Microsoft.Quota/preview/2021-03-15-preview/examples/getMachineLearningServicesQuotaLimits.json
     */
    /**
     * Sample code: Quotas_listQuotaLimitsMachineLearningServices.
     *
     * @param manager Entry point to QuotaManager.
     */
    public static void quotasListQuotaLimitsMachineLearningServices(
        com.azure.resourcemanager.quota.QuotaManager manager) {
        manager
            .quotas()
            .list(
                "subscriptions/00000000-0000-0000-0000-000000000000/providers/Microsoft.MachineLearningServices/locations/eastus",
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/quota/resource-manager/Microsoft.Quota/preview/2021-03-15-preview/examples/getComputeQuotaLimits.json
     */
    /**
     * Sample code: Quotas_listQuotaLimitsForCompute.
     *
     * @param manager Entry point to QuotaManager.
     */
    public static void quotasListQuotaLimitsForCompute(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager
            .quotas()
            .list(
                "subscriptions/00000000-0000-0000-0000-000000000000/providers/Microsoft.Compute/locations/eastus",
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/quota/resource-manager/Microsoft.Quota/preview/2021-03-15-preview/examples/getNetworkQuotaLimits.json
     */
    /**
     * Sample code: Quotas_listQuotaLimitsForNetwork.
     *
     * @param manager Entry point to QuotaManager.
     */
    public static void quotasListQuotaLimitsForNetwork(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager
            .quotas()
            .list(
                "subscriptions/00000000-0000-0000-0000-000000000000/providers/Microsoft.Network/locations/eastus",
                Context.NONE);
    }
}
```

### Quota_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.quota.models.CurrentQuotaLimitBase;
import com.azure.resourcemanager.quota.models.LimitType;
import com.azure.resourcemanager.quota.models.LimitValue;
import com.azure.resourcemanager.quota.models.QuotaProperties;
import com.azure.resourcemanager.quota.models.ResourceName;

/** Samples for Quota Update. */
public final class QuotaUpdateSamples {
    /*
     * x-ms-original-file: specification/quota/resource-manager/Microsoft.Quota/preview/2021-03-15-preview/examples/patchComputeQuotaRequest.json
     */
    /**
     * Sample code: Quotas_Request_PatchForCompute.
     *
     * @param manager Entry point to QuotaManager.
     */
    public static void quotasRequestPatchForCompute(com.azure.resourcemanager.quota.QuotaManager manager) {
        CurrentQuotaLimitBase resource =
            manager
                .quotas()
                .getWithResponse(
                    "standardFSv2Family",
                    "subscriptions/D7EC67B3-7657-4966-BFFC-41EFD36BAAB3/providers/Microsoft.Compute/locations/eastus",
                    Context.NONE)
                .getValue();
        resource
            .update()
            .withProperties(
                new QuotaProperties()
                    .withLimit(new LimitValue().withValue(10).withLimitObjectType(LimitType.LIMIT_VALUE))
                    .withName(new ResourceName().withValue("standardFSv2Family")))
            .apply();
    }

    /*
     * x-ms-original-file: specification/quota/resource-manager/Microsoft.Quota/preview/2021-03-15-preview/examples/patchNetworkOneSkuQuotaRequest.json
     */
    /**
     * Sample code: Quotas_Request_PatchForNetwork.
     *
     * @param manager Entry point to QuotaManager.
     */
    public static void quotasRequestPatchForNetwork(com.azure.resourcemanager.quota.QuotaManager manager) {
        CurrentQuotaLimitBase resource =
            manager
                .quotas()
                .getWithResponse(
                    "MinPublicIpInterNetworkPrefixLength",
                    "subscriptions/D7EC67B3-7657-4966-BFFC-41EFD36BAAB3/providers/Microsoft.Network/locations/eastus",
                    Context.NONE)
                .getValue();
        resource
            .update()
            .withProperties(
                new QuotaProperties()
                    .withLimit(new LimitValue().withValue(10).withLimitObjectType(LimitType.LIMIT_VALUE))
                    .withName(new ResourceName().withValue("MinPublicIpInterNetworkPrefixLength"))
                    .withResourceType("MinPublicIpInterNetworkPrefixLength"))
            .apply();
    }
}
```

### QuotaRequestStatus_Get

```java
import com.azure.core.util.Context;

/** Samples for QuotaRequestStatus Get. */
public final class QuotaRequestStatusGetSamples {
    /*
     * x-ms-original-file: specification/quota/resource-manager/Microsoft.Quota/preview/2021-03-15-preview/examples/getQuotaRequestStatusFailed.json
     */
    /**
     * Sample code: QuotaRequestFailed.
     *
     * @param manager Entry point to QuotaManager.
     */
    public static void quotaRequestFailed(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager
            .quotaRequestStatus()
            .getWithResponse(
                "2B5C8515-37D8-4B6A-879B-CD641A2CF605",
                "subscriptions/00000000-0000-0000-0000-000000000000/providers/Microsoft.Compute/locations/eastus",
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/quota/resource-manager/Microsoft.Quota/preview/2021-03-15-preview/examples/getQuotaRequestStatusById.json
     */
    /**
     * Sample code: QuotaRequestStatus.
     *
     * @param manager Entry point to QuotaManager.
     */
    public static void quotaRequestStatus(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager
            .quotaRequestStatus()
            .getWithResponse(
                "2B5C8515-37D8-4B6A-879B-CD641A2CF605",
                "subscriptions/D7EC67B3-7657-4966-BFFC-41EFD36BAAB3/providers/Microsoft.Compute/locations/eastus",
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/quota/resource-manager/Microsoft.Quota/preview/2021-03-15-preview/examples/getQuotaRequestStatusInProgress.json
     */
    /**
     * Sample code: QuotaRequestInProgress.
     *
     * @param manager Entry point to QuotaManager.
     */
    public static void quotaRequestInProgress(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager
            .quotaRequestStatus()
            .getWithResponse(
                "2B5C8515-37D8-4B6A-879B-CD641A2CF605",
                "subscriptions/00000000-0000-0000-0000-000000000000/providers/Microsoft.Compute/locations/eastus",
                Context.NONE);
    }
}
```

### QuotaRequestStatus_List

```java
import com.azure.core.util.Context;

/** Samples for QuotaRequestStatus List. */
public final class QuotaRequestStatusListSamples {
    /*
     * x-ms-original-file: specification/quota/resource-manager/Microsoft.Quota/preview/2021-03-15-preview/examples/getQuotaRequestsHistory.json
     */
    /**
     * Sample code: QuotaRequestHistory.
     *
     * @param manager Entry point to QuotaManager.
     */
    public static void quotaRequestHistory(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager
            .quotaRequestStatus()
            .list(
                "subscriptions/D7EC67B3-7657-4966-BFFC-41EFD36BAAB3/providers/Microsoft.Compute/locations/eastus",
                null,
                null,
                null,
                Context.NONE);
    }
}
```

### Usages_Get

```java
import com.azure.core.util.Context;

/** Samples for Usages Get. */
public final class UsagesGetSamples {
    /*
     * x-ms-original-file: specification/quota/resource-manager/Microsoft.Quota/preview/2021-03-15-preview/examples/getNetworkOneSkuUsages.json
     */
    /**
     * Sample code: Quotas_UsagesRequest_ForNetwork.
     *
     * @param manager Entry point to QuotaManager.
     */
    public static void quotasUsagesRequestForNetwork(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager
            .usages()
            .getWithResponse(
                "MinPublicIpInterNetworkPrefixLength",
                "subscriptions/00000000-0000-0000-0000-000000000000/providers/Microsoft.Network/locations/eastus",
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/quota/resource-manager/Microsoft.Quota/preview/2021-03-15-preview/examples/getComputeOneSkuUsages.json
     */
    /**
     * Sample code: Quotas_UsagesRequest_ForCompute.
     *
     * @param manager Entry point to QuotaManager.
     */
    public static void quotasUsagesRequestForCompute(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager
            .usages()
            .getWithResponse(
                "standardNDSFamily",
                "subscriptions/00000000-0000-0000-0000-000000000000/providers/Microsoft.Compute/locations/eastus",
                Context.NONE);
    }
}
```

### Usages_List

```java
import com.azure.core.util.Context;

/** Samples for Usages List. */
public final class UsagesListSamples {
    /*
     * x-ms-original-file: specification/quota/resource-manager/Microsoft.Quota/preview/2021-03-15-preview/examples/getComputeUsages.json
     */
    /**
     * Sample code: Quotas_listUsagesForCompute.
     *
     * @param manager Entry point to QuotaManager.
     */
    public static void quotasListUsagesForCompute(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager
            .usages()
            .list(
                "subscriptions/00000000-0000-0000-0000-000000000000/providers/Microsoft.Compute/locations/eastus",
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/quota/resource-manager/Microsoft.Quota/preview/2021-03-15-preview/examples/getNetworkUsages.json
     */
    /**
     * Sample code: Quotas_listUsagesForNetwork.
     *
     * @param manager Entry point to QuotaManager.
     */
    public static void quotasListUsagesForNetwork(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager
            .usages()
            .list(
                "subscriptions/00000000-0000-0000-0000-000000000000/providers/Microsoft.Network/locations/eastus",
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/quota/resource-manager/Microsoft.Quota/preview/2021-03-15-preview/examples/getMachineLearningServicesUsages.json
     */
    /**
     * Sample code: Quotas_listUsagesMachineLearningServices.
     *
     * @param manager Entry point to QuotaManager.
     */
    public static void quotasListUsagesMachineLearningServices(com.azure.resourcemanager.quota.QuotaManager manager) {
        manager
            .usages()
            .list(
                "subscriptions/00000000-0000-0000-0000-000000000000/providers/Microsoft.MachineLearningServices/locations/eastus",
                Context.NONE);
    }
}
```

