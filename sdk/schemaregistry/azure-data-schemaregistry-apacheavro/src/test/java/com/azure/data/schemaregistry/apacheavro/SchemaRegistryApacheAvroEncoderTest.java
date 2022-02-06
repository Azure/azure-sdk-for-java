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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.data.schemaregistry.apacheavro.SchemaRegistryApacheAvroEncoder.AVRO_MIME_TYPE;
import static com.azure.data.schemaregistry.apacheavro.SchemaRegistryApacheAvroEncoder.SCHEMA_ID_SIZE;
import static com.azure.data.schemaregistry.apacheavro.SchemaRegistryApacheAvroEncoderIntegrationTest.PLAYBACK_ENDPOINT;
import static com.azure.data.schemaregistry.apacheavro.SchemaRegistryApacheAvroEncoderIntegrationTest.PLAYBACK_TEST_GROUP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link SchemaRegistryApacheAvroEncoder}.
 */
public class SchemaRegistryApacheAvroEncoderTest {
    private static final String MOCK_GUID = new String(new char[SCHEMA_ID_SIZE]).replace("\0", "a");
    private static final String MOCK_SCHEMA_GROUP = "mock-group";
    private static final int MOCK_CACHE_SIZE = 128;
    private static final DecoderFactory DECODER_FACTORY = DecoderFactory.get();
    private static final EncoderFactory ENCODER_FACTORY = EncoderFactory.get();

    private InterceptorManager interceptorManager;
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
        if (interceptorManager != null) {
            interceptorManager.close();
        }

        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
    }

    @Test
    void testRegistryGuidPrefixedToPayload() {
        // manually add SchemaRegistryObject into mock registry client cache
        final AvroSerializer avroSerializer = new AvroSerializer(false, new Schema.Parser(),
            ENCODER_FACTORY, DECODER_FACTORY);
        final PlayingCard playingCard = new PlayingCard(true, 10, PlayingCardSuit.DIAMONDS);
        final Schema playingClassSchema = PlayingCard.getClassSchema();
        final SchemaProperties registered = new SchemaProperties(MOCK_GUID, SchemaFormat.AVRO);
        final SerializerOptions serializerOptions = new SerializerOptions(MOCK_SCHEMA_GROUP, false, MOCK_CACHE_SIZE);

        when(client.getSchemaProperties(MOCK_SCHEMA_GROUP, playingClassSchema.getFullName(),
            playingClassSchema.toString(), SchemaFormat.AVRO)).thenReturn(Mono.just(registered));

        final String expectedContentType = AVRO_MIME_TYPE + "+" + MOCK_GUID;
        final SchemaRegistryApacheAvroEncoder encoder = new SchemaRegistryApacheAvroEncoder(client,
            avroSerializer, serializerOptions);

        StepVerifier.create(encoder.encodeMessageDataAsync(playingCard,
                TypeReference.createInstance(MessageWithMetadata.class)))
            .assertNext(message -> {
                // guid should match preloaded SchemaRegistryObject guid
                assertEquals(expectedContentType, message.getContentType());
                assertNotNull(message.getBodyAsBinaryData());
            })
            .verifyComplete();
    }

    @Test
    void testNullPayloadThrowsSerializationException() {
        // Arrange
        final AvroSerializer encoder = new AvroSerializer(false, parser, ENCODER_FACTORY,
            DECODER_FACTORY);
        final SerializerOptions serializerOptions = new SerializerOptions(MOCK_SCHEMA_GROUP, false, MOCK_CACHE_SIZE);
        final SchemaRegistryApacheAvroEncoder serializer = new SchemaRegistryApacheAvroEncoder(
            client, encoder, serializerOptions);

        final MessageWithMetadata message = new MessageWithMetadata();

        // Act & Assert
        StepVerifier.create(serializer.encodeMessageDataAsync(message, null))
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
            () -> new SchemaRegistryApacheAvroEncoder(null, encoder, serializerOptions));
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

        final SchemaRegistryApacheAvroEncoder serializer = new SchemaRegistryApacheAvroEncoder(client, decoder,
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

        StepVerifier.create(serializer.decodeMessageDataAsync(message, TypeReference.createInstance(PlayingCard.class)))
            .assertNext(actual -> {
                assertEquals(playingCard.getPlayingCardSuit(), actual.getPlayingCardSuit());
                assertEquals(playingCard.getCardValue(), actual.getCardValue());
                assertEquals(playingCard.getIsFaceCard(), actual.getIsFaceCard());
            })
            .verifyComplete();
    }

    /**
     * Asserts that NPE is thrown to align with exception behaviour with other languages.
     */
    @Test
    void testNullPayload() {
        final AvroSerializer avroSerializer = new AvroSerializer(false, parser, ENCODER_FACTORY, DECODER_FACTORY);
        final SerializerOptions serializerOptions = new SerializerOptions(MOCK_SCHEMA_GROUP, true, MOCK_CACHE_SIZE);

        SchemaRegistryApacheAvroEncoder deserializer = new SchemaRegistryApacheAvroEncoder(client,
            avroSerializer, serializerOptions);

        // Null payload should throw NullPointerException.
        StepVerifier.create(deserializer.encodeMessageDataAsync(null, null))
            .expectError(NullPointerException.class)
            .verify();
    }

    /**
     * Asserts that NPE is thrown to align with exception behaviour with other languages.
     */
    @Test
    void testNullPayloadSync() {
        final AvroSerializer avroSerializer = new AvroSerializer(false, parser, ENCODER_FACTORY, DECODER_FACTORY);
        final SerializerOptions serializerOptions = new SerializerOptions(MOCK_SCHEMA_GROUP, true, MOCK_CACHE_SIZE);

        SchemaRegistryApacheAvroEncoder deserializer = new SchemaRegistryApacheAvroEncoder(
            client, avroSerializer, serializerOptions);

        // Null payload should throw NullPointerException.
        assertThrows(NullPointerException.class, () -> deserializer.decodeMessageData(null, null));
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
        final SchemaRegistryApacheAvroEncoder serializer = new SchemaRegistryApacheAvroEncoderBuilder()
            .schemaGroup(PLAYBACK_TEST_GROUP)
            .schemaRegistryAsyncClient(client)
            .avroSpecificReader(true)
            .buildEncoder();
        final String expectedContentType = AVRO_MIME_TYPE + "+64fc737160ff41bdb8a0b8af028e6827";

        // Act
        StepVerifier.create(serializer.encodeMessageDataAsync(record, TypeReference.createInstance(MockMessage.class)))
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
        final SchemaRegistryApacheAvroEncoder serializer = new SchemaRegistryApacheAvroEncoderBuilder()
            .schemaGroup(PLAYBACK_TEST_GROUP)
            .schemaRegistryAsyncClient(client)
            .avroSpecificReader(true)
            .buildEncoder();
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
        StepVerifier.create(serializer.encodeMessageDataAsync(writerPerson, TypeReference.createInstance(MockMessage.class)))
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

        final Person readerPerson = serializer.decodeMessageData(outputData.get(), TypeReference.createInstance(Person.class));

        assertNotNull(readerPerson);
        assertEquals(name, readerPerson.getName());
        assertEquals(number, readerPerson.getFavouriteNumber());
        assertEquals(colour, readerPerson.getFavouriteColour());
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
            when(tokenCredential.getToken(any(TokenRequestContext.class))).thenAnswer(invocationOnMock -> {
                return Mono.fromCallable(() -> {
                    return new AccessToken("foo", OffsetDateTime.now().plusMinutes(20));
                });
            });

            endpoint = PLAYBACK_ENDPOINT;
        } else {
            tokenCredential = new DefaultAzureCredentialBuilder().build();
            endpoint = System.getenv(SchemaRegistryApacheAvroEncoderIntegrationTest.SCHEMA_REGISTRY_ENDPOINT);

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
