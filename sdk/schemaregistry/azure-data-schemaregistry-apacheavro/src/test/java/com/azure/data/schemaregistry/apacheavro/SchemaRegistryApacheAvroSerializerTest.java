// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.apacheavro;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.models.MessageContent;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.MockTokenCredential;
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
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.message.RawMessageEncoder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.azure.data.schemaregistry.apacheavro.SchemaRegistryApacheAvroSerializer.AVRO_MIME_TYPE;
import static com.azure.data.schemaregistry.apacheavro.SchemaRegistryApacheAvroSerializerIntegrationTest.PLAYBACK_ENDPOINT;
import static com.azure.data.schemaregistry.apacheavro.SchemaRegistryApacheAvroSerializerIntegrationTest.PLAYBACK_TEST_GROUP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link SchemaRegistryApacheAvroSerializer}.
 */
public class SchemaRegistryApacheAvroSerializerTest {
    private static final int SCHEMA_ID_SIZE = 32;

    private static final String MOCK_GUID = new String(new char[SCHEMA_ID_SIZE]).replace("\0", "a");
    private static final String MOCK_SCHEMA_GROUP = "mock-group";
    private static final int MOCK_CACHE_SIZE = 128;
    private static final DecoderFactory DECODER_FACTORY = DecoderFactory.get();
    private static final EncoderFactory ENCODER_FACTORY = EncoderFactory.get();

    private AutoCloseable mocksCloseable;

    @Mock
    private SchemaRegistryAsyncClient client;

    @BeforeEach
    public void beforeEach() {
        this.mocksCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void afterEach() throws Exception {
        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
    }

    @Test
    public void testRegistryGuidPrefixedToPayload() {
        // manually add SchemaRegistryObject into mock registry client cache
        final AvroSerializer avroSerializer = new AvroSerializer(false, ENCODER_FACTORY, DECODER_FACTORY);
        final PlayingCard playingCard = new PlayingCard(true, 10, PlayingCardSuit.DIAMONDS);
        final Schema playingClassSchema = PlayingCard.getClassSchema();
        final String schemaName = playingClassSchema.getFullName();

        final SchemaProperties schemaProperties = mock(SchemaProperties.class);
        when(schemaProperties.getGroupName()).thenReturn(MOCK_SCHEMA_GROUP);
        when(schemaProperties.getName()).thenReturn(schemaName);
        when(schemaProperties.getId()).thenReturn(MOCK_GUID);

        final SerializerOptions serializerOptions = new SerializerOptions(MOCK_SCHEMA_GROUP, false, MOCK_CACHE_SIZE);

        when(
            client.getSchemaProperties(MOCK_SCHEMA_GROUP, schemaName, playingClassSchema.toString(), SchemaFormat.AVRO))
                .thenReturn(Mono.just(schemaProperties));

        final String expectedContentType = AVRO_MIME_TYPE + "+" + MOCK_GUID;
        final SchemaRegistryApacheAvroSerializer serializer
            = new SchemaRegistryApacheAvroSerializer(client, avroSerializer, serializerOptions);

        StepVerifier.create(serializer.serializeAsync(playingCard, TypeReference.createInstance(MessageContent.class)))
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
        final AvroSerializer avroSerializer = new AvroSerializer(false, ENCODER_FACTORY, DECODER_FACTORY);
        final SerializerOptions serializerOptions = new SerializerOptions(MOCK_SCHEMA_GROUP, false, MOCK_CACHE_SIZE);
        final SchemaRegistryApacheAvroSerializer serializer
            = new SchemaRegistryApacheAvroSerializer(client, avroSerializer, serializerOptions);

        final MessageContent message = new MessageContent();

        // Act & Assert
        StepVerifier.create(serializer.serializeAsync(message, null)).verifyError(NullPointerException.class);
    }

    @Test
    public void testIfRegistryNullThenThrow() {
        // Arrange
        AvroSerializer encoder = new AvroSerializer(false, ENCODER_FACTORY, DECODER_FACTORY);
        final SerializerOptions serializerOptions = new SerializerOptions(MOCK_SCHEMA_GROUP, false, MOCK_CACHE_SIZE);

        // Act & Assert
        assertThrows(NullPointerException.class,
            () -> new SchemaRegistryApacheAvroSerializer(null, encoder, serializerOptions));
    }

    @Test
    void testGetSchemaAndDeserialize() throws IOException {
        // manually add SchemaRegistryObject to cache
        final AvroSerializer avroSerializer = new AvroSerializer(false, ENCODER_FACTORY, DECODER_FACTORY);
        final PlayingCard playingCard = new PlayingCard(true, 10, PlayingCardSuit.DIAMONDS);
        final String playingClassSchema = PlayingCard.getClassSchema().toString();
        final String schemaName = PlayingCard.getClassSchema().getFullName();

        final SchemaProperties schemaProperties = mock(SchemaProperties.class);
        when(schemaProperties.getGroupName()).thenReturn(MOCK_SCHEMA_GROUP);
        when(schemaProperties.getName()).thenReturn(schemaName);
        when(schemaProperties.getId()).thenReturn(MOCK_GUID);

        final SchemaRegistrySchema registrySchema = new SchemaRegistrySchema(schemaProperties, playingClassSchema);
        final SerializerOptions serializerOptions = new SerializerOptions(MOCK_SCHEMA_GROUP, true, MOCK_CACHE_SIZE);

        final SchemaRegistryApacheAvroSerializer serializer
            = new SchemaRegistryApacheAvroSerializer(client, avroSerializer, serializerOptions);

        assertNotNull(registrySchema.getProperties());

        when(client.getSchema(MOCK_GUID)).thenReturn(Mono.just(registrySchema));

        StepVerifier.create(client.getSchema(MOCK_GUID)).assertNext(schema -> {
            assertNotNull(schema.getProperties());

            assertEquals(playingClassSchema, schema.getDefinition());
            assertEquals(MOCK_GUID, schema.getProperties().getId());
        }).verifyComplete();

        final MockMessage message = getPayload(playingCard);

        StepVerifier.create(serializer.deserializeAsync(message, TypeReference.createInstance(PlayingCard.class)))
            .assertNext(actual -> {
                assertEquals(playingCard.getPlayingCardSuit(), actual.getPlayingCardSuit());
                assertEquals(playingCard.getCardValue(), actual.getCardValue());
                assertEquals(playingCard.getIsFaceCard(), actual.getIsFaceCard());
            })
            .verifyComplete();

        // Deserializing the same message again should work.
        StepVerifier.create(serializer.deserializeAsync(message, TypeReference.createInstance(PlayingCard.class)))
            .assertNext(actual -> {
                assertEquals(playingCard.getPlayingCardSuit(), actual.getPlayingCardSuit());
                assertEquals(playingCard.getCardValue(), actual.getCardValue());
                assertEquals(playingCard.getIsFaceCard(), actual.getIsFaceCard());
            })
            .verifyComplete();
    }

    public static Stream<Arguments> testEmptyPayload() {
        return Stream.of(Arguments.of(new MockMessage(), new MessageContent().setContentType("avro/binary")));
    }

    /**
     * Checks that an empty payload completes the mono.
     */
    @MethodSource
    @ParameterizedTest
    public void testEmptyPayload(MessageContent message) {
        // Arrange
        final AvroSerializer avroSerializer = new AvroSerializer(false, ENCODER_FACTORY, DECODER_FACTORY);
        final SerializerOptions serializerOptions = new SerializerOptions(MOCK_SCHEMA_GROUP, true, MOCK_CACHE_SIZE);

        final SchemaRegistryApacheAvroSerializer serializer
            = new SchemaRegistryApacheAvroSerializer(client, avroSerializer, serializerOptions);

        // Act & Assert
        StepVerifier.create(serializer.deserializeAsync(message, TypeReference.createInstance(PlayingCard.class)))
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

        final SchemaRegistryApacheAvroSerializer serializer
            = new SchemaRegistryApacheAvroSerializer(client, avroSerializer, serializerOptions);

        // Act
        final Person actual = serializer.deserialize(message, TypeReference.createInstance(Person.class));

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

        final SchemaRegistryApacheAvroSerializer serializer
            = new SchemaRegistryApacheAvroSerializer(client, avroSerializer, serializerOptions);

        // Null payload should throw NullPointerException.
        StepVerifier.create(serializer.serializeAsync(null, null)).expectError(NullPointerException.class).verify();
    }

    /**
     * Asserts that NPE is thrown to align with exception behaviour with other languages.
     */
    @Test
    public void testNullPayloadSync() {
        final AvroSerializer avroSerializer = new AvroSerializer(false, ENCODER_FACTORY, DECODER_FACTORY);
        final SerializerOptions serializerOptions = new SerializerOptions(MOCK_SCHEMA_GROUP, true, MOCK_CACHE_SIZE);

        SchemaRegistryApacheAvroSerializer serializer
            = new SchemaRegistryApacheAvroSerializer(client, avroSerializer, serializerOptions);

        // Null payload should throw NullPointerException.
        assertThrows(NullPointerException.class, () -> serializer.deserialize(null, null));
    }

    /**
     * Tests that a schema registered in the portal can be deserialized here.
     */
    @SuppressWarnings("deprecation")
    @Test
    public void serializeFromPortal() {
        // Arrange
        LinkedList<Function<HttpRequest, HttpResponse>> responses = new LinkedList<>();
        responses.add(request -> {
            HttpHeaders headers = new HttpHeaders().add(HttpHeaderName.CONTENT_LENGTH, "0")
                .add("Schema-Version", "1")
                .add(HttpHeaderName.SERVER, "Microsoft-HTTPAPI/2.0")
                .add("Schema-Name", "com.example.Person")
                .add("Schema-Id-Location",
                    "https://conniey.servicebus.windows.net:443/$schemagroups/$schemas/64fc737160ff41bdb8a0b8af028e6827?api-version=2021-10")
                .add(HttpHeaderName.DATE, "Wed, 05 Jan 2022 18:06:06 GMT")
                .add(HttpHeaderName.STRICT_TRANSPORT_SECURITY, "max-age=31536000")
                .add("Schema-Id", "64fc737160ff41bdb8a0b8af028e6827")
                .add("Schema-Group-Name", "azsdk_java_group")
                .add(HttpHeaderName.LOCATION,
                    "https://conniey.servicebus.windows.net:443/$schemagroups/azsdk_java_group/schemas/com.example.Person/versions/1?api-version=2021-10")
                .add("Schema-Versions-Location",
                    "https://conniey.servicebus.windows.net:443/$schemagroups/azsdk_java_group/schemas/com.example.Person/versions?api-version=2021-10");
            return new MockHttpResponse(request, 204, headers, new byte[0]);
        });

        final Schema schema = SchemaBuilder.record("Person")
            .namespace("com.example")
            .fields()
            .name("name")
            .type()
            .stringType()
            .noDefault()
            .name("favourite_number")
            .type()
            .nullable()
            .intType()
            .noDefault()
            .name("favourite_colour")
            .type()
            .nullable()
            .stringType()
            .noDefault()
            .endRecord();

        final String expectedName = "Pearson";
        final int expectedNumber = 10;
        final String expectedColour = "blue";
        final GenericData.Record record = new GenericRecordBuilder(schema).set("name", expectedName)
            .set("favourite_number", expectedNumber)
            .set("favourite_colour", expectedColour)
            .build();

        final SchemaRegistryAsyncClient client = getSchemaRegistryClient(responses);
        final SchemaRegistryApacheAvroSerializer serializer
            = new SchemaRegistryApacheAvroSerializerBuilder().schemaGroup(PLAYBACK_TEST_GROUP)
                .schemaRegistryClient(client)
                .avroSpecificReader(true)
                .buildSerializer();
        final String expectedContentType = AVRO_MIME_TYPE + "+64fc737160ff41bdb8a0b8af028e6827";

        // Act
        StepVerifier.create(serializer.serializeAsync(record, TypeReference.createInstance(MockMessage.class)))
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
    @SuppressWarnings("deprecation")
    @Test
    public void serializeForwardCompatibility() {
        // Arrange
        LinkedList<Function<HttpRequest, HttpResponse>> responses = new LinkedList<>();
        responses.add(request -> {
            HttpHeaders headers = new HttpHeaders().add(HttpHeaderName.CONTENT_LENGTH, "0")
                .add("Schema-Version", "2")
                .add(HttpHeaderName.SERVER, "Microsoft-HTTPAPI/2.0")
                .add("Schema-Name", "com.azure.data.schemaregistry.apacheavro.generatedtestsources.Person")
                .add("Schema-Id-Location",
                    "https://conniey.servicebus.windows.net:443/$schemagroups/$schemas/f047cfa64b374167b3a1d101370c1483?api-version=2021-10")
                .add(HttpHeaderName.DATE, "Wed, 12 Jan 2022 22:58:41 GMT")
                .add(HttpHeaderName.STRICT_TRANSPORT_SECURITY, "max-age=31536000")
                .add("Schema-Id", "f047cfa64b374167b3a1d101370c1483")
                .add("Schema-Group-Name", "azsdk_java_group")
                .add(HttpHeaderName.LOCATION,
                    "https://conniey.servicebus.windows.net:443/$schemagroups/azsdk_java_group/schemas/com.azure.data.schemaregistry.apacheavro.generatedtestsources.Person/versions/2?api-version=2021-10")
                .add("Schema-Versions-Location",
                    "https://conniey.servicebus.windows.net:443/$schemagroups/azsdk_java_group/schemas/com.azure.data.schemaregistry.apacheavro.generatedtestsources.Person/versions?api-version=2021-10");
            return new MockHttpResponse(request, 204, headers, new byte[0]);
        });

        responses.add(request -> {
            HttpHeaders headers = new HttpHeaders().add(HttpHeaderName.TRANSFER_ENCODING, "chunked")
                .add("Schema-Version", "2")
                .add(HttpHeaderName.SERVER, "Microsoft-HTTPAPI/2.0")
                .add("Schema-Name", "com.azure.data.schemaregistry.apacheavro.generatedtestsources.Person")
                .add("Schema-Id-Location",
                    "https://conniey.servicebus.windows.net:443/$schemagroups/$schemas/f047cfa64b374167b3a1d101370c1483?api-version=2021-10")
                .add(HttpHeaderName.DATE, "Wed, 12 Jan 2022 22:58:57 GMT")
                .add(HttpHeaderName.STRICT_TRANSPORT_SECURITY, "max-age=31536000")
                .add("Schema-Id", "f047cfa64b374167b3a1d101370c1483")
                .add("Schema-Group-Name", "azsdk_java_group")
                .add(HttpHeaderName.LOCATION,
                    "https://conniey.servicebus.windows.net:443/$schemagroups/azsdk_java_group/schemas/com.azure.data.schemaregistry.apacheavro.generatedtestsources.Person/versions/2?api-version=2021-10")
                .add("Schema-Versions-Location",
                    "https://conniey.servicebus.windows.net:443/$schemagroups/azsdk_java_group/schemas/com.azure.data.schemaregistry.apacheavro.generatedtestsources.Person/versions?api-version=2021-10");
            return new MockHttpResponse(request, 200, headers,
                ("{\"namespace\":\"com.azure.data.schemaregistry.apacheavro.generatedtestsources\",\"type\":\"record\","
                    + "\"name\":\"Person\",\"fields\":[{\"name\":\"name\",\"type\":\"string\"},"
                    + "{\"name\":\"favourite_number\",\"type\":[\"int\",\"null\"]},{\"name\":\"favourite_colour\","
                    + "\"type\":[\"string\",\"null\"]},{\"name\":\"favourite_pet\",\"type\":[\"string\",\"null\"]}]}")
                        .getBytes(StandardCharsets.UTF_8));
        });

        final SchemaRegistryAsyncClient client = getSchemaRegistryClient(responses);
        final SchemaRegistryApacheAvroSerializer serializer
            = new SchemaRegistryApacheAvroSerializerBuilder().schemaGroup(PLAYBACK_TEST_GROUP)
                .schemaRegistryClient(client)
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
        final AtomicReference<MessageContent> outputData = new AtomicReference<>();

        // Act: Serialize the new Person2.
        StepVerifier.create(serializer.serializeAsync(writerPerson, TypeReference.createInstance(MockMessage.class)))
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

        final Person readerPerson
            = serializer.deserialize(outputData.get(), TypeReference.createInstance(Person.class));

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
        final SchemaRegistryApacheAvroSerializer serializer
            = new SchemaRegistryApacheAvroSerializer(client, avroSerializer, serializerOptions);

        final PlayingCard playingCard = new PlayingCard(true, 10, PlayingCardSuit.DIAMONDS);
        final TypeReference<InvalidMessage> typeReference = TypeReference.createInstance(InvalidMessage.class);

        // Act & Assert
        StepVerifier.create(serializer.serializeAsync(playingCard, typeReference))
            .expectError(IllegalArgumentException.class)
            .verify();
    }

    /**
     * Verifies that we get an {@link IllegalStateException} if no schemaGroup was set.
     */
    @Test
    public void throwsWhenNoSchemaGroupSet() {
        // Arrange
        final AvroSerializer avroSerializer = new AvroSerializer(false, ENCODER_FACTORY, DECODER_FACTORY);
        final SerializerOptions serializerOptions = new SerializerOptions(null, true, MOCK_CACHE_SIZE);
        final SchemaRegistryApacheAvroSerializer serializer
            = new SchemaRegistryApacheAvroSerializer(client, avroSerializer, serializerOptions);

        final PlayingCard playingCard = new PlayingCard(true, 10, PlayingCardSuit.DIAMONDS);
        final TypeReference<MessageContent> typeReference = TypeReference.createInstance(MessageContent.class);

        // Act & Assert
        StepVerifier.create(serializer.serializeAsync(playingCard, typeReference))
            .expectError(IllegalStateException.class)
            .verify();
    }

    /**
     * Creates the schema registry client that returns mocked records.
     *
     * @return Corresponding SchemaRegistryAsyncClient.
     */
    private SchemaRegistryAsyncClient getSchemaRegistryClient(Queue<Function<HttpRequest, HttpResponse>> responses) {
        return new SchemaRegistryClientBuilder().credential(new MockTokenCredential())
            .pipeline(
                new HttpPipelineBuilder().httpClient(request -> Mono.just(responses.poll().apply(request))).build())
            .fullyQualifiedNamespace(PLAYBACK_ENDPOINT)
            .buildAsyncClient();
    }

    private static MockMessage getPayload(PlayingCard card) throws IOException {
        final MockMessage message = new MockMessage();
        message.setContentType(AVRO_MIME_TYPE + "+" + MOCK_GUID);

        final RawMessageEncoder<PlayingCard> encoder
            = new RawMessageEncoder<>(new GenericData(), PlayingCard.getClassSchema());

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            encoder.encode(card, outputStream);

            final BinaryData data = BinaryData.fromBytes(outputStream.toByteArray());
            message.setBodyAsBinaryData(data);
        }

        return message;
    }

    /**
     * Test class that extends from MessageContent
     */
    static class MockMessage extends MessageContent {
    }

    /**
     * This class does not expose the no-args constructor that we look for.
     */
    static class InvalidMessage extends MessageContent {
        InvalidMessage(String contents) {
            super();

            setBodyAsBinaryData(BinaryData.fromString(contents));
        }
    }
}
