// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.Configuration;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.monitor.query.models.LogsBatchQuery;

public class LargeQuerySample {
    
    public static void main(String[] args) {

        String LOG_WORKSPACE_ID = "{workspace-id}";

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

    public static LogsBatchQuery getBatchQueryByRowCount(String query, int maxRowCountPerQuery) {
        return getBatchQueryByRowCount(query, maxRowCountPerQuery, "TimeGenerated");
    }

    public static LogsBatchQuery getBatchQueryByRowCount(String query, int maxRowCountPerQuery, String timeColumn) {

    }
}
