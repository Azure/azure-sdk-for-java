// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.implementation.ServiceBusConnectionProcessor;
import com.azure.messaging.servicebus.implementation.ServiceBusConstants;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusReceiverInstrumentation;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusTracer;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.messaging.servicebus.ReceiverOptions.createNamedSessionOptions;

/**
 * This <b>asynchronous</b> session receiver client is used to acquire session locks from a queue or topic and create
 * {@link ServiceBusReceiverAsyncClient} instances that are tied to the locked sessions.
 *
 * <p><strong>Receive messages from a specific session</strong></p>
 * <p>Use {@link #acceptSession(String)} to acquire the lock of a session if you know the session id.</p>
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
 * &#47;&#47; `Flux.usingWhen` is used, so we dispose of the receiver resource after `receiveMessages&#40;&#41;` completes.
 * &#47;&#47; `Mono.usingWhen` can also be used if the resource closure only returns a single item.
 * Flux&lt;ServiceBusReceivedMessage&gt; sessionMessages = Flux.usingWhen&#40;
 *     sessionReceiver.acceptSession&#40;&quot;&lt;&lt;my-session-id&gt;&gt;&quot;&#41;,
 *     receiver -&gt; &#123;
 *         &#47;&#47; Receive messages from &lt;&lt;my-session-id&gt;&gt; session.
 *         return receiver.receiveMessages&#40;&#41;;
 *     &#125;,
 *     receiver -&gt; Mono.fromRunnable&#40;&#40;&#41; -&gt; &#123;
 *         &#47;&#47; Dispose of
 *         receiver.close&#40;&#41;;
 *         sessionReceiver.close&#40;&#41;;
 *     &#125;&#41;&#41;;
 *
 * &#47;&#47; When program ends, or you're done receiving all messages, the `subscription` can be disposed of. This code
 * &#47;&#47; is non-blocking and kicks off the operation.
 * Disposable subscription = sessionMessages.subscribe&#40;
 *     message -&gt; System.out.printf&#40;&quot;Received Sequence #: %s. Contents: %s%n&quot;,
 *         message.getSequenceNumber&#40;&#41;, message.getBody&#40;&#41;&#41;,
 *     error -&gt; System.err.print&#40;error&#41;,
 *     &#40;&#41; -&gt; System.out.println&#40;&quot;Completed receiving from session.&quot;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.servicebusreceiverasyncclient.instantiation#sessionId -->
 *
 * <p><strong>Receive messages from the first available session</strong></p>
 * <p>Use {@link #acceptNextSession()} to acquire the lock of the next available session without specifying the session
 * id.</p>
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
 * Disposable disposable = Flux.usingWhen&#40;receiverMono,
 *         receiver -&gt; receiver.receiveMessages&#40;&#41;,
 *         receiver -&gt; Mono.fromRunnable&#40;&#40;&#41; -&gt; &#123;
 *             &#47;&#47; Dispose of the receiver and sessionReceiver when done receiving messages.
 *             receiver.close&#40;&#41;;
 *             sessionReceiver.close&#40;&#41;;
 *         &#125;&#41;&#41;
 *     .subscribe&#40;message -&gt; &#123;
 *         System.out.println&#40;&quot;Received message: &quot; + message.getBody&#40;&#41;&#41;;
 *     &#125;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.servicebusreceiverasyncclient.instantiation#nextsession -->
 */
@ServiceClient(builder = ServiceBusClientBuilder.class, isAsync = true)
public final class ServiceBusSessionReceiverAsyncClient implements AutoCloseable {
    private static final ClientLogger LOGGER = new ClientLogger(ServiceBusSessionReceiverAsyncClient.class);

    private final String fullyQualifiedNamespace;
    private final String entityPath;
    private final MessagingEntityType entityType;
    private final ReceiverOptions receiverOptions;
    private final ServiceBusConnectionProcessor connectionProcessor;
    private final ServiceBusReceiverInstrumentation instrumentation;
    private final ServiceBusTracer tracer;
    private final MessageSerializer messageSerializer;
    private final Runnable onClientClose;
    private final ServiceBusSessionManager unNamedSessionManager;  // for acceptNextSession()
    private final String identifier;

    ServiceBusSessionReceiverAsyncClient(String fullyQualifiedNamespace, String entityPath,
        MessagingEntityType entityType, ReceiverOptions receiverOptions,
        ServiceBusConnectionProcessor connectionProcessor, ServiceBusReceiverInstrumentation instrumentation,
        MessageSerializer messageSerializer, Runnable onClientClose, String identifier) {
        this.fullyQualifiedNamespace = Objects.requireNonNull(fullyQualifiedNamespace,
            "'fullyQualifiedNamespace' cannot be null.");
        this.entityPath = Objects.requireNonNull(entityPath, "'entityPath' cannot be null.");
        this.entityType = Objects.requireNonNull(entityType, "'entityType' cannot be null.");
        this.receiverOptions = Objects.requireNonNull(receiverOptions, "'receiveOptions cannot be null.'");
        this.connectionProcessor = Objects.requireNonNull(connectionProcessor, "'connectionProcessor' cannot be null.");
        this.instrumentation = Objects.requireNonNull(instrumentation, "'instrumentation' cannot be null.");
        this.messageSerializer = Objects.requireNonNull(messageSerializer, "'messageSerializer' cannot be null.");
        this.onClientClose = Objects.requireNonNull(onClientClose, "'onClientClose' cannot be null.");
        this.unNamedSessionManager = new ServiceBusSessionManager(entityPath, entityType, connectionProcessor,
             messageSerializer, receiverOptions, identifier, instrumentation.getTracer());
        this.identifier = identifier;
        this.tracer = instrumentation.getTracer();
    }

    /**
     * Acquires a session lock for the next available session and creates a {@link ServiceBusReceiverAsyncClient}
     * to receive messages from the session. It will wait until a session is available if none is immediately
     * available.
     *
     * @return A {@link ServiceBusReceiverAsyncClient} that is tied to the available session.
     *
     * @throws UnsupportedOperationException if the queue or topic subscription is not session-enabled.
     * @throws AmqpException if the operation times out. The timeout duration is the tryTimeout
     *      of when you build this client with the {@link ServiceBusClientBuilder#retryOptions(AmqpRetryOptions)}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ServiceBusReceiverAsyncClient> acceptNextSession() {
        return tracer.traceMono("ServiceBus.acceptNextSession", unNamedSessionManager.getActiveLink().flatMap(receiveLink -> receiveLink.getSessionId()
            .map(sessionId -> {
                final ReceiverOptions newReceiverOptions = createNamedSessionOptions(receiverOptions.getReceiveMode(),
                    receiverOptions.getPrefetchCount(), receiverOptions.getMaxLockRenewDuration(),
                    receiverOptions.isEnableAutoComplete(), sessionId);
                final ServiceBusSessionManager sessionSpecificManager = new ServiceBusSessionManager(entityPath,
                    entityType, connectionProcessor, messageSerializer, newReceiverOptions,
                    receiveLink, identifier, instrumentation.getTracer());
                return new ServiceBusReceiverAsyncClient(fullyQualifiedNamespace, entityPath,
                    entityType, newReceiverOptions, connectionProcessor, ServiceBusConstants.OPERATION_TIMEOUT,
                    instrumentation, messageSerializer, () -> { }, sessionSpecificManager);
            })));

    }

    /**
     * Acquires a session lock for {@code sessionId} and create a {@link ServiceBusReceiverAsyncClient}
     * to receive messages from the session. If the session is already locked by another client, an
     * {@link AmqpException} is thrown.
     *
     * @param sessionId The session id.
     *
     * @return A {@link ServiceBusReceiverAsyncClient} that is tied to the specified session.
     *
     * @throws NullPointerException if {@code sessionId} is null.
     * @throws IllegalArgumentException if {@code sessionId} is empty.
     * @throws UnsupportedOperationException if the queue or topic subscription is not session-enabled.
     * @throws AmqpException if the lock cannot be acquired.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ServiceBusReceiverAsyncClient> acceptSession(String sessionId) {
        if (sessionId == null) {
            return monoError(LOGGER, new NullPointerException("'sessionId' cannot be null"));
        }
        if (CoreUtils.isNullOrEmpty(sessionId)) {
            return monoError(LOGGER, new IllegalArgumentException("'sessionId' cannot be empty"));
        }

        final ReceiverOptions newReceiverOptions = createNamedSessionOptions(receiverOptions.getReceiveMode(),
            receiverOptions.getPrefetchCount(), receiverOptions.getMaxLockRenewDuration(),
            receiverOptions.isEnableAutoComplete(), sessionId);
        final ServiceBusSessionManager sessionSpecificManager = new ServiceBusSessionManager(entityPath, entityType,
            connectionProcessor, messageSerializer, newReceiverOptions, identifier, instrumentation.getTracer());

        return tracer.traceMono("ServiceBus.acceptSession",
            sessionSpecificManager.getActiveLink().map(receiveLink -> new ServiceBusReceiverAsyncClient(
                        fullyQualifiedNamespace, entityPath, entityType, newReceiverOptions, connectionProcessor,
                        ServiceBusConstants.OPERATION_TIMEOUT, instrumentation, messageSerializer, () -> { },
                        sessionSpecificManager)));
    }

    @Override
    public void close() {
        this.unNamedSessionManager.close();
        this.onClientClose.run();
    }
}
