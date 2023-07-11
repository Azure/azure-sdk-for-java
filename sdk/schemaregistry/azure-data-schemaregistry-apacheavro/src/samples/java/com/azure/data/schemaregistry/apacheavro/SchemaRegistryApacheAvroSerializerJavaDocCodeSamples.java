// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.apacheavro;

import com.azure.core.credential.TokenCredential;
import com.azure.core.models.MessageContent;
import com.azure.core.util.serializer.TypeReference;
import com.azure.data.schemaregistry.SchemaRegistryAsyncClient;
import com.azure.data.schemaregistry.SchemaRegistryClientBuilder;
import com.azure.data.schemaregistry.apacheavro.generatedtestsources.Person;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerClient;
import com.azure.messaging.eventhubs.models.EventPosition;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Java doc samples for {@link SchemaRegistryApacheAvroSerializer}.
 */
public class SchemaRegistryApacheAvroSerializerJavaDocCodeSamples {
    private final SchemaRegistryAsyncClient schemaRegistryAsyncClient = new SchemaRegistryClientBuilder()
        .credential(new DefaultAzureCredentialBuilder().build())
        .fullyQualifiedNamespace("{schema-registry-endpoint}")
        .buildAsyncClient();
    private final SchemaRegistryApacheAvroSerializer serializer = new SchemaRegistryApacheAvroSerializerBuilder()
        .schemaRegistryClient(schemaRegistryAsyncClient)
        .schemaGroup("{schema-group}")
        .buildSerializer();

    /**
     * Demonstrates how to instantiate the serializer.
     */
    public void instantiate() {
        // BEGIN: com.azure.data.schemaregistry.apacheavro.schemaregistryapacheavroserializer.construct
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
        SchemaRegistryAsyncClient schemaRegistryAsyncClient = new SchemaRegistryClientBuilder()
            .credential(tokenCredential)
            .fullyQualifiedNamespace("{schema-registry-endpoint}")
            .buildAsyncClient();

        SchemaRegistryApacheAvroSerializer serializer = new SchemaRegistryApacheAvroSerializerBuilder()
            .schemaRegistryClient(schemaRegistryAsyncClient)
            .schemaGroup("{schema-group}")
            .buildSerializer();
        // END: com.azure.data.schemaregistry.apacheavro.schemaregistryapacheavroserializer.construct
    }

    /**
     * Sample demonstrating how to serialize an object.
     */
    public void serialize() {
        // BEGIN: com.azure.data.schemaregistry.apacheavro.schemaregistryapacheavroserializer.serialize
        // The object to encode. The Avro schema is:
        // {
        //     "namespace": "com.azure.data.schemaregistry.apacheavro.generatedtestsources",
        //     "type": "record",
        //     "name": "Person",
        //     "fields": [
        //         {"name":"name", "type": "string"},
        //         {"name":"favourite_number", "type": ["int", "null"]},
        //         {"name":"favourite_colour", "type": ["string", "null"]}
        //   ]
        // }
        Person person = Person.newBuilder()
            .setName("Chase")
            .setFavouriteColour("Turquoise")
            .build();

        MessageContent message = serializer.serialize(person,
            TypeReference.createInstance(MessageContent.class));
        // END: com.azure.data.schemaregistry.apacheavro.schemaregistryapacheavroserializer.serialize
    }

    /**
     * Sample demonstrating how to serialize an object into an EventData.
     */
    public void serializeEventData() {
        // BEGIN: com.azure.data.schemaregistry.apacheavro.schemaregistryapacheavroserializer.serialize-eventdata
        // The object to encode. The Avro schema is:
        // {
        //     "namespace": "com.azure.data.schemaregistry.apacheavro.generatedtestsources",
        //     "type": "record",
        //     "name": "Person",
        //     "fields": [
        //         {"name":"name", "type": "string"},
        //         {"name":"favourite_number", "type": ["int", "null"]},
        //         {"name":"favourite_colour", "type": ["string", "null"]}
        //   ]
        // }
        Person person = Person.newBuilder()
            .setName("Chase")
            .setFavouriteColour("Turquoise")
            .build();

        EventData eventData = serializer.serialize(person, TypeReference.createInstance(EventData.class));
        // END: com.azure.data.schemaregistry.apacheavro.schemaregistryapacheavroserializer.serialize-eventdata
    }

    /**
     * Serializes an object using a message factory.
     */
    public void serializeMessageFactory() {
        // BEGIN: com.azure.data.schemaregistry.apacheavro.schemaregistryapacheavroserializer.serializeMessageFactory
        // The object to encode. The Avro schema is:
        // {
        //     "namespace": "com.azure.data.schemaregistry.apacheavro.generatedtestsources",
        //     "type": "record",
        //     "name": "Person",
        //     "fields": [
        //         {"name":"name", "type": "string"},
        //         {"name":"favourite_number", "type": ["int", "null"]},
        //         {"name":"favourite_colour", "type": ["string", "null"]}
        //   ]
        // }
        Person person = Person.newBuilder()
            .setName("Chase")
            .setFavouriteColour("Turquoise")
            .build();

        // Serializes and creates an instance of ComplexMessage using the messageFactory function.
        ComplexMessage message = serializer.serialize(person,
            TypeReference.createInstance(ComplexMessage.class),
            (encodedData) -> {
                return new ComplexMessage("unique-id", OffsetDateTime.now());
            });
        // END: com.azure.data.schemaregistry.apacheavro.schemaregistryapacheavroserializer.serializeMessageFactory
    }

    /**
     * Sample demonstrating how to deserialize an object.
     */
    public void deserialize() {
        EventHubConsumerClient consumer = new EventHubClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .consumerGroup("{consumer-group}")
            .buildConsumerClient();

        // BEGIN: com.azure.data.schemaregistry.apacheavro.schemaregistryapacheavroserializer.deserialize
        List<EventData> eventsList =
            consumer.receiveFromPartition("0", 10, EventPosition.latest(), Duration.ofSeconds(30))
                .stream()
                .map(partitionEvent -> partitionEvent.getData())
                .collect(Collectors.toList());

        for (EventData eventData : eventsList) {
            Person person = serializer.deserialize(eventData, TypeReference.createInstance(Person.class));

            System.out.printf("Name: %s, Number: %s%n", person.getName(), person.getFavouriteNumber());
        }
        // END: com.azure.data.schemaregistry.apacheavro.schemaregistryapacheavroserializer.deserialize

        // Dispose of client after.
        consumer.close();
    }

    /**
     * Snippet for deserializing Avro object.
     */
    public void deserializeEventData() {
        Person person = Person.newBuilder()
            .setName("Foo Bar")
            .setFavouriteNumber(3)
            .build();

        // BEGIN: com.azure.data.schemaregistry.apacheavro.schemaregistryapacheavroserializer.deserialize-eventdata
        // EventData created from the Avro generated object, person.
        EventData eventData = serializer.serialize(person, TypeReference.createInstance(EventData.class));

        Person deserialized = serializer.deserialize(eventData, TypeReference.createInstance(Person.class));

        System.out.printf("Name: %s, Number: %s%n", deserialized.getName(), deserialized.getFavouriteNumber());
        // END: com.azure.data.schemaregistry.apacheavro.schemaregistryapacheavroserializer.deserialize-eventdata
    }
}
