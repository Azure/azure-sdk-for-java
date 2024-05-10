// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.util.binarydata;

import io.clientcore.core.implementation.http.serializer.DefaultJsonSerializer;
import io.clientcore.core.implementation.util.IterableOfByteBuffersInputStream;
import io.clientcore.core.json.JsonReader;
import io.clientcore.core.json.JsonSerializable;
import io.clientcore.core.json.JsonToken;
import io.clientcore.core.json.JsonWriter;
import io.clientcore.core.models.MockFile;
import io.clientcore.core.models.MockPath;
import io.clientcore.core.models.Person;
import io.clientcore.core.util.serializer.ObjectSerializer;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static io.clientcore.core.util.TestUtils.assertArraysEqual;
import static io.clientcore.core.util.TestUtils.fillArray;
import static io.clientcore.core.util.TestUtils.readStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for {@link BinaryData}.
 */
public class BinaryDataTest {
    private static final ObjectSerializer SERIALIZER = new DefaultJsonSerializer();
    private static final byte[] RANDOM_DATA;

    static {
        RANDOM_DATA = new byte[1024 * 1024]; // 1 MB

        fillArray(RANDOM_DATA);
    }

    @Test
    public void fromCustomObject() throws IOException {
        // Arrange
        final Person actualValue = new Person().setName("John Doe").setAge(50);
        final Person expectedValue = new Person().setName("John Doe").setAge(50);

        // Act
        final BinaryData data = BinaryData.fromObject(actualValue, SERIALIZER);

        // Assert
        assertEquals(expectedValue, data.toObject(expectedValue.getClass(), SERIALIZER));
    }

    @Test
    public void fromDouble() throws IOException {
        // Arrange
        final Double actualValue = Double.valueOf("10.1");
        final Double expectedValue = Double.valueOf("10.1");

        // Act
        final BinaryData data = BinaryData.fromObject(actualValue, SERIALIZER);

        // Assert
        assertEquals(expectedValue, data.toObject(expectedValue.getClass(), SERIALIZER));
    }

    @Test
    public void anyTypeToByteArray() {
        // Assert
        final Person actualValue = new Person().setName("John Doe").setAge(50);
        final byte[] expectedValue = "{\"name\":\"John Doe\",\"age\":50}".getBytes(StandardCharsets.UTF_8);

        // Act
        final BinaryData data = BinaryData.fromObject(actualValue, SERIALIZER);

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
        BinaryData binaryData = BinaryData.fromObject(null, SERIALIZER);

        assertNull(binaryData.toBytes());
        assertNull(binaryData.getLength());
    }

    @Test
    public void createFromNullFile() {
        assertThrows(NullPointerException.class, () -> BinaryData.fromFile(null));
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
    public void createFromEmptyStream() {
        // Arrange
        final byte[] expected = "".getBytes();

        // Act
        BinaryData data = BinaryData.fromStream(new ByteArrayInputStream(expected));

        // Assert
        assertArraysEqual(expected, data.toBytes());
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
    public void fromCustomObjectWithDefaultSerializer() throws IOException {
        // Arrange
        final Person actualValue = new Person().setName("John Doe").setAge(50);
        final Person expectedValue = new Person().setName("John Doe").setAge(50);

        // Act
        final BinaryData data = BinaryData.fromObject(actualValue);

        // Assert
        assertEquals(expectedValue, data.toObject(expectedValue.getClass()));
    }

    @Test
    public void fromDoubleWithDefaultSerializer() throws IOException {
        // Arrange
        final Double actualValue = Double.valueOf("10.1");
        final Double expectedValue = Double.valueOf("10.1");

        // Act
        final BinaryData data = BinaryData.fromObject(actualValue);

        // Assert
        assertEquals(expectedValue, data.toObject(expectedValue.getClass()));
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
    public void testFromFile() throws Exception {
        Path file = Files.createTempFile("binaryDataFromFile" + UUID.randomUUID(), ".txt");

        file.toFile().deleteOnExit();

        Files.write(file, "The quick brown fox jumps over the lazy dog".getBytes(StandardCharsets.UTF_8));

        BinaryData data = BinaryData.fromFile(file);

        assertEquals("The quick brown fox jumps over the lazy dog", data.toString());
    }

    @Test
    public void testFromLargeFileStream() throws Exception {
        int chunkSize = 1024 * 1024; // 1 MB
        long numberOfChunks = 2200L; // 2200 MB total

        MockFile mockFile = new MockFile("binaryDataFromFile" + UUID.randomUUID() + ".txt", RANDOM_DATA,
            numberOfChunks * chunkSize);
        FileBinaryData fileContent = new MockFileBinaryData(new MockPath(mockFile), 32768, null, null);

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

    @ParameterizedTest
    @ValueSource(ints = {10, 113, 1024, 1024 + 113, 10 * 1024 * 1024 + 13})
    public void testFromFileSegment(int size) throws Exception {
        int leftPadding = 10 * 1024 + 13;
        int rightPadding = 10 * 1024 + 27;
        byte[] fullFile = new byte[size + leftPadding + rightPadding];

        fillArray(fullFile);

        MockFile mockFile =
            new MockFile("binaryDataFromFileSegment" + UUID.randomUUID() + ".txt", fullFile, fullFile.length);
        FileBinaryData fileContent = new MockFileBinaryData(new MockPath(mockFile), 8192, (long) leftPadding,
            (long) size);

        assertEquals(size, fileContent.getLength());

        byte[] actualBytes = fileContent.toBytes();

        assertArraysEqual(fullFile, leftPadding, size, actualBytes, actualBytes.length);

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
        byte[] firstStreamConsumption = readStream(data.toStream());
        byte[] secondStreamConsumption = readStream(data.toStream());

        // Either flux or stream consumption is not replayable.
        assertFalse(Arrays.equals(firstStreamConsumption, secondStreamConsumption));
    }

    public static Stream<Arguments> createNonRetryableBinaryData() {
        byte[] bytes = new byte[1024];

        fillArray(bytes);

        return Stream.of(
            Arguments.of(
                Named.named("stream",
                    (Supplier<BinaryData>) () -> BinaryData.fromStream(new ByteArrayInputStream(bytes)))),
            Arguments.of(
                Named.named("byte array stream",
                    (Supplier<BinaryData>) () -> BinaryData.fromStream(new ByteArrayInputStream(bytes), null))
            )
        );
    }

    @ParameterizedTest
    @MethodSource("createRetryableBinaryData")
    public void testReplayableContentTypes(Supplier<BinaryData> binaryDataSupplier, byte[] expectedBytes)
        throws IOException {

        assertTrue(binaryDataSupplier.get().isReplayable());

        // Check toStream consumption
        BinaryData data = binaryDataSupplier.get();
        byte[] firstConsumption = readStream(data.toStream());
        byte[] secondConsumption = readStream(data.toStream());
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
    }

    public static Stream<Arguments> createRetryableBinaryData() throws IOException {
        byte[] bytes = new byte[1024];
        fillArray(bytes);

        MockFile mockFile = new MockFile("binaryDataFromFile" + UUID.randomUUID() + ".txt", bytes, 1024);

        return Stream.of(
            Arguments.of(
                Named.named("bytes", (Supplier<BinaryData>) () -> BinaryData.fromBytes(bytes)),
                Named.named("expected bytes", bytes)
            ),
            Arguments.of(
                Named.named("string", (Supplier<BinaryData>) () -> BinaryData.fromString("test string")),
                Named.named("expected bytes", "test string".getBytes(StandardCharsets.UTF_8))
            ),
            Arguments.of(
                Named.named("object", (Supplier<BinaryData>) () -> BinaryData.fromObject("\"test string\"")),
                Named.named("expected bytes", BinaryData.SERIALIZER.serializeToBytes("\"test string\""))
            ),
            Arguments.of(
                Named.named("file", (Supplier<BinaryData>) () -> new MockFileBinaryData(new MockPath(mockFile))),
                Named.named("expected bytes", bytes)
            ),
            Arguments.of(
                Named.named("byte buffer",
                    (Supplier<BinaryData>) () -> BinaryData.fromByteBuffer(ByteBuffer.wrap(bytes))),
                Named.named("expected bytes", bytes)
            ),
            Arguments.of(
                Named.named("byte array stream", (Supplier<BinaryData>) () -> BinaryData.fromStream(
                    new ByteArrayInputStream(bytes), (long) bytes.length)),
                Named.named("expected bytes", bytes)
            )
        );
    }

    @Test
    public void testMakeSmallMarkableStreamReplayable() throws IOException {
        byte[] bytes = new byte[1024];
        fillArray(bytes);

        // Delegate to testReplayableContentTypes to assert accessors replayability
        testReplayableContentTypes(
            () -> BinaryData.fromStream(new ByteArrayInputStream(bytes), (long) bytes.length).toReplayableBinaryData(),
            bytes);

        // When using markable stream
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        assertSame(
            byteArrayInputStream,
            BinaryData.fromStream(byteArrayInputStream, (long) bytes.length).toReplayableBinaryData().toStream()
        );
    }

    @Test
    public void testMakeUnknownLengthMarkableStreamReplayable() throws IOException {
        byte[] bytes = new byte[1024];
        fillArray(bytes);

        // Delegate to testReplayableContentTypes to assert accessors replayability
        testReplayableContentTypes(
            () -> BinaryData.fromStream(new ByteArrayInputStream(bytes)).toReplayableBinaryData(),
            bytes);

        // When using markable stream
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        assertNotSame(
            byteArrayInputStream,
            BinaryData.fromStream(byteArrayInputStream).toReplayableBinaryData().toStream()
        );

        // Check that buffering happened. This is part assumes implementation.
        assertInstanceOf(IterableOfByteBuffersInputStream.class,
            BinaryData.fromStream(byteArrayInputStream).toReplayableBinaryData().toStream());
    }

    @ParameterizedTest
    // Try various sizes. That hit MIN and MAX buffers size in the InputStreamContent
    @ValueSource(ints = {10, 1024, 8 * 1024 - 1, 8 * 1024 + 113, 4 * 1024 * 1024 + 117,
        8 * 1024 * 1024, 8 * 1024 * 1024 + 117, 64 * 1024 * 1024 + 117})
    public void testCanBufferNotMarkableStreams(int size) throws IOException {
        byte[] bytes = new byte[size];
        fillArray(bytes);

        MockFile mockFile = new MockFile("binaryDataFromFile" + UUID.randomUUID() + ".txt", bytes, size);

        // Delegate to testReplayableContentTypes to assert accessors replayability
        // with unknown length
        testReplayableContentTypes(() -> new MockFileBinaryData(new MockPath(mockFile)).toReplayableBinaryData(),
            bytes);

        // with known length
        testReplayableContentTypes(() -> new MockFileBinaryData(new MockPath(mockFile), 8192, null, (long) bytes.length)
            .toReplayableBinaryData(), bytes);

        // When using markable stream
        FileInputStream fileInputStream = new MockFileInputStream(mockFile);
        assertFalse(fileInputStream.markSupported());
        assertNotSame(fileInputStream, BinaryData.fromStream(fileInputStream).toReplayableBinaryData().toStream());

        // Check that buffering happened. This is part assumes implementation.
        assertInstanceOf(IterableOfByteBuffersInputStream.class,
            BinaryData.fromStream(fileInputStream).toReplayableBinaryData().toStream());
    }

    /**
     * On Windows {@link FileChannel#map(FileChannel.MapMode, long, long)} can block file deletion until buffer is
     * reclaimed by GC. https://bugs.java.com/bugdatabase/view_bug.do?bug_id=4715154
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
    public void binaryDataAsPropertySerialization() throws IOException {
        BinaryDataAsProperty binaryDataAsProperty = new BinaryDataAsProperty()
            .setProperty(BinaryData.fromObject(new BinaryDataPropertyClass().setTest("test")));
        String expectedJson = "{\"property\":{\"test\":\"test\"}}";
        String actualJson = new String(new DefaultJsonSerializer().serializeToBytes(binaryDataAsProperty));

        assertEquals(expectedJson, actualJson);
    }

    @Test
    public void binaryDataAsPropertyDeserialization() throws IOException {
        BinaryDataAsProperty expected = new BinaryDataAsProperty()
            .setProperty(BinaryData.fromObject(new BinaryDataPropertyClass().setTest("test")));
        String json = "{\"property\":{\"test\":\"test\"}}";
        BinaryDataAsProperty actual = new DefaultJsonSerializer()
            .deserializeFromBytes(json.getBytes(), BinaryDataAsProperty.class);

        assertEquals(expected.getProperty().toString(), actual.getProperty().toString());
    }

    public static final class BinaryDataAsProperty implements JsonSerializable<BinaryDataAsProperty> {
        private BinaryData property;

        public BinaryData getProperty() {
            return property;
        }

        public BinaryDataAsProperty setProperty(BinaryData property) {
            this.property = property;

            return this;
        }


        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            jsonWriter.writeStartObject();

            BinaryDataPropertyClass binaryDataPropertyClass =
                property.toObject(BinaryDataPropertyClass.class, SERIALIZER);

            jsonWriter.writeJsonField("property", binaryDataPropertyClass);
            jsonWriter.writeEndObject();

            return jsonWriter;
        }

        public static BinaryDataAsProperty fromJson(JsonReader jsonReader) throws IOException {
            return jsonReader.readObject(
                reader -> {
                    BinaryDataAsProperty binaryDataAsProperty = new BinaryDataAsProperty();

                    while (reader.nextToken() != JsonToken.END_OBJECT) {
                        String fieldName = reader.getFieldName();
                        reader.nextToken();

                        if ("property".equals(fieldName)) {
                            binaryDataAsProperty.setProperty(BinaryData.fromObject(reader.readUntyped()));
                        } else {
                            reader.skipChildren();
                        }
                    }

                    return binaryDataAsProperty;
                });
        }
    }

    public static final class BinaryDataPropertyClass implements JsonSerializable<BinaryDataPropertyClass> {
        private String test;

        public String getTest() {
            return test;
        }

        public BinaryDataPropertyClass setTest(String test) {
            this.test = test;

            return this;
        }

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            jsonWriter.writeStartObject();
            jsonWriter.writeStringField("test", test);
            jsonWriter.writeEndObject();

            return jsonWriter;
        }

        public static BinaryDataPropertyClass fromJson(JsonReader jsonReader) throws IOException {
            return jsonReader.readObject(
                reader -> {
                    BinaryDataPropertyClass binaryDataPropertyClass = new BinaryDataPropertyClass();

                    while (reader.nextToken() != JsonToken.END_OBJECT) {
                        String fieldName = reader.getFieldName();
                        reader.nextToken();

                        if ("test".equals(fieldName)) {
                            binaryDataPropertyClass.setTest(reader.getString());
                        } else {
                            reader.skipChildren();
                        }
                    }

                    return binaryDataPropertyClass;
                });
        }
    }

    private static byte[] readByteBuffer(ByteBuffer buffer) {
        // Simplified implementation good enough for testing.
        byte[] result = new byte[buffer.remaining()];

        buffer.get(result);

        return result;
    }
}
