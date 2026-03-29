// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.stress;

import com.azure.storage.common.StorageChecksumAlgorithm;
import com.azure.storage.stress.StorageStressOptions;
import com.beust.jcommander.Parameter;

/**
 * Options for stress scenarios that enable transactional request content validation on uploads
 * (CRC64 / structured message). See {@link com.azure.storage.blob.BlobContentValidationUploadTests}.
 */
public class ContentValidationStressOptions extends StorageStressOptions {
    /**
     * Request checksum behavior for upload APIs. Use CRC64 or AUTO to exercise content validation.
     * MD5 is not supported for uploadFromFile. NONE disables request validation.
     */
    @Parameter(names = { "--requestChecksumAlgorithm" },
        description = "CRC64 (default), AUTO, NONE, or MD5 (not valid for upload-from-file scenarios)")
    private StorageChecksumAlgorithm requestChecksumAlgorithm = StorageChecksumAlgorithm.CRC64;

    public StorageChecksumAlgorithm getRequestChecksumAlgorithm() {
        return requestChecksumAlgorithm;
    }
}
