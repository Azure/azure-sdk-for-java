// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.AmqpSession;
import com.azure.core.amqp.AmqpTransaction;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.CreditFlowMode;
import com.azure.core.amqp.implementation.MessageFlux;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.RequestResponseChannelClosedException;
import com.azure.core.amqp.implementation.RetryUtil;
import com.azure.core.amqp.implementation.StringUtil;
import com.azure.core.amqp.implementation.handler.DeliveryNotOnLinkException;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder;
import com.azure.messaging.servicebus.implementation.DispositionStatus;
import com.azure.messaging.servicebus.implementation.LockContainer;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.implementation.ServiceBusAmqpConnection;
import com.azure.messaging.servicebus.implementation.ServiceBusReceiveLink;
import com.azure.messaging.servicebus.implementation.ServiceBusReceiveLinkProcessor;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusReceiverInstrumentation;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusTracer;
import com.azure.messaging.servicebus.models.AbandonOptions;
import com.azure.messaging.servicebus.models.CompleteOptions;
import com.azure.messaging.servicebus.models.DeadLetterOptions;
import com.azure.messaging.servicebus.models.DeferOptions;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.azure.core.amqp.implementation.ClientConstants.ENTITY_PATH_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.LINK_NAME_KEY;
import static com.azure.core.util.FluxUtil.fluxError;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.messaging.servicebus.implementation.Messages.INVALID_OPERATION_DISPOSED_RECEIVER;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.DISPOSITION_STATUS_KEY;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.ENTITY_TYPE_KEY;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.LOCK_TOKEN_KEY;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.SEQUENCE_NUMBER_KEY;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.SESSION_ID_KEY;

/**
 * <p>An <b>asynchronous</b> receiver responsible for receiving {@link ServiceBusReceivedMessage messages} from an
 * Azure Service Bus queue or topic/subscription.</p>
 *
 * <p>The examples shown in this document use a credential object named DefaultAzureCredential for authentication,
 * which is appropriate for most scenarios, including local development and production environments. Additionally, we
 * recommend using
 * <a href="https://learn.microsoft.com/azure/active-directory/managed-identities-azure-resources/">managed identity</a>
 * for authentication in production environments. You can find more information on different ways of authenticating and
 * their corresponding credential types in the
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme">Azure Identity documentation"</a>.
 * </p>
 *
 * <p><strong>Sample: Creating a {@link ServiceBusReceiverAsyncClient}</strong></p>
 *
 * <p>The following code sample demonstrates the creation of the asynchronous client
 * {@link ServiceBusReceiverAsyncClient}.  The {@code fullyQualifiedNamespace} is the Service Bus namespace's host name.
 * It is listed under the "Essentials" panel after navigating to the Event Hubs Namespace via Azure Portal.
 * The credential used is {@code DefaultAzureCredential} because it combines commonly used credentials in deployment
 * and development and chooses the credential to used based on its running environment.
 * {@link ServiceBusReceiveMode#PEEK_LOCK} (the default receive mode) and
 * {@link ServiceBusClientBuilder.ServiceBusReceiverClientBuilder#disableAutoComplete() disableAutoComplete()} are
 * <strong>strongly</strong> recommended so users have control over message settlement.</p>
 *
 * <!-- src_embed com.azure.messaging.servicebus.servicebusreceiverasyncclient.instantiation -->
 * <pre>
 * TokenCredential credential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 *
 * &#47;&#47; 'fullyQualifiedNamespace' will look similar to &quot;&#123;your-namespace&#125;.servicebus.windows.net&quot;
 * &#47;&#47; 'disableAutoComplete' indicates that users will explicitly settle their message.
 * ServiceBusReceiverAsyncClient asyncReceiver = new ServiceBusClientBuilder&#40;&#41;
 *     .credential&#40;fullyQualifiedNamespace, credential&#41;
 *     .receiver&#40;&#41;
 *     .disableAutoComplete&#40;&#41;
 *     .queueName&#40;queueName&#41;
 *     .buildAsyncClient&#40;&#41;;
 *
 * &#47;&#47; When users are done with the receiver, dispose of the receiver.
 * &#47;&#47; Clients should be long-lived objects as they require resources
 * &#47;&#47; and time to establish a connection to the service.
 * asyncReceiver.close&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.servicebusreceiverasyncclient.instantiation -->
 *
 * <p><strong>Sample: Receive all messages from Service Bus resource</strong></p>
 *
 * <p>This returns an infinite stream of messages from Service Bus. The stream ends when the subscription is disposed
 * or other terminal scenarios. See {@link #receiveMessages()} for more information.</p>
 *
 * <!-- src_embed com.azure.messaging.servicebus.servicebusreceiverasyncclient.receiveMessages -->
 * <pre>
 * &#47;&#47; Keep a reference to `subscription`. When the program is finished receiving messages, call
 * &#47;&#47; subscription.dispose&#40;&#41;. This will stop fetching messages from the Service Bus.
 * &#47;&#47; Consider using Flux.usingWhen to scope the creation, usage, and cleanup of the receiver.
 * Disposable subscription = asyncReceiver.receiveMessages&#40;&#41;
 *     .flatMap&#40;message -&gt; &#123;
 *         System.out.printf&#40;&quot;Received Seq #: %s%n&quot;, message.getSequenceNumber&#40;&#41;&#41;;
 *         System.out.printf&#40;&quot;Contents of message as string: %s%n&quot;, message.getBody&#40;&#41;&#41;;
 *
 *         &#47;&#47; Explicitly settle the message using complete, abandon, defer, dead-letter, etc.
 *         if &#40;isMessageProcessed&#41; &#123;
 *             return asyncReceiver.complete&#40;message&#41;;
 *         &#125; else &#123;
 *             return asyncReceiver.abandon&#40;message&#41;;
 *         &#125;
 *     &#125;&#41;
 *     .subscribe&#40;unused -&gt; &#123;
 *     &#125;, error -&gt; System.out.println&#40;&quot;Error occurred: &quot; + error&#41;,
 *         &#40;&#41; -&gt; System.out.println&#40;&quot;Receiving complete.&quot;&#41;&#41;;
 *
 * &#47;&#47; When program ends, or you're done receiving all messages, dispose of the receiver.
 * &#47;&#47; Clients should be long-lived objects as they
 * &#47;&#47; require resources and time to establish a connection to the service.
 * asyncReceiver.close&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.servicebusreceiverasyncclient.receiveMessages -->
 *
 * <p><strong>Sample: Receive messages in {@link ServiceBusReceiveMode#RECEIVE_AND_DELETE} mode from a Service Bus
 * entity</strong></p>
 *
 * <p>The following code sample demonstrates the creation of the asynchronous client
 * {@link ServiceBusReceiverAsyncClient} using {@link ServiceBusReceiveMode#RECEIVE_AND_DELETE}.  The
 * {@code fullyQualifiedNamespace} is the Service Bus namespace's host name.  It is listed under the "Essentials" panel
 * after navigating to the Event Hubs Namespace via Azure Portal.  The credential used is {@code DefaultAzureCredential}
 * because it combines commonly used credentials in deployment  and development and chooses the credential to used based
 * on its running environment.  See {@link ServiceBusReceiveMode#RECEIVE_AND_DELETE} docs for more information about
 * receiving messages using this mode.</p>
 *
 * <!-- src_embed com.azure.messaging.servicebus.servicebusreceiverasyncclient.receiveWithReceiveAndDeleteMode -->
 * <pre>
 * TokenCredential credential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 *
 * &#47;&#47; Keep a reference to `subscription`. When the program is finished receiving messages, call
 * &#47;&#47; subscription.dispose&#40;&#41;. This will stop fetching messages from the Service Bus.
 * Disposable subscription = Flux.usingWhen&#40;
 *         Mono.fromCallable&#40;&#40;&#41; -&gt; &#123;
 *             &#47;&#47; Setting the receiveMode when creating the receiver enables receive and delete mode. By default,
 *             &#47;&#47; peek lock mode is used. In peek lock mode, users are responsible for settling messages.
 *             return new ServiceBusClientBuilder&#40;&#41;
 *                 .credential&#40;fullyQualifiedNamespace, credential&#41;
 *                 .receiver&#40;&#41;
 *                 .receiveMode&#40;ServiceBusReceiveMode.RECEIVE_AND_DELETE&#41;
 *                 .queueName&#40;queueName&#41;
 *                 .buildAsyncClient&#40;&#41;;
 *         &#125;&#41;, receiver -&gt; &#123;
 *             return receiver.receiveMessages&#40;&#41;;
 *         &#125;, receiver -&gt; &#123;
 *             return Mono.fromRunnable&#40;&#40;&#41; -&gt; receiver.close&#40;&#41;&#41;;
 *         &#125;&#41;
 *     .subscribe&#40;message -&gt; &#123;
 *             &#47;&#47; Messages received in RECEIVE_AND_DELETE mode do not have to be settled because they are automatically
 *             &#47;&#47; removed from the queue.
 *         System.out.printf&#40;&quot;Received Seq #: %s%n&quot;, message.getSequenceNumber&#40;&#41;&#41;;
 *         System.out.printf&#40;&quot;Contents of message as string: %s%n&quot;, message.getBody&#40;&#41;&#41;;
 *     &#125;,
 *         error -&gt; System.out.println&#40;&quot;Error occurred: &quot; + error&#41;,
 *         &#40;&#41; -&gt; System.out.println&#40;&quot;Receiving complete.&quot;&#41;&#41;;
 *
 * </pre>
 * <!-- end com.azure.messaging.servicebus.servicebusreceiverasyncclient.receiveWithReceiveAndDeleteMode -->
 *
 * <p><strong>Sample: Receive messages from a specific session</strong></p>
 *
 * <p>To fetch messages from a specific session, switch to {@link ServiceBusSessionReceiverClientBuilder} and
 * build the session receiver client. Use {@link ServiceBusSessionReceiverAsyncClient#acceptSession(String)} to create
 * a session-bound {@link ServiceBusReceiverAsyncClient}.  The sample assumes that Service Bus sessions were
 * <a href="https://learn.microsoft.com/azure/service-bus-messaging/enable-message-sessions">enabled at the time of
 * the queue creation</a>.</p>
 *
 * <!-- src_embed com.azure.messaging.servicebus.servicebusreceiverasyncclient.instantiation#sessionId -->
 * <pre>
 * TokenCredential credential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 *
 * &#47;&#47; 'fullyQualifiedNamespace' will look similar to &quot;&#123;your-namespace&#125;.servicebus.windows.net&quot;
 * &#47;&#47; 'disableAutoComplete' indicates that users will explicitly settle their message.
 * ServiceBusSessionReceiverAsyncClient sessionReceiver = new ServiceBusClientBuilder&#40;&#41;
 *     .credential&#40;fullyQualifiedNamespace, credential&#41;
 *     .sessionReceiver&#40;&#41;
 *     .disableAutoComplete&#40;&#41;
 *     .queueName&#40;sessionEnabledQueueName&#41;
 *     .buildAsyncClient&#40;&#41;;
 *
 * &#47;&#47; acceptSession&#40;String&#41; completes successfully with a receiver when &quot;&lt;&lt;my-session-id&gt;&gt;&quot; session is
 * &#47;&#47; successfully locked.
 * &#47;&#47; `Flux.usingWhen` is used, so we dispose of the receiver resource after `receiveMessages&#40;&#41;` and the settlement
 * &#47;&#47; operations complete.
 * &#47;&#47; `Mono.usingWhen` can also be used if the resource closure returns a single item.
 * Flux&lt;Void&gt; sessionMessages = Flux.usingWhen&#40;
 *     sessionReceiver.acceptSession&#40;&quot;&lt;&lt;my-session-id&gt;&gt;&quot;&#41;,
 *     receiver -&gt; &#123;
 *         &#47;&#47; Receive messages from &lt;&lt;my-session-id&gt;&gt; session.
 *         return receiver.receiveMessages&#40;&#41;.flatMap&#40;message -&gt; &#123;
 *             System.out.printf&#40;&quot;Received Sequence #: %s. Contents: %s%n&quot;, message.getSequenceNumber&#40;&#41;,
 *                 message.getBody&#40;&#41;&#41;;
 *
 *             &#47;&#47; Explicitly settle the message using complete, abandon, defer, dead-letter, etc.
 *             if &#40;isMessageProcessed&#41; &#123;
 *                 return receiver.complete&#40;message&#41;;
 *             &#125; else &#123;
 *                 return receiver.abandon&#40;message&#41;;
 *             &#125;
 *         &#125;&#41;;
 *     &#125;,
 *     receiver -&gt; Mono.fromRunnable&#40;&#40;&#41; -&gt; &#123;
 *         &#47;&#47; Dispose of resources.
 *         receiver.close&#40;&#41;;
 *         sessionReceiver.close&#40;&#41;;
 *     &#125;&#41;&#41;;
 *
 * &#47;&#47; When program ends, or you're done receiving all messages, the `subscription` can be disposed of. This code
 * &#47;&#47; is non-blocking and kicks off the operation.
 * Disposable subscription = sessionMessages.subscribe&#40;
 *     unused -&gt; &#123;
 *     &#125;, error -&gt; System.err.print&#40;&quot;Error receiving message from session: &quot; + error&#41;,
 *     &#40;&#41; -&gt; System.out.println&#40;&quot;Completed receiving from session.&quot;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.servicebusreceiverasyncclient.instantiation#sessionId -->
 *
 * <p><strong>Sample:  Receive messages from the first available session</strong></p>
 *
 * <p>To process messages from the first available session, switch to {@link ServiceBusSessionReceiverClientBuilder}
 * and build the session receiver client. Use
 * {@link ServiceBusSessionReceiverAsyncClient#acceptNextSession() acceptNextSession()} to find the first available
 * session to process messages from.</p>
 *
 * <!-- src_embed com.azure.messaging.servicebus.servicebusreceiverasyncclient.instantiation#nextsession -->
 * <pre>
 * TokenCredential credential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 *
 * &#47;&#47; 'fullyQualifiedNamespace' will look similar to &quot;&#123;your-namespace&#125;.servicebus.windows.net&quot;
 * &#47;&#47; 'disableAutoComplete' indicates that users will explicitly settle their message.
 * ServiceBusSessionReceiverAsyncClient sessionReceiver = new ServiceBusClientBuilder&#40;&#41;
 *     .credential&#40;fullyQualifiedNamespace, credential&#41;
 *     .sessionReceiver&#40;&#41;
 *     .disableAutoComplete&#40;&#41;
 *     .queueName&#40;sessionEnabledQueueName&#41;
 *     .buildAsyncClient&#40;&#41;;
 *
 * &#47;&#47; Creates a client to receive messages from the first available session. It waits until
 * &#47;&#47; AmqpRetryOptions.getTryTimeout&#40;&#41; elapses. If no session is available within that operation timeout, it
 * &#47;&#47; completes with a retriable error. Otherwise, a receiver is returned when a lock on the session is acquired.
 * Mono&lt;ServiceBusReceiverAsyncClient&gt; receiverMono = sessionReceiver.acceptNextSession&#40;&#41;;
 *
 * Flux&lt;Void&gt; receiveMessagesFlux = Flux.usingWhen&#40;receiverMono,
 *     receiver -&gt; receiver.receiveMessages&#40;&#41;.flatMap&#40;message -&gt; &#123;
 *         System.out.println&#40;&quot;Received message: &quot; + message.getBody&#40;&#41;&#41;;
 *
 *         &#47;&#47; Explicitly settle the message via complete, abandon, defer, dead-letter, etc.
 *         if &#40;isMessageProcessed&#41; &#123;
 *             return receiver.complete&#40;message&#41;;
 *         &#125; else &#123;
 *             return receiver.abandon&#40;message&#41;;
 *         &#125;
 *     &#125;&#41;,
 *     receiver -&gt; Mono.fromRunnable&#40;&#40;&#41; -&gt; &#123;
 *         &#47;&#47; Dispose of the receiver and sessionReceiver when done receiving messages.
 *         receiver.close&#40;&#41;;
 *         sessionReceiver.close&#40;&#41;;
 *     &#125;&#41;&#41;;
 *
 * &#47;&#47; This is a non-blocking call that moves onto the next line of code after setting up and starting the receive
 * &#47;&#47; operation. Customers can keep a reference to `subscription` and dispose of it when they want to stop
 * &#47;&#47; receiving messages.
 * Disposable subscription = receiveMessagesFlux.subscribe&#40;unused -&gt; &#123;
 * &#125;, error -&gt; System.out.println&#40;&quot;Error occurred: &quot; + error&#41;,
 *     &#40;&#41; -&gt; System.out.println&#40;&quot;Receiving complete.&quot;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.servicebusreceiverasyncclient.instantiation#nextsession -->
 *
 * <p><strong>Sample:  Rate limiting consumption of messages from a Service Bus entity</strong></p>
 *
 * <p>For message receivers that need to limit the number of messages they receive at a given time, they can use
 * {@link BaseSubscriber#request(long)}.</p>
 *
 * <!-- src_embed com.azure.messaging.servicebus.servicebusreceiverasyncclient.receive#basesubscriber -->
 * <pre>
 * &#47;&#47; This is a non-blocking call. The program will move to the next line of code after setting up the operation.
 * asyncReceiver.receiveMessages&#40;&#41;.subscribe&#40;new BaseSubscriber&lt;ServiceBusReceivedMessage&gt;&#40;&#41; &#123;
 *     private static final int NUMBER_OF_MESSAGES = 5;
 *     private final AtomicInteger currentNumberOfMessages = new AtomicInteger&#40;&#41;;
 *
 *     &#64;Override
 *     protected void hookOnSubscribe&#40;Subscription subscription&#41; &#123;
 *         &#47;&#47; Tell the Publisher we only want 5 message at a time.
 *         request&#40;NUMBER_OF_MESSAGES&#41;;
 *     &#125;
 *
 *     &#64;Override
 *     protected void hookOnNext&#40;ServiceBusReceivedMessage message&#41; &#123;
 *         &#47;&#47; Process the ServiceBusReceivedMessage
 *         &#47;&#47; If the number of messages we have currently received is a multiple of 5, that means we have reached
 *         &#47;&#47; the last message the Subscriber will provide to us. Invoking request&#40;long&#41; here, tells the Publisher
 *         &#47;&#47; that the subscriber is ready to get more messages from upstream.
 *         if &#40;currentNumberOfMessages.incrementAndGet&#40;&#41; % 5 == 0&#41; &#123;
 *             request&#40;NUMBER_OF_MESSAGES&#41;;
 *         &#125;
 *     &#125;
 * &#125;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.servicebusreceiverasyncclient.receive#basesubscriber -->
 *
 * @see ServiceBusClientBuilder
 * @see ServiceBusReceiverClient To communicate with a Service Bus resource using a synchronous client.
 */
@ServiceClient(builder = ServiceBusClientBuilder.class, isAsync = true)
public final class ServiceBusReceiverAsyncClient implements AutoCloseable {
    private static final Duration EXPIRED_RENEWAL_CLEANUP_INTERVAL = Duration.ofMinutes(2);
    private static final DeadLetterOptions DEFAULT_DEAD_LETTER_OPTIONS = new DeadLetterOptions();
    private static final String TRANSACTION_LINK_NAME = "coordinator";
    private static final ClientLogger LOGGER = new ClientLogger(ServiceBusReceiverAsyncClient.class);

    private final LockContainer<LockRenewalOperation> renewalContainer;
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final LockContainer<OffsetDateTime> managementNodeLocks;
    private final String fullyQualifiedNamespace;
    private final String entityPath;
    private final MessagingEntityType entityType;
    private final ReceiverOptions receiverOptions;
    private final ConnectionCacheWrapper connectionCacheWrapper;
    private final boolean isOnV2;
    private final Mono<ServiceBusAmqpConnection> connectionProcessor;
    private final ServiceBusReceiverInstrumentation instrumentation;
    private final ServiceBusTracer tracer;
    private final MessageSerializer messageSerializer;
    private final Runnable onClientClose;
    private final IServiceBusSessionManager sessionManager;
    private final boolean isSessionEnabled;
    private final Semaphore completionLock = new Semaphore(1);
    private final String identifier;

    // Starting at -1 because that is before the beginning of the stream.
    private final AtomicLong lastPeekedSequenceNumber = new AtomicLong(-1);
    private final AtomicReference<ServiceBusAsyncConsumer> consumer = new AtomicReference<>();
    private final AutoCloseable trackSettlementSequenceNumber;

    /**
     * Creates a receiver that listens to a Service Bus resource.
     *
     * @param fullyQualifiedNamespace The fully qualified domain name for the Service Bus resource.
     * @param entityPath The name of the topic or queue.
     * @param entityType The type of the Service Bus resource.
     * @param receiverOptions Options when receiving messages.
     * @param connectionCacheWrapper The AMQP connection to the Service Bus resource.
     * @param instrumentation ServiceBus tracing and metrics helper
     * @param messageSerializer Serializes and deserializes Service Bus messages.
     * @param onClientClose Operation to run when the client completes.
     */
    // Client to work with a non-session entity.
    ServiceBusReceiverAsyncClient(String fullyQualifiedNamespace, String entityPath, MessagingEntityType entityType,
        ReceiverOptions receiverOptions, ConnectionCacheWrapper connectionCacheWrapper, Duration cleanupInterval,
        ServiceBusReceiverInstrumentation instrumentation, MessageSerializer messageSerializer, Runnable onClientClose, String identifier) {
        this.fullyQualifiedNamespace = Objects.requireNonNull(fullyQualifiedNamespace,
            "'fullyQualifiedNamespace' cannot be null.");
        this.entityPath = Objects.requireNonNull(entityPath, "'entityPath' cannot be null.");
        this.entityType = Objects.requireNonNull(entityType, "'entityType' cannot be null.");
        this.receiverOptions = Objects.requireNonNull(receiverOptions, "'receiveOptions cannot be null.'");
        this.connectionCacheWrapper = Objects.requireNonNull(connectionCacheWrapper, "'connectionCacheWrapper' cannot be null.");
        this.connectionProcessor = this.connectionCacheWrapper.getConnection();
        this.instrumentation = Objects.requireNonNull(instrumentation, "'tracer' cannot be null");
        this.messageSerializer = Objects.requireNonNull(messageSerializer, "'messageSerializer' cannot be null.");
        this.onClientClose = Objects.requireNonNull(onClientClose, "'onClientClose' cannot be null.");
        this.sessionManager = null;
        if (receiverOptions.getSessionId() != null || receiverOptions.getMaxConcurrentSessions() != null) {
            // Assert the internal invariant for above 'sessionManager = null' i.e, session-unaware call-sites should not set these options.
            throw new IllegalStateException("Session-specific options are not expected to be present on a client for session unaware entity.");
        }
        this.isSessionEnabled = false;
        this.isOnV2 = this.connectionCacheWrapper.isV2();

        this.managementNodeLocks = new LockContainer<OffsetDateTime>(cleanupInterval);
        final Consumer<LockRenewalOperation> onExpired = renewal -> {
            LOGGER.atVerbose()
                .addKeyValue(LOCK_TOKEN_KEY, renewal.getLockToken())
                .addKeyValue("status", renewal.getStatus())
                .log("Closing expired renewal operation.", renewal.getThrowable());
            renewal.close();
        };
        this.renewalContainer = new LockContainer<LockRenewalOperation>(EXPIRED_RENEWAL_CLEANUP_INTERVAL, onExpired);

        this.identifier = identifier;
        this.tracer = instrumentation.getTracer();
        this.trackSettlementSequenceNumber = instrumentation.startTrackingSettlementSequenceNumber();
    }

    // Client to work with a session-enabled entity (client to receive from one-session (V1, V2) or receive from multiple-sessions (V1)).
    ServiceBusReceiverAsyncClient(String fullyQualifiedNamespace, String entityPath, MessagingEntityType entityType,
                                  ReceiverOptions receiverOptions, ConnectionCacheWrapper connectionCacheWrapper, Duration cleanupInterval,
                                  ServiceBusReceiverInstrumentation instrumentation, MessageSerializer messageSerializer, Runnable onClientClose,
                                  IServiceBusSessionManager sessionManager) {
        this.fullyQualifiedNamespace = Objects.requireNonNull(fullyQualifiedNamespace,
            "'fullyQualifiedNamespace' cannot be null.");
        this.entityPath = Objects.requireNonNull(entityPath, "'entityPath' cannot be null.");
        this.entityType = Objects.requireNonNull(entityType, "'entityType' cannot be null.");
        this.receiverOptions = Objects.requireNonNull(receiverOptions, "'receiveOptions cannot be null.'");
        this.connectionCacheWrapper = Objects.requireNonNull(connectionCacheWrapper, "'connectionCacheWrapper' cannot be null.");
        this.connectionProcessor = this.connectionCacheWrapper.getConnection();
        this.instrumentation = Objects.requireNonNull(instrumentation, "'tracer' cannot be null");
        this.messageSerializer = Objects.requireNonNull(messageSerializer, "'messageSerializer' cannot be null.");
        this.onClientClose = Objects.requireNonNull(onClientClose, "'onClientClose' cannot be null.");
        this.sessionManager = Objects.requireNonNull(sessionManager, "'sessionManager' cannot be null.");
        this.isSessionEnabled = true;
        this.isOnV2 = this.connectionCacheWrapper.isV2();
        final boolean isV2SessionManager = this.sessionManager instanceof ServiceBusSingleSessionManager;
        // Once side-by-side support for V1 is no longer needed, we'll directly use the "ServiceBusSingleSessionManager" type
        // in the constructor and IServiceBusSessionManager interface will be deleted (so excuse the temporary 'I' prefix
        // used to avoid type conflict with V1 ServiceBusSessionManager. The V1 ServiceBusSessionManager will also be deleted
        // once side-by-side support is no longer needed).
        if (isOnV2 ^ isV2SessionManager) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("For V2 Session, the manager should be ServiceBusSingleSessionManager, and ConnectionCache should be on V2."));
        }

        this.managementNodeLocks = new LockContainer<OffsetDateTime>(cleanupInterval);
        final Consumer<LockRenewalOperation> onExpired = renewal -> {
            LOGGER.atInfo()
                .addKeyValue(SESSION_ID_KEY, renewal.getSessionId())
                .addKeyValue("status", renewal.getStatus())
                .log("Closing expired renewal operation.", renewal.getThrowable());
            renewal.close();
        };
        this.renewalContainer = new LockContainer<LockRenewalOperation>(EXPIRED_RENEWAL_CLEANUP_INTERVAL, onExpired);

        this.identifier = sessionManager.getIdentifier();
        this.tracer = instrumentation.getTracer();
        this.trackSettlementSequenceNumber = instrumentation.startTrackingSettlementSequenceNumber();
    }

    /**
     * Gets the fully qualified Service Bus namespace that the connection is associated with. This is likely similar to
     * {@code {yournamespace}.servicebus.windows.net}.
     *
     * @return The fully qualified Service Bus namespace that the connection is associated with.
     */
    public String getFullyQualifiedNamespace() {
        return fullyQualifiedNamespace;
    }

    /**
     * Gets the Service Bus resource this client interacts with.
     *
     * @return The Service Bus resource this client interacts with.
     */
    public String getEntityPath() {
        return entityPath;
    }

    /**
     * Gets the SessionId of the session if this receiver is a session receiver.
     *
     * @return The SessionId or null if this is not a session receiver.
     */
    public String getSessionId() {
        return receiverOptions.getSessionId();
    }

    /**
     * Gets the identifier of the instance of {@link ServiceBusReceiverAsyncClient}.
     *
     * @return The identifier that can identify the instance of {@link ServiceBusReceiverAsyncClient}.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Abandons a {@link ServiceBusReceivedMessage message}. This will make the message available again for processing.
     * Abandoning a message will increase the delivery count on the message.
     *
     * @param message The {@link ServiceBusReceivedMessage} to perform this operation.
     *
     * @return A {@link Mono} that completes when the Service Bus abandon operation completes.
     *
     * @throws NullPointerException if {@code message} is null.
     * @throws UnsupportedOperationException if the receiver was opened in
     *     {@link ServiceBusReceiveMode#RECEIVE_AND_DELETE} mode or if the message was received from
     *     {@link ServiceBusReceiverAsyncClient#peekMessage() peekMessage}.
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if the message could not be abandoned.
     * @throws IllegalArgumentException if the message has either been deleted or already settled.
     */
    public Mono<Void> abandon(ServiceBusReceivedMessage message) {
        return updateDisposition(message, DispositionStatus.ABANDONED, null, null,
            null, null);
    }

    /**
     * Abandons a {@link ServiceBusReceivedMessage message} updates the message's properties. This will make the
     * message available again for processing. Abandoning a message will increase the delivery count on the message.
     *
     * @param message The {@link ServiceBusReceivedMessage} to perform this operation.
     * @param options The options to set while abandoning the message.
     *
     * @return A {@link Mono} that completes when the Service Bus operation finishes.
     *
     * @throws NullPointerException if {@code message} or {@code options} is null. Also if
     *     {@code transactionContext.transactionId} is null when {@code options.transactionContext} is specified.
     * @throws UnsupportedOperationException if the receiver was opened in
     *     {@link ServiceBusReceiveMode#RECEIVE_AND_DELETE} mode or if the message was received from
     *     {@link ServiceBusReceiverAsyncClient#peekMessage() peekMessage}.
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if the message could not be abandoned.
     * @throws IllegalArgumentException if the message has either been deleted or already settled.
     */
    public Mono<Void> abandon(ServiceBusReceivedMessage message, AbandonOptions options) {
        if (Objects.isNull(options)) {
            return monoError(LOGGER, new NullPointerException("'settlementOptions' cannot be null."));
        } else if (!Objects.isNull(options.getTransactionContext())
            && Objects.isNull(options.getTransactionContext().getTransactionId())) {
            return monoError(LOGGER, new NullPointerException(
                "'options.transactionContext.transactionId' cannot be null."));
        }

        return updateDisposition(message, DispositionStatus.ABANDONED, null, null,
            options.getPropertiesToModify(), options.getTransactionContext());
    }

    /**
     * Completes a {@link ServiceBusReceivedMessage message}. This will delete the message from the service.
     *
     * @param message The {@link ServiceBusReceivedMessage} to perform this operation.
     *
     * @return A {@link Mono} that finishes when the message is completed on Service Bus.
     *
     * @throws NullPointerException if {@code message} is null.
     * @throws UnsupportedOperationException if the receiver was opened in
     *     {@link ServiceBusReceiveMode#RECEIVE_AND_DELETE} mode or if the message was received from
     *     {@link ServiceBusReceiverAsyncClient#peekMessage() peekMessage}.
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if the message could not be completed.
     * @throws IllegalArgumentException if the message has either been deleted or already settled.
     */
    public Mono<Void> complete(ServiceBusReceivedMessage message) {
        return updateDisposition(message, DispositionStatus.COMPLETED, null, null,
            null, null);
    }

    /**
     * Completes a {@link ServiceBusReceivedMessage message} with the given options. This will delete the message from
     * the service.
     *
     * @param message The {@link ServiceBusReceivedMessage} to perform this operation.
     * @param options Options used to complete the message.
     *
     * @return A {@link Mono} that finishes when the message is completed on Service Bus.
     *
     * @throws NullPointerException if {@code message} or {@code options} is null. Also if
     *     {@code transactionContext.transactionId} is null when {@code options.transactionContext} is specified.
     * @throws UnsupportedOperationException if the receiver was opened in
     *     {@link ServiceBusReceiveMode#RECEIVE_AND_DELETE} mode or if the message was received from
     *     {@link ServiceBusReceiverAsyncClient#peekMessage() peekMessage}.
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if the message could not be completed.
     * @throws IllegalArgumentException if the message has either been deleted or already settled.
     */
    public Mono<Void> complete(ServiceBusReceivedMessage message, CompleteOptions options) {
        if (Objects.isNull(options)) {
            return monoError(LOGGER, new NullPointerException("'options' cannot be null."));
        } else if (!Objects.isNull(options.getTransactionContext())
            && Objects.isNull(options.getTransactionContext().getTransactionId())) {
            return monoError(LOGGER, new NullPointerException(
                "'options.transactionContext.transactionId' cannot be null."));
        }

        return updateDisposition(message, DispositionStatus.COMPLETED, null, null,
            null, options.getTransactionContext());
    }

    /**
     * Defers a {@link ServiceBusReceivedMessage message}. This will move message into the deferred sub-queue.
     *
     * @param message The {@link ServiceBusReceivedMessage} to perform this operation.
     *
     * @return A {@link Mono} that completes when the Service Bus defer operation finishes.
     *
     * @throws NullPointerException if {@code message} is null.
     * @throws UnsupportedOperationException if the receiver was opened in
     *     {@link ServiceBusReceiveMode#RECEIVE_AND_DELETE} mode or if the message was received from
     *     {@link ServiceBusReceiverAsyncClient#peekMessage() peekMessage}.
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if the message could not be deferred.
     * @throws IllegalArgumentException if the message has either been deleted or already settled.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-deferral">Message deferral</a>
     */
    public Mono<Void> defer(ServiceBusReceivedMessage message) {
        return updateDisposition(message, DispositionStatus.DEFERRED, null, null,
            null, null);
    }

    /**
     * Defers a {@link ServiceBusReceivedMessage message} with the options set. This will move message into
     * the deferred sub-queue.
     *
     * @param message The {@link ServiceBusReceivedMessage} to perform this operation.
     * @param options Options used to defer the message.
     *
     * @return A {@link Mono} that completes when the defer operation finishes.
     *
     * @throws NullPointerException if {@code message} or {@code options} is null. Also if
     *     {@code transactionContext.transactionId} is null when {@code options.transactionContext} is specified.
     * @throws UnsupportedOperationException if the receiver was opened in
     *     {@link ServiceBusReceiveMode#RECEIVE_AND_DELETE} mode or if the message was received from
     *     {@link ServiceBusReceiverAsyncClient#peekMessage() peekMessage}.
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if the message could not be deferred.
     * @throws IllegalArgumentException if the message has either been deleted or already settled.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-deferral">Message deferral</a>
     */
    public Mono<Void> defer(ServiceBusReceivedMessage message, DeferOptions options) {
        if (Objects.isNull(options)) {
            return monoError(LOGGER, new NullPointerException("'options' cannot be null."));
        } else if (!Objects.isNull(options.getTransactionContext())
            && Objects.isNull(options.getTransactionContext().getTransactionId())) {
            return monoError(LOGGER, new NullPointerException(
                "'options.transactionContext.transactionId' cannot be null."));
        }

        return updateDisposition(message, DispositionStatus.DEFERRED, null, null,
            options.getPropertiesToModify(), options.getTransactionContext());
    }

    /**
     * Moves a {@link ServiceBusReceivedMessage message} to the dead-letter sub-queue.
     *
     * @param message The {@link ServiceBusReceivedMessage} to perform this operation.
     *
     * @return A {@link Mono} that completes when the dead letter operation finishes.
     *
     * @throws NullPointerException if {@code message} is null.
     * @throws UnsupportedOperationException if the receiver was opened in
     *     {@link ServiceBusReceiveMode#RECEIVE_AND_DELETE} mode or if the message was received from
     *     {@link ServiceBusReceiverAsyncClient#peekMessage() peekMessage}.
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if the message could not be dead-lettered.
     * @throws IllegalArgumentException if the message has either been deleted or already settled.
     *
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-dead-letter-queues">Dead letter
     *     queues</a>
     */
    public Mono<Void> deadLetter(ServiceBusReceivedMessage message) {
        return deadLetter(message, DEFAULT_DEAD_LETTER_OPTIONS);
    }

    /**
     * Moves a {@link ServiceBusReceivedMessage message} to the dead-letter sub-queue with the given options.
     *
     * @param message The {@link ServiceBusReceivedMessage} to perform this operation.
     * @param options Options used to dead-letter the message.
     *
     * @return A {@link Mono} that completes when the dead letter operation finishes.
     *
     * @throws NullPointerException if {@code message} or {@code options} is null. Also if
     *     {@code transactionContext.transactionId} is null when {@code options.transactionContext} is specified.
     * @throws UnsupportedOperationException if the receiver was opened in
     *     {@link ServiceBusReceiveMode#RECEIVE_AND_DELETE} mode or if the message was received from
     *     {@link ServiceBusReceiverAsyncClient#peekMessage() peekMessage}.
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if the message could not be dead-lettered.
     * @throws IllegalArgumentException if the message has either been deleted or already settled.
     *
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/service-bus-dead-letter-queues">Dead letter
     *     queues</a>
     */
    public Mono<Void> deadLetter(ServiceBusReceivedMessage message, DeadLetterOptions options) {
        if (Objects.isNull(options)) {
            return monoError(LOGGER, new NullPointerException("'options' cannot be null."));
        } else if (!Objects.isNull(options.getTransactionContext())
            && Objects.isNull(options.getTransactionContext().getTransactionId())) {
            return monoError(LOGGER, new NullPointerException(
                "'options.transactionContext.transactionId' cannot be null."));
        }
        return updateDisposition(message, DispositionStatus.SUSPENDED, options.getDeadLetterReason(),
            options.getDeadLetterErrorDescription(), options.getPropertiesToModify(),
            options.getTransactionContext());
    }

    /**
     * Gets the state of the session if this receiver is a session receiver.
     *
     * @return The session state or an empty Mono if there is no state set for the session.
     * @throws IllegalStateException if the receiver is a non-session receiver or receiver is already closed.
     * @throws ServiceBusException if the session state could not be acquired.
     */
    public Mono<byte[]> getSessionState() {
        return getSessionState(receiverOptions.getSessionId());
    }

    /**
     * Reads the next active message without changing the state of the receiver or the message source. The first call to
     * {@code peek()} fetches the first active message for this receiver. Each subsequent call fetches the subsequent
     * message in the entity.
     *
     * @return A peeked {@link ServiceBusReceivedMessage}.
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if an error occurs while peeking at the message.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-browsing">Message browsing</a>
     */
    public Mono<ServiceBusReceivedMessage> peekMessage() {
        return peekMessage(receiverOptions.getSessionId());
    }

    /**
     * Reads the next active message without changing the state of the receiver or the message source. The first call to
     * {@code peek()} fetches the first active message for this receiver. Each subsequent call fetches the subsequent
     * message in the entity.
     *
     * @param sessionId Session id of the message to peek from. {@code null} if there is no session.
     *
     * @return A peeked {@link ServiceBusReceivedMessage}.
     * @throws IllegalStateException if the receiver is disposed.
     * @throws ServiceBusException if an error occurs while peeking at the message.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-browsing">Message browsing</a>
     */
    Mono<ServiceBusReceivedMessage> peekMessage(String sessionId) {
        if (isDisposed.get()) {
            return monoError(LOGGER, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, "peek")));
        }

        Mono<ServiceBusReceivedMessage> result = connectionProcessor
                .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
                .flatMap(channel -> {
                    final long sequence = lastPeekedSequenceNumber.get() + 1;

                    LOGGER.atVerbose()
                        .addKeyValue(SEQUENCE_NUMBER_KEY, sequence)
                        .log("Peek message.");

                    return channel.peek(sequence, sessionId, getLinkName(sessionId));
                })
                .onErrorMap(throwable -> mapError(throwable, ServiceBusErrorSource.RECEIVE))
            .handle((message, sink) -> {
                final long current = lastPeekedSequenceNumber
                    .updateAndGet(value -> Math.max(value, message.getSequenceNumber()));

                LOGGER.atVerbose()
                    .addKeyValue(SEQUENCE_NUMBER_KEY, current)
                    .log("Updating last peeked sequence number.");

                sink.next(message);
            });

        return tracer.traceManagementReceive("ServiceBus.peekMessage", result);
    }

    /**
     * Starting from the given sequence number, reads next the active message without changing the state of the receiver
     * or the message source.
     *
     * @param sequenceNumber The sequence number from where to read the message.
     *
     * @return A peeked {@link ServiceBusReceivedMessage}.
     *
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if an error occurs while peeking at the message.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-browsing">Message browsing</a>
     */
    public Mono<ServiceBusReceivedMessage> peekMessage(long sequenceNumber) {
        return peekMessage(sequenceNumber, receiverOptions.getSessionId());
    }

    /**
     * Starting from the given sequence number, reads next the active message without changing the state of the receiver
     * or the message source.
     *
     * @param sequenceNumber The sequence number from where to read the message.
     * @param sessionId Session id of the message to peek from. {@code null} if there is no session.
     *
     * @return A peeked {@link ServiceBusReceivedMessage}.
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if an error occurs while peeking at the message.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-browsing">Message browsing</a>
     */
    Mono<ServiceBusReceivedMessage> peekMessage(long sequenceNumber, String sessionId) {
        if (isDisposed.get()) {
            return monoError(LOGGER, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, "peekAt")));
        }

        return tracer.traceManagementReceive("ServiceBus.peekMessage",
            connectionProcessor
                .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
                .flatMap(node -> node.peek(sequenceNumber, sessionId, getLinkName(sessionId)))
                .onErrorMap(throwable -> mapError(throwable, ServiceBusErrorSource.RECEIVE)));
    }

    /**
     * Reads the next batch of active messages without changing the state of the receiver or the message source.
     *
     * @param maxMessages The number of messages.
     *
     * @return A {@link Flux} of {@link ServiceBusReceivedMessage messages} that are peeked.
     *
     * @throws IllegalArgumentException if {@code maxMessages} is not a positive integer.
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if an error occurs while peeking at messages.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-browsing">Message browsing</a>
     */
    public Flux<ServiceBusReceivedMessage> peekMessages(int maxMessages) {
        return tracer.traceSyncReceive("ServiceBus.peekMessages",
            peekMessages(maxMessages, receiverOptions.getSessionId()));
    }

    /**
     * Reads the next batch of active messages without changing the state of the receiver or the message source.
     *
     * @param maxMessages The number of messages.
     * @param sessionId Session id of the messages to peek from. {@code null} if there is no session.
     *
     * @return An {@link IterableStream} of {@link ServiceBusReceivedMessage messages} that are peeked.
     * @throws IllegalArgumentException if {@code maxMessages} is not a positive integer.
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if an error occurs while peeking at messages.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-browsing">Message browsing</a>
     */
    Flux<ServiceBusReceivedMessage> peekMessages(int maxMessages, String sessionId) {
        if (isDisposed.get()) {
            return fluxError(LOGGER, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, "peekBatch")));
        }

        if (maxMessages <= 0) {
            return fluxError(LOGGER, new IllegalArgumentException("'maxMessages' is not positive."));
        }

        return
            connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMapMany(node -> {
                final long nextSequenceNumber = lastPeekedSequenceNumber.get() + 1;
                LOGGER.atVerbose().addKeyValue(SEQUENCE_NUMBER_KEY, nextSequenceNumber).log("Peek batch.");

                return node
                    .peek(nextSequenceNumber, sessionId, getLinkName(sessionId), maxMessages)
                    .doOnNext(next -> {
                        final long current = lastPeekedSequenceNumber
                            .updateAndGet(value -> Math.max(value, next.getSequenceNumber()));

                        LOGGER.atVerbose()
                            .addKeyValue(SEQUENCE_NUMBER_KEY, current)
                            .log("Last peeked sequence number in batch.");
                    });
            })
            .onErrorMap(throwable -> mapError(throwable, ServiceBusErrorSource.RECEIVE));
    }

    /**
     * Starting from the given sequence number, reads the next batch of active messages without changing the state of
     * the receiver or the message source.
     *
     * @param maxMessages The number of messages.
     * @param sequenceNumber The sequence number from where to start reading messages.
     *
     * @return A {@link Flux} of {@link ServiceBusReceivedMessage messages} peeked.
     *
     * @throws IllegalArgumentException if {@code maxMessages} is not a positive integer.
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if an error occurs while peeking at messages.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-browsing">Message browsing</a>
     */
    public Flux<ServiceBusReceivedMessage> peekMessages(int maxMessages, long sequenceNumber) {
        return peekMessages(maxMessages, sequenceNumber, receiverOptions.getSessionId());
    }

    /**
     * Starting from the given sequence number, reads the next batch of active messages without changing the state of
     * the receiver or the message source.
     *
     * @param maxMessages The number of messages.
     * @param sequenceNumber The sequence number from where to start reading messages.
     * @param sessionId Session id of the messages to peek from. {@code null} if there is no session.
     *
     * @return An {@link IterableStream} of {@link ServiceBusReceivedMessage} peeked.
     * @throws IllegalArgumentException if {@code maxMessages} is not a positive integer.
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if an error occurs while peeking at messages.
     * @see <a href="https://docs.microsoft.com/azure/service-bus-messaging/message-browsing">Message browsing</a>
     */
    Flux<ServiceBusReceivedMessage> peekMessages(int maxMessages, long sequenceNumber, String sessionId) {
        if (isDisposed.get()) {
            return fluxError(LOGGER, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, "peekBatchAt")));
        }
        if (maxMessages <= 0) {
            return fluxError(LOGGER, new IllegalArgumentException("'maxMessages' is not positive."));
        }

        return tracer.traceSyncReceive("ServiceBus.peekMessages",
            connectionProcessor
                .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
                .flatMapMany(node -> node.peek(sequenceNumber, sessionId, getLinkName(sessionId), maxMessages))
                .onErrorMap(throwable -> mapError(throwable, ServiceBusErrorSource.RECEIVE)));
    }

    /**
     * Receives an <b>infinite</b> stream of {@link ServiceBusReceivedMessage messages} from the Service Bus entity.
     * This Flux continuously receives messages from a Service Bus entity until either:
     *
     * <ul>
     *     <li>The receiver is closed.</li>
     *     <li>The subscription to the Flux is disposed.</li>
     *     <li>A terminal signal from a downstream subscriber is propagated upstream (ie. {@link Flux#take(long)} or
     *     {@link Flux#take(Duration)}).</li>
     *     <li>An {@link AmqpException} occurs that causes the receive link to stop.</li>
     * </ul>
     *
     * <p>
     * The client uses an AMQP link underneath to receive the messages; the client will transparently transition to
     * a new AMQP link if the current one encounters a retriable error. When the client experiences a non-retriable error
     * or exhausts the retries, the Subscriber's {@link org.reactivestreams.Subscriber#onError(Throwable)} terminal handler
     * will be notified with this error. No further messages will be delivered to {@link org.reactivestreams.Subscriber#onNext(Object)}
     * after the terminal event; the application must create a new client to resume the receive. Re-subscribing to the Flux
     * of the old client will have no effect.
     * </p>
     * <p>
     * Note: A few examples of non-retriable errors are - the application attempting to connect to a queue that does not
     * exist, deleting or disabling the queue in the middle of receiving, the user explicitly initiating Geo-DR.
     * These are certain events where the Service Bus communicates to the client that a non-retriable error occurred.
     * </p>
     *
     * @return An <b>infinite</b> stream of messages from the Service Bus entity.
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if an error occurs while receiving messages.
     */
    public Flux<ServiceBusReceivedMessage> receiveMessages() {
        if (isDisposed.get()) {
            return fluxError(LOGGER, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, "receiveMessages")));
        }
        if (isOnV2) {
            if (isSessionEnabled) {
                return sessionReactiveReceiveV2();
            } else {
                return nonSessionReactiveReceiveV2();
            }
        }
        // Without limitRate(), if the user calls receiveMessages().subscribe(), it will call
        // ServiceBusReceiveLinkProcessor.request(long request) where request = Long.MAX_VALUE.
        // We turn this one-time non-backpressure request to continuous requests with backpressure.
        // If receiverOptions.prefetchCount is set to non-zero, it will be passed to ServiceBusReceiveLinkProcessor
        // to auto-refill the prefetch buffer. A request will retrieve one message from this buffer.
        // If receiverOptions.prefetchCount is 0 (default value),
        // the request will add a link credit so one message is retrieved from the service.
        return receiveMessagesNoBackPressure().limitRate(1, 0);
    }

    @SuppressWarnings("try")
    Flux<ServiceBusReceivedMessage> receiveMessagesNoBackPressure() {
        return receiveMessagesWithContext(0)
                .handle((serviceBusMessageContext, sink) -> {
                    if (serviceBusMessageContext.hasError()) {
                        sink.error(serviceBusMessageContext.getThrowable());
                        return;
                    }
                    sink.next(serviceBusMessageContext.getMessage());
                });
    }

    /**
     * Receives an <b>infinite</b> stream of {@link ServiceBusReceivedMessage messages} from the Service Bus entity.
     * This Flux continuously receives messages from a Service Bus entity until either:
     *
     * <ul>
     *     <li>The receiver is closed.</li>
     *     <li>The subscription to the Flux is disposed.</li>
     *     <li>A terminal signal from a downstream subscriber is propagated upstream (ie. {@link Flux#take(long)} or
     *     {@link Flux#take(Duration)}).</li>
     *     <li>An {@link AmqpException} occurs that causes the receive link to stop.</li>
     * </ul>
     *
     * @return An <b>infinite</b> stream of messages from the Service Bus entity.
     */
    Flux<ServiceBusMessageContext> receiveMessagesWithContext() {
        return receiveMessagesWithContext(1);
    }

    private Flux<ServiceBusMessageContext> receiveMessagesWithContext(int highTide) {
        final Flux<ServiceBusMessageContext> messageFlux = sessionManager != null
            ? sessionManager.receive()
            : getOrCreateConsumer().receive().map(ServiceBusMessageContext::new);

        final Flux<ServiceBusMessageContext> messageFluxWithTracing = new FluxTrace(messageFlux, instrumentation);
        final Flux<ServiceBusMessageContext> withAutoLockRenewal;

        if (!isSessionEnabled && receiverOptions.isAutoLockRenewEnabled()) {
            withAutoLockRenewal = new FluxAutoLockRenew(messageFluxWithTracing, receiverOptions,
                renewalContainer, this::renewMessageLock, tracer);
        } else {
            withAutoLockRenewal = messageFluxWithTracing;
        }

        Flux<ServiceBusMessageContext> result;
        if (receiverOptions.isEnableAutoComplete()) {
            result = new FluxAutoComplete(withAutoLockRenewal, completionLock,
                context -> context.getMessage() != null ? complete(context.getMessage()) : Mono.empty(),
                context -> context.getMessage() != null ? abandon(context.getMessage()) : Mono.empty());
        } else {
            result = withAutoLockRenewal;
        }

        if (highTide > 0) {
            result = result.limitRate(highTide, 0);
        }

        return result
            .onErrorMap(throwable -> mapError(throwable, ServiceBusErrorSource.RECEIVE));
    }

    /**
     * Receives a deferred {@link ServiceBusReceivedMessage message}. Deferred messages can only be received by using
     * sequence number.
     *
     * @param sequenceNumber The {@link ServiceBusReceivedMessage#getSequenceNumber() sequence number} of the
     *     message.
     *
     * @return A deferred message with the matching {@code sequenceNumber}.
     *
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if deferred message cannot be received.
     */
    public Mono<ServiceBusReceivedMessage> receiveDeferredMessage(long sequenceNumber) {
        return receiveDeferredMessage(sequenceNumber, receiverOptions.getSessionId());
    }

    /**
     * Receives a deferred {@link ServiceBusReceivedMessage message}. Deferred messages can only be received by using
     * sequence number.
     *
     * @param sequenceNumber The {@link ServiceBusReceivedMessage#getSequenceNumber() sequence number} of the
     *     message.
     * @param sessionId Session id of the deferred message. {@code null} if there is no session.
     *
     * @return A deferred message with the matching {@code sequenceNumber}.
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if deferred message cannot be received.
     */
    Mono<ServiceBusReceivedMessage> receiveDeferredMessage(long sequenceNumber, String sessionId) {
        if (isDisposed.get()) {
            return monoError(LOGGER, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, "receiveDeferredMessage")));
        }

        return
            tracer.traceManagementReceive("ServiceBus.receiveDeferredMessage",
                connectionProcessor
                    .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
                    .flatMap(node -> node.receiveDeferredMessages(receiverOptions.getReceiveMode(),
                        sessionId, getLinkName(sessionId), Collections.singleton(sequenceNumber)).last())
                    .map(receivedMessage -> {
                        if (CoreUtils.isNullOrEmpty(receivedMessage.getLockToken())) {
                            return receivedMessage;
                        }
                        if (receiverOptions.getReceiveMode() == ServiceBusReceiveMode.PEEK_LOCK) {
                            receivedMessage.setLockedUntil(managementNodeLocks.addOrUpdate(receivedMessage.getLockToken(),
                                receivedMessage.getLockedUntil(),
                                receivedMessage.getLockedUntil()));
                        }

                        return receivedMessage;
                    })
                    .onErrorMap(throwable -> mapError(throwable, ServiceBusErrorSource.RECEIVE)));
    }

    /**
     * Receives a batch of deferred {@link ServiceBusReceivedMessage messages}. Deferred messages can only be received
     * by using sequence number.
     *
     * @param sequenceNumbers The sequence numbers of the deferred messages.
     *
     * @return A {@link Flux} of deferred {@link ServiceBusReceivedMessage messages}.
     *
     * @throws NullPointerException if {@code sequenceNumbers} is null.
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if deferred messages cannot be received.
     */
    public Flux<ServiceBusReceivedMessage> receiveDeferredMessages(Iterable<Long> sequenceNumbers) {
        return tracer.traceSyncReceive("ServiceBus.receiveDeferredMessages",
            receiveDeferredMessages(sequenceNumbers, receiverOptions.getSessionId()));
    }

    /**
     * Receives a batch of deferred {@link ServiceBusReceivedMessage messages}. Deferred messages can only be received
     * by using sequence number.
     *
     * @param sequenceNumbers The sequence numbers of the deferred messages.
     * @param sessionId Session id of the deferred messages. {@code null} if there is no session.
     *
     * @return An {@link IterableStream} of deferred {@link ServiceBusReceivedMessage messages}.
     * @throws IllegalStateException if receiver is already disposed.
     * @throws NullPointerException if {@code sequenceNumbers} is null.
     * @throws ServiceBusException if deferred message cannot be received.
     */
    Flux<ServiceBusReceivedMessage> receiveDeferredMessages(Iterable<Long> sequenceNumbers, String sessionId) {
        if (isDisposed.get()) {
            return fluxError(LOGGER, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, "receiveDeferredMessageBatch")));
        }
        if (sequenceNumbers == null) {
            return fluxError(LOGGER, new NullPointerException("'sequenceNumbers' cannot be null"));
        }
        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMapMany(node -> node.receiveDeferredMessages(receiverOptions.getReceiveMode(),
                sessionId, getLinkName(sessionId), sequenceNumbers))
            .map(receivedMessage -> {
                if (CoreUtils.isNullOrEmpty(receivedMessage.getLockToken())) {
                    return receivedMessage;
                }
                if (receiverOptions.getReceiveMode() == ServiceBusReceiveMode.PEEK_LOCK) {
                    receivedMessage.setLockedUntil(managementNodeLocks.addOrUpdate(receivedMessage.getLockToken(),
                        receivedMessage.getLockedUntil(),
                        receivedMessage.getLockedUntil()));
                }

                return receivedMessage;
            })
            .onErrorMap(throwable -> mapError(throwable, ServiceBusErrorSource.RECEIVE));
    }

    /**
     * Package-private method that releases a message.
     *
     * @param message Message to release.
     * @return Mono that completes when message is successfully released.
     */
    Mono<Void> release(ServiceBusReceivedMessage message) {
        return updateDisposition(message, DispositionStatus.RELEASED, null, null, null, null);
    }

    /**
     * Asynchronously renews the lock on the message. The lock will be renewed based on the setting specified on the
     * entity. When a message is received in {@link ServiceBusReceiveMode#PEEK_LOCK} mode, the message is locked on the
     * server for this receiver instance for a duration as specified during the entity creation (LockDuration). If
     * processing of the message requires longer than this duration, the lock needs to be renewed. For each renewal, the
     * lock is reset to the entity's LockDuration value.
     *
     * @param message The {@link ServiceBusReceivedMessage} to perform auto-lock renewal.
     *
     * @return The new expiration time for the message.
     *
     * @throws NullPointerException if {@code message} or {@code message.getLockToken()} is null.
     * @throws UnsupportedOperationException if the receiver was opened in
     *     {@link ServiceBusReceiveMode#RECEIVE_AND_DELETE} mode or if the message was received from peekMessage.
     * @throws IllegalStateException if the receiver is a session receiver or receiver is already disposed.
     * @throws IllegalArgumentException if {@code message.getLockToken()} is an empty value.
     */
    public Mono<OffsetDateTime> renewMessageLock(ServiceBusReceivedMessage message) {
        if (isDisposed.get()) {
            return monoError(LOGGER, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, "renewMessageLock")));
        } else if (isSessionEnabled) {
            final String errorMessage = "Renewing message lock is an invalid operation when working with sessions.";
            return monoError(LOGGER, new IllegalStateException(errorMessage));
        } else if (Objects.isNull(message)) {
            return monoError(LOGGER, new NullPointerException("'message' cannot be null."));
        } else if (Objects.isNull(message.getLockToken())) {
            return monoError(LOGGER, new NullPointerException("'message.getLockToken()' cannot be null."));
        } else if (message.getLockToken().isEmpty()) {
            return monoError(LOGGER, new IllegalArgumentException("'message.getLockToken()' cannot be empty."));
        }

        return tracer.traceRenewMessageLock(renewMessageLock(message.getLockToken()), message)
            .onErrorMap(throwable -> mapError(throwable, ServiceBusErrorSource.RENEW_LOCK));
    }

    /**
     * Asynchronously renews the lock on the message. The lock will be renewed based on the setting specified on the
     * entity.
     *
     * @param lockToken to be renewed.
     *
     * @return The new expiration time for the message.
     * @throws IllegalStateException if receiver is already disposed.
     */
    Mono<OffsetDateTime> renewMessageLock(String lockToken) {
        if (isDisposed.get()) {
            return monoError(LOGGER, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, "renewMessageLock")));
        }

        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMap(serviceBusManagementNode ->
                serviceBusManagementNode.renewMessageLock(lockToken, getLinkName(null)))
            .map(offsetDateTime -> isOnV2 ? offsetDateTime : managementNodeLocks.addOrUpdate(lockToken, offsetDateTime,
                offsetDateTime));
    }

    /**
     * Starts the auto lock renewal for a {@link ServiceBusReceivedMessage message}.
     *
     * @param message The {@link ServiceBusReceivedMessage} to perform this operation.
     * @param maxLockRenewalDuration Maximum duration to keep renewing the lock token.
     *
     * @return A Mono that completes when the message renewal operation has completed up until
     *      {@code maxLockRenewalDuration}.
     *
     * @throws NullPointerException if {@code message}, {@code message.getLockToken()}, or
     *      {@code maxLockRenewalDuration} is null.
     * @throws IllegalStateException if the receiver is a session receiver or the receiver is disposed.
     * @throws IllegalArgumentException if {@code message.getLockToken()} is an empty value.
     * @throws ServiceBusException If the message lock cannot be renewed.
     */
    public Mono<Void> renewMessageLock(ServiceBusReceivedMessage message, Duration maxLockRenewalDuration) {
        if (isDisposed.get()) {
            return monoError(LOGGER, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, "getAutoRenewMessageLock")));
        } else if (isSessionEnabled) {
            final String errorMessage = "Renewing message lock is an invalid operation when working with sessions.";
            return monoError(LOGGER, new IllegalStateException(errorMessage));
        } else if (Objects.isNull(message)) {
            return monoError(LOGGER, new NullPointerException("'message' cannot be null."));
        } else if (Objects.isNull(message.getLockToken())) {
            return monoError(LOGGER, new NullPointerException("'message.getLockToken()' cannot be null."));
        } else if (message.getLockToken().isEmpty()) {
            return monoError(LOGGER, new IllegalArgumentException("'message.getLockToken()' cannot be empty."));
        } else if (maxLockRenewalDuration == null) {
            return monoError(LOGGER, new NullPointerException("'maxLockRenewalDuration' cannot be null."));
        } else if (maxLockRenewalDuration.isNegative()) {
            return monoError(LOGGER, new IllegalArgumentException("'maxLockRenewalDuration' cannot be negative."));
        }

        final LockRenewalOperation operation = new LockRenewalOperation(message.getLockToken(), maxLockRenewalDuration,
            false, ignored -> renewMessageLock(message));
        renewalContainer.addOrUpdate(message.getLockToken(), OffsetDateTime.now().plus(maxLockRenewalDuration),
            operation);

        return operation.getCompletionOperation()
            .onErrorMap(throwable -> mapError(throwable, ServiceBusErrorSource.RENEW_LOCK));
    }

    /**
     * Renews the session lock if this receiver is a session receiver.
     *
     * @return The next expiration time for the session lock.
     * @throws IllegalStateException if the receiver is a non-session receiver or if receiver is already disposed.
     * @throws ServiceBusException if the session lock cannot be renewed.
     */
    public Mono<OffsetDateTime> renewSessionLock() {
        return renewSessionLock(receiverOptions.getSessionId());
    }

    /**
     * Starts the auto lock renewal for the session this receiver works for.
     *
     * @param maxLockRenewalDuration Maximum duration to keep renewing the session lock.
     *
     * @return A lock renewal operation for the message.
     *
     * @throws NullPointerException if {@code sessionId} or {@code maxLockRenewalDuration} is null.
     * @throws IllegalStateException if the receiver is a non-session receiver or the receiver is disposed.
     * @throws ServiceBusException if the session lock renewal operation cannot be started.
     * @throws IllegalArgumentException if {@code sessionId} is an empty string or {@code maxLockRenewalDuration} is negative.
     */
    public Mono<Void> renewSessionLock(Duration maxLockRenewalDuration) {
        return this.renewSessionLock(receiverOptions.getSessionId(), maxLockRenewalDuration);
    }

    /**
     * Sets the state of the session this receiver works for.
     *
     * @param sessionState State to set on the session.
     *
     * @return A Mono that completes when the session is set
     * @throws IllegalStateException if the receiver is a non-session receiver or receiver is already disposed.
     * @throws ServiceBusException if the session state cannot be set.
     */
    public Mono<Void> setSessionState(byte[] sessionState) {
        return this.setSessionState(receiverOptions.getSessionId(), sessionState);
    }

    /**
     * Starts a new service side transaction. The {@link ServiceBusTransactionContext transaction context} should be
     * passed to all operations that needs to be in this transaction.
     *
     * <p><strong>Creating and using a transaction</strong></p>
     * <!-- src_embed com.azure.messaging.servicebus.servicebusreceiverasyncclient.committransaction#servicebustransactioncontext -->
     * <pre>
     * &#47;&#47; This mono creates a transaction and caches the output value, so we can associate operations with the
     * &#47;&#47; transaction. It does not cache the value if it is an error or completes with no items, effectively retrying
     * &#47;&#47; the operation.
     * Mono&lt;ServiceBusTransactionContext&gt; transactionContext = asyncReceiver.createTransaction&#40;&#41;
     *     .cache&#40;value -&gt; Duration.ofMillis&#40;Long.MAX_VALUE&#41;,
     *         error -&gt; Duration.ZERO,
     *         &#40;&#41; -&gt; Duration.ZERO&#41;;
     *
     * &#47;&#47; Dispose of the disposable to cancel the operation.
     * Disposable disposable = transactionContext.flatMap&#40;transaction -&gt; &#123;
     *     &#47;&#47; Process messages and associate operations with the transaction.
     *     Mono&lt;Void&gt; operations = Mono.when&#40;
     *         asyncReceiver.receiveDeferredMessage&#40;sequenceNumber&#41;.flatMap&#40;message -&gt;
     *             asyncReceiver.complete&#40;message, new CompleteOptions&#40;&#41;.setTransactionContext&#40;transaction&#41;&#41;&#41;,
     *         asyncReceiver.abandon&#40;receivedMessage, new AbandonOptions&#40;&#41;.setTransactionContext&#40;transaction&#41;&#41;&#41;;
     *
     *     &#47;&#47; Finally, either commit or rollback the transaction once all the operations are associated with it.
     *     return operations.then&#40;asyncReceiver.commitTransaction&#40;transaction&#41;&#41;;
     * &#125;&#41;.subscribe&#40;unused -&gt; &#123;
     * &#125;, error -&gt; &#123;
     *     System.err.println&#40;&quot;Error occurred processing transaction: &quot; + error&#41;;
     * &#125;, &#40;&#41; -&gt; &#123;
     *     System.out.println&#40;&quot;Completed transaction&quot;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.messaging.servicebus.servicebusreceiverasyncclient.committransaction#servicebustransactioncontext -->
     *
     * @return The {@link Mono} that finishes this operation on service bus resource.
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if a transaction cannot be created.
     */
    public Mono<ServiceBusTransactionContext> createTransaction() {
        if (isDisposed.get()) {
            return monoError(LOGGER, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, "createTransaction")));
        }

        return tracer.traceMono("ServiceBus.commitTransaction", connectionProcessor
                    .flatMap(connection -> connection.createSession(TRANSACTION_LINK_NAME))
                    .flatMap(AmqpSession::createTransaction)
                    .map(transaction -> new ServiceBusTransactionContext(transaction.getTransactionId())))
            .onErrorMap(throwable -> mapError(throwable, ServiceBusErrorSource.RECEIVE));
    }

    /**
     * Commits the transaction and all the operations associated with it.
     *
     * <p><strong>Creating and using a transaction</strong></p>
     * <!-- src_embed com.azure.messaging.servicebus.servicebusreceiverasyncclient.committransaction#servicebustransactioncontext -->
     * <pre>
     * &#47;&#47; This mono creates a transaction and caches the output value, so we can associate operations with the
     * &#47;&#47; transaction. It does not cache the value if it is an error or completes with no items, effectively retrying
     * &#47;&#47; the operation.
     * Mono&lt;ServiceBusTransactionContext&gt; transactionContext = asyncReceiver.createTransaction&#40;&#41;
     *     .cache&#40;value -&gt; Duration.ofMillis&#40;Long.MAX_VALUE&#41;,
     *         error -&gt; Duration.ZERO,
     *         &#40;&#41; -&gt; Duration.ZERO&#41;;
     *
     * &#47;&#47; Dispose of the disposable to cancel the operation.
     * Disposable disposable = transactionContext.flatMap&#40;transaction -&gt; &#123;
     *     &#47;&#47; Process messages and associate operations with the transaction.
     *     Mono&lt;Void&gt; operations = Mono.when&#40;
     *         asyncReceiver.receiveDeferredMessage&#40;sequenceNumber&#41;.flatMap&#40;message -&gt;
     *             asyncReceiver.complete&#40;message, new CompleteOptions&#40;&#41;.setTransactionContext&#40;transaction&#41;&#41;&#41;,
     *         asyncReceiver.abandon&#40;receivedMessage, new AbandonOptions&#40;&#41;.setTransactionContext&#40;transaction&#41;&#41;&#41;;
     *
     *     &#47;&#47; Finally, either commit or rollback the transaction once all the operations are associated with it.
     *     return operations.then&#40;asyncReceiver.commitTransaction&#40;transaction&#41;&#41;;
     * &#125;&#41;.subscribe&#40;unused -&gt; &#123;
     * &#125;, error -&gt; &#123;
     *     System.err.println&#40;&quot;Error occurred processing transaction: &quot; + error&#41;;
     * &#125;, &#40;&#41; -&gt; &#123;
     *     System.out.println&#40;&quot;Completed transaction&quot;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.messaging.servicebus.servicebusreceiverasyncclient.committransaction#servicebustransactioncontext -->
     *
     * @param transactionContext The transaction to be commit.
     *
     * @return The {@link Mono} that finishes this operation on service bus resource.
     *
     * @throws NullPointerException if {@code transactionContext} or {@code transactionContext.transactionId} is null.
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if the transaction could not be committed.
     */
    public Mono<Void> commitTransaction(ServiceBusTransactionContext transactionContext) {
        if (isDisposed.get()) {
            return monoError(LOGGER, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, "commitTransaction")));
        }
        if (Objects.isNull(transactionContext)) {
            return monoError(LOGGER, new NullPointerException("'transactionContext' cannot be null."));
        } else if (Objects.isNull(transactionContext.getTransactionId())) {
            return monoError(LOGGER, new NullPointerException("'transactionContext.transactionId' cannot be null."));
        }

        return tracer.traceMono("ServiceBus.commitTransaction", connectionProcessor
                    .flatMap(connection -> connection.createSession(TRANSACTION_LINK_NAME))
                    .flatMap(transactionSession -> transactionSession.commitTransaction(new AmqpTransaction(
                        transactionContext.getTransactionId()))))
            .onErrorMap(throwable -> mapError(throwable, ServiceBusErrorSource.RECEIVE));
    }

    /**
     * Rollbacks the transaction given and all operations associated with it.
     *
     * <p><strong>Creating and using a transaction</strong></p>
     * <!-- src_embed com.azure.messaging.servicebus.servicebusreceiverasyncclient.committransaction#servicebustransactioncontext -->
     * <pre>
     * &#47;&#47; This mono creates a transaction and caches the output value, so we can associate operations with the
     * &#47;&#47; transaction. It does not cache the value if it is an error or completes with no items, effectively retrying
     * &#47;&#47; the operation.
     * Mono&lt;ServiceBusTransactionContext&gt; transactionContext = asyncReceiver.createTransaction&#40;&#41;
     *     .cache&#40;value -&gt; Duration.ofMillis&#40;Long.MAX_VALUE&#41;,
     *         error -&gt; Duration.ZERO,
     *         &#40;&#41; -&gt; Duration.ZERO&#41;;
     *
     * &#47;&#47; Dispose of the disposable to cancel the operation.
     * Disposable disposable = transactionContext.flatMap&#40;transaction -&gt; &#123;
     *     &#47;&#47; Process messages and associate operations with the transaction.
     *     Mono&lt;Void&gt; operations = Mono.when&#40;
     *         asyncReceiver.receiveDeferredMessage&#40;sequenceNumber&#41;.flatMap&#40;message -&gt;
     *             asyncReceiver.complete&#40;message, new CompleteOptions&#40;&#41;.setTransactionContext&#40;transaction&#41;&#41;&#41;,
     *         asyncReceiver.abandon&#40;receivedMessage, new AbandonOptions&#40;&#41;.setTransactionContext&#40;transaction&#41;&#41;&#41;;
     *
     *     &#47;&#47; Finally, either commit or rollback the transaction once all the operations are associated with it.
     *     return operations.then&#40;asyncReceiver.commitTransaction&#40;transaction&#41;&#41;;
     * &#125;&#41;.subscribe&#40;unused -&gt; &#123;
     * &#125;, error -&gt; &#123;
     *     System.err.println&#40;&quot;Error occurred processing transaction: &quot; + error&#41;;
     * &#125;, &#40;&#41; -&gt; &#123;
     *     System.out.println&#40;&quot;Completed transaction&quot;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.messaging.servicebus.servicebusreceiverasyncclient.committransaction#servicebustransactioncontext -->
     *
     * @param transactionContext The transaction to rollback.
     *
     * @return The {@link Mono} that finishes this operation on service bus resource.
     * @throws NullPointerException if {@code transactionContext} or {@code transactionContext.transactionId} is null.
     * @throws IllegalStateException if receiver is already disposed.
     * @throws ServiceBusException if the transaction could not be rolled back.
     */
    public Mono<Void> rollbackTransaction(ServiceBusTransactionContext transactionContext) {
        if (isDisposed.get()) {
            return monoError(LOGGER, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, "rollbackTransaction")));
        }
        if (Objects.isNull(transactionContext)) {
            return monoError(LOGGER, new NullPointerException("'transactionContext' cannot be null."));
        } else if (Objects.isNull(transactionContext.getTransactionId())) {
            return monoError(LOGGER, new NullPointerException("'transactionContext.transactionId' cannot be null."));
        }

        return tracer.traceMono("ServiceBus.rollbackTransaction", connectionProcessor
                    .flatMap(connection -> connection.createSession(TRANSACTION_LINK_NAME))
                    .flatMap(transactionSession -> transactionSession.rollbackTransaction(new AmqpTransaction(
                        transactionContext.getTransactionId()))))
                .onErrorMap(throwable -> mapError(throwable, ServiceBusErrorSource.RECEIVE));

    }

    /**
     * Disposes of the consumer by closing the underlying links to the service.
     */
    @Override
    public void close() {
        if (isDisposed.get()) {
            return;
        }

        try {
            // Related with issue https://github.com/Azure/azure-sdk-for-java/issues/25709. When defining ServiceBusProcessorClient as bean in SpringBoot application and throw error in processMessage(), the application can not be shutdown gracefully using ctrl-c.
            // The cause is completionLock's acquire method is stuck. So we have added a timeout for acquiring the lock, to work around the issue.
            boolean acquired = completionLock.tryAcquire(5, TimeUnit.SECONDS);
            if (!acquired) {
                LOGGER.info("Unable to obtain completion lock.");
            }
        } catch (InterruptedException e) {
            LOGGER.info("Unable to obtain completion lock.", e);
        }

        if (isDisposed.getAndSet(true)) {
            return;
        }

        // Blocking until the last message has been completed.
        LOGGER.info("Removing receiver links.");
        final ServiceBusAsyncConsumer disposed = consumer.getAndSet(null);
        if (disposed != null) {
            disposed.close();
        }

        if (sessionManager != null) {
            sessionManager.close();
        }

        managementNodeLocks.close();
        renewalContainer.close();

        if (trackSettlementSequenceNumber != null) {
            try {
                trackSettlementSequenceNumber.close();
            } catch (Exception e) {
                LOGGER.info("Unable to close settlement sequence number subscription.", e);
            }
        }

        onClientClose.run();
    }

    /**
     * @return receiver options set by user;
     */
    ReceiverOptions getReceiverOptions() {
        return receiverOptions;
    }

    /**
     * Gets whether or not the management node contains the message lock token and it has not expired. Lock tokens are
     * held by the management node when they are received from the management node or management operations are
     * performed using that {@code lockToken}.
     *
     * @param lockToken Lock token to check for.
     *
     * @return {@code true} if the management node contains the lock token and false otherwise.
     */
    private boolean isManagementToken(String lockToken) {
        return managementNodeLocks.containsUnexpired(lockToken);
    }

    private Mono<Void> updateDisposition(ServiceBusReceivedMessage message, DispositionStatus dispositionStatus,
        String deadLetterReason, String deadLetterErrorDescription, Map<String, Object> propertiesToModify,
        ServiceBusTransactionContext transactionContext) {

        if (isDisposed.get()) {
            return monoError(LOGGER, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, dispositionStatus.getValue())));
        } else if (Objects.isNull(message)) {
            return monoError(LOGGER, new NullPointerException("'message' cannot be null."));
        }

        final String lockToken = message.getLockToken();
        final String sessionId = message.getSessionId();

        if (receiverOptions.getReceiveMode() != ServiceBusReceiveMode.PEEK_LOCK) {
            return Mono.error(LOGGER.logExceptionAsError(new UnsupportedOperationException(String.format(
                "'%s' is not supported on a receiver opened in ReceiveMode.RECEIVE_AND_DELETE.", dispositionStatus))));
        } else if (message.isSettled()) {
            return Mono.error(LOGGER.logExceptionAsError(
                new IllegalArgumentException("The message has either been deleted or already settled.")));
        } else if (message.getLockToken() == null) {
            // message must be a peeked message (or somehow they created a message w/o a lock token)
            final String errorMessage = "This operation is not supported for peeked messages. "
                + "Only messages received using receiveMessages() in PEEK_LOCK mode can be settled.";
            return Mono.error(
                LOGGER.logExceptionAsError(new UnsupportedOperationException(errorMessage))
            );
        }

        final String sessionIdToUse;
        if (sessionId == null && !CoreUtils.isNullOrEmpty(receiverOptions.getSessionId())) {
            sessionIdToUse = receiverOptions.getSessionId();
        } else {
            sessionIdToUse = sessionId;
        }

        LOGGER.atVerbose()
            .addKeyValue(LOCK_TOKEN_KEY, lockToken)
            .addKeyValue(ENTITY_PATH_KEY, entityPath)
            .addKeyValue(SESSION_ID_KEY, sessionIdToUse)
            .addKeyValue(DISPOSITION_STATUS_KEY, dispositionStatus)
            .log("Disposition started.");

        final Mono<Void> updateDispositionOperation;
        if (isSessionEnabled) {
            // The final this.sessionManager is guaranteed to be set when final isSessionEnabled is true.
            if (isOnV2) {
                // V2: The final this.sessionManager is guaranteed to be 'ServiceBusSingleSessionManager'.
                updateDispositionOperation = sessionManager.updateDisposition(lockToken, sessionId, dispositionStatus,
                        propertiesToModify, deadLetterReason, deadLetterErrorDescription, transactionContext)
                    .<Void>then(Mono.fromRunnable(() -> {
                        LOGGER.atVerbose()
                            .addKeyValue(LOCK_TOKEN_KEY, lockToken)
                            .addKeyValue(ENTITY_PATH_KEY, entityPath)
                            .addKeyValue(DISPOSITION_STATUS_KEY, dispositionStatus)
                            .log("Disposition completed.");
                        message.setIsSettled();
                        // The session-lock-renew logic in V2 is localized to ServiceBusReactorReceiver instance that
                        // ServiceBusSingleSessionManager composes. The logic does not use 'renewalContainer', hence
                        // unlike V1, no call to renewalContainer.remove(lockToken).
                    })).onErrorResume(DeliveryNotOnLinkException.class, __ -> {
                        // If a disposition, e.g., defer, of this session message was done via a previous
                        // `updateDisposition(..)` call then the delivery would have removed from the session-link map.
                        // In that case, if the application attempts another disposition by calling
                        // `updateDisposition(..)` again, e.g., complete, then the session-link map look up will fail.
                        // However, this disposition can be done on the management node if the broker still has the
                        // session active.
                        LOGGER.info("Could not perform disposition on session manger. Performing on management node.");
                        return dispositionViaManagementNode(message, dispositionStatus, deadLetterReason,
                            deadLetterErrorDescription, propertiesToModify, transactionContext);
                    });
            } else {
                // V1: The final this.sessionManager is guaranteed to be 'ServiceBusSessionManager'.
                updateDispositionOperation = sessionManager.updateDisposition(lockToken, sessionId, dispositionStatus,
                        propertiesToModify, deadLetterReason, deadLetterErrorDescription, transactionContext)
                    .flatMap(isSuccess -> {
                        if (isSuccess) {
                            message.setIsSettled();
                            renewalContainer.remove(lockToken);
                            return Mono.empty();
                        }
                        LOGGER.info("Could not perform on session manger. Performing on management node.");
                        return dispositionViaManagementNode(message, dispositionStatus, deadLetterReason,
                            deadLetterErrorDescription, propertiesToModify, transactionContext);
                    });
            }
        } else {
            final ServiceBusAsyncConsumer existingConsumer = consumer.get();
            if (isManagementToken(lockToken) || existingConsumer == null) {
                updateDispositionOperation = dispositionViaManagementNode(message, dispositionStatus, deadLetterReason,
                    deadLetterErrorDescription, propertiesToModify, transactionContext);
            } else {
                if (isOnV2) {
                    updateDispositionOperation = existingConsumer.updateDisposition(lockToken, dispositionStatus,
                            deadLetterReason, deadLetterErrorDescription, propertiesToModify, transactionContext)
                        .<Void>then(Mono.fromRunnable(() -> {
                            LOGGER.atVerbose()
                                .addKeyValue(LOCK_TOKEN_KEY, lockToken)
                                .addKeyValue(ENTITY_PATH_KEY, entityPath)
                                .addKeyValue(DISPOSITION_STATUS_KEY, dispositionStatus)
                                .log("Disposition completed.");

                            message.setIsSettled();
                            renewalContainer.remove(lockToken);
                        })).onErrorResume(DeliveryNotOnLinkException.class, __ -> {
                            // V2: fallback to Disposition via Management Channel on DeliveryNotOnLinkException.
                            return dispositionViaManagementNode(message, dispositionStatus, deadLetterReason,
                                deadLetterErrorDescription, propertiesToModify, transactionContext);
                        });
                } else {
                    updateDispositionOperation = existingConsumer.updateDisposition(lockToken, dispositionStatus,
                            deadLetterReason, deadLetterErrorDescription, propertiesToModify, transactionContext)
                        .then(Mono.fromRunnable(() -> {
                            LOGGER.atVerbose()
                                .addKeyValue(LOCK_TOKEN_KEY, lockToken)
                                .addKeyValue(ENTITY_PATH_KEY, entityPath)
                                .addKeyValue(DISPOSITION_STATUS_KEY, dispositionStatus)
                                .log("Disposition completed.");

                            message.setIsSettled();
                            renewalContainer.remove(lockToken);
                        }));
                }
            }
        }

        return instrumentation.instrumentSettlement(updateDispositionOperation, message, message.getContext(), dispositionStatus)
            .onErrorMap(throwable -> {
                if (throwable instanceof ServiceBusException) {
                    return throwable;
                }

                switch (dispositionStatus) {
                    case COMPLETED:
                        return new ServiceBusException(throwable, ServiceBusErrorSource.COMPLETE);
                    case ABANDONED:
                        return new ServiceBusException(throwable, ServiceBusErrorSource.ABANDON);
                    default:
                        return new ServiceBusException(throwable, ServiceBusErrorSource.UNKNOWN);
                }
            });
    }

    private Mono<Void> dispositionViaManagementNode(ServiceBusReceivedMessage message, DispositionStatus dispositionStatus,
        String deadLetterReason, String deadLetterErrorDescription, Map<String, Object> propertiesToModify,
        ServiceBusTransactionContext transactionContext) {
        final String lockToken = message.getLockToken();
        final String sessionId = message.getSessionId();
        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMap(node -> node.updateDisposition(lockToken, dispositionStatus, deadLetterReason,
                deadLetterErrorDescription, propertiesToModify, sessionId, getLinkName(sessionId), transactionContext))
            .then(Mono.fromRunnable(() -> {
                LOGGER.atInfo()
                    .addKeyValue(LOCK_TOKEN_KEY, lockToken)
                    .addKeyValue(ENTITY_PATH_KEY, entityPath)
                    .addKeyValue(DISPOSITION_STATUS_KEY, dispositionStatus)
                    .log("Disposition (via management node) completed.");

                message.setIsSettled();
                managementNodeLocks.remove(lockToken);
                renewalContainer.remove(lockToken);
            }));
    }

    private ServiceBusAsyncConsumer getOrCreateConsumer() {
        if (isSessionEnabled) {
            throw LOGGER.logExceptionAsError(new IllegalStateException("The ServiceBusAsyncConsumer is expected to work only with session unaware entity."));
        }
        final ServiceBusAsyncConsumer existing = consumer.get();
        if (existing != null) {
            return existing;
        }

        final String linkName = StringUtil.getRandomString(entityPath);
        LOGGER.atInfo()
            .addKeyValue(LINK_NAME_KEY, linkName)
            .addKeyValue(ENTITY_PATH_KEY, entityPath)
            .log("Creating consumer.");

        // The Mono, when subscribed, creates a ServiceBusReceiveLink in the ServiceBusAmqpConnection emitted by the connectionProcessor
        //
        final Mono<ServiceBusReceiveLink> receiveLinkMono = connectionProcessor.flatMap(connection -> {
            return connection.createReceiveLink(linkName, entityPath, receiverOptions.getReceiveMode(),
                null, entityType, identifier);
        }).doOnNext(next -> {
            LOGGER.atVerbose()
                .addKeyValue(LINK_NAME_KEY, linkName)
                .addKeyValue(ENTITY_PATH_KEY, next.getEntityPath())
                .addKeyValue("mode", receiverOptions.getReceiveMode())
                .addKeyValue("isSessionEnabled", false)
                .addKeyValue(ENTITY_TYPE_KEY, entityType)
                .log("Created consumer for Service Bus resource.");
        });

        // A Mono that resubscribes to 'receiveLinkMono' to retry the creation of ServiceBusReceiveLink.
        //
        // The scenarios where this retry helps are -
        // [1]. When we try to create a link on a session being disposed but connection is healthy, the retry can
        //      eventually create a new session then the link.
        // [2]. When we try to create a new session (to host the new link) but on a connection being disposed,
        //      the retry can eventually receive a new connection and then proceed with creating session and link.
        //
        final Mono<ServiceBusReceiveLink> retryableReceiveLinkMono = RetryUtil.withRetry(receiveLinkMono.onErrorMap(
                RequestResponseChannelClosedException.class,
                e -> {
                    // When the current connection is being disposed, the V1 ConnectionProcessor or V2 ReactorConnectionCache
                    // can produce a new connection if downstream request. In this context, treat
                    // RequestResponseChannelClosedException error from the following two sources as retry-able so that
                    // retry can obtain a new connection -
                    // 1. error from the RequestResponseChannel scoped to the current connection being disposed,
                    // 2. error from the V2 RequestResponseChannelCache scoped to the current connection being disposed.
                    //
                    return new AmqpException(true, e.getMessage(), e, null);
                }),
            connectionCacheWrapper.getRetryOptions(),
            "Failed to create receive link " + linkName,
            true);

        // A Flux that produces a new AmqpReceiveLink each time it receives a request from the below
        // 'AmqpReceiveLinkProcessor'. Obviously, the processor requests a link when there is a downstream subscriber.
        // It also requests a new link (i.e. retry) when the current link it holds gets terminated
        // (e.g., when the service decides to close that link).
        //
        final Flux<ServiceBusReceiveLink> receiveLinkFlux = retryableReceiveLinkMono
            .repeat()
            // The re-subscribe nature of 'MonoRepeat' following the emission of a link will cause the 'MonoFlatmap' (the
            // upstream of repeat operator) to cache the same link. When ServiceBusReceiveLinkProcessor later requests a new link,
            // the corresponding request from the 'MonoRepeat' will be answered with the cached (and closed) link by the
            // 'MonoFlatmap'. We'll filter out these cached (closed) links to avoid ServiceBusReceiveLinkProcessor from doing
            // unusable work (creating subscriptions and attempting to place the credit) on those links and associated logging.
            // See the PR description (https://github.com/Azure/azure-sdk-for-java/pull/33204) for more details.
            .filter(link -> !link.isDisposed());

        final AmqpRetryPolicy retryPolicy = RetryUtil.getRetryPolicy(connectionCacheWrapper.getRetryOptions());
        final ServiceBusAsyncConsumer newConsumer;
        if (isOnV2) {
            final MessageFlux messageFlux = new MessageFlux(receiveLinkFlux, receiverOptions.getPrefetchCount(),
                CreditFlowMode.RequestDriven, retryPolicy);

            newConsumer = new ServiceBusAsyncConsumer(linkName, messageFlux, messageSerializer, receiverOptions, instrumentation);
        } else {
            final ServiceBusReceiveLinkProcessor linkMessageProcessor = receiveLinkFlux.subscribeWith(
                new ServiceBusReceiveLinkProcessor(receiverOptions.getPrefetchCount(), retryPolicy));
            newConsumer = new ServiceBusAsyncConsumer(linkName, linkMessageProcessor, messageSerializer, receiverOptions);
        }

        // There could have been multiple threads trying to create this async consumer when the result was null.
        // If another one had set the value while we were creating this resource, dispose of newConsumer.
        if (consumer.compareAndSet(null, newConsumer)) {
            return newConsumer;
        } else {
            newConsumer.close();
            return consumer.get();
        }
    }

    /**
     * If the receiver has not connected via {@link #receiveMessages()}, all its current operations have been performed
     * through the management node.
     *
     * @return The name of the receive link, or null of it has not connected via a receive link.
     */
    private String getLinkName(String sessionId) {
        if (!CoreUtils.isNullOrEmpty(sessionId)) {
            return isSessionEnabled ? sessionManager.getLinkName(sessionId) : null;
        } else {
            final ServiceBusAsyncConsumer existing = consumer.get();
            return existing != null ? existing.getLinkName() : null;
        }
    }

    private Mono<OffsetDateTime> renewSessionLock(String sessionId) {
        if (isDisposed.get()) {
            return monoError(LOGGER, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, "renewSessionLock")));
        } else if (!isSessionEnabled) {
            return monoError(LOGGER, new IllegalStateException("Cannot renew session lock on a non-session receiver."));
        }
        final String linkName = sessionManager.getLinkName(sessionId);


        return connectionProcessor
                    .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
                    .flatMap(channel -> tracer.traceMono("ServiceBus.renewSessionLock", channel.renewSessionLock(sessionId, linkName)))
            .onErrorMap(throwable -> mapError(throwable, ServiceBusErrorSource.RENEW_LOCK));
    }

    private Mono<Void> renewSessionLock(String sessionId, Duration maxLockRenewalDuration) {
        if (isDisposed.get()) {
            return monoError(LOGGER, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, "renewSessionLock")));
        } else if (!isSessionEnabled) {
            return monoError(LOGGER, new IllegalStateException(
                "Cannot renew session lock on a non-session receiver."));
        } else if (maxLockRenewalDuration == null) {
            return monoError(LOGGER, new NullPointerException("'maxLockRenewalDuration' cannot be null."));
        } else if (maxLockRenewalDuration.isNegative()) {
            return monoError(LOGGER, new IllegalArgumentException(
                "'maxLockRenewalDuration' cannot be negative."));
        } else if (Objects.isNull(sessionId)) {
            return monoError(LOGGER, new NullPointerException("'sessionId' cannot be null."));
        } else if (sessionId.isEmpty()) {
            return monoError(LOGGER, new IllegalArgumentException("'sessionId' cannot be empty."));
        }
        final LockRenewalOperation operation = new LockRenewalOperation(sessionId, maxLockRenewalDuration,
            true, this::renewSessionLock);

        renewalContainer.addOrUpdate(sessionId, OffsetDateTime.now().plus(maxLockRenewalDuration), operation);
        return operation.getCompletionOperation();
    }

    private Mono<Void> setSessionState(String sessionId, byte[] sessionState) {
        if (isDisposed.get()) {
            return monoError(LOGGER, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, "setSessionState")));
        } else if (!isSessionEnabled) {
            return monoError(LOGGER, new IllegalStateException("Cannot set session state on a non-session receiver."));
        }
        assert sessionManager != null; // guaranteed to be non-null when isSessionEnabled is true.
        final String linkName = sessionManager.getLinkName(sessionId);

        return tracer.traceMono("ServiceBus.setSessionState", connectionProcessor
                    .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
                    .flatMap(channel -> channel.setSessionState(sessionId, sessionState, linkName)))
            .onErrorMap((err) -> mapError(err, ServiceBusErrorSource.RECEIVE));
    }

    private Mono<byte[]> getSessionState(String sessionId) {
        if (isDisposed.get()) {
            return monoError(LOGGER, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, "getSessionState")));
        } else if (!isSessionEnabled) {
            return monoError(LOGGER, new IllegalStateException("Cannot get session state on a non-session receiver."));
        }
        assert sessionManager != null; // guaranteed to be non-null when isSessionEnabled is true.
        final String linkName = sessionManager.getLinkName(sessionId);

        return tracer.traceMono("ServiceBus.setSessionState", connectionProcessor
                .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
                .flatMap(channel -> channel.getSessionState(sessionId, linkName)))
            .onErrorMap((err) -> mapError(err, ServiceBusErrorSource.RECEIVE));
    }

    ServiceBusReceiverInstrumentation getInstrumentation() {
        return instrumentation;
    }

    /**
     * Map the error to {@link ServiceBusException}
     */
    private Throwable mapError(Throwable throwable, ServiceBusErrorSource errorSource) {
        if (!(throwable instanceof ServiceBusException)) {
            return new ServiceBusException(throwable, errorSource);
        }
        return throwable;
    }

    boolean isConnectionClosed() {
        return this.connectionCacheWrapper.isChannelClosed();
    }

    boolean isManagementNodeLocksClosed() {
        return this.managementNodeLocks.isClosed();
    }

    boolean isRenewalContainerClosed() {
        return this.renewalContainer.isClosed();
    }

    boolean isSessionEnabled() {
        return isSessionEnabled;
    }

    boolean isAutoLockRenewRequested() {
        return receiverOptions.isAutoLockRenewEnabled();
    }

    boolean isV2() {
        return isOnV2;
    }

    Flux<ServiceBusReceivedMessage> nonSessionProcessorReceiveV2() {
        assert isOnV2 && !isSessionEnabled;
        return getOrCreateConsumer().receive();
    }

    private Flux<ServiceBusReceivedMessage> nonSessionReactiveReceiveV2() {
        assert isOnV2 && !isSessionEnabled;

        final boolean enableAutoDisposition = receiverOptions.isEnableAutoComplete();
        final boolean enableAutoLockRenew = receiverOptions.isAutoLockRenewEnabled();
        final Flux<ServiceBusReceivedMessage> messages = getOrCreateConsumer().receive()
            .onErrorMap(throwable -> mapError(throwable, ServiceBusErrorSource.RECEIVE));

        if (enableAutoDisposition | enableAutoLockRenew) {
            // AutoDisposition(Complete|Abandon) and AutoLockRenew features in Low-Level Reactor Receiver Client are
            // slated for deprecation.
            return new AutoDispositionLockRenew(messages, this, enableAutoDisposition, enableAutoLockRenew, completionLock);
        } else {
            return messages;
        }
    }

    Flux<ServiceBusReceivedMessage> nonSessionSyncReceiveV2() {
        assert isOnV2 && !isSessionEnabled;
        final Flux<ServiceBusReceivedMessage> messages = getOrCreateConsumer().receive();
        return receiverOptions.isAutoLockRenewEnabled() ? messages.doOnNext(this::beginLockRenewal) : messages;
    }

    private Flux<ServiceBusReceivedMessage> sessionReactiveReceiveV2() {
        assert isOnV2 && isSessionEnabled && sessionManager instanceof ServiceBusSingleSessionManager;
        final ServiceBusSingleSessionManager singleSessionManager = (ServiceBusSingleSessionManager) sessionManager;
        // Note: Once side-by-side support for V1 is no longer needed, we'll update the ServiceBusSingleSessionManager::receive()
        // to return Flux<ServiceBusReceivedMessage> and delete ServiceBusSingleSessionManager.receiveMessages(), which removes
        // the above casting and type check assertion.
        final Flux<ServiceBusReceivedMessage> messages = singleSessionManager.receiveMessages();
        final boolean enableAutoDisposition = receiverOptions.isEnableAutoComplete();
        if (enableAutoDisposition) {
            // AutoDisposition(Complete|Abandon) and AutoLockRenew features in Low-Level Reactor Receiver Client are
            // slated for deprecation.
            return new AutoDispositionLockRenew(messages, this, true, false, completionLock);
        } else {
            return messages;
        }
    }

    Flux<ServiceBusReceivedMessage> sessionSyncReceiveV2() {
        assert isOnV2 && isSessionEnabled && sessionManager instanceof ServiceBusSingleSessionManager;
        final ServiceBusSingleSessionManager singleSessionManager = (ServiceBusSingleSessionManager) sessionManager;
        // Note: See the note in sessionReactiveReceiveV2().
        return singleSessionManager.receiveMessages();
    }

    /**
     * Begin the recurring lock renewal of the given message.
     *
     * @param message the message to keep renewing.
     * @return {@link Disposable} that when disposed of, results in stopping the recurring renewal.
     */
    Disposable beginLockRenewal(ServiceBusReceivedMessage message) {
        if (isSessionEnabled) {
            throw LOGGER.logExceptionAsError(new IllegalStateException("Renewing message lock is an invalid operation when working with sessions."));
        }
        final Duration maxRenewalDuration = receiverOptions.getMaxLockRenewDuration();
        Objects.requireNonNull(maxRenewalDuration, "'receivingOptions.maxAutoLockRenewDuration' is required for recurring lock renewal.");

        if (message == null) {
            return Disposables.disposed();
        }

        final String lockToken = message.getLockToken();
        if (Objects.isNull(lockToken)) {
            LOGGER.atWarning()
                .addKeyValue(SEQUENCE_NUMBER_KEY, message.getSequenceNumber())
                .log("Unexpected, LockToken is required for recurring lock renewal.");
            return Disposables.disposed();
        }

        final OffsetDateTime initialExpireAt = message.getLockedUntil();
        if (Objects.isNull(initialExpireAt)) {
            LOGGER.atWarning()
                .addKeyValue(SEQUENCE_NUMBER_KEY, message.getSequenceNumber())
                .log("Unexpected, LockedUntil is required for recurring lock renewal.");
            return Disposables.disposed();
        }

        // A Mono, when subscribed, requests the broker to renew the message once and updates the message's lockedUntil
        // field to reflect the new expiration time.
        final Mono<OffsetDateTime> renewalMono = tracer.traceRenewMessageLock(this.renewMessageLock(lockToken)
            .map(nextExpireAt -> {
                message.setLockedUntil(nextExpireAt);
                return nextExpireAt;
            }), message);

        // The operation performing recurring renewal by subscribing to 'renewalMono' before the message expires each time.
        // The periodic renewal stops when the object is disposed of, or when the 'maxRenewalDuration' elapses.
        final LockRenewalOperation recurringRenewal = new LockRenewalOperation(lockToken, maxRenewalDuration, false, __ -> renewalMono, initialExpireAt);
        // TODO: anu ^ - (allocation improvement)
        //  Update LockRenewalOperation::Ctr to take Mono<OffsetDateTime> instead of a Func<String, Mono<OffsetDateTime>>,
        //  the Func code never uses first lockToken 'String' param.
        try {
            // Track the recurring renewal operation in client scope so that it can be disposed of (to prevent memory leak)
            // 1. when the client closes, or
            // 2. when the lock is identified as expired i.e., no renewal happened, so lock lifetime past the current time.
            renewalContainer.addOrUpdate(lockToken, OffsetDateTime.now().plus(maxRenewalDuration), recurringRenewal);
        } catch (Exception e) {
            LOGGER.atInfo()
                .addKeyValue(LOCK_TOKEN_KEY, lockToken)
                .log("Exception occurred while updating lockContainer.", e);
        }

        // TODO: anu, maybe have LockRenewalOperation implement Disposable so the following inline interface impl can be removed.
        return Disposables.composite(() -> recurringRenewal.close(), () -> renewalContainer.remove(lockToken));
    }
}
