// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

import com.beust.jcommander.Parameter;

import java.net.URL;

/**
 * Perf testing options for {@link HttpPipelineTest}.
 */
public class HttpPipelineOptions extends PerfStressOptions {
    @Parameter(names = { "-u", "--url" }, description = "URL to fetch", required = true)
    private URL url;

    @Parameter(names = { "--first-run-extra-requests" },  description = "Extra requests to send on first run. "
        + "Simulates SDKs which require extra requests (like authentication) on first API call.")
    private int firstRunExtraRequests = 0;

    /**
     * Returns the URL used by the HTTP request.
     * @return The URL used by the HTTP request.
     */
    public URL getUrl() {
        return url;
    }

    /**
     * Returns the extra requests to send on first run.
     * @return The extra requests to send on first run.
     */
    public int getFirstRunExtraRequests() {
        return firstRunExtraRequests;
    }
}
