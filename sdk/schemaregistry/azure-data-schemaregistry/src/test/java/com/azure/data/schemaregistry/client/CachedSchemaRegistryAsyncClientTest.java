// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.data.schemaregistry.implementation.AzureSchemaRegistryRestService;
import com.azure.data.schemaregistry.implementation.models.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CachedSchemaRegistryAsyncClientTest {
    private static final String MOCK_SERIALIZATION = "mock_serialization_type";
    private static final String MOCK_ID = "mock_guid";
    private static final SchemaId MOCK_SCHEMA_ID = new SchemaId();
    private static final String MOCK_GROUP = "mockgroup";
    private static final String MOCK_SCHEMA_NAME = "mockname";
    private static final String MOCK_AVRO_SCHEMA = "{\"namespace\":\"example2.avro\",\"type\":\"record\",\"name\":\"User\",\"fields\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"favorite_number\",\"type\": [\"int\", \"null\"]}]}";

    private CachedSchemaRegistryAsyncClient client;
    private AzureSchemaRegistryRestService restService;
    private HashMap<String, SchemaRegistryObject> guidCache;
    private HashMap<String, SchemaRegistryObject> schemaStringCache;
    private ConcurrentSkipListMap<String, Function<String, Object>> typeParserDictionary;

    @BeforeEach
    protected void setUp() {
        this.guidCache = new HashMap<String, SchemaRegistryObject>();
        this.schemaStringCache = new HashMap<String, SchemaRegistryObject>();

        this.typeParserDictionary = new ConcurrentSkipListMap<>(String.CASE_INSENSITIVE_ORDER);
        this.typeParserDictionary.put(MOCK_SERIALIZATION, (s) -> s);

        this.restService = mock(AzureSchemaRegistryRestService.class);
        this.client = new CachedSchemaRegistryAsyncClient(
            this.restService,
            this.guidCache,
            this.schemaStringCache,
            this.typeParserDictionary);
    }

    @AfterEach
    protected void tearDown() {
        validateMockitoUsage();
    }

    @Test
    public void testRegisterThenSchemaCacheHit() throws Exception {
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
            client.register(MOCK_GROUP, MOCK_SCHEMA_NAME, MOCK_AVRO_SCHEMA, MOCK_SERIALIZATION).block().getSchemaId());
        assertEquals(
            MOCK_ID,
            client.register(MOCK_GROUP, MOCK_SCHEMA_NAME, MOCK_AVRO_SCHEMA, MOCK_SERIALIZATION).block().getSchemaId());

        verify(restService, times(1))
            .createSchemaWithResponseAsync(MOCK_GROUP, MOCK_SCHEMA_NAME, MOCK_SERIALIZATION, MOCK_AVRO_SCHEMA);
    }

    @Test
    public void testGetGuidThenSchemaCacheHit() throws Exception {
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
    public void testGetSchemaThenGuidCacheHit() throws Exception {
        String mockId = "mock-id---";
        GetSchemaByIdHeaders mockHeaders = new GetSchemaByIdHeaders();
        mockHeaders.setXSchemaType(MOCK_SERIALIZATION);
        when(restService.getSchemaByIdWithResponseAsync(mockId))
            .thenReturn(
                Mono.just(new GetSchemaByIdResponse(
                    null,
                    200,
                    null,
                    MOCK_AVRO_SCHEMA,
                    mockHeaders)));

        SchemaRegistryObject first = client.getSchemaById(mockId.toString()).block();
        SchemaRegistryObject second = client.getSchemaById(mockId.toString()).block();

        assertTrue(first.equals(second));
        assertEquals(mockId.toString(), first.getSchemaId());

        verify(restService, times(1)).getSchemaByIdWithResponseAsync(mockId);
    }

    @Test
    public void testClientReset() throws Exception {
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
            client.register(MOCK_GROUP, MOCK_SCHEMA_NAME, MOCK_AVRO_SCHEMA, MOCK_SERIALIZATION).block().getSchemaId());

        client.reset();

        assertEquals(0, guidCache.size());
        assertEquals(0, schemaStringCache.size());
        assertEquals(0, this.typeParserDictionary.size());

        this.typeParserDictionary.put(MOCK_SERIALIZATION, (s) -> s);

        assertEquals(
            MOCK_ID,
            client.register(MOCK_GROUP, MOCK_SCHEMA_NAME, MOCK_AVRO_SCHEMA, MOCK_SERIALIZATION).block().getSchemaId());

        verify(restService, times(2))
            .createSchemaWithResponseAsync(MOCK_GROUP, MOCK_SCHEMA_NAME, MOCK_SERIALIZATION, MOCK_AVRO_SCHEMA);
    }

    @Test
    public void testBadRegisterRequestThenThrows() {
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
            client.register(
                "doesn't matter",
                "doesn't matter",
                "doesn't matter",
                "doesn't matter").block();
            fail("Should throw on 400 status code");
        } catch (SchemaRegistryClientException e) {
            assert true;
        }

        verify(restService, times(1))
            .createSchemaWithResponseAsync(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void testGetIdBySchemaContentNotFoundThenThrows() {
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
                "doesn't matter").block();
            fail("Should throw on 404 status code");
        } catch (SchemaRegistryClientException e) {
            assert true;
        }

        verify(restService, times(1))
            .getIdBySchemaContentWithResponseAsync(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void testGetSchemaByIdNotFoundThenThrows() {
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
            client.getSchemaById(mockId).block();
            fail("Should have thrown on 404 status code");
        } catch (SchemaRegistryClientException e) {
            assert true;
        } catch (Exception e){
            assert false;
        }

        verify(restService, times(1))
            .getSchemaByIdWithResponseAsync(mockId);
    }
}
