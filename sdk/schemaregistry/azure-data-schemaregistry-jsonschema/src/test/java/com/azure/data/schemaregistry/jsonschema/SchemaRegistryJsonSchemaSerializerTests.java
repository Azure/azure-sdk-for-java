// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.jsonschema;

import com.azure.core.http.ContentType;
import com.azure.core.models.MessageContent;
import com.azure.core.util.BinaryData;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.core.util.serializer.TypeReference;
import com.azure.data.schemaregistry.SchemaRegistryAsyncClient;
import com.azure.data.schemaregistry.models.SchemaFormat;
import com.azure.data.schemaregistry.models.SchemaProperties;
import com.azure.data.schemaregistry.models.SchemaRegistrySchema;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Tests {@link SchemaRegistryJsonSchemaSerializer}.
 */
public class SchemaRegistryJsonSchemaSerializerTests {
    private static final String ADDRESS_SCHEMA_ID = "address-schema-id";
    private static final String SCHEMA_GROUP = "test-group";
    private static final String CONTENT_TYPE_WITH_ID = ContentType.APPLICATION_JSON + "+" + ADDRESS_SCHEMA_ID;
    private static final SerializerAdapter SERIALIZER_ADAPTER = JacksonAdapter.createDefaultSerializerAdapter();

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
        SerializerOptions options = new SerializerOptions("foo", false, 10, SERIALIZER_ADAPTER);

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
            100, SERIALIZER_ADAPTER);

        when(registryAsyncClient.registerSchema(eq(SCHEMA_GROUP), eq(Address.class.getName()), any(),
            eq(SchemaFormat.JSON))).thenAnswer(invocation -> Mono.just(schemaProperties));

        SchemaRegistryJsonSchemaSerializer serializer = new SchemaRegistryJsonSchemaSerializer(registryAsyncClient,
            jsonSchemaGenerator, serializerOptions);

        when(jsonSchemaGenerator.generateSchema(eq(TypeReference.createInstance(Address.class))))
            .thenReturn(Address.JSON_SCHEMA);

        String expectedContents = SERIALIZER_ADAPTER.serialize(address, SerializerEncoding.JSON);

        // Act
        NoArgMessage actual = serializer.serialize(address, TypeReference.createInstance(NoArgMessage.class));

        // Assert
        assertEquals(expectedContents, actual.getBodyAsBinaryData().toString());

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
            100, SERIALIZER_ADAPTER);

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
            100, SERIALIZER_ADAPTER);

        SchemaRegistryJsonSchemaSerializer serializer = new SchemaRegistryJsonSchemaSerializer(registryAsyncClient,
            jsonSchemaGenerator, serializerOptions);

        when(jsonSchemaGenerator.generateSchema(eq(TypeReference.createInstance(Address.class))))
            .thenReturn(Address.JSON_SCHEMA);

        // Act & Assert
        assertThrows(IllegalStateException.class,
            () -> serializer.serialize(address, TypeReference.createInstance(MessageContent.class)));
    }

    /**
     * Deserializes a message into its object and validates it.
     */
    @Test
    public void deserialize() throws IOException {
        // Arrange
        SerializerOptions serializerOptions = new SerializerOptions(SCHEMA_GROUP, false,
            100, SERIALIZER_ADAPTER);

        SchemaRegistryJsonSchemaSerializer serializer = new SchemaRegistryJsonSchemaSerializer(registryAsyncClient,
            jsonSchemaGenerator, serializerOptions);

        String schemaDefinition = Address.JSON_SCHEMA;
        SchemaRegistrySchema theSchema = new SchemaRegistrySchema(schemaProperties, schemaDefinition);
        when(registryAsyncClient.getSchema(ADDRESS_SCHEMA_ID)).thenReturn(Mono.just(theSchema));

        TypeReference<Address> type = TypeReference.createInstance(Address.class);
        when(jsonSchemaGenerator.generateSchema(eq(type))).thenReturn(schemaDefinition);
        when(jsonSchemaGenerator.isValid(any(Address.class), eq(type), eq(schemaDefinition))).thenReturn(true);

        String serialized = SERIALIZER_ADAPTER.serialize(address, SerializerEncoding.JSON);

        MessageContent message = new MessageContent()
            .setContentType(CONTENT_TYPE_WITH_ID)
            .setBodyAsBinaryData(BinaryData.fromString(serialized));

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
            100, SERIALIZER_ADAPTER);

        SchemaRegistryJsonSchemaSerializer serializer = new SchemaRegistryJsonSchemaSerializer(registryAsyncClient,
            jsonSchemaGenerator, serializerOptions);

        String schemaDefinition = Address.JSON_SCHEMA;
        SchemaRegistrySchema theSchema = new SchemaRegistrySchema(schemaProperties, schemaDefinition);
        when(registryAsyncClient.getSchema(ADDRESS_SCHEMA_ID)).thenReturn(Mono.just(theSchema));

        TypeReference<Address> type = TypeReference.createInstance(Address.class);
        when(jsonSchemaGenerator.generateSchema(eq(type))).thenReturn(schemaDefinition);
        when(jsonSchemaGenerator.isValid(any(Address.class), eq(type), eq(schemaDefinition))).thenReturn(false);

        String serialized = SERIALIZER_ADAPTER.serialize(address, SerializerEncoding.JSON);

        MessageContent message = new MessageContent()
            .setContentType(CONTENT_TYPE_WITH_ID)
            .setBodyAsBinaryData(BinaryData.fromString(serialized));

        // Act
        assertThrows(IllegalArgumentException.class, () -> serializer.deserialize(message, type));
    }

    static final class NoArgMessage extends MessageContent {
        NoArgMessage() {
            super();
        }
    }
}
