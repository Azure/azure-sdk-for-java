// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import java.util.List;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.Configuration;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.monitor.query.models.LogsBatchQuery;
import com.azure.monitor.query.models.QueryTimeInterval;

public class LargeQuerySample {


    static String workspaceId = "{workspace-id}";

    public static void main(String[] args) {

        

        ClientSecretCredential tokenCredential = new ClientSecretCredentialBuilder()
                .clientId(Configuration.getGlobalConfiguration().get("AZURE_MONITOR_CLIENT_ID"))
                .clientSecret(Configuration.getGlobalConfiguration().get("AZURE_MONITOR_CLIENT_SECRET"))
                .tenantId(Configuration.getGlobalConfiguration().get("AZURE_TENANT_ID"))
                .build();

        LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
                .credential(tokenCredential)
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .buildClient();

        
        
    }

    static LogsBatchQuery getBatchQueryByRowCount(LogsQueryClient client, String query, int maxRowCountPerQuery) {
        return getBatchQueryByRowCount(client, query, maxRowCountPerQuery, "TimeGenerated");
    }

    static LogsBatchQuery getBatchQueryByRowCount(LogsQueryClient client, String query, int maxRowCountPerQuery, String timeColumn) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    static List<String> getBatchEndpoints(LogsQueryClient client, String originalQuery, QueryTimeInterval timeInterval,
        int maxRowCountPerQuery, String timeColumn) {
        
        String findBatchEndpointsQuery = String.format(
            "%1$s | sort by %2$s desc | extend batch_num = row_cumsum(1) / %3$s | summarize endpoint=mins(%2$s) by batch_num | sort by batch_num asc | project endpoint", 
            originalQuery,
            timeColumn,
            maxRowCountPerQuery);

        client.queryWorkspace(workspaceId, findBatchEndpointsQuery, timeInterval, String.class);

        throw new UnsupportedOperationException("Not implemented yet");
        
    } 
}
