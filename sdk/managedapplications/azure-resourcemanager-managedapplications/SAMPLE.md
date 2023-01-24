# Code snippets and samples


## ApplicationDefinitions

- [CreateOrUpdate](#applicationdefinitions_createorupdate)
- [CreateOrUpdateById](#applicationdefinitions_createorupdatebyid)
- [Delete](#applicationdefinitions_delete)
- [DeleteById](#applicationdefinitions_deletebyid)
- [GetById](#applicationdefinitions_getbyid)
- [GetByResourceGroup](#applicationdefinitions_getbyresourcegroup)
- [ListByResourceGroup](#applicationdefinitions_listbyresourcegroup)

## Applications

- [CreateOrUpdate](#applications_createorupdate)
- [CreateOrUpdateById](#applications_createorupdatebyid)
- [Delete](#applications_delete)
- [DeleteById](#applications_deletebyid)
- [GetById](#applications_getbyid)
- [GetByResourceGroup](#applications_getbyresourcegroup)
- [List](#applications_list)
- [ListByResourceGroup](#applications_listbyresourcegroup)
- [Update](#applications_update)
- [UpdateById](#applications_updatebyid)

## ResourceProvider

- [ListOperations](#resourceprovider_listoperations)
### ApplicationDefinitions_CreateOrUpdate

```java
import com.azure.resourcemanager.managedapplications.models.ApplicationLockLevel;
import com.azure.resourcemanager.managedapplications.models.ApplicationProviderAuthorization;
import java.util.Arrays;

/** Samples for ApplicationDefinitions CreateOrUpdate. */
public final class ApplicationDefinitionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/resources/resource-manager/Microsoft.Solutions/stable/2018-06-01/examples/createOrUpdateApplicationDefinition.json
     */
    /**
     * Sample code: Create or update managed application definition.
     *
     * @param manager Entry point to ApplicationManager.
     */
    public static void createOrUpdateManagedApplicationDefinition(
        com.azure.resourcemanager.managedapplications.ApplicationManager manager) {
        manager
            .applicationDefinitions()
            .define("myManagedApplicationDef")
            .withRegion("East US 2")
            .withExistingResourceGroup("rg")
            .withLockLevel(ApplicationLockLevel.NONE)
            .withAuthorizations(
                Arrays
                    .asList(
                        new ApplicationProviderAuthorization()
                            .withPrincipalId("validprincipalguid")
                            .withRoleDefinitionId("validroleguid")))
            .withDisplayName("myManagedApplicationDef")
            .withDescription("myManagedApplicationDef description")
            .withPackageFileUri("https://path/to/packagezipfile")
            .create();
    }
}
```

### ApplicationDefinitions_CreateOrUpdateById

```java
import com.azure.resourcemanager.managedapplications.fluent.models.ApplicationDefinitionInner;
import com.azure.resourcemanager.managedapplications.models.ApplicationLockLevel;
import com.azure.resourcemanager.managedapplications.models.ApplicationProviderAuthorization;
import java.util.Arrays;

/** Samples for ApplicationDefinitions CreateOrUpdateById. */
public final class ApplicationDefinitionsCreateOrUpdateByIdSamples {
    /*
     * x-ms-original-file: specification/resources/resource-manager/Microsoft.Solutions/stable/2018-06-01/examples/createOrUpdateApplicationDefinition.json
     */
    /**
     * Sample code: Create or update managed application definition.
     *
     * @param manager Entry point to ApplicationManager.
     */
    public static void createOrUpdateManagedApplicationDefinition(
        com.azure.resourcemanager.managedapplications.ApplicationManager manager) {
        manager
            .applicationDefinitions()
            .createOrUpdateById(
                "rg",
                "myManagedApplicationDef",
                new ApplicationDefinitionInner()
                    .withLocation("East US 2")
                    .withLockLevel(ApplicationLockLevel.NONE)
                    .withDisplayName("myManagedApplicationDef")
                    .withAuthorizations(
                        Arrays
                            .asList(
                                new ApplicationProviderAuthorization()
                                    .withPrincipalId("validprincipalguid")
                                    .withRoleDefinitionId("validroleguid")))
                    .withDescription("myManagedApplicationDef description")
                    .withPackageFileUri("https://path/to/packagezipfile"),
                com.azure.core.util.Context.NONE);
    }
}
```

### ApplicationDefinitions_Delete

```java
/** Samples for ApplicationDefinitions Delete. */
public final class ApplicationDefinitionsDeleteSamples {
    /*
     * x-ms-original-file: specification/resources/resource-manager/Microsoft.Solutions/stable/2018-06-01/examples/deleteApplicationDefinition.json
     */
    /**
     * Sample code: Deletes a managed application.
     *
     * @param manager Entry point to ApplicationManager.
     */
    public static void deletesAManagedApplication(
        com.azure.resourcemanager.managedapplications.ApplicationManager manager) {
        manager.applicationDefinitions().delete("rg", "myManagedApplicationDef", com.azure.core.util.Context.NONE);
    }
}
```

### ApplicationDefinitions_DeleteById

```java
/** Samples for ApplicationDefinitions DeleteById. */
public final class ApplicationDefinitionsDeleteByIdSamples {
    /*
     * x-ms-original-file: specification/resources/resource-manager/Microsoft.Solutions/stable/2018-06-01/examples/deleteApplicationDefinition.json
     */
    /**
     * Sample code: Delete application definition.
     *
     * @param manager Entry point to ApplicationManager.
     */
    public static void deleteApplicationDefinition(
        com.azure.resourcemanager.managedapplications.ApplicationManager manager) {
        manager.applicationDefinitions().deleteById("rg", "myManagedApplicationDef", com.azure.core.util.Context.NONE);
    }
}
```

### ApplicationDefinitions_GetById

```java
/** Samples for ApplicationDefinitions GetById. */
public final class ApplicationDefinitionsGetByIdSamples {
    /*
     * x-ms-original-file: specification/resources/resource-manager/Microsoft.Solutions/stable/2018-06-01/examples/getApplicationDefinition.json
     */
    /**
     * Sample code: Get managed application definition.
     *
     * @param manager Entry point to ApplicationManager.
     */
    public static void getManagedApplicationDefinition(
        com.azure.resourcemanager.managedapplications.ApplicationManager manager) {
        manager
            .applicationDefinitions()
            .getByIdWithResponse("rg", "myManagedApplicationDef", com.azure.core.util.Context.NONE);
    }
}
```

### ApplicationDefinitions_GetByResourceGroup

```java
/** Samples for ApplicationDefinitions GetByResourceGroup. */
public final class ApplicationDefinitionsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/resources/resource-manager/Microsoft.Solutions/stable/2018-06-01/examples/getApplicationDefinition.json
     */
    /**
     * Sample code: Get managed application definition.
     *
     * @param manager Entry point to ApplicationManager.
     */
    public static void getManagedApplicationDefinition(
        com.azure.resourcemanager.managedapplications.ApplicationManager manager) {
        manager
            .applicationDefinitions()
            .getByResourceGroupWithResponse("rg", "myManagedApplicationDef", com.azure.core.util.Context.NONE);
    }
}
```

### ApplicationDefinitions_ListByResourceGroup

```java
/** Samples for ApplicationDefinitions ListByResourceGroup. */
public final class ApplicationDefinitionsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/resources/resource-manager/Microsoft.Solutions/stable/2018-06-01/examples/listApplicationDefinitionsByResourceGroup.json
     */
    /**
     * Sample code: List managed application definitions.
     *
     * @param manager Entry point to ApplicationManager.
     */
    public static void listManagedApplicationDefinitions(
        com.azure.resourcemanager.managedapplications.ApplicationManager manager) {
        manager.applicationDefinitions().listByResourceGroup("rg", com.azure.core.util.Context.NONE);
    }
}
```

### Applications_CreateOrUpdate

```java
/** Samples for Applications CreateOrUpdate. */
public final class ApplicationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/resources/resource-manager/Microsoft.Solutions/stable/2018-06-01/examples/createOrUpdateApplication.json
     */
    /**
     * Sample code: Create or update managed application.
     *
     * @param manager Entry point to ApplicationManager.
     */
    public static void createOrUpdateManagedApplication(
        com.azure.resourcemanager.managedapplications.ApplicationManager manager) {
        manager
            .applications()
            .define("myManagedApplication")
            .withRegion("East US 2")
            .withExistingResourceGroup("rg")
            .withKind("ServiceCatalog")
            .withManagedResourceGroupId("/subscriptions/subid/resourceGroups/myManagedRG")
            .create();
    }
}
```

### Applications_CreateOrUpdateById

```java
import com.azure.resourcemanager.managedapplications.fluent.models.ApplicationInner;

/** Samples for Applications CreateOrUpdateById. */
public final class ApplicationsCreateOrUpdateByIdSamples {
    /*
     * x-ms-original-file: specification/resources/resource-manager/Microsoft.Solutions/stable/2018-06-01/examples/createOrUpdateApplicationById.json
     */
    /**
     * Sample code: Create or update application by id.
     *
     * @param manager Entry point to ApplicationManager.
     */
    public static void createOrUpdateApplicationById(
        com.azure.resourcemanager.managedapplications.ApplicationManager manager) {
        manager
            .applications()
            .createOrUpdateById(
                "myApplicationId",
                new ApplicationInner()
                    .withLocation("East US 2")
                    .withKind("ServiceCatalog")
                    .withManagedResourceGroupId("/subscriptions/subid/resourceGroups/myManagedRG"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Applications_Delete

```java
/** Samples for Applications Delete. */
public final class ApplicationsDeleteSamples {
    /*
     * x-ms-original-file: specification/resources/resource-manager/Microsoft.Solutions/stable/2018-06-01/examples/deleteApplication.json
     */
    /**
     * Sample code: Deletes a managed application.
     *
     * @param manager Entry point to ApplicationManager.
     */
    public static void deletesAManagedApplication(
        com.azure.resourcemanager.managedapplications.ApplicationManager manager) {
        manager.applications().delete("rg", "myManagedApplication", com.azure.core.util.Context.NONE);
    }
}
```

### Applications_DeleteById

```java
/** Samples for Applications DeleteById. */
public final class ApplicationsDeleteByIdSamples {
    /*
     * x-ms-original-file: specification/resources/resource-manager/Microsoft.Solutions/stable/2018-06-01/examples/deleteApplicationById.json
     */
    /**
     * Sample code: Delete application by id.
     *
     * @param manager Entry point to ApplicationManager.
     */
    public static void deleteApplicationById(com.azure.resourcemanager.managedapplications.ApplicationManager manager) {
        manager.applications().deleteById("myApplicationId", com.azure.core.util.Context.NONE);
    }
}
```

### Applications_GetById

```java
/** Samples for Applications GetById. */
public final class ApplicationsGetByIdSamples {
    /*
     * x-ms-original-file: specification/resources/resource-manager/Microsoft.Solutions/stable/2018-06-01/examples/getApplicationById.json
     */
    /**
     * Sample code: Get application by id.
     *
     * @param manager Entry point to ApplicationManager.
     */
    public static void getApplicationById(com.azure.resourcemanager.managedapplications.ApplicationManager manager) {
        manager.applications().getByIdWithResponse("myApplicationId", com.azure.core.util.Context.NONE);
    }
}
```

### Applications_GetByResourceGroup

```java
/** Samples for Applications GetByResourceGroup. */
public final class ApplicationsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/resources/resource-manager/Microsoft.Solutions/stable/2018-06-01/examples/getApplication.json
     */
    /**
     * Sample code: Get a managed application.
     *
     * @param manager Entry point to ApplicationManager.
     */
    public static void getAManagedApplication(
        com.azure.resourcemanager.managedapplications.ApplicationManager manager) {
        manager
            .applications()
            .getByResourceGroupWithResponse("rg", "myManagedApplication", com.azure.core.util.Context.NONE);
    }
}
```

### Applications_List

```java
/** Samples for Applications List. */
public final class ApplicationsListSamples {
    /*
     * x-ms-original-file: specification/resources/resource-manager/Microsoft.Solutions/stable/2018-06-01/examples/listApplicationsBySubscription.json
     */
    /**
     * Sample code: Lists applications by subscription.
     *
     * @param manager Entry point to ApplicationManager.
     */
    public static void listsApplicationsBySubscription(
        com.azure.resourcemanager.managedapplications.ApplicationManager manager) {
        manager.applications().list(com.azure.core.util.Context.NONE);
    }
}
```

### Applications_ListByResourceGroup

```java
/** Samples for Applications ListByResourceGroup. */
public final class ApplicationsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/resources/resource-manager/Microsoft.Solutions/stable/2018-06-01/examples/listApplicationsByResourceGroup.json
     */
    /**
     * Sample code: Lists applications.
     *
     * @param manager Entry point to ApplicationManager.
     */
    public static void listsApplications(com.azure.resourcemanager.managedapplications.ApplicationManager manager) {
        manager.applications().listByResourceGroup("rg", com.azure.core.util.Context.NONE);
    }
}
```

### Applications_Update

```java
import com.azure.resourcemanager.managedapplications.models.Application;

/** Samples for Applications Update. */
public final class ApplicationsUpdateSamples {
    /*
     * x-ms-original-file: specification/resources/resource-manager/Microsoft.Solutions/stable/2018-06-01/examples/updateApplication.json
     */
    /**
     * Sample code: Updates a managed application.
     *
     * @param manager Entry point to ApplicationManager.
     */
    public static void updatesAManagedApplication(
        com.azure.resourcemanager.managedapplications.ApplicationManager manager) {
        Application resource =
            manager
                .applications()
                .getByResourceGroupWithResponse("rg", "myManagedApplication", com.azure.core.util.Context.NONE)
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

### Applications_UpdateById

```java
import com.azure.resourcemanager.managedapplications.fluent.models.ApplicationInner;

/** Samples for Applications UpdateById. */
public final class ApplicationsUpdateByIdSamples {
    /*
     * x-ms-original-file: specification/resources/resource-manager/Microsoft.Solutions/stable/2018-06-01/examples/updateApplicationById.json
     */
    /**
     * Sample code: Update application by id.
     *
     * @param manager Entry point to ApplicationManager.
     */
    public static void updateApplicationById(com.azure.resourcemanager.managedapplications.ApplicationManager manager) {
        manager
            .applications()
            .updateByIdWithResponse(
                "myApplicationId",
                new ApplicationInner()
                    .withKind("ServiceCatalog")
                    .withManagedResourceGroupId("/subscriptions/subid/resourceGroups/myManagedRG")
                    .withApplicationDefinitionId(
                        "/subscriptions/subid/resourceGroups/rg/providers/Microsoft.Solutions/applicationDefinitions/myAppDef"),
                com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_ListOperations

```java
/** Samples for ResourceProvider ListOperations. */
public final class ResourceProviderListOperationsSamples {
    /*
     * x-ms-original-file: specification/resources/resource-manager/Microsoft.Solutions/stable/2018-06-01/examples/listSolutionsOperations.json
     */
    /**
     * Sample code: List Solutions operations.
     *
     * @param manager Entry point to ApplicationManager.
     */
    public static void listSolutionsOperations(
        com.azure.resourcemanager.managedapplications.ApplicationManager manager) {
        manager.resourceProviders().listOperations(com.azure.core.util.Context.NONE);
    }
}
```

