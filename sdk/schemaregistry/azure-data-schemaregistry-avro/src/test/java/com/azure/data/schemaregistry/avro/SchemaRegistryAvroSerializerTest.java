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
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static com.azure.data.schemaregistry.avro.SchemaRegistryAvroSerializer.RECORD_FORMAT_INDICATOR_SIZE;
import static com.azure.data.schemaregistry.avro.SchemaRegistryAvroSerializer.SCHEMA_ID_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * Unit tests for {@link SchemaRegistryAvroSerializer}.
 */
public class SchemaRegistryAvroSerializerTest {
    private static final String MOCK_GUID = new String(new char[SCHEMA_ID_SIZE]).replace("\0", "a");
    private static final String MOCK_AVRO_SCHEMA_STRING =
        "{\"namespace\":\"example2.avro\",\"type\":\"record\",\"name\":\"User\",\"fields\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"favorite_number\",\"type\": [\"int\", \"null\"]}]}";
    private static final Schema MOCK_AVRO_SCHEMA = (new Schema.Parser()).parse(MOCK_AVRO_SCHEMA_STRING);
    private static final String MOCK_SCHEMA_GROUP = "mock-group";
    private static final String CONSTANT_PAYLOAD = "{\"name\": \"arthur\", \"favorite_number\": 23}";
    private static final DecoderFactory DECODER_FACTORY = DecoderFactory.get();
    private static final EncoderFactory ENCODER_FACTORY = EncoderFactory.get();

    private Schema.Parser parser;
    private AutoCloseable mocksCloseable;

    @Mock
    private SchemaRegistryAsyncClient client;

    @BeforeAll
    public static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));
    }

    @AfterAll
    public static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @BeforeEach
    public void beforeEach() {
        mocksCloseable = MockitoAnnotations.openMocks(this);
        parser = new Schema.Parser();
    }

    @AfterEach
    public void afterEach() throws Exception {
        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
    }

    @Test
    void testRegistryGuidPrefixedToPayload() throws IOException {
        // manually add SchemaRegistryObject into mock registry client cache
        AvroSchemaRegistryUtils encoder = new AvroSchemaRegistryUtils(false, parser, ENCODER_FACTORY,
            DECODER_FACTORY);

        SchemaProperties registered = new SchemaProperties(MOCK_GUID,
            SerializationType.AVRO,
            encoder.getSchemaName(null),
            encoder.getSchemaString(null).getBytes());

        assertEquals(encoder.getSchemaString(null), new String(registered.getSchema()));

        Mockito.when(client.getSchemaId(anyString(), anyString(), anyString(),
            any(SerializationType.class)))
            .thenReturn(Mono.just(MOCK_GUID));

        SchemaRegistryAvroSerializer serializer = new SchemaRegistryAvroSerializer(
            client, encoder, MOCK_SCHEMA_GROUP, false);

        try (ByteArrayOutputStream payload = new ByteArrayOutputStream()) {

            StepVerifier.create(serializer.serializeAsync(payload, 1))
                .verifyComplete();

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
        // Arrange
        AvroSchemaRegistryUtils encoder = new AvroSchemaRegistryUtils(false, parser, ENCODER_FACTORY,
            DECODER_FACTORY);
        SchemaRegistryAvroSerializer serializer = new SchemaRegistryAvroSerializer(
            client, encoder, MOCK_SCHEMA_GROUP, false);

        // Act & Assert
        StepVerifier.create(serializer.serializeAsync(new ByteArrayOutputStream(), null))
            .verifyError(NullPointerException.class);
    }

    @Test
    void testIfRegistryNullThenThrow() {
        // Arrange
        AvroSchemaRegistryUtils encoder = new AvroSchemaRegistryUtils(false, parser, ENCODER_FACTORY,
            DECODER_FACTORY);

        // Act & Assert
        assertThrows(NullPointerException.class,
            () -> new SchemaRegistryAvroSerializer(null, encoder, MOCK_SCHEMA_GROUP, false));
    }

    @Test
    void testAddUtils() throws IOException {
        // manually add SchemaRegistryObject to cache
        AvroSchemaRegistryUtils decoder = new AvroSchemaRegistryUtils(false, parser, ENCODER_FACTORY,
            DECODER_FACTORY);

        SchemaProperties registered = new SchemaProperties(MOCK_GUID,
            SerializationType.AVRO,
            decoder.getSchemaName(null),
            MOCK_AVRO_SCHEMA_STRING.getBytes());

        assertNotNull(registered.getSchema());

        Mockito.when(client.getSchema(anyString()))
            .thenReturn(Mono.just(registered));

        SchemaRegistryAvroSerializer serializer = new SchemaRegistryAvroSerializer(
            client, decoder, MOCK_SCHEMA_GROUP, true);

        StepVerifier.create(client.getSchema(MOCK_GUID))
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
            client, new AvroSchemaRegistryUtils(false, parser, ENCODER_FACTORY, DECODER_FACTORY),
            MOCK_SCHEMA_GROUP, true);

        // Null payload should just complete the mono.
        StepVerifier.create(deserializer.deserializeAsync(null, null))
            .verifyComplete();
    }

    @Test
    void testNullPayloadSync() {
        SchemaRegistryAvroSerializer deserializer = new SchemaRegistryAvroSerializer(
            client, new AvroSchemaRegistryUtils(false, parser, ENCODER_FACTORY, DECODER_FACTORY),
            MOCK_SCHEMA_GROUP, true);

        // Null payload should return null.
        final Object actual = deserializer.deserialize(null, null);

        // Assert
        assertNull(actual);
    }

    @Test
    void testIfTooShortPayloadThrow() throws IOException {
        SchemaRegistryAvroSerializer deserializer = new SchemaRegistryAvroSerializer(
            client, new AvroSchemaRegistryUtils(false, parser, ENCODER_FACTORY, DECODER_FACTORY),
            MOCK_SCHEMA_GROUP, true);

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream("aaa".getBytes())) {
            StepVerifier.create(deserializer.deserializeAsync(inputStream, TypeReference.createInstance(String.class)))
                .verifyError(BufferUnderflowException.class);
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            outputStream.write(new byte[]{0x00, 0x00, 0x00, 0x00});
            outputStream.write("aa".getBytes());

            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray())){
                StepVerifier.create(deserializer.deserializeAsync(inputStream,
                    TypeReference.createInstance(String.class)))
                    .verifyError(BufferUnderflowException.class);
            }
        }
    }

    private static byte[] getPayload() throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            out.write(new byte[]{0x00, 0x00, 0x00, 0x00});
            out.write(ByteBuffer.allocate(SCHEMA_ID_SIZE)
                .put(MOCK_GUID.getBytes(StandardCharsets.UTF_8))
                .array());

            GenericRecord record = getAvroRecord();
            BinaryEncoder encoder = ENCODER_FACTORY.directBinaryEncoder(out, null);
            GenericDatumWriter<GenericRecord> writer = new GenericDatumWriter<>(MOCK_AVRO_SCHEMA);
            writer.write(record, encoder);
            encoder.flush();

            return out.toByteArray();
        }
    }

    private static GenericRecord getAvroRecord() {
        GenericRecord avroRecord = new GenericData.Record(MOCK_AVRO_SCHEMA);
        avroRecord.put("name", "arthur");
        avroRecord.put("favorite_number", 23);
        return avroRecord;
    }
}
