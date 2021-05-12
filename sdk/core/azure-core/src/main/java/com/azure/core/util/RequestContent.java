// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.implementation.util.ArrayContent;
import com.azure.core.implementation.util.ByteBufferContent;
import com.azure.core.implementation.util.FileContent;
import com.azure.core.implementation.util.SerializableContent;
import com.azure.core.util.serializer.JsonSerializerProviders;
import com.azure.core.util.serializer.ObjectSerializer;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Represents the content sent as part of a request.
 */
public interface RequestContent {
    /**
     * Write the {@link RequestContent} to the {@link RequestOutbound}.
     *
     * @param requestOutbound The outbound where the request will be written.
     * @return An asynchronous response which will emit once the request has completed writing.
     */
    default Mono<Void> writeToAsync(RequestOutbound requestOutbound) {
        return Mono.defer(() -> Mono.fromRunnable(() -> writeTo(requestOutbound)));
    }

    /**
     * Write the {@link RequestContent} to the {@link RequestOutbound}.
     *
     * @param requestOutbound The outbound where the request will be written.
     */
    void writeTo(RequestOutbound requestOutbound);

    /**
     * Gets the length of the {@link RequestContent} if it is able to be calculated.
     * <p>
     * If the content length isn't able to be calculated null will be returned.
     *
     * @return The length of the {@link RequestContent} if it is able to be calculated, otherwise null.
     */
    Long getLength();

    /**
     * Creates a {@link RequestContent} that uses {@code byte[]} as its data.
     *
     * @param bytes The bytes that will be the {@link RequestContent} data.
     * @return A new {@link RequestContent}.
     */
    static RequestContent create(byte[] bytes) {
        Objects.requireNonNull(bytes, "'bytes' cannot be null.");
        return create(bytes, 0, bytes.length);
    }

    /**
     * Creates a {@link RequestContent} that uses {@code byte[]} as its data.
     *
     * @param bytes The bytes that will be the {@link RequestContent} data.
     * @param offset Offset in the bytes where the data will begin.
     * @param length Length of the data.
     * @return A new {@link RequestContent}.
     */
    static RequestContent create(byte[] bytes, int offset, int length) {
        Objects.requireNonNull(bytes, "'bytes' cannot be null.");
        return new ArrayContent(bytes, offset, length);
    }

    /**
     * Creates a {@link RequestContent} that uses {@link String} as its data.
     *
     * @param content The string that will be the {@link RequestContent} data.
     * @return A new {@link RequestContent}.
     */
    static RequestContent create(String content) {
        return create(content.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Creates a {@link RequestContent} that uses {@link BinaryData} as its data.
     *
     * @param content The {@link BinaryData} that will be the {@link RequestContent} data.
     * @return A new {@link RequestContent}.
     */
    static RequestContent create(BinaryData content) {
        return new ByteBufferContent(content.toByteBuffer());
    }

    /**
     * Creates a {@link RequestContent} that uses {@link Path} as its data.
     *
     * @param file The {@link Path} that will be the {@link RequestContent} data.
     * @return A new {@link RequestContent}.
     */
    static RequestContent create(Path file) {
        return create(file, 0, file.toFile().length());
    }

    /**
     * Creates a {@link RequestContent} that uses {@link Path} as its data.
     *
     * @param file The {@link Path} that will be the {@link RequestContent} data.
     * @param offset Offset in the {@link Path} where the data will begin.
     * @param length Length of the data.
     * @return A new {@link RequestContent}.
     */
    static RequestContent create(Path file, long offset, long length) {
        return new FileContent(file, offset, length);
    }

    /**
     * Creates a {@link RequestContent} that uses a serialized {@link Object} as its data.
     * <p>
     * This uses an {@link ObjectSerializer} found on the classpath.
     *
     * @param serializable An {@link Object} that will be serialized to be the {@link RequestContent} data.
     * @return A new {@link RequestContent}.
     */
    static RequestContent create(Object serializable) {
        return create(serializable, JsonSerializerProviders.createInstance(true));
    }

    /**
     * Creates a {@link RequestContent} that uses a serialized {@link Object} as its data.
     *
     * @param serializable An {@link Object} that will be serialized to be the {@link RequestContent} data.
     * @param serializer The {@link ObjectSerializer} that will serialize the {@link Object}.
     * @return A new {@link RequestContent}.
     */
    static RequestContent create(Object serializable, ObjectSerializer serializer) {
        return new SerializableContent(serializable, serializer);
    }
}
