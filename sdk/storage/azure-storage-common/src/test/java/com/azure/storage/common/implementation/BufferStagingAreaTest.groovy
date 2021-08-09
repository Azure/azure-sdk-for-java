package com.azure.storage.common.implementation

import reactor.core.publisher.Flux
import reactor.util.function.Tuples
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.ByteBuffer

class BufferStagingAreaTest extends Specification {
    static def generateData(int numBuffs, int minBuffSize, int maxBuffSize) {
        def random = new Random()
        // Generate random sizes between minBuffSize and maxBuffSize for the buffers
        def totalSize = 0
        def sizes = new int[numBuffs]
        for (int i = 0; i < numBuffs; i++) {
            def size = minBuffSize
            if (maxBuffSize != minBuffSize)
                size += random.nextInt(maxBuffSize - minBuffSize)
            sizes[i] = size
            totalSize += size
        }
        // Generate random data
        def bytes = new byte[totalSize]
        random.nextBytes(bytes)

        // Partition the data based off random sizes
        def data = new ByteBuffer[numBuffs]
        def begin = 0
        for (int i = 0; i < numBuffs; i++) {
            def end = begin + sizes[i]
            data[i] = ByteBuffer.wrap(Arrays.copyOfRange(bytes, begin, end))
            begin += sizes[i]
        }

        // Expected data is partitioned by maxBuffSize
        def expectedData = new ByteBuffer[Math.ceil(totalSize / maxBuffSize)]
        for (int i = 0; i < totalSize / maxBuffSize; i ++) {
            begin = i * maxBuffSize
            def end = Math.min((i + 1) * maxBuffSize, totalSize)
            expectedData[i] = ByteBuffer.wrap(Arrays.copyOfRange(bytes, begin, end))
        }
        return Tuples.of(data, expectedData)
    }

    static def byteBufferListToByteArray(List<ByteBuffer> buffers) {
        def totalSize = 0
        for (def b: buffers) {
            totalSize += b.remaining()
        }
        def bytes = new byte[totalSize]
        def begin = 0
        for (def b: buffers) {
            System.arraycopy(b.array(), b.position(), bytes, begin, b.remaining())
            begin += b.remaining()
        }
        return bytes
    }

    @Unroll
    def "BufferStagingArea"() {
        setup:
        def stagingArea = new BufferStagingArea(maxBuffSize, maxBuffSize)
        def generatedData = generateData(numBuffs, minBuffSize, maxBuffSize)
        def data = Flux.fromArray(generatedData.getT1())
        def expectedData = generatedData.getT2()

        when:
        def recoveredData = data.flatMapSequential(stagingArea.&write, 1)
            .concatWith(Flux.defer(stagingArea.&flush))
            .flatMap({aggregator -> ((BufferAggregator) aggregator).asFlux().collectList() })
            .collectList()
            .block()

        then:
        recoveredData.size() == expectedData.length
        for(int i = 0; i < expectedData.length; i++) {
            assert Arrays.equals(expectedData[i].array(), byteBufferListToByteArray(recoveredData[i]))
        }

        where:
        numBuffs | minBuffSize       | maxBuffSize       || _
        10       | 1000              | 1000              || _ /* These test no variation in buffSize. */
        100      | 1000              | 1000              || _
        1000     | 1000              | 1000              || _
        10000    | 1000              | 1000              || _
        10000    | 1                 | 1000              || _ /* These test variation in buffSize. */
        100      | 1                 | Constants.MB * 4  || _
        100      | Constants.MB * 4  | Constants.MB * 8  || _
    }

}
