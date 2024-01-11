# Code snippets and samples


## Operations

- [List](#operations_list)

## Services

- [CreateOrUpdate](#services_createorupdate)
- [Delete](#services_delete)
- [GetByResourceGroup](#services_getbyresourcegroup)
- [List](#services_list)
- [ListByResourceGroup](#services_listbyresourcegroup)
- [Update](#services_update)
### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/apicenter/resource-manager/Microsoft.ApiCenter/preview/2023-07-01-preview/examples/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     *
     * @param manager Entry point to ApiCenterManager.
     */
    public static void operationsList(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Services_CreateOrUpdate

```java
/** Samples for Services CreateOrUpdate. */
public final class ServicesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/apicenter/resource-manager/Microsoft.ApiCenter/preview/2023-07-01-preview/examples/Services_CreateOrUpdate.json
     */
    /**
     * Sample code: Services_Create.
     *
     * @param manager Entry point to ApiCenterManager.
     */
    public static void servicesCreate(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager
            .services()
            .define("contoso")
            .withRegion((String) null)
            .withExistingResourceGroup("contoso-resources")
            .create();
    }
}
```

### Services_Delete

```java
/** Samples for Services Delete. */
public final class ServicesDeleteSamples {
    /*
     * x-ms-original-file: specification/apicenter/resource-manager/Microsoft.ApiCenter/preview/2023-07-01-preview/examples/Services_Delete.json
     */
    /**
     * Sample code: Services_Delete.
     *
     * @param manager Entry point to ApiCenterManager.
     */
    public static void servicesDelete(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager
            .services()
            .deleteByResourceGroupWithResponse("contoso-resources", "contoso", com.azure.core.util.Context.NONE);
    }
}
```

### Services_GetByResourceGroup

```java
/** Samples for Services GetByResourceGroup. */
public final class ServicesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/apicenter/resource-manager/Microsoft.ApiCenter/preview/2023-07-01-preview/examples/Services_Get.json
     */
    /**
     * Sample code: Services_Get.
     *
     * @param manager Entry point to ApiCenterManager.
     */
    public static void servicesGet(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager
            .services()
            .getByResourceGroupWithResponse("contoso-resources", "contoso", com.azure.core.util.Context.NONE);
    }
}
```

### Services_List

```java
/** Samples for Services List. */
public final class ServicesListSamples {
    /*
     * x-ms-original-file: specification/apicenter/resource-manager/Microsoft.ApiCenter/preview/2023-07-01-preview/examples/Services_ListBySubscription.json
     */
    /**
     * Sample code: Services_ListBySubscription.
     *
     * @param manager Entry point to ApiCenterManager.
     */
    public static void servicesListBySubscription(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.services().list(com.azure.core.util.Context.NONE);
    }
}
```

### Services_ListByResourceGroup

```java
/** Samples for Services ListByResourceGroup. */
public final class ServicesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/apicenter/resource-manager/Microsoft.ApiCenter/preview/2023-07-01-preview/examples/Services_ListByResourceGroup.json
     */
    /**
     * Sample code: Services_ListByResourceGroup.
     *
     * @param manager Entry point to ApiCenterManager.
     */
    public static void servicesListByResourceGroup(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.services().listByResourceGroup("contoso-resources", com.azure.core.util.Context.NONE);
    }
}
```

### Services_Update

```java
import com.azure.resourcemanager.apicenter.models.Service;

/** Samples for Services Update. */
public final class ServicesUpdateSamples {
    /*
     * x-ms-original-file: specification/apicenter/resource-manager/Microsoft.ApiCenter/preview/2023-07-01-preview/examples/Services_Update.json
     */
    /**
     * Sample code: Services_Update.
     *
     * @param manager Entry point to ApiCenterManager.
     */
    public static void servicesUpdate(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        Service resource =
            manager
                .services()
                .getByResourceGroupWithResponse("contoso-resources", "contoso", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().apply();
    }
}
```

