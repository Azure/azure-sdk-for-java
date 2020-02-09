package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.implementation.AmqpReceiveLink;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.RetryUtil;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.amqp.models.ReceiveOptions;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.ServiceBusConnectionProcessor;
import com.azure.messaging.servicebus.implementation.ServiceBusReceiveLinkProcessor;
import reactor.core.publisher.BaseSubscriber;
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
 * An <b>asynchronous</b> consumer responsible for reading {@link Message} from either a specific Event Hub partition
 * or all partitions in the context of a specific consumer group.
 *
 * <p><strong>Creating an {@link QueueReceiverAsyncClient}</strong></p>
 * {@codesnippet com.azure.messaging.eventhubs.eventhubconsumerasyncclient.instantiation}
 *
 * <p><strong>Consuming events a single partition from Event Hub</strong></p>
 * {@codesnippet com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receive#string-eventposition}
 *
 * <p><strong>Viewing latest partition information</strong></p>
 * <p>Latest partition information as events are received can by setting
 * TODO setTrackLastEnqueuedEventProperties} to
 * {@code true}. As events come in, explore the {@link Message} object.
 * {@codesnippet com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receiveFromPartition#string-eventposition-receiveoptions}
 *
 * <p><strong>Rate limiting consumption of events from Event Hub</strong></p>
 * <p>For event consumers that need to limit the number of events they receive at a given time, they can use
 * {@link BaseSubscriber#request(long)}.</p>
 * {@codesnippet com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receive#string-eventposition-basesubscriber}
 *
 * <p><strong>Receiving from all partitions</strong></p>
 * {@codesnippet com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receive#boolean}
 */
@ServiceClient(builder = QueueClientBuilder.class, isAsync = true)
public final class QueueReceiverAsyncClient implements Closeable {
    //private static final String RECEIVER_ENTITY_PATH_FORMAT = "%s/Partitions/%s";
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

    private final ReceiveOptions defaultReceiveOptions = new ReceiveOptions();

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
     * Gets the fully qualified Event Hubs namespace that the connection is associated with. This is likely similar to
     * {@code {yournamespace}.servicebus.windows.net}.
     *
     * @return The fully qualified Event Hubs namespace that the connection is associated with
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
     * Consumes events from all partitions starting from the beginning of each partition.
     *
     * <p>This method is <b>not</b> recommended for production use; the TODO should be used for
     * reading events from all partitions in a production scenario, as it offers a much more robust experience with
     * higher throughput.
     *
     * It is important to note that this method does not guarantee fairness amongst the partitions. Depending on service
     * communication, there may be a clustering of events per partition and/or there may be a noticeable bias for a
     * given partition or subset of partitions.</p>
     *
     *
     * @return A stream of events for every partition in the Event Hub starting from the beginning of each partition.
     */
   public Flux<Message> receive() {
        return receive(defaultReceiveOptions);
    }

    public Flux<Message> peek() {
        return receive(defaultReceiveOptions);
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
     * @param receiveOptions Options when receiving events from each Event Hub partition.
     *
     * @return A stream of events for every partition in the Event Hub.
     *
     * @throws NullPointerException if {@code receiveOptions} is null.
     */
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
    private ServiceBusAsyncConsumer createPartitionConsumer(String linkName, ReceiveOptions receiveOptions) {
        final String entityPath = String.format(Locale.US, RECEIVER_ENTITY_PATH_FORMAT, getQueueName());

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
