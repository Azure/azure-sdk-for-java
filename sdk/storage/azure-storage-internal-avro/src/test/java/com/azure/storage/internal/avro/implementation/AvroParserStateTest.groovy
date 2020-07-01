package com.azure.storage.internal.avro.implementation

import com.azure.storage.internal.avro.implementation.schema.AvroSchema
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.ByteBuffer

class AvroParserStateTest extends Specification {

    def "constructor"() {
        setup:
        AvroParserState state = new AvroParserState()

        expect:
        state.size == 0
        state.cache.isEmpty()
    }

    @Unroll
    def "write"() {
        setup:
        AvroParserState state = new AvroParserState()

        when:
        byte[] b = new byte[size];
        new Random().nextBytes(b);
        state.write(ByteBuffer.wrap(b))

        then:
        state.size == size
        AvroSchema.getBytes(state.read(size)) == b

        where:
        size || _
        0    || _
        10   || _
        100  || _
        1000 || _
    }

    @Unroll
    def "readSize"() {
        setup:
        AvroParserState state = new AvroParserState()
        ByteBuffer b1 = ByteBuffer.wrap("Hello ".getBytes())
        ByteBuffer b2 = ByteBuffer.wrap("World!".getBytes())
        state.write(b1)
        state.write(b2)

        expect:
        AvroSchema.getBytes(state.read(size)) == value
        state.size == remaining
        state.cache.size() == buffersLeft

        where:
        size || remaining | value                       | buffersLeft
        0    ||  12       | []                          | 2               /* 0 buffers */
        3    ||  9        | "Hel".getBytes()            | 2               /* 1 buffer */
        6    ||  6        | "Hello ".getBytes()         | 1               /* Exactly 1 buffer. */
        7    ||  5        | "Hello W".getBytes()        | 1               /* 2 buffers */
        12   ||  0        | "Hello World!".getBytes()   | 0               /* Exactly 2 buffers. */
    }

    def "read"() {
        setup:
        String word = "Hello World!"
        AvroParserState state = new AvroParserState()
        ByteBuffer b1 = ByteBuffer.wrap(word.substring(0, 6).getBytes())
        ByteBuffer b2 = ByteBuffer.wrap(word.substring(6, 12).getBytes())
        state.write(b1)
        state.write(b2)

        expect:
        def index = 0
        while (index < word.length()) {
            assert state.read() == (byte) word.charAt(index)
            index++
            assert state.size == word.length() - index
        }
    }

}
