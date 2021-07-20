// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf;

import com.azure.perf.test.core.PerfStressOptions;
import com.beust.jcommander.Parameter;

public class UploadBlobPerfStressOptions extends PerfStressOptions {

    @Parameter(names = { "--transfer-single-upload-size" })
    private Long transferSingleUploadSize;

    @Parameter(names = { "--transfer-block-size" })
    private Long transferBlockSize;

    @Parameter(names = { "--transfer-concurrency" })
    private Integer transferConcurrency;

    public Long getTransferSingleUploadSize() {
        return transferSingleUploadSize;
    }

    public Long getTransferBlockSize() {
        return transferBlockSize;
    }

    public Integer getTransferConcurrency() {
        return transferConcurrency;
    }
}
