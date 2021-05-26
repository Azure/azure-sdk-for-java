// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

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
import java.nio.ReadOnlyBufferException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class for {@link BinaryData}.
 */
public class BinaryDataTest {
    private static final ObjectSerializer CUSTOM_SERIALIZER = new MyJsonSerializer();

    @Test
    public void fromCustomObject() {
        // Arrange
        final Person actualValue = new Person().setName("John Doe").setAge(50);
        final Person expectedValue = new Person().setName("John Doe").setAge(50);

        // Act
        final BinaryData data = BinaryData.fromObject(actualValue, CUSTOM_SERIALIZER);

        // Assert
        assertEquals(expectedValue, data.toObject(TypeReference.createInstance(expectedValue.getClass()),
            CUSTOM_SERIALIZER));
    }

    @Test
    public void fromDouble() {
        // Arrange
        final Double actualValue = Double.valueOf("10.1");
        final Double expectedValue = Double.valueOf("10.1");

        // Act
        final BinaryData data = BinaryData.fromObject(actualValue, CUSTOM_SERIALIZER);

        // Assert
        assertEquals(expectedValue, data.toObject(TypeReference.createInstance(expectedValue.getClass()),
            CUSTOM_SERIALIZER));
    }

    @Test
    public void anyTypeToByteArray() {
        // Assert
        final Person actualValue = new Person().setName("John Doe").setAge(50);
        final byte[] expectedValue = "{\"name\":\"John Doe\",\"age\":50}".getBytes(StandardCharsets.UTF_8);

        // Act
        final BinaryData data = BinaryData.fromObject(actualValue, CUSTOM_SERIALIZER);

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
    public void createFromByteArray() {
        // Arrange
        final byte[] expected = "Doe".getBytes(StandardCharsets.UTF_8);

        // Act
        final BinaryData data = new BinaryData(expected);

        // Assert
        assertArrayEquals(expected, data.toBytes());
    }

    @Test
    public void createFromNullStream() throws IOException {
        // Arrange
        final byte[] expected = new byte[0];

        // Act
        BinaryData data = BinaryData.fromStream(null);
        final byte[] actual = new byte[0];
        data.toStream().read(actual, 0, expected.length);

        // Assert
        assertArrayEquals(expected, data.toBytes());
        assertArrayEquals(expected, actual);
    }

    @Test
    public void createFromNullByteArray() {
        // Arrange
        final byte[] expected = new byte[0];

        // Act
        BinaryData actual = BinaryData.fromBytes(null);

        // Assert
        assertArrayEquals(expected, actual.toBytes());
    }

    @Test
    public void createFromNullObject() {
        // Arrange
        final byte[] expected = new byte[0];

        // Act
        BinaryData actual = BinaryData.fromObject(null, null);

        // Assert
        assertArrayEquals(expected, actual.toBytes());
    }

    @Test
    public void createFromStream() throws IOException {
        // Arrange
        final byte[] expected = "Doe".getBytes(StandardCharsets.UTF_8);

        // Act
        BinaryData data = BinaryData.fromStream(new ByteArrayInputStream(expected));
        final byte[] actual = new byte[expected.length];
        (data.toStream()).read(actual, 0, expected.length);

        // Assert
        assertArrayEquals(expected, data.toBytes());
        assertArrayEquals(expected, actual);
    }

    @Test
    public void createFromEmptyStream() throws IOException {
        // Arrange
        final byte[] expected = "".getBytes();
        final byte[] actual = new byte[expected.length];

        // Act
        BinaryData data = BinaryData.fromStream(new ByteArrayInputStream(expected));
        data.toStream().read(actual, 0, expected.length);

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
            .assertNext(actual -> Assertions.assertArrayEquals(expected, actual.toBytes()))
            .verifyComplete();
    }

    @Test
    public void createFromStreamAsync() {
        // Arrange
        final byte[] expected = "Doe".getBytes(StandardCharsets.UTF_8);

        // Act & Assert
        StepVerifier.create(BinaryData.fromStreamAsync(new ByteArrayInputStream(expected)))
            .assertNext(actual -> Assertions.assertArrayEquals(expected, actual.toBytes()))
            .verifyComplete();
    }

    @Test
    public void createFromObjectAsync() {
        // Arrange
        final Person expected = new Person().setName("Jon").setAge(50);
        final TypeReference<Person> personTypeReference = TypeReference.createInstance(Person.class);

        // Act & Assert
        StepVerifier.create(BinaryData.fromObjectAsync(expected, CUSTOM_SERIALIZER)
            .flatMap(binaryData -> binaryData.toObjectAsync(personTypeReference, CUSTOM_SERIALIZER)))
            .assertNext(actual -> Assertions.assertEquals(expected, actual))
            .verifyComplete();
    }

    @Test
    public void createFromObjectAsyncWithGenerics() {
        // Arrange
        final Person person1 = new Person().setName("Jon").setAge(50);
        final Person person2 = new Person().setName("Jack").setAge(25);
        List<Person> personList = new ArrayList<>();
        personList.add(person1);
        personList.add(person2);
        final TypeReference<List<Person>> personListTypeReference = new TypeReference<List<Person>>() { };

        // Act & Assert
        StepVerifier.create(BinaryData.fromObjectAsync(personList, CUSTOM_SERIALIZER)
            .flatMap(binaryData -> binaryData.toObjectAsync(personListTypeReference, CUSTOM_SERIALIZER)))
            .assertNext(persons -> {
                assertEquals(2, persons.size());
                assertEquals("Jon", persons.get(0).getName());
                assertEquals("Jack", persons.get(1).getName());
                assertEquals(50, persons.get(0).getAge());
                assertEquals(25, persons.get(1).getAge());
            })
            .verifyComplete();
    }

    @Test
    public void createFromEmptyString() {
        // Arrange
        final String expected = "";

        // Act
        final BinaryData data = BinaryData.fromString(expected);

        // Assert
        assertArrayEquals(expected.getBytes(), data.toBytes());
        assertEquals(expected, data.toString());
    }

    @Test
    public void createFromEmptyByteArray() {
        // Arrange
        final byte[] expected = new byte[0];

        // Act
        final BinaryData data = BinaryData.fromBytes(expected);

        // Assert
        assertArrayEquals(expected, data.toBytes());
    }

    @Test
    public void createFromNullString() {
        // Arrange
        final String expected = null;

        // Arrange & Act
        final BinaryData data = BinaryData.fromString(expected);

        // Assert
        assertArrayEquals(new byte[0], data.toBytes());
        assertEquals("", data.toString());
    }

    @Test
    public void createFromNullByte() {
        // Arrange
        final byte[] expected = null;

        // Arrange & Act
        final BinaryData data = BinaryData.fromBytes(expected);

        // Assert
        assertArrayEquals(new byte[0], data.toBytes());
        assertEquals("", data.toString());
    }

    @Test
    public void toReadOnlyByteBufferThrowsOnMutation() {
        BinaryData binaryData = BinaryData.fromString("Hello");

        assertThrows(ReadOnlyBufferException.class, () -> binaryData.toByteBuffer().put((byte) 0));
    }

    @Test
    public void fromCustomObjectWithDefaultSerializer() {
        // Arrange
        final Person actualValue = new Person().setName("John Doe").setAge(50);
        final Person expectedValue = new Person().setName("John Doe").setAge(50);

        // Act
        final BinaryData data = BinaryData.fromObject(actualValue);

        // Assert
        assertEquals(expectedValue, data.toObject(TypeReference.createInstance(expectedValue.getClass())));
    }

    @Test
    public void fromDoubleWithDefaultSerializer() {
        // Arrange
        final Double actualValue = Double.valueOf("10.1");
        final Double expectedValue = Double.valueOf("10.1");

        // Act
        final BinaryData data = BinaryData.fromObject(actualValue);

        // Assert
        assertEquals(expectedValue, data.toObject(TypeReference.createInstance(expectedValue.getClass())));
    }

    @Test
    public void anyTypeToByteArrayWithDefaultSerializer() {
        // Assert
        final Person actualValue = new Person().setName("John Doe").setAge(50);
        final byte[] expectedValue = "{\"name\":\"John Doe\",\"age\":50}".getBytes(StandardCharsets.UTF_8);

        // Act
        final BinaryData data = BinaryData.fromObject(actualValue);

        // Assert
        assertArrayEquals(expectedValue, data.toBytes());
    }

    @Test
    public void createFromObjectAsyncWithDefaultSerializer() {
        // Arrange
        final Person expected = new Person().setName("Jon").setAge(50);

        // Act & Assert
        StepVerifier.create(BinaryData.fromObjectAsync(expected)
            .flatMap(binaryData -> binaryData.toObjectAsync(TypeReference.createInstance(Person.class))))
            .assertNext(actual -> Assertions.assertEquals(expected, actual))
            .verifyComplete();
    }

    @Test
    public void createFromObjectAsyncWithGenericsWithDefaultSerializer() {
        // Arrange
        final Person person1 = new Person().setName("Jon").setAge(50);
        final Person person2 = new Person().setName("Jack").setAge(25);
        List<Person> personList = new ArrayList<>();
        personList.add(person1);
        personList.add(person2);

        // Act & Assert
        StepVerifier.create(BinaryData.fromObjectAsync(personList)
            .flatMap(binaryData -> binaryData.toObjectAsync(new TypeReference<List<Person>>() { })))
            .assertNext(persons -> {
                assertEquals(2, persons.size());
                assertEquals("Jon", persons.get(0).getName());
                assertEquals("Jack", persons.get(1).getName());
                assertEquals(50, persons.get(0).getAge());
                assertEquals(25, persons.get(1).getAge());
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
