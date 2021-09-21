// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.avro;

import com.azure.core.util.serializer.TypeReference;
import com.azure.data.schemaregistry.SchemaRegistryAsyncClient;
import com.azure.data.schemaregistry.avro.generatedtestsources.HandOfCards;
import com.azure.data.schemaregistry.avro.generatedtestsources.PlayingCard;
import com.azure.data.schemaregistry.avro.generatedtestsources.PlayingCardSuit;
import com.azure.data.schemaregistry.models.SchemaProperties;
import com.azure.data.schemaregistry.models.SerializationType;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
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
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static com.azure.data.schemaregistry.avro.SchemaRegistryAvroSerializer.RECORD_FORMAT_INDICATOR_SIZE;
import static com.azure.data.schemaregistry.avro.SchemaRegistryAvroSerializer.SCHEMA_ID_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link SchemaRegistryAvroSerializer}.
 */
public class SchemaRegistryAvroSerializerTest {
    private static final String MOCK_GUID = new String(new char[SCHEMA_ID_SIZE]).replace("\0", "a");
    private static final String MOCK_AVRO_SCHEMA_STRING =
        "{\"namespace\":\"example2.avro\",\"type\":\"record\",\"name\":\"User\",\"fields\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"favorite_number\",\"type\": [\"int\", \"null\"]}]}";
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

    public static Stream<Arguments> getSchemaTypes() {
        final byte[] byteArray = new byte[] { 10, 3, 5};
        final ByteBuffer byteBuffer= ByteBuffer.wrap(byteArray);
        final Byte[] byteObjectArray = new Byte[] { 5, 10, 5, 2};

        return Stream.of(
            Arguments.of("foo", Schema.create(Schema.Type.STRING)),

            Arguments.of(byteArray, Schema.create(Schema.Type.BYTES)),
            Arguments.of(byteObjectArray, Schema.create(Schema.Type.BYTES)),
            Arguments.of(byteBuffer, Schema.create(Schema.Type.BYTES)),

            Arguments.of(Integer.valueOf("50"), Schema.create(Schema.Type.INT)),
            Arguments.of(51, Schema.create(Schema.Type.INT)),

            Arguments.of(Long.valueOf("10"), Schema.create(Schema.Type.LONG)),
            Arguments.of(15L, Schema.create(Schema.Type.LONG)),

            Arguments.of(Float.valueOf("24.4"), Schema.create(Schema.Type.FLOAT)),
            Arguments.of(52.1f, Schema.create(Schema.Type.FLOAT)),

            Arguments.of(Double.valueOf("24.4"), Schema.create(Schema.Type.DOUBLE)),
            Arguments.of(52.1d, Schema.create(Schema.Type.DOUBLE)),

            Arguments.of(Boolean.TRUE, Schema.create(Schema.Type.BOOLEAN)),
            Arguments.of(false, Schema.create(Schema.Type.BOOLEAN)),

            Arguments.of(null, Schema.create(Schema.Type.NULL))
        );
    }

    @MethodSource
    @ParameterizedTest
    public void getSchemaTypes(Object object, Schema expectedSchema) {
        // Act
        final Schema actual = AvroSerializer.getSchema(object);

        // Assert
        assertEquals(expectedSchema, actual);
    }

    @Test
    public void getSchemaTypeGenericRecord() {
        final String json = "{\n" +
            "   \"type\": \"record\",\n" +
            "   \"name\": \"Shoe\",\n" +
            "   \"namespace\": \"org.example.model\",\n" +
            "   \"fields\": [\n" +
            "      {\n" +
            "         \"name\": \"name\",\n" +
            "         \"type\": \"string\"\n" +
            "      },\n" +
            "      {\n" +
            "         \"name\": \"size\",\n" +
            "         \"type\": \"double\"\n" +
            "      },\n" +
            "      {\n" +
            "         \"name\": \"quantities\",\n" +
            "         \"type\": {\n" +
            "            \"type\": \"array\",\n" +
            "            \"items\": \"int\",\n" +
            "            \"java-class\": \"java.util.List\"\n" +
            "         }\n" +
            "      }\n" +
            "   ]\n" +
            "}";
        final Schema expectedSchema = new Schema.Parser().parse(json);
        final GenericRecord record = new GenericData.Record(expectedSchema);

        // Act
        final Schema actual = AvroSerializer.getSchema(record);

        // Assert
        assertEquals(expectedSchema, actual);
    }

    @Test
    public void getSchemaTypeSpecificRecord() {
        // Arrange
        final HandOfCards expected = HandOfCards.newBuilder().build();

        // Act
        final Schema actual = AvroSerializer.getSchema(expected);

        assertNotNull(actual);
        assertEquals(expected.getSchema(), actual);
        assertEquals(expected.getSchema().getType(), actual.getType());
    }

    @Test
    public void getSchemaTypeNotSupported() {
        // Arrange
        final Map<String, Object> testMap = new HashMap<>();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> AvroSerializer.getSchema(testMap));
    }

    @Test
    void testRegistryGuidPrefixedToPayload() throws IOException {
        // manually add SchemaRegistryObject into mock registry client cache
        final AvroSerializer avroSerializer = new AvroSerializer(false, mock(Schema.Parser.class),
            mock(EncoderFactory.class), mock(DecoderFactory.class));
        final PlayingCard playingCard = new PlayingCard(true, 10, PlayingCardSuit.DIAMONDS);
        final Schema playingClassSchema = PlayingCard.getClassSchema();
        final SchemaProperties registered = new SchemaProperties(MOCK_GUID, SerializationType.AVRO,
            playingClassSchema.getFullName(), playingClassSchema.toString().getBytes(StandardCharsets.UTF_8));

        when(client.getSchemaId(MOCK_SCHEMA_GROUP, registered.getSchemaName(), MOCK_AVRO_SCHEMA_STRING,
            SerializationType.AVRO)).thenReturn(Mono.just(MOCK_GUID));

        final SchemaRegistryAvroSerializer serializer = new SchemaRegistryAvroSerializer(client, avroSerializer,
            MOCK_SCHEMA_GROUP, false);

        try (ByteArrayOutputStream payload = new ByteArrayOutputStream()) {
            StepVerifier.create(serializer.serializeAsync(payload, playingCard))
                .verifyComplete();

            final ByteBuffer buffer = ByteBuffer.wrap(payload.toByteArray());
            buffer.get(new byte[RECORD_FORMAT_INDICATOR_SIZE]);

            final byte[] schemaGuidByteArray = new byte[SCHEMA_ID_SIZE];
            buffer.get(schemaGuidByteArray);

            // guid should match preloaded SchemaRegistryObject guid
            assertEquals(MOCK_GUID, new String(schemaGuidByteArray));
        } catch (RuntimeException e) {
            fail("Exception occurred", e);
        }
    }

    @Test
    void testNullPayloadThrowsSerializationException() {
        // Arrange
        AvroSerializer encoder = new AvroSerializer(false, parser, ENCODER_FACTORY,
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
        AvroSerializer encoder = new AvroSerializer(false, parser, ENCODER_FACTORY,
            DECODER_FACTORY);

        // Act & Assert
        assertThrows(NullPointerException.class,
            () -> new SchemaRegistryAvroSerializer(null, encoder, MOCK_SCHEMA_GROUP, false));
    }

    @Test
    void testAddUtils() throws IOException {
        // manually add SchemaRegistryObject to cache
        final AvroSerializer decoder = new AvroSerializer(false, parser, ENCODER_FACTORY,
            DECODER_FACTORY);
        final PlayingCard playingCard = new PlayingCard(true, 10, PlayingCardSuit.DIAMONDS);
        final Schema playingClassSchema = PlayingCard.getClassSchema();
        final SchemaProperties registered = new SchemaProperties(MOCK_GUID, SerializationType.AVRO,
            playingClassSchema.getFullName(), playingClassSchema.toString().getBytes(StandardCharsets.UTF_8));
        final SchemaRegistryAvroSerializer serializer = new SchemaRegistryAvroSerializer(client, decoder,
            MOCK_SCHEMA_GROUP, true);

        assertNotNull(registered.getSchema());

        when(client.getSchema(playingClassSchema.toString())).thenReturn(Mono.just(registered));

        StepVerifier.create(client.getSchema(MOCK_GUID))
            .assertNext(properties -> assertEquals(MOCK_GUID, properties.getSchemaId()))
            .verifyComplete();

        StepVerifier.create(serializer.deserializeAsync(new ByteArrayInputStream(getPayload(playingCard)),
            TypeReference.createInstance(GenericData.Record.class)))
            .assertNext(record -> assertEquals(CONSTANT_PAYLOAD, record.toString()))
            .verifyComplete();
    }

    @Test
    void testNullPayload() {
        SchemaRegistryAvroSerializer deserializer = new SchemaRegistryAvroSerializer(
            client, new AvroSerializer(false, parser, ENCODER_FACTORY, DECODER_FACTORY),
            MOCK_SCHEMA_GROUP, true);

        // Null payload should just complete the mono.
        StepVerifier.create(deserializer.deserializeAsync(null, null))
            .verifyComplete();
    }

    @Test
    void testNullPayloadSync() {
        SchemaRegistryAvroSerializer deserializer = new SchemaRegistryAvroSerializer(
            client, new AvroSerializer(false, parser, ENCODER_FACTORY, DECODER_FACTORY),
            MOCK_SCHEMA_GROUP, true);

        // Null payload should return null.
        final Object actual = deserializer.deserialize(null, null);

        // Assert
        assertNull(actual);
    }

    @Test
    void testIfTooShortPayloadThrow() throws IOException {
        SchemaRegistryAvroSerializer deserializer = new SchemaRegistryAvroSerializer(
            client, new AvroSerializer(false, parser, ENCODER_FACTORY, DECODER_FACTORY),
            MOCK_SCHEMA_GROUP, true);

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream("aaa".getBytes())) {
            StepVerifier.create(deserializer.deserializeAsync(inputStream, TypeReference.createInstance(String.class)))
                .verifyError(BufferUnderflowException.class);
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            outputStream.write(new byte[]{0x00, 0x00, 0x00, 0x00});
            outputStream.write("aa".getBytes());

            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray())) {
                StepVerifier.create(deserializer.deserializeAsync(inputStream,
                    TypeReference.createInstance(String.class)))
                    .verifyError(BufferUnderflowException.class);
            }
        }
    }

    private static byte[] getPayload(PlayingCard card) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            out.write(new byte[]{0x00, 0x00, 0x00, 0x00});
            out.write(ByteBuffer.allocate(SCHEMA_ID_SIZE)
                .put(MOCK_GUID.getBytes(StandardCharsets.UTF_8))
                .array());

            BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
            card.customEncode(encoder);

            return out.toByteArray();
        }
    }
}
