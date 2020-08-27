// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.azure.data.schemaregistry.implementation.AzureSchemaRegistryRestService;
import com.azure.data.schemaregistry.implementation.models.CreateSchemaResponse;
import com.azure.data.schemaregistry.implementation.models.GetIdBySchemaContentResponse;
import com.azure.data.schemaregistry.implementation.models.GetSchemaByIdHeaders;
import com.azure.data.schemaregistry.implementation.models.GetSchemaByIdResponse;
import com.azure.data.schemaregistry.implementation.models.SchemaId;
import com.azure.data.schemaregistry.models.SchemaRegistryObject;
import com.azure.data.schemaregistry.models.SerializationType;
import java.util.HashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Function;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

class SchemaRegistryAsyncClientTest {

    private static final SerializationType MOCK_SERIALIZATION = SerializationType.fromString("mock_serialization_type");
    private static final String MOCK_ID = "mock_guid";
    private static final SchemaId MOCK_SCHEMA_ID = new SchemaId();
    private static final String MOCK_GROUP = "mockgroup";
    private static final String MOCK_SCHEMA_NAME = "mockname";
    private static final String MOCK_AVRO_SCHEMA = "{\"namespace\":\"example2.avro\",\"type\":\"record\",\"name\":\"User\",\"fields\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"favorite_number\",\"type\": [\"int\", \"null\"]}]}";

    private SchemaRegistryAsyncClient client;
    private AzureSchemaRegistryRestService restService;
    private HashMap<String, SchemaRegistryObject> guidCache;
    private HashMap<String, SchemaRegistryObject> schemaStringCache;
    private ConcurrentSkipListMap<String, Function<String, Object>> typeParserDictionary;

    @BeforeEach
    void setUp() {
        this.guidCache = new HashMap<String, SchemaRegistryObject>();
        this.schemaStringCache = new HashMap<String, SchemaRegistryObject>();

        this.typeParserDictionary = new ConcurrentSkipListMap<>(String.CASE_INSENSITIVE_ORDER);
        this.typeParserDictionary.put(MOCK_SERIALIZATION.toString(), (s) -> s);

        this.restService = mock(AzureSchemaRegistryRestService.class);
        this.client = new SchemaRegistryAsyncClient(
            this.restService,
            this.guidCache,
            this.schemaStringCache,
            this.typeParserDictionary);
    }

    @AfterEach
    void tearDown() {
        validateMockitoUsage();
    }

    @Test
    void testRegisterThenSchemaCacheHit() {
        MOCK_SCHEMA_ID.setId(MOCK_ID);
        when(restService.createSchemaWithResponseAsync(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(
                Mono.just(
                    new CreateSchemaResponse(
                        null,
                        200,
                        null,
                        MOCK_SCHEMA_ID,
                        null)));

        assertEquals(
            MOCK_ID,
            client.registerSchema(MOCK_GROUP, MOCK_SCHEMA_NAME, MOCK_AVRO_SCHEMA, MOCK_SERIALIZATION).block().getSchemaId());
        assertEquals(
            MOCK_ID,
            client.registerSchema(MOCK_GROUP, MOCK_SCHEMA_NAME, MOCK_AVRO_SCHEMA, MOCK_SERIALIZATION).block().getSchemaId());

        verify(restService, times(1))
            .createSchemaWithResponseAsync(MOCK_GROUP, MOCK_SCHEMA_NAME, MOCK_SERIALIZATION.toString(), MOCK_AVRO_SCHEMA);
    }

    @Test
    void testGetGuidThenSchemaCacheHit() throws Exception {
        MOCK_SCHEMA_ID.setId(MOCK_ID);
        when(restService.getIdBySchemaContentWithResponseAsync(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(
                Mono.just(
                    new GetIdBySchemaContentResponse(
                        null,
                        200,
                        null,
                        MOCK_SCHEMA_ID,
                        null)));

        assertEquals(MOCK_ID,
            client.getSchemaId(MOCK_GROUP, MOCK_SCHEMA_NAME, MOCK_AVRO_SCHEMA, MOCK_SERIALIZATION).block());
        assertEquals(MOCK_ID,
            client.getSchemaId(MOCK_GROUP, MOCK_SCHEMA_NAME, MOCK_AVRO_SCHEMA, MOCK_SERIALIZATION).block());

        verify(restService, times(1))
            .getIdBySchemaContentWithResponseAsync(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testGetSchemaThenGuidCacheHit() throws Exception {
        String mockId = "mock-id---";
        GetSchemaByIdHeaders mockHeaders = new GetSchemaByIdHeaders();
        mockHeaders.setXSchemaType(MOCK_SERIALIZATION.toString());
        when(restService.getSchemaByIdWithResponseAsync(mockId))
            .thenReturn(
                Mono.just(new GetSchemaByIdResponse(
                    null,
                    200,
                    null,
                    MOCK_AVRO_SCHEMA,
                    mockHeaders)));

        SchemaRegistryObject first = client.getSchema(mockId.toString()).block();
        SchemaRegistryObject second = client.getSchema(mockId.toString()).block();

        assertTrue(first.equals(second));
        assertEquals(mockId.toString(), first.getSchemaId());

        verify(restService, times(1)).getSchemaByIdWithResponseAsync(mockId);
    }

    @Test
    void testClientReset() throws Exception {
        MOCK_SCHEMA_ID.setId(MOCK_ID);
        when(restService.createSchemaWithResponseAsync(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(
                Mono.just(
                    new CreateSchemaResponse(
                        null,
                        200,
                        null,
                        MOCK_SCHEMA_ID,
                        null)));

        assertEquals(
            MOCK_ID,
            client.registerSchema(MOCK_GROUP, MOCK_SCHEMA_NAME, MOCK_AVRO_SCHEMA, MOCK_SERIALIZATION).block().getSchemaId());

        client.clearCache();

        assertEquals(0, guidCache.size());
        assertEquals(0, schemaStringCache.size());
        assertEquals(0, this.typeParserDictionary.size());

        this.typeParserDictionary.put(MOCK_SERIALIZATION.toString(), (s) -> s);

        assertEquals(
            MOCK_ID,
            client.registerSchema(MOCK_GROUP, MOCK_SCHEMA_NAME, MOCK_AVRO_SCHEMA, MOCK_SERIALIZATION).block().getSchemaId());

        verify(restService, times(2))
            .createSchemaWithResponseAsync(MOCK_GROUP, MOCK_SCHEMA_NAME, MOCK_SERIALIZATION.toString(), MOCK_AVRO_SCHEMA);
    }

    @Test
    void testBadRegisterRequestThenThrows() {
        MOCK_SCHEMA_ID.setId(MOCK_ID);
        when(restService.createSchemaWithResponseAsync(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(
                Mono.just(
                    new CreateSchemaResponse(
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
                SerializationType.fromString("doesn't matter")).block();
            fail("Should throw on 400 status code");
        } catch (IllegalStateException e) {
            assert true;
        }

        verify(restService, times(1))
            .createSchemaWithResponseAsync(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testGetIdBySchemaContentNotFoundThenThrows() {
        MOCK_SCHEMA_ID.setId(MOCK_ID);
        when(restService.getIdBySchemaContentWithResponseAsync(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(
                Mono.just(
                    new GetIdBySchemaContentResponse(
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
                SerializationType.fromString("doesn't matter")).block();
            fail("Should throw on 404 status code");
        } catch (IllegalStateException e) {
            assert true;
        }

        verify(restService, times(1))
            .getIdBySchemaContentWithResponseAsync(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testGetSchemaByIdNotFoundThenThrows() {
        String mockId = "mock-id---";
        when(restService.getSchemaByIdWithResponseAsync(mockId))
            .thenReturn(
                Mono.just(new GetSchemaByIdResponse(
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
        } catch (Exception e){
            assert false;
        }

        verify(restService, times(1))
            .getSchemaByIdWithResponseAsync(mockId);
    }
}
