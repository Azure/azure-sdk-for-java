// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.apacheavro;

import com.azure.core.credential.TokenCredential;
import com.azure.core.models.MessageContent;
import com.azure.core.util.serializer.TypeReference;
import com.azure.data.schemaregistry.SchemaRegistryAsyncClient;
import com.azure.data.schemaregistry.SchemaRegistryClientBuilder;
import com.azure.data.schemaregistry.apacheavro.generatedtestsources.PlayingCard;
import com.azure.data.schemaregistry.apacheavro.generatedtestsources.PlayingCardSuit;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.eventhubs.EventData;

/**
 * Sample application to demonstrate deserializing data into a strongly-typed object using Schema Registry-based Avro
 * Serializer.
 */
public class SchemaRegistryApacheAvroDeserializationSample {
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

        // Create the serializer instance by configuring it with the schema registry client
        SchemaRegistryApacheAvroSerializer serializer = new SchemaRegistryApacheAvroSerializerBuilder()
            .schemaRegistryClient(schemaRegistryAsyncClient)
            .schemaGroup("{schema-group}")
            .avroSpecificReader(true)
            .buildSerializer();

        // Get an EventData to deserialize.  EventData extends from MessageContent, so the serializer knows where
        // to set the body and content-type which points to the schema in Schema Registry.
        EventData eventData = getEventDataToDeserialize(serializer);

        PlayingCard deserializedObject = serializer.deserialize(eventData,
            TypeReference.createInstance(PlayingCard.class));

        // If customers are not using Event Hubs, they can also serialize their data using a class that extends from
        // MessageContent.
        MessageContent message = getMessageToDeserialize(serializer);

        PlayingCard deserializedMessage = serializer.deserialize(message,
            TypeReference.createInstance(PlayingCard.class));
    }

    private static MessageContent getMessageToDeserialize(SchemaRegistryApacheAvroSerializer serializer) {
        PlayingCard playingCard = new PlayingCard();
        playingCard.setCardValue(5);
        playingCard.setIsFaceCard(false);
        playingCard.setPlayingCardSuit(PlayingCardSuit.SPADES);

        return serializer.serialize(playingCard, TypeReference.createInstance(MessageContent.class));
    }

    private static EventData getEventDataToDeserialize(SchemaRegistryApacheAvroSerializer serializer) {
        PlayingCard playingCard = new PlayingCard();
        playingCard.setCardValue(5);
        playingCard.setIsFaceCard(false);
        playingCard.setPlayingCardSuit(PlayingCardSuit.SPADES);

        return serializer.serialize(playingCard, TypeReference.createInstance(EventData.class));
    }
}
