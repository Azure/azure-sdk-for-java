// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.util.Configuration;

import java.util.HashMap;

public abstract class MonitorQueryTestUtils {

    public static final String LOG_WORKSPACE_ID = Configuration.getGlobalConfiguration()
        .get("AZURE_MONITOR_LOGS_WORKSPACE_ID");

    public static final String LOG_RESOURCE_ID = Configuration.getGlobalConfiguration()
        .get("AZURE_MONITOR_LOGS_RESOURCE_ID");

    public static final String METRIC_RESOURCE_URI = Configuration.getGlobalConfiguration()
        .get("AZURE_MONITOR_METRICS_RESOURCE_URI");

    public static final String QUERY_STRING = "let dt = datatable (DateTime: datetime, Bool:bool, Guid: guid, Int: "
        + "int, Long:long, Double: double, String: string, Timespan: timespan, Decimal: decimal, Dynamic: dynamic)\n"
        + "[datetime(2015-12-31 23:59:59.9), false, guid(74be27de-1e4e-49d9-b579-fe0b331d3642), 12345, 1, 12345.6789,"
        + " 'string value', 10s, decimal(0.10101), dynamic({\"a\":123, \"b\":\"hello\", \"c\":[1,2,3], \"d\":{}})];"
        + "range x from 1 to 100 step 1 | extend y=1 | join kind=fullouter dt on $left.y == $right.Long";

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
