// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.test.utils.metrics.TestCounter;
import com.azure.core.test.utils.metrics.TestHistogram;
import com.azure.core.test.utils.metrics.TestMeasurement;
import com.azure.core.test.utils.metrics.TestMeter;
import com.azure.core.util.Context;
import com.azure.core.util.metrics.Meter;
import com.azure.core.util.metrics.MeterProvider;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AmqpMetricsProviderTest {
    private static final Meter DEFAULT_METER = MeterProvider.getDefaultProvider().createMeter("tests", "version", null);
    private static final Symbol TIMEOUT_SYMBOL = Symbol.valueOf(AmqpErrorCode.TIMEOUT_ERROR.toString());
    private static final String NAMESPACE = "namespace";
    private static final String ENTITY_NAME = "name";
    private static final String ENTITY_PATH = "name/and/partition";


    @Test
    public void constructorInvalid() {
        assertThrows(NullPointerException.class, () -> new AmqpMetricsProvider(null, null, null));
        assertThrows(NullPointerException.class, () -> new AmqpMetricsProvider(DEFAULT_METER, null, null));
        assertThrows(NullPointerException.class, () -> new AmqpMetricsProvider(DEFAULT_METER, null, "path"));
    }

    @Test
    public void disabledMeter() {
        TestMeter meter = new TestMeter(false);
        AmqpMetricsProvider provider = new AmqpMetricsProvider(meter, NAMESPACE, null);
        assertDoesNotThrow(() -> provider.recordAddCredits(1));
        assertDoesNotThrow(() -> provider.recordConnectionClosed(null));
        assertDoesNotThrow(() -> provider.recordConnectionInit());
        assertDoesNotThrow(() -> provider.recordSendDelivery(0, DeliveryState.DeliveryStateType.Declared));
        assertDoesNotThrow(() -> provider.recordReceivedMessage());
        assertDoesNotThrow(() -> provider.recordLinkError(new ErrorCondition(TIMEOUT_SYMBOL, "")));
        assertDoesNotThrow(() -> provider.recordSessionError(new ErrorCondition(TIMEOUT_SYMBOL, "")));

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
        assertDoesNotThrow(() -> provider.recordConnectionInit());
        assertDoesNotThrow(() -> provider.recordSendDelivery(0, DeliveryState.DeliveryStateType.Declared));
        assertDoesNotThrow(() -> provider.recordReceivedMessage());
        assertDoesNotThrow(() -> provider.recordLinkError(new ErrorCondition(TIMEOUT_SYMBOL, "")));
        assertDoesNotThrow(() -> provider.recordSessionError(new ErrorCondition(TIMEOUT_SYMBOL, "")));
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
    public void sendDuration() {
        TestMeter meter = new TestMeter();
        AmqpMetricsProvider provider = new AmqpMetricsProvider(meter, NAMESPACE, ENTITY_NAME);

        long start = Instant.now().toEpochMilli() - 100;
        provider.recordSendDelivery(start, DeliveryState.DeliveryStateType.Rejected);
        long end = Instant.now().toEpochMilli();

        provider.recordSendDelivery(start, DeliveryState.DeliveryStateType.Accepted);
        provider.recordSendDelivery(start, null);

        assertTrue(meter.getHistograms().containsKey("messaging.az.amqp.producer.send.duration"));
        TestHistogram histogram = meter.getHistograms().get("messaging.az.amqp.producer.send.duration");

        assertEquals(3, histogram.getMeasurements().size());

        TestMeasurement<Double> measurement1 = histogram.getMeasurements().get(0);
        assertEquals(Context.NONE, measurement1.getContext());
        assertCommonAttributes(measurement1.getAttributes(), NAMESPACE, ENTITY_NAME, null);
        assertEquals("Rejected", measurement1.getAttributes().get(ClientConstants.AMQP_ERROR_KEY));
        assertTrue(100 <= measurement1.getValue());
        assertTrue(end - start >= measurement1.getValue());

        TestMeasurement<Double> measurement2 = histogram.getMeasurements().get(1);
        assertEquals("Accepted", measurement2.getAttributes().get(ClientConstants.AMQP_ERROR_KEY));

        TestMeasurement<Double> measurement3 = histogram.getMeasurements().get(2);
        assertEquals("unknown_error", measurement3.getAttributes().get(ClientConstants.AMQP_ERROR_KEY));
    }

    @Test
    public void initCloseConnection() {
        TestMeter meter = new TestMeter();
        AmqpMetricsProvider provider = new AmqpMetricsProvider(meter, NAMESPACE, null);

        provider.recordConnectionInit();
        provider.recordConnectionInit();
        provider.recordConnectionInit();
        provider.recordConnectionClosed(null);
        provider.recordConnectionClosed(new ErrorCondition(TIMEOUT_SYMBOL, ""));

        assertTrue(meter.getCounters().containsKey("messaging.az.amqp.client.connections.closed"));
        TestCounter closedCounter = meter.getCounters().get("messaging.az.amqp.client.connections.closed");

        assertTrue(meter.getUpDownCounters().containsKey("messaging.az.amqp.client.connections.usage"));
        TestCounter activeCounter = meter.getUpDownCounters().get("messaging.az.amqp.client.connections.usage");

        assertEquals(5, activeCounter.getMeasurements().size());
        assertEquals(2, closedCounter.getMeasurements().size());

        TestMeasurement<Long> active1 = activeCounter.getMeasurements().get(0);
        assertEquals(1, active1.getValue());
        assertEquals(Context.NONE, active1.getContext());
        assertCommonAttributes(active1.getAttributes(), NAMESPACE, null, null);

        assertEquals(1, activeCounter.getMeasurements().get(1).getValue());
        assertEquals(1, activeCounter.getMeasurements().get(2).getValue());
        assertEquals(-1, activeCounter.getMeasurements().get(3).getValue());
        assertEquals(-1, activeCounter.getMeasurements().get(4).getValue());

        TestMeasurement<Long> closed1 = closedCounter.getMeasurements().get(0);
        assertEquals("ok", closed1.getAttributes().get(ClientConstants.AMQP_ERROR_KEY));

        TestMeasurement<Long> closed2 = closedCounter.getMeasurements().get(1);
        assertEquals("com.microsoft:timeout", closed2.getAttributes().get(ClientConstants.AMQP_ERROR_KEY));
    }

    @Test
    public void receivedMessage() {
        TestMeter meter = new TestMeter();
        AmqpMetricsProvider provider = new AmqpMetricsProvider(meter, NAMESPACE, ENTITY_PATH);

        provider.recordReceivedMessage();
        provider.recordReceivedMessage();

        assertTrue(meter.getCounters().containsKey("messaging.az.amqp.consumer.messages.received"));
        TestCounter counter = meter.getCounters().get("messaging.az.amqp.consumer.messages.received");

        assertEquals(2, counter.getMeasurements().size());
        TestMeasurement<Long> measurement1 = counter.getMeasurements().get(0);
        assertEquals(1, measurement1.getValue());
        assertEquals(Context.NONE, measurement1.getContext());
        assertCommonAttributes(measurement1.getAttributes(), NAMESPACE, ENTITY_NAME, ENTITY_PATH);

        assertEquals(1, counter.getMeasurements().get(1).getValue());
    }

    @Test
    public void linkErrors() {
        TestMeter meter = new TestMeter();
        AmqpMetricsProvider provider = new AmqpMetricsProvider(meter, NAMESPACE, ENTITY_PATH);

        provider.recordLinkError(null);
        provider.recordLinkError(new ErrorCondition(TIMEOUT_SYMBOL, ""));

        assertTrue(meter.getCounters().containsKey("messaging.az.amqp.client.link.errors"));
        TestCounter counter = meter.getCounters().get("messaging.az.amqp.client.link.errors");

        assertEquals(1, counter.getMeasurements().size());
        TestMeasurement<Long> measurement1 = counter.getMeasurements().get(0);
        assertEquals(1, measurement1.getValue());
        assertEquals(Context.NONE, measurement1.getContext());
        assertCommonAttributes(measurement1.getAttributes(), NAMESPACE, ENTITY_NAME, ENTITY_PATH);
        assertEquals("com.microsoft:timeout", measurement1.getAttributes().get(ClientConstants.AMQP_ERROR_KEY));
    }

    @Test
    public void sessionErrors() {
        TestMeter meter = new TestMeter();
        AmqpMetricsProvider provider = new AmqpMetricsProvider(meter, NAMESPACE, ENTITY_PATH);

        provider.recordSessionError(null);
        provider.recordSessionError(new ErrorCondition(TIMEOUT_SYMBOL, ""));

        assertTrue(meter.getCounters().containsKey("messaging.az.amqp.client.session.errors"));
        TestCounter counter = meter.getCounters().get("messaging.az.amqp.client.session.errors");

        assertEquals(1, counter.getMeasurements().size());
        TestMeasurement<Long> measurement1 = counter.getMeasurements().get(0);
        assertEquals(1, measurement1.getValue());
        assertEquals(Context.NONE, measurement1.getContext());
        assertCommonAttributes(measurement1.getAttributes(), NAMESPACE, ENTITY_NAME, ENTITY_PATH);
        assertEquals("com.microsoft:timeout", measurement1.getAttributes().get(ClientConstants.AMQP_ERROR_KEY));
    }

    public void assertCommonAttributes(Map<String, Object> actual, String expectedNamespace, String expectedEntityName, String expectedEntityPath) {
        assertEquals(expectedNamespace, actual.get(ClientConstants.HOSTNAME_KEY));
        if (expectedEntityName != null) {
            assertEquals(expectedEntityName, actual.get(ClientConstants.ENTITY_NAME_KEY));
        }
        if (expectedEntityPath != null) {
            assertEquals(expectedEntityPath, actual.get(ClientConstants.ENTITY_PATH_KEY));
        }
    }
}
