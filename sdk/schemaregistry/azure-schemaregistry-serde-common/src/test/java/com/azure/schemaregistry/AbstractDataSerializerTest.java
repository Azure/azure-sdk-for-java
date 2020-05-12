/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.azure.schemaregistry;

import com.azure.schemaregistry.client.MockSchemaRegistryClient;
import com.azure.schemaregistry.client.SchemaRegistryObject;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;

public class AbstractDataSerializerTest extends TestCase {
    public AbstractDataSerializerTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(AbstractDataSerializerTest.class);
    }

    public void testRegistryGuidPrefixedToPayload() {
        Random rnd = new Random();
        String MOCK_GUID = "";
        for (int i = 0; i < AbstractDataSerDe.idSize; i++) {
            MOCK_GUID += (char)(rnd.nextInt(26) + 'a');
        }

        // manually add SchemaRegistryObject into mock registry client cache
        SampleByteEncoder encoder = new SampleByteEncoder();
        SchemaRegistryObject<String> registered = new SchemaRegistryObject<>(MOCK_GUID,
                encoder.serializationFormat(),
                encoder.getSchemaString(null).getBytes(), // always returns same schema string
                s -> encoder.parseSchemaString(s));

        assertEquals(encoder.getSchemaString(null), registered.deserialize());

        MockSchemaRegistryClient mockRegistryClient = new MockSchemaRegistryClient();
        mockRegistryClient.schemaStringCache.put(encoder.getSchemaString(null), registered);

        TestDummySerializer serializer = new TestDummySerializer.Builder(mockRegistryClient)
                .byteEncoder(encoder)
                .build();

        try {
            byte[] payload = serializer.serializeImpl(1);
            ByteBuffer buffer = ByteBuffer.wrap(payload);
            byte[] schemaGuidByteArray = new byte[AbstractDataSerDe.idSize];
            try {
                buffer.get(schemaGuidByteArray);
            } catch (BufferUnderflowException e) {
                throw new SerializationException("Payload too short, no readable guid.", e);
            }

            assertEquals(MOCK_GUID, new String(schemaGuidByteArray)); // guid should match preloaded SchemaRegistryObject guid

            int start = buffer.position() + buffer.arrayOffset();
            int length = buffer.limit() - AbstractDataSerDe.idSize;
            byte[] encodedBytes = Arrays.copyOfRange(buffer.array(), start, start + length);
            assertTrue(Arrays.equals(encoder.encode(null).toByteArray(), encodedBytes));
        } catch (SerializationException e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testNullPayloadThrowsSerializationException() {
        TestDummySerializer serializer = new TestDummySerializer.Builder(new MockSchemaRegistryClient())
                .byteEncoder(new SampleByteEncoder())
                .build();

        try {
            serializer.serializeImpl(null);
            fail("Serializing null payload failed to throw SerializationException");
        } catch (SerializationException e) {
            assertTrue(true);
        }
    }

    public void testSerializeWithNullByteEncoderThrows() {
        // don't set byte encoder on constructor
        TestDummySerializer serializer = new TestDummySerializer.Builder(new MockSchemaRegistryClient())
                .constructWithoutByteEncoder()
                .build();

        try {
            serializer.serializeImpl(1);
        } catch (SerializationException e) {
            assert(true);
        }
    }

    public void testBuilderIfRegistryNullThrow() {
        try {
            TestDummySerializer serializer = new TestDummySerializer.Builder(null).build();
            fail("Building serializer instance with null registry client failed to throw");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } catch (Exception e) {
            fail("Building serializer instance with null registry client should throw illegal argument exception");
        }
    }

    public void testBuilderAutoRegister() {
        TestDummySerializer serializer = new TestDummySerializer.Builder(new MockSchemaRegistryClient())
                .constructWithoutByteEncoder() // doesn't matter
                .build();
        assertEquals(false, (boolean) serializer.autoRegisterSchemas);

        serializer = new TestDummySerializer.Builder(new MockSchemaRegistryClient())
                .constructWithoutByteEncoder() // doesn't matter
                .autoRegisterSchemas(true)
                .build();
        assertEquals(true, (boolean) serializer.autoRegisterSchemas);
    }
}
