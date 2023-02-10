package com.azure.storage.common

import com.azure.storage.common.implementation.Constants
import com.ctc.wstx.shaded.msv_core.verifier.jarv.Const
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.ByteBuffer

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
            getCachedLength() >> data.length
        }

        def channel = new StorageSeekableByteChannel(chunkSize, StorageChannelMode.READ, behavior)
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
            getCachedLength() >> data.length
        }
        def channel = new StorageSeekableByteChannel(bufferLength, StorageChannelMode.READ, behavior)

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
}
