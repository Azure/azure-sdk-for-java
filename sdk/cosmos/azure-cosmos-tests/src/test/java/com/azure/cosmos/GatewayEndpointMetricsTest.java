// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.FeedResponseDiagnostics;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.QueryCompatibilityMode;
import com.azure.cosmos.implementation.RequestTimeline;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.RxGatewayStoreModel;
import com.azure.cosmos.implementation.ThinClientStoreModel;
import com.azure.cosmos.implementation.clienttelemetry.ClientTelemetryMetrics;
import com.azure.cosmos.implementation.clienttelemetry.TagName;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import com.azure.cosmos.implementation.directconnectivity.StoreResponseDiagnostics;
import com.azure.cosmos.implementation.directconnectivity.StoreResultDiagnostics;
import com.azure.cosmos.implementation.directconnectivity.Uri;
import com.azure.cosmos.implementation.query.QueryInfo;
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import com.azure.cosmos.models.CosmosMetricName;
import com.azure.cosmos.models.CosmosMetricCategory;
import com.azure.cosmos.models.CosmosMetricTagName;
import com.azure.cosmos.models.CosmosMicrometerMeterOptions;
import com.azure.cosmos.models.CosmosMicrometerMetricsOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class GatewayEndpointMetricsTest {
    private static final String SERVICE_ENDPOINT = TagName.ServiceEndpoint.toString();
    private static final RequestTimeline REQUEST_TIMELINE = RequestTimeline.of(
        new RequestTimeline.Event(RequestTimeline.EventName.REQUEST_SENT, Instant.EPOCH, Instant.EPOCH.plusMillis(1)));

    @DataProvider(name = "queryPlanTagConfigurations")
    public Object[][] queryPlanTagConfigurations() {
        return new Object[][] {
            {false, false, false}, {false, false, true},
            {true, false, false}, {true, false, true},
            {true, true, false}, {true, true, true}
        };
    }

    @Test(groups = {"unit"}, dataProvider = "queryPlanTagConfigurations")
    public void queryPlanAndGatewayRequestsUseTheSameTagKeys(
        boolean endpointEnabled, boolean suppressEndpoint, boolean queryPlanFirst) throws Exception {

        try (TestState state = new TestState(
            endpointEnabled ? CosmosMetricTagName.ALL : CosmosMetricTagName.DEFAULT)) {
            state.options.setMetricCategories(CosmosMetricCategory.ALL);
            if (suppressEndpoint) {
                for (CosmosMetricName name : new CosmosMetricName[] {
                    CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUESTS,
                    CosmosMetricName.REQUEST_SUMMARY_GATEWAY_LATENCY,
                    CosmosMetricName.REQUEST_DETAILS_GATEWAY_TIMELINE}) {
                    state.options.configureMeter(name,
                        new CosmosMicrometerMeterOptions().suppressTagNames(CosmosMetricTagName.SERVICE_ENDPOINT));
                }
            }
            Map<String, Set<String>> tagKeysByName = new HashMap<>();
            state.registry.config().meterFilter(new MeterFilter() {
                @Override
                public Meter.Id map(Meter.Id id) {
                    Set<String> keys = id.getTags().stream().map(Tag::getKey).collect(Collectors.toSet());
                    Set<String> existingKeys = tagKeysByName.putIfAbsent(id.getName(), keys);
                    if (existingKeys != null) {
                        assertThat(keys).as("Consistent label keys for meter %s", id.getName())
                            .isEqualTo(existingKeys);
                    }
                    return id;
                }
            });
            if (queryPlanFirst) {
                state.recordQueryPlan();
            }
            state.recordGateway(URI.create("https://account.documents.azure.com:443/dbs/db/docs/id"), 200);
            if (!queryPlanFirst) {
                state.recordQueryPlan();
            }

            for (String name : new String[] {
                CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUESTS.toString(),
                CosmosMetricName.REQUEST_SUMMARY_GATEWAY_LATENCY.toString(),
                CosmosMetricName.REQUEST_DETAILS_GATEWAY_TIMELINE + ".requestSent"}) {
                List<Meter> meters = state.registry.getMeters().stream()
                    .filter(meter -> name.equals(meter.getId().getName()))
                    .collect(Collectors.toList());
                assertThat(meters).hasSize(3);
                for (Meter meter : meters) {
                    boolean planContextMetric = "DocumentCollection/QueryPlan".equals(
                        meter.getId().getTag(TagName.RequestOperationType.toString()));
                    String expectedEndpoint = endpointEnabled && !suppressEndpoint
                        ? (planContextMetric ? "NONE" : "account.documents.azure.com_443") : null;
                    assertThat(meter.getId().getTag(SERVICE_ENDPOINT)).isEqualTo(expectedEndpoint);
                    assertThat(meter.getId().getTag(TagName.RequestStatusCode.toString()))
                        .isEqualTo(planContextMetric ? "NONE" : "200/0");
                }
            }
            assertThat(state.registry.get(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUESTS.toString())
                .tag(TagName.RequestOperationType.toString(), "DocumentCollection/QueryPlan")
                .counter().count()).isEqualTo(1);
            assertThat(state.registry.get(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_LATENCY.toString())
                .tag(TagName.RequestOperationType.toString(), "DocumentCollection/QueryPlan")
                .timer().count()).isEqualTo(1);
            assertThat(state.registry.get(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_LATENCY.toString())
                .tag(TagName.RequestOperationType.toString(), "DocumentCollection/QueryPlan")
                .timer().totalTime(TimeUnit.MILLISECONDS)).isEqualTo(10);
            assertThat(state.registry.get(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUESTS.toString())
                .tag(TagName.RequestOperationType.toString(), "Document/QueryPlan")
                .counter().count()).isEqualTo(1);
            assertThat(state.registry.get(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_LATENCY.toString())
                .tag(TagName.RequestOperationType.toString(), "Document/QueryPlan")
                .timer().totalTime(TimeUnit.MILLISECONDS)).isEqualTo(37);
            assertThat(state.registry.find(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUEST_CHARGE.toString())
                .tag(TagName.RequestOperationType.toString(), "Document/QueryPlan")
                .summaries()).isEmpty();
            assertThat(state.registry.get(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUEST_CHARGE.toString())
                .tag(TagName.RequestOperationType.toString(), "Document/Read")
                .summary().totalAmount()).isEqualTo(2.5);
            assertThat(state.registry.get(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUEST_CHARGE.toString())
                .summaries()).hasSize(1);
            assertThat(state.registry.find(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUEST_CHARGE.toString())
                .tag(TagName.RequestOperationType.toString(), "DocumentCollection/QueryPlan").summaries()).isEmpty();
        }
    }

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

    @Test(groups = {"unit"})
    public void queryPlanRetryAttemptsUseTheirActualEndpointAndStatus() throws Exception {
        try (TestState state = new TestState(CosmosMetricTagName.SERVICE_ENDPOINT)) {
            state.options.setMetricCategories(CosmosMetricCategory.ALL);
            ClientSideRequestStatistics statistics = new ClientSideRequestStatistics(mock(DiagnosticsClientContext.class));
            for (int status : new int[] {429, 200}) {
                StoreResponseDiagnostics response = mock(StoreResponseDiagnostics.class);
                doReturn(status).when(response).getStatusCode();
                doReturn(2.5).when(response).getRequestCharge();
                doReturn("activity-id").when(response).getActivityId();
                doReturn(REQUEST_TIMELINE).when(response).getRequestTimeline();
                String host = status == 429 ? "account-eastus" : "account-westus";
                doReturn(state.storeModel.getServiceEndpointMetricTag(
                    URI.create("https://" + host + ".documents.azure.com:443/docs")))
                    .when(response).getServiceEndpointMetricTag();
                RxDocumentServiceRequest request = mock(RxDocumentServiceRequest.class);
                doReturn(OperationType.QueryPlan).when(request).getOperationType();
                doReturn(ResourceType.Document).when(request).getResourceType();
                statistics.recordGatewayResponse(request, response, null);
            }
            FieldUtils.writeField(statistics, "requestStartTimeUTC", Instant.EPOCH, true);
            FieldUtils.writeField(statistics, "requestEndTimeUTC", Instant.EPOCH.plusMillis(37), true);
            FeedResponseDiagnostics feed = new FeedResponseDiagnostics(
                Collections.emptyMap(), Collections.singletonList(statistics));
            feed.setDiagnosticsContext(new QueryInfo.QueryPlanDiagnosticsContext(
                Instant.EPOCH, Instant.EPOCH.plusMillis(10), REQUEST_TIMELINE));
            state.record(new CosmosDiagnostics(feed), 200);
            FeedResponseDiagnostics laterPage = new FeedResponseDiagnostics(Collections.emptyMap(), Collections.emptyList());
            laterPage.setDiagnosticsContext(feed.getQueryPlanDiagnosticsContext());
            state.record(new CosmosDiagnostics(laterPage), 200);
            assertThat(state.registry.find(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUESTS.toString())
                .tag(TagName.RequestOperationType.toString(), "Document/QueryPlan").counters())
                .hasSize(2).allSatisfy(counter -> assertThat(counter.count()).isEqualTo(1));
            assertThat(state.registry.get(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUESTS.toString())
                .tag(TagName.RequestOperationType.toString(), "DocumentCollection/QueryPlan")
                .tag(TagName.RequestStatusCode.toString(), "NONE")
                .tag(SERVICE_ENDPOINT, "NONE").counter().count()).isEqualTo(2);
            assertThat(state.registry.get(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_LATENCY.toString())
                .tag(TagName.RequestOperationType.toString(), "DocumentCollection/QueryPlan")
                .timer().totalTime(TimeUnit.MILLISECONDS)).isEqualTo(20);
            assertThat(state.registry.find(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_LATENCY.toString())
                .tag(TagName.RequestOperationType.toString(), "Document/QueryPlan").timers())
                .hasSize(2).allSatisfy(timer ->
                    assertThat(timer.totalTime(TimeUnit.MILLISECONDS)).isEqualTo(37));
            assertThat(state.registry.find(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUEST_CHARGE.toString())
                .summaries()).isEmpty();
            assertThat(state.registry.get(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUESTS.toString())
                .tag(SERVICE_ENDPOINT, "account-eastus.documents.azure.com_443")
                .tag(TagName.RequestStatusCode.toString(), "429/0").counter().count()).isEqualTo(1);
            assertThat(state.registry.get(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUESTS.toString())
                .tag(SERVICE_ENDPOINT, "account-westus.documents.azure.com_443")
                .tag(TagName.RequestStatusCode.toString(), "200/0").counter().count()).isEqualTo(1);
        }
    }

    @Test(groups = {"unit"})
    public void queryPlanRuExclusionDoesNotDependOnOperationTag() throws Exception {
        try (TestState state = new TestState(CosmosMetricTagName.DEFAULT)) {
            state.registry.config().meterFilter(MeterFilter.ignoreTags(TagName.RequestOperationType.toString()));
            state.recordGateway(URI.create("https://account.documents.azure.com/"), 200);
            state.recordQueryPlan();

            assertThat(state.registry.get(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUEST_CHARGE.toString())
                .summary().count()).isEqualTo(1);
            assertThat(state.registry.get(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUEST_CHARGE.toString())
                .summary().totalAmount()).isEqualTo(2.5);
            assertThat(state.registry.get(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUEST_CHARGE.toString())
                .summary().getId().getTag(TagName.RequestOperationType.toString())).isNull();
        }
    }

    @Test(groups = {"unit"})
    public void queryPlanWithoutNetworkStatisticsRetainsLegacyMetrics() throws Exception {
        for (CosmosMetricTagName tags : new CosmosMetricTagName[] {
            CosmosMetricTagName.DEFAULT, CosmosMetricTagName.SERVICE_ENDPOINT}) {
            try (TestState state = new TestState(tags)) {
                state.options.setMetricCategories(CosmosMetricCategory.ALL);
                FeedResponseDiagnostics feed = new FeedResponseDiagnostics(Collections.emptyMap(), Collections.emptyList());
                feed.setDiagnosticsContext(new QueryInfo.QueryPlanDiagnosticsContext(
                    Instant.EPOCH, Instant.EPOCH.plusMillis(10), REQUEST_TIMELINE));
                state.record(new CosmosDiagnostics(feed), 200);
                assertThat(state.registry.get(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUESTS.toString())
                    .tag(TagName.RequestOperationType.toString(), "DocumentCollection/QueryPlan")
                    .tag(TagName.RequestStatusCode.toString(), "NONE").counter().count()).isEqualTo(1);
                assertThat(state.registry.get(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_LATENCY.toString())
                    .tag(TagName.RequestOperationType.toString(), "DocumentCollection/QueryPlan")
                    .timer().totalTime(TimeUnit.MILLISECONDS)).isEqualTo(10);
                assertThat(state.registry.get(CosmosMetricName.REQUEST_DETAILS_GATEWAY_TIMELINE + ".requestSent")
                    .tag(TagName.RequestOperationType.toString(), "DocumentCollection/QueryPlan")
                    .timer().totalTime(TimeUnit.MILLISECONDS)).isEqualTo(1);
                assertThat(state.registry.find(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUESTS.toString())
                    .tag(TagName.RequestOperationType.toString(), "Document/QueryPlan").counters()).isEmpty();
                assertThat(state.registry.find(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUEST_CHARGE.toString())
                    .summaries()).isEmpty();
                for (Meter meter : state.registry.getMeters()) {
                    if (meter.getId().getName().startsWith("cosmos.client.req.gw.")) {
                        assertThat(meter.getId().getTag(SERVICE_ENDPOINT))
                            .isEqualTo(tags == CosmosMetricTagName.DEFAULT ? null : "NONE");
                    }
                }
            }
        }
    }

    @Test(groups = {"unit"}, dataProvider = "gatewayEndpoints")
    public void gatewayMetersUseDestinationHostAndPort(URI endpoint, String expectedEndpoint) throws Exception {
        try (TestState state = new TestState(CosmosMetricTagName.ALL)) {
            Tag capturedTag = state.storeModel.getServiceEndpointMetricTag(endpoint);
            if ("NONE".equals(expectedEndpoint)) {
                assertThat(capturedTag).isNull();
                assertThat(FieldUtils.readField(state.storeModel, "serviceEndpointMetricTags", true))
                    .isEqualTo(Collections.emptyMap());
            } else {
                assertThat(capturedTag).isNotNull();
                assertThat(capturedTag.getValue()).isEqualTo(expectedEndpoint);
            }
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
            Tag endpointTag = state.storeModel.getServiceEndpointMetricTag(firstUri);
            state.recordGateway(firstUri, 200);
            state.recordGateway(secondUri, 200);

            assertThat(state.storeModel.getServiceEndpointMetricTag(secondUri)).isSameAs(endpointTag);
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
                .mapToObj(i -> state.storeModel.getServiceEndpointMetricTag(
                    URI.create("https://account.documents.azure.com:10250/dbs/db/docs/" + i)))
                .collect(Collectors.toList());

            assertThat(endpointTags).hasSize(100);
            assertThat(endpointTags).allSatisfy(tag -> assertThat(tag).isSameAs(endpointTags.get(0)));
            assertThat(endpointTags.get(0).getValue()).isEqualTo("account.documents.azure.com_10250");
        }
    }

    @Test(groups = {"unit"})
    public void endpointTagsAreStoreModelScopedAndClearedOnClose() throws Exception {
        URI requestUri = URI.create("https://account.documents.azure.com:10250/dbs/db/docs/id");
        try (TestState first = new TestState(CosmosMetricTagName.SERVICE_ENDPOINT);
             TestState second = new TestState(CosmosMetricTagName.SERVICE_ENDPOINT)) {
            Tag firstTag = first.storeModel.getServiceEndpointMetricTag(requestUri);
            Tag secondTag = second.storeModel.getServiceEndpointMetricTag(requestUri);
            assertThat(firstTag).isEqualTo(secondTag).isNotSameAs(secondTag);

            first.storeModel.close();
            assertThat(FieldUtils.readField(first.storeModel, "serviceEndpointMetricTags", true))
                .isEqualTo(Collections.emptyMap());
            assertThat(second.storeModel.getServiceEndpointMetricTag(requestUri)).isSameAs(secondTag);
        }
    }

    @Test(groups = {"unit"})
    public void copiedStoreModelsReuseTheSameTags() throws Exception {
        URI requestUri = URI.create("https://account.documents.azure.com:10250/dbs/db/docs/id");
        try (TestState state = new TestState(CosmosMetricTagName.SERVICE_ENDPOINT);
             RxGatewayStoreModel copy = new RxGatewayStoreModel(state.storeModel)) {
            assertThat(copy.getServiceEndpointMetricTag(requestUri))
                .isSameAs(state.storeModel.getServiceEndpointMetricTag(requestUri));
        }
    }

    @Test(groups = {"unit"})
    public void thinClientUsesInheritedEndpointTagCache() throws Exception {
        URI requestUri = URI.create("https://account.documents.azure.com:10250/dbs/db/docs/id");
        try (TestState state = new TestState(CosmosMetricTagName.SERVICE_ENDPOINT);
             ThinClientStoreModel thinClient = new ThinClientStoreModel(
                 mock(DiagnosticsClientContext.class), null, ConsistencyLevel.SESSION, null, null, null, null,
                 state.telemetryConfig)) {
            Tag tag = thinClient.getServiceEndpointMetricTag(requestUri);
            assertThat(tag.getValue()).isEqualTo("account.documents.azure.com_10250");
            assertThat(thinClient.getServiceEndpointMetricTag(
                URI.create("https://account.documents.azure.com:10250/dbs/db/docs/other"))).isSameAs(tag);
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
            assertThat(FieldUtils.readField(state.storeModel, "serviceEndpointMetricTags", true))
                .isEqualTo(Collections.emptyMap());
        }
    }

    @Test(groups = {"unit"})
    public void globallyDisabledMetricsDoNotPopulateTheStoreCache() throws Exception {
        try (TestState state = new TestState(CosmosMetricTagName.SERVICE_ENDPOINT)) {
            state.telemetryConfig.metricsOptions(
                new CosmosMicrometerMetricsOptions().configureDefaultTagNames(CosmosMetricTagName.SERVICE_ENDPOINT)
                    .setEnabled(false));
            assertThat(state.storeModel.getServiceEndpointMetricTag(
                URI.create("https://account.documents.azure.com:10250/dbs/db"))).isNull();
            assertThat(FieldUtils.readField(state.storeModel, "serviceEndpointMetricTags", true))
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
        try (TestState state = new TestState(CosmosMetricTagName.SERVICE_ENDPOINT)) {
            URI requestUri = URI.create("https://account.documents.azure.com:10250/dbs/db/docs/id");
            Tag endpointTag = state.storeModel.getServiceEndpointMetricTag(requestUri);
            StoreResponse response = new StoreResponse("not a URI", 200,
                Collections.singletonMap(HttpConstants.HttpHeaders.REQUEST_CHARGE, "2.5"), null, 0);
            response.setRequestUri(requestUri);
            response.setServiceEndpointMetricTag(endpointTag);
            StoreResponse remappedResponse = response.withRemappedStatusCode(200, 0);
            assertThat(remappedResponse.getRequestUri()).isSameAs(requestUri);
            assertThat(remappedResponse.getServiceEndpointMetricTag()).isSameAs(endpointTag);

            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
                mock(DiagnosticsClientContext.class), OperationType.Read, ResourceType.Document);
            StoreResponseDiagnostics diagnostics = StoreResponseDiagnostics.createStoreResponseDiagnostics(
                remappedResponse, request);
            assertThat(diagnostics.getRequestUri()).isSameAs(requestUri);
            assertThat(diagnostics.getEndpoint()).isEqualTo("not a URI");
            assertThat(diagnostics.getServiceEndpointMetricTag()).isSameAs(endpointTag);
            assertThat(new ObjectMapper().valueToTree(diagnostics).has("requestUri")).isFalse();
            assertThat(new ObjectMapper().valueToTree(diagnostics).has("serviceEndpointMetricTag")).isFalse();

            state.storeModel.close();
            ClientSideRequestStatistics.GatewayStatistics statistics = state.recordGateway(diagnostics);
            assertThat(statistics.getRequestUri()).isSameAs(requestUri);
            assertThat(statistics.getServiceEndpointMetricTag()).isSameAs(endpointTag);
            assertThat(new ObjectMapper().valueToTree(statistics).has("requestUri")).isFalse();
            assertThat(new ObjectMapper().valueToTree(statistics).has("serviceEndpointMetricTag")).isFalse();
            assertThat(state.registry.get(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUESTS.toString())
                .tag(SERVICE_ENDPOINT, "account.documents.azure.com_10250").counter().count()).isEqualTo(1);
        }
    }

    @Test(groups = {"unit"})
    public void failedResponsesSnapshotTheExistingExceptionUri() throws Exception {
        try (TestState state = new TestState(CosmosMetricTagName.SERVICE_ENDPOINT)) {
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
                mock(DiagnosticsClientContext.class), OperationType.Read, ResourceType.Document);
            CosmosException exception = new CosmosException(503, "test failure");
            Uri firstUri = new Uri("https://account-eastus.documents.azure.com:10250/dbs/db/docs/id");
            Tag firstTag = state.storeModel.getServiceEndpointMetricTag(firstUri.getURI());
            ImplementationBridgeHelpers.CosmosExceptionHelper.getCosmosExceptionAccessor()
                .setRequestUri(exception, firstUri);
            StoreResponseDiagnostics firstDiagnostics = StoreResponseDiagnostics.createStoreResponseDiagnostics(
                exception, request, firstTag);

            Uri secondUri = new Uri("https://account-westus.documents.azure.com/dbs/db/docs/id");
            Tag secondTag = state.storeModel.getServiceEndpointMetricTag(secondUri.getURI());
            ImplementationBridgeHelpers.CosmosExceptionHelper.getCosmosExceptionAccessor()
                .setRequestUri(exception, secondUri);
            StoreResponseDiagnostics secondDiagnostics = StoreResponseDiagnostics.createStoreResponseDiagnostics(
                exception, request, secondTag);

            ClientSideRequestStatistics.GatewayStatistics first = state.recordGateway(firstDiagnostics);
            ClientSideRequestStatistics.GatewayStatistics second = state.recordGateway(secondDiagnostics);
            assertThat(first.getRequestUri()).isSameAs(firstUri.getURI());
            assertThat(second.getRequestUri()).isSameAs(secondUri.getURI());
            assertThat(first.getServiceEndpointMetricTag()).isSameAs(firstTag);
            assertThat(second.getServiceEndpointMetricTag()).isSameAs(secondTag);
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
        private final RxGatewayStoreModel storeModel;
        private final CosmosClientTelemetryConfig telemetryConfig;

        private TestState(CosmosMetricTagName tags) throws IllegalAccessException {
            this.options = new CosmosMicrometerMetricsOptions()
                .meterRegistry(this.registry)
                .configureDefaultTagNames(tags)
                .enableHistogramsByDefault(false)
                .configureDefaultPercentiles()
                .applyDiagnosticThresholdsForTransportLevelMeters(false);
            this.options.setEnabled(true);
            this.telemetryConfig = new CosmosClientTelemetryConfig().metricsOptions(this.options);
            this.storeModel = new RxGatewayStoreModel(
                mock(DiagnosticsClientContext.class), null, ConsistencyLevel.SESSION, QueryCompatibilityMode.Default,
                null, mock(GlobalEndpointManager.class), null, null, null, this.telemetryConfig);
            FieldUtils.writeField(this.client, "clientTelemetryConfig",
                this.telemetryConfig, true);
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
            doReturn(this.storeModel.getServiceEndpointMetricTag(requestUri)).when(response).getServiceEndpointMetricTag();
            doReturn(statusCode).when(response).getStatusCode();
            doReturn(2.5).when(response).getRequestCharge();
            doReturn("activity-id").when(response).getActivityId();
            doReturn(REQUEST_TIMELINE).when(response).getRequestTimeline();
            recordGateway(response);
        }

        private void recordQueryPlan() throws IllegalAccessException {
            ClientSideRequestStatistics statistics = new ClientSideRequestStatistics(mock(DiagnosticsClientContext.class));
            RxDocumentServiceRequest request = mock(RxDocumentServiceRequest.class);
            doReturn(OperationType.QueryPlan).when(request).getOperationType();
            doReturn(ResourceType.Document).when(request).getResourceType();
            StoreResponseDiagnostics response = mock(StoreResponseDiagnostics.class);
            doReturn(200).when(response).getStatusCode();
            doReturn(2.5).when(response).getRequestCharge();
            doReturn("activity-id").when(response).getActivityId();
            doReturn(REQUEST_TIMELINE).when(response).getRequestTimeline();
            doReturn(storeModel.getServiceEndpointMetricTag(
                URI.create("https://account.documents.azure.com:443/dbs/db/docs"))).when(response).getServiceEndpointMetricTag();
            statistics.recordGatewayResponse(request, response, null);
            FieldUtils.writeField(statistics, "requestStartTimeUTC", Instant.EPOCH, true);
            FieldUtils.writeField(statistics, "requestEndTimeUTC", Instant.EPOCH.plusMillis(37), true);
            FeedResponseDiagnostics feedDiagnostics = new FeedResponseDiagnostics(
                Collections.emptyMap(), Collections.singletonList(statistics));
            feedDiagnostics.setDiagnosticsContext(new QueryInfo.QueryPlanDiagnosticsContext(
                Instant.EPOCH, Instant.EPOCH.plusMillis(10), REQUEST_TIMELINE));
            record(new CosmosDiagnostics(feedDiagnostics), 200);
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
            this.storeModel.close();
        }
    }
}
