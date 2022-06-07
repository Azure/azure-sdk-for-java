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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.spi.FileSystemProvider;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static com.azure.core.implementation.util.BinaryDataContent.STREAM_READ_SIZE;
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
    private static final Random RANDOM = new Random();

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
        BinaryData binaryData = BinaryData.fromObject(null, BinaryData.SERIALIZER);
        Assertions.assertNull(binaryData.toBytes());
        Assertions.assertNull(binaryData.getLength());
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
    public void createFromFluxEagerly() {
        // Arrange
        final byte[] data = "Doe".getBytes(StandardCharsets.UTF_8);
        final Flux<ByteBuffer> dataFlux = Flux.defer(() -> Flux.just(ByteBuffer.wrap(data), ByteBuffer.wrap(data)));
        final byte[] expected = "DoeDoe".getBytes(StandardCharsets.UTF_8);
        final long length = expected.length;

        Arrays.asList(
            BinaryData.fromFlux(dataFlux),
            BinaryData.fromFlux(dataFlux, null),
            BinaryData.fromFlux(dataFlux, length),
            BinaryData.fromFlux(dataFlux, null, true),
            BinaryData.fromFlux(dataFlux, length, true)
        ).forEach(binaryDataMono -> {
            // Act & Assert
            StepVerifier.create(binaryDataMono)
                .assertNext(actual -> {
                    assertArrayEquals(expected, actual.toBytes());
                    assertEquals(expected.length, actual.getLength());
                })
                .verifyComplete();

            // Verify that data got buffered
            StepVerifier.create(binaryDataMono
                    .flatMapMany(BinaryData::toFluxByteBuffer)
                    .count())
                .assertNext(actual -> assertEquals(1, actual))
                .verifyComplete();
        });
    }

    @Test
    public void createFromFluxLazy() {
        final byte[] data = "Doe".getBytes(StandardCharsets.UTF_8);
        final Flux<ByteBuffer> dataFlux = Flux.defer(() -> Flux.just(ByteBuffer.wrap(data), ByteBuffer.wrap(data)));
        final byte[] expected = "DoeDoe".getBytes(StandardCharsets.UTF_8);

        // Act & Assert
        Arrays.asList((long) expected.length, null).forEach(
            providedLength -> {
                StepVerifier.create(BinaryData.fromFlux(dataFlux, providedLength, false))
                    .assertNext(actual -> {
                        assertArrayEquals(expected, actual.toBytes());
                        // toBytes buffers and reveals length
                        assertEquals(expected.length, actual.getLength());
                    })
                    .verifyComplete();

                StepVerifier.create(BinaryData.fromFlux(dataFlux, providedLength, false))
                    .assertNext(actual -> {
                        // Assert that length isn't computed eagerly.
                        assertEquals(providedLength, actual.getLength());
                    })
                    .verifyComplete();

                // Verify that data isn't buffered
                StepVerifier.create(BinaryData.fromFlux(dataFlux, providedLength, false)
                        .flatMapMany(BinaryData::toFluxByteBuffer)
                        .count())
                    .assertNext(actual -> assertEquals(2, actual))
                    .verifyComplete();
            }
        );
    }

    @Test
    public void createFromFluxValidations() {
        Stream.of(
            BinaryData.fromFlux(null),
            BinaryData.fromFlux(null, null),
            BinaryData.fromFlux(null, null, false),
            BinaryData.fromFlux(null, null, true)
        ).forEach(binaryDataMono -> StepVerifier.create(binaryDataMono)
            .expectError(NullPointerException.class)
            .verify());

        Stream.of(
            BinaryData.fromFlux(Flux.empty(), -1L),
            BinaryData.fromFlux(Flux.empty(), -1L, false),
            BinaryData.fromFlux(Flux.empty(), -1L, true),
            BinaryData.fromFlux(Flux.empty(), Integer.MAX_VALUE - 7L, true)
        ).forEach(binaryDataMono -> StepVerifier.create(binaryDataMono)
            .expectError(IllegalArgumentException.class)
            .verify());
    }

    @Test
    public void createFromStreamAsync() {
        // Arrange
        final byte[] expected = "Doe".getBytes(StandardCharsets.UTF_8);

        // Act & Assert
        StepVerifier.create(BinaryData.fromStreamAsync(new ByteArrayInputStream(expected)))
            .assertNext(actual -> assertArrayEquals(expected, actual.toBytes()))
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
            .assertNext(actual -> assertEquals(expected, actual))
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
            .assertNext(actual -> assertEquals(expected, actual))
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
    public void testFromFile() throws Exception {
        Path file = Files.createTempFile("binaryDataFromFile" + UUID.randomUUID(), ".txt");
        file.toFile().deleteOnExit();
        try (FileWriter fileWriter = new FileWriter(file.toFile())) {
            fileWriter.write("The quick brown fox jumps over the lazy dog");
        }
        BinaryData data = BinaryData.fromFile(file);
        assertEquals("The quick brown fox jumps over the lazy dog", data.toString());
    }

    @Test
    public void testFromLargeFileFlux() throws Exception {
        Path file = Files.createTempFile("binaryDataFromFile" + UUID.randomUUID(), ".txt");
        file.toFile().deleteOnExit();
        int chunkSize = 100 * 1024 * 1024; // 100 MB
        int numberOfChunks = 22; // 2200 MB total
        byte[] bytes = new byte[chunkSize];
        RANDOM.nextBytes(bytes);
        for (int i = 0 ; i < numberOfChunks; i++) {
            Files.write(file, bytes, StandardOpenOption.APPEND);
        }
        assertEquals((long) chunkSize * numberOfChunks, file.toFile().length());

        AtomicInteger index = new AtomicInteger();
        BinaryData.fromFile(file).toFluxByteBuffer()
            .map(buffer -> {
                while (buffer.hasRemaining()){
                    int idx = index.getAndUpdate(operand -> (operand + 1) % chunkSize);
                    assertEquals(bytes[idx], buffer.get());
                }
                return buffer;
            }).blockLast();
    }

    @Test
    public void testFromLargeFileStream() throws Exception {
        Path file = Files.createTempFile("binaryDataFromFile" + UUID.randomUUID(), ".txt");
        file.toFile().deleteOnExit();
        int chunkSize = 100 * 1024 * 1024; // 100 MB
        int numberOfChunks = 22; // 2200 MB total
        byte[] bytes = new byte[chunkSize];
        RANDOM.nextBytes(bytes);
        for (int i = 0 ; i < numberOfChunks; i++) {
            Files.write(file, bytes, StandardOpenOption.APPEND);
        }
        assertEquals((long) chunkSize * numberOfChunks, file.toFile().length());

        try(InputStream is = BinaryData.fromFile(file).toStream()) {
            int read;
            int idx = 0;
            while ((read = is.read()) >= 0) {
                assertEquals(bytes[idx], (byte) read);
                idx = (idx + 1) % chunkSize;
            }
        }
    }

    @Test
    public void testFromFileToFlux() throws Exception {
        Path file = Files.createTempFile("binaryDataFromFile" + UUID.randomUUID(), ".txt");
        file.toFile().deleteOnExit();
        try (FileWriter fileWriter = new FileWriter(file.toFile())) {
            fileWriter.write("The quick brown fox jumps over the lazy dog");
        }
        BinaryData data = BinaryData.fromFile(file);
        StepVerifier.create(data.toFluxByteBuffer())
                .assertNext(bb -> assertEquals("The quick brown fox jumps over the lazy dog",
                        StandardCharsets.UTF_8.decode(bb).toString()))
                .verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(ints = { 10, 113, 1024, 1024 + 113, 10 * 1024 * 1024 + 13 })
    public void testFromFileSegment(int size) throws Exception {
        int leftPadding = 10 * 1024 + 13;
        int rightPadding = 10 * 1024 + 27;
        byte[] fullFile = new byte[size + leftPadding + rightPadding];
        RANDOM.nextBytes(fullFile);
        byte[] expectedBytes = Arrays.copyOfRange(fullFile, leftPadding, size + leftPadding);
        Path file = Files.createTempFile("binaryDataFromFileSegment" + UUID.randomUUID(), ".txt");
        file.toFile().deleteOnExit();
        Files.write(file, fullFile);

        assertEquals(size, BinaryData.fromFile(file, (long) leftPadding, (long) size).getLength());
        assertArrayEquals(expectedBytes, BinaryData.fromFile(file, (long) leftPadding, (long) size).toBytes());
        assertArrayEquals(expectedBytes,
            FluxUtil.collectBytesInByteBufferStream(
                BinaryData.fromFile(file, (long) leftPadding, (long) size).toFluxByteBuffer()).block());

        ByteArrayOutputStream bos = new ByteArrayOutputStream(size);
        try (InputStream is = BinaryData.fromFile(file, (long) leftPadding, (long) size).toStream()) {
            int nRead;
            byte[] buffer = new byte[1024];
            while ((nRead = is.read(buffer, 0, buffer.length)) != -1) {
                bos.write(buffer, 0, nRead);
            }
        }
        assertArrayEquals(expectedBytes, bos.toByteArray());

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
