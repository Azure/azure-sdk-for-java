// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse;

import com.azure.monitor.opentelemetry.exporter.implementation.builders.ExceptionTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.configuration.ConnectionString;
import com.azure.monitor.opentelemetry.exporter.implementation.models.MetricDataPoint;
import com.azure.monitor.opentelemetry.exporter.implementation.models.MetricsData;
import com.azure.monitor.opentelemetry.exporter.implementation.models.MonitorBase;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;
import com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.model.OTelMetric;
import com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.model.QuickPulseMetrics;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.Resource;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import static com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.QuickPulseTestBase.createRemoteDependencyTelemetry;
import static com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.QuickPulseTestBase.createRequestTelemetry;
import static org.assertj.core.api.Assertions.assertThat;

class QuickPulseDataCollectorTests {

    private static final String FAKE_INSTRUMENTATION_KEY = "fake-instrumentation-key";
    private static final ConnectionString FAKE_CONNECTION_STRING
        = ConnectionString.parse("InstrumentationKey=" + FAKE_INSTRUMENTATION_KEY);

    @Test
    void initialStateIsDisabled() {
        assertThat(new QuickPulseDataCollector(true, new QuickPulseConfiguration()).peek()).isNull();
    }

    @Test
    void emptyCountsAndDurationsAfterEnable() {
        QuickPulseDataCollector collector = new QuickPulseDataCollector(true, new QuickPulseConfiguration());

        collector.enable(FAKE_CONNECTION_STRING::getInstrumentationKey);
        QuickPulseDataCollector.FinalCounters counters = collector.peek();
        assertCountersReset(counters);
        ArrayList<QuickPulseMetrics> storedMetrics = collector.retrieveOtelMetrics();
        assertThat(storedMetrics).isEmpty();
    }

    @Test
    void nullCountersAfterDisable() {
        QuickPulseDataCollector collector = new QuickPulseDataCollector(true, new QuickPulseConfiguration());

        collector.enable(FAKE_CONNECTION_STRING::getInstrumentationKey);
        collector.disable();
        assertThat(collector.peek()).isNull();
        assertThat(collector.retrieveOtelMetrics()).isEmpty();
    }

    @Test
    void requestTelemetryIsCounted_DurationIsSum() {
        QuickPulseDataCollector collector = new QuickPulseDataCollector(true, new QuickPulseConfiguration());

        collector.setQuickPulseStatus(QuickPulseStatus.QP_IS_ON);
        collector.enable(FAKE_CONNECTION_STRING::getInstrumentationKey);

        // add a success and peek
        long duration = 112233L;
        TelemetryItem telemetry = createRequestTelemetry("request-test", new Date(), duration, "200", true);
        telemetry.setConnectionString(FAKE_CONNECTION_STRING);
        collector.add(telemetry);
        QuickPulseDataCollector.FinalCounters counters = collector.peek();
        assertThat(counters.requests).isEqualTo(1);
        assertThat(counters.unsuccessfulRequests).isEqualTo(0);
        assertThat(counters.requestsDuration).isEqualTo((double) duration);

        // add another success and peek
        long duration2 = 65421L;
        telemetry = createRequestTelemetry("request-test-2", new Date(), duration2, "200", true);
        telemetry.setConnectionString(FAKE_CONNECTION_STRING);
        collector.add(telemetry);
        counters = collector.peek();
        double total = duration + duration2;
        assertThat(counters.requests).isEqualTo(2);
        assertThat(counters.unsuccessfulRequests).isEqualTo(0);
        assertThat(counters.requestsDuration).isEqualTo(total);

        // add a failure and get/reset
        long duration3 = 9988L;
        telemetry = createRequestTelemetry("request-test-3", new Date(), duration3, "400", false);
        telemetry.setConnectionString(FAKE_CONNECTION_STRING);
        collector.add(telemetry);
        counters = collector.getAndRestart();
        total += duration3;
        assertThat(counters.requests).isEqualTo(3);
        assertThat(counters.unsuccessfulRequests).isEqualTo(1);
        assertThat(counters.requestsDuration).isEqualTo(total);

        assertCountersReset(collector.peek());
    }

    @Test
    void dependencyTelemetryIsCounted_DurationIsSum() {
        QuickPulseDataCollector collector = new QuickPulseDataCollector(true, new QuickPulseConfiguration());

        collector.setQuickPulseStatus(QuickPulseStatus.QP_IS_ON);
        collector.enable(FAKE_CONNECTION_STRING::getInstrumentationKey);

        // add a success and peek.
        long duration = 112233L;
        TelemetryItem telemetry = createRemoteDependencyTelemetry("dep-test", "dep-test-cmd", duration, true);
        telemetry.setConnectionString(FAKE_CONNECTION_STRING);
        collector.add(telemetry);
        QuickPulseDataCollector.FinalCounters counters = collector.peek();
        assertThat(counters.rdds).isEqualTo(1);
        assertThat(counters.unsuccessfulRdds).isEqualTo(0);
        assertThat(counters.rddsDuration).isEqualTo((double) duration);

        // add another success and peek.
        long duration2 = 334455L;
        telemetry = createRemoteDependencyTelemetry("dep-test-2", "dep-test-cmd-2", duration2, true);
        telemetry.setConnectionString(FAKE_CONNECTION_STRING);
        collector.add(telemetry);
        counters = collector.peek();
        assertThat(counters.rdds).isEqualTo(2);
        assertThat(counters.unsuccessfulRdds).isEqualTo(0);
        double total = duration + duration2;
        assertThat(counters.rddsDuration).isEqualTo(total);

        // add a failure and get/reset.
        long duration3 = 123456L;
        telemetry = createRemoteDependencyTelemetry("dep-test-3", "dep-test-cmd-3", duration3, false);
        telemetry.setConnectionString(FAKE_CONNECTION_STRING);
        collector.add(telemetry);
        counters = collector.getAndRestart();
        assertThat(counters.rdds).isEqualTo(3);
        assertThat(counters.unsuccessfulRdds).isEqualTo(1);
        total += duration3;
        assertThat(counters.rddsDuration).isEqualTo(total);

        assertCountersReset(collector.peek());
    }

    @Test
    void exceptionTelemetryIsCounted() {
        QuickPulseDataCollector collector = new QuickPulseDataCollector(true, new QuickPulseConfiguration());

        collector.setQuickPulseStatus(QuickPulseStatus.QP_IS_ON);
        collector.enable(FAKE_CONNECTION_STRING::getInstrumentationKey);

        TelemetryItem telemetry = ExceptionTelemetryBuilder.create().build();
        telemetry.setConnectionString(FAKE_CONNECTION_STRING);
        collector.add(telemetry);
        QuickPulseDataCollector.FinalCounters counters = collector.peek();
        assertThat(counters.exceptions).isEqualTo(1);

        telemetry = ExceptionTelemetryBuilder.create().build();
        telemetry.setConnectionString(FAKE_CONNECTION_STRING);
        collector.add(telemetry);
        counters = collector.getAndRestart();
        assertThat(counters.exceptions).isEqualTo(2);

        assertCountersReset(collector.peek());
    }

    @Test
    void openTelemetryMetricsAreCounted() {
        QuickPulseDataCollector collector = new QuickPulseDataCollector(true, new QuickPulseConfiguration());

        collector.setQuickPulseStatus(QuickPulseStatus.QP_IS_ON);
        collector.enable(FAKE_CONNECTION_STRING::getInstrumentationKey);

        TelemetryItem telemetry = new TelemetryItem();
        telemetry.setConnectionString(FAKE_CONNECTION_STRING);
        MonitorBase data = new MonitorBase();
        MetricDataPoint point = new MetricDataPoint();
        point.setName("TestMetric");
        point.setValue(123.456);
        ArrayList<MetricDataPoint> metricsList = new ArrayList<>();
        metricsList.add(point);
        data.setBaseData(new MetricsData().setMetrics(metricsList));
        telemetry.setData(data);
        Attributes attributes = Attributes.builder().put("telemetry.sdk.name", "opentelemetry").build();
        Resource resource = Resource.create(attributes);
        telemetry.setResource(resource);
        collector.addOtelMetric(telemetry);
        ConcurrentHashMap<String, OTelMetric> storedMetrics = collector.getOtelMetrics();
        assertThat(storedMetrics.size()).isEqualTo(1);
        assertThat(storedMetrics.containsKey("TestMetric")).isTrue();
        assertThat(storedMetrics.get("TestMetric").getDataValues().get(0)).isEqualTo(123.456);

        point.setName("TestMetric2");
        point.setValue(789.012);
        collector.addOtelMetric(telemetry);
        storedMetrics = collector.getOtelMetrics();
        assertThat(storedMetrics.size()).isEqualTo(2);
        assertThat(storedMetrics.containsKey("TestMetric2")).isTrue();
        assertThat(storedMetrics.get("TestMetric2").getDataValues().get(0)).isEqualTo(789.012);

        collector.flushOtelMetrics();
        assertThat(collector.getOtelMetrics().size()).isEqualTo(0);
    }

    @Test
    void encodeDecodeIsIdentity() {
        long count = 456L;
        long duration = 112233L;
        long encoded = QuickPulseDataCollector.Counters.encodeCountAndDuration(count, duration);
        QuickPulseDataCollector.CountAndDuration inputs
            = QuickPulseDataCollector.Counters.decodeCountAndDuration(encoded);
        assertThat(inputs.count).isEqualTo(count);
        assertThat(inputs.duration).isEqualTo(duration);
    }

    @Test
    void parseDurations() {
        assertThat(QuickPulseDataCollector.parseDurationToMillis("00:00:00.123456")).isEqualTo(123);
        // current behavior rounds down (not sure if that's good or not?)
        assertThat(QuickPulseDataCollector.parseDurationToMillis("00:00:00.123999")).isEqualTo(123);
        assertThat(QuickPulseDataCollector.parseDurationToMillis("00:00:01.123456"))
            .isEqualTo(Duration.ofSeconds(1).plusMillis(123).toMillis());
        assertThat(QuickPulseDataCollector.parseDurationToMillis("00:00:12.123456"))
            .isEqualTo(Duration.ofSeconds(12).plusMillis(123).toMillis());
        assertThat(QuickPulseDataCollector.parseDurationToMillis("00:01:23.123456"))
            .isEqualTo(Duration.ofMinutes(1).plusSeconds(23).plusMillis(123).toMillis());
        assertThat(QuickPulseDataCollector.parseDurationToMillis("00:12:34.123456"))
            .isEqualTo(Duration.ofMinutes(12).plusSeconds(34).plusMillis(123).toMillis());
        assertThat(QuickPulseDataCollector.parseDurationToMillis("01:23:45.123456"))
            .isEqualTo(Duration.ofHours(1).plusMinutes(23).plusSeconds(45).plusMillis(123).toMillis());
        assertThat(QuickPulseDataCollector.parseDurationToMillis("12:34:56.123456"))
            .isEqualTo(Duration.ofHours(12).plusMinutes(34).plusSeconds(56).plusMillis(123).toMillis());
        assertThat(QuickPulseDataCollector.parseDurationToMillis("1.22:33:44.123456"))
            .isEqualTo(Duration.ofDays(1).plusHours(22).plusMinutes(33).plusSeconds(44).plusMillis(123).toMillis());
        assertThat(QuickPulseDataCollector.parseDurationToMillis("11.22:33:44.123456"))
            .isEqualTo(Duration.ofDays(11).plusHours(22).plusMinutes(33).plusSeconds(44).plusMillis(123).toMillis());
        assertThat(QuickPulseDataCollector.parseDurationToMillis("111.22:33:44.123456"))
            .isEqualTo(Duration.ofDays(111).plusHours(22).plusMinutes(33).plusSeconds(44).plusMillis(123).toMillis());
        assertThat(QuickPulseDataCollector.parseDurationToMillis("1111.22:33:44.123456"))
            .isEqualTo(Duration.ofDays(1111).plusHours(22).plusMinutes(33).plusSeconds(44).plusMillis(123).toMillis());
    }

    private static void assertCountersReset(QuickPulseDataCollector.FinalCounters counters) {
        assertThat(counters).isNotNull();

        assertThat(counters.rdds).isEqualTo(0);
        assertThat(counters.rddsDuration).isEqualTo(0);
        assertThat(counters.unsuccessfulRdds).isEqualTo(0);

        assertThat(counters.requests).isEqualTo(0);
        assertThat(counters.requestsDuration).isEqualTo(0);
        assertThat(counters.unsuccessfulRequests).isEqualTo(0);

        assertThat(counters.exceptions).isEqualTo(0);
    }

    @Test
    void checkDocumentsListSize() {
        QuickPulseDataCollector collector = new QuickPulseDataCollector(true, new QuickPulseConfiguration());

        collector.setQuickPulseStatus(QuickPulseStatus.QP_IS_ON);
        collector.enable(FAKE_CONNECTION_STRING::getInstrumentationKey);

        long duration = 112233L;
        TelemetryItem telemetry = createRequestTelemetry("request-test", new Date(), duration, "200", true);
        telemetry.setConnectionString(FAKE_CONNECTION_STRING);
        for (int i = 0; i < 1005; i++) {
            collector.add(telemetry);
        }
        // check max documentList size
        assertThat(collector.getAndRestart().documentList.size()).isEqualTo(1000);

        collector.setQuickPulseStatus(QuickPulseStatus.QP_IS_OFF);
        for (int i = 0; i < 5; i++) {
            collector.add(telemetry);
        }
        // no telemetry items are added when QP_IS_OFF
        assertThat(collector.getAndRestart().documentList.size()).isEqualTo(0);
    }
}
