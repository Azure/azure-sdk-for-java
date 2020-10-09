// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.util;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test class for {@link BinaryData}.
 */
public class BinaryDataTest {
    private static final ObjectSerializer DEFAULT_SERIALIZER = new MyJsonSerializer();

    @Test
    public void fromCustomObject() {
        // Arrange
        final Person actualValue = new Person().setName("John Doe").setAge(50);
        final Person expectedValue = new Person().setName("John Doe").setAge(50);

        // Act
        final BinaryData data = BinaryData.fromObject(actualValue, DEFAULT_SERIALIZER);

        // Assert
        assertEquals(expectedValue, data.toObject(expectedValue.getClass(), DEFAULT_SERIALIZER));
    }

    @Test
    public void fromDouble() {
        // Arrange
        final Double actualValue = Double.valueOf("10.1");
        final Double expectedValue = Double.valueOf("10.1");

        // Act
        final BinaryData data = BinaryData.fromObject(actualValue, DEFAULT_SERIALIZER);

        // Assert
        assertEquals(expectedValue, data.toObject(expectedValue.getClass(), DEFAULT_SERIALIZER));
    }

    @Test
    public void anyTypeToByteArray() {
        // Assert
        final Person actualValue = new Person().setName("John Doe").setAge(50);
        final byte[] expectedValue = "{\"name\":\"John Doe\",\"age\":50}".getBytes(StandardCharsets.UTF_8);

        // Act
        final BinaryData data = BinaryData.fromObject(actualValue, DEFAULT_SERIALIZER);

        // Assert
        assertArrayEquals(expectedValue, data.toBytes());
    }

    @Test
    public void createFromString() {
        // Arrange
        final String expected = "Doe";

        // Act
        final BinaryData data = BinaryData.fromString(expected);

        // Assert
        assertArrayEquals(expected.getBytes(), data.toBytes());
        assertEquals(expected, data.toString());
    }

    @Test
    public void createFromStringCharSet() {
        // Arrange
        final String expected = "Doe";

        // Act
        final BinaryData data = BinaryData.fromString(expected, StandardCharsets.UTF_8);

        // Assert
        assertArrayEquals(expected.getBytes(), data.toBytes());
        assertEquals(expected, data.toString(StandardCharsets.UTF_8));
    }

    @Test
    public void createFromByteArray() {
        // Arrange
        final byte[] expected = "Doe".getBytes(StandardCharsets.UTF_8);

        // Act
        final BinaryData data = new BinaryData(expected);

        // Assert
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

    public static class MyJsonSerializer implements JsonSerializer {
        private final ClientLogger logger = new ClientLogger(MyJsonSerializer.class);
        private final ObjectMapper mapper;
        private final TypeFactory typeFactory;

        public MyJsonSerializer() {
            this.mapper = new ObjectMapper();
            this.typeFactory = mapper.getTypeFactory();
        }

        @Override
        public <T> T deserialize(InputStream stream, TypeReference<T> typeReference) {
            if (stream == null) {
                return null;
            }

            try {
                return mapper.readValue(stream, typeFactory.constructType(typeReference.getJavaType()));
            } catch (IOException ex) {
                throw logger.logExceptionAsError(new UncheckedIOException(ex));
            }
        }

        @Override
        public <T> Mono<T> deserializeAsync(InputStream stream, TypeReference<T> typeReference) {
            return Mono.fromCallable(() -> deserialize(stream, typeReference));
        }


        @Override
        public void serialize(OutputStream stream, Object value) {
            try {
                mapper.writeValue(stream, value);
            } catch (IOException ex) {
                throw logger.logExceptionAsError(new UncheckedIOException(ex));
            }
        }

        @Override
        public Mono<Void> serializeAsync(OutputStream stream, Object value) {
            return Mono.fromRunnable(() -> serialize(stream, value));
        }
    }
}
