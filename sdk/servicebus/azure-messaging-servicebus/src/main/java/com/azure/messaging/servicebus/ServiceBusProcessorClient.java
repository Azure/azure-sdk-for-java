// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.ProcessKind;
import com.azure.messaging.servicebus.ServiceBusClientBuilder.ServiceBusProcessorClientBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder;
import com.azure.messaging.servicebus.implementation.models.ServiceBusProcessorClientOptions;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Signal;
import reactor.core.scheduler.Schedulers;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;
import static com.azure.core.util.tracing.Tracer.DIAGNOSTIC_ID_KEY;
import static com.azure.core.util.tracing.Tracer.ENTITY_PATH_KEY;
import static com.azure.core.util.tracing.Tracer.HOST_NAME_KEY;
import static com.azure.core.util.tracing.Tracer.MESSAGE_ENQUEUED_TIME;
import static com.azure.core.util.tracing.Tracer.SCOPE_KEY;
import static com.azure.core.util.tracing.Tracer.SPAN_CONTEXT_KEY;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.AZ_TRACING_NAMESPACE_VALUE;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.AZ_TRACING_SERVICE_NAME;

/**
 * The processor client for processing Service Bus messages. {@link ServiceBusProcessorClient} provides a push-based
 * mechanism that invokes the message processing callback when a message is received or the error handler when an error
 * occurs when receiving messages. A {@link ServiceBusProcessorClient} can be created to process messages for a
 * session-enabled or non session-enabled Service Bus entity. It supports auto-settlement of messages by default.
 *
 * <p><strong>Create and run a processor</strong></p>
 * {@codesnippet com.azure.messaging.servicebus.servicebusprocessorclient#instantiation}
 *
 * <p><strong>Create and run a session-enabled processor</strong></p>
 * {@codesnippet com.azure.messaging.servicebus.servicebusprocessorclient#session-instantiation}
 *
 * @see ServiceBusProcessorClientBuilder
 * @see ServiceBusSessionProcessorClientBuilder
 */
public final class ServiceBusProcessorClient implements AutoCloseable {

    private static final int SCHEDULER_INTERVAL_IN_SECONDS = 10;
    private final ClientLogger logger = new ClientLogger(ServiceBusProcessorClient.class);
    private final ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder sessionReceiverBuilder;
    private final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder receiverBuilder;
    private final Consumer<ServiceBusReceivedMessageContext> processMessage;
    private final Consumer<ServiceBusErrorContext> processError;
    private final ServiceBusProcessorClientOptions processorOptions;
    // Use ConcurrentHashMap as a set because there is no ConcurrentHashSet.
    private final Map<Subscription, Subscription> receiverSubscriptions = new ConcurrentHashMap<>();
    private final AtomicReference<ServiceBusReceiverAsyncClient> asyncClient = new AtomicReference<>();
    private final AtomicBoolean isRunning = new AtomicBoolean();
    private final TracerProvider tracerProvider;
    private ScheduledExecutorService scheduledExecutor;

    /**
     * Constructor to create a sessions-enabled processor.
     *
     * @param sessionReceiverBuilder The session processor builder to create new instances of async clients.
     * @param processMessage The message processing callback.
     * @param processError The error handler.
     * @param processorOptions Options to configure this instance of the processor.
     */
    ServiceBusProcessorClient(ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder sessionReceiverBuilder,
        Consumer<ServiceBusReceivedMessageContext> processMessage,
        Consumer<ServiceBusErrorContext> processError,
        ServiceBusProcessorClientOptions processorOptions) {
        this.sessionReceiverBuilder = Objects.requireNonNull(sessionReceiverBuilder,
            "'sessionReceiverBuilder' cannot be null");
        this.processMessage = Objects.requireNonNull(processMessage, "'processMessage' cannot be null");
        this.processError = Objects.requireNonNull(processError, "'processError' cannot be null");
        this.processorOptions = Objects.requireNonNull(processorOptions, "'processorOptions' cannot be null");
        this.asyncClient.set(sessionReceiverBuilder.buildAsyncClientForProcessor());
        this.receiverBuilder = null;
        this.tracerProvider = processorOptions.getTracerProvider();
    }

    /**
     * Constructor to create a processor.
     *
     * @param receiverBuilder The processor builder to create new instances of async clients.
     * @param processMessage The message processing callback.
     * @param processError The error handler.
     * @param processorOptions Options to configure this instance of the processor.
     */
    ServiceBusProcessorClient(ServiceBusClientBuilder.ServiceBusReceiverClientBuilder receiverBuilder,
        Consumer<ServiceBusReceivedMessageContext> processMessage,
        Consumer<ServiceBusErrorContext> processError, ServiceBusProcessorClientOptions processorOptions) {
        this.receiverBuilder = Objects.requireNonNull(receiverBuilder, "'receiverBuilder' cannot be null");
        this.processMessage = Objects.requireNonNull(processMessage, "'processMessage' cannot be null");
        this.processError = Objects.requireNonNull(processError, "'processError' cannot be null");
        this.processorOptions = Objects.requireNonNull(processorOptions, "'processorOptions' cannot be null");
        this.asyncClient.set(receiverBuilder.buildAsyncClient());
        this.sessionReceiverBuilder = null;
        this.tracerProvider = processorOptions.getTracerProvider();
    }

    /**
     * Starts the processor in the background. When this method is called, the processor will initiate a message
     * receiver that will invoke the message handler when new messages are available. This method is idempotent (ie.
     * calling {@code start()} again after the processor is already running is a no-op).
     * <p>
     * Calling {@code start()} after calling {@link #stop() stop()} will resume processing messages using the same
     * underlying connection.
     * </p>
     * <p>
     * Calling {@code start()} after calling {@link #close() close()} will start the processor with a new connection.
     * </p>
     */
    public synchronized void start() {
        if (isRunning.getAndSet(true)) {
            logger.info("Processor is already running");
            return;
        }

        if (asyncClient.get() == null) {
            ServiceBusReceiverAsyncClient newReceiverClient = this.receiverBuilder == null
                ? this.sessionReceiverBuilder.buildAsyncClientForProcessor()
                : this.receiverBuilder.buildAsyncClient();
            asyncClient.set(newReceiverClient);
        }

        receiveMessages();

        // Start an executor to periodically check if the client's connection is active
        if (this.scheduledExecutor == null) {
            this.scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutor.scheduleWithFixedDelay(() -> {
                if (this.asyncClient.get().isConnectionClosed()) {
                    restartMessageReceiver(null);
                }
            }, SCHEDULER_INTERVAL_IN_SECONDS, SCHEDULER_INTERVAL_IN_SECONDS, TimeUnit.SECONDS);
        }
    }

    /**
     * Stops the message processing for this processor. The receiving links and sessions are kept active and this
     * processor can resume processing messages by calling {@link #start()} again.
     */
    public synchronized void stop() {
        isRunning.set(false);
    }

    /**
     * Stops message processing and closes the processor. The receiving links and sessions are closed and calling
     * {@link #start()} will create a new processing cycle with new links and new sessions.
     */
    @Override
    public synchronized void close() {
        isRunning.set(false);
        receiverSubscriptions.keySet().forEach(Subscription::cancel);
        receiverSubscriptions.clear();
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdown();
            scheduledExecutor = null;
        }
        if (asyncClient.get() != null) {
            asyncClient.get().close();
            asyncClient.set(null);
        }
    }

    /**
     * Returns {@code true} if the processor is running. If the processor is stopped or closed, this method returns
     * {@code false}.
     *
     * @return {@code true} if the processor is running; {@code false} otherwise.
     */
    public synchronized boolean isRunning() {
        return isRunning.get();
    }

    private synchronized void receiveMessages() {
        if (receiverSubscriptions.size() > 0) {
            // For the case of start -> stop -> start again
            receiverSubscriptions.keySet().forEach(subscription -> subscription.request(1L));
            return;
        }
        ServiceBusReceiverAsyncClient receiverClient = asyncClient.get();

        @SuppressWarnings({"unchecked", "rawtypes"})
        CoreSubscriber<ServiceBusMessageContext>[] subscribers = new CoreSubscriber[processorOptions.getMaxConcurrentCalls()];

        for (int i = 0; i < processorOptions.getMaxConcurrentCalls(); i++) {
            subscribers[i] = new CoreSubscriber<ServiceBusMessageContext>() {
                private Subscription subscription = null;

                @Override
                public void onSubscribe(Subscription subscription) {
                    this.subscription = subscription;
                    receiverSubscriptions.put(subscription, subscription);
                    subscription.request(1);
                }

                @Override
                public void onNext(ServiceBusMessageContext serviceBusMessageContext) {
                    if (serviceBusMessageContext.hasError()) {
                        handleError(serviceBusMessageContext.getThrowable());
                    } else {
                        Context processSpanContext = null;
                        try {
                            ServiceBusReceivedMessageContext serviceBusReceivedMessageContext =
                                new ServiceBusReceivedMessageContext(receiverClient, serviceBusMessageContext);

                            processSpanContext =
                                startProcessTracingSpan(serviceBusMessageContext.getMessage(),
                                    receiverClient.getEntityPath(), receiverClient.getFullyQualifiedNamespace());
                            if (processSpanContext.getData(SPAN_CONTEXT_KEY).isPresent()) {
                                serviceBusMessageContext.getMessage().addContext(SPAN_CONTEXT_KEY, processSpanContext);
                            }
                            processMessage.accept(serviceBusReceivedMessageContext);
                            endProcessTracingSpan(processSpanContext, Signal.complete());
                        } catch (Exception ex) {
                            handleError(new ServiceBusException(ex, ServiceBusErrorSource.USER_CALLBACK));
                            endProcessTracingSpan(processSpanContext, Signal.error(ex));
                            if (!processorOptions.isDisableAutoComplete()) {
                                logger.warning("Error when processing message. Abandoning message.", ex);
                                abandonMessage(serviceBusMessageContext, receiverClient);
                            }
                        }
                    }
                    if (isRunning.get()) {
                        logger.verbose("Requesting 1 more message from upstream");
                        subscription.request(1);
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    logger.info("Error receiving messages.", throwable);
                    handleError(throwable);
                    if (isRunning.get()) {
                        restartMessageReceiver(subscription);
                    }
                }

                @Override
                public void onComplete() {
                    logger.info("Completed receiving messages.");
                    if (isRunning.get()) {
                        restartMessageReceiver(subscription);
                    }
                }
            };
        }

        receiverClient.receiveMessagesWithContext()
            .parallel(processorOptions.getMaxConcurrentCalls(), 1)
            .runOn(Schedulers.boundedElastic(), 1)
            .subscribe(subscribers);
    }

    private void endProcessTracingSpan(Context processSpanContext, Signal<Void> signal) {
        if (processSpanContext == null) {
            return;
        }

        Optional<Object> spanScope = processSpanContext.getData(SCOPE_KEY);
        // Disposes of the scope when the trace span closes.
        if (!spanScope.isPresent() || !tracerProvider.isEnabled()) {
            return;
        }
        if (spanScope.get() instanceof AutoCloseable) {
            AutoCloseable close = (AutoCloseable) processSpanContext.getData(SCOPE_KEY).get();
            try {
                close.close();
            } catch (Exception exception) {
                logger.error("endTracingSpan().close() failed with an error {}", exception);
            }

        } else {
            logger.warning(String.format(Locale.US,
                "Process span scope type is not of type AutoCloseable, but type: %s. Not closing the scope"
                    + " and span", spanScope.get() != null ? spanScope.getClass() : "null"));
        }
        tracerProvider.endSpan(processSpanContext, signal);
    }

    private Context startProcessTracingSpan(ServiceBusReceivedMessage receivedMessage, String entityPath,
        String fullyQualifiedNamespace) {

        Object diagnosticId = receivedMessage.getApplicationProperties().get(DIAGNOSTIC_ID_KEY);
        if (diagnosticId == null || !tracerProvider.isEnabled()) {
            return Context.NONE;
        }

        Context spanContext = tracerProvider.extractContext(diagnosticId.toString(), Context.NONE);

        spanContext = spanContext
            .addData(ENTITY_PATH_KEY, entityPath)
            .addData(HOST_NAME_KEY, fullyQualifiedNamespace)
            .addData(AZ_TRACING_NAMESPACE_KEY, AZ_TRACING_NAMESPACE_VALUE);
        spanContext = receivedMessage.getEnqueuedTime() == null
            ? spanContext
            : spanContext.addData(MESSAGE_ENQUEUED_TIME,
            receivedMessage.getEnqueuedTime().toInstant().getEpochSecond());

        return tracerProvider.startSpan(AZ_TRACING_SERVICE_NAME, spanContext, ProcessKind.PROCESS);
    }

    private void abandonMessage(ServiceBusMessageContext serviceBusMessageContext,
        ServiceBusReceiverAsyncClient receiverClient) {
        try {
            receiverClient.abandon(serviceBusMessageContext.getMessage()).block();
        } catch (Exception exception) {
            logger.verbose("Failed to abandon message", exception);
        }
    }

    private void handleError(Throwable throwable) {
        try {
            ServiceBusReceiverAsyncClient client = asyncClient.get();
            final String fullyQualifiedNamespace = client.getFullyQualifiedNamespace();
            final String entityPath = client.getEntityPath();
            processError.accept(new ServiceBusErrorContext(throwable, fullyQualifiedNamespace, entityPath));
        } catch (Exception ex) {
            logger.verbose("Error from error handler. Ignoring error.", ex);
        }
    }

    private synchronized void restartMessageReceiver(Subscription requester) {
        if (!isRunning()) {
            return;
        }
        if (requester != null && !receiverSubscriptions.containsKey(requester)) {
            return;
        }
        receiverSubscriptions.keySet().forEach(Subscription::cancel);
        receiverSubscriptions.clear();
        ServiceBusReceiverAsyncClient receiverClient = asyncClient.get();
        receiverClient.close();
        ServiceBusReceiverAsyncClient newReceiverClient = this.receiverBuilder == null
            ? this.sessionReceiverBuilder.buildAsyncClientForProcessor()
            : this.receiverBuilder.buildAsyncClient();
        asyncClient.set(newReceiverClient);
        receiveMessages();
    }
}
