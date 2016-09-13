/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.datalake.store.uploader;

import com.microsoft.azure.management.datalake.store.models.AdlsErrorException;
import com.microsoft.rest.RestException;
import com.microsoft.azure.management.datalake.store.implementation.DataLakeStoreFileSystemManagementClientImpl;
import com.microsoft.azure.management.datalake.store.models.FileStatusResult;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;

/**
 * A front end adapter that communicates with the DataLake Store.
 * This is a syncrhonous call adapter, which has certain efficiency limitations.
 * In the future, new adapters that are created should consider implementing the methods
 * asynchronously.
 */
public class DataLakeStoreFrontEndAdapterImpl implements FrontEndAdapter {

    private String accountName;

    private DataLakeStoreFileSystemManagementClientImpl client;

    /**
     * Initializes a new instance of the DataLakeStoreFrontEndAdapter adapter.
     *
     * @param accountName The Data Lake Store account name associated with this adapter
     * @param client the {@link DataLakeStoreFileSystemManagementClientImpl} used by this adapter
     */
    public DataLakeStoreFrontEndAdapterImpl(String accountName, DataLakeStoreFileSystemManagementClientImpl client) {
        this.accountName = accountName;
        this.client = client;
    }

    /**
     * Creates a new, empty stream at the given path.
     *
     * @param streamPath The relative path to the stream.
     * @param overwrite  Whether to overwrite an existing stream.
     * @param data Optionally pass in data to add to the stream during creation. If null is passed in an empty stream is created
     * @param byteCount If data is passed in, indicates how many bytes of the data passed in should be pushed into the stream
     * @throws IOException if the file does not exist or is inaccessible.
     * @throws RestException if there is a failure communicating with the service.
     */
    public void createStream(String streamPath, boolean overwrite, byte[] data, int byteCount) throws RestException, IOException {
        byte[] toCreate;
        if (data == null) {
            toCreate = new byte[0];
        } else {
            toCreate = new byte[byteCount];
            System.arraycopy(data, 0, toCreate, 0, byteCount);
        }
        client.fileSystems().create(accountName, streamPath, toCreate, overwrite);
    }

    /**
     * Deletes an existing stream at the given path.
     *
     * @param streamPath The relative path to the stream.
     * @param recurse    if set to true recursively delete. This is used for folder streams only.
     * @throws IOException if the file does not exist or is inaccessible.
     * @throws RestException if there is a failure communicating with the service.
     */
    public void deleteStream(String streamPath, boolean recurse) throws IOException, RestException {
        client.fileSystems().delete(accountName, streamPath, recurse);
    }

    /**
     * Appends to stream.
     *
     * @param streamPath The relative path to the stream.
     * @param data The data to append to the stream
     * @param offset This parameter is unused by this implementation, and any value put here is ignored
     * @param byteCount The number of bytes from the data stream to append (starting at offset 0 of data).
     * @throws IOException if the file does not exist or is inaccessible.
     * @throws RestException if there is a failure communicating with the service.
     */
    public void appendToStream(String streamPath, byte[] data, long offset, int byteCount) throws IOException, RestException {
        byte[] toAppend = new byte[byteCount];
        System.arraycopy(data, 0, toAppend, 0, byteCount);
        client.fileSystems().append(accountName, streamPath, toAppend);
    }

    /**
     * Determines if the stream with given path exists.
     *
     * @param streamPath The relative path to the stream.
     * @return True if the stream exists, false otherwise.
     * @throws IOException if the file does not exist or is inaccessible.
     * @throws RestException if there is a failure communicating with the service.
     */
    public boolean streamExists(String streamPath) throws RestException, IOException {
        try {
            client.fileSystems().getFileStatus(accountName, streamPath);
        } catch (AdlsErrorException cloudEx) {
            if (cloudEx.getResponse().code() == 404) {
                return false;
            }

            throw cloudEx;
        }

        return true;
    }

    /**
     * Gets a value indicating the length of a stream, in bytes.
     *
     * @param streamPath The relative path to the stream.
     * @return The length of the stream, in bytes.
     * @throws IOException if the file does not exist or is inaccessible.
     * @throws RestException if there is a failure communicating with the service.
     */
    public long getStreamLength(String streamPath) throws IOException, RestException {
        FileStatusResult fileInfoResponse = client.fileSystems().getFileStatus(accountName, streamPath).getBody();
        return fileInfoResponse.fileStatus().length();
    }

    /**
     * Concatenates the given input streams (in order) into the given target stream.
     * At the end of this operation, input streams will be deleted.
     *
     * @param targetStreamPath The relative path to the target stream.
     * @param inputStreamPaths An ordered array of paths to the input streams to concatenate into the target stream.
     * @throws IOException if the file does not exist or is inaccessible.
     * @throws RestException if there is a failure communicating with the service.
     */
    public void concatenate(String targetStreamPath, String[] inputStreamPaths) throws IOException, RestException {
        // this is required for the current version of the microsoft concatenate
        // TODO: Improve WebHDFS concatenate to take in the list of paths to concatenate
        // in the request body.
        String paths = MessageFormat.format("sources={0}", StringUtils.join(inputStreamPaths, ','));

        // For the current implementation, we require UTF8 encoding.
        client.fileSystems().msConcat(accountName, targetStreamPath, paths.getBytes(StandardCharsets.UTF_8), true);
    }
}
