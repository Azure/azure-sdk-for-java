// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.util.Context;
import com.azure.core.util.MetricsOptions;
import com.azure.core.util.TelemetryAttributes;
import com.azure.core.util.metrics.DoubleHistogram;
import com.azure.core.util.metrics.LongCounter;
import com.azure.core.util.metrics.Meter;
import com.azure.core.util.metrics.MeterProvider;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Helper class responsible for efficient reporting metrics in AMQP core. It's efficient and safe to use when there is no
 * meter configured by client SDK when metrics are disabled.
 */
public class AmqpMetricsProvider {
    private final boolean isEnabled;
    private DoubleHistogram sendDuration = null;
    private LongCounter activeConnections = null;
    private LongCounter closedConnections = null;
    private LongCounter sessionErrors = null;
    private LongCounter linkErrors = null;
    private LongCounter receivedMessages = null;
    private LongCounter addCredits = null;
    private AttributeCache sendDeliveryAttributeCache = null;
    private AttributeCache amqpErrorAttributeCache = null;
    private TelemetryAttributes commonAttributes = null;
    private static final Meter DEFAULT_METER = MeterProvider.getDefaultProvider().createMeter("azure-core-amqp", "xxx", new MetricsOptions());
    private static final AmqpMetricsProvider NOOP = new AmqpMetricsProvider();

    private AmqpMetricsProvider() {
        isEnabled = false;
    }

    public AmqpMetricsProvider(Meter meter, String namespace, String entityPath) {
        if (meter == null) {
            meter = DEFAULT_METER;
        }

        isEnabled = meter.isEnabled();
        if (isEnabled) {
            Map<String, Object> commonAttributesMap = new HashMap<>();
            commonAttributesMap.put("net.peer.name", namespace);

            if (entityPath != null) {
                int entityNameEnd = entityPath.indexOf('/');
                if (entityNameEnd > 0) {
                    commonAttributesMap.put("entity_name",  entityPath.substring(0, entityNameEnd));
                    commonAttributesMap.put("entity_path", entityPath);
                } else {
                    commonAttributesMap.put("entity_name",  entityPath);
                }
            }

            this.commonAttributes = meter.createAttributes(commonAttributesMap);
            this.sendDeliveryAttributeCache = new AttributeCache(meter, "status", commonAttributesMap);
            this.amqpErrorAttributeCache = new AttributeCache(meter, "status", commonAttributesMap);
            this.sendDuration = meter.createDoubleHistogram("messaging.az.amqp.send.duration", "Send AMQP message duration", "ms");
            this.activeConnections = meter.createLongUpDownCounter("messaging.az.amqp.connections.active", "Active connections", null);
            this.closedConnections = meter.createLongCounter("messaging.az.amqp.connections.closed", "Closed connections", null);
            this.sessionErrors = meter.createLongCounter("messaging.az.amqp.session.errors", "AMQP session errors", null);
            this.linkErrors = meter.createLongCounter("messaging.az.amqp.link.errors", "AMQP link errors", null);
            this.receivedMessages = meter.createLongCounter("messaging.az.amqp.messages.received", "Number of received messages", null);
            this.addCredits = meter.createLongCounter("messaging.az.amqp.credit.requested", "Number of requested credits", null);
        }
    }

    public static AmqpMetricsProvider noop() {
        return NOOP;
    }

    /**
     * Records duration of AMQP send call.
     */
    public void recordSendDelivery(long start, DeliveryState.DeliveryStateType deliveryState) {
        if (isEnabled && sendDuration.isEnabled()) {
            String deliveryStateStr = deliveryState != null ? deliveryState.toString() : "other";
            TelemetryAttributes attributes = sendDeliveryAttributeCache.getOrCreate(deliveryStateStr);
            sendDuration.record(Instant.now().toEpochMilli() - start, attributes, Context.NONE);
        }
    }

    /**
     * Records connection init.
     */
    public void recordConnectionInit() {
        if (isEnabled && activeConnections.isEnabled()) {
            activeConnections.add(1, commonAttributes, Context.NONE);
        }
    }

    /**
     * Records connection close.
     */
    public void recordConnectionClosed(ErrorCondition condition) {
        if (isEnabled) {
            if (activeConnections.isEnabled()) {
                activeConnections.add(-1, commonAttributes, Context.NONE);
            }

            if (closedConnections.isEnabled()) {
                String conditionStr = condition != null ? condition.toString() : "OK";
                closedConnections.add(1, amqpErrorAttributeCache.getOrCreate(conditionStr), Context.NONE);
            }
        }
    }

    /**
     * Records the message was received.
     */
    public void recordReceivedMessage() {
        if (isEnabled && receivedMessages.isEnabled()) {
            receivedMessages.add(1, commonAttributes, Context.NONE);
        }
    }

    /**
     * Records that credits were added to link
     */
    public void recordAddCredits(int credits) {
        if (isEnabled && addCredits.isEnabled()) {
            addCredits.add(credits, commonAttributes, Context.NONE);
        }
    }

    /**
     * Records link error. Noop if condition is null (no error).
     */
    public void recordLinkError(ErrorCondition condition) {
        if (isEnabled && linkErrors.isEnabled() && condition != null && condition.getCondition() != null) {
            linkErrors.add(1,
                amqpErrorAttributeCache.getOrCreate(condition.getCondition().toString()),
                Context.NONE);
        }
    }

    /**
     * Records session error. Noop if condition is null (no error).
     */
    public void recordSessionError(ErrorCondition condition) {
        if (isEnabled && sessionErrors.isEnabled() && condition != null && condition.getCondition() != null) {
            sessionErrors.add(1,
                amqpErrorAttributeCache.getOrCreate(condition.getCondition().toString()),
                Context.NONE);
        }
    }

    private static class AttributeCache {
        private final Map<String, TelemetryAttributes> attr;
        private final Map<String, Object> commonAttributesMap;
        private final String dimensionName;
        private final Meter meter;

        AttributeCache(Meter meter, String dimensionName, Map<String, Object> commonAttributesMap) {
            this.attr = new ConcurrentHashMap<>();
            this.meter = meter;
            this.commonAttributesMap = commonAttributesMap;
            this.dimensionName = dimensionName;
        }

        public TelemetryAttributes getOrCreate(String value) {
            return attr.computeIfAbsent(value, v ->  {
                Map<String, Object> attributes = new HashMap<>(commonAttributesMap);
                attributes.put(dimensionName, value);
                return meter.createAttributes(attributes);
            });
        }
    }
}
