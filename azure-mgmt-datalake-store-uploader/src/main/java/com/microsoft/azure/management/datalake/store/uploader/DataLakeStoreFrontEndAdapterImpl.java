/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.datalake.store.uploader;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.datalake.store.DataLakeStoreFileSystemManagementClient;
import com.microsoft.azure.management.datalake.store.models.FileStatusResult;

import java.io.IOException;

/**
 * Created by begoldsm on 4/11/2016.
 */
public class DataLakeStoreFrontEndAdapterImpl implements FrontEndAdapter {
    /// <summary>
    /// A front end adapter that communicates with the DataLake Store.
    /// This is a syncrhonous call adapter, which has certain efficiency limitations.
    /// In the future, new adapters that are created should consider implementing the methods
    /// asynchronously.
    /// </summary>


    private String _accountName;

    private DataLakeStoreFileSystemManagementClient _client;

    private final int PerRequestTimeoutMs = 30000; // 30 seconds and we timeout the request

    /// <summary>
    /// Initializes a new instance of the <see cref="DataLakeStoreFrontEndAdapter"/> class.
    /// </summary>
    /// <param name="accountName">Name of the account.</param>
    /// <param name="client">The client.</param>
    /// <param name="token">The token.</param>
    public DataLakeStoreFrontEndAdapterImpl(String accountName, DataLakeStoreFileSystemManagementClient client) {
        _accountName = accountName;
        _client = client;
    }


    /// <summary>
    /// Creates a new, empty stream at the given path.
    /// </summary>
    /// <param name="streamPath">The relative path to the stream.</param>
    /// <param name="overwrite">Whether to overwrite an existing stream.</param>
    /// <param name="data"></param>
    /// <param name="byteCount"></param>
    public void CreateStream(String streamPath, boolean overwrite, byte[] data, int byteCount) throws CloudException, IOException {
        byte[] toCreate;
        if (data == null) {
            toCreate = new byte[0];
        } else {
            toCreate = new byte[byteCount];
            System.arraycopy(data, 0, toCreate, 0, byteCount);
        }
        _client.getFileSystemOperations().create(streamPath, _accountName, data, overwrite);
    }

    /// <summary>
    /// Deletes an existing stream at the given path.
    /// </summary>
    /// <param name="streamPath">The relative path to the stream.</param>
    /// <param name="recurse">if set to <c>true</c> [recurse]. This is used for folder streams only.</param>
    public void DeleteStream(String streamPath, boolean recurse) throws IOException, CloudException {
        _client.getFileSystemOperations().delete(streamPath, _accountName, recurse);
    }

    /// <summary>
    /// Appends to stream.
    /// </summary>
    /// <param name="streamPath">The stream path.</param>
    /// <param name="data">The data.</param>
    /// <param name="offset">The offset.</param>
    /// <param name="byteCount">The byte count.</param>
    public void AppendToStream(String streamPath, byte[] data, long offset, int byteCount) throws IOException, CloudException {
        byte[] toAppend = new byte[byteCount];
        System.arraycopy(data, 0, toAppend, 0, byteCount);
        _client.getFileSystemOperations().append(streamPath, _accountName, toAppend);
    }

    /// <summary>
    /// Determines if the stream with given path exists.
    /// </summary>
    /// <param name="streamPath">The relative path to the stream.</param>
    /// <returns>
    /// True if the stream exists, false otherwise.
    /// </returns>
    public boolean StreamExists(String streamPath) throws IOException, CloudException {
        try {
            _client.getFileSystemOperations().getFileStatus(streamPath, _accountName);
        } catch (CloudException cloudEx) {
            if (cloudEx.getResponse().code() == 404) {
                return false;
            }

            throw cloudEx;
        }

        return true;
    }

    /// <summary>
    /// Gets a value indicating the length of a stream, in bytes.
    /// </summary>
    /// <param name="streamPath">The relative path to the stream.</param>
    /// <returns>
    /// The length of the stream, in bytes.
    /// </returns>
    public long GetStreamLength(String streamPath) throws IOException, CloudException {
        FileStatusResult fileInfoResponse = _client.getFileSystemOperations().getFileStatus(streamPath, _accountName).getBody();
        return fileInfoResponse.getFileStatus().getLength();
    }

    /// <summary>
    /// Concatenates the given input streams (in order) into the given target stream.
    /// At the end of this operation, input streams will be deleted.
    /// </summary>
    /// <param name="targetStreamPath">The relative path to the target stream.</param>
    /// <param name="inputStreamPaths">An ordered array of paths to the input streams.</param>
    public void Concatenate(String targetStreamPath, String[] inputStreamPaths) throws IOException, CloudException {
        // this is required for the current version of the microsoft concatenate
        // TODO: Improve WebHDFS concatenate to take in the list of paths to concatenate
        // in the request body.
        String paths = "sources=" + String.join(",", inputStreamPaths);

        // For the current implementation, we require UTF8 encoding.
        _client.getFileSystemOperations().msConcat(targetStreamPath, _accountName, paths.getBytes(), true);
    }
}
