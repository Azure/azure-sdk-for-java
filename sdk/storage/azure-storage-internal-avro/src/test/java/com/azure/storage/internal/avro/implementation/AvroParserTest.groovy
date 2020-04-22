package com.azure.storage.internal.avro.implementation

import com.azure.core.util.FluxUtil
import com.azure.storage.internal.avro.implementation.schema.primitive.AvroNullSchema
import com.azure.storage.internal.avro.implementation.util.AvroUtils
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.nio.channels.AsynchronousFileChannel
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.function.Predicate

class AvroParserTest extends Specification {

    String getTestCasePath(int testCase) {
        String fileName = String.format("test_null_%d.avro", testCase)
        ClassLoader classLoader = getClass().getClassLoader()
        File file = new File(classLoader.getResource(fileName).getFile())
        return  file.getAbsolutePath();
    }

    def verify(StepVerifier.FirstStep verifier, Predicate predicate) {
        verifier.expectNextMatches(predicate)
            .expectNextMatches(predicate)
            .expectNextMatches(predicate)
            .expectNextMatches(predicate)
            .expectNextMatches(predicate)
            .expectNextMatches(predicate)
            .expectNextMatches(predicate)
            .expectNextMatches(predicate)
            .expectNextMatches(predicate)
            .expectNextMatches(predicate)
            .verifyComplete()
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
        verify(verifier, predicate)

        where:
        testCase | predicate
        0        | { o -> o instanceof AvroNullSchema.Null } // Null
        1        | { o -> (Boolean) o } // Boolean
        2        | { o -> ((String) o).equals("adsfasdf09809dsf-=adsf") } // String
        3        | { o -> Arrays.equals(AvroUtils.getBytes((List<?>) o), '12345abcd'.getBytes()) } // Bytes
        4        | { o -> ((Integer) o).equals(1234) } // Integer
        5        | { o -> ((Long) o).equals(1234L) } // Long
        6        | { o -> ((Float) o).equals(1234.0 as Float) } // Float
        7        | { o -> ((Double) o).equals(1234.0 as Double) } // Double
        8        | { o -> Arrays.equals(AvroUtils.getBytes((List<?>) o), 'B'.getBytes()) } // Fixed
        9        | { o -> ((String) o).equals("B") } // Enum
        10       | { o -> ((List) o).equals([1, 3, 2]) } // Array
        11       | { o -> ((Map) o).equals(['a': 1, 'b': 3, 'c': 2])} // Map
        12       | { o -> o instanceof AvroNullSchema.Null } // Union
        13       | { o -> ((Map) o).equals(['$record': 'Test', 'f': 5]) } // Record
        /* TODO (gapra) : Not necessary for QQ or CF but case 14 tests the ability to reference named types as a type in a record. */
    }

    /* TODO (gapra) : Once this is in the same branch as QQ and CF, add network tests for both of them. */

}
