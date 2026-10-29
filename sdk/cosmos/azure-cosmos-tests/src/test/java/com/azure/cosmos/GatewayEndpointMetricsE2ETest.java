// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.OperationType;
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
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.test.faultinjection.CosmosFaultInjectionHelper;
import com.azure.cosmos.test.faultinjection.FaultInjectionConditionBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionConnectionType;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.test.faultinjection.FaultInjectionResultBuilders;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionRuleBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class GatewayEndpointMetricsE2ETest extends TestSuiteBase {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String CONTAINER_ID = "endpointMetrics";
    private CosmosAsyncClient resourceSetupClient;
    private CosmosAsyncDatabase database;

    public GatewayEndpointMetricsE2ETest() {
        super(createGatewayRxDocumentClient());
    }

    @BeforeClass(groups = {"fast", "thinclient", "thinclientEndpointProbe"}, timeOut = SETUP_TIMEOUT)
    public void setUp() {
        resourceSetupClient = gatewayBuilder(false)
            .clientTelemetryConfig(new CosmosClientTelemetryConfig()
                .metricsOptions(new CosmosMicrometerMetricsOptions().setEnabled(false)))
            .buildAsyncClient();
        database = createTestDatabase(resourceSetupClient, "gatewayEndpointMetrics");
        createCollection(resourceSetupClient, database.getId(), new CosmosContainerProperties(CONTAINER_ID, "/mypk"));
    }

    @AfterClass(groups = {"fast", "thinclient", "thinclientEndpointProbe"},
        timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void cleanUp() {
        try {
            if (database != null) {
                safeDeleteDatabase(database);
            }
        } finally {
            if (resourceSetupClient != null) {
                resourceSetupClient.close();
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

    @Test(groups = {"fast"}, timeOut = SETUP_TIMEOUT)
    public void computeGatewayRetryIsCapturedInEndpointMetrics() {
        validateRetryMetrics(false);
    }

    @Test(groups = {"thinclient", "thinclientEndpointProbe"}, timeOut = SETUP_TIMEOUT)
    public void gatewayV2RetryIsCapturedInEndpointMetrics() {
        validateRetryMetrics(true);
    }

    @Test(groups = {"fast"}, timeOut = SETUP_TIMEOUT)
    public void queryPlanAndDocumentMetricsHaveConsistentEndpointLabels() {
        validateQueryPlanMetrics(false);
    }

    @Test(groups = {"thinclient", "thinclientEndpointProbe"}, timeOut = SETUP_TIMEOUT)
    public void gatewayV2QueryPlanAttemptsAreCapturedInMetrics() {
        validateQueryPlanMetrics(true);
    }

    private void validateQueryPlanMetrics(boolean gatewayV2) {
        String id = UUID.randomUUID().toString();
        resourceSetupClient.getDatabase(database.getId()).getContainer(CONTAINER_ID)
            .createItem(OBJECT_MAPPER.createObjectNode().put("id", id).put("mypk", id)).block();
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        FaultInjectionRule rule = new FaultInjectionRuleBuilder("query-plan-metric-retry-" + id)
            .condition(new FaultInjectionConditionBuilder()
                .connectionType(FaultInjectionConnectionType.GATEWAY)
                .operationType(FaultInjectionOperationType.METADATA_REQUEST_QUERY_PLAN).build())
            .result(FaultInjectionResultBuilders.getResultBuilder(FaultInjectionServerErrorType.TOO_MANY_REQUEST)
                .times(1).build())
            .hitLimit(1).build();
        try {
            CosmosMicrometerMetricsOptions options = new CosmosMicrometerMetricsOptions()
                .meterRegistry(registry).setEnabled(true)
                .configureDefaultTagNames(CosmosMetricTagName.DEFAULT, CosmosMetricTagName.SERVICE_ENDPOINT)
                .applyDiagnosticThresholdsForTransportLevelMeters(false);
            try (CosmosClient client = gatewayBuilder(gatewayV2)
                .clientTelemetryConfig(new CosmosClientTelemetryConfig().metricsOptions(options))
                .buildClient()) {
                awaitGatewayV2Readiness(client, gatewayV2);
                CosmosContainer container = client.getDatabase(database.getId()).getContainer(CONTAINER_ID);
                CosmosItemResponse<ObjectNode> response = container.readItem(id, new PartitionKey(id), ObjectNode.class);
                assertMetrics(registry, client.asyncClient().getClientCorrelationTag(),
                    response.getDiagnostics(), 200, true, gatewayV2);
                CosmosFaultInjectionHelper.configureFaultInjectionRules(
                    client.asyncClient().getDatabase(database.getId()).getContainer(CONTAINER_ID),
                    Collections.singletonList(rule)).block();
                String query = "SELECT VALUE c.id FROM c WHERE c.id = '" + id + "'";
                CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions().setPartitionKey(new PartitionKey(id));
                assertThat(container.queryItems(query, queryOptions, String.class).iterator().next()).isEqualTo(id);
                assertThat(rule.getHitCount()).isEqualTo(1);

                List<Counter> planCounters = registry.find(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUESTS.toString())
                    .tag(TagName.RequestOperationType.toString(), "Document/QueryPlan")
                    .counters().stream().collect(Collectors.toList());
                assertThat(planCounters).hasSize(2);
                assertThat(planCounters.stream().mapToDouble(Counter::count).sum()).isEqualTo(2);
                assertThat(planCounters).anySatisfy(counter ->
                    assertThat(counter.getId().getTag(TagName.RequestStatusCode.toString())).startsWith("429/"));
                assertThat(planCounters).anySatisfy(counter ->
                    assertThat(counter.getId().getTag(TagName.RequestStatusCode.toString())).isEqualTo("200/0"));
                for (Counter counter : planCounters) {
                    String destination = counter.getId().getTag(TagName.ServiceEndpoint.toString());
                    assertThat(destination).isNotNull().isNotEqualTo("NONE");
                    if (gatewayV2 && Configs.isThinClientQueryPlanEnabled()) {
                        assertThat(destination).endsWith("_10250");
                    }
                    assertThat(registry.get(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_LATENCY.toString())
                        .tags(counter.getId().getTags()).timer().count()).isEqualTo((long) counter.count());
                    assertThat(registry.find(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUEST_CHARGE.toString())
                        .tags(counter.getId().getTags()).summaries()).isEmpty();
                }
                Counter planContextCounter = registry.get(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUESTS.toString())
                    .tag(TagName.RequestOperationType.toString(), "DocumentCollection/QueryPlan")
                    .tag(TagName.RequestStatusCode.toString(), "NONE")
                    .tag(TagName.ServiceEndpoint.toString(), "NONE").counter();
                assertThat(planContextCounter.count()).isEqualTo(1);
                assertThat(registry.find(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUEST_CHARGE.toString())
                    .tag(TagName.RequestOperationType.toString(), "DocumentCollection/QueryPlan").summaries()).isEmpty();
                assertThat(container.queryItems(query, queryOptions, String.class).iterator().next()).isEqualTo(id);
                assertThat(planCounters.stream().mapToDouble(Counter::count).sum())
                    .as("A cached plan must not emit another network request measurement").isEqualTo(2);
                assertThat(planContextCounter.count())
                    .as("Cached plans retain the legacy plan-context measurement").isEqualTo(2);

                for (CosmosMetricName name : new CosmosMetricName[] {
                    CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUESTS,
                    CosmosMetricName.REQUEST_SUMMARY_GATEWAY_LATENCY}) {
                    List<Meter> meters = registry.getMeters().stream()
                        .filter(meter -> name.toString().equals(meter.getId().getName()))
                        .collect(Collectors.toList());
                    Set<String> keys = meters.get(0).getId().getTags().stream()
                        .map(Tag::getKey).collect(Collectors.toSet());
                    assertThat(meters).allSatisfy(meter -> assertThat(
                        meter.getId().getTags().stream().map(Tag::getKey).collect(Collectors.toSet()))
                        .isEqualTo(keys));
                }
            }
        } finally {
            rule.disable();
            registry.close();
        }
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
                    awaitGatewayV2Readiness(client, gatewayV2);
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

    private void awaitGatewayV2Readiness(CosmosClient client, boolean gatewayV2) {
        if (!gatewayV2) {
            return;
        }
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

    private void validateRetryMetrics(boolean gatewayV2) {
        String id = UUID.randomUUID().toString();
        ObjectNode document = OBJECT_MAPPER.createObjectNode().put("id", id).put("mypk", id);
        resourceSetupClient.getDatabase(database.getId()).getContainer(CONTAINER_ID).createItem(document).block();

        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        FaultInjectionRule rule = new FaultInjectionRuleBuilder("endpoint-metric-retry-" + id)
            .condition(new FaultInjectionConditionBuilder()
                .connectionType(FaultInjectionConnectionType.GATEWAY)
                .operationType(FaultInjectionOperationType.READ_ITEM)
                .build())
            .result(FaultInjectionResultBuilders.getResultBuilder(FaultInjectionServerErrorType.TOO_MANY_REQUEST)
                .times(1).build())
            .hitLimit(1)
            .build();
        try {
            CosmosMicrometerMetricsOptions metricsOptions = new CosmosMicrometerMetricsOptions()
                .meterRegistry(registry)
                .setEnabled(true)
                .setMetricCategories(CosmosMetricCategory.DEFAULT)
                .configureDefaultTagNames(CosmosMetricTagName.DEFAULT, CosmosMetricTagName.SERVICE_ENDPOINT)
                .applyDiagnosticThresholdsForTransportLevelMeters(false);
            try (CosmosClient client = gatewayBuilder(gatewayV2)
                .clientTelemetryConfig(new CosmosClientTelemetryConfig().metricsOptions(metricsOptions))
                .buildClient()) {
                awaitGatewayV2Readiness(client, gatewayV2);
                CosmosContainer container = client.getDatabase(database.getId()).getContainer(CONTAINER_ID);
                PartitionKey partitionKey = new PartitionKey(id);
                container.readItem(id, partitionKey, ObjectNode.class);
                CosmosFaultInjectionHelper.configureFaultInjectionRules(
                    client.asyncClient().getDatabase(database.getId()).getContainer(CONTAINER_ID),
                    Collections.singletonList(rule)).block();

                Map<Meter.Id, Double> baseline = registry.getMeters().stream()
                    .filter(meter -> meter instanceof Counter)
                    .collect(Collectors.toMap(Meter::getId, meter -> ((Counter) meter).count()));
                CosmosItemResponse<ObjectNode> response = container.readItem(id, partitionKey, ObjectNode.class);
                assertThat(response.getStatusCode()).isEqualTo(200);
                assertThat(response.getItem().get("id").asText()).isEqualTo(id);
                assertThat(rule.getHitCount()).isEqualTo(1);
                assertThat(response.getDiagnostics().getDiagnosticsContext().getRetryCount()).isGreaterThanOrEqualTo(1);

                List<ClientSideRequestStatistics.GatewayStatistics> attempts = response.getDiagnostics()
                    .getClientSideRequestStatistics().stream()
                    .flatMap(statistics -> statistics.getGatewayStatisticsList().stream())
                    .filter(statistics -> statistics.getResourceType() == ResourceType.Document
                        && statistics.getOperationType() == OperationType.Read)
                    .collect(Collectors.toList());
                assertThat(attempts).hasSizeGreaterThanOrEqualTo(2);
                assertThat(attempts.stream().filter(attempt -> attempt.getStatusCode() == 429).count()).isEqualTo(1);
                assertThat(attempts).anySatisfy(attempt -> {
                    assertThat(attempt.getStatusCode()).isEqualTo(429);
                    assertThat(attempt.getFaultInjectionRuleId()).isEqualTo(rule.getId());
                });
                assertThat(attempts).anySatisfy(attempt -> {
                    assertThat(attempt.getStatusCode()).isEqualTo(200);
                    assertThat(attempt.isHttp2()).isEqualTo(gatewayV2);
                });

                Tag correlationTag = client.asyncClient().getClientCorrelationTag();
                Map<Tags, Long> expectedCounts = attempts.stream().collect(Collectors.groupingBy(attempt -> {
                    URI endpoint = attempt.getRequestUri();
                    assertThat(endpoint).isNotNull();
                    int port = endpoint.getPort() == -1 ? 443 : endpoint.getPort();
                    if (gatewayV2) {
                        assertThat(port).isEqualTo(10250);
                    }
                    Tag endpointTag = attempt.getServiceEndpointMetricTag();
                    assertThat(endpointTag).isNotNull();
                    assertThat(endpointTag.getValue()).isEqualTo(endpoint.getHost() + "_" + port);
                    return Tags.of(correlationTag, endpointTag,
                        Tag.of(TagName.RequestOperationType.toString(), "Document/Read"),
                        Tag.of(TagName.RequestStatusCode.toString(),
                            attempt.getStatusCode() + "/" + attempt.getSubStatusCode()));
                }, Collectors.counting()));
                expectedCounts.forEach((tags, expectedCount) -> {
                    Counter counter = registry.get(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUESTS.toString())
                        .tags(tags).counter();
                    assertThat(counter.count() - baseline.getOrDefault(counter.getId(), 0d))
                        .as("Request counter delta must match recorded attempts for %s", tags)
                        .isEqualTo(expectedCount.doubleValue());
                });
                Counter operations = registry.get(CosmosMetricName.OPERATION_SUMMARY_CALLS.toString())
                    .tags(Tags.of(correlationTag, Tag.of(TagName.OperationStatusCode.toString(), "200"))).counter();
                assertThat(operations.count() - baseline.getOrDefault(operations.getId(), 0d)).isEqualTo(1);
            }
        } finally {
            rule.disable();
            registry.close();
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
            int port = endpoint.getPort() == -1 ? 443 : endpoint.getPort();
            if (gatewayV2) {
                assertThat(port).as("Document request must use GatewayV2, endpoint: %s", endpoint)
                    .isEqualTo(10250);
            }
            assertThat(statistics.isHttp2()).as("Negotiated HTTP/2 for endpoint %s", endpoint)
                .isEqualTo(gatewayV2);
            String expectedEndpoint = endpoint.getHost() + "_" + port;
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
