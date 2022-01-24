// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.apacheavro;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.InterceptorManager;
import com.azure.core.test.TestContextManager;
import com.azure.core.test.TestMode;
import com.azure.core.util.serializer.TypeReference;
import com.azure.data.schemaregistry.SchemaRegistryAsyncClient;
import com.azure.data.schemaregistry.SchemaRegistryClientBuilder;
import com.azure.data.schemaregistry.apacheavro.generatedtestsources.PlayingCard;
import com.azure.data.schemaregistry.apacheavro.generatedtestsources.PlayingCardSuit;
import com.azure.data.schemaregistry.models.SchemaFormat;
import com.azure.data.schemaregistry.models.SchemaProperties;
import com.azure.data.schemaregistry.models.SchemaRegistrySchema;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.message.RawMessageEncoder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;

import static com.azure.data.schemaregistry.apacheavro.SchemaRegistryApacheAvroSerializer.RECORD_FORMAT_INDICATOR;
import static com.azure.data.schemaregistry.apacheavro.SchemaRegistryApacheAvroSerializer.RECORD_FORMAT_INDICATOR_SIZE;
import static com.azure.data.schemaregistry.apacheavro.SchemaRegistryApacheAvroSerializer.SCHEMA_ID_SIZE;
import static com.azure.data.schemaregistry.apacheavro.SchemaRegistryApacheAvroSerializerIntegrationTest.PLAYBACK_ENDPOINT;
import static com.azure.data.schemaregistry.apacheavro.SchemaRegistryApacheAvroSerializerIntegrationTest.PLAYBACK_TEST_GROUP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link SchemaRegistryApacheAvroSerializer}.
 */
public class SchemaRegistryApacheAvroSerializerTest {
    private static final String MOCK_GUID = new String(new char[SCHEMA_ID_SIZE]).replace("\0", "a");
    private static final String MOCK_SCHEMA_GROUP = "mock-group";
    private static final int MOCK_CACHE_SIZE = 128;
    private static final DecoderFactory DECODER_FACTORY = DecoderFactory.get();
    private static final EncoderFactory ENCODER_FACTORY = EncoderFactory.get();

    private Schema.Parser parser;
    private AutoCloseable mocksCloseable;
    private TestInfo testInfo;

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
    public void beforeEach(TestInfo testInfo) {
        if (!testInfo.getTestMethod().isPresent()) {
            throw new IllegalStateException(
                "Expected testInfo.getTestMethod() not be empty since we need a method for TestContextManager.");
        }

        this.testInfo = testInfo;
        this.mocksCloseable = MockitoAnnotations.openMocks(this);
        this.parser = new Schema.Parser();
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
        final SchemaProperties registered = new SchemaProperties(MOCK_GUID, SchemaFormat.AVRO);
        final SerializerOptions serializerOptions = new SerializerOptions(MOCK_SCHEMA_GROUP, false, MOCK_CACHE_SIZE);

        when(client.getSchemaProperties(MOCK_SCHEMA_GROUP, playingClassSchema.getFullName(),
            playingClassSchema.toString(), SchemaFormat.AVRO)).thenReturn(Mono.just(registered));

        final SchemaRegistryApacheAvroSerializer serializer = new SchemaRegistryApacheAvroSerializer(client,
            avroSerializer, serializerOptions);

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
        final AvroSerializer encoder = new AvroSerializer(false, parser, ENCODER_FACTORY,
            DECODER_FACTORY);
        final SerializerOptions serializerOptions = new SerializerOptions(MOCK_SCHEMA_GROUP, false, MOCK_CACHE_SIZE);

        SchemaRegistryApacheAvroSerializer serializer = new SchemaRegistryApacheAvroSerializer(
            client, encoder, serializerOptions);

        // Act & Assert
        StepVerifier.create(serializer.serializeAsync(new ByteArrayOutputStream(), null))
            .verifyError(NullPointerException.class);
    }

    @Test
    void testIfRegistryNullThenThrow() {
        // Arrange
        AvroSerializer encoder = new AvroSerializer(false, parser, ENCODER_FACTORY,
            DECODER_FACTORY);
        final SerializerOptions serializerOptions = new SerializerOptions(MOCK_SCHEMA_GROUP, false, MOCK_CACHE_SIZE);

        // Act & Assert
        assertThrows(NullPointerException.class,
            () -> new SchemaRegistryApacheAvroSerializer(null, encoder, serializerOptions));
    }

    @Test
    void testGetSchemaAndDeserialize() throws IOException {
        // manually add SchemaRegistryObject to cache
        final AvroSerializer decoder = new AvroSerializer(false, parser, ENCODER_FACTORY,
            DECODER_FACTORY);
        final PlayingCard playingCard = new PlayingCard(true, 10, PlayingCardSuit.DIAMONDS);
        final String playingClassSchema = PlayingCard.getClassSchema().toString();
        final SchemaProperties registered = new SchemaProperties(MOCK_GUID, SchemaFormat.AVRO);
        final SchemaRegistrySchema registrySchema = new SchemaRegistrySchema(registered, playingClassSchema);
        final SerializerOptions serializerOptions = new SerializerOptions(MOCK_SCHEMA_GROUP, true, MOCK_CACHE_SIZE);

        final SchemaRegistryApacheAvroSerializer serializer = new SchemaRegistryApacheAvroSerializer(client, decoder,
            serializerOptions);

        assertNotNull(registrySchema.getProperties());

        when(client.getSchema(MOCK_GUID)).thenReturn(Mono.just(registrySchema));

        StepVerifier.create(client.getSchema(MOCK_GUID))
            .assertNext(schema -> {
                assertNotNull(schema.getProperties());

                assertEquals(playingClassSchema, schema.getDefinition());
                assertEquals(MOCK_GUID, schema.getProperties().getId());
            })
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
        final AvroSerializer avroSerializer = new AvroSerializer(false, parser, ENCODER_FACTORY, DECODER_FACTORY);
        final SerializerOptions serializerOptions = new SerializerOptions(MOCK_SCHEMA_GROUP, true, MOCK_CACHE_SIZE);

        SchemaRegistryApacheAvroSerializer deserializer = new SchemaRegistryApacheAvroSerializer(client,
            avroSerializer, serializerOptions);

        // Null payload should just complete the mono.
        StepVerifier.create(deserializer.deserializeAsync(null, null))
            .verifyComplete();
    }

    @Test
    void testNullPayloadSync() {
        final AvroSerializer avroSerializer = new AvroSerializer(false, parser, ENCODER_FACTORY, DECODER_FACTORY);
        final SerializerOptions serializerOptions = new SerializerOptions(MOCK_SCHEMA_GROUP, true, MOCK_CACHE_SIZE);

        SchemaRegistryApacheAvroSerializer deserializer = new SchemaRegistryApacheAvroSerializer(
            client, avroSerializer, serializerOptions);

        // Null payload should return null.
        final Object actual = deserializer.deserialize(null, null);

        // Assert
        assertNull(actual);
    }

    @Test
    void testIfTooShortPayloadThrow() throws IOException {
        final AvroSerializer avroSerializer = new AvroSerializer(false, parser, ENCODER_FACTORY, DECODER_FACTORY);
        final SerializerOptions serializerOptions = new SerializerOptions(MOCK_SCHEMA_GROUP, true, MOCK_CACHE_SIZE);

        SchemaRegistryApacheAvroSerializer deserializer = new SchemaRegistryApacheAvroSerializer(
            client, avroSerializer, serializerOptions);

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

    /**
     * Tests that a schema registered in the portal can be deserialized here.
     */
    @Test
    public void serializeFromPortal() throws IOException {
        // Arrange
        final Schema schema = SchemaBuilder.record("Person").namespace("com.example")
            .fields()
            .name("name").type().stringType().noDefault()
            .name("favourite_number").type().nullable().intType().noDefault()
            .name("favourite_colour").type().nullable().stringType().noDefault()
            .endRecord();

        final String expectedName = "Pearson";
        final int expectedNumber = 10;
        final String expectedColour = "blue";
        final GenericData.Record record = new GenericRecordBuilder(schema)
            .set("name", expectedName)
            .set("favourite_number", expectedNumber)
            .set("favourite_colour", expectedColour)
            .build();

        final TestContextManager testContextManager = new TestContextManager(testInfo.getTestMethod().get(), TestMode.PLAYBACK);
        final InterceptorManager interceptorManager;
        try {
            interceptorManager = new InterceptorManager(testContextManager);
        } catch (UncheckedIOException e) {
            Assertions.fail(e);
            return;
        }

        final TokenCredential tokenCredential = mock(TokenCredential.class);
        when(tokenCredential.getToken(any(TokenRequestContext.class))).thenAnswer(invocationOnMock -> {
            return Mono.fromCallable(() -> {
                return new AccessToken("foo", OffsetDateTime.now().plusMinutes(20));
            });
        });

        final SchemaRegistryAsyncClient schemaRegistryAsyncClient = new SchemaRegistryClientBuilder()
            .httpClient(interceptorManager.getPlaybackClient())
            .credential(tokenCredential)
            .fullyQualifiedNamespace(PLAYBACK_ENDPOINT)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildAsyncClient();

        final SchemaRegistryApacheAvroSerializer serializer = new SchemaRegistryApacheAvroSerializerBuilder()
            .schemaGroup(PLAYBACK_TEST_GROUP)
            .schemaRegistryAsyncClient(schemaRegistryAsyncClient)
            .avroSpecificReader(true)
            .buildSerializer();

        // Act
        byte[] outputArray;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096)) {
            StepVerifier.create(serializer.serializeAsync(outputStream, record))
                .expectComplete()
                .verify(Duration.ofSeconds(30));

            outputArray = outputStream.toByteArray();
        }

        assertTrue(outputArray.length > 0, "There should have been contents in array.");
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
