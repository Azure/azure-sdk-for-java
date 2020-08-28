// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.data.schemaregistry.models.SchemaRegistryObject;
import com.azure.data.schemaregistry.models.SerializationType;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;

class SchemaRegistrySerializerTest {
    private static final String MOCK_GUID = new String(new char[SchemaRegistrySerializer.SCHEMA_ID_SIZE]).replace("\0", "a");
    private static final String MOCK_AVRO_SCHEMA_STRING = "{\"namespace\":\"example2.avro\",\"type\":\"record\",\"name\":\"User\",\"fields\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"favorite_number\",\"type\": [\"int\", \"null\"]}]}";
    private final EncoderFactory encoderFactory = EncoderFactory.get();
    private static final Schema MOCK_AVRO_SCHEMA = (new Schema.Parser()).parse(MOCK_AVRO_SCHEMA_STRING);
    private static final String MOCK_SCHEMA_GROUP = "mock-group";

    @Test
    void testRegistryGuidPrefixedToPayload() {
        // manually add SchemaRegistryObject into mock registry client cache
        TestSchemaRegistryUtils encoder = new TestSchemaRegistryUtils();
        SchemaRegistryObject registered = new SchemaRegistryObject(MOCK_GUID,
            encoder.getSerializationType(),
            encoder.getSchemaName(null),
            MOCK_SCHEMA_GROUP,
            encoder.getSchemaString(null).getBytes(), // always returns same schema string
            encoder::parseSchemaString);

        assertEquals(encoder.getSchemaString(null), registered.getSchema());

        SchemaRegistryAsyncClient mockRegistryClient = getMockClient();
        Mockito.when(mockRegistryClient.getSchemaId(anyString(), anyString(), anyString(),
            any(SerializationType.class)))
            .thenReturn(Mono.just(MOCK_GUID));

        TestSerializer serializer = new TestSerializer(
            mockRegistryClient, encoder,false, MOCK_SCHEMA_GROUP);

        try {
            ByteArrayOutputStream payload =  new ByteArrayOutputStream();
            serializer.serializeInternalAsync(payload, 1).block();
            ByteBuffer buffer = ByteBuffer.wrap(payload.toByteArray());
            buffer.get(new byte[SchemaRegistrySerializer.RECORD_FORMAT_INDICATOR_SIZE]);
            byte[] schemaGuidByteArray = new byte[SchemaRegistrySerializer.SCHEMA_ID_SIZE];
            buffer.get(schemaGuidByteArray);

            System.out.println(new String(schemaGuidByteArray));
            // guid should match preloaded SchemaRegistryObject guid
            assertEquals(MOCK_GUID, new String(schemaGuidByteArray));
        } catch (RuntimeException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void testNullPayloadThrowsSerializationException() {
        TestSerializer serializer = new TestSerializer(
            getMockClient(),
            new TestSchemaRegistryUtils(),
            false,
            MOCK_SCHEMA_GROUP);

        try {
            serializer.serializeInternalAsync(new ByteArrayOutputStream(), null).block();
            fail("Serializing null payload failed to throw NullPointerException");
        } catch (NullPointerException e) {
            assertTrue(true);
        }
    }


    @Test
    void testIfRegistryNullThenThrow() {
        try {
            TestSerializer serializer = new TestSerializer(
                null, new TestSchemaRegistryUtils(), false, MOCK_SCHEMA_GROUP);
            fail("Building serializer instance with null registry client failed to throw");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } catch (Exception e) {
            fail("Building serializer instance with null registry client should throw illegal argument exception");
        }
    }

    @Test
    void testAddUtils() throws IOException {
        TestSchemaRegistryUtils decoder = new TestSchemaRegistryUtils();

        // manually add SchemaRegistryObject to cache
        SchemaRegistryObject registered = new SchemaRegistryObject(MOCK_GUID,
            decoder.getSerializationType(),
            decoder.getSchemaName(null),
            MOCK_SCHEMA_GROUP,
            MOCK_AVRO_SCHEMA_STRING.getBytes(),
            decoder::parseSchemaString);

        assertTrue(registered.getSchema() != null);

        SchemaRegistryAsyncClient mockClient = getMockClient();
        Mockito.when(mockClient.getSchema(anyString()))
            .thenReturn(Mono.just(registered));

        TestSerializer serializer = new TestSerializer(mockClient, decoder, true, MOCK_SCHEMA_GROUP);

        assertEquals(MOCK_GUID,
            serializer.schemaRegistryClient.getSchema(MOCK_GUID).block().getSchemaId());

        assertEquals(TestSchemaRegistryUtils.CONSTANT_PAYLOAD,
            serializer.deserializeInternalAsync(new ByteArrayInputStream(getPayload())).block());
    }

    @Test
    void testNullPayload() {
        TestSerializer deserializer = new TestSerializer(
            getMockClient(), new TestSchemaRegistryUtils(), true, MOCK_SCHEMA_GROUP);
        assertNull(deserializer.deserializeInternalAsync(null).block());
    }

    @Test
    void testIfTooShortPayloadThrow() {
        TestSerializer serializer = new TestSerializer(
            getMockClient(), new TestSchemaRegistryUtils(), true, MOCK_SCHEMA_GROUP);

        try {
            serializer.deserializeInternalAsync(new ByteArrayInputStream("aaa".getBytes())).block();
            fail("Too short payload did not throw BufferUnderflowException");
        } catch (BufferUnderflowException e) {
            assertTrue(true);
        }

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                out.write(new byte[] {0x00, 0x00, 0x00, 0x00});
                out.write("aa".getBytes());
            } catch (IOException e) {
                fail();
            }

            ByteArrayInputStream stream = new ByteArrayInputStream(out.toByteArray());
            serializer.deserializeInternalAsync(stream).block();
            fail("Too short payload did not throw BufferUnderflowException");
        } catch (BufferUnderflowException e) {
            assertTrue(true);
        }
    }

    // TODO: add for non-existing guid

    @Test
    void testIfRegistryClientNullOnBuildThrow() {
        try {
            TestSerializer deserializer = new TestSerializer(
                null, new TestSchemaRegistryUtils(), true, MOCK_SCHEMA_GROUP);
            fail("should not get here.");
        } catch (IllegalArgumentException e) {
            // good
        }
    }

    private SchemaRegistryAsyncClient getMockClient() {
        return mock(SchemaRegistryAsyncClient.class);
    }

    private byte[] getPayload() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(new byte[] {0x00, 0x00, 0x00, 0x00});
        out.write(ByteBuffer.allocate(SchemaRegistrySerializer.SCHEMA_ID_SIZE)
            .put(MOCK_GUID.getBytes(StandardCharsets.UTF_8))
            .array());
        GenericRecord record = getAvroRecord();
        BinaryEncoder encoder = encoderFactory.directBinaryEncoder(out, null);
        GenericDatumWriter<GenericRecord> writer = new GenericDatumWriter<>(MOCK_AVRO_SCHEMA);
        writer.write(record, encoder);
        encoder.flush();
        return out.toByteArray();
    }

    private GenericRecord getAvroRecord() {
        GenericRecord avroRecord = new GenericData.Record(MOCK_AVRO_SCHEMA);
        avroRecord.put("name", "arthur");
        avroRecord.put("favorite_number", 23);
        return avroRecord;
    }
}
