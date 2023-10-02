// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.test.utils;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 Utilities to compute hashes in tests.
 */
public final class MessageDigestUtils {

    private MessageDigestUtils() {
    }

    /**
     * Returns base64 encoded MD5 of bytes.
     * @param bytes bytes.
     * @return base64 encoded MD5 of bytes.
     * @throws RuntimeException if md5 is not found.
     */
    public static String md5(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(bytes);
            return Base64.getEncoder().encodeToString(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns base64 encoded MD5 of flux of byte buffers.
     * @param bufferFlux flux of byte buffers.
     * @return Mono that emits base64 encoded MD5 of bytes.
     */
    public static Mono<String> md5(Flux<ByteBuffer> bufferFlux) {
        return bufferFlux.reduceWith(() -> {
            try {
                return MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw Exceptions.propagate(e);
            }
        }, (digest, buffer) -> {
            digest.update(buffer);
            return digest;
        }).map(digest -> Base64.getEncoder().encodeToString(digest.digest()));
    }
}
