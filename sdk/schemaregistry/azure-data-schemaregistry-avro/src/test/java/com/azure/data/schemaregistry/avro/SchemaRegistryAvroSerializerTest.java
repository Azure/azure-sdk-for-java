package com.azure.data.schemaregistry.avro;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.validateMockitoUsage;

import java.io.ByteArrayOutputStream;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.data.schemaregistry.SchemaRegistryAsyncClient;
import com.azure.data.schemaregistry.SchemaRegistryClientBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class SchemaRegistryAvroSerializerTest {
    private static String MOCK_GROUP = "mock-group";
    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
        validateMockitoUsage();
    }

    @Test
    void testSerializeNullObjectReturnsEmpty() {
        SchemaRegistryAvroSerializer serializer = new SchemaRegistryAvroSerializer(
            getMockSchemaRegistryAsyncClient(),
            MOCK_GROUP,
            false,
            new SchemaRegistryAvroUtils(true));

        assertNull(serializer.serialize(new ByteArrayOutputStream(), null));
    }

    SchemaRegistryAsyncClient getMockSchemaRegistryAsyncClient() {
        return new SchemaRegistryClientBuilder()
            .credential(mock(TokenCredential.class))
            .httpClient(mock(HttpClient.class))
            .buildAsyncClient();
    }
}
