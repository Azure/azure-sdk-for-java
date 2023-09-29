// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;

import java.time.Duration;
import java.util.Objects;

/**
 * This <b>synchronous</b> session receiver client is used to acquire session locks from a queue or topic and create
 * {@link ServiceBusReceiverClient} instances that are tied to the locked sessions.
 *
 * <p><strong>Receive messages from a specific session</strong></p>
 * <p>Use {@link #acceptSession(String)} to acquire the lock of a session if you know the session id.</p>
 * <!-- src_embed com.azure.messaging.servicebus.servicebusreceiverclient.instantiation#sessionId -->
 * <pre>
 * TokenCredential credential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 *
 * &#47;&#47; 'fullyQualifiedNamespace' will look similar to &quot;&#123;your-namespace&#125;.servicebus.windows.net&quot;
 * &#47;&#47; 'disableAutoComplete' indicates that users will explicitly settle their message.
 * ServiceBusSessionReceiverClient sessionReceiver = new ServiceBusClientBuilder&#40;&#41;
 *     .credential&#40;fullyQualifiedNamespace, credential&#41;
 *     .sessionReceiver&#40;&#41;
 *     .queueName&#40;sessionEnabledQueueName&#41;
 *     .disableAutoComplete&#40;&#41;
 *     .buildClient&#40;&#41;;
 * ServiceBusReceiverClient receiver = sessionReceiver.acceptSession&#40;&quot;&lt;&lt;my-session-id&gt;&gt;&quot;&#41;;
 *
 * &#47;&#47; Use the receiver and finally close it along with the sessionReceiver.
 * receiver.close&#40;&#41;;
 * sessionReceiver.close&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.servicebusreceiverclient.instantiation#sessionId -->
 *
 * <p><strong>Receive messages from the first available session</strong></p>
 * <p>Use {@link #acceptNextSession()} to acquire the lock of the next available session without specifying the session
 * id.</p>
 * <!-- src_embed com.azure.messaging.servicebus.servicebusreceiverclient.instantiation#nextsession -->
 * <pre>
 * TokenCredential credential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 *
 * &#47;&#47; 'fullyQualifiedNamespace' will look similar to &quot;&#123;your-namespace&#125;.servicebus.windows.net&quot;
 * &#47;&#47; 'disableAutoComplete' indicates that users will explicitly settle their message.
 * ServiceBusSessionReceiverClient sessionReceiver = new ServiceBusClientBuilder&#40;&#41;
 *     .credential&#40;fullyQualifiedNamespace, credential&#41;
 *     .sessionReceiver&#40;&#41;
 *     .disableAutoComplete&#40;&#41;
 *     .queueName&#40;sessionEnabledQueueName&#41;
 *     .buildClient&#40;&#41;;
 *
 * &#47;&#47; Creates a client to receive messages from the first available session. It waits until
 * &#47;&#47; AmqpRetryOptions.getTryTimeout&#40;&#41; elapses. If no session is available within that operation timeout, it
 * &#47;&#47; throws a retriable error. Otherwise, a receiver is returned when a lock on the session is acquired.
 * ServiceBusReceiverClient receiver = sessionReceiver.acceptNextSession&#40;&#41;;
 *
 * &#47;&#47; Use the receiver and finally close it along with the sessionReceiver.
 * try &#123;
 *     IterableStream&lt;ServiceBusReceivedMessage&gt; receivedMessages =
 *         receiver.receiveMessages&#40;10, Duration.ofSeconds&#40;30&#41;&#41;;
 *
 *     for &#40;ServiceBusReceivedMessage message : receivedMessages&#41; &#123;
 *         System.out.println&#40;&quot;Body: &quot; + message&#41;;
 *     &#125;
 * &#125; finally &#123;
 *     receiver.close&#40;&#41;;
 *     sessionReceiver.close&#40;&#41;;
 * &#125;
 *
 * </pre>
 * <!-- end com.azure.messaging.servicebus.servicebusreceiverclient.instantiation#nextsession -->
 */
@ServiceClient(builder = ServiceBusClientBuilder.class)
public final class ServiceBusSessionReceiverClient implements AutoCloseable {
    private final ServiceBusSessionReceiverAsyncClient sessionAsyncClient;
    private final boolean isPrefetchDisabled;
    private final Duration operationTimeout;

    ServiceBusSessionReceiverClient(ServiceBusSessionReceiverAsyncClient asyncClient, boolean isPrefetchDisabled, Duration operationTimeout) {
        this.sessionAsyncClient = Objects.requireNonNull(asyncClient, "'asyncClient' cannot be null.");
        this.operationTimeout = operationTimeout;
        this.isPrefetchDisabled = isPrefetchDisabled;
    }

    /**
     * Acquires a session lock for the next available session and creates a {@link ServiceBusReceiverClient}
     * to receive messages from the session. It will wait until a session is available if no one is available
     * immediately.
     *
     * @return A {@link ServiceBusReceiverClient} that is tied to the available session.
     *
     * @throws UnsupportedOperationException if the queue or topic subscription is not session-enabled.
     * @throws AmqpException if the operation times out. The timeout duration is the tryTimeout
     *      of when you build this client with the {@link ServiceBusClientBuilder#retryOptions(AmqpRetryOptions)}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ServiceBusReceiverClient acceptNextSession() {
        return sessionAsyncClient.acceptNextSession()
            .map(asyncClient -> new ServiceBusReceiverClient(asyncClient, isPrefetchDisabled, operationTimeout))
            .block(operationTimeout);
    }

    /**
     * Acquires a session lock for {@code sessionId} and create a {@link ServiceBusReceiverClient}
     * to receive messages from the session. If the session is already locked by another client, an
     * {@link AmqpException} is thrown immediately.
     *
     * @param sessionId The session id.
     *
     * @return A {@link ServiceBusReceiverClient} that is tied to the specified session.
     *
     * @throws NullPointerException if {@code sessionId} is null.
     * @throws IllegalArgumentException if {@code sessionId} is empty.
     * @throws UnsupportedOperationException if the queue or topic subscription is not session-enabled.
     * @throws ServiceBusException if the lock cannot be acquired.
     * @throws AmqpException if the operation times out. The timeout duration is the tryTimeout
     *      of when you build this client with the {@link ServiceBusClientBuilder#retryOptions(AmqpRetryOptions)}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ServiceBusReceiverClient acceptSession(String sessionId) {
        return sessionAsyncClient.acceptSession(sessionId)
            .map(asyncClient -> new ServiceBusReceiverClient(asyncClient, isPrefetchDisabled, operationTimeout))
            .block(operationTimeout);
    }

    @Override
    public void close() {
        sessionAsyncClient.close();
    }
}
