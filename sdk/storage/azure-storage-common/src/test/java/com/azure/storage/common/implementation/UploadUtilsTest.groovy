// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation

import com.azure.core.http.HttpResponse
import com.azure.core.test.http.MockHttpResponse
import com.azure.core.util.BinaryData
import com.azure.core.util.FluxUtil
import com.azure.core.util.logging.ClientLogger
import com.azure.storage.common.ParallelTransferOptions
import com.azure.storage.common.test.shared.BinaryDataProvider
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.security.MessageDigest
import java.util.function.Function

class UploadUtilsTest extends Specification {

    @Unroll
    def "computeMd5 md5"() {
        setup:
        def md5 = MessageDigest.getInstance("MD5").digest("Hello World!".getBytes())
        def bdata = BinaryData.fromList(data.stream().map({ str -> ByteBuffer.wrap(str.getBytes())}).toList())

        when: "computeMd5 = true"
        def sv = StepVerifier.create(UploadUtils.computeMd5(bdata, true, new ClientLogger(UploadUtilsTest.class)))

        then:
        sv.expectNextMatches({ w -> w.getMd5() == md5 })
            .expectComplete()

        when: "computeMd5 = false"
        sv = StepVerifier.create(UploadUtils.computeMd5(bdata, false, new ClientLogger(UploadUtilsTest.class)))

        then:
        sv.expectNextMatches({ w -> w.getMd5() == null })
            .expectComplete()

        where:
        data             || _
        ["Hello World!"] || _
        ["Hello ", "World!"] || _
        ["H", "e", "l", "l", "o", " ", "W", "o", "r", "l", "d", "!"] || _
        ["Hel", "lo World!"] || _
    }

    @Unroll
    def "computeMd5 data"() { // This test checks that we maintain the integrity of data when we reset the buffers in the compute md5 calculation.
        setup:
        def bdata = BinaryData.fromList(data.stream().map({ str -> ByteBuffer.wrap(str.getBytes())}).toList())

        when: "computeMd5 = true"
        def sv = StepVerifier.create(
            UploadUtils.computeMd5(bdata, true, new ClientLogger(UploadUtilsTest.class))
                .flatMapMany({ wrapper -> wrapper.getData().toFluxByteBuffer() })
                .reduce(new StringBuilder(),  { sb, buffer ->
                    sb.append(FluxUtil.byteBufferToArray(buffer))
                    return sb
                }).map( { sb -> sb.toString()} ))

        then:
        sv.expectNext("Hello World!")
            .expectComplete()

        when: "computeMd5 = false"
        sv = StepVerifier.create(
            UploadUtils.computeMd5(bdata, false, new ClientLogger(UploadUtilsTest.class))
                .flatMapMany({ wrapper -> wrapper.getData().toFluxByteBuffer() })
                .reduce(new StringBuilder(),  { sb, buffer ->
                    sb.append(FluxUtil.byteBufferToArray(buffer))
                    return sb
                }).map( { sb -> sb.toString()} ))

        then:
        sv.expectNext("Hello World!")
            .expectComplete()


        where:
        data             || _
        ["Hello World!"] || _
        ["Hello ", "World!"] || _
        ["H", "e", "l", "l", "o", " ", "W", "o", "r", "l", "d", "!"] || _
        ["Hel", "lo World!"] || _
    }

    def "uploadFullOrChunked chunked various sources"() {
        setup:
        def parallel = new ParallelTransferOptions().setBlockSizeLong(64)
            .setMaxSingleUploadSizeLong(64).setMaxConcurrency(8)

        when:
        UploadUtils.uploadFullOrChunked(data.getData(), parallel, this::consumeData, this::consumeData)

        then:
        notThrown(Exception)

        where:
        data << BinaryDataProvider.all(Constants.KB, null, getRng(new Random(123456789L))).toList()
    }

    def "uploadFullOrChunked full various sources"() {
        setup:
        def parallel = new ParallelTransferOptions().setBlockSizeLong(Integer.MAX_VALUE)
            .setMaxSingleUploadSizeLong(Integer.MAX_VALUE).setMaxConcurrency(8)

        when:
        UploadUtils.uploadFullOrChunked(data.getData(), parallel, this::consumeData, this::consumeData)

        then:
        notThrown(Exception)

        where:
        data << BinaryDataProvider.all(Constants.KB, null, getRng(new Random(123456789L))).toList()
    }

    private static Function<Integer, byte[]> getRng(Random random) {
        return { Integer size ->
            def bytes = new byte[size]
            random.nextBytes()
            return bytes
        }
    }

    private static Mono<HttpResponse> consumeData(BinaryData data) {
        data.toFluxByteBuffer()
            .map({it.get(new byte[it.remaining()])})
            .blockLast()
        return Mono.just(new MockHttpResponse(null, 201))
    }
}
