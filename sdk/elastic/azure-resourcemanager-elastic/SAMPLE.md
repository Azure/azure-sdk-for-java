# Code snippets and samples


## AllTrafficFilters

- [List](#alltrafficfilters_list)

## AssociateTrafficFilter

- [Associate](#associatetrafficfilter_associate)

## CreateAndAssociateIpFilter

- [Create](#createandassociateipfilter_create)

## CreateAndAssociatePLFilter

- [Create](#createandassociateplfilter_create)

## DeploymentInfo

- [List](#deploymentinfo_list)

## DetachAndDeleteTrafficFilter

- [Delete](#detachanddeletetrafficfilter_delete)

## DetachTrafficFilter

- [Update](#detachtrafficfilter_update)

## ElasticVersions

- [List](#elasticversions_list)

## ExternalUser

- [CreateOrUpdate](#externaluser_createorupdate)

## ListAssociatedTrafficFilters

- [List](#listassociatedtrafficfilters_list)

## MonitorOperation

- [Upgrade](#monitoroperation_upgrade)

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

## Organizations

- [GetApiKey](#organizations_getapikey)

## TagRules

- [CreateOrUpdate](#tagrules_createorupdate)
- [Delete](#tagrules_delete)
- [Get](#tagrules_get)
- [List](#tagrules_list)

## TrafficFilters

- [Delete](#trafficfilters_delete)

## UpgradableVersions

- [Details](#upgradableversions_details)

## VMCollection

- [Update](#vmcollection_update)

## VMHost

- [List](#vmhost_list)

## VMIngestion

- [Details](#vmingestion_details)
### AllTrafficFilters_List

```java
/** Samples for AllTrafficFilters List. */
public final class AllTrafficFiltersListSamples {
    /*
     * x-ms-original-file: specification/elastic/resource-manager/Microsoft.Elastic/preview/2023-02-01-preview/examples/AllTrafficFilters_list.json
     */
    /**
     * Sample code: AllTrafficFilters_list.
     *
     * @param manager Entry point to ElasticManager.
     */
    public static void allTrafficFiltersList(com.azure.resourcemanager.elastic.ElasticManager manager) {
        manager.allTrafficFilters().listWithResponse("myResourceGroup", "myMonitor", com.azure.core.util.Context.NONE);
    }
}
```

### AssociateTrafficFilter_Associate

```java
/** Samples for AssociateTrafficFilter Associate. */
public final class AssociateTrafficFilterAssociateSamples {
    /*
     * x-ms-original-file: specification/elastic/resource-manager/Microsoft.Elastic/preview/2023-02-01-preview/examples/AssociateTrafficFilter_Update.json
     */
    /**
     * Sample code: AssociateTrafficFilter_Associate.
     *
     * @param manager Entry point to ElasticManager.
     */
    public static void associateTrafficFilterAssociate(com.azure.resourcemanager.elastic.ElasticManager manager) {
        manager
            .associateTrafficFilters()
            .associate(
                "myResourceGroup", "myMonitor", "31d91b5afb6f4c2eaaf104c97b1991dd", com.azure.core.util.Context.NONE);
    }
}
```

### CreateAndAssociateIpFilter_Create

```java
/** Samples for CreateAndAssociateIpFilter Create. */
public final class CreateAndAssociateIpFilterCreateSamples {
    /*
     * x-ms-original-file: specification/elastic/resource-manager/Microsoft.Elastic/preview/2023-02-01-preview/examples/IPTrafficFilter_Create.json
     */
    /**
     * Sample code: createAndAssociateIPFilter_Create.
     *
     * @param manager Entry point to ElasticManager.
     */
    public static void createAndAssociateIPFilterCreate(com.azure.resourcemanager.elastic.ElasticManager manager) {
        manager
            .createAndAssociateIpFilters()
            .create(
                "myResourceGroup",
                "myMonitor",
                "192.168.131.0, 192.168.132.6/22",
                null,
                com.azure.core.util.Context.NONE);
    }
}
```

### CreateAndAssociatePLFilter_Create

```java
/** Samples for CreateAndAssociatePLFilter Create. */
public final class CreateAndAssociatePLFilterCreateSamples {
    /*
     * x-ms-original-file: specification/elastic/resource-manager/Microsoft.Elastic/preview/2023-02-01-preview/examples/PrivateLinkTrafficFilters_Create.json
     */
    /**
     * Sample code: createAndAssociatePLFilter_Create.
     *
     * @param manager Entry point to ElasticManager.
     */
    public static void createAndAssociatePLFilterCreate(com.azure.resourcemanager.elastic.ElasticManager manager) {
        manager
            .createAndAssociatePLFilters()
            .create(
                "myResourceGroup",
                "myMonitor",
                null,
                "fdb54d3b-e85e-4d08-8958-0d2f7g523df9",
                "myPrivateEndpoint",
                com.azure.core.util.Context.NONE);
    }
}
```

### DeploymentInfo_List

```java
/** Samples for DeploymentInfo List. */
public final class DeploymentInfoListSamples {
    /*
     * x-ms-original-file: specification/elastic/resource-manager/Microsoft.Elastic/preview/2023-02-01-preview/examples/DeploymentInfo_List.json
     */
    /**
     * Sample code: DeploymentInfo_List.
     *
     * @param manager Entry point to ElasticManager.
     */
    public static void deploymentInfoList(com.azure.resourcemanager.elastic.ElasticManager manager) {
        manager.deploymentInfoes().listWithResponse("myResourceGroup", "myMonitor", com.azure.core.util.Context.NONE);
    }
}
```

### DetachAndDeleteTrafficFilter_Delete

```java
/** Samples for DetachAndDeleteTrafficFilter Delete. */
public final class DetachAndDeleteTrafficFilterDeleteSamples {
    /*
     * x-ms-original-file: specification/elastic/resource-manager/Microsoft.Elastic/preview/2023-02-01-preview/examples/DetachAndDeleteTrafficFilter_Delete.json
     */
    /**
     * Sample code: DetachAndDeleteTrafficFilter_Delete.
     *
     * @param manager Entry point to ElasticManager.
     */
    public static void detachAndDeleteTrafficFilterDelete(com.azure.resourcemanager.elastic.ElasticManager manager) {
        manager
            .detachAndDeleteTrafficFilters()
            .deleteWithResponse(
                "myResourceGroup", "myMonitor", "31d91b5afb6f4c2eaaf104c97b1991dd", com.azure.core.util.Context.NONE);
    }
}
```

### DetachTrafficFilter_Update

```java
/** Samples for DetachTrafficFilter Update. */
public final class DetachTrafficFilterUpdateSamples {
    /*
     * x-ms-original-file: specification/elastic/resource-manager/Microsoft.Elastic/preview/2023-02-01-preview/examples/DetachTrafficFilters_Update.json
     */
    /**
     * Sample code: DetachTrafficFilter_Update.
     *
     * @param manager Entry point to ElasticManager.
     */
    public static void detachTrafficFilterUpdate(com.azure.resourcemanager.elastic.ElasticManager manager) {
        manager
            .detachTrafficFilters()
            .update(
                "myResourceGroup", "myMonitor", "31d91b5afb6f4c2eaaf104c97b1991dd", com.azure.core.util.Context.NONE);
    }
}
```

### ElasticVersions_List

```java
/** Samples for ElasticVersions List. */
public final class ElasticVersionsListSamples {
    /*
     * x-ms-original-file: specification/elastic/resource-manager/Microsoft.Elastic/preview/2023-02-01-preview/examples/ElasticVersions_List.json
     */
    /**
     * Sample code: ElasticVersions_List.
     *
     * @param manager Entry point to ElasticManager.
     */
    public static void elasticVersionsList(com.azure.resourcemanager.elastic.ElasticManager manager) {
        manager.elasticVersions().listWithResponse("myregion", com.azure.core.util.Context.NONE);
    }
}
```

### ExternalUser_CreateOrUpdate

```java
/** Samples for ExternalUser CreateOrUpdate. */
public final class ExternalUserCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/elastic/resource-manager/Microsoft.Elastic/preview/2023-02-01-preview/examples/ExternalUserInfo.json
     */
    /**
     * Sample code: ExternalUser_CreateOrUpdate.
     *
     * @param manager Entry point to ElasticManager.
     */
    public static void externalUserCreateOrUpdate(com.azure.resourcemanager.elastic.ElasticManager manager) {
        manager
            .externalUsers()
            .createOrUpdateWithResponse("myResourceGroup", "myMonitor", null, com.azure.core.util.Context.NONE);
    }
}
```

### ListAssociatedTrafficFilters_List

```java
/** Samples for ListAssociatedTrafficFilters List. */
public final class ListAssociatedTrafficFiltersListSamples {
    /*
     * x-ms-original-file: specification/elastic/resource-manager/Microsoft.Elastic/preview/2023-02-01-preview/examples/AssociatedFiltersForDeployment_list.json
     */
    /**
     * Sample code: listAssociatedTrafficFilters_list.
     *
     * @param manager Entry point to ElasticManager.
     */
    public static void listAssociatedTrafficFiltersList(com.azure.resourcemanager.elastic.ElasticManager manager) {
        manager
            .listAssociatedTrafficFilters()
            .listWithResponse("myResourceGroup", "myMonitor", com.azure.core.util.Context.NONE);
    }
}
```

### MonitorOperation_Upgrade

```java
/** Samples for MonitorOperation Upgrade. */
public final class MonitorOperationUpgradeSamples {
    /*
     * x-ms-original-file: specification/elastic/resource-manager/Microsoft.Elastic/preview/2023-02-01-preview/examples/Monitor_Upgrade.json
     */
    /**
     * Sample code: Monitor_Upgrade.
     *
     * @param manager Entry point to ElasticManager.
     */
    public static void monitorUpgrade(com.azure.resourcemanager.elastic.ElasticManager manager) {
        manager.monitorOperations().upgrade("myResourceGroup", "myMonitor", null, com.azure.core.util.Context.NONE);
    }
}
```

### MonitoredResources_List

```java
/** Samples for MonitoredResources List. */
public final class MonitoredResourcesListSamples {
    /*
     * x-ms-original-file: specification/elastic/resource-manager/Microsoft.Elastic/preview/2023-02-01-preview/examples/MonitoredResources_List.json
     */
    /**
     * Sample code: MonitoredResources_List.
     *
     * @param manager Entry point to ElasticManager.
     */
    public static void monitoredResourcesList(com.azure.resourcemanager.elastic.ElasticManager manager) {
        manager.monitoredResources().list("myResourceGroup", "myMonitor", com.azure.core.util.Context.NONE);
    }
}
```

### Monitors_Create

```java
/** Samples for Monitors Create. */
public final class MonitorsCreateSamples {
    /*
     * x-ms-original-file: specification/elastic/resource-manager/Microsoft.Elastic/preview/2023-02-01-preview/examples/Monitors_Create.json
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
/** Samples for Monitors Delete. */
public final class MonitorsDeleteSamples {
    /*
     * x-ms-original-file: specification/elastic/resource-manager/Microsoft.Elastic/preview/2023-02-01-preview/examples/Monitors_Delete.json
     */
    /**
     * Sample code: Monitors_Delete.
     *
     * @param manager Entry point to ElasticManager.
     */
    public static void monitorsDelete(com.azure.resourcemanager.elastic.ElasticManager manager) {
        manager.monitors().delete("myResourceGroup", "myMonitor", com.azure.core.util.Context.NONE);
    }
}
```

### Monitors_GetByResourceGroup

```java
/** Samples for Monitors GetByResourceGroup. */
public final class MonitorsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/elastic/resource-manager/Microsoft.Elastic/preview/2023-02-01-preview/examples/Monitors_Get.json
     */
    /**
     * Sample code: Monitors_Get.
     *
     * @param manager Entry point to ElasticManager.
     */
    public static void monitorsGet(com.azure.resourcemanager.elastic.ElasticManager manager) {
        manager
            .monitors()
            .getByResourceGroupWithResponse("myResourceGroup", "myMonitor", com.azure.core.util.Context.NONE);
    }
}
```

### Monitors_List

```java
/** Samples for Monitors List. */
public final class MonitorsListSamples {
    /*
     * x-ms-original-file: specification/elastic/resource-manager/Microsoft.Elastic/preview/2023-02-01-preview/examples/Monitors_List.json
     */
    /**
     * Sample code: Monitors_List.
     *
     * @param manager Entry point to ElasticManager.
     */
    public static void monitorsList(com.azure.resourcemanager.elastic.ElasticManager manager) {
        manager.monitors().list(com.azure.core.util.Context.NONE);
    }
}
```

### Monitors_ListByResourceGroup

```java
/** Samples for Monitors ListByResourceGroup. */
public final class MonitorsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/elastic/resource-manager/Microsoft.Elastic/preview/2023-02-01-preview/examples/Monitors_ListByResourceGroup.json
     */
    /**
     * Sample code: Monitors_ListByResourceGroup.
     *
     * @param manager Entry point to ElasticManager.
     */
    public static void monitorsListByResourceGroup(com.azure.resourcemanager.elastic.ElasticManager manager) {
        manager.monitors().listByResourceGroup("myResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### Monitors_Update

```java
import com.azure.resourcemanager.elastic.models.ElasticMonitorResource;

/** Samples for Monitors Update. */
public final class MonitorsUpdateSamples {
    /*
     * x-ms-original-file: specification/elastic/resource-manager/Microsoft.Elastic/preview/2023-02-01-preview/examples/Monitors_Update.json
     */
    /**
     * Sample code: Monitors_Update.
     *
     * @param manager Entry point to ElasticManager.
     */
    public static void monitorsUpdate(com.azure.resourcemanager.elastic.ElasticManager manager) {
        ElasticMonitorResource resource =
            manager
                .monitors()
                .getByResourceGroupWithResponse("myResourceGroup", "myMonitor", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().apply();
    }
}
```

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/elastic/resource-manager/Microsoft.Elastic/preview/2023-02-01-preview/examples/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     *
     * @param manager Entry point to ElasticManager.
     */
    public static void operationsList(com.azure.resourcemanager.elastic.ElasticManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Organizations_GetApiKey

```java
/** Samples for Organizations GetApiKey. */
public final class OrganizationsGetApiKeySamples {
    /*
     * x-ms-original-file: specification/elastic/resource-manager/Microsoft.Elastic/preview/2023-02-01-preview/examples/Organizations_GetApiKey.json
     */
    /**
     * Sample code: Organizations_GetApiKey.
     *
     * @param manager Entry point to ElasticManager.
     */
    public static void organizationsGetApiKey(com.azure.resourcemanager.elastic.ElasticManager manager) {
        manager.organizations().getApiKeyWithResponse(null, com.azure.core.util.Context.NONE);
    }
}
```

### TagRules_CreateOrUpdate

```java
/** Samples for TagRules CreateOrUpdate. */
public final class TagRulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/elastic/resource-manager/Microsoft.Elastic/preview/2023-02-01-preview/examples/TagRules_CreateOrUpdate.json
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
/** Samples for TagRules Delete. */
public final class TagRulesDeleteSamples {
    /*
     * x-ms-original-file: specification/elastic/resource-manager/Microsoft.Elastic/preview/2023-02-01-preview/examples/TagRules_Delete.json
     */
    /**
     * Sample code: TagRules_Delete.
     *
     * @param manager Entry point to ElasticManager.
     */
    public static void tagRulesDelete(com.azure.resourcemanager.elastic.ElasticManager manager) {
        manager.tagRules().delete("myResourceGroup", "myMonitor", "default", com.azure.core.util.Context.NONE);
    }
}
```

### TagRules_Get

```java
/** Samples for TagRules Get. */
public final class TagRulesGetSamples {
    /*
     * x-ms-original-file: specification/elastic/resource-manager/Microsoft.Elastic/preview/2023-02-01-preview/examples/TagRules_Get.json
     */
    /**
     * Sample code: TagRules_Get.
     *
     * @param manager Entry point to ElasticManager.
     */
    public static void tagRulesGet(com.azure.resourcemanager.elastic.ElasticManager manager) {
        manager.tagRules().getWithResponse("myResourceGroup", "myMonitor", "default", com.azure.core.util.Context.NONE);
    }
}
```

### TagRules_List

```java
/** Samples for TagRules List. */
public final class TagRulesListSamples {
    /*
     * x-ms-original-file: specification/elastic/resource-manager/Microsoft.Elastic/preview/2023-02-01-preview/examples/TagRules_List.json
     */
    /**
     * Sample code: TagRules_List.
     *
     * @param manager Entry point to ElasticManager.
     */
    public static void tagRulesList(com.azure.resourcemanager.elastic.ElasticManager manager) {
        manager.tagRules().list("myResourceGroup", "myMonitor", com.azure.core.util.Context.NONE);
    }
}
```

### TrafficFilters_Delete

```java
/** Samples for TrafficFilters Delete. */
public final class TrafficFiltersDeleteSamples {
    /*
     * x-ms-original-file: specification/elastic/resource-manager/Microsoft.Elastic/preview/2023-02-01-preview/examples/TrafficFilters_Delete.json
     */
    /**
     * Sample code: TrafficFilters_Delete.
     *
     * @param manager Entry point to ElasticManager.
     */
    public static void trafficFiltersDelete(com.azure.resourcemanager.elastic.ElasticManager manager) {
        manager
            .trafficFilters()
            .deleteWithResponse(
                "myResourceGroup", "myMonitor", "31d91b5afb6f4c2eaaf104c97b1991dd", com.azure.core.util.Context.NONE);
    }
}
```

### UpgradableVersions_Details

```java
/** Samples for UpgradableVersions Details. */
public final class UpgradableVersionsDetailsSamples {
    /*
     * x-ms-original-file: specification/elastic/resource-manager/Microsoft.Elastic/preview/2023-02-01-preview/examples/UpgradableVersions_Details.json
     */
    /**
     * Sample code: UpgradableVersions_Details.
     *
     * @param manager Entry point to ElasticManager.
     */
    public static void upgradableVersionsDetails(com.azure.resourcemanager.elastic.ElasticManager manager) {
        manager
            .upgradableVersions()
            .detailsWithResponse("myResourceGroup", "myMonitor", com.azure.core.util.Context.NONE);
    }
}
```

### VMCollection_Update

```java
/** Samples for VMCollection Update. */
public final class VMCollectionUpdateSamples {
    /*
     * x-ms-original-file: specification/elastic/resource-manager/Microsoft.Elastic/preview/2023-02-01-preview/examples/VMCollection_Update.json
     */
    /**
     * Sample code: VMCollection_Update.
     *
     * @param manager Entry point to ElasticManager.
     */
    public static void vMCollectionUpdate(com.azure.resourcemanager.elastic.ElasticManager manager) {
        manager
            .vMCollections()
            .updateWithResponse("myResourceGroup", "myMonitor", null, com.azure.core.util.Context.NONE);
    }
}
```

### VMHost_List

```java
/** Samples for VMHost List. */
public final class VMHostListSamples {
    /*
     * x-ms-original-file: specification/elastic/resource-manager/Microsoft.Elastic/preview/2023-02-01-preview/examples/VMHost_List.json
     */
    /**
     * Sample code: VMHost_List.
     *
     * @param manager Entry point to ElasticManager.
     */
    public static void vMHostList(com.azure.resourcemanager.elastic.ElasticManager manager) {
        manager.vMHosts().list("myResourceGroup", "myMonitor", com.azure.core.util.Context.NONE);
    }
}
```

### VMIngestion_Details

```java
/** Samples for VMIngestion Details. */
public final class VMIngestionDetailsSamples {
    /*
     * x-ms-original-file: specification/elastic/resource-manager/Microsoft.Elastic/preview/2023-02-01-preview/examples/VMIngestion_Details.json
     */
    /**
     * Sample code: VMIngestion_Details.
     *
     * @param manager Entry point to ElasticManager.
     */
    public static void vMIngestionDetails(com.azure.resourcemanager.elastic.ElasticManager manager) {
        manager.vMIngestions().detailsWithResponse("myResourceGroup", "myMonitor", com.azure.core.util.Context.NONE);
    }
}
```

