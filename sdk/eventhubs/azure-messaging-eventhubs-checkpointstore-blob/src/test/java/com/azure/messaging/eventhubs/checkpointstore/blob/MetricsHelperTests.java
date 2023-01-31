// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.checkpointstore.blob;

import com.azure.core.test.utils.metrics.TestGauge;
import com.azure.core.test.utils.metrics.TestHistogram;
import com.azure.core.test.utils.metrics.TestMeasurement;
import com.azure.core.test.utils.metrics.TestMeter;
import com.azure.core.util.MetricsOptions;
import com.azure.core.util.metrics.Meter;
import com.azure.messaging.eventhubs.models.Checkpoint;
import com.azure.storage.blob.models.BlobItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Execution(ExecutionMode.SAME_THREAD)
@Isolated
public class MetricsHelperTests {
    private static final int MAX_ATTRIBUTES_SETS = 100;

    @Test
    public void testUpdateDisabledMetrics() {
        Checkpoint checkpoint = new Checkpoint()
            .setFullyQualifiedNamespace("ns")
            .setEventHubName("eh")
            .setConsumerGroup("cg")
            .setPartitionId("0")
            .setSequenceNumber(2L)
            .setOffset(100L);

        Meter meter = mock(Meter.class);
        when(meter.isEnabled()).thenReturn(false);

        TestMeterProvider testProvider = new TestMeterProvider((lib, ver, opts) -> {
            assertEquals("azure-messaging-eventhubs-checkpointstore-blob", lib);
            assertNotNull(ver);
            return meter;
        });


        MetricsHelper helper = new MetricsHelper(new MetricsOptions(), testProvider);
        helper.reportCheckpoint(checkpoint, "ns/eh/ch/0", true, null);

        verify(meter, atLeastOnce()).isEnabled();
        verify(meter, never()).createAttributes(anyMap());
        verify(meter, never()).createLongGauge(any(), any(), any());
        verify(meter, never()).createDoubleHistogram(any(), any(), any());
    }

    @Test
    public void testUpdateDisabledMetricsViaOptions() {
        Checkpoint checkpoint = new Checkpoint()
            .setFullyQualifiedNamespace("ns")
            .setEventHubName("eh")
            .setConsumerGroup("cg")
            .setPartitionId("0")
            .setSequenceNumber(2L)
            .setOffset(100L);

        Meter meter = mock(Meter.class);
        TestMeterProvider testProvider = new TestMeterProvider((lib, ver, opts) -> meter);
        MetricsHelper helper = new MetricsHelper(new MetricsOptions().setEnabled(false), testProvider);
        helper.reportCheckpoint(checkpoint, "ns/eh/cg/0", true, null);

        verify(meter, never()).createAttributes(anyMap());
        verify(meter, never()).createLongGauge(any(), any(), any());
        verify(meter, never()).createDoubleHistogram(any(), any(), any());
    }

    @Test
    public void testUpdateEnabledMetrics() {
        Checkpoint checkpoint = new Checkpoint()
            .setFullyQualifiedNamespace("ns")
            .setEventHubName("eh")
            .setConsumerGroup("cg")
            .setPartitionId("0")
            .setSequenceNumber(2L)
            .setOffset(100L);

        TestMeter meter = new TestMeter();
        TestMeterProvider testProvider = new TestMeterProvider((lib, ver, opts) -> {
            assertEquals("azure-messaging-eventhubs-checkpointstore-blob", lib);
            assertNotNull(ver);
            return meter;
        });

        MetricsHelper helper = new MetricsHelper(new MetricsOptions(), testProvider);
        Instant startTime = Instant.now().minus(10, ChronoUnit.SECONDS);
        helper.reportCheckpoint(checkpoint, "ns/eh/cg/0", true, startTime);

        assertTrue(meter.getGauges().containsKey("messaging.eventhubs.checkpoint.sequence_number"));
        TestGauge seqNo = meter.getGauges().get("messaging.eventhubs.checkpoint.sequence_number");
        assertEquals(1, seqNo.getSubscriptions().size());
        TestGauge.Subscription subs = seqNo.getSubscriptions().get(0);

        assertEquals(0, subs.getMeasurements().size());
        subs.measure();

        TestMeasurement<Long> seqNoMeasurement = subs.getMeasurements().get(0);
        assertEquals(2L, seqNoMeasurement.getValue());
        assertCommonAttributes(checkpoint, seqNoMeasurement.getAttributes());

        assertTrue(meter.getHistograms().containsKey("messaging.eventhubs.checkpoint.duration"));
        TestHistogram checkpointDuration = meter.getHistograms().get("messaging.eventhubs.checkpoint.duration");
        assertEquals(1, checkpointDuration.getMeasurements().size());
        TestMeasurement<Double> durationMeasurements = checkpointDuration.getMeasurements().get(0);
        assertEquals(10000d, durationMeasurements.getValue(), 1000d);
        assertStatusAttributes(checkpoint, "ok", durationMeasurements.getAttributes());
    }

    @Test
    public void testUpdateEnabledMetricsFailure() {
        Checkpoint checkpoint = new Checkpoint()
            .setFullyQualifiedNamespace("ns")
            .setEventHubName("eh")
            .setConsumerGroup("cg")
            .setPartitionId("0")
            .setSequenceNumber(2L)
            .setOffset(100L);

        TestMeter meter = new TestMeter();
        Instant startTime = Instant.now().minus(1, ChronoUnit.SECONDS);
        MetricsHelper helper = new MetricsHelper(new MetricsOptions(), new TestMeterProvider((lib, ver, opts) -> meter));
        helper.reportCheckpoint(checkpoint, "ns/eh/cg/0", false, startTime);

        // sequence number is only reported for successful checkpoints
        assertEquals(0, meter.getGauges().get("messaging.eventhubs.checkpoint.sequence_number").getSubscriptions().size());

        TestHistogram checkpointDuration = meter.getHistograms().get("messaging.eventhubs.checkpoint.duration");
        TestMeasurement<Double> durationMeasurements = checkpointDuration.getMeasurements().get(0);
        assertEquals(1000d, durationMeasurements.getValue(), 1000d);
        assertStatusAttributes(checkpoint, "error", durationMeasurements.getAttributes());
    }

    @Test
    public void testUpdateEnabledMetricsNullSeqNo() {
        Checkpoint checkpoint = new Checkpoint()
            .setFullyQualifiedNamespace("ns")
            .setEventHubName("eh")
            .setConsumerGroup("cg")
            .setPartitionId("0")
            .setOffset(100L);

        TestMeter meter = new TestMeter();
        Instant startTime = Instant.now();
        MetricsHelper helper = new MetricsHelper(new MetricsOptions(), new TestMeterProvider((lib, ver, opts) -> meter));
        helper.reportCheckpoint(checkpoint, "ns/eh/cg/0", true, startTime);

        assertEquals(0, meter.getGauges().get("messaging.eventhubs.checkpoint.sequence_number").getSubscriptions().size());

        TestHistogram checkpointDuration = meter.getHistograms().get("messaging.eventhubs.checkpoint.duration");
        TestMeasurement<Double> durationMeasurements = checkpointDuration.getMeasurements().get(0);
        assertEquals(0d, durationMeasurements.getValue(), 1000d);
        assertStatusAttributes(checkpoint, "ok", durationMeasurements.getAttributes());
    }

    @Test
    public void testUpdateEnabledMetricsTooManyAttributes() {
        TestMeter meter = new TestMeter();
        List<Checkpoint> checkpoints = IntStream.range(0, MAX_ATTRIBUTES_SETS + 10)
            .mapToObj(n -> new Checkpoint()
                    .setFullyQualifiedNamespace("ns")
                    .setEventHubName("eh")
                    .setConsumerGroup("cg")
                    .setPartitionId(String.valueOf(n))
                    .setSequenceNumber((long) n)
                    .setOffset(100L))
            .collect(Collectors.toList());

        MetricsHelper helper = new MetricsHelper(new MetricsOptions(), new TestMeterProvider((lib, ver, opts) -> meter));
        checkpoints.forEach(ch -> helper.reportCheckpoint(ch, "ns/eh/cg/" + ch.getPartitionId(), true, Instant.now()));

        List<TestGauge.Subscription> subscriptions = meter.getGauges().get("messaging.eventhubs.checkpoint.sequence_number").getSubscriptions();
        assertEquals(MAX_ATTRIBUTES_SETS, subscriptions.size());
        subscriptions.forEach(subs -> subs.measure());

        final int[] i = {0};
        subscriptions.forEach(subs -> {
            assertEquals(1, subs.getMeasurements().size());
            TestMeasurement<Long> seqNoMeasurement = subs.getMeasurements().get(0);
            assertEquals(i[0], seqNoMeasurement.getValue());
            assertCommonAttributes(checkpoints.get(i[0]), seqNoMeasurement.getAttributes());
            i[0]++;
        });

        TestHistogram durationHistogram = meter.getHistograms().get("messaging.eventhubs.checkpoint.duration");
        assertEquals(MAX_ATTRIBUTES_SETS, durationHistogram.getMeasurements().size());

        final int[] k = {0};
        durationHistogram.getMeasurements().forEach(m -> {
            assertEquals(0d, m.getValue(), 1000d);
            assertStatusAttributes(checkpoints.get(k[0]), "ok", m.getAttributes());
            k[0]++;
        });
    }

    @Test
    public void testUpdateEnabledMetricsMultipleMeasurements() {
        Checkpoint checkpoint1 = new Checkpoint()
            .setFullyQualifiedNamespace("ns")
            .setEventHubName("eh")
            .setConsumerGroup("cg")
            .setPartitionId("0")
            .setSequenceNumber(2L)
            .setOffset(100L);

        Checkpoint checkpoint2 = new Checkpoint()
            .setFullyQualifiedNamespace("ns")
            .setEventHubName("eh")
            .setConsumerGroup("cg")
            .setPartitionId("0")
            .setSequenceNumber(42L)
            .setOffset(100L);

        TestMeter meter = new TestMeter();
        MetricsHelper helper = new MetricsHelper(new MetricsOptions(), new TestMeterProvider((lib, ver, opts) -> meter));
        helper.reportCheckpoint(checkpoint1, "ns/eh/cg/0", true, Instant.now().minus(10, ChronoUnit.SECONDS));
        helper.reportCheckpoint(checkpoint2, "ns/eh/cg/0", true, Instant.now());

        TestGauge seqNo = meter.getGauges().get("messaging.eventhubs.checkpoint.sequence_number");
        TestGauge.Subscription subs = seqNo.getSubscriptions().get(0);
        subs.measure();

        TestMeasurement<Long> seqNoMeasurement = subs.getMeasurements().get(0);
        assertEquals(42L, seqNoMeasurement.getValue());

        TestHistogram duration = meter.getHistograms().get("messaging.eventhubs.checkpoint.duration");
        assertEquals(2, duration.getMeasurements().size());

        assertEquals(10000d, duration.getMeasurements().get(0).getValue(), 1000d);
        assertEquals(0d, duration.getMeasurements().get(1).getValue(), 1000d);
        assertStatusAttributes(checkpoint2, "ok", duration.getMeasurements().get(1).getAttributes());
    }

    @Test
    public void testUpdateEnabledMetricsMultipleHubs() {
        Checkpoint checkpoint1 = new Checkpoint()
            .setFullyQualifiedNamespace("ns")
            .setEventHubName("eh1")
            .setConsumerGroup("cg")
            .setPartitionId("0")
            .setSequenceNumber(2L)
            .setOffset(100L);

        Checkpoint checkpoint2 = new Checkpoint()
            .setFullyQualifiedNamespace("ns")
            .setEventHubName("eh2")
            .setConsumerGroup("cg")
            .setPartitionId("0")
            .setSequenceNumber(42L)
            .setOffset(100L);

        TestMeter meter = new TestMeter();
        MetricsHelper helper = new MetricsHelper(new MetricsOptions(), new TestMeterProvider((lib, ver, opts) -> meter));

        helper.reportCheckpoint(checkpoint1, "ns/eh1/cg/0", true, Instant.now());
        helper.reportCheckpoint(checkpoint2, "ns/eh2/cg/0", true, Instant.now());

        TestGauge seqNo = meter.getGauges().get("messaging.eventhubs.checkpoint.sequence_number");
        assertEquals(2, seqNo.getSubscriptions().size());
        TestGauge.Subscription subs1 = seqNo.getSubscriptions().get(0);
        TestGauge.Subscription subs2 = seqNo.getSubscriptions().get(1);
        subs1.measure();
        subs2.measure();

        TestMeasurement<Long> seqNoMeasurement1 = subs1.getMeasurements().get(0);
        assertEquals(2L, seqNoMeasurement1.getValue());
        assertCommonAttributes(checkpoint1, seqNoMeasurement1.getAttributes());

        TestMeasurement<Long> seqNoMeasurement2 = subs2.getMeasurements().get(0);
        assertEquals(42L, seqNoMeasurement2.getValue());
        assertCommonAttributes(checkpoint2, seqNoMeasurement2.getAttributes());

        TestHistogram duration = meter.getHistograms().get("messaging.eventhubs.checkpoint.duration");
        assertEquals(2, duration.getMeasurements().size());

        assertEquals(0d, duration.getMeasurements().get(0).getValue(), 1000d);
        assertStatusAttributes(checkpoint1, "ok", duration.getMeasurements().get(0).getAttributes());
        assertEquals(0d, duration.getMeasurements().get(1).getValue(), 1000d);
        assertStatusAttributes(checkpoint2, "ok", duration.getMeasurements().get(1).getAttributes());
    }


    private void assertStatusAttributes(Checkpoint checkpoint, String expectedStatus, Map<String, Object> attributes) {
        assertEquals(5, attributes.size());
        assertEquals(checkpoint.getFullyQualifiedNamespace(), attributes.get("hostName"));
        assertEquals(checkpoint.getEventHubName(), attributes.get("entityName"));
        assertEquals(checkpoint.getPartitionId(), attributes.get("partitionId"));
        assertEquals(checkpoint.getConsumerGroup(), attributes.get("consumerGroup"));
        assertEquals(expectedStatus, attributes.get("status"));
    }

    private void assertCommonAttributes(Checkpoint checkpoint, Map<String, Object> attributes) {
        assertEquals(4, attributes.size());
        assertEquals(checkpoint.getFullyQualifiedNamespace(), attributes.get("hostName"));
        assertEquals(checkpoint.getEventHubName(), attributes.get("entityName"));
        assertEquals(checkpoint.getPartitionId(), attributes.get("partitionId"));
        assertEquals(checkpoint.getConsumerGroup(), attributes.get("consumerGroup"));
    }

    private BlobItem getCheckpointBlobItem(String offset, String sequenceNumber, String blobName) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("sequencenumber", sequenceNumber);
        metadata.put("offset", offset);
        return new BlobItem()
            .setName(blobName)
            .setMetadata(metadata);
    }
}
