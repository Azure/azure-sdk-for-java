// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation

import com.azure.core.exception.HttpResponseException
import com.azure.core.test.http.MockHttpResponse
import reactor.core.publisher.Flux
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.ByteBuffer

class FluxInputStreamTest extends Specification {

    /* Network tests to be performed by implementors of the FluxInputStream. */
    Flux<ByteBuffer> generateData(int num) {
        List<ByteBuffer> buffers = new ArrayList<>()
        for(int i = 0; i < num; i++) {
            buffers.add(ByteBuffer.wrap(i.byteValue()))
        }
        return Flux.fromIterable(buffers)
    }

    @Unroll
    def "FluxIS min"() {
        setup:
        def data = generateData(num)

        when:
        def is = new FluxInputStream(data)
        def bytes = new byte[num]

        def totalRead = 0
        def bytesRead = 0

        while (bytesRead != -1 && totalRead < num) {
            bytesRead = is.read(bytes, totalRead, num)
            if (bytesRead != -1) {
                totalRead += bytesRead
                num -= bytesRead
            }
        }

        is.close()

        then:
        for (int i = 0; i < num; i++) {
            assert bytes[i] == i.byteValue()
        }

        where:
        num             || _
        1               || _
        10              || _
        100             || _
        Constants.KB    || _
        Constants.MB    || _
    }

    @Unroll
    def "FluxIS with empty byte buffers"() {
        setup:
        def num = Constants.KB
        List<ByteBuffer> buffers = new ArrayList<>()
        for(int i = 0; i < num; i++) {
            buffers.add(ByteBuffer.wrap(i.byteValue()))
            buffers.add(ByteBuffer.wrap(new byte[0]))
        }
        def data = Flux.fromIterable(buffers)

        when:
        def is = new FluxInputStream(data)
        def bytes = new byte[num]

        def totalRead = 0
        def bytesRead = 0

        while (bytesRead != -1 && totalRead < num) {
            bytesRead = is.read(bytes, totalRead, num)
            if (bytesRead != -1) {
                totalRead += bytesRead
                num -= bytesRead
            }
        }

        is.close()

        then:
        for (int i = 0; i < num; i++) {
            assert bytes[i] == i.byteValue()
        }
    }

    def "FluxIS error"() {
        setup:
        def data = Flux.error(exception)

        when:
        def is = new FluxInputStream(data)

        is.read()

        is.close()

        then:
        thrown(IOException)

        where:
        exception                                                                                  || _
        new IllegalArgumentException("Mock illegal argument exception.")                           || _
        new HttpResponseException("Mock storage exception", new MockHttpResponse(null, 404), null) || _
        new UncheckedIOException(new IOException("Mock IO Exception."))                            || _
    }

}
