// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.ServiceBusClientBuilder.ServiceBusProcessorClientBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder;
import com.azure.messaging.servicebus.implementation.ServiceBusProcessorClientOptions;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * The processor client for processing Service Bus messages. {@link ServiceBusProcessorClient} provides a push-based
 * mechanism that invokes the message processing callback when a message is received or the error handler when an error
 * occurs when receiving messages. A {@link ServiceBusProcessorClient} can be created to process messages for a
 * session-enabled or non session-enabled Service Bus entity. It supports auto-settlement of messages by default.
 *
 * <p><strong>Sample code to instantiate a processor client and receive in PeekLock mode</strong></p>
 * <!-- src_embed com.azure.messaging.servicebus.servicebusprocessorclient#receive-mode-peek-lock-instantiation -->
 * <pre>
 * &#47;&#47; Function that gets called whenever a message is received.
 * Consumer&lt;ServiceBusReceivedMessageContext&gt; processMessage = context -&gt; &#123;
 *     final ServiceBusReceivedMessage message = context.getMessage&#40;&#41;;
 *     &#47;&#47; Randomly complete or abandon each message. Ideally, in real-world scenarios, if the business logic
 *     &#47;&#47; handling message reaches desired state such that it doesn't require Service Bus to redeliver
 *     &#47;&#47; the same message, then context.complete&#40;&#41; should be called otherwise context.abandon&#40;&#41;.
 *     final boolean success = Math.random&#40;&#41; &lt; 0.5;
 *     if &#40;success&#41; &#123;
 *         try &#123;
 *             context.complete&#40;&#41;;
 *         &#125; catch &#40;RuntimeException error&#41; &#123;
 *             System.out.printf&#40;&quot;Completion of the message %s failed.%n Error: %s%n&quot;,
 *                 message.getMessageId&#40;&#41;, error&#41;;
 *         &#125;
 *     &#125; else &#123;
 *         try &#123;
 *             context.abandon&#40;&#41;;
 *         &#125; catch &#40;RuntimeException error&#41; &#123;
 *             System.out.printf&#40;&quot;Abandoning of the message %s failed.%nError: %s%n&quot;,
 *                 message.getMessageId&#40;&#41;, error&#41;;
 *         &#125;
 *     &#125;
 * &#125;;
 *
 * &#47;&#47; Sample code that gets called if there's an error
 * Consumer&lt;ServiceBusErrorContext&gt; processError = errorContext -&gt; &#123;
 *     if &#40;errorContext.getException&#40;&#41; instanceof ServiceBusException&#41; &#123;
 *         ServiceBusException exception = &#40;ServiceBusException&#41; errorContext.getException&#40;&#41;;
 *
 *         System.out.printf&#40;&quot;Error source: %s, reason %s%n&quot;, errorContext.getErrorSource&#40;&#41;,
 *             exception.getReason&#40;&#41;&#41;;
 *     &#125; else &#123;
 *         System.out.printf&#40;&quot;Error occurred: %s%n&quot;, errorContext.getException&#40;&#41;&#41;;
 *     &#125;
 * &#125;;
 *
 * TokenCredential tokenCredential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 *
 * &#47;&#47; Create the processor client via the builder and its sub-builder
 * &#47;&#47; 'fullyQualifiedNamespace' will look similar to &quot;&#123;your-namespace&#125;.servicebus.windows.net&quot;
 * ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder&#40;&#41;
 *     .credential&#40;fullyQualifiedNamespace, tokenCredential&#41;
 *     .processor&#40;&#41;
 *     .queueName&#40;queueName&#41;
 *     .receiveMode&#40;ServiceBusReceiveMode.PEEK_LOCK&#41;
 *     .disableAutoComplete&#40;&#41;  &#47;&#47; Make sure to explicitly opt in to manual settlement &#40;e.g. complete, abandon&#41;.
 *     .processMessage&#40;processMessage&#41;
 *     .processError&#40;processError&#41;
 *     .disableAutoComplete&#40;&#41;
 *     .buildProcessorClient&#40;&#41;;
 *
 * &#47;&#47; Starts the processor in the background. Control returns immediately.
 * processorClient.start&#40;&#41;;
 *
 * &#47;&#47; Stop processor and dispose when done processing messages.
 * processorClient.stop&#40;&#41;;
 * processorClient.close&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.servicebusprocessorclient#receive-mode-peek-lock-instantiation -->
 * <p><strong>Sample code to instantiate a processor client and receive in ReceiveAndDelete mode</strong></p>
 * <!-- src_embed com.azure.messaging.servicebus.servicebusprocessorclient#receive-mode-receive-and-delete-instantiation -->
 * <pre>
 * &#47;&#47; Function that gets called whenever a message is received.
 * Consumer&lt;ServiceBusReceivedMessageContext&gt; processMessage = context -&gt; &#123;
 *     final ServiceBusReceivedMessage message = context.getMessage&#40;&#41;;
 *     System.out.printf&#40;&quot;Processing message. Session: %s, Sequence #: %s. Contents: %s%n&quot;,
 *         message.getSessionId&#40;&#41;, message.getSequenceNumber&#40;&#41;, message.getBody&#40;&#41;&#41;;
 * &#125;;
 *
 * &#47;&#47; Sample code that gets called if there's an error
 * Consumer&lt;ServiceBusErrorContext&gt; processError = errorContext -&gt; &#123;
 *     if &#40;errorContext.getException&#40;&#41; instanceof ServiceBusException&#41; &#123;
 *         ServiceBusException exception = &#40;ServiceBusException&#41; errorContext.getException&#40;&#41;;
 *
 *         System.out.printf&#40;&quot;Error source: %s, reason %s%n&quot;, errorContext.getErrorSource&#40;&#41;,
 *             exception.getReason&#40;&#41;&#41;;
 *     &#125; else &#123;
 *         System.out.printf&#40;&quot;Error occurred: %s%n&quot;, errorContext.getException&#40;&#41;&#41;;
 *     &#125;
 * &#125;;
 *
 * TokenCredential tokenCredential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 *
 * &#47;&#47; Create the processor client via the builder and its sub-builder
 * &#47;&#47; 'fullyQualifiedNamespace' will look similar to &quot;&#123;your-namespace&#125;.servicebus.windows.net&quot;
 * &#47;&#47; 'disableAutoComplete&#40;&#41;' will opt in to manual settlement &#40;e.g. complete, abandon&#41;.
 * ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder&#40;&#41;
 *     .credential&#40;fullyQualifiedNamespace, tokenCredential&#41;
 *     .processor&#40;&#41;
 *     .queueName&#40;queueName&#41;
 *     .receiveMode&#40;ServiceBusReceiveMode.RECEIVE_AND_DELETE&#41;
 *     .processMessage&#40;processMessage&#41;
 *     .processError&#40;processError&#41;
 *     .disableAutoComplete&#40;&#41;
 *     .buildProcessorClient&#40;&#41;;
 *
 * &#47;&#47; Starts the processor in the background. Control returns immediately.
 * processorClient.start&#40;&#41;;
 *
 * &#47;&#47; Stop processor and dispose when done processing messages.
 * processorClient.stop&#40;&#41;;
 * processorClient.close&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.servicebusprocessorclient#receive-mode-receive-and-delete-instantiation -->
 * <p><strong>Create and run a session-enabled processor</strong></p>
 * <!-- src_embed com.azure.messaging.servicebus.servicebusprocessorclient#session-instantiation -->
 * <pre>
 * &#47;&#47; Function that gets called whenever a message is received.
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
 *
 *         System.out.printf&#40;&quot;Error source: %s, reason %s%n&quot;, context.getErrorSource&#40;&#41;,
 *             exception.getReason&#40;&#41;&#41;;
 *     &#125; else &#123;
 *         System.out.printf&#40;&quot;Error occurred: %s%n&quot;, context.getException&#40;&#41;&#41;;
 *     &#125;
 * &#125;;
 *
 * TokenCredential tokenCredential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 *
 * &#47;&#47; Create the processor client via the builder and its sub-builder
 * &#47;&#47; 'fullyQualifiedNamespace' will look similar to &quot;&#123;your-namespace&#125;.servicebus.windows.net&quot;
 * ServiceBusProcessorClient sessionProcessor = new ServiceBusClientBuilder&#40;&#41;
 *     .credential&#40;fullyQualifiedNamespace, tokenCredential&#41;
 *     .sessionProcessor&#40;&#41;
 *     .queueName&#40;sessionEnabledQueueName&#41;
 *     .receiveMode&#40;ServiceBusReceiveMode.PEEK_LOCK&#41;
 *     .disableAutoComplete&#40;&#41;
 *     .maxConcurrentSessions&#40;2&#41;
 *     .processMessage&#40;onMessage&#41;
 *     .processError&#40;onError&#41;
 *     .buildProcessorClient&#40;&#41;;
 *
 * &#47;&#47; Starts the processor in the background. Control returns immediately.
 * sessionProcessor.start&#40;&#41;;
 *
 * &#47;&#47; Stop processor and dispose when done processing messages.
 * sessionProcessor.stop&#40;&#41;;
 * sessionProcessor.close&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.servicebusprocessorclient#session-instantiation -->
 *
 * @see ServiceBusProcessorClientBuilder
 * @see ServiceBusSessionProcessorClientBuilder
 */
public final class ServiceBusProcessorClient implements AutoCloseable {
    private final ServiceBusProcessorClientOptions processorOptions;
    private final String queueName;
    private final String topicName;
    private final String subscriptionName;
    private final ServiceBusProcessor processor;

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
        Consumer<ServiceBusReceivedMessageContext> processMessage, Consumer<ServiceBusErrorContext> processError,
        ServiceBusProcessorClientOptions processorOptions) {
        Objects.requireNonNull(sessionReceiverBuilder, "'sessionReceiverBuilder' cannot be null");
        Objects.requireNonNull(processMessage, "'processMessage' cannot be null");
        Objects.requireNonNull(processError, "'processError' cannot be null");
        this.processorOptions = Objects.requireNonNull(processorOptions, "'processorOptions' cannot be null");
        this.queueName = queueName;
        this.topicName = topicName;
        this.subscriptionName = subscriptionName;
        final int concurrencyPerSession = this.processorOptions.getMaxConcurrentCalls();
        this.processor
            = new ServiceBusProcessor(sessionReceiverBuilder, processMessage, processError, concurrencyPerSession);
    }

    /**
     * Constructor to create a non-session processor.
     *
     * @param receiverBuilder The processor builder to create new instances of async clients.
     * @param queueName The name of the queue this processor is associated with.
     * @param topicName The name of the topic this processor is associated with.
     * @param subscriptionName The name of the subscription this processor is associated with.
     * @param processMessage The message processing callback.
     * @param processError The error handler.
     * @param processorOptions Options to configure this instance of the processor.
     */
    ServiceBusProcessorClient(ServiceBusClientBuilder.ServiceBusReceiverClientBuilder receiverBuilder, String queueName,
        String topicName, String subscriptionName, Consumer<ServiceBusReceivedMessageContext> processMessage,
        Consumer<ServiceBusErrorContext> processError, ServiceBusProcessorClientOptions processorOptions) {
        Objects.requireNonNull(receiverBuilder, "'receiverBuilder' cannot be null");
        Objects.requireNonNull(processMessage, "'processMessage' cannot be null");
        Objects.requireNonNull(processError, "'processError' cannot be null");
        this.processorOptions = Objects.requireNonNull(processorOptions, "'processorOptions' cannot be null");

        this.queueName = queueName;
        this.topicName = topicName;
        this.subscriptionName = subscriptionName;
        final int concurrency = this.processorOptions.getMaxConcurrentCalls();
        final boolean enableAutoDisposition = !this.processorOptions.isDisableAutoComplete();
        this.processor = new ServiceBusProcessor(receiverBuilder, processMessage, processError, concurrency,
            enableAutoDisposition);
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
        processor.start();
    }

    /**
     * Stops the message processing for this processor. The receiving links and sessions are kept active and this
     * processor can resume processing messages by calling {@link #start()} again.
     */
    public synchronized void stop() {
        processor.stop();
    }

    /**
     * Stops message processing and closes the processor. The receiving links and sessions are closed and calling
     * {@link #start()} will create a new processing cycle with new links and new sessions.
     */
    @Override
    public synchronized void close() {
        processor.close();
    }

    /**
     * Returns {@code true} if the processor is running. If the processor is stopped or closed, this method returns
     * {@code false}.
     *
     * @return {@code true} if the processor is running; {@code false} otherwise.
     */
    public synchronized boolean isRunning() {
        return processor.isRunning();
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
        return processor.getIdentifier();
    }
}
