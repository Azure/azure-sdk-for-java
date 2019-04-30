// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventprocessorhost;

import com.microsoft.azure.eventhubs.EventPosition;
import com.microsoft.azure.eventhubs.PartitionReceiver;

import java.time.Duration;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * Options affecting the behavior of the event processor host instance in general.
 */
public final class EventProcessorOptions {
    private Consumer<ExceptionReceivedEventArgs> exceptionNotificationHandler = null;
    private Boolean invokeProcessorAfterReceiveTimeout = false;
    private boolean receiverRuntimeMetricEnabled = false;
    private int maxBatchSize = 10;
    private int prefetchCount = 300;
    private Duration receiveTimeOut = Duration.ofMinutes(1);
    private Function<String, EventPosition> initialPositionProvider = (partitionId) -> {
        return EventPosition.fromStartOfStream();
    };

    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(EventProcessorOptions.class);

    public EventProcessorOptions() {
    }

    /***
     * Returns an EventProcessorOptions instance with all options set to the default values.
     *
     * The default values are:
     * <pre>
     * MaxBatchSize: 10
     * ReceiveTimeOut: 1 minute
     * PrefetchCount: 300
     * InitialPositionProvider: uses the last checkpoint, or START_OF_STREAM
     * InvokeProcessorAfterReceiveTimeout: false
     * ReceiverRuntimeMetricEnabled: false
     * </pre>
     *
     * @return an EventProcessorOptions instance with all options set to the default values
     */
    public static EventProcessorOptions getDefaultOptions() {
        return new EventProcessorOptions();
    }

    /**
     * Sets a handler which receives notification of general exceptions.
     * <p>
     * Exceptions which occur while processing events from a particular Event Hub partition are delivered
     * to the onError method of the event processor for that partition. This handler is called on occasions
     * when there is no event processor associated with the throwing activity, or the event processor could
     * not be created.
     * <p>
     * The handler is not expected to do anything about the exception. If it is possible to recover, the
     * event processor host instance will recover automatically.
     *
     * @param notificationHandler Handler which is called when an exception occurs. Set to null to stop handling.
     */
    public void setExceptionNotification(Consumer<ExceptionReceivedEventArgs> notificationHandler) {
        this.exceptionNotificationHandler = notificationHandler;
    }

    /**
     * Returns the maximum number of events that will be passed to one call to IEventProcessor.onEvents
     *
     * @return the maximum maximum number of events that will be passed to one call to IEventProcessor.onEvents
     */
    public int getMaxBatchSize() {
        return this.maxBatchSize;
    }

    /**
     * Sets the maximum number of events that will be passed to one call to IEventProcessor.onEvents
     *
     * @param maxBatchSize the maximum number of events that will be passed to one call to IEventProcessor.onEvents
     */
    public void setMaxBatchSize(int maxBatchSize) {
        this.maxBatchSize = maxBatchSize;
    }

    /**
     * Returns the timeout for receive operations.
     *
     * @return the timeout for receive operations
     */
    public Duration getReceiveTimeOut() {
        return this.receiveTimeOut;
    }

    /**
     * Sets the timeout for receive operations.
     *
     * @param receiveTimeOut new timeout for receive operations
     */
    public void setReceiveTimeOut(Duration receiveTimeOut) {
        this.receiveTimeOut = receiveTimeOut;
    }

    /***
     * Returns the current prefetch count for the underlying event hub client.
     *
     * @return the current prefetch count for the underlying client
     */
    public int getPrefetchCount() {
        return this.prefetchCount;
    }

    /***
     * Sets the prefetch count for the underlying event hub client.
     *
     * The default is 300. This controls how many events are received in advance.
     *
     * @param prefetchCount  The new prefetch count.
     */
    public void setPrefetchCount(int prefetchCount) {
        if (prefetchCount < PartitionReceiver.MINIMUM_PREFETCH_COUNT) {
            throw new IllegalArgumentException(String.format(Locale.US,
                    "PrefetchCount has to be above %s", PartitionReceiver.MINIMUM_PREFETCH_COUNT));
        }

        if (prefetchCount > PartitionReceiver.MAXIMUM_PREFETCH_COUNT) {
            throw new IllegalArgumentException(String.format(Locale.US,
                    "PrefetchCount has to be below %s", PartitionReceiver.MAXIMUM_PREFETCH_COUNT));
        }

        this.prefetchCount = prefetchCount;
    }

    /***
     * If there is no checkpoint for a partition, the initialPositionProvider function is used to determine
     * the position at which to start receiving events for that partition.
     *
     * @return the current initial position provider function
     */
    public Function<String, EventPosition> getInitialPositionProvider() {
        return this.initialPositionProvider;
    }

    /***
     * Sets the function used to determine the position at which to start receiving events for a
     * partition if there is no checkpoint for that partition.
     *
     * The provider function takes one argument, the partition id (a String), and returns the desired position.
     *
     * @param initialPositionProvider The new provider function.
     */
    public void setInitialPositionProvider(Function<String, EventPosition> initialPositionProvider) {
        this.initialPositionProvider = initialPositionProvider;
    }

    /***
     * Returns whether the EventProcessorHost will call IEventProcessor.onEvents() with an empty iterable
     * when a receive timeout occurs (true) or not (false).
     *
     * Defaults to false.
     *
     * @return true if EventProcessorHost will call IEventProcessor.OnEvents on receive timeout, false otherwise
     */
    public Boolean getInvokeProcessorAfterReceiveTimeout() {
        return this.invokeProcessorAfterReceiveTimeout;
    }

    /**
     * Changes whether the EventProcessorHost will call IEventProcessor.onEvents() with an empty iterable
     * when a receive timeout occurs (true) or not (false).
     * <p>
     * The default is false (no call).
     *
     * @param invokeProcessorAfterReceiveTimeout the new value for what to do
     */
    public void setInvokeProcessorAfterReceiveTimeout(Boolean invokeProcessorAfterReceiveTimeout) {
        this.invokeProcessorAfterReceiveTimeout = invokeProcessorAfterReceiveTimeout;
    }

    /**
     * Knob to enable/disable runtime metric of the receiver. If this is set to true,
     * the first parameter {@link com.microsoft.azure.eventprocessorhost.PartitionContext#runtimeInformation} of
     * {@link IEventProcessor#onEvents(com.microsoft.azure.eventprocessorhost.PartitionContext, java.lang.Iterable)} will be populated.
     * <p>
     * Enabling this knob will add 3 additional properties to all raw AMQP events received.
     *
     * @return the {@link boolean} indicating, whether, the runtime metric of the receiver was enabled
     */
    public boolean getReceiverRuntimeMetricEnabled() {
        return this.receiverRuntimeMetricEnabled;
    }

    /**
     * Knob to enable/disable runtime metric of the receiver. If this is set to true,
     * the first parameter {@link com.microsoft.azure.eventprocessorhost.PartitionContext#runtimeInformation} of
     * {@link IEventProcessor#onEvents(com.microsoft.azure.eventprocessorhost.PartitionContext, java.lang.Iterable)} will be populated.
     * <p>
     * Enabling this knob will add 3 additional properties to all raw AMQP events received.
     *
     * @param value the {@link boolean} to indicate, whether, the runtime metric of the receiver should be enabled
     */
    public void setReceiverRuntimeMetricEnabled(boolean value) {
        this.receiverRuntimeMetricEnabled = value;
    }

    void notifyOfException(String hostname, Exception exception, String action) {
        notifyOfException(hostname, exception, action, ExceptionReceivedEventArgs.NO_ASSOCIATED_PARTITION);
    }

    void notifyOfException(String hostname, Exception exception, String action, String partitionId) {
        // Capture handler so it doesn't get set to null between test and use
        Consumer<ExceptionReceivedEventArgs> handler = this.exceptionNotificationHandler;
        if (handler != null) {
            try {
                handler.accept(new ExceptionReceivedEventArgs(hostname, exception, action, partitionId));
            } catch (Exception e) {
                TRACE_LOGGER.error("host " + hostname + ": caught exception from user-provided exception notification handler", e);
            }
        }
    }

    /***
     * A prefab initial position provider that starts from the first event available.
     *
     * How to use this initial position provider: setInitialPositionProvider(new EventProcessorOptions.StartOfStreamInitialPositionProvider());
     */
    public class StartOfStreamInitialPositionProvider implements Function<String, EventPosition> {
        @Override
        public EventPosition apply(String t) {
            return EventPosition.fromStartOfStream();
        }
    }

    /***
     * A prefab initial position provider that starts from the next event that becomes available.
     *
     * How to use this initial position provider: setInitialPositionProvider(new EventProcessorOptions.EndOfStreamInitialPositionProvider());
     */
    public class EndOfStreamInitialPositionProvider implements Function<String, EventPosition> {
        @Override
        public EventPosition apply(String t) {
            return EventPosition.fromEndOfStream();
        }
    }
}
