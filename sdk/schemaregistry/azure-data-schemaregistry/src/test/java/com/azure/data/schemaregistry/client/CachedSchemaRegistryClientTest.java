// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.client;

import com.azure.data.schemaregistry.client.implementation.AzureSchemaRegistryRestService;
import com.azure.data.schemaregistry.client.implementation.models.GetSchemaByIdHeaders;
import com.azure.data.schemaregistry.client.implementation.models.GetSchemaByIdResponse;
import com.azure.data.schemaregistry.client.implementation.models.SchemaId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Function;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CachedSchemaRegistryClientTest {
    private static final String MOCK_SERIALIZATION = "mock_serialization_type";
    private static final String MOCK_ID = "mock_guid";
    private static final SchemaId MOCK_SCHEMA_ID = new SchemaId();
    private static final String MOCK_GROUP = "mockgroup";
    private static final String MOCK_SCHEMA_NAME = "mockname";
    private static final String MOCK_AVRO_SCHEMA = "{\"namespace\":\"example2.avro\",\"type\":\"record\",\"name\":\"User\",\"fields\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"favorite_number\",\"type\": [\"int\", \"null\"]}]}";

    private CachedSchemaRegistryClient client;
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
        this.client = new CachedSchemaRegistryClient(
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
        when(restService.createSchema(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(MOCK_SCHEMA_ID);

        assertEquals(
            MOCK_ID,
            client.register(MOCK_GROUP, MOCK_SCHEMA_NAME, MOCK_AVRO_SCHEMA, MOCK_SERIALIZATION).getSchemaId());
        assertEquals(
            MOCK_ID,
            client.register(MOCK_GROUP, MOCK_SCHEMA_NAME, MOCK_AVRO_SCHEMA, MOCK_SERIALIZATION).getSchemaId());

        verify(restService, times(1))
            .createSchema(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void testGetGuidThenSchemaCacheHit() throws Exception {
        MOCK_SCHEMA_ID.setId(MOCK_ID);
        when(restService.getIdBySchemaContent(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(MOCK_SCHEMA_ID);

        assertEquals(MOCK_ID, client.getSchemaId(MOCK_GROUP, MOCK_SCHEMA_NAME, MOCK_AVRO_SCHEMA, MOCK_SERIALIZATION));
        assertEquals(MOCK_ID, client.getSchemaId(MOCK_GROUP, MOCK_SCHEMA_NAME, MOCK_AVRO_SCHEMA, MOCK_SERIALIZATION));

        verify(restService, times(1))
            .getIdBySchemaContent(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void testGetSchemaThenGuidCacheHit() throws Exception {
        UUID mockId = UUID.randomUUID();
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

        SchemaRegistryObject first = client.getSchemaByGuid(mockId.toString());
        SchemaRegistryObject second = client.getSchemaByGuid(mockId.toString());

        assertTrue(first.equals(second));
        assertEquals(mockId.toString(), first.getSchemaId());

        verify(restService, times(1)).getSchemaByIdWithResponseAsync(mockId);
    }

    @Test
    public void testClientReset() throws Exception {
        MOCK_SCHEMA_ID.setId(MOCK_ID);
        when(restService.createSchema(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(MOCK_SCHEMA_ID);

        assertEquals(
            MOCK_ID,
            client.register(MOCK_GROUP, MOCK_SCHEMA_NAME, MOCK_AVRO_SCHEMA, MOCK_SERIALIZATION).getSchemaId());

        client.reset();

        assertEquals(0, guidCache.size());
        assertEquals(0, schemaStringCache.size());
        assertEquals(0, this.typeParserDictionary.size());

        this.typeParserDictionary.put(MOCK_SERIALIZATION, (s) -> s);

        assertEquals(
            MOCK_ID,
            client.register(MOCK_GROUP, MOCK_SCHEMA_NAME, MOCK_AVRO_SCHEMA, MOCK_SERIALIZATION).getSchemaId());

        verify(restService, times(2))
            .createSchema(anyString(), anyString(), anyString(), anyString());
    }
}
