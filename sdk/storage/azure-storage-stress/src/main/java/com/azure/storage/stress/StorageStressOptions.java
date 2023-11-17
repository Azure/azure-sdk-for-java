// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.stress;

import com.azure.core.util.Configuration;
import com.azure.perf.test.core.PerfStressOptions;
import com.beust.jcommander.Parameter;

import java.util.UUID;

public class StorageStressOptions extends PerfStressOptions {
    @Parameter(names = { "--faults" }, description = "Enable fault injection")
    private boolean enableFaultInjection = false;
    @Parameter(names = { "--timeout" }, description = "Operation timeout in seconds")
    private int timeoutInSeconds = 60;
    @Parameter(names = { "--cs"}, description = "Storage connection string")
    private String connectionString = Configuration.getGlobalConfiguration().get("STORAGE_CONNECTION_STRING");
    @Parameter(names = { "--blob-name"}, description = "Blob name")
    private String blobName = generateBlobPrefix();

    public boolean isFaultInjectionEnabled() {
        return enableFaultInjection;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public int getTimeoutSeconds() {
        return timeoutInSeconds;
    }

    public String getBlobName() {
        return blobName;
    }

    private static String generateBlobPrefix() {
        return "blob-" + UUID.randomUUID();
    }
}
