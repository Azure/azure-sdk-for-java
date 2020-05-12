/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.azure.schemaregistry;

import com.azure.schemaregistry.client.MockSchemaRegistryClient;
import com.azure.schemaregistry.client.SchemaRegistryObject;
import com.azure.schemaregistry.client.SchemaRegistryClientException;
import com.azure.schemaregistry.client.rest.RestService;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class AbstractDataDeserializerTest extends TestCase {
    private static final String MOCK_GUID = new String(new char[AbstractDataSerDe.idSize]).replace("\0", "a");
    private static final String MOCK_AVRO_SCHEMA_STRING = "{\"namespace\":\"example2.avro\",\"type\":\"record\",\"name\":\"User\",\"fields\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"favorite_number\",\"type\": [\"int\", \"null\"]}]}";

    private final EncoderFactory encoderFactory = EncoderFactory.get();
    private final Schema MOCK_AVRO_SCHEMA;

    public AbstractDataDeserializerTest(String testName) {
        super(testName);
        this.MOCK_AVRO_SCHEMA = (new Schema.Parser()).parse(MOCK_AVRO_SCHEMA_STRING);
    }

    public static Test suite() {
        return new TestSuite(AbstractDataDeserializerTest.class);
    }

    public void testLoadDecoder() throws IOException, SchemaRegistryClientException, SerializationException {
        // add standard avro decoder class and test that it is used for decoding payload
        SampleByteDecoder decoder = new SampleByteDecoder();

        // manually add SchemaRegistryObject to cache
        SchemaRegistryObject<Schema> registered = new SchemaRegistryObject<>(MOCK_GUID,
            decoder.serializationFormat(),
            MOCK_AVRO_SCHEMA_STRING.getBytes(RestService.SERVICE_CHARSET),
            s -> decoder.parseSchemaString(s));

        assertTrue(registered.deserialize() != null);

        MockSchemaRegistryClient mockRegistryClient = new MockSchemaRegistryClient();
        mockRegistryClient.guidCache.put(MOCK_GUID, registered);
        TestDummyDeserializer deserializer = new TestDummyDeserializer.Builder(mockRegistryClient)
                .byteDecoder(new SampleByteDecoder())
                .build();

        assertEquals(MOCK_GUID, deserializer.schemaRegistryClient.getSchemaByGuid(MOCK_GUID).schemaGuid);
        assertEquals(decoder.samplePayload, deserializer.deserialize(getPayload()));
    }

    public void testNullPayload() throws IOException, SchemaRegistryClientException, SerializationException {
        TestDummyDeserializer deserializer = new TestDummyDeserializer.Builder(new MockSchemaRegistryClient())
                .build();

        assertEquals(null, deserializer.deserialize(null));
    }

    public void testIfTooShortPayloadThrow() {
        TestDummyDeserializer deserializer = new TestDummyDeserializer.Builder(new MockSchemaRegistryClient())
                .build();

        try {
            deserializer.deserialize("bad payload".getBytes());
            fail("Too short payload did not throw SerializationException");
        } catch (SerializationException e) {
            assertTrue(true);
        }
    }

    // TODO: add for non-existing guid

    // builder tests
    public void testBuilderIfRegistryNullOnBuildThrow() {
        try {
            TestDummyDeserializer deserializer = new TestDummyDeserializer.Builder(null).build();
            assert(deserializer == null);
            fail("should not get here.");
        } catch (IllegalArgumentException e) {
            // good
        } catch (Exception e) {
            fail("Building deserializer with null registry should fail with IllegalArgumentException");
        }
    }

    private byte[] getPayload() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(ByteBuffer.allocate(AbstractDataSerDe.idSize)
                            .put(MOCK_GUID.getBytes(Charset.forName("UTF-8")))
                            .array());
        GenericRecord record = getAvroRecord();
        BinaryEncoder encoder = encoderFactory.directBinaryEncoder(out, null);
        GenericDatumWriter<GenericRecord> writer = new GenericDatumWriter<>(MOCK_AVRO_SCHEMA);
        writer.write(record, encoder);
        encoder.flush();
        byte[] bytes = out.toByteArray();
        return bytes;
    }


    private GenericRecord getAvroRecord() {
        GenericRecord avroRecord = new GenericData.Record(MOCK_AVRO_SCHEMA);
        avroRecord.put("name", "arthur");
        avroRecord.put("favorite_number", 23);
        return avroRecord;
    }
}
