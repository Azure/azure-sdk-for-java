// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation;

import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UploadUtilsTests {

    @ParameterizedTest
    @MethodSource("computeMd5Supplier")
    public void computeMd5Md5(List<String> data) throws NoSuchAlgorithmException {
        byte[] md5 = MessageDigest.getInstance("MD5").digest("Hello World!".getBytes());

        // Create ByteBuffer flux from the data
        List<ByteBuffer> byteBuffers = new ArrayList<>();
        for (String str : data) {
            byteBuffers.add(ByteBuffer.wrap(str.getBytes()));
        }
        Flux<ByteBuffer> flux = Flux.fromIterable(byteBuffers);

        // When computeMd5 = true
        StepVerifier.create(UploadUtils.computeMd5(flux, true, new ClientLogger(UploadUtilsTests.class)))
            .expectNextMatches(w -> Arrays.equals(w.getMd5(), md5))
            .expectComplete()
            .verify();

        // When computeMd5 = false
        StepVerifier.create(UploadUtils.computeMd5(flux, false, new ClientLogger(UploadUtilsTests.class)))
            .expectNextMatches(w -> w.getMd5() == null)
            .expectComplete()
            .verify();
    }

    @ParameterizedTest
    @MethodSource("computeMd5Supplier")
    void computeMd5Data(List<String> data) {
        // This test checks that we maintain the integrity of data when we reset the buffers in the compute md5 calculation.

        // Create ByteBuffer flux from the data
        Flux<ByteBuffer> flux = Flux.fromIterable(data.stream()
            .map(str -> ByteBuffer.wrap(str.getBytes()))
            .collect(Collectors.toList()));

        // When computeMd5 = true
        StepVerifier.create(
                UploadUtils.computeMd5(flux, true, new ClientLogger(UploadUtilsTests.class))
                    .flatMapMany(UploadUtils.FluxMd5Wrapper::getData)
                    .reduce(new ByteArrayOutputStream(), (baos, buffer) -> {
                        byte[] bytes = new byte[buffer.remaining()];
                        buffer.get(bytes);
                        baos.write(bytes, 0, bytes.length);
                        return baos;
                    })
                    .map(ByteArrayOutputStream::toByteArray)
                    .map(bytes -> new String(bytes, StandardCharsets.UTF_8))
            )
            .expectNext("Hello World!")
            .expectComplete()
            .verify();

        // Recreate ByteBuffer flux from the data
        flux = Flux.fromIterable(data.stream()
            .map(str -> ByteBuffer.wrap(str.getBytes()))
            .collect(Collectors.toList()));

        // When computeMd5 = false
        StepVerifier.create(
                UploadUtils.computeMd5(flux, false, new ClientLogger(UploadUtilsTests.class))
                    .flatMapMany(UploadUtils.FluxMd5Wrapper::getData)
                    .reduce(new StringBuilder(), (sb, buffer) -> {
                        byte[] bytes = FluxUtil.byteBufferToArray(buffer);
                        sb.append(new String(bytes, StandardCharsets.UTF_8));
                        return sb;
                    })
                    .map(StringBuilder::toString)
            )
            .expectNext("Hello World!")
            .expectComplete()
            .verify();
    }

    private static Stream<List<String>> computeMd5Supplier() {
        return Stream.of(Arrays.asList("Hello World!"),
            Arrays.asList("Hello ", "World!"),
            Arrays.asList("H", "e", "l", "l", "o", " ", "W", "o", "r", "l", "d", "!"),
            Arrays.asList("Hel", "lo World!")
        );
    }

}
