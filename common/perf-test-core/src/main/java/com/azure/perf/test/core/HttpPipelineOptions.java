// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

import com.beust.jcommander.Parameter;

import java.net.URL;

public class HttpPipelineOptions extends PerfStressOptions {
    @Parameter(names = { "-u", "--url" }, description = "URL to fetch", required = true)
    private URL url;

    public URL getUrl() {
        return url;
    }
}
