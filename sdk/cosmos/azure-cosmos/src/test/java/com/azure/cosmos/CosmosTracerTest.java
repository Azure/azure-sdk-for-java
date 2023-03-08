// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.core.util.Context;
import com.azure.core.util.tracing.StartSpanOptions;
import com.azure.core.util.tracing.Tracer;
import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.DiagnosticsProvider;
import com.azure.cosmos.implementation.FeedResponseDiagnostics;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers.CosmosDiagnosticsHelper;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers.CosmosDiagnosticsHelper.CosmosDiagnosticsAccessor;
import com.azure.cosmos.implementation.LifeCycleUtils;
import com.azure.cosmos.implementation.QueryMetrics;
import com.azure.cosmos.implementation.RequestTimeline;
import com.azure.cosmos.implementation.SerializationDiagnosticsContext;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.directconnectivity.StoreResponseDiagnostics;
import com.azure.cosmos.implementation.directconnectivity.StoreResultDiagnostics;
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import com.azure.cosmos.models.CosmosDatabaseProperties;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.rx.TestSuiteBase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosTracerTest extends TestSuiteBase {
    private final static Logger LOGGER = LoggerFactory.getLogger(CosmosTracerTest.class);
    private final static ObjectMapper OBJECT_MAPPER = Utils.getSimpleObjectMapper();

    private CosmosDiagnosticsAccessor cosmosDiagnosticsAccessor;
    CosmosAsyncClient client;
    CosmosAsyncDatabase cosmosAsyncDatabase;
    CosmosAsyncContainer cosmosAsyncContainer;

    @Factory(dataProvider = "clientBuildersWithDirectSessionIncludeComputeGateway")
    public CosmosTracerTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder.contentResponseOnWriteEnabled(true));
    }

    @BeforeClass(groups = {"emulator"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = getClientBuilder().buildAsyncClient();
        cosmosAsyncDatabase = getSharedCosmosDatabase(client);
        cosmosAsyncContainer = getSharedMultiPartitionCosmosContainer(client);
        cosmosDiagnosticsAccessor = CosmosDiagnosticsHelper.getCosmosDiagnosticsAccessor();
    }

    @Override
    public String resolveTestNameSuffix(Object[] row) {
        if (row != null && row.length == 3) {
            StringBuilder sb = new StringBuilder();
            if ((boolean)row[0]) {
                sb.append("Legacy");
            } else {
                sb.append("OTel");
            }
            sb.append("|");
            if ((boolean)row[1]) {
                sb.append("WithReq");
            } else {
                sb.append("NoReq");
            }
            sb.append("|");
            if ((boolean)row[2]) {
                sb.append("ForceThresholdViolation");
            } else {
                sb.append("NoThresholdViolation");
            }

            return sb.toString();
        }

        return "";
    }

    @DataProvider(name = "traceTestCaseProvider")
    private Object[][] traceTestCaseProvider() {
        return new Object[][]{
            new Object[] { true, false, true },
            new Object[] { true, false, false },
            new Object[] { false, false, false },
            new Object[] { false, true, false },
            new Object[] { false, false, true },
            new Object[] { false, true, true },
        };
    }

    @Test(groups = {"simple", "emulator"}, dataProvider = "traceTestCaseProvider", timeOut = TIMEOUT)
    public void cosmosAsyncClient(
        boolean useLegacyTracing,
        boolean enableRequestLevelTracing,
        boolean forceThresholdViolations) throws Exception {

        TracerUnderTest mockTracer = Mockito.spy(new TracerUnderTest());

        CosmosDiagnosticsThresholds thresholds = forceThresholdViolations ?
            new CosmosDiagnosticsThresholds()
                .configureLatencyThresholds(Duration.ZERO, Duration.ZERO)
            : new CosmosDiagnosticsThresholds()
                .configureLatencyThresholds(Duration.ofDays(1), Duration.ofDays(1));
        CosmosClientTelemetryConfig clientTelemetryConfig = new CosmosClientTelemetryConfig()
            .diagnosticsThresholds(thresholds);

        if (useLegacyTracing) {
            clientTelemetryConfig.useLegacyOpenTelemetryTracing();
        }

        if (enableRequestLevelTracing) {
            clientTelemetryConfig.enableTransportLevelTracing();
        }

        DiagnosticsProvider tracerProvider = Mockito.spy(new DiagnosticsProvider(mockTracer, clientTelemetryConfig));
        ReflectionUtils.setClientTelemetryConfig(client, clientTelemetryConfig);
        ReflectionUtils.setDiagnosticsProvider(client, tracerProvider);

        CosmosDatabaseResponse cosmosDatabaseResponse = client.createDatabaseIfNotExists(cosmosAsyncDatabase.getId(),
            ThroughputProperties.createManualThroughput(5000)).block();

        assertThat(cosmosDatabaseResponse).isNotNull();

        verifyTracerAttributes(
            mockTracer,
            "createDatabaseIfNotExists." + cosmosAsyncDatabase.getId(),
            cosmosAsyncDatabase.getId(),
            cosmosDatabaseResponse.getDiagnostics(),
            useLegacyTracing,
            enableRequestLevelTracing,
            forceThresholdViolations);

        mockTracer.reset();

        FeedResponse<CosmosDatabaseProperties> feedResponseReadAllDatabases =
            client.readAllDatabases(new CosmosQueryRequestOptions()).byPage().single().block();

        assertThat(feedResponseReadAllDatabases).isNotNull();

        verifyTracerAttributes(
            mockTracer,
            "readAllDatabases",
            null,
            feedResponseReadAllDatabases.getCosmosDiagnostics(),
            useLegacyTracing,
            enableRequestLevelTracing,
            forceThresholdViolations);

        mockTracer.reset();

        String query = "select * from c where c.id = '" + cosmosAsyncDatabase.getId() + "'";
        FeedResponse<CosmosDatabaseProperties> feedResponseQueryDatabases = client.queryDatabases(query,
            new CosmosQueryRequestOptions()).byPage().single().block();

        assertThat(feedResponseQueryDatabases).isNotNull();

        verifyTracerAttributes(
            mockTracer,
            "queryDatabases",
            null,
            feedResponseQueryDatabases.getCosmosDiagnostics(),
            useLegacyTracing,
            enableRequestLevelTracing,
            forceThresholdViolations);
    }

    /*
    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void cosmosAsyncDatabase() throws Exception {
        Tracer mockTracer = getMockTracer();
        TracerProvider tracerProvider = Mockito.spy(new TracerProvider(mockTracer, false, false));
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
    @Ignore
    public void cosmosAsyncContainer() throws Exception {
        Tracer mockTracer = getMockTracer();
        TracerProvider tracerProvider = Mockito.spy(new TracerProvider(mockTracer, false, false));
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
            InternalObjectNode.class).byPage().blockFirst();
        Mockito.verify(tracerProvider, Mockito.times(traceApiCounter)).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(Context.class));
        verifyTracerAttributes(tracerProvider, mockTracer, "readAllItems." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null, feedItemResponse.getCosmosDiagnostics(), attributesMap);
        traceApiCounter++;

        String query = "select * from c where c.id = '" + ITEM_ID + "'";
        feedItemResponse =
            cosmosAsyncContainer.queryItems(query, queryRequestOptions, InternalObjectNode.class).byPage().blockFirst();
        verifyTracerAttributes(tracerProvider, mockTracer, "queryItems." + cosmosAsyncContainer.getId(), context,
            cosmosAsyncDatabase.getId(), traceApiCounter, null, feedItemResponse.getCosmosDiagnostics(), attributesMap);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void cosmosAsyncScripts() throws Exception {
        Tracer mockTracer = getMockTracer();
        TracerProvider tracerProvider = Mockito.spy(new TracerProvider(mockTracer, false, false));
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
    @Ignore
    public void tracerExceptionSpan() throws Exception {
        Tracer mockTracer = getMockTracer();
        TracerProvider tracerProvider = Mockito.spy(new TracerProvider(mockTracer, false, false));
        setThreshHoldDurationOnTracer(tracerProvider, Duration.ZERO, "CRUD_THRESHOLD_FOR_DIAGNOSTICS");
        ReflectionUtils.setTracerProvider(client, tracerProvider);
        int traceApiCounter = 1;

        TracerProviderCapture tracerProviderCapture = new TracerProviderCapture();
        AddEventCapture addEventCapture = new AddEventCapture();

        Mockito.doAnswer(tracerProviderCapture).when(tracerProvider).startSpan(ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any());
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
    }*/

    @AfterClass(groups = {"emulator"}, timeOut = SETUP_TIMEOUT)
    public void afterClass() {
        LifeCycleUtils.closeQuietly(client);
    }

    private void verifyTracerAttributes(
        TracerUnderTest mockTracer,
        String methodName,
        String databaseName,
        CosmosDiagnostics cosmosDiagnostics,
        boolean useLegacyTracing,
        boolean enableRequestLevelTracing,
        boolean forceThresholdViolation) throws JsonProcessingException {

        if (useLegacyTracing) {
            verifyLegacyTracerAttributes(
                mockTracer,
                methodName,
                databaseName,
                cosmosDiagnostics,
                enableRequestLevelTracing,
                forceThresholdViolation);
            return;
        }

        verifyOTelTracerAttributes(
            mockTracer,
            methodName,
            databaseName,
            cosmosDiagnostics,
            enableRequestLevelTracing);
    }

    private void verifyOTelTracerAttributes(
        TracerUnderTest mockTracer,
        String methodName,
        String databaseName,
        CosmosDiagnostics cosmosDiagnostics,
        boolean enableRequestLevelTracing) {

        Map<String, Object> attributes = mockTracer.attributes;
        if (databaseName != null) {
            assertThat(attributes.get("db.name")).isEqualTo(databaseName);
        }

        assertThat(attributes.get("db.system")).isEqualTo("cosmosdb");
        assertThat(attributes.get("db.operation")).isEqualTo(methodName);
        assertThat(attributes.get("net.peer.name")).isEqualTo("localhost");
        assertThat(attributes.get(Tracer.AZ_TRACING_NAMESPACE_KEY)).isEqualTo(DiagnosticsProvider.RESOURCE_PROVIDER_NAME);

        verifyOTelTracerDiagnostics(cosmosDiagnostics, mockTracer);

        verifyOTelTracerTransport(
            cosmosDiagnostics, mockTracer, enableRequestLevelTracing);
    }

    private void verifyOTelTracerDiagnostics(CosmosDiagnostics cosmosDiagnostics,
                                             TracerUnderTest mockTracer) {
        ClientSideRequestStatistics clientSideRequestStatistics =
            BridgeInternal.getClientSideRequestStatics(cosmosDiagnostics);

        FeedResponseDiagnostics feedResponseDiagnostics =
            cosmosDiagnosticsAccessor.getFeedResponseDiagnostics(cosmosDiagnostics);
        if (clientSideRequestStatistics != null ||
            (feedResponseDiagnostics != null &&
                feedResponseDiagnostics.getClientSideRequestStatisticsList().size() > 0)) {

            assertThat(mockTracer).isNotNull();
            assertThat(mockTracer.context).isNotNull();

            CosmosDiagnosticsContext ctx = DiagnosticsProvider.getCosmosDiagnosticsContextFromTraceContextOrThrow(
                mockTracer.context
            );

            if (ctx.isFailure() || ctx.isThresholdViolated()) {
                if (ctx.isFailure()) {
                    assertThat(mockTracer.events).anyMatch(e -> e.name .equals("failure"));
                    assertThat(mockTracer.events).noneMatch(e -> e.name.equals("threshold_violation"));

                } else {
                    assertThat(mockTracer.events).noneMatch(e -> e.name.equals("failure"));
                    assertThat(mockTracer.events).anyMatch(e -> e.name.equals("threshold_violation"));
                }
            } else {
                assertThat(mockTracer.events).noneMatch(e -> e.name.equals("threshold_violation"));
                assertThat(mockTracer.events).noneMatch(e -> e.name.equals("failure"));
            }
        }
    }

    private void verifyOTelTracerTransport(CosmosDiagnostics cosmosDiagnostics,
                                           TracerUnderTest mockTracer,
                                           boolean enableRequestLevelTracing) {

        assertThat(mockTracer).isNotNull();
        assertThat(mockTracer.context).isNotNull();

        if (!enableRequestLevelTracing ||
            // For Gateway we rely on http out-of-the-box tracing
            client.getConnectionPolicy().getConnectionMode() != ConnectionMode.DIRECT) {

            assertThat(mockTracer.events).noneMatch(e -> e.name.equals("rntbd.request"));
            return;
        } else {
            assertThat(mockTracer.events).anyMatch(e -> e.name.equals("rntbd.request"));
        }

        ClientSideRequestStatistics clientSideRequestStatistics =
            BridgeInternal.getClientSideRequestStatics(cosmosDiagnostics);

        FeedResponseDiagnostics feedResponseDiagnostics =
            cosmosDiagnosticsAccessor.getFeedResponseDiagnostics(cosmosDiagnostics);
        if (clientSideRequestStatistics != null ||
            (feedResponseDiagnostics != null &&
                feedResponseDiagnostics.getClientSideRequestStatisticsList().size() > 0)) {

            assertThat(mockTracer).isNotNull();
            assertThat(mockTracer.context).isNotNull();

            CosmosDiagnosticsContext ctx = DiagnosticsProvider.getCosmosDiagnosticsContextFromTraceContextOrThrow(
                mockTracer.context
            );

            for (CosmosDiagnostics d: ctx.getDiagnostics()) {
                if (d.getClientSideRequestStatistics() != null) {
                    for (ClientSideRequestStatistics s: d.getClientSideRequestStatistics()) {
                        if (s.getResponseStatisticsList() == null) {
                            continue;
                        }
                        assertStoreResponseStatistics(mockTracer, s.getResponseStatisticsList());
                    }
                }

            }
        }
    }

    private void assertStoreResponseStatistics(
        TracerUnderTest mockTracer,
        List<ClientSideRequestStatistics.StoreResponseStatistics> storeResponseStatistics) {

        for (ClientSideRequestStatistics.StoreResponseStatistics responseStatistics: storeResponseStatistics) {
            StoreResultDiagnostics storeResultDiagnostics = responseStatistics.getStoreResult();
            StoreResponseDiagnostics storeResponseDiagnostics =
                storeResultDiagnostics.getStoreResponseDiagnostics();

            Map<String, Object> attributes = new HashMap<>();
            attributes.put("rntbd.url", storeResultDiagnostics.getStorePhysicalAddressAsString());
            attributes.put("rntbd.resource_type", responseStatistics.getRequestResourceType().toString());
            attributes.put("rntbd.operation_type", responseStatistics.getRequestOperationType().toString());
            attributes.put("rntbd.region", responseStatistics.getRegionName());

            if (storeResultDiagnostics.getLsn() > 0) {
                attributes.put("rntbd.lsn", Long.toString(storeResultDiagnostics.getLsn()));
            }

            if (storeResultDiagnostics.getGlobalCommittedLSN() > 0) {
                attributes.put("rntbd.gclsn", Long.toString(storeResultDiagnostics.getGlobalCommittedLSN()));
            }

            String responseSessionToken = responseStatistics.getRequestSessionToken();
            if (responseSessionToken != null && !responseSessionToken.isEmpty()) {
                attributes.put("rntbd.session_token", responseSessionToken);
            }

            String requestSessionToken = responseStatistics.getRequestSessionToken();
            if (requestSessionToken != null && !requestSessionToken.isEmpty()) {
                attributes.put("rntbd.request_session_token", requestSessionToken);
            }

            String activityId = storeResponseDiagnostics.getActivityId();
            if (requestSessionToken != null && !requestSessionToken.isEmpty()) {
                attributes.put("rntbd.activity_id", activityId);
            }

            String pkRangeId = storeResponseDiagnostics.getPartitionKeyRangeId();
            if (pkRangeId != null && !pkRangeId.isEmpty()) {
                attributes.put("rntbd.partition_key_range_id", pkRangeId);
            }

            attributes.put("rntbd.status_code", Integer.toString(storeResponseDiagnostics.getStatusCode()));
            if (storeResponseDiagnostics.getSubStatusCode() != 0) {
                attributes.put("rntbd.sub_status_code", Integer.toString(storeResponseDiagnostics.getSubStatusCode()));
            }

            Double backendLatency = storeResultDiagnostics.getBackendLatencyInMs();
            if (backendLatency != null) {
                attributes.put("rntbd.backend_latency", Double.toString(backendLatency));
            }

            double requestCharge = storeResponseDiagnostics.getRequestCharge();
            attributes.put("rntbd.request_charge", Double.toString(requestCharge));

            Duration latency = responseStatistics.getDuration();
            if (latency != null) {
                attributes.put("rntbd.latency", latency.toString());
            }

            if (storeResponseDiagnostics.getRntbdChannelStatistics() != null) {
                attributes.put(
                    "rntbd.is_new_channel",
                    storeResponseDiagnostics.getRntbdChannelStatistics().isWaitForConnectionInit());
            }

            Instant startTime = null;
            for (RequestTimeline.Event event : storeResponseDiagnostics.getRequestTimeline()) {
                Instant eventTime = event.getStartTime() != null ?
                    event.getStartTime() : null;

                if (eventTime != null &&
                    (startTime == null || startTime.isBefore(eventTime))) {
                    startTime = eventTime;
                }

                Duration duration = event.getDuration();
                if (duration == null || duration == Duration.ZERO) {
                    continue;
                }

                attributes.put("rntbd.latency_" + event.getName().toLowerCase(Locale.ROOT), duration.toString());
            }

            attributes.put("rntbd.request_size_bytes",storeResponseDiagnostics.getRequestPayloadLength());
            attributes.put("rntbd.response_size_bytes",storeResponseDiagnostics.getResponsePayloadLength());

            assertEvent(mockTracer, "rntbd.request", startTime, attributes);
        }
    }

    /*
    private void verifyTracerDiagnostics(DiagnosticsProvider tracerProvider,
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
            assertThat(attributesMap.get("RegionContacted").get("JSON")).isEqualTo(OBJECT_MAPPER.writeValueAsString(clientSideRequestStatistics.getContactedRegionNames()));

            //verifying add event call for clientCfgs
            Mockito.verify(tracerProvider, Mockito.times(1)).addEvent(Mockito.eq("ClientCfgs")
                , ArgumentMatchers.any(),
                Mockito.eq(OffsetDateTime.ofInstant(clientSideRequestStatistics.getRequestStartTimeUTC(),
                    ZoneOffset.UTC)), ArgumentMatchers.any());
            assertThat(attributesMap.get("ClientCfgs").get("JSON")).isEqualTo(OBJECT_MAPPER.writeValueAsString(clientSideRequestStatistics.getDiagnosticsClientConfig()));


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
                    eventIterator = storeResponseStatistics.getStoreResult().getStoreResponseDiagnostics().getRequestTimeline().iterator();
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
                    eventIterator = storeResponseStatistics.getStoreResult().getStoreResponseDiagnostics().getRequestTimeline().iterator();
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
                assertThat(attributesMap.get("Query Plan Statistics").get("JSON"))
                    .isEqualTo(OBJECT_MAPPER.writeValueAsString(feedResponseDiagnostics.getQueryPlanDiagnosticsContext()));
            }

            counter = 1;
            for (ClientSideRequestStatistics clientSideStatistics :
                feedResponseDiagnostics.getClientSideRequestStatisticsList()) {
                if (clientSideStatistics.getResponseStatisticsList() != null && clientSideStatistics.getResponseStatisticsList().size() > 0
                    && clientSideStatistics.getResponseStatisticsList().get(0).getStoreResult() != null) {
                    Mockito.verify(tracerProvider, Mockito.atLeast(1)).addEvent(Mockito.eq("Diagnostics for PKRange "
                            + clientSideStatistics.getResponseStatisticsList().get(0).getStoreResult().getStoreResponseDiagnostics().getPartitionKeyRangeId())
                        , ArgumentMatchers.any(),
                        Mockito.eq(OffsetDateTime.ofInstant(clientSideStatistics.getRequestStartTimeUTC(),
                            ZoneOffset.UTC)), ArgumentMatchers.any());
                } else if (clientSideStatistics.getGatewayStatistics() != null) {
                    Mockito.verify(tracerProvider, Mockito.atLeast(1)).addEvent(Mockito.eq("Diagnostics for PKRange "
                            + clientSideStatistics.getGatewayStatistics().getPartitionKeyRangeId())
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
    }*/

    private void verifyLegacyTracerAttributes(TracerUnderTest mockTracer,
                                              String methodName,
                                              String databaseName,
                                              CosmosDiagnostics cosmosDiagnostics,
                                              boolean enableRequestLevelTracing,
                                              boolean forceThresholdViolation) throws JsonProcessingException {
        Map<String, Object> attributes = mockTracer.attributes;

        assertThat(enableRequestLevelTracing).isEqualTo(false);

        if (databaseName != null) {
            assertThat(attributes.get("db.instance")).isEqualTo(databaseName);
        }

        assertThat(attributes.get("db.type")).isEqualTo("Cosmos");
        assertThat(attributes.get("db.url"))
            .matches(url -> url.equals(TestConfigurations.HOST) ||
                url.equals(TestConfigurations.HOST.replace(
                    ROUTING_GATEWAY_EMULATOR_PORT, COMPUTE_GATEWAY_EMULATOR_PORT
                )));
        assertThat(attributes.get("db.statement")).isEqualTo(methodName);
        assertThat(attributes.get(Tracer.AZ_TRACING_NAMESPACE_KEY)).isEqualTo("Microsoft.DocumentDB");

        //verifying diagnostics as events
        if (forceThresholdViolation) {
            verifyLegacyTracerDiagnostics(cosmosDiagnostics, mockTracer);
        }
    }

    private static void assertEvent(TracerUnderTest mockTracer, String eventName, Instant time) {
        Map<String, Object> attributes = new HashMap<>();
        assertEvent(mockTracer, eventName, time, attributes);
    }

    private static void assertEvent(
        TracerUnderTest mockTracer, String eventName, Instant time, String value) {

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("JSON", value);
        assertEvent(mockTracer, eventName, time, attributes);
    }

    private static void assertEvent(
        TracerUnderTest mockTracer, String eventName, Instant time, Map<String, Object> attributes) {

        List<EventRecord> filteredEvents =
            mockTracer.events.stream().filter(e -> e.name.equals(eventName)).collect(Collectors.toList());
        assertThat(filteredEvents).hasSizeGreaterThanOrEqualTo(1);
        if (time != null) {
            filteredEvents =
                filteredEvents
                    .stream()
                    .filter(e -> e.timestamp != null &&
                        e.timestamp.equals(OffsetDateTime.ofInstant(time, ZoneOffset.UTC)))
                    .collect(Collectors.toList());

            assertThat(filteredEvents).hasSizeGreaterThanOrEqualTo(1);
        }

        if (attributes == null || attributes.size() == 0) {
            return;
        }

        filteredEvents =
            filteredEvents
                .stream()
                .filter(e -> {
                    if (e.attributes == null || e.attributes.size() < attributes.size()) {
                        return false;
                    }

                    for(String key: attributes.keySet()) {
                        if (!e.attributes.containsKey((key))) {
                            return false;
                        }

                        if (!e.attributes.get(key).equals(attributes.get(key))) {
                            return false;
                        }
                    }

                    return true;
                })
                .collect(Collectors.toList());

        assertThat(filteredEvents).hasSizeGreaterThanOrEqualTo(1);
    }

    private void verifyLegacyTracerDiagnostics(CosmosDiagnostics cosmosDiagnostics,
                                               TracerUnderTest mockTracer) throws JsonProcessingException {
        ClientSideRequestStatistics clientSideRequestStatistics =
            BridgeInternal.getClientSideRequestStatics(cosmosDiagnostics);
        int counter = 1;
        if (clientSideRequestStatistics != null) {
            assertEvent(
                mockTracer, "SystemInformation", clientSideRequestStatistics.getRequestStartTimeUTC());
            assertEvent(
                mockTracer,
                "RegionContacted",
                clientSideRequestStatistics.getRequestStartTimeUTC(),
                OBJECT_MAPPER.writeValueAsString(clientSideRequestStatistics.getContactedRegionNames()));
            assertEvent(
                mockTracer,
                "ClientCfgs",
                clientSideRequestStatistics.getRequestStartTimeUTC(),
                OBJECT_MAPPER.writeValueAsString(clientSideRequestStatistics.getDiagnosticsClientConfig()));

            //verifying add event call for serializationDiagnostics
            if (BridgeInternal.getClientSideRequestStatics(cosmosDiagnostics).getSerializationDiagnosticsContext().serializationDiagnosticsList != null) {
                for (SerializationDiagnosticsContext.SerializationDiagnostics serializationDiagnostics :
                    clientSideRequestStatistics.getSerializationDiagnosticsContext().serializationDiagnosticsList) {

                    String eventName = "SerializationDiagnostics " + serializationDiagnostics.serializationType;
                    assertEvent(
                        mockTracer,
                        eventName,
                        serializationDiagnostics.startTimeUTC,
                        OBJECT_MAPPER.writeValueAsString(serializationDiagnostics));
                }
            }

            //verifying add event call for retry context
            if (clientSideRequestStatistics.getRetryContext().getRetryStartTime() != null) {
                String eventName = "Retry Context";
                assertEvent(
                    mockTracer,
                    eventName,
                    clientSideRequestStatistics.getRetryContext().getRetryStartTime(),
                    OBJECT_MAPPER.writeValueAsString(clientSideRequestStatistics.getRetryContext()));
            }

            //verifying add event call for storeResponseStatistics
            for (ClientSideRequestStatistics.StoreResponseStatistics storeResponseStatistics :
                clientSideRequestStatistics.getResponseStatisticsList()) {
                Iterator<RequestTimeline.Event> eventIterator;
                try {
                    eventIterator = storeResponseStatistics.getStoreResult().getStoreResponseDiagnostics().getRequestTimeline().iterator();
                } catch (CosmosException ex) {
                    eventIterator = BridgeInternal.getRequestTimeline(ex).iterator();
                }

                Instant requestStartTime =
                    storeResponseStatistics.getRequestResponseTimeUTC();
                while (eventIterator.hasNext()) {
                    RequestTimeline.Event event = eventIterator.next();
                    if (event.getName().equals("created")) {
                        requestStartTime = event.getStartTime();
                        break;
                    }
                }

                String eventName = "StoreResponse" + counter;
                assertEvent(
                    mockTracer,
                    eventName,
                    requestStartTime,
                    OBJECT_MAPPER.writeValueAsString(storeResponseStatistics));

                counter++;
            }

            counter = 1;
            for (ClientSideRequestStatistics.AddressResolutionStatistics addressResolutionStatistics :
                BridgeInternal.getClientSideRequestStatics(cosmosDiagnostics).getAddressResolutionStatistics().values()) {

                String eventName = "AddressResolutionStatistics" + counter;
                assertEvent(
                    mockTracer,
                    eventName,
                    addressResolutionStatistics.getStartTimeUTC(),
                    OBJECT_MAPPER.writeValueAsString(addressResolutionStatistics));

                counter++;
            }
        }

        FeedResponseDiagnostics feedResponseDiagnostics =
            cosmosDiagnosticsAccessor.getFeedResponseDiagnostics(cosmosDiagnostics);
        if (feedResponseDiagnostics != null && feedResponseDiagnostics.getClientSideRequestStatisticsList().size() > 0) {
            if (feedResponseDiagnostics.getQueryPlanDiagnosticsContext() != null) {
                //verifying add event call for query plan
                assertEvent(
                    mockTracer,
                    "Query Plan Statistics",
                    feedResponseDiagnostics.getQueryPlanDiagnosticsContext().getStartTimeUTC(),
                    OBJECT_MAPPER.writeValueAsString(feedResponseDiagnostics.getQueryPlanDiagnosticsContext()));
            }

            counter = 1;
            for (ClientSideRequestStatistics clientSideStatistics :
                feedResponseDiagnostics.getClientSideRequestStatisticsList()) {
                if (clientSideStatistics.getResponseStatisticsList() != null && clientSideStatistics.getResponseStatisticsList().size() > 0
                    && clientSideStatistics.getResponseStatisticsList().get(0).getStoreResult() != null) {

                    String pkRangeId = clientSideStatistics
                        .getResponseStatisticsList()
                        .get(0)
                        .getStoreResult()
                        .getStoreResponseDiagnostics()
                        .getPartitionKeyRangeId();

                    if (pkRangeId != null) {
                        String eventName = "Diagnostics for PKRange "
                            + clientSideStatistics
                            .getResponseStatisticsList()
                            .get(0)
                            .getStoreResult()
                            .getStoreResponseDiagnostics()
                            .getPartitionKeyRangeId();
                        assertEvent(
                            mockTracer,
                            eventName,
                            clientSideStatistics.getRequestStartTimeUTC());
                    }
                } else if (clientSideStatistics.getGatewayStatistics() != null) {
                    String pkRangeId = clientSideStatistics.getGatewayStatistics().getPartitionKeyRangeId();

                    if (pkRangeId != null) {
                        String eventName = "Diagnostics for PKRange "
                            + clientSideStatistics.getGatewayStatistics().getPartitionKeyRangeId();
                        assertEvent(
                            mockTracer,
                            eventName,
                            clientSideStatistics.getRequestStartTimeUTC());
                    }
                } else {
                    String eventName = "Diagnostics " + counter++;
                    assertEvent(
                        mockTracer,
                        eventName,
                        clientSideStatistics.getRequestStartTimeUTC());
                }
            }

            for (Map.Entry<String, QueryMetrics> queryMetrics :
                feedResponseDiagnostics.getQueryMetricsMap().entrySet()) {
                String eventName = "Query Metrics for PKRange " + queryMetrics.getKey();
                Stream<EventRecord> filteredEvents =
                    mockTracer.events.stream().filter(e -> e.name.equals(eventName));
                assertThat(filteredEvents).hasSize(1);
                assertThat(filteredEvents.findFirst().isPresent()).isEqualTo(true);
                assertThat(filteredEvents.findFirst().get().attributes.get("Query Metrics"))
                    .isEqualTo(queryMetrics.getValue().toString());
            }
        }
    }

    private static class EventRecord {
        private final String name;
        private final OffsetDateTime timestamp;
        private final Map<String, Object> attributes;

        public EventRecord(String name, OffsetDateTime timestamp,  Map<String, Object> attributes) {
            this.name = name;
            this.timestamp = timestamp;
            this.attributes = attributes;
        }

        @Override
        public String toString() {

            StringBuilder sb = new StringBuilder();
            sb.append(this.name)
              .append(" - ")
              .append(this.timestamp)
              .append(": { '");

            for(String key: this.attributes.keySet()) {
                sb.append(key).append("' : '").append(this.attributes.get(key)).append("'");
            }

            sb.append(" }");

            return sb.toString();
        }
    }

    private static class TracerUnderTest implements Tracer {

        public Map<String, Object> attributes = new HashMap<>();
        public String methodName;
        public String statusMessage;
        public Throwable error;
        public List<EventRecord> events = new ArrayList<>();
        public Context context;

        @Override
        public Context start(String methodName, Context context) {
            LOGGER.info("--> start {}", methodName);
            assertThat(this.methodName).isNull();
            this.methodName = methodName;

            return this.context = context;
        }

        @Override
        public Context start(String methodName, StartSpanOptions options, Context context) {
            Context ctx = Tracer.super.start(methodName, options, context);

            if (options != null) {
                for (String key : options.getAttributes().keySet()) {
                    this.attributes.put(key, options.getAttributes().get(key));
                }
            }

            return this.context = ctx;
        }

        @Override
        public void end(String statusMessage, Throwable error, Context context) {
            LOGGER.info("--> end {}, {}", statusMessage, error);
            assertThat(this.error).isNull();
            assertThat(this.statusMessage).isNull();
            this.error = error;
            this.statusMessage = statusMessage;
            this.context = context;
        }

        @Override
        public void setAttribute(String key, String value, Context context) {
            LOGGER.info("--> SetAttribute {}: {}", key, value);
            this.attributes.put(key, value);
            this.context = context;
        }

        @Override
        public void addEvent(String name, Map<String, Object> attributes, OffsetDateTime timestamp, Context context) {
            Tracer.super.addEvent(name, attributes, timestamp, context);

            this.events.add(new EventRecord(name, timestamp, attributes));
            this.context = context;
        }

        public void reset() {
            this.error = null;
            this.statusMessage = null;
            this.methodName = null;
            this.context = null;
            this.attributes.clear();
            this.events.clear();
        }
    }
}
