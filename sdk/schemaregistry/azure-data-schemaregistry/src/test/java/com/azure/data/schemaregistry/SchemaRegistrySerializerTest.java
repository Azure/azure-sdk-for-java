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

public class SchemaRegistrySerializerTest {
    private static final String MOCK_GUID = new String(new char[SchemaRegistrySerializer.SCHEMA_ID_SIZE]).replace("\0", "a");
    private static final String MOCK_AVRO_SCHEMA_STRING = "{\"namespace\":\"example2.avro\",\"type\":\"record\",\"name\":\"User\",\"fields\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"favorite_number\",\"type\": [\"int\", \"null\"]}]}";
    private final EncoderFactory encoderFactory = EncoderFactory.get();
    private static final Schema MOCK_AVRO_SCHEMA = (new Schema.Parser()).parse(MOCK_AVRO_SCHEMA_STRING);

    @Test
    public void testRegistryGuidPrefixedToPayload() {
        // manually add SchemaRegistryObject into mock registry client cache
        SampleSchemaRegistryCodec encoder = new SampleSchemaRegistryCodec();
        SchemaRegistryObject registered = new SchemaRegistryObject(MOCK_GUID,
            encoder.getSerializationType(),
            encoder.getSchemaName(null),
            encoder.getSchemaGroup(),
            encoder.getSchemaString(null).getBytes(), // always returns same schema string
            encoder::parseSchemaString);

        assertEquals(encoder.getSchemaString(null), registered.getSchema());

        SchemaRegistryAsyncClient mockRegistryClient = getMockClient();
        Mockito.when(mockRegistryClient.getSchemaId(anyString(), anyString(), anyString(),
            any(SerializationType.class)))
            .thenReturn(Mono.just(encoder.getSchemaString(null)));

        TestDummySerializer serializer = new TestDummySerializer(
            mockRegistryClient, false);

        try {
            ByteArrayOutputStream payload = serializer.serializeAsync(new ByteArrayOutputStream(), 1).block();
            ByteBuffer buffer = ByteBuffer.wrap(payload.toByteArray());
            byte[] schemaGuidByteArray = new byte[SchemaRegistrySerializer.SCHEMA_ID_SIZE];
            buffer.get(schemaGuidByteArray);

            // guid should match preloaded SchemaRegistryObject guid
            assertEquals(MOCK_GUID, new String(schemaGuidByteArray));

            int start = buffer.position() + buffer.arrayOffset();
            int length = buffer.limit() - SchemaRegistrySerializer.SCHEMA_ID_SIZE;
            byte[] encodedBytes = Arrays.copyOfRange(buffer.array(), start, start + length);
            assertTrue(Arrays.equals(encoder.encode(null), encodedBytes));
        } catch (RuntimeException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testNullPayloadThrowsSerializationException() {
        TestDummySerializer serializer = new TestDummySerializer(
            getMockClient(),
            false);

        try {
            serializer.serializeAsync(new ByteArrayOutputStream(), null).block();
            fail("Serializing null payload failed to throw NullPointerException");
        } catch (NullPointerException e) {
            assertTrue(true);
        }
    }


    @Test
    public void testIfRegistryNullThenThrow() {
        try {
            TestDummySerializer serializer = new TestDummySerializer(
                null, false);
            fail("Building serializer instance with null registry client failed to throw");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } catch (Exception e) {
            fail("Building serializer instance with null registry client should throw illegal argument exception");
        }
    }

    @Test
    public void testAddDeserializerCodec() throws IOException {
        // add sample codec impl and test that it is used for decoding payload
        SampleSchemaRegistryCodec decoder = new SampleSchemaRegistryCodec();

        // manually add SchemaRegistryObject to cache
        SchemaRegistryObject registered = new SchemaRegistryObject(MOCK_GUID,
            decoder.getSerializationType(),
            decoder.getSchemaName(null),
            decoder.getSchemaGroup(),
            MOCK_AVRO_SCHEMA_STRING.getBytes(),
            decoder::parseSchemaString);

        assertTrue(registered.getSchema() != null);

        SchemaRegistryAsyncClient mockClient = getMockClient();
        Mockito.when(mockClient.getSchema(anyString()))
            .thenReturn(Mono.just(registered));

        // constructor loads deserializer codec
        TestDummySerializer serializer = new TestDummySerializer(mockClient, true);

        assertEquals(MOCK_GUID,
            serializer.schemaRegistryClient.getSchema(MOCK_GUID).block().getSchemaId());

            serializer.deserializeAsync(new ByteArrayInputStream(getPayload()))
                .subscribe(unused -> {
                        System.out.println(unused);
                    },
                    ex -> System.out.println(ex));

        assertEquals(SampleSchemaRegistryCodec.CONSTANT_PAYLOAD,
            serializer.deserializeAsync(new ByteArrayInputStream(getPayload())).block());
    }

    @Test
    public void testNullPayload() {
        TestDummySerializer deserializer = new TestDummySerializer(
            getMockClient(), true);
        assertNull(deserializer.deserializeAsync(null).block());
    }

    @Test
    public void testIfTooShortPayloadThrow() {
        TestDummySerializer serializer = new TestDummySerializer(
            getMockClient(), true);

        try {
            serializer.deserializeAsync(new ByteArrayInputStream("bad payload".getBytes())).block();
            fail("Too short payload did not throw BufferUnderflowException");
        } catch (BufferUnderflowException e) {
            assertTrue(true);
        }
    }

    // TODO: add for non-existing guid

    @Test
    public void testIfRegistryClientNullOnBuildThrow() {
        try {
            TestDummySerializer deserializer = new TestDummySerializer(null, true);
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
