// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.common.implementation;

import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class UploadUtilsTests {
    private static final ClientLogger LOGGER = new ClientLogger(UploadUtilsTests.class);

    @ParameterizedTest
    @MethodSource("data")
    public void computeMd5Md5(List<String> data) throws NoSuchAlgorithmException {
        byte[] md5 = MessageDigest.getInstance("MD5").digest("Hello World!".getBytes());
        Flux<ByteBuffer> flux
            = Flux.fromIterable(data).map(str -> ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8)));

        // computeMd5 = true
        StepVerifier.create(UploadUtils.computeMd5(flux, true, LOGGER))
            .assertNext(w -> TestUtils.assertArraysEqual(md5, w.getMd5()))
            .verifyComplete();

        // computeMd5 = false
        StepVerifier.create(UploadUtils.computeMd5(flux, false, LOGGER))
            .assertNext(w -> assertNull(w.getMd5()))
            .verifyComplete();
    }

    // This test checks that we maintain the integrity of data when we reset the buffers in the compute md5 calculation.
    @ParameterizedTest
    @MethodSource("data")
    public void computeMd5Data(List<String> data) {
        Flux<ByteBuffer> flux
            = Flux.fromIterable(data).map(str -> ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8)));

        // computeMd5 = true
        StepVerifier
            .create(FluxUtil
                .collectBytesInByteBufferStream(
                    UploadUtils.computeMd5(flux, true, LOGGER).flatMapMany(UploadUtils.FluxMd5Wrapper::getData))
                .map(bytes -> new String(bytes, StandardCharsets.UTF_8)))
            .assertNext(str -> assertEquals("Hello World!", str))
            .verifyComplete();

        // computeMd5 = false
        StepVerifier
            .create(FluxUtil
                .collectBytesInByteBufferStream(
                    UploadUtils.computeMd5(flux, false, LOGGER).flatMapMany(UploadUtils.FluxMd5Wrapper::getData))
                .map(bytes -> new String(bytes, StandardCharsets.UTF_8)))
            .assertNext(str -> assertEquals("Hello World!", str))
            .verifyComplete();
    }

    private static Stream<List<String>> data() {
        return Stream.of(Collections.singletonList("Hello World!"), Arrays.asList("Hello ", "World!"),
            Arrays.asList("H", "e", "l", "l", "o", " ", "W", "o", "r", "l", "d", "!"),
            Arrays.asList("Hel", "lo World!"));
    }
}
