# Code snippets and samples


## ActivityRuns

- [QueryByPipelineRun](#activityruns_querybypipelinerun)

## DataFlowDebugSession

- [AddDataFlow](#dataflowdebugsession_adddataflow)
- [Create](#dataflowdebugsession_create)
- [Delete](#dataflowdebugsession_delete)
- [ExecuteCommand](#dataflowdebugsession_executecommand)
- [QueryByFactory](#dataflowdebugsession_querybyfactory)

## DataFlows

- [CreateOrUpdate](#dataflows_createorupdate)
- [Delete](#dataflows_delete)
- [Get](#dataflows_get)
- [ListByFactory](#dataflows_listbyfactory)

## Datasets

- [CreateOrUpdate](#datasets_createorupdate)
- [Delete](#datasets_delete)
- [Get](#datasets_get)
- [ListByFactory](#datasets_listbyfactory)

## ExposureControl

- [GetFeatureValue](#exposurecontrol_getfeaturevalue)
- [GetFeatureValueByFactory](#exposurecontrol_getfeaturevaluebyfactory)
- [QueryFeatureValuesByFactory](#exposurecontrol_queryfeaturevaluesbyfactory)

## Factories

- [ConfigureFactoryRepo](#factories_configurefactoryrepo)
- [CreateOrUpdate](#factories_createorupdate)
- [Delete](#factories_delete)
- [GetByResourceGroup](#factories_getbyresourcegroup)
- [GetDataPlaneAccess](#factories_getdataplaneaccess)
- [GetGitHubAccessToken](#factories_getgithubaccesstoken)
- [List](#factories_list)
- [ListByResourceGroup](#factories_listbyresourcegroup)
- [Update](#factories_update)

## IntegrationRuntimeNodes

- [Delete](#integrationruntimenodes_delete)
- [Get](#integrationruntimenodes_get)
- [GetIpAddress](#integrationruntimenodes_getipaddress)
- [Update](#integrationruntimenodes_update)

## IntegrationRuntimeObjectMetadata

- [Get](#integrationruntimeobjectmetadata_get)
- [Refresh](#integrationruntimeobjectmetadata_refresh)

## IntegrationRuntimes

- [CreateLinkedIntegrationRuntime](#integrationruntimes_createlinkedintegrationruntime)
- [CreateOrUpdate](#integrationruntimes_createorupdate)
- [Delete](#integrationruntimes_delete)
- [Get](#integrationruntimes_get)
- [GetConnectionInfo](#integrationruntimes_getconnectioninfo)
- [GetMonitoringData](#integrationruntimes_getmonitoringdata)
- [GetStatus](#integrationruntimes_getstatus)
- [ListAuthKeys](#integrationruntimes_listauthkeys)
- [ListByFactory](#integrationruntimes_listbyfactory)
- [ListOutboundNetworkDependenciesEndpoints](#integrationruntimes_listoutboundnetworkdependenciesendpoints)
- [RegenerateAuthKey](#integrationruntimes_regenerateauthkey)
- [RemoveLinks](#integrationruntimes_removelinks)
- [Start](#integrationruntimes_start)
- [Stop](#integrationruntimes_stop)
- [SyncCredentials](#integrationruntimes_synccredentials)
- [Update](#integrationruntimes_update)
- [Upgrade](#integrationruntimes_upgrade)

## LinkedServices

- [CreateOrUpdate](#linkedservices_createorupdate)
- [Delete](#linkedservices_delete)
- [Get](#linkedservices_get)
- [ListByFactory](#linkedservices_listbyfactory)

## ManagedPrivateEndpoints

- [CreateOrUpdate](#managedprivateendpoints_createorupdate)
- [Delete](#managedprivateendpoints_delete)
- [Get](#managedprivateendpoints_get)
- [ListByFactory](#managedprivateendpoints_listbyfactory)

## ManagedVirtualNetworks

- [CreateOrUpdate](#managedvirtualnetworks_createorupdate)
- [Get](#managedvirtualnetworks_get)
- [ListByFactory](#managedvirtualnetworks_listbyfactory)

## Operations

- [List](#operations_list)

## PipelineRuns

- [Cancel](#pipelineruns_cancel)
- [Get](#pipelineruns_get)
- [QueryByFactory](#pipelineruns_querybyfactory)

## Pipelines

- [CreateOrUpdate](#pipelines_createorupdate)
- [CreateRun](#pipelines_createrun)
- [Delete](#pipelines_delete)
- [Get](#pipelines_get)
- [ListByFactory](#pipelines_listbyfactory)

## PrivateEndPointConnections

- [ListByFactory](#privateendpointconnections_listbyfactory)

## PrivateEndpointConnectionOperation

- [CreateOrUpdate](#privateendpointconnectionoperation_createorupdate)
- [Delete](#privateendpointconnectionoperation_delete)
- [Get](#privateendpointconnectionoperation_get)

## PrivateLinkResources

- [Get](#privatelinkresources_get)

## TriggerRuns

- [Cancel](#triggerruns_cancel)
- [QueryByFactory](#triggerruns_querybyfactory)
- [Rerun](#triggerruns_rerun)

## Triggers

- [CreateOrUpdate](#triggers_createorupdate)
- [Delete](#triggers_delete)
- [Get](#triggers_get)
- [GetEventSubscriptionStatus](#triggers_geteventsubscriptionstatus)
- [ListByFactory](#triggers_listbyfactory)
- [QueryByFactory](#triggers_querybyfactory)
- [Start](#triggers_start)
- [Stop](#triggers_stop)
- [SubscribeToEvents](#triggers_subscribetoevents)
- [UnsubscribeFromEvents](#triggers_unsubscribefromevents)
### ActivityRuns_QueryByPipelineRun

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datafactory.models.RunFilterParameters;
import java.time.OffsetDateTime;

/** Samples for ActivityRuns QueryByPipelineRun. */
public final class ActivityRunsQueryByPipelineRunSamples {
    /*
     * operationId: ActivityRuns_QueryByPipelineRun
     * api-version: 2018-06-01
     * x-ms-examples: ActivityRuns_QueryByPipelineRun
     */
    /**
     * Sample code: ActivityRuns_QueryByPipelineRun.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void activityRunsQueryByPipelineRun(
        com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .activityRuns()
            .queryByPipelineRunWithResponse(
                "exampleResourceGroup",
                "exampleFactoryName",
                "2f7fdb90-5df1-4b8e-ac2f-064cfa58202b",
                new RunFilterParameters()
                    .withLastUpdatedAfter(OffsetDateTime.parse("2018-06-16T00:36:44.3345758Z"))
                    .withLastUpdatedBefore(OffsetDateTime.parse("2018-06-16T00:49:48.3686473Z")),
                Context.NONE);
    }
}
```

### DataFlowDebugSession_AddDataFlow

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.datafactory.models.AzureBlobStorageLinkedService;
import com.azure.resourcemanager.datafactory.models.AzureBlobStorageLocation;
import com.azure.resourcemanager.datafactory.models.DataFlowDebugPackage;
import com.azure.resourcemanager.datafactory.models.DataFlowDebugPackageDebugSettings;
import com.azure.resourcemanager.datafactory.models.DataFlowDebugResource;
import com.azure.resourcemanager.datafactory.models.DataFlowSource;
import com.azure.resourcemanager.datafactory.models.DataFlowSourceSetting;
import com.azure.resourcemanager.datafactory.models.DatasetDebugResource;
import com.azure.resourcemanager.datafactory.models.DatasetReference;
import com.azure.resourcemanager.datafactory.models.DelimitedTextDataset;
import com.azure.resourcemanager.datafactory.models.LinkedServiceDebugResource;
import com.azure.resourcemanager.datafactory.models.LinkedServiceReference;
import com.azure.resourcemanager.datafactory.models.MappingDataFlow;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for DataFlowDebugSession AddDataFlow. */
public final class DataFlowDebugSessionAddDataFlowSamples {
    /*
     * operationId: DataFlowDebugSession_AddDataFlow
     * api-version: 2018-06-01
     * x-ms-examples: DataFlowDebugSession_AddDataFlow
     */
    /**
     * Sample code: DataFlowDebugSession_AddDataFlow.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void dataFlowDebugSessionAddDataFlow(com.azure.resourcemanager.datafactory.DataFactoryManager manager)
        throws IOException {
        manager
            .dataFlowDebugSessions()
            .addDataFlowWithResponse(
                "exampleResourceGroup",
                "exampleFactoryName",
                new DataFlowDebugPackage()
                    .withSessionId("f06ed247-9d07-49b2-b05e-2cb4a2fc871e")
                    .withDataFlow(
                        new DataFlowDebugResource()
                            .withName("dataflow1")
                            .withProperties(
                                new MappingDataFlow()
                                    .withSources(
                                        Arrays
                                            .asList(
                                                new DataFlowSource()
                                                    .withName("source1")
                                                    .withDataset(
                                                        new DatasetReference().withReferenceName("DelimitedText2"))))
                                    .withSinks(Arrays.asList())
                                    .withTransformations(Arrays.asList())
                                    .withScript(
                                        "\n\n"
                                            + "source(output(\n"
                                            + "\t\tColumn_1 as string\n"
                                            + "\t),\n"
                                            + "\tallowSchemaDrift: true,\n"
                                            + "\tvalidateSchema: false) ~> source1")))
                    .withDatasets(
                        Arrays
                            .asList(
                                new DatasetDebugResource()
                                    .withName("dataset1")
                                    .withProperties(
                                        new DelimitedTextDataset()
                                            .withSchema(
                                                SerializerFactory
                                                    .createDefaultManagementSerializerAdapter()
                                                    .deserialize(
                                                        "[{\"type\":\"String\"}]",
                                                        Object.class,
                                                        SerializerEncoding.JSON))
                                            .withLinkedServiceName(
                                                new LinkedServiceReference().withReferenceName("linkedService5"))
                                            .withAnnotations(Arrays.asList())
                                            .withLocation(
                                                new AzureBlobStorageLocation()
                                                    .withFileName("Ansiencoding.csv")
                                                    .withContainer("dataflow-sample-data"))
                                            .withColumnDelimiter(",")
                                            .withQuoteChar("\"")
                                            .withEscapeChar("\\")
                                            .withFirstRowAsHeader(true))))
                    .withLinkedServices(
                        Arrays
                            .asList(
                                new LinkedServiceDebugResource()
                                    .withName("linkedService1")
                                    .withProperties(
                                        new AzureBlobStorageLinkedService()
                                            .withAnnotations(Arrays.asList())
                                            .withConnectionString(
                                                "DefaultEndpointsProtocol=https;AccountName=<storageName>;EndpointSuffix=core.windows.net;")
                                            .withEncryptedCredential("<credential>"))))
                    .withDebugSettings(
                        new DataFlowDebugPackageDebugSettings()
                            .withSourceSettings(
                                Arrays
                                    .asList(
                                        new DataFlowSourceSetting()
                                            .withSourceName("source1")
                                            .withRowLimit(1000)
                                            .withAdditionalProperties(mapOf()),
                                        new DataFlowSourceSetting()
                                            .withSourceName("source2")
                                            .withRowLimit(222)
                                            .withAdditionalProperties(mapOf())))
                            .withParameters(mapOf("sourcePath", "Toy"))
                            .withDatasetParameters(
                                SerializerFactory
                                    .createDefaultManagementSerializerAdapter()
                                    .deserialize(
                                        "{\"Movies\":{\"path\":\"abc\"},\"Output\":{\"time\":\"def\"}}",
                                        Object.class,
                                        SerializerEncoding.JSON)))
                    .withAdditionalProperties(mapOf()),
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

### DataFlowDebugSession_Create

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datafactory.models.CreateDataFlowDebugSessionRequest;
import com.azure.resourcemanager.datafactory.models.DataFlowComputeType;
import com.azure.resourcemanager.datafactory.models.IntegrationRuntimeComputeProperties;
import com.azure.resourcemanager.datafactory.models.IntegrationRuntimeDataFlowProperties;
import com.azure.resourcemanager.datafactory.models.IntegrationRuntimeDebugResource;
import com.azure.resourcemanager.datafactory.models.ManagedIntegrationRuntime;
import java.util.HashMap;
import java.util.Map;

/** Samples for DataFlowDebugSession Create. */
public final class DataFlowDebugSessionCreateSamples {
    /*
     * operationId: DataFlowDebugSession_Create
     * api-version: 2018-06-01
     * x-ms-examples: DataFlowDebugSession_Create
     */
    /**
     * Sample code: DataFlowDebugSession_Create.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void dataFlowDebugSessionCreate(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .dataFlowDebugSessions()
            .create(
                "exampleResourceGroup",
                "exampleFactoryName",
                new CreateDataFlowDebugSessionRequest()
                    .withTimeToLive(60)
                    .withIntegrationRuntime(
                        new IntegrationRuntimeDebugResource()
                            .withName("ir1")
                            .withProperties(
                                new ManagedIntegrationRuntime()
                                    .withComputeProperties(
                                        new IntegrationRuntimeComputeProperties()
                                            .withLocation("AutoResolve")
                                            .withDataFlowProperties(
                                                new IntegrationRuntimeDataFlowProperties()
                                                    .withComputeType(DataFlowComputeType.GENERAL)
                                                    .withCoreCount(48)
                                                    .withTimeToLive(10)
                                                    .withAdditionalProperties(mapOf()))
                                            .withAdditionalProperties(mapOf())))),
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

### DataFlowDebugSession_Delete

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datafactory.models.DeleteDataFlowDebugSessionRequest;

/** Samples for DataFlowDebugSession Delete. */
public final class DataFlowDebugSessionDeleteSamples {
    /*
     * operationId: DataFlowDebugSession_Delete
     * api-version: 2018-06-01
     * x-ms-examples: DataFlowDebugSession_Delete
     */
    /**
     * Sample code: DataFlowDebugSession_Delete.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void dataFlowDebugSessionDelete(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .dataFlowDebugSessions()
            .deleteWithResponse(
                "exampleResourceGroup",
                "exampleFactoryName",
                new DeleteDataFlowDebugSessionRequest().withSessionId("91fb57e0-8292-47be-89ff-c8f2d2bb2a7e"),
                Context.NONE);
    }
}
```

### DataFlowDebugSession_ExecuteCommand

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datafactory.models.DataFlowDebugCommandPayload;
import com.azure.resourcemanager.datafactory.models.DataFlowDebugCommandRequest;
import com.azure.resourcemanager.datafactory.models.DataFlowDebugCommandType;

/** Samples for DataFlowDebugSession ExecuteCommand. */
public final class DataFlowDebugSessionExecuteCommandSamples {
    /*
     * operationId: DataFlowDebugSession_ExecuteCommand
     * api-version: 2018-06-01
     * x-ms-examples: DataFlowDebugSession_ExecuteCommand
     */
    /**
     * Sample code: DataFlowDebugSession_ExecuteCommand.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void dataFlowDebugSessionExecuteCommand(
        com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .dataFlowDebugSessions()
            .executeCommand(
                "exampleResourceGroup",
                "exampleFactoryName",
                new DataFlowDebugCommandRequest()
                    .withSessionId("f06ed247-9d07-49b2-b05e-2cb4a2fc871e")
                    .withCommand(DataFlowDebugCommandType.EXECUTE_PREVIEW_QUERY)
                    .withCommandPayload(new DataFlowDebugCommandPayload().withStreamName("source1").withRowLimits(100)),
                Context.NONE);
    }
}
```

### DataFlowDebugSession_QueryByFactory

```java
import com.azure.core.util.Context;

/** Samples for DataFlowDebugSession QueryByFactory. */
public final class DataFlowDebugSessionQueryByFactorySamples {
    /*
     * operationId: DataFlowDebugSession_QueryByFactory
     * api-version: 2018-06-01
     * x-ms-examples: DataFlowDebugSession_QueryByFactory
     */
    /**
     * Sample code: DataFlowDebugSession_QueryByFactory.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void dataFlowDebugSessionQueryByFactory(
        com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.dataFlowDebugSessions().queryByFactory("exampleResourceGroup", "exampleFactoryName", Context.NONE);
    }
}
```

### DataFlows_CreateOrUpdate

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datafactory.models.DataFlowResource;
import com.azure.resourcemanager.datafactory.models.DataFlowSink;
import com.azure.resourcemanager.datafactory.models.DataFlowSource;
import com.azure.resourcemanager.datafactory.models.DatasetReference;
import com.azure.resourcemanager.datafactory.models.MappingDataFlow;
import java.util.Arrays;

/** Samples for DataFlows CreateOrUpdate. */
public final class DataFlowsCreateOrUpdateSamples {
    /*
     * operationId: DataFlows_CreateOrUpdate
     * api-version: 2018-06-01
     * x-ms-examples: DataFlows_Create
     */
    /**
     * Sample code: DataFlows_Create.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void dataFlowsCreate(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .dataFlows()
            .define("exampleDataFlow")
            .withExistingFactory("exampleResourceGroup", "exampleFactoryName")
            .withProperties(
                new MappingDataFlow()
                    .withDescription(
                        "Sample demo data flow to convert currencies showing usage of union, derive and conditional"
                            + " split transformation.")
                    .withSources(
                        Arrays
                            .asList(
                                new DataFlowSource()
                                    .withName("USDCurrency")
                                    .withDataset(new DatasetReference().withReferenceName("CurrencyDatasetUSD")),
                                new DataFlowSource()
                                    .withName("CADSource")
                                    .withDataset(new DatasetReference().withReferenceName("CurrencyDatasetCAD"))))
                    .withSinks(
                        Arrays
                            .asList(
                                new DataFlowSink()
                                    .withName("USDSink")
                                    .withDataset(new DatasetReference().withReferenceName("USDOutput")),
                                new DataFlowSink()
                                    .withName("CADSink")
                                    .withDataset(new DatasetReference().withReferenceName("CADOutput"))))
                    .withScript(
                        "source(output(PreviousConversionRate as double,Country as string,DateTime1 as"
                            + " string,CurrentConversionRate as double),allowSchemaDrift: false,validateSchema: false)"
                            + " ~> USDCurrency\n"
                            + "source(output(PreviousConversionRate as double,Country as string,DateTime1 as"
                            + " string,CurrentConversionRate as double),allowSchemaDrift: true,validateSchema: false)"
                            + " ~> CADSource\n"
                            + "USDCurrency, CADSource union(byName: true)~> Union\n"
                            + "Union derive(NewCurrencyRate = round(CurrentConversionRate*1.25)) ~>"
                            + " NewCurrencyColumn\n"
                            + "NewCurrencyColumn split(Country == 'USD',Country == 'CAD',disjoint: false) ~>"
                            + " ConditionalSplit1@(USD, CAD)\n"
                            + "ConditionalSplit1@USD sink(saveMode:'overwrite' ) ~> USDSink\n"
                            + "ConditionalSplit1@CAD sink(saveMode:'overwrite' ) ~> CADSink"))
            .create();
    }

    /*
     * operationId: DataFlows_CreateOrUpdate
     * api-version: 2018-06-01
     * x-ms-examples: DataFlows_Update
     */
    /**
     * Sample code: DataFlows_Update.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void dataFlowsUpdate(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        DataFlowResource resource =
            manager
                .dataFlows()
                .getWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleDataFlow", null, Context.NONE)
                .getValue();
        resource
            .update()
            .withProperties(
                new MappingDataFlow()
                    .withDescription(
                        "Sample demo data flow to convert currencies showing usage of union, derive and conditional"
                            + " split transformation.")
                    .withSources(
                        Arrays
                            .asList(
                                new DataFlowSource()
                                    .withName("USDCurrency")
                                    .withDataset(new DatasetReference().withReferenceName("CurrencyDatasetUSD")),
                                new DataFlowSource()
                                    .withName("CADSource")
                                    .withDataset(new DatasetReference().withReferenceName("CurrencyDatasetCAD"))))
                    .withSinks(
                        Arrays
                            .asList(
                                new DataFlowSink()
                                    .withName("USDSink")
                                    .withDataset(new DatasetReference().withReferenceName("USDOutput")),
                                new DataFlowSink()
                                    .withName("CADSink")
                                    .withDataset(new DatasetReference().withReferenceName("CADOutput"))))
                    .withScript(
                        "source(output(PreviousConversionRate as double,Country as string,DateTime1 as"
                            + " string,CurrentConversionRate as double),allowSchemaDrift: false,validateSchema: false)"
                            + " ~> USDCurrency\n"
                            + "source(output(PreviousConversionRate as double,Country as string,DateTime1 as"
                            + " string,CurrentConversionRate as double),allowSchemaDrift: true,validateSchema: false)"
                            + " ~> CADSource\n"
                            + "USDCurrency, CADSource union(byName: true)~> Union\n"
                            + "Union derive(NewCurrencyRate = round(CurrentConversionRate*1.25)) ~>"
                            + " NewCurrencyColumn\n"
                            + "NewCurrencyColumn split(Country == 'USD',Country == 'CAD',disjoint: false) ~>"
                            + " ConditionalSplit1@(USD, CAD)\n"
                            + "ConditionalSplit1@USD sink(saveMode:'overwrite' ) ~> USDSink\n"
                            + "ConditionalSplit1@CAD sink(saveMode:'overwrite' ) ~> CADSink"))
            .apply();
    }
}
```

### DataFlows_Delete

```java
import com.azure.core.util.Context;

/** Samples for DataFlows Delete. */
public final class DataFlowsDeleteSamples {
    /*
     * operationId: DataFlows_Delete
     * api-version: 2018-06-01
     * x-ms-examples: DataFlows_Delete
     */
    /**
     * Sample code: DataFlows_Delete.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void dataFlowsDelete(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .dataFlows()
            .deleteWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleDataFlow", Context.NONE);
    }
}
```

### DataFlows_Get

```java
import com.azure.core.util.Context;

/** Samples for DataFlows Get. */
public final class DataFlowsGetSamples {
    /*
     * operationId: DataFlows_Get
     * api-version: 2018-06-01
     * x-ms-examples: DataFlows_Get
     */
    /**
     * Sample code: DataFlows_Get.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void dataFlowsGet(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .dataFlows()
            .getWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleDataFlow", null, Context.NONE);
    }
}
```

### DataFlows_ListByFactory

```java
import com.azure.core.util.Context;

/** Samples for DataFlows ListByFactory. */
public final class DataFlowsListByFactorySamples {
    /*
     * operationId: DataFlows_ListByFactory
     * api-version: 2018-06-01
     * x-ms-examples: DataFlows_ListByFactory
     */
    /**
     * Sample code: DataFlows_ListByFactory.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void dataFlowsListByFactory(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.dataFlows().listByFactory("exampleResourceGroup", "exampleFactoryName", Context.NONE);
    }
}
```

### Datasets_CreateOrUpdate

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.datafactory.models.AzureBlobDataset;
import com.azure.resourcemanager.datafactory.models.DatasetResource;
import com.azure.resourcemanager.datafactory.models.LinkedServiceReference;
import com.azure.resourcemanager.datafactory.models.ParameterSpecification;
import com.azure.resourcemanager.datafactory.models.ParameterType;
import com.azure.resourcemanager.datafactory.models.TextFormat;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** Samples for Datasets CreateOrUpdate. */
public final class DatasetsCreateOrUpdateSamples {
    /*
     * operationId: Datasets_CreateOrUpdate
     * api-version: 2018-06-01
     * x-ms-examples: Datasets_Create
     */
    /**
     * Sample code: Datasets_Create.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void datasetsCreate(com.azure.resourcemanager.datafactory.DataFactoryManager manager)
        throws IOException {
        manager
            .datasets()
            .define("exampleDataset")
            .withExistingFactory("exampleResourceGroup", "exampleFactoryName")
            .withProperties(
                new AzureBlobDataset()
                    .withLinkedServiceName(new LinkedServiceReference().withReferenceName("exampleLinkedService"))
                    .withParameters(
                        mapOf(
                            "MyFileName",
                            new ParameterSpecification().withType(ParameterType.STRING),
                            "MyFolderPath",
                            new ParameterSpecification().withType(ParameterType.STRING)))
                    .withFolderPath(
                        SerializerFactory
                            .createDefaultManagementSerializerAdapter()
                            .deserialize(
                                "{\"type\":\"Expression\",\"value\":\"@dataset().MyFolderPath\"}",
                                Object.class,
                                SerializerEncoding.JSON))
                    .withFileName(
                        SerializerFactory
                            .createDefaultManagementSerializerAdapter()
                            .deserialize(
                                "{\"type\":\"Expression\",\"value\":\"@dataset().MyFileName\"}",
                                Object.class,
                                SerializerEncoding.JSON))
                    .withFormat(new TextFormat()))
            .create();
    }

    /*
     * operationId: Datasets_CreateOrUpdate
     * api-version: 2018-06-01
     * x-ms-examples: Datasets_Update
     */
    /**
     * Sample code: Datasets_Update.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void datasetsUpdate(com.azure.resourcemanager.datafactory.DataFactoryManager manager)
        throws IOException {
        DatasetResource resource =
            manager
                .datasets()
                .getWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleDataset", null, Context.NONE)
                .getValue();
        resource
            .update()
            .withProperties(
                new AzureBlobDataset()
                    .withDescription("Example description")
                    .withLinkedServiceName(new LinkedServiceReference().withReferenceName("exampleLinkedService"))
                    .withParameters(
                        mapOf(
                            "MyFileName",
                            new ParameterSpecification().withType(ParameterType.STRING),
                            "MyFolderPath",
                            new ParameterSpecification().withType(ParameterType.STRING)))
                    .withFolderPath(
                        SerializerFactory
                            .createDefaultManagementSerializerAdapter()
                            .deserialize(
                                "{\"type\":\"Expression\",\"value\":\"@dataset().MyFolderPath\"}",
                                Object.class,
                                SerializerEncoding.JSON))
                    .withFileName(
                        SerializerFactory
                            .createDefaultManagementSerializerAdapter()
                            .deserialize(
                                "{\"type\":\"Expression\",\"value\":\"@dataset().MyFileName\"}",
                                Object.class,
                                SerializerEncoding.JSON))
                    .withFormat(new TextFormat()))
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

### Datasets_Delete

```java
import com.azure.core.util.Context;

/** Samples for Datasets Delete. */
public final class DatasetsDeleteSamples {
    /*
     * operationId: Datasets_Delete
     * api-version: 2018-06-01
     * x-ms-examples: Datasets_Delete
     */
    /**
     * Sample code: Datasets_Delete.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void datasetsDelete(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .datasets()
            .deleteWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleDataset", Context.NONE);
    }
}
```

### Datasets_Get

```java
import com.azure.core.util.Context;

/** Samples for Datasets Get. */
public final class DatasetsGetSamples {
    /*
     * operationId: Datasets_Get
     * api-version: 2018-06-01
     * x-ms-examples: Datasets_Get
     */
    /**
     * Sample code: Datasets_Get.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void datasetsGet(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .datasets()
            .getWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleDataset", null, Context.NONE);
    }
}
```

### Datasets_ListByFactory

```java
import com.azure.core.util.Context;

/** Samples for Datasets ListByFactory. */
public final class DatasetsListByFactorySamples {
    /*
     * operationId: Datasets_ListByFactory
     * api-version: 2018-06-01
     * x-ms-examples: Datasets_ListByFactory
     */
    /**
     * Sample code: Datasets_ListByFactory.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void datasetsListByFactory(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.datasets().listByFactory("exampleResourceGroup", "exampleFactoryName", Context.NONE);
    }
}
```

### ExposureControl_GetFeatureValue

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datafactory.models.ExposureControlRequest;

/** Samples for ExposureControl GetFeatureValue. */
public final class ExposureControlGetFeatureValueSamples {
    /*
     * operationId: ExposureControl_GetFeatureValue
     * api-version: 2018-06-01
     * x-ms-examples: ExposureControl_GetFeatureValue
     */
    /**
     * Sample code: ExposureControl_GetFeatureValue.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void exposureControlGetFeatureValue(
        com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .exposureControls()
            .getFeatureValueWithResponse(
                "WestEurope",
                new ExposureControlRequest()
                    .withFeatureName("ADFIntegrationRuntimeSharingRbac")
                    .withFeatureType("Feature"),
                Context.NONE);
    }
}
```

### ExposureControl_GetFeatureValueByFactory

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datafactory.models.ExposureControlRequest;

/** Samples for ExposureControl GetFeatureValueByFactory. */
public final class ExposureControlGetFeatureValueByFactorySamples {
    /*
     * operationId: ExposureControl_GetFeatureValueByFactory
     * api-version: 2018-06-01
     * x-ms-examples: ExposureControl_GetFeatureValueByFactory
     */
    /**
     * Sample code: ExposureControl_GetFeatureValueByFactory.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void exposureControlGetFeatureValueByFactory(
        com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .exposureControls()
            .getFeatureValueByFactoryWithResponse(
                "exampleResourceGroup",
                "exampleFactoryName",
                new ExposureControlRequest()
                    .withFeatureName("ADFIntegrationRuntimeSharingRbac")
                    .withFeatureType("Feature"),
                Context.NONE);
    }
}
```

### ExposureControl_QueryFeatureValuesByFactory

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datafactory.models.ExposureControlBatchRequest;
import com.azure.resourcemanager.datafactory.models.ExposureControlRequest;
import java.util.Arrays;

/** Samples for ExposureControl QueryFeatureValuesByFactory. */
public final class ExposureControlQueryFeatureValuesByFactorySamples {
    /*
     * operationId: ExposureControl_QueryFeatureValuesByFactory
     * api-version: 2018-06-01
     * x-ms-examples: ExposureControl_QueryFeatureValuesByFactory
     */
    /**
     * Sample code: ExposureControl_QueryFeatureValuesByFactory.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void exposureControlQueryFeatureValuesByFactory(
        com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .exposureControls()
            .queryFeatureValuesByFactoryWithResponse(
                "exampleResourceGroup",
                "exampleFactoryName",
                new ExposureControlBatchRequest()
                    .withExposureControlRequests(
                        Arrays
                            .asList(
                                new ExposureControlRequest()
                                    .withFeatureName("ADFIntegrationRuntimeSharingRbac")
                                    .withFeatureType("Feature"),
                                new ExposureControlRequest()
                                    .withFeatureName("ADFSampleFeature")
                                    .withFeatureType("Feature"))),
                Context.NONE);
    }
}
```

### Factories_ConfigureFactoryRepo

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datafactory.models.FactoryRepoUpdate;
import com.azure.resourcemanager.datafactory.models.FactoryVstsConfiguration;

/** Samples for Factories ConfigureFactoryRepo. */
public final class FactoriesConfigureFactoryRepoSamples {
    /*
     * operationId: Factories_ConfigureFactoryRepo
     * api-version: 2018-06-01
     * x-ms-examples: Factories_ConfigureFactoryRepo
     */
    /**
     * Sample code: Factories_ConfigureFactoryRepo.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void factoriesConfigureFactoryRepo(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .factories()
            .configureFactoryRepoWithResponse(
                "East US",
                new FactoryRepoUpdate()
                    .withFactoryResourceId(
                        "/subscriptions/12345678-1234-1234-1234-12345678abc/resourceGroups/exampleResourceGroup/providers/Microsoft.DataFactory/factories/exampleFactoryName")
                    .withRepoConfiguration(
                        new FactoryVstsConfiguration()
                            .withAccountName("ADF")
                            .withRepositoryName("repo")
                            .withCollaborationBranch("master")
                            .withRootFolder("/")
                            .withLastCommitId("")
                            .withProjectName("project")
                            .withTenantId("")),
                Context.NONE);
    }
}
```

### Factories_CreateOrUpdate

```java
/** Samples for Factories CreateOrUpdate. */
public final class FactoriesCreateOrUpdateSamples {
    /*
     * operationId: Factories_CreateOrUpdate
     * api-version: 2018-06-01
     * x-ms-examples: Factories_CreateOrUpdate
     */
    /**
     * Sample code: Factories_CreateOrUpdate.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void factoriesCreateOrUpdate(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .factories()
            .define("exampleFactoryName")
            .withRegion("East US")
            .withExistingResourceGroup("exampleResourceGroup")
            .create();
    }
}
```

### Factories_Delete

```java
import com.azure.core.util.Context;

/** Samples for Factories Delete. */
public final class FactoriesDeleteSamples {
    /*
     * operationId: Factories_Delete
     * api-version: 2018-06-01
     * x-ms-examples: Factories_Delete
     */
    /**
     * Sample code: Factories_Delete.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void factoriesDelete(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.factories().deleteWithResponse("exampleResourceGroup", "exampleFactoryName", Context.NONE);
    }
}
```

### Factories_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Factories GetByResourceGroup. */
public final class FactoriesGetByResourceGroupSamples {
    /*
     * operationId: Factories_Get
     * api-version: 2018-06-01
     * x-ms-examples: Factories_Get
     */
    /**
     * Sample code: Factories_Get.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void factoriesGet(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .factories()
            .getByResourceGroupWithResponse("exampleResourceGroup", "exampleFactoryName", null, Context.NONE);
    }
}
```

### Factories_GetDataPlaneAccess

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datafactory.models.UserAccessPolicy;

/** Samples for Factories GetDataPlaneAccess. */
public final class FactoriesGetDataPlaneAccessSamples {
    /*
     * operationId: Factories_GetDataPlaneAccess
     * api-version: 2018-06-01
     * x-ms-examples: Factories_GetDataPlaneAccess
     */
    /**
     * Sample code: Factories_GetDataPlaneAccess.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void factoriesGetDataPlaneAccess(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .factories()
            .getDataPlaneAccessWithResponse(
                "exampleResourceGroup",
                "exampleFactoryName",
                new UserAccessPolicy()
                    .withPermissions("r")
                    .withAccessResourcePath("")
                    .withProfileName("DefaultProfile")
                    .withStartTime("2018-11-10T02:46:20.2659347Z")
                    .withExpireTime("2018-11-10T09:46:20.2659347Z"),
                Context.NONE);
    }
}
```

### Factories_GetGitHubAccessToken

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datafactory.models.GitHubAccessTokenRequest;

/** Samples for Factories GetGitHubAccessToken. */
public final class FactoriesGetGitHubAccessTokenSamples {
    /*
     * operationId: Factories_GetGitHubAccessToken
     * api-version: 2018-06-01
     * x-ms-examples: Factories_GetGitHubAccessToken
     */
    /**
     * Sample code: Factories_GetGitHubAccessToken.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void factoriesGetGitHubAccessToken(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .factories()
            .getGitHubAccessTokenWithResponse(
                "exampleResourceGroup",
                "exampleFactoryName",
                new GitHubAccessTokenRequest()
                    .withGitHubAccessCode("some")
                    .withGitHubClientId("some")
                    .withGitHubAccessTokenBaseUrl("some"),
                Context.NONE);
    }
}
```

### Factories_List

```java
import com.azure.core.util.Context;

/** Samples for Factories List. */
public final class FactoriesListSamples {
    /*
     * operationId: Factories_List
     * api-version: 2018-06-01
     * x-ms-examples: Factories_List
     */
    /**
     * Sample code: Factories_List.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void factoriesList(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.factories().list(Context.NONE);
    }
}
```

### Factories_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Factories ListByResourceGroup. */
public final class FactoriesListByResourceGroupSamples {
    /*
     * operationId: Factories_ListByResourceGroup
     * api-version: 2018-06-01
     * x-ms-examples: Factories_ListByResourceGroup
     */
    /**
     * Sample code: Factories_ListByResourceGroup.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void factoriesListByResourceGroup(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.factories().listByResourceGroup("exampleResourceGroup", Context.NONE);
    }
}
```

### Factories_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datafactory.models.Factory;
import java.util.HashMap;
import java.util.Map;

/** Samples for Factories Update. */
public final class FactoriesUpdateSamples {
    /*
     * operationId: Factories_Update
     * api-version: 2018-06-01
     * x-ms-examples: Factories_Update
     */
    /**
     * Sample code: Factories_Update.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void factoriesUpdate(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        Factory resource =
            manager
                .factories()
                .getByResourceGroupWithResponse("exampleResourceGroup", "exampleFactoryName", null, Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("exampleTag", "exampleValue")).apply();
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

### IntegrationRuntimeNodes_Delete

```java
import com.azure.core.util.Context;

/** Samples for IntegrationRuntimeNodes Delete. */
public final class IntegrationRuntimeNodesDeleteSamples {
    /*
     * operationId: IntegrationRuntimeNodes_Delete
     * api-version: 2018-06-01
     * x-ms-examples: IntegrationRuntimesNodes_Delete
     */
    /**
     * Sample code: IntegrationRuntimesNodes_Delete.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void integrationRuntimesNodesDelete(
        com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .integrationRuntimeNodes()
            .deleteWithResponse(
                "exampleResourceGroup", "exampleFactoryName", "exampleIntegrationRuntime", "Node_1", Context.NONE);
    }
}
```

### IntegrationRuntimeNodes_Get

```java
import com.azure.core.util.Context;

/** Samples for IntegrationRuntimeNodes Get. */
public final class IntegrationRuntimeNodesGetSamples {
    /*
     * operationId: IntegrationRuntimeNodes_Get
     * api-version: 2018-06-01
     * x-ms-examples: IntegrationRuntimeNodes_Get
     */
    /**
     * Sample code: IntegrationRuntimeNodes_Get.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void integrationRuntimeNodesGet(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .integrationRuntimeNodes()
            .getWithResponse(
                "exampleResourceGroup", "exampleFactoryName", "exampleIntegrationRuntime", "Node_1", Context.NONE);
    }
}
```

### IntegrationRuntimeNodes_GetIpAddress

```java
import com.azure.core.util.Context;

/** Samples for IntegrationRuntimeNodes GetIpAddress. */
public final class IntegrationRuntimeNodesGetIpAddressSamples {
    /*
     * operationId: IntegrationRuntimeNodes_GetIpAddress
     * api-version: 2018-06-01
     * x-ms-examples: IntegrationRuntimeNodes_GetIpAddress
     */
    /**
     * Sample code: IntegrationRuntimeNodes_GetIpAddress.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void integrationRuntimeNodesGetIpAddress(
        com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .integrationRuntimeNodes()
            .getIpAddressWithResponse(
                "exampleResourceGroup", "exampleFactoryName", "exampleIntegrationRuntime", "Node_1", Context.NONE);
    }
}
```

### IntegrationRuntimeNodes_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datafactory.models.UpdateIntegrationRuntimeNodeRequest;

/** Samples for IntegrationRuntimeNodes Update. */
public final class IntegrationRuntimeNodesUpdateSamples {
    /*
     * operationId: IntegrationRuntimeNodes_Update
     * api-version: 2018-06-01
     * x-ms-examples: IntegrationRuntimeNodes_Update
     */
    /**
     * Sample code: IntegrationRuntimeNodes_Update.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void integrationRuntimeNodesUpdate(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .integrationRuntimeNodes()
            .updateWithResponse(
                "exampleResourceGroup",
                "exampleFactoryName",
                "exampleIntegrationRuntime",
                "Node_1",
                new UpdateIntegrationRuntimeNodeRequest().withConcurrentJobsLimit(2),
                Context.NONE);
    }
}
```

### IntegrationRuntimeObjectMetadata_Get

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datafactory.models.GetSsisObjectMetadataRequest;

/** Samples for IntegrationRuntimeObjectMetadata Get. */
public final class IntegrationRuntimeObjectMetadataGetSamples {
    /*
     * operationId: IntegrationRuntimeObjectMetadata_Get
     * api-version: 2018-06-01
     * x-ms-examples: IntegrationRuntimeObjectMetadata_Get
     */
    /**
     * Sample code: IntegrationRuntimeObjectMetadata_Get.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void integrationRuntimeObjectMetadataGet(
        com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .integrationRuntimeObjectMetadatas()
            .getWithResponse(
                "exampleResourceGroup",
                "exampleFactoryName",
                "testactivityv2",
                new GetSsisObjectMetadataRequest().withMetadataPath("ssisFolders"),
                Context.NONE);
    }
}
```

### IntegrationRuntimeObjectMetadata_Refresh

```java
import com.azure.core.util.Context;

/** Samples for IntegrationRuntimeObjectMetadata Refresh. */
public final class IntegrationRuntimeObjectMetadataRefreshSamples {
    /*
     * operationId: IntegrationRuntimeObjectMetadata_Refresh
     * api-version: 2018-06-01
     * x-ms-examples: IntegrationRuntimeObjectMetadata_Refresh
     */
    /**
     * Sample code: IntegrationRuntimeObjectMetadata_Refresh.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void integrationRuntimeObjectMetadataRefresh(
        com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .integrationRuntimeObjectMetadatas()
            .refresh("exampleResourceGroup", "exampleFactoryName", "testactivityv2", Context.NONE);
    }
}
```

### IntegrationRuntimes_CreateLinkedIntegrationRuntime

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datafactory.models.CreateLinkedIntegrationRuntimeRequest;

/** Samples for IntegrationRuntimes CreateLinkedIntegrationRuntime. */
public final class IntegrationRuntimesCreateLinkedIntegrationRuntimeSamples {
    /*
     * operationId: IntegrationRuntimes_CreateLinkedIntegrationRuntime
     * api-version: 2018-06-01
     * x-ms-examples: IntegrationRuntimes_CreateLinkedIntegrationRuntime
     */
    /**
     * Sample code: IntegrationRuntimes_CreateLinkedIntegrationRuntime.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void integrationRuntimesCreateLinkedIntegrationRuntime(
        com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .integrationRuntimes()
            .createLinkedIntegrationRuntimeWithResponse(
                "exampleResourceGroup",
                "exampleFactoryName",
                "exampleIntegrationRuntime",
                new CreateLinkedIntegrationRuntimeRequest()
                    .withName("bfa92911-9fb6-4fbe-8f23-beae87bc1c83")
                    .withSubscriptionId("061774c7-4b5a-4159-a55b-365581830283")
                    .withDataFactoryName("e9955d6d-56ea-4be3-841c-52a12c1a9981")
                    .withDataFactoryLocation("West US"),
                Context.NONE);
    }
}
```

### IntegrationRuntimes_CreateOrUpdate

```java
import com.azure.resourcemanager.datafactory.models.SelfHostedIntegrationRuntime;

/** Samples for IntegrationRuntimes CreateOrUpdate. */
public final class IntegrationRuntimesCreateOrUpdateSamples {
    /*
     * operationId: IntegrationRuntimes_CreateOrUpdate
     * api-version: 2018-06-01
     * x-ms-examples: IntegrationRuntimes_Create
     */
    /**
     * Sample code: IntegrationRuntimes_Create.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void integrationRuntimesCreate(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .integrationRuntimes()
            .define("exampleIntegrationRuntime")
            .withExistingFactory("exampleResourceGroup", "exampleFactoryName")
            .withProperties(new SelfHostedIntegrationRuntime().withDescription("A selfhosted integration runtime"))
            .create();
    }
}
```

### IntegrationRuntimes_Delete

```java
import com.azure.core.util.Context;

/** Samples for IntegrationRuntimes Delete. */
public final class IntegrationRuntimesDeleteSamples {
    /*
     * operationId: IntegrationRuntimes_Delete
     * api-version: 2018-06-01
     * x-ms-examples: IntegrationRuntimes_Delete
     */
    /**
     * Sample code: IntegrationRuntimes_Delete.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void integrationRuntimesDelete(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .integrationRuntimes()
            .deleteWithResponse(
                "exampleResourceGroup", "exampleFactoryName", "exampleIntegrationRuntime", Context.NONE);
    }
}
```

### IntegrationRuntimes_Get

```java
import com.azure.core.util.Context;

/** Samples for IntegrationRuntimes Get. */
public final class IntegrationRuntimesGetSamples {
    /*
     * operationId: IntegrationRuntimes_Get
     * api-version: 2018-06-01
     * x-ms-examples: IntegrationRuntimes_Get
     */
    /**
     * Sample code: IntegrationRuntimes_Get.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void integrationRuntimesGet(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .integrationRuntimes()
            .getWithResponse(
                "exampleResourceGroup", "exampleFactoryName", "exampleIntegrationRuntime", null, Context.NONE);
    }
}
```

### IntegrationRuntimes_GetConnectionInfo

```java
import com.azure.core.util.Context;

/** Samples for IntegrationRuntimes GetConnectionInfo. */
public final class IntegrationRuntimesGetConnectionInfoSamples {
    /*
     * operationId: IntegrationRuntimes_GetConnectionInfo
     * api-version: 2018-06-01
     * x-ms-examples: IntegrationRuntimes_GetConnectionInfo
     */
    /**
     * Sample code: IntegrationRuntimes_GetConnectionInfo.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void integrationRuntimesGetConnectionInfo(
        com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .integrationRuntimes()
            .getConnectionInfoWithResponse(
                "exampleResourceGroup", "exampleFactoryName", "exampleIntegrationRuntime", Context.NONE);
    }
}
```

### IntegrationRuntimes_GetMonitoringData

```java
import com.azure.core.util.Context;

/** Samples for IntegrationRuntimes GetMonitoringData. */
public final class IntegrationRuntimesGetMonitoringDataSamples {
    /*
     * operationId: IntegrationRuntimes_GetMonitoringData
     * api-version: 2018-06-01
     * x-ms-examples: IntegrationRuntimes_GetMonitoringData
     */
    /**
     * Sample code: IntegrationRuntimes_GetMonitoringData.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void integrationRuntimesGetMonitoringData(
        com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .integrationRuntimes()
            .getMonitoringDataWithResponse(
                "exampleResourceGroup", "exampleFactoryName", "exampleIntegrationRuntime", Context.NONE);
    }
}
```

### IntegrationRuntimes_GetStatus

```java
import com.azure.core.util.Context;

/** Samples for IntegrationRuntimes GetStatus. */
public final class IntegrationRuntimesGetStatusSamples {
    /*
     * operationId: IntegrationRuntimes_GetStatus
     * api-version: 2018-06-01
     * x-ms-examples: IntegrationRuntimes_GetStatus
     */
    /**
     * Sample code: IntegrationRuntimes_GetStatus.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void integrationRuntimesGetStatus(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .integrationRuntimes()
            .getStatusWithResponse(
                "exampleResourceGroup", "exampleFactoryName", "exampleIntegrationRuntime", Context.NONE);
    }
}
```

### IntegrationRuntimes_ListAuthKeys

```java
import com.azure.core.util.Context;

/** Samples for IntegrationRuntimes ListAuthKeys. */
public final class IntegrationRuntimesListAuthKeysSamples {
    /*
     * operationId: IntegrationRuntimes_ListAuthKeys
     * api-version: 2018-06-01
     * x-ms-examples: IntegrationRuntimes_ListAuthKeys
     */
    /**
     * Sample code: IntegrationRuntimes_ListAuthKeys.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void integrationRuntimesListAuthKeys(
        com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .integrationRuntimes()
            .listAuthKeysWithResponse(
                "exampleResourceGroup", "exampleFactoryName", "exampleIntegrationRuntime", Context.NONE);
    }
}
```

### IntegrationRuntimes_ListByFactory

```java
import com.azure.core.util.Context;

/** Samples for IntegrationRuntimes ListByFactory. */
public final class IntegrationRuntimesListByFactorySamples {
    /*
     * operationId: IntegrationRuntimes_ListByFactory
     * api-version: 2018-06-01
     * x-ms-examples: IntegrationRuntimes_ListByFactory
     */
    /**
     * Sample code: IntegrationRuntimes_ListByFactory.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void integrationRuntimesListByFactory(
        com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.integrationRuntimes().listByFactory("exampleResourceGroup", "exampleFactoryName", Context.NONE);
    }
}
```

### IntegrationRuntimes_ListOutboundNetworkDependenciesEndpoints

```java
import com.azure.core.util.Context;

/** Samples for IntegrationRuntimes ListOutboundNetworkDependenciesEndpoints. */
public final class IntegrationRuntimesListOutboundNetworkDependenciesEndpointsSamples {
    /*
     * operationId: IntegrationRuntimes_ListOutboundNetworkDependenciesEndpoints
     * api-version: 2018-06-01
     * x-ms-examples: IntegrationRuntimes_OutboundNetworkDependenciesEndpoints
     */
    /**
     * Sample code: IntegrationRuntimes_OutboundNetworkDependenciesEndpoints.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void integrationRuntimesOutboundNetworkDependenciesEndpoints(
        com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .integrationRuntimes()
            .listOutboundNetworkDependenciesEndpointsWithResponse(
                "exampleResourceGroup", "exampleFactoryName", "exampleIntegrationRuntime", Context.NONE);
    }
}
```

### IntegrationRuntimes_RegenerateAuthKey

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datafactory.models.IntegrationRuntimeAuthKeyName;
import com.azure.resourcemanager.datafactory.models.IntegrationRuntimeRegenerateKeyParameters;

/** Samples for IntegrationRuntimes RegenerateAuthKey. */
public final class IntegrationRuntimesRegenerateAuthKeySamples {
    /*
     * operationId: IntegrationRuntimes_RegenerateAuthKey
     * api-version: 2018-06-01
     * x-ms-examples: IntegrationRuntimes_RegenerateAuthKey
     */
    /**
     * Sample code: IntegrationRuntimes_RegenerateAuthKey.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void integrationRuntimesRegenerateAuthKey(
        com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .integrationRuntimes()
            .regenerateAuthKeyWithResponse(
                "exampleResourceGroup",
                "exampleFactoryName",
                "exampleIntegrationRuntime",
                new IntegrationRuntimeRegenerateKeyParameters().withKeyName(IntegrationRuntimeAuthKeyName.AUTH_KEY2),
                Context.NONE);
    }
}
```

### IntegrationRuntimes_RemoveLinks

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datafactory.models.LinkedIntegrationRuntimeRequest;

/** Samples for IntegrationRuntimes RemoveLinks. */
public final class IntegrationRuntimesRemoveLinksSamples {
    /*
     * operationId: IntegrationRuntimes_RemoveLinks
     * api-version: 2018-06-01
     * x-ms-examples: IntegrationRuntimes_Upgrade
     */
    /**
     * Sample code: IntegrationRuntimes_Upgrade.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void integrationRuntimesUpgrade(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .integrationRuntimes()
            .removeLinksWithResponse(
                "exampleResourceGroup",
                "exampleFactoryName",
                "exampleIntegrationRuntime",
                new LinkedIntegrationRuntimeRequest().withLinkedFactoryName("exampleFactoryName-linked"),
                Context.NONE);
    }
}
```

### IntegrationRuntimes_Start

```java
import com.azure.core.util.Context;

/** Samples for IntegrationRuntimes Start. */
public final class IntegrationRuntimesStartSamples {
    /*
     * operationId: IntegrationRuntimes_Start
     * api-version: 2018-06-01
     * x-ms-examples: IntegrationRuntimes_Start
     */
    /**
     * Sample code: IntegrationRuntimes_Start.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void integrationRuntimesStart(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .integrationRuntimes()
            .start("exampleResourceGroup", "exampleFactoryName", "exampleManagedIntegrationRuntime", Context.NONE);
    }
}
```

### IntegrationRuntimes_Stop

```java
import com.azure.core.util.Context;

/** Samples for IntegrationRuntimes Stop. */
public final class IntegrationRuntimesStopSamples {
    /*
     * operationId: IntegrationRuntimes_Stop
     * api-version: 2018-06-01
     * x-ms-examples: IntegrationRuntimes_Stop
     */
    /**
     * Sample code: IntegrationRuntimes_Stop.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void integrationRuntimesStop(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .integrationRuntimes()
            .stop("exampleResourceGroup", "exampleFactoryName", "exampleManagedIntegrationRuntime", Context.NONE);
    }
}
```

### IntegrationRuntimes_SyncCredentials

```java
import com.azure.core.util.Context;

/** Samples for IntegrationRuntimes SyncCredentials. */
public final class IntegrationRuntimesSyncCredentialsSamples {
    /*
     * operationId: IntegrationRuntimes_SyncCredentials
     * api-version: 2018-06-01
     * x-ms-examples: IntegrationRuntimes_SyncCredentials
     */
    /**
     * Sample code: IntegrationRuntimes_SyncCredentials.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void integrationRuntimesSyncCredentials(
        com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .integrationRuntimes()
            .syncCredentialsWithResponse(
                "exampleResourceGroup", "exampleFactoryName", "exampleIntegrationRuntime", Context.NONE);
    }
}
```

### IntegrationRuntimes_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datafactory.models.IntegrationRuntimeAutoUpdate;
import com.azure.resourcemanager.datafactory.models.IntegrationRuntimeResource;

/** Samples for IntegrationRuntimes Update. */
public final class IntegrationRuntimesUpdateSamples {
    /*
     * operationId: IntegrationRuntimes_Update
     * api-version: 2018-06-01
     * x-ms-examples: IntegrationRuntimes_Update
     */
    /**
     * Sample code: IntegrationRuntimes_Update.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void integrationRuntimesUpdate(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        IntegrationRuntimeResource resource =
            manager
                .integrationRuntimes()
                .getWithResponse(
                    "exampleResourceGroup", "exampleFactoryName", "exampleIntegrationRuntime", null, Context.NONE)
                .getValue();
        resource.update().withAutoUpdate(IntegrationRuntimeAutoUpdate.OFF).withUpdateDelayOffset("\"PT3H\"").apply();
    }
}
```

### IntegrationRuntimes_Upgrade

```java
import com.azure.core.util.Context;

/** Samples for IntegrationRuntimes Upgrade. */
public final class IntegrationRuntimesUpgradeSamples {
    /*
     * operationId: IntegrationRuntimes_Upgrade
     * api-version: 2018-06-01
     * x-ms-examples: IntegrationRuntimes_Upgrade
     */
    /**
     * Sample code: IntegrationRuntimes_Upgrade.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void integrationRuntimesUpgrade(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .integrationRuntimes()
            .upgradeWithResponse(
                "exampleResourceGroup", "exampleFactoryName", "exampleIntegrationRuntime", Context.NONE);
    }
}
```

### LinkedServices_CreateOrUpdate

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.datafactory.models.AzureStorageLinkedService;
import com.azure.resourcemanager.datafactory.models.LinkedServiceResource;
import java.io.IOException;

/** Samples for LinkedServices CreateOrUpdate. */
public final class LinkedServicesCreateOrUpdateSamples {
    /*
     * operationId: LinkedServices_CreateOrUpdate
     * api-version: 2018-06-01
     * x-ms-examples: LinkedServices_Create
     */
    /**
     * Sample code: LinkedServices_Create.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void linkedServicesCreate(com.azure.resourcemanager.datafactory.DataFactoryManager manager)
        throws IOException {
        manager
            .linkedServices()
            .define("exampleLinkedService")
            .withExistingFactory("exampleResourceGroup", "exampleFactoryName")
            .withProperties(
                new AzureStorageLinkedService()
                    .withConnectionString(
                        SerializerFactory
                            .createDefaultManagementSerializerAdapter()
                            .deserialize(
                                "{\"type\":\"SecureString\",\"value\":\"DefaultEndpointsProtocol=https;AccountName=examplestorageaccount;AccountKey=<storage"
                                    + " key>\"}",
                                Object.class,
                                SerializerEncoding.JSON)))
            .create();
    }

    /*
     * operationId: LinkedServices_CreateOrUpdate
     * api-version: 2018-06-01
     * x-ms-examples: LinkedServices_Update
     */
    /**
     * Sample code: LinkedServices_Update.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void linkedServicesUpdate(com.azure.resourcemanager.datafactory.DataFactoryManager manager)
        throws IOException {
        LinkedServiceResource resource =
            manager
                .linkedServices()
                .getWithResponse(
                    "exampleResourceGroup", "exampleFactoryName", "exampleLinkedService", null, Context.NONE)
                .getValue();
        resource
            .update()
            .withProperties(
                new AzureStorageLinkedService()
                    .withDescription("Example description")
                    .withConnectionString(
                        SerializerFactory
                            .createDefaultManagementSerializerAdapter()
                            .deserialize(
                                "{\"type\":\"SecureString\",\"value\":\"DefaultEndpointsProtocol=https;AccountName=examplestorageaccount;AccountKey=<storage"
                                    + " key>\"}",
                                Object.class,
                                SerializerEncoding.JSON)))
            .apply();
    }
}
```

### LinkedServices_Delete

```java
import com.azure.core.util.Context;

/** Samples for LinkedServices Delete. */
public final class LinkedServicesDeleteSamples {
    /*
     * operationId: LinkedServices_Delete
     * api-version: 2018-06-01
     * x-ms-examples: LinkedServices_Delete
     */
    /**
     * Sample code: LinkedServices_Delete.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void linkedServicesDelete(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .linkedServices()
            .deleteWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleLinkedService", Context.NONE);
    }
}
```

### LinkedServices_Get

```java
import com.azure.core.util.Context;

/** Samples for LinkedServices Get. */
public final class LinkedServicesGetSamples {
    /*
     * operationId: LinkedServices_Get
     * api-version: 2018-06-01
     * x-ms-examples: LinkedServices_Get
     */
    /**
     * Sample code: LinkedServices_Get.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void linkedServicesGet(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .linkedServices()
            .getWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleLinkedService", null, Context.NONE);
    }
}
```

### LinkedServices_ListByFactory

```java
import com.azure.core.util.Context;

/** Samples for LinkedServices ListByFactory. */
public final class LinkedServicesListByFactorySamples {
    /*
     * operationId: LinkedServices_ListByFactory
     * api-version: 2018-06-01
     * x-ms-examples: LinkedServices_ListByFactory
     */
    /**
     * Sample code: LinkedServices_ListByFactory.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void linkedServicesListByFactory(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.linkedServices().listByFactory("exampleResourceGroup", "exampleFactoryName", Context.NONE);
    }
}
```

### ManagedPrivateEndpoints_CreateOrUpdate

```java
import com.azure.resourcemanager.datafactory.models.ManagedPrivateEndpoint;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for ManagedPrivateEndpoints CreateOrUpdate. */
public final class ManagedPrivateEndpointsCreateOrUpdateSamples {
    /*
     * operationId: ManagedPrivateEndpoints_CreateOrUpdate
     * api-version: 2018-06-01
     * x-ms-examples: ManagedVirtualNetworks_Create
     */
    /**
     * Sample code: ManagedVirtualNetworks_Create.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void managedVirtualNetworksCreate(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .managedPrivateEndpoints()
            .define("exampleManagedPrivateEndpointName")
            .withExistingManagedVirtualNetwork(
                "exampleResourceGroup", "exampleFactoryName", "exampleManagedVirtualNetworkName")
            .withProperties(
                new ManagedPrivateEndpoint()
                    .withFqdns(Arrays.asList())
                    .withGroupId("blob")
                    .withPrivateLinkResourceId(
                        "/subscriptions/12345678-1234-1234-1234-12345678abc/resourceGroups/exampleResourceGroup/providers/Microsoft.Storage/storageAccounts/exampleBlobStorage")
                    .withAdditionalProperties(mapOf()))
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

### ManagedPrivateEndpoints_Delete

```java
import com.azure.core.util.Context;

/** Samples for ManagedPrivateEndpoints Delete. */
public final class ManagedPrivateEndpointsDeleteSamples {
    /*
     * operationId: ManagedPrivateEndpoints_Delete
     * api-version: 2018-06-01
     * x-ms-examples: ManagedVirtualNetworks_Delete
     */
    /**
     * Sample code: ManagedVirtualNetworks_Delete.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void managedVirtualNetworksDelete(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .managedPrivateEndpoints()
            .deleteWithResponse(
                "exampleResourceGroup",
                "exampleFactoryName",
                "exampleManagedVirtualNetworkName",
                "exampleManagedPrivateEndpointName",
                Context.NONE);
    }
}
```

### ManagedPrivateEndpoints_Get

```java
import com.azure.core.util.Context;

/** Samples for ManagedPrivateEndpoints Get. */
public final class ManagedPrivateEndpointsGetSamples {
    /*
     * operationId: ManagedPrivateEndpoints_Get
     * api-version: 2018-06-01
     * x-ms-examples: ManagedPrivateEndpoints_Get
     */
    /**
     * Sample code: ManagedPrivateEndpoints_Get.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void managedPrivateEndpointsGet(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .managedPrivateEndpoints()
            .getWithResponse(
                "exampleResourceGroup",
                "exampleFactoryName",
                "exampleManagedVirtualNetworkName",
                "exampleManagedPrivateEndpointName",
                null,
                Context.NONE);
    }
}
```

### ManagedPrivateEndpoints_ListByFactory

```java
import com.azure.core.util.Context;

/** Samples for ManagedPrivateEndpoints ListByFactory. */
public final class ManagedPrivateEndpointsListByFactorySamples {
    /*
     * operationId: ManagedPrivateEndpoints_ListByFactory
     * api-version: 2018-06-01
     * x-ms-examples: ManagedPrivateEndpoints_ListByFactory
     */
    /**
     * Sample code: ManagedPrivateEndpoints_ListByFactory.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void managedPrivateEndpointsListByFactory(
        com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .managedPrivateEndpoints()
            .listByFactory(
                "exampleResourceGroup", "exampleFactoryName", "exampleManagedVirtualNetworkName", Context.NONE);
    }
}
```

### ManagedVirtualNetworks_CreateOrUpdate

```java
import com.azure.resourcemanager.datafactory.models.ManagedVirtualNetwork;
import java.util.HashMap;
import java.util.Map;

/** Samples for ManagedVirtualNetworks CreateOrUpdate. */
public final class ManagedVirtualNetworksCreateOrUpdateSamples {
    /*
     * operationId: ManagedVirtualNetworks_CreateOrUpdate
     * api-version: 2018-06-01
     * x-ms-examples: ManagedVirtualNetworks_Create
     */
    /**
     * Sample code: ManagedVirtualNetworks_Create.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void managedVirtualNetworksCreate(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .managedVirtualNetworks()
            .define("exampleManagedVirtualNetworkName")
            .withExistingFactory("exampleResourceGroup", "exampleFactoryName")
            .withProperties(new ManagedVirtualNetwork().withAdditionalProperties(mapOf()))
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

### ManagedVirtualNetworks_Get

```java
import com.azure.core.util.Context;

/** Samples for ManagedVirtualNetworks Get. */
public final class ManagedVirtualNetworksGetSamples {
    /*
     * operationId: ManagedVirtualNetworks_Get
     * api-version: 2018-06-01
     * x-ms-examples: ManagedVirtualNetworks_Get
     */
    /**
     * Sample code: ManagedVirtualNetworks_Get.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void managedVirtualNetworksGet(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .managedVirtualNetworks()
            .getWithResponse(
                "exampleResourceGroup", "exampleFactoryName", "exampleManagedVirtualNetworkName", null, Context.NONE);
    }
}
```

### ManagedVirtualNetworks_ListByFactory

```java
import com.azure.core.util.Context;

/** Samples for ManagedVirtualNetworks ListByFactory. */
public final class ManagedVirtualNetworksListByFactorySamples {
    /*
     * operationId: ManagedVirtualNetworks_ListByFactory
     * api-version: 2018-06-01
     * x-ms-examples: ManagedVirtualNetworks_ListByFactory
     */
    /**
     * Sample code: ManagedVirtualNetworks_ListByFactory.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void managedVirtualNetworksListByFactory(
        com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.managedVirtualNetworks().listByFactory("exampleResourceGroup", "exampleFactoryName", Context.NONE);
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * operationId: Operations_List
     * api-version: 2018-06-01
     * x-ms-examples: Operations_List
     */
    /**
     * Sample code: Operations_List.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void operationsList(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### PipelineRuns_Cancel

```java
import com.azure.core.util.Context;

/** Samples for PipelineRuns Cancel. */
public final class PipelineRunsCancelSamples {
    /*
     * operationId: PipelineRuns_Cancel
     * api-version: 2018-06-01
     * x-ms-examples: PipelineRuns_Cancel
     */
    /**
     * Sample code: PipelineRuns_Cancel.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void pipelineRunsCancel(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .pipelineRuns()
            .cancelWithResponse(
                "exampleResourceGroup",
                "exampleFactoryName",
                "16ac5348-ff82-4f95-a80d-638c1d47b721",
                null,
                Context.NONE);
    }
}
```

### PipelineRuns_Get

```java
import com.azure.core.util.Context;

/** Samples for PipelineRuns Get. */
public final class PipelineRunsGetSamples {
    /*
     * operationId: PipelineRuns_Get
     * api-version: 2018-06-01
     * x-ms-examples: PipelineRuns_Get
     */
    /**
     * Sample code: PipelineRuns_Get.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void pipelineRunsGet(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .pipelineRuns()
            .getWithResponse(
                "exampleResourceGroup", "exampleFactoryName", "2f7fdb90-5df1-4b8e-ac2f-064cfa58202b", Context.NONE);
    }
}
```

### PipelineRuns_QueryByFactory

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datafactory.models.RunFilterParameters;
import com.azure.resourcemanager.datafactory.models.RunQueryFilter;
import com.azure.resourcemanager.datafactory.models.RunQueryFilterOperand;
import com.azure.resourcemanager.datafactory.models.RunQueryFilterOperator;
import java.time.OffsetDateTime;
import java.util.Arrays;

/** Samples for PipelineRuns QueryByFactory. */
public final class PipelineRunsQueryByFactorySamples {
    /*
     * operationId: PipelineRuns_QueryByFactory
     * api-version: 2018-06-01
     * x-ms-examples: PipelineRuns_QueryByFactory
     */
    /**
     * Sample code: PipelineRuns_QueryByFactory.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void pipelineRunsQueryByFactory(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .pipelineRuns()
            .queryByFactoryWithResponse(
                "exampleResourceGroup",
                "exampleFactoryName",
                new RunFilterParameters()
                    .withLastUpdatedAfter(OffsetDateTime.parse("2018-06-16T00:36:44.3345758Z"))
                    .withLastUpdatedBefore(OffsetDateTime.parse("2018-06-16T00:49:48.3686473Z"))
                    .withFilters(
                        Arrays
                            .asList(
                                new RunQueryFilter()
                                    .withOperand(RunQueryFilterOperand.PIPELINE_NAME)
                                    .withOperator(RunQueryFilterOperator.EQUALS)
                                    .withValues(Arrays.asList("examplePipeline")))),
                Context.NONE);
    }
}
```

### Pipelines_CreateOrUpdate

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.datafactory.models.Activity;
import com.azure.resourcemanager.datafactory.models.ParameterSpecification;
import com.azure.resourcemanager.datafactory.models.ParameterType;
import com.azure.resourcemanager.datafactory.models.PipelineElapsedTimeMetricPolicy;
import com.azure.resourcemanager.datafactory.models.PipelinePolicy;
import com.azure.resourcemanager.datafactory.models.PipelineResource;
import com.azure.resourcemanager.datafactory.models.VariableSpecification;
import com.azure.resourcemanager.datafactory.models.VariableType;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Pipelines CreateOrUpdate. */
public final class PipelinesCreateOrUpdateSamples {
    /*
     * operationId: Pipelines_CreateOrUpdate
     * api-version: 2018-06-01
     * x-ms-examples: Pipelines_Create
     */
    /**
     * Sample code: Pipelines_Create.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void pipelinesCreate(com.azure.resourcemanager.datafactory.DataFactoryManager manager)
        throws IOException {
        manager
            .pipelines()
            .define("examplePipeline")
            .withExistingFactory("exampleResourceGroup", "exampleFactoryName")
            .withActivities(
                Arrays
                    .asList(
                        new Activity()
                            .withName("ExampleForeachActivity")
                            .withAdditionalProperties(
                                mapOf(
                                    "typeProperties",
                                    SerializerFactory
                                        .createDefaultManagementSerializerAdapter()
                                        .deserialize(
                                            "{\"activities\":[{\"name\":\"ExampleCopyActivity\",\"type\":\"Copy\",\"inputs\":[{\"type\":\"DatasetReference\",\"parameters\":{\"MyFileName\":\"examplecontainer.csv\",\"MyFolderPath\":\"examplecontainer\"},\"referenceName\":\"exampleDataset\"}],\"outputs\":[{\"type\":\"DatasetReference\",\"parameters\":{\"MyFileName\":{\"type\":\"Expression\",\"value\":\"@item()\"},\"MyFolderPath\":\"examplecontainer\"},\"referenceName\":\"exampleDataset\"}],\"typeProperties\":{\"dataIntegrationUnits\":32,\"sink\":{\"type\":\"BlobSink\"},\"source\":{\"type\":\"BlobSource\"}}}],\"isSequential\":true,\"items\":{\"type\":\"Expression\",\"value\":\"@pipeline().parameters.OutputBlobNameList\"}}",
                                            Object.class,
                                            SerializerEncoding.JSON),
                                    "type",
                                    "ForEach"))))
            .withParameters(
                mapOf(
                    "JobId",
                    new ParameterSpecification().withType(ParameterType.STRING),
                    "OutputBlobNameList",
                    new ParameterSpecification().withType(ParameterType.ARRAY)))
            .withVariables(mapOf("TestVariableArray", new VariableSpecification().withType(VariableType.ARRAY)))
            .withRunDimensions(
                mapOf(
                    "JobId",
                    SerializerFactory
                        .createDefaultManagementSerializerAdapter()
                        .deserialize(
                            "{\"type\":\"Expression\",\"value\":\"@pipeline().parameters.JobId\"}",
                            Object.class,
                            SerializerEncoding.JSON)))
            .withPolicy(
                new PipelinePolicy()
                    .withElapsedTimeMetric(new PipelineElapsedTimeMetricPolicy().withDuration("0.00:10:00")))
            .create();
    }

    /*
     * operationId: Pipelines_CreateOrUpdate
     * api-version: 2018-06-01
     * x-ms-examples: Pipelines_Update
     */
    /**
     * Sample code: Pipelines_Update.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void pipelinesUpdate(com.azure.resourcemanager.datafactory.DataFactoryManager manager)
        throws IOException {
        PipelineResource resource =
            manager
                .pipelines()
                .getWithResponse("exampleResourceGroup", "exampleFactoryName", "examplePipeline", null, Context.NONE)
                .getValue();
        resource
            .update()
            .withDescription("Example description")
            .withActivities(
                Arrays
                    .asList(
                        new Activity()
                            .withName("ExampleForeachActivity")
                            .withAdditionalProperties(
                                mapOf(
                                    "typeProperties",
                                    SerializerFactory
                                        .createDefaultManagementSerializerAdapter()
                                        .deserialize(
                                            "{\"activities\":[{\"name\":\"ExampleCopyActivity\",\"type\":\"Copy\",\"inputs\":[{\"type\":\"DatasetReference\",\"parameters\":{\"MyFileName\":\"examplecontainer.csv\",\"MyFolderPath\":\"examplecontainer\"},\"referenceName\":\"exampleDataset\"}],\"outputs\":[{\"type\":\"DatasetReference\",\"parameters\":{\"MyFileName\":{\"type\":\"Expression\",\"value\":\"@item()\"},\"MyFolderPath\":\"examplecontainer\"},\"referenceName\":\"exampleDataset\"}],\"typeProperties\":{\"dataIntegrationUnits\":32,\"sink\":{\"type\":\"BlobSink\"},\"source\":{\"type\":\"BlobSource\"}}}],\"isSequential\":true,\"items\":{\"type\":\"Expression\",\"value\":\"@pipeline().parameters.OutputBlobNameList\"}}",
                                            Object.class,
                                            SerializerEncoding.JSON),
                                    "type",
                                    "ForEach"))))
            .withParameters(mapOf("OutputBlobNameList", new ParameterSpecification().withType(ParameterType.ARRAY)))
            .withPolicy(
                new PipelinePolicy()
                    .withElapsedTimeMetric(new PipelineElapsedTimeMetricPolicy().withDuration("0.00:10:00")))
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

### Pipelines_CreateRun

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.SerializerEncoding;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** Samples for Pipelines CreateRun. */
public final class PipelinesCreateRunSamples {
    /*
     * operationId: Pipelines_CreateRun
     * api-version: 2018-06-01
     * x-ms-examples: Pipelines_CreateRun
     */
    /**
     * Sample code: Pipelines_CreateRun.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void pipelinesCreateRun(com.azure.resourcemanager.datafactory.DataFactoryManager manager)
        throws IOException {
        manager
            .pipelines()
            .createRunWithResponse(
                "exampleResourceGroup",
                "exampleFactoryName",
                "examplePipeline",
                null,
                null,
                null,
                null,
                mapOf(
                    "OutputBlobNameList",
                    SerializerFactory
                        .createDefaultManagementSerializerAdapter()
                        .deserialize("[\"exampleoutput.csv\"]", Object.class, SerializerEncoding.JSON)),
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

### Pipelines_Delete

```java
import com.azure.core.util.Context;

/** Samples for Pipelines Delete. */
public final class PipelinesDeleteSamples {
    /*
     * operationId: Pipelines_Delete
     * api-version: 2018-06-01
     * x-ms-examples: Pipelines_Delete
     */
    /**
     * Sample code: Pipelines_Delete.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void pipelinesDelete(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .pipelines()
            .deleteWithResponse("exampleResourceGroup", "exampleFactoryName", "examplePipeline", Context.NONE);
    }
}
```

### Pipelines_Get

```java
import com.azure.core.util.Context;

/** Samples for Pipelines Get. */
public final class PipelinesGetSamples {
    /*
     * operationId: Pipelines_Get
     * api-version: 2018-06-01
     * x-ms-examples: Pipelines_Get
     */
    /**
     * Sample code: Pipelines_Get.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void pipelinesGet(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .pipelines()
            .getWithResponse("exampleResourceGroup", "exampleFactoryName", "examplePipeline", null, Context.NONE);
    }
}
```

### Pipelines_ListByFactory

```java
import com.azure.core.util.Context;

/** Samples for Pipelines ListByFactory. */
public final class PipelinesListByFactorySamples {
    /*
     * operationId: Pipelines_ListByFactory
     * api-version: 2018-06-01
     * x-ms-examples: Pipelines_ListByFactory
     */
    /**
     * Sample code: Pipelines_ListByFactory.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void pipelinesListByFactory(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.pipelines().listByFactory("exampleResourceGroup", "exampleFactoryName", Context.NONE);
    }
}
```

### PrivateEndPointConnections_ListByFactory

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndPointConnections ListByFactory. */
public final class PrivateEndPointConnectionsListByFactorySamples {
    /*
     * operationId: PrivateEndPointConnections_ListByFactory
     * api-version: 2018-06-01
     * x-ms-examples: privateEndPointConnections_ListByFactory
     */
    /**
     * Sample code: privateEndPointConnections_ListByFactory.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void privateEndPointConnectionsListByFactory(
        com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.privateEndPointConnections().listByFactory("exampleResourceGroup", "exampleFactoryName", Context.NONE);
    }
}
```

### PrivateEndpointConnectionOperation_CreateOrUpdate

```java
import com.azure.resourcemanager.datafactory.models.PrivateLinkConnectionApprovalRequest;
import com.azure.resourcemanager.datafactory.models.PrivateLinkConnectionState;

/** Samples for PrivateEndpointConnectionOperation CreateOrUpdate. */
public final class PrivateEndpointConnectionOperationCreateOrUpdateSamples {
    /*
     * operationId: PrivateEndpointConnection_CreateOrUpdate
     * api-version: 2018-06-01
     * x-ms-examples: Approves or rejects a private endpoint connection for a factory.
     */
    /**
     * Sample code: Approves or rejects a private endpoint connection for a factory.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void approvesOrRejectsAPrivateEndpointConnectionForAFactory(
        com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .privateEndpointConnectionOperations()
            .define("connection")
            .withExistingFactory("exampleResourceGroup", "exampleFactoryName")
            .withProperties(
                new PrivateLinkConnectionApprovalRequest()
                    .withPrivateLinkServiceConnectionState(
                        new PrivateLinkConnectionState()
                            .withStatus("Approved")
                            .withDescription("Approved by admin.")
                            .withActionsRequired("")))
            .create();
    }
}
```

### PrivateEndpointConnectionOperation_Delete

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnectionOperation Delete. */
public final class PrivateEndpointConnectionOperationDeleteSamples {
    /*
     * operationId: PrivateEndpointConnection_Delete
     * api-version: 2018-06-01
     * x-ms-examples: Delete a private endpoint connection for a datafactory.
     */
    /**
     * Sample code: Delete a private endpoint connection for a datafactory.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void deleteAPrivateEndpointConnectionForADatafactory(
        com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .privateEndpointConnectionOperations()
            .deleteWithResponse("exampleResourceGroup", "exampleFactoryName", "connection", Context.NONE);
    }
}
```

### PrivateEndpointConnectionOperation_Get

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnectionOperation Get. */
public final class PrivateEndpointConnectionOperationGetSamples {
    /*
     * operationId: PrivateEndpointConnection_Get
     * api-version: 2018-06-01
     * x-ms-examples: Get a private endpoint connection for a datafactory.
     */
    /**
     * Sample code: Get a private endpoint connection for a datafactory.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void getAPrivateEndpointConnectionForADatafactory(
        com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .privateEndpointConnectionOperations()
            .getWithResponse("exampleResourceGroup", "exampleFactoryName", "connection", null, Context.NONE);
    }
}
```

### PrivateLinkResources_Get

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkResources Get. */
public final class PrivateLinkResourcesGetSamples {
    /*
     * operationId: PrivateLinkResources_Get
     * api-version: 2018-06-01
     * x-ms-examples: Get private link resources of a site
     */
    /**
     * Sample code: Get private link resources of a site.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void getPrivateLinkResourcesOfASite(
        com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.privateLinkResources().getWithResponse("exampleResourceGroup", "exampleFactoryName", Context.NONE);
    }
}
```

### TriggerRuns_Cancel

```java
import com.azure.core.util.Context;

/** Samples for TriggerRuns Cancel. */
public final class TriggerRunsCancelSamples {
    /*
     * operationId: TriggerRuns_Cancel
     * api-version: 2018-06-01
     * x-ms-examples: Triggers_Cancel
     */
    /**
     * Sample code: Triggers_Cancel.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void triggersCancel(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .triggerRuns()
            .cancelWithResponse(
                "exampleResourceGroup",
                "exampleFactoryName",
                "exampleTrigger",
                "2f7fdb90-5df1-4b8e-ac2f-064cfa58202b",
                Context.NONE);
    }
}
```

### TriggerRuns_QueryByFactory

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datafactory.models.RunFilterParameters;
import com.azure.resourcemanager.datafactory.models.RunQueryFilter;
import com.azure.resourcemanager.datafactory.models.RunQueryFilterOperand;
import com.azure.resourcemanager.datafactory.models.RunQueryFilterOperator;
import java.time.OffsetDateTime;
import java.util.Arrays;

/** Samples for TriggerRuns QueryByFactory. */
public final class TriggerRunsQueryByFactorySamples {
    /*
     * operationId: TriggerRuns_QueryByFactory
     * api-version: 2018-06-01
     * x-ms-examples: TriggerRuns_QueryByFactory
     */
    /**
     * Sample code: TriggerRuns_QueryByFactory.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void triggerRunsQueryByFactory(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .triggerRuns()
            .queryByFactoryWithResponse(
                "exampleResourceGroup",
                "exampleFactoryName",
                new RunFilterParameters()
                    .withLastUpdatedAfter(OffsetDateTime.parse("2018-06-16T00:36:44.3345758Z"))
                    .withLastUpdatedBefore(OffsetDateTime.parse("2018-06-16T00:49:48.3686473Z"))
                    .withFilters(
                        Arrays
                            .asList(
                                new RunQueryFilter()
                                    .withOperand(RunQueryFilterOperand.TRIGGER_NAME)
                                    .withOperator(RunQueryFilterOperator.EQUALS)
                                    .withValues(Arrays.asList("exampleTrigger")))),
                Context.NONE);
    }
}
```

### TriggerRuns_Rerun

```java
import com.azure.core.util.Context;

/** Samples for TriggerRuns Rerun. */
public final class TriggerRunsRerunSamples {
    /*
     * operationId: TriggerRuns_Rerun
     * api-version: 2018-06-01
     * x-ms-examples: Triggers_Rerun
     */
    /**
     * Sample code: Triggers_Rerun.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void triggersRerun(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .triggerRuns()
            .rerunWithResponse(
                "exampleResourceGroup",
                "exampleFactoryName",
                "exampleTrigger",
                "2f7fdb90-5df1-4b8e-ac2f-064cfa58202b",
                Context.NONE);
    }
}
```

### Triggers_CreateOrUpdate

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.datafactory.models.Trigger;
import com.azure.resourcemanager.datafactory.models.TriggerResource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** Samples for Triggers CreateOrUpdate. */
public final class TriggersCreateOrUpdateSamples {
    /*
     * operationId: Triggers_CreateOrUpdate
     * api-version: 2018-06-01
     * x-ms-examples: Triggers_Create
     */
    /**
     * Sample code: Triggers_Create.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void triggersCreate(com.azure.resourcemanager.datafactory.DataFactoryManager manager)
        throws IOException {
        manager
            .triggers()
            .define("exampleTrigger")
            .withExistingFactory("exampleResourceGroup", "exampleFactoryName")
            .withProperties(
                new Trigger()
                    .withAdditionalProperties(
                        mapOf(
                            "typeProperties",
                            SerializerFactory
                                .createDefaultManagementSerializerAdapter()
                                .deserialize(
                                    "{\"recurrence\":{\"endTime\":\"2018-06-16T00:55:13.8441801Z\",\"frequency\":\"Minute\",\"interval\":4,\"startTime\":\"2018-06-16T00:39:13.8441801Z\",\"timeZone\":\"UTC\"}}",
                                    Object.class,
                                    SerializerEncoding.JSON),
                            "pipelines",
                            SerializerFactory
                                .createDefaultManagementSerializerAdapter()
                                .deserialize(
                                    "[{\"parameters\":{\"OutputBlobNameList\":[\"exampleoutput.csv\"]},\"pipelineReference\":{\"type\":\"PipelineReference\",\"referenceName\":\"examplePipeline\"}}]",
                                    Object.class,
                                    SerializerEncoding.JSON),
                            "type",
                            "ScheduleTrigger")))
            .create();
    }

    /*
     * operationId: Triggers_CreateOrUpdate
     * api-version: 2018-06-01
     * x-ms-examples: Triggers_Update
     */
    /**
     * Sample code: Triggers_Update.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void triggersUpdate(com.azure.resourcemanager.datafactory.DataFactoryManager manager)
        throws IOException {
        TriggerResource resource =
            manager
                .triggers()
                .getWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleTrigger", null, Context.NONE)
                .getValue();
        resource
            .update()
            .withProperties(
                new Trigger()
                    .withDescription("Example description")
                    .withAdditionalProperties(
                        mapOf(
                            "typeProperties",
                            SerializerFactory
                                .createDefaultManagementSerializerAdapter()
                                .deserialize(
                                    "{\"recurrence\":{\"endTime\":\"2018-06-16T00:55:14.905167Z\",\"frequency\":\"Minute\",\"interval\":4,\"startTime\":\"2018-06-16T00:39:14.905167Z\",\"timeZone\":\"UTC\"}}",
                                    Object.class,
                                    SerializerEncoding.JSON),
                            "pipelines",
                            SerializerFactory
                                .createDefaultManagementSerializerAdapter()
                                .deserialize(
                                    "[{\"parameters\":{\"OutputBlobNameList\":[\"exampleoutput.csv\"]},\"pipelineReference\":{\"type\":\"PipelineReference\",\"referenceName\":\"examplePipeline\"}}]",
                                    Object.class,
                                    SerializerEncoding.JSON),
                            "type",
                            "ScheduleTrigger")))
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

### Triggers_Delete

```java
import com.azure.core.util.Context;

/** Samples for Triggers Delete. */
public final class TriggersDeleteSamples {
    /*
     * operationId: Triggers_Delete
     * api-version: 2018-06-01
     * x-ms-examples: Triggers_Delete
     */
    /**
     * Sample code: Triggers_Delete.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void triggersDelete(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .triggers()
            .deleteWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleTrigger", Context.NONE);
    }
}
```

### Triggers_Get

```java
import com.azure.core.util.Context;

/** Samples for Triggers Get. */
public final class TriggersGetSamples {
    /*
     * operationId: Triggers_Get
     * api-version: 2018-06-01
     * x-ms-examples: Triggers_Get
     */
    /**
     * Sample code: Triggers_Get.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void triggersGet(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .triggers()
            .getWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleTrigger", null, Context.NONE);
    }
}
```

### Triggers_GetEventSubscriptionStatus

```java
import com.azure.core.util.Context;

/** Samples for Triggers GetEventSubscriptionStatus. */
public final class TriggersGetEventSubscriptionStatusSamples {
    /*
     * operationId: Triggers_GetEventSubscriptionStatus
     * api-version: 2018-06-01
     * x-ms-examples: Triggers_GetEventSubscriptionStatus
     */
    /**
     * Sample code: Triggers_GetEventSubscriptionStatus.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void triggersGetEventSubscriptionStatus(
        com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .triggers()
            .getEventSubscriptionStatusWithResponse(
                "exampleResourceGroup", "exampleFactoryName", "exampleTrigger", Context.NONE);
    }
}
```

### Triggers_ListByFactory

```java
import com.azure.core.util.Context;

/** Samples for Triggers ListByFactory. */
public final class TriggersListByFactorySamples {
    /*
     * operationId: Triggers_ListByFactory
     * api-version: 2018-06-01
     * x-ms-examples: Triggers_ListByFactory
     */
    /**
     * Sample code: Triggers_ListByFactory.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void triggersListByFactory(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.triggers().listByFactory("exampleResourceGroup", "exampleFactoryName", Context.NONE);
    }
}
```

### Triggers_QueryByFactory

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datafactory.models.TriggerFilterParameters;

/** Samples for Triggers QueryByFactory. */
public final class TriggersQueryByFactorySamples {
    /*
     * operationId: Triggers_QueryByFactory
     * api-version: 2018-06-01
     * x-ms-examples: Triggers_QueryByFactory
     */
    /**
     * Sample code: Triggers_QueryByFactory.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void triggersQueryByFactory(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .triggers()
            .queryByFactoryWithResponse(
                "exampleResourceGroup",
                "exampleFactoryName",
                new TriggerFilterParameters().withParentTriggerName("exampleTrigger"),
                Context.NONE);
    }
}
```

### Triggers_Start

```java
import com.azure.core.util.Context;

/** Samples for Triggers Start. */
public final class TriggersStartSamples {
    /*
     * operationId: Triggers_Start
     * api-version: 2018-06-01
     * x-ms-examples: Triggers_Start
     */
    /**
     * Sample code: Triggers_Start.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void triggersStart(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.triggers().start("exampleResourceGroup", "exampleFactoryName", "exampleTrigger", Context.NONE);
    }
}
```

### Triggers_Stop

```java
import com.azure.core.util.Context;

/** Samples for Triggers Stop. */
public final class TriggersStopSamples {
    /*
     * operationId: Triggers_Stop
     * api-version: 2018-06-01
     * x-ms-examples: Triggers_Stop
     */
    /**
     * Sample code: Triggers_Stop.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void triggersStop(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.triggers().stop("exampleResourceGroup", "exampleFactoryName", "exampleTrigger", Context.NONE);
    }
}
```

### Triggers_SubscribeToEvents

```java
import com.azure.core.util.Context;

/** Samples for Triggers SubscribeToEvents. */
public final class TriggersSubscribeToEventsSamples {
    /*
     * operationId: Triggers_SubscribeToEvents
     * api-version: 2018-06-01
     * x-ms-examples: Triggers_SubscribeToEvents
     */
    /**
     * Sample code: Triggers_SubscribeToEvents.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void triggersSubscribeToEvents(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .triggers()
            .subscribeToEvents("exampleResourceGroup", "exampleFactoryName", "exampleTrigger", Context.NONE);
    }
}
```

### Triggers_UnsubscribeFromEvents

```java
import com.azure.core.util.Context;

/** Samples for Triggers UnsubscribeFromEvents. */
public final class TriggersUnsubscribeFromEventsSamples {
    /*
     * operationId: Triggers_UnsubscribeFromEvents
     * api-version: 2018-06-01
     * x-ms-examples: Triggers_UnsubscribeFromEvents
     */
    /**
     * Sample code: Triggers_UnsubscribeFromEvents.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void triggersUnsubscribeFromEvents(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .triggers()
            .unsubscribeFromEvents("exampleResourceGroup", "exampleFactoryName", "exampleTrigger", Context.NONE);
    }
}
```

