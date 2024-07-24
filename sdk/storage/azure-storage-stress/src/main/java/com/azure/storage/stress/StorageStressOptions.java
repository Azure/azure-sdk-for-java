// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.stress;

import com.azure.core.util.Configuration;
import com.azure.perf.test.core.PerfStressOptions;
import com.beust.jcommander.Parameter;

public class StorageStressOptions extends PerfStressOptions {
    @Parameter(names = { "--downloadFaults" }, description = "Enable fault injection for downloads")
    private boolean enableFaultInjectionDownloads = false;
    @Parameter(names = { "--uploadFaults" }, description = "Enable fault injection for uploads")
    private boolean enableFaultInjectionUploads = false;
    @Parameter(names = { "--endpoint"}, description = "Storage endpoint string")
    private String endpointString = Configuration.getGlobalConfiguration().get("STORAGE_ENDPOINT_STRING");
    @Parameter(names = { "--pbEndpoint"}, description = "Page Blob Storage endpoint string")
    private String pageBlobEndpointString = Configuration.getGlobalConfiguration().get("PAGE_BLOB_STORAGE_ENDPOINT_STRING");

    public boolean isFaultInjectionEnabledForDownloads() {
        return enableFaultInjectionDownloads;
    }

    /**
     * If fault injection is enabled, this flag will be used to determine if the request should be faulted.
     * If fault injection is not enabled, this flag will be ignored.
     * True: The request will be faulted. False: The response will be faulted. Default is false.
     * @return whether the request is faulted.
     */
    public boolean isFaultInjectionEnabledForUploads() {
        return enableFaultInjectionUploads;
    }

    public String getEndpointString() {
        return endpointString;
    }

    public String getPageBlobEndpointString() {
        return pageBlobEndpointString;
    }
}
