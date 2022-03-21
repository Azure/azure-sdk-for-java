// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.apacheavro;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.experimental.models.MessageWithMetadata;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.InterceptorManager;
import com.azure.core.test.TestContextManager;
import com.azure.core.test.TestMode;
import com.azure.core.util.BinaryData;
import com.azure.core.util.serializer.TypeReference;
import com.azure.data.schemaregistry.SchemaRegistryAsyncClient;
import com.azure.data.schemaregistry.SchemaRegistryClientBuilder;
import com.azure.data.schemaregistry.apacheavro.generatedtestsources.Person;
import com.azure.data.schemaregistry.apacheavro.generatedtestsources.Person2;
import com.azure.data.schemaregistry.apacheavro.generatedtestsources.PlayingCard;
import com.azure.data.schemaregistry.apacheavro.generatedtestsources.PlayingCardSuit;
import com.azure.data.schemaregistry.models.SchemaFormat;
import com.azure.data.schemaregistry.models.SchemaProperties;
import com.azure.data.schemaregistry.models.SchemaRegistrySchema;
import com.azure.identity.DefaultAzureCredentialBuilder;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static com.azure.data.schemaregistry.apacheavro.SchemaRegistryApacheAvroSerializer.AVRO_MIME_TYPE;
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

    private InterceptorManager interceptorManager;
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
    }

    @AfterEach
    public void afterEach() throws Exception {
        if (interceptorManager != null) {
            interceptorManager.close();
        }

        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
    }

    @Test
    public void testRegistryGuidPrefixedToPayload() {
        // manually add SchemaRegistryObject into mock registry client cache
        final AvroSerializer avroSerializer = new AvroSerializer(false,
            ENCODER_FACTORY, DECODER_FACTORY);
        final PlayingCard playingCard = new PlayingCard(true, 10, PlayingCardSuit.DIAMONDS);
        final Schema playingClassSchema = PlayingCard.getClassSchema();
        final SchemaProperties registered = new SchemaProperties(MOCK_GUID, SchemaFormat.AVRO);
        final SerializerOptions serializerOptions = new SerializerOptions(MOCK_SCHEMA_GROUP, false, MOCK_CACHE_SIZE);

        when(client.getSchemaProperties(MOCK_SCHEMA_GROUP, playingClassSchema.getFullName(),
            playingClassSchema.toString(), SchemaFormat.AVRO)).thenReturn(Mono.just(registered));

        final String expectedContentType = AVRO_MIME_TYPE + "+" + MOCK_GUID;
        final SchemaRegistryApacheAvroSerializer encoder = new SchemaRegistryApacheAvroSerializer(client,
            avroSerializer, serializerOptions);

        StepVerifier.create(encoder.serializeMessageDataAsync(playingCard,
                TypeReference.createInstance(MessageWithMetadata.class)))
            .assertNext(message -> {
                // guid should match preloaded SchemaRegistryObject guid
                assertEquals(expectedContentType, message.getContentType());
                assertNotNull(message.getBodyAsBinaryData());
            })
            .verifyComplete();
    }

    @Test
    public void testNullPayloadThrowsSerializationException() {
        // Arrange
        final AvroSerializer avroSerializer = new AvroSerializer(false, ENCODER_FACTORY,
            DECODER_FACTORY);
        final SerializerOptions serializerOptions = new SerializerOptions(MOCK_SCHEMA_GROUP, false, MOCK_CACHE_SIZE);
        final SchemaRegistryApacheAvroSerializer encoder = new SchemaRegistryApacheAvroSerializer(client, avroSerializer,
            serializerOptions);

        final MessageWithMetadata message = new MessageWithMetadata();

        // Act & Assert
        StepVerifier.create(encoder.serializeMessageDataAsync(message, null))
            .verifyError(NullPointerException.class);
    }

    @Test
    public void testIfRegistryNullThenThrow() {
        // Arrange
        AvroSerializer encoder = new AvroSerializer(false, ENCODER_FACTORY,
            DECODER_FACTORY);
        final SerializerOptions serializerOptions = new SerializerOptions(MOCK_SCHEMA_GROUP, false, MOCK_CACHE_SIZE);

        // Act & Assert
        assertThrows(NullPointerException.class,
            () -> new SchemaRegistryApacheAvroSerializer(null, encoder, serializerOptions));
    }

    @Test
    void testGetSchemaAndDeserialize() throws IOException {
        // manually add SchemaRegistryObject to cache
        final AvroSerializer decoder = new AvroSerializer(false, ENCODER_FACTORY,
            DECODER_FACTORY);
        final PlayingCard playingCard = new PlayingCard(true, 10, PlayingCardSuit.DIAMONDS);
        final String playingClassSchema = PlayingCard.getClassSchema().toString();
        final SchemaProperties registered = new SchemaProperties(MOCK_GUID, SchemaFormat.AVRO);
        final SchemaRegistrySchema registrySchema = new SchemaRegistrySchema(registered, playingClassSchema);
        final SerializerOptions serializerOptions = new SerializerOptions(MOCK_SCHEMA_GROUP, true, MOCK_CACHE_SIZE);

        final SchemaRegistryApacheAvroSerializer encoder = new SchemaRegistryApacheAvroSerializer(client, decoder,
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

        final MockMessage message = getPayload(playingCard);

        StepVerifier.create(encoder.deserializeMessageDataAsync(message, TypeReference.createInstance(PlayingCard.class)))
            .assertNext(actual -> {
                assertEquals(playingCard.getPlayingCardSuit(), actual.getPlayingCardSuit());
                assertEquals(playingCard.getCardValue(), actual.getCardValue());
                assertEquals(playingCard.getIsFaceCard(), actual.getIsFaceCard());
            })
            .verifyComplete();

        // Deserializing the same message again should work.
        StepVerifier.create(encoder.deserializeMessageDataAsync(message, TypeReference.createInstance(PlayingCard.class)))
                .assertNext(actual -> {
                    assertEquals(playingCard.getPlayingCardSuit(), actual.getPlayingCardSuit());
                    assertEquals(playingCard.getCardValue(), actual.getCardValue());
                    assertEquals(playingCard.getIsFaceCard(), actual.getIsFaceCard());
                })
                .verifyComplete();
    }

    public static Stream<Arguments> testEmptyPayload() {
        return Stream.of(
            Arguments.of(
                new MockMessage(),
                new MessageWithMetadata().setContentType("avro/binary"))
        );
    }

    /**
     * Checks that an empty payload completes the mono.
     */
    @MethodSource
    @ParameterizedTest
    public void testEmptyPayload(MessageWithMetadata message) {
        // Arrange
        final AvroSerializer avroSerializer = new AvroSerializer(false, ENCODER_FACTORY, DECODER_FACTORY);
        final SerializerOptions serializerOptions = new SerializerOptions(MOCK_SCHEMA_GROUP, true, MOCK_CACHE_SIZE);

        final SchemaRegistryApacheAvroSerializer encoder = new SchemaRegistryApacheAvroSerializer(client,
            avroSerializer, serializerOptions);

        // Act & Assert
        StepVerifier.create(encoder.deserializeMessageDataAsync(message, TypeReference.createInstance(PlayingCard.class)))
            .expectComplete()
            .verify();
    }

    /**
     * Checks that an empty payload returns null.
     */
    @Test
    public void testEmptyPayloadSync() {
        // Arrange
        final AvroSerializer avroSerializer = new AvroSerializer(false, ENCODER_FACTORY, DECODER_FACTORY);
        final SerializerOptions serializerOptions = new SerializerOptions(MOCK_SCHEMA_GROUP, true, MOCK_CACHE_SIZE);
        final MockMessage message = new MockMessage();

        final SchemaRegistryApacheAvroSerializer encoder = new SchemaRegistryApacheAvroSerializer(client,
            avroSerializer, serializerOptions);

        // Act
        final Person actual = encoder.deserializeMessageData(message, TypeReference.createInstance(Person.class));

        // Assert
        assertNull(actual);
    }

    /**
     * Asserts that NPE is thrown to align with exception behaviour with other languages.
     */
    @Test
    public void testNullPayload() {
        final AvroSerializer avroSerializer = new AvroSerializer(false, ENCODER_FACTORY, DECODER_FACTORY);
        final SerializerOptions serializerOptions = new SerializerOptions(MOCK_SCHEMA_GROUP, true, MOCK_CACHE_SIZE);

        final SchemaRegistryApacheAvroSerializer encoder = new SchemaRegistryApacheAvroSerializer(client,
            avroSerializer, serializerOptions);

        // Null payload should throw NullPointerException.
        StepVerifier.create(encoder.serializeMessageDataAsync(null, null))
            .expectError(NullPointerException.class)
            .verify();
    }

    /**
     * Asserts that NPE is thrown to align with exception behaviour with other languages.
     */
    @Test
    public void testNullPayloadSync() {
        final AvroSerializer avroSerializer = new AvroSerializer(false, ENCODER_FACTORY, DECODER_FACTORY);
        final SerializerOptions serializerOptions = new SerializerOptions(MOCK_SCHEMA_GROUP, true, MOCK_CACHE_SIZE);

        SchemaRegistryApacheAvroSerializer encoder = new SchemaRegistryApacheAvroSerializer(
            client, avroSerializer, serializerOptions);

        // Null payload should throw NullPointerException.
        assertThrows(NullPointerException.class, () -> encoder.deserializeMessageData(null, null));
    }

    /**
     * Tests that a schema registered in the portal can be deserialized here.
     */
    @Test
    public void serializeFromPortal() {
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

        final SchemaRegistryAsyncClient client = getSchemaRegistryClient(testInfo, TestMode.PLAYBACK);
        final SchemaRegistryApacheAvroSerializer encoder = new SchemaRegistryApacheAvroSerializerBuilder()
            .schemaGroup(PLAYBACK_TEST_GROUP)
            .schemaRegistryAsyncClient(client)
            .avroSpecificReader(true)
            .buildSerializer();
        final String expectedContentType = AVRO_MIME_TYPE + "+64fc737160ff41bdb8a0b8af028e6827";

        // Act
        StepVerifier.create(encoder.serializeMessageDataAsync(record, TypeReference.createInstance(MockMessage.class)))
            .assertNext(message -> {
                assertEquals(expectedContentType, message.getContentType());
                assertNotNull(message.getBodyAsBinaryData());
                assertTrue(message.getBodyAsBinaryData().getLength() > 0, "There should have been contents in array.");
            })
            .expectComplete()
            .verify(Duration.ofSeconds(30));
    }

    /**
     * Verifies that we can deserialize and serialize when the writer schema is newer than the reader schema. Writer
     * schema is using a forward compatible schema.
     */
    @Test
    public void serializeForwardCompatibility() {
        // Arrange
        final SchemaRegistryAsyncClient client = getSchemaRegistryClient(testInfo, TestMode.PLAYBACK);
        final SchemaRegistryApacheAvroSerializer encoder = new SchemaRegistryApacheAvroSerializerBuilder()
            .schemaGroup(PLAYBACK_TEST_GROUP)
            .schemaRegistryAsyncClient(client)
            .avroSpecificReader(true)
            .buildSerializer();
        final String expectedContentType = AVRO_MIME_TYPE + "+f047cfa64b374167b3a1d101370c1483";

        final String name = "Jackson";
        final String colour = "green";
        final String pet = "Gophers";
        final int number = 19;

        // The writer has a newer schema.
        final Person2 writerPerson = Person2.newBuilder()
            .setName(name)
            .setFavouriteNumber(number)
            .setFavouriteColour(colour)
            .setFavouritePet(pet)
            .build();
        final AtomicReference<MessageWithMetadata> outputData = new AtomicReference<>();

        // Act: Serialize the new Person2.
        StepVerifier.create(encoder.serializeMessageDataAsync(writerPerson, TypeReference.createInstance(MockMessage.class)))
            .assertNext(message -> {
                assertEquals(expectedContentType, message.getContentType());

                assertNotNull(message.getBodyAsBinaryData());
                assertTrue(message.getBodyAsBinaryData().getLength() > 0, "There should have been contents in array.");
                assertTrue(outputData.compareAndSet(null, message), "There should not have been a value set.");
            })
            .expectComplete()
            .verify(Duration.ofSeconds(30));

        // Act: Deserialize Person (the older schema)
        assertNotNull(outputData.get(), "Value should have been set from the test.");

        final Person readerPerson = encoder.deserializeMessageData(outputData.get(), TypeReference.createInstance(Person.class));

        assertNotNull(readerPerson);
        assertEquals(name, readerPerson.getName());
        assertEquals(number, readerPerson.getFavouriteNumber());
        assertEquals(colour, readerPerson.getFavouriteColour());
    }

    /**
     * Verifies that we get an {@link IllegalArgumentException} if the implementing class does not have an no-args
     * constructor.
     */
    @Test
    public void throwsWhenConstructorNotAvailable() {
        // Arrange
        final AvroSerializer avroSerializer = new AvroSerializer(false, ENCODER_FACTORY, DECODER_FACTORY);
        final SerializerOptions serializerOptions = new SerializerOptions(MOCK_SCHEMA_GROUP, true, MOCK_CACHE_SIZE);
        final SchemaRegistryApacheAvroSerializer encoder = new SchemaRegistryApacheAvroSerializer(
            client, avroSerializer, serializerOptions);

        final PlayingCard playingCard = new PlayingCard(true, 10, PlayingCardSuit.DIAMONDS);
        final TypeReference<InvalidMessage> typeReference = TypeReference.createInstance(InvalidMessage.class);

        // Act & Assert
        StepVerifier.create(encoder.serializeMessageDataAsync(playingCard, typeReference))
            .expectError(IllegalArgumentException.class)
            .verify();
    }

    /**
     * Test that the original preamble payload can be deserialized.
     */
    @Test
    public void backwardsCompatiblePreamble() throws IOException {
        // Arrange
        final PlayingCard expected = PlayingCard.newBuilder()
            .setIsFaceCard(true)
            .setCardValue(15)
            .setPlayingCardSuit(PlayingCardSuit.SPADES)
            .build();
        final SchemaRegistrySchema schemaResponse = new SchemaRegistrySchema(
            new SchemaProperties(MOCK_GUID, SchemaFormat.AVRO), expected.getSchema().toString());
        final AvroSerializer avroSerializer = new AvroSerializer(true, ENCODER_FACTORY,
            DECODER_FACTORY);
        final SerializerOptions serializerOptions = new SerializerOptions(MOCK_SCHEMA_GROUP, true, MOCK_CACHE_SIZE);
        final SchemaRegistryApacheAvroSerializer encoder = new SchemaRegistryApacheAvroSerializer(client, avroSerializer,
            serializerOptions);

        when(client.getSchema(MOCK_GUID)).thenReturn(Mono.just(schemaResponse));

        // Manually serialize the data with the preamble.
        final BinaryData binaryData;
        final byte[] encodedCard = avroSerializer.encode(expected);
        final int size = RECORD_FORMAT_INDICATOR_SIZE + SCHEMA_ID_SIZE + encodedCard.length;
        final byte[] guidBytes = MOCK_GUID.getBytes(StandardCharsets.UTF_8);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(size)) {
            outputStream.write(RECORD_FORMAT_INDICATOR, 0, RECORD_FORMAT_INDICATOR.length);
            outputStream.write(guidBytes, 0, guidBytes.length);
            outputStream.write(encodedCard, 0, encodedCard.length);
            outputStream.flush();

            binaryData = BinaryData.fromBytes(outputStream.toByteArray());
        }

        final MockMessage message = new MockMessage();
        message.setBodyAsBinaryData(binaryData);

        // Act
        final PlayingCard actual = encoder.deserializeMessageData(message, TypeReference.createInstance(PlayingCard.class));

        // Assert
        assertNotNull(actual);
        assertEquals(expected.getCardValue(), actual.getCardValue());
        assertEquals(expected.getIsFaceCard(), actual.getIsFaceCard());
        assertEquals(expected.getPlayingCardSuit(), actual.getPlayingCardSuit());
    }

    private static MockMessage getPayload(PlayingCard card) throws IOException {
        final MockMessage message = new MockMessage();
        message.setContentType(AVRO_MIME_TYPE + "+" + MOCK_GUID);

        final RawMessageEncoder<PlayingCard> encoder = new RawMessageEncoder<>(new GenericData(),
            PlayingCard.getClassSchema());

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            encoder.encode(card, outputStream);

            final BinaryData data = BinaryData.fromBytes(outputStream.toByteArray());
            message.setBodyAsBinaryData(data);
        }

        return message;
    }

    /**
     * Creates the schema registry client based on the test mode.
     *
     * @param testInfo Information about current test.
     * @param testMode Test mode
     *
     * @return Corresponding SchemaRegistryAsyncClient.
     */
    private SchemaRegistryAsyncClient getSchemaRegistryClient(TestInfo testInfo, TestMode testMode) {
        final TestContextManager testContextManager = new TestContextManager(testInfo.getTestMethod().get(), testMode);
        try {
            interceptorManager = new InterceptorManager(testContextManager);
        } catch (UncheckedIOException e) {
            Assertions.fail(e);
            throw e;
        }

        TokenCredential tokenCredential;
        String endpoint;
        if (testMode == TestMode.PLAYBACK) {
            tokenCredential = mock(TokenCredential.class);

            // Sometimes it throws an "NotAMockException", so we had to change from thenReturn to thenAnswer.
            when(tokenCredential.getToken(any(TokenRequestContext.class)))
                    .thenAnswer(invocationOnMock -> Mono.fromCallable(() ->
                            new AccessToken("foo", OffsetDateTime.now().plusMinutes(20))));

            endpoint = PLAYBACK_ENDPOINT;
        } else {
            tokenCredential = new DefaultAzureCredentialBuilder().build();
            endpoint = System.getenv(SchemaRegistryApacheAvroSerializerIntegrationTest.SCHEMA_REGISTRY_ENDPOINT);

            assertNotNull(endpoint, "'endpoint' cannot be null in LIVE/RECORD mode.");
        }

        final SchemaRegistryClientBuilder builder = new SchemaRegistryClientBuilder()
            .credential(tokenCredential)
            .fullyQualifiedNamespace(endpoint);

        if (testMode == TestMode.PLAYBACK) {
            builder.httpClient(interceptorManager.getPlaybackClient());
        } else {
            builder.addPolicy(new RetryPolicy())
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .addPolicy(interceptorManager.getRecordPolicy());
        }

        return builder.buildAsyncClient();
    }

    /**
     * Test class that extends from MessageWithMetadata
     */
    static class MockMessage extends MessageWithMetadata {
    }

    /**
     * This class does not expose the no-args constructor that we look for.
     */
    static class InvalidMessage extends MessageWithMetadata {
        InvalidMessage(String contents) {
            super();

            setBodyAsBinaryData(BinaryData.fromString(contents));
        }
    }
}
