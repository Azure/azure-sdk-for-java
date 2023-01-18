# Code snippets and samples


## ManagementAssociations

- [CreateOrUpdate](#managementassociations_createorupdate)
- [Delete](#managementassociations_delete)
- [Get](#managementassociations_get)
- [ListBySubscription](#managementassociations_listbysubscription)

## ManagementConfigurations

- [CreateOrUpdate](#managementconfigurations_createorupdate)
- [Delete](#managementconfigurations_delete)
- [GetByResourceGroup](#managementconfigurations_getbyresourcegroup)
- [ListBySubscription](#managementconfigurations_listbysubscription)

## Operations

- [List](#operations_list)

## Solutions

- [CreateOrUpdate](#solutions_createorupdate)
- [Delete](#solutions_delete)
- [GetByResourceGroup](#solutions_getbyresourcegroup)
- [ListByResourceGroup](#solutions_listbyresourcegroup)
- [ListBySubscription](#solutions_listbysubscription)
- [Update](#solutions_update)
### ManagementAssociations_CreateOrUpdate

```java
import com.azure.resourcemanager.operationsmanagement.fluent.models.ManagementAssociationInner;
import com.azure.resourcemanager.operationsmanagement.models.ManagementAssociationProperties;

/** Samples for ManagementAssociations CreateOrUpdate. */
public final class ManagementAssociationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/operationsmanagement/resource-manager/Microsoft.OperationsManagement/preview/2015-11-01-preview/examples/ManagementAssociationCreate.json
     */
    /**
     * Sample code: SolutionCreate.
     *
     * @param manager Entry point to OperationsManagementManager.
     */
    public static void solutionCreate(
        com.azure.resourcemanager.operationsmanagement.OperationsManagementManager manager) {
        manager
            .managementAssociations()
            .createOrUpdateWithResponse(
                "rg1",
                "providerName",
                "resourceType",
                "resourceName",
                "managementAssociation1",
                new ManagementAssociationInner()
                    .withLocation("East US")
                    .withProperties(
                        new ManagementAssociationProperties()
                            .withApplicationId(
                                "/subscriptions/sub1/resourcegroups/rg1/providers/Microsoft.Appliance/Appliances/appliance1")),
                com.azure.core.util.Context.NONE);
    }
}
```

### ManagementAssociations_Delete

```java
/** Samples for ManagementAssociations Delete. */
public final class ManagementAssociationsDeleteSamples {
    /*
     * x-ms-original-file: specification/operationsmanagement/resource-manager/Microsoft.OperationsManagement/preview/2015-11-01-preview/examples/ManagementAssociationDelete.json
     */
    /**
     * Sample code: SolutionDelete.
     *
     * @param manager Entry point to OperationsManagementManager.
     */
    public static void solutionDelete(
        com.azure.resourcemanager.operationsmanagement.OperationsManagementManager manager) {
        manager
            .managementAssociations()
            .deleteWithResponse(
                "rg1",
                "providerName",
                "resourceType",
                "resourceName",
                "managementAssociationName",
                com.azure.core.util.Context.NONE);
    }
}
```

### ManagementAssociations_Get

```java
/** Samples for ManagementAssociations Get. */
public final class ManagementAssociationsGetSamples {
    /*
     * x-ms-original-file: specification/operationsmanagement/resource-manager/Microsoft.OperationsManagement/preview/2015-11-01-preview/examples/ManagementAssociationGet.json
     */
    /**
     * Sample code: SolutionGet.
     *
     * @param manager Entry point to OperationsManagementManager.
     */
    public static void solutionGet(com.azure.resourcemanager.operationsmanagement.OperationsManagementManager manager) {
        manager
            .managementAssociations()
            .getWithResponse(
                "rg1",
                "providerName",
                "resourceType",
                "resourceName",
                "managementAssociation1",
                com.azure.core.util.Context.NONE);
    }
}
```

### ManagementAssociations_ListBySubscription

```java
/** Samples for ManagementAssociations ListBySubscription. */
public final class ManagementAssociationsListBySubscriptionSamples {
    /*
     * x-ms-original-file: specification/operationsmanagement/resource-manager/Microsoft.OperationsManagement/preview/2015-11-01-preview/examples/ManagementAssociationListForSubscription.json
     */
    /**
     * Sample code: SolutionList.
     *
     * @param manager Entry point to OperationsManagementManager.
     */
    public static void solutionList(
        com.azure.resourcemanager.operationsmanagement.OperationsManagementManager manager) {
        manager.managementAssociations().listBySubscriptionWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

### ManagementConfigurations_CreateOrUpdate

```java
/** Samples for ManagementConfigurations CreateOrUpdate. */
public final class ManagementConfigurationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/operationsmanagement/resource-manager/Microsoft.OperationsManagement/preview/2015-11-01-preview/examples/ManagementConfigurationCreate.json
     */
    /**
     * Sample code: ManagementConfigurationCreate.
     *
     * @param manager Entry point to OperationsManagementManager.
     */
    public static void managementConfigurationCreate(
        com.azure.resourcemanager.operationsmanagement.OperationsManagementManager manager) {
        manager
            .managementConfigurations()
            .define("managementConfiguration1")
            .withExistingResourceGroup("rg1")
            .withRegion("East US")
            .create();
    }
}
```

### ManagementConfigurations_Delete

```java
/** Samples for ManagementConfigurations Delete. */
public final class ManagementConfigurationsDeleteSamples {
    /*
     * x-ms-original-file: specification/operationsmanagement/resource-manager/Microsoft.OperationsManagement/preview/2015-11-01-preview/examples/ManagementConfigurationDelete.json
     */
    /**
     * Sample code: ManagementConfigurationDelete.
     *
     * @param manager Entry point to OperationsManagementManager.
     */
    public static void managementConfigurationDelete(
        com.azure.resourcemanager.operationsmanagement.OperationsManagementManager manager) {
        manager
            .managementConfigurations()
            .deleteByResourceGroupWithResponse("rg1", "managementConfigurationName", com.azure.core.util.Context.NONE);
    }
}
```

### ManagementConfigurations_GetByResourceGroup

```java
/** Samples for ManagementConfigurations GetByResourceGroup. */
public final class ManagementConfigurationsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/operationsmanagement/resource-manager/Microsoft.OperationsManagement/preview/2015-11-01-preview/examples/ManagementConfigurationGet.json
     */
    /**
     * Sample code: SolutionGet.
     *
     * @param manager Entry point to OperationsManagementManager.
     */
    public static void solutionGet(com.azure.resourcemanager.operationsmanagement.OperationsManagementManager manager) {
        manager
            .managementConfigurations()
            .getByResourceGroupWithResponse("rg1", "managementConfigurationName", com.azure.core.util.Context.NONE);
    }
}
```

### ManagementConfigurations_ListBySubscription

```java
/** Samples for ManagementConfigurations ListBySubscription. */
public final class ManagementConfigurationsListBySubscriptionSamples {
    /*
     * x-ms-original-file: specification/operationsmanagement/resource-manager/Microsoft.OperationsManagement/preview/2015-11-01-preview/examples/ManagementConfigurationListForSubscription.json
     */
    /**
     * Sample code: SolutionList.
     *
     * @param manager Entry point to OperationsManagementManager.
     */
    public static void solutionList(
        com.azure.resourcemanager.operationsmanagement.OperationsManagementManager manager) {
        manager.managementConfigurations().listBySubscriptionWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/operationsmanagement/resource-manager/Microsoft.OperationsManagement/preview/2015-11-01-preview/examples/OperationsList.json
     */
    /**
     * Sample code: OperationsList.
     *
     * @param manager Entry point to OperationsManagementManager.
     */
    public static void operationsList(
        com.azure.resourcemanager.operationsmanagement.OperationsManagementManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Solutions_CreateOrUpdate

```java
import com.azure.resourcemanager.operationsmanagement.models.SolutionPlan;
import com.azure.resourcemanager.operationsmanagement.models.SolutionProperties;
import java.util.Arrays;

/** Samples for Solutions CreateOrUpdate. */
public final class SolutionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/operationsmanagement/resource-manager/Microsoft.OperationsManagement/preview/2015-11-01-preview/examples/SolutionCreate.json
     */
    /**
     * Sample code: SolutionCreate.
     *
     * @param manager Entry point to OperationsManagementManager.
     */
    public static void solutionCreate(
        com.azure.resourcemanager.operationsmanagement.OperationsManagementManager manager) {
        manager
            .solutions()
            .define("solution1")
            .withRegion("East US")
            .withExistingResourceGroup("rg1")
            .withPlan(
                new SolutionPlan()
                    .withName("name1")
                    .withPublisher("publisher1")
                    .withPromotionCode("fakeTokenPlaceholder")
                    .withProduct("product1"))
            .withProperties(
                new SolutionProperties()
                    .withWorkspaceResourceId(
                        "/subscriptions/sub2/resourceGroups/rg2/providers/Microsoft.OperationalInsights/workspaces/ws1")
                    .withContainedResources(
                        Arrays
                            .asList(
                                "/subscriptions/sub2/resourceGroups/rg2/providers/provider1/resources/resource1",
                                "/subscriptions/sub2/resourceGroups/rg2/providers/provider2/resources/resource2"))
                    .withReferencedResources(
                        Arrays
                            .asList(
                                "/subscriptions/sub2/resourceGroups/rg2/providers/provider1/resources/resource2",
                                "/subscriptions/sub2/resourceGroups/rg2/providers/provider2/resources/resource3")))
            .create();
    }
}
```

### Solutions_Delete

```java
/** Samples for Solutions Delete. */
public final class SolutionsDeleteSamples {
    /*
     * x-ms-original-file: specification/operationsmanagement/resource-manager/Microsoft.OperationsManagement/preview/2015-11-01-preview/examples/SolutionDelete.json
     */
    /**
     * Sample code: SolutionDelete.
     *
     * @param manager Entry point to OperationsManagementManager.
     */
    public static void solutionDelete(
        com.azure.resourcemanager.operationsmanagement.OperationsManagementManager manager) {
        manager.solutions().delete("rg1", "solution1", com.azure.core.util.Context.NONE);
    }
}
```

### Solutions_GetByResourceGroup

```java
/** Samples for Solutions GetByResourceGroup. */
public final class SolutionsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/operationsmanagement/resource-manager/Microsoft.OperationsManagement/preview/2015-11-01-preview/examples/SolutionGet.json
     */
    /**
     * Sample code: SolutionGet.
     *
     * @param manager Entry point to OperationsManagementManager.
     */
    public static void solutionGet(com.azure.resourcemanager.operationsmanagement.OperationsManagementManager manager) {
        manager.solutions().getByResourceGroupWithResponse("rg1", "solution1", com.azure.core.util.Context.NONE);
    }
}
```

### Solutions_ListByResourceGroup

```java
/** Samples for Solutions ListByResourceGroup. */
public final class SolutionsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/operationsmanagement/resource-manager/Microsoft.OperationsManagement/preview/2015-11-01-preview/examples/SolutionList.json
     */
    /**
     * Sample code: SolutionList.
     *
     * @param manager Entry point to OperationsManagementManager.
     */
    public static void solutionList(
        com.azure.resourcemanager.operationsmanagement.OperationsManagementManager manager) {
        manager.solutions().listByResourceGroupWithResponse("rg1", com.azure.core.util.Context.NONE);
    }
}
```

### Solutions_ListBySubscription

```java
/** Samples for Solutions ListBySubscription. */
public final class SolutionsListBySubscriptionSamples {
    /*
     * x-ms-original-file: specification/operationsmanagement/resource-manager/Microsoft.OperationsManagement/preview/2015-11-01-preview/examples/SolutionListForSubscription.json
     */
    /**
     * Sample code: SolutionList.
     *
     * @param manager Entry point to OperationsManagementManager.
     */
    public static void solutionList(
        com.azure.resourcemanager.operationsmanagement.OperationsManagementManager manager) {
        manager.solutions().listBySubscriptionWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

### Solutions_Update

```java
import com.azure.resourcemanager.operationsmanagement.models.Solution;
import java.util.HashMap;
import java.util.Map;

/** Samples for Solutions Update. */
public final class SolutionsUpdateSamples {
    /*
     * x-ms-original-file: specification/operationsmanagement/resource-manager/Microsoft.OperationsManagement/preview/2015-11-01-preview/examples/SolutionUpdate.json
     */
    /**
     * Sample code: SolutionUpdate.
     *
     * @param manager Entry point to OperationsManagementManager.
     */
    public static void solutionUpdate(
        com.azure.resourcemanager.operationsmanagement.OperationsManagementManager manager) {
        Solution resource =
            manager
                .solutions()
                .getByResourceGroupWithResponse("rg1", "solution1", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("Dept", "IT", "Environment", "Test")).apply();
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

