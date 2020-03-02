// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.quickquery

import com.azure.core.http.rest.ResponseBase
import com.azure.core.test.http.MockHttpResponse
import com.azure.core.util.logging.ClientLogger
import com.azure.storage.blob.models.BlobRequestConditions
import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.common.implementation.Constants
import com.azure.storage.quickquery.implementation.util.NetworkInputStream
import reactor.core.publisher.Flux
import spock.lang.Unroll

import java.nio.ByteBuffer

class NetworkInputStreamAPITest extends APISpec {

    Flux<ByteBuffer> generateData(int num) {
        List<ByteBuffer> buffers = new ArrayList<>()
        for(int i = 0; i < num; i++) {
            buffers.add(ByteBuffer.wrap(i.byteValue()))
        }
        return Flux.fromIterable(buffers)
    }

    @Unroll
    def "NetworkIS min"() {
        setup:
        def data = generateData(num)

        when:
        def is = new NetworkInputStream(data, new ClientLogger(NetworkInputStream.class))
        def bytes = new byte[num]

        def totalRead = 0
        def bytesRead = 0

        while (bytesRead != -1) {
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

    def "NetworkIS error"() {
        setup:
        def data = Flux.error(exception)

        when:
        def is = new NetworkInputStream(data, new ClientLogger(NetworkInputStream.class))

        is.read()

        then:
        def e = thrown(Throwable)
        e instanceof IllegalArgumentException || e instanceof BlobStorageException

        is.close()

        where:
        exception                                                                                       || _
        new IllegalArgumentException("Mock illegal argument exception.")                                || _
        new BlobStorageException("Mock blob storage exception", new MockHttpResponse(null, 404), null)  || _
    }

    @Unroll
    def "NetworkIS network download"() {
        setup:
        bcAsync.upload(generateData(num), null, true).block()
        def data = bcAsync.download()

        when:
        def is = new NetworkInputStream(data, new ClientLogger(NetworkInputStream.class))
        def bytes = new byte[num]

        def totalRead = 0
        def bytesRead = 0

        while (bytesRead != -1) {
            bytesRead = is.read(bytes, totalRead, num)
            if (bytesRead != -1) {
                totalRead += bytesRead
                num -= bytesRead
            }
        }

        is.close()

        then:
        for (int j = 0; j < num; j++) {
            assert bytes[j] == j.byteValue()
        }

        where:
        num             || _
        1               || _
        10              || _
        100             || _
        Constants.KB    || _
        Constants.MB    || _
    }

    // TODO : Determine wy this does not propogate correctly
    @Unroll
    def "Network IS error BlobStorageException"() {
        setup:
        setupBlobLeaseCondition(bc, garbageLeaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(garbageLeaseID)

        def qqAsyncClient = new BlobQuickQueryClientBuilder(bcAsync).buildAsyncClient()
        def data = qqAsyncClient.queryWithResponse("SELECT * from BlobStorage", null, null, bac)
            .flatMapMany(ResponseBase.&getValue)

        when:
        def is = new NetworkInputStream(data, new ClientLogger(NetworkInputStream.class))

        is.read()

        then:
        thrown(BlobStorageException)
        is.close()
    }

    @Unroll
    def "NetworkIS error IA"() {
        setup:
        def qqAsyncClient = new BlobQuickQueryClientBuilder(bcAsync).buildAsyncClient()
        def data = qqAsyncClient.queryWithResponse("SELECT * from BlobStorage", input, output, null)
            .flatMapMany(ResponseBase.&getValue)

        when:
        def is = new NetworkInputStream(data, new ClientLogger(NetworkInputStream.class))
        is.read()

        then:
        thrown(IllegalArgumentException)
        is.close()

        where:
        input                                                    | output                                                   || _
        new MockSerialization().setRecordSeparator('\n' as char) | null                                                     || _
        null                                                     | new MockSerialization().setRecordSeparator('\n' as char) || _
    }

}

