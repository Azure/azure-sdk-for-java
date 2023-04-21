// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation

import com.azure.storage.common.implementation.Constants
import com.azure.storage.common.implementation.StorageSeekableByteChannel
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.nio.channels.NonReadableChannelException
import java.nio.channels.NonWritableChannelException

class StorageSeekableByteChannelTest extends Specification {
    private byte[] getRandomData(int size) {
        def result = new byte[size]
        new Random().nextBytes(result)
        return result
    }

    @Unroll
    def "Read sequentially"() {
        setup:
        def data = getRandomData(dataSize)
        StorageSeekableByteChannel.ReadBehavior behavior = Stub() {
            read(_, _) >> { ByteBuffer dst, long sourceOffset ->
                assert sourceOffset >= 0
                if (sourceOffset >= data.length) {
                    return -1
                }
                def read = Math.min(dst.remaining(), data.length - (int) sourceOffset)
                dst.put(data, (int) sourceOffset, read)
                return read
            }
            getResourceLength() >> data.length
        }

        def channel = new StorageSeekableByteChannel(chunkSize, behavior, 0L)
        def dest = new ByteArrayOutputStream()

        when:
        def temp = new byte[readLength]
        int read
        while ((read = channel.read(ByteBuffer.wrap(temp))) != -1) {
            dest.write(temp, 0, read)
        }

        then:
        channel.size() == data.length
        dest.toByteArray() == data

        where:
        dataSize         | chunkSize    | readLength
        8 * Constants.KB | Constants.KB | Constants.KB // easy path
        8 * Constants.KB | Constants.KB | 100          // reads unaligned (smaller)
        8 * Constants.KB | Constants.KB | 1500         // reads unaligned (larger)
        8 * Constants.KB | 1000         | 1000         // buffer unaligned
        100              | Constants.KB | Constants.KB // buffer larger than data
    }

    def "Seek within buffer"() {
        setup:
        def bufferLength = 4 * Constants.KB
        def data = getRandomData(2 * bufferLength)

        StorageSeekableByteChannel.ReadBehavior behavior = Mock {
            // expect only one read call of any parameter set
            1 * read(_, _) >> { ByteBuffer dst, long sourceOffset ->
                assert sourceOffset == 0 // test is designed to have gotten its buffer at position 0, ensure we do this
                def read = Math.min(dst.remaining(), data.length - (int)sourceOffset)
                dst.put(data, (int)sourceOffset, read)
                return read
            }
            getResourceLength() >> data.length
        }
        def channel = new StorageSeekableByteChannel(bufferLength, behavior, 0L)

        expect:
        def temp = ByteBuffer.allocate(100)
        for (long seekIndex : [
            0,                  // initial read at 0 to control buffer location
            bufferLength/2,     // seek somewhere in middle
            bufferLength/2,     // seek back to that same spot
            0,                  // seek back to beginning
            bufferLength - 100, // seek to last temp.length chunk of the internal buffer
            bufferLength - 1    // seek to last byte of the internal buffer
        ]) {
            temp.clear()
            channel.position(seekIndex)
            assert channel.read(temp) == Math.min(temp.limit(), bufferLength - seekIndex)
        }
    }

    def "Seek to new buffer"() {
        setup:
        def bufferLength = 5
        def data = getRandomData(Constants.KB)
        // each index should be outside the previous buffer
        long[] seekIndices = [20, 500, 1, 6, 5]

        StorageSeekableByteChannel.ReadBehavior behavior = Mock {
            getResourceLength() >> data.length
        }
        def channel = new StorageSeekableByteChannel(bufferLength, behavior, 0L)

        when:
        def temp = ByteBuffer.allocate(bufferLength * 2)
        for (long seekIndex : seekIndices) {
            temp.clear()
            channel.position(seekIndex)
            channel.read(temp)
        }

        then:
        // expect a buffer refill at each seek index
        for (long l : seekIndices) {
            1 * behavior.read(_, l) >> { ByteBuffer dst, long sourceOffset ->
                dst.put(data, (int)sourceOffset, bufferLength)
                return bufferLength
            }
        }
    }

    def "Seek results in correct read"() {
        setup:
        def bufferLength = 5
        def data = getRandomData(Constants.KB)
        long seekIndex = 345

        StorageSeekableByteChannel.ReadBehavior behavior = Mock {
            getResourceLength() >> data.length
        }
        def channel = new StorageSeekableByteChannel(bufferLength, behavior, 0L)

        when:
        ByteBuffer result = ByteBuffer.allocate(bufferLength)
        channel.position(seekIndex)
        channel.read(result)

        then:
        result.array() == data[seekIndex..seekIndex+bufferLength-1] as byte[]
        // expect exactly one read at the chosen index
        1 * behavior.read(_, seekIndex) >> { ByteBuffer dst, long sourceOffset ->
            dst.put(data, (int)sourceOffset, bufferLength)
            return bufferLength
        }
        // expect no other reads
        0 * behavior.read(_, _)
    }

    def "Read past resource end"() {
        given:
        StorageSeekableByteChannel.ReadBehavior behavior = Stub {
            getResourceLength() >> resourceSize
            read(_,_) >> { ByteBuffer dst, long sourceOffset ->
                def toRead = Math.min(dst.remaining(), resourceSize - sourceOffset)
                if (toRead > 0) {
                    dst.put(new byte[toRead])
                    return toRead
                } else {
                    return -1
                }
            }
        }
        def channel = new StorageSeekableByteChannel(resourceSize, behavior, 0L)

        when: "read past resource end"
        channel.position(offset)
        def read = channel.read(ByteBuffer.allocate(resourceSize))

        then: "Graceful operation"
        notThrown(Throwable)

        and: "Appropriate values"
        read == expectedReadLength
        channel.position() == resourceSize
        channel.size() == resourceSize


        where:
        resourceSize | offset            | expectedReadLength
        Constants.KB | 500               | Constants.KB - 500 // overlap on end of resource
        Constants.KB | Constants.KB      | -1                 // starts at end of resource
        Constants.KB | Constants.KB + 20 | -1                 // completely past resource
    }

    @Unroll
    def "Write"() {
        setup:
        byte[] source = getRandomData(dataSize)
        byte[] dest = new byte[dataSize]
        StorageSeekableByteChannel.WriteBehavior behavior = Stub() {
            write(_, _) >> { ByteBuffer src, long destOffset ->
                src.get(dest, (int)destOffset, src.remaining())
            }
        }
        def channel = new StorageSeekableByteChannel(chunkSize, behavior, 0L)

        when:
        for (int i = 0, bytesLastWritten; i < source.length; i += bytesLastWritten) {
            bytesLastWritten = channel.write(ByteBuffer.wrap(source, i, Math.min(writeSize, source.length - i)))
        }
        channel.close()

        then:
        source == dest

        where:
        dataSize         | chunkSize    | writeSize
        8 * Constants.KB | Constants.KB | Constants.KB // easy path
        8 * Constants.KB | Constants.KB | 100          // writes unaligned (smaller)
        8 * Constants.KB | Constants.KB | 1500         // writes unaligned (larger)
        8 * Constants.KB | 1000         | 1000         // buffer unaligned
        100              | Constants.KB | Constants.KB // buffer larger than data
    }

    def "Write mode seek"() {
        given:
        def bufferSize = Constants.KB
        StorageSeekableByteChannel.WriteBehavior writeBehavior = Mock()
        def channel = new StorageSeekableByteChannel(bufferSize, writeBehavior, 0L)

        when: "Write partial data then seek"
        channel.write(ByteBuffer.wrap(getRandomData(bufferSize - 5)))
        channel.position(2048)

        then: "behavior write correctly called once"
        1 * writeBehavior.write({ ByteBuffer bb -> bb.limit() == bufferSize - 5 }, 0L)
        0 * writeBehavior.write(_,_)

        when: "Fill entire buffer"
        channel.write(ByteBuffer.wrap(getRandomData(bufferSize)))
        channel.position(0)

        then: "behavior write correctly called once"
        1 * writeBehavior.write({ ByteBuffer bb -> bb.limit() == bufferSize }, 2048L)
        0 * writeBehavior.write(_,_)

        when: "No data before seek"
        channel.position(1000)

        then: "behavior write not called"
        0 * writeBehavior.write(_,_)
    }

    def "Write mode seek obeys behavior"() {
        given: "Channel that allows you to seek in 512 byte increments"
        StorageSeekableByteChannel.WriteBehavior writeBehavior = Mock() {
            assertCanSeek(_) >> { long index -> if (index % 512 != 0) throw new UnsupportedOperationException() }
        }
        def channel = new StorageSeekableByteChannel(Constants.KB, writeBehavior, 0L)

        when: "Seek to 0"
        channel.position(0)

        then: "success"
        channel.position() == 0

        when: "Seek to 512"
        channel.position(512)

        then: "success"
        channel.position() == 512

        when: "Seek to 5 gigs"
        channel.position(5 * Constants.GB)

        then: "success"
        channel.position() == 5 * Constants.GB

        when: "Seek is invalid"
        channel.position(100)

        then: "failure"
        def e = thrown(UnsupportedOperationException)
    }

    def "Failed behavior write can resume where left off"() {
        given: "Channel with behavior that throws first write attempt"
        def testWriteDest = ByteBuffer.allocate(Constants.KB)
        StorageSeekableByteChannel.WriteBehavior writeBehavior = Mock() {
            2 * write(_,0) >> { throw new RuntimeException("mock behavior interrupt") } >> { ByteBuffer src, long offset -> testWriteDest.put(src) }
            0 * write(_, _)
        }
        def channel = new StorageSeekableByteChannel(Constants.KB, writeBehavior, 0L)

        when: "first attempt"
        def data1 = ByteBuffer.wrap(getRandomData(Constants.KB))
        channel.write(data1)

        then: "failure; channel state unchanged"
        def e = thrown(RuntimeException)
        e.message == "mock behavior interrupt"
        channel.position() == 0

        when: "second attempt"
        def data2 = ByteBuffer.wrap(getRandomData(Constants.KB))
        def written = channel.write(data2)

        then: "success; channel state updated, data correctly written"
        notThrown(Throwable)
        data2.position() == Constants.KB
        written == Constants.KB
        channel.position() == Constants.KB
        testWriteDest.array() == data2.array()
    }

    def "Write mode cannot read"() {
        setup:
        def channel = new StorageSeekableByteChannel(Constants.KB, Mock(StorageSeekableByteChannel.WriteBehavior), 0L)

        when:
        channel.read(ByteBuffer.allocate(Constants.KB))

        then:
        thrown(NonReadableChannelException)
    }

    def "Read mode cannot write"() {
        setup:
        def channel = new StorageSeekableByteChannel(Constants.KB, Mock(StorageSeekableByteChannel.ReadBehavior), 0L)

        when:
        channel.write(ByteBuffer.allocate(Constants.KB))

        then:
        thrown(NonWritableChannelException)
    }
}
