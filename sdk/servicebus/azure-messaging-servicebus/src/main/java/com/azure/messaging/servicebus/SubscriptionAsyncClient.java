package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.implementation.AmqpReceiveLink;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.RetryUtil;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.amqp.models.ReceiveOptions;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.ServiceBusConnectionProcessor;
import com.azure.messaging.servicebus.implementation.ServiceBusReceiveLinkProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import java.io.Closeable;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static com.azure.core.util.FluxUtil.fluxError;

public final class SubscriptionAsyncClient implements Closeable {
    private static final String RECEIVER_ENTITY_PATH_FORMAT = "%s";

    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final ClientLogger logger = new ClientLogger(QueueReceiverAsyncClient.class);
    private final String fullyQualifiedNamespace;
    private final String entityPath;
    private final ServiceBusConnectionProcessor connectionProcessor;
    private final MessageSerializer messageSerializer;
    private final int prefetchCount;
    private final boolean isSharedConnection;
    private final TracerProvider tracerProvider;

    private final ReceiveOptions defaultReceiveOptions = new ReceiveOptions();

    private final ConcurrentHashMap<String, ServiceBusAsyncConsumer> openConsumers =
        new ConcurrentHashMap<>();

    SubscriptionAsyncClient(String fullyQualifiedNamespace, String entityPath,
                             ServiceBusConnectionProcessor connectionProcessor, TracerProvider tracerProvider ,
                             MessageSerializer messageSerializer,int prefetchCount, boolean isSharedConnection) {
        this.fullyQualifiedNamespace = fullyQualifiedNamespace;
        this.entityPath = entityPath;
        this.connectionProcessor = connectionProcessor;
        this.messageSerializer = messageSerializer;
        this.prefetchCount = prefetchCount;
        this.isSharedConnection = isSharedConnection;
        this.tracerProvider = tracerProvider;
    }

    public Flux<Message> receive() {
        return receive(defaultReceiveOptions);
    }

    public Flux<Message> peek() {
        return receive(defaultReceiveOptions);
    }




    public Flux<Message> receive( ReceiveOptions receiveOptions) {
        if (Objects.isNull(receiveOptions)) {
            return fluxError(logger, new NullPointerException("'receiveOptions' cannot be null."));
        }

        final String linkName = connectionProcessor.getEntityPath();
        return createConsumer(linkName, receiveOptions);
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

    private Flux<Message> createConsumer(String linkName, ReceiveOptions receiveOptions) {
        return openConsumers
            .computeIfAbsent(linkName, name -> {
                logger.info("{}: Creating receive consumer.", linkName);
                return createPartitionConsumer(name, receiveOptions);
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

    public String getTopicName() {
        return null;
    }
    private ServiceBusAsyncConsumer createPartitionConsumer(String linkName, ReceiveOptions receiveOptions) {
        final String entityPath = String.format(Locale.US, RECEIVER_ENTITY_PATH_FORMAT, getTopicName());

        final Flux<AmqpReceiveLink> receiveLinkMono =
            connectionProcessor.flatMap(connection ->
                connection.createReceiveLink(linkName, entityPath, receiveOptions))
                .doOnNext(next -> logger.verbose("Creating consumer for path: {}", next.getEntityPath()))
                .repeat();

        final AmqpRetryPolicy retryPolicy = RetryUtil.getRetryPolicy(connectionProcessor.getRetryOptions());
        final ServiceBusReceiveLinkProcessor linkMessageProcessor = receiveLinkMono.subscribeWith(
            new ServiceBusReceiveLinkProcessor(prefetchCount, retryPolicy, connectionProcessor));

        return new ServiceBusAsyncConsumer(linkMessageProcessor, messageSerializer, fullyQualifiedNamespace
            , entityPath, null );
    }

    public Mono<Void>  abandon(UUID lockToken){
        return null;
    }
    public Mono<Void>  complete(UUID lockToken){
        return null;
    }
    public Mono<Void>  complete(UUID lockToken, TransactionContext context){
        return null;
    }
    public Mono<Void>  deadLetter(UUID lockToken, TransactionContext context){
        return null;
    }
    public Mono<Void>  abandon(UUID lockToken, TransactionContext context){
        return null;
    }

    public Mono<Void> deadLetter(UUID lockToken){
        return null;
    }

    public Mono<Void>  registerMessageHandler(Supplier<Message> messageSupplier) {
        return null;
    }
    public Mono<Void> defer(UUID lockToken){
        return null;
    }


}
