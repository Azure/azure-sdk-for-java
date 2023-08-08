package com.azure.storage.common.implementation

import spock.lang.Specification

import java.nio.ByteBuffer

class BufferAggregatorTest extends Specification{

    def "Read first n bytes"() {
        setup:
        def aggregator = new BufferAggregator(100)

        // Get original data
        Random rand = new Random(System.currentTimeMillis())
        def data = new byte[100]
        rand.nextBytes(data)

        // Fill the individual buffers and add them to the aggregator
        int totalDataWritten = 0;
        for (int i=0; i<bufferArray.size(); i++) {
            aggregator.append(
                ByteBuffer.wrap(bufferArray.get(i))
                    .put(ByteBuffer.wrap(data, totalDataWritten, bufferArray.get(i).size()))
                    .flip())
            totalDataWritten += bufferArray.get(i).size()
        }

        when:
        def outArr = aggregator.getFirstNBytes(50)

        then:
        aggregator.length() == 50
        ByteBuffer.wrap(outArr) == ByteBuffer.wrap(data, 0, 50)

        when:
        outArr = aggregator.getFirstNBytes(50)

        then:
        aggregator.length() == 0
        ByteBuffer.wrap(outArr) == ByteBuffer.wrap(data, 50, 50)

        where:
        bufferArray | _
        [new byte[5], new byte[95]] | _
        [new byte[10], new byte[10], new byte[10], new byte[70]] | _
        [new byte[100]] | _
        [new byte[50], new byte[50]] | _
        [new byte[70], new byte [20], new byte[10]] | _
    }

    def "Read first n bytes fail"() {

    }
}
