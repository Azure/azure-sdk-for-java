// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.data.schemaregistry.client.SchemaRegistryObject;
import com.azure.data.schemaregistry.client.SchemaRegistryClientException;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;

public class AbstractDataDeserializerTest {
    private static final String MOCK_GUID = new String(new char[AbstractDataSerDe.SCHEMA_ID_SIZE]).replace("\0", "a");
    private static final String MOCK_AVRO_SCHEMA_STRING = "{\"namespace\":\"example2.avro\",\"type\":\"record\",\"name\":\"User\",\"fields\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"favorite_number\",\"type\": [\"int\", \"null\"]}]}";

    private final EncoderFactory encoderFactory = EncoderFactory.get();
    private static final Schema MOCK_AVRO_SCHEMA = (new Schema.Parser()).parse(MOCK_AVRO_SCHEMA_STRING);

    @Test
    public void testLoadDecoder() throws IOException, SchemaRegistryClientException, SerializationException {
        // add standard avro decoder class and test that it is used for decoding payload
        SampleByteDecoder decoder = new SampleByteDecoder();

        // manually add SchemaRegistryObject to cache
        SchemaRegistryObject registered = new SchemaRegistryObject(MOCK_GUID,
            decoder.schemaType(),
            MOCK_AVRO_SCHEMA_STRING.getBytes(),
            decoder::parseSchemaString);

        assertTrue(registered.deserialize() != null);

        MockSchemaRegistryClient mockRegistryClient = new MockSchemaRegistryClient();
        mockRegistryClient.getGuidCache().put(MOCK_GUID, registered);
        TestDummyDeserializer deserializer = new TestDummyDeserializer(mockRegistryClient); // contains byte decoder

        assertEquals(MOCK_GUID, deserializer.schemaRegistryClient.getSchemaByGuid(MOCK_GUID).getSchemaId());
        assertEquals(SampleByteDecoder.CONSTANT_PAYLOAD, deserializer.deserialize(getPayload()));
    }

    @Test
    public void testNullPayload() throws IOException, SchemaRegistryClientException, SerializationException {
        TestDummyDeserializer deserializer = new TestDummyDeserializer(new MockSchemaRegistryClient());
        assertEquals(null, deserializer.deserialize(null));
    }

    @Test
    public void testIfTooShortPayloadThrow() {
        TestDummyDeserializer deserializer = new TestDummyDeserializer(new MockSchemaRegistryClient());

        try {
            deserializer.deserialize("bad payload".getBytes());
            fail("Too short payload did not throw SerializationException");
        } catch (SerializationException e) {
            assertTrue(true);
        }
    }

    // TODO: add for non-existing guid

    @Test
    public void testIfRegistryClientNullOnBuildThrow() {
        try {
            TestDummyDeserializer deserializer = new TestDummyDeserializer(null);
            fail("should not get here.");
        } catch (IllegalArgumentException e) {
            // good
        }
    }

    private byte[] getPayload() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(ByteBuffer.allocate(AbstractDataSerDe.SCHEMA_ID_SIZE)
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
