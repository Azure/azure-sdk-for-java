// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.core.util.Context;
import com.azure.cosmos.implementation.ConsoleLoggingRegistryFactory;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosMicrometerMetricsOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micrometer.core.instrument.MeterRegistry;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    @BeforeClass(groups = {"simple", "emulator"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        assertThat(this.client).isNull();
    }

    @AfterClass(groups = {"simple", "emulator"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.client).isNotNull();
        this.client.close();
    }

    public String resolveTestNameSuffix(Object[] row) {
        return "";
    }

    @Test(groups = { "simple", "emulator" }, timeOut = TIMEOUT)
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

    @Test(groups = { "simple", "emulator" }, timeOut = TIMEOUT)
    public void onlyDefaultLogger() {
        CosmosClientBuilder builder = this
            .getClientBuilder()
            .clientTelemetryConfig(
                new CosmosClientTelemetryConfig()
                    .diagnosticLogs()
            );
        CosmosContainer container = this.getContainer(builder);
        executeTestCase(container);

        // no assertions here - invocations for diagnostics handler are validated above
        // log4j event logging isn't validated in general in unit tests because it is too brittle to do so
        // with custom appender
    }

    @Test(groups = { "simple", "emulator" }, timeOut = TIMEOUT)
    public void onlyLoggerWithCustomConfig() {
        CosmosClientBuilder builder = this
            .getClientBuilder()
            .clientTelemetryConfig(
                new CosmosClientTelemetryConfig()
                    .diagnosticsThresholds(
                        new CosmosDiagnosticsThresholds()
                            .configureLatencyThresholds(Duration.ofMillis(100), Duration.ofMillis(2000))
                            .setRequestChargeThreshold(100)
                    )
                    .diagnosticLogs()
            );
        CosmosContainer container = this.getContainer(builder);
        executeTestCase(container);

        // no assertions here - invocations for diagnostics handler are validated above
        // log4j event logging isn't validated in general in unit tests because it is too brittle to do so
        // with custom appender
    }

    @Test(groups = { "simple", "emulator" }, timeOut = TIMEOUT)
    public void onlyCustomLoggerWithCustomConfig() {

        CapturingLogger capturingLogger = new CapturingLogger(new CosmosDiagnosticsLoggerConfig());

        CosmosClientBuilder builder = this
            .getClientBuilder()
            .clientTelemetryConfig(
                new CosmosClientTelemetryConfig()
                    .diagnosticsThresholds(
                        new CosmosDiagnosticsThresholds()
                            .configureLatencyThresholds(Duration.ofMillis(100), Duration.ofMillis(2000))
                            .setRequestChargeThreshold(100)
                    )
                    .diagnosticsHandler(capturingLogger)
            );
        CosmosContainer container = this.getContainer(builder);
        executeTestCase(container);

        assertThat(capturingLogger.getLoggedMessages()).isNotNull();
        assertThat(capturingLogger.getLoggedMessages()).hasSize(1);
    }

    @Test(groups = { "simple", "emulator" }, timeOut = TIMEOUT)
    public void defaultLoggerAndMetrics() {
        MeterRegistry meterRegistry = ConsoleLoggingRegistryFactory.create(1);

        CosmosClientBuilder builder = this
            .getClientBuilder()
            .clientTelemetryConfig(
                new CosmosClientTelemetryConfig()
                    .diagnosticLogs()
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

    @Test(groups = { "simple", "emulator" }, timeOut = TIMEOUT)
    public void defaultLoggerWithLegacyOpenTelemetryTraces() {
        CosmosClientBuilder builder = this
            .getClientBuilder()
            .clientTelemetryConfig(
                new CosmosClientTelemetryConfig()
                    .diagnosticLogs()
                    .useLegacyOpenTelemetryTracing()
            );
        CosmosContainer container = this.getContainer(builder);
        executeTestCase(container);

        // no assertions here - invocations for diagnostics handler are validated above
        // log4j event logging isn't validated in general in unit tests because it is too brittle to do so
        // with custom appender
    }

    private void executeTestCase(CosmosContainer container) {
        String id = UUID.randomUUID().toString();
        CosmosItemResponse<ObjectNode> response = container.createItem(
            getDocumentDefinition(id),
            new PartitionKey(id),
            null);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(201);
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

    private static class CapturingDiagnosticsHandler implements CosmosDiagnosticsHandler {

        private final ArrayList<CosmosDiagnosticsContext> diagnosticsContexts = new ArrayList<>();

        @Override
        public void handleDiagnostics(Context traceContext, CosmosDiagnosticsContext diagnosticsContext) {
            diagnosticsContexts.add(diagnosticsContext);
        }

        public List<CosmosDiagnosticsContext> getDiagnosticsContexts() {
            return this.diagnosticsContexts;
        }
    }

    private static class CapturingLogger extends CosmosDiagnosticsLogger {
        private final List<String> loggedMessages = new ArrayList<>();
        public CapturingLogger(CosmosDiagnosticsLoggerConfig config) {
            super(config);
        }

        @Override
        protected boolean shouldLog(CosmosDiagnosticsContext diagnosticsContext) {
            logger.info("--> should Log: ctx: {}", diagnosticsContext);
            return true;
        }

        @Override
        protected void log(CosmosDiagnosticsContext ctx) {
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

            logger.info(msg);
        }

        public List<String> getLoggedMessages() {
            return this.loggedMessages;
        }
    }
}
