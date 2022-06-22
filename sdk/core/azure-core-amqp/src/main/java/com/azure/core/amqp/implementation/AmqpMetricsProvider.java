// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.util.AzureAttributeCollection;
import com.azure.core.util.Context;
import com.azure.core.util.metrics.AzureLongCounter;
import com.azure.core.util.metrics.AzureLongHistogram;
import com.azure.core.util.metrics.AzureMeter;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class AmqpMetricsProvider {
    private final AzureMeter meter;
    private final String namespace;
    private final String entityName;
    private final AzureLongCounter sendThroughput;
    private final AzureLongCounter sendQueueSize;
    private final AzureLongHistogram sendDuration;
    private final AzureLongCounter receiveQueueSize;
    private final AzureLongCounter receiveThroughput;
    private final AzureLongCounter activeConnections;
    private final AzureLongCounter closedConnections;

    private final AzureLongCounter sessionErrors;
    private final AzureLongCounter linkErrors;

    private final AttributeCache<DeliveryState.DeliveryStateType> sendDeliveryAttributeCache;
    private final AttributeCache<AmqpErrorCondition> amqpErrorAttributeCache;
    private final AzureAttributeCollection commonAttributes;

    public AmqpMetricsProvider(AzureMeter meter, String namespace, String entityName) {
        Objects.requireNonNull(meter,  "'meter' cannot be null");
        this.meter = meter;
        this.namespace = namespace;
        this.entityName = entityName;
        if (meter.isEnabled()) {
            this.commonAttributes = createAttributes();
            this.sendDeliveryAttributeCache = new AttributeCache<>("status", "other", this::createAttributes);
            this.amqpErrorAttributeCache = new AttributeCache<>("status", "OK", this::createAttributes);
            this.sendDuration = meter.createLongHistogram("messaging.az.amqp.send.duration", "Send AMQP message duration", "ms");
            this.sendThroughput = meter.createLongCounter("messaging.az.amqp.send.bytes", "Outgoing throughput", "bytes");
            this.receiveThroughput = meter.createLongCounter("messaging.az.amqp.receive.bytes", "Incoming throughput", "bytes");
            this.sendQueueSize = meter.createLongUpDownCounter("messaging.az.amqp.send.queue.size", "Count of pending messages in send queue", null);
            this.receiveQueueSize = meter.createLongUpDownCounter("messaging.az.amqp.receive.queue.size", "Count of pending messages in receive queue", null);
            this.activeConnections = meter.createLongUpDownCounter("messaging.az.amqp.connections.active", "Active connections", null);
            this.closedConnections = meter.createLongCounter("messaging.az.amqp.connections.closed", "Closed connections", null);
            this.sessionErrors = meter.createLongCounter("messaging.az.amqp.session.errors", "AMQP session errors", null);
            this.linkErrors = meter.createLongCounter("messaging.az.amqp.link.errors", "AMQP link errors", null);
        } else {
            this.commonAttributes = null;
            this.sendDeliveryAttributeCache = null;
            this.amqpErrorAttributeCache = null;
            this.sendDuration = null;
            this.sendThroughput = null;
            this.receiveThroughput = null;
            this.sendQueueSize = null;
            this.receiveQueueSize = null;
            this.activeConnections = null;
            this.closedConnections = null;
            this.sessionErrors = null;
            this.linkErrors = null;
        }
    }

    public void recordSendQueue(long size) {
        if (!isEnabled()) {
            return;
        }

        sendQueueSize.add(size, commonAttributes, Context.NONE);
    }

    public void recordSendBytes(long bytes) {
        if (!isEnabled()) {
            return;
        }

        sendThroughput.add(bytes, commonAttributes, Context.NONE);
    }

    public void recordReceiveQueue(long size) {
        if (!isEnabled()) {
            return;
        }

        receiveQueueSize.add(size, commonAttributes, Context.NONE);
    }

    public void recordReceiveBytes(long bytes) {
        if (!isEnabled()) {
            return;
        }

        receiveThroughput.add(bytes, commonAttributes, Context.NONE);
     }

    private AzureAttributeCollection createAttributes() {
        return meter.createAttributeBuilder()
            .add("namespace", namespace)
            .add("entity", entityName);
    }

    public void recordSendDelivery(long start, long messageSize, DeliveryState.DeliveryStateType deliveryState) {
        if (!isEnabled()) {
            return;
        }

        AzureAttributeCollection attributes = sendDeliveryAttributeCache.getAttributeBuilder(deliveryState);
        sendDuration.record(Instant.now().toEpochMilli() - start, attributes, Context.NONE);
        sendThroughput.add(messageSize, attributes, Context.NONE);
    }

    public void recordConnectionInit() {
        if (!isEnabled()) {
            return;
        }

        activeConnections.add(1, commonAttributes, Context.NONE);
    }

    public void recordConnectionClosed(ErrorCondition condition) {
        if (!isEnabled()) {
            return;
        }

        activeConnections.add(-1, commonAttributes, Context.NONE);

        AzureAttributeCollection attrs = getCached(condition, amqpErrorAttributeCache.getAttributeBuilderForUnknown());
        closedConnections.add(1, attrs, Context.NONE);
    }

    public void recordLinkError(ErrorCondition condition) {
        if (!isEnabled() || condition == null) {
            return;
        }

        AzureAttributeCollection attrs = getCached(condition, null);
        if (attrs != null) {
            linkErrors.add(1, attrs, Context.NONE);
        }
    }

    public void recordSessionError(ErrorCondition condition) {
        if (!isEnabled() || condition == null) {
            return;
        }

        AzureAttributeCollection attrs = getCached(condition, null);
        if (attrs != null) {
            sessionErrors.add(1, attrs, Context.NONE);
        }
    }

    private AzureAttributeCollection getCached(ErrorCondition condition, AzureAttributeCollection fallback) {
        Symbol errorCondition = condition != null ? condition.getCondition() : null;
        if (errorCondition != null) {
            return amqpErrorAttributeCache
                .getAttributeBuilder(AmqpErrorCondition.fromString(errorCondition.toString()));
        }

        return fallback;
    }

    public boolean isEnabled() {
        return meter.isEnabled();
    }

    private static class AttributeCache<T extends Enum<T>> {
        private final AzureAttributeCollection defaultAttr;
        private final Map<T, AzureAttributeCollection> attr;
        private final Supplier<AzureAttributeCollection> commonAttributesSupplier;
        private final String dimensionName;

        public AttributeCache(String dimensionName, String unknownValue, Supplier<AzureAttributeCollection> commonAttributesSupplier) {
            this.attr = new ConcurrentHashMap<>();
            this.commonAttributesSupplier = commonAttributesSupplier;
            this.dimensionName = dimensionName;
            this.defaultAttr = commonAttributesSupplier.get();
            this.defaultAttr.add(dimensionName, unknownValue);
        }

        public AzureAttributeCollection getAttributeBuilder(T value) {
            return attr.computeIfAbsent(value, v -> commonAttributesSupplier.get()
                .add(dimensionName, value.toString()));
        }

        public AzureAttributeCollection getAttributeBuilderForUnknown() {
            return defaultAttr;
        }
    }
}
