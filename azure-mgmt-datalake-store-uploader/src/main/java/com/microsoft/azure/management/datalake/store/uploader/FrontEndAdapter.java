/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.datalake.store.uploader;

public interface FrontEndAdapter {
    /**
     * Creates a new, empty stream at the given path.
     *
     * @param streamPath The relative path to the stream.
     * @param overwrite  Whether to overwrite an existing stream.
     */
    void CreateStream(String streamPath, boolean overwrite, byte[] data, int byteCount) throws Exception;

    /**
     * Deletes an existing stream at the given path.
     *
     * @param streamPath The relative path to the stream.
     * @param recurse    if set to true recursively delete. This is used for folder streams only.
     */
    void DeleteStream(String streamPath, boolean recurse) throws Exception;

    /**
     * Appends the given byte array to the end of a given stream.
     *
     * @param streamPath The relative path to the stream.
     * @param data An array of bytes to be appended to the stream.
     * @param offset The offset at which to append to the stream.
     * @param length The number of bytes to append (starting at 0).
     */
    void AppendToStream(String streamPath, byte[] data, long offset, int length) throws Exception;

    /**
     * Determines if the stream with given path exists.
     *
     * @param streamPath The relative path to the stream.
     * @return True if the stream exists, false otherwise.
     */
    boolean StreamExists(String streamPath) throws Exception;

    /**
     * Gets a value indicating the length of a stream, in bytes.
     *
     * @param streamPath The relative path to the stream.
     * @return The length of the stream, in bytes.
     * @throws Exception
     */
    long GetStreamLength(String streamPath) throws Exception;

    /**
     * Concatenates the given input streams (in order) into the given target stream.
     * At the end of this operation, input streams will be deleted.
     *
     * @param targetStreamPath The relative path to the target stream.
     * @param inputStreamPaths An ordered array of paths to the input streams.
     * @throws Exception
     */
    void Concatenate(String targetStreamPath, String[] inputStreamPaths) throws Exception;
}
