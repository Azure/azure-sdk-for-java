// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.stress;

import com.azure.core.util.Configuration;
import com.azure.perf.test.core.PerfStressOptions;
import com.beust.jcommander.Parameter;

public class StorageStressOptions extends PerfStressOptions {
    @Parameter(names = { "--faults" }, description = "Enable fault injection")
    private boolean enableFaultInjection = false;
    @Parameter(names = { "--cs"}, description = "Storage connection string")
    private String connectionString = Configuration.getGlobalConfiguration().get("STORAGE_CONNECTION_STRING");
    @Parameter(names = { "--pbcs"}, description = "Page Blob Storage connection string")
    private String pageBlobConnectionString = Configuration.getGlobalConfiguration().get("PAGE_BLOB_STORAGE_CONNECTION_STRING");
    @Parameter(names = { "--requestFaulted" }, description = "Is request faulted")
    private boolean isRequestFaulted = false;

    public boolean isFaultInjectionEnabled() {
        return enableFaultInjection;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public String getPageBlobConnectionString() {
        return pageBlobConnectionString;
    }

    /**
     * If fault injection is enabled, this flag will be used to determine if the request should be faulted.
     * If fault injection is not enabled, this flag will be ignored.
     * True: The request will be faulted. False: The response will be faulted. Default is false.
     * @return whether the request is faulted.
     */
    public boolean isRequestFaulted() {
        return isRequestFaulted;
    }
}
