/*
 * ApplicationInsights-Java
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.azure.monitor.opentelemetry.exporter;

import com.azure.monitor.opentelemetry.exporter.implementation.MetricDataMapper;
import com.azure.monitor.opentelemetry.exporter.implementation.builders.MetricTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.models.MetricsData;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.instrumenter.OperationListener;
import io.opentelemetry.instrumentation.api.instrumenter.http.HttpClientMetrics;
import io.opentelemetry.instrumentation.api.instrumenter.http.HttpServerMetrics;
import io.opentelemetry.instrumentation.api.instrumenter.rpc.RpcClientMetrics;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.azure.monitor.opentelemetry.exporter.implementation.preaggregatedmetrics.DependencyExtractor.DEPENDENCIES_DURATION;
import static com.azure.monitor.opentelemetry.exporter.implementation.preaggregatedmetrics.DependencyExtractor.DEPENDENCY_RESULT_CODE;
import static com.azure.monitor.opentelemetry.exporter.implementation.preaggregatedmetrics.DependencyExtractor.DEPENDENCY_SUCCESS;
import static com.azure.monitor.opentelemetry.exporter.implementation.preaggregatedmetrics.DependencyExtractor.DEPENDENCY_TARGET;
import static com.azure.monitor.opentelemetry.exporter.implementation.preaggregatedmetrics.DependencyExtractor.DEPENDENCY_TYPE;
import static com.azure.monitor.opentelemetry.exporter.implementation.preaggregatedmetrics.ExtractorHelper.FALSE;
import static com.azure.monitor.opentelemetry.exporter.implementation.preaggregatedmetrics.ExtractorHelper.MS_IS_AUTOCOLLECTED;
import static com.azure.monitor.opentelemetry.exporter.implementation.preaggregatedmetrics.ExtractorHelper.MS_METRIC_ID;
import static com.azure.monitor.opentelemetry.exporter.implementation.preaggregatedmetrics.ExtractorHelper.OPERATION_SYNTHETIC;
import static com.azure.monitor.opentelemetry.exporter.implementation.preaggregatedmetrics.ExtractorHelper.TRUE;
import static com.azure.monitor.opentelemetry.exporter.implementation.preaggregatedmetrics.RequestExtractor.REQUESTS_DURATION;
import static com.azure.monitor.opentelemetry.exporter.implementation.preaggregatedmetrics.RequestExtractor.REQUEST_RESULT_CODE;
import static com.azure.monitor.opentelemetry.exporter.implementation.preaggregatedmetrics.RequestExtractor.REQUEST_SUCCESS;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.equalTo;

public class PreAggregatedMetricsTest {

  private InMemoryMetricReader metricReader;
  private SdkMeterProvider meterProvider;

  @BeforeEach
  void setup() {
    metricReader = InMemoryMetricReader.create();
    meterProvider = SdkMeterProvider.builder().registerMetricReader(metricReader).build();
  }

  @SuppressWarnings("SystemOut")
  @Test
  void generateHttpClientMetrics() {
    OperationListener listener = HttpClientMetrics.get().create(meterProvider.get("test"));

    Attributes requestAttributes =
        Attributes.builder()
            .put("http.method", "GET")
            .put("http.url", "https://localhost:1234/")
            .put("http.host", "host")
            .put("http.target", "/")
            .put("http.scheme", "https")
            .put("net.peer.name", "localhost")
            .put("net.peer.ip", "0.0.0.0")
            .put("net.peer.port", 1234)
            .put("http.request_content_length", 100)
            .build();

    Attributes responseAttributes =
        Attributes.builder()
            .put("http.flavor", "2.0")
            .put("http.server_name", "server")
            .put("http.status_code", 200)
            .put("http.response_content_length", 200)
            .build();

    Context parent =
        Context.root()
            .with(
                Span.wrap(
                    SpanContext.create(
                        "ff01020304050600ff0a0b0c0d0e0f00",
                        "090a0b0c0d0e0f00",
                        TraceFlags.getSampled(),
                        TraceState.getDefault())));

    Context context1 = listener.onStart(parent, requestAttributes, nanos(100));
    listener.onEnd(context1, responseAttributes, nanos(250));

    Collection<MetricData> metricDataCollection = metricReader.collectAllMetrics();
    metricDataCollection =
        metricDataCollection.stream()
            .sorted(Comparator.comparing(o -> o.getName()))
            .collect(Collectors.toList());
    for (MetricData metricData : metricDataCollection) {
      System.out.println("metric: " + metricData);
    }

    assertThat(metricDataCollection.size()).isEqualTo(3);

    assertThat(metricDataCollection)
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasName("http.client.duration")
                    .hasUnit("ms")
                    .hasHistogramSatisfying(
                        histogram ->
                            histogram.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasSum(150 /* millis */)
                                        .hasAttributesSatisfying(
                                            equalTo(SemanticAttributes.NET_PEER_NAME, "localhost"),
                                            equalTo(SemanticAttributes.NET_PEER_PORT, 1234),
                                            equalTo(SemanticAttributes.HTTP_METHOD, "GET"),
                                            equalTo(SemanticAttributes.HTTP_FLAVOR, "2.0"),
                                            equalTo(SemanticAttributes.HTTP_STATUS_CODE, 200))
                                        .hasExemplarsSatisfying(
                                            exemplar ->
                                                exemplar
                                                    .hasTraceId("ff01020304050600ff0a0b0c0d0e0f00")
                                                    .hasSpanId("090a0b0c0d0e0f00")))),
            metric -> assertThat(metric).hasName("http.client.request.size"),
            metric -> assertThat(metric).hasName("http.client.response.size"));

    MetricTelemetryBuilder builder = MetricTelemetryBuilder.create();
    MetricData metricData = metricDataCollection.iterator().next();
    MetricDataMapper.updateMetricPointBuilder(
        builder, metricData, metricData.getData().getPoints().iterator().next(), true, true);
    TelemetryItem telemetryItem = builder.build();
    MetricsData metricsData = (MetricsData) telemetryItem.getData().getBaseData();

    assertThat(metricsData.getProperties())
        .containsExactlyInAnyOrderEntriesOf(
            generateExpectedDependencyCustomDimensions("http", "localhost:1234"));
  }

  @SuppressWarnings("SystemOut")
  @Test
  void generateRpcClientMetrics() {
    OperationListener listener = RpcClientMetrics.get().create(meterProvider.get("test"));

    Attributes requestAttributes =
        Attributes.builder()
            .put(SemanticAttributes.RPC_SYSTEM, "grpc")
            .put(SemanticAttributes.RPC_SERVICE, "myservice.EchoService")
            .put(SemanticAttributes.RPC_METHOD, "exampleMethod")
            .build();

    Attributes responseAttributes1 =
        Attributes.builder()
            .put(SemanticAttributes.NET_PEER_NAME, "example.com")
            .put(SemanticAttributes.NET_PEER_IP, "127.0.0.1")
            .put(SemanticAttributes.NET_PEER_PORT, 8080)
            .put(SemanticAttributes.NET_TRANSPORT, "ip_tcp")
            .build();

    Context parent =
        Context.root()
            .with(
                Span.wrap(
                    SpanContext.create(
                        "ff01020304050600ff0a0b0c0d0e0f00",
                        "090a0b0c0d0e0f00",
                        TraceFlags.getSampled(),
                        TraceState.getDefault())));

    Context context1 = listener.onStart(parent, requestAttributes, nanos(100));

    assertThat(metricReader.collectAllMetrics()).isEmpty();

    listener.onEnd(context1, responseAttributes1, nanos(250));

    Collection<MetricData> metricDataCollection = metricReader.collectAllMetrics();
    for (MetricData metricData : metricDataCollection) {
      System.out.println("metric: " + metricData);
    }

    assertThat(metricDataCollection.size()).isEqualTo(1);

    assertThat(metricDataCollection)
        .satisfiesExactlyInAnyOrder(
            metric ->
                assertThat(metric)
                    .hasName("rpc.client.duration")
                    .hasUnit("ms")
                    .hasHistogramSatisfying(
                        histogram ->
                            histogram.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasSum(150 /* millis */)
                                        .hasAttributesSatisfying(
                                            equalTo(SemanticAttributes.RPC_SYSTEM, "grpc"),
                                            equalTo(
                                                SemanticAttributes.RPC_SERVICE,
                                                "myservice.EchoService"),
                                            equalTo(SemanticAttributes.RPC_METHOD, "exampleMethod"),
                                            equalTo(
                                                SemanticAttributes.NET_PEER_NAME, "example.com"),
                                            equalTo(SemanticAttributes.NET_PEER_PORT, 8080),
                                            equalTo(SemanticAttributes.NET_TRANSPORT, "ip_tcp"))
                                        .hasExemplarsSatisfying(
                                            exemplar ->
                                                exemplar
                                                    .hasTraceId("ff01020304050600ff0a0b0c0d0e0f00")
                                                    .hasSpanId("090a0b0c0d0e0f00")))));

    MetricTelemetryBuilder builder = MetricTelemetryBuilder.create();
    MetricData metricData = metricDataCollection.iterator().next();
    MetricDataMapper.updateMetricPointBuilder(
        builder, metricData, metricData.getData().getPoints().iterator().next(), true, true);
    TelemetryItem telemetryItem = builder.build();
    MetricsData metricsData = (MetricsData) telemetryItem.getData().getBaseData();

    assertThat(metricsData.getProperties())
        .containsExactlyInAnyOrderEntriesOf(
            generateExpectedDependencyCustomDimensions("grpc", "example.com:8080"));
  }

  @SuppressWarnings("SystemOut")
  @Test
  void generateHttpServerMetrics() {
    OperationListener listener = HttpServerMetrics.get().create(meterProvider.get("test"));

    Attributes requestAttributes =
        Attributes.builder()
            .put("http.method", "GET")
            .put("http.host", "host")
            .put("http.target", "/")
            .put("http.scheme", "https")
            .put("net.host.name", "localhost")
            .put("net.host.port", 1234)
            .put("http.request_content_length", 100)
            .build();

    Attributes responseAttributes =
        Attributes.builder()
            .put("http.flavor", "2.0")
            .put("http.server_name", "server")
            .put("http.status_code", 200)
            .put("http.response_content_length", 200)
            .build();

    SpanContext spanContext1 =
        SpanContext.create(
            "ff01020304050600ff0a0b0c0d0e0f00",
            "090a0b0c0d0e0f00",
            TraceFlags.getSampled(),
            TraceState.getDefault());
    Context parent1 = Context.root().with(Span.wrap(spanContext1));
    Context context1 = listener.onStart(parent1, requestAttributes, nanos(100));
    listener.onEnd(context1, responseAttributes, nanos(250));

    Collection<MetricData> metricDataCollection = metricReader.collectAllMetrics();
    MetricData target = null;
    for (MetricData metricData : metricDataCollection) {
      if ("http.server.duration".equals(metricData.getName())) {
        target = metricData;
        System.out.println("metric: " + metricData);
      }
    }

    assertThat(target)
        .satisfies(
            metric ->
                assertThat(metric)
                    .hasName("http.server.duration")
                    .hasUnit("ms")
                    .hasHistogramSatisfying(
                        histogram ->
                            histogram.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasSum(150 /* millis */)
                                        .hasAttributesSatisfying(
                                            equalTo(SemanticAttributes.HTTP_SCHEME, "https"),
                                            equalTo(SemanticAttributes.HTTP_HOST, "host"),
                                            equalTo(SemanticAttributes.HTTP_METHOD, "GET"),
                                            equalTo(SemanticAttributes.HTTP_STATUS_CODE, 200),
                                            equalTo(SemanticAttributes.HTTP_FLAVOR, "2.0"))
                                        .hasExemplarsSatisfying(
                                            exemplar ->
                                                exemplar
                                                    .hasTraceId(spanContext1.getTraceId())
                                                    .hasSpanId(spanContext1.getSpanId())))));

    listener.onEnd(context1, responseAttributes, nanos(250));
    MetricTelemetryBuilder builder = MetricTelemetryBuilder.create();
    MetricData metricData = target;
    MetricDataMapper.updateMetricPointBuilder(
        builder, metricData, metricData.getData().getPoints().iterator().next(), true, true);
    TelemetryItem telemetryItem = builder.build();
    MetricsData metricsData = (MetricsData) telemetryItem.getData().getBaseData();

    assertThat(metricsData.getProperties())
        .containsExactlyInAnyOrderEntriesOf(generateExpectedRequestCustomDimensions("http"));
  }

  private static long nanos(int millis) {
    return TimeUnit.MILLISECONDS.toNanos(millis);
  }

  private static Map<String, String> generateExpectedDependencyCustomDimensions(
      String type, String target) {
    Map<String, String> expectedMap = new HashMap<>();
    expectedMap.put(MS_METRIC_ID, DEPENDENCIES_DURATION);
    expectedMap.put(MS_IS_AUTOCOLLECTED, TRUE);
    expectedMap.put(OPERATION_SYNTHETIC, FALSE);
    expectedMap.put(DEPENDENCY_SUCCESS, TRUE);
    if ("http".equals(type)) {
      expectedMap.put(DEPENDENCY_TYPE, "Http");
      expectedMap.put(DEPENDENCY_RESULT_CODE, "200");
    } else {
      expectedMap.put(DEPENDENCY_TYPE, "grpc");
    }
    expectedMap.put(DEPENDENCY_TARGET, target);
    // TODO test cloud_role_name and cloud_role_instance
    //    expectedMap.put(
    //        CLOUD_ROLE_NAME,
    // telemetryItem.getTags().get(ContextTagKeys.AI_CLOUD_ROLE.toString()));
    //    expectedMap.put(
    //        CLOUD_ROLE_INSTANCE,
    //        telemetryItem.getTags().get(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString()));
    return expectedMap;
  }

  private static Map<String, String> generateExpectedRequestCustomDimensions(String type) {
    Map<String, String> expectedMap = new HashMap<>();
    expectedMap.put(MS_METRIC_ID, REQUESTS_DURATION);
    expectedMap.put(MS_IS_AUTOCOLLECTED, TRUE);
    expectedMap.put(OPERATION_SYNTHETIC, FALSE);
    expectedMap.put(REQUEST_SUCCESS, TRUE);
    if ("http".equals(type)) {
      expectedMap.put(REQUEST_RESULT_CODE, "200");
    }
    // TODO test cloud_role_name and cloud_role_instance
    //    expectedMap.put(
    //        CLOUD_ROLE_NAME,
    // telemetryItem.getTags().get(ContextTagKeys.AI_CLOUD_ROLE.toString()));
    //    expectedMap.put(
    //        CLOUD_ROLE_INSTANCE,
    //        telemetryItem.getTags().get(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString()));
    return expectedMap;
  }
}
