# Code snippets and samples


## LoadTests

- [CreateOrUpdate](#loadtests_createorupdate)
- [Delete](#loadtests_delete)
- [GetByResourceGroup](#loadtests_getbyresourcegroup)
- [List](#loadtests_list)
- [ListByResourceGroup](#loadtests_listbyresourcegroup)
- [ListOutboundNetworkDependenciesEndpoints](#loadtests_listoutboundnetworkdependenciesendpoints)
- [Update](#loadtests_update)

## Operations

- [List](#operations_list)

## Quotas

- [CheckAvailability](#quotas_checkavailability)
- [Get](#quotas_get)
- [List](#quotas_list)
### LoadTests_CreateOrUpdate

```java
import com.azure.resourcemanager.loadtesting.models.EncryptionProperties;
import com.azure.resourcemanager.loadtesting.models.EncryptionPropertiesIdentity;
import com.azure.resourcemanager.loadtesting.models.ManagedServiceIdentity;
import com.azure.resourcemanager.loadtesting.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.loadtesting.models.Type;
import com.azure.resourcemanager.loadtesting.models.UserAssignedIdentity;
import java.util.HashMap;
import java.util.Map;

/** Samples for LoadTests CreateOrUpdate. */
public final class LoadTestsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/loadtestservice/resource-manager/Microsoft.LoadTestService/stable/2022-12-01/examples/LoadTests_CreateOrUpdate.json
     */
    /**
     * Sample code: LoadTests_CreateOrUpdate.
     *
     * @param manager Entry point to LoadTestManager.
     */
    public static void loadTestsCreateOrUpdate(com.azure.resourcemanager.loadtesting.LoadTestManager manager) {
        manager
            .loadTests()
            .define("myLoadTest")
            .withRegion("westus")
            .withExistingResourceGroup("dummyrg")
            .withTags(mapOf("Team", "Dev Exp"))
            .withIdentity(
                new ManagedServiceIdentity()
                    .withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/dummyrg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id1",
                            new UserAssignedIdentity())))
            .withDescription("This is new load test resource")
            .withEncryption(
                new EncryptionProperties()
                    .withIdentity(
                        new EncryptionPropertiesIdentity()
                            .withType(Type.USER_ASSIGNED)
                            .withResourceId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/dummyrg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id1"))
                    .withKeyUrl("fakeTokenPlaceholder"))
            .create();
    }

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

### LoadTests_Delete

```java
/** Samples for LoadTests Delete. */
public final class LoadTestsDeleteSamples {
    /*
     * x-ms-original-file: specification/loadtestservice/resource-manager/Microsoft.LoadTestService/stable/2022-12-01/examples/LoadTests_Delete.json
     */
    /**
     * Sample code: LoadTests_Delete.
     *
     * @param manager Entry point to LoadTestManager.
     */
    public static void loadTestsDelete(com.azure.resourcemanager.loadtesting.LoadTestManager manager) {
        manager.loadTests().delete("dummyrg", "myLoadTest", com.azure.core.util.Context.NONE);
    }
}
```

### LoadTests_GetByResourceGroup

```java
/** Samples for LoadTests GetByResourceGroup. */
public final class LoadTestsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/loadtestservice/resource-manager/Microsoft.LoadTestService/stable/2022-12-01/examples/LoadTests_Get.json
     */
    /**
     * Sample code: LoadTests_Get.
     *
     * @param manager Entry point to LoadTestManager.
     */
    public static void loadTestsGet(com.azure.resourcemanager.loadtesting.LoadTestManager manager) {
        manager.loadTests().getByResourceGroupWithResponse("dummyrg", "myLoadTest", com.azure.core.util.Context.NONE);
    }
}
```

### LoadTests_List

```java
/** Samples for LoadTests List. */
public final class LoadTestsListSamples {
    /*
     * x-ms-original-file: specification/loadtestservice/resource-manager/Microsoft.LoadTestService/stable/2022-12-01/examples/LoadTests_ListBySubscription.json
     */
    /**
     * Sample code: LoadTests_ListBySubscription.
     *
     * @param manager Entry point to LoadTestManager.
     */
    public static void loadTestsListBySubscription(com.azure.resourcemanager.loadtesting.LoadTestManager manager) {
        manager.loadTests().list(com.azure.core.util.Context.NONE);
    }
}
```

### LoadTests_ListByResourceGroup

```java
/** Samples for LoadTests ListByResourceGroup. */
public final class LoadTestsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/loadtestservice/resource-manager/Microsoft.LoadTestService/stable/2022-12-01/examples/LoadTests_ListByResourceGroup.json
     */
    /**
     * Sample code: LoadTests_ListByResourceGroup.
     *
     * @param manager Entry point to LoadTestManager.
     */
    public static void loadTestsListByResourceGroup(com.azure.resourcemanager.loadtesting.LoadTestManager manager) {
        manager.loadTests().listByResourceGroup("dummyrg", com.azure.core.util.Context.NONE);
    }
}
```

### LoadTests_ListOutboundNetworkDependenciesEndpoints

```java
/** Samples for LoadTests ListOutboundNetworkDependenciesEndpoints. */
public final class LoadTestsListOutboundNetworkDependenciesEndpointsSamples {
    /*
     * x-ms-original-file: specification/loadtestservice/resource-manager/Microsoft.LoadTestService/stable/2022-12-01/examples/LoadTests_ListOutboundNetworkDependenciesEndpoints.json
     */
    /**
     * Sample code: ListOutboundNetworkDependencies.
     *
     * @param manager Entry point to LoadTestManager.
     */
    public static void listOutboundNetworkDependencies(com.azure.resourcemanager.loadtesting.LoadTestManager manager) {
        manager
            .loadTests()
            .listOutboundNetworkDependenciesEndpoints(
                "default-azureloadtest-japaneast", "sampleloadtest", com.azure.core.util.Context.NONE);
    }
}
```

### LoadTests_Update

```java
import com.azure.resourcemanager.loadtesting.models.EncryptionProperties;
import com.azure.resourcemanager.loadtesting.models.EncryptionPropertiesIdentity;
import com.azure.resourcemanager.loadtesting.models.LoadTestResource;
import com.azure.resourcemanager.loadtesting.models.ManagedServiceIdentity;
import com.azure.resourcemanager.loadtesting.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.loadtesting.models.Type;
import com.azure.resourcemanager.loadtesting.models.UserAssignedIdentity;
import java.util.HashMap;
import java.util.Map;

/** Samples for LoadTests Update. */
public final class LoadTestsUpdateSamples {
    /*
     * x-ms-original-file: specification/loadtestservice/resource-manager/Microsoft.LoadTestService/stable/2022-12-01/examples/LoadTests_Update.json
     */
    /**
     * Sample code: LoadTests_Update.
     *
     * @param manager Entry point to LoadTestManager.
     */
    public static void loadTestsUpdate(com.azure.resourcemanager.loadtesting.LoadTestManager manager) {
        LoadTestResource resource =
            manager
                .loadTests()
                .getByResourceGroupWithResponse("dummyrg", "myLoadTest", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("Division", "LT", "Team", "Dev Exp"))
            .withIdentity(
                new ManagedServiceIdentity()
                    .withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/dummyrg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id1",
                            new UserAssignedIdentity())))
            .withDescription("This is new load test resource")
            .withEncryption(
                new EncryptionProperties()
                    .withIdentity(new EncryptionPropertiesIdentity().withType(Type.SYSTEM_ASSIGNED))
                    .withKeyUrl("fakeTokenPlaceholder"))
            .apply();
    }

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

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/loadtestservice/resource-manager/Microsoft.LoadTestService/stable/2022-12-01/examples/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     *
     * @param manager Entry point to LoadTestManager.
     */
    public static void operationsList(com.azure.resourcemanager.loadtesting.LoadTestManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Quotas_CheckAvailability

```java
import com.azure.resourcemanager.loadtesting.models.QuotaBucketRequest;
import com.azure.resourcemanager.loadtesting.models.QuotaBucketRequestPropertiesDimensions;

/** Samples for Quotas CheckAvailability. */
public final class QuotasCheckAvailabilitySamples {
    /*
     * x-ms-original-file: specification/loadtestservice/resource-manager/Microsoft.LoadTestService/stable/2022-12-01/examples/Quotas_CheckAvailability.json
     */
    /**
     * Sample code: Quotas_CheckAvailability.
     *
     * @param manager Entry point to LoadTestManager.
     */
    public static void quotasCheckAvailability(com.azure.resourcemanager.loadtesting.LoadTestManager manager) {
        manager
            .quotas()
            .checkAvailabilityWithResponse(
                "westus",
                "testQuotaBucket",
                new QuotaBucketRequest()
                    .withCurrentUsage(20)
                    .withCurrentQuota(40)
                    .withNewQuota(50)
                    .withDimensions(
                        new QuotaBucketRequestPropertiesDimensions()
                            .withSubscriptionId("testsubscriptionId")
                            .withLocation("westus")),
                com.azure.core.util.Context.NONE);
    }
}
```

### Quotas_Get

```java
/** Samples for Quotas Get. */
public final class QuotasGetSamples {
    /*
     * x-ms-original-file: specification/loadtestservice/resource-manager/Microsoft.LoadTestService/stable/2022-12-01/examples/Quotas_Get.json
     */
    /**
     * Sample code: Quotas_Get.
     *
     * @param manager Entry point to LoadTestManager.
     */
    public static void quotasGet(com.azure.resourcemanager.loadtesting.LoadTestManager manager) {
        manager.quotas().getWithResponse("westus", "testQuotaBucket", com.azure.core.util.Context.NONE);
    }
}
```

### Quotas_List

```java
/** Samples for Quotas List. */
public final class QuotasListSamples {
    /*
     * x-ms-original-file: specification/loadtestservice/resource-manager/Microsoft.LoadTestService/stable/2022-12-01/examples/Quotas_List.json
     */
    /**
     * Sample code: Quotas_List.
     *
     * @param manager Entry point to LoadTestManager.
     */
    public static void quotasList(com.azure.resourcemanager.loadtesting.LoadTestManager manager) {
        manager.quotas().list("westus", com.azure.core.util.Context.NONE);
    }
}
```

