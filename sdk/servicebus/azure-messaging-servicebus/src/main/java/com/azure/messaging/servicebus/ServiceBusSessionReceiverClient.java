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
 * &#47;&#47; The connectionString&#47;sessionQueueName must be set by the application. The 'connectionString' format is shown below.
 * &#47;&#47; &quot;Endpoint=&#123;fully-qualified-namespace&#125;;SharedAccessKeyName=&#123;policy-name&#125;;SharedAccessKey=&#123;key&#125;&quot;
 * ServiceBusSessionReceiverClient sessionReceiver = new ServiceBusClientBuilder&#40;&#41;
 *     .connectionString&#40;connectionString&#41;
 *     .sessionReceiver&#40;&#41;
 *     .queueName&#40;sessionQueueName&#41;
 *     .buildClient&#40;&#41;;
 * ServiceBusReceiverClient receiver = sessionReceiver.acceptSession&#40;&quot;&lt;&lt; my-session-id &gt;&gt;&quot;&#41;;
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
 * &#47;&#47; The connectionString&#47;sessionQueueName must be set by the application. The 'connectionString' format is shown below.
 * &#47;&#47; &quot;Endpoint=&#123;fully-qualified-namespace&#125;;SharedAccessKeyName=&#123;policy-name&#125;;SharedAccessKey=&#123;key&#125;&quot;
 * ServiceBusSessionReceiverClient sessionReceiver = new ServiceBusClientBuilder&#40;&#41;
 *     .connectionString&#40;
 *         &quot;Endpoint=&#123;fully-qualified-namespace&#125;;SharedAccessKeyName=&#123;policy-name&#125;;SharedAccessKey=&#123;key&#125;&quot;&#41;
 *     .sessionReceiver&#40;&#41;
 *     .queueName&#40;&quot;&lt;&lt; QUEUE NAME &gt;&gt;&quot;&#41;
 *     .buildClient&#40;&#41;;
 * ServiceBusReceiverClient receiver = sessionReceiver.acceptNextSession&#40;&#41;;
 *
 * &#47;&#47; Use the receiver and finally close it along with the sessionReceiver.
 * receiver.close&#40;&#41;;
 * sessionReceiver.close&#40;&#41;;
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
