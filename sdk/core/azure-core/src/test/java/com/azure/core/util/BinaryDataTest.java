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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.azure.core.util.implementation.BinaryDataContent.STREAM_READ_SIZE;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

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
        final BinaryData data = BinaryData.fromBytes(expected);

        // Assert
        assertArrayEquals(expected, data.toBytes());
    }

    @Test
    public void createFromNullStream() throws IOException {
        assertThrows(NullPointerException.class, () -> BinaryData.fromStream(null));
    }

    @Test
    public void createFromNullByteArray() {
        assertThrows(NullPointerException.class, () -> BinaryData.fromBytes(null));
    }

    @Test
    public void createFromNullObject() {
        assertThrows(NullPointerException.class, () -> BinaryData.fromObject(null, null));
    }

    @Test
    public void createFromNullFile() {
        assertThrows(NullPointerException.class, () -> BinaryData.fromFile(null));
    }

    @Test
    public void createFromNullFlux() {
        StepVerifier.create(BinaryData.fromFlux(null))
                .verifyError(NullPointerException.class);
    }

    @Test
    public void createFromStream() throws IOException {
        // Arrange
        final byte[] expected = "Doe".getBytes(StandardCharsets.UTF_8);

        // Act
        BinaryData data = BinaryData.fromStream(new ByteArrayInputStream(expected));

        // Assert
        assertArrayEquals(expected, data.toBytes());
    }

    @Test
    public void createFromLargeStreamAndReadAsFlux() {
        // Arrange
        final byte[] expected = String.join("", Collections.nCopies(STREAM_READ_SIZE * 100, "A"))
                .concat("A").getBytes(StandardCharsets.UTF_8);

        // Act
        BinaryData data = BinaryData.fromStream(new ByteArrayInputStream(expected));

        // Assert
        StepVerifier.create(data.toFluxByteBuffer())
                // the inputstream should be broken down into a series of byte buffers, each of max CHUNK_SIZE
                // assert first chunk is equal to CHUNK_SIZE and is a string of repeating A's
                .assertNext(bb -> assertEquals(String.join("", Collections.nCopies(STREAM_READ_SIZE, "A")),
                        StandardCharsets.UTF_8.decode(bb).toString()))
                // skip 99 chunks
                .expectNextCount(99)
                // assert last chunk is just "A"
                .assertNext(bb -> assertEquals("A", StandardCharsets.UTF_8.decode(bb).toString()))
                .verifyComplete();
    }

    @Test
    public void createFromEmptyStream() throws IOException {
        // Arrange
        final byte[] expected = "".getBytes();

        // Act
        BinaryData data = BinaryData.fromStream(new ByteArrayInputStream(expected));

        // Assert
        assertArrayEquals(expected, data.toBytes());
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
        assertThrows(NullPointerException.class, () -> BinaryData.fromString(expected));
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

    @Test
    public void fileChannelOpenErrorReturnsReactively() {
        Path notARealPath = Paths.get("fake");
        assertThrows(UncheckedIOException.class, () -> BinaryData.fromFile(notARealPath));
    }

    @Test
    public void fileChannelCloseErrorReturnsReactively() throws IOException {
        MyFileChannel myFileChannel = spy(MyFileChannel.class);
        when(myFileChannel.map(any(), anyLong(), anyLong())).thenReturn(mock(MappedByteBuffer.class));
        doThrow(IOException.class).when(myFileChannel).implCloseChannel();

        FileSystemProvider fileSystemProvider = mock(FileSystemProvider.class);
        when(fileSystemProvider.newFileChannel(any(), any(), any())).thenReturn(myFileChannel);

        FileSystem fileSystem = mock(FileSystem.class);
        when(fileSystem.provider()).thenReturn(fileSystemProvider);

        Path path = mock(Path.class);
        when(path.getFileSystem()).thenReturn(fileSystem);
        File file = mock(File.class);
        when(file.length()).thenReturn(1024L);
        when(file.exists()).thenReturn(true);
        when(path.toFile()).thenReturn(file);

        BinaryData binaryData = BinaryData.fromFile(path);
        StepVerifier.create(binaryData.toFluxByteBuffer())
                .thenConsumeWhile(Objects::nonNull)
                .verifyError(IOException.class);
    }

    @Test
    public void fileChannelIsClosedWhenMapErrors() throws IOException {
        MyFileChannel myFileChannel = spy(MyFileChannel.class);
        when(myFileChannel.map(any(), anyLong(), anyLong())).thenThrow(IOException.class);

        FileSystemProvider fileSystemProvider = mock(FileSystemProvider.class);
        when(fileSystemProvider.newFileChannel(any(), any(), any())).thenReturn(myFileChannel);

        FileSystem fileSystem = mock(FileSystem.class);
        when(fileSystem.provider()).thenReturn(fileSystemProvider);

        Path path = mock(Path.class);
        when(path.getFileSystem()).thenReturn(fileSystem);
        File file = mock(File.class);
        when(file.length()).thenReturn(1024L);
        when(file.exists()).thenReturn(true);
        when(path.toFile()).thenReturn(file);

        BinaryData binaryData = BinaryData.fromFile(path);
        StepVerifier.create(binaryData.toFluxByteBuffer())
                .thenConsumeWhile(Objects::nonNull)
                .verifyError(IOException.class);

        assertFalse(myFileChannel.isOpen());
    }

    @Test
    public void fluxContent() {
        Mono<BinaryData> binaryDataMono = BinaryData.fromFlux(Flux
                .just(ByteBuffer.wrap("Hello".getBytes(StandardCharsets.UTF_8))).delayElements(Duration.ofMillis(10)));

        StepVerifier.create(binaryDataMono)
                .assertNext(binaryData -> assertEquals("Hello", new String(binaryData.toBytes())))
                .verifyComplete();
    }

    @Test
    public void testFromFile() throws URISyntaxException {
        URL res = getClass().getClassLoader().getResource("upload.txt");
        Path path = Paths.get(res.toURI());
        BinaryData data = BinaryData.fromFile(path);
        assertEquals("The quick brown fox jumps over the lazy dog", data.toString());
    }

    @Test
    public void testFromFileToFlux() throws URISyntaxException {
        URL res = getClass().getClassLoader().getResource("upload.txt");
        Path path = Paths.get(res.toURI());
        BinaryData data = BinaryData.fromFile(path);
        StepVerifier.create(data.toFluxByteBuffer())
                .assertNext(bb -> assertEquals("The quick brown fox jumps over the lazy dog",
                        StandardCharsets.UTF_8.decode(bb).toString()))
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
