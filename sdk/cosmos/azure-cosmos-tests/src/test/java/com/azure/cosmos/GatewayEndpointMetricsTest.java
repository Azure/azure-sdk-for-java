// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.clienttelemetry.ClientTelemetryMetrics;
import com.azure.cosmos.implementation.clienttelemetry.TagName;
import com.azure.cosmos.implementation.directconnectivity.StoreResponseDiagnostics;
import com.azure.cosmos.implementation.directconnectivity.StoreResultDiagnostics;
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import com.azure.cosmos.models.CosmosMetricName;
import com.azure.cosmos.models.CosmosMetricTagName;
import com.azure.cosmos.models.CosmosMicrometerMeterOptions;
import com.azure.cosmos.models.CosmosMicrometerMetricsOptions;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class GatewayEndpointMetricsTest {
    private static final String SERVICE_ENDPOINT = TagName.ServiceEndpoint.toString();

    @DataProvider(name = "gatewayEndpoints")
    public Object[][] gatewayEndpoints() {
        return new Object[][] {
            {"https://account-eastus.documents.azure.com/dbs/db/colls/c/docs/id", "account-eastus.documents.azure.com_443"},
            {"https://account-eastus.documents.azure.com:443/", "account-eastus.documents.azure.com_443"},
            {"https://account-eastus.documents.azure.com:10250/dbs/db/colls/c/docs/id",
                "account-eastus.documents.azure.com_10250"},
            {"https://localhost:8081/dbs/db", "localhost_8081"},
            {"http://localhost/dbs/db", "NONE"},
            {"http://127.0.0.1:8080/dbs/db", "NONE"},
            {"https://user:password@account.documents.azure.com/dbs/db?key=value#fragment",
                "account.documents.azure.com_443"},
            {null, "NONE"},
            {"", "NONE"},
            {"  ", "NONE"},
            {"not a URI", "NONE"},
            {"https:///dbs/db", "NONE"},
            {"https://[invalid", "NONE"},
            {"rntbd://replica.documents.azure.com:14050/", "NONE"}
        };
    }

    @Test(groups = {"unit"}, dataProvider = "gatewayEndpoints")
    public void gatewayMetersUseDestinationHostAndPort(String endpoint, String expectedEndpoint) throws Exception {
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
            state.recordGateway("https://account.documents.azure.com/dbs/db1?query=first", 200);
            state.recordGateway("https://account.documents.azure.com:443/dbs/db2?query=second", 200);

            assertThat(state.registry.get(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUESTS.toString())
                .counters()).hasSize(1);
            assertThat(state.registry.get(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUESTS.toString())
                .tag(SERVICE_ENDPOINT, "account.documents.azure.com_443").counter().count()).isEqualTo(2);
        }
    }

    @Test(groups = {"unit"})
    public void failedRequestsRetainTheirDestination() throws Exception {
        try (TestState state = new TestState(CosmosMetricTagName.SERVICE_ENDPOINT)) {
            state.recordGateway("https://account-eastus.documents.azure.com/dbs/db", 503);
            state.recordGateway("https://account-westus.documents.azure.com/dbs/db", 200);

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
            state.recordGateway("https://account.documents.azure.com/", 200);

            assertThat(state.registry.get(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUESTS.toString())
                .counter().count()).isEqualTo(1);
            for (Meter meter : state.registry.getMeters()) {
                assertThat(meter.getId().getTag(SERVICE_ENDPOINT)).isNull();
            }
        }
    }

    @Test(groups = {"unit"})
    public void endpointTagCanBeSuppressedPerMeter() throws Exception {
        try (TestState state = new TestState(CosmosMetricTagName.SERVICE_ENDPOINT)) {
            state.options.configureMeter(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_LATENCY,
                new CosmosMicrometerMeterOptions().suppressTagNames(CosmosMetricTagName.SERVICE_ENDPOINT));
            state.recordGateway("https://account.documents.azure.com/", 200);

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

    private static final class TestState implements AutoCloseable {
        private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
        private final CosmosMicrometerMetricsOptions options;
        private final CosmosAsyncClient client = mock(CosmosAsyncClient.class);

        private TestState(CosmosMetricTagName tags) throws IllegalAccessException {
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

        private void recordGateway(String endpoint, int statusCode) {
            CosmosDiagnostics diagnostics = new CosmosDiagnostics(mock(DiagnosticsClientContext.class));
            RxDocumentServiceRequest request = mock(RxDocumentServiceRequest.class);
            doReturn(OperationType.Read).when(request).getOperationType();
            doReturn(ResourceType.Document).when(request).getResourceType();
            StoreResponseDiagnostics response = mock(StoreResponseDiagnostics.class);
            doReturn(endpoint).when(response).getEndpoint();
            doReturn(statusCode).when(response).getStatusCode();
            doReturn(2.5).when(response).getRequestCharge();
            doReturn("activity-id").when(response).getActivityId();
            diagnostics.clientSideRequestStatistics().recordGatewayResponse(request, response, null);
            record(diagnostics, statusCode);
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
        }
    }
}
