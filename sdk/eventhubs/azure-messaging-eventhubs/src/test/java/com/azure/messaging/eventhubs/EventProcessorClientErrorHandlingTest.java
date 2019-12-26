// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import static com.azure.messaging.eventhubs.EventHubClientBuilder.DEFAULT_PREFETCH_COUNT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.azure.messaging.eventhubs.implementation.ClientConstants;
import com.azure.messaging.eventhubs.implementation.PartitionProcessor;
import com.azure.messaging.eventhubs.models.Checkpoint;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionContext;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import com.azure.messaging.eventhubs.models.PartitionOwnership;
import com.azure.messaging.eventhubs.models.ReceiveOptions;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Unit tests for {@link EventProcessorClient} error handling.
 */
public class EventProcessorClientErrorHandlingTest {

    private static final String NAMESPACE_NAME = "dummyNamespaceName";
    private static final String DEFAULT_DOMAIN_NAME = "servicebus.windows.net/";
    private static final String EVENT_HUB_NAME = "eventHubName";
    private static final String SHARED_ACCESS_KEY_NAME = "dummySasKeyName";
    private static final String SHARED_ACCESS_KEY = "dummySasKey";
    private static final String ENDPOINT = getURI(ClientConstants.ENDPOINT_FORMAT, NAMESPACE_NAME, DEFAULT_DOMAIN_NAME)
        .toString();

    private static final String CORRECT_CONNECTION_STRING = String
        .format("Endpoint=%s;SharedAccessKeyName=%s;SharedAccessKey=%s;EntityPath=%s",
            ENDPOINT, SHARED_ACCESS_KEY_NAME, SHARED_ACCESS_KEY, EVENT_HUB_NAME);

    @Mock
    private EventHubClientBuilder eventHubClientBuilder;

    @Mock
    private EventHubAsyncClient eventHubAsyncClient;

    @Mock
    private EventHubConsumerAsyncClient eventHubConsumer;

    @Mock
    private EventData eventData1, eventData2, eventData3, eventData4;

    private CountDownLatch countDownLatch;

    private static URI getURI(String endpointFormat, String namespace, String domainName) {
        try {
            return new URI(String.format(Locale.US, endpointFormat, namespace, domainName));
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException(String.format(Locale.US,
                "Invalid namespace name: %s", namespace), exception);
        }
    }

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        when(eventHubClientBuilder.buildAsyncClient()).thenReturn(eventHubAsyncClient);
        when(eventHubAsyncClient.getPartitionIds()).thenReturn(Flux.just("1", "2", "3"));
        when(eventHubAsyncClient.getFullyQualifiedNamespace()).thenReturn("test-ns");
        when(eventHubAsyncClient.getEventHubName()).thenReturn("test-eh");
    }

    @ParameterizedTest(name = "{displayName} with [{arguments}]")
    @MethodSource("checkpointStoreSupplier")
    public void testCheckpointStoreErrors(CheckpointStore checkpointStore) throws InterruptedException {
        countDownLatch = new CountDownLatch(1);
        EventProcessorClient client = new EventProcessorClient(eventHubClientBuilder, "cg",
            () -> new TestPartitionProcessor(), checkpointStore, false,
            null, errorContext -> {
            countDownLatch.countDown();
            Assertions.assertEquals("NONE", errorContext.getPartitionContext().getPartitionId());
            Assertions.assertEquals("cg", errorContext.getPartitionContext().getConsumerGroup());
            Assertions.assertTrue(errorContext.getThrowable() instanceof IllegalStateException);
        });
        client.start();
        boolean completed = countDownLatch.await(3, TimeUnit.SECONDS);
        client.stop();
        Assertions.assertTrue(completed);
    }

    @Test
    public void testUserHandlerError() throws InterruptedException {
        countDownLatch = new CountDownLatch(1);
        when(eventHubAsyncClient.createConsumer("cg", DEFAULT_PREFETCH_COUNT)).thenReturn(eventHubConsumer);
        when(eventHubConsumer.receiveFromPartition(anyString(), any(EventPosition.class), any(ReceiveOptions.class)))
            .thenReturn(Flux.just(getEvent(eventData1)));
        EventProcessorClient client = new EventProcessorClient(eventHubClientBuilder, "cg",
            () -> new BadPartitionProcessor(countDownLatch), new InMemoryCheckpointStore(), false,
            null, errorContext -> { });
        client.start();
        boolean completed = countDownLatch.await(3, TimeUnit.SECONDS);
        client.stop();
        Assertions.assertTrue(completed);
    }

    private static Stream<Arguments> checkpointStoreSupplier() {
        return Stream.of(
            Arguments.of(new ListOwnershipErrorStore()),
            Arguments.of(new ClaimOwnershipErrorStore()),
            Arguments.of(new ListCheckpointErrorStore()));
    }

    private PartitionEvent getEvent(EventData event) {
        PartitionContext context = new PartitionContext("ns", "foo", "bar", "baz");
        return new PartitionEvent(context, event, null);
    }


    private static class ListOwnershipErrorStore implements CheckpointStore {

        @Override
        public Flux<PartitionOwnership> listOwnership(
            String fullyQualifiedNamespace, String eventHubName, String consumerGroup) {
            return Flux.error(new IllegalStateException("List ownership error"));
        }

        @Override
        public Flux<PartitionOwnership> claimOwnership(
            List<PartitionOwnership> requestedPartitionOwnerships) {
            return null;
        }

        @Override
        public Flux<Checkpoint> listCheckpoints(String fullyQualifiedNamespace,
            String eventHubName, String consumerGroup) {
            return null;
        }

        @Override
        public Mono<Void> updateCheckpoint(Checkpoint checkpoint) {
            return null;
        }
    }

    private static class ClaimOwnershipErrorStore implements CheckpointStore {

        @Override
        public Flux<PartitionOwnership> listOwnership(
            String fullyQualifiedNamespace, String eventHubName, String consumerGroup) {
            return Flux.empty();
        }

        @Override
        public Flux<PartitionOwnership> claimOwnership(
            List<PartitionOwnership> requestedPartitionOwnerships) {
            return Flux.error(new IllegalStateException("Claim Ownership error"));
        }

        @Override
        public Flux<Checkpoint> listCheckpoints(String fullyQualifiedNamespace,
            String eventHubName, String consumerGroup) {
            return null;
        }

        @Override
        public Mono<Void> updateCheckpoint(Checkpoint checkpoint) {
            return null;
        }
    }

    private static class ListCheckpointErrorStore implements CheckpointStore {

        @Override
        public Flux<PartitionOwnership> listOwnership(
            String fullyQualifiedNamespace, String eventHubName, String consumerGroup) {
            return Flux.empty();
        }

        @Override
        public Flux<PartitionOwnership> claimOwnership(
            List<PartitionOwnership> requestedPartitionOwnerships) {
            return Flux.fromIterable(requestedPartitionOwnerships);
        }

        @Override
        public Flux<Checkpoint> listCheckpoints(String fullyQualifiedNamespace,
            String eventHubName, String consumerGroup) {
            return Flux.error(new IllegalStateException("List checkpoint error"));
        }

        @Override
        public Mono<Void> updateCheckpoint(Checkpoint checkpoint) {
            return null;
        }
    }

    private static final class TestPartitionProcessor extends PartitionProcessor {

        @Override
        public void processEvent(EventContext eventContext) {
            eventContext.updateCheckpoint();
        }

        @Override
        public void processError(ErrorContext errorContext) {
            // do nothing
            return;
        }
    }

    private static final class BadPartitionProcessor extends PartitionProcessor {

        CountDownLatch countDownLatch;

        public BadPartitionProcessor(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void processEvent(EventContext eventContext) {
            countDownLatch.countDown();
            throw new IllegalStateException("User code error");
        }

        @Override
        public void processError(ErrorContext errorContext) {
            Assertions.fail("Process error handler should not be called when process event throws exception");
            return;
        }

    }
}
