// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation

import com.azure.core.util.FluxUtil
import com.azure.core.util.logging.ClientLogger
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.security.MessageDigest

class UploadUtilsTest extends Specification {

    @Unroll
    def "computeMd5 md5"() {
        setup:
        def md5 = MessageDigest.getInstance("MD5").digest("Hello World!".getBytes())
        def flux = Flux.fromIterable(data.stream().map({ str -> ByteBuffer.wrap(str.getBytes())}) as Iterable<ByteBuffer>)

        when: "computeMd5 = true"
        def sv = StepVerifier.create(UploadUtils.computeMd5(flux, true, new ClientLogger(UploadUtilsTest.class)))

        then:
        sv.expectNextMatches({ w -> w.getMd5() == md5 })
            .expectComplete()

        when: "computeMd5 = false"
        sv = StepVerifier.create(UploadUtils.computeMd5(flux, false, new ClientLogger(UploadUtilsTest.class)))

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
        def flux = Flux.fromIterable(data.stream().map({ str -> ByteBuffer.wrap(str.getBytes())}) as Iterable<ByteBuffer>)

        when: "computeMd5 = true"
        def sv = StepVerifier.create(
            UploadUtils.computeMd5(flux, true, new ClientLogger(UploadUtilsTest.class))
                .flatMapMany({ wrapper -> wrapper.getData() })
                .reduce(new StringBuilder(),  { sb, buffer ->
                    sb.append(FluxUtil.byteBufferToArray(buffer))
                    return sb
                }).map( { sb -> sb.toString()} ))

        then:
        sv.expectNext("Hello World!")
            .expectComplete()

        when: "computeMd5 = false"
        sv = StepVerifier.create(
            UploadUtils.computeMd5(flux, false, new ClientLogger(UploadUtilsTest.class))
                .flatMapMany({ wrapper -> wrapper.getData() })
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
}
