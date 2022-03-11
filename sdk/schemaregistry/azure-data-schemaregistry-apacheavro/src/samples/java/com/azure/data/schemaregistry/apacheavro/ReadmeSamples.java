// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.apacheavro;

import com.azure.core.credential.TokenCredential;
import com.azure.core.experimental.models.MessageWithMetadata;
import com.azure.core.util.BinaryData;
import com.azure.core.util.serializer.TypeReference;
import com.azure.data.schemaregistry.SchemaRegistryAsyncClient;
import com.azure.data.schemaregistry.SchemaRegistryClientBuilder;
import com.azure.data.schemaregistry.apacheavro.generatedtestsources.PlayingCard;
import com.azure.data.schemaregistry.apacheavro.generatedtestsources.PlayingCardSuit;
import com.azure.identity.DefaultAzureCredentialBuilder;

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

        // {schema-registry-endpoint} is the fully qualified namespace of the Event Hubs instance. It is usually
        // of the form "{your-namespace}.servicebus.windows.net"
        SchemaRegistryAsyncClient schemaRegistryAsyncClient = new SchemaRegistryClientBuilder()
            .fullyQualifiedNamespace("{your-event-hubs-namespace}.servicebus.windows.net")
            .credential(tokenCredential)
            .buildAsyncClient();
        // END: readme-sample-createSchemaRegistryAsyncClient

        // BEGIN: readme-sample-createSchemaRegistryAvroSerializer
        SchemaRegistryApacheAvroSerializer serializer = new SchemaRegistryApacheAvroSerializerBuilder()
            .schemaRegistryAsyncClient(schemaRegistryAsyncClient)
            .schemaGroup("{schema-group}")
            .buildSerializer();
        // END: readme-sample-createSchemaRegistryAvroSerializer

        return serializer;
    }

    /**
     * Encode a strongly-typed object into avro payload compatible with schema registry.
     */
    public void serializeSample() {
        SchemaRegistryApacheAvroSerializer serializer = createAvroSchemaRegistrySerializer();

        // BEGIN: readme-sample-serializeSample
        PlayingCard playingCard = new PlayingCard();
        playingCard.setPlayingCardSuit(PlayingCardSuit.SPADES);
        playingCard.setIsFaceCard(false);
        playingCard.setCardValue(5);

        MessageWithMetadata message = serializer.serializeMessageData(playingCard,
            TypeReference.createInstance(MessageWithMetadata.class));
        // END: readme-sample-serializeSample
    }

    /**
     * Decode avro payload compatible with schema registry into a strongly-type object.
     */
    public void deserializeSample() {
        // BEGIN: readme-sample-deserializeSample
        SchemaRegistryApacheAvroSerializer serializer = createAvroSchemaRegistrySerializer();
        MessageWithMetadata message = getSchemaRegistryAvroMessage();
        PlayingCard playingCard = serializer.deserializeMessageData(message, TypeReference.createInstance(PlayingCard.class));
        // END: readme-sample-deserializeSample
    }

    /**
     * Non-functional method not visible on README sample
     * @return a new message.
     */
    private MessageWithMetadata getSchemaRegistryAvroMessage() {
        return new MessageWithMetadata()
            .setBodyAsBinaryData(BinaryData.fromBytes(new byte[1]))
            .setContentType("avro/binary+schema_id");
    }
}
