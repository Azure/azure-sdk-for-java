# Code snippets and samples


## Accounts

- [CheckNameAvailability](#accounts_checknameavailability)
- [CreateOrUpdate](#accounts_createorupdate)
- [Delete](#accounts_delete)
- [GetByResourceGroup](#accounts_getbyresourcegroup)
- [GetStatus](#accounts_getstatus)
- [List](#accounts_list)
- [ListByResourceGroup](#accounts_listbyresourcegroup)
- [Update](#accounts_update)

## Modeling

- [CreateOrUpdate](#modeling_createorupdate)
- [Delete](#modeling_delete)
- [Get](#modeling_get)
- [ListByAccountResource](#modeling_listbyaccountresource)
- [Update](#modeling_update)

## OperationStatuses

- [Get](#operationstatuses_get)

## Operations

- [List](#operations_list)

## ServiceEndpoints

- [CreateOrUpdate](#serviceendpoints_createorupdate)
- [Delete](#serviceendpoints_delete)
- [Get](#serviceendpoints_get)
- [ListByAccountResource](#serviceendpoints_listbyaccountresource)
- [Update](#serviceendpoints_update)
### Accounts_CheckNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.recommendationsservice.models.CheckNameAvailabilityRequest;

/** Samples for Accounts CheckNameAvailability. */
public final class AccountsCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/recommendationsservice/resource-manager/Microsoft.RecommendationsService/preview/2022-03-01-preview/examples/Accounts_CheckNameAvailabilityAvailable.json
     */
    /**
     * Sample code: Check name availability of RecommendationsService Account - name available.
     *
     * @param manager Entry point to RecommendationsServiceManager.
     */
    public static void checkNameAvailabilityOfRecommendationsServiceAccountNameAvailable(
        com.azure.resourcemanager.recommendationsservice.RecommendationsServiceManager manager) {
        manager
            .accounts()
            .checkNameAvailabilityWithResponse(
                new CheckNameAvailabilityRequest()
                    .withName("sampleAccount")
                    .withType("Microsoft.RecommendationsService/accounts"),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/recommendationsservice/resource-manager/Microsoft.RecommendationsService/preview/2022-03-01-preview/examples/Accounts_CheckNameAvailabilityUnavailable.json
     */
    /**
     * Sample code: Check name availability of RecommendationsService Account - name unavailable.
     *
     * @param manager Entry point to RecommendationsServiceManager.
     */
    public static void checkNameAvailabilityOfRecommendationsServiceAccountNameUnavailable(
        com.azure.resourcemanager.recommendationsservice.RecommendationsServiceManager manager) {
        manager
            .accounts()
            .checkNameAvailabilityWithResponse(
                new CheckNameAvailabilityRequest()
                    .withName("sampleAccount")
                    .withType("Microsoft.RecommendationsService/accounts"),
                Context.NONE);
    }
}
```

### Accounts_CreateOrUpdate

```java
import com.azure.resourcemanager.recommendationsservice.models.AccountConfiguration;
import com.azure.resourcemanager.recommendationsservice.models.AccountResourceProperties;
import com.azure.resourcemanager.recommendationsservice.models.EndpointAuthentication;
import com.azure.resourcemanager.recommendationsservice.models.ManagedServiceIdentity;
import com.azure.resourcemanager.recommendationsservice.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.recommendationsservice.models.PrincipalType;
import com.azure.resourcemanager.recommendationsservice.models.UserAssignedIdentity;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Accounts CreateOrUpdate. */
public final class AccountsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/recommendationsservice/resource-manager/Microsoft.RecommendationsService/preview/2022-03-01-preview/examples/Accounts_CreateOrUpdate.json
     */
    /**
     * Sample code: Create or update RecommendationsService Account resource.
     *
     * @param manager Entry point to RecommendationsServiceManager.
     */
    public static void createOrUpdateRecommendationsServiceAccountResource(
        com.azure.resourcemanager.recommendationsservice.RecommendationsServiceManager manager) {
        manager
            .accounts()
            .define("sampleAccount")
            .withRegion("West US")
            .withExistingResourceGroup("rg")
            .withTags(mapOf("Environment", "Prod"))
            .withProperties(
                new AccountResourceProperties()
                    .withConfiguration(AccountConfiguration.CAPACITY)
                    .withEndpointAuthentications(
                        Arrays
                            .asList(
                                new EndpointAuthentication()
                                    .withAadTenantId("tenant")
                                    .withPrincipalId("oid")
                                    .withPrincipalType(PrincipalType.USER))))
            .create();
    }

    /*
     * x-ms-original-file: specification/recommendationsservice/resource-manager/Microsoft.RecommendationsService/preview/2022-03-01-preview/examples/Accounts_CreateOrUpdateWithManagedIdentity.json
     */
    /**
     * Sample code: Create or update RecommendationsService Account resource with managed identity.
     *
     * @param manager Entry point to RecommendationsServiceManager.
     */
    public static void createOrUpdateRecommendationsServiceAccountResourceWithManagedIdentity(
        com.azure.resourcemanager.recommendationsservice.RecommendationsServiceManager manager) {
        manager
            .accounts()
            .define("sampleAccount")
            .withRegion("West US")
            .withExistingResourceGroup("rg")
            .withTags(mapOf("Environment", "Prod"))
            .withProperties(
                new AccountResourceProperties()
                    .withConfiguration(AccountConfiguration.CAPACITY)
                    .withEndpointAuthentications(
                        Arrays
                            .asList(
                                new EndpointAuthentication()
                                    .withAadTenantId("tenant")
                                    .withPrincipalId("oid")
                                    .withPrincipalType(PrincipalType.USER))))
            .withIdentity(
                new ManagedServiceIdentity()
                    .withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx/resourceGroups/rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/userAssignedIdentity",
                            new UserAssignedIdentity())))
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

### Accounts_Delete

```java
import com.azure.core.util.Context;

/** Samples for Accounts Delete. */
public final class AccountsDeleteSamples {
    /*
     * x-ms-original-file: specification/recommendationsservice/resource-manager/Microsoft.RecommendationsService/preview/2022-03-01-preview/examples/Accounts_Delete.json
     */
    /**
     * Sample code: Delete RecommendationsService Account resource.
     *
     * @param manager Entry point to RecommendationsServiceManager.
     */
    public static void deleteRecommendationsServiceAccountResource(
        com.azure.resourcemanager.recommendationsservice.RecommendationsServiceManager manager) {
        manager.accounts().delete("rg", "sampleAccount", Context.NONE);
    }
}
```

### Accounts_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Accounts GetByResourceGroup. */
public final class AccountsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/recommendationsservice/resource-manager/Microsoft.RecommendationsService/preview/2022-03-01-preview/examples/Accounts_Get.json
     */
    /**
     * Sample code: Get RecommendationsService Account resource.
     *
     * @param manager Entry point to RecommendationsServiceManager.
     */
    public static void getRecommendationsServiceAccountResource(
        com.azure.resourcemanager.recommendationsservice.RecommendationsServiceManager manager) {
        manager.accounts().getByResourceGroupWithResponse("rg", "sampleAccount", Context.NONE);
    }
}
```

### Accounts_GetStatus

```java
import com.azure.core.util.Context;

/** Samples for Accounts GetStatus. */
public final class AccountsGetStatusSamples {
    /*
     * x-ms-original-file: specification/recommendationsservice/resource-manager/Microsoft.RecommendationsService/preview/2022-03-01-preview/examples/Accounts_GetStatus_ModelingExists.json
     */
    /**
     * Sample code: Get RecommendationsService Account status - Modeling resource exists.
     *
     * @param manager Entry point to RecommendationsServiceManager.
     */
    public static void getRecommendationsServiceAccountStatusModelingResourceExists(
        com.azure.resourcemanager.recommendationsservice.RecommendationsServiceManager manager) {
        manager.accounts().getStatusWithResponse("rg", "sampleAccount", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/recommendationsservice/resource-manager/Microsoft.RecommendationsService/preview/2022-03-01-preview/examples/Accounts_GetStatus_NoModelingExists.json
     */
    /**
     * Sample code: Get account status - no Modeling resource exists.
     *
     * @param manager Entry point to RecommendationsServiceManager.
     */
    public static void getAccountStatusNoModelingResourceExists(
        com.azure.resourcemanager.recommendationsservice.RecommendationsServiceManager manager) {
        manager.accounts().getStatusWithResponse("rg", "sampleAccount", Context.NONE);
    }
}
```

### Accounts_List

```java
import com.azure.core.util.Context;

/** Samples for Accounts List. */
public final class AccountsListSamples {
    /*
     * x-ms-original-file: specification/recommendationsservice/resource-manager/Microsoft.RecommendationsService/preview/2022-03-01-preview/examples/Accounts_ListBySubscription.json
     */
    /**
     * Sample code: Get RecommendationsService Account resources by subscription.
     *
     * @param manager Entry point to RecommendationsServiceManager.
     */
    public static void getRecommendationsServiceAccountResourcesBySubscription(
        com.azure.resourcemanager.recommendationsservice.RecommendationsServiceManager manager) {
        manager.accounts().list(Context.NONE);
    }
}
```

### Accounts_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Accounts ListByResourceGroup. */
public final class AccountsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/recommendationsservice/resource-manager/Microsoft.RecommendationsService/preview/2022-03-01-preview/examples/Accounts_ListByResourceGroup.json
     */
    /**
     * Sample code: Get RecommendationsService Account resources by resource group.
     *
     * @param manager Entry point to RecommendationsServiceManager.
     */
    public static void getRecommendationsServiceAccountResourcesByResourceGroup(
        com.azure.resourcemanager.recommendationsservice.RecommendationsServiceManager manager) {
        manager.accounts().listByResourceGroup("rg", Context.NONE);
    }
}
```

### Accounts_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.recommendationsservice.models.AccountPatchResourceProperties;
import com.azure.resourcemanager.recommendationsservice.models.AccountResource;
import com.azure.resourcemanager.recommendationsservice.models.CorsRule;
import com.azure.resourcemanager.recommendationsservice.models.EndpointAuthentication;
import com.azure.resourcemanager.recommendationsservice.models.ManagedServiceIdentity;
import com.azure.resourcemanager.recommendationsservice.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.recommendationsservice.models.PrincipalType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Accounts Update. */
public final class AccountsUpdateSamples {
    /*
     * x-ms-original-file: specification/recommendationsservice/resource-manager/Microsoft.RecommendationsService/preview/2022-03-01-preview/examples/Accounts_Update.json
     */
    /**
     * Sample code: Update existing RecommendationsService Account resource.
     *
     * @param manager Entry point to RecommendationsServiceManager.
     */
    public static void updateExistingRecommendationsServiceAccountResource(
        com.azure.resourcemanager.recommendationsservice.RecommendationsServiceManager manager) {
        AccountResource resource =
            manager.accounts().getByResourceGroupWithResponse("rg", "sampleAccount", Context.NONE).getValue();
        resource
            .update()
            .withTags(mapOf("Environment", "Test"))
            .withProperties(
                new AccountPatchResourceProperties()
                    .withEndpointAuthentications(
                        Arrays
                            .asList(
                                new EndpointAuthentication()
                                    .withAadTenantId("tenant")
                                    .withPrincipalId("oid")
                                    .withPrincipalType(PrincipalType.USER),
                                new EndpointAuthentication()
                                    .withAadTenantId("tenant")
                                    .withPrincipalId("oid2")
                                    .withPrincipalType(PrincipalType.USER)))
                    .withCors(
                        Arrays
                            .asList(
                                new CorsRule()
                                    .withAllowedOrigins(Arrays.asList("http://siteA.com"))
                                    .withAllowedMethods(Arrays.asList("GET", "PUT", "POST"))
                                    .withAllowedHeaders(Arrays.asList("X-Custom-Header", "X-Custom-Header2"))))
                    .withReportsConnectionString("https://storageAccount.blob.core.windows.net/container/root"))
            .apply();
    }

    /*
     * x-ms-original-file: specification/recommendationsservice/resource-manager/Microsoft.RecommendationsService/preview/2022-03-01-preview/examples/Accounts_UpdateWithManagedIdentity.json
     */
    /**
     * Sample code: Update existing RecommendationsService Account resource with managed identity.
     *
     * @param manager Entry point to RecommendationsServiceManager.
     */
    public static void updateExistingRecommendationsServiceAccountResourceWithManagedIdentity(
        com.azure.resourcemanager.recommendationsservice.RecommendationsServiceManager manager) {
        AccountResource resource =
            manager.accounts().getByResourceGroupWithResponse("rg", "sampleAccount", Context.NONE).getValue();
        resource
            .update()
            .withTags(mapOf("Environment", "Test"))
            .withProperties(
                new AccountPatchResourceProperties()
                    .withEndpointAuthentications(
                        Arrays
                            .asList(
                                new EndpointAuthentication()
                                    .withAadTenantId("tenant")
                                    .withPrincipalId("oid")
                                    .withPrincipalType(PrincipalType.USER),
                                new EndpointAuthentication()
                                    .withAadTenantId("tenant")
                                    .withPrincipalId("oid2")
                                    .withPrincipalType(PrincipalType.USER)))
                    .withCors(
                        Arrays
                            .asList(
                                new CorsRule()
                                    .withAllowedOrigins(Arrays.asList("http://siteA.com"))
                                    .withAllowedMethods(Arrays.asList("GET", "PUT", "POST"))
                                    .withAllowedHeaders(Arrays.asList("X-Custom-Header", "X-Custom-Header2"))))
                    .withReportsConnectionString("https://storageAccount.blob.core.windows.net/container/root"))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED))
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

### Modeling_CreateOrUpdate

```java
import com.azure.resourcemanager.recommendationsservice.models.ModelingFeatures;
import com.azure.resourcemanager.recommendationsservice.models.ModelingFrequency;
import com.azure.resourcemanager.recommendationsservice.models.ModelingInputData;
import com.azure.resourcemanager.recommendationsservice.models.ModelingResourceProperties;
import com.azure.resourcemanager.recommendationsservice.models.ModelingSize;
import java.util.HashMap;
import java.util.Map;

/** Samples for Modeling CreateOrUpdate. */
public final class ModelingCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/recommendationsservice/resource-manager/Microsoft.RecommendationsService/preview/2022-03-01-preview/examples/Modeling_CreateOrUpdate.json
     */
    /**
     * Sample code: Create or update Modeling resource.
     *
     * @param manager Entry point to RecommendationsServiceManager.
     */
    public static void createOrUpdateModelingResource(
        com.azure.resourcemanager.recommendationsservice.RecommendationsServiceManager manager) {
        manager
            .modelings()
            .define("c1")
            .withRegion("West US")
            .withExistingAccount("rg", "sampleAccount")
            .withTags(mapOf("Environment", "Prod"))
            .withProperties(
                new ModelingResourceProperties()
                    .withFeatures(ModelingFeatures.STANDARD)
                    .withFrequency(ModelingFrequency.HIGH)
                    .withSize(ModelingSize.MEDIUM)
                    .withInputData(
                        new ModelingInputData()
                            .withConnectionString("https://storageAccount.blob.core.windows.net/container/root")))
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

### Modeling_Delete

```java
import com.azure.core.util.Context;

/** Samples for Modeling Delete. */
public final class ModelingDeleteSamples {
    /*
     * x-ms-original-file: specification/recommendationsservice/resource-manager/Microsoft.RecommendationsService/preview/2022-03-01-preview/examples/Modeling_Delete.json
     */
    /**
     * Sample code: Delete Modeling resource.
     *
     * @param manager Entry point to RecommendationsServiceManager.
     */
    public static void deleteModelingResource(
        com.azure.resourcemanager.recommendationsservice.RecommendationsServiceManager manager) {
        manager.modelings().delete("rg", "sampleAccount", "c1", Context.NONE);
    }
}
```

### Modeling_Get

```java
import com.azure.core.util.Context;

/** Samples for Modeling Get. */
public final class ModelingGetSamples {
    /*
     * x-ms-original-file: specification/recommendationsservice/resource-manager/Microsoft.RecommendationsService/preview/2022-03-01-preview/examples/Modeling_Get.json
     */
    /**
     * Sample code: Get Modeling resource.
     *
     * @param manager Entry point to RecommendationsServiceManager.
     */
    public static void getModelingResource(
        com.azure.resourcemanager.recommendationsservice.RecommendationsServiceManager manager) {
        manager.modelings().getWithResponse("rg", "sampleAccount", "c1", Context.NONE);
    }
}
```

### Modeling_ListByAccountResource

```java
import com.azure.core.util.Context;

/** Samples for Modeling ListByAccountResource. */
public final class ModelingListByAccountResourceSamples {
    /*
     * x-ms-original-file: specification/recommendationsservice/resource-manager/Microsoft.RecommendationsService/preview/2022-03-01-preview/examples/Modeling_ListByAccountResource.json
     */
    /**
     * Sample code: Get Modeling resources by RecommendationsService Account resource.
     *
     * @param manager Entry point to RecommendationsServiceManager.
     */
    public static void getModelingResourcesByRecommendationsServiceAccountResource(
        com.azure.resourcemanager.recommendationsservice.RecommendationsServiceManager manager) {
        manager.modelings().listByAccountResource("rg", "sampleAccount", Context.NONE);
    }
}
```

### Modeling_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.recommendationsservice.models.ModelingInputData;
import com.azure.resourcemanager.recommendationsservice.models.ModelingPatchResourceProperties;
import com.azure.resourcemanager.recommendationsservice.models.ModelingResource;
import java.util.HashMap;
import java.util.Map;

/** Samples for Modeling Update. */
public final class ModelingUpdateSamples {
    /*
     * x-ms-original-file: specification/recommendationsservice/resource-manager/Microsoft.RecommendationsService/preview/2022-03-01-preview/examples/Modeling_Update.json
     */
    /**
     * Sample code: Update Modeling resource.
     *
     * @param manager Entry point to RecommendationsServiceManager.
     */
    public static void updateModelingResource(
        com.azure.resourcemanager.recommendationsservice.RecommendationsServiceManager manager) {
        ModelingResource resource =
            manager.modelings().getWithResponse("rg", "sampleAccount", "c1", Context.NONE).getValue();
        resource
            .update()
            .withTags(mapOf("Environment", "Test"))
            .withProperties(
                new ModelingPatchResourceProperties()
                    .withInputData(
                        new ModelingInputData()
                            .withConnectionString("https://storageAccount.blob.core.windows.net/container/root")))
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

### OperationStatuses_Get

```java
import com.azure.core.util.Context;

/** Samples for OperationStatuses Get. */
public final class OperationStatusesGetSamples {
    /*
     * x-ms-original-file: specification/recommendationsservice/resource-manager/Microsoft.RecommendationsService/preview/2022-03-01-preview/examples/OperationStatuses_Get.json
     */
    /**
     * Sample code: Get operation status.
     *
     * @param manager Entry point to RecommendationsServiceManager.
     */
    public static void getOperationStatus(
        com.azure.resourcemanager.recommendationsservice.RecommendationsServiceManager manager) {
        manager
            .operationStatuses()
            .getWithResponse("testLocation", "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx", Context.NONE);
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/recommendationsservice/resource-manager/Microsoft.RecommendationsService/preview/2022-03-01-preview/examples/Operations_List.json
     */
    /**
     * Sample code: Get all RecommendationsService operations.
     *
     * @param manager Entry point to RecommendationsServiceManager.
     */
    public static void getAllRecommendationsServiceOperations(
        com.azure.resourcemanager.recommendationsservice.RecommendationsServiceManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### ServiceEndpoints_CreateOrUpdate

```java
import com.azure.resourcemanager.recommendationsservice.models.ServiceEndpointResourceProperties;
import java.util.HashMap;
import java.util.Map;

/** Samples for ServiceEndpoints CreateOrUpdate. */
public final class ServiceEndpointsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/recommendationsservice/resource-manager/Microsoft.RecommendationsService/preview/2022-03-01-preview/examples/ServiceEndpoints_CreateOrUpdate.json
     */
    /**
     * Sample code: Create or update ServiceEndpoint resource.
     *
     * @param manager Entry point to RecommendationsServiceManager.
     */
    public static void createOrUpdateServiceEndpointResource(
        com.azure.resourcemanager.recommendationsservice.RecommendationsServiceManager manager) {
        manager
            .serviceEndpoints()
            .define("s1")
            .withRegion("West US")
            .withExistingAccount("rg", "sampleAccount")
            .withTags(mapOf("Environment", "Prod"))
            .withProperties(new ServiceEndpointResourceProperties().withPreAllocatedCapacity(100))
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

### ServiceEndpoints_Delete

```java
import com.azure.core.util.Context;

/** Samples for ServiceEndpoints Delete. */
public final class ServiceEndpointsDeleteSamples {
    /*
     * x-ms-original-file: specification/recommendationsservice/resource-manager/Microsoft.RecommendationsService/preview/2022-03-01-preview/examples/ServiceEndpoints_Delete.json
     */
    /**
     * Sample code: Delete ServiceEndpoint resource.
     *
     * @param manager Entry point to RecommendationsServiceManager.
     */
    public static void deleteServiceEndpointResource(
        com.azure.resourcemanager.recommendationsservice.RecommendationsServiceManager manager) {
        manager.serviceEndpoints().delete("rg", "sampleAccount", "s1", Context.NONE);
    }
}
```

### ServiceEndpoints_Get

```java
import com.azure.core.util.Context;

/** Samples for ServiceEndpoints Get. */
public final class ServiceEndpointsGetSamples {
    /*
     * x-ms-original-file: specification/recommendationsservice/resource-manager/Microsoft.RecommendationsService/preview/2022-03-01-preview/examples/ServiceEndpoints_Get.json
     */
    /**
     * Sample code: Get ServiceEndpoint resource.
     *
     * @param manager Entry point to RecommendationsServiceManager.
     */
    public static void getServiceEndpointResource(
        com.azure.resourcemanager.recommendationsservice.RecommendationsServiceManager manager) {
        manager.serviceEndpoints().getWithResponse("rg", "sampleAccount", "s1", Context.NONE);
    }
}
```

### ServiceEndpoints_ListByAccountResource

```java
import com.azure.core.util.Context;

/** Samples for ServiceEndpoints ListByAccountResource. */
public final class ServiceEndpointsListByAccountResourceSamples {
    /*
     * x-ms-original-file: specification/recommendationsservice/resource-manager/Microsoft.RecommendationsService/preview/2022-03-01-preview/examples/ServiceEndpoints_ListByAccountResource.json
     */
    /**
     * Sample code: Get ServiceEndpoint resources by RecommendationsService Account resource.
     *
     * @param manager Entry point to RecommendationsServiceManager.
     */
    public static void getServiceEndpointResourcesByRecommendationsServiceAccountResource(
        com.azure.resourcemanager.recommendationsservice.RecommendationsServiceManager manager) {
        manager.serviceEndpoints().listByAccountResource("rg", "sampleAccount", Context.NONE);
    }
}
```

### ServiceEndpoints_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.recommendationsservice.models.ServiceEndpointResource;
import java.util.HashMap;
import java.util.Map;

/** Samples for ServiceEndpoints Update. */
public final class ServiceEndpointsUpdateSamples {
    /*
     * x-ms-original-file: specification/recommendationsservice/resource-manager/Microsoft.RecommendationsService/preview/2022-03-01-preview/examples/ServiceEndpoints_Update.json
     */
    /**
     * Sample code: Update ServiceEndpoint resource.
     *
     * @param manager Entry point to RecommendationsServiceManager.
     */
    public static void updateServiceEndpointResource(
        com.azure.resourcemanager.recommendationsservice.RecommendationsServiceManager manager) {
        ServiceEndpointResource resource =
            manager.serviceEndpoints().getWithResponse("rg", "sampleAccount", "s1", Context.NONE).getValue();
        resource.update().withTags(mapOf("Environment", "Prod")).apply();
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

