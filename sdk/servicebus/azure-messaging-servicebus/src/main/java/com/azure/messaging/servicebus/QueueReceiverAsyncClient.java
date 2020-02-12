// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpRetryPolicy;

import com.azure.core.amqp.implementation.AmqpReceiveLink;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.RetryUtil;
import com.azure.core.amqp.implementation.TracerProvider;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.ServiceBusConnectionProcessor;
import com.azure.messaging.servicebus.implementation.ServiceBusReceiveLinkProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import java.io.Closeable;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static com.azure.core.util.FluxUtil.fluxError;

/**
 * An <b>asynchronous</b> receiver responsible for reading {@link Message} from either a specific Queue.
 *
 */
@ServiceClient(builder = QueueClientBuilder.class, isAsync = true)
public final class QueueReceiverAsyncClient implements Closeable {

    private static final String RECEIVER_ENTITY_PATH_FORMAT = "%s";

    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final ClientLogger logger = new ClientLogger(QueueReceiverAsyncClient.class);
    private final String fullyQualifiedNamespace;
    private final String queueName;
    private final ServiceBusConnectionProcessor connectionProcessor;
    private final MessageSerializer messageSerializer;
    private final int prefetchCount;
    private final boolean isSharedConnection;
    private final TracerProvider tracerProvider;
    private final ReceiveMode defaultReceiveMode = ReceiveMode.PEEKLOCK;


    AmqpRetryOptions defaultRetryOptions =  new AmqpRetryOptions();

    /**
     * Keeps track of the open consumers keyed by linkName. The link name is generated as: {@code
     * "partitionId_GUID"}. For receiving from all partitions, links are prefixed with {@code "all-GUID-partitionId"}.
     */
    private final ConcurrentHashMap<String, ServiceBusAsyncConsumer> openConsumers =
        new ConcurrentHashMap<>();

    QueueReceiverAsyncClient(String fullyQualifiedNamespace, String queueName,
                             ServiceBusConnectionProcessor connectionProcessor, TracerProvider tracerProvider ,
                             MessageSerializer messageSerializer,int prefetchCount, boolean isSharedConnection) {
        this.fullyQualifiedNamespace = fullyQualifiedNamespace;
        this.queueName = queueName;
        this.connectionProcessor = connectionProcessor;
        this.messageSerializer = messageSerializer;
        this.prefetchCount = prefetchCount;
        this.isSharedConnection = isSharedConnection;
        this.tracerProvider = tracerProvider;
    }

    /**
     * Gets the fully qualified Service Bus  namespace that the connection is associated with. This is likely similar to
     * {@code {yournamespace}.servicebus.windows.net}.
     *
     * @return The fully qualified Service Bus namespace that the connection is associated with.
     */
    public String getFullyQualifiedNamespace() {
        return fullyQualifiedNamespace;
    }

    /**
     * Gets the Queue name this client interacts with.
     *
     * @return The Queue name this client interacts with.
     */
    public String getQueueName() {
        return queueName;
    }

    /**
     * Consumes messages from Queue.
     *
     * <p>This method is <b>not</b> recommended for production use; the TODO should be used for
     * reading messages in a production scenario, as it offers a much more robust experience with
     * higher throughput.
     *
     * @return A stream of messages from Queue.
     */
   public Flux<Message> receive() {
        return receive(defaultReceiveMode);
    }

    /**
     *
     * @return A stream of messages from Queue.
     */
    public Flux<Message> peek() {
        return receive(defaultReceiveMode);
    }

    /**
     * Receive message for specific session id.
     * @param sessionId of the message.
     * @return A stream of messages from Queue.
     */
    public Flux<Message> receive(String sessionId) {
        return receive(defaultReceiveMode);
        //TODO : Session id needs to be implemented.
    }

    /**
     * Consumes events from all partitions configured with a set of {@code receiveOptions}.
     *
     * <p>This method is <b>not</b> recommended for production use; the TODO should be used for
     * reading events from all partitions in a production scenario, as it offers a much more robust experience with
     * higher throughput.
     *
     * It is important to note that this method does not guarantee fairness amongst the partitions. Depending on service
     * communication, there may be a clustering of events per partition and/or there may be a noticeable bias for a
     * given partition or subset of partitions.</p>
     *
     * <ul>
     * <li>If receive is invoked where TODO has a value, then Event Hubs service will
     * guarantee only one active consumer exists per partitionId and consumer group combination. This receive operation
     * is sometimes referred to as an "Epoch Consumer".</li>
     * <li>Multiple consumers per partitionId and consumer group combination can be created by not setting
     * TODO when invoking receive operations. This non-exclusive consumer is sometimes
     * referred to as a "Non-Epoch Consumer."</li>
     * </ul>
     *
     * @param receiveMode {@link ReceiveMode} when receiving events from Queue.
     *
     * @return A stream of events for every partition from Queue.
     *
     * @throws NullPointerException if {@code receiveMode} is null.
     */
    public Flux<Message> receive( ReceiveMode receiveMode) {
        if (Objects.isNull(receiveMode)) {
            return fluxError(logger, new NullPointerException("'receiveMode' cannot be null."));
        }

        final String linkName = connectionProcessor.getEntityPath();
        return createConsumer(linkName, receiveMode);
    }

    /**
     *
     * @param receiveMode for messages from Queue.
     * @param sessionId of the message.
     * @return A stream of messages from Queue.
     * @throws NullPointerException if {@code receiveMode} is null.
     */
    public Flux<Message> receive( ReceiveMode receiveMode, String sessionId) {
        //TODO : Session id needs to be implemented.
        if (Objects.isNull(receiveMode)) {
            return fluxError(logger, new NullPointerException("'receiveMode' cannot be null."));
        }

        final String linkName = connectionProcessor.getEntityPath();
        return createConsumer(linkName, receiveMode);
    }

    /**
     * Disposes of the consumer by closing the underlying connection to the service.
     */
    @Override
    public void close() {
        if (isDisposed.getAndSet(true)) {
            return;
        }
        openConsumers.forEach((key, value) -> value.close());
        openConsumers.clear();

        if (!isSharedConnection) {
            connectionProcessor.dispose();
        }
    }

    private Flux<Message> createConsumer(String linkName, ReceiveMode receiveMode) {
        return openConsumers
            .computeIfAbsent(linkName, name -> {
                logger.info("{}: Creating receive consumer.", linkName);
                return createServiceBusConsumer(name, receiveMode);
            })
            .receive()
            .doOnCancel(() -> removeLink(linkName, SignalType.CANCEL))
            .doOnComplete(() -> removeLink(linkName, SignalType.ON_COMPLETE))
            .doOnError(error -> removeLink(linkName, SignalType.ON_ERROR));
    }

    private void removeLink(String linkName, SignalType signalType) {
        logger.info("{}: Receiving completed. Signal[{}]", linkName, signalType);
        final ServiceBusAsyncConsumer consumer = openConsumers.remove(linkName);

        if (consumer != null) {
            consumer.close();
        }
    }
    private ServiceBusAsyncConsumer createServiceBusConsumer(String linkName, ReceiveMode receiveMode) {
        final String entityPath = String.format(Locale.US, RECEIVER_ENTITY_PATH_FORMAT, getQueueName());

        final Flux<AmqpReceiveLink> receiveLinkMono =
            connectionProcessor.flatMap(connection ->
                connection.createReceiveLink(linkName, entityPath, receiveMode))
                .doOnNext(next -> logger.verbose("Creating consumer for path: {}", next.getEntityPath()))
                .repeat();

        final AmqpRetryPolicy retryPolicy = RetryUtil.getRetryPolicy(connectionProcessor.getRetryOptions());
        final ServiceBusReceiveLinkProcessor linkMessageProcessor = receiveLinkMono.subscribeWith(
            new ServiceBusReceiveLinkProcessor(prefetchCount, retryPolicy, connectionProcessor));

        return new ServiceBusAsyncConsumer(linkMessageProcessor, messageSerializer, fullyQualifiedNamespace
        , entityPath, null );
    }

    public Mono<Void> abandon(UUID lockToken){
        return null;
    }

    public Mono<Void> complete(UUID lockToken){
        return null;
    }
    public Mono<Void> defer(UUID lockToken){

        return null;
    }
    public Mono<Void> deadLetter(UUID lockToken){
        return null;
    }

    public void registerMessageHandler(Supplier<Message> messageSupplier) {

    }
    public Instant renewMessageLock(UUID lockToken) {
        return null;
    }

    public Message receiveDeferredMessage(long sequenceNumber) {
        return null;
    }
    public Mono<Void> complete(UUID lockToken, TransactionContext context){
        return null;
    }
    public Mono<Void> deadLetter(UUID lockToken, TransactionContext context){
        return null;

    }
    public Mono<Void> abandon(UUID lockToken, TransactionContext context){
        return null;
    }
    public Mono<Void> defer(UUID lockToken, TransactionContext context){
        return null;
    }

}
