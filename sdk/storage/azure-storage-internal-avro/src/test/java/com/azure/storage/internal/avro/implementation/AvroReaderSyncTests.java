package com.azure.storage.internal.avro.implementation;

import com.azure.core.util.FluxUtil;
import com.azure.storage.internal.avro.implementation.schema.primitive.AvroNullSchema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.azure.core.test.utils.TestUtils.assertArraysEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("unchecked")
public class AvroReaderSyncTests {
    private static final String LARGE_AVRO_PATH = "/blobServices/default/containers/test-container/blobs/";

    private FileChannel openChannel(int testCase) throws IOException, URISyntaxException {
        return openChannel("test_null_" + testCase + ".avro");
    }

    private FileChannel openChannel(String filename) throws IOException, URISyntaxException {
        return FileChannel.open(Paths.get(getClass().getClassLoader().getResource(filename).toURI()),
            StandardOpenOption.READ);
    }

    /*
     * These are the schemas that are being tested.
     *   ('"null"', None),
     *   ('"boolean"', True),
     *   ('"string"', 'adsfasdf09809dsf-=adsf'),
     *   ('"bytes"', b'12345abcd'),
     *   ('"int"', 1234),
     *   ('"long"', 1234),
     *   ('"float"', 1234.0),
     *   ('"double"', 1234.0),
     *   ('{"type": "fixed", "name": "Test", "size": 1}', b'B'),
     *   ('{"type": "enum", "name": "Test", "symbols": ["A", "B"]}', 'B'),
     *   ('{"type": "array", "items": "long"}', [1, 3, 2]),
     *   ('{"type": "map", "values": "long"}', {'a': 1, 'b': 3, 'c': 2}),
     *   ('["string", "null", "long"]', null),
     *
     *   ("""
     *    {
     *      "type": "record",
     *      "name": "Test",
     *      "fields": [{"name": "f", "type": "long"}]
     *    }
     * "",
     *    {'f': 5}),
     *      ("""
     *    {
     *      "type": "record",
     *      "name": "Lisp",
     *      "fields": [{
     *         "name": "value",
     *         "type": [
     *           "null",
     *           "string",
     *           {
     *             "type": "record",
     *             "name": "Cons",
     *             "fields": [{"name": "car", "type": "Lisp"},
     *                        {"name": "cdrpe": "Lisp"}]
     *           }
     *         ]
     *      }]
     *    }
     *    """,
     *    {'value': {'car': {'value': 'head'}, 'cdr': {'value': None}}}),
     */

    @ParameterizedTest
    @MethodSource("parseSupplier")
    public void parse(int testCase, int blockOffset, Consumer<Object> consumer) throws IOException, URISyntaxException {
        try (FileChannel fileChannel = openChannel(testCase)) {
            // Move the file pointer to the block offset before reading
            fileChannel.position(blockOffset);
            ByteBuffer buffer = ByteBuffer.allocate((int) (fileChannel.size() - blockOffset));
            fileChannel.read(buffer);
            buffer.flip();  // Prepare the buffer for reading

            /* Normal use case. */
            Iterable<AvroObject> result = new AvroReaderSyncFactory().getAvroReader(buffer).read();

            // Process results as before
            List<Object> results = StreamSupport.stream(result.spliterator(), false)
                .map(AvroObject::getObject)
                .collect(Collectors.toList());

            assertNextMatches(results, consumer, 10);

//            fileChannel.position(0); // Reset position for header reading if needed
//            ByteBuffer headerBuffer = ByteBuffer.allocate(blockOffset); // Size for header
//            fileChannel.read(headerBuffer);
//            headerBuffer.flip();
            buffer.rewind();

            /* Special use case for Changefeed - parse header and block separate. */
            Iterable<AvroObject> secondResult = new AvroReaderSyncFactory().getAvroReader(buffer, blockOffset, -1).read();
            // Process results as before
            List<Object> secondResults = StreamSupport.stream(secondResult.spliterator(), false)
                .map(AvroObject::getObject)
                .collect(Collectors.toList());

            assertNextMatches(secondResults, consumer, 10);
        }
    }

    private static Stream<Arguments> parseSupplier() {
        Map<String, Long> map = new HashMap<>();
        map.put("a", 1L);
        map.put("b", 3L);
        map.put("c", 2L);

        Map<String, Object> record = new HashMap<>();
        record.put("$record", "Test");
        record.put("f", 5L);

        return Stream.of(
            Arguments.of(0, 57, createConsumer(o -> assertInstanceOf(AvroNullSchema.Null.class, o)))
//            Arguments.of(1, 60, createConsumer(o -> assertTrue((boolean) o))),
//            Arguments.of(2, 59, createConsumer(o -> assertEquals("adsfasdf09809dsf-=adsf", o))),
//            Arguments.of(3, 58, createConsumer(o -> bytesEqual(o, "12345abcd".getBytes(StandardCharsets.UTF_8)))),
//            Arguments.of(4, 56, createConsumer(o -> assertEquals(1234, o))),
//            Arguments.of(5, 57, createConsumer(o -> assertEquals(1234L, o))),
//            Arguments.of(6, 58, createConsumer(o -> assertEquals(1234F, o))),
//            Arguments.of(7, 59, createConsumer(o -> assertEquals(1234D, o))),
//            Arguments.of(8, 95, createConsumer(o -> bytesEqual(o, "B".getBytes(StandardCharsets.UTF_8)))),
//            Arguments.of(9, 106, createConsumer(o -> assertEquals("B", o))),
//            Arguments.of(10, 85, createConsumer(o -> assertEquals(Arrays.asList(1L, 3L, 2L), o))),
//            Arguments.of(11, 84, createConsumer(o -> assertEquals(map, o))),
//            Arguments.of(12, 77, createConsumer(o -> assertInstanceOf(AvroNullSchema.Null.class, o))),
//            Arguments.of(13, 129, createConsumer(o -> assertEquals(record, o)))
            /* TODO (gapra) : Not necessary for QQ or CF but case 14 tests the ability to reference named types as a type in a record. */
        );
    }

    private static Consumer<Object> createConsumer(Consumer<Object> consumer) {
        return consumer;
    }

    static void bytesEqual(Object actual, byte[] expected) {
        int index = 0;
        byte[] actualBytes = new byte[expected.length];
        for (ByteBuffer bytes : (List<ByteBuffer>) actual) {
            int remaining = bytes.remaining();
            bytes.get(actualBytes, index, remaining);
            index += remaining;
        }

        assertArraysEqual(expected, actualBytes);
    }

    private static void containsConsumer(Object o) {
        Map<?, ?> map = (Map<?, ?>) o;
        assertTrue(map.containsKey("$record"));
        assertTrue(map.containsKey("data"));
        assertTrue(map.containsValue("resultData"));
    }

    private static void progressMatchesConsumer(Object o, int scanned, int total) {
        Map<?, ?> map = (Map<?, ?>) o;
        assertEquals("progress", map.remove("$record"));
        assertEquals(scanned, Integer.valueOf(String.valueOf(map.remove("bytesScanned"))));
        assertEquals(total, Integer.valueOf(String.valueOf(map.remove("totalBytes"))));
        assertTrue(map.isEmpty());
    }

    private static void endMatchesConsumer(Object o, int total) {
        Map<?, ?> map = (Map<?, ?>) o;
        assertEquals("end", map.remove("$record"));
        assertEquals(total, Integer.valueOf(String.valueOf(map.remove("totalBytes"))));
        assertTrue(map.isEmpty());
    }

    public static <T> void assertNextMatches(Iterable<T> elements, Consumer<T> consumer, int count) {
        int index = 0;
        for (T element : elements) {
            if (index++ >= count) break;
            consumer.accept(element); // Apply the consumer, which contains assertions or checks
        }

        // Verify that we've consumed exactly 'count' elements
        if (index != count) {
            throw new AssertionError("Expected " + count + " elements, but got " + index);
        }
    }
}
