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
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    private static final String MOCK_AVRO_SCHEMA =
        "{\"namespace\":\"example2.avro\",\"type\":\"record\",\"name\":\"User\",\"fields\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"favorite_number\",\"type\": [\"int\", \"null\"]}]}";

    private SchemaRegistryAsyncClient client;
    private AzureSchemaRegistry restService;
    private HashMap<String, SchemaProperties> guidCache;
    private HashMap<String, SchemaProperties> schemaStringCache;
    private ConcurrentSkipListMap<String, Function<String, Object>> typeParserDictionary;
    private Schemas schemas;

    @BeforeEach
    protected void setUp() {
        this.guidCache = new HashMap<>();
        this.schemaStringCache = new HashMap<>();

        this.typeParserDictionary = new ConcurrentSkipListMap<>(String.CASE_INSENSITIVE_ORDER);
        this.typeParserDictionary.put(MOCK_SERIALIZATION.toString(), (s) -> s);

        this.restService = mock(AzureSchemaRegistry.class);
        this.client = new SchemaRegistryAsyncClient(
            this.restService,
            this.typeParserDictionary);
        this.schemas = mock(Schemas.class);
    }

    @AfterEach
    protected void tearDown() {
        validateMockitoUsage();
    }

    @Test
    public void testRegisterThenSchemaCacheHit() {
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

        StepVerifier.create(client.registerSchema(MOCK_GROUP, MOCK_SCHEMA_NAME, MOCK_AVRO_SCHEMA, MOCK_SERIALIZATION))
            .assertNext(properties -> assertEquals(MOCK_ID, properties.getSchemaId()))
            .verifyComplete();

        StepVerifier.create(client.registerSchema(MOCK_GROUP, MOCK_SCHEMA_NAME, MOCK_AVRO_SCHEMA, MOCK_SERIALIZATION))
            .assertNext(properties -> assertEquals(MOCK_ID, properties.getSchemaId()))
            .verifyComplete();

        verify(schemas, times(1))
            .registerWithResponseAsync(MOCK_GROUP, MOCK_SCHEMA_NAME, SerializationType.AVRO, MOCK_AVRO_SCHEMA);
    }

    @Test
    public void testGetGuidThenSchemaCacheHit() {
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

        StepVerifier.create(client.getSchemaId(MOCK_GROUP, MOCK_SCHEMA_NAME, MOCK_AVRO_SCHEMA, MOCK_SERIALIZATION))
            .assertNext(schemaId -> assertEquals(MOCK_ID, schemaId))
            .verifyComplete();

        StepVerifier.create(client.getSchemaId(MOCK_GROUP, MOCK_SCHEMA_NAME, MOCK_AVRO_SCHEMA, MOCK_SERIALIZATION))
            .assertNext(schemaId -> assertEquals(MOCK_ID, schemaId))
            .verifyComplete();

        verify(schemas, times(1))
            .queryIdByContentWithResponseAsync(anyString(), anyString(), any(SerializationType.class), anyString());
    }

    @Test
    public void testGetSchemaThenGuidCacheHit() {
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

        StepVerifier.create(client.getSchema(mockId)
            .flatMap(properties -> Mono.just(properties).zipWith(client.getSchema(mockId))))
            .assertNext(tuple2 -> {
                assertEquals(mockId, tuple2.getT1().getSchemaId());
                assertTrue(areSchemaPropertiesEqual(tuple2.getT1(), tuple2.getT2()));
            })
            .verifyComplete();

        verify(schemas, times(1)).getByIdWithResponseAsync(mockId);
    }

    private static boolean areSchemaPropertiesEqual(SchemaProperties properties1, SchemaProperties properties2) {
        if (properties1 == null) {
            return properties2 == null;
        }

        return Arrays.equals(properties1.getSchema(), properties2.getSchema())
            && Objects.equals(properties1.getSchemaId(), properties2.getSchemaId())
            && Objects.equals(properties1.getSchemaName(), properties2.getSchemaName())
            && Objects.equals(properties1.getSerializationType(), properties2.getSerializationType());
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

        StepVerifier.create(client.registerSchema(MOCK_GROUP, MOCK_SCHEMA_NAME, MOCK_AVRO_SCHEMA, MOCK_SERIALIZATION))
            .assertNext(properties -> assertEquals(MOCK_ID, properties.getSchemaId()))
            .verifyComplete();

        client.clearCache();

        assertEquals(0, guidCache.size());
        assertEquals(0, schemaStringCache.size());
        assertEquals(0, this.typeParserDictionary.size());

        this.typeParserDictionary.put(MOCK_SERIALIZATION.toString(), (s) -> s);

        StepVerifier.create(client.registerSchema(MOCK_GROUP, MOCK_SCHEMA_NAME, MOCK_AVRO_SCHEMA, MOCK_SERIALIZATION))
            .assertNext(properties -> assertEquals(MOCK_ID, properties.getSchemaId()))
            .verifyComplete();

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

        String doesntMatter = "doesn't matter";
        StepVerifier.create(client.registerSchema(doesntMatter, doesntMatter, doesntMatter, MOCK_SERIALIZATION))
            .verifyError(IllegalStateException.class);

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

        String doesntMatter = "doesn't matter";
        StepVerifier.create(client.getSchemaId(doesntMatter, doesntMatter, doesntMatter, MOCK_SERIALIZATION))
            .verifyError(IllegalStateException.class);

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

        StepVerifier.create(client.getSchema(mockId))
            .verifyError(IllegalStateException.class);

        verify(schemas, times(1))
            .getByIdWithResponseAsync(mockId);
    }
}
