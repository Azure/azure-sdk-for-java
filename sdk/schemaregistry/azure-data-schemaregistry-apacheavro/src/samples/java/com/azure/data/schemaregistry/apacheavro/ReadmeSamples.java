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
 * Code samples for the README.md
 */
public class ReadmeSamples {

    /**
     * Sample to demonstrate creation of {@link SchemaRegistryApacheAvroSerializer}.
     * @return The {@link SchemaRegistryApacheAvroSerializer}.
     */
    public SchemaRegistryApacheAvroSerializer createAvroSchemaRegistrySerializer() {
        // BEGIN: readme-sample-createSchemaRegistryAsyncClient
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        SchemaRegistryAsyncClient schemaRegistryAsyncClient = new SchemaRegistryClientBuilder()
            .fullyQualifiedNamespace("{schema-registry-endpoint")
            .credential(tokenCredential)
            .buildAsyncClient();
        // END: readme-sample-createSchemaRegistryAsyncClient

        // BEGIN: readme-sample-createSchemaRegistryAvroSerializer
        SchemaRegistryApacheAvroSerializer schemaRegistryAvroSerializer = new SchemaRegistryApacheAvroSerializerBuilder()
            .schemaRegistryAsyncClient(schemaRegistryAsyncClient)
            .schemaGroup("{schema-group}")
            .buildSerializer();
        // END: readme-sample-createSchemaRegistryAvroSerializer

        return schemaRegistryAvroSerializer;
    }

    /**
     * Serialize a strongly-typed object into avro payload compatible with schema registry.
     */
    public void serializeSample() {
        SchemaRegistryApacheAvroSerializer schemaRegistryAvroSerializer = createAvroSchemaRegistrySerializer();

        // BEGIN: readme-sample-serializeSample
        PlayingCard playingCard = new PlayingCard();
        playingCard.setPlayingCardSuit(PlayingCardSuit.SPADES);
        playingCard.setIsFaceCard(false);
        playingCard.setCardValue(5);

        // write serialized data to ByteArrayOutputStream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        schemaRegistryAvroSerializer.serialize(outputStream, playingCard);
        // END: readme-sample-serializeSample
    }

    /**
     * Deserialize avro payload compatible with schema registry into a strongly-type object.
     */
    public void deserializeSample() {
        // BEGIN: readme-sample-deserializeSample
        SchemaRegistryApacheAvroSerializer schemaRegistryAvroSerializer = createAvroSchemaRegistrySerializer();
        InputStream inputStream = getSchemaRegistryAvroData();
        PlayingCard playingCard = schemaRegistryAvroSerializer.deserialize(inputStream,
            TypeReference.createInstance(PlayingCard.class));
        // END: readme-sample-deserializeSample
    }

    /**
     * Non-functional method not visible on README sample
     * @return a new ByteArrayInputStream
     */
    private InputStream getSchemaRegistryAvroData() {
        return new ByteArrayInputStream(new byte[1]);
    }

}
