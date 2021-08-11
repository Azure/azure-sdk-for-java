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

    /**
     * Returns the URL used by the HTTP request.
     * @return The URL used by the HTTP request.
     */
    public URL getUrl() {
        return url;
    }
}
