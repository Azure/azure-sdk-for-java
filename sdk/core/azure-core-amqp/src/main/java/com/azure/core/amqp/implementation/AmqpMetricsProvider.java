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
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class AmqpMetricsProvider {
    private static final AmqpMetricsProvider NOOP = new AmqpMetricsProvider(null, null, null, null, false);
    private static final Meter DEFAULT_METER = MeterProvider.getDefaultProvider().createMeter("azure-core-amqp", "xxx", new MetricsOptions());
    private final static ConcurrentHashMap<String, AmqpMetricsProvider> METRICS_CACHE = new ConcurrentHashMap<>();
    private final boolean isEnabled;
    private final DoubleHistogram sendDuration;
    private final LongCounter activeConnections;
    private final LongCounter closedConnections;
    private final LongCounter sessionErrors;
    private final LongCounter linkErrors;
    private final LongCounter receivedMessages;
    private final LongCounter addCredits;
    private final AttributeCache sendDeliveryAttributeCache;
    private final AttributeCache amqpErrorAttributeCache;
    private final TelemetryAttributes commonAttributes;

    private AmqpMetricsProvider(Meter meter, String namespace, String entityName, String entityPath, boolean isEnabled) {
        this.isEnabled = isEnabled;
        if (isEnabled) {
            Objects.requireNonNull(meter, "'meter' cannot be null");

            Map<String, Object> commonAttributesMap = new HashMap<>();
            commonAttributesMap.put("net.peer.name", namespace);
            if (entityName != null) {
                commonAttributesMap.put("entity_name", entityName);
            }
            if (entityPath != null) {
                commonAttributesMap.put("entity_path", entityPath);
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
        } else {
            this.commonAttributes = null;
            this.sendDeliveryAttributeCache = null;
            this.amqpErrorAttributeCache = null;
            this.sendDuration = null;
            this.activeConnections = null;
            this.closedConnections = null;
            this.sessionErrors = null;
            this.linkErrors = null;
            this.receivedMessages = null;
            this.addCredits = null;
        }
    }

    public static AmqpMetricsProvider getOrCreate(Meter meter, String namespace, String entityPath) {
        if (meter == null) {
            meter = DEFAULT_METER;
        }

        if (!meter.isEnabled()) {
            return NOOP;
        }

        Meter finalMeter = meter;

        String meterId = namespace;
        if (entityPath != null) {
            meterId += "/" + entityPath;
        }

        return METRICS_CACHE.computeIfAbsent(meterId, ignored -> {
            String name = null;
            String path = null;
            if (entityPath != null) {
                int entityNameEnd = entityPath.indexOf('/');
                if (entityNameEnd > 0) {
                    name = entityPath.substring(0, entityNameEnd);
                    path = entityPath;
                } else {
                    name = entityPath;
                    path = null;
                }
            }

            return new AmqpMetricsProvider(finalMeter, namespace, name, path, finalMeter.isEnabled());
        });
    }

    public boolean isSendDurationEnabled() {
        return isEnabled && sendDuration.isEnabled();
    }

    public void recordSendDelivery(long start, DeliveryState.DeliveryStateType deliveryState) {
        if (isEnabled && sendDuration.isEnabled()) {
            String deliveryStateStr = deliveryState != null ? deliveryState.toString() : "other";
            TelemetryAttributes attributes = sendDeliveryAttributeCache.getOrCreate(deliveryStateStr);
            sendDuration.record(Instant.now().toEpochMilli() - start, attributes, Context.NONE);
        }
    }

    public void recordConnectionInit() {
        if (isEnabled && activeConnections.isEnabled()) {
            activeConnections.add(1, commonAttributes, Context.NONE);
        }
    }

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

    public void recordReceivedMessage() {
        if (isEnabled && receivedMessages.isEnabled()) {
            receivedMessages.add(1, commonAttributes, Context.NONE);
        }
    }

    public void recordAddCredits(int credits) {
        if (isEnabled && addCredits.isEnabled()) {
            addCredits.add(credits, commonAttributes, Context.NONE);
        }
    }

    public void recordLinkError(ErrorCondition condition) {
        if (isEnabled && linkErrors.isEnabled() && condition != null) {
            linkErrors.add(1,
                amqpErrorAttributeCache.getOrCreate(condition.toString()),
                Context.NONE);
        }
    }

    public void recordSessionError(ErrorCondition condition) {
        if (isEnabled && sessionErrors.isEnabled() && condition != null) {
            sessionErrors.add(1,
                amqpErrorAttributeCache.getOrCreate(condition.toString()),
                Context.NONE);
        }
    }

    private static class AttributeCache {
        private final Map<String, TelemetryAttributes> attr;
        private final Map<String, Object>  commonAttributesMap;
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
