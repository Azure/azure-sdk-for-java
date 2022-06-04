// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.schemaregistry.apacheavro;

import com.azure.core.credential.TokenCredential;
import com.azure.core.util.serializer.TypeReference;
import com.azure.data.schemaregistry.SchemaRegistryAsyncClient;
import com.azure.data.schemaregistry.SchemaRegistryClientBuilder;
import com.azure.data.schemaregistry.apacheavro.generatedtestsources.PlayingCard;
import com.azure.data.schemaregistry.apacheavro.generatedtestsources.PlayingCardSuit;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerClient;

import java.util.Collections;

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

        EventHubProducerClient producerClient = new EventHubClientBuilder()
            .credential("{event-hub-namespace}", "{event-hub-name}", tokenCredential)
            .buildProducerClient();

        PlayingCard playingCard = new PlayingCard();
        playingCard.setCardValue(5);
        playingCard.setIsFaceCard(false);
        playingCard.setPlayingCardSuit(PlayingCardSuit.SPADES);

        EventData eventData = serializer.serialize(playingCard, TypeReference.createInstance(EventData.class));
        producerClient.send(Collections.singleton(eventData));

        // Dispose of the client.
        producerClient.close();
    }
}
