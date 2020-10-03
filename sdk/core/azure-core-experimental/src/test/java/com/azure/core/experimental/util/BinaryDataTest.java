// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.util;

import com.azure.core.serializer.json.jackson.JacksonJsonSerializer;
import com.azure.core.serializer.json.jackson.JacksonJsonSerializerBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test class for {@link BinaryData}.
 */
public class BinaryDataTest {
    private static final JacksonJsonSerializer DEFAULT_SERIALIZER = new JacksonJsonSerializerBuilder().build();

    @MethodSource()
    @ParameterizedTest
    public void anyTypeToObject(Object actualValue, Object expectedValue) {
        final BinaryData data = BinaryData.fromObject(actualValue, DEFAULT_SERIALIZER);
        assertEquals(expectedValue, data.toObject(expectedValue.getClass(), DEFAULT_SERIALIZER));
    }

    @MethodSource()
    @ParameterizedTest
    public void anyTypeToByteArray(Object actualValue, byte[] expectedValue) {
        final BinaryData data = BinaryData.fromObject(actualValue, DEFAULT_SERIALIZER);
        assertArrayEquals(expectedValue, data.toBytes());
    }

    @Test
    public void createFromString() {
        final String expected = "Doe";
        final BinaryData data = BinaryData.fromString(expected);
        assertArrayEquals(expected.getBytes(), data.toBytes());
        assertEquals(expected, data.toString());
    }

    @Test
    public void createFromStringCharSet() {
        final String expected = "Doe";
        final BinaryData data = BinaryData.fromString(expected, StandardCharsets.UTF_8);
        assertArrayEquals(expected.getBytes(), data.toBytes());
        assertEquals(expected, data.toString(StandardCharsets.UTF_8));
    }

    @Test
    public void createFromByteArray() {
        final byte[] expected = "Doe".getBytes(StandardCharsets.UTF_8);
        final BinaryData data = new BinaryData(expected);
        assertArrayEquals(expected, data.toBytes());
    }

    @Test
    public void createFromStream() throws IOException {
        // Arrange
        final byte[] expected = "Doe".getBytes(StandardCharsets.UTF_8);
        final byte[] actual = new byte[expected.length];

        // Act
        BinaryData data = BinaryData.fromStream(new ByteArrayInputStream(expected));
        (data.toStream()).read(actual, 0, expected.length);

        // Assert
        assertArrayEquals(expected, data.toBytes());
        assertArrayEquals(expected, actual);
    }

    @Test
    public void createFromFlux() {
        // Arrange
        final byte[] data = "Doe".getBytes(StandardCharsets.UTF_8);
        final Flux<ByteBuffer> expectedFlux = Flux.just(ByteBuffer.wrap(data), ByteBuffer.wrap(data));
        final byte[] expected = "DoeDoe".getBytes(StandardCharsets.UTF_8);

        // Act & Assert
        StepVerifier.create(BinaryData.fromFlux(expectedFlux))
            .assertNext(actual -> {
                Assertions.assertArrayEquals(expected, actual.toBytes());
            })
            .verifyComplete();
    }

    @Test
    public void createFromStreamAsync() throws IOException {
        // Arrange
        final byte[] expected = "Doe".getBytes(StandardCharsets.UTF_8);

        // Act & Assert
        StepVerifier.create(BinaryData.fromStreamAsync(new ByteArrayInputStream(expected)))
            .assertNext(actual -> {
                Assertions.assertArrayEquals(expected, actual.toBytes());
            })
            .verifyComplete();
    }

    @Test
    public void createToStreamAsync() {
        // Arrange
        final byte[] expected = "Doe".getBytes(StandardCharsets.UTF_8);
        final BinaryData actual = BinaryData.fromStreamAsync(new ByteArrayInputStream(expected)).block();
        // Act & Assert
        StepVerifier.create(actual.toStreamAsync())
            .assertNext(inutStream -> {
                byte[] actualBytes = new byte[expected.length];

                // Act
                try {
                    inutStream.read(actualBytes, 0, expected.length);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Assertions.assertArrayEquals(expected, actualBytes);
            })
            .verifyComplete();
    }

    @Test
    public void createFromObjectAsync() {
        // Arrange
        final Person expected = new Person().setName("Jon").setAge(50);
        final BinaryData expectedBinaryData = BinaryData.fromObjectAsync(expected, DEFAULT_SERIALIZER).block();

        // Act & Assert
        StepVerifier.create(expectedBinaryData.toObjectAsync(Person.class, DEFAULT_SERIALIZER))
            .assertNext(actual -> {
                System.out.println(actual.getName());
                System.out.println(actual.getAge());
                Assertions.assertEquals(expected, actual);
            })
            .verifyComplete();
    }

    static Stream<Arguments> anyTypeToByteArray() {
        return Stream.of(
            Arguments.of(new Person().setName("John Doe"), "{\"name\":\"John Doe\",\"age\":0}".getBytes(StandardCharsets.UTF_8)),
            Arguments.of(new Person().setName("John Doe").setAge(50), "{\"name\":\"John Doe\",\"age\":50}".getBytes(StandardCharsets.UTF_8))
        );
    }

    static Stream<Arguments> anyTypeToObject() {
        return Stream.of(
            Arguments.of("10", "10"),
            Arguments.of(Long.valueOf("10"), Long.valueOf("10")),
            Arguments.of(Double.valueOf("10.1"), Double.valueOf("10.1")),
            Arguments.of(Boolean.TRUE, Boolean.TRUE),
            Arguments.of(new Person().setName("John Doe").setAge(50), new Person().setName("John Doe").setAge(50))
        );
    }
}
