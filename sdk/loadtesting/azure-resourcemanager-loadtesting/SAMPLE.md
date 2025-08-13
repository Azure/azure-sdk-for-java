# Code snippets and samples


## ResourceProvider

- [CheckAvailabilityQuota](#resourceprovider_checkavailabilityquota)
- [CreateOrUpdateLoadtest](#resourceprovider_createorupdateloadtest)
- [Delete](#resourceprovider_delete)
- [GetByResourceGroup](#resourceprovider_getbyresourcegroup)
- [GetQuota](#resourceprovider_getquota)
- [List](#resourceprovider_list)
- [ListByResourceGroup](#resourceprovider_listbyresourcegroup)
- [ListQuota](#resourceprovider_listquota)
- [OutboundNetworkDependenciesEndpoints](#resourceprovider_outboundnetworkdependenciesendpoints)
- [UpdateLoadtest](#resourceprovider_updateloadtest)
### ResourceProvider_CheckAvailabilityQuota

```java
import com.azure.resourcemanager.loadtesting.models.QuotaBucketRequest;
import com.azure.resourcemanager.loadtesting.models.QuotaBucketRequestPropertiesDimensions;

/**
 * Samples for ResourceProvider CheckAvailabilityQuota.
 */
public final class ResourceProviderCheckAvailabilityQuotaSamples {
    /*
     * x-ms-original-file: 2024-12-01-preview/Quotas_CheckAvailability.json
     */
    /**
     * Sample code: Check Quota Availability on quota bucket per region per subscription.
     * 
     * @param manager Entry point to LoadTestManager.
     */
    public static void checkQuotaAvailabilityOnQuotaBucketPerRegionPerSubscription(
        com.azure.resourcemanager.loadtesting.LoadTestManager manager) {
        manager.resourceProviders()
            .checkAvailabilityQuotaWithResponse("westus", "testQuotaBucket",
                new QuotaBucketRequest().withCurrentUsage(20)
                    .withCurrentQuota(40)
                    .withNewQuota(50)
                    .withDimensions(
                        new QuotaBucketRequestPropertiesDimensions().withSubscriptionId("testsubscriptionId")
                            .withLocation("westus")),
                com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_CreateOrUpdateLoadtest

```java
import com.azure.resourcemanager.loadtesting.models.EncryptionProperties;
import com.azure.resourcemanager.loadtesting.models.EncryptionPropertiesIdentity;
import com.azure.resourcemanager.loadtesting.models.LoadTestProperties;
import com.azure.resourcemanager.loadtesting.models.ManagedServiceIdentity;
import com.azure.resourcemanager.loadtesting.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.loadtesting.models.Type;
import com.azure.resourcemanager.loadtesting.models.UserAssignedIdentity;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ResourceProvider CreateOrUpdateLoadtest.
 */
public final class ResourceProviderCreateOrUpdateLoadtestSamples {
    /*
     * x-ms-original-file: 2024-12-01-preview/LoadTests_CreateOrUpdate.json
     */
    /**
     * Sample code: Create a LoadTestResource.
     * 
     * @param manager Entry point to LoadTestManager.
     */
    public static void createALoadTestResource(com.azure.resourcemanager.loadtesting.LoadTestManager manager) {
        manager.resourceProviders()
            .define("myLoadTest")
            .withRegion("westus")
            .withExistingResourceGroup("dummyrg")
            .withTags(mapOf("Team", "Dev Exp"))
            .withProperties(new LoadTestProperties().withDescription("This is new load test resource")
                .withEncryption(new EncryptionProperties().withIdentity(new EncryptionPropertiesIdentity()
                    .withType(Type.USER_ASSIGNED)
                    .withResourceId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/dummyrg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id1"))
                    .withKeyUrl("fakeTokenPlaceholder")))
            .withIdentity(new ManagedServiceIdentity()
                .withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/dummyrg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id1",
                    new UserAssignedIdentity())))
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

### ResourceProvider_Delete

```java
/**
 * Samples for ResourceProvider Delete.
 */
public final class ResourceProviderDeleteSamples {
    /*
     * x-ms-original-file: 2024-12-01-preview/LoadTests_Delete.json
     */
    /**
     * Sample code: Delete a LoadTestResource.
     * 
     * @param manager Entry point to LoadTestManager.
     */
    public static void deleteALoadTestResource(com.azure.resourcemanager.loadtesting.LoadTestManager manager) {
        manager.resourceProviders().delete("dummyrg", "myLoadTest", com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_GetByResourceGroup

```java
/**
 * Samples for ResourceProvider GetByResourceGroup.
 */
public final class ResourceProviderGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-12-01-preview/LoadTests_Get.json
     */
    /**
     * Sample code: Get a LoadTestResource.
     * 
     * @param manager Entry point to LoadTestManager.
     */
    public static void getALoadTestResource(com.azure.resourcemanager.loadtesting.LoadTestManager manager) {
        manager.resourceProviders()
            .getByResourceGroupWithResponse("dummyrg", "myLoadTest", com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_GetQuota

```java
/**
 * Samples for ResourceProvider GetQuota.
 */
public final class ResourceProviderGetQuotaSamples {
    /*
     * x-ms-original-file: 2024-12-01-preview/Quotas_Get.json
     */
    /**
     * Sample code: Get the available quota for a quota bucket per region per subscription.
     * 
     * @param manager Entry point to LoadTestManager.
     */
    public static void getTheAvailableQuotaForAQuotaBucketPerRegionPerSubscription(
        com.azure.resourcemanager.loadtesting.LoadTestManager manager) {
        manager.resourceProviders().getQuotaWithResponse("westus", "testQuotaBucket", com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_List

```java
/**
 * Samples for ResourceProvider List.
 */
public final class ResourceProviderListSamples {
    /*
     * x-ms-original-file: 2024-12-01-preview/LoadTests_ListBySubscription.json
     */
    /**
     * Sample code: List LoadTestResource resources by subscription ID.
     * 
     * @param manager Entry point to LoadTestManager.
     */
    public static void
        listLoadTestResourceResourcesBySubscriptionID(com.azure.resourcemanager.loadtesting.LoadTestManager manager) {
        manager.resourceProviders().list(com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_ListByResourceGroup

```java
/**
 * Samples for ResourceProvider ListByResourceGroup.
 */
public final class ResourceProviderListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-12-01-preview/LoadTests_ListByResourceGroup.json
     */
    /**
     * Sample code: List LoadTestResource resources by resource group.
     * 
     * @param manager Entry point to LoadTestManager.
     */
    public static void
        listLoadTestResourceResourcesByResourceGroup(com.azure.resourcemanager.loadtesting.LoadTestManager manager) {
        manager.resourceProviders().listByResourceGroup("dummyrg", com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_ListQuota

```java
/**
 * Samples for ResourceProvider ListQuota.
 */
public final class ResourceProviderListQuotaSamples {
    /*
     * x-ms-original-file: 2024-12-01-preview/Quotas_List.json
     */
    /**
     * Sample code: List quotas for a given subscription Id.
     * 
     * @param manager Entry point to LoadTestManager.
     */
    public static void
        listQuotasForAGivenSubscriptionId(com.azure.resourcemanager.loadtesting.LoadTestManager manager) {
        manager.resourceProviders().listQuota("westus", com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_OutboundNetworkDependenciesEndpoints

```java
/**
 * Samples for ResourceProvider OutboundNetworkDependenciesEndpoints.
 */
public final class ResourceProviderOutboundNetworkDependenciesEndpointsSamples {
    /*
     * x-ms-original-file: 2024-12-01-preview/LoadTests_ListOutboundNetworkDependenciesEndpoints.json
     */
    /**
     * Sample code: Lists the endpoints that agents may call as part of load testing.
     * 
     * @param manager Entry point to LoadTestManager.
     */
    public static void listsTheEndpointsThatAgentsMayCallAsPartOfLoadTesting(
        com.azure.resourcemanager.loadtesting.LoadTestManager manager) {
        manager.resourceProviders()
            .outboundNetworkDependenciesEndpoints("default-azureloadtest-japaneast", "sampleloadtest",
                com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_UpdateLoadtest

```java
import com.azure.resourcemanager.loadtesting.models.EncryptionProperties;
import com.azure.resourcemanager.loadtesting.models.EncryptionPropertiesIdentity;
import com.azure.resourcemanager.loadtesting.models.LoadTestResource;
import com.azure.resourcemanager.loadtesting.models.LoadTestResourceUpdateProperties;
import com.azure.resourcemanager.loadtesting.models.ManagedServiceIdentity;
import com.azure.resourcemanager.loadtesting.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.loadtesting.models.Type;
import com.azure.resourcemanager.loadtesting.models.UserAssignedIdentity;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ResourceProvider UpdateLoadtest.
 */
public final class ResourceProviderUpdateLoadtestSamples {
    /*
     * x-ms-original-file: 2024-12-01-preview/LoadTests_Update.json
     */
    /**
     * Sample code: Update a LoadTestResource.
     * 
     * @param manager Entry point to LoadTestManager.
     */
    public static void updateALoadTestResource(com.azure.resourcemanager.loadtesting.LoadTestManager manager) {
        LoadTestResource resource = manager.resourceProviders()
            .getByResourceGroupWithResponse("dummyrg", "myLoadTest", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("Team", "Dev Exp", "Division", "LT"))
            .withIdentity(new ManagedServiceIdentity()
                .withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/dummyrg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id1",
                    new UserAssignedIdentity())))
            .withProperties(new LoadTestResourceUpdateProperties().withDescription("This is new load test resource")
                .withEncryption(new EncryptionProperties()
                    .withIdentity(new EncryptionPropertiesIdentity().withType(Type.SYSTEM_ASSIGNED))
                    .withKeyUrl("fakeTokenPlaceholder")))
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

