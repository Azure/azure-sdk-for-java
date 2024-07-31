// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.DiagnosticsProvider;
import com.azure.cosmos.implementation.FeedResponseDiagnostics;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers.CosmosDiagnosticsHelper;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers.CosmosDiagnosticsHelper.CosmosDiagnosticsAccessor;
import com.azure.cosmos.implementation.LifeCycleUtils;
import com.azure.cosmos.implementation.QueryMetrics;
import com.azure.cosmos.implementation.RequestTimeline;
import com.azure.cosmos.implementation.SerializationDiagnosticsContext;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.models.CosmosItemIdentity;
import com.azure.cosmos.models.ShowQueryMode;
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
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.models.ThroughputResponse;
import com.azure.cosmos.models.TriggerOperation;
import com.azure.cosmos.models.TriggerType;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.test.faultinjection.FaultInjectionCondition;
import com.azure.cosmos.test.faultinjection.FaultInjectionConditionBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionConnectionType;
import com.azure.cosmos.test.faultinjection.FaultInjectionEndpointBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.test.faultinjection.FaultInjectionResultBuilders;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionRuleBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
import com.azure.cosmos.test.faultinjection.IFaultInjectionResult;
import com.azure.cosmos.test.implementation.faultinjection.FaultInjectorProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.assertj.core.api.Assertions;
import org.mockito.Mockito;
import org.testng.SkipException;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

public class CosmosTracerTest extends TestSuiteBase {
    private final static ObjectMapper OBJECT_MAPPER = Utils.getSimpleObjectMapper();
    private static String ITEM_ID;

    private static final AtomicInteger testCaseCount = new AtomicInteger(0);

    private CosmosDiagnosticsAccessor cosmosDiagnosticsAccessor;
    CosmosAsyncClient client;
    CosmosAsyncDatabase cosmosAsyncDatabase;
    CosmosAsyncContainer cosmosAsyncContainer;

    @Factory(dataProvider = "clientBuildersWithDirectSessionIncludeComputeGateway")
    public CosmosTracerTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder.contentResponseOnWriteEnabled(true));
    }

    @BeforeClass(groups = { "fast", "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        try {
            client = getClientBuilder().buildAsyncClient();
            cosmosAsyncDatabase = getSharedCosmosDatabase(client);
            cosmosAsyncContainer = getSharedMultiPartitionCosmosContainerWithIdAsPartitionKey(client);
            cosmosDiagnosticsAccessor = CosmosDiagnosticsHelper.getCosmosDiagnosticsAccessor();
        } catch (Throwable error) {
            logger.error("BeforeClass of CosmosTracerTest failed unexpectedly", error);
            error.printStackTrace();
            throw error;
        }
    }

    @Override
    public String resolveTestNameSuffix(Object[] row) {
        if (row != null && row.length == 4) {
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

            sb.append("|").append((double)row[3]);

            return sb.toString();
        }

        return "";
    }

    @DataProvider(name = "traceTestCaseProvider")
    private Object[][] traceTestCaseProvider() {
        return new Object[][]{
            new Object[] { true, false, true, 1d },
            new Object[] { true, false, false, 1d },
            new Object[] { false, false, false, 1d },
            new Object[] { false, true, false, 1d },
            new Object[] { false, false, true, 1d },
            new Object[] { false, true, true, 1d },
            new Object[] { false, true, true, 0.99999999 },
            new Object[] { false, true, true, 0d },
        };
    }
    
    @DataProvider(name = "traceTestCaseProviderAsyncContainer")
    private Object[][] traceTestCaseProviderAsyncContainer() {
        return new Object[][]{
            new Object[] { true, false, ShowQueryMode.NONE, true, 1d },
            new Object[] { true, false, ShowQueryMode.NONE, false, 1d },
            new Object[] { false, false, ShowQueryMode.NONE, false, 1d },
            new Object[] { false, true, ShowQueryMode.NONE, false, 1d },
            new Object[] { false, false, ShowQueryMode.NONE, true, 1d },
            new Object[] { false, true, ShowQueryMode.NONE, true, 1d },
            new Object[] { false, true, ShowQueryMode.NONE, true, 0.99999999 },
            new Object[] { false, true, ShowQueryMode.NONE, true, 0d },
            
            new Object[] { false, true, ShowQueryMode.ALL, true, 1d },
            new Object[] { false, false, ShowQueryMode.ALL, true, 1d },
            new Object[] { false, false, ShowQueryMode.ALL, false, 1d },

            new Object[] { false, true, ShowQueryMode.PARAMETERIZED_ONLY, true, 1d },
            new Object[] { false, false, ShowQueryMode.PARAMETERIZED_ONLY, true, 1d },
            new Object[] { false, false, ShowQueryMode.PARAMETERIZED_ONLY, false, 1d }
        };
    }

    @Test(groups = { "fast", "simple" }, dataProvider = "traceTestCaseProvider", timeOut = TIMEOUT)
    public void cosmosAsyncClient(
        boolean useLegacyTracing,
        boolean enableRequestLevelTracing,
        boolean forceThresholdViolations,
        double samplingRate) throws Exception {

        TracerUnderTest mockTracer = new TracerUnderTest();

        createAndInitializeDiagnosticsProvider(
            mockTracer, useLegacyTracing, enableRequestLevelTracing, ShowQueryMode.NONE, forceThresholdViolations, samplingRate);

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
            forceThresholdViolations,
            samplingRate);
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
            forceThresholdViolations,
            samplingRate);
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
            forceThresholdViolations,
            samplingRate);
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
            forceThresholdViolations,
            samplingRate);

        mockTracer.reset();
    }

    @Test(groups = { "fast", "simple" }, dataProvider = "traceTestCaseProvider", timeOut = TIMEOUT)
    public void cosmosAsyncDatabase(
                                    boolean useLegacyTracing,
                                    boolean enableRequestLevelTracing,
                                    boolean forceThresholdViolations,
                                    double samplingRate) throws Exception {
        TracerUnderTest mockTracer = Mockito.spy(new TracerUnderTest());

        createAndInitializeDiagnosticsProvider(
            mockTracer, useLegacyTracing, enableRequestLevelTracing, ShowQueryMode.NONE, forceThresholdViolations, samplingRate);

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
            forceThresholdViolations,
            samplingRate);
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
            forceThresholdViolations,
            samplingRate);
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
            forceThresholdViolations,
            samplingRate);
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
            forceThresholdViolations,
            samplingRate);
        mockTracer.reset();
    }

    @Test(groups = { "fast", "simple" }, dataProvider = "traceTestCaseProvider", timeOut = 10 * TIMEOUT)
    public void cosmosAsyncContainerWithFaultInjectionOnCreate(
        boolean useLegacyTracing,
        boolean enableRequestLevelTracing,
        boolean forceThresholdViolations,
        double samplingRate) throws Exception {

        if (client.getConnectionPolicy().getConnectionMode() != ConnectionMode.DIRECT) {
            throw new SkipException("Failure ingestion is only supported for Direct mode currently.");
        }

        TracerUnderTest mockTracer = Mockito.spy(new TracerUnderTest());

        createAndInitializeDiagnosticsProvider(
            mockTracer, useLegacyTracing, enableRequestLevelTracing, ShowQueryMode.NONE, forceThresholdViolations, samplingRate);

        IFaultInjectionResult result = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.RESPONSE_DELAY)
            .delay(Duration.ofMillis(20))
            .build();

        FaultInjectionCondition condition = new FaultInjectionConditionBuilder()
            .operationType(FaultInjectionOperationType.CREATE_ITEM)
            .connectionType(FaultInjectionConnectionType.DIRECT)
            .build();

        String faultInjectionRuleId = "InjectedResponseDelay" + UUID.randomUUID();
        FaultInjectionRule rule = new FaultInjectionRuleBuilder(faultInjectionRuleId)
            .condition(condition)
            .result(result)
            .build();

        FaultInjectorProvider injectorProvider = (FaultInjectorProvider) cosmosAsyncContainer
            .getOrConfigureFaultInjectorProvider(() -> new FaultInjectorProvider(cosmosAsyncContainer));

        injectorProvider.configureFaultInjectionRules(Arrays.asList(rule)).block();

        ObjectNode item = getDocumentDefinition(ITEM_ID);
        CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions();

        try {
            for (boolean injectedFailureEnabled : Arrays.asList(true, false)) {

                try {
                    if (!injectedFailureEnabled) {
                        rule.disable();
                    }

                    CosmosItemResponse<ObjectNode> cosmosItemResponse = cosmosAsyncContainer
                        .createItem(item, requestOptions)
                        .block();

                    assertThat(cosmosItemResponse).isNotNull();

                    if (injectedFailureEnabled) {
                        assertThat(cosmosItemResponse.getDiagnostics().toString().contains(faultInjectionRuleId)).isTrue();
                        assertThat(cosmosItemResponse.getDiagnostics().toString().contains("faultInjectionEvaluationResults")).isFalse();
                    } else {
                        assertThat(
                            cosmosItemResponse
                                .getDiagnostics()
                                .toString()
                                .contains(faultInjectionRuleId + "[Disable or Duration reached"))
                            .isTrue();
                    }
                    verifyTracerAttributes(
                        mockTracer,
                        "createItem." + cosmosAsyncContainer.getId(),
                        cosmosAsyncDatabase.getId(),
                        cosmosAsyncContainer.getId(),
                        cosmosItemResponse.getDiagnostics(),
                        null,
                        useLegacyTracing,
                        enableRequestLevelTracing,
                        forceThresholdViolations,
                        samplingRate);

                } finally {
                    mockTracer.reset();

                    cosmosAsyncContainer
                        .deleteItem(item, requestOptions)
                        .block();
                    mockTracer.reset();
                }
            }
        }
        finally {
            rule.disable();
        }
    }

    @Test(groups = { "fast", "simple" }, dataProvider = "traceTestCaseProvider", timeOut = 10 * TIMEOUT)
    public void cosmosAsyncContainerWithFaultInjectionOnRead(
        boolean useLegacyTracing,
        boolean enableRequestLevelTracing,
        boolean forceThresholdViolations,
        double samplingRate) throws Exception {

        if (client.getConnectionPolicy().getConnectionMode() != ConnectionMode.DIRECT) {
            throw new SkipException("Failure ingestion is only supported for Direct mode currently.");
        }

        ITEM_ID =  "tracerDoc_" + testCaseCount.incrementAndGet();
        TracerUnderTest mockTracer = Mockito.spy(new TracerUnderTest());

        createAndInitializeDiagnosticsProvider(
            mockTracer, useLegacyTracing, enableRequestLevelTracing, ShowQueryMode.NONE, forceThresholdViolations, samplingRate);

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
            forceThresholdViolations,
            samplingRate);

        IFaultInjectionResult result = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.TOO_MANY_REQUEST)
            .times(2)
            .build();

        FaultInjectionCondition condition = new FaultInjectionConditionBuilder()
            .operationType(FaultInjectionOperationType.READ_ITEM)
            .connectionType(FaultInjectionConnectionType.DIRECT)
            .endpoints(new FaultInjectionEndpointBuilder(FeedRange.forLogicalPartition(new PartitionKey(ITEM_ID)))
                .replicaCount(4)
                .includePrimary(true)
                .build())
            .build();

        FaultInjectionRule rule = new FaultInjectionRuleBuilder("Injected410" + UUID.randomUUID())
            .condition(condition)
            .result(result)
            //.hitLimit(2)
            .build();

        FaultInjectorProvider injectorProvider = (FaultInjectorProvider) cosmosAsyncContainer
            .getOrConfigureFaultInjectorProvider(() -> new FaultInjectorProvider(cosmosAsyncContainer));

        injectorProvider.configureFaultInjectionRules(Arrays.asList(rule)).block();

        mockTracer.reset();

        try {
            cosmosItemResponse = cosmosAsyncContainer
                .readItem(ITEM_ID, new PartitionKey(ITEM_ID), requestOptions, ObjectNode.class)
                .block();
            assertThat(cosmosItemResponse).isNotNull();
            verifyTracerAttributes(
                mockTracer,
                "readItem." + cosmosAsyncContainer.getId(),
                cosmosAsyncDatabase.getId(),
                cosmosAsyncContainer.getId(),
                cosmosItemResponse.getDiagnostics(),
                null,
                useLegacyTracing,
                enableRequestLevelTracing,
                forceThresholdViolations,
                samplingRate);

            assertThat(cosmosItemResponse.getDiagnostics().toString().contains("Injected410")).isEqualTo(true);
            if (samplingRate > 0) {
                assertThat(cosmosItemResponse.getDiagnostics().getDiagnosticsContext().getRetryCount())
                    .isGreaterThanOrEqualTo(1);
            }

            mockTracer.reset();
        }
        finally {
            rule.disable();
        }

       cosmosAsyncContainer
            .deleteItem(item, requestOptions)
            .block();
        mockTracer.reset();

    }

    @Test(groups = { "fast", "simple" }, dataProvider = "traceTestCaseProviderAsyncContainer", timeOut = 10 * TIMEOUT)
    public void cosmosAsyncContainer(
        boolean useLegacyTracing,
        boolean enableRequestLevelTracing,
        ShowQueryMode showQueryMode,
        boolean forceThresholdViolations,
        double samplingRate) throws Exception {

        ITEM_ID =  "tracerDoc_" + testCaseCount.incrementAndGet();
        TracerUnderTest mockTracer = Mockito.spy(new TracerUnderTest());

        DiagnosticsProvider provider = createAndInitializeDiagnosticsProvider(
            mockTracer, useLegacyTracing, enableRequestLevelTracing, showQueryMode, forceThresholdViolations, samplingRate);
        
        CosmosClientTelemetryConfig telemetryConfigSnapshot = provider.getClientTelemetryConfig();

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
            forceThresholdViolations,
            samplingRate);
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
            forceThresholdViolations,
            samplingRate);
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
            forceThresholdViolations,
            samplingRate);
        mockTracer.reset();

        List<CosmosItemIdentity> createdDocs = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            // inserting high enough number of documents to make sure we have at least 1 doc on each
            // of the two partitions
            String id = ITEM_ID + "_" + i;
            createdDocs.add(new CosmosItemIdentity(new PartitionKey(id), id));
            item = getDocumentDefinition(id);
            requestOptions = new CosmosItemRequestOptions();
            cosmosItemResponse = cosmosAsyncContainer
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
                forceThresholdViolations,
                samplingRate);
            mockTracer.reset();
        }

        cosmosItemResponse = cosmosAsyncContainer.upsertItem(item, requestOptions).block();
        assertThat(cosmosItemResponse).isNotNull();
        verifyTracerAttributes(
            mockTracer,
            "upsertItem." + cosmosAsyncContainer.getId(),
            cosmosAsyncDatabase.getId(),
            cosmosAsyncContainer.getId(),
            cosmosItemResponse.getDiagnostics(),
            null,
            useLegacyTracing,
            enableRequestLevelTracing,
            forceThresholdViolations,
            samplingRate);
        mockTracer.reset();

        cosmosItemResponse = cosmosAsyncContainer
            .readItem(ITEM_ID, new PartitionKey(ITEM_ID), requestOptions, ObjectNode.class)
            .block();
        assertThat(cosmosItemResponse).isNotNull();
        verifyTracerAttributes(
            mockTracer,
            "readItem." + cosmosAsyncContainer.getId(),
            cosmosAsyncDatabase.getId(),
            cosmosAsyncContainer.getId(),
            cosmosItemResponse.getDiagnostics(),
            null,
            useLegacyTracing,
            enableRequestLevelTracing,
            forceThresholdViolations,
            samplingRate);
        mockTracer.reset();

        // also validate that samplingRate can be dynamically changed
        if (samplingRate != 0d && samplingRate != 1d) {

            for (double updatedSamplingRate : Arrays.asList(0d, 1d)) {
                telemetryConfigSnapshot.sampleDiagnostics(updatedSamplingRate);

                cosmosItemResponse = cosmosAsyncContainer
                    .readItem(ITEM_ID, new PartitionKey(ITEM_ID), requestOptions, ObjectNode.class)
                    .block();
                assertThat(cosmosItemResponse).isNotNull();
                verifyTracerAttributes(
                    mockTracer,
                    "readItem." + cosmosAsyncContainer.getId(),
                    cosmosAsyncDatabase.getId(),
                    cosmosAsyncContainer.getId(),
                    cosmosItemResponse.getDiagnostics(),
                    null,
                    useLegacyTracing,
                    enableRequestLevelTracing,
                    forceThresholdViolations,
                    updatedSamplingRate);

                mockTracer.reset();
            }

            telemetryConfigSnapshot.sampleDiagnostics(samplingRate);
        }

        CosmosItemResponse<Object> deleteItemResponse = cosmosAsyncContainer
            .deleteItem(ITEM_ID, new PartitionKey(ITEM_ID), requestOptions)
            .block();
        assertThat(deleteItemResponse).isNotNull();
        verifyTracerAttributes(
            mockTracer,
            "deleteItem." + cosmosAsyncContainer.getId(),
            cosmosAsyncDatabase.getId(),
            cosmosAsyncContainer.getId(),
            deleteItemResponse.getDiagnostics(),
            null,
            useLegacyTracing,
            enableRequestLevelTracing,
            forceThresholdViolations,
            samplingRate);
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
            forceThresholdViolations,
            samplingRate);
        mockTracer.reset();

        String query = "select * from c where c.id = '" + ITEM_ID + "'";
        feedItemResponse = cosmosAsyncContainer
            .queryItems(query, queryRequestOptions, ObjectNode.class)
            .byPage()
            .blockFirst();
        assertThat(feedItemResponse).isNotNull();
        verifyTracerAttributes(
            mockTracer,
            "queryItems." + cosmosAsyncContainer.getId(),
            cosmosAsyncDatabase.getId(),
            cosmosAsyncContainer.getId(),
            feedItemResponse.getCosmosDiagnostics(),
            null,
            useLegacyTracing,
            enableRequestLevelTracing,
            showQueryMode,
            query,
            forceThresholdViolations,
            null,
            samplingRate);
        mockTracer.reset();

        CosmosQueryRequestOptions queryRequestOptionsWithCustomOpsId = new CosmosQueryRequestOptions()
            .setQueryName("CustomQueryName");
        query = "select * from c where c.id = '" + ITEM_ID + "'";
        feedItemResponse = cosmosAsyncContainer
            .queryItems(query, queryRequestOptionsWithCustomOpsId, ObjectNode.class)
            .byPage()
            .blockFirst();
        assertThat(feedItemResponse).isNotNull();
        verifyTracerAttributes(
            mockTracer,
            "queryItems." + cosmosAsyncContainer.getId(),
            cosmosAsyncDatabase.getId(),
            cosmosAsyncContainer.getId(),
            feedItemResponse.getCosmosDiagnostics(),
            null,
            useLegacyTracing,
            enableRequestLevelTracing,
            showQueryMode,
            query,
            forceThresholdViolations,
            "CustomQueryName",
            
            samplingRate);
        mockTracer.reset();
        
        feedItemResponse = cosmosAsyncContainer
                .queryItems(query, ObjectNode.class)
                .byPage()
                .blockFirst();
            assertThat(feedItemResponse).isNotNull();
            verifyTracerAttributes(
                mockTracer,
                "queryItems." + cosmosAsyncContainer.getId(),
                cosmosAsyncDatabase.getId(),
                cosmosAsyncContainer.getId(),
                feedItemResponse.getCosmosDiagnostics(),
                null,
                useLegacyTracing,
                enableRequestLevelTracing,
                showQueryMode,
                query,
                forceThresholdViolations,
                null,
                samplingRate);
        mockTracer.reset();

        queryRequestOptions = new CosmosQueryRequestOptions();
        query = "select * from c";
        Iterator<FeedResponse<ObjectNode>> responseIterator = cosmosAsyncContainer
            .queryItems(query, queryRequestOptions, ObjectNode.class)
            .byPage(1000)
            .toIterable()
            .iterator();

        CosmosDiagnostics lastDiagnostics = null;
        while(responseIterator.hasNext()) {
            feedItemResponse = responseIterator.next();
            assertThat(feedItemResponse).isNotNull();

            lastDiagnostics = feedItemResponse.getCosmosDiagnostics();
            assertThat(lastDiagnostics).isNotNull();
        }

        assertThat(lastDiagnostics).isNotNull();
        verifyTracerAttributes(
            mockTracer,
            "queryItems." + cosmosAsyncContainer.getId(),
            cosmosAsyncDatabase.getId(),
            cosmosAsyncContainer.getId(),
            lastDiagnostics,
            null,
            useLegacyTracing,
            enableRequestLevelTracing,
            showQueryMode,
            query,
            forceThresholdViolations,
            null,
            samplingRate);

        mockTracer.reset();

        List<SqlParameter> paramList = new LinkedList<>();
        query = "select * from c where c.id=@Id";
        paramList.add(new SqlParameter("@Id", "1238"));
        SqlQuerySpec querySpec = new SqlQuerySpec(query, paramList);
        feedItemResponse = cosmosAsyncContainer
                .queryItems(querySpec, new CosmosQueryRequestOptions(), ObjectNode.class)
                .byPage()
                .blockFirst();
        assertThat(feedItemResponse).isNotNull();
        verifyTracerAttributes(
                mockTracer,
                "queryItems." + cosmosAsyncContainer.getId(),
                cosmosAsyncDatabase.getId(),
                cosmosAsyncContainer.getId(),
                feedItemResponse.getCosmosDiagnostics(),
                null,
                useLegacyTracing,
                enableRequestLevelTracing,
                showQueryMode,
                query,
                forceThresholdViolations,
                null,
                samplingRate);
        mockTracer.reset();
        
        feedItemResponse = cosmosAsyncContainer.queryItems(querySpec, ObjectNode.class)
                .byPage()
                .blockFirst();
        assertThat(feedItemResponse).isNotNull();
        verifyTracerAttributes(
                mockTracer,
                "queryItems." + cosmosAsyncContainer.getId(),
                cosmosAsyncDatabase.getId(),
                cosmosAsyncContainer.getId(),
                feedItemResponse.getCosmosDiagnostics(),
                null,
                useLegacyTracing,
                enableRequestLevelTracing,
                showQueryMode,
                query,
                forceThresholdViolations,
                null,
                samplingRate);
        mockTracer.reset();

        // read many single item
        feedItemResponse = cosmosAsyncContainer.readMany(Arrays.asList(createdDocs.get(0)), ObjectNode.class)
                                               .block();
        assertThat(feedItemResponse).isNotNull();
        verifyTracerAttributes(
            mockTracer,
            "readManyItems." + cosmosAsyncContainer.getId(),
            cosmosAsyncDatabase.getId(),
            cosmosAsyncContainer.getId(),
            feedItemResponse.getCosmosDiagnostics(),
            null,
            useLegacyTracing,
            enableRequestLevelTracing,
            showQueryMode,
            query,
            forceThresholdViolations,
            "readMany",
            samplingRate);
        mockTracer.reset();
    }

    @Test(groups = { "fast", "simple" }, dataProvider = "traceTestCaseProvider", timeOut = TIMEOUT)
    public void cosmosAsyncScripts(
        boolean useLegacyTracing,
        boolean enableRequestLevelTracing,
        boolean forceThresholdViolations,
        double samplingRate) throws Exception {

        if (this.client.getContextClient().getGlobalEndpointManager().getAvailableWriteEndpoints().size() > 1) {

            throw new SkipException("Tests would take too long to run on multi master account because " +
                "scrips etc. creation can take several seconds for replication.");
        }

        TracerUnderTest mockTracer = Mockito.spy(new TracerUnderTest());

        createAndInitializeDiagnosticsProvider(
            mockTracer, useLegacyTracing, enableRequestLevelTracing, ShowQueryMode.NONE, forceThresholdViolations, samplingRate);

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
            forceThresholdViolations,
            samplingRate);
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
            forceThresholdViolations,
            samplingRate);
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
            forceThresholdViolations,
            samplingRate);
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
            forceThresholdViolations,
            samplingRate);
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
            forceThresholdViolations,
            samplingRate);
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
            forceThresholdViolations,
            samplingRate);
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
            forceThresholdViolations,
            samplingRate);
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
            forceThresholdViolations,
            samplingRate);
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
            forceThresholdViolations,
            samplingRate);
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
            forceThresholdViolations,
            samplingRate);
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
            forceThresholdViolations,
            samplingRate);
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
            forceThresholdViolations,
            samplingRate);
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
            forceThresholdViolations,
            samplingRate);
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
            forceThresholdViolations,
            samplingRate);
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
            forceThresholdViolations,
            samplingRate);
        mockTracer.reset();
    }

    @Test(groups = { "fast", "simple" }, dataProvider = "traceTestCaseProvider", timeOut = TIMEOUT)
    public void tracerExceptionSpan(
        boolean useLegacyTracing,
        boolean enableRequestLevelTracing,
        boolean forceThresholdViolations,
        double samplingRate) throws Exception {

        TracerUnderTest mockTracer = Mockito.spy(new TracerUnderTest());

        createAndInitializeDiagnosticsProvider(
            mockTracer, useLegacyTracing, enableRequestLevelTracing, ShowQueryMode.NONE, forceThresholdViolations, samplingRate);


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
            forceThresholdViolations,
            samplingRate);
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
            forceThresholdViolations,
            samplingRate);
        mockTracer.reset();
    }

    @AfterClass(groups = { "fast", "simple" }, timeOut = SETUP_TIMEOUT)
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
        boolean forceThresholdViolation,
        double samplingRate) throws JsonProcessingException {

        verifyTracerAttributes(
            mockTracer,
            methodName,
            databaseName,
            containerName,
            cosmosDiagnostics,
            error,
            useLegacyTracing,
            enableRequestLevelTracing,
            forceThresholdViolation,
            true,
            samplingRate);
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
        boolean forceThresholdViolation,
        boolean shouldExpectOperationTrace,
        double samplingRate) throws JsonProcessingException {

        verifyTracerAttributes(
            mockTracer,
            methodName,
            databaseName,
            containerName,
            cosmosDiagnostics,
            error,
            useLegacyTracing,
            enableRequestLevelTracing,
            forceThresholdViolation,
            null,
            samplingRate);
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
        boolean forceThresholdViolation,
        String customOperationId,
        double samplingRate) throws JsonProcessingException {

        if (useLegacyTracing) {
            verifyLegacyTracerAttributes(
                mockTracer,
                methodName,
                databaseName,
                cosmosDiagnostics,
                enableRequestLevelTracing,
                forceThresholdViolation,
                samplingRate);
            return;
        }

        verifyOTelTracerAttributes(
            mockTracer,
            methodName,
            databaseName,
            containerName,
            cosmosDiagnostics,
            error,
            enableRequestLevelTracing,
            ShowQueryMode.NONE,
            null,
            customOperationId,
            samplingRate);
    }
    
    private void verifyTracerAttributes(TracerUnderTest mockTracer,
            String methodName,
            String databaseName,
            String containerName,
            CosmosDiagnostics cosmosDiagnostics,
            CosmosException error,
            boolean useLegacyTracing,
            boolean enableRequestLevelTracing,
            ShowQueryMode showQueryMode,
            String queryStatement,
            boolean forceThresholdViolation,
            String customOperationId,
            double samplingRate) throws JsonProcessingException {

            if (useLegacyTracing) {
                verifyLegacyTracerAttributes(
                    mockTracer,
                    methodName,
                    databaseName,
                    cosmosDiagnostics,
                    enableRequestLevelTracing,
                    forceThresholdViolation,
                    samplingRate);
                return;
            }

            verifyOTelTracerAttributes(
                mockTracer,
                methodName,
                databaseName,
                containerName,
                cosmosDiagnostics,
                error,
                enableRequestLevelTracing,
                showQueryMode,
                queryStatement,
                customOperationId,
                samplingRate);
    }

    private void verifyOTelTracerAttributes(
        TracerUnderTest mockTracer,
        String methodName,
        String databaseName,
        String containerName,
        CosmosDiagnostics cosmosDiagnostics,
        CosmosException error,
        boolean enableRequestLevelTracing,
        ShowQueryMode showQueryMode,
        String queryStatement,
        String customOperationId,
        double samplingRate) {

        assertThat(mockTracer).isNotNull();
        assertThat(cosmosDiagnostics).isNotNull();
        assertThat(cosmosDiagnostics.getSamplingRateSnapshot()).isEqualTo(samplingRate);
        assertThat(
            cosmosDiagnostics.toString().contains("\"samplingRateSnapshot\":" + String.valueOf(samplingRate)))
            .isEqualTo(true);

        TracerUnderTest.SpanRecord currentSpan = mockTracer.getCurrentSpan();

        if (samplingRate == 0) {
            return;
        }

        assertThat(currentSpan).isNotNull();
        assertThat(currentSpan.getContext()).isNotNull();

        assertThat(cosmosDiagnostics.getDiagnosticsContext()).isNotNull();
        CosmosDiagnosticsContext ctx = cosmosDiagnostics.getDiagnosticsContext();

        Map<String, Object> attributes = currentSpan.getAttributes();
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
        assertThat(attributes.get("net.peer.name")).isNotNull();
        if (attributes.get("db.cosmosdb.is_empty_completion") == null) {

            assertThat(attributes.get("db.cosmosdb.request_content_length")).isNotNull();

            assertThat(attributes.get("db.cosmosdb.operation_type")).isEqualTo(ctx.getOperationType());
            if (customOperationId != null) {
                assertThat(attributes.get("db.cosmosdb.operation_id")).isEqualTo(customOperationId);
                assertThat(attributes.get("db.cosmosdb.operation_id")).isEqualTo(ctx.getOperationId());
            } else {
                assertThat(attributes.get("db.cosmosdb.operation_id")).isNull();
            }

            assertThat(attributes.get("db.cosmosdb.resource_type")).isEqualTo(ctx.getResourceType());
            assertThat(attributes.get("db.cosmosdb.connection_mode"))
                .isEqualTo(client.getConnectionPolicy().getConnectionMode().toString().toLowerCase(Locale.ROOT));
            assertThat(attributes.get("user_agent.original")).isEqualTo(client.getUserAgent());
            assertThat(attributes.get("db.cosmosdb.client_id")).isEqualTo(client.getClientCorrelationTag().getValue());

            verifyOTelTracerDiagnostics(cosmosDiagnostics, mockTracer);

            verifyOTelTracerTransport(
                cosmosDiagnostics, error, mockTracer, enableRequestLevelTracing);
            
            String dbStatement = (String) attributes.get("db.statement");

            boolean isReadMany = "readMany".equals(cosmosDiagnostics.getDiagnosticsContext().getOperationId());
            if (!isReadMany && ShowQueryMode.ALL.equals(showQueryMode)) {
                assertThat(attributes.get("db.statement")).isNotNull();
                assertThat(attributes.get("db.statement")).isEqualTo(queryStatement);
            } else if (!isReadMany && ShowQueryMode.PARAMETERIZED_ONLY.equals(showQueryMode)
                    && null != dbStatement && dbStatement.contains("@")) {
                assertThat(attributes.get("db.statement")).isNotNull();
                assertThat(attributes.get("db.statement")).isEqualTo(queryStatement);
            } else {
                assertThat(attributes.get("db.statement")).isNull();
            }

            if (error != null) {
                assertThat(attributes.get("exception.type")).isEqualTo("com.azure.cosmos.CosmosException");
                assertThat(attributes.get("exception.message")).isEqualTo(error.getShortMessage());

                StringWriter stackWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stackWriter);
                error.printStackTrace(printWriter);
                printWriter.flush();
                stackWriter.flush();
                assertThat(stackWriter.toString().contains((String) attributes.get("exception.stacktrace")))
                    .isEqualTo(true);
                printWriter.close();
            }
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
                feedResponseDiagnostics.getClientSideRequestStatistics().size() > 0)) {

            assertThat(mockTracer).isNotNull();
            TracerUnderTest.SpanRecord currentSpan = mockTracer.getCurrentSpan();
            assertThat(currentSpan).isNotNull();
            assertThat(currentSpan.getContext()).isNotNull();

            CosmosDiagnosticsContext ctx = cosmosDiagnostics.getDiagnosticsContext();

            assertThat(cosmosDiagnostics.getUserAgent()).isEqualTo(ctx.getUserAgent());
            assertThat(ctx.getSystemUsage()).isNotNull();
            assertThat(ctx.getConnectionMode()).isEqualTo(client.getConnectionPolicy().getConnectionMode().toString());

            Collection<TracerUnderTest.EventRecord> events  = mockTracer.getEventsOfAllCollectedSiblingSpans();
            if (ctx.isCompleted() && (ctx.isFailure() || ctx.isThresholdViolated())) {
                if (ctx.isFailure()) {
                    assertThat(events).anyMatch(e -> e.getName().startsWith("Failure - CTX"));
                    assertThat(events).noneMatch(e -> e.getName().startsWith("ThresholdViolation - CTX"));

                } else {
                    assertThat(events).noneMatch(e -> e.getName().startsWith("Failure - CTX"));
                    assertThat(events).anyMatch(e -> e.getName().startsWith("ThresholdViolation - CTX"));
                }
            } else {
                assertThat(events).noneMatch(e -> e.getName().startsWith("ThresholdViolation - CTX"));
                assertThat(events).noneMatch(e -> e.getName().startsWith("Failure - CTX"));
            }
        }
    }

    private void verifyOTelTracerTransport(CosmosDiagnostics lastCosmosDiagnostics,
                                           CosmosException error,
                                           TracerUnderTest mockTracer,
                                           boolean enableRequestLevelTracing) {

        assertThat(mockTracer).isNotNull();
        TracerUnderTest.SpanRecord currentSpan = mockTracer.getCurrentSpan();
        assertThat(currentSpan).isNotNull();
        assertThat(currentSpan.getContext()).isNotNull();
        CosmosDiagnosticsContext ctx = lastCosmosDiagnostics.getDiagnosticsContext();

        assertThat(lastCosmosDiagnostics).isNotNull();
        assertThat(lastCosmosDiagnostics.getDiagnosticsContext()).isNotNull();
        assertThat(lastCosmosDiagnostics.getDiagnosticsContext()).isSameAs(ctx);

        Collection<TracerUnderTest.EventRecord> events  = mockTracer.getEventsOfAllCollectedSiblingSpans();
        if (!enableRequestLevelTracing ||
            // For Gateway we rely on http out-of-the-box tracing
            client.getConnectionPolicy().getConnectionMode() != ConnectionMode.DIRECT) {

            assertThat(events).noneMatch(e -> e.getName().equals("rntbd.request"));
            return;
        } else {
            if (error == null) {
                assertThat(events).anyMatch(e -> e.getName().equals("rntbd.request"));
            }
        }

        for (CosmosDiagnostics cosmosDiagnostics : ctx.getDiagnostics()) {
            ClientSideRequestStatistics clientSideRequestStatistics =
                BridgeInternal.getClientSideRequestStatics(cosmosDiagnostics);

            FeedResponseDiagnostics feedResponseDiagnostics =
                cosmosDiagnosticsAccessor.getFeedResponseDiagnostics(cosmosDiagnostics);
            if (clientSideRequestStatistics != null ||
                (feedResponseDiagnostics != null &&
                    feedResponseDiagnostics.getClientSideRequestStatistics().size() > 0)) {

                for (CosmosDiagnostics d : ctx.getDiagnostics()) {
                    if (d.getClientSideRequestStatistics() != null) {
                        for (ClientSideRequestStatistics s : d.getClientSideRequestStatistics()) {
                            if (s.getResponseStatisticsList() == null) {
                                continue;
                            }
                            assertStoreResponseStatistics(ctx, mockTracer, s.getResponseStatisticsList());
                        }
                    }

                }
            }
        }
    }

    private void assertStoreResponseStatistics(
        CosmosDiagnosticsContext ctx,
        TracerUnderTest mockTracer,
        Collection<ClientSideRequestStatistics.StoreResponseStatistics> storeResponseStatistics) {

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
            if (activityId != null && !activityId.isEmpty()) {
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
            int eventCount = 0;
            for (RequestTimeline.Event event : storeResponseDiagnostics.getRequestTimeline()) {
                Instant eventTime = event.getStartTime();

                if (eventTime != null &&
                    (startTime == null || startTime.isBefore(eventTime))) {
                    startTime = eventTime;
                }

                Duration duration = event.getDuration();
                if (duration == null || duration == Duration.ZERO) {
                    continue;
                }

                attributes.put("rntbd.latency_" + event.getName().toLowerCase(Locale.ROOT), duration.toString());
                if (event.getStartTime() != null) {
                    eventCount++;
                }
            }

            attributes.put("rntbd.request_size_bytes",storeResponseDiagnostics.getRequestPayloadLength());
            attributes.put("rntbd.response_size_bytes",storeResponseDiagnostics.getResponsePayloadLength());

            Collection<CosmosDiagnosticsRequestInfo> requestInfo = ctx.getRequestInfo();
            assertThat(requestInfo).isNotNull();
            final int expectedEventCount = eventCount;
            assertThat(requestInfo).anyMatch(info -> {
                    assertThat(info.getStartTime()).isNotNull();

                    if (activityId != null && !activityId.isEmpty() && !info.getActivityId().equals(activityId)) {
                        logger.info("ActivityId mismatch {} - {}", activityId, info.getActivityId());

                        return false;
                    }

                    if (latency != null && !latency.equals(info.getDuration())) {
                        logger.info("Duration mismatch {} - {}", latency, info.getDuration());

                        return false;
                    }


                    if (backendLatency != null && info.getBackendLatency().minusNanos((long)(backendLatency * 1000000d)).abs().compareTo(Duration.ofMillis(1)) > 1) {
                        logger.info("Backend duration mismatch {} - {}", backendLatency, info.getBackendLatency());

                        return false;
                    }

                    if (pkRangeId != null && !pkRangeId.isEmpty() && !pkRangeId.equals(info.getPartitionKeyRangeId())) {
                        logger.info("PKRangeId mismatch {} - {}", pkRangeId, info.getPartitionKeyRangeId());

                        return false;
                    }

                    if (expectedEventCount != info.getRequestPipelineEvents().size()) {
                        logger.info("Event count mismatch {} - {}", expectedEventCount, info.getRequestPipelineEvents().size());

                        return false;
                    }

                    return true;
                });
            assertEvent(mockTracer, "rntbd.request", startTime, attributes);
        }
    }

    private void verifyLegacyTracerAttributes(TracerUnderTest mockTracer,
                                              String methodName,
                                              String databaseName,
                                              CosmosDiagnostics cosmosDiagnostics,
                                              boolean enableRequestLevelTracing,
                                              boolean forceThresholdViolation,
                                              double samplingRate) throws JsonProcessingException {

        assertThat(mockTracer).isNotNull();

        assertThat(cosmosDiagnostics).isNotNull();
        assertThat(cosmosDiagnostics.getSamplingRateSnapshot()).isEqualTo(samplingRate);
        assertThat(
            cosmosDiagnostics.toString().contains("\"samplingRateSnapshot\":" + String.valueOf(samplingRate)))
            .isEqualTo(true);

        TracerUnderTest.SpanRecord currentSpan = mockTracer.getCurrentSpan();

        if (samplingRate == 0) {
            return;
        }

        assertThat(currentSpan).isNotNull();
        Map<String, Object> attributes = mockTracer.getCurrentSpan().getAttributes();

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

        assertThat(mockTracer).isNotNull();
        assertThat(mockTracer.getCurrentSpan()).isNotNull();
        List<TracerUnderTest.EventRecord> filteredEvents =
            mockTracer.getEventsOfAllCollectedSiblingSpans().stream().filter(e ->
                e.getName().equals(eventName)).collect(Collectors.toList());
        if (filteredEvents.size() == 0) {
            logger.error("Event: {}", eventName);
        }
        assertThat(filteredEvents).hasSizeGreaterThanOrEqualTo(1);
        if (time != null) {
            filteredEvents =
                filteredEvents
                    .stream()
                    .filter(e -> e.getTimestamp() != null &&
                        e.getTimestamp().equals(OffsetDateTime.ofInstant(time, ZoneOffset.UTC)))
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
                    if (e.getAttributes() == null || e.getAttributes().size() < attributes.size()) {
                        return false;
                    }

                    for (String key : attributes.keySet()) {
                        if (!e.getAttributes().containsKey((key))) {
                            return false;
                        }

                        if (!e.getAttributes().get(key).equals(attributes.get(key))) {
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
                    eventIterator =
                        storeResponseStatistics.getStoreResult().getStoreResponseDiagnostics().getRequestTimeline().iterator();
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
        if (feedResponseDiagnostics != null && feedResponseDiagnostics.getClientSideRequestStatistics().size() > 0) {
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
                feedResponseDiagnostics.getClientSideRequestStatistics()) {
                if (clientSideStatistics.getResponseStatisticsList() != null && clientSideStatistics.getResponseStatisticsList().size() > 0
                    && clientSideStatistics.getResponseStatisticsList().iterator().next().getStoreResult() != null) {

                    String pkRangeId = clientSideStatistics
                        .getResponseStatisticsList()
                        .iterator().next()
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
                } else if (clientSideStatistics.getGatewayStatisticsList() != null && clientSideStatistics.getGatewayStatisticsList().size() > 0) {
                    String pkRangeId = clientSideStatistics.getGatewayStatisticsList().get(0).getPartitionKeyRangeId();

                    if (pkRangeId != null) {
                        String eventName = "Diagnostics for PKRange "
                            + clientSideStatistics.getGatewayStatisticsList().get(0).getPartitionKeyRangeId();
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

            assertThat(mockTracer).isNotNull();
            for (Map.Entry<String, QueryMetrics> queryMetrics :
                feedResponseDiagnostics.getQueryMetricsMap().entrySet()) {
                String eventName = "Query Metrics for PKRange " + queryMetrics.getKey();
                assertThat(mockTracer.getCurrentSpan()).isNotNull();
                List<TracerUnderTest.EventRecord> filteredEvents =
                    mockTracer.getEventsOfAllCollectedSiblingSpans().stream().filter(
                        e -> e.getName().equals(eventName)).collect(Collectors.toList());
                assertThat(filteredEvents).hasSizeGreaterThanOrEqualTo(1);
                assertThat(filteredEvents.size()).isGreaterThanOrEqualTo(1);
                assertThat(filteredEvents.get(0).getAttributes().get("Query Metrics"))
                    .isEqualTo(queryMetrics.getValue().toString());
            }
        }
    }

    private DiagnosticsProvider createAndInitializeDiagnosticsProvider(TracerUnderTest mockTracer,
                                                                       boolean useLegacyTracing,
                                                                       boolean enableRequestLevelTracing,
                                                                       ShowQueryMode showQueryMode,
                                                                       boolean forceThresholdViolations,
                                                                       double samplingRate) {
        CosmosDiagnosticsThresholds thresholds = forceThresholdViolations ?
            new CosmosDiagnosticsThresholds()
                .setPointOperationLatencyThreshold(Duration.ZERO)
                .setNonPointOperationLatencyThreshold(Duration.ZERO)
            : new CosmosDiagnosticsThresholds()
                .setPointOperationLatencyThreshold(Duration.ofDays(1))
                .setNonPointOperationLatencyThreshold(Duration.ofDays(1));

        thresholds.setFailureHandler((statusCode, subStatusCode) -> {
            checkNotNull(statusCode, "Argument 'statusCode' must not be null." );
            checkNotNull(subStatusCode, "Argument 'subStatusCode' must not be null." );
            if (statusCode >= 500) {
                return true;
            }

            if (statusCode == 404) {
                return true;
            }

            if (subStatusCode == 0 &&
                (statusCode == HttpConstants.StatusCodes.CONFLICT ||
                    statusCode == HttpConstants.StatusCodes.PRECONDITION_FAILED)) {

                return false;
            }

            if (statusCode == 429 &&
                (subStatusCode == HttpConstants.SubStatusCodes.THROUGHPUT_CONTROL_REQUEST_RATE_TOO_LARGE ||
                    subStatusCode == HttpConstants.SubStatusCodes.USER_REQUEST_RATE_TOO_LARGE)) {
                return false;
            }

            return statusCode >= 400;
        });

        CosmosClientTelemetryConfig clientTelemetryConfig = new CosmosClientTelemetryConfig()
            .diagnosticsThresholds(thresholds);

        if (samplingRate != 1) {
            clientTelemetryConfig.sampleDiagnostics(samplingRate);
        }

        ImplementationBridgeHelpers
            .CosmosClientTelemetryConfigHelper
            .getCosmosClientTelemetryConfigAccessor()
            .setUseLegacyTracing(clientTelemetryConfig, useLegacyTracing);

        if (enableRequestLevelTracing) {
            clientTelemetryConfig.enableTransportLevelTracing();
        }
        
        clientTelemetryConfig.showQueryMode(showQueryMode);

        ImplementationBridgeHelpers
            .CosmosClientTelemetryConfigHelper
            .getCosmosClientTelemetryConfigAccessor()
                .setTracer(clientTelemetryConfig, mockTracer);

        DiagnosticsProvider tracerProvider = new DiagnosticsProvider(
            clientTelemetryConfig,
            client.getClientCorrelationTag().getValue(),
            client.getUserAgent(),
            client.getConnectionPolicy().getConnectionMode());
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
}
