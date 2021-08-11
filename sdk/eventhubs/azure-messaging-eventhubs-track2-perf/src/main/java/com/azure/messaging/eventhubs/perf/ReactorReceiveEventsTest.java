// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpSession;
import com.azure.core.amqp.ExponentialAmqpRetryPolicy;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.implementation.AmqpReceiveLink;
import com.azure.core.amqp.implementation.AzureTokenManagerProvider;
import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.amqp.implementation.ReactorConnection;
import com.azure.core.amqp.implementation.ReactorHandlerProvider;
import com.azure.core.amqp.implementation.ReactorProvider;
import com.azure.core.amqp.implementation.ReactorReceiver;
import com.azure.core.amqp.implementation.StringUtil;
import com.azure.core.amqp.implementation.TokenManagerProvider;
import com.azure.core.amqp.models.CbsAuthorizationType;
import com.azure.core.util.ClientOptions;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.implementation.AmqpReceiveLinkProcessor;
import com.azure.messaging.eventhubs.implementation.ClientConstants;
import com.azure.messaging.eventhubs.implementation.EventHubSharedKeyCredential;
import com.azure.messaging.eventhubs.models.EventHubConnectionStringProperties;
import org.apache.qpid.proton.amqp.transport.ReceiverSettleMode;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.engine.SslDomain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

/**
 * Receives a set of events using {@link ReactorReceiver} rather than {@link EventHubConsumerAsyncClient} which has an
 * additional level of abstraction {@link AmqpReceiveLinkProcessor}.
 */
public class ReactorReceiveEventsTest extends ServiceTest<EventHubsReceiveOptions> {
    private static final String PRODUCT_NAME = "azure-messaging-eventhubs-track2-perf";
    private static final String VERSION_NAME = "1.0.0-beta.1";

    private final int totalMessagesToSend;
    private final ConnectionOptions connectionOptions;
    private final ExponentialAmqpRetryPolicy retryPolicy;

    private ReactorConnection connection;

    /**
     * Creates an instance of performance test.
     *
     * @param options the options configured for the test.
     */
    public ReactorReceiveEventsTest(EventHubsReceiveOptions options) {
        super(options);
        this.totalMessagesToSend = options.getCount() * 2;
        final EventHubConnectionStringProperties properties = EventHubConnectionStringProperties.parse(
            options.getConnectionString());
        final EventHubSharedKeyCredential credential = new EventHubSharedKeyCredential(
            properties.getSharedAccessKeyName(), properties.getSharedAccessKey(),
            Duration.ofMinutes(options.getDuration()));

        final Scheduler scheduler = Schedulers.boundedElastic();
        final AmqpRetryOptions retryOptions = new AmqpRetryOptions().setTryTimeout(Duration.ofMinutes(1));
        retryPolicy = new ExponentialAmqpRetryPolicy(retryOptions);

        connectionOptions = new ConnectionOptions(properties.getFullyQualifiedNamespace(), credential,
            CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, ClientConstants.AZURE_ACTIVE_DIRECTORY_SCOPE,
            options.getTransportType(), retryOptions, ProxyOptions.SYSTEM_DEFAULTS, scheduler, new ClientOptions(),
            SslDomain.VerifyMode.VERIFY_PEER_NAME, PRODUCT_NAME, VERSION_NAME);
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        return Mono.using(
            () -> createEventHubClientBuilder().buildAsyncProducerClient(),
            client -> sendMessages(client, options.getPartitionId(), totalMessagesToSend),
            client -> client.close());
    }

    @Override
    public Mono<Void> setupAsync() {
        final String connectionId = "MF-" + Instant.now().getEpochSecond();
        final TokenManagerProvider tokenManagerProvider = new AzureTokenManagerProvider(
            connectionOptions.getAuthorizationType(), connectionOptions.getFullyQualifiedNamespace(),
            connectionOptions.getAuthorizationScope());
        final ReactorProvider provider = new ReactorProvider();
        final ReactorHandlerProvider handlerProvider = new ReactorHandlerProvider(provider);
        final PerfMessageSerializer messageSerializer = new PerfMessageSerializer();
        connection = new ReactorConnection(connectionId,
            connectionOptions, provider, handlerProvider, tokenManagerProvider,
            messageSerializer, SenderSettleMode.SETTLED, ReceiverSettleMode.SECOND);

        return Mono.empty();
    }

    @Override
    public void run() {
        runAsync().block();
    }

    @Override
    public Mono<Void> runAsync() {
        Objects.requireNonNull(options.getConsumerGroup(), "'getConsumerGroup' requires a value.");
        Objects.requireNonNull(options.getPartitionId(), "'getPartitionId' requires a value.");

        return Flux.usingWhen(
            Mono.defer(() -> {
                final String sessionName = StringUtil.getRandomString(options.getPartitionId());
                return connection.createSession(sessionName);
            }),
            (AmqpSession session) -> {
                final String linkName = options.getPartitionId() + "-" + Instant.now().getEpochSecond();
                final String entityPath = String.format(Locale.ROOT, "%s/ConsumerGroups/%s/Partitions/%s",
                    options.getEventHubName(), options.getConsumerGroup(), options.getPartitionId());
                final Duration timeout = retryPolicy.getRetryOptions().getTryTimeout();

                final Mono<AmqpReceiveLink> createConsumer = session.createConsumer(linkName, entityPath, timeout,
                    retryPolicy).cast(AmqpReceiveLink.class);

                return Flux.usingWhen(createConsumer,
                    (AmqpReceiveLink consumer) -> {
                        consumer.setEmptyCreditListener(() -> {
                            // After prefetch has gotten to 0, it will call this.
                            return options.getCreditsAfterPrefetch();
                        });

                        // The link starts off with 0 credits, so we have to add some.
                        return consumer.addCredits(options.getPrefetch())
                            .thenMany(consumer.receive());
                    },
                    (AmqpReceiveLink consumer) -> consumer.closeAsync());
            },
            (AmqpSession session) -> session.closeAsync())
            .take(options.getCount())
            .then();
    }

    @Override
    public Mono<Void> cleanupAsync() {
        if (connection != null) {
            return Mono.whenDelayError(connection.closeAsync(), super.cleanupAsync());
        } else {
            return super.cleanupAsync();
        }
    }
}
