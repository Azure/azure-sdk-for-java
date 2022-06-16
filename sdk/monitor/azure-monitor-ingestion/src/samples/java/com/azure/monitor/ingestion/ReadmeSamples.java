// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.monitor.ingestion.models.UploadLogsResult;

import java.util.List;

/**
 *
 */
public final class ReadmeSamples {

    public void createClient() {
        DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        LogsIngestionClient client = new LogsIngestionClientBuilder()
                .endpoint("<data-collection-endpoint")
                .credential(tokenCredential)
                .buildClient();
    }

    public void createAsyncClient() {
        DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        LogsIngestionAsyncClient asyncClient = new LogsIngestionClientBuilder()
                .endpoint("<data-collection-endpoint")
                .credential(tokenCredential)
                .buildAsyncClient();
    }

    public void uploadLogs() {
        DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        LogsIngestionClient client = new LogsIngestionClientBuilder()
                .endpoint("<data-collection-endpoint")
                .credential(tokenCredential)
                .buildClient();

        List<Object> logs = getLogs();
        UploadLogsResult result = client.upload("<data-collection-rule-id", "stream-name", logs);
        System.out.println("Logs upload result status " + result.getStatus());
    }

    private List<Object> getLogs() {
        return null;
    }
}
