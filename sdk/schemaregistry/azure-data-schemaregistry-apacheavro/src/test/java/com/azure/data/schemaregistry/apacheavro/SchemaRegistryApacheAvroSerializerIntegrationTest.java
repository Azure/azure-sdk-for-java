// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.apacheavro;

import com.azure.core.credential.TokenCredential;
import com.azure.core.models.MessageContent;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.serializer.TypeReference;
import com.azure.data.schemaregistry.SchemaRegistryClient;
import com.azure.data.schemaregistry.SchemaRegistryClientBuilder;
import com.azure.data.schemaregistry.apacheavro.generatedtestsources.HandOfCards;
import com.azure.data.schemaregistry.apacheavro.generatedtestsources.Person;
import com.azure.data.schemaregistry.apacheavro.generatedtestsources.PlayingCard;
import com.azure.data.schemaregistry.apacheavro.generatedtestsources.PlayingCardSuit;
import com.azure.data.schemaregistry.models.SchemaFormat;
import com.azure.data.schemaregistry.models.SchemaProperties;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.messaging.eventhubs.PartitionProperties;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.SendOptions;
import org.apache.avro.Schema;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests end to end experience of the schema registry class.
 */
public class SchemaRegistryApacheAvroSerializerIntegrationTest extends TestProxyTestBase {
    static final String SCHEMA_REGISTRY_AVRO_FULLY_QUALIFIED_NAMESPACE = "SCHEMA_REGISTRY_AVRO_FULLY_QUALIFIED_NAMESPACE";
    static final String SCHEMA_REGISTRY_GROUP = "SCHEMA_REGISTRY_GROUP";
    static final String SCHEMA_REGISTRY_AVRO_EVENT_HUB_NAME = "SCHEMA_REGISTRY_AVRO_EVENT_HUB_NAME";
    static final String SCHEMA_REGISTRY_AVRO_EVENT_HUB_CONNECTION_STRING = "SCHEMA_REGISTRY_AVRO_EVENT_HUB_CONNECTION_STRING";

    // When we regenerate recordings, make sure that the schema group matches what we are persisting.
    static final String PLAYBACK_TEST_GROUP = "azsdk_java_group";
    static final String PLAYBACK_ENDPOINT = "https://foo.servicebus.windows.net";

    private String schemaGroup;
    private SchemaRegistryClientBuilder builder;
    private String eventHubName;
    private String connectionString;

    @Override
    protected void beforeTest() {
        String endpoint;
        TokenCredential tokenCredential;
        if (interceptorManager.isPlaybackMode()) {
            tokenCredential = new MockTokenCredential();
            schemaGroup = PLAYBACK_TEST_GROUP;

            endpoint = PLAYBACK_ENDPOINT;
            eventHubName = "javaeventhub";
            connectionString = "foo-bar";
        } else {
            tokenCredential = new DefaultAzureCredentialBuilder().build();
            endpoint = System.getenv(SCHEMA_REGISTRY_AVRO_FULLY_QUALIFIED_NAMESPACE);
            eventHubName = System.getenv(SCHEMA_REGISTRY_AVRO_EVENT_HUB_NAME);
            schemaGroup = System.getenv(SCHEMA_REGISTRY_GROUP);
            connectionString = System.getenv(SCHEMA_REGISTRY_AVRO_EVENT_HUB_CONNECTION_STRING);

            assertNotNull(eventHubName, "'eventHubName' cannot be null in LIVE/RECORD mode.");
            assertNotNull(endpoint, "'endpoint' cannot be null in LIVE/RECORD mode.");
            assertNotNull(schemaGroup, "'schemaGroup' cannot be null in LIVE/RECORD mode.");
            assertNotNull(connectionString, "'connectionString' cannot be null in LIVE/RECORD mode.");
        }

        builder = new SchemaRegistryClientBuilder()
            .credential(tokenCredential)
            .fullyQualifiedNamespace(endpoint);

        if (interceptorManager.isPlaybackMode()) {
            builder.httpClient(interceptorManager.getPlaybackClient());
        } else if (interceptorManager.isRecordMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }
    }

    @Override
    protected void afterTest() {
        Mockito.framework().clearInlineMock(this);
    }

    /**
     * Verifies that we can register a schema, fetch it, and deserialize it.
     */
    @Test
    public void registerAndGetSchema() {
        // Arrange
        final SchemaRegistryClient registryClient = builder.buildClient();
        final SchemaRegistryApacheAvroSerializer encoder = new SchemaRegistryApacheAvroSerializerBuilder()
            .schemaGroup(schemaGroup)
            .schemaRegistryClient(builder.buildAsyncClient())
            .avroSpecificReader(true)
            .buildSerializer();

        final PlayingCard playingCard = PlayingCard.newBuilder()
            .setCardValue(1)
            .setPlayingCardSuit(PlayingCardSuit.SPADES)
            .setIsFaceCard(false)
            .build();
        final PlayingCard playingCard2 = PlayingCard.newBuilder()
            .setCardValue(11)
            .setIsFaceCard(true)
            .setPlayingCardSuit(PlayingCardSuit.DIAMONDS)
            .build();
        final ArrayList<PlayingCard> allCards = new ArrayList<>();
        allCards.add(playingCard);
        allCards.add(playingCard2);

        final HandOfCards cards = HandOfCards.newBuilder()
            .setCards(allCards)
            .build();

        // Register a schema first.
        final Schema handOfCardsSchema = HandOfCards.SCHEMA$;
        final SchemaProperties schemaProperties = registryClient.registerSchema(schemaGroup,
            handOfCardsSchema.getFullName(), handOfCardsSchema.toString(), SchemaFormat.AVRO);

        assertNotNull(schemaProperties);

        // Act & Assert
        final MessageContent encodedMessage = encoder.serialize(cards,
            TypeReference.createInstance(MessageContent.class));
        assertNotNull(encodedMessage);

        final byte[] outputArray = encodedMessage.getBodyAsBinaryData().toBytes();
        assertTrue(outputArray.length > 0, "There should have been contents in array.");

        final HandOfCards actual = encoder.deserialize(encodedMessage,
            TypeReference.createInstance(HandOfCards.class));

        assertNotNull(actual);
        assertNotNull(actual.getCards());
        assertEquals(cards.getCards().size(), actual.getCards().size());
    }

    /**
     * Verifies that an event can be sent to Event Hubs and deserialized.
     */
    @Test
    public void serializeAndDeserializeEventData() {
        Assumptions.assumeFalse(interceptorManager.isPlaybackMode(),
            "Cannot run this test in playback mode because it uses AMQP and Event Hubs calls.");

        // Arrange
        final SchemaRegistryApacheAvroSerializer serializer = new SchemaRegistryApacheAvroSerializerBuilder()
            .schemaGroup(schemaGroup)
            .schemaRegistryClient(builder.buildAsyncClient())
            .autoRegisterSchemas(true)
            .avroSpecificReader(true)
            .buildSerializer();

        final PlayingCard playingCard = PlayingCard.newBuilder()
            .setCardValue(1)
            .setPlayingCardSuit(PlayingCardSuit.SPADES)
            .setIsFaceCard(false)
            .build();
        final String uuid = UUID.randomUUID().toString();
        final String applicationKey = "SCHEMA_REGISTRY_KEY";
        final EventData event = serializer.serialize(playingCard, TypeReference.createInstance(EventData.class));
        final String partitionId = "0";
        event.getProperties().put(applicationKey, uuid);

        EventHubProducerClient producer = null;
        EventHubConsumerAsyncClient consumer = null;
        try {
            producer = new EventHubClientBuilder()
                .connectionString(connectionString, eventHubName)
                .buildProducerClient();
            consumer = new EventHubClientBuilder()
                .connectionString(connectionString, eventHubName)
                .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
                .buildAsyncConsumerClient();

            final PartitionProperties partitionProperties = producer.getPartitionProperties(partitionId);
            final EventPosition last = EventPosition.fromSequenceNumber(partitionProperties.getLastEnqueuedSequenceNumber());

            producer.send(Collections.singleton(event), new SendOptions().setPartitionId(partitionId));

            StepVerifier.create(consumer.receiveFromPartition(partitionId, last).publishOn(Schedulers.boundedElastic()))
                .assertNext(partitionEvent -> {
                    final PlayingCard deserialize = serializer.deserialize(partitionEvent.getData(),
                        TypeReference.createInstance(PlayingCard.class));

                    assertEquals(playingCard, deserialize);
                })
                .thenCancel()
                .verify(Duration.ofMinutes(2));
        } finally {
            if (producer != null) {
                producer.close();
            }
            if (consumer != null) {
                consumer.close();
            }
        }
    }

    /**
     * Tests that we auto-register and use cached versions.
     */
    @Test
    public void autoRegisterSchema() {
        // Arrange
        final SchemaRegistryApacheAvroSerializer serializer = new SchemaRegistryApacheAvroSerializerBuilder()
            .schemaGroup(schemaGroup)
            .schemaRegistryClient(builder.buildAsyncClient())
            .avroSpecificReader(true)
            .autoRegisterSchemas(true)
            .buildSerializer();

        final Person person = Person.newBuilder()
            .setFavouriteColour("Blue")
            .setFavouriteNumber(10)
            .setName("Joe")
            .build();

        // Act
        final MessageContent message = serializer.serialize(person, TypeReference.createInstance(MessageContent.class));
        assertNotNull(message);

        // Should use the cached version of the schema.
        final MessageContent message2 = serializer.serialize(person, TypeReference.createInstance(MessageContent.class));
        assertNotNull(message2);

        // This should also use the cached version.
        final Person deserialized = serializer.deserialize(message, TypeReference.createInstance(Person.class));
        assertEquals(person, deserialized);
    }
}
