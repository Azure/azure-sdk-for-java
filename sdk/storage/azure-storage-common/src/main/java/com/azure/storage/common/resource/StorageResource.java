// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.resource;

import java.nio.channels.ReadableByteChannel;
import java.util.List;

/**
 * Represents transfer capabilities of a single persistent storage resource, e.g. local file, blob, s3 object.
 */
public interface StorageResource {

    /**
     * Gets length of the resource.
     * @return Length of the resource.
     */
    long getLength();

    /**
     * Gets an abstract path segments of the resource.
     * <p>
     *     The path segments don't carry path separator which is specific to storage resource provider.
     * </p>
     * @return An abstract path segments of the resource.
     */
    List<String> getPath();

    /**
     * A flag indicating that the storage resource can consume an {@link ReadableByteChannel}. I.e., it can
     * transfer bytes from the {@link ReadableByteChannel} to itself.
     *
     * @return A flag indicating that the storage resource can consume an {@link ReadableByteChannel}.
     */
    boolean canConsumeReadableByteChannel();

    /**
     * A flag indicating that the storage resource can produce an {@link ReadableByteChannel}. I.e., the caller
     * can obtain an {@link ReadableByteChannel} that returns bytes stored by this storage resource.
     * @return A flag indicating that the storage resource can produce an {@link ReadableByteChannel}.
     */
    boolean canProduceReadableByteChannel();
    /**
     * A flag indicating that the storage resource can consume a URL. I.e., it can
     * transfer bytes from remote storage resource using provided URL.
     * <p>
     *     The URL can be either publicly accessible or it should contain authorization parameters.
     * </p>
     * @return A flag indicating that the storage resource can consume a URL.
     */
    boolean canConsumeUrl();
    /**
     * A flag indicating that the storage resource can produce a URL. I.e., a simple GET request
     * with returned URL returns bytes stored by this storage resource.
     * @return A flag indicating that the storage resource can produce a URL.
     */
    boolean canProduceUrl();

    /**
     * Opens an {@link ReadableByteChannel} to access bytes stored by this storage resource.
     *
     * TODO (kasobol-msft) In .NET look at System.IO.Pipes
     *
     * @return An {@link ReadableByteChannel} to access bytes stored by this storage resource.
     * @throws UnsupportedOperationException If {@link #canProduceReadableByteChannel()} returns {@code false}.
     */
    ReadableByteChannel openReadableByteChannel();
    /**
     * Writes bytes from provided {@link ReadableByteChannel} to itself.
     * @param channel An {@link ReadableByteChannel} with bytes to write.
     * @param length The length of the {@code inputStream}.
     * @throws UnsupportedOperationException If {@link #canConsumeReadableByteChannel()} ()} returns {@code false}.
     */
    void consumeReadableByteChannel(ReadableByteChannel channel, long length);

    /**
     * Returns a URL that can be used to access this resource with GET request.
     * @return A URL that can be used to access this resource with GET request.
     * @throws UnsupportedOperationException If {@link #canProduceUrl()} ()} ()} returns {@code false}.
     */
    String getUrl();

    /**
     * Copies bytes from provided {@code url} to itself.
     * @param url The url of a resource that's source of the bytes.
     * @throws UnsupportedOperationException If {@link #canConsumeUrl()} ()} ()} ()} returns {@code false}.
     */
    void consumeUrl(String url);

}
