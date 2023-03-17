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
import com.azure.cosmos.models.ThroughputResponse;
import com.azure.cosmos.models.TriggerOperation;
import com.azure.cosmos.models.TriggerType;
import com.azure.cosmos.rx.TestSuiteBase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.assertj.core.api.Assertions;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.io.PrintWriter;
import java.io.StringWriter;
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
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

public class CosmosTracerTest extends TestSuiteBase {
    private final static Logger LOGGER = LoggerFactory.getLogger(CosmosTracerTest.class);
    private final static ObjectMapper OBJECT_MAPPER = Utils.getSimpleObjectMapper();
    private static final String ITEM_ID = "tracerDoc";

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

        createAndInitializeDiagnosticsProvider(
            mockTracer, useLegacyTracing, enableRequestLevelTracing, forceThresholdViolations);

        CosmosDatabaseResponse cosmosDatabaseResponse = client.createDatabaseIfNotExists(cosmosAsyncDatabase.getId(),
            ThroughputProperties.createManualThroughput(5000)).block();
        assertThat(cosmosDatabaseResponse).isNotNull();
        verifyTracerAttributes(
            mockTracer,
            "createDatabaseIfNotExists." + cosmosAsyncDatabase.getId(),
            cosmosAsyncDatabase.getId(),
            null,
            cosmosDatabaseResponse.getDiagnostics(),
            null,
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
            null,
            feedResponseReadAllDatabases.getCosmosDiagnostics(),
            null,
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
            null,
            feedResponseQueryDatabases.getCosmosDiagnostics(),
            null,
            useLegacyTracing,
            enableRequestLevelTracing,
            forceThresholdViolations);
        mockTracer.reset();

        CosmosException cosmosError = null;
        // Trying to create already existing database to trigger 409 (escaped exception)
        try {
            client.createDatabase(cosmosAsyncDatabase.getId(),
                ThroughputProperties.createManualThroughput(5000)).block();

            fail("Should have thrown 409 exception");
        } catch (CosmosException error) {
            assertThat(error.getStatusCode()).isEqualTo(409);
            assertThat(error.getDiagnostics()).isNotNull();
            cosmosError = error;
        }

        verifyTracerAttributes(
            mockTracer,
            "createDatabase." + cosmosAsyncDatabase.getId(),
            cosmosAsyncDatabase.getId(),
            null,
            cosmosError.getDiagnostics(),
            cosmosError,
            useLegacyTracing,
            enableRequestLevelTracing,
            forceThresholdViolations);

        mockTracer.reset();
    }

    @Test(groups = {"simple", "emulator"}, dataProvider = "traceTestCaseProvider", timeOut = TIMEOUT)
    public void cosmosAsyncDatabase(
                                    boolean useLegacyTracing,
                                    boolean enableRequestLevelTracing,
                                    boolean forceThresholdViolations) throws Exception {
        TracerUnderTest mockTracer = Mockito.spy(new TracerUnderTest());

        createAndInitializeDiagnosticsProvider(
            mockTracer, useLegacyTracing, enableRequestLevelTracing, forceThresholdViolations);

        CosmosContainerResponse containerResponse =
            cosmosAsyncDatabase.createContainerIfNotExists(cosmosAsyncContainer.getId(),
            "/pk", 5000).block();
        assertThat(containerResponse).isNotNull();
        verifyTracerAttributes(
            mockTracer,
            "createContainerIfNotExists." + cosmosAsyncContainer.getId(),
            cosmosAsyncDatabase.getId(),
            cosmosAsyncContainer.getId(),
            containerResponse.getDiagnostics(),
            null,
            useLegacyTracing,
            false, // will always go through Gateway
            forceThresholdViolations);
        mockTracer.reset();

        FeedResponse<CosmosUserProperties> userPropertiesFeedResponse =
            cosmosAsyncDatabase.readAllUsers().byPage().single().block();
        assertThat(userPropertiesFeedResponse).isNotNull();
        verifyTracerAttributes(
            mockTracer,
            "readAllUsers." + cosmosAsyncDatabase.getId(),
            cosmosAsyncDatabase.getId(),
            null,
            userPropertiesFeedResponse.getCosmosDiagnostics(),
            null,
            useLegacyTracing,
            enableRequestLevelTracing,
            forceThresholdViolations);
        mockTracer.reset();

        FeedResponse<CosmosContainerProperties> containerPropertiesFeedResponse =
            cosmosAsyncDatabase.readAllContainers().byPage().single().block();
        assertThat(containerPropertiesFeedResponse).isNotNull();
        verifyTracerAttributes(
            mockTracer,
            "readAllContainers." + cosmosAsyncDatabase.getId(),
            cosmosAsyncDatabase.getId(),
            null,
            containerPropertiesFeedResponse.getCosmosDiagnostics(),
            null,
            useLegacyTracing,
            enableRequestLevelTracing,
            forceThresholdViolations);
        mockTracer.reset();

        CosmosException cosmosError = null;
        try {
            cosmosAsyncDatabase.readThroughput().block().getDiagnostics();
            fail("Should have thrown 400 exception");
        } catch (CosmosException error) {
            assertThat(error.getStatusCode()).isEqualTo(400);
            cosmosError = error;
        }

        verifyTracerAttributes(
            mockTracer,
            "readThroughput." + cosmosAsyncDatabase.getId(),
            cosmosAsyncDatabase.getId(),
            null,
            cosmosError.getDiagnostics(),
            cosmosError,
            useLegacyTracing,
            enableRequestLevelTracing,
            forceThresholdViolations);
        mockTracer.reset();
    }

    @Test(groups = {"simple", "emulator"}, dataProvider = "traceTestCaseProvider", timeOut = 10000000 * TIMEOUT)
    public void cosmosAsyncContainer(
        boolean useLegacyTracing,
        boolean enableRequestLevelTracing,
        boolean forceThresholdViolations) throws Exception {

        TracerUnderTest mockTracer = Mockito.spy(new TracerUnderTest());

        createAndInitializeDiagnosticsProvider(
            mockTracer, useLegacyTracing, enableRequestLevelTracing, forceThresholdViolations);

        CosmosContainerResponse containerResponse = cosmosAsyncContainer.read().block();
        assertThat(containerResponse).isNotNull();
        verifyTracerAttributes(
            mockTracer,
            "readContainer." + cosmosAsyncContainer.getId(),
            cosmosAsyncDatabase.getId(),
            cosmosAsyncContainer.getId(),
            containerResponse.getDiagnostics(),
            null,
            useLegacyTracing,
            false, // will always go through Gateway
            forceThresholdViolations);
        mockTracer.reset();

        ThroughputResponse throughputResponse = cosmosAsyncContainer.readThroughput().block();

        verifyTracerAttributes(
            mockTracer,
            "readThroughput." + cosmosAsyncContainer.getId(),
            cosmosAsyncDatabase.getId(),
            cosmosAsyncContainer.getId(),
            throughputResponse.getDiagnostics(),
            null,
            useLegacyTracing,
            false, // will always go through Gateway
            forceThresholdViolations);
        mockTracer.reset();

        ObjectNode item = getDocumentDefinition(ITEM_ID);
        CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions();
        CosmosItemResponse<ObjectNode> cosmosItemResponse = cosmosAsyncContainer
            .createItem(item, requestOptions)
            .block();

        assertThat(cosmosItemResponse).isNotNull();
        verifyTracerAttributes(
            mockTracer,
            "createItem." + cosmosAsyncContainer.getId(),
            cosmosAsyncDatabase.getId(),
            cosmosAsyncContainer.getId(),
            cosmosItemResponse.getDiagnostics(),
            null,
            useLegacyTracing,
            enableRequestLevelTracing,
            forceThresholdViolations);
        mockTracer.reset();

        cosmosItemResponse = cosmosAsyncContainer.upsertItem(item, requestOptions).block();
        assertThat(containerResponse).isNotNull();
        verifyTracerAttributes(
            mockTracer,
            "upsertItem." + cosmosAsyncContainer.getId(),
            cosmosAsyncDatabase.getId(),
            cosmosAsyncContainer.getId(),
            cosmosItemResponse.getDiagnostics(),
            null,
            useLegacyTracing,
            enableRequestLevelTracing,
            forceThresholdViolations);
        mockTracer.reset();

        cosmosItemResponse = cosmosAsyncContainer
            .readItem(ITEM_ID, PartitionKey.NONE, requestOptions, ObjectNode.class)
            .block();
        assertThat(containerResponse).isNotNull();
        verifyTracerAttributes(
            mockTracer,
            "readItem." + cosmosAsyncContainer.getId(),
            cosmosAsyncDatabase.getId(),
            cosmosAsyncContainer.getId(),
            cosmosItemResponse.getDiagnostics(),
            null,
            useLegacyTracing,
            enableRequestLevelTracing,
            forceThresholdViolations);
        mockTracer.reset();

        CosmosItemResponse<Object> deleteItemResponse = cosmosAsyncContainer
            .deleteItem(ITEM_ID, PartitionKey.NONE, requestOptions)
            .block();
        assertThat(containerResponse).isNotNull();
        verifyTracerAttributes(
            mockTracer,
            "deleteItem." + cosmosAsyncContainer.getId(),
            cosmosAsyncDatabase.getId(),
            cosmosAsyncContainer.getId(),
            deleteItemResponse.getDiagnostics(),
            null,
            useLegacyTracing,
            enableRequestLevelTracing,
            forceThresholdViolations);
        mockTracer.reset();

        CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions();
        Flux<FeedResponse<ObjectNode>> flux = cosmosAsyncContainer
            .readAllItems(queryRequestOptions, ObjectNode.class)
            .byPage();
        FeedResponse<ObjectNode> feedItemResponse = flux
            .blockFirst();
        assertThat(feedItemResponse).isNotNull();
        verifyTracerAttributes(
            mockTracer,
            "readAllItems." + cosmosAsyncContainer.getId(),
            cosmosAsyncDatabase.getId(),
            cosmosAsyncContainer.getId(),
            feedItemResponse.getCosmosDiagnostics(),
            null,
            useLegacyTracing,
            enableRequestLevelTracing,
            forceThresholdViolations);
        mockTracer.reset();

        String query = "select * from c where c.id = '" + ITEM_ID + "'";
        feedItemResponse = cosmosAsyncContainer
            .queryItems(query, queryRequestOptions, ObjectNode.class)
            .byPage()
            .blockFirst();
        assertThat(containerResponse).isNotNull();
        verifyTracerAttributes(
            mockTracer,
            "queryItems." + cosmosAsyncContainer.getId(),
            cosmosAsyncDatabase.getId(),
            cosmosAsyncContainer.getId(),
            feedItemResponse.getCosmosDiagnostics(),
            null,
            useLegacyTracing,
            enableRequestLevelTracing,
            forceThresholdViolations);
        mockTracer.reset();
    }

    @Test(groups = {"simple", "emulator"}, dataProvider = "traceTestCaseProvider", timeOut = TIMEOUT)
    public void cosmosAsyncScripts(
        boolean useLegacyTracing,
        boolean enableRequestLevelTracing,
        boolean forceThresholdViolations) throws Exception {

        TracerUnderTest mockTracer = Mockito.spy(new TracerUnderTest());

        createAndInitializeDiagnosticsProvider(
            mockTracer, useLegacyTracing, enableRequestLevelTracing, forceThresholdViolations);

        FeedResponse<CosmosStoredProcedureProperties> sprocFeedResponse = cosmosAsyncContainer
            .getScripts()
            .readAllStoredProcedures(new CosmosQueryRequestOptions())
            .byPage()
            .single()
            .block();
        assertThat(sprocFeedResponse).isNotNull();
        verifyTracerAttributes(
            mockTracer,
            "readAllStoredProcedures." + cosmosAsyncContainer.getId(),
            cosmosAsyncDatabase.getId(),
            cosmosAsyncContainer.getId(),
            sprocFeedResponse.getCosmosDiagnostics(),
            null,
            useLegacyTracing,
            false, // will always go through Gateway
            forceThresholdViolations);
        mockTracer.reset();

        FeedResponse<CosmosTriggerProperties> triggerFeedResponse = cosmosAsyncContainer
            .getScripts()
            .readAllTriggers(new CosmosQueryRequestOptions())
            .byPage()
            .single()
            .block();
        assertThat(triggerFeedResponse).isNotNull();
        verifyTracerAttributes(
            mockTracer,
            "readAllTriggers." + cosmosAsyncContainer.getId(),
            cosmosAsyncDatabase.getId(),
            cosmosAsyncContainer.getId(),
            triggerFeedResponse.getCosmosDiagnostics(),
            null,
            useLegacyTracing,
            false, // will always go through Gateway
            forceThresholdViolations);
        mockTracer.reset();

        FeedResponse<CosmosUserDefinedFunctionProperties> udfFeedResponse = cosmosAsyncContainer
            .getScripts()
            .readAllUserDefinedFunctions(new CosmosQueryRequestOptions())
            .byPage()
            .single()
            .block();
        assertThat(udfFeedResponse).isNotNull();
        verifyTracerAttributes(
            mockTracer,
            "readAllUserDefinedFunctions." + cosmosAsyncContainer.getId(),
            cosmosAsyncDatabase.getId(),
            cosmosAsyncContainer.getId(),
            udfFeedResponse.getCosmosDiagnostics(),
            null,
            useLegacyTracing,
            false, // will always go through Gateway
            forceThresholdViolations);
        mockTracer.reset();

        CosmosUserDefinedFunctionProperties cosmosUserDefinedFunctionProperties =
            getCosmosUserDefinedFunctionProperties();
        CosmosUserDefinedFunctionResponse resultUdf = cosmosAsyncContainer
            .getScripts()
            .createUserDefinedFunction(cosmosUserDefinedFunctionProperties)
            .block();
        assertThat(resultUdf).isNotNull();
        verifyTracerAttributes(
            mockTracer,
            "createUserDefinedFunction." + cosmosAsyncContainer.getId(),
            cosmosAsyncDatabase.getId(),
            cosmosAsyncContainer.getId(),
            resultUdf.getDiagnostics(),
            null,
            useLegacyTracing,
            false, // will always go through Gateway
            forceThresholdViolations);
        mockTracer.reset();

        resultUdf = cosmosAsyncContainer
            .getScripts()
            .getUserDefinedFunction(cosmosUserDefinedFunctionProperties.getId())
            .read()
            .block();
        assertThat(resultUdf).isNotNull();
        verifyTracerAttributes(
            mockTracer,
            "readUserDefinedFunction." + cosmosAsyncContainer.getId(),
            cosmosAsyncDatabase.getId(),
            cosmosAsyncContainer.getId(),
            resultUdf.getDiagnostics(),
            null,
            useLegacyTracing,
            false, // will always go through Gateway
            forceThresholdViolations);
        mockTracer.reset();

        cosmosUserDefinedFunctionProperties.setBody("function() {var x = 15;}");
        resultUdf = cosmosAsyncContainer
            .getScripts()
            .getUserDefinedFunction(resultUdf.getProperties().getId())
            .replace(resultUdf.getProperties())
            .block();
        assertThat(resultUdf).isNotNull();
        verifyTracerAttributes(
            mockTracer,
            "replaceUserDefinedFunction." + cosmosAsyncContainer.getId(),
            cosmosAsyncDatabase.getId(),
            cosmosAsyncContainer.getId(),
            resultUdf.getDiagnostics(),
            null,
            useLegacyTracing,
            false, // will always go through Gateway
            forceThresholdViolations);
        mockTracer.reset();

        resultUdf = cosmosAsyncContainer
            .getScripts()
            .getUserDefinedFunction(cosmosUserDefinedFunctionProperties.getId())
            .delete()
            .block();
        assertThat(resultUdf).isNotNull();
        verifyTracerAttributes(
            mockTracer,
            "deleteUserDefinedFunction." + cosmosAsyncContainer.getId(),
            cosmosAsyncDatabase.getId(),
            cosmosAsyncContainer.getId(),
            resultUdf.getDiagnostics(),
            null,
            useLegacyTracing,
            false, // will always go through Gateway
            forceThresholdViolations);
        mockTracer.reset();

        CosmosTriggerProperties cosmosTriggerProperties = getCosmosTriggerProperties();
        CosmosTriggerResponse resultTrigger =
            cosmosAsyncContainer.getScripts().createTrigger(cosmosTriggerProperties).block();
        assertThat(resultTrigger).isNotNull();
        verifyTracerAttributes(
            mockTracer,
            "createTrigger." + cosmosAsyncContainer.getId(),
            cosmosAsyncDatabase.getId(),
            cosmosAsyncContainer.getId(),
            resultTrigger.getDiagnostics(),
            null,
            useLegacyTracing,
            false, // will always go through Gateway
            forceThresholdViolations);
        mockTracer.reset();

        resultTrigger = cosmosAsyncContainer.getScripts().getTrigger(cosmosTriggerProperties.getId()).read().block();
        assertThat(resultTrigger).isNotNull();
        verifyTracerAttributes(
            mockTracer,
            "readTrigger." + cosmosAsyncContainer.getId(),
            cosmosAsyncDatabase.getId(),
            cosmosAsyncContainer.getId(),
            resultTrigger.getDiagnostics(),
            null,
            useLegacyTracing,
            false, // will always go through Gateway
            forceThresholdViolations);
        mockTracer.reset();

        resultTrigger =
            cosmosAsyncContainer
                .getScripts()
                .getTrigger(cosmosTriggerProperties.getId())
                .replace(resultTrigger.getProperties()).block();
        assertThat(resultTrigger).isNotNull();
        verifyTracerAttributes(
            mockTracer,
            "replaceTrigger." + cosmosAsyncContainer.getId(),
            cosmosAsyncDatabase.getId(),
            cosmosAsyncContainer.getId(),
            resultTrigger.getDiagnostics(),
            null,
            useLegacyTracing,
            false, // will always go through Gateway
            forceThresholdViolations);
        mockTracer.reset();

        resultTrigger = cosmosAsyncContainer
            .getScripts()
            .getTrigger(cosmosTriggerProperties.getId())
            .delete()
            .block();
        assertThat(resultTrigger).isNotNull();
        verifyTracerAttributes(
            mockTracer,
            "deleteTrigger." + cosmosAsyncContainer.getId(),
            cosmosAsyncDatabase.getId(),
            cosmosAsyncContainer.getId(),
            resultTrigger.getDiagnostics(),
            null,
            useLegacyTracing,
            false, // will always go through Gateway
            forceThresholdViolations);
        mockTracer.reset();

        CosmosStoredProcedureProperties procedureProperties = getCosmosStoredProcedureProperties();
        CosmosStoredProcedureResponse resultSproc =
            cosmosAsyncContainer.getScripts().createStoredProcedure(procedureProperties).block();
        assertThat(resultSproc).isNotNull();
        verifyTracerAttributes(
            mockTracer,
            "createStoredProcedure." + cosmosAsyncContainer.getId(),
            cosmosAsyncDatabase.getId(),
            cosmosAsyncContainer.getId(),
            resultSproc.getDiagnostics(),
            null,
            useLegacyTracing,
            false, // will always go through Gateway
            forceThresholdViolations);
        mockTracer.reset();

        resultSproc = cosmosAsyncContainer.getScripts().getStoredProcedure(procedureProperties.getId()).read().block();
        assertThat(resultSproc).isNotNull();
        verifyTracerAttributes(
            mockTracer,
            "readStoredProcedure." + cosmosAsyncContainer.getId(),
            cosmosAsyncDatabase.getId(),
            cosmosAsyncContainer.getId(),
            resultSproc.getDiagnostics(),
            null,
            useLegacyTracing,
            false, // will always go through Gateway
            forceThresholdViolations);
        mockTracer.reset();

        resultSproc = cosmosAsyncContainer
            .getScripts()
            .getStoredProcedure(procedureProperties.getId())
            .replace(resultSproc.getProperties())
            .block();
        assertThat(resultSproc).isNotNull();
        verifyTracerAttributes(
            mockTracer,
            "replaceStoredProcedure." + cosmosAsyncContainer.getId(),
            cosmosAsyncDatabase.getId(),
            cosmosAsyncContainer.getId(),
            resultSproc.getDiagnostics(),
            null,
            useLegacyTracing,
            false, // will always go through Gateway
            forceThresholdViolations);
        mockTracer.reset();


        resultSproc =
            cosmosAsyncContainer.getScripts().getStoredProcedure(procedureProperties.getId()).delete().block();
        assertThat(resultSproc).isNotNull();
        verifyTracerAttributes(
            mockTracer,
            "deleteStoredProcedure." + cosmosAsyncContainer.getId(),
            cosmosAsyncDatabase.getId(),
            cosmosAsyncContainer.getId(),
            resultSproc.getDiagnostics(),
            null,
            useLegacyTracing,
            false, // will always go through Gateway
            forceThresholdViolations);
        mockTracer.reset();
    }

    @Test(groups = {"simple", "emulator"}, dataProvider = "traceTestCaseProvider", timeOut = TIMEOUT)
    public void tracerExceptionSpan(
        boolean useLegacyTracing,
        boolean enableRequestLevelTracing,
        boolean forceThresholdViolations) throws Exception {

        TracerUnderTest mockTracer = Mockito.spy(new TracerUnderTest());

        createAndInitializeDiagnosticsProvider(
            mockTracer, useLegacyTracing, enableRequestLevelTracing, forceThresholdViolations);


        ObjectNode item = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions();
        CosmosItemResponse<ObjectNode> cosmosItemResponse = cosmosAsyncContainer
            .createItem(item, requestOptions)
            .block();
        assertThat(cosmosItemResponse).isNotNull();
        verifyTracerAttributes(
            mockTracer,
            "createItem." + cosmosAsyncContainer.getId(),
            cosmosAsyncDatabase.getId(),
            cosmosAsyncContainer.getId(),
            cosmosItemResponse.getDiagnostics(),
            null,
            useLegacyTracing,
            enableRequestLevelTracing,
            forceThresholdViolations);
        mockTracer.reset();

        CosmosException cosmosError = null;
        try {
            PartitionKey partitionKey = new PartitionKey("wrongPk");
            cosmosAsyncContainer.readItem("testDoc", partitionKey, null, ObjectNode.class).block();
            fail("readItem should fail due to wrong pk");
        } catch (CosmosException error) {
            assertThat(error.getStatusCode()).isEqualTo(404);
            cosmosError = error;
        }

        verifyTracerAttributes(
            mockTracer,
            "readItem." + cosmosAsyncContainer.getId(),
            cosmosAsyncDatabase.getId(),
            cosmosAsyncContainer.getId(),
            cosmosError.getDiagnostics(),
            cosmosError,
            useLegacyTracing,
            enableRequestLevelTracing,
            forceThresholdViolations);
        mockTracer.reset();
    }

    @AfterClass(groups = {"emulator"}, timeOut = SETUP_TIMEOUT)
    public void afterClass() {
        LifeCycleUtils.closeQuietly(client);
    }

    private void verifyTracerAttributes(
        TracerUnderTest mockTracer,
        String methodName,
        String databaseName,
        String containerName,
        CosmosDiagnostics cosmosDiagnostics,
        CosmosException error,
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
            containerName,
            cosmosDiagnostics,
            error,
            enableRequestLevelTracing);
    }

    private void verifyOTelTracerAttributes(
        TracerUnderTest mockTracer,
        String methodName,
        String databaseName,
        String containerName,
        CosmosDiagnostics cosmosDiagnostics,
        CosmosException error,
        boolean enableRequestLevelTracing) {

        CosmosDiagnosticsContext ctx = DiagnosticsProvider.getCosmosDiagnosticsContextFromTraceContextOrThrow(
            mockTracer.context
        );



        Map<String, Object> attributes = mockTracer.attributes;
        if (databaseName != null) {
            assertThat(attributes.get("db.name")).isEqualTo(databaseName);
            assertThat(ctx.getDatabaseName()).isEqualTo(databaseName);
        }

        if (containerName != null) {
            assertThat(attributes.get("db.cosmosdb.container")).isEqualTo(containerName);
            assertThat(ctx.getContainerName()).isEqualTo(containerName);
        }

        assertThat(attributes.get("db.system")).isEqualTo("cosmosdb");
        assertThat(attributes.get("db.operation")).isEqualTo(methodName);
        assertThat(attributes.get("net.peer.name")).isEqualTo("localhost");

        assertThat(attributes.get("db.cosmosdb.operation_type")).isEqualTo(ctx.getOperationType());
        assertThat(attributes.get("db.cosmosdb.resource_type")).isEqualTo(ctx.getResourceType());

        verifyOTelTracerDiagnostics(cosmosDiagnostics, mockTracer);

        verifyOTelTracerTransport(
            cosmosDiagnostics, error,  mockTracer, enableRequestLevelTracing);

        if (error != null) {
            assertThat(attributes.get("exception.type")).isEqualTo("com.azure.cosmos.CosmosException");
            assertThat(attributes.get("exception.message")).isEqualTo(error.getMessageWithoutDiagnostics());

            StringWriter stackWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stackWriter);
            error.printStackTrace(printWriter);
            printWriter.flush();
            stackWriter.flush();
            assertThat(stackWriter.toString().contains((String)attributes.get("exception.stacktrace")))
                .isEqualTo(true);
            printWriter.close();
        }
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

            if (ctx.isCompleted() && (ctx.isFailure() || ctx.isThresholdViolated())) {
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
                                           CosmosException error,
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
            if (error == null) {
                assertThat(mockTracer.events).anyMatch(e -> e.name.equals("rntbd.request"));
            }
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
                        String eventName = "Diagnostics for PKRange " + pkRangeId;
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
                List<EventRecord> filteredEvents =
                    mockTracer.events.stream().filter(e -> e.name.equals(eventName)).collect(Collectors.toList());
                assertThat(filteredEvents).hasSize(1);
                assertThat(filteredEvents.size()).isGreaterThanOrEqualTo(1);
                assertThat(filteredEvents.get(0).attributes.get("Query Metrics"))
                    .isEqualTo(queryMetrics.getValue().toString());
            }
        }
    }

    private DiagnosticsProvider createAndInitializeDiagnosticsProvider(TracerUnderTest mockTracer,
                                                                       boolean useLegacyTracing,
                                                                       boolean enableRequestLevelTracing,
                                                                       boolean forceThresholdViolations) {
        CosmosDiagnosticsThresholds thresholds = forceThresholdViolations ?
            new CosmosDiagnosticsThresholds()
                .configureLatencyThresholds(Duration.ZERO, Duration.ZERO)
            : new CosmosDiagnosticsThresholds()
                .configureLatencyThresholds(Duration.ofDays(1), Duration.ofDays(1));

        thresholds.configureStatusCodeHandling(404, 404, true);

        CosmosClientTelemetryConfig clientTelemetryConfig = new CosmosClientTelemetryConfig()
            .diagnosticsThresholds(thresholds);


        if (useLegacyTracing) {
            clientTelemetryConfig.useLegacyOpenTelemetryTracing();
        }

        if (enableRequestLevelTracing) {
            clientTelemetryConfig.enableTransportLevelTracing();
        }

        clientTelemetryConfig.tracer(mockTracer);

        DiagnosticsProvider tracerProvider = new DiagnosticsProvider(clientTelemetryConfig);
        ReflectionUtils.setClientTelemetryConfig(client, clientTelemetryConfig);
        ReflectionUtils.setDiagnosticsProvider(client, tracerProvider);

        return tracerProvider;
    }

    private ObjectNode getDocumentDefinition(String documentId) {
        String json = String.format(
            "{ \"id\": \"%s\" }",
            documentId);

        try {
            return
                OBJECT_MAPPER.readValue(json, ObjectNode.class);
        } catch (JsonProcessingException jsonError) {
            Assertions.fail("No json processing error expected", jsonError);

            throw new IllegalStateException("No json processing error expected", jsonError);
        }
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
