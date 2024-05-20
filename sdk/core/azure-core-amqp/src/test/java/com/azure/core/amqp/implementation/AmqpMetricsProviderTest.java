// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.exception.AmqpResponseCode;
import com.azure.core.test.utils.metrics.TestCounter;
import com.azure.core.test.utils.metrics.TestGauge;
import com.azure.core.test.utils.metrics.TestHistogram;
import com.azure.core.test.utils.metrics.TestMeasurement;
import com.azure.core.test.utils.metrics.TestMeter;
import com.azure.core.util.Context;
import com.azure.core.util.metrics.Meter;
import com.azure.core.util.metrics.MeterProvider;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AmqpMetricsProviderTest {
    private static final Meter DEFAULT_METER = MeterProvider.getDefaultProvider().createMeter("tests", "version", null);
    private static final Symbol TIMEOUT_SYMBOL = Symbol.valueOf(AmqpErrorCode.TIMEOUT_ERROR.toString());
    private static final String NAMESPACE = "namespace";
    private static final String ENTITY_NAME = "name";
    private static final String ENTITY_PATH = "name/and/partition";

    @Test
    public void nullNamespaceIsOk() {
        assertDoesNotThrow(() -> new AmqpMetricsProvider(null, null, null));
        assertDoesNotThrow(() -> new AmqpMetricsProvider(DEFAULT_METER, null, null));
        assertDoesNotThrow(() -> new AmqpMetricsProvider(DEFAULT_METER, null, "path"));
    }

    @Test
    public void disabledMeter() {
        TestMeter meter = new TestMeter(false);
        AmqpMetricsProvider provider = new AmqpMetricsProvider(meter, NAMESPACE, null);
        assertDoesNotThrow(() -> provider.recordAddCredits(1));
        assertDoesNotThrow(() -> provider.recordConnectionClosed(null));
        assertDoesNotThrow(() -> provider.recordRequestResponseDuration(0, "foo", AmqpResponseCode.OK));
        assertDoesNotThrow(() -> provider.recordRequestResponseDuration(0, null, AmqpResponseCode.OK));
        assertDoesNotThrow(() -> provider.recordRequestResponseDuration(0, "foo", null));
        assertDoesNotThrow(() -> provider.recordSend(0, DeliveryState.DeliveryStateType.Accepted));
        assertDoesNotThrow(() -> provider.recordSend(0, null));
        assertDoesNotThrow(() -> provider.trackPrefetchSequenceNumber(() -> Long.valueOf(1)));
        assertDoesNotThrow(() -> provider.recordHandlerError(AmqpMetricsProvider.ErrorSource.TRANSPORT,
            new ErrorCondition(TIMEOUT_SYMBOL, "")));
        assertDoesNotThrow(() -> provider.recordHandlerError(null, new ErrorCondition(TIMEOUT_SYMBOL, "")));

        assertEquals(0, meter.getCounters().size());
        assertEquals(0, meter.getHistograms().size());
    }

    @Test
    public void defaultMeter() {
        // sanity check
        assertFalse(DEFAULT_METER.isEnabled());

        AmqpMetricsProvider provider = new AmqpMetricsProvider(null, NAMESPACE, null);
        assertDoesNotThrow(() -> provider.recordAddCredits(1));
        assertDoesNotThrow(() -> provider.recordConnectionClosed(null));
        assertDoesNotThrow(() -> provider.recordRequestResponseDuration(0, "foo", AmqpResponseCode.OK));
        assertDoesNotThrow(() -> provider.recordRequestResponseDuration(0, null, AmqpResponseCode.OK));
        assertDoesNotThrow(() -> provider.recordRequestResponseDuration(0, "foo", null));
        assertDoesNotThrow(() -> provider.recordSend(0, DeliveryState.DeliveryStateType.Accepted));
        assertDoesNotThrow(() -> provider.recordSend(0, null));
        assertDoesNotThrow(() -> provider.trackPrefetchSequenceNumber(() -> Long.valueOf(1)));
        assertDoesNotThrow(() -> provider.recordHandlerError(AmqpMetricsProvider.ErrorSource.LINK,
            new ErrorCondition(TIMEOUT_SYMBOL, "")));
        assertDoesNotThrow(() -> provider.recordHandlerError(null, new ErrorCondition(TIMEOUT_SYMBOL, "")));
    }

    @Test
    public void addCredits() {
        TestMeter meter = new TestMeter();
        AmqpMetricsProvider provider = new AmqpMetricsProvider(meter, NAMESPACE, ENTITY_PATH);
        provider.recordAddCredits(1);
        provider.recordAddCredits(2);
        provider.recordAddCredits(100);

        assertTrue(meter.getCounters().containsKey("messaging.az.amqp.consumer.credits.requested"));
        TestCounter counter = meter.getCounters().get("messaging.az.amqp.consumer.credits.requested");

        assertEquals(3, counter.getMeasurements().size());
        TestMeasurement<Long> measurement1 = counter.getMeasurements().get(0);
        assertEquals(1, measurement1.getValue());
        assertEquals(Context.NONE, measurement1.getContext());
        assertCommonAttributes(measurement1.getAttributes(), NAMESPACE, ENTITY_NAME, ENTITY_PATH);

        TestMeasurement<Long> measurement2 = counter.getMeasurements().get(1);
        assertEquals(2, measurement2.getValue());
        assertEquals(Context.NONE, measurement2.getContext());
        assertCommonAttributes(measurement2.getAttributes(), NAMESPACE, ENTITY_NAME, ENTITY_PATH);

        TestMeasurement<Long> measurement3 = counter.getMeasurements().get(2);
        assertEquals(100, measurement3.getValue());
        assertEquals(Context.NONE, measurement3.getContext());
        assertCommonAttributes(measurement3.getAttributes(), NAMESPACE, ENTITY_NAME, ENTITY_PATH);
    }

    @Test
    public void requestResponseDuration() {
        TestMeter meter = new TestMeter();
        AmqpMetricsProvider provider = new AmqpMetricsProvider(meter, NAMESPACE, ENTITY_NAME);

        long start = Instant.now().toEpochMilli() - 100;
        provider.recordRequestResponseDuration(start, "peek", AmqpResponseCode.FORBIDDEN);
        long end = Instant.now().toEpochMilli();

        provider.recordRequestResponseDuration(start, "schedule", AmqpResponseCode.AMBIGUOUS);
        provider.recordRequestResponseDuration(start, "complete", null);

        assertTrue(meter.getHistograms().containsKey("messaging.az.amqp.management.request.duration"));
        TestHistogram histogram = meter.getHistograms().get("messaging.az.amqp.management.request.duration");

        assertEquals(3, histogram.getMeasurements().size());

        TestMeasurement<Double> measurement1 = histogram.getMeasurements().get(0);
        assertEquals(Context.NONE, measurement1.getContext());
        assertCommonAttributes(measurement1.getAttributes(), NAMESPACE, ENTITY_NAME, null);
        assertEquals("forbidden", measurement1.getAttributes().get(AmqpMetricsProvider.STATUS_CODE_KEY));
        assertEquals("peek", measurement1.getAttributes().get(ClientConstants.OPERATION_NAME_KEY));
        assertTrue(100 <= measurement1.getValue());
        assertTrue(end - start >= measurement1.getValue());

        TestMeasurement<Double> measurement2 = histogram.getMeasurements().get(1);
        assertEquals("ambiguous", measurement2.getAttributes().get(AmqpMetricsProvider.STATUS_CODE_KEY));
        assertEquals("schedule", measurement2.getAttributes().get(ClientConstants.OPERATION_NAME_KEY));

        TestMeasurement<Double> measurement3 = histogram.getMeasurements().get(2);
        assertEquals("error", measurement3.getAttributes().get(AmqpMetricsProvider.STATUS_CODE_KEY));
        assertEquals("complete", measurement3.getAttributes().get(ClientConstants.OPERATION_NAME_KEY));
    }

    @Test
    public void sendDuration() {
        TestMeter meter = new TestMeter();
        AmqpMetricsProvider provider = new AmqpMetricsProvider(meter, NAMESPACE, ENTITY_NAME);

        long start = Instant.now().toEpochMilli() - 100;
        provider.recordSend(start, DeliveryState.DeliveryStateType.Rejected);
        long end = Instant.now().toEpochMilli();

        provider.recordSend(start, DeliveryState.DeliveryStateType.Accepted);
        provider.recordSend(start, null);

        assertTrue(meter.getHistograms().containsKey("messaging.az.amqp.producer.send.duration"));
        TestHistogram histogram = meter.getHistograms().get("messaging.az.amqp.producer.send.duration");

        assertEquals(3, histogram.getMeasurements().size());

        TestMeasurement<Double> measurement1 = histogram.getMeasurements().get(0);
        assertEquals(Context.NONE, measurement1.getContext());
        assertCommonAttributes(measurement1.getAttributes(), NAMESPACE, ENTITY_NAME, null);
        assertEquals("rejected", measurement1.getAttributes().get(ClientConstants.DELIVERY_STATE_KEY));
        assertTrue(100 <= measurement1.getValue());
        assertTrue(end - start >= measurement1.getValue());

        TestMeasurement<Double> measurement2 = histogram.getMeasurements().get(1);
        assertEquals("accepted", measurement2.getAttributes().get(ClientConstants.DELIVERY_STATE_KEY));

        TestMeasurement<Double> measurement3 = histogram.getMeasurements().get(2);
        assertEquals("error", measurement3.getAttributes().get(ClientConstants.DELIVERY_STATE_KEY));
    }

    @Test
    public void initCloseConnection() {
        TestMeter meter = new TestMeter();
        AmqpMetricsProvider provider = new AmqpMetricsProvider(meter, NAMESPACE, null);

        provider.recordConnectionClosed(null);
        provider.recordConnectionClosed(new ErrorCondition(TIMEOUT_SYMBOL, ""));

        assertTrue(meter.getCounters().containsKey("messaging.az.amqp.client.connections.closed"));
        TestCounter closedCounter = meter.getCounters().get("messaging.az.amqp.client.connections.closed");

        assertEquals(2, closedCounter.getMeasurements().size());

        TestMeasurement<Long> closed1 = closedCounter.getMeasurements().get(0);
        assertEquals("ok", closed1.getAttributes().get(ClientConstants.ERROR_CONDITION_KEY));

        TestMeasurement<Long> closed2 = closedCounter.getMeasurements().get(1);
        assertEquals("com.microsoft:timeout", closed2.getAttributes().get(ClientConstants.ERROR_CONDITION_KEY));
    }

    @Test
    @SuppressWarnings("try")
    public void receivedMessage() throws Exception {
        TestMeter meter = new TestMeter();
        AmqpMetricsProvider provider = new AmqpMetricsProvider(meter, NAMESPACE, ENTITY_PATH);

        AtomicLong seqNo = new AtomicLong(0);

        TestGauge gauge;
        try (AutoCloseable subscription = provider.trackPrefetchSequenceNumber(seqNo::get)) {
            assertTrue(meter.getGauges().containsKey("messaging.az.amqp.prefetch.sequence_number"));
            gauge = meter.getGauges().get("messaging.az.amqp.prefetch.sequence_number");
            assertEquals(1, gauge.getSubscriptions().size());
            assertSame(subscription, gauge.getSubscriptions().get(0));
            TestGauge.Subscription testSubscription = (TestGauge.Subscription) subscription;

            testSubscription.measure();
            seqNo.set(1);
            seqNo.set(2);
            testSubscription.measure();
            seqNo.set(42);
            testSubscription.measure();

            assertEquals(3, testSubscription.getMeasurements().size());
            TestMeasurement<Long> measurement1 = testSubscription.getMeasurements().get(0);

            assertEquals(0, measurement1.getValue());
            assertEquals(Context.NONE, measurement1.getContext());
            assertCommonAttributes(measurement1.getAttributes(), NAMESPACE, ENTITY_NAME, ENTITY_PATH);

            assertEquals(2, testSubscription.getMeasurements().get(1).getValue());
            assertEquals(42, testSubscription.getMeasurements().get(2).getValue());
        }

        seqNo.set(-1);
        assertEquals(0, gauge.getSubscriptions().size());
    }

    private Message createMessage(Object seqNo) {
        Message msg = Proton.message();
        MessageAnnotations annotations
            = new MessageAnnotations(Collections.singletonMap(Symbol.valueOf("x-opt-sequence-number"), seqNo));
        msg.setMessageAnnotations(annotations);
        msg.setBody(new AmqpValue("body"));

        return msg;
    }

    @Test
    public void linkErrors() {
        TestMeter meter = new TestMeter();
        AmqpMetricsProvider provider = new AmqpMetricsProvider(meter, NAMESPACE, ENTITY_PATH);

        provider.recordHandlerError(AmqpMetricsProvider.ErrorSource.LINK, null);
        provider.recordHandlerError(AmqpMetricsProvider.ErrorSource.LINK, new ErrorCondition(TIMEOUT_SYMBOL, ""));

        assertTrue(meter.getCounters().containsKey("messaging.az.amqp.client.link.errors"));
        TestCounter counter = meter.getCounters().get("messaging.az.amqp.client.link.errors");

        assertEquals(1, counter.getMeasurements().size());
        TestMeasurement<Long> measurement1 = counter.getMeasurements().get(0);
        assertEquals(1, measurement1.getValue());
        assertEquals(Context.NONE, measurement1.getContext());
        assertCommonAttributes(measurement1.getAttributes(), NAMESPACE, ENTITY_NAME, ENTITY_PATH);
        assertEquals("com.microsoft:timeout", measurement1.getAttributes().get(ClientConstants.ERROR_CONDITION_KEY));
    }

    @Test
    public void sessionErrors() {
        TestMeter meter = new TestMeter();
        AmqpMetricsProvider provider = new AmqpMetricsProvider(meter, NAMESPACE, ENTITY_PATH);

        provider.recordHandlerError(AmqpMetricsProvider.ErrorSource.SESSION, null);
        provider.recordHandlerError(AmqpMetricsProvider.ErrorSource.SESSION, new ErrorCondition(TIMEOUT_SYMBOL, ""));

        assertTrue(meter.getCounters().containsKey("messaging.az.amqp.client.session.errors"));
        TestCounter counter = meter.getCounters().get("messaging.az.amqp.client.session.errors");

        assertEquals(1, counter.getMeasurements().size());
        TestMeasurement<Long> measurement1 = counter.getMeasurements().get(0);
        assertEquals(1, measurement1.getValue());
        assertEquals(Context.NONE, measurement1.getContext());
        assertCommonAttributes(measurement1.getAttributes(), NAMESPACE, ENTITY_NAME, ENTITY_PATH);
        assertEquals("com.microsoft:timeout", measurement1.getAttributes().get(ClientConstants.ERROR_CONDITION_KEY));
    }

    @Test
    public void transportErrors() {
        TestMeter meter = new TestMeter();
        AmqpMetricsProvider provider = new AmqpMetricsProvider(meter, NAMESPACE, ENTITY_PATH);

        provider.recordHandlerError(AmqpMetricsProvider.ErrorSource.TRANSPORT, null);
        provider.recordHandlerError(AmqpMetricsProvider.ErrorSource.TRANSPORT, new ErrorCondition(TIMEOUT_SYMBOL, ""));

        assertTrue(meter.getCounters().containsKey("messaging.az.amqp.client.transport.errors"));
        TestCounter counter = meter.getCounters().get("messaging.az.amqp.client.transport.errors");

        assertEquals(1, counter.getMeasurements().size());
        TestMeasurement<Long> measurement1 = counter.getMeasurements().get(0);
        assertEquals(1, measurement1.getValue());
        assertEquals(Context.NONE, measurement1.getContext());
        assertCommonAttributes(measurement1.getAttributes(), NAMESPACE, ENTITY_NAME, ENTITY_PATH);
        assertEquals("com.microsoft:timeout", measurement1.getAttributes().get(ClientConstants.ERROR_CONDITION_KEY));
    }

    public void assertCommonAttributes(Map<String, Object> actual, String expectedNamespace, String expectedEntityName,
        String expectedEntityPath) {
        assertEquals(expectedNamespace, actual.get(ClientConstants.HOSTNAME_KEY));
        if (expectedEntityName != null) {
            assertEquals(expectedEntityName, actual.get(ClientConstants.ENTITY_NAME_KEY));
        }
        if (expectedEntityPath != null) {
            assertEquals(expectedEntityPath, actual.get(ClientConstants.ENTITY_PATH_KEY));
        }
    }
}
