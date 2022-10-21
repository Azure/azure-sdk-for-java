// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation.instrumentation;

import com.azure.core.amqp.implementation.ClientConstants;
import com.azure.core.util.Context;
import com.azure.core.util.TelemetryAttributes;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.metrics.DoubleHistogram;
import com.azure.core.util.metrics.LongCounter;
import com.azure.core.util.metrics.LongGauge;
import com.azure.core.util.metrics.Meter;
import com.azure.messaging.servicebus.implementation.DispositionStatus;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.core.amqp.implementation.ClientConstants.HOSTNAME_KEY;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.DISPOSITION_STATUS_KEY;

/**
 * Contains methods to report servicebus metrics.
 */
public class ServiceBusMeter {
    private static final ClientLogger LOGGER = new ClientLogger(ServiceBusMeter.class);
    private static final String GENERIC_STATUS_KEY = "status";
    private static final int DISPOSITION_STATUSES_COUNT  = DispositionStatus.values().length;
    private static final AutoCloseable NOOP_CLOSEABLE = () -> {
    };
    private final Meter meter;
    private final boolean isEnabled;

    private TelemetryAttributes sendAttributesSuccess;
    private TelemetryAttributes sendAttributesFailure;
    private TelemetryAttributes receiveAttributes;

    /**
     * Settle attributes contain disposition status - thus the array where ordinal number of disposition status
     * is used as an index.
     * We also have a combination of attributes that has success and failure statuses.
     */
    private TelemetryAttributes[] settleSuccessAttributes;
    private TelemetryAttributes[] settleFailureAttributes;

    private AtomicReference<CompositeSubscription> lastSeqNoSubscription = new AtomicReference<>(null);
    private LongCounter sentMessagesCounter;
    private DoubleHistogram consumerLag;
    private DoubleHistogram settleMessageDuration;
    private LongGauge settledSequenceNumber;

    public ServiceBusMeter(Meter meter, String namespace, String entityPath, String subscriptionName) {
        this.meter = meter;
        this.isEnabled = meter != null && meter.isEnabled();
        if (this.isEnabled) {
            Map<String, Object> commonAttributesMap = new HashMap<>(3);
            commonAttributesMap.put(HOSTNAME_KEY, namespace);
            int entityNameEnd = entityPath.indexOf('/');
            if (entityNameEnd > 0) {
                commonAttributesMap.put(ClientConstants.ENTITY_NAME_KEY,  entityPath.substring(0, entityNameEnd));
            } else {
                commonAttributesMap.put(ClientConstants.ENTITY_NAME_KEY,  entityPath);
            }

            if (subscriptionName != null) {
                commonAttributesMap.put("subscriptionName", subscriptionName);
            }

            Map<String, Object> successMap = new HashMap<>(commonAttributesMap);
            successMap.put(GENERIC_STATUS_KEY, "ok");
            this.sendAttributesSuccess = meter.createAttributes(successMap);

            Map<String, Object> failureMap = new HashMap<>(commonAttributesMap);
            failureMap.put(GENERIC_STATUS_KEY, "error");
            this.sendAttributesFailure = meter.createAttributes(failureMap);

            this.settleSuccessAttributes = new TelemetryAttributes[DISPOSITION_STATUSES_COUNT];
            this.settleFailureAttributes = new TelemetryAttributes[DISPOSITION_STATUSES_COUNT];
            for (int i = 0; i < DISPOSITION_STATUSES_COUNT; i++) {
                Map<String, Object> successMapWithStatus = new HashMap<>(successMap);
                successMapWithStatus.put(DISPOSITION_STATUS_KEY, DispositionStatus.values()[i].getValue());
                this.settleSuccessAttributes[i] = meter.createAttributes(successMapWithStatus);

                Map<String, Object> failureMapWithStatus = new HashMap<>(failureMap);
                failureMapWithStatus.put(DISPOSITION_STATUS_KEY, DispositionStatus.values()[i].getValue());
                this.settleFailureAttributes[i] = meter.createAttributes(failureMapWithStatus);
            }

            this.receiveAttributes = meter.createAttributes(commonAttributesMap);
            this.sentMessagesCounter = meter.createLongCounter("messaging.servicebus.messages.sent", "Number of sent messages", "messages");
            this.settleMessageDuration = meter.createDoubleHistogram("messaging.servicebus.settlement.request.duration", "Duration of settlement call.", "ms");
            this.consumerLag = meter.createDoubleHistogram("messaging.servicebus.receiver.lag", "Difference between local time when event was received and the local time it was enqueued on broker.", "sec");
            this.settledSequenceNumber = this.meter.createLongGauge("messaging.servicebus.settlement.sequence_number", "Last settled message sequence number", "seqNo");
        }
    }

    /**
     * Checks if metrics and reporting event count are enabled. Used for micro-optimizations
     */
    public boolean isBatchSendEnabled() {
        return isEnabled && sentMessagesCounter.isEnabled();
    }

    /**
     * Reports sent messages count.
     */
    public void reportBatchSend(int batchSize, Throwable throwable, Context context) {
        if (isEnabled && sentMessagesCounter.isEnabled()) {
            TelemetryAttributes attributes = throwable == null ? sendAttributesSuccess : sendAttributesFailure;
            sentMessagesCounter.add(batchSize, attributes, context);
        }
    }

    /**
     * Checks if metrics and reporting consumer lag are enabled. Used for micro-optimizations
     */
    boolean isConsumerLagEnabled() {
        return isEnabled && consumerLag.isEnabled();
    }

    /**
     * Reports consumer lag for received message.
     */
    void reportConsumerLag(OffsetDateTime enqueuedTime, Context context) {
        if (isEnabled && consumerLag.isEnabled()) {
            double diff = 0d;
            if (enqueuedTime != null) {
                diff = Instant.now().toEpochMilli() - enqueuedTime.toInstant().toEpochMilli();
                if (diff < 0) {
                    // time skew on machines
                    diff = 0;
                }
            }
            consumerLag.record(diff / 1000d, receiveAttributes, context);
        }
    }

    /**
     * Checks if metrics and reporting settlement are enabled. Used for micro-optimizations.
     */
    public boolean isSettlementEnabled() {
        return isEnabled && (settleMessageDuration.isEnabled() || settledSequenceNumber.isEnabled());
    }

    /**
     * Reports count of settled messages by corresponding status. Also reports last sequence number
     * if there is an active subscription for last sequence number reporting obtained
     * with {@link ServiceBusMeter#trackSettlementSequenceNumber()}.
     */
    public void reportSettlement(long start, long seqNo, DispositionStatus status, Throwable throwable, Context context) {
        if (isEnabled) {
            if (settleMessageDuration.isEnabled()) {
                TelemetryAttributes attributes = throwable == null ? settleSuccessAttributes[status.ordinal()]
                    : settleFailureAttributes[status.ordinal()];

                settleMessageDuration.record(Instant.now().toEpochMilli() - start, attributes, context);
            }

            CompositeSubscription subs = lastSeqNoSubscription.get();
            if (settledSequenceNumber.isEnabled() && subs != null) {
                subs.set(seqNo, status, throwable == null);
            }
        }
    }

    /**
     * Creates gauge subscription to report the latest sequence number value. Call it in client constructor and
     * close once client closes.
     */
    AutoCloseable trackSettlementSequenceNumber() {
        if (!isEnabled || !settledSequenceNumber.isEnabled()) {
            return NOOP_CLOSEABLE;
        }

        CompositeSubscription existingSubscription = lastSeqNoSubscription.get();
        if (existingSubscription == null) {
            CompositeSubscription subs = new CompositeSubscription(settledSequenceNumber, settleSuccessAttributes, settleFailureAttributes);
            if (lastSeqNoSubscription.compareAndSet(null, subs)) {
                return subs;
            }
            subs.close();
        }

        LOGGER.warning("Sequence number subscription has been already created.");
        return existingSubscription;
    }

    private static class CompositeSubscription implements AutoCloseable {
        private final AtomicLong[] lastSeqNoSuccess = new AtomicLong[DISPOSITION_STATUSES_COUNT];
        private final AtomicLong[] lastSeqNoFailure = new AtomicLong[DISPOSITION_STATUSES_COUNT];
        private final AutoCloseable[] subscriptionsSuccess = new AutoCloseable[DISPOSITION_STATUSES_COUNT];
        private final AutoCloseable[] subscriptionsFailure = new AutoCloseable[DISPOSITION_STATUSES_COUNT];

        CompositeSubscription(LongGauge settledSequenceNumber,
            TelemetryAttributes[] settleSuccessAttributes, TelemetryAttributes[] settleFailureAttributes) {
            for (int i = 0; i < DISPOSITION_STATUSES_COUNT; i++) {
                lastSeqNoSuccess[i] = new AtomicLong();
                lastSeqNoFailure[i] = new AtomicLong();
                final int fi = i;
                subscriptionsSuccess[i] = settledSequenceNumber.registerCallback(() -> lastSeqNoSuccess[fi].get(), settleSuccessAttributes[i]);
                subscriptionsFailure[i] = settledSequenceNumber.registerCallback(() -> lastSeqNoFailure[fi].get(), settleFailureAttributes[i]);
            }
        }

        public void set(long value, DispositionStatus status, boolean success) {
            AtomicLong valueSetter = success ? lastSeqNoSuccess[status.ordinal()] : lastSeqNoFailure[status.ordinal()];
            valueSetter.set(value);
        }

        @Override
        public void close() {
            for (int i = 0; i < DISPOSITION_STATUSES_COUNT; i++) {
                try {
                    subscriptionsSuccess[i].close();
                    subscriptionsFailure[i].close();
                } catch (Exception ex) {
                    LOGGER.info("Unable to close settlement sequence number subscription.", ex);
                }
            }
        }
    }
}
