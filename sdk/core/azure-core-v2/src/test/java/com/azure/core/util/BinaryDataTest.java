// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.v2.implementation.util.BinaryDataContent;
import com.azure.core.v2.implementation.util.BinaryDataHelper;
import com.azure.core.v2.implementation.util.FileContent;
import com.azure.core.v2.implementation.util.FluxByteBufferContent;
import com.azure.core.v2.implementation.util.IterableOfByteBuffersInputStream;
import com.azure.core.v2.implementation.util.MyFileContent;
import io.clientcore.core.util.ClientLogger;
import com.azure.core.v2.util.mocking.MockAsynchronousFileChannel;
import com.azure.core.v2.util.mocking.MockFile;
import com.azure.core.v2.util.mocking.MockFileContent;
import com.azure.core.v2.util.mocking.MockFileInputStream;
import com.azure.core.v2.util.mocking.MockPath;
import com.azure.core.v2.util.serializer.JacksonAdapter;
import com.azure.core.v2.util.serializer.JsonSerializer;
import com.azure.core.v2.util.serializer.ObjectSerializer;
import com.azure.core.v2.util.serializer.SerializerEncoding;
import com.azure.core.v2.util.serializer.TypeReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;

import reactor.core.publisher.SynchronousSink;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.azure.core.CoreTestUtils.assertArraysEqual;
import static com.azure.core.CoreTestUtils.fillArray;
import static com.azure.core.CoreTestUtils.readStream;
import static com.azure.core.v2.implementation.util.BinaryDataContent.STREAM_READ_SIZE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for {@link BinaryData}.
 */
public class BinaryDataTest {
    private static final ObjectSerializer CUSTOM_SERIALIZER = new MyJsonSerializer();
    private static final byte[] RANDOM_DATA;

    static {
        RANDOM_DATA = new byte[1024 * 1024]; // 1 MB
        fillArray(RANDOM_DATA);
    }

    @Test
    public void fromCustomObject() {
        // Arrange
        final Person actualValue = new Person().setName("John Doe").setAge(50);
        final Person expectedValue = new Person().setName("John Doe").setAge(50);

        // Act
        final BinaryData data = BinaryData.fromObject(actualValue, CUSTOM_SERIALIZER);

        // Assert
        assertEquals(expectedValue,
            data.toObject(TypeReference.createInstance(expectedValue.getClass()), CUSTOM_SERIALIZER));
    }

    @Test
    public void fromDouble() {
        // Arrange
        final Double actualValue = Double.valueOf("10.1");
        final Double expectedValue = Double.valueOf("10.1");

        // Act
        final BinaryData data = BinaryData.fromObject(actualValue, CUSTOM_SERIALIZER);

        // Assert
        assertEquals(expectedValue,
            data.toObject(TypeReference.createInstance(expectedValue.getClass()), CUSTOM_SERIALIZER));
    }

    @Test
    public void anyTypeToByteArray() {
        // Assert
        final Person actualValue = new Person().setName("John Doe").setAge(50);
        final byte[] expectedValue = "{\"name\":\"John Doe\",\"age\":50}".getBytes(StandardCharsets.UTF_8);

        // Act
        final BinaryData data = BinaryData.fromObject(actualValue, CUSTOM_SERIALIZER);

        // Assert
        assertArraysEqual(expectedValue, data.toBytes());
    }

    @Test
    public void createFromString() {
        // Arrange
        final String expected = "Doe";

        // Act
        final BinaryData data = BinaryData.fromString(expected);

        // Assert
        assertArraysEqual(expected.getBytes(), data.toBytes());
        assertEquals(expected, data.toString());
    }

    @Test
    public void createFromByteArray() {
        // Arrange
        final byte[] expected = "Doe".getBytes(StandardCharsets.UTF_8);

        // Act
        final BinaryData data = BinaryData.fromBytes(expected);

        // Assert
        assertArraysEqual(expected, data.toBytes());
    }

    @Test
    public void createFromNullStream() {
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
        StepVerifier.create(BinaryData.fromFlux(null)).verifyError(NullPointerException.class);
    }

    @Test
    public void createFromStream() {
        // Arrange
        final byte[] expected = "Doe".getBytes(StandardCharsets.UTF_8);

        // Act
        BinaryData data = BinaryData.fromStream(new ByteArrayInputStream(expected));

        // Assert
        assertArraysEqual(expected, data.toBytes());
    }

    @Test
    public void createFromLargeStreamAndReadAsFlux() {
        // Arrange
        final byte[] expected = String.join("", Collections.nCopies(STREAM_READ_SIZE * 100, "A"))
            .concat("A")
            .getBytes(StandardCharsets.UTF_8);

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
    public void createFromEmptyStream() {
        // Arrange
        final byte[] expected = "".getBytes();

        // Act
        BinaryData data = BinaryData.fromStream(new ByteArrayInputStream(expected));

        // Assert
        assertArraysEqual(expected, data.toBytes());
    }

    @ParameterizedTest
    @MethodSource("createFromFluxEagerlySupplier")
    public void createFromFluxEagerly(BinaryData> binaryDataMono, byte[] expectedBytes, int expectedCount) {
        StepVerifier.create(binaryDataMono).assertNext(actual -> {
            assertArraysEqual(expectedBytes, actual.toBytes());
            assertEquals(expectedBytes.length, actual.getLength());
        }).verifyComplete();

        // Verify that the data got buffered.
        StepVerifier.create(binaryDataMono.flatMapMany(BinaryData::toFluxByteBuffer).count())
            .assertNext(actualCount -> assertEquals(expectedCount, actualCount))
            .verifyComplete();
    }

    private static Stream<Arguments> createFromFluxEagerlySupplier() {
        final byte[] data = "Doe".getBytes(StandardCharsets.UTF_8);
        final Flux<ByteBuffer> dataFlux = Flux.defer(() -> Flux.just(ByteBuffer.wrap(data), ByteBuffer.wrap(data)));
        final byte[] expected = "DoeDoe".getBytes(StandardCharsets.UTF_8);
        final long length = expected.length;

        return Stream.of(Arguments.of(BinaryData.fromFlux(dataFlux), expected, 2),
            Arguments.of(BinaryData.fromFlux(dataFlux, null), expected, 2),
            Arguments.of(BinaryData.fromFlux(dataFlux, length), expected, 2),
            Arguments.of(BinaryData.fromFlux(dataFlux, null, true), expected, 2),
            Arguments.of(BinaryData.fromFlux(dataFlux, length, true), expected, 2));
    }

    @Test
    public void createFromFluxLazy() {
        final byte[] data = "Doe".getBytes(StandardCharsets.UTF_8);
        final Flux<ByteBuffer> dataFlux = Flux.defer(() -> Flux.just(ByteBuffer.wrap(data), ByteBuffer.wrap(data)));
        final byte[] expected = "DoeDoe".getBytes(StandardCharsets.UTF_8);

        // Act & Assert
        Arrays.asList((long) expected.length, null).forEach(providedLength -> {
            StepVerifier.create(BinaryData.fromFlux(dataFlux, providedLength, false)).assertNext(actual -> {
                assertArraysEqual(expected, actual.toBytes());
                // toBytes buffers and reveals length
                assertEquals(expected.length, actual.getLength());
            }).verifyComplete();

            StepVerifier.create(BinaryData.fromFlux(dataFlux, providedLength, false)).assertNext(actual -> {
                // Assert that length isn't computed eagerly.
                assertEquals(providedLength, actual.getLength());
            }).verifyComplete();

            // Verify that data isn't buffered
            StepVerifier.create(
                BinaryData.fromFlux(dataFlux, providedLength, false).flatMapMany(BinaryData::toFluxByteBuffer).count())
                .assertNext(actual -> assertEquals(2, actual))
                .verifyComplete();
        });
    }

    @ParameterizedTest
    @MethodSource("createFromFluxValidationsSupplier")
    public void createFromFluxValidations(Flux<ByteBuffer> flux, Long length, Boolean buffer,
        Class<? extends Throwable> expectedException) {
        if (length == null && buffer == null) {
            StepVerifier.create(BinaryData.fromFlux(flux)).expectError(expectedException).verify();
        } else if (buffer == null) {
            StepVerifier.create(BinaryData.fromFlux(flux, length)).expectError(expectedException).verify();
        } else {
            StepVerifier.create(BinaryData.fromFlux(flux, length, buffer)).expectError(expectedException).verify();
        }
    }

    private static Stream<Arguments> createFromFluxValidationsSupplier() {
        return Stream.of(Arguments.of(null, null, null, RuntimeException.class),
            Arguments.of(null, null, false, RuntimeException.class),
            Arguments.of(null, null, true, RuntimeException.class),

            Arguments.of(Flux.empty(), -1L, null, IllegalArgumentException.class),
            Arguments.of(Flux.empty(), -1L, false, IllegalArgumentException.class),
            Arguments.of(Flux.empty(), -1L, true, IllegalArgumentException.class));
    }

    @Test
    public void createFromStreamAsync() {
        // Arrange
        final byte[] expected = "Doe".getBytes(StandardCharsets.UTF_8);

        // Act & Assert
        StepVerifier.create(BinaryData.fromStreamAsync(new ByteArrayInputStream(expected)))
            .assertNext(actual -> assertArraysEqual(expected, actual.toBytes()))
            .verifyComplete();
    }

    @Test
    public void createFromObjectAsync() {
        // Arrange
        final Person expected = new Person().setName("Jon").setAge(50);
        final TypeReference<Person> personTypeReference = TypeReference.createInstance(Person.class);

        // Act & Assert
        StepVerifier
            .create(BinaryData.fromObjectAsync(expected, CUSTOM_SERIALIZER)
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
        final TypeReference<List<Person>> personListTypeReference = new TypeReference<List<Person>>() {
        };

        // Act & Assert
        StepVerifier
            .create(BinaryData.fromObjectAsync(personList, CUSTOM_SERIALIZER)
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
        assertArraysEqual(expected.getBytes(), data.toBytes());
        assertEquals(expected, data.toString());
    }

    @Test
    public void createFromEmptyByteArray() {
        // Arrange
        final byte[] expected = new byte[0];

        // Act
        final BinaryData data = BinaryData.fromBytes(expected);

        // Assert
        assertArraysEqual(expected, data.toBytes());
    }

    @Test
    public void createFromNullString() {
        // Arrange
        final String expected = null;

        // Arrange & Act
        assertThrows(NullPointerException.class, () -> BinaryData.fromString(expected));
    }

    @Test
    public void createFromListByteBuffer() {

        final byte[] data = "Doe".getBytes(StandardCharsets.UTF_8);
        final List<ByteBuffer> list = Arrays.asList(ByteBuffer.wrap(data), ByteBuffer.wrap(data));
        final byte[] expected = "DoeDoe".getBytes(StandardCharsets.UTF_8);

        BinaryData binaryData = BinaryData.fromListByteBuffer(list);
        assertArraysEqual(expected, binaryData.toBytes());
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
        assertArraysEqual(expectedValue, data.toBytes());
    }

    @Test
    public void createFromObjectAsyncWithDefaultSerializer() {
        // Arrange
        final Person expected = new Person().setName("Jon").setAge(50);

        // Act & Assert
        StepVerifier
            .create(BinaryData.fromObjectAsync(expected)
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
            .flatMap(binaryData -> binaryData.toObjectAsync(new TypeReference<List<Person>>() {
            }))).assertNext(persons -> {
                assertEquals(2, persons.size());
                assertEquals("Jon", persons.get(0).getName());
                assertEquals("Jack", persons.get(1).getName());
                assertEquals(50, persons.get(0).getAge());
                assertEquals(25, persons.get(1).getAge());
            }).verifyComplete();
    }

    @Test
    public void fileChannelOpenErrorReturnsReactively() {
        Path notARealPath = Paths.get("fake");
        assertThrows(UncheckedIOException.class, () -> BinaryData.fromFile(notARealPath));
    }

    @Test
    public void fileChannelCloseErrorReturnsReactively() {
        AtomicInteger closeCalls = new AtomicInteger();
        AsynchronousFileChannel myFileChannel = new MockAsynchronousFileChannel() {
            @Override
            public <A> void read(ByteBuffer dst, long position, A attachment,
                CompletionHandler<Integer, ? super A> handler) {
                // -1 means EOF.
                handler.completed(-1, attachment);
            }

            @Override
            public void close() throws IOException {
                closeCalls.incrementAndGet();
                throw new IOException("kaboom");
            }
        };

        FileContent fileContent = new MyFileContent(null, 8192, 0, 1024) {
            @Override
            public AsynchronousFileChannel openAsynchronousFileChannel() {
                return myFileChannel;
            }
        };

        BinaryData binaryData = BinaryDataHelper.createBinaryData(fileContent);
        StepVerifier.create(binaryData.toFluxByteBuffer())
            .thenConsumeWhile(Objects::nonNull)
            .verifyErrorMatches(t -> t instanceof IOException && t.getMessage().equals("kaboom"));
        assertEquals(1, closeCalls.get());
    }

    @Test
    public void fileChannelIsClosedWhenReadErrors() {
        AtomicInteger closeCalls = new AtomicInteger();
        AsynchronousFileChannel myFileChannel = new MockAsynchronousFileChannel() {
            @Override
            public <A> void read(ByteBuffer dst, long position, A attachment,
                CompletionHandler<Integer, ? super A> handler) {
                handler.failed(new IOException("kaboom"), attachment);
            }

            @Override
            public void close() {
                closeCalls.incrementAndGet();
            }
        };

        FileContent fileContent = new MyFileContent(null, 8192, 0, 1024) {
            @Override
            public AsynchronousFileChannel openAsynchronousFileChannel() {
                return myFileChannel;
            }
        };

        BinaryData binaryData = BinaryDataHelper.createBinaryData(fileContent);
        StepVerifier.create(binaryData.toFluxByteBuffer())
            .thenConsumeWhile(Objects::nonNull)
            .verifyErrorMatches(t -> t instanceof IOException && t.getMessage().equals("kaboom"));

        assertEquals(1, closeCalls.get());
    }

    @Test
    public void fluxContent() {
        BinaryData> binaryDataMono = BinaryData.fromFlux(
            Flux.just(ByteBuffer.wrap("Hello".getBytes(StandardCharsets.UTF_8))).delayElements(Duration.ofMillis(10)));

        StepVerifier.create(binaryDataMono)
            .assertNext(binaryData -> assertEquals("Hello", new String(binaryData.toBytes())))
            .verifyComplete();
    }

    @Test
    public void testFromFile() throws Exception {
        Path file = Files.createTempFile("binaryDataFromFile" + UUID.randomUUID(), ".txt");
        file.toFile().deleteOnExit();

        Files.write(file, "The quick brown fox jumps over the lazy dog".getBytes(StandardCharsets.UTF_8));
        BinaryData data = BinaryData.fromFile(file);
        assertEquals("The quick brown fox jumps over the lazy dog", data.toString());
    }

    @Test
    public void testFromLargeFileFlux() throws Exception {
        int chunkSize = 1024 * 1024; // 1 MB
        long numberOfChunks = 2200L; // 2200 MB total;

        MockFile mockFile
            = new MockFile("binaryDataFromFile" + UUID.randomUUID() + ".txt", RANDOM_DATA, numberOfChunks * chunkSize);
        FileContent fileContent = new MockFileContent(new MockPath(mockFile), 32768, null, null);

        AtomicInteger index = new AtomicInteger();
        AtomicLong totalRead = new AtomicLong();

        StepVerifier.create(fileContent.toFluxByteBuffer()).thenConsumeWhile(byteBuffer -> {
            totalRead.addAndGet(byteBuffer.remaining());
            int idx = index.getAndUpdate(operand -> (operand + byteBuffer.remaining()) % chunkSize);

            // This may look a bit odd but ByteBuffer has array-based comparison optimizations that aren't available
            // in Arrays until Java 9+. Wrapping the bytes chunk that was expected to be read and the read range
            // will allow for many bytes to be validated at once instead of byte-by-byte.
            assertEquals(ByteBuffer.wrap(RANDOM_DATA, idx, byteBuffer.remaining()), byteBuffer);
            return true;
        }).verifyComplete();

        assertEquals((long) chunkSize * numberOfChunks, totalRead.get());
    }

    @Test
    public void testFromLargeFileStream() throws Exception {
        int chunkSize = 1024 * 1024; // 1 MB
        long numberOfChunks = 2200L; // 2200 MB total

        MockFile mockFile
            = new MockFile("binaryDataFromFile" + UUID.randomUUID() + ".txt", RANDOM_DATA, numberOfChunks * chunkSize);
        FileContent fileContent = new MockFileContent(new MockPath(mockFile), 32768, null, null);

        try (InputStream is = fileContent.toStream()) {
            // Read and validate in chunks to optimize validation compared to byte-by-byte checking.
            byte[] buffer = new byte[4096];
            long totalRead = 0;
            int read;
            int idx = 0;
            while ((read = is.read(buffer)) >= 0) {
                totalRead += read;

                // This may look a bit odd but ByteBuffer has array-based comparison optimizations that aren't available
                // in Arrays until Java 9+. Wrapping the bytes chunk that was expected to be read and the read range
                // will allow for many bytes to be validated at once instead of byte-by-byte.
                assertEquals(ByteBuffer.wrap(RANDOM_DATA, idx, read), ByteBuffer.wrap(buffer, 0, read));

                idx = (idx + read) % chunkSize;
            }

            assertEquals((long) chunkSize * numberOfChunks, totalRead);
        }
    }

    @Test
    public void testFromFileToFlux() throws Exception {
        Path file = Files.createTempFile("binaryDataFromFile" + UUID.randomUUID(), ".txt");
        file.toFile().deleteOnExit();

        Files.write(file, "The quick brown fox jumps over the lazy dog".getBytes(StandardCharsets.UTF_8));
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
        fillArray(fullFile);

        MockFile mockFile
            = new MockFile("binaryDataFromFileSegment" + UUID.randomUUID() + ".txt", fullFile, fullFile.length);
        FileContent fileContent = new MockFileContent(new MockPath(mockFile), 8192, (long) leftPadding, (long) size);

        assertEquals(size, fileContent.getLength());

        byte[] actualBytes = fileContent.toBytes();
        assertArraysEqual(fullFile, leftPadding, size, actualBytes, actualBytes.length);

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(fileContent.toFluxByteBuffer(), size))
            .assertNext(actual -> assertArraysEqual(fullFile, leftPadding, size, actual, actual.length))
            .verifyComplete();

        actualBytes = new byte[size];
        try (InputStream is = fileContent.toStream()) {
            int totalRead = 0;
            int nRead;
            byte[] buffer = new byte[4096];
            while ((nRead = is.read(buffer, 0, buffer.length)) != -1) {
                System.arraycopy(buffer, 0, actualBytes, totalRead, nRead);
                totalRead += nRead;
            }
        }

        assertArraysEqual(fullFile, leftPadding, size, actualBytes, actualBytes.length);
    }

    @ParameterizedTest
    @MethodSource("createNonRetryableBinaryData")
    public void testNonReplayableContentTypes(Supplier<BinaryData> binaryDataSupplier) throws IOException {

        assertFalse(binaryDataSupplier.get().isReplayable());

        BinaryData data = binaryDataSupplier.get();
        byte[] firstFluxConsumption = FluxUtil.collectBytesInByteBufferStream(data.toFluxByteBuffer()).block();
        byte[] secondFluxConsumption = FluxUtil.collectBytesInByteBufferStream(data.toFluxByteBuffer()).block();

        data = binaryDataSupplier.get();
        byte[] firstStreamConsumption = readStream(data.toStream());
        byte[] secondStreamConsumption = readStream(data.toStream());

        // Either flux or stream consumption is not replayable.
        assertFalse(Arrays.equals(firstFluxConsumption, secondFluxConsumption)
            && Arrays.equals(firstStreamConsumption, secondStreamConsumption));
    }

    public static Stream<Arguments> createNonRetryableBinaryData() {
        byte[] bytes = new byte[1024];
        fillArray(bytes);
        return Stream.of(
            Arguments.of(Named.named("stream",
                (Supplier<BinaryData>) () -> BinaryData.fromStream(new ByteArrayInputStream(bytes)))),
            Arguments.of(Named.named("unbuffered flux",
                (Supplier<BinaryData>) () -> BinaryData.fromFlux(Flux.just(ByteBuffer.wrap(bytes)), null, false)
                    .block())),
            Arguments.of(Named.named("byte array stream",
                (Supplier<BinaryData>) () -> BinaryData.fromStream(new ByteArrayInputStream(bytes), null))));
    }

    @ParameterizedTest
    @MethodSource("createRetryableBinaryData")
    public void testReplayableContentTypes(Supplier<BinaryData> binaryDataSupplier, byte[] expectedBytes)
        throws IOException {

        assertTrue(binaryDataSupplier.get().isReplayable());

        // Check toFluxByteBuffer consumption
        BinaryData data = binaryDataSupplier.get();
        byte[] firstConsumption = FluxUtil.collectBytesInByteBufferStream(data.toFluxByteBuffer()).block();
        byte[] secondConsumption = FluxUtil.collectBytesInByteBufferStream(data.toFluxByteBuffer()).block();
        assertArraysEqual(firstConsumption, secondConsumption);
        assertArraysEqual(expectedBytes, firstConsumption);

        // Check toStream consumption
        data = binaryDataSupplier.get();
        firstConsumption = readStream(data.toStream());
        secondConsumption = readStream(data.toStream());
        assertArraysEqual(firstConsumption, secondConsumption);
        assertArraysEqual(expectedBytes, firstConsumption);

        // Check toByteBuffer consumption
        data = binaryDataSupplier.get();
        firstConsumption = readByteBuffer(data.toByteBuffer());
        secondConsumption = readByteBuffer(data.toByteBuffer());
        assertArraysEqual(firstConsumption, secondConsumption);
        assertArraysEqual(expectedBytes, firstConsumption);

        // Check toBytes consumption
        data = binaryDataSupplier.get();
        firstConsumption = data.toBytes();
        secondConsumption = data.toBytes();
        assertArraysEqual(firstConsumption, secondConsumption);
        assertArraysEqual(expectedBytes, firstConsumption);

        // Check that attempt to make repeatable returns itself.
        data = binaryDataSupplier.get();
        BinaryData clone = data.toReplayableBinaryData();
        assertSame(data, clone);

        data = binaryDataSupplier.get();
        clone = data.toReplayableBinaryDataAsync().block();
        assertSame(data, clone);
    }

    public static Stream<Arguments> createRetryableBinaryData() {
        byte[] bytes = new byte[1024];
        fillArray(bytes);

        MockFile mockFile = new MockFile("binaryDataFromFile" + UUID.randomUUID() + ".txt", bytes, 1024);

        return Stream.of(
            Arguments.of(Named.named("bytes", (Supplier<BinaryData>) () -> BinaryData.fromBytes(bytes)),
                Named.named("expected bytes", bytes)),
            Arguments.of(Named.named("string", (Supplier<BinaryData>) () -> BinaryData.fromString("test string")),
                Named.named("expected bytes", "test string".getBytes(StandardCharsets.UTF_8))),
            Arguments.of(Named.named("object", (Supplier<BinaryData>) () -> BinaryData.fromObject("\"test string\"")),
                Named.named("expected bytes", BinaryData.SERIALIZER.serializeToBytes("\"test string\""))),
            Arguments.of(
                Named.named("file",
                    (Supplier<BinaryData>) () -> new BinaryData(new MockFileContent(new MockPath(mockFile)))),
                Named.named("expected bytes", bytes)),
            Arguments.of(
                Named.named("buffered flux",
                    (Supplier<BinaryData>) () -> BinaryData.fromFlux(Flux.just(ByteBuffer.wrap(bytes))).block()),
                Named.named("expected bytes", bytes)),
            Arguments.of(
                Named.named("byte array stream", (Supplier<BinaryData>) () -> BinaryData
                    .fromStream(new ByteArrayInputStream(bytes), (long) bytes.length)),
                Named.named("expected bytes", bytes)));
    }

    @Test
    public void testMakeSmallMarkableStreamReplayable() throws IOException {
        byte[] bytes = new byte[1024];
        fillArray(bytes);

        // Delegate to testReplayableContentTypes to assert accessors replayability
        testReplayableContentTypes(
            () -> BinaryData.fromStream(new ByteArrayInputStream(bytes), (long) bytes.length).toReplayableBinaryData(),
            bytes);
        testReplayableContentTypes(() -> BinaryData.fromStream(new ByteArrayInputStream(bytes), (long) bytes.length)
            .toReplayableBinaryDataAsync()
            .block(), bytes);

        // When using markable stream
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        assertSame(byteArrayInputStream,
            BinaryData.fromStream(byteArrayInputStream, (long) bytes.length).toReplayableBinaryData().toStream());
        assertSame(byteArrayInputStream,
            BinaryData.fromStream(byteArrayInputStream, (long) bytes.length)
                .toReplayableBinaryDataAsync()
                .block()
                .toStream());
    }

    @Test
    public void testMakeUnknownLengthMarkableStreamReplayable() throws IOException {
        byte[] bytes = new byte[1024];
        fillArray(bytes);

        // Delegate to testReplayableContentTypes to assert accessors replayability
        testReplayableContentTypes(
            () -> BinaryData.fromStream(new ByteArrayInputStream(bytes)).toReplayableBinaryData(), bytes);
        testReplayableContentTypes(
            () -> BinaryData.fromStream(new ByteArrayInputStream(bytes)).toReplayableBinaryDataAsync().block(), bytes);

        // When using markable stream
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        assertNotSame(byteArrayInputStream,
            BinaryData.fromStream(byteArrayInputStream).toReplayableBinaryData().toStream());
        assertNotSame(byteArrayInputStream,
            BinaryData.fromStream(byteArrayInputStream).toReplayableBinaryDataAsync().block().toStream());

        // Check that buffering happened. This is part assumes implementation.
        assertInstanceOf(IterableOfByteBuffersInputStream.class,
            BinaryData.fromStream(byteArrayInputStream).toReplayableBinaryData().toStream());
        assertInstanceOf(IterableOfByteBuffersInputStream.class,
            BinaryData.fromStream(byteArrayInputStream).toReplayableBinaryDataAsync().block().toStream());
    }

    @ParameterizedTest
    // Try various sizes. That hit MIN and MAX buffers size in the InputStreamContent
    @ValueSource(
        ints = {
            10,
            1024,
            8 * 1024 - 1,
            8 * 1024 + 113,
            4 * 1024 * 1024 + 117,
            8 * 1024 * 1024,
            8 * 1024 * 1024 + 117,
            64 * 1024 * 1024 + 117 })
    public void testCanBufferNotMarkableStreams(int size) throws IOException {
        byte[] bytes = new byte[size];
        fillArray(bytes);

        MockFile mockFile = new MockFile("binaryDataFromFile" + UUID.randomUUID() + ".txt", bytes, size);

        // Delegate to testReplayableContentTypes to assert accessors replayability
        // with unknown length
        testReplayableContentTypes(
            () -> new BinaryData(new MockFileContent(new MockPath(mockFile))).toReplayableBinaryData(), bytes);
        testReplayableContentTypes(
            () -> new BinaryData(new MockFileContent(new MockPath(mockFile))).toReplayableBinaryDataAsync().block(),
            bytes);

        // with known length
        testReplayableContentTypes(
            () -> new BinaryData(new MockFileContent(new MockPath(mockFile), 8192, null, (long) bytes.length))
                .toReplayableBinaryData(),
            bytes);
        testReplayableContentTypes(
            () -> new BinaryData(new MockFileContent(new MockPath(mockFile), 8192, null, (long) bytes.length))
                .toReplayableBinaryDataAsync()
                .block(),
            bytes);

        // When using markable stream
        FileInputStream fileInputStream = new MockFileInputStream(mockFile);
        assertFalse(fileInputStream.markSupported());
        assertNotSame(fileInputStream, BinaryData.fromStream(fileInputStream).toReplayableBinaryData().toStream());
        assertNotSame(fileInputStream,
            BinaryData.fromStream(fileInputStream).toReplayableBinaryDataAsync().block().toStream());

        // Check that buffering happened. This is part assumes implementation.
        assertInstanceOf(IterableOfByteBuffersInputStream.class,
            BinaryData.fromStream(fileInputStream).toReplayableBinaryData().toStream());
        assertInstanceOf(IterableOfByteBuffersInputStream.class,
            BinaryData.fromStream(fileInputStream).toReplayableBinaryDataAsync().block().toStream());
    }

    @Test
    public void testMakeColdFluxReplayable() throws IOException {
        byte[] bytes = new byte[32 * 1024 * 1024 + 113]; // go big, more than chunk size.
        fillArray(bytes);

        Supplier<Flux<ByteBuffer>> coldFluxSupplier = createColdFluxSupplier(bytes, 1024);

        testReplayableContentTypes(() -> BinaryData.fromFlux(coldFluxSupplier.get(), null, false)
            .map(BinaryData::toReplayableBinaryData)
            .block(), bytes);

        testReplayableContentTypes(() -> BinaryData.fromFlux(coldFluxSupplier.get(), null, false)
            .flatMap(BinaryData::toReplayableBinaryDataAsync)
            .block(), bytes);
    }

    @Test
    public void testCachesBufferedFluxContent() {

        Flux<ByteBuffer> flux = Flux.empty();
        FluxByteBufferContent content = new FluxByteBufferContent(flux);

        BinaryDataContent replayableContent1 = content.toReplayableContent();
        BinaryDataContent replayableContent2 = content.toReplayableContent();

        assertSame(replayableContent1, replayableContent2);
    }

    @Test
    public void testMultipleSubscriptionsToReplayableFlux() {
        byte[] bytes = new byte[32 * 1024 * 1024 + 113]; // go big, more than chunk size.
        fillArray(bytes);

        Supplier<Flux<ByteBuffer>> coldFluxSupplier = createColdFluxSupplier(bytes, 1024);
        FluxByteBufferContent content = new FluxByteBufferContent(coldFluxSupplier.get());

        StepVerifier.create(Flux.range(0, 100)
            .parallel()
            .flatMap(
                ignored -> FluxUtil.collectBytesInByteBufferStream(content.toReplayableContent().toFluxByteBuffer()))
            .map(actualBytes -> {
                assertArraysEqual(bytes, actualBytes);
                return bytes;
            })
            .then()).verifyComplete();
    }

    /**
     * On Windows
     * {@link java.nio.channels.FileChannel#map(FileChannel.MapMode, long, long)}
     * can block file deletion until buffer is reclaimed by GC.
     * https://bugs.java.com/bugdatabase/view_bug.do?bug_id=4715154
     */
    @Test
    public void binaryDataFromFileToFluxDoesNotBlockDelete() throws IOException {
        byte[] bytes = new byte[10240];
        fillArray(bytes);
        Path tempFile = Files.createTempFile("deletionTest", null);
        tempFile.toFile().deleteOnExit();
        Files.write(tempFile, bytes);

        // create and consume flux.
        BinaryData.fromFile(tempFile).toFluxByteBuffer().blockLast();

        // immediate delete should succeed.
        assertTrue(tempFile.toFile().delete());
    }

    /**
     * On Windows
     * {@link java.nio.channels.FileChannel#map(FileChannel.MapMode, long, long)}
     * can block file deletion until buffer is reclaimed by GC.
     * https://bugs.java.com/bugdatabase/view_bug.do?bug_id=4715154
     */
    @Test
    public void binaryDataFromFileToBytesDoesNotBlockDelete() throws IOException {
        byte[] bytes = new byte[10240];
        fillArray(bytes);
        Path tempFile = Files.createTempFile("deletionTest", null);
        tempFile.toFile().deleteOnExit();
        Files.write(tempFile, bytes);

        // create and consume flux.
        BinaryData.fromFile(tempFile).toBytes();

        // immediate delete should succeed.
        assertTrue(tempFile.toFile().delete());
    }

    @Test
    public void coldFluxSupplierIsReallyCold() {
        byte[] bytes = new byte[1024];

        Supplier<Flux<ByteBuffer>> coldFluxSupplier = createColdFluxSupplier(bytes, 128);

        // Assert that cold flux is really cold.
        Flux<ByteBuffer> flux = coldFluxSupplier.get();
        flux.blockLast();
        assertThrows(RuntimeException.class, flux::blockLast);
    }

    private static Supplier<Flux<ByteBuffer>> createColdFluxSupplier(byte[] bytes, int chunkSize) {
        // Hack cold flux. Throws on second consumption.
        return () -> {
            AtomicInteger offset = new AtomicInteger();
            AtomicInteger remaining = new AtomicInteger(bytes.length);
            AtomicBoolean used = new AtomicBoolean(false);
            return Flux.generate((Consumer<SynchronousSink<ByteBuffer>>) synchronousSink -> {
                if (used.get()) {
                    synchronousSink.error(new RuntimeException("Kaboom"));
                }
                if (remaining.get() == 0) {
                    synchronousSink.complete();
                    used.set(true);
                } else {
                    int length = Math.min(chunkSize, remaining.get());
                    synchronousSink.next(ByteBuffer.wrap(bytes, offset.get(), length));
                    offset.addAndGet(length);
                    remaining.addAndGet(-1 * length);
                }
            });
        };
    }

    @Test
    public void binaryDataAsPropertySerialization() throws IOException {
        BinaryDataAsProperty binaryDataAsProperty = new BinaryDataAsProperty()
            .setProperty(BinaryData.fromObject(new BinaryDataPropertyClass().setTest("test")));
        String expectedJson = "{\"property\":{\"test\":\"test\"}}";

        String actualJson
            = JacksonAdapter.createDefaultSerializerAdapter().serialize(binaryDataAsProperty, SerializerEncoding.JSON);

        assertEquals(expectedJson, actualJson);
    }

    @Test
    public void binaryDataAsPropertyDeserialization() throws IOException {
        BinaryDataAsProperty expected = new BinaryDataAsProperty()
            .setProperty(BinaryData.fromObject(new BinaryDataPropertyClass().setTest("test")));
        String json = "{\"property\":{\"test\":\"test\"}}";

        BinaryDataAsProperty actual = JacksonAdapter.createDefaultSerializerAdapter()
            .deserialize(json, BinaryDataAsProperty.class, SerializerEncoding.JSON);

        assertEquals(expected.getProperty().toString(), actual.getProperty().toString());
    }

    @Test
    public void emptyFluxByteBufferToReplayable() {
        BinaryData binaryData = BinaryData.fromFlux(Flux.empty()).block();

        BinaryData replayable = assertDoesNotThrow(() -> binaryData.toReplayableBinaryData());
        assertEquals("", replayable.toString());
    }

    @Test
    public void emptyFluxByteBufferToReplayableAsync() {
        StepVerifier.create(BinaryData.fromFlux(Flux.empty()).flatMap(BinaryData::toReplayableBinaryDataAsync))
            .assertNext(replayable -> assertEquals("", replayable.toString()))
            .verifyComplete();
    }

    /**
     * Tests that {@link FluxByteBufferContent#toReplayableContent()} eagerly makes the {@link FluxByteBufferContent}
     * replayable. Before, this method wouldn't make the content replayable until the return
     * {@link FluxByteBufferContent} was consumed, which defeated the purpose of the method as the underlying data could
     * be reclaimed or consumed before it was made replayable.
     */
    @Test
    public void fluxByteBufferToReplayableEagerlyConvertsToReplayable() {
        byte[] data = new byte[1024];
        ThreadLocalRandom.current().nextBytes(data);
        byte[] expectedData = CoreUtils.clone(data);

        BinaryDataContent binaryDataContent
            = new FluxByteBufferContent(Flux.just(ByteBuffer.wrap(data))).toReplayableContent();

        Arrays.fill(data, (byte) 0);

        assertArraysEqual(expectedData, binaryDataContent.toBytes());
    }

    /**
     * Tests that {@link FluxByteBufferContent} returned by {@link FluxByteBufferContent#toReplayableContentAsync()}
     * won't attempt to access the original {@link Flux Flux&lt;ByteBuffer&gt;} as the initial duplicated is cached as a
     * stream of {@link ByteBuffer ByteBuffers} that are shared to all subscribers, and duplicated in each subscription
     * so that the underlying content cannot be modified.
     */
    @Test
    public void multipleSubscriptionsToReplayableAsyncFluxByteBufferAreConsistent() {
        byte[] data = new byte[1024];
        ThreadLocalRandom.current().nextBytes(data);
        byte[] expectedData = CoreUtils.clone(data);

        BinaryDataContent> binaryDataContentMono
            = new FluxByteBufferContent(Flux.just(ByteBuffer.wrap(data))).toReplayableContentAsync();

        StepVerifier.create(binaryDataContentMono)
            .assertNext(binaryDataContent -> assertArraysEqual(expectedData, binaryDataContent.toBytes()))
            .verifyComplete();

        Arrays.fill(data, (byte) 0);

        StepVerifier.create(binaryDataContentMono)
            .assertNext(binaryDataContent -> assertArraysEqual(expectedData, binaryDataContent.toBytes()))
            .verifyComplete();
    }

    public static final class BinaryDataAsProperty {
        @JsonProperty("property")
        private BinaryData property;

        public BinaryData getProperty() {
            return property;
        }

        public BinaryDataAsProperty setProperty(BinaryData property) {
            this.property = property;
            return this;
        }
    }

    public static final class BinaryDataPropertyClass {
        @JsonProperty("test")
        private String test;

        public String getTest() {
            return test;
        }

        public BinaryDataPropertyClass setTest(String test) {
            this.test = test;
            return this;
        }
    }

    private static byte[] readByteBuffer(ByteBuffer buffer) {
        // simplified implementation good enough for testing.
        byte[] result = new byte[buffer.remaining()];
        buffer.get(result);
        return result;
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
                throw logger.logThrowableAsError(new UncheckedIOException(ex));
            }
        }

        @Override
        public <T> T> deserializeAsync(InputStream stream, TypeReference<T> typeReference) {
            return Mono.fromCallable(() -> deserialize(stream, typeReference));
        }

        @Override
        public void serialize(OutputStream stream, Object value) {
            try {
                mapper.writeValue(stream, value);
            } catch (IOException ex) {
                throw logger.logThrowableAsError(new UncheckedIOException(ex));
            }
        }

        @Override
        public Void> serializeAsync(OutputStream stream, Object value) {
            return Mono.fromRunnable(() -> serialize(stream, value));
        }
    }
}
