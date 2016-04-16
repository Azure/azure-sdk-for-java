/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.datalake.store.uploader;

import com.microsoft.azure.CloudException;

import java.io.IOException;

/**
 * Created by begoldsm on 4/11/2016.
 */
public interface FrontEndAdapter {
    /**
     * Creates a new, empty stream at the given path.
     *
     * @param streamPath The relative path to the stream.
     * @param overwrite  Whether to overwrite an existing stream.
     */
    void CreateStream(String streamPath, boolean overwrite, byte[] data, int byteCount) throws Exception;

    /**
     * Creates a new, empty stream at the given path.
     *
     * @param streamPath The relative path to the stream.
     * @param recurse    if set to true recursively delete. This is used for folder streams only.
     */
    void DeleteStream(String streamPath, boolean recurse) throws Exception;

    /**
     * @param streamPath
     * @param data
     * @param offset
     * @param length
     */
    void AppendToStream(String streamPath, byte[] data, long offset, int length) throws Exception;

    /// <summary>
    /// Determines if the stream with given path exists.
    /// </summary>
    /// <param name="streamPath">The relative path to the stream.</param>
    /// <returns>True if the stream exists, false otherwise.</returns>

    /**
     * @param streamPath
     * @return
     */
    boolean StreamExists(String streamPath) throws Exception;

    /// <summary>
    /// Gets a value indicating the length of a stream, in bytes.
    /// </summary>
    /// <param name="streamPath">The relative path to the stream.</param>
    /// <returns>The length of the stream, in bytes.</returns>
    long GetStreamLength(String streamPath) throws Exception;

    /// <summary>
    /// Concatenates the given input streams (in order) into the given target stream.
    /// At the end of this operation, input streams will be deleted.
    /// </summary>
    /// <param name="targetStreamPath">The relative path to the target stream.</param>
    /// <param name="inputStreamPaths">An ordered array of paths to the input streams.</param>
    void Concatenate(String targetStreamPath, String[] inputStreamPaths) throws Exception;
}
