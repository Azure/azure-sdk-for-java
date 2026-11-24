# Code snippets and samples


## ActivityRuns

- [QueryByPipelineRun](#activityruns_querybypipelinerun)

## ChangeDataCapture

- [CreateOrUpdate](#changedatacapture_createorupdate)
- [Delete](#changedatacapture_delete)
- [Get](#changedatacapture_get)
- [ListByFactory](#changedatacapture_listbyfactory)
- [Start](#changedatacapture_start)
- [Status](#changedatacapture_status)
- [Stop](#changedatacapture_stop)

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

## IntegrationRuntimeOperation

- [DisableInteractiveQuery](#integrationruntimeoperation_disableinteractivequery)
- [EnableInteractiveQuery](#integrationruntimeoperation_enableinteractivequery)

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

/**
 * Samples for ActivityRuns QueryByPipelineRun.
 */
public final class ActivityRunsQueryByPipelineRunSamples {
    /*
     * x-ms-original-file: 2018-06-01/ActivityRuns_QueryByPipelineRun.json
     */
    /**
     * Sample code: ActivityRuns_QueryByPipelineRun.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void
        activityRunsQueryByPipelineRun(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.activityRuns()
            .queryByPipelineRunWithResponse("exampleResourceGroup", "exampleFactoryName",
                "2f7fdb90-5df1-4b8e-ac2f-064cfa58202b",
                new RunFilterParameters().withLastUpdatedAfter(OffsetDateTime.parse("2018-06-16T00:36:44.3345758Z"))
                    .withLastUpdatedBefore(OffsetDateTime.parse("2018-06-16T00:49:48.3686473Z")),
                com.azure.core.util.Context.NONE);
    }
}
```

### ChangeDataCapture_CreateOrUpdate

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.datafactory.models.ChangeDataCaptureResource;
import com.azure.resourcemanager.datafactory.models.ConnectionType;
import com.azure.resourcemanager.datafactory.models.DataMapperMapping;
import com.azure.resourcemanager.datafactory.models.FrequencyType;
import com.azure.resourcemanager.datafactory.models.LinkedServiceReference;
import com.azure.resourcemanager.datafactory.models.MapperAttributeMapping;
import com.azure.resourcemanager.datafactory.models.MapperAttributeMappings;
import com.azure.resourcemanager.datafactory.models.MapperAttributeReference;
import com.azure.resourcemanager.datafactory.models.MapperConnection;
import com.azure.resourcemanager.datafactory.models.MapperConnectionReference;
import com.azure.resourcemanager.datafactory.models.MapperDslConnectorProperties;
import com.azure.resourcemanager.datafactory.models.MapperPolicy;
import com.azure.resourcemanager.datafactory.models.MapperPolicyRecurrence;
import com.azure.resourcemanager.datafactory.models.MapperSourceConnectionsInfo;
import com.azure.resourcemanager.datafactory.models.MapperTable;
import com.azure.resourcemanager.datafactory.models.MapperTableSchema;
import com.azure.resourcemanager.datafactory.models.MapperTargetConnectionsInfo;
import com.azure.resourcemanager.datafactory.models.MappingType;
import com.azure.resourcemanager.datafactory.models.Type;
import java.io.IOException;
import java.util.Arrays;

/**
 * Samples for ChangeDataCapture CreateOrUpdate.
 */
public final class ChangeDataCaptureCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2018-06-01/ChangeDataCapture_Create.json
     */
    /**
     * Sample code: ChangeDataCapture_Create.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void changeDataCaptureCreate(com.azure.resourcemanager.datafactory.DataFactoryManager manager)
        throws IOException {
        manager.changeDataCaptures()
            .define("exampleChangeDataCapture")
            .withExistingFactory("exampleResourceGroup", "exampleFactoryName")
            .withSourceConnectionsInfo(
                Arrays
                    .asList(
                        new MapperSourceConnectionsInfo()
                            .withSourceEntities(
                                Arrays
                                    .asList(
                                        new MapperTable().withName("source/customer")
                                            .withSchema(Arrays.asList(
                                                new MapperTableSchema().withName("CustId").withDataType("short"),
                                                new MapperTableSchema().withName("CustName").withDataType("string"),
                                                new MapperTableSchema().withName("CustAddres").withDataType("string"),
                                                new MapperTableSchema().withName("CustDepName")
                                                    .withDataType("string"),
                                                new MapperTableSchema().withName("CustDepLoc").withDataType("string")))
                                            .withDslConnectorProperties(Arrays.asList(
                                                new MapperDslConnectorProperties().withName("container")
                                                    .withValue("source"),
                                                new MapperDslConnectorProperties().withName("fileSystem")
                                                    .withValue("source"),
                                                new MapperDslConnectorProperties().withName("folderPath")
                                                    .withValue("customer"),
                                                new MapperDslConnectorProperties().withName("allowSchemaDrift")
                                                    .withValue(false),
                                                new MapperDslConnectorProperties()
                                                    .withName("inferDriftedColumnTypes")
                                                    .withValue(false))),
                                        new MapperTable()
                                            .withName("source/employee")
                                            .withSchema(Arrays.asList())
                                            .withDslConnectorProperties(
                                                Arrays.asList(
                                                    new MapperDslConnectorProperties().withName("container")
                                                        .withValue("source"),
                                                    new MapperDslConnectorProperties().withName("fileSystem")
                                                        .withValue("source"),
                                                    new MapperDslConnectorProperties()
                                                        .withName("folderPath")
                                                        .withValue("employee"))),
                                        new MapperTable().withName("lookup")
                                            .withSchema(Arrays.asList(
                                                new MapperTableSchema().withName("EmpId").withDataType("short"),
                                                new MapperTableSchema().withName("EmpName").withDataType("string"),
                                                new MapperTableSchema().withName("HomeAddress").withDataType("string"),
                                                new MapperTableSchema()
                                                    .withName("OfficeAddress")
                                                    .withDataType("string"),
                                                new MapperTableSchema().withName("EmpPhoneNumber")
                                                    .withDataType("integer"),
                                                new MapperTableSchema().withName("DepName").withDataType("string"),
                                                new MapperTableSchema().withName("DepLoc")
                                                    .withDataType("string"),
                                                new MapperTableSchema().withName("DecimalCol").withDataType("double")))
                                            .withDslConnectorProperties(Arrays.asList(
                                                new MapperDslConnectorProperties().withName("container")
                                                    .withValue("lookup"),
                                                new MapperDslConnectorProperties().withName("fileSystem")
                                                    .withValue("lookup"),
                                                new MapperDslConnectorProperties().withName("allowSchemaDrift")
                                                    .withValue(false),
                                                new MapperDslConnectorProperties()
                                                    .withName("inferDriftedColumnTypes")
                                                    .withValue(false))),
                                        new MapperTable().withName("source/justSchema")
                                            .withSchema(Arrays.asList(
                                                new MapperTableSchema().withName("CustId").withDataType("string"),
                                                new MapperTableSchema().withName("CustName").withDataType("string"),
                                                new MapperTableSchema().withName("CustAddres").withDataType("string"),
                                                new MapperTableSchema().withName("CustDepName").withDataType("string"),
                                                new MapperTableSchema().withName("CustDepLoc").withDataType("string")))
                                            .withDslConnectorProperties(Arrays.asList(
                                                new MapperDslConnectorProperties().withName("container")
                                                    .withValue("source"),
                                                new MapperDslConnectorProperties().withName("fileSystem")
                                                    .withValue("source"),
                                                new MapperDslConnectorProperties().withName("folderPath")
                                                    .withValue("justSchema"),
                                                new MapperDslConnectorProperties().withName("allowSchemaDrift")
                                                    .withValue(false),
                                                new MapperDslConnectorProperties().withName("inferDriftedColumnTypes")
                                                    .withValue(false)))))
                            .withConnection(new MapperConnection()
                                .withLinkedService(new LinkedServiceReference().withType(Type.LINKED_SERVICE_REFERENCE)
                                    .withReferenceName("amjaAdls03"))
                                .withLinkedServiceType("AzureBlobFS")
                                .withType(ConnectionType.LINKEDSERVICETYPE)
                                .withIsInlineDataset(true)
                                .withCommonDslConnectorProperties(Arrays.asList(new MapperDslConnectorProperties()
                                    .withName("allowSchemaDrift")
                                    .withValue(true),
                                    new MapperDslConnectorProperties()
                                        .withName("inferDriftedColumnTypes")
                                        .withValue(true),
                                    new MapperDslConnectorProperties()
                                        .withName("format")
                                        .withValue("delimited"),
                                    new MapperDslConnectorProperties().withName("dateFormats")
                                        .withValue(SerializerFactory
                                            .createDefaultManagementSerializerAdapter()
                                            .deserialize(
                                                "[\"MM/dd/yyyy\",\"dd/MM/yyyy\",\"yyyy/MM/dd\",\"MM-dd-yyyy\",\"dd-MM-yyyy\",\"yyyy-MM-dd\",\"dd.MM.yyyy\",\"MM.dd.yyyy\",\"yyyy.MM.dd\"]",
                                                Object.class, SerializerEncoding.JSON)),
                                    new MapperDslConnectorProperties().withName("timestampFormats")
                                        .withValue(SerializerFactory.createDefaultManagementSerializerAdapter()
                                            .deserialize(
                                                "[\"yyyyMMddHHmm\",\"yyyyMMdd HHmm\",\"yyyyMMddHHmmss\",\"yyyyMMdd HHmmss\",\"dd-MM-yyyy HH:mm:ss\",\"dd-MM-yyyy HH:mm\",\"yyyy-M-d H:m:s\",\"yyyy-MM-dd\\\\'T\\\\'HH:mm:ss\\\\'Z\\\\'\",\"yyyy-M-d\\\\'T\\\\'H:m:s\\\\'Z\\\\'\",\"yyyy-M-d\\\\'T\\\\'H:m:s\",\"yyyy-MM-dd\\\\'T\\\\'HH:mm:ss\",\"yyyy-MM-dd HH:mm:ss\",\"yyyy-MM-dd HH:mm\",\"yyyy.MM.dd HH:mm:ss\",\"MM/dd/yyyy HH:mm:ss\",\"M/d/yyyy H:m:s\",\"yyyy/MM/dd HH:mm:ss\",\"yyyy/M/d H:m:s\",\"dd MMM yyyy HH:mm:ss\",\"dd MMMM yyyy HH:mm:ss\",\"d MMM yyyy H:m:s\",\"d MMMM yyyy H:m:s\",\"d-M-yyyy H:m:s\",\"d-M-yyyy H:m\",\"yyyy-M-d H:m\",\"MM/dd/yyyy HH:mm\",\"M/d/yyyy H:m\",\"yyyy/MM/dd HH:mm\",\"yyyy/M/d H:m\",\"dd MMMM yyyy HH:mm\",\"dd MMM yyyy HH:mm\",\"d MMMM yyyy H:m\",\"d MMM yyyy H:m\",\"MM-dd-yyyy hh:mm:ss a\",\"MM-dd-yyyy HH:mm:ss\",\"MM/dd/yyyy hh:mm:ss a\",\"yyyy.MM.dd hh:mm:ss a\",\"MM/dd/yyyy\",\"dd/MM/yyyy\",\"yyyy/MM/dd\",\"MM-dd-yyyy\",\"dd-MM-yyyy\",\"yyyy-MM-dd\",\"dd.MM.yyyy\",\"MM.dd.yyyy\",\"yyyy.MM.dd\"]",
                                                Object.class, SerializerEncoding.JSON)),
                                    new MapperDslConnectorProperties().withName("enableCdc").withValue(true),
                                    new MapperDslConnectorProperties().withName("skipInitialLoad").withValue(true),
                                    new MapperDslConnectorProperties().withName("columnNamesAsHeader").withValue(true),
                                    new MapperDslConnectorProperties().withName("columnDelimiter").withValue(","),
                                    new MapperDslConnectorProperties().withName("escapeChar").withValue("\\\\"),
                                    new MapperDslConnectorProperties().withName("quoteChar").withValue("\\\""))))))
            .withTargetConnectionsInfo(
                Arrays
                    .asList(
                        new MapperTargetConnectionsInfo()
                            .withTargetEntities(Arrays.asList(
                                new MapperTable().withName("dbo.employee")
                                    .withSchema(Arrays.asList())
                                    .withDslConnectorProperties(Arrays.asList(
                                        new MapperDslConnectorProperties().withName("schemaName").withValue("dbo"),
                                        new MapperDslConnectorProperties().withName("tableName")
                                            .withValue("employee"))),
                                new MapperTable().withName("dbo.justSchema")
                                    .withSchema(Arrays.asList())
                                    .withDslConnectorProperties(Arrays.asList(
                                        new MapperDslConnectorProperties().withName("schemaName").withValue("dbo"),
                                        new MapperDslConnectorProperties().withName("tableName")
                                            .withValue("justSchema"),
                                        new MapperDslConnectorProperties().withName("allowSchemaDrift").withValue(true),
                                        new MapperDslConnectorProperties()
                                            .withName("inferDriftedColumnTypes")
                                            .withValue(true))),
                                new MapperTable().withName("dbo.customer")
                                    .withSchema(Arrays.asList(
                                        new MapperTableSchema().withName("CustId").withDataType("integer"),
                                        new MapperTableSchema().withName("CustName").withDataType("string"),
                                        new MapperTableSchema().withName("CustAddres").withDataType("string"),
                                        new MapperTableSchema().withName("CustDeptName").withDataType("string"),
                                        new MapperTableSchema().withName("CustEmail").withDataType("string")))
                                    .withDslConnectorProperties(Arrays.asList(
                                        new MapperDslConnectorProperties().withName("schemaName").withValue("dbo"),
                                        new MapperDslConnectorProperties().withName("tableName").withValue("customer"),
                                        new MapperDslConnectorProperties().withName("allowSchemaDrift")
                                            .withValue(false),
                                        new MapperDslConnectorProperties()
                                            .withName("inferDriftedColumnTypes")
                                            .withValue(false))),
                                new MapperTable().withName("dbo.data_source_table")
                                    .withSchema(Arrays.asList(
                                        new MapperTableSchema().withName("PersonID").withDataType("integer"),
                                        new MapperTableSchema().withName("Name")
                                            .withDataType("string"),
                                        new MapperTableSchema().withName("LastModifytime").withDataType("timestamp")))
                                    .withDslConnectorProperties(Arrays.asList(
                                        new MapperDslConnectorProperties().withName("schemaName").withValue("dbo"),
                                        new MapperDslConnectorProperties()
                                            .withName("tableName")
                                            .withValue("data_source_table"),
                                        new MapperDslConnectorProperties().withName("allowSchemaDrift")
                                            .withValue(false),
                                        new MapperDslConnectorProperties()
                                            .withName("inferDriftedColumnTypes")
                                            .withValue(false),
                                        new MapperDslConnectorProperties().withName("defaultToUpsert")
                                            .withValue(false)))))
                            .withConnection(new MapperConnection()
                                .withLinkedService(new LinkedServiceReference().withType(Type.LINKED_SERVICE_REFERENCE)
                                    .withReferenceName("amjaSql"))
                                .withLinkedServiceType("AzureSqlDatabase")
                                .withType(ConnectionType.LINKEDSERVICETYPE)
                                .withIsInlineDataset(true)
                                .withCommonDslConnectorProperties(Arrays.asList(
                                    new MapperDslConnectorProperties().withName("allowSchemaDrift").withValue(true),
                                    new MapperDslConnectorProperties()
                                        .withName("inferDriftedColumnTypes")
                                        .withValue(true),
                                    new MapperDslConnectorProperties().withName("format").withValue("table"),
                                    new MapperDslConnectorProperties().withName("store").withValue("sqlserver"),
                                    new MapperDslConnectorProperties()
                                        .withName("databaseType")
                                        .withValue("databaseType"),
                                    new MapperDslConnectorProperties().withName("database").withValue("database"),
                                    new MapperDslConnectorProperties().withName("deletable").withValue(false),
                                    new MapperDslConnectorProperties().withName("insertable").withValue(true),
                                    new MapperDslConnectorProperties().withName("updateable").withValue(false),
                                    new MapperDslConnectorProperties().withName("upsertable").withValue(false),
                                    new MapperDslConnectorProperties().withName("skipDuplicateMapInputs")
                                        .withValue(true),
                                    new MapperDslConnectorProperties()
                                        .withName("skipDuplicateMapOutputs")
                                        .withValue(true))))
                            .withDataMapperMappings(Arrays.asList(
                                new DataMapperMapping().withTargetEntityName("dbo.customer")
                                    .withSourceEntityName("source/customer")
                                    .withSourceConnectionReference(new MapperConnectionReference()
                                        .withConnectionName("amjaAdls03")
                                        .withType(ConnectionType.LINKEDSERVICETYPE))
                                    .withAttributeMappingInfo(
                                        new MapperAttributeMappings().withAttributeMappings(Arrays.asList())),
                                new DataMapperMapping().withTargetEntityName("dbo.data_source_table")
                                    .withSourceEntityName("lookup")
                                    .withSourceConnectionReference(
                                        new MapperConnectionReference().withConnectionName("amjaAdls03")
                                            .withType(ConnectionType.LINKEDSERVICETYPE))
                                    .withAttributeMappingInfo(
                                        new MapperAttributeMappings().withAttributeMappings(Arrays.asList(
                                            new MapperAttributeMapping().withName("Name")
                                                .withType(MappingType.DERIVED)
                                                .withFunctionName("upper")
                                                .withExpression("upper(EmpName)")
                                                .withAttributeReferences(
                                                    Arrays.asList(new MapperAttributeReference().withName("EmpName")
                                                        .withEntity("lookup")
                                                        .withEntityConnectionReference(new MapperConnectionReference()
                                                            .withConnectionName("amjaAdls03")
                                                            .withType(ConnectionType.LINKEDSERVICETYPE)))),
                                            new MapperAttributeMapping().withName("PersonID")
                                                .withType(MappingType.DIRECT)
                                                .withFunctionName("")
                                                .withAttributeReference(
                                                    new MapperAttributeReference().withName("EmpId")
                                                        .withEntity("lookup")
                                                        .withEntityConnectionReference(new MapperConnectionReference()
                                                            .withConnectionName("amjaAdls03")
                                                            .withType(ConnectionType.LINKEDSERVICETYPE)))))),
                                new DataMapperMapping().withTargetEntityName("dbo.employee")
                                    .withSourceEntityName("source/employee")
                                    .withSourceConnectionReference(
                                        new MapperConnectionReference().withConnectionName("amjaAdls03")
                                            .withType(ConnectionType.LINKEDSERVICETYPE))
                                    .withAttributeMappingInfo(
                                        new MapperAttributeMappings().withAttributeMappings(Arrays.asList())),
                                new DataMapperMapping().withTargetEntityName("dbo.justSchema")
                                    .withSourceEntityName("source/justSchema")
                                    .withSourceConnectionReference(
                                        new MapperConnectionReference().withConnectionName("amjaAdls03")
                                            .withType(ConnectionType.LINKEDSERVICETYPE))
                                    .withAttributeMappingInfo(
                                        new MapperAttributeMappings().withAttributeMappings(Arrays.asList(
                                            new MapperAttributeMapping().withName("CustAddres")
                                                .withType(MappingType.DERIVED)
                                                .withFunctionName("trim")
                                                .withExpression("trim(CustAddres)")
                                                .withAttributeReferences(
                                                    Arrays.asList(new MapperAttributeReference().withName("CustAddres")
                                                        .withEntity("source/justSchema")
                                                        .withEntityConnectionReference(new MapperConnectionReference()
                                                            .withConnectionName("amjaAdls03")
                                                            .withType(ConnectionType.LINKEDSERVICETYPE)))),
                                            new MapperAttributeMapping().withName("CustDepLoc")
                                                .withType(MappingType.DIRECT)
                                                .withAttributeReference(
                                                    new MapperAttributeReference()
                                                        .withName("CustDepLoc")
                                                        .withEntity("source/justSchema")
                                                        .withEntityConnectionReference(
                                                            new MapperConnectionReference()
                                                                .withConnectionName("amjaAdls03")
                                                                .withType(ConnectionType.LINKEDSERVICETYPE))),
                                            new MapperAttributeMapping().withName("CustDepName")
                                                .withType(MappingType.DERIVED)
                                                .withFunctionName("")
                                                .withExpression("concat(CustName, \" -> \", CustDepName)")
                                                .withAttributeReferences(Arrays.asList(
                                                    new MapperAttributeReference().withName("CustName")
                                                        .withEntity("source/justSchema")
                                                        .withEntityConnectionReference(
                                                            new MapperConnectionReference()
                                                                .withConnectionName("amjaAdls03")
                                                                .withType(ConnectionType.LINKEDSERVICETYPE)),
                                                    new MapperAttributeReference().withName("CustDepName")
                                                        .withEntity("source/justSchema")
                                                        .withEntityConnectionReference(new MapperConnectionReference()
                                                            .withConnectionName("amjaAdls03")
                                                            .withType(ConnectionType.LINKEDSERVICETYPE)))),
                                            new MapperAttributeMapping().withName("CustId")
                                                .withType(MappingType.DIRECT)
                                                .withFunctionName("")
                                                .withAttributeReference(
                                                    new MapperAttributeReference().withName("CustId")
                                                        .withEntity("source/justSchema")
                                                        .withEntityConnectionReference(new MapperConnectionReference()
                                                            .withConnectionName("amjaAdls03")
                                                            .withType(ConnectionType.LINKEDSERVICETYPE))),
                                            new MapperAttributeMapping().withName("CustName")
                                                .withType(MappingType.DIRECT)
                                                .withAttributeReference(
                                                    new MapperAttributeReference().withName("CustName")
                                                        .withEntity("source/justSchema")
                                                        .withEntityConnectionReference(new MapperConnectionReference()
                                                            .withConnectionName("amjaAdls03")
                                                            .withType(ConnectionType.LINKEDSERVICETYPE))))))))
                            .withRelationships(Arrays.asList())))
            .withPolicy(new MapperPolicy().withMode("Microbatch")
                .withRecurrence(new MapperPolicyRecurrence().withFrequency(FrequencyType.MINUTE).withInterval(15)))
            .withDescription(
                "Sample demo change data capture to transfer data from delimited (csv) to Azure SQL Database with automapped and non-automapped mappings.")
            .withAllowVNetOverride(false)
            .create();
    }

    /*
     * x-ms-original-file: 2018-06-01/ChangeDataCapture_Update.json
     */
    /**
     * Sample code: ChangeDataCapture_Update.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void changeDataCaptureUpdate(com.azure.resourcemanager.datafactory.DataFactoryManager manager)
        throws IOException {
        ChangeDataCaptureResource resource = manager.changeDataCaptures()
            .getWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleChangeDataCapture", null,
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withDescription(
                "Sample demo change data capture to transfer data from delimited (csv) to Azure SQL Database. Updating table mappings.")
            .withSourceConnectionsInfo(
                Arrays
                    .asList(
                        new MapperSourceConnectionsInfo()
                            .withSourceEntities(
                                Arrays
                                    .asList(
                                        new MapperTable().withName("source/customer")
                                            .withSchema(Arrays.asList(
                                                new MapperTableSchema().withName("CustId").withDataType("short"),
                                                new MapperTableSchema().withName("CustName").withDataType("string"),
                                                new MapperTableSchema().withName("CustAddres").withDataType("string"),
                                                new MapperTableSchema().withName("CustDepName")
                                                    .withDataType("string"),
                                                new MapperTableSchema().withName("CustDepLoc").withDataType("string")))
                                            .withDslConnectorProperties(Arrays.asList(
                                                new MapperDslConnectorProperties().withName("container")
                                                    .withValue("source"),
                                                new MapperDslConnectorProperties().withName("fileSystem")
                                                    .withValue("source"),
                                                new MapperDslConnectorProperties().withName("folderPath")
                                                    .withValue("customer"),
                                                new MapperDslConnectorProperties().withName("allowSchemaDrift")
                                                    .withValue(false),
                                                new MapperDslConnectorProperties()
                                                    .withName("inferDriftedColumnTypes")
                                                    .withValue(false))),
                                        new MapperTable()
                                            .withName("source/employee")
                                            .withSchema(Arrays.asList())
                                            .withDslConnectorProperties(
                                                Arrays.asList(
                                                    new MapperDslConnectorProperties().withName("container")
                                                        .withValue("source"),
                                                    new MapperDslConnectorProperties().withName("fileSystem")
                                                        .withValue("source"),
                                                    new MapperDslConnectorProperties()
                                                        .withName("folderPath")
                                                        .withValue("employee"))),
                                        new MapperTable().withName("lookup")
                                            .withSchema(Arrays.asList(
                                                new MapperTableSchema().withName("EmpId").withDataType("short"),
                                                new MapperTableSchema().withName("EmpName").withDataType("string"),
                                                new MapperTableSchema().withName("HomeAddress").withDataType("string"),
                                                new MapperTableSchema()
                                                    .withName("OfficeAddress")
                                                    .withDataType("string"),
                                                new MapperTableSchema().withName("EmpPhoneNumber")
                                                    .withDataType("integer"),
                                                new MapperTableSchema().withName("DepName").withDataType("string"),
                                                new MapperTableSchema().withName("DepLoc")
                                                    .withDataType("string"),
                                                new MapperTableSchema().withName("DecimalCol").withDataType("double")))
                                            .withDslConnectorProperties(Arrays.asList(
                                                new MapperDslConnectorProperties().withName("container")
                                                    .withValue("lookup"),
                                                new MapperDslConnectorProperties().withName("fileSystem")
                                                    .withValue("lookup"),
                                                new MapperDslConnectorProperties().withName("allowSchemaDrift")
                                                    .withValue(false),
                                                new MapperDslConnectorProperties()
                                                    .withName("inferDriftedColumnTypes")
                                                    .withValue(false))),
                                        new MapperTable().withName("source/justSchema")
                                            .withSchema(Arrays.asList(
                                                new MapperTableSchema().withName("CustId").withDataType("string"),
                                                new MapperTableSchema().withName("CustName").withDataType("string"),
                                                new MapperTableSchema().withName("CustAddres").withDataType("string"),
                                                new MapperTableSchema().withName("CustDepName").withDataType("string"),
                                                new MapperTableSchema().withName("CustDepLoc").withDataType("string")))
                                            .withDslConnectorProperties(Arrays.asList(
                                                new MapperDslConnectorProperties().withName("container")
                                                    .withValue("source"),
                                                new MapperDslConnectorProperties().withName("fileSystem")
                                                    .withValue("source"),
                                                new MapperDslConnectorProperties().withName("folderPath")
                                                    .withValue("justSchema"),
                                                new MapperDslConnectorProperties().withName("allowSchemaDrift")
                                                    .withValue(false),
                                                new MapperDslConnectorProperties().withName("inferDriftedColumnTypes")
                                                    .withValue(false)))))
                            .withConnection(new MapperConnection()
                                .withLinkedService(new LinkedServiceReference().withType(Type.LINKED_SERVICE_REFERENCE)
                                    .withReferenceName("amjaAdls03"))
                                .withLinkedServiceType("AzureBlobFS")
                                .withType(ConnectionType.LINKEDSERVICETYPE)
                                .withIsInlineDataset(true)
                                .withCommonDslConnectorProperties(Arrays.asList(new MapperDslConnectorProperties()
                                    .withName("allowSchemaDrift")
                                    .withValue(true),
                                    new MapperDslConnectorProperties()
                                        .withName("inferDriftedColumnTypes")
                                        .withValue(true),
                                    new MapperDslConnectorProperties()
                                        .withName("format")
                                        .withValue("delimited"),
                                    new MapperDslConnectorProperties().withName("dateFormats")
                                        .withValue(SerializerFactory
                                            .createDefaultManagementSerializerAdapter()
                                            .deserialize(
                                                "[\"MM/dd/yyyy\",\"dd/MM/yyyy\",\"yyyy/MM/dd\",\"MM-dd-yyyy\",\"dd-MM-yyyy\",\"yyyy-MM-dd\",\"dd.MM.yyyy\",\"MM.dd.yyyy\",\"yyyy.MM.dd\"]",
                                                Object.class, SerializerEncoding.JSON)),
                                    new MapperDslConnectorProperties().withName("timestampFormats")
                                        .withValue(SerializerFactory.createDefaultManagementSerializerAdapter()
                                            .deserialize(
                                                "[\"yyyyMMddHHmm\",\"yyyyMMdd HHmm\",\"yyyyMMddHHmmss\",\"yyyyMMdd HHmmss\",\"dd-MM-yyyy HH:mm:ss\",\"dd-MM-yyyy HH:mm\",\"yyyy-M-d H:m:s\",\"yyyy-MM-dd\\\\'T\\\\'HH:mm:ss\\\\'Z\\\\'\",\"yyyy-M-d\\\\'T\\\\'H:m:s\\\\'Z\\\\'\",\"yyyy-M-d\\\\'T\\\\'H:m:s\",\"yyyy-MM-dd\\\\'T\\\\'HH:mm:ss\",\"yyyy-MM-dd HH:mm:ss\",\"yyyy-MM-dd HH:mm\",\"yyyy.MM.dd HH:mm:ss\",\"MM/dd/yyyy HH:mm:ss\",\"M/d/yyyy H:m:s\",\"yyyy/MM/dd HH:mm:ss\",\"yyyy/M/d H:m:s\",\"dd MMM yyyy HH:mm:ss\",\"dd MMMM yyyy HH:mm:ss\",\"d MMM yyyy H:m:s\",\"d MMMM yyyy H:m:s\",\"d-M-yyyy H:m:s\",\"d-M-yyyy H:m\",\"yyyy-M-d H:m\",\"MM/dd/yyyy HH:mm\",\"M/d/yyyy H:m\",\"yyyy/MM/dd HH:mm\",\"yyyy/M/d H:m\",\"dd MMMM yyyy HH:mm\",\"dd MMM yyyy HH:mm\",\"d MMMM yyyy H:m\",\"d MMM yyyy H:m\",\"MM-dd-yyyy hh:mm:ss a\",\"MM-dd-yyyy HH:mm:ss\",\"MM/dd/yyyy hh:mm:ss a\",\"yyyy.MM.dd hh:mm:ss a\",\"MM/dd/yyyy\",\"dd/MM/yyyy\",\"yyyy/MM/dd\",\"MM-dd-yyyy\",\"dd-MM-yyyy\",\"yyyy-MM-dd\",\"dd.MM.yyyy\",\"MM.dd.yyyy\",\"yyyy.MM.dd\"]",
                                                Object.class, SerializerEncoding.JSON)),
                                    new MapperDslConnectorProperties().withName("enableCdc").withValue(true),
                                    new MapperDslConnectorProperties().withName("skipInitialLoad").withValue(true),
                                    new MapperDslConnectorProperties().withName("columnNamesAsHeader").withValue(true),
                                    new MapperDslConnectorProperties().withName("columnDelimiter").withValue(","),
                                    new MapperDslConnectorProperties().withName("escapeChar").withValue("\\\\"),
                                    new MapperDslConnectorProperties().withName("quoteChar").withValue("\\\""))))))
            .withTargetConnectionsInfo(
                Arrays
                    .asList(
                        new MapperTargetConnectionsInfo()
                            .withTargetEntities(Arrays.asList(
                                new MapperTable().withName("dbo.employee")
                                    .withSchema(Arrays.asList())
                                    .withDslConnectorProperties(Arrays.asList(
                                        new MapperDslConnectorProperties().withName("schemaName").withValue("dbo"),
                                        new MapperDslConnectorProperties().withName("tableName")
                                            .withValue("employee"))),
                                new MapperTable().withName("dbo.justSchema")
                                    .withSchema(Arrays.asList())
                                    .withDslConnectorProperties(Arrays.asList(
                                        new MapperDslConnectorProperties().withName("schemaName").withValue("dbo"),
                                        new MapperDslConnectorProperties().withName("tableName")
                                            .withValue("justSchema"),
                                        new MapperDslConnectorProperties().withName("allowSchemaDrift").withValue(true),
                                        new MapperDslConnectorProperties()
                                            .withName("inferDriftedColumnTypes")
                                            .withValue(true))),
                                new MapperTable().withName("dbo.customer")
                                    .withSchema(Arrays.asList(
                                        new MapperTableSchema().withName("CustId").withDataType("integer"),
                                        new MapperTableSchema().withName("CustName").withDataType("string"),
                                        new MapperTableSchema().withName("CustAddres").withDataType("string"),
                                        new MapperTableSchema().withName("CustDeptName").withDataType("string"),
                                        new MapperTableSchema().withName("CustEmail").withDataType("string")))
                                    .withDslConnectorProperties(Arrays.asList(
                                        new MapperDslConnectorProperties().withName("schemaName").withValue("dbo"),
                                        new MapperDslConnectorProperties().withName("tableName").withValue("customer"),
                                        new MapperDslConnectorProperties().withName("allowSchemaDrift")
                                            .withValue(false),
                                        new MapperDslConnectorProperties()
                                            .withName("inferDriftedColumnTypes")
                                            .withValue(false))),
                                new MapperTable().withName("dbo.data_source_table")
                                    .withSchema(Arrays.asList(
                                        new MapperTableSchema().withName("PersonID").withDataType("integer"),
                                        new MapperTableSchema().withName("Name")
                                            .withDataType("string"),
                                        new MapperTableSchema().withName("LastModifytime").withDataType("timestamp")))
                                    .withDslConnectorProperties(Arrays.asList(
                                        new MapperDslConnectorProperties().withName("schemaName").withValue("dbo"),
                                        new MapperDslConnectorProperties()
                                            .withName("tableName")
                                            .withValue("data_source_table"),
                                        new MapperDslConnectorProperties().withName("allowSchemaDrift")
                                            .withValue(false),
                                        new MapperDslConnectorProperties()
                                            .withName("inferDriftedColumnTypes")
                                            .withValue(false),
                                        new MapperDslConnectorProperties().withName("defaultToUpsert")
                                            .withValue(false)))))
                            .withConnection(new MapperConnection()
                                .withLinkedService(new LinkedServiceReference().withType(Type.LINKED_SERVICE_REFERENCE)
                                    .withReferenceName("amjaSql"))
                                .withLinkedServiceType("AzureSqlDatabase")
                                .withType(ConnectionType.LINKEDSERVICETYPE)
                                .withIsInlineDataset(true)
                                .withCommonDslConnectorProperties(Arrays.asList(
                                    new MapperDslConnectorProperties().withName("allowSchemaDrift").withValue(true),
                                    new MapperDslConnectorProperties().withName("inferDriftedColumnTypes")
                                        .withValue(true),
                                    new MapperDslConnectorProperties().withName("format").withValue("table"),
                                    new MapperDslConnectorProperties().withName("store").withValue("sqlserver"),
                                    new MapperDslConnectorProperties().withName("databaseType")
                                        .withValue("databaseType"),
                                    new MapperDslConnectorProperties().withName("database").withValue("database"),
                                    new MapperDslConnectorProperties().withName("deletable").withValue(false),
                                    new MapperDslConnectorProperties().withName("insertable").withValue(true),
                                    new MapperDslConnectorProperties().withName("updateable").withValue(false),
                                    new MapperDslConnectorProperties().withName("upsertable").withValue(false),
                                    new MapperDslConnectorProperties().withName("skipDuplicateMapInputs")
                                        .withValue(true),
                                    new MapperDslConnectorProperties().withName("skipDuplicateMapOutputs")
                                        .withValue(true))))
                            .withDataMapperMappings(Arrays.asList(
                                new DataMapperMapping().withTargetEntityName("dbo.customer")
                                    .withSourceEntityName("source/customer")
                                    .withSourceConnectionReference(
                                        new MapperConnectionReference().withConnectionName("amjaAdls03")
                                            .withType(ConnectionType.LINKEDSERVICETYPE))
                                    .withAttributeMappingInfo(
                                        new MapperAttributeMappings().withAttributeMappings(Arrays.asList(
                                            new MapperAttributeMapping().withName("CustAddres")
                                                .withType(MappingType.DERIVED)
                                                .withFunctionName("trim")
                                                .withExpression("trim(CustAddres)")
                                                .withAttributeReferences(
                                                    Arrays.asList(new MapperAttributeReference().withName("CustAddres")
                                                        .withEntity("source/customer")
                                                        .withEntityConnectionReference(new MapperConnectionReference()
                                                            .withConnectionName("amjaAdls03")
                                                            .withType(ConnectionType.LINKEDSERVICETYPE)))),
                                            new MapperAttributeMapping().withName("CustDeptName")
                                                .withType(MappingType.DIRECT)
                                                .withFunctionName("")
                                                .withAttributeReference(
                                                    new MapperAttributeReference().withName("CustDepName")
                                                        .withEntity("source/customer")
                                                        .withEntityConnectionReference(new MapperConnectionReference()
                                                            .withConnectionName("amjaAdls03")
                                                            .withType(ConnectionType.LINKEDSERVICETYPE))),
                                            new MapperAttributeMapping().withName("CustEmail")
                                                .withType(MappingType.DIRECT)
                                                .withFunctionName("")
                                                .withAttributeReference(
                                                    new MapperAttributeReference().withName("CustName")
                                                        .withEntity("source/customer")
                                                        .withEntityConnectionReference(new MapperConnectionReference()
                                                            .withConnectionName("amjaAdls03")
                                                            .withType(ConnectionType.LINKEDSERVICETYPE))),
                                            new MapperAttributeMapping().withName("CustId")
                                                .withType(MappingType.DIRECT)
                                                .withFunctionName("")
                                                .withAttributeReference(
                                                    new MapperAttributeReference().withName("CustId")
                                                        .withEntity("source/customer")
                                                        .withEntityConnectionReference(new MapperConnectionReference()
                                                            .withConnectionName("amjaAdls03")
                                                            .withType(ConnectionType.LINKEDSERVICETYPE))),
                                            new MapperAttributeMapping()
                                                .withName("CustName")
                                                .withType(MappingType.DIRECT)
                                                .withFunctionName("")
                                                .withAttributeReference(
                                                    new MapperAttributeReference()
                                                        .withName("CustName")
                                                        .withEntity("source/customer")
                                                        .withEntityConnectionReference(
                                                            new MapperConnectionReference()
                                                                .withConnectionName("amjaAdls03")
                                                                .withType(ConnectionType.LINKEDSERVICETYPE)))))),
                                new DataMapperMapping().withTargetEntityName("dbo.data_source_table")
                                    .withSourceEntityName("lookup")
                                    .withSourceConnectionReference(
                                        new MapperConnectionReference().withConnectionName("amjaAdls03")
                                            .withType(ConnectionType.LINKEDSERVICETYPE))
                                    .withAttributeMappingInfo(
                                        new MapperAttributeMappings().withAttributeMappings(Arrays.asList(
                                            new MapperAttributeMapping().withName("Name")
                                                .withType(MappingType.DERIVED)
                                                .withFunctionName("upper")
                                                .withExpression("upper(EmpName)")
                                                .withAttributeReferences(
                                                    Arrays.asList(new MapperAttributeReference().withName("EmpName")
                                                        .withEntity("lookup")
                                                        .withEntityConnectionReference(new MapperConnectionReference()
                                                            .withConnectionName("amjaAdls03")
                                                            .withType(ConnectionType.LINKEDSERVICETYPE)))),
                                            new MapperAttributeMapping().withName("PersonID")
                                                .withType(MappingType.DIRECT)
                                                .withFunctionName("")
                                                .withAttributeReference(
                                                    new MapperAttributeReference().withName("EmpId")
                                                        .withEntity("lookup")
                                                        .withEntityConnectionReference(new MapperConnectionReference()
                                                            .withConnectionName("amjaAdls03")
                                                            .withType(ConnectionType.LINKEDSERVICETYPE)))))),
                                new DataMapperMapping().withTargetEntityName("dbo.employee")
                                    .withSourceEntityName("source/employee")
                                    .withSourceConnectionReference(
                                        new MapperConnectionReference().withConnectionName("amjaAdls03")
                                            .withType(ConnectionType.LINKEDSERVICETYPE))
                                    .withAttributeMappingInfo(
                                        new MapperAttributeMappings().withAttributeMappings(Arrays.asList())),
                                new DataMapperMapping().withTargetEntityName("dbo.justSchema")
                                    .withSourceEntityName("source/justSchema")
                                    .withSourceConnectionReference(
                                        new MapperConnectionReference().withConnectionName("amjaAdls03")
                                            .withType(ConnectionType.LINKEDSERVICETYPE))
                                    .withAttributeMappingInfo(
                                        new MapperAttributeMappings().withAttributeMappings(Arrays.asList(
                                            new MapperAttributeMapping().withName("CustAddres")
                                                .withType(MappingType.DERIVED)
                                                .withFunctionName("trim")
                                                .withExpression("trim(CustAddres)")
                                                .withAttributeReferences(
                                                    Arrays.asList(new MapperAttributeReference().withName("CustAddres")
                                                        .withEntity("source/justSchema")
                                                        .withEntityConnectionReference(new MapperConnectionReference()
                                                            .withConnectionName("amjaAdls03")
                                                            .withType(ConnectionType.LINKEDSERVICETYPE)))),
                                            new MapperAttributeMapping().withName("CustDepLoc")
                                                .withType(MappingType.DIRECT)
                                                .withAttributeReference(
                                                    new MapperAttributeReference()
                                                        .withName("CustDepLoc")
                                                        .withEntity("source/justSchema")
                                                        .withEntityConnectionReference(
                                                            new MapperConnectionReference()
                                                                .withConnectionName("amjaAdls03")
                                                                .withType(ConnectionType.LINKEDSERVICETYPE))),
                                            new MapperAttributeMapping().withName("CustDepName")
                                                .withType(MappingType.DERIVED)
                                                .withFunctionName("")
                                                .withExpression("concat(CustName, \" -> \", CustDepName)")
                                                .withAttributeReferences(Arrays.asList(
                                                    new MapperAttributeReference().withName("CustName")
                                                        .withEntity("source/justSchema")
                                                        .withEntityConnectionReference(
                                                            new MapperConnectionReference()
                                                                .withConnectionName("amjaAdls03")
                                                                .withType(ConnectionType.LINKEDSERVICETYPE)),
                                                    new MapperAttributeReference().withName("CustDepName")
                                                        .withEntity("source/justSchema")
                                                        .withEntityConnectionReference(new MapperConnectionReference()
                                                            .withConnectionName("amjaAdls03")
                                                            .withType(ConnectionType.LINKEDSERVICETYPE)))),
                                            new MapperAttributeMapping().withName("CustId")
                                                .withType(MappingType.DIRECT)
                                                .withFunctionName("")
                                                .withAttributeReference(
                                                    new MapperAttributeReference().withName("CustId")
                                                        .withEntity("source/justSchema")
                                                        .withEntityConnectionReference(new MapperConnectionReference()
                                                            .withConnectionName("amjaAdls03")
                                                            .withType(ConnectionType.LINKEDSERVICETYPE))),
                                            new MapperAttributeMapping().withName("CustName")
                                                .withType(MappingType.DIRECT)
                                                .withAttributeReference(
                                                    new MapperAttributeReference().withName("CustName")
                                                        .withEntity("source/justSchema")
                                                        .withEntityConnectionReference(new MapperConnectionReference()
                                                            .withConnectionName("amjaAdls03")
                                                            .withType(ConnectionType.LINKEDSERVICETYPE))))))))
                            .withRelationships(Arrays.asList())))
            .withPolicy(new MapperPolicy().withMode("Microbatch")
                .withRecurrence(new MapperPolicyRecurrence().withFrequency(FrequencyType.MINUTE).withInterval(15)))
            .withAllowVNetOverride(false)
            .withStatus("Stopped")
            .apply();
    }
}
```

### ChangeDataCapture_Delete

```java
/**
 * Samples for ChangeDataCapture Delete.
 */
public final class ChangeDataCaptureDeleteSamples {
    /*
     * x-ms-original-file: 2018-06-01/ChangeDataCapture_Delete.json
     */
    /**
     * Sample code: ChangeDataCapture_Delete.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void changeDataCaptureDelete(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.changeDataCaptures()
            .deleteWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleChangeDataCapture",
                com.azure.core.util.Context.NONE);
    }
}
```

### ChangeDataCapture_Get

```java
/**
 * Samples for ChangeDataCapture Get.
 */
public final class ChangeDataCaptureGetSamples {
    /*
     * x-ms-original-file: 2018-06-01/ChangeDataCapture_Get.json
     */
    /**
     * Sample code: ChangeDataCapture_Get.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void changeDataCaptureGet(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.changeDataCaptures()
            .getWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleChangeDataCapture", null,
                com.azure.core.util.Context.NONE);
    }
}
```

### ChangeDataCapture_ListByFactory

```java
/**
 * Samples for ChangeDataCapture ListByFactory.
 */
public final class ChangeDataCaptureListByFactorySamples {
    /*
     * x-ms-original-file: 2018-06-01/ChangeDataCapture_ListByFactory.json
     */
    /**
     * Sample code: ChangeDataCapture_ListByFactory.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void
        changeDataCaptureListByFactory(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.changeDataCaptures()
            .listByFactory("exampleResourceGroup", "exampleFactoryName", com.azure.core.util.Context.NONE);
    }
}
```

### ChangeDataCapture_Start

```java
/**
 * Samples for ChangeDataCapture Start.
 */
public final class ChangeDataCaptureStartSamples {
    /*
     * x-ms-original-file: 2018-06-01/ChangeDataCapture_Start.json
     */
    /**
     * Sample code: ChangeDataCapture_Start.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void changeDataCaptureStart(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.changeDataCaptures()
            .startWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleChangeDataCapture",
                com.azure.core.util.Context.NONE);
    }
}
```

### ChangeDataCapture_Status

```java
/**
 * Samples for ChangeDataCapture Status.
 */
public final class ChangeDataCaptureStatusSamples {
    /*
     * x-ms-original-file: 2018-06-01/ChangeDataCapture_Status.json
     */
    /**
     * Sample code: ChangeDataCapture_Start.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void changeDataCaptureStart(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.changeDataCaptures()
            .statusWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleChangeDataCapture",
                com.azure.core.util.Context.NONE);
    }
}
```

### ChangeDataCapture_Stop

```java
/**
 * Samples for ChangeDataCapture Stop.
 */
public final class ChangeDataCaptureStopSamples {
    /*
     * x-ms-original-file: 2018-06-01/ChangeDataCapture_Stop.json
     */
    /**
     * Sample code: ChangeDataCapture_Stop.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void changeDataCaptureStop(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.changeDataCaptures()
            .stopWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleChangeDataCapture",
                com.azure.core.util.Context.NONE);
    }
}
```

### CredentialOperations_CreateOrUpdate

```java
import com.azure.resourcemanager.datafactory.models.ManagedIdentityCredential;

/**
 * Samples for CredentialOperations CreateOrUpdate.
 */
public final class CredentialOperationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2018-06-01/Credentials_Create.json
     */
    /**
     * Sample code: Credentials_Create.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void credentialsCreate(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.credentialOperations()
            .define("exampleCredential")
            .withExistingFactory("exampleResourceGroup", "exampleFactoryName")
            .withProperties(new ManagedIdentityCredential().withResourceId(
                "/subscriptions/12345678-1234-1234-1234-123456789012/resourcegroups/exampleResourceGroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/exampleUami"))
            .create();
    }
}
```

### CredentialOperations_Delete

```java
/**
 * Samples for CredentialOperations Delete.
 */
public final class CredentialOperationsDeleteSamples {
    /*
     * x-ms-original-file: 2018-06-01/Credentials_Delete.json
     */
    /**
     * Sample code: Credentials_Delete.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void credentialsDelete(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.credentialOperations()
            .deleteWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleCredential",
                com.azure.core.util.Context.NONE);
    }
}
```

### CredentialOperations_Get

```java
/**
 * Samples for CredentialOperations Get.
 */
public final class CredentialOperationsGetSamples {
    /*
     * x-ms-original-file: 2018-06-01/Credentials_Get.json
     */
    /**
     * Sample code: Credentials_Get.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void credentialsGet(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.credentialOperations()
            .getWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleCredential", null,
                com.azure.core.util.Context.NONE);
    }
}
```

### CredentialOperations_ListByFactory

```java
/**
 * Samples for CredentialOperations ListByFactory.
 */
public final class CredentialOperationsListByFactorySamples {
    /*
     * x-ms-original-file: 2018-06-01/Credentials_ListByFactory.json
     */
    /**
     * Sample code: Credentials_ListByFactory.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void credentialsListByFactory(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.credentialOperations()
            .listByFactory("exampleResourceGroup", "exampleFactoryName", com.azure.core.util.Context.NONE);
    }
}
```

### DataFlowDebugSession_AddDataFlow

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.datafactory.models.DataFlowDebugPackage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for DataFlowDebugSession AddDataFlow.
 */
public final class DataFlowDebugSessionAddDataFlowSamples {
    /*
     * x-ms-original-file: 2018-06-01/DataFlowDebugSession_AddDataFlow.json
     */
    /**
     * Sample code: DataFlowDebugSession_AddDataFlow.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void dataFlowDebugSessionAddDataFlow(com.azure.resourcemanager.datafactory.DataFactoryManager manager)
        throws IOException {
        manager.dataFlowDebugSessions()
            .addDataFlowWithResponse("exampleResourceGroup", "exampleFactoryName",
                new DataFlowDebugPackage().withAdditionalProperties(mapOf("properties", SerializerFactory
                    .createDefaultManagementSerializerAdapter()
                    .deserialize(
                        "{\"dataFlow\":{\"name\":\"dataflow1\",\"properties\":{\"type\":\"MappingDataFlow\",\"typeProperties\":{\"script\":\"\\n\\nsource(output(\\n\\t\\tColumn_1 as string\\n\\t),\\n\\tallowSchemaDrift: true,\\n\\tvalidateSchema: false) ~> source1\",\"sinks\":[],\"sources\":[{\"name\":\"source1\",\"dataset\":{\"type\":\"DatasetReference\",\"referenceName\":\"DelimitedText2\"}}],\"transformations\":[]}}},\"datasets\":[{\"name\":\"dataset1\",\"properties\":{\"type\":\"DelimitedText\",\"schema\":[{\"type\":\"String\"}],\"annotations\":[],\"linkedServiceName\":{\"type\":\"LinkedServiceReference\",\"referenceName\":\"linkedService5\"},\"typeProperties\":{\"columnDelimiter\":\",\",\"escapeChar\":\"\\\\\",\"firstRowAsHeader\":true,\"location\":{\"type\":\"AzureBlobStorageLocation\",\"container\":\"dataflow-sample-data\",\"fileName\":\"Ansiencoding.csv\"},\"quoteChar\":\"\\\"\"}}}],\"debugSettings\":{\"datasetParameters\":{\"Movies\":{\"path\":\"abc\"},\"Output\":{\"time\":\"def\"}},\"parameters\":{\"sourcePath\":\"Toy\"},\"sourceSettings\":[{\"rowLimit\":1000,\"sourceName\":\"source1\"},{\"rowLimit\":222,\"sourceName\":\"source2\"}]},\"linkedServices\":[{\"name\":\"linkedService1\",\"properties\":{\"type\":\"AzureBlobStorage\",\"annotations\":[],\"typeProperties\":{\"connectionString\":\"DefaultEndpointsProtocol=https;AccountName=<storageName>;EndpointSuffix=core.windows.net;\",\"encryptedCredential\":\"<credential>\"}}}],\"sessionId\":\"f06ed247-9d07-49b2-b05e-2cb4a2fc871e\"}",
                        Object.class, SerializerEncoding.JSON))),
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

/**
 * Samples for DataFlowDebugSession Create.
 */
public final class DataFlowDebugSessionCreateSamples {
    /*
     * x-ms-original-file: 2018-06-01/DataFlowDebugSession_Create.json
     */
    /**
     * Sample code: DataFlowDebugSession_Create.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void dataFlowDebugSessionCreate(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.dataFlowDebugSessions()
            .create("exampleResourceGroup", "exampleFactoryName",
                new CreateDataFlowDebugSessionRequest().withTimeToLive(60)
                    .withIntegrationRuntime(new IntegrationRuntimeDebugResource().withName("ir1")
                        .withProperties(new ManagedIntegrationRuntime()
                            .withComputeProperties(new IntegrationRuntimeComputeProperties().withLocation("AutoResolve")
                                .withDataFlowProperties(new IntegrationRuntimeDataFlowProperties()
                                    .withComputeType(DataFlowComputeType.GENERAL)
                                    .withCoreCount(48)
                                    .withTimeToLive(10)
                                    .withAdditionalProperties(mapOf()))
                                .withAdditionalProperties(mapOf())))),
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

### DataFlowDebugSession_Delete

```java
import com.azure.resourcemanager.datafactory.models.DeleteDataFlowDebugSessionRequest;

/**
 * Samples for DataFlowDebugSession Delete.
 */
public final class DataFlowDebugSessionDeleteSamples {
    /*
     * x-ms-original-file: 2018-06-01/DataFlowDebugSession_Delete.json
     */
    /**
     * Sample code: DataFlowDebugSession_Delete.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void dataFlowDebugSessionDelete(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.dataFlowDebugSessions()
            .deleteWithResponse("exampleResourceGroup", "exampleFactoryName",
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

/**
 * Samples for DataFlowDebugSession ExecuteCommand.
 */
public final class DataFlowDebugSessionExecuteCommandSamples {
    /*
     * x-ms-original-file: 2018-06-01/DataFlowDebugSession_ExecuteCommand.json
     */
    /**
     * Sample code: DataFlowDebugSession_ExecuteCommand.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void
        dataFlowDebugSessionExecuteCommand(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.dataFlowDebugSessions()
            .executeCommand("exampleResourceGroup", "exampleFactoryName",
                new DataFlowDebugCommandRequest().withSessionId("f06ed247-9d07-49b2-b05e-2cb4a2fc871e")
                    .withCommand(DataFlowDebugCommandType.EXECUTE_PREVIEW_QUERY)
                    .withCommandPayload(new DataFlowDebugCommandPayload().withStreamName("source1").withRowLimits(100)),
                com.azure.core.util.Context.NONE);
    }
}
```

### DataFlowDebugSession_QueryByFactory

```java
/**
 * Samples for DataFlowDebugSession QueryByFactory.
 */
public final class DataFlowDebugSessionQueryByFactorySamples {
    /*
     * x-ms-original-file: 2018-06-01/DataFlowDebugSession_QueryByFactory.json
     */
    /**
     * Sample code: DataFlowDebugSession_QueryByFactory.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void
        dataFlowDebugSessionQueryByFactory(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.dataFlowDebugSessions()
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
import com.azure.resourcemanager.datafactory.models.DatasetReferenceType;
import com.azure.resourcemanager.datafactory.models.MappingDataFlow;
import java.util.Arrays;

/**
 * Samples for DataFlows CreateOrUpdate.
 */
public final class DataFlowsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2018-06-01/DataFlows_Create.json
     */
    /**
     * Sample code: DataFlows_Create.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void dataFlowsCreate(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.dataFlows()
            .define("exampleDataFlow")
            .withExistingFactory("exampleResourceGroup", "exampleFactoryName")
            .withProperties(new MappingDataFlow().withDescription(
                "Sample demo data flow to convert currencies showing usage of union, derive and conditional split transformation.")
                .withSources(Arrays.asList(
                    new DataFlowSource().withName("USDCurrency")
                        .withDataset(new DatasetReference().withType(DatasetReferenceType.DATASET_REFERENCE)
                            .withReferenceName("CurrencyDatasetUSD")),
                    new DataFlowSource().withName("CADSource")
                        .withDataset(new DatasetReference().withType(DatasetReferenceType.DATASET_REFERENCE)
                            .withReferenceName("CurrencyDatasetCAD"))))
                .withSinks(Arrays.asList(
                    new DataFlowSink().withName("USDSink")
                        .withDataset(new DatasetReference().withType(DatasetReferenceType.DATASET_REFERENCE)
                            .withReferenceName("USDOutput")),
                    new DataFlowSink().withName("CADSink")
                        .withDataset(new DatasetReference().withType(DatasetReferenceType.DATASET_REFERENCE)
                            .withReferenceName("CADOutput"))))
                .withScriptLines(Arrays.asList("source(output(", "PreviousConversionRate as double,",
                    "Country as string,", "DateTime1 as string,", "CurrentConversionRate as double", "),",
                    "allowSchemaDrift: false,", "validateSchema: false) ~> USDCurrency", "source(output(",
                    "PreviousConversionRate as double,", "Country as string,", "DateTime1 as string,",
                    "CurrentConversionRate as double", "),", "allowSchemaDrift: true,",
                    "validateSchema: false) ~> CADSource", "USDCurrency, CADSource union(byName: true)~> Union",
                    "Union derive(NewCurrencyRate = round(CurrentConversionRate*1.25)) ~> NewCurrencyColumn",
                    "NewCurrencyColumn split(Country == 'USD',",
                    "Country == 'CAD',disjoint: false) ~> ConditionalSplit1@(USD, CAD)",
                    "ConditionalSplit1@USD sink(saveMode:'overwrite' ) ~> USDSink",
                    "ConditionalSplit1@CAD sink(saveMode:'overwrite' ) ~> CADSink")))
            .create();
    }

    /*
     * x-ms-original-file: 2018-06-01/DataFlows_Update.json
     */
    /**
     * Sample code: DataFlows_Update.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void dataFlowsUpdate(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        DataFlowResource resource = manager.dataFlows()
            .getWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleDataFlow", null,
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new MappingDataFlow().withDescription(
                "Sample demo data flow to convert currencies showing usage of union, derive and conditional split transformation.")
                .withSources(Arrays.asList(
                    new DataFlowSource().withName("USDCurrency")
                        .withDataset(new DatasetReference().withType(DatasetReferenceType.DATASET_REFERENCE)
                            .withReferenceName("CurrencyDatasetUSD")),
                    new DataFlowSource().withName("CADSource")
                        .withDataset(new DatasetReference().withType(DatasetReferenceType.DATASET_REFERENCE)
                            .withReferenceName("CurrencyDatasetCAD"))))
                .withSinks(Arrays.asList(
                    new DataFlowSink().withName("USDSink")
                        .withDataset(new DatasetReference().withType(DatasetReferenceType.DATASET_REFERENCE)
                            .withReferenceName("USDOutput")),
                    new DataFlowSink().withName("CADSink")
                        .withDataset(new DatasetReference().withType(DatasetReferenceType.DATASET_REFERENCE)
                            .withReferenceName("CADOutput"))))
                .withScriptLines(Arrays.asList("source(output(", "PreviousConversionRate as double,",
                    "Country as string,", "DateTime1 as string,", "CurrentConversionRate as double", "),",
                    "allowSchemaDrift: false,", "validateSchema: false) ~> USDCurrency", "source(output(",
                    "PreviousConversionRate as double,", "Country as string,", "DateTime1 as string,",
                    "CurrentConversionRate as double", "),", "allowSchemaDrift: true,",
                    "validateSchema: false) ~> CADSource", "USDCurrency, CADSource union(byName: true)~> Union",
                    "Union derive(NewCurrencyRate = round(CurrentConversionRate*1.25)) ~> NewCurrencyColumn",
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
/**
 * Samples for DataFlows Delete.
 */
public final class DataFlowsDeleteSamples {
    /*
     * x-ms-original-file: 2018-06-01/DataFlows_Delete.json
     */
    /**
     * Sample code: DataFlows_Delete.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void dataFlowsDelete(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.dataFlows()
            .deleteWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleDataFlow",
                com.azure.core.util.Context.NONE);
    }
}
```

### DataFlows_Get

```java
/**
 * Samples for DataFlows Get.
 */
public final class DataFlowsGetSamples {
    /*
     * x-ms-original-file: 2018-06-01/DataFlows_Get.json
     */
    /**
     * Sample code: DataFlows_Get.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void dataFlowsGet(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.dataFlows()
            .getWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleDataFlow", null,
                com.azure.core.util.Context.NONE);
    }
}
```

### DataFlows_ListByFactory

```java
/**
 * Samples for DataFlows ListByFactory.
 */
public final class DataFlowsListByFactorySamples {
    /*
     * x-ms-original-file: 2018-06-01/DataFlows_ListByFactory.json
     */
    /**
     * Sample code: DataFlows_ListByFactory.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void dataFlowsListByFactory(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.dataFlows()
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
import com.azure.resourcemanager.datafactory.models.Type;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Datasets CreateOrUpdate.
 */
public final class DatasetsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2018-06-01/Datasets_Create.json
     */
    /**
     * Sample code: Datasets_Create.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void datasetsCreate(com.azure.resourcemanager.datafactory.DataFactoryManager manager)
        throws IOException {
        manager.datasets()
            .define("exampleDataset")
            .withExistingFactory("exampleResourceGroup", "exampleFactoryName")
            .withProperties(new AzureBlobDataset()
                .withLinkedServiceName(new LinkedServiceReference().withType(Type.LINKED_SERVICE_REFERENCE)
                    .withReferenceName("exampleLinkedService"))
                .withParameters(mapOf("MyFileName", new ParameterSpecification().withType(ParameterType.STRING),
                    "MyFolderPath", new ParameterSpecification().withType(ParameterType.STRING)))
                .withFolderPath(SerializerFactory.createDefaultManagementSerializerAdapter()
                    .deserialize("{\"type\":\"Expression\",\"value\":\"@dataset().MyFolderPath\"}", Object.class,
                        SerializerEncoding.JSON))
                .withFileName(SerializerFactory.createDefaultManagementSerializerAdapter()
                    .deserialize("{\"type\":\"Expression\",\"value\":\"@dataset().MyFileName\"}", Object.class,
                        SerializerEncoding.JSON))
                .withFormat(new TextFormat()))
            .create();
    }

    /*
     * x-ms-original-file: 2018-06-01/Datasets_Update.json
     */
    /**
     * Sample code: Datasets_Update.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void datasetsUpdate(com.azure.resourcemanager.datafactory.DataFactoryManager manager)
        throws IOException {
        DatasetResource resource = manager.datasets()
            .getWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleDataset", null,
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new AzureBlobDataset().withDescription("Example description")
                .withLinkedServiceName(new LinkedServiceReference().withType(Type.LINKED_SERVICE_REFERENCE)
                    .withReferenceName("exampleLinkedService"))
                .withParameters(mapOf("MyFileName", new ParameterSpecification().withType(ParameterType.STRING),
                    "MyFolderPath", new ParameterSpecification().withType(ParameterType.STRING)))
                .withFolderPath(SerializerFactory.createDefaultManagementSerializerAdapter()
                    .deserialize("{\"type\":\"Expression\",\"value\":\"@dataset().MyFolderPath\"}", Object.class,
                        SerializerEncoding.JSON))
                .withFileName(SerializerFactory.createDefaultManagementSerializerAdapter()
                    .deserialize("{\"type\":\"Expression\",\"value\":\"@dataset().MyFileName\"}", Object.class,
                        SerializerEncoding.JSON))
                .withFormat(new TextFormat()))
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

### Datasets_Delete

```java
/**
 * Samples for Datasets Delete.
 */
public final class DatasetsDeleteSamples {
    /*
     * x-ms-original-file: 2018-06-01/Datasets_Delete.json
     */
    /**
     * Sample code: Datasets_Delete.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void datasetsDelete(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.datasets()
            .deleteWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleDataset",
                com.azure.core.util.Context.NONE);
    }
}
```

### Datasets_Get

```java
/**
 * Samples for Datasets Get.
 */
public final class DatasetsGetSamples {
    /*
     * x-ms-original-file: 2018-06-01/Datasets_Get.json
     */
    /**
     * Sample code: Datasets_Get.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void datasetsGet(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.datasets()
            .getWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleDataset", null,
                com.azure.core.util.Context.NONE);
    }
}
```

### Datasets_ListByFactory

```java
/**
 * Samples for Datasets ListByFactory.
 */
public final class DatasetsListByFactorySamples {
    /*
     * x-ms-original-file: 2018-06-01/Datasets_ListByFactory.json
     */
    /**
     * Sample code: Datasets_ListByFactory.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void datasetsListByFactory(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.datasets()
            .listByFactory("exampleResourceGroup", "exampleFactoryName", com.azure.core.util.Context.NONE);
    }
}
```

### ExposureControl_GetFeatureValue

```java
import com.azure.resourcemanager.datafactory.models.ExposureControlRequest;

/**
 * Samples for ExposureControl GetFeatureValue.
 */
public final class ExposureControlGetFeatureValueSamples {
    /*
     * x-ms-original-file: 2018-06-01/ExposureControl_GetFeatureValue.json
     */
    /**
     * Sample code: ExposureControl_GetFeatureValue.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void
        exposureControlGetFeatureValue(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.exposureControls()
            .getFeatureValueWithResponse("WestEurope",
                new ExposureControlRequest().withFeatureName("ADFIntegrationRuntimeSharingRbac")
                    .withFeatureType("Feature"),
                com.azure.core.util.Context.NONE);
    }
}
```

### ExposureControl_GetFeatureValueByFactory

```java
import com.azure.resourcemanager.datafactory.models.ExposureControlRequest;

/**
 * Samples for ExposureControl GetFeatureValueByFactory.
 */
public final class ExposureControlGetFeatureValueByFactorySamples {
    /*
     * x-ms-original-file: 2018-06-01/ExposureControl_GetFeatureValueByFactory.json
     */
    /**
     * Sample code: ExposureControl_GetFeatureValueByFactory.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void
        exposureControlGetFeatureValueByFactory(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.exposureControls()
            .getFeatureValueByFactoryWithResponse("exampleResourceGroup", "exampleFactoryName",
                new ExposureControlRequest().withFeatureName("ADFIntegrationRuntimeSharingRbac")
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

/**
 * Samples for ExposureControl QueryFeatureValuesByFactory.
 */
public final class ExposureControlQueryFeatureValuesByFactorySamples {
    /*
     * x-ms-original-file: 2018-06-01/ExposureControl_QueryFeatureValuesByFactory.json
     */
    /**
     * Sample code: ExposureControl_QueryFeatureValuesByFactory.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void
        exposureControlQueryFeatureValuesByFactory(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.exposureControls()
            .queryFeatureValuesByFactoryWithResponse("exampleResourceGroup", "exampleFactoryName",
                new ExposureControlBatchRequest().withExposureControlRequests(Arrays.asList(
                    new ExposureControlRequest().withFeatureName("ADFIntegrationRuntimeSharingRbac")
                        .withFeatureType("Feature"),
                    new ExposureControlRequest().withFeatureName("ADFSampleFeature").withFeatureType("Feature"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### Factories_ConfigureFactoryRepo

```java
import com.azure.resourcemanager.datafactory.models.FactoryRepoUpdate;
import com.azure.resourcemanager.datafactory.models.FactoryVstsConfiguration;

/**
 * Samples for Factories ConfigureFactoryRepo.
 */
public final class FactoriesConfigureFactoryRepoSamples {
    /*
     * x-ms-original-file: 2018-06-01/Factories_ConfigureFactoryRepo.json
     */
    /**
     * Sample code: Factories_ConfigureFactoryRepo.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void factoriesConfigureFactoryRepo(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.factories()
            .configureFactoryRepoWithResponse("East US", new FactoryRepoUpdate().withFactoryResourceId(
                "/subscriptions/12345678-1234-1234-1234-123456789012/resourceGroups/exampleResourceGroup/providers/Microsoft.DataFactory/factories/exampleFactoryName")
                .withRepoConfiguration(new FactoryVstsConfiguration().withAccountName("ADF")
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
/**
 * Samples for Factories CreateOrUpdate.
 */
public final class FactoriesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2018-06-01/Factories_CreateOrUpdate.json
     */
    /**
     * Sample code: Factories_CreateOrUpdate.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void factoriesCreateOrUpdate(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.factories()
            .define("exampleFactoryName")
            .withExistingResourceGroup("exampleResourceGroup")
            .withRegion("East US")
            .create();
    }
}
```

### Factories_Delete

```java
/**
 * Samples for Factories Delete.
 */
public final class FactoriesDeleteSamples {
    /*
     * x-ms-original-file: 2018-06-01/Factories_Delete.json
     */
    /**
     * Sample code: Factories_Delete.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void factoriesDelete(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.factories()
            .deleteByResourceGroupWithResponse("exampleResourceGroup", "exampleFactoryName",
                com.azure.core.util.Context.NONE);
    }
}
```

### Factories_GetByResourceGroup

```java
/**
 * Samples for Factories GetByResourceGroup.
 */
public final class FactoriesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2018-06-01/Factories_Get.json
     */
    /**
     * Sample code: Factories_Get.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void factoriesGet(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.factories()
            .getByResourceGroupWithResponse("exampleResourceGroup", "exampleFactoryName", null,
                com.azure.core.util.Context.NONE);
    }
}
```

### Factories_GetDataPlaneAccess

```java
import com.azure.resourcemanager.datafactory.models.UserAccessPolicy;

/**
 * Samples for Factories GetDataPlaneAccess.
 */
public final class FactoriesGetDataPlaneAccessSamples {
    /*
     * x-ms-original-file: 2018-06-01/Factories_GetDataPlaneAccess.json
     */
    /**
     * Sample code: Factories_GetDataPlaneAccess.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void factoriesGetDataPlaneAccess(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.factories()
            .getDataPlaneAccessWithResponse("exampleResourceGroup", "exampleFactoryName",
                new UserAccessPolicy().withPermissions("r")
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

/**
 * Samples for Factories GetGitHubAccessToken.
 */
public final class FactoriesGetGitHubAccessTokenSamples {
    /*
     * x-ms-original-file: 2018-06-01/Factories_GetGitHubAccessToken.json
     */
    /**
     * Sample code: Factories_GetGitHubAccessToken.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void factoriesGetGitHubAccessToken(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.factories()
            .getGitHubAccessTokenWithResponse("exampleResourceGroup", "exampleFactoryName",
                new GitHubAccessTokenRequest().withGitHubAccessCode("fakeTokenPlaceholder")
                    .withGitHubClientId("some")
                    .withGitHubAccessTokenBaseUrl("fakeTokenPlaceholder"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Factories_List

```java
/**
 * Samples for Factories List.
 */
public final class FactoriesListSamples {
    /*
     * x-ms-original-file: 2018-06-01/Factories_List.json
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
/**
 * Samples for Factories ListByResourceGroup.
 */
public final class FactoriesListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2018-06-01/Factories_ListByResourceGroup.json
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

/**
 * Samples for Factories Update.
 */
public final class FactoriesUpdateSamples {
    /*
     * x-ms-original-file: 2018-06-01/Factories_Update.json
     */
    /**
     * Sample code: Factories_Update.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void factoriesUpdate(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        Factory resource = manager.factories()
            .getByResourceGroupWithResponse("exampleResourceGroup", "exampleFactoryName", null,
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("exampleTag", "exampleValue")).apply();
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

### GlobalParameters_CreateOrUpdate

```java
import com.azure.resourcemanager.datafactory.models.GlobalParameterResource;
import com.azure.resourcemanager.datafactory.models.GlobalParameterSpecification;
import java.util.Map;

/**
 * Samples for GlobalParameters CreateOrUpdate.
 */
public final class GlobalParametersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2018-06-01/GlobalParameters_Create.json
     */
    /**
     * Sample code: GlobalParameters_Create.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void globalParametersCreate(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.globalParameters()
            .define("default")
            .withExistingFactory("exampleResourceGroup", "exampleFactoryName")
            .withProperties((Map<String, GlobalParameterSpecification>) null)
            .create();
    }

    /*
     * x-ms-original-file: 2018-06-01/GlobalParameters_Update.json
     */
    /**
     * Sample code: GlobalParameters_Update.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void globalParametersUpdate(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        GlobalParameterResource resource = manager.globalParameters()
            .getWithResponse("exampleResourceGroup", "exampleFactoryName", "default", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().apply();
    }
}
```

### GlobalParameters_Delete

```java
/**
 * Samples for GlobalParameters Delete.
 */
public final class GlobalParametersDeleteSamples {
    /*
     * x-ms-original-file: 2018-06-01/GlobalParameters_Delete.json
     */
    /**
     * Sample code: GlobalParameters_Delete.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void globalParametersDelete(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.globalParameters()
            .deleteWithResponse("exampleResourceGroup", "exampleFactoryName", "default",
                com.azure.core.util.Context.NONE);
    }
}
```

### GlobalParameters_Get

```java
/**
 * Samples for GlobalParameters Get.
 */
public final class GlobalParametersGetSamples {
    /*
     * x-ms-original-file: 2018-06-01/GlobalParameters_Get.json
     */
    /**
     * Sample code: GlobalParameters_Get.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void globalParametersGet(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.globalParameters()
            .getWithResponse("exampleResourceGroup", "exampleFactoryName", "default", com.azure.core.util.Context.NONE);
    }
}
```

### GlobalParameters_ListByFactory

```java
/**
 * Samples for GlobalParameters ListByFactory.
 */
public final class GlobalParametersListByFactorySamples {
    /*
     * x-ms-original-file: 2018-06-01/GlobalParameters_ListByFactory.json
     */
    /**
     * Sample code: GlobalParameters_ListByFactory.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void globalParametersListByFactory(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.globalParameters()
            .listByFactory("exampleResourceGroup", "exampleFactoryName", com.azure.core.util.Context.NONE);
    }
}
```

### IntegrationRuntimeNodes_Delete

```java
/**
 * Samples for IntegrationRuntimeNodes Delete.
 */
public final class IntegrationRuntimeNodesDeleteSamples {
    /*
     * x-ms-original-file: 2018-06-01/IntegrationRuntimeNodes_Delete.json
     */
    /**
     * Sample code: IntegrationRuntimesNodes_Delete.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void
        integrationRuntimesNodesDelete(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.integrationRuntimeNodes()
            .deleteWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleIntegrationRuntime", "Node_1",
                com.azure.core.util.Context.NONE);
    }
}
```

### IntegrationRuntimeNodes_Get

```java
/**
 * Samples for IntegrationRuntimeNodes Get.
 */
public final class IntegrationRuntimeNodesGetSamples {
    /*
     * x-ms-original-file: 2018-06-01/IntegrationRuntimeNodes_Get.json
     */
    /**
     * Sample code: IntegrationRuntimeNodes_Get.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void integrationRuntimeNodesGet(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.integrationRuntimeNodes()
            .getWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleIntegrationRuntime", "Node_1",
                com.azure.core.util.Context.NONE);
    }
}
```

### IntegrationRuntimeNodes_GetIpAddress

```java
/**
 * Samples for IntegrationRuntimeNodes GetIpAddress.
 */
public final class IntegrationRuntimeNodesGetIpAddressSamples {
    /*
     * x-ms-original-file: 2018-06-01/IntegrationRuntimeNodes_GetIpAddress.json
     */
    /**
     * Sample code: IntegrationRuntimeNodes_GetIpAddress.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void
        integrationRuntimeNodesGetIpAddress(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.integrationRuntimeNodes()
            .getIpAddressWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleIntegrationRuntime",
                "Node_1", com.azure.core.util.Context.NONE);
    }
}
```

### IntegrationRuntimeNodes_Update

```java
import com.azure.resourcemanager.datafactory.models.UpdateIntegrationRuntimeNodeRequest;

/**
 * Samples for IntegrationRuntimeNodes Update.
 */
public final class IntegrationRuntimeNodesUpdateSamples {
    /*
     * x-ms-original-file: 2018-06-01/IntegrationRuntimeNodes_Update.json
     */
    /**
     * Sample code: IntegrationRuntimeNodes_Update.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void integrationRuntimeNodesUpdate(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.integrationRuntimeNodes()
            .updateWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleIntegrationRuntime", "Node_1",
                new UpdateIntegrationRuntimeNodeRequest().withConcurrentJobsLimit(2), com.azure.core.util.Context.NONE);
    }
}
```

### IntegrationRuntimeObjectMetadata_Get

```java
import com.azure.resourcemanager.datafactory.models.GetSsisObjectMetadataRequest;

/**
 * Samples for IntegrationRuntimeObjectMetadata Get.
 */
public final class IntegrationRuntimeObjectMetadataGetSamples {
    /*
     * x-ms-original-file: 2018-06-01/IntegrationRuntimeObjectMetadata_Get.json
     */
    /**
     * Sample code: IntegrationRuntimeObjectMetadata_Get.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void
        integrationRuntimeObjectMetadataGet(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.integrationRuntimeObjectMetadatas()
            .getWithResponse("exampleResourceGroup", "exampleFactoryName", "testactivityv2",
                new GetSsisObjectMetadataRequest().withMetadataPath("ssisFolders"), com.azure.core.util.Context.NONE);
    }
}
```

### IntegrationRuntimeObjectMetadata_Refresh

```java
/**
 * Samples for IntegrationRuntimeObjectMetadata Refresh.
 */
public final class IntegrationRuntimeObjectMetadataRefreshSamples {
    /*
     * x-ms-original-file: 2018-06-01/IntegrationRuntimeObjectMetadata_Refresh.json
     */
    /**
     * Sample code: IntegrationRuntimeObjectMetadata_Refresh.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void
        integrationRuntimeObjectMetadataRefresh(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.integrationRuntimeObjectMetadatas()
            .refresh("exampleResourceGroup", "exampleFactoryName", "testactivityv2", com.azure.core.util.Context.NONE);
    }
}
```

### IntegrationRuntimeOperation_DisableInteractiveQuery

```java
/**
 * Samples for IntegrationRuntimeOperation DisableInteractiveQuery.
 */
public final class IntegrationRuntimeOperationDisableInteractiveQuerySamples {
    /*
     * x-ms-original-file: 2018-06-01/IntegrationRuntimes_DisableInteractiveQuery.json
     */
    /**
     * Sample code: IntegrationRuntime_DisableInteractiveQuery.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void
        integrationRuntimeDisableInteractiveQuery(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.integrationRuntimeOperations()
            .disableInteractiveQuery("exampleResourceGroup", "exampleFactoryName", "exampleIntegrationRuntime",
                com.azure.core.util.Context.NONE);
    }
}
```

### IntegrationRuntimeOperation_EnableInteractiveQuery

```java
import com.azure.resourcemanager.datafactory.models.EnableInteractiveQueryRequest;

/**
 * Samples for IntegrationRuntimeOperation EnableInteractiveQuery.
 */
public final class IntegrationRuntimeOperationEnableInteractiveQuerySamples {
    /*
     * x-ms-original-file: 2018-06-01/IntegrationRuntimes_EnableInteractiveQuery.json
     */
    /**
     * Sample code: IntegrationRuntime_EnableInteractiveQuery.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void
        integrationRuntimeEnableInteractiveQuery(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.integrationRuntimeOperations()
            .enableInteractiveQuery("exampleResourceGroup", "exampleFactoryName", "exampleIntegrationRuntime",
                new EnableInteractiveQueryRequest().withAutoTerminationMinutes(10), com.azure.core.util.Context.NONE);
    }
}
```

### IntegrationRuntimes_CreateLinkedIntegrationRuntime

```java
import com.azure.resourcemanager.datafactory.models.CreateLinkedIntegrationRuntimeRequest;

/**
 * Samples for IntegrationRuntimes CreateLinkedIntegrationRuntime.
 */
public final class IntegrationRuntimesCreateLinkedIntegrationRuntimeSamples {
    /*
     * x-ms-original-file: 2018-06-01/IntegrationRuntimes_CreateLinkedIntegrationRuntime.json
     */
    /**
     * Sample code: IntegrationRuntimes_CreateLinkedIntegrationRuntime.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void integrationRuntimesCreateLinkedIntegrationRuntime(
        com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.integrationRuntimes()
            .createLinkedIntegrationRuntimeWithResponse("exampleResourceGroup", "exampleFactoryName",
                "exampleIntegrationRuntime",
                new CreateLinkedIntegrationRuntimeRequest().withName("bfa92911-9fb6-4fbe-8f23-beae87bc1c83")
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

/**
 * Samples for IntegrationRuntimes CreateOrUpdate.
 */
public final class IntegrationRuntimesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2018-06-01/IntegrationRuntimes_Create.json
     */
    /**
     * Sample code: IntegrationRuntimes_Create.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void integrationRuntimesCreate(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.integrationRuntimes()
            .define("exampleIntegrationRuntime")
            .withExistingFactory("exampleResourceGroup", "exampleFactoryName")
            .withProperties(new SelfHostedIntegrationRuntime().withDescription("A selfhosted integration runtime"))
            .create();
    }
}
```

### IntegrationRuntimes_Delete

```java
/**
 * Samples for IntegrationRuntimes Delete.
 */
public final class IntegrationRuntimesDeleteSamples {
    /*
     * x-ms-original-file: 2018-06-01/IntegrationRuntimes_Delete.json
     */
    /**
     * Sample code: IntegrationRuntimes_Delete.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void integrationRuntimesDelete(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.integrationRuntimes()
            .deleteWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleIntegrationRuntime",
                com.azure.core.util.Context.NONE);
    }
}
```

### IntegrationRuntimes_Get

```java
/**
 * Samples for IntegrationRuntimes Get.
 */
public final class IntegrationRuntimesGetSamples {
    /*
     * x-ms-original-file: 2018-06-01/IntegrationRuntimes_Get.json
     */
    /**
     * Sample code: IntegrationRuntimes_Get.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void integrationRuntimesGet(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.integrationRuntimes()
            .getWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleIntegrationRuntime", null,
                com.azure.core.util.Context.NONE);
    }
}
```

### IntegrationRuntimes_GetConnectionInfo

```java
/**
 * Samples for IntegrationRuntimes GetConnectionInfo.
 */
public final class IntegrationRuntimesGetConnectionInfoSamples {
    /*
     * x-ms-original-file: 2018-06-01/IntegrationRuntimes_GetConnectionInfo.json
     */
    /**
     * Sample code: IntegrationRuntimes_GetConnectionInfo.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void
        integrationRuntimesGetConnectionInfo(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.integrationRuntimes()
            .getConnectionInfoWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleIntegrationRuntime",
                com.azure.core.util.Context.NONE);
    }
}
```

### IntegrationRuntimes_GetMonitoringData

```java
/**
 * Samples for IntegrationRuntimes GetMonitoringData.
 */
public final class IntegrationRuntimesGetMonitoringDataSamples {
    /*
     * x-ms-original-file: 2018-06-01/IntegrationRuntimes_GetMonitoringData.json
     */
    /**
     * Sample code: IntegrationRuntimes_GetMonitoringData.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void
        integrationRuntimesGetMonitoringData(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.integrationRuntimes()
            .getMonitoringDataWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleIntegrationRuntime",
                com.azure.core.util.Context.NONE);
    }
}
```

### IntegrationRuntimes_GetStatus

```java
/**
 * Samples for IntegrationRuntimes GetStatus.
 */
public final class IntegrationRuntimesGetStatusSamples {
    /*
     * x-ms-original-file: 2018-06-01/IntegrationRuntimes_GetStatus.json
     */
    /**
     * Sample code: IntegrationRuntimes_GetStatus.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void integrationRuntimesGetStatus(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.integrationRuntimes()
            .getStatusWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleIntegrationRuntime",
                com.azure.core.util.Context.NONE);
    }
}
```

### IntegrationRuntimes_ListAuthKeys

```java
/**
 * Samples for IntegrationRuntimes ListAuthKeys.
 */
public final class IntegrationRuntimesListAuthKeysSamples {
    /*
     * x-ms-original-file: 2018-06-01/IntegrationRuntimes_ListAuthKeys.json
     */
    /**
     * Sample code: IntegrationRuntimes_ListAuthKeys.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void
        integrationRuntimesListAuthKeys(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.integrationRuntimes()
            .listAuthKeysWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleIntegrationRuntime",
                com.azure.core.util.Context.NONE);
    }
}
```

### IntegrationRuntimes_ListByFactory

```java
/**
 * Samples for IntegrationRuntimes ListByFactory.
 */
public final class IntegrationRuntimesListByFactorySamples {
    /*
     * x-ms-original-file: 2018-06-01/IntegrationRuntimes_ListByFactory.json
     */
    /**
     * Sample code: IntegrationRuntimes_ListByFactory.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void
        integrationRuntimesListByFactory(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.integrationRuntimes()
            .listByFactory("exampleResourceGroup", "exampleFactoryName", com.azure.core.util.Context.NONE);
    }
}
```

### IntegrationRuntimes_ListOutboundNetworkDependenciesEndpoints

```java
/**
 * Samples for IntegrationRuntimes ListOutboundNetworkDependenciesEndpoints.
 */
public final class IntegrationRuntimesListOutboundNetworkDependenciesEndpointsSamples {
    /*
     * x-ms-original-file: 2018-06-01/IntegrationRuntimes_ListOutboundNetworkDependenciesEndpoints.json
     */
    /**
     * Sample code: IntegrationRuntimes_OutboundNetworkDependenciesEndpoints.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void integrationRuntimesOutboundNetworkDependenciesEndpoints(
        com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.integrationRuntimes()
            .listOutboundNetworkDependenciesEndpointsWithResponse("exampleResourceGroup", "exampleFactoryName",
                "exampleIntegrationRuntime", com.azure.core.util.Context.NONE);
    }
}
```

### IntegrationRuntimes_RegenerateAuthKey

```java
import com.azure.resourcemanager.datafactory.models.IntegrationRuntimeAuthKeyName;
import com.azure.resourcemanager.datafactory.models.IntegrationRuntimeRegenerateKeyParameters;

/**
 * Samples for IntegrationRuntimes RegenerateAuthKey.
 */
public final class IntegrationRuntimesRegenerateAuthKeySamples {
    /*
     * x-ms-original-file: 2018-06-01/IntegrationRuntimes_RegenerateAuthKey.json
     */
    /**
     * Sample code: IntegrationRuntimes_RegenerateAuthKey.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void
        integrationRuntimesRegenerateAuthKey(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.integrationRuntimes()
            .regenerateAuthKeyWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleIntegrationRuntime",
                new IntegrationRuntimeRegenerateKeyParameters().withKeyName(IntegrationRuntimeAuthKeyName.AUTH_KEY2),
                com.azure.core.util.Context.NONE);
    }
}
```

### IntegrationRuntimes_RemoveLinks

```java
import com.azure.resourcemanager.datafactory.models.LinkedIntegrationRuntimeRequest;

/**
 * Samples for IntegrationRuntimes RemoveLinks.
 */
public final class IntegrationRuntimesRemoveLinksSamples {
    /*
     * x-ms-original-file: 2018-06-01/IntegrationRuntimes_RemoveLinks.json
     */
    /**
     * Sample code: IntegrationRuntimes_Upgrade.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void integrationRuntimesUpgrade(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.integrationRuntimes()
            .removeLinksWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleIntegrationRuntime",
                new LinkedIntegrationRuntimeRequest().withLinkedFactoryName("exampleFactoryName-linked"),
                com.azure.core.util.Context.NONE);
    }
}
```

### IntegrationRuntimes_Start

```java
/**
 * Samples for IntegrationRuntimes Start.
 */
public final class IntegrationRuntimesStartSamples {
    /*
     * x-ms-original-file: 2018-06-01/IntegrationRuntimes_Start.json
     */
    /**
     * Sample code: IntegrationRuntimes_Start.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void integrationRuntimesStart(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.integrationRuntimes()
            .start("exampleResourceGroup", "exampleFactoryName", "exampleManagedIntegrationRuntime",
                com.azure.core.util.Context.NONE);
    }
}
```

### IntegrationRuntimes_Stop

```java
/**
 * Samples for IntegrationRuntimes Stop.
 */
public final class IntegrationRuntimesStopSamples {
    /*
     * x-ms-original-file: 2018-06-01/IntegrationRuntimes_Stop.json
     */
    /**
     * Sample code: IntegrationRuntimes_Stop.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void integrationRuntimesStop(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.integrationRuntimes()
            .stop("exampleResourceGroup", "exampleFactoryName", "exampleManagedIntegrationRuntime",
                com.azure.core.util.Context.NONE);
    }
}
```

### IntegrationRuntimes_SyncCredentials

```java
/**
 * Samples for IntegrationRuntimes SyncCredentials.
 */
public final class IntegrationRuntimesSyncCredentialsSamples {
    /*
     * x-ms-original-file: 2018-06-01/IntegrationRuntimes_SyncCredentials.json
     */
    /**
     * Sample code: IntegrationRuntimes_SyncCredentials.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void
        integrationRuntimesSyncCredentials(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.integrationRuntimes()
            .syncCredentialsWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleIntegrationRuntime",
                com.azure.core.util.Context.NONE);
    }
}
```

### IntegrationRuntimes_Update

```java
import com.azure.resourcemanager.datafactory.models.IntegrationRuntimeAutoUpdate;
import com.azure.resourcemanager.datafactory.models.IntegrationRuntimeResource;

/**
 * Samples for IntegrationRuntimes Update.
 */
public final class IntegrationRuntimesUpdateSamples {
    /*
     * x-ms-original-file: 2018-06-01/IntegrationRuntimes_Update.json
     */
    /**
     * Sample code: IntegrationRuntimes_Update.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void integrationRuntimesUpdate(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        IntegrationRuntimeResource resource = manager.integrationRuntimes()
            .getWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleIntegrationRuntime", null,
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withAutoUpdate(IntegrationRuntimeAutoUpdate.OFF).withUpdateDelayOffset("\"PT3H\"").apply();
    }
}
```

### IntegrationRuntimes_Upgrade

```java
/**
 * Samples for IntegrationRuntimes Upgrade.
 */
public final class IntegrationRuntimesUpgradeSamples {
    /*
     * x-ms-original-file: 2018-06-01/IntegrationRuntimes_Upgrade.json
     */
    /**
     * Sample code: IntegrationRuntimes_Upgrade.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void integrationRuntimesUpgrade(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.integrationRuntimes()
            .upgradeWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleIntegrationRuntime",
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

/**
 * Samples for LinkedServices CreateOrUpdate.
 */
public final class LinkedServicesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2018-06-01/LinkedServices_Create.json
     */
    /**
     * Sample code: LinkedServices_Create.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void linkedServicesCreate(com.azure.resourcemanager.datafactory.DataFactoryManager manager)
        throws IOException {
        manager.linkedServices()
            .define("exampleLinkedService")
            .withExistingFactory("exampleResourceGroup", "exampleFactoryName")
            .withProperties(new AzureStorageLinkedService().withConnectionString(SerializerFactory
                .createDefaultManagementSerializerAdapter()
                .deserialize(
                    "{\"type\":\"SecureString\",\"value\":\"DefaultEndpointsProtocol=https;AccountName=examplestorageaccount;AccountKey=<storage key>\"}",
                    Object.class, SerializerEncoding.JSON)))
            .create();
    }

    /*
     * x-ms-original-file: 2018-06-01/LinkedServices_Update.json
     */
    /**
     * Sample code: LinkedServices_Update.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void linkedServicesUpdate(com.azure.resourcemanager.datafactory.DataFactoryManager manager)
        throws IOException {
        LinkedServiceResource resource = manager.linkedServices()
            .getWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleLinkedService", null,
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new AzureStorageLinkedService().withDescription("Example description")
                .withConnectionString(SerializerFactory.createDefaultManagementSerializerAdapter()
                    .deserialize(
                        "{\"type\":\"SecureString\",\"value\":\"DefaultEndpointsProtocol=https;AccountName=examplestorageaccount;AccountKey=<storage key>\"}",
                        Object.class, SerializerEncoding.JSON)))
            .apply();
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
     * x-ms-original-file: 2018-06-01/LinkedServices_Delete.json
     */
    /**
     * Sample code: LinkedServices_Delete.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void linkedServicesDelete(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.linkedServices()
            .deleteWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleLinkedService",
                com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2018-06-01/LinkedServices_Get.json
     */
    /**
     * Sample code: LinkedServices_Get.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void linkedServicesGet(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.linkedServices()
            .getWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleLinkedService", null,
                com.azure.core.util.Context.NONE);
    }
}
```

### LinkedServices_ListByFactory

```java
/**
 * Samples for LinkedServices ListByFactory.
 */
public final class LinkedServicesListByFactorySamples {
    /*
     * x-ms-original-file: 2018-06-01/LinkedServices_ListByFactory.json
     */
    /**
     * Sample code: LinkedServices_ListByFactory.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void linkedServicesListByFactory(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.linkedServices()
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

/**
 * Samples for ManagedPrivateEndpoints CreateOrUpdate.
 */
public final class ManagedPrivateEndpointsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2018-06-01/ManagedPrivateEndpoints_Create.json
     */
    /**
     * Sample code: ManagedVirtualNetworks_Create.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void managedVirtualNetworksCreate(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.managedPrivateEndpoints()
            .define("exampleManagedPrivateEndpointName")
            .withExistingManagedVirtualNetwork("exampleResourceGroup", "exampleFactoryName",
                "exampleManagedVirtualNetworkName")
            .withProperties(new ManagedPrivateEndpoint().withFqdns(Arrays.asList())
                .withGroupId("blob")
                .withPrivateLinkResourceId(
                    "/subscriptions/12345678-1234-1234-1234-123456789012/resourceGroups/exampleResourceGroup/providers/Microsoft.Storage/storageAccounts/exampleBlobStorage")
                .withAdditionalProperties(mapOf()))
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

### ManagedPrivateEndpoints_Delete

```java
/**
 * Samples for ManagedPrivateEndpoints Delete.
 */
public final class ManagedPrivateEndpointsDeleteSamples {
    /*
     * x-ms-original-file: 2018-06-01/ManagedPrivateEndpoints_Delete.json
     */
    /**
     * Sample code: ManagedVirtualNetworks_Delete.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void managedVirtualNetworksDelete(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.managedPrivateEndpoints()
            .deleteWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleManagedVirtualNetworkName",
                "exampleManagedPrivateEndpointName", com.azure.core.util.Context.NONE);
    }
}
```

### ManagedPrivateEndpoints_Get

```java
/**
 * Samples for ManagedPrivateEndpoints Get.
 */
public final class ManagedPrivateEndpointsGetSamples {
    /*
     * x-ms-original-file: 2018-06-01/ManagedPrivateEndpoints_Get.json
     */
    /**
     * Sample code: ManagedPrivateEndpoints_Get.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void managedPrivateEndpointsGet(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.managedPrivateEndpoints()
            .getWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleManagedVirtualNetworkName",
                "exampleManagedPrivateEndpointName", null, com.azure.core.util.Context.NONE);
    }
}
```

### ManagedPrivateEndpoints_ListByFactory

```java
/**
 * Samples for ManagedPrivateEndpoints ListByFactory.
 */
public final class ManagedPrivateEndpointsListByFactorySamples {
    /*
     * x-ms-original-file: 2018-06-01/ManagedPrivateEndpoints_ListByFactory.json
     */
    /**
     * Sample code: ManagedPrivateEndpoints_ListByFactory.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void
        managedPrivateEndpointsListByFactory(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.managedPrivateEndpoints()
            .listByFactory("exampleResourceGroup", "exampleFactoryName", "exampleManagedVirtualNetworkName",
                com.azure.core.util.Context.NONE);
    }
}
```

### ManagedVirtualNetworks_CreateOrUpdate

```java
import com.azure.resourcemanager.datafactory.models.ManagedVirtualNetwork;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ManagedVirtualNetworks CreateOrUpdate.
 */
public final class ManagedVirtualNetworksCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2018-06-01/ManagedVirtualNetworks_Create.json
     */
    /**
     * Sample code: ManagedVirtualNetworks_Create.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void managedVirtualNetworksCreate(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.managedVirtualNetworks()
            .define("exampleManagedVirtualNetworkName")
            .withExistingFactory("exampleResourceGroup", "exampleFactoryName")
            .withProperties(new ManagedVirtualNetwork().withAdditionalProperties(mapOf()))
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

### ManagedVirtualNetworks_Get

```java
/**
 * Samples for ManagedVirtualNetworks Get.
 */
public final class ManagedVirtualNetworksGetSamples {
    /*
     * x-ms-original-file: 2018-06-01/ManagedVirtualNetworks_Get.json
     */
    /**
     * Sample code: ManagedVirtualNetworks_Get.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void managedVirtualNetworksGet(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.managedVirtualNetworks()
            .getWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleManagedVirtualNetworkName", null,
                com.azure.core.util.Context.NONE);
    }
}
```

### ManagedVirtualNetworks_ListByFactory

```java
/**
 * Samples for ManagedVirtualNetworks ListByFactory.
 */
public final class ManagedVirtualNetworksListByFactorySamples {
    /*
     * x-ms-original-file: 2018-06-01/ManagedVirtualNetworks_ListByFactory.json
     */
    /**
     * Sample code: ManagedVirtualNetworks_ListByFactory.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void
        managedVirtualNetworksListByFactory(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.managedVirtualNetworks()
            .listByFactory("exampleResourceGroup", "exampleFactoryName", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2018-06-01/Operations_List.json
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
/**
 * Samples for PipelineRuns Cancel.
 */
public final class PipelineRunsCancelSamples {
    /*
     * x-ms-original-file: 2018-06-01/PipelineRuns_Cancel.json
     */
    /**
     * Sample code: PipelineRuns_Cancel.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void pipelineRunsCancel(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.pipelineRuns()
            .cancelWithResponse("exampleResourceGroup", "exampleFactoryName", "16ac5348-ff82-4f95-a80d-638c1d47b721",
                null, com.azure.core.util.Context.NONE);
    }
}
```

### PipelineRuns_Get

```java
/**
 * Samples for PipelineRuns Get.
 */
public final class PipelineRunsGetSamples {
    /*
     * x-ms-original-file: 2018-06-01/PipelineRuns_Get.json
     */
    /**
     * Sample code: PipelineRuns_Get.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void pipelineRunsGet(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.pipelineRuns()
            .getWithResponse("exampleResourceGroup", "exampleFactoryName", "2f7fdb90-5df1-4b8e-ac2f-064cfa58202b",
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

/**
 * Samples for PipelineRuns QueryByFactory.
 */
public final class PipelineRunsQueryByFactorySamples {
    /*
     * x-ms-original-file: 2018-06-01/PipelineRuns_QueryByFactory.json
     */
    /**
     * Sample code: PipelineRuns_QueryByFactory.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void pipelineRunsQueryByFactory(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.pipelineRuns()
            .queryByFactoryWithResponse("exampleResourceGroup", "exampleFactoryName",
                new RunFilterParameters().withLastUpdatedAfter(OffsetDateTime.parse("2018-06-16T00:36:44.3345758Z"))
                    .withLastUpdatedBefore(OffsetDateTime.parse("2018-06-16T00:49:48.3686473Z"))
                    .withFilters(Arrays.asList(new RunQueryFilter().withOperand(RunQueryFilterOperand.PIPELINE_NAME)
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
import com.azure.resourcemanager.datafactory.models.DatasetReferenceType;
import com.azure.resourcemanager.datafactory.models.Expression;
import com.azure.resourcemanager.datafactory.models.ExpressionType;
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

/**
 * Samples for Pipelines CreateOrUpdate.
 */
public final class PipelinesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2018-06-01/Pipelines_Create.json
     */
    /**
     * Sample code: Pipelines_Create.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void pipelinesCreate(com.azure.resourcemanager.datafactory.DataFactoryManager manager)
        throws IOException {
        manager.pipelines()
            .define("examplePipeline")
            .withExistingFactory("exampleResourceGroup", "exampleFactoryName")
            .withActivities(Arrays.asList(new ForEachActivity().withName("ExampleForeachActivity")
                .withIsSequential(true)
                .withItems(new Expression().withType(ExpressionType.EXPRESSION)
                    .withValue("@pipeline().parameters.OutputBlobNameList"))
                .withActivities(Arrays.asList(new CopyActivity().withName("ExampleCopyActivity")
                    .withInputs(Arrays.asList(new DatasetReference().withType(DatasetReferenceType.DATASET_REFERENCE)
                        .withReferenceName("exampleDataset")
                        .withParameters(
                            mapOf("MyFileName", "examplecontainer.csv", "MyFolderPath", "examplecontainer"))))
                    .withOutputs(Arrays.asList(new DatasetReference().withType(DatasetReferenceType.DATASET_REFERENCE)
                        .withReferenceName("exampleDataset")
                        .withParameters(mapOf("MyFileName",
                            SerializerFactory.createDefaultManagementSerializerAdapter()
                                .deserialize("{\"type\":\"Expression\",\"value\":\"@item()\"}", Object.class,
                                    SerializerEncoding.JSON),
                            "MyFolderPath", "examplecontainer"))))
                    .withSource(new BlobSource())
                    .withSink(new BlobSink())
                    .withDataIntegrationUnits(32)))))
            .withParameters(mapOf("JobId", new ParameterSpecification().withType(ParameterType.STRING),
                "OutputBlobNameList", new ParameterSpecification().withType(ParameterType.ARRAY)))
            .withVariables(mapOf("TestVariableArray", new VariableSpecification().withType(VariableType.ARRAY)))
            .withRunDimensions(mapOf("JobId",
                SerializerFactory.createDefaultManagementSerializerAdapter()
                    .deserialize("{\"type\":\"Expression\",\"value\":\"@pipeline().parameters.JobId\"}", Object.class,
                        SerializerEncoding.JSON)))
            .withPolicy(new PipelinePolicy()
                .withElapsedTimeMetric(new PipelineElapsedTimeMetricPolicy().withDuration("0.00:10:00")))
            .create();
    }

    /*
     * x-ms-original-file: 2018-06-01/Pipelines_Update.json
     */
    /**
     * Sample code: Pipelines_Update.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void pipelinesUpdate(com.azure.resourcemanager.datafactory.DataFactoryManager manager)
        throws IOException {
        PipelineResource resource = manager.pipelines()
            .getWithResponse("exampleResourceGroup", "exampleFactoryName", "examplePipeline", null,
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withDescription("Example description")
            .withActivities(Arrays.asList(new ForEachActivity().withName("ExampleForeachActivity")
                .withIsSequential(true)
                .withItems(new Expression().withType(ExpressionType.EXPRESSION)
                    .withValue("@pipeline().parameters.OutputBlobNameList"))
                .withActivities(Arrays.asList(new CopyActivity().withName("ExampleCopyActivity")
                    .withInputs(Arrays.asList(new DatasetReference().withType(DatasetReferenceType.DATASET_REFERENCE)
                        .withReferenceName("exampleDataset")
                        .withParameters(
                            mapOf("MyFileName", "examplecontainer.csv", "MyFolderPath", "examplecontainer"))))
                    .withOutputs(Arrays.asList(new DatasetReference().withType(DatasetReferenceType.DATASET_REFERENCE)
                        .withReferenceName("exampleDataset")
                        .withParameters(mapOf("MyFileName",
                            SerializerFactory.createDefaultManagementSerializerAdapter()
                                .deserialize("{\"type\":\"Expression\",\"value\":\"@item()\"}", Object.class,
                                    SerializerEncoding.JSON),
                            "MyFolderPath", "examplecontainer"))))
                    .withSource(new BlobSource())
                    .withSink(new BlobSink())
                    .withDataIntegrationUnits(32)))))
            .withParameters(mapOf("OutputBlobNameList", new ParameterSpecification().withType(ParameterType.ARRAY)))
            .withPolicy(new PipelinePolicy()
                .withElapsedTimeMetric(new PipelineElapsedTimeMetricPolicy().withDuration("0.00:10:00")))
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

### Pipelines_CreateRun

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Pipelines CreateRun.
 */
public final class PipelinesCreateRunSamples {
    /*
     * x-ms-original-file: 2018-06-01/Pipelines_CreateRun.json
     */
    /**
     * Sample code: Pipelines_CreateRun.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void pipelinesCreateRun(com.azure.resourcemanager.datafactory.DataFactoryManager manager)
        throws IOException {
        manager.pipelines()
            .createRunWithResponse("exampleResourceGroup", "exampleFactoryName", "examplePipeline", null, null, null,
                null,
                mapOf("OutputBlobNameList",
                    SerializerFactory.createDefaultManagementSerializerAdapter()
                        .deserialize("[\"exampleoutput.csv\"]", Object.class, SerializerEncoding.JSON)),
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

### Pipelines_Delete

```java
/**
 * Samples for Pipelines Delete.
 */
public final class PipelinesDeleteSamples {
    /*
     * x-ms-original-file: 2018-06-01/Pipelines_Delete.json
     */
    /**
     * Sample code: Pipelines_Delete.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void pipelinesDelete(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.pipelines()
            .deleteWithResponse("exampleResourceGroup", "exampleFactoryName", "examplePipeline",
                com.azure.core.util.Context.NONE);
    }
}
```

### Pipelines_Get

```java
/**
 * Samples for Pipelines Get.
 */
public final class PipelinesGetSamples {
    /*
     * x-ms-original-file: 2018-06-01/Pipelines_Get.json
     */
    /**
     * Sample code: Pipelines_Get.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void pipelinesGet(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.pipelines()
            .getWithResponse("exampleResourceGroup", "exampleFactoryName", "examplePipeline", null,
                com.azure.core.util.Context.NONE);
    }
}
```

### Pipelines_ListByFactory

```java
/**
 * Samples for Pipelines ListByFactory.
 */
public final class PipelinesListByFactorySamples {
    /*
     * x-ms-original-file: 2018-06-01/Pipelines_ListByFactory.json
     */
    /**
     * Sample code: Pipelines_ListByFactory.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void pipelinesListByFactory(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.pipelines()
            .listByFactory("exampleResourceGroup", "exampleFactoryName", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndPointConnections_ListByFactory

```java
/**
 * Samples for PrivateEndPointConnections ListByFactory.
 */
public final class PrivateEndPointConnectionsListByFactorySamples {
    /*
     * x-ms-original-file: 2018-06-01/PrivateEndPointConnections_ListByFactory.json
     */
    /**
     * Sample code: privateEndPointConnections_ListByFactory.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void
        privateEndPointConnectionsListByFactory(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.privateEndPointConnections()
            .listByFactory("exampleResourceGroup", "exampleFactoryName", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkResources_Get

```java
/**
 * Samples for PrivateLinkResources Get.
 */
public final class PrivateLinkResourcesGetSamples {
    /*
     * x-ms-original-file: 2018-06-01/GetPrivateLinkResources.json
     */
    /**
     * Sample code: Get private link resources of a site.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void
        getPrivateLinkResourcesOfASite(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.privateLinkResources()
            .getWithResponse("exampleResourceGroup", "exampleFactoryName", com.azure.core.util.Context.NONE);
    }
}
```

### TriggerRuns_Cancel

```java
/**
 * Samples for TriggerRuns Cancel.
 */
public final class TriggerRunsCancelSamples {
    /*
     * x-ms-original-file: 2018-06-01/TriggerRuns_Cancel.json
     */
    /**
     * Sample code: Triggers_Cancel.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void triggersCancel(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.triggerRuns()
            .cancelWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleTrigger",
                "2f7fdb90-5df1-4b8e-ac2f-064cfa58202b", com.azure.core.util.Context.NONE);
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

/**
 * Samples for TriggerRuns QueryByFactory.
 */
public final class TriggerRunsQueryByFactorySamples {
    /*
     * x-ms-original-file: 2018-06-01/TriggerRuns_QueryByFactory.json
     */
    /**
     * Sample code: TriggerRuns_QueryByFactory.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void triggerRunsQueryByFactory(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.triggerRuns()
            .queryByFactoryWithResponse("exampleResourceGroup", "exampleFactoryName",
                new RunFilterParameters().withLastUpdatedAfter(OffsetDateTime.parse("2018-06-16T00:36:44.3345758Z"))
                    .withLastUpdatedBefore(OffsetDateTime.parse("2018-06-16T00:49:48.3686473Z"))
                    .withFilters(Arrays.asList(new RunQueryFilter().withOperand(RunQueryFilterOperand.TRIGGER_NAME)
                        .withOperator(RunQueryFilterOperator.EQUALS)
                        .withValues(Arrays.asList("exampleTrigger")))),
                com.azure.core.util.Context.NONE);
    }
}
```

### TriggerRuns_Rerun

```java
/**
 * Samples for TriggerRuns Rerun.
 */
public final class TriggerRunsRerunSamples {
    /*
     * x-ms-original-file: 2018-06-01/TriggerRuns_Rerun.json
     */
    /**
     * Sample code: Triggers_Rerun.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void triggersRerun(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.triggerRuns()
            .rerunWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleTrigger",
                "2f7fdb90-5df1-4b8e-ac2f-064cfa58202b", com.azure.core.util.Context.NONE);
    }
}
```

### Triggers_CreateOrUpdate

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.datafactory.models.PipelineReference;
import com.azure.resourcemanager.datafactory.models.PipelineReferenceType;
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

/**
 * Samples for Triggers CreateOrUpdate.
 */
public final class TriggersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2018-06-01/Triggers_Create.json
     */
    /**
     * Sample code: Triggers_Create.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void triggersCreate(com.azure.resourcemanager.datafactory.DataFactoryManager manager)
        throws IOException {
        manager.triggers()
            .define("exampleTrigger")
            .withExistingFactory("exampleResourceGroup", "exampleFactoryName")
            .withProperties(new ScheduleTrigger()
                .withPipelines(Arrays.asList(new TriggerPipelineReference()
                    .withPipelineReference(new PipelineReference().withType(PipelineReferenceType.PIPELINE_REFERENCE)
                        .withReferenceName("examplePipeline"))
                    .withParameters(mapOf("OutputBlobNameList",
                        SerializerFactory.createDefaultManagementSerializerAdapter()
                            .deserialize("[\"exampleoutput.csv\"]", Object.class, SerializerEncoding.JSON)))))
                .withRecurrence(new ScheduleTriggerRecurrence().withFrequency(RecurrenceFrequency.MINUTE)
                    .withInterval(4)
                    .withStartTime(OffsetDateTime.parse("2018-06-16T00:39:13.8441801Z"))
                    .withEndTime(OffsetDateTime.parse("2018-06-16T00:55:13.8441801Z"))
                    .withTimeZone("UTC")
                    .withAdditionalProperties(mapOf())))
            .create();
    }

    /*
     * x-ms-original-file: 2018-06-01/Triggers_Update.json
     */
    /**
     * Sample code: Triggers_Update.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void triggersUpdate(com.azure.resourcemanager.datafactory.DataFactoryManager manager)
        throws IOException {
        TriggerResource resource = manager.triggers()
            .getWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleTrigger", null,
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new ScheduleTrigger().withDescription("Example description")
                .withPipelines(Arrays.asList(new TriggerPipelineReference()
                    .withPipelineReference(new PipelineReference().withType(PipelineReferenceType.PIPELINE_REFERENCE)
                        .withReferenceName("examplePipeline"))
                    .withParameters(mapOf("OutputBlobNameList",
                        SerializerFactory.createDefaultManagementSerializerAdapter()
                            .deserialize("[\"exampleoutput.csv\"]", Object.class, SerializerEncoding.JSON)))))
                .withRecurrence(new ScheduleTriggerRecurrence().withFrequency(RecurrenceFrequency.MINUTE)
                    .withInterval(4)
                    .withStartTime(OffsetDateTime.parse("2018-06-16T00:39:14.905167Z"))
                    .withEndTime(OffsetDateTime.parse("2018-06-16T00:55:14.905167Z"))
                    .withTimeZone("UTC")
                    .withAdditionalProperties(mapOf())))
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

### Triggers_Delete

```java
/**
 * Samples for Triggers Delete.
 */
public final class TriggersDeleteSamples {
    /*
     * x-ms-original-file: 2018-06-01/Triggers_Delete.json
     */
    /**
     * Sample code: Triggers_Delete.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void triggersDelete(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.triggers()
            .deleteWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleTrigger",
                com.azure.core.util.Context.NONE);
    }
}
```

### Triggers_Get

```java
/**
 * Samples for Triggers Get.
 */
public final class TriggersGetSamples {
    /*
     * x-ms-original-file: 2018-06-01/Triggers_Get.json
     */
    /**
     * Sample code: Triggers_Get.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void triggersGet(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.triggers()
            .getWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleTrigger", null,
                com.azure.core.util.Context.NONE);
    }
}
```

### Triggers_GetEventSubscriptionStatus

```java
/**
 * Samples for Triggers GetEventSubscriptionStatus.
 */
public final class TriggersGetEventSubscriptionStatusSamples {
    /*
     * x-ms-original-file: 2018-06-01/Triggers_GetEventSubscriptionStatus.json
     */
    /**
     * Sample code: Triggers_GetEventSubscriptionStatus.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void
        triggersGetEventSubscriptionStatus(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.triggers()
            .getEventSubscriptionStatusWithResponse("exampleResourceGroup", "exampleFactoryName", "exampleTrigger",
                com.azure.core.util.Context.NONE);
    }
}
```

### Triggers_ListByFactory

```java
/**
 * Samples for Triggers ListByFactory.
 */
public final class TriggersListByFactorySamples {
    /*
     * x-ms-original-file: 2018-06-01/Triggers_ListByFactory.json
     */
    /**
     * Sample code: Triggers_ListByFactory.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void triggersListByFactory(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.triggers()
            .listByFactory("exampleResourceGroup", "exampleFactoryName", com.azure.core.util.Context.NONE);
    }
}
```

### Triggers_QueryByFactory

```java
import com.azure.resourcemanager.datafactory.models.TriggerFilterParameters;

/**
 * Samples for Triggers QueryByFactory.
 */
public final class TriggersQueryByFactorySamples {
    /*
     * x-ms-original-file: 2018-06-01/Triggers_QueryByFactory.json
     */
    /**
     * Sample code: Triggers_QueryByFactory.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void triggersQueryByFactory(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.triggers()
            .queryByFactoryWithResponse("exampleResourceGroup", "exampleFactoryName",
                new TriggerFilterParameters().withParentTriggerName("exampleTrigger"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Triggers_Start

```java
/**
 * Samples for Triggers Start.
 */
public final class TriggersStartSamples {
    /*
     * x-ms-original-file: 2018-06-01/Triggers_Start.json
     */
    /**
     * Sample code: Triggers_Start.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void triggersStart(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.triggers()
            .start("exampleResourceGroup", "exampleFactoryName", "exampleTrigger", com.azure.core.util.Context.NONE);
    }
}
```

### Triggers_Stop

```java
/**
 * Samples for Triggers Stop.
 */
public final class TriggersStopSamples {
    /*
     * x-ms-original-file: 2018-06-01/Triggers_Stop.json
     */
    /**
     * Sample code: Triggers_Stop.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void triggersStop(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.triggers()
            .stop("exampleResourceGroup", "exampleFactoryName", "exampleTrigger", com.azure.core.util.Context.NONE);
    }
}
```

### Triggers_SubscribeToEvents

```java
/**
 * Samples for Triggers SubscribeToEvents.
 */
public final class TriggersSubscribeToEventsSamples {
    /*
     * x-ms-original-file: 2018-06-01/Triggers_SubscribeToEvents.json
     */
    /**
     * Sample code: Triggers_SubscribeToEvents.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void triggersSubscribeToEvents(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.triggers()
            .subscribeToEvents("exampleResourceGroup", "exampleFactoryName", "exampleTrigger",
                com.azure.core.util.Context.NONE);
    }
}
```

### Triggers_UnsubscribeFromEvents

```java
/**
 * Samples for Triggers UnsubscribeFromEvents.
 */
public final class TriggersUnsubscribeFromEventsSamples {
    /*
     * x-ms-original-file: 2018-06-01/Triggers_UnsubscribeFromEvents.json
     */
    /**
     * Sample code: Triggers_UnsubscribeFromEvents.
     * 
     * @param manager Entry point to DataFactoryManager.
     */
    public static void triggersUnsubscribeFromEvents(com.azure.resourcemanager.datafactory.DataFactoryManager manager) {
        manager.triggers()
            .unsubscribeFromEvents("exampleResourceGroup", "exampleFactoryName", "exampleTrigger",
                com.azure.core.util.Context.NONE);
    }
}
```

