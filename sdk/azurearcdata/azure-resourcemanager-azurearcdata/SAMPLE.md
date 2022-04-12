# Code snippets and samples


## ActiveDirectoryConnectors

- [Create](#activedirectoryconnectors_create)
- [Delete](#activedirectoryconnectors_delete)
- [Get](#activedirectoryconnectors_get)
- [List](#activedirectoryconnectors_list)

## DataControllers

- [Delete](#datacontrollers_delete)
- [GetByResourceGroup](#datacontrollers_getbyresourcegroup)
- [List](#datacontrollers_list)
- [ListByResourceGroup](#datacontrollers_listbyresourcegroup)
- [PatchDataController](#datacontrollers_patchdatacontroller)
- [PutDataController](#datacontrollers_putdatacontroller)

## Operations

- [List](#operations_list)

## PostgresInstances

- [Create](#postgresinstances_create)
- [Delete](#postgresinstances_delete)
- [GetByResourceGroup](#postgresinstances_getbyresourcegroup)
- [List](#postgresinstances_list)
- [ListByResourceGroup](#postgresinstances_listbyresourcegroup)
- [Update](#postgresinstances_update)

## SqlManagedInstances

- [Create](#sqlmanagedinstances_create)
- [Delete](#sqlmanagedinstances_delete)
- [GetByResourceGroup](#sqlmanagedinstances_getbyresourcegroup)
- [List](#sqlmanagedinstances_list)
- [ListByResourceGroup](#sqlmanagedinstances_listbyresourcegroup)
- [Update](#sqlmanagedinstances_update)

## SqlServerInstances

- [Create](#sqlserverinstances_create)
- [Delete](#sqlserverinstances_delete)
- [GetByResourceGroup](#sqlserverinstances_getbyresourcegroup)
- [List](#sqlserverinstances_list)
- [ListByResourceGroup](#sqlserverinstances_listbyresourcegroup)
- [Update](#sqlserverinstances_update)
### ActiveDirectoryConnectors_Create

```java
import com.azure.resourcemanager.azurearcdata.models.AccountProvisioningMode;
import com.azure.resourcemanager.azurearcdata.models.ActiveDirectoryConnectorDnsDetails;
import com.azure.resourcemanager.azurearcdata.models.ActiveDirectoryConnectorDomainDetails;
import com.azure.resourcemanager.azurearcdata.models.ActiveDirectoryConnectorProperties;
import com.azure.resourcemanager.azurearcdata.models.ActiveDirectoryConnectorSpec;
import com.azure.resourcemanager.azurearcdata.models.ActiveDirectoryDomainController;
import com.azure.resourcemanager.azurearcdata.models.ActiveDirectoryDomainControllers;
import java.util.Arrays;

/** Samples for ActiveDirectoryConnectors Create. */
public final class ActiveDirectoryConnectorsCreateSamples {
    /*
     * x-ms-original-file: specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2022-03-01-preview/examples/CreateOrUpdateActiveDirectoryConnector.json
     */
    /**
     * Sample code: Create or update an Active Directory connector instance.
     *
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void createOrUpdateAnActiveDirectoryConnectorInstance(
        com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager
            .activeDirectoryConnectors()
            .define("testADConnector")
            .withExistingDataController("testrg", "testdataController")
            .withProperties(
                new ActiveDirectoryConnectorProperties()
                    .withSpec(
                        new ActiveDirectoryConnectorSpec()
                            .withActiveDirectory(
                                new ActiveDirectoryConnectorDomainDetails()
                                    .withRealm("CONTOSO.LOCAL")
                                    .withServiceAccountProvisioning(AccountProvisioningMode.MANUAL)
                                    .withDomainControllers(
                                        new ActiveDirectoryDomainControllers()
                                            .withPrimaryDomainController(
                                                new ActiveDirectoryDomainController().withHostname("dc1.contoso.local"))
                                            .withSecondaryDomainControllers(
                                                Arrays
                                                    .asList(
                                                        new ActiveDirectoryDomainController()
                                                            .withHostname("dc2.contoso.local"),
                                                        new ActiveDirectoryDomainController()
                                                            .withHostname("dc3.contoso.local")))))
                            .withDns(
                                new ActiveDirectoryConnectorDnsDetails()
                                    .withNameserverIpAddresses(Arrays.asList("11.11.111.111", "22.22.222.222"))
                                    .withReplicas(1L)
                                    .withPreferK8SDnsForPtrLookups(false))))
            .create();
    }
}
```

### ActiveDirectoryConnectors_Delete

```java
import com.azure.core.util.Context;

/** Samples for ActiveDirectoryConnectors Delete. */
public final class ActiveDirectoryConnectorsDeleteSamples {
    /*
     * x-ms-original-file: specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2022-03-01-preview/examples/DeleteActiveDirectoryConnector.json
     */
    /**
     * Sample code: Deletes an Active Directory connector instance.
     *
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void deletesAnActiveDirectoryConnectorInstance(
        com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.activeDirectoryConnectors().delete("testrg", "testdataController", "testADConnector", Context.NONE);
    }
}
```

### ActiveDirectoryConnectors_Get

```java
import com.azure.core.util.Context;

/** Samples for ActiveDirectoryConnectors Get. */
public final class ActiveDirectoryConnectorsGetSamples {
    /*
     * x-ms-original-file: specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2022-03-01-preview/examples/GetActiveDirectoryConnector.json
     */
    /**
     * Sample code: Retrieves an Active Directory connector resource.
     *
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void retrievesAnActiveDirectoryConnectorResource(
        com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager
            .activeDirectoryConnectors()
            .getWithResponse("testrg", "testdataController", "testADConnector", Context.NONE);
    }
}
```

### ActiveDirectoryConnectors_List

```java
import com.azure.core.util.Context;

/** Samples for ActiveDirectoryConnectors List. */
public final class ActiveDirectoryConnectorsListSamples {
    /*
     * x-ms-original-file: specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2022-03-01-preview/examples/ListByDataControllerActiveDirectoryConnector.json
     */
    /**
     * Sample code: Gets all Active Directory connectors associated with a data controller.
     *
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void getsAllActiveDirectoryConnectorsAssociatedWithADataController(
        com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.activeDirectoryConnectors().list("testrg", "testdataController", Context.NONE);
    }
}
```

### DataControllers_Delete

```java
import com.azure.core.util.Context;

/** Samples for DataControllers Delete. */
public final class DataControllersDeleteSamples {
    /*
     * x-ms-original-file: specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2022-03-01-preview/examples/DeleteDataController.json
     */
    /**
     * Sample code: Delete a dataController.
     *
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void deleteADataController(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.dataControllers().delete("testrg", "testdataController", Context.NONE);
    }
}
```

### DataControllers_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for DataControllers GetByResourceGroup. */
public final class DataControllersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2022-03-01-preview/examples/GetDataController.json
     */
    /**
     * Sample code: Get a data controller.
     *
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void getADataController(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.dataControllers().getByResourceGroupWithResponse("testrg", "testdataController", Context.NONE);
    }
}
```

### DataControllers_List

```java
import com.azure.core.util.Context;

/** Samples for DataControllers List. */
public final class DataControllersListSamples {
    /*
     * x-ms-original-file: specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2022-03-01-preview/examples/ListSubscriptionDataController.json
     */
    /**
     * Sample code: Gets all dataControllers in a subscription.
     *
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void getsAllDataControllersInASubscription(
        com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.dataControllers().list(Context.NONE);
    }
}
```

### DataControllers_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for DataControllers ListByResourceGroup. */
public final class DataControllersListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2022-03-01-preview/examples/ListByResourceGroupDataController.json
     */
    /**
     * Sample code: Gets all dataControllers in a resource group.
     *
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void getsAllDataControllersInAResourceGroup(
        com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.dataControllers().listByResourceGroup("testrg", Context.NONE);
    }
}
```

### DataControllers_PatchDataController

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.azurearcdata.models.DataControllerResource;
import java.util.HashMap;
import java.util.Map;

/** Samples for DataControllers PatchDataController. */
public final class DataControllersPatchDataControllerSamples {
    /*
     * x-ms-original-file: specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2022-03-01-preview/examples/UpdateDataController.json
     */
    /**
     * Sample code: Updates a dataController tags.
     *
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void updatesADataControllerTags(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        DataControllerResource resource =
            manager
                .dataControllers()
                .getByResourceGroupWithResponse("testrg", "testdataController1", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("mytag", "myval")).apply();
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

### DataControllers_PutDataController

```java
import com.azure.resourcemanager.azurearcdata.models.BasicLoginInformation;
import com.azure.resourcemanager.azurearcdata.models.DataControllerProperties;
import com.azure.resourcemanager.azurearcdata.models.ExtendedLocation;
import com.azure.resourcemanager.azurearcdata.models.ExtendedLocationTypes;
import com.azure.resourcemanager.azurearcdata.models.Infrastructure;
import com.azure.resourcemanager.azurearcdata.models.LogAnalyticsWorkspaceConfig;
import com.azure.resourcemanager.azurearcdata.models.OnPremiseProperty;
import com.azure.resourcemanager.azurearcdata.models.UploadServicePrincipal;
import com.azure.resourcemanager.azurearcdata.models.UploadWatermark;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Samples for DataControllers PutDataController. */
public final class DataControllersPutDataControllerSamples {
    /*
     * x-ms-original-file: specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2022-03-01-preview/examples/CreateOrUpdateDataController.json
     */
    /**
     * Sample code: Create or update a Data Controller.
     *
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void createOrUpdateADataController(
        com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager
            .dataControllers()
            .define("testdataController")
            .withRegion("northeurope")
            .withExistingResourceGroup("testrg")
            .withProperties(
                new DataControllerProperties()
                    .withInfrastructure(Infrastructure.ONPREMISES)
                    .withOnPremiseProperty(
                        new OnPremiseProperty()
                            .withId(UUID.fromString("12345678-1234-1234-ab12-1a2b3c4d5e6f"))
                            .withPublicSigningKey("publicOnPremSigningKey"))
                    .withUploadWatermark(
                        new UploadWatermark()
                            .withMetrics(OffsetDateTime.parse("2020-01-01T17:18:19.1234567Z"))
                            .withLogs(OffsetDateTime.parse("2020-01-01T17:18:19.1234567Z"))
                            .withUsages(OffsetDateTime.parse("2020-01-01T17:18:19.1234567Z")))
                    .withBasicLoginInformation(
                        new BasicLoginInformation().withUsername("username").withPassword("********"))
                    .withMetricsDashboardCredential(
                        new BasicLoginInformation().withUsername("username").withPassword("********"))
                    .withLogsDashboardCredential(
                        new BasicLoginInformation().withUsername("username").withPassword("********"))
                    .withLogAnalyticsWorkspaceConfig(
                        new LogAnalyticsWorkspaceConfig()
                            .withWorkspaceId(UUID.fromString("00000000-1111-2222-3333-444444444444"))
                            .withPrimaryKey("********"))
                    .withUploadServicePrincipal(
                        new UploadServicePrincipal()
                            .withClientId(UUID.fromString("00000000-1111-2222-3333-444444444444"))
                            .withTenantId(UUID.fromString("00000000-1111-2222-3333-444444444444"))
                            .withAuthority("https://login.microsoftonline.com/")
                            .withClientSecret("********"))
                    .withClusterId(
                        "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.Kubernetes/connectedClusters/connectedk8s")
                    .withExtensionId(
                        "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.Kubernetes/connectedClusters/connectedk8s/providers/Microsoft.KubernetesConfiguration/extensions/extension"))
            .withTags(mapOf("mytag", "myval"))
            .withExtendedLocation(
                new ExtendedLocation()
                    .withName(
                        "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.ExtendedLocation/customLocations/arclocation")
                    .withType(ExtendedLocationTypes.CUSTOM_LOCATION))
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

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2022-03-01-preview/examples/ListOperation.json
     */
    /**
     * Sample code: Lists all of the available Azure Data Services on Azure Arc API operations.
     *
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void listsAllOfTheAvailableAzureDataServicesOnAzureArcAPIOperations(
        com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### PostgresInstances_Create

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.azurearcdata.models.BasicLoginInformation;
import com.azure.resourcemanager.azurearcdata.models.ExtendedLocation;
import com.azure.resourcemanager.azurearcdata.models.ExtendedLocationTypes;
import com.azure.resourcemanager.azurearcdata.models.PostgresInstanceProperties;
import com.azure.resourcemanager.azurearcdata.models.PostgresInstanceSku;
import com.azure.resourcemanager.azurearcdata.models.PostgresInstanceSkuTier;
import java.io.IOException;

/** Samples for PostgresInstances Create. */
public final class PostgresInstancesCreateSamples {
    /*
     * x-ms-original-file: specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2022-03-01-preview/examples/CreateOrUpdatePostgresInstance.json
     */
    /**
     * Sample code: Create or update a Postgres Instance.
     *
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void createOrUpdateAPostgresInstance(
        com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) throws IOException {
        manager
            .postgresInstances()
            .define("testpostgresInstance")
            .withRegion("eastus")
            .withExistingResourceGroup("testrg")
            .withProperties(
                new PostgresInstanceProperties()
                    .withDataControllerId("dataControllerId")
                    .withAdmin("admin")
                    .withBasicLoginInformation(
                        new BasicLoginInformation().withUsername("username").withPassword("********"))
                    .withK8SRaw(
                        SerializerFactory
                            .createDefaultManagementSerializerAdapter()
                            .deserialize(
                                "{\"apiVersion\":\"apiVersion\",\"kind\":\"postgresql-12\",\"metadata\":{\"name\":\"pg1\",\"creationTimestamp\":\"2020-08-25T14:55:10Z\",\"generation\":1,\"namespace\":\"test\",\"resourceVersion\":\"527780\",\"selfLink\":\"/apis/arcdata.microsoft.com/v1alpha1/namespaces/test/postgresql-12s/pg1\",\"uid\":\"1111aaaa-ffff-ffff-ffff-99999aaaaaaa\"},\"spec\":{\"backups\":{\"deltaMinutes\":3,\"fullMinutes\":10,\"tiers\":[{\"retention\":{\"maximums\":[\"6\",\"512MB\"],\"minimums\":[\"3\"]},\"storage\":{\"volumeSize\":\"1Gi\"}}]},\"engine\":{\"extensions\":[{\"name\":\"citus\"}]},\"scale\":{\"shards\":3},\"scheduling\":{\"default\":{\"resources\":{\"requests\":{\"memory\":\"256Mi\"}}}},\"service\":{\"type\":\"NodePort\"},\"storage\":{\"data\":{\"className\":\"local-storage\",\"size\":\"5Gi\"},\"logs\":{\"className\":\"local-storage\",\"size\":\"5Gi\"}}},\"status\":{\"externalEndpoint\":null,\"readyPods\":\"4/4\",\"state\":\"Ready\"}}",
                                Object.class,
                                SerializerEncoding.JSON)))
            .withExtendedLocation(
                new ExtendedLocation()
                    .withName(
                        "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.ExtendedLocation/customLocations/arclocation")
                    .withType(ExtendedLocationTypes.CUSTOM_LOCATION))
            .withSku(
                new PostgresInstanceSku()
                    .withName("default")
                    .withDev(true)
                    .withTier(PostgresInstanceSkuTier.HYPERSCALE))
            .create();
    }
}
```

### PostgresInstances_Delete

```java
import com.azure.core.util.Context;

/** Samples for PostgresInstances Delete. */
public final class PostgresInstancesDeleteSamples {
    /*
     * x-ms-original-file: specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2022-03-01-preview/examples/DeletePostgresInstance.json
     */
    /**
     * Sample code: Deletes a PostgresInstances.
     *
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void deletesAPostgresInstances(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.postgresInstances().delete("testrg", "testpostgresInstance", Context.NONE);
    }
}
```

### PostgresInstances_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for PostgresInstances GetByResourceGroup. */
public final class PostgresInstancesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2022-03-01-preview/examples/GetPostgresInstance.json
     */
    /**
     * Sample code: Gets a postgres Instances.
     *
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void getsAPostgresInstances(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.postgresInstances().getByResourceGroupWithResponse("testrg", "testpostgresInstances", Context.NONE);
    }
}
```

### PostgresInstances_List

```java
import com.azure.core.util.Context;

/** Samples for PostgresInstances List. */
public final class PostgresInstancesListSamples {
    /*
     * x-ms-original-file: specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2022-03-01-preview/examples/ListSubscriptionPostgresInstance.json
     */
    /**
     * Sample code: Gets all Postgres Instance in a subscription.
     *
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void getsAllPostgresInstanceInASubscription(
        com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.postgresInstances().list(Context.NONE);
    }
}
```

### PostgresInstances_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for PostgresInstances ListByResourceGroup. */
public final class PostgresInstancesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2022-03-01-preview/examples/ListByResourceGroupPostgresInstance.json
     */
    /**
     * Sample code: Gets all postgres Instances in a resource group.
     *
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void getsAllPostgresInstancesInAResourceGroup(
        com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.postgresInstances().listByResourceGroup("testrg", Context.NONE);
    }
}
```

### PostgresInstances_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.azurearcdata.models.PostgresInstance;
import java.util.HashMap;
import java.util.Map;

/** Samples for PostgresInstances Update. */
public final class PostgresInstancesUpdateSamples {
    /*
     * x-ms-original-file: specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2022-03-01-preview/examples/UpdatePostgresInstance.json
     */
    /**
     * Sample code: Updates a postgres Instances tags.
     *
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void updatesAPostgresInstancesTags(
        com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        PostgresInstance resource =
            manager
                .postgresInstances()
                .getByResourceGroupWithResponse("testrg", "testpostgresInstance", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("mytag", "myval")).apply();
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

### SqlManagedInstances_Create

```java
import com.azure.resourcemanager.azurearcdata.models.ActiveDirectoryInformation;
import com.azure.resourcemanager.azurearcdata.models.ArcSqlManagedInstanceLicenseType;
import com.azure.resourcemanager.azurearcdata.models.BasicLoginInformation;
import com.azure.resourcemanager.azurearcdata.models.ExtendedLocation;
import com.azure.resourcemanager.azurearcdata.models.ExtendedLocationTypes;
import com.azure.resourcemanager.azurearcdata.models.K8SResourceRequirements;
import com.azure.resourcemanager.azurearcdata.models.K8SScheduling;
import com.azure.resourcemanager.azurearcdata.models.K8SSchedulingOptions;
import com.azure.resourcemanager.azurearcdata.models.KeytabInformation;
import com.azure.resourcemanager.azurearcdata.models.SqlManagedInstanceK8SRaw;
import com.azure.resourcemanager.azurearcdata.models.SqlManagedInstanceK8SSpec;
import com.azure.resourcemanager.azurearcdata.models.SqlManagedInstanceProperties;
import com.azure.resourcemanager.azurearcdata.models.SqlManagedInstanceSku;
import com.azure.resourcemanager.azurearcdata.models.SqlManagedInstanceSkuTier;
import java.util.HashMap;
import java.util.Map;

/** Samples for SqlManagedInstances Create. */
public final class SqlManagedInstancesCreateSamples {
    /*
     * x-ms-original-file: specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2022-03-01-preview/examples/CreateOrUpdateSqlManagedInstance.json
     */
    /**
     * Sample code: Create or update a SQL Managed Instance.
     *
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void createOrUpdateASQLManagedInstance(
        com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager
            .sqlManagedInstances()
            .define("testsqlManagedInstance")
            .withRegion("northeurope")
            .withExistingResourceGroup("testrg")
            .withProperties(
                new SqlManagedInstanceProperties()
                    .withAdmin("Admin user")
                    .withStartTime("Instance start time")
                    .withEndTime("Instance end time")
                    .withK8SRaw(
                        new SqlManagedInstanceK8SRaw()
                            .withSpec(
                                new SqlManagedInstanceK8SSpec()
                                    .withScheduling(
                                        new K8SScheduling()
                                            .withDefaultProperty(
                                                new K8SSchedulingOptions()
                                                    .withResources(
                                                        new K8SResourceRequirements()
                                                            .withRequests(
                                                                mapOf(
                                                                    "additionalProperty",
                                                                    "additionalValue",
                                                                    "cpu",
                                                                    "1",
                                                                    "memory",
                                                                    "8Gi"))
                                                            .withLimits(
                                                                mapOf(
                                                                    "additionalProperty",
                                                                    "additionalValue",
                                                                    "cpu",
                                                                    "1",
                                                                    "memory",
                                                                    "8Gi"))
                                                            .withAdditionalProperties(mapOf()))
                                                    .withAdditionalProperties(mapOf()))
                                            .withAdditionalProperties(mapOf()))
                                    .withReplicas(1)
                                    .withAdditionalProperties(mapOf()))
                            .withAdditionalProperties(mapOf("additionalProperty", 1234)))
                    .withBasicLoginInformation(
                        new BasicLoginInformation().withUsername("username").withPassword("********"))
                    .withActiveDirectoryInformation(
                        new ActiveDirectoryInformation()
                            .withKeytabInformation(new KeytabInformation().withKeytab("********")))
                    .withLicenseType(ArcSqlManagedInstanceLicenseType.LICENSE_INCLUDED)
                    .withClusterId(
                        "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.Kubernetes/connectedClusters/connectedk8s")
                    .withExtensionId(
                        "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.Kubernetes/connectedClusters/connectedk8s/providers/Microsoft.KubernetesConfiguration/extensions/extension"))
            .withTags(mapOf("mytag", "myval"))
            .withExtendedLocation(
                new ExtendedLocation()
                    .withName(
                        "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.ExtendedLocation/customLocations/arclocation")
                    .withType(ExtendedLocationTypes.CUSTOM_LOCATION))
            .withSku(new SqlManagedInstanceSku().withTier(SqlManagedInstanceSkuTier.GENERAL_PURPOSE).withDev(true))
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

### SqlManagedInstances_Delete

```java
import com.azure.core.util.Context;

/** Samples for SqlManagedInstances Delete. */
public final class SqlManagedInstancesDeleteSamples {
    /*
     * x-ms-original-file: specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2022-03-01-preview/examples/DeleteSqlManagedInstance.json
     */
    /**
     * Sample code: Delete a SQL Instance.
     *
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void deleteASQLInstance(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlManagedInstances().delete("testrg", "testsqlManagedInstance", Context.NONE);
    }
}
```

### SqlManagedInstances_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for SqlManagedInstances GetByResourceGroup. */
public final class SqlManagedInstancesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2022-03-01-preview/examples/GetSqlManagedInstance.json
     */
    /**
     * Sample code: Updates a SQL Instance tags.
     *
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void updatesASQLInstanceTags(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlManagedInstances().getByResourceGroupWithResponse("testrg", "testsqlManagedInstance", Context.NONE);
    }
}
```

### SqlManagedInstances_List

```java
import com.azure.core.util.Context;

/** Samples for SqlManagedInstances List. */
public final class SqlManagedInstancesListSamples {
    /*
     * x-ms-original-file: specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2022-03-01-preview/examples/ListSubscriptionSqlManagedInstance.json
     */
    /**
     * Sample code: Gets all SQL Instance in a subscription.
     *
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void getsAllSQLInstanceInASubscription(
        com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlManagedInstances().list(Context.NONE);
    }
}
```

### SqlManagedInstances_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for SqlManagedInstances ListByResourceGroup. */
public final class SqlManagedInstancesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2022-03-01-preview/examples/ListByResourceGroupSqlManagedInstance.json
     */
    /**
     * Sample code: Gets all SQL Instance in a resource group.
     *
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void getsAllSQLInstanceInAResourceGroup(
        com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlManagedInstances().listByResourceGroup("testrg", Context.NONE);
    }
}
```

### SqlManagedInstances_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.azurearcdata.models.SqlManagedInstance;
import java.util.HashMap;
import java.util.Map;

/** Samples for SqlManagedInstances Update. */
public final class SqlManagedInstancesUpdateSamples {
    /*
     * x-ms-original-file: specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2022-03-01-preview/examples/UpdateSqlManagedInstance.json
     */
    /**
     * Sample code: Updates a sql Instance tags.
     *
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void updatesASqlInstanceTags(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        SqlManagedInstance resource =
            manager
                .sqlManagedInstances()
                .getByResourceGroupWithResponse("testrg", "testsqlManagedInstance", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("mytag", "myval")).apply();
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

### SqlServerInstances_Create

```java
import com.azure.resourcemanager.azurearcdata.models.ArcSqlServerLicenseType;
import com.azure.resourcemanager.azurearcdata.models.ConnectionStatus;
import com.azure.resourcemanager.azurearcdata.models.DefenderStatus;
import com.azure.resourcemanager.azurearcdata.models.EditionType;
import com.azure.resourcemanager.azurearcdata.models.HostType;
import com.azure.resourcemanager.azurearcdata.models.SqlServerInstanceProperties;
import com.azure.resourcemanager.azurearcdata.models.SqlVersion;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/** Samples for SqlServerInstances Create. */
public final class SqlServerInstancesCreateSamples {
    /*
     * x-ms-original-file: specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2022-03-01-preview/examples/CreateOrUpdateSqlServerInstance.json
     */
    /**
     * Sample code: Updates a SQL Server Instance tags.
     *
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void updatesASQLServerInstanceTags(
        com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager
            .sqlServerInstances()
            .define("testsqlServerInstance")
            .withRegion("northeurope")
            .withExistingResourceGroup("testrg")
            .withTags(mapOf("mytag", "myval"))
            .withProperties(
                new SqlServerInstanceProperties()
                    .withVersion(SqlVersion.SQL_SERVER_2012)
                    .withEdition(EditionType.DEVELOPER)
                    .withContainerResourceId("Resource id of hosting Arc Machine")
                    .withVCore("4")
                    .withStatus(ConnectionStatus.REGISTERED)
                    .withPatchLevel("patchlevel")
                    .withCollation("collation")
                    .withCurrentVersion("2012")
                    .withInstanceName("name of instance")
                    .withTcpDynamicPorts("1433")
                    .withTcpStaticPorts("1433")
                    .withProductId("sql id")
                    .withLicenseType(ArcSqlServerLicenseType.FREE)
                    .withAzureDefenderStatusLastUpdated(OffsetDateTime.parse("2020-01-02T17:18:19.1234567Z"))
                    .withAzureDefenderStatus(DefenderStatus.PROTECTED)
                    .withHostType(HostType.PHYSICAL_SERVER))
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

### SqlServerInstances_Delete

```java
import com.azure.core.util.Context;

/** Samples for SqlServerInstances Delete. */
public final class SqlServerInstancesDeleteSamples {
    /*
     * x-ms-original-file: specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2022-03-01-preview/examples/DeleteSqlServerInstance.json
     */
    /**
     * Sample code: Delete a SQL Server Instance.
     *
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void deleteASQLServerInstance(com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlServerInstances().delete("testrg", "testsqlServerInstance", Context.NONE);
    }
}
```

### SqlServerInstances_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for SqlServerInstances GetByResourceGroup. */
public final class SqlServerInstancesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2022-03-01-preview/examples/GetSqlServerInstance.json
     */
    /**
     * Sample code: Updates a SQL Server Instance tags.
     *
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void updatesASQLServerInstanceTags(
        com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlServerInstances().getByResourceGroupWithResponse("testrg", "testsqlServerInstance", Context.NONE);
    }
}
```

### SqlServerInstances_List

```java
import com.azure.core.util.Context;

/** Samples for SqlServerInstances List. */
public final class SqlServerInstancesListSamples {
    /*
     * x-ms-original-file: specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2022-03-01-preview/examples/ListSubscriptionSqlServerInstance.json
     */
    /**
     * Sample code: Gets all SQL Server Instance in a subscription.
     *
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void getsAllSQLServerInstanceInASubscription(
        com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlServerInstances().list(Context.NONE);
    }
}
```

### SqlServerInstances_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for SqlServerInstances ListByResourceGroup. */
public final class SqlServerInstancesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2022-03-01-preview/examples/ListByResourceGroupSqlServerInstance.json
     */
    /**
     * Sample code: Gets all SQL Server Instance in a resource group.
     *
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void getsAllSQLServerInstanceInAResourceGroup(
        com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        manager.sqlServerInstances().listByResourceGroup("testrg", Context.NONE);
    }
}
```

### SqlServerInstances_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.azurearcdata.models.SqlServerInstance;
import java.util.HashMap;
import java.util.Map;

/** Samples for SqlServerInstances Update. */
public final class SqlServerInstancesUpdateSamples {
    /*
     * x-ms-original-file: specification/azurearcdata/resource-manager/Microsoft.AzureArcData/preview/2022-03-01-preview/examples/UpdateSqlServerInstance.json
     */
    /**
     * Sample code: Updates a SQL Server Instance tags.
     *
     * @param manager Entry point to AzureArcDataManager.
     */
    public static void updatesASQLServerInstanceTags(
        com.azure.resourcemanager.azurearcdata.AzureArcDataManager manager) {
        SqlServerInstance resource =
            manager
                .sqlServerInstances()
                .getByResourceGroupWithResponse("testrg", "testsqlServerInstance", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("mytag", "myval")).apply();
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

