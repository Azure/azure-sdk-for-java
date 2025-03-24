// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.jsonschema;

import com.azure.core.models.MessageContent;
import com.azure.core.util.BinaryData;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.TypeReference;
import com.azure.data.schemaregistry.SchemaRegistryAsyncClient;
import com.azure.data.schemaregistry.SchemaRegistryClient;
import com.azure.data.schemaregistry.models.SchemaFormat;
import com.azure.data.schemaregistry.models.SchemaProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link SchemaRegistryJsonSchemaSerializerBuilder}
 */
public class SchemaRegistryJsonSchemaSerializerBuilderTests {
    private final SchemaRegistryJsonSchemaSerializerBuilder builder = new SchemaRegistryJsonSchemaSerializerBuilder();

    @Mock
    private JsonSerializer jsonSerializer;
    @Mock
    private SchemaRegistryAsyncClient schemaRegistryAsyncClient;
    @Mock
    private JsonSchemaGenerator jsonSchemaGenerator;
    @Mock
    private SchemaProperties schemaProperties;

    private AutoCloseable mocksCloseable;

    @BeforeEach
    public void beforeEach() {
        mocksCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void afterEach() throws Exception {
        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
    }

    /**
     * Throws if both schema registry clients are null.
     */
    @Test
    public void nullSchemaRegistryClients() {
        // Arrange
        builder.jsonSchemaGenerator(jsonSchemaGenerator)
            .serializer(jsonSerializer);

        // Act & Assert
        assertThrows(NullPointerException.class, () -> builder.buildSerializer());
    }

    @Test
    public void doesNotThrowWhenOneClientIsSet() {
        final SchemaRegistryJsonSchemaSerializerBuilder asyncBuilder = new SchemaRegistryJsonSchemaSerializerBuilder()
            .schemaRegistryClient(schemaRegistryAsyncClient)
            .schemaRegistryClient((SchemaRegistryClient) null)
            .jsonSchemaGenerator(jsonSchemaGenerator);

        assertDoesNotThrow(() -> asyncBuilder.buildSerializer());

        final SchemaRegistryClient schemaRegistryClient = mock(SchemaRegistryClient.class);
        final SchemaRegistryJsonSchemaSerializerBuilder syncBuilder = new SchemaRegistryJsonSchemaSerializerBuilder()
            .schemaRegistryClient((SchemaRegistryAsyncClient) null)
            .schemaRegistryClient(schemaRegistryClient)
            .jsonSchemaGenerator(jsonSchemaGenerator);

        assertDoesNotThrow(() -> syncBuilder.buildSerializer());
    }

    @Test
    public void nullJsonSchemaGenerator() {
        // Arrange
        builder.schemaRegistryClient(schemaRegistryAsyncClient)
            .serializer(jsonSerializer);

        // Act & Assert
        assertThrows(NullPointerException.class, () -> builder.buildSerializer());
    }

    @Test
    public void autoRegisterNoSchemaGroup() {
        // Arrange
        builder.autoRegisterSchemas(true)
            .schemaRegistryClient(schemaRegistryAsyncClient)
            .jsonSchemaGenerator(jsonSchemaGenerator)
            .serializer(jsonSerializer);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> builder.buildSerializer());
    }

    @Test
    public void usesSerializer() {
        // Arrange
        final String schemaGroup = "my-schema-group";
        final Address address = new Address();
        final byte[] addressAsBytes = "test-address".getBytes(StandardCharsets.UTF_8);
        final TypeReference<Address> typeReference = TypeReference.createInstance(Address.class);
        final String jsonSchema = "test-json-schema";
        final String schemaId = "test-schema-id";

        when(schemaProperties.getId()).thenReturn(schemaId);

        when(jsonSchemaGenerator.generateSchema(typeReference)).thenReturn(jsonSchema);
        when(schemaRegistryAsyncClient.getSchemaProperties(eq(schemaGroup), eq(Address.class.getName()),
            eq(jsonSchema), eq(SchemaFormat.JSON)))
            .thenReturn(Mono.just(schemaProperties));

        when(jsonSerializer.serializeToBytes(address)).thenReturn(addressAsBytes);
        when(jsonSerializer.serializeToBytesAsync(address)).thenReturn(Mono.just(addressAsBytes));

        builder.schemaRegistryClient(schemaRegistryAsyncClient)
            .serializer(jsonSerializer)
            .jsonSchemaGenerator(jsonSchemaGenerator)
            .autoRegisterSchemas(false)
            .schemaGroup(schemaGroup);

        SchemaRegistryJsonSchemaSerializer serializer = builder.buildSerializer();

        // Act & Assert
        StepVerifier.create(serializer.serializeAsync(address, TypeReference.createInstance(MessageContent.class)))
            .assertNext(messageContent -> {
                BinaryData actual = messageContent.getBodyAsBinaryData();

                assertEquals(addressAsBytes, actual.toBytes());
                assertTrue(messageContent.getContentType().endsWith(schemaId),
                    String.format("Message does not content matching schema id. Expected: %s. Actual: %s", schemaId,
                        messageContent.getContentType()));
            })
            .expectComplete()
            .verify();
    }
}
