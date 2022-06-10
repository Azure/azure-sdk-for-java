# Code snippets and samples


## Clusters

- [CreateOrUpdate](#clusters_createorupdate)
- [Delete](#clusters_delete)
- [GetByResourceGroup](#clusters_getbyresourcegroup)
- [List](#clusters_list)
- [ListByResourceGroup](#clusters_listbyresourcegroup)
- [ListStreamingJobs](#clusters_liststreamingjobs)
- [Update](#clusters_update)

## Functions

- [CreateOrReplace](#functions_createorreplace)
- [Delete](#functions_delete)
- [Get](#functions_get)
- [ListByStreamingJob](#functions_listbystreamingjob)
- [RetrieveDefaultDefinition](#functions_retrievedefaultdefinition)
- [Test](#functions_test)
- [Update](#functions_update)

## Inputs

- [CreateOrReplace](#inputs_createorreplace)
- [Delete](#inputs_delete)
- [Get](#inputs_get)
- [ListByStreamingJob](#inputs_listbystreamingjob)
- [Test](#inputs_test)
- [Update](#inputs_update)

## Operations

- [List](#operations_list)

## Outputs

- [CreateOrReplace](#outputs_createorreplace)
- [Delete](#outputs_delete)
- [Get](#outputs_get)
- [ListByStreamingJob](#outputs_listbystreamingjob)
- [Test](#outputs_test)
- [Update](#outputs_update)

## PrivateEndpoints

- [CreateOrUpdate](#privateendpoints_createorupdate)
- [Delete](#privateendpoints_delete)
- [Get](#privateendpoints_get)
- [ListByCluster](#privateendpoints_listbycluster)

## StreamingJobs

- [CreateOrReplace](#streamingjobs_createorreplace)
- [Delete](#streamingjobs_delete)
- [GetByResourceGroup](#streamingjobs_getbyresourcegroup)
- [List](#streamingjobs_list)
- [ListByResourceGroup](#streamingjobs_listbyresourcegroup)
- [Scale](#streamingjobs_scale)
- [Start](#streamingjobs_start)
- [Stop](#streamingjobs_stop)
- [Update](#streamingjobs_update)

## Subscriptions

- [ListQuotas](#subscriptions_listquotas)

## Transformations

- [CreateOrReplace](#transformations_createorreplace)
- [Get](#transformations_get)
- [Update](#transformations_update)
### Clusters_CreateOrUpdate

```java
import com.azure.resourcemanager.streamanalytics.models.ClusterSku;
import com.azure.resourcemanager.streamanalytics.models.ClusterSkuName;
import java.util.HashMap;
import java.util.Map;

/** Samples for Clusters CreateOrUpdate. */
public final class ClustersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Cluster_Create.json
     */
    /**
     * Sample code: Create a new cluster.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void createANewCluster(com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager
            .clusters()
            .define("An Example Cluster")
            .withRegion("North US")
            .withExistingResourceGroup("sjrg")
            .withTags(mapOf("key", "value"))
            .withSku(new ClusterSku().withName(ClusterSkuName.DEFAULT).withCapacity(48))
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

### Clusters_Delete

```java
import com.azure.core.util.Context;

/** Samples for Clusters Delete. */
public final class ClustersDeleteSamples {
    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Cluster_Delete.json
     */
    /**
     * Sample code: Delete a cluster.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void deleteACluster(com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager.clusters().delete("sjrg", "testcluster", Context.NONE);
    }
}
```

### Clusters_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Clusters GetByResourceGroup. */
public final class ClustersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Cluster_Get.json
     */
    /**
     * Sample code: Get a cluster.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void getACluster(com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager.clusters().getByResourceGroupWithResponse("sjrg", "testcluster", Context.NONE);
    }
}
```

### Clusters_List

```java
import com.azure.core.util.Context;

/** Samples for Clusters List. */
public final class ClustersListSamples {
    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Cluster_ListBySubscription.json
     */
    /**
     * Sample code: List the clusters in a subscription.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void listTheClustersInASubscription(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
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
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Cluster_ListByResourceGroup.json
     */
    /**
     * Sample code: List clusters in resource group.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void listClustersInResourceGroup(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager.clusters().listByResourceGroup("sjrg", Context.NONE);
    }
}
```

### Clusters_ListStreamingJobs

```java
import com.azure.core.util.Context;

/** Samples for Clusters ListStreamingJobs. */
public final class ClustersListStreamingJobsSamples {
    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Cluster_ListStreamingJobs.json
     */
    /**
     * Sample code: List all streaming jobs in cluster.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void listAllStreamingJobsInCluster(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager.clusters().listStreamingJobs("sjrg", "testcluster", Context.NONE);
    }
}
```

### Clusters_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.streamanalytics.models.Cluster;
import com.azure.resourcemanager.streamanalytics.models.ClusterSku;

/** Samples for Clusters Update. */
public final class ClustersUpdateSamples {
    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Cluster_Update.json
     */
    /**
     * Sample code: Update a cluster.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void updateACluster(com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        Cluster resource =
            manager.clusters().getByResourceGroupWithResponse("sjrg", "testcluster", Context.NONE).getValue();
        resource.update().withSku(new ClusterSku().withCapacity(96)).apply();
    }
}
```

### Functions_CreateOrReplace

```java
import com.azure.resourcemanager.streamanalytics.models.AzureMachineLearningWebServiceFunctionBinding;
import com.azure.resourcemanager.streamanalytics.models.AzureMachineLearningWebServiceInputColumn;
import com.azure.resourcemanager.streamanalytics.models.AzureMachineLearningWebServiceInputs;
import com.azure.resourcemanager.streamanalytics.models.AzureMachineLearningWebServiceOutputColumn;
import com.azure.resourcemanager.streamanalytics.models.FunctionInput;
import com.azure.resourcemanager.streamanalytics.models.FunctionOutput;
import com.azure.resourcemanager.streamanalytics.models.JavaScriptFunctionBinding;
import com.azure.resourcemanager.streamanalytics.models.ScalarFunctionProperties;
import java.util.Arrays;

/** Samples for Functions CreateOrReplace. */
public final class FunctionsCreateOrReplaceSamples {
    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Function_Create_JavaScript.json
     */
    /**
     * Sample code: Create a JavaScript function.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void createAJavaScriptFunction(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager
            .functions()
            .define("function8197")
            .withExistingStreamingjob("sjrg1637", "sj8653")
            .withProperties(
                new ScalarFunctionProperties()
                    .withInputs(Arrays.asList(new FunctionInput().withDataType("Any")))
                    .withOutput(new FunctionOutput().withDataType("Any"))
                    .withBinding(new JavaScriptFunctionBinding().withScript("function (x, y) { return x + y; }")))
            .create();
    }

    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Function_Create_AzureML.json
     */
    /**
     * Sample code: Create an Azure ML function.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void createAnAzureMLFunction(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager
            .functions()
            .define("function588")
            .withExistingStreamingjob("sjrg7", "sj9093")
            .withProperties(
                new ScalarFunctionProperties()
                    .withInputs(Arrays.asList(new FunctionInput().withDataType("nvarchar(max)")))
                    .withOutput(new FunctionOutput().withDataType("nvarchar(max)"))
                    .withBinding(
                        new AzureMachineLearningWebServiceFunctionBinding()
                            .withEndpoint("someAzureMLEndpointURL")
                            .withApiKey("someApiKey==")
                            .withInputs(
                                new AzureMachineLearningWebServiceInputs()
                                    .withName("input1")
                                    .withColumnNames(
                                        Arrays
                                            .asList(
                                                new AzureMachineLearningWebServiceInputColumn()
                                                    .withName("tweet")
                                                    .withDataType("string")
                                                    .withMapTo(0))))
                            .withOutputs(
                                Arrays
                                    .asList(
                                        new AzureMachineLearningWebServiceOutputColumn()
                                            .withName("Sentiment")
                                            .withDataType("string")))
                            .withBatchSize(1000)))
            .create();
    }
}
```

### Functions_Delete

```java
import com.azure.core.util.Context;

/** Samples for Functions Delete. */
public final class FunctionsDeleteSamples {
    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Function_Delete.json
     */
    /**
     * Sample code: Delete a function.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void deleteAFunction(com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager.functions().deleteWithResponse("sjrg1637", "sj8653", "function8197", Context.NONE);
    }
}
```

### Functions_Get

```java
import com.azure.core.util.Context;

/** Samples for Functions Get. */
public final class FunctionsGetSamples {
    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Function_Get_AzureML.json
     */
    /**
     * Sample code: Get an Azure ML function.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void getAnAzureMLFunction(com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager.functions().getWithResponse("sjrg7", "sj9093", "function588", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Function_Get_JavaScript.json
     */
    /**
     * Sample code: Get a JavaScript function.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void getAJavaScriptFunction(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager.functions().getWithResponse("sjrg1637", "sj8653", "function8197", Context.NONE);
    }
}
```

### Functions_ListByStreamingJob

```java
import com.azure.core.util.Context;

/** Samples for Functions ListByStreamingJob. */
public final class FunctionsListByStreamingJobSamples {
    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Function_ListByStreamingJob.json
     */
    /**
     * Sample code: List all functions in a streaming job.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void listAllFunctionsInAStreamingJob(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager.functions().listByStreamingJob("sjrg1637", "sj8653", null, Context.NONE);
    }
}
```

### Functions_RetrieveDefaultDefinition

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.streamanalytics.models.AzureMachineLearningWebServiceFunctionRetrieveDefaultDefinitionParameters;
import com.azure.resourcemanager.streamanalytics.models.UdfType;

/** Samples for Functions RetrieveDefaultDefinition. */
public final class FunctionsRetrieveDefaultDefinitionSamples {
    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Function_RetrieveDefaultDefinition_AzureML.json
     */
    /**
     * Sample code: Retrieve the default definition for an Azure ML function.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void retrieveTheDefaultDefinitionForAnAzureMLFunction(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager
            .functions()
            .retrieveDefaultDefinitionWithResponse(
                "sjrg7",
                "sj9093",
                "function588",
                new AzureMachineLearningWebServiceFunctionRetrieveDefaultDefinitionParameters()
                    .withExecuteEndpoint("someAzureMLExecuteEndpointUrl")
                    .withUdfType(UdfType.SCALAR),
                Context.NONE);
    }
}
```

### Functions_Test

```java
import com.azure.core.util.Context;

/** Samples for Functions Test. */
public final class FunctionsTestSamples {
    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Function_Test_AzureML.json
     */
    /**
     * Sample code: Test the connection for an Azure ML function.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void testTheConnectionForAnAzureMLFunction(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager.functions().test("sjrg7", "sj9093", "function588", null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Function_Test_JavaScript.json
     */
    /**
     * Sample code: Test the connection for a JavaScript function.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void testTheConnectionForAJavaScriptFunction(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager.functions().test("sjrg1637", "sj8653", "function8197", null, Context.NONE);
    }
}
```

### Functions_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.streamanalytics.models.AzureMachineLearningWebServiceFunctionBinding;
import com.azure.resourcemanager.streamanalytics.models.Function;
import com.azure.resourcemanager.streamanalytics.models.JavaScriptFunctionBinding;
import com.azure.resourcemanager.streamanalytics.models.ScalarFunctionProperties;

/** Samples for Functions Update. */
public final class FunctionsUpdateSamples {
    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Function_Update_JavaScript.json
     */
    /**
     * Sample code: Update a JavaScript function.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void updateAJavaScriptFunction(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        Function resource =
            manager.functions().getWithResponse("sjrg1637", "sj8653", "function8197", Context.NONE).getValue();
        resource
            .update()
            .withProperties(
                new ScalarFunctionProperties()
                    .withBinding(new JavaScriptFunctionBinding().withScript("function (a, b) { return a * b; }")))
            .apply();
    }

    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Function_Update_AzureML.json
     */
    /**
     * Sample code: Update an Azure ML function.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void updateAnAzureMLFunction(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        Function resource =
            manager.functions().getWithResponse("sjrg7", "sj9093", "function588", Context.NONE).getValue();
        resource
            .update()
            .withProperties(
                new ScalarFunctionProperties()
                    .withBinding(new AzureMachineLearningWebServiceFunctionBinding().withBatchSize(5000)))
            .apply();
    }
}
```

### Inputs_CreateOrReplace

```java
import com.azure.resourcemanager.streamanalytics.models.AvroSerialization;
import com.azure.resourcemanager.streamanalytics.models.BlobReferenceInputDataSource;
import com.azure.resourcemanager.streamanalytics.models.BlobStreamInputDataSource;
import com.azure.resourcemanager.streamanalytics.models.CsvSerialization;
import com.azure.resourcemanager.streamanalytics.models.Encoding;
import com.azure.resourcemanager.streamanalytics.models.EventHubStreamInputDataSource;
import com.azure.resourcemanager.streamanalytics.models.IoTHubStreamInputDataSource;
import com.azure.resourcemanager.streamanalytics.models.JsonSerialization;
import com.azure.resourcemanager.streamanalytics.models.ReferenceInputProperties;
import com.azure.resourcemanager.streamanalytics.models.StorageAccount;
import com.azure.resourcemanager.streamanalytics.models.StreamInputProperties;
import java.util.Arrays;

/** Samples for Inputs CreateOrReplace. */
public final class InputsCreateOrReplaceSamples {
    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Input_Create_Stream_IoTHub_Avro.json
     */
    /**
     * Sample code: Create a stream IoT Hub input with Avro serialization.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void createAStreamIoTHubInputWithAvroSerialization(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager
            .inputs()
            .define("input7970")
            .withExistingStreamingjob("sjrg3467", "sj9742")
            .withProperties(
                new StreamInputProperties()
                    .withSerialization(new AvroSerialization())
                    .withDatasource(
                        new IoTHubStreamInputDataSource()
                            .withIotHubNamespace("iothub")
                            .withSharedAccessPolicyName("owner")
                            .withSharedAccessPolicyKey("sharedAccessPolicyKey=")
                            .withConsumerGroupName("sdkconsumergroup")
                            .withEndpoint("messages/events")))
            .create();
    }

    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Input_Create_Reference_Blob_CSV.json
     */
    /**
     * Sample code: Create a reference blob input with CSV serialization.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void createAReferenceBlobInputWithCSVSerialization(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager
            .inputs()
            .define("input7225")
            .withExistingStreamingjob("sjrg8440", "sj9597")
            .withProperties(
                new ReferenceInputProperties()
                    .withSerialization(new CsvSerialization().withFieldDelimiter(",").withEncoding(Encoding.UTF8))
                    .withDatasource(new BlobReferenceInputDataSource()))
            .create();
    }

    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Input_Create_Stream_EventHub_JSON.json
     */
    /**
     * Sample code: Create a stream Event Hub input with JSON serialization.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void createAStreamEventHubInputWithJSONSerialization(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager
            .inputs()
            .define("input7425")
            .withExistingStreamingjob("sjrg3139", "sj197")
            .withProperties(
                new StreamInputProperties()
                    .withSerialization(new JsonSerialization().withEncoding(Encoding.UTF8))
                    .withDatasource(
                        new EventHubStreamInputDataSource()
                            .withConsumerGroupName("sdkconsumergroup")
                            .withEventHubName("sdkeventhub")
                            .withServiceBusNamespace("sdktest")
                            .withSharedAccessPolicyName("RootManageSharedAccessKey")
                            .withSharedAccessPolicyKey("someSharedAccessPolicyKey==")))
            .create();
    }

    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Input_Create_Stream_Blob_CSV.json
     */
    /**
     * Sample code: Create a stream blob input with CSV serialization.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void createAStreamBlobInputWithCSVSerialization(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager
            .inputs()
            .define("input8899")
            .withExistingStreamingjob("sjrg8161", "sj6695")
            .withProperties(
                new StreamInputProperties()
                    .withSerialization(new CsvSerialization().withFieldDelimiter(",").withEncoding(Encoding.UTF8))
                    .withDatasource(
                        new BlobStreamInputDataSource()
                            .withSourcePartitionCount(16)
                            .withStorageAccounts(
                                Arrays
                                    .asList(
                                        new StorageAccount()
                                            .withAccountName("someAccountName")
                                            .withAccountKey("someAccountKey==")))
                            .withContainer("state")
                            .withPathPattern("{date}/{time}")
                            .withDateFormat("yyyy/MM/dd")
                            .withTimeFormat("HH")))
            .create();
    }
}
```

### Inputs_Delete

```java
import com.azure.core.util.Context;

/** Samples for Inputs Delete. */
public final class InputsDeleteSamples {
    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Input_Delete.json
     */
    /**
     * Sample code: Delete an input.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void deleteAnInput(com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager.inputs().deleteWithResponse("sjrg8440", "sj9597", "input7225", Context.NONE);
    }
}
```

### Inputs_Get

```java
import com.azure.core.util.Context;

/** Samples for Inputs Get. */
public final class InputsGetSamples {
    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Input_Get_Reference_Blob_CSV.json
     */
    /**
     * Sample code: Get a reference blob input with CSV serialization.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void getAReferenceBlobInputWithCSVSerialization(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager.inputs().getWithResponse("sjrg8440", "sj9597", "input7225", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Input_Get_Stream_EventHub_JSON.json
     */
    /**
     * Sample code: Get a stream Event Hub input with JSON serialization.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void getAStreamEventHubInputWithJSONSerialization(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager.inputs().getWithResponse("sjrg3139", "sj197", "input7425", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Input_Get_Stream_IoTHub_Avro.json
     */
    /**
     * Sample code: Get a stream IoT Hub input with Avro serialization.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void getAStreamIoTHubInputWithAvroSerialization(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager.inputs().getWithResponse("sjrg3467", "sj9742", "input7970", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Input_Get_Stream_Blob_CSV.json
     */
    /**
     * Sample code: Get a stream blob input with CSV serialization.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void getAStreamBlobInputWithCSVSerialization(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager.inputs().getWithResponse("sjrg8161", "sj6695", "input8899", Context.NONE);
    }
}
```

### Inputs_ListByStreamingJob

```java
import com.azure.core.util.Context;

/** Samples for Inputs ListByStreamingJob. */
public final class InputsListByStreamingJobSamples {
    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Input_ListByStreamingJob.json
     */
    /**
     * Sample code: List all inputs in a streaming job.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void listAllInputsInAStreamingJob(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager.inputs().listByStreamingJob("sjrg8440", "sj9597", null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Input_ListByStreamingJob_Diagnostics.json
     */
    /**
     * Sample code: List all inputs in a streaming job and include diagnostic information using the $select OData query
     * parameter.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void listAllInputsInAStreamingJobAndIncludeDiagnosticInformationUsingTheSelectODataQueryParameter(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager.inputs().listByStreamingJob("sjrg3276", "sj7804", "*", Context.NONE);
    }
}
```

### Inputs_Test

```java
import com.azure.core.util.Context;

/** Samples for Inputs Test. */
public final class InputsTestSamples {
    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Input_Test.json
     */
    /**
     * Sample code: Test the connection for an input.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void testTheConnectionForAnInput(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager.inputs().test("sjrg8440", "sj9597", "input7225", null, Context.NONE);
    }
}
```

### Inputs_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.streamanalytics.models.AvroSerialization;
import com.azure.resourcemanager.streamanalytics.models.BlobReferenceInputDataSource;
import com.azure.resourcemanager.streamanalytics.models.BlobStreamInputDataSource;
import com.azure.resourcemanager.streamanalytics.models.CsvSerialization;
import com.azure.resourcemanager.streamanalytics.models.Encoding;
import com.azure.resourcemanager.streamanalytics.models.EventHubStreamInputDataSource;
import com.azure.resourcemanager.streamanalytics.models.Input;
import com.azure.resourcemanager.streamanalytics.models.IoTHubStreamInputDataSource;
import com.azure.resourcemanager.streamanalytics.models.ReferenceInputProperties;
import com.azure.resourcemanager.streamanalytics.models.StreamInputProperties;

/** Samples for Inputs Update. */
public final class InputsUpdateSamples {
    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Input_Update_Stream_IoTHub.json
     */
    /**
     * Sample code: Update a stream IoT Hub input.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void updateAStreamIoTHubInput(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        Input resource = manager.inputs().getWithResponse("sjrg3467", "sj9742", "input7970", Context.NONE).getValue();
        resource
            .update()
            .withProperties(
                new StreamInputProperties()
                    .withSerialization(new CsvSerialization().withFieldDelimiter("|").withEncoding(Encoding.UTF8))
                    .withDatasource(
                        new IoTHubStreamInputDataSource().withEndpoint("messages/operationsMonitoringEvents")))
            .apply();
    }

    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Input_Update_Reference_Blob.json
     */
    /**
     * Sample code: Update a reference blob input.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void updateAReferenceBlobInput(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        Input resource = manager.inputs().getWithResponse("sjrg8440", "sj9597", "input7225", Context.NONE).getValue();
        resource
            .update()
            .withProperties(
                new ReferenceInputProperties()
                    .withSerialization(new CsvSerialization().withFieldDelimiter("|").withEncoding(Encoding.UTF8))
                    .withDatasource(new BlobReferenceInputDataSource()))
            .apply();
    }

    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Input_Update_Stream_EventHub.json
     */
    /**
     * Sample code: Update a stream Event Hub input.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void updateAStreamEventHubInput(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        Input resource = manager.inputs().getWithResponse("sjrg3139", "sj197", "input7425", Context.NONE).getValue();
        resource
            .update()
            .withProperties(
                new StreamInputProperties()
                    .withSerialization(new AvroSerialization())
                    .withDatasource(
                        new EventHubStreamInputDataSource().withConsumerGroupName("differentConsumerGroupName")))
            .apply();
    }

    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Input_Update_Stream_Blob.json
     */
    /**
     * Sample code: Update a stream blob input.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void updateAStreamBlobInput(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        Input resource = manager.inputs().getWithResponse("sjrg8161", "sj6695", "input8899", Context.NONE).getValue();
        resource
            .update()
            .withProperties(
                new StreamInputProperties()
                    .withSerialization(new CsvSerialization().withFieldDelimiter("|").withEncoding(Encoding.UTF8))
                    .withDatasource(new BlobStreamInputDataSource().withSourcePartitionCount(32)))
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
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Operation_List.json
     */
    /**
     * Sample code: List available operations for the Stream Analytics resource provider.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void listAvailableOperationsForTheStreamAnalyticsResourceProvider(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### Outputs_CreateOrReplace

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.streamanalytics.models.AvroSerialization;
import com.azure.resourcemanager.streamanalytics.models.AzureDataLakeStoreOutputDataSource;
import com.azure.resourcemanager.streamanalytics.models.AzureSqlDatabaseOutputDataSource;
import com.azure.resourcemanager.streamanalytics.models.AzureTableOutputDataSource;
import com.azure.resourcemanager.streamanalytics.models.BlobOutputDataSource;
import com.azure.resourcemanager.streamanalytics.models.CsvSerialization;
import com.azure.resourcemanager.streamanalytics.models.DocumentDbOutputDataSource;
import com.azure.resourcemanager.streamanalytics.models.Encoding;
import com.azure.resourcemanager.streamanalytics.models.EventHubOutputDataSource;
import com.azure.resourcemanager.streamanalytics.models.JsonOutputSerializationFormat;
import com.azure.resourcemanager.streamanalytics.models.JsonSerialization;
import com.azure.resourcemanager.streamanalytics.models.PowerBIOutputDataSource;
import com.azure.resourcemanager.streamanalytics.models.ServiceBusQueueOutputDataSource;
import com.azure.resourcemanager.streamanalytics.models.ServiceBusTopicOutputDataSource;
import com.azure.resourcemanager.streamanalytics.models.StorageAccount;
import java.io.IOException;
import java.util.Arrays;

/** Samples for Outputs CreateOrReplace. */
public final class OutputsCreateOrReplaceSamples {
    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Output_Create_Blob_CSV.json
     */
    /**
     * Sample code: Create a blob output with CSV serialization.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void createABlobOutputWithCSVSerialization(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager
            .outputs()
            .define("output1623")
            .withExistingStreamingjob("sjrg5023", "sj900")
            .withDatasource(
                new BlobOutputDataSource()
                    .withStorageAccounts(
                        Arrays
                            .asList(
                                new StorageAccount().withAccountName("someAccountName").withAccountKey("accountKey==")))
                    .withContainer("state")
                    .withPathPattern("{date}/{time}")
                    .withDateFormat("yyyy/MM/dd")
                    .withTimeFormat("HH"))
            .withSerialization(new CsvSerialization().withFieldDelimiter(",").withEncoding(Encoding.UTF8))
            .create();
    }

    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Output_Create_ServiceBusTopic_CSV.json
     */
    /**
     * Sample code: Create a Service Bus Topic output with CSV serialization.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void createAServiceBusTopicOutputWithCSVSerialization(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager
            .outputs()
            .define("output7886")
            .withExistingStreamingjob("sjrg6450", "sj7094")
            .withDatasource(
                new ServiceBusTopicOutputDataSource()
                    .withTopicName("sdktopic")
                    .withPropertyColumns(Arrays.asList("column1", "column2"))
                    .withServiceBusNamespace("sdktest")
                    .withSharedAccessPolicyName("RootManageSharedAccessKey")
                    .withSharedAccessPolicyKey("sharedAccessPolicyKey="))
            .withSerialization(new CsvSerialization().withFieldDelimiter(",").withEncoding(Encoding.UTF8))
            .create();
    }

    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Output_Create_AzureSQL.json
     */
    /**
     * Sample code: Create an Azure SQL database output.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void createAnAzureSQLDatabaseOutput(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager
            .outputs()
            .define("output1755")
            .withExistingStreamingjob("sjrg2157", "sj6458")
            .withDatasource(new AzureSqlDatabaseOutputDataSource())
            .create();
    }

    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Output_Create_PowerBI.json
     */
    /**
     * Sample code: Create a Power BI output.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void createAPowerBIOutput(com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager
            .outputs()
            .define("output3022")
            .withExistingStreamingjob("sjrg7983", "sj2331")
            .withDatasource(
                new PowerBIOutputDataSource()
                    .withDataset("someDataset")
                    .withTable("someTable")
                    .withGroupId("ac40305e-3e8d-43ac-8161-c33799f43e95")
                    .withGroupName("MyPowerBIGroup")
                    .withRefreshToken("someRefreshToken==")
                    .withTokenUserPrincipalName("bobsmith@contoso.com")
                    .withTokenUserDisplayName("Bob Smith"))
            .create();
    }

    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Output_Create_EventHub_JSON.json
     */
    /**
     * Sample code: Create an Event Hub output with JSON serialization.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void createAnEventHubOutputWithJSONSerialization(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager
            .outputs()
            .define("output5195")
            .withExistingStreamingjob("sjrg6912", "sj3310")
            .withDatasource(
                new EventHubOutputDataSource()
                    .withPartitionKey("partitionKey")
                    .withEventHubName("sdkeventhub")
                    .withServiceBusNamespace("sdktest")
                    .withSharedAccessPolicyName("RootManageSharedAccessKey")
                    .withSharedAccessPolicyKey("sharedAccessPolicyKey="))
            .withSerialization(
                new JsonSerialization().withEncoding(Encoding.UTF8).withFormat(JsonOutputSerializationFormat.ARRAY))
            .create();
    }

    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Output_Create_ServiceBusQueue_Avro.json
     */
    /**
     * Sample code: Create a Service Bus Queue output with Avro serialization.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void createAServiceBusQueueOutputWithAvroSerialization(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) throws IOException {
        manager
            .outputs()
            .define("output3456")
            .withExistingStreamingjob("sjrg3410", "sj5095")
            .withDatasource(
                new ServiceBusQueueOutputDataSource()
                    .withQueueName("sdkqueue")
                    .withPropertyColumns(Arrays.asList("column1", "column2"))
                    .withSystemPropertyColumns(
                        SerializerFactory
                            .createDefaultManagementSerializerAdapter()
                            .deserialize(
                                "{\"MessageId\":\"col3\",\"PartitionKey\":\"col4\"}",
                                Object.class,
                                SerializerEncoding.JSON))
                    .withServiceBusNamespace("sdktest")
                    .withSharedAccessPolicyName("RootManageSharedAccessKey")
                    .withSharedAccessPolicyKey("sharedAccessPolicyKey="))
            .withSerialization(new AvroSerialization())
            .create();
    }

    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Output_Create_DocumentDB.json
     */
    /**
     * Sample code: Create a DocumentDB output.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void createADocumentDBOutput(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager
            .outputs()
            .define("output3022")
            .withExistingStreamingjob("sjrg7983", "sj2331")
            .withDatasource(
                new DocumentDbOutputDataSource()
                    .withAccountId("someAccountId")
                    .withAccountKey("accountKey==")
                    .withDatabase("db01")
                    .withCollectionNamePattern("collection")
                    .withPartitionKey("key")
                    .withDocumentId("documentId"))
            .create();
    }

    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Output_Create_AzureTable.json
     */
    /**
     * Sample code: Create an Azure Table output.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void createAnAzureTableOutput(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager
            .outputs()
            .define("output958")
            .withExistingStreamingjob("sjrg5176", "sj2790")
            .withDatasource(
                new AzureTableOutputDataSource()
                    .withAccountName("someAccountName")
                    .withAccountKey("accountKey==")
                    .withTable("samples")
                    .withPartitionKey("partitionKey")
                    .withRowKey("rowKey")
                    .withColumnsToRemove(Arrays.asList("column1", "column2"))
                    .withBatchSize(25))
            .create();
    }

    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Output_Create_AzureDataLakeStore_JSON.json
     */
    /**
     * Sample code: Create an Azure Data Lake Store output with JSON serialization.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void createAnAzureDataLakeStoreOutputWithJSONSerialization(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager
            .outputs()
            .define("output5195")
            .withExistingStreamingjob("sjrg6912", "sj3310")
            .withDatasource(
                new AzureDataLakeStoreOutputDataSource()
                    .withAccountName("someaccount")
                    .withTenantId("cea4e98b-c798-49e7-8c40-4a2b3beb47dd")
                    .withFilePathPrefix("{date}/{time}")
                    .withDateFormat("yyyy/MM/dd")
                    .withTimeFormat("HH")
                    .withRefreshToken("someRefreshToken==")
                    .withTokenUserPrincipalName("bobsmith@contoso.com")
                    .withTokenUserDisplayName("Bob Smith"))
            .withSerialization(
                new JsonSerialization().withEncoding(Encoding.UTF8).withFormat(JsonOutputSerializationFormat.ARRAY))
            .create();
    }
}
```

### Outputs_Delete

```java
import com.azure.core.util.Context;

/** Samples for Outputs Delete. */
public final class OutputsDeleteSamples {
    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Output_Delete.json
     */
    /**
     * Sample code: Delete an output.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void deleteAnOutput(com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager.outputs().deleteWithResponse("sjrg2157", "sj6458", "output1755", Context.NONE);
    }
}
```

### Outputs_Get

```java
import com.azure.core.util.Context;

/** Samples for Outputs Get. */
public final class OutputsGetSamples {
    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Output_Get_DocumentDB.json
     */
    /**
     * Sample code: Get a DocumentDB output.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void getADocumentDBOutput(com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager.outputs().getWithResponse("sjrg7983", "sj2331", "output3022", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Output_Get_EventHub_JSON.json
     */
    /**
     * Sample code: Get an Event Hub output with JSON serialization.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void getAnEventHubOutputWithJSONSerialization(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager.outputs().getWithResponse("sjrg6912", "sj3310", "output5195", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Output_Get_AzureSQL.json
     */
    /**
     * Sample code: Get an Azure SQL database output.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void getAnAzureSQLDatabaseOutput(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager.outputs().getWithResponse("sjrg2157", "sj6458", "output1755", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Output_Get_ServiceBusQueue_Avro.json
     */
    /**
     * Sample code: Get a Service Bus Queue output with Avro serialization.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void getAServiceBusQueueOutputWithAvroSerialization(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager.outputs().getWithResponse("sjrg3410", "sj5095", "output3456", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Output_Get_AzureTable.json
     */
    /**
     * Sample code: Get an Azure Table output.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void getAnAzureTableOutput(com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager.outputs().getWithResponse("sjrg5176", "sj2790", "output958", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Output_Get_PowerBI.json
     */
    /**
     * Sample code: Get a Power BI output.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void getAPowerBIOutput(com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager.outputs().getWithResponse("sjrg7983", "sj2331", "output3022", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Output_Get_Blob_CSV.json
     */
    /**
     * Sample code: Get a blob output with CSV serialization.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void getABlobOutputWithCSVSerialization(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager.outputs().getWithResponse("sjrg5023", "sj900", "output1623", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Output_Get_ServiceBusTopic_CSV.json
     */
    /**
     * Sample code: Get a Service Bus Topic output with CSV serialization.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void getAServiceBusTopicOutputWithCSVSerialization(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager.outputs().getWithResponse("sjrg6450", "sj7094", "output7886", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Output_Get_AzureDataLakeStore_JSON.json
     */
    /**
     * Sample code: Get an Azure Data Lake Store output with JSON serialization.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void getAnAzureDataLakeStoreOutputWithJSONSerialization(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager.outputs().getWithResponse("sjrg6912", "sj3310", "output5195", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Output_Get_DataWarehouse.json
     */
    /**
     * Sample code: Get an Azure Data Warehouse output.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void getAnAzureDataWarehouseOutput(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager.outputs().getWithResponse("sjrg", "sjName", "output958", Context.NONE);
    }
}
```

### Outputs_ListByStreamingJob

```java
import com.azure.core.util.Context;

/** Samples for Outputs ListByStreamingJob. */
public final class OutputsListByStreamingJobSamples {
    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Output_ListByStreamingJob.json
     */
    /**
     * Sample code: List all outputs in a streaming job.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void listAllOutputsInAStreamingJob(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager.outputs().listByStreamingJob("sjrg2157", "sj6458", null, Context.NONE);
    }
}
```

### Outputs_Test

```java
import com.azure.core.util.Context;

/** Samples for Outputs Test. */
public final class OutputsTestSamples {
    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Output_Test.json
     */
    /**
     * Sample code: Test the connection for an output.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void testTheConnectionForAnOutput(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager.outputs().test("sjrg2157", "sj6458", "output1755", null, Context.NONE);
    }
}
```

### Outputs_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.streamanalytics.models.AzureDataLakeStoreOutputDataSource;
import com.azure.resourcemanager.streamanalytics.models.AzureSqlDatabaseOutputDataSource;
import com.azure.resourcemanager.streamanalytics.models.AzureTableOutputDataSource;
import com.azure.resourcemanager.streamanalytics.models.BlobOutputDataSource;
import com.azure.resourcemanager.streamanalytics.models.CsvSerialization;
import com.azure.resourcemanager.streamanalytics.models.DocumentDbOutputDataSource;
import com.azure.resourcemanager.streamanalytics.models.Encoding;
import com.azure.resourcemanager.streamanalytics.models.EventHubOutputDataSource;
import com.azure.resourcemanager.streamanalytics.models.JsonOutputSerializationFormat;
import com.azure.resourcemanager.streamanalytics.models.JsonSerialization;
import com.azure.resourcemanager.streamanalytics.models.Output;
import com.azure.resourcemanager.streamanalytics.models.PowerBIOutputDataSource;
import com.azure.resourcemanager.streamanalytics.models.ServiceBusQueueOutputDataSource;
import com.azure.resourcemanager.streamanalytics.models.ServiceBusTopicOutputDataSource;

/** Samples for Outputs Update. */
public final class OutputsUpdateSamples {
    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Output_Update_ServiceBusQueue.json
     */
    /**
     * Sample code: Update a Service Bus Queue output with Avro serialization.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void updateAServiceBusQueueOutputWithAvroSerialization(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        Output resource =
            manager.outputs().getWithResponse("sjrg3410", "sj5095", "output3456", Context.NONE).getValue();
        resource
            .update()
            .withDatasource(new ServiceBusQueueOutputDataSource().withQueueName("differentQueueName"))
            .withSerialization(
                new JsonSerialization()
                    .withEncoding(Encoding.UTF8)
                    .withFormat(JsonOutputSerializationFormat.LINE_SEPARATED))
            .apply();
    }

    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Output_Update_ServiceBusTopic.json
     */
    /**
     * Sample code: Update a Service Bus Topic output with CSV serialization.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void updateAServiceBusTopicOutputWithCSVSerialization(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        Output resource =
            manager.outputs().getWithResponse("sjrg6450", "sj7094", "output7886", Context.NONE).getValue();
        resource
            .update()
            .withDatasource(new ServiceBusTopicOutputDataSource().withTopicName("differentTopicName"))
            .withSerialization(new CsvSerialization().withFieldDelimiter("|").withEncoding(Encoding.UTF8))
            .apply();
    }

    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Output_Update_AzureDataLakeStore.json
     */
    /**
     * Sample code: Update an Azure Data Lake Store output with JSON serialization.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void updateAnAzureDataLakeStoreOutputWithJSONSerialization(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        Output resource =
            manager.outputs().getWithResponse("sjrg6912", "sj3310", "output5195", Context.NONE).getValue();
        resource
            .update()
            .withDatasource(new AzureDataLakeStoreOutputDataSource().withAccountName("differentaccount"))
            .withSerialization(
                new JsonSerialization()
                    .withEncoding(Encoding.UTF8)
                    .withFormat(JsonOutputSerializationFormat.LINE_SEPARATED))
            .apply();
    }

    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Output_Update_AzureTable.json
     */
    /**
     * Sample code: Update an Azure Table output.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void updateAnAzureTableOutput(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        Output resource = manager.outputs().getWithResponse("sjrg5176", "sj2790", "output958", Context.NONE).getValue();
        resource
            .update()
            .withDatasource(new AzureTableOutputDataSource().withPartitionKey("differentPartitionKey"))
            .apply();
    }

    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Output_Update_PowerBI.json
     */
    /**
     * Sample code: Update a Power BI output.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void updateAPowerBIOutput(com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        Output resource =
            manager.outputs().getWithResponse("sjrg7983", "sj2331", "output3022", Context.NONE).getValue();
        resource.update().withDatasource(new PowerBIOutputDataSource().withDataset("differentDataset")).apply();
    }

    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Output_Update_Blob.json
     */
    /**
     * Sample code: Update a blob output with CSV serialization.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void updateABlobOutputWithCSVSerialization(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        Output resource = manager.outputs().getWithResponse("sjrg5023", "sj900", "output1623", Context.NONE).getValue();
        resource
            .update()
            .withDatasource(new BlobOutputDataSource().withContainer("differentContainer"))
            .withSerialization(new CsvSerialization().withFieldDelimiter("|").withEncoding(Encoding.UTF8))
            .apply();
    }

    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Output_Update_AzureSQL.json
     */
    /**
     * Sample code: Update an Azure SQL database output.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void updateAnAzureSQLDatabaseOutput(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        Output resource =
            manager.outputs().getWithResponse("sjrg2157", "sj6458", "output1755", Context.NONE).getValue();
        resource.update().withDatasource(new AzureSqlDatabaseOutputDataSource()).apply();
    }

    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Output_Update_EventHub.json
     */
    /**
     * Sample code: Update an Event Hub output with JSON serialization.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void updateAnEventHubOutputWithJSONSerialization(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        Output resource =
            manager.outputs().getWithResponse("sjrg6912", "sj3310", "output5195", Context.NONE).getValue();
        resource
            .update()
            .withDatasource(new EventHubOutputDataSource().withPartitionKey("differentPartitionKey"))
            .withSerialization(
                new JsonSerialization()
                    .withEncoding(Encoding.UTF8)
                    .withFormat(JsonOutputSerializationFormat.LINE_SEPARATED))
            .apply();
    }

    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Output_Update_DocumentDB.json
     */
    /**
     * Sample code: Update a DocumentDB output.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void updateADocumentDBOutput(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        Output resource =
            manager.outputs().getWithResponse("sjrg7983", "sj2331", "output3022", Context.NONE).getValue();
        resource
            .update()
            .withDatasource(new DocumentDbOutputDataSource().withPartitionKey("differentPartitionKey"))
            .apply();
    }
}
```

### PrivateEndpoints_CreateOrUpdate

```java
import com.azure.resourcemanager.streamanalytics.models.PrivateLinkServiceConnection;
import java.util.Arrays;

/** Samples for PrivateEndpoints CreateOrUpdate. */
public final class PrivateEndpointsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/PrivateEndpoint_Create.json
     */
    /**
     * Sample code: Create a private endpoint.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void createAPrivateEndpoint(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager
            .privateEndpoints()
            .define("testpe")
            .withExistingCluster("sjrg", "testcluster")
            .withManualPrivateLinkServiceConnections(
                Arrays
                    .asList(
                        new PrivateLinkServiceConnection()
                            .withPrivateLinkServiceId(
                                "/subscriptions/subId/resourceGroups/rg1/providers/Microsoft.Network/privateLinkServices/testPls")
                            .withGroupIds(Arrays.asList("groupIdFromResource"))))
            .create();
    }
}
```

### PrivateEndpoints_Delete

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpoints Delete. */
public final class PrivateEndpointsDeleteSamples {
    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/PrivateEndpoint_Delete.json
     */
    /**
     * Sample code: Delete a private endpoint.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void deleteAPrivateEndpoint(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager.privateEndpoints().delete("sjrg", "testcluster", "testpe", Context.NONE);
    }
}
```

### PrivateEndpoints_Get

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpoints Get. */
public final class PrivateEndpointsGetSamples {
    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/PrivateEndpoint_Get.json
     */
    /**
     * Sample code: Get a private endpoint.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void getAPrivateEndpoint(com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager.privateEndpoints().getWithResponse("sjrg", "testcluster", "testpe", Context.NONE);
    }
}
```

### PrivateEndpoints_ListByCluster

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpoints ListByCluster. */
public final class PrivateEndpointsListByClusterSamples {
    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/PrivateEndpoint_ListByCluster.json
     */
    /**
     * Sample code: Get the private endpoints in a cluster.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void getThePrivateEndpointsInACluster(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager.privateEndpoints().listByCluster("sjrg", "testcluster", Context.NONE);
    }
}
```

### StreamingJobs_CreateOrReplace

```java
import com.azure.resourcemanager.streamanalytics.fluent.models.InputInner;
import com.azure.resourcemanager.streamanalytics.fluent.models.OutputInner;
import com.azure.resourcemanager.streamanalytics.fluent.models.TransformationInner;
import com.azure.resourcemanager.streamanalytics.models.AzureSqlDatabaseOutputDataSource;
import com.azure.resourcemanager.streamanalytics.models.BlobStreamInputDataSource;
import com.azure.resourcemanager.streamanalytics.models.CompatibilityLevel;
import com.azure.resourcemanager.streamanalytics.models.Encoding;
import com.azure.resourcemanager.streamanalytics.models.EventsOutOfOrderPolicy;
import com.azure.resourcemanager.streamanalytics.models.JsonSerialization;
import com.azure.resourcemanager.streamanalytics.models.OutputErrorPolicy;
import com.azure.resourcemanager.streamanalytics.models.Sku;
import com.azure.resourcemanager.streamanalytics.models.SkuName;
import com.azure.resourcemanager.streamanalytics.models.StorageAccount;
import com.azure.resourcemanager.streamanalytics.models.StreamInputProperties;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for StreamingJobs CreateOrReplace. */
public final class StreamingJobsCreateOrReplaceSamples {
    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/StreamingJob_Create_JobShell.json
     */
    /**
     * Sample code: Create a streaming job shell (a streaming job with no inputs, outputs, transformation, or
     * functions).
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void createAStreamingJobShellAStreamingJobWithNoInputsOutputsTransformationOrFunctions(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager
            .streamingJobs()
            .define("sj59")
            .withRegion("West US")
            .withExistingResourceGroup("sjrg6936")
            .withTags(mapOf("key1", "value1", "key3", "value3", "randomKey", "randomValue"))
            .withSku(new Sku().withName(SkuName.STANDARD))
            .withEventsOutOfOrderPolicy(EventsOutOfOrderPolicy.DROP)
            .withOutputErrorPolicy(OutputErrorPolicy.DROP)
            .withEventsOutOfOrderMaxDelayInSeconds(5)
            .withEventsLateArrivalMaxDelayInSeconds(16)
            .withDataLocale("en-US")
            .withCompatibilityLevel(CompatibilityLevel.ONE_ZERO)
            .withInputs(Arrays.asList())
            .withOutputs(Arrays.asList())
            .withFunctions(Arrays.asList())
            .create();
    }

    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/StreamingJob_Create_CompleteJob.json
     */
    /**
     * Sample code: Create a complete streaming job (a streaming job with a transformation, at least 1 input and at
     * least 1 output).
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void createACompleteStreamingJobAStreamingJobWithATransformationAtLeast1InputAndAtLeast1Output(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager
            .streamingJobs()
            .define("sj7804")
            .withRegion("West US")
            .withExistingResourceGroup("sjrg3276")
            .withTags(mapOf("key1", "value1", "key3", "value3", "randomKey", "randomValue"))
            .withSku(new Sku().withName(SkuName.STANDARD))
            .withEventsOutOfOrderPolicy(EventsOutOfOrderPolicy.DROP)
            .withOutputErrorPolicy(OutputErrorPolicy.DROP)
            .withEventsOutOfOrderMaxDelayInSeconds(0)
            .withEventsLateArrivalMaxDelayInSeconds(5)
            .withDataLocale("en-US")
            .withCompatibilityLevel(CompatibilityLevel.ONE_ZERO)
            .withInputs(
                Arrays
                    .asList(
                        new InputInner()
                            .withProperties(
                                new StreamInputProperties()
                                    .withSerialization(new JsonSerialization().withEncoding(Encoding.UTF8))
                                    .withDatasource(
                                        new BlobStreamInputDataSource()
                                            .withStorageAccounts(
                                                Arrays
                                                    .asList(
                                                        new StorageAccount()
                                                            .withAccountName("yourAccountName")
                                                            .withAccountKey("yourAccountKey==")))
                                            .withContainer("containerName")
                                            .withPathPattern("")))
                            .withName("inputtest")))
            .withTransformation(
                new TransformationInner()
                    .withName("transformationtest")
                    .withStreamingUnits(1)
                    .withQuery("Select Id, Name from inputtest"))
            .withOutputs(
                Arrays
                    .asList(
                        new OutputInner()
                            .withName("outputtest")
                            .withDatasource(new AzureSqlDatabaseOutputDataSource())))
            .withFunctions(Arrays.asList())
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

### StreamingJobs_Delete

```java
import com.azure.core.util.Context;

/** Samples for StreamingJobs Delete. */
public final class StreamingJobsDeleteSamples {
    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/StreamingJob_Delete.json
     */
    /**
     * Sample code: Delete a streaming job.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void deleteAStreamingJob(com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager.streamingJobs().delete("sjrg6936", "sj59", Context.NONE);
    }
}
```

### StreamingJobs_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for StreamingJobs GetByResourceGroup. */
public final class StreamingJobsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/StreamingJob_Get_NoExpand.json
     */
    /**
     * Sample code: Get a streaming job and do not use the $expand OData query parameter.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void getAStreamingJobAndDoNotUseTheExpandODataQueryParameter(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager.streamingJobs().getByResourceGroupWithResponse("sjrg6936", "sj59", null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/StreamingJob_Get_Expand.json
     */
    /**
     * Sample code: Get a streaming job and use the $expand OData query parameter to expand inputs, outputs,
     * transformation, and functions.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void
        getAStreamingJobAndUseTheExpandODataQueryParameterToExpandInputsOutputsTransformationAndFunctions(
            com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager
            .streamingJobs()
            .getByResourceGroupWithResponse(
                "sjrg3276", "sj7804", "inputs,outputs,transformation,functions", Context.NONE);
    }
}
```

### StreamingJobs_List

```java
import com.azure.core.util.Context;

/** Samples for StreamingJobs List. */
public final class StreamingJobsListSamples {
    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/StreamingJob_List_BySubscription_Expand.json
     */
    /**
     * Sample code: List all streaming jobs in a subscription and use the $expand OData query parameter to expand
     * inputs, outputs, transformation, and functions.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void
        listAllStreamingJobsInASubscriptionAndUseTheExpandODataQueryParameterToExpandInputsOutputsTransformationAndFunctions(
            com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager.streamingJobs().list("inputs,outputs,transformation,functions", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/StreamingJob_List_BySubscription_NoExpand.json
     */
    /**
     * Sample code: List all streaming jobs in a subscription and do not use the $expand OData query parameter.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void listAllStreamingJobsInASubscriptionAndDoNotUseTheExpandODataQueryParameter(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager.streamingJobs().list(null, Context.NONE);
    }
}
```

### StreamingJobs_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for StreamingJobs ListByResourceGroup. */
public final class StreamingJobsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/StreamingJob_List_ByResourceGroup_Expand.json
     */
    /**
     * Sample code: List all streaming jobs in a resource group and use the $expand OData query parameter to expand
     * inputs, outputs, transformation, and functions.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void
        listAllStreamingJobsInAResourceGroupAndUseTheExpandODataQueryParameterToExpandInputsOutputsTransformationAndFunctions(
            com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager
            .streamingJobs()
            .listByResourceGroup("sjrg3276", "inputs,outputs,transformation,functions", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/StreamingJob_List_ByResourceGroup_NoExpand.json
     */
    /**
     * Sample code: List all streaming jobs in a resource group and do not use the $expand OData query parameter.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void listAllStreamingJobsInAResourceGroupAndDoNotUseTheExpandODataQueryParameter(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager.streamingJobs().listByResourceGroup("sjrg6936", null, Context.NONE);
    }
}
```

### StreamingJobs_Scale

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.streamanalytics.models.ScaleStreamingJobParameters;

/** Samples for StreamingJobs Scale. */
public final class StreamingJobsScaleSamples {
    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/StreamingJob_Scale.json
     */
    /**
     * Sample code: Scale a streaming job.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void scaleAStreamingJob(com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager
            .streamingJobs()
            .scale("sjrg6936", "sj59", new ScaleStreamingJobParameters().withStreamingUnits(36), Context.NONE);
    }
}
```

### StreamingJobs_Start

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.streamanalytics.models.OutputStartMode;
import com.azure.resourcemanager.streamanalytics.models.StartStreamingJobParameters;
import java.time.OffsetDateTime;

/** Samples for StreamingJobs Start. */
public final class StreamingJobsStartSamples {
    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/StreamingJob_Start_JobStartTime.json
     */
    /**
     * Sample code: Start a streaming job with JobStartTime output start mode.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void startAStreamingJobWithJobStartTimeOutputStartMode(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager
            .streamingJobs()
            .start(
                "sjrg6936",
                "sj59",
                new StartStreamingJobParameters().withOutputStartMode(OutputStartMode.JOB_START_TIME),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/StreamingJob_Start_CustomTime.json
     */
    /**
     * Sample code: Start a streaming job with CustomTime output start mode.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void startAStreamingJobWithCustomTimeOutputStartMode(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager
            .streamingJobs()
            .start(
                "sjrg6936",
                "sj59",
                new StartStreamingJobParameters()
                    .withOutputStartMode(OutputStartMode.CUSTOM_TIME)
                    .withOutputStartTime(OffsetDateTime.parse("2012-12-12T12:12:12Z")),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/StreamingJob_Start_LastOutputEventTime.json
     */
    /**
     * Sample code: Start a streaming job with LastOutputEventTime output start mode.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void startAStreamingJobWithLastOutputEventTimeOutputStartMode(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager
            .streamingJobs()
            .start(
                "sjrg6936",
                "sj59",
                new StartStreamingJobParameters().withOutputStartMode(OutputStartMode.LAST_OUTPUT_EVENT_TIME),
                Context.NONE);
    }
}
```

### StreamingJobs_Stop

```java
import com.azure.core.util.Context;

/** Samples for StreamingJobs Stop. */
public final class StreamingJobsStopSamples {
    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/StreamingJob_Stop.json
     */
    /**
     * Sample code: Stop a streaming job.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void stopAStreamingJob(com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager.streamingJobs().stop("sjrg6936", "sj59", Context.NONE);
    }
}
```

### StreamingJobs_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.streamanalytics.models.StreamingJob;

/** Samples for StreamingJobs Update. */
public final class StreamingJobsUpdateSamples {
    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/StreamingJob_Update.json
     */
    /**
     * Sample code: Update a streaming job.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void updateAStreamingJob(com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        StreamingJob resource =
            manager.streamingJobs().getByResourceGroupWithResponse("sjrg6936", "sj59", null, Context.NONE).getValue();
        resource.update().withEventsOutOfOrderMaxDelayInSeconds(21).withEventsLateArrivalMaxDelayInSeconds(13).apply();
    }
}
```

### Subscriptions_ListQuotas

```java
import com.azure.core.util.Context;

/** Samples for Subscriptions ListQuotas. */
public final class SubscriptionsListQuotasSamples {
    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Subscription_ListQuotas.json
     */
    /**
     * Sample code: List subscription quota information in West US.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void listSubscriptionQuotaInformationInWestUS(
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager.subscriptions().listQuotasWithResponse("West US", Context.NONE);
    }
}
```

### Transformations_CreateOrReplace

```java
/** Samples for Transformations CreateOrReplace. */
public final class TransformationsCreateOrReplaceSamples {
    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Transformation_Create.json
     */
    /**
     * Sample code: Create a transformation.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void createATransformation(com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager
            .transformations()
            .define("transformation952")
            .withExistingStreamingjob("sjrg4423", "sj8374")
            .withStreamingUnits(6)
            .withQuery("Select Id, Name from inputtest")
            .create();
    }
}
```

### Transformations_Get

```java
import com.azure.core.util.Context;

/** Samples for Transformations Get. */
public final class TransformationsGetSamples {
    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Transformation_Get.json
     */
    /**
     * Sample code: Get a transformation.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void getATransformation(com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        manager.transformations().getWithResponse("sjrg4423", "sj8374", "transformation952", Context.NONE);
    }
}
```

### Transformations_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.streamanalytics.models.Transformation;

/** Samples for Transformations Update. */
public final class TransformationsUpdateSamples {
    /*
     * x-ms-original-file: specification/streamanalytics/resource-manager/Microsoft.StreamAnalytics/stable/2020-03-01/examples/Transformation_Update.json
     */
    /**
     * Sample code: Update a transformation.
     *
     * @param manager Entry point to StreamAnalyticsManager.
     */
    public static void updateATransformation(com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager) {
        Transformation resource =
            manager
                .transformations()
                .getWithResponse("sjrg4423", "sj8374", "transformation952", Context.NONE)
                .getValue();
        resource.update().withQuery("New query").apply();
    }
}
```

