package com.azure.data.schemaregistry.apacheavro;

import com.azure.core.util.IterableStream;
import com.azure.core.util.serializer.TypeReference;
import com.azure.data.schemaregistry.SchemaRegistryAsyncClient;
import com.azure.data.schemaregistry.SchemaRegistryClient;
import com.azure.data.schemaregistry.SchemaRegistryClientBuilder;
import com.azure.data.schemaregistry.jsonschemavalidator.SchemaRegistryJsonSchemaSerializer;
import com.azure.data.schemaregistry.jsonschemavalidator.SchemaRegistryJsonSchemaSerializerBuilder;
import com.azure.data.schemaregistry.jsonschemavalidator.models.SerializationResult;
import com.azure.data.schemaregistry.jsonschemavalidator.models.ValidationError;
import com.azure.data.schemaregistry.models.SchemaRegistrySchema;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerClient;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationResult;

import java.io.IOException;

public class DeserializeEventData {
    public static void main(String[] args) throws IOException {
        EventHubConsumerClient consumer = new EventHubClientBuilder()
            .credential("{fully-qualified-namespace}", "{event-hub-name}",
                new DefaultAzureCredentialBuilder().build())
            .buildConsumerClient();

        DeserializeEventData sample = new DeserializeEventData();

        sample.withSerializer(consumer);
        sample.withoutSerializer(consumer);
    }

    public void withoutSerializer(EventHubConsumerClient consumer) throws IOException {
        ObjectMapper jsonSerializer = new ObjectMapper();

        // User fetches their schema.
        SchemaRegistryClient client = new SchemaRegistryClientBuilder()
            .fullyQualifiedNamespace("{fully-qualified-namespace}")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
        SchemaRegistrySchema personSchema = client.getSchema("{schema-id}");

        JsonSchemaFactory schemaFactory = JsonSchemaFactory.builder().build();
        JsonSchema schema = schemaFactory.getSchema(personSchema.getDefinition());

        IterableStream<PartitionEvent> partitionEvents = consumer.receiveFromPartition("{partition-id}", 100, EventPosition.latest());
        for (PartitionEvent event : partitionEvents) {
            JsonNode jsonNode = jsonSerializer.readTree(event.getData().getBody());
            Person person = jsonSerializer.treeToValue(jsonNode, Person.class);

            ValidationResult validationResult = schema.validateAndCollect(jsonNode);
            validationResult.getValidationMessages().forEach(validationMessage -> {
                System.out.printf("Error: %s. Path: %s. Code: %s%n", validationMessage.getMessage(),
                    validationMessage.getPath(), validationMessage.getCode());
            });
        }
    }

    public void withSerializer(EventHubConsumerClient consumer) {
        // User fetches their schema.
        SchemaRegistryAsyncClient client = new SchemaRegistryClientBuilder()
            .fullyQualifiedNamespace("{fully-qualified-namespace}")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();

        SchemaRegistryJsonSchemaSerializer serializer = new SchemaRegistryJsonSchemaSerializerBuilder()
            .schemaRegistryClient(client)
            .buildSerializer();

        // Assuming they know the schema-id. Otherwise, they'd have to fetch it via one of the schema client methods.
        // Then we'd fetch it again for them. :)
        // client.getSchema("group-name", "schema-name", 1).block();
        String schemaId = "{schema-id}";

        IterableStream<PartitionEvent> partitionEvents = consumer.receiveFromPartition("{partition-id}", 100, EventPosition.latest());
        for (PartitionEvent event : partitionEvents) {

            SerializationResult<Person> results = serializer.deserializeWithValidation(event.getData(),
                TypeReference.createInstance(Person.class));

            for (ValidationError validationError : results.getValidationErrors()) {
                System.out.println("Error: " + validationError);
            }
        }
    }
}
