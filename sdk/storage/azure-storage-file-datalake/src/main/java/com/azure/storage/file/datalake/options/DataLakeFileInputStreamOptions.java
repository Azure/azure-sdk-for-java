// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.file.datalake.models.ConsistentReadControl;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.FileRange;

/**
 * Extended options that may be passed when opening a blob input stream.
 */
@Fluent
public class DataLakeFileInputStreamOptions {

    private FileRange range;
    private DataLakeRequestConditions requestConditions;
    private Integer blockSize;
    private ConsistentReadControl consistentReadControl;

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
    public DataLakeFileInputStreamOptions setRange(FileRange range) {
        this.range = range;
        return this;
    }

    /**
     * @return {@link DataLakeRequestConditions}
     */
    public DataLakeRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * @param requestConditions {@link DataLakeRequestConditions}
     * @return The updated options.
     */
    public DataLakeFileInputStreamOptions setRequestConditions(DataLakeRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }

    /**
     * @return The size of each data chunk returned from the service. If block size is large, input stream will make
     * fewer network calls, but each individual call will send more data and will therefore take longer.
     * The default value is 4 MB.
     */
    public Integer getBlockSize() {
        return blockSize;
    }

    /**
     * @param blockSize The size of each data chunk returned from the service. If block size is large, input stream
     * will make fewer network calls, but each individual call will send more data and will therefore take longer.
     * The default value is 4 MB.
     * @return The updated options.
     */
    public DataLakeFileInputStreamOptions setBlockSize(Integer blockSize) {
        this.blockSize = blockSize;
        return this;
    }

    /**
     * @return {@link ConsistentReadControl} Default is E-Tag.
     */
    public ConsistentReadControl getConsistentReadControl() {
        return consistentReadControl;
    }

    /**
     * @param consistentReadControl {@link ConsistentReadControl} Default is E-Tag.
     * @return The updated options.
     */
    public DataLakeFileInputStreamOptions setConsistentReadControl(ConsistentReadControl consistentReadControl) {
        this.consistentReadControl = consistentReadControl;
        return this;
    }
}
