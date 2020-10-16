// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.avro;

import com.azure.core.util.serializer.TypeReference;
import com.azure.data.schemaregistry.SchemaRegistryAsyncClient;
import com.azure.data.schemaregistry.models.SchemaProperties;
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
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static com.azure.data.schemaregistry.avro.SchemaRegistryAvroSerializer.RECORD_FORMAT_INDICATOR_SIZE;
import static com.azure.data.schemaregistry.avro.SchemaRegistryAvroSerializer.SCHEMA_ID_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link SchemaRegistryAvroSerializer}.
 */
public class SchemaRegistryAvroSerializerTest {
    private static final String MOCK_GUID = new String(new char[SCHEMA_ID_SIZE]).replace("\0", "a");
    private static final String MOCK_AVRO_SCHEMA_STRING =
        "{\"namespace\":\"example2.avro\",\"type\":\"record\",\"name\":\"User\",\"fields\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"favorite_number\",\"type\": [\"int\", \"null\"]}]}";
    private final EncoderFactory encoderFactory = EncoderFactory.get();
    private static final Schema MOCK_AVRO_SCHEMA = (new Schema.Parser()).parse(MOCK_AVRO_SCHEMA_STRING);
    private static final String MOCK_SCHEMA_GROUP = "mock-group";
    private static final String CONSTANT_PAYLOAD = "{\"name\": \"arthur\", \"favorite_number\": 23}";

    @Test
    void testRegistryGuidPrefixedToPayload() {
        // manually add SchemaRegistryObject into mock registry client cache
        AvroSchemaRegistryUtils encoder = new AvroSchemaRegistryUtils(false);
        SchemaProperties registered = new SchemaProperties(MOCK_GUID,
            encoder.getSerializationType(),
            encoder.getSchemaName(null),
            encoder.getSchemaString(null).getBytes());

        assertEquals(encoder.getSchemaString(null), new String(registered.getSchema()));

        SchemaRegistryAsyncClient mockRegistryClient = getMockClient();
        Mockito.when(mockRegistryClient.getSchemaId(anyString(), anyString(), anyString(),
            any(SerializationType.class)))
            .thenReturn(Mono.just(MOCK_GUID));

        SchemaRegistryAvroSerializer serializer = new SchemaRegistryAvroSerializer(
            mockRegistryClient, encoder, MOCK_SCHEMA_GROUP, false);

        try {
            ByteArrayOutputStream payload = new ByteArrayOutputStream();
            serializer.serializeAsync(payload, 1).block();
            ByteBuffer buffer = ByteBuffer.wrap(payload.toByteArray());
            buffer.get(new byte[RECORD_FORMAT_INDICATOR_SIZE]);
            byte[] schemaGuidByteArray = new byte[SCHEMA_ID_SIZE];
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
        SchemaRegistryAsyncClient mockRegistryClient = getMockClient();
        AvroSchemaRegistryUtils encoder = new AvroSchemaRegistryUtils(false);
        SchemaRegistryAvroSerializer serializer = new SchemaRegistryAvroSerializer(
            mockRegistryClient, encoder, MOCK_SCHEMA_GROUP, false);

        StepVerifier.create(serializer.serializeAsync(new ByteArrayOutputStream(), null))
            .verifyError(NullPointerException.class);
    }


    @Test
    void testIfRegistryNullThenThrow() {
        try {
            AvroSchemaRegistryUtils encoder = new AvroSchemaRegistryUtils(false);
            SchemaRegistryAvroSerializer serializer = new SchemaRegistryAvroSerializer(
                null, encoder, MOCK_SCHEMA_GROUP, false);
            fail("Building serializer instance with null registry client failed to throw");
        } catch (NullPointerException e) {
            assertTrue(true);
        } catch (Exception e) {
            fail("Building serializer instance with null registry client should throw null pointer exception");
        }
    }

    @Test
    void testAddUtils() throws IOException {

        // manually add SchemaRegistryObject to cache
        AvroSchemaRegistryUtils decoder = new AvroSchemaRegistryUtils(false);

        SchemaProperties registered = new SchemaProperties(MOCK_GUID,
            decoder.getSerializationType(),
            decoder.getSchemaName(null),
            MOCK_AVRO_SCHEMA_STRING.getBytes());

        assertNotNull(registered.getSchema());

        SchemaRegistryAsyncClient mockClient = getMockClient();
        Mockito.when(mockClient.getSchema(anyString()))
            .thenReturn(Mono.just(registered));

        SchemaRegistryAvroSerializer serializer = new SchemaRegistryAvroSerializer(
            mockClient, decoder, MOCK_SCHEMA_GROUP, true);

        StepVerifier.create(mockClient.getSchema(MOCK_GUID))
            .assertNext(properties -> assertEquals(MOCK_GUID, properties.getSchemaId()))
            .verifyComplete();

        StepVerifier.create(serializer.deserializeAsync(new ByteArrayInputStream(getPayload()),
            TypeReference.createInstance(GenericData.Record.class)))
            .assertNext(record -> assertEquals(CONSTANT_PAYLOAD, record.toString()))
            .verifyComplete();
    }

    @Test
    void testNullPayload() {
        SchemaRegistryAvroSerializer deserializer = new SchemaRegistryAvroSerializer(
            getMockClient(), new AvroSchemaRegistryUtils(false), MOCK_SCHEMA_GROUP, true);

        StepVerifier.create(deserializer.deserializeAsync(null, null))
            .verifyComplete();
    }

    @Test
    void testIfTooShortPayloadThrow() {
        SchemaRegistryAvroSerializer deserializer = new SchemaRegistryAvroSerializer(
            getMockClient(), new AvroSchemaRegistryUtils(false), MOCK_SCHEMA_GROUP, true);

        StepVerifier.create(deserializer.deserializeAsync(new ByteArrayInputStream("aaa".getBytes()),
            TypeReference.createInstance(String.class)))
            .verifyError(BufferUnderflowException.class);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            out.write(new byte[]{0x00, 0x00, 0x00, 0x00});
            out.write("aa".getBytes());
        } catch (IOException e) {
            fail();
        }

        ByteArrayInputStream stream = new ByteArrayInputStream(out.toByteArray());

        StepVerifier.create(deserializer.deserializeAsync(stream, TypeReference.createInstance(String.class)))
            .verifyError(BufferUnderflowException.class);
    }

    private SchemaRegistryAsyncClient getMockClient() {
        return mock(SchemaRegistryAsyncClient.class);
    }

    private byte[] getPayload() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(new byte[]{0x00, 0x00, 0x00, 0x00});
        out.write(ByteBuffer.allocate(SCHEMA_ID_SIZE)
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
