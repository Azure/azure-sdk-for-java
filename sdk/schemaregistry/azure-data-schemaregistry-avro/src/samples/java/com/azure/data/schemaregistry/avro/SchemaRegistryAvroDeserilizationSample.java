// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.avro;

import com.azure.core.credential.TokenCredential;
import com.azure.core.util.serializer.TypeReference;
import com.azure.data.schemaregistry.SchemaRegistryAsyncClient;
import com.azure.data.schemaregistry.SchemaRegistryClientBuilder;
import com.azure.data.schemaregistry.avro.generatedtestsources.PlayingCard;
import com.azure.data.schemaregistry.avro.generatedtestsources.PlayingCardSuit;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Sample application to demonstrate deserializing data into a strongly-typed object using Schema Registry-based Avro
 * Serializer.
 */
public class SchemaRegistryAvroDeserilizationSample {
    /**
     * Main method to run this sample.
     *
     * @param args Ignore arguments.
     */
    public static void main(String[] args) throws IOException {
        // Create AAD token credential
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
        // Create the schema registry async client
        SchemaRegistryAsyncClient schemaRegistryAsyncClient = new SchemaRegistryClientBuilder()
            .credential(tokenCredential)
            .fullyQualifiedNamespace("{schema-registry-endpoint}")
            .buildAsyncClient();

        // Create the serializer instance by configuring the serializer with the schema registry client and
        // enabling auto registering of new schemas
        SchemaRegistryAvroSerializer schemaRegistryAvroSerializer = new SchemaRegistryAvroSerializerBuilder()
            .schemaRegistryAsyncClient(schemaRegistryAsyncClient)
            .schemaGroup("{schema-group}")
            .avroSpecificReader(true)
            .autoRegisterSchema(true)
            .buildSerializer();

        // Get serialized avro data to deserialize into strongly-typed object.
        InputStream inputStream = getDataToDeserialize();
        PlayingCard deserializedObject = schemaRegistryAvroSerializer.deserialize(inputStream,
            TypeReference.createInstance(PlayingCard.class));
    }

    private static InputStream getDataToDeserialize() throws IOException {
        PlayingCard playingCard = new PlayingCard();
        playingCard.setCardValue(5);
        playingCard.setIsFaceCard(false);
        playingCard.setPlayingCardSuit(PlayingCardSuit.SPADES);
        // get byte array of PlayingCard
        InputStream inputStream = new ByteArrayInputStream(playingCard.toByteBuffer().array());
        return inputStream;
    }
}
