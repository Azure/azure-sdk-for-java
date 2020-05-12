/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.azure.schemaregistry.client;

import com.azure.schemaregistry.client.rest.RestService;
import com.azure.schemaregistry.client.rest.entities.responses.SchemaObjectResponse;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.HashMap;
import java.util.function.Function;

import static org.easymock.EasyMock.*;

public class CachedSchemaRegistryClientTest extends TestCase {
    private static final String MOCK_SERIALIZATION = "mock_serialization_type";
    private static final String MOCK_GUID = "mock_guid";
    private static final String MOCK_GROUP = "mockgroup";
    private static final String MOCK_SCHEMA_NAME = "mockname";
    private static final String MOCK_AVRO_SCHEMA = "{\"namespace\":\"example2.avro\",\"type\":\"record\",\"name\":\"User\",\"fields\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"favorite_number\",\"type\": [\"int\", \"null\"]}]}";

    private CachedSchemaRegistryClient client;
    private RestService restService;
    private HashMap<String, SchemaRegistryObject<?>> guidCache;
    private HashMap<String, SchemaRegistryObject<?>> schemaStringCache;
    private HashMap<String, Function<String, ?>> typeParserDictionary;

    public CachedSchemaRegistryClientTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(CachedSchemaRegistryClientTest.class);
    }

    protected void setUp() {
        this.guidCache = new HashMap<String, SchemaRegistryObject<?>>();
        this.schemaStringCache = new HashMap<String, SchemaRegistryObject<?>>();

        this.typeParserDictionary = new HashMap<String, Function<String, ?>>();
        this.typeParserDictionary.put(MOCK_SERIALIZATION, (s)->s);

        this.restService = createNiceMock(RestService.class);
        this.client = new CachedSchemaRegistryClient(
            this.restService,
            this.guidCache,
            this.schemaStringCache,
            this.typeParserDictionary);
    }

    protected void tearDown() {
    }

    public void testRegisterThenSchemaCacheHit() throws Exception {
        expect(restService.registerSchema(anyString(), anyString(), anyString(), anyString()))
            .andReturn(MOCK_GUID)
            .once();

        replay(restService);

        assertEquals(MOCK_GUID, client.register(MOCK_GROUP, MOCK_SCHEMA_NAME, MOCK_AVRO_SCHEMA, MOCK_SERIALIZATION).schemaGuid);
        assertEquals(MOCK_GUID, client.register(MOCK_GROUP, MOCK_SCHEMA_NAME, MOCK_AVRO_SCHEMA, MOCK_SERIALIZATION).schemaGuid);

        verify(restService);
    }

    public void testGetGuidThenSchemaCacheHit() throws Exception {
        expect(restService.getGuid(anyString(), anyString(), anyString(), anyString()))
            .andReturn(MOCK_GUID)
            .once();

        replay(restService);

        assertEquals(MOCK_GUID, client.getGuid(MOCK_GROUP, MOCK_SCHEMA_NAME, MOCK_AVRO_SCHEMA, MOCK_SERIALIZATION));
        assertEquals(MOCK_GUID, client.getGuid(MOCK_GROUP, MOCK_SCHEMA_NAME, MOCK_AVRO_SCHEMA, MOCK_SERIALIZATION));

        verify(restService);
    }

    public void testGetSchemaThenGuidCacheHit() throws Exception {
        expect(restService.getSchemaByGuid(anyString()))
            .andReturn(new SchemaObjectResponse(null, MOCK_SERIALIZATION, MOCK_GUID))
            .once();

        replay(restService);

        assertEquals(MOCK_GUID, client.getSchemaByGuid(MOCK_GUID).schemaGuid);
        assertEquals(MOCK_GUID, client.getSchemaByGuid(MOCK_GUID).schemaGuid);

        verify(restService);
    }

    public void testClientReset() throws Exception {
        expect(restService.registerSchema(anyString(), anyString(), anyString(), anyString()))
            .andReturn(MOCK_GUID)
            .times(2);

        replay(restService);

        assertEquals(MOCK_GUID, client.register(MOCK_GROUP, MOCK_SCHEMA_NAME, MOCK_AVRO_SCHEMA, MOCK_SERIALIZATION).schemaGuid);

        client.reset();

        assertEquals(0, guidCache.size());
        assertEquals(0, schemaStringCache.size());
        assertEquals(0, this.typeParserDictionary.size());

        this.typeParserDictionary.put(MOCK_SERIALIZATION, (s)->s);

        assertEquals(MOCK_GUID, client.register(MOCK_GROUP, MOCK_SCHEMA_NAME, MOCK_AVRO_SCHEMA, MOCK_SERIALIZATION).schemaGuid);

        verify(restService);
    }

    // builder tests
    public void testBuilderIfRegistryUrlNullOrEmptyThrow() {
        try {
            new CachedSchemaRegistryClient.Builder("")
                    .loadSchemaParser(null, null)
                    .build();
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            new CachedSchemaRegistryClient.Builder(null)
                    .build();
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }
}
