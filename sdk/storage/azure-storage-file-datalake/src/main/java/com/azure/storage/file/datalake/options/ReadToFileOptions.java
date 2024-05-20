// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.options;

import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.file.datalake.DataLakeFileClient;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.DownloadRetryOptions;
import com.azure.storage.file.datalake.models.FileRange;

import java.io.UncheckedIOException;
import java.nio.file.OpenOption;
import java.util.Set;


/**
 * Parameters when calling readToFile() on {@link DataLakeFileClient}
 */
public class ReadToFileOptions {
    private final String filePath;
    private FileRange range;
    private ParallelTransferOptions parallelTransferOptions;
    private DownloadRetryOptions downloadRetryOptions;
    private DataLakeRequestConditions dataLakeRequestConditions;
    private Boolean rangeGetContentMd5;
    private Set<OpenOption> openOptions;
    private Boolean userPrincipalName;

    /**
     * Constructs a {@link ReadToFileOptions}.
     *
     * @param filePath Path of the file to download to.
     */
    public ReadToFileOptions(String filePath) {
        StorageImplUtils.assertNotNull("filePath", filePath);
        this.filePath = filePath;
    }

    /**
     * @return A {@link String} representing the filePath where the downloaded data will be written.
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * @return {@link FileRange}
     */
    public FileRange getRange() {
        return range;
    }

    /**
     * @param range {@link FileRange}
     * @return The updated options.
     */
    public ReadToFileOptions setRange(FileRange range) {
        this.range = range;
        return this;
    }

    /**
     * @return {@link ParallelTransferOptions} to use to download to file. Number of parallel
     * transfers parameter is ignored.
     */
    public ParallelTransferOptions getParallelTransferOptions() {
        return parallelTransferOptions;
    }

    /**
     * @param parallelTransferOptions {@link ParallelTransferOptions} to use to download to file. Number of parallel
     * transfers parameter is ignored.
     * @return The updated options.
     */
    public ReadToFileOptions setParallelTransferOptions(ParallelTransferOptions parallelTransferOptions) {
        this.parallelTransferOptions = parallelTransferOptions;
        return this;
    }

    /**
     * @return {@link DownloadRetryOptions}
     */
    public DownloadRetryOptions getDownloadRetryOptions() {
        return downloadRetryOptions;
    }

    /**
     * @param downloadRetryOptions {@link DownloadRetryOptions}
     * @return The updated options.
     */
    public ReadToFileOptions setDownloadRetryOptions(DownloadRetryOptions downloadRetryOptions) {
        this.downloadRetryOptions = downloadRetryOptions;
        return this;
    }

    /**
     * @return requestConditions {@link DataLakeRequestConditions}
     */
    public DataLakeRequestConditions getDataLakeRequestConditions() {
        return dataLakeRequestConditions;
    }

    /**
     * @param dataLakeRequestConditions {@link DataLakeRequestConditions}
     * @return The updated options.
     * @throws UncheckedIOException If an I/O error occurs.
     */
    public ReadToFileOptions setDataLakeRequestConditions(DataLakeRequestConditions dataLakeRequestConditions) {
        this.dataLakeRequestConditions = dataLakeRequestConditions;
        return this;
    }

    /**
     * @return Whether the contentMD5 for the specified file range should be returned.
     */
    public Boolean isRangeGetContentMd5() {
        return rangeGetContentMd5;
    }

    /**
     * @param rangeGetContentMd5 Whether the contentMD5 for the specified file range should be returned.
     * @return The updated options.
     */
    public ReadToFileOptions setRangeGetContentMd5(Boolean rangeGetContentMd5) {
        this.rangeGetContentMd5 = rangeGetContentMd5;
        return this;
    }

    /**
     * @return {@link OpenOption OpenOptions} to use to configure how to open or create the file.
     */
    public Set<OpenOption> getOpenOptions() {
        return openOptions;
    }

    /**
     * @param openOptions {@link OpenOption OpenOptions} to use to configure how to open or create the file.
     * @return The updated options.
     */
    public ReadToFileOptions setOpenOptions(Set<OpenOption> openOptions) {
        this.openOptions = openOptions;
        return this;
    }

    /**
     * @return The value for the x-ms-upn header.
     */
    public Boolean isUserPrincipalName() {
        return userPrincipalName;
    }

    /**
     * @param userPrincipalName The value for the x-ms-upn header.
     * @return The updated options.
     */
    public ReadToFileOptions setUserPrincipalName(Boolean userPrincipalName) {
        this.userPrincipalName = userPrincipalName;
        return this;
    }
}
