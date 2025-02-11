// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.http;

import com.azure.cosmos.implementation.Configs;

// [TODO Http2]: when adding public API, making this class public, add setter method
public class Http2ConnectionConfig {
    private int maxConnectionPoolSize;
    private int minConnectionPoolSize;
    private int maxConcurrentStreams;
    private boolean enabled;

    public Http2ConnectionConfig() {
        this.maxConnectionPoolSize = Configs.getHttp2MaxConnectionPoolSize();
        this.minConnectionPoolSize = Configs.getHttp2MinConnectionPoolSize();
        this.maxConcurrentStreams = Configs.getHttp2MaxConcurrentStreams();
        this.enabled = Configs.isHttp2Enabled();
    }

    public int getMaxConnectionPoolSize() {
        return maxConnectionPoolSize;
    }

    public int getMinConnectionPoolSize() {
        return minConnectionPoolSize;
    }

    public int getMaxConcurrentStreams() {
        return maxConcurrentStreams;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
