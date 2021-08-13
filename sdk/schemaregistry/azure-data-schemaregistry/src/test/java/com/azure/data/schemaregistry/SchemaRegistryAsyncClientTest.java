// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpResponse;
import com.azure.data.schemaregistry.implementation.AzureSchemaRegistry;
import com.azure.data.schemaregistry.implementation.Schemas;
import com.azure.data.schemaregistry.implementation.models.SchemaId;
import com.azure.data.schemaregistry.implementation.models.SchemasGetByIdHeaders;
import com.azure.data.schemaregistry.implementation.models.SchemasGetByIdResponse;
import com.azure.data.schemaregistry.implementation.models.SchemasQueryIdByContentResponse;
import com.azure.data.schemaregistry.implementation.models.SchemasRegisterResponse;
import com.azure.data.schemaregistry.implementation.models.SerializationType;
import com.azure.data.schemaregistry.implementation.models.ServiceErrorResponseException;
import com.azure.data.schemaregistry.models.SchemaProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
    private int maximumCacheSize;

    @BeforeEach
    protected void setUp() {
        this.guidCache = new HashMap<>();
        this.schemaStringCache = new HashMap<>();

        this.typeParserDictionary = new ConcurrentSkipListMap<>(String.CASE_INSENSITIVE_ORDER);
        this.typeParserDictionary.put(MOCK_SERIALIZATION.toString(), (s) -> s);

        this.maximumCacheSize = 15;
        this.restService = mock(AzureSchemaRegistry.class);
        this.client = new SchemaRegistryAsyncClient(
            this.restService,
            this.maximumCacheSize,
            this.typeParserDictionary);
        this.schemas = mock(Schemas.class);
    }

    @AfterEach
    protected void tearDown() {
        validateMockitoUsage();
    }

    @Test
    public void testRegisterSchemaRegistersTwoVersions() {
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

        verify(schemas, times(2))
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
        mockHeaders.setSchemaType(MOCK_SERIALIZATION.toString());
        when(restService.getSchemas()).thenReturn(schemas);
        when(schemas.getByIdWithResponseAsync(mockId))
            .thenReturn(
                Mono.just(new SchemasGetByIdResponse(
                    null,
                    200,
                    null,
                    MOCK_AVRO_SCHEMA.getBytes(StandardCharsets.UTF_8),
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
    public void testClientReset() {
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

        String doesntMatter = "doesn't matter";
        when(restService.getSchemas()).thenReturn(schemas);
        when(schemas.registerWithResponseAsync(eq(doesntMatter), eq(doesntMatter), eq(SerializationType.AVRO), anyString()))
            .thenReturn(Mono.error(new ServiceErrorResponseException("foo", mock(HttpResponse.class))));

        // Act & Assert
        StepVerifier.create(client.registerSchema(doesntMatter, doesntMatter, doesntMatter, MOCK_SERIALIZATION))
            .expectErrorSatisfies(error -> {
                assertTrue(error instanceof HttpResponseException);
            })
            .verify();

        verify(schemas)
            .registerWithResponseAsync(eq(doesntMatter), eq(doesntMatter), any(SerializationType.class), anyString());
    }

    @Test
    public void testGetIdBySchemaContentNotFoundThenThrows() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Not found", mock(HttpResponse.class));
        MOCK_SCHEMA_ID.setId(MOCK_ID);

        String doesntMatter = "doesn't matter";

        when(restService.getSchemas()).thenReturn(schemas);
        when(schemas.queryIdByContentWithResponseAsync(anyString(), anyString(),
            any(SerializationType.class), anyString()))
            .thenReturn(Mono.error(exception));

        StepVerifier.create(client.getSchemaId(doesntMatter, doesntMatter, doesntMatter, MOCK_SERIALIZATION))
            .verifyError(ResourceNotFoundException.class);

        verify(schemas, times(1))
            .queryIdByContentWithResponseAsync(eq(doesntMatter), eq(doesntMatter), any(SerializationType.class), anyString());
    }

    @Test
    public void testGetSchemaByIdNotFoundThenThrows() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Not found", mock(HttpResponse.class));

        String mockId = "mock-id---";
        when(restService.getSchemas()).thenReturn(schemas);
        when(schemas.getByIdWithResponseAsync(mockId))
            .thenReturn(Mono.error(exception));

        StepVerifier.create(client.getSchema(mockId))
            .verifyError(ResourceNotFoundException.class);

        verify(schemas, times(1))
            .getByIdWithResponseAsync(mockId);
    }
}
