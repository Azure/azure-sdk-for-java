// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.core.util.Context;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.ConsoleLoggingRegistryFactory;
import com.azure.cosmos.implementation.DiagnosticsProviderJvmFatalErrorMapper;
import com.azure.cosmos.implementation.Exceptions;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.TestUtils;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosMicrometerMetricsOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micrometer.core.instrument.MeterRegistry;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class CosmosDiagnosticsE2ETest extends TestSuiteBase {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().configure(
        JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(),
        true
    );

    private CosmosClient client;

    @Factory(dataProvider = "clientBuildersWithDirectSession")
    public CosmosDiagnosticsE2ETest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"fast", "emulator"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        assertThat(this.client).isNull();
    }

    @AfterClass(groups = {"fast", "emulator"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        CosmosClient clientSnapshot = this.client;
        if (clientSnapshot != null) {
            clientSnapshot.close();
        }
    }

    @DataProvider
    public static Object[][] operationTypeProvider() {
        return new Object[][]{
            { OperationType.Read },
            { OperationType.Replace },
            { OperationType.Create },
            { OperationType.Delete },
            { OperationType.Query }
        };
    }

    public String resolveTestNameSuffix(Object[] row) {
        return "";
    }

    @Test(groups = { "fast", "emulator" }, timeOut = TIMEOUT)
    public void onlyCustomDiagnosticsHandler() {

        CapturingDiagnosticsHandler capturingHandler = new CapturingDiagnosticsHandler();

        CosmosClientBuilder builder = this
            .getClientBuilder()
            .clientTelemetryConfig(
                new CosmosClientTelemetryConfig()
                    .diagnosticsHandler(capturingHandler)
            );
        CosmosContainer container = this.getContainer(builder);

        executeTestCase(container);

        assertThat(capturingHandler.getDiagnosticsContexts()).hasSize(1);
        CosmosDiagnosticsContext ctx = capturingHandler.getDiagnosticsContexts().get(0);
        assertThat(ctx).isNotNull();
        assertThat(ctx.isCompleted()).isEqualTo(true);
        assertThat(ctx.getDiagnostics()).isNotEmpty();
        assertThat(ctx.getContainerName()).isEqualTo(container.getId());
        assertThat(ctx.getDatabaseName()).isEqualTo(container.asyncContainer.getDatabase().getId());
        assertThat(ctx.getDuration()).isNotNull();
        assertThat(ctx.getDuration()).isGreaterThan(Duration.ZERO);
        assertThat(ctx.getFinalError()).isNull();
        assertThat(ctx.getMaxItemCount()).isNull();
        if (this.getClientBuilder().isContentResponseOnWriteEnabled()) {
            assertThat(ctx.getMaxResponsePayloadSizeInBytes()).isGreaterThan(0);
        } else {
            assertThat(ctx.getMaxResponsePayloadSizeInBytes()).isEqualTo(0);
        }
        assertThat(ctx.getOperationType()).isEqualTo(OperationType.Create.toString());
        assertThat(ctx.getOperationTypeInternal()).isEqualTo(OperationType.Create);
        assertThat(ctx.getResourceType()).isEqualTo(ResourceType.Document.toString());
        assertThat(ctx.getResourceTypeInternal()).isEqualTo(ResourceType.Document);
    }

    @Test(groups = { "fast", "emulator" }, timeOut = TIMEOUT)
    public void onlyDefaultLogger() {
        CosmosClientBuilder builder = this
            .getClientBuilder()
            .clientTelemetryConfig(
                new CosmosClientTelemetryConfig()
                    .diagnosticsHandler(CosmosDiagnosticsHandler.DEFAULT_LOGGING_HANDLER)
            );
        CosmosContainer container = this.getContainer(builder);
        executeTestCase(container);


        // no assertions here - invocations for diagnostics handler are validated above
        // log4j event logging isn't validated in general in unit tests because it is too brittle to do so
        // with custom appender
    }

    @Test(groups = { "fast", "emulator" }, timeOut = TIMEOUT)
    public void onlyLoggerWithCustomConfig() {
        CosmosClientBuilder builder = this
            .getClientBuilder()
            .clientTelemetryConfig(
                new CosmosClientTelemetryConfig()
                    .diagnosticsThresholds(
                        new CosmosDiagnosticsThresholds()
                            .setPointOperationLatencyThreshold(Duration.ofMillis(100))
                            .setNonPointOperationLatencyThreshold(Duration.ofMillis(2000))
                            .setRequestChargeThreshold(100)
                    )
                    .diagnosticsHandler(CosmosDiagnosticsHandler.DEFAULT_LOGGING_HANDLER)
            );
        CosmosContainer container = this.getContainer(builder);
        CosmosItemResponse<ObjectNode> response = executeTestCase(container);
        assertThat(response.getDiagnostics()).isNotNull();
        assertThat(response.getDiagnostics().getDiagnosticsContext()).isNotNull();
        // no assertions here - invocations for diagnostics handler are validated above
        // log4j event logging isn't validated in general in unit tests because it is too brittle to do so
        // with custom appender
    }

    @Test(groups = { "fast", "emulator" }, timeOut = TIMEOUT)
    public void onlyLoggerAlwaysSampledOut() {
        CosmosClientBuilder builder = this
            .getClientBuilder()
            .clientTelemetryConfig(
                new CosmosClientTelemetryConfig()
                    .diagnosticsThresholds(
                        new CosmosDiagnosticsThresholds()
                            .setPointOperationLatencyThreshold(Duration.ofMillis(100))
                            .setNonPointOperationLatencyThreshold(Duration.ofMillis(2000))
                            .setRequestChargeThreshold(100)
                    )
                    .diagnosticsHandler(CosmosDiagnosticsHandler.DEFAULT_LOGGING_HANDLER)
                    .sampleDiagnostics(0)
            );
        CosmosContainer container = this.getContainer(builder);
        CosmosItemResponse<ObjectNode> response = executeTestCase(container);
        assertThat(response.getDiagnostics()).isNotNull();
        assertThat(response.getDiagnostics().getDiagnosticsContext()).isNotNull();
        // no assertions here - invocations for diagnostics handler are validated above
        // log4j event logging isn't validated in general in unit tests because it is too brittle to do so
        // with custom appender
    }

    @Test(groups = { "fast", "emulator" }, timeOut = TIMEOUT)
    public void onlyCustomLoggerWithCustomConfig() {

        CapturingLogger capturingLogger = new CapturingLogger();

        CosmosClientBuilder builder = this
            .getClientBuilder()
            .clientTelemetryConfig(
                new CosmosClientTelemetryConfig()
                    .diagnosticsThresholds(
                        new CosmosDiagnosticsThresholds()
                            .setPointOperationLatencyThreshold(Duration.ofMillis(100))
                            .setNonPointOperationLatencyThreshold(Duration.ofMillis(2000))
                            .setRequestChargeThreshold(100)
                    )
                    .diagnosticsHandler(capturingLogger)
            );
        CosmosContainer container = this.getContainer(builder);
        String id = UUID.randomUUID().toString();
        ObjectNode doc = getDocumentDefinition(id);

        CosmosDiagnostics diagnostics = executeDocumentOperation(container, OperationType.Create, id, doc);
        assertThat(diagnostics).isNotNull();
        assertThat(diagnostics.getDiagnosticsContext()).isNotNull();

        diagnostics = executeDocumentOperation(container, OperationType.Query, id, doc);
        assertThat(diagnostics).isNotNull();
        assertThat(diagnostics.getDiagnosticsContext()).isNotNull();

        assertThat(capturingLogger.getLoggedMessages()).isNotNull();
        assertThat(capturingLogger.getLoggedMessages()).hasSize(2);
    }

    @Test(groups = { "fast", "emulator" }, timeOut = TIMEOUT)
    public void onlyCustomLoggerAlwaysSampledOut() {

        CapturingLogger capturingLogger = new CapturingLogger();

        CosmosClientBuilder builder = this
            .getClientBuilder()
            .clientTelemetryConfig(
                new CosmosClientTelemetryConfig()
                    .diagnosticsThresholds(
                        new CosmosDiagnosticsThresholds()
                            .setPointOperationLatencyThreshold(Duration.ofMillis(100))
                            .setNonPointOperationLatencyThreshold(Duration.ofMillis(2000))
                            .setRequestChargeThreshold(100)
                    )
                    .diagnosticsHandler(capturingLogger)
                    .sampleDiagnostics(0)
            );
        CosmosContainer container = this.getContainer(builder);
        CosmosItemResponse<ObjectNode> response = executeTestCase(container);

        assertThat(response.getDiagnostics()).isNotNull();
        assertThat(response.getDiagnostics().getDiagnosticsContext()).isNotNull();
        assertThat(capturingLogger.getLoggedMessages()).isNotNull();
        assertThat(capturingLogger.getLoggedMessages()).hasSize(0);
    }

    @Test(groups = { "fast", "emulator" }, timeOut = TIMEOUT)
    public void defaultLoggerAndMetrics() {
        MeterRegistry meterRegistry = ConsoleLoggingRegistryFactory.create(1);

        CosmosClientBuilder builder = this
            .getClientBuilder()
            .clientTelemetryConfig(
                new CosmosClientTelemetryConfig()
                    .diagnosticsHandler(CosmosDiagnosticsHandler.DEFAULT_LOGGING_HANDLER)
                    .metricsOptions(new CosmosMicrometerMetricsOptions().meterRegistry(meterRegistry))
            );
        CosmosContainer container = this.getContainer(builder);
        executeTestCase(container);

        meterRegistry.clear();
        meterRegistry.close();

        // no assertions here - invocations for diagnostics handler are validated above
        // log4j event logging isn't validated in general in unit tests because it is too brittle to do so
        // with custom appender
    }

    @Test(groups = { "fast", "emulator" }, dataProvider = "operationTypeProvider", timeOut = TIMEOUT)
    public void delayedSampling(OperationType operationType) {
        MeterRegistry meterRegistry = ConsoleLoggingRegistryFactory.create(1);

        CapturingLogger capturingLogger = new CapturingLogger();
        CosmosClientTelemetryConfig clientTelemetryCfg = new CosmosClientTelemetryConfig()
            .diagnosticsHandler(capturingLogger)
            .metricsOptions(new CosmosMicrometerMetricsOptions().meterRegistry(meterRegistry));

        CosmosClientBuilder builder = this
            .getClientBuilder()
            .clientTelemetryConfig(clientTelemetryCfg);

        CosmosContainer container = this.getContainer(builder);
        executeTestCase(container);

        meterRegistry.clear();
        meterRegistry.close();

        String id = UUID.randomUUID().toString();
        ObjectNode newItem = getDocumentDefinition(id);
        container.createItem(newItem);

        // change sample rate to 25%
        clientTelemetryCfg.sampleDiagnostics(0.25);
        executeDocumentOperation(container, operationType, id, newItem);

        int loggedMessageSizeBefore = capturingLogger.getLoggedMessages().size();
        // reduce sample rate to 0 - disable all diagnostics
        clientTelemetryCfg.sampleDiagnostics(0);
        executeDocumentOperation(container, operationType, id, newItem);
        int loggedMessageSizeAfter = capturingLogger.getLoggedMessages().size();
        // verify when sample rate is 0, the diagnostics will not be logged
        assertThat(loggedMessageSizeBefore).isEqualTo(loggedMessageSizeAfter);

        // set sample rate to 1 - enable all diagnostics (no sampling anymore)
        clientTelemetryCfg.sampleDiagnostics(1);
        executeDocumentOperation(container, operationType, id, newItem);
        loggedMessageSizeAfter = capturingLogger.getLoggedMessages().size();
        // Verify when sample rate is 1, the diagnostics will be logged
        assertThat(loggedMessageSizeBefore + 1).isEqualTo(loggedMessageSizeAfter);

        // no assertions here - invocations for diagnostics handler are validated above
        // log4j event logging isn't validated in general in unit tests because it is too brittle to do so
        // with custom appender
    }

    @Test(groups = { "fast", "emulator" }, timeOut = TIMEOUT)
    public void defaultLoggerWithLegacyOpenTelemetryTraces() {
        System.setProperty("COSMOS.USE_LEGACY_TRACING", "true");
        CosmosClientBuilder builder = this
            .getClientBuilder()
            .clientTelemetryConfig(
                new CosmosClientTelemetryConfig()
                    .diagnosticsHandler(CosmosDiagnosticsHandler.DEFAULT_LOGGING_HANDLER)
            );
        CosmosContainer container = this.getContainer(builder);
        executeTestCase(container);

        // no assertions here - invocations for diagnostics handler are validated above
        // log4j event logging isn't validated in general in unit tests because it is too brittle to do so
        // with custom appender
        System.setProperty("COSMOS.USE_LEGACY_TRACING", "false");
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void nullPointerDiagnosticsHandler() {
        NullPointerDiagnosticsHandle capturingHandler = new NullPointerDiagnosticsHandle();

        CosmosClientBuilder builder = this
            .getClientBuilder()
            .clientTelemetryConfig(new CosmosClientTelemetryConfig().diagnosticsHandler(capturingHandler));

        CosmosContainer container = this.getContainer(builder);

        AtomicBoolean systemExited = new AtomicBoolean(false);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                systemExited.set(true);
            }
        });

        executeTestCase(container);
        assertThat(systemExited.get()).isFalse();
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void OOMDiagnosticsHandler() throws InterruptedException {
        // validate when false, System.exit will not be called
        System.setProperty("COSMOS.DIAGNOSTICS_PROVIDER_SYSTEM_EXIT_ON_ERROR", "false");

        try {
            OOMDiagnosticsHandle capturingHandler = new OOMDiagnosticsHandle();

            CosmosClientBuilder builder = this
                .getClientBuilder()
                .clientTelemetryConfig(new CosmosClientTelemetryConfig().diagnosticsHandler(capturingHandler));

            CosmosContainer container = this.getContainer(builder);

            AtomicBoolean systemExited = new AtomicBoolean(false);
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    systemExited.set(true);
                }
            });

            // with OOM exception, if no system.exit is called, the reactor thread will hang
            // so put it in different thead
            Mono.just(this)
                .doOnNext(t -> executeTestCase(container))
                .subscribeOn(Schedulers.parallel())
                .subscribe();

            Thread.sleep(2000);
            assertThat(systemExited.get()).isFalse();
        } finally {
            System.clearProperty("COSMOS.DIAGNOSTICS_PROVIDER_SYSTEM_EXIT_ON_ERROR");
        }
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void OOMDiagnosticsHandlerWithErrorMapper() throws JsonProcessingException {
        DiagnosticsProviderJvmFatalErrorMapper
            .getMapper()
            .registerFatalErrorMapper((error) -> new NullPointerException("test"));

        // validate when false, System.exit will not be called
        OOMDiagnosticsHandle capturingHandler = new OOMDiagnosticsHandle(ResourceType.Document, OperationType.Create);

        CosmosClientBuilder builder = this
            .getClientBuilder()
            .clientTelemetryConfig(new CosmosClientTelemetryConfig().diagnosticsHandler(capturingHandler));

        CosmosContainer container = this.getContainer(builder);

        AtomicBoolean systemExited = new AtomicBoolean(false);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                systemExited.set(true);
            }
        });

        try {
            executeTestCase(container);
            fail("should fail with RuntimeException");

        } catch (RuntimeException e) {
            assertThat(e.getCause() instanceof NullPointerException).isTrue();
            assertThat(systemExited.get()).isFalse();
        }

        // doing an upsert and verify in the diagnostics contain mapper execution count
        CosmosDiagnostics cosmosDiagnostics =
            container.upsertItem(getDocumentDefinition(UUID.randomUUID().toString())).getDiagnostics();
        ObjectNode cosmosDiagnosticsNode = (ObjectNode) Utils.getSimpleObjectMapper().readTree(cosmosDiagnostics.toString());
        assertThat(cosmosDiagnosticsNode.get("jvmFatalErrorMapperExecutionCount")).isNotNull();
        assertThat(cosmosDiagnosticsNode.get("jvmFatalErrorMapperExecutionCount").asLong()).isGreaterThan(0);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void validateFeedOperationStateNullCheck() {
        CosmosAsyncClient client = this
            .getClientBuilder()
            .buildAsyncClient();
        TestUtils.createDummyQueryFeedOperationStateWithoutPagedFluxOptions(
            ResourceType.Document,
            OperationType.Query,
            new CosmosQueryRequestOptions(),
            client
        );
    }

    private CosmosItemResponse<ObjectNode> executeTestCase(CosmosContainer container) {
        String id = UUID.randomUUID().toString();
        CosmosItemResponse<ObjectNode> response = container.createItem(
            getDocumentDefinition(id),
            new PartitionKey(id),
            new CosmosItemRequestOptions());
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(201);

        return response;
    }

    private ObjectNode getDocumentDefinition(String documentId) {
        String json = String.format(
            "{ \"id\": \"%s\", \"mypk\": \"%s\" }",
            documentId,
            documentId);

        try {
            return
                OBJECT_MAPPER.readValue(json, ObjectNode.class);
        } catch (JsonProcessingException jsonError) {
            fail("No json processing error expected", jsonError);

            throw new IllegalStateException("No json processing error expected", jsonError);
        }
    }

    private CosmosContainer getContainer(CosmosClientBuilder builder) {

        CosmosClient oldClient = this.client;
        if (oldClient != null) {
            oldClient.close();
        }

        assertThat(builder).isNotNull();
        this.client = builder.buildClient();
        CosmosAsyncContainer asyncContainer = getSharedMultiPartitionCosmosContainer(this.client.asyncClient());
        return this.client.getDatabase(asyncContainer.getDatabase().getId()).getContainer(asyncContainer.getId());
    }

    private CosmosDiagnostics executeDocumentOperation(
        CosmosContainer cosmosContainer,
        OperationType operationType,
        String createdItemId,
        ObjectNode createdItem) {

        final AtomicReference<CosmosDiagnostics> diagnostics = new AtomicReference<>(null);

        switch (operationType) {
            case Query:
                String query = String.format("SELECT * from c");
                CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions();
                queryRequestOptions.setFeedRange(FeedRange.forLogicalPartition(new PartitionKey(createdItemId)));
                Iterable<FeedResponse<JsonNode>> queryResults = cosmosContainer.queryItems(query, queryRequestOptions, JsonNode.class).iterableByPage();
                queryResults.forEach(t -> diagnostics.set(t.getCosmosDiagnostics()));
                break;
            case ReadFeed:
                CosmosChangeFeedRequestOptions changeFeedRequestOptions = CosmosChangeFeedRequestOptions
                    .createForProcessingFromBeginning(FeedRange.forFullRange());
                Iterable<FeedResponse<JsonNode>> changeFeedResults =
                    cosmosContainer.queryChangeFeed(changeFeedRequestOptions, JsonNode.class).iterableByPage();
                changeFeedResults.forEach(t -> diagnostics.set(t.getCosmosDiagnostics()));
                break;
            case Read:
                CosmosItemResponse<JsonNode> readResponse = cosmosContainer
                    .readItem(createdItemId, new PartitionKey(createdItemId), JsonNode.class);
                diagnostics.set(readResponse.getDiagnostics());
                break;
            case Replace:
                CosmosItemResponse<JsonNode> replaceResponse = cosmosContainer
                    .replaceItem(createdItem, createdItemId, new PartitionKey(createdItemId), new CosmosItemRequestOptions());
                diagnostics.set(replaceResponse.getDiagnostics());
                break;
            case Delete:
                try {
                    CosmosItemResponse<Object> deleteResponse = cosmosContainer.deleteItem(getDocumentDefinition(UUID.randomUUID().toString()), new CosmosItemRequestOptions());
                    diagnostics.set(deleteResponse.getDiagnostics());
                } catch (CosmosException e) {
                    if (!Exceptions.isNotFound(e)) {
                        throw e;
                    }
                }
                break;
            case Create:
                CosmosItemResponse<JsonNode> createResponse = cosmosContainer.createItem(getDocumentDefinition(UUID.randomUUID().toString()));
                diagnostics.set(createResponse.getDiagnostics());
                break;
            default:
                throw new IllegalArgumentException("The operation type is not supported");
        }

        return diagnostics.get();
    }

    private static class CapturingDiagnosticsHandler implements CosmosDiagnosticsHandler {

        private final ArrayList<CosmosDiagnosticsContext> diagnosticsContexts = new ArrayList<>();

        @Override
        public void handleDiagnostics(CosmosDiagnosticsContext diagnosticsContext, Context traceContext) {
            diagnosticsContexts.add(diagnosticsContext);
        }

        public List<CosmosDiagnosticsContext> getDiagnosticsContexts() {
            return this.diagnosticsContexts;
        }
    }

    private static class CapturingLogger implements CosmosDiagnosticsHandler {
        private final List<String> loggedMessages = new ArrayList<>();
        public CapturingLogger() {
            super();
        }

        @Override
        public void handleDiagnostics(CosmosDiagnosticsContext ctx, Context traceContext) {
            logger.info("--> log - ctx: {} - json: {}", ctx, ctx != null ? ctx.toJson() : "n/a");
            String msg = String.format(
                "Account: %s -> DB: %s, Col:%s, StatusCode: %d:%d Diagnostics: %s",
                ctx.getAccountName(),
                ctx.getDatabaseName(),
                ctx.getContainerName(),
                ctx.getStatusCode(),
                ctx.getSubStatusCode(),
                ctx);

            this.loggedMessages.add(msg);

            logger.info(msg);
        }

        public List<String> getLoggedMessages() {
            return this.loggedMessages;
        }
    }

    private static class ConsoleOutLogger implements CosmosDiagnosticsHandler {
        private final List<String> loggedMessages = new ArrayList<>();
        public ConsoleOutLogger() {
            super();
        }

        @Override
        public void handleDiagnostics(CosmosDiagnosticsContext ctx, Context traceContext) {
            logger.info("--> log - ctx: {}", ctx);
            String msg = String.format(
                "Account: %s -> DB: %s, Col:%s, StatusCode: %d:%d Diagnostics: %s",
                ctx.getAccountName(),
                ctx.getDatabaseName(),
                ctx.getContainerName(),
                ctx.getStatusCode(),
                ctx.getSubStatusCode(),
                ctx);

            this.loggedMessages.add(msg);

            System.out.println(msg);
        }

        public List<String> getLoggedMessages() {
            return this.loggedMessages;
        }
    }

    private static class NullPointerDiagnosticsHandle implements CosmosDiagnosticsHandler {

        @Override
        public void handleDiagnostics(CosmosDiagnosticsContext diagnosticsContext, Context traceContext) {
            throw new NullPointerException("NullPointerDiagnosticsHandle");
        }
    }

    private static class OOMDiagnosticsHandle implements CosmosDiagnosticsHandler {
        private ResourceType resourceType;
        private OperationType operationType;
        private final boolean oomLimitByResourceAndOperationType;

        public OOMDiagnosticsHandle(ResourceType resourceType, OperationType operationType) {
            this.resourceType = resourceType;
            this.operationType = operationType;
            this.oomLimitByResourceAndOperationType = true;
        }

        public OOMDiagnosticsHandle() {
            this.oomLimitByResourceAndOperationType = false;
        }

        @Override
        public void handleDiagnostics(CosmosDiagnosticsContext diagnosticsContext, Context traceContext) {
            if (this.oomLimitByResourceAndOperationType) {
                if (diagnosticsContext.getOperationType() == this.operationType.toString()
                    && diagnosticsContext.getResourceType() == this.resourceType.toString()) {
                    throw new OutOfMemoryError("OOMDiagnosticsHandle");
                }
            } else {
                throw new OutOfMemoryError("OOMDiagnosticsHandle");
            }
        }
    }
}
