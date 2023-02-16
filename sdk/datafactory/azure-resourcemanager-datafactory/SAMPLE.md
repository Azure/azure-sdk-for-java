# Code snippets and samples


## ActivityRuns

- [QueryByPipelineRun](#activityruns_querybypipelinerun)

## CredentialOperations

- [CreateOrUpdate](#credentialoperations_createorupdate)
- [Delete](#credentialoperations_delete)
- [Get](#credentialoperations_get)
- [ListByFactory](#credentialoperations_listbyfactory)

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

## GlobalParameters

- [CreateOrUpdate](#globalparameters_createorupdate)
- [Delete](#globalparameters_delete)
- [Get](#globalparameters_get)
- [ListByFactory](#globalparameters_listbyfactory)

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
import com.azure.resourcemanager.datafactory.models.RunFilterParameters;
import java.time.OffsetDateTime;

/** Samples for ActivityRuns QueryByPipelineRun. */
public final class ActivityRunsQueryByPipelineRunSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/ActivityRuns_QueryByPipelineRun.json
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
                com.azure.core.util.Context.NONE);
    }
}
```

### CredentialOperations_CreateOrUpdate

```java
import com.azure.resourcemanager.datafactory.models.ManagedIdentityCredential;

/** Samples for CredentialOperations CreateOrUpdate. */
public final class CredentialOperationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/Credentials_Create.json
     */
    /**
     * Sample code: Credentials_Create.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void credentialsCreate(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .credentialOperations()
            .define("exampleCredential")
            .withExistingFactory("exampleResourceGroup", "exampleFactoryName")
            .withProperties(
                new ManagedIdentityCredential()
                    .withResourceId(
                        "/subscriptions/12345678-1234-1234-1234-12345678abc/resourcegroups/exampleResourceGroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/exampleUami"))
            .create();
    }
}
```

### CredentialOperations_Delete

```java
/** Samples for CredentialOperations Delete. */
public final class CredentialOperationsDeleteSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/Credentials_Delete.json
     */
    /**
     * Sample code: Credentials_Delete.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void credentialsDelete(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .credentialOperations()
            .deleteWithResponse(
                "exampleResourceGroup", "exampleFactoryName", "exampleCredential", com.azure.core.util.Context.NONE);
    }
}
```

### CredentialOperations_Get

```java
/** Samples for CredentialOperations Get. */
public final class CredentialOperationsGetSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/Credentials_Get.json
     */
    /**
     * Sample code: Credentials_Get.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void credentialsGet(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .credentialOperations()
            .getWithResponse(
                "exampleResourceGroup",
                "exampleFactoryName",
                "exampleCredential",
                null,
                com.azure.core.util.Context.NONE);
    }
}
```

### CredentialOperations_ListByFactory

```java
/** Samples for CredentialOperations ListByFactory. */
public final class CredentialOperationsListByFactorySamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/Credentials_ListByFactory.json
     */
    /**
     * Sample code: Credentials_ListByFactory.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void credentialsListByFactory(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .credentialOperations()
            .listByFactory("exampleResourceGroup", "exampleFactoryName", com.azure.core.util.Context.NONE);
    }
}
```

### DataFlowDebugSession_AddDataFlow

```java
import com.azure.core.management.serializer.SerializerFactory;
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
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/DataFlowDebugSession_AddDataFlow.json
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
                                            .withEncryptedCredential("fakeTokenPlaceholder"))))
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
                com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/DataFlowDebugSession_Create.json
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
                com.azure.core.util.Context.NONE);
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
import com.azure.resourcemanager.datafactory.models.DeleteDataFlowDebugSessionRequest;

/** Samples for DataFlowDebugSession Delete. */
public final class DataFlowDebugSessionDeleteSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/DataFlowDebugSession_Delete.json
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
                com.azure.core.util.Context.NONE);
    }
}
```

### DataFlowDebugSession_ExecuteCommand

```java
import com.azure.resourcemanager.datafactory.models.DataFlowDebugCommandPayload;
import com.azure.resourcemanager.datafactory.models.DataFlowDebugCommandRequest;
import com.azure.resourcemanager.datafactory.models.DataFlowDebugCommandType;

/** Samples for DataFlowDebugSession ExecuteCommand. */
public final class DataFlowDebugSessionExecuteCommandSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/DataFlowDebugSession_ExecuteCommand.json
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
                com.azure.core.util.Context.NONE);
    }
}
```

### DataFlowDebugSession_QueryByFactory

```java
/** Samples for DataFlowDebugSession QueryByFactory. */
public final class DataFlowDebugSessionQueryByFactorySamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/DataFlowDebugSession_QueryByFactory.json
     */
    /**
     * Sample code: DataFlowDebugSession_QueryByFactory.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void dataFlowDebugSessionQueryByFactory(
        com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .dataFlowDebugSessions()
            .queryByFactory("exampleResourceGroup", "exampleFactoryName", com.azure.core.util.Context.NONE);
    }
}
```

### DataFlows_CreateOrUpdate

```java
import com.azure.resourcemanager.datafactory.models.DataFlowResource;
import com.azure.resourcemanager.datafactory.models.DataFlowSink;
import com.azure.resourcemanager.datafactory.models.DataFlowSource;
import com.azure.resourcemanager.datafactory.models.DatasetReference;
import com.azure.resourcemanager.datafactory.models.MappingDataFlow;
import java.util.Arrays;

/** Samples for DataFlows CreateOrUpdate. */
public final class DataFlowsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/DataFlows_Create.json
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
                    .withScriptLines(
                        Arrays
                            .asList(
                                "source(output(",
                                "PreviousConversionRate as double,",
                                "Country as string,",
                                "DateTime1 as string,",
                                "CurrentConversionRate as double",
                                "),",
                                "allowSchemaDrift: false,",
                                "validateSchema: false) ~> USDCurrency",
                                "source(output(",
                                "PreviousConversionRate as double,",
                                "Country as string,",
                                "DateTime1 as string,",
                                "CurrentConversionRate as double",
                                "),",
                                "allowSchemaDrift: true,",
                                "validateSchema: false) ~> CADSource",
                                "USDCurrency, CADSource union(byName: true)~> Union",
                                "Union derive(NewCurrencyRate = round(CurrentConversionRate*1.25)) ~>"
                                    + " NewCurrencyColumn",
                                "NewCurrencyColumn split(Country == 'USD',",
                                "Country == 'CAD',disjoint: false) ~> ConditionalSplit1@(USD, CAD)",
                                "ConditionalSplit1@USD sink(saveMode:'overwrite' ) ~> USDSink",
                                "ConditionalSplit1@CAD sink(saveMode:'overwrite' ) ~> CADSink")))
            .create();
    }

    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/DataFlows_Update.json
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
                .getWithResponse(
                    "exampleResourceGroup",
                    "exampleFactoryName",
                    "exampleDataFlow",
                    null,
                    com.azure.core.util.Context.NONE)
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
                    .withScriptLines(
                        Arrays
                            .asList(
                                "source(output(",
                                "PreviousConversionRate as double,",
                                "Country as string,",
                                "DateTime1 as string,",
                                "CurrentConversionRate as double",
                                "),",
                                "allowSchemaDrift: false,",
                                "validateSchema: false) ~> USDCurrency",
                                "source(output(",
                                "PreviousConversionRate as double,",
                                "Country as string,",
                                "DateTime1 as string,",
                                "CurrentConversionRate as double",
                                "),",
                                "allowSchemaDrift: true,",
                                "validateSchema: false) ~> CADSource",
                                "USDCurrency, CADSource union(byName: true)~> Union",
                                "Union derive(NewCurrencyRate = round(CurrentConversionRate*1.25)) ~>"
                                    + " NewCurrencyColumn",
                                "NewCurrencyColumn split(Country == 'USD',",
                                "Country == 'CAD',disjoint: false) ~> ConditionalSplit1@(USD, CAD)",
                                "ConditionalSplit1@USD sink(saveMode:'overwrite' ) ~> USDSink",
                                "ConditionalSplit1@CAD sink(saveMode:'overwrite' ) ~> CADSink")))
            .apply();
    }
}
```

### DataFlows_Delete

```java
/** Samples for DataFlows Delete. */
public final class DataFlowsDeleteSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/DataFlows_Delete.json
     */
    /**
     * Sample code: DataFlows_Delete.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void dataFlowsDelete(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .dataFlows()
            .deleteWithResponse(
                "exampleResourceGroup", "exampleFactoryName", "exampleDataFlow", com.azure.core.util.Context.NONE);
    }
}
```

### DataFlows_Get

```java
/** Samples for DataFlows Get. */
public final class DataFlowsGetSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/DataFlows_Get.json
     */
    /**
     * Sample code: DataFlows_Get.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void dataFlowsGet(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .dataFlows()
            .getWithResponse(
                "exampleResourceGroup",
                "exampleFactoryName",
                "exampleDataFlow",
                null,
                com.azure.core.util.Context.NONE);
    }
}
```

### DataFlows_ListByFactory

```java
/** Samples for DataFlows ListByFactory. */
public final class DataFlowsListByFactorySamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/DataFlows_ListByFactory.json
     */
    /**
     * Sample code: DataFlows_ListByFactory.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void dataFlowsListByFactory(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .dataFlows()
            .listByFactory("exampleResourceGroup", "exampleFactoryName", com.azure.core.util.Context.NONE);
    }
}
```

### Datasets_CreateOrUpdate

```java
import com.azure.core.management.serializer.SerializerFactory;
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
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/Datasets_Create.json
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
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/Datasets_Update.json
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
                .getWithResponse(
                    "exampleResourceGroup",
                    "exampleFactoryName",
                    "exampleDataset",
                    null,
                    com.azure.core.util.Context.NONE)
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
/** Samples for Datasets Delete. */
public final class DatasetsDeleteSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/Datasets_Delete.json
     */
    /**
     * Sample code: Datasets_Delete.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void datasetsDelete(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .datasets()
            .deleteWithResponse(
                "exampleResourceGroup", "exampleFactoryName", "exampleDataset", com.azure.core.util.Context.NONE);
    }
}
```

### Datasets_Get

```java
/** Samples for Datasets Get. */
public final class DatasetsGetSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/Datasets_Get.json
     */
    /**
     * Sample code: Datasets_Get.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void datasetsGet(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .datasets()
            .getWithResponse(
                "exampleResourceGroup", "exampleFactoryName", "exampleDataset", null, com.azure.core.util.Context.NONE);
    }
}
```

### Datasets_ListByFactory

```java
/** Samples for Datasets ListByFactory. */
public final class DatasetsListByFactorySamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/Datasets_ListByFactory.json
     */
    /**
     * Sample code: Datasets_ListByFactory.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void datasetsListByFactory(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .datasets()
            .listByFactory("exampleResourceGroup", "exampleFactoryName", com.azure.core.util.Context.NONE);
    }
}
```

### ExposureControl_GetFeatureValue

```java
import com.azure.resourcemanager.datafactory.models.ExposureControlRequest;

/** Samples for ExposureControl GetFeatureValue. */
public final class ExposureControlGetFeatureValueSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/ExposureControl_GetFeatureValue.json
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
                com.azure.core.util.Context.NONE);
    }
}
```

### ExposureControl_GetFeatureValueByFactory

```java
import com.azure.resourcemanager.datafactory.models.ExposureControlRequest;

/** Samples for ExposureControl GetFeatureValueByFactory. */
public final class ExposureControlGetFeatureValueByFactorySamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/ExposureControl_GetFeatureValueByFactory.json
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
                com.azure.core.util.Context.NONE);
    }
}
```

### ExposureControl_QueryFeatureValuesByFactory

```java
import com.azure.resourcemanager.datafactory.models.ExposureControlBatchRequest;
import com.azure.resourcemanager.datafactory.models.ExposureControlRequest;
import java.util.Arrays;

/** Samples for ExposureControl QueryFeatureValuesByFactory. */
public final class ExposureControlQueryFeatureValuesByFactorySamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/ExposureControl_QueryFeatureValuesByFactory.json
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
                com.azure.core.util.Context.NONE);
    }
}
```

### Factories_ConfigureFactoryRepo

```java
import com.azure.resourcemanager.datafactory.models.FactoryRepoUpdate;
import com.azure.resourcemanager.datafactory.models.FactoryVstsConfiguration;

/** Samples for Factories ConfigureFactoryRepo. */
public final class FactoriesConfigureFactoryRepoSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/Factories_ConfigureFactoryRepo.json
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
                com.azure.core.util.Context.NONE);
    }
}
```

### Factories_CreateOrUpdate

```java
/** Samples for Factories CreateOrUpdate. */
public final class FactoriesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/Factories_CreateOrUpdate.json
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
/** Samples for Factories Delete. */
public final class FactoriesDeleteSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/Factories_Delete.json
     */
    /**
     * Sample code: Factories_Delete.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void factoriesDelete(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .factories()
            .deleteByResourceGroupWithResponse(
                "exampleResourceGroup", "exampleFactoryName", com.azure.core.util.Context.NONE);
    }
}
```

### Factories_GetByResourceGroup

```java
/** Samples for Factories GetByResourceGroup. */
public final class FactoriesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/Factories_Get.json
     */
    /**
     * Sample code: Factories_Get.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void factoriesGet(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .factories()
            .getByResourceGroupWithResponse(
                "exampleResourceGroup", "exampleFactoryName", null, com.azure.core.util.Context.NONE);
    }
}
```

### Factories_GetDataPlaneAccess

```java
import com.azure.resourcemanager.datafactory.models.UserAccessPolicy;

/** Samples for Factories GetDataPlaneAccess. */
public final class FactoriesGetDataPlaneAccessSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/Factories_GetDataPlaneAccess.json
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
                com.azure.core.util.Context.NONE);
    }
}
```

### Factories_GetGitHubAccessToken

```java
import com.azure.resourcemanager.datafactory.models.GitHubAccessTokenRequest;

/** Samples for Factories GetGitHubAccessToken. */
public final class FactoriesGetGitHubAccessTokenSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/Factories_GetGitHubAccessToken.json
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
                    .withGitHubAccessCode("fakeTokenPlaceholder")
                    .withGitHubClientId("some")
                    .withGitHubAccessTokenBaseUrl("fakeTokenPlaceholder"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Factories_List

```java
/** Samples for Factories List. */
public final class FactoriesListSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/Factories_List.json
     */
    /**
     * Sample code: Factories_List.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void factoriesList(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.factories().list(com.azure.core.util.Context.NONE);
    }
}
```

### Factories_ListByResourceGroup

```java
/** Samples for Factories ListByResourceGroup. */
public final class FactoriesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/Factories_ListByResourceGroup.json
     */
    /**
     * Sample code: Factories_ListByResourceGroup.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void factoriesListByResourceGroup(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.factories().listByResourceGroup("exampleResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### Factories_Update

```java
import com.azure.resourcemanager.datafactory.models.Factory;
import java.util.HashMap;
import java.util.Map;

/** Samples for Factories Update. */
public final class FactoriesUpdateSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/Factories_Update.json
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
                .getByResourceGroupWithResponse(
                    "exampleResourceGroup", "exampleFactoryName", null, com.azure.core.util.Context.NONE)
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

### GlobalParameters_CreateOrUpdate

```java
import com.azure.resourcemanager.datafactory.models.GlobalParameterResource;
import com.azure.resourcemanager.datafactory.models.GlobalParameterSpecification;
import java.util.Map;

/** Samples for GlobalParameters CreateOrUpdate. */
public final class GlobalParametersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/GlobalParameters_Create.json
     */
    /**
     * Sample code: GlobalParameters_Create.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void globalParametersCreate(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .globalParameters()
            .define("default")
            .withExistingFactory("exampleResourceGroup", "exampleFactoryName")
            .withProperties((Map<String, GlobalParameterSpecification>) null)
            .create();
    }

    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/GlobalParameters_Update.json
     */
    /**
     * Sample code: GlobalParameters_Update.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void globalParametersUpdate(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        GlobalParameterResource resource =
            manager
                .globalParameters()
                .getWithResponse(
                    "exampleResourceGroup", "exampleFactoryName", "default", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().apply();
    }
}
```

### GlobalParameters_Delete

```java
/** Samples for GlobalParameters Delete. */
public final class GlobalParametersDeleteSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/GlobalParameters_Delete.json
     */
    /**
     * Sample code: GlobalParameters_Delete.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void globalParametersDelete(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .globalParameters()
            .deleteWithResponse(
                "exampleResourceGroup", "exampleFactoryName", "default", com.azure.core.util.Context.NONE);
    }
}
```

### GlobalParameters_Get

```java
/** Samples for GlobalParameters Get. */
public final class GlobalParametersGetSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/GlobalParameters_Get.json
     */
    /**
     * Sample code: GlobalParameters_Get.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void globalParametersGet(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .globalParameters()
            .getWithResponse("exampleResourceGroup", "exampleFactoryName", "default", com.azure.core.util.Context.NONE);
    }
}
```

### GlobalParameters_ListByFactory

```java
/** Samples for GlobalParameters ListByFactory. */
public final class GlobalParametersListByFactorySamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/GlobalParameters_ListByFactory.json
     */
    /**
     * Sample code: GlobalParameters_ListByFactory.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void globalParametersListByFactory(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .globalParameters()
            .listByFactory("exampleResourceGroup", "exampleFactoryName", com.azure.core.util.Context.NONE);
    }
}
```

### IntegrationRuntimeNodes_Delete

```java
/** Samples for IntegrationRuntimeNodes Delete. */
public final class IntegrationRuntimeNodesDeleteSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/IntegrationRuntimeNodes_Delete.json
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
                "exampleResourceGroup",
                "exampleFactoryName",
                "exampleIntegrationRuntime",
                "Node_1",
                com.azure.core.util.Context.NONE);
    }
}
```

### IntegrationRuntimeNodes_Get

```java
/** Samples for IntegrationRuntimeNodes Get. */
public final class IntegrationRuntimeNodesGetSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/IntegrationRuntimeNodes_Get.json
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
                "exampleResourceGroup",
                "exampleFactoryName",
                "exampleIntegrationRuntime",
                "Node_1",
                com.azure.core.util.Context.NONE);
    }
}
```

### IntegrationRuntimeNodes_GetIpAddress

```java
/** Samples for IntegrationRuntimeNodes GetIpAddress. */
public final class IntegrationRuntimeNodesGetIpAddressSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/IntegrationRuntimeNodes_GetIpAddress.json
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
                "exampleResourceGroup",
                "exampleFactoryName",
                "exampleIntegrationRuntime",
                "Node_1",
                com.azure.core.util.Context.NONE);
    }
}
```

### IntegrationRuntimeNodes_Update

```java
import com.azure.resourcemanager.datafactory.models.UpdateIntegrationRuntimeNodeRequest;

/** Samples for IntegrationRuntimeNodes Update. */
public final class IntegrationRuntimeNodesUpdateSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/IntegrationRuntimeNodes_Update.json
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
                com.azure.core.util.Context.NONE);
    }
}
```

### IntegrationRuntimeObjectMetadata_Get

```java
import com.azure.resourcemanager.datafactory.models.GetSsisObjectMetadataRequest;

/** Samples for IntegrationRuntimeObjectMetadata Get. */
public final class IntegrationRuntimeObjectMetadataGetSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/IntegrationRuntimeObjectMetadata_Get.json
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
                com.azure.core.util.Context.NONE);
    }
}
```

### IntegrationRuntimeObjectMetadata_Refresh

```java
/** Samples for IntegrationRuntimeObjectMetadata Refresh. */
public final class IntegrationRuntimeObjectMetadataRefreshSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/IntegrationRuntimeObjectMetadata_Refresh.json
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
            .refresh("exampleResourceGroup", "exampleFactoryName", "testactivityv2", com.azure.core.util.Context.NONE);
    }
}
```

### IntegrationRuntimes_CreateLinkedIntegrationRuntime

```java
import com.azure.resourcemanager.datafactory.models.CreateLinkedIntegrationRuntimeRequest;

/** Samples for IntegrationRuntimes CreateLinkedIntegrationRuntime. */
public final class IntegrationRuntimesCreateLinkedIntegrationRuntimeSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/IntegrationRuntimes_CreateLinkedIntegrationRuntime.json
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
                com.azure.core.util.Context.NONE);
    }
}
```

### IntegrationRuntimes_CreateOrUpdate

```java
import com.azure.resourcemanager.datafactory.models.SelfHostedIntegrationRuntime;

/** Samples for IntegrationRuntimes CreateOrUpdate. */
public final class IntegrationRuntimesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/IntegrationRuntimes_Create.json
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
/** Samples for IntegrationRuntimes Delete. */
public final class IntegrationRuntimesDeleteSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/IntegrationRuntimes_Delete.json
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
                "exampleResourceGroup",
                "exampleFactoryName",
                "exampleIntegrationRuntime",
                com.azure.core.util.Context.NONE);
    }
}
```

### IntegrationRuntimes_Get

```java
/** Samples for IntegrationRuntimes Get. */
public final class IntegrationRuntimesGetSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/IntegrationRuntimes_Get.json
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
                "exampleResourceGroup",
                "exampleFactoryName",
                "exampleIntegrationRuntime",
                null,
                com.azure.core.util.Context.NONE);
    }
}
```

### IntegrationRuntimes_GetConnectionInfo

```java
/** Samples for IntegrationRuntimes GetConnectionInfo. */
public final class IntegrationRuntimesGetConnectionInfoSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/IntegrationRuntimes_GetConnectionInfo.json
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
                "exampleResourceGroup",
                "exampleFactoryName",
                "exampleIntegrationRuntime",
                com.azure.core.util.Context.NONE);
    }
}
```

### IntegrationRuntimes_GetMonitoringData

```java
/** Samples for IntegrationRuntimes GetMonitoringData. */
public final class IntegrationRuntimesGetMonitoringDataSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/IntegrationRuntimes_GetMonitoringData.json
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
                "exampleResourceGroup",
                "exampleFactoryName",
                "exampleIntegrationRuntime",
                com.azure.core.util.Context.NONE);
    }
}
```

### IntegrationRuntimes_GetStatus

```java
/** Samples for IntegrationRuntimes GetStatus. */
public final class IntegrationRuntimesGetStatusSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/IntegrationRuntimes_GetStatus.json
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
                "exampleResourceGroup",
                "exampleFactoryName",
                "exampleIntegrationRuntime",
                com.azure.core.util.Context.NONE);
    }
}
```

### IntegrationRuntimes_ListAuthKeys

```java
/** Samples for IntegrationRuntimes ListAuthKeys. */
public final class IntegrationRuntimesListAuthKeysSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/IntegrationRuntimes_ListAuthKeys.json
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
                "exampleResourceGroup",
                "exampleFactoryName",
                "exampleIntegrationRuntime",
                com.azure.core.util.Context.NONE);
    }
}
```

### IntegrationRuntimes_ListByFactory

```java
/** Samples for IntegrationRuntimes ListByFactory. */
public final class IntegrationRuntimesListByFactorySamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/IntegrationRuntimes_ListByFactory.json
     */
    /**
     * Sample code: IntegrationRuntimes_ListByFactory.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void integrationRuntimesListByFactory(
        com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .integrationRuntimes()
            .listByFactory("exampleResourceGroup", "exampleFactoryName", com.azure.core.util.Context.NONE);
    }
}
```

### IntegrationRuntimes_ListOutboundNetworkDependenciesEndpoints

```java
/** Samples for IntegrationRuntimes ListOutboundNetworkDependenciesEndpoints. */
public final class IntegrationRuntimesListOutboundNetworkDependenciesEndpointsSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/IntegrationRuntimes_ListOutboundNetworkDependenciesEndpoints.json
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
                "exampleResourceGroup",
                "exampleFactoryName",
                "exampleIntegrationRuntime",
                com.azure.core.util.Context.NONE);
    }
}
```

### IntegrationRuntimes_RegenerateAuthKey

```java
import com.azure.resourcemanager.datafactory.models.IntegrationRuntimeAuthKeyName;
import com.azure.resourcemanager.datafactory.models.IntegrationRuntimeRegenerateKeyParameters;

/** Samples for IntegrationRuntimes RegenerateAuthKey. */
public final class IntegrationRuntimesRegenerateAuthKeySamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/IntegrationRuntimes_RegenerateAuthKey.json
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
                com.azure.core.util.Context.NONE);
    }
}
```

### IntegrationRuntimes_RemoveLinks

```java
import com.azure.resourcemanager.datafactory.models.LinkedIntegrationRuntimeRequest;

/** Samples for IntegrationRuntimes RemoveLinks. */
public final class IntegrationRuntimesRemoveLinksSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/IntegrationRuntimes_RemoveLinks.json
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
                com.azure.core.util.Context.NONE);
    }
}
```

### IntegrationRuntimes_Start

```java
/** Samples for IntegrationRuntimes Start. */
public final class IntegrationRuntimesStartSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/IntegrationRuntimes_Start.json
     */
    /**
     * Sample code: IntegrationRuntimes_Start.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void integrationRuntimesStart(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .integrationRuntimes()
            .start(
                "exampleResourceGroup",
                "exampleFactoryName",
                "exampleManagedIntegrationRuntime",
                com.azure.core.util.Context.NONE);
    }
}
```

### IntegrationRuntimes_Stop

```java
/** Samples for IntegrationRuntimes Stop. */
public final class IntegrationRuntimesStopSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/IntegrationRuntimes_Stop.json
     */
    /**
     * Sample code: IntegrationRuntimes_Stop.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void integrationRuntimesStop(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .integrationRuntimes()
            .stop(
                "exampleResourceGroup",
                "exampleFactoryName",
                "exampleManagedIntegrationRuntime",
                com.azure.core.util.Context.NONE);
    }
}
```

### IntegrationRuntimes_SyncCredentials

```java
/** Samples for IntegrationRuntimes SyncCredentials. */
public final class IntegrationRuntimesSyncCredentialsSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/IntegrationRuntimes_SyncCredentials.json
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
                "exampleResourceGroup",
                "exampleFactoryName",
                "exampleIntegrationRuntime",
                com.azure.core.util.Context.NONE);
    }
}
```

### IntegrationRuntimes_Update

```java
import com.azure.resourcemanager.datafactory.models.IntegrationRuntimeAutoUpdate;
import com.azure.resourcemanager.datafactory.models.IntegrationRuntimeResource;

/** Samples for IntegrationRuntimes Update. */
public final class IntegrationRuntimesUpdateSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/IntegrationRuntimes_Update.json
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
                    "exampleResourceGroup",
                    "exampleFactoryName",
                    "exampleIntegrationRuntime",
                    null,
                    com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withAutoUpdate(IntegrationRuntimeAutoUpdate.OFF).withUpdateDelayOffset("\"PT3H\"").apply();
    }
}
```

### IntegrationRuntimes_Upgrade

```java
/** Samples for IntegrationRuntimes Upgrade. */
public final class IntegrationRuntimesUpgradeSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/IntegrationRuntimes_Upgrade.json
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
                "exampleResourceGroup",
                "exampleFactoryName",
                "exampleIntegrationRuntime",
                com.azure.core.util.Context.NONE);
    }
}
```

### LinkedServices_CreateOrUpdate

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.datafactory.models.AzureStorageLinkedService;
import com.azure.resourcemanager.datafactory.models.LinkedServiceResource;
import java.io.IOException;

/** Samples for LinkedServices CreateOrUpdate. */
public final class LinkedServicesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/LinkedServices_Create.json
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
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/LinkedServices_Update.json
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
                    "exampleResourceGroup",
                    "exampleFactoryName",
                    "exampleLinkedService",
                    null,
                    com.azure.core.util.Context.NONE)
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
/** Samples for LinkedServices Delete. */
public final class LinkedServicesDeleteSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/LinkedServices_Delete.json
     */
    /**
     * Sample code: LinkedServices_Delete.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void linkedServicesDelete(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .linkedServices()
            .deleteWithResponse(
                "exampleResourceGroup", "exampleFactoryName", "exampleLinkedService", com.azure.core.util.Context.NONE);
    }
}
```

### LinkedServices_Get

```java
/** Samples for LinkedServices Get. */
public final class LinkedServicesGetSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/LinkedServices_Get.json
     */
    /**
     * Sample code: LinkedServices_Get.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void linkedServicesGet(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .linkedServices()
            .getWithResponse(
                "exampleResourceGroup",
                "exampleFactoryName",
                "exampleLinkedService",
                null,
                com.azure.core.util.Context.NONE);
    }
}
```

### LinkedServices_ListByFactory

```java
/** Samples for LinkedServices ListByFactory. */
public final class LinkedServicesListByFactorySamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/LinkedServices_ListByFactory.json
     */
    /**
     * Sample code: LinkedServices_ListByFactory.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void linkedServicesListByFactory(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .linkedServices()
            .listByFactory("exampleResourceGroup", "exampleFactoryName", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/ManagedPrivateEndpoints_Create.json
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
/** Samples for ManagedPrivateEndpoints Delete. */
public final class ManagedPrivateEndpointsDeleteSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/ManagedPrivateEndpoints_Delete.json
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
                com.azure.core.util.Context.NONE);
    }
}
```

### ManagedPrivateEndpoints_Get

```java
/** Samples for ManagedPrivateEndpoints Get. */
public final class ManagedPrivateEndpointsGetSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/ManagedPrivateEndpoints_Get.json
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
                com.azure.core.util.Context.NONE);
    }
}
```

### ManagedPrivateEndpoints_ListByFactory

```java
/** Samples for ManagedPrivateEndpoints ListByFactory. */
public final class ManagedPrivateEndpointsListByFactorySamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/ManagedPrivateEndpoints_ListByFactory.json
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
                "exampleResourceGroup",
                "exampleFactoryName",
                "exampleManagedVirtualNetworkName",
                com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/ManagedVirtualNetworks_Create.json
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
/** Samples for ManagedVirtualNetworks Get. */
public final class ManagedVirtualNetworksGetSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/ManagedVirtualNetworks_Get.json
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
                "exampleResourceGroup",
                "exampleFactoryName",
                "exampleManagedVirtualNetworkName",
                null,
                com.azure.core.util.Context.NONE);
    }
}
```

### ManagedVirtualNetworks_ListByFactory

```java
/** Samples for ManagedVirtualNetworks ListByFactory. */
public final class ManagedVirtualNetworksListByFactorySamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/ManagedVirtualNetworks_ListByFactory.json
     */
    /**
     * Sample code: ManagedVirtualNetworks_ListByFactory.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void managedVirtualNetworksListByFactory(
        com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .managedVirtualNetworks()
            .listByFactory("exampleResourceGroup", "exampleFactoryName", com.azure.core.util.Context.NONE);
    }
}
```

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void operationsList(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### PipelineRuns_Cancel

```java
/** Samples for PipelineRuns Cancel. */
public final class PipelineRunsCancelSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/PipelineRuns_Cancel.json
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
                com.azure.core.util.Context.NONE);
    }
}
```

### PipelineRuns_Get

```java
/** Samples for PipelineRuns Get. */
public final class PipelineRunsGetSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/PipelineRuns_Get.json
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
                "exampleResourceGroup",
                "exampleFactoryName",
                "2f7fdb90-5df1-4b8e-ac2f-064cfa58202b",
                com.azure.core.util.Context.NONE);
    }
}
```

### PipelineRuns_QueryByFactory

```java
import com.azure.resourcemanager.datafactory.models.RunFilterParameters;
import com.azure.resourcemanager.datafactory.models.RunQueryFilter;
import com.azure.resourcemanager.datafactory.models.RunQueryFilterOperand;
import com.azure.resourcemanager.datafactory.models.RunQueryFilterOperator;
import java.time.OffsetDateTime;
import java.util.Arrays;

/** Samples for PipelineRuns QueryByFactory. */
public final class PipelineRunsQueryByFactorySamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/PipelineRuns_QueryByFactory.json
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
                com.azure.core.util.Context.NONE);
    }
}
```

### Pipelines_CreateOrUpdate

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.datafactory.models.BlobSink;
import com.azure.resourcemanager.datafactory.models.BlobSource;
import com.azure.resourcemanager.datafactory.models.CopyActivity;
import com.azure.resourcemanager.datafactory.models.DatasetReference;
import com.azure.resourcemanager.datafactory.models.Expression;
import com.azure.resourcemanager.datafactory.models.ForEachActivity;
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
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/Pipelines_Create.json
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
                        new ForEachActivity()
                            .withName("ExampleForeachActivity")
                            .withIsSequential(true)
                            .withItems(new Expression().withValue("@pipeline().parameters.OutputBlobNameList"))
                            .withActivities(
                                Arrays
                                    .asList(
                                        new CopyActivity()
                                            .withName("ExampleCopyActivity")
                                            .withInputs(
                                                Arrays
                                                    .asList(
                                                        new DatasetReference()
                                                            .withReferenceName("exampleDataset")
                                                            .withParameters(
                                                                mapOf(
                                                                    "MyFileName",
                                                                    "examplecontainer.csv",
                                                                    "MyFolderPath",
                                                                    "examplecontainer"))))
                                            .withOutputs(
                                                Arrays
                                                    .asList(
                                                        new DatasetReference()
                                                            .withReferenceName("exampleDataset")
                                                            .withParameters(
                                                                mapOf(
                                                                    "MyFileName",
                                                                    SerializerFactory
                                                                        .createDefaultManagementSerializerAdapter()
                                                                        .deserialize(
                                                                            "{\"type\":\"Expression\",\"value\":\"@item()\"}",
                                                                            Object.class,
                                                                            SerializerEncoding.JSON),
                                                                    "MyFolderPath",
                                                                    "examplecontainer"))))
                                            .withSource(new BlobSource())
                                            .withSink(new BlobSink())
                                            .withDataIntegrationUnits(32)))))
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
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/Pipelines_Update.json
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
                .getWithResponse(
                    "exampleResourceGroup",
                    "exampleFactoryName",
                    "examplePipeline",
                    null,
                    com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withDescription("Example description")
            .withActivities(
                Arrays
                    .asList(
                        new ForEachActivity()
                            .withName("ExampleForeachActivity")
                            .withIsSequential(true)
                            .withItems(new Expression().withValue("@pipeline().parameters.OutputBlobNameList"))
                            .withActivities(
                                Arrays
                                    .asList(
                                        new CopyActivity()
                                            .withName("ExampleCopyActivity")
                                            .withInputs(
                                                Arrays
                                                    .asList(
                                                        new DatasetReference()
                                                            .withReferenceName("exampleDataset")
                                                            .withParameters(
                                                                mapOf(
                                                                    "MyFileName",
                                                                    "examplecontainer.csv",
                                                                    "MyFolderPath",
                                                                    "examplecontainer"))))
                                            .withOutputs(
                                                Arrays
                                                    .asList(
                                                        new DatasetReference()
                                                            .withReferenceName("exampleDataset")
                                                            .withParameters(
                                                                mapOf(
                                                                    "MyFileName",
                                                                    SerializerFactory
                                                                        .createDefaultManagementSerializerAdapter()
                                                                        .deserialize(
                                                                            "{\"type\":\"Expression\",\"value\":\"@item()\"}",
                                                                            Object.class,
                                                                            SerializerEncoding.JSON),
                                                                    "MyFolderPath",
                                                                    "examplecontainer"))))
                                            .withSource(new BlobSource())
                                            .withSink(new BlobSink())
                                            .withDataIntegrationUnits(32)))))
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
import com.azure.core.util.serializer.SerializerEncoding;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** Samples for Pipelines CreateRun. */
public final class PipelinesCreateRunSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/Pipelines_CreateRun.json
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
                com.azure.core.util.Context.NONE);
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
/** Samples for Pipelines Delete. */
public final class PipelinesDeleteSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/Pipelines_Delete.json
     */
    /**
     * Sample code: Pipelines_Delete.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void pipelinesDelete(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .pipelines()
            .deleteWithResponse(
                "exampleResourceGroup", "exampleFactoryName", "examplePipeline", com.azure.core.util.Context.NONE);
    }
}
```

### Pipelines_Get

```java
/** Samples for Pipelines Get. */
public final class PipelinesGetSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/Pipelines_Get.json
     */
    /**
     * Sample code: Pipelines_Get.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void pipelinesGet(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .pipelines()
            .getWithResponse(
                "exampleResourceGroup",
                "exampleFactoryName",
                "examplePipeline",
                null,
                com.azure.core.util.Context.NONE);
    }
}
```

### Pipelines_ListByFactory

```java
/** Samples for Pipelines ListByFactory. */
public final class PipelinesListByFactorySamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/Pipelines_ListByFactory.json
     */
    /**
     * Sample code: Pipelines_ListByFactory.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void pipelinesListByFactory(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .pipelines()
            .listByFactory("exampleResourceGroup", "exampleFactoryName", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndPointConnections_ListByFactory

```java
/** Samples for PrivateEndPointConnections ListByFactory. */
public final class PrivateEndPointConnectionsListByFactorySamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/PrivateEndPointConnections_ListByFactory.json
     */
    /**
     * Sample code: privateEndPointConnections_ListByFactory.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void privateEndPointConnectionsListByFactory(
        com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .privateEndPointConnections()
            .listByFactory("exampleResourceGroup", "exampleFactoryName", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnectionOperation_CreateOrUpdate

```java
import com.azure.resourcemanager.datafactory.models.PrivateEndpoint;
import com.azure.resourcemanager.datafactory.models.PrivateLinkConnectionApprovalRequest;
import com.azure.resourcemanager.datafactory.models.PrivateLinkConnectionState;

/** Samples for PrivateEndpointConnectionOperation CreateOrUpdate. */
public final class PrivateEndpointConnectionOperationCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/ApproveRejectPrivateEndpointConnection.json
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
                            .withActionsRequired(""))
                    .withPrivateEndpoint(
                        new PrivateEndpoint()
                            .withId(
                                "/subscriptions/12345678-1234-1234-1234-12345678abc/resourceGroups/exampleResourceGroup/providers/Microsoft.DataFactory/factories/exampleFactoryName/privateEndpoints/myPrivateEndpoint")))
            .create();
    }
}
```

### PrivateEndpointConnectionOperation_Delete

```java
/** Samples for PrivateEndpointConnectionOperation Delete. */
public final class PrivateEndpointConnectionOperationDeleteSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/DeletePrivateEndpointConnection.json
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
            .deleteWithResponse(
                "exampleResourceGroup", "exampleFactoryName", "connection", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnectionOperation_Get

```java
/** Samples for PrivateEndpointConnectionOperation Get. */
public final class PrivateEndpointConnectionOperationGetSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/GetPrivateEndpointConnection.json
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
            .getWithResponse(
                "exampleResourceGroup", "exampleFactoryName", "connection", null, com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkResources_Get

```java
/** Samples for PrivateLinkResources Get. */
public final class PrivateLinkResourcesGetSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/GetPrivateLinkResources.json
     */
    /**
     * Sample code: Get private link resources of a site.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void getPrivateLinkResourcesOfASite(
        com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .privateLinkResources()
            .getWithResponse("exampleResourceGroup", "exampleFactoryName", com.azure.core.util.Context.NONE);
    }
}
```

### TriggerRuns_Cancel

```java
/** Samples for TriggerRuns Cancel. */
public final class TriggerRunsCancelSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/TriggerRuns_Cancel.json
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
                com.azure.core.util.Context.NONE);
    }
}
```

### TriggerRuns_QueryByFactory

```java
import com.azure.resourcemanager.datafactory.models.RunFilterParameters;
import com.azure.resourcemanager.datafactory.models.RunQueryFilter;
import com.azure.resourcemanager.datafactory.models.RunQueryFilterOperand;
import com.azure.resourcemanager.datafactory.models.RunQueryFilterOperator;
import java.time.OffsetDateTime;
import java.util.Arrays;

/** Samples for TriggerRuns QueryByFactory. */
public final class TriggerRunsQueryByFactorySamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/TriggerRuns_QueryByFactory.json
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
                com.azure.core.util.Context.NONE);
    }
}
```

### TriggerRuns_Rerun

```java
/** Samples for TriggerRuns Rerun. */
public final class TriggerRunsRerunSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/TriggerRuns_Rerun.json
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
                com.azure.core.util.Context.NONE);
    }
}
```

### Triggers_CreateOrUpdate

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.datafactory.models.PipelineReference;
import com.azure.resourcemanager.datafactory.models.RecurrenceFrequency;
import com.azure.resourcemanager.datafactory.models.ScheduleTrigger;
import com.azure.resourcemanager.datafactory.models.ScheduleTriggerRecurrence;
import com.azure.resourcemanager.datafactory.models.TriggerPipelineReference;
import com.azure.resourcemanager.datafactory.models.TriggerResource;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Triggers CreateOrUpdate. */
public final class TriggersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/Triggers_Create.json
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
                new ScheduleTrigger()
                    .withPipelines(
                        Arrays
                            .asList(
                                new TriggerPipelineReference()
                                    .withPipelineReference(new PipelineReference().withReferenceName("examplePipeline"))
                                    .withParameters(
                                        mapOf(
                                            "OutputBlobNameList",
                                            SerializerFactory
                                                .createDefaultManagementSerializerAdapter()
                                                .deserialize(
                                                    "[\"exampleoutput.csv\"]",
                                                    Object.class,
                                                    SerializerEncoding.JSON)))))
                    .withRecurrence(
                        new ScheduleTriggerRecurrence()
                            .withFrequency(RecurrenceFrequency.MINUTE)
                            .withInterval(4)
                            .withStartTime(OffsetDateTime.parse("2018-06-16T00:39:13.8441801Z"))
                            .withEndTime(OffsetDateTime.parse("2018-06-16T00:55:13.8441801Z"))
                            .withTimeZone("UTC")
                            .withAdditionalProperties(mapOf())))
            .create();
    }

    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/Triggers_Update.json
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
                .getWithResponse(
                    "exampleResourceGroup",
                    "exampleFactoryName",
                    "exampleTrigger",
                    null,
                    com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withProperties(
                new ScheduleTrigger()
                    .withDescription("Example description")
                    .withPipelines(
                        Arrays
                            .asList(
                                new TriggerPipelineReference()
                                    .withPipelineReference(new PipelineReference().withReferenceName("examplePipeline"))
                                    .withParameters(
                                        mapOf(
                                            "OutputBlobNameList",
                                            SerializerFactory
                                                .createDefaultManagementSerializerAdapter()
                                                .deserialize(
                                                    "[\"exampleoutput.csv\"]",
                                                    Object.class,
                                                    SerializerEncoding.JSON)))))
                    .withRecurrence(
                        new ScheduleTriggerRecurrence()
                            .withFrequency(RecurrenceFrequency.MINUTE)
                            .withInterval(4)
                            .withStartTime(OffsetDateTime.parse("2018-06-16T00:39:14.905167Z"))
                            .withEndTime(OffsetDateTime.parse("2018-06-16T00:55:14.905167Z"))
                            .withTimeZone("UTC")
                            .withAdditionalProperties(mapOf())))
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
/** Samples for Triggers Delete. */
public final class TriggersDeleteSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/Triggers_Delete.json
     */
    /**
     * Sample code: Triggers_Delete.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void triggersDelete(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .triggers()
            .deleteWithResponse(
                "exampleResourceGroup", "exampleFactoryName", "exampleTrigger", com.azure.core.util.Context.NONE);
    }
}
```

### Triggers_Get

```java
/** Samples for Triggers Get. */
public final class TriggersGetSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/Triggers_Get.json
     */
    /**
     * Sample code: Triggers_Get.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void triggersGet(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .triggers()
            .getWithResponse(
                "exampleResourceGroup", "exampleFactoryName", "exampleTrigger", null, com.azure.core.util.Context.NONE);
    }
}
```

### Triggers_GetEventSubscriptionStatus

```java
/** Samples for Triggers GetEventSubscriptionStatus. */
public final class TriggersGetEventSubscriptionStatusSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/Triggers_GetEventSubscriptionStatus.json
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
                "exampleResourceGroup", "exampleFactoryName", "exampleTrigger", com.azure.core.util.Context.NONE);
    }
}
```

### Triggers_ListByFactory

```java
/** Samples for Triggers ListByFactory. */
public final class TriggersListByFactorySamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/Triggers_ListByFactory.json
     */
    /**
     * Sample code: Triggers_ListByFactory.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void triggersListByFactory(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .triggers()
            .listByFactory("exampleResourceGroup", "exampleFactoryName", com.azure.core.util.Context.NONE);
    }
}
```

### Triggers_QueryByFactory

```java
import com.azure.resourcemanager.datafactory.models.TriggerFilterParameters;

/** Samples for Triggers QueryByFactory. */
public final class TriggersQueryByFactorySamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/Triggers_QueryByFactory.json
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
                com.azure.core.util.Context.NONE);
    }
}
```

### Triggers_Start

```java
/** Samples for Triggers Start. */
public final class TriggersStartSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/Triggers_Start.json
     */
    /**
     * Sample code: Triggers_Start.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void triggersStart(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .triggers()
            .start("exampleResourceGroup", "exampleFactoryName", "exampleTrigger", com.azure.core.util.Context.NONE);
    }
}
```

### Triggers_Stop

```java
/** Samples for Triggers Stop. */
public final class TriggersStopSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/Triggers_Stop.json
     */
    /**
     * Sample code: Triggers_Stop.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void triggersStop(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .triggers()
            .stop("exampleResourceGroup", "exampleFactoryName", "exampleTrigger", com.azure.core.util.Context.NONE);
    }
}
```

### Triggers_SubscribeToEvents

```java
/** Samples for Triggers SubscribeToEvents. */
public final class TriggersSubscribeToEventsSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/Triggers_SubscribeToEvents.json
     */
    /**
     * Sample code: Triggers_SubscribeToEvents.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void triggersSubscribeToEvents(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .triggers()
            .subscribeToEvents(
                "exampleResourceGroup", "exampleFactoryName", "exampleTrigger", com.azure.core.util.Context.NONE);
    }
}
```

### Triggers_UnsubscribeFromEvents

```java
/** Samples for Triggers UnsubscribeFromEvents. */
public final class TriggersUnsubscribeFromEventsSamples {
    /*
     * x-ms-original-file: specification/datafactory/resource-manager/Microsoft.DataFactory/stable/2018-06-01/examples/Triggers_UnsubscribeFromEvents.json
     */
    /**
     * Sample code: Triggers_UnsubscribeFromEvents.
     *
     * @param manager Entry point to DataFactoryManager.
     */
    public static void triggersUnsubscribeFromEvents(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager
            .triggers()
            .unsubscribeFromEvents(
                "exampleResourceGroup", "exampleFactoryName", "exampleTrigger", com.azure.core.util.Context.NONE);
    }
}
```

