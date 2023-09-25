// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.credential.TokenCredential;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.LastEnqueuedEventProperties;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import com.azure.messaging.eventhubs.models.ReceiveOptions;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public final class ConsumePartitionEventsAsyncRetrySample {
    public static void main(String[] args) throws Exception {
        // "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // "<<event-hub-name>>" will be the name of the Event Hub instance you created inside the Event Hubs namespace.
        final String fullyQualifiedNamespace = "<<fully-qualified-namespace>>";
        final String eventHubName = "<<event-hub-name>>";
        //
        // The "$Default" consumer group is created by default. This value can be found by going to the Event Hub
        // instance you are connecting to, and selecting the "Consumer groups" page.
        final String consumerGroup = "$Default";
        //
        // "<<partition-id>>" is the identifier of the partition within the Event Hub.
        final String partitionId = "<<partition-id>>";
        //
        // The position to start reading events from, latest() means receive from the next event to be enqueued.
        final EventPosition initialEventPosition = EventPosition.latest();
        //
        // The credential used is DefaultAzureCredential because it combines commonly used credentials
        // in deployment and development and chooses the credential to used based on its running environment.
        // More information can be found at: https://learn.microsoft.com/java/api/overview/azure/identity-readme
        final TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
        //
        // The handler to process each event from the partition identified by the 'partitionId'.
        final Consumer<PartitionEvent> eventConsumer = new Consumer<PartitionEvent>() {
            @Override
            public void accept(PartitionEvent partitionEvent) {
                System.out.println("Processing the event at offset: " + partitionEvent.getData().getOffset());
            }
        };
        //
        // The handler that will be notified if receiving from the partition terminated due to unrecoverable error.
        final Consumer<Throwable> terminalErrorConsumer = new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                System.err.println("Unrecoverable error occurred");
                throwable.printStackTrace();
            }
        };

        final RetriablePartitionReceiver receiver = new RetriablePartitionReceiver(fullyQualifiedNamespace, eventHubName,
            consumerGroup, partitionId, initialEventPosition, tokenCredential);
        //
        // Start pumping events from the partition to the 'eventConsumer'.
        receiver.startReceive(eventConsumer, terminalErrorConsumer);
        //
        // Receive events for 5 minutes and then
        TimeUnit.MINUTES.sleep(5);
        //
        // dispose the receiver.
        receiver.close();
    }

    private static final class RetriablePartitionReceiver implements AutoCloseable {
        private static final String PARTITION_ID_KEY = "PartitionId";
        private static final Duration RETRY_WAIT_TIME = Duration.ofSeconds(5);
        private static final ReceiveOptions RECEIVE_OPTIONS = new ReceiveOptions().setTrackLastEnqueuedEventProperties(true);
        private final ClientLogger logger;
        private final String fullyQualifiedNamespace;
        private final String eventHubName;
        private final String consumerGroupName;
        private final String partitionId;
        private final EventPosition initialEventPosition;
        private final TokenCredential credential;
        private final Disposable.Composite subscription = Disposables.composite();
        private final AtomicReference<LastEnqueuedEventProperties> lastEnqueuedEventProperties = new AtomicReference<>();
        private final AtomicReference<State> currentState = new AtomicReference<>(null);

        RetriablePartitionReceiver(String fullyQualifiedNamespace, String eventHubName, String consumerGroupName,
                String partitionId, EventPosition initialEventPosition, TokenCredential credential) {
            final HashMap<String, Object> logContext = new HashMap<>(1);
            logContext.put(PARTITION_ID_KEY, partitionId);
            this.logger = new ClientLogger(RetriablePartitionReceiver.class, logContext);

            this.fullyQualifiedNamespace = Objects.requireNonNull(fullyQualifiedNamespace, "fullyQualifiedNamespace cannot be null.");
            this.eventHubName = Objects.requireNonNull(eventHubName, "eventHubName cannot be null.");
            this.consumerGroupName = consumerGroupName != null ? consumerGroupName : EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME;
            this.partitionId = Objects.requireNonNull(partitionId, "partitionId cannot be null.");
            this.initialEventPosition = Objects.requireNonNull(initialEventPosition, "initialEventPosition cannot be null.");
            this.credential = Objects.requireNonNull(credential, "credential cannot be null.");
        }

        void startReceive(Consumer<PartitionEvent> eventConsumer, Consumer<Throwable> terminalErrorConsumer) {
            Objects.requireNonNull(eventConsumer, "eventConsumer cannot be null");
            if (!currentState.compareAndSet(null, State.INIT)) {
                throw logger.logExceptionAsError(
                    new IllegalStateException("startReceive() cannot be called more than once or after RetriablePartitionReceiver is closed."));
            }

            final Flux<PartitionEvent> events = Flux.using(
                    () -> buildClient(),
                    client ->  {
                        return client.receiveFromPartition(partitionId, getEventStartingPosition(), RECEIVE_OPTIONS)
                            .publishOn(Schedulers.boundedElastic(), 1)
                            .doOnNext(event -> {
                                lastEnqueuedEventProperties.set(event.getLastEnqueuedEventProperties());
                            })
                            .switchIfEmpty(Mono.error(new RuntimeException("Current PartitionReceiver encountered terminal completion.")));
                    },
                    client -> {
                        logger.atVerbose().log("Disposing current PartitionReceiver.");
                        client.close();
                    })
                .retryWhen(
                    Retry.fixedDelay(Long.MAX_VALUE, RETRY_WAIT_TIME)
                        .filter(throwable -> {
                            if (currentState.get() == State.TERMINATED) {
                                return false;
                            }
                            if (throwable instanceof Exception) {
                                logger.atWarning()
                                    .log("Current PartitionReceiver encountered error, retrying..", throwable);
                                return true;
                            } else {
                                logger.atVerbose()
                                    .log("Current PartitionReceiver encountered non-recoverable error.", throwable);
                                return false;
                            }
                        }));

            final Disposable disposable;
            if (terminalErrorConsumer == null) {
                disposable = events.subscribe(eventConsumer);
            } else {
                disposable = events.subscribe(eventConsumer, terminalErrorConsumer);
            }
            subscription.add(disposable);
        }

        @Override
        public void close() {
            subscription.dispose();
            final State state = currentState.getAndSet(State.TERMINATED);
            if (state == null) {
                return;
            }
            state.closeClient();
        }

        private EventHubConsumerAsyncClient buildClient() {
            if (currentState.get() == State.TERMINATED) {
                throw logger.atError()
                    .log(new IllegalStateException("Cannot perform receive on the closed RetriablePartitionReceiver."));
            }
            final EventHubConsumerAsyncClient client = new EventHubClientBuilder()
                .credential(fullyQualifiedNamespace, eventHubName, credential)
                .consumerGroup(consumerGroupName)
                .buildAsyncConsumerClient();
            if (currentState.getAndSet(new State(client)) == State.TERMINATED) {
                client.close();
                throw logger.atError()
                    .log(new IllegalStateException("Cannot perform receive on the closed RetriablePartitionReceiver."));
            } else {
                logger.atInfo().log("Created a new PartitionReceiver.");
                return client;
            }
        }

        private EventPosition getEventStartingPosition() {
            final LastEnqueuedEventProperties properties = lastEnqueuedEventProperties.get();
            if (properties == null) {
                return initialEventPosition;
            } else {
                return EventPosition.fromSequenceNumber(properties.getSequenceNumber());
            }
        }

        private static final class State {
            private static final State INIT = new State();
            private static final State TERMINATED = new State();
            private final EventHubConsumerAsyncClient client;

            State(EventHubConsumerAsyncClient client) {
                this.client = Objects.requireNonNull(client);
            }

            void closeClient() {
                if (client != null) {
                    client.close();
                }
            }

            private State() {
                // Private ctr only used by init(), terminated().
                this.client = null;
            }
        }
    }
}
