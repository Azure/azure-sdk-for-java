// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf;

import com.azure.storage.StoragePerfStressOptions;
import com.beust.jcommander.Parameter;

public class BlobPerfStressOptions extends StoragePerfStressOptions {

    @Parameter(names = { "--encryption-version" })
    private String encryptionVersion;

    public String getEncryptionVersion() {
        return encryptionVersion;
    }
}
