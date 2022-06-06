// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.apacheavro;

import com.azure.core.credential.TokenCredential;
import com.azure.core.models.MessageContent;
import com.azure.core.util.BinaryData;
import com.azure.core.util.serializer.TypeReference;
import com.azure.data.schemaregistry.SchemaRegistryAsyncClient;
import com.azure.data.schemaregistry.SchemaRegistryClientBuilder;
import com.azure.data.schemaregistry.apacheavro.generatedtestsources.PlayingCard;
import com.azure.data.schemaregistry.apacheavro.generatedtestsources.PlayingCardSuit;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.io.IOException;
import java.io.UncheckedIOException;

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

        // Create the encoder instance by configuring it with the schema registry client and
        // enabling auto registering of new schemas
        SchemaRegistryApacheAvroSerializer encoder = new SchemaRegistryApacheAvroSerializerBuilder()
            .schemaRegistryClient(schemaRegistryAsyncClient)
            .schemaGroup("{schema-group}")
            .avroSpecificReader(true)
            .autoRegisterSchemas(true)
            .buildSerializer();

        // Get serialized avro data to deserialize into strongly-typed object.
        MessageContent inputStream = getMessageToDeserialize();
        PlayingCard deserializedObject = encoder.deserialize(inputStream,
            TypeReference.createInstance(PlayingCard.class));
    }

    private static MessageContent getMessageToDeserialize() {
        PlayingCard playingCard = new PlayingCard();
        playingCard.setCardValue(5);
        playingCard.setIsFaceCard(false);
        playingCard.setPlayingCardSuit(PlayingCardSuit.SPADES);

        try {
            return new MessageContent()
                .setBodyAsBinaryData(BinaryData.fromBytes(playingCard.toByteBuffer().array()))
                .setContentType("avro/binary+schema_id");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
