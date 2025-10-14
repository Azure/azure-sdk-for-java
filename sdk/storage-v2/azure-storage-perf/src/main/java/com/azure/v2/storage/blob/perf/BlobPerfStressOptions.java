// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.storage.blob.perf;

import com.beust.jcommander.Parameter;

public class BlobPerfStressOptions extends StoragePerfStressOptions {

    @Parameter(names = { "--client-encryption" })
    private String clientEncryption = null;

    // Does nothing, is just a sentinel value for the perf pipeline to run get-properties without being hacky.
    @Parameter(names = { "--get-properties" })
    private boolean getProperties = false;

    public String getClientEncryption() {
        return clientEncryption;
    }
}
