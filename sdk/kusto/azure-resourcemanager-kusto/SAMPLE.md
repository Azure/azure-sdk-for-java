# Code snippets and samples


## AttachedDatabaseConfigurations

- [CheckNameAvailability](#attacheddatabaseconfigurations_checknameavailability)
- [CreateOrUpdate](#attacheddatabaseconfigurations_createorupdate)
- [Delete](#attacheddatabaseconfigurations_delete)
- [Get](#attacheddatabaseconfigurations_get)
- [ListByCluster](#attacheddatabaseconfigurations_listbycluster)

## ClusterPrincipalAssignments

- [CheckNameAvailability](#clusterprincipalassignments_checknameavailability)
- [CreateOrUpdate](#clusterprincipalassignments_createorupdate)
- [Delete](#clusterprincipalassignments_delete)
- [Get](#clusterprincipalassignments_get)
- [List](#clusterprincipalassignments_list)

## Clusters

- [AddLanguageExtensions](#clusters_addlanguageextensions)
- [CheckNameAvailability](#clusters_checknameavailability)
- [CreateOrUpdate](#clusters_createorupdate)
- [Delete](#clusters_delete)
- [DetachFollowerDatabases](#clusters_detachfollowerdatabases)
- [DiagnoseVirtualNetwork](#clusters_diagnosevirtualnetwork)
- [GetByResourceGroup](#clusters_getbyresourcegroup)
- [List](#clusters_list)
- [ListByResourceGroup](#clusters_listbyresourcegroup)
- [ListFollowerDatabases](#clusters_listfollowerdatabases)
- [ListLanguageExtensions](#clusters_listlanguageextensions)
- [ListOutboundNetworkDependenciesEndpoints](#clusters_listoutboundnetworkdependenciesendpoints)
- [ListSkus](#clusters_listskus)
- [ListSkusByResource](#clusters_listskusbyresource)
- [RemoveLanguageExtensions](#clusters_removelanguageextensions)
- [Start](#clusters_start)
- [Stop](#clusters_stop)
- [Update](#clusters_update)

## DataConnections

- [CheckNameAvailability](#dataconnections_checknameavailability)
- [CreateOrUpdate](#dataconnections_createorupdate)
- [DataConnectionValidation](#dataconnections_dataconnectionvalidation)
- [Delete](#dataconnections_delete)
- [Get](#dataconnections_get)
- [ListByDatabase](#dataconnections_listbydatabase)
- [Update](#dataconnections_update)

## DatabasePrincipalAssignments

- [CheckNameAvailability](#databaseprincipalassignments_checknameavailability)
- [CreateOrUpdate](#databaseprincipalassignments_createorupdate)
- [Delete](#databaseprincipalassignments_delete)
- [Get](#databaseprincipalassignments_get)
- [List](#databaseprincipalassignments_list)

## Databases

- [AddPrincipals](#databases_addprincipals)
- [CheckNameAvailability](#databases_checknameavailability)
- [CreateOrUpdate](#databases_createorupdate)
- [Delete](#databases_delete)
- [Get](#databases_get)
- [ListByCluster](#databases_listbycluster)
- [ListPrincipals](#databases_listprincipals)
- [RemovePrincipals](#databases_removeprincipals)
- [Update](#databases_update)

## ManagedPrivateEndpoints

- [CheckNameAvailability](#managedprivateendpoints_checknameavailability)
- [CreateOrUpdate](#managedprivateendpoints_createorupdate)
- [Delete](#managedprivateendpoints_delete)
- [Get](#managedprivateendpoints_get)
- [List](#managedprivateendpoints_list)
- [Update](#managedprivateendpoints_update)

## Operations

- [List](#operations_list)

## OperationsResults

- [Get](#operationsresults_get)

## PrivateEndpointConnections

- [CreateOrUpdate](#privateendpointconnections_createorupdate)
- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [List](#privateendpointconnections_list)

## PrivateLinkResources

- [Get](#privatelinkresources_get)
- [List](#privatelinkresources_list)

## Scripts

- [CheckNameAvailability](#scripts_checknameavailability)
- [CreateOrUpdate](#scripts_createorupdate)
- [Delete](#scripts_delete)
- [Get](#scripts_get)
- [ListByDatabase](#scripts_listbydatabase)
- [Update](#scripts_update)
### AttachedDatabaseConfigurations_CheckNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.kusto.models.AttachedDatabaseConfigurationsCheckNameRequest;

/** Samples for AttachedDatabaseConfigurations CheckNameAvailability. */
public final class AttachedDatabaseConfigurationsCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoAttachedDatabaseConfigurationCheckNameAvailability.json
     */
    /**
     * Sample code: KustoAttachedDatabaseConfigurationCheckNameAvailability.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoAttachedDatabaseConfigurationCheckNameAvailability(
        com.azure.resourcemanager.kusto.KustoManager manager) {
        manager
            .attachedDatabaseConfigurations()
            .checkNameAvailabilityWithResponse(
                "kustorptest",
                "kustoclusterrptest4",
                new AttachedDatabaseConfigurationsCheckNameRequest().withName("adc1"),
                Context.NONE);
    }
}
```

### AttachedDatabaseConfigurations_CreateOrUpdate

```java
import com.azure.resourcemanager.kusto.models.DefaultPrincipalsModificationKind;
import com.azure.resourcemanager.kusto.models.TableLevelSharingProperties;
import java.util.Arrays;

/** Samples for AttachedDatabaseConfigurations CreateOrUpdate. */
public final class AttachedDatabaseConfigurationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoAttachedDatabaseConfigurationsCreateOrUpdate.json
     */
    /**
     * Sample code: AttachedDatabaseConfigurationsCreateOrUpdate.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void attachedDatabaseConfigurationsCreateOrUpdate(
        com.azure.resourcemanager.kusto.KustoManager manager) {
        manager
            .attachedDatabaseConfigurations()
            .define("attachedDatabaseConfigurations1")
            .withExistingCluster("kustorptest", "kustoclusterrptest4")
            .withRegion("westus")
            .withDatabaseName("kustodatabase")
            .withClusterResourceId(
                "/subscriptions/12345678-1234-1234-1234-123456789098/resourceGroups/kustorptest/providers/Microsoft.Kusto/Clusters/KustoClusterLeader")
            .withDefaultPrincipalsModificationKind(DefaultPrincipalsModificationKind.UNION)
            .withTableLevelSharingProperties(
                new TableLevelSharingProperties()
                    .withTablesToInclude(Arrays.asList("Table1"))
                    .withTablesToExclude(Arrays.asList("Table2"))
                    .withExternalTablesToInclude(Arrays.asList("ExternalTable1"))
                    .withExternalTablesToExclude(Arrays.asList("ExternalTable2"))
                    .withMaterializedViewsToInclude(Arrays.asList("MaterializedViewTable1"))
                    .withMaterializedViewsToExclude(Arrays.asList("MaterializedViewTable2")))
            .create();
    }
}
```

### AttachedDatabaseConfigurations_Delete

```java
import com.azure.core.util.Context;

/** Samples for AttachedDatabaseConfigurations Delete. */
public final class AttachedDatabaseConfigurationsDeleteSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoAttachedDatabaseConfigurationsDelete.json
     */
    /**
     * Sample code: AttachedDatabaseConfigurationsDelete.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void attachedDatabaseConfigurationsDelete(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager
            .attachedDatabaseConfigurations()
            .delete("kustorptest", "kustoclusterrptest4", "attachedDatabaseConfigurations1", Context.NONE);
    }
}
```

### AttachedDatabaseConfigurations_Get

```java
import com.azure.core.util.Context;

/** Samples for AttachedDatabaseConfigurations Get. */
public final class AttachedDatabaseConfigurationsGetSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoAttachedDatabaseConfigurationsGet.json
     */
    /**
     * Sample code: AttachedDatabaseConfigurationsGet.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void attachedDatabaseConfigurationsGet(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager
            .attachedDatabaseConfigurations()
            .getWithResponse("kustorptest", "kustoclusterrptest4", "attachedDatabaseConfigurations1", Context.NONE);
    }
}
```

### AttachedDatabaseConfigurations_ListByCluster

```java
import com.azure.core.util.Context;

/** Samples for AttachedDatabaseConfigurations ListByCluster. */
public final class AttachedDatabaseConfigurationsListByClusterSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoAttachedDatabaseConfigurationsListByCluster.json
     */
    /**
     * Sample code: KustoAttachedDatabaseConfigurationsListByCluster.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoAttachedDatabaseConfigurationsListByCluster(
        com.azure.resourcemanager.kusto.KustoManager manager) {
        manager.attachedDatabaseConfigurations().listByCluster("kustorptest", "kustoclusterrptest4", Context.NONE);
    }
}
```

### ClusterPrincipalAssignments_CheckNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.kusto.models.ClusterPrincipalAssignmentCheckNameRequest;

/** Samples for ClusterPrincipalAssignments CheckNameAvailability. */
public final class ClusterPrincipalAssignmentsCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoClusterPrincipalAssignmentsCheckNameAvailability.json
     */
    /**
     * Sample code: KustoClusterPrincipalAssignmentsCheckNameAvailability.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoClusterPrincipalAssignmentsCheckNameAvailability(
        com.azure.resourcemanager.kusto.KustoManager manager) {
        manager
            .clusterPrincipalAssignments()
            .checkNameAvailabilityWithResponse(
                "kustorptest",
                "kustoclusterrptest4",
                new ClusterPrincipalAssignmentCheckNameRequest().withName("kustoprincipal1"),
                Context.NONE);
    }
}
```

### ClusterPrincipalAssignments_CreateOrUpdate

```java
import com.azure.resourcemanager.kusto.models.ClusterPrincipalRole;
import com.azure.resourcemanager.kusto.models.PrincipalType;

/** Samples for ClusterPrincipalAssignments CreateOrUpdate. */
public final class ClusterPrincipalAssignmentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoClusterPrincipalAssignmentsCreateOrUpdate.json
     */
    /**
     * Sample code: KustoClusterPrincipalAssignmentsCreateOrUpdate.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoClusterPrincipalAssignmentsCreateOrUpdate(
        com.azure.resourcemanager.kusto.KustoManager manager) {
        manager
            .clusterPrincipalAssignments()
            .define("kustoprincipal1")
            .withExistingCluster("kustorptest", "kustoclusterrptest4")
            .withPrincipalId("87654321-1234-1234-1234-123456789123")
            .withRole(ClusterPrincipalRole.ALL_DATABASES_ADMIN)
            .withTenantId("12345678-1234-1234-1234-123456789123")
            .withPrincipalType(PrincipalType.APP)
            .create();
    }
}
```

### ClusterPrincipalAssignments_Delete

```java
import com.azure.core.util.Context;

/** Samples for ClusterPrincipalAssignments Delete. */
public final class ClusterPrincipalAssignmentsDeleteSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoClusterPrincipalAssignmentsDelete.json
     */
    /**
     * Sample code: KustoClusterPrincipalAssignmentsDelete.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoClusterPrincipalAssignmentsDelete(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager
            .clusterPrincipalAssignments()
            .delete("kustorptest", "kustoclusterrptest4", "kustoprincipal1", Context.NONE);
    }
}
```

### ClusterPrincipalAssignments_Get

```java
import com.azure.core.util.Context;

/** Samples for ClusterPrincipalAssignments Get. */
public final class ClusterPrincipalAssignmentsGetSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoClusterPrincipalAssignmentsGet.json
     */
    /**
     * Sample code: KustoClusterPrincipalAssignmentsGet.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoClusterPrincipalAssignmentsGet(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager
            .clusterPrincipalAssignments()
            .getWithResponse("kustorptest", "kustoclusterrptest4", "kustoprincipal1", Context.NONE);
    }
}
```

### ClusterPrincipalAssignments_List

```java
import com.azure.core.util.Context;

/** Samples for ClusterPrincipalAssignments List. */
public final class ClusterPrincipalAssignmentsListSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoClusterPrincipalAssignmentsList.json
     */
    /**
     * Sample code: KustoPrincipalAssignmentsList.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoPrincipalAssignmentsList(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager.clusterPrincipalAssignments().list("kustorptest", "kustoclusterrptest4", Context.NONE);
    }
}
```

### Clusters_AddLanguageExtensions

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.kusto.fluent.models.LanguageExtensionInner;
import com.azure.resourcemanager.kusto.models.LanguageExtensionName;
import com.azure.resourcemanager.kusto.models.LanguageExtensionsList;
import java.util.Arrays;

/** Samples for Clusters AddLanguageExtensions. */
public final class ClustersAddLanguageExtensionsSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoClusterAddLanguageExtensions.json
     */
    /**
     * Sample code: KustoClusterAddLanguageExtensions.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoClusterAddLanguageExtensions(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager
            .clusters()
            .addLanguageExtensions(
                "kustorptest",
                "kustoclusterrptest4",
                new LanguageExtensionsList()
                    .withValue(
                        Arrays
                            .asList(
                                new LanguageExtensionInner().withLanguageExtensionName(LanguageExtensionName.PYTHON),
                                new LanguageExtensionInner().withLanguageExtensionName(LanguageExtensionName.R))),
                Context.NONE);
    }
}
```

### Clusters_CheckNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.kusto.models.ClusterCheckNameRequest;

/** Samples for Clusters CheckNameAvailability. */
public final class ClustersCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoClustersCheckNameAvailability.json
     */
    /**
     * Sample code: KustoClustersCheckNameAvailability.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoClustersCheckNameAvailability(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager
            .clusters()
            .checkNameAvailabilityWithResponse(
                "westus", new ClusterCheckNameRequest().withName("kustoclusterrptest4"), Context.NONE);
    }
}
```

### Clusters_CreateOrUpdate

```java
import com.azure.resourcemanager.kusto.models.AzureSku;
import com.azure.resourcemanager.kusto.models.AzureSkuName;
import com.azure.resourcemanager.kusto.models.AzureSkuTier;
import com.azure.resourcemanager.kusto.models.Identity;
import com.azure.resourcemanager.kusto.models.IdentityType;
import com.azure.resourcemanager.kusto.models.PublicNetworkAccess;
import java.util.Arrays;

/** Samples for Clusters CreateOrUpdate. */
public final class ClustersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoClustersCreateOrUpdate.json
     */
    /**
     * Sample code: KustoClustersCreateOrUpdate.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoClustersCreateOrUpdate(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager
            .clusters()
            .define("kustoclusterrptest4")
            .withRegion("westus")
            .withExistingResourceGroup("kustorptest")
            .withSku(new AzureSku().withName(AzureSkuName.STANDARD_L8S).withCapacity(2).withTier(AzureSkuTier.STANDARD))
            .withIdentity(new Identity().withType(IdentityType.SYSTEM_ASSIGNED))
            .withEnableStreamingIngest(true)
            .withEnablePurge(true)
            .withEnableDoubleEncryption(false)
            .withPublicNetworkAccess(PublicNetworkAccess.ENABLED)
            .withAllowedIpRangeList(Arrays.asList("0.0.0.0/0"))
            .withEnableAutoStop(true)
            .create();
    }
}
```

### Clusters_Delete

```java
import com.azure.core.util.Context;

/** Samples for Clusters Delete. */
public final class ClustersDeleteSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoClustersDelete.json
     */
    /**
     * Sample code: KustoClustersDelete.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoClustersDelete(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager.clusters().delete("kustorptest", "kustoclusterrptest4", Context.NONE);
    }
}
```

### Clusters_DetachFollowerDatabases

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.kusto.fluent.models.FollowerDatabaseDefinitionInner;

/** Samples for Clusters DetachFollowerDatabases. */
public final class ClustersDetachFollowerDatabasesSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoClusterDetachFollowerDatabases.json
     */
    /**
     * Sample code: KustoClusterDetachFollowerDatabases.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoClusterDetachFollowerDatabases(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager
            .clusters()
            .detachFollowerDatabases(
                "kustorptest",
                "kustoclusterrptest4",
                new FollowerDatabaseDefinitionInner()
                    .withClusterResourceId(
                        "/subscriptions/12345678-1234-1234-1234-123456789098/resourceGroups/kustorptest/providers/Microsoft.Kusto/clusters/leader4")
                    .withAttachedDatabaseConfigurationName("myAttachedDatabaseConfiguration"),
                Context.NONE);
    }
}
```

### Clusters_DiagnoseVirtualNetwork

```java
import com.azure.core.util.Context;

/** Samples for Clusters DiagnoseVirtualNetwork. */
public final class ClustersDiagnoseVirtualNetworkSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoClustersDiagnoseVirtualNetwork.json
     */
    /**
     * Sample code: KustoClusterDiagnoseVirtualNetwork.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoClusterDiagnoseVirtualNetwork(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager.clusters().diagnoseVirtualNetwork("kustorptest", "kustoclusterrptest4", Context.NONE);
    }
}
```

### Clusters_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Clusters GetByResourceGroup. */
public final class ClustersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoClustersGet.json
     */
    /**
     * Sample code: KustoClustersGet.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoClustersGet(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager.clusters().getByResourceGroupWithResponse("kustorptest", "kustoclusterrptest4", Context.NONE);
    }
}
```

### Clusters_List

```java
import com.azure.core.util.Context;

/** Samples for Clusters List. */
public final class ClustersListSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoClustersList.json
     */
    /**
     * Sample code: KustoClustersList.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoClustersList(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager.clusters().list(Context.NONE);
    }
}
```

### Clusters_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Clusters ListByResourceGroup. */
public final class ClustersListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoClustersListByResourceGroup.json
     */
    /**
     * Sample code: KustoClustersListByResourceGroup.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoClustersListByResourceGroup(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager.clusters().listByResourceGroup("kustorptest", Context.NONE);
    }
}
```

### Clusters_ListFollowerDatabases

```java
import com.azure.core.util.Context;

/** Samples for Clusters ListFollowerDatabases. */
public final class ClustersListFollowerDatabasesSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoClusterListFollowerDatabases.json
     */
    /**
     * Sample code: KustoClusterListFollowerDatabases.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoClusterListFollowerDatabases(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager.clusters().listFollowerDatabases("kustorptest", "kustoclusterrptest4", Context.NONE);
    }
}
```

### Clusters_ListLanguageExtensions

```java
import com.azure.core.util.Context;

/** Samples for Clusters ListLanguageExtensions. */
public final class ClustersListLanguageExtensionsSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoClusterListLanguageExtensions.json
     */
    /**
     * Sample code: KustoClusterListLanguageExtensions.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoClusterListLanguageExtensions(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager.clusters().listLanguageExtensions("kustorptest", "kustoclusterrptest4", Context.NONE);
    }
}
```

### Clusters_ListOutboundNetworkDependenciesEndpoints

```java
import com.azure.core.util.Context;

/** Samples for Clusters ListOutboundNetworkDependenciesEndpoints. */
public final class ClustersListOutboundNetworkDependenciesEndpointsSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoOutboundNetworkDependenciesList.json
     */
    /**
     * Sample code: Get Kusto cluster outbound network dependencies.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void getKustoClusterOutboundNetworkDependencies(
        com.azure.resourcemanager.kusto.KustoManager manager) {
        manager.clusters().listOutboundNetworkDependenciesEndpoints("kustorptest", "kustoclusterrptest", Context.NONE);
    }
}
```

### Clusters_ListSkus

```java
import com.azure.core.util.Context;

/** Samples for Clusters ListSkus. */
public final class ClustersListSkusSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoClustersListSkus.json
     */
    /**
     * Sample code: KustoClustersListSkus.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoClustersListSkus(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager.clusters().listSkus(Context.NONE);
    }
}
```

### Clusters_ListSkusByResource

```java
import com.azure.core.util.Context;

/** Samples for Clusters ListSkusByResource. */
public final class ClustersListSkusByResourceSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoClustersListResourceSkus.json
     */
    /**
     * Sample code: KustoClustersListResourceSkus.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoClustersListResourceSkus(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager.clusters().listSkusByResource("kustorptest", "kustoclusterrptest4", Context.NONE);
    }
}
```

### Clusters_RemoveLanguageExtensions

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.kusto.fluent.models.LanguageExtensionInner;
import com.azure.resourcemanager.kusto.models.LanguageExtensionName;
import com.azure.resourcemanager.kusto.models.LanguageExtensionsList;
import java.util.Arrays;

/** Samples for Clusters RemoveLanguageExtensions. */
public final class ClustersRemoveLanguageExtensionsSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoClusterRemoveLanguageExtensions.json
     */
    /**
     * Sample code: KustoClusterRemoveLanguageExtensions.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoClusterRemoveLanguageExtensions(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager
            .clusters()
            .removeLanguageExtensions(
                "kustorptest",
                "kustoclusterrptest4",
                new LanguageExtensionsList()
                    .withValue(
                        Arrays
                            .asList(
                                new LanguageExtensionInner().withLanguageExtensionName(LanguageExtensionName.PYTHON),
                                new LanguageExtensionInner().withLanguageExtensionName(LanguageExtensionName.R))),
                Context.NONE);
    }
}
```

### Clusters_Start

```java
import com.azure.core.util.Context;

/** Samples for Clusters Start. */
public final class ClustersStartSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoClustersStart.json
     */
    /**
     * Sample code: KustoClustersStart.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoClustersStart(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager.clusters().start("kustorptest", "kustoclusterrptest4", Context.NONE);
    }
}
```

### Clusters_Stop

```java
import com.azure.core.util.Context;

/** Samples for Clusters Stop. */
public final class ClustersStopSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoClustersStop.json
     */
    /**
     * Sample code: KustoClustersStop.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoClustersStop(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager.clusters().stop("kustorptest", "kustoclusterrptest4", Context.NONE);
    }
}
```

### Clusters_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.kusto.models.Cluster;

/** Samples for Clusters Update. */
public final class ClustersUpdateSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoClustersUpdate.json
     */
    /**
     * Sample code: KustoClustersUpdate.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoClustersUpdate(com.azure.resourcemanager.kusto.KustoManager manager) {
        Cluster resource =
            manager
                .clusters()
                .getByResourceGroupWithResponse("kustorptest", "kustoclusterrptest4", Context.NONE)
                .getValue();
        resource.update().withIfMatch("*").apply();
    }
}
```

### DataConnections_CheckNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.kusto.models.DataConnectionCheckNameRequest;

/** Samples for DataConnections CheckNameAvailability. */
public final class DataConnectionsCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoDataConnectionsCheckNameAvailability.json
     */
    /**
     * Sample code: KustoDataConnectionsCheckNameAvailability.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoDataConnectionsCheckNameAvailability(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager
            .dataConnections()
            .checkNameAvailabilityWithResponse(
                "kustorptest",
                "kustoclusterrptest4",
                "KustoDatabase8",
                new DataConnectionCheckNameRequest().withName("DataConnections8"),
                Context.NONE);
    }
}
```

### DataConnections_CreateOrUpdate

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.kusto.models.EventHubDataConnection;

/** Samples for DataConnections CreateOrUpdate. */
public final class DataConnectionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoDataConnectionsCreateOrUpdate.json
     */
    /**
     * Sample code: KustoDataConnectionsCreateOrUpdate.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoDataConnectionsCreateOrUpdate(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager
            .dataConnections()
            .createOrUpdate(
                "kustorptest",
                "kustoclusterrptest4",
                "KustoDatabase8",
                "DataConnections8",
                new EventHubDataConnection()
                    .withLocation("westus")
                    .withEventHubResourceId(
                        "/subscriptions/12345678-1234-1234-1234-123456789098/resourceGroups/kustorptest/providers/Microsoft.EventHub/namespaces/eventhubTestns1/eventhubs/eventhubTest1")
                    .withConsumerGroup("testConsumerGroup1")
                    .withManagedIdentityResourceId(
                        "/subscriptions/12345678-1234-1234-1234-123456789098/resourceGroups/kustorptest/providers/Microsoft.ManagedIdentity/userAssignedIdentities/managedidentityTest1"),
                Context.NONE);
    }
}
```

### DataConnections_DataConnectionValidation

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.kusto.fluent.models.DataConnectionValidationInner;
import com.azure.resourcemanager.kusto.models.EventHubDataConnection;

/** Samples for DataConnections DataConnectionValidation. */
public final class DataConnectionsDataConnectionValidationSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoDataConnectionValidationAsync.json
     */
    /**
     * Sample code: KustoDataConnectionValidation.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoDataConnectionValidation(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager
            .dataConnections()
            .dataConnectionValidation(
                "kustorptest",
                "kustoclusterrptest4",
                "KustoDatabase8",
                new DataConnectionValidationInner()
                    .withDataConnectionName("DataConnections8")
                    .withProperties(new EventHubDataConnection()),
                Context.NONE);
    }
}
```

### DataConnections_Delete

```java
import com.azure.core.util.Context;

/** Samples for DataConnections Delete. */
public final class DataConnectionsDeleteSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoDataConnectionsDelete.json
     */
    /**
     * Sample code: KustoDataConnectionsDelete.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoDataConnectionsDelete(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager
            .dataConnections()
            .delete("kustorptest", "kustoclusterrptest4", "KustoDatabase8", "kustoeventhubconnection1", Context.NONE);
    }
}
```

### DataConnections_Get

```java
import com.azure.core.util.Context;

/** Samples for DataConnections Get. */
public final class DataConnectionsGetSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoDataConnectionsGet.json
     */
    /**
     * Sample code: KustoDataConnectionsGet.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoDataConnectionsGet(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager
            .dataConnections()
            .getWithResponse("kustorptest", "kustoclusterrptest4", "KustoDatabase8", "DataConnections8", Context.NONE);
    }
}
```

### DataConnections_ListByDatabase

```java
import com.azure.core.util.Context;

/** Samples for DataConnections ListByDatabase. */
public final class DataConnectionsListByDatabaseSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoDataConnectionsListByDatabase.json
     */
    /**
     * Sample code: KustoDatabasesListByCluster.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoDatabasesListByCluster(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager.dataConnections().listByDatabase("kustorptest", "kustoclusterrptest4", "KustoDatabase8", Context.NONE);
    }
}
```

### DataConnections_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.kusto.models.EventHubDataConnection;

/** Samples for DataConnections Update. */
public final class DataConnectionsUpdateSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoDataConnectionsUpdate.json
     */
    /**
     * Sample code: KustoDataConnectionsUpdate.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoDataConnectionsUpdate(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager
            .dataConnections()
            .update(
                "kustorptest",
                "kustoclusterrptest4",
                "KustoDatabase8",
                "DataConnections8",
                new EventHubDataConnection()
                    .withLocation("westus")
                    .withEventHubResourceId(
                        "/subscriptions/12345678-1234-1234-1234-123456789098/resourceGroups/kustorptest/providers/Microsoft.EventHub/namespaces/eventhubTestns1/eventhubs/eventhubTest1")
                    .withConsumerGroup("testConsumerGroup1")
                    .withManagedIdentityResourceId(
                        "/subscriptions/12345678-1234-1234-1234-123456789098/resourceGroups/kustorptest/providers/Microsoft.ManagedIdentity/userAssignedIdentities/managedidentityTest1"),
                Context.NONE);
    }
}
```

### DatabasePrincipalAssignments_CheckNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.kusto.models.DatabasePrincipalAssignmentCheckNameRequest;

/** Samples for DatabasePrincipalAssignments CheckNameAvailability. */
public final class DatabasePrincipalAssignmentsCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoDatabasePrincipalAssignmentsCheckNameAvailability.json
     */
    /**
     * Sample code: KustoDatabaseCheckNameAvailability.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoDatabaseCheckNameAvailability(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager
            .databasePrincipalAssignments()
            .checkNameAvailabilityWithResponse(
                "kustorptest",
                "kustoclusterrptest4",
                "Kustodatabase8",
                new DatabasePrincipalAssignmentCheckNameRequest().withName("kustoprincipal1"),
                Context.NONE);
    }
}
```

### DatabasePrincipalAssignments_CreateOrUpdate

```java
import com.azure.resourcemanager.kusto.models.DatabasePrincipalRole;
import com.azure.resourcemanager.kusto.models.PrincipalType;

/** Samples for DatabasePrincipalAssignments CreateOrUpdate. */
public final class DatabasePrincipalAssignmentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoDatabasePrincipalAssignmentsCreateOrUpdate.json
     */
    /**
     * Sample code: KustoDatabasePrincipalAssignmentsCreateOrUpdate.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoDatabasePrincipalAssignmentsCreateOrUpdate(
        com.azure.resourcemanager.kusto.KustoManager manager) {
        manager
            .databasePrincipalAssignments()
            .define("kustoprincipal1")
            .withExistingDatabase("kustorptest", "kustoclusterrptest4", "Kustodatabase8")
            .withPrincipalId("87654321-1234-1234-1234-123456789123")
            .withRole(DatabasePrincipalRole.ADMIN)
            .withTenantId("12345678-1234-1234-1234-123456789123")
            .withPrincipalType(PrincipalType.APP)
            .create();
    }
}
```

### DatabasePrincipalAssignments_Delete

```java
import com.azure.core.util.Context;

/** Samples for DatabasePrincipalAssignments Delete. */
public final class DatabasePrincipalAssignmentsDeleteSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoDatabasePrincipalAssignmentsDelete.json
     */
    /**
     * Sample code: KustoDatabasePrincipalAssignmentsDelete.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoDatabasePrincipalAssignmentsDelete(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager
            .databasePrincipalAssignments()
            .delete("kustorptest", "kustoclusterrptest4", "Kustodatabase8", "kustoprincipal1", Context.NONE);
    }
}
```

### DatabasePrincipalAssignments_Get

```java
import com.azure.core.util.Context;

/** Samples for DatabasePrincipalAssignments Get. */
public final class DatabasePrincipalAssignmentsGetSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoDatabasePrincipalAssignmentsGet.json
     */
    /**
     * Sample code: KustoDatabasePrincipalAssignmentsGet.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoDatabasePrincipalAssignmentsGet(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager
            .databasePrincipalAssignments()
            .getWithResponse("kustorptest", "kustoclusterrptest4", "Kustodatabase8", "kustoprincipal1", Context.NONE);
    }
}
```

### DatabasePrincipalAssignments_List

```java
import com.azure.core.util.Context;

/** Samples for DatabasePrincipalAssignments List. */
public final class DatabasePrincipalAssignmentsListSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoDatabasePrincipalAssignmentsList.json
     */
    /**
     * Sample code: KustoPrincipalAssignmentsList.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoPrincipalAssignmentsList(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager
            .databasePrincipalAssignments()
            .list("kustorptest", "kustoclusterrptest4", "Kustodatabase8", Context.NONE);
    }
}
```

### Databases_AddPrincipals

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.kusto.fluent.models.DatabasePrincipalInner;
import com.azure.resourcemanager.kusto.models.DatabasePrincipalListRequest;
import com.azure.resourcemanager.kusto.models.DatabasePrincipalRole;
import com.azure.resourcemanager.kusto.models.DatabasePrincipalType;
import java.util.Arrays;

/** Samples for Databases AddPrincipals. */
public final class DatabasesAddPrincipalsSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoDatabaseAddPrincipals.json
     */
    /**
     * Sample code: KustoDatabaseAddPrincipals.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoDatabaseAddPrincipals(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager
            .databases()
            .addPrincipalsWithResponse(
                "kustorptest",
                "kustoclusterrptest4",
                "KustoDatabase8",
                new DatabasePrincipalListRequest()
                    .withValue(
                        Arrays
                            .asList(
                                new DatabasePrincipalInner()
                                    .withRole(DatabasePrincipalRole.ADMIN)
                                    .withName("Some User")
                                    .withType(DatabasePrincipalType.USER)
                                    .withFqn("aaduser=some_guid")
                                    .withEmail("user@microsoft.com")
                                    .withAppId(""),
                                new DatabasePrincipalInner()
                                    .withRole(DatabasePrincipalRole.VIEWER)
                                    .withName("Kusto")
                                    .withType(DatabasePrincipalType.GROUP)
                                    .withFqn("aadgroup=some_guid")
                                    .withEmail("kusto@microsoft.com")
                                    .withAppId(""),
                                new DatabasePrincipalInner()
                                    .withRole(DatabasePrincipalRole.ADMIN)
                                    .withName("SomeApp")
                                    .withType(DatabasePrincipalType.APP)
                                    .withFqn("aadapp=some_guid_app_id")
                                    .withEmail("")
                                    .withAppId("some_guid_app_id"))),
                Context.NONE);
    }
}
```

### Databases_CheckNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.kusto.models.CheckNameRequest;
import com.azure.resourcemanager.kusto.models.Type;

/** Samples for Databases CheckNameAvailability. */
public final class DatabasesCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoDatabasesCheckNameAvailability.json
     */
    /**
     * Sample code: KustoDatabasesCheckNameAvailability.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoDatabasesCheckNameAvailability(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager
            .databases()
            .checkNameAvailabilityWithResponse(
                "kustorptest",
                "kustoclusterrptest4",
                new CheckNameRequest().withName("database1").withType(Type.MICROSOFT_KUSTO_CLUSTERS_DATABASES),
                Context.NONE);
    }
}
```

### Databases_CreateOrUpdate

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.kusto.fluent.models.DatabaseInner;
import com.azure.resourcemanager.kusto.models.ReadOnlyFollowingDatabase;
import java.time.Duration;

/** Samples for Databases CreateOrUpdate. */
public final class DatabasesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoDatabaseReadonlyUpdate.json
     */
    /**
     * Sample code: Kusto ReadOnly database update.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoReadOnlyDatabaseUpdate(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager
            .databases()
            .createOrUpdate(
                "kustorptest",
                "kustoclusterrptest4",
                "KustoreadOnlyDatabase",
                new ReadOnlyFollowingDatabase().withLocation("westus").withHotCachePeriod(Duration.parse("P1D")),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoDatabasesCreateOrUpdate.json
     */
    /**
     * Sample code: Kusto ReadWrite database create or update.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoReadWriteDatabaseCreateOrUpdate(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager
            .databases()
            .createOrUpdate(
                "kustorptest",
                "kustoclusterrptest4",
                "KustoDatabase8",
                new DatabaseInner().withLocation("westus"),
                Context.NONE);
    }
}
```

### Databases_Delete

```java
import com.azure.core.util.Context;

/** Samples for Databases Delete. */
public final class DatabasesDeleteSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoDatabasesDelete.json
     */
    /**
     * Sample code: KustoDatabasesDelete.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoDatabasesDelete(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager.databases().delete("kustorptest", "kustoclusterrptest4", "KustoDatabase8", Context.NONE);
    }
}
```

### Databases_Get

```java
import com.azure.core.util.Context;

/** Samples for Databases Get. */
public final class DatabasesGetSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoDatabasesGet.json
     */
    /**
     * Sample code: KustoDatabasesGet.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoDatabasesGet(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager.databases().getWithResponse("kustorptest", "kustoclusterrptest4", "KustoDatabase8", Context.NONE);
    }
}
```

### Databases_ListByCluster

```java
import com.azure.core.util.Context;

/** Samples for Databases ListByCluster. */
public final class DatabasesListByClusterSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoDatabasesListByCluster.json
     */
    /**
     * Sample code: KustoDatabasesListByCluster.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoDatabasesListByCluster(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager.databases().listByCluster("kustorptest", "kustoclusterrptest4", Context.NONE);
    }
}
```

### Databases_ListPrincipals

```java
import com.azure.core.util.Context;

/** Samples for Databases ListPrincipals. */
public final class DatabasesListPrincipalsSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoDatabaseListPrincipals.json
     */
    /**
     * Sample code: KustoDatabaseListPrincipals.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoDatabaseListPrincipals(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager.databases().listPrincipals("kustorptest", "kustoclusterrptest4", "KustoDatabase8", Context.NONE);
    }
}
```

### Databases_RemovePrincipals

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.kusto.fluent.models.DatabasePrincipalInner;
import com.azure.resourcemanager.kusto.models.DatabasePrincipalListRequest;
import com.azure.resourcemanager.kusto.models.DatabasePrincipalRole;
import com.azure.resourcemanager.kusto.models.DatabasePrincipalType;
import java.util.Arrays;

/** Samples for Databases RemovePrincipals. */
public final class DatabasesRemovePrincipalsSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoDatabaseRemovePrincipals.json
     */
    /**
     * Sample code: KustoDatabaseRemovePrincipals.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoDatabaseRemovePrincipals(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager
            .databases()
            .removePrincipalsWithResponse(
                "kustorptest",
                "kustoclusterrptest4",
                "KustoDatabase8",
                new DatabasePrincipalListRequest()
                    .withValue(
                        Arrays
                            .asList(
                                new DatabasePrincipalInner()
                                    .withRole(DatabasePrincipalRole.ADMIN)
                                    .withName("Some User")
                                    .withType(DatabasePrincipalType.USER)
                                    .withFqn("aaduser=some_guid")
                                    .withEmail("user@microsoft.com")
                                    .withAppId(""),
                                new DatabasePrincipalInner()
                                    .withRole(DatabasePrincipalRole.VIEWER)
                                    .withName("Kusto")
                                    .withType(DatabasePrincipalType.GROUP)
                                    .withFqn("aadgroup=some_guid")
                                    .withEmail("kusto@microsoft.com")
                                    .withAppId(""),
                                new DatabasePrincipalInner()
                                    .withRole(DatabasePrincipalRole.ADMIN)
                                    .withName("SomeApp")
                                    .withType(DatabasePrincipalType.APP)
                                    .withFqn("aadapp=some_guid_app_id")
                                    .withEmail("")
                                    .withAppId("some_guid_app_id"))),
                Context.NONE);
    }
}
```

### Databases_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.kusto.fluent.models.DatabaseInner;

/** Samples for Databases Update. */
public final class DatabasesUpdateSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoDatabasesUpdate.json
     */
    /**
     * Sample code: KustoDatabasesUpdate.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoDatabasesUpdate(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager
            .databases()
            .update("kustorptest", "kustoclusterrptest4", "KustoDatabase8", new DatabaseInner(), Context.NONE);
    }
}
```

### ManagedPrivateEndpoints_CheckNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.kusto.models.ManagedPrivateEndpointsCheckNameRequest;

/** Samples for ManagedPrivateEndpoints CheckNameAvailability. */
public final class ManagedPrivateEndpointsCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoManagedPrivateEndpointsCheckNameAvailability.json
     */
    /**
     * Sample code: KustoManagedPrivateEndpointsCheckNameAvailability.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoManagedPrivateEndpointsCheckNameAvailability(
        com.azure.resourcemanager.kusto.KustoManager manager) {
        manager
            .managedPrivateEndpoints()
            .checkNameAvailabilityWithResponse(
                "kustorptest",
                "kustoclusterrptest4",
                new ManagedPrivateEndpointsCheckNameRequest().withName("pme1"),
                Context.NONE);
    }
}
```

### ManagedPrivateEndpoints_CreateOrUpdate

```java
/** Samples for ManagedPrivateEndpoints CreateOrUpdate. */
public final class ManagedPrivateEndpointsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoManagedPrivateEndpointsCreateOrUpdate.json
     */
    /**
     * Sample code: KustoManagedPrivateEndpointsCreateOrUpdate.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoManagedPrivateEndpointsCreateOrUpdate(
        com.azure.resourcemanager.kusto.KustoManager manager) {
        manager
            .managedPrivateEndpoints()
            .define("kustoManagedPrivateEndpoint1")
            .withExistingCluster("kustorptest", "kustoclusterrptest4")
            .withPrivateLinkResourceId(
                "/subscriptions/12345678-1234-1234-1234-123456789098/resourceGroups/kustorptest/providers/Microsoft.Storage/storageAccounts/storageAccountTest")
            .withGroupId("blob")
            .withRequestMessage("Please Approve.")
            .create();
    }
}
```

### ManagedPrivateEndpoints_Delete

```java
import com.azure.core.util.Context;

/** Samples for ManagedPrivateEndpoints Delete. */
public final class ManagedPrivateEndpointsDeleteSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoManagedPrivateEndpointsDelete.json
     */
    /**
     * Sample code: ManagedPrivateEndpointsDelete.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void managedPrivateEndpointsDelete(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager
            .managedPrivateEndpoints()
            .delete("kustorptest", "kustoclusterrptest4", "kustoManagedPrivateEndpoint1", Context.NONE);
    }
}
```

### ManagedPrivateEndpoints_Get

```java
import com.azure.core.util.Context;

/** Samples for ManagedPrivateEndpoints Get. */
public final class ManagedPrivateEndpointsGetSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoManagedPrivateEndpointsGet.json
     */
    /**
     * Sample code: KustoManagedPrivateEndpointsGet.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoManagedPrivateEndpointsGet(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager
            .managedPrivateEndpoints()
            .getWithResponse("kustorptest", "kustoclusterrptest4", "kustoManagedPrivateEndpoint1", Context.NONE);
    }
}
```

### ManagedPrivateEndpoints_List

```java
import com.azure.core.util.Context;

/** Samples for ManagedPrivateEndpoints List. */
public final class ManagedPrivateEndpointsListSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoManagedPrivateEndpointsList.json
     */
    /**
     * Sample code: KustoManagedPrivateEndpointsList.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoManagedPrivateEndpointsList(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager.managedPrivateEndpoints().list("kustorptest", "kustoclusterrptest4", Context.NONE);
    }
}
```

### ManagedPrivateEndpoints_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.kusto.models.ManagedPrivateEndpoint;

/** Samples for ManagedPrivateEndpoints Update. */
public final class ManagedPrivateEndpointsUpdateSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoManagedPrivateEndpointsUpdate.json
     */
    /**
     * Sample code: KustoManagedPrivateEndpointsUpdate.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoManagedPrivateEndpointsUpdate(com.azure.resourcemanager.kusto.KustoManager manager) {
        ManagedPrivateEndpoint resource =
            manager
                .managedPrivateEndpoints()
                .getWithResponse("kustorptest", "kustoclusterrptest4", "kustoManagedPrivateEndpoint1", Context.NONE)
                .getValue();
        resource
            .update()
            .withPrivateLinkResourceId(
                "/subscriptions/12345678-1234-1234-1234-123456789098/resourceGroups/kustorptest/providers/Microsoft.Storage/storageAccounts/storageAccountTest")
            .withGroupId("blob")
            .withRequestMessage("Please Approve Managed Private Endpoint Request.")
            .apply();
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoOperationsList.json
     */
    /**
     * Sample code: KustoOperationsList.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoOperationsList(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### OperationsResults_Get

```java
import com.azure.core.util.Context;

/** Samples for OperationsResults Get. */
public final class OperationsResultsGetSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoOperationResultsGet.json
     */
    /**
     * Sample code: KustoOperationResultsGet.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoOperationResultsGet(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager.operationsResults().getWithResponse("westus", "30972f1b-b61d-4fd8-bd34-3dcfa24670f3", Context.NONE);
    }
}
```

### PrivateEndpointConnections_CreateOrUpdate

```java
import com.azure.resourcemanager.kusto.models.PrivateLinkServiceConnectionStateProperty;

/** Samples for PrivateEndpointConnections CreateOrUpdate. */
public final class PrivateEndpointConnectionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoPrivateEndpointConnectionsCreateOrUpdate.json
     */
    /**
     * Sample code: Approve or reject a private endpoint connection with a given name.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void approveOrRejectAPrivateEndpointConnectionWithAGivenName(
        com.azure.resourcemanager.kusto.KustoManager manager) {
        manager
            .privateEndpointConnections()
            .define("privateEndpointConnectionName")
            .withExistingCluster("kustorptest", "kustoclusterrptest4")
            .withPrivateLinkServiceConnectionState(
                new PrivateLinkServiceConnectionStateProperty()
                    .withStatus("Approved")
                    .withDescription("Approved by johndoe@contoso.com"))
            .create();
    }
}
```

### PrivateEndpointConnections_Delete

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections Delete. */
public final class PrivateEndpointConnectionsDeleteSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoPrivateEndpointConnectionsDelete.json
     */
    /**
     * Sample code: Deletes a private endpoint connection with a given name.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void deletesAPrivateEndpointConnectionWithAGivenName(
        com.azure.resourcemanager.kusto.KustoManager manager) {
        manager
            .privateEndpointConnections()
            .delete("kustorptest", "kustoclusterrptest4", "privateEndpointConnectionName", Context.NONE);
    }
}
```

### PrivateEndpointConnections_Get

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections Get. */
public final class PrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoPrivateEndpointConnectionsGet.json
     */
    /**
     * Sample code: Gets private endpoint connection.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void getsPrivateEndpointConnection(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager
            .privateEndpointConnections()
            .getWithResponse("kustorptest", "kustoclusterrptest4", "privateEndpointConnectionName", Context.NONE);
    }
}
```

### PrivateEndpointConnections_List

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections List. */
public final class PrivateEndpointConnectionsListSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoPrivateEndpointConnectionsList.json
     */
    /**
     * Sample code: KustoPrivateEndpointConnectionsList.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoPrivateEndpointConnectionsList(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager.privateEndpointConnections().list("kustorptest", "kustoclusterrptest4", Context.NONE);
    }
}
```

### PrivateLinkResources_Get

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkResources Get. */
public final class PrivateLinkResourcesGetSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoPrivateLinkResourcesGet.json
     */
    /**
     * Sample code: Gets private endpoint connection.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void getsPrivateEndpointConnection(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager.privateLinkResources().getWithResponse("kustorptest", "kustoclusterrptest4", "cluster", Context.NONE);
    }
}
```

### PrivateLinkResources_List

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkResources List. */
public final class PrivateLinkResourcesListSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoPrivateLinkResourcesList.json
     */
    /**
     * Sample code: Gets private endpoint connections.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void getsPrivateEndpointConnections(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager.privateLinkResources().list("kustorptest", "kustoclusterrptest4", Context.NONE);
    }
}
```

### Scripts_CheckNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.kusto.models.ScriptCheckNameRequest;

/** Samples for Scripts CheckNameAvailability. */
public final class ScriptsCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoScriptsCheckNameAvailability.json
     */
    /**
     * Sample code: KustoScriptsCheckNameAvailability.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoScriptsCheckNameAvailability(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager
            .scripts()
            .checkNameAvailabilityWithResponse(
                "kustorptest",
                "kustoclusterrptest4",
                "db",
                new ScriptCheckNameRequest().withName("kustoScriptName1"),
                Context.NONE);
    }
}
```

### Scripts_CreateOrUpdate

```java
/** Samples for Scripts CreateOrUpdate. */
public final class ScriptsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoScriptsCreateOrUpdate.json
     */
    /**
     * Sample code: KustoScriptsCreateOrUpdate.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoScriptsCreateOrUpdate(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager
            .scripts()
            .define("kustoScript1")
            .withExistingDatabase("kustorptest", "kustoclusterrptest4", "KustoDatabase8")
            .withScriptUrl("https://mysa.blob.core.windows.net/container/script.txt")
            .withScriptUrlSasToken(
                "?sv=2019-02-02&st=2019-04-29T22%3A18%3A26Z&se=2019-04-30T02%3A23%3A26Z&sr=b&sp=rw&sip=168.1.5.60-168.1.5.70&spr=https&sig=********************************")
            .withForceUpdateTag("2bcf3c21-ffd1-4444-b9dd-e52e00ee53fe")
            .withContinueOnErrors(true)
            .create();
    }
}
```

### Scripts_Delete

```java
import com.azure.core.util.Context;

/** Samples for Scripts Delete. */
public final class ScriptsDeleteSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoScriptsDelete.json
     */
    /**
     * Sample code: KustoScriptsDelete.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoScriptsDelete(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager.scripts().delete("kustorptest", "kustoclusterrptest4", "KustoDatabase8", "kustoScript1", Context.NONE);
    }
}
```

### Scripts_Get

```java
import com.azure.core.util.Context;

/** Samples for Scripts Get. */
public final class ScriptsGetSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoScriptsGet.json
     */
    /**
     * Sample code: KustoScriptsGet.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoScriptsGet(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager
            .scripts()
            .getWithResponse("kustorptest", "kustoclusterrptest4", "Kustodatabase8", "kustoScript1", Context.NONE);
    }
}
```

### Scripts_ListByDatabase

```java
import com.azure.core.util.Context;

/** Samples for Scripts ListByDatabase. */
public final class ScriptsListByDatabaseSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoScriptsListByDatabase.json
     */
    /**
     * Sample code: KustoScriptsList.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoScriptsList(com.azure.resourcemanager.kusto.KustoManager manager) {
        manager.scripts().listByDatabase("kustorptest", "kustoclusterrptest4", "Kustodatabase8", Context.NONE);
    }
}
```

### Scripts_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.kusto.models.Script;

/** Samples for Scripts Update. */
public final class ScriptsUpdateSamples {
    /*
     * x-ms-original-file: specification/azure-kusto/resource-manager/Microsoft.Kusto/stable/2021-08-27/examples/KustoScriptsUpdate.json
     */
    /**
     * Sample code: KustoScriptsUpdate.
     *
     * @param manager Entry point to KustoManager.
     */
    public static void kustoScriptsUpdate(com.azure.resourcemanager.kusto.KustoManager manager) {
        Script resource =
            manager
                .scripts()
                .getWithResponse("kustorptest", "kustoclusterrptest4", "KustoDatabase8", "kustoScript1", Context.NONE)
                .getValue();
        resource
            .update()
            .withScriptUrl("https://mysa.blob.core.windows.net/container/script.txt")
            .withForceUpdateTag("2bcf3c21-ffd1-4444-b9dd-e52e00ee53fe")
            .withContinueOnErrors(true)
            .apply();
    }
}
```

