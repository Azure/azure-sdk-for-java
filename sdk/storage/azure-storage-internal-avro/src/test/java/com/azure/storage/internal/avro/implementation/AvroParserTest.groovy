package com.azure.storage.internal.avro.implementation

import com.azure.core.util.FluxUtil
import com.azure.storage.internal.avro.implementation.schema.primitive.AvroNullSchema
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.nio.channels.AsynchronousFileChannel
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

class AvroParserTest extends Specification {

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
        AvroParser parser = new AvroParser()
        Path path = Paths.get(getTestCasePath(testCase))
        Flux<ByteBuffer> file = FluxUtil.readFile(AsynchronousFileChannel.open(path, StandardOpenOption.READ))

        when:
        def verifier = StepVerifier.create(file
            .concatMap({ buffer -> parser.parse(buffer) })
        )
        then:
        verifier.expectNextMatches(predicate)
            .expectNextCount(9)
            .verifyComplete()

        where:
        testCase | predicate
        0        | { o -> o instanceof AvroNullSchema.Null } // Null
        1        | { o -> (Boolean) o } // Boolean
        2        | { o -> ((String) o).equals("adsfasdf09809dsf-=adsf") } // String
        3        | { o -> this.&bytesEqual(o, '12345abcd'.getBytes()) } // Bytes
        4        | { o -> ((Integer) o).equals(1234) } // Integer
        5        | { o -> ((Long) o).equals(1234L) } // Long
        6        | { o -> ((Float) o).equals(1234.0 as Float) } // Float
        7        | { o -> ((Double) o).equals(1234.0 as Double) } // Double
        8        | { o -> this.&bytesEqual(o, 'B'.getBytes()) } // Fixed
        9        | { o -> ((String) o).equals("B") } // Enum
        10       | { o -> ((List) o).equals([1, 3, 2]) } // Array
        11       | { o -> ((Map) o).equals(['a': 1, 'b': 3, 'c': 2])} // Map
        12       | { o -> o instanceof AvroNullSchema.Null } // Union
        13       | { o -> ((Map) o).equals(['$record': 'Test', 'f': 5]) } // Record
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
        AvroParser parser = new AvroParser()
        Path path = Paths.get(getTestCasePath(13))
        Flux<ByteBuffer> file = FluxUtil.readFile(AsynchronousFileChannel.open(path, StandardOpenOption.READ),
            chunkSize, 0, 157)

        when:
        def verifier = StepVerifier.create(file
            .concatMap({ buffer -> parser.parse(buffer) })
        )
        then:
        verifier.expectNextMatches({ o -> ((Map) o).equals(['$record': 'Test', 'f': 5]) })
            .expectNextCount(9)
            .verifyComplete()

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
        AvroParser parser = new AvroParser()
        String fileName = "changefeed_large.avro"
        ClassLoader classLoader = getClass().getClassLoader()
        File f = new File(classLoader.getResource(fileName).getFile())
        Path path = Paths.get(f.getAbsolutePath())
        Flux<ByteBuffer> file = FluxUtil.readFile(AsynchronousFileChannel.open(path, StandardOpenOption.READ))

        when:
        def sv = StepVerifier.create(file
            .concatMap({buffer -> parser.parse(buffer)})
            .map({o -> (String)((Map<String, Object>) o).get("subject")})
            .index()
        )

        then:
        sv
            .expectNextMatches({t -> t.getT2() == "/blobServices/default/containers/test-container/blobs/" + t.getT1()})
            .expectNextCount(999)
            .verifyComplete()
    }

    def "Parse QQ small"() {
        setup:
        AvroParser parser = new AvroParser()
        String fileName = "query_small.avro"
        ClassLoader classLoader = getClass().getClassLoader()
        File f = new File(classLoader.getResource(fileName).getFile())
        Path path = Paths.get(f.getAbsolutePath())
        Flux<ByteBuffer> file = FluxUtil.readFile(AsynchronousFileChannel.open(path, StandardOpenOption.READ))

        when:
        def sv = StepVerifier.create(file
            .concatMap({buffer -> parser.parse(buffer)})
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
        AvroParser parser = new AvroParser()
        String fileName = "query_large.avro"
        ClassLoader classLoader = getClass().getClassLoader()
        File f = new File(classLoader.getResource(fileName).getFile())
        Path path = Paths.get(f.getAbsolutePath())
        Flux<ByteBuffer> file = FluxUtil.readFile(AsynchronousFileChannel.open(path, StandardOpenOption.READ))

        when:
        def sv = StepVerifier.create(file
            .concatMap({buffer -> parser.parse(buffer)})
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
            .expectComplete()
            .verify()
    }
    /* TODO (gapra) : Once this is in the same branch as QQ and CF, add network tests for both of them. (this could just go in the CF/QQ packages) */

}
