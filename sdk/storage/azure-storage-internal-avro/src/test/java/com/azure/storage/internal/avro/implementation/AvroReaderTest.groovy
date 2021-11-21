package com.azure.storage.internal.avro.implementation

import com.azure.core.util.FluxUtil
import com.azure.storage.internal.avro.implementation.schema.primitive.AvroNullSchema
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import reactor.util.function.Tuples
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.nio.channels.AsynchronousFileChannel
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

class AvroReaderTest extends Specification {

    String getTestCasePath(int testCase) {
        String fileName = String.format("test_null_%d.avro", testCase)
        ClassLoader classLoader = getClass().getClassLoader()
        File file = new File(classLoader.getResource(fileName).getFile())
        return  file.getAbsolutePath();
    }

    /**
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

    @Unroll
    def "Parse"() {
        setup:
        Path path = Paths.get(getTestCasePath(testCase))
        AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ)

        /* Normal use case. */
        when:
        Flux<ByteBuffer> data = FluxUtil.readFile(fileChannel)
        def simpleVerifier = StepVerifier.create(
            new AvroReaderFactory().getAvroReader(data)
                .read()
            .map({ avroObject -> avroObject.getObject() })
        )
        then:
        int simpleCounter = 0
        while (simpleCounter < 10) {
            assert simpleVerifier
                .expectNextMatches(predicate)
            simpleCounter++
        }
        assert simpleVerifier.verifyComplete()

        /* Special use case for Changefeed - parse header and block separate. */
        when:
        Flux<ByteBuffer> header = FluxUtil.readFile(fileChannel)
        Flux<ByteBuffer> body = FluxUtil.readFile(fileChannel, blockOffset, fileChannel.size())
        def complexVerifier = StepVerifier.create(
            new AvroReaderFactory().getAvroReader(header, body, blockOffset, -1 as long)
                .read()
                .map({ avroObject -> avroObject.getObject() })
        )

        then:
        int complexCounter = 0
        while (complexCounter < 10) {
            assert complexVerifier
                .expectNextMatches(predicate)
            complexCounter++
        }
        assert complexVerifier.verifyComplete()

        where:
        /* blockOffsets were manually extracted from a run. */
        testCase | blockOffset || predicate
        0        | 57          || { o -> o instanceof AvroNullSchema.Null } // Null
        1        | 60          || { o -> (Boolean) o } // Boolean
        2        | 59          || { o -> ((String) o).equals("adsfasdf09809dsf-=adsf") } // String
        3        | 58          || { o -> this.&bytesEqual(o, '12345abcd'.getBytes()) } // Bytes
        4        | 56          || { o -> ((Integer) o).equals(1234) } // Integer
        5        | 57          || { o -> ((Long) o).equals(1234L) } // Long
        6        | 58          || { o -> ((Float) o).equals(1234.0 as Float) } // Float
        7        | 59          || { o -> ((Double) o).equals(1234.0 as Double) } // Double
        8        | 95          || { o -> this.&bytesEqual(o, 'B'.getBytes()) } // Fixed
        9        | 106         || { o -> ((String) o).equals("B") } // Enum
        10       | 85          || { o -> ((List) o).equals([1, 3, 2]) } // Array
        11       | 84          || { o -> ((Map) o).equals(['a': 1, 'b': 3, 'c': 2])} // Map
        12       | 77          || { o -> o instanceof AvroNullSchema.Null } // Union
        13       | 129         || { o -> ((Map) o).equals(['$record': 'Test', 'f': 5]) } // Record
        /* TODO (gapra) : Not necessary for QQ or CF but case 14 tests the ability to reference named types as a type in a record. */
    }

    boolean bytesEqual(Object actual, byte[] expected) {
        Iterator<ByteBuffer> bytes = ((LinkedList<ByteBuffer>) actual).iterator()

        boolean match = true
        int index = 0;
        while (bytes.hasNext()) {
            ByteBuffer b = bytes.next()
            while (b.hasRemaining()) {
                match &= b.get() == expected[index]
                index++;
            }
        }

        return match
    }

    /* This test checks that different chunk sizes still result in validly parsed files. */
    @Unroll
    def "Parse chunk size"() {
        setup:
        Path path = Paths.get(getTestCasePath(13))
        AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ)

        /* Normal use case. */
        when:
        Flux<ByteBuffer> data = FluxUtil.readFile(fileChannel, chunkSize, 0, 157)
        def simpleVerifier = StepVerifier.create(
            new AvroReaderFactory().getAvroReader(data)
                .read()
                .map({avroObject -> avroObject.getObject()})
        )
        then:
        int counter = 0
        while (counter < 10) {
            assert simpleVerifier
                .expectNextMatches({ o -> ((Map) o).equals(['$record': 'Test', 'f': 5]) })
            counter++
        }
        assert simpleVerifier.verifyComplete()

        /* Special use case for Changefeed - parse header and block separate. */
        when:
        Flux<ByteBuffer> header = FluxUtil.readFile(fileChannel)
        Flux<ByteBuffer> body = FluxUtil.readFile(fileChannel, chunkSize, 129, fileChannel.size())
        def complexVerifier = StepVerifier.create(
            new AvroReaderFactory().getAvroReader(header, body, 129 as long, -1 as long)
                .read()
                .map({avroObject -> avroObject.getObject()})
        )

        then:
        int complexCounter = 0
        while (complexCounter < 10) {
            assert complexVerifier
                .expectNextMatches({ o -> ((Map) o).equals(['$record': 'Test', 'f': 5]) })
            complexCounter++
        }
        assert complexVerifier.verifyComplete()

        /* The record avro file is 157 bytes long. */
        where:
        chunkSize || _
        1         || _ /* 157 1 byte ByteBuffers. */
        36        || _ /* 4 36 byte ByteBuffers. */
        78        || _ /* 2 78 byte ByteBuffers. */
        157       || _ /* 1 157 byte ByteBuffer. */
    }

    /* TODO (gapra) : Download a CF file with a single record and add a test with that, validate all parts of map are correct. Also chunk the file. */
    def "Parse CF large"() {
        setup:
        String fileName = "changefeed_large.avro"
        ClassLoader classLoader = getClass().getClassLoader()
        File f = new File(classLoader.getResource(fileName).getFile())
        Path path = Paths.get(f.getAbsolutePath())
        AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ)

        /* Normal use case. */
        when:
        Flux<ByteBuffer> data = FluxUtil.readFile(fileChannel)
        def simpleVerifier = StepVerifier.create(
            new AvroReaderFactory().getAvroReader(data)
                .read()
                .map({avroObject -> avroObject.getObject()})
                .map({o -> (String)((Map<String, Object>) o).get("subject")})
                .index()
        )

        then:
        int simpleCounter = 0
        while (simpleCounter < 1000) {
            assert simpleVerifier
                .expectNextMatches({t -> t.getT2() == "/blobServices/default/containers/test-container/blobs/" + t.getT1()})
            simpleCounter++
        }
        assert simpleVerifier.verifyComplete()
    }

    @Unroll
    def "Parse CF large blockOffset"() {
        setup:
        String fileName = "changefeed_large.avro"
        ClassLoader classLoader = getClass().getClassLoader()
        File f = new File(classLoader.getResource(fileName).getFile())
        Path path = Paths.get(f.getAbsolutePath())
        AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ)

        /* Special use case for Changefeed - parse header and block separate. */
        when:
        Flux<ByteBuffer> header = FluxUtil.readFile(fileChannel, 0, 5 * 1024)
        Flux<ByteBuffer> body = FluxUtil.readFile(fileChannel, blockOffset, fileChannel.size())
        def complexVerifier = StepVerifier.create(
            new AvroReaderFactory().getAvroReader(header, body, blockOffset, -1 as long)
                .read()
                .map({avroObject -> avroObject.getObject()})
                .map({o -> (String)((Map<String, Object>) o).get("subject")})
                .index()
                .map({ tuple2 -> Tuples.of(tuple2.getT1() + 1000 - numObjects, tuple2.getT2()) })
        )

        then:
        int complexCounter = 0
        while (complexCounter < numObjects) {
            assert complexVerifier
                .expectNextMatches({t -> t.getT2() == "/blobServices/default/containers/test-container/blobs/" + t.getT1()})
            complexCounter++
        }
        assert complexVerifier.verifyComplete()

        where:
        blockOffset | numObjects || _
        1953        | 1000       || _
        67686       | 881        || _
        133529      | 762        || _
        199372      | 643        || _
        265215      | 524        || _
        331058      | 405        || _
        396901      | 286        || _
        462744      | 167        || _
        528587      | 48         || _
        555167      | 0          || _
    }

    @Unroll
    def "Parse CF large filterIndex"() {
        setup:
        String fileName = "changefeed_large.avro"
        ClassLoader classLoader = getClass().getClassLoader()
        File f = new File(classLoader.getResource(fileName).getFile())
        Path path = Paths.get(f.getAbsolutePath())
        AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ)

        /* Special use case for Changefeed - parse header and block separate. */
        when:
        Flux<ByteBuffer> header = FluxUtil.readFile(fileChannel, 0, 5 * 1024)
        Flux<ByteBuffer> body = FluxUtil.readFile(fileChannel, blockOffset, fileChannel.size())
        def complexVerifier = StepVerifier.create(
            new AvroReaderFactory().getAvroReader(header, body, blockOffset, filterIndex)
                .read()
                .map({avroObject -> avroObject.getObject()})
                .map({o -> (String)((Map<String, Object>) o).get("subject")})
                .index()
                .map({ tuple2 -> Tuples.of(tuple2.getT1() + 1000 - numObjects, tuple2.getT2()) })
        )

        then:
        int complexCounter = 0
        while (complexCounter < numObjects) {
            assert complexVerifier
                .expectNextMatches({t -> t.getT2() == "/blobServices/default/containers/test-container/blobs/" + t.getT1()})
            complexCounter++
        }
        assert complexVerifier.verifyComplete()

        where:
        blockOffset | filterIndex | numObjects || _
        1953        | 1           | 999        || _
        67686       | 35          | 846        || _
        133529      | 57          | 705        || _
        199372      | 0           | 643        || _
        265215      | 51          | 473        || _
        331058      | 0           | 405        || _
        396901      | 11          | 275        || _
        462744      | 68          | 99         || _
        528587      | 41          | 7          || _
        555167      | 0           | 0          || _
    }

    def "Parse CF small"() {
        setup:
        String fileName = "changefeed_small.avro"
        ClassLoader classLoader = getClass().getClassLoader()
        File f = new File(classLoader.getResource(fileName).getFile())
        Path path = Paths.get(f.getAbsolutePath())
        AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ)

        /* Normal use case. */
        when:
        Flux<ByteBuffer> data = FluxUtil.readFile(fileChannel)
        def simpleVerifier = StepVerifier.create(
            new AvroReaderFactory().getAvroReader(data)
                .read()
                .map({avroObject -> avroObject.getObject()})
        )

        then:
        simpleVerifier.expectNextMatches({ t -> this.&verifyChangefeedEvent(t) })
        .verifyComplete()

    }

    boolean verifyChangefeedEvent(Object record) {
        boolean valid = true;
        valid &= record instanceof Map
        Map<?, ?> r = (Map<?, ?>) record
        valid &= r.get('$record') == "BlobChangeEvent"
        valid &= r.get("schemaVersion") == 3
        valid &= r.get("topic") == "/subscriptions/ba45b233-e2ef-4169-8808-49eb0d8eba0d/resourceGroups/XClient/providers/Microsoft.Storage/storageAccounts/seanchangefeedstage"
        valid &= r.get("subject") == "/blobServices/default/containers/myblobscontainer9/blobs/myblob"
        valid &= r.get("eventType") == "BlobCreated"
        valid &= r.get("eventTime") == "2020-03-28T05:05:46.6636036Z"
        valid &= r.get("id") == "ecc8f58e-301e-0022-18be-045724067dce"
        valid &= verifyChangefeedData(r.get("data"))
        return valid
    }

    boolean verifyChangefeedData(Object record) {
        boolean valid = true;
        valid &= record instanceof Map
        Map<?, ?> r = (Map<?, ?>) record
        valid &= r.get('$record') == "BlobChangeEventData"
        valid &= r.get('api') == "PutBlob"
        valid &= r.get('clientRequestId') == "c8a34806-70b1-11ea-a4b0-001a7dda7113"
        valid &= r.get('requestId') == "ecc8f58e-301e-0022-18be-045724000000"
        valid &= r.get('etag') == "0x8D7D2D5ACE2CC31"
        valid &= r.get("contentType") == "application/octet-stream"
        valid &= r.get("contentLength") == 55
        valid &= r.get("blobType") == "BlockBlob"
        valid &= r.get("url") == ""
        valid &= r.get("sequencer") == "00000000000000000000000000001839000000000001a9dc"
        valid &= r.get("previousInfo") instanceof AvroNullSchema.Null
        valid &= r.get("snapshot") instanceof AvroNullSchema.Null
        valid &= r.get("blobPropertiesUpdated") instanceof AvroNullSchema.Null
        valid &= r.get("storageDiagnostics") instanceof Map
        Map<?, ?> sd = (Map<?, ?>) r.get("storageDiagnostics")
        valid &= sd.get("bid") == "6148d063-2006-0001-00be-04cde7000000"
        valid &= sd.get("sid") == "5083feab-0027-eed2-bf03-0d533fee5677"
        valid &= sd.get("seq") == "(6201,22103,109020,108961)"
        return valid
    }

    def "Parse QQ small"() {
        setup:
        String fileName = "query_small.avro"
        ClassLoader classLoader = getClass().getClassLoader()
        File f = new File(classLoader.getResource(fileName).getFile())
        Path path = Paths.get(f.getAbsolutePath())
        Flux<ByteBuffer> data = FluxUtil.readFile(AsynchronousFileChannel.open(path, StandardOpenOption.READ))

        when:
        def sv = StepVerifier.create(
            new AvroReaderFactory().getAvroReader(data)
                .read()
                .map({avroObject -> avroObject.getObject()})
        )

        then:
        sv
        .expectNextMatches({ o -> ((Map) o).containsKey('$record') && ((Map) o).containsKey('data') && ((Map) o).containsValue('resultData')})
            .expectNextMatches({ o -> ((Map) o).equals(['$record': 'progress', 'bytesScanned': 1024, 'totalBytes': 1024]) })
            .expectNextMatches({ o -> ((Map) o).equals(['$record': 'end', 'totalBytes': 1024]) })
            .verifyComplete()
    }

    def "Parse QQ large"() {
        setup:
        String fileName = "query_large.avro"
        ClassLoader classLoader = getClass().getClassLoader()
        File f = new File(classLoader.getResource(fileName).getFile())
        Path path = Paths.get(f.getAbsolutePath())
        Flux<ByteBuffer> data = FluxUtil.readFile(AsynchronousFileChannel.open(path, StandardOpenOption.READ))

        when:
        def sv = StepVerifier.create(
            new AvroReaderFactory().getAvroReader(data)
                .read()
                .map({avroObject -> avroObject.getObject()})
        )

        then:
        sv
            .expectNextMatches({ o -> ((Map) o).containsKey('$record') && ((Map) o).containsKey('data') && ((Map) o).containsValue('resultData')})
            .expectNextMatches({ o -> ((Map) o).equals(['$record': 'progress', 'bytesScanned': 4194304, 'totalBytes': 16384000]) })
            .expectNextMatches({ o -> ((Map) o).containsKey('$record') && ((Map) o).containsKey('data') && ((Map) o).containsValue('resultData')})
            .expectNextMatches({ o -> ((Map) o).equals(['$record': 'progress', 'bytesScanned': 8388608, 'totalBytes': 16384000]) })
            .expectNextMatches({ o -> ((Map) o).containsKey('$record') && ((Map) o).containsKey('data') && ((Map) o).containsValue('resultData')})
            .expectNextMatches({ o -> ((Map) o).equals(['$record': 'progress', 'bytesScanned': 12582912, 'totalBytes': 16384000]) })
            .expectNextMatches({ o -> ((Map) o).containsKey('$record') && ((Map) o).containsKey('data') && ((Map) o).containsValue('resultData')})
            .expectNextMatches({ o -> ((Map) o).equals(['$record': 'progress', 'bytesScanned': 16384000, 'totalBytes': 16384000]) })
            .expectNextMatches({ o -> ((Map) o).equals(['$record': 'end', 'totalBytes': 16384000]) })
            .verifyComplete()
    }

}
