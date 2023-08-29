// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.jsonschema;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.ContentType;
import com.azure.core.models.MessageContent;
import com.azure.core.util.BinaryData;
import com.azure.core.util.serializer.TypeReference;
import com.azure.data.schemaregistry.SchemaRegistryAsyncClient;
import com.azure.data.schemaregistry.SchemaRegistryClientBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.eventhubs.EventData;

public class ReadmeSamples {

    /**
     * Sample to demonstrate creation of {@link SchemaRegistryJsonSchemaSerializer}.
     *
     * @return The {@link SchemaRegistryJsonSchemaSerializer}.
     */
    public SchemaRegistryJsonSchemaSerializer createSerializer() {
        // BEGIN: readme-sample-createSchemaRegistryAsyncClient
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
        JsonSchemaGenerator jsonSchemaGenerator = null;

        // {schema-registry-endpoint} is the fully qualified namespace of the Event Hubs instance. It is usually
        // of the form "{your-namespace}.servicebus.windows.net"
        SchemaRegistryAsyncClient schemaRegistryAsyncClient = new SchemaRegistryClientBuilder()
            .fullyQualifiedNamespace("{your-event-hubs-namespace}.servicebus.windows.net")
            .credential(tokenCredential)
            .buildAsyncClient();
        // END: readme-sample-createSchemaRegistryAsyncClient

        // BEGIN: readme-sample-createSchemaRegistryJsonSchemaSerializer
        SchemaRegistryJsonSchemaSerializer serializer = new SchemaRegistryJsonSchemaSerializerBuilder()
            .schemaRegistryClient(schemaRegistryAsyncClient)
            .schemaGroup("{schema-group}")
            .jsonSchemaGenerator(jsonSchemaGenerator)
            .buildSerializer();
        // END: readme-sample-createSchemaRegistryJsonSchemaSerializer

        return serializer;
    }

    /**
     * Encode a strongly-typed object into payload compatible with schema registry.
     */
    public void serializeSample() {
        SchemaRegistryJsonSchemaSerializer serializer = createSerializer();

        // BEGIN: readme-sample-serializeSample
        Address address = new Address();
        address.setNumber(105);
        address.setStreetName("1st");
        address.setStreetType("Street");

        EventData eventData = serializer.serialize(address, TypeReference.createInstance(EventData.class));
        // END: readme-sample-serializeSample
    }

    /**
     * Decode payload compatible with schema registry into a strongly-type object.
     */
    public void deserializeSample() {
        // BEGIN: readme-sample-deserializeSample
        SchemaRegistryJsonSchemaSerializer serializer = createSerializer();
        MessageContent message = getSchemaRegistryJSONMessage();
        Address address = serializer.deserialize(message, TypeReference.createInstance(Address.class));
        // END: readme-sample-deserializeSample
    }

    /**
     * Non-functional method not visible on README sample
     *
     * @return a new message.
     */
    private MessageContent getSchemaRegistryJSONMessage() {
        return new MessageContent()
            .setBodyAsBinaryData(BinaryData.fromBytes(new byte[1]))
            .setContentType(ContentType.APPLICATION_JSON + "+<schema-id>");
    }
}
