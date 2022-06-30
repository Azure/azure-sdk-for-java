// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf;

import com.azure.storage.StoragePerfStressOptions;
import com.azure.storage.blob.specialized.cryptography.EncryptionVersion;
import com.beust.jcommander.Parameter;

public class BlobPerfStressOptions extends StoragePerfStressOptions {

    @Parameter(names = { "--client-encryption" })
    private String encryptionVersion = null;

    public String getEncryptionVersion() {
        return encryptionVersion;
    }
}
