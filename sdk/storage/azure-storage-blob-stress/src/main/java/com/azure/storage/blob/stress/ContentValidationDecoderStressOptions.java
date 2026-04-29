// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.stress;

import com.azure.storage.common.ContentValidationAlgorithm;
import com.azure.storage.stress.StorageStressOptions;
import com.beust.jcommander.Parameter;

/**
 * Options for stress scenarios that enable transactional response content validation on downloads
 * (CRC64 / structured message). See {@link com.azure.storage.blob.BlobContentValidationDownloadTests}.
 */
public class ContentValidationDecoderStressOptions extends StorageStressOptions {
    /**
     * Response content validation behavior for download APIs. Use CRC64 or AUTO to exercise content validation.
     * NONE disables response validation.
     */
    @Parameter(names = { "--contentValidationAlgorithm" },
        description = "CRC64 (default), AUTO, or NONE")
    private ContentValidationAlgorithm contentValidationAlgorithm = ContentValidationAlgorithm.CRC64;

    public ContentValidationAlgorithm getContentValidationAlgorithm() {
        return contentValidationAlgorithm;
    }
}
