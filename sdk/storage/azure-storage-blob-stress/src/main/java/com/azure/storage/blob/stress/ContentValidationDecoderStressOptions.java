// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.stress;

import com.azure.storage.common.StorageChecksumAlgorithm;
import com.azure.storage.stress.StorageStressOptions;
import com.beust.jcommander.Parameter;

/**
 * Options for stress scenarios that enable transactional response content validation on downloads
 * (CRC64 / structured message). See {@link com.azure.storage.blob.BlobMessageDecoderDownloadTests}.
 */
public class ContentValidationDecoderStressOptions extends StorageStressOptions {
    /**
     * Response checksum behavior for download APIs. Use CRC64 or AUTO to exercise content validation.
     * NONE disables response validation.
     */
    @Parameter(names = { "--responseChecksumAlgorithm" },
        description = "CRC64 (default), AUTO, NONE, or MD5")
    private StorageChecksumAlgorithm responseChecksumAlgorithm = StorageChecksumAlgorithm.CRC64;

    public StorageChecksumAlgorithm getResponseChecksumAlgorithm() {
        return responseChecksumAlgorithm;
    }
}
