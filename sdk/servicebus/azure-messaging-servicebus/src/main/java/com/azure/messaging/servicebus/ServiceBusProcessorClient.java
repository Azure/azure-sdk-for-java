// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusClientBuilder.ServiceBusProcessorClientBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder;
import com.azure.messaging.servicebus.implementation.ServiceBusProcessorClientOptions;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusTracer;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.scheduler.Schedulers;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.azure.messaging.servicebus.FluxTrace.PROCESS_ERROR_KEY;

/**
 * The processor client for processing Service Bus messages. {@link ServiceBusProcessorClient} provides a push-based
 * mechanism that invokes the message processing callback when a message is received or the error handler when an error
 * occurs when receiving messages. A {@link ServiceBusProcessorClient} can be created to process messages for a
 * session-enabled or non session-enabled Service Bus entity. It supports auto-settlement of messages by default.
 *
 * <p><strong>Sample code to instantiate a processor client and receive in PeekLock mode</strong></p>
 * <!-- src_embed com.azure.messaging.servicebus.servicebusprocessorclient#receive-mode-peek-lock-instantiation -->
 * <pre>
 * Consumer&lt;ServiceBusReceivedMessageContext&gt; processMessage = context -&gt; &#123;
 *     final ServiceBusReceivedMessage message = context.getMessage&#40;&#41;;
 *     &#47;&#47; Randomly complete or abandon each message. Ideally, in real-world scenarios, if the business logic
 *     &#47;&#47; handling message reaches desired state such that it doesn't require Service Bus to redeliver
 *     &#47;&#47; the same message, then context.complete&#40;&#41; should be called otherwise context.abandon&#40;&#41;.
 *     final boolean success = Math.random&#40;&#41; &lt; 0.5;
 *     if &#40;success&#41; &#123;
 *         try &#123;
 *             context.complete&#40;&#41;;
 *         &#125; catch &#40;Exception completionError&#41; &#123;
 *             System.out.printf&#40;&quot;Completion of the message %s failed&#92;n&quot;, message.getMessageId&#40;&#41;&#41;;
 *             completionError.printStackTrace&#40;&#41;;
 *         &#125;
 *     &#125; else &#123;
 *         try &#123;
 *             context.abandon&#40;&#41;;
 *         &#125; catch &#40;Exception abandonError&#41; &#123;
 *             System.out.printf&#40;&quot;Abandoning of the message %s failed&#92;n&quot;, message.getMessageId&#40;&#41;&#41;;
 *             abandonError.printStackTrace&#40;&#41;;
 *         &#125;
 *     &#125;
 * &#125;;
 *
 * &#47;&#47; Sample code that gets called if there's an error
 * Consumer&lt;ServiceBusErrorContext&gt; processError = errorContext -&gt; &#123;
 *     System.err.println&#40;&quot;Error occurred while receiving message: &quot; + errorContext.getException&#40;&#41;&#41;;
 * &#125;;
 *
 * &#47;&#47; create the processor client via the builder and its sub-builder
 * ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder&#40;&#41;
 *     .connectionString&#40;&quot;&lt;&lt; CONNECTION STRING FOR THE SERVICE BUS NAMESPACE &gt;&gt;&quot;&#41;
 *     .processor&#40;&#41;
 *     .queueName&#40;&quot;&lt;&lt; QUEUE NAME &gt;&gt;&quot;&#41;
 *     .receiveMode&#40;ServiceBusReceiveMode.PEEK_LOCK&#41;
 *     .disableAutoComplete&#40;&#41;  &#47;&#47; Make sure to explicitly opt in to manual settlement &#40;e.g. complete, abandon&#41;.
 *     .processMessage&#40;processMessage&#41;
 *     .processError&#40;processError&#41;
 *     .disableAutoComplete&#40;&#41;
 *     .buildProcessorClient&#40;&#41;;
 *
 * &#47;&#47; Starts the processor in the background and returns immediately
 * processorClient.start&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.servicebusprocessorclient#receive-mode-peek-lock-instantiation -->
 * <p><strong>Sample code to instantiate a processor client and receive in ReceiveAndDelete mode</strong></p>
 * <!-- src_embed com.azure.messaging.servicebus.servicebusprocessorclient#receive-mode-receive-and-delete-instantiation -->
 * <pre>
 * Consumer&lt;ServiceBusReceivedMessageContext&gt; processMessage = context -&gt; &#123;
 *     final ServiceBusReceivedMessage message = context.getMessage&#40;&#41;;
 *     System.out.printf&#40;&quot;Processing message. Session: %s, Sequence #: %s. Contents: %s%n&quot;,
 *         message.getSessionId&#40;&#41;, message.getSequenceNumber&#40;&#41;, message.getBody&#40;&#41;&#41;;
 * &#125;;
 *
 * &#47;&#47; Sample code that gets called if there's an error
 * Consumer&lt;ServiceBusErrorContext&gt; processError = errorContext -&gt; &#123;
 *     System.err.println&#40;&quot;Error occurred while receiving message: &quot; + errorContext.getException&#40;&#41;&#41;;
 * &#125;;
 *
 * &#47;&#47; create the processor client via the builder and its sub-builder
 * ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder&#40;&#41;
 *     .connectionString&#40;&quot;&lt;&lt; CONNECTION STRING FOR THE SERVICE BUS NAMESPACE &gt;&gt;&quot;&#41;
 *     .processor&#40;&#41;
 *     .queueName&#40;&quot;&lt;&lt; QUEUE NAME &gt;&gt;&quot;&#41;
 *     .receiveMode&#40;ServiceBusReceiveMode.RECEIVE_AND_DELETE&#41;
 *     .processMessage&#40;processMessage&#41;
 *     .processError&#40;processError&#41;
 *     .disableAutoComplete&#40;&#41;
 *     .buildProcessorClient&#40;&#41;;
 *
 * &#47;&#47; Starts the processor in the background and returns immediately
 * processorClient.start&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.servicebusprocessorclient#receive-mode-receive-and-delete-instantiation -->
 * <p><strong>Create and run a session-enabled processor</strong></p>
 * <!-- src_embed com.azure.messaging.servicebus.servicebusprocessorclient#session-instantiation -->
 * <pre>
 * Consumer&lt;ServiceBusReceivedMessageContext&gt; onMessage = context -&gt; &#123;
 *     ServiceBusReceivedMessage message = context.getMessage&#40;&#41;;
 *     System.out.printf&#40;&quot;Processing message. Session: %s, Sequence #: %s. Contents: %s%n&quot;,
 *         message.getSessionId&#40;&#41;, message.getSequenceNumber&#40;&#41;, message.getBody&#40;&#41;&#41;;
 * &#125;;
 *
 * Consumer&lt;ServiceBusErrorContext&gt; onError = context -&gt; &#123;
 *     System.out.printf&#40;&quot;Error when receiving messages from namespace: '%s'. Entity: '%s'%n&quot;,
 *         context.getFullyQualifiedNamespace&#40;&#41;, context.getEntityPath&#40;&#41;&#41;;
 *
 *     if &#40;context.getException&#40;&#41; instanceof ServiceBusException&#41; &#123;
 *         ServiceBusException exception = &#40;ServiceBusException&#41; context.getException&#40;&#41;;
 *         System.out.printf&#40;&quot;Error source: %s, reason %s%n&quot;, context.getErrorSource&#40;&#41;,
 *             exception.getReason&#40;&#41;&#41;;
 *     &#125; else &#123;
 *         System.out.printf&#40;&quot;Error occurred: %s%n&quot;, context.getException&#40;&#41;&#41;;
 *     &#125;
 * &#125;;
 *
 * &#47;&#47; Retrieve 'connectionString&#47;queueName' from your configuration.
 *
 * ServiceBusProcessorClient sessionProcessor = new ServiceBusClientBuilder&#40;&#41;
 *     .connectionString&#40;connectionString&#41;
 *     .sessionProcessor&#40;&#41;
 *     .queueName&#40;queueName&#41;
 *     .maxConcurrentSessions&#40;2&#41;
 *     .processMessage&#40;onMessage&#41;
 *     .processError&#40;onError&#41;
 *     .buildProcessorClient&#40;&#41;;
 *
 * &#47;&#47; Start the processor in the background
 * sessionProcessor.start&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.servicebusprocessorclient#session-instantiation -->
 *
 * @see ServiceBusProcessorClientBuilder
 * @see ServiceBusSessionProcessorClientBuilder
 */
public final class ServiceBusProcessorClient implements AutoCloseable {

    private static final int SCHEDULER_INTERVAL_IN_SECONDS = 10;
    private static final ClientLogger LOGGER = new ClientLogger(ServiceBusProcessorClient.class);
    private final ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder sessionReceiverBuilder;
    private final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder receiverBuilder;
    private final Consumer<ServiceBusReceivedMessageContext> processMessage;
    private final Consumer<ServiceBusErrorContext> processError;
    private final ServiceBusProcessorClientOptions processorOptions;
    // Use ConcurrentHashMap as a set because there is no ConcurrentHashSet.
    private final Map<Subscription, Subscription> receiverSubscriptions = new ConcurrentHashMap<>();
    private final AtomicReference<ServiceBusReceiverAsyncClient> asyncClient = new AtomicReference<>();
    private final AtomicBoolean isRunning = new AtomicBoolean();
    private final String queueName;
    private final String topicName;
    private final String subscriptionName;
    private final ServiceBusTracer tracer;
    private Disposable monitorDisposable;
    private boolean wasStopped = false;

    /**
     * Constructor to create a sessions-enabled processor.
     *
     * @param sessionReceiverBuilder The session processor builder to create new instances of async clients.
     * @param queueName The name of the queue this processor is associated with.
     * @param topicName The name of the topic this processor is associated with.
     * @param subscriptionName The name of the subscription this processor is associated with.
     * @param processMessage The message processing callback.
     * @param processError The error handler.
     * @param processorOptions Options to configure this instance of the processor.
     */
    ServiceBusProcessorClient(ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder sessionReceiverBuilder,
        String queueName, String topicName, String subscriptionName,
        Consumer<ServiceBusReceivedMessageContext> processMessage,
        Consumer<ServiceBusErrorContext> processError,
        ServiceBusProcessorClientOptions processorOptions) {
        this.sessionReceiverBuilder = Objects.requireNonNull(sessionReceiverBuilder,
            "'sessionReceiverBuilder' cannot be null");
        this.processMessage = Objects.requireNonNull(processMessage, "'processMessage' cannot be null");
        this.processError = Objects.requireNonNull(processError, "'processError' cannot be null");
        this.processorOptions = Objects.requireNonNull(processorOptions, "'processorOptions' cannot be null");

        ServiceBusReceiverAsyncClient client = sessionReceiverBuilder.buildAsyncClientForProcessor();
        this.asyncClient.set(client);
        this.receiverBuilder = null;
        this.queueName = queueName;
        this.topicName = topicName;
        this.subscriptionName = subscriptionName;
        this.tracer = client.getInstrumentation().getTracer();
    }

    /**
     * Constructor to create a processor.
     *
     * @param receiverBuilder The processor builder to create new instances of async clients.
     * @param queueName The name of the queue this processor is associated with.
     * @param topicName The name of the topic this processor is associated with.
     * @param subscriptionName The name of the subscription this processor is associated with.
     * @param processMessage The message processing callback.
     * @param processError The error handler.
     * @param processorOptions Options to configure this instance of the processor.
     */
    ServiceBusProcessorClient(ServiceBusClientBuilder.ServiceBusReceiverClientBuilder receiverBuilder,
        String queueName, String topicName, String subscriptionName,
        Consumer<ServiceBusReceivedMessageContext> processMessage,
        Consumer<ServiceBusErrorContext> processError, ServiceBusProcessorClientOptions processorOptions) {
        this.receiverBuilder = Objects.requireNonNull(receiverBuilder, "'receiverBuilder' cannot be null");
        this.processMessage = Objects.requireNonNull(processMessage, "'processMessage' cannot be null");
        this.processError = Objects.requireNonNull(processError, "'processError' cannot be null");
        this.processorOptions = Objects.requireNonNull(processorOptions, "'processorOptions' cannot be null");

        ServiceBusReceiverAsyncClient client = receiverBuilder.buildAsyncClient();
        this.asyncClient.set(client);
        this.sessionReceiverBuilder = null;
        this.queueName = queueName;
        this.topicName = topicName;
        this.subscriptionName = subscriptionName;
        this.tracer = client.getInstrumentation().getTracer();
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
            LOGGER.info("Processor is already running");
            return;
        }

        if (wasStopped) {
            wasStopped = false;
            LOGGER.warning("Starting Processor that was stopped before is not recommended, and this feature may be deprecated in the future. "
                + "Please close this processor instance and create a new one to restart processing. "
                + "Refer to the GitHub issue https://github.com/Azure/azure-sdk-for-java/issues/34464 for more details");
        }

        if (asyncClient.get() == null) {
            ServiceBusReceiverAsyncClient newReceiverClient = this.receiverBuilder == null
                ? this.sessionReceiverBuilder.buildAsyncClientForProcessor()
                : this.receiverBuilder.buildAsyncClient();
            asyncClient.set(newReceiverClient);
        }

        receiveMessages();

        // Start a monitor to periodically check if the client's connection is active.
        // NOTE: Schedulers.boundedElastic() is used here instead of Flux.interval() because the restart route involves
        // tearing down multiple levels of clients synchronously. The boundedElastic is used instead of the parallel
        // (parallel scheduler backing Flux.interval), so that we don't block any of the parallel threads.
        if (monitorDisposable == null) {
            monitorDisposable = Schedulers.boundedElastic().schedulePeriodically(() -> {
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
        wasStopped = true;
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
        if (monitorDisposable != null) {
            monitorDisposable.dispose();
            monitorDisposable = null;
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

    /**
     * Returns the queue name associated with this instance of {@link ServiceBusProcessorClient}.
     *
     * @return the queue name associated with this instance of {@link ServiceBusProcessorClient} or {@code null} if
     * the processor instance is for a topic and subscription.
     */
    public String getQueueName() {
        return this.queueName;
    }

    /**
     * Returns the topic name associated with this instance of {@link ServiceBusProcessorClient}.
     *
     * @return the topic name associated with this instance of {@link ServiceBusProcessorClient} or {@code null} if
     * the processor instance is for a queue.
     */
    public String getTopicName() {
        return this.topicName;
    }

    /**
     * Returns the subscription name associated with this instance of {@link ServiceBusProcessorClient}.
     *
     * @return the subscription name associated with this instance of {@link ServiceBusProcessorClient} or {@code null}
     * if the processor instance is for a queue.
     */
    public String getSubscriptionName() {
        return this.subscriptionName;
    }

    /**
     * Gets the identifier of the instance of {@link ServiceBusProcessorClient}.
     *
     * @return The identifier that can identify the instance of {@link ServiceBusProcessorClient}.
     */
    public synchronized String getIdentifier() {
        if (asyncClient.get() == null) {
            ServiceBusReceiverAsyncClient newReceiverClient = receiverBuilder == null
                ? sessionReceiverBuilder.buildAsyncClientForProcessor()
                : receiverBuilder.buildAsyncClient();
            asyncClient.set(newReceiverClient);
        }

        return asyncClient.get().getIdentifier();
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

                @SuppressWarnings("try")
                @Override
                public void onNext(ServiceBusMessageContext serviceBusMessageContext) {
                    try (AutoCloseable scope = tracer.makeSpanCurrent(serviceBusMessageContext.getMessage().getContext())) {
                        if (serviceBusMessageContext.hasError()) {
                            handleError(serviceBusMessageContext.getThrowable());
                        } else {
                            ServiceBusReceivedMessageContext serviceBusReceivedMessageContext =
                                new ServiceBusReceivedMessageContext(receiverClient, serviceBusMessageContext);

                            try {
                                processMessage.accept(serviceBusReceivedMessageContext);
                            } catch (Exception ex) {
                                serviceBusMessageContext.getMessage().setContext(
                                    serviceBusMessageContext.getMessage().getContext().addData(PROCESS_ERROR_KEY, ex));
                                handleError(new ServiceBusException(ex, ServiceBusErrorSource.USER_CALLBACK));

                                if (!processorOptions.isDisableAutoComplete()) {
                                    LOGGER.warning("Error when processing message. Abandoning message.", ex);
                                    abandonMessage(serviceBusMessageContext, receiverClient);
                                }
                            }
                        }
                        if (isRunning.get()) {
                            LOGGER.verbose("Requesting 1 more message from upstream");
                            subscription.request(1);
                        }
                    } catch (Exception e) {
                        LOGGER.verbose("Error disposing scope", e);
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    LOGGER.info("Error receiving messages.", throwable);
                    handleError(throwable);
                    if (isRunning.get()) {
                        restartMessageReceiver(subscription);
                    }
                }

                @Override
                public void onComplete() {
                    LOGGER.info("Completed receiving messages.");
                    if (isRunning.get()) {
                        restartMessageReceiver(subscription);
                    }
                }
            };
        }

        if (processorOptions.getMaxConcurrentCalls() > 1) {
            receiverClient.receiveMessagesWithContext()
                .parallel(processorOptions.getMaxConcurrentCalls(), 1)
                .runOn(Schedulers.boundedElastic(), 1)
                .subscribe(subscribers);
        } else {
            // For the default case, i.e., when max-concurrent-call is one, the Processor handler can be invoked on
            // the same Bounded-Elastic thread that the Low-Level Receiver obtained. This way, we can avoid
            // the unnecessary thread hopping and allocation that otherwise would have been introduced by the parallel
            // and runOn operators for this code path.
            receiverClient.receiveMessagesWithContext()
                .subscribe(subscribers[0]);
        }
    }

    private void abandonMessage(ServiceBusMessageContext serviceBusMessageContext,
        ServiceBusReceiverAsyncClient receiverClient) {
        try {
            receiverClient.abandon(serviceBusMessageContext.getMessage()).block();
        } catch (Exception exception) {
            LOGGER.verbose("Failed to abandon message", exception);
        }
    }

    private void handleError(Throwable throwable) {
        try {
            ServiceBusReceiverAsyncClient client = asyncClient.get();
            final String fullyQualifiedNamespace = client.getFullyQualifiedNamespace();
            final String entityPath = client.getEntityPath();
            processError.accept(new ServiceBusErrorContext(throwable, fullyQualifiedNamespace, entityPath));
        } catch (Exception ex) {
            LOGGER.verbose("Error from error handler. Ignoring error.", ex);
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
