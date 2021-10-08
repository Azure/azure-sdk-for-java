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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 *
 * Code samples for the README.md
 */
public class ReadmeSamples {

    /**
     * Sample to demonstrate creation of {@link SchemaRegistryApacheAvroSerializer}.
     * @return The {@link SchemaRegistryApacheAvroSerializer}.
     */
    public SchemaRegistryApacheAvroSerializer createAvroSchemaRegistrySerializer() {
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        SchemaRegistryAsyncClient schemaRegistryAsyncClient = new SchemaRegistryClientBuilder()
            .fullyQualifiedNamespace("{schema-registry-endpoint")
            .credential(tokenCredential)
            .buildAsyncClient();

        SchemaRegistryApacheAvroSerializer schemaRegistryAvroSerializer = new SchemaRegistryApacheAvroSerializerBuilder()
            .schemaRegistryAsyncClient(schemaRegistryAsyncClient)
            .schemaGroup("{schema-group}")
            .buildSerializer();

        return schemaRegistryAvroSerializer;
    }

    /**
     * Serialize a strongly-typed object into avro payload compatible with schema registry.
     */
    public void serializeSample() {
        SchemaRegistryApacheAvroSerializer schemaRegistryAvroSerializer = createAvroSchemaRegistrySerializer();

        PlayingCard playingCard = new PlayingCard();
        playingCard.setPlayingCardSuit(PlayingCardSuit.SPADES);
        playingCard.setIsFaceCard(false);
        playingCard.setCardValue(5);

        // write serialized data to byte array outputstream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        schemaRegistryAvroSerializer.serialize(outputStream, playingCard);
    }

    /**
     * Deserialize avro payload compatible with schema registry into a strongly-type object.
     */
    public void deserializeSample() {
        SchemaRegistryApacheAvroSerializer schemaRegistryAvroSerializer = createAvroSchemaRegistrySerializer();
        InputStream inputStream = getSchemaRegistryAvroData();
        PlayingCard playingCard = schemaRegistryAvroSerializer.deserialize(inputStream,
            TypeReference.createInstance(PlayingCard.class));
    }

    /**
     * Non-functional method not visible on README sample
     * @return a new ByteArrayInputStream
     */
    private InputStream getSchemaRegistryAvroData() {
        return new ByteArrayInputStream(new byte[1]);
    }

}
