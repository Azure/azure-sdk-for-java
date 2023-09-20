// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.apacheavro;

import com.azure.core.credential.TokenCredential;
import com.azure.core.models.MessageContent;
import com.azure.core.util.BinaryData;
import com.azure.core.util.serializer.TypeReference;
import com.azure.data.schemaregistry.SchemaRegistryAsyncClient;
import com.azure.data.schemaregistry.SchemaRegistryClientBuilder;
import com.azure.data.schemaregistry.apacheavro.generatedtestsources.Person;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.time.OffsetDateTime;

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
        // BEGIN: com.azure.data.schemaregistry.apacheavro.schemaregistryapacheavroserializer.instantiation
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
        SchemaRegistryAsyncClient schemaRegistryAsyncClient = new SchemaRegistryClientBuilder()
            .credential(tokenCredential)
            .fullyQualifiedNamespace("{schema-registry-endpoint}")
            .buildAsyncClient();

        // By setting autoRegisterSchema to true, if the schema does not exist in the Schema Registry instance, it is
        // added to the instance. By default, this is false, so it will error if the schema is not found.
        SchemaRegistryApacheAvroSerializer serializer = new SchemaRegistryApacheAvroSerializerBuilder()
            .schemaRegistryClient(schemaRegistryAsyncClient)
            .autoRegisterSchemas(true)
            .schemaGroup("{schema-group}")
            .buildSerializer();
        // END: com.azure.data.schemaregistry.apacheavro.schemaregistryapacheavroserializer.instantiation
    }

    /**
     * Sample demonstrating how to serialize an object.
     */
    public void serialize() {
        // BEGIN: com.azure.data.schemaregistry.apacheavro.schemaregistryapacheavroserializer.serialize
        // The object to encode. The avro schema is:
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
            .setName("Alina")
            .setFavouriteColour("Turquoise")
            .build();

        MessageContent message = serializer.serialize(person,
            TypeReference.createInstance(MessageContent.class));
        // END: com.azure.data.schemaregistry.apacheavro.schemaregistryapacheavroserializer.serialize
    }

    /**
     * Serializes an object using a message factory.
     */
    public void serializeMessageFactory() {
        // BEGIN: com.azure.data.schemaregistry.apacheavro.schemaregistryapacheavroserializer.serializeMessageFactory
        // The object to encode. The avro schema is:
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
            .setName("Alina")
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
        // BEGIN: com.azure.data.schemaregistry.apacheavro.schemaregistryapacheavroserializer.deserialize
        // Message to deserialize. Assume that the body contains data which has been serialized using an Avro encoder.
        MessageContent message = new MessageContent()
            .setBodyAsBinaryData(BinaryData.fromBytes(new byte[0]))
            .setContentType("avro/binary+{schema-id}");

        // This is an object generated from the Avro schema used in the serialization sample.
        Person person = serializer.deserialize(message, TypeReference.createInstance(Person.class));
        // END: com.azure.data.schemaregistry.apacheavro.schemaregistryapacheavroserializer.deserialize
    }
}
