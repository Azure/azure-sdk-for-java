// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.util.Configuration;

import java.util.HashMap;

public abstract class MonitorQueryTestUtils {

    public static final HashMap<String, String> ENDPOINTS = new HashMap<String, String>() {
        {
            put("AzureCloud", "https://api.loganalytics.io/v1");
            put("AzureChinaCloud", "https://api.loganalytics.azure.cn/v1");
            put("AzureUSGovernment", "https://api.loganalytics.us/v1");
        }
    };

    public static String getLogEndpoint() {
        return ENDPOINTS.get(Configuration.getGlobalConfiguration().get("MONITOR_ENVIRONMENT"));
    }

    public static String getMetricEndpoint() {
        return Configuration.getGlobalConfiguration().get("MONITOR_RESOURCE_MANAGER_URL");
    }
}
