# Code snippets and samples


## ApplicationDefinitions

- [CreateOrUpdate](#applicationdefinitions_createorupdate)
- [Delete](#applicationdefinitions_delete)
- [GetByResourceGroup](#applicationdefinitions_getbyresourcegroup)
- [List](#applicationdefinitions_list)
- [ListByResourceGroup](#applicationdefinitions_listbyresourcegroup)
- [Update](#applicationdefinitions_update)

## Applications

- [CreateOrUpdate](#applications_createorupdate)
- [Delete](#applications_delete)
- [GetByResourceGroup](#applications_getbyresourcegroup)
- [List](#applications_list)
- [ListAllowedUpgradePlans](#applications_listallowedupgradeplans)
- [ListByResourceGroup](#applications_listbyresourcegroup)
- [RefreshPermissions](#applications_refreshpermissions)
- [Update](#applications_update)

## JitRequests

- [CreateOrUpdate](#jitrequests_createorupdate)
- [Delete](#jitrequests_delete)
- [GetByResourceGroup](#jitrequests_getbyresourcegroup)
- [ListByResourceGroup](#jitrequests_listbyresourcegroup)
- [ListBySubscription](#jitrequests_listbysubscription)
- [Update](#jitrequests_update)

## ResourceProvider

- [ListOperations](#resourceprovider_listoperations)
### ApplicationDefinitions_CreateOrUpdate

```java
import com.azure.resourcemanager.solutions.models.ApplicationAuthorization;
import com.azure.resourcemanager.solutions.models.ApplicationLockLevel;
import java.util.Arrays;

/** Samples for ApplicationDefinitions CreateOrUpdate. */
public final class ApplicationDefinitionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/solutions/resource-manager/Microsoft.Solutions/stable/2021-07-01/examples/createOrUpdateApplicationDefinition.json
     */
    /**
     * Sample code: Create or update managed application definition.
     *
     * @param manager Entry point to ManagedApplicationManager.
     */
    public static void createOrUpdateManagedApplicationDefinition(
        com.azure.resourcemanager.solutions.ManagedApplicationManager manager) {
        manager
            .applicationDefinitions()
            .define("myManagedApplicationDef")
            .withRegion((String) null)
            .withExistingResourceGroup("rg")
            .withLockLevel(ApplicationLockLevel.NONE)
            .withDisplayName("myManagedApplicationDef")
            .withAuthorizations(
                Arrays
                    .asList(
                        new ApplicationAuthorization()
                            .withPrincipalId("validprincipalguid")
                            .withRoleDefinitionId("validroleguid")))
            .withDescription("myManagedApplicationDef description")
            .withPackageFileUri("https://path/to/packagezipfile")
            .create();
    }
}
```

### ApplicationDefinitions_Delete

```java
import com.azure.core.util.Context;

/** Samples for ApplicationDefinitions Delete. */
public final class ApplicationDefinitionsDeleteSamples {
    /*
     * x-ms-original-file: specification/solutions/resource-manager/Microsoft.Solutions/stable/2021-07-01/examples/deleteApplicationDefinition.json
     */
    /**
     * Sample code: delete managed application definition.
     *
     * @param manager Entry point to ManagedApplicationManager.
     */
    public static void deleteManagedApplicationDefinition(
        com.azure.resourcemanager.solutions.ManagedApplicationManager manager) {
        manager.applicationDefinitions().delete("rg", "myManagedApplicationDef", Context.NONE);
    }
}
```

### ApplicationDefinitions_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for ApplicationDefinitions GetByResourceGroup. */
public final class ApplicationDefinitionsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/solutions/resource-manager/Microsoft.Solutions/stable/2021-07-01/examples/getApplicationDefinition.json
     */
    /**
     * Sample code: Get managed application definition.
     *
     * @param manager Entry point to ManagedApplicationManager.
     */
    public static void getManagedApplicationDefinition(
        com.azure.resourcemanager.solutions.ManagedApplicationManager manager) {
        manager.applicationDefinitions().getByResourceGroupWithResponse("rg", "myManagedApplicationDef", Context.NONE);
    }
}
```

### ApplicationDefinitions_List

```java
import com.azure.core.util.Context;

/** Samples for ApplicationDefinitions List. */
public final class ApplicationDefinitionsListSamples {
    /*
     * x-ms-original-file: specification/solutions/resource-manager/Microsoft.Solutions/stable/2021-07-01/examples/listApplicationDefinitionsByResourceGroup.json
     */
    /**
     * Sample code: List managed application definitions.
     *
     * @param manager Entry point to ManagedApplicationManager.
     */
    public static void listManagedApplicationDefinitions(
        com.azure.resourcemanager.solutions.ManagedApplicationManager manager) {
        manager.applicationDefinitions().list(Context.NONE);
    }
}
```

### ApplicationDefinitions_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for ApplicationDefinitions ListByResourceGroup. */
public final class ApplicationDefinitionsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/solutions/resource-manager/Microsoft.Solutions/stable/2021-07-01/examples/listApplicationDefinitionsByResourceGroup.json
     */
    /**
     * Sample code: List managed application definitions.
     *
     * @param manager Entry point to ManagedApplicationManager.
     */
    public static void listManagedApplicationDefinitions(
        com.azure.resourcemanager.solutions.ManagedApplicationManager manager) {
        manager.applicationDefinitions().listByResourceGroup("rg", Context.NONE);
    }
}
```

### ApplicationDefinitions_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.solutions.models.ApplicationDefinition;
import java.util.HashMap;
import java.util.Map;

/** Samples for ApplicationDefinitions Update. */
public final class ApplicationDefinitionsUpdateSamples {
    /*
     * x-ms-original-file: specification/solutions/resource-manager/Microsoft.Solutions/stable/2021-07-01/examples/updateApplicationDefinition.json
     */
    /**
     * Sample code: Update managed application definition.
     *
     * @param manager Entry point to ManagedApplicationManager.
     */
    public static void updateManagedApplicationDefinition(
        com.azure.resourcemanager.solutions.ManagedApplicationManager manager) {
        ApplicationDefinition resource =
            manager
                .applicationDefinitions()
                .getByResourceGroupWithResponse("rg", "myManagedApplicationDef", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("department", "Finance")).apply();
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

### Applications_CreateOrUpdate

```java
/** Samples for Applications CreateOrUpdate. */
public final class ApplicationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/solutions/resource-manager/Microsoft.Solutions/stable/2021-07-01/examples/createOrUpdateApplication.json
     */
    /**
     * Sample code: Create or update managed application.
     *
     * @param manager Entry point to ManagedApplicationManager.
     */
    public static void createOrUpdateManagedApplication(
        com.azure.resourcemanager.solutions.ManagedApplicationManager manager) {
        manager
            .applications()
            .define("myManagedApplication")
            .withRegion((String) null)
            .withExistingResourceGroup("rg")
            .withKind("ServiceCatalog")
            .withManagedResourceGroupId("/subscriptions/subid/resourceGroups/myManagedRG")
            .withApplicationDefinitionId(
                "/subscriptions/subid/resourceGroups/rg/providers/Microsoft.Solutions/applicationDefinitions/myAppDef")
            .create();
    }
}
```

### Applications_Delete

```java
import com.azure.core.util.Context;

/** Samples for Applications Delete. */
public final class ApplicationsDeleteSamples {
    /*
     * x-ms-original-file: specification/solutions/resource-manager/Microsoft.Solutions/stable/2021-07-01/examples/deleteApplication.json
     */
    /**
     * Sample code: Delete managed application.
     *
     * @param manager Entry point to ManagedApplicationManager.
     */
    public static void deleteManagedApplication(com.azure.resourcemanager.solutions.ManagedApplicationManager manager) {
        manager.applications().delete("rg", "myManagedApplication", Context.NONE);
    }
}
```

### Applications_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Applications GetByResourceGroup. */
public final class ApplicationsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/solutions/resource-manager/Microsoft.Solutions/stable/2021-07-01/examples/getApplication.json
     */
    /**
     * Sample code: Get a managed application.
     *
     * @param manager Entry point to ManagedApplicationManager.
     */
    public static void getAManagedApplication(com.azure.resourcemanager.solutions.ManagedApplicationManager manager) {
        manager.applications().getByResourceGroupWithResponse("rg", "myManagedApplication", Context.NONE);
    }
}
```

### Applications_List

```java
import com.azure.core.util.Context;

/** Samples for Applications List. */
public final class ApplicationsListSamples {
    /*
     * x-ms-original-file: specification/solutions/resource-manager/Microsoft.Solutions/stable/2021-07-01/examples/listApplicationsByResourceGroup.json
     */
    /**
     * Sample code: Lists applications.
     *
     * @param manager Entry point to ManagedApplicationManager.
     */
    public static void listsApplications(com.azure.resourcemanager.solutions.ManagedApplicationManager manager) {
        manager.applications().list(Context.NONE);
    }
}
```

### Applications_ListAllowedUpgradePlans

```java
import com.azure.core.util.Context;

/** Samples for Applications ListAllowedUpgradePlans. */
public final class ApplicationsListAllowedUpgradePlansSamples {
    /*
     * x-ms-original-file: specification/solutions/resource-manager/Microsoft.Solutions/stable/2021-07-01/examples/listAllowedUpgradePlans.json
     */
    /**
     * Sample code: List allowed upgrade plans for application.
     *
     * @param manager Entry point to ManagedApplicationManager.
     */
    public static void listAllowedUpgradePlansForApplication(
        com.azure.resourcemanager.solutions.ManagedApplicationManager manager) {
        manager.applications().listAllowedUpgradePlansWithResponse("rg", "myManagedApplication", Context.NONE);
    }
}
```

### Applications_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Applications ListByResourceGroup. */
public final class ApplicationsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/solutions/resource-manager/Microsoft.Solutions/stable/2021-07-01/examples/listApplicationsByResourceGroup.json
     */
    /**
     * Sample code: Lists applications.
     *
     * @param manager Entry point to ManagedApplicationManager.
     */
    public static void listsApplications(com.azure.resourcemanager.solutions.ManagedApplicationManager manager) {
        manager.applications().listByResourceGroup("rg", Context.NONE);
    }
}
```

### Applications_RefreshPermissions

```java
import com.azure.core.util.Context;

/** Samples for Applications RefreshPermissions. */
public final class ApplicationsRefreshPermissionsSamples {
    /*
     * x-ms-original-file: specification/solutions/resource-manager/Microsoft.Solutions/stable/2021-07-01/examples/refreshApplicationPermissions.json
     */
    /**
     * Sample code: Refresh managed application permissions.
     *
     * @param manager Entry point to ManagedApplicationManager.
     */
    public static void refreshManagedApplicationPermissions(
        com.azure.resourcemanager.solutions.ManagedApplicationManager manager) {
        manager.applications().refreshPermissions("rg", "myManagedApplication", Context.NONE);
    }
}
```

### Applications_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.solutions.models.Application;

/** Samples for Applications Update. */
public final class ApplicationsUpdateSamples {
    /*
     * x-ms-original-file: specification/solutions/resource-manager/Microsoft.Solutions/stable/2021-07-01/examples/updateApplication.json
     */
    /**
     * Sample code: Create or update managed application.
     *
     * @param manager Entry point to ManagedApplicationManager.
     */
    public static void createOrUpdateManagedApplication(
        com.azure.resourcemanager.solutions.ManagedApplicationManager manager) {
        Application resource =
            manager
                .applications()
                .getByResourceGroupWithResponse("rg", "myManagedApplication", Context.NONE)
                .getValue();
        resource
            .update()
            .withKind("ServiceCatalog")
            .withManagedResourceGroupId("/subscriptions/subid/resourceGroups/myManagedRG")
            .withApplicationDefinitionId(
                "/subscriptions/subid/resourceGroups/rg/providers/Microsoft.Solutions/applicationDefinitions/myAppDef")
            .apply();
    }
}
```

### JitRequests_CreateOrUpdate

```java
import com.azure.resourcemanager.solutions.models.JitAuthorizationPolicies;
import com.azure.resourcemanager.solutions.models.JitSchedulingPolicy;
import com.azure.resourcemanager.solutions.models.JitSchedulingType;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;

/** Samples for JitRequests CreateOrUpdate. */
public final class JitRequestsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/solutions/resource-manager/Microsoft.Solutions/stable/2021-07-01/examples/createOrUpdateJitRequest.json
     */
    /**
     * Sample code: Create or update jit request.
     *
     * @param manager Entry point to ManagedApplicationManager.
     */
    public static void createOrUpdateJitRequest(com.azure.resourcemanager.solutions.ManagedApplicationManager manager) {
        manager
            .jitRequests()
            .define("myJitRequest")
            .withRegion((String) null)
            .withExistingResourceGroup("rg")
            .withApplicationResourceId(
                "/subscriptions/00c76877-e316-48a7-af60-4a09fec9d43f/resourceGroups/52F30DB2/providers/Microsoft.Solutions/applications/7E193158")
            .withJitAuthorizationPolicies(
                Arrays
                    .asList(
                        new JitAuthorizationPolicies()
                            .withPrincipalId("1db8e132e2934dbcb8e1178a61319491")
                            .withRoleDefinitionId("ecd05a23-931a-4c38-a52b-ac7c4c583334")))
            .withJitSchedulingPolicy(
                new JitSchedulingPolicy()
                    .withType(JitSchedulingType.ONCE)
                    .withDuration(Duration.parse("PT8H"))
                    .withStartTime(OffsetDateTime.parse("2021-04-22T05:48:30.6661804Z")))
            .create();
    }
}
```

### JitRequests_Delete

```java
import com.azure.core.util.Context;

/** Samples for JitRequests Delete. */
public final class JitRequestsDeleteSamples {
    /*
     * x-ms-original-file: specification/solutions/resource-manager/Microsoft.Solutions/stable/2021-07-01/examples/deleteJitRequest.json
     */
    /**
     * Sample code: Delete jit request.
     *
     * @param manager Entry point to ManagedApplicationManager.
     */
    public static void deleteJitRequest(com.azure.resourcemanager.solutions.ManagedApplicationManager manager) {
        manager.jitRequests().deleteWithResponse("rg", "myJitRequest", Context.NONE);
    }
}
```

### JitRequests_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for JitRequests GetByResourceGroup. */
public final class JitRequestsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/solutions/resource-manager/Microsoft.Solutions/stable/2021-07-01/examples/getJitRequest.json
     */
    /**
     * Sample code: Create or update jit request.
     *
     * @param manager Entry point to ManagedApplicationManager.
     */
    public static void createOrUpdateJitRequest(com.azure.resourcemanager.solutions.ManagedApplicationManager manager) {
        manager.jitRequests().getByResourceGroupWithResponse("rg", "myJitRequest", Context.NONE);
    }
}
```

### JitRequests_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for JitRequests ListByResourceGroup. */
public final class JitRequestsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/solutions/resource-manager/Microsoft.Solutions/stable/2021-07-01/examples/listJitRequestsByResourceGroup.json
     */
    /**
     * Sample code: List jit requests.
     *
     * @param manager Entry point to ManagedApplicationManager.
     */
    public static void listJitRequests(com.azure.resourcemanager.solutions.ManagedApplicationManager manager) {
        manager.jitRequests().listByResourceGroupWithResponse("rg", Context.NONE);
    }
}
```

### JitRequests_ListBySubscription

```java
import com.azure.core.util.Context;

/** Samples for JitRequests ListBySubscription. */
public final class JitRequestsListBySubscriptionSamples {
    /*
     * x-ms-original-file: specification/solutions/resource-manager/Microsoft.Solutions/stable/2021-07-01/examples/listJitRequestsByResourceGroup.json
     */
    /**
     * Sample code: List jit requests.
     *
     * @param manager Entry point to ManagedApplicationManager.
     */
    public static void listJitRequests(com.azure.resourcemanager.solutions.ManagedApplicationManager manager) {
        manager.jitRequests().listBySubscriptionWithResponse(Context.NONE);
    }
}
```

### JitRequests_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.solutions.models.JitRequestDefinition;
import java.util.HashMap;
import java.util.Map;

/** Samples for JitRequests Update. */
public final class JitRequestsUpdateSamples {
    /*
     * x-ms-original-file: specification/solutions/resource-manager/Microsoft.Solutions/stable/2021-07-01/examples/updateJitRequest.json
     */
    /**
     * Sample code: Create or update jit request.
     *
     * @param manager Entry point to ManagedApplicationManager.
     */
    public static void createOrUpdateJitRequest(com.azure.resourcemanager.solutions.ManagedApplicationManager manager) {
        JitRequestDefinition resource =
            manager.jitRequests().getByResourceGroupWithResponse("rg", "myJitRequest", Context.NONE).getValue();
        resource.update().withTags(mapOf("department", "Finance")).apply();
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

### ResourceProvider_ListOperations

```java
import com.azure.core.util.Context;

/** Samples for ResourceProvider ListOperations. */
public final class ResourceProviderListOperationsSamples {
    /*
     * x-ms-original-file: specification/solutions/resource-manager/Microsoft.Solutions/stable/2021-07-01/examples/listSolutionsOperations.json
     */
    /**
     * Sample code: List Solutions operations.
     *
     * @param manager Entry point to ManagedApplicationManager.
     */
    public static void listSolutionsOperations(com.azure.resourcemanager.solutions.ManagedApplicationManager manager) {
        manager.resourceProviders().listOperations(Context.NONE);
    }
}
```

