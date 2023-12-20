// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.schemaregistry.apacheavro;

import com.azure.core.credential.TokenCredential;
import com.azure.core.util.IterableStream;
import com.azure.core.util.serializer.TypeReference;
import com.azure.data.schemaregistry.SchemaRegistryAsyncClient;
import com.azure.data.schemaregistry.SchemaRegistryClientBuilder;
import com.azure.data.schemaregistry.apacheavro.generatedtestsources.PlayingCard;
import com.azure.data.schemaregistry.apacheavro.generatedtestsources.PlayingCardSuit;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerClient;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import com.azure.messaging.eventhubs.models.SendOptions;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Demonstrates how to use Schema Registry with Event Hubs to publish an event.
 */
public class SchemaRegistryWithEventHubs {
    /**
     * Main method to run this sample.
     *
     * @param args Ignore arguments.
     */
    public static void main(String[] args) {
        // Create AAD token credential
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        // Create the schema registry async client
        SchemaRegistryAsyncClient schemaRegistryAsyncClient = new SchemaRegistryClientBuilder()
            .credential(tokenCredential)
            .fullyQualifiedNamespace("{schema-registry-endpoint}")
            .buildAsyncClient();

        // Create the encoder instance by configuring it with the schema registry client and
        // enabling auto registering of new schemas
        SchemaRegistryApacheAvroSerializer serializer = new SchemaRegistryApacheAvroSerializerBuilder()
            .schemaRegistryClient(schemaRegistryAsyncClient)
            .schemaGroup("{schema-group}")
            .avroSpecificReader(true)
            .buildSerializer();

        publishEventsToEventHubs(tokenCredential, serializer);

        consumeEventsFromEventHubs(tokenCredential, serializer);
    }

    /**
     * Demonstrates how to serialize PlayingCard objects into EventData and publish them to an Event Hub.
     *
     * @param tokenCredential Token credential used to authenticate with Schema Registry and Event Hubs.
     * @param serializer Schema Registry serializer.
     */
    private static void publishEventsToEventHubs(TokenCredential tokenCredential,
        SchemaRegistryApacheAvroSerializer serializer) {

        List<PlayingCard> playingCards = Arrays.asList(
            PlayingCard.newBuilder().setIsFaceCard(true).setCardValue(5).setPlayingCardSuit(PlayingCardSuit.CLUBS).build(),
            PlayingCard.newBuilder().setIsFaceCard(true).setCardValue(10).setPlayingCardSuit(PlayingCardSuit.SPADES).build()
        );

        // Sending all events to partition 1.
        SendOptions sendOptions = new SendOptions().setPartitionId("1");

        EventHubProducerClient producerClient = new EventHubClientBuilder()
            .credential("{event-hub-namespace}", "{event-hub-name}", tokenCredential)
            .buildProducerClient();

        // Serialize each playing card into its Avro equivalent.
        List<EventData> serializedCards = playingCards.stream()
            .map(card -> {
                return serializer.serialize(card, TypeReference.createInstance(EventData.class));
            })
            .collect(Collectors.toList());

        // Publish the events
        producerClient.send(serializedCards, sendOptions);

        // Dispose of the client.
        producerClient.close();
    }


    /**
     * Demonstrates how to deserialize an EventData that was serialized using {@link SchemaRegistryApacheAvroSerializer}
     * and published to Event Hubs.
     *
     * @param tokenCredential Token credential used to authenticate with Schema Registry and Event Hubs.
     * @param serializer Schema Registry serializer.
     */
    private static void consumeEventsFromEventHubs(TokenCredential tokenCredential,
        SchemaRegistryApacheAvroSerializer serializer) {

        EventHubConsumerClient consumerClient = new EventHubClientBuilder()
            .credential("{event-hub-namespace}", "{event-hub-name}", tokenCredential)
            .consumerGroup("{my-consumer-group}")
            .buildConsumerClient();

        // Receive up to 10 events within 20 seconds.  When 20 seconds elapses, the method returns the number of events
        // it received so far.
        IterableStream<PartitionEvent> events = consumerClient.receiveFromPartition("1",
            10, EventPosition.earliest(), Duration.ofSeconds(20));

        for (PartitionEvent partitionEvent : events) {
            PlayingCard playingCard = serializer.deserialize(partitionEvent.getData(),
                TypeReference.createInstance(PlayingCard.class));

            System.out.printf("Suit: %s, Value [%d]%n", playingCard.getPlayingCardSuit(), playingCard.getCardValue());
        }

        // Dispose of the client.
        consumerClient.close();
    }
}
