// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.avro;

import com.azure.core.util.serializer.TypeReference;
import com.azure.data.schemaregistry.SchemaRegistryAsyncClient;
import com.azure.data.schemaregistry.avro.generatedtestsources.PlayingCard;
import com.azure.data.schemaregistry.avro.generatedtestsources.PlayingCardSuit;
import com.azure.data.schemaregistry.models.SchemaProperties;
import com.azure.data.schemaregistry.models.SerializationFormat;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.message.RawMessageEncoder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import static com.azure.data.schemaregistry.avro.SchemaRegistryAvroSerializer.RECORD_FORMAT_INDICATOR;
import static com.azure.data.schemaregistry.avro.SchemaRegistryAvroSerializer.RECORD_FORMAT_INDICATOR_SIZE;
import static com.azure.data.schemaregistry.avro.SchemaRegistryAvroSerializer.SCHEMA_ID_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link SchemaRegistryAvroSerializer}.
 */
public class SchemaRegistryAvroSerializerTest {
    private static final String MOCK_GUID = new String(new char[SCHEMA_ID_SIZE]).replace("\0", "a");
    private static final String MOCK_SCHEMA_GROUP = "mock-group";
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
        final AvroSerializer avroSerializer = new AvroSerializer(false, new Schema.Parser(),
            ENCODER_FACTORY, DECODER_FACTORY);
        final PlayingCard playingCard = new PlayingCard(true, 10, PlayingCardSuit.DIAMONDS);
        final Schema playingClassSchema = PlayingCard.getClassSchema();
        final byte[] schemaBytes = playingClassSchema.toString().getBytes(StandardCharsets.UTF_8);
        final SchemaProperties registered = new SchemaProperties(MOCK_GUID, SerializationFormat.AVRO,
            playingClassSchema.getFullName(), schemaBytes);

        when(client.getSchemaId(MOCK_SCHEMA_GROUP, registered.getSchemaName(), playingClassSchema.toString(),
            SerializationFormat.AVRO)).thenReturn(Mono.just(MOCK_GUID));

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
    void testGetSchemaAndDeserialize() throws IOException {
        // manually add SchemaRegistryObject to cache
        final AvroSerializer decoder = new AvroSerializer(false, parser, ENCODER_FACTORY,
            DECODER_FACTORY);
        final PlayingCard playingCard = new PlayingCard(true, 10, PlayingCardSuit.DIAMONDS);
        final Schema playingClassSchema = PlayingCard.getClassSchema();
        final SchemaProperties registered = new SchemaProperties(MOCK_GUID, SerializationFormat.AVRO,
            playingClassSchema.getFullName(), playingClassSchema.toString().getBytes(StandardCharsets.UTF_8));
        final SchemaRegistryAvroSerializer serializer = new SchemaRegistryAvroSerializer(client, decoder,
            MOCK_SCHEMA_GROUP, true);

        assertNotNull(registered.getSchema());

        when(client.getSchema(MOCK_GUID)).thenReturn(Mono.just(registered));

        StepVerifier.create(client.getSchema(MOCK_GUID))
            .assertNext(properties -> assertEquals(MOCK_GUID, properties.getSchemaId()))
            .verifyComplete();

        final byte[] serializedPayload = getPayload(playingCard);

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(serializedPayload)) {
            StepVerifier.create(serializer.deserializeAsync(inputStream, TypeReference.createInstance(PlayingCard.class)))
                .assertNext(actual -> {
                    assertEquals(playingCard.getPlayingCardSuit(), actual.getPlayingCardSuit());
                    assertEquals(playingCard.getCardValue(), actual.getCardValue());
                    assertEquals(playingCard.getIsFaceCard(), actual.getIsFaceCard());
                })
                .verifyComplete();
        }
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
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            outputStream.write(RECORD_FORMAT_INDICATOR);
            outputStream.write(MOCK_GUID.getBytes(StandardCharsets.UTF_8));

            final RawMessageEncoder<PlayingCard> encoder = new RawMessageEncoder<>(new GenericData(),
                PlayingCard.getClassSchema());

            encoder.encode(card, outputStream);

            return outputStream.toByteArray();
        }
    }
}
