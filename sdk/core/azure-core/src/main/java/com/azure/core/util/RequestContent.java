// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.implementation.util.ArrayContent;
import com.azure.core.util.serializer.ObjectSerializer;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Represents the content sent as part of a request.
 */
public abstract class RequestContent {
    /**
     * Write the {@link RequestContent} to the {@link RequestOutbound}.
     *
     * @param requestOutbound The outbound where the request will be written.
     * @return An asynchronous response which will emit once the request has completed writing.
     */
    public abstract Mono<Void> writeToAsync(RequestOutbound requestOutbound);

    /**
     * Write the {@link RequestContent} to the {@link RequestOutbound}.
     *
     * @param requestOutbound The outbound where the request will be written.
     */
    public abstract void writeTo(RequestOutbound requestOutbound);

    /**
     * Gets the length of the {@link RequestContent}, if it is able to be calculated.
     *
     * @return The length of the {@link RequestContent}, if it is able to be calculated.
     */
    public abstract Long attemptToGetLength();

    /**
     * Creates a {@link RequestContent} comprised of the {@code bytes}.
     *
     * @param bytes The bytes that will be the {@link RequestContent} data.
     * @return A new {@link RequestContent}.
     */
    public static RequestContent create(byte[] bytes) {
        Objects.requireNonNull(bytes, "'bytes' cannot be null.");
        return create(bytes, 0, bytes.length);
    }

    /**
     * Creates a {@link RequestContent} comprised of the {@code bytes}.
     *
     * @param bytes The bytes that will be the {@link RequestContent} data.
     * @param offset Offset in the bytes where the data will begin.
     * @param length Length of the data.
     * @return A new {@link RequestContent}.
     */
    public static RequestContent create(byte[] bytes, int offset, int length) {
        Objects.requireNonNull(bytes, "'bytes' cannot be null.");
        return new ArrayContent(bytes, offset, length);
    }

    /**
     * Creates a {@link RequestContent} comprised of the {@code content}.
     *
     * @param content
     * @return
     */
    public static RequestContent create(String content) {
        return create(content.getBytes(StandardCharsets.UTF_8));
    }

    /**
     *
     * @param content
     * @return
     */
    public static RequestContent create(BinaryData content) {
        return create(content.toBytes());
    }

    /**
     *
     * @param serializable
     * @return
     */
    public static RequestContent create(Object serializable) {
        return create(serializable, null);
    }

    /**
     *
     * @param serializable
     * @param serializer
     * @return
     */
    public static RequestContent create(Object serializable, ObjectSerializer serializer) {
        return create(serializer.serializeToBytes(serializable));
    }
}
