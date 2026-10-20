// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.DatabaseAccountManagerInternal;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.clienttelemetry.ClientTelemetryMetrics;
import com.azure.cosmos.implementation.clienttelemetry.TagName;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import com.azure.cosmos.implementation.directconnectivity.StoreResponseDiagnostics;
import com.azure.cosmos.implementation.directconnectivity.StoreResultDiagnostics;
import com.azure.cosmos.implementation.directconnectivity.Uri;
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import com.azure.cosmos.models.CosmosMetricName;
import com.azure.cosmos.models.CosmosMetricTagName;
import com.azure.cosmos.models.CosmosMicrometerMeterOptions;
import com.azure.cosmos.models.CosmosMicrometerMetricsOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class GatewayEndpointMetricsTest {
    private static final String SERVICE_ENDPOINT = TagName.ServiceEndpoint.toString();

    @DataProvider(name = "gatewayEndpoints")
    public Object[][] gatewayEndpoints() {
        return new Object[][] {
            {URI.create("https://account-eastus.documents.azure.com/dbs/db/colls/c/docs/id"),
                "account-eastus.documents.azure.com_443"},
            {URI.create("https://account-eastus.documents.azure.com:443/"), "account-eastus.documents.azure.com_443"},
            {URI.create("https://account-eastus.documents.azure.com:10250/dbs/db/colls/c/docs/id"),
                "account-eastus.documents.azure.com_10250"},
            {URI.create("https://localhost:8081/dbs/db"), "localhost_8081"},
            {URI.create("http://localhost/dbs/db"), "NONE"},
            {URI.create("http://127.0.0.1:8080/dbs/db"), "NONE"},
            {URI.create("https://user:password@account.documents.azure.com/dbs/db?key=value#fragment"),
                "account.documents.azure.com_443"},
            {null, "NONE"},
            {URI.create(""), "NONE"},
            {URI.create("https:///dbs/db"), "NONE"},
            {URI.create("rntbd://replica.documents.azure.com:14050/"), "NONE"}
        };
    }

    @Test(groups = {"unit"}, dataProvider = "gatewayEndpoints")
    public void gatewayMetersUseDestinationHostAndPort(URI endpoint, String expectedEndpoint) throws Exception {
        try (TestState state = new TestState(CosmosMetricTagName.ALL)) {
            state.recordGateway(endpoint, 200);

            assertThat(state.registry.get(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUESTS.toString())
                .tag(SERVICE_ENDPOINT, expectedEndpoint).counter().count()).isEqualTo(1);
            assertThat(state.registry.get(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_LATENCY.toString())
                .tag(SERVICE_ENDPOINT, expectedEndpoint).timer().count()).isEqualTo(1);
            assertThat(state.registry.get(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUEST_CHARGE.toString())
                .tag(SERVICE_ENDPOINT, expectedEndpoint).summary().totalAmount()).isEqualTo(2.5);

            for (Meter meter : state.registry.getMeters()) {
                if (meter.getId().getName().startsWith("cosmos.client.req.gw.")) {
                    assertThat(meter.getId().getTag(SERVICE_ENDPOINT)).isEqualTo(expectedEndpoint);
                    assertThat(meter.getId().getTag(TagName.ServiceAddress.toString())).isNull();
                    assertThat(meter.getId().getTag(TagName.PartitionId.toString())).isNull();
                    assertThat(meter.getId().getTag(TagName.ReplicaId.toString())).isNull();
                } else if (meter.getId().getName().startsWith("cosmos.client.op.")) {
                    assertThat(meter.getId().getTag(SERVICE_ENDPOINT)).isNull();
                }
            }
        }
    }

    @Test(groups = {"unit"})
    public void resourcePathsDoNotCreateSeparateEndpointSeries() throws Exception {
        try (TestState state = new TestState(CosmosMetricTagName.SERVICE_ENDPOINT)) {
            state.recordGateway(URI.create("https://account.documents.azure.com/dbs/db1?query=first"), 200);
            state.recordGateway(URI.create("https://account.documents.azure.com:443/dbs/db2?query=second"), 200);

            assertThat(state.registry.get(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUESTS.toString())
                .counters()).hasSize(1);
            assertThat(state.registry.get(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUESTS.toString())
                .tag(SERVICE_ENDPOINT, "account.documents.azure.com_443").counter().count()).isEqualTo(2);
        }
    }

    @Test(groups = {"unit"})
    public void requestsReuseTheCachedEndpointTag() throws Exception {
        URI firstUri = URI.create("https://account.documents.azure.com:10250/dbs/db/docs/first");
        URI secondUri = URI.create("https://account.documents.azure.com:10250/dbs/db/docs/second?query=value");
        try (TestState state = new TestState(CosmosMetricTagName.SERVICE_ENDPOINT)) {
            Tag endpointTag = state.endpointManager.getServiceEndpointMetricTag(firstUri);
            state.recordGateway(firstUri, 200);
            state.recordGateway(secondUri, 200);

            assertThat(state.endpointManager.getServiceEndpointMetricTag(secondUri)).isSameAs(endpointTag);
            Tag emittedTag = state.registry.get(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUESTS.toString())
                .counter().getId().getTags().stream()
                .filter(tag -> SERVICE_ENDPOINT.equals(tag.getKey()))
                .findFirst().orElseThrow(AssertionError::new);
            assertThat(emittedTag).isSameAs(endpointTag);
        }
    }

    @Test(groups = {"unit"})
    public void concurrentRequestsShareOneEndpointTag() throws Exception {
        try (TestState state = new TestState(CosmosMetricTagName.SERVICE_ENDPOINT)) {
            List<Tag> endpointTags = IntStream.range(0, 100).parallel()
                .mapToObj(i -> state.endpointManager.getServiceEndpointMetricTag(
                    URI.create("https://account.documents.azure.com:10250/dbs/db/docs/" + i)))
                .collect(Collectors.toList());

            assertThat(endpointTags).hasSize(100);
            assertThat(endpointTags).allSatisfy(tag -> assertThat(tag).isSameAs(endpointTags.get(0)));
            assertThat(endpointTags.get(0).getValue()).isEqualTo("account.documents.azure.com_10250");
        }
    }

    @Test(groups = {"unit"})
    public void endpointTagsAreClientScopedAndClearedOnClose() throws Exception {
        URI requestUri = URI.create("https://account.documents.azure.com:10250/dbs/db/docs/id");
        try (TestState first = new TestState(CosmosMetricTagName.SERVICE_ENDPOINT);
             TestState second = new TestState(CosmosMetricTagName.SERVICE_ENDPOINT)) {
            Tag firstTag = first.endpointManager.getServiceEndpointMetricTag(requestUri);
            Tag secondTag = second.endpointManager.getServiceEndpointMetricTag(requestUri);
            assertThat(firstTag).isEqualTo(secondTag).isNotSameAs(secondTag);

            first.endpointManager.close();
            assertThat(FieldUtils.readField(first.endpointManager, "serviceEndpointMetricTags", true))
                .isEqualTo(Collections.emptyMap());
            assertThat(second.endpointManager.getServiceEndpointMetricTag(requestUri)).isSameAs(secondTag);
        }
    }

    @Test(groups = {"unit"})
    public void failedRequestsRetainTheirDestination() throws Exception {
        try (TestState state = new TestState(CosmosMetricTagName.SERVICE_ENDPOINT)) {
            state.recordGateway(URI.create("https://account-eastus.documents.azure.com/dbs/db"), 503);
            state.recordGateway(URI.create("https://account-westus.documents.azure.com/dbs/db"), 200);

            assertThat(state.registry.get(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUESTS.toString())
                .tag(SERVICE_ENDPOINT, "account-eastus.documents.azure.com_443")
                .tag(TagName.RequestStatusCode.toString(), "503/0").counter().count()).isEqualTo(1);
            assertThat(state.registry.get(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUESTS.toString())
                .tag(SERVICE_ENDPOINT, "account-westus.documents.azure.com_443")
                .tag(TagName.RequestStatusCode.toString(), "200/0").counter().count()).isEqualTo(1);
        }
    }

    @Test(groups = {"unit"})
    public void endpointTagIsDisabledByDefault() throws Exception {
        try (TestState state = new TestState(CosmosMetricTagName.DEFAULT)) {
            state.recordGateway(URI.create("https://account.documents.azure.com/"), 200);

            assertThat(state.registry.get(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUESTS.toString())
                .counter().count()).isEqualTo(1);
            for (Meter meter : state.registry.getMeters()) {
                assertThat(meter.getId().getTag(SERVICE_ENDPOINT)).isNull();
            }
            assertThat(FieldUtils.readField(state.endpointManager, "serviceEndpointMetricTags", true))
                .isEqualTo(Collections.emptyMap());
        }
    }

    @Test(groups = {"unit"})
    public void endpointTagCanBeSuppressedPerMeter() throws Exception {
        try (TestState state = new TestState(CosmosMetricTagName.SERVICE_ENDPOINT)) {
            state.options.configureMeter(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_LATENCY,
                new CosmosMicrometerMeterOptions().suppressTagNames(CosmosMetricTagName.SERVICE_ENDPOINT));
            state.recordGateway(URI.create("https://account.documents.azure.com/"), 200);

            assertThat(state.registry.get(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_LATENCY.toString())
                .timer().getId().getTag(SERVICE_ENDPOINT)).isNull();
            assertThat(state.registry.get(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUESTS.toString())
                .counter().getId().getTag(SERVICE_ENDPOINT)).isEqualTo("account.documents.azure.com_443");
        }
    }

    @Test(groups = {"unit"})
    public void directRequestEndpointFormatIsUnchanged() throws Exception {
        try (TestState state = new TestState(CosmosMetricTagName.SERVICE_ENDPOINT)) {
            CosmosDiagnostics diagnostics = new CosmosDiagnostics(mock(DiagnosticsClientContext.class));
            StoreResponseDiagnostics response = mock(StoreResponseDiagnostics.class);
            doReturn(200).when(response).getStatusCode();
            StoreResultDiagnostics result = mock(StoreResultDiagnostics.class);
            doReturn(response).when(result).getStoreResponseDiagnostics();
            doReturn("replica.documents.azure.com_14050").when(result).getStorePhysicalAddressEscapedAuthority();
            ClientSideRequestStatistics.StoreResponseStatistics statistics =
                mock(ClientSideRequestStatistics.StoreResponseStatistics.class);
            doReturn(result).when(statistics).getStoreResult();
            doReturn(ResourceType.Document).when(statistics).getRequestResourceType();
            doReturn(OperationType.Read).when(statistics).getRequestOperationType();
            diagnostics.clientSideRequestStatistics().getResponseStatisticsList().add(statistics);

            state.record(diagnostics, 200);

            assertThat(state.registry.get(CosmosMetricName.REQUEST_SUMMARY_DIRECT_REQUESTS.toString())
                .tag(SERVICE_ENDPOINT, "replica.documents.azure.com_14050").counter().count()).isEqualTo(1);
        }
    }

    @Test(groups = {"unit"})
    public void successfulResponseReusesCapturedUriWithoutParsingEndpointText() throws Exception {
        URI requestUri = URI.create("https://account.documents.azure.com:10250/dbs/db/docs/id");
        StoreResponse response = new StoreResponse("not a URI", 200,
            Collections.singletonMap(HttpConstants.HttpHeaders.REQUEST_CHARGE, "2.5"), null, 0);
        response.setRequestUri(requestUri);
        StoreResponse remappedResponse = response.withRemappedStatusCode(200, 0);
        assertThat(remappedResponse.getRequestUri()).isSameAs(requestUri);

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
            mock(DiagnosticsClientContext.class), OperationType.Read, ResourceType.Document);
        StoreResponseDiagnostics diagnostics = StoreResponseDiagnostics.createStoreResponseDiagnostics(
            remappedResponse, request);
        assertThat(diagnostics.getRequestUri()).isSameAs(requestUri);
        assertThat(diagnostics.getEndpoint()).isEqualTo("not a URI");
        assertThat(new ObjectMapper().valueToTree(diagnostics).has("requestUri")).isFalse();

        try (TestState state = new TestState(CosmosMetricTagName.SERVICE_ENDPOINT)) {
            ClientSideRequestStatistics.GatewayStatistics statistics = state.recordGateway(diagnostics);
            assertThat(statistics.getRequestUri()).isSameAs(requestUri);
            assertThat(new ObjectMapper().valueToTree(statistics).has("requestUri")).isFalse();
            assertThat(state.registry.get(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUESTS.toString())
                .tag(SERVICE_ENDPOINT, "account.documents.azure.com_10250").counter().count()).isEqualTo(1);
        }
    }

    @Test(groups = {"unit"})
    public void failedResponsesSnapshotTheExistingExceptionUri() throws Exception {
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
            mock(DiagnosticsClientContext.class), OperationType.Read, ResourceType.Document);
        CosmosException exception = new CosmosException(503, "test failure");
        Uri firstUri = new Uri("https://account-eastus.documents.azure.com:10250/dbs/db/docs/id");
        ImplementationBridgeHelpers.CosmosExceptionHelper.getCosmosExceptionAccessor()
            .setRequestUri(exception, firstUri);
        StoreResponseDiagnostics firstDiagnostics = StoreResponseDiagnostics.createStoreResponseDiagnostics(
            exception, request);

        Uri secondUri = new Uri("https://account-westus.documents.azure.com/dbs/db/docs/id");
        ImplementationBridgeHelpers.CosmosExceptionHelper.getCosmosExceptionAccessor()
            .setRequestUri(exception, secondUri);
        StoreResponseDiagnostics secondDiagnostics = StoreResponseDiagnostics.createStoreResponseDiagnostics(
            exception, request);

        try (TestState state = new TestState(CosmosMetricTagName.SERVICE_ENDPOINT)) {
            assertThat(state.recordGateway(firstDiagnostics).getRequestUri()).isSameAs(firstUri.getURI());
            assertThat(state.recordGateway(secondDiagnostics).getRequestUri()).isSameAs(secondUri.getURI());
            assertThat(state.registry.get(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUESTS.toString())
                .tag(SERVICE_ENDPOINT, "account-eastus.documents.azure.com_10250").counter().count()).isEqualTo(1);
            assertThat(state.registry.get(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUESTS.toString())
                .tag(SERVICE_ENDPOINT, "account-westus.documents.azure.com_443").counter().count()).isEqualTo(1);
        }
    }

    @Test(groups = {"unit"})
    public void missingCapturedUriDoesNotTriggerEndpointTextParsing() throws Exception {
        StoreResponse response = new StoreResponse("https://account.documents.azure.com:443/dbs/db", 200,
            Collections.singletonMap(HttpConstants.HttpHeaders.REQUEST_CHARGE, "2.5"), null, 0);
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
            mock(DiagnosticsClientContext.class), OperationType.Read, ResourceType.Document);
        StoreResponseDiagnostics diagnostics = StoreResponseDiagnostics.createStoreResponseDiagnostics(
            response, request);

        try (TestState state = new TestState(CosmosMetricTagName.SERVICE_ENDPOINT)) {
            assertThat(state.recordGateway(diagnostics).getRequestUri()).isNull();
            assertThat(state.registry.get(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUESTS.toString())
                .tag(SERVICE_ENDPOINT, "NONE").counter().count()).isEqualTo(1);
        }
    }

    private static final class TestState implements AutoCloseable {
        private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
        private final CosmosMicrometerMetricsOptions options;
        private final CosmosAsyncClient client = mock(CosmosAsyncClient.class);
        private final GlobalEndpointManager endpointManager;

        private TestState(CosmosMetricTagName tags) throws IllegalAccessException {
            DatabaseAccountManagerInternal owner = mock(DatabaseAccountManagerInternal.class);
            doReturn(URI.create("https://account.documents.azure.com")).when(owner).getServiceEndpoint();
            this.endpointManager = new GlobalEndpointManager(
                owner, new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig()), new Configs());
            AsyncDocumentClient contextClient = mock(AsyncDocumentClient.class);
            doReturn(this.endpointManager).when(contextClient).getGlobalEndpointManager();
            doReturn(contextClient).when(this.client).getContextClient();
            this.options = new CosmosMicrometerMetricsOptions()
                .meterRegistry(this.registry)
                .configureDefaultTagNames(tags)
                .enableHistogramsByDefault(false)
                .configureDefaultPercentiles()
                .applyDiagnosticThresholdsForTransportLevelMeters(false);
            this.options.setEnabled(true);
            FieldUtils.writeField(this.client, "clientTelemetryConfig",
                new CosmosClientTelemetryConfig().metricsOptions(this.options), true);
            FieldUtils.writeField(this.client, "clientMetricRegistrySnapshot", this.registry, true);
            doReturn("account").when(this.client).getAccountTagValue();
            doReturn(Tag.of(TagName.ClientCorrelationId.toString(), "gateway-endpoint-metrics"))
                .when(this.client).getClientCorrelationTag();
            ClientTelemetryMetrics.add(this.registry, null, null);
        }

        private void recordGateway(URI requestUri, int statusCode) {
            StoreResponseDiagnostics response = mock(StoreResponseDiagnostics.class);
            doReturn(requestUri != null ? requestUri.toString() : null).when(response).getEndpoint();
            doReturn(requestUri).when(response).getRequestUri();
            doReturn(statusCode).when(response).getStatusCode();
            doReturn(2.5).when(response).getRequestCharge();
            doReturn("activity-id").when(response).getActivityId();
            recordGateway(response);
        }

        private ClientSideRequestStatistics.GatewayStatistics recordGateway(StoreResponseDiagnostics response) {
            CosmosDiagnostics diagnostics = new CosmosDiagnostics(mock(DiagnosticsClientContext.class));
            RxDocumentServiceRequest request = mock(RxDocumentServiceRequest.class);
            doReturn(OperationType.Read).when(request).getOperationType();
            doReturn(ResourceType.Document).when(request).getResourceType();
            doReturn(new UUID(0, 1)).when(request).getActivityId();
            diagnostics.clientSideRequestStatistics().recordGatewayResponse(request, response, null);
            record(diagnostics, response.getStatusCode());
            return diagnostics.clientSideRequestStatistics().getGatewayStatisticsList().get(0);
        }

        private void record(CosmosDiagnostics diagnostics, int statusCode) {
            CosmosDiagnosticsContext context = mock(CosmosDiagnosticsContext.class);
            doReturn(statusCode).when(context).getStatusCode();
            doReturn("Read").when(context).getOperationType();
            doReturn("Document").when(context).getResourceType();
            doReturn(ConsistencyLevel.SESSION).when(context).getEffectiveConsistencyLevel();
            doReturn(Duration.ofMillis(10)).when(context).getDuration();
            doReturn(Collections.singletonList(diagnostics)).when(context).getDiagnostics();
            ClientTelemetryMetrics.recordOperation(this.client, context);
        }

        @Override
        public void close() {
            ClientTelemetryMetrics.remove(this.registry);
            this.endpointManager.close();
        }
    }
}
