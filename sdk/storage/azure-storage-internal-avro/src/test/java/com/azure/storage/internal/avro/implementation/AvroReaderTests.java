// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.internal.avro.implementation;

import com.azure.core.util.FluxUtil;
import com.azure.storage.internal.avro.implementation.schema.primitive.AvroNullSchema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.test.StepVerifier;
import reactor.util.function.Tuples;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.azure.core.test.utils.TestUtils.assertArraysEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("unchecked")
public class AvroReaderTests {
    private static final String LARGE_AVRO_PATH = "/blobServices/default/containers/test-container/blobs/";

    AsynchronousFileChannel openChannel(int testCase) throws IOException {
        return openChannel("test_null_" + testCase + ".avro");
    }

    AsynchronousFileChannel openChannel(String testCase) throws IOException {
        return AsynchronousFileChannel.open(
            new File(getClass().getClassLoader().getResource(testCase).getFile()).toPath(), StandardOpenOption.READ);
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
    public void parse(int testCase, int blockOffset, Consumer<Object> consumer) throws IOException {
        AsynchronousFileChannel fileChannel = openChannel(testCase);

        /* Normal use case. */
        assertNextMatches(StepVerifier.create(
                new AvroReaderFactory().getAvroReader(FluxUtil.readFile(fileChannel)).read().map(AvroObject::getObject)),
            consumer, 10)
            .verifyComplete();

        /* Special use case for Changefeed - parse header and block separate. */
        assertNextMatches(StepVerifier.create(new AvroReaderFactory().getAvroReader(FluxUtil.readFile(fileChannel),
                FluxUtil.readFile(fileChannel, blockOffset, fileChannel.size()), blockOffset, -1)
            .read().map(AvroObject::getObject)), consumer, 10)
            .verifyComplete();
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


    /* This test checks that different chunk sizes still result in validly parsed files. */
    @ParameterizedTest
    @ValueSource(ints = {1, 36, 78, 157})
    public void parseChunkSize(int chunkSize) throws IOException {
        AsynchronousFileChannel fileChannel = openChannel(13);
        Map<String, Object> record = new HashMap<>();
        record.put("$record", "Test");
        record.put("f", 5L);

        /* Normal use case. */
        assertNextMatches(StepVerifier.create(
            new AvroReaderFactory().getAvroReader(FluxUtil.readFile(fileChannel, chunkSize, 0, 157)).read()
                .map(AvroObject::getObject)), o -> assertEquals(record, o), 10)
            .verifyComplete();

        /* Special use case for Changefeed - parse header and block separate. */
        assertNextMatches(StepVerifier.create(new AvroReaderFactory().getAvroReader(FluxUtil.readFile(fileChannel),
                FluxUtil.readFile(fileChannel, chunkSize, 129, fileChannel.size()), 129L, -1L).read()
            .map(AvroObject::getObject)), o -> assertEquals(record, o), 10)
            .verifyComplete();
    }

    /* TODO (gapra) : Download a CF file with a single record and add a test with that, validate all parts of map are correct. Also chunk the file. */
    @Test
    public void parseCfLarge() throws IOException {
        /* Normal use case. */
        assertNextMatches(StepVerifier.create(new AvroReaderFactory().getAvroReader(
                    FluxUtil.readFile(openChannel("changefeed_large.avro"))).read().map(AvroObject::getObject)
                .map(o -> (String) ((Map<String, Object>) o).get("subject")).index()),
            t -> assertEquals(LARGE_AVRO_PATH + t.getT1(), t.getT2()), 1000)
            .verifyComplete();
    }

    @ParameterizedTest
    @CsvSource({"1953,1000", "67686,881", "133529,762", "199372,643", "265215,524", "331058,405", "396901,286",
        "462744,167", "528587,48", "555167,0"})
    public void parseCfLargeBlockOffset(int blockOffset, int numObjects) throws IOException {
        AsynchronousFileChannel fileChannel = openChannel("changefeed_large.avro");

        /* Special use case for Changefeed - parse header and block separate. */
        assertNextMatches(StepVerifier.create(new AvroReaderFactory().getAvroReader(
                    FluxUtil.readFile(fileChannel, 0, 5 * 1024),
                    FluxUtil.readFile(fileChannel, blockOffset, fileChannel.size()), blockOffset, -1L).read()
                .map(AvroObject::getObject)
                .map(o -> (String) ((Map<String, Object>) o).get("subject"))
                .index()
                .map(tuple2 -> Tuples.of(tuple2.getT1() + 1000 - numObjects, tuple2.getT2()))),
            t -> assertEquals(LARGE_AVRO_PATH + t.getT1(), t.getT2()), numObjects)
            .verifyComplete();
    }

    @ParameterizedTest
    @CsvSource({"1953,1,999", "67686,35,846", "133529,57,705", "199372,0,643", "265215,51,473", "331058,0,405",
        "396901,11,275", "462744,68,99", "528587,41,7", "555167,0,0"})
    public void parseCfLargeFilterIndex(int blockOffset, int filterIndex, int numObjects) throws IOException {
        AsynchronousFileChannel fileChannel = openChannel("changefeed_large.avro");

        /* Special use case for Changefeed - parse header and block separate. */
        assertNextMatches(StepVerifier.create(new AvroReaderFactory().getAvroReader(
                    FluxUtil.readFile(fileChannel, 0, 5 * 1024),
                    FluxUtil.readFile(fileChannel, blockOffset, fileChannel.size()), blockOffset, filterIndex)
                .read().map(AvroObject::getObject).map(o -> (String) ((Map<String, Object>) o).get("subject")).index()
                .map(tuple2 -> Tuples.of(tuple2.getT1() + 1000 - numObjects, tuple2.getT2()))),
            t -> assertEquals(LARGE_AVRO_PATH + t.getT1(), t.getT2()), numObjects)
            .verifyComplete();
    }

    @Test
    public void parseCfSmall() throws IOException {
        Path path = new File(getClass().getClassLoader().getResource("changefeed_small.avro").getFile())
            .toPath();

        try (AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ)) {
            /* Normal use case. */
            StepVerifier.create(new AvroReaderFactory().getAvroReader(FluxUtil.readFile(fileChannel))
                    .read()
                    .map(AvroObject::getObject))
                .assertNext(AvroReaderTests::verifyChangefeedEvent)
                .verifyComplete();
        }
    }

    static void verifyChangefeedEvent(Object record) {
        assertInstanceOf(Map.class, record);
        Map<?, ?> r = (Map<?, ?>) record;

        assertEquals("BlobChangeEvent", r.get("$record"));
        assertEquals(3, Integer.valueOf(String.valueOf(r.get("schemaVersion"))));
        assertEquals("/subscriptions/ba45b233-e2ef-4169-8808-49eb0d8eba0d/resourceGroups/XClient/providers/"
            + "Microsoft.Storage/storageAccounts/seanchangefeedstage", r.get("topic"));
        assertEquals("/blobServices/default/containers/myblobscontainer9/blobs/myblob", r.get("subject"));
        assertEquals("BlobCreated", r.get("eventType"));
        assertEquals("2020-03-28T05:05:46.6636036Z", r.get("eventTime"));
        assertEquals("ecc8f58e-301e-0022-18be-045724067dce", r.get("id"));
        verifyChangefeedData(r.get("data"));
    }

    static void verifyChangefeedData(Object record) {
        assertInstanceOf(Map.class, record);
        Map<?, ?> r = (Map<?, ?>) record;

        assertEquals("BlobChangeEventData", r.get("$record"));
        assertEquals("PutBlob", r.get("api"));
        assertEquals("c8a34806-70b1-11ea-a4b0-001a7dda7113", r.get("clientRequestId"));
        assertEquals("ecc8f58e-301e-0022-18be-045724000000", r.get("requestId"));
        assertEquals("0x8D7D2D5ACE2CC31", r.get("etag"));
        assertEquals("application/octet-stream", r.get("contentType"));
        assertEquals(55, Integer.valueOf(String.valueOf(r.get("contentLength"))));
        assertEquals("BlockBlob", r.get("blobType"));
        assertEquals("", r.get("url"));
        assertEquals("00000000000000000000000000001839000000000001a9dc", r.get("sequencer"));
        assertInstanceOf(AvroNullSchema.Null.class, r.get("previousInfo"));
        assertInstanceOf(AvroNullSchema.Null.class, r.get("snapshot"));
        assertInstanceOf(AvroNullSchema.Null.class, r.get("blobPropertiesUpdated"));

        assertInstanceOf(Map.class, r.get("storageDiagnostics"));
        Map<?, ?> sd = (Map<?, ?>) r.get("storageDiagnostics");

        assertEquals("6148d063-2006-0001-00be-04cde7000000", sd.get("bid"));
        assertEquals("5083feab-0027-eed2-bf03-0d533fee5677", sd.get("sid"));
        assertEquals("(6201,22103,109020,108961)", sd.get("seq"));
    }

    @Test
    public void parseQqSmall() throws IOException {
        StepVerifier.create(new AvroReaderFactory().getAvroReader(FluxUtil.readFile(openChannel("query_small.avro")))
                .read().map(AvroObject::getObject))
            .assertNext(AvroReaderTests::containsConsumer)
            .assertNext(o -> progressMatchesConsumer(o, 1024, 1024))
            .assertNext(o -> endMatchesConsumer(o, 1024))
            .verifyComplete();
    }

    @Test
    public void parseQqLarge() throws IOException {
        StepVerifier.create(new AvroReaderFactory().getAvroReader(FluxUtil.readFile(openChannel("query_large.avro")))
                .read().map(AvroObject::getObject))
            .assertNext(AvroReaderTests::containsConsumer)
            .assertNext(o -> progressMatchesConsumer(o, 4194304, 16384000))
            .assertNext(AvroReaderTests::containsConsumer)
            .assertNext(o -> progressMatchesConsumer(o, 8388608, 16384000))
            .assertNext(AvroReaderTests::containsConsumer)
            .assertNext(o -> progressMatchesConsumer(o, 12582912, 16384000))
            .assertNext(AvroReaderTests::containsConsumer)
            .assertNext(o -> progressMatchesConsumer(o, 16384000, 16384000))
            .assertNext(o -> endMatchesConsumer(o, 16384000))
            .verifyComplete();
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

    private static <T> StepVerifier.Step<T> assertNextMatches(StepVerifier.FirstStep<T> firstStep,
        Consumer<T> consumer, int count) {
        if (count == 0) {
            return firstStep;
        }

        StepVerifier.Step<T> step = firstStep.assertNext(consumer);

        for (int i = 0; i < count - 1; i++) {
            step = step.assertNext(consumer);
        }

        return step;
    }
}
