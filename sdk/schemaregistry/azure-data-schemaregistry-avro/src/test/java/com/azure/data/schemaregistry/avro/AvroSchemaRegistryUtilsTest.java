// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.avro;

import com.azure.core.util.serializer.TypeReference;
import com.azure.data.schemaregistry.avro.generatedtestsources.HandOfCards;
import com.azure.data.schemaregistry.avro.generatedtestsources.PlayingCard;
import com.azure.data.schemaregistry.avro.generatedtestsources.PlayingCardSuit;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.message.RawMessageEncoder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link AvroSchemaRegistryUtils}.
 */
public class AvroSchemaRegistryUtilsTest {

    private final Schema.Parser parser = new Schema.Parser();
    private final EncoderFactory encoderFactory = EncoderFactory.get();
    private final DecoderFactory decoderFactory = DecoderFactory.get();

    private AutoCloseable mocksCloseable;

    @BeforeEach
    public void beforeEach() {
        mocksCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void afterEach() throws Exception {
        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
    }

    /**
     * Tests that the correct exceptions are thrown when constructing an instance with null.
     */
    @Test
    public void constructorNull() {
        assertThrows(NullPointerException.class,
            () -> new AvroSchemaRegistryUtils(true, null, encoderFactory, decoderFactory));
        assertThrows(NullPointerException.class,
            () -> new AvroSchemaRegistryUtils(true, parser, null, decoderFactory));
        assertThrows(NullPointerException.class,
            () -> new AvroSchemaRegistryUtils(true, parser, encoderFactory, null));
    }

    public static Stream<Arguments> getSchemaStringPrimitive() {
        return Stream.of(
            Arguments.of("foo", Schema.create(Schema.Type.STRING)),
            Arguments.of(new byte[4], Schema.create(Schema.Type.BYTES)),
            Arguments.of(14, Schema.create(Schema.Type.INT)),
            Arguments.of(14L, Schema.create(Schema.Type.LONG)),
            Arguments.of(15.0f, Schema.create(Schema.Type.FLOAT)),
            Arguments.of(15.00d, Schema.create(Schema.Type.DOUBLE)),
            Arguments.of(Boolean.FALSE, Schema.create(Schema.Type.BOOLEAN)),
            Arguments.of(null, Schema.create(Schema.Type.NULL)));
    }

    /**
     * Tests primitive schemas are returned with correct names and schema representations.
     *
     * @param value Value to get schema representation of.
     * @param expected Expected schema.
     */
    @MethodSource
    @ParameterizedTest
    public void getSchemaStringPrimitive(Object value, Schema expected) {
        // Arrange
        final AvroSchemaRegistryUtils registryUtils = new AvroSchemaRegistryUtils(false, parser,
            encoderFactory, decoderFactory);

        // Act
        final String schemaString = registryUtils.getSchemaString(value);
        final String fullName = registryUtils.getSchemaName(value);

        // Assert
        assertEquals(expected.toString(), schemaString);
        assertEquals(expected.getFullName(), fullName);
    }

    /**
     * Verifies that the schema for generic containers can be obtained.
     */
    @Test
    public void getSchemaGenericContainer() {
        // Arrange
        final AvroSchemaRegistryUtils registryUtils = new AvroSchemaRegistryUtils(false, parser,
            encoderFactory, decoderFactory);
        final Schema arraySchema = Schema.createArray(Schema.create(Schema.Type.STRING));
        final GenericData.Array<String> genericArray = new GenericData.Array<>(10, arraySchema);

        // Act
        final String schemaString = registryUtils.getSchemaString(genericArray);
        final String fullName = registryUtils.getSchemaName(genericArray);

        // Assert
        assertEquals(arraySchema.toString(), schemaString);
        assertEquals(arraySchema.getFullName(), fullName);
    }

    /**
     * Tests that we can encode an object.
     *
     * @throws IOException If card cannot be serialized.
     */
    @Test
    public void encodesObject() throws IOException {
        // Arrange
        final AvroSchemaRegistryUtils registryUtils = new AvroSchemaRegistryUtils(false, parser,
            encoderFactory, decoderFactory);

        final PlayingCard card = PlayingCard.newBuilder()
            .setPlayingCardSuit(PlayingCardSuit.DIAMONDS)
            .setIsFaceCard(true).setCardValue(13)
            .build();

        // Using the raw message encoder because the default card.getByteBuffer() uses BinaryMessageEncoder which adds
        // a header.
        final RawMessageEncoder<PlayingCard> rawMessageEncoder = new RawMessageEncoder<>(card.getSpecificData(),
            card.getSchema());
        final byte[] expectedData = rawMessageEncoder.encode(card).array();

        // Act
        final byte[] encoded = registryUtils.encode(card);

        // Assert
        assertArrayEquals(expectedData, encoded);
    }


    /**
     * Tests that we can encode and decode an object using {@link AvroSchemaRegistryUtils#encode(Object)} and {@link
     * AvroSchemaRegistryUtils#decode(byte[], byte[], TypeReference)}.
     *
     * @throws IOException If card cannot be serialized.
     */
    @Test
    public void encodesAndDecodesObject() throws IOException {
        // Arrange
        final AvroSchemaRegistryUtils registryUtils = new AvroSchemaRegistryUtils(false, parser,
            encoderFactory, decoderFactory);

        final PlayingCard expected = PlayingCard.newBuilder()
            .setPlayingCardSuit(PlayingCardSuit.DIAMONDS)
            .setIsFaceCard(true)
            .setCardValue(13)
            .build();

        // Using the raw message encoder because the default card.getByteBuffer() uses BinaryMessageEncoder which adds
        // a header.
        final byte[] encoded = registryUtils.encode(expected);
        final byte[] schemaBytes = expected.getSchema().toString().getBytes(StandardCharsets.UTF_8);

        // Act
        final PlayingCard actual = registryUtils.decode(encoded, schemaBytes,
            TypeReference.createInstance(PlayingCard.class));

        // Assert
        assertCardEquals(expected, actual);
    }

    /**
     * Tests that we can decode an object that uses single object encoding.
     *
     * @throws IOException If card cannot be decoded.
     */
    @Test
    public void decodeSingleObjectEncodedObject() throws IOException {
        // Arrange
        final AvroSchemaRegistryUtils registryUtils = new AvroSchemaRegistryUtils(false, parser,
            encoderFactory, decoderFactory);

        final PlayingCard card = PlayingCard.newBuilder()
            .setPlayingCardSuit(PlayingCardSuit.DIAMONDS)
            .setIsFaceCard(true).setCardValue(13)
            .build();
        final PlayingCard card2 = PlayingCard.newBuilder()
            .setPlayingCardSuit(PlayingCardSuit.SPADES)
            .setIsFaceCard(false).setCardValue(25)
            .build();
        final HandOfCards expected = HandOfCards.newBuilder()
            .setCards(Arrays.asList(card, card2))
            .build();

        final byte[] expectedData = expected.toByteBuffer().array();

        final String schemaString = expected.getSchema().toString();
        final byte[] schemaBytes = schemaString.getBytes(StandardCharsets.UTF_8);

        // Act
        final HandOfCards actual = registryUtils.decode(expectedData, schemaBytes,
            TypeReference.createInstance(HandOfCards.class));

        // Assert
        assertNotNull(actual);
        assertNotNull(actual.getCards());
        assertEquals(expected.getCards().size(), actual.getCards().size());

        final List<PlayingCard> list = new ArrayList<>(actual.getCards());

        expected.getCards().forEach(expectedCard -> {
            final int expectedSize = list.size() - 1;

            assertTrue(list.removeIf(playingCard -> {
                return expectedCard.getIsFaceCard() == playingCard.getIsFaceCard()
                    && expectedCard.getCardValue() == playingCard.getCardValue()
                    && expectedCard.getPlayingCardSuit() == playingCard.getPlayingCardSuit();
            }));

            assertEquals(expectedSize, list.size());
        });

        assertTrue(list.isEmpty());
    }

    private static void assertCardEquals(PlayingCard expected, PlayingCard actual) {
        if (expected == null) {
            assertNull(actual);
            return;
        }

        assertNotNull(actual);
        assertEquals(expected.getPlayingCardSuit(), actual.getPlayingCardSuit());
        assertEquals(expected.getCardValue(), actual.getCardValue());
        assertEquals(expected.getIsFaceCard(), actual.getIsFaceCard());
    }
}
