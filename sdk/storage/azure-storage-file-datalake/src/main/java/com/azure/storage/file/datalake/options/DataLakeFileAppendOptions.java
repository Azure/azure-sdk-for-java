// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.options;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.CoreUtils;

/**
 * Optional parameters for appending data to a file.
 */
@Fluent
public class DataLakeFileAppendOptions {

    private String leaseId;
    private byte[] contentHash;
    private Boolean flush;


    /**
     * Gets the lease ID to access the file.
     *
     * @return lease ID to access this file.
     */
    public String getLeaseId() {
        return leaseId;
    }

    /**
     * Sets the lease ID.
     *
     * @param leaseId The lease ID.
     * @return the updated DataLakeFileAppendOptions object.
     */
    public DataLakeFileAppendOptions setLeaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }

    /**
     * When this header is specified, the storage service compares the hash of the content that has arrived with this
     * header value. If the two hashes do not match, the operation will fail with error code 400 (Bad Request). Note
     * that this MD5 hash is not stored with the file. This header is associated with the request content, and not with
     * the stored content of the file itself.
     *
     * @return MD5 hash of the content of the data.
     */
    public byte[] getContentHash() {
        return CoreUtils.clone(this.contentHash);
    }

    /**
     * This hash is used to verify the integrity of the request content during transport. When this header is specified,
     * the storage service compares the hash of the content that has arrived with this header value. If the two hashes
     * do not match, the operation will fail with error code 400 (Bad Request). Note that this MD5 hash is not stored
     * with the file. This header is associated with the request content, and not with the stored content of the file itself.
     *
     * @param contentHash contentMd5 An MD5 hash of the content of the data. If specified, the service will calculate
     * the MD5 of the received data and fail the request if it does not match the provided MD5.
     * @return the updated DataLakeFileAppendOptions object.
     */
    public DataLakeFileAppendOptions setContentHash(byte[] contentHash) {
        this.contentHash = CoreUtils.clone(contentHash);
        return this;
    }

    /**
     * Returns whether file will be flushed after the append.
     *
     * @return the boolean flag for flush.
     */
    public Boolean getFlush() {
        return flush;
    }

    /**
     * If true, the file will be flushed after the append.
     *
     * @param flush boolean flag to indicate whether file should be flushed.
     * @return the updated DataLakeFileAppendOptions object.
     */
    public DataLakeFileAppendOptions setFlush(Boolean flush) {
        this.flush = flush;
        return this;
    }
}
