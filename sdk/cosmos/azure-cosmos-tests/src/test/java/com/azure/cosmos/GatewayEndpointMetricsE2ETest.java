// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.clienttelemetry.TagName;
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosMetricCategory;
import com.azure.cosmos.models.CosmosMetricName;
import com.azure.cosmos.models.CosmosMetricTagName;
import com.azure.cosmos.models.CosmosMicrometerMetricsOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class GatewayEndpointMetricsE2ETest extends TestSuiteBase {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String CONTAINER_ID = "endpointMetrics";
    private CosmosAsyncClient managementClient;
    private CosmosAsyncDatabase database;

    public GatewayEndpointMetricsE2ETest() {
        super(createGatewayRxDocumentClient());
    }

    @BeforeClass(groups = {"fast", "thinclient", "thinclientEndpointProbe"}, timeOut = SETUP_TIMEOUT)
    public void setUp() {
        managementClient = gatewayBuilder(false)
            .clientTelemetryConfig(new CosmosClientTelemetryConfig()
                .metricsOptions(new CosmosMicrometerMetricsOptions().setEnabled(false)))
            .buildAsyncClient();
        database = createTestDatabase(managementClient, "gatewayEndpointMetrics");
        createCollection(managementClient, database.getId(), new CosmosContainerProperties(CONTAINER_ID, "/mypk"));
    }

    @AfterClass(groups = {"fast", "thinclient", "thinclientEndpointProbe"},
        timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void cleanUp() {
        try {
            if (database != null) {
                safeDeleteDatabase(database);
            }
        } finally {
            if (managementClient != null) {
                managementClient.close();
            }
        }
    }

    @Test(groups = {"fast"}, timeOut = SETUP_TIMEOUT, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void computeGatewayRequestMetricsUseRecordedServiceEndpoint() {
        validateGatewayRequestMetrics(false);
    }

    @Test(groups = {"thinclient", "thinclientEndpointProbe"}, timeOut = SETUP_TIMEOUT,
        retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void gatewayV2RequestMetricsUseRecordedServiceEndpoint() {
        validateGatewayRequestMetrics(true);
    }

    private CosmosClientBuilder gatewayBuilder(boolean http2Enabled) {
        GatewayConnectionConfig connectionConfig = new GatewayConnectionConfig();
        connectionConfig.getHttp2ConnectionConfig().setEnabled(http2Enabled);
        return createGatewayRxDocumentClient().gatewayMode(connectionConfig);
    }

    private void validateGatewayRequestMetrics(boolean gatewayV2) {
        for (boolean serviceEndpointEnabled : new boolean[] {false, true}) {
            SimpleMeterRegistry registry = new SimpleMeterRegistry();
            try {
                CosmosMicrometerMetricsOptions metricsOptions = new CosmosMicrometerMetricsOptions()
                    .meterRegistry(registry)
                    .setEnabled(true)
                    .setMetricCategories(CosmosMetricCategory.DEFAULT)
                    .configureDefaultTagNames(CosmosMetricTagName.DEFAULT)
                    .applyDiagnosticThresholdsForTransportLevelMeters(false);
                if (serviceEndpointEnabled) {
                    metricsOptions.configureDefaultTagNames(
                        CosmosMetricTagName.DEFAULT, CosmosMetricTagName.SERVICE_ENDPOINT);
                }

                try (CosmosClient client = gatewayBuilder(gatewayV2)
                    .clientTelemetryConfig(new CosmosClientTelemetryConfig().metricsOptions(metricsOptions))
                    .buildClient()) {
                    assertThat(client.asyncClient().getConnectionPolicy().getConnectionMode())
                        .isEqualTo(ConnectionMode.GATEWAY);
                    if (gatewayV2) {
                        assertThat(Configs.isThinClientEnabled())
                            .as("GatewayV2 must not be explicitly disabled by the test environment")
                            .isNotEqualTo(Boolean.FALSE);
                        if (Configs.isThinClientEnabled() == null) {
                            GlobalEndpointManager endpointManager = BridgeInternal.getContextClient(client.asyncClient())
                                .getGlobalEndpointManager();
                            Mono.delay(Duration.ZERO)
                                .repeatWhen(signals -> signals.delayElements(Duration.ofMillis(100)))
                                .filter(ignored -> Boolean.TRUE.equals(endpointManager.getProxyProbeDecision()))
                                .next()
                                .block(Duration.ofSeconds(30));
                        }
                    }
                    CosmosContainer container = client.getDatabase(database.getId()).getContainer(CONTAINER_ID);
                    Tag correlationTag = client.asyncClient().getClientCorrelationTag();

                    for (int i = 0; i < 2; i++) {
                        String id = UUID.randomUUID().toString();
                        ObjectNode document = OBJECT_MAPPER.createObjectNode()
                            .put("id", id).put("mypk", id).put("value", i);
                        PartitionKey partitionKey = new PartitionKey(id);
                        CosmosItemResponse<ObjectNode> created = container.createItem(document);
                        assertThat(created.getStatusCode()).isEqualTo(201);
                        assertMetrics(registry, correlationTag, created.getDiagnostics(), 201,
                            serviceEndpointEnabled, gatewayV2);

                        CosmosItemResponse<ObjectNode> read = container.readItem(id, partitionKey, ObjectNode.class);
                        assertThat(read.getStatusCode()).isEqualTo(200);
                        assertThat(read.getItem().get("id").asText()).isEqualTo(id);
                        assertThat(read.getItem().get("mypk").asText()).isEqualTo(id);
                        assertThat(read.getItem().get("value").asInt()).isEqualTo(i);
                        assertMetrics(registry, correlationTag, read.getDiagnostics(), 200,
                            serviceEndpointEnabled, gatewayV2);

                        CosmosItemResponse<Object> deleted = container.deleteItem(
                            id, partitionKey, new CosmosItemRequestOptions());
                        assertThat(deleted.getStatusCode()).isEqualTo(204);
                        assertMetrics(registry, correlationTag, deleted.getDiagnostics(), 204,
                            serviceEndpointEnabled, gatewayV2);

                        try {
                            container.readItem(id, partitionKey, ObjectNode.class);
                            fail("Reading the deleted document must fail with 404.");
                        } catch (CosmosException exception) {
                            assertThat(exception.getStatusCode()).isEqualTo(404);
                            assertMetrics(registry, correlationTag, exception.getDiagnostics(), 404,
                                serviceEndpointEnabled, gatewayV2);
                        }
                    }
                }
            } finally {
                registry.close();
            }
        }
    }

    private void assertMetrics(
        MeterRegistry registry, Tag correlationTag, CosmosDiagnostics diagnostics, int statusCode,
        boolean serviceEndpointEnabled, boolean gatewayV2) {

        assertThat(diagnostics).isNotNull();
        List<ClientSideRequestStatistics.GatewayStatistics> gatewayStatistics = diagnostics
            .getClientSideRequestStatistics().stream()
            .flatMap(statistics -> statistics.getGatewayStatisticsList().stream())
            .filter(statistics -> statistics.getResourceType() == ResourceType.Document)
            .collect(Collectors.toList());
        assertThat(gatewayStatistics).isNotEmpty();
        assertThat(gatewayStatistics).anySatisfy(statistics ->
            assertThat(statistics.getStatusCode()).isEqualTo(statusCode));

        for (ClientSideRequestStatistics.GatewayStatistics statistics : gatewayStatistics) {
            assertThat(statistics.getEndpoint()).isNotBlank();
            URI endpoint = URI.create(statistics.getEndpoint());
            assertThat(statistics.getRequestUri()).isEqualTo(endpoint);
            assertThat(endpoint.getScheme()).isEqualToIgnoringCase("https");
            assertThat(endpoint.getHost()).isNotBlank();
            int expectedPort = gatewayV2 ? 10250 : 443;
            assertThat(endpoint.getPort() == -1 ? 443 : endpoint.getPort())
                .as("Document request must use %s, endpoint: %s",
                    gatewayV2 ? "GatewayV2" : "Compute Gateway", endpoint)
                .isEqualTo(expectedPort);
            assertThat(statistics.isHttp2()).as("Negotiated HTTP/2 for endpoint %s", endpoint)
                .isEqualTo(gatewayV2);
            String expectedEndpoint = endpoint.getHost() + "_" + expectedPort;
            if (serviceEndpointEnabled) {
                assertThat(statistics.getServiceEndpointMetricTag()).isNotNull();
                assertThat(statistics.getServiceEndpointMetricTag().getValue()).isEqualTo(expectedEndpoint);
            } else {
                assertThat(statistics.getServiceEndpointMetricTag()).isNull();
            }
            Tags requestTags = Tags.of(
                correlationTag,
                Tag.of(TagName.RequestOperationType.toString(),
                    statistics.getResourceType() + "/" + statistics.getOperationType()),
                Tag.of(TagName.RequestStatusCode.toString(),
                    statistics.getStatusCode() + "/" + statistics.getSubStatusCode()));
            if (serviceEndpointEnabled) {
                requestTags = requestTags.and(TagName.ServiceEndpoint.toString(), expectedEndpoint);
            }

            Counter requests = registry.get(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUESTS.toString())
                .tags(requestTags).counter();
            Timer latency = registry.get(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_LATENCY.toString())
                .tags(requestTags).timer();
            DistributionSummary requestCharge = registry
                .get(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUEST_CHARGE.toString())
                .tags(requestTags).summary();
            assertThat(requests.count()).isGreaterThan(0);
            assertThat(latency.count()).isGreaterThan(0);
            assertThat(requestCharge.count()).isGreaterThan(0);
            if (statistics.getStatusCode() >= 200 && statistics.getStatusCode() < 300) {
                assertThat(requestCharge.totalAmount()).isGreaterThan(0);
            } else {
                assertThat(requestCharge.totalAmount()).isGreaterThanOrEqualTo(0);
            }

            for (Meter meter : Arrays.asList(requests, latency, requestCharge)) {
                assertThat(meter.getId().getTag(TagName.ServiceEndpoint.toString()))
                    .isEqualTo(serviceEndpointEnabled ? expectedEndpoint : null);
            }
        }
    }
}
