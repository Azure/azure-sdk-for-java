package com.azure.storage.internal.avro.implementation

import com.azure.storage.internal.avro.implementation.schema.AvroSchema
import spock.lang.Specification

import java.nio.ByteBuffer

class AvroSchemaTest extends Specification {

    def "getBytes"() {
        setup:
        ByteBuffer b1 = ByteBuffer.wrap("Hello ".getBytes())
        ByteBuffer b2 = ByteBuffer.wrap("World!".getBytes())
        LinkedList<ByteBuffer> buffers = new LinkedList<>()
        buffers.add(b1)
        buffers.add(b2)

        when:
        byte[] bytes = AvroSchema.getBytes(buffers)

        then:
        bytes == "Hello World!".getBytes()
    }
}
