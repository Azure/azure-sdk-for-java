// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.avro;

import com.azure.data.schemaregistry.avro.generatedtestsources.PlayingCard;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link AvroSchemaRegistryUtils}.
 */
public class AvroSchemaRegistryUtilsTest {

    @Mock
    private EncoderFactory encoderFactory;

    @Mock
    private DecoderFactory decoderFactory;

    @Mock
    private Schema.Parser parser;

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
        final Schema expected = PlayingCard.getClassSchema();
        final GenericData.Array<PlayingCard> genericArray = new GenericData.Array<>(10, expected);

        // Act
        final String schemaString = registryUtils.getSchemaString(genericArray);
        final String fullName = registryUtils.getSchemaName(genericArray);

        // Assert
        assertEquals(expected.toString(), schemaString);
        assertEquals(expected.getFullName(), fullName);
    }

}
