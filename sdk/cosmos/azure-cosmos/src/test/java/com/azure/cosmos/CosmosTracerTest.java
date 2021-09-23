// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.core.util.Context;
import com.azure.core.util.tracing.Tracer;
import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.FeedResponseDiagnostics;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers.CosmosDiagnosticsHelper;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers.CosmosDiagnosticsHelper.CosmosDiagnosticsAccessor;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.LifeCycleUtils;
import com.azure.cosmos.implementation.QueryMetrics;
import com.azure.cosmos.implementation.RequestTimeline;
import com.azure.cosmos.implementation.SerializationDiagnosticsContext;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.TracerProvider;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.directconnectivity.DirectBridgeInternal;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosDatabaseProperties;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosStoredProcedureProperties;
import com.azure.cosmos.models.CosmosStoredProcedureResponse;
import com.azure.cosmos.models.CosmosTriggerProperties;
import com.azure.cosmos.models.CosmosTriggerResponse;
import com.azure.cosmos.models.CosmosUserDefinedFunctionProperties;
import com.azure.cosmos.models.CosmosUserDefinedFunctionResponse;
import com.azure.cosmos.models.CosmosUserProperties;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.models.TriggerOperation;
import com.azure.cosmos.models.TriggerType;
import com.azure.cosmos.rx.TestSuiteBase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class CosmosTracerTest extends TestSuiteBase {
    private final static ObjectMapper OBJECT_MAPPER = Utils.getSimpleObjectMapper();
    private static final String ITEM_ID = "tracerDoc";
    private CosmosDiagnosticsAccessor cosmosDiagnosticsAccessor;
    CosmosAsyncClient client;
    CosmosAsyncDatabase cosmosAsyncDatabase;
    CosmosAsyncContainer cosmosAsyncContainer;

    @BeforeClass(groups = {"emulator"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .directMode(DirectConnectionConfig.getDefaultConfig())
            .buildAsyncClient();
        cosmosAsyncDatabase = getSharedCosmosDatabase(client);
        cosmosAsyncContainer = getSharedMultiPartitionCosmosContainer(client);
        cosmosDiagnosticsAccessor = CosmosDiagnosticsHelper.getCosmosDiagnosticsAccessor();

    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void cosmosAsyncClient() throws Exception {
        Tracer mockTracer = getMockTracer();
        TracerProvider tracerProvider = Mockito.spy(new TracerProvider(mockTracer));
        ReflectionUtils.setTracerProvider(client, tracerProvider);
        setThreshHoldDurationOnTracer(tracerProvider, Duration.ZERO, "CRUD_THRESHOLD_FOR_DIAGNOSTICS");
        setThreshHoldDurationOnTracer(tracerProvider, Duration.ZERO, "QUERY_THRESHOLD_FOR_DIAGNOSTICS");

        int traceApiCounter = 1;

        TracerProviderCapture tracerProviderCapture = new TracerProviderCapture();
        AddEventCapture addEventCapture = new AddEventCapture();

        Mockito.doAnswer(tracerProviderCapture).when(tracerProvider).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.doAnswer(addEventCapture).when(tracerProvider).addEvent(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any());

        CosmosDatabaseResponse cosmosDatabaseResponse = client.createDatabaseIfNotExists(cosmosAsyncDatabase.getId(),
            ThroughputProperties.createManualThroughput(5000)).block();
        Context context = tracerProviderCapture.getResult();
        Map<String, Map<String, Object>> attributesMap = addEventCapture.getAttributesMap();
        verifyTracerAttributes(tracerProvider, mockTracer, "createDatabaseIfNotExists." + cosmosAsyncDatabase.getId()
            , context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null, cosmosDatabaseResponse.getDiagnostics(), attributesMap);
        traceApiCounter++;

        FeedResponse<CosmosDatabaseProperties> feedResponseReadAllDatabases =
            client.readAllDatabases(new CosmosQueryRequestOptions()).byPage().single().block();
        verifyTracerAttributes(tracerProvider, mockTracer, "readAllDatabases", context, null, traceApiCounter, null,
            feedResponseReadAllDatabases.getCosmosDiagnostics(), attributesMap);
        traceApiCounter++;


        String query = "select * from c where c.id = '" + cosmosAsyncDatabase.getId() + "'";
        FeedResponse<CosmosDatabaseProperties> feedResponseQueryDatabases = client.queryDatabases(query,
            new CosmosQueryRequestOptions()).byPage().single().block();
        verifyTracerAttributes(tracerProvider, mockTracer, "queryDatabases", context, null, traceApiCounter, null,
            feedResponseQueryDatabases.getCosmosDiagnostics(), attributesMap);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void cosmosAsyncDatabase() throws Exception {
        Tracer mockTracer = getMockTracer();
        TracerProvider tracerProvider = Mockito.spy(new TracerProvider(mockTracer));
        ReflectionUtils.setTracerProvider(client, tracerProvider);
        setThreshHoldDurationOnTracer(tracerProvider, Duration.ZERO, "CRUD_THRESHOLD_FOR_DIAGNOSTICS");
        setThreshHoldDurationOnTracer(tracerProvider, Duration.ZERO, "QUERY_THRESHOLD_FOR_DIAGNOSTICS");
        TracerProviderCapture tracerProviderCapture = new TracerProviderCapture();
        AddEventCapture addEventCapture = new AddEventCapture();

        Mockito.doAnswer(tracerProviderCapture).when(tracerProvider).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.doAnswer(addEventCapture).when(tracerProvider).addEvent(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any());

        int traceApiCounter = 1;
        CosmosContainerResponse containerResponse =
            cosmosAsyncDatabase.createContainerIfNotExists(cosmosAsyncContainer.getId(),
            "/pk", 5000).block();
        Context context = tracerProviderCapture.getResult();
        Map<String, Map<String, Object>> attributesMap = addEventCapture.getAttributesMap();
        verifyTracerAttributes(tracerProvider, mockTracer,
            "createContainerIfNotExists." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null, containerResponse.getDiagnostics(), attributesMap);
        traceApiCounter++;

        FeedResponse<CosmosUserProperties> userPropertiesFeedResponse =
            cosmosAsyncDatabase.readAllUsers().byPage().single().block();
        verifyTracerAttributes(tracerProvider, mockTracer, "readAllUsers." + cosmosAsyncDatabase.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null, userPropertiesFeedResponse.getCosmosDiagnostics(),
            attributesMap);
        traceApiCounter++;

        FeedResponse<CosmosContainerProperties> containerPropertiesFeedResponse =
            cosmosAsyncDatabase.readAllContainers().byPage().single().block();
        verifyTracerAttributes(tracerProvider, mockTracer, "readAllContainers." + cosmosAsyncDatabase.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null,
            containerPropertiesFeedResponse.getCosmosDiagnostics(), attributesMap);
        traceApiCounter++;

        String errorType = null;
        CosmosDiagnostics cosmosDiagnostics = null;
        try {
            cosmosDiagnostics = cosmosAsyncDatabase.readThroughput().block().getDiagnostics();
        } catch (CosmosException ex) {
            cosmosDiagnostics = ex.getDiagnostics();
            errorType = ex.getClass().getName();
        }

        verifyTracerAttributes(tracerProvider, mockTracer, "readThroughput." + cosmosAsyncDatabase.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, errorType, cosmosDiagnostics, attributesMap);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void cosmosAsyncContainer() throws Exception {
        Tracer mockTracer = getMockTracer();
        TracerProvider tracerProvider = Mockito.spy(new TracerProvider(mockTracer));
        setThreshHoldDurationOnTracer(tracerProvider, Duration.ZERO, "CRUD_THRESHOLD_FOR_DIAGNOSTICS");
        setThreshHoldDurationOnTracer(tracerProvider, Duration.ZERO, "QUERY_THRESHOLD_FOR_DIAGNOSTICS");
        ReflectionUtils.setTracerProvider(client, tracerProvider);
        TracerProviderCapture tracerProviderCapture = new TracerProviderCapture();
        AddEventCapture addEventCapture = new AddEventCapture();

        Mockito.doAnswer(tracerProviderCapture).when(tracerProvider).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.doAnswer(addEventCapture).when(tracerProvider).addEvent(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any());

        int traceApiCounter = 1;
        CosmosContainerResponse containerResponse = cosmosAsyncContainer.read().block();
        Context context = tracerProviderCapture.getResult();
        Map<String, Map<String, Object>> attributesMap = addEventCapture.getAttributesMap();
        Mockito.verify(tracerProvider, Mockito.times(traceApiCounter)).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(Context.class));
        verifyTracerAttributes(tracerProvider, mockTracer, "readContainer." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null, containerResponse.getDiagnostics(), attributesMap);
        traceApiCounter++;

        CosmosDiagnostics cosmosDiagnostics = null;
        try {
            cosmosDiagnostics = cosmosAsyncContainer.readThroughput().block().getDiagnostics();
        } catch (CosmosException ex) {
            //do nothing
        }
        verifyTracerAttributes(tracerProvider, mockTracer, "readThroughput." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null, cosmosDiagnostics, attributesMap);
        traceApiCounter++;

        setThreshHoldDurationOnTracer(tracerProvider, Duration.ZERO, "CRUD_THRESHOLD_FOR_DIAGNOSTICS");
        setThreshHoldDurationOnTracer(tracerProvider, Duration.ZERO, "QUERY_THRESHOLD_FOR_DIAGNOSTICS");

        CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions();
        requestOptions.setThresholdForDiagnosticsOnTracer(Duration.ZERO);
        InternalObjectNode item = new InternalObjectNode();
        item.setId(ITEM_ID);
        CosmosItemResponse<InternalObjectNode> cosmosItemResponse = cosmosAsyncContainer.createItem(item,
            requestOptions).block();
        verifyTracerAttributes(tracerProvider, mockTracer, "createItem." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null, cosmosItemResponse.getDiagnostics(), attributesMap);
        traceApiCounter++;

        cosmosItemResponse = cosmosAsyncContainer.upsertItem(item,
            requestOptions).block();
        verifyTracerAttributes(tracerProvider, mockTracer, "upsertItem." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null, cosmosItemResponse.getDiagnostics(), attributesMap);
        traceApiCounter++;

        cosmosItemResponse = cosmosAsyncContainer.readItem(ITEM_ID, PartitionKey.NONE, requestOptions,
            InternalObjectNode.class).block();
        verifyTracerAttributes(tracerProvider, mockTracer, "readItem." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null, cosmosItemResponse.getDiagnostics(), attributesMap);
        traceApiCounter++;

        CosmosItemResponse<Object> deleteItemResponse = cosmosAsyncContainer.deleteItem(ITEM_ID, PartitionKey.NONE,
            requestOptions).block();
        verifyTracerAttributes(tracerProvider, mockTracer, "deleteItem." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null, deleteItemResponse.getDiagnostics(), attributesMap);
        traceApiCounter++;

        CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions();
        queryRequestOptions.setThresholdForDiagnosticsOnTracer(Duration.ZERO);
        FeedResponse<InternalObjectNode> feedItemResponse = cosmosAsyncContainer.readAllItems(queryRequestOptions,
            InternalObjectNode.class).byPage().single().block();
        Mockito.verify(tracerProvider, Mockito.times(traceApiCounter)).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(Context.class));
        verifyTracerAttributes(tracerProvider, mockTracer, "readAllItems." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null, feedItemResponse.getCosmosDiagnostics(), attributesMap);
        traceApiCounter++;

        String query = "select * from c where c.id = '" + ITEM_ID + "'";
        feedItemResponse =
            cosmosAsyncContainer.queryItems(query, queryRequestOptions, InternalObjectNode.class).byPage().single().block();
        verifyTracerAttributes(tracerProvider, mockTracer, "queryItems." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null, feedItemResponse.getCosmosDiagnostics(), attributesMap);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void cosmosAsyncScripts() throws Exception {
        Tracer mockTracer = getMockTracer();
        TracerProvider tracerProvider = Mockito.spy(new TracerProvider(mockTracer));
        ReflectionUtils.setTracerProvider(client, tracerProvider);
        setThreshHoldDurationOnTracer(tracerProvider, Duration.ZERO, "CRUD_THRESHOLD_FOR_DIAGNOSTICS");
        setThreshHoldDurationOnTracer(tracerProvider, Duration.ZERO, "QUERY_THRESHOLD_FOR_DIAGNOSTICS");
        TracerProviderCapture tracerProviderCapture = new TracerProviderCapture();
        AddEventCapture addEventCapture = new AddEventCapture();

        Mockito.doAnswer(tracerProviderCapture).when(tracerProvider).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.doAnswer(addEventCapture).when(tracerProvider).addEvent(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any());

        int traceApiCounter = 1;
        FeedResponse<CosmosStoredProcedureProperties> sprocFeedResponse =
            cosmosAsyncContainer.getScripts().readAllStoredProcedures(new CosmosQueryRequestOptions()).byPage().single().block();
        Context context = tracerProviderCapture.getResult();
        Map<String, Map<String, Object>> attributesMap = addEventCapture.getAttributesMap();
        verifyTracerAttributes(tracerProvider, mockTracer, "readAllStoredProcedures." + cosmosAsyncContainer.getId(),
            context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null, sprocFeedResponse.getCosmosDiagnostics(),
            attributesMap);
        traceApiCounter++;

        FeedResponse<CosmosTriggerProperties> triggerFeedResponse =
            cosmosAsyncContainer.getScripts().readAllTriggers(new CosmosQueryRequestOptions()).byPage().single().block();
        verifyTracerAttributes(tracerProvider, mockTracer, "readAllTriggers." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null, triggerFeedResponse.getCosmosDiagnostics(),
            attributesMap);
        traceApiCounter++;

        FeedResponse<CosmosUserDefinedFunctionProperties> udfFeedResponse =
            cosmosAsyncContainer.getScripts().readAllUserDefinedFunctions(new CosmosQueryRequestOptions()).byPage().single().block();
        verifyTracerAttributes(tracerProvider, mockTracer,
            "readAllUserDefinedFunctions." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null, udfFeedResponse.getCosmosDiagnostics(), attributesMap);
        traceApiCounter++;

        CosmosUserDefinedFunctionProperties cosmosUserDefinedFunctionProperties =
            getCosmosUserDefinedFunctionProperties();
        CosmosUserDefinedFunctionResponse resultUdf =
            cosmosAsyncContainer.getScripts().createUserDefinedFunction(cosmosUserDefinedFunctionProperties).block();
        verifyTracerAttributes(tracerProvider, mockTracer,
            "createUserDefinedFunction." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null, resultUdf.getDiagnostics(), attributesMap);
        traceApiCounter++;

        resultUdf =
            cosmosAsyncContainer.getScripts().getUserDefinedFunction(cosmosUserDefinedFunctionProperties.getId()).read().block();
        verifyTracerAttributes(tracerProvider, mockTracer, "readUserDefinedFunction." + cosmosAsyncContainer.getId(),
            context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null, resultUdf.getDiagnostics(), attributesMap);
        traceApiCounter++;

        cosmosUserDefinedFunctionProperties.setBody("function() {var x = 15;}");
        resultUdf =
            cosmosAsyncContainer.getScripts().getUserDefinedFunction(resultUdf.getProperties().getId()).replace(resultUdf.getProperties()).block();
        verifyTracerAttributes(tracerProvider, mockTracer,
            "replaceUserDefinedFunction." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null, resultUdf.getDiagnostics(), attributesMap);
        traceApiCounter++;

        cosmosAsyncContainer.getScripts().readAllUserDefinedFunctions(new CosmosQueryRequestOptions()).byPage().single().block();
        Mockito.verify(tracerProvider, Mockito.times(traceApiCounter)).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(Context.class));
        traceApiCounter++;

        resultUdf =
            cosmosAsyncContainer.getScripts().getUserDefinedFunction(cosmosUserDefinedFunctionProperties.getId()).delete().block();
        verifyTracerAttributes(tracerProvider, mockTracer,
            "deleteUserDefinedFunction." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null, resultUdf.getDiagnostics(), attributesMap);
        traceApiCounter++;

        CosmosTriggerProperties cosmosTriggerProperties = getCosmosTriggerProperties();
        CosmosTriggerResponse resultTrigger =
            cosmosAsyncContainer.getScripts().createTrigger(cosmosTriggerProperties).block();
        verifyTracerAttributes(tracerProvider, mockTracer, "createTrigger." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null, resultTrigger.getDiagnostics(), attributesMap);
        traceApiCounter++;

        resultTrigger = cosmosAsyncContainer.getScripts().getTrigger(cosmosTriggerProperties.getId()).read().block();
        verifyTracerAttributes(tracerProvider, mockTracer, "readTrigger." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null, resultTrigger.getDiagnostics(), attributesMap);
        traceApiCounter++;

        resultTrigger =
            cosmosAsyncContainer.getScripts().getTrigger(cosmosTriggerProperties.getId()).replace(resultTrigger.getProperties()).block();
        verifyTracerAttributes(tracerProvider, mockTracer, "replaceTrigger." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null, resultTrigger.getDiagnostics(), attributesMap);
        traceApiCounter++;

        cosmosAsyncContainer.getScripts().readAllTriggers(new CosmosQueryRequestOptions()).byPage().single().block();
        Mockito.verify(tracerProvider, Mockito.times(traceApiCounter)).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(Context.class));
        traceApiCounter++;

        resultTrigger = cosmosAsyncContainer.getScripts().getTrigger(cosmosTriggerProperties.getId()).delete().block();
        verifyTracerAttributes(tracerProvider, mockTracer, "deleteTrigger." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null, resultTrigger.getDiagnostics(), attributesMap);
        traceApiCounter++;

        CosmosStoredProcedureProperties procedureProperties = getCosmosStoredProcedureProperties();
        CosmosStoredProcedureResponse resultSproc =
            cosmosAsyncContainer.getScripts().createStoredProcedure(procedureProperties).block();
        verifyTracerAttributes(tracerProvider, mockTracer, "createStoredProcedure." + cosmosAsyncContainer.getId(),
            context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null, resultSproc.getDiagnostics(), attributesMap);
        traceApiCounter++;

        resultSproc = cosmosAsyncContainer.getScripts().getStoredProcedure(procedureProperties.getId()).read().block();
        verifyTracerAttributes(tracerProvider, mockTracer, "readStoredProcedure." + cosmosAsyncContainer.getId(),
            context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null, resultSproc.getDiagnostics(), attributesMap);
        traceApiCounter++;

        resultSproc =
            cosmosAsyncContainer.getScripts().getStoredProcedure(procedureProperties.getId()).replace(resultSproc.getProperties()).block();
        verifyTracerAttributes(tracerProvider, mockTracer, "replaceStoredProcedure." + cosmosAsyncContainer.getId(),
            context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null, resultSproc.getDiagnostics(), attributesMap);
        traceApiCounter++;

        cosmosAsyncContainer.getScripts().readAllStoredProcedures(new CosmosQueryRequestOptions()).byPage().single().block();

        resultSproc =
            cosmosAsyncContainer.getScripts().getStoredProcedure(procedureProperties.getId()).delete().block();
        traceApiCounter++;
        verifyTracerAttributes(tracerProvider, mockTracer, "deleteStoredProcedure." + cosmosAsyncContainer.getId(),
            context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null, resultSproc.getDiagnostics(), attributesMap);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void tracerExceptionSpan() throws Exception {
        Tracer mockTracer = getMockTracer();
        TracerProvider tracerProvider = Mockito.spy(new TracerProvider(mockTracer));
        setThreshHoldDurationOnTracer(tracerProvider, Duration.ZERO, "CRUD_THRESHOLD_FOR_DIAGNOSTICS");
        ReflectionUtils.setTracerProvider(client, tracerProvider);
        int traceApiCounter = 1;

        TracerProviderCapture tracerProviderCapture = new TracerProviderCapture();
        AddEventCapture addEventCapture = new AddEventCapture();

        Mockito.doAnswer(tracerProviderCapture).when(tracerProvider).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.doAnswer(addEventCapture).when(tracerProvider).addEvent(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any());

        InternalObjectNode item = new InternalObjectNode();
        item.setId("testDoc");
        CosmosItemResponse<InternalObjectNode> itemResponse = cosmosAsyncContainer.createItem(item).block();
        Context context = tracerProviderCapture.getResult();
        Map<String, Map<String, Object>> attributesMap = addEventCapture.getAttributesMap();
        verifyTracerAttributes(tracerProvider, mockTracer, "createItem." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null, itemResponse.getDiagnostics(), attributesMap);
        traceApiCounter++;

        String errorType = null;
        try {
            PartitionKey partitionKey = new PartitionKey("wrongPk");
            cosmosAsyncContainer.readItem("testDoc", partitionKey, null, InternalObjectNode.class).block();
            fail("readItem should fail due to wrong pk");
        } catch (CosmosException ex) {
            assertThat(ex.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.NOTFOUND);
            errorType = ex.getClass().getName();
        }

        verifyTracerAttributes(tracerProvider, mockTracer, "readItem." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter
            , errorType, null, attributesMap);
        // sending null diagnostics as we don't want diagnostics in events for exception as this information is
        // already there as part of exception message
    }

    @AfterClass(groups = {"emulator"}, timeOut = SETUP_TIMEOUT)
    public void afterClass() {
        LifeCycleUtils.closeQuietly(client);
    }

    private static CosmosUserDefinedFunctionProperties getCosmosUserDefinedFunctionProperties() {
        CosmosUserDefinedFunctionProperties udf =
            new CosmosUserDefinedFunctionProperties(UUID.randomUUID().toString(), "function() {var x = 10;}");
        return udf;
    }

    private static CosmosTriggerProperties getCosmosTriggerProperties() {
        CosmosTriggerProperties trigger = new CosmosTriggerProperties(UUID.randomUUID().toString(), "function() {var " +
            "x = 10;}");
        trigger.setTriggerOperation(TriggerOperation.CREATE);
        trigger.setTriggerType(TriggerType.PRE);
        return trigger;
    }

    private static CosmosStoredProcedureProperties getCosmosStoredProcedureProperties() {
        CosmosStoredProcedureProperties storedProcedureDef =
            new CosmosStoredProcedureProperties(UUID.randomUUID().toString(), "function() {var x = 10;}");
        return storedProcedureDef;
    }

    private Tracer getMockTracer() {
        Tracer mockTracer = Mockito.mock(Tracer.class);
        Mockito.when(mockTracer.start(ArgumentMatchers.any(String.class), ArgumentMatchers.any(Context.class))).thenReturn(Context.NONE);
        return mockTracer;
    }

    private void verifyTracerAttributes(TracerProvider tracerProvider, Tracer mockTracer, String methodName,
                                        Context context, String databaseName,
                                        int numberOfTimesCalledWithinTest, String errorType,
                                        CosmosDiagnostics cosmosDiagnostics,
                                        Map<String, Map<String, Object>> attributesMap) throws JsonProcessingException {
        Mockito.verify(tracerProvider, Mockito.times(numberOfTimesCalledWithinTest)).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(Context.class));

        if (databaseName != null) {
            Mockito.verify(mockTracer, Mockito.times(numberOfTimesCalledWithinTest)).setAttribute(TracerProvider.DB_INSTANCE,
                databaseName, context);
        }
        Mockito.verify(mockTracer, Mockito.times(numberOfTimesCalledWithinTest)).setAttribute(TracerProvider.DB_TYPE,
            TracerProvider.DB_TYPE_VALUE, context);
        Mockito.verify(mockTracer, Mockito.times(numberOfTimesCalledWithinTest)).setAttribute(TracerProvider.DB_URL,
            TestConfigurations.HOST,
            context);
        Mockito.verify(mockTracer, Mockito.times(1)).setAttribute(TracerProvider.DB_STATEMENT, methodName, context);

        //verifying diagnostics as events
        verifyTracerDiagnostics(tracerProvider, cosmosDiagnostics, attributesMap);
    }

    private void verifyTracerDiagnostics(TracerProvider tracerProvider,
                                         CosmosDiagnostics cosmosDiagnostics,
                                         Map<String, Map<String, Object>> attributesMap) throws JsonProcessingException {
        ClientSideRequestStatistics clientSideRequestStatistics =
            BridgeInternal.getClientSideRequestStatics(cosmosDiagnostics);
        int counter = 1;
        if (clientSideRequestStatistics != null) {
            //verifying add event call for systemInformation
            Mockito.verify(tracerProvider, Mockito.times(1)).addEvent(Mockito.eq("SystemInformation")
                , ArgumentMatchers.any(),
                Mockito.eq(OffsetDateTime.ofInstant(clientSideRequestStatistics.getRequestStartTimeUTC(),
                    ZoneOffset.UTC)), ArgumentMatchers.any());

            //verifying add event call for regionContacted
            Mockito.verify(tracerProvider, Mockito.times(1)).addEvent(Mockito.eq("RegionContacted")
                , ArgumentMatchers.any(),
                Mockito.eq(OffsetDateTime.ofInstant(clientSideRequestStatistics.getRequestStartTimeUTC(),
                    ZoneOffset.UTC)), ArgumentMatchers.any());
            assertThat(attributesMap.get("RegionContacted").get("JSON")).isEqualTo(OBJECT_MAPPER.writeValueAsString(clientSideRequestStatistics.getRegionsContacted()));

            //verifying add event call for clientCfgs
            Mockito.verify(tracerProvider, Mockito.times(1)).addEvent(Mockito.eq("ClientCfgs")
                , ArgumentMatchers.any(),
                Mockito.eq(OffsetDateTime.ofInstant(clientSideRequestStatistics.getRequestStartTimeUTC(),
                    ZoneOffset.UTC)), ArgumentMatchers.any());
            assertThat(attributesMap.get("ClientCfgs").get("JSON")).isEqualTo(OBJECT_MAPPER.writeValueAsString(clientSideRequestStatistics.getDiagnosticsClientContext()));


            //verifying add event call for serializationDiagnostics
            if (BridgeInternal.getClientSideRequestStatics(cosmosDiagnostics).getSerializationDiagnosticsContext().serializationDiagnosticsList != null) {
                for (SerializationDiagnosticsContext.SerializationDiagnostics serializationDiagnostics :
                    clientSideRequestStatistics.getSerializationDiagnosticsContext().serializationDiagnosticsList) {
                    Mockito.verify(tracerProvider, Mockito.times(1)).addEvent(Mockito.eq("SerializationDiagnostics " + serializationDiagnostics.serializationType)
                        , ArgumentMatchers.any(),
                        Mockito.eq(OffsetDateTime.ofInstant(serializationDiagnostics.startTimeUTC, ZoneOffset.UTC)),
                        ArgumentMatchers.any());
                    assertThat(attributesMap.get("SerializationDiagnostics " + serializationDiagnostics.serializationType).get("JSON")).isEqualTo(OBJECT_MAPPER.writeValueAsString(serializationDiagnostics));
                }
            }

            //verifying add event call for retry context
            if (clientSideRequestStatistics.getRetryContext().getRetryStartTime() != null) {
                Mockito.verify(tracerProvider, Mockito.times(1)).addEvent(Mockito.eq("Retry Context")
                    , ArgumentMatchers.any(),
                    Mockito.eq(OffsetDateTime.ofInstant(clientSideRequestStatistics.getRetryContext().getRetryStartTime()
                        , ZoneOffset.UTC)), ArgumentMatchers.any());
                assertThat(attributesMap.get("Retry Context").get("JSON")).isEqualTo(OBJECT_MAPPER.writeValueAsString(clientSideRequestStatistics.getRetryContext()));
            }

            //verifying add event call for storeResponseStatistics
            for (ClientSideRequestStatistics.StoreResponseStatistics storeResponseStatistics :
                clientSideRequestStatistics.getResponseStatisticsList()) {
                Iterator<RequestTimeline.Event> eventIterator = null;
                try {
                    eventIterator =
                        DirectBridgeInternal.getRequestTimeline(storeResponseStatistics.getStoreResult().toResponse()).iterator();
                } catch (CosmosException ex) {
                    eventIterator = BridgeInternal.getRequestTimeline(ex).iterator();
                }

                OffsetDateTime requestStartTime =
                    OffsetDateTime.ofInstant(storeResponseStatistics.getRequestResponseTimeUTC(), ZoneOffset.UTC);
                while (eventIterator.hasNext()) {
                    RequestTimeline.Event event = eventIterator.next();
                    if (event.getName().equals("created")) {
                        requestStartTime = OffsetDateTime.ofInstant(event.getStartTime(), ZoneOffset.UTC);
                        break;
                    }
                }
                Mockito.verify(tracerProvider, Mockito.times(1)).addEvent(Mockito.eq("StoreResponse" + counter)
                    , ArgumentMatchers.any(),
                    Mockito.eq(requestStartTime),
                    ArgumentMatchers.any());
                assertThat(attributesMap.get("StoreResponse" + counter).get("JSON")).isEqualTo(OBJECT_MAPPER.writeValueAsString(storeResponseStatistics));
                counter++;
            }

            //verifying add event call for supplemental storeResponseStatistics
            counter = 1;
            for (ClientSideRequestStatistics.StoreResponseStatistics storeResponseStatistics :
                ClientSideRequestStatistics.getCappedSupplementalResponseStatisticsList(clientSideRequestStatistics.getSupplementalResponseStatisticsList())) {
                Iterator<RequestTimeline.Event> eventIterator = null;
                try {
                    eventIterator =
                        DirectBridgeInternal.getRequestTimeline(storeResponseStatistics.getStoreResult().toResponse()).iterator();
                } catch (CosmosException ex) {
                    eventIterator = BridgeInternal.getRequestTimeline(ex).iterator();
                }

                OffsetDateTime requestStartTime =
                    OffsetDateTime.ofInstant(storeResponseStatistics.getRequestResponseTimeUTC(), ZoneOffset.UTC);
                while (eventIterator.hasNext()) {
                    RequestTimeline.Event event = eventIterator.next();
                    if (event.getName().equals("created")) {
                        requestStartTime = OffsetDateTime.ofInstant(event.getStartTime(), ZoneOffset.UTC);
                        break;
                    }
                }
                Mockito.verify(tracerProvider, Mockito.times(1)).addEvent(Mockito.eq("StoreResponse" + counter)
                    , ArgumentMatchers.any(),
                    Mockito.eq(requestStartTime),
                    ArgumentMatchers.any());
                assertThat(attributesMap.get("Supplemental StoreResponse" + counter).get("JSON")).isEqualTo(OBJECT_MAPPER.writeValueAsString(storeResponseStatistics));
                counter++;
            }

            counter = 1;
            for (ClientSideRequestStatistics.AddressResolutionStatistics addressResolutionStatistics :
                BridgeInternal.getClientSideRequestStatics(cosmosDiagnostics).getAddressResolutionStatistics().values()) {
                Mockito.verify(tracerProvider, Mockito.times(1)).addEvent(Mockito.eq("AddressResolutionStatistics" + counter)
                    , ArgumentMatchers.any(),
                    Mockito.eq(OffsetDateTime.ofInstant(addressResolutionStatistics.getStartTimeUTC(),
                        ZoneOffset.UTC)), ArgumentMatchers.any());
                assertThat(attributesMap.get("AddressResolutionStatistics" + counter).get("JSON")).isEqualTo(OBJECT_MAPPER.writeValueAsString(addressResolutionStatistics));
                counter++;
            }
        }

        FeedResponseDiagnostics feedResponseDiagnostics =
            cosmosDiagnosticsAccessor.getFeedResponseDiagnostics(cosmosDiagnostics);
        if (feedResponseDiagnostics != null && feedResponseDiagnostics.getClientSideRequestStatisticsList().size() > 0) {
            if (feedResponseDiagnostics.getQueryPlanDiagnosticsContext() != null) {
                //verifying add event call for query plan
                Mockito.verify(tracerProvider, Mockito.times(1)).addEvent(Mockito.eq("Query Plan Statistics")
                    , ArgumentMatchers.any(),
                    Mockito.eq(OffsetDateTime.ofInstant(feedResponseDiagnostics.getQueryPlanDiagnosticsContext().getStartTimeUTC(),
                        ZoneOffset.UTC)), ArgumentMatchers.any());
                assertThat(attributesMap.get("Query Plan Statistics").get("JSON")).isEqualTo(OBJECT_MAPPER.writeValueAsString(feedResponseDiagnostics.getQueryPlanDiagnosticsContext()));
            }

            counter = 1;
            for (ClientSideRequestStatistics clientSideStatistics :
                feedResponseDiagnostics.getClientSideRequestStatisticsList()) {
                if (clientSideStatistics.getResponseStatisticsList() != null && clientSideStatistics.getResponseStatisticsList().size() > 0
                    && clientSideStatistics.getResponseStatisticsList().get(0).getStoreResult() != null) {
                    Mockito.verify(tracerProvider, Mockito.atLeast(1)).addEvent(Mockito.eq("Diagnostics for PKRange " + clientSideStatistics.getResponseStatisticsList().get(0).getStoreResult().partitionKeyRangeId)
                        , ArgumentMatchers.any(),
                        Mockito.eq(OffsetDateTime.ofInstant(clientSideStatistics.getRequestStartTimeUTC(),
                            ZoneOffset.UTC)), ArgumentMatchers.any());
                } else if (clientSideStatistics.getGatewayStatistics() != null) {
                    Mockito.verify(tracerProvider, Mockito.atLeast(1)).addEvent(Mockito.eq("Diagnostics for PKRange " + clientSideStatistics.getGatewayStatistics().getPartitionKeyRangeId())
                        , ArgumentMatchers.any(),
                        Mockito.eq(OffsetDateTime.ofInstant(clientSideStatistics.getRequestStartTimeUTC(),
                            ZoneOffset.UTC)), ArgumentMatchers.any());
                } else {
                    Mockito.verify(tracerProvider, Mockito.atLeast(1)).addEvent(Mockito.eq("Diagnostics " + counter++)
                        , ArgumentMatchers.any(),
                        Mockito.eq(OffsetDateTime.ofInstant(clientSideStatistics.getRequestStartTimeUTC(),
                            ZoneOffset.UTC)), ArgumentMatchers.any());
                }
            }

            for (Map.Entry<String, QueryMetrics> queryMetrics :
                feedResponseDiagnostics.getQueryMetricsMap().entrySet()) {
                Mockito.verify(tracerProvider, Mockito.atLeast(1)).addEvent(Mockito.eq("Query Metrics for PKRange " + queryMetrics.getKey())
                    , ArgumentMatchers.any(),
                    ArgumentMatchers.any(),
                    ArgumentMatchers.any());
                assertThat(attributesMap.get("Query Metrics for PKRange " + queryMetrics.getKey()).get("Query " +
                    "Metrics")).isEqualTo(queryMetrics.getValue().toString());
            }
        }
    }

    private class TracerProviderCapture implements Answer<Context> {
        private Context result = null;

        public Context getResult() {
            return result;
        }

        @Override
        public Context answer(InvocationOnMock invocationOnMock) throws Throwable {
            result = (Context) invocationOnMock.callRealMethod();
            return result;
        }
    }

    private class AddEventCapture implements Answer<Void> {
        private Map<String, Map<String, Object>> attributesMap = new HashMap<>();

        @Override
        public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
            attributesMap.put(invocationOnMock.getArgument(0), invocationOnMock.getArgument(1));
            return null;
        }

        public Map<String, Map<String, Object>> getAttributesMap() {
            return attributesMap;
        }
    }

    private void setThreshHoldDurationOnTracer(TracerProvider tracerProvider, Duration duration, String fieldName) throws Exception {
        Field field = TracerProvider.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(tracerProvider, duration);
    }
}
