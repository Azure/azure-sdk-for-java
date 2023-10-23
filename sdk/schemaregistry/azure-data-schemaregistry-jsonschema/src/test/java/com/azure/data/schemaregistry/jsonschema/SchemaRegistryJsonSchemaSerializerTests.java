// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.jsonschema;

import com.azure.core.http.ContentType;
import com.azure.core.models.MessageContent;
import com.azure.core.util.BinaryData;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.JsonSerializerProviders;
import com.azure.core.util.serializer.TypeReference;
import com.azure.data.schemaregistry.SchemaRegistryAsyncClient;
import com.azure.data.schemaregistry.models.SchemaFormat;
import com.azure.data.schemaregistry.models.SchemaProperties;
import com.azure.data.schemaregistry.models.SchemaRegistrySchema;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests {@link SchemaRegistryJsonSchemaSerializer}.
 */
public class SchemaRegistryJsonSchemaSerializerTests {
    private static final String ADDRESS_SCHEMA_ID = "address-schema-id";
    private static final String SCHEMA_GROUP = "test-group";
    private static final String CONTENT_TYPE_WITH_ID = ContentType.APPLICATION_JSON + "+" + ADDRESS_SCHEMA_ID;
    private static final JsonSerializer JSON_SERIALIZER = JsonSerializerProviders.createInstance(true);

    @Mock
    private SchemaRegistryAsyncClient registryAsyncClient;
    @Mock
    private JsonSchemaGenerator jsonSchemaGenerator;
    @Mock
    private SchemaProperties schemaProperties;

    private AutoCloseable mocksCloseable;

    private final Address address = new Address();

    @BeforeEach
    public void beforeEach() {
        this.mocksCloseable = MockitoAnnotations.openMocks(this);

        address.setStreetName("Rodeo");
        address.setNumber(20);
        address.setStreetType("Street");

        when(schemaProperties.getId()).thenReturn(ADDRESS_SCHEMA_ID);
        when(schemaProperties.getFormat()).thenReturn(SchemaFormat.JSON);
        when(schemaProperties.getGroupName()).thenReturn(SCHEMA_GROUP);
        when(schemaProperties.getVersion()).thenReturn(1);
    }

    @AfterEach
    public void afterEach() throws Exception {
        if (mocksCloseable != null) {
            mocksCloseable.close();
        }

        Mockito.framework().clearInlineMock(this);
    }

    /**
     * Tests that constructor throws exceptions we expect.
     */
    @Test
    public void nullConstructorArgs() {
        // Arrange
        SerializerOptions options = new SerializerOptions("foo", false, 10, JSON_SERIALIZER);

        // Act & Assert
        assertThrows(NullPointerException.class,
            () -> new SchemaRegistryJsonSchemaSerializer(null, jsonSchemaGenerator, options));
        assertThrows(NullPointerException.class,
            () -> new SchemaRegistryJsonSchemaSerializer(registryAsyncClient, null, options));
        assertThrows(NullPointerException.class,
            () -> new SchemaRegistryJsonSchemaSerializer(registryAsyncClient, jsonSchemaGenerator, null));
    }

    /**
     * Serializes an object, and registers the schema when auto-register is set to true.
     */
    @Test
    public void serializeNoArgConstructor() throws IOException {
        // Arrange
        SerializerOptions serializerOptions = new SerializerOptions(SCHEMA_GROUP, true,
            100, JSON_SERIALIZER);

        when(registryAsyncClient.registerSchema(eq(SCHEMA_GROUP), eq(Address.class.getName()), any(),
            eq(SchemaFormat.JSON))).thenAnswer(invocation -> Mono.just(schemaProperties));

        SchemaRegistryJsonSchemaSerializer serializer = new SchemaRegistryJsonSchemaSerializer(registryAsyncClient,
            jsonSchemaGenerator, serializerOptions);

        when(jsonSchemaGenerator.generateSchema(eq(TypeReference.createInstance(Address.class))))
            .thenReturn(Address.JSON_SCHEMA);

        final byte[] expectedContents = JSON_SERIALIZER.serializeToBytes(address);

        // Act
        NoArgMessage actual = serializer.serialize(address, TypeReference.createInstance(NoArgMessage.class));

        // Assert
        assertArrayEquals(expectedContents, actual.getBodyAsBinaryData().toBytes());

        String contentType = actual.getContentType();
        String[] parts = contentType.split("\\+");

        assertEquals(2, parts.length, "Expect 2 parts. Actual: " + contentType);
        assertEquals(ContentType.APPLICATION_JSON, parts[0]);
        assertEquals(schemaProperties.getId(), parts[1],
            "Expected id." + schemaProperties.getId() + " Actual: " + parts[1]);
    }

    /**
     * Returns an error if there is no schema definition for the object.
     */
    @Test
    public void serializesNoSchemaDefinition() {
        // Arrange
        SerializerOptions serializerOptions = new SerializerOptions(SCHEMA_GROUP, true,
            100, JSON_SERIALIZER);

        when(registryAsyncClient.registerSchema(eq(SCHEMA_GROUP), eq(Address.class.getName()), any(),
            eq(SchemaFormat.JSON))).thenAnswer(invocation -> Mono.just(schemaProperties));

        SchemaRegistryJsonSchemaSerializer serializer = new SchemaRegistryJsonSchemaSerializer(registryAsyncClient,
            jsonSchemaGenerator, serializerOptions);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
            () -> serializer.serialize(address, TypeReference.createInstance(MessageContent.class)));
    }

    /**
     * Returns an error when no schema group is set.
     */
    @Test
    public void serializesNoSchemaGroup() {
        // Arrange
        SerializerOptions serializerOptions = new SerializerOptions(null, false,
            100, JSON_SERIALIZER);

        SchemaRegistryJsonSchemaSerializer serializer = new SchemaRegistryJsonSchemaSerializer(registryAsyncClient,
            jsonSchemaGenerator, serializerOptions);

        when(jsonSchemaGenerator.generateSchema(eq(TypeReference.createInstance(Address.class))))
            .thenReturn(Address.JSON_SCHEMA);

        // Act & Assert
        assertThrows(IllegalStateException.class,
            () -> serializer.serialize(address, TypeReference.createInstance(MessageContent.class)));
    }

    /**
     * Test simple error cases.
     */
    @Test
    public void deserializeInvalidArgs() {
        // Arrange
        SerializerOptions serializerOptions = new SerializerOptions(SCHEMA_GROUP, false,
            100, JSON_SERIALIZER);

        SchemaRegistryJsonSchemaSerializer serializer = new SchemaRegistryJsonSchemaSerializer(registryAsyncClient,
            jsonSchemaGenerator, serializerOptions);

        MessageContent messageContent = new MessageContent()
            .setContentType(ContentType.APPLICATION_JSON +  "+" + ADDRESS_SCHEMA_ID)
            .setBodyAsBinaryData(BinaryData.fromString("some-test-address-contents"));
        TypeReference<Address> typeReference = TypeReference.createInstance(Address.class);

        // Act & Assert
        assertThrows(NullPointerException.class, () -> serializer.deserialize(null, typeReference));
        assertThrows(NullPointerException.class, () -> serializer.deserialize(messageContent, null));
    }

    /**
     * Empty body contents.
     */
    @Test
    public void deserializeEmptyOrNullBody() {
        // Arrange
        SerializerOptions serializerOptions = new SerializerOptions(SCHEMA_GROUP, false,
            100, JSON_SERIALIZER);

        SchemaRegistryJsonSchemaSerializer serializer = new SchemaRegistryJsonSchemaSerializer(registryAsyncClient,
            jsonSchemaGenerator, serializerOptions);

        MessageContent emptyBodyMessage = new MessageContent()
            .setContentType(ContentType.APPLICATION_JSON +  "+" + ADDRESS_SCHEMA_ID)
            .setBodyAsBinaryData(BinaryData.fromBytes(new byte[0]));
        MessageContent nullBodyMessage = new MessageContent()
            .setContentType(ContentType.APPLICATION_JSON +  "+" + ADDRESS_SCHEMA_ID)
            .setBodyAsBinaryData(null);

        TypeReference<Address> typeReference = TypeReference.createInstance(Address.class);

        // Act & Assert
        // Test the empty body cases.
        assertNull(serializer.deserialize(emptyBodyMessage, typeReference));
        StepVerifier.create(serializer.deserializeAsync(emptyBodyMessage, typeReference))
            .verifyComplete();

        // Test the null body cases.
        assertNull(serializer.deserialize(nullBodyMessage, typeReference));

        StepVerifier.create(serializer.deserializeAsync(nullBodyMessage, typeReference))
            .verifyComplete();
    }

    static Stream<String> deserializeInvalidContentType() {
        return Stream.of("random-content-type",
            ContentType.APPLICATION_OCTET_STREAM +  "+" + ADDRESS_SCHEMA_ID,
            null,
            "");
    }

    /**
     * Invalid content types.
     */
    @MethodSource
    @ParameterizedTest
    public void deserializeInvalidContentType(String contentType) {
        // Arrange
        SerializerOptions serializerOptions = new SerializerOptions(SCHEMA_GROUP, false,
            100, JSON_SERIALIZER);

        SchemaRegistryJsonSchemaSerializer serializer = new SchemaRegistryJsonSchemaSerializer(registryAsyncClient,
            jsonSchemaGenerator, serializerOptions);

        MessageContent incorrectFormat = new MessageContent()
            .setContentType(contentType)
            .setBodyAsBinaryData(BinaryData.fromString("test"));

        TypeReference<Address> typeReference = TypeReference.createInstance(Address.class);

        StepVerifier.create(serializer.deserializeAsync(incorrectFormat, typeReference))
            .expectError(IllegalArgumentException.class)
            .verify();
    }

    /**
     * Deserializes a message into its object and validates it.
     */
    @Test
    public void deserialize() throws IOException {
        // Arrange
        SerializerOptions serializerOptions = new SerializerOptions(SCHEMA_GROUP, false,
            100, JSON_SERIALIZER);

        SchemaRegistryJsonSchemaSerializer serializer = new SchemaRegistryJsonSchemaSerializer(registryAsyncClient,
            jsonSchemaGenerator, serializerOptions);

        String schemaDefinition = Address.JSON_SCHEMA;
        SchemaRegistrySchema theSchema = new SchemaRegistrySchema(schemaProperties, schemaDefinition);
        when(registryAsyncClient.getSchema(ADDRESS_SCHEMA_ID)).thenReturn(Mono.just(theSchema));

        TypeReference<Address> type = TypeReference.createInstance(Address.class);
        when(jsonSchemaGenerator.generateSchema(eq(type))).thenReturn(schemaDefinition);
        when(jsonSchemaGenerator.isValid(any(Address.class), eq(type), eq(schemaDefinition))).thenReturn(true);

        byte[] serialized = JSON_SERIALIZER.serializeToBytes(address);

        MessageContent message = new MessageContent()
            .setContentType(CONTENT_TYPE_WITH_ID)
            .setBodyAsBinaryData(BinaryData.fromBytes(serialized));

        // Act
        Address actual = serializer.deserialize(message, type);

        // Assert
        assertEquals(address.getNumber(), actual.getNumber());
        assertEquals(address.getStreetName(), actual.getStreetName());
        assertEquals(address.getStreetType(), actual.getStreetType());
    }

    /**
     * Deserializes a message and throws an error when the schema is not valid.
     */
    @Test
    public void deserializeErrorNotMatchingSchema() throws IOException {
        // Arrange
        SerializerOptions serializerOptions = new SerializerOptions(SCHEMA_GROUP, false,
            100, JSON_SERIALIZER);

        SchemaRegistryJsonSchemaSerializer serializer = new SchemaRegistryJsonSchemaSerializer(registryAsyncClient,
            jsonSchemaGenerator, serializerOptions);

        String schemaDefinition = Address.JSON_SCHEMA;
        SchemaRegistrySchema theSchema = new SchemaRegistrySchema(schemaProperties, schemaDefinition);
        when(registryAsyncClient.getSchema(ADDRESS_SCHEMA_ID)).thenReturn(Mono.just(theSchema));

        TypeReference<Address> type = TypeReference.createInstance(Address.class);
        when(jsonSchemaGenerator.generateSchema(eq(type))).thenReturn(schemaDefinition);
        when(jsonSchemaGenerator.isValid(any(Address.class), eq(type), eq(schemaDefinition))).thenReturn(false);

        byte[] serialized = JSON_SERIALIZER.serializeToBytes(address);

        MessageContent message = new MessageContent()
            .setContentType(CONTENT_TYPE_WITH_ID)
            .setBodyAsBinaryData(BinaryData.fromBytes(serialized));

        // Act
        assertThrows(IllegalArgumentException.class, () -> serializer.deserialize(message, type));

        // Verify the IllegalArgumentException is actually due to the false return.
        verify(jsonSchemaGenerator).isValid(any(Address.class), eq(type), eq(schemaDefinition));
    }


    /**
     * Deserializes a message and user method throws an error. We expect user code to return normally.
     */
    @Test
    public void deserializeErrorIsValidThrowsException() throws IOException {
        // Arrange
        SerializerOptions serializerOptions = new SerializerOptions(SCHEMA_GROUP, false,
            100, JSON_SERIALIZER);

        SchemaRegistryJsonSchemaSerializer serializer = new SchemaRegistryJsonSchemaSerializer(registryAsyncClient,
            jsonSchemaGenerator, serializerOptions);

        String schemaDefinition = Address.JSON_SCHEMA;
        SchemaRegistrySchema theSchema = new SchemaRegistrySchema(schemaProperties, schemaDefinition);
        when(registryAsyncClient.getSchema(ADDRESS_SCHEMA_ID)).thenReturn(Mono.just(theSchema));

        TypeReference<Address> type = TypeReference.createInstance(Address.class);
        when(jsonSchemaGenerator.generateSchema(eq(type))).thenReturn(schemaDefinition);
        when(jsonSchemaGenerator.isValid(any(Address.class), eq(type), eq(schemaDefinition))).thenThrow(new SomeException());

        byte[] serialized = JSON_SERIALIZER.serializeToBytes(address);

        MessageContent message = new MessageContent()
            .setContentType(CONTENT_TYPE_WITH_ID)
            .setBodyAsBinaryData(BinaryData.fromBytes(serialized));

        // Act
        assertThrows(SomeException.class, () -> serializer.deserialize(message, type));

        // Verify the IllegalArgumentException is actually due to the false return.
        verify(jsonSchemaGenerator).isValid(any(Address.class), eq(type), eq(schemaDefinition));
    }

    static final class SomeException extends RuntimeException {

    }

    static final class NoArgMessage extends MessageContent {
        NoArgMessage() {
            super();
        }
    }
}
