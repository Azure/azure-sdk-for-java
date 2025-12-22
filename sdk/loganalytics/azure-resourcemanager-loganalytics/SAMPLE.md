# Code snippets and samples


## AvailableServiceTiers

- [ListByWorkspace](#availableservicetiers_listbyworkspace)

## Clusters

- [CreateOrUpdate](#clusters_createorupdate)
- [Delete](#clusters_delete)
- [GetByResourceGroup](#clusters_getbyresourcegroup)
- [List](#clusters_list)
- [ListByResourceGroup](#clusters_listbyresourcegroup)
- [Update](#clusters_update)

## DataExports

- [CreateOrUpdate](#dataexports_createorupdate)
- [Delete](#dataexports_delete)
- [Get](#dataexports_get)
- [ListByWorkspace](#dataexports_listbyworkspace)

## DataSources

- [CreateOrUpdate](#datasources_createorupdate)
- [Delete](#datasources_delete)
- [Get](#datasources_get)
- [ListByWorkspace](#datasources_listbyworkspace)

## DeletedWorkspaces

- [List](#deletedworkspaces_list)
- [ListByResourceGroup](#deletedworkspaces_listbyresourcegroup)

## Gateways

- [Delete](#gateways_delete)

## IntelligencePacks

- [Disable](#intelligencepacks_disable)
- [Enable](#intelligencepacks_enable)
- [List](#intelligencepacks_list)

## LinkedServices

- [CreateOrUpdate](#linkedservices_createorupdate)
- [Delete](#linkedservices_delete)
- [Get](#linkedservices_get)
- [ListByWorkspace](#linkedservices_listbyworkspace)

## LinkedStorageAccounts

- [CreateOrUpdate](#linkedstorageaccounts_createorupdate)
- [Delete](#linkedstorageaccounts_delete)
- [Get](#linkedstorageaccounts_get)
- [ListByWorkspace](#linkedstorageaccounts_listbyworkspace)

## ManagementGroups

- [List](#managementgroups_list)

## OperationStatuses

- [Get](#operationstatuses_get)

## Operations

- [List](#operations_list)

## Queries

- [Delete](#queries_delete)
- [Get](#queries_get)
- [List](#queries_list)
- [Put](#queries_put)
- [Search](#queries_search)
- [Update](#queries_update)

## QueryPacks

- [CreateOrUpdate](#querypacks_createorupdate)
- [CreateOrUpdateWithoutName](#querypacks_createorupdatewithoutname)
- [Delete](#querypacks_delete)
- [GetByResourceGroup](#querypacks_getbyresourcegroup)
- [List](#querypacks_list)
- [ListByResourceGroup](#querypacks_listbyresourcegroup)
- [UpdateTags](#querypacks_updatetags)

## SavedSearches

- [CreateOrUpdate](#savedsearches_createorupdate)
- [Delete](#savedsearches_delete)
- [Get](#savedsearches_get)
- [ListByWorkspace](#savedsearches_listbyworkspace)

## Schema

- [Get](#schema_get)

## SharedKeysOperation

- [GetSharedKeys](#sharedkeysoperation_getsharedkeys)
- [Regenerate](#sharedkeysoperation_regenerate)

## StorageInsightConfigs

- [CreateOrUpdate](#storageinsightconfigs_createorupdate)
- [Delete](#storageinsightconfigs_delete)
- [Get](#storageinsightconfigs_get)
- [ListByWorkspace](#storageinsightconfigs_listbyworkspace)

## SummaryLogsOperation

- [CreateOrUpdate](#summarylogsoperation_createorupdate)
- [Delete](#summarylogsoperation_delete)
- [Get](#summarylogsoperation_get)
- [ListByWorkspace](#summarylogsoperation_listbyworkspace)
- [RetryBin](#summarylogsoperation_retrybin)
- [Start](#summarylogsoperation_start)
- [Stop](#summarylogsoperation_stop)

## Tables

- [CancelSearch](#tables_cancelsearch)
- [CreateOrUpdate](#tables_createorupdate)
- [Delete](#tables_delete)
- [Get](#tables_get)
- [ListByWorkspace](#tables_listbyworkspace)
- [Migrate](#tables_migrate)
- [Update](#tables_update)

## Usages

- [List](#usages_list)

## WorkspacePurge

- [GetPurgeStatus](#workspacepurge_getpurgestatus)
- [Purge](#workspacepurge_purge)

## Workspaces

- [CreateOrUpdate](#workspaces_createorupdate)
- [Delete](#workspaces_delete)
- [Failback](#workspaces_failback)
- [Failover](#workspaces_failover)
- [GetByResourceGroup](#workspaces_getbyresourcegroup)
- [GetNsp](#workspaces_getnsp)
- [List](#workspaces_list)
- [ListByResourceGroup](#workspaces_listbyresourcegroup)
- [ListNsp](#workspaces_listnsp)
- [ReconcileNsp](#workspaces_reconcilensp)
- [Update](#workspaces_update)
### AvailableServiceTiers_ListByWorkspace

```java
/**
 * Samples for AvailableServiceTiers ListByWorkspace.
 */
public final class AvailableServiceTiersListByWorkspaceSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/WorkspacesAvailableServiceTiers.json
     */
    /**
     * Sample code: AvailableServiceTiers.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void availableServiceTiers(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.availableServiceTiers()
            .listByWorkspaceWithResponse("rg1", "workspace1", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_CreateOrUpdate

```java
import com.azure.resourcemanager.loganalytics.models.ClusterSku;
import com.azure.resourcemanager.loganalytics.models.ClusterSkuNameEnum;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Clusters CreateOrUpdate.
 */
public final class ClustersCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/ClustersCreate.json
     */
    /**
     * Sample code: ClustersCreate.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void clustersCreate(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.clusters()
            .define("oiautorest6685")
            .withRegion("eastus")
            .withExistingResourceGroup("oiautorest6685")
            .withTags(mapOf("tag1", "val1"))
            .withSku(new ClusterSku().withCapacity(100L).withName(ClusterSkuNameEnum.CAPACITY_RESERVATION))
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

### Clusters_Delete

```java
/**
 * Samples for Clusters Delete.
 */
public final class ClustersDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/ClustersDelete.json
     */
    /**
     * Sample code: ClustersDelete.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void clustersDelete(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.clusters().delete("oiautorest6685", "oiautorest6685", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_GetByResourceGroup

```java
/**
 * Samples for Clusters GetByResourceGroup.
 */
public final class ClustersGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/ClustersGet.json
     */
    /**
     * Sample code: ClustersGet.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void clustersGet(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.clusters()
            .getByResourceGroupWithResponse("oiautorest6685", "oiautorest6685", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_List

```java
/**
 * Samples for Clusters List.
 */
public final class ClustersListSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/ClustersSubscriptionList.json
     */
    /**
     * Sample code: ClustersSubscriptionList.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void clustersSubscriptionList(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.clusters().list(com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_ListByResourceGroup

```java
/**
 * Samples for Clusters ListByResourceGroup.
 */
public final class ClustersListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/ClustersListByResourceGroup.json
     */
    /**
     * Sample code: ClustersGet.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void clustersGet(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.clusters().listByResourceGroup("oiautorest6685", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_Update

```java
import com.azure.resourcemanager.loganalytics.models.Cluster;
import com.azure.resourcemanager.loganalytics.models.ClusterSku;
import com.azure.resourcemanager.loganalytics.models.ClusterSkuNameEnum;
import com.azure.resourcemanager.loganalytics.models.KeyVaultProperties;
import com.azure.resourcemanager.loganalytics.models.ManagedServiceIdentity;
import com.azure.resourcemanager.loganalytics.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.loganalytics.models.UserAssignedIdentity;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Clusters Update.
 */
public final class ClustersUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/ClustersUpdate.json
     */
    /**
     * Sample code: ClustersPatch.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void clustersPatch(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        Cluster resource = manager.clusters()
            .getByResourceGroupWithResponse("oiautorest6685", "oiautorest6685", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("tag1", "val1"))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/53bc36c5-91e1-4d09-92c9-63b89e571926/resourcegroups/oiautorest6685/providers/Microsoft.ManagedIdentity/userAssignedIdentities/myidentity",
                    new UserAssignedIdentity())))
            .withSku(new ClusterSku().withCapacity(1000L).withName(ClusterSkuNameEnum.CAPACITY_RESERVATION))
            .withKeyVaultProperties(new KeyVaultProperties().withKeyVaultUri("fakeTokenPlaceholder")
                .withKeyName("fakeTokenPlaceholder")
                .withKeyVersion("fakeTokenPlaceholder")
                .withKeyRsaSize(1024))
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

### DataExports_CreateOrUpdate

```java
import java.util.Arrays;

/**
 * Samples for DataExports CreateOrUpdate.
 */
public final class DataExportsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/DataExportCreateOrUpdate.json
     */
    /**
     * Sample code: DataExportCreate.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void dataExportCreate(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.dataExports()
            .define("export1")
            .withExistingWorkspace("RgTest1", "DeWnTest1234")
            .withTableNames(Arrays.asList("Heartbeat"))
            .withResourceId(
                "/subscriptions/192b9f85-a39a-4276-b96d-d5cd351703f9/resourceGroups/OIAutoRest1234/providers/Microsoft.EventHub/namespaces/test")
            .create();
    }
}
```

### DataExports_Delete

```java
/**
 * Samples for DataExports Delete.
 */
public final class DataExportsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/DataExportDelete.json
     */
    /**
     * Sample code: DataExportDelete.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void dataExportDelete(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.dataExports()
            .deleteWithResponse("RgTest1", "DeWnTest1234", "export1", com.azure.core.util.Context.NONE);
    }
}
```

### DataExports_Get

```java
/**
 * Samples for DataExports Get.
 */
public final class DataExportsGetSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/DataExportGet.json
     */
    /**
     * Sample code: DataExportGet.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void dataExportGet(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.dataExports().getWithResponse("RgTest1", "DeWnTest1234", "export1", com.azure.core.util.Context.NONE);
    }
}
```

### DataExports_ListByWorkspace

```java
/**
 * Samples for DataExports ListByWorkspace.
 */
public final class DataExportsListByWorkspaceSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/DataExportListByWorkspace.json
     */
    /**
     * Sample code: DataExportGet.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void dataExportGet(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.dataExports().listByWorkspace("RgTest1", "DeWnTest1234", com.azure.core.util.Context.NONE);
    }
}
```

### DataSources_CreateOrUpdate

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.loganalytics.models.DataSourceKind;
import java.io.IOException;

/**
 * Samples for DataSources CreateOrUpdate.
 */
public final class DataSourcesCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/DataSourcesCreate.json
     */
    /**
     * Sample code: DataSourcesCreate.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void dataSourcesCreate(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager)
        throws IOException {
        manager.dataSources()
            .define("AzTestDS774")
            .withExistingWorkspace("OIAutoRest5123", "AzTest9724")
            .withProperties(SerializerFactory.createDefaultManagementSerializerAdapter()
                .deserialize(
                    "{\"LinkedResourceId\":\"/subscriptions/00000000-0000-0000-0000-00000000000/providers/microsoft.insights/eventtypes/management\"}",
                    Object.class, SerializerEncoding.JSON))
            .withKind(DataSourceKind.AZURE_ACTIVITY_LOG)
            .create();
    }
}
```

### DataSources_Delete

```java
/**
 * Samples for DataSources Delete.
 */
public final class DataSourcesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/DataSourcesDelete.json
     */
    /**
     * Sample code: DataSourcesDelete.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void dataSourcesDelete(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.dataSources()
            .deleteWithResponse("OIAutoRest5123", "AzTest9724", "AzTestDS774", com.azure.core.util.Context.NONE);
    }
}
```

### DataSources_Get

```java
/**
 * Samples for DataSources Get.
 */
public final class DataSourcesGetSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/DataSourcesGet.json
     */
    /**
     * Sample code: DataSourcesGet.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void dataSourcesGet(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.dataSources()
            .getWithResponse("OIAutoRest5123", "AzTest9724", "AzTestDS774", com.azure.core.util.Context.NONE);
    }
}
```

### DataSources_ListByWorkspace

```java
/**
 * Samples for DataSources ListByWorkspace.
 */
public final class DataSourcesListByWorkspaceSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/DataSourcesListByWorkspace.json
     */
    /**
     * Sample code: DataSourcesListByWorkspace.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void dataSourcesListByWorkspace(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.dataSources()
            .listByWorkspace("OIAutoRest5123", "AzTest9724", "kind='WindowsEvent'", null,
                com.azure.core.util.Context.NONE);
    }
}
```

### DeletedWorkspaces_List

```java
/**
 * Samples for DeletedWorkspaces List.
 */
public final class DeletedWorkspacesListSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/WorkspacesSubscriptionList.json
     */
    /**
     * Sample code: WorkspacesSubscriptionList.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void workspacesSubscriptionList(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.deletedWorkspaces().list(com.azure.core.util.Context.NONE);
    }
}
```

### DeletedWorkspaces_ListByResourceGroup

```java
/**
 * Samples for DeletedWorkspaces ListByResourceGroup.
 */
public final class DeletedWorkspacesListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/WorkspacesListByResourceGroup.json
     */
    /**
     * Sample code: WorkspacesGet.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void workspacesGet(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.deletedWorkspaces().listByResourceGroup("oiautorest6685", com.azure.core.util.Context.NONE);
    }
}
```

### Gateways_Delete

```java
/**
 * Samples for Gateways Delete.
 */
public final class GatewaysDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/WorkspacesGatewaysDelete.json
     */
    /**
     * Sample code: DeleteGateways.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void deleteGateways(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.gateways()
            .deleteWithResponse("OIAutoRest5123", "aztest5048", "00000000-0000-0000-0000-00000000000",
                com.azure.core.util.Context.NONE);
    }
}
```

### IntelligencePacks_Disable

```java
/**
 * Samples for IntelligencePacks Disable.
 */
public final class IntelligencePacksDisableSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/WorkspacesDisableIntelligencePack.json
     */
    /**
     * Sample code: IntelligencePacksDisable.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void intelligencePacksDisable(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.intelligencePacks()
            .disableWithResponse("rg1", "TestLinkWS", "ChangeTracking", com.azure.core.util.Context.NONE);
    }
}
```

### IntelligencePacks_Enable

```java
/**
 * Samples for IntelligencePacks Enable.
 */
public final class IntelligencePacksEnableSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/WorkspacesEnableIntelligencePack.json
     */
    /**
     * Sample code: IntelligencePacksEnable.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void intelligencePacksEnable(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.intelligencePacks()
            .enableWithResponse("rg1", "TestLinkWS", "ChangeTracking", com.azure.core.util.Context.NONE);
    }
}
```

### IntelligencePacks_List

```java
/**
 * Samples for IntelligencePacks List.
 */
public final class IntelligencePacksListSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/WorkspacesListIntelligencePacks.json
     */
    /**
     * Sample code: IntelligencePacksList.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void intelligencePacksList(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.intelligencePacks().listWithResponse("rg1", "TestLinkWS", com.azure.core.util.Context.NONE);
    }
}
```

### LinkedServices_CreateOrUpdate

```java
/**
 * Samples for LinkedServices CreateOrUpdate.
 */
public final class LinkedServicesCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/LinkedServicesCreate.json
     */
    /**
     * Sample code: LinkedServicesCreate.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void linkedServicesCreate(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.linkedServices()
            .define("Cluster")
            .withExistingWorkspace("mms-eus", "TestLinkWS")
            .withWriteAccessResourceId(
                "/subscriptions/00000000-0000-0000-0000-00000000000/resourceGroups/mms-eus/providers/Microsoft.OperationalInsights/clusters/testcluster")
            .create();
    }
}
```

### LinkedServices_Delete

```java
/**
 * Samples for LinkedServices Delete.
 */
public final class LinkedServicesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/LinkedServicesDelete.json
     */
    /**
     * Sample code: LinkedServicesDelete.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void linkedServicesDelete(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.linkedServices().delete("rg1", "TestLinkWS", "Cluster", com.azure.core.util.Context.NONE);
    }
}
```

### LinkedServices_Get

```java
/**
 * Samples for LinkedServices Get.
 */
public final class LinkedServicesGetSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/LinkedServicesGet.json
     */
    /**
     * Sample code: LinkedServicesGet.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void linkedServicesGet(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.linkedServices().getWithResponse("mms-eus", "TestLinkWS", "Cluster", com.azure.core.util.Context.NONE);
    }
}
```

### LinkedServices_ListByWorkspace

```java
/**
 * Samples for LinkedServices ListByWorkspace.
 */
public final class LinkedServicesListByWorkspaceSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/LinkedServicesListByWorkspace.json
     */
    /**
     * Sample code: LinkedServicesListByWorkspace.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void
        linkedServicesListByWorkspace(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.linkedServices().listByWorkspace("mms-eus", "TestLinkWS", com.azure.core.util.Context.NONE);
    }
}
```

### LinkedStorageAccounts_CreateOrUpdate

```java
import com.azure.resourcemanager.loganalytics.models.DataSourceType;
import java.util.Arrays;

/**
 * Samples for LinkedStorageAccounts CreateOrUpdate.
 */
public final class LinkedStorageAccountsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/LinkedStorageAccountsCreate.json
     */
    /**
     * Sample code: LinkedStorageAccountsCreate.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void linkedStorageAccountsCreate(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.linkedStorageAccounts()
            .define(DataSourceType.CUSTOM_LOGS)
            .withExistingWorkspace("mms-eus", "testLinkStorageAccountsWS")
            .withStorageAccountIds(Arrays.asList(
                "/subscriptions/00000000-0000-0000-0000-00000000000/resourceGroups/mms-eus/providers/Microsoft.Storage/storageAccounts/testStorageA",
                "/subscriptions/00000000-0000-0000-0000-00000000000/resourceGroups/mms-eus/providers/Microsoft.Storage/storageAccounts/testStorageB"))
            .create();
    }
}
```

### LinkedStorageAccounts_Delete

```java
import com.azure.resourcemanager.loganalytics.models.DataSourceType;

/**
 * Samples for LinkedStorageAccounts Delete.
 */
public final class LinkedStorageAccountsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/LinkedStorageAccountsDelete.json
     */
    /**
     * Sample code: LinkedStorageAccountsDelete.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void linkedStorageAccountsDelete(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.linkedStorageAccounts()
            .deleteWithResponse("mms-eus", "testLinkStorageAccountsWS", DataSourceType.CUSTOM_LOGS,
                com.azure.core.util.Context.NONE);
    }
}
```

### LinkedStorageAccounts_Get

```java
import com.azure.resourcemanager.loganalytics.models.DataSourceType;

/**
 * Samples for LinkedStorageAccounts Get.
 */
public final class LinkedStorageAccountsGetSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/LinkedStorageAccountsGet.json
     */
    /**
     * Sample code: LinkedStorageAccountsGet.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void linkedStorageAccountsGet(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.linkedStorageAccounts()
            .getWithResponse("mms-eus", "testLinkStorageAccountsWS", DataSourceType.CUSTOM_LOGS,
                com.azure.core.util.Context.NONE);
    }
}
```

### LinkedStorageAccounts_ListByWorkspace

```java
/**
 * Samples for LinkedStorageAccounts ListByWorkspace.
 */
public final class LinkedStorageAccountsListByWorkspaceSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/LinkedStorageAccountsListByWorkspace.json
     */
    /**
     * Sample code: Gets list of linked storage accounts on a workspace.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void getsListOfLinkedStorageAccountsOnAWorkspace(
        com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.linkedStorageAccounts()
            .listByWorkspace("mms-eus", "testLinkStorageAccountsWS", com.azure.core.util.Context.NONE);
    }
}
```

### ManagementGroups_List

```java
/**
 * Samples for ManagementGroups List.
 */
public final class ManagementGroupsListSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/WorkspacesListManagementGroups.json
     */
    /**
     * Sample code: WorkspacesListManagementGroups.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void
        workspacesListManagementGroups(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.managementGroups().list("rg1", "TestLinkWS", com.azure.core.util.Context.NONE);
    }
}
```

### OperationStatuses_Get

```java
/**
 * Samples for OperationStatuses Get.
 */
public final class OperationStatusesGetSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/OperationStatusesGet.json
     */
    /**
     * Sample code: Get specific operation status.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void getSpecificOperationStatus(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.operationStatuses()
            .getWithResponse("West US", "713192d7-503f-477a-9cfe-4efc3ee2bd11", com.azure.core.util.Context.NONE);
    }
}
```

### Operations_List

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/OperationsListByTenant.json
     */
    /**
     * Sample code: Get specific operation status.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void getSpecificOperationStatus(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Queries_Delete

```java
/**
 * Samples for Queries Delete.
 */
public final class QueriesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/QueryPackQueriesDelete.json
     */
    /**
     * Sample code: QueryDelete.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void queryDelete(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.queries()
            .deleteWithResponse("my-resource-group", "my-querypack", "a449f8af-8e64-4b3a-9b16-5a7165ff98c4",
                com.azure.core.util.Context.NONE);
    }
}
```

### Queries_Get

```java
/**
 * Samples for Queries Get.
 */
public final class QueriesGetSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/QueryPackQueriesGet.json
     */
    /**
     * Sample code: QueryGet.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void queryGet(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.queries()
            .getWithResponse("my-resource-group", "my-querypack", "a449f8af-8e64-4b3a-9b16-5a7165ff98c4",
                com.azure.core.util.Context.NONE);
    }
}
```

### Queries_List

```java
/**
 * Samples for Queries List.
 */
public final class QueriesListSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/QueryPackQueriesList.json
     */
    /**
     * Sample code: QueryList.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void queryList(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.queries().list("my-resource-group", "my-querypack", null, true, null, com.azure.core.util.Context.NONE);
    }
}
```

### Queries_Put

```java
import com.azure.resourcemanager.loganalytics.models.LogAnalyticsQueryPackQueryPropertiesRelated;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Queries Put.
 */
public final class QueriesPutSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/QueryPackQueriesPut.json
     */
    /**
     * Sample code: QueryPut.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void queryPut(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.queries()
            .define("a449f8af-8e64-4b3a-9b16-5a7165ff98c4")
            .withExistingQueryPack("my-resource-group", "my-querypack")
            .withTags(mapOf("my-label", Arrays.asList("label1"), "my-other-label", Arrays.asList("label2")))
            .withDisplayName("Exceptions - New in the last 24 hours")
            .withDescription("my description")
            .withBody(
                "let newExceptionsTimeRange = 1d;\nlet timeRangeToCheckBefore = 7d;\nexceptions\n| where timestamp < ago(timeRangeToCheckBefore)\n| summarize count() by problemId\n| join kind= rightanti (\nexceptions\n| where timestamp >= ago(newExceptionsTimeRange)\n| extend stack = tostring(details[0].rawStack)\n| summarize count(), dcount(user_AuthenticatedId), min(timestamp), max(timestamp), any(stack) by problemId  \n) on problemId \n| order by  count_ desc\n")
            .withRelated(new LogAnalyticsQueryPackQueryPropertiesRelated().withCategories(Arrays.asList("analytics")))
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

### Queries_Search

```java
import com.azure.resourcemanager.loganalytics.models.LogAnalyticsQueryPackQuerySearchProperties;
import com.azure.resourcemanager.loganalytics.models.LogAnalyticsQueryPackQuerySearchPropertiesRelated;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Queries Search.
 */
public final class QueriesSearchSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/QueryPackQueriesSearch.json
     */
    /**
     * Sample code: QuerySearch.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void querySearch(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.queries()
            .search("my-resource-group", "my-querypack",
                new LogAnalyticsQueryPackQuerySearchProperties()
                    .withRelated(new LogAnalyticsQueryPackQuerySearchPropertiesRelated()
                        .withCategories(Arrays.asList("other", "analytics")))
                    .withTags(mapOf("my-label", Arrays.asList("label1"))),
                3L, true, null, com.azure.core.util.Context.NONE);
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

### Queries_Update

```java
import com.azure.resourcemanager.loganalytics.models.LogAnalyticsQueryPackQuery;
import com.azure.resourcemanager.loganalytics.models.LogAnalyticsQueryPackQueryPropertiesRelated;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Queries Update.
 */
public final class QueriesUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/QueryPackQueriesUpdate.json
     */
    /**
     * Sample code: QueryPatch.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void queryPatch(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        LogAnalyticsQueryPackQuery resource = manager.queries()
            .getWithResponse("my-resource-group", "my-querypack", "a449f8af-8e64-4b3a-9b16-5a7165ff98c4",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("my-label", Arrays.asList("label1"), "my-other-label", Arrays.asList("label2")))
            .withDisplayName("Exceptions - New in the last 24 hours")
            .withDescription("my description")
            .withBody(
                "let newExceptionsTimeRange = 1d;\nlet timeRangeToCheckBefore = 7d;\nexceptions\n| where timestamp < ago(timeRangeToCheckBefore)\n| summarize count() by problemId\n| join kind= rightanti (\nexceptions\n| where timestamp >= ago(newExceptionsTimeRange)\n| extend stack = tostring(details[0].rawStack)\n| summarize count(), dcount(user_AuthenticatedId), min(timestamp), max(timestamp), any(stack) by problemId  \n) on problemId \n| order by  count_ desc\n")
            .withRelated(new LogAnalyticsQueryPackQueryPropertiesRelated().withCategories(Arrays.asList("analytics")))
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

### QueryPacks_CreateOrUpdate

```java
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for QueryPacks CreateOrUpdate.
 */
public final class QueryPacksCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/QueryPacksCreate.json
     */
    /**
     * Sample code: QueryPackCreate.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void queryPackCreate(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.queryPacks()
            .define("my-querypack")
            .withRegion("South Central US")
            .withExistingResourceGroup("my-resource-group")
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/QueryPacksUpdate.json
     */
    /**
     * Sample code: QueryPackUpdate.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void queryPackUpdate(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.queryPacks()
            .define("my-querypack")
            .withRegion("South Central US")
            .withExistingResourceGroup("my-resource-group")
            .withTags(mapOf("Tag1", "Value1"))
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

### QueryPacks_CreateOrUpdateWithoutName

```java
import com.azure.resourcemanager.loganalytics.fluent.models.LogAnalyticsQueryPackInner;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for QueryPacks CreateOrUpdateWithoutName.
 */
public final class QueryPacksCreateOrUpdateWithoutNameSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/QueryPacksCreateNoName.json
     */
    /**
     * Sample code: QueryPackCreateNoName.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void queryPackCreateNoName(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.queryPacks()
            .createOrUpdateWithoutNameWithResponse("my-resource-group",
                new LogAnalyticsQueryPackInner().withLocation("South Central US"), com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/QueryPackUpdateNoName.json
     */
    /**
     * Sample code: QueryPackUpdateNoName.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void queryPackUpdateNoName(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.queryPacks()
            .createOrUpdateWithoutNameWithResponse("my-resource-group",
                new LogAnalyticsQueryPackInner().withLocation("South Central US").withTags(mapOf("Tag1", "Value1")),
                com.azure.core.util.Context.NONE);
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

### QueryPacks_Delete

```java
/**
 * Samples for QueryPacks Delete.
 */
public final class QueryPacksDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/QueryPacksDelete.json
     */
    /**
     * Sample code: QueryPacksDelete.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void queryPacksDelete(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.queryPacks()
            .deleteByResourceGroupWithResponse("my-resource-group", "my-querypack", com.azure.core.util.Context.NONE);
    }
}
```

### QueryPacks_GetByResourceGroup

```java
/**
 * Samples for QueryPacks GetByResourceGroup.
 */
public final class QueryPacksGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/QueryPacksGet.json
     */
    /**
     * Sample code: QueryPackGet.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void queryPackGet(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.queryPacks()
            .getByResourceGroupWithResponse("my-resource-group", "my-querypack", com.azure.core.util.Context.NONE);
    }
}
```

### QueryPacks_List

```java
/**
 * Samples for QueryPacks List.
 */
public final class QueryPacksListSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/QueryPacksList.json
     */
    /**
     * Sample code: QueryPacksList.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void queryPacksList(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.queryPacks().list(com.azure.core.util.Context.NONE);
    }
}
```

### QueryPacks_ListByResourceGroup

```java
/**
 * Samples for QueryPacks ListByResourceGroup.
 */
public final class QueryPacksListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/QueryPacksListByResourceGroup.json
     */
    /**
     * Sample code: QueryPackListByResourceGroup.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void
        queryPackListByResourceGroup(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.queryPacks().listByResourceGroup("my-resource-group", com.azure.core.util.Context.NONE);
    }
}
```

### QueryPacks_UpdateTags

```java
import com.azure.resourcemanager.loganalytics.models.LogAnalyticsQueryPack;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for QueryPacks UpdateTags.
 */
public final class QueryPacksUpdateTagsSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/QueryPacksUpdateTagsOnly.json
     */
    /**
     * Sample code: QueryPackUpdateTagsOnly.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void queryPackUpdateTagsOnly(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        LogAnalyticsQueryPack resource = manager.queryPacks()
            .getByResourceGroupWithResponse("my-resource-group", "my-querypack", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("Tag1", "Value1", "Tag2", "Value2")).apply();
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

### SavedSearches_CreateOrUpdate

```java
import com.azure.resourcemanager.loganalytics.models.Tag;
import java.util.Arrays;

/**
 * Samples for SavedSearches CreateOrUpdate.
 */
public final class SavedSearchesCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/WorkspacesSavedSearchesCreateOrUpdate.json
     */
    /**
     * Sample code: SavedSearchCreateOrUpdate.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void savedSearchCreateOrUpdate(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.savedSearches()
            .define("00000000-0000-0000-0000-00000000000")
            .withExistingWorkspace("TestRG", "TestWS")
            .withCategory("Saved Search Test Category")
            .withDisplayName("Create or Update Saved Search Test")
            .withQuery("Heartbeat | summarize Count() by Computer | take a")
            .withTags(Arrays.asList(new Tag().withName("Group").withValue("Computer")))
            .withFunctionAlias("heartbeat_func")
            .withFunctionParameters("a:int=1")
            .withVersion(2L)
            .create();
    }
}
```

### SavedSearches_Delete

```java
/**
 * Samples for SavedSearches Delete.
 */
public final class SavedSearchesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/WorkspacesDeleteSavedSearches.json
     */
    /**
     * Sample code: SavedSearchesDelete.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void savedSearchesDelete(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.savedSearches()
            .deleteWithResponse("TestRG", "TestWS", "00000000-0000-0000-0000-00000000000",
                com.azure.core.util.Context.NONE);
    }
}
```

### SavedSearches_Get

```java
/**
 * Samples for SavedSearches Get.
 */
public final class SavedSearchesGetSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/WorkspacesSavedSearchesGet.json
     */
    /**
     * Sample code: SavedSearchesGet.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void savedSearchesGet(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.savedSearches()
            .getWithResponse("TestRG", "TestWS", "00000000-0000-0000-0000-00000000000",
                com.azure.core.util.Context.NONE);
    }
}
```

### SavedSearches_ListByWorkspace

```java
/**
 * Samples for SavedSearches ListByWorkspace.
 */
public final class SavedSearchesListByWorkspaceSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/SavedSearchesListByWorkspace.json
     */
    /**
     * Sample code: SavedSearchesList.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void savedSearchesList(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.savedSearches().listByWorkspaceWithResponse("TestRG", "TestWS", com.azure.core.util.Context.NONE);
    }
}
```

### Schema_Get

```java
/**
 * Samples for Schema Get.
 */
public final class SchemaGetSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/SavedSearchesGetSchema.json
     */
    /**
     * Sample code: WorkspacesGetSchema.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void workspacesGetSchema(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.schemas().getWithResponse("mms-eus", "atlantisdemo", com.azure.core.util.Context.NONE);
    }
}
```

### SharedKeysOperation_GetSharedKeys

```java
/**
 * Samples for SharedKeysOperation GetSharedKeys.
 */
public final class SharedKeysOperationGetSharedKeysSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/WorkspacesGetSharedKeys.json
     */
    /**
     * Sample code: SharedKeysList.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void sharedKeysList(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.sharedKeysOperations().getSharedKeysWithResponse("rg1", "TestLinkWS", com.azure.core.util.Context.NONE);
    }
}
```

### SharedKeysOperation_Regenerate

```java
/**
 * Samples for SharedKeysOperation Regenerate.
 */
public final class SharedKeysOperationRegenerateSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/WorkspacesRegenerateSharedKeys.json
     */
    /**
     * Sample code: RegenerateSharedKeys.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void regenerateSharedKeys(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.sharedKeysOperations().regenerateWithResponse("rg1", "workspace1", com.azure.core.util.Context.NONE);
    }
}
```

### StorageInsightConfigs_CreateOrUpdate

```java
import com.azure.resourcemanager.loganalytics.models.StorageAccount;
import java.util.Arrays;

/**
 * Samples for StorageInsightConfigs CreateOrUpdate.
 */
public final class StorageInsightConfigsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/StorageInsightsCreateOrUpdate.json
     */
    /**
     * Sample code: StorageInsightsCreate.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void storageInsightsCreate(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.storageInsightConfigs()
            .define("AzTestSI1110")
            .withExistingWorkspace("OIAutoRest5123", "aztest5048")
            .withContainers(Arrays.asList("wad-iis-logfiles"))
            .withTables(Arrays.asList("WADWindowsEventLogsTable", "LinuxSyslogVer2v0"))
            .withStorageAccount(new StorageAccount().withId(
                "/subscriptions/00000000-0000-0000-0000-000000000005/resourcegroups/OIAutoRest6987/providers/microsoft.storage/storageaccounts/AzTestFakeSA9945")
                .withKey("fakeTokenPlaceholder"))
            .create();
    }
}
```

### StorageInsightConfigs_Delete

```java
/**
 * Samples for StorageInsightConfigs Delete.
 */
public final class StorageInsightConfigsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/StorageInsightsDelete.json
     */
    /**
     * Sample code: StorageInsightsDelete.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void storageInsightsDelete(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.storageInsightConfigs()
            .deleteWithResponse("OIAutoRest5123", "aztest5048", "AzTestSI1110", com.azure.core.util.Context.NONE);
    }
}
```

### StorageInsightConfigs_Get

```java
/**
 * Samples for StorageInsightConfigs Get.
 */
public final class StorageInsightConfigsGetSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/StorageInsightsGet.json
     */
    /**
     * Sample code: StorageInsightsGet.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void storageInsightsGet(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.storageInsightConfigs()
            .getWithResponse("OIAutoRest5123", "aztest5048", "AzTestSI1110", com.azure.core.util.Context.NONE);
    }
}
```

### StorageInsightConfigs_ListByWorkspace

```java
/**
 * Samples for StorageInsightConfigs ListByWorkspace.
 */
public final class StorageInsightConfigsListByWorkspaceSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/StorageInsightsListByWorkspace.json
     */
    /**
     * Sample code: StorageInsightsList.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void storageInsightsList(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.storageInsightConfigs()
            .listByWorkspace("OIAutoRest5123", "aztest5048", com.azure.core.util.Context.NONE);
    }
}
```

### SummaryLogsOperation_CreateOrUpdate

```java
import com.azure.resourcemanager.loganalytics.models.RuleDefinition;
import com.azure.resourcemanager.loganalytics.models.RuleTypeEnum;
import java.time.OffsetDateTime;

/**
 * Samples for SummaryLogsOperation CreateOrUpdate.
 */
public final class SummaryLogsOperationCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/SummaryLogsUpsert.json
     */
    /**
     * Sample code: SummaryLogsUpsert.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void summaryLogsUpsert(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.summaryLogsOperations()
            .define("summarylogs1")
            .withExistingWorkspace("oiautorest6685", "oiautorest6685")
            .withRuleType(RuleTypeEnum.USER)
            .withRuleDefinition(new RuleDefinition().withQuery("MyTable_CL")
                .withBinSize(180)
                .withBinDelay(10)
                .withBinStartTime(OffsetDateTime.parse("2020-02-03T04:05:06Z")))
            .create();
    }
}
```

### SummaryLogsOperation_Delete

```java
/**
 * Samples for SummaryLogsOperation Delete.
 */
public final class SummaryLogsOperationDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/SummaryLogsDelete.json
     */
    /**
     * Sample code: SummaryLogsDelete.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void summaryLogsDelete(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.summaryLogsOperations()
            .delete("oiautorest6685", "oiautorest6685", "summarylogs1", com.azure.core.util.Context.NONE);
    }
}
```

### SummaryLogsOperation_Get

```java
/**
 * Samples for SummaryLogsOperation Get.
 */
public final class SummaryLogsOperationGetSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/SummaryLogsGet.json
     */
    /**
     * Sample code: SummaryLogsGet.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void summaryLogsGet(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.summaryLogsOperations()
            .getWithResponse("oiautorest6685", "oiautorest6685", "summarylogs1", com.azure.core.util.Context.NONE);
    }
}
```

### SummaryLogsOperation_ListByWorkspace

```java
/**
 * Samples for SummaryLogsOperation ListByWorkspace.
 */
public final class SummaryLogsOperationListByWorkspaceSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/SummaryLogsList.json
     */
    /**
     * Sample code: SummaryLogsListByWorkspace.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void summaryLogsListByWorkspace(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.summaryLogsOperations()
            .listByWorkspace("oiautorest6685", "oiautorest6685", com.azure.core.util.Context.NONE);
    }
}
```

### SummaryLogsOperation_RetryBin

```java
import com.azure.resourcemanager.loganalytics.models.SummaryLogsRetryBin;
import com.azure.resourcemanager.loganalytics.models.SummaryLogsRetryBinProperties;
import java.time.OffsetDateTime;

/**
 * Samples for SummaryLogsOperation RetryBin.
 */
public final class SummaryLogsOperationRetryBinSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/SummaryLogsRetryBin.json
     */
    /**
     * Sample code: SummaryLogsRetryBin.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void summaryLogsRetryBin(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.summaryLogsOperations()
            .retryBin("oiautorest6685", "oiautorest6685", "summarylogs1",
                new SummaryLogsRetryBin().withProperties(new SummaryLogsRetryBinProperties()
                    .withRetryBinStartTime(OffsetDateTime.parse("2020-02-03T04:00:00Z"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### SummaryLogsOperation_Start

```java
/**
 * Samples for SummaryLogsOperation Start.
 */
public final class SummaryLogsOperationStartSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/SummaryLogsStart.json
     */
    /**
     * Sample code: SummaryLogsStart.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void summaryLogsStart(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.summaryLogsOperations()
            .start("exampleresourcegroup", "exampleworkspace", "summarylogs3", com.azure.core.util.Context.NONE);
    }
}
```

### SummaryLogsOperation_Stop

```java
/**
 * Samples for SummaryLogsOperation Stop.
 */
public final class SummaryLogsOperationStopSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/SummaryLogsStop.json
     */
    /**
     * Sample code: SummaryLogsStop.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void summaryLogsStop(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.summaryLogsOperations()
            .stopWithResponse("oiautorest6685", "oiautorest6685", "summarylogs1", com.azure.core.util.Context.NONE);
    }
}
```

### Tables_CancelSearch

```java
/**
 * Samples for Tables CancelSearch.
 */
public final class TablesCancelSearchSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/TablesSearchCancel.json
     */
    /**
     * Sample code: TablesSearchCancel.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void tablesSearchCancel(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.tables()
            .cancelSearchWithResponse("oiautorest6685", "oiautorest6685", "table1_SRCH",
                com.azure.core.util.Context.NONE);
    }
}
```

### Tables_CreateOrUpdate

```java
import com.azure.resourcemanager.loganalytics.models.Column;
import com.azure.resourcemanager.loganalytics.models.ColumnTypeEnum;
import com.azure.resourcemanager.loganalytics.models.Schema;
import java.util.Arrays;

/**
 * Samples for Tables CreateOrUpdate.
 */
public final class TablesCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/TablesUpsert.json
     */
    /**
     * Sample code: TablesUpsert.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void tablesUpsert(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.tables()
            .define("AzureNetworkFlow")
            .withExistingWorkspace("oiautorest6685", "oiautorest6685")
            .withRetentionInDays(45)
            .withTotalRetentionInDays(70)
            .withSchema(new Schema().withName("AzureNetworkFlow")
                .withColumns(Arrays.asList(new Column().withName("MyNewColumn").withType(ColumnTypeEnum.GUID))))
            .create();
    }
}
```

### Tables_Delete

```java
/**
 * Samples for Tables Delete.
 */
public final class TablesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/TablesDelete.json
     */
    /**
     * Sample code: TablesDelete.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void tablesDelete(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.tables().delete("oiautorest6685", "oiautorest6685", "table1_CL", com.azure.core.util.Context.NONE);
    }
}
```

### Tables_Get

```java
/**
 * Samples for Tables Get.
 */
public final class TablesGetSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/TablesGet.json
     */
    /**
     * Sample code: TablesGet.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void tablesGet(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.tables()
            .getWithResponse("oiautorest6685", "oiautorest6685", "table1_SRCH", com.azure.core.util.Context.NONE);
    }
}
```

### Tables_ListByWorkspace

```java
/**
 * Samples for Tables ListByWorkspace.
 */
public final class TablesListByWorkspaceSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/TablesList.json
     */
    /**
     * Sample code: TablesListByWorkspace.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void tablesListByWorkspace(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.tables().listByWorkspace("oiautorest6685", "oiautorest6685", com.azure.core.util.Context.NONE);
    }
}
```

### Tables_Migrate

```java
/**
 * Samples for Tables Migrate.
 */
public final class TablesMigrateSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/TablesMigrate.json
     */
    /**
     * Sample code: TablesMigrate.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void tablesMigrate(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.tables()
            .migrateWithResponse("oiautorest6685", "oiautorest6685", "table1_CL", com.azure.core.util.Context.NONE);
    }
}
```

### Tables_Update

```java
import com.azure.resourcemanager.loganalytics.models.Column;
import com.azure.resourcemanager.loganalytics.models.ColumnTypeEnum;
import com.azure.resourcemanager.loganalytics.models.Schema;
import com.azure.resourcemanager.loganalytics.models.Table;
import java.util.Arrays;

/**
 * Samples for Tables Update.
 */
public final class TablesUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/TablesUpsert.json
     */
    /**
     * Sample code: TablesUpsert.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void tablesUpsert(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        Table resource = manager.tables()
            .getWithResponse("oiautorest6685", "oiautorest6685", "AzureNetworkFlow", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withRetentionInDays(45)
            .withTotalRetentionInDays(70)
            .withSchema(new Schema().withName("AzureNetworkFlow")
                .withColumns(Arrays.asList(new Column().withName("MyNewColumn").withType(ColumnTypeEnum.GUID))))
            .apply();
    }
}
```

### Usages_List

```java
/**
 * Samples for Usages List.
 */
public final class UsagesListSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/WorkspacesListUsages.json
     */
    /**
     * Sample code: UsagesList.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void usagesList(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.usages().list("rg1", "TestLinkWS", com.azure.core.util.Context.NONE);
    }
}
```

### WorkspacePurge_GetPurgeStatus

```java
/**
 * Samples for WorkspacePurge GetPurgeStatus.
 */
public final class WorkspacePurgeGetPurgeStatusSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/WorkspacesPurgeOperation.json
     */
    /**
     * Sample code: WorkspacePurgeOperation.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void workspacePurgeOperation(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.workspacePurges()
            .getPurgeStatusWithResponse("OIAutoRest5123", "aztest5048", "purge-970318e7-b859-4edb-8903-83b1b54d0b74",
                com.azure.core.util.Context.NONE);
    }
}
```

### WorkspacePurge_Purge

```java
import com.azure.resourcemanager.loganalytics.models.WorkspacePurgeBody;
import com.azure.resourcemanager.loganalytics.models.WorkspacePurgeBodyFilters;
import java.util.Arrays;

/**
 * Samples for WorkspacePurge Purge.
 */
public final class WorkspacePurgePurgeSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/WorkspacesPurgeResourceId.json
     */
    /**
     * Sample code: WorkspacePurgeResourceId.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void workspacePurgeResourceId(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.workspacePurges()
            .purgeWithResponse("OIAutoRest5123", "aztest5048", new WorkspacePurgeBody().withTable("Heartbeat")
                .withFilters(Arrays.asList(new WorkspacePurgeBodyFilters().withColumn("_ResourceId")
                    .withOperator("==")
                    .withValue(
                        "/subscriptions/12341234-1234-1234-1234-123412341234/resourceGroups/SomeResourceGroup/providers/microsoft.insights/components/AppInsightResource"))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/WorkspacesPurge.json
     */
    /**
     * Sample code: WorkspacePurge.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void workspacePurge(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.workspacePurges()
            .purgeWithResponse("OIAutoRest5123", "aztest5048",
                new WorkspacePurgeBody().withTable("Heartbeat")
                    .withFilters(Arrays.asList(new WorkspacePurgeBodyFilters().withColumn("TimeGenerated")
                        .withOperator(">")
                        .withValue("2017-09-01T00:00:00"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_CreateOrUpdate

```java
import com.azure.resourcemanager.loganalytics.models.WorkspaceSku;
import com.azure.resourcemanager.loganalytics.models.WorkspaceSkuNameEnum;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Workspaces CreateOrUpdate.
 */
public final class WorkspacesCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/WorkspacesCreate.json
     */
    /**
     * Sample code: WorkspacesCreate.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void workspacesCreate(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.workspaces()
            .define("oiautorest6685")
            .withRegion("australiasoutheast")
            .withExistingResourceGroup("oiautorest6685")
            .withTags(mapOf("tag1", "val1"))
            .withSku(new WorkspaceSku().withName(WorkspaceSkuNameEnum.PER_GB2018))
            .withRetentionInDays(30)
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

### Workspaces_Delete

```java
/**
 * Samples for Workspaces Delete.
 */
public final class WorkspacesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/WorkspacesDelete.json
     */
    /**
     * Sample code: WorkspacesDelete.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void workspacesDelete(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.workspaces().delete("oiautorest6685", "oiautorest6685", null, com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_Failback

```java
/**
 * Samples for Workspaces Failback.
 */
public final class WorkspacesFailbackSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/WorkspacesFailback.json
     */
    /**
     * Sample code: WorkspacesFailover.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void workspacesFailover(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.workspaces().failback("oiautorest6685", "oiautorest6685", com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_Failover

```java
/**
 * Samples for Workspaces Failover.
 */
public final class WorkspacesFailoverSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/WorkspacesFailover.json
     */
    /**
     * Sample code: WorkspacesFailover.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void workspacesFailover(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.workspaces().failover("oiautorest6685", "eastus", "oiautorest6685", com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_GetByResourceGroup

```java
/**
 * Samples for Workspaces GetByResourceGroup.
 */
public final class WorkspacesGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/WorkspacesGet.json
     */
    /**
     * Sample code: WorkspaceGet.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void workspaceGet(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.workspaces()
            .getByResourceGroupWithResponse("oiautorest6685", "oiautorest6685", com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_GetNsp

```java
/**
 * Samples for Workspaces GetNsp.
 */
public final class WorkspacesGetNspSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/NSPForWorkspaces_Get.json
     */
    /**
     * Sample code: Get NSP config by name for Scheduled Query Rule.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void
        getNSPConfigByNameForScheduledQueryRule(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.workspaces()
            .getNspWithResponse("exampleRG", "someWorkspace", "somePerimeterConfiguration",
                com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_List

```java
/**
 * Samples for Workspaces List.
 */
public final class WorkspacesListSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/WorkspacesSubscriptionList.json
     */
    /**
     * Sample code: WorkspacesSubscriptionList.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void workspacesSubscriptionList(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.workspaces().list(com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_ListByResourceGroup

```java
/**
 * Samples for Workspaces ListByResourceGroup.
 */
public final class WorkspacesListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/WorkspacesListByResourceGroup.json
     */
    /**
     * Sample code: WorkspacesGet.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void workspacesGet(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.workspaces().listByResourceGroup("oiautorest6685", com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_ListNsp

```java
/**
 * Samples for Workspaces ListNsp.
 */
public final class WorkspacesListNspSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/NSPForWorkspaces_List.json
     */
    /**
     * Sample code: List NSP configs by Scheduled Query Rule.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void
        listNSPConfigsByScheduledQueryRule(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.workspaces().listNsp("exampleRG", "someWorkspace", com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_ReconcileNsp

```java
/**
 * Samples for Workspaces ReconcileNsp.
 */
public final class WorkspacesReconcileNspSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/NSPForWorkspaces_Reconcile.json
     */
    /**
     * Sample code: Reconcile NSP config for Scheduled Query Rule.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void
        reconcileNSPConfigForScheduledQueryRule(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        manager.workspaces()
            .reconcileNsp("exampleRG", "someWorkspace", "somePerimeterConfiguration", com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_Update

```java
import com.azure.resourcemanager.loganalytics.models.Workspace;
import com.azure.resourcemanager.loganalytics.models.WorkspaceCapping;
import com.azure.resourcemanager.loganalytics.models.WorkspaceSku;
import com.azure.resourcemanager.loganalytics.models.WorkspaceSkuNameEnum;

/**
 * Samples for Workspaces Update.
 */
public final class WorkspacesUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/operationalinsights/resource-manager/Microsoft.OperationalInsights/OperationalInsights/stable/2025-
     * 07-01/examples/WorkspacesUpdate.json
     */
    /**
     * Sample code: WorkspacesPatch.
     * 
     * @param manager Entry point to LogAnalyticsManager.
     */
    public static void workspacesPatch(com.azure.resourcemanager.loganalytics.LogAnalyticsManager manager) {
        Workspace resource = manager.workspaces()
            .getByResourceGroupWithResponse("oiautorest6685", "oiautorest6685", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withSku(new WorkspaceSku().withName(WorkspaceSkuNameEnum.PER_GB2018))
            .withRetentionInDays(30)
            .withWorkspaceCapping(new WorkspaceCapping().withDailyQuotaGb(-1.0D))
            .apply();
    }
}
```

