// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpMessageConstant;
import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.implementation.AmqpSendLink;
import com.azure.core.amqp.implementation.CbsAuthorizationType;
import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.credential.TokenCredential;
import com.azure.messaging.eventhubs.implementation.ClientConstants;
import com.azure.messaging.eventhubs.implementation.EventHubAmqpConnection;
import com.azure.messaging.eventhubs.implementation.EventHubConnectionProcessor;
import com.azure.messaging.eventhubs.implementation.PartitionPublishingState;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.messaging.eventhubs.models.SendOptions;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class EventHubProducerAsyncClientIdempotentTest {
    private static final String HOSTNAME = "something.servicebus.windows.net";
    private static final String EVENT_HUB_NAME = "anEventHub";
    private static final String PARTITION_0 = "0";
    private static final String PARTITION_1 = "1";
    private static final String ENTITY_PATH_0 = EVENT_HUB_NAME + "/Partitions/" + PARTITION_0;
    private static final String ENTITY_PATH_1 = EVENT_HUB_NAME + "/Partitions/" + PARTITION_1;
    private static final String TEST_CONNECTION_STRING = "Endpoint=sb://something.servicebus.windows.net/;"
        + "SharedAccessKeyName=anAccessKeyName;"
        + "SharedAccessKey=anAccessKey;EntityPath=anEventHub";

    private static final Long PRODUCER_GROUP_ID = 1L;
    private static final Short PRODUCER_OWNER_LEVEL = (short) 10;
    private static final Integer PRODUCER_SEQ_NUMBER = 100;

    @Mock
    private AmqpSendLink sendLink;

    @Mock
    private AmqpSendLink sendLink2;

    @Mock
    private EventHubAmqpConnection connection;

    @Mock
    private TokenCredential tokenCredential;
    @Mock
    private Runnable onClientClosed;

    private final MessageSerializer messageSerializer = new EventHubMessageSerializer();
    private final AmqpRetryOptions retryOptions = new AmqpRetryOptions()
        .setDelay(Duration.ofMillis(500))
        .setMode(AmqpRetryMode.FIXED)
        .setTryTimeout(Duration.ofSeconds(5))
        .setMaxRetries(2);
    private final DirectProcessor<AmqpEndpointState> endpointProcessor = DirectProcessor.create();
    private final FluxSink<AmqpEndpointState> endpointSink = endpointProcessor.sink(FluxSink.OverflowStrategy.BUFFER);
    private EventHubProducerAsyncClient producer;
    private EventHubConnectionProcessor connectionProcessor;
    private TracerProvider tracerProvider;
    private ConnectionOptions connectionOptions;
    private final Scheduler testScheduler = Schedulers.newElastic("test");

    private PartitionPublishingProperties partition0InitialState;
    private Map<String, PartitionPublishingProperties> initialStates = new HashMap<>();

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @BeforeEach
    void setup(TestInfo testInfo) {
        MockitoAnnotations.initMocks(this);

        tracerProvider = new TracerProvider(Collections.emptyList());
        connectionOptions = new ConnectionOptions(HOSTNAME, tokenCredential,
            CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, AmqpTransportType.AMQP_WEB_SOCKETS, retryOptions,
            ProxyOptions.SYSTEM_DEFAULTS, testScheduler);

        when(connection.getEndpointStates()).thenReturn(endpointProcessor);
        endpointSink.next(AmqpEndpointState.ACTIVE);

        connectionProcessor = Mono.fromCallable(() -> connection).repeat(10).subscribeWith(
            new EventHubConnectionProcessor(connectionOptions.getFullyQualifiedNamespace(),
                EVENT_HUB_NAME, connectionOptions.getRetry()));

        partition0InitialState = new PartitionPublishingProperties(PRODUCER_GROUP_ID, PRODUCER_OWNER_LEVEL, PRODUCER_SEQ_NUMBER);
        initialStates = new HashMap<>();
        initialStates.put(PARTITION_0, partition0InitialState);

        Map<String, PartitionPublishingState> internalStates = new HashMap<>();
        initialStates.forEach((k, v) -> internalStates.put(k, new PartitionPublishingState(v)));
        producer = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME, connectionProcessor, retryOptions,
            tracerProvider, messageSerializer, testScheduler, false, onClientClosed,
            true, internalStates);

        Map<Symbol, Object> remoteProperties = new HashMap<>();
        remoteProperties.put(
            Symbol.getSymbol(AmqpMessageConstant.PRODUCER_EPOCH_ANNOTATION_NAME.getValue()),
            partition0InitialState.getOwnerLevel());
        remoteProperties.put(
            Symbol.getSymbol(AmqpMessageConstant.PRODUCER_SEQUENCE_NUMBER_ANNOTATION_NAME.getValue()),
            partition0InitialState.getSequenceNumber());
        remoteProperties.put(
            Symbol.getSymbol(AmqpMessageConstant.PRODUCER_ID_ANNOTATION_NAME.getValue()),
            partition0InitialState.getProducerGroupId());

        when(connection.createSendLink(eq(ENTITY_PATH_0), eq(ENTITY_PATH_0),
            eq(retryOptions), eq(true), any(PartitionPublishingState.class))).thenReturn(Mono.just(sendLink));
        when(sendLink.getRemoteProperties()).thenReturn(
            Mono.just(remoteProperties));
        when(sendLink.getLinkSize()).thenReturn(Mono.just(ClientConstants.MAX_MESSAGE_LENGTH_BYTES));
        when(sendLink.send(any(Message.class))).thenReturn(Mono.empty());
        when(sendLink.send(anyList())).thenReturn(Mono.empty());
    }

    @AfterEach
    void teardown(TestInfo testInfo) {
        testScheduler.dispose();
        Mockito.framework().clearInlineMocks();
        Mockito.reset(sendLink, connection);
    }

    @Test
    void buildClientIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> new EventHubClientBuilder()
            .connectionString(TEST_CONNECTION_STRING)
            .initialPartitionPublishingStates(initialStates)  // Not an idempotent producer. Shouldn't set.
            .buildAsyncProducerClient());
    }

    @Test
    void getPartitionPublishingProperties() {
        StepVerifier.create(producer.getPartitionPublishingProperties(PARTITION_0))
            .assertNext(properties -> {
                assertEquals(properties.getOwnerLevel(), partition0InitialState.getOwnerLevel());
                assertEquals(properties.getProducerGroupId(), partition0InitialState.getProducerGroupId());
                assertEquals(properties.getSequenceNumber(), partition0InitialState.getSequenceNumber());
            })
            .verifyComplete();
    }

    @Test
    void createEventDataBatch() {
        CreateBatchOptions options = new CreateBatchOptions();
        options.setPartitionId(PARTITION_0);
        StepVerifier.create(producer.createBatch(options))
            .assertNext(eventDataBatch -> {
                assertNull(eventDataBatch.getStartingPublishedSequenceNumber());
            })
            .verifyComplete();
    }

    @Test
    void createBatchWithoutPartitionId() {
        StepVerifier.create(producer.createBatch())
            .expectError(IllegalArgumentException.class)
            .verify();
    }

    @Test
    void sendEventDataBatch() {
        CreateBatchOptions options = new CreateBatchOptions();
        options.setPartitionId(PARTITION_0);
        EventDataBatch batch = producer.createBatch(options).block();
        assertNotNull(batch);
        EventData eventData = new EventData("This is a test event");
        batch.tryAdd(eventData);
        StepVerifier.create(producer.send(batch)).verifyComplete();
        assertEquals(eventData.getSystemProperties().get(AmqpMessageConstant.PRODUCER_ID_ANNOTATION_NAME.getValue()),
            PRODUCER_GROUP_ID);
        assertEquals(eventData.getSystemProperties().get(AmqpMessageConstant.PRODUCER_EPOCH_ANNOTATION_NAME.getValue()),
            PRODUCER_OWNER_LEVEL);
        assertEquals(eventData.getSystemProperties().get(
            AmqpMessageConstant.PRODUCER_SEQUENCE_NUMBER_ANNOTATION_NAME.getValue()),
            PRODUCER_SEQ_NUMBER);
        assertEquals(batch.getStartingPublishedSequenceNumber(), PRODUCER_SEQ_NUMBER);

        StepVerifier.create(producer.getPartitionPublishingState(PARTITION_0))
            .assertNext(state -> {
                assertEquals(state.getSequenceNumber(), PRODUCER_SEQ_NUMBER + batch.getCount());
            }).verifyComplete();
    }

    @Test
    void sendEventList() {
        EventData eventData = new EventData("This is a test event");
        List<EventData> eventDataList = new ArrayList<>();
        eventDataList.add(eventData);
        StepVerifier.create(producer.send(eventDataList, new SendOptions().setPartitionId(PARTITION_0))).verifyComplete();
        assertEquals(eventData.getSystemProperties().get(AmqpMessageConstant.PRODUCER_ID_ANNOTATION_NAME.getValue()),
            PRODUCER_GROUP_ID);
        assertEquals(eventData.getSystemProperties().get(AmqpMessageConstant.PRODUCER_EPOCH_ANNOTATION_NAME.getValue()),
            PRODUCER_OWNER_LEVEL);
        assertEquals(eventData.getSystemProperties().get(
            AmqpMessageConstant.PRODUCER_SEQUENCE_NUMBER_ANNOTATION_NAME.getValue()),
            PRODUCER_SEQ_NUMBER);

        StepVerifier.create(producer.getPartitionPublishingState(PARTITION_0))
            .assertNext(state -> {
                assertEquals(state.getSequenceNumber(), PRODUCER_SEQ_NUMBER + eventDataList.size());
            }).verifyComplete();
    }

    @Test
    void sendEventDataListFail() {
        Map<Symbol, Object> remoteProperties1 = new HashMap<>();
        remoteProperties1.put(
            Symbol.getSymbol(AmqpMessageConstant.PRODUCER_EPOCH_ANNOTATION_NAME.getValue()),
            partition0InitialState.getOwnerLevel());
        remoteProperties1.put(
            Symbol.getSymbol(AmqpMessageConstant.PRODUCER_SEQUENCE_NUMBER_ANNOTATION_NAME.getValue()),
            partition0InitialState.getSequenceNumber());
        remoteProperties1.put(
            Symbol.getSymbol(AmqpMessageConstant.PRODUCER_ID_ANNOTATION_NAME.getValue()),
            partition0InitialState.getProducerGroupId());

        when(connection.createSendLink(eq(ENTITY_PATH_1), eq(ENTITY_PATH_1),
            eq(retryOptions), eq(true), any(PartitionPublishingState.class))).thenReturn(Mono.just(sendLink2));
        when(sendLink2.getRemoteProperties()).thenReturn(
            Mono.just(remoteProperties1));
        when(sendLink2.getLinkSize()).thenReturn(Mono.just(ClientConstants.MAX_MESSAGE_LENGTH_BYTES));
        when(sendLink2.send(any(Message.class))).thenReturn(Mono.error(new RuntimeException("simulated error")));
        when(sendLink2.send(anyList())).thenReturn(Mono.error(new RuntimeException("simulated error")));

        EventData eventData = new EventData("This is a test event");
        List<EventData> eventDataList = new ArrayList<>();
        eventDataList.add(eventData);
        StepVerifier.create(producer.send(eventDataList)).expectError(RuntimeException.class).verify();
        StepVerifier.create(producer.getPartitionPublishingProperties("1"))
            .assertNext(publishingProperties -> {
                assertEquals(publishingProperties.getProducerGroupId(), 1);
                assertEquals(publishingProperties.getOwnerLevel(), (short) 10);
                assertEquals(publishingProperties.getSequenceNumber(), 100);
            })
            .verifyComplete();
        assertNull(eventData.getPublishedGroupId());
        assertNull(eventData.getPublishedOwnerLevel());
        assertNull(eventData.getPublishedSequenceNumber());
    }

    @Test
    void sendEventDataListWithoutPartition() {
        EventData eventData = new EventData("This is a test event");
        List<EventData> eventDataList = new ArrayList<>();
        eventDataList.add(eventData);
        StepVerifier.create(producer.send(eventDataList)).verifyError(IllegalArgumentException.class);
    }

    @Test
    void sendEventBatchesToSamePartitionConcurrently() {
        CreateBatchOptions options = new CreateBatchOptions();
        options.setPartitionId(PARTITION_0);
        EventDataBatch batch1 = producer.createBatch(options).block();
        assertNotNull(batch1);
        EventData eventData1 = new EventData("This is a test event");
        batch1.tryAdd(eventData1);

        EventDataBatch batch2 = producer.createBatch(options).block();
        assertNotNull(batch2);
        EventData eventData2 = new EventData("This is a test event");
        batch2.tryAdd(eventData2);

        assertNull(eventData1.getPublishedSequenceNumber());
        assertNull(eventData1.getPublishedGroupId());
        assertNull(eventData1.getPublishedOwnerLevel());
        assertNull(batch1.getStartingPublishedSequenceNumber());

        StepVerifier.create(Mono.when(producer.send(batch1), producer.send(batch2))).verifyComplete();

        assertEquals(eventData1.getPublishedSequenceNumber(), PRODUCER_SEQ_NUMBER);
        assertEquals(eventData1.getPublishedGroupId(), PRODUCER_GROUP_ID);
        assertEquals(eventData1.getPublishedOwnerLevel(), PRODUCER_OWNER_LEVEL);
        assertEquals(batch1.getStartingPublishedSequenceNumber(), PRODUCER_SEQ_NUMBER);

        assertEquals(eventData2.getPublishedSequenceNumber(), PRODUCER_SEQ_NUMBER + 1);
        assertEquals(eventData2.getPublishedGroupId(), PRODUCER_GROUP_ID);
        assertEquals(eventData2.getPublishedOwnerLevel(), PRODUCER_OWNER_LEVEL);
        assertEquals(batch2.getStartingPublishedSequenceNumber(), PRODUCER_SEQ_NUMBER + 1);

        StepVerifier.create(producer.getPartitionPublishingState(PARTITION_0))
            .assertNext(state -> {
                assertEquals(state.getSequenceNumber(), PRODUCER_SEQ_NUMBER + batch1.getCount() + batch2.getCount());
            }).verifyComplete();
    }

    @Test
    void sendEventBatchesToTwoPartitionsConcurrently() {
        Map<Symbol, Object> remoteProperties1 = new HashMap<>();
        remoteProperties1.put(
            Symbol.getSymbol(AmqpMessageConstant.PRODUCER_EPOCH_ANNOTATION_NAME.getValue()),
            partition0InitialState.getOwnerLevel());
        remoteProperties1.put(
            Symbol.getSymbol(AmqpMessageConstant.PRODUCER_SEQUENCE_NUMBER_ANNOTATION_NAME.getValue()),
            partition0InitialState.getSequenceNumber());
        remoteProperties1.put(
            Symbol.getSymbol(AmqpMessageConstant.PRODUCER_ID_ANNOTATION_NAME.getValue()),
            partition0InitialState.getProducerGroupId());

        when(connection.createSendLink(eq(ENTITY_PATH_1), eq(ENTITY_PATH_1),
            eq(retryOptions), eq(true), any(PartitionPublishingState.class))).thenReturn(Mono.just(sendLink2));
        when(sendLink2.getRemoteProperties()).thenReturn(
            Mono.just(remoteProperties1));
        when(sendLink2.getLinkSize()).thenReturn(Mono.just(ClientConstants.MAX_MESSAGE_LENGTH_BYTES));
        when(sendLink2.send(any(Message.class))).thenReturn(Mono.empty());
        when(sendLink2.send(anyList())).thenReturn(Mono.empty());

        CreateBatchOptions options = new CreateBatchOptions();
        options.setPartitionId(PARTITION_0);
        EventDataBatch batch1 = producer.createBatch(options).block();
        assertNotNull(batch1);
        EventData eventData1 = new EventData("This is a test event");
        batch1.tryAdd(eventData1);
        try {
            EventDataBatch batch2 = producer.createBatch(new CreateBatchOptions()
                .setPartitionId(PARTITION_1)).block();
            assertNotNull(batch2);
            EventData eventData2 = new EventData("This is a test event");
            batch2.tryAdd(eventData2);

            StepVerifier.create(Mono.when(producer.send(batch1), producer.send(batch2)))
                .verifyComplete();

            assertEquals(eventData1.getPublishedSequenceNumber(), PRODUCER_SEQ_NUMBER);
            assertEquals(eventData2.getPublishedSequenceNumber(), PRODUCER_SEQ_NUMBER);

            StepVerifier.create(producer.getPartitionPublishingState(PARTITION_0))
                .assertNext(state -> {
                    assertEquals(state.getSequenceNumber(), PRODUCER_SEQ_NUMBER + batch1.getCount());
                }).verifyComplete();

            StepVerifier.create(producer.getPartitionPublishingState(PARTITION_1))
                .assertNext(state -> {
                    assertEquals(state.getSequenceNumber(), PRODUCER_SEQ_NUMBER + batch2.getCount());
                }).verifyComplete();
        } finally {
            Mockito.reset(sendLink2);
        }
    }
}
