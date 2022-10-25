# Code snippets and samples


## BatchDeployments

- [CreateOrUpdate](#batchdeployments_createorupdate)
- [Delete](#batchdeployments_delete)
- [Get](#batchdeployments_get)
- [List](#batchdeployments_list)
- [Update](#batchdeployments_update)

## BatchEndpoints

- [CreateOrUpdate](#batchendpoints_createorupdate)
- [Delete](#batchendpoints_delete)
- [Get](#batchendpoints_get)
- [List](#batchendpoints_list)
- [ListKeys](#batchendpoints_listkeys)
- [Update](#batchendpoints_update)

## CodeContainers

- [CreateOrUpdate](#codecontainers_createorupdate)
- [Delete](#codecontainers_delete)
- [Get](#codecontainers_get)
- [List](#codecontainers_list)

## CodeVersions

- [CreateOrUpdate](#codeversions_createorupdate)
- [Delete](#codeversions_delete)
- [Get](#codeversions_get)
- [List](#codeversions_list)

## ComponentContainers

- [CreateOrUpdate](#componentcontainers_createorupdate)
- [Delete](#componentcontainers_delete)
- [Get](#componentcontainers_get)
- [List](#componentcontainers_list)

## ComponentVersions

- [CreateOrUpdate](#componentversions_createorupdate)
- [Delete](#componentversions_delete)
- [Get](#componentversions_get)
- [List](#componentversions_list)

## Compute

- [CreateOrUpdate](#compute_createorupdate)
- [Delete](#compute_delete)
- [Get](#compute_get)
- [List](#compute_list)
- [ListKeys](#compute_listkeys)
- [ListNodes](#compute_listnodes)
- [Restart](#compute_restart)
- [Start](#compute_start)
- [Stop](#compute_stop)
- [Update](#compute_update)

## DataContainers

- [CreateOrUpdate](#datacontainers_createorupdate)
- [Delete](#datacontainers_delete)
- [Get](#datacontainers_get)
- [List](#datacontainers_list)

## DataVersions

- [CreateOrUpdate](#dataversions_createorupdate)
- [Delete](#dataversions_delete)
- [Get](#dataversions_get)
- [List](#dataversions_list)

## Datastores

- [CreateOrUpdate](#datastores_createorupdate)
- [Delete](#datastores_delete)
- [Get](#datastores_get)
- [List](#datastores_list)
- [ListSecrets](#datastores_listsecrets)

## EnvironmentContainers

- [CreateOrUpdate](#environmentcontainers_createorupdate)
- [Delete](#environmentcontainers_delete)
- [Get](#environmentcontainers_get)
- [List](#environmentcontainers_list)

## EnvironmentVersions

- [CreateOrUpdate](#environmentversions_createorupdate)
- [Delete](#environmentversions_delete)
- [Get](#environmentversions_get)
- [List](#environmentversions_list)

## Jobs

- [Cancel](#jobs_cancel)
- [CreateOrUpdate](#jobs_createorupdate)
- [Delete](#jobs_delete)
- [Get](#jobs_get)
- [List](#jobs_list)

## ModelContainers

- [CreateOrUpdate](#modelcontainers_createorupdate)
- [Delete](#modelcontainers_delete)
- [Get](#modelcontainers_get)
- [List](#modelcontainers_list)

## ModelVersions

- [CreateOrUpdate](#modelversions_createorupdate)
- [Delete](#modelversions_delete)
- [Get](#modelversions_get)
- [List](#modelversions_list)

## OnlineDeployments

- [CreateOrUpdate](#onlinedeployments_createorupdate)
- [Delete](#onlinedeployments_delete)
- [Get](#onlinedeployments_get)
- [GetLogs](#onlinedeployments_getlogs)
- [List](#onlinedeployments_list)
- [ListSkus](#onlinedeployments_listskus)
- [Update](#onlinedeployments_update)

## OnlineEndpoints

- [CreateOrUpdate](#onlineendpoints_createorupdate)
- [Delete](#onlineendpoints_delete)
- [Get](#onlineendpoints_get)
- [GetToken](#onlineendpoints_gettoken)
- [List](#onlineendpoints_list)
- [ListKeys](#onlineendpoints_listkeys)
- [RegenerateKeys](#onlineendpoints_regeneratekeys)
- [Update](#onlineendpoints_update)

## Operations

- [List](#operations_list)

## PrivateEndpointConnections

- [CreateOrUpdate](#privateendpointconnections_createorupdate)
- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [List](#privateendpointconnections_list)

## PrivateLinkResources

- [List](#privatelinkresources_list)

## Quotas

- [List](#quotas_list)
- [Update](#quotas_update)

## Schedules

- [CreateOrUpdate](#schedules_createorupdate)
- [Delete](#schedules_delete)
- [Get](#schedules_get)
- [List](#schedules_list)

## Usages

- [List](#usages_list)

## VirtualMachineSizes

- [List](#virtualmachinesizes_list)

## WorkspaceConnections

- [Create](#workspaceconnections_create)
- [Delete](#workspaceconnections_delete)
- [Get](#workspaceconnections_get)
- [List](#workspaceconnections_list)

## WorkspaceFeatures

- [List](#workspacefeatures_list)

## Workspaces

- [CreateOrUpdate](#workspaces_createorupdate)
- [Delete](#workspaces_delete)
- [Diagnose](#workspaces_diagnose)
- [GetByResourceGroup](#workspaces_getbyresourcegroup)
- [List](#workspaces_list)
- [ListByResourceGroup](#workspaces_listbyresourcegroup)
- [ListKeys](#workspaces_listkeys)
- [ListNotebookAccessToken](#workspaces_listnotebookaccesstoken)
- [ListNotebookKeys](#workspaces_listnotebookkeys)
- [ListOutboundNetworkDependenciesEndpoints](#workspaces_listoutboundnetworkdependenciesendpoints)
- [ListStorageAccountKeys](#workspaces_liststorageaccountkeys)
- [PrepareNotebook](#workspaces_preparenotebook)
- [ResyncKeys](#workspaces_resynckeys)
- [Update](#workspaces_update)
### BatchDeployments_CreateOrUpdate

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.machinelearning.models.BatchDeploymentProperties;
import com.azure.resourcemanager.machinelearning.models.BatchLoggingLevel;
import com.azure.resourcemanager.machinelearning.models.BatchOutputAction;
import com.azure.resourcemanager.machinelearning.models.BatchRetrySettings;
import com.azure.resourcemanager.machinelearning.models.CodeConfiguration;
import com.azure.resourcemanager.machinelearning.models.DeploymentResourceConfiguration;
import com.azure.resourcemanager.machinelearning.models.IdAssetReference;
import com.azure.resourcemanager.machinelearning.models.ManagedServiceIdentity;
import com.azure.resourcemanager.machinelearning.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.machinelearning.models.Sku;
import com.azure.resourcemanager.machinelearning.models.SkuTier;
import com.azure.resourcemanager.machinelearning.models.UserAssignedIdentity;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/** Samples for BatchDeployments CreateOrUpdate. */
public final class BatchDeploymentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/BatchDeployment/createOrUpdate.json
     */
    /**
     * Sample code: CreateOrUpdate Batch Deployment.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void createOrUpdateBatchDeployment(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) throws IOException {
        manager
            .batchDeployments()
            .define("testDeploymentName")
            .withRegion("string")
            .withExistingBatchEndpoint("test-rg", "my-aml-workspace", "testEndpointName")
            .withProperties(
                new BatchDeploymentProperties()
                    .withCodeConfiguration(new CodeConfiguration().withCodeId("string").withScoringScript("string"))
                    .withDescription("string")
                    .withEnvironmentId("string")
                    .withEnvironmentVariables(mapOf("string", "string"))
                    .withProperties(mapOf("string", "string"))
                    .withCompute("string")
                    .withErrorThreshold(1)
                    .withLoggingLevel(BatchLoggingLevel.INFO)
                    .withMaxConcurrencyPerInstance(1)
                    .withMiniBatchSize(1L)
                    .withModel(new IdAssetReference().withAssetId("string"))
                    .withOutputAction(BatchOutputAction.SUMMARY_ONLY)
                    .withOutputFileName("string")
                    .withResources(
                        new DeploymentResourceConfiguration()
                            .withInstanceCount(1)
                            .withInstanceType("string")
                            .withProperties(
                                mapOf(
                                    "string",
                                    SerializerFactory
                                        .createDefaultManagementSerializerAdapter()
                                        .deserialize(
                                            "{\"cd3c37dc-2876-4ca4-8a54-21bd7619724a\":null}",
                                            Object.class,
                                            SerializerEncoding.JSON))))
                    .withRetrySettings(new BatchRetrySettings().withMaxRetries(1).withTimeout(Duration.parse("PT5M"))))
            .withTags(mapOf())
            .withIdentity(
                new ManagedServiceIdentity()
                    .withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED)
                    .withUserAssignedIdentities(mapOf("string", new UserAssignedIdentity())))
            .withKind("string")
            .withSku(
                new Sku()
                    .withName("string")
                    .withTier(SkuTier.FREE)
                    .withSize("string")
                    .withFamily("string")
                    .withCapacity(1))
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

### BatchDeployments_Delete

```java
import com.azure.core.util.Context;

/** Samples for BatchDeployments Delete. */
public final class BatchDeploymentsDeleteSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/BatchDeployment/delete.json
     */
    /**
     * Sample code: Delete Batch Deployment.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void deleteBatchDeployment(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .batchDeployments()
            .delete("test-rg", "my-aml-workspace", "testEndpointName", "testDeploymentName", Context.NONE);
    }
}
```

### BatchDeployments_Get

```java
import com.azure.core.util.Context;

/** Samples for BatchDeployments Get. */
public final class BatchDeploymentsGetSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/BatchDeployment/get.json
     */
    /**
     * Sample code: Get Batch Deployment.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void getBatchDeployment(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .batchDeployments()
            .getWithResponse("test-rg", "my-aml-workspace", "testEndpointName", "testDeploymentName", Context.NONE);
    }
}
```

### BatchDeployments_List

```java
import com.azure.core.util.Context;

/** Samples for BatchDeployments List. */
public final class BatchDeploymentsListSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/BatchDeployment/list.json
     */
    /**
     * Sample code: List Batch Deployment.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void listBatchDeployment(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .batchDeployments()
            .list("test-rg", "my-aml-workspace", "testEndpointName", "string", 1, null, Context.NONE);
    }
}
```

### BatchDeployments_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.machinelearning.models.BatchDeployment;
import com.azure.resourcemanager.machinelearning.models.PartialBatchDeployment;
import java.util.HashMap;
import java.util.Map;

/** Samples for BatchDeployments Update. */
public final class BatchDeploymentsUpdateSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/BatchDeployment/update.json
     */
    /**
     * Sample code: Update Batch Deployment.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void updateBatchDeployment(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        BatchDeployment resource =
            manager
                .batchDeployments()
                .getWithResponse("test-rg", "my-aml-workspace", "testEndpointName", "testDeploymentName", Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf())
            .withProperties(new PartialBatchDeployment().withDescription("string"))
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

### BatchEndpoints_CreateOrUpdate

```java
import com.azure.resourcemanager.machinelearning.models.BatchEndpointDefaults;
import com.azure.resourcemanager.machinelearning.models.BatchEndpointProperties;
import com.azure.resourcemanager.machinelearning.models.EndpointAuthMode;
import com.azure.resourcemanager.machinelearning.models.ManagedServiceIdentity;
import com.azure.resourcemanager.machinelearning.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.machinelearning.models.Sku;
import com.azure.resourcemanager.machinelearning.models.SkuTier;
import com.azure.resourcemanager.machinelearning.models.UserAssignedIdentity;
import java.util.HashMap;
import java.util.Map;

/** Samples for BatchEndpoints CreateOrUpdate. */
public final class BatchEndpointsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/BatchEndpoint/createOrUpdate.json
     */
    /**
     * Sample code: CreateOrUpdate Batch Endpoint.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void createOrUpdateBatchEndpoint(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .batchEndpoints()
            .define("testEndpointName")
            .withRegion("string")
            .withExistingWorkspace("test-rg", "my-aml-workspace")
            .withProperties(
                new BatchEndpointProperties()
                    .withAuthMode(EndpointAuthMode.AMLTOKEN)
                    .withDescription("string")
                    .withProperties(mapOf("string", "string"))
                    .withDefaults(new BatchEndpointDefaults().withDeploymentName("string")))
            .withTags(mapOf())
            .withIdentity(
                new ManagedServiceIdentity()
                    .withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED)
                    .withUserAssignedIdentities(mapOf("string", new UserAssignedIdentity())))
            .withKind("string")
            .withSku(
                new Sku()
                    .withName("string")
                    .withTier(SkuTier.FREE)
                    .withSize("string")
                    .withFamily("string")
                    .withCapacity(1))
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

### BatchEndpoints_Delete

```java
import com.azure.core.util.Context;

/** Samples for BatchEndpoints Delete. */
public final class BatchEndpointsDeleteSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/BatchEndpoint/delete.json
     */
    /**
     * Sample code: Delete Batch Endpoint.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void deleteBatchEndpoint(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.batchEndpoints().delete("resourceGroup-1234", "testworkspace", "testBatchEndpoint", Context.NONE);
    }
}
```

### BatchEndpoints_Get

```java
import com.azure.core.util.Context;

/** Samples for BatchEndpoints Get. */
public final class BatchEndpointsGetSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/BatchEndpoint/get.json
     */
    /**
     * Sample code: Get Batch Endpoint.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void getBatchEndpoint(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.batchEndpoints().getWithResponse("test-rg", "my-aml-workspace", "testEndpointName", Context.NONE);
    }
}
```

### BatchEndpoints_List

```java
import com.azure.core.util.Context;

/** Samples for BatchEndpoints List. */
public final class BatchEndpointsListSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/BatchEndpoint/list.json
     */
    /**
     * Sample code: List Batch Endpoint.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void listBatchEndpoint(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.batchEndpoints().list("test-rg", "my-aml-workspace", 1, null, Context.NONE);
    }
}
```

### BatchEndpoints_ListKeys

```java
import com.azure.core.util.Context;

/** Samples for BatchEndpoints ListKeys. */
public final class BatchEndpointsListKeysSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/BatchEndpoint/listKeys.json
     */
    /**
     * Sample code: ListKeys Batch Endpoint.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void listKeysBatchEndpoint(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.batchEndpoints().listKeysWithResponse("test-rg", "my-aml-workspace", "testEndpointName", Context.NONE);
    }
}
```

### BatchEndpoints_Update

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.machinelearning.models.BatchEndpoint;
import com.azure.resourcemanager.machinelearning.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.machinelearning.models.PartialManagedServiceIdentity;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** Samples for BatchEndpoints Update. */
public final class BatchEndpointsUpdateSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/BatchEndpoint/update.json
     */
    /**
     * Sample code: Update Batch Endpoint.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void updateBatchEndpoint(com.azure.resourcemanager.machinelearning.MachineLearningManager manager)
        throws IOException {
        BatchEndpoint resource =
            manager
                .batchEndpoints()
                .getWithResponse("test-rg", "my-aml-workspace", "testEndpointName", Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf())
            .withIdentity(
                new PartialManagedServiceIdentity()
                    .withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "string",
                            SerializerFactory
                                .createDefaultManagementSerializerAdapter()
                                .deserialize("{}", Object.class, SerializerEncoding.JSON))))
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

### CodeContainers_CreateOrUpdate

```java
import com.azure.resourcemanager.machinelearning.models.CodeContainerProperties;
import java.util.HashMap;
import java.util.Map;

/** Samples for CodeContainers CreateOrUpdate. */
public final class CodeContainersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/CodeContainer/createOrUpdate.json
     */
    /**
     * Sample code: CreateOrUpdate Code Container.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void createOrUpdateCodeContainer(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .codeContainers()
            .define("testContainer")
            .withExistingWorkspace("testrg123", "testworkspace")
            .withProperties(
                new CodeContainerProperties()
                    .withDescription("string")
                    .withTags(mapOf("tag1", "value1", "tag2", "value2")))
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

### CodeContainers_Delete

```java
import com.azure.core.util.Context;

/** Samples for CodeContainers Delete. */
public final class CodeContainersDeleteSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/CodeContainer/delete.json
     */
    /**
     * Sample code: Delete Code Container.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void deleteCodeContainer(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.codeContainers().deleteWithResponse("testrg123", "testworkspace", "testContainer", Context.NONE);
    }
}
```

### CodeContainers_Get

```java
import com.azure.core.util.Context;

/** Samples for CodeContainers Get. */
public final class CodeContainersGetSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/CodeContainer/get.json
     */
    /**
     * Sample code: Get Code Container.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void getCodeContainer(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.codeContainers().getWithResponse("testrg123", "testworkspace", "testContainer", Context.NONE);
    }
}
```

### CodeContainers_List

```java
import com.azure.core.util.Context;

/** Samples for CodeContainers List. */
public final class CodeContainersListSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/CodeContainer/list.json
     */
    /**
     * Sample code: List Code Container.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void listCodeContainer(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.codeContainers().list("testrg123", "testworkspace", null, Context.NONE);
    }
}
```

### CodeVersions_CreateOrUpdate

```java
import com.azure.resourcemanager.machinelearning.models.CodeVersionProperties;
import java.util.HashMap;
import java.util.Map;

/** Samples for CodeVersions CreateOrUpdate. */
public final class CodeVersionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/CodeVersion/createOrUpdate.json
     */
    /**
     * Sample code: CreateOrUpdate Code Version.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void createOrUpdateCodeVersion(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .codeVersions()
            .define("string")
            .withExistingCode("test-rg", "my-aml-workspace", "string")
            .withProperties(
                new CodeVersionProperties()
                    .withDescription("string")
                    .withProperties(mapOf("string", "string"))
                    .withTags(mapOf("string", "string"))
                    .withIsAnonymous(false)
                    .withCodeUri("https://blobStorage/folderName"))
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

### CodeVersions_Delete

```java
import com.azure.core.util.Context;

/** Samples for CodeVersions Delete. */
public final class CodeVersionsDeleteSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/CodeVersion/delete.json
     */
    /**
     * Sample code: Delete Code Version.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void deleteCodeVersion(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.codeVersions().deleteWithResponse("test-rg", "my-aml-workspace", "string", "string", Context.NONE);
    }
}
```

### CodeVersions_Get

```java
import com.azure.core.util.Context;

/** Samples for CodeVersions Get. */
public final class CodeVersionsGetSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/CodeVersion/get.json
     */
    /**
     * Sample code: Get Code Version.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void getCodeVersion(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.codeVersions().getWithResponse("test-rg", "my-aml-workspace", "string", "string", Context.NONE);
    }
}
```

### CodeVersions_List

```java
import com.azure.core.util.Context;

/** Samples for CodeVersions List. */
public final class CodeVersionsListSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/CodeVersion/list.json
     */
    /**
     * Sample code: List Code Version.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void listCodeVersion(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.codeVersions().list("test-rg", "my-aml-workspace", "string", "string", 1, null, Context.NONE);
    }
}
```

### ComponentContainers_CreateOrUpdate

```java
import com.azure.resourcemanager.machinelearning.models.ComponentContainerProperties;
import java.util.HashMap;
import java.util.Map;

/** Samples for ComponentContainers CreateOrUpdate. */
public final class ComponentContainersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/ComponentContainer/createOrUpdate.json
     */
    /**
     * Sample code: CreateOrUpdate Component Container.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void createOrUpdateComponentContainer(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .componentContainers()
            .define("string")
            .withExistingWorkspace("test-rg", "my-aml-workspace")
            .withProperties(
                new ComponentContainerProperties()
                    .withDescription("string")
                    .withProperties(mapOf("string", "string"))
                    .withTags(mapOf("string", "string")))
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

### ComponentContainers_Delete

```java
import com.azure.core.util.Context;

/** Samples for ComponentContainers Delete. */
public final class ComponentContainersDeleteSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/ComponentContainer/delete.json
     */
    /**
     * Sample code: Delete Component Container.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void deleteComponentContainer(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.componentContainers().deleteWithResponse("test-rg", "my-aml-workspace", "string", Context.NONE);
    }
}
```

### ComponentContainers_Get

```java
import com.azure.core.util.Context;

/** Samples for ComponentContainers Get. */
public final class ComponentContainersGetSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/ComponentContainer/get.json
     */
    /**
     * Sample code: Get Component Container.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void getComponentContainer(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.componentContainers().getWithResponse("test-rg", "my-aml-workspace", "string", Context.NONE);
    }
}
```

### ComponentContainers_List

```java
import com.azure.core.util.Context;

/** Samples for ComponentContainers List. */
public final class ComponentContainersListSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/ComponentContainer/list.json
     */
    /**
     * Sample code: List Component Container.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void listComponentContainer(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.componentContainers().list("test-rg", "my-aml-workspace", null, null, Context.NONE);
    }
}
```

### ComponentVersions_CreateOrUpdate

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.machinelearning.models.ComponentVersionProperties;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** Samples for ComponentVersions CreateOrUpdate. */
public final class ComponentVersionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/ComponentVersion/createOrUpdate.json
     */
    /**
     * Sample code: CreateOrUpdate Component Version.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void createOrUpdateComponentVersion(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) throws IOException {
        manager
            .componentVersions()
            .define("string")
            .withExistingComponent("test-rg", "my-aml-workspace", "string")
            .withProperties(
                new ComponentVersionProperties()
                    .withDescription("string")
                    .withProperties(mapOf("string", "string"))
                    .withTags(mapOf("string", "string"))
                    .withIsAnonymous(false)
                    .withComponentSpec(
                        SerializerFactory
                            .createDefaultManagementSerializerAdapter()
                            .deserialize(
                                "{\"8ced901b-d826-477d-bfef-329da9672513\":null}",
                                Object.class,
                                SerializerEncoding.JSON)))
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

### ComponentVersions_Delete

```java
import com.azure.core.util.Context;

/** Samples for ComponentVersions Delete. */
public final class ComponentVersionsDeleteSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/ComponentVersion/delete.json
     */
    /**
     * Sample code: Delete Component Version.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void deleteComponentVersion(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.componentVersions().deleteWithResponse("test-rg", "my-aml-workspace", "string", "string", Context.NONE);
    }
}
```

### ComponentVersions_Get

```java
import com.azure.core.util.Context;

/** Samples for ComponentVersions Get. */
public final class ComponentVersionsGetSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/ComponentVersion/get.json
     */
    /**
     * Sample code: Get Component Version.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void getComponentVersion(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.componentVersions().getWithResponse("test-rg", "my-aml-workspace", "string", "string", Context.NONE);
    }
}
```

### ComponentVersions_List

```java
import com.azure.core.util.Context;

/** Samples for ComponentVersions List. */
public final class ComponentVersionsListSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/ComponentVersion/list.json
     */
    /**
     * Sample code: List Component Version.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void listComponentVersion(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .componentVersions()
            .list("test-rg", "my-aml-workspace", "string", "string", 1, null, null, Context.NONE);
    }
}
```

### Compute_CreateOrUpdate

```java
import com.azure.resourcemanager.machinelearning.models.Aks;
import com.azure.resourcemanager.machinelearning.models.AksSchemaProperties;
import com.azure.resourcemanager.machinelearning.models.AmlCompute;
import com.azure.resourcemanager.machinelearning.models.AmlComputeProperties;
import com.azure.resourcemanager.machinelearning.models.ApplicationSharingPolicy;
import com.azure.resourcemanager.machinelearning.models.AssignedUser;
import com.azure.resourcemanager.machinelearning.models.ComputeInstance;
import com.azure.resourcemanager.machinelearning.models.ComputeInstanceAuthorizationType;
import com.azure.resourcemanager.machinelearning.models.ComputeInstanceProperties;
import com.azure.resourcemanager.machinelearning.models.ComputeInstanceSshSettings;
import com.azure.resourcemanager.machinelearning.models.DataFactory;
import com.azure.resourcemanager.machinelearning.models.InstanceTypeSchema;
import com.azure.resourcemanager.machinelearning.models.InstanceTypeSchemaResources;
import com.azure.resourcemanager.machinelearning.models.Kubernetes;
import com.azure.resourcemanager.machinelearning.models.KubernetesProperties;
import com.azure.resourcemanager.machinelearning.models.OsType;
import com.azure.resourcemanager.machinelearning.models.PersonalComputeInstanceSettings;
import com.azure.resourcemanager.machinelearning.models.RemoteLoginPortPublicAccess;
import com.azure.resourcemanager.machinelearning.models.ResourceId;
import com.azure.resourcemanager.machinelearning.models.ScaleSettings;
import com.azure.resourcemanager.machinelearning.models.SshPublicAccess;
import com.azure.resourcemanager.machinelearning.models.VirtualMachineImage;
import com.azure.resourcemanager.machinelearning.models.VmPriority;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/** Samples for Compute CreateOrUpdate. */
public final class ComputeCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Compute/createOrUpdate/BasicAKSCompute.json
     */
    /**
     * Sample code: Create an AKS Compute.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void createAnAKSCompute(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .computes()
            .define("compute123")
            .withExistingWorkspace("testrg123", "workspaces123")
            .withRegion("eastus")
            .withProperties(new Aks())
            .create();
    }

    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Compute/createOrUpdate/AKSCompute.json
     */
    /**
     * Sample code: Update an AKS Compute.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void updateAnAKSCompute(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .computes()
            .define("compute123")
            .withExistingWorkspace("testrg123", "workspaces123")
            .withRegion("eastus")
            .withProperties(
                new Aks()
                    .withDescription("some compute")
                    .withResourceId(
                        "/subscriptions/34adfa4f-cedf-4dc0-ba29-b6d1a69ab345/resourcegroups/testrg123/providers/Microsoft.ContainerService/managedClusters/compute123-56826-c9b00420020b2")
                    .withProperties(new AksSchemaProperties().withAgentCount(4)))
            .create();
    }

    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Compute/createOrUpdate/KubernetesCompute.json
     */
    /**
     * Sample code: Attach a Kubernetes Compute.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void attachAKubernetesCompute(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .computes()
            .define("compute123")
            .withExistingWorkspace("testrg123", "workspaces123")
            .withRegion("eastus")
            .withProperties(
                new Kubernetes()
                    .withDescription("some compute")
                    .withResourceId(
                        "/subscriptions/34adfa4f-cedf-4dc0-ba29-b6d1a69ab345/resourcegroups/testrg123/providers/Microsoft.ContainerService/managedClusters/compute123-56826-c9b00420020b2")
                    .withProperties(
                        new KubernetesProperties()
                            .withNamespace("default")
                            .withDefaultInstanceType("defaultInstanceType")
                            .withInstanceTypes(
                                mapOf(
                                    "defaultInstanceType",
                                    new InstanceTypeSchema()
                                        .withResources(
                                            new InstanceTypeSchemaResources()
                                                .withRequests(
                                                    mapOf("cpu", "1", "memory", "4Gi", "nvidia.com/gpu", null))
                                                .withLimits(
                                                    mapOf("cpu", "1", "memory", "4Gi", "nvidia.com/gpu", null)))))))
            .create();
    }

    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Compute/createOrUpdate/ComputeInstanceWithSchedules.json
     */
    /**
     * Sample code: Create an ComputeInstance Compute with Schedules.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void createAnComputeInstanceComputeWithSchedules(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .computes()
            .define("compute123")
            .withExistingWorkspace("testrg123", "workspaces123")
            .withRegion("eastus")
            .withProperties(
                new ComputeInstance()
                    .withProperties(
                        new ComputeInstanceProperties()
                            .withVmSize("STANDARD_NC6")
                            .withApplicationSharingPolicy(ApplicationSharingPolicy.PERSONAL)
                            .withSshSettings(
                                new ComputeInstanceSshSettings().withSshPublicAccess(SshPublicAccess.DISABLED))
                            .withComputeInstanceAuthorizationType(ComputeInstanceAuthorizationType.PERSONAL)
                            .withPersonalComputeInstanceSettings(
                                new PersonalComputeInstanceSettings()
                                    .withAssignedUser(
                                        new AssignedUser()
                                            .withObjectId("00000000-0000-0000-0000-000000000000")
                                            .withTenantId("00000000-0000-0000-0000-000000000000")))))
            .create();
    }

    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Compute/createOrUpdate/BasicAmlCompute.json
     */
    /**
     * Sample code: Create a AML Compute.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void createAAMLCompute(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .computes()
            .define("compute123")
            .withExistingWorkspace("testrg123", "workspaces123")
            .withRegion("eastus")
            .withProperties(
                new AmlCompute()
                    .withProperties(
                        new AmlComputeProperties()
                            .withOsType(OsType.WINDOWS)
                            .withVmSize("STANDARD_NC6")
                            .withVmPriority(VmPriority.DEDICATED)
                            .withVirtualMachineImage(
                                new VirtualMachineImage()
                                    .withId(
                                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.Compute/galleries/myImageGallery/images/myImageDefinition/versions/0.0.1"))
                            .withIsolatedNetwork(false)
                            .withScaleSettings(
                                new ScaleSettings()
                                    .withMaxNodeCount(1)
                                    .withMinNodeCount(0)
                                    .withNodeIdleTimeBeforeScaleDown(Duration.parse("PT5M")))
                            .withRemoteLoginPortPublicAccess(RemoteLoginPortPublicAccess.NOT_SPECIFIED)
                            .withEnableNodePublicIp(true)))
            .create();
    }

    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Compute/createOrUpdate/ComputeInstance.json
     */
    /**
     * Sample code: Create an ComputeInstance Compute.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void createAnComputeInstanceCompute(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .computes()
            .define("compute123")
            .withExistingWorkspace("testrg123", "workspaces123")
            .withRegion("eastus")
            .withProperties(
                new ComputeInstance()
                    .withProperties(
                        new ComputeInstanceProperties()
                            .withVmSize("STANDARD_NC6")
                            .withSubnet(new ResourceId().withId("test-subnet-resource-id"))
                            .withApplicationSharingPolicy(ApplicationSharingPolicy.PERSONAL)
                            .withSshSettings(
                                new ComputeInstanceSshSettings().withSshPublicAccess(SshPublicAccess.DISABLED))
                            .withComputeInstanceAuthorizationType(ComputeInstanceAuthorizationType.PERSONAL)
                            .withPersonalComputeInstanceSettings(
                                new PersonalComputeInstanceSettings()
                                    .withAssignedUser(
                                        new AssignedUser()
                                            .withObjectId("00000000-0000-0000-0000-000000000000")
                                            .withTenantId("00000000-0000-0000-0000-000000000000")))))
            .create();
    }

    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Compute/createOrUpdate/ComputeInstanceMinimal.json
     */
    /**
     * Sample code: Create an ComputeInstance Compute with minimal inputs.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void createAnComputeInstanceComputeWithMinimalInputs(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .computes()
            .define("compute123")
            .withExistingWorkspace("testrg123", "workspaces123")
            .withRegion("eastus")
            .withProperties(
                new ComputeInstance().withProperties(new ComputeInstanceProperties().withVmSize("STANDARD_NC6")))
            .create();
    }

    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Compute/createOrUpdate/AmlCompute.json
     */
    /**
     * Sample code: Update a AML Compute.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void updateAAMLCompute(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .computes()
            .define("compute123")
            .withExistingWorkspace("testrg123", "workspaces123")
            .withRegion("eastus")
            .withProperties(
                new AmlCompute()
                    .withDescription("some compute")
                    .withProperties(
                        new AmlComputeProperties()
                            .withScaleSettings(
                                new ScaleSettings()
                                    .withMaxNodeCount(4)
                                    .withMinNodeCount(4)
                                    .withNodeIdleTimeBeforeScaleDown(Duration.parse("PT5M")))))
            .create();
    }

    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Compute/createOrUpdate/BasicDataFactoryCompute.json
     */
    /**
     * Sample code: Create a DataFactory Compute.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void createADataFactoryCompute(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .computes()
            .define("compute123")
            .withExistingWorkspace("testrg123", "workspaces123")
            .withRegion("eastus")
            .withProperties(new DataFactory())
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

### Compute_Delete

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.machinelearning.models.UnderlyingResourceAction;

/** Samples for Compute Delete. */
public final class ComputeDeleteSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Compute/delete.json
     */
    /**
     * Sample code: Delete Compute.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void deleteCompute(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .computes()
            .delete("testrg123", "workspaces123", "compute123", UnderlyingResourceAction.DELETE, Context.NONE);
    }
}
```

### Compute_Get

```java
import com.azure.core.util.Context;

/** Samples for Compute Get. */
public final class ComputeGetSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Compute/get/AKSCompute.json
     */
    /**
     * Sample code: Get a AKS Compute.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void getAAKSCompute(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.computes().getWithResponse("testrg123", "workspaces123", "compute123", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Compute/get/KubernetesCompute.json
     */
    /**
     * Sample code: Get a Kubernetes Compute.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void getAKubernetesCompute(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.computes().getWithResponse("testrg123", "workspaces123", "compute123", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Compute/get/ComputeInstance.json
     */
    /**
     * Sample code: Get an ComputeInstance.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void getAnComputeInstance(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.computes().getWithResponse("testrg123", "workspaces123", "compute123", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Compute/get/AmlCompute.json
     */
    /**
     * Sample code: Get a AML Compute.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void getAAMLCompute(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.computes().getWithResponse("testrg123", "workspaces123", "compute123", Context.NONE);
    }
}
```

### Compute_List

```java
import com.azure.core.util.Context;

/** Samples for Compute List. */
public final class ComputeListSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Compute/list.json
     */
    /**
     * Sample code: Get Computes.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void getComputes(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.computes().list("testrg123", "workspaces123", null, Context.NONE);
    }
}
```

### Compute_ListKeys

```java
import com.azure.core.util.Context;

/** Samples for Compute ListKeys. */
public final class ComputeListKeysSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Compute/listKeys.json
     */
    /**
     * Sample code: List AKS Compute Keys.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void listAKSComputeKeys(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.computes().listKeysWithResponse("testrg123", "workspaces123", "compute123", Context.NONE);
    }
}
```

### Compute_ListNodes

```java
import com.azure.core.util.Context;

/** Samples for Compute ListNodes. */
public final class ComputeListNodesSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Compute/listNodes.json
     */
    /**
     * Sample code: Get compute nodes information for a compute.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void getComputeNodesInformationForACompute(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.computes().listNodes("testrg123", "workspaces123", "compute123", Context.NONE);
    }
}
```

### Compute_Restart

```java
import com.azure.core.util.Context;

/** Samples for Compute Restart. */
public final class ComputeRestartSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Compute/restart.json
     */
    /**
     * Sample code: Restart ComputeInstance Compute.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void restartComputeInstanceCompute(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.computes().restart("testrg123", "workspaces123", "compute123", Context.NONE);
    }
}
```

### Compute_Start

```java
import com.azure.core.util.Context;

/** Samples for Compute Start. */
public final class ComputeStartSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Compute/start.json
     */
    /**
     * Sample code: Start ComputeInstance Compute.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void startComputeInstanceCompute(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.computes().start("testrg123", "workspaces123", "compute123", Context.NONE);
    }
}
```

### Compute_Stop

```java
import com.azure.core.util.Context;

/** Samples for Compute Stop. */
public final class ComputeStopSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Compute/stop.json
     */
    /**
     * Sample code: Stop ComputeInstance Compute.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void stopComputeInstanceCompute(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.computes().stop("testrg123", "workspaces123", "compute123", Context.NONE);
    }
}
```

### Compute_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.machinelearning.models.ComputeResource;
import com.azure.resourcemanager.machinelearning.models.ScaleSettings;
import com.azure.resourcemanager.machinelearning.models.ScaleSettingsInformation;
import java.time.Duration;

/** Samples for Compute Update. */
public final class ComputeUpdateSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Compute/patch.json
     */
    /**
     * Sample code: Update a AmlCompute Compute.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void updateAAmlComputeCompute(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        ComputeResource resource =
            manager.computes().getWithResponse("testrg123", "workspaces123", "compute123", Context.NONE).getValue();
        resource
            .update()
            .withProperties(
                new ScaleSettingsInformation()
                    .withScaleSettings(
                        new ScaleSettings()
                            .withMaxNodeCount(4)
                            .withMinNodeCount(4)
                            .withNodeIdleTimeBeforeScaleDown(Duration.parse("PT5M"))))
            .apply();
    }
}
```

### DataContainers_CreateOrUpdate

```java
import com.azure.resourcemanager.machinelearning.models.DataContainerProperties;
import com.azure.resourcemanager.machinelearning.models.DataType;
import java.util.HashMap;
import java.util.Map;

/** Samples for DataContainers CreateOrUpdate. */
public final class DataContainersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/DataContainer/createOrUpdate.json
     */
    /**
     * Sample code: CreateOrUpdate Data Container.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void createOrUpdateDataContainer(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .dataContainers()
            .define("datacontainer123")
            .withExistingWorkspace("testrg123", "workspace123")
            .withProperties(
                new DataContainerProperties()
                    .withDescription("string")
                    .withProperties(mapOf("properties1", "value1", "properties2", "value2"))
                    .withTags(mapOf("tag1", "value1", "tag2", "value2"))
                    .withDataType(DataType.fromString("UriFile")))
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

### DataContainers_Delete

```java
import com.azure.core.util.Context;

/** Samples for DataContainers Delete. */
public final class DataContainersDeleteSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/DataContainer/delete.json
     */
    /**
     * Sample code: Delete Data Container.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void deleteDataContainer(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.dataContainers().deleteWithResponse("testrg123", "workspace123", "datacontainer123", Context.NONE);
    }
}
```

### DataContainers_Get

```java
import com.azure.core.util.Context;

/** Samples for DataContainers Get. */
public final class DataContainersGetSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/DataContainer/get.json
     */
    /**
     * Sample code: Get Data Container.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void getDataContainer(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.dataContainers().getWithResponse("testrg123", "workspace123", "datacontainer123", Context.NONE);
    }
}
```

### DataContainers_List

```java
import com.azure.core.util.Context;

/** Samples for DataContainers List. */
public final class DataContainersListSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/DataContainer/list.json
     */
    /**
     * Sample code: List Data Container.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void listDataContainer(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.dataContainers().list("testrg123", "workspace123", null, null, Context.NONE);
    }
}
```

### DataVersions_CreateOrUpdate

```java
import com.azure.resourcemanager.machinelearning.models.UriFileDataVersion;
import java.util.HashMap;
import java.util.Map;

/** Samples for DataVersions CreateOrUpdate. */
public final class DataVersionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/DataVersionBase/createOrUpdate.json
     */
    /**
     * Sample code: CreateOrUpdate Data Version Base.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void createOrUpdateDataVersionBase(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .dataVersions()
            .define("string")
            .withExistingData("test-rg", "my-aml-workspace", "string")
            .withProperties(
                new UriFileDataVersion()
                    .withDescription("string")
                    .withProperties(mapOf("string", "string"))
                    .withTags(mapOf("string", "string"))
                    .withIsAnonymous(false)
                    .withDataUri("string"))
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

### DataVersions_Delete

```java
import com.azure.core.util.Context;

/** Samples for DataVersions Delete. */
public final class DataVersionsDeleteSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/DataVersionBase/delete.json
     */
    /**
     * Sample code: Delete Data Version Base.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void deleteDataVersionBase(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.dataVersions().deleteWithResponse("test-rg", "my-aml-workspace", "string", "string", Context.NONE);
    }
}
```

### DataVersions_Get

```java
import com.azure.core.util.Context;

/** Samples for DataVersions Get. */
public final class DataVersionsGetSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/DataVersionBase/get.json
     */
    /**
     * Sample code: Get Data Version Base.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void getDataVersionBase(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.dataVersions().getWithResponse("test-rg", "my-aml-workspace", "string", "string", Context.NONE);
    }
}
```

### DataVersions_List

```java
import com.azure.core.util.Context;

/** Samples for DataVersions List. */
public final class DataVersionsListSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/DataVersionBase/list.json
     */
    /**
     * Sample code: List Data Version Base.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void listDataVersionBase(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .dataVersions()
            .list("test-rg", "my-aml-workspace", "string", "string", 1, null, "string", null, Context.NONE);
    }
}
```

### Datastores_CreateOrUpdate

```java
import com.azure.resourcemanager.machinelearning.models.AccountKeyDatastoreCredentials;
import com.azure.resourcemanager.machinelearning.models.AccountKeyDatastoreSecrets;
import com.azure.resourcemanager.machinelearning.models.AzureBlobDatastore;
import com.azure.resourcemanager.machinelearning.models.AzureDataLakeGen1Datastore;
import com.azure.resourcemanager.machinelearning.models.AzureDataLakeGen2Datastore;
import com.azure.resourcemanager.machinelearning.models.AzureFileDatastore;
import com.azure.resourcemanager.machinelearning.models.ServicePrincipalDatastoreCredentials;
import com.azure.resourcemanager.machinelearning.models.ServicePrincipalDatastoreSecrets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Samples for Datastores CreateOrUpdate. */
public final class DatastoresCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Datastore/AzureDataLakeGen1WServicePrincipal/createOrUpdate.json
     */
    /**
     * Sample code: CreateOrUpdate datastore (Azure Data Lake Gen1 w/ ServicePrincipal).
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void createOrUpdateDatastoreAzureDataLakeGen1WServicePrincipal(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .datastores()
            .define("string")
            .withExistingWorkspace("test-rg", "my-aml-workspace")
            .withProperties(
                new AzureDataLakeGen1Datastore()
                    .withDescription("string")
                    .withTags(mapOf("string", "string"))
                    .withCredentials(
                        new ServicePrincipalDatastoreCredentials()
                            .withAuthorityUrl("string")
                            .withClientId(UUID.fromString("00000000-1111-2222-3333-444444444444"))
                            .withResourceUrl("string")
                            .withSecrets(new ServicePrincipalDatastoreSecrets().withClientSecret("string"))
                            .withTenantId(UUID.fromString("00000000-1111-2222-3333-444444444444")))
                    .withStoreName("string"))
            .withSkipValidation(false)
            .create();
    }

    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Datastore/AzureDataLakeGen2WServicePrincipal/createOrUpdate.json
     */
    /**
     * Sample code: CreateOrUpdate datastore (Azure Data Lake Gen2 w/ Service Principal).
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void createOrUpdateDatastoreAzureDataLakeGen2WServicePrincipal(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .datastores()
            .define("string")
            .withExistingWorkspace("test-rg", "my-aml-workspace")
            .withProperties(
                new AzureDataLakeGen2Datastore()
                    .withDescription("string")
                    .withTags(mapOf("string", "string"))
                    .withCredentials(
                        new ServicePrincipalDatastoreCredentials()
                            .withAuthorityUrl("string")
                            .withClientId(UUID.fromString("00000000-1111-2222-3333-444444444444"))
                            .withResourceUrl("string")
                            .withSecrets(new ServicePrincipalDatastoreSecrets().withClientSecret("string"))
                            .withTenantId(UUID.fromString("00000000-1111-2222-3333-444444444444")))
                    .withAccountName("string")
                    .withEndpoint("string")
                    .withFilesystem("string")
                    .withProtocol("string"))
            .withSkipValidation(false)
            .create();
    }

    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Datastore/AzureBlobWAccountKey/createOrUpdate.json
     */
    /**
     * Sample code: CreateOrUpdate datastore (AzureBlob w/ AccountKey).
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void createOrUpdateDatastoreAzureBlobWAccountKey(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .datastores()
            .define("string")
            .withExistingWorkspace("test-rg", "my-aml-workspace")
            .withProperties(
                new AzureBlobDatastore()
                    .withDescription("string")
                    .withTags(mapOf("string", "string"))
                    .withCredentials(
                        new AccountKeyDatastoreCredentials()
                            .withSecrets(new AccountKeyDatastoreSecrets().withKey("string")))
                    .withAccountName("string")
                    .withContainerName("string")
                    .withEndpoint("core.windows.net")
                    .withProtocol("https"))
            .withSkipValidation(false)
            .create();
    }

    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Datastore/AzureFileWAccountKey/createOrUpdate.json
     */
    /**
     * Sample code: CreateOrUpdate datastore (Azure File store w/ AccountKey).
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void createOrUpdateDatastoreAzureFileStoreWAccountKey(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .datastores()
            .define("string")
            .withExistingWorkspace("test-rg", "my-aml-workspace")
            .withProperties(
                new AzureFileDatastore()
                    .withDescription("string")
                    .withTags(mapOf("string", "string"))
                    .withCredentials(
                        new AccountKeyDatastoreCredentials()
                            .withSecrets(new AccountKeyDatastoreSecrets().withKey("string")))
                    .withAccountName("string")
                    .withEndpoint("string")
                    .withFileShareName("string")
                    .withProtocol("string"))
            .withSkipValidation(false)
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

### Datastores_Delete

```java
import com.azure.core.util.Context;

/** Samples for Datastores Delete. */
public final class DatastoresDeleteSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Datastore/delete.json
     */
    /**
     * Sample code: Delete datastore.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void deleteDatastore(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.datastores().deleteWithResponse("test-rg", "my-aml-workspace", "string", Context.NONE);
    }
}
```

### Datastores_Get

```java
import com.azure.core.util.Context;

/** Samples for Datastores Get. */
public final class DatastoresGetSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Datastore/get.json
     */
    /**
     * Sample code: Get datastore.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void getDatastore(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.datastores().getWithResponse("test-rg", "my-aml-workspace", "string", Context.NONE);
    }
}
```

### Datastores_List

```java
import com.azure.core.util.Context;
import java.util.Arrays;

/** Samples for Datastores List. */
public final class DatastoresListSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Datastore/list.json
     */
    /**
     * Sample code: List datastores.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void listDatastores(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .datastores()
            .list(
                "test-rg",
                "my-aml-workspace",
                null,
                1,
                false,
                Arrays.asList("string"),
                "string",
                "string",
                false,
                Context.NONE);
    }
}
```

### Datastores_ListSecrets

```java
import com.azure.core.util.Context;

/** Samples for Datastores ListSecrets. */
public final class DatastoresListSecretsSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Datastore/listSecrets.json
     */
    /**
     * Sample code: Get datastore secrets.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void getDatastoreSecrets(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.datastores().listSecretsWithResponse("test-rg", "my-aml-workspace", "string", Context.NONE);
    }
}
```

### EnvironmentContainers_CreateOrUpdate

```java
import com.azure.resourcemanager.machinelearning.models.EnvironmentContainerProperties;
import java.util.HashMap;
import java.util.Map;

/** Samples for EnvironmentContainers CreateOrUpdate. */
public final class EnvironmentContainersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/EnvironmentContainer/createOrUpdate.json
     */
    /**
     * Sample code: CreateOrUpdate Environment Container.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void createOrUpdateEnvironmentContainer(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .environmentContainers()
            .define("testEnvironment")
            .withExistingWorkspace("testrg123", "testworkspace")
            .withProperties(
                new EnvironmentContainerProperties()
                    .withDescription("string")
                    .withProperties(
                        mapOf("additionalProp1", "string", "additionalProp2", "string", "additionalProp3", "string"))
                    .withTags(
                        mapOf("additionalProp1", "string", "additionalProp2", "string", "additionalProp3", "string")))
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

### EnvironmentContainers_Delete

```java
import com.azure.core.util.Context;

/** Samples for EnvironmentContainers Delete. */
public final class EnvironmentContainersDeleteSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/EnvironmentContainer/delete.json
     */
    /**
     * Sample code: Delete Environment Container.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void deleteEnvironmentContainer(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.environmentContainers().deleteWithResponse("testrg123", "testworkspace", "testContainer", Context.NONE);
    }
}
```

### EnvironmentContainers_Get

```java
import com.azure.core.util.Context;

/** Samples for EnvironmentContainers Get. */
public final class EnvironmentContainersGetSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/EnvironmentContainer/get.json
     */
    /**
     * Sample code: Get Environment Container.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void getEnvironmentContainer(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.environmentContainers().getWithResponse("testrg123", "testworkspace", "testEnvironment", Context.NONE);
    }
}
```

### EnvironmentContainers_List

```java
import com.azure.core.util.Context;

/** Samples for EnvironmentContainers List. */
public final class EnvironmentContainersListSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/EnvironmentContainer/list.json
     */
    /**
     * Sample code: List Environment Container.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void listEnvironmentContainer(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.environmentContainers().list("testrg123", "testworkspace", null, null, Context.NONE);
    }
}
```

### EnvironmentVersions_CreateOrUpdate

```java
import com.azure.resourcemanager.machinelearning.models.BuildContext;
import com.azure.resourcemanager.machinelearning.models.EnvironmentVersionProperties;
import com.azure.resourcemanager.machinelearning.models.InferenceContainerProperties;
import com.azure.resourcemanager.machinelearning.models.Route;
import java.util.HashMap;
import java.util.Map;

/** Samples for EnvironmentVersions CreateOrUpdate. */
public final class EnvironmentVersionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/EnvironmentVersion/createOrUpdate.json
     */
    /**
     * Sample code: CreateOrUpdate Environment Version.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void createOrUpdateEnvironmentVersion(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .environmentVersions()
            .define("string")
            .withExistingEnvironment("test-rg", "my-aml-workspace", "string")
            .withProperties(
                new EnvironmentVersionProperties()
                    .withDescription("string")
                    .withProperties(mapOf("string", "string"))
                    .withTags(mapOf("string", "string"))
                    .withIsAnonymous(false)
                    .withBuild(
                        new BuildContext()
                            .withContextUri(
                                "https://storage-account.blob.core.windows.net/azureml/DockerBuildContext/95ddede6b9b8c4e90472db3acd0a8d28/")
                            .withDockerfilePath("prod/Dockerfile"))
                    .withCondaFile("string")
                    .withImage("docker.io/tensorflow/serving:latest")
                    .withInferenceConfig(
                        new InferenceContainerProperties()
                            .withLivenessRoute(new Route().withPath("string").withPort(1))
                            .withReadinessRoute(new Route().withPath("string").withPort(1))
                            .withScoringRoute(new Route().withPath("string").withPort(1))))
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

### EnvironmentVersions_Delete

```java
import com.azure.core.util.Context;

/** Samples for EnvironmentVersions Delete. */
public final class EnvironmentVersionsDeleteSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/EnvironmentVersion/delete.json
     */
    /**
     * Sample code: Delete Environment Version.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void deleteEnvironmentVersion(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .environmentVersions()
            .deleteWithResponse("test-rg", "my-aml-workspace", "string", "string", Context.NONE);
    }
}
```

### EnvironmentVersions_Get

```java
import com.azure.core.util.Context;

/** Samples for EnvironmentVersions Get. */
public final class EnvironmentVersionsGetSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/EnvironmentVersion/get.json
     */
    /**
     * Sample code: Get Environment Version.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void getEnvironmentVersion(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.environmentVersions().getWithResponse("test-rg", "my-aml-workspace", "string", "string", Context.NONE);
    }
}
```

### EnvironmentVersions_List

```java
import com.azure.core.util.Context;

/** Samples for EnvironmentVersions List. */
public final class EnvironmentVersionsListSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/EnvironmentVersion/list.json
     */
    /**
     * Sample code: List Environment Version.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void listEnvironmentVersion(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .environmentVersions()
            .list("test-rg", "my-aml-workspace", "string", "string", 1, null, null, Context.NONE);
    }
}
```

### Jobs_Cancel

```java
import com.azure.core.util.Context;

/** Samples for Jobs Cancel. */
public final class JobsCancelSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Job/cancel.json
     */
    /**
     * Sample code: Cancel Job.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void cancelJob(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.jobs().cancel("test-rg", "my-aml-workspace", "string", Context.NONE);
    }
}
```

### Jobs_CreateOrUpdate

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.machinelearning.models.AmlToken;
import com.azure.resourcemanager.machinelearning.models.AutoMLJob;
import com.azure.resourcemanager.machinelearning.models.CommandJob;
import com.azure.resourcemanager.machinelearning.models.CommandJobLimits;
import com.azure.resourcemanager.machinelearning.models.Goal;
import com.azure.resourcemanager.machinelearning.models.GridSamplingAlgorithm;
import com.azure.resourcemanager.machinelearning.models.ImageClassification;
import com.azure.resourcemanager.machinelearning.models.ImageLimitSettings;
import com.azure.resourcemanager.machinelearning.models.ImageModelDistributionSettingsClassification;
import com.azure.resourcemanager.machinelearning.models.ImageModelSettingsClassification;
import com.azure.resourcemanager.machinelearning.models.JobResourceConfiguration;
import com.azure.resourcemanager.machinelearning.models.JobService;
import com.azure.resourcemanager.machinelearning.models.LiteralJobInput;
import com.azure.resourcemanager.machinelearning.models.MLTableJobInput;
import com.azure.resourcemanager.machinelearning.models.MedianStoppingPolicy;
import com.azure.resourcemanager.machinelearning.models.Mpi;
import com.azure.resourcemanager.machinelearning.models.Objective;
import com.azure.resourcemanager.machinelearning.models.OutputDeliveryMode;
import com.azure.resourcemanager.machinelearning.models.PipelineJob;
import com.azure.resourcemanager.machinelearning.models.SweepJob;
import com.azure.resourcemanager.machinelearning.models.SweepJobLimits;
import com.azure.resourcemanager.machinelearning.models.TensorFlow;
import com.azure.resourcemanager.machinelearning.models.TrialComponent;
import com.azure.resourcemanager.machinelearning.models.UriFileJobOutput;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Jobs CreateOrUpdate. */
public final class JobsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Job/AutoMLJob/createOrUpdate.json
     */
    /**
     * Sample code: CreateOrUpdate AutoML Job.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void createOrUpdateAutoMLJob(com.azure.resourcemanager.machinelearning.MachineLearningManager manager)
        throws IOException {
        manager
            .jobs()
            .define("string")
            .withExistingWorkspace("test-rg", "my-aml-workspace")
            .withProperties(
                new AutoMLJob()
                    .withDescription("string")
                    .withProperties(mapOf("string", "string"))
                    .withTags(mapOf("string", "string"))
                    .withComputeId("string")
                    .withDisplayName("string")
                    .withExperimentName("string")
                    .withIdentity(new AmlToken())
                    .withIsArchived(false)
                    .withServices(
                        mapOf(
                            "string",
                            new JobService()
                                .withEndpoint("string")
                                .withJobServiceType("string")
                                .withPort(1)
                                .withProperties(mapOf("string", "string"))))
                    .withEnvironmentId("string")
                    .withEnvironmentVariables(mapOf("string", "string"))
                    .withOutputs(
                        mapOf(
                            "string",
                            new UriFileJobOutput()
                                .withDescription("string")
                                .withMode(OutputDeliveryMode.READ_WRITE_MOUNT)
                                .withUri("string")))
                    .withResources(
                        new JobResourceConfiguration()
                            .withInstanceCount(1)
                            .withInstanceType("string")
                            .withProperties(
                                mapOf(
                                    "string",
                                    SerializerFactory
                                        .createDefaultManagementSerializerAdapter()
                                        .deserialize(
                                            "{\"9bec0ab0-c62f-4fa9-a97c-7b24bbcc90ad\":null}",
                                            Object.class,
                                            SerializerEncoding.JSON))))
                    .withTaskDetails(
                        new ImageClassification()
                            .withTargetColumnName("string")
                            .withTrainingData(new MLTableJobInput().withUri("string"))
                            .withModelSettings(new ImageModelSettingsClassification().withValidationCropSize(2))
                            .withSearchSpace(
                                Arrays
                                    .asList(
                                        new ImageModelDistributionSettingsClassification()
                                            .withValidationCropSize("choice(2, 360)")))
                            .withLimitSettings(new ImageLimitSettings().withMaxTrials(2))))
            .create();
    }

    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Job/SweepJob/createOrUpdate.json
     */
    /**
     * Sample code: CreateOrUpdate Sweep Job.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void createOrUpdateSweepJob(com.azure.resourcemanager.machinelearning.MachineLearningManager manager)
        throws IOException {
        manager
            .jobs()
            .define("string")
            .withExistingWorkspace("test-rg", "my-aml-workspace")
            .withProperties(
                new SweepJob()
                    .withDescription("string")
                    .withProperties(mapOf("string", "string"))
                    .withTags(mapOf("string", "string"))
                    .withComputeId("string")
                    .withDisplayName("string")
                    .withExperimentName("string")
                    .withServices(
                        mapOf(
                            "string",
                            new JobService()
                                .withEndpoint("string")
                                .withJobServiceType("string")
                                .withPort(1)
                                .withProperties(mapOf("string", "string"))))
                    .withEarlyTermination(new MedianStoppingPolicy().withDelayEvaluation(1).withEvaluationInterval(1))
                    .withLimits(
                        new SweepJobLimits()
                            .withMaxConcurrentTrials(1)
                            .withMaxTotalTrials(1)
                            .withTrialTimeout(Duration.parse("PT1S")))
                    .withObjective(new Objective().withGoal(Goal.MINIMIZE).withPrimaryMetric("string"))
                    .withSamplingAlgorithm(new GridSamplingAlgorithm())
                    .withSearchSpace(
                        SerializerFactory
                            .createDefaultManagementSerializerAdapter()
                            .deserialize("{\"string\":{}}", Object.class, SerializerEncoding.JSON))
                    .withTrial(
                        new TrialComponent()
                            .withCodeId("string")
                            .withCommand("string")
                            .withDistribution(new Mpi().withProcessCountPerInstance(1))
                            .withEnvironmentId("string")
                            .withEnvironmentVariables(mapOf("string", "string"))
                            .withResources(
                                new JobResourceConfiguration()
                                    .withInstanceCount(1)
                                    .withInstanceType("string")
                                    .withProperties(
                                        mapOf(
                                            "string",
                                            SerializerFactory
                                                .createDefaultManagementSerializerAdapter()
                                                .deserialize(
                                                    "{\"e6b6493e-7d5e-4db3-be1e-306ec641327e\":null}",
                                                    Object.class,
                                                    SerializerEncoding.JSON))))))
            .create();
    }

    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Job/PipelineJob/createOrUpdate.json
     */
    /**
     * Sample code: CreateOrUpdate Pipeline Job.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void createOrUpdatePipelineJob(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) throws IOException {
        manager
            .jobs()
            .define("string")
            .withExistingWorkspace("test-rg", "my-aml-workspace")
            .withProperties(
                new PipelineJob()
                    .withDescription("string")
                    .withProperties(mapOf("string", "string"))
                    .withTags(mapOf("string", "string"))
                    .withComputeId("string")
                    .withDisplayName("string")
                    .withExperimentName("string")
                    .withServices(
                        mapOf(
                            "string",
                            new JobService()
                                .withEndpoint("string")
                                .withJobServiceType("string")
                                .withPort(1)
                                .withProperties(mapOf("string", "string"))))
                    .withInputs(mapOf("string", new LiteralJobInput().withDescription("string").withValue("string")))
                    .withOutputs(
                        mapOf(
                            "string",
                            new UriFileJobOutput()
                                .withDescription("string")
                                .withMode(OutputDeliveryMode.UPLOAD)
                                .withUri("string")))
                    .withSettings(
                        SerializerFactory
                            .createDefaultManagementSerializerAdapter()
                            .deserialize("{}", Object.class, SerializerEncoding.JSON)))
            .create();
    }

    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Job/CommandJob/createOrUpdate.json
     */
    /**
     * Sample code: CreateOrUpdate Command Job.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void createOrUpdateCommandJob(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) throws IOException {
        manager
            .jobs()
            .define("string")
            .withExistingWorkspace("test-rg", "my-aml-workspace")
            .withProperties(
                new CommandJob()
                    .withDescription("string")
                    .withProperties(mapOf("string", "string"))
                    .withTags(mapOf("string", "string"))
                    .withComputeId("string")
                    .withDisplayName("string")
                    .withExperimentName("string")
                    .withIdentity(new AmlToken())
                    .withServices(
                        mapOf(
                            "string",
                            new JobService()
                                .withEndpoint("string")
                                .withJobServiceType("string")
                                .withPort(1)
                                .withProperties(mapOf("string", "string"))))
                    .withCodeId("string")
                    .withCommand("string")
                    .withDistribution(new TensorFlow().withParameterServerCount(1).withWorkerCount(1))
                    .withEnvironmentId("string")
                    .withEnvironmentVariables(mapOf("string", "string"))
                    .withInputs(mapOf("string", new LiteralJobInput().withDescription("string").withValue("string")))
                    .withLimits(new CommandJobLimits().withTimeout(Duration.parse("PT5M")))
                    .withOutputs(
                        mapOf(
                            "string",
                            new UriFileJobOutput()
                                .withDescription("string")
                                .withMode(OutputDeliveryMode.READ_WRITE_MOUNT)
                                .withUri("string")))
                    .withResources(
                        new JobResourceConfiguration()
                            .withInstanceCount(1)
                            .withInstanceType("string")
                            .withProperties(
                                mapOf(
                                    "string",
                                    SerializerFactory
                                        .createDefaultManagementSerializerAdapter()
                                        .deserialize(
                                            "{\"e6b6493e-7d5e-4db3-be1e-306ec641327e\":null}",
                                            Object.class,
                                            SerializerEncoding.JSON)))))
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

### Jobs_Delete

```java
import com.azure.core.util.Context;

/** Samples for Jobs Delete. */
public final class JobsDeleteSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Job/delete.json
     */
    /**
     * Sample code: Delete Job.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void deleteJob(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.jobs().delete("test-rg", "my-aml-workspace", "string", Context.NONE);
    }
}
```

### Jobs_Get

```java
import com.azure.core.util.Context;

/** Samples for Jobs Get. */
public final class JobsGetSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Job/CommandJob/get.json
     */
    /**
     * Sample code: Get Command Job.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void getCommandJob(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.jobs().getWithResponse("test-rg", "my-aml-workspace", "string", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Job/AutoMLJob/get.json
     */
    /**
     * Sample code: Get AutoML Job.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void getAutoMLJob(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.jobs().getWithResponse("test-rg", "my-aml-workspace", "string", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Job/SweepJob/get.json
     */
    /**
     * Sample code: Get Sweep Job.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void getSweepJob(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.jobs().getWithResponse("test-rg", "my-aml-workspace", "string", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Job/PipelineJob/get.json
     */
    /**
     * Sample code: Get Pipeline Job.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void getPipelineJob(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.jobs().getWithResponse("test-rg", "my-aml-workspace", "string", Context.NONE);
    }
}
```

### Jobs_List

```java
import com.azure.core.util.Context;

/** Samples for Jobs List. */
public final class JobsListSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Job/CommandJob/list.json
     */
    /**
     * Sample code: List Command Job.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void listCommandJob(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.jobs().list("test-rg", "my-aml-workspace", null, "string", "string", null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Job/PipelineJob/list.json
     */
    /**
     * Sample code: List Pipeline Job.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void listPipelineJob(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.jobs().list("test-rg", "my-aml-workspace", null, "string", "string", null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Job/SweepJob/list.json
     */
    /**
     * Sample code: List Sweep Job.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void listSweepJob(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.jobs().list("test-rg", "my-aml-workspace", null, "string", "string", null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Job/AutoMLJob/list.json
     */
    /**
     * Sample code: List AutoML Job.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void listAutoMLJob(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.jobs().list("test-rg", "my-aml-workspace", null, null, null, null, Context.NONE);
    }
}
```

### ModelContainers_CreateOrUpdate

```java
import com.azure.resourcemanager.machinelearning.models.ModelContainerProperties;
import java.util.HashMap;
import java.util.Map;

/** Samples for ModelContainers CreateOrUpdate. */
public final class ModelContainersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/ModelContainer/createOrUpdate.json
     */
    /**
     * Sample code: CreateOrUpdate Model Container.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void createOrUpdateModelContainer(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .modelContainers()
            .define("testContainer")
            .withExistingWorkspace("testrg123", "workspace123")
            .withProperties(
                new ModelContainerProperties()
                    .withDescription("Model container description")
                    .withTags(mapOf("tag1", "value1", "tag2", "value2")))
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

### ModelContainers_Delete

```java
import com.azure.core.util.Context;

/** Samples for ModelContainers Delete. */
public final class ModelContainersDeleteSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/ModelContainer/delete.json
     */
    /**
     * Sample code: Delete Model Container.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void deleteModelContainer(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.modelContainers().deleteWithResponse("testrg123", "workspace123", "testContainer", Context.NONE);
    }
}
```

### ModelContainers_Get

```java
import com.azure.core.util.Context;

/** Samples for ModelContainers Get. */
public final class ModelContainersGetSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/ModelContainer/get.json
     */
    /**
     * Sample code: Get Model Container.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void getModelContainer(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.modelContainers().getWithResponse("testrg123", "workspace123", "testContainer", Context.NONE);
    }
}
```

### ModelContainers_List

```java
import com.azure.core.util.Context;

/** Samples for ModelContainers List. */
public final class ModelContainersListSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/ModelContainer/list.json
     */
    /**
     * Sample code: List Model Container.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void listModelContainer(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.modelContainers().list("testrg123", "workspace123", null, null, null, Context.NONE);
    }
}
```

### ModelVersions_CreateOrUpdate

```java
import com.azure.resourcemanager.machinelearning.models.FlavorData;
import com.azure.resourcemanager.machinelearning.models.ModelVersionProperties;
import java.util.HashMap;
import java.util.Map;

/** Samples for ModelVersions CreateOrUpdate. */
public final class ModelVersionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/ModelVersion/createOrUpdate.json
     */
    /**
     * Sample code: CreateOrUpdate Model Version.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void createOrUpdateModelVersion(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .modelVersions()
            .define("string")
            .withExistingModel("test-rg", "my-aml-workspace", "string")
            .withProperties(
                new ModelVersionProperties()
                    .withDescription("string")
                    .withProperties(mapOf("string", "string"))
                    .withTags(mapOf("string", "string"))
                    .withIsAnonymous(false)
                    .withFlavors(mapOf("string", new FlavorData().withData(mapOf("string", "string"))))
                    .withModelType("CustomModel")
                    .withModelUri("string"))
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

### ModelVersions_Delete

```java
import com.azure.core.util.Context;

/** Samples for ModelVersions Delete. */
public final class ModelVersionsDeleteSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/ModelVersion/delete.json
     */
    /**
     * Sample code: Delete Model Version.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void deleteModelVersion(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.modelVersions().deleteWithResponse("test-rg", "my-aml-workspace", "string", "string", Context.NONE);
    }
}
```

### ModelVersions_Get

```java
import com.azure.core.util.Context;

/** Samples for ModelVersions Get. */
public final class ModelVersionsGetSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/ModelVersion/get.json
     */
    /**
     * Sample code: Get Model Version.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void getModelVersion(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.modelVersions().getWithResponse("test-rg", "my-aml-workspace", "string", "string", Context.NONE);
    }
}
```

### ModelVersions_List

```java
import com.azure.core.util.Context;

/** Samples for ModelVersions List. */
public final class ModelVersionsListSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/ModelVersion/list.json
     */
    /**
     * Sample code: List Model Version.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void listModelVersion(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .modelVersions()
            .list(
                "test-rg",
                "my-aml-workspace",
                "string",
                null,
                "string",
                1,
                "string",
                "string",
                1,
                "string",
                "string",
                null,
                null,
                Context.NONE);
    }
}
```

### OnlineDeployments_CreateOrUpdate

```java
import com.azure.resourcemanager.machinelearning.models.CodeConfiguration;
import com.azure.resourcemanager.machinelearning.models.ContainerResourceRequirements;
import com.azure.resourcemanager.machinelearning.models.ContainerResourceSettings;
import com.azure.resourcemanager.machinelearning.models.DefaultScaleSettings;
import com.azure.resourcemanager.machinelearning.models.KubernetesOnlineDeployment;
import com.azure.resourcemanager.machinelearning.models.ManagedOnlineDeployment;
import com.azure.resourcemanager.machinelearning.models.ManagedServiceIdentity;
import com.azure.resourcemanager.machinelearning.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.machinelearning.models.OnlineRequestSettings;
import com.azure.resourcemanager.machinelearning.models.ProbeSettings;
import com.azure.resourcemanager.machinelearning.models.Sku;
import com.azure.resourcemanager.machinelearning.models.SkuTier;
import com.azure.resourcemanager.machinelearning.models.UserAssignedIdentity;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/** Samples for OnlineDeployments CreateOrUpdate. */
public final class OnlineDeploymentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/OnlineDeployment/KubernetesOnlineDeployment/createOrUpdate.json
     */
    /**
     * Sample code: CreateOrUpdate Kubernetes Online Deployment.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void createOrUpdateKubernetesOnlineDeployment(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .onlineDeployments()
            .define("testDeploymentName")
            .withRegion("string")
            .withExistingOnlineEndpoint("test-rg", "my-aml-workspace", "testEndpointName")
            .withProperties(
                new KubernetesOnlineDeployment()
                    .withCodeConfiguration(new CodeConfiguration().withCodeId("string").withScoringScript("string"))
                    .withDescription("string")
                    .withEnvironmentId("string")
                    .withEnvironmentVariables(mapOf("string", "string"))
                    .withProperties(mapOf("string", "string"))
                    .withAppInsightsEnabled(false)
                    .withInstanceType("string")
                    .withLivenessProbe(
                        new ProbeSettings()
                            .withFailureThreshold(1)
                            .withInitialDelay(Duration.parse("PT5M"))
                            .withPeriod(Duration.parse("PT5M"))
                            .withSuccessThreshold(1)
                            .withTimeout(Duration.parse("PT5M")))
                    .withModel("string")
                    .withModelMountPath("string")
                    .withRequestSettings(
                        new OnlineRequestSettings()
                            .withMaxConcurrentRequestsPerInstance(1)
                            .withMaxQueueWait(Duration.parse("PT5M"))
                            .withRequestTimeout(Duration.parse("PT5M")))
                    .withScaleSettings(new DefaultScaleSettings())
                    .withContainerResourceRequirements(
                        new ContainerResourceRequirements()
                            .withContainerResourceLimits(
                                new ContainerResourceSettings().withCpu("\"1\"").withGpu("\"1\"").withMemory("\"2Gi\""))
                            .withContainerResourceRequests(
                                new ContainerResourceSettings()
                                    .withCpu("\"1\"")
                                    .withGpu("\"1\"")
                                    .withMemory("\"2Gi\""))))
            .withTags(mapOf())
            .withIdentity(
                new ManagedServiceIdentity()
                    .withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED)
                    .withUserAssignedIdentities(mapOf("string", new UserAssignedIdentity())))
            .withKind("string")
            .withSku(
                new Sku()
                    .withName("string")
                    .withTier(SkuTier.FREE)
                    .withSize("string")
                    .withFamily("string")
                    .withCapacity(1))
            .create();
    }

    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/OnlineDeployment/ManagedOnlineDeployment/createOrUpdate.json
     */
    /**
     * Sample code: CreateOrUpdate Managed Online Deployment.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void createOrUpdateManagedOnlineDeployment(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .onlineDeployments()
            .define("testDeploymentName")
            .withRegion("string")
            .withExistingOnlineEndpoint("test-rg", "my-aml-workspace", "testEndpointName")
            .withProperties(
                new ManagedOnlineDeployment()
                    .withCodeConfiguration(new CodeConfiguration().withCodeId("string").withScoringScript("string"))
                    .withDescription("string")
                    .withEnvironmentId("string")
                    .withEnvironmentVariables(mapOf("string", "string"))
                    .withProperties(mapOf("string", "string"))
                    .withAppInsightsEnabled(false)
                    .withInstanceType("string")
                    .withLivenessProbe(
                        new ProbeSettings()
                            .withFailureThreshold(1)
                            .withInitialDelay(Duration.parse("PT5M"))
                            .withPeriod(Duration.parse("PT5M"))
                            .withSuccessThreshold(1)
                            .withTimeout(Duration.parse("PT5M")))
                    .withModel("string")
                    .withModelMountPath("string")
                    .withReadinessProbe(
                        new ProbeSettings()
                            .withFailureThreshold(30)
                            .withInitialDelay(Duration.parse("PT1S"))
                            .withPeriod(Duration.parse("PT10S"))
                            .withSuccessThreshold(1)
                            .withTimeout(Duration.parse("PT2S")))
                    .withRequestSettings(
                        new OnlineRequestSettings()
                            .withMaxConcurrentRequestsPerInstance(1)
                            .withMaxQueueWait(Duration.parse("PT5M"))
                            .withRequestTimeout(Duration.parse("PT5M")))
                    .withScaleSettings(new DefaultScaleSettings()))
            .withTags(mapOf())
            .withIdentity(
                new ManagedServiceIdentity()
                    .withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED)
                    .withUserAssignedIdentities(mapOf("string", new UserAssignedIdentity())))
            .withKind("string")
            .withSku(
                new Sku()
                    .withName("string")
                    .withTier(SkuTier.FREE)
                    .withSize("string")
                    .withFamily("string")
                    .withCapacity(1))
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

### OnlineDeployments_Delete

```java
import com.azure.core.util.Context;

/** Samples for OnlineDeployments Delete. */
public final class OnlineDeploymentsDeleteSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/OnlineDeployment/delete.json
     */
    /**
     * Sample code: Delete Online Deployment.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void deleteOnlineDeployment(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.onlineDeployments().delete("testrg123", "workspace123", "testEndpoint", "testDeployment", Context.NONE);
    }
}
```

### OnlineDeployments_Get

```java
import com.azure.core.util.Context;

/** Samples for OnlineDeployments Get. */
public final class OnlineDeploymentsGetSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/OnlineDeployment/KubernetesOnlineDeployment/get.json
     */
    /**
     * Sample code: Get Kubernetes Online Deployment.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void getKubernetesOnlineDeployment(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .onlineDeployments()
            .getWithResponse("test-rg", "my-aml-workspace", "testEndpointName", "testDeploymentName", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/OnlineDeployment/ManagedOnlineDeployment/get.json
     */
    /**
     * Sample code: Get Managed Online Deployment.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void getManagedOnlineDeployment(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .onlineDeployments()
            .getWithResponse("test-rg", "my-aml-workspace", "testEndpointName", "testDeploymentName", Context.NONE);
    }
}
```

### OnlineDeployments_GetLogs

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.machinelearning.models.ContainerType;
import com.azure.resourcemanager.machinelearning.models.DeploymentLogsRequest;

/** Samples for OnlineDeployments GetLogs. */
public final class OnlineDeploymentsGetLogsSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/OnlineDeployment/getLogs.json
     */
    /**
     * Sample code: Get Online Deployment Logs.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void getOnlineDeploymentLogs(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .onlineDeployments()
            .getLogsWithResponse(
                "testrg123",
                "workspace123",
                "testEndpoint",
                "testDeployment",
                new DeploymentLogsRequest().withContainerType(ContainerType.STORAGE_INITIALIZER).withTail(0),
                Context.NONE);
    }
}
```

### OnlineDeployments_List

```java
import com.azure.core.util.Context;

/** Samples for OnlineDeployments List. */
public final class OnlineDeploymentsListSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/OnlineDeployment/list.json
     */
    /**
     * Sample code: List Online Deployments.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void listOnlineDeployments(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .onlineDeployments()
            .list("test-rg", "my-aml-workspace", "testEndpointName", "string", 1, null, Context.NONE);
    }
}
```

### OnlineDeployments_ListSkus

```java
import com.azure.core.util.Context;

/** Samples for OnlineDeployments ListSkus. */
public final class OnlineDeploymentsListSkusSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/OnlineDeployment/ManagedOnlineDeployment/listSkus.json
     */
    /**
     * Sample code: List Managed Online Deployment Skus.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void listManagedOnlineDeploymentSkus(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .onlineDeployments()
            .listSkus("test-rg", "my-aml-workspace", "testEndpointName", "testDeploymentName", 1, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/OnlineDeployment/KubernetesOnlineDeployment/listSkus.json
     */
    /**
     * Sample code: List Kubernetes Online Deployment Skus.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void listKubernetesOnlineDeploymentSkus(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .onlineDeployments()
            .listSkus("test-rg", "my-aml-workspace", "testEndpointName", "testDeploymentName", 1, null, Context.NONE);
    }
}
```

### OnlineDeployments_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.machinelearning.models.OnlineDeployment;
import com.azure.resourcemanager.machinelearning.models.PartialSku;
import com.azure.resourcemanager.machinelearning.models.SkuTier;
import java.util.HashMap;
import java.util.Map;

/** Samples for OnlineDeployments Update. */
public final class OnlineDeploymentsUpdateSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/OnlineDeployment/ManagedOnlineDeployment/update.json
     */
    /**
     * Sample code: Update Managed Online Deployment.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void updateManagedOnlineDeployment(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        OnlineDeployment resource =
            manager
                .onlineDeployments()
                .getWithResponse("test-rg", "my-aml-workspace", "testEndpointName", "testDeploymentName", Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf())
            .withSku(
                new PartialSku()
                    .withCapacity(1)
                    .withFamily("string")
                    .withName("string")
                    .withSize("string")
                    .withTier(SkuTier.FREE))
            .apply();
    }

    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/OnlineDeployment/KubernetesOnlineDeployment/update.json
     */
    /**
     * Sample code: Update Kubernetes Online Deployment.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void updateKubernetesOnlineDeployment(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        OnlineDeployment resource =
            manager
                .onlineDeployments()
                .getWithResponse("test-rg", "my-aml-workspace", "testEndpointName", "testDeploymentName", Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf())
            .withSku(
                new PartialSku()
                    .withCapacity(1)
                    .withFamily("string")
                    .withName("string")
                    .withSize("string")
                    .withTier(SkuTier.FREE))
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

### OnlineEndpoints_CreateOrUpdate

```java
import com.azure.resourcemanager.machinelearning.models.EndpointAuthMode;
import com.azure.resourcemanager.machinelearning.models.ManagedServiceIdentity;
import com.azure.resourcemanager.machinelearning.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.machinelearning.models.OnlineEndpointProperties;
import com.azure.resourcemanager.machinelearning.models.Sku;
import com.azure.resourcemanager.machinelearning.models.SkuTier;
import com.azure.resourcemanager.machinelearning.models.UserAssignedIdentity;
import java.util.HashMap;
import java.util.Map;

/** Samples for OnlineEndpoints CreateOrUpdate. */
public final class OnlineEndpointsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/OnlineEndpoint/createOrUpdate.json
     */
    /**
     * Sample code: CreateOrUpdate Online Endpoint.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void createOrUpdateOnlineEndpoint(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .onlineEndpoints()
            .define("testEndpointName")
            .withRegion("string")
            .withExistingWorkspace("test-rg", "my-aml-workspace")
            .withProperties(
                new OnlineEndpointProperties()
                    .withAuthMode(EndpointAuthMode.AMLTOKEN)
                    .withDescription("string")
                    .withProperties(mapOf("string", "string"))
                    .withCompute("string")
                    .withTraffic(mapOf("string", 1)))
            .withTags(mapOf())
            .withIdentity(
                new ManagedServiceIdentity()
                    .withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED)
                    .withUserAssignedIdentities(mapOf("string", new UserAssignedIdentity())))
            .withKind("string")
            .withSku(
                new Sku()
                    .withName("string")
                    .withTier(SkuTier.FREE)
                    .withSize("string")
                    .withFamily("string")
                    .withCapacity(1))
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

### OnlineEndpoints_Delete

```java
import com.azure.core.util.Context;

/** Samples for OnlineEndpoints Delete. */
public final class OnlineEndpointsDeleteSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/OnlineEndpoint/delete.json
     */
    /**
     * Sample code: Delete Online Endpoint.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void deleteOnlineEndpoint(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.onlineEndpoints().delete("test-rg", "my-aml-workspace", "testEndpointName", Context.NONE);
    }
}
```

### OnlineEndpoints_Get

```java
import com.azure.core.util.Context;

/** Samples for OnlineEndpoints Get. */
public final class OnlineEndpointsGetSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/OnlineEndpoint/get.json
     */
    /**
     * Sample code: Get Online Endpoint.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void getOnlineEndpoint(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.onlineEndpoints().getWithResponse("test-rg", "my-aml-workspace", "testEndpointName", Context.NONE);
    }
}
```

### OnlineEndpoints_GetToken

```java
import com.azure.core.util.Context;

/** Samples for OnlineEndpoints GetToken. */
public final class OnlineEndpointsGetTokenSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/OnlineEndpoint/getToken.json
     */
    /**
     * Sample code: GetToken Online Endpoint.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void getTokenOnlineEndpoint(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.onlineEndpoints().getTokenWithResponse("test-rg", "my-aml-workspace", "testEndpointName", Context.NONE);
    }
}
```

### OnlineEndpoints_List

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.machinelearning.models.EndpointComputeType;
import com.azure.resourcemanager.machinelearning.models.OrderString;

/** Samples for OnlineEndpoints List. */
public final class OnlineEndpointsListSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/OnlineEndpoint/list.json
     */
    /**
     * Sample code: List Online Endpoint.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void listOnlineEndpoint(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .onlineEndpoints()
            .list(
                "test-rg",
                "my-aml-workspace",
                "string",
                1,
                EndpointComputeType.MANAGED,
                null,
                "string",
                "string",
                OrderString.CREATED_AT_DESC,
                Context.NONE);
    }
}
```

### OnlineEndpoints_ListKeys

```java
import com.azure.core.util.Context;

/** Samples for OnlineEndpoints ListKeys. */
public final class OnlineEndpointsListKeysSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/OnlineEndpoint/listKeys.json
     */
    /**
     * Sample code: ListKeys Online Endpoint.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void listKeysOnlineEndpoint(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.onlineEndpoints().listKeysWithResponse("test-rg", "my-aml-workspace", "testEndpointName", Context.NONE);
    }
}
```

### OnlineEndpoints_RegenerateKeys

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.machinelearning.models.KeyType;
import com.azure.resourcemanager.machinelearning.models.RegenerateEndpointKeysRequest;

/** Samples for OnlineEndpoints RegenerateKeys. */
public final class OnlineEndpointsRegenerateKeysSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/OnlineEndpoint/regenerateKeys.json
     */
    /**
     * Sample code: RegenerateKeys Online Endpoint.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void regenerateKeysOnlineEndpoint(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .onlineEndpoints()
            .regenerateKeys(
                "test-rg",
                "my-aml-workspace",
                "testEndpointName",
                new RegenerateEndpointKeysRequest().withKeyType(KeyType.PRIMARY).withKeyValue("string"),
                Context.NONE);
    }
}
```

### OnlineEndpoints_Update

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.machinelearning.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.machinelearning.models.OnlineEndpoint;
import com.azure.resourcemanager.machinelearning.models.PartialManagedServiceIdentity;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** Samples for OnlineEndpoints Update. */
public final class OnlineEndpointsUpdateSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/OnlineEndpoint/update.json
     */
    /**
     * Sample code: Update Online Endpoint.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void updateOnlineEndpoint(com.azure.resourcemanager.machinelearning.MachineLearningManager manager)
        throws IOException {
        OnlineEndpoint resource =
            manager
                .onlineEndpoints()
                .getWithResponse("test-rg", "my-aml-workspace", "testEndpointName", Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf())
            .withIdentity(
                new PartialManagedServiceIdentity()
                    .withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "string",
                            SerializerFactory
                                .createDefaultManagementSerializerAdapter()
                                .deserialize("{}", Object.class, SerializerEncoding.JSON))))
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

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Workspace/operationsList.json
     */
    /**
     * Sample code: OperationsList.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void operationsList(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### PrivateEndpointConnections_CreateOrUpdate

```java
import com.azure.resourcemanager.machinelearning.models.PrivateEndpointServiceConnectionStatus;
import com.azure.resourcemanager.machinelearning.models.PrivateLinkServiceConnectionState;

/** Samples for PrivateEndpointConnections CreateOrUpdate. */
public final class PrivateEndpointConnectionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/PrivateEndpointConnection/createOrUpdate.json
     */
    /**
     * Sample code: WorkspacePutPrivateEndpointConnection.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void workspacePutPrivateEndpointConnection(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .privateEndpointConnections()
            .define("{privateEndpointConnectionName}")
            .withExistingWorkspace("rg-1234", "testworkspace")
            .withPrivateLinkServiceConnectionState(
                new PrivateLinkServiceConnectionState()
                    .withStatus(PrivateEndpointServiceConnectionStatus.APPROVED)
                    .withDescription("Auto-Approved"))
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
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/PrivateEndpointConnection/delete.json
     */
    /**
     * Sample code: WorkspaceDeletePrivateEndpointConnection.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void workspaceDeletePrivateEndpointConnection(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .privateEndpointConnections()
            .deleteWithResponse("rg-1234", "testworkspace", "{privateEndpointConnectionName}", Context.NONE);
    }
}
```

### PrivateEndpointConnections_Get

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections Get. */
public final class PrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/PrivateEndpointConnection/get.json
     */
    /**
     * Sample code: WorkspaceGetPrivateEndpointConnection.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void workspaceGetPrivateEndpointConnection(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .privateEndpointConnections()
            .getWithResponse("rg-1234", "testworkspace", "{privateEndpointConnectionName}", Context.NONE);
    }
}
```

### PrivateEndpointConnections_List

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections List. */
public final class PrivateEndpointConnectionsListSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/PrivateEndpointConnection/list.json
     */
    /**
     * Sample code: StorageAccountListPrivateEndpointConnections.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void storageAccountListPrivateEndpointConnections(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.privateEndpointConnections().list("rg-1234", "testworkspace", Context.NONE);
    }
}
```

### PrivateLinkResources_List

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkResources List. */
public final class PrivateLinkResourcesListSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/PrivateLinkResource/list.json
     */
    /**
     * Sample code: WorkspaceListPrivateLinkResources.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void workspaceListPrivateLinkResources(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.privateLinkResources().listWithResponse("rg-1234", "testworkspace", Context.NONE);
    }
}
```

### Quotas_List

```java
import com.azure.core.util.Context;

/** Samples for Quotas List. */
public final class QuotasListSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Quota/list.json
     */
    /**
     * Sample code: List workspace quotas by VMFamily.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void listWorkspaceQuotasByVMFamily(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.quotas().list("eastus", Context.NONE);
    }
}
```

### Quotas_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.machinelearning.models.QuotaBaseProperties;
import com.azure.resourcemanager.machinelearning.models.QuotaUnit;
import com.azure.resourcemanager.machinelearning.models.QuotaUpdateParameters;
import java.util.Arrays;

/** Samples for Quotas Update. */
public final class QuotasUpdateSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Quota/update.json
     */
    /**
     * Sample code: update quotas.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void updateQuotas(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .quotas()
            .updateWithResponse(
                "eastus",
                new QuotaUpdateParameters()
                    .withValue(
                        Arrays
                            .asList(
                                new QuotaBaseProperties()
                                    .withId(
                                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg/providers/Microsoft.MachineLearningServices/workspaces/demo_workspace1/quotas/Standard_DSv2_Family_Cluster_Dedicated_vCPUs")
                                    .withType("Microsoft.MachineLearningServices/workspaces/quotas")
                                    .withLimit(100L)
                                    .withUnit(QuotaUnit.COUNT),
                                new QuotaBaseProperties()
                                    .withId(
                                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg/providers/Microsoft.MachineLearningServices/workspaces/demo_workspace2/quotas/Standard_DSv2_Family_Cluster_Dedicated_vCPUs")
                                    .withType("Microsoft.MachineLearningServices/workspaces/quotas")
                                    .withLimit(200L)
                                    .withUnit(QuotaUnit.COUNT))),
                Context.NONE);
    }
}
```

### Schedules_CreateOrUpdate

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.machinelearning.models.CronTrigger;
import com.azure.resourcemanager.machinelearning.models.EndpointScheduleAction;
import com.azure.resourcemanager.machinelearning.models.ScheduleProperties;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** Samples for Schedules CreateOrUpdate. */
public final class SchedulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Schedule/createOrUpdate.json
     */
    /**
     * Sample code: CreateOrUpdate Schedule.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void createOrUpdateSchedule(com.azure.resourcemanager.machinelearning.MachineLearningManager manager)
        throws IOException {
        manager
            .schedules()
            .define("string")
            .withExistingWorkspace("test-rg", "my-aml-workspace")
            .withProperties(
                new ScheduleProperties()
                    .withDescription("string")
                    .withProperties(mapOf("string", "string"))
                    .withTags(mapOf("string", "string"))
                    .withAction(
                        new EndpointScheduleAction()
                            .withEndpointInvocationDefinition(
                                SerializerFactory
                                    .createDefaultManagementSerializerAdapter()
                                    .deserialize(
                                        "{\"9965593e-526f-4b89-bb36-761138cf2794\":null}",
                                        Object.class,
                                        SerializerEncoding.JSON)))
                    .withDisplayName("string")
                    .withIsEnabled(false)
                    .withTrigger(
                        new CronTrigger()
                            .withEndTime("string")
                            .withStartTime("string")
                            .withTimeZone("string")
                            .withExpression("string")))
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

### Schedules_Delete

```java
import com.azure.core.util.Context;

/** Samples for Schedules Delete. */
public final class SchedulesDeleteSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Schedule/delete.json
     */
    /**
     * Sample code: Delete Schedule.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void deleteSchedule(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.schedules().delete("test-rg", "my-aml-workspace", "string", Context.NONE);
    }
}
```

### Schedules_Get

```java
import com.azure.core.util.Context;

/** Samples for Schedules Get. */
public final class SchedulesGetSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Schedule/get.json
     */
    /**
     * Sample code: Get Schedule.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void getSchedule(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.schedules().getWithResponse("test-rg", "my-aml-workspace", "string", Context.NONE);
    }
}
```

### Schedules_List

```java
import com.azure.core.util.Context;

/** Samples for Schedules List. */
public final class SchedulesListSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Schedule/list.json
     */
    /**
     * Sample code: List Schedules.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void listSchedules(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.schedules().list("test-rg", "my-aml-workspace", null, null, Context.NONE);
    }
}
```

### Usages_List

```java
import com.azure.core.util.Context;

/** Samples for Usages List. */
public final class UsagesListSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Usage/list.json
     */
    /**
     * Sample code: List Usages.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void listUsages(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.usages().list("eastus", Context.NONE);
    }
}
```

### VirtualMachineSizes_List

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachineSizes List. */
public final class VirtualMachineSizesListSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/VirtualMachineSize/list.json
     */
    /**
     * Sample code: List VM Sizes.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void listVMSizes(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.virtualMachineSizes().listWithResponse("eastus", Context.NONE);
    }
}
```

### WorkspaceConnections_Create

```java
import com.azure.resourcemanager.machinelearning.models.ConnectionCategory;
import com.azure.resourcemanager.machinelearning.models.NoneAuthTypeWorkspaceConnectionProperties;

/** Samples for WorkspaceConnections Create. */
public final class WorkspaceConnectionsCreateSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/WorkspaceConnection/create.json
     */
    /**
     * Sample code: CreateWorkspaceConnection.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void createWorkspaceConnection(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .workspaceConnections()
            .define("connection-1")
            .withExistingWorkspace("resourceGroup-1", "workspace-1")
            .withProperties(
                new NoneAuthTypeWorkspaceConnectionProperties()
                    .withCategory(ConnectionCategory.CONTAINER_REGISTRY)
                    .withTarget("www.facebook.com"))
            .create();
    }
}
```

### WorkspaceConnections_Delete

```java
import com.azure.core.util.Context;

/** Samples for WorkspaceConnections Delete. */
public final class WorkspaceConnectionsDeleteSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/WorkspaceConnection/delete.json
     */
    /**
     * Sample code: DeleteWorkspaceConnection.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void deleteWorkspaceConnection(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .workspaceConnections()
            .deleteWithResponse("resourceGroup-1", "workspace-1", "connection-1", Context.NONE);
    }
}
```

### WorkspaceConnections_Get

```java
import com.azure.core.util.Context;

/** Samples for WorkspaceConnections Get. */
public final class WorkspaceConnectionsGetSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/WorkspaceConnection/get.json
     */
    /**
     * Sample code: GetWorkspaceConnection.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void getWorkspaceConnection(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.workspaceConnections().getWithResponse("resourceGroup-1", "workspace-1", "connection-1", Context.NONE);
    }
}
```

### WorkspaceConnections_List

```java
import com.azure.core.util.Context;

/** Samples for WorkspaceConnections List. */
public final class WorkspaceConnectionsListSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/WorkspaceConnection/list.json
     */
    /**
     * Sample code: ListWorkspaceConnections.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void listWorkspaceConnections(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .workspaceConnections()
            .list("resourceGroup-1", "workspace-1", "www.facebook.com", "ContainerRegistry", Context.NONE);
    }
}
```

### WorkspaceFeatures_List

```java
import com.azure.core.util.Context;

/** Samples for WorkspaceFeatures List. */
public final class WorkspaceFeaturesListSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/WorkspaceFeature/list.json
     */
    /**
     * Sample code: List Workspace features.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void listWorkspaceFeatures(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.workspaceFeatures().list("myResourceGroup", "testworkspace", Context.NONE);
    }
}
```

### Workspaces_CreateOrUpdate

```java
import com.azure.resourcemanager.machinelearning.models.EncryptionKeyVaultProperties;
import com.azure.resourcemanager.machinelearning.models.EncryptionProperty;
import com.azure.resourcemanager.machinelearning.models.EncryptionStatus;
import com.azure.resourcemanager.machinelearning.models.IdentityForCmk;
import com.azure.resourcemanager.machinelearning.models.ManagedServiceIdentity;
import com.azure.resourcemanager.machinelearning.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.machinelearning.models.PrivateEndpointServiceConnectionStatus;
import com.azure.resourcemanager.machinelearning.models.SharedPrivateLinkResource;
import com.azure.resourcemanager.machinelearning.models.UserAssignedIdentity;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Workspaces CreateOrUpdate. */
public final class WorkspacesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Workspace/create.json
     */
    /**
     * Sample code: Create Workspace.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void createWorkspace(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .workspaces()
            .define("testworkspace")
            .withExistingResourceGroup("workspace-1234")
            .withRegion("eastus2euap")
            .withIdentity(
                new ManagedServiceIdentity()
                    .withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/workspace-1234/providers/Microsoft.ManagedIdentity/userAssignedIdentities/testuai",
                            new UserAssignedIdentity())))
            .withDescription("test description")
            .withFriendlyName("HelloName")
            .withKeyVault(
                "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/workspace-1234/providers/Microsoft.KeyVault/vaults/testkv")
            .withApplicationInsights(
                "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/workspace-1234/providers/microsoft.insights/components/testinsights")
            .withContainerRegistry(
                "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/workspace-1234/providers/Microsoft.ContainerRegistry/registries/testRegistry")
            .withStorageAccount(
                "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/accountcrud-1234/providers/Microsoft.Storage/storageAccounts/testStorageAccount")
            .withEncryption(
                new EncryptionProperty()
                    .withStatus(EncryptionStatus.ENABLED)
                    .withIdentity(
                        new IdentityForCmk()
                            .withUserAssignedIdentity(
                                "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/workspace-1234/providers/Microsoft.ManagedIdentity/userAssignedIdentities/testuai"))
                    .withKeyVaultProperties(
                        new EncryptionKeyVaultProperties()
                            .withKeyVaultArmId(
                                "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/workspace-1234/providers/Microsoft.KeyVault/vaults/testkv")
                            .withKeyIdentifier(
                                "https://testkv.vault.azure.net/keys/testkey/aabbccddee112233445566778899aabb")
                            .withIdentityClientId("")))
            .withHbiWorkspace(false)
            .withSharedPrivateLinkResources(
                Arrays
                    .asList(
                        new SharedPrivateLinkResource()
                            .withName("testdbresource")
                            .withPrivateLinkResourceId(
                                "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/workspace-1234/providers/Microsoft.DocumentDB/databaseAccounts/testdbresource/privateLinkResources/Sql")
                            .withGroupId("Sql")
                            .withRequestMessage("Please approve")
                            .withStatus(PrivateEndpointServiceConnectionStatus.APPROVED)))
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

### Workspaces_Delete

```java
import com.azure.core.util.Context;

/** Samples for Workspaces Delete. */
public final class WorkspacesDeleteSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Workspace/delete.json
     */
    /**
     * Sample code: Delete Workspace.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void deleteWorkspace(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.workspaces().delete("workspace-1234", "testworkspace", Context.NONE);
    }
}
```

### Workspaces_Diagnose

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.machinelearning.models.DiagnoseRequestProperties;
import com.azure.resourcemanager.machinelearning.models.DiagnoseWorkspaceParameters;
import java.util.HashMap;
import java.util.Map;

/** Samples for Workspaces Diagnose. */
public final class WorkspacesDiagnoseSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Workspace/diagnose.json
     */
    /**
     * Sample code: Diagnose Workspace.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void diagnoseWorkspace(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .workspaces()
            .diagnose(
                "workspace-1234",
                "testworkspace",
                new DiagnoseWorkspaceParameters()
                    .withValue(
                        new DiagnoseRequestProperties()
                            .withUdr(mapOf())
                            .withNsg(mapOf())
                            .withResourceLock(mapOf())
                            .withDnsResolution(mapOf())
                            .withStorageAccount(mapOf())
                            .withKeyVault(mapOf())
                            .withContainerRegistry(mapOf())
                            .withApplicationInsights(mapOf())
                            .withOthers(mapOf())),
                Context.NONE);
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

### Workspaces_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Workspaces GetByResourceGroup. */
public final class WorkspacesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Workspace/get.json
     */
    /**
     * Sample code: Get Workspace.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void getWorkspace(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.workspaces().getByResourceGroupWithResponse("workspace-1234", "testworkspace", Context.NONE);
    }
}
```

### Workspaces_List

```java
import com.azure.core.util.Context;

/** Samples for Workspaces List. */
public final class WorkspacesListSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Workspace/listBySubscription.json
     */
    /**
     * Sample code: Get Workspaces by subscription.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void getWorkspacesBySubscription(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.workspaces().list(null, Context.NONE);
    }
}
```

### Workspaces_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Workspaces ListByResourceGroup. */
public final class WorkspacesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Workspace/listByResourceGroup.json
     */
    /**
     * Sample code: Get Workspaces by Resource Group.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void getWorkspacesByResourceGroup(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.workspaces().listByResourceGroup("workspace-1234", null, Context.NONE);
    }
}
```

### Workspaces_ListKeys

```java
import com.azure.core.util.Context;

/** Samples for Workspaces ListKeys. */
public final class WorkspacesListKeysSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Workspace/listKeys.json
     */
    /**
     * Sample code: List Workspace Keys.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void listWorkspaceKeys(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.workspaces().listKeysWithResponse("testrg123", "workspaces123", Context.NONE);
    }
}
```

### Workspaces_ListNotebookAccessToken

```java
import com.azure.core.util.Context;

/** Samples for Workspaces ListNotebookAccessToken. */
public final class WorkspacesListNotebookAccessTokenSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Workspace/listNotebookAccessToken.json
     */
    /**
     * Sample code: List Workspace Keys.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void listWorkspaceKeys(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.workspaces().listNotebookAccessTokenWithResponse("workspace-1234", "testworkspace", Context.NONE);
    }
}
```

### Workspaces_ListNotebookKeys

```java
import com.azure.core.util.Context;

/** Samples for Workspaces ListNotebookKeys. */
public final class WorkspacesListNotebookKeysSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Notebook/listKeys.json
     */
    /**
     * Sample code: List Workspace Keys.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void listWorkspaceKeys(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.workspaces().listNotebookKeysWithResponse("testrg123", "workspaces123", Context.NONE);
    }
}
```

### Workspaces_ListOutboundNetworkDependenciesEndpoints

```java
import com.azure.core.util.Context;

/** Samples for Workspaces ListOutboundNetworkDependenciesEndpoints. */
public final class WorkspacesListOutboundNetworkDependenciesEndpointsSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/ExternalFQDN/get.json
     */
    /**
     * Sample code: ListOutboundNetworkDependenciesEndpoints.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void listOutboundNetworkDependenciesEndpoints(
        com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager
            .workspaces()
            .listOutboundNetworkDependenciesEndpointsWithResponse("workspace-1234", "testworkspace", Context.NONE);
    }
}
```

### Workspaces_ListStorageAccountKeys

```java
import com.azure.core.util.Context;

/** Samples for Workspaces ListStorageAccountKeys. */
public final class WorkspacesListStorageAccountKeysSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Workspace/listStorageAccountKeys.json
     */
    /**
     * Sample code: List Workspace Keys.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void listWorkspaceKeys(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.workspaces().listStorageAccountKeysWithResponse("testrg123", "workspaces123", Context.NONE);
    }
}
```

### Workspaces_PrepareNotebook

```java
import com.azure.core.util.Context;

/** Samples for Workspaces PrepareNotebook. */
public final class WorkspacesPrepareNotebookSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Notebook/prepare.json
     */
    /**
     * Sample code: Prepare Notebook.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void prepareNotebook(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.workspaces().prepareNotebook("testrg123", "workspaces123", Context.NONE);
    }
}
```

### Workspaces_ResyncKeys

```java
import com.azure.core.util.Context;

/** Samples for Workspaces ResyncKeys. */
public final class WorkspacesResyncKeysSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Workspace/resyncKeys.json
     */
    /**
     * Sample code: Resync Workspace Keys.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void resyncWorkspaceKeys(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.workspaces().resyncKeys("testrg123", "workspaces123", Context.NONE);
    }
}
```

### Workspaces_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.machinelearning.models.PublicNetworkAccess;
import com.azure.resourcemanager.machinelearning.models.Workspace;

/** Samples for Workspaces Update. */
public final class WorkspacesUpdateSamples {
    /*
     * x-ms-original-file: specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2022-10-01/examples/Workspace/update.json
     */
    /**
     * Sample code: Update Workspace.
     *
     * @param manager Entry point to MachineLearningManager.
     */
    public static void updateWorkspace(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        Workspace resource =
            manager
                .workspaces()
                .getByResourceGroupWithResponse("workspace-1234", "testworkspace", Context.NONE)
                .getValue();
        resource
            .update()
            .withDescription("new description")
            .withFriendlyName("New friendly name")
            .withPublicNetworkAccess(PublicNetworkAccess.DISABLED)
            .apply();
    }
}
```

