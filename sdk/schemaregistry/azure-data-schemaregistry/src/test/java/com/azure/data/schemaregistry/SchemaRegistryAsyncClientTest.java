// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.data.schemaregistry.implementation.AzureSchemaRegistry;
import com.azure.data.schemaregistry.implementation.Schemas;
import com.azure.data.schemaregistry.implementation.models.SchemaId;
import com.azure.data.schemaregistry.implementation.models.SchemasGetByIdHeaders;
import com.azure.data.schemaregistry.implementation.models.SchemasGetByIdResponse;
import com.azure.data.schemaregistry.implementation.models.SchemasQueryIdByContentResponse;
import com.azure.data.schemaregistry.implementation.models.SchemasRegisterResponse;
import com.azure.data.schemaregistry.implementation.models.SerializationType;
import com.azure.data.schemaregistry.models.SchemaProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SchemaRegistryAsyncClientTest {

    private static final com.azure.data.schemaregistry.models.SerializationType MOCK_SERIALIZATION =
        com.azure.data.schemaregistry.models.SerializationType.fromString("mock_serialization_type");
    private static final String MOCK_ID = "mock_guid";
    private static final SchemaId MOCK_SCHEMA_ID = new SchemaId();
    private static final String MOCK_GROUP = "mockgroup";
    private static final String MOCK_SCHEMA_NAME = "mockname";
    private static final String MOCK_AVRO_SCHEMA = "{\"namespace\":\"example2.avro\",\"type\":\"record\",\"name\":\"User\",\"fields\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"favorite_number\",\"type\": [\"int\", \"null\"]}]}";

    private SchemaRegistryAsyncClient client;
    private AzureSchemaRegistry restService;
    private HashMap<String, SchemaProperties> guidCache;
    private HashMap<String, SchemaProperties> schemaStringCache;
    private ConcurrentSkipListMap<String, Function<String, Object>> typeParserDictionary;
    private Schemas schemas;

    @BeforeEach
    protected void setUp() {
        this.guidCache = new HashMap<String, SchemaProperties>();
        this.schemaStringCache = new HashMap<String, SchemaProperties>();

        this.typeParserDictionary = new ConcurrentSkipListMap<>(String.CASE_INSENSITIVE_ORDER);
        this.typeParserDictionary.put(MOCK_SERIALIZATION.toString(), (s) -> s);

        this.restService = mock(AzureSchemaRegistry.class);
        this.client = new SchemaRegistryAsyncClient(
            this.restService,
            SchemaRegistryAsyncClient.MAX_SCHEMA_MAP_SIZE_DEFAULT,
            this.typeParserDictionary);
        this.schemas = mock(Schemas.class);
    }

    @AfterEach
    protected void tearDown() {
        validateMockitoUsage();
    }

    @Test
    public void testRegisterThenSchemaCacheHit() throws Exception {
        MOCK_SCHEMA_ID.setId(MOCK_ID);
        when(restService.getSchemas()).thenReturn(schemas);
        when(schemas.registerWithResponseAsync(anyString(), anyString(),
            any(com.azure.data.schemaregistry.implementation.models.SerializationType.class),
            anyString()))
            .thenReturn(
                Mono.just(
                    new SchemasRegisterResponse(
                        null,
                        200,
                        null,
                        MOCK_SCHEMA_ID,
                        null)));

        assertEquals(
            MOCK_ID,
            client.registerSchema(MOCK_GROUP, MOCK_SCHEMA_NAME, MOCK_AVRO_SCHEMA, MOCK_SERIALIZATION)
                .block().getSchemaId());
        assertEquals(
            MOCK_ID,
            client.registerSchema(MOCK_GROUP, MOCK_SCHEMA_NAME, MOCK_AVRO_SCHEMA, MOCK_SERIALIZATION)
                .block().getSchemaId());

        verify(schemas, times(1))
            .registerWithResponseAsync(MOCK_GROUP, MOCK_SCHEMA_NAME, SerializationType.AVRO, MOCK_AVRO_SCHEMA);
    }

    @Test
    public void testGetGuidThenSchemaCacheHit() throws Exception {
        MOCK_SCHEMA_ID.setId(MOCK_ID);
        when(restService.getSchemas()).thenReturn(schemas);
        when(schemas.queryIdByContentWithResponseAsync(anyString(), anyString(),
            any(SerializationType.class), anyString()))
            .thenReturn(
                Mono.just(
                    new SchemasQueryIdByContentResponse(
                        null,
                        200,
                        null,
                        MOCK_SCHEMA_ID,
                        null)));

        assertEquals(MOCK_ID,
            client.getSchemaId(MOCK_GROUP, MOCK_SCHEMA_NAME, MOCK_AVRO_SCHEMA, MOCK_SERIALIZATION).block());
        assertEquals(MOCK_ID,
            client.getSchemaId(MOCK_GROUP, MOCK_SCHEMA_NAME, MOCK_AVRO_SCHEMA, MOCK_SERIALIZATION).block());

        verify(schemas, times(1))
            .queryIdByContentWithResponseAsync(anyString(), anyString(), any(SerializationType.class), anyString());
    }

    @Test
    public void testGetSchemaThenGuidCacheHit() throws Exception {
        String mockId = "mock-id---";
        SchemasGetByIdHeaders mockHeaders = new SchemasGetByIdHeaders();
        mockHeaders.setXSchemaType(MOCK_SERIALIZATION.toString());
        when(restService.getSchemas()).thenReturn(schemas);
        when(schemas.getByIdWithResponseAsync(mockId))
            .thenReturn(
                Mono.just(new SchemasGetByIdResponse(
                    null,
                    200,
                    null,
                    MOCK_AVRO_SCHEMA,
                    mockHeaders)));

        SchemaProperties first = client.getSchema(mockId.toString()).block();
        SchemaProperties second = client.getSchema(mockId.toString()).block();

        assertTrue(first.equals(second));
        assertEquals(mockId.toString(), first.getSchemaId());

        verify(schemas, times(1)).getByIdWithResponseAsync(mockId);
    }

    @Test
    public void testClientReset() throws Exception {
        MOCK_SCHEMA_ID.setId(MOCK_ID);
        when(restService.getSchemas()).thenReturn(schemas);
        when(schemas.registerWithResponseAsync(anyString(), anyString(), any(SerializationType.class),
            anyString()))
            .thenReturn(
                Mono.just(
                    new SchemasRegisterResponse(
                        null,
                        200,
                        null,
                        MOCK_SCHEMA_ID,
                        null)));

        assertEquals(
            MOCK_ID,
            client.registerSchema(MOCK_GROUP, MOCK_SCHEMA_NAME, MOCK_AVRO_SCHEMA, MOCK_SERIALIZATION)
                .block().getSchemaId());

        client.clearCache();

        assertEquals(0, guidCache.size());
        assertEquals(0, schemaStringCache.size());
        assertEquals(0, this.typeParserDictionary.size());

        this.typeParserDictionary.put(MOCK_SERIALIZATION.toString(), (s) -> s);

        assertEquals(
            MOCK_ID,
            client.registerSchema(MOCK_GROUP, MOCK_SCHEMA_NAME, MOCK_AVRO_SCHEMA, MOCK_SERIALIZATION)
                .block().getSchemaId());

        verify(schemas, times(2))
            .registerWithResponseAsync(MOCK_GROUP, MOCK_SCHEMA_NAME, SerializationType.AVRO, MOCK_AVRO_SCHEMA);
    }

    @Test
    public void testBadRegisterRequestThenThrows() {
        MOCK_SCHEMA_ID.setId(MOCK_ID);
        when(restService.getSchemas()).thenReturn(schemas);
        when(schemas.registerWithResponseAsync(anyString(), anyString(), any(SerializationType.class),
            anyString()))
            .thenReturn(
                Mono.just(
                    new SchemasRegisterResponse(
                        null,
                        400,
                        null,
                        null,
                        null)));
        try {
            client.registerSchema(
                "doesn't matter",
                "doesn't matter",
                "doesn't matter",
                MOCK_SERIALIZATION).block();
            fail("Should throw on 400 status code");
        } catch (IllegalStateException e) {
            assert true;
        }

        verify(schemas, times(1))
            .registerWithResponseAsync(anyString(), anyString(), any(SerializationType.class), anyString());
    }

    @Test
    public void testGetIdBySchemaContentNotFoundThenThrows() {
        MOCK_SCHEMA_ID.setId(MOCK_ID);
        when(restService.getSchemas()).thenReturn(schemas);
        when(schemas.queryIdByContentWithResponseAsync(anyString(), anyString(),
            any(SerializationType.class), anyString()))
            .thenReturn(
                Mono.just(
                    new SchemasQueryIdByContentResponse(
                        null,
                        404,
                        null,
                        null,
                        null)));

        try {
            client.getSchemaId(
                "doesn't matter",
                "doesn't matter",
                "doesn't matter",
                MOCK_SERIALIZATION).block();
            fail("Should throw on 404 status code");
        } catch (IllegalStateException e) {
            assert true;
        }

        verify(schemas, times(1))
            .queryIdByContentWithResponseAsync(anyString(), anyString(), any(SerializationType.class), anyString());
    }

    @Test
    public void testGetSchemaByIdNotFoundThenThrows() {
        String mockId = "mock-id---";
        when(restService.getSchemas()).thenReturn(schemas);
        when(schemas.getByIdWithResponseAsync(mockId))
            .thenReturn(
                Mono.just(new SchemasGetByIdResponse(
                    null,
                    404,
                    null,
                    null,
                    null)));

        try {
            client.getSchema(mockId).block();
            fail("Should have thrown on 404 status code");
        } catch (IllegalStateException e) {
            assert true;
        } catch (Exception e) {
            assert false;
        }

        verify(schemas, times(1))
            .getByIdWithResponseAsync(mockId);
    }
}
