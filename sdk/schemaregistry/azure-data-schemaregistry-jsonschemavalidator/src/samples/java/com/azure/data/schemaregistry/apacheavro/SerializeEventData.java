package com.azure.data.schemaregistry.apacheavro;

import com.azure.core.util.serializer.TypeReference;
import com.azure.data.schemaregistry.SchemaRegistryAsyncClient;
import com.azure.data.schemaregistry.SchemaRegistryClient;
import com.azure.data.schemaregistry.SchemaRegistryClientBuilder;
import com.azure.data.schemaregistry.jsonschemavalidator.SchemaRegistryJsonSchemaSerializer;
import com.azure.data.schemaregistry.jsonschemavalidator.SchemaRegistryJsonSchemaSerializerBuilder;
import com.azure.data.schemaregistry.jsonschemavalidator.models.SerializationResult;
import com.azure.data.schemaregistry.models.SchemaRegistrySchema;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationResult;

import java.io.IOException;
import java.util.Collections;

public class SerializeEventData {
    public static void main(String[] args) throws IOException {
        EventHubProducerClient producer = new EventHubClientBuilder()
            .credential("{fully-qualified-namespace}", "{event-hub-name}",
                new DefaultAzureCredentialBuilder().build())
            .buildProducerClient();

        SerializeEventData sample = new SerializeEventData();

        sample.withSerializer(producer);
        sample.withoutSerializer(producer);
    }

    public void withoutSerializer(EventHubProducerClient producer) throws IOException {
        ObjectMapper jsonSerializer = new ObjectMapper();

        Person person = new Person();
        person.setAge(25);
        person.setName("Tom Foolery");

        // User fetches their schema.
        SchemaRegistryClient client = new SchemaRegistryClientBuilder()
            .fullyQualifiedNamespace("{fully-qualified-namespace}")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
        SchemaRegistrySchema personSchema = client.getSchema("{schema-id}");

        JsonSchemaFactory schemaFactory = JsonSchemaFactory.builder().build();
        JsonSchema schema = schemaFactory.getSchema(personSchema.getDefinition());

        // Serialize it first to get the JsonNode.
        byte[] bytes = jsonSerializer.writeValueAsBytes(person);
        JsonNode jsonNode = jsonSerializer.readTree(bytes);

        // Get validation results for this object.
        ValidationResult validationResult = schema.validateAndCollect(jsonNode);
        validationResult.getValidationMessages().forEach(validationMessage -> {
            System.out.printf("Error: %s. Path: %s. Code: %s%n", validationMessage.getMessage(),
                validationMessage.getPath(), validationMessage.getCode());
        });

        EventData serializedEvent = new EventData(bytes);
        producer.send(Collections.singleton(serializedEvent));
    }

    public void withSerializer(EventHubProducerClient producer) {
        Person person = new Person();
        person.setAge(25);
        person.setName("Tom Foolery");

        // User fetches their schema.
        SchemaRegistryAsyncClient client = new SchemaRegistryClientBuilder()
            .fullyQualifiedNamespace("{fully-qualified-namespace}")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();

        SchemaRegistryJsonSchemaSerializer serializer = new SchemaRegistryJsonSchemaSerializerBuilder()
            .schemaRegistryClient(client)
            .buildSerializer();

        // Assuming they know the schema-id. Otherwise, they'd have to fetch it via one of the schema client methods.
        // client.getSchema("group-name", "schema-name", 1).block();
        String schemaId = "{schema-id}";
        SerializationResult<EventData> serializationResult = serializer.serializeWithValidation(person,
            TypeReference.createInstance(EventData.class), schemaId);

        serializationResult.getValidationErrors().forEach(validationMessage -> {
            System.out.println("Error: " + validationMessage);
        });

        producer.send(Collections.singleton(serializationResult.getValue()));
    }
}
