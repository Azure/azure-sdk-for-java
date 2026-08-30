// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.DownloadRetryOptions;
import com.azure.storage.file.datalake.models.FileRange;
import com.azure.storage.common.DataLocalityEndpoint;

/**
 * Extended options that may be passed when reading a file range to an output stream.
 */
@Fluent
public final class FileReadOptions {
    private FileRange range;
    private DownloadRetryOptions downloadRetryOptions;
    private DataLakeRequestConditions requestConditions;
    private boolean retrieveContentRangeMd5;
    private DataLocalityEndpoint dataLocalityEndpoint;
    private Boolean userPrincipalName;

    /**
     * Creates a new instance of {@link FileReadOptions}.
     */
    public FileReadOptions() {
    }

    /**
     * Gets the {@link FileRange}.
     *
     * @return The file range.
     */
    public FileRange getRange() {
        return range;
    }

    /**
     * Sets the {@link FileRange}.
     *
     * @param range The file range.
     * @return The updated options.
     */
    public FileReadOptions setRange(FileRange range) {
        this.range = range;
        return this;
    }

    /**
     * Gets the {@link DownloadRetryOptions}.
     *
     * @return The download retry options.
     */
    public DownloadRetryOptions getDownloadRetryOptions() {
        return downloadRetryOptions;
    }

    /**
     * Sets the {@link DownloadRetryOptions}.
     *
     * @param downloadRetryOptions The download retry options.
     * @return The updated options.
     */
    public FileReadOptions setDownloadRetryOptions(DownloadRetryOptions downloadRetryOptions) {
        this.downloadRetryOptions = downloadRetryOptions;
        return this;
    }

    /**
     * Gets the {@link DataLakeRequestConditions}.
     *
     * @return The request conditions.
     */
    public DataLakeRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * Sets the {@link DataLakeRequestConditions}.
     *
     * @param requestConditions The request conditions.
     * @return The updated options.
     */
    public FileReadOptions setRequestConditions(DataLakeRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }

    /**
     * Gets whether the content MD5 for the specified file range should be returned.
     *
     * @return Whether to retrieve content range MD5.
     */
    public boolean isRetrieveContentRangeMd5() {
        return retrieveContentRangeMd5;
    }

    /**
     * Sets whether the content MD5 for the specified file range should be returned.
     *
     * @param retrieveContentRangeMd5 Whether to retrieve content range MD5.
     * @return The updated options.
     */
    public FileReadOptions setRetrieveContentRangeMd5(boolean retrieveContentRangeMd5) {
        this.retrieveContentRangeMd5 = retrieveContentRangeMd5;
        return this;
    }

    /**
     * Gets the endpoint to use for this download, when selected from file layout information.
     *
     * @return The data locality endpoint, or {@code null} if the client's configured endpoint should be used.
     */
    public DataLocalityEndpoint getDataLocalityEndpoint() {
        return dataLocalityEndpoint;
    }

    /**
     * Sets the endpoint to use for this download.
     *
     * @param dataLocalityEndpoint The endpoint selected from file layout information.
     * @return The updated options.
     */
    public FileReadOptions setDataLocalityEndpoint(DataLocalityEndpoint dataLocalityEndpoint) {
        this.dataLocalityEndpoint = dataLocalityEndpoint;
        return this;
    }

    /**
     * Gets the value for the x-ms-upn header.
     *
     * @return The value for the x-ms-upn header.
     */
    public Boolean isUserPrincipalName() {
        return userPrincipalName;
    }

    /**
     * Sets the value for the x-ms-upn header.
     *
     * @param userPrincipalName The value for the x-ms-upn header.
     * @return The updated options.
     */
    public FileReadOptions setUserPrincipalName(Boolean userPrincipalName) {
        this.userPrincipalName = userPrincipalName;
        return this;
    }
}
