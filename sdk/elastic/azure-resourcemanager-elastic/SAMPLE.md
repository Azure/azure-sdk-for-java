# Code snippets and samples


## DeploymentInfo

- [List](#deploymentinfo_list)

## MonitoredResources

- [List](#monitoredresources_list)

## Monitors

- [Create](#monitors_create)
- [Delete](#monitors_delete)
- [GetByResourceGroup](#monitors_getbyresourcegroup)
- [List](#monitors_list)
- [ListByResourceGroup](#monitors_listbyresourcegroup)
- [Update](#monitors_update)

## Operations

- [List](#operations_list)

## TagRules

- [CreateOrUpdate](#tagrules_createorupdate)
- [Delete](#tagrules_delete)
- [Get](#tagrules_get)
- [List](#tagrules_list)

## VMCollection

- [Update](#vmcollection_update)

## VMHost

- [List](#vmhost_list)

## VMIngestion

- [Details](#vmingestion_details)
### DeploymentInfo_List

```java
import com.azure.core.util.Context;

/** Samples for DeploymentInfo List. */
public final class DeploymentInfoListSamples {
    /*
     * x-ms-original-file: specification/elastic/resource-manager/Microsoft.Elastic/preview/2020-07-01-preview/examples/DeploymentInfo_List.json
     */
    /**
     * Sample code: DeploymentInfo_List.
     *
     * @param manager Entry point to ElasticManager.
     */
    public static void deploymentInfoList(com.azure.resourcemanager.elastic.ElasticManager manager) {
        manager.deploymentInfoes().listWithResponse("myResourceGroup", "myMonitor", Context.NONE);
    }
}
```

### MonitoredResources_List

```java
import com.azure.core.util.Context;

/** Samples for MonitoredResources List. */
public final class MonitoredResourcesListSamples {
    /*
     * x-ms-original-file: specification/elastic/resource-manager/Microsoft.Elastic/preview/2020-07-01-preview/examples/MonitoredResources_List.json
     */
    /**
     * Sample code: MonitoredResources_List.
     *
     * @param manager Entry point to ElasticManager.
     */
    public static void monitoredResourcesList(com.azure.resourcemanager.elastic.ElasticManager manager) {
        manager.monitoredResources().list("myResourceGroup", "myMonitor", Context.NONE);
    }
}
```

### Monitors_Create

```java
/** Samples for Monitors Create. */
public final class MonitorsCreateSamples {
    /*
     * x-ms-original-file: specification/elastic/resource-manager/Microsoft.Elastic/preview/2020-07-01-preview/examples/Monitors_Create.json
     */
    /**
     * Sample code: Monitors_Create.
     *
     * @param manager Entry point to ElasticManager.
     */
    public static void monitorsCreate(com.azure.resourcemanager.elastic.ElasticManager manager) {
        manager
            .monitors()
            .define("myMonitor")
            .withRegion((String) null)
            .withExistingResourceGroup("myResourceGroup")
            .create();
    }
}
```

### Monitors_Delete

```java
import com.azure.core.util.Context;

/** Samples for Monitors Delete. */
public final class MonitorsDeleteSamples {
    /*
     * x-ms-original-file: specification/elastic/resource-manager/Microsoft.Elastic/preview/2020-07-01-preview/examples/Monitors_Delete.json
     */
    /**
     * Sample code: Monitors_Delete.
     *
     * @param manager Entry point to ElasticManager.
     */
    public static void monitorsDelete(com.azure.resourcemanager.elastic.ElasticManager manager) {
        manager.monitors().delete("myResourceGroup", "myMonitor", Context.NONE);
    }
}
```

### Monitors_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Monitors GetByResourceGroup. */
public final class MonitorsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/elastic/resource-manager/Microsoft.Elastic/preview/2020-07-01-preview/examples/Monitors_Get.json
     */
    /**
     * Sample code: Monitors_Get.
     *
     * @param manager Entry point to ElasticManager.
     */
    public static void monitorsGet(com.azure.resourcemanager.elastic.ElasticManager manager) {
        manager.monitors().getByResourceGroupWithResponse("myResourceGroup", "myMonitor", Context.NONE);
    }
}
```

### Monitors_List

```java
import com.azure.core.util.Context;

/** Samples for Monitors List. */
public final class MonitorsListSamples {
    /*
     * x-ms-original-file: specification/elastic/resource-manager/Microsoft.Elastic/preview/2020-07-01-preview/examples/Monitors_List.json
     */
    /**
     * Sample code: Monitors_List.
     *
     * @param manager Entry point to ElasticManager.
     */
    public static void monitorsList(com.azure.resourcemanager.elastic.ElasticManager manager) {
        manager.monitors().list(Context.NONE);
    }
}
```

### Monitors_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Monitors ListByResourceGroup. */
public final class MonitorsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/elastic/resource-manager/Microsoft.Elastic/preview/2020-07-01-preview/examples/Monitors_ListByResourceGroup.json
     */
    /**
     * Sample code: Monitors_ListByResourceGroup.
     *
     * @param manager Entry point to ElasticManager.
     */
    public static void monitorsListByResourceGroup(com.azure.resourcemanager.elastic.ElasticManager manager) {
        manager.monitors().listByResourceGroup("myResourceGroup", Context.NONE);
    }
}
```

### Monitors_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.elastic.models.ElasticMonitorResource;

/** Samples for Monitors Update. */
public final class MonitorsUpdateSamples {
    /*
     * x-ms-original-file: specification/elastic/resource-manager/Microsoft.Elastic/preview/2020-07-01-preview/examples/Monitors_Update.json
     */
    /**
     * Sample code: Monitors_Update.
     *
     * @param manager Entry point to ElasticManager.
     */
    public static void monitorsUpdate(com.azure.resourcemanager.elastic.ElasticManager manager) {
        ElasticMonitorResource resource =
            manager.monitors().getByResourceGroupWithResponse("myResourceGroup", "myMonitor", Context.NONE).getValue();
        resource.update().apply();
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/elastic/resource-manager/Microsoft.Elastic/preview/2020-07-01-preview/examples/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     *
     * @param manager Entry point to ElasticManager.
     */
    public static void operationsList(com.azure.resourcemanager.elastic.ElasticManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### TagRules_CreateOrUpdate

```java
/** Samples for TagRules CreateOrUpdate. */
public final class TagRulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/elastic/resource-manager/Microsoft.Elastic/preview/2020-07-01-preview/examples/TagRules_CreateOrUpdate.json
     */
    /**
     * Sample code: TagRules_CreateOrUpdate.
     *
     * @param manager Entry point to ElasticManager.
     */
    public static void tagRulesCreateOrUpdate(com.azure.resourcemanager.elastic.ElasticManager manager) {
        manager.tagRules().define("default").withExistingMonitor("myResourceGroup", "myMonitor").create();
    }
}
```

### TagRules_Delete

```java
import com.azure.core.util.Context;

/** Samples for TagRules Delete. */
public final class TagRulesDeleteSamples {
    /*
     * x-ms-original-file: specification/elastic/resource-manager/Microsoft.Elastic/preview/2020-07-01-preview/examples/TagRules_Delete.json
     */
    /**
     * Sample code: TagRules_Delete.
     *
     * @param manager Entry point to ElasticManager.
     */
    public static void tagRulesDelete(com.azure.resourcemanager.elastic.ElasticManager manager) {
        manager.tagRules().delete("myResourceGroup", "myMonitor", "default", Context.NONE);
    }
}
```

### TagRules_Get

```java
import com.azure.core.util.Context;

/** Samples for TagRules Get. */
public final class TagRulesGetSamples {
    /*
     * x-ms-original-file: specification/elastic/resource-manager/Microsoft.Elastic/preview/2020-07-01-preview/examples/TagRules_Get.json
     */
    /**
     * Sample code: TagRules_Get.
     *
     * @param manager Entry point to ElasticManager.
     */
    public static void tagRulesGet(com.azure.resourcemanager.elastic.ElasticManager manager) {
        manager.tagRules().getWithResponse("myResourceGroup", "myMonitor", "default", Context.NONE);
    }
}
```

### TagRules_List

```java
import com.azure.core.util.Context;

/** Samples for TagRules List. */
public final class TagRulesListSamples {
    /*
     * x-ms-original-file: specification/elastic/resource-manager/Microsoft.Elastic/preview/2020-07-01-preview/examples/TagRules_List.json
     */
    /**
     * Sample code: TagRules_List.
     *
     * @param manager Entry point to ElasticManager.
     */
    public static void tagRulesList(com.azure.resourcemanager.elastic.ElasticManager manager) {
        manager.tagRules().list("myResourceGroup", "myMonitor", Context.NONE);
    }
}
```

### VMCollection_Update

```java
import com.azure.core.util.Context;

/** Samples for VMCollection Update. */
public final class VMCollectionUpdateSamples {
    /*
     * x-ms-original-file: specification/elastic/resource-manager/Microsoft.Elastic/preview/2020-07-01-preview/examples/VMCollection_Update.json
     */
    /**
     * Sample code: VMCollection_Update.
     *
     * @param manager Entry point to ElasticManager.
     */
    public static void vMCollectionUpdate(com.azure.resourcemanager.elastic.ElasticManager manager) {
        manager.vMCollections().updateWithResponse("myResourceGroup", "myMonitor", null, Context.NONE);
    }
}
```

### VMHost_List

```java
import com.azure.core.util.Context;

/** Samples for VMHost List. */
public final class VMHostListSamples {
    /*
     * x-ms-original-file: specification/elastic/resource-manager/Microsoft.Elastic/preview/2020-07-01-preview/examples/VMHost_List.json
     */
    /**
     * Sample code: VMHost_List.
     *
     * @param manager Entry point to ElasticManager.
     */
    public static void vMHostList(com.azure.resourcemanager.elastic.ElasticManager manager) {
        manager.vMHosts().list("myResourceGroup", "myMonitor", Context.NONE);
    }
}
```

### VMIngestion_Details

```java
import com.azure.core.util.Context;

/** Samples for VMIngestion Details. */
public final class VMIngestionDetailsSamples {
    /*
     * x-ms-original-file: specification/elastic/resource-manager/Microsoft.Elastic/preview/2020-07-01-preview/examples/VMIngestion_Details.json
     */
    /**
     * Sample code: VMIngestion_Details.
     *
     * @param manager Entry point to ElasticManager.
     */
    public static void vMIngestionDetails(com.azure.resourcemanager.elastic.ElasticManager manager) {
        manager.vMIngestions().detailsWithResponse("myResourceGroup", "myMonitor", Context.NONE);
    }
}
```

